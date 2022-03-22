package sun.security.util;

import java.io.IOException;
import java.security.CodeSigner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.Manifest;
import sun.misc.BASE64Decoder;
import sun.security.provider.Sun;

public class ManifestEntryVerifier
{
  private static final Debug debug = Debug.getInstance("jar");
  private static final Provider sunProvider = new Sun();
  HashMap createdDigests = new HashMap(11);
  ArrayList digests = new ArrayList();
  ArrayList manifestHashes = new ArrayList();
  private BASE64Decoder decoder = null;
  private String name = null;
  private Manifest man;
  private boolean skip = true;
  private JarEntry entry;
  private CodeSigner[] signers = null;
  private static final char[] hexc = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  public ManifestEntryVerifier(Manifest paramManifest)
  {
    this.man = paramManifest;
  }

  public void setEntry(String paramString, JarEntry paramJarEntry)
    throws IOException
  {
    this.digests.clear();
    this.manifestHashes.clear();
    this.name = paramString;
    this.entry = paramJarEntry;
    this.skip = true;
    this.signers = null;
    if ((this.man == null) || (paramString == null))
      return;
    Attributes localAttributes = this.man.getAttributes(paramString);
    if (localAttributes == null)
    {
      localAttributes = this.man.getAttributes("./" + paramString);
      if (localAttributes == null)
      {
        localAttributes = this.man.getAttributes("/" + paramString);
        if (localAttributes == null)
          return;
      }
    }
    Iterator localIterator = localAttributes.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      String str1 = localEntry.getKey().toString();
      if (str1.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST"))
      {
        String str2 = str1.substring(0, str1.length() - 7);
        MessageDigest localMessageDigest = (MessageDigest)this.createdDigests.get(str2);
        if (localMessageDigest == null)
          try
          {
            localMessageDigest = MessageDigest.getInstance(str2, sunProvider);
            this.createdDigests.put(str2, localMessageDigest);
          }
          catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
          {
          }
        if (localMessageDigest != null)
        {
          this.skip = false;
          localMessageDigest.reset();
          this.digests.add(localMessageDigest);
          this.manifestHashes.add(this.decoder.decodeBuffer((String)localEntry.getValue()));
        }
      }
    }
  }

  public void update(byte paramByte)
  {
    if (this.skip)
      return;
    for (int i = 0; i < this.digests.size(); ++i)
      ((MessageDigest)this.digests.get(i)).update(paramByte);
  }

  public void update(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (this.skip)
      return;
    for (int i = 0; i < this.digests.size(); ++i)
      ((MessageDigest)this.digests.get(i)).update(paramArrayOfByte, paramInt1, paramInt2);
  }

  public JarEntry getEntry()
  {
    return this.entry;
  }

  public CodeSigner[] verify(Hashtable paramHashtable1, Hashtable paramHashtable2)
    throws JarException
  {
    if (this.skip)
      return null;
    if (this.signers != null)
      return this.signers;
    for (int i = 0; i < this.digests.size(); ++i)
    {
      MessageDigest localMessageDigest = (MessageDigest)this.digests.get(i);
      byte[] arrayOfByte1 = (byte[])(byte[])this.manifestHashes.get(i);
      byte[] arrayOfByte2 = localMessageDigest.digest();
      if (debug != null)
      {
        debug.println("Manifest Entry: " + this.name + " digest=" + localMessageDigest.getAlgorithm());
        debug.println("  manifest " + toHex(arrayOfByte1));
        debug.println("  computed " + toHex(arrayOfByte2));
        debug.println();
      }
      if (!(MessageDigest.isEqual(arrayOfByte2, arrayOfByte1)))
        throw new SecurityException(localMessageDigest.getAlgorithm() + " digest error for " + this.name);
    }
    this.signers = ((CodeSigner[])(CodeSigner[])paramHashtable2.remove(this.name));
    if (this.signers != null)
      paramHashtable1.put(this.name, this.signers);
    return this.signers;
  }

  static String toHex(byte[] paramArrayOfByte)
  {
    StringBuffer localStringBuffer = new StringBuffer(paramArrayOfByte.length * 2);
    for (int i = 0; i < paramArrayOfByte.length; ++i)
    {
      localStringBuffer.append(hexc[(paramArrayOfByte[i] >> 4 & 0xF)]);
      localStringBuffer.append(hexc[(paramArrayOfByte[i] & 0xF)]);
    }
    return localStringBuffer.toString();
  }
}
package sun.tools.jar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import sun.misc.BASE64Encoder;
import sun.net.www.MessageHeader;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.x509.AlgorithmId;

public class SignatureFile
{
  static final boolean debug = 0;
  private Vector entries;
  static final String[] hashes = { "SHA" };
  private Manifest manifest;
  private String rawName;
  private PKCS7 signatureBlock;
  private Hashtable digests;

  static final void debug(String paramString)
  {
  }

  private SignatureFile(String paramString)
    throws sun.tools.jar.JarException
  {
    this.entries = new Vector();
    this.digests = new Hashtable();
    this.entries = new Vector();
    if (paramString != null)
    {
      if ((paramString.length() > 8) || (paramString.indexOf(46) != -1))
        throw new sun.tools.jar.JarException("invalid file name");
      this.rawName = paramString.toUpperCase();
    }
  }

  private SignatureFile(String paramString, boolean paramBoolean)
    throws sun.tools.jar.JarException
  {
    this(paramString);
    if (paramBoolean)
    {
      MessageHeader localMessageHeader = new MessageHeader();
      localMessageHeader.set("Signature-Version", "1.0");
      this.entries.addElement(localMessageHeader);
    }
  }

  public SignatureFile(Manifest paramManifest, String paramString)
    throws sun.tools.jar.JarException
  {
    this(paramString, true);
    this.manifest = paramManifest;
    Enumeration localEnumeration = paramManifest.entries();
    while (localEnumeration.hasMoreElements())
    {
      MessageHeader localMessageHeader = (MessageHeader)localEnumeration.nextElement();
      String str = localMessageHeader.findValue("Name");
      if (str != null)
        add(str);
    }
  }

  public SignatureFile(Manifest paramManifest, String[] paramArrayOfString, String paramString)
    throws sun.tools.jar.JarException
  {
    this(paramString, true);
    this.manifest = paramManifest;
    add(paramArrayOfString);
  }

  public SignatureFile(InputStream paramInputStream, String paramString)
    throws IOException
  {
    this(paramString);
    while (paramInputStream.available() > 0)
    {
      MessageHeader localMessageHeader = new MessageHeader(paramInputStream);
      this.entries.addElement(localMessageHeader);
    }
  }

  public SignatureFile(InputStream paramInputStream)
    throws IOException
  {
    this(paramInputStream, null);
  }

  public SignatureFile(byte[] paramArrayOfByte)
    throws IOException
  {
    this(new ByteArrayInputStream(paramArrayOfByte));
  }

  public String getName()
  {
    return "META-INF/" + this.rawName + ".SF";
  }

  public String getBlockName()
  {
    String str = "DSA";
    if (this.signatureBlock != null)
    {
      SignerInfo localSignerInfo = this.signatureBlock.getSignerInfos()[0];
      str = localSignerInfo.getDigestEncryptionAlgorithmId().getName();
      str = str.substring(str.length() - 3);
    }
    return "META-INF/" + this.rawName + "." + str;
  }

  public PKCS7 getBlock()
  {
    return this.signatureBlock;
  }

  public void setBlock(PKCS7 paramPKCS7)
  {
    this.signatureBlock = paramPKCS7;
  }

  public void add(String[] paramArrayOfString)
    throws sun.tools.jar.JarException
  {
    for (int i = 0; i < paramArrayOfString.length; ++i)
      add(paramArrayOfString[i]);
  }

  public void add(String paramString)
    throws sun.tools.jar.JarException
  {
    MessageHeader localMessageHeader2;
    MessageHeader localMessageHeader1 = this.manifest.getEntry(paramString);
    if (localMessageHeader1 == null)
      throw new sun.tools.jar.JarException("entry " + paramString + " not in manifest");
    try
    {
      localMessageHeader2 = computeEntry(localMessageHeader1);
    }
    catch (IOException localIOException)
    {
      throw new sun.tools.jar.JarException(localIOException.getMessage());
    }
    this.entries.addElement(localMessageHeader2);
  }

  public MessageHeader getEntry(String paramString)
  {
    Enumeration localEnumeration = entries();
    while (localEnumeration.hasMoreElements())
    {
      MessageHeader localMessageHeader = (MessageHeader)localEnumeration.nextElement();
      if (paramString.equals(localMessageHeader.findValue("Name")))
        return localMessageHeader;
    }
    return null;
  }

  public MessageHeader entryAt(int paramInt)
  {
    return ((MessageHeader)this.entries.elementAt(paramInt));
  }

  public Enumeration entries()
  {
    return this.entries.elements();
  }

  private MessageHeader computeEntry(MessageHeader paramMessageHeader)
    throws IOException
  {
    MessageHeader localMessageHeader = new MessageHeader();
    String str = paramMessageHeader.findValue("Name");
    if (str == null)
      return null;
    localMessageHeader.set("Name", str);
    BASE64Encoder localBASE64Encoder = new BASE64Encoder();
    try
    {
      for (int i = 0; i < hashes.length; ++i)
      {
        MessageDigest localMessageDigest = getDigest(hashes[i]);
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream localPrintStream = new PrintStream(localByteArrayOutputStream);
        paramMessageHeader.print(localPrintStream);
        byte[] arrayOfByte1 = localByteArrayOutputStream.toByteArray();
        byte[] arrayOfByte2 = localMessageDigest.digest(arrayOfByte1);
        localMessageHeader.set(hashes[i] + "-Digest", localBASE64Encoder.encode(arrayOfByte2));
      }
      return localMessageHeader;
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new sun.tools.jar.JarException(localNoSuchAlgorithmException.getMessage());
    }
  }

  private MessageDigest getDigest(String paramString)
    throws NoSuchAlgorithmException
  {
    MessageDigest localMessageDigest = (MessageDigest)this.digests.get(paramString);
    if (localMessageDigest == null)
    {
      localMessageDigest = MessageDigest.getInstance(paramString);
      this.digests.put(paramString, localMessageDigest);
    }
    localMessageDigest.reset();
    return localMessageDigest;
  }

  public void stream(OutputStream paramOutputStream)
    throws IOException
  {
    MessageHeader localMessageHeader1 = (MessageHeader)this.entries.elementAt(0);
    if (localMessageHeader1.findValue("Signature-Version") == null)
      throw new sun.tools.jar.JarException("Signature file requires Signature-Version: 1.0 in 1st header");
    PrintStream localPrintStream = new PrintStream(paramOutputStream);
    localMessageHeader1.print(localPrintStream);
    for (int i = 1; i < this.entries.size(); ++i)
    {
      MessageHeader localMessageHeader2 = (MessageHeader)this.entries.elementAt(i);
      localMessageHeader2.print(localPrintStream);
    }
  }
}
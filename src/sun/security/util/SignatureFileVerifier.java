package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.CodeSigner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarException;
import java.util.jar.Manifest;
import sun.misc.BASE64Decoder;
import sun.security.jca.Providers;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.pkcs.PKCS9Attributes;
import sun.security.pkcs.SignerInfo;
import sun.security.timestamp.TimestampToken;

public class SignatureFileVerifier
{
  private static final Debug debug = Debug.getInstance("jar");
  private ArrayList signerCache;
  private static final String ATTR_DIGEST = "-DIGEST-Manifest-Main-Attributes".toUpperCase(Locale.ENGLISH);
  private PKCS7 block;
  private byte[] sfBytes;
  private String name;
  private ManifestDigester md;
  private HashMap createdDigests;
  private boolean workaround = false;
  private CertificateFactory certificateFactory = null;
  private static final char[] hexc = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  public SignatureFileVerifier(ArrayList paramArrayList, ManifestDigester paramManifestDigester, String paramString, byte[] paramArrayOfByte)
    throws IOException, CertificateException
  {
    Object localObject1 = null;
    try
    {
      localObject1 = Providers.startJarVerification();
      this.block = new PKCS7(paramArrayOfByte);
      this.sfBytes = this.block.getContentInfo().getData();
      this.certificateFactory = CertificateFactory.getInstance("X509");
    }
    finally
    {
      Providers.stopJarVerification(localObject1);
    }
    this.name = paramString.substring(0, paramString.lastIndexOf(".")).toUpperCase(Locale.ENGLISH);
    this.md = paramManifestDigester;
    this.signerCache = paramArrayList;
  }

  public boolean needSignatureFileBytes()
  {
    return (this.sfBytes == null);
  }

  public boolean needSignatureFile(String paramString)
  {
    return this.name.equalsIgnoreCase(paramString);
  }

  public void setSignatureFile(byte[] paramArrayOfByte)
  {
    this.sfBytes = paramArrayOfByte;
  }

  public static boolean isBlockOrSF(String paramString)
  {
    return ((paramString.endsWith(".SF")) || (paramString.endsWith(".DSA")) || (paramString.endsWith(".RSA")));
  }

  private MessageDigest getDigest(String paramString)
  {
    if (this.createdDigests == null)
      this.createdDigests = new HashMap();
    MessageDigest localMessageDigest = (MessageDigest)this.createdDigests.get(paramString);
    if (localMessageDigest == null)
      try
      {
        localMessageDigest = MessageDigest.getInstance(paramString);
        this.createdDigests.put(paramString, localMessageDigest);
      }
      catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
      {
      }
    return localMessageDigest;
  }

  public void process(Hashtable paramHashtable)
    throws IOException, SignatureException, NoSuchAlgorithmException, JarException, CertificateException
  {
    Object localObject1 = null;
    try
    {
      localObject1 = Providers.startJarVerification();
      processImpl(paramHashtable);
    }
    finally
    {
      Providers.stopJarVerification(localObject1);
    }
  }

  private void processImpl(Hashtable paramHashtable)
    throws IOException, SignatureException, NoSuchAlgorithmException, JarException, CertificateException
  {
    Manifest localManifest = new Manifest();
    localManifest.read(new ByteArrayInputStream(this.sfBytes));
    String str1 = localManifest.getMainAttributes().getValue(Attributes.Name.SIGNATURE_VERSION);
    if ((str1 == null) || (!(str1.equalsIgnoreCase("1.0"))))
      return;
    SignerInfo[] arrayOfSignerInfo = this.block.verify(this.sfBytes);
    if (arrayOfSignerInfo == null)
      throw new SecurityException("cannot verify signature block file " + this.name);
    BASE64Decoder localBASE64Decoder = new BASE64Decoder();
    CodeSigner[] arrayOfCodeSigner = getSigners(arrayOfSignerInfo, this.block);
    if (arrayOfCodeSigner == null)
      return;
    Iterator localIterator = localManifest.getEntries().entrySet().iterator();
    boolean bool = verifyManifestHash(localManifest, this.md, localBASE64Decoder);
    if ((!(bool)) && (!(verifyManifestMainAttrs(localManifest, this.md, localBASE64Decoder))))
      throw new SecurityException("Invalid signature file digest for Manifest main attributes");
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      String str2 = (String)localEntry.getKey();
      if ((bool) || (verifySection((Attributes)localEntry.getValue(), str2, this.md, localBASE64Decoder)))
      {
        if (str2.startsWith("./"))
          str2 = str2.substring(2);
        if (str2.startsWith("/"))
          str2 = str2.substring(1);
        updateSigners(arrayOfCodeSigner, paramHashtable, str2);
        if (debug != null)
          debug.println("processSignature signed name = " + str2);
      }
      else if (debug != null)
      {
        debug.println("processSignature unsigned name = " + str2);
      }
    }
  }

  private boolean verifyManifestHash(Manifest paramManifest, ManifestDigester paramManifestDigester, BASE64Decoder paramBASE64Decoder)
    throws IOException
  {
    Attributes localAttributes = paramManifest.getMainAttributes();
    int i = 0;
    Iterator localIterator = localAttributes.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      String str1 = localEntry.getKey().toString();
      if (str1.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST-MANIFEST"))
      {
        String str2 = str1.substring(0, str1.length() - 16);
        MessageDigest localMessageDigest = getDigest(str2);
        if (localMessageDigest != null)
        {
          byte[] arrayOfByte1 = paramManifestDigester.manifestDigest(localMessageDigest);
          byte[] arrayOfByte2 = paramBASE64Decoder.decodeBuffer((String)localEntry.getValue());
          if (debug != null)
          {
            debug.println("Signature File: Manifest digest " + localMessageDigest.getAlgorithm());
            debug.println("  sigfile  " + toHex(arrayOfByte2));
            debug.println("  computed " + toHex(arrayOfByte1));
            debug.println();
          }
          if (MessageDigest.isEqual(arrayOfByte1, arrayOfByte2))
            i = 1;
        }
      }
    }
    return i;
  }

  private boolean verifyManifestMainAttrs(Manifest paramManifest, ManifestDigester paramManifestDigester, BASE64Decoder paramBASE64Decoder)
    throws IOException
  {
    Attributes localAttributes = paramManifest.getMainAttributes();
    int i = 1;
    Iterator localIterator = localAttributes.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      String str1 = localEntry.getKey().toString();
      if (str1.toUpperCase(Locale.ENGLISH).endsWith(ATTR_DIGEST))
      {
        String str2 = str1.substring(0, str1.length() - ATTR_DIGEST.length());
        MessageDigest localMessageDigest = getDigest(str2);
        if (localMessageDigest != null)
        {
          ManifestDigester.Entry localEntry1 = paramManifestDigester.get("Manifest-Main-Attributes", false);
          byte[] arrayOfByte1 = localEntry1.digest(localMessageDigest);
          byte[] arrayOfByte2 = paramBASE64Decoder.decodeBuffer((String)localEntry.getValue());
          if (debug != null)
          {
            debug.println("Signature File: Manifest Main Attributes digest " + localMessageDigest.getAlgorithm());
            debug.println("  sigfile  " + toHex(arrayOfByte2));
            debug.println("  computed " + toHex(arrayOfByte1));
            debug.println();
          }
          if (MessageDigest.isEqual(arrayOfByte1, arrayOfByte2))
            continue;
          i = 0;
          if (debug == null)
            break;
          debug.println("Verification of Manifest main attributes failed");
          debug.println();
          break;
        }
      }
    }
    return i;
  }

  private boolean verifySection(Attributes paramAttributes, String paramString, ManifestDigester paramManifestDigester, BASE64Decoder paramBASE64Decoder)
    throws IOException
  {
    int i = 0;
    ManifestDigester.Entry localEntry = paramManifestDigester.get(paramString, this.block.isOldStyle());
    if (localEntry == null)
      throw new SecurityException("no manifiest section for signature file entry " + paramString);
    if (paramAttributes != null)
    {
      Iterator localIterator = paramAttributes.entrySet().iterator();
      while (localIterator.hasNext())
      {
        Map.Entry localEntry1 = (Map.Entry)localIterator.next();
        String str1 = localEntry1.getKey().toString();
        if (str1.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST"))
        {
          String str2 = str1.substring(0, str1.length() - 7);
          MessageDigest localMessageDigest = getDigest(str2);
          if (localMessageDigest != null)
          {
            byte[] arrayOfByte2;
            int j = 0;
            byte[] arrayOfByte1 = paramBASE64Decoder.decodeBuffer((String)localEntry1.getValue());
            if (this.workaround)
              arrayOfByte2 = localEntry.digestWorkaround(localMessageDigest);
            else
              arrayOfByte2 = localEntry.digest(localMessageDigest);
            if (debug != null)
            {
              debug.println("Signature Block File: " + paramString + " digest=" + localMessageDigest.getAlgorithm());
              debug.println("  expected " + toHex(arrayOfByte1));
              debug.println("  computed " + toHex(arrayOfByte2));
              debug.println();
            }
            if (MessageDigest.isEqual(arrayOfByte2, arrayOfByte1))
            {
              i = 1;
              j = 1;
            }
            else if (!(this.workaround))
            {
              arrayOfByte2 = localEntry.digestWorkaround(localMessageDigest);
              if (MessageDigest.isEqual(arrayOfByte2, arrayOfByte1))
              {
                if (debug != null)
                {
                  debug.println("  re-computed " + toHex(arrayOfByte2));
                  debug.println();
                }
                this.workaround = true;
                i = 1;
                j = 1;
              }
            }
            if (j == 0)
              throw new SecurityException("invalid " + localMessageDigest.getAlgorithm() + " signature file digest for " + paramString);
          }
        }
      }
    }
    return i;
  }

  private CodeSigner[] getSigners(SignerInfo[] paramArrayOfSignerInfo, PKCS7 paramPKCS7)
    throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException
  {
    ArrayList localArrayList1 = null;
    for (int i = 0; i < paramArrayOfSignerInfo.length; ++i)
    {
      SignerInfo localSignerInfo = paramArrayOfSignerInfo[i];
      ArrayList localArrayList2 = localSignerInfo.getCertificateChain(paramPKCS7);
      CertPath localCertPath = this.certificateFactory.generateCertPath(localArrayList2);
      if (localArrayList1 == null)
        localArrayList1 = new ArrayList();
      localArrayList1.add(new CodeSigner(localCertPath, getTimestamp(localSignerInfo)));
      if (debug != null)
        debug.println("Signature Block Certificate: " + ((X509Certificate)localArrayList2.get(0)));
    }
    if (localArrayList1 != null)
      return ((CodeSigner[])(CodeSigner[])localArrayList1.toArray(new CodeSigner[localArrayList1.size()]));
    return null;
  }

  private Timestamp getTimestamp(SignerInfo paramSignerInfo)
    throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException
  {
    Timestamp localTimestamp = null;
    PKCS9Attributes localPKCS9Attributes = paramSignerInfo.getUnauthenticatedAttributes();
    if (localPKCS9Attributes != null)
    {
      PKCS9Attribute localPKCS9Attribute = localPKCS9Attributes.getAttribute("signatureTimestampToken");
      if (localPKCS9Attribute != null)
      {
        PKCS7 localPKCS7 = new PKCS7((byte[])(byte[])localPKCS9Attribute.getValue());
        byte[] arrayOfByte = localPKCS7.getContentInfo().getData();
        SignerInfo[] arrayOfSignerInfo = localPKCS7.verify(arrayOfByte);
        ArrayList localArrayList = arrayOfSignerInfo[0].getCertificateChain(localPKCS7);
        CertPath localCertPath = this.certificateFactory.generateCertPath(localArrayList);
        TimestampToken localTimestampToken = new TimestampToken(arrayOfByte);
        localTimestamp = new Timestamp(localTimestampToken.getDate(), localCertPath);
      }
    }
    return localTimestamp;
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

  static boolean contains(CodeSigner[] paramArrayOfCodeSigner, CodeSigner paramCodeSigner)
  {
    for (int i = 0; i < paramArrayOfCodeSigner.length; ++i)
      if (paramArrayOfCodeSigner[i].equals(paramCodeSigner))
        return true;
    return false;
  }

  static boolean isSubSet(CodeSigner[] paramArrayOfCodeSigner1, CodeSigner[] paramArrayOfCodeSigner2)
  {
    if (paramArrayOfCodeSigner2 == paramArrayOfCodeSigner1)
      return true;
    for (int i = 0; i < paramArrayOfCodeSigner1.length; ++i)
      if (!(contains(paramArrayOfCodeSigner2, paramArrayOfCodeSigner1[i])))
        return false;
    return true;
  }

  static boolean matches(CodeSigner[] paramArrayOfCodeSigner1, CodeSigner[] paramArrayOfCodeSigner2, CodeSigner[] paramArrayOfCodeSigner3)
  {
    if ((paramArrayOfCodeSigner2 == null) && (paramArrayOfCodeSigner1 == paramArrayOfCodeSigner3))
      return true;
    if ((paramArrayOfCodeSigner2 != null) && (!(isSubSet(paramArrayOfCodeSigner2, paramArrayOfCodeSigner1))))
      return false;
    if (!(isSubSet(paramArrayOfCodeSigner3, paramArrayOfCodeSigner1)))
      return false;
    for (int i = 0; i < paramArrayOfCodeSigner1.length; ++i)
    {
      int j = (((paramArrayOfCodeSigner2 != null) && (contains(paramArrayOfCodeSigner2, paramArrayOfCodeSigner1[i]))) || (contains(paramArrayOfCodeSigner3, paramArrayOfCodeSigner1[i]))) ? 1 : 0;
      if (j == 0)
        return false;
    }
    return true;
  }

  void updateSigners(CodeSigner[] paramArrayOfCodeSigner, Hashtable paramHashtable, String paramString)
  {
    CodeSigner[] arrayOfCodeSigner2;
    CodeSigner[] arrayOfCodeSigner1 = (CodeSigner[])(CodeSigner[])paramHashtable.get(paramString);
    for (int i = this.signerCache.size() - 1; i != -1; --i)
    {
      arrayOfCodeSigner2 = (CodeSigner[])(CodeSigner[])this.signerCache.get(i);
      if (matches(arrayOfCodeSigner2, arrayOfCodeSigner1, paramArrayOfCodeSigner))
      {
        paramHashtable.put(paramString, arrayOfCodeSigner2);
        return;
      }
    }
    if (arrayOfCodeSigner1 == null)
    {
      arrayOfCodeSigner2 = paramArrayOfCodeSigner;
    }
    else
    {
      arrayOfCodeSigner2 = new CodeSigner[arrayOfCodeSigner1.length + paramArrayOfCodeSigner.length];
      System.arraycopy(arrayOfCodeSigner1, 0, arrayOfCodeSigner2, 0, arrayOfCodeSigner1.length);
      System.arraycopy(paramArrayOfCodeSigner, 0, arrayOfCodeSigner2, arrayOfCodeSigner1.length, paramArrayOfCodeSigner.length);
    }
    this.signerCache.add(arrayOfCodeSigner2);
    paramHashtable.put(paramString, arrayOfCodeSigner2);
  }
}
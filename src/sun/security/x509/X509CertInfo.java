package sun.security.x509;

import B;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import sun.misc.HexDumpEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class X509CertInfo
  implements CertAttrSet
{
  public static final String IDENT = "x509.info";
  public static final String NAME = "info";
  public static final String VERSION = "version";
  public static final String SERIAL_NUMBER = "serialNumber";
  public static final String ALGORITHM_ID = "algorithmID";
  public static final String ISSUER = "issuer";
  public static final String VALIDITY = "validity";
  public static final String SUBJECT = "subject";
  public static final String KEY = "key";
  public static final String ISSUER_ID = "issuerID";
  public static final String SUBJECT_ID = "subjectID";
  public static final String EXTENSIONS = "extensions";
  protected CertificateVersion version = new CertificateVersion();
  protected CertificateSerialNumber serialNum = null;
  protected CertificateAlgorithmId algId = null;
  protected CertificateIssuerName issuer = null;
  protected CertificateValidity interval = null;
  protected CertificateSubjectName subject = null;
  protected CertificateX509Key pubKey = null;
  protected CertificateIssuerUniqueIdentity issuerUniqueId = null;
  protected CertificateSubjectUniqueIdentity subjectUniqueId = null;
  protected CertificateExtensions extensions = null;
  private static final int ATTR_VERSION = 1;
  private static final int ATTR_SERIAL = 2;
  private static final int ATTR_ALGORITHM = 3;
  private static final int ATTR_ISSUER = 4;
  private static final int ATTR_VALIDITY = 5;
  private static final int ATTR_SUBJECT = 6;
  private static final int ATTR_KEY = 7;
  private static final int ATTR_ISSUER_ID = 8;
  private static final int ATTR_SUBJECT_ID = 9;
  private static final int ATTR_EXTENSIONS = 10;
  private byte[] rawCertInfo = null;
  private static final Map<String, Integer> map = new HashMap();

  public X509CertInfo()
  {
  }

  public X509CertInfo(byte[] paramArrayOfByte)
    throws CertificateParsingException
  {
    try
    {
      DerValue localDerValue = new DerValue(paramArrayOfByte);
      parse(localDerValue);
    }
    catch (IOException localIOException)
    {
      CertificateParsingException localCertificateParsingException = new CertificateParsingException(localIOException.toString());
      localCertificateParsingException.initCause(localIOException);
      throw localCertificateParsingException;
    }
  }

  public X509CertInfo(DerValue paramDerValue)
    throws CertificateParsingException
  {
    try
    {
      parse(paramDerValue);
    }
    catch (IOException localIOException)
    {
      CertificateParsingException localCertificateParsingException = new CertificateParsingException(localIOException.toString());
      localCertificateParsingException.initCause(localIOException);
      throw localCertificateParsingException;
    }
  }

  public void encode(OutputStream paramOutputStream)
    throws CertificateException, IOException
  {
    if (this.rawCertInfo == null)
    {
      DerOutputStream localDerOutputStream = new DerOutputStream();
      emit(localDerOutputStream);
      this.rawCertInfo = localDerOutputStream.toByteArray();
    }
    paramOutputStream.write((byte[])(byte[])this.rawCertInfo.clone());
  }

  public Enumeration<String> getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("version");
    localAttributeNameEnumeration.addElement("serialNumber");
    localAttributeNameEnumeration.addElement("algorithmID");
    localAttributeNameEnumeration.addElement("issuer");
    localAttributeNameEnumeration.addElement("validity");
    localAttributeNameEnumeration.addElement("subject");
    localAttributeNameEnumeration.addElement("key");
    localAttributeNameEnumeration.addElement("issuerID");
    localAttributeNameEnumeration.addElement("subjectID");
    localAttributeNameEnumeration.addElement("extensions");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "info";
  }

  public byte[] getEncodedInfo()
    throws CertificateEncodingException
  {
    try
    {
      if (this.rawCertInfo == null)
      {
        DerOutputStream localDerOutputStream = new DerOutputStream();
        emit(localDerOutputStream);
        this.rawCertInfo = localDerOutputStream.toByteArray();
      }
      return ((byte[])(byte[])this.rawCertInfo.clone());
    }
    catch (IOException localIOException)
    {
      throw new CertificateEncodingException(localIOException.toString());
    }
    catch (CertificateException localCertificateException)
    {
      throw new CertificateEncodingException(localCertificateException.toString());
    }
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof X509CertInfo)
      return equals((X509CertInfo)paramObject);
    return false;
  }

  public boolean equals(X509CertInfo paramX509CertInfo)
  {
    if (this == paramX509CertInfo)
      return true;
    if ((this.rawCertInfo == null) || (paramX509CertInfo.rawCertInfo == null))
      return false;
    if (this.rawCertInfo.length != paramX509CertInfo.rawCertInfo.length)
      return false;
    for (int i = 0; i < this.rawCertInfo.length; ++i)
      if (this.rawCertInfo[i] != paramX509CertInfo.rawCertInfo[i])
        return false;
    return true;
  }

  public int hashCode()
  {
    int i = 0;
    for (int j = 1; j < this.rawCertInfo.length; ++j)
      i += this.rawCertInfo[j] * j;
    return i;
  }

  public String toString()
  {
    if ((this.subject == null) || (this.pubKey == null) || (this.interval == null) || (this.issuer == null) || (this.algId == null) || (this.serialNum == null))
      throw new NullPointerException("X.509 cert is incomplete");
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("[\n");
    localStringBuilder.append("  " + this.version.toString() + "\n");
    localStringBuilder.append("  Subject: " + this.subject.toString() + "\n");
    localStringBuilder.append("  Signature Algorithm: " + this.algId.toString() + "\n");
    localStringBuilder.append("  Key:  " + this.pubKey.toString() + "\n");
    localStringBuilder.append("  " + this.interval.toString() + "\n");
    localStringBuilder.append("  Issuer: " + this.issuer.toString() + "\n");
    localStringBuilder.append("  " + this.serialNum.toString() + "\n");
    if (this.issuerUniqueId != null)
      localStringBuilder.append("  Issuer Id:\n" + this.issuerUniqueId.toString() + "\n");
    if (this.subjectUniqueId != null)
      localStringBuilder.append("  Subject Id:\n" + this.subjectUniqueId.toString() + "\n");
    if (this.extensions != null)
    {
      Object localObject;
      Collection localCollection = this.extensions.getAllExtensions();
      Object[] arrayOfObject = localCollection.toArray();
      localStringBuilder.append("\nCertificate Extensions: " + arrayOfObject.length);
      for (int i = 0; i < arrayOfObject.length; ++i)
      {
        localStringBuilder.append("\n[" + (i + 1) + "]: ");
        Extension localExtension = (Extension)arrayOfObject[i];
        try
        {
          if (OIDMap.getClass(localExtension.getExtensionId()) == null)
          {
            localStringBuilder.append(localExtension.toString());
            byte[] arrayOfByte = localExtension.getExtensionValue();
            if (arrayOfByte != null)
            {
              localObject = new DerOutputStream();
              ((DerOutputStream)localObject).putOctetString(arrayOfByte);
              arrayOfByte = ((DerOutputStream)localObject).toByteArray();
              HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
              localStringBuilder.append("Extension unknown: DER encoded OCTET string =\n" + localHexDumpEncoder.encodeBuffer(arrayOfByte) + "\n");
            }
          }
          else
          {
            localStringBuilder.append(localExtension.toString());
          }
        }
        catch (Exception localException)
        {
          localStringBuilder.append(", Error parsing this extension");
        }
      }
      Map localMap = this.extensions.getUnparseableExtensions();
      if (!(localMap.isEmpty()))
      {
        localStringBuilder.append("\nUnparseable certificate extensions: " + localMap.size());
        int j = 1;
        Iterator localIterator = localMap.values().iterator();
        while (localIterator.hasNext())
        {
          localObject = (Extension)localIterator.next();
          localStringBuilder.append("\n[" + (j++) + "]: ");
          localStringBuilder.append(localObject);
        }
      }
    }
    localStringBuilder.append("\n]");
    return ((String)localStringBuilder.toString());
  }

  public void set(String paramString, Object paramObject)
    throws CertificateException, IOException
  {
    X509AttributeName localX509AttributeName = new X509AttributeName(paramString);
    int i = attributeMap(localX509AttributeName.getPrefix());
    if (i == 0)
      throw new CertificateException("Attribute name not recognized: " + paramString);
    this.rawCertInfo = null;
    String str = localX509AttributeName.getSuffix();
    switch (i)
    {
    case 1:
      if (str == null)
      {
        setVersion(paramObject);
        return;
      }
      this.version.set(str, paramObject);
      break;
    case 2:
      if (str == null)
      {
        setSerialNumber(paramObject);
        return;
      }
      this.serialNum.set(str, paramObject);
      break;
    case 3:
      if (str == null)
      {
        setAlgorithmId(paramObject);
        return;
      }
      this.algId.set(str, paramObject);
      break;
    case 4:
      if (str == null)
      {
        setIssuer(paramObject);
        return;
      }
      this.issuer.set(str, paramObject);
      break;
    case 5:
      if (str == null)
      {
        setValidity(paramObject);
        return;
      }
      this.interval.set(str, paramObject);
      break;
    case 6:
      if (str == null)
      {
        setSubject(paramObject);
        return;
      }
      this.subject.set(str, paramObject);
      break;
    case 7:
      if (str == null)
      {
        setKey(paramObject);
        return;
      }
      this.pubKey.set(str, paramObject);
      break;
    case 8:
      if (str == null)
      {
        setIssuerUniqueId(paramObject);
        return;
      }
      this.issuerUniqueId.set(str, paramObject);
      break;
    case 9:
      if (str == null)
      {
        setSubjectUniqueId(paramObject);
        return;
      }
      this.subjectUniqueId.set(str, paramObject);
      break;
    case 10:
      if (str == null)
      {
        setExtensions(paramObject);
        return;
      }
      if (this.extensions == null)
        this.extensions = new CertificateExtensions();
      this.extensions.set(str, paramObject);
    }
  }

  public void delete(String paramString)
    throws CertificateException, IOException
  {
    X509AttributeName localX509AttributeName = new X509AttributeName(paramString);
    int i = attributeMap(localX509AttributeName.getPrefix());
    if (i == 0)
      throw new CertificateException("Attribute name not recognized: " + paramString);
    this.rawCertInfo = null;
    String str = localX509AttributeName.getSuffix();
    switch (i)
    {
    case 1:
      if (str == null)
      {
        this.version = null;
        return;
      }
      this.version.delete(str);
      break;
    case 2:
      if (str == null)
      {
        this.serialNum = null;
        return;
      }
      this.serialNum.delete(str);
      break;
    case 3:
      if (str == null)
      {
        this.algId = null;
        return;
      }
      this.algId.delete(str);
      break;
    case 4:
      if (str == null)
      {
        this.issuer = null;
        return;
      }
      this.issuer.delete(str);
      break;
    case 5:
      if (str == null)
      {
        this.interval = null;
        return;
      }
      this.interval.delete(str);
      break;
    case 6:
      if (str == null)
      {
        this.subject = null;
        return;
      }
      this.subject.delete(str);
      break;
    case 7:
      if (str == null)
      {
        this.pubKey = null;
        return;
      }
      this.pubKey.delete(str);
      break;
    case 8:
      if (str == null)
      {
        this.issuerUniqueId = null;
        return;
      }
      this.issuerUniqueId.delete(str);
      break;
    case 9:
      if (str == null)
      {
        this.subjectUniqueId = null;
        return;
      }
      this.subjectUniqueId.delete(str);
      break;
    case 10:
      if (str == null)
      {
        this.extensions = null;
        return;
      }
      if (this.extensions == null)
        return;
      this.extensions.delete(str);
    }
  }

  public Object get(String paramString)
    throws CertificateException, IOException
  {
    X509AttributeName localX509AttributeName = new X509AttributeName(paramString);
    int i = attributeMap(localX509AttributeName.getPrefix());
    if (i == 0)
      throw new CertificateParsingException("Attribute name not recognized: " + paramString);
    String str = localX509AttributeName.getSuffix();
    switch (i)
    {
    case 10:
      if (str == null)
        return this.extensions;
      if (this.extensions == null)
        return null;
      return this.extensions.get(str);
    case 6:
      if (str == null)
        return this.subject;
      return this.subject.get(str);
    case 4:
      if (str == null)
        return this.issuer;
      return this.issuer.get(str);
    case 7:
      if (str == null)
        return this.pubKey;
      return this.pubKey.get(str);
    case 3:
      if (str == null)
        return this.algId;
      return this.algId.get(str);
    case 5:
      if (str == null)
        return this.interval;
      return this.interval.get(str);
    case 1:
      if (str == null)
        return this.version;
      return this.version.get(str);
    case 2:
      if (str == null)
        return this.serialNum;
      return this.serialNum.get(str);
    case 8:
      if (str == null)
        return this.issuerUniqueId;
      if (this.issuerUniqueId == null)
        return null;
      return this.issuerUniqueId.get(str);
    case 9:
      if (str == null)
        return this.subjectUniqueId;
      if (this.subjectUniqueId == null)
        return null;
      return this.subjectUniqueId.get(str);
    }
    return null;
  }

  private void parse(DerValue paramDerValue)
    throws CertificateParsingException, IOException
  {
    if (paramDerValue.tag != 48)
      throw new CertificateParsingException("signed fields invalid");
    this.rawCertInfo = paramDerValue.toByteArray();
    DerInputStream localDerInputStream = paramDerValue.data;
    DerValue localDerValue = localDerInputStream.getDerValue();
    if (localDerValue.isContextSpecific(0))
    {
      this.version = new CertificateVersion(localDerValue);
      localDerValue = localDerInputStream.getDerValue();
    }
    this.serialNum = new CertificateSerialNumber(localDerValue);
    this.algId = new CertificateAlgorithmId(localDerInputStream);
    this.issuer = new CertificateIssuerName(localDerInputStream);
    X500Name localX500Name1 = (X500Name)this.issuer.get("dname");
    if (localX500Name1.isEmpty())
      throw new CertificateParsingException("Empty issuer DN not allowed in X509Certificates");
    this.interval = new CertificateValidity(localDerInputStream);
    this.subject = new CertificateSubjectName(localDerInputStream);
    X500Name localX500Name2 = (X500Name)this.subject.get("dname");
    if ((this.version.compare(0) == 0) && (localX500Name2.isEmpty()))
      throw new CertificateParsingException("Empty subject DN not allowed in v1 certificate");
    this.pubKey = new CertificateX509Key(localDerInputStream);
    if (localDerInputStream.available() != 0)
    {
      if (this.version.compare(0) != 0)
        break label238;
      throw new CertificateParsingException("no more data allowed for version 1 certificate");
    }
    return;
    label238: localDerValue = localDerInputStream.getDerValue();
    if (localDerValue.isContextSpecific(1))
    {
      this.issuerUniqueId = new CertificateIssuerUniqueIdentity(localDerValue);
      if (localDerInputStream.available() == 0)
        return;
      localDerValue = localDerInputStream.getDerValue();
    }
    if (localDerValue.isContextSpecific(2))
    {
      this.subjectUniqueId = new CertificateSubjectUniqueIdentity(localDerValue);
      if (localDerInputStream.available() == 0)
        return;
      localDerValue = localDerInputStream.getDerValue();
    }
    if (this.version.compare(2) != 0)
      throw new CertificateParsingException("Extensions not allowed in v2 certificate");
    if ((localDerValue.isConstructed()) && (localDerValue.isContextSpecific(3)))
      this.extensions = new CertificateExtensions(localDerValue.data);
    verifyCert(this.subject, this.extensions);
  }

  private void verifyCert(CertificateSubjectName paramCertificateSubjectName, CertificateExtensions paramCertificateExtensions)
    throws CertificateParsingException, IOException
  {
    X500Name localX500Name = (X500Name)paramCertificateSubjectName.get("dname");
    if (localX500Name.isEmpty())
    {
      if (paramCertificateExtensions == null)
        throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and certificate has no extensions");
      SubjectAlternativeNameExtension localSubjectAlternativeNameExtension = null;
      Object localObject = null;
      GeneralNames localGeneralNames = null;
      try
      {
        localSubjectAlternativeNameExtension = (SubjectAlternativeNameExtension)paramCertificateExtensions.get("SubjectAlternativeName");
        localGeneralNames = (GeneralNames)localSubjectAlternativeNameExtension.get("subject_name");
      }
      catch (IOException localIOException)
      {
        throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is absent");
      }
      if ((localGeneralNames == null) || (localGeneralNames.isEmpty()))
        throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is empty");
      if (!(localSubjectAlternativeNameExtension.isCritical()))
        throw new CertificateParsingException("X.509 Certificate is incomplete: SubjectAlternativeName extension MUST be marked critical when subject field is empty");
    }
  }

  private void emit(DerOutputStream paramDerOutputStream)
    throws CertificateException, IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    this.version.encode(localDerOutputStream);
    this.serialNum.encode(localDerOutputStream);
    this.algId.encode(localDerOutputStream);
    if ((this.version.compare(0) == 0) && (this.issuer.toString() == null))
      throw new CertificateParsingException("Null issuer DN not allowed in v1 certificate");
    this.issuer.encode(localDerOutputStream);
    this.interval.encode(localDerOutputStream);
    if ((this.version.compare(0) == 0) && (this.subject.toString() == null))
      throw new CertificateParsingException("Null subject DN not allowed in v1 certificate");
    this.subject.encode(localDerOutputStream);
    this.pubKey.encode(localDerOutputStream);
    if (this.issuerUniqueId != null)
      this.issuerUniqueId.encode(localDerOutputStream);
    if (this.subjectUniqueId != null)
      this.subjectUniqueId.encode(localDerOutputStream);
    if (this.extensions != null)
      this.extensions.encode(localDerOutputStream);
    paramDerOutputStream.write(48, localDerOutputStream);
  }

  private int attributeMap(String paramString)
  {
    Integer localInteger = (Integer)map.get(paramString);
    if (localInteger == null)
      return 0;
    return localInteger.intValue();
  }

  private void setVersion(Object paramObject)
    throws CertificateException
  {
    if (!(paramObject instanceof CertificateVersion))
      throw new CertificateException("Version class type invalid.");
    this.version = ((CertificateVersion)paramObject);
  }

  private void setSerialNumber(Object paramObject)
    throws CertificateException
  {
    if (!(paramObject instanceof CertificateSerialNumber))
      throw new CertificateException("SerialNumber class type invalid.");
    this.serialNum = ((CertificateSerialNumber)paramObject);
  }

  private void setAlgorithmId(Object paramObject)
    throws CertificateException
  {
    if (!(paramObject instanceof CertificateAlgorithmId))
      throw new CertificateException("AlgorithmId class type invalid.");
    this.algId = ((CertificateAlgorithmId)paramObject);
  }

  private void setIssuer(Object paramObject)
    throws CertificateException
  {
    if (!(paramObject instanceof CertificateIssuerName))
      throw new CertificateException("Issuer class type invalid.");
    this.issuer = ((CertificateIssuerName)paramObject);
  }

  private void setValidity(Object paramObject)
    throws CertificateException
  {
    if (!(paramObject instanceof CertificateValidity))
      throw new CertificateException("CertificateValidity class type invalid.");
    this.interval = ((CertificateValidity)paramObject);
  }

  private void setSubject(Object paramObject)
    throws CertificateException
  {
    if (!(paramObject instanceof CertificateSubjectName))
      throw new CertificateException("Subject class type invalid.");
    this.subject = ((CertificateSubjectName)paramObject);
  }

  private void setKey(Object paramObject)
    throws CertificateException
  {
    if (!(paramObject instanceof CertificateX509Key))
      throw new CertificateException("Key class type invalid.");
    this.pubKey = ((CertificateX509Key)paramObject);
  }

  private void setIssuerUniqueId(Object paramObject)
    throws CertificateException
  {
    if (this.version.compare(1) < 0)
      throw new CertificateException("Invalid version");
    if (!(paramObject instanceof CertificateIssuerUniqueIdentity))
      throw new CertificateException("IssuerUniqueId class type invalid.");
    this.issuerUniqueId = ((CertificateIssuerUniqueIdentity)paramObject);
  }

  private void setSubjectUniqueId(Object paramObject)
    throws CertificateException
  {
    if (this.version.compare(1) < 0)
      throw new CertificateException("Invalid version");
    if (!(paramObject instanceof CertificateSubjectUniqueIdentity))
      throw new CertificateException("SubjectUniqueId class type invalid.");
    this.subjectUniqueId = ((CertificateSubjectUniqueIdentity)paramObject);
  }

  private void setExtensions(Object paramObject)
    throws CertificateException
  {
    if (this.version.compare(2) < 0)
      throw new CertificateException("Invalid version");
    if (!(paramObject instanceof CertificateExtensions))
      throw new CertificateException("Extensions class type invalid.");
    this.extensions = ((CertificateExtensions)paramObject);
  }

  static
  {
    map.put("version", Integer.valueOf(1));
    map.put("serialNumber", Integer.valueOf(2));
    map.put("algorithmID", Integer.valueOf(3));
    map.put("issuer", Integer.valueOf(4));
    map.put("validity", Integer.valueOf(5));
    map.put("subject", Integer.valueOf(6));
    map.put("key", Integer.valueOf(7));
    map.put("issuerID", Integer.valueOf(8));
    map.put("subjectID", Integer.valueOf(9));
    map.put("extensions", Integer.valueOf(10));
  }
}
package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class ExtendedKeyUsageExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.ExtendedKeyUsage";
  public static final String NAME = "ExtendedKeyUsage";
  public static final String USAGES = "usages";
  private static final Map<ObjectIdentifier, String> map = new HashMap();
  private static final int[] anyExtendedKeyUsageOidData = { 2, 5, 29, 37, 0 };
  private static final int[] serverAuthOidData = { 1, 3, 6, 1, 5, 5, 7, 3, 1 };
  private static final int[] clientAuthOidData = { 1, 3, 6, 1, 5, 5, 7, 3, 2 };
  private static final int[] codeSigningOidData = { 1, 3, 6, 1, 5, 5, 7, 3, 3 };
  private static final int[] emailProtectionOidData = { 1, 3, 6, 1, 5, 5, 7, 3, 4 };
  private static final int[] ipsecEndSystemOidData = { 1, 3, 6, 1, 5, 5, 7, 3, 5 };
  private static final int[] ipsecTunnelOidData = { 1, 3, 6, 1, 5, 5, 7, 3, 6 };
  private static final int[] ipsecUserOidData = { 1, 3, 6, 1, 5, 5, 7, 3, 7 };
  private static final int[] timeStampingOidData = { 1, 3, 6, 1, 5, 5, 7, 3, 8 };
  private static final int[] OCSPSigningOidData = { 1, 3, 6, 1, 5, 5, 7, 3, 9 };
  private Vector<ObjectIdentifier> keyUsages;

  private void encodeThis()
    throws IOException
  {
    if ((this.keyUsages == null) || (this.keyUsages.isEmpty()))
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    for (int i = 0; i < this.keyUsages.size(); ++i)
      localDerOutputStream2.putOID((ObjectIdentifier)this.keyUsages.elementAt(i));
    localDerOutputStream1.write(48, localDerOutputStream2);
    this.extensionValue = localDerOutputStream1.toByteArray();
  }

  public ExtendedKeyUsageExtension(Vector<ObjectIdentifier> paramVector)
    throws IOException
  {
    this(Boolean.FALSE, paramVector);
  }

  public ExtendedKeyUsageExtension(Boolean paramBoolean, Vector<ObjectIdentifier> paramVector)
    throws IOException
  {
    this.keyUsages = paramVector;
    this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
    this.critical = paramBoolean.booleanValue();
    encodeThis();
  }

  public ExtendedKeyUsageExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue1 = new DerValue(this.extensionValue);
    if (localDerValue1.tag != 48)
      throw new IOException("Invalid encoding for ExtendedKeyUsageExtension.");
    this.keyUsages = new Vector();
    while (localDerValue1.data.available() != 0)
    {
      DerValue localDerValue2 = localDerValue1.data.getDerValue();
      ObjectIdentifier localObjectIdentifier = localDerValue2.getOID();
      this.keyUsages.addElement(localObjectIdentifier);
    }
  }

  public String toString()
  {
    if (this.keyUsages == null)
      return "";
    String str1 = "  ";
    int i = 1;
    Iterator localIterator = this.keyUsages.iterator();
    while (localIterator.hasNext())
    {
      ObjectIdentifier localObjectIdentifier = (ObjectIdentifier)localIterator.next();
      if (i == 0)
        str1 = str1 + "\n  ";
      String str2 = (String)map.get(localObjectIdentifier);
      if (str2 != null)
        str1 = str1 + str2;
      else
        str1 = str1 + localObjectIdentifier.toString();
      i = 0;
    }
    return super.toString() + "ExtendedKeyUsages [\n" + str1 + "\n]\n";
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("usages"))
    {
      if (!(paramObject instanceof Vector))
        throw new IOException("Attribute value should be of type Vector.");
      this.keyUsages = ((Vector)paramObject);
    }
    else
    {
      throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:ExtendedKeyUsageExtension.");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("usages"))
      return this.keyUsages;
    throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:ExtendedKeyUsageExtension.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("usages"))
      this.keyUsages = null;
    else
      throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:ExtendedKeyUsageExtension.");
    encodeThis();
  }

  public Enumeration<String> getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("usages");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "ExtendedKeyUsage";
  }

  public List<String> getExtendedKeyUsage()
  {
    ArrayList localArrayList = new ArrayList(this.keyUsages.size());
    Iterator localIterator = this.keyUsages.iterator();
    while (localIterator.hasNext())
    {
      ObjectIdentifier localObjectIdentifier = (ObjectIdentifier)localIterator.next();
      localArrayList.add(localObjectIdentifier.toString());
    }
    return localArrayList;
  }

  static
  {
    map.put(ObjectIdentifier.newInternal(anyExtendedKeyUsageOidData), "anyExtendedKeyUsage");
    map.put(ObjectIdentifier.newInternal(serverAuthOidData), "serverAuth");
    map.put(ObjectIdentifier.newInternal(clientAuthOidData), "clientAuth");
    map.put(ObjectIdentifier.newInternal(codeSigningOidData), "codeSigning");
    map.put(ObjectIdentifier.newInternal(emailProtectionOidData), "emailProtection");
    map.put(ObjectIdentifier.newInternal(ipsecEndSystemOidData), "ipsecEndSystem");
    map.put(ObjectIdentifier.newInternal(ipsecTunnelOidData), "ipsecTunnel");
    map.put(ObjectIdentifier.newInternal(ipsecUserOidData), "ipsecUser");
    map.put(ObjectIdentifier.newInternal(timeStampingOidData), "timeStamping");
    map.put(ObjectIdentifier.newInternal(OCSPSigningOidData), "OCSPSigning");
  }
}
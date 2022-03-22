package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CRLDistributionPointsExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.CRLDistributionPoints";
  public static final String NAME = "CRLDistributionPoints";
  public static final String POINTS = "points";
  private List<DistributionPoint> distributionPoints;
  private String extensionName;

  public CRLDistributionPointsExtension(List<DistributionPoint> paramList)
    throws IOException
  {
    this(false, paramList);
  }

  public CRLDistributionPointsExtension(boolean paramBoolean, List<DistributionPoint> paramList)
    throws IOException
  {
    this(PKIXExtensions.CRLDistributionPoints_Id, paramBoolean, paramList, "CRLDistributionPoints");
  }

  protected CRLDistributionPointsExtension(ObjectIdentifier paramObjectIdentifier, boolean paramBoolean, List<DistributionPoint> paramList, String paramString)
    throws IOException
  {
    this.extensionId = paramObjectIdentifier;
    this.critical = paramBoolean;
    this.distributionPoints = paramList;
    encodeThis();
    this.extensionName = paramString;
  }

  public CRLDistributionPointsExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this(PKIXExtensions.CRLDistributionPoints_Id, paramBoolean, paramObject, "CRLDistributionPoints");
  }

  protected CRLDistributionPointsExtension(ObjectIdentifier paramObjectIdentifier, Boolean paramBoolean, Object paramObject, String paramString)
    throws IOException
  {
    this.extensionId = paramObjectIdentifier;
    this.critical = paramBoolean.booleanValue();
    if (!(paramObject instanceof byte[]))
      throw new IOException("Illegal argument type");
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue1 = new DerValue(this.extensionValue);
    if (localDerValue1.tag != 48)
      throw new IOException("Invalid encoding for " + paramString + " extension.");
    this.distributionPoints = new ArrayList();
    while (localDerValue1.data.available() != 0)
    {
      DerValue localDerValue2 = localDerValue1.data.getDerValue();
      DistributionPoint localDistributionPoint = new DistributionPoint(localDerValue2);
      this.distributionPoints.add(localDistributionPoint);
    }
    this.extensionName = paramString;
  }

  public String getName()
  {
    return this.extensionName;
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    encode(paramOutputStream, PKIXExtensions.CRLDistributionPoints_Id, false);
  }

  protected void encode(OutputStream paramOutputStream, ObjectIdentifier paramObjectIdentifier, boolean paramBoolean)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = paramObjectIdentifier;
      this.critical = paramBoolean;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("points"))
    {
      if (!(paramObject instanceof List))
        throw new IOException("Attribute value should be of type List.");
      this.distributionPoints = ((List)paramObject);
    }
    else
    {
      throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:" + this.extensionName + ".");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("points"))
      return this.distributionPoints;
    throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:" + this.extensionName + ".");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("points"))
      this.distributionPoints = new ArrayList();
    else
      throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:" + this.extensionName + ".");
    encodeThis();
  }

  public Enumeration<String> getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("points");
    return localAttributeNameEnumeration.elements();
  }

  private void encodeThis()
    throws IOException
  {
    if (this.distributionPoints.isEmpty())
    {
      this.extensionValue = null;
    }
    else
    {
      DerOutputStream localDerOutputStream = new DerOutputStream();
      Object localObject = this.distributionPoints.iterator();
      while (((Iterator)localObject).hasNext())
      {
        DistributionPoint localDistributionPoint = (DistributionPoint)((Iterator)localObject).next();
        localDistributionPoint.encode(localDerOutputStream);
      }
      localObject = new DerOutputStream();
      ((DerOutputStream)localObject).write(48, localDerOutputStream);
      this.extensionValue = ((DerOutputStream)localObject).toByteArray();
    }
  }

  public String toString()
  {
    return super.toString() + this.extensionName + " [\n  " + this.distributionPoints + "]\n";
  }
}
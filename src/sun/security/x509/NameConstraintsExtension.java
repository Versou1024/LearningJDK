package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class NameConstraintsExtension extends Extension
  implements CertAttrSet, Cloneable
{
  public static final String IDENT = "x509.info.extensions.NameConstraints";
  public static final String NAME = "NameConstraints";
  public static final String PERMITTED_SUBTREES = "permitted_subtrees";
  public static final String EXCLUDED_SUBTREES = "excluded_subtrees";
  private static final byte TAG_PERMITTED = 0;
  private static final byte TAG_EXCLUDED = 1;
  private GeneralSubtrees permitted = null;
  private GeneralSubtrees excluded = null;
  private boolean hasMin;
  private boolean hasMax;
  private boolean minMaxValid = false;

  private void calcMinMax()
    throws IOException
  {
    int i;
    GeneralSubtree localGeneralSubtree;
    this.hasMin = false;
    this.hasMax = false;
    if (this.excluded != null)
      for (i = 0; i < this.excluded.size(); ++i)
      {
        localGeneralSubtree = this.excluded.get(i);
        if (localGeneralSubtree.getMinimum() != 0)
          this.hasMin = true;
        if (localGeneralSubtree.getMaximum() != -1)
          this.hasMax = true;
      }
    if (this.permitted != null)
      for (i = 0; i < this.permitted.size(); ++i)
      {
        localGeneralSubtree = this.permitted.get(i);
        if (localGeneralSubtree.getMinimum() != 0)
          this.hasMin = true;
        if (localGeneralSubtree.getMaximum() != -1)
          this.hasMax = true;
      }
    this.minMaxValid = true;
  }

  private void encodeThis()
    throws IOException
  {
    DerOutputStream localDerOutputStream3;
    this.minMaxValid = false;
    if ((this.permitted == null) && (this.excluded == null))
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    if (this.permitted != null)
    {
      localDerOutputStream3 = new DerOutputStream();
      this.permitted.encode(localDerOutputStream3);
      localDerOutputStream2.writeImplicit(DerValue.createTag(-128, true, 0), localDerOutputStream3);
    }
    if (this.excluded != null)
    {
      localDerOutputStream3 = new DerOutputStream();
      this.excluded.encode(localDerOutputStream3);
      localDerOutputStream2.writeImplicit(DerValue.createTag(-128, true, 1), localDerOutputStream3);
    }
    localDerOutputStream1.write(48, localDerOutputStream2);
    this.extensionValue = localDerOutputStream1.toByteArray();
  }

  public NameConstraintsExtension(GeneralSubtrees paramGeneralSubtrees1, GeneralSubtrees paramGeneralSubtrees2)
    throws IOException
  {
    this.permitted = paramGeneralSubtrees1;
    this.excluded = paramGeneralSubtrees2;
    this.extensionId = PKIXExtensions.NameConstraints_Id;
    this.critical = true;
    encodeThis();
  }

  public NameConstraintsExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.NameConstraints_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue1 = new DerValue(this.extensionValue);
    if (localDerValue1.tag != 48)
      throw new IOException("Invalid encoding for NameConstraintsExtension.");
    if (localDerValue1.data == null)
      return;
    while (localDerValue1.data.available() != 0)
    {
      DerValue localDerValue2 = localDerValue1.data.getDerValue();
      if ((localDerValue2.isContextSpecific(0)) && (localDerValue2.isConstructed()))
      {
        if (this.permitted != null)
          throw new IOException("Duplicate permitted GeneralSubtrees in NameConstraintsExtension.");
        localDerValue2.resetTag(48);
        this.permitted = new GeneralSubtrees(localDerValue2);
      }
      else if ((localDerValue2.isContextSpecific(1)) && (localDerValue2.isConstructed()))
      {
        if (this.excluded != null)
          throw new IOException("Duplicate excluded GeneralSubtrees in NameConstraintsExtension.");
        localDerValue2.resetTag(48);
        this.excluded = new GeneralSubtrees(localDerValue2);
      }
      else
      {
        throw new IOException("Invalid encoding of NameConstraintsExtension.");
      }
    }
    this.minMaxValid = false;
  }

  public String toString()
  {
    return super.toString() + "NameConstraints: [" + ((this.permitted == null) ? "" : new StringBuilder().append("\n    Permitted:").append(this.permitted.toString()).toString()) + ((this.excluded == null) ? "" : new StringBuilder().append("\n    Excluded:").append(this.excluded.toString()).toString()) + "   ]\n";
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.NameConstraints_Id;
      this.critical = true;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("permitted_subtrees"))
    {
      if (!(paramObject instanceof GeneralSubtrees))
        throw new IOException("Attribute value should be of type GeneralSubtrees.");
      this.permitted = ((GeneralSubtrees)paramObject);
    }
    else if (paramString.equalsIgnoreCase("excluded_subtrees"))
    {
      if (!(paramObject instanceof GeneralSubtrees))
        throw new IOException("Attribute value should be of type GeneralSubtrees.");
      this.excluded = ((GeneralSubtrees)paramObject);
    }
    else
    {
      throw new IOException("Attribute name not recognized by CertAttrSet:NameConstraintsExtension.");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("permitted_subtrees"))
      return this.permitted;
    if (paramString.equalsIgnoreCase("excluded_subtrees"))
      return this.excluded;
    throw new IOException("Attribute name not recognized by CertAttrSet:NameConstraintsExtension.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("permitted_subtrees"))
      this.permitted = null;
    else if (paramString.equalsIgnoreCase("excluded_subtrees"))
      this.excluded = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:NameConstraintsExtension.");
    encodeThis();
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("permitted_subtrees");
    localAttributeNameEnumeration.addElement("excluded_subtrees");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "NameConstraints";
  }

  public void merge(NameConstraintsExtension paramNameConstraintsExtension)
    throws IOException
  {
    if (paramNameConstraintsExtension == null)
      return;
    GeneralSubtrees localGeneralSubtrees1 = (GeneralSubtrees)paramNameConstraintsExtension.get("excluded_subtrees");
    if (this.excluded == null)
      this.excluded = ((localGeneralSubtrees1 != null) ? (GeneralSubtrees)localGeneralSubtrees1.clone() : null);
    else if (localGeneralSubtrees1 != null)
      this.excluded.union(localGeneralSubtrees1);
    GeneralSubtrees localGeneralSubtrees2 = (GeneralSubtrees)paramNameConstraintsExtension.get("permitted_subtrees");
    if (this.permitted == null)
    {
      this.permitted = ((localGeneralSubtrees2 != null) ? (GeneralSubtrees)localGeneralSubtrees2.clone() : null);
    }
    else if (localGeneralSubtrees2 != null)
    {
      localGeneralSubtrees1 = this.permitted.intersect(localGeneralSubtrees2);
      if (localGeneralSubtrees1 != null)
        if (this.excluded != null)
          this.excluded.union(localGeneralSubtrees1);
        else
          this.excluded = ((GeneralSubtrees)localGeneralSubtrees1.clone());
    }
    if (this.permitted != null)
      this.permitted.reduce(this.excluded);
    encodeThis();
  }

  public boolean verify(X509Certificate paramX509Certificate)
    throws IOException
  {
    Object localObject;
    if (paramX509Certificate == null)
      throw new IOException("Certificate is null");
    if (!(this.minMaxValid))
      calcMinMax();
    if (this.hasMin)
      throw new IOException("Non-zero minimum BaseDistance in name constraints not supported");
    if (this.hasMax)
      throw new IOException("Maximum BaseDistance in name constraints not supported");
    X500Principal localX500Principal = paramX509Certificate.getSubjectX500Principal();
    X500Name localX500Name = X500Name.asX500Name(localX500Principal);
    if ((!(localX500Name.isEmpty())) && (!(verify(localX500Name))))
      return false;
    GeneralNames localGeneralNames = null;
    try
    {
      X509CertImpl localX509CertImpl = X509CertImpl.toImpl(paramX509Certificate);
      localObject = localX509CertImpl.getSubjectAlternativeNameExtension();
      if (localObject != null)
        localGeneralNames = (GeneralNames)(GeneralNames)((SubjectAlternativeNameExtension)localObject).get("subject_name");
    }
    catch (CertificateException localCertificateException)
    {
      throw new IOException("Unable to extract extensions from certificate: " + localCertificateException.getMessage());
    }
    if (localGeneralNames == null)
      return verifyRFC822SpecialCase(localX500Name);
    for (int i = 0; i < localGeneralNames.size(); ++i)
    {
      localObject = localGeneralNames.get(i).getName();
      if (!(verify((GeneralNameInterface)localObject)))
        return false;
    }
    return true;
  }

  public boolean verify(GeneralNameInterface paramGeneralNameInterface)
    throws IOException
  {
    Object localObject1;
    Object localObject2;
    if (paramGeneralNameInterface == null)
      throw new IOException("name is null");
    if ((this.excluded != null) && (this.excluded.size() > 0))
      for (i = 0; i < this.excluded.size(); ++i)
      {
        GeneralSubtree localGeneralSubtree = this.excluded.get(i);
        if (localGeneralSubtree == null)
          break label137:
        localObject1 = localGeneralSubtree.getName();
        if (localObject1 == null)
          break label137:
        localObject2 = ((GeneralName)localObject1).getName();
        if (localObject2 == null)
          break label137:
        label137: switch (((GeneralNameInterface)localObject2).constrains(paramGeneralNameInterface))
        {
        case -1:
        case 2:
        case 3:
          break;
        case 0:
        case 1:
          return false;
        }
      }
    if ((this.permitted == null) || (this.permitted.size() <= 0))
      break label286;
    int i = 0;
    for (int j = 0; j < this.permitted.size(); ++j)
    {
      localObject1 = this.permitted.get(j);
      if (localObject1 == null)
        break label274:
      localObject2 = ((GeneralSubtree)localObject1).getName();
      if (localObject2 == null)
        break label274:
      GeneralNameInterface localGeneralNameInterface = ((GeneralName)localObject2).getName();
      if (localGeneralNameInterface == null)
        break label274:
      switch (localGeneralNameInterface.constrains(paramGeneralNameInterface))
      {
      case -1:
        break;
      case 2:
      case 3:
        i = 1;
        label274: break;
      case 0:
      case 1:
        return true;
      }
    }
    label286: return (i == 0);
  }

  public boolean verifyRFC822SpecialCase(X500Name paramX500Name)
    throws IOException
  {
    Iterator localIterator = paramX500Name.allAvas().iterator();
    while (localIterator.hasNext())
    {
      label10: AVA localAVA = (AVA)localIterator.next();
      ObjectIdentifier localObjectIdentifier = localAVA.getObjectIdentifier();
      if (localObjectIdentifier.equals(PKCS9Attribute.EMAIL_ADDRESS_OID))
      {
        String str = localAVA.getValueString();
        if (str != null)
        {
          RFC822Name localRFC822Name;
          try
          {
            localRFC822Name = new RFC822Name(str);
          }
          catch (IOException localIOException)
          {
            break label10:
          }
          if (!(verify(localRFC822Name)))
            return false;
        }
      }
    }
    return true;
  }

  public Object clone()
  {
    NameConstraintsExtension localNameConstraintsExtension;
    try
    {
      localNameConstraintsExtension = (NameConstraintsExtension)clone();
      if (this.permitted != null)
        localNameConstraintsExtension.permitted = ((GeneralSubtrees)this.permitted.clone());
      if (this.excluded != null)
        localNameConstraintsExtension.excluded = ((GeneralSubtrees)this.excluded.clone());
      return localNameConstraintsExtension;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      throw new RuntimeException("CloneNotSupportedException while cloning NameConstraintsException. This should never happen.");
    }
  }
}
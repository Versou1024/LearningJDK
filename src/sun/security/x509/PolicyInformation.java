package sun.security.x509;

import java.io.IOException;
import java.security.cert.PolicyQualifierInfo;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PolicyInformation
{
  public static final String NAME = "PolicyInformation";
  public static final String ID = "id";
  public static final String QUALIFIERS = "qualifiers";
  private CertificatePolicyId policyIdentifier;
  private Set<PolicyQualifierInfo> policyQualifiers;

  public PolicyInformation(CertificatePolicyId paramCertificatePolicyId, Set<PolicyQualifierInfo> paramSet)
    throws IOException
  {
    if (paramSet == null)
      throw new NullPointerException("policyQualifiers is null");
    this.policyQualifiers = new LinkedHashSet(paramSet);
    this.policyIdentifier = paramCertificatePolicyId;
  }

  public PolicyInformation(DerValue paramDerValue)
    throws IOException
  {
    if (paramDerValue.tag != 48)
      throw new IOException("Invalid encoding of PolicyInformation");
    this.policyIdentifier = new CertificatePolicyId(paramDerValue.data.getDerValue());
    if (paramDerValue.data.available() != 0)
    {
      this.policyQualifiers = new LinkedHashSet();
      DerValue localDerValue = paramDerValue.data.getDerValue();
      if (localDerValue.tag != 48)
        throw new IOException("Invalid encoding of PolicyInformation");
      if (localDerValue.data.available() == 0)
        throw new IOException("No data available in policyQualifiers");
      while (localDerValue.data.available() != 0)
        this.policyQualifiers.add(new PolicyQualifierInfo(localDerValue.data.getDerValue().toByteArray()));
    }
    else
    {
      this.policyQualifiers = Collections.emptySet();
    }
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof PolicyInformation))
      return false;
    PolicyInformation localPolicyInformation = (PolicyInformation)paramObject;
    if (!(this.policyIdentifier.equals(localPolicyInformation.getPolicyIdentifier())))
      return false;
    return this.policyQualifiers.equals(localPolicyInformation.getPolicyQualifiers());
  }

  public int hashCode()
  {
    int i = 37 + this.policyIdentifier.hashCode();
    i = 37 * i + this.policyQualifiers.hashCode();
    return i;
  }

  public CertificatePolicyId getPolicyIdentifier()
  {
    return this.policyIdentifier;
  }

  public Set<PolicyQualifierInfo> getPolicyQualifiers()
  {
    return this.policyQualifiers;
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("id"))
      return this.policyIdentifier;
    if (paramString.equalsIgnoreCase("qualifiers"))
      return this.policyQualifiers;
    throw new IOException("Attribute name [" + paramString + "] not recognized by PolicyInformation.");
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("id"))
    {
      if (paramObject instanceof CertificatePolicyId)
      {
        this.policyIdentifier = ((CertificatePolicyId)paramObject);
        return;
      }
      throw new IOException("Attribute value must be instance of CertificatePolicyId.");
    }
    if (paramString.equalsIgnoreCase("qualifiers"))
    {
      if (this.policyIdentifier == null)
        throw new IOException("Attribute must have a CertificatePolicyIdentifier value before PolicyQualifierInfo can be set.");
      if (paramObject instanceof Set)
      {
        Iterator localIterator = ((Set)paramObject).iterator();
        while (localIterator.hasNext())
        {
          Object localObject = localIterator.next();
          if (!(localObject instanceof PolicyQualifierInfo))
            throw new IOException("Attribute value must be aSet of PolicyQualifierInfo objects.");
        }
        this.policyQualifiers = ((Set)paramObject);
        return;
      }
      throw new IOException("Attribute value must be of type Set.");
    }
    throw new IOException("Attribute name [" + paramString + "] not recognized by PolicyInformation");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("qualifiers"))
    {
      this.policyQualifiers = Collections.emptySet();
    }
    else
    {
      if (paramString.equalsIgnoreCase("id"))
        throw new IOException("Attribute ID may not be deleted from PolicyInformation.");
      throw new IOException("Attribute name [" + paramString + "] not recognized by PolicyInformation.");
    }
  }

  public Enumeration<String> getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("id");
    localAttributeNameEnumeration.addElement("qualifiers");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "PolicyInformation";
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder("  [" + this.policyIdentifier.toString());
    localStringBuilder.append(this.policyQualifiers + "  ]\n");
    return localStringBuilder.toString();
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    this.policyIdentifier.encode(localDerOutputStream1);
    if (!(this.policyQualifiers.isEmpty()))
    {
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      Iterator localIterator = this.policyQualifiers.iterator();
      while (localIterator.hasNext())
      {
        PolicyQualifierInfo localPolicyQualifierInfo = (PolicyQualifierInfo)localIterator.next();
        localDerOutputStream2.write(localPolicyQualifierInfo.getEncoded());
      }
      localDerOutputStream1.write(48, localDerOutputStream2);
    }
    paramDerOutputStream.write(48, localDerOutputStream1);
  }
}
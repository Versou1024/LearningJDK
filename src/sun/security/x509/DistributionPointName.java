package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class DistributionPointName
{
  private static final byte TAG_FULL_NAME = 0;
  private static final byte TAG_RELATIVE_NAME = 1;
  private GeneralNames fullName = null;
  private RDN relativeName = null;
  private volatile int hashCode;

  public DistributionPointName(GeneralNames paramGeneralNames)
  {
    if (paramGeneralNames == null)
      throw new IllegalArgumentException("fullName must not be null");
    this.fullName = paramGeneralNames;
  }

  public DistributionPointName(RDN paramRDN)
  {
    if (paramRDN == null)
      throw new IllegalArgumentException("relativeName must not be null");
    this.relativeName = paramRDN;
  }

  public DistributionPointName(DerValue paramDerValue)
    throws IOException
  {
    if ((paramDerValue.isContextSpecific(0)) && (paramDerValue.isConstructed()))
    {
      paramDerValue.resetTag(48);
      this.fullName = new GeneralNames(paramDerValue);
    }
    else if ((paramDerValue.isContextSpecific(1)) && (paramDerValue.isConstructed()))
    {
      paramDerValue.resetTag(49);
      this.relativeName = new RDN(paramDerValue);
    }
    else
    {
      throw new IOException("Invalid encoding for DistributionPointName");
    }
  }

  public GeneralNames getFullName()
  {
    return this.fullName;
  }

  public RDN getRelativeName()
  {
    return this.relativeName;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.fullName != null)
    {
      this.fullName.encode(localDerOutputStream);
      paramDerOutputStream.writeImplicit(DerValue.createTag(-128, true, 0), localDerOutputStream);
    }
    else
    {
      this.relativeName.encode(localDerOutputStream);
      paramDerOutputStream.writeImplicit(DerValue.createTag(-128, true, 1), localDerOutputStream);
    }
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof DistributionPointName))
      return false;
    DistributionPointName localDistributionPointName = (DistributionPointName)paramObject;
    return ((equals(this.fullName, localDistributionPointName.fullName)) && (equals(this.relativeName, localDistributionPointName.relativeName)));
  }

  public int hashCode()
  {
    int i = this.hashCode;
    if (i == 0)
    {
      i = 1;
      if (this.fullName != null)
        i += this.fullName.hashCode();
      else
        i += this.relativeName.hashCode();
      this.hashCode = i;
    }
    return i;
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    if (this.fullName != null)
      localStringBuilder.append("DistributionPointName:\n     " + this.fullName + "\n");
    else
      localStringBuilder.append("DistributionPointName:\n     " + this.relativeName + "\n");
    return localStringBuilder.toString();
  }

  private static boolean equals(Object paramObject1, Object paramObject2)
  {
    return ((paramObject1 == null) ? false : (paramObject2 == null) ? true : paramObject1.equals(paramObject2));
  }
}
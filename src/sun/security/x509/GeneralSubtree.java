package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class GeneralSubtree
{
  private static final byte TAG_MIN = 0;
  private static final byte TAG_MAX = 1;
  private static final int MIN_DEFAULT = 0;
  private GeneralName name;
  private int minimum = 0;
  private int maximum = -1;
  private int myhash = -1;

  public GeneralSubtree(GeneralName paramGeneralName, int paramInt1, int paramInt2)
  {
    this.name = paramGeneralName;
    this.minimum = paramInt1;
    this.maximum = paramInt2;
  }

  public GeneralSubtree(DerValue paramDerValue)
    throws IOException
  {
    if (paramDerValue.tag != 48)
      throw new IOException("Invalid encoding for GeneralSubtree.");
    this.name = new GeneralName(paramDerValue.data.getDerValue(), true);
    while (paramDerValue.data.available() != 0)
    {
      DerValue localDerValue = paramDerValue.data.getDerValue();
      if ((localDerValue.isContextSpecific(0)) && (!(localDerValue.isConstructed())))
      {
        localDerValue.resetTag(2);
        this.minimum = localDerValue.getInteger();
      }
      else if ((localDerValue.isContextSpecific(1)) && (!(localDerValue.isConstructed())))
      {
        localDerValue.resetTag(2);
        this.maximum = localDerValue.getInteger();
      }
      else
      {
        throw new IOException("Invalid encoding of GeneralSubtree.");
      }
    }
  }

  public GeneralName getName()
  {
    return this.name;
  }

  public int getMinimum()
  {
    return this.minimum;
  }

  public int getMaximum()
  {
    return this.maximum;
  }

  public String toString()
  {
    String str = "\n   GeneralSubtree: [\n    GeneralName: " + ((this.name == null) ? "" : this.name.toString()) + "\n    Minimum: " + this.minimum;
    if (this.maximum == -1)
      str = str + "\t    Maximum: undefined";
    else
      str = str + "\t    Maximum: " + this.maximum;
    str = str + "    ]\n";
    return str;
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof GeneralSubtree))
      return false;
    GeneralSubtree localGeneralSubtree = (GeneralSubtree)paramObject;
    if (this.name == null)
    {
      if (localGeneralSubtree.name == null)
        break label46;
      return false;
    }
    if (!(this.name.equals(localGeneralSubtree.name)))
      return false;
    if (this.minimum != localGeneralSubtree.minimum)
      label46: return false;
    return (this.maximum == localGeneralSubtree.maximum);
  }

  public int hashCode()
  {
    if (this.myhash == -1)
    {
      this.myhash = 17;
      if (this.name != null)
        this.myhash = (37 * this.myhash + this.name.hashCode());
      if (this.minimum != 0)
        this.myhash = (37 * this.myhash + this.minimum);
      if (this.maximum != -1)
        this.myhash = (37 * this.myhash + this.maximum);
    }
    return this.myhash;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream2;
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    this.name.encode(localDerOutputStream1);
    if (this.minimum != 0)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putInteger(this.minimum);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, false, 0), localDerOutputStream2);
    }
    if (this.maximum != -1)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putInteger(this.maximum);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, false, 1), localDerOutputStream2);
    }
    paramDerOutputStream.write(48, localDerOutputStream1);
  }
}
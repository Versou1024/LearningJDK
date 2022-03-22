package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EDIPartyName
  implements GeneralNameInterface
{
  private static final byte TAG_ASSIGNER = 0;
  private static final byte TAG_PARTYNAME = 1;
  private String assigner = null;
  private String party = null;
  private int myhash = -1;

  public EDIPartyName(String paramString1, String paramString2)
  {
    this.assigner = paramString1;
    this.party = paramString2;
  }

  public EDIPartyName(String paramString)
  {
    this.party = paramString;
  }

  public EDIPartyName(DerValue paramDerValue)
    throws IOException
  {
    DerInputStream localDerInputStream = new DerInputStream(paramDerValue.toByteArray());
    DerValue[] arrayOfDerValue = localDerInputStream.getSequence(2);
    int i = arrayOfDerValue.length;
    if ((i < 1) || (i > 2))
      throw new IOException("Invalid encoding of EDIPartyName");
    for (int j = 0; j < i; ++j)
    {
      DerValue localDerValue = arrayOfDerValue[j];
      if ((localDerValue.isContextSpecific(0)) && (!(localDerValue.isConstructed())))
      {
        if (this.assigner != null)
          throw new IOException("Duplicate nameAssigner found in EDIPartyName");
        localDerValue = localDerValue.data.getDerValue();
        this.assigner = localDerValue.getAsString();
      }
      if ((localDerValue.isContextSpecific(1)) && (!(localDerValue.isConstructed())))
      {
        if (this.party != null)
          throw new IOException("Duplicate partyName found in EDIPartyName");
        localDerValue = localDerValue.data.getDerValue();
        this.party = localDerValue.getAsString();
      }
    }
  }

  public int getType()
  {
    return 5;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    if (this.assigner != null)
    {
      DerOutputStream localDerOutputStream3 = new DerOutputStream();
      localDerOutputStream3.putPrintableString(this.assigner);
      localDerOutputStream1.write(DerValue.createTag(-128, false, 0), localDerOutputStream3);
    }
    if (this.party == null)
      throw new IOException("Cannot have null partyName");
    localDerOutputStream2.putPrintableString(this.party);
    localDerOutputStream1.write(DerValue.createTag(-128, false, 1), localDerOutputStream2);
    paramDerOutputStream.write(48, localDerOutputStream1);
  }

  public String getAssignerName()
  {
    return this.assigner;
  }

  public String getPartyName()
  {
    return this.party;
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof EDIPartyName))
      return false;
    String str1 = ((EDIPartyName)paramObject).assigner;
    if (this.assigner == null)
    {
      if (str1 == null)
        break label43;
      return false;
    }
    if (!(this.assigner.equals(str1)))
      return false;
    label43: String str2 = ((EDIPartyName)paramObject).party;
    if (this.party == null)
    {
      if (str2 == null)
        break label77;
      return false;
    }
    label77: return (this.party.equals(str2));
  }

  public int hashCode()
  {
    if (this.myhash == -1)
    {
      this.myhash = (37 + this.party.hashCode());
      if (this.assigner != null)
        this.myhash = (37 * this.myhash + this.assigner.hashCode());
    }
    return this.myhash;
  }

  public String toString()
  {
    return "EDIPartyName: " + ((this.assigner == null) ? "" : new StringBuilder().append("  nameAssigner = ").append(this.assigner).append(",").toString()) + "  partyName = " + this.party;
  }

  public int constrains(GeneralNameInterface paramGeneralNameInterface)
    throws UnsupportedOperationException
  {
    int i;
    if (paramGeneralNameInterface == null)
      i = -1;
    else if (paramGeneralNameInterface.getType() != 5)
      i = -1;
    else
      throw new UnsupportedOperationException("Narrowing, widening, and matching of names not supported for EDIPartyName");
    return i;
  }

  public int subtreeDepth()
    throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("subtreeDepth() not supported for EDIPartyName");
  }
}
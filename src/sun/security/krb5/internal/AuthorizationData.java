package sun.security.krb5.internal;

import java.io.IOException;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.internal.ccache.CCacheOutputStream;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class AuthorizationData
  implements Cloneable
{
  private AuthorizationDataEntry[] entry = null;

  private AuthorizationData()
  {
  }

  public AuthorizationData(AuthorizationDataEntry[] paramArrayOfAuthorizationDataEntry)
    throws IOException
  {
    if (paramArrayOfAuthorizationDataEntry != null)
    {
      this.entry = new AuthorizationDataEntry[paramArrayOfAuthorizationDataEntry.length];
      for (int i = 0; i < paramArrayOfAuthorizationDataEntry.length; ++i)
      {
        if (paramArrayOfAuthorizationDataEntry[i] == null)
          throw new IOException("Cannot create an AuthorizationData");
        this.entry[i] = ((AuthorizationDataEntry)paramArrayOfAuthorizationDataEntry[i].clone());
      }
    }
  }

  public AuthorizationData(AuthorizationDataEntry paramAuthorizationDataEntry)
  {
    this.entry = new AuthorizationDataEntry[1];
    this.entry[0] = paramAuthorizationDataEntry;
  }

  public Object clone()
  {
    AuthorizationData localAuthorizationData = new AuthorizationData();
    if (this.entry != null)
    {
      localAuthorizationData.entry = new AuthorizationDataEntry[this.entry.length];
      for (int i = 0; i < this.entry.length; ++i)
        localAuthorizationData.entry[i] = ((AuthorizationDataEntry)this.entry[i].clone());
    }
    return localAuthorizationData;
  }

  public AuthorizationData(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    Vector localVector = new Vector();
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    while (paramDerValue.getData().available() > 0)
      localVector.addElement(new AuthorizationDataEntry(paramDerValue.getData().getDerValue()));
    if (localVector.size() > 0)
    {
      this.entry = new AuthorizationDataEntry[localVector.size()];
      localVector.copyInto(this.entry);
    }
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    DerValue[] arrayOfDerValue = new DerValue[this.entry.length];
    for (int i = 0; i < this.entry.length; ++i)
      arrayOfDerValue[i] = new DerValue(this.entry[i].asn1Encode());
    localDerOutputStream.putSequence(arrayOfDerValue);
    return localDerOutputStream.toByteArray();
  }

  public static AuthorizationData parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new AuthorizationData(localDerValue2);
  }

  public void writeAuth(CCacheOutputStream paramCCacheOutputStream)
    throws IOException
  {
    for (int i = 0; i < this.entry.length; ++i)
      this.entry[i].writeEntry(paramCCacheOutputStream);
  }

  public String toString()
  {
    String str = "AuthorizationData:\n";
    for (int i = 0; i < this.entry.length; ++i)
      str = str + this.entry[i].toString();
    return str;
  }
}
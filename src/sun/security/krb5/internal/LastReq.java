package sun.security.krb5.internal;

import java.io.IOException;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class LastReq
{
  private LastReqEntry[] entry = null;

  public LastReq(LastReqEntry[] paramArrayOfLastReqEntry)
    throws IOException
  {
    if (paramArrayOfLastReqEntry != null)
    {
      this.entry = new LastReqEntry[paramArrayOfLastReqEntry.length];
      for (int i = 0; i < paramArrayOfLastReqEntry.length; ++i)
      {
        if (paramArrayOfLastReqEntry[i] == null)
          throw new IOException("Cannot create a LastReqEntry");
        this.entry[i] = ((LastReqEntry)paramArrayOfLastReqEntry[i].clone());
      }
    }
  }

  public LastReq(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    Vector localVector = new Vector();
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    while (paramDerValue.getData().available() > 0)
      localVector.addElement(new LastReqEntry(paramDerValue.getData().getDerValue()));
    if (localVector.size() > 0)
    {
      this.entry = new LastReqEntry[localVector.size()];
      localVector.copyInto(this.entry);
    }
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    if ((this.entry != null) && (this.entry.length > 0))
    {
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      for (int i = 0; i < this.entry.length; ++i)
        localDerOutputStream2.write(this.entry[i].asn1Encode());
      localDerOutputStream1.write(48, localDerOutputStream2);
      return localDerOutputStream1.toByteArray();
    }
    return null;
  }

  public static LastReq parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new LastReq(localDerValue2);
  }
}
package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptedData;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KRBCred
{
  public Ticket[] tickets = null;
  public EncryptedData encPart;
  private int pvno;
  private int msgType;

  public KRBCred(Ticket[] paramArrayOfTicket, EncryptedData paramEncryptedData)
    throws IOException
  {
    this.pvno = 5;
    this.msgType = 22;
    if (paramArrayOfTicket != null)
    {
      this.tickets = new Ticket[paramArrayOfTicket.length];
      for (int i = 0; i < paramArrayOfTicket.length; ++i)
      {
        if (paramArrayOfTicket[i] == null)
          throw new IOException("Cannot create a KRBCred");
        this.tickets[i] = ((Ticket)paramArrayOfTicket[i].clone());
      }
    }
    this.encPart = paramEncryptedData;
  }

  public KRBCred(byte[] paramArrayOfByte)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public KRBCred(DerValue paramDerValue)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    if (((paramDerValue.getTag() & 0x1F) != 22) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 0)
    {
      this.pvno = localDerValue2.getData().getBigInteger().intValue();
      if (this.pvno == 5)
        break label128;
      throw new sun.security.krb5.internal.KrbApErrException(39);
    }
    throw new Asn1Exception(906);
    label128: localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 1)
    {
      this.msgType = localDerValue2.getData().getBigInteger().intValue();
      if (this.msgType == 22)
        break label191;
      throw new sun.security.krb5.internal.KrbApErrException(40);
    }
    throw new Asn1Exception(906);
    label191: localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 2)
    {
      DerValue localDerValue3 = localDerValue2.getData().getDerValue();
      if (localDerValue3.getTag() != 48)
        throw new Asn1Exception(906);
      Vector localVector = new Vector();
      while (localDerValue3.getData().available() > 0)
        localVector.addElement(new Ticket(localDerValue3.getData().getDerValue()));
      if (localVector.size() > 0)
      {
        this.tickets = new Ticket[localVector.size()];
        localVector.copyInto(this.tickets);
      }
    }
    else
    {
      throw new Asn1Exception(906);
    }
    this.encPart = EncryptedData.parse(localDerValue1.getData(), 3, false);
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.pvno));
    DerOutputStream localDerOutputStream3 = new DerOutputStream();
    localDerOutputStream3.write(DerValue.createTag(-128, true, 0), localDerOutputStream1);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.msgType));
    localDerOutputStream3.write(DerValue.createTag(-128, true, 1), localDerOutputStream1);
    localDerOutputStream1 = new DerOutputStream();
    for (int i = 0; i < this.tickets.length; ++i)
      localDerOutputStream1.write(this.tickets[i].asn1Encode());
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    localDerOutputStream3.write(DerValue.createTag(-128, true, 2), localDerOutputStream2);
    localDerOutputStream3.write(DerValue.createTag(-128, true, 3), this.encPart.asn1Encode());
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream3);
    localDerOutputStream3 = new DerOutputStream();
    localDerOutputStream3.write(DerValue.createTag(64, true, 22), localDerOutputStream2);
    return localDerOutputStream3.toByteArray();
  }
}
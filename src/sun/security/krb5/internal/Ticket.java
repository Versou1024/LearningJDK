package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptedData;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class Ticket
  implements Cloneable
{
  public int tkt_vno;
  public Realm realm;
  public PrincipalName sname;
  public EncryptedData encPart;

  private Ticket()
  {
  }

  public Object clone()
  {
    Ticket localTicket = new Ticket();
    localTicket.realm = ((Realm)this.realm.clone());
    localTicket.sname = ((PrincipalName)this.sname.clone());
    localTicket.encPart = ((EncryptedData)this.encPart.clone());
    localTicket.tkt_vno = this.tkt_vno;
    return localTicket;
  }

  public Ticket(Realm paramRealm, PrincipalName paramPrincipalName, EncryptedData paramEncryptedData)
  {
    this.tkt_vno = 5;
    this.realm = paramRealm;
    this.sname = paramPrincipalName;
    this.encPart = paramEncryptedData;
  }

  public Ticket(byte[] paramArrayOfByte)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public Ticket(DerValue paramDerValue)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    if (((paramDerValue.getTag() & 0x1F) != 1) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) != 0)
      throw new Asn1Exception(906);
    this.tkt_vno = localDerValue2.getData().getBigInteger().intValue();
    if (this.tkt_vno != 5)
      throw new sun.security.krb5.internal.KrbApErrException(39);
    this.realm = Realm.parse(localDerValue1.getData(), 1, false);
    this.sname = PrincipalName.parse(localDerValue1.getData(), 2, false);
    this.encPart = EncryptedData.parse(localDerValue1.getData(), 3, false);
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    DerValue[] arrayOfDerValue = new DerValue[4];
    localDerOutputStream2.putInteger(BigInteger.valueOf(this.tkt_vno));
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), this.realm.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 2), this.sname.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 3), this.encPart.asn1Encode());
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    DerOutputStream localDerOutputStream3 = new DerOutputStream();
    localDerOutputStream3.write(DerValue.createTag(64, true, 1), localDerOutputStream2);
    return localDerOutputStream3.toByteArray();
  }

  public static Ticket parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException, RealmException, sun.security.krb5.internal.KrbApErrException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new Ticket(localDerValue2);
  }
}
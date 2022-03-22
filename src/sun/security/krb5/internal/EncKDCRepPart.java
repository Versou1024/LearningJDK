package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EncKDCRepPart
{
  public EncryptionKey key;
  public LastReq lastReq;
  public int nonce;
  public KerberosTime keyExpiration;
  public TicketFlags flags;
  public KerberosTime authtime;
  public KerberosTime starttime;
  public KerberosTime endtime;
  public KerberosTime renewTill;
  public Realm srealm;
  public PrincipalName sname;
  public HostAddresses caddr;
  public int msgType;

  public EncKDCRepPart(EncryptionKey paramEncryptionKey, LastReq paramLastReq, int paramInt1, KerberosTime paramKerberosTime1, TicketFlags paramTicketFlags, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, KerberosTime paramKerberosTime4, KerberosTime paramKerberosTime5, Realm paramRealm, PrincipalName paramPrincipalName, HostAddresses paramHostAddresses, int paramInt2)
  {
    this.key = paramEncryptionKey;
    this.lastReq = paramLastReq;
    this.nonce = paramInt1;
    this.keyExpiration = paramKerberosTime1;
    this.flags = paramTicketFlags;
    this.authtime = paramKerberosTime2;
    this.starttime = paramKerberosTime3;
    this.endtime = paramKerberosTime4;
    this.renewTill = paramKerberosTime5;
    this.srealm = paramRealm;
    this.sname = paramPrincipalName;
    this.caddr = paramHostAddresses;
    this.msgType = paramInt2;
  }

  public EncKDCRepPart()
  {
  }

  public EncKDCRepPart(byte[] paramArrayOfByte, int paramInt)
    throws Asn1Exception, IOException, RealmException
  {
    init(new DerValue(paramArrayOfByte), paramInt);
  }

  public EncKDCRepPart(DerValue paramDerValue, int paramInt)
    throws Asn1Exception, IOException, RealmException
  {
    init(paramDerValue, paramInt);
  }

  protected void init(DerValue paramDerValue, int paramInt)
    throws Asn1Exception, IOException, RealmException
  {
    this.msgType = (paramDerValue.getTag() & 0x1F);
    if ((this.msgType != 25) && (this.msgType != 26))
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    this.key = EncryptionKey.parse(localDerValue1.getData(), 0, false);
    this.lastReq = LastReq.parse(localDerValue1.getData(), 1, false);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 2)
      this.nonce = localDerValue2.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    this.keyExpiration = KerberosTime.parse(localDerValue1.getData(), 3, true);
    this.flags = TicketFlags.parse(localDerValue1.getData(), 4, false);
    this.authtime = KerberosTime.parse(localDerValue1.getData(), 5, false);
    this.starttime = KerberosTime.parse(localDerValue1.getData(), 6, true);
    this.endtime = KerberosTime.parse(localDerValue1.getData(), 7, false);
    this.renewTill = KerberosTime.parse(localDerValue1.getData(), 8, true);
    this.srealm = Realm.parse(localDerValue1.getData(), 9, false);
    this.sname = PrincipalName.parse(localDerValue1.getData(), 10, false);
    if (localDerValue1.getData().available() > 0)
      this.caddr = HostAddresses.parse(localDerValue1.getData(), 11, true);
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode(int paramInt)
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(DerValue.createTag(-128, true, 0), this.key.asn1Encode());
    localDerOutputStream2.write(DerValue.createTag(-128, true, 1), this.lastReq.asn1Encode());
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.nonce));
    localDerOutputStream2.write(DerValue.createTag(-128, true, 2), localDerOutputStream1);
    if (this.keyExpiration != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 3), this.keyExpiration.asn1Encode());
    localDerOutputStream2.write(DerValue.createTag(-128, true, 4), this.flags.asn1Encode());
    localDerOutputStream2.write(DerValue.createTag(-128, true, 5), this.authtime.asn1Encode());
    if (this.starttime != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 6), this.starttime.asn1Encode());
    localDerOutputStream2.write(DerValue.createTag(-128, true, 7), this.endtime.asn1Encode());
    if (this.renewTill != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 8), this.renewTill.asn1Encode());
    localDerOutputStream2.write(DerValue.createTag(-128, true, 9), this.srealm.asn1Encode());
    localDerOutputStream2.write(DerValue.createTag(-128, true, 10), this.sname.asn1Encode());
    if (this.caddr != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 11), this.caddr.asn1Encode());
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.write(48, localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(DerValue.createTag(64, true, (byte)this.msgType), localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }
}
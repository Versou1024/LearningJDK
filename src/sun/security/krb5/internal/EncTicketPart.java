package sun.security.krb5.internal;

import java.io.IOException;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EncTicketPart
{
  public TicketFlags flags;
  public EncryptionKey key;
  public Realm crealm;
  public PrincipalName cname;
  public TransitedEncoding transited;
  public KerberosTime authtime;
  public KerberosTime starttime;
  public KerberosTime endtime;
  public KerberosTime renewTill;
  public HostAddresses caddr;
  public AuthorizationData authorizationData;

  public EncTicketPart(TicketFlags paramTicketFlags, EncryptionKey paramEncryptionKey, Realm paramRealm, PrincipalName paramPrincipalName, TransitedEncoding paramTransitedEncoding, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, KerberosTime paramKerberosTime4, HostAddresses paramHostAddresses, AuthorizationData paramAuthorizationData)
  {
    this.flags = paramTicketFlags;
    this.key = paramEncryptionKey;
    this.crealm = paramRealm;
    this.cname = paramPrincipalName;
    this.transited = paramTransitedEncoding;
    this.authtime = paramKerberosTime1;
    this.starttime = paramKerberosTime2;
    this.endtime = paramKerberosTime3;
    this.renewTill = paramKerberosTime4;
    this.caddr = paramHostAddresses;
    this.authorizationData = paramAuthorizationData;
  }

  public EncTicketPart(byte[] paramArrayOfByte)
    throws Asn1Exception, KrbException, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public EncTicketPart(DerValue paramDerValue)
    throws Asn1Exception, KrbException, IOException
  {
    init(paramDerValue);
  }

  private static String getHexBytes(byte[] paramArrayOfByte, int paramInt)
    throws IOException
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < paramInt; ++i)
    {
      int j = paramArrayOfByte[i] >> 4 & 0xF;
      int k = paramArrayOfByte[i] & 0xF;
      localStringBuffer.append(Integer.toHexString(j));
      localStringBuffer.append(Integer.toHexString(k));
      localStringBuffer.append(' ');
    }
    return localStringBuffer.toString();
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, IOException, RealmException
  {
    this.renewTill = null;
    this.caddr = null;
    this.authorizationData = null;
    if (((paramDerValue.getTag() & 0x1F) != 3) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
      throw new Asn1Exception(906);
    DerValue localDerValue = paramDerValue.getData().getDerValue();
    if (localDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    this.flags = TicketFlags.parse(localDerValue.getData(), 0, false);
    this.key = EncryptionKey.parse(localDerValue.getData(), 1, false);
    this.crealm = Realm.parse(localDerValue.getData(), 2, false);
    this.cname = PrincipalName.parse(localDerValue.getData(), 3, false);
    this.transited = TransitedEncoding.parse(localDerValue.getData(), 4, false);
    this.authtime = KerberosTime.parse(localDerValue.getData(), 5, false);
    this.starttime = KerberosTime.parse(localDerValue.getData(), 6, true);
    this.endtime = KerberosTime.parse(localDerValue.getData(), 7, false);
    if (localDerValue.getData().available() > 0)
      this.renewTill = KerberosTime.parse(localDerValue.getData(), 8, true);
    if (localDerValue.getData().available() > 0)
      this.caddr = HostAddresses.parse(localDerValue.getData(), 9, true);
    if (localDerValue.getData().available() > 0)
      this.authorizationData = AuthorizationData.parse(localDerValue.getData(), 10, true);
    if (localDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), this.flags.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), this.key.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 2), this.crealm.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 3), this.cname.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 4), this.transited.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 5), this.authtime.asn1Encode());
    if (this.starttime != null)
      localDerOutputStream1.write(DerValue.createTag(-128, true, 6), this.starttime.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 7), this.endtime.asn1Encode());
    if (this.renewTill != null)
      localDerOutputStream1.write(DerValue.createTag(-128, true, 8), this.renewTill.asn1Encode());
    if (this.caddr != null)
      localDerOutputStream1.write(DerValue.createTag(-128, true, 9), this.caddr.asn1Encode());
    if (this.authorizationData != null)
      localDerOutputStream1.write(DerValue.createTag(-128, true, 10), this.authorizationData.asn1Encode());
    localDerOutputStream2.write(48, localDerOutputStream1);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.write(DerValue.createTag(64, true, 3), localDerOutputStream2);
    return localDerOutputStream1.toByteArray();
  }
}
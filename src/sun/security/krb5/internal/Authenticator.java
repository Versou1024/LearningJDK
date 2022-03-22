package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.Checksum;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class Authenticator
{
  public int authenticator_vno;
  public Realm crealm;
  public PrincipalName cname;
  Checksum cksum;
  public int cusec;
  public KerberosTime ctime;
  EncryptionKey subKey;
  Integer seqNumber;
  public AuthorizationData authorizationData;

  public Authenticator(Realm paramRealm, PrincipalName paramPrincipalName, Checksum paramChecksum, int paramInt, KerberosTime paramKerberosTime, EncryptionKey paramEncryptionKey, Integer paramInteger, AuthorizationData paramAuthorizationData)
  {
    this.authenticator_vno = 5;
    this.crealm = paramRealm;
    this.cname = paramPrincipalName;
    this.cksum = paramChecksum;
    this.cusec = paramInt;
    this.ctime = paramKerberosTime;
    this.subKey = paramEncryptionKey;
    this.seqNumber = paramInteger;
    this.authorizationData = paramAuthorizationData;
  }

  public Authenticator(byte[] paramArrayOfByte)
    throws Asn1Exception, IOException, sun.security.krb5.internal.KrbApErrException, RealmException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public Authenticator(DerValue paramDerValue)
    throws Asn1Exception, IOException, sun.security.krb5.internal.KrbApErrException, RealmException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, IOException, sun.security.krb5.internal.KrbApErrException, RealmException
  {
    if (((paramDerValue.getTag() & 0x1F) != 2) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) != 0)
      throw new Asn1Exception(906);
    this.authenticator_vno = localDerValue2.getData().getBigInteger().intValue();
    if (this.authenticator_vno != 5)
      throw new sun.security.krb5.internal.KrbApErrException(39);
    this.crealm = Realm.parse(localDerValue1.getData(), 1, false);
    this.cname = PrincipalName.parse(localDerValue1.getData(), 2, false);
    this.cksum = Checksum.parse(localDerValue1.getData(), 3, true);
    localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 4)
      this.cusec = localDerValue2.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    this.ctime = KerberosTime.parse(localDerValue1.getData(), 5, false);
    if (localDerValue1.getData().available() > 0)
    {
      this.subKey = EncryptionKey.parse(localDerValue1.getData(), 6, true);
    }
    else
    {
      this.subKey = null;
      this.seqNumber = null;
      this.authorizationData = null;
    }
    if (localDerValue1.getData().available() > 0)
    {
      if ((localDerValue1.getData().peekByte() & 0x1F) == 7)
      {
        localDerValue2 = localDerValue1.getData().getDerValue();
        if ((localDerValue2.getTag() & 0x1F) == 7)
          this.seqNumber = new Integer(localDerValue2.getData().getBigInteger().intValue());
      }
    }
    else
    {
      this.seqNumber = null;
      this.authorizationData = null;
    }
    if (localDerValue1.getData().available() > 0)
      this.authorizationData = AuthorizationData.parse(localDerValue1.getData(), 8, true);
    else
      this.authorizationData = null;
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    Vector localVector = new Vector();
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.authenticator_vno));
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 0), localDerOutputStream1.toByteArray()));
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 1), this.crealm.asn1Encode()));
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 2), this.cname.asn1Encode()));
    if (this.cksum != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 3), this.cksum.asn1Encode()));
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.cusec));
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 4), localDerOutputStream1.toByteArray()));
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 5), this.ctime.asn1Encode()));
    if (this.subKey != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 6), this.subKey.asn1Encode()));
    if (this.seqNumber != null)
    {
      localDerOutputStream1 = new DerOutputStream();
      localDerOutputStream1.putInteger(BigInteger.valueOf(this.seqNumber.longValue()));
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 7), localDerOutputStream1.toByteArray()));
    }
    if (this.authorizationData != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 8), this.authorizationData.asn1Encode()));
    DerValue[] arrayOfDerValue = new DerValue[localVector.size()];
    localVector.copyInto(arrayOfDerValue);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putSequence(arrayOfDerValue);
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(DerValue.createTag(64, true, 2), localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public final Checksum getChecksum()
  {
    return this.cksum;
  }

  public final Integer getSeqNumber()
  {
    return this.seqNumber;
  }

  public final EncryptionKey getSubKey()
  {
    return this.subKey;
  }
}
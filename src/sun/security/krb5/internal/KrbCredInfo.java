package sun.security.krb5.internal;

import java.io.IOException;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KrbCredInfo
{
  public EncryptionKey key;
  public Realm prealm;
  public PrincipalName pname;
  public TicketFlags flags;
  public KerberosTime authtime;
  public KerberosTime starttime;
  public KerberosTime endtime;
  public KerberosTime renewTill;
  public Realm srealm;
  public PrincipalName sname;
  public HostAddresses caddr;

  private KrbCredInfo()
  {
  }

  public KrbCredInfo(EncryptionKey paramEncryptionKey, Realm paramRealm1, PrincipalName paramPrincipalName1, TicketFlags paramTicketFlags, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, KerberosTime paramKerberosTime4, Realm paramRealm2, PrincipalName paramPrincipalName2, HostAddresses paramHostAddresses)
  {
    this.key = paramEncryptionKey;
    this.prealm = paramRealm1;
    this.pname = paramPrincipalName1;
    this.flags = paramTicketFlags;
    this.authtime = paramKerberosTime1;
    this.starttime = paramKerberosTime2;
    this.endtime = paramKerberosTime3;
    this.renewTill = paramKerberosTime4;
    this.srealm = paramRealm2;
    this.sname = paramPrincipalName2;
    this.caddr = paramHostAddresses;
  }

  public KrbCredInfo(DerValue paramDerValue)
    throws Asn1Exception, IOException, RealmException
  {
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    this.prealm = null;
    this.pname = null;
    this.flags = null;
    this.authtime = null;
    this.starttime = null;
    this.endtime = null;
    this.renewTill = null;
    this.srealm = null;
    this.sname = null;
    this.caddr = null;
    this.key = EncryptionKey.parse(paramDerValue.getData(), 0, false);
    if (paramDerValue.getData().available() > 0)
      this.prealm = Realm.parse(paramDerValue.getData(), 1, true);
    if (paramDerValue.getData().available() > 0)
      this.pname = PrincipalName.parse(paramDerValue.getData(), 2, true);
    if (paramDerValue.getData().available() > 0)
      this.flags = TicketFlags.parse(paramDerValue.getData(), 3, true);
    if (paramDerValue.getData().available() > 0)
      this.authtime = KerberosTime.parse(paramDerValue.getData(), 4, true);
    if (paramDerValue.getData().available() > 0)
      this.starttime = KerberosTime.parse(paramDerValue.getData(), 5, true);
    if (paramDerValue.getData().available() > 0)
      this.endtime = KerberosTime.parse(paramDerValue.getData(), 6, true);
    if (paramDerValue.getData().available() > 0)
      this.renewTill = KerberosTime.parse(paramDerValue.getData(), 7, true);
    if (paramDerValue.getData().available() > 0)
      this.srealm = Realm.parse(paramDerValue.getData(), 8, true);
    if (paramDerValue.getData().available() > 0)
      this.sname = PrincipalName.parse(paramDerValue.getData(), 9, true);
    if (paramDerValue.getData().available() > 0)
      this.caddr = HostAddresses.parse(paramDerValue.getData(), 10, true);
    if (paramDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    Vector localVector = new Vector();
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 0), this.key.asn1Encode()));
    if (this.prealm != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 1), this.prealm.asn1Encode()));
    if (this.pname != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 2), this.pname.asn1Encode()));
    if (this.flags != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 3), this.flags.asn1Encode()));
    if (this.authtime != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 4), this.authtime.asn1Encode()));
    if (this.starttime != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 5), this.starttime.asn1Encode()));
    if (this.endtime != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 6), this.endtime.asn1Encode()));
    if (this.renewTill != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 7), this.renewTill.asn1Encode()));
    if (this.srealm != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 8), this.srealm.asn1Encode()));
    if (this.sname != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 9), this.sname.asn1Encode()));
    if (this.caddr != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 10), this.caddr.asn1Encode()));
    DerValue[] arrayOfDerValue = new DerValue[localVector.size()];
    localVector.copyInto(arrayOfDerValue);
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putSequence(arrayOfDerValue);
    return localDerOutputStream.toByteArray();
  }

  public Object clone()
  {
    KrbCredInfo localKrbCredInfo = new KrbCredInfo();
    localKrbCredInfo.key = ((EncryptionKey)this.key.clone());
    if (this.prealm != null)
      localKrbCredInfo.prealm = ((Realm)this.prealm.clone());
    if (this.pname != null)
      localKrbCredInfo.pname = ((PrincipalName)this.pname.clone());
    if (this.flags != null)
      localKrbCredInfo.flags = ((TicketFlags)this.flags.clone());
    if (this.authtime != null)
      localKrbCredInfo.authtime = ((KerberosTime)this.authtime.clone());
    if (this.starttime != null)
      localKrbCredInfo.starttime = ((KerberosTime)this.starttime.clone());
    if (this.endtime != null)
      localKrbCredInfo.endtime = ((KerberosTime)this.endtime.clone());
    if (this.renewTill != null)
      localKrbCredInfo.renewTill = ((KerberosTime)this.renewTill.clone());
    if (this.srealm != null)
      localKrbCredInfo.srealm = ((Realm)this.srealm.clone());
    if (this.sname != null)
      localKrbCredInfo.sname = ((PrincipalName)this.sname.clone());
    if (this.caddr != null)
      localKrbCredInfo.caddr = ((HostAddresses)this.caddr.clone());
    return localKrbCredInfo;
  }
}
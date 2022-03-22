package sun.security.krb5.internal;

import B;
import I;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptedData;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KDCReqBody
{
  public KDCOptions kdcOptions;
  public PrincipalName cname;
  public Realm crealm;
  public PrincipalName sname;
  public KerberosTime from;
  public KerberosTime till;
  public KerberosTime rtime;
  public HostAddresses addresses;
  private int nonce;
  private int[] eType = null;
  private EncryptedData encAuthorizationData;
  private Ticket[] additionalTickets;

  public KDCReqBody(KDCOptions paramKDCOptions, PrincipalName paramPrincipalName1, Realm paramRealm, PrincipalName paramPrincipalName2, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, int paramInt, int[] paramArrayOfInt, HostAddresses paramHostAddresses, EncryptedData paramEncryptedData, Ticket[] paramArrayOfTicket)
    throws IOException
  {
    this.kdcOptions = paramKDCOptions;
    this.cname = paramPrincipalName1;
    this.crealm = paramRealm;
    this.sname = paramPrincipalName2;
    this.from = paramKerberosTime1;
    this.till = paramKerberosTime2;
    this.rtime = paramKerberosTime3;
    this.nonce = paramInt;
    if (paramArrayOfInt != null)
      this.eType = ((int[])(int[])paramArrayOfInt.clone());
    this.addresses = paramHostAddresses;
    this.encAuthorizationData = paramEncryptedData;
    if (paramArrayOfTicket != null)
    {
      this.additionalTickets = new Ticket[paramArrayOfTicket.length];
      for (int i = 0; i < paramArrayOfTicket.length; ++i)
      {
        if (paramArrayOfTicket[i] == null)
          throw new IOException("Cannot create a KDCReqBody");
        this.additionalTickets[i] = ((Ticket)paramArrayOfTicket[i].clone());
      }
    }
  }

  public KDCReqBody(DerValue paramDerValue, int paramInt)
    throws Asn1Exception, RealmException, KrbException, IOException
  {
    this.addresses = null;
    this.encAuthorizationData = null;
    this.additionalTickets = null;
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    this.kdcOptions = KDCOptions.parse(paramDerValue.getData(), 0, false);
    this.cname = PrincipalName.parse(paramDerValue.getData(), 1, true);
    if ((paramInt != 10) && (this.cname != null))
      throw new Asn1Exception(906);
    this.crealm = Realm.parse(paramDerValue.getData(), 2, false);
    this.sname = PrincipalName.parse(paramDerValue.getData(), 3, true);
    this.from = KerberosTime.parse(paramDerValue.getData(), 4, true);
    this.till = KerberosTime.parse(paramDerValue.getData(), 5, false);
    this.rtime = KerberosTime.parse(paramDerValue.getData(), 6, true);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if ((localDerValue1.getTag() & 0x1F) == 7)
      this.nonce = localDerValue1.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    localDerValue1 = paramDerValue.getData().getDerValue();
    Vector localVector1 = new Vector();
    if ((localDerValue1.getTag() & 0x1F) == 8)
    {
      localDerValue2 = localDerValue1.getData().getDerValue();
      if (localDerValue2.getTag() == 48)
      {
        while (localDerValue2.getData().available() > 0)
          localVector1.addElement(new Integer(localDerValue2.getData().getBigInteger().intValue()));
        this.eType = new int[localVector1.size()];
        for (int i = 0; i < localVector1.size(); ++i)
          this.eType[i] = ((Integer)localVector1.elementAt(i)).intValue();
        break label368:
      }
      throw new Asn1Exception(906);
    }
    throw new Asn1Exception(906);
    if (paramDerValue.getData().available() > 0)
      label368: this.addresses = HostAddresses.parse(paramDerValue.getData(), 9, true);
    if (paramDerValue.getData().available() > 0)
      this.encAuthorizationData = EncryptedData.parse(paramDerValue.getData(), 10, true);
    if (paramDerValue.getData().available() > 0)
    {
      Vector localVector2 = new Vector();
      localDerValue1 = paramDerValue.getData().getDerValue();
      if ((localDerValue1.getTag() & 0x1F) == 11)
      {
        localDerValue2 = localDerValue1.getData().getDerValue();
        if (localDerValue2.getTag() == 48)
          while (true)
          {
            if (localDerValue2.getData().available() <= 0)
              break label519;
            localVector2.addElement(new Ticket(localDerValue2.getData().getDerValue()));
          }
        throw new Asn1Exception(906);
        if (localVector2.size() > 0)
        {
          label519: this.additionalTickets = new Ticket[localVector2.size()];
          localVector2.copyInto(this.additionalTickets);
        }
      }
      else
      {
        throw new Asn1Exception(906);
      }
    }
    if (paramDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode(int paramInt)
    throws Asn1Exception, IOException
  {
    Vector localVector = new Vector();
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 0), this.kdcOptions.asn1Encode()));
    if ((paramInt == 10) && (this.cname != null))
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 1), this.cname.asn1Encode()));
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 2), this.crealm.asn1Encode()));
    if (this.sname != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 3), this.sname.asn1Encode()));
    if (this.from != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 4), this.from.asn1Encode()));
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 5), this.till.asn1Encode()));
    if (this.rtime != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 6), this.rtime.asn1Encode()));
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.nonce));
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 7), localDerOutputStream1.toByteArray()));
    localDerOutputStream1 = new DerOutputStream();
    for (int i = 0; i < this.eType.length; ++i)
      localDerOutputStream1.putInteger(BigInteger.valueOf(this.eType[i]));
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 8), localDerOutputStream2.toByteArray()));
    if (this.addresses != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 9), this.addresses.asn1Encode()));
    if (this.encAuthorizationData != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 10), this.encAuthorizationData.asn1Encode()));
    if ((this.additionalTickets != null) && (this.additionalTickets.length > 0))
    {
      localDerOutputStream1 = new DerOutputStream();
      for (int j = 0; j < this.additionalTickets.length; ++j)
        localDerOutputStream1.write(this.additionalTickets[j].asn1Encode());
      localObject = new DerOutputStream();
      ((DerOutputStream)localObject).write(48, localDerOutputStream1);
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 11), ((DerOutputStream)localObject).toByteArray()));
    }
    Object localObject = new DerValue[localVector.size()];
    localVector.copyInto(localObject);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putSequence(localObject);
    return ((B)localDerOutputStream1.toByteArray());
  }
}
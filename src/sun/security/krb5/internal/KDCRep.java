package sun.security.krb5.internal;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptedData;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KDCRep
{
  public Realm crealm;
  public PrincipalName cname;
  public Ticket ticket;
  public EncryptedData encPart;
  public EncKDCRepPart encKDCRepPart;
  private int pvno;
  private int msgType;
  private PAData[] pAData = null;
  private boolean DEBUG = Krb5.DEBUG;

  public KDCRep(PAData[] paramArrayOfPAData, Realm paramRealm, PrincipalName paramPrincipalName, Ticket paramTicket, EncryptedData paramEncryptedData, int paramInt)
    throws IOException
  {
    this.pvno = 5;
    this.msgType = paramInt;
    if (paramArrayOfPAData != null)
    {
      this.pAData = new PAData[paramArrayOfPAData.length];
      for (int i = 0; i < paramArrayOfPAData.length; ++i)
      {
        if (paramArrayOfPAData[i] == null)
          throw new IOException("Cannot create a KDCRep");
        this.pAData[i] = ((PAData)paramArrayOfPAData[i].clone());
      }
    }
    this.crealm = paramRealm;
    this.cname = paramPrincipalName;
    this.ticket = paramTicket;
    this.encPart = paramEncryptedData;
  }

  public KDCRep()
  {
  }

  public KDCRep(byte[] paramArrayOfByte, int paramInt)
    throws Asn1Exception, sun.security.krb5.internal.KrbApErrException, RealmException, IOException
  {
    init(new DerValue(paramArrayOfByte), paramInt);
  }

  public KDCRep(DerValue paramDerValue, int paramInt)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(paramDerValue, paramInt);
  }

  protected void init(DerValue paramDerValue, int paramInt)
    throws Asn1Exception, RealmException, IOException, sun.security.krb5.internal.KrbApErrException
  {
    if ((paramDerValue.getTag() & 0x1F) != paramInt)
    {
      if (this.DEBUG)
        System.out.println(">>> KDCRep: init() encoding tag is " + paramDerValue.getTag() + " req type is " + paramInt);
      throw new Asn1Exception(906);
    }
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 0)
    {
      this.pvno = localDerValue2.getData().getBigInteger().intValue();
      if (this.pvno == 5)
        break label158;
      throw new sun.security.krb5.internal.KrbApErrException(39);
    }
    throw new Asn1Exception(906);
    label158: localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 1)
    {
      this.msgType = localDerValue2.getData().getBigInteger().intValue();
      if (this.msgType == paramInt)
        break label223;
      throw new sun.security.krb5.internal.KrbApErrException(40);
    }
    throw new Asn1Exception(906);
    if ((localDerValue1.getData().peekByte() & 0x1F) == 2)
    {
      label223: localDerValue2 = localDerValue1.getData().getDerValue();
      Vector localVector = new Vector();
      DerValue[] arrayOfDerValue = localDerValue2.getData().getSequence(1);
      this.pAData = new PAData[arrayOfDerValue.length];
      for (int i = 0; i < arrayOfDerValue.length; ++i)
        this.pAData[i] = new PAData(arrayOfDerValue[i]);
    }
    else
    {
      this.pAData = null;
    }
    this.crealm = Realm.parse(localDerValue1.getData(), 3, false);
    this.cname = PrincipalName.parse(localDerValue1.getData(), 4, false);
    this.ticket = Ticket.parse(localDerValue1.getData(), 5, false);
    this.encPart = EncryptedData.parse(localDerValue1.getData(), 6, false);
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(BigInteger.valueOf(this.pvno));
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(BigInteger.valueOf(this.msgType));
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    if ((this.pAData != null) && (this.pAData.length > 0))
    {
      DerOutputStream localDerOutputStream3 = new DerOutputStream();
      for (int i = 0; i < this.pAData.length; ++i)
        localDerOutputStream3.write(this.pAData[i].asn1Encode());
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.write(48, localDerOutputStream3);
      localDerOutputStream1.write(DerValue.createTag(-128, true, 2), localDerOutputStream2);
    }
    localDerOutputStream1.write(DerValue.createTag(-128, true, 3), this.crealm.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 4), this.cname.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 5), this.ticket.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 6), this.encPart.asn1Encode());
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }
}
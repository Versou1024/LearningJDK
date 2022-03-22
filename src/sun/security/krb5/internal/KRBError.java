package sun.security.krb5.internal;

import B;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Date;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.Checksum;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KRBError
{
  private int pvno;
  private int msgType;
  private KerberosTime cTime;
  private Integer cuSec;
  private KerberosTime sTime;
  private Integer suSec;
  private int errorCode;
  private Realm crealm;
  private PrincipalName cname;
  private Realm realm;
  private PrincipalName sname;
  private String eText;
  private byte[] eData;
  private Checksum eCksum;
  private int etype = 0;
  private byte[] salt = null;
  private byte[] s2kparams = null;
  private boolean DEBUG = Krb5.DEBUG;

  public KRBError(APOptions paramAPOptions, KerberosTime paramKerberosTime1, Integer paramInteger1, KerberosTime paramKerberosTime2, Integer paramInteger2, int paramInt, Realm paramRealm1, PrincipalName paramPrincipalName1, Realm paramRealm2, PrincipalName paramPrincipalName2, String paramString, byte[] paramArrayOfByte)
  {
    this.pvno = 5;
    this.msgType = 30;
    this.cTime = paramKerberosTime1;
    this.cuSec = paramInteger1;
    this.sTime = paramKerberosTime2;
    this.suSec = paramInteger2;
    this.errorCode = paramInt;
    this.crealm = paramRealm1;
    this.cname = paramPrincipalName1;
    this.realm = paramRealm2;
    this.sname = paramPrincipalName2;
    this.eText = paramString;
    this.eData = paramArrayOfByte;
  }

  public KRBError(APOptions paramAPOptions, KerberosTime paramKerberosTime1, Integer paramInteger1, KerberosTime paramKerberosTime2, Integer paramInteger2, int paramInt, Realm paramRealm1, PrincipalName paramPrincipalName1, Realm paramRealm2, PrincipalName paramPrincipalName2, String paramString, byte[] paramArrayOfByte, Checksum paramChecksum)
  {
    this.pvno = 5;
    this.msgType = 30;
    this.cTime = paramKerberosTime1;
    this.cuSec = paramInteger1;
    this.sTime = paramKerberosTime2;
    this.suSec = paramInteger2;
    this.errorCode = paramInt;
    this.crealm = paramRealm1;
    this.cname = paramPrincipalName1;
    this.realm = paramRealm2;
    this.sname = paramPrincipalName2;
    this.eText = paramString;
    this.eData = paramArrayOfByte;
    this.eCksum = paramChecksum;
  }

  public KRBError(byte[] paramArrayOfByte)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public KRBError(DerValue paramDerValue)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(paramDerValue);
    if (this.DEBUG)
    {
      System.out.println(">>>KRBError:");
      if (this.cTime != null)
        System.out.println("\t cTime is " + this.cTime.toDate().toString() + " " + this.cTime.toDate().getTime());
      if (this.cuSec != null)
        System.out.println("\t cuSec is " + this.cuSec.intValue());
      System.out.println("\t sTime is " + this.sTime.toDate().toString() + " " + this.sTime.toDate().getTime());
      System.out.println("\t suSec is " + this.suSec);
      System.out.println("\t error code is " + this.errorCode);
      System.out.println("\t error Message is " + Krb5.getErrorMessage(this.errorCode));
      if (this.crealm != null)
        System.out.println("\t crealm is " + this.crealm.toString());
      if (this.cname != null)
        System.out.println("\t cname is " + this.cname.toString());
      if (this.realm != null)
        System.out.println("\t realm is " + this.realm.toString());
      if (this.sname != null)
        System.out.println("\t sname is " + this.sname.toString());
      if (this.eData != null)
        System.out.println("\t eData provided.");
      if (this.eCksum != null)
        System.out.println("\t checksum provided.");
      System.out.println("\t msgType is " + this.msgType);
    }
    if ((this.eData != null) && (((this.errorCode == 25) || (this.errorCode == 24))))
    {
      DerValue localDerValue1 = new DerValue(this.eData);
      while (localDerValue1.data.available() > 0)
      {
        DerValue localDerValue2 = localDerValue1.data.getDerValue();
        PAData localPAData = new PAData(localDerValue2);
        parsePAData(localPAData.getType(), localPAData.getValue());
      }
    }
  }

  private void parsePAData(int paramInt, byte[] paramArrayOfByte)
    throws IOException, Asn1Exception
  {
    DerValue localDerValue1;
    DerValue localDerValue2;
    Object localObject;
    if (this.DEBUG)
    {
      System.out.println(">>>Pre-Authentication Data:");
      System.out.println("\t PA-DATA type = " + paramInt);
    }
    switch (paramInt)
    {
    case 2:
      if (!(this.DEBUG))
        return;
      System.out.println("\t PA-ENC-TIMESTAMP");
      break;
    case 11:
      if (paramArrayOfByte == null)
        return;
      localDerValue1 = new DerValue(paramArrayOfByte);
      localDerValue2 = localDerValue1.data.getDerValue();
      localObject = new ETypeInfo(localDerValue2);
      this.etype = ((ETypeInfo)localObject).getEType();
      this.salt = ((ETypeInfo)localObject).getSalt();
      if (!(this.DEBUG))
        break label180;
      System.out.println("\t PA-ETYPE-INFO etype = " + this.etype);
      break;
    case 19:
      if (paramArrayOfByte == null)
        label180: return;
      localDerValue1 = new DerValue(paramArrayOfByte);
      localDerValue2 = localDerValue1.data.getDerValue();
      localObject = new ETypeInfo2(localDerValue2);
      this.etype = ((ETypeInfo2)localObject).getEType();
      this.salt = ((ETypeInfo2)localObject).getSalt();
      this.s2kparams = ((ETypeInfo2)localObject).getParams();
      if (!(this.DEBUG))
        break label278;
      System.out.println("\t PA-ETYPE-INFO2 etype = " + this.etype);
    }
    label278:
  }

  public final KerberosTime getServerTime()
  {
    return this.sTime;
  }

  public final KerberosTime getClientTime()
  {
    return this.cTime;
  }

  public final Integer getServerMicroSeconds()
  {
    return this.suSec;
  }

  public final Integer getClientMicroSeconds()
  {
    return this.cuSec;
  }

  public final int getErrorCode()
  {
    return this.errorCode;
  }

  public final int getEType()
  {
    return this.etype;
  }

  public final byte[] getSalt()
  {
    return ((this.salt == null) ? null : (byte[])(byte[])this.salt.clone());
  }

  public final byte[] getParams()
  {
    return ((this.s2kparams == null) ? null : (byte[])(byte[])this.s2kparams.clone());
  }

  public final String getErrorString()
  {
    return this.eText;
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    if (((paramDerValue.getTag() & 0x1F) != 30) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
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
      if (this.msgType == 30)
        break label191;
      throw new sun.security.krb5.internal.KrbApErrException(40);
    }
    throw new Asn1Exception(906);
    label191: this.cTime = KerberosTime.parse(localDerValue1.getData(), 2, true);
    if ((localDerValue1.getData().peekByte() & 0x1F) == 3)
    {
      localDerValue2 = localDerValue1.getData().getDerValue();
      this.cuSec = new Integer(localDerValue2.getData().getBigInteger().intValue());
    }
    else
    {
      this.cuSec = null;
    }
    this.sTime = KerberosTime.parse(localDerValue1.getData(), 4, false);
    localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 5)
      this.suSec = new Integer(localDerValue2.getData().getBigInteger().intValue());
    else
      throw new Asn1Exception(906);
    localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 6)
      this.errorCode = localDerValue2.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    this.crealm = Realm.parse(localDerValue1.getData(), 7, true);
    this.cname = PrincipalName.parse(localDerValue1.getData(), 8, true);
    this.realm = Realm.parse(localDerValue1.getData(), 9, false);
    this.sname = PrincipalName.parse(localDerValue1.getData(), 10, false);
    this.eText = null;
    this.eData = null;
    this.eCksum = null;
    if ((localDerValue1.getData().available() > 0) && ((localDerValue1.getData().peekByte() & 0x1F) == 11))
    {
      localDerValue2 = localDerValue1.getData().getDerValue();
      this.eText = localDerValue2.getData().getGeneralString();
    }
    if ((localDerValue1.getData().available() > 0) && ((localDerValue1.getData().peekByte() & 0x1F) == 12))
    {
      localDerValue2 = localDerValue1.getData().getDerValue();
      this.eData = localDerValue2.getData().getOctetString();
    }
    if (localDerValue1.getData().available() > 0)
      this.eCksum = Checksum.parse(localDerValue1.getData(), 13, true);
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.pvno));
    localDerOutputStream2.write(DerValue.createTag(-128, true, 0), localDerOutputStream1);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.msgType));
    localDerOutputStream2.write(DerValue.createTag(-128, true, 1), localDerOutputStream1);
    if (this.cTime != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 2), this.cTime.asn1Encode());
    if (this.cuSec != null)
    {
      localDerOutputStream1 = new DerOutputStream();
      localDerOutputStream1.putInteger(BigInteger.valueOf(this.cuSec.intValue()));
      localDerOutputStream2.write(DerValue.createTag(-128, true, 3), localDerOutputStream1);
    }
    localDerOutputStream2.write(DerValue.createTag(-128, true, 4), this.sTime.asn1Encode());
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.suSec.intValue()));
    localDerOutputStream2.write(DerValue.createTag(-128, true, 5), localDerOutputStream1);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.errorCode));
    localDerOutputStream2.write(DerValue.createTag(-128, true, 6), localDerOutputStream1);
    if (this.crealm != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 7), this.crealm.asn1Encode());
    if (this.cname != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 8), this.cname.asn1Encode());
    localDerOutputStream2.write(DerValue.createTag(-128, true, 9), this.realm.asn1Encode());
    localDerOutputStream2.write(DerValue.createTag(-128, true, 10), this.sname.asn1Encode());
    if (this.eText != null)
    {
      localDerOutputStream1 = new DerOutputStream();
      localDerOutputStream1.putGeneralString(this.eText);
      localDerOutputStream2.write(DerValue.createTag(-128, true, 11), localDerOutputStream1);
    }
    if (this.eData != null)
    {
      localDerOutputStream1 = new DerOutputStream();
      localDerOutputStream1.putOctetString(this.eData);
      localDerOutputStream2.write(DerValue.createTag(-128, true, 12), localDerOutputStream1);
    }
    if (this.eCksum != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 13), this.eCksum.asn1Encode());
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.write(48, localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(DerValue.createTag(64, true, 30), localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }
}
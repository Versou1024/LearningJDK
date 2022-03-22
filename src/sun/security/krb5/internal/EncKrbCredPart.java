package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EncKrbCredPart
{
  public KrbCredInfo[] ticketInfo = null;
  public KerberosTime timeStamp;
  private Integer nonce;
  private Integer usec;
  private HostAddress sAddress;
  private HostAddresses rAddress;

  public EncKrbCredPart(KrbCredInfo[] paramArrayOfKrbCredInfo, KerberosTime paramKerberosTime, Integer paramInteger1, Integer paramInteger2, HostAddress paramHostAddress, HostAddresses paramHostAddresses)
    throws IOException
  {
    if (paramArrayOfKrbCredInfo != null)
    {
      this.ticketInfo = new KrbCredInfo[paramArrayOfKrbCredInfo.length];
      for (int i = 0; i < paramArrayOfKrbCredInfo.length; ++i)
      {
        if (paramArrayOfKrbCredInfo[i] == null)
          throw new IOException("Cannot create a EncKrbCredPart");
        this.ticketInfo[i] = ((KrbCredInfo)paramArrayOfKrbCredInfo[i].clone());
      }
    }
    this.timeStamp = paramKerberosTime;
    this.usec = paramInteger1;
    this.nonce = paramInteger2;
    this.sAddress = paramHostAddress;
    this.rAddress = paramHostAddresses;
  }

  public EncKrbCredPart(byte[] paramArrayOfByte)
    throws Asn1Exception, IOException, RealmException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public EncKrbCredPart(DerValue paramDerValue)
    throws Asn1Exception, IOException, RealmException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, IOException, RealmException
  {
    this.nonce = null;
    this.timeStamp = null;
    this.usec = null;
    this.sAddress = null;
    this.rAddress = null;
    if (((paramDerValue.getTag() & 0x1F) != 29) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 0)
    {
      DerValue[] arrayOfDerValue = localDerValue2.getData().getSequence(1);
      this.ticketInfo = new KrbCredInfo[arrayOfDerValue.length];
      for (int i = 0; i < arrayOfDerValue.length; ++i)
        this.ticketInfo[i] = new KrbCredInfo(arrayOfDerValue[i]);
    }
    else
    {
      throw new Asn1Exception(906);
    }
    if ((localDerValue1.getData().available() > 0) && (((byte)localDerValue1.getData().peekByte() & 0x1F) == 1))
    {
      localDerValue2 = localDerValue1.getData().getDerValue();
      this.nonce = new Integer(localDerValue2.getData().getBigInteger().intValue());
    }
    if (localDerValue1.getData().available() > 0)
      this.timeStamp = KerberosTime.parse(localDerValue1.getData(), 2, true);
    if ((localDerValue1.getData().available() > 0) && (((byte)localDerValue1.getData().peekByte() & 0x1F) == 3))
    {
      localDerValue2 = localDerValue1.getData().getDerValue();
      this.usec = new Integer(localDerValue2.getData().getBigInteger().intValue());
    }
    if (localDerValue1.getData().available() > 0)
      this.sAddress = HostAddress.parse(localDerValue1.getData(), 4, true);
    if (localDerValue1.getData().available() > 0)
      this.rAddress = HostAddresses.parse(localDerValue1.getData(), 5, true);
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    DerValue[] arrayOfDerValue = new DerValue[this.ticketInfo.length];
    for (int i = 0; i < this.ticketInfo.length; ++i)
      arrayOfDerValue[i] = new DerValue(this.ticketInfo[i].asn1Encode());
    localDerOutputStream2.putSequence(arrayOfDerValue);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    if (this.nonce != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putInteger(BigInteger.valueOf(this.nonce.intValue()));
      localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    }
    if (this.timeStamp != null)
      localDerOutputStream1.write(DerValue.createTag(-128, true, 2), this.timeStamp.asn1Encode());
    if (this.usec != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putInteger(BigInteger.valueOf(this.usec.intValue()));
      localDerOutputStream1.write(DerValue.createTag(-128, true, 3), localDerOutputStream2);
    }
    if (this.sAddress != null)
      localDerOutputStream1.write(DerValue.createTag(-128, true, 4), this.sAddress.asn1Encode());
    if (this.rAddress != null)
      localDerOutputStream1.write(DerValue.createTag(-128, true, 5), this.rAddress.asn1Encode());
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.write(DerValue.createTag(64, true, 29), localDerOutputStream2);
    return localDerOutputStream1.toByteArray();
  }
}
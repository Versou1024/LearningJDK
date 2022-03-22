package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.KrbException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KDCReq
{
  public KDCReqBody reqBody;
  private int pvno;
  private int msgType;
  private PAData[] pAData = null;

  public KDCReq(PAData[] paramArrayOfPAData, KDCReqBody paramKDCReqBody, int paramInt)
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
    this.reqBody = paramKDCReqBody;
  }

  public KDCReq()
  {
  }

  public KDCReq(byte[] paramArrayOfByte, int paramInt)
    throws Asn1Exception, IOException, KrbException
  {
    init(new DerValue(paramArrayOfByte), paramInt);
  }

  public KDCReq(DerValue paramDerValue, int paramInt)
    throws Asn1Exception, IOException, KrbException
  {
    init(paramDerValue, paramInt);
  }

  protected void init(DerValue paramDerValue, int paramInt)
    throws Asn1Exception, IOException, KrbException
  {
    BigInteger localBigInteger;
    DerValue localDerValue3;
    if ((paramDerValue.getTag() & 0x1F) != paramInt)
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 1)
    {
      localBigInteger = localDerValue2.getData().getBigInteger();
      this.pvno = localBigInteger.intValue();
      if (this.pvno == 5)
        break label119;
      throw new KrbApErrException(39);
    }
    throw new Asn1Exception(906);
    label119: localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 2)
    {
      localBigInteger = localDerValue2.getData().getBigInteger();
      this.msgType = localBigInteger.intValue();
      if (this.msgType == paramInt)
        break label188;
      throw new KrbApErrException(40);
    }
    throw new Asn1Exception(906);
    label188: localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 3)
    {
      localDerValue3 = localDerValue2.getData().getDerValue();
      if (localDerValue3.getTag() != 48)
        throw new Asn1Exception(906);
      Vector localVector = new Vector();
      while (localDerValue3.getData().available() > 0)
        localVector.addElement(new PAData(localDerValue3.getData().getDerValue()));
      if (localVector.size() > 0)
      {
        this.pAData = new PAData[localVector.size()];
        localVector.copyInto(this.pAData);
      }
    }
    else
    {
      this.pAData = null;
    }
    localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 4)
    {
      localDerValue3 = localDerValue2.getData().getDerValue();
      this.reqBody = new KDCReqBody(localDerValue3, this.msgType);
    }
    else
    {
      throw new Asn1Exception(906);
    }
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.pvno));
    DerOutputStream localDerOutputStream3 = new DerOutputStream();
    localDerOutputStream3.write(DerValue.createTag(-128, true, 1), localDerOutputStream1);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.msgType));
    localDerOutputStream3.write(DerValue.createTag(-128, true, 2), localDerOutputStream1);
    if ((this.pAData != null) && (this.pAData.length > 0))
    {
      localDerOutputStream1 = new DerOutputStream();
      for (int i = 0; i < this.pAData.length; ++i)
        localDerOutputStream1.write(this.pAData[i].asn1Encode());
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.write(48, localDerOutputStream1);
      localDerOutputStream3.write(DerValue.createTag(-128, true, 3), localDerOutputStream2);
    }
    localDerOutputStream3.write(DerValue.createTag(-128, true, 4), this.reqBody.asn1Encode(this.msgType));
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream3);
    localDerOutputStream3 = new DerOutputStream();
    localDerOutputStream3.write(DerValue.createTag(64, true, (byte)this.msgType), localDerOutputStream2);
    return localDerOutputStream3.toByteArray();
  }

  public byte[] asn1EncodeReqBody()
    throws Asn1Exception, IOException
  {
    return this.reqBody.asn1Encode(this.msgType);
  }
}
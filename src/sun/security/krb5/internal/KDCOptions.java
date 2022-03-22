package sun.security.krb5.internal;

import java.io.IOException;
import java.io.PrintStream;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.Config;
import sun.security.krb5.KrbException;
import sun.security.krb5.internal.util.KrbBitArray;
import sun.security.util.BitArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KDCOptions extends KrbBitArray
{
  public final int KDC_OPT_PROXIABLE = 268435456;
  public final int KDC_OPT_RENEWABLE_OK = 16;
  public final int KDC_OPT_FORWARDABLE = 1073741824;
  public static final int RESERVED = 0;
  public static final int FORWARDABLE = 1;
  public static final int FORWARDED = 2;
  public static final int PROXIABLE = 3;
  public static final int PROXY = 4;
  public static final int ALLOW_POSTDATE = 5;
  public static final int POSTDATED = 6;
  public static final int UNUSED7 = 7;
  public static final int RENEWABLE = 8;
  public static final int UNUSED9 = 9;
  public static final int UNUSED10 = 10;
  public static final int UNUSED11 = 11;
  public static final int RENEWABLE_OK = 27;
  public static final int ENC_TKT_IN_SKEY = 28;
  public static final int RENEW = 30;
  public static final int VALIDATE = 31;
  public static final int MAX = 31;
  private boolean DEBUG;

  public KDCOptions()
  {
    super(32);
    this.KDC_OPT_PROXIABLE = 268435456;
    this.KDC_OPT_RENEWABLE_OK = 16;
    this.KDC_OPT_FORWARDABLE = 1073741824;
    this.DEBUG = Krb5.DEBUG;
    setDefault();
  }

  public KDCOptions(int paramInt, byte[] paramArrayOfByte)
    throws Asn1Exception
  {
    super(paramInt, paramArrayOfByte);
    this.KDC_OPT_PROXIABLE = 268435456;
    this.KDC_OPT_RENEWABLE_OK = 16;
    this.KDC_OPT_FORWARDABLE = 1073741824;
    this.DEBUG = Krb5.DEBUG;
    if ((paramInt > paramArrayOfByte.length * 8) || (paramInt > 32))
      throw new Asn1Exception(502);
  }

  public KDCOptions(boolean[] paramArrayOfBoolean)
    throws Asn1Exception
  {
    super(paramArrayOfBoolean);
    this.KDC_OPT_PROXIABLE = 268435456;
    this.KDC_OPT_RENEWABLE_OK = 16;
    this.KDC_OPT_FORWARDABLE = 1073741824;
    this.DEBUG = Krb5.DEBUG;
    if (paramArrayOfBoolean.length > 32)
      throw new Asn1Exception(502);
  }

  public KDCOptions(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    this(paramDerValue.getUnalignedBitString(true).toBooleanArray());
  }

  public KDCOptions(byte[] paramArrayOfByte)
  {
    super(paramArrayOfByte.length * 8, paramArrayOfByte);
    this.KDC_OPT_PROXIABLE = 268435456;
    this.KDC_OPT_RENEWABLE_OK = 16;
    this.KDC_OPT_FORWARDABLE = 1073741824;
    this.DEBUG = Krb5.DEBUG;
  }

  public static KDCOptions parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new KDCOptions(localDerValue2);
  }

  public byte[] asn1Encode()
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putUnalignedBitString(new BitArray(toBooleanArray()));
    return localDerOutputStream.toByteArray();
  }

  public void set(int paramInt, boolean paramBoolean)
    throws ArrayIndexOutOfBoundsException
  {
    super.set(paramInt, paramBoolean);
  }

  public boolean get(int paramInt)
    throws ArrayIndexOutOfBoundsException
  {
    return super.get(paramInt);
  }

  private void setDefault()
  {
    Config localConfig;
    try
    {
      localConfig = Config.getInstance();
      int i = localConfig.getDefaultIntValue("kdc_default_options", "libdefaults");
      if ((i & 0x1B) == 27)
        set(27, true);
      else if (localConfig.getDefaultBooleanValue("renewable", "libdefaults"))
        set(27, true);
      if ((i & 0x3) == 3)
        set(3, true);
      else if (localConfig.getDefaultBooleanValue("proxiable", "libdefaults"))
        set(3, true);
      if ((i & 0x1) == 1)
        set(1, true);
      else if (localConfig.getDefaultBooleanValue("forwardable", "libdefaults"))
        set(1, true);
    }
    catch (KrbException localKrbException)
    {
      if (this.DEBUG)
      {
        System.out.println("Exception in getting default values for KDC Options from the configuration ");
        localKrbException.printStackTrace();
      }
    }
  }
}
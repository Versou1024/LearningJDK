package sun.security.krb5.internal;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.Config;
import sun.security.krb5.KrbException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KerberosTime
  implements Cloneable
{
  private long kerberosTime;
  private static long syncTime;
  private static boolean DEBUG = Krb5.DEBUG;
  public static final boolean NOW = 1;
  public static final boolean UNADJUSTED_NOW = 0;

  public KerberosTime()
  {
    this.kerberosTime = 3412046964836007936L;
  }

  public KerberosTime(long paramLong)
  {
    this.kerberosTime = paramLong;
  }

  public Object clone()
  {
    return new KerberosTime(this.kerberosTime);
  }

  public KerberosTime(String paramString)
    throws Asn1Exception
  {
    this.kerberosTime = toKerberosTime(paramString);
  }

  public KerberosTime(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    GregorianCalendar localGregorianCalendar = new GregorianCalendar();
    Date localDate = paramDerValue.getGeneralizedTime();
    this.kerberosTime = localDate.getTime();
  }

  private static long toKerberosTime(String paramString)
    throws Asn1Exception
  {
    if (paramString.length() != 15)
      throw new Asn1Exception(900);
    if (paramString.charAt(14) != 'Z')
      throw new Asn1Exception(900);
    int i = Integer.parseInt(paramString.substring(0, 4));
    Calendar localCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    localCalendar.clear();
    localCalendar.set(i, Integer.parseInt(paramString.substring(4, 6)) - 1, Integer.parseInt(paramString.substring(6, 8)), Integer.parseInt(paramString.substring(8, 10)), Integer.parseInt(paramString.substring(10, 12)), Integer.parseInt(paramString.substring(12, 14)));
    return localCalendar.getTime().getTime();
  }

  public static String zeroPad(String paramString, int paramInt)
  {
    StringBuffer localStringBuffer = new StringBuffer(paramString);
    while (localStringBuffer.length() < paramInt)
      localStringBuffer.insert(0, '0');
    return localStringBuffer.toString();
  }

  public KerberosTime(Date paramDate)
  {
    this.kerberosTime = paramDate.getTime();
  }

  public KerberosTime(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      Date localDate = new Date();
      setTime(localDate);
    }
    else
    {
      this.kerberosTime = 3412047617671036928L;
    }
  }

  public String toGeneralizedTimeString()
  {
    Calendar localCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    localCalendar.clear();
    localCalendar.setTimeInMillis(this.kerberosTime);
    return zeroPad(Integer.toString(localCalendar.get(1)), 4) + zeroPad(Integer.toString(localCalendar.get(2) + 1), 2) + zeroPad(Integer.toString(localCalendar.get(5)), 2) + zeroPad(Integer.toString(localCalendar.get(11)), 2) + zeroPad(Integer.toString(localCalendar.get(12)), 2) + zeroPad(Integer.toString(localCalendar.get(13)), 2) + 'Z';
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putGeneralizedTime(toDate());
    return localDerOutputStream.toByteArray();
  }

  public long getTime()
  {
    return this.kerberosTime;
  }

  public void setTime(Date paramDate)
  {
    this.kerberosTime = paramDate.getTime();
  }

  public void setTime(long paramLong)
  {
    this.kerberosTime = paramLong;
  }

  public Date toDate()
  {
    Date localDate = new Date(this.kerberosTime);
    localDate.setTime(localDate.getTime());
    return localDate;
  }

  public void setNow()
  {
    Date localDate = new Date();
    setTime(localDate);
  }

  public int getMicroSeconds()
  {
    Long localLong = new Long(this.kerberosTime % 1000L * 1000L);
    return localLong.intValue();
  }

  public void setMicroSeconds(int paramInt)
  {
    Integer localInteger = new Integer(paramInt);
    long l = localInteger.longValue() / 1000L;
    this.kerberosTime = (this.kerberosTime - this.kerberosTime % 1000L + l);
  }

  public void setMicroSeconds(Integer paramInteger)
  {
    if (paramInteger != null)
    {
      long l = paramInteger.longValue() / 1000L;
      this.kerberosTime = (this.kerberosTime - this.kerberosTime % 1000L + l);
    }
  }

  public boolean inClockSkew(int paramInt)
  {
    KerberosTime localKerberosTime = new KerberosTime(true);
    return (Math.abs(this.kerberosTime - localKerberosTime.kerberosTime) <= paramInt * 1000L);
  }

  public boolean inClockSkew()
  {
    return inClockSkew(getDefaultSkew());
  }

  public boolean inClockSkew(int paramInt, KerberosTime paramKerberosTime)
  {
    return (Math.abs(this.kerberosTime - paramKerberosTime.kerberosTime) <= paramInt * 1000L);
  }

  public boolean inClockSkew(KerberosTime paramKerberosTime)
  {
    return inClockSkew(getDefaultSkew(), paramKerberosTime);
  }

  public boolean greaterThanWRTClockSkew(KerberosTime paramKerberosTime, int paramInt)
  {
    return (this.kerberosTime - paramKerberosTime.kerberosTime > paramInt * 1000L);
  }

  public boolean greaterThanWRTClockSkew(KerberosTime paramKerberosTime)
  {
    return greaterThanWRTClockSkew(paramKerberosTime, getDefaultSkew());
  }

  public boolean greaterThan(KerberosTime paramKerberosTime)
  {
    return (this.kerberosTime > paramKerberosTime.kerberosTime);
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof KerberosTime))
      return false;
    return (this.kerberosTime == ((KerberosTime)paramObject).kerberosTime);
  }

  public int hashCode()
  {
    return (629 + (int)(this.kerberosTime ^ this.kerberosTime >>> 32));
  }

  public boolean isZero()
  {
    return (this.kerberosTime == 3412047102274961408L);
  }

  public int getSeconds()
  {
    Long localLong = new Long(this.kerberosTime / 1000L);
    return localLong.intValue();
  }

  public void setSeconds(int paramInt)
  {
    Integer localInteger = new Integer(paramInt);
    this.kerberosTime = (localInteger.longValue() * 1000L);
  }

  public static KerberosTime parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new KerberosTime(localDerValue2);
  }

  public static int getDefaultSkew()
  {
    int i = 300;
    try
    {
      Config localConfig = Config.getInstance();
      if ((i = localConfig.getDefaultIntValue("clockskew", "libdefaults")) == -2147483648)
        i = 300;
    }
    catch (KrbException localKrbException)
    {
      if (DEBUG)
        System.out.println("Exception in getting clockskew from Configuration using default value " + localKrbException.getMessage());
    }
    return i;
  }

  public String toString()
  {
    return toGeneralizedTimeString();
  }
}
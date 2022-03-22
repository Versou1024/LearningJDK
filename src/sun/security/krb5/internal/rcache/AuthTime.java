package sun.security.krb5.internal.rcache;

public class AuthTime
{
  long kerberosTime;
  int cusec;

  public AuthTime(long paramLong, int paramInt)
  {
    this.kerberosTime = paramLong;
    this.cusec = paramInt;
  }

  public boolean equals(Object paramObject)
  {
    return ((paramObject instanceof AuthTime) && (((AuthTime)paramObject).kerberosTime == this.kerberosTime) && (((AuthTime)paramObject).cusec == this.cusec));
  }

  public int hashCode()
  {
    int i = 17;
    i = 37 * i + (int)(this.kerberosTime ^ this.kerberosTime >>> 32);
    i = 37 * i + this.cusec;
    return i;
  }
}
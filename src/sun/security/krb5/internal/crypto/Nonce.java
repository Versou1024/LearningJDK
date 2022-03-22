package sun.security.krb5.internal.crypto;

public class Nonce
{
  private static int lastNonce;

  public static synchronized int value()
  {
    int i = (int)(System.currentTimeMillis() / 1000L);
    if (i <= lastNonce)
      lastNonce += 1;
    else
      lastNonce = i;
    return lastNonce;
  }
}
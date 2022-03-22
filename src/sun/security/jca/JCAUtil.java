package sun.security.jca;

import java.security.SecureRandom;

public final class JCAUtil
{
  private static final Object LOCK = JCAUtil.class;
  private static volatile SecureRandom secureRandom;
  private static final int ARRAY_SIZE = 4096;

  public static int getTempArraySize(int paramInt)
  {
    return Math.min(4096, paramInt);
  }

  public static SecureRandom getSecureRandom()
  {
    SecureRandom localSecureRandom = secureRandom;
    if (localSecureRandom == null)
      synchronized (LOCK)
      {
        localSecureRandom = secureRandom;
        if (localSecureRandom == null)
        {
          localSecureRandom = new SecureRandom();
          secureRandom = localSecureRandom;
        }
      }
    return localSecureRandom;
  }
}
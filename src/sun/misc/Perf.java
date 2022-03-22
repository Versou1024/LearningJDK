package sun.misc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.PrivilegedAction;

public final class Perf
{
  private static Perf instance;
  private static final int PERF_MODE_RO = 0;
  private static final int PERF_MODE_RW = 1;

  public static Perf getPerf()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      RuntimePermission localRuntimePermission = new RuntimePermission("sun.misc.Perf.getPerf");
      localSecurityManager.checkPermission(localRuntimePermission);
    }
    return instance;
  }

  public ByteBuffer attach(int paramInt, String paramString)
    throws IllegalArgumentException, IOException
  {
    if (paramString.compareTo("r") == 0)
      return attachImpl(null, paramInt, 0);
    if (paramString.compareTo("rw") == 0)
      return attachImpl(null, paramInt, 1);
    throw new IllegalArgumentException("unknown mode");
  }

  public ByteBuffer attach(String paramString1, int paramInt, String paramString2)
    throws IllegalArgumentException, IOException
  {
    if (paramString2.compareTo("r") == 0)
      return attachImpl(paramString1, paramInt, 0);
    if (paramString2.compareTo("rw") == 0)
      return attachImpl(paramString1, paramInt, 1);
    throw new IllegalArgumentException("unknown mode");
  }

  private ByteBuffer attachImpl(String paramString, int paramInt1, int paramInt2)
    throws IllegalArgumentException, IOException
  {
    ByteBuffer localByteBuffer1 = attach(paramString, paramInt1, paramInt2);
    if (paramInt1 == 0)
      return localByteBuffer1;
    ByteBuffer localByteBuffer2 = localByteBuffer1.duplicate();
    Cleaner.create(localByteBuffer2, new Runnable(this, localByteBuffer1)
    {
      public void run()
      {
        try
        {
          Perf.access$100(Perf.access$000(), this.val$b);
        }
        catch (Throwable localThrowable)
        {
          if (!($assertionsDisabled))
            throw new AssertionError(localThrowable.toString());
        }
      }
    });
    return localByteBuffer2;
  }

  private native ByteBuffer attach(String paramString, int paramInt1, int paramInt2)
    throws IllegalArgumentException, IOException;

  private native void detach(ByteBuffer paramByteBuffer);

  public native ByteBuffer createLong(String paramString, int paramInt1, int paramInt2, long paramLong);

  public ByteBuffer createString(String paramString1, int paramInt1, int paramInt2, String paramString2, int paramInt3)
  {
    byte[] arrayOfByte1 = getBytes(paramString2);
    byte[] arrayOfByte2 = new byte[arrayOfByte1.length + 1];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
    arrayOfByte2[arrayOfByte1.length] = 0;
    return createByteArray(paramString1, paramInt1, paramInt2, arrayOfByte2, Math.max(arrayOfByte2.length, paramInt3));
  }

  public ByteBuffer createString(String paramString1, int paramInt1, int paramInt2, String paramString2)
  {
    byte[] arrayOfByte1 = getBytes(paramString2);
    byte[] arrayOfByte2 = new byte[arrayOfByte1.length + 1];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
    arrayOfByte2[arrayOfByte1.length] = 0;
    return createByteArray(paramString1, paramInt1, paramInt2, arrayOfByte2, arrayOfByte2.length);
  }

  public native ByteBuffer createByteArray(String paramString, int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3);

  private static byte[] getBytes(String paramString)
  {
    byte[] arrayOfByte = null;
    try
    {
      arrayOfByte = paramString.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    return arrayOfByte;
  }

  public native long highResCounter();

  public native long highResFrequency();

  private static native void registerNatives();

  static
  {
    registerNatives();
    instance = new Perf();
  }

  public static class GetPerfAction
  implements PrivilegedAction
  {
    public Object run()
    {
      return Perf.getPerf();
    }
  }
}
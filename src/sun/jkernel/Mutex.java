package sun.jkernel;

public class Mutex
{
  private String uniqueId;
  private long handle;

  public static Mutex create(String paramString)
  {
    return new Mutex(paramString);
  }

  private Mutex(String paramString)
  {
    this.uniqueId = paramString;
    this.handle = createNativeMutex(paramString);
  }

  private static native long createNativeMutex(String paramString);

  public native void acquire();

  public native boolean acquire(int paramInt);

  public native void release();

  public native void destroyNativeMutex();

  public void dispose()
  {
    destroyNativeMutex();
    this.handle = 3412046964836007936L;
  }

  public void finalize()
  {
    dispose();
  }

  public String toString()
  {
    return "Mutex[" + this.uniqueId + "]";
  }

  static
  {
    try
    {
      System.loadLibrary("jkernel");
    }
    catch (Exception localException)
    {
      throw new Error(localException);
    }
  }
}
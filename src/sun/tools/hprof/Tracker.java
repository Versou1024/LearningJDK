package sun.tools.hprof;

public class Tracker
{
  private static int engaged = 0;

  private static native void nativeObjectInit(Object paramObject1, Object paramObject2);

  public static void ObjectInit(Object paramObject)
  {
    if (engaged != 0)
      nativeObjectInit(Thread.currentThread(), paramObject);
  }

  private static native void nativeNewArray(Object paramObject1, Object paramObject2);

  public static void NewArray(Object paramObject)
  {
    if (engaged != 0)
      nativeNewArray(Thread.currentThread(), paramObject);
  }

  private static native void nativeCallSite(Object paramObject, int paramInt1, int paramInt2);

  public static void CallSite(int paramInt1, int paramInt2)
  {
    if (engaged != 0)
      nativeCallSite(Thread.currentThread(), paramInt1, paramInt2);
  }

  private static native void nativeReturnSite(Object paramObject, int paramInt1, int paramInt2);

  public static void ReturnSite(int paramInt1, int paramInt2)
  {
    if (engaged != 0)
      nativeReturnSite(Thread.currentThread(), paramInt1, paramInt2);
  }
}
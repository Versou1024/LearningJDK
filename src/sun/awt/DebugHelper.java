package sun.awt;

public abstract class DebugHelper
{
  protected static final String DBG_FIELD_NAME = "dbg";
  protected static final String DBG_ON_FIELD_NAME = "on";
  public static final boolean on = 0;
  private static final DebugHelper dbgStub;

  static void init()
  {
  }

  public static final DebugHelper create(Class paramClass)
  {
    return dbgStub;
  }

  public abstract void setAssertOn(boolean paramBoolean);

  public abstract void setTraceOn(boolean paramBoolean);

  public abstract void setDebugOn(boolean paramBoolean);

  public abstract void println(Object paramObject);

  public abstract void print(Object paramObject);

  public abstract void printStackTrace();

  public abstract void assertion(boolean paramBoolean);

  public abstract void assertion(boolean paramBoolean, String paramString);

  static
  {
    NativeLibLoader.loadLibraries();
    dbgStub = new DebugHelperStub();
  }
}
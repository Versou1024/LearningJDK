package sun.awt;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

abstract class DebugHelperImpl extends DebugHelper
{
  protected static DebugSettings settings;
  protected static DebugHelperImpl globalDebugHelperImpl;
  private static boolean initialized = false;
  private static final String PROP_ON = "on";
  private static final String PROP_TRACE = "trace";
  private static final String PROP_ASSERT = "assert";
  private DebugHelperImpl parent = null;
  private boolean tracingOn = false;
  private boolean assertionsOn = false;

  static final void initGlobals()
  {
    if (!(initialized))
    {
      NativeLibLoader.loadLibraries();
      initialized = true;
      settings = DebugSettings.getInstance();
      globalDebugHelperImpl = GlobalDebugHelperImpl.getInstance();
    }
  }

  static final DebugHelper factoryCreate(Class paramClass)
  {
    initGlobals();
    return new ClassDebugHelperImpl(paramClass);
  }

  protected DebugHelperImpl(DebugHelperImpl paramDebugHelperImpl)
  {
    this.parent = paramDebugHelperImpl;
  }

  public final synchronized void setAssertOn(boolean paramBoolean)
  {
    this.assertionsOn = paramBoolean;
  }

  public final synchronized void setTraceOn(boolean paramBoolean)
  {
    this.tracingOn = paramBoolean;
  }

  public final synchronized void setDebugOn(boolean paramBoolean)
  {
    DebugHelper localDebugHelper;
    try
    {
      localDebugHelper = DebugHelper.class;
      Field localField = localDebugHelper.getDeclaredField("on");
      if (!(Modifier.isFinal(localField.getModifiers())))
        localField.setBoolean(this, paramBoolean);
    }
    catch (NoSuchFieldException localNoSuchFieldException)
    {
      localNoSuchFieldException.printStackTrace();
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      localIllegalAccessException.printStackTrace();
    }
  }

  public final synchronized void println(Object paramObject)
  {
    if (this.tracingOn)
      printlnImpl(paramObject.toString());
  }

  public final synchronized void print(Object paramObject)
  {
    if (this.tracingOn)
      printImpl(paramObject.toString());
  }

  public final synchronized String toString()
  {
    String str = "Debug {\ton=false, \ttrace=" + this.tracingOn + ", " + "\tassert=" + this.assertionsOn + "}";
    return str;
  }

  static synchronized native void printlnImpl(String paramString);

  static synchronized native void printImpl(String paramString);

  protected synchronized native void setCTracingOn(boolean paramBoolean);

  protected synchronized native void setCTracingOn(boolean paramBoolean, String paramString);

  protected synchronized native void setCTracingOn(boolean paramBoolean, String paramString, int paramInt);

  public final synchronized void printStackTrace()
  {
    if (this.tracingOn)
      Thread.dumpStack();
  }

  public final synchronized void assertion(boolean paramBoolean)
  {
    assertion(paramBoolean, "");
  }

  public final synchronized void assertion(boolean paramBoolean, String paramString)
  {
    if ((this.assertionsOn) && (!(paramBoolean)))
      throw new AssertionFailure(this, paramString);
  }

  protected void setParent(DebugHelperImpl paramDebugHelperImpl)
  {
    this.parent = paramDebugHelperImpl;
  }

  protected DebugHelperImpl getParent()
  {
    return this.parent;
  }

  protected void loadSettings()
  {
    boolean bool1 = getBoolean("on", this.parent == null);
    boolean bool2 = getBoolean("assert", (this.parent != null) ? this.parent.assertionsOn : true);
    boolean bool3 = getBoolean("trace", (this.parent != null) ? this.parent.tracingOn : false);
    setDebugOn(bool1);
    setAssertOn(bool2);
    setTraceOn(bool3);
  }

  protected synchronized boolean getBoolean(String paramString, boolean paramBoolean)
  {
    String str = getString(paramString, String.valueOf(paramBoolean));
    return str.equalsIgnoreCase("true");
  }

  protected synchronized int getInt(String paramString, int paramInt)
  {
    String str = getString(paramString, String.valueOf(paramInt));
    return Integer.parseInt(str);
  }

  protected synchronized String getString(String paramString1, String paramString2)
  {
    String str = settings.getString(paramString1, paramString2);
    return str;
  }

  class AssertionFailure extends Error
  {
    AssertionFailure(, String paramString)
    {
      super(paramString);
    }
  }
}
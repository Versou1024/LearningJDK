package sun.org.mozilla.javascript.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;
import sun.org.mozilla.javascript.internal.debug.DebuggableScript;
import sun.org.mozilla.javascript.internal.debug.Debugger;
import sun.org.mozilla.javascript.internal.xml.XMLLib;

public class Context
{
  public static final int VERSION_UNKNOWN = -1;
  public static final int VERSION_DEFAULT = 0;
  public static final int VERSION_1_0 = 100;
  public static final int VERSION_1_1 = 110;
  public static final int VERSION_1_2 = 120;
  public static final int VERSION_1_3 = 130;
  public static final int VERSION_1_4 = 140;
  public static final int VERSION_1_5 = 150;
  public static final int VERSION_1_6 = 160;
  public static final int FEATURE_NON_ECMA_GET_YEAR = 1;
  public static final int FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME = 2;
  public static final int FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER = 3;
  public static final int FEATURE_TO_STRING_AS_SOURCE = 4;
  public static final int FEATURE_PARENT_PROTO_PROPRTIES = 5;
  public static final int FEATURE_E4X = 6;
  public static final int FEATURE_DYNAMIC_SCOPE = 7;
  public static final int FEATURE_STRICT_VARS = 8;
  public static final int FEATURE_STRICT_EVAL = 9;
  public static final String languageVersionProperty = "language version";
  public static final String errorReporterProperty = "error reporter";
  public static final Object[] emptyArgs = ScriptRuntime.emptyArgs;
  private TimeZone thisContextTimeZone;
  private double LocalTZA;
  private static Class codegenClass = Kit.classOrNull("sun.org.mozilla.javascript.internal.optimizer.Codegen");
  private static String implementationVersion;
  private ContextFactory factory;
  private boolean sealed;
  private Object sealKey;
  Scriptable topCallScope;
  NativeCall currentActivationCall;
  XMLLib cachedXMLLib;
  ObjToIntMap iterating;
  Object interpreterSecurityDomain;
  int version;
  private SecurityController securityController;
  private ClassShutter classShutter;
  private ErrorReporter errorReporter;
  RegExpProxy regExpProxy;
  private Locale locale;
  private boolean generatingDebug;
  private boolean generatingDebugChanged;
  private boolean generatingSource = true;
  boolean compileFunctionsWithDynamicScopeFlag;
  boolean useDynamicScope;
  private int optimizationLevel;
  private WrapFactory wrapFactory;
  Debugger debugger;
  private Object debuggerData;
  private int enterCount;
  private Object propertyListeners;
  private Hashtable hashtable;
  private ClassLoader applicationClassLoader;
  private boolean creationEventWasSent;
  Hashtable activationNames;
  Object lastInterpreterFrame;
  ObjArray previousInterpreterInvocations;
  int instructionCount;
  int instructionThreshold;
  int scratchIndex;
  long scratchUint32;
  Scriptable scratchScriptable;

  public void setTimeZone()
  {
    this.thisContextTimeZone = TimeZone.getDefault();
  }

  public TimeZone getTimeZone()
  {
    return this.thisContextTimeZone;
  }

  public void setLocalTZA()
  {
    this.LocalTZA = this.thisContextTimeZone.getRawOffset();
  }

  public double getLocalTZA()
  {
    return this.LocalTZA;
  }

  public Context()
  {
    setLanguageVersion(0);
    this.optimizationLevel = ((codegenClass != null) ? 0 : -1);
  }

  public static Context getCurrentContext()
  {
    Object localObject = VMBridge.instance.getThreadContextHelper();
    return VMBridge.instance.getContext(localObject);
  }

  public static Context enter()
  {
    return enter(null);
  }

  public static Context enter(Context paramContext)
  {
    Object localObject = VMBridge.instance.getThreadContextHelper();
    Context localContext = VMBridge.instance.getContext(localObject);
    if (localContext != null)
    {
      if ((paramContext != null) && (paramContext != localContext) && (paramContext.enterCount != 0))
        throw new IllegalArgumentException("Cannot enter Context active on another thread");
      if (localContext.factory != null)
        return localContext;
      if (localContext.sealed)
        onSealedMutation();
      paramContext = localContext;
    }
    else
    {
      if (paramContext == null)
        paramContext = ContextFactory.getGlobal().makeContext();
      else if (paramContext.sealed)
        onSealedMutation();
      if ((paramContext.enterCount != 0) || (paramContext.factory != null))
        throw new IllegalStateException();
      if (!(paramContext.creationEventWasSent))
      {
        paramContext.creationEventWasSent = true;
        ContextFactory.getGlobal().onContextCreated(paramContext);
      }
    }
    if (localContext == null)
      VMBridge.instance.setContext(localObject, paramContext);
    paramContext.enterCount += 1;
    return paramContext;
  }

  public static void exit()
  {
    Object localObject = VMBridge.instance.getThreadContextHelper();
    Context localContext = VMBridge.instance.getContext(localObject);
    if (localContext == null)
      throw new IllegalStateException("Calling Context.exit without previous Context.enter");
    if (localContext.factory != null)
      return;
    if (localContext.enterCount < 1)
      Kit.codeBug();
    if (localContext.sealed)
      onSealedMutation();
    localContext.enterCount -= 1;
    if (localContext.enterCount == 0)
    {
      VMBridge.instance.setContext(localObject, null);
      ContextFactory.getGlobal().onContextReleased(localContext);
    }
  }

  public static Object call(ContextAction paramContextAction)
  {
    return call(ContextFactory.getGlobal(), paramContextAction);
  }

  public static Object call(ContextFactory paramContextFactory, Callable paramCallable, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    Object localObject2;
    if (paramContextFactory == null)
      paramContextFactory = ContextFactory.getGlobal();
    Object localObject1 = VMBridge.instance.getThreadContextHelper();
    Context localContext = VMBridge.instance.getContext(localObject1);
    if (localContext != null)
    {
      if (localContext.factory != null)
      {
        localObject2 = paramCallable.call(localContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
      }
      else
      {
        localContext.factory = paramContextFactory;
        try
        {
          localObject2 = paramCallable.call(localContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
        }
        finally
        {
          localContext.factory = null;
        }
      }
      return localObject2;
    }
    localContext = prepareNewContext(paramContextFactory, localObject1);
    try
    {
      localObject2 = paramCallable.call(localContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
      return localObject2;
    }
    finally
    {
      releaseContext(localObject1, localContext);
    }
  }

  static Object call(ContextFactory paramContextFactory, ContextAction paramContextAction)
  {
    Object localObject2;
    Object localObject1 = VMBridge.instance.getThreadContextHelper();
    Context localContext = VMBridge.instance.getContext(localObject1);
    if (localContext != null)
    {
      if (localContext.factory != null)
        return paramContextAction.run(localContext);
      localContext.factory = paramContextFactory;
      try
      {
        localObject2 = paramContextAction.run(localContext);
        return localObject2;
      }
      finally
      {
        localContext.factory = null;
      }
    }
    localContext = prepareNewContext(paramContextFactory, localObject1);
    try
    {
      localObject2 = paramContextAction.run(localContext);
      return localObject2;
    }
    finally
    {
      releaseContext(localObject1, localContext);
    }
  }

  private static Context prepareNewContext(ContextFactory paramContextFactory, Object paramObject)
  {
    Context localContext = paramContextFactory.makeContext();
    if ((localContext.factory != null) || (localContext.enterCount != 0))
      throw new IllegalStateException("factory.makeContext() returned Context instance already associated with some thread");
    localContext.factory = paramContextFactory;
    paramContextFactory.onContextCreated(localContext);
    if ((paramContextFactory.isSealed()) && (!(localContext.isSealed())))
      localContext.seal(null);
    VMBridge.instance.setContext(paramObject, localContext);
    return localContext;
  }

  private static void releaseContext(Object paramObject, Context paramContext)
  {
    VMBridge.instance.setContext(paramObject, null);
    try
    {
      paramContext.factory.onContextReleased(paramContext);
    }
    finally
    {
      paramContext.factory = null;
    }
  }

  /**
   * @deprecated
   */
  public static void addContextListener(ContextListener paramContextListener)
  {
    String str = "sun.org.mozilla.javascript.internal.tools.debugger.Main";
    if (str.equals(paramContextListener.getClass().getName()))
    {
      Class localClass1 = paramContextListener.getClass();
      Class localClass2 = Kit.classOrNull("sun.org.mozilla.javascript.internal.ContextFactory");
      Class[] arrayOfClass = { localClass2 };
      Object[] arrayOfObject = { ContextFactory.getGlobal() };
      try
      {
        Method localMethod = localClass1.getMethod("attachTo", arrayOfClass);
        localMethod.invoke(paramContextListener, arrayOfObject);
      }
      catch (Exception localException)
      {
        RuntimeException localRuntimeException = new RuntimeException();
        Kit.initCause(localRuntimeException, localException);
        throw localRuntimeException;
      }
      return;
    }
    ContextFactory.getGlobal().addListener(paramContextListener);
  }

  /**
   * @deprecated
   */
  public static void removeContextListener(ContextListener paramContextListener)
  {
    ContextFactory.getGlobal().addListener(paramContextListener);
  }

  public final ContextFactory getFactory()
  {
    ContextFactory localContextFactory = this.factory;
    if (localContextFactory == null)
      localContextFactory = ContextFactory.getGlobal();
    return localContextFactory;
  }

  public final boolean isSealed()
  {
    return this.sealed;
  }

  public final void seal(Object paramObject)
  {
    if (this.sealed)
      onSealedMutation();
    this.sealed = true;
    this.sealKey = paramObject;
  }

  public final void unseal(Object paramObject)
  {
    if (paramObject == null)
      throw new IllegalArgumentException();
    if (this.sealKey != paramObject)
      throw new IllegalArgumentException();
    if (!(this.sealed))
      throw new IllegalStateException();
    this.sealed = false;
    this.sealKey = null;
  }

  static void onSealedMutation()
  {
    throw new IllegalStateException();
  }

  public final int getLanguageVersion()
  {
    return this.version;
  }

  public void setLanguageVersion(int paramInt)
  {
    if (this.sealed)
      onSealedMutation();
    checkLanguageVersion(paramInt);
    Object localObject = this.propertyListeners;
    if ((localObject != null) && (paramInt != this.version))
      firePropertyChangeImpl(localObject, "language version", new Integer(this.version), new Integer(paramInt));
    this.version = paramInt;
  }

  public static boolean isValidLanguageVersion(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
    case 100:
    case 110:
    case 120:
    case 130:
    case 140:
    case 150:
    case 160:
      return true;
    }
    return false;
  }

  public static void checkLanguageVersion(int paramInt)
  {
    if (isValidLanguageVersion(paramInt))
      return;
    throw new IllegalArgumentException("Bad language version: " + paramInt);
  }

  public final String getImplementationVersion()
  {
    if (implementationVersion == null)
      implementationVersion = ScriptRuntime.getMessage0("implementation.version");
    return implementationVersion;
  }

  public final ErrorReporter getErrorReporter()
  {
    if (this.errorReporter == null)
      return DefaultErrorReporter.instance;
    return this.errorReporter;
  }

  public final ErrorReporter setErrorReporter(ErrorReporter paramErrorReporter)
  {
    if (this.sealed)
      onSealedMutation();
    if (paramErrorReporter == null)
      throw new IllegalArgumentException();
    ErrorReporter localErrorReporter = getErrorReporter();
    if (paramErrorReporter == localErrorReporter)
      return localErrorReporter;
    Object localObject = this.propertyListeners;
    if (localObject != null)
      firePropertyChangeImpl(localObject, "error reporter", localErrorReporter, paramErrorReporter);
    this.errorReporter = paramErrorReporter;
    return localErrorReporter;
  }

  public final Locale getLocale()
  {
    if (this.locale == null)
      this.locale = Locale.getDefault();
    return this.locale;
  }

  public final Locale setLocale(Locale paramLocale)
  {
    if (this.sealed)
      onSealedMutation();
    Locale localLocale = this.locale;
    this.locale = paramLocale;
    return localLocale;
  }

  public final void addPropertyChangeListener(PropertyChangeListener paramPropertyChangeListener)
  {
    if (this.sealed)
      onSealedMutation();
    this.propertyListeners = Kit.addListener(this.propertyListeners, paramPropertyChangeListener);
  }

  public final void removePropertyChangeListener(PropertyChangeListener paramPropertyChangeListener)
  {
    if (this.sealed)
      onSealedMutation();
    this.propertyListeners = Kit.removeListener(this.propertyListeners, paramPropertyChangeListener);
  }

  final void firePropertyChange(String paramString, Object paramObject1, Object paramObject2)
  {
    Object localObject = this.propertyListeners;
    if (localObject != null)
      firePropertyChangeImpl(localObject, paramString, paramObject1, paramObject2);
  }

  private void firePropertyChangeImpl(Object paramObject1, String paramString, Object paramObject2, Object paramObject3)
  {
    int i = 0;
    while (true)
    {
      Object localObject = Kit.getListener(paramObject1, i);
      if (localObject == null)
        return;
      if (localObject instanceof PropertyChangeListener)
      {
        PropertyChangeListener localPropertyChangeListener = (PropertyChangeListener)localObject;
        localPropertyChangeListener.propertyChange(new PropertyChangeEvent(this, paramString, paramObject2, paramObject3));
      }
      ++i;
    }
  }

  public static void reportWarning(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2)
  {
    Context localContext = getContext();
    localContext.getErrorReporter().warning(paramString1, paramString2, paramInt1, paramString3, paramInt2);
  }

  public static void reportWarning(String paramString)
  {
    int[] arrayOfInt = { 0 };
    String str = getSourcePositionFromStack(arrayOfInt);
    reportWarning(paramString, str, arrayOfInt[0], null, 0);
  }

  public static void reportError(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2)
  {
    Context localContext = getCurrentContext();
    if (localContext != null)
      localContext.getErrorReporter().error(paramString1, paramString2, paramInt1, paramString3, paramInt2);
    else
      throw new EvaluatorException(paramString1, paramString2, paramInt1, paramString3, paramInt2);
  }

  public static void reportError(String paramString)
  {
    int[] arrayOfInt = { 0 };
    String str = getSourcePositionFromStack(arrayOfInt);
    reportError(paramString, str, arrayOfInt[0], null, 0);
  }

  public static EvaluatorException reportRuntimeError(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2)
  {
    Context localContext = getCurrentContext();
    if (localContext != null)
      return localContext.getErrorReporter().runtimeError(paramString1, paramString2, paramInt1, paramString3, paramInt2);
    throw new EvaluatorException(paramString1, paramString2, paramInt1, paramString3, paramInt2);
  }

  static EvaluatorException reportRuntimeError0(String paramString)
  {
    String str = ScriptRuntime.getMessage0(paramString);
    return reportRuntimeError(str);
  }

  static EvaluatorException reportRuntimeError1(String paramString, Object paramObject)
  {
    String str = ScriptRuntime.getMessage1(paramString, paramObject);
    return reportRuntimeError(str);
  }

  static EvaluatorException reportRuntimeError2(String paramString, Object paramObject1, Object paramObject2)
  {
    String str = ScriptRuntime.getMessage2(paramString, paramObject1, paramObject2);
    return reportRuntimeError(str);
  }

  static EvaluatorException reportRuntimeError3(String paramString, Object paramObject1, Object paramObject2, Object paramObject3)
  {
    String str = ScriptRuntime.getMessage3(paramString, paramObject1, paramObject2, paramObject3);
    return reportRuntimeError(str);
  }

  static EvaluatorException reportRuntimeError4(String paramString, Object paramObject1, Object paramObject2, Object paramObject3, Object paramObject4)
  {
    String str = ScriptRuntime.getMessage4(paramString, paramObject1, paramObject2, paramObject3, paramObject4);
    return reportRuntimeError(str);
  }

  public static EvaluatorException reportRuntimeError(String paramString)
  {
    int[] arrayOfInt = { 0 };
    String str = getSourcePositionFromStack(arrayOfInt);
    return reportRuntimeError(paramString, str, arrayOfInt[0], null, 0);
  }

  public final ScriptableObject initStandardObjects()
  {
    return initStandardObjects(null, false);
  }

  public final Scriptable initStandardObjects(ScriptableObject paramScriptableObject)
  {
    return initStandardObjects(paramScriptableObject, false);
  }

  public ScriptableObject initStandardObjects(ScriptableObject paramScriptableObject, boolean paramBoolean)
  {
    return ScriptRuntime.initStandardObjects(this, paramScriptableObject, paramBoolean);
  }

  public static Object getUndefinedValue()
  {
    return Undefined.instance;
  }

  public final Object evaluateString(Scriptable paramScriptable, String paramString1, String paramString2, int paramInt, Object paramObject)
  {
    Script localScript = compileString(paramString1, paramString2, paramInt, paramObject);
    if (localScript != null)
      return localScript.exec(this, paramScriptable);
    return null;
  }

  public final Object evaluateReader(Scriptable paramScriptable, Reader paramReader, String paramString, int paramInt, Object paramObject)
    throws IOException
  {
    Script localScript = compileReader(paramScriptable, paramReader, paramString, paramInt, paramObject);
    if (localScript != null)
      return localScript.exec(this, paramScriptable);
    return null;
  }

  public final boolean stringIsCompilableUnit(String paramString)
  {
    int i = 0;
    CompilerEnvirons localCompilerEnvirons = new CompilerEnvirons();
    localCompilerEnvirons.initFromContext(this);
    localCompilerEnvirons.setGeneratingSource(false);
    Parser localParser = new Parser(localCompilerEnvirons, DefaultErrorReporter.instance);
    try
    {
      localParser.parse(paramString, null, 1);
    }
    catch (EvaluatorException localEvaluatorException)
    {
      i = 1;
    }
    return ((i == 0) || (!(localParser.eof())));
  }

  /**
   * @deprecated
   */
  public final Script compileReader(Scriptable paramScriptable, Reader paramReader, String paramString, int paramInt, Object paramObject)
    throws IOException
  {
    return compileReader(paramReader, paramString, paramInt, paramObject);
  }

  public final Script compileReader(Reader paramReader, String paramString, int paramInt, Object paramObject)
    throws IOException
  {
    if (paramInt < 0)
      paramInt = 0;
    return ((Script)compileImpl(null, paramReader, null, paramString, paramInt, paramObject, false, null, null));
  }

  public final Script compileString(String paramString1, String paramString2, int paramInt, Object paramObject)
  {
    if (paramInt < 0)
      paramInt = 0;
    return compileString(paramString1, null, null, paramString2, paramInt, paramObject);
  }

  final Script compileString(String paramString1, Interpreter paramInterpreter, ErrorReporter paramErrorReporter, String paramString2, int paramInt, Object paramObject)
  {
    try
    {
      return ((Script)compileImpl(null, null, paramString1, paramString2, paramInt, paramObject, false, paramInterpreter, paramErrorReporter));
    }
    catch (IOException localIOException)
    {
      throw new RuntimeException();
    }
  }

  public final Function compileFunction(Scriptable paramScriptable, String paramString1, String paramString2, int paramInt, Object paramObject)
  {
    return compileFunction(paramScriptable, paramString1, null, null, paramString2, paramInt, paramObject);
  }

  final Function compileFunction(Scriptable paramScriptable, String paramString1, Interpreter paramInterpreter, ErrorReporter paramErrorReporter, String paramString2, int paramInt, Object paramObject)
  {
    try
    {
      return ((Function)compileImpl(paramScriptable, null, paramString1, paramString2, paramInt, paramObject, true, paramInterpreter, paramErrorReporter));
    }
    catch (IOException localIOException)
    {
      throw new RuntimeException();
    }
  }

  public final String decompileScript(Script paramScript, int paramInt)
  {
    NativeFunction localNativeFunction = (NativeFunction)paramScript;
    return localNativeFunction.decompile(paramInt, 0);
  }

  public final String decompileFunction(Function paramFunction, int paramInt)
  {
    if (paramFunction instanceof BaseFunction)
      return ((BaseFunction)paramFunction).decompile(paramInt, 0);
    return "function " + paramFunction.getClassName() + "() {\n\t[native code]\n}\n";
  }

  public final String decompileFunctionBody(Function paramFunction, int paramInt)
  {
    if (paramFunction instanceof BaseFunction)
    {
      BaseFunction localBaseFunction = (BaseFunction)paramFunction;
      return localBaseFunction.decompile(paramInt, 1);
    }
    return "[native code]\n";
  }

  public final Scriptable newObject(Scriptable paramScriptable)
  {
    return newObject(paramScriptable, "Object", ScriptRuntime.emptyArgs);
  }

  public final Scriptable newObject(Scriptable paramScriptable, String paramString)
  {
    return newObject(paramScriptable, paramString, ScriptRuntime.emptyArgs);
  }

  public final Scriptable newObject(Scriptable paramScriptable, String paramString, Object[] paramArrayOfObject)
  {
    paramScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
    Function localFunction = ScriptRuntime.getExistingCtor(this, paramScriptable, paramString);
    if (paramArrayOfObject == null)
      paramArrayOfObject = ScriptRuntime.emptyArgs;
    return localFunction.construct(this, paramScriptable, paramArrayOfObject);
  }

  public final Scriptable newArray(Scriptable paramScriptable, int paramInt)
  {
    NativeArray localNativeArray = new NativeArray(paramInt);
    ScriptRuntime.setObjectProtoAndParent(localNativeArray, paramScriptable);
    return localNativeArray;
  }

  public final Scriptable newArray(Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    if (paramArrayOfObject.getClass().getComponentType() != ScriptRuntime.ObjectClass)
      throw new IllegalArgumentException();
    NativeArray localNativeArray = new NativeArray(paramArrayOfObject);
    ScriptRuntime.setObjectProtoAndParent(localNativeArray, paramScriptable);
    return localNativeArray;
  }

  public final Object[] getElements(Scriptable paramScriptable)
  {
    return ScriptRuntime.getArrayElements(paramScriptable);
  }

  public static boolean toBoolean(Object paramObject)
  {
    return ScriptRuntime.toBoolean(paramObject);
  }

  public static double toNumber(Object paramObject)
  {
    return ScriptRuntime.toNumber(paramObject);
  }

  public static String toString(Object paramObject)
  {
    return ScriptRuntime.toString(paramObject);
  }

  public static Scriptable toObject(Object paramObject, Scriptable paramScriptable)
  {
    return ScriptRuntime.toObject(paramScriptable, paramObject);
  }

  /**
   * @deprecated
   */
  public static Scriptable toObject(Object paramObject, Scriptable paramScriptable, Class paramClass)
  {
    return ScriptRuntime.toObject(paramScriptable, paramObject);
  }

  public static Object javaToJS(Object paramObject, Scriptable paramScriptable)
  {
    if ((paramObject instanceof String) || (paramObject instanceof Number) || (paramObject instanceof Boolean) || (paramObject instanceof Scriptable))
      return paramObject;
    if (paramObject instanceof Character)
      return String.valueOf(((Character)paramObject).charValue());
    Context localContext = getContext();
    return localContext.getWrapFactory().wrap(localContext, paramScriptable, paramObject, null);
  }

  public static Object jsToJava(Object paramObject, Class paramClass)
    throws sun.org.mozilla.javascript.internal.EvaluatorException
  {
    return NativeJavaObject.coerceTypeImpl(paramClass, paramObject);
  }

  /**
   * @deprecated
   */
  public static Object toType(Object paramObject, Class paramClass)
    throws IllegalArgumentException
  {
    try
    {
      return jsToJava(paramObject, paramClass);
    }
    catch (EvaluatorException localEvaluatorException)
    {
      IllegalArgumentException localIllegalArgumentException = new IllegalArgumentException(localEvaluatorException.getMessage());
      Kit.initCause(localIllegalArgumentException, localEvaluatorException);
      throw localIllegalArgumentException;
    }
  }

  public static RuntimeException throwAsScriptRuntimeEx(Throwable paramThrowable)
  {
    while (paramThrowable instanceof InvocationTargetException)
      paramThrowable = ((InvocationTargetException)paramThrowable).getTargetException();
    if (paramThrowable instanceof Error)
      throw ((Error)paramThrowable);
    if (paramThrowable instanceof RhinoException)
      throw ((RhinoException)paramThrowable);
    throw new WrappedException(paramThrowable);
  }

  public final boolean isGeneratingDebug()
  {
    return this.generatingDebug;
  }

  public final void setGeneratingDebug(boolean paramBoolean)
  {
    if (this.sealed)
      onSealedMutation();
    this.generatingDebugChanged = true;
    if ((paramBoolean) && (getOptimizationLevel() > 0))
      setOptimizationLevel(0);
    this.generatingDebug = paramBoolean;
  }

  public final boolean isGeneratingSource()
  {
    return this.generatingSource;
  }

  public final void setGeneratingSource(boolean paramBoolean)
  {
    if (this.sealed)
      onSealedMutation();
    this.generatingSource = paramBoolean;
  }

  public final int getOptimizationLevel()
  {
    return this.optimizationLevel;
  }

  public final void setOptimizationLevel(int paramInt)
  {
    if (this.sealed)
      onSealedMutation();
    if (paramInt == -2)
      paramInt = -1;
    checkOptimizationLevel(paramInt);
    if (codegenClass == null)
      paramInt = -1;
    this.optimizationLevel = paramInt;
  }

  public static boolean isValidOptimizationLevel(int paramInt)
  {
    return ((-1 <= paramInt) && (paramInt <= 9));
  }

  public static void checkOptimizationLevel(int paramInt)
  {
    if (isValidOptimizationLevel(paramInt))
      return;
    throw new IllegalArgumentException("Optimization level outside [-1..9]: " + paramInt);
  }

  public final void setSecurityController(SecurityController paramSecurityController)
  {
    if (this.sealed)
      onSealedMutation();
    if (paramSecurityController == null)
      throw new IllegalArgumentException();
    if (this.securityController != null)
      throw new SecurityException("Can not overwrite existing SecurityController object");
    if (SecurityController.hasGlobal())
      throw new SecurityException("Can not overwrite existing global SecurityController object");
    this.securityController = paramSecurityController;
  }

  public final void setClassShutter(ClassShutter paramClassShutter)
  {
    if (this.sealed)
      onSealedMutation();
    if (paramClassShutter == null)
      throw new IllegalArgumentException();
    if (this.classShutter != null)
      throw new SecurityException("Cannot overwrite existing ClassShutter object");
    this.classShutter = paramClassShutter;
  }

  final ClassShutter getClassShutter()
  {
    return this.classShutter;
  }

  public final Object getThreadLocal(Object paramObject)
  {
    if (this.hashtable == null)
      return null;
    return this.hashtable.get(paramObject);
  }

  public final void putThreadLocal(Object paramObject1, Object paramObject2)
  {
    if (this.sealed)
      onSealedMutation();
    if (this.hashtable == null)
      this.hashtable = new Hashtable();
    this.hashtable.put(paramObject1, paramObject2);
  }

  public final void removeThreadLocal(Object paramObject)
  {
    if (this.sealed)
      onSealedMutation();
    if (this.hashtable == null)
      return;
    this.hashtable.remove(paramObject);
  }

  /**
   * @deprecated
   */
  public final boolean hasCompileFunctionsWithDynamicScope()
  {
    return this.compileFunctionsWithDynamicScopeFlag;
  }

  /**
   * @deprecated
   */
  public final void setCompileFunctionsWithDynamicScope(boolean paramBoolean)
  {
    if (this.sealed)
      onSealedMutation();
    this.compileFunctionsWithDynamicScopeFlag = paramBoolean;
  }

  /**
   * @deprecated
   */
  public static void setCachingEnabled(boolean paramBoolean)
  {
  }

  public final void setWrapFactory(WrapFactory paramWrapFactory)
  {
    if (this.sealed)
      onSealedMutation();
    if (paramWrapFactory == null)
      throw new IllegalArgumentException();
    this.wrapFactory = paramWrapFactory;
  }

  public final WrapFactory getWrapFactory()
  {
    if (this.wrapFactory == null)
      this.wrapFactory = new WrapFactory();
    return this.wrapFactory;
  }

  public final Debugger getDebugger()
  {
    return this.debugger;
  }

  public final Object getDebuggerContextData()
  {
    return this.debuggerData;
  }

  public final void setDebugger(Debugger paramDebugger, Object paramObject)
  {
    if (this.sealed)
      onSealedMutation();
    this.debugger = paramDebugger;
    this.debuggerData = paramObject;
  }

  public static DebuggableScript getDebuggableView(Script paramScript)
  {
    if (paramScript instanceof NativeFunction)
      return ((NativeFunction)paramScript).getDebuggableView();
    return null;
  }

  public boolean hasFeature(int paramInt)
  {
    ContextFactory localContextFactory = getFactory();
    return localContextFactory.hasFeature(this, paramInt);
  }

  public final int getInstructionObserverThreshold()
  {
    return this.instructionThreshold;
  }

  public final void setInstructionObserverThreshold(int paramInt)
  {
    if (this.sealed)
      onSealedMutation();
    if (paramInt < 0)
      throw new IllegalArgumentException();
    this.instructionThreshold = paramInt;
  }

  protected void observeInstructionCount(int paramInt)
  {
    ContextFactory localContextFactory = getFactory();
    localContextFactory.observeInstructionCount(this, paramInt);
  }

  public GeneratedClassLoader createClassLoader(ClassLoader paramClassLoader)
  {
    ContextFactory localContextFactory = getFactory();
    return localContextFactory.createClassLoader(paramClassLoader);
  }

  public final ClassLoader getApplicationClassLoader()
  {
    if (this.applicationClassLoader == null)
    {
      ContextFactory localContextFactory = getFactory();
      ClassLoader localClassLoader1 = localContextFactory.getApplicationClassLoader();
      if (localClassLoader1 == null)
      {
        ClassLoader localClassLoader2 = VMBridge.instance.getCurrentThreadClassLoader();
        if (localClassLoader2 != null)
          return localClassLoader2;
        Class localClass = localContextFactory.getClass();
        if (localClass != ScriptRuntime.ContextFactoryClass)
          localClassLoader1 = localClass.getClassLoader();
        else
          localClassLoader1 = super.getClass().getClassLoader();
      }
      this.applicationClassLoader = localClassLoader1;
    }
    return this.applicationClassLoader;
  }

  public final void setApplicationClassLoader(ClassLoader paramClassLoader)
  {
    if (this.sealed)
      onSealedMutation();
    if (paramClassLoader == null)
    {
      this.applicationClassLoader = null;
      return;
    }
    if (!(Kit.testIfCanLoadRhinoClasses(paramClassLoader)))
      throw new IllegalArgumentException("Loader can not resolve Rhino classes");
    this.applicationClassLoader = paramClassLoader;
  }

  static Context getContext()
  {
    Context localContext = getCurrentContext();
    if (localContext == null)
      throw new RuntimeException("No Context associated with current Thread");
    return localContext;
  }

  private Object compileImpl(Scriptable paramScriptable, Reader paramReader, String paramString1, String paramString2, int paramInt, Object paramObject, boolean paramBoolean, Interpreter paramInterpreter, ErrorReporter paramErrorReporter)
    throws IOException
  {
    ScriptOrFnNode localScriptOrFnNode;
    Object localObject2;
    if ((paramObject != null) && (this.securityController == null))
      throw new IllegalArgumentException("securityDomain should be null if setSecurityController() was never called");
    if ((((paramReader == null) ? 1 : 0) ^ ((paramString1 == null) ? 1 : 0)) == 0)
      Kit.codeBug();
    if ((((paramScriptable == null) ? 1 : false) ^ paramBoolean) == 0)
      Kit.codeBug();
    CompilerEnvirons localCompilerEnvirons = new CompilerEnvirons();
    localCompilerEnvirons.initFromContext(this);
    if (paramErrorReporter == null)
      paramErrorReporter = localCompilerEnvirons.getErrorReporter();
    if ((this.debugger != null) && (paramReader != null))
    {
      paramString1 = Kit.readReader(paramReader);
      paramReader = null;
    }
    Parser localParser = new Parser(localCompilerEnvirons, paramErrorReporter);
    if (paramBoolean)
      localParser.calledByCompileFunction = true;
    if (paramString1 != null)
      localScriptOrFnNode = localParser.parse(paramString1, paramString2, paramInt);
    else
      localScriptOrFnNode = localParser.parse(paramReader, paramString2, paramInt);
    if ((paramBoolean) && (((localScriptOrFnNode.getFunctionCount() != 1) || (localScriptOrFnNode.getFirstChild() == null) || (localScriptOrFnNode.getFirstChild().getType() != 105))))
      throw new IllegalArgumentException("compileFunction only accepts source with single JS function: " + paramString1);
    if (paramInterpreter == null)
      paramInterpreter = createCompiler();
    String str = localParser.getEncodedSource();
    Object localObject1 = paramInterpreter.compile(localCompilerEnvirons, localScriptOrFnNode, str, paramBoolean);
    if (this.debugger != null)
    {
      if (paramString1 == null)
        Kit.codeBug();
      if (localObject1 instanceof DebuggableScript)
      {
        localObject2 = (DebuggableScript)localObject1;
        notifyDebugger_r(this, (DebuggableScript)localObject2, paramString1);
      }
      else
      {
        throw new RuntimeException("NOT SUPPORTED");
      }
    }
    if (paramBoolean)
      localObject2 = paramInterpreter.createFunctionObject(this, paramScriptable, localObject1, paramObject);
    else
      localObject2 = paramInterpreter.createScriptObject(localObject1, paramObject);
    return localObject2;
  }

  private static void notifyDebugger_r(Context paramContext, DebuggableScript paramDebuggableScript, String paramString)
  {
    paramContext.debugger.handleCompilationDone(paramContext, paramDebuggableScript, paramString);
    for (int i = 0; i != paramDebuggableScript.getFunctionCount(); ++i)
      notifyDebugger_r(paramContext, paramDebuggableScript.getFunction(i), paramString);
  }

  private Interpreter createCompiler()
  {
    Interpreter localInterpreter = null;
    if ((this.optimizationLevel >= 0) && (codegenClass != null))
      localInterpreter = (Interpreter)Kit.newInstanceOrNull(codegenClass);
    if (localInterpreter == null)
      localInterpreter = new Interpreter();
    return localInterpreter;
  }

  static String getSourcePositionFromStack(int[] paramArrayOfInt)
  {
    Context localContext = getCurrentContext();
    if (localContext == null)
      return null;
    if (localContext.lastInterpreterFrame != null)
      return Interpreter.getSourcePositionFromStack(localContext, paramArrayOfInt);
    CharArrayWriter localCharArrayWriter = new CharArrayWriter();
    RuntimeException localRuntimeException = new RuntimeException();
    localRuntimeException.printStackTrace(new PrintWriter(localCharArrayWriter));
    String str1 = localCharArrayWriter.toString();
    int i = -1;
    int j = -1;
    int k = -1;
    int l = 0;
    if (l < str1.length())
    {
      String str2;
      String str3;
      int i1 = str1.charAt(l);
      if (i1 == 58)
      {
        k = l;
      }
      else if (i1 == 40)
      {
        i = l;
      }
      else if (i1 == 41)
      {
        j = l;
      }
      else if ((i1 == 10) && (i != -1) && (j != -1) && (k != -1) && (i < k) && (k < j))
      {
        str2 = str1.substring(i + 1, k);
        if (!(str2.endsWith(".java")))
          str3 = str1.substring(k + 1, j);
      }
      try
      {
        paramArrayOfInt[0] = Integer.parseInt(str3);
        if (paramArrayOfInt[0] < 0)
          paramArrayOfInt[0] = 0;
        return str2;
      }
      catch (NumberFormatException localNumberFormatException)
      {
        i = j = k = -1;
        ++l;
      }
    }
    return null;
  }

  RegExpProxy getRegExpProxy()
  {
    if (this.regExpProxy == null)
    {
      Class localClass = Kit.classOrNull("sun.org.mozilla.javascript.internal.regexp.RegExpImpl");
      if (localClass != null)
        this.regExpProxy = ((RegExpProxy)Kit.newInstanceOrNull(localClass));
    }
    return this.regExpProxy;
  }

  final boolean isVersionECMA1()
  {
    return ((this.version == 0) || (this.version >= 130));
  }

  SecurityController getSecurityController()
  {
    SecurityController localSecurityController = SecurityController.global();
    if (localSecurityController != null)
      return localSecurityController;
    return this.securityController;
  }

  public final boolean isGeneratingDebugChanged()
  {
    return this.generatingDebugChanged;
  }

  public void addActivationName(String paramString)
  {
    if (this.sealed)
      onSealedMutation();
    if (this.activationNames == null)
      this.activationNames = new Hashtable(5);
    this.activationNames.put(paramString, paramString);
  }

  public final boolean isActivationNeeded(String paramString)
  {
    return ((this.activationNames != null) && (this.activationNames.containsKey(paramString)));
  }

  public void removeActivationName(String paramString)
  {
    if (this.sealed)
      onSealedMutation();
    if (this.activationNames != null)
      this.activationNames.remove(paramString);
  }
}
package sun.org.mozilla.javascript.internal;

public class NativeJavaTopPackage extends NativeJavaPackage
  implements Function, IdFunctionCall
{
  static final long serialVersionUID = -1455787259477709999L;
  private static final String commonPackages = "java.lang;java.lang.reflect;java.io;java.math;java.net;java.util;java.util.zip;java.text;java.text.resources;java.applet;javax.swing;";
  private static final Object FTAG = new Object();
  private static final int Id_getClass = 1;

  NativeJavaTopPackage(ClassLoader paramClassLoader)
  {
    super(true, "", paramClassLoader);
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    return construct(paramContext, paramScriptable1, paramArrayOfObject);
  }

  public Scriptable construct(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    ClassLoader localClassLoader = null;
    if (paramArrayOfObject.length != 0)
    {
      Object localObject = paramArrayOfObject[0];
      if (localObject instanceof Wrapper)
        localObject = ((Wrapper)localObject).unwrap();
      if (localObject instanceof ClassLoader)
        localClassLoader = (ClassLoader)localObject;
    }
    if (localClassLoader == null)
    {
      Context.reportRuntimeError0("msg.not.classloader");
      return null;
    }
    return new NativeJavaPackage(true, "", localClassLoader);
  }

  public static void init(Context paramContext, Scriptable paramScriptable, boolean paramBoolean)
  {
    ClassLoader localClassLoader = paramContext.getApplicationClassLoader();
    NativeJavaTopPackage localNativeJavaTopPackage = new NativeJavaTopPackage(localClassLoader);
    localNativeJavaTopPackage.setPrototype(getObjectPrototype(paramScriptable));
    localNativeJavaTopPackage.setParentScope(paramScriptable);
    String[] arrayOfString = Kit.semicolonSplit("java.lang;java.lang.reflect;java.io;java.math;java.net;java.util;java.util.zip;java.text;java.text.resources;java.applet;javax.swing;");
    for (int i = 0; i != arrayOfString.length; ++i)
      localNativeJavaTopPackage.forcePackage(arrayOfString[i], paramScriptable);
    IdFunctionObject localIdFunctionObject = new IdFunctionObject(localNativeJavaTopPackage, FTAG, 1, "getClass", 1, paramScriptable);
    NativeJavaPackage localNativeJavaPackage = (NativeJavaPackage)localNativeJavaTopPackage.get("java", localNativeJavaTopPackage);
    ScriptableObject localScriptableObject = (ScriptableObject)paramScriptable;
    if (paramBoolean)
      localIdFunctionObject.sealObject();
    localIdFunctionObject.exportAsScopeProperty();
    localScriptableObject.defineProperty("Packages", localNativeJavaTopPackage, 2);
    localScriptableObject.defineProperty("java", localNativeJavaPackage, 2);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if ((paramIdFunctionObject.hasTag(FTAG)) && (paramIdFunctionObject.methodId() == 1))
      return js_getClass(paramContext, paramScriptable1, paramArrayOfObject);
    throw paramIdFunctionObject.unknown();
  }

  private Scriptable js_getClass(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    if ((paramArrayOfObject.length > 0) && (paramArrayOfObject[0] instanceof Wrapper))
    {
      Object localObject1 = this;
      Class localClass = ((Wrapper)paramArrayOfObject[0]).unwrap().getClass();
      String str1 = localClass.getName();
      int i = 0;
      while (true)
      {
        int j = str1.indexOf(46, i);
        String str2 = (j == -1) ? str1.substring(i) : str1.substring(i, j);
        Object localObject2 = ((Scriptable)localObject1).get(str2, (Scriptable)localObject1);
        if (!(localObject2 instanceof Scriptable))
          break;
        localObject1 = (Scriptable)localObject2;
        if (j == -1)
          return localObject1;
        i = j + 1;
      }
    }
    throw Context.reportRuntimeError0("msg.not.java.obj");
  }
}
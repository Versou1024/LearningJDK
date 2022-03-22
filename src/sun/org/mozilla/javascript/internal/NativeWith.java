package sun.org.mozilla.javascript.internal;

public class NativeWith
  implements Scriptable, IdFunctionCall
{
  private static final Object FTAG = new Object();
  private static final int Id_constructor = 1;
  protected Scriptable prototype;
  protected Scriptable parent;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeWith localNativeWith = new NativeWith();
    localNativeWith.setParentScope(paramScriptable);
    localNativeWith.setPrototype(ScriptableObject.getObjectPrototype(paramScriptable));
    IdFunctionObject localIdFunctionObject = new IdFunctionObject(localNativeWith, FTAG, 1, "With", 0, paramScriptable);
    localIdFunctionObject.markAsConstructor(localNativeWith);
    if (paramBoolean)
      localIdFunctionObject.sealObject();
    localIdFunctionObject.exportAsScopeProperty();
  }

  private NativeWith()
  {
  }

  protected NativeWith(Scriptable paramScriptable1, Scriptable paramScriptable2)
  {
    this.parent = paramScriptable1;
    this.prototype = paramScriptable2;
  }

  public String getClassName()
  {
    return "With";
  }

  public boolean has(String paramString, Scriptable paramScriptable)
  {
    return this.prototype.has(paramString, this.prototype);
  }

  public boolean has(int paramInt, Scriptable paramScriptable)
  {
    return this.prototype.has(paramInt, this.prototype);
  }

  public Object get(String paramString, Scriptable paramScriptable)
  {
    if (paramScriptable == this)
      paramScriptable = this.prototype;
    return this.prototype.get(paramString, paramScriptable);
  }

  public Object get(int paramInt, Scriptable paramScriptable)
  {
    if (paramScriptable == this)
      paramScriptable = this.prototype;
    return this.prototype.get(paramInt, paramScriptable);
  }

  public void put(String paramString, Scriptable paramScriptable, Object paramObject)
  {
    if (paramScriptable == this)
      paramScriptable = this.prototype;
    this.prototype.put(paramString, paramScriptable, paramObject);
  }

  public void put(int paramInt, Scriptable paramScriptable, Object paramObject)
  {
    if (paramScriptable == this)
      paramScriptable = this.prototype;
    this.prototype.put(paramInt, paramScriptable, paramObject);
  }

  public void delete(String paramString)
  {
    this.prototype.delete(paramString);
  }

  public void delete(int paramInt)
  {
    this.prototype.delete(paramInt);
  }

  public Scriptable getPrototype()
  {
    return this.prototype;
  }

  public void setPrototype(Scriptable paramScriptable)
  {
    this.prototype = paramScriptable;
  }

  public Scriptable getParentScope()
  {
    return this.parent;
  }

  public void setParentScope(Scriptable paramScriptable)
  {
    this.parent = paramScriptable;
  }

  public Object[] getIds()
  {
    return this.prototype.getIds();
  }

  public Object getDefaultValue(Class paramClass)
  {
    return this.prototype.getDefaultValue(paramClass);
  }

  public boolean hasInstance(Scriptable paramScriptable)
  {
    return this.prototype.hasInstance(paramScriptable);
  }

  protected Object updateDotQuery(boolean paramBoolean)
  {
    throw new IllegalStateException();
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if ((paramIdFunctionObject.hasTag(FTAG)) && (paramIdFunctionObject.methodId() == 1))
      throw Context.reportRuntimeError1("msg.cant.call.indirect", "With");
    throw paramIdFunctionObject.unknown();
  }

  static boolean isWithFunction(Object paramObject)
  {
    if (paramObject instanceof IdFunctionObject)
    {
      IdFunctionObject localIdFunctionObject = (IdFunctionObject)paramObject;
      return ((localIdFunctionObject.hasTag(FTAG)) && (localIdFunctionObject.methodId() == 1));
    }
    return false;
  }

  static Object newWithSpecial(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    ScriptRuntime.checkDeprecated(paramContext, "With");
    paramScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
    NativeWith localNativeWith = new NativeWith();
    localNativeWith.setPrototype((paramArrayOfObject.length == 0) ? ScriptableObject.getClassPrototype(paramScriptable, "Object") : ScriptRuntime.toObject(paramContext, paramScriptable, paramArrayOfObject[0]));
    localNativeWith.setParentScope(paramScriptable);
    return localNativeWith;
  }
}
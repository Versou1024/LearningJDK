package sun.org.mozilla.javascript.internal;

public class IdFunctionObject extends BaseFunction
{
  static final long serialVersionUID = -5332312783643935019L;
  private final IdFunctionCall idcall;
  private final Object tag;
  private final int methodId;
  private int arity;
  private boolean useCallAsConstructor;
  private String functionName;

  public IdFunctionObject(IdFunctionCall paramIdFunctionCall, Object paramObject, int paramInt1, int paramInt2)
  {
    if (paramInt2 < 0)
      throw new IllegalArgumentException();
    this.idcall = paramIdFunctionCall;
    this.tag = paramObject;
    this.methodId = paramInt1;
    this.arity = paramInt2;
    if (paramInt2 < 0)
      throw new IllegalArgumentException();
  }

  public IdFunctionObject(IdFunctionCall paramIdFunctionCall, Object paramObject, int paramInt1, String paramString, int paramInt2, Scriptable paramScriptable)
  {
    super(paramScriptable, null);
    if (paramInt2 < 0)
      throw new IllegalArgumentException();
    if (paramString == null)
      throw new IllegalArgumentException();
    this.idcall = paramIdFunctionCall;
    this.tag = paramObject;
    this.methodId = paramInt1;
    this.arity = paramInt2;
    this.functionName = paramString;
  }

  public void initFunction(String paramString, Scriptable paramScriptable)
  {
    if (paramString == null)
      throw new IllegalArgumentException();
    if (paramScriptable == null)
      throw new IllegalArgumentException();
    this.functionName = paramString;
    setParentScope(paramScriptable);
  }

  public final boolean hasTag(Object paramObject)
  {
    return (this.tag == paramObject);
  }

  public final int methodId()
  {
    return this.methodId;
  }

  public final void markAsConstructor(Scriptable paramScriptable)
  {
    this.useCallAsConstructor = true;
    setImmunePrototypeProperty(paramScriptable);
  }

  public final void addAsProperty(Scriptable paramScriptable)
  {
    ScriptableObject.defineProperty(paramScriptable, this.functionName, this, 2);
  }

  public void exportAsScopeProperty()
  {
    addAsProperty(getParentScope());
  }

  public Scriptable getPrototype()
  {
    Scriptable localScriptable = super.getPrototype();
    if (localScriptable == null)
    {
      localScriptable = getFunctionPrototype(getParentScope());
      setPrototype(localScriptable);
    }
    return localScriptable;
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    return this.idcall.execIdCall(this, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
  }

  public Scriptable createObject(Context paramContext, Scriptable paramScriptable)
  {
    if (this.useCallAsConstructor)
      return null;
    throw ScriptRuntime.typeError1("msg.not.ctor", this.functionName);
  }

  String decompile(int paramInt1, int paramInt2)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = (0 != (paramInt2 & 0x1)) ? 1 : 0;
    if (i == 0)
    {
      localStringBuffer.append("function ");
      localStringBuffer.append(getFunctionName());
      localStringBuffer.append("() { ");
    }
    localStringBuffer.append("[native code for ");
    if (this.idcall instanceof Scriptable)
    {
      Scriptable localScriptable = (Scriptable)this.idcall;
      localStringBuffer.append(localScriptable.getClassName());
      localStringBuffer.append('.');
    }
    localStringBuffer.append(getFunctionName());
    localStringBuffer.append(", arity=");
    localStringBuffer.append(getArity());
    localStringBuffer.append((i != 0) ? "]\n" : "] }\n");
    return localStringBuffer.toString();
  }

  public int getArity()
  {
    return this.arity;
  }

  public int getLength()
  {
    return getArity();
  }

  public String getFunctionName()
  {
    return ((this.functionName == null) ? "" : this.functionName);
  }

  public final RuntimeException unknown()
  {
    return new IllegalArgumentException("BAD FUNCTION ID=" + this.methodId + " MASTER=" + this.idcall);
  }
}
package sun.org.mozilla.javascript.internal;

class SpecialRef extends Ref
{
  static final long serialVersionUID = -7521596632456797847L;
  private static final int SPECIAL_NONE = 0;
  private static final int SPECIAL_PROTO = 1;
  private static final int SPECIAL_PARENT = 2;
  private Scriptable target;
  private int type;
  private String name;

  private SpecialRef(Scriptable paramScriptable, int paramInt, String paramString)
  {
    this.target = paramScriptable;
    this.type = paramInt;
    this.name = paramString;
  }

  static Ref createSpecial(Context paramContext, Object paramObject, String paramString)
  {
    int i;
    Scriptable localScriptable = ScriptRuntime.toObjectOrNull(paramContext, paramObject);
    if (localScriptable == null)
      throw ScriptRuntime.undefReadError(paramObject, paramString);
    if (paramString.equals("__proto__"))
      i = 1;
    else if (paramString.equals("__parent__"))
      i = 2;
    else
      throw new IllegalArgumentException(paramString);
    if (!(paramContext.hasFeature(5)))
      i = 0;
    return new SpecialRef(localScriptable, i, paramString);
  }

  public Object get(Context paramContext)
  {
    switch (this.type)
    {
    case 0:
      return ScriptRuntime.getObjectProp(this.target, this.name, paramContext);
    case 1:
      return this.target.getPrototype();
    case 2:
      return this.target.getParentScope();
    }
    throw Kit.codeBug();
  }

  public Object set(Context paramContext, Object paramObject)
  {
    switch (this.type)
    {
    case 0:
      return ScriptRuntime.setObjectProp(this.target, this.name, paramObject, paramContext);
    case 1:
    case 2:
      Scriptable localScriptable1 = ScriptRuntime.toObjectOrNull(paramContext, paramObject);
      if (localScriptable1 != null)
      {
        Scriptable localScriptable2 = localScriptable1;
        do
        {
          if (localScriptable2 == this.target)
            throw Context.reportRuntimeError1("msg.cyclic.value", this.name);
          if (this.type == 1)
            localScriptable2 = localScriptable2.getPrototype();
          else
            localScriptable2 = localScriptable2.getParentScope();
        }
        while (localScriptable2 != null);
      }
      if (this.type == 1)
        this.target.setPrototype(localScriptable1);
      else
        this.target.setParentScope(localScriptable1);
      return localScriptable1;
    }
    throw Kit.codeBug();
  }

  public boolean has(Context paramContext)
  {
    if (this.type == 0)
      return ScriptRuntime.hasObjectElem(this.target, this.name, paramContext);
    return true;
  }

  public boolean delete(Context paramContext)
  {
    if (this.type == 0)
      return ScriptRuntime.deleteObjectElem(this.target, this.name, paramContext);
    return false;
  }
}
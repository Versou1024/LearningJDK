package sun.org.mozilla.javascript.internal;

final class Arguments extends IdScriptableObject
{
  static final long serialVersionUID = 4275508002492040609L;
  private static final int Id_callee = 1;
  private static final int Id_length = 2;
  private static final int Id_caller = 3;
  private static final int MAX_INSTANCE_ID = 3;
  private Object callerObj;
  private Object calleeObj;
  private Object lengthObj;
  private NativeCall activation;
  private Object[] args;

  public Arguments(NativeCall paramNativeCall)
  {
    this.activation = paramNativeCall;
    Scriptable localScriptable = paramNativeCall.getParentScope();
    setParentScope(localScriptable);
    setPrototype(ScriptableObject.getObjectPrototype(localScriptable));
    this.args = paramNativeCall.originalArgs;
    this.lengthObj = new Integer(this.args.length);
    NativeFunction localNativeFunction = paramNativeCall.function;
    this.calleeObj = localNativeFunction;
    int i = localNativeFunction.getLanguageVersion();
    if ((i <= 130) && (i != 0))
      this.callerObj = null;
    else
      this.callerObj = NOT_FOUND;
  }

  public String getClassName()
  {
    return "Arguments";
  }

  public boolean has(int paramInt, Scriptable paramScriptable)
  {
    if ((0 <= paramInt) && (paramInt < this.args.length) && (this.args[paramInt] != NOT_FOUND))
      return true;
    return super.has(paramInt, paramScriptable);
  }

  public Object get(int paramInt, Scriptable paramScriptable)
  {
    if ((0 <= paramInt) && (paramInt < this.args.length))
    {
      Object localObject = this.args[paramInt];
      if (localObject != NOT_FOUND)
      {
        if (sharedWithActivation(paramInt))
        {
          NativeFunction localNativeFunction = this.activation.function;
          String str = localNativeFunction.getParamOrVarName(paramInt);
          localObject = this.activation.get(str, this.activation);
          if (localObject == NOT_FOUND)
            Kit.codeBug();
        }
        return localObject;
      }
    }
    return super.get(paramInt, paramScriptable);
  }

  private boolean sharedWithActivation(int paramInt)
  {
    NativeFunction localNativeFunction = this.activation.function;
    int i = localNativeFunction.getParamCount();
    if (paramInt < i)
    {
      if (paramInt < i - 1)
      {
        String str = localNativeFunction.getParamOrVarName(paramInt);
        for (int j = paramInt + 1; j < i; ++j)
          if (str.equals(localNativeFunction.getParamOrVarName(j)))
            return false;
      }
      return true;
    }
    return false;
  }

  public void put(int paramInt, Scriptable paramScriptable, Object paramObject)
  {
    if ((0 <= paramInt) && (paramInt < this.args.length) && (this.args[paramInt] != NOT_FOUND))
    {
      if (sharedWithActivation(paramInt))
      {
        ??? = this.activation.function.getParamOrVarName(paramInt);
        this.activation.put((String)???, this.activation, paramObject);
        return;
      }
      synchronized (this)
      {
        if (this.args[paramInt] == NOT_FOUND)
          break label121;
        if (this.args == this.activation.originalArgs)
          this.args = ((Object[])(Object[])this.args.clone());
        this.args[paramInt] = paramObject;
        label121: return;
      }
    }
    super.put(paramInt, paramScriptable, paramObject);
  }

  public void delete(int paramInt)
  {
    if ((0 <= paramInt) && (paramInt < this.args.length))
      synchronized (this)
      {
        if (this.args[paramInt] == NOT_FOUND)
          break label73;
        if (this.args == this.activation.originalArgs)
          this.args = ((Object[])(Object[])this.args.clone());
        this.args[paramInt] = NOT_FOUND;
        label73: return;
      }
    super.delete(paramInt);
  }

  protected int getMaxInstanceId()
  {
    return 3;
  }

  protected int findInstanceIdInfo(String paramString)
  {
    int j;
    int i = 0;
    String str = null;
    if (paramString.length() == 6)
    {
      int k = paramString.charAt(5);
      if (k == 101)
      {
        str = "callee";
        i = 1;
      }
      else if (k == 104)
      {
        str = "length";
        i = 2;
      }
      else if (k == 114)
      {
        str = "caller";
        i = 3;
      }
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    if (i == 0)
      return super.findInstanceIdInfo(paramString);
    switch (i)
    {
    case 1:
    case 2:
    case 3:
      j = 2;
      break;
    default:
      throw new IllegalStateException();
    }
    return instanceIdInfo(j, i);
  }

  protected String getInstanceIdName(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return "callee";
    case 2:
      return "length";
    case 3:
      return "caller";
    }
    return null;
  }

  protected Object getInstanceIdValue(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return this.calleeObj;
    case 2:
      return this.lengthObj;
    case 3:
      Object localObject = this.callerObj;
      if (localObject == UniqueTag.NULL_VALUE)
      {
        localObject = null;
      }
      else if (localObject == null)
      {
        NativeCall localNativeCall = this.activation.parentActivationCall;
        if (localNativeCall != null)
          localObject = localNativeCall.get("arguments", localNativeCall);
        else
          localObject = null;
      }
      return localObject;
    }
    return super.getInstanceIdValue(paramInt);
  }

  protected void setInstanceIdValue(int paramInt, Object paramObject)
  {
    switch (paramInt)
    {
    case 1:
      this.calleeObj = paramObject;
      return;
    case 2:
      this.lengthObj = paramObject;
      return;
    case 3:
      this.callerObj = ((paramObject != null) ? paramObject : UniqueTag.NULL_VALUE);
      return;
    }
    super.setInstanceIdValue(paramInt, paramObject);
  }

  Object[] getIds(boolean paramBoolean)
  {
    Object localObject1 = super.getIds(paramBoolean);
    if ((paramBoolean) && (this.args.length != 0))
    {
      int l;
      boolean[] arrayOfBoolean = null;
      int i = this.args.length;
      for (int j = 0; j != localObject1.length; ++j)
      {
        Object localObject2 = localObject1[j];
        if (localObject2 instanceof Integer)
        {
          l = ((Integer)localObject2).intValue();
          if ((0 <= l) && (l < this.args.length))
          {
            if (arrayOfBoolean == null)
              arrayOfBoolean = new boolean[this.args.length];
            if (arrayOfBoolean[l] == 0)
            {
              arrayOfBoolean[l] = true;
              --i;
            }
          }
        }
      }
      if (i != 0)
      {
        Object[] arrayOfObject = new Object[i + localObject1.length];
        System.arraycopy(localObject1, 0, arrayOfObject, i, localObject1.length);
        localObject1 = arrayOfObject;
        int k = 0;
        for (l = 0; l != this.args.length; ++l)
          if ((arrayOfBoolean == null) || (arrayOfBoolean[l] == 0))
          {
            localObject1[k] = new Integer(l);
            ++k;
          }
        if (k != i)
          Kit.codeBug();
      }
    }
    return ((Object)localObject1);
  }
}
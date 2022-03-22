package sun.org.mozilla.javascript.internal;

public class NativeObject extends IdScriptableObject
{
  static final long serialVersionUID = -6345305608474346996L;
  private static final Object OBJECT_TAG = new Object();
  private static final int Id_constructor = 1;
  private static final int Id_toString = 2;
  private static final int Id_toLocaleString = 3;
  private static final int Id_valueOf = 4;
  private static final int Id_hasOwnProperty = 5;
  private static final int Id_propertyIsEnumerable = 6;
  private static final int Id_isPrototypeOf = 7;
  private static final int Id_toSource = 8;
  private static final int MAX_PROTOTYPE_ID = 8;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeObject localNativeObject = new NativeObject();
    localNativeObject.exportAsJSClass(8, paramScriptable, paramBoolean);
  }

  public String getClassName()
  {
    return "Object";
  }

  public String toString()
  {
    return ScriptRuntime.defaultObjectToString(this);
  }

  protected void initPrototypeId(int paramInt)
  {
    String str;
    int i;
    switch (paramInt)
    {
    case 1:
      i = 1;
      str = "constructor";
      break;
    case 2:
      i = 0;
      str = "toString";
      break;
    case 3:
      i = 0;
      str = "toLocaleString";
      break;
    case 4:
      i = 0;
      str = "valueOf";
      break;
    case 5:
      i = 1;
      str = "hasOwnProperty";
      break;
    case 6:
      i = 1;
      str = "propertyIsEnumerable";
      break;
    case 7:
      i = 1;
      str = "isPrototypeOf";
      break;
    case 8:
      i = 0;
      str = "toSource";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(OBJECT_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    boolean bool;
    Object localObject;
    int k;
    if (!(paramIdFunctionObject.hasTag(OBJECT_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case 1:
      if (paramScriptable2 != null)
        return paramIdFunctionObject.construct(paramContext, paramScriptable1, paramArrayOfObject);
      if ((paramArrayOfObject.length == 0) || (paramArrayOfObject[0] == null) || (paramArrayOfObject[0] == Undefined.instance))
        return new NativeObject();
      return ScriptRuntime.toObject(paramContext, paramScriptable1, paramArrayOfObject[0]);
    case 2:
    case 3:
      if (paramContext.hasFeature(4))
      {
        String str = ScriptRuntime.defaultObjectToSource(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
        int j = str.length();
        if ((j != 0) && (str.charAt(0) == '(') && (str.charAt(j - 1) == ')'))
          str = str.substring(1, j - 1);
        return str;
      }
      return ScriptRuntime.defaultObjectToString(paramScriptable2);
    case 4:
      return paramScriptable2;
    case 5:
      if (paramArrayOfObject.length == 0)
      {
        bool = false;
      }
      else
      {
        localObject = ScriptRuntime.toStringIdOrIndex(paramContext, paramArrayOfObject[0]);
        if (localObject == null)
        {
          k = ScriptRuntime.lastIndexResult(paramContext);
          bool = paramScriptable2.has(k, paramScriptable2);
        }
        else
        {
          bool = paramScriptable2.has((String)localObject, paramScriptable2);
        }
      }
      return ScriptRuntime.wrapBoolean(bool);
    case 6:
      if (paramArrayOfObject.length == 0)
      {
        bool = false;
      }
      else
      {
        localObject = ScriptRuntime.toStringIdOrIndex(paramContext, paramArrayOfObject[0]);
        if (localObject == null)
        {
          k = ScriptRuntime.lastIndexResult(paramContext);
          bool = paramScriptable2.has(k, paramScriptable2);
          if ((bool) && (paramScriptable2 instanceof ScriptableObject))
          {
            ScriptableObject localScriptableObject2 = (ScriptableObject)paramScriptable2;
            int i1 = localScriptableObject2.getAttributes(k);
            bool = (i1 & 0x2) == 0;
          }
        }
        else
        {
          bool = paramScriptable2.has((String)localObject, paramScriptable2);
          if ((bool) && (paramScriptable2 instanceof ScriptableObject))
          {
            ScriptableObject localScriptableObject1 = (ScriptableObject)paramScriptable2;
            int l = localScriptableObject1.getAttributes((String)localObject);
            bool = (l & 0x2) == 0;
          }
        }
      }
      return ScriptRuntime.wrapBoolean(bool);
    case 7:
      bool = false;
      if ((paramArrayOfObject.length != 0) && (paramArrayOfObject[0] instanceof Scriptable))
      {
        localObject = (Scriptable)paramArrayOfObject[0];
        do
        {
          localObject = ((Scriptable)localObject).getPrototype();
          if (localObject == paramScriptable2)
          {
            bool = true;
            break;
          }
        }
        while (localObject != null);
      }
      return ScriptRuntime.wrapBoolean(bool);
    case 8:
      return ScriptRuntime.defaultObjectToSource(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  protected int findPrototypeId(String paramString)
  {
    int j;
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 7:
      str = "valueOf";
      i = 4;
      break;
    case 8:
      j = paramString.charAt(3);
      if (j == 111)
      {
        str = "toSource";
        i = 8;
      }
      else if (j == 116)
      {
        str = "toString";
        i = 2;
      }
      break;
    case 11:
      str = "constructor";
      i = 1;
      break;
    case 13:
      str = "isPrototypeOf";
      i = 7;
      break;
    case 14:
      j = paramString.charAt(0);
      if (j == 104)
      {
        str = "hasOwnProperty";
        i = 5;
      }
      else if (j == 116)
      {
        str = "toLocaleString";
        i = 3;
      }
      break;
    case 20:
      str = "propertyIsEnumerable";
      i = 6;
    case 9:
    case 10:
    case 12:
    case 15:
    case 16:
    case 17:
    case 18:
    case 19:
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    return i;
  }
}
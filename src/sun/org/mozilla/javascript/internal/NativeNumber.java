package sun.org.mozilla.javascript.internal;

final class NativeNumber extends IdScriptableObject
{
  static final long serialVersionUID = 3504516769741512101L;
  private static final Object NUMBER_TAG = new Object();
  private static final int MAX_PRECISION = 100;
  private static final int Id_constructor = 1;
  private static final int Id_toString = 2;
  private static final int Id_toLocaleString = 3;
  private static final int Id_toSource = 4;
  private static final int Id_valueOf = 5;
  private static final int Id_toFixed = 6;
  private static final int Id_toExponential = 7;
  private static final int Id_toPrecision = 8;
  private static final int MAX_PROTOTYPE_ID = 8;
  private double doubleValue;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeNumber localNativeNumber = new NativeNumber(0D);
    localNativeNumber.exportAsJSClass(8, paramScriptable, paramBoolean);
  }

  private NativeNumber(double paramDouble)
  {
    this.doubleValue = paramDouble;
  }

  public String getClassName()
  {
    return "Number";
  }

  protected void fillConstructorProperties(IdFunctionObject paramIdFunctionObject)
  {
    paramIdFunctionObject.defineProperty("NaN", ScriptRuntime.NaNobj, 7);
    paramIdFunctionObject.defineProperty("POSITIVE_INFINITY", ScriptRuntime.wrapNumber((1.0D / 0.0D)), 7);
    paramIdFunctionObject.defineProperty("NEGATIVE_INFINITY", ScriptRuntime.wrapNumber((-1.0D / 0.0D)), 7);
    paramIdFunctionObject.defineProperty("MAX_VALUE", ScriptRuntime.wrapNumber(1.7976931348623157e+308D), 7);
    paramIdFunctionObject.defineProperty("MIN_VALUE", ScriptRuntime.wrapNumber(4.9e-324D), 7);
    super.fillConstructorProperties(paramIdFunctionObject);
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
      i = 1;
      str = "toString";
      break;
    case 3:
      i = 1;
      str = "toLocaleString";
      break;
    case 4:
      i = 0;
      str = "toSource";
      break;
    case 5:
      i = 0;
      str = "valueOf";
      break;
    case 6:
      i = 1;
      str = "toFixed";
      break;
    case 7:
      i = 1;
      str = "toExponential";
      break;
    case 8:
      i = 1;
      str = "toPrecision";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(NUMBER_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(paramIdFunctionObject.hasTag(NUMBER_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    if (i == 1)
    {
      d = (paramArrayOfObject.length >= 1) ? ScriptRuntime.toNumber(paramArrayOfObject[0]) : 0D;
      if (paramScriptable2 == null)
        return new NativeNumber(d);
      return ScriptRuntime.wrapNumber(d);
    }
    if (!(paramScriptable2 instanceof NativeNumber))
      throw incompatibleCallError(paramIdFunctionObject);
    double d = ((NativeNumber)paramScriptable2).doubleValue;
    switch (i)
    {
    case 2:
    case 3:
      int j = (paramArrayOfObject.length == 0) ? 10 : ScriptRuntime.toInt32(paramArrayOfObject[0]);
      return ScriptRuntime.numberToString(d, j);
    case 4:
      return "(new Number(" + ScriptRuntime.toString(d) + "))";
    case 5:
      return ScriptRuntime.wrapNumber(d);
    case 6:
      return num_to(d, paramArrayOfObject, 2, 2, -20, 0);
    case 7:
      return num_to(d, paramArrayOfObject, 1, 3, 0, 1);
    case 8:
      return num_to(d, paramArrayOfObject, 0, 4, 1, 0);
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  public String toString()
  {
    return ScriptRuntime.numberToString(this.doubleValue, 10);
  }

  private static String num_to(double paramDouble, Object[] paramArrayOfObject, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i;
    if (paramArrayOfObject.length == 0)
    {
      i = 0;
      paramInt2 = paramInt1;
    }
    else
    {
      i = ScriptRuntime.toInt32(paramArrayOfObject[0]);
      if ((i < paramInt3) || (i > 100))
      {
        localObject = ScriptRuntime.getMessage1("msg.bad.precision", ScriptRuntime.toString(paramArrayOfObject[0]));
        throw ScriptRuntime.constructError("RangeError", (String)localObject);
      }
    }
    Object localObject = new StringBuffer();
    DToA.JS_dtostr((StringBuffer)localObject, paramInt2, i + paramInt4, paramDouble);
    return ((String)((StringBuffer)localObject).toString());
  }

  protected int findPrototypeId(String paramString)
  {
    int j;
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 7:
      j = paramString.charAt(0);
      if (j == 116)
      {
        str = "toFixed";
        i = 6;
      }
      else if (j == 118)
      {
        str = "valueOf";
        i = 5;
      }
      break;
    case 8:
      j = paramString.charAt(3);
      if (j == 111)
      {
        str = "toSource";
        i = 4;
      }
      else if (j == 116)
      {
        str = "toString";
        i = 2;
      }
      break;
    case 11:
      j = paramString.charAt(0);
      if (j == 99)
      {
        str = "constructor";
        i = 1;
      }
      else if (j == 116)
      {
        str = "toPrecision";
        i = 8;
      }
      break;
    case 13:
      str = "toExponential";
      i = 7;
      break;
    case 14:
      str = "toLocaleString";
      i = 3;
    case 9:
    case 10:
    case 12:
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    return i;
  }
}
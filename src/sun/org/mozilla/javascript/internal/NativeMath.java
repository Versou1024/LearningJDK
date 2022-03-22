package sun.org.mozilla.javascript.internal;

final class NativeMath extends IdScriptableObject
{
  static final long serialVersionUID = -8838847185801131569L;
  private static final Object MATH_TAG = new Object();
  private static final int Id_toSource = 1;
  private static final int Id_abs = 2;
  private static final int Id_acos = 3;
  private static final int Id_asin = 4;
  private static final int Id_atan = 5;
  private static final int Id_atan2 = 6;
  private static final int Id_ceil = 7;
  private static final int Id_cos = 8;
  private static final int Id_exp = 9;
  private static final int Id_floor = 10;
  private static final int Id_log = 11;
  private static final int Id_max = 12;
  private static final int Id_min = 13;
  private static final int Id_pow = 14;
  private static final int Id_random = 15;
  private static final int Id_round = 16;
  private static final int Id_sin = 17;
  private static final int Id_sqrt = 18;
  private static final int Id_tan = 19;
  private static final int LAST_METHOD_ID = 19;
  private static final int Id_E = 20;
  private static final int Id_PI = 21;
  private static final int Id_LN10 = 22;
  private static final int Id_LN2 = 23;
  private static final int Id_LOG2E = 24;
  private static final int Id_LOG10E = 25;
  private static final int Id_SQRT1_2 = 26;
  private static final int Id_SQRT2 = 27;
  private static final int MAX_ID = 27;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeMath localNativeMath = new NativeMath();
    localNativeMath.activatePrototypeMap(27);
    localNativeMath.setPrototype(getObjectPrototype(paramScriptable));
    localNativeMath.setParentScope(paramScriptable);
    if (paramBoolean)
      localNativeMath.sealObject();
    ScriptableObject.defineProperty(paramScriptable, "Math", localNativeMath, 2);
  }

  public String getClassName()
  {
    return "Math";
  }

  protected void initPrototypeId(int paramInt)
  {
    String str;
    if (paramInt <= 19)
    {
      int i;
      switch (paramInt)
      {
      case 1:
        i = 0;
        str = "toSource";
        break;
      case 2:
        i = 1;
        str = "abs";
        break;
      case 3:
        i = 1;
        str = "acos";
        break;
      case 4:
        i = 1;
        str = "asin";
        break;
      case 5:
        i = 1;
        str = "atan";
        break;
      case 6:
        i = 2;
        str = "atan2";
        break;
      case 7:
        i = 1;
        str = "ceil";
        break;
      case 8:
        i = 1;
        str = "cos";
        break;
      case 9:
        i = 1;
        str = "exp";
        break;
      case 10:
        i = 1;
        str = "floor";
        break;
      case 11:
        i = 1;
        str = "log";
        break;
      case 12:
        i = 2;
        str = "max";
        break;
      case 13:
        i = 2;
        str = "min";
        break;
      case 14:
        i = 2;
        str = "pow";
        break;
      case 15:
        i = 0;
        str = "random";
        break;
      case 16:
        i = 1;
        str = "round";
        break;
      case 17:
        i = 1;
        str = "sin";
        break;
      case 18:
        i = 1;
        str = "sqrt";
        break;
      case 19:
        i = 1;
        str = "tan";
        break;
      default:
        throw new IllegalStateException(String.valueOf(paramInt));
      }
      initPrototypeMethod(MATH_TAG, paramInt, str, i);
    }
    else
    {
      double d;
      switch (paramInt)
      {
      case 20:
        d = 2.7182818284590451D;
        str = "E";
        break;
      case 21:
        d = 3.1415926535897931D;
        str = "PI";
        break;
      case 22:
        d = 2.3025850929940459D;
        str = "LN10";
        break;
      case 23:
        d = 0.69314718055994529D;
        str = "LN2";
        break;
      case 24:
        d = 1.4426950408889634D;
        str = "LOG2E";
        break;
      case 25:
        d = 0.43429448190325182D;
        str = "LOG10E";
        break;
      case 26:
        d = 0.70710678118654757D;
        str = "SQRT1_2";
        break;
      case 27:
        d = 1.4142135623730951D;
        str = "SQRT2";
        break;
      default:
        throw new IllegalStateException(String.valueOf(paramInt));
      }
      initPrototypeValue(paramInt, str, ScriptRuntime.wrapNumber(d), 7);
    }
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    double d1;
    if (!(paramIdFunctionObject.hasTag(MATH_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case 1:
      return "Math";
    case 2:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = (d1 < 0D) ? -d1 : (d1 == 0D) ? 0D : d1;
      break;
    case 3:
    case 4:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      if ((d1 == d1) && (-1.0D <= d1) && (d1 <= 1D))
      {
        d1 = (i == 3) ? Math.acos(d1) : Math.asin(d1);
        break label721:
      }
      d1 = (0.0D / 0.0D);
      break;
    case 5:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = Math.atan(d1);
      break;
    case 6:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = Math.atan2(d1, ScriptRuntime.toNumber(paramArrayOfObject, 1));
      break;
    case 7:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = Math.ceil(d1);
      break;
    case 8:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = ((d1 == (1.0D / 0.0D)) || (d1 == (-1.0D / 0.0D))) ? (0.0D / 0.0D) : Math.cos(d1);
      break;
    case 9:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = (d1 == (-1.0D / 0.0D)) ? 0D : (d1 == (1.0D / 0.0D)) ? d1 : Math.exp(d1);
      break;
    case 10:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = Math.floor(d1);
      break;
    case 11:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = (d1 < 0D) ? (0.0D / 0.0D) : Math.log(d1);
      break;
    case 12:
    case 13:
      d1 = (i == 12) ? (-1.0D / 0.0D) : (1.0D / 0.0D);
      for (int j = 0; j != paramArrayOfObject.length; ++j)
      {
        double d2 = ScriptRuntime.toNumber(paramArrayOfObject[j]);
        if (d2 != d2)
        {
          d1 = d2;
          break;
        }
        if (i == 12)
          d1 = Math.max(d1, d2);
        else
          d1 = Math.min(d1, d2);
      }
      break;
    case 14:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = js_pow(d1, ScriptRuntime.toNumber(paramArrayOfObject, 1));
      break;
    case 15:
      d1 = Math.random();
      break;
    case 16:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      if ((d1 != d1) || (d1 == (1.0D / 0.0D)) || (d1 == (-1.0D / 0.0D)))
        break label721;
      long l = Math.round(d1);
      if (l != 3412047497411952640L)
      {
        d1 = l;
        break label627:
      }
      if (d1 < 0D)
      {
        d1 = ScriptRuntime.negativeZero;
        break label627:
      }
      if (d1 == 0D)
        break label627;
      d1 = 0D;
      break;
    case 17:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = ((d1 == (1.0D / 0.0D)) || (d1 == (-1.0D / 0.0D))) ? (0.0D / 0.0D) : Math.sin(d1);
      break;
    case 18:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = Math.sqrt(d1);
      break;
    case 19:
      d1 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      d1 = Math.tan(d1);
      break;
    default:
      label627: throw new IllegalStateException(String.valueOf(i));
    }
    label721: return ScriptRuntime.wrapNumber(d1);
  }

  private double js_pow(double paramDouble1, double paramDouble2)
  {
    double d;
    if (paramDouble2 != paramDouble2)
    {
      d = paramDouble2;
    }
    else if (paramDouble2 == 0D)
    {
      d = 1D;
    }
    else
    {
      long l;
      if (paramDouble1 == 0D)
      {
        if (1D / paramDouble1 > 0D)
        {
          d = (paramDouble2 > 0D) ? 0D : (1.0D / 0.0D);
        }
        else
        {
          l = ()paramDouble2;
          if ((l == paramDouble2) && ((l & 3412040865982447617L) != 3412040144427941888L))
            d = (paramDouble2 > 0D) ? -0.0D : (-1.0D / 0.0D);
          else
            d = (paramDouble2 > 0D) ? 0D : (1.0D / 0.0D);
        }
      }
      else
      {
        d = Math.pow(paramDouble1, paramDouble2);
        if (d != d)
          if (paramDouble2 == (1.0D / 0.0D))
          {
            if ((paramDouble1 < -1.0D) || (1D < paramDouble1))
              d = (1.0D / 0.0D);
            else if ((-1.0D < paramDouble1) && (paramDouble1 < 1D))
              d = 0D;
          }
          else if (paramDouble2 == (-1.0D / 0.0D))
          {
            if ((paramDouble1 < -1.0D) || (1D < paramDouble1))
              d = 0D;
            else if ((-1.0D < paramDouble1) && (paramDouble1 < 1D))
              d = (1.0D / 0.0D);
          }
          else if (paramDouble1 == (1.0D / 0.0D))
          {
            d = (paramDouble2 > 0D) ? (1.0D / 0.0D) : 0D;
          }
          else if (paramDouble1 == (-1.0D / 0.0D))
          {
            l = ()paramDouble2;
            if ((l == paramDouble2) && ((l & 3412041484457738241L) != 3412040762903232512L))
              d = (paramDouble2 > 0D) ? (-1.0D / 0.0D) : -0.0D;
            else
              d = (paramDouble2 > 0D) ? (1.0D / 0.0D) : 0D;
          }
      }
    }
    return d;
  }

  protected int findPrototypeId(String paramString)
  {
    int j;
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 1:
      if (paramString.charAt(0) == 'E')
        i = 20;
      break;
    case 2:
      if ((paramString.charAt(0) == 'P') && (paramString.charAt(1) == 'I'))
        i = 21;
      break;
    case 3:
      switch (paramString.charAt(0))
      {
      case 'L':
        if ((paramString.charAt(2) == '2') && (paramString.charAt(1) == 'N'))
          i = 23;
        break;
      case 'a':
        if ((paramString.charAt(2) == 's') && (paramString.charAt(1) == 'b'))
          i = 2;
        break;
      case 'c':
        if ((paramString.charAt(2) == 's') && (paramString.charAt(1) == 'o'))
          i = 8;
        break;
      case 'e':
        if ((paramString.charAt(2) == 'p') && (paramString.charAt(1) == 'x'))
          i = 9;
        break;
      case 'l':
        if ((paramString.charAt(2) == 'g') && (paramString.charAt(1) == 'o'))
          i = 11;
        break;
      case 'm':
        j = paramString.charAt(2);
        if (j == 110)
        {
          if (paramString.charAt(1) != 'i')
            break label724;
          i = 13;
          break label743:
        }
        if ((j == 120) && (paramString.charAt(1) == 'a'))
          i = 12;
        break;
      case 'p':
        if ((paramString.charAt(2) == 'w') && (paramString.charAt(1) == 'o'))
          i = 14;
        break;
      case 's':
        if ((paramString.charAt(2) == 'n') && (paramString.charAt(1) == 'i'))
          i = 17;
        break;
      case 't':
        if ((paramString.charAt(2) == 'n') && (paramString.charAt(1) == 'a'))
        {
          i = 19;
          break label743:
        }
      }
      break;
    case 4:
      switch (paramString.charAt(1))
      {
      case 'N':
        str = "LN10";
        i = 22;
        break;
      case 'c':
        str = "acos";
        i = 3;
        break;
      case 'e':
        str = "ceil";
        i = 7;
        break;
      case 'q':
        str = "sqrt";
        i = 18;
        break;
      case 's':
        str = "asin";
        i = 4;
        break;
      case 't':
        str = "atan";
        i = 5;
        break label724:
      }
      break;
    case 5:
      switch (paramString.charAt(0))
      {
      case 'L':
        str = "LOG2E";
        i = 24;
        break;
      case 'S':
        str = "SQRT2";
        i = 27;
        break;
      case 'a':
        str = "atan2";
        i = 6;
        break;
      case 'f':
        str = "floor";
        i = 10;
        break;
      case 'r':
        str = "round";
        i = 16;
        break label724:
      }
      break;
    case 6:
      j = paramString.charAt(0);
      if (j == 76)
      {
        str = "LOG10E";
        i = 25;
      }
      else if (j == 114)
      {
        str = "random";
        i = 15;
      }
      break;
    case 7:
      str = "SQRT1_2";
      i = 26;
      break;
    case 8:
      str = "toSource";
      i = 1;
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      label724: i = 0;
    label743: return i;
  }
}
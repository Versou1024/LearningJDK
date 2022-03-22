package sun.org.mozilla.javascript.internal;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import sun.org.mozilla.javascript.internal.continuations.Continuation;
import sun.org.mozilla.javascript.internal.xml.XMLLib;
import sun.org.mozilla.javascript.internal.xml.XMLObject;

public class ScriptRuntime
{
  public static final Class BooleanClass = Kit.classOrNull("java.lang.Boolean");
  public static final Class ByteClass = Kit.classOrNull("java.lang.Byte");
  public static final Class CharacterClass = Kit.classOrNull("java.lang.Character");
  public static final Class ClassClass = Kit.classOrNull("java.lang.Class");
  public static final Class DoubleClass = Kit.classOrNull("java.lang.Double");
  public static final Class FloatClass = Kit.classOrNull("java.lang.Float");
  public static final Class IntegerClass = Kit.classOrNull("java.lang.Integer");
  public static final Class LongClass = Kit.classOrNull("java.lang.Long");
  public static final Class NumberClass = Kit.classOrNull("java.lang.Number");
  public static final Class ObjectClass = Kit.classOrNull("java.lang.Object");
  public static final Class ShortClass = Kit.classOrNull("java.lang.Short");
  public static final Class StringClass = Kit.classOrNull("java.lang.String");
  public static final Class DateClass = Kit.classOrNull("java.util.Date");
  public static final Class ContextClass = Kit.classOrNull("sun.org.mozilla.javascript.internal.Context");
  public static final Class ContextFactoryClass = Kit.classOrNull("sun.org.mozilla.javascript.internal.ContextFactory");
  public static final Class FunctionClass = Kit.classOrNull("sun.org.mozilla.javascript.internal.Function");
  public static final Class ScriptableClass = Kit.classOrNull("sun.org.mozilla.javascript.internal.Scriptable");
  public static final Class ScriptableObjectClass = Kit.classOrNull("sun.org.mozilla.javascript.internal.ScriptableObject");
  private static final String XML_INIT_CLASS = "sun.org.mozilla.javascript.internal.xmlimpl.XMLLibImpl";
  private static final String[] lazilyNames = { "RegExp", "sun.org.mozilla.javascript.internal.regexp.NativeRegExp", "Packages", "sun.org.mozilla.javascript.internal.NativeJavaTopPackage", "java", "sun.org.mozilla.javascript.internal.NativeJavaTopPackage", "getClass", "sun.org.mozilla.javascript.internal.NativeJavaTopPackage", "JavaAdapter", "sun.org.mozilla.javascript.internal.JavaAdapter", "JavaImporter", "sun.org.mozilla.javascript.internal.ImporterTopLevel", "XML", "sun.org.mozilla.javascript.internal.xmlimpl.XMLLibImpl", "XMLList", "sun.org.mozilla.javascript.internal.xmlimpl.XMLLibImpl", "Namespace", "sun.org.mozilla.javascript.internal.xmlimpl.XMLLibImpl", "QName", "sun.org.mozilla.javascript.internal.xmlimpl.XMLLibImpl" };
  private static final Object LIBRARY_SCOPE_KEY = new Object();
  public static final double NaN = Double.longBitsToDouble(9221120237041090560L);
  public static final double negativeZero = Double.longBitsToDouble(-9223372036854775808L);
  public static final Double NaNobj = new Double(NaN);
  private static final boolean MSJVM_BUG_WORKAROUNDS = 1;
  private static final String DEFAULT_NS_TAG = "__default_namespace__";
  public static final Object[] emptyArgs = new Object[0];
  public static final String[] emptyStrings = new String[0];

  public static boolean isRhinoRuntimeType(Class paramClass)
  {
    if (paramClass.isPrimitive())
      return (paramClass != Character.TYPE);
    return ((paramClass == StringClass) || (paramClass == BooleanClass) || (NumberClass.isAssignableFrom(paramClass)) || (ScriptableClass.isAssignableFrom(paramClass)));
  }

  public static ScriptableObject initStandardObjects(Context paramContext, ScriptableObject paramScriptableObject, boolean paramBoolean)
  {
    if (paramScriptableObject == null)
      paramScriptableObject = new NativeObject();
    paramScriptableObject.associateValue(LIBRARY_SCOPE_KEY, paramScriptableObject);
    new ClassCache().associate(paramScriptableObject);
    BaseFunction.init(paramScriptableObject, paramBoolean);
    NativeObject.init(paramScriptableObject, paramBoolean);
    Scriptable localScriptable1 = ScriptableObject.getObjectPrototype(paramScriptableObject);
    Scriptable localScriptable2 = ScriptableObject.getFunctionPrototype(paramScriptableObject);
    localScriptable2.setPrototype(localScriptable1);
    if (paramScriptableObject.getPrototype() == null)
      paramScriptableObject.setPrototype(localScriptable1);
    NativeError.init(paramScriptableObject, paramBoolean);
    NativeGlobal.init(paramContext, paramScriptableObject, paramBoolean);
    NativeArray.init(paramScriptableObject, paramBoolean);
    NativeString.init(paramScriptableObject, paramBoolean);
    NativeBoolean.init(paramScriptableObject, paramBoolean);
    NativeNumber.init(paramScriptableObject, paramBoolean);
    NativeDate.init(paramContext, paramScriptableObject, paramBoolean);
    NativeMath.init(paramScriptableObject, paramBoolean);
    NativeWith.init(paramScriptableObject, paramBoolean);
    NativeCall.init(paramScriptableObject, paramBoolean);
    NativeScript.init(paramScriptableObject, paramBoolean);
    boolean bool = paramContext.hasFeature(6);
    for (int i = 0; i != lazilyNames.length; i += 2)
    {
      String str1 = lazilyNames[i];
      String str2 = lazilyNames[(i + 1)];
      if ((!(bool)) && (str2 == "sun.org.mozilla.javascript.internal.xmlimpl.XMLLibImpl"))
        break label198:
      label198: new LazilyLoadedCtor(paramScriptableObject, str1, str2, paramBoolean);
    }
    Continuation.init(paramScriptableObject, paramBoolean);
    return paramScriptableObject;
  }

  public static ScriptableObject getLibraryScopeOrNull(Scriptable paramScriptable)
  {
    ScriptableObject localScriptableObject = (ScriptableObject)ScriptableObject.getTopScopeValue(paramScriptable, LIBRARY_SCOPE_KEY);
    return localScriptableObject;
  }

  public static boolean isJSLineTerminator(int paramInt)
  {
    if ((paramInt & 0xDFD0) != 0)
      return false;
    return ((paramInt == 10) || (paramInt == 13) || (paramInt == 8232) || (paramInt == 8233));
  }

  public static Boolean wrapBoolean(boolean paramBoolean)
  {
    return ((paramBoolean) ? Boolean.TRUE : Boolean.FALSE);
  }

  public static Integer wrapInt(int paramInt)
  {
    return new Integer(paramInt);
  }

  public static Number wrapNumber(double paramDouble)
  {
    if (paramDouble != paramDouble)
      return NaNobj;
    return new Double(paramDouble);
  }

  public static boolean toBoolean(Object paramObject)
  {
    do
    {
      if (paramObject instanceof Boolean)
        return ((Boolean)paramObject).booleanValue();
      if ((paramObject == null) || (paramObject == Undefined.instance))
        return false;
      if (paramObject instanceof String)
        return (((String)paramObject).length() != 0);
      if (paramObject instanceof Number)
      {
        double d = ((Number)paramObject).doubleValue();
        return ((d == d) && (d != 0D));
      }
      if (!(paramObject instanceof Scriptable))
        break label129;
      if (Context.getContext().isVersionECMA1())
        return true;
      paramObject = ((Scriptable)paramObject).getDefaultValue(BooleanClass);
    }
    while (!(paramObject instanceof Scriptable));
    throw errorWithClassName("msg.primitive.expected", paramObject);
    label129: warnAboutNonJSObject(paramObject);
    return true;
  }

  public static boolean toBoolean(Object[] paramArrayOfObject, int paramInt)
  {
    return ((paramInt < paramArrayOfObject.length) ? toBoolean(paramArrayOfObject[paramInt]) : false);
  }

  public static double toNumber(Object paramObject)
  {
    do
    {
      if (paramObject instanceof Number)
        return ((Number)paramObject).doubleValue();
      if (paramObject == null)
        return 0D;
      if (paramObject == Undefined.instance)
        return NaN;
      if (paramObject instanceof String)
        return toNumber((String)paramObject);
      if (paramObject instanceof Boolean)
        return ((((Boolean)paramObject).booleanValue()) ? 1D : 0D);
      if (!(paramObject instanceof Scriptable))
        break label104;
      paramObject = ((Scriptable)paramObject).getDefaultValue(NumberClass);
    }
    while (!(paramObject instanceof Scriptable));
    throw errorWithClassName("msg.primitive.expected", paramObject);
    label104: warnAboutNonJSObject(paramObject);
    return NaN;
  }

  public static double toNumber(Object[] paramArrayOfObject, int paramInt)
  {
    return ((paramInt < paramArrayOfObject.length) ? toNumber(paramArrayOfObject[paramInt]) : NaN);
  }

  static double stringToNumber(String paramString, int paramInt1, int paramInt2)
  {
    int i4;
    int i = 57;
    int j = 97;
    int k = 65;
    int l = paramString.length();
    if (paramInt2 < 10)
      i = (char)(48 + paramInt2 - 1);
    if (paramInt2 > 10)
    {
      j = (char)(97 + paramInt2 - 10);
      k = (char)(65 + paramInt2 - 10);
    }
    double d1 = 0D;
    for (int i1 = paramInt1; i1 < l; ++i1)
    {
      int i2 = paramString.charAt(i1);
      if ((48 <= i2) && (i2 <= i))
      {
        i4 = i2 - 48;
      }
      else if ((97 <= i2) && (i2 < j))
      {
        i4 = i2 - 97 + 10;
      }
      else
      {
        if ((65 > i2) || (i2 >= k))
          break;
        i4 = i2 - 65 + 10;
      }
      d1 = d1 * paramInt2 + i4;
    }
    if (paramInt1 == i1)
      return NaN;
    if (d1 >= 9007199254740992.0D)
    {
      if (paramInt2 == 10)
        try
        {
          return Double.valueOf(paramString.substring(paramInt1, i1)).doubleValue();
        }
        catch (NumberFormatException localNumberFormatException)
        {
          return NaN;
        }
      if ((paramInt2 == 2) || (paramInt2 == 4) || (paramInt2 == 8) || (paramInt2 == 16) || (paramInt2 == 32))
      {
        int i3 = 1;
        i4 = 0;
        int i5 = 0;
        int i6 = 53;
        double d2 = 0D;
        int i7 = 0;
        int i8 = 0;
        while (true)
        {
          if (i3 == 1)
          {
            if (paramInt1 == i1)
              break;
            i4 = paramString.charAt(paramInt1++);
            if ((48 <= i4) && (i4 <= 57))
              i4 -= 48;
            else if ((97 <= i4) && (i4 <= 122))
              i4 -= 87;
            else
              i4 -= 55;
            i3 = paramInt2;
          }
          i3 >>= 1;
          int i9 = ((i4 & i3) != 0) ? 1 : 0;
          switch (i5)
          {
          case 0:
            if (i9 != 0)
            {
              --i6;
              d1 = 1D;
              i5 = 1;
            }
            break;
          case 1:
            d1 *= 2.0D;
            if (i9 != 0)
              d1 += 1D;
            if (--i6 == 0)
            {
              i7 = i9;
              i5 = 2;
            }
            break;
          case 2:
            i8 = i9;
            d2 = 2.0D;
            i5 = 3;
            break;
          case 3:
            if (i9 != 0)
              i5 = 4;
          case 4:
            d2 *= 2.0D;
          }
        }
        switch (i5)
        {
        case 0:
          d1 = 0D;
          break;
        case 1:
        case 2:
          break;
        case 3:
          if ((i8 & i7) != 0)
            d1 += 1D;
          d1 *= d2;
          break;
        case 4:
          if (i8 != 0)
            d1 += 1D;
          d1 *= d2;
        }
      }
    }
    return d1;
  }

  public static double toNumber(String paramString)
  {
    char c;
    int l;
    int i = paramString.length();
    int j = 0;
    while (true)
    {
      if (j == i)
        return 0D;
      c = paramString.charAt(j);
      if (!(Character.isWhitespace(c)))
        break;
      ++j;
    }
    if (c == '0')
    {
      if (j + 2 < i)
      {
        k = paramString.charAt(j + 1);
        if ((k == 120) || (k == 88))
          return stringToNumber(paramString, j + 2, 16);
      }
    }
    else if ((((c == '+') || (c == '-'))) && (j + 3 < i) && (paramString.charAt(j + 1) == '0'))
    {
      k = paramString.charAt(j + 2);
      if ((k == 120) || (k == 88))
      {
        double d = stringToNumber(paramString, j + 3, 16);
        return ((c == '-') ? -d : d);
      }
    }
    for (int k = i - 1; Character.isWhitespace(l = paramString.charAt(k)); --k);
    if (l == 121)
    {
      if ((c == '+') || (c == '-'))
        ++j;
      if ((j + 7 == k) && (paramString.regionMatches(j, "Infinity", 0, 8)))
        return ((c == '-') ? (-1.0D / 0.0D) : (1.0D / 0.0D));
      return NaN;
    }
    String str = paramString.substring(j, k + 1);
    for (int i1 = str.length() - 1; i1 >= 0; --i1)
    {
      int i2 = str.charAt(i1);
      if ((((48 > i2) || (i2 > 57))) && (i2 != 46) && (i2 != 101) && (i2 != 69) && (i2 != 43))
      {
        if (i2 == 45)
          break label345:
        label345: return NaN;
      }
    }
    try
    {
      return Double.valueOf(str).doubleValue();
    }
    catch (NumberFormatException localNumberFormatException)
    {
    }
    return NaN;
  }

  public static Object[] padArguments(Object[] paramArrayOfObject, int paramInt)
  {
    if (paramInt < paramArrayOfObject.length)
      return paramArrayOfObject;
    Object[] arrayOfObject = new Object[paramInt];
    for (int i = 0; i < paramArrayOfObject.length; ++i)
      arrayOfObject[i] = paramArrayOfObject[i];
    while (i < paramInt)
    {
      arrayOfObject[i] = Undefined.instance;
      ++i;
    }
    return arrayOfObject;
  }

  public static String escapeString(String paramString)
  {
    return escapeString(paramString, '"');
  }

  public static String escapeString(String paramString, char paramChar)
  {
    if ((paramChar != '"') && (paramChar != '\''))
      Kit.codeBug();
    StringBuffer localStringBuffer = null;
    int i = 0;
    int j = paramString.length();
    while (i != j)
    {
      char c1 = paramString.charAt(i);
      if ((' ' <= c1) && (c1 <= '~') && (c1 != paramChar) && (c1 != '\\'))
      {
        if (localStringBuffer != null)
          localStringBuffer.append((char)c1);
      }
      else
      {
        if (localStringBuffer == null)
        {
          localStringBuffer = new StringBuffer(j + 3);
          localStringBuffer.append(paramString);
          localStringBuffer.setLength(i);
        }
        int k = -1;
        switch (c1)
        {
        case '\b':
          k = 98;
          break;
        case '\f':
          k = 102;
          break;
        case '\n':
          k = 110;
          break;
        case '\r':
          k = 114;
          break;
        case '\t':
          k = 116;
          break;
        case '\11':
          k = 118;
          break;
        case ' ':
          k = 32;
          break;
        case '\\':
          k = 92;
        }
        if (k >= 0)
        {
          localStringBuffer.append('\\');
          localStringBuffer.append((char)k);
        }
        else if (c1 == paramChar)
        {
          localStringBuffer.append('\\');
          localStringBuffer.append(paramChar);
        }
        else
        {
          int l;
          if (c1 < 256)
          {
            localStringBuffer.append("\\x");
            l = 2;
          }
          else
          {
            localStringBuffer.append("\\u");
            l = 4;
          }
          for (char c2 = (l - 1) * 4; c2 >= 0; c2 -= 4)
          {
            int i1 = 0xF & c1 >> c2;
            int i2 = (i1 < 10) ? 48 + i1 : 87 + i1;
            localStringBuffer.append((char)i2);
          }
        }
      }
      ++i;
    }
    return ((localStringBuffer == null) ? paramString : localStringBuffer.toString());
  }

  static boolean isValidIdentifierName(String paramString)
  {
    int i = paramString.length();
    if (i == 0)
      return false;
    if (!(Character.isJavaIdentifierStart(paramString.charAt(0))))
      return false;
    for (int j = 1; j != i; ++j)
      if (!(Character.isJavaIdentifierPart(paramString.charAt(j))))
        return false;
    return (!(TokenStream.isKeyword(paramString)));
  }

  public static String toString(Object paramObject)
  {
    do
    {
      if (paramObject == null)
        return "null";
      if (paramObject == Undefined.instance)
        return "undefined";
      if (paramObject instanceof String)
        return ((String)paramObject);
      if (paramObject instanceof Number)
        return numberToString(((Number)paramObject).doubleValue(), 10);
      if (!(paramObject instanceof Scriptable))
        break label83;
      paramObject = ((Scriptable)paramObject).getDefaultValue(StringClass);
    }
    while (!(paramObject instanceof Scriptable));
    throw errorWithClassName("msg.primitive.expected", paramObject);
    label83: return paramObject.toString();
  }

  static String defaultObjectToString(Scriptable paramScriptable)
  {
    return "[object " + paramScriptable.getClassName() + ']';
  }

  public static String toString(Object[] paramArrayOfObject, int paramInt)
  {
    return ((paramInt < paramArrayOfObject.length) ? toString(paramArrayOfObject[paramInt]) : "undefined");
  }

  public static String toString(double paramDouble)
  {
    return numberToString(paramDouble, 10);
  }

  public static String numberToString(double paramDouble, int paramInt)
  {
    if (paramDouble != paramDouble)
      return "NaN";
    if (paramDouble == (1.0D / 0.0D))
      return "Infinity";
    if (paramDouble == (-1.0D / 0.0D))
      return "-Infinity";
    if (paramDouble == 0D)
      return "0";
    if ((paramInt < 2) || (paramInt > 36))
      throw Context.reportRuntimeError1("msg.bad.radix", Integer.toString(paramInt));
    if (paramInt != 10)
      return DToA.JS_dtobasestr(paramInt, paramDouble);
    StringBuffer localStringBuffer = new StringBuffer();
    DToA.JS_dtostr(localStringBuffer, 0, 0, paramDouble);
    return localStringBuffer.toString();
  }

  static String uneval(Context paramContext, Scriptable paramScriptable, Object paramObject)
  {
    Object localObject;
    if (paramObject == null)
      return "null";
    if (paramObject == Undefined.instance)
      return "undefined";
    if (paramObject instanceof String)
    {
      String str = escapeString((String)paramObject);
      localObject = new StringBuffer(str.length() + 2);
      ((StringBuffer)localObject).append('"');
      ((StringBuffer)localObject).append(str);
      ((StringBuffer)localObject).append('"');
      return ((StringBuffer)localObject).toString();
    }
    if (paramObject instanceof Number)
    {
      double d = ((Number)paramObject).doubleValue();
      if ((d == 0D) && (1D / d < 0D))
        return "-0";
      return toString(d);
    }
    if (paramObject instanceof Boolean)
      return toString(paramObject);
    if (paramObject instanceof Scriptable)
    {
      Scriptable localScriptable = (Scriptable)paramObject;
      localObject = ScriptableObject.getProperty(localScriptable, "toSource");
      if (localObject instanceof Function)
      {
        Function localFunction = (Function)localObject;
        return toString(localFunction.call(paramContext, paramScriptable, localScriptable, emptyArgs));
      }
      return toString(paramObject);
    }
    warnAboutNonJSObject(paramObject);
    return ((String)paramObject.toString());
  }

  static String defaultObjectToSource(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    int i;
    boolean bool;
    if (paramContext.iterating == null)
    {
      i = 1;
      bool = false;
      paramContext.iterating = new ObjToIntMap(31);
    }
    else
    {
      i = 0;
      bool = paramContext.iterating.has(paramScriptable2);
    }
    StringBuffer localStringBuffer = new StringBuffer(128);
    if (i != 0)
      localStringBuffer.append("(");
    localStringBuffer.append('{');
    try
    {
      if (!(bool))
      {
        paramContext.iterating.intern(paramScriptable2);
        Object[] arrayOfObject = paramScriptable2.getIds();
        for (int j = 0; j < arrayOfObject.length; ++j)
        {
          Object localObject2;
          if (j > 0)
            localStringBuffer.append(", ");
          Object localObject1 = arrayOfObject[j];
          if (localObject1 instanceof Integer)
          {
            int k = ((Integer)localObject1).intValue();
            localObject2 = paramScriptable2.get(k, paramScriptable2);
            localStringBuffer.append(k);
          }
          else
          {
            String str = (String)localObject1;
            localObject2 = paramScriptable2.get(str, paramScriptable2);
            if (isValidIdentifierName(str))
            {
              localStringBuffer.append(str);
            }
            else
            {
              localStringBuffer.append('\'');
              localStringBuffer.append(escapeString(str, '\''));
              localStringBuffer.append('\'');
            }
          }
          localStringBuffer.append(':');
          localStringBuffer.append(uneval(paramContext, paramScriptable1, localObject2));
        }
      }
    }
    finally
    {
      if (i != 0)
        paramContext.iterating = null;
    }
    localStringBuffer.append('}');
    if (i != 0)
      localStringBuffer.append(')');
    return localStringBuffer.toString();
  }

  public static Scriptable toObject(Scriptable paramScriptable, Object paramObject)
  {
    if (paramObject instanceof Scriptable)
      return ((Scriptable)paramObject);
    return toObject(Context.getContext(), paramScriptable, paramObject);
  }

  public static Scriptable toObjectOrNull(Context paramContext, Object paramObject)
  {
    if (paramObject instanceof Scriptable)
      return ((Scriptable)paramObject);
    if ((paramObject != null) && (paramObject != Undefined.instance))
      return toObject(paramContext, getTopCallScope(paramContext), paramObject);
    return null;
  }

  /**
   * @deprecated
   */
  public static Scriptable toObject(Scriptable paramScriptable, Object paramObject, Class paramClass)
  {
    if (paramObject instanceof Scriptable)
      return ((Scriptable)paramObject);
    return toObject(Context.getContext(), paramScriptable, paramObject);
  }

  public static Scriptable toObject(Context paramContext, Scriptable paramScriptable, Object paramObject)
  {
    if (paramObject instanceof Scriptable)
      return ((Scriptable)paramObject);
    if (paramObject == null)
      throw typeError0("msg.null.to.object");
    if (paramObject == Undefined.instance)
      throw typeError0("msg.undef.to.object");
    String str = (paramObject instanceof Boolean) ? "Boolean" : (paramObject instanceof Number) ? "Number" : (paramObject instanceof String) ? "String" : null;
    if (str != null)
    {
      localObject = { paramObject };
      paramScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
      return newObject(paramContext, paramScriptable, str, localObject);
    }
    Object localObject = paramContext.getWrapFactory().wrap(paramContext, paramScriptable, paramObject, null);
    if (localObject instanceof Scriptable)
      return ((Scriptable)localObject);
    throw errorWithClassName("msg.invalid.type", paramObject);
  }

  /**
   * @deprecated
   */
  public static Scriptable toObject(Context paramContext, Scriptable paramScriptable, Object paramObject, Class paramClass)
  {
    return toObject(paramContext, paramScriptable, paramObject);
  }

  /**
   * @deprecated
   */
  public static Object call(Context paramContext, Object paramObject1, Object paramObject2, Object[] paramArrayOfObject, Scriptable paramScriptable)
  {
    if (!(paramObject1 instanceof Function))
      throw notFunctionError(toString(paramObject1));
    Function localFunction = (Function)paramObject1;
    Scriptable localScriptable = toObjectOrNull(paramContext, paramObject2);
    if (localScriptable == null)
      throw undefCallError(localScriptable, "function");
    return localFunction.call(paramContext, paramScriptable, localScriptable, paramArrayOfObject);
  }

  public static Scriptable newObject(Context paramContext, Scriptable paramScriptable, String paramString, Object[] paramArrayOfObject)
  {
    paramScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
    Function localFunction = getExistingCtor(paramContext, paramScriptable, paramString);
    if (paramArrayOfObject == null)
      paramArrayOfObject = emptyArgs;
    return localFunction.construct(paramContext, paramScriptable, paramArrayOfObject);
  }

  public static double toInteger(Object paramObject)
  {
    return toInteger(toNumber(paramObject));
  }

  public static double toInteger(double paramDouble)
  {
    if (paramDouble != paramDouble)
      return 0D;
    if ((paramDouble == 0D) || (paramDouble == (1.0D / 0.0D)) || (paramDouble == (-1.0D / 0.0D)))
      return paramDouble;
    if (paramDouble > 0D)
      return Math.floor(paramDouble);
    return Math.ceil(paramDouble);
  }

  public static double toInteger(Object[] paramArrayOfObject, int paramInt)
  {
    return ((paramInt < paramArrayOfObject.length) ? toInteger(paramArrayOfObject[paramInt]) : 0D);
  }

  public static int toInt32(Object paramObject)
  {
    if (paramObject instanceof Integer)
      return ((Integer)paramObject).intValue();
    return toInt32(toNumber(paramObject));
  }

  public static int toInt32(Object[] paramArrayOfObject, int paramInt)
  {
    return ((paramInt < paramArrayOfObject.length) ? toInt32(paramArrayOfObject[paramInt]) : 0);
  }

  public static int toInt32(double paramDouble)
  {
    int i = (int)paramDouble;
    if (i == paramDouble)
      return i;
    if ((paramDouble != paramDouble) || (paramDouble == (1.0D / 0.0D)) || (paramDouble == (-1.0D / 0.0D)))
      return 0;
    paramDouble = (paramDouble >= 0D) ? Math.floor(paramDouble) : Math.ceil(paramDouble);
    double d = 4294967296.0D;
    paramDouble = Math.IEEEremainder(paramDouble, d);
    long l = ()paramDouble;
    return (int)l;
  }

  public static long toUint32(double paramDouble)
  {
    long l = ()paramDouble;
    if (l == paramDouble)
      return (l & 0xFFFFFFFF);
    if ((paramDouble != paramDouble) || (paramDouble == (1.0D / 0.0D)) || (paramDouble == (-1.0D / 0.0D)))
      return 3412047463052214272L;
    paramDouble = (paramDouble >= 0D) ? Math.floor(paramDouble) : Math.ceil(paramDouble);
    double d = 4294967296.0D;
    l = ()Math.IEEEremainder(paramDouble, d);
    return (l & 0xFFFFFFFF);
  }

  public static long toUint32(Object paramObject)
  {
    return toUint32(toNumber(paramObject));
  }

  public static char toUint16(Object paramObject)
  {
    double d = toNumber(paramObject);
    int i = (int)d;
    if (i == d)
      return (char)i;
    if ((d != d) || (d == (1.0D / 0.0D)) || (d == (-1.0D / 0.0D)))
      return ';
    d = (d >= 0D) ? Math.floor(d) : Math.ceil(d);
    int j = 65536;
    i = (int)Math.IEEEremainder(d, j);
    return (char)i;
  }

  public static Object setDefaultNamespace(Object paramObject, Context paramContext)
  {
    Object localObject1 = paramContext.currentActivationCall;
    if (localObject1 == null)
      localObject1 = getTopCallScope(paramContext);
    XMLLib localXMLLib = currentXMLLib(paramContext);
    Object localObject2 = localXMLLib.toDefaultXmlNamespace(paramContext, paramObject);
    if (!(((Scriptable)localObject1).has("__default_namespace__", (Scriptable)localObject1)))
      ScriptableObject.defineProperty((Scriptable)localObject1, "__default_namespace__", localObject2, 6);
    else
      ((Scriptable)localObject1).put("__default_namespace__", (Scriptable)localObject1, localObject2);
    return Undefined.instance;
  }

  public static Object searchDefaultNamespace(Context paramContext)
  {
    Object localObject2;
    Object localObject1 = paramContext.currentActivationCall;
    if (localObject1 == null)
      localObject1 = getTopCallScope(paramContext);
    while (true)
    {
      Scriptable localScriptable = ((Scriptable)localObject1).getParentScope();
      if (localScriptable == null)
      {
        localObject2 = ScriptableObject.getProperty((Scriptable)localObject1, "__default_namespace__");
        if (localObject2 != Scriptable.NOT_FOUND)
          break;
        return null;
      }
      localObject2 = ((Scriptable)localObject1).get("__default_namespace__", (Scriptable)localObject1);
      if (localObject2 != Scriptable.NOT_FOUND)
        break;
      localObject1 = localScriptable;
    }
    return localObject2;
  }

  public static Object getTopLevelProp(Scriptable paramScriptable, String paramString)
  {
    paramScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
    return ScriptableObject.getProperty(paramScriptable, paramString);
  }

  static Function getExistingCtor(Context paramContext, Scriptable paramScriptable, String paramString)
  {
    Object localObject = ScriptableObject.getProperty(paramScriptable, paramString);
    if (localObject instanceof Function)
      return ((Function)localObject);
    if (localObject == Scriptable.NOT_FOUND)
      throw Context.reportRuntimeError1("msg.ctor.not.found", paramString);
    throw Context.reportRuntimeError1("msg.not.ctor", paramString);
  }

  private static long indexFromString(String paramString)
  {
    int i = paramString.length();
    if (i > 0)
    {
      int j = 0;
      int k = 0;
      int l = paramString.charAt(0);
      if ((l == 45) && (i > 1))
      {
        l = paramString.charAt(1);
        j = 1;
        k = 1;
      }
      if ((0 <= (l -= 48)) && (l <= 9))
        if (i <= ((k != 0) ? 11 : 10))
        {
          int i1 = -l;
          int i2 = 0;
          ++j;
          while ((i1 != 0) && (j != i))
          {
            if ((0 > (l = paramString.charAt(j) - '0')) || (l > 9))
              break;
            i2 = i1;
            i1 = 10 * i1 - l;
            ++j;
          }
          if (j == i)
          {
            if (i2 <= -214748364)
            {
              if (i2 != -214748364)
                break label195;
              if (l > ((k != 0) ? 8 : 7))
                break label195;
            }
            return (0xFFFFFFFF & ((k != 0) ? i1 : -i1));
          }
        }
    }
    label195: return -1L;
  }

  public static long testUint32String(String paramString)
  {
    int i = paramString.length();
    if ((1 <= i) && (i <= 10))
    {
      int j = paramString.charAt(0);
      if ((j -= 48) == 0)
        return ((i == 1) ? 3412039714931212288L : -1L);
      if ((1 <= j) && (j <= 9))
      {
        long l = j;
        for (int k = 1; k != i; ++k)
        {
          j = paramString.charAt(k) - '0';
          if ((0 > j) || (j > 9))
            return -1L;
          l = 10L * l + j;
        }
        if (l >>> 32 == 3412047669210644480L)
          return l;
      }
    }
    return -1L;
  }

  static Object getIndexObject(String paramString)
  {
    long l = indexFromString(paramString);
    if (l >= 3412046810217185280L)
      return new Integer((int)l);
    return paramString;
  }

  static Object getIndexObject(double paramDouble)
  {
    int i = (int)paramDouble;
    if (i == paramDouble)
      return new Integer(i);
    return toString(paramDouble);
  }

  static String toStringIdOrIndex(Context paramContext, Object paramObject)
  {
    String str;
    if (paramObject instanceof Number)
    {
      double d = ((Number)paramObject).doubleValue();
      int i = (int)d;
      if (i == d)
      {
        storeIndexResult(paramContext, i);
        return null;
      }
      return toString(paramObject);
    }
    if (paramObject instanceof String)
      str = (String)paramObject;
    else
      str = toString(paramObject);
    long l = indexFromString(str);
    if (l >= 3412046810217185280L)
    {
      storeIndexResult(paramContext, (int)l);
      return null;
    }
    return str;
  }

  public static Object getObjectElem(Object paramObject1, Object paramObject2, Context paramContext)
  {
    Scriptable localScriptable = toObjectOrNull(paramContext, paramObject1);
    if (localScriptable == null)
      throw undefReadError(paramObject1, paramObject2);
    return getObjectElem(localScriptable, paramObject2, paramContext);
  }

  public static Object getObjectElem(Scriptable paramScriptable, Object paramObject, Context paramContext)
  {
    Object localObject;
    if (paramScriptable instanceof XMLObject)
    {
      localObject = (XMLObject)paramScriptable;
      return ((XMLObject)localObject).ecmaGet(paramContext, paramObject);
    }
    String str = toStringIdOrIndex(paramContext, paramObject);
    if (str == null)
    {
      int i = lastIndexResult(paramContext);
      localObject = ScriptableObject.getProperty(paramScriptable, i);
    }
    else
    {
      localObject = ScriptableObject.getProperty(paramScriptable, str);
    }
    if (localObject == Scriptable.NOT_FOUND)
      localObject = Undefined.instance;
    return localObject;
  }

  public static Object getObjectProp(Object paramObject, String paramString, Context paramContext)
  {
    Scriptable localScriptable = toObjectOrNull(paramContext, paramObject);
    if (localScriptable == null)
      throw undefReadError(paramObject, paramString);
    return getObjectProp(localScriptable, paramString, paramContext);
  }

  public static Object getObjectProp(Scriptable paramScriptable, String paramString, Context paramContext)
  {
    if (paramScriptable instanceof XMLObject)
    {
      localObject = (XMLObject)paramScriptable;
      return ((XMLObject)localObject).ecmaGet(paramContext, paramString);
    }
    Object localObject = ScriptableObject.getProperty(paramScriptable, paramString);
    if (localObject == Scriptable.NOT_FOUND)
      localObject = Undefined.instance;
    return localObject;
  }

  public static Object getObjectIndex(Object paramObject, double paramDouble, Context paramContext)
  {
    Scriptable localScriptable = toObjectOrNull(paramContext, paramObject);
    if (localScriptable == null)
      throw undefReadError(paramObject, toString(paramDouble));
    int i = (int)paramDouble;
    if (i == paramDouble)
      return getObjectIndex(localScriptable, i, paramContext);
    String str = toString(paramDouble);
    return getObjectProp(localScriptable, str, paramContext);
  }

  public static Object getObjectIndex(Scriptable paramScriptable, int paramInt, Context paramContext)
  {
    if (paramScriptable instanceof XMLObject)
    {
      localObject = (XMLObject)paramScriptable;
      return ((XMLObject)localObject).ecmaGet(paramContext, new Integer(paramInt));
    }
    Object localObject = ScriptableObject.getProperty(paramScriptable, paramInt);
    if (localObject == Scriptable.NOT_FOUND)
      localObject = Undefined.instance;
    return localObject;
  }

  public static Object setObjectElem(Object paramObject1, Object paramObject2, Object paramObject3, Context paramContext)
  {
    Scriptable localScriptable = toObjectOrNull(paramContext, paramObject1);
    if (localScriptable == null)
      throw undefWriteError(paramObject1, paramObject2, paramObject3);
    return setObjectElem(localScriptable, paramObject2, paramObject3, paramContext);
  }

  public static Object setObjectElem(Scriptable paramScriptable, Object paramObject1, Object paramObject2, Context paramContext)
  {
    if (paramScriptable instanceof XMLObject)
    {
      localObject = (XMLObject)paramScriptable;
      ((XMLObject)localObject).ecmaPut(paramContext, paramObject1, paramObject2);
      return paramObject2;
    }
    Object localObject = toStringIdOrIndex(paramContext, paramObject1);
    if (localObject == null)
    {
      int i = lastIndexResult(paramContext);
      ScriptableObject.putProperty(paramScriptable, i, paramObject2);
    }
    else
    {
      ScriptableObject.putProperty(paramScriptable, (String)localObject, paramObject2);
    }
    return paramObject2;
  }

  public static Object setObjectProp(Object paramObject1, String paramString, Object paramObject2, Context paramContext)
  {
    Scriptable localScriptable = toObjectOrNull(paramContext, paramObject1);
    if (localScriptable == null)
      throw undefWriteError(paramObject1, paramString, paramObject2);
    return setObjectProp(localScriptable, paramString, paramObject2, paramContext);
  }

  public static Object setObjectProp(Scriptable paramScriptable, String paramString, Object paramObject, Context paramContext)
  {
    if (paramScriptable instanceof XMLObject)
    {
      XMLObject localXMLObject = (XMLObject)paramScriptable;
      localXMLObject.ecmaPut(paramContext, paramString, paramObject);
    }
    else
    {
      ScriptableObject.putProperty(paramScriptable, paramString, paramObject);
    }
    return paramObject;
  }

  public static Object setObjectIndex(Object paramObject1, double paramDouble, Object paramObject2, Context paramContext)
  {
    Scriptable localScriptable = toObjectOrNull(paramContext, paramObject1);
    if (localScriptable == null)
      throw undefWriteError(paramObject1, String.valueOf(paramDouble), paramObject2);
    int i = (int)paramDouble;
    if (i == paramDouble)
      return setObjectIndex(localScriptable, i, paramObject2, paramContext);
    String str = toString(paramDouble);
    return setObjectProp(localScriptable, str, paramObject2, paramContext);
  }

  public static Object setObjectIndex(Scriptable paramScriptable, int paramInt, Object paramObject, Context paramContext)
  {
    if (paramScriptable instanceof XMLObject)
    {
      XMLObject localXMLObject = (XMLObject)paramScriptable;
      localXMLObject.ecmaPut(paramContext, new Integer(paramInt), paramObject);
    }
    else
    {
      ScriptableObject.putProperty(paramScriptable, paramInt, paramObject);
    }
    return paramObject;
  }

  public static boolean deleteObjectElem(Scriptable paramScriptable, Object paramObject, Context paramContext)
  {
    boolean bool;
    Object localObject;
    if (paramScriptable instanceof XMLObject)
    {
      localObject = (XMLObject)paramScriptable;
      bool = ((XMLObject)localObject).ecmaDelete(paramContext, paramObject);
    }
    else
    {
      localObject = toStringIdOrIndex(paramContext, paramObject);
      if (localObject == null)
      {
        int i = lastIndexResult(paramContext);
        bool = ScriptableObject.deleteProperty(paramScriptable, i);
      }
      else
      {
        bool = ScriptableObject.deleteProperty(paramScriptable, (String)localObject);
      }
    }
    return bool;
  }

  public static boolean hasObjectElem(Scriptable paramScriptable, Object paramObject, Context paramContext)
  {
    boolean bool;
    Object localObject;
    if (paramScriptable instanceof XMLObject)
    {
      localObject = (XMLObject)paramScriptable;
      bool = ((XMLObject)localObject).ecmaHas(paramContext, paramObject);
    }
    else
    {
      localObject = toStringIdOrIndex(paramContext, paramObject);
      if (localObject == null)
      {
        int i = lastIndexResult(paramContext);
        bool = ScriptableObject.hasProperty(paramScriptable, i);
      }
      else
      {
        bool = ScriptableObject.hasProperty(paramScriptable, (String)localObject);
      }
    }
    return bool;
  }

  public static Object refGet(Ref paramRef, Context paramContext)
  {
    return paramRef.get(paramContext);
  }

  public static Object refSet(Ref paramRef, Object paramObject, Context paramContext)
  {
    return paramRef.set(paramContext, paramObject);
  }

  public static Object refDel(Ref paramRef, Context paramContext)
  {
    return wrapBoolean(paramRef.delete(paramContext));
  }

  static boolean isSpecialProperty(String paramString)
  {
    return ((paramString.equals("__proto__")) || (paramString.equals("__parent__")));
  }

  public static Ref specialRef(Object paramObject, String paramString, Context paramContext)
  {
    return SpecialRef.createSpecial(paramContext, paramObject, paramString);
  }

  public static Object delete(Object paramObject1, Object paramObject2, Context paramContext)
  {
    Scriptable localScriptable = toObjectOrNull(paramContext, paramObject1);
    if (localScriptable == null)
    {
      String str = (paramObject2 == null) ? "null" : paramObject2.toString();
      throw typeError2("msg.undef.prop.delete", toString(paramObject1), str);
    }
    boolean bool = deleteObjectElem(localScriptable, paramObject2, paramContext);
    return wrapBoolean(bool);
  }

  public static Object name(Context paramContext, Scriptable paramScriptable, String paramString)
  {
    Scriptable localScriptable = paramScriptable.getParentScope();
    if (localScriptable == null)
    {
      Object localObject = topScopeName(paramContext, paramScriptable, paramString);
      if (localObject == Scriptable.NOT_FOUND)
        throw notFoundError(paramScriptable, paramString);
      return localObject;
    }
    return nameOrFunction(paramContext, paramScriptable, localScriptable, paramString, false);
  }

  private static Object nameOrFunction(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, String paramString, boolean paramBoolean)
  {
    Object localObject2 = paramScriptable1;
    Object localObject3 = null;
    do
    {
      if (paramScriptable1 instanceof NativeWith)
      {
        Scriptable localScriptable = paramScriptable1.getPrototype();
        if (localScriptable instanceof XMLObject)
        {
          XMLObject localXMLObject = (XMLObject)localScriptable;
          if (localXMLObject.ecmaHas(paramContext, paramString))
          {
            localObject2 = localXMLObject;
            localObject1 = localXMLObject.ecmaGet(paramContext, paramString);
            break label220:
          }
          if (localObject3 == null)
            localObject3 = localXMLObject;
        }
        else
        {
          localObject1 = ScriptableObject.getProperty(localScriptable, paramString);
          if (localObject1 != Scriptable.NOT_FOUND)
          {
            localObject2 = localScriptable;
            break label220:
          }
        }
      }
      else
      {
        if (paramScriptable1 instanceof NativeCall)
        {
          localObject1 = paramScriptable1.get(paramString, paramScriptable1);
          if (localObject1 == Scriptable.NOT_FOUND)
            break label160;
          if (!(paramBoolean))
            break label220;
          localObject2 = ScriptableObject.getTopLevelScope(paramScriptable2);
          break label220:
        }
        localObject1 = ScriptableObject.getProperty(paramScriptable1, paramString);
        if (localObject1 != Scriptable.NOT_FOUND)
        {
          localObject2 = paramScriptable1;
          break label220:
        }
      }
      label160: paramScriptable1 = paramScriptable2;
      paramScriptable2 = paramScriptable2.getParentScope();
    }
    while (paramScriptable2 != null);
    Object localObject1 = topScopeName(paramContext, paramScriptable1, paramString);
    if (localObject1 == Scriptable.NOT_FOUND)
    {
      if ((localObject3 == null) || (paramBoolean))
        throw notFoundError(paramScriptable1, paramString);
      localObject1 = localObject3.ecmaGet(paramContext, paramString);
    }
    localObject2 = paramScriptable1;
    if (paramBoolean)
    {
      if (!(localObject1 instanceof Callable))
        label220: throw notFunctionError(localObject1, paramString);
      storeScriptable(paramContext, (Scriptable)localObject2);
    }
    return localObject1;
  }

  private static Object topScopeName(Context paramContext, Scriptable paramScriptable, String paramString)
  {
    if (paramContext.useDynamicScope)
      paramScriptable = checkDynamicScope(paramContext.topCallScope, paramScriptable);
    return ScriptableObject.getProperty(paramScriptable, paramString);
  }

  public static Scriptable bind(Context paramContext, Scriptable paramScriptable, String paramString)
  {
    Object localObject = null;
    Scriptable localScriptable1 = paramScriptable.getParentScope();
    if (localScriptable1 != null)
    {
      while (paramScriptable instanceof NativeWith)
      {
        Scriptable localScriptable2 = paramScriptable.getPrototype();
        if (localScriptable2 instanceof XMLObject)
        {
          XMLObject localXMLObject = (XMLObject)localScriptable2;
          if (localXMLObject.ecmaHas(paramContext, paramString))
            return localXMLObject;
          if (localObject == null)
            localObject = localXMLObject;
        }
        else if (ScriptableObject.hasProperty(localScriptable2, paramString))
        {
          return localScriptable2;
        }
        paramScriptable = localScriptable1;
        localScriptable1 = localScriptable1.getParentScope();
        if (localScriptable1 == null)
          break label133:
      }
      do
      {
        if (ScriptableObject.hasProperty(paramScriptable, paramString))
          return paramScriptable;
        paramScriptable = localScriptable1;
        localScriptable1 = localScriptable1.getParentScope();
      }
      while (localScriptable1 != null);
    }
    if (paramContext.useDynamicScope)
      label133: paramScriptable = checkDynamicScope(paramContext.topCallScope, paramScriptable);
    if (ScriptableObject.hasProperty(paramScriptable, paramString))
      return paramScriptable;
    return localObject;
  }

  public static Object setName(Scriptable paramScriptable1, Object paramObject, Context paramContext, Scriptable paramScriptable2, String paramString)
  {
    if (paramScriptable1 != null)
    {
      if (paramScriptable1 instanceof XMLObject)
      {
        XMLObject localXMLObject = (XMLObject)paramScriptable1;
        localXMLObject.ecmaPut(paramContext, paramString, paramObject);
      }
      else
      {
        ScriptableObject.putProperty(paramScriptable1, paramString, paramObject);
      }
    }
    else
    {
      if (paramContext.hasFeature(8))
        throw Context.reportRuntimeError1("msg.assn.create.strict", paramString);
      paramScriptable1 = ScriptableObject.getTopLevelScope(paramScriptable2);
      if (paramContext.useDynamicScope)
        paramScriptable1 = checkDynamicScope(paramContext.topCallScope, paramScriptable1);
      paramScriptable1.put(paramString, paramScriptable1, paramObject);
    }
    return paramObject;
  }

  public static Object enumInit(Object paramObject, Context paramContext, boolean paramBoolean)
  {
    IdEnumeration localIdEnumeration = new IdEnumeration(null);
    localIdEnumeration.obj = toObjectOrNull(paramContext, paramObject);
    if (localIdEnumeration.obj != null)
    {
      localIdEnumeration.enumValues = paramBoolean;
      enumChangeObject(localIdEnumeration);
    }
    return localIdEnumeration;
  }

  public static Boolean enumNext(Object paramObject)
  {
    label130: int i;
    IdEnumeration localIdEnumeration = (IdEnumeration)paramObject;
    while (true)
    {
      Object localObject;
      String str;
      while (true)
      {
        while (true)
        {
          while (true)
          {
            if (localIdEnumeration.obj == null)
            {
              bool = false;
              break label174:
            }
            if (localIdEnumeration.index != localIdEnumeration.ids.length)
              break;
            localIdEnumeration.obj = localIdEnumeration.obj.getPrototype();
            enumChangeObject(localIdEnumeration);
          }
          localObject = localIdEnumeration.ids[(localIdEnumeration.index++)];
          if ((localIdEnumeration.used == null) || (!(localIdEnumeration.used.has(localObject))))
            break;
        }
        if (!(localObject instanceof String))
          break label130;
        str = (String)localObject;
        if (localIdEnumeration.obj.has(str, localIdEnumeration.obj))
          break;
      }
      localIdEnumeration.currentId = str;
      break label169:
      i = ((Number)localObject).intValue();
      if (localIdEnumeration.obj.has(i, localIdEnumeration.obj))
        break;
    }
    localIdEnumeration.currentId = String.valueOf(i);
    label169: boolean bool = true;
    label174: return wrapBoolean(bool);
  }

  public static Object enumId(Object paramObject, Context paramContext)
  {
    Object localObject;
    IdEnumeration localIdEnumeration = (IdEnumeration)paramObject;
    if (!(localIdEnumeration.enumValues))
      return localIdEnumeration.currentId;
    String str = toStringIdOrIndex(paramContext, localIdEnumeration.currentId);
    if (str == null)
    {
      int i = lastIndexResult(paramContext);
      localObject = localIdEnumeration.obj.get(i, localIdEnumeration.obj);
    }
    else
    {
      localObject = localIdEnumeration.obj.get(str, localIdEnumeration.obj);
    }
    return localObject;
  }

  private static void enumChangeObject(IdEnumeration paramIdEnumeration)
  {
    Object[] arrayOfObject1 = null;
    while (paramIdEnumeration.obj != null)
    {
      arrayOfObject1 = paramIdEnumeration.obj.getIds();
      if (arrayOfObject1.length != 0)
        break;
      paramIdEnumeration.obj = paramIdEnumeration.obj.getPrototype();
    }
    if ((paramIdEnumeration.obj != null) && (paramIdEnumeration.ids != null))
    {
      Object[] arrayOfObject2 = paramIdEnumeration.ids;
      int i = arrayOfObject2.length;
      if (paramIdEnumeration.used == null)
        paramIdEnumeration.used = new ObjToIntMap(i);
      for (int j = 0; j != i; ++j)
        paramIdEnumeration.used.intern(arrayOfObject2[j]);
    }
    paramIdEnumeration.ids = arrayOfObject1;
    paramIdEnumeration.index = 0;
  }

  public static Callable getNameFunctionAndThis(String paramString, Context paramContext, Scriptable paramScriptable)
  {
    Scriptable localScriptable1 = paramScriptable.getParentScope();
    if (localScriptable1 == null)
    {
      Object localObject = topScopeName(paramContext, paramScriptable, paramString);
      if (!(localObject instanceof Callable))
      {
        if (localObject == Scriptable.NOT_FOUND)
          throw notFoundError(paramScriptable, paramString);
        throw notFunctionError(localObject, paramString);
      }
      Scriptable localScriptable2 = paramScriptable;
      storeScriptable(paramContext, localScriptable2);
      return ((Callable)localObject);
    }
    return ((Callable)nameOrFunction(paramContext, paramScriptable, localScriptable1, paramString, true));
  }

  public static Callable getElemFunctionAndThis(Object paramObject1, Object paramObject2, Context paramContext)
  {
    Object localObject2;
    String str = toStringIdOrIndex(paramContext, paramObject2);
    if (str != null)
      return getPropFunctionAndThis(paramObject1, str, paramContext);
    int i = lastIndexResult(paramContext);
    Object localObject1 = toObjectOrNull(paramContext, paramObject1);
    if (localObject1 == null)
      throw undefCallError(paramObject1, String.valueOf(i));
    while (true)
    {
      localObject2 = ScriptableObject.getProperty((Scriptable)localObject1, i);
      if (localObject2 != Scriptable.NOT_FOUND)
        break;
      if (!(localObject1 instanceof XMLObject))
        break;
      XMLObject localXMLObject = (XMLObject)localObject1;
      Scriptable localScriptable = localXMLObject.getExtraMethodSource(paramContext);
      if (localScriptable == null)
        break;
      localObject1 = localScriptable;
    }
    if (!(localObject2 instanceof Callable))
      throw notFunctionError(localObject2, paramObject2);
    storeScriptable(paramContext, (Scriptable)localObject1);
    return ((Callable)(Callable)localObject2);
  }

  public static Callable getPropFunctionAndThis(Object paramObject, String paramString, Context paramContext)
  {
    Object localObject2;
    Object localObject1 = toObjectOrNull(paramContext, paramObject);
    if (localObject1 == null)
      throw undefCallError(paramObject, paramString);
    while (true)
    {
      localObject2 = ScriptableObject.getProperty((Scriptable)localObject1, paramString);
      if (localObject2 != Scriptable.NOT_FOUND)
        break;
      if (!(localObject1 instanceof XMLObject))
        break;
      XMLObject localXMLObject = (XMLObject)localObject1;
      Scriptable localScriptable = localXMLObject.getExtraMethodSource(paramContext);
      if (localScriptable == null)
        break;
      localObject1 = localScriptable;
    }
    if (!(localObject2 instanceof Callable))
      throw notFunctionError(localObject2, paramString);
    storeScriptable(paramContext, (Scriptable)localObject1);
    return ((Callable)(Callable)localObject2);
  }

  public static Callable getValueFunctionAndThis(Object paramObject, Context paramContext)
  {
    Scriptable localScriptable;
    if (!(paramObject instanceof Callable))
      throw notFunctionError(paramObject);
    Callable localCallable = (Callable)paramObject;
    if (localCallable instanceof Scriptable)
    {
      localScriptable = ((Scriptable)localCallable).getParentScope();
    }
    else
    {
      if (paramContext.topCallScope == null)
        throw new IllegalStateException();
      localScriptable = paramContext.topCallScope;
    }
    if (localScriptable.getParentScope() != null)
    {
      if (localScriptable instanceof NativeWith)
        break label88:
      if (localScriptable instanceof NativeCall)
        localScriptable = ScriptableObject.getTopLevelScope(localScriptable);
    }
    label88: storeScriptable(paramContext, localScriptable);
    return localCallable;
  }

  public static Ref callRef(Callable paramCallable, Scriptable paramScriptable, Object[] paramArrayOfObject, Context paramContext)
  {
    if (paramCallable instanceof RefCallable)
    {
      localObject = (RefCallable)paramCallable;
      Ref localRef = ((RefCallable)localObject).refCall(paramContext, paramScriptable, paramArrayOfObject);
      if (localRef == null)
        throw new IllegalStateException(localObject.getClass().getName() + ".refCall() returned null");
      return localRef;
    }
    Object localObject = getMessage1("msg.no.ref.from.function", toString(paramCallable));
    throw constructError("ReferenceError", (String)localObject);
  }

  public static Scriptable newObject(Object paramObject, Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    if (!(paramObject instanceof Function))
      throw notFunctionError(paramObject);
    Function localFunction = (Function)paramObject;
    return localFunction.construct(paramContext, paramScriptable, paramArrayOfObject);
  }

  public static Object callSpecial(Context paramContext, Callable paramCallable, Scriptable paramScriptable1, Object[] paramArrayOfObject, Scriptable paramScriptable2, Scriptable paramScriptable3, int paramInt1, String paramString, int paramInt2)
  {
    if (paramInt1 == 1)
    {
      if (!(NativeGlobal.isEvalFunction(paramCallable)))
        break label54;
      return evalSpecial(paramContext, paramScriptable2, paramScriptable3, paramArrayOfObject, paramString, paramInt2);
    }
    if (paramInt1 == 2)
    {
      if (!(NativeWith.isWithFunction(paramCallable)))
        break label54;
      throw Context.reportRuntimeError1("msg.only.from.new", "With");
    }
    throw Kit.codeBug();
    label54: return paramCallable.call(paramContext, paramScriptable2, paramScriptable1, paramArrayOfObject);
  }

  public static Object newSpecial(Context paramContext, Object paramObject, Object[] paramArrayOfObject, Scriptable paramScriptable, int paramInt)
  {
    if (paramInt == 1)
    {
      if (!(NativeGlobal.isEvalFunction(paramObject)))
        break label46;
      throw typeError1("msg.not.ctor", "eval");
    }
    if (paramInt == 2)
    {
      if (!(NativeWith.isWithFunction(paramObject)))
        break label46;
      return NativeWith.newWithSpecial(paramContext, paramScriptable, paramArrayOfObject);
    }
    throw Kit.codeBug();
    label46: return newObject(paramObject, paramContext, paramScriptable, paramArrayOfObject);
  }

  public static Object applyOrCall(boolean paramBoolean, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    Callable localCallable;
    Object[] arrayOfObject;
    int i = paramArrayOfObject.length;
    if (paramScriptable2 instanceof Callable)
    {
      localCallable = (Callable)paramScriptable2;
    }
    else
    {
      localObject1 = paramScriptable2.getDefaultValue(FunctionClass);
      if (!(localObject1 instanceof Callable))
        throw notFunctionError(localObject1, paramScriptable2);
      localCallable = (Callable)localObject1;
    }
    Object localObject1 = null;
    if (i != 0)
      localObject1 = toObjectOrNull(paramContext, paramArrayOfObject[0]);
    if (localObject1 == null)
      localObject1 = getTopCallScope(paramContext);
    if (paramBoolean)
    {
      if (i <= 1)
      {
        arrayOfObject = emptyArgs;
      }
      else
      {
        Object localObject2 = paramArrayOfObject[1];
        if ((localObject2 == null) || (localObject2 == Undefined.instance))
          arrayOfObject = emptyArgs;
        else if ((localObject2 instanceof NativeArray) || (localObject2 instanceof Arguments))
          arrayOfObject = paramContext.getElements((Scriptable)localObject2);
        else
          throw typeError0("msg.arg.isnt.array");
      }
    }
    else if (i <= 1)
    {
      arrayOfObject = emptyArgs;
    }
    else
    {
      arrayOfObject = new Object[i - 1];
      System.arraycopy(paramArrayOfObject, 1, arrayOfObject, 0, i - 1);
    }
    return localCallable.call(paramContext, paramScriptable1, (Scriptable)localObject1, arrayOfObject);
  }

  public static Object evalSpecial(Context paramContext, Scriptable paramScriptable, Object paramObject, Object[] paramArrayOfObject, String paramString, int paramInt)
  {
    if (paramArrayOfObject.length < 1)
      return Undefined.instance;
    Object localObject1 = paramArrayOfObject[0];
    if (!(localObject1 instanceof String))
    {
      if (paramContext.hasFeature(9))
        throw Context.reportRuntimeError0("msg.eval.nonstring.strict");
      localObject2 = getMessage0("msg.eval.nonstring");
      Context.reportWarning((String)localObject2);
      return localObject1;
    }
    if (paramString == null)
    {
      localObject2 = new int[1];
      paramString = Context.getSourcePositionFromStack(localObject2);
      if (paramString != null)
        paramInt = localObject2[0];
      else
        paramString = "";
    }
    Object localObject2 = makeUrlForGeneratedScript(true, paramString, paramInt);
    ErrorReporter localErrorReporter = DefaultErrorReporter.forEval(paramContext.getErrorReporter());
    Script localScript = paramContext.compileString((String)localObject1, new Interpreter(), localErrorReporter, (String)localObject2, 1, null);
    ((InterpretedFunction)localScript).idata.evalScriptFlag = true;
    Callable localCallable = (Callable)localScript;
    return localCallable.call(paramContext, paramScriptable, (Scriptable)paramObject, emptyArgs);
  }

  public static String typeof(Object paramObject)
  {
    if (paramObject == null)
      return "object";
    if (paramObject == Undefined.instance)
      return "undefined";
    if (paramObject instanceof Scriptable)
    {
      if (paramObject instanceof XMLObject)
        return "xml";
      return ((paramObject instanceof Callable) ? "function" : "object");
    }
    if (paramObject instanceof String)
      return "string";
    if (paramObject instanceof Number)
      return "number";
    if (paramObject instanceof Boolean)
      return "boolean";
    throw errorWithClassName("msg.invalid.type", paramObject);
  }

  public static String typeofName(Scriptable paramScriptable, String paramString)
  {
    Context localContext = Context.getContext();
    Scriptable localScriptable = bind(localContext, paramScriptable, paramString);
    if (localScriptable == null)
      return "undefined";
    return typeof(getObjectProp(localScriptable, paramString, localContext));
  }

  public static Object add(Object paramObject1, Object paramObject2, Context paramContext)
  {
    Object localObject;
    if ((paramObject1 instanceof Number) && (paramObject2 instanceof Number))
      return wrapNumber(((Number)paramObject1).doubleValue() + ((Number)paramObject2).doubleValue());
    if (paramObject1 instanceof XMLObject)
    {
      localObject = ((XMLObject)paramObject1).addValues(paramContext, true, paramObject2);
      if (localObject != Scriptable.NOT_FOUND)
        return localObject;
    }
    if (paramObject2 instanceof XMLObject)
    {
      localObject = ((XMLObject)paramObject2).addValues(paramContext, false, paramObject1);
      if (localObject != Scriptable.NOT_FOUND)
        return localObject;
    }
    if (paramObject1 instanceof Scriptable)
      paramObject1 = ((Scriptable)paramObject1).getDefaultValue(null);
    if (paramObject2 instanceof Scriptable)
      paramObject2 = ((Scriptable)paramObject2).getDefaultValue(null);
    if ((!(paramObject1 instanceof String)) && (!(paramObject2 instanceof String)))
    {
      if ((paramObject1 instanceof Number) && (paramObject2 instanceof Number))
        return wrapNumber(((Number)paramObject1).doubleValue() + ((Number)paramObject2).doubleValue());
      return wrapNumber(toNumber(paramObject1) + toNumber(paramObject2));
    }
    return toString(paramObject1).concat(toString(paramObject2));
  }

  public static Object nameIncrDecr(Scriptable paramScriptable, String paramString, int paramInt)
  {
    Scriptable localScriptable;
    Object localObject;
    do
    {
      localScriptable = paramScriptable;
      do
      {
        localObject = localScriptable.get(paramString, paramScriptable);
        if (localObject != Scriptable.NOT_FOUND)
          break label51:
        localScriptable = localScriptable.getPrototype();
      }
      while (localScriptable != null);
      paramScriptable = paramScriptable.getParentScope();
    }
    while (paramScriptable != null);
    throw notFoundError(paramScriptable, paramString);
    label51: return doScriptableIncrDecr(localScriptable, paramString, paramScriptable, localObject, paramInt);
  }

  public static Object propIncrDecr(Object paramObject, String paramString, Context paramContext, int paramInt)
  {
    Object localObject;
    Scriptable localScriptable1 = toObjectOrNull(paramContext, paramObject);
    if (localScriptable1 == null)
      throw undefReadError(paramObject, paramString);
    Scriptable localScriptable2 = localScriptable1;
    do
    {
      localObject = localScriptable2.get(paramString, localScriptable1);
      if (localObject != Scriptable.NOT_FOUND)
        break label76:
      localScriptable2 = localScriptable2.getPrototype();
    }
    while (localScriptable2 != null);
    localScriptable1.put(paramString, localScriptable1, NaNobj);
    return NaNobj;
    label76: return doScriptableIncrDecr(localScriptable2, paramString, localScriptable1, localObject, paramInt);
  }

  private static Object doScriptableIncrDecr(Scriptable paramScriptable1, String paramString, Scriptable paramScriptable2, Object paramObject, int paramInt)
  {
    double d;
    int i = ((paramInt & 0x2) != 0) ? 1 : 0;
    if (paramObject instanceof Number)
    {
      d = ((Number)paramObject).doubleValue();
    }
    else
    {
      d = toNumber(paramObject);
      if (i != 0)
        paramObject = wrapNumber(d);
    }
    if ((paramInt & 0x1) == 0)
      d += 1D;
    else
      d -= 1D;
    Number localNumber = wrapNumber(d);
    paramScriptable1.put(paramString, paramScriptable2, localNumber);
    if (i != 0)
      return paramObject;
    return localNumber;
  }

  public static Object elemIncrDecr(Object paramObject1, Object paramObject2, Context paramContext, int paramInt)
  {
    double d;
    Object localObject = getObjectElem(paramObject1, paramObject2, paramContext);
    int i = ((paramInt & 0x2) != 0) ? 1 : 0;
    if (localObject instanceof Number)
    {
      d = ((Number)localObject).doubleValue();
    }
    else
    {
      d = toNumber(localObject);
      if (i != 0)
        localObject = wrapNumber(d);
    }
    if ((paramInt & 0x1) == 0)
      d += 1D;
    else
      d -= 1D;
    Number localNumber = wrapNumber(d);
    setObjectElem(paramObject1, paramObject2, localNumber, paramContext);
    if (i != 0)
      return localObject;
    return localNumber;
  }

  public static Object refIncrDecr(Ref paramRef, Context paramContext, int paramInt)
  {
    double d;
    Object localObject = paramRef.get(paramContext);
    int i = ((paramInt & 0x2) != 0) ? 1 : 0;
    if (localObject instanceof Number)
    {
      d = ((Number)localObject).doubleValue();
    }
    else
    {
      d = toNumber(localObject);
      if (i != 0)
        localObject = wrapNumber(d);
    }
    if ((paramInt & 0x1) == 0)
      d += 1D;
    else
      d -= 1D;
    Number localNumber = wrapNumber(d);
    paramRef.set(paramContext, localNumber);
    if (i != 0)
      return localObject;
    return localNumber;
  }

  private static Object toPrimitive(Object paramObject)
  {
    if (!(paramObject instanceof Scriptable))
      return paramObject;
    Scriptable localScriptable = (Scriptable)paramObject;
    Object localObject = localScriptable.getDefaultValue(null);
    if (localObject instanceof Scriptable)
      throw typeError0("msg.bad.default.value");
    return localObject;
  }

  public static boolean eq(Object paramObject1, Object paramObject2)
  {
    if ((paramObject1 == null) || (paramObject1 == Undefined.instance))
    {
      if ((paramObject2 == null) || (paramObject2 == Undefined.instance))
        return true;
      if (paramObject2 instanceof ScriptableObject)
      {
        Object localObject1 = ((ScriptableObject)paramObject2).equivalentValues(paramObject1);
        if (localObject1 != Scriptable.NOT_FOUND)
          return ((Boolean)localObject1).booleanValue();
      }
      return false;
    }
    if (paramObject1 instanceof Number)
      return eqNumber(((Number)paramObject1).doubleValue(), paramObject2);
    if (paramObject1 instanceof String)
      return eqString((String)paramObject1, paramObject2);
    if (paramObject1 instanceof Boolean)
    {
      boolean bool = ((Boolean)paramObject1).booleanValue();
      if (paramObject2 instanceof Boolean)
        return (bool == ((Boolean)paramObject2).booleanValue());
      if (paramObject2 instanceof ScriptableObject)
      {
        Object localObject3 = ((ScriptableObject)paramObject2).equivalentValues(paramObject1);
        if (localObject3 != Scriptable.NOT_FOUND)
          return ((Boolean)localObject3).booleanValue();
      }
      return eqNumber((bool) ? 1D : 0D, paramObject2);
    }
    if (paramObject1 instanceof Scriptable)
    {
      Object localObject2;
      if (paramObject2 instanceof Scriptable)
      {
        if (paramObject1 == paramObject2)
          return true;
        if (paramObject1 instanceof ScriptableObject)
        {
          localObject2 = ((ScriptableObject)paramObject1).equivalentValues(paramObject2);
          if (localObject2 != Scriptable.NOT_FOUND)
            return ((Boolean)localObject2).booleanValue();
        }
        if (paramObject2 instanceof ScriptableObject)
        {
          localObject2 = ((ScriptableObject)paramObject2).equivalentValues(paramObject1);
          if (localObject2 != Scriptable.NOT_FOUND)
            return ((Boolean)localObject2).booleanValue();
        }
        if ((paramObject1 instanceof Wrapper) && (paramObject2 instanceof Wrapper))
          return (((Wrapper)paramObject1).unwrap() == ((Wrapper)paramObject2).unwrap());
        return false;
      }
      if (paramObject2 instanceof Boolean)
      {
        if (paramObject1 instanceof ScriptableObject)
        {
          localObject2 = ((ScriptableObject)paramObject1).equivalentValues(paramObject2);
          if (localObject2 != Scriptable.NOT_FOUND)
            return ((Boolean)localObject2).booleanValue();
        }
        double d = (((Boolean)paramObject2).booleanValue()) ? 1D : 0D;
        return eqNumber(d, paramObject1);
      }
      if (paramObject2 instanceof Number)
        return eqNumber(((Number)paramObject2).doubleValue(), paramObject1);
      if (paramObject2 instanceof String)
        return eqString((String)paramObject2, paramObject1);
      return false;
    }
    warnAboutNonJSObject(paramObject1);
    return (paramObject1 == paramObject2);
  }

  static boolean eqNumber(double paramDouble, Object paramObject)
  {
    while (true)
    {
      if ((paramObject == null) || (paramObject == Undefined.instance))
        return false;
      if (paramObject instanceof Number)
        return (paramDouble == ((Number)paramObject).doubleValue());
      if (paramObject instanceof String)
        return (paramDouble == toNumber(paramObject));
      if (paramObject instanceof Boolean)
        return (paramDouble == ((((Boolean)paramObject).booleanValue()) ? 1D : 0D));
      if (!(paramObject instanceof Scriptable))
        break;
      if (paramObject instanceof ScriptableObject)
      {
        Number localNumber = wrapNumber(paramDouble);
        Object localObject = ((ScriptableObject)paramObject).equivalentValues(localNumber);
        if (localObject != Scriptable.NOT_FOUND)
          return ((Boolean)localObject).booleanValue();
      }
      paramObject = toPrimitive(paramObject);
    }
    warnAboutNonJSObject(paramObject);
    return false;
  }

  private static boolean eqString(String paramString, Object paramObject)
  {
    while (true)
    {
      if ((paramObject == null) || (paramObject == Undefined.instance))
        return false;
      if (paramObject instanceof String)
        return paramString.equals(paramObject);
      if (paramObject instanceof Number)
        return (toNumber(paramString) == ((Number)paramObject).doubleValue());
      if (paramObject instanceof Boolean)
        return (toNumber(paramString) == ((((Boolean)paramObject).booleanValue()) ? 1D : 0D));
      if (!(paramObject instanceof Scriptable))
        break;
      if (paramObject instanceof ScriptableObject)
      {
        Object localObject = ((ScriptableObject)paramObject).equivalentValues(paramString);
        if (localObject != Scriptable.NOT_FOUND)
          return ((Boolean)localObject).booleanValue();
      }
      paramObject = toPrimitive(paramObject);
    }
    warnAboutNonJSObject(paramObject);
    return false;
  }

  public static boolean shallowEq(Object paramObject1, Object paramObject2)
  {
    if (paramObject1 == paramObject2)
    {
      if (!(paramObject1 instanceof Number))
        return true;
      double d = ((Number)paramObject1).doubleValue();
      return (d == d);
    }
    if ((paramObject1 == null) || (paramObject1 == Undefined.instance))
      return false;
    if (paramObject1 instanceof Number)
    {
      if (!(paramObject2 instanceof Number))
        break label188;
      return (((Number)paramObject1).doubleValue() == ((Number)paramObject2).doubleValue());
    }
    if (paramObject1 instanceof String)
    {
      if (!(paramObject2 instanceof String))
        break label188;
      return paramObject1.equals(paramObject2);
    }
    if (paramObject1 instanceof Boolean)
    {
      if (!(paramObject2 instanceof Boolean))
        break label188;
      return paramObject1.equals(paramObject2);
    }
    if (paramObject1 instanceof Scriptable)
    {
      if ((!(paramObject1 instanceof Wrapper)) || (!(paramObject2 instanceof Wrapper)))
        break label188;
      return (((Wrapper)paramObject1).unwrap() == ((Wrapper)paramObject2).unwrap());
    }
    warnAboutNonJSObject(paramObject1);
    return (paramObject1 == paramObject2);
    label188: return false;
  }

  public static boolean instanceOf(Object paramObject1, Object paramObject2, Context paramContext)
  {
    if (!(paramObject2 instanceof Scriptable))
      throw typeError0("msg.instanceof.not.object");
    if (!(paramObject1 instanceof Scriptable))
      return false;
    return ((Scriptable)paramObject2).hasInstance((Scriptable)paramObject1);
  }

  protected static boolean jsDelegatesTo(Scriptable paramScriptable1, Scriptable paramScriptable2)
  {
    for (Scriptable localScriptable = paramScriptable1.getPrototype(); localScriptable != null; localScriptable = localScriptable.getPrototype())
      if (localScriptable.equals(paramScriptable2))
        return true;
    return false;
  }

  public static boolean in(Object paramObject1, Object paramObject2, Context paramContext)
  {
    if (!(paramObject2 instanceof Scriptable))
      throw typeError0("msg.instanceof.not.object");
    return hasObjectElem((Scriptable)paramObject2, paramObject1, paramContext);
  }

  public static boolean cmp_LT(Object paramObject1, Object paramObject2)
  {
    double d1;
    double d2;
    if ((paramObject1 instanceof Number) && (paramObject2 instanceof Number))
    {
      d1 = ((Number)paramObject1).doubleValue();
      d2 = ((Number)paramObject2).doubleValue();
    }
    else
    {
      if (paramObject1 instanceof Scriptable)
        paramObject1 = ((Scriptable)paramObject1).getDefaultValue(NumberClass);
      if (paramObject2 instanceof Scriptable)
        paramObject2 = ((Scriptable)paramObject2).getDefaultValue(NumberClass);
      if ((paramObject1 instanceof String) && (paramObject2 instanceof String))
        return (((String)paramObject1).compareTo((String)paramObject2) < 0);
      d1 = toNumber(paramObject1);
      d2 = toNumber(paramObject2);
    }
    return (d1 < d2);
  }

  public static boolean cmp_LE(Object paramObject1, Object paramObject2)
  {
    double d1;
    double d2;
    if ((paramObject1 instanceof Number) && (paramObject2 instanceof Number))
    {
      d1 = ((Number)paramObject1).doubleValue();
      d2 = ((Number)paramObject2).doubleValue();
    }
    else
    {
      if (paramObject1 instanceof Scriptable)
        paramObject1 = ((Scriptable)paramObject1).getDefaultValue(NumberClass);
      if (paramObject2 instanceof Scriptable)
        paramObject2 = ((Scriptable)paramObject2).getDefaultValue(NumberClass);
      if ((paramObject1 instanceof String) && (paramObject2 instanceof String))
        return (((String)paramObject1).compareTo((String)paramObject2) <= 0);
      d1 = toNumber(paramObject1);
      d2 = toNumber(paramObject2);
    }
    return (d1 <= d2);
  }

  public static ScriptableObject getGlobal(Context paramContext)
  {
    Class localClass = Kit.classOrNull("sun.org.mozilla.javascript.internal.tools.shell.Global");
    if (localClass != null)
      try
      {
        Class[] arrayOfClass = { ContextClass };
        Constructor localConstructor = localClass.getConstructor(arrayOfClass);
        Object[] arrayOfObject = { paramContext };
        return ((ScriptableObject)localConstructor.newInstance(arrayOfObject));
      }
      catch (Exception localException)
      {
      }
    return new ImporterTopLevel(paramContext);
  }

  public static boolean hasTopCall(Context paramContext)
  {
    return (paramContext.topCallScope != null);
  }

  public static Scriptable getTopCallScope(Context paramContext)
  {
    Scriptable localScriptable = paramContext.topCallScope;
    if (localScriptable == null)
      throw new IllegalStateException();
    return localScriptable;
  }

  public static Object doTopCall(Callable paramCallable, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    Object localObject1;
    if (paramScriptable1 == null)
      throw new IllegalArgumentException();
    if (paramContext.topCallScope != null)
      throw new IllegalStateException();
    paramContext.topCallScope = ScriptableObject.getTopLevelScope(paramScriptable1);
    paramContext.useDynamicScope = paramContext.hasFeature(7);
    ContextFactory localContextFactory = paramContext.getFactory();
    try
    {
      throw new IllegalStateException();
    }
    finally
    {
      paramContext.topCallScope = null;
      paramContext.cachedXMLLib = null;
      if (paramContext.currentActivationCall != null)
        throw new IllegalStateException();
    }
    return localObject1;
  }

  static Scriptable checkDynamicScope(Scriptable paramScriptable1, Scriptable paramScriptable2)
  {
    if (paramScriptable1 == paramScriptable2)
      return paramScriptable1;
    Scriptable localScriptable = paramScriptable1;
    do
    {
      localScriptable = localScriptable.getPrototype();
      if (localScriptable == paramScriptable2)
        return paramScriptable1;
    }
    while (localScriptable != null);
    return paramScriptable2;
  }

  public static void initScript(NativeFunction paramNativeFunction, Scriptable paramScriptable1, Context paramContext, Scriptable paramScriptable2, boolean paramBoolean)
  {
    if (paramContext.topCallScope == null)
      throw new IllegalStateException();
    int i = paramNativeFunction.getParamAndVarCount();
    if (i != 0)
    {
      for (Scriptable localScriptable = paramScriptable2; localScriptable instanceof NativeWith; localScriptable = localScriptable.getParentScope());
      int j = i;
      while (j-- != 0)
      {
        String str = paramNativeFunction.getParamOrVarName(j);
        if (!(ScriptableObject.hasProperty(paramScriptable2, str)))
          if (!(paramBoolean))
            ScriptableObject.defineProperty(localScriptable, str, Undefined.instance, 4);
          else
            localScriptable.put(str, localScriptable, Undefined.instance);
      }
    }
  }

  public static Scriptable createFunctionActivation(NativeFunction paramNativeFunction, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    return new NativeCall(paramNativeFunction, paramScriptable, paramArrayOfObject);
  }

  public static void enterActivationFunction(Context paramContext, Scriptable paramScriptable)
  {
    if (paramContext.topCallScope == null)
      throw new IllegalStateException();
    NativeCall localNativeCall = (NativeCall)paramScriptable;
    localNativeCall.parentActivationCall = paramContext.currentActivationCall;
    paramContext.currentActivationCall = localNativeCall;
  }

  public static void exitActivationFunction(Context paramContext)
  {
    NativeCall localNativeCall = paramContext.currentActivationCall;
    paramContext.currentActivationCall = localNativeCall.parentActivationCall;
    localNativeCall.parentActivationCall = null;
  }

  static NativeCall findFunctionActivation(Context paramContext, Function paramFunction)
  {
    for (NativeCall localNativeCall = paramContext.currentActivationCall; localNativeCall != null; localNativeCall = localNativeCall.parentActivationCall)
      if (localNativeCall.function == paramFunction)
        return localNativeCall;
    return null;
  }

  public static Scriptable newCatchScope(Throwable paramThrowable, Scriptable paramScriptable1, String paramString, Context paramContext, Scriptable paramScriptable2)
  {
    Object localObject1;
    int i;
    if (paramThrowable instanceof JavaScriptException)
    {
      i = 0;
      localObject1 = ((JavaScriptException)paramThrowable).getValue();
    }
    else
    {
      i = 1;
      if (paramScriptable1 != null)
      {
        localObject2 = (NativeObject)paramScriptable1;
        localObject1 = ((NativeObject)localObject2).getAssociatedValue(paramThrowable);
        if (localObject1 == null)
          Kit.codeBug();
      }
      else
      {
        String str1;
        String str2;
        Object[] arrayOfObject;
        Object localObject4;
        Throwable localThrowable = null;
        if (paramThrowable instanceof EcmaError)
        {
          localObject3 = (EcmaError)paramThrowable;
          localObject2 = localObject3;
          str1 = ((EcmaError)localObject3).getName();
          str2 = ((EcmaError)localObject3).getErrorMessage();
        }
        else if (paramThrowable instanceof WrappedException)
        {
          localObject3 = (WrappedException)paramThrowable;
          localObject2 = localObject3;
          localThrowable = ((WrappedException)localObject3).getWrappedException();
          str1 = "JavaException";
          str2 = localThrowable.getClass().getName() + ": " + localThrowable.getMessage();
        }
        else if (paramThrowable instanceof EvaluatorException)
        {
          localObject3 = (EvaluatorException)paramThrowable;
          localObject2 = localObject3;
          str1 = "InternalError";
          str2 = ((EvaluatorException)localObject3).getMessage();
        }
        else
        {
          throw Kit.codeBug();
        }
        Object localObject3 = ((RhinoException)localObject2).sourceName();
        if (localObject3 == null)
          localObject3 = "";
        int j = ((RhinoException)localObject2).lineNumber();
        if (j > 0)
          arrayOfObject = { str2, localObject3, new Integer(j) };
        else
          arrayOfObject = { str2, localObject3 };
        Scriptable localScriptable = paramContext.newObject(paramScriptable2, str1, arrayOfObject);
        ScriptableObject.putProperty(localScriptable, "name", str1);
        if (localThrowable != null)
        {
          localObject4 = paramContext.getWrapFactory().wrap(paramContext, paramScriptable2, localThrowable, null);
          ScriptableObject.defineProperty(localScriptable, "javaException", localObject4, 5);
        }
        if (localObject2 != null)
        {
          localObject4 = paramContext.getWrapFactory().wrap(paramContext, paramScriptable2, localObject2, null);
          ScriptableObject.defineProperty(localScriptable, "rhinoException", localObject4, 5);
        }
        localObject1 = localScriptable;
      }
    }
    Object localObject2 = new NativeObject();
    ((NativeObject)localObject2).defineProperty(paramString, localObject1, 4);
    if (i != 0)
      ((NativeObject)localObject2).associateValue(paramThrowable, localObject1);
    return ((Scriptable)(Scriptable)localObject2);
  }

  public static Scriptable enterWith(Object paramObject, Context paramContext, Scriptable paramScriptable)
  {
    Scriptable localScriptable = toObjectOrNull(paramContext, paramObject);
    if (localScriptable == null)
      throw typeError1("msg.undef.with", toString(paramObject));
    if (localScriptable instanceof XMLObject)
    {
      XMLObject localXMLObject = (XMLObject)localScriptable;
      return localXMLObject.enterWith(paramScriptable);
    }
    return new NativeWith(paramScriptable, localScriptable);
  }

  public static Scriptable leaveWith(Scriptable paramScriptable)
  {
    NativeWith localNativeWith = (NativeWith)paramScriptable;
    return localNativeWith.getParentScope();
  }

  public static Scriptable enterDotQuery(Object paramObject, Scriptable paramScriptable)
  {
    if (!(paramObject instanceof XMLObject))
      throw notXmlError(paramObject);
    XMLObject localXMLObject = (XMLObject)paramObject;
    return localXMLObject.enterDotQuery(paramScriptable);
  }

  public static Object updateDotQuery(boolean paramBoolean, Scriptable paramScriptable)
  {
    NativeWith localNativeWith = (NativeWith)paramScriptable;
    return localNativeWith.updateDotQuery(paramBoolean);
  }

  public static Scriptable leaveDotQuery(Scriptable paramScriptable)
  {
    NativeWith localNativeWith = (NativeWith)paramScriptable;
    return localNativeWith.getParentScope();
  }

  public static void setFunctionProtoAndParent(BaseFunction paramBaseFunction, Scriptable paramScriptable)
  {
    paramBaseFunction.setParentScope(paramScriptable);
    paramBaseFunction.setPrototype(ScriptableObject.getFunctionPrototype(paramScriptable));
  }

  public static void setObjectProtoAndParent(ScriptableObject paramScriptableObject, Scriptable paramScriptable)
  {
    paramScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
    paramScriptableObject.setParentScope(paramScriptable);
    Scriptable localScriptable = ScriptableObject.getClassPrototype(paramScriptable, paramScriptableObject.getClassName());
    paramScriptableObject.setPrototype(localScriptable);
  }

  public static void initFunction(Context paramContext, Scriptable paramScriptable, NativeFunction paramNativeFunction, int paramInt, boolean paramBoolean)
  {
    String str;
    if (paramInt == 1)
    {
      str = paramNativeFunction.getFunctionName();
      if ((str != null) && (str.length() != 0))
        if (!(paramBoolean))
          ScriptableObject.defineProperty(paramScriptable, str, paramNativeFunction, 4);
        else
          paramScriptable.put(str, paramScriptable, paramNativeFunction);
    }
    else if (paramInt == 3)
    {
      str = paramNativeFunction.getFunctionName();
      if ((str != null) && (str.length() != 0))
      {
        while (paramScriptable instanceof NativeWith)
          paramScriptable = paramScriptable.getParentScope();
        paramScriptable.put(str, paramScriptable, paramNativeFunction);
      }
    }
    else
    {
      throw Kit.codeBug();
    }
  }

  public static Scriptable newArrayLiteral(Object[] paramArrayOfObject, int[] paramArrayOfInt, Context paramContext, Scriptable paramScriptable)
  {
    Scriptable localScriptable;
    int i = paramArrayOfObject.length;
    int j = 0;
    if (paramArrayOfInt != null)
      j = paramArrayOfInt.length;
    int k = i + j;
    Integer localInteger = new Integer(k);
    if (paramContext.getLanguageVersion() == 120)
    {
      localScriptable = paramContext.newObject(paramScriptable, "Array", emptyArgs);
      ScriptableObject.putProperty(localScriptable, "length", localInteger);
    }
    else
    {
      localScriptable = paramContext.newObject(paramScriptable, "Array", new Object[] { localInteger });
    }
    int l = 0;
    int i1 = 0;
    int i2 = 0;
    while (i1 != k)
    {
      if ((l != j) && (paramArrayOfInt[l] == i1))
      {
        ++l;
      }
      else
      {
        ScriptableObject.putProperty(localScriptable, i1, paramArrayOfObject[i2]);
        ++i2;
      }
      ++i1;
    }
    return localScriptable;
  }

  public static Scriptable newObjectLiteral(Object[] paramArrayOfObject1, Object[] paramArrayOfObject2, Context paramContext, Scriptable paramScriptable)
  {
    Scriptable localScriptable = paramContext.newObject(paramScriptable);
    int i = 0;
    int j = paramArrayOfObject1.length;
    while (i != j)
    {
      Object localObject1 = paramArrayOfObject1[i];
      Object localObject2 = paramArrayOfObject2[i];
      if (localObject1 instanceof String)
      {
        ScriptableObject.putProperty(localScriptable, (String)localObject1, localObject2);
      }
      else
      {
        int k = ((Integer)localObject1).intValue();
        ScriptableObject.putProperty(localScriptable, k, localObject2);
      }
      ++i;
    }
    return localScriptable;
  }

  public static boolean isArrayObject(Object paramObject)
  {
    return ((paramObject instanceof NativeArray) || (paramObject instanceof Arguments));
  }

  public static Object[] getArrayElements(Scriptable paramScriptable)
  {
    Context localContext = Context.getContext();
    long l = NativeArray.getLengthProperty(localContext, paramScriptable);
    if (l > 2147483647L)
      throw new IllegalArgumentException();
    int i = (int)l;
    if (i == 0)
      return emptyArgs;
    Object[] arrayOfObject = new Object[i];
    for (int j = 0; j < i; ++j)
    {
      Object localObject = ScriptableObject.getProperty(paramScriptable, j);
      arrayOfObject[j] = ((localObject == Scriptable.NOT_FOUND) ? Undefined.instance : localObject);
    }
    return arrayOfObject;
  }

  static void checkDeprecated(Context paramContext, String paramString)
  {
    int i = paramContext.getLanguageVersion();
    if ((i >= 140) || (i == 0))
    {
      String str = getMessage1("msg.deprec.ctor", paramString);
      if (i == 0)
        Context.reportWarning(str);
      else
        throw Context.reportRuntimeError(str);
    }
  }

  public static String getMessage0(String paramString)
  {
    return getMessage(paramString, null);
  }

  public static String getMessage1(String paramString, Object paramObject)
  {
    Object[] arrayOfObject = { paramObject };
    return getMessage(paramString, arrayOfObject);
  }

  public static String getMessage2(String paramString, Object paramObject1, Object paramObject2)
  {
    Object[] arrayOfObject = { paramObject1, paramObject2 };
    return getMessage(paramString, arrayOfObject);
  }

  public static String getMessage3(String paramString, Object paramObject1, Object paramObject2, Object paramObject3)
  {
    Object[] arrayOfObject = { paramObject1, paramObject2, paramObject3 };
    return getMessage(paramString, arrayOfObject);
  }

  public static String getMessage4(String paramString, Object paramObject1, Object paramObject2, Object paramObject3, Object paramObject4)
  {
    Object[] arrayOfObject = { paramObject1, paramObject2, paramObject3, paramObject4 };
    return getMessage(paramString, arrayOfObject);
  }

  public static String getMessage(String paramString, Object[] paramArrayOfObject)
  {
    String str;
    Context localContext = Context.getCurrentContext();
    Locale localLocale = (localContext != null) ? localContext.getLocale() : Locale.getDefault();
    ResourceBundle localResourceBundle = (ResourceBundle)AccessController.doPrivileged(new PrivilegedAction(localLocale)
    {
      public ResourceBundle run()
      {
        return ResourceBundle.getBundle("sun.org.mozilla.javascript.internal.resources.Messages", this.val$locale);
      }
    });
    try
    {
      str = localResourceBundle.getString(paramString);
    }
    catch (MissingResourceException localMissingResourceException)
    {
      throw new RuntimeException("no message resource found for message property " + paramString);
    }
    java.text.MessageFormat localMessageFormat = new java.text.MessageFormat(str);
    return localMessageFormat.format(paramArrayOfObject);
  }

  public static EcmaError constructError(String paramString1, String paramString2)
  {
    int[] arrayOfInt = new int[1];
    String str = Context.getSourcePositionFromStack(arrayOfInt);
    return constructError(paramString1, paramString2, str, arrayOfInt[0], null, 0);
  }

  public static EcmaError constructError(String paramString1, String paramString2, String paramString3, int paramInt1, String paramString4, int paramInt2)
  {
    return new EcmaError(paramString1, paramString2, paramString3, paramInt1, paramString4, paramInt2);
  }

  public static EcmaError typeError(String paramString)
  {
    return constructError("TypeError", paramString);
  }

  public static EcmaError typeError0(String paramString)
  {
    String str = getMessage0(paramString);
    return typeError(str);
  }

  public static EcmaError typeError1(String paramString1, String paramString2)
  {
    String str = getMessage1(paramString1, paramString2);
    return typeError(str);
  }

  public static EcmaError typeError2(String paramString1, String paramString2, String paramString3)
  {
    String str = getMessage2(paramString1, paramString2, paramString3);
    return typeError(str);
  }

  public static EcmaError typeError3(String paramString1, String paramString2, String paramString3, String paramString4)
  {
    String str = getMessage3(paramString1, paramString2, paramString3, paramString4);
    return typeError(str);
  }

  public static RuntimeException undefReadError(Object paramObject1, Object paramObject2)
  {
    String str = (paramObject2 == null) ? "null" : paramObject2.toString();
    return typeError2("msg.undef.prop.read", toString(paramObject1), str);
  }

  public static RuntimeException undefCallError(Object paramObject1, Object paramObject2)
  {
    String str = (paramObject2 == null) ? "null" : paramObject2.toString();
    return typeError2("msg.undef.method.call", toString(paramObject1), str);
  }

  public static RuntimeException undefWriteError(Object paramObject1, Object paramObject2, Object paramObject3)
  {
    String str1 = (paramObject2 == null) ? "null" : paramObject2.toString();
    String str2 = (paramObject3 instanceof Scriptable) ? paramObject3.toString() : toString(paramObject3);
    return typeError3("msg.undef.prop.write", toString(paramObject1), str1, str2);
  }

  public static RuntimeException notFoundError(Scriptable paramScriptable, String paramString)
  {
    String str = getMessage1("msg.is.not.defined", paramString);
    throw constructError("ReferenceError", str);
  }

  public static RuntimeException notFunctionError(Object paramObject)
  {
    return notFunctionError(paramObject, paramObject);
  }

  public static RuntimeException notFunctionError(Object paramObject1, Object paramObject2)
  {
    String str = (paramObject2 == null) ? "null" : paramObject2.toString();
    if (paramObject1 == Scriptable.NOT_FOUND)
      return typeError1("msg.function.not.found", str);
    return typeError2("msg.isnt.function", str, (paramObject1 == null) ? "null" : paramObject1.getClass().getName());
  }

  private static RuntimeException notXmlError(Object paramObject)
  {
    throw typeError1("msg.isnt.xml.object", toString(paramObject));
  }

  private static void warnAboutNonJSObject(Object paramObject)
  {
    String str = "RHINO USAGE WARNING: Missed Context.javaToJS() conversion:\nRhino runtime detected object " + paramObject + " of class " + paramObject.getClass().getName() + " where it expected String, Number, Boolean or Scriptable instance. Please check your code for missig Context.javaToJS() call.";
    Context.reportWarning(str);
    System.err.println(str);
  }

  public static RegExpProxy getRegExpProxy(Context paramContext)
  {
    return paramContext.getRegExpProxy();
  }

  public static void setRegExpProxy(Context paramContext, RegExpProxy paramRegExpProxy)
  {
    if (paramRegExpProxy == null)
      throw new IllegalArgumentException();
    paramContext.regExpProxy = paramRegExpProxy;
  }

  public static RegExpProxy checkRegExpProxy(Context paramContext)
  {
    RegExpProxy localRegExpProxy = getRegExpProxy(paramContext);
    if (localRegExpProxy == null)
      throw Context.reportRuntimeError0("msg.no.regexp");
    return localRegExpProxy;
  }

  private static XMLLib currentXMLLib(Context paramContext)
  {
    if (paramContext.topCallScope == null)
      throw new IllegalStateException();
    XMLLib localXMLLib = paramContext.cachedXMLLib;
    if (localXMLLib == null)
    {
      localXMLLib = XMLLib.extractFromScope(paramContext.topCallScope);
      if (localXMLLib == null)
        throw new IllegalStateException();
      paramContext.cachedXMLLib = localXMLLib;
    }
    return localXMLLib;
  }

  public static String escapeAttributeValue(Object paramObject, Context paramContext)
  {
    XMLLib localXMLLib = currentXMLLib(paramContext);
    return localXMLLib.escapeAttributeValue(paramObject);
  }

  public static String escapeTextValue(Object paramObject, Context paramContext)
  {
    XMLLib localXMLLib = currentXMLLib(paramContext);
    return localXMLLib.escapeTextValue(paramObject);
  }

  public static Ref memberRef(Object paramObject1, Object paramObject2, Context paramContext, int paramInt)
  {
    if (!(paramObject1 instanceof XMLObject))
      throw notXmlError(paramObject1);
    XMLObject localXMLObject = (XMLObject)paramObject1;
    return localXMLObject.memberRef(paramContext, paramObject2, paramInt);
  }

  public static Ref memberRef(Object paramObject1, Object paramObject2, Object paramObject3, Context paramContext, int paramInt)
  {
    if (!(paramObject1 instanceof XMLObject))
      throw notXmlError(paramObject1);
    XMLObject localXMLObject = (XMLObject)paramObject1;
    return localXMLObject.memberRef(paramContext, paramObject2, paramObject3, paramInt);
  }

  public static Ref nameRef(Object paramObject, Context paramContext, Scriptable paramScriptable, int paramInt)
  {
    XMLLib localXMLLib = currentXMLLib(paramContext);
    return localXMLLib.nameRef(paramContext, paramObject, paramScriptable, paramInt);
  }

  public static Ref nameRef(Object paramObject1, Object paramObject2, Context paramContext, Scriptable paramScriptable, int paramInt)
  {
    XMLLib localXMLLib = currentXMLLib(paramContext);
    return localXMLLib.nameRef(paramContext, paramObject1, paramObject2, paramScriptable, paramInt);
  }

  private static void storeIndexResult(Context paramContext, int paramInt)
  {
    paramContext.scratchIndex = paramInt;
  }

  static int lastIndexResult(Context paramContext)
  {
    return paramContext.scratchIndex;
  }

  public static void storeUint32Result(Context paramContext, long paramLong)
  {
    if (paramLong >>> 32 != 3412046810217185280L)
      throw new IllegalArgumentException();
    paramContext.scratchUint32 = paramLong;
  }

  public static long lastUint32Result(Context paramContext)
  {
    long l = paramContext.scratchUint32;
    if (l >>> 32 != 3412046810217185280L)
      throw new IllegalStateException();
    return l;
  }

  private static void storeScriptable(Context paramContext, Scriptable paramScriptable)
  {
    if (paramContext.scratchScriptable != null)
      throw new IllegalStateException();
    paramContext.scratchScriptable = paramScriptable;
  }

  public static Scriptable lastStoredScriptable(Context paramContext)
  {
    Scriptable localScriptable = paramContext.scratchScriptable;
    paramContext.scratchScriptable = null;
    return localScriptable;
  }

  static String makeUrlForGeneratedScript(boolean paramBoolean, String paramString, int paramInt)
  {
    if (paramBoolean)
      return paramString + '#' + paramInt + "(eval)";
    return paramString + '#' + paramInt + "(Function)";
  }

  static boolean isGeneratedScript(String paramString)
  {
    return ((paramString.indexOf("(eval)") >= 0) || (paramString.indexOf("(Function)") >= 0));
  }

  private static RuntimeException errorWithClassName(String paramString, Object paramObject)
  {
    return Context.reportRuntimeError1(paramString, paramObject.getClass().getName());
  }

  private static class IdEnumeration
  {
    Scriptable obj;
    Object[] ids;
    int index;
    ObjToIntMap used;
    String currentId;
    boolean enumValues;
  }
}
package sun.org.mozilla.javascript.internal;

public class NativeArray extends IdScriptableObject
{
  static final long serialVersionUID = 7331366857676127338L;
  private static final Object ARRAY_TAG = new Object();
  private static final int Id_length = 1;
  private static final int MAX_INSTANCE_ID = 1;
  private static final int Id_constructor = 1;
  private static final int Id_toString = 2;
  private static final int Id_toLocaleString = 3;
  private static final int Id_toSource = 4;
  private static final int Id_join = 5;
  private static final int Id_reverse = 6;
  private static final int Id_sort = 7;
  private static final int Id_push = 8;
  private static final int Id_pop = 9;
  private static final int Id_shift = 10;
  private static final int Id_unshift = 11;
  private static final int Id_splice = 12;
  private static final int Id_concat = 13;
  private static final int Id_slice = 14;
  private static final int MAX_PROTOTYPE_ID = 14;
  private long length;
  private Object[] dense;
  private static final int maximumDenseLength = 10000;

  static void init(Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeArray localNativeArray = new NativeArray();
    localNativeArray.exportAsJSClass(14, paramScriptable, paramBoolean);
  }

  private NativeArray()
  {
    this.dense = null;
    this.length = 3412046964836007936L;
  }

  public NativeArray(long paramLong)
  {
    int i = (int)paramLong;
    if ((i == paramLong) && (i > 0))
    {
      if (i > 10000)
        i = 10000;
      this.dense = new Object[i];
      for (int j = 0; j < i; ++j)
        this.dense[j] = NOT_FOUND;
    }
    this.length = paramLong;
  }

  public NativeArray(Object[] paramArrayOfObject)
  {
    this.dense = paramArrayOfObject;
    this.length = paramArrayOfObject.length;
  }

  public String getClassName()
  {
    return "Array";
  }

  protected int getMaxInstanceId()
  {
    return 1;
  }

  protected int findInstanceIdInfo(String paramString)
  {
    if (paramString.equals("length"))
      return instanceIdInfo(6, 1);
    return super.findInstanceIdInfo(paramString);
  }

  protected String getInstanceIdName(int paramInt)
  {
    if (paramInt == 1)
      return "length";
    return super.getInstanceIdName(paramInt);
  }

  protected Object getInstanceIdValue(int paramInt)
  {
    if (paramInt == 1)
      return ScriptRuntime.wrapNumber(this.length);
    return super.getInstanceIdValue(paramInt);
  }

  protected void setInstanceIdValue(int paramInt, Object paramObject)
  {
    if (paramInt == 1)
    {
      setLength(paramObject);
      return;
    }
    super.setInstanceIdValue(paramInt, paramObject);
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
      i = 1;
      str = "toLocaleString";
      break;
    case 4:
      i = 0;
      str = "toSource";
      break;
    case 5:
      i = 1;
      str = "join";
      break;
    case 6:
      i = 0;
      str = "reverse";
      break;
    case 7:
      i = 1;
      str = "sort";
      break;
    case 8:
      i = 1;
      str = "push";
      break;
    case 9:
      i = 1;
      str = "pop";
      break;
    case 10:
      i = 1;
      str = "shift";
      break;
    case 11:
      i = 1;
      str = "unshift";
      break;
    case 12:
      i = 1;
      str = "splice";
      break;
    case 13:
      i = 1;
      str = "concat";
      break;
    case 14:
      i = 1;
      str = "slice";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(ARRAY_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(paramIdFunctionObject.hasTag(ARRAY_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case 1:
      int j = (paramScriptable2 == null) ? 1 : 0;
      if (j == 0)
        return paramIdFunctionObject.construct(paramContext, paramScriptable1, paramArrayOfObject);
      return jsConstructor(paramContext, paramScriptable1, paramArrayOfObject);
    case 2:
      return toStringHelper(paramContext, paramScriptable1, paramScriptable2, paramContext.hasFeature(4), false);
    case 3:
      return toStringHelper(paramContext, paramScriptable1, paramScriptable2, false, true);
    case 4:
      return toStringHelper(paramContext, paramScriptable1, paramScriptable2, true, false);
    case 5:
      return js_join(paramContext, paramScriptable2, paramArrayOfObject);
    case 6:
      return js_reverse(paramContext, paramScriptable2, paramArrayOfObject);
    case 7:
      return js_sort(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    case 8:
      return js_push(paramContext, paramScriptable2, paramArrayOfObject);
    case 9:
      return js_pop(paramContext, paramScriptable2, paramArrayOfObject);
    case 10:
      return js_shift(paramContext, paramScriptable2, paramArrayOfObject);
    case 11:
      return js_unshift(paramContext, paramScriptable2, paramArrayOfObject);
    case 12:
      return js_splice(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    case 13:
      return js_concat(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    case 14:
      return js_slice(paramContext, paramScriptable2, paramArrayOfObject);
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  public Object get(int paramInt, Scriptable paramScriptable)
  {
    if ((this.dense != null) && (0 <= paramInt) && (paramInt < this.dense.length))
      return this.dense[paramInt];
    return super.get(paramInt, paramScriptable);
  }

  public boolean has(int paramInt, Scriptable paramScriptable)
  {
    if ((this.dense != null) && (0 <= paramInt) && (paramInt < this.dense.length))
      return (this.dense[paramInt] != NOT_FOUND);
    return super.has(paramInt, paramScriptable);
  }

  private static long toArrayIndex(String paramString)
  {
    double d = ScriptRuntime.toNumber(paramString);
    if (d == d)
    {
      long l = ScriptRuntime.toUint32(d);
      if ((l == d) && (l != 4294967295L) && (Long.toString(l).equals(paramString)))
        return l;
    }
    return -1L;
  }

  public void put(String paramString, Scriptable paramScriptable, Object paramObject)
  {
    super.put(paramString, paramScriptable, paramObject);
    if (paramScriptable == this)
    {
      long l = toArrayIndex(paramString);
      if (l >= this.length)
        this.length = (l + 3412039749290950657L);
    }
  }

  public void put(int paramInt, Scriptable paramScriptable, Object paramObject)
  {
    if ((paramScriptable == this) && (!(isSealed())) && (this.dense != null) && (0 <= paramInt) && (paramInt < this.dense.length))
      this.dense[paramInt] = paramObject;
    else
      super.put(paramInt, paramScriptable, paramObject);
    if ((paramScriptable == this) && (this.length <= paramInt))
      this.length = (paramInt + 3412048184606720001L);
  }

  public void delete(int paramInt)
  {
    if ((!(isSealed())) && (this.dense != null) && (0 <= paramInt) && (paramInt < this.dense.length))
      this.dense[paramInt] = NOT_FOUND;
    else
      super.delete(paramInt);
  }

  public Object[] getIds()
  {
    Object[] arrayOfObject1 = super.getIds();
    if (this.dense == null)
      return arrayOfObject1;
    int i = this.dense.length;
    long l = this.length;
    if (i > l)
      i = (int)l;
    if (i == 0)
      return arrayOfObject1;
    int j = arrayOfObject1.length;
    Object localObject = new Object[i + j];
    System.arraycopy(this.dense, 0, localObject, 0, i);
    int k = 0;
    for (int i1 = 0; i1 != i; ++i1)
      if (localObject[i1] != NOT_FOUND)
      {
        localObject[k] = new Integer(i1);
        ++k;
      }
    if (k != i)
    {
      Object[] arrayOfObject2 = new Object[k + j];
      System.arraycopy(localObject, 0, arrayOfObject2, 0, k);
      localObject = arrayOfObject2;
    }
    System.arraycopy(arrayOfObject1, 0, localObject, k, j);
    return ((Object)localObject);
  }

  public Object getDefaultValue(Class paramClass)
  {
    if (paramClass == ScriptRuntime.NumberClass)
    {
      Context localContext = Context.getContext();
      if (localContext.getLanguageVersion() == 120)
        return new Long(this.length);
    }
    return super.getDefaultValue(paramClass);
  }

  private static Object jsConstructor(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    if (paramArrayOfObject.length == 0)
      return new NativeArray();
    if (paramContext.getLanguageVersion() == 120)
      return new NativeArray(paramArrayOfObject);
    Object localObject = paramArrayOfObject[0];
    if ((paramArrayOfObject.length > 1) || (!(localObject instanceof Number)))
      return new NativeArray(paramArrayOfObject);
    long l = ScriptRuntime.toUint32(localObject);
    if (l != ((Number)localObject).doubleValue())
      throw Context.reportRuntimeError0("msg.arraylength.bad");
    return new NativeArray(l);
  }

  public long getLength()
  {
    return this.length;
  }

  /**
   * @deprecated
   */
  public long jsGet_length()
  {
    return getLength();
  }

  private void setLength(Object paramObject)
  {
    double d = ScriptRuntime.toNumber(paramObject);
    long l1 = ScriptRuntime.toUint32(d);
    if (l1 != d)
      throw Context.reportRuntimeError0("msg.arraylength.bad");
    if (l1 < this.length)
      if (this.length - l1 > 4096L)
      {
        Object[] arrayOfObject = getIds();
        for (int i = 0; i < arrayOfObject.length; ++i)
        {
          Object localObject = arrayOfObject[i];
          if (localObject instanceof String)
          {
            String str = (String)localObject;
            long l3 = toArrayIndex(str);
            if (l3 >= l1)
              delete(str);
          }
          else
          {
            int j = ((Integer)localObject).intValue();
            if (j >= l1)
              delete(j);
          }
        }
      }
      else
      {
        long l2 = l1;
        while (l2 < this.length)
        {
          deleteElem(this, l2);
          l2 += 3412039921089642497L;
        }
      }
    this.length = l1;
  }

  static long getLengthProperty(Context paramContext, Scriptable paramScriptable)
  {
    if (paramScriptable instanceof NativeString)
      return ((NativeString)paramScriptable).getLength();
    if (paramScriptable instanceof NativeArray)
      return ((NativeArray)paramScriptable).getLength();
    if (!(paramScriptable instanceof Scriptable))
      return 3412047463052214272L;
    return ScriptRuntime.toUint32(ScriptRuntime.getObjectProp(paramScriptable, "length", paramContext));
  }

  private static Object setLengthProperty(Context paramContext, Scriptable paramScriptable, long paramLong)
  {
    return ScriptRuntime.setObjectProp(paramScriptable, "length", ScriptRuntime.wrapNumber(paramLong), paramContext);
  }

  private static void deleteElem(Scriptable paramScriptable, long paramLong)
  {
    int i = (int)paramLong;
    if (i == paramLong)
      paramScriptable.delete(i);
    else
      paramScriptable.delete(Long.toString(paramLong));
  }

  private static Object getElem(Context paramContext, Scriptable paramScriptable, long paramLong)
  {
    if (paramLong > 2147483647L)
    {
      String str = Long.toString(paramLong);
      return ScriptRuntime.getObjectProp(paramScriptable, str, paramContext);
    }
    return ScriptRuntime.getObjectIndex(paramScriptable, (int)paramLong, paramContext);
  }

  private static void setElem(Context paramContext, Scriptable paramScriptable, long paramLong, Object paramObject)
  {
    if (paramLong > 2147483647L)
    {
      String str = Long.toString(paramLong);
      ScriptRuntime.setObjectProp(paramScriptable, str, paramObject, paramContext);
    }
    else
    {
      ScriptRuntime.setObjectIndex(paramScriptable, (int)paramLong, paramObject, paramContext);
    }
  }

  private static String toStringHelper(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, boolean paramBoolean1, boolean paramBoolean2)
  {
    String str;
    int j;
    boolean bool;
    long l1 = getLengthProperty(paramContext, paramScriptable2);
    StringBuffer localStringBuffer = new StringBuffer(256);
    if (paramBoolean1)
    {
      localStringBuffer.append('[');
      str = ", ";
    }
    else
    {
      str = ",";
    }
    int i = 0;
    long l2 = 3412047291253522432L;
    if (paramContext.iterating == null)
    {
      j = 1;
      bool = false;
      paramContext.iterating = new ObjToIntMap(31);
    }
    else
    {
      j = 0;
      bool = paramContext.iterating.has(paramScriptable2);
    }
    try
    {
      if (!(bool))
      {
        paramContext.iterating.put(paramScriptable2, 0);
        l2 = 3412048098707374080L;
        while (l2 < l1)
        {
          if (l2 > 3412048253326196736L)
            localStringBuffer.append(str);
          Object localObject1 = getElem(paramContext, paramScriptable2, l2);
          if ((localObject1 == null) || (localObject1 == Undefined.instance))
          {
            i = 0;
          }
          else
          {
            i = 1;
            if (paramBoolean1)
            {
              localStringBuffer.append(ScriptRuntime.uneval(paramContext, paramScriptable1, localObject1));
            }
            else
            {
              Object localObject2;
              if (localObject1 instanceof String)
              {
                localObject2 = (String)localObject1;
                if (paramBoolean1)
                {
                  localStringBuffer.append('"');
                  localStringBuffer.append(ScriptRuntime.escapeString((String)localObject2));
                  localStringBuffer.append('"');
                }
                else
                {
                  localStringBuffer.append((String)localObject2);
                }
              }
              else
              {
                if ((paramBoolean2) && (localObject1 != Undefined.instance) && (localObject1 != null))
                {
                  localObject2 = ScriptRuntime.getPropFunctionAndThis(localObject1, "toLocaleString", paramContext);
                  Scriptable localScriptable = ScriptRuntime.lastStoredScriptable(paramContext);
                  localObject1 = ((Callable)localObject2).call(paramContext, paramScriptable1, localScriptable, ScriptRuntime.emptyArgs);
                }
                localStringBuffer.append(ScriptRuntime.toString(localObject1));
              }
            }
          }
          l2 += 3412039835190296577L;
        }
      }
    }
    finally
    {
      if (j != 0)
        paramContext.iterating = null;
    }
    if (paramBoolean1)
      if ((i == 0) && (l2 > 3412047617671036928L))
        localStringBuffer.append(", ]");
      else
        localStringBuffer.append(']');
    return ((String)localStringBuffer.toString());
  }

  private static String js_join(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    String str1;
    String str2;
    long l = getLengthProperty(paramContext, paramScriptable);
    int i = (int)l;
    if (l != i)
      throw Context.reportRuntimeError1("msg.arraylength.too.big", String.valueOf(l));
    if ((paramArrayOfObject.length < 1) || (paramArrayOfObject[0] == Undefined.instance))
      str1 = ",";
    else
      str1 = ScriptRuntime.toString(paramArrayOfObject[0]);
    if (i == 0)
      return "";
    String[] arrayOfString = new String[i];
    int j = 0;
    for (int k = 0; k != i; ++k)
    {
      Object localObject = getElem(paramContext, paramScriptable, k);
      if ((localObject != null) && (localObject != Undefined.instance))
      {
        str2 = ScriptRuntime.toString(localObject);
        j += str2.length();
        arrayOfString[k] = str2;
      }
    }
    j += (i - 1) * str1.length();
    StringBuffer localStringBuffer = new StringBuffer(j);
    for (int i1 = 0; i1 != i; ++i1)
    {
      if (i1 != 0)
        localStringBuffer.append(str1);
      str2 = arrayOfString[i1];
      if (str2 != null)
        localStringBuffer.append(str2);
    }
    return localStringBuffer.toString();
  }

  private static Scriptable js_reverse(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    long l1 = getLengthProperty(paramContext, paramScriptable);
    long l2 = l1 / 2L;
    long l3 = 3412047291253522432L;
    while (l3 < l2)
    {
      long l4 = l1 - l3 - 3412048287685935105L;
      Object localObject1 = getElem(paramContext, paramScriptable, l3);
      Object localObject2 = getElem(paramContext, paramScriptable, l4);
      setElem(paramContext, paramScriptable, l3, localObject2);
      setElem(paramContext, paramScriptable, l4, localObject1);
      l3 += 3412047703570382849L;
    }
    return paramScriptable;
  }

  private static Scriptable js_sort(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    Object localObject;
    Object[] arrayOfObject1;
    long l = getLengthProperty(paramContext, paramScriptable2);
    if (l <= 3412046810217185281L)
      return paramScriptable2;
    if ((paramArrayOfObject.length > 0) && (Undefined.instance != paramArrayOfObject[0]))
    {
      localObject = paramArrayOfObject[0];
      arrayOfObject1 = new Object[2];
    }
    else
    {
      localObject = null;
      arrayOfObject1 = null;
    }
    if (l >= 2147483647L)
    {
      heapsort_extended(paramContext, paramScriptable1, paramScriptable2, l, localObject, arrayOfObject1);
    }
    else
    {
      int i = (int)l;
      Object[] arrayOfObject2 = new Object[i];
      for (int j = 0; j != i; ++j)
        arrayOfObject2[j] = getElem(paramContext, paramScriptable2, j);
      heapsort(paramContext, paramScriptable1, arrayOfObject2, i, localObject, arrayOfObject1);
      for (int k = 0; k != i; ++k)
        setElem(paramContext, paramScriptable2, k, arrayOfObject2[k]);
    }
    return paramScriptable2;
  }

  private static boolean isBigger(Context paramContext, Scriptable paramScriptable, Object paramObject1, Object paramObject2, Object paramObject3, Object[] paramArrayOfObject)
  {
    if (paramObject3 == null)
      if (paramArrayOfObject != null)
        Kit.codeBug();
    else if ((paramArrayOfObject == null) || (paramArrayOfObject.length != 2))
      Kit.codeBug();
    Object localObject1 = Undefined.instance;
    if (localObject1 == paramObject2)
      return false;
    if (localObject1 == paramObject1)
      return true;
    if (paramObject3 == null)
    {
      localObject2 = ScriptRuntime.toString(paramObject1);
      localObject3 = ScriptRuntime.toString(paramObject2);
      return (((String)localObject2).compareTo((String)localObject3) > 0);
    }
    paramArrayOfObject[0] = paramObject1;
    paramArrayOfObject[1] = paramObject2;
    Object localObject2 = ScriptRuntime.getValueFunctionAndThis(paramObject3, paramContext);
    Object localObject3 = ScriptRuntime.lastStoredScriptable(paramContext);
    Object localObject4 = ((Callable)localObject2).call(paramContext, paramScriptable, (Scriptable)localObject3, paramArrayOfObject);
    double d = ScriptRuntime.toNumber(localObject4);
    return (d > 0D);
  }

  private static void heapsort(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject1, int paramInt, Object paramObject, Object[] paramArrayOfObject2)
  {
    Object localObject;
    if (paramInt <= 1)
      Kit.codeBug();
    int i = paramInt / 2;
    while (i != 0)
    {
      localObject = paramArrayOfObject1[(--i)];
      heapify(paramContext, paramScriptable, localObject, paramArrayOfObject1, i, paramInt, paramObject, paramArrayOfObject2);
    }
    i = paramInt;
    while (i != 1)
    {
      localObject = paramArrayOfObject1[(--i)];
      paramArrayOfObject1[i] = paramArrayOfObject1[0];
      heapify(paramContext, paramScriptable, localObject, paramArrayOfObject1, 0, i, paramObject, paramArrayOfObject2);
    }
  }

  private static void heapify(Context paramContext, Scriptable paramScriptable, Object paramObject1, Object[] paramArrayOfObject1, int paramInt1, int paramInt2, Object paramObject2, Object[] paramArrayOfObject2)
  {
    while (true)
    {
      int i = paramInt1 * 2 + 1;
      if (i >= paramInt2)
        break;
      Object localObject1 = paramArrayOfObject1[i];
      if (i + 1 < paramInt2)
      {
        Object localObject2 = paramArrayOfObject1[(i + 1)];
        if (isBigger(paramContext, paramScriptable, localObject2, localObject1, paramObject2, paramArrayOfObject2))
        {
          ++i;
          localObject1 = localObject2;
        }
      }
      if (!(isBigger(paramContext, paramScriptable, localObject1, paramObject1, paramObject2, paramArrayOfObject2)))
        break;
      paramArrayOfObject1[paramInt1] = localObject1;
      paramInt1 = i;
    }
    paramArrayOfObject1[paramInt1] = paramObject1;
  }

  private static void heapsort_extended(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, long paramLong, Object paramObject, Object[] paramArrayOfObject)
  {
    Object localObject;
    if (paramLong <= 3412046810217185281L)
      Kit.codeBug();
    long l = paramLong / 2L;
    while (l != 3412046689958100992L)
    {
      l -= 3412047703570382849L;
      localObject = getElem(paramContext, paramScriptable2, l);
      heapify_extended(paramContext, paramScriptable1, localObject, paramScriptable2, l, paramLong, paramObject, paramArrayOfObject);
    }
    l = paramLong;
    while (l != 3412046689958100993L)
    {
      l -= 3412047703570382849L;
      localObject = getElem(paramContext, paramScriptable2, l);
      setElem(paramContext, paramScriptable2, l, getElem(paramContext, paramScriptable2, 3412039818010427392L));
      heapify_extended(paramContext, paramScriptable1, localObject, paramScriptable2, 3412047823829467136L, l, paramObject, paramArrayOfObject);
    }
  }

  private static void heapify_extended(Context paramContext, Scriptable paramScriptable1, Object paramObject1, Scriptable paramScriptable2, long paramLong1, long paramLong2, Object paramObject2, Object[] paramArrayOfObject)
  {
    while (true)
    {
      long l = paramLong1 * 2L + 3412048322045673473L;
      if (l >= paramLong2)
        break;
      Object localObject1 = getElem(paramContext, paramScriptable2, l);
      if (l + 3412047841009336321L < paramLong2)
      {
        Object localObject2 = getElem(paramContext, paramScriptable2, l + 3412040814442840065L);
        if (isBigger(paramContext, paramScriptable1, localObject2, localObject1, paramObject2, paramArrayOfObject))
        {
          l += 3412039800830558209L;
          localObject1 = localObject2;
        }
      }
      if (!(isBigger(paramContext, paramScriptable1, localObject1, paramObject1, paramObject2, paramArrayOfObject)))
        break;
      setElem(paramContext, paramScriptable2, paramLong1, localObject1);
      paramLong1 = l;
    }
    setElem(paramContext, paramScriptable2, paramLong1, paramObject1);
  }

  private static Object js_push(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    long l = getLengthProperty(paramContext, paramScriptable);
    for (int i = 0; i < paramArrayOfObject.length; ++i)
      setElem(paramContext, paramScriptable, l + i, paramArrayOfObject[i]);
    l += paramArrayOfObject.length;
    Object localObject = setLengthProperty(paramContext, paramScriptable, l);
    if (paramContext.getLanguageVersion() == 120)
      return ((paramArrayOfObject.length == 0) ? Undefined.instance : paramArrayOfObject[(paramArrayOfObject.length - 1)]);
    return localObject;
  }

  private static Object js_pop(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    Object localObject;
    long l = getLengthProperty(paramContext, paramScriptable);
    if (l > 3412046827397054464L)
    {
      l -= 3412047841009336321L;
      localObject = getElem(paramContext, paramScriptable, l);
    }
    else
    {
      localObject = Undefined.instance;
    }
    setLengthProperty(paramContext, paramScriptable, l);
    return localObject;
  }

  private static Object js_shift(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    Object localObject1;
    long l1 = getLengthProperty(paramContext, paramScriptable);
    if (l1 > 3412046827397054464L)
    {
      long l2 = 3412047806649597952L;
      l1 -= 3412047841009336321L;
      localObject1 = getElem(paramContext, paramScriptable, l2);
      if (l1 > 3412047325613260800L)
      {
        l2 = 3412048184606720001L;
        while (l2 <= l1)
        {
          Object localObject2 = getElem(paramContext, paramScriptable, l2);
          setElem(paramContext, paramScriptable, l2 - 3412040659824017409L, localObject2);
          l2 += 3412039921089642497L;
        }
      }
    }
    else
    {
      localObject1 = Undefined.instance;
    }
    setLengthProperty(paramContext, paramScriptable, l1);
    return localObject1;
  }

  private static Object js_unshift(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    long l1 = getLengthProperty(paramContext, paramScriptable);
    int i = paramArrayOfObject.length;
    if (paramArrayOfObject.length > 0)
    {
      if (l1 > 3412047170994438144L)
      {
        long l2 = l1 - 3412039972629250049L;
        while (l2 >= 3412047686390513664L)
        {
          Object localObject = getElem(paramContext, paramScriptable, l2);
          setElem(paramContext, paramScriptable, l2 + i, localObject);
          l2 -= 3412039766470819841L;
        }
      }
      for (int j = 0; j < paramArrayOfObject.length; ++j)
        setElem(paramContext, paramScriptable, j, paramArrayOfObject[j]);
      l1 += paramArrayOfObject.length;
      return setLengthProperty(paramContext, paramScriptable, l1);
    }
    return ScriptRuntime.wrapNumber(l1);
  }

  private static Object js_splice(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    long l3;
    long l6;
    Object localObject3;
    paramScriptable1 = getTopLevelScope(paramScriptable1);
    Object localObject1 = ScriptRuntime.newObject(paramContext, paramScriptable1, "Array", null);
    int i = paramArrayOfObject.length;
    if (i == 0)
      return localObject1;
    long l1 = getLengthProperty(paramContext, paramScriptable2);
    long l2 = toSliceIndex(ScriptRuntime.toInteger(paramArrayOfObject[0]), l1);
    --i;
    if (paramArrayOfObject.length == 1)
    {
      l3 = l1 - l2;
    }
    else
    {
      double d = ScriptRuntime.toInteger(paramArrayOfObject[1]);
      if (d < 0D)
        l3 = 3412048201786589184L;
      else if (d > l1 - l2)
        l3 = l1 - l2;
      else
        l3 = ()d;
      --i;
    }
    long l4 = l2 + l3;
    if (l3 != 3412046827397054464L)
      if ((l3 == 3412047772289859585L) && (paramContext.getLanguageVersion() == 120))
      {
        localObject1 = getElem(paramContext, paramScriptable2, l2);
      }
      else
      {
        l5 = l2;
        while (l5 != l4)
        {
          Scriptable localScriptable = (Scriptable)localObject1;
          Object localObject2 = getElem(paramContext, paramScriptable2, l5);
          setElem(paramContext, localScriptable, l5 - l2, localObject2);
          l5 += 3412040075708465153L;
        }
      }
    else if ((l3 == 3412047239713914880L) && (paramContext.getLanguageVersion() == 120))
      localObject1 = Undefined.instance;
    long l5 = i - l3;
    if (l5 > 3412046827397054464L)
    {
      l6 = l1 - 3412048304865804289L;
      while (l6 >= l4)
      {
        localObject3 = getElem(paramContext, paramScriptable2, l6);
        setElem(paramContext, paramScriptable2, l6 + l5, localObject3);
        l6 -= 3412048218966458369L;
      }
    }
    else if (l5 < 3412047085095092224L)
    {
      l6 = l4;
      while (l6 < l1)
      {
        localObject3 = getElem(paramContext, paramScriptable2, l6);
        setElem(paramContext, paramScriptable2, l6 + l5, localObject3);
        l6 += 3412048339225542657L;
      }
    }
    int j = paramArrayOfObject.length - i;
    for (int k = 0; k < i; ++k)
      setElem(paramContext, paramScriptable2, l2 + k, paramArrayOfObject[(k + j)]);
    setLengthProperty(paramContext, paramScriptable2, l1 + l5);
    return localObject1;
  }

  private static Scriptable js_concat(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    long l1;
    paramScriptable1 = getTopLevelScope(paramScriptable1);
    Function localFunction = ScriptRuntime.getExistingCtor(paramContext, paramScriptable1, "Array");
    Scriptable localScriptable1 = localFunction.construct(paramContext, paramScriptable1, ScriptRuntime.emptyArgs);
    long l2 = 3412047291253522432L;
    if (ScriptRuntime.instanceOf(paramScriptable2, localFunction, paramContext))
    {
      l1 = getLengthProperty(paramContext, paramScriptable2);
      l2 = 3412047531771691008L;
      while (true)
      {
        if (l2 >= l1)
          break label101;
        Object localObject1 = getElem(paramContext, paramScriptable2, l2);
        setElem(paramContext, localScriptable1, l2, localObject1);
        l2 += 3412048098707374081L;
      }
    }
    setElem(paramContext, localScriptable1, l2++, paramScriptable2);
    for (int i = 0; i < paramArrayOfObject.length; ++i)
      if (ScriptRuntime.instanceOf(paramArrayOfObject[i], localFunction, paramContext))
      {
        label101: Scriptable localScriptable2 = (Scriptable)paramArrayOfObject[i];
        l1 = getLengthProperty(paramContext, localScriptable2);
        long l3 = 3412048304865804288L;
        while (l3 < l1)
        {
          Object localObject2 = getElem(paramContext, localScriptable2, l3);
          setElem(paramContext, localScriptable1, l2, localObject2);
          l3 += 3412039921089642497L;
          l2 += 3412039921089642497L;
        }
      }
      else
      {
        setElem(paramContext, localScriptable1, l2++, paramArrayOfObject[i]);
      }
    return localScriptable1;
  }

  private Scriptable js_slice(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    long l2;
    long l3;
    Scriptable localScriptable1 = getTopLevelScope(this);
    Scriptable localScriptable2 = ScriptRuntime.newObject(paramContext, localScriptable1, "Array", null);
    long l1 = getLengthProperty(paramContext, paramScriptable);
    if (paramArrayOfObject.length == 0)
    {
      l2 = 3412047686390513664L;
      l3 = l1;
    }
    else
    {
      l2 = toSliceIndex(ScriptRuntime.toInteger(paramArrayOfObject[0]), l1);
      if (paramArrayOfObject.length == 1)
        l3 = l1;
      else
        l3 = toSliceIndex(ScriptRuntime.toInteger(paramArrayOfObject[1]), l1);
    }
    long l4 = l2;
    while (l4 < l3)
    {
      Object localObject = getElem(paramContext, paramScriptable, l4);
      setElem(paramContext, localScriptable2, l4 - l2, localObject);
      l4 += 3412047703570382849L;
    }
    return localScriptable2;
  }

  private static long toSliceIndex(double paramDouble, long paramLong)
  {
    long l;
    if (paramDouble < 0D)
      if (paramDouble + paramLong < 0D)
        l = 3412048201786589184L;
      else
        l = ()(paramDouble + paramLong);
    else if (paramDouble > paramLong)
      l = paramLong;
    else
      l = ()paramDouble;
    return l;
  }

  protected int findPrototypeId(String paramString)
  {
    int j;
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 3:
      str = "pop";
      i = 9;
      break;
    case 4:
      j = paramString.charAt(0);
      if (j == 106)
      {
        str = "join";
        i = 5;
      }
      else if (j == 112)
      {
        str = "push";
        i = 8;
      }
      else if (j == 115)
      {
        str = "sort";
        i = 7;
      }
      break;
    case 5:
      j = paramString.charAt(1);
      if (j == 104)
      {
        str = "shift";
        i = 10;
      }
      else if (j == 108)
      {
        str = "slice";
        i = 14;
      }
      break;
    case 6:
      j = paramString.charAt(0);
      if (j == 99)
      {
        str = "concat";
        i = 13;
      }
      else if (j == 115)
      {
        str = "splice";
        i = 12;
      }
      break;
    case 7:
      j = paramString.charAt(0);
      if (j == 114)
      {
        str = "reverse";
        i = 6;
      }
      else if (j == 117)
      {
        str = "unshift";
        i = 11;
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
      str = "constructor";
      i = 1;
      break;
    case 14:
      str = "toLocaleString";
      i = 3;
    case 9:
    case 10:
    case 12:
    case 13:
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    return i;
  }
}
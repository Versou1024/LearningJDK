package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class IdScriptableObject extends ScriptableObject
  implements IdFunctionCall
{
  private volatile transient PrototypeValues prototypeValues;

  public IdScriptableObject()
  {
  }

  public IdScriptableObject(Scriptable paramScriptable1, Scriptable paramScriptable2)
  {
    super(paramScriptable1, paramScriptable2);
  }

  protected final Object defaultGet(String paramString)
  {
    return super.get(paramString, this);
  }

  protected final void defaultPut(String paramString, Object paramObject)
  {
    super.put(paramString, this, paramObject);
  }

  public boolean has(String paramString, Scriptable paramScriptable)
  {
    int j;
    int i = findInstanceIdInfo(paramString);
    if (i != 0)
    {
      j = i >>> 16;
      if ((j & 0x4) != 0)
        return true;
      int k = i & 0xFFFF;
      return (NOT_FOUND != getInstanceIdValue(k));
    }
    if (this.prototypeValues != null)
    {
      j = this.prototypeValues.findId(paramString);
      if (j != 0)
        return this.prototypeValues.has(j);
    }
    return super.has(paramString, paramScriptable);
  }

  public Object get(String paramString, Scriptable paramScriptable)
  {
    int j;
    int i = findInstanceIdInfo(paramString);
    if (i != 0)
    {
      j = i & 0xFFFF;
      return getInstanceIdValue(j);
    }
    if (this.prototypeValues != null)
    {
      j = this.prototypeValues.findId(paramString);
      if (j != 0)
        return this.prototypeValues.get(j);
    }
    return super.get(paramString, paramScriptable);
  }

  public void put(String paramString, Scriptable paramScriptable, Object paramObject)
  {
    int j;
    int i = findInstanceIdInfo(paramString);
    if (i != 0)
    {
      if ((paramScriptable == this) && (isSealed()))
        throw Context.reportRuntimeError1("msg.modify.sealed", paramString);
      j = i >>> 16;
      if ((j & 0x1) == 0)
        if (paramScriptable == this)
        {
          int k = i & 0xFFFF;
          setInstanceIdValue(k, paramObject);
        }
        else
        {
          paramScriptable.put(paramString, paramScriptable, paramObject);
        }
      return;
    }
    if (this.prototypeValues != null)
    {
      j = this.prototypeValues.findId(paramString);
      if (j != 0)
      {
        if ((paramScriptable == this) && (isSealed()))
          throw Context.reportRuntimeError1("msg.modify.sealed", paramString);
        this.prototypeValues.set(j, paramScriptable, paramObject);
        return;
      }
    }
    super.put(paramString, paramScriptable, paramObject);
  }

  public void delete(String paramString)
  {
    int j;
    int i = findInstanceIdInfo(paramString);
    if ((i != 0) && (!(isSealed())))
    {
      j = i >>> 16;
      if ((j & 0x4) == 0)
      {
        int k = i & 0xFFFF;
        setInstanceIdValue(k, NOT_FOUND);
      }
      return;
    }
    if (this.prototypeValues != null)
    {
      j = this.prototypeValues.findId(paramString);
      if (j != 0)
      {
        if (!(isSealed()))
          this.prototypeValues.delete(j);
        return;
      }
    }
    super.delete(paramString);
  }

  public int getAttributes(String paramString)
  {
    int j;
    int i = findInstanceIdInfo(paramString);
    if (i != 0)
    {
      j = i >>> 16;
      return j;
    }
    if (this.prototypeValues != null)
    {
      j = this.prototypeValues.findId(paramString);
      if (j != 0)
        return this.prototypeValues.getAttributes(j);
    }
    return super.getAttributes(paramString);
  }

  public void setAttributes(String paramString, int paramInt)
  {
    int j;
    ScriptableObject.checkValidAttributes(paramInt);
    int i = findInstanceIdInfo(paramString);
    if (i != 0)
    {
      j = i >>> 16;
      if (paramInt != j)
        throw new RuntimeException("Change of attributes for this id is not supported");
      return;
    }
    if (this.prototypeValues != null)
    {
      j = this.prototypeValues.findId(paramString);
      if (j != 0)
      {
        this.prototypeValues.setAttributes(j, paramInt);
        return;
      }
    }
    super.setAttributes(paramString, paramInt);
  }

  Object[] getIds(boolean paramBoolean)
  {
    Object localObject = super.getIds(paramBoolean);
    if (this.prototypeValues != null)
      localObject = this.prototypeValues.getNames(paramBoolean, localObject);
    int i = getMaxInstanceId();
    if (i != 0)
    {
      Object[] arrayOfObject1 = null;
      int j = 0;
      for (int k = i; k != 0; --k)
      {
        String str = getInstanceIdName(k);
        int l = findInstanceIdInfo(str);
        if (l != 0)
        {
          int i1 = l >>> 16;
          if (((i1 & 0x4) == 0) && (NOT_FOUND == getInstanceIdValue(k)))
            break label129:
          if ((paramBoolean) || ((i1 & 0x2) == 0))
          {
            if (j == 0)
              arrayOfObject1 = new Object[k];
            label129: arrayOfObject1[(j++)] = str;
          }
        }
      }
      if (j != 0)
        if ((localObject.length == 0) && (arrayOfObject1.length == j))
        {
          localObject = arrayOfObject1;
        }
        else
        {
          Object[] arrayOfObject2 = new Object[localObject.length + j];
          System.arraycopy(localObject, 0, arrayOfObject2, 0, localObject.length);
          System.arraycopy(arrayOfObject1, 0, arrayOfObject2, localObject.length, j);
          localObject = arrayOfObject2;
        }
    }
    return ((Object)localObject);
  }

  protected int getMaxInstanceId()
  {
    return 0;
  }

  protected static int instanceIdInfo(int paramInt1, int paramInt2)
  {
    return (paramInt1 << 16 | paramInt2);
  }

  protected int findInstanceIdInfo(String paramString)
  {
    return 0;
  }

  protected String getInstanceIdName(int paramInt)
  {
    throw new IllegalArgumentException(String.valueOf(paramInt));
  }

  protected Object getInstanceIdValue(int paramInt)
  {
    throw new IllegalStateException(String.valueOf(paramInt));
  }

  protected void setInstanceIdValue(int paramInt, Object paramObject)
  {
    throw new IllegalStateException(String.valueOf(paramInt));
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    throw paramIdFunctionObject.unknown();
  }

  public final IdFunctionObject exportAsJSClass(int paramInt, Scriptable paramScriptable, boolean paramBoolean)
  {
    if ((paramScriptable != this) && (paramScriptable != null))
    {
      setParentScope(paramScriptable);
      setPrototype(getObjectPrototype(paramScriptable));
    }
    activatePrototypeMap(paramInt);
    IdFunctionObject localIdFunctionObject = this.prototypeValues.createPrecachedConstructor();
    if (paramBoolean)
      sealObject();
    fillConstructorProperties(localIdFunctionObject);
    if (paramBoolean)
      localIdFunctionObject.sealObject();
    localIdFunctionObject.exportAsScopeProperty();
    return localIdFunctionObject;
  }

  public final boolean hasPrototypeMap()
  {
    return (this.prototypeValues != null);
  }

  public final void activatePrototypeMap(int paramInt)
  {
    PrototypeValues localPrototypeValues = new PrototypeValues(this, paramInt);
    synchronized (this)
    {
      if (this.prototypeValues != null)
        throw new IllegalStateException();
      this.prototypeValues = localPrototypeValues;
    }
  }

  public final void initPrototypeMethod(Object paramObject, int paramInt1, String paramString, int paramInt2)
  {
    Scriptable localScriptable = ScriptableObject.getTopLevelScope(this);
    IdFunctionObject localIdFunctionObject = newIdFunction(paramObject, paramInt1, paramString, paramInt2, localScriptable);
    this.prototypeValues.initValue(paramInt1, paramString, localIdFunctionObject, 2);
  }

  public final void initPrototypeConstructor(IdFunctionObject paramIdFunctionObject)
  {
    int i = this.prototypeValues.constructorId;
    if (i == 0)
      throw new IllegalStateException();
    if (paramIdFunctionObject.methodId() != i)
      throw new IllegalArgumentException();
    if (isSealed())
      paramIdFunctionObject.sealObject();
    this.prototypeValues.initValue(i, "constructor", paramIdFunctionObject, 2);
  }

  public final void initPrototypeValue(int paramInt1, String paramString, Object paramObject, int paramInt2)
  {
    this.prototypeValues.initValue(paramInt1, paramString, paramObject, paramInt2);
  }

  protected void initPrototypeId(int paramInt)
  {
    throw new IllegalStateException(String.valueOf(paramInt));
  }

  protected int findPrototypeId(String paramString)
  {
    throw new IllegalStateException(paramString);
  }

  protected void fillConstructorProperties(IdFunctionObject paramIdFunctionObject)
  {
  }

  protected void addIdFunctionProperty(Scriptable paramScriptable, Object paramObject, int paramInt1, String paramString, int paramInt2)
  {
    Scriptable localScriptable = ScriptableObject.getTopLevelScope(paramScriptable);
    IdFunctionObject localIdFunctionObject = newIdFunction(paramObject, paramInt1, paramString, paramInt2, localScriptable);
    localIdFunctionObject.addAsProperty(paramScriptable);
  }

  protected static EcmaError incompatibleCallError(IdFunctionObject paramIdFunctionObject)
  {
    throw ScriptRuntime.typeError1("msg.incompat.call", paramIdFunctionObject.getFunctionName());
  }

  private IdFunctionObject newIdFunction(Object paramObject, int paramInt1, String paramString, int paramInt2, Scriptable paramScriptable)
  {
    IdFunctionObject localIdFunctionObject = new IdFunctionObject(this, paramObject, paramInt1, paramString, paramInt2, paramScriptable);
    if (isSealed())
      localIdFunctionObject.sealObject();
    return localIdFunctionObject;
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    int i = paramObjectInputStream.readInt();
    if (i != 0)
      activatePrototypeMap(i);
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    paramObjectOutputStream.defaultWriteObject();
    int i = 0;
    if (this.prototypeValues != null)
      i = this.prototypeValues.getMaxId();
    paramObjectOutputStream.writeInt(i);
  }

  private static final class PrototypeValues
  implements Serializable
  {
    static final long serialVersionUID = 3038645279153854371L;
    private static final int VALUE_SLOT = 0;
    private static final int NAME_SLOT = 1;
    private static final int SLOT_SPAN = 2;
    private IdScriptableObject obj;
    private Object tag;
    private int maxId;
    private volatile Object[] valueArray;
    private volatile short[] attributeArray;
    private volatile int lastFoundId = 1;
    int constructorId;
    private IdFunctionObject constructor;
    private short constructorAttrs;

    PrototypeValues(IdScriptableObject paramIdScriptableObject, int paramInt)
    {
      if (paramIdScriptableObject == null)
        throw new IllegalArgumentException();
      if (paramInt < 1)
        throw new IllegalArgumentException();
      this.obj = paramIdScriptableObject;
      this.maxId = paramInt;
    }

    final int getMaxId()
    {
      return this.maxId;
    }

    final void initValue(int paramInt1, String paramString, Object paramObject, int paramInt2)
    {
      if ((1 > paramInt1) || (paramInt1 > this.maxId))
        throw new IllegalArgumentException();
      if (paramString == null)
        throw new IllegalArgumentException();
      if (paramObject == Scriptable.NOT_FOUND)
        throw new IllegalArgumentException();
      ScriptableObject.checkValidAttributes(paramInt2);
      if (this.obj.findPrototypeId(paramString) != paramInt1)
        throw new IllegalArgumentException(paramString);
      if (paramInt1 == this.constructorId)
      {
        if (!(paramObject instanceof IdFunctionObject))
          throw new IllegalArgumentException("consructor should be initialized with IdFunctionObject");
        this.constructor = ((IdFunctionObject)paramObject);
        this.constructorAttrs = (short)paramInt2;
        return;
      }
      initSlot(paramInt1, paramString, paramObject, paramInt2);
    }

    private void initSlot(int paramInt1, String paramString, Object paramObject, int paramInt2)
    {
      Object[] arrayOfObject = this.valueArray;
      if (arrayOfObject == null)
        throw new IllegalStateException();
      if (paramObject == null)
        paramObject = UniqueTag.NULL_VALUE;
      int i = (paramInt1 - 1) * 2;
      synchronized (this)
      {
        Object localObject1 = arrayOfObject[(i + 0)];
        if (localObject1 == null)
        {
          arrayOfObject[(i + 0)] = paramObject;
          arrayOfObject[(i + 1)] = paramString;
          this.attributeArray[(paramInt1 - 1)] = (short)paramInt2;
        }
        else if (!(paramString.equals(arrayOfObject[(i + 1)])))
        {
          throw new IllegalStateException();
        }
      }
    }

    final IdFunctionObject createPrecachedConstructor()
    {
      if (this.constructorId != 0)
        throw new IllegalStateException();
      this.constructorId = this.obj.findPrototypeId("constructor");
      if (this.constructorId == 0)
        throw new IllegalStateException("No id for constructor property");
      this.obj.initPrototypeId(this.constructorId);
      if (this.constructor == null)
        throw new IllegalStateException(this.obj.getClass().getName() + ".initPrototypeId() did not " + "initialize id=" + this.constructorId);
      this.constructor.initFunction(this.obj.getClassName(), ScriptableObject.getTopLevelScope(this.obj));
      this.constructor.markAsConstructor(this.obj);
      return this.constructor;
    }

    final int findId(String paramString)
    {
      Object[] arrayOfObject = this.valueArray;
      if (arrayOfObject == null)
        return this.obj.findPrototypeId(paramString);
      int i = this.lastFoundId;
      if (paramString == arrayOfObject[((i - 1) * 2 + 1)])
        return i;
      i = this.obj.findPrototypeId(paramString);
      if (i != 0)
      {
        int j = (i - 1) * 2 + 1;
        arrayOfObject[j] = paramString;
        this.lastFoundId = i;
      }
      return i;
    }

    final boolean has(int paramInt)
    {
      Object[] arrayOfObject = this.valueArray;
      if (arrayOfObject == null)
        return true;
      int i = (paramInt - 1) * 2 + 0;
      Object localObject = arrayOfObject[i];
      if (localObject == null)
        return true;
      return (localObject != Scriptable.NOT_FOUND);
    }

    final Object get(int paramInt)
    {
      Object localObject = ensureId(paramInt);
      if (localObject == UniqueTag.NULL_VALUE)
        localObject = null;
      return localObject;
    }

    final void set(int paramInt, Scriptable paramScriptable, Object paramObject)
    {
      if (paramObject == Scriptable.NOT_FOUND)
        throw new IllegalArgumentException();
      ensureId(paramInt);
      int i = this.attributeArray[(paramInt - 1)];
      if ((i & 0x1) == 0)
      {
        int j;
        if (paramScriptable == this.obj)
        {
          if (paramObject == null)
            paramObject = UniqueTag.NULL_VALUE;
          j = (paramInt - 1) * 2 + 0;
          synchronized (this)
          {
            this.valueArray[j] = paramObject;
          }
        }
        else
        {
          j = (paramInt - 1) * 2 + 1;
          ??? = (String)this.valueArray[j];
          paramScriptable.put((String)???, paramScriptable, paramObject);
        }
      }
    }

    final void delete(int paramInt)
    {
      ensureId(paramInt);
      int i = this.attributeArray[(paramInt - 1)];
      if ((i & 0x4) == 0)
      {
        int j = (paramInt - 1) * 2 + 0;
        synchronized (this)
        {
          this.valueArray[j] = Scriptable.NOT_FOUND;
          this.attributeArray[(paramInt - 1)] = 0;
        }
      }
    }

    final int getAttributes(int paramInt)
    {
      ensureId(paramInt);
      return this.attributeArray[(paramInt - 1)];
    }

    final void setAttributes(int paramInt1, int paramInt2)
    {
      ScriptableObject.checkValidAttributes(paramInt2);
      ensureId(paramInt1);
      synchronized (this)
      {
        this.attributeArray[(paramInt1 - 1)] = (short)paramInt2;
      }
    }

    final Object[] getNames(boolean paramBoolean, Object[] paramArrayOfObject)
    {
      Object localObject1 = null;
      int i = 0;
      for (int j = 1; j <= this.maxId; ++j)
      {
        localObject2 = ensureId(j);
        if ((((paramBoolean) || ((this.attributeArray[(j - 1)] & 0x2) == 0))) && (localObject2 != Scriptable.NOT_FOUND))
        {
          int l = (j - 1) * 2 + 1;
          String str = (String)this.valueArray[l];
          if (localObject1 == null)
            localObject1 = new Object[this.maxId];
          localObject1[(i++)] = str;
        }
      }
      if (i == 0)
        return paramArrayOfObject;
      if ((paramArrayOfObject == null) || (paramArrayOfObject.length == 0))
      {
        if (i != localObject1.length)
        {
          Object[] arrayOfObject = new Object[i];
          System.arraycopy(localObject1, 0, arrayOfObject, 0, i);
          localObject1 = arrayOfObject;
        }
        return localObject1;
      }
      int k = paramArrayOfObject.length;
      Object localObject2 = new Object[k + i];
      System.arraycopy(paramArrayOfObject, 0, localObject2, 0, k);
      System.arraycopy(localObject1, 0, localObject2, k, i);
      return ((Object)(Object)localObject2);
    }

    private Object ensureId(int paramInt)
    {
      Object[] arrayOfObject = this.valueArray;
      if (arrayOfObject == null)
        synchronized (this)
        {
          arrayOfObject = this.valueArray;
          if (arrayOfObject == null)
          {
            arrayOfObject = new Object[this.maxId * 2];
            this.valueArray = arrayOfObject;
            this.attributeArray = new short[this.maxId];
          }
        }
      int i = (paramInt - 1) * 2 + 0;
      Object localObject2 = arrayOfObject[i];
      if (localObject2 == null)
      {
        if (paramInt == this.constructorId)
        {
          initSlot(this.constructorId, "constructor", this.constructor, this.constructorAttrs);
          this.constructor = null;
        }
        else
        {
          this.obj.initPrototypeId(paramInt);
        }
        localObject2 = arrayOfObject[i];
        if (localObject2 == null)
          throw new IllegalStateException(this.obj.getClass().getName() + ".initPrototypeId(int id) " + "did not initialize id=" + paramInt);
      }
      return localObject2;
    }
  }
}
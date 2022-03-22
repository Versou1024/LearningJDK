package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjArray
  implements Serializable
{
  static final long serialVersionUID = 4174889037736658296L;
  private int size;
  private boolean sealed;
  private static final int FIELDS_STORE_SIZE = 5;
  private transient Object f0;
  private transient Object f1;
  private transient Object f2;
  private transient Object f3;
  private transient Object f4;
  private transient Object[] data;

  public final boolean isSealed()
  {
    return this.sealed;
  }

  public final void seal()
  {
    this.sealed = true;
  }

  public final boolean isEmpty()
  {
    return (this.size == 0);
  }

  public final int size()
  {
    return this.size;
  }

  public final void setSize(int paramInt)
  {
    if (paramInt < 0)
      throw new IllegalArgumentException();
    if (this.sealed)
      throw onSeledMutation();
    int i = this.size;
    if (paramInt < i)
      for (int j = paramInt; j != i; ++j)
        setImpl(j, null);
    else if ((paramInt > i) && (paramInt > 5))
      ensureCapacity(paramInt);
    this.size = paramInt;
  }

  public final Object get(int paramInt)
  {
    if ((0 > paramInt) || (paramInt >= this.size))
      throw onInvalidIndex(paramInt, this.size);
    return getImpl(paramInt);
  }

  public final void set(int paramInt, Object paramObject)
  {
    if ((0 > paramInt) || (paramInt >= this.size))
      throw onInvalidIndex(paramInt, this.size);
    if (this.sealed)
      throw onSeledMutation();
    setImpl(paramInt, paramObject);
  }

  private Object getImpl(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return this.f0;
    case 1:
      return this.f1;
    case 2:
      return this.f2;
    case 3:
      return this.f3;
    case 4:
      return this.f4;
    }
    return this.data[(paramInt - 5)];
  }

  private void setImpl(int paramInt, Object paramObject)
  {
    switch (paramInt)
    {
    case 0:
      this.f0 = paramObject;
      break;
    case 1:
      this.f1 = paramObject;
      break;
    case 2:
      this.f2 = paramObject;
      break;
    case 3:
      this.f3 = paramObject;
      break;
    case 4:
      this.f4 = paramObject;
      break;
    default:
      this.data[(paramInt - 5)] = paramObject;
    }
  }

  public int indexOf(Object paramObject)
  {
    int i = this.size;
    for (int j = 0; j != i; ++j)
    {
      Object localObject = getImpl(j);
      if ((localObject == paramObject) || ((localObject != null) && (localObject.equals(paramObject))))
        return j;
    }
    return -1;
  }

  public int lastIndexOf(Object paramObject)
  {
    int i = this.size;
    while (i != 0)
    {
      Object localObject = getImpl(--i);
      if ((localObject == paramObject) || ((localObject != null) && (localObject.equals(paramObject))))
        return i;
    }
    return -1;
  }

  public final Object peek()
  {
    int i = this.size;
    if (i == 0)
      throw onEmptyStackTopRead();
    return getImpl(i - 1);
  }

  public final Object pop()
  {
    Object localObject;
    if (this.sealed)
      throw onSeledMutation();
    int i = this.size;
    switch (--i)
    {
    case -1:
      throw onEmptyStackTopRead();
    case 0:
      localObject = this.f0;
      this.f0 = null;
      break;
    case 1:
      localObject = this.f1;
      this.f1 = null;
      break;
    case 2:
      localObject = this.f2;
      this.f2 = null;
      break;
    case 3:
      localObject = this.f3;
      this.f3 = null;
      break;
    case 4:
      localObject = this.f4;
      this.f4 = null;
      break;
    default:
      localObject = this.data[(i - 5)];
      this.data[(i - 5)] = null;
    }
    this.size = i;
    return localObject;
  }

  public final void push(Object paramObject)
  {
    add(paramObject);
  }

  public final void add(Object paramObject)
  {
    if (this.sealed)
      throw onSeledMutation();
    int i = this.size;
    if (i >= 5)
      ensureCapacity(i + 1);
    this.size = (i + 1);
    setImpl(i, paramObject);
  }

  public final void add(int paramInt, Object paramObject)
  {
    Object localObject;
    int i = this.size;
    if ((0 > paramInt) || (paramInt > i))
      throw onInvalidIndex(paramInt, i + 1);
    if (this.sealed)
      throw onSeledMutation();
    switch (paramInt)
    {
    case 0:
      if (i == 0)
      {
        this.f0 = paramObject;
        break label247:
      }
      localObject = this.f0;
      this.f0 = paramObject;
      paramObject = localObject;
    case 1:
      if (i == 1)
      {
        this.f1 = paramObject;
        break label247:
      }
      localObject = this.f1;
      this.f1 = paramObject;
      paramObject = localObject;
    case 2:
      if (i == 2)
      {
        this.f2 = paramObject;
        break label247:
      }
      localObject = this.f2;
      this.f2 = paramObject;
      paramObject = localObject;
    case 3:
      if (i == 3)
      {
        this.f3 = paramObject;
        break label247:
      }
      localObject = this.f3;
      this.f3 = paramObject;
      paramObject = localObject;
    case 4:
      if (i == 4)
      {
        this.f4 = paramObject;
        break label247:
      }
      localObject = this.f4;
      this.f4 = paramObject;
      paramObject = localObject;
      paramInt = 5;
    }
    ensureCapacity(i + 1);
    if (paramInt != i)
      System.arraycopy(this.data, paramInt - 5, this.data, paramInt - 5 + 1, i - paramInt);
    this.data[(paramInt - 5)] = paramObject;
    label247: this.size = (i + 1);
  }

  public final void remove(int paramInt)
  {
    int i = this.size;
    if ((0 > paramInt) || (paramInt >= i))
      throw onInvalidIndex(paramInt, i);
    if (this.sealed)
      throw onSeledMutation();
    --i;
    switch (paramInt)
    {
    case 0:
      if (i == 0)
      {
        this.f0 = null;
        break label216:
      }
      this.f0 = this.f1;
    case 1:
      if (i == 1)
      {
        this.f1 = null;
        break label216:
      }
      this.f1 = this.f2;
    case 2:
      if (i == 2)
      {
        this.f2 = null;
        break label216:
      }
      this.f2 = this.f3;
    case 3:
      if (i == 3)
      {
        this.f3 = null;
        break label216:
      }
      this.f3 = this.f4;
    case 4:
      if (i == 4)
      {
        this.f4 = null;
        break label216:
      }
      this.f4 = this.data[0];
      paramInt = 5;
    }
    if (paramInt != i)
      System.arraycopy(this.data, paramInt - 5 + 1, this.data, paramInt - 5, i - paramInt);
    this.data[(i - 5)] = null;
    label216: this.size = i;
  }

  public final void clear()
  {
    if (this.sealed)
      throw onSeledMutation();
    int i = this.size;
    for (int j = 0; j != i; ++j)
      setImpl(j, null);
    this.size = 0;
  }

  public final Object[] toArray()
  {
    Object[] arrayOfObject = new Object[this.size];
    toArray(arrayOfObject, 0);
    return arrayOfObject;
  }

  public final void toArray(Object[] paramArrayOfObject)
  {
    toArray(paramArrayOfObject, 0);
  }

  public final void toArray(Object[] paramArrayOfObject, int paramInt)
  {
    int i = this.size;
    switch (i)
    {
    default:
      System.arraycopy(this.data, 0, paramArrayOfObject, paramInt + 5, i - 5);
    case 5:
      paramArrayOfObject[(paramInt + 4)] = this.f4;
    case 4:
      paramArrayOfObject[(paramInt + 3)] = this.f3;
    case 3:
      paramArrayOfObject[(paramInt + 2)] = this.f2;
    case 2:
      paramArrayOfObject[(paramInt + 1)] = this.f1;
    case 1:
      paramArrayOfObject[(paramInt + 0)] = this.f0;
    case 0:
    }
  }

  private void ensureCapacity(int paramInt)
  {
    int j;
    int i = paramInt - 5;
    if (i <= 0)
      throw new IllegalArgumentException();
    if (this.data == null)
    {
      j = 10;
      if (j < i)
        j = i;
      this.data = new Object[j];
    }
    else
    {
      j = this.data.length;
      if (j < i)
      {
        if (j <= 5)
          j = 10;
        else
          j *= 2;
        if (j < i)
          j = i;
        Object[] arrayOfObject = new Object[j];
        if (this.size > 5)
          System.arraycopy(this.data, 0, arrayOfObject, 0, this.size - 5);
        this.data = arrayOfObject;
      }
    }
  }

  private static RuntimeException onInvalidIndex(int paramInt1, int paramInt2)
  {
    String str = paramInt1 + " ∉ [0, " + paramInt2 + ')';
    throw new IndexOutOfBoundsException(str);
  }

  private static RuntimeException onEmptyStackTopRead()
  {
    throw new RuntimeException("Empty stack");
  }

  private static RuntimeException onSeledMutation()
  {
    throw new IllegalStateException("Attempt to modify sealed array");
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    paramObjectOutputStream.defaultWriteObject();
    int i = this.size;
    for (int j = 0; j != i; ++j)
    {
      Object localObject = getImpl(j);
      paramObjectOutputStream.writeObject(localObject);
    }
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    int i = this.size;
    if (i > 5)
      this.data = new Object[i - 5];
    for (int j = 0; j != i; ++j)
    {
      Object localObject = paramObjectInputStream.readObject();
      setImpl(j, localObject);
    }
  }
}
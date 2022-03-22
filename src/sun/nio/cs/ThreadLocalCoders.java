package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class ThreadLocalCoders
{
  private static final int CACHE_SIZE = 3;
  private static Cache decoderCache = new Cache(3)
  {
    boolean hasName(Object paramObject1, Object paramObject2)
    {
      if (paramObject2 instanceof String)
        return ((CharsetDecoder)paramObject1).charset().name().equals(paramObject2);
      if (paramObject2 instanceof Charset)
        return ((CharsetDecoder)paramObject1).charset().equals(paramObject2);
      return false;
    }

    Object create(Object paramObject)
    {
      if (paramObject instanceof String)
        return Charset.forName((String)paramObject).newDecoder();
      if (paramObject instanceof Charset)
        return ((Charset)paramObject).newDecoder();
      if (!($assertionsDisabled))
        throw new AssertionError();
      return null;
    }
  };
  private static Cache encoderCache = new Cache(3)
  {
    boolean hasName(Object paramObject1, Object paramObject2)
    {
      if (paramObject2 instanceof String)
        return ((CharsetEncoder)paramObject1).charset().name().equals(paramObject2);
      if (paramObject2 instanceof Charset)
        return ((CharsetEncoder)paramObject1).charset().equals(paramObject2);
      return false;
    }

    Object create(Object paramObject)
    {
      if (paramObject instanceof String)
        return Charset.forName((String)paramObject).newEncoder();
      if (paramObject instanceof Charset)
        return ((Charset)paramObject).newEncoder();
      if (!($assertionsDisabled))
        throw new AssertionError();
      return null;
    }
  };

  public static CharsetDecoder decoderFor(Object paramObject)
  {
    CharsetDecoder localCharsetDecoder = (CharsetDecoder)decoderCache.forName(paramObject);
    localCharsetDecoder.reset();
    return localCharsetDecoder;
  }

  public static CharsetEncoder encoderFor(Object paramObject)
  {
    CharsetEncoder localCharsetEncoder = (CharsetEncoder)encoderCache.forName(paramObject);
    localCharsetEncoder.reset();
    return localCharsetEncoder;
  }

  private static abstract class Cache
  {
    private ThreadLocal cache = new ThreadLocal();
    private final int size;

    Cache(int paramInt)
    {
      this.size = paramInt;
    }

    abstract Object create(Object paramObject);

    private void moveToFront(Object[] paramArrayOfObject, int paramInt)
    {
      Object localObject = paramArrayOfObject[paramInt];
      for (int i = paramInt; i > 0; --i)
        paramArrayOfObject[i] = paramArrayOfObject[(i - 1)];
      paramArrayOfObject[0] = localObject;
    }

    abstract boolean hasName(Object paramObject1, Object paramObject2);

    Object forName(Object paramObject)
    {
      Object[] arrayOfObject = (Object[])(Object[])this.cache.get();
      if (arrayOfObject == null)
      {
        arrayOfObject = new Object[this.size];
        label81: this.cache.set(arrayOfObject);
      }
      else
      {
        for (int i = 0; i < arrayOfObject.length; ++i)
        {
          Object localObject2 = arrayOfObject[i];
          if (localObject2 == null)
            break label81:
          if (hasName(localObject2, paramObject))
          {
            if (i > 0)
              moveToFront(arrayOfObject, i);
            return localObject2;
          }
        }
      }
      Object localObject1 = create(paramObject);
      arrayOfObject[(arrayOfObject.length - 1)] = localObject1;
      moveToFront(arrayOfObject, arrayOfObject.length - 1);
      return localObject1;
    }
  }
}
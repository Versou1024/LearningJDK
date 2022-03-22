package sun.misc;

import java.util.ArrayList;

public abstract class ClassFileTransformer
{
  private static ArrayList transformerList = new ArrayList();
  private static Object[] transformers = new Object[0];

  public static void add(ClassFileTransformer paramClassFileTransformer)
  {
    synchronized (transformerList)
    {
      transformerList.add(paramClassFileTransformer);
      transformers = transformerList.toArray();
    }
  }

  public static Object[] getTransformers()
  {
    return transformers;
  }

  public abstract byte[] transform(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws ClassFormatError;
}
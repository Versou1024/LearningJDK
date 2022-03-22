package sun.misc;

import sun.nio.ch.Interruptible;
import sun.reflect.ConstantPool;
import sun.reflect.annotation.AnnotationType;

public abstract interface JavaLangAccess
{
  public abstract ConstantPool getConstantPool(Class paramClass);

  public abstract void setAnnotationType(Class paramClass, AnnotationType paramAnnotationType);

  public abstract AnnotationType getAnnotationType(Class paramClass);

  public abstract <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> paramClass);

  public abstract void blockedOn(Thread paramThread, Interruptible paramInterruptible);
}
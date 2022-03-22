package sun.reflect;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

abstract class UnsafeStaticFieldAccessorImpl extends UnsafeFieldAccessorImpl
{
  protected Object base;

  UnsafeStaticFieldAccessorImpl(Field paramField)
  {
    super(paramField);
    this.base = unsafe.staticFieldBase(paramField);
  }

  static
  {
    Reflection.registerFieldsToFilter(UnsafeStaticFieldAccessorImpl.class, new String[] { "base" });
  }
}
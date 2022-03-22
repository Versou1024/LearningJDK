package sun.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import sun.misc.Unsafe;

class UnsafeFieldAccessorFactory
{
  static FieldAccessor newFieldAccessor(Field paramField, boolean paramBoolean)
  {
    Class localClass = paramField.getType();
    boolean bool1 = Modifier.isStatic(paramField.getModifiers());
    boolean bool2 = Modifier.isFinal(paramField.getModifiers());
    boolean bool3 = Modifier.isVolatile(paramField.getModifiers());
    int i = ((bool2) || (bool3)) ? 1 : 0;
    boolean bool4 = (bool2) && (((bool1) || (!(paramBoolean))));
    if (bool1)
    {
      UnsafeFieldAccessorImpl.unsafe.ensureClassInitialized(paramField.getDeclaringClass());
      if (i == 0)
      {
        if (localClass == Boolean.TYPE)
          return new UnsafeStaticBooleanFieldAccessorImpl(paramField);
        if (localClass == Byte.TYPE)
          return new UnsafeStaticByteFieldAccessorImpl(paramField);
        if (localClass == Short.TYPE)
          return new UnsafeStaticShortFieldAccessorImpl(paramField);
        if (localClass == Character.TYPE)
          return new UnsafeStaticCharacterFieldAccessorImpl(paramField);
        if (localClass == Integer.TYPE)
          return new UnsafeStaticIntegerFieldAccessorImpl(paramField);
        if (localClass == Long.TYPE)
          return new UnsafeStaticLongFieldAccessorImpl(paramField);
        if (localClass == Float.TYPE)
          return new UnsafeStaticFloatFieldAccessorImpl(paramField);
        if (localClass == Double.TYPE)
          return new UnsafeStaticDoubleFieldAccessorImpl(paramField);
        return new UnsafeStaticObjectFieldAccessorImpl(paramField);
      }
      if (localClass == Boolean.TYPE)
        return new UnsafeQualifiedStaticBooleanFieldAccessorImpl(paramField, bool4);
      if (localClass == Byte.TYPE)
        return new UnsafeQualifiedStaticByteFieldAccessorImpl(paramField, bool4);
      if (localClass == Short.TYPE)
        return new UnsafeQualifiedStaticShortFieldAccessorImpl(paramField, bool4);
      if (localClass == Character.TYPE)
        return new UnsafeQualifiedStaticCharacterFieldAccessorImpl(paramField, bool4);
      if (localClass == Integer.TYPE)
        return new UnsafeQualifiedStaticIntegerFieldAccessorImpl(paramField, bool4);
      if (localClass == Long.TYPE)
        return new UnsafeQualifiedStaticLongFieldAccessorImpl(paramField, bool4);
      if (localClass == Float.TYPE)
        return new UnsafeQualifiedStaticFloatFieldAccessorImpl(paramField, bool4);
      if (localClass == Double.TYPE)
        return new UnsafeQualifiedStaticDoubleFieldAccessorImpl(paramField, bool4);
      return new UnsafeQualifiedStaticObjectFieldAccessorImpl(paramField, bool4);
    }
    if (i == 0)
    {
      if (localClass == Boolean.TYPE)
        return new UnsafeBooleanFieldAccessorImpl(paramField);
      if (localClass == Byte.TYPE)
        return new UnsafeByteFieldAccessorImpl(paramField);
      if (localClass == Short.TYPE)
        return new UnsafeShortFieldAccessorImpl(paramField);
      if (localClass == Character.TYPE)
        return new UnsafeCharacterFieldAccessorImpl(paramField);
      if (localClass == Integer.TYPE)
        return new UnsafeIntegerFieldAccessorImpl(paramField);
      if (localClass == Long.TYPE)
        return new UnsafeLongFieldAccessorImpl(paramField);
      if (localClass == Float.TYPE)
        return new UnsafeFloatFieldAccessorImpl(paramField);
      if (localClass == Double.TYPE)
        return new UnsafeDoubleFieldAccessorImpl(paramField);
      return new UnsafeObjectFieldAccessorImpl(paramField);
    }
    if (localClass == Boolean.TYPE)
      return new UnsafeQualifiedBooleanFieldAccessorImpl(paramField, bool4);
    if (localClass == Byte.TYPE)
      return new UnsafeQualifiedByteFieldAccessorImpl(paramField, bool4);
    if (localClass == Short.TYPE)
      return new UnsafeQualifiedShortFieldAccessorImpl(paramField, bool4);
    if (localClass == Character.TYPE)
      return new UnsafeQualifiedCharacterFieldAccessorImpl(paramField, bool4);
    if (localClass == Integer.TYPE)
      return new UnsafeQualifiedIntegerFieldAccessorImpl(paramField, bool4);
    if (localClass == Long.TYPE)
      return new UnsafeQualifiedLongFieldAccessorImpl(paramField, bool4);
    if (localClass == Float.TYPE)
      return new UnsafeQualifiedFloatFieldAccessorImpl(paramField, bool4);
    if (localClass == Double.TYPE)
      return new UnsafeQualifiedDoubleFieldAccessorImpl(paramField, bool4);
    return new UnsafeQualifiedObjectFieldAccessorImpl(paramField, bool4);
  }
}
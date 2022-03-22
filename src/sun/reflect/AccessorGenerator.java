package sun.reflect;

import java.lang.reflect.Modifier;
import sun.misc.Unsafe;

class AccessorGenerator
  implements ClassFileConstants
{
  static final Unsafe unsafe = Unsafe.getUnsafe();
  protected static final short S0 = 0;
  protected static final short S1 = 1;
  protected static final short S2 = 2;
  protected static final short S3 = 3;
  protected static final short S4 = 4;
  protected static final short S5 = 5;
  protected static final short S6 = 6;
  protected ClassFileAssembler asm;
  protected int modifiers;
  protected short thisClass;
  protected short superClass;
  protected short targetClass;
  protected short throwableClass;
  protected short classCastClass;
  protected short nullPointerClass;
  protected short illegalArgumentClass;
  protected short invocationTargetClass;
  protected short initIdx;
  protected short initNameAndTypeIdx;
  protected short initStringNameAndTypeIdx;
  protected short nullPointerCtorIdx;
  protected short illegalArgumentCtorIdx;
  protected short illegalArgumentStringCtorIdx;
  protected short invocationTargetCtorIdx;
  protected short superCtorIdx;
  protected short objectClass;
  protected short toStringIdx;
  protected short codeIdx;
  protected short exceptionsIdx;
  protected short booleanIdx;
  protected short booleanCtorIdx;
  protected short booleanUnboxIdx;
  protected short byteIdx;
  protected short byteCtorIdx;
  protected short byteUnboxIdx;
  protected short characterIdx;
  protected short characterCtorIdx;
  protected short characterUnboxIdx;
  protected short doubleIdx;
  protected short doubleCtorIdx;
  protected short doubleUnboxIdx;
  protected short floatIdx;
  protected short floatCtorIdx;
  protected short floatUnboxIdx;
  protected short integerIdx;
  protected short integerCtorIdx;
  protected short integerUnboxIdx;
  protected short longIdx;
  protected short longCtorIdx;
  protected short longUnboxIdx;
  protected short shortIdx;
  protected short shortCtorIdx;
  protected short shortUnboxIdx;
  protected final short NUM_COMMON_CPOOL_ENTRIES = 30;
  protected final short NUM_BOXING_CPOOL_ENTRIES = 72;
  protected static final Class[] primitiveTypes = { Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE };
  private ClassFileAssembler illegalArgumentCodeBuffer;

  protected void emitCommonConstantPoolEntries()
  {
    this.asm.emitConstantPoolUTF8("java/lang/Throwable");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.throwableClass = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/ClassCastException");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.classCastClass = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/NullPointerException");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.nullPointerClass = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/IllegalArgumentException");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.illegalArgumentClass = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/reflect/InvocationTargetException");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.invocationTargetClass = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("<init>");
    this.initIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("()V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.initNameAndTypeIdx = this.asm.cpi();
    this.asm.emitConstantPoolMethodref(this.nullPointerClass, this.initNameAndTypeIdx);
    this.nullPointerCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolMethodref(this.illegalArgumentClass, this.initNameAndTypeIdx);
    this.illegalArgumentCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(Ljava/lang/String;)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.initStringNameAndTypeIdx = this.asm.cpi();
    this.asm.emitConstantPoolMethodref(this.illegalArgumentClass, this.initStringNameAndTypeIdx);
    this.illegalArgumentStringCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(Ljava/lang/Throwable;)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.asm.emitConstantPoolMethodref(this.invocationTargetClass, this.asm.cpi());
    this.invocationTargetCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolMethodref(this.superClass, this.initNameAndTypeIdx);
    this.superCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/Object");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.objectClass = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("toString");
    this.asm.emitConstantPoolUTF8("()Ljava/lang/String;");
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    this.asm.emitConstantPoolMethodref(this.objectClass, this.asm.cpi());
    this.toStringIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("Code");
    this.codeIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("Exceptions");
    this.exceptionsIdx = this.asm.cpi();
  }

  protected void emitBoxingContantPoolEntries()
  {
    this.asm.emitConstantPoolUTF8("java/lang/Boolean");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.booleanIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(Z)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 2), this.asm.cpi());
    this.booleanCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("booleanValue");
    this.asm.emitConstantPoolUTF8("()Z");
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 6), this.asm.cpi());
    this.booleanUnboxIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/Byte");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.byteIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(B)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 2), this.asm.cpi());
    this.byteCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("byteValue");
    this.asm.emitConstantPoolUTF8("()B");
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 6), this.asm.cpi());
    this.byteUnboxIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/Character");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.characterIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(C)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 2), this.asm.cpi());
    this.characterCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("charValue");
    this.asm.emitConstantPoolUTF8("()C");
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 6), this.asm.cpi());
    this.characterUnboxIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/Double");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.doubleIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(D)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 2), this.asm.cpi());
    this.doubleCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("doubleValue");
    this.asm.emitConstantPoolUTF8("()D");
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 6), this.asm.cpi());
    this.doubleUnboxIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/Float");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.floatIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(F)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 2), this.asm.cpi());
    this.floatCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("floatValue");
    this.asm.emitConstantPoolUTF8("()F");
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 6), this.asm.cpi());
    this.floatUnboxIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/Integer");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.integerIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(I)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 2), this.asm.cpi());
    this.integerCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("intValue");
    this.asm.emitConstantPoolUTF8("()I");
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 6), this.asm.cpi());
    this.integerUnboxIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/Long");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.longIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(J)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 2), this.asm.cpi());
    this.longCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("longValue");
    this.asm.emitConstantPoolUTF8("()J");
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 6), this.asm.cpi());
    this.longUnboxIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("java/lang/Short");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.shortIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("(S)V");
    this.asm.emitConstantPoolNameAndType(this.initIdx, this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 2), this.asm.cpi());
    this.shortCtorIdx = this.asm.cpi();
    this.asm.emitConstantPoolUTF8("shortValue");
    this.asm.emitConstantPoolUTF8("()S");
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    this.asm.emitConstantPoolMethodref(sub(this.asm.cpi(), 6), this.asm.cpi());
    this.shortUnboxIdx = this.asm.cpi();
  }

  protected static short add(short paramShort1, short paramShort2)
  {
    return (short)(paramShort1 + paramShort2);
  }

  protected static short sub(short paramShort1, short paramShort2)
  {
    return (short)(paramShort1 - paramShort2);
  }

  protected boolean isStatic()
  {
    return Modifier.isStatic(this.modifiers);
  }

  protected static String getClassName(Class paramClass, boolean paramBoolean)
  {
    if (paramClass.isPrimitive())
    {
      if (paramClass == Boolean.TYPE)
        return "Z";
      if (paramClass == Byte.TYPE)
        return "B";
      if (paramClass == Character.TYPE)
        return "C";
      if (paramClass == Double.TYPE)
        return "D";
      if (paramClass == Float.TYPE)
        return "F";
      if (paramClass == Integer.TYPE)
        return "I";
      if (paramClass == Long.TYPE)
        return "J";
      if (paramClass == Short.TYPE)
        return "S";
      if (paramClass == Void.TYPE)
        return "V";
      throw new InternalError("Should have found primitive type");
    }
    if (paramClass.isArray())
      return "[" + getClassName(paramClass.getComponentType(), true);
    if (paramBoolean)
      return internalize("L" + paramClass.getName() + ";");
    return internalize(paramClass.getName());
  }

  private static String internalize(String paramString)
  {
    return paramString.replace('.', '/');
  }

  protected void emitConstructor()
  {
    ClassFileAssembler localClassFileAssembler = new ClassFileAssembler();
    localClassFileAssembler.setMaxLocals(1);
    localClassFileAssembler.opc_aload_0();
    localClassFileAssembler.opc_invokespecial(this.superCtorIdx, 0, 0);
    localClassFileAssembler.opc_return();
    emitMethod(this.initIdx, localClassFileAssembler.getMaxLocals(), localClassFileAssembler, null, null);
  }

  protected void emitMethod(short paramShort, int paramInt, ClassFileAssembler paramClassFileAssembler1, ClassFileAssembler paramClassFileAssembler2, short[] paramArrayOfShort)
  {
    int i = paramClassFileAssembler1.getLength();
    int j = 0;
    if (paramClassFileAssembler2 != null)
    {
      j = paramClassFileAssembler2.getLength();
      if (j % 8 != 0)
        throw new IllegalArgumentException("Illegal exception table");
    }
    int k = 12 + i + j;
    j /= 8;
    this.asm.emitShort(1);
    this.asm.emitShort(paramShort);
    this.asm.emitShort(add(paramShort, 1));
    if (paramArrayOfShort == null)
      this.asm.emitShort(1);
    else
      this.asm.emitShort(2);
    this.asm.emitShort(this.codeIdx);
    this.asm.emitInt(k);
    this.asm.emitShort(paramClassFileAssembler1.getMaxStack());
    this.asm.emitShort((short)Math.max(paramInt, paramClassFileAssembler1.getMaxLocals()));
    this.asm.emitInt(i);
    this.asm.append(paramClassFileAssembler1);
    this.asm.emitShort((short)j);
    if (paramClassFileAssembler2 != null)
      this.asm.append(paramClassFileAssembler2);
    this.asm.emitShort(0);
    if (paramArrayOfShort != null)
    {
      this.asm.emitShort(this.exceptionsIdx);
      this.asm.emitInt(2 + 2 * paramArrayOfShort.length);
      this.asm.emitShort((short)paramArrayOfShort.length);
      for (int l = 0; l < paramArrayOfShort.length; ++l)
        this.asm.emitShort(paramArrayOfShort[l]);
    }
  }

  protected short indexForPrimitiveType(Class paramClass)
  {
    if (paramClass == Boolean.TYPE)
      return this.booleanIdx;
    if (paramClass == Byte.TYPE)
      return this.byteIdx;
    if (paramClass == Character.TYPE)
      return this.characterIdx;
    if (paramClass == Double.TYPE)
      return this.doubleIdx;
    if (paramClass == Float.TYPE)
      return this.floatIdx;
    if (paramClass == Integer.TYPE)
      return this.integerIdx;
    if (paramClass == Long.TYPE)
      return this.longIdx;
    if (paramClass == Short.TYPE)
      return this.shortIdx;
    throw new InternalError("Should have found primitive type");
  }

  protected short ctorIndexForPrimitiveType(Class paramClass)
  {
    if (paramClass == Boolean.TYPE)
      return this.booleanCtorIdx;
    if (paramClass == Byte.TYPE)
      return this.byteCtorIdx;
    if (paramClass == Character.TYPE)
      return this.characterCtorIdx;
    if (paramClass == Double.TYPE)
      return this.doubleCtorIdx;
    if (paramClass == Float.TYPE)
      return this.floatCtorIdx;
    if (paramClass == Integer.TYPE)
      return this.integerCtorIdx;
    if (paramClass == Long.TYPE)
      return this.longCtorIdx;
    if (paramClass == Short.TYPE)
      return this.shortCtorIdx;
    throw new InternalError("Should have found primitive type");
  }

  protected static boolean canWidenTo(Class paramClass1, Class paramClass2)
  {
    if (!(paramClass1.isPrimitive()))
      return false;
    if (paramClass1 == Boolean.TYPE)
    {
      if (paramClass2 != Boolean.TYPE)
        break label270;
      return true;
    }
    if (paramClass1 == Byte.TYPE)
    {
      if ((paramClass2 != Byte.TYPE) && (paramClass2 != Short.TYPE) && (paramClass2 != Integer.TYPE) && (paramClass2 != Long.TYPE) && (paramClass2 != Float.TYPE) && (paramClass2 != Double.TYPE))
        break label270;
      return true;
    }
    if (paramClass1 == Short.TYPE)
    {
      if ((paramClass2 != Short.TYPE) && (paramClass2 != Integer.TYPE) && (paramClass2 != Long.TYPE) && (paramClass2 != Float.TYPE) && (paramClass2 != Double.TYPE))
        break label270;
      return true;
    }
    if (paramClass1 == Character.TYPE)
    {
      if ((paramClass2 != Character.TYPE) && (paramClass2 != Integer.TYPE) && (paramClass2 != Long.TYPE) && (paramClass2 != Float.TYPE) && (paramClass2 != Double.TYPE))
        break label270;
      return true;
    }
    if (paramClass1 == Integer.TYPE)
    {
      if ((paramClass2 != Integer.TYPE) && (paramClass2 != Long.TYPE) && (paramClass2 != Float.TYPE) && (paramClass2 != Double.TYPE))
        break label270;
      return true;
    }
    if (paramClass1 == Long.TYPE)
    {
      if ((paramClass2 != Long.TYPE) && (paramClass2 != Float.TYPE) && (paramClass2 != Double.TYPE))
        break label270;
      return true;
    }
    if (paramClass1 == Float.TYPE)
    {
      if ((paramClass2 != Float.TYPE) && (paramClass2 != Double.TYPE))
        break label270;
      return true;
    }
    label270: return ((paramClass1 == Double.TYPE) && (paramClass2 == Double.TYPE));
  }

  protected static void emitWideningBytecodeForPrimitiveConversion(ClassFileAssembler paramClassFileAssembler, Class paramClass1, Class paramClass2)
  {
    if ((paramClass1 == Byte.TYPE) || (paramClass1 == Short.TYPE) || (paramClass1 == Character.TYPE) || (paramClass1 == Integer.TYPE))
      if (paramClass2 == Long.TYPE)
        paramClassFileAssembler.opc_i2l();
      else if (paramClass2 == Float.TYPE)
        paramClassFileAssembler.opc_i2f();
      else if (paramClass2 == Double.TYPE)
        paramClassFileAssembler.opc_i2d();
    else if (paramClass1 == Long.TYPE)
      if (paramClass2 == Float.TYPE)
        paramClassFileAssembler.opc_l2f();
      else if (paramClass2 == Double.TYPE)
        paramClassFileAssembler.opc_l2d();
    else if ((paramClass1 == Float.TYPE) && (paramClass2 == Double.TYPE))
      paramClassFileAssembler.opc_f2d();
  }

  protected short unboxingMethodForPrimitiveType(Class paramClass)
  {
    if (paramClass == Boolean.TYPE)
      return this.booleanUnboxIdx;
    if (paramClass == Byte.TYPE)
      return this.byteUnboxIdx;
    if (paramClass == Character.TYPE)
      return this.characterUnboxIdx;
    if (paramClass == Short.TYPE)
      return this.shortUnboxIdx;
    if (paramClass == Integer.TYPE)
      return this.integerUnboxIdx;
    if (paramClass == Long.TYPE)
      return this.longUnboxIdx;
    if (paramClass == Float.TYPE)
      return this.floatUnboxIdx;
    if (paramClass == Double.TYPE)
      return this.doubleUnboxIdx;
    throw new InternalError("Illegal primitive type " + paramClass.getName());
  }

  protected static boolean isPrimitive(Class paramClass)
  {
    return ((paramClass.isPrimitive()) && (paramClass != Void.TYPE));
  }

  protected int typeSizeInStackSlots(Class paramClass)
  {
    if (paramClass == Void.TYPE)
      return 0;
    if ((paramClass == Long.TYPE) || (paramClass == Double.TYPE))
      return 2;
    return 1;
  }

  protected ClassFileAssembler illegalArgumentCodeBuffer()
  {
    if (this.illegalArgumentCodeBuffer == null)
    {
      this.illegalArgumentCodeBuffer = new ClassFileAssembler();
      this.illegalArgumentCodeBuffer.opc_new(this.illegalArgumentClass);
      this.illegalArgumentCodeBuffer.opc_dup();
      this.illegalArgumentCodeBuffer.opc_invokespecial(this.illegalArgumentCtorIdx, 0, 0);
      this.illegalArgumentCodeBuffer.opc_athrow();
    }
    return this.illegalArgumentCodeBuffer;
  }
}
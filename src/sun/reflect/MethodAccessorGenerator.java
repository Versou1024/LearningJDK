package sun.reflect;

import java.security.AccessController;
import java.security.PrivilegedAction;

class MethodAccessorGenerator extends AccessorGenerator
{
  private static final short NUM_BASE_CPOOL_ENTRIES = 12;
  private static final short NUM_METHODS = 2;
  private static final short NUM_SERIALIZATION_CPOOL_ENTRIES = 2;
  private static volatile int methodSymnum = 0;
  private static volatile int constructorSymnum = 0;
  private static volatile int serializationConstructorSymnum = 0;
  private Class declaringClass;
  private Class[] parameterTypes;
  private Class returnType;
  private boolean isConstructor;
  private boolean forSerialization;
  private short targetMethodRef;
  private short invokeIdx;
  private short invokeDescriptorIdx;
  private short nonPrimitiveParametersBaseIdx;

  public MethodAccessor generateMethod(Class paramClass1, String paramString, Class[] paramArrayOfClass1, Class paramClass2, Class[] paramArrayOfClass2, int paramInt)
  {
    return ((MethodAccessor)generate(paramClass1, paramString, paramArrayOfClass1, paramClass2, paramArrayOfClass2, paramInt, false, false, null));
  }

  public ConstructorAccessor generateConstructor(Class paramClass, Class[] paramArrayOfClass1, Class[] paramArrayOfClass2, int paramInt)
  {
    return ((ConstructorAccessor)generate(paramClass, "<init>", paramArrayOfClass1, Void.TYPE, paramArrayOfClass2, paramInt, true, false, null));
  }

  public SerializationConstructorAccessorImpl generateSerializationConstructor(Class paramClass1, Class[] paramArrayOfClass1, Class[] paramArrayOfClass2, int paramInt, Class paramClass2)
  {
    return ((SerializationConstructorAccessorImpl)generate(paramClass1, "<init>", paramArrayOfClass1, Void.TYPE, paramArrayOfClass2, paramInt, true, true, paramClass2));
  }

  private MagicAccessorImpl generate(Class paramClass1, String paramString, Class[] paramArrayOfClass1, Class paramClass2, Class[] paramArrayOfClass2, int paramInt, boolean paramBoolean1, boolean paramBoolean2, Class paramClass3)
  {
    ByteVector localByteVector = ByteVectorFactory.create();
    this.asm = new ClassFileAssembler(localByteVector);
    this.declaringClass = paramClass1;
    this.parameterTypes = paramArrayOfClass1;
    this.returnType = paramClass2;
    this.modifiers = paramInt;
    this.isConstructor = paramBoolean1;
    this.forSerialization = paramBoolean2;
    this.asm.emitMagicAndVersion();
    short s1 = 42;
    boolean bool = usesPrimitiveTypes();
    if (bool)
      s1 = (short)(s1 + 72);
    if (paramBoolean2)
      s1 = (short)(s1 + 2);
    s1 = (short)(s1 + (short)(2 * numNonPrimitiveParameterTypes()));
    this.asm.emitShort(add(s1, 1));
    String str = generateName(paramBoolean1, paramBoolean2);
    this.asm.emitConstantPoolUTF8(str);
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.thisClass = this.asm.cpi();
    if (paramBoolean1)
      if (paramBoolean2)
        this.asm.emitConstantPoolUTF8("sun/reflect/SerializationConstructorAccessorImpl");
      else
        this.asm.emitConstantPoolUTF8("sun/reflect/ConstructorAccessorImpl");
    else
      this.asm.emitConstantPoolUTF8("sun/reflect/MethodAccessorImpl");
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.superClass = this.asm.cpi();
    this.asm.emitConstantPoolUTF8(getClassName(paramClass1, false));
    this.asm.emitConstantPoolClass(this.asm.cpi());
    this.targetClass = this.asm.cpi();
    short s2 = 0;
    if (paramBoolean2)
    {
      this.asm.emitConstantPoolUTF8(getClassName(paramClass3, false));
      this.asm.emitConstantPoolClass(this.asm.cpi());
      s2 = this.asm.cpi();
    }
    this.asm.emitConstantPoolUTF8(paramString);
    this.asm.emitConstantPoolUTF8(buildInternalSignature());
    this.asm.emitConstantPoolNameAndType(sub(this.asm.cpi(), 1), this.asm.cpi());
    if (isInterface())
      this.asm.emitConstantPoolInterfaceMethodref(this.targetClass, this.asm.cpi());
    else if (paramBoolean2)
      this.asm.emitConstantPoolMethodref(s2, this.asm.cpi());
    else
      this.asm.emitConstantPoolMethodref(this.targetClass, this.asm.cpi());
    this.targetMethodRef = this.asm.cpi();
    if (paramBoolean1)
      this.asm.emitConstantPoolUTF8("newInstance");
    else
      this.asm.emitConstantPoolUTF8("invoke");
    this.invokeIdx = this.asm.cpi();
    if (paramBoolean1)
      this.asm.emitConstantPoolUTF8("([Ljava/lang/Object;)Ljava/lang/Object;");
    else
      this.asm.emitConstantPoolUTF8("(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
    this.invokeDescriptorIdx = this.asm.cpi();
    this.nonPrimitiveParametersBaseIdx = add(this.asm.cpi(), 2);
    for (int i = 0; i < paramArrayOfClass1.length; ++i)
    {
      Class localClass = paramArrayOfClass1[i];
      if (!(isPrimitive(localClass)))
      {
        this.asm.emitConstantPoolUTF8(getClassName(localClass, false));
        this.asm.emitConstantPoolClass(this.asm.cpi());
      }
    }
    emitCommonConstantPoolEntries();
    if (bool)
      emitBoxingContantPoolEntries();
    if (this.asm.cpi() != s1)
      throw new InternalError("Adjust this code (cpi = " + this.asm.cpi() + ", numCPEntries = " + s1 + ")");
    this.asm.emitShort(1);
    this.asm.emitShort(this.thisClass);
    this.asm.emitShort(this.superClass);
    this.asm.emitShort(0);
    this.asm.emitShort(0);
    this.asm.emitShort(2);
    emitConstructor();
    emitInvoke();
    this.asm.emitShort(0);
    localByteVector.trim();
    byte[] arrayOfByte = localByteVector.getData();
    return ((MagicAccessorImpl)AccessController.doPrivileged(new PrivilegedAction(this, str, arrayOfByte, paramClass1)
    {
      public Object run()
      {
        try
        {
          return ClassDefiner.defineClass(this.val$generatedName, this.val$bytes, 0, this.val$bytes.length, this.val$declaringClass.getClassLoader()).newInstance();
        }
        catch (InstantiationException localInstantiationException)
        {
          throw ((InternalError)new InternalError().initCause(localInstantiationException));
        }
        catch (IllegalAccessException localIllegalAccessException)
        {
          throw ((InternalError)new InternalError().initCause(localIllegalAccessException));
        }
      }
    }));
  }

  private void emitInvoke()
  {
    if (this.parameterTypes.length > 65535)
      throw new InternalError("Can't handle more than 65535 parameters");
    ClassFileAssembler localClassFileAssembler = new ClassFileAssembler();
    if (this.isConstructor)
      localClassFileAssembler.setMaxLocals(2);
    else
      localClassFileAssembler.setMaxLocals(3);
    short s1 = 0;
    if (this.isConstructor)
    {
      localClassFileAssembler.opc_new(this.targetClass);
      localClassFileAssembler.opc_dup();
    }
    else
    {
      if (isPrimitive(this.returnType))
      {
        localClassFileAssembler.opc_new(indexForPrimitiveType(this.returnType));
        localClassFileAssembler.opc_dup();
      }
      if (!(isStatic()))
      {
        localClassFileAssembler.opc_aload_1();
        localLabel1 = new Label();
        localClassFileAssembler.opc_ifnonnull(localLabel1);
        localClassFileAssembler.opc_new(this.nullPointerClass);
        localClassFileAssembler.opc_dup();
        localClassFileAssembler.opc_invokespecial(this.nullPointerCtorIdx, 0, 0);
        localClassFileAssembler.opc_athrow();
        localLabel1.bind();
        s1 = localClassFileAssembler.getLength();
        localClassFileAssembler.opc_aload_1();
        localClassFileAssembler.opc_checkcast(this.targetClass);
      }
    }
    Label localLabel1 = new Label();
    if (this.parameterTypes.length == 0)
    {
      if (this.isConstructor)
        localClassFileAssembler.opc_aload_1();
      else
        localClassFileAssembler.opc_aload_2();
      localClassFileAssembler.opc_ifnull(localLabel1);
    }
    if (this.isConstructor)
      localClassFileAssembler.opc_aload_1();
    else
      localClassFileAssembler.opc_aload_2();
    localClassFileAssembler.opc_arraylength();
    localClassFileAssembler.opc_sipush((short)this.parameterTypes.length);
    localClassFileAssembler.opc_if_icmpeq(localLabel1);
    localClassFileAssembler.opc_new(this.illegalArgumentClass);
    localClassFileAssembler.opc_dup();
    localClassFileAssembler.opc_invokespecial(this.illegalArgumentCtorIdx, 0, 0);
    localClassFileAssembler.opc_athrow();
    localLabel1.bind();
    short s2 = this.nonPrimitiveParametersBaseIdx;
    Label localLabel2 = null;
    byte b = 1;
    for (int i = 0; i < this.parameterTypes.length; ++i)
    {
      Class localClass = this.parameterTypes[i];
      b = (byte)(b + (byte)typeSizeInStackSlots(localClass));
      if (localLabel2 != null)
      {
        localLabel2.bind();
        localLabel2 = null;
      }
      if (this.isConstructor)
        localClassFileAssembler.opc_aload_1();
      else
        localClassFileAssembler.opc_aload_2();
      localClassFileAssembler.opc_sipush((short)i);
      localClassFileAssembler.opc_aaload();
      if (isPrimitive(localClass))
      {
        if (this.isConstructor)
          localClassFileAssembler.opc_astore_2();
        else
          localClassFileAssembler.opc_astore_3();
        Label localLabel3 = null;
        localLabel2 = new Label();
        for (j = 0; j < primitiveTypes.length; ++j)
        {
          localObject = primitiveTypes[j];
          if (canWidenTo((Class)localObject, localClass))
          {
            if (localLabel3 != null)
              localLabel3.bind();
            if (this.isConstructor)
              localClassFileAssembler.opc_aload_2();
            else
              localClassFileAssembler.opc_aload_3();
            localClassFileAssembler.opc_instanceof(indexForPrimitiveType((Class)localObject));
            localLabel3 = new Label();
            localClassFileAssembler.opc_ifeq(localLabel3);
            if (this.isConstructor)
              localClassFileAssembler.opc_aload_2();
            else
              localClassFileAssembler.opc_aload_3();
            localClassFileAssembler.opc_checkcast(indexForPrimitiveType((Class)localObject));
            localClassFileAssembler.opc_invokevirtual(unboxingMethodForPrimitiveType((Class)localObject), 0, typeSizeInStackSlots((Class)localObject));
            emitWideningBytecodeForPrimitiveConversion(localClassFileAssembler, (Class)localObject, localClass);
            localClassFileAssembler.opc_goto(localLabel2);
          }
        }
        if (localLabel3 == null)
          throw new InternalError("Must have found at least identity conversion");
        localLabel3.bind();
        localClassFileAssembler.opc_new(this.illegalArgumentClass);
        localClassFileAssembler.opc_dup();
        localClassFileAssembler.opc_invokespecial(this.illegalArgumentCtorIdx, 0, 0);
        localClassFileAssembler.opc_athrow();
      }
      else
      {
        localClassFileAssembler.opc_checkcast(s2);
        s2 = add(s2, 2);
      }
    }
    if (localLabel2 != null)
      localLabel2.bind();
    i = localClassFileAssembler.getLength();
    if (this.isConstructor)
      localClassFileAssembler.opc_invokespecial(this.targetMethodRef, b, 0);
    else if (isStatic())
      localClassFileAssembler.opc_invokestatic(this.targetMethodRef, b, typeSizeInStackSlots(this.returnType));
    else if (isInterface())
      localClassFileAssembler.opc_invokeinterface(this.targetMethodRef, b, b, typeSizeInStackSlots(this.returnType));
    else
      localClassFileAssembler.opc_invokevirtual(this.targetMethodRef, b, typeSizeInStackSlots(this.returnType));
    short s3 = localClassFileAssembler.getLength();
    if (!(this.isConstructor))
      if (isPrimitive(this.returnType))
        localClassFileAssembler.opc_invokespecial(ctorIndexForPrimitiveType(this.returnType), typeSizeInStackSlots(this.returnType), 0);
      else if (this.returnType == Void.TYPE)
        localClassFileAssembler.opc_aconst_null();
    localClassFileAssembler.opc_areturn();
    short s4 = localClassFileAssembler.getLength();
    localClassFileAssembler.setStack(1);
    localClassFileAssembler.opc_invokespecial(this.toStringIdx, 0, 1);
    localClassFileAssembler.opc_new(this.illegalArgumentClass);
    localClassFileAssembler.opc_dup_x1();
    localClassFileAssembler.opc_swap();
    localClassFileAssembler.opc_invokespecial(this.illegalArgumentStringCtorIdx, 1, 0);
    localClassFileAssembler.opc_athrow();
    int j = localClassFileAssembler.getLength();
    localClassFileAssembler.setStack(1);
    localClassFileAssembler.opc_new(this.invocationTargetClass);
    localClassFileAssembler.opc_dup_x1();
    localClassFileAssembler.opc_swap();
    localClassFileAssembler.opc_invokespecial(this.invocationTargetCtorIdx, 1, 0);
    localClassFileAssembler.opc_athrow();
    Object localObject = new ClassFileAssembler();
    ((ClassFileAssembler)localObject).emitShort(s1);
    ((ClassFileAssembler)localObject).emitShort(i);
    ((ClassFileAssembler)localObject).emitShort(s4);
    ((ClassFileAssembler)localObject).emitShort(this.classCastClass);
    ((ClassFileAssembler)localObject).emitShort(s1);
    ((ClassFileAssembler)localObject).emitShort(i);
    ((ClassFileAssembler)localObject).emitShort(s4);
    ((ClassFileAssembler)localObject).emitShort(this.nullPointerClass);
    ((ClassFileAssembler)localObject).emitShort(i);
    ((ClassFileAssembler)localObject).emitShort(s3);
    ((ClassFileAssembler)localObject).emitShort(j);
    ((ClassFileAssembler)localObject).emitShort(this.throwableClass);
    emitMethod(this.invokeIdx, localClassFileAssembler.getMaxLocals(), localClassFileAssembler, (ClassFileAssembler)localObject, new short[] { this.invocationTargetClass });
  }

  private boolean usesPrimitiveTypes()
  {
    if (this.returnType.isPrimitive())
      return true;
    for (int i = 0; i < this.parameterTypes.length; ++i)
      if (this.parameterTypes[i].isPrimitive())
        return true;
    return false;
  }

  private int numNonPrimitiveParameterTypes()
  {
    int i = 0;
    for (int j = 0; j < this.parameterTypes.length; ++j)
      if (!(this.parameterTypes[j].isPrimitive()))
        ++i;
    return i;
  }

  private boolean isInterface()
  {
    return this.declaringClass.isInterface();
  }

  private String buildInternalSignature()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("(");
    for (int i = 0; i < this.parameterTypes.length; ++i)
      localStringBuffer.append(getClassName(this.parameterTypes[i], true));
    localStringBuffer.append(")");
    localStringBuffer.append(getClassName(this.returnType, true));
    return localStringBuffer.toString();
  }

  private static synchronized String generateName(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramBoolean1)
    {
      if (paramBoolean2)
      {
        i = ++serializationConstructorSymnum;
        return "sun/reflect/GeneratedSerializationConstructorAccessor" + i;
      }
      i = ++constructorSymnum;
      return "sun/reflect/GeneratedConstructorAccessor" + i;
    }
    int i = ++methodSymnum;
    return "sun/reflect/GeneratedMethodAccessor" + i;
  }
}
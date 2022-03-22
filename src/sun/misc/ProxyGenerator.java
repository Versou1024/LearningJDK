package sun.misc;

import B;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import sun.security.action.GetBooleanAction;

public class ProxyGenerator
{
  private static final int CLASSFILE_MAJOR_VERSION = 49;
  private static final int CLASSFILE_MINOR_VERSION = 0;
  private static final int CONSTANT_UTF8 = 1;
  private static final int CONSTANT_UNICODE = 2;
  private static final int CONSTANT_INTEGER = 3;
  private static final int CONSTANT_FLOAT = 4;
  private static final int CONSTANT_LONG = 5;
  private static final int CONSTANT_DOUBLE = 6;
  private static final int CONSTANT_CLASS = 7;
  private static final int CONSTANT_STRING = 8;
  private static final int CONSTANT_FIELD = 9;
  private static final int CONSTANT_METHOD = 10;
  private static final int CONSTANT_INTERFACEMETHOD = 11;
  private static final int CONSTANT_NAMEANDTYPE = 12;
  private static final int ACC_PUBLIC = 1;
  private static final int ACC_PRIVATE = 2;
  private static final int ACC_STATIC = 8;
  private static final int ACC_FINAL = 16;
  private static final int ACC_SUPER = 32;
  private static final int opc_aconst_null = 1;
  private static final int opc_iconst_0 = 3;
  private static final int opc_bipush = 16;
  private static final int opc_sipush = 17;
  private static final int opc_ldc = 18;
  private static final int opc_ldc_w = 19;
  private static final int opc_iload = 21;
  private static final int opc_lload = 22;
  private static final int opc_fload = 23;
  private static final int opc_dload = 24;
  private static final int opc_aload = 25;
  private static final int opc_iload_0 = 26;
  private static final int opc_lload_0 = 30;
  private static final int opc_fload_0 = 34;
  private static final int opc_dload_0 = 38;
  private static final int opc_aload_0 = 42;
  private static final int opc_astore = 58;
  private static final int opc_astore_0 = 75;
  private static final int opc_aastore = 83;
  private static final int opc_pop = 87;
  private static final int opc_dup = 89;
  private static final int opc_ireturn = 172;
  private static final int opc_lreturn = 173;
  private static final int opc_freturn = 174;
  private static final int opc_dreturn = 175;
  private static final int opc_areturn = 176;
  private static final int opc_return = 177;
  private static final int opc_getstatic = 178;
  private static final int opc_putstatic = 179;
  private static final int opc_getfield = 180;
  private static final int opc_invokevirtual = 182;
  private static final int opc_invokespecial = 183;
  private static final int opc_invokestatic = 184;
  private static final int opc_invokeinterface = 185;
  private static final int opc_new = 187;
  private static final int opc_anewarray = 189;
  private static final int opc_athrow = 191;
  private static final int opc_checkcast = 192;
  private static final int opc_wide = 196;
  private static final String superclassName = "java/lang/reflect/Proxy";
  private static final String handlerFieldName = "h";
  private static final boolean saveGeneratedFiles;
  private static Method hashCodeMethod;
  private static Method equalsMethod;
  private static Method toStringMethod;
  private String className;
  private Class[] interfaces;
  private ConstantPool cp = new ConstantPool(null);
  private List<FieldInfo> fields = new ArrayList();
  private List<MethodInfo> methods = new ArrayList();
  private Map<String, List<ProxyMethod>> proxyMethods = new HashMap();
  private int proxyMethodCount = 0;

  public static byte[] generateProxyClass(String paramString, Class[] paramArrayOfClass)
  {
    ProxyGenerator localProxyGenerator = new ProxyGenerator(paramString, paramArrayOfClass);
    byte[] arrayOfByte = localProxyGenerator.generateClassFile();
    if (saveGeneratedFiles)
      AccessController.doPrivileged(new PrivilegedAction(paramString, arrayOfByte)
      {
        public Object run()
        {
          FileOutputStream localFileOutputStream;
          try
          {
            localFileOutputStream = new FileOutputStream(ProxyGenerator.access$000(this.val$name) + ".class");
            localFileOutputStream.write(this.val$classFile);
            localFileOutputStream.close();
            return null;
          }
          catch (IOException localIOException)
          {
            throw new InternalError("I/O exception saving generated file: " + localIOException);
          }
        }
      });
    return arrayOfByte;
  }

  private ProxyGenerator(String paramString, Class[] paramArrayOfClass)
  {
    this.className = paramString;
    this.interfaces = paramArrayOfClass;
  }

  private byte[] generateClassFile()
  {
    Object localObject2;
    addProxyMethod(hashCodeMethod, Object.class);
    addProxyMethod(equalsMethod, Object.class);
    addProxyMethod(toStringMethod, Object.class);
    for (int i = 0; i < this.interfaces.length; ++i)
    {
      localObject1 = this.interfaces[i].getMethods();
      for (int k = 0; k < localObject1.length; ++k)
        addProxyMethod(localObject1[k], this.interfaces[i]);
    }
    Iterator localIterator1 = this.proxyMethods.values().iterator();
    while (localIterator1.hasNext())
    {
      localObject1 = (List)localIterator1.next();
      checkReturnTypes((List)localObject1);
    }
    try
    {
      this.methods.add(generateConstructor());
      localIterator1 = this.proxyMethods.values().iterator();
      while (localIterator1.hasNext())
      {
        localObject1 = (List)localIterator1.next();
        Iterator localIterator2 = ((List)localObject1).iterator();
        while (localIterator2.hasNext())
        {
          localObject2 = (ProxyMethod)localIterator2.next();
          this.fields.add(new FieldInfo(this, ((ProxyMethod)localObject2).methodFieldName, "Ljava/lang/reflect/Method;", 10));
          this.methods.add(ProxyMethod.access$200((ProxyMethod)localObject2));
        }
      }
      this.methods.add(generateStaticInitializer());
    }
    catch (IOException localIOException1)
    {
      throw new InternalError("unexpected I/O Exception");
    }
    if (this.methods.size() > 65535)
      throw new IllegalArgumentException("method limit exceeded");
    if (this.fields.size() > 65535)
      throw new IllegalArgumentException("field limit exceeded");
    this.cp.getClass(dotToSlash(this.className));
    this.cp.getClass("java/lang/reflect/Proxy");
    for (int j = 0; j < this.interfaces.length; ++j)
      this.cp.getClass(dotToSlash(this.interfaces[j].getName()));
    this.cp.setReadOnly();
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    Object localObject1 = new DataOutputStream(localByteArrayOutputStream);
    try
    {
      ((DataOutputStream)localObject1).writeInt(-889275714);
      ((DataOutputStream)localObject1).writeShort(0);
      ((DataOutputStream)localObject1).writeShort(49);
      this.cp.write((OutputStream)localObject1);
      ((DataOutputStream)localObject1).writeShort(49);
      ((DataOutputStream)localObject1).writeShort(this.cp.getClass(dotToSlash(this.className)));
      ((DataOutputStream)localObject1).writeShort(this.cp.getClass("java/lang/reflect/Proxy"));
      ((DataOutputStream)localObject1).writeShort(this.interfaces.length);
      for (int l = 0; l < this.interfaces.length; ++l)
        ((DataOutputStream)localObject1).writeShort(this.cp.getClass(dotToSlash(this.interfaces[l].getName())));
      ((DataOutputStream)localObject1).writeShort(this.fields.size());
      Iterator localIterator3 = this.fields.iterator();
      while (localIterator3.hasNext())
      {
        localObject2 = (FieldInfo)localIterator3.next();
        ((FieldInfo)localObject2).write((DataOutputStream)localObject1);
      }
      ((DataOutputStream)localObject1).writeShort(this.methods.size());
      localIterator3 = this.methods.iterator();
      while (localIterator3.hasNext())
      {
        localObject2 = (MethodInfo)localIterator3.next();
        ((MethodInfo)localObject2).write((DataOutputStream)localObject1);
      }
      ((DataOutputStream)localObject1).writeShort(0);
    }
    catch (IOException localIOException2)
    {
      throw new InternalError("unexpected I/O Exception");
    }
    return ((B)(B)localByteArrayOutputStream.toByteArray());
  }

  private void addProxyMethod(Method paramMethod, Class paramClass)
  {
    String str1 = paramMethod.getName();
    Class[] arrayOfClass1 = paramMethod.getParameterTypes();
    Class localClass = paramMethod.getReturnType();
    Class[] arrayOfClass2 = paramMethod.getExceptionTypes();
    String str2 = str1 + getParameterDescriptors(arrayOfClass1);
    Object localObject = (List)this.proxyMethods.get(str2);
    if (localObject != null)
    {
      Iterator localIterator = ((List)localObject).iterator();
      while (localIterator.hasNext())
      {
        ProxyMethod localProxyMethod = (ProxyMethod)localIterator.next();
        if (localClass == localProxyMethod.returnType)
        {
          ArrayList localArrayList = new ArrayList();
          collectCompatibleTypes(arrayOfClass2, localProxyMethod.exceptionTypes, localArrayList);
          collectCompatibleTypes(localProxyMethod.exceptionTypes, arrayOfClass2, localArrayList);
          localProxyMethod.exceptionTypes = new Class[localArrayList.size()];
          localProxyMethod.exceptionTypes = ((Class[])localArrayList.toArray(localProxyMethod.exceptionTypes));
          return;
        }
      }
    }
    else
    {
      localObject = new ArrayList(3);
      this.proxyMethods.put(str2, localObject);
    }
    ((List)localObject).add(new ProxyMethod(this, str1, arrayOfClass1, localClass, arrayOfClass2, paramClass, null));
  }

  private static void checkReturnTypes(List<ProxyMethod> paramList)
  {
    if (paramList.size() < 2)
      return;
    LinkedList localLinkedList = new LinkedList();
    Object localObject = paramList.iterator();
    while (((Iterator)localObject).hasNext())
    {
      ProxyMethod localProxyMethod = (ProxyMethod)((Iterator)localObject).next();
      Class localClass1 = localProxyMethod.returnType;
      if (localClass1.isPrimitive())
        throw new IllegalArgumentException("methods with same signature " + getFriendlyMethodSignature(localProxyMethod.methodName, localProxyMethod.parameterTypes) + " but incompatible return types: " + localClass1.getName() + " and others");
      int i = 0;
      ListIterator localListIterator = localLinkedList.listIterator();
      while (true)
      {
        Class localClass2;
        do
        {
          if (!(localListIterator.hasNext()))
            break label214;
          localClass2 = (Class)localListIterator.next();
          if (!(localClass1.isAssignableFrom(localClass2)))
            break label174;
        }
        while (($assertionsDisabled) || (i == 0));
        throw new AssertionError();
        if (localClass2.isAssignableFrom(localClass1))
          if (i == 0)
          {
            label174: localListIterator.set(localClass1);
            i = 1;
          }
          else
          {
            localListIterator.remove();
          }
      }
      if (i == 0)
        label214: localLinkedList.add(localClass1);
    }
    if (localLinkedList.size() > 1)
    {
      localObject = (ProxyMethod)paramList.get(0);
      throw new IllegalArgumentException("methods with same signature " + getFriendlyMethodSignature(((ProxyMethod)localObject).methodName, ((ProxyMethod)localObject).parameterTypes) + " but incompatible return types: " + localLinkedList);
    }
  }

  private MethodInfo generateConstructor()
    throws IOException
  {
    MethodInfo localMethodInfo = new MethodInfo(this, "<init>", "(Ljava/lang/reflect/InvocationHandler;)V", 1);
    DataOutputStream localDataOutputStream = new DataOutputStream(localMethodInfo.code);
    code_aload(0, localDataOutputStream);
    code_aload(1, localDataOutputStream);
    localDataOutputStream.writeByte(183);
    localDataOutputStream.writeShort(this.cp.getMethodRef("java/lang/reflect/Proxy", "<init>", "(Ljava/lang/reflect/InvocationHandler;)V"));
    localDataOutputStream.writeByte(177);
    localMethodInfo.maxStack = 10;
    localMethodInfo.maxLocals = 2;
    localMethodInfo.declaredExceptions = new short[0];
    return localMethodInfo;
  }

  private MethodInfo generateStaticInitializer()
    throws IOException
  {
    MethodInfo localMethodInfo = new MethodInfo(this, "<clinit>", "()V", 8);
    int i = 1;
    short s2 = 0;
    DataOutputStream localDataOutputStream = new DataOutputStream(localMethodInfo.code);
    Iterator localIterator1 = this.proxyMethods.values().iterator();
    while (localIterator1.hasNext())
    {
      List localList = (List)localIterator1.next();
      Iterator localIterator2 = localList.iterator();
      while (localIterator2.hasNext())
      {
        ProxyMethod localProxyMethod = (ProxyMethod)localIterator2.next();
        ProxyMethod.access$1900(localProxyMethod, localDataOutputStream);
      }
    }
    localDataOutputStream.writeByte(177);
    short s3 = s1 = (short)localMethodInfo.code.size();
    localMethodInfo.exceptionTable.add(new ExceptionTableEntry(s2, s3, s1, this.cp.getClass("java/lang/NoSuchMethodException")));
    code_astore(i, localDataOutputStream);
    localDataOutputStream.writeByte(187);
    localDataOutputStream.writeShort(this.cp.getClass("java/lang/NoSuchMethodError"));
    localDataOutputStream.writeByte(89);
    code_aload(i, localDataOutputStream);
    localDataOutputStream.writeByte(182);
    localDataOutputStream.writeShort(this.cp.getMethodRef("java/lang/Throwable", "getMessage", "()Ljava/lang/String;"));
    localDataOutputStream.writeByte(183);
    localDataOutputStream.writeShort(this.cp.getMethodRef("java/lang/NoSuchMethodError", "<init>", "(Ljava/lang/String;)V"));
    localDataOutputStream.writeByte(191);
    short s1 = (short)localMethodInfo.code.size();
    localMethodInfo.exceptionTable.add(new ExceptionTableEntry(s2, s3, s1, this.cp.getClass("java/lang/ClassNotFoundException")));
    code_astore(i, localDataOutputStream);
    localDataOutputStream.writeByte(187);
    localDataOutputStream.writeShort(this.cp.getClass("java/lang/NoClassDefFoundError"));
    localDataOutputStream.writeByte(89);
    code_aload(i, localDataOutputStream);
    localDataOutputStream.writeByte(182);
    localDataOutputStream.writeShort(this.cp.getMethodRef("java/lang/Throwable", "getMessage", "()Ljava/lang/String;"));
    localDataOutputStream.writeByte(183);
    localDataOutputStream.writeShort(this.cp.getMethodRef("java/lang/NoClassDefFoundError", "<init>", "(Ljava/lang/String;)V"));
    localDataOutputStream.writeByte(191);
    if (localMethodInfo.code.size() > 65535)
      throw new IllegalArgumentException("code size limit exceeded");
    localMethodInfo.maxStack = 10;
    localMethodInfo.maxLocals = (short)(i + 1);
    localMethodInfo.declaredExceptions = new short[0];
    return localMethodInfo;
  }

  private void code_iload(int paramInt, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    codeLocalLoadStore(paramInt, 21, 26, paramDataOutputStream);
  }

  private void code_lload(int paramInt, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    codeLocalLoadStore(paramInt, 22, 30, paramDataOutputStream);
  }

  private void code_fload(int paramInt, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    codeLocalLoadStore(paramInt, 23, 34, paramDataOutputStream);
  }

  private void code_dload(int paramInt, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    codeLocalLoadStore(paramInt, 24, 38, paramDataOutputStream);
  }

  private void code_aload(int paramInt, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    codeLocalLoadStore(paramInt, 25, 42, paramDataOutputStream);
  }

  private void code_astore(int paramInt, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    codeLocalLoadStore(paramInt, 58, 75, paramDataOutputStream);
  }

  private void codeLocalLoadStore(int paramInt1, int paramInt2, int paramInt3, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    if ((!($assertionsDisabled)) && (((paramInt1 < 0) || (paramInt1 > 65535))))
      throw new AssertionError();
    if (paramInt1 <= 3)
    {
      paramDataOutputStream.writeByte(paramInt3 + paramInt1);
    }
    else if (paramInt1 <= 255)
    {
      paramDataOutputStream.writeByte(paramInt2);
      paramDataOutputStream.writeByte(paramInt1 & 0xFF);
    }
    else
    {
      paramDataOutputStream.writeByte(196);
      paramDataOutputStream.writeByte(paramInt2);
      paramDataOutputStream.writeShort(paramInt1 & 0xFFFF);
    }
  }

  private void code_ldc(int paramInt, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    if ((!($assertionsDisabled)) && (((paramInt < 0) || (paramInt > 65535))))
      throw new AssertionError();
    if (paramInt <= 255)
    {
      paramDataOutputStream.writeByte(18);
      paramDataOutputStream.writeByte(paramInt & 0xFF);
    }
    else
    {
      paramDataOutputStream.writeByte(19);
      paramDataOutputStream.writeShort(paramInt & 0xFFFF);
    }
  }

  private void code_ipush(int paramInt, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    if ((paramInt >= -1) && (paramInt <= 5))
    {
      paramDataOutputStream.writeByte(3 + paramInt);
    }
    else if ((paramInt >= -128) && (paramInt <= 127))
    {
      paramDataOutputStream.writeByte(16);
      paramDataOutputStream.writeByte(paramInt & 0xFF);
    }
    else if ((paramInt >= -32768) && (paramInt <= 32767))
    {
      paramDataOutputStream.writeByte(17);
      paramDataOutputStream.writeShort(paramInt & 0xFFFF);
    }
    else
    {
      throw new AssertionError();
    }
  }

  private void codeClassForName(Class paramClass, DataOutputStream paramDataOutputStream)
    throws IOException
  {
    code_ldc(this.cp.getString(paramClass.getName()), paramDataOutputStream);
    paramDataOutputStream.writeByte(184);
    paramDataOutputStream.writeShort(this.cp.getMethodRef("java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;"));
  }

  private static String dotToSlash(String paramString)
  {
    return paramString.replace('.', '/');
  }

  private static String getMethodDescriptor(Class[] paramArrayOfClass, Class paramClass)
  {
    return getParameterDescriptors(paramArrayOfClass) + ((paramClass == Void.TYPE) ? "V" : getFieldType(paramClass));
  }

  private static String getParameterDescriptors(Class[] paramArrayOfClass)
  {
    StringBuilder localStringBuilder = new StringBuilder("(");
    for (int i = 0; i < paramArrayOfClass.length; ++i)
      localStringBuilder.append(getFieldType(paramArrayOfClass[i]));
    localStringBuilder.append(')');
    return localStringBuilder.toString();
  }

  private static String getFieldType(Class paramClass)
  {
    if (paramClass.isPrimitive())
      return PrimitiveTypeInfo.get(paramClass).baseTypeString;
    if (paramClass.isArray())
      return paramClass.getName().replace('.', '/');
    return "L" + dotToSlash(paramClass.getName()) + ";";
  }

  private static String getFriendlyMethodSignature(String paramString, Class[] paramArrayOfClass)
  {
    StringBuilder localStringBuilder = new StringBuilder(paramString);
    localStringBuilder.append('(');
    for (int i = 0; i < paramArrayOfClass.length; ++i)
    {
      if (i > 0)
        localStringBuilder.append(',');
      Class localClass = paramArrayOfClass[i];
      for (int j = 0; localClass.isArray(); ++j)
        localClass = localClass.getComponentType();
      localStringBuilder.append(localClass.getName());
      while (j-- > 0)
        localStringBuilder.append("[]");
    }
    localStringBuilder.append(')');
    return localStringBuilder.toString();
  }

  private static int getWordsPerType(Class paramClass)
  {
    if ((paramClass == Long.TYPE) || (paramClass == Double.TYPE))
      return 2;
    return 1;
  }

  private static void collectCompatibleTypes(Class[] paramArrayOfClass1, Class[] paramArrayOfClass2, List<Class> paramList)
  {
    for (int i = 0; i < paramArrayOfClass1.length; ++i)
      if (!(paramList.contains(paramArrayOfClass1[i])))
        for (int j = 0; j < paramArrayOfClass2.length; ++j)
          if (paramArrayOfClass2[j].isAssignableFrom(paramArrayOfClass1[i]))
          {
            paramList.add(paramArrayOfClass1[i]);
            break;
          }
  }

  private static List<Class> computeUniqueCatchList(Class[] paramArrayOfClass)
  {
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(Error.class);
    localArrayList.add(RuntimeException.class);
    for (int i = 0; i < paramArrayOfClass.length; ++i)
    {
      Class localClass1 = paramArrayOfClass[i];
      if (localClass1.isAssignableFrom(Throwable.class))
      {
        localArrayList.clear();
        break;
      }
      if (!(Throwable.class.isAssignableFrom(localClass1)))
        break label146:
      int j = 0;
      while (j < localArrayList.size())
      {
        Class localClass2 = (Class)localArrayList.get(j);
        if (localClass2.isAssignableFrom(localClass1))
          break label146:
        if (localClass1.isAssignableFrom(localClass2))
          localArrayList.remove(j);
        else
          ++j;
      }
      label146: localArrayList.add(localClass1);
    }
    return localArrayList;
  }

  static
  {
    saveGeneratedFiles = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.misc.ProxyGenerator.saveGeneratedFiles"))).booleanValue();
    try
    {
      hashCodeMethod = Object.class.getMethod("hashCode", new Class[0]);
      equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
      toStringMethod = Object.class.getMethod("toString", new Class[0]);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new NoSuchMethodError(localNoSuchMethodException.getMessage());
    }
  }

  private static class ConstantPool
  {
    private List<Entry> pool = new ArrayList(32);
    private Map<Object, Short> map = new HashMap(16);
    private boolean readOnly = false;

    public short getUtf8(String paramString)
    {
      if (paramString == null)
        throw new NullPointerException();
      return getValue(paramString);
    }

    public short getInteger(int paramInt)
    {
      return getValue(new Integer(paramInt));
    }

    public short getFloat(float paramFloat)
    {
      return getValue(new Float(paramFloat));
    }

    public short getClass(String paramString)
    {
      short s = getUtf8(paramString);
      return getIndirect(new IndirectEntry(7, s));
    }

    public short getString(String paramString)
    {
      short s = getUtf8(paramString);
      return getIndirect(new IndirectEntry(8, s));
    }

    public short getFieldRef(String paramString1, String paramString2, String paramString3)
    {
      short s1 = getClass(paramString1);
      short s2 = getNameAndType(paramString2, paramString3);
      return getIndirect(new IndirectEntry(9, s1, s2));
    }

    public short getMethodRef(String paramString1, String paramString2, String paramString3)
    {
      short s1 = getClass(paramString1);
      short s2 = getNameAndType(paramString2, paramString3);
      return getIndirect(new IndirectEntry(10, s1, s2));
    }

    public short getInterfaceMethodRef(String paramString1, String paramString2, String paramString3)
    {
      short s1 = getClass(paramString1);
      short s2 = getNameAndType(paramString2, paramString3);
      return getIndirect(new IndirectEntry(11, s1, s2));
    }

    public short getNameAndType(String paramString1, String paramString2)
    {
      short s1 = getUtf8(paramString1);
      short s2 = getUtf8(paramString2);
      return getIndirect(new IndirectEntry(12, s1, s2));
    }

    public void setReadOnly()
    {
      this.readOnly = true;
    }

    public void write(OutputStream paramOutputStream)
      throws IOException
    {
      DataOutputStream localDataOutputStream = new DataOutputStream(paramOutputStream);
      localDataOutputStream.writeShort(this.pool.size() + 1);
      Iterator localIterator = this.pool.iterator();
      while (localIterator.hasNext())
      {
        Entry localEntry = (Entry)localIterator.next();
        localEntry.write(localDataOutputStream);
      }
    }

    private short addEntry(Entry paramEntry)
    {
      this.pool.add(paramEntry);
      if (this.pool.size() >= 65535)
        throw new IllegalArgumentException("constant pool size limit exceeded");
      return (short)this.pool.size();
    }

    private short getValue(Object paramObject)
    {
      Short localShort = (Short)this.map.get(paramObject);
      if (localShort != null)
        return localShort.shortValue();
      if (this.readOnly)
        throw new InternalError("late constant pool addition: " + paramObject);
      short s = addEntry(new ValueEntry(paramObject));
      this.map.put(paramObject, new Short(s));
      return s;
    }

    private short getIndirect(IndirectEntry paramIndirectEntry)
    {
      Short localShort = (Short)this.map.get(paramIndirectEntry);
      if (localShort != null)
        return localShort.shortValue();
      if (this.readOnly)
        throw new InternalError("late constant pool addition");
      short s = addEntry(paramIndirectEntry);
      this.map.put(paramIndirectEntry, new Short(s));
      return s;
    }

    private static abstract class Entry
    {
      public abstract void write(DataOutputStream paramDataOutputStream)
        throws IOException;
    }

    private static class IndirectEntry extends ProxyGenerator.ConstantPool.Entry
    {
      private int tag;
      private short index0;
      private short index1;

      public IndirectEntry(int paramInt, short paramShort)
      {
        super(null);
        this.tag = paramInt;
        this.index0 = paramShort;
        this.index1 = 0;
      }

      public IndirectEntry(int paramInt, short paramShort1, short paramShort2)
      {
        super(null);
        this.tag = paramInt;
        this.index0 = paramShort1;
        this.index1 = paramShort2;
      }

      public void write(DataOutputStream paramDataOutputStream)
        throws IOException
      {
        paramDataOutputStream.writeByte(this.tag);
        paramDataOutputStream.writeShort(this.index0);
        if ((this.tag == 9) || (this.tag == 10) || (this.tag == 11) || (this.tag == 12))
          paramDataOutputStream.writeShort(this.index1);
      }

      public int hashCode()
      {
        return (this.tag + this.index0 + this.index1);
      }

      public boolean equals(Object paramObject)
      {
        if (!(paramObject instanceof IndirectEntry))
          break label47;
        IndirectEntry localIndirectEntry = (IndirectEntry)paramObject;
        label47: return ((this.tag == localIndirectEntry.tag) && (this.index0 == localIndirectEntry.index0) && (this.index1 == localIndirectEntry.index1));
      }
    }

    private static class ValueEntry extends ProxyGenerator.ConstantPool.Entry
    {
      private Object value;

      public ValueEntry(Object paramObject)
      {
        super(null);
        this.value = paramObject;
      }

      public void write(DataOutputStream paramDataOutputStream)
        throws IOException
      {
        if (this.value instanceof String)
        {
          paramDataOutputStream.writeByte(1);
          paramDataOutputStream.writeUTF((String)this.value);
        }
        else if (this.value instanceof Integer)
        {
          paramDataOutputStream.writeByte(3);
          paramDataOutputStream.writeInt(((Integer)this.value).intValue());
        }
        else if (this.value instanceof Float)
        {
          paramDataOutputStream.writeByte(4);
          paramDataOutputStream.writeFloat(((Float)this.value).floatValue());
        }
        else if (this.value instanceof Long)
        {
          paramDataOutputStream.writeByte(5);
          paramDataOutputStream.writeLong(((Long)this.value).longValue());
        }
        else if (this.value instanceof Double)
        {
          paramDataOutputStream.writeDouble(6.0D);
          paramDataOutputStream.writeDouble(((Double)this.value).doubleValue());
        }
        else
        {
          throw new InternalError("bogus value entry: " + this.value);
        }
      }
    }
  }

  private static class ExceptionTableEntry
  {
    public short startPc;
    public short endPc;
    public short handlerPc;
    public short catchType;

    public ExceptionTableEntry(short paramShort1, short paramShort2, short paramShort3, short paramShort4)
    {
      this.startPc = paramShort1;
      this.endPc = paramShort2;
      this.handlerPc = paramShort3;
      this.catchType = paramShort4;
    }
  }

  private class FieldInfo
  {
    public int accessFlags;
    public String name;
    public String descriptor;

    public FieldInfo(, String paramString1, String paramString2, int paramInt)
    {
      this.name = paramString1;
      this.descriptor = paramString2;
      this.accessFlags = paramInt;
      ProxyGenerator.access$400(paramProxyGenerator).getUtf8(paramString1);
      ProxyGenerator.access$400(paramProxyGenerator).getUtf8(paramString2);
    }

    public void write()
      throws IOException
    {
      paramDataOutputStream.writeShort(this.accessFlags);
      paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getUtf8(this.name));
      paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getUtf8(this.descriptor));
      paramDataOutputStream.writeShort(0);
    }
  }

  private class MethodInfo
  {
    public int accessFlags;
    public String name;
    public String descriptor;
    public short maxStack;
    public short maxLocals;
    public ByteArrayOutputStream code = new ByteArrayOutputStream();
    public List<ProxyGenerator.ExceptionTableEntry> exceptionTable = new ArrayList();
    public short[] declaredExceptions;

    public MethodInfo(, String paramString1, String paramString2, int paramInt)
    {
      this.name = paramString1;
      this.descriptor = paramString2;
      this.accessFlags = paramInt;
      ProxyGenerator.access$400(paramProxyGenerator).getUtf8(paramString1);
      ProxyGenerator.access$400(paramProxyGenerator).getUtf8(paramString2);
      ProxyGenerator.access$400(paramProxyGenerator).getUtf8("Code");
      ProxyGenerator.access$400(paramProxyGenerator).getUtf8("Exceptions");
    }

    public void write()
      throws IOException
    {
      paramDataOutputStream.writeShort(this.accessFlags);
      paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getUtf8(this.name));
      paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getUtf8(this.descriptor));
      paramDataOutputStream.writeShort(2);
      paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getUtf8("Code"));
      paramDataOutputStream.writeInt(12 + this.code.size() + 8 * this.exceptionTable.size());
      paramDataOutputStream.writeShort(this.maxStack);
      paramDataOutputStream.writeShort(this.maxLocals);
      paramDataOutputStream.writeInt(this.code.size());
      this.code.writeTo(paramDataOutputStream);
      paramDataOutputStream.writeShort(this.exceptionTable.size());
      Iterator localIterator = this.exceptionTable.iterator();
      while (localIterator.hasNext())
      {
        ProxyGenerator.ExceptionTableEntry localExceptionTableEntry = (ProxyGenerator.ExceptionTableEntry)localIterator.next();
        paramDataOutputStream.writeShort(localExceptionTableEntry.startPc);
        paramDataOutputStream.writeShort(localExceptionTableEntry.endPc);
        paramDataOutputStream.writeShort(localExceptionTableEntry.handlerPc);
        paramDataOutputStream.writeShort(localExceptionTableEntry.catchType);
      }
      paramDataOutputStream.writeShort(0);
      paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getUtf8("Exceptions"));
      paramDataOutputStream.writeInt(2 + 2 * this.declaredExceptions.length);
      paramDataOutputStream.writeShort(this.declaredExceptions.length);
      for (int i = 0; i < this.declaredExceptions.length; ++i)
        paramDataOutputStream.writeShort(this.declaredExceptions[i]);
    }
  }

  private static class PrimitiveTypeInfo
  {
    public String baseTypeString;
    public String wrapperClassName;
    public String wrapperValueOfDesc;
    public String unwrapMethodName;
    public String unwrapMethodDesc;
    private static Map<Class, PrimitiveTypeInfo> table;

    private static void add(Class paramClass1, Class paramClass2)
    {
      table.put(paramClass1, new PrimitiveTypeInfo(paramClass1, paramClass2));
    }

    private PrimitiveTypeInfo(Class paramClass1, Class paramClass2)
    {
      if ((!($assertionsDisabled)) && (!(paramClass1.isPrimitive())))
        throw new AssertionError();
      this.baseTypeString = Array.newInstance(paramClass1, 0).getClass().getName().substring(1);
      this.wrapperClassName = ProxyGenerator.access$000(paramClass2.getName());
      this.wrapperValueOfDesc = "(" + this.baseTypeString + ")L" + this.wrapperClassName + ";";
      this.unwrapMethodName = paramClass1.getName() + "Value";
      this.unwrapMethodDesc = "()" + this.baseTypeString;
    }

    public static PrimitiveTypeInfo get(Class paramClass)
    {
      return ((PrimitiveTypeInfo)table.get(paramClass));
    }

    static
    {
      table = new HashMap();
      add(Byte.TYPE, Byte.class);
      add(Character.TYPE, Character.class);
      add(Double.TYPE, Double.class);
      add(Float.TYPE, Float.class);
      add(Integer.TYPE, Integer.class);
      add(Long.TYPE, Long.class);
      add(Short.TYPE, Short.class);
      add(Boolean.TYPE, Boolean.class);
    }
  }

  private class ProxyMethod
  {
    public String methodName;
    public Class[] parameterTypes;
    public Class returnType;
    public Class[] exceptionTypes;
    public Class fromClass;
    public String methodFieldName;

    private ProxyMethod(, String paramString, Class[] paramArrayOfClass1, Class paramClass1, Class[] paramArrayOfClass2, Class paramClass2)
    {
      this.methodName = paramString;
      this.parameterTypes = paramArrayOfClass1;
      this.returnType = paramClass1;
      this.exceptionTypes = paramArrayOfClass2;
      this.fromClass = paramClass2;
      this.methodFieldName = "m" + ProxyGenerator.access$508(paramProxyGenerator);
    }

    private ProxyGenerator.MethodInfo generateMethod()
      throws IOException
    {
      short s1;
      String str = ProxyGenerator.access$600(this.parameterTypes, this.returnType);
      ProxyGenerator.MethodInfo localMethodInfo = new ProxyGenerator.MethodInfo(this.this$0, this.methodName, str, 17);
      int[] arrayOfInt = new int[this.parameterTypes.length];
      int i = 1;
      for (int j = 0; j < arrayOfInt.length; ++j)
      {
        arrayOfInt[j] = i;
        i += ProxyGenerator.access$700(this.parameterTypes[j]);
      }
      j = i;
      short s2 = 0;
      DataOutputStream localDataOutputStream = new DataOutputStream(localMethodInfo.code);
      ProxyGenerator.access$800(this.this$0, 0, localDataOutputStream);
      localDataOutputStream.writeByte(180);
      localDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getFieldRef("java/lang/reflect/Proxy", "h", "Ljava/lang/reflect/InvocationHandler;"));
      ProxyGenerator.access$800(this.this$0, 0, localDataOutputStream);
      localDataOutputStream.writeByte(178);
      localDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getFieldRef(ProxyGenerator.access$000(ProxyGenerator.access$900(this.this$0)), this.methodFieldName, "Ljava/lang/reflect/Method;"));
      if (this.parameterTypes.length > 0)
      {
        ProxyGenerator.access$1000(this.this$0, this.parameterTypes.length, localDataOutputStream);
        localDataOutputStream.writeByte(189);
        localDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getClass("java/lang/Object"));
        for (int k = 0; k < this.parameterTypes.length; ++k)
        {
          localDataOutputStream.writeByte(89);
          ProxyGenerator.access$1000(this.this$0, k, localDataOutputStream);
          codeWrapArgument(this.parameterTypes[k], arrayOfInt[k], localDataOutputStream);
          localDataOutputStream.writeByte(83);
        }
      }
      else
      {
        localDataOutputStream.writeByte(1);
      }
      localDataOutputStream.writeByte(185);
      localDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getInterfaceMethodRef("java/lang/reflect/InvocationHandler", "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;"));
      localDataOutputStream.writeByte(4);
      localDataOutputStream.writeByte(0);
      if (this.returnType == Void.TYPE)
      {
        localDataOutputStream.writeByte(87);
        localDataOutputStream.writeByte(177);
      }
      else
      {
        codeUnwrapReturnValue(this.returnType, localDataOutputStream);
      }
      short s3 = s1 = (short)localMethodInfo.code.size();
      List localList = ProxyGenerator.access$1100(this.exceptionTypes);
      if (localList.size() > 0)
      {
        Iterator localIterator = localList.iterator();
        while (localIterator.hasNext())
        {
          Class localClass = (Class)localIterator.next();
          localMethodInfo.exceptionTable.add(new ProxyGenerator.ExceptionTableEntry(s2, s3, s1, ProxyGenerator.access$400(this.this$0).getClass(ProxyGenerator.access$000(localClass.getName()))));
        }
        localDataOutputStream.writeByte(191);
        s1 = (short)localMethodInfo.code.size();
        localMethodInfo.exceptionTable.add(new ProxyGenerator.ExceptionTableEntry(s2, s3, s1, ProxyGenerator.access$400(this.this$0).getClass("java/lang/Throwable")));
        ProxyGenerator.access$1200(this.this$0, j, localDataOutputStream);
        localDataOutputStream.writeByte(187);
        localDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getClass("java/lang/reflect/UndeclaredThrowableException"));
        localDataOutputStream.writeByte(89);
        ProxyGenerator.access$800(this.this$0, j, localDataOutputStream);
        localDataOutputStream.writeByte(183);
        localDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getMethodRef("java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V"));
        localDataOutputStream.writeByte(191);
      }
      if (localMethodInfo.code.size() > 65535)
        throw new IllegalArgumentException("code size limit exceeded");
      localMethodInfo.maxStack = 10;
      localMethodInfo.maxLocals = (short)(j + 1);
      localMethodInfo.declaredExceptions = new short[this.exceptionTypes.length];
      for (int l = 0; l < this.exceptionTypes.length; ++l)
        localMethodInfo.declaredExceptions[l] = ProxyGenerator.access$400(this.this$0).getClass(ProxyGenerator.access$000(this.exceptionTypes[l].getName()));
      return localMethodInfo;
    }

    private void codeWrapArgument(, int paramInt, DataOutputStream paramDataOutputStream)
      throws IOException
    {
      if (paramClass.isPrimitive())
      {
        ProxyGenerator.PrimitiveTypeInfo localPrimitiveTypeInfo = ProxyGenerator.PrimitiveTypeInfo.get(paramClass);
        if ((paramClass == Integer.TYPE) || (paramClass == Boolean.TYPE) || (paramClass == Byte.TYPE) || (paramClass == Character.TYPE) || (paramClass == Short.TYPE))
          ProxyGenerator.access$1300(this.this$0, paramInt, paramDataOutputStream);
        else if (paramClass == Long.TYPE)
          ProxyGenerator.access$1400(this.this$0, paramInt, paramDataOutputStream);
        else if (paramClass == Float.TYPE)
          ProxyGenerator.access$1500(this.this$0, paramInt, paramDataOutputStream);
        else if (paramClass == Double.TYPE)
          ProxyGenerator.access$1600(this.this$0, paramInt, paramDataOutputStream);
        else
          throw new AssertionError();
        paramDataOutputStream.writeByte(184);
        paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getMethodRef(localPrimitiveTypeInfo.wrapperClassName, "valueOf", localPrimitiveTypeInfo.wrapperValueOfDesc));
      }
      else
      {
        ProxyGenerator.access$800(this.this$0, paramInt, paramDataOutputStream);
      }
    }

    private void codeUnwrapReturnValue(, DataOutputStream paramDataOutputStream)
      throws IOException
    {
      if (paramClass.isPrimitive())
      {
        ProxyGenerator.PrimitiveTypeInfo localPrimitiveTypeInfo = ProxyGenerator.PrimitiveTypeInfo.get(paramClass);
        paramDataOutputStream.writeByte(192);
        paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getClass(localPrimitiveTypeInfo.wrapperClassName));
        paramDataOutputStream.writeByte(182);
        paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getMethodRef(localPrimitiveTypeInfo.wrapperClassName, localPrimitiveTypeInfo.unwrapMethodName, localPrimitiveTypeInfo.unwrapMethodDesc));
        if ((paramClass == Integer.TYPE) || (paramClass == Boolean.TYPE) || (paramClass == Byte.TYPE) || (paramClass == Character.TYPE) || (paramClass == Short.TYPE))
          paramDataOutputStream.writeByte(172);
        else if (paramClass == Long.TYPE)
          paramDataOutputStream.writeByte(173);
        else if (paramClass == Float.TYPE)
          paramDataOutputStream.writeByte(174);
        else if (paramClass == Double.TYPE)
          paramDataOutputStream.writeByte(175);
        else
          throw new AssertionError();
      }
      else
      {
        paramDataOutputStream.writeByte(192);
        paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getClass(ProxyGenerator.access$000(paramClass.getName())));
        paramDataOutputStream.writeByte(176);
      }
    }

    private void codeFieldInitialization()
      throws IOException
    {
      ProxyGenerator.access$1700(this.this$0, this.fromClass, paramDataOutputStream);
      ProxyGenerator.access$1800(this.this$0, ProxyGenerator.access$400(this.this$0).getString(this.methodName), paramDataOutputStream);
      ProxyGenerator.access$1000(this.this$0, this.parameterTypes.length, paramDataOutputStream);
      paramDataOutputStream.writeByte(189);
      paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getClass("java/lang/Class"));
      for (int i = 0; i < this.parameterTypes.length; ++i)
      {
        paramDataOutputStream.writeByte(89);
        ProxyGenerator.access$1000(this.this$0, i, paramDataOutputStream);
        if (this.parameterTypes[i].isPrimitive())
        {
          ProxyGenerator.PrimitiveTypeInfo localPrimitiveTypeInfo = ProxyGenerator.PrimitiveTypeInfo.get(this.parameterTypes[i]);
          paramDataOutputStream.writeByte(178);
          paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getFieldRef(localPrimitiveTypeInfo.wrapperClassName, "TYPE", "Ljava/lang/Class;"));
        }
        else
        {
          ProxyGenerator.access$1700(this.this$0, this.parameterTypes[i], paramDataOutputStream);
        }
        paramDataOutputStream.writeByte(83);
      }
      paramDataOutputStream.writeByte(182);
      paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getMethodRef("java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"));
      paramDataOutputStream.writeByte(179);
      paramDataOutputStream.writeShort(ProxyGenerator.access$400(this.this$0).getFieldRef(ProxyGenerator.access$000(ProxyGenerator.access$900(this.this$0)), this.methodFieldName, "Ljava/lang/reflect/Method;"));
    }
  }
}
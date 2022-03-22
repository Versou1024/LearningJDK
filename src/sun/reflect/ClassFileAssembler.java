package sun.reflect;

class ClassFileAssembler
  implements ClassFileConstants
{
  private ByteVector vec;
  private short cpIdx;
  private int stack;
  private int maxStack;
  private int maxLocals;

  public ClassFileAssembler()
  {
    this(ByteVectorFactory.create());
  }

  public ClassFileAssembler(ByteVector paramByteVector)
  {
    this.cpIdx = 0;
    this.stack = 0;
    this.maxStack = 0;
    this.maxLocals = 0;
    this.vec = paramByteVector;
  }

  public ByteVector getData()
  {
    return this.vec;
  }

  public short getLength()
  {
    return (short)this.vec.getLength();
  }

  public void emitMagicAndVersion()
  {
    emitInt(-889275714);
    emitShort(0);
    emitShort(49);
  }

  public void emitInt(int paramInt)
  {
    emitByte((byte)(paramInt >> 24));
    emitByte((byte)(paramInt >> 16 & 0xFF));
    emitByte((byte)(paramInt >> 8 & 0xFF));
    emitByte((byte)(paramInt & 0xFF));
  }

  public void emitShort(short paramShort)
  {
    emitByte((byte)(paramShort >> 8 & 0xFF));
    emitByte((byte)(paramShort & 0xFF));
  }

  void emitShort(short paramShort1, short paramShort2)
  {
    this.vec.put(paramShort1, (byte)(paramShort2 >> 8 & 0xFF));
    this.vec.put(paramShort1 + 1, (byte)(paramShort2 & 0xFF));
  }

  public void emitByte(byte paramByte)
  {
    this.vec.add(paramByte);
  }

  public void append(ClassFileAssembler paramClassFileAssembler)
  {
    append(paramClassFileAssembler.vec);
  }

  public void append(ByteVector paramByteVector)
  {
    for (int i = 0; i < paramByteVector.getLength(); ++i)
      emitByte(paramByteVector.get(i));
  }

  public short cpi()
  {
    if (this.cpIdx == 0)
      throw new RuntimeException("Illegal use of ClassFileAssembler");
    return this.cpIdx;
  }

  public void emitConstantPoolUTF8(String paramString)
  {
    byte[] arrayOfByte = UTF8.encode(paramString);
    emitByte(1);
    emitShort((short)arrayOfByte.length);
    for (int i = 0; i < arrayOfByte.length; ++i)
      emitByte(arrayOfByte[i]);
    ClassFileAssembler tmp39_38 = this;
    tmp39_38.cpIdx = (short)(tmp39_38.cpIdx + 1);
  }

  public void emitConstantPoolClass(short paramShort)
  {
    emitByte(7);
    emitShort(paramShort);
    ClassFileAssembler tmp12_11 = this;
    tmp12_11.cpIdx = (short)(tmp12_11.cpIdx + 1);
  }

  public void emitConstantPoolNameAndType(short paramShort1, short paramShort2)
  {
    emitByte(12);
    emitShort(paramShort1);
    emitShort(paramShort2);
    ClassFileAssembler tmp17_16 = this;
    tmp17_16.cpIdx = (short)(tmp17_16.cpIdx + 1);
  }

  public void emitConstantPoolFieldref(short paramShort1, short paramShort2)
  {
    emitByte(9);
    emitShort(paramShort1);
    emitShort(paramShort2);
    ClassFileAssembler tmp17_16 = this;
    tmp17_16.cpIdx = (short)(tmp17_16.cpIdx + 1);
  }

  public void emitConstantPoolMethodref(short paramShort1, short paramShort2)
  {
    emitByte(10);
    emitShort(paramShort1);
    emitShort(paramShort2);
    ClassFileAssembler tmp17_16 = this;
    tmp17_16.cpIdx = (short)(tmp17_16.cpIdx + 1);
  }

  public void emitConstantPoolInterfaceMethodref(short paramShort1, short paramShort2)
  {
    emitByte(11);
    emitShort(paramShort1);
    emitShort(paramShort2);
    ClassFileAssembler tmp17_16 = this;
    tmp17_16.cpIdx = (short)(tmp17_16.cpIdx + 1);
  }

  public void emitConstantPoolString(short paramShort)
  {
    emitByte(8);
    emitShort(paramShort);
    ClassFileAssembler tmp12_11 = this;
    tmp12_11.cpIdx = (short)(tmp12_11.cpIdx + 1);
  }

  private void incStack()
  {
    setStack(this.stack + 1);
  }

  private void decStack()
  {
    this.stack -= 1;
  }

  public short getMaxStack()
  {
    return (short)this.maxStack;
  }

  public short getMaxLocals()
  {
    return (short)this.maxLocals;
  }

  public void setMaxLocals(int paramInt)
  {
    this.maxLocals = paramInt;
  }

  public int getStack()
  {
    return this.stack;
  }

  public void setStack(int paramInt)
  {
    this.stack = paramInt;
    if (this.stack > this.maxStack)
      this.maxStack = this.stack;
  }

  public void opc_aconst_null()
  {
    emitByte(1);
    incStack();
  }

  public void opc_sipush(short paramShort)
  {
    emitByte(17);
    emitShort(paramShort);
    incStack();
  }

  public void opc_ldc(byte paramByte)
  {
    emitByte(18);
    emitByte(paramByte);
    incStack();
  }

  public void opc_iload_0()
  {
    emitByte(26);
    if (this.maxLocals < 1)
      this.maxLocals = 1;
    incStack();
  }

  public void opc_iload_1()
  {
    emitByte(27);
    if (this.maxLocals < 2)
      this.maxLocals = 2;
    incStack();
  }

  public void opc_iload_2()
  {
    emitByte(28);
    if (this.maxLocals < 3)
      this.maxLocals = 3;
    incStack();
  }

  public void opc_iload_3()
  {
    emitByte(29);
    if (this.maxLocals < 4)
      this.maxLocals = 4;
    incStack();
  }

  public void opc_lload_0()
  {
    emitByte(30);
    if (this.maxLocals < 2)
      this.maxLocals = 2;
    incStack();
    incStack();
  }

  public void opc_lload_1()
  {
    emitByte(31);
    if (this.maxLocals < 3)
      this.maxLocals = 3;
    incStack();
    incStack();
  }

  public void opc_lload_2()
  {
    emitByte(32);
    if (this.maxLocals < 4)
      this.maxLocals = 4;
    incStack();
    incStack();
  }

  public void opc_lload_3()
  {
    emitByte(33);
    if (this.maxLocals < 5)
      this.maxLocals = 5;
    incStack();
    incStack();
  }

  public void opc_fload_0()
  {
    emitByte(34);
    if (this.maxLocals < 1)
      this.maxLocals = 1;
    incStack();
  }

  public void opc_fload_1()
  {
    emitByte(35);
    if (this.maxLocals < 2)
      this.maxLocals = 2;
    incStack();
  }

  public void opc_fload_2()
  {
    emitByte(36);
    if (this.maxLocals < 3)
      this.maxLocals = 3;
    incStack();
  }

  public void opc_fload_3()
  {
    emitByte(37);
    if (this.maxLocals < 4)
      this.maxLocals = 4;
    incStack();
  }

  public void opc_dload_0()
  {
    emitByte(38);
    if (this.maxLocals < 2)
      this.maxLocals = 2;
    incStack();
    incStack();
  }

  public void opc_dload_1()
  {
    emitByte(39);
    if (this.maxLocals < 3)
      this.maxLocals = 3;
    incStack();
    incStack();
  }

  public void opc_dload_2()
  {
    emitByte(40);
    if (this.maxLocals < 4)
      this.maxLocals = 4;
    incStack();
    incStack();
  }

  public void opc_dload_3()
  {
    emitByte(41);
    if (this.maxLocals < 5)
      this.maxLocals = 5;
    incStack();
    incStack();
  }

  public void opc_aload_0()
  {
    emitByte(42);
    if (this.maxLocals < 1)
      this.maxLocals = 1;
    incStack();
  }

  public void opc_aload_1()
  {
    emitByte(43);
    if (this.maxLocals < 2)
      this.maxLocals = 2;
    incStack();
  }

  public void opc_aload_2()
  {
    emitByte(44);
    if (this.maxLocals < 3)
      this.maxLocals = 3;
    incStack();
  }

  public void opc_aload_3()
  {
    emitByte(45);
    if (this.maxLocals < 4)
      this.maxLocals = 4;
    incStack();
  }

  public void opc_aaload()
  {
    emitByte(50);
    decStack();
  }

  public void opc_astore_0()
  {
    emitByte(75);
    if (this.maxLocals < 1)
      this.maxLocals = 1;
    decStack();
  }

  public void opc_astore_1()
  {
    emitByte(76);
    if (this.maxLocals < 2)
      this.maxLocals = 2;
    decStack();
  }

  public void opc_astore_2()
  {
    emitByte(77);
    if (this.maxLocals < 3)
      this.maxLocals = 3;
    decStack();
  }

  public void opc_astore_3()
  {
    emitByte(78);
    if (this.maxLocals < 4)
      this.maxLocals = 4;
    decStack();
  }

  public void opc_pop()
  {
    emitByte(87);
    decStack();
  }

  public void opc_dup()
  {
    emitByte(89);
    incStack();
  }

  public void opc_dup_x1()
  {
    emitByte(90);
    incStack();
  }

  public void opc_swap()
  {
    emitByte(95);
  }

  public void opc_i2l()
  {
    emitByte(-123);
  }

  public void opc_i2f()
  {
    emitByte(-122);
  }

  public void opc_i2d()
  {
    emitByte(-121);
  }

  public void opc_l2f()
  {
    emitByte(-119);
  }

  public void opc_l2d()
  {
    emitByte(-118);
  }

  public void opc_f2d()
  {
    emitByte(-115);
  }

  public void opc_ifeq(short paramShort)
  {
    emitByte(-103);
    emitShort(paramShort);
    decStack();
  }

  public void opc_ifeq(Label paramLabel)
  {
    short s = getLength();
    emitByte(-103);
    paramLabel.add(this, s, getLength(), getStack() - 1);
    emitShort(-1);
  }

  public void opc_if_icmpeq(short paramShort)
  {
    emitByte(-97);
    emitShort(paramShort);
    setStack(getStack() - 2);
  }

  public void opc_if_icmpeq(Label paramLabel)
  {
    short s = getLength();
    emitByte(-97);
    paramLabel.add(this, s, getLength(), getStack() - 2);
    emitShort(-1);
  }

  public void opc_goto(short paramShort)
  {
    emitByte(-89);
    emitShort(paramShort);
  }

  public void opc_goto(Label paramLabel)
  {
    short s = getLength();
    emitByte(-89);
    paramLabel.add(this, s, getLength(), getStack());
    emitShort(-1);
  }

  public void opc_ifnull(short paramShort)
  {
    emitByte(-58);
    emitShort(paramShort);
    decStack();
  }

  public void opc_ifnull(Label paramLabel)
  {
    short s = getLength();
    emitByte(-58);
    paramLabel.add(this, s, getLength(), getStack() - 1);
    emitShort(-1);
    decStack();
  }

  public void opc_ifnonnull(short paramShort)
  {
    emitByte(-57);
    emitShort(paramShort);
    decStack();
  }

  public void opc_ifnonnull(Label paramLabel)
  {
    short s = getLength();
    emitByte(-57);
    paramLabel.add(this, s, getLength(), getStack() - 1);
    emitShort(-1);
    decStack();
  }

  public void opc_ireturn()
  {
    emitByte(-84);
    setStack(0);
  }

  public void opc_lreturn()
  {
    emitByte(-83);
    setStack(0);
  }

  public void opc_freturn()
  {
    emitByte(-82);
    setStack(0);
  }

  public void opc_dreturn()
  {
    emitByte(-81);
    setStack(0);
  }

  public void opc_areturn()
  {
    emitByte(-80);
    setStack(0);
  }

  public void opc_return()
  {
    emitByte(-79);
    setStack(0);
  }

  public void opc_getstatic(short paramShort, int paramInt)
  {
    emitByte(-78);
    emitShort(paramShort);
    setStack(getStack() + paramInt);
  }

  public void opc_putstatic(short paramShort, int paramInt)
  {
    emitByte(-77);
    emitShort(paramShort);
    setStack(getStack() - paramInt);
  }

  public void opc_getfield(short paramShort, int paramInt)
  {
    emitByte(-76);
    emitShort(paramShort);
    setStack(getStack() + paramInt - 1);
  }

  public void opc_putfield(short paramShort, int paramInt)
  {
    emitByte(-75);
    emitShort(paramShort);
    setStack(getStack() - paramInt - 1);
  }

  public void opc_invokevirtual(short paramShort, int paramInt1, int paramInt2)
  {
    emitByte(-74);
    emitShort(paramShort);
    setStack(getStack() - paramInt1 - 1 + paramInt2);
  }

  public void opc_invokespecial(short paramShort, int paramInt1, int paramInt2)
  {
    emitByte(-73);
    emitShort(paramShort);
    setStack(getStack() - paramInt1 - 1 + paramInt2);
  }

  public void opc_invokestatic(short paramShort, int paramInt1, int paramInt2)
  {
    emitByte(-72);
    emitShort(paramShort);
    setStack(getStack() - paramInt1 + paramInt2);
  }

  public void opc_invokeinterface(short paramShort, int paramInt1, byte paramByte, int paramInt2)
  {
    emitByte(-71);
    emitShort(paramShort);
    emitByte(paramByte);
    emitByte(0);
    setStack(getStack() - paramInt1 - 1 + paramInt2);
  }

  public void opc_arraylength()
  {
    emitByte(-66);
  }

  public void opc_new(short paramShort)
  {
    emitByte(-69);
    emitShort(paramShort);
    incStack();
  }

  public void opc_athrow()
  {
    emitByte(-65);
    setStack(1);
  }

  public void opc_checkcast(short paramShort)
  {
    emitByte(-64);
    emitShort(paramShort);
  }

  public void opc_instanceof(short paramShort)
  {
    emitByte(-63);
    emitShort(paramShort);
  }
}
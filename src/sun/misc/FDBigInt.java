package sun.misc;

class FDBigInt
{
  int nWords;
  int[] data;

  public FDBigInt(int paramInt)
  {
    this.nWords = 1;
    this.data = new int[1];
    this.data[0] = paramInt;
  }

  public FDBigInt(long paramLong)
  {
    this.data = new int[2];
    this.data[0] = (int)paramLong;
    this.data[1] = (int)(paramLong >>> 32);
    this.nWords = ((this.data[1] == 0) ? 1 : 2);
  }

  public FDBigInt(FDBigInt paramFDBigInt)
  {
    this.data = new int[this.nWords = paramFDBigInt.nWords];
    System.arraycopy(paramFDBigInt.data, 0, this.data, 0, this.nWords);
  }

  private FDBigInt(int[] paramArrayOfInt, int paramInt)
  {
    this.data = paramArrayOfInt;
    this.nWords = paramInt;
  }

  public FDBigInt(long paramLong, char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    int i = (paramInt2 + 8) / 9;
    if (i < 2)
      i = 2;
    this.data = new int[i];
    this.data[0] = (int)paramLong;
    this.data[1] = (int)(paramLong >>> 32);
    this.nWords = ((this.data[1] == 0) ? 1 : 2);
    int j = paramInt1;
    int k = paramInt2 - 5;
    while (j < k)
    {
      int i1 = j + 5;
      for (l = paramArrayOfChar[(j++)] - '0'; j < i1; l = 10 * l + paramArrayOfChar[(j++)] - 48);
      multaddMe(100000, l);
    }
    int i2 = 1;
    int l = 0;
    while (j < paramInt2)
    {
      l = 10 * l + paramArrayOfChar[(j++)] - 48;
      i2 *= 10;
    }
    if (i2 != 1)
      multaddMe(i2, l);
  }

  public void lshiftMe(int paramInt)
    throws IllegalArgumentException
  {
    if (paramInt <= 0)
    {
      if (paramInt == 0)
        return;
      throw new IllegalArgumentException("negative shift count");
    }
    int i = paramInt >> 5;
    int j = paramInt & 0x1F;
    int k = 32 - j;
    int[] arrayOfInt1 = this.data;
    int[] arrayOfInt2 = this.data;
    if (this.nWords + i + 1 > arrayOfInt1.length)
      arrayOfInt1 = new int[this.nWords + i + 1];
    int l = this.nWords + i;
    int i1 = this.nWords - 1;
    if (j == 0)
    {
      System.arraycopy(arrayOfInt2, 0, arrayOfInt1, i, this.nWords);
      l = i - 1;
    }
    else
    {
      arrayOfInt1[(l--)] = (arrayOfInt2[i1] >>> k);
      while (i1 >= 1)
        arrayOfInt1[(l--)] = (arrayOfInt2[i1] << j | arrayOfInt2[(--i1)] >>> k);
      arrayOfInt1[(l--)] = (arrayOfInt2[i1] << j);
    }
    while (l >= 0)
      arrayOfInt1[(l--)] = 0;
    this.data = arrayOfInt1;
    this.nWords += i + 1;
    while ((this.nWords > 1) && (this.data[(this.nWords - 1)] == 0))
      this.nWords -= 1;
  }

  public int normalizeMe()
    throws IllegalArgumentException
  {
    int j = 0;
    int k = 0;
    int l = 0;
    for (int i = this.nWords - 1; i >= 0; --i)
    {
      if ((l = this.data[i]) != 0)
        break;
      ++j;
    }
    if (i < 0)
      throw new IllegalArgumentException("zero value");
    this.nWords -= j;
    if ((l & 0xF0000000) != 0)
    {
      k = 32;
      while (true)
      {
        if ((l & 0xF0000000) == 0)
          break label133;
        l >>>= 1;
        --k;
      }
    }
    while (l <= 1048575)
    {
      l <<= 8;
      k += 8;
    }
    while (l <= 134217727)
    {
      l <<= 1;
      ++k;
    }
    if (k != 0)
      label133: lshiftMe(k);
    return k;
  }

  public FDBigInt mult(int paramInt)
  {
    long l1 = paramInt;
    int[] arrayOfInt = new int[(l1 * (this.data[(this.nWords - 1)] & 0xFFFFFFFF) > 268435455L) ? this.nWords + 1 : this.nWords];
    long l2 = 3412047291253522432L;
    for (int i = 0; i < this.nWords; ++i)
    {
      l2 += l1 * (this.data[i] & 0xFFFFFFFF);
      arrayOfInt[i] = (int)l2;
      l2 >>>= 32;
    }
    if (l2 == 3412046810217185280L)
      return new FDBigInt(arrayOfInt, this.nWords);
    arrayOfInt[this.nWords] = (int)l2;
    return new FDBigInt(arrayOfInt, this.nWords + 1);
  }

  public void multaddMe(int paramInt1, int paramInt2)
  {
    long l1 = paramInt1;
    long l2 = l1 * (this.data[0] & 0xFFFFFFFF) + (paramInt2 & 0xFFFFFFFF);
    this.data[0] = (int)l2;
    l2 >>>= 32;
    for (int i = 1; i < this.nWords; ++i)
    {
      l2 += l1 * (this.data[i] & 0xFFFFFFFF);
      this.data[i] = (int)l2;
      l2 >>>= 32;
    }
    if (l2 != 3412046810217185280L)
    {
      this.data[this.nWords] = (int)l2;
      this.nWords += 1;
    }
  }

  public FDBigInt mult(FDBigInt paramFDBigInt)
  {
    int[] arrayOfInt = new int[this.nWords + paramFDBigInt.nWords];
    for (int i = 0; i < this.nWords; ++i)
    {
      long l1 = this.data[i] & 0xFFFFFFFF;
      long l2 = 3412047652030775296L;
      for (int j = 0; j < paramFDBigInt.nWords; ++j)
      {
        l2 += (arrayOfInt[(i + j)] & 0xFFFFFFFF) + l1 * (paramFDBigInt.data[j] & 0xFFFFFFFF);
        arrayOfInt[(i + j)] = (int)l2;
        l2 >>>= 32;
      }
      arrayOfInt[(i + j)] = (int)l2;
    }
    for (i = arrayOfInt.length - 1; i > 0; --i)
      if (arrayOfInt[i] != 0)
        break;
    return new FDBigInt(arrayOfInt, i + 1);
  }

  public FDBigInt add(FDBigInt paramFDBigInt)
  {
    int[] arrayOfInt1;
    int[] arrayOfInt2;
    int j;
    int k;
    long l = 3412047291253522432L;
    if (this.nWords >= paramFDBigInt.nWords)
    {
      arrayOfInt1 = this.data;
      j = this.nWords;
      arrayOfInt2 = paramFDBigInt.data;
      k = paramFDBigInt.nWords;
    }
    else
    {
      arrayOfInt1 = paramFDBigInt.data;
      j = paramFDBigInt.nWords;
      arrayOfInt2 = this.data;
      k = this.nWords;
    }
    int[] arrayOfInt3 = new int[j];
    for (int i = 0; i < j; ++i)
    {
      l += (arrayOfInt1[i] & 0xFFFFFFFF);
      if (i < k)
        l += (arrayOfInt2[i] & 0xFFFFFFFF);
      arrayOfInt3[i] = (int)l;
      l >>= 32;
    }
    if (l != 3412046810217185280L)
    {
      int[] arrayOfInt4 = new int[arrayOfInt3.length + 1];
      System.arraycopy(arrayOfInt3, 0, arrayOfInt4, 0, arrayOfInt3.length);
      arrayOfInt4[(i++)] = (int)l;
      return new FDBigInt(arrayOfInt4, i);
    }
    return new FDBigInt(arrayOfInt3, i);
  }

  public FDBigInt sub(FDBigInt paramFDBigInt)
  {
    int[] arrayOfInt = new int[this.nWords];
    int j = this.nWords;
    int k = paramFDBigInt.nWords;
    int l = 0;
    long l1 = 3412047291253522432L;
    for (int i = 0; i < j; ++i)
    {
      l1 += (this.data[i] & 0xFFFFFFFF);
      if (i < k)
        l1 -= (paramFDBigInt.data[i] & 0xFFFFFFFF);
      if ((arrayOfInt[i] = (int)l1) == 0)
        ++l;
      else
        l = 0;
      l1 >>= 32;
    }
    if ((!($assertionsDisabled)) && (l1 != 3412046964836007936L))
      throw new AssertionError(l1);
    if ((!($assertionsDisabled)) && (!(dataInRangeIsZero(i, k, paramFDBigInt))))
      throw new AssertionError();
    return new FDBigInt(arrayOfInt, j - l);
  }

  private static boolean dataInRangeIsZero(int paramInt1, int paramInt2, FDBigInt paramFDBigInt)
  {
    do
      if (paramInt1 >= paramInt2)
        break label19;
    while (paramFDBigInt.data[(paramInt1++)] == 0);
    return false;
    label19: return true;
  }

  // ERROR //
  public int cmp(FDBigInt paramFDBigInt)
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 116	sun/misc/FDBigInt:nWords	I
    //   4: aload_1
    //   5: getfield 116	sun/misc/FDBigInt:nWords	I
    //   8: if_icmple +42 -> 50
    //   11: aload_1
    //   12: getfield 116	sun/misc/FDBigInt:nWords	I
    //   15: iconst_1
    //   16: isub
    //   17: istore_3
    //   18: aload_0
    //   19: getfield 116	sun/misc/FDBigInt:nWords	I
    //   22: iconst_1
    //   23: isub
    //   24: istore_2
    //   25: iload_2
    //   26: iload_3
    //   27: if_icmple +20 -> 47
    //   30: aload_0
    //   31: getfield 118	sun/misc/FDBigInt:data	[I
    //   34: iload_2
    //   35: iaload
    //   36: ifeq +5 -> 41
    //   39: iconst_1
    //   40: ireturn
    //   41: iinc 2 255
    //   44: goto -19 -> 25
    //   47: goto +60 -> 107
    //   50: aload_0
    //   51: getfield 116	sun/misc/FDBigInt:nWords	I
    //   54: aload_1
    //   55: getfield 116	sun/misc/FDBigInt:nWords	I
    //   58: if_icmpge +42 -> 100
    //   61: aload_0
    //   62: getfield 116	sun/misc/FDBigInt:nWords	I
    //   65: iconst_1
    //   66: isub
    //   67: istore_3
    //   68: aload_1
    //   69: getfield 116	sun/misc/FDBigInt:nWords	I
    //   72: iconst_1
    //   73: isub
    //   74: istore_2
    //   75: iload_2
    //   76: iload_3
    //   77: if_icmple +20 -> 97
    //   80: aload_1
    //   81: getfield 118	sun/misc/FDBigInt:data	[I
    //   84: iload_2
    //   85: iaload
    //   86: ifeq +5 -> 91
    //   89: iconst_m1
    //   90: ireturn
    //   91: iinc 2 255
    //   94: goto -19 -> 75
    //   97: goto +10 -> 107
    //   100: aload_0
    //   101: getfield 116	sun/misc/FDBigInt:nWords	I
    //   104: iconst_1
    //   105: isub
    //   106: istore_2
    //   107: iload_2
    //   108: ifle +27 -> 135
    //   111: aload_0
    //   112: getfield 118	sun/misc/FDBigInt:data	[I
    //   115: iload_2
    //   116: iaload
    //   117: aload_1
    //   118: getfield 118	sun/misc/FDBigInt:data	[I
    //   121: iload_2
    //   122: iaload
    //   123: if_icmpeq +6 -> 129
    //   126: goto +9 -> 135
    //   129: iinc 2 255
    //   132: goto -25 -> 107
    //   135: aload_0
    //   136: getfield 118	sun/misc/FDBigInt:data	[I
    //   139: iload_2
    //   140: iaload
    //   141: istore_3
    //   142: aload_1
    //   143: getfield 118	sun/misc/FDBigInt:data	[I
    //   146: iload_2
    //   147: iaload
    //   148: istore 4
    //   150: iload_3
    //   151: ifge +15 -> 166
    //   154: iload 4
    //   156: ifge +8 -> 164
    //   159: iload_3
    //   160: iload 4
    //   162: isub
    //   163: ireturn
    //   164: iconst_1
    //   165: ireturn
    //   166: iload 4
    //   168: ifge +5 -> 173
    //   171: iconst_m1
    //   172: ireturn
    //   173: iload_3
    //   174: iload 4
    //   176: isub
    //   177: ireturn
  }

  public int quoRemIteration(FDBigInt paramFDBigInt)
    throws IllegalArgumentException
  {
    if (this.nWords != paramFDBigInt.nWords)
      throw new IllegalArgumentException("disparate values");
    int i = this.nWords - 1;
    long l1 = (this.data[i] & 0xFFFFFFFF) / paramFDBigInt.data[i];
    long l2 = 3412047291253522432L;
    for (int j = 0; j <= i; ++j)
    {
      l2 += (this.data[j] & 0xFFFFFFFF) - l1 * (paramFDBigInt.data[j] & 0xFFFFFFFF);
      this.data[j] = (int)l2;
      l2 >>= 32;
    }
    if (l2 != 3412046810217185280L)
    {
      l3 = 3412047531771691008L;
      while (l3 == 3412047188174307328L)
      {
        l3 = 3412048047167766528L;
        for (k = 0; k <= i; ++k)
        {
          l3 += (this.data[k] & 0xFFFFFFFF) + (paramFDBigInt.data[k] & 0xFFFFFFFF);
          this.data[k] = (int)l3;
          l3 >>= 32;
        }
        if ((!($assertionsDisabled)) && (l3 != 3412047978448289792L) && (l3 != 3412047978448289793L))
          throw new AssertionError(l3);
        l1 -= 3412048064347635713L;
      }
    }
    long l3 = 3412047291253522432L;
    for (int k = 0; k <= i; ++k)
    {
      l3 += 10L * (this.data[k] & 0xFFFFFFFF);
      this.data[k] = (int)l3;
      l3 >>= 32;
    }
    if ((!($assertionsDisabled)) && (l3 != 3412046964836007936L))
      throw new AssertionError(l3);
    return (int)l1;
  }

  public long longValue()
  {
    if ((!($assertionsDisabled)) && (this.nWords <= 0))
      throw new AssertionError(this.nWords);
    if (this.nWords == 1)
      return (this.data[0] & 0xFFFFFFFF);
    if ((!($assertionsDisabled)) && (!(dataInRangeIsZero(2, this.nWords, this))))
      throw new AssertionError();
    if ((!($assertionsDisabled)) && (this.data[1] < 0))
      throw new AssertionError();
    return (this.data[1] << 32 | this.data[0] & 0xFFFFFFFF);
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer(30);
    localStringBuffer.append('[');
    int i = Math.min(this.nWords - 1, this.data.length - 1);
    if (this.nWords > this.data.length)
      localStringBuffer.append("(" + this.data.length + "<" + this.nWords + "!)");
    while (i > 0)
    {
      localStringBuffer.append(Integer.toHexString(this.data[i]));
      localStringBuffer.append(' ');
      --i;
    }
    localStringBuffer.append(Integer.toHexString(this.data[0]));
    localStringBuffer.append(']');
    return new String(localStringBuffer);
  }
}
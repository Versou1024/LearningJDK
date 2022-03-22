package sun.security.provider;

import java.nio.ByteOrder;
import java.security.AccessController;
import sun.misc.Unsafe;
import sun.security.action.GetPropertyAction;

final class ByteArrayAccess
{
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static final boolean littleEndianUnaligned;
  private static final boolean bigEndian;
  private static final int byteArrayOfs = unsafe.arrayBaseOffset([B.class);

  private static boolean unaligned()
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("os.arch", ""));
    return ((str.equals("i386")) || (str.equals("x86")) || (str.equals("amd64")));
  }

  static void b2iLittle(byte[] paramArrayOfByte, int paramInt1, int[] paramArrayOfInt, int paramInt2, int paramInt3)
  {
    if (littleEndianUnaligned)
    {
      paramInt1 += byteArrayOfs;
      paramInt3 += paramInt1;
      while (true)
      {
        if (paramInt1 >= paramInt3)
          return;
        paramArrayOfInt[(paramInt2++)] = unsafe.getInt(paramArrayOfByte, paramInt1);
        paramInt1 += 4;
      }
    }
    if ((bigEndian) && ((paramInt1 & 0x3) == 0))
    {
      paramInt1 += byteArrayOfs;
      paramInt3 += paramInt1;
      while (true)
      {
        if (paramInt1 >= paramInt3)
          return;
        paramArrayOfInt[(paramInt2++)] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt1));
        paramInt1 += 4;
      }
    }
    paramInt3 += paramInt1;
    while (paramInt1 < paramInt3)
    {
      paramArrayOfInt[(paramInt2++)] = (paramArrayOfByte[paramInt1] & 0xFF | (paramArrayOfByte[(paramInt1 + 1)] & 0xFF) << 8 | (paramArrayOfByte[(paramInt1 + 2)] & 0xFF) << 16 | paramArrayOfByte[(paramInt1 + 3)] << 24);
      paramInt1 += 4;
    }
  }

  static void b2iLittle64(byte[] paramArrayOfByte, int paramInt, int[] paramArrayOfInt)
  {
    if (littleEndianUnaligned)
    {
      paramInt += byteArrayOfs;
      paramArrayOfInt[0] = unsafe.getInt(paramArrayOfByte, paramInt);
      paramArrayOfInt[1] = unsafe.getInt(paramArrayOfByte, paramInt + 4);
      paramArrayOfInt[2] = unsafe.getInt(paramArrayOfByte, paramInt + 8);
      paramArrayOfInt[3] = unsafe.getInt(paramArrayOfByte, paramInt + 12);
      paramArrayOfInt[4] = unsafe.getInt(paramArrayOfByte, paramInt + 16);
      paramArrayOfInt[5] = unsafe.getInt(paramArrayOfByte, paramInt + 20);
      paramArrayOfInt[6] = unsafe.getInt(paramArrayOfByte, paramInt + 24);
      paramArrayOfInt[7] = unsafe.getInt(paramArrayOfByte, paramInt + 28);
      paramArrayOfInt[8] = unsafe.getInt(paramArrayOfByte, paramInt + 32);
      paramArrayOfInt[9] = unsafe.getInt(paramArrayOfByte, paramInt + 36);
      paramArrayOfInt[10] = unsafe.getInt(paramArrayOfByte, paramInt + 40);
      paramArrayOfInt[11] = unsafe.getInt(paramArrayOfByte, paramInt + 44);
      paramArrayOfInt[12] = unsafe.getInt(paramArrayOfByte, paramInt + 48);
      paramArrayOfInt[13] = unsafe.getInt(paramArrayOfByte, paramInt + 52);
      paramArrayOfInt[14] = unsafe.getInt(paramArrayOfByte, paramInt + 56);
      paramArrayOfInt[15] = unsafe.getInt(paramArrayOfByte, paramInt + 60);
    }
    else if ((bigEndian) && ((paramInt & 0x3) == 0))
    {
      paramInt += byteArrayOfs;
      paramArrayOfInt[0] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt));
      paramArrayOfInt[1] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 4));
      paramArrayOfInt[2] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 8));
      paramArrayOfInt[3] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 12));
      paramArrayOfInt[4] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 16));
      paramArrayOfInt[5] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 20));
      paramArrayOfInt[6] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 24));
      paramArrayOfInt[7] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 28));
      paramArrayOfInt[8] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 32));
      paramArrayOfInt[9] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 36));
      paramArrayOfInt[10] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 40));
      paramArrayOfInt[11] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 44));
      paramArrayOfInt[12] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 48));
      paramArrayOfInt[13] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 52));
      paramArrayOfInt[14] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 56));
      paramArrayOfInt[15] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 60));
    }
    else
    {
      b2iLittle(paramArrayOfByte, paramInt, paramArrayOfInt, 0, 64);
    }
  }

  static void i2bLittle(int[] paramArrayOfInt, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
  {
    if (littleEndianUnaligned)
    {
      paramInt2 += byteArrayOfs;
      paramInt3 += paramInt2;
      while (true)
      {
        if (paramInt2 >= paramInt3)
          return;
        unsafe.putInt(paramArrayOfByte, paramInt2, paramArrayOfInt[(paramInt1++)]);
        paramInt2 += 4;
      }
    }
    if ((bigEndian) && ((paramInt2 & 0x3) == 0))
    {
      paramInt2 += byteArrayOfs;
      paramInt3 += paramInt2;
      while (true)
      {
        if (paramInt2 >= paramInt3)
          return;
        unsafe.putInt(paramArrayOfByte, paramInt2, Integer.reverseBytes(paramArrayOfInt[(paramInt1++)]));
        paramInt2 += 4;
      }
    }
    paramInt3 += paramInt2;
    while (paramInt2 < paramInt3)
    {
      int i = paramArrayOfInt[(paramInt1++)];
      paramArrayOfByte[(paramInt2++)] = (byte)i;
      paramArrayOfByte[(paramInt2++)] = (byte)(i >> 8);
      paramArrayOfByte[(paramInt2++)] = (byte)(i >> 16);
      paramArrayOfByte[(paramInt2++)] = (byte)(i >> 24);
    }
  }

  static void i2bLittle4(int paramInt1, byte[] paramArrayOfByte, int paramInt2)
  {
    if (littleEndianUnaligned)
    {
      unsafe.putInt(paramArrayOfByte, byteArrayOfs + paramInt2, paramInt1);
    }
    else if ((bigEndian) && ((paramInt2 & 0x3) == 0))
    {
      unsafe.putInt(paramArrayOfByte, byteArrayOfs + paramInt2, Integer.reverseBytes(paramInt1));
    }
    else
    {
      paramArrayOfByte[paramInt2] = (byte)paramInt1;
      paramArrayOfByte[(paramInt2 + 1)] = (byte)(paramInt1 >> 8);
      paramArrayOfByte[(paramInt2 + 2)] = (byte)(paramInt1 >> 16);
      paramArrayOfByte[(paramInt2 + 3)] = (byte)(paramInt1 >> 24);
    }
  }

  static void b2iBig(byte[] paramArrayOfByte, int paramInt1, int[] paramArrayOfInt, int paramInt2, int paramInt3)
  {
    if (littleEndianUnaligned)
    {
      paramInt1 += byteArrayOfs;
      paramInt3 += paramInt1;
      while (true)
      {
        if (paramInt1 >= paramInt3)
          return;
        paramArrayOfInt[(paramInt2++)] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt1));
        paramInt1 += 4;
      }
    }
    if ((bigEndian) && ((paramInt1 & 0x3) == 0))
    {
      paramInt1 += byteArrayOfs;
      paramInt3 += paramInt1;
      while (true)
      {
        if (paramInt1 >= paramInt3)
          return;
        paramArrayOfInt[(paramInt2++)] = unsafe.getInt(paramArrayOfByte, paramInt1);
        paramInt1 += 4;
      }
    }
    paramInt3 += paramInt1;
    while (paramInt1 < paramInt3)
    {
      paramArrayOfInt[(paramInt2++)] = (paramArrayOfByte[(paramInt1 + 3)] & 0xFF | (paramArrayOfByte[(paramInt1 + 2)] & 0xFF) << 8 | (paramArrayOfByte[(paramInt1 + 1)] & 0xFF) << 16 | paramArrayOfByte[paramInt1] << 24);
      paramInt1 += 4;
    }
  }

  static void b2iBig64(byte[] paramArrayOfByte, int paramInt, int[] paramArrayOfInt)
  {
    if (littleEndianUnaligned)
    {
      paramInt += byteArrayOfs;
      paramArrayOfInt[0] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt));
      paramArrayOfInt[1] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 4));
      paramArrayOfInt[2] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 8));
      paramArrayOfInt[3] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 12));
      paramArrayOfInt[4] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 16));
      paramArrayOfInt[5] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 20));
      paramArrayOfInt[6] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 24));
      paramArrayOfInt[7] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 28));
      paramArrayOfInt[8] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 32));
      paramArrayOfInt[9] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 36));
      paramArrayOfInt[10] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 40));
      paramArrayOfInt[11] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 44));
      paramArrayOfInt[12] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 48));
      paramArrayOfInt[13] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 52));
      paramArrayOfInt[14] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 56));
      paramArrayOfInt[15] = Integer.reverseBytes(unsafe.getInt(paramArrayOfByte, paramInt + 60));
    }
    else if ((bigEndian) && ((paramInt & 0x3) == 0))
    {
      paramInt += byteArrayOfs;
      paramArrayOfInt[0] = unsafe.getInt(paramArrayOfByte, paramInt);
      paramArrayOfInt[1] = unsafe.getInt(paramArrayOfByte, paramInt + 4);
      paramArrayOfInt[2] = unsafe.getInt(paramArrayOfByte, paramInt + 8);
      paramArrayOfInt[3] = unsafe.getInt(paramArrayOfByte, paramInt + 12);
      paramArrayOfInt[4] = unsafe.getInt(paramArrayOfByte, paramInt + 16);
      paramArrayOfInt[5] = unsafe.getInt(paramArrayOfByte, paramInt + 20);
      paramArrayOfInt[6] = unsafe.getInt(paramArrayOfByte, paramInt + 24);
      paramArrayOfInt[7] = unsafe.getInt(paramArrayOfByte, paramInt + 28);
      paramArrayOfInt[8] = unsafe.getInt(paramArrayOfByte, paramInt + 32);
      paramArrayOfInt[9] = unsafe.getInt(paramArrayOfByte, paramInt + 36);
      paramArrayOfInt[10] = unsafe.getInt(paramArrayOfByte, paramInt + 40);
      paramArrayOfInt[11] = unsafe.getInt(paramArrayOfByte, paramInt + 44);
      paramArrayOfInt[12] = unsafe.getInt(paramArrayOfByte, paramInt + 48);
      paramArrayOfInt[13] = unsafe.getInt(paramArrayOfByte, paramInt + 52);
      paramArrayOfInt[14] = unsafe.getInt(paramArrayOfByte, paramInt + 56);
      paramArrayOfInt[15] = unsafe.getInt(paramArrayOfByte, paramInt + 60);
    }
    else
    {
      b2iBig(paramArrayOfByte, paramInt, paramArrayOfInt, 0, 64);
    }
  }

  static void i2bBig(int[] paramArrayOfInt, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
  {
    if (littleEndianUnaligned)
    {
      paramInt2 += byteArrayOfs;
      paramInt3 += paramInt2;
      while (true)
      {
        if (paramInt2 >= paramInt3)
          return;
        unsafe.putInt(paramArrayOfByte, paramInt2, Integer.reverseBytes(paramArrayOfInt[(paramInt1++)]));
        paramInt2 += 4;
      }
    }
    if ((bigEndian) && ((paramInt2 & 0x3) == 0))
    {
      paramInt2 += byteArrayOfs;
      paramInt3 += paramInt2;
      while (true)
      {
        if (paramInt2 >= paramInt3)
          return;
        unsafe.putInt(paramArrayOfByte, paramInt2, paramArrayOfInt[(paramInt1++)]);
        paramInt2 += 4;
      }
    }
    paramInt3 += paramInt2;
    while (paramInt2 < paramInt3)
    {
      int i = paramArrayOfInt[(paramInt1++)];
      paramArrayOfByte[(paramInt2++)] = (byte)(i >> 24);
      paramArrayOfByte[(paramInt2++)] = (byte)(i >> 16);
      paramArrayOfByte[(paramInt2++)] = (byte)(i >> 8);
      paramArrayOfByte[(paramInt2++)] = (byte)i;
    }
  }

  static void i2bBig4(int paramInt1, byte[] paramArrayOfByte, int paramInt2)
  {
    if (littleEndianUnaligned)
    {
      unsafe.putInt(paramArrayOfByte, byteArrayOfs + paramInt2, Integer.reverseBytes(paramInt1));
    }
    else if ((bigEndian) && ((paramInt2 & 0x3) == 0))
    {
      unsafe.putInt(paramArrayOfByte, byteArrayOfs + paramInt2, paramInt1);
    }
    else
    {
      paramArrayOfByte[paramInt2] = (byte)(paramInt1 >> 24);
      paramArrayOfByte[(paramInt2 + 1)] = (byte)(paramInt1 >> 16);
      paramArrayOfByte[(paramInt2 + 2)] = (byte)(paramInt1 >> 8);
      paramArrayOfByte[(paramInt2 + 3)] = (byte)paramInt1;
    }
  }

  static void b2lBig(byte[] paramArrayOfByte, int paramInt1, long[] paramArrayOfLong, int paramInt2, int paramInt3)
  {
    if (littleEndianUnaligned)
    {
      paramInt1 += byteArrayOfs;
      paramInt3 += paramInt1;
      while (true)
      {
        if (paramInt1 >= paramInt3)
          return;
        paramArrayOfLong[(paramInt2++)] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt1));
        paramInt1 += 8;
      }
    }
    if ((bigEndian) && ((paramInt1 & 0x3) == 0))
    {
      paramInt1 += byteArrayOfs;
      paramInt3 += paramInt1;
      while (true)
      {
        if (paramInt1 >= paramInt3)
          return;
        paramArrayOfLong[(paramInt2++)] = (unsafe.getInt(paramArrayOfByte, paramInt1) << 32 | unsafe.getInt(paramArrayOfByte, paramInt1 + 4) & 0xFFFFFFFF);
        paramInt1 += 8;
      }
    }
    paramInt3 += paramInt1;
    while (paramInt1 < paramInt3)
    {
      int i = paramArrayOfByte[(paramInt1 + 3)] & 0xFF | (paramArrayOfByte[(paramInt1 + 2)] & 0xFF) << 8 | (paramArrayOfByte[(paramInt1 + 1)] & 0xFF) << 16 | paramArrayOfByte[paramInt1] << 24;
      int j = paramArrayOfByte[((paramInt1 += 4) + 3)] & 0xFF | (paramArrayOfByte[(paramInt1 + 2)] & 0xFF) << 8 | (paramArrayOfByte[(paramInt1 + 1)] & 0xFF) << 16 | paramArrayOfByte[paramInt1] << 24;
      paramArrayOfLong[(paramInt2++)] = (i << 32 | j & 0xFFFFFFFF);
      paramInt1 += 4;
    }
  }

  static void b2lBig128(byte[] paramArrayOfByte, int paramInt, long[] paramArrayOfLong)
  {
    if (littleEndianUnaligned)
    {
      paramInt += byteArrayOfs;
      paramArrayOfLong[0] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt));
      paramArrayOfLong[1] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 8));
      paramArrayOfLong[2] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 16));
      paramArrayOfLong[3] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 24));
      paramArrayOfLong[4] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 32));
      paramArrayOfLong[5] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 40));
      paramArrayOfLong[6] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 48));
      paramArrayOfLong[7] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 56));
      paramArrayOfLong[8] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 64));
      paramArrayOfLong[9] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 72));
      paramArrayOfLong[10] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 80));
      paramArrayOfLong[11] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 88));
      paramArrayOfLong[12] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 96));
      paramArrayOfLong[13] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 104));
      paramArrayOfLong[14] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 112));
      paramArrayOfLong[15] = Long.reverseBytes(unsafe.getLong(paramArrayOfByte, paramInt + 120));
    }
    else
    {
      b2lBig(paramArrayOfByte, paramInt, paramArrayOfLong, 0, 128);
    }
  }

  static void l2bBig(long[] paramArrayOfLong, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
  {
    paramInt3 += paramInt2;
    while (paramInt2 < paramInt3)
    {
      long l = paramArrayOfLong[(paramInt1++)];
      paramArrayOfByte[(paramInt2++)] = (byte)(int)(l >> 56);
      paramArrayOfByte[(paramInt2++)] = (byte)(int)(l >> 48);
      paramArrayOfByte[(paramInt2++)] = (byte)(int)(l >> 40);
      paramArrayOfByte[(paramInt2++)] = (byte)(int)(l >> 32);
      paramArrayOfByte[(paramInt2++)] = (byte)(int)(l >> 24);
      paramArrayOfByte[(paramInt2++)] = (byte)(int)(l >> 16);
      paramArrayOfByte[(paramInt2++)] = (byte)(int)(l >> 8);
      paramArrayOfByte[(paramInt2++)] = (byte)(int)l;
    }
  }

  static
  {
    int i = ((unsafe.arrayIndexScale([B.class) == 1) && (unsafe.arrayIndexScale([I.class) == 4) && (unsafe.arrayIndexScale([J.class) == 8) && ((byteArrayOfs & 0x3) == 0)) ? 1 : 0;
    ByteOrder localByteOrder = ByteOrder.nativeOrder();
    littleEndianUnaligned = (i != 0) && (unaligned()) && (localByteOrder == ByteOrder.LITTLE_ENDIAN);
    bigEndian = (i != 0) && (localByteOrder == ByteOrder.BIG_ENDIAN);
  }
}
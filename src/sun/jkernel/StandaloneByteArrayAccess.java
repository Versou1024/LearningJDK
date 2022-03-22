package sun.jkernel;

final class StandaloneByteArrayAccess
{
  static void b2iLittle(byte[] paramArrayOfByte, int paramInt1, int[] paramArrayOfInt, int paramInt2, int paramInt3)
  {
    paramInt3 += paramInt1;
    while (paramInt1 < paramInt3)
    {
      paramArrayOfInt[(paramInt2++)] = (paramArrayOfByte[paramInt1] & 0xFF | (paramArrayOfByte[(paramInt1 + 1)] & 0xFF) << 8 | (paramArrayOfByte[(paramInt1 + 2)] & 0xFF) << 16 | paramArrayOfByte[(paramInt1 + 3)] << 24);
      paramInt1 += 4;
    }
  }

  static void i2bLittle(int[] paramArrayOfInt, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
  {
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

  static void b2iBig(byte[] paramArrayOfByte, int paramInt1, int[] paramArrayOfInt, int paramInt2, int paramInt3)
  {
    paramInt3 += paramInt1;
    while (paramInt1 < paramInt3)
    {
      paramArrayOfInt[(paramInt2++)] = (paramArrayOfByte[(paramInt1 + 3)] & 0xFF | (paramArrayOfByte[(paramInt1 + 2)] & 0xFF) << 8 | (paramArrayOfByte[(paramInt1 + 1)] & 0xFF) << 16 | paramArrayOfByte[paramInt1] << 24);
      paramInt1 += 4;
    }
  }

  static void i2bBig(int[] paramArrayOfInt, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
  {
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
    paramArrayOfByte[paramInt2] = (byte)(paramInt1 >> 24);
    paramArrayOfByte[(paramInt2 + 1)] = (byte)(paramInt1 >> 16);
    paramArrayOfByte[(paramInt2 + 2)] = (byte)(paramInt1 >> 8);
    paramArrayOfByte[(paramInt2 + 3)] = (byte)paramInt1;
  }

  static void b2lBig(byte[] paramArrayOfByte, int paramInt1, long[] paramArrayOfLong, int paramInt2, int paramInt3)
  {
    paramInt3 += paramInt1;
    while (paramInt1 < paramInt3)
    {
      int i = paramArrayOfByte[(paramInt1 + 3)] & 0xFF | (paramArrayOfByte[(paramInt1 + 2)] & 0xFF) << 8 | (paramArrayOfByte[(paramInt1 + 1)] & 0xFF) << 16 | paramArrayOfByte[paramInt1] << 24;
      int j = paramArrayOfByte[((paramInt1 += 4) + 3)] & 0xFF | (paramArrayOfByte[(paramInt1 + 2)] & 0xFF) << 8 | (paramArrayOfByte[(paramInt1 + 1)] & 0xFF) << 16 | paramArrayOfByte[paramInt1] << 24;
      paramArrayOfLong[(paramInt2++)] = (i << 32 | j & 0xFFFFFFFF);
      paramInt1 += 4;
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
}
package sun.misc;

public class FpUtils
{
  static double twoToTheDoubleScaleUp;
  static double twoToTheDoubleScaleDown;

  public static int getExponent(double paramDouble)
  {
    return (int)(((Double.doubleToRawLongBits(paramDouble) & 0x0) >> 52) - 1023L);
  }

  public static int getExponent(float paramFloat)
  {
    return (((Float.floatToRawIntBits(paramFloat) & 0x7F800000) >> 23) - 127);
  }

  static double powerOfTwoD(int paramInt)
  {
    if ((!($assertionsDisabled)) && (((paramInt < -1022) || (paramInt > 1023))))
      throw new AssertionError();
    return Double.longBitsToDouble(paramInt + 1023L << 52 & 0x0);
  }

  static float powerOfTwoF(int paramInt)
  {
    if ((!($assertionsDisabled)) && (((paramInt < -126) || (paramInt > 127))))
      throw new AssertionError();
    return Float.intBitsToFloat(paramInt + 127 << 23 & 0x7F800000);
  }

  public static double rawCopySign(double paramDouble1, double paramDouble2)
  {
    return Double.longBitsToDouble(Double.doubleToRawLongBits(paramDouble2) & 0x0 | Double.doubleToRawLongBits(paramDouble1) & 0xFFFFFFFF);
  }

  public static float rawCopySign(float paramFloat1, float paramFloat2)
  {
    return Float.intBitsToFloat(Float.floatToRawIntBits(paramFloat2) & 0x80000000 | Float.floatToRawIntBits(paramFloat1) & 0x7FFFFFFF);
  }

  public static boolean isFinite(double paramDouble)
  {
    return (Math.abs(paramDouble) <= 1.7976931348623157e+308D);
  }

  public static boolean isFinite(float paramFloat)
  {
    return (Math.abs(paramFloat) <= 3.4028235e+38F);
  }

  public static boolean isInfinite(double paramDouble)
  {
    return Double.isInfinite(paramDouble);
  }

  public static boolean isInfinite(float paramFloat)
  {
    return Float.isInfinite(paramFloat);
  }

  public static boolean isNaN(double paramDouble)
  {
    return Double.isNaN(paramDouble);
  }

  public static boolean isNaN(float paramFloat)
  {
    return Float.isNaN(paramFloat);
  }

  public static boolean isUnordered(double paramDouble1, double paramDouble2)
  {
    return ((isNaN(paramDouble1)) || (isNaN(paramDouble2)));
  }

  public static boolean isUnordered(float paramFloat1, float paramFloat2)
  {
    return ((isNaN(paramFloat1)) || (isNaN(paramFloat2)));
  }

  public static int ilogb(double paramDouble)
  {
    int i = getExponent(paramDouble);
    switch (i)
    {
    case 1024:
      if (isNaN(paramDouble))
        return 1073741824;
      return 268435456;
    case -1023:
      if (paramDouble == 0D)
        return -268435456;
      long l = Double.doubleToRawLongBits(paramDouble);
      l &= 4503599627370495L;
      if ((!($assertionsDisabled)) && (l == 3412047652030775296L))
        throw new AssertionError();
      while (l < 4503599627370496L)
      {
        l *= 2L;
        --i;
      }
      ++i;
      if ((!($assertionsDisabled)) && (((i < -1074) || (i >= -1022))))
        throw new AssertionError();
      return i;
    }
    if ((!($assertionsDisabled)) && (((i < -1022) || (i > 1023))))
      throw new AssertionError();
    return i;
  }

  public static int ilogb(float paramFloat)
  {
    int i = getExponent(paramFloat);
    switch (i)
    {
    case 128:
      if (isNaN(paramFloat))
        return 1073741824;
      return 268435456;
    case -127:
      if (paramFloat == 0F)
        return -268435456;
      int j = Float.floatToRawIntBits(paramFloat);
      j &= 8388607;
      if ((!($assertionsDisabled)) && (j == 0))
        throw new AssertionError();
      while (j < 8388608)
      {
        j *= 2;
        --i;
      }
      ++i;
      if ((!($assertionsDisabled)) && (((i < -149) || (i >= -126))))
        throw new AssertionError();
      return i;
    }
    if ((!($assertionsDisabled)) && (((i < -126) || (i > 127))))
      throw new AssertionError();
    return i;
  }

  public static double scalb(double paramDouble, int paramInt)
  {
    int i = 0;
    int j = 0;
    double d = (0.0D / 0.0D);
    if (paramInt < 0)
    {
      paramInt = Math.max(paramInt, -2099);
      j = -512;
      d = twoToTheDoubleScaleDown;
    }
    else
    {
      paramInt = Math.min(paramInt, 2099);
      j = 512;
      d = twoToTheDoubleScaleUp;
    }
    int k = paramInt >> 8 >>> 23;
    i = (paramInt + k & 0x1FF) - k;
    paramDouble *= powerOfTwoD(i);
    paramInt -= i;
    while (paramInt != 0)
    {
      paramDouble *= d;
      paramInt -= j;
    }
    return paramDouble;
  }

  public static float scalb(float paramFloat, int paramInt)
  {
    paramInt = Math.max(Math.min(paramInt, 278), -278);
    return (float)(paramFloat * powerOfTwoD(paramInt));
  }

  public static double nextAfter(double paramDouble1, double paramDouble2)
  {
    if ((isNaN(paramDouble1)) || (isNaN(paramDouble2)))
      return (paramDouble1 + paramDouble2);
    if (paramDouble1 == paramDouble2)
      return paramDouble2;
    long l = Double.doubleToRawLongBits(paramDouble1 + 0D);
    if (paramDouble2 > paramDouble1)
    {
      l += ((l >= 3412048459484626944L) ? 3412048287685935105L : -1L);
    }
    else
    {
      if ((!($assertionsDisabled)) && (paramDouble2 >= paramDouble1))
        throw new AssertionError();
      if (l > 3412047480232083456L)
        l -= 3412048356405411841L;
      else if (l < 3412047480232083456L)
        l += 3412048356405411841L;
      else
        l = -9223372036854775807L;
    }
    return Double.longBitsToDouble(l);
  }

  public static float nextAfter(float paramFloat, double paramDouble)
  {
    if ((isNaN(paramFloat)) || (isNaN(paramDouble)))
      return (paramFloat + (float)paramDouble);
    if (paramFloat == paramDouble)
      return (float)paramDouble;
    int i = Float.floatToRawIntBits(paramFloat + 0F);
    if (paramDouble > paramFloat)
    {
      i += ((i >= 0) ? 1 : -1);
    }
    else
    {
      if ((!($assertionsDisabled)) && (paramDouble >= paramFloat))
        throw new AssertionError();
      if (i > 0)
        --i;
      else if (i < 0)
        ++i;
      else
        i = -2147483647;
    }
    return Float.intBitsToFloat(i);
  }

  public static double nextUp(double paramDouble)
  {
    if ((isNaN(paramDouble)) || (paramDouble == (1.0D / 0.0D)))
      return paramDouble;
    paramDouble += 0D;
    return Double.longBitsToDouble(Double.doubleToRawLongBits(paramDouble) + ((paramDouble >= 0D) ? 3412040161607811073L : -1L));
  }

  public static float nextUp(float paramFloat)
  {
    if ((isNaN(paramFloat)) || (paramFloat == (1.0F / 1.0F)))
      return paramFloat;
    paramFloat += 0F;
    return Float.intBitsToFloat(Float.floatToRawIntBits(paramFloat) + ((paramFloat >= 0F) ? 1 : -1));
  }

  public static double nextDown(double paramDouble)
  {
    if ((isNaN(paramDouble)) || (paramDouble == (-1.0D / 0.0D)))
      return paramDouble;
    if (paramDouble == 0D)
      return -0.0D;
    return Double.longBitsToDouble(Double.doubleToRawLongBits(paramDouble) + ((paramDouble > 0D) ? -1L : 3412040161607811073L));
  }

  public static double nextDown(float paramFloat)
  {
    if ((isNaN(paramFloat)) || (paramFloat == (1.0F / -1.0F)))
      return paramFloat;
    if (paramFloat == 0F)
      return -0.0000000000000000000000000000000000000000000014013D;
    return Float.intBitsToFloat(Float.floatToRawIntBits(paramFloat) + ((paramFloat > 0F) ? -1 : 1));
  }

  public static double copySign(double paramDouble1, double paramDouble2)
  {
    return rawCopySign(paramDouble1, (isNaN(paramDouble2)) ? 1D : paramDouble2);
  }

  public static float copySign(float paramFloat1, float paramFloat2)
  {
    return rawCopySign(paramFloat1, (isNaN(paramFloat2)) ? 1F : paramFloat2);
  }

  public static double ulp(double paramDouble)
  {
    int i = getExponent(paramDouble);
    switch (i)
    {
    case 1024:
      return Math.abs(paramDouble);
    case -1023:
      return 4.9e-324D;
    }
    if ((!($assertionsDisabled)) && (((i > 1023) || (i < -1022))))
      throw new AssertionError();
    i -= 52;
    if (i >= -1022)
      return powerOfTwoD(i);
    return Double.longBitsToDouble(3412039714931212289L << i - -1074);
  }

  public static float ulp(float paramFloat)
  {
    int i = getExponent(paramFloat);
    switch (i)
    {
    case 128:
      return Math.abs(paramFloat);
    case -127:
      return 1.4e-45F;
    }
    if ((!($assertionsDisabled)) && (((i > 127) || (i < -126))))
      throw new AssertionError();
    i -= 23;
    if (i >= -126)
      return powerOfTwoF(i);
    return Float.intBitsToFloat(1 << i - -149);
  }

  public static double signum(double paramDouble)
  {
    return (((paramDouble == 0D) || (isNaN(paramDouble))) ? paramDouble : copySign(1D, paramDouble));
  }

  public static float signum(float paramFloat)
  {
    return (((paramFloat == 0F) || (isNaN(paramFloat))) ? paramFloat : copySign(1F, paramFloat));
  }

  static
  {
    twoToTheDoubleScaleUp = powerOfTwoD(512);
    twoToTheDoubleScaleDown = powerOfTwoD(-512);
  }
}
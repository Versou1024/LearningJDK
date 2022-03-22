package sun.java2d.loops;

import java.awt.geom.Path2D.Float;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ProcessPath
{
  public static final int PH_MODE_DRAW_CLIP = 0;
  public static final int PH_MODE_FILL_CLIP = 1;
  public static EndSubPathHandler noopEndSubPathHandler = new EndSubPathHandler()
  {
    public void processEndSubPath()
    {
    }
  };
  private static final float UPPER_BND = 85070586659632215000000000000000000000.0F;
  private static final float LOWER_BND = -85070586659632215000000000000000000000.0F;
  private static final int FWD_PREC = 7;
  private static final int MDP_PREC = 10;
  private static final int MDP_MULT = 1024;
  private static final int MDP_HALF_MULT = 512;
  private static final int UPPER_OUT_BND = 1048576;
  private static final int LOWER_OUT_BND = -1048576;
  private static final float CALC_UBND = 1048576.0F;
  private static final float CALC_LBND = -1048576.0F;
  public static final int EPSFX = 1;
  public static final float EPSF = 0.0009765625F;
  private static final int MDP_W_MASK = -1024;
  private static final int MDP_F_MASK = 1023;
  private static final int MAX_CUB_SIZE = 256;
  private static final int MAX_QUAD_SIZE = 1024;
  private static final int DF_CUB_STEPS = 3;
  private static final int DF_QUAD_STEPS = 2;
  private static final int DF_CUB_SHIFT = 6;
  private static final int DF_QUAD_SHIFT = 1;
  private static final int DF_CUB_COUNT = 8;
  private static final int DF_QUAD_COUNT = 4;
  private static final int DF_CUB_DEC_BND = 262144;
  private static final int DF_CUB_INC_BND = 32768;
  private static final int DF_QUAD_DEC_BND = 8192;
  private static final int DF_QUAD_INC_BND = 1024;
  private static final int CUB_A_SHIFT = 7;
  private static final int CUB_B_SHIFT = 11;
  private static final int CUB_C_SHIFT = 13;
  private static final int CUB_A_MDP_MULT = 128;
  private static final int CUB_B_MDP_MULT = 2048;
  private static final int CUB_C_MDP_MULT = 8192;
  private static final int QUAD_A_SHIFT = 7;
  private static final int QUAD_B_SHIFT = 9;
  private static final int QUAD_A_MDP_MULT = 128;
  private static final int QUAD_B_MDP_MULT = 512;
  private static final int CRES_MIN_CLIPPED = 0;
  private static final int CRES_MAX_CLIPPED = 1;
  private static final int CRES_NOT_CLIPPED = 3;
  private static final int CRES_INVISIBLE = 4;
  private static final int DF_MAX_POINT = 256;

  public static boolean fillPath(DrawHandler paramDrawHandler, Path2D.Float paramFloat, int paramInt1, int paramInt2)
  {
    FillProcessHandler localFillProcessHandler = new FillProcessHandler(paramDrawHandler);
    if (!(doProcessPath(localFillProcessHandler, paramFloat, paramInt1, paramInt2)))
      return false;
    FillPolygon(localFillProcessHandler, paramFloat.getWindingRule());
    return true;
  }

  public static boolean drawPath(DrawHandler paramDrawHandler, EndSubPathHandler paramEndSubPathHandler, Path2D.Float paramFloat, int paramInt1, int paramInt2)
  {
    return doProcessPath(new DrawProcessHandler(paramDrawHandler, paramEndSubPathHandler), paramFloat, paramInt1, paramInt2);
  }

  public static boolean drawPath(DrawHandler paramDrawHandler, Path2D.Float paramFloat, int paramInt1, int paramInt2)
  {
    return doProcessPath(new DrawProcessHandler(paramDrawHandler, noopEndSubPathHandler), paramFloat, paramInt1, paramInt2);
  }

  private static float CLIP(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, double paramDouble)
  {
    return (float)(paramFloat2 + (paramDouble - paramFloat1) * (paramFloat4 - paramFloat2) / (paramFloat3 - paramFloat1));
  }

  private static int CLIP(int paramInt1, int paramInt2, int paramInt3, int paramInt4, double paramDouble)
  {
    return (int)(paramInt2 + (paramDouble - paramInt1) * (paramInt4 - paramInt2) / (paramInt3 - paramInt1));
  }

  private static boolean IS_CLIPPED(int paramInt)
  {
    return ((paramInt == 0) || (paramInt == 1));
  }

  private static int TESTANDCLIP(float paramFloat1, float paramFloat2, float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = 3;
    if ((paramArrayOfFloat[paramInt1] < paramFloat1) || (paramArrayOfFloat[paramInt1] > paramFloat2))
    {
      double d;
      if (paramArrayOfFloat[paramInt1] < paramFloat1)
      {
        if (paramArrayOfFloat[paramInt3] < paramFloat1)
          return 4;
        i = 0;
        d = paramFloat1;
      }
      else
      {
        if (paramArrayOfFloat[paramInt3] > paramFloat2)
          return 4;
        i = 1;
        d = paramFloat2;
      }
      paramArrayOfFloat[paramInt2] = CLIP(paramArrayOfFloat[paramInt1], paramArrayOfFloat[paramInt2], paramArrayOfFloat[paramInt3], paramArrayOfFloat[paramInt4], d);
      paramArrayOfFloat[paramInt1] = (float)d;
    }
    return i;
  }

  private static int TESTANDCLIP(int paramInt1, int paramInt2, int[] paramArrayOfInt, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    int i = 3;
    if ((paramArrayOfInt[paramInt3] < paramInt1) || (paramArrayOfInt[paramInt3] > paramInt2))
    {
      double d;
      if (paramArrayOfInt[paramInt3] < paramInt1)
      {
        if (paramArrayOfInt[paramInt5] < paramInt1)
          return 4;
        i = 0;
        d = paramInt1;
      }
      else
      {
        if (paramArrayOfInt[paramInt5] > paramInt2)
          return 4;
        i = 1;
        d = paramInt2;
      }
      paramArrayOfInt[paramInt4] = CLIP(paramArrayOfInt[paramInt3], paramArrayOfInt[paramInt4], paramArrayOfInt[paramInt5], paramArrayOfInt[paramInt6], d);
      paramArrayOfInt[paramInt3] = (int)d;
    }
    return i;
  }

  private static int CLIPCLAMP(float paramFloat1, float paramFloat2, float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    paramArrayOfFloat[paramInt5] = paramArrayOfFloat[paramInt1];
    paramArrayOfFloat[paramInt6] = paramArrayOfFloat[paramInt2];
    int i = TESTANDCLIP(paramFloat1, paramFloat2, paramArrayOfFloat, paramInt1, paramInt2, paramInt3, paramInt4);
    if (i == 0)
    {
      paramArrayOfFloat[paramInt5] = paramArrayOfFloat[paramInt1];
    }
    else if (i == 1)
    {
      paramArrayOfFloat[paramInt5] = paramArrayOfFloat[paramInt1];
      i = 1;
    }
    else if (i == 4)
    {
      if (paramArrayOfFloat[paramInt1] > paramFloat2)
      {
        i = 4;
      }
      else
      {
        paramArrayOfFloat[paramInt1] = paramFloat1;
        paramArrayOfFloat[paramInt3] = paramFloat1;
        i = 3;
      }
    }
    return i;
  }

  private static int CLIPCLAMP(int paramInt1, int paramInt2, int[] paramArrayOfInt, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8)
  {
    paramArrayOfInt[paramInt7] = paramArrayOfInt[paramInt3];
    paramArrayOfInt[paramInt8] = paramArrayOfInt[paramInt4];
    int i = TESTANDCLIP(paramInt1, paramInt2, paramArrayOfInt, paramInt3, paramInt4, paramInt5, paramInt6);
    if (i == 0)
    {
      paramArrayOfInt[paramInt7] = paramArrayOfInt[paramInt3];
    }
    else if (i == 1)
    {
      paramArrayOfInt[paramInt7] = paramArrayOfInt[paramInt3];
      i = 1;
    }
    else if (i == 4)
    {
      if (paramArrayOfInt[paramInt3] > paramInt2)
      {
        i = 4;
      }
      else
      {
        paramArrayOfInt[paramInt3] = paramInt1;
        paramArrayOfInt[paramInt5] = paramInt1;
        i = 3;
      }
    }
    return i;
  }

  private static void DrawMonotonicQuad(ProcessHandler paramProcessHandler, float[] paramArrayOfFloat, boolean paramBoolean, int[] paramArrayOfInt)
  {
    int i = (int)(paramArrayOfFloat[0] * 1024.0F);
    int j = (int)(paramArrayOfFloat[1] * 1024.0F);
    int k = (int)(paramArrayOfFloat[4] * 1024.0F);
    int l = (int)(paramArrayOfFloat[5] * 1024.0F);
    int i1 = (i & 0x3FF) << 1;
    int i2 = (j & 0x3FF) << 1;
    int i3 = 4;
    int i4 = 1;
    int i5 = (int)((paramArrayOfFloat[0] - 2F * paramArrayOfFloat[2] + paramArrayOfFloat[4]) * 128.0F);
    int i6 = (int)((paramArrayOfFloat[1] - 2F * paramArrayOfFloat[3] + paramArrayOfFloat[5]) * 128.0F);
    int i7 = (int)((-2.0F * paramArrayOfFloat[0] + 2F * paramArrayOfFloat[2]) * 512.0F);
    int i8 = (int)((-2.0F * paramArrayOfFloat[1] + 2F * paramArrayOfFloat[3]) * 512.0F);
    int i9 = 2 * i5;
    int i10 = 2 * i6;
    int i11 = i5 + i7;
    int i12 = i6 + i8;
    int i15 = i;
    int i16 = j;
    int i17 = Math.max(Math.abs(i9), Math.abs(i10));
    int i18 = k - i;
    int i19 = l - j;
    int i20 = i & 0xFFFFFC00;
    int i21 = j & 0xFFFFFC00;
    while (i17 > 8192)
    {
      i11 = (i11 << 1) - i5;
      i12 = (i12 << 1) - i6;
      i3 <<= 1;
      i17 >>= 2;
      i1 <<= 2;
      i2 <<= 2;
      i4 += 2;
    }
    while (i3-- > 1)
    {
      i1 += i11;
      i2 += i12;
      i11 += i9;
      i12 += i10;
      int i13 = i15;
      int i14 = i16;
      i15 = i20 + (i1 >> i4);
      i16 = i21 + (i2 >> i4);
      if ((k - i15 ^ i18) < 0)
        i15 = k;
      if ((l - i16 ^ i19) < 0)
        i16 = l;
      paramProcessHandler.processFixedLine(i13, i14, i15, i16, paramArrayOfInt, paramBoolean, false);
    }
    paramProcessHandler.processFixedLine(i15, i16, k, l, paramArrayOfInt, paramBoolean, false);
  }

  private static void ProcessMonotonicQuad(ProcessHandler paramProcessHandler, float[] paramArrayOfFloat, int[] paramArrayOfInt)
  {
    float f3;
    float f4;
    float[] arrayOfFloat = new float[6];
    float f1 = f3 = paramArrayOfFloat[0];
    float f2 = f4 = paramArrayOfFloat[1];
    for (int i = 2; i < 6; i += 2)
    {
      f1 = (f1 > paramArrayOfFloat[i]) ? paramArrayOfFloat[i] : f1;
      f3 = (f3 < paramArrayOfFloat[i]) ? paramArrayOfFloat[i] : f3;
      f2 = (f2 > paramArrayOfFloat[(i + 1)]) ? paramArrayOfFloat[(i + 1)] : f2;
      f4 = (f4 < paramArrayOfFloat[(i + 1)]) ? paramArrayOfFloat[(i + 1)] : f4;
    }
    if (paramProcessHandler.clipMode == 0)
    {
      if ((paramProcessHandler.dhnd.xMaxf >= f1) && (paramProcessHandler.dhnd.xMinf <= f3) && (paramProcessHandler.dhnd.yMaxf >= f2) && (paramProcessHandler.dhnd.yMinf <= f4))
        break label260;
      return;
    }
    if ((paramProcessHandler.dhnd.yMaxf < f2) || (paramProcessHandler.dhnd.yMinf > f4) || (paramProcessHandler.dhnd.xMaxf < f1))
      return;
    if (paramProcessHandler.dhnd.xMinf > f3)
      paramArrayOfFloat[0] = (paramArrayOfFloat[2] = paramArrayOfFloat[4] = paramProcessHandler.dhnd.xMinf);
    if ((f3 - f1 > 1024.0F) || (f4 - f2 > 1024.0F))
    {
      label260: arrayOfFloat[4] = paramArrayOfFloat[4];
      arrayOfFloat[5] = paramArrayOfFloat[5];
      arrayOfFloat[2] = ((paramArrayOfFloat[2] + paramArrayOfFloat[4]) / 2F);
      arrayOfFloat[3] = ((paramArrayOfFloat[3] + paramArrayOfFloat[5]) / 2F);
      paramArrayOfFloat[2] = ((paramArrayOfFloat[0] + paramArrayOfFloat[2]) / 2F);
      paramArrayOfFloat[3] = ((paramArrayOfFloat[1] + paramArrayOfFloat[3]) / 2F);
      paramArrayOfFloat[4] = (arrayOfFloat[0] = (paramArrayOfFloat[2] + arrayOfFloat[2]) / 2F);
      paramArrayOfFloat[5] = (arrayOfFloat[1] = (paramArrayOfFloat[3] + arrayOfFloat[3]) / 2F);
      ProcessMonotonicQuad(paramProcessHandler, paramArrayOfFloat, paramArrayOfInt);
      ProcessMonotonicQuad(paramProcessHandler, arrayOfFloat, paramArrayOfInt);
    }
    else
    {
      DrawMonotonicQuad(paramProcessHandler, paramArrayOfFloat, ((paramProcessHandler.dhnd.xMinf >= f1) || (paramProcessHandler.dhnd.xMaxf <= f3) || (paramProcessHandler.dhnd.yMinf >= f2) || (paramProcessHandler.dhnd.yMaxf <= f4)) ? 1 : false, paramArrayOfInt);
    }
  }

  private static void ProcessQuad(ProcessHandler paramProcessHandler, float[] paramArrayOfFloat, int[] paramArrayOfInt)
  {
    double d1;
    double d2;
    double d3;
    double[] arrayOfDouble = new double[2];
    int i = 0;
    if ((((paramArrayOfFloat[0] > paramArrayOfFloat[2]) || (paramArrayOfFloat[2] > paramArrayOfFloat[4]))) && (((paramArrayOfFloat[0] < paramArrayOfFloat[2]) || (paramArrayOfFloat[2] < paramArrayOfFloat[4]))))
    {
      d2 = paramArrayOfFloat[0] - 2F * paramArrayOfFloat[2] + paramArrayOfFloat[4];
      if (d2 != 0D)
      {
        d3 = paramArrayOfFloat[0] - paramArrayOfFloat[2];
        d1 = d3 / d2;
        if ((d1 < 1D) && (d1 > 0D))
          arrayOfDouble[(i++)] = d1;
      }
    }
    if ((((paramArrayOfFloat[1] > paramArrayOfFloat[3]) || (paramArrayOfFloat[3] > paramArrayOfFloat[5]))) && (((paramArrayOfFloat[1] < paramArrayOfFloat[3]) || (paramArrayOfFloat[3] < paramArrayOfFloat[5]))))
    {
      d2 = paramArrayOfFloat[1] - 2F * paramArrayOfFloat[3] + paramArrayOfFloat[5];
      if (d2 != 0D)
      {
        d3 = paramArrayOfFloat[1] - paramArrayOfFloat[3];
        d1 = d3 / d2;
        if ((d1 < 1D) && (d1 > 0D))
          if (i > 0)
            if (arrayOfDouble[0] > d1)
            {
              arrayOfDouble[(i++)] = arrayOfDouble[0];
              arrayOfDouble[0] = d1;
            }
            else if (arrayOfDouble[0] < d1)
            {
              arrayOfDouble[(i++)] = d1;
            }
          else
            arrayOfDouble[(i++)] = d1;
      }
    }
    switch (i)
    {
    case 0:
      break;
    case 1:
      ProcessFirstMonotonicPartOfQuad(paramProcessHandler, paramArrayOfFloat, paramArrayOfInt, (float)arrayOfDouble[0]);
      break;
    case 2:
      ProcessFirstMonotonicPartOfQuad(paramProcessHandler, paramArrayOfFloat, paramArrayOfInt, (float)arrayOfDouble[0]);
      d1 = arrayOfDouble[1] - arrayOfDouble[0];
      if (d1 > 0D)
        ProcessFirstMonotonicPartOfQuad(paramProcessHandler, paramArrayOfFloat, paramArrayOfInt, (float)(d1 / (1D - arrayOfDouble[0])));
    }
    ProcessMonotonicQuad(paramProcessHandler, paramArrayOfFloat, paramArrayOfInt);
  }

  private static void ProcessFirstMonotonicPartOfQuad(ProcessHandler paramProcessHandler, float[] paramArrayOfFloat, int[] paramArrayOfInt, float paramFloat)
  {
    float[] arrayOfFloat = new float[6];
    arrayOfFloat[0] = paramArrayOfFloat[0];
    arrayOfFloat[1] = paramArrayOfFloat[1];
    arrayOfFloat[2] = (paramArrayOfFloat[0] + paramFloat * (paramArrayOfFloat[2] - paramArrayOfFloat[0]));
    arrayOfFloat[3] = (paramArrayOfFloat[1] + paramFloat * (paramArrayOfFloat[3] - paramArrayOfFloat[1]));
    paramArrayOfFloat[2] += paramFloat * (paramArrayOfFloat[4] - paramArrayOfFloat[2]);
    paramArrayOfFloat[3] += paramFloat * (paramArrayOfFloat[5] - paramArrayOfFloat[3]);
    paramArrayOfFloat[0] = (arrayOfFloat[4] = arrayOfFloat[2] + paramFloat * (paramArrayOfFloat[2] - arrayOfFloat[2]));
    paramArrayOfFloat[1] = (arrayOfFloat[5] = arrayOfFloat[3] + paramFloat * (paramArrayOfFloat[3] - arrayOfFloat[3]));
    ProcessMonotonicQuad(paramProcessHandler, arrayOfFloat, paramArrayOfInt);
  }

  private static void DrawMonotonicCubic(ProcessHandler paramProcessHandler, float[] paramArrayOfFloat, boolean paramBoolean, int[] paramArrayOfInt)
  {
    int i = (int)(paramArrayOfFloat[0] * 1024.0F);
    int j = (int)(paramArrayOfFloat[1] * 1024.0F);
    int k = (int)(paramArrayOfFloat[6] * 1024.0F);
    int l = (int)(paramArrayOfFloat[7] * 1024.0F);
    int i1 = (i & 0x3FF) << 6;
    int i2 = (j & 0x3FF) << 6;
    int i3 = 32768;
    int i4 = 262144;
    int i5 = 8;
    int i6 = 6;
    int i7 = (int)((-paramArrayOfFloat[0] + 3.0F * paramArrayOfFloat[2] - 3.0F * paramArrayOfFloat[4] + paramArrayOfFloat[6]) * 128.0F);
    int i8 = (int)((-paramArrayOfFloat[1] + 3.0F * paramArrayOfFloat[3] - 3.0F * paramArrayOfFloat[5] + paramArrayOfFloat[7]) * 128.0F);
    int i9 = (int)((3.0F * paramArrayOfFloat[0] - 6.0F * paramArrayOfFloat[2] + 3.0F * paramArrayOfFloat[4]) * 2048.0F);
    int i10 = (int)((3.0F * paramArrayOfFloat[1] - 6.0F * paramArrayOfFloat[3] + 3.0F * paramArrayOfFloat[5]) * 2048.0F);
    int i11 = (int)((-3.0F * paramArrayOfFloat[0] + 3.0F * paramArrayOfFloat[2]) * 8192.0F);
    int i12 = (int)((-3.0F * paramArrayOfFloat[1] + 3.0F * paramArrayOfFloat[3]) * 8192.0F);
    int i13 = 6 * i7;
    int i14 = 6 * i8;
    int i15 = i13 + i9;
    int i16 = i14 + i10;
    int i17 = i7 + (i9 >> 1) + i11;
    int i18 = i8 + (i10 >> 1) + i12;
    int i21 = i;
    int i22 = j;
    int i23 = i & 0xFFFFFC00;
    int i24 = j & 0xFFFFFC00;
    int i25 = k - i;
    int i26 = l - j;
    while (true)
    {
      while (true)
      {
        if (i5 <= 0)
          return;
        while ((Math.abs(i15) > i4) || (Math.abs(i16) > i4))
        {
          i15 = (i15 << 1) - i13;
          i16 = (i16 << 1) - i14;
          i17 = (i17 << 2) - (i15 >> 1);
          i18 = (i18 << 2) - (i16 >> 1);
          i5 <<= 1;
          i4 <<= 3;
          i3 <<= 3;
          i1 <<= 3;
          i2 <<= 3;
          i6 += 3;
        }
        while (((i5 & 0x1) == 0) && (i6 > 6) && (Math.abs(i17) <= i3) && (Math.abs(i18) <= i3))
        {
          i17 = (i17 >> 2) + (i15 >> 3);
          i18 = (i18 >> 2) + (i16 >> 3);
          i15 = i15 + i13 >> 1;
          i16 = i16 + i14 >> 1;
          i5 >>= 1;
          i4 >>= 3;
          i3 >>= 3;
          i1 >>= 3;
          i2 >>= 3;
          i6 -= 3;
        }
        if (--i5 <= 0)
          break;
        i1 += i17;
        i2 += i18;
        i17 += i15;
        i18 += i16;
        i15 += i13;
        i16 += i14;
        int i19 = i21;
        int i20 = i22;
        i21 = i23 + (i1 >> i6);
        i22 = i24 + (i2 >> i6);
        if ((k - i21 ^ i25) < 0)
          i21 = k;
        if ((l - i22 ^ i26) < 0)
          i22 = l;
        paramProcessHandler.processFixedLine(i19, i20, i21, i22, paramArrayOfInt, paramBoolean, false);
      }
      paramProcessHandler.processFixedLine(i21, i22, k, l, paramArrayOfInt, paramBoolean, false);
    }
  }

  private static void ProcessMonotonicCubic(ProcessHandler paramProcessHandler, float[] paramArrayOfFloat, int[] paramArrayOfInt)
  {
    float f4;
    float f6;
    float[] arrayOfFloat = new float[8];
    float f3 = f4 = paramArrayOfFloat[0];
    float f5 = f6 = paramArrayOfFloat[1];
    for (int i = 2; i < 8; i += 2)
    {
      f3 = (f3 > paramArrayOfFloat[i]) ? paramArrayOfFloat[i] : f3;
      f4 = (f4 < paramArrayOfFloat[i]) ? paramArrayOfFloat[i] : f4;
      f5 = (f5 > paramArrayOfFloat[(i + 1)]) ? paramArrayOfFloat[(i + 1)] : f5;
      f6 = (f6 < paramArrayOfFloat[(i + 1)]) ? paramArrayOfFloat[(i + 1)] : f6;
    }
    if (paramProcessHandler.clipMode == 0)
    {
      if ((paramProcessHandler.dhnd.xMaxf >= f3) && (paramProcessHandler.dhnd.xMinf <= f4) && (paramProcessHandler.dhnd.yMaxf >= f5) && (paramProcessHandler.dhnd.yMinf <= f6))
        break label265;
      return;
    }
    if ((paramProcessHandler.dhnd.yMaxf < f5) || (paramProcessHandler.dhnd.yMinf > f6) || (paramProcessHandler.dhnd.xMaxf < f3))
      return;
    if (paramProcessHandler.dhnd.xMinf > f4)
      paramArrayOfFloat[0] = (paramArrayOfFloat[2] = paramArrayOfFloat[4] = paramArrayOfFloat[6] = paramProcessHandler.dhnd.xMinf);
    if ((f4 - f3 > 256.0F) || (f6 - f5 > 256.0F))
    {
      label265: arrayOfFloat[6] = paramArrayOfFloat[6];
      arrayOfFloat[7] = paramArrayOfFloat[7];
      arrayOfFloat[4] = ((paramArrayOfFloat[4] + paramArrayOfFloat[6]) / 2F);
      arrayOfFloat[5] = ((paramArrayOfFloat[5] + paramArrayOfFloat[7]) / 2F);
      float f1 = (paramArrayOfFloat[2] + paramArrayOfFloat[4]) / 2F;
      float f2 = (paramArrayOfFloat[3] + paramArrayOfFloat[5]) / 2F;
      arrayOfFloat[2] = ((f1 + arrayOfFloat[4]) / 2F);
      arrayOfFloat[3] = ((f2 + arrayOfFloat[5]) / 2F);
      paramArrayOfFloat[2] = ((paramArrayOfFloat[0] + paramArrayOfFloat[2]) / 2F);
      paramArrayOfFloat[3] = ((paramArrayOfFloat[1] + paramArrayOfFloat[3]) / 2F);
      paramArrayOfFloat[4] = ((paramArrayOfFloat[2] + f1) / 2F);
      paramArrayOfFloat[5] = ((paramArrayOfFloat[3] + f2) / 2F);
      paramArrayOfFloat[6] = (arrayOfFloat[0] = (paramArrayOfFloat[4] + arrayOfFloat[2]) / 2F);
      paramArrayOfFloat[7] = (arrayOfFloat[1] = (paramArrayOfFloat[5] + arrayOfFloat[3]) / 2F);
      ProcessMonotonicCubic(paramProcessHandler, paramArrayOfFloat, paramArrayOfInt);
      ProcessMonotonicCubic(paramProcessHandler, arrayOfFloat, paramArrayOfInt);
    }
    else
    {
      DrawMonotonicCubic(paramProcessHandler, paramArrayOfFloat, ((paramProcessHandler.dhnd.xMinf > f3) || (paramProcessHandler.dhnd.xMaxf < f4) || (paramProcessHandler.dhnd.yMinf > f5) || (paramProcessHandler.dhnd.yMaxf < f6)) ? 1 : false, paramArrayOfInt);
    }
  }

  private static void ProcessCubic(ProcessHandler paramProcessHandler, float[] paramArrayOfFloat, int[] paramArrayOfInt)
  {
    int j;
    int k;
    double[] arrayOfDouble1 = new double[4];
    double[] arrayOfDouble2 = new double[3];
    double[] arrayOfDouble3 = new double[2];
    int i = 0;
    if ((((paramArrayOfFloat[0] > paramArrayOfFloat[2]) || (paramArrayOfFloat[2] > paramArrayOfFloat[4]) || (paramArrayOfFloat[4] > paramArrayOfFloat[6]))) && (((paramArrayOfFloat[0] < paramArrayOfFloat[2]) || (paramArrayOfFloat[2] < paramArrayOfFloat[4]) || (paramArrayOfFloat[4] < paramArrayOfFloat[6]))))
    {
      arrayOfDouble2[2] = (-paramArrayOfFloat[0] + 3.0F * paramArrayOfFloat[2] - 3.0F * paramArrayOfFloat[4] + paramArrayOfFloat[6]);
      arrayOfDouble2[1] = (2F * (paramArrayOfFloat[0] - 2F * paramArrayOfFloat[2] + paramArrayOfFloat[4]));
      arrayOfDouble2[0] = (-paramArrayOfFloat[0] + paramArrayOfFloat[2]);
      j = QuadCurve2D.solveQuadratic(arrayOfDouble2, arrayOfDouble3);
      for (k = 0; k < j; ++k)
        if ((arrayOfDouble3[k] > 0D) && (arrayOfDouble3[k] < 1D))
          arrayOfDouble1[(i++)] = arrayOfDouble3[k];
    }
    if ((((paramArrayOfFloat[1] > paramArrayOfFloat[3]) || (paramArrayOfFloat[3] > paramArrayOfFloat[5]) || (paramArrayOfFloat[5] > paramArrayOfFloat[7]))) && (((paramArrayOfFloat[1] < paramArrayOfFloat[3]) || (paramArrayOfFloat[3] < paramArrayOfFloat[5]) || (paramArrayOfFloat[5] < paramArrayOfFloat[7]))))
    {
      arrayOfDouble2[2] = (-paramArrayOfFloat[1] + 3.0F * paramArrayOfFloat[3] - 3.0F * paramArrayOfFloat[5] + paramArrayOfFloat[7]);
      arrayOfDouble2[1] = (2F * (paramArrayOfFloat[1] - 2F * paramArrayOfFloat[3] + paramArrayOfFloat[5]));
      arrayOfDouble2[0] = (-paramArrayOfFloat[1] + paramArrayOfFloat[3]);
      j = QuadCurve2D.solveQuadratic(arrayOfDouble2, arrayOfDouble3);
      for (k = 0; k < j; ++k)
        if ((arrayOfDouble3[k] > 0D) && (arrayOfDouble3[k] < 1D))
          arrayOfDouble1[(i++)] = arrayOfDouble3[k];
    }
    if (i > 0)
    {
      Arrays.sort(arrayOfDouble1, 0, i);
      ProcessFirstMonotonicPartOfCubic(paramProcessHandler, paramArrayOfFloat, paramArrayOfInt, (float)arrayOfDouble1[0]);
      for (j = 1; j < i; ++j)
      {
        double d = arrayOfDouble1[j] - arrayOfDouble1[(j - 1)];
        if (d > 0D)
          ProcessFirstMonotonicPartOfCubic(paramProcessHandler, paramArrayOfFloat, paramArrayOfInt, (float)(d / (1D - arrayOfDouble1[(j - 1)])));
      }
    }
    ProcessMonotonicCubic(paramProcessHandler, paramArrayOfFloat, paramArrayOfInt);
  }

  private static void ProcessFirstMonotonicPartOfCubic(ProcessHandler paramProcessHandler, float[] paramArrayOfFloat, int[] paramArrayOfInt, float paramFloat)
  {
    float[] arrayOfFloat = new float[8];
    arrayOfFloat[0] = paramArrayOfFloat[0];
    arrayOfFloat[1] = paramArrayOfFloat[1];
    float f1 = paramArrayOfFloat[2] + paramFloat * (paramArrayOfFloat[4] - paramArrayOfFloat[2]);
    float f2 = paramArrayOfFloat[3] + paramFloat * (paramArrayOfFloat[5] - paramArrayOfFloat[3]);
    arrayOfFloat[2] = (paramArrayOfFloat[0] + paramFloat * (paramArrayOfFloat[2] - paramArrayOfFloat[0]));
    arrayOfFloat[3] = (paramArrayOfFloat[1] + paramFloat * (paramArrayOfFloat[3] - paramArrayOfFloat[1]));
    arrayOfFloat[4] = (arrayOfFloat[2] + paramFloat * (f1 - arrayOfFloat[2]));
    arrayOfFloat[5] = (arrayOfFloat[3] + paramFloat * (f2 - arrayOfFloat[3]));
    paramArrayOfFloat[4] += paramFloat * (paramArrayOfFloat[6] - paramArrayOfFloat[4]);
    paramArrayOfFloat[5] += paramFloat * (paramArrayOfFloat[7] - paramArrayOfFloat[5]);
    paramArrayOfFloat[2] = (f1 + paramFloat * (paramArrayOfFloat[4] - f1));
    paramArrayOfFloat[3] = (f2 + paramFloat * (paramArrayOfFloat[5] - f2));
    paramArrayOfFloat[0] = (arrayOfFloat[6] = arrayOfFloat[4] + paramFloat * (paramArrayOfFloat[2] - arrayOfFloat[4]));
    paramArrayOfFloat[1] = (arrayOfFloat[7] = arrayOfFloat[5] + paramFloat * (paramArrayOfFloat[3] - arrayOfFloat[5]));
    ProcessMonotonicCubic(paramProcessHandler, arrayOfFloat, paramArrayOfInt);
  }

  private static void ProcessLine(ProcessHandler paramProcessHandler, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, int[] paramArrayOfInt)
  {
    int i;
    int j;
    int k;
    int l;
    boolean bool1 = false;
    float[] arrayOfFloat = { paramFloat1, paramFloat2, paramFloat3, paramFloat4, 0F, 0F };
    float f1 = paramProcessHandler.dhnd.xMinf;
    float f2 = paramProcessHandler.dhnd.yMinf;
    float f3 = paramProcessHandler.dhnd.xMaxf;
    float f4 = paramProcessHandler.dhnd.yMaxf;
    int i3 = TESTANDCLIP(f2, f4, arrayOfFloat, 1, 0, 3, 2);
    if (i3 == 4)
      return;
    bool1 = IS_CLIPPED(i3);
    i3 = TESTANDCLIP(f2, f4, arrayOfFloat, 3, 2, 1, 0);
    if (i3 == 4)
      return;
    boolean bool2 = IS_CLIPPED(i3);
    bool1 = (bool1) || (bool2);
    if (paramProcessHandler.clipMode == 0)
    {
      i3 = TESTANDCLIP(f1, f3, arrayOfFloat, 0, 1, 2, 3);
      if (i3 == 4)
        return;
      bool1 = (bool1) || (IS_CLIPPED(i3));
      i3 = TESTANDCLIP(f1, f3, arrayOfFloat, 2, 3, 0, 1);
      if (i3 == 4)
        return;
      bool2 = (bool2) || (IS_CLIPPED(i3));
      bool1 = (bool1) || (bool2);
      i = (int)(arrayOfFloat[0] * 1024.0F);
      j = (int)(arrayOfFloat[1] * 1024.0F);
      k = (int)(arrayOfFloat[2] * 1024.0F);
      l = (int)(arrayOfFloat[3] * 1024.0F);
      paramProcessHandler.processFixedLine(i, j, k, l, paramArrayOfInt, bool1, bool2);
    }
    else
    {
      int i1;
      int i2;
      i3 = CLIPCLAMP(f1, f3, arrayOfFloat, 0, 1, 2, 3, 4, 5);
      i = (int)(arrayOfFloat[0] * 1024.0F);
      j = (int)(arrayOfFloat[1] * 1024.0F);
      if (i3 == 0)
      {
        i1 = (int)(arrayOfFloat[4] * 1024.0F);
        i2 = (int)(arrayOfFloat[5] * 1024.0F);
        paramProcessHandler.processFixedLine(i1, i2, i, j, paramArrayOfInt, false, bool2);
      }
      else if (i3 == 4)
      {
        return;
      }
      i3 = CLIPCLAMP(f1, f3, arrayOfFloat, 2, 3, 0, 1, 4, 5);
      bool2 = (bool2) || (i3 == 1);
      k = (int)(arrayOfFloat[2] * 1024.0F);
      l = (int)(arrayOfFloat[3] * 1024.0F);
      paramProcessHandler.processFixedLine(i, j, k, l, paramArrayOfInt, false, bool2);
      if (i3 == 0)
      {
        i1 = (int)(arrayOfFloat[4] * 1024.0F);
        i2 = (int)(arrayOfFloat[5] * 1024.0F);
        paramProcessHandler.processFixedLine(k, l, i1, i2, paramArrayOfInt, false, bool2);
      }
    }
  }

  private static boolean doProcessPath(ProcessHandler paramProcessHandler, Path2D.Float paramFloat, float paramFloat1, float paramFloat2)
  {
    float[] arrayOfFloat1 = new float[8];
    float[] arrayOfFloat2 = new float[8];
    float[] arrayOfFloat3 = { 0F, 0F };
    float[] arrayOfFloat4 = new float[2];
    int[] arrayOfInt = new int[5];
    int i = 0;
    int j = 0;
    arrayOfInt[0] = 0;
    paramProcessHandler.dhnd.adjustBounds(-1048576, -1048576, 1048576, 1048576);
    if (paramProcessHandler.dhnd.strokeControl == 2)
    {
      arrayOfFloat3[0] = -0.5F;
      arrayOfFloat3[1] = -0.5F;
      paramFloat1 = (float)(paramFloat1 - 0.5D);
      paramFloat2 = (float)(paramFloat2 - 0.5D);
    }
    PathIterator localPathIterator = paramFloat.getPathIterator(null);
    while (!(localPathIterator.isDone()))
    {
      float f1;
      float f2;
      switch (localPathIterator.currentSegment(arrayOfFloat1))
      {
      case 0:
        if ((i != 0) && (j == 0))
        {
          if ((paramProcessHandler.clipMode == 1) && (((arrayOfFloat2[0] != arrayOfFloat3[0]) || (arrayOfFloat2[1] != arrayOfFloat3[1]))))
            ProcessLine(paramProcessHandler, arrayOfFloat2[0], arrayOfFloat2[1], arrayOfFloat3[0], arrayOfFloat3[1], arrayOfInt);
          paramProcessHandler.processEndSubPath();
        }
        arrayOfFloat2[0] = (arrayOfFloat1[0] + paramFloat1);
        arrayOfFloat2[1] = (arrayOfFloat1[1] + paramFloat2);
        if ((arrayOfFloat2[0] < 85070586659632215000000000000000000000.0F) && (arrayOfFloat2[0] > -85070586659632215000000000000000000000.0F) && (arrayOfFloat2[1] < 85070586659632215000000000000000000000.0F) && (arrayOfFloat2[1] > -85070586659632215000000000000000000000.0F))
        {
          i = 1;
          j = 0;
          arrayOfFloat3[0] = arrayOfFloat2[0];
          arrayOfFloat3[1] = arrayOfFloat2[1];
        }
        else
        {
          j = 1;
        }
        arrayOfInt[0] = 0;
        break;
      case 1:
        f1 = arrayOfFloat2[2] = arrayOfFloat1[0] + paramFloat1;
        f2 = arrayOfFloat2[3] = arrayOfFloat1[1] + paramFloat2;
        if ((f1 < 85070586659632215000000000000000000000.0F) && (f1 > -85070586659632215000000000000000000000.0F) && (f2 < 85070586659632215000000000000000000000.0F) && (f2 > -85070586659632215000000000000000000000.0F))
          if (j != 0)
          {
            arrayOfFloat2[0] = (arrayOfFloat3[0] = f1);
            arrayOfFloat2[1] = (arrayOfFloat3[1] = f2);
            i = 1;
            j = 0;
          }
          else
          {
            ProcessLine(paramProcessHandler, arrayOfFloat2[0], arrayOfFloat2[1], arrayOfFloat2[2], arrayOfFloat2[3], arrayOfInt);
            arrayOfFloat2[0] = f1;
            arrayOfFloat2[1] = f2;
          }
        break;
      case 2:
        arrayOfFloat2[2] = (arrayOfFloat1[0] + paramFloat1);
        arrayOfFloat2[3] = (arrayOfFloat1[1] + paramFloat2);
        f1 = arrayOfFloat2[4] = arrayOfFloat1[2] + paramFloat1;
        f2 = arrayOfFloat2[5] = arrayOfFloat1[3] + paramFloat2;
        if ((f1 < 85070586659632215000000000000000000000.0F) && (f1 > -85070586659632215000000000000000000000.0F) && (f2 < 85070586659632215000000000000000000000.0F) && (f2 > -85070586659632215000000000000000000000.0F))
          if (j != 0)
          {
            arrayOfFloat2[0] = (arrayOfFloat3[0] = f1);
            arrayOfFloat2[1] = (arrayOfFloat3[1] = f2);
            i = 1;
            j = 0;
          }
          else
          {
            if ((arrayOfFloat2[2] < 85070586659632215000000000000000000000.0F) && (arrayOfFloat2[2] > -85070586659632215000000000000000000000.0F) && (arrayOfFloat2[3] < 85070586659632215000000000000000000000.0F) && (arrayOfFloat2[3] > -85070586659632215000000000000000000000.0F))
              ProcessQuad(paramProcessHandler, arrayOfFloat2, arrayOfInt);
            else
              ProcessLine(paramProcessHandler, arrayOfFloat2[0], arrayOfFloat2[1], arrayOfFloat2[4], arrayOfFloat2[5], arrayOfInt);
            arrayOfFloat2[0] = f1;
            arrayOfFloat2[1] = f2;
          }
        break;
      case 3:
        arrayOfFloat2[2] = (arrayOfFloat1[0] + paramFloat1);
        arrayOfFloat2[3] = (arrayOfFloat1[1] + paramFloat2);
        arrayOfFloat2[4] = (arrayOfFloat1[2] + paramFloat1);
        arrayOfFloat2[5] = (arrayOfFloat1[3] + paramFloat2);
        f1 = arrayOfFloat2[6] = arrayOfFloat1[4] + paramFloat1;
        f2 = arrayOfFloat2[7] = arrayOfFloat1[5] + paramFloat2;
        if ((f1 < 85070586659632215000000000000000000000.0F) && (f1 > -85070586659632215000000000000000000000.0F) && (f2 < 85070586659632215000000000000000000000.0F) && (f2 > -85070586659632215000000000000000000000.0F))
          if (j != 0)
          {
            arrayOfFloat2[0] = (arrayOfFloat3[0] = arrayOfFloat2[6]);
            arrayOfFloat2[1] = (arrayOfFloat3[1] = arrayOfFloat2[7]);
            i = 1;
            j = 0;
          }
          else
          {
            if ((arrayOfFloat2[2] < 85070586659632215000000000000000000000.0F) && (arrayOfFloat2[2] > -85070586659632215000000000000000000000.0F) && (arrayOfFloat2[3] < 85070586659632215000000000000000000000.0F) && (arrayOfFloat2[3] > -85070586659632215000000000000000000000.0F) && (arrayOfFloat2[4] < 85070586659632215000000000000000000000.0F) && (arrayOfFloat2[4] > -85070586659632215000000000000000000000.0F) && (arrayOfFloat2[5] < 85070586659632215000000000000000000000.0F) && (arrayOfFloat2[5] > -85070586659632215000000000000000000000.0F))
              ProcessCubic(paramProcessHandler, arrayOfFloat2, arrayOfInt);
            else
              ProcessLine(paramProcessHandler, arrayOfFloat2[0], arrayOfFloat2[1], arrayOfFloat2[6], arrayOfFloat2[7], arrayOfInt);
            arrayOfFloat2[0] = f1;
            arrayOfFloat2[1] = f2;
          }
        break;
      case 4:
        if ((i != 0) && (j == 0))
        {
          j = 0;
          if ((arrayOfFloat2[0] != arrayOfFloat3[0]) || (arrayOfFloat2[1] != arrayOfFloat3[1]))
          {
            ProcessLine(paramProcessHandler, arrayOfFloat2[0], arrayOfFloat2[1], arrayOfFloat3[0], arrayOfFloat3[1], arrayOfInt);
            arrayOfFloat2[0] = arrayOfFloat3[0];
            arrayOfFloat2[1] = arrayOfFloat3[1];
          }
          paramProcessHandler.processEndSubPath();
        }
      }
      localPathIterator.next();
    }
    if ((i & ((j == 0) ? 1 : 0)) != 0)
    {
      if ((paramProcessHandler.clipMode == 1) && (((arrayOfFloat2[0] != arrayOfFloat3[0]) || (arrayOfFloat2[1] != arrayOfFloat3[1]))))
        ProcessLine(paramProcessHandler, arrayOfFloat2[0], arrayOfFloat2[1], arrayOfFloat3[0], arrayOfFloat3[1], arrayOfInt);
      paramProcessHandler.processEndSubPath();
    }
    return true;
  }

  private static void FillPolygon(FillProcessHandler paramFillProcessHandler, int paramInt)
  {
    int i1 = paramFillProcessHandler.dhnd.xMax - 1;
    FillData localFillData = paramFillProcessHandler.fd;
    int i2 = localFillData.plgYMin;
    int i3 = localFillData.plgYMax;
    int i4 = (i3 - i2 >> 10) + 4;
    int i5 = i2 - 1 & 0xFFFFFC00;
    int i7 = (paramInt == 1) ? -1 : 1;
    List localList = localFillData.plgPnts;
    int k = localList.size();
    if (k <= 1)
      return;
    Point[] arrayOfPoint = new Point[i4];
    Point localPoint1 = (Point)localList.get(0);
    localPoint1.prev = null;
    for (int i8 = 0; i8 < k - 1; ++i8)
    {
      localPoint1 = (Point)localList.get(i8);
      Point localPoint3 = (Point)localList.get(i8 + 1);
      int i10 = localPoint1.y - i5 - 1 >> 10;
      localPoint1.nextByY = arrayOfPoint[i10];
      arrayOfPoint[i10] = localPoint1;
      localPoint1.next = localPoint3;
      localPoint3.prev = localPoint1;
    }
    Point localPoint2 = (Point)localList.get(k - 1);
    int i9 = localPoint2.y - i5 - 1 >> 10;
    localPoint2.nextByY = arrayOfPoint[i9];
    arrayOfPoint[i9] = localPoint2;
    ActiveEdgeList localActiveEdgeList = new ActiveEdgeList(null);
    int j = i5 + 1024;
    for (int i = 0; (j <= i3) && (i < i4); ++i)
    {
      int i12;
      for (Point localPoint4 = arrayOfPoint[i]; localPoint4 != null; localPoint4 = localPoint4.nextByY)
      {
        if ((localPoint4.prev != null) && (!(localPoint4.prev.lastPoint)))
          if ((localPoint4.prev.edge != null) && (localPoint4.prev.y <= j))
          {
            localActiveEdgeList.delete(localPoint4.prev.edge);
            localPoint4.prev.edge = null;
          }
          else if (localPoint4.prev.y > j)
          {
            localActiveEdgeList.insert(localPoint4.prev, j);
          }
        if ((!(localPoint4.lastPoint)) && (localPoint4.next != null))
          if ((localPoint4.edge != null) && (localPoint4.next.y <= j))
          {
            localActiveEdgeList.delete(localPoint4.edge);
            localPoint4.edge = null;
          }
          else if (localPoint4.next.y > j)
          {
            localActiveEdgeList.insert(localPoint4, j);
          }
      }
      if (localActiveEdgeList.isEmpty())
        break label663:
      localActiveEdgeList.sort();
      int i6 = 0;
      int l = 0;
      int i11 = i12 = paramFillProcessHandler.dhnd.xMin;
      for (Edge localEdge = localActiveEdgeList.head; localEdge != null; localEdge = localEdge.next)
      {
        i6 += localEdge.dir;
        if (((i6 & i7) != 0) && (l == 0))
        {
          i11 = localEdge.x + 1024 - 1 >> 10;
          l = 1;
        }
        if (((i6 & i7) == 0) && (l != 0))
        {
          i12 = localEdge.x - 1 >> 10;
          if (i11 <= i12)
            paramFillProcessHandler.dhnd.drawScanline(i11, i12, j >> 10);
          l = 0;
        }
        localEdge.x += localEdge.dx;
      }
      if ((l != 0) && (i11 <= i1))
        paramFillProcessHandler.dhnd.drawScanline(i11, i1, j >> 10);
      label663: j += 1024;
    }
  }

  private static class ActiveEdgeList
  {
    ProcessPath.Edge head;

    public boolean isEmpty()
    {
      return (this.head == null);
    }

    public void insert(ProcessPath.Point paramPoint, int paramInt)
    {
      int i3;
      int i4;
      int i5;
      int i6;
      ProcessPath.Point localPoint = paramPoint.next;
      int i = paramPoint.x;
      int j = paramPoint.y;
      int k = localPoint.x;
      int l = localPoint.y;
      if (j == l)
        return;
      int i1 = k - i;
      int i2 = l - j;
      if (j < l)
      {
        i4 = i;
        i5 = paramInt - j;
        i6 = -1;
      }
      else
      {
        i4 = k;
        i5 = paramInt - l;
        i6 = 1;
      }
      if ((i1 > 1048576.0F) || (i1 < -1048576.0F))
      {
        i3 = (int)(i1 * 1024.0D / i2);
        i4 += (int)(i1 * i5 / i2);
      }
      else
      {
        i3 = (i1 << 10) / i2;
        i4 += i1 * i5 / i2;
      }
      ProcessPath.Edge localEdge = new ProcessPath.Edge(paramPoint, i4, i3, i6);
      localEdge.next = this.head;
      localEdge.prev = null;
      if (this.head != null)
        this.head.prev = localEdge;
      this.head = (paramPoint.edge = localEdge);
    }

    public void delete(ProcessPath.Edge paramEdge)
    {
      ProcessPath.Edge localEdge1 = paramEdge.prev;
      ProcessPath.Edge localEdge2 = paramEdge.next;
      if (localEdge1 != null)
        localEdge1.next = localEdge2;
      else
        this.head = localEdge2;
      if (localEdge2 != null)
        localEdge2.prev = localEdge1;
    }

    public void sort()
    {
      Object localObject2 = null;
      int i = 1;
      if ((localObject2 != this.head.next) && (i != 0))
      {
        Object localObject1 = localEdge1 = this.head;
        localEdge2 = localEdge1.next;
        i = 0;
        while (true)
        {
          do
          {
            if (localEdge1 == localObject2);
            if (localEdge1.x >= localEdge2.x)
            {
              ProcessPath.Edge localEdge3;
              i = 1;
              if (localEdge1 == this.head)
              {
                localEdge3 = localEdge2.next;
                localEdge2.next = localEdge1;
                localEdge1.next = localEdge3;
                this.head = localEdge2;
                localObject1 = localEdge2;
              }
              else
              {
                localEdge3 = localEdge2.next;
                localEdge2.next = localEdge1;
                localEdge1.next = localEdge3;
                ((ProcessPath.Edge)localObject1).next = localEdge2;
                localObject1 = localEdge2;
              }
            }
            else
            {
              localObject1 = localEdge1;
              localEdge1 = localEdge1.next;
            }
            localEdge2 = localEdge1.next;
          }
          while (localEdge2 != localObject2);
          localObject2 = localEdge1;
        }
      }
      ProcessPath.Edge localEdge1 = this.head;
      ProcessPath.Edge localEdge2 = null;
      while (localEdge1 != null)
      {
        localEdge1.prev = localEdge2;
        localEdge2 = localEdge1;
        localEdge1 = localEdge1.next;
      }
    }
  }

  public static abstract class DrawHandler
  {
    public int xMin;
    public int yMin;
    public int xMax;
    public int yMax;
    public float xMinf;
    public float yMinf;
    public float xMaxf;
    public float yMaxf;
    public int strokeControl;

    public DrawHandler(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      setBounds(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
    }

    public void setBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      this.xMin = paramInt1;
      this.yMin = paramInt2;
      this.xMax = paramInt3;
      this.yMax = paramInt4;
      this.xMinf = (paramInt1 - 0.5F);
      this.yMinf = (paramInt2 - 0.5F);
      this.xMaxf = (paramInt3 - 0.5F - 0.0009765625F);
      this.yMaxf = (paramInt4 - 0.5F - 0.0009765625F);
    }

    public void setBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      this.strokeControl = paramInt5;
      setBounds(paramInt1, paramInt2, paramInt3, paramInt4);
    }

    public void adjustBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      if (this.xMin > paramInt1)
        paramInt1 = this.xMin;
      if (this.xMax < paramInt3)
        paramInt3 = this.xMax;
      if (this.yMin > paramInt2)
        paramInt2 = this.yMin;
      if (this.yMax < paramInt4)
        paramInt4 = this.yMax;
      setBounds(paramInt1, paramInt2, paramInt3, paramInt4);
    }

    public DrawHandler(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      this(paramInt1, paramInt2, paramInt3, paramInt4, 0);
    }

    public abstract void drawLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

    public abstract void drawPixel(int paramInt1, int paramInt2);

    public abstract void drawScanline(int paramInt1, int paramInt2, int paramInt3);
  }

  private static class DrawProcessHandler extends ProcessPath.ProcessHandler
  {
    ProcessPath.EndSubPathHandler processESP;

    public DrawProcessHandler(ProcessPath.DrawHandler paramDrawHandler, ProcessPath.EndSubPathHandler paramEndSubPathHandler)
    {
      super(paramDrawHandler, 0);
      this.dhnd = paramDrawHandler;
      this.processESP = paramEndSubPathHandler;
    }

    public void processEndSubPath()
    {
      this.processESP.processEndSubPath();
    }

    void PROCESS_LINE(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean, int[] paramArrayOfInt)
    {
      int i = paramInt1 >> 10;
      int j = paramInt2 >> 10;
      int k = paramInt3 >> 10;
      int l = paramInt4 >> 10;
      if ((i ^ k | j ^ l) == 0)
      {
        if ((paramBoolean) && (((this.dhnd.yMin > j) || (this.dhnd.yMax <= j) || (this.dhnd.xMin > i) || (this.dhnd.xMax <= i))))
          return;
        if (paramArrayOfInt[0] == 0)
        {
          paramArrayOfInt[0] = 1;
          paramArrayOfInt[1] = i;
          paramArrayOfInt[2] = j;
          paramArrayOfInt[3] = i;
          paramArrayOfInt[4] = j;
          this.dhnd.drawPixel(i, j);
        }
        else if ((((i != paramArrayOfInt[3]) || (j != paramArrayOfInt[4]))) && (((i != paramArrayOfInt[1]) || (j != paramArrayOfInt[2]))))
        {
          this.dhnd.drawPixel(i, j);
          paramArrayOfInt[3] = i;
          paramArrayOfInt[4] = j;
        }
        return;
      }
      if ((((!(paramBoolean)) || ((this.dhnd.yMin <= j) && (this.dhnd.yMax > j) && (this.dhnd.xMin <= i) && (this.dhnd.xMax > i)))) && (paramArrayOfInt[0] == 1) && ((((paramArrayOfInt[1] == i) && (paramArrayOfInt[2] == j)) || ((paramArrayOfInt[3] == i) && (paramArrayOfInt[4] == j)))))
        this.dhnd.drawPixel(i, j);
      this.dhnd.drawLine(i, j, k, l);
      if (paramArrayOfInt[0] == 0)
      {
        paramArrayOfInt[0] = 1;
        paramArrayOfInt[1] = i;
        paramArrayOfInt[2] = j;
        paramArrayOfInt[3] = i;
        paramArrayOfInt[4] = j;
      }
      if (((paramArrayOfInt[1] == k) && (paramArrayOfInt[2] == l)) || ((paramArrayOfInt[3] == k) && (paramArrayOfInt[4] == l)))
      {
        if ((paramBoolean) && (((this.dhnd.yMin > l) || (this.dhnd.yMax <= l) || (this.dhnd.xMin > k) || (this.dhnd.xMax <= k))))
          return;
        this.dhnd.drawPixel(k, l);
      }
      paramArrayOfInt[3] = k;
      paramArrayOfInt[4] = l;
    }

    void PROCESS_POINT(int paramInt1, int paramInt2, boolean paramBoolean, int[] paramArrayOfInt)
    {
      int i = paramInt1 >> 10;
      int j = paramInt2 >> 10;
      if ((paramBoolean) && (((this.dhnd.yMin > j) || (this.dhnd.yMax <= j) || (this.dhnd.xMin > i) || (this.dhnd.xMax <= i))))
        return;
      if (paramArrayOfInt[0] == 0)
      {
        paramArrayOfInt[0] = 1;
        paramArrayOfInt[1] = i;
        paramArrayOfInt[2] = j;
        paramArrayOfInt[3] = i;
        paramArrayOfInt[4] = j;
        this.dhnd.drawPixel(i, j);
      }
      else if ((((i != paramArrayOfInt[3]) || (j != paramArrayOfInt[4]))) && (((i != paramArrayOfInt[1]) || (j != paramArrayOfInt[2]))))
      {
        this.dhnd.drawPixel(i, j);
        paramArrayOfInt[3] = i;
        paramArrayOfInt[4] = j;
      }
    }

    public void processFixedLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt, boolean paramBoolean1, boolean paramBoolean2)
    {
      int j;
      int k;
      int l;
      int i1;
      int i = paramInt1 ^ paramInt3 | paramInt2 ^ paramInt4;
      if ((i & 0xFFFFFC00) == 0)
      {
        if (i == 0)
          PROCESS_POINT(paramInt1 + 512, paramInt2 + 512, paramBoolean1, paramArrayOfInt);
        return;
      }
      if ((paramInt1 == paramInt3) || (paramInt2 == paramInt4))
      {
        j = paramInt1 + 512;
        l = paramInt3 + 512;
        k = paramInt2 + 512;
        i1 = paramInt4 + 512;
      }
      else
      {
        int i8;
        int i9;
        int i10;
        int i2 = paramInt3 - paramInt1;
        int i3 = paramInt4 - paramInt2;
        int i4 = paramInt1 & 0xFFFFFC00;
        int i5 = paramInt2 & 0xFFFFFC00;
        int i6 = paramInt3 & 0xFFFFFC00;
        int i7 = paramInt4 & 0xFFFFFC00;
        if ((i4 == paramInt1) || (i5 == paramInt2))
        {
          j = paramInt1 + 512;
          k = paramInt2 + 512;
        }
        else
        {
          i8 = (paramInt1 < paramInt3) ? i4 + 1024 : i4;
          i9 = (paramInt2 < paramInt4) ? i5 + 1024 : i5;
          i10 = paramInt2 + (i8 - paramInt1) * i3 / i2;
          if ((i10 >= i5) && (i10 <= i5 + 1024))
          {
            j = i8;
            k = i10 + 512;
          }
          else
          {
            i10 = paramInt1 + (i9 - paramInt2) * i2 / i3;
            j = i10 + 512;
            k = i9;
          }
        }
        if ((i6 == paramInt3) || (i7 == paramInt4))
        {
          l = paramInt3 + 512;
          i1 = paramInt4 + 512;
        }
        else
        {
          i8 = (paramInt1 > paramInt3) ? i6 + 1024 : i6;
          i9 = (paramInt2 > paramInt4) ? i7 + 1024 : i7;
          i10 = paramInt4 + (i8 - paramInt3) * i3 / i2;
          if ((i10 >= i7) && (i10 <= i7 + 1024))
          {
            l = i8;
            i1 = i10 + 512;
          }
          else
          {
            i10 = paramInt3 + (i9 - paramInt4) * i2 / i3;
            l = i10 + 512;
            i1 = i9;
          }
        }
      }
      PROCESS_LINE(j, k, l, i1, paramBoolean1, paramArrayOfInt);
    }
  }

  private static class Edge
  {
    int x;
    int dx;
    ProcessPath.Point p;
    int dir;
    Edge prev;
    Edge next;

    public Edge(ProcessPath.Point paramPoint, int paramInt1, int paramInt2, int paramInt3)
    {
      this.p = paramPoint;
      this.x = paramInt1;
      this.dx = paramInt2;
      this.dir = paramInt3;
    }
  }

  public static abstract interface EndSubPathHandler
  {
    public abstract void processEndSubPath();
  }

  private static class FillData
  {
    List<ProcessPath.Point> plgPnts = new Vector(256);
    public int plgYMin;
    public int plgYMax;

    public void addPoint(int paramInt1, int paramInt2, boolean paramBoolean)
    {
      if (this.plgPnts.size() == 0)
      {
        this.plgYMin = (this.plgYMax = paramInt2);
      }
      else
      {
        this.plgYMin = ((this.plgYMin > paramInt2) ? paramInt2 : this.plgYMin);
        this.plgYMax = ((this.plgYMax < paramInt2) ? paramInt2 : this.plgYMax);
      }
      this.plgPnts.add(new ProcessPath.Point(paramInt1, paramInt2, paramBoolean));
    }

    public boolean isEmpty()
    {
      return (this.plgPnts.size() == 0);
    }

    public boolean isEnded()
    {
      return ((ProcessPath.Point)this.plgPnts.get(this.plgPnts.size() - 1)).lastPoint;
    }

    public boolean setEnded()
    {
      return (((ProcessPath.Point)this.plgPnts.get(this.plgPnts.size() - 1)).lastPoint = 1);
    }
  }

  private static class FillProcessHandler extends ProcessPath.ProcessHandler
  {
    ProcessPath.FillData fd = new ProcessPath.FillData();

    public void processFixedLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt, boolean paramBoolean1, boolean paramBoolean2)
    {
      if (paramBoolean1)
      {
        int[] arrayOfInt = { paramInt1, paramInt2, paramInt3, paramInt4, 0, 0 };
        int i = (int)(this.dhnd.xMinf * 1024.0F);
        int j = (int)(this.dhnd.xMaxf * 1024.0F);
        int k = (int)(this.dhnd.yMinf * 1024.0F);
        int l = (int)(this.dhnd.yMaxf * 1024.0F);
        int i1 = ProcessPath.access$100(k, l, arrayOfInt, 1, 0, 3, 2);
        if (i1 == 4)
          return;
        i1 = ProcessPath.access$100(k, l, arrayOfInt, 3, 2, 1, 0);
        if (i1 == 4)
          return;
        boolean bool = ProcessPath.access$200(i1);
        i1 = ProcessPath.access$300(i, j, arrayOfInt, 0, 1, 2, 3, 4, 5);
        if (i1 == 0)
          processFixedLine(arrayOfInt[4], arrayOfInt[5], arrayOfInt[0], arrayOfInt[1], paramArrayOfInt, false, bool);
        else if (i1 == 4)
          return;
        i1 = ProcessPath.access$300(i, j, arrayOfInt, 2, 3, 0, 1, 4, 5);
        bool = (bool) || (i1 == 1);
        processFixedLine(arrayOfInt[0], arrayOfInt[1], arrayOfInt[2], arrayOfInt[3], paramArrayOfInt, false, bool);
        if (i1 == 0)
          processFixedLine(arrayOfInt[2], arrayOfInt[3], arrayOfInt[4], arrayOfInt[5], paramArrayOfInt, false, bool);
        return;
      }
      if ((this.fd.isEmpty()) || (this.fd.isEnded()))
        this.fd.addPoint(paramInt1, paramInt2, false);
      this.fd.addPoint(paramInt3, paramInt4, false);
      if (paramBoolean2)
        this.fd.setEnded();
    }

    FillProcessHandler(ProcessPath.DrawHandler paramDrawHandler)
    {
      super(paramDrawHandler, 1);
    }

    public void processEndSubPath()
    {
      if (!(this.fd.isEmpty()))
        this.fd.setEnded();
    }
  }

  private static class Point
  {
    public int x;
    public int y;
    public boolean lastPoint;
    public Point prev;
    public Point next;
    public Point nextByY;
    public ProcessPath.Edge edge;

    public Point(int paramInt1, int paramInt2, boolean paramBoolean)
    {
      this.x = paramInt1;
      this.y = paramInt2;
      this.lastPoint = paramBoolean;
    }
  }

  public static abstract class ProcessHandler
  implements ProcessPath.EndSubPathHandler
  {
    ProcessPath.DrawHandler dhnd;
    int clipMode;

    public ProcessHandler(ProcessPath.DrawHandler paramDrawHandler, int paramInt)
    {
      this.dhnd = paramDrawHandler;
      this.clipMode = paramInt;
    }

    public abstract void processFixedLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int[] paramArrayOfInt, boolean paramBoolean1, boolean paramBoolean2);
  }
}
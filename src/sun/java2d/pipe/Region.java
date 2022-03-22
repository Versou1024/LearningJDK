package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class Region
{
  static final int INIT_SIZE = 50;
  static final int GROW_SIZE = 50;
  static final Region EMPTY_REGION = new Region(0, 0, 0, 0);
  static final Region WHOLE_REGION = new Region(-2147483648, -2147483648, 2147483647, 2147483647);
  int lox;
  int loy;
  int hix;
  int hiy;
  int endIndex;
  int[] bands;
  static final int INCLUDE_A = 1;
  static final int INCLUDE_B = 2;
  static final int INCLUDE_COMMON = 4;

  private static native void initIDs();

  public static int dimAdd(int paramInt1, int paramInt2)
  {
    if (paramInt2 <= 0)
      return paramInt1;
    if (paramInt2 += paramInt1 < paramInt1)
      return 2147483647;
    return paramInt2;
  }

  public static int clipAdd(int paramInt1, int paramInt2)
  {
    int i = paramInt1 + paramInt2;
    if (((i > paramInt1) ? 1 : 0) != ((paramInt2 > 0) ? 1 : 0))
      i = (paramInt2 < 0) ? -2147483648 : 2147483647;
    return i;
  }

  private Region(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.lox = paramInt1;
    this.loy = paramInt2;
    this.hix = paramInt3;
    this.hiy = paramInt4;
  }

  public static Region getInstance(Shape paramShape, AffineTransform paramAffineTransform)
  {
    return getInstance(WHOLE_REGION, false, paramShape, paramAffineTransform);
  }

  public static Region getInstance(Region paramRegion, Shape paramShape, AffineTransform paramAffineTransform)
  {
    return getInstance(paramRegion, false, paramShape, paramAffineTransform);
  }

  public static Region getInstance(Region paramRegion, boolean paramBoolean, Shape paramShape, AffineTransform paramAffineTransform)
  {
    int[] arrayOfInt = new int[4];
    ShapeSpanIterator localShapeSpanIterator = new ShapeSpanIterator(paramBoolean);
    try
    {
      localShapeSpanIterator.setOutputArea(paramRegion);
      localShapeSpanIterator.appendPath(paramShape.getPathIterator(paramAffineTransform));
      localShapeSpanIterator.getPathBox(arrayOfInt);
      Region localRegion1 = getInstance(arrayOfInt);
      localRegion1.appendSpans(localShapeSpanIterator);
      Region localRegion2 = localRegion1;
      return localRegion2;
    }
    finally
    {
      localShapeSpanIterator.dispose();
    }
  }

  public static Region getInstance(Rectangle paramRectangle)
  {
    return getInstanceXYWH(paramRectangle.x, paramRectangle.y, paramRectangle.width, paramRectangle.height);
  }

  public static Region getInstanceXYWH(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return getInstanceXYXY(paramInt1, paramInt2, dimAdd(paramInt1, paramInt3), dimAdd(paramInt2, paramInt4));
  }

  public static Region getInstance(int[] paramArrayOfInt)
  {
    return new Region(paramArrayOfInt[0], paramArrayOfInt[1], paramArrayOfInt[2], paramArrayOfInt[3]);
  }

  public static Region getInstanceXYXY(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return new Region(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public void setOutputArea(Rectangle paramRectangle)
  {
    setOutputAreaXYWH(paramRectangle.x, paramRectangle.y, paramRectangle.width, paramRectangle.height);
  }

  public void setOutputAreaXYWH(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    setOutputAreaXYXY(paramInt1, paramInt2, dimAdd(paramInt1, paramInt3), dimAdd(paramInt2, paramInt4));
  }

  public void setOutputArea(int[] paramArrayOfInt)
  {
    this.lox = paramArrayOfInt[0];
    this.loy = paramArrayOfInt[1];
    this.hix = paramArrayOfInt[2];
    this.hiy = paramArrayOfInt[3];
  }

  public void setOutputAreaXYXY(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.lox = paramInt1;
    this.loy = paramInt2;
    this.hix = paramInt3;
    this.hiy = paramInt4;
  }

  public void appendSpans(SpanIterator paramSpanIterator)
  {
    int[] arrayOfInt = new int[6];
    while (paramSpanIterator.nextSpan(arrayOfInt))
      appendSpan(arrayOfInt);
    endRow(arrayOfInt);
    calcBBox();
  }

  public Region getTranslatedRegion(int paramInt1, int paramInt2)
  {
    if ((paramInt1 | paramInt2) == 0)
      return this;
    int i = this.lox + paramInt1;
    int j = this.loy + paramInt2;
    int k = this.hix + paramInt1;
    int l = this.hiy + paramInt2;
    if (((i > this.lox) ? 1 : 0) == ((paramInt1 > 0) ? 1 : 0))
      if (((j > this.loy) ? 1 : 0) == ((paramInt2 > 0) ? 1 : 0))
        if (((k > this.hix) ? 1 : 0) == ((paramInt1 > 0) ? 1 : 0))
          if (((l > this.hiy) ? 1 : 0) == ((paramInt2 > 0) ? 1 : 0))
            break label149;
    return getSafeTranslatedRegion(paramInt1, paramInt2);
    label149: Region localRegion = new Region(i, j, k, l);
    int[] arrayOfInt1 = this.bands;
    if (arrayOfInt1 != null)
    {
      int i1 = this.endIndex;
      localRegion.endIndex = i1;
      int[] arrayOfInt2 = new int[i1];
      localRegion.bands = arrayOfInt2;
      int i2 = 0;
      if (i2 < i1)
      {
        int i3;
        arrayOfInt2[i2] = (arrayOfInt1[i2] + paramInt2);
        arrayOfInt2[(++i2)] = (arrayOfInt1[i2] + paramInt2);
        arrayOfInt2[(++i2)] = (i3 = arrayOfInt1[i2]);
        ++i2;
        while (true)
        {
          if (--i3 < 0);
          arrayOfInt2[i2] = (arrayOfInt1[i2] + paramInt1);
          ???[(++i2)] = (arrayOfInt1[i2] + paramInt1);
          ++i2;
        }
      }
    }
    return localRegion;
  }

  private Region getSafeTranslatedRegion(int paramInt1, int paramInt2)
  {
    int i = clipAdd(this.lox, paramInt1);
    int j = clipAdd(this.loy, paramInt2);
    int k = clipAdd(this.hix, paramInt1);
    int l = clipAdd(this.hiy, paramInt2);
    Region localRegion = new Region(i, j, k, l);
    int[] arrayOfInt1 = this.bands;
    if (arrayOfInt1 != null)
    {
      int i1 = this.endIndex;
      int[] arrayOfInt2 = new int[i1];
      int i2 = 0;
      int i3 = 0;
      while (i2 < i1)
      {
        int i4;
        int i5;
        int i6;
        arrayOfInt2[(i3++)] = (i5 = clipAdd(arrayOfInt1[(i2++)], paramInt2));
        arrayOfInt2[(i3++)] = (i6 = clipAdd(arrayOfInt1[(i2++)], paramInt2));
        arrayOfInt2[(i3++)] = (i4 = arrayOfInt1[(i2++)]);
        int i7 = i3;
        if (i5 < i6)
          while (true)
          {
            if (--i4 < 0)
              break label242;
            int i8 = clipAdd(arrayOfInt1[(i2++)], paramInt1);
            int i9 = clipAdd(arrayOfInt1[(i2++)], paramInt1);
            if (i8 < i9)
            {
              arrayOfInt2[(i3++)] = i8;
              arrayOfInt2[(i3++)] = i9;
            }
          }
        i2 += i4 * 2;
        if (i3 > i7)
          label242: arrayOfInt2[(i7 - 1)] = ((i3 - i7) / 2);
        else
          i3 = i7 - 3;
      }
      if (i3 <= 5)
      {
        if (i3 < 5)
        {
          localRegion.lox = (localRegion.loy = localRegion.hix = localRegion.hiy = 0);
        }
        else
        {
          localRegion.loy = arrayOfInt2[0];
          localRegion.hiy = arrayOfInt2[1];
          localRegion.lox = arrayOfInt2[3];
          localRegion.hix = arrayOfInt2[4];
        }
      }
      else
      {
        localRegion.endIndex = i3;
        localRegion.bands = arrayOfInt2;
      }
    }
    return localRegion;
  }

  public Region getIntersection(Rectangle paramRectangle)
  {
    return getIntersectionXYWH(paramRectangle.x, paramRectangle.y, paramRectangle.width, paramRectangle.height);
  }

  public Region getIntersectionXYWH(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return getIntersectionXYXY(paramInt1, paramInt2, dimAdd(paramInt1, paramInt3), dimAdd(paramInt2, paramInt4));
  }

  public Region getIntersectionXYXY(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (isInsideXYXY(paramInt1, paramInt2, paramInt3, paramInt4))
      return this;
    Region localRegion = new Region(paramInt1, paramInt2, paramInt3, paramInt4);
    if (this.bands != null)
      localRegion.appendSpans(getSpanIterator());
    return localRegion;
  }

  public Region getIntersection(Region paramRegion)
  {
    if (isInsideQuickCheck(paramRegion))
      return this;
    if (paramRegion.isInsideQuickCheck(this))
      return paramRegion;
    Region localRegion = new Region(paramRegion.lox, paramRegion.loy, paramRegion.hix, paramRegion.hiy);
    if (!(localRegion.isEmpty()))
      localRegion.filterSpans(this, paramRegion, 4);
    return localRegion;
  }

  public Region getUnion(Region paramRegion)
  {
    if ((paramRegion.isEmpty()) || (paramRegion.isInsideQuickCheck(this)))
      return this;
    if ((isEmpty()) || (isInsideQuickCheck(paramRegion)))
      return paramRegion;
    Region localRegion = new Region(paramRegion.lox, paramRegion.loy, paramRegion.hix, paramRegion.hiy);
    localRegion.filterSpans(this, paramRegion, 7);
    return localRegion;
  }

  public Region getDifference(Region paramRegion)
  {
    if (!(paramRegion.intersectsQuickCheck(this)))
      return this;
    if (isInsideQuickCheck(paramRegion))
      return EMPTY_REGION;
    Region localRegion = new Region(this.lox, this.loy, this.hix, this.hiy);
    localRegion.filterSpans(this, paramRegion, 1);
    return localRegion;
  }

  public Region getExclusiveOr(Region paramRegion)
  {
    if (paramRegion.isEmpty())
      return this;
    if (isEmpty())
      return paramRegion;
    Region localRegion = new Region(paramRegion.lox, paramRegion.loy, paramRegion.hix, paramRegion.hiy);
    localRegion.filterSpans(this, paramRegion, 3);
    return localRegion;
  }

  private void filterSpans(Region paramRegion1, Region paramRegion2, int paramInt)
  {
    int[] arrayOfInt1 = paramRegion1.bands;
    int[] arrayOfInt2 = paramRegion2.bands;
    if (arrayOfInt1 == null)
      arrayOfInt1 = { paramRegion1.loy, paramRegion1.hiy, 1, paramRegion1.lox, paramRegion1.hix };
    if (arrayOfInt2 == null)
      arrayOfInt2 = { paramRegion2.loy, paramRegion2.hiy, 1, paramRegion2.lox, paramRegion2.hix };
    int[] arrayOfInt3 = new int[6];
    int i = 0;
    int j = arrayOfInt1[(i++)];
    int k = arrayOfInt1[(i++)];
    int l = arrayOfInt1[(i++)];
    l = i + 2 * l;
    int i1 = 0;
    int i2 = arrayOfInt2[(i1++)];
    int i3 = arrayOfInt2[(i1++)];
    int i4 = arrayOfInt2[(i1++)];
    i4 = i1 + 2 * i4;
    int i5 = this.loy;
    while (true)
    {
      while (true)
      {
        while (true)
        {
          while (true)
          {
            while (true)
            {
              while (true)
              {
                if (i5 >= this.hiy)
                  break label911;
                if (i5 < k)
                  break label284;
                if (l >= paramRegion1.endIndex)
                  break;
                i = l;
                j = arrayOfInt1[(i++)];
                k = arrayOfInt1[(i++)];
                l = arrayOfInt1[(i++)];
                l = i + 2 * l;
              }
              if ((paramInt & 0x2) == 0)
                break label911:
              j = k = this.hiy;
            }
            label284: if (i5 < i3)
              break label367;
            if (i4 >= paramRegion2.endIndex)
              break;
            i1 = i4;
            i2 = arrayOfInt2[(i1++)];
            i3 = arrayOfInt2[(i1++)];
            i4 = arrayOfInt2[(i1++)];
            i4 = i1 + 2 * i4;
          }
          if ((paramInt & 0x1) == 0)
            break label911:
          i2 = i3 = this.hiy;
        }
        label367: if (i5 >= i2)
          break label467;
        if (i5 >= j)
          break;
        i5 = Math.min(j, i2);
      }
      int i6 = Math.min(k, i2);
      if ((paramInt & 0x1) != 0)
      {
        arrayOfInt3[1] = i5;
        arrayOfInt3[3] = i6;
        int i7 = i;
        while (i7 < l)
        {
          arrayOfInt3[0] = arrayOfInt1[(i7++)];
          arrayOfInt3[2] = arrayOfInt1[(i7++)];
          appendSpan(arrayOfInt3);
        }
        break label904:
        if (i5 < j)
        {
          label467: i6 = Math.min(i3, j);
          if ((paramInt & 0x2) != 0)
          {
            arrayOfInt3[1] = i5;
            arrayOfInt3[3] = i6;
            i7 = i1;
            while (i7 < i4)
            {
              arrayOfInt3[0] = arrayOfInt2[(i7++)];
              arrayOfInt3[2] = arrayOfInt2[(i7++)];
              label708: label766: appendSpan(arrayOfInt3);
            }
          }
        }
        else
        {
          i6 = Math.min(k, i3);
          arrayOfInt3[1] = i5;
          arrayOfInt3[3] = i6;
          i7 = i;
          int i8 = i1;
          int i9 = arrayOfInt1[(i7++)];
          int i10 = arrayOfInt1[(i7++)];
          int i11 = arrayOfInt2[(i8++)];
          int i12 = arrayOfInt2[(i8++)];
          int i13 = Math.min(i9, i11);
          if (i13 < this.lox)
            i13 = this.lox;
          while (true)
          {
            int i14;
            int i15;
            while (true)
            {
              while (true)
              {
                while (true)
                {
                  while (true)
                  {
                    if (i13 >= this.hix)
                      break label904;
                    if (i13 < i10)
                      break label708;
                    if (i7 >= l)
                      break;
                    i9 = arrayOfInt1[(i7++)];
                    i10 = arrayOfInt1[(i7++)];
                  }
                  if ((paramInt & 0x2) == 0)
                    break label904:
                  i9 = i10 = this.hix;
                }
                if (i13 < i12)
                  break label766;
                if (i8 >= i4)
                  break;
                i11 = arrayOfInt2[(i8++)];
                i12 = arrayOfInt2[(i8++)];
              }
              if ((paramInt & 0x1) == 0)
                break label904:
              i11 = i12 = this.hix;
            }
            if (i13 < i11)
            {
              if (i13 < i9)
              {
                i14 = Math.min(i9, i11);
                i15 = 0;
              }
              else
              {
                i14 = Math.min(i10, i11);
                i15 = ((paramInt & 0x1) != 0) ? 1 : 0;
              }
            }
            else if (i13 < i9)
            {
              i14 = Math.min(i9, i12);
              i15 = ((paramInt & 0x2) != 0) ? 1 : 0;
            }
            else
            {
              i14 = Math.min(i10, i12);
              i15 = ((paramInt & 0x4) != 0) ? 1 : 0;
            }
            if (i15 != 0)
            {
              arrayOfInt3[0] = i13;
              arrayOfInt3[2] = i14;
              appendSpan(arrayOfInt3);
            }
            i13 = i14;
          }
        }
      }
      label904: i5 = i6;
    }
    label911: endRow(arrayOfInt3);
    calcBBox();
  }

  public Region getBoundsIntersection(Rectangle paramRectangle)
  {
    return getBoundsIntersectionXYWH(paramRectangle.x, paramRectangle.y, paramRectangle.width, paramRectangle.height);
  }

  public Region getBoundsIntersectionXYWH(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return getBoundsIntersectionXYXY(paramInt1, paramInt2, dimAdd(paramInt1, paramInt3), dimAdd(paramInt2, paramInt4));
  }

  public Region getBoundsIntersectionXYXY(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((this.bands == null) && (this.lox >= paramInt1) && (this.loy >= paramInt2) && (this.hix <= paramInt3) && (this.hiy <= paramInt4))
      return this;
    return new Region(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public Region getBoundsIntersection(Region paramRegion)
  {
    if (encompasses(paramRegion))
      return paramRegion;
    if (paramRegion.encompasses(this))
      return this;
    return new Region(paramRegion.lox, paramRegion.loy, paramRegion.hix, paramRegion.hiy);
  }

  private void appendSpan(int[] paramArrayOfInt)
  {
    int i;
    int j;
    int k;
    int l;
    if ((i = paramArrayOfInt[0]) < this.lox)
      i = this.lox;
    if ((j = paramArrayOfInt[1]) < this.loy)
      j = this.loy;
    if ((k = paramArrayOfInt[2]) > this.hix)
      k = this.hix;
    if ((l = paramArrayOfInt[3]) > this.hiy)
      l = this.hiy;
    if ((k <= i) || (l <= j))
      return;
    int i1 = paramArrayOfInt[4];
    if ((this.endIndex == 0) || (j >= this.bands[(i1 + 1)]))
    {
      if (this.bands == null)
      {
        this.bands = new int[50];
      }
      else
      {
        needSpace(5);
        endRow(paramArrayOfInt);
        i1 = paramArrayOfInt[4];
      }
      this.bands[(this.endIndex++)] = j;
      this.bands[(this.endIndex++)] = l;
      this.bands[(this.endIndex++)] = 0;
    }
    else if ((j == this.bands[i1]) && (l == this.bands[(i1 + 1)]) && (i >= this.bands[(this.endIndex - 1)]))
    {
      if (i == this.bands[(this.endIndex - 1)])
      {
        this.bands[(this.endIndex - 1)] = k;
        return;
      }
      needSpace(2);
    }
    else
    {
      throw new InternalError("bad span");
    }
    this.bands[(this.endIndex++)] = i;
    this.bands[(this.endIndex++)] = k;
    this.bands[(i1 + 2)] += 1;
  }

  private void needSpace(int paramInt)
  {
    if (this.endIndex + paramInt >= this.bands.length)
    {
      int[] arrayOfInt = new int[this.bands.length + 50];
      System.arraycopy(this.bands, 0, arrayOfInt, 0, this.endIndex);
      this.bands = arrayOfInt;
    }
  }

  private void endRow(int[] paramArrayOfInt)
  {
    int i = paramArrayOfInt[4];
    int j = paramArrayOfInt[5];
    if (i > j)
    {
      int[] arrayOfInt = this.bands;
      if ((arrayOfInt[(j + 1)] == arrayOfInt[i]) && (arrayOfInt[(j + 2)] == arrayOfInt[(i + 2)]))
      {
        int k = arrayOfInt[(i + 2)] * 2;
        i += 3;
        j += 3;
        while (k > 0)
        {
          if (arrayOfInt[(i++)] != arrayOfInt[(j++)])
            break;
          --k;
        }
        if (k == 0)
        {
          arrayOfInt[(paramArrayOfInt[5] + 1)] = arrayOfInt[(j + 1)];
          this.endIndex = j;
          return;
        }
      }
    }
    paramArrayOfInt[5] = paramArrayOfInt[4];
    paramArrayOfInt[4] = this.endIndex;
  }

  private void calcBBox()
  {
    int[] arrayOfInt = this.bands;
    if (this.endIndex <= 5)
    {
      if (this.endIndex == 0)
      {
        this.lox = (this.loy = this.hix = this.hiy = 0);
      }
      else
      {
        this.loy = arrayOfInt[0];
        this.hiy = arrayOfInt[1];
        this.lox = arrayOfInt[3];
        this.hix = arrayOfInt[4];
        this.endIndex = 0;
      }
      this.bands = null;
      return;
    }
    int i = this.hix;
    int j = this.lox;
    int k = 0;
    int l = 0;
    while (l < this.endIndex)
    {
      k = l;
      int i1 = arrayOfInt[(l + 2)];
      if (i > arrayOfInt[(l += 3)])
        i = arrayOfInt[l];
      l += i1 * 2;
      if (j < arrayOfInt[(l - 1)])
        j = arrayOfInt[(l - 1)];
    }
    this.lox = i;
    this.loy = arrayOfInt[0];
    this.hix = j;
    this.hiy = arrayOfInt[(k + 1)];
  }

  public final int getLoX()
  {
    return this.lox;
  }

  public final int getLoY()
  {
    return this.loy;
  }

  public final int getHiX()
  {
    return this.hix;
  }

  public final int getHiY()
  {
    return this.hiy;
  }

  public final int getWidth()
  {
    int i;
    if (this.hix < this.lox)
      return 0;
    if ((i = this.hix - this.lox) < 0)
      i = 2147483647;
    return i;
  }

  public final int getHeight()
  {
    int i;
    if (this.hiy < this.loy)
      return 0;
    if ((i = this.hiy - this.loy) < 0)
      i = 2147483647;
    return i;
  }

  public boolean isEmpty()
  {
    return ((this.hix <= this.lox) || (this.hiy <= this.loy));
  }

  public boolean isRectangular()
  {
    return (this.bands == null);
  }

  public boolean contains(int paramInt1, int paramInt2)
  {
    if ((paramInt1 < this.lox) || (paramInt1 >= this.hix) || (paramInt2 < this.loy) || (paramInt2 >= this.hiy))
      return false;
    if (this.bands == null)
      return true;
    int i = 0;
    while (true)
    {
      if (i >= this.endIndex)
        break label159;
      if (paramInt2 < this.bands[(i++)])
        return false;
      if (paramInt2 < this.bands[(i++)])
        break;
      j = this.bands[(i++)];
      i += j * 2;
    }
    int j = this.bands[(i++)];
    j = i + j * 2;
    do
    {
      if (i >= j)
        break label157;
      if (paramInt1 < this.bands[(i++)])
        return false;
    }
    while (paramInt1 >= this.bands[(i++)]);
    return true;
    label157: return false;
    label159: return false;
  }

  public boolean isInsideXYWH(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return isInsideXYXY(paramInt1, paramInt2, dimAdd(paramInt1, paramInt3), dimAdd(paramInt2, paramInt4));
  }

  public boolean isInsideXYXY(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return ((this.lox >= paramInt1) && (this.loy >= paramInt2) && (this.hix <= paramInt3) && (this.hiy <= paramInt4));
  }

  public boolean isInsideQuickCheck(Region paramRegion)
  {
    return ((paramRegion.bands == null) && (paramRegion.lox <= this.lox) && (paramRegion.loy <= this.loy) && (paramRegion.hix >= this.hix) && (paramRegion.hiy >= this.hiy));
  }

  public boolean intersectsQuickCheckXYXY(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return ((paramInt3 > this.lox) && (paramInt1 < this.hix) && (paramInt4 > this.loy) && (paramInt2 < this.hiy));
  }

  public boolean intersectsQuickCheck(Region paramRegion)
  {
    return ((paramRegion.hix > this.lox) && (paramRegion.lox < this.hix) && (paramRegion.hiy > this.loy) && (paramRegion.loy < this.hiy));
  }

  public boolean encompasses(Region paramRegion)
  {
    return ((this.bands == null) && (this.lox <= paramRegion.lox) && (this.loy <= paramRegion.loy) && (this.hix >= paramRegion.hix) && (this.hiy >= paramRegion.hiy));
  }

  public boolean encompassesXYWH(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return encompassesXYXY(paramInt1, paramInt2, dimAdd(paramInt1, paramInt3), dimAdd(paramInt2, paramInt4));
  }

  public boolean encompassesXYXY(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return ((this.bands == null) && (this.lox <= paramInt1) && (this.loy <= paramInt2) && (this.hix >= paramInt3) && (this.hiy >= paramInt4));
  }

  public void getBounds(int[] paramArrayOfInt)
  {
    paramArrayOfInt[0] = this.lox;
    paramArrayOfInt[1] = this.loy;
    paramArrayOfInt[2] = this.hix;
    paramArrayOfInt[3] = this.hiy;
  }

  public void clipBoxToBounds(int[] paramArrayOfInt)
  {
    if (paramArrayOfInt[0] < this.lox)
      paramArrayOfInt[0] = this.lox;
    if (paramArrayOfInt[1] < this.loy)
      paramArrayOfInt[1] = this.loy;
    if (paramArrayOfInt[2] > this.hix)
      paramArrayOfInt[2] = this.hix;
    if (paramArrayOfInt[3] > this.hiy)
      paramArrayOfInt[3] = this.hiy;
  }

  public RegionIterator getIterator()
  {
    return new RegionIterator(this);
  }

  public SpanIterator getSpanIterator()
  {
    return new RegionSpanIterator(this);
  }

  public SpanIterator getSpanIterator(int[] paramArrayOfInt)
  {
    SpanIterator localSpanIterator = getSpanIterator();
    localSpanIterator.intersectClipBox(paramArrayOfInt[0], paramArrayOfInt[1], paramArrayOfInt[2], paramArrayOfInt[3]);
    return localSpanIterator;
  }

  public SpanIterator filter(SpanIterator paramSpanIterator)
  {
    if (this.bands == null)
      paramSpanIterator.intersectClipBox(this.lox, this.loy, this.hix, this.hiy);
    else
      paramSpanIterator = new RegionClipSpanIterator(this, paramSpanIterator);
    return paramSpanIterator;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("Region[[");
    localStringBuffer.append(this.lox);
    localStringBuffer.append(", ");
    localStringBuffer.append(this.loy);
    localStringBuffer.append(" => ");
    localStringBuffer.append(this.hix);
    localStringBuffer.append(", ");
    localStringBuffer.append(this.hiy);
    localStringBuffer.append("]");
    if (this.bands != null)
    {
      int i = 0;
      while (i < this.endIndex)
      {
        localStringBuffer.append("y{");
        localStringBuffer.append(this.bands[(i++)]);
        localStringBuffer.append(",");
        localStringBuffer.append(this.bands[(i++)]);
        localStringBuffer.append("}[");
        int j = this.bands[(i++)];
        j = i + j * 2;
        while (i < j)
        {
          localStringBuffer.append("x(");
          localStringBuffer.append(this.bands[(i++)]);
          localStringBuffer.append(", ");
          localStringBuffer.append(this.bands[(i++)]);
          localStringBuffer.append(")");
        }
        localStringBuffer.append("]");
      }
    }
    localStringBuffer.append("]");
    return localStringBuffer.toString();
  }

  public int hashCode()
  {
    return ((isEmpty()) ? 0 : this.lox * 3 + this.loy * 5 + this.hix * 7 + this.hiy * 9);
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof Region))
      return false;
    Region localRegion = (Region)paramObject;
    if (isEmpty())
      return localRegion.isEmpty();
    if (localRegion.isEmpty())
      return false;
    if ((localRegion.lox != this.lox) || (localRegion.loy != this.loy) || (localRegion.hiy != this.hiy) || (localRegion.hiy != this.hiy))
      return false;
    if (this.bands == null)
      return (localRegion.bands == null);
    if (localRegion.bands == null)
      return false;
    if (this.endIndex != localRegion.endIndex)
      return false;
    int[] arrayOfInt1 = this.bands;
    int[] arrayOfInt2 = localRegion.bands;
    for (int i = 0; i < this.endIndex; ++i)
      if (arrayOfInt1[i] != arrayOfInt2[i])
        return false;
    return true;
  }

  static
  {
    initIDs();
  }
}
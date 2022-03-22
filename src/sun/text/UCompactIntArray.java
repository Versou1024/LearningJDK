package sun.text;

public final class UCompactIntArray
  implements Cloneable
{
  private static final int PLANEMASK = 196608;
  private static final int PLANESHIFT = 16;
  private static final int PLANECOUNT = 16;
  private static final int CODEPOINTMASK = 65535;
  private static final int UNICODECOUNT = 65536;
  private static final int BLOCKSHIFT = 7;
  private static final int BLOCKCOUNT = 128;
  private static final int INDEXSHIFT = 9;
  private static final int INDEXCOUNT = 512;
  private static final int BLOCKMASK = 127;
  private int defaultValue;
  private int[][] values;
  private short[][] indices;
  private boolean isCompact;
  private boolean[][] blockTouched;
  private boolean[] planeTouched;

  public UCompactIntArray()
  {
    this.values = new int[16][];
    this.indices = new short[16][];
    this.blockTouched = new boolean[16][];
    this.planeTouched = new boolean[16];
  }

  public UCompactIntArray(int paramInt)
  {
    this.defaultValue = paramInt;
  }

  public int elementAt(int paramInt)
  {
    int i = (paramInt & 0x30000) >> 16;
    if (this.planeTouched[i] == 0)
      return this.defaultValue;
    paramInt &= 65535;
    return this.values[i][((this.indices[i][(paramInt >> 7)] & 0xFFFF) + (paramInt & 0x7F))];
  }

  public void setElementAt(int paramInt1, int paramInt2)
  {
    if (this.isCompact)
      expand();
    int i = (paramInt1 & 0x30000) >> 16;
    if (this.planeTouched[i] == 0)
      initPlane(i);
    paramInt1 &= 65535;
    this.values[i][paramInt1] = paramInt2;
    this.blockTouched[i][(paramInt1 >> 7)] = 1;
  }

  public void compact()
  {
    if (this.isCompact)
      return;
    for (int i = 0; i < 16; ++i)
    {
      if (this.planeTouched[i] == 0)
        break label213:
      int j = 0;
      int k = 0;
      int l = -1;
      int i1 = 0;
      while (i1 < this.indices[i].length)
      {
        this.indices[i][i1] = -1;
        if ((this.blockTouched[i][i1] == 0) && (l != -1))
        {
          this.indices[i][i1] = l;
        }
        else
        {
          int i2 = j * 128;
          if (i1 > j)
            System.arraycopy(this.values[i], k, this.values[i], i2, 128);
          if (this.blockTouched[i][i1] == 0)
            l = (short)i2;
          this.indices[i][i1] = (short)i2;
          ++j;
        }
        ++i1;
        k += 128;
      }
      i1 = j * 128;
      int[] arrayOfInt = new int[i1];
      System.arraycopy(this.values[i], 0, arrayOfInt, 0, i1);
      this.values[i] = arrayOfInt;
      label213: this.blockTouched[i] = null;
    }
    this.isCompact = true;
  }

  private void expand()
  {
    if (this.isCompact)
    {
      for (int j = 0; j < 16; ++j)
      {
        if (this.planeTouched[j] == 0)
          break label133:
        this.blockTouched[j] = new boolean[512];
        int[] arrayOfInt = new int[65536];
        for (int i = 0; i < 65536; ++i)
        {
          arrayOfInt[i] = this.values[j][(this.indices[j][(i >> 7)] & 65535 + (i & 0x7F))];
          this.blockTouched[j][(i >> 7)] = 1;
        }
        for (i = 0; i < 512; ++i)
          this.indices[j][i] = (short)(i << 7);
        label133: this.values[j] = arrayOfInt;
      }
      this.isCompact = false;
    }
  }

  private void initPlane(int paramInt)
  {
    this.values[paramInt] = new int[65536];
    this.indices[paramInt] = new short[512];
    this.blockTouched[paramInt] = new boolean[512];
    this.planeTouched[paramInt] = true;
    if ((this.planeTouched[0] != 0) && (paramInt != 0))
      System.arraycopy(this.indices[0], 0, this.indices[paramInt], 0, 512);
    else
      for (i = 0; i < 512; ++i)
        this.indices[paramInt][i] = (short)(i << 7);
    for (int i = 0; i < 65536; ++i)
      this.values[paramInt][i] = this.defaultValue;
  }

  public int getKSize()
  {
    int i = 0;
    for (int j = 0; j < 16; ++j)
      if (this.planeTouched[j] != 0)
        i += this.values[j].length * 4 + this.indices[j].length * 2;
    return (i / 1024);
  }
}
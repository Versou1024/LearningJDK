package sun.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.BitSet;

public class VariableGridLayout extends GridLayout
{
  BitSet rowsSet;
  double[] rowFractions;
  BitSet colsSet;
  double[] colFractions;
  int rows;
  int cols;
  int hgap;
  int vgap;

  public VariableGridLayout(int paramInt1, int paramInt2)
  {
    this(paramInt1, paramInt2, 0, 0);
    if (paramInt1 != 0)
    {
      this.rowsSet = new BitSet(paramInt1);
      stdRowFractions(paramInt1);
    }
    if (paramInt2 != 0)
    {
      this.colsSet = new BitSet(paramInt2);
      stdColFractions(paramInt2);
    }
  }

  public VariableGridLayout(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super(paramInt1, paramInt2, paramInt3, paramInt4);
    this.rowsSet = new BitSet();
    this.rowFractions = null;
    this.colsSet = new BitSet();
    this.colFractions = null;
    this.rows = paramInt1;
    this.cols = paramInt2;
    this.hgap = paramInt3;
    this.vgap = paramInt4;
    if (paramInt1 != 0)
    {
      this.rowsSet = new BitSet(paramInt1);
      stdRowFractions(paramInt1);
    }
    if (paramInt2 != 0)
    {
      this.colsSet = new BitSet(paramInt2);
      stdColFractions(paramInt2);
    }
  }

  void stdRowFractions(int paramInt)
  {
    this.rowFractions = new double[paramInt];
    for (int i = 0; i < paramInt; ++i)
      this.rowFractions[i] = (1D / paramInt);
  }

  void stdColFractions(int paramInt)
  {
    this.colFractions = new double[paramInt];
    for (int i = 0; i < paramInt; ++i)
      this.colFractions[i] = (1D / paramInt);
  }

  public void setRowFraction(int paramInt, double paramDouble)
  {
    this.rowsSet.set(paramInt);
    this.rowFractions[paramInt] = paramDouble;
  }

  public void setColFraction(int paramInt, double paramDouble)
  {
    this.colsSet.set(paramInt);
    this.colFractions[paramInt] = paramDouble;
  }

  public double getRowFraction(int paramInt)
  {
    return this.rowFractions[paramInt];
  }

  public double getColFraction(int paramInt)
  {
    return this.colFractions[paramInt];
  }

  void allocateExtraSpace(double[] paramArrayOfDouble, BitSet paramBitSet)
  {
    double d1 = 0D;
    int i = 0;
    for (int j = 0; j < paramArrayOfDouble.length; ++j)
      if (paramBitSet.get(j))
        d1 += paramArrayOfDouble[j];
      else
        ++i;
    if (i != 0)
    {
      double d2 = (1D - d1) / i;
      for (j = 0; j < paramArrayOfDouble.length; ++j)
        if (!(paramBitSet.get(j)))
        {
          paramArrayOfDouble[j] = d2;
          paramBitSet.set(j);
        }
    }
  }

  void allocateExtraSpace()
  {
    allocateExtraSpace(this.rowFractions, this.rowsSet);
    allocateExtraSpace(this.colFractions, this.colsSet);
  }

  public void layoutContainer(Container paramContainer)
  {
    Insets localInsets = paramContainer.insets();
    int i = paramContainer.countComponents();
    int j = this.rows;
    int k = this.cols;
    if (j > 0)
      k = (i + j - 1) / j;
    else
      j = (i + k - 1) / k;
    if (this.rows == 0)
      stdRowFractions(j);
    if (this.cols == 0)
      stdColFractions(k);
    Dimension localDimension = paramContainer.size();
    int l = localDimension.width - localInsets.left + localInsets.right;
    int i1 = localDimension.height - localInsets.top + localInsets.bottom;
    l -= (k - 1) * this.hgap;
    i1 -= (j - 1) * this.vgap;
    allocateExtraSpace();
    int i2 = 0;
    int i3 = localInsets.left;
    while (i2 < k)
    {
      int i4 = (int)(getColFraction(i2) * l);
      int i5 = 0;
      int i6 = localInsets.top;
      while (i5 < j)
      {
        int i7 = i5 * k + i2;
        int i8 = (int)(getRowFraction(i5) * i1);
        if (i7 < i)
          paramContainer.getComponent(i7).reshape(i3, i6, i4, i8);
        i6 += i8 + this.vgap;
        ++i5;
      }
      i3 += i4 + this.hgap;
      ++i2;
    }
  }

  static String fracsToString(double[] paramArrayOfDouble)
  {
    String str = "[" + paramArrayOfDouble.length + "]";
    for (int i = 0; i < paramArrayOfDouble.length; ++i)
      str = str + "<" + paramArrayOfDouble[i] + ">";
    return str;
  }

  public String toString()
  {
    return getClass().getName() + "[hgap=" + this.hgap + ",vgap=" + this.vgap + ",rows=" + this.rows + ",cols=" + this.cols + ",rowFracs=" + fracsToString(this.rowFractions) + ",colFracs=" + fracsToString(this.colFractions) + "]";
  }
}
package sun.awt.geom;

import java.awt.geom.PathIterator;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

public abstract class Crossings
{
  public static final boolean debug = 0;
  int limit = 0;
  double[] yranges = new double[10];
  double xlo;
  double ylo;
  double xhi;
  double yhi;
  private Vector tmp = new Vector();

  public Crossings(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4)
  {
    this.xlo = paramDouble1;
    this.ylo = paramDouble2;
    this.xhi = paramDouble3;
    this.yhi = paramDouble4;
  }

  public final double getXLo()
  {
    return this.xlo;
  }

  public final double getYLo()
  {
    return this.ylo;
  }

  public final double getXHi()
  {
    return this.xhi;
  }

  public final double getYHi()
  {
    return this.yhi;
  }

  public abstract void record(double paramDouble1, double paramDouble2, int paramInt);

  public void print()
  {
    System.out.println("Crossings [");
    System.out.println("  bounds = [" + this.ylo + ", " + this.yhi + "]");
    for (int i = 0; i < this.limit; i += 2)
      System.out.println("  [" + this.yranges[i] + ", " + this.yranges[(i + 1)] + "]");
    System.out.println("]");
  }

  public final boolean isEmpty()
  {
    return (this.limit == 0);
  }

  public abstract boolean covers(double paramDouble1, double paramDouble2);

  public static Crossings findCrossings(Vector paramVector, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4)
  {
    EvenOdd localEvenOdd = new EvenOdd(paramDouble1, paramDouble2, paramDouble3, paramDouble4);
    Enumeration localEnumeration = paramVector.elements();
    while (localEnumeration.hasMoreElements())
    {
      Curve localCurve = (Curve)localEnumeration.nextElement();
      if (localCurve.accumulateCrossings(localEvenOdd))
        return null;
    }
    return localEvenOdd;
  }

  public static Crossings findCrossings(PathIterator paramPathIterator, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4)
  {
    Object localObject;
    if (paramPathIterator.getWindingRule() == 0)
      localObject = new EvenOdd(paramDouble1, paramDouble2, paramDouble3, paramDouble4);
    else
      localObject = new NonZero(paramDouble1, paramDouble2, paramDouble3, paramDouble4);
    double[] arrayOfDouble = new double[23];
    double d1 = 0D;
    double d2 = 0D;
    double d3 = 0D;
    double d4 = 0D;
    while (!(paramPathIterator.isDone()))
    {
      double d5;
      double d6;
      int i = paramPathIterator.currentSegment(arrayOfDouble);
      switch (i)
      {
      case 0:
        if ((d2 != d4) && (((Crossings)localObject).accumulateLine(d3, d4, d1, d2)))
          return null;
        d1 = d3 = arrayOfDouble[0];
        d2 = d4 = arrayOfDouble[1];
        break;
      case 1:
        d5 = arrayOfDouble[0];
        d6 = arrayOfDouble[1];
        if (((Crossings)localObject).accumulateLine(d3, d4, d5, d6))
          return null;
        d3 = d5;
        d4 = d6;
        break;
      case 2:
        d5 = arrayOfDouble[2];
        d6 = arrayOfDouble[3];
        if (((Crossings)localObject).accumulateQuad(d3, d4, arrayOfDouble))
          return null;
        d3 = d5;
        d4 = d6;
        break;
      case 3:
        d5 = arrayOfDouble[4];
        d6 = arrayOfDouble[5];
        if (((Crossings)localObject).accumulateCubic(d3, d4, arrayOfDouble))
          return null;
        d3 = d5;
        d4 = d6;
        break;
      case 4:
        if ((d2 != d4) && (((Crossings)localObject).accumulateLine(d3, d4, d1, d2)))
          return null;
        d3 = d1;
        d4 = d2;
      }
      paramPathIterator.next();
    }
    if ((d2 != d4) && (((Crossings)localObject).accumulateLine(d3, d4, d1, d2)))
      return null;
    return ((Crossings)localObject);
  }

  public boolean accumulateLine(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4)
  {
    if (paramDouble2 <= paramDouble4)
      return accumulateLine(paramDouble1, paramDouble2, paramDouble3, paramDouble4, 1);
    return accumulateLine(paramDouble3, paramDouble4, paramDouble1, paramDouble2, -1);
  }

  public boolean accumulateLine(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, int paramInt)
  {
    double d1;
    double d2;
    double d3;
    double d4;
    if ((this.yhi <= paramDouble2) || (this.ylo >= paramDouble4))
      return false;
    if ((paramDouble1 >= this.xhi) && (paramDouble3 >= this.xhi))
      return false;
    if (paramDouble2 == paramDouble4)
      return ((paramDouble1 >= this.xlo) || (paramDouble3 >= this.xlo));
    double d5 = paramDouble3 - paramDouble1;
    double d6 = paramDouble4 - paramDouble2;
    if (paramDouble2 < this.ylo)
    {
      d1 = paramDouble1 + (this.ylo - paramDouble2) * d5 / d6;
      d2 = this.ylo;
    }
    else
    {
      d1 = paramDouble1;
      d2 = paramDouble2;
    }
    if (this.yhi < paramDouble4)
    {
      d3 = paramDouble1 + (this.yhi - paramDouble2) * d5 / d6;
      d4 = this.yhi;
    }
    else
    {
      d3 = paramDouble3;
      d4 = paramDouble4;
    }
    if ((d1 >= this.xhi) && (d3 >= this.xhi))
      return false;
    if ((d1 > this.xlo) || (d3 > this.xlo))
      return true;
    record(d2, d4, paramInt);
    return false;
  }

  public boolean accumulateQuad(double paramDouble1, double paramDouble2, double[] paramArrayOfDouble)
  {
    if ((paramDouble2 < this.ylo) && (paramArrayOfDouble[1] < this.ylo) && (paramArrayOfDouble[3] < this.ylo))
      return false;
    if ((paramDouble2 > this.yhi) && (paramArrayOfDouble[1] > this.yhi) && (paramArrayOfDouble[3] > this.yhi))
      return false;
    if ((paramDouble1 > this.xhi) && (paramArrayOfDouble[0] > this.xhi) && (paramArrayOfDouble[2] > this.xhi))
      return false;
    if ((paramDouble1 < this.xlo) && (paramArrayOfDouble[0] < this.xlo) && (paramArrayOfDouble[2] < this.xlo))
    {
      if (paramDouble2 < paramArrayOfDouble[3])
        record(Math.max(paramDouble2, this.ylo), Math.min(paramArrayOfDouble[3], this.yhi), 1);
      else if (paramDouble2 > paramArrayOfDouble[3])
        record(Math.max(paramArrayOfDouble[3], this.ylo), Math.min(paramDouble2, this.yhi), -1);
      return false;
    }
    Curve.insertQuad(this.tmp, paramDouble1, paramDouble2, paramArrayOfDouble);
    Enumeration localEnumeration = this.tmp.elements();
    while (localEnumeration.hasMoreElements())
    {
      Curve localCurve = (Curve)localEnumeration.nextElement();
      if (localCurve.accumulateCrossings(this))
        return true;
    }
    this.tmp.clear();
    return false;
  }

  public boolean accumulateCubic(double paramDouble1, double paramDouble2, double[] paramArrayOfDouble)
  {
    if ((paramDouble2 < this.ylo) && (paramArrayOfDouble[1] < this.ylo) && (paramArrayOfDouble[3] < this.ylo) && (paramArrayOfDouble[5] < this.ylo))
      return false;
    if ((paramDouble2 > this.yhi) && (paramArrayOfDouble[1] > this.yhi) && (paramArrayOfDouble[3] > this.yhi) && (paramArrayOfDouble[5] > this.yhi))
      return false;
    if ((paramDouble1 > this.xhi) && (paramArrayOfDouble[0] > this.xhi) && (paramArrayOfDouble[2] > this.xhi) && (paramArrayOfDouble[4] > this.xhi))
      return false;
    if ((paramDouble1 < this.xlo) && (paramArrayOfDouble[0] < this.xlo) && (paramArrayOfDouble[2] < this.xlo) && (paramArrayOfDouble[4] < this.xlo))
    {
      if (paramDouble2 <= paramArrayOfDouble[5])
        record(Math.max(paramDouble2, this.ylo), Math.min(paramArrayOfDouble[5], this.yhi), 1);
      else
        record(Math.max(paramArrayOfDouble[5], this.ylo), Math.min(paramDouble2, this.yhi), -1);
      return false;
    }
    Curve.insertCubic(this.tmp, paramDouble1, paramDouble2, paramArrayOfDouble);
    Enumeration localEnumeration = this.tmp.elements();
    while (localEnumeration.hasMoreElements())
    {
      Curve localCurve = (Curve)localEnumeration.nextElement();
      if (localCurve.accumulateCrossings(this))
        return true;
    }
    this.tmp.clear();
    return false;
  }

  public static final class EvenOdd extends Crossings
  {
    public EvenOdd(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4)
    {
      super(paramDouble1, paramDouble2, paramDouble3, paramDouble4);
    }

    public final boolean covers(double paramDouble1, double paramDouble2)
    {
      return ((this.limit == 2) && (this.yranges[0] <= paramDouble1) && (this.yranges[1] >= paramDouble2));
    }

    public void record(double paramDouble1, double paramDouble2, int paramInt)
    {
      if (paramDouble1 >= paramDouble2)
        return;
      for (int i = 0; (i < this.limit) && (paramDouble1 > this.yranges[(i + 1)]); i += 2);
      int j = i;
      while (true)
      {
        double d1;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6;
        while (true)
        {
          if (i >= this.limit)
            break label247;
          d1 = this.yranges[(i++)];
          d2 = this.yranges[(i++)];
          if (paramDouble2 >= d1)
            break;
          this.yranges[(j++)] = paramDouble1;
          this.yranges[(j++)] = paramDouble2;
          paramDouble1 = d1;
          paramDouble2 = d2;
        }
        if (paramDouble1 < d1)
        {
          d3 = paramDouble1;
          d4 = d1;
        }
        else
        {
          d3 = d1;
          d4 = paramDouble1;
        }
        if (paramDouble2 < d2)
        {
          d5 = paramDouble2;
          d6 = d2;
        }
        else
        {
          d5 = d2;
          d6 = paramDouble2;
        }
        if (d4 == d5)
        {
          paramDouble1 = d3;
          paramDouble2 = d6;
        }
        else
        {
          if (d4 > d5)
          {
            paramDouble1 = d5;
            d5 = d4;
            d4 = paramDouble1;
          }
          if (d3 != d4)
          {
            this.yranges[(j++)] = d3;
            this.yranges[(j++)] = d4;
          }
          paramDouble1 = d5;
          paramDouble2 = d6;
        }
        if (paramDouble1 >= paramDouble2)
          break;
      }
      if ((j < i) && (i < this.limit))
        label247: System.arraycopy(this.yranges, i, this.yranges, j, this.limit - i);
      j += this.limit - i;
      if (paramDouble1 < paramDouble2)
      {
        if (j >= this.yranges.length)
        {
          double[] arrayOfDouble = new double[j + 10];
          System.arraycopy(this.yranges, 0, arrayOfDouble, 0, j);
          this.yranges = arrayOfDouble;
        }
        this.yranges[(j++)] = paramDouble1;
        this.yranges[(j++)] = paramDouble2;
      }
      this.limit = j;
    }
  }

  public static final class NonZero extends Crossings
  {
    private int[] crosscounts = new int[this.yranges.length / 2];

    public NonZero(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4)
    {
      super(paramDouble1, paramDouble2, paramDouble3, paramDouble4);
    }

    public final boolean covers(double paramDouble1, double paramDouble2)
    {
      int i = 0;
      while (true)
      {
        double d1;
        double d2;
        while (true)
        {
          if (i >= this.limit)
            break label70;
          d1 = this.yranges[(i++)];
          d2 = this.yranges[(i++)];
          if (paramDouble1 < d2)
            break;
        }
        if (paramDouble1 < d1)
          return false;
        if (paramDouble2 <= d2)
          return true;
        paramDouble1 = d2;
      }
      label70: return (paramDouble1 >= paramDouble2);
    }

    public void remove(int paramInt)
    {
      this.limit -= 2;
      int i = this.limit - paramInt;
      if (i > 0)
      {
        System.arraycopy(this.yranges, paramInt + 2, this.yranges, paramInt, i);
        System.arraycopy(this.crosscounts, paramInt / 2 + 1, this.crosscounts, paramInt / 2, i / 2);
      }
    }

    public void insert(int paramInt1, double paramDouble1, double paramDouble2, int paramInt2)
    {
      int i = this.limit - paramInt1;
      double[] arrayOfDouble = this.yranges;
      int[] arrayOfInt = this.crosscounts;
      if (this.limit >= this.yranges.length)
      {
        this.yranges = new double[this.limit + 10];
        System.arraycopy(arrayOfDouble, 0, this.yranges, 0, paramInt1);
        this.crosscounts = new int[(this.limit + 10) / 2];
        System.arraycopy(arrayOfInt, 0, this.crosscounts, 0, paramInt1 / 2);
      }
      if (i > 0)
      {
        System.arraycopy(arrayOfDouble, paramInt1, this.yranges, paramInt1 + 2, i);
        System.arraycopy(arrayOfInt, paramInt1 / 2, this.crosscounts, paramInt1 / 2 + 1, i / 2);
      }
      this.yranges[(paramInt1 + 0)] = paramDouble1;
      this.yranges[(paramInt1 + 1)] = paramDouble2;
      this.crosscounts[(paramInt1 / 2)] = paramInt2;
      this.limit += 2;
    }

    public void record(double paramDouble1, double paramDouble2, int paramInt)
    {
      if (paramDouble1 >= paramDouble2)
        return;
      for (int i = 0; (i < this.limit) && (paramDouble1 > this.yranges[(i + 1)]); i += 2);
      if (i < this.limit)
      {
        int j = this.crosscounts[(i / 2)];
        double d1 = this.yranges[(i + 0)];
        double d2 = this.yranges[(i + 1)];
        if ((d2 == paramDouble1) && (j == paramInt))
        {
          if (i + 2 == this.limit)
          {
            this.yranges[(i + 1)] = paramDouble2;
            return;
          }
          remove(i);
          paramDouble1 = d1;
          j = this.crosscounts[(i / 2)];
          d1 = this.yranges[(i + 0)];
          d2 = this.yranges[(i + 1)];
        }
        if (paramDouble2 < d1)
        {
          insert(i, paramDouble1, paramDouble2, paramInt);
          return;
        }
        if ((paramDouble2 == d1) && (j == paramInt))
        {
          this.yranges[i] = paramDouble1;
          return;
        }
        if (paramDouble1 < d1)
        {
          insert(i, paramDouble1, d1, paramInt);
          i += 2;
          paramDouble1 = d1;
        }
        else if (d1 < paramDouble1)
        {
          insert(i, d1, paramDouble1, j);
          i += 2;
          d1 = paramDouble1;
        }
        int k = j + paramInt;
        double d3 = Math.min(paramDouble2, d2);
        if (k == 0)
        {
          remove(i);
        }
        else
        {
          this.crosscounts[(i / 2)] = k;
          this.yranges[(i++)] = paramDouble1;
          this.yranges[(i++)] = d3;
        }
        paramDouble1 = d1 = d3;
        if (d1 < d2)
          insert(i, d1, d2, j);
      }
      if (paramDouble1 < paramDouble2)
        insert(i, paramDouble1, paramDouble2, paramInt);
    }
  }
}
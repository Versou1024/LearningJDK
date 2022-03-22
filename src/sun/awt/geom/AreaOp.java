package sun.awt.geom;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

public abstract class AreaOp
{
  public static final int CTAG_LEFT = 0;
  public static final int CTAG_RIGHT = 1;
  public static final int ETAG_IGNORE = 0;
  public static final int ETAG_ENTER = 1;
  public static final int ETAG_EXIT = -1;
  public static final int RSTAG_INSIDE = 1;
  public static final int RSTAG_OUTSIDE = -1;
  private static Comparator YXTopComparator = new Comparator()
  {
    public int compare(Object paramObject1, Object paramObject2)
    {
      double d2;
      Curve localCurve1 = ((Edge)paramObject1).getCurve();
      Curve localCurve2 = ((Edge)paramObject2).getCurve();
      double d1 = localCurve1.getYTop();
      if (d1 == (d2 = localCurve2.getYTop()))
      {
        d1 = localCurve1.getXTop();
        if (d1 == (d2 = localCurve2.getXTop()))
          return 0;
      }
      if (d1 < d2)
        return -1;
      return 1;
    }
  };
  private static CurveLink[] EmptyLinkList = new CurveLink[2];
  private static ChainEnd[] EmptyChainList = new ChainEnd[2];

  public abstract void newRow();

  public abstract int classify(Edge paramEdge);

  public abstract int getState();

  public Vector calculate(Vector paramVector1, Vector paramVector2)
  {
    Vector localVector = new Vector();
    addEdges(localVector, paramVector1, 0);
    addEdges(localVector, paramVector2, 1);
    localVector = pruneEdges(localVector);
    return localVector;
  }

  private static void addEdges(Vector paramVector1, Vector paramVector2, int paramInt)
  {
    Enumeration localEnumeration = paramVector2.elements();
    while (localEnumeration.hasMoreElements())
    {
      Curve localCurve = (Curve)localEnumeration.nextElement();
      if (localCurve.getOrder() > 0)
        paramVector1.add(new Edge(localCurve, paramInt));
    }
  }

  private Vector pruneEdges(Vector paramVector)
  {
    int i = paramVector.size();
    if (i < 2)
      return paramVector;
    Edge[] arrayOfEdge = (Edge[])(Edge[])paramVector.toArray(new Edge[i]);
    Arrays.sort(arrayOfEdge, YXTopComparator);
    int j = 0;
    int k = 0;
    int l = 0;
    int i1 = 0;
    double[] arrayOfDouble = new double[2];
    Vector localVector1 = new Vector();
    Vector localVector2 = new Vector();
    Vector localVector3 = new Vector();
    while (j < i)
    {
      Object localObject1;
      int i5;
      double d1 = arrayOfDouble[0];
      for (l = --k; l >= j; --l)
      {
        localObject1 = arrayOfEdge[l];
        if (((Edge)localObject1).getCurve().getYBot() > d1)
        {
          if (i1 > l)
            arrayOfEdge[i1] = localObject1;
          --i1;
        }
      }
      j = i1 + 1;
      if (j >= k)
      {
        if (k >= i)
          break;
        d1 = arrayOfEdge[k].getCurve().getYTop();
        if (d1 > arrayOfDouble[0])
          finalizeSubCurves(localVector1, localVector2);
        arrayOfDouble[0] = d1;
      }
      while (k < i)
      {
        localObject1 = arrayOfEdge[k];
        if (((Edge)localObject1).getCurve().getYTop() > d1)
          break;
        ++k;
      }
      arrayOfDouble[1] = arrayOfEdge[j].getCurve().getYBot();
      if (k < i)
      {
        d1 = arrayOfEdge[k].getCurve().getYTop();
        if (arrayOfDouble[1] > d1)
          arrayOfDouble[1] = d1;
      }
      int i2 = 1;
      for (l = j; l < k; ++l)
      {
        localObject1 = arrayOfEdge[l];
        ((Edge)localObject1).setEquivalence(0);
        for (i1 = l; i1 > j; --i1)
        {
          Edge localEdge = arrayOfEdge[(i1 - 1)];
          int i3 = ((Edge)localObject1).compareTo(localEdge, arrayOfDouble);
          if (arrayOfDouble[1] <= arrayOfDouble[0])
            throw new InternalError("backstepping to " + arrayOfDouble[1] + " from " + arrayOfDouble[0]);
          if (i3 >= 0)
          {
            if (i3 != 0)
              break;
            int i4 = localEdge.getEquivalence();
            if (i4 == 0)
            {
              i4 = i2++;
              localEdge.setEquivalence(i4);
            }
            ((Edge)localObject1).setEquivalence(i4);
            break;
          }
          arrayOfEdge[i1] = localEdge;
        }
        arrayOfEdge[i1] = localObject1;
      }
      newRow();
      double d2 = arrayOfDouble[0];
      double d3 = arrayOfDouble[1];
      for (l = j; l < k; ++l)
      {
        localObject1 = arrayOfEdge[l];
        int i6 = ((Edge)localObject1).getEquivalence();
        if (i6 != 0)
        {
          int i7 = getState();
          i5 = (i7 == 1) ? -1 : 1;
          Object localObject4 = null;
          Object localObject5 = localObject1;
          double d4 = d3;
          do
          {
            classify((Edge)localObject1);
            if ((localObject4 == null) && (((Edge)localObject1).isActiveFor(d2, i5)))
              localObject4 = localObject1;
            d1 = ((Edge)localObject1).getCurve().getYBot();
            if (d1 > d4)
            {
              localObject5 = localObject1;
              d4 = d1;
            }
            if (++l >= k)
              break;
          }
          while ((localObject1 = arrayOfEdge[l]).getEquivalence() == i6);
          --l;
          if (getState() == i7)
            i5 = 0;
          else
            localObject1 = (localObject4 != null) ? localObject4 : localObject5;
        }
        else
        {
          i5 = classify((Edge)localObject1);
        }
        if (i5 != 0)
        {
          ((Edge)localObject1).record(d3, i5);
          localVector3.add(new CurveLink(((Edge)localObject1).getCurve(), d2, d3, i5));
        }
      }
      if (getState() != -1)
      {
        System.out.println("Still inside at end of active edge list!");
        System.out.println("num curves = " + (k - j));
        System.out.println("num links = " + localVector3.size());
        System.out.println("y top = " + arrayOfDouble[0]);
        if (k < i)
          System.out.println("y top of next curve = " + arrayOfEdge[k].getCurve().getYTop());
        else
          System.out.println("no more curves");
        for (l = j; l < k; ++l)
        {
          localObject1 = arrayOfEdge[l];
          System.out.println(localObject1);
          i5 = ((Edge)localObject1).getEquivalence();
          if (i5 != 0)
            System.out.println("  was equal to " + i5 + "...");
        }
      }
      resolveLinks(localVector1, localVector2, localVector3);
      localVector3.clear();
      arrayOfDouble[0] = d3;
    }
    finalizeSubCurves(localVector1, localVector2);
    Vector localVector4 = new Vector();
    Enumeration localEnumeration = localVector1.elements();
    while (localEnumeration.hasMoreElements())
    {
      Object localObject2 = (CurveLink)localEnumeration.nextElement();
      localVector4.add(((CurveLink)localObject2).getMoveto());
      Object localObject3 = localObject2;
      while (true)
      {
        do
          if ((localObject3 = ((CurveLink)localObject3).getNext()) == null)
            break label1058;
        while (((CurveLink)localObject2).absorb((CurveLink)localObject3));
        localVector4.add(((CurveLink)localObject2).getSubCurve());
        localObject2 = localObject3;
      }
      label1058: localVector4.add(((CurveLink)localObject2).getSubCurve());
    }
    return ((Vector)(Vector)(Vector)localVector4);
  }

  public static void finalizeSubCurves(Vector paramVector1, Vector paramVector2)
  {
    int i = paramVector2.size();
    if (i == 0)
      return;
    if ((i & 0x1) != 0)
      throw new InternalError("Odd number of chains!");
    ChainEnd[] arrayOfChainEnd = new ChainEnd[i];
    paramVector2.toArray(arrayOfChainEnd);
    for (int j = 1; j < i; j += 2)
    {
      ChainEnd localChainEnd1 = arrayOfChainEnd[(j - 1)];
      ChainEnd localChainEnd2 = arrayOfChainEnd[j];
      CurveLink localCurveLink = localChainEnd1.linkTo(localChainEnd2);
      if (localCurveLink != null)
        paramVector1.add(localCurveLink);
    }
    paramVector2.clear();
  }

  public static void resolveLinks(Vector paramVector1, Vector paramVector2, Vector paramVector3)
  {
    CurveLink[] arrayOfCurveLink;
    ChainEnd[] arrayOfChainEnd;
    int i = paramVector3.size();
    if (i == 0)
    {
      arrayOfCurveLink = EmptyLinkList;
    }
    else
    {
      if ((i & 0x1) != 0)
        throw new InternalError("Odd number of new curves!");
      arrayOfCurveLink = new CurveLink[i + 2];
      paramVector3.toArray(arrayOfCurveLink);
    }
    int j = paramVector2.size();
    if (j == 0)
    {
      arrayOfChainEnd = EmptyChainList;
    }
    else
    {
      if ((j & 0x1) != 0)
        throw new InternalError("Odd number of chains!");
      arrayOfChainEnd = new ChainEnd[j + 2];
      paramVector2.toArray(arrayOfChainEnd);
    }
    int k = 0;
    int l = 0;
    paramVector2.clear();
    Object localObject1 = arrayOfChainEnd[0];
    ChainEnd localChainEnd1 = arrayOfChainEnd[1];
    Object localObject2 = arrayOfCurveLink[0];
    CurveLink localCurveLink = arrayOfCurveLink[1];
    while ((localObject1 != null) || (localObject2 != null))
    {
      Object localObject3;
      int i1 = (localObject2 == null) ? 1 : 0;
      int i2 = (localObject1 == null) ? 1 : 0;
      if ((i1 == 0) && (i2 == 0))
      {
        i1 = (((k & 0x1) == 0) && (((ChainEnd)localObject1).getX() == localChainEnd1.getX())) ? 1 : 0;
        i2 = (((l & 0x1) == 0) && (((CurveLink)localObject2).getX() == localCurveLink.getX())) ? 1 : 0;
        if ((i1 == 0) && (i2 == 0))
        {
          double d1 = ((ChainEnd)localObject1).getX();
          double d2 = ((CurveLink)localObject2).getX();
          i1 = ((localChainEnd1 != null) && (d1 < d2) && (obstructs(localChainEnd1.getX(), d2, k))) ? 1 : 0;
          i2 = ((localCurveLink != null) && (d2 < d1) && (obstructs(localCurveLink.getX(), d1, l))) ? 1 : 0;
        }
      }
      if (i1 != 0)
      {
        localObject3 = ((ChainEnd)localObject1).linkTo(localChainEnd1);
        if (localObject3 != null)
          paramVector1.add(localObject3);
        localObject1 = arrayOfChainEnd[(k += 2)];
        localChainEnd1 = arrayOfChainEnd[(k + 1)];
      }
      if (i2 != 0)
      {
        localObject3 = new ChainEnd((CurveLink)localObject2, null);
        ChainEnd localChainEnd2 = new ChainEnd(localCurveLink, (ChainEnd)localObject3);
        ((ChainEnd)localObject3).setOtherEnd(localChainEnd2);
        paramVector2.add(localObject3);
        paramVector2.add(localChainEnd2);
        localObject2 = arrayOfCurveLink[(l += 2)];
        localCurveLink = arrayOfCurveLink[(l + 1)];
      }
      if ((i1 == 0) && (i2 == 0))
      {
        ((ChainEnd)localObject1).addLink((CurveLink)localObject2);
        paramVector2.add(localObject1);
        ++k;
        localObject1 = localChainEnd1;
        localChainEnd1 = arrayOfChainEnd[(k + 1)];
        ++l;
        localObject2 = localCurveLink;
        localCurveLink = arrayOfCurveLink[(l + 1)];
      }
    }
    if ((paramVector2.size() & 0x1) != 0)
      System.out.println("Odd number of chains!");
  }

  public static boolean obstructs(double paramDouble1, double paramDouble2, int paramInt)
  {
    return (paramDouble1 <= paramDouble2);
  }

  public static class AddOp extends AreaOp.CAGOp
  {
    public boolean newClassification(boolean paramBoolean1, boolean paramBoolean2)
    {
      return ((paramBoolean1) || (paramBoolean2));
    }
  }

  public static abstract class CAGOp extends AreaOp
  {
    boolean inLeft;
    boolean inRight;
    boolean inResult;

    public CAGOp()
    {
      super(null);
    }

    public void newRow()
    {
      this.inLeft = false;
      this.inRight = false;
      this.inResult = false;
    }

    public int classify(Edge paramEdge)
    {
      if (paramEdge.getCurveTag() == 0)
        this.inLeft = (!(this.inLeft));
      else
        this.inRight = (!(this.inRight));
      boolean bool = newClassification(this.inLeft, this.inRight);
      if (this.inResult == bool)
        return 0;
      this.inResult = bool;
      return ((bool) ? 1 : -1);
    }

    public int getState()
    {
      return ((this.inResult) ? 1 : -1);
    }

    public abstract boolean newClassification(boolean paramBoolean1, boolean paramBoolean2);
  }

  public static class EOWindOp extends AreaOp
  {
    private boolean inside;

    public EOWindOp()
    {
      super(null);
    }

    public void newRow()
    {
      this.inside = false;
    }

    public int classify(Edge paramEdge)
    {
      int i = (!(this.inside)) ? 1 : 0;
      this.inside = i;
      return ((i != 0) ? 1 : -1);
    }

    public int getState()
    {
      return ((this.inside) ? 1 : -1);
    }
  }

  public static class IntOp extends AreaOp.CAGOp
  {
    public boolean newClassification(boolean paramBoolean1, boolean paramBoolean2)
    {
      return ((paramBoolean1) && (paramBoolean2));
    }
  }

  public static class NZWindOp extends AreaOp
  {
    private int count;

    public NZWindOp()
    {
      super(null);
    }

    public void newRow()
    {
      this.count = 0;
    }

    public int classify(Edge paramEdge)
    {
      int i = this.count;
      int j = (i == 0) ? 1 : 0;
      i += paramEdge.getCurve().getDirection();
      this.count = i;
      return ((i == 0) ? -1 : j);
    }

    public int getState()
    {
      return ((this.count == 0) ? -1 : 1);
    }
  }

  public static class SubOp extends AreaOp.CAGOp
  {
    public boolean newClassification(boolean paramBoolean1, boolean paramBoolean2)
    {
      return ((paramBoolean1) && (!(paramBoolean2)));
    }
  }

  public static class XorOp extends AreaOp.CAGOp
  {
    public boolean newClassification(boolean paramBoolean1, boolean paramBoolean2)
    {
      return (paramBoolean1 != paramBoolean2);
    }
  }
}
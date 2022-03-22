package sun.java2d.loops;

import java.awt.AlphaComposite;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;
import sun.security.action.GetPropertyAction;

public abstract class GraphicsPrimitive
{
  private String methodSignature;
  private int uniqueID;
  private static int unusedPrimID = 1;
  private SurfaceType sourceType;
  private CompositeType compositeType;
  private SurfaceType destType;
  private long pNativePrim;
  static HashMap traceMap;
  public static int traceflags;
  public static String tracefile;
  public static PrintStream traceout;
  public static final int TRACELOG = 1;
  public static final int TRACETIMESTAMP = 2;
  public static final int TRACECOUNTS = 4;
  private String cachedname;

  public static final synchronized int makePrimTypeID()
  {
    if (unusedPrimID > 255)
      throw new InternalError("primitive id overflow");
    return (unusedPrimID++);
  }

  public static final synchronized int makeUniqueID(int paramInt, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return (paramInt << 24 | paramSurfaceType2.getUniqueID() << 16 | paramCompositeType.getUniqueID() << 8 | paramSurfaceType1.getUniqueID());
  }

  protected GraphicsPrimitive(String paramString, int paramInt, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    this.methodSignature = paramString;
    this.sourceType = paramSurfaceType1;
    this.compositeType = paramCompositeType;
    this.destType = paramSurfaceType2;
    if ((paramSurfaceType1 == null) || (paramCompositeType == null) || (paramSurfaceType2 == null))
      this.uniqueID = (paramInt << 24);
    else
      this.uniqueID = makeUniqueID(paramInt, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  protected GraphicsPrimitive(long paramLong, String paramString, int paramInt, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    this.pNativePrim = paramLong;
    this.methodSignature = paramString;
    this.sourceType = paramSurfaceType1;
    this.compositeType = paramCompositeType;
    this.destType = paramSurfaceType2;
    if ((paramSurfaceType1 == null) || (paramCompositeType == null) || (paramSurfaceType2 == null))
      this.uniqueID = (paramInt << 24);
    else
      this.uniqueID = makeUniqueID(paramInt, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public final int getUniqueID()
  {
    return this.uniqueID;
  }

  public final String getSignature()
  {
    return this.methodSignature;
  }

  public final int getPrimTypeID()
  {
    return (this.uniqueID >>> 24);
  }

  public final long getNativePrim()
  {
    return this.pNativePrim;
  }

  public final SurfaceType getSourceType()
  {
    return this.sourceType;
  }

  public final CompositeType getCompositeType()
  {
    return this.compositeType;
  }

  public final SurfaceType getDestType()
  {
    return this.destType;
  }

  public final boolean satisfies(String paramString, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    if (paramString != this.methodSignature)
      return false;
    while (true)
    {
      if (paramSurfaceType1 == null)
        return false;
      if (paramSurfaceType1.equals(this.sourceType))
        break;
      paramSurfaceType1 = paramSurfaceType1.getSuperType();
    }
    while (true)
    {
      if (paramCompositeType == null)
        return false;
      if (paramCompositeType.equals(this.compositeType))
        break;
      paramCompositeType = paramCompositeType.getSuperType();
    }
    while (paramSurfaceType2 != null)
    {
      if (paramSurfaceType2.equals(this.destType))
        break;
      paramSurfaceType2 = paramSurfaceType2.getSuperType();
    }
    return true;
  }

  final boolean satisfiesSameAs(GraphicsPrimitive paramGraphicsPrimitive)
  {
    return ((this.methodSignature == paramGraphicsPrimitive.methodSignature) && (this.sourceType.equals(paramGraphicsPrimitive.sourceType)) && (this.compositeType.equals(paramGraphicsPrimitive.compositeType)) && (this.destType.equals(paramGraphicsPrimitive.destType)));
  }

  public abstract GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2);

  public abstract GraphicsPrimitive traceWrap();

  public static boolean tracingEnabled()
  {
    return (traceflags != 0);
  }

  private static PrintStream getTraceOutputFile()
  {
    if (traceout == null)
      if (tracefile != null)
      {
        Object localObject = AccessController.doPrivileged(new PrivilegedAction()
        {
          public Object run()
          {
            try
            {
              return new FileOutputStream(GraphicsPrimitive.tracefile);
            }
            catch (FileNotFoundException localFileNotFoundException)
            {
            }
            return null;
          }
        });
        if (localObject != null)
          traceout = new PrintStream((OutputStream)localObject);
        else
          traceout = System.err;
      }
      else
      {
        traceout = System.err;
      }
    return traceout;
  }

  public static synchronized void tracePrimitive(Object paramObject)
  {
    Object localObject;
    if ((traceflags & 0x4) != 0)
    {
      if (traceMap == null)
      {
        traceMap = new HashMap();
        TraceReporter.setShutdownHook();
      }
      localObject = traceMap.get(paramObject);
      if (localObject == null)
      {
        localObject = new int[1];
        traceMap.put(paramObject, localObject);
      }
      ((int[])(int[])localObject)[0] += 1;
    }
    if ((traceflags & 0x1) != 0)
    {
      localObject = getTraceOutputFile();
      if ((traceflags & 0x2) != 0)
        ((PrintStream)localObject).print(System.currentTimeMillis() + ": ");
      ((PrintStream)localObject).println(paramObject);
    }
  }

  protected void setupGeneralBinaryOp(GeneralBinaryOp paramGeneralBinaryOp)
  {
    Blit localBlit2;
    Blit localBlit3;
    int i = paramGeneralBinaryOp.getPrimTypeID();
    String str = paramGeneralBinaryOp.getSignature();
    SurfaceType localSurfaceType1 = paramGeneralBinaryOp.getSourceType();
    CompositeType localCompositeType = paramGeneralBinaryOp.getCompositeType();
    SurfaceType localSurfaceType2 = paramGeneralBinaryOp.getDestType();
    Blit localBlit1 = createConverter(localSurfaceType1, SurfaceType.IntArgb);
    GraphicsPrimitive localGraphicsPrimitive = GraphicsPrimitiveMgr.locatePrim(i, SurfaceType.IntArgb, localCompositeType, localSurfaceType2);
    if (localGraphicsPrimitive != null)
    {
      localBlit2 = null;
      localBlit3 = null;
    }
    else
    {
      localGraphicsPrimitive = getGeneralOp(i, localCompositeType);
      if (localGraphicsPrimitive == null)
        throw new InternalError("Cannot construct general op for " + str + " " + localCompositeType);
      localBlit2 = createConverter(localSurfaceType2, SurfaceType.IntArgb);
      localBlit3 = createConverter(SurfaceType.IntArgb, localSurfaceType2);
    }
    paramGeneralBinaryOp.setPrimitives(localBlit1, localBlit2, localGraphicsPrimitive, localBlit3);
  }

  protected void setupGeneralUnaryOp(GeneralUnaryOp paramGeneralUnaryOp)
  {
    int i = paramGeneralUnaryOp.getPrimTypeID();
    String str = paramGeneralUnaryOp.getSignature();
    CompositeType localCompositeType = paramGeneralUnaryOp.getCompositeType();
    SurfaceType localSurfaceType = paramGeneralUnaryOp.getDestType();
    Blit localBlit1 = createConverter(localSurfaceType, SurfaceType.IntArgb);
    GraphicsPrimitive localGraphicsPrimitive = getGeneralOp(i, localCompositeType);
    Blit localBlit2 = createConverter(SurfaceType.IntArgb, localSurfaceType);
    if ((localBlit1 == null) || (localGraphicsPrimitive == null) || (localBlit2 == null))
      throw new InternalError("Cannot construct binary op for " + localCompositeType + " " + localSurfaceType);
    paramGeneralUnaryOp.setPrimitives(localBlit1, localGraphicsPrimitive, localBlit2);
  }

  protected static Blit createConverter(SurfaceType paramSurfaceType1, SurfaceType paramSurfaceType2)
  {
    if (paramSurfaceType1.equals(paramSurfaceType2))
      return null;
    Blit localBlit = Blit.getFromCache(paramSurfaceType1, CompositeType.SrcNoEa, paramSurfaceType2);
    if (localBlit == null)
      throw new InternalError("Cannot construct converter for " + paramSurfaceType1 + "=>" + paramSurfaceType2);
    return localBlit;
  }

  protected static SurfaceData convertFrom(Blit paramBlit, SurfaceData paramSurfaceData1, int paramInt1, int paramInt2, int paramInt3, int paramInt4, SurfaceData paramSurfaceData2)
  {
    return convertFrom(paramBlit, paramSurfaceData1, paramInt1, paramInt2, paramInt3, paramInt4, paramSurfaceData2, 2);
  }

  protected static SurfaceData convertFrom(Blit paramBlit, SurfaceData paramSurfaceData1, int paramInt1, int paramInt2, int paramInt3, int paramInt4, SurfaceData paramSurfaceData2, int paramInt5)
  {
    Object localObject;
    if (paramSurfaceData2 != null)
    {
      localObject = paramSurfaceData2.getBounds();
      if ((paramInt3 > ((Rectangle)localObject).width) || (paramInt4 > ((Rectangle)localObject).height))
        paramSurfaceData2 = null;
    }
    if (paramSurfaceData2 == null)
    {
      localObject = new BufferedImage(paramInt3, paramInt4, paramInt5);
      paramSurfaceData2 = BufImgSurfaceData.createData((BufferedImage)localObject);
    }
    paramBlit.Blit(paramSurfaceData1, paramSurfaceData2, AlphaComposite.Src, null, paramInt1, paramInt2, 0, 0, paramInt3, paramInt4);
    return ((SurfaceData)paramSurfaceData2);
  }

  protected static void convertTo(Blit paramBlit, SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (paramBlit != null)
      paramBlit.Blit(paramSurfaceData1, paramSurfaceData2, AlphaComposite.Src, paramRegion, 0, 0, paramInt1, paramInt2, paramInt3, paramInt4);
  }

  protected static GraphicsPrimitive getGeneralOp(int paramInt, CompositeType paramCompositeType)
  {
    return GraphicsPrimitiveMgr.locatePrim(paramInt, SurfaceType.IntArgb, paramCompositeType, SurfaceType.IntArgb);
  }

  public static String simplename(Field[] paramArrayOfField, Object paramObject)
  {
    for (int i = 0; i < paramArrayOfField.length; ++i)
    {
      Field localField = paramArrayOfField[i];
      try
      {
        if (paramObject == localField.get(null))
          return localField.getName();
      }
      catch (Exception localException)
      {
      }
    }
    return "\"" + paramObject.toString() + "\"";
  }

  public static String simplename(SurfaceType paramSurfaceType)
  {
    return simplename(SurfaceType.class.getDeclaredFields(), paramSurfaceType);
  }

  public static String simplename(CompositeType paramCompositeType)
  {
    return simplename(CompositeType.class.getDeclaredFields(), paramCompositeType);
  }

  public String toString()
  {
    if (this.cachedname == null)
    {
      String str = this.methodSignature;
      int i = str.indexOf(40);
      if (i >= 0)
        str = str.substring(0, i);
      this.cachedname = super.getClass().getName() + "::" + str + "(" + simplename(this.sourceType) + ", " + simplename(this.compositeType) + ", " + simplename(this.destType) + ")";
    }
    return this.cachedname;
  }

  static
  {
    GetPropertyAction localGetPropertyAction = new GetPropertyAction("sun.java2d.trace");
    String str1 = (String)AccessController.doPrivileged(localGetPropertyAction);
    if (str1 != null)
    {
      int i = 0;
      int j = 0;
      StringTokenizer localStringTokenizer = new StringTokenizer(str1, ",");
      while (localStringTokenizer.hasMoreTokens())
      {
        String str2 = localStringTokenizer.nextToken();
        if (str2.equalsIgnoreCase("count"))
        {
          j |= 4;
        }
        else if (str2.equalsIgnoreCase("log"))
        {
          j |= 1;
        }
        else if (str2.equalsIgnoreCase("timestamp"))
        {
          j |= 2;
        }
        else if (str2.equalsIgnoreCase("verbose"))
        {
          i = 1;
        }
        else if (str2.regionMatches(true, 0, "out:", 0, 4))
        {
          tracefile = str2.substring(4);
        }
        else
        {
          if (!(str2.equalsIgnoreCase("help")))
            System.err.println("unrecognized token: " + str2);
          System.err.println("usage: -Dsun.java2d.trace=[log[,timestamp]],[count],[out:<filename>],[help],[verbose]");
        }
      }
      if (i != 0)
      {
        System.err.print("GraphicsPrimitive logging ");
        if ((j & 0x1) != 0)
        {
          System.err.println("enabled");
          System.err.print("GraphicsPrimitive timetamps ");
          if ((j & 0x2) != 0)
            System.err.println("enabled");
          else
            System.err.println("disabled");
        }
        else
        {
          System.err.println("[and timestamps] disabled");
        }
        System.err.print("GraphicsPrimitive invocation counts ");
        if ((j & 0x4) != 0)
          System.err.println("enabled");
        else
          System.err.println("disabled");
        System.err.print("GraphicsPrimitive trace output to ");
        if (tracefile == null)
          System.err.println("System.err");
        else
          System.err.println("file '" + tracefile + "'");
      }
      traceflags = j;
    }
  }

  protected static abstract interface GeneralBinaryOp
  {
    public abstract void setPrimitives(Blit paramBlit1, Blit paramBlit2, GraphicsPrimitive paramGraphicsPrimitive, Blit paramBlit3);

    public abstract SurfaceType getSourceType();

    public abstract CompositeType getCompositeType();

    public abstract SurfaceType getDestType();

    public abstract String getSignature();

    public abstract int getPrimTypeID();
  }

  protected static abstract interface GeneralUnaryOp
  {
    public abstract void setPrimitives(Blit paramBlit1, GraphicsPrimitive paramGraphicsPrimitive, Blit paramBlit2);

    public abstract CompositeType getCompositeType();

    public abstract SurfaceType getDestType();

    public abstract String getSignature();

    public abstract int getPrimTypeID();
  }

  public static class TraceReporter extends Thread
  {
    public static void setShutdownHook()
    {
      AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          Runtime.getRuntime().addShutdownHook(new GraphicsPrimitive.TraceReporter());
          return null;
        }
      });
    }

    public void run()
    {
      PrintStream localPrintStream = GraphicsPrimitive.access$000();
      Iterator localIterator = GraphicsPrimitive.traceMap.entrySet().iterator();
      long l = 3412047652030775296L;
      int i = 0;
      while (localIterator.hasNext())
      {
        Map.Entry localEntry = (Map.Entry)localIterator.next();
        Object localObject = localEntry.getKey();
        int[] arrayOfInt = (int[])(int[])localEntry.getValue();
        if (arrayOfInt[0] == 1)
          localPrintStream.print("1 call to ");
        else
          localPrintStream.print(arrayOfInt[0] + " calls to ");
        localPrintStream.println(localObject);
        ++i;
        l += arrayOfInt[0];
      }
      if (i == 0)
        localPrintStream.println("No graphics primitives executed");
      else if (i > 1)
        localPrintStream.println(l + " total calls to " + i + " different primitives");
    }
  }
}
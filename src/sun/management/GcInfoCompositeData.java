package sun.management;

import com.sun.management.GcInfo;
import java.io.InvalidObjectException;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Method;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;

public class GcInfoCompositeData extends LazyCompositeData
{
  private final GcInfo info;
  private final GcInfoBuilder builder;
  private final Object[] gcExtItemValues;
  private static final String ID = "id";
  private static final String START_TIME = "startTime";
  private static final String END_TIME = "endTime";
  private static final String DURATION = "duration";
  private static final String MEMORY_USAGE_BEFORE_GC = "memoryUsageBeforeGc";
  private static final String MEMORY_USAGE_AFTER_GC = "memoryUsageAfterGc";
  private static final String[] baseGcInfoItemNames = { "id", "startTime", "endTime", "duration", "memoryUsageBeforeGc", "memoryUsageAfterGc" };
  private static MappedMXBeanType memoryUsageMapType;
  private static OpenType[] baseGcInfoItemTypes;
  private static CompositeType baseGcInfoCompositeType;

  public GcInfoCompositeData(GcInfo paramGcInfo, GcInfoBuilder paramGcInfoBuilder, Object[] paramArrayOfObject)
  {
    this.info = paramGcInfo;
    this.builder = paramGcInfoBuilder;
    this.gcExtItemValues = paramArrayOfObject;
  }

  public GcInfo getGcInfo()
  {
    return this.info;
  }

  protected CompositeData getCompositeData()
  {
    Object[] arrayOfObject1;
    try
    {
      arrayOfObject1 = { new Long(this.info.getId()), new Long(this.info.getStartTime()), new Long(this.info.getEndTime()), new Long(this.info.getDuration()), memoryUsageMapType.toOpenTypeData(this.info.getMemoryUsageBeforeGc()), memoryUsageMapType.toOpenTypeData(this.info.getMemoryUsageAfterGc()) };
    }
    catch (OpenDataException localOpenDataException1)
    {
      throw Util.newAssertionError(localOpenDataException1);
    }
    int i = this.builder.getGcExtItemCount();
    if ((i == 0) && (this.gcExtItemValues != null) && (this.gcExtItemValues.length != 0))
      throw new InternalError("Unexpected Gc Extension Item Values");
    if ((i > 0) && (((this.gcExtItemValues == null) || (i != this.gcExtItemValues.length))))
      throw new InternalError("Unmatched Gc Extension Item Values");
    Object[] arrayOfObject2 = new Object[arrayOfObject1.length + i];
    System.arraycopy(arrayOfObject1, 0, arrayOfObject2, 0, arrayOfObject1.length);
    if (i > 0)
      System.arraycopy(this.gcExtItemValues, 0, arrayOfObject2, arrayOfObject1.length, i);
    try
    {
      return new CompositeDataSupport(this.builder.getGcInfoCompositeType(), this.builder.getItemNames(), arrayOfObject2);
    }
    catch (OpenDataException localOpenDataException2)
    {
      throw Util.newInternalError(localOpenDataException2);
    }
  }

  static String[] getBaseGcInfoItemNames()
  {
    return baseGcInfoItemNames;
  }

  static synchronized OpenType[] getBaseGcInfoItemTypes()
  {
    if (baseGcInfoItemTypes == null)
    {
      OpenType localOpenType = memoryUsageMapType.getOpenType();
      baseGcInfoItemTypes = { SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG, localOpenType, localOpenType };
    }
    return baseGcInfoItemTypes;
  }

  public static long getId(CompositeData paramCompositeData)
  {
    return getLong(paramCompositeData, "id");
  }

  public static long getStartTime(CompositeData paramCompositeData)
  {
    return getLong(paramCompositeData, "startTime");
  }

  public static long getEndTime(CompositeData paramCompositeData)
  {
    return getLong(paramCompositeData, "endTime");
  }

  public static Map<String, MemoryUsage> getMemoryUsageBeforeGc(CompositeData paramCompositeData)
  {
    TabularData localTabularData;
    try
    {
      localTabularData = (TabularData)paramCompositeData.get("memoryUsageBeforeGc");
      return ((Map)memoryUsageMapType.toJavaTypeData(localTabularData));
    }
    catch (InvalidObjectException localInvalidObjectException)
    {
      throw Util.newAssertionError(localInvalidObjectException);
    }
    catch (OpenDataException localOpenDataException)
    {
      throw Util.newAssertionError(localOpenDataException);
    }
  }

  public static Map<String, MemoryUsage> getMemoryUsageAfterGc(CompositeData paramCompositeData)
  {
    TabularData localTabularData;
    try
    {
      localTabularData = (TabularData)paramCompositeData.get("memoryUsageAfterGc");
      return ((Map)memoryUsageMapType.toJavaTypeData(localTabularData));
    }
    catch (InvalidObjectException localInvalidObjectException)
    {
      throw Util.newAssertionError(localInvalidObjectException);
    }
    catch (OpenDataException localOpenDataException)
    {
      throw Util.newAssertionError(localOpenDataException);
    }
  }

  public static void validateCompositeData(CompositeData paramCompositeData)
  {
    if (paramCompositeData == null)
      throw new NullPointerException("Null CompositeData");
    if (!(isTypeMatched(getBaseGcInfoCompositeType(), paramCompositeData.getCompositeType())))
      throw new IllegalArgumentException("Unexpected composite type for GcInfo");
  }

  private static synchronized CompositeType getBaseGcInfoCompositeType()
  {
    if (baseGcInfoCompositeType == null)
      try
      {
        baseGcInfoCompositeType = new CompositeType("sun.management.BaseGcInfoCompositeType", "CompositeType for Base GcInfo", getBaseGcInfoItemNames(), getBaseGcInfoItemNames(), getBaseGcInfoItemTypes());
      }
      catch (OpenDataException localOpenDataException)
      {
        throw Util.newException(localOpenDataException);
      }
    return baseGcInfoCompositeType;
  }

  static
  {
    try
    {
      Method localMethod = GcInfo.class.getMethod("getMemoryUsageBeforeGc", new Class[0]);
      memoryUsageMapType = MappedMXBeanType.getMappedType(localMethod.getGenericReturnType());
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw Util.newAssertionError(localNoSuchMethodException);
    }
    catch (OpenDataException localOpenDataException)
    {
      throw Util.newAssertionError(localOpenDataException);
    }
    baseGcInfoItemTypes = null;
    baseGcInfoCompositeType = null;
  }
}
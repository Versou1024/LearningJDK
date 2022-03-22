package sun.management;

import java.lang.management.MonitorInfo;
import java.util.Set;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;

public class MonitorInfoCompositeData extends LazyCompositeData
{
  private final MonitorInfo lock;
  private static final CompositeType monitorInfoCompositeType;
  private static final String[] monitorInfoItemNames;
  private static final String CLASS_NAME = "className";
  private static final String IDENTITY_HASH_CODE = "identityHashCode";
  private static final String LOCKED_STACK_FRAME = "lockedStackFrame";
  private static final String LOCKED_STACK_DEPTH = "lockedStackDepth";

  private MonitorInfoCompositeData(MonitorInfo paramMonitorInfo)
  {
    this.lock = paramMonitorInfo;
  }

  public MonitorInfo getMonitorInfo()
  {
    return this.lock;
  }

  public static CompositeData toCompositeData(MonitorInfo paramMonitorInfo)
  {
    MonitorInfoCompositeData localMonitorInfoCompositeData = new MonitorInfoCompositeData(paramMonitorInfo);
    return localMonitorInfoCompositeData.getCompositeData();
  }

  protected CompositeData getCompositeData()
  {
    int i = monitorInfoItemNames.length;
    Object[] arrayOfObject = new Object[i];
    CompositeData localCompositeData = LockDataConverter.toLockInfoCompositeData(this.lock);
    for (int j = 0; j < i; ++j)
    {
      String str = monitorInfoItemNames[j];
      if (str.equals("lockedStackFrame"))
      {
        StackTraceElement localStackTraceElement = this.lock.getLockedStackFrame();
        arrayOfObject[j] = ((localStackTraceElement != null) ? StackTraceElementCompositeData.toCompositeData(localStackTraceElement) : null);
      }
      else if (str.equals("lockedStackDepth"))
      {
        arrayOfObject[j] = new Integer(this.lock.getLockedStackDepth());
      }
      else
      {
        arrayOfObject[j] = localCompositeData.get(str);
      }
    }
    try
    {
      return new CompositeDataSupport(monitorInfoCompositeType, monitorInfoItemNames, arrayOfObject);
    }
    catch (OpenDataException localOpenDataException)
    {
      throw Util.newInternalError(localOpenDataException);
    }
  }

  static CompositeType getMonitorInfoCompositeType()
  {
    return monitorInfoCompositeType;
  }

  public static String getClassName(CompositeData paramCompositeData)
  {
    return getString(paramCompositeData, "className");
  }

  public static int getIdentityHashCode(CompositeData paramCompositeData)
  {
    return getInt(paramCompositeData, "identityHashCode");
  }

  public static StackTraceElement getLockedStackFrame(CompositeData paramCompositeData)
  {
    CompositeData localCompositeData = (CompositeData)paramCompositeData.get("lockedStackFrame");
    if (localCompositeData != null)
      return StackTraceElementCompositeData.from(localCompositeData);
    return null;
  }

  public static int getLockedStackDepth(CompositeData paramCompositeData)
  {
    return getInt(paramCompositeData, "lockedStackDepth");
  }

  public static void validateCompositeData(CompositeData paramCompositeData)
  {
    if (paramCompositeData == null)
      throw new NullPointerException("Null CompositeData");
    if (!(isTypeMatched(monitorInfoCompositeType, paramCompositeData.getCompositeType())))
      throw new IllegalArgumentException("Unexpected composite type for MonitorInfo");
  }

  static
  {
    try
    {
      monitorInfoCompositeType = (CompositeType)MappedMXBeanType.toOpenType(MonitorInfo.class);
      Set localSet = monitorInfoCompositeType.keySet();
      monitorInfoItemNames = (String[])(String[])localSet.toArray(new String[0]);
    }
    catch (OpenDataException localOpenDataException)
    {
      throw Util.newInternalError(localOpenDataException);
    }
  }
}
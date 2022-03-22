package sun.management;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import sun.management.counter.Counter;
import sun.management.counter.Units;
import sun.management.counter.perf.PerfInstrumentation;
import sun.misc.Perf;

public class ConnectorAddressLink
{
  private static final String CONNECTOR_ADDRESS_COUNTER = "sun.management.JMXConnectorServer.address";
  private static final String REMOTE_CONNECTOR_COUNTER_PREFIX = "sun.management.JMXConnectorServer.";
  private static AtomicInteger counter = new AtomicInteger();

  public static void export(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0))
      throw new IllegalArgumentException("address not specified");
    Perf localPerf = Perf.getPerf();
    localPerf.createString("sun.management.JMXConnectorServer.address", 1, Units.STRING.intValue(), paramString);
  }

  public static String importFrom(int paramInt)
    throws IOException
  {
    ByteBuffer localByteBuffer;
    Perf localPerf = Perf.getPerf();
    try
    {
      localByteBuffer = localPerf.attach(paramInt, "r");
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw new IOException(localIllegalArgumentException.getMessage());
    }
    List localList = new PerfInstrumentation(localByteBuffer).findByPattern("sun.management.JMXConnectorServer.address");
    Iterator localIterator = localList.iterator();
    if (localIterator.hasNext())
    {
      Counter localCounter = (Counter)localIterator.next();
      return ((String)localCounter.getValue());
    }
    return null;
  }

  public static void exportRemote(Map<String, String> paramMap)
  {
    int i = counter.getAndIncrement();
    Perf localPerf = Perf.getPerf();
    Iterator localIterator = paramMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      localPerf.createString("sun.management.JMXConnectorServer." + i + "." + ((String)localEntry.getKey()), 1, Units.STRING.intValue(), (String)localEntry.getValue());
    }
  }

  public static Map<String, String> importRemoteFrom(int paramInt)
    throws IOException
  {
    ByteBuffer localByteBuffer;
    Perf localPerf = Perf.getPerf();
    try
    {
      localByteBuffer = localPerf.attach(paramInt, "r");
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw new IOException(localIllegalArgumentException.getMessage());
    }
    List localList = new PerfInstrumentation(localByteBuffer).getAllCounters();
    HashMap localHashMap = new HashMap();
    Iterator localIterator = localList.iterator();
    while (localIterator.hasNext())
    {
      Object localObject = localIterator.next();
      String str = ((Counter)localObject).getName();
      if ((str.startsWith("sun.management.JMXConnectorServer.")) && (!(str.equals("sun.management.JMXConnectorServer.address"))))
        localHashMap.put(str, ((Counter)localObject).getValue().toString());
    }
    return localHashMap;
  }
}
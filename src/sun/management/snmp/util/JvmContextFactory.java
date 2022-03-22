package sun.management.snmp.util;

import com.sun.jmx.snmp.SnmpPdu;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.ThreadContext;
import com.sun.jmx.snmp.agent.SnmpUserDataFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JvmContextFactory
  implements SnmpUserDataFactory
{
  public Object allocateUserData(SnmpPdu paramSnmpPdu)
    throws SnmpStatusException
  {
    return Collections.synchronizedMap(new HashMap());
  }

  public void releaseUserData(Object paramObject, SnmpPdu paramSnmpPdu)
    throws SnmpStatusException
  {
    ((Map)paramObject).clear();
  }

  public static Map getUserData()
  {
    Object localObject = ThreadContext.get("SnmpUserData");
    if (localObject instanceof Map)
      return ((Map)localObject);
    return null;
  }
}
package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpStatusException;
import java.io.Serializable;
import sun.management.snmp.jvmmib.JvmRTClassPathEntryMBean;

public class JvmRTClassPathEntryImpl
  implements JvmRTClassPathEntryMBean, Serializable
{
  private final String item = validPathElementTC(paramString);
  private final int index;

  public JvmRTClassPathEntryImpl(String paramString, int paramInt)
  {
    this.index = paramInt;
  }

  private String validPathElementTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(paramString);
  }

  public String getJvmRTClassPathItem()
    throws SnmpStatusException
  {
    return this.item;
  }

  public Integer getJvmRTClassPathIndex()
    throws SnmpStatusException
  {
    return new Integer(this.index);
  }
}
package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpStatusException;
import java.io.Serializable;
import sun.management.snmp.jvmmib.JvmRTBootClassPathEntryMBean;

public class JvmRTBootClassPathEntryImpl
  implements JvmRTBootClassPathEntryMBean, Serializable
{
  private final String item = validPathElementTC(paramString);
  private final int index;

  public JvmRTBootClassPathEntryImpl(String paramString, int paramInt)
  {
    this.index = paramInt;
  }

  private String validPathElementTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(paramString);
  }

  public String getJvmRTBootClassPathItem()
    throws SnmpStatusException
  {
    return this.item;
  }

  public Integer getJvmRTBootClassPathIndex()
    throws SnmpStatusException
  {
    return new Integer(this.index);
  }
}
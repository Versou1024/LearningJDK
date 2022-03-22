package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpStatusException;
import java.io.Serializable;
import sun.management.snmp.jvmmib.JvmRTLibraryPathEntryMBean;

public class JvmRTLibraryPathEntryImpl
  implements JvmRTLibraryPathEntryMBean, Serializable
{
  private final String item = validPathElementTC(paramString);
  private final int index;

  public JvmRTLibraryPathEntryImpl(String paramString, int paramInt)
  {
    this.index = paramInt;
  }

  private String validPathElementTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(paramString);
  }

  public String getJvmRTLibraryPathItem()
    throws SnmpStatusException
  {
    return this.item;
  }

  public Integer getJvmRTLibraryPathIndex()
    throws SnmpStatusException
  {
    return new Integer(this.index);
  }
}
package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpStatusException;
import java.io.Serializable;
import sun.management.snmp.jvmmib.JvmRTInputArgsEntryMBean;

public class JvmRTInputArgsEntryImpl
  implements JvmRTInputArgsEntryMBean, Serializable
{
  private final String item = validArgValueTC(paramString);
  private final int index;

  public JvmRTInputArgsEntryImpl(String paramString, int paramInt)
  {
    this.index = paramInt;
  }

  private String validArgValueTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validArgValueTC(paramString);
  }

  public String getJvmRTInputArgsItem()
    throws SnmpStatusException
  {
    return this.item;
  }

  public Integer getJvmRTInputArgsIndex()
    throws SnmpStatusException
  {
    return new Integer(this.index);
  }
}
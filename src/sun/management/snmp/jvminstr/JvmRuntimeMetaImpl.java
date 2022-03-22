package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.agent.SnmpMib;
import com.sun.jmx.snmp.agent.SnmpStandardObjectServer;
import javax.management.MBeanServer;
import sun.management.snmp.jvmmib.JvmRTBootClassPathTableMeta;
import sun.management.snmp.jvmmib.JvmRTClassPathTableMeta;
import sun.management.snmp.jvmmib.JvmRTInputArgsTableMeta;
import sun.management.snmp.jvmmib.JvmRTLibraryPathTableMeta;
import sun.management.snmp.jvmmib.JvmRuntimeMeta;

public class JvmRuntimeMetaImpl extends JvmRuntimeMeta
{
  public JvmRuntimeMetaImpl(SnmpMib paramSnmpMib, SnmpStandardObjectServer paramSnmpStandardObjectServer)
  {
    super(paramSnmpMib, paramSnmpStandardObjectServer);
  }

  protected JvmRTInputArgsTableMeta createJvmRTInputArgsTableMetaNode(String paramString1, String paramString2, SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    return new JvmRTInputArgsTableMetaImpl(paramSnmpMib, this.objectserver);
  }

  protected JvmRTLibraryPathTableMeta createJvmRTLibraryPathTableMetaNode(String paramString1, String paramString2, SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    return new JvmRTLibraryPathTableMetaImpl(paramSnmpMib, this.objectserver);
  }

  protected JvmRTClassPathTableMeta createJvmRTClassPathTableMetaNode(String paramString1, String paramString2, SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    return new JvmRTClassPathTableMetaImpl(paramSnmpMib, this.objectserver);
  }

  protected JvmRTBootClassPathTableMeta createJvmRTBootClassPathTableMetaNode(String paramString1, String paramString2, SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    return new JvmRTBootClassPathTableMetaImpl(paramSnmpMib, this.objectserver);
  }
}
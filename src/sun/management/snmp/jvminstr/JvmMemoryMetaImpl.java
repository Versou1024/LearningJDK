package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.agent.SnmpMib;
import com.sun.jmx.snmp.agent.SnmpStandardObjectServer;
import javax.management.MBeanServer;
import sun.management.snmp.jvmmib.JvmMemGCTableMeta;
import sun.management.snmp.jvmmib.JvmMemManagerTableMeta;
import sun.management.snmp.jvmmib.JvmMemMgrPoolRelTableMeta;
import sun.management.snmp.jvmmib.JvmMemPoolTableMeta;
import sun.management.snmp.jvmmib.JvmMemoryMeta;

public class JvmMemoryMetaImpl extends JvmMemoryMeta
{
  public JvmMemoryMetaImpl(SnmpMib paramSnmpMib, SnmpStandardObjectServer paramSnmpStandardObjectServer)
  {
    super(paramSnmpMib, paramSnmpStandardObjectServer);
  }

  protected JvmMemManagerTableMeta createJvmMemManagerTableMetaNode(String paramString1, String paramString2, SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    return new JvmMemManagerTableMetaImpl(paramSnmpMib, this.objectserver);
  }

  protected JvmMemGCTableMeta createJvmMemGCTableMetaNode(String paramString1, String paramString2, SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    return new JvmMemGCTableMetaImpl(paramSnmpMib, this.objectserver);
  }

  protected JvmMemPoolTableMeta createJvmMemPoolTableMetaNode(String paramString1, String paramString2, SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    return new JvmMemPoolTableMetaImpl(paramSnmpMib, this.objectserver);
  }

  protected JvmMemMgrPoolRelTableMeta createJvmMemMgrPoolRelTableMetaNode(String paramString1, String paramString2, SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    return new JvmMemMgrPoolRelTableMetaImpl(paramSnmpMib, this.objectserver);
  }
}
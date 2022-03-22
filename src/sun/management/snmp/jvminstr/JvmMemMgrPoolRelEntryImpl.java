package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpStatusException;
import sun.management.snmp.jvmmib.JvmMemMgrPoolRelEntryMBean;

public class JvmMemMgrPoolRelEntryImpl
  implements JvmMemMgrPoolRelEntryMBean
{
  protected final int JvmMemManagerIndex;
  protected final int JvmMemPoolIndex;
  protected final String mmmName;
  protected final String mpmName;

  public JvmMemMgrPoolRelEntryImpl(String paramString1, String paramString2, int paramInt1, int paramInt2)
  {
    this.JvmMemManagerIndex = paramInt1;
    this.JvmMemPoolIndex = paramInt2;
    this.mmmName = paramString1;
    this.mpmName = paramString2;
  }

  public String getJvmMemMgrRelPoolName()
    throws SnmpStatusException
  {
    return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(this.mpmName);
  }

  public String getJvmMemMgrRelManagerName()
    throws SnmpStatusException
  {
    return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(this.mmmName);
  }

  public Integer getJvmMemManagerIndex()
    throws SnmpStatusException
  {
    return new Integer(this.JvmMemManagerIndex);
  }

  public Integer getJvmMemPoolIndex()
    throws SnmpStatusException
  {
    return new Integer(this.JvmMemPoolIndex);
  }
}
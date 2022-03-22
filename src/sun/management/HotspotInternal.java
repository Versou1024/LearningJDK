package sun.management;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class HotspotInternal
  implements HotspotInternalMBean, MBeanRegistration
{
  private MBeanServer server = null;

  public ObjectName preRegister(MBeanServer paramMBeanServer, ObjectName paramObjectName)
    throws Exception
  {
    ManagementFactory.registerInternalMBeans(paramMBeanServer);
    this.server = paramMBeanServer;
    return ManagementFactory.getHotspotInternalObjectName();
  }

  public void postRegister(Boolean paramBoolean)
  {
  }

  public void preDeregister()
    throws Exception
  {
    ManagementFactory.unregisterInternalMBeans(this.server);
  }

  public void postDeregister()
  {
  }
}
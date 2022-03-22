package sun.management;

import java.lang.management.OperatingSystemMXBean;
import sun.misc.Unsafe;

public class OperatingSystemImpl
  implements OperatingSystemMXBean
{
  private final VMManagement jvm;
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private double[] loadavg = new double[1];

  protected OperatingSystemImpl(VMManagement paramVMManagement)
  {
    this.jvm = paramVMManagement;
  }

  public String getName()
  {
    return this.jvm.getOsName();
  }

  public String getArch()
  {
    return this.jvm.getOsArch();
  }

  public String getVersion()
  {
    return this.jvm.getOsVersion();
  }

  public int getAvailableProcessors()
  {
    return this.jvm.getAvailableProcessors();
  }

  public double getSystemLoadAverage()
  {
    if (unsafe.getLoadAverage(this.loadavg, 1) == 1)
      return this.loadavg[0];
    return -1.0D;
  }
}
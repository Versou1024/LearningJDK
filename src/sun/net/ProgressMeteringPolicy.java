package sun.net;

import java.net.URL;

public abstract interface ProgressMeteringPolicy
{
  public abstract boolean shouldMeterInput(URL paramURL, String paramString);

  public abstract int getProgressUpdateThreshold();
}
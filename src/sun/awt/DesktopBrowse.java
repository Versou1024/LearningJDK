package sun.awt;

import java.net.URL;

public abstract class DesktopBrowse
{
  private static volatile DesktopBrowse mInstance;

  public static void setInstance(DesktopBrowse paramDesktopBrowse)
  {
    if (mInstance != null)
      throw new IllegalStateException("DesktopBrowse instance has already been set.");
    mInstance = paramDesktopBrowse;
  }

  public static DesktopBrowse getInstance()
  {
    return mInstance;
  }

  public abstract void browse(URL paramURL);
}
package sun.net;

import java.util.EventListener;

public abstract interface ProgressListener extends EventListener
{
  public abstract void progressStart(ProgressEvent paramProgressEvent);

  public abstract void progressUpdate(ProgressEvent paramProgressEvent);

  public abstract void progressFinish(ProgressEvent paramProgressEvent);
}
package sun.net;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class ProgressMonitor
{
  private static ProgressMeteringPolicy meteringPolicy = new DefaultProgressMeteringPolicy();
  private static ProgressMonitor pm = new ProgressMonitor();
  private ArrayList<ProgressSource> progressSourceList = new ArrayList();
  private ArrayList<ProgressListener> progressListenerList = new ArrayList();

  public static synchronized ProgressMonitor getDefault()
  {
    return pm;
  }

  public static synchronized void setDefault(ProgressMonitor paramProgressMonitor)
  {
    if (paramProgressMonitor != null)
      pm = paramProgressMonitor;
  }

  public static synchronized void setMeteringPolicy(ProgressMeteringPolicy paramProgressMeteringPolicy)
  {
    if (paramProgressMeteringPolicy != null)
      meteringPolicy = paramProgressMeteringPolicy;
  }

  public ArrayList<ProgressSource> getProgressSources()
  {
    ArrayList localArrayList1 = new ArrayList();
    try
    {
      synchronized (this.progressSourceList)
      {
        Iterator localIterator = this.progressSourceList.iterator();
        while (localIterator.hasNext())
        {
          ProgressSource localProgressSource = (ProgressSource)localIterator.next();
          localArrayList1.add((ProgressSource)localProgressSource.clone());
        }
      }
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      localCloneNotSupportedException.printStackTrace();
    }
    return localArrayList1;
  }

  public synchronized int getProgressUpdateThreshold()
  {
    return meteringPolicy.getProgressUpdateThreshold();
  }

  public boolean shouldMeterInput(URL paramURL, String paramString)
  {
    return meteringPolicy.shouldMeterInput(paramURL, paramString);
  }

  public void registerSource(ProgressSource paramProgressSource)
  {
    synchronized (this.progressSourceList)
    {
      if (!(this.progressSourceList.contains(paramProgressSource)))
        break label21;
      return;
      label21: this.progressSourceList.add(paramProgressSource);
    }
    if (this.progressListenerList.size() > 0)
    {
      Object localObject3;
      ??? = new ArrayList();
      synchronized (this.progressListenerList)
      {
        localObject3 = this.progressListenerList.iterator();
        while (((Iterator)localObject3).hasNext())
          ???.add(((Iterator)localObject3).next());
      }
      ??? = ???.iterator();
      while (((Iterator)???).hasNext())
      {
        localObject3 = (ProgressListener)((Iterator)???).next();
        ProgressEvent localProgressEvent = new ProgressEvent(paramProgressSource, paramProgressSource.getURL(), paramProgressSource.getMethod(), paramProgressSource.getContentType(), paramProgressSource.getState(), paramProgressSource.getProgress(), paramProgressSource.getExpected());
        ((ProgressListener)localObject3).progressStart(localProgressEvent);
      }
    }
  }

  public void unregisterSource(ProgressSource paramProgressSource)
  {
    synchronized (this.progressSourceList)
    {
      if (this.progressSourceList.contains(paramProgressSource))
        break label21;
      return;
      label21: paramProgressSource.close();
      this.progressSourceList.remove(paramProgressSource);
    }
    if (this.progressListenerList.size() > 0)
    {
      Object localObject3;
      ??? = new ArrayList();
      synchronized (this.progressListenerList)
      {
        localObject3 = this.progressListenerList.iterator();
        while (((Iterator)localObject3).hasNext())
          ???.add(((Iterator)localObject3).next());
      }
      ??? = ???.iterator();
      while (((Iterator)???).hasNext())
      {
        localObject3 = (ProgressListener)((Iterator)???).next();
        ProgressEvent localProgressEvent = new ProgressEvent(paramProgressSource, paramProgressSource.getURL(), paramProgressSource.getMethod(), paramProgressSource.getContentType(), paramProgressSource.getState(), paramProgressSource.getProgress(), paramProgressSource.getExpected());
        ((ProgressListener)localObject3).progressFinish(localProgressEvent);
      }
    }
  }

  public void updateProgress(ProgressSource paramProgressSource)
  {
    synchronized (this.progressSourceList)
    {
      if (this.progressSourceList.contains(paramProgressSource))
        break label21;
      label21: return;
    }
    if (this.progressListenerList.size() > 0)
    {
      Object localObject3;
      ??? = new ArrayList();
      synchronized (this.progressListenerList)
      {
        localObject3 = this.progressListenerList.iterator();
        while (((Iterator)localObject3).hasNext())
          ???.add(((Iterator)localObject3).next());
      }
      ??? = ???.iterator();
      while (((Iterator)???).hasNext())
      {
        localObject3 = (ProgressListener)((Iterator)???).next();
        ProgressEvent localProgressEvent = new ProgressEvent(paramProgressSource, paramProgressSource.getURL(), paramProgressSource.getMethod(), paramProgressSource.getContentType(), paramProgressSource.getState(), paramProgressSource.getProgress(), paramProgressSource.getExpected());
        ((ProgressListener)localObject3).progressUpdate(localProgressEvent);
      }
    }
  }

  public void addProgressListener(ProgressListener paramProgressListener)
  {
    synchronized (this.progressListenerList)
    {
      this.progressListenerList.add(paramProgressListener);
    }
  }

  public void removeProgressListener(ProgressListener paramProgressListener)
  {
    synchronized (this.progressListenerList)
    {
      this.progressListenerList.remove(paramProgressListener);
    }
  }
}
package sun.print;

import java.util.Vector;
import javax.print.PrintService;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.event.PrintServiceAttributeEvent;
import javax.print.event.PrintServiceAttributeListener;

class ServiceNotifier extends Thread
{
  private PrintService service;
  private Vector listeners;
  private boolean stop = false;
  private PrintServiceAttributeSet lastSet;

  ServiceNotifier(PrintService paramPrintService)
  {
    super(paramPrintService.getName() + " notifier");
    this.service = paramPrintService;
    this.listeners = new Vector();
    try
    {
      setPriority(4);
      setDaemon(true);
      start();
    }
    catch (SecurityException localSecurityException)
    {
    }
  }

  void addListener(PrintServiceAttributeListener paramPrintServiceAttributeListener)
  {
    synchronized (this)
    {
      if ((paramPrintServiceAttributeListener != null) && (this.listeners != null))
        break label18;
      return;
      label18: this.listeners.add(paramPrintServiceAttributeListener);
    }
  }

  void removeListener(PrintServiceAttributeListener paramPrintServiceAttributeListener)
  {
    synchronized (this)
    {
      if ((paramPrintServiceAttributeListener != null) && (this.listeners != null))
        break label18;
      return;
      label18: this.listeners.remove(paramPrintServiceAttributeListener);
    }
  }

  boolean isEmpty()
  {
    return ((this.listeners == null) || (this.listeners.isEmpty()));
  }

  void stopNotifier()
  {
    this.stop = true;
  }

  void wake()
  {
    try
    {
      interrupt();
    }
    catch (SecurityException localSecurityException)
    {
    }
  }

  public void run()
  {
    long l1 = 15000L;
    long l2 = 2000L;
    while (!(this.stop))
    {
      try
      {
        Thread.sleep(l2);
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      synchronized (this)
      {
        while (this.listeners == null);
        long l3 = System.currentTimeMillis();
        if (this.listeners == null)
          break label177;
        if (!(this.service instanceof AttributeUpdater))
          break label81;
        PrintServiceAttributeSet localPrintServiceAttributeSet = ((AttributeUpdater)this.service).getUpdatedAttributes();
        break label92:
        label81: localPrintServiceAttributeSet = this.service.getAttributes();
        label92: if ((localPrintServiceAttributeSet == null) || (localPrintServiceAttributeSet.isEmpty()))
          break label177;
        for (int i = 0; i < this.listeners.size(); ++i)
        {
          PrintServiceAttributeListener localPrintServiceAttributeListener = (PrintServiceAttributeListener)this.listeners.elementAt(i);
          HashPrintServiceAttributeSet localHashPrintServiceAttributeSet = new HashPrintServiceAttributeSet(localPrintServiceAttributeSet);
          PrintServiceAttributeEvent localPrintServiceAttributeEvent = new PrintServiceAttributeEvent(this.service, localHashPrintServiceAttributeSet);
          localPrintServiceAttributeListener.attributeUpdate(localPrintServiceAttributeEvent);
        }
        label177: l2 = (System.currentTimeMillis() - l3) * 10L;
        if (l2 >= l1)
          break label196;
        label196: l2 = l1;
      }
    }
  }
}
package sun.print;

import javax.print.PrintService;

public abstract interface BackgroundLookupListener
{
  public abstract void notifyServices(PrintService[] paramArrayOfPrintService);
}
package sun.awt.windows;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

public class WPageDialog extends WPrintDialog
{
  PageFormat page;
  Printable painter;

  WPageDialog(Frame paramFrame, PrinterJob paramPrinterJob, PageFormat paramPageFormat, Printable paramPrintable)
  {
    super(paramFrame, paramPrinterJob);
    this.page = paramPageFormat;
    this.painter = paramPrintable;
  }

  WPageDialog(Dialog paramDialog, PrinterJob paramPrinterJob, PageFormat paramPageFormat, Printable paramPrintable)
  {
    super(paramDialog, paramPrinterJob);
    this.page = paramPageFormat;
    this.painter = paramPrintable;
  }

  public void addNotify()
  {
    synchronized (getTreeLock())
    {
      Container localContainer = getParent();
      if ((localContainer != null) && (localContainer.getPeer() == null))
        localContainer.addNotify();
      if (getPeer() == null)
      {
        WPageDialogPeer localWPageDialogPeer = ((WToolkit)Toolkit.getDefaultToolkit()).createWPageDialog(this);
        setPeer(localWPageDialogPeer);
      }
      super.addNotify();
    }
  }

  private static native void initIDs();

  static
  {
    initIDs();
  }
}
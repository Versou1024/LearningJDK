package sun.awt.windows;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.peer.ComponentPeer;
import java.awt.print.PrinterJob;

public class WPrintDialog extends Dialog
{
  protected PrintJob job;
  protected PrinterJob pjob;
  private boolean retval = false;

  public WPrintDialog(Frame paramFrame, PrinterJob paramPrinterJob)
  {
    super(paramFrame, true);
    this.pjob = paramPrinterJob;
    setLayout(null);
  }

  public WPrintDialog(Dialog paramDialog, PrinterJob paramPrinterJob)
  {
    super(paramDialog, "", true);
    this.pjob = paramPrinterJob;
    setLayout(null);
  }

  protected native void setPeer(ComponentPeer paramComponentPeer);

  public void addNotify()
  {
    synchronized (getTreeLock())
    {
      Container localContainer = getParent();
      if ((localContainer != null) && (localContainer.getPeer() == null))
        localContainer.addNotify();
      if (getPeer() == null)
      {
        WPrintDialogPeer localWPrintDialogPeer = ((WToolkit)Toolkit.getDefaultToolkit()).createWPrintDialog(this);
        setPeer(localWPrintDialogPeer);
      }
      super.addNotify();
    }
  }

  public void setRetVal(boolean paramBoolean)
  {
    this.retval = paramBoolean;
  }

  public boolean getRetVal()
  {
    return this.retval;
  }

  private static native void initIDs();

  static
  {
    initIDs();
  }
}
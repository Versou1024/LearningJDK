package sun.awt.windows;

public class WPageDialogPeer extends WPrintDialogPeer
{
  WPageDialogPeer(WPageDialog paramWPageDialog)
  {
    super(paramWPageDialog);
  }

  private native boolean _show();

  public void show()
  {
    new Thread(new Runnable(this)
    {
      public void run()
      {
        ((WPrintDialog)this.this$0.target).setRetVal(WPageDialogPeer.access$000(this.this$0));
        ((WPrintDialog)this.this$0.target).hide();
      }
    }).start();
  }
}
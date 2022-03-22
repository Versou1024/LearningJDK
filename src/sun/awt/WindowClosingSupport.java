package sun.awt;

public abstract interface WindowClosingSupport
{
  public abstract WindowClosingListener getWindowClosingListener();

  public abstract void setWindowClosingListener(WindowClosingListener paramWindowClosingListener);
}
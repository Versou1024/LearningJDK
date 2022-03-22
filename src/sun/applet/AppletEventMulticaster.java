package sun.applet;

public class AppletEventMulticaster
  implements AppletListener
{
  private final AppletListener a;
  private final AppletListener b;

  public AppletEventMulticaster(AppletListener paramAppletListener1, AppletListener paramAppletListener2)
  {
    this.a = paramAppletListener1;
    this.b = paramAppletListener2;
  }

  public void appletStateChanged(AppletEvent paramAppletEvent)
  {
    this.a.appletStateChanged(paramAppletEvent);
    this.b.appletStateChanged(paramAppletEvent);
  }

  public static AppletListener add(AppletListener paramAppletListener1, AppletListener paramAppletListener2)
  {
    return addInternal(paramAppletListener1, paramAppletListener2);
  }

  public static AppletListener remove(AppletListener paramAppletListener1, AppletListener paramAppletListener2)
  {
    return removeInternal(paramAppletListener1, paramAppletListener2);
  }

  private static AppletListener addInternal(AppletListener paramAppletListener1, AppletListener paramAppletListener2)
  {
    if (paramAppletListener1 == null)
      return paramAppletListener2;
    if (paramAppletListener2 == null)
      return paramAppletListener1;
    return new AppletEventMulticaster(paramAppletListener1, paramAppletListener2);
  }

  protected AppletListener remove(AppletListener paramAppletListener)
  {
    if (paramAppletListener == this.a)
      return this.b;
    if (paramAppletListener == this.b)
      return this.a;
    AppletListener localAppletListener1 = removeInternal(this.a, paramAppletListener);
    AppletListener localAppletListener2 = removeInternal(this.b, paramAppletListener);
    if ((localAppletListener1 == this.a) && (localAppletListener2 == this.b))
      return this;
    return addInternal(localAppletListener1, localAppletListener2);
  }

  private static AppletListener removeInternal(AppletListener paramAppletListener1, AppletListener paramAppletListener2)
  {
    if ((paramAppletListener1 == paramAppletListener2) || (paramAppletListener1 == null))
      return null;
    if (paramAppletListener1 instanceof AppletEventMulticaster)
      return ((AppletEventMulticaster)paramAppletListener1).remove(paramAppletListener2);
    return paramAppletListener1;
  }
}
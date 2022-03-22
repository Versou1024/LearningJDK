package sun.org.mozilla.javascript.internal;

/**
 * @deprecated
 */
public abstract interface ContextListener extends ContextFactory.Listener
{
  /**
   * @deprecated
   */
  public abstract void contextEntered(Context paramContext);

  /**
   * @deprecated
   */
  public abstract void contextExited(Context paramContext);
}
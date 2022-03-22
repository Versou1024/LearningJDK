package sun.org.mozilla.javascript.internal;

public class WrappedException extends EvaluatorException
{
  static final long serialVersionUID = -1551979216966520648L;
  private Throwable exception;

  public WrappedException(Throwable paramThrowable)
  {
    super("Wrapped " + paramThrowable.toString());
    this.exception = paramThrowable;
    Kit.initCause(this, paramThrowable);
    int[] arrayOfInt = { 0 };
    String str = Context.getSourcePositionFromStack(arrayOfInt);
    int i = arrayOfInt[0];
    if (str != null)
      initSourceName(str);
    if (i != 0)
      initLineNumber(i);
  }

  public Throwable getWrappedException()
  {
    return this.exception;
  }

  /**
   * @deprecated
   */
  public Object unwrap()
  {
    return getWrappedException();
  }
}
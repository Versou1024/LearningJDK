package sun.org.mozilla.javascript.internal;

public class EvaluatorException extends RhinoException
{
  static final long serialVersionUID = -8743165779676009808L;

  public EvaluatorException(String paramString)
  {
    super(paramString);
  }

  public EvaluatorException(String paramString1, String paramString2, int paramInt)
  {
    this(paramString1, paramString2, paramInt, null, 0);
  }

  public EvaluatorException(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2)
  {
    super(paramString1);
    recordErrorOrigin(paramString2, paramInt1, paramString3, paramInt2);
  }

  /**
   * @deprecated
   */
  public String getSourceName()
  {
    return sourceName();
  }

  /**
   * @deprecated
   */
  public int getLineNumber()
  {
    return lineNumber();
  }

  /**
   * @deprecated
   */
  public int getColumnNumber()
  {
    return columnNumber();
  }

  /**
   * @deprecated
   */
  public String getLineSource()
  {
    return lineSource();
  }
}
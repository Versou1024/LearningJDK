package sun.org.mozilla.javascript.internal;

public class EcmaError extends RhinoException
{
  static final long serialVersionUID = -6261226256957286699L;
  private String errorName;
  private String errorMessage;

  EcmaError(String paramString1, String paramString2, String paramString3, int paramInt1, String paramString4, int paramInt2)
  {
    recordErrorOrigin(paramString3, paramInt1, paramString4, paramInt2);
    this.errorName = paramString1;
    this.errorMessage = paramString2;
  }

  /**
   * @deprecated
   */
  public EcmaError(Scriptable paramScriptable, String paramString1, int paramInt1, int paramInt2, String paramString2)
  {
    this("InternalError", ScriptRuntime.toString(paramScriptable), paramString1, paramInt1, paramString2, paramInt2);
  }

  public String details()
  {
    return this.errorName + ": " + this.errorMessage;
  }

  public String getName()
  {
    return this.errorName;
  }

  public String getErrorMessage()
  {
    return this.errorMessage;
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

  /**
   * @deprecated
   */
  public Scriptable getErrorObject()
  {
    return null;
  }
}
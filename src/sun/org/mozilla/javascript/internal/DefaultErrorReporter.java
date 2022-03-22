package sun.org.mozilla.javascript.internal;

class DefaultErrorReporter
  implements ErrorReporter
{
  static final DefaultErrorReporter instance = new DefaultErrorReporter();
  private boolean forEval;
  private ErrorReporter chainedReporter;

  static ErrorReporter forEval(ErrorReporter paramErrorReporter)
  {
    DefaultErrorReporter localDefaultErrorReporter = new DefaultErrorReporter();
    localDefaultErrorReporter.forEval = true;
    localDefaultErrorReporter.chainedReporter = paramErrorReporter;
    return localDefaultErrorReporter;
  }

  public void warning(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2)
  {
    if (this.chainedReporter != null)
      this.chainedReporter.warning(paramString1, paramString2, paramInt1, paramString3, paramInt2);
  }

  public void error(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2)
  {
    if (this.forEval)
      throw ScriptRuntime.constructError("SyntaxError", paramString1, paramString2, paramInt1, paramString3, paramInt2);
    if (this.chainedReporter != null)
      this.chainedReporter.error(paramString1, paramString2, paramInt1, paramString3, paramInt2);
    else
      throw runtimeError(paramString1, paramString2, paramInt1, paramString3, paramInt2);
  }

  public EvaluatorException runtimeError(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2)
  {
    if (this.chainedReporter != null)
      return this.chainedReporter.runtimeError(paramString1, paramString2, paramInt1, paramString3, paramInt2);
    return new EvaluatorException(paramString1, paramString2, paramInt1, paramString3, paramInt2);
  }
}
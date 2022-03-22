package sun.org.mozilla.javascript.internal;

public abstract interface ErrorReporter
{
  public abstract void warning(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2);

  public abstract void error(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2);

  public abstract EvaluatorException runtimeError(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2);
}
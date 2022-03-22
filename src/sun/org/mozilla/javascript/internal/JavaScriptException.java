package sun.org.mozilla.javascript.internal;

public class JavaScriptException extends RhinoException
{
  static final long serialVersionUID = -7666130513694669293L;
  private Object value;

  /**
   * @deprecated
   */
  public JavaScriptException(Object paramObject)
  {
    this(paramObject, "", 0);
  }

  public JavaScriptException(Object paramObject, String paramString, int paramInt)
  {
    recordErrorOrigin(paramString, paramInt, null, 0);
    this.value = paramObject;
  }

  public String details()
  {
    if (this.value instanceof Scriptable)
      return ScriptRuntime.defaultObjectToString((Scriptable)this.value);
    return ScriptRuntime.toString(this.value);
  }

  public Object getValue()
  {
    return this.value;
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
}
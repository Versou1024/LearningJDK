package sun.org.mozilla.javascript.internal;

import java.io.CharArrayWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

public abstract class RhinoException extends RuntimeException
{
  private String sourceName;
  private int lineNumber;
  private String lineSource;
  private int columnNumber;
  Object interpreterStackInfo;
  int[] interpreterLineData;

  RhinoException()
  {
    Interpreter.captureInterpreterStackInfo(this);
  }

  RhinoException(String paramString)
  {
    super(paramString);
    Interpreter.captureInterpreterStackInfo(this);
  }

  public final String getMessage()
  {
    String str = details();
    if ((this.sourceName == null) || (this.lineNumber <= 0))
      return str;
    StringBuffer localStringBuffer = new StringBuffer(str);
    localStringBuffer.append(" (");
    if (this.sourceName != null)
      localStringBuffer.append(this.sourceName);
    if (this.lineNumber > 0)
    {
      localStringBuffer.append('#');
      localStringBuffer.append(this.lineNumber);
    }
    localStringBuffer.append(')');
    return localStringBuffer.toString();
  }

  public String details()
  {
    return super.getMessage();
  }

  public final String sourceName()
  {
    return this.sourceName;
  }

  public final void initSourceName(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException();
    if (this.sourceName != null)
      throw new IllegalStateException();
    this.sourceName = paramString;
  }

  public final int lineNumber()
  {
    return this.lineNumber;
  }

  public final void initLineNumber(int paramInt)
  {
    if (paramInt <= 0)
      throw new IllegalArgumentException(String.valueOf(paramInt));
    if (this.lineNumber > 0)
      throw new IllegalStateException();
    this.lineNumber = paramInt;
  }

  public final int columnNumber()
  {
    return this.columnNumber;
  }

  public final void initColumnNumber(int paramInt)
  {
    if (paramInt <= 0)
      throw new IllegalArgumentException(String.valueOf(paramInt));
    if (this.columnNumber > 0)
      throw new IllegalStateException();
    this.columnNumber = paramInt;
  }

  public final String lineSource()
  {
    return this.lineSource;
  }

  public final void initLineSource(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException();
    if (this.lineSource != null)
      throw new IllegalStateException();
    this.lineSource = paramString;
  }

  final void recordErrorOrigin(String paramString1, int paramInt1, String paramString2, int paramInt2)
  {
    if (paramInt1 == -1)
      paramInt1 = 0;
    if (paramString1 != null)
      initSourceName(paramString1);
    if (paramInt1 != 0)
      initLineNumber(paramInt1);
    if (paramString2 != null)
      initLineSource(paramString2);
    if (paramInt2 != 0)
      initColumnNumber(paramInt2);
  }

  private String generateStackTrace()
  {
    CharArrayWriter localCharArrayWriter = new CharArrayWriter();
    super.printStackTrace(new PrintWriter(localCharArrayWriter));
    String str = localCharArrayWriter.toString();
    return Interpreter.getPatchedStack(this, str);
  }

  public void printStackTrace(PrintWriter paramPrintWriter)
  {
    if (this.interpreterStackInfo == null)
      super.printStackTrace(paramPrintWriter);
    else
      paramPrintWriter.print(generateStackTrace());
  }

  public void printStackTrace(PrintStream paramPrintStream)
  {
    if (this.interpreterStackInfo == null)
      super.printStackTrace(paramPrintStream);
    else
      paramPrintStream.print(generateStackTrace());
  }
}
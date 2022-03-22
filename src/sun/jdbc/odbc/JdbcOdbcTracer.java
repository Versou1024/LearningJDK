package sun.jdbc.odbc;

import java.io.PrintWriter;
import java.sql.DriverManager;

public class JdbcOdbcTracer
{
  private PrintWriter outWriter;

  public boolean isTracing()
  {
    if (this.outWriter != null)
      return true;
    if (DriverManager.getLogWriter() != null)
      return (DriverManager.getLogWriter() != null);
    return false;
  }

  public void trace(String paramString)
  {
    if (this.outWriter != null)
    {
      this.outWriter.println(paramString);
      this.outWriter.flush();
    }
    else if (DriverManager.getLogWriter() != null)
    {
      DriverManager.getLogWriter().println(paramString);
      DriverManager.getLogWriter().flush();
    }
  }

  public void trace(String paramString, boolean paramBoolean)
  {
    if (paramBoolean)
      trace(paramString);
    if (this.outWriter != null)
    {
      this.outWriter.println(paramString);
      this.outWriter.flush();
    }
    else if (DriverManager.getLogWriter() != null)
    {
      DriverManager.getLogWriter().println(paramString);
      DriverManager.getLogWriter().flush();
    }
  }

  public void setWriter(PrintWriter paramPrintWriter)
  {
    if (paramPrintWriter != null)
      this.outWriter = paramPrintWriter;
    else
      this.outWriter = null;
  }

  public PrintWriter getWriter()
  {
    return this.outWriter;
  }
}
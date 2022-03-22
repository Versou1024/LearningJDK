package sun.jdbc.odbc;

import java.sql.SQLWarning;

public class JdbcOdbcSQLWarning extends SQLWarning
{
  Object value;

  public JdbcOdbcSQLWarning(String paramString1, String paramString2, int paramInt)
  {
    super(paramString1, paramString2, paramInt);
  }

  public JdbcOdbcSQLWarning(String paramString1, String paramString2)
  {
    super(paramString1, paramString2);
  }

  public JdbcOdbcSQLWarning(String paramString)
  {
    super(paramString);
  }

  public JdbcOdbcSQLWarning()
  {
  }
}
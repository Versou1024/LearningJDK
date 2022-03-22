package sun.jdbc.odbc;

import java.sql.Driver;
import java.sql.SQLException;

public abstract interface JdbcOdbcDriverInterface extends Driver
{
  public abstract long allocConnection(long paramLong)
    throws SQLException;

  public abstract void closeConnection(long paramLong)
    throws SQLException;

  public abstract void disconnect(long paramLong)
    throws SQLException;
}
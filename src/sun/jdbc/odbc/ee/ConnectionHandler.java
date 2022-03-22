package sun.jdbc.odbc.ee;

import java.sql.SQLException;
import sun.jdbc.odbc.JdbcOdbc;
import sun.jdbc.odbc.JdbcOdbcConnection;
import sun.jdbc.odbc.JdbcOdbcDriverInterface;
import sun.jdbc.odbc.JdbcOdbcTracer;

public class ConnectionHandler extends JdbcOdbcConnection
{
  private JdbcOdbcConnection con;
  private JdbcOdbcTracer tracer = new JdbcOdbcTracer();
  final int NOTOPEN = 0;
  final int OPEN = 1;
  final int CLOSING = 2;
  final int CLOSED = 3;
  final int DESTROYING = 4;
  final int DESTROYED = 5;
  private int state = 0;
  private PooledObject jpo;

  public ConnectionHandler(JdbcOdbc paramJdbcOdbc, long paramLong, JdbcOdbcDriverInterface paramJdbcOdbcDriverInterface)
  {
    super(paramJdbcOdbc, paramLong, paramJdbcOdbcDriverInterface);
    this.tracer = paramJdbcOdbc.getTracer();
  }

  public synchronized void close()
  {
    if (this.state != 1)
      return;
    this.state = 2;
    try
    {
      if (this.tracer.isTracing())
        this.tracer.trace("*Releasing all resources to this connection Connection.close");
      super.setFreeStmtsFromConnectionOnly();
      super.closeAllStatements();
      super.setFreeStmtsFromAnyWhere();
      if (this.tracer.isTracing())
        this.tracer.trace("*Releasing all resources to this connection Connection.close");
      this.jpo.markUsable();
      this.state = 3;
      ((PooledConnection)this.jpo).connectionClosed();
    }
    catch (Exception localException)
    {
      this.tracer.trace("Error occured while closing the connection " + this + " " + localException.getMessage());
      ((PooledConnection)this.jpo).connectionErrorOccurred(new SQLException(localException.getMessage()));
    }
  }

  public boolean isClosed()
    throws SQLException
  {
    return (this.state != 1);
  }

  public synchronized void actualClose()
    throws SQLException
  {
    if ((this.state == 4) || (this.state == 5))
      return;
    if (this.state == 1)
    {
      this.jpo.markForSweep();
      close();
    }
    this.state = 4;
    try
    {
      if (this.tracer.isTracing())
        this.tracer.trace("*Actual Connection.close");
      super.close();
      this.state = 5;
    }
    catch (SQLException localSQLException)
    {
      this.state = 5;
      this.tracer.trace("Error occured while closing the connection " + this + " " + localSQLException.getMessage());
      throw localSQLException;
    }
    catch (Exception localException)
    {
      this.state = 5;
      this.tracer.trace("Error occured while closing the connection " + this + " " + localException.getMessage());
      throw new SQLException("Unexpected exception:" + localException.getMessage());
    }
  }

  public void destroy()
    throws SQLException
  {
    if ((this.state == 4) || (this.state == 5))
      return;
    this.state = 4;
    try
    {
      if (this.tracer.isTracing())
        this.tracer.trace("*ConnectionHandler.destroy");
      super.close();
      this.state = 5;
    }
    catch (SQLException localSQLException)
    {
      this.state = 5;
      this.tracer.trace("Error occured while closing the connection " + this + " " + localSQLException.getMessage());
      throw localSQLException;
    }
    catch (Exception localException)
    {
      this.state = 5;
      this.tracer.trace("Error occured while closing the connection " + this + " " + localException.getMessage());
      throw new SQLException("Unexpected exception:" + localException.getMessage());
    }
  }

  public int getState()
  {
    return this.state;
  }

  public void setState(int paramInt)
  {
    this.state = paramInt;
  }

  public void setPooledObject(PooledObject paramPooledObject)
  {
    this.jpo = paramPooledObject;
  }

  public void finalize()
  {
    this.tracer.trace("Connectionhandler Finalize....");
    try
    {
      destroy();
    }
    catch (Exception localException)
    {
    }
  }
}
package sun.jdbc.odbc.ee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import javax.sql.ConnectionEvent;
import javax.sql.StatementEventListener;
import sun.jdbc.odbc.JdbcOdbcDriver;
import sun.jdbc.odbc.JdbcOdbcTracer;

public class PooledConnection
  implements javax.sql.PooledConnection, PooledObject
{
  private String strUserId = null;
  private String strPassword = null;
  private String strUrl = null;
  private String strCharset;
  private int timeout;
  private Properties pr = null;
  private ConnectionHandler conHandler;
  private boolean isAvailableForUse = true;
  private Hashtable htListener;
  private long time = 3412045521726996480L;
  private JdbcOdbcTracer tracer = new JdbcOdbcTracer();
  private JdbcOdbcDriver driver = null;
  private int state;
  private ConnectionEventListener listener;

  public PooledConnection(Properties paramProperties, JdbcOdbcTracer paramJdbcOdbcTracer)
    throws SQLException
  {
    try
    {
      this.tracer = paramJdbcOdbcTracer;
      this.strUserId = ((String)paramProperties.get("user"));
      this.strPassword = ((String)paramProperties.get("password"));
      this.strUrl = ((String)paramProperties.get("url"));
      this.strCharset = ((String)paramProperties.get("charset"));
      this.timeout = Integer.parseInt((String)paramProperties.get("loginTimeout"));
      this.pr = paramProperties;
      this.time = System.currentTimeMillis();
      this.htListener = new Hashtable();
      this.driver = new JdbcOdbcDriver();
      this.driver.setTimeOut(this.timeout);
      this.driver.setWriter(paramJdbcOdbcTracer.getWriter());
      paramJdbcOdbcTracer.trace(" PooledConnection Being created ...." + this.strUserId + ":" + this.strPassword + ":" + this.strUrl + ":" + this.driver);
      this.conHandler = ((ConnectionHandler)this.driver.EEConnect(this.strUrl, paramProperties));
      this.conHandler.setPooledObject(this);
    }
    catch (SQLException localSQLException)
    {
      throw localSQLException;
    }
    catch (Exception localException)
    {
      throw new SQLException("Error in creating pooled connection" + localException.getMessage());
    }
  }

  public boolean isMatching(Properties paramProperties)
  {
    return this.pr.equals(paramProperties);
  }

  public boolean isUsable()
  {
    return this.isAvailableForUse;
  }

  public void markForSweep()
  {
    this.state = 3;
  }

  public boolean isMarkedForSweep()
  {
    return (this.state == 3);
  }

  public void markUsable()
  {
    this.isAvailableForUse = true;
  }

  public long getCreatedTime()
  {
    return this.time;
  }

  public void addConnectionEventListener(javax.sql.ConnectionEventListener paramConnectionEventListener)
  {
    this.htListener.put(paramConnectionEventListener, "");
    if (paramConnectionEventListener instanceof ConnectionEventListener)
      this.listener = ((ConnectionEventListener)paramConnectionEventListener);
  }

  public Connection getConnection()
    throws SQLException
  {
    this.conHandler.getClass();
    if (this.conHandler.getState() != 3)
    {
      this.conHandler.getClass();
      if (this.conHandler.getState() != 0)
        throw new SQLException("Connection is not available now!");
    }
    if (this.state == 3)
      throw new SQLException("PooledConnection is not usable");
    if (this.state == 1)
      this.listener.connectionCheckOut(new ConnectionEvent(this));
    this.isAvailableForUse = false;
    this.conHandler.getClass();
    this.conHandler.setState(1);
    return this.conHandler;
  }

  public void removeConnectionEventListener(javax.sql.ConnectionEventListener paramConnectionEventListener)
  {
    this.htListener.remove(paramConnectionEventListener);
  }

  public void close()
    throws SQLException
  {
    try
    {
      this.isAvailableForUse = false;
      this.state = 3;
      this.conHandler.actualClose();
    }
    catch (SQLException localSQLException)
    {
      throw localSQLException;
    }
    catch (Exception localException)
    {
      throw new SQLException("Unexpected Exception : " + localException.getMessage());
    }
  }

  public void destroy()
  {
    try
    {
      this.isAvailableForUse = false;
      this.state = 3;
      this.conHandler.destroy();
    }
    catch (Exception localException)
    {
    }
  }

  public void connectionClosed()
  {
    Enumeration localEnumeration = this.htListener.keys();
    while (localEnumeration.hasMoreElements())
    {
      javax.sql.ConnectionEventListener localConnectionEventListener = (javax.sql.ConnectionEventListener)localEnumeration.nextElement();
      ConnectionEvent localConnectionEvent = new ConnectionEvent(this);
      localConnectionEventListener.connectionClosed(localConnectionEvent);
    }
  }

  public void connectionErrorOccurred(SQLException paramSQLException)
  {
    Enumeration localEnumeration = this.htListener.keys();
    while (localEnumeration.hasMoreElements())
    {
      javax.sql.ConnectionEventListener localConnectionEventListener = (javax.sql.ConnectionEventListener)localEnumeration.nextElement();
      ConnectionEvent localConnectionEvent = new ConnectionEvent(this, paramSQLException);
      localConnectionEventListener.connectionErrorOccurred(localConnectionEvent);
    }
  }

  public void checkedOut()
  {
    if (this.state != 3)
      this.state = 2;
  }

  public void checkedIn()
  {
    if (this.state != 3)
      this.state = 1;
  }

  public void addConnectionEventListener(ConnectionEventListener paramConnectionEventListener)
  {
    throw new UnsupportedOperationException();
  }

  public void removeConnectionEventListener(ConnectionEventListener paramConnectionEventListener)
  {
    throw new UnsupportedOperationException();
  }

  public void addStatementEventListener(StatementEventListener paramStatementEventListener)
  {
    throw new UnsupportedOperationException();
  }

  public void removeStatementEventListener(StatementEventListener paramStatementEventListener)
  {
    throw new UnsupportedOperationException();
  }
}
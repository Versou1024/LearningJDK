package sun.jdbc.odbc.ee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.PooledConnection;

public class ConnectionPoolDataSource extends CommonDataSource
  implements javax.sql.ConnectionPoolDataSource
{
  private int maxStatements;
  private int initialPoolSize;
  private int minPoolSize;
  private int maxPoolSize;
  private int maxIdleTime;
  private int propertyCycle;
  private int timeoutFromPool;
  private int mInterval;
  private boolean shutdown = false;
  static final long serialVersionUID = 8730440750011279189L;

  public ConnectionPoolDataSource()
  {
  }

  public ConnectionPoolDataSource(String paramString)
  {
    super.setDataSourceName(paramString);
  }

  public Connection getConnection()
    throws SQLException
  {
    return getPooledConnection().getConnection();
  }

  public Connection getConnection(String paramString1, String paramString2)
    throws SQLException
  {
    return getPooledConnection(paramString1, paramString2).getConnection();
  }

  public PooledConnection getPooledConnection()
    throws SQLException
  {
    return ((PooledConnection)getPool().checkOut());
  }

  public PooledConnection getPooledConnection(String paramString1, String paramString2)
    throws SQLException
  {
    Properties localProperties = super.getAttributes().getProperties();
    localProperties.put("user", paramString1);
    localProperties.put("password", paramString2);
    return ((PooledConnection)getPool().checkOut(localProperties));
  }

  public void setMaxStatements(String paramString)
    throws SQLException
  {
  }

  public int getMaxStatements()
  {
    return this.maxStatements;
  }

  public void setInitialPoolSize(String paramString)
    throws SQLException
  {
    if (paramString == null)
      throw new SQLException("Initial pool size cannot be null");
    try
    {
      this.initialPoolSize = Integer.parseInt(paramString.trim());
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new SQLException("Initial pool size is not a number ");
    }
  }

  public int getInitialPoolSize()
  {
    return this.initialPoolSize;
  }

  public void setMaxPoolSize(String paramString)
    throws SQLException
  {
    if (paramString == null)
      throw new SQLException("Max pool size cannot be null");
    try
    {
      this.maxPoolSize = Integer.parseInt(paramString.trim());
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new SQLException("Max pool size is not a number ");
    }
  }

  public int getMaxPoolSize()
  {
    return this.maxPoolSize;
  }

  public void setMinPoolSize(String paramString)
    throws SQLException
  {
    if (paramString == null)
      throw new SQLException("Min pool size cannot be null");
    try
    {
      this.minPoolSize = Integer.parseInt(paramString.trim());
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new SQLException("Min pool size is not a number ");
    }
  }

  public int getMinPoolSize()
  {
    return this.minPoolSize;
  }

  public void setMaxIdleTime(String paramString)
    throws SQLException
  {
    if (paramString == null)
      throw new SQLException("Idle time cannot be null");
    try
    {
      this.maxIdleTime = Integer.parseInt(paramString.trim());
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new SQLException("Max Idle time is not a number ");
    }
  }

  public int getMaxIdleTime()
  {
    return this.maxIdleTime;
  }

  public void setPropertyCycle(String paramString)
  {
  }

  public int getPropertyCycle()
  {
    return this.propertyCycle;
  }

  public void setTimeoutFromPool(String paramString)
    throws SQLException
  {
    if (paramString == null)
      throw new SQLException("timeout cannot be null");
    try
    {
      this.timeoutFromPool = Integer.parseInt(paramString.trim());
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new SQLException("Timeout is not a number ");
    }
  }

  public int getTimeoutFromPool()
  {
    return this.timeoutFromPool;
  }

  public void setMaintenanceInterval(String paramString)
    throws SQLException
  {
    if (paramString == null)
      throw new SQLException("Maintenance interval cannot be null");
    try
    {
      this.mInterval = Integer.parseInt(paramString.trim());
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new SQLException("Maintenance interval is not a number ");
    }
  }

  public int getMaintenanceInterval()
  {
    return this.mInterval;
  }

  public Reference getReference()
    throws NamingException
  {
    Reference localReference = new Reference(getClass().getName(), "sun.jdbc.odbc.ee.ObjectFactory", null);
    localReference.add(new StringRefAddr("databaseName", super.getDatabaseName()));
    localReference.add(new StringRefAddr("dataSourceName", super.getDataSourceName()));
    ConnectionAttributes localConnectionAttributes = super.getAttributes();
    localReference.add(new StringRefAddr("user", localConnectionAttributes.getUser()));
    localReference.add(new StringRefAddr("password", localConnectionAttributes.getPassword()));
    localReference.add(new StringRefAddr("charSet", localConnectionAttributes.getCharSet()));
    localReference.add(new StringRefAddr("loginTimeout", "" + super.getLoginTimeout()));
    localReference.add(new StringRefAddr("maxStatements", "" + this.maxStatements));
    localReference.add(new StringRefAddr("initialPoolSize", "" + this.initialPoolSize));
    localReference.add(new StringRefAddr("maxPoolSize", "" + this.maxPoolSize));
    localReference.add(new StringRefAddr("minPoolSize", "" + this.minPoolSize));
    localReference.add(new StringRefAddr("maxIdleTime", "" + this.maxIdleTime));
    localReference.add(new StringRefAddr("propertyCycle", "" + this.propertyCycle));
    localReference.add(new StringRefAddr("timeoutFromPool", "" + this.timeoutFromPool));
    localReference.add(new StringRefAddr("mInterval", "" + this.mInterval));
    return localReference;
  }

  public void shutDown(boolean paramBoolean)
  {
    ConnectionPool localConnectionPool = ConnectionPoolFactory.obtainConnectionPool(getDataSourceName());
    localConnectionPool.shutDown(paramBoolean);
    this.shutdown = true;
  }

  private ConnectionPool getPool()
    throws SQLException
  {
    if (this.shutdown)
      throw new SQLException("Pool is shutdown!");
    ConnectionPool localConnectionPool = ConnectionPoolFactory.obtainConnectionPool(super.getDataSourceName());
    localConnectionPool.setTracer(super.getTracer());
    PoolProperties localPoolProperties = new PoolProperties();
    localPoolProperties.set("initialPoolSize", this.initialPoolSize);
    localPoolProperties.set("maxPoolSize", this.maxPoolSize);
    localPoolProperties.set("minPoolSize", this.minPoolSize);
    localPoolProperties.set("maxIdleTime", this.maxIdleTime);
    localPoolProperties.set("timeOutFromPool", this.timeoutFromPool);
    localPoolProperties.set("mInterval", this.mInterval);
    localConnectionPool.setProperties(localPoolProperties);
    localConnectionPool.setConnectionDetails(super.getAttributes().getProperties());
    localConnectionPool.initializePool();
    return localConnectionPool;
  }

  public <T> T unwrap(Class<T> paramClass)
    throws SQLException
  {
    return null;
  }

  public boolean isWrapperFor(Class<?> paramClass)
    throws SQLException
  {
    return false;
  }
}
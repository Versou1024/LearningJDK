package sun.jdbc.odbc.ee;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

public class ConnectionPool extends ObjectPool
{
  private ConnectionEventListener cel;
  Properties cp;

  public ConnectionPool(String paramString)
  {
    super(paramString);
    this.cel = new ConnectionEventListener(paramString);
  }

  public void setConnectionDetails(Properties paramProperties)
  {
    this.cp = paramProperties;
  }

  protected PooledObject create(Properties paramProperties)
    throws SQLException
  {
    Properties localProperties = null;
    if (paramProperties != null)
      localProperties = paramProperties;
    else
      localProperties = this.cp;
    PooledConnection localPooledConnection = new PooledConnection(localProperties, super.getTracer());
    localPooledConnection.addConnectionEventListener(this.cel);
    return localPooledConnection;
  }

  protected void destroyFromPool(PooledObject paramPooledObject, Hashtable paramHashtable)
  {
    super.destroyFromPool(paramPooledObject, paramHashtable);
    ((PooledConnection)paramPooledObject).removeConnectionEventListener(this.cel);
  }
}
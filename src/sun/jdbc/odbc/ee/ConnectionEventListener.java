package sun.jdbc.odbc.ee;

import java.sql.SQLException;
import javax.sql.ConnectionEvent;

public class ConnectionEventListener
  implements javax.sql.ConnectionEventListener
{
  private PooledObject objPool;
  private String name;

  public ConnectionEventListener(String paramString)
  {
    this.name = paramString;
  }

  public void connectionClosed(ConnectionEvent paramConnectionEvent)
  {
    Object localObject = paramConnectionEvent.getSource();
    this.objPool = ((PooledObject)localObject);
    ConnectionPool localConnectionPool = ConnectionPoolFactory.obtainConnectionPool(this.name);
    localConnectionPool.checkIn(this.objPool);
  }

  public void connectionErrorOccurred(ConnectionEvent paramConnectionEvent)
  {
    Object localObject = paramConnectionEvent.getSource();
    this.objPool = ((PooledObject)localObject);
    this.objPool.markForSweep();
    ConnectionPool localConnectionPool = ConnectionPoolFactory.obtainConnectionPool(this.name);
    localConnectionPool.checkIn(this.objPool);
  }

  public void connectionCheckOut(ConnectionEvent paramConnectionEvent)
    throws SQLException
  {
    Object localObject = paramConnectionEvent.getSource();
    this.objPool = ((PooledObject)localObject);
    ConnectionPool localConnectionPool = ConnectionPoolFactory.obtainConnectionPool(this.name);
    localConnectionPool.tryCheckOut(this.objPool);
  }
}
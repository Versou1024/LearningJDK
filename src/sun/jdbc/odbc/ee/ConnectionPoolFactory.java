package sun.jdbc.odbc.ee;

import java.util.Hashtable;

public class ConnectionPoolFactory
{
  private static Hashtable pools;

  public static ConnectionPool obtainConnectionPool(String paramString)
  {
    if (pools == null)
      pools = new Hashtable();
    if ((pools.containsKey(paramString)) && (pools.get(paramString) != null))
      return ((ConnectionPool)pools.get(paramString));
    ConnectionPool localConnectionPool = new ConnectionPool(paramString);
    pools.put(paramString, localConnectionPool);
    return localConnectionPool;
  }

  public static void removePool(String paramString)
  {
    pools.remove(paramString);
  }
}
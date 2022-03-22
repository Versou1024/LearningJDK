package sun.jdbc.odbc.ee;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class ObjectFactory
  implements ObjectFactory
{
  public Object getObjectInstance(Object paramObject, Name paramName, Context paramContext, Hashtable<?, ?> paramHashtable)
    throws Exception
  {
    Object localObject;
    Reference localReference = (Reference)paramObject;
    String str1 = localReference.getClassName();
    String str2 = (String)localReference.get("databaseName").getContent();
    String str3 = (String)localReference.get("dataSourceName").getContent();
    String str4 = (String)localReference.get("user").getContent();
    String str5 = (String)localReference.get("password").getContent();
    String str6 = (String)localReference.get("charSet").getContent();
    int i = Integer.parseInt((String)localReference.get("loginTimeout").getContent());
    if (str1.equals("sun.jdbc.odbc.ee.DataSource"))
    {
      localObject = new DataSource();
      if (str2 != null)
        ((DataSource)localObject).setDatabaseName(str2);
      if (str3 != null)
        ((DataSource)localObject).setDataSourceName(str3);
      if (str4 != null)
        ((DataSource)localObject).setUser(str4);
      if (str5 != null)
        ((DataSource)localObject).setPassword(str5);
      if (str6 != null)
        ((DataSource)localObject).setCharSet(str6);
      ((DataSource)localObject).setLoginTimeout(i);
      return localObject;
    }
    if (str1.equals("sun.jdbc.odbc.ee.ConnectionPoolDataSource"))
    {
      if (str3 == null)
        throw new NamingException("Datasource Name is null for a connection pool");
      localObject = new ConnectionPoolDataSource(str3);
      String str7 = (String)localReference.get("maxStatements").getContent();
      String str8 = (String)localReference.get("initialPoolSize").getContent();
      String str9 = (String)localReference.get("minPoolSize").getContent();
      String str10 = (String)localReference.get("maxPoolSize").getContent();
      String str11 = (String)localReference.get("maxIdleTime").getContent();
      String str12 = (String)localReference.get("propertyCycle").getContent();
      String str13 = (String)localReference.get("timeoutFromPool").getContent();
      String str14 = (String)localReference.get("mInterval").getContent();
      if (str2 != null)
        ((ConnectionPoolDataSource)localObject).setDatabaseName(str2);
      if (str4 != null)
        ((ConnectionPoolDataSource)localObject).setUser(str4);
      if (str5 != null)
        ((ConnectionPoolDataSource)localObject).setPassword(str5);
      if (str6 != null)
        ((ConnectionPoolDataSource)localObject).setCharSet(str6);
      ((ConnectionPoolDataSource)localObject).setLoginTimeout(i);
      ((ConnectionPoolDataSource)localObject).setMaxStatements(str7);
      ((ConnectionPoolDataSource)localObject).setInitialPoolSize(str8);
      ((ConnectionPoolDataSource)localObject).setMinPoolSize(str9);
      ((ConnectionPoolDataSource)localObject).setMaxPoolSize(str10);
      ((ConnectionPoolDataSource)localObject).setMaxIdleTime(str11);
      ((ConnectionPoolDataSource)localObject).setPropertyCycle(str12);
      ((ConnectionPoolDataSource)localObject).setTimeoutFromPool(str13);
      ((ConnectionPoolDataSource)localObject).setMaintenanceInterval(str14);
      return localObject;
    }
    if (str1.equals("sun.jdbc.odbc.ee.XADataSource"));
    return null;
  }
}
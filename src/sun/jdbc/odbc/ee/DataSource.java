package sun.jdbc.odbc.ee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import sun.jdbc.odbc.JdbcOdbcDriver;

public class DataSource extends CommonDataSource
{
  private ConnectionAttributes attrib = null;
  static final long serialVersionUID = -7768089779584724575L;

  public Connection getConnection()
    throws SQLException
  {
    this.attrib = super.getAttributes();
    JdbcOdbcDriver localJdbcOdbcDriver = new JdbcOdbcDriver();
    localJdbcOdbcDriver.setTimeOut(super.getLoginTimeout());
    localJdbcOdbcDriver.setWriter(super.getLogWriter());
    return localJdbcOdbcDriver.connect(this.attrib.getUrl(), this.attrib.getProperties());
  }

  public Connection getConnection(String paramString1, String paramString2)
    throws SQLException
  {
    this.attrib = super.getAttributes();
    JdbcOdbcDriver localJdbcOdbcDriver = new JdbcOdbcDriver();
    localJdbcOdbcDriver.setTimeOut(super.getLoginTimeout());
    localJdbcOdbcDriver.setWriter(super.getLogWriter());
    Properties localProperties = this.attrib.getProperties();
    localProperties.put("user", paramString1);
    localProperties.put("password", paramString2);
    return localJdbcOdbcDriver.connect(this.attrib.getUrl(), localProperties);
  }

  public Reference getReference()
    throws NamingException
  {
    Reference localReference = new Reference(getClass().getName(), "sun.jdbc.odbc.ee.ObjectFactory", null);
    localReference.add(new StringRefAddr("databaseName", super.getDatabaseName()));
    localReference.add(new StringRefAddr("dataSourceName", super.getDataSourceName()));
    localReference.add(new StringRefAddr("user", super.getUser()));
    localReference.add(new StringRefAddr("password", super.getPassword()));
    localReference.add(new StringRefAddr("charSet", super.getCharSet()));
    localReference.add(new StringRefAddr("loginTimeout", "" + super.getLoginTimeout()));
    return localReference;
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
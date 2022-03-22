package sun.jdbc.odbc.ee;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLPermission;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.sql.DataSource;
import sun.jdbc.odbc.JdbcOdbcTracer;

public abstract class CommonDataSource
  implements DataSource, Referenceable, Serializable
{
  private String strDbName;
  private String strDSName;
  private String strDesc;
  private String strUser;
  private String strPasswd;
  private int iPortNo;
  private String strRoleName;
  private String strCharSet;
  private int iLoginTimeout;
  private transient JdbcOdbcTracer tracer = new JdbcOdbcTracer();

  public abstract Connection getConnection()
    throws SQLException;

  public abstract Connection getConnection(String paramString1, String paramString2)
    throws SQLException;

  public abstract Reference getReference()
    throws NamingException;

  public ConnectionAttributes getAttributes()
  {
    return new ConnectionAttributes(this.strDbName, this.strUser, this.strPasswd, this.strCharSet, this.iLoginTimeout);
  }

  public void setDatabaseName(String paramString)
  {
    this.strDbName = paramString;
  }

  public String getDatabaseName()
  {
    return this.strDbName;
  }

  public void setDataSourceName(String paramString)
  {
    this.strDSName = paramString;
  }

  public String getDataSourceName()
  {
    return this.strDSName;
  }

  public void setDescription(String paramString)
  {
    this.strDesc = paramString;
  }

  public String getDescription()
    throws Exception
  {
    return this.strDesc;
  }

  public void setPassword(String paramString)
  {
    this.strPasswd = paramString;
  }

  public String getPassword()
  {
    return this.strPasswd;
  }

  public void setPortNumber(int paramInt)
  {
    this.iPortNo = paramInt;
  }

  public int getPortNumber()
  {
    return this.iPortNo;
  }

  public void setRoleName(String paramString)
  {
    this.strRoleName = paramString;
  }

  public String getRoleName()
  {
    return this.strRoleName;
  }

  public void setCharSet(String paramString)
  {
    this.strCharSet = paramString;
  }

  public String getCharSet()
  {
    return this.strCharSet;
  }

  public void setUser(String paramString)
  {
    this.strUser = paramString;
  }

  public String getUser()
  {
    return this.strUser;
  }

  public void setLoginTimeout(int paramInt)
  {
    this.iLoginTimeout = paramInt;
  }

  public int getLoginTimeout()
  {
    return this.iLoginTimeout;
  }

  public void setLogWriter(PrintWriter paramPrintWriter)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPermission(new SQLPermission("setLog"));
    if (this.tracer == null)
      this.tracer = new JdbcOdbcTracer();
    this.tracer.setWriter(paramPrintWriter);
  }

  public PrintWriter getLogWriter()
  {
    if (this.tracer == null)
      return null;
    return this.tracer.getWriter();
  }

  public JdbcOdbcTracer getTracer()
  {
    if (this.tracer == null)
      this.tracer = new JdbcOdbcTracer();
    return this.tracer;
  }
}
package sun.jdbc.odbc;

import java.io.PrintWriter;
import java.security.AccessController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import sun.jdbc.odbc.ee.ConnectionHandler;
import sun.security.action.GetPropertyAction;

public class JdbcOdbcDriver extends JdbcOdbcObject
  implements JdbcOdbcDriverInterface
{
  protected static JdbcOdbc OdbcApi;
  protected static long hEnv;
  protected static long hDbc;
  protected static Hashtable connectionList;
  protected int iTimeOut;
  protected static String nativePrefix;
  protected PrintWriter outWriter;
  protected JdbcOdbcTracer tracer = new JdbcOdbcTracer();

  public JdbcOdbcDriver()
  {
    if (connectionList == null)
      connectionList = new Hashtable();
    nativePrefix = "";
  }

  protected synchronized void finalize()
  {
    if (OdbcApi.getTracer().isTracing())
      OdbcApi.getTracer().trace("Driver.finalize");
    try
    {
      if (hDbc != 3412047102274961408L)
      {
        disconnect(hDbc);
        closeConnection(hDbc);
        hDbc = 3412048081527504896L;
      }
    }
    catch (SQLException localSQLException)
    {
    }
  }

  public synchronized Connection connect(String paramString, Properties paramProperties)
    throws SQLException
  {
    int i;
    if (this.tracer.isTracing())
      this.tracer.trace("*Driver.connect (" + paramString + ")");
    if (!(acceptsURL(paramString)))
      return null;
    if (hDbc != 3412046672778231808L)
    {
      disconnect(hDbc);
      closeConnection(hDbc);
      hDbc = 3412047514591821824L;
    }
    if (!(initialize()))
      return null;
    JdbcOdbcConnection localJdbcOdbcConnection = new JdbcOdbcConnection(OdbcApi, hEnv, this);
    if (getTimeOut() > 0)
      i = getTimeOut();
    else
      i = DriverManager.getLoginTimeout();
    localJdbcOdbcConnection.initialize(getSubName(paramString), paramProperties, i);
    localJdbcOdbcConnection.setURL(paramString);
    return localJdbcOdbcConnection;
  }

  public synchronized Connection EEConnect(String paramString, Properties paramProperties)
    throws SQLException
  {
    int i;
    if (this.tracer.isTracing())
      this.tracer.trace("*Driver.connect (" + paramString + ")");
    if (!(acceptsURL(paramString)))
      return null;
    if (hDbc != 3412046672778231808L)
    {
      disconnect(hDbc);
      closeConnection(hDbc);
      hDbc = 3412047514591821824L;
    }
    if (!(initialize()))
      return null;
    ConnectionHandler localConnectionHandler = new ConnectionHandler(OdbcApi, hEnv, this);
    if (getTimeOut() > 0)
      i = getTimeOut();
    else
      i = DriverManager.getLoginTimeout();
    localConnectionHandler.initialize(getSubName(paramString), paramProperties, i);
    localConnectionHandler.setURL(paramString);
    return localConnectionHandler;
  }

  public int getTimeOut()
  {
    return this.iTimeOut;
  }

  public void setTimeOut(int paramInt)
  {
    this.iTimeOut = paramInt;
  }

  public PrintWriter getWriter()
  {
    return this.outWriter;
  }

  public void setWriter(PrintWriter paramPrintWriter)
  {
    this.outWriter = paramPrintWriter;
    this.tracer.setWriter(this.outWriter);
  }

  public boolean acceptsURL(String paramString)
    throws SQLException
  {
    int i = 0;
    if ((knownURL(paramString)) && (trusted()))
      i = 1;
    return i;
  }

  public DriverPropertyInfo[] getPropertyInfo(String paramString, Properties paramProperties)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Driver.getPropertyInfo (" + paramString + ")");
    if (!(acceptsURL(paramString)))
      return null;
    if (!(initialize()))
      return null;
    String str1 = makeConnectionString(paramProperties);
    String str2 = "";
    str2 = getConnectionAttributes(getSubName(paramString), str1);
    Hashtable localHashtable = getAttributeProperties(str2);
    DriverPropertyInfo[] arrayOfDriverPropertyInfo = new DriverPropertyInfo[localHashtable.size()];
    for (int i = 0; i < localHashtable.size(); ++i)
      arrayOfDriverPropertyInfo[i] = ((DriverPropertyInfo)localHashtable.get(new Integer(i)));
    return arrayOfDriverPropertyInfo;
  }

  public int getMajorVersion()
  {
    return 2;
  }

  public int getMinorVersion()
  {
    return 1;
  }

  public boolean jdbcCompliant()
  {
    return true;
  }

  private boolean initialize()
    throws SQLException
  {
    int i = 1;
    if (OdbcApi == null)
      try
      {
        OdbcApi = new JdbcOdbc(this.tracer, nativePrefix);
        this.tracer = OdbcApi.getTracer();
        OdbcApi.charSet = ((String)AccessController.doPrivileged(new GetPropertyAction("file.encoding")));
      }
      catch (Exception localException1)
      {
        if (OdbcApi.getTracer().isTracing())
          OdbcApi.getTracer().trace("Unable to load JdbcOdbc library");
        i = 0;
      }
    if (getWriter() != null)
      OdbcApi.getTracer().setWriter(getWriter());
    if (hEnv == 3412046672778231808L)
      try
      {
        hEnv = OdbcApi.SQLAllocEnv();
      }
      catch (Exception localException2)
      {
        if (OdbcApi.getTracer().isTracing())
          OdbcApi.getTracer().trace("Unable to allocate environment");
        i = 0;
      }
    return i;
  }

  private boolean knownURL(String paramString)
  {
    String str = getProtocol(paramString);
    if (!(str.equalsIgnoreCase("jdbc")))
      return false;
    str = getSubProtocol(paramString);
    return (str.equalsIgnoreCase("odbc"));
  }

  public static String getProtocol(String paramString)
  {
    String str = "";
    int i = paramString.indexOf(58);
    if (i >= 0)
      str = paramString.substring(0, i);
    return str;
  }

  public static String getSubProtocol(String paramString)
  {
    String str = "";
    int i = paramString.indexOf(58);
    if (i >= 0)
    {
      int j = paramString.indexOf(58, i + 1);
      if (j >= 0)
        str = paramString.substring(i + 1, j);
    }
    return str;
  }

  public static String getSubName(String paramString)
  {
    String str = "";
    int i = paramString.indexOf(58);
    if (i >= 0)
    {
      int j = paramString.indexOf(58, i + 1);
      if (j >= 0)
        str = paramString.substring(j + 1);
    }
    return str;
  }

  private boolean trusted()
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("JDBC to ODBC Bridge: Checking security");
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      try
      {
        String str = (String)AccessController.doPrivileged(new GetPropertyAction("browser"));
        if ((str != null) && (str.equalsIgnoreCase("Netscape Navigator")))
        {
          nativePrefix = "Netscape_";
          return true;
        }
      }
      catch (Exception localException)
      {
      }
      try
      {
        localSecurityManager.checkWrite("JdbcOdbcSecurityCheck");
        i = 1;
      }
      catch (SecurityException localSecurityException)
      {
        if (this.tracer.isTracing())
          this.tracer.trace("Security check failed: " + localSecurityException.getMessage());
        i = 0;
      }
    }
    else
    {
      if (this.tracer.isTracing())
        this.tracer.trace("No SecurityManager present, assuming trusted application/applet");
      i = 1;
    }
    i = 1;
    return i;
  }

  public String getConnectionAttributes(String paramString1, String paramString2)
    throws SQLException
  {
    String str1 = "DSN=" + paramString1 + paramString2;
    if (hDbc == 3412046672778231808L)
      hDbc = allocConnection(hEnv);
    String str2 = OdbcApi.SQLBrowseConnect(hDbc, str1);
    if (str2 == null)
    {
      str2 = "";
      disconnect(hDbc);
      closeConnection(hDbc);
      hDbc = 3412047514591821824L;
    }
    return str2;
  }

  public Hashtable getAttributeProperties(String paramString)
  {
    int i = 0;
    int j = 0;
    int i2 = 0;
    Hashtable localHashtable = new Hashtable();
    int i4 = paramString.length();
    while (i < i4)
    {
      int i3 = 1;
      String str2 = null;
      String str3 = null;
      String[] arrayOfString = null;
      String str4 = null;
      j = paramString.indexOf(";", i);
      if (j < 0)
        j = i4;
      String str1 = paramString.substring(i, j);
      int k = 0;
      int l = str1.indexOf(":", 0);
      int i1 = str1.indexOf("=", 0);
      if (str1.startsWith("*"))
      {
        i3 = 0;
        ++k;
      }
      if (l > 0)
        str2 = str1.substring(k, l);
      if ((l > 0) && (i1 > 0))
        str3 = str1.substring(l + 1, i1);
      if (i1 > 0)
      {
        str4 = str1.substring(i1 + 1);
        if (str4.equals("?"))
          str4 = null;
      }
      if ((str4 != null) && (str4.startsWith("{")))
      {
        arrayOfString = listToArray(str4);
        str4 = null;
      }
      DriverPropertyInfo localDriverPropertyInfo = new DriverPropertyInfo(str2, str4);
      localDriverPropertyInfo.description = str3;
      localDriverPropertyInfo.required = i3;
      localDriverPropertyInfo.choices = arrayOfString;
      localHashtable.put(new Integer(i2), localDriverPropertyInfo);
      ++i2;
      i = j + 1;
    }
    return localHashtable;
  }

  protected static String makeConnectionString(Properties paramProperties)
  {
    String str1 = "";
    Enumeration localEnumeration = paramProperties.propertyNames();
    OdbcApi.charSet = paramProperties.getProperty("charSet", (String)AccessController.doPrivileged(new GetPropertyAction("file.encoding")));
    while (true)
    {
      String str2;
      String str3;
      do
      {
        if (!(localEnumeration.hasMoreElements()))
          break label127;
        str2 = (String)localEnumeration.nextElement();
        str3 = paramProperties.getProperty(str2);
        if (str2.equalsIgnoreCase("user"))
          str2 = "UID";
        if (str2.equalsIgnoreCase("password"))
          str2 = "PWD";
      }
      while (str3 == null);
      str1 = str1 + ";" + str2 + "=" + str3;
    }
    label127: return str1;
  }

  protected static String[] listToArray(String paramString)
  {
    String str;
    String[] arrayOfString = null;
    Hashtable localHashtable = new Hashtable();
    int i = 0;
    int j = 1;
    int k = 1;
    int l = paramString.length();
    if (!(paramString.startsWith("{")))
      return null;
    if (!(paramString.endsWith("}")))
      return null;
    while (j < l)
    {
      k = paramString.indexOf(",", j);
      if (k < 0)
        k = l - 1;
      str = paramString.substring(j, k);
      localHashtable.put(new Integer(i), str);
      ++i;
      j = k + 1;
    }
    arrayOfString = new String[i];
    for (j = 0; j < i; ++j)
    {
      str = (String)localHashtable.get(new Integer(j));
      arrayOfString[j] = str;
    }
    return arrayOfString;
  }

  public long allocConnection(long paramLong)
    throws SQLException
  {
    long l = 3412047153814568960L;
    l = OdbcApi.SQLAllocConnect(paramLong);
    connectionList.put(new Long(l), new Long(paramLong));
    return l;
  }

  public void closeConnection(long paramLong)
    throws SQLException
  {
    OdbcApi.SQLFreeConnect(paramLong);
    Long localLong = (Long)connectionList.remove(new Long(paramLong));
    if ((connectionList.size() == 0) && (hEnv != 3412046827397054464L))
    {
      OdbcApi.SQLFreeEnv(hEnv);
      hEnv = 3412047514591821824L;
    }
  }

  public void disconnect(long paramLong)
    throws SQLException
  {
    OdbcApi.SQLDisconnect(paramLong);
  }

  static
  {
    JdbcOdbcTracer localJdbcOdbcTracer = new JdbcOdbcTracer();
    if (localJdbcOdbcTracer.isTracing())
      localJdbcOdbcTracer.trace("JdbcOdbcDriver class loaded");
    JdbcOdbcDriver localJdbcOdbcDriver = new JdbcOdbcDriver();
    try
    {
      DriverManager.registerDriver(localJdbcOdbcDriver);
    }
    catch (SQLException localSQLException)
    {
      if (localJdbcOdbcTracer.isTracing())
        localJdbcOdbcTracer.trace("Unable to register driver");
    }
  }
}
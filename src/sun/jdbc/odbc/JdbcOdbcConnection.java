package sun.jdbc.odbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.WeakHashMap;

public class JdbcOdbcConnection extends JdbcOdbcObject
  implements JdbcOdbcConnectionInterface
{
  protected JdbcOdbc OdbcApi;
  protected JdbcOdbcDriverInterface myDriver;
  protected long hEnv;
  protected long hDbc;
  protected SQLWarning lastWarning;
  protected boolean closed;
  protected String URL;
  protected int odbcVer;
  protected Hashtable typeInfo;
  public WeakHashMap statements;
  protected Hashtable batchStatements;
  protected short rsTypeFO;
  protected short rsTypeSI;
  protected short rsTypeSS;
  protected short rsTypeBest;
  protected int rsBlockSize;
  protected int batchInStatements;
  protected int batchInProcedures;
  protected int batchInPrepares;
  private boolean freeStmtsFromConnectionOnly;
  protected JdbcOdbcTracer tracer = new JdbcOdbcTracer();

  public JdbcOdbcConnection(JdbcOdbc paramJdbcOdbc, long paramLong, JdbcOdbcDriverInterface paramJdbcOdbcDriverInterface)
  {
    this.OdbcApi = paramJdbcOdbc;
    this.tracer = this.OdbcApi.getTracer();
    this.myDriver = paramJdbcOdbcDriverInterface;
    this.hEnv = paramLong;
    this.hDbc = 3412046964836007936L;
    this.URL = null;
    this.lastWarning = null;
    this.closed = true;
    this.freeStmtsFromConnectionOnly = false;
  }

  protected void finalize()
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Connection.finalize " + this);
    try
    {
      close();
    }
    catch (SQLException localSQLException)
    {
    }
  }

  public void initialize(String paramString, Properties paramProperties, int paramInt)
    throws SQLException
  {
    Object localObject1 = "";
    Object localObject2 = "";
    Object localObject3 = null;
    Object localObject4 = null;
    Object localObject5 = null;
    Object localObject6 = "";
    if (this.closed)
      this.hDbc = this.myDriver.allocConnection(this.hEnv);
    if (paramInt > 0)
      setLoginTimeout(paramInt);
    String str3 = paramProperties.getProperty("odbcRowSetSize");
    if (str3 != null)
      setResultSetBlockSize(str3);
    this.OdbcApi.charSet = paramProperties.getProperty("charSet", System.getProperty("file.encoding"));
    String str4 = paramProperties.getProperty("licfile", "");
    String str5 = paramProperties.getProperty("licpwd", "");
    String str1 = paramProperties.getProperty("user", "");
    String str2 = paramProperties.getProperty("password", "");
    String str6 = null;
    if ((paramString.indexOf("DRIVER") != -1) || (paramString.indexOf("Driver") != -1) || (paramString.indexOf("driver") != -1))
      str6 = paramString;
    else
      str6 = "DSN=" + paramString;
    StringTokenizer localStringTokenizer = new StringTokenizer(str6, ";", false);
    if (localStringTokenizer.countTokens() > 1)
    {
      int i = 0;
      while (localStringTokenizer.hasMoreTokens())
      {
        ++i;
        String str10 = localStringTokenizer.nextToken();
        if (str10.startsWith("user"))
          localObject3 = str10;
        else if (str10.startsWith("password"))
          localObject4 = str10;
        else if (str10.startsWith("odbcRowSetSize"))
          localObject5 = str10;
        else if (i > 1)
          localObject2 = ((String)localObject2) + ";" + str10;
        else
          localObject2 = ((String)localObject2) + str10;
      }
    }
    else
    {
      localObject2 = str6;
    }
    localObject1 = localObject2;
    try
    {
      int l;
      StringBuffer localStringBuffer3;
      if ((str1.equals("")) && (localObject3 != null))
      {
        localObject6 = localObject3;
        String str7 = localObject3.substring(4);
        if (!(str7.equals("")))
          if (((String)localObject1).indexOf("UID=") == -1)
          {
            localObject1 = ((String)localObject1) + ";UID" + str7;
          }
          else
          {
            l = ((String)localObject1).indexOf("UID=");
            int i1 = ((String)localObject1).indexOf(";", l);
            localStringBuffer3 = new StringBuffer((String)localObject1);
            localStringBuffer3.replace(l, i1, "UID=" + str7);
            localObject1 = localStringBuffer3.toString();
          }
      }
      else if (!(str1.equals("")))
      {
        if (((String)localObject1).indexOf("UID=") == -1)
        {
          localObject1 = ((String)localObject1) + ";UID=" + str1;
        }
        else
        {
          int j = ((String)localObject1).indexOf("UID=");
          l = ((String)localObject1).indexOf(";", j);
          StringBuffer localStringBuffer1 = new StringBuffer((String)localObject1);
          localStringBuffer1.replace(j, l, "UID=" + str1);
          localObject1 = localStringBuffer1.toString();
        }
      }
      if ((str2.equals("")) && (localObject4 != null))
      {
        localObject6 = localObject4;
        String str8 = localObject4.substring(8);
        if (((String)localObject1).indexOf("UID=") != -1)
          if (((String)localObject1).indexOf("PWD=") == -1)
          {
            localObject1 = ((String)localObject1) + ";PWD" + str8;
          }
          else
          {
            l = ((String)localObject1).indexOf("PWD=");
            int i2 = ((String)localObject1).indexOf(";", l);
            localStringBuffer3 = new StringBuffer((String)localObject1);
            localStringBuffer3.replace(l, i2, "PWD=" + str8);
            localObject1 = localStringBuffer3.toString();
          }
      }
      else if ((!(str2.equals(""))) && (((String)localObject1).indexOf("UID=") != -1))
      {
        if (((String)localObject1).indexOf("PWD=") == -1)
        {
          localObject1 = ((String)localObject1) + ";PWD=" + str2;
        }
        else
        {
          int k = ((String)localObject1).indexOf("PWD=");
          l = ((String)localObject1).indexOf(";", k);
          StringBuffer localStringBuffer2 = new StringBuffer((String)localObject1);
          localStringBuffer2.replace(k, l, "PWD=" + str2);
          localObject1 = localStringBuffer2.toString();
        }
      }
      if ((str3 == null) && (localObject5 != null))
      {
        localObject6 = localObject5;
        String str9 = localObject5.substring(15);
        setResultSetBlockSize(str9);
      }
    }
    catch (StringIndexOutOfBoundsException localStringIndexOutOfBoundsException)
    {
      throw new SQLException("invalid property values [" + ((String)localObject6) + "]");
    }
    try
    {
      this.OdbcApi.SQLDriverConnect(this.hDbc, (String)localObject1);
    }
    catch (SQLWarning localSQLWarning)
    {
      this.lastWarning = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.myDriver.closeConnection(this.hDbc);
      throw localSQLException;
    }
    this.closed = false;
    if ((str4 == null) || (str5 != null));
    this.statements = new WeakHashMap();
    this.batchStatements = new Hashtable();
    DatabaseMetaData localDatabaseMetaData = getMetaData();
    this.OdbcApi.odbcDriverName = localDatabaseMetaData.getDriverName() + " " + localDatabaseMetaData.getDriverVersion();
    if (this.tracer.isTracing())
    {
      this.tracer.trace("Driver name:    " + localDatabaseMetaData.getDriverName());
      this.tracer.trace("Driver version: " + localDatabaseMetaData.getDriverVersion());
    }
    else
    {
      localDatabaseMetaData = null;
    }
    buildTypeInfo();
    checkScrollCursorSupport();
    checkBatchUpdateSupport();
  }

  public Statement createStatement()
    throws SQLException
  {
    return createStatement(1003, 1007);
  }

  public Statement createStatement(int paramInt1, int paramInt2)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.createStatement");
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    JdbcOdbcStatement localJdbcOdbcStatement = new JdbcOdbcStatement(this);
    localJdbcOdbcStatement.initialize(this.OdbcApi, this.hDbc, l, null, paramInt1, paramInt2);
    localJdbcOdbcStatement.setBlockCursorSize(this.rsBlockSize);
    registerStatement(localJdbcOdbcStatement);
    return localJdbcOdbcStatement;
  }

  public PreparedStatement prepareStatement(String paramString)
    throws SQLException
  {
    return prepareStatement(paramString, 1003, 1007);
  }

  public PreparedStatement prepareStatement(String paramString, int paramInt1, int paramInt2)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.prepareStatement (" + paramString + ")");
    JdbcOdbcPreparedStatement localJdbcOdbcPreparedStatement = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    localJdbcOdbcPreparedStatement = new JdbcOdbcPreparedStatement(this);
    localJdbcOdbcPreparedStatement.initialize(this.OdbcApi, this.hDbc, l, this.typeInfo, paramInt1, paramInt2);
    try
    {
      this.OdbcApi.SQLPrepare(l, paramString);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      localJdbcOdbcPreparedStatement.close();
      throw localSQLException;
    }
    localJdbcOdbcPreparedStatement.initBoundParam();
    localJdbcOdbcPreparedStatement.setWarning(localObject);
    localJdbcOdbcPreparedStatement.setBlockCursorSize(this.rsBlockSize);
    localJdbcOdbcPreparedStatement.setSql(paramString);
    registerStatement(localJdbcOdbcPreparedStatement);
    return localJdbcOdbcPreparedStatement;
  }

  public CallableStatement prepareCall(String paramString)
    throws SQLException
  {
    return prepareCall(paramString, 1003, 1007);
  }

  public CallableStatement prepareCall(String paramString, int paramInt1, int paramInt2)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.prepareCall (" + paramString + ")");
    JdbcOdbcCallableStatement localJdbcOdbcCallableStatement = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    localJdbcOdbcCallableStatement = new JdbcOdbcCallableStatement(this);
    localJdbcOdbcCallableStatement.initialize(this.OdbcApi, this.hDbc, l, this.typeInfo, paramInt1, paramInt2);
    try
    {
      this.OdbcApi.SQLPrepare(l, paramString);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      localJdbcOdbcCallableStatement.close();
      throw localSQLException;
    }
    localJdbcOdbcCallableStatement.initBoundParam();
    localJdbcOdbcCallableStatement.setWarning(localObject);
    localJdbcOdbcCallableStatement.setBlockCursorSize(this.rsBlockSize);
    localJdbcOdbcCallableStatement.setSql(paramString);
    registerStatement(localJdbcOdbcCallableStatement);
    return localJdbcOdbcCallableStatement;
  }

  public String nativeSQL(String paramString)
    throws SQLException
  {
    String str;
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.nativeSQL (" + paramString + ")");
    try
    {
      str = this.OdbcApi.SQLNativeSql(this.hDbc, paramString);
    }
    catch (SQLException localSQLException)
    {
      str = paramString;
    }
    return str;
  }

  public void setAutoCommit(boolean paramBoolean)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.setAutoCommit (" + paramBoolean + ")");
    int i = 1;
    validateConnection();
    if (!(paramBoolean))
      i = 0;
    this.OdbcApi.SQLSetConnectOption(this.hDbc, 102, i);
  }

  public boolean getAutoCommit()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.getAutoCommit");
    int j = 0;
    validateConnection();
    int i = (int)this.OdbcApi.SQLGetConnectOption(this.hDbc, 102);
    if (i == 1)
      j = 1;
    return j;
  }

  public void commit()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.commit");
    validateConnection();
    this.OdbcApi.SQLTransact(this.hEnv, this.hDbc, 0);
  }

  public void rollback()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.rollback");
    validateConnection();
    this.OdbcApi.SQLTransact(this.hEnv, this.hDbc, 1);
  }

  public void close()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.close");
    setFreeStmtsFromConnectionOnly();
    closeAllStatements();
    if (!(this.closed))
    {
      this.myDriver.disconnect(this.hDbc);
      this.myDriver.closeConnection(this.hDbc);
    }
    this.closed = true;
    this.URL = null;
  }

  public boolean isFreeStmtsFromConnectionOnly()
  {
    return this.freeStmtsFromConnectionOnly;
  }

  public void setFreeStmtsFromConnectionOnly()
  {
    this.freeStmtsFromConnectionOnly = true;
  }

  public void setFreeStmtsFromAnyWhere()
  {
    this.freeStmtsFromConnectionOnly = false;
  }

  public boolean isClosed()
    throws SQLException
  {
    return this.closed;
  }

  public DatabaseMetaData getMetaData()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.getMetaData");
    validateConnection();
    JdbcOdbcDatabaseMetaData localJdbcOdbcDatabaseMetaData = new JdbcOdbcDatabaseMetaData(this.OdbcApi, this);
    return localJdbcOdbcDatabaseMetaData;
  }

  public void setReadOnly(boolean paramBoolean)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.setReadOnly (" + paramBoolean + ")");
    int i = 0;
    validateConnection();
    if (paramBoolean)
      i = 1;
    try
    {
      this.OdbcApi.SQLSetConnectOption(this.hDbc, 101, i);
    }
    catch (SQLException localSQLException)
    {
      if (this.tracer.isTracing())
        this.tracer.trace("setReadOnly exception ignored");
    }
  }

  public boolean isReadOnly()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.isReadOnly");
    int j = 0;
    validateConnection();
    int i = (int)this.OdbcApi.SQLGetConnectOption(this.hDbc, 101);
    if (i == 1)
      j = 1;
    return j;
  }

  public void setCatalog(String paramString)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.setCatalog (" + paramString + ")");
    validateConnection();
    this.OdbcApi.SQLSetConnectOption(this.hDbc, 109, paramString);
  }

  public String getCatalog()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.getCatalog");
    validateConnection();
    return this.OdbcApi.SQLGetInfoString(this.hDbc, 16);
  }

  public void setTransactionIsolation(int paramInt)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.setTransactionIsolation (" + paramInt + ")");
    validateConnection();
    switch (paramInt)
    {
    case 0:
      setAutoCommit(true);
      break;
    case 1:
      setAutoCommit(false);
      this.OdbcApi.SQLSetConnectOption(this.hDbc, 108, 1);
      break;
    case 2:
      setAutoCommit(false);
      this.OdbcApi.SQLSetConnectOption(this.hDbc, 108, 2);
      break;
    case 4:
      setAutoCommit(false);
      this.OdbcApi.SQLSetConnectOption(this.hDbc, 108, 4);
      break;
    case 8:
      setAutoCommit(false);
      this.OdbcApi.SQLSetConnectOption(this.hDbc, 108, 8);
      break;
    case 3:
    case 5:
    case 6:
    case 7:
    default:
      setAutoCommit(false);
      this.OdbcApi.SQLSetConnectOption(this.hDbc, 108, paramInt);
    }
  }

  public void setLicenseFile(String paramString)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.setLicenseFile (" + paramString + ")");
    this.OdbcApi.SQLSetConnectOption(this.hDbc, 1041, paramString);
  }

  public void setLicensePassword(String paramString)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.setPassword (" + paramString + ")");
    this.OdbcApi.SQLSetConnectOption(this.hDbc, 1042, paramString);
  }

  public int getTransactionIsolation()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.getTransactionIsolation");
    int i = 0;
    validateConnection();
    int j = (int)this.OdbcApi.SQLGetConnectOption(this.hDbc, 108);
    switch (j)
    {
    case 1:
      i = 1;
      break;
    case 2:
      i = 2;
      break;
    case 4:
      i = 4;
      break;
    case 8:
      i = 8;
      break;
    case 3:
    case 5:
    case 6:
    case 7:
    default:
      i = j;
    }
    return i;
  }

  public SQLWarning getWarnings()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("*Connection.getWarnings");
    return this.lastWarning;
  }

  public void clearWarnings()
    throws SQLException
  {
    this.lastWarning = null;
  }

  public void validateConnection()
    throws SQLException
  {
    if (this.closed)
      throw new SQLException("Connection is closed");
  }

  public long getHDBC()
  {
    return this.hDbc;
  }

  public void setURL(String paramString)
  {
    this.URL = paramString;
  }

  public String getURL()
  {
    return this.URL;
  }

  protected void setLoginTimeout(int paramInt)
    throws SQLException
  {
    this.OdbcApi.SQLSetConnectOption(this.hDbc, 103, paramInt);
  }

  public int getODBCVer()
  {
    if (this.odbcVer == 0)
    {
      String str;
      try
      {
        str = this.OdbcApi.SQLGetInfoString(this.hDbc, 10);
      }
      catch (SQLException localSQLException)
      {
        str = "-1";
      }
      Integer localInteger = new Integer(str.substring(0, 2));
      this.odbcVer = localInteger.intValue();
    }
    return this.odbcVer;
  }

  protected void checkBatchUpdateSupport()
  {
    this.batchInStatements = -1;
    this.batchInProcedures = -1;
    this.batchInPrepares = -1;
    int i = -1;
    int j = -1;
    int k = 0;
    int l = 0;
    int i1 = 0;
    try
    {
      i = this.OdbcApi.SQLGetInfo(this.hDbc, 121);
      if ((i & 0x2) > 0)
        k = 1;
      if ((i & 0x8) > 0)
        l = 1;
      j = this.OdbcApi.SQLGetInfo(this.hDbc, 120);
      if ((j & 0x4) > 0)
      {
        this.batchInStatements = 4;
        this.batchInProcedures = 4;
      }
      else
      {
        if ((k != 0) && ((j & 0x2) > 0))
          this.batchInStatements = 2;
        if ((l != 0) && ((j & 0x1) > 0))
          this.batchInProcedures = 1;
      }
      i = this.OdbcApi.SQLGetInfo(this.hDbc, 153);
      if ((i & 0x1) > 0)
      {
        i1 = 1;
        this.batchInPrepares = 1;
      }
    }
    catch (SQLException localSQLException)
    {
      this.batchInStatements = -1;
      this.batchInProcedures = -1;
      this.batchInPrepares = -1;
    }
  }

  public int getBatchRowCountFlag(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return this.batchInStatements;
    case 2:
      return this.batchInPrepares;
    case 3:
      return this.batchInProcedures;
    }
    return -1;
  }

  public void checkScrollCursorSupport()
    throws SQLException
  {
    short s = -1;
    int i = 0;
    int j = this.OdbcApi.SQLGetInfo(this.hDbc, 44);
    this.rsTypeFO = -1;
    this.rsTypeSI = -1;
    this.rsTypeSS = -1;
    if ((j & 0x1) != 0)
      this.rsTypeFO = 0;
    if ((j & 0x10) != 0)
      this.rsTypeSI = 3;
    if ((j & 0x10) != 0)
    {
      s = 3;
      i = getOdbcCursorAttr2(s);
      if ((i & 0x40) != 0)
        this.rsTypeSS = s;
    }
    if (((j & 0x2) != 0) || ((j & 0x8) != 0))
    {
      s = 1;
      i = getOdbcCursorAttr2(s);
      if ((i & 0x40) != 0)
        this.rsTypeSS = s;
      else
        this.rsTypeSI = s;
    }
    if ((j & 0x4) != 0)
    {
      s = 2;
      i = getOdbcCursorAttr2(s);
      if ((i & 0x40) != 0)
        this.rsTypeSS = s;
    }
    this.rsTypeBest = s;
    if (this.rsTypeBest == -1)
      this.rsTypeBest = this.rsTypeSS;
    if (this.rsTypeBest == -1)
      this.rsTypeBest = this.rsTypeSI;
    if (this.rsTypeBest == -1)
      this.rsTypeBest = this.rsTypeFO;
  }

  public short getBestOdbcCursorType()
  {
    return this.rsTypeBest;
  }

  public short getOdbcCursorType(int paramInt)
  {
    int i = -1;
    switch (paramInt)
    {
    case 1003:
      i = this.rsTypeFO;
      break;
    case 1004:
      i = this.rsTypeSI;
      break;
    case 1005:
      i = this.rsTypeSS;
    }
    return i;
  }

  public short getOdbcConcurrency(int paramInt)
  {
    switch (paramInt)
    {
    case 1007:
      return 1;
    case 1008:
      return 2;
    }
    return 1;
  }

  public int getOdbcCursorAttr2(short paramShort)
    throws SQLException
  {
    short s = 0;
    switch (paramShort)
    {
    case 0:
      s = 147;
      break;
    case 3:
      s = 168;
      break;
    case 1:
      s = 151;
      break;
    case 2:
      s = 145;
    }
    try
    {
      return this.OdbcApi.SQLGetInfo(this.hDbc, s);
    }
    catch (SQLException localSQLException)
    {
    }
    return 0;
  }

  public Map<String, Class<?>> getTypeMap()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setTypeMap(Map<String, Class<?>> paramMap)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  protected void buildTypeInfo()
    throws SQLException
  {
    this.typeInfo = new Hashtable();
    if (this.tracer.isTracing())
      this.tracer.trace("Caching SQL type information");
    ResultSet localResultSet = getMetaData().getTypeInfo();
    for (boolean bool = localResultSet.next(); bool; bool = localResultSet.next())
    {
      String str = localResultSet.getString(1);
      int i = localResultSet.getInt(2);
      if (this.typeInfo.get(new Integer(i)) == null)
      {
        JdbcOdbcTypeInfo localJdbcOdbcTypeInfo = new JdbcOdbcTypeInfo();
        localJdbcOdbcTypeInfo.setName(str);
        localJdbcOdbcTypeInfo.setPrec(localResultSet.getInt(3));
        this.typeInfo.put(new Integer(i), localJdbcOdbcTypeInfo);
      }
    }
    localResultSet.close();
  }

  protected void registerStatement(Statement paramStatement)
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Registering Statement " + paramStatement);
    this.statements.put(paramStatement, "");
  }

  public void deregisterStatement(Statement paramStatement)
  {
    if (this.statements.get(paramStatement) != null)
    {
      if (this.tracer.isTracing())
        this.tracer.trace("deregistering Statement " + paramStatement);
      this.statements.remove(paramStatement);
    }
  }

  public synchronized void closeAllStatements()
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("" + this.statements.size() + " Statement(s) to close");
    if (this.statements.size() == 0)
      return;
    Set localSet = this.statements.keySet();
    Iterator localIterator = localSet.iterator();
    if (localIterator.hasNext());
    try
    {
      Statement localStatement = (Statement)localIterator.next();
      localStatement.close();
    }
    catch (Exception localException)
    {
      while (true)
      {
        localSet = this.statements.keySet();
        localIterator = localSet.iterator();
      }
      this.batchStatements = null;
    }
  }

  public synchronized void setBatchVector(Vector paramVector, Statement paramStatement)
  {
    int i = -1;
    if (this.tracer.isTracing())
      this.tracer.trace("setBatchVector " + paramStatement);
    this.batchStatements.put(paramStatement, paramVector);
  }

  public Vector getBatchVector(Statement paramStatement)
  {
    if (this.tracer.isTracing())
      this.tracer.trace("getBatchVector " + paramStatement);
    return ((Vector)this.batchStatements.get(paramStatement));
  }

  public synchronized void removeBatchVector(Statement paramStatement)
  {
    if (this.tracer.isTracing())
      this.tracer.trace("removeBatchVector " + paramStatement);
    this.batchStatements.remove(paramStatement);
  }

  protected void setResultSetBlockSize(String paramString)
    throws SQLException
  {
    this.rsBlockSize = 10;
    if (paramString != null)
    {
      paramString.trim();
      if (!(paramString.equals("")))
        try
        {
          int i = new Integer(paramString).intValue();
          if (i > 0)
            this.rsBlockSize = i;
        }
        catch (NumberFormatException localNumberFormatException)
        {
          throw new SQLException("invalid property value: [odbcRowSetSize=" + paramString + "]");
        }
    }
  }

  public void setHoldability(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int getHoldability()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Savepoint setSavepoint()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Savepoint setSavepoint(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void rollback(Savepoint paramSavepoint)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Statement createStatement(int paramInt1, int paramInt2, int paramInt3)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public PreparedStatement prepareStatement(String paramString, int paramInt1, int paramInt2, int paramInt3)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void releaseSavepoint(Savepoint paramSavepoint)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public CallableStatement prepareCall(String paramString, int paramInt1, int paramInt2, int paramInt3)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public PreparedStatement prepareStatement(String paramString, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public PreparedStatement prepareStatement(String paramString, int[] paramArrayOfInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public PreparedStatement prepareStatement(String paramString, String[] paramArrayOfString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Clob createClob()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Blob createBlob()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public NClob createNClob()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public SQLXML createSQLXML()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean isValid(int paramInt)
  {
    throw new UnsupportedOperationException();
  }

  public void setClientInfo(String paramString1, String paramString2)
    throws SQLClientInfoException
  {
    throw new UnsupportedOperationException();
  }

  public void setClientInfo(Properties paramProperties)
    throws SQLClientInfoException
  {
    throw new UnsupportedOperationException();
  }

  public String getClientInfo(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Properties getClientInfo()
    throws SQLException
  {
    throw new UnsupportedOperationException();
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

  public Struct createStruct(String paramString, Object[] paramArrayOfObject)
    throws SQLException
  {
    return null;
  }

  public Array createArrayOf(String paramString, Object[] paramArrayOfObject)
    throws SQLException
  {
    return null;
  }
}
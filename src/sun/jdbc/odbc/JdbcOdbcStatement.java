package sun.jdbc.odbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class JdbcOdbcStatement extends JdbcOdbcObject
  implements Statement
{
  protected JdbcOdbc OdbcApi = null;
  protected long hDbc = 3412045659165949952L;
  protected long hStmt = 3412045659165949952L;
  protected SQLWarning lastWarning = null;
  protected Hashtable typeInfo;
  protected ResultSet myResultSet;
  protected JdbcOdbcConnectionInterface myConnection;
  protected int rsType;
  protected int rsConcurrency;
  protected int fetchDirection;
  protected int fetchSize;
  protected Vector batchSqlVec;
  protected boolean batchSupport;
  protected int batchRCFlag;
  protected String mySql;
  protected boolean batchOn;
  protected int rsBlockSize;
  protected int moreResults;
  protected boolean closeCalledFromFinalize;

  public JdbcOdbcStatement(JdbcOdbcConnectionInterface paramJdbcOdbcConnectionInterface)
  {
    this.myConnection = paramJdbcOdbcConnectionInterface;
    this.rsType = 1003;
    this.rsConcurrency = 1007;
    this.fetchDirection = 1000;
    this.fetchSize = 1;
    this.batchRCFlag = -1;
    this.batchSupport = false;
    this.moreResults = 1;
  }

  protected void finalize()
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("Statement.finalize " + this);
    try
    {
      this.closeCalledFromFinalize = true;
      close();
    }
    catch (SQLException localSQLException)
    {
    }
  }

  public void initialize(JdbcOdbc paramJdbcOdbc, long paramLong1, long paramLong2, Hashtable paramHashtable, int paramInt1, int paramInt2)
    throws SQLException
  {
    this.OdbcApi = paramJdbcOdbc;
    this.hDbc = paramLong1;
    this.hStmt = paramLong2;
    this.rsType = paramInt1;
    this.rsConcurrency = paramInt2;
    this.typeInfo = paramHashtable;
    this.batchRCFlag = this.myConnection.getBatchRowCountFlag(1);
    if ((this.batchRCFlag > 0) && (this.batchRCFlag == 2))
      this.batchSupport = true;
    else
      this.batchSupport = false;
    if ((this.rsType == 1003) || (this.rsType == 1004) || (this.rsType == 1005))
    {
      if (this.rsConcurrency == 1007)
        break label157;
      if (this.rsConcurrency == 1008)
        break label157:
      close();
      throw new SQLException("Invalid Concurrency Type.");
    }
    close();
    throw new SQLException("Invalid Cursor Type.");
    label157: int i = this.myConnection.getOdbcCursorType(this.rsType);
    if (i == -1)
    {
      i = this.myConnection.getBestOdbcCursorType();
      if (i == -1)
        throw new SQLException("The result set type is not supported.");
      setWarning(new SQLWarning("The result set type has been downgraded and changed."));
      switch (i)
      {
      case 0:
        this.rsType = 1003;
        break;
      case 1:
      case 3:
        this.rsType = 1004;
      case 2:
      }
    }
    if (this.rsConcurrency == 1008)
      i = 2;
    try
    {
      this.OdbcApi.SQLSetStmtOption(this.hStmt, 6, i);
    }
    catch (SQLWarning localSQLWarning1)
    {
      setWarning(localSQLWarning1);
    }
    catch (SQLException localSQLException1)
    {
      if (i != 0)
      {
        localSQLException1.fillInStackTrace();
        throw localSQLException1;
      }
    }
    int j = this.myConnection.getOdbcConcurrency(this.rsConcurrency);
    try
    {
      this.OdbcApi.SQLSetStmtOption(this.hStmt, 7, j);
    }
    catch (SQLWarning localSQLWarning2)
    {
      setWarning(localSQLWarning2);
    }
    catch (SQLException localSQLException2)
    {
      if (j != 1)
      {
        localSQLException2.fillInStackTrace();
        throw localSQLException2;
      }
    }
  }

  public ResultSet executeQuery(String paramString)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.executeQuery (" + paramString + ")");
    ResultSet localResultSet = null;
    if (execute(paramString))
      localResultSet = getResultSet(false);
    else
      throw new SQLException("No ResultSet was produced");
    if (this.batchOn)
      clearBatch();
    return localResultSet;
  }

  public int executeUpdate(String paramString)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.executeUpdate (" + paramString + ")");
    int i = -1;
    if (!(execute(paramString)))
      i = getUpdateCount();
    else
      throw new SQLException("No row count was produced");
    if (this.batchOn)
      clearBatch();
    return i;
  }

  public synchronized boolean execute(String paramString)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.execute (" + paramString + ")");
    int i = 0;
    Object localObject = null;
    setSql(paramString);
    reset();
    lockIfNecessary(paramString);
    try
    {
      this.OdbcApi.SQLExecDirect(this.hStmt, paramString);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    if (getColumnCount() > 0)
      i = 1;
    if (this.batchOn)
      clearBatch();
    return i;
  }

  public ResultSet getResultSet()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.getResultSet");
    if (this.myResultSet != null)
      return this.myResultSet;
    this.myResultSet = getResultSet(true);
    return this.myResultSet;
  }

  public synchronized ResultSet getResultSet(boolean paramBoolean)
    throws SQLException
  {
    if (this.myResultSet != null)
      throw new SQLException("Invalid state for getResultSet");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    int i = 1;
    if (paramBoolean)
      i = getColumnCount();
    if (i > 0)
    {
      if (this.rsType != 1003)
        checkCursorDowngrade();
      localJdbcOdbcResultSet = new JdbcOdbcResultSet();
      localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, this.hStmt, true, this);
      this.myResultSet = localJdbcOdbcResultSet;
    }
    else
    {
      clearMyResultSet();
    }
    return localJdbcOdbcResultSet;
  }

  public int getUpdateCount()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.getUpdateCount");
    int i = -1;
    if (this.moreResults == 3)
      return i;
    if (getColumnCount() == 0)
      i = getRowCount();
    return i;
  }

  public synchronized void close()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.close");
    clearMyResultSet();
    try
    {
      clearWarnings();
      if (this.hStmt != 3412047239713914880L)
      {
        if (this.closeCalledFromFinalize == true)
          if (!(this.myConnection.isFreeStmtsFromConnectionOnly()))
            this.OdbcApi.SQLFreeStmt(this.hStmt, 1);
        else
          this.OdbcApi.SQLFreeStmt(this.hStmt, 1);
        this.hStmt = 3412048029987897344L;
      }
    }
    catch (SQLException localSQLException)
    {
      localSQLException.printStackTrace();
    }
    this.myConnection.deregisterStatement(this);
  }

  protected void reset()
    throws SQLException
  {
    clearWarnings();
    clearMyResultSet();
    if (this.hStmt != 3412046810217185280L)
      this.OdbcApi.SQLFreeStmt(this.hStmt, 0);
  }

  public boolean getMoreResults()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.getMoreResults");
    Object localObject = null;
    int i = 0;
    if (this.moreResults == 1)
      this.moreResults = 3;
    clearWarnings();
    try
    {
      if (this.OdbcApi.SQLMoreResults(this.hStmt))
        this.moreResults = 2;
      else
        this.moreResults = 3;
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    if ((this.moreResults == 2) && (getColumnCount() != 0))
      i = 1;
    setWarning(localObject);
    return i;
  }

  public int getMaxFieldSize()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.getMaxFieldSize");
    return getStmtOption(3);
  }

  public void setMaxFieldSize(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.setMaxFieldSize (" + paramInt + ")");
    this.OdbcApi.SQLSetStmtOption(this.hStmt, 3, paramInt);
  }

  public int getMaxRows()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.getMaxRows");
    return getStmtOption(1);
  }

  public void setMaxRows(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.setMaxRows (" + paramInt + ")");
    if (paramInt < 0)
      throw new SQLException("Invalid new max row limit");
    this.OdbcApi.SQLSetStmtOption(this.hStmt, 1, paramInt);
  }

  public void setEscapeProcessing(boolean paramBoolean)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.setEscapeProcessing (" + paramBoolean + ")");
    int i = 0;
    if (!(paramBoolean))
      i = 1;
    this.OdbcApi.SQLSetStmtOption(this.hStmt, 2, i);
  }

  public int getQueryTimeout()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.getQueryTimeout");
    return getStmtOption(0);
  }

  public void setQueryTimeout(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.setQueryTimeout (" + paramInt + ")");
    this.OdbcApi.SQLSetStmtOption(this.hStmt, 0, paramInt);
  }

  public void cancel()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.cancel");
    clearWarnings();
    try
    {
      this.OdbcApi.SQLCancel(this.hStmt);
    }
    catch (SQLWarning localSQLWarning)
    {
      setWarning(localSQLWarning);
    }
  }

  public SQLWarning getWarnings()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.getWarnings");
    return this.lastWarning;
  }

  public void clearWarnings()
    throws SQLException
  {
    this.lastWarning = null;
  }

  public void setWarning(SQLWarning paramSQLWarning)
  {
    this.lastWarning = paramSQLWarning;
  }

  public void setCursorName(String paramString)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.setCursorName " + paramString + ")");
    this.OdbcApi.SQLSetCursorName(this.hStmt, paramString);
  }

  public void setFetchDirection(int paramInt)
    throws SQLException
  {
    if ((paramInt == 1000) || (paramInt == 1001) || (paramInt == 1002))
      this.fetchDirection = paramInt;
    else
      throw new SQLException("Invalid fetch direction");
  }

  public int getFetchDirection()
    throws SQLException
  {
    return this.fetchDirection;
  }

  public void setFetchSize(int paramInt)
    throws SQLException
  {
    if ((0 <= paramInt) && (paramInt <= getMaxRows()))
      this.fetchSize = paramInt;
    else
      throw new SQLException("Invalid Fetch Size");
  }

  public int getFetchSize()
    throws SQLException
  {
    return this.fetchSize;
  }

  public int getResultSetConcurrency()
    throws SQLException
  {
    return this.rsConcurrency;
  }

  public int getResultSetType()
    throws SQLException
  {
    return this.rsType;
  }

  public void addBatch(String paramString)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.addBatch (" + paramString + ")");
    try
    {
      if (paramString != null)
      {
        this.batchSqlVec = this.myConnection.getBatchVector(this);
        if (this.batchSqlVec == null)
          this.batchSqlVec = new Vector(5, 10);
        this.batchSqlVec.addElement(paramString);
        this.myConnection.setBatchVector(this.batchSqlVec, this);
        this.batchOn = true;
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }

  public void clearBatch()
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.clearBatch");
    try
    {
      if (this.batchSqlVec != null)
      {
        this.myConnection.removeBatchVector(this);
        this.batchSqlVec = null;
        this.batchOn = false;
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }

  public int[] executeBatch()
    throws BatchUpdateException
  {
    return executeBatchUpdate();
  }

  protected int[] executeBatchUpdate()
    throws BatchUpdateException
  {
    int[] arrayOfInt1 = new int[0];
    int[] arrayOfInt2 = null;
    int i = 0;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*Statement.executeBatch");
    if (!(this.batchSupport))
      return emulateBatchUpdate();
    this.batchSqlVec = this.myConnection.getBatchVector(this);
    if (this.batchSqlVec != null)
    {
      Enumeration localEnumeration = this.batchSqlVec.elements();
      arrayOfInt1 = new int[this.batchSqlVec.size()];
      int j = arrayOfInt1.length;
      StringBuffer localStringBuffer = new StringBuffer();
      for (int k = 0; k < j; ++k)
        if (localEnumeration.hasMoreElements())
        {
          String str = (String)localEnumeration.nextElement();
          localStringBuffer.append(str + "\n");
        }
      try
      {
        if (!(execute(localStringBuffer.toString())))
        {
          while (true)
          {
            k = getUpdateCount();
            if (k == -1)
              break;
            arrayOfInt1[(i++)] = k;
            getMoreResults();
          }
          if (i >= j)
            break label268;
          arrayOfInt2 = new int[i];
          for (k = 0; k < i; ++k)
            arrayOfInt2[k] = arrayOfInt1[k];
          clearBatch();
          throw new JdbcOdbcBatchUpdateException("SQL Attempt to produce a ResultSet from executeBatch", arrayOfInt2);
        }
        clearBatch();
        label268: throw new JdbcOdbcBatchUpdateException("SQL Attempt to produce a ResultSet from executeBatch", null);
      }
      catch (SQLException localSQLException)
      {
        clearBatch();
        throw new JdbcOdbcBatchUpdateException(localSQLException.getMessage(), localSQLException.getSQLState(), arrayOfInt2);
      }
    }
    clearBatch();
    return arrayOfInt1;
  }

  protected int[] emulateBatchUpdate()
    throws BatchUpdateException
  {
    int[] arrayOfInt1 = new int[0];
    this.batchSqlVec = this.myConnection.getBatchVector(this);
    if (this.batchSqlVec != null)
    {
      int[] arrayOfInt2 = new int[0];
      int i = 0;
      Enumeration localEnumeration = this.batchSqlVec.elements();
      arrayOfInt1 = new int[this.batchSqlVec.size()];
      for (int j = 0; j < arrayOfInt1.length; ++j)
        if (localEnumeration.hasMoreElements())
        {
          String str = (String)localEnumeration.nextElement();
          try
          {
            if (!(execute(str)))
            {
              arrayOfInt1[j] = getUpdateCount();
              ++i;
            }
            else
            {
              arrayOfInt2 = new int[i];
              for (int k = 0; k <= j - 1; ++k)
                arrayOfInt2[k] = arrayOfInt1[k];
              clearBatch();
              throw new JdbcOdbcBatchUpdateException("No row count was produced from executeBatch", arrayOfInt2);
            }
          }
          catch (SQLException localSQLException)
          {
            arrayOfInt2 = new int[i];
            for (int l = 0; l <= j - 1; ++l)
              arrayOfInt2[l] = arrayOfInt1[l];
            clearBatch();
            throw new JdbcOdbcBatchUpdateException(localSQLException.getMessage(), localSQLException.getSQLState(), arrayOfInt2);
          }
        }
      clearBatch();
    }
    return arrayOfInt1;
  }

  public Connection getConnection()
    throws SQLException
  {
    return this.myConnection;
  }

  public String getSql()
  {
    return this.mySql;
  }

  public void setSql(String paramString)
  {
    this.mySql = paramString.toUpperCase();
  }

  public Object[] getObjects()
  {
    Object[] arrayOfObject = new Object[0];
    return arrayOfObject;
  }

  public int[] getObjectTypes()
  {
    int[] arrayOfInt = new int[0];
    return arrayOfInt;
  }

  public int getParamCount()
  {
    return 0;
  }

  public int getBlockCursorSize()
  {
    return this.rsBlockSize;
  }

  public void setBlockCursorSize(int paramInt)
  {
    this.rsBlockSize = paramInt;
  }

  protected int getStmtOption(short paramShort)
    throws SQLException
  {
    int i = 0;
    clearWarnings();
    try
    {
      i = (int)this.OdbcApi.SQLGetStmtOption(this.hStmt, paramShort);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      BigDecimal localBigDecimal = (BigDecimal)localJdbcOdbcSQLWarning.value;
      i = localBigDecimal.intValue();
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    return i;
  }

  protected int getColumnCount()
    throws SQLException
  {
    int i = 0;
    try
    {
      i = this.OdbcApi.SQLNumResultCols(this.hStmt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      BigDecimal localBigDecimal = (BigDecimal)localJdbcOdbcSQLWarning.value;
      i = localBigDecimal.intValue();
    }
    return i;
  }

  protected int getRowCount()
    throws SQLException
  {
    int i = 0;
    try
    {
      i = this.OdbcApi.SQLRowCount(this.hStmt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      BigDecimal localBigDecimal = (BigDecimal)localJdbcOdbcSQLWarning.value;
      i = localBigDecimal.intValue();
    }
    return i;
  }

  protected boolean lockIfNecessary(String paramString)
    throws SQLException
  {
    int i = 0;
    String str = paramString.toUpperCase();
    int j = str.indexOf(" FOR UPDATE");
    if (j > 0)
    {
      if (this.OdbcApi.getTracer().isTracing())
        this.OdbcApi.getTracer().trace("Setting concurrency for update");
      try
      {
        this.OdbcApi.SQLSetStmtOption(this.hStmt, 7, 2);
      }
      catch (SQLWarning localSQLWarning)
      {
        setWarning(localSQLWarning);
      }
      i = 1;
    }
    return i;
  }

  protected int getPrecision(int paramInt)
  {
    int i = -1;
    if (this.typeInfo != null)
    {
      JdbcOdbcTypeInfo localJdbcOdbcTypeInfo = (JdbcOdbcTypeInfo)this.typeInfo.get(new Integer(paramInt));
      if (localJdbcOdbcTypeInfo != null)
        i = localJdbcOdbcTypeInfo.getPrec();
    }
    if ((paramInt == -2) && (i == -1))
      i = getPrecision(-3);
    return i;
  }

  protected synchronized void clearMyResultSet()
    throws SQLException
  {
    if (this.myResultSet != null)
    {
      if (this.hStmt != 3412047170994438144L)
        this.myResultSet.close();
      this.myResultSet = null;
    }
  }

  protected void checkCursorDowngrade()
    throws SQLException
  {
    int i = (int)this.OdbcApi.SQLGetStmtOption(this.hStmt, 6);
    if (i != this.myConnection.getOdbcCursorType(this.rsType))
      if (i == 0)
        this.rsType = 1003;
      else
        this.rsType = 1004;
    setWarning(new SQLWarning("Result set type has been changed."));
  }

  public static int getTypeFromObject(Object paramObject)
  {
    if (paramObject == null)
      return 0;
    if (paramObject instanceof String)
      return 1;
    if (paramObject instanceof BigDecimal)
      return 2;
    if (paramObject instanceof Boolean)
      return -7;
    if (paramObject instanceof Byte)
      return -6;
    if (paramObject instanceof Short)
      return 5;
    if (paramObject instanceof Integer)
      return 4;
    if (paramObject instanceof Long)
      return -5;
    if (paramObject instanceof Float)
      return 6;
    if (paramObject instanceof Double)
      return 8;
    if (paramObject instanceof byte[])
      return -3;
    if (paramObject instanceof InputStream)
      return -4;
    if (paramObject instanceof Reader)
      return -1;
    if (paramObject instanceof Date)
      return 91;
    if (paramObject instanceof Time)
      return 92;
    if (paramObject instanceof Timestamp)
      return 93;
    return 1111;
  }

  public boolean getMoreResults(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public ResultSet getGeneratedKeys()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int executeUpdate(String paramString, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int executeUpdate(String paramString, int[] paramArrayOfInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int executeUpdate(String paramString, String[] paramArrayOfString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean execute(String paramString, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean execute(String paramString, int[] paramArrayOfInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean execute(String paramString, String[] paramArrayOfString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int getResultSetHoldability()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean isClosed()
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

  public boolean isPoolable()
    throws SQLException
  {
    return false;
  }

  public void setPoolable(boolean paramBoolean)
    throws SQLException
  {
  }
}
package sun.jdbc.odbc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

public class JdbcOdbcResultSet extends JdbcOdbcObject
  implements JdbcOdbcResultSetInterface
{
  protected JdbcOdbc OdbcApi = null;
  protected long hDbc = 3412045521726996480L;
  protected long hStmt = 3412045521726996480L;
  protected SQLWarning lastWarning = null;
  protected boolean keepHSTMT = false;
  protected JdbcOdbcBoundCol[] boundCols;
  protected int numberOfCols;
  protected int numResultCols = -1;
  protected int firstPseudoCol;
  protected int lastPseudoCol;
  protected JdbcOdbcPseudoCol[] pseudoCols;
  protected int[] colMappings;
  protected ResultSetMetaData rsmd;
  private Hashtable colNameToNum;
  private Hashtable colNumToName;
  private boolean lastColumnNull = false;
  private boolean closed;
  private int sqlTypeColumn;
  private boolean freed;
  private JdbcOdbcUtils utils = new JdbcOdbcUtils();
  private boolean ownInsertsAreVisible;
  private boolean ownDeletesAreVisible;
  protected JdbcOdbcStatement ownerStatement;
  protected int numberOfRows;
  protected int rowPosition;
  protected int lastRowPosition;
  protected int[] rowStatusArray;
  protected boolean atInsertRow;
  protected int lastForwardRecord;
  protected int lastColumnData;
  protected int rowSet;
  protected boolean blockCursor;
  protected int fetchCount;
  protected int currentBlockCell;
  protected int lastBlockPosition;
  protected boolean moveUpBlock;
  protected boolean moveDownBlock;
  protected short odbcCursorType;
  protected boolean rowUpdated;
  protected long[] pA;

  protected void finalize()
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("ResultSet.finalize " + this);
    try
    {
      if (!(this.closed))
      {
        this.hStmt = 3412047892548943872L;
        close();
      }
    }
    catch (SQLException localSQLException)
    {
    }
  }

  public void initialize(JdbcOdbc paramJdbcOdbc, long paramLong1, long paramLong2, boolean paramBoolean, JdbcOdbcStatement paramJdbcOdbcStatement)
    throws SQLException
  {
    this.OdbcApi = paramJdbcOdbc;
    this.hDbc = paramLong1;
    this.hStmt = paramLong2;
    this.keepHSTMT = paramBoolean;
    this.numberOfCols = getColumnCount();
    this.boundCols = new JdbcOdbcBoundCol[this.numberOfCols];
    for (int i = 0; i < this.numberOfCols; ++i)
      this.boundCols[i] = new JdbcOdbcBoundCol();
    this.ownerStatement = paramJdbcOdbcStatement;
    this.rowPosition = 0;
    this.lastForwardRecord = 0;
    this.lastRowPosition = 0;
    this.lastColumnData = 0;
    this.currentBlockCell = 0;
    this.blockCursor = false;
    this.rowSet = 1;
    if (getType() != 1003)
    {
      if (this.ownerStatement != null)
        this.rowSet = this.ownerStatement.getBlockCursorSize();
      setRowStatusPtr();
      setResultSetVisibilityIndicators();
      calculateRowCount();
      if (this.numberOfRows >= 0)
      {
        i = 0;
        boolean bool = setRowArraySize();
        if (!(bool))
          this.rowSet = 1;
        if ((this.pA != null) && (this.pA[0] != 3412047823829467136L))
        {
          JdbcOdbc.ReleaseStoredIntegers(this.pA[0], this.pA[1]);
          this.pA[0] = 3412048322045673472L;
          this.pA[1] = 3412048322045673472L;
        }
        setRowStatusPtr();
        if (this.rowSet > 1)
        {
          this.blockCursor = true;
          setCursorType();
        }
        for (int j = 0; j < this.numberOfCols; ++j)
          this.boundCols[j].initStagingArea(this.rowSet);
      }
    }
  }

  public boolean wasNull()
    throws SQLException
  {
    return this.lastColumnNull;
  }

  public void setAliasColumnName(String paramString, int paramInt)
  {
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
      this.boundCols[(paramInt - 1)].setAliasName(paramString);
  }

  public String mapColumnName(String paramString, int paramInt)
  {
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
      return this.boundCols[(paramInt - 1)].mapAliasName(paramString);
    return paramString;
  }

  public String getString(int paramInt)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      return null;
    }
    int i = getMaxCharLen(paramInt);
    int j = getColumnLength(paramInt);
    String str1 = null;
    if (j > 32767)
    {
      JdbcOdbcInputStream localJdbcOdbcInputStream = (JdbcOdbcInputStream)getAsciiStream(paramInt);
      try
      {
        byte[] arrayOfByte = localJdbcOdbcInputStream.readAllData();
        if (arrayOfByte != null)
          if ((arrayOfByte.length > 2) && (arrayOfByte[1] == 0))
            str1 = BytesToChars("UnicodeLittleUnmarked", arrayOfByte);
          else if ((arrayOfByte.length >= 2) && (arrayOfByte[0] == 0))
            str1 = BytesToChars("UnicodeBigUnmarked", arrayOfByte);
          else
            str1 = BytesToChars(this.OdbcApi.charSet, arrayOfByte);
        else
          this.lastColumnNull = true;
      }
      catch (Exception localException)
      {
        SQLException localSQLException = new SQLException(localException.getMessage());
        localSQLException.fillInStackTrace();
        throw localSQLException;
      }
    }
    else
    {
      if (i == -1)
      {
        this.lastColumnNull = true;
        return null;
      }
      boolean bool = true;
      int k = getColumnType(paramInt);
      switch (k)
      {
      case -1:
      case 1:
      case 12:
        bool = false;
      }
      str1 = getDataString(paramInt, ++i, bool);
      if (str1 == null)
      {
        this.lastColumnNull = true;
        return str1;
      }
      int l = str1.length();
      if ((l == i - 1) && (!(bool)))
        str1 = str1.substring(0, i - 1);
      if ((((k == -1) || (k == -4))) && (l == i - 1))
      {
        String str2 = str1;
        while (true)
        {
          do
          {
            if (str2.length() != 32767)
              break label509;
            str2 = getDataString(paramInt, i, bool);
            if (str2 == null)
              break label509;
            if (this.OdbcApi.getTracer().isTracing())
              this.OdbcApi.getTracer().trace("" + str2.length() + " byte(s) read");
            if (str2.length() == i)
              str2 = str2.substring(0, i - 1);
            str1 = str1 + str2;
          }
          while (!(this.OdbcApi.getTracer().isTracing()));
          this.OdbcApi.getTracer().trace("" + str1.length() + " bytes total");
        }
      }
    }
    label509: return str1;
  }

  public String getString(String paramString)
    throws SQLException
  {
    return getString(findColumn(paramString));
  }

  public boolean getBoolean(int paramInt)
    throws SQLException
  {
    checkOpen();
    int i = 0;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) == null)
      i = (getInt(paramInt) != 0) ? 1 : 0;
    else
      this.lastColumnNull = true;
    return i;
  }

  public boolean getBoolean(String paramString)
    throws SQLException
  {
    return getBoolean(findColumn(paramString));
  }

  public byte getByte(int paramInt)
    throws SQLException
  {
    checkOpen();
    int i = 0;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) == null)
      i = (byte)getInt(paramInt);
    else
      this.lastColumnNull = true;
    return i;
  }

  public byte getByte(String paramString)
    throws SQLException
  {
    return getByte(findColumn(paramString));
  }

  public short getShort(int paramInt)
    throws SQLException
  {
    checkOpen();
    int i = 0;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) == null)
      i = (short)getInt(paramInt);
    else
      this.lastColumnNull = true;
    return i;
  }

  public short getShort(String paramString)
    throws SQLException
  {
    return getShort(findColumn(paramString));
  }

  public int getInt(int paramInt)
    throws SQLException
  {
    checkOpen();
    int i = 0;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) == null)
    {
      Integer localInteger = getDataInteger(paramInt);
      if (localInteger != null)
        i = localInteger.intValue();
    }
    else
    {
      this.lastColumnNull = true;
    }
    return i;
  }

  public int getInt(String paramString)
    throws SQLException
  {
    return getInt(findColumn(paramString));
  }

  public long getLong(int paramInt)
    throws SQLException
  {
    checkOpen();
    long l = 3412047153814568960L;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) == null)
    {
      Double localDouble = getDataDouble(paramInt);
      if (localDouble != null)
        l = localDouble.longValue();
    }
    else
    {
      this.lastColumnNull = true;
    }
    return l;
  }

  public long getLong(String paramString)
    throws SQLException
  {
    return getLong(findColumn(paramString));
  }

  public float getFloat(int paramInt)
    throws SQLException
  {
    return (float)getDouble(paramInt);
  }

  public float getFloat(String paramString)
    throws SQLException
  {
    return getFloat(findColumn(paramString));
  }

  public double getDouble(int paramInt)
    throws SQLException
  {
    checkOpen();
    double d = 0D;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) == null)
    {
      Double localDouble = getDataDouble(paramInt);
      if (localDouble != null)
        d = localDouble.doubleValue();
    }
    else
    {
      this.lastColumnNull = true;
    }
    return d;
  }

  public double getDouble(String paramString)
    throws SQLException
  {
    return getDouble(findColumn(paramString));
  }

  public BigDecimal getBigDecimal(int paramInt1, int paramInt2)
    throws SQLException
  {
    checkOpen();
    BigDecimal localBigDecimal = null;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt1 = mapColumn(paramInt1);
    consecutiveFetch(paramInt1);
    if (getPseudoCol(paramInt1) == null)
    {
      String str = getDataString(paramInt1, 300, true);
      if (str != null)
      {
        localBigDecimal = new BigDecimal(str);
        localBigDecimal = localBigDecimal.setScale(paramInt2, 6);
      }
    }
    else
    {
      this.lastColumnNull = true;
    }
    return localBigDecimal;
  }

  public BigDecimal getBigDecimal(String paramString, int paramInt)
    throws SQLException
  {
    return getBigDecimal(findColumn(paramString), paramInt);
  }

  public synchronized byte[] getBytes(int paramInt)
    throws SQLException
  {
    int l;
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      return null;
    }
    int i = getMaxBinaryLen(paramInt);
    if (i == -1)
    {
      this.lastColumnNull = true;
      return null;
    }
    int j = getColumnLength(paramInt);
    if (j > 32767)
    {
      JdbcOdbcInputStream localJdbcOdbcInputStream = (JdbcOdbcInputStream)getBinaryStream(paramInt);
      try
      {
        return localJdbcOdbcInputStream.readAllData();
      }
      catch (Exception localException)
      {
        throw new SQLException(localException.getMessage());
      }
    }
    int k = getColumnType(paramInt);
    byte[] arrayOfByte1 = new byte[i];
    try
    {
      l = this.OdbcApi.SQLGetDataBinary(this.hStmt, paramInt, arrayOfByte1);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      Integer localInteger = (Integer)localJdbcOdbcSQLWarning.value;
      l = localInteger.intValue();
    }
    if (l == -1)
    {
      this.lastColumnNull = true;
      arrayOfByte1 = null;
    }
    if ((k != -2) && (l != i) && (arrayOfByte1 != null))
    {
      byte[] arrayOfByte2 = new byte[l];
      System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, l);
      return arrayOfByte2;
    }
    return arrayOfByte1;
  }

  public byte[] getBytes(String paramString)
    throws SQLException
  {
    return getBytes(findColumn(paramString));
  }

  public Date getDate(int paramInt)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      return null;
    }
    String str = getDataStringDate(paramInt);
    if (str == null)
      return null;
    return Date.valueOf(str);
  }

  public Date getDate(String paramString)
    throws SQLException
  {
    return getDate(findColumn(paramString));
  }

  public Time getTime(int paramInt)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      return null;
    }
    String str = getDataStringTime(paramInt);
    if (str == null)
      return null;
    return Time.valueOf(str);
  }

  public Time getTime(String paramString)
    throws SQLException
  {
    return getTime(findColumn(paramString));
  }

  public Timestamp getTimestamp(int paramInt)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      return null;
    }
    String str = getDataStringTimestamp(paramInt);
    if (str == null)
      return null;
    if (str.length() == 10)
      str = str + " 00:00:00";
    return Timestamp.valueOf(str);
  }

  public Timestamp getTimestamp(String paramString)
    throws SQLException
  {
    return getTimestamp(findColumn(paramString));
  }

  public InputStream getAsciiStream(int paramInt)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    int i = getColumnType(paramInt);
    int j = -2;
    switch (i)
    {
    case -10:
    case -9:
    case -8:
    case -1:
    case 1:
    case 12:
      j = 1;
    }
    JdbcOdbcInputStream localJdbcOdbcInputStream = new JdbcOdbcInputStream(this.OdbcApi, this.hStmt, paramInt, 1, j, this.ownerStatement);
    setInputStream(paramInt, localJdbcOdbcInputStream);
    return localJdbcOdbcInputStream;
  }

  public InputStream getAsciiStream(String paramString)
    throws SQLException
  {
    return getAsciiStream(findColumn(paramString));
  }

  public InputStream getUnicodeStream(int paramInt)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    int i = getColumnType(paramInt);
    int j = -2;
    switch (i)
    {
    case -10:
    case -9:
    case -8:
    case -1:
    case 1:
    case 12:
      j = 1;
    }
    JdbcOdbcInputStream localJdbcOdbcInputStream = new JdbcOdbcInputStream(this.OdbcApi, this.hStmt, paramInt, 2, j, this.ownerStatement);
    setInputStream(paramInt, localJdbcOdbcInputStream);
    return localJdbcOdbcInputStream;
  }

  public InputStream getUnicodeStream(String paramString)
    throws SQLException
  {
    return getUnicodeStream(findColumn(paramString));
  }

  public InputStream getBinaryStream(int paramInt)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    int i = getColumnType(paramInt);
    int j = -2;
    switch (i)
    {
    case -10:
    case -9:
    case -8:
    case -1:
    case 1:
    case 12:
      j = 1;
    }
    JdbcOdbcInputStream localJdbcOdbcInputStream = new JdbcOdbcInputStream(this.OdbcApi, this.hStmt, paramInt, 3, j, this.ownerStatement);
    setInputStream(paramInt, localJdbcOdbcInputStream);
    return localJdbcOdbcInputStream;
  }

  public InputStream getBinaryStream(String paramString)
    throws SQLException
  {
    return getBinaryStream(findColumn(paramString));
  }

  public boolean next()
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      bool = false;
      int i = 0;
      if (getFetchDirection() == 1000)
      {
        if (this.rowPosition == this.numberOfRows)
        {
          afterLast();
          return false;
        }
        if (this.blockCursor)
        {
          bool = relative(1, false);
        }
        else
        {
          this.rowPosition += 1;
          bool = fetchScrollOption(this.rowPosition, 5);
        }
      }
      else
      {
        if (this.rowPosition == 1)
        {
          beforeFirst();
          return false;
        }
        if (this.blockCursor)
        {
          bool = relative(-1, false);
        }
        else
        {
          this.rowPosition -= 1;
          bool = fetchScrollOption(this.rowPosition, 5);
        }
      }
      return bool;
    }
    boolean bool = true;
    this.lastColumnNull = false;
    closeInputStreams();
    clearWarnings();
    try
    {
      bool = this.OdbcApi.SQLFetch(this.hStmt);
    }
    catch (SQLWarning localSQLWarning)
    {
      setWarning(localSQLWarning);
    }
    if (bool == true)
      this.rowPosition += 1;
    else if (!(bool))
      if (this.lastForwardRecord == 0)
      {
        this.lastForwardRecord = this.rowPosition;
        this.rowPosition = 0;
      }
      else
      {
        this.rowPosition = 0;
      }
    return bool;
  }

  public int getRowNumber()
    throws SQLException
  {
    checkOpen();
    int i = 0;
    clearWarnings();
    try
    {
      i = (int)this.OdbcApi.SQLGetStmtOption(this.hStmt, 14);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      BigDecimal localBigDecimal = (BigDecimal)localJdbcOdbcSQLWarning.value;
      i = localBigDecimal.intValue();
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    return i;
  }

  public int getColumnCount()
    throws SQLException
  {
    checkOpen();
    int i = 0;
    clearWarnings();
    if (this.lastPseudoCol > 0)
      return this.lastPseudoCol;
    if (this.colMappings != null)
      return this.colMappings.length;
    try
    {
      this.numResultCols = this.OdbcApi.SQLNumResultCols(this.hStmt);
      i = this.numResultCols;
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      BigDecimal localBigDecimal = (BigDecimal)localJdbcOdbcSQLWarning.value;
      i = localBigDecimal.intValue();
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    return i;
  }

  public int getRowCount()
    throws SQLException
  {
    checkOpen();
    return this.numberOfRows;
  }

  public synchronized void close()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSet.close");
    closeInputStreams();
    clearWarnings();
    this.lastColumnNull = false;
    if ((this.OdbcApi != null) && (this.hStmt != 3412046827397054464L))
    {
      if (this.keepHSTMT)
        break label81:
      this.OdbcApi.SQLFreeStmt(this.hStmt, 1);
      this.hStmt = 3412047325613260800L;
      label81: this.closed = true;
      FreeCols();
      if ((this.pA != null) && (this.pA[0] != 3412047325613260800L))
      {
        JdbcOdbc.ReleaseStoredIntegers(this.pA[0], this.pA[1]);
        this.pA[0] = 3412047823829467136L;
        this.pA[1] = 3412047823829467136L;
      }
      if (this.ownerStatement != null)
        this.ownerStatement.myResultSet = null;
      if (this.OdbcApi.getTracer().isTracing())
        this.OdbcApi.getTracer().trace("*ResultSet has been closed");
    }
  }

  public synchronized void FreeCols()
    throws NullPointerException
  {
    int i;
    try
    {
      for (i = 0; i < this.boundCols.length; ++i)
      {
        if (this.boundCols[i].pA1 != 3412047600491167744L)
        {
          JdbcOdbc.ReleaseStoredBytes(this.boundCols[i].pA1, this.boundCols[i].pA2);
          this.boundCols[i].pA1 = 3412048390765150208L;
          this.boundCols[i].pA2 = 3412048390765150208L;
        }
        if (this.boundCols[i].pB1 != 3412047600491167744L)
        {
          JdbcOdbc.ReleaseStoredBytes(this.boundCols[i].pB1, this.boundCols[i].pB2);
          this.boundCols[i].pB1 = 3412048390765150208L;
          this.boundCols[i].pB2 = 3412048390765150208L;
        }
        if (this.boundCols[i].pC1 != 3412047600491167744L)
        {
          JdbcOdbc.ReleaseStoredBytes(this.boundCols[i].pC1, this.boundCols[i].pC2);
          this.boundCols[i].pC1 = 3412048390765150208L;
          this.boundCols[i].pC2 = 3412048390765150208L;
        }
        if (this.boundCols[i].pS1 != 3412047600491167744L)
        {
          JdbcOdbc.ReleaseStoredChars(this.boundCols[i].pS1, this.boundCols[i].pS2);
          this.boundCols[i].pS1 = 3412048390765150208L;
          this.boundCols[i].pS2 = 3412048390765150208L;
        }
      }
    }
    catch (NullPointerException localNullPointerException)
    {
    }
  }

  public String getCursorName()
    throws SQLException
  {
    checkOpen();
    String str = "";
    clearWarnings();
    try
    {
      str = this.OdbcApi.SQLGetCursorName(this.hStmt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      str = (String)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    return str.trim();
  }

  public ResultSetMetaData getMetaData()
    throws SQLException
  {
    checkOpen();
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSet.getMetaData");
    if (this.closed)
      throw new SQLException("ResultSet is closed");
    return new JdbcOdbcResultSetMetaData(this.OdbcApi, this);
  }

  public Object getObject(int paramInt)
    throws SQLException
  {
    checkOpen();
    Object localObject = null;
    int i = getColumnType(paramInt);
    int j = paramInt;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      return null;
    }
    switch (i)
    {
    case -1:
    case 1:
    case 12:
      localObject = getString(paramInt);
      break;
    case 2:
    case 3:
      localObject = getBigDecimal(paramInt, getScale(j));
      break;
    case -7:
      localObject = new Boolean(getBoolean(paramInt));
      break;
    case -6:
    case 4:
    case 5:
      localObject = new Integer(getInt(paramInt));
      break;
    case -5:
      localObject = new Long(getLong(paramInt));
      break;
    case 7:
      localObject = new Float(getFloat(paramInt));
      break;
    case 6:
    case 8:
      localObject = new Double(getDouble(paramInt));
      break;
    case -4:
    case -3:
    case -2:
      localObject = getBytes(paramInt);
      break;
    case 91:
      localObject = getDate(paramInt);
      break;
    case 92:
      localObject = getTime(paramInt);
      break;
    case 93:
      localObject = getTimestamp(paramInt);
    }
    if (wasNull())
      localObject = null;
    return localObject;
  }

  public Object getObject(String paramString)
    throws SQLException
  {
    return getObject(findColumn(paramString));
  }

  public SQLWarning getWarnings()
    throws SQLException
  {
    checkOpen();
    return this.lastWarning;
  }

  public void clearWarnings()
    throws SQLException
  {
    checkOpen();
    this.lastWarning = null;
  }

  public void setWarning(SQLWarning paramSQLWarning)
    throws SQLException
  {
    checkOpen();
    this.lastWarning = paramSQLWarning;
  }

  public long getHSTMT()
  {
    return this.hStmt;
  }

  public synchronized int findColumn(String paramString)
    throws SQLException
  {
    if (this.rsmd == null)
    {
      this.rsmd = getMetaData();
      this.colNameToNum = new Hashtable();
      this.colNumToName = new Hashtable();
    }
    Integer localInteger = (Integer)this.colNameToNum.get(paramString);
    if (localInteger == null)
    {
      for (int i = 1; i <= this.rsmd.getColumnCount(); ++i)
      {
        String str = (String)this.colNumToName.get(new Integer(i));
        if (str == null)
        {
          str = this.rsmd.getColumnName(i);
          this.colNameToNum.put(str, new Integer(i));
          this.colNumToName.put(new Integer(i), str);
        }
        if (str.equalsIgnoreCase(paramString))
          return i;
      }
      throw new SQLException("Column not found", "S0022");
    }
    return localInteger.intValue();
  }

  public Reader getCharacterStream(int paramInt)
    throws SQLException
  {
    checkOpen();
    InputStreamReader localInputStreamReader = null;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    int i = getColumnType(paramInt);
    int j = -2;
    switch (i)
    {
    case -10:
    case -9:
    case -8:
    case -1:
    case 1:
    case 12:
      j = 1;
    }
    String str = this.OdbcApi.charSet;
    JdbcOdbcInputStream localJdbcOdbcInputStream = new JdbcOdbcInputStream(this.OdbcApi, this.hStmt, paramInt, 5, j, this.ownerStatement);
    setInputStream(paramInt, localJdbcOdbcInputStream);
    try
    {
      localInputStreamReader = new InputStreamReader(localJdbcOdbcInputStream, str);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new SQLException("getCharacterStream() with Encoding ('encoding') :" + localUnsupportedEncodingException.getMessage());
    }
    return localInputStreamReader;
  }

  public Reader getCharacterStream(String paramString)
    throws SQLException
  {
    int i = findColumn(paramString);
    return getCharacterStream(i);
  }

  public BigDecimal getBigDecimal(int paramInt)
    throws SQLException
  {
    checkOpen();
    BigDecimal localBigDecimal = null;
    clearWarnings();
    this.lastColumnNull = false;
    paramInt = mapColumn(paramInt);
    consecutiveFetch(paramInt);
    if (getPseudoCol(paramInt) == null)
    {
      String str = getDataString(paramInt, 300, true);
      if (str != null)
        localBigDecimal = new BigDecimal(str);
    }
    else
    {
      this.lastColumnNull = true;
    }
    return localBigDecimal;
  }

  public BigDecimal getBigDecimal(String paramString)
    throws SQLException
  {
    int i = findColumn(paramString);
    return getBigDecimal(i);
  }

  public boolean isBeforeFirst()
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      if (this.numberOfRows > 0)
        return (this.rowPosition == 0);
      return false;
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  public boolean isAfterLast()
    throws SQLException
  {
    checkOpen();
    if (this.closed)
      throw new SQLException("ResultSet is closed");
    if (getType() != 1003)
    {
      if (this.numberOfRows > 0)
        return (this.rowPosition > this.numberOfRows);
      return false;
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  public boolean isFirst()
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      if (this.numberOfRows > 0)
        return (this.rowPosition == 1);
      return false;
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  public boolean isLast()
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      if (this.numberOfRows > 0)
        return (this.rowPosition == this.numberOfRows);
      return false;
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  public void beforeFirst()
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      boolean bool = false;
      bool = fetchScrollOption(0, 5);
      this.rowPosition = 0;
      this.currentBlockCell = 0;
      if (this.atInsertRow)
      {
        this.lastRowPosition = 0;
        this.lastBlockPosition = 0;
        this.atInsertRow = false;
      }
    }
    else
    {
      throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
    }
  }

  public void afterLast()
    throws SQLException
  {
    checkOpen();
    boolean bool = false;
    if (getType() != 1003)
    {
      bool = fetchScrollOption(this.numberOfRows + 1, 5);
      this.rowPosition = (this.numberOfRows + 1);
      this.currentBlockCell = (this.rowSet + 1);
      if (this.atInsertRow)
      {
        this.lastRowPosition = 0;
        this.lastBlockPosition = 0;
        this.atInsertRow = false;
      }
    }
    else
    {
      throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
    }
  }

  public boolean first()
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      if (this.numberOfRows > 0)
      {
        boolean bool1 = false;
        boolean bool2 = false;
        if (this.blockCursor)
        {
          bool1 = blockFetch(1, 2);
          if (!(bool1))
            bool2 = true;
        }
        if ((!(this.blockCursor)) || (bool1))
        {
          resetInsertRow();
          this.lastColumnNull = false;
          closeInputStreams();
          clearWarnings();
          bool2 = fetchScrollOption(this.rowPosition, 2);
        }
        if (bool2)
        {
          this.rowPosition = 1;
          this.currentBlockCell = this.rowPosition;
        }
        return bool2;
      }
      return false;
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  public boolean last()
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      if (this.numberOfRows > 0)
      {
        moveToCurrentRow();
        boolean bool1 = false;
        boolean bool2 = false;
        if (this.blockCursor)
        {
          bool1 = blockFetch(this.numberOfRows, 3);
          if (!(bool1))
          {
            setPos(this.currentBlockCell, 0);
            bool2 = true;
          }
          else
          {
            bool2 = true;
          }
        }
        if ((!(this.blockCursor)) || (bool1))
        {
          resetInsertRow();
          this.lastColumnNull = false;
          closeInputStreams();
          clearWarnings();
          if (bool1)
            bool2 = fetchScrollOption(this.numberOfRows, 5);
          else
            bool2 = fetchScrollOption(this.numberOfRows, 3);
          if (bool2)
            this.rowPosition = this.numberOfRows;
          this.currentBlockCell = 1;
        }
        return bool2;
      }
      return false;
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  public int getRow()
    throws SQLException
  {
    checkOpen();
    if (getType() == 1003)
    {
      if (this.lastForwardRecord == 0)
        return this.rowPosition;
      return 0;
    }
    if (this.numberOfRows > 0)
    {
      if ((this.rowPosition <= 0) || (this.rowPosition > this.numberOfRows))
        return 0;
      return this.rowPosition;
    }
    return 0;
  }

  public boolean absolute(int paramInt)
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      if (this.numberOfRows > 0)
      {
        boolean bool1 = false;
        boolean bool2 = false;
        if (paramInt != 0)
        {
          if (this.blockCursor)
          {
            if (this.atInsertRow)
            {
              this.rowPosition = this.lastRowPosition;
              this.currentBlockCell = this.lastBlockPosition;
              this.atInsertRow = false;
            }
            bool2 = blockFetch(paramInt, 5);
            if (bool2)
            {
              this.currentBlockCell = 1;
            }
            else
            {
              setPos(this.currentBlockCell, 0);
              bool1 = true;
            }
          }
          if ((!(this.blockCursor)) || (bool2))
          {
            if (paramInt >= 0)
              this.rowPosition = paramInt;
            else
              this.rowPosition = (this.numberOfRows + 1 + paramInt);
            if (this.rowPosition > this.numberOfRows)
            {
              afterLast();
              return false;
            }
            if (this.rowPosition < 1)
            {
              beforeFirst();
              return false;
            }
            this.lastColumnNull = false;
            closeInputStreams();
            clearWarnings();
            bool1 = fetchScrollOption(paramInt, 5);
          }
          return bool1;
        }
        throw new SQLException("Cursor position (" + paramInt + ") is invalid");
      }
      return false;
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  protected boolean fetchScrollOption(int paramInt, short paramShort)
    throws SQLException
  {
    if (this.numberOfRows > 0)
    {
      try
      {
        this.OdbcApi.SQLFetchScroll(this.hStmt, paramShort, paramInt);
      }
      catch (SQLWarning localSQLWarning)
      {
        setWarning(localSQLWarning);
        return true;
      }
      catch (SQLException localSQLException)
      {
        return false;
      }
      return true;
    }
    return false;
  }

  protected void consecutiveFetch(int paramInt)
    throws SQLException
  {
    int i = 0;
    if ((this.blockCursor) && (this.rowUpdated))
      i = 1;
    if (this.rowSet == 1)
      return;
    if ((this.lastColumnData == paramInt) || (i != 0))
    {
      try
      {
        this.OdbcApi.SQLFetchScroll(this.hStmt, 5, getRow());
        this.lastColumnData = 0;
        if (this.blockCursor)
          this.currentBlockCell = 1;
      }
      catch (SQLWarning localSQLWarning)
      {
        setWarning(localSQLWarning);
      }
      catch (SQLException localSQLException)
      {
      }
      this.rowUpdated = false;
    }
    else
    {
      this.lastColumnData = paramInt;
    }
  }

  public boolean relative(int paramInt)
    throws SQLException
  {
    checkOpen();
    return relative(paramInt, true);
  }

  protected boolean relative(int paramInt, boolean paramBoolean)
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      if (this.numberOfRows > 0)
      {
        moveToCurrentRow();
        boolean bool1 = false;
        boolean bool2 = false;
        int i = -1;
        if (paramBoolean)
        {
          if (this.rowPosition == 0)
            throw new SQLException("Cursor is positioned before the ResultSet");
          if (this.rowPosition > this.numberOfRows)
            throw new SQLException("Cursor is positioned after the ResultSet");
        }
        if (this.blockCursor)
        {
          bool2 = blockFetch(this.rowPosition + paramInt, 5);
          if (bool2)
          {
            i = this.rowPosition;
            i += paramInt;
          }
          else if (paramInt == 0)
          {
            i = paramInt;
            bool2 = true;
          }
        }
        else
        {
          i = this.rowPosition;
          i += paramInt;
        }
        if ((i <= 1) && (paramInt < 0))
        {
          beforeFirst();
          if (paramBoolean)
            return false;
          if (i != 1)
            break label287;
          return true;
        }
        if ((i >= this.numberOfRows) && (paramInt > 0))
        {
          afterLast();
          if (paramBoolean)
            return false;
          if (i != this.numberOfRows)
            break label287;
          return true;
        }
        this.lastColumnNull = false;
        closeInputStreams();
        clearWarnings();
        if (this.blockCursor)
        {
          if (bool2)
          {
            bool1 = fetchScrollOption(i, 5);
            if (bool1)
            {
              this.rowPosition = i;
              this.currentBlockCell = 1;
            }
          }
          else
          {
            setPos(this.currentBlockCell, 0);
            bool1 = true;
          }
        }
        else
        {
          bool1 = fetchScrollOption(paramInt, 6);
          if (bool1)
            this.rowPosition = i;
        }
        label287: return bool1;
      }
      else
      {
        throw new SQLException("Call to relative(" + paramInt + ") when there is no current row.");
      }
      return false;
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  public boolean previous()
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      int i = 0;
      if (this.numberOfRows > 0)
      {
        if (this.atInsertRow)
          i = this.lastRowPosition;
        moveToCurrentRow();
        if (getFetchDirection() == 1000)
        {
          if (i > 0)
            return absolute(i - 1);
          if (this.rowPosition > 1)
            return absolute(this.rowPosition - 1);
          if (this.rowPosition == 1)
          {
            beforeFirst();
            return false;
          }
          return (!(isBeforeFirst()));
        }
        if (i > 0)
          return absolute(i + 1);
        if (this.rowPosition < this.numberOfRows)
          return absolute(this.rowPosition + 1);
        if (this.rowPosition == this.numberOfRows)
        {
          afterLast();
          return false;
        }
        return (!(isAfterLast()));
      }
      return false;
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  protected boolean blockFetch(int paramInt, short paramShort)
    throws SQLException
  {
    int i = 0;
    if ((isBeforeFirst()) || (isAfterLast()))
      return true;
    switch (paramShort)
    {
    case 2:
      if (this.rowPosition == 1)
        break label278;
      if (!(isRowWithinTheBlock(1)))
      {
        i = 1;
        break label278:
      }
      this.rowPosition = 1;
      this.currentBlockCell = this.rowPosition;
      break;
    case 3:
      if (this.rowPosition < this.numberOfRows)
      {
        if (!(isRowWithinTheBlock(this.numberOfRows)))
        {
          i = 1;
          break label278:
        }
        while (this.rowPosition != this.numberOfRows)
        {
          this.rowPosition += 1;
          this.currentBlockCell += 1;
        }
        i = 0;
        break label278:
      }
      if (this.rowPosition != this.numberOfRows)
        break label278;
      i = 0;
      break;
    case 5:
      if (this.rowPosition == paramInt)
        break label278;
      if ((paramInt < 0) || (paramInt > this.numberOfRows))
      {
        i = 1;
        break label278:
      }
      if (!(isRowWithinTheBlock(paramInt)))
      {
        i = 1;
        break label278:
      }
      while (true)
      {
        do
          while (true)
          {
            if (this.rowPosition == paramInt)
              break label273;
            if (!(this.moveUpBlock))
              break;
            this.rowPosition -= 1;
            this.currentBlockCell -= 1;
          }
        while (!(this.moveDownBlock));
        this.rowPosition += 1;
        this.currentBlockCell += 1;
      }
      label273: i = 0;
    case 4:
    }
    label278: return i;
  }

  protected boolean isRowWithinTheBlock(int paramInt)
  {
    int i = 0;
    if (this.rowPosition != 0)
    {
      int j = this.rowPosition - this.currentBlockCell - 1;
      int k = this.rowPosition + this.rowSet - this.currentBlockCell;
      if ((k < paramInt) || (j > paramInt))
      {
        i = 0;
      }
      else if (paramInt > this.rowPosition)
      {
        i = 1;
        this.moveUpBlock = false;
        this.moveDownBlock = true;
      }
      else if (paramInt < this.rowPosition)
      {
        i = 1;
        this.moveUpBlock = true;
        this.moveDownBlock = false;
      }
    }
    return i;
  }

  protected int getRowIndex()
  {
    int i = 0;
    if (this.blockCursor)
      i = this.currentBlockCell - 1;
    else if (this.atInsertRow)
      i = this.rowSet;
    return i;
  }

  public void setFetchDirection(int paramInt)
    throws SQLException
  {
    checkOpen();
    this.ownerStatement.setFetchDirection(paramInt);
  }

  public int getFetchDirection()
    throws SQLException
  {
    checkOpen();
    return this.ownerStatement.getFetchDirection();
  }

  public void setFetchSize(int paramInt)
    throws SQLException
  {
    checkOpen();
    this.ownerStatement.setFetchSize(paramInt);
  }

  public int getFetchSize()
    throws SQLException
  {
    checkOpen();
    return this.ownerStatement.getFetchSize();
  }

  public int getType()
    throws SQLException
  {
    checkOpen();
    if (this.ownerStatement != null)
      return this.ownerStatement.getResultSetType();
    return 1003;
  }

  public int getConcurrency()
    throws SQLException
  {
    checkOpen();
    return this.ownerStatement.getResultSetConcurrency();
  }

  public boolean rowUpdated()
    throws SQLException
  {
    checkOpen();
    if (this.numberOfRows > 0)
    {
      int i = getRowIndex();
      if (this.blockCursor)
        return (this.rowStatusArray[i] == 2);
      return (this.rowStatusArray[(this.rowSet - 1)] == 2);
    }
    return false;
  }

  public boolean rowInserted()
    throws SQLException
  {
    checkOpen();
    if (this.numberOfRows > 0)
    {
      int i = getRowIndex();
      if (this.blockCursor)
        return (this.rowStatusArray[i] == 4);
      return (this.rowStatusArray[(this.rowSet - 1)] == 4);
    }
    return false;
  }

  public boolean rowDeleted()
    throws SQLException
  {
    checkOpen();
    if (this.numberOfRows > 0)
    {
      int i = getRowIndex();
      if (this.blockCursor)
        return (this.rowStatusArray[i] == 1);
      return (this.rowStatusArray[(this.rowSet - 1)] == 1);
    }
    return false;
  }

  public void updateNull(int paramInt)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
    {
      int j = getColumnType(paramInt);
      if (j != 9999)
        this.boundCols[(paramInt - 1)].setRowValues(i, null, -1);
      else
        throw new SQLException("Unknown Data Type for column [#" + paramInt + "]");
    }
  }

  public void updateBoolean(int paramInt, boolean paramBoolean)
    throws SQLException
  {
    checkOpen();
    int i = 0;
    if (paramBoolean)
      i = 1;
    updateInt(paramInt, i);
  }

  public void updateByte(int paramInt, byte paramByte)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
    {
      int j = getColumnType(paramInt);
      if (j != -6)
        this.boundCols[(paramInt - 1)].setType(-6);
      this.boundCols[(paramInt - 1)].setRowValues(i, new Integer(paramByte), 4);
    }
  }

  public void updateShort(int paramInt, short paramShort)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
    {
      int j = getColumnType(paramInt);
      if (j != 5)
        this.boundCols[(paramInt - 1)].setType(5);
      this.boundCols[(paramInt - 1)].setRowValues(i, new Integer(paramShort), 4);
    }
  }

  public void updateInt(int paramInt1, int paramInt2)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt1 > 0) && (paramInt1 <= this.numberOfCols))
    {
      int j = getColumnType(paramInt1);
      if (j != 4)
        this.boundCols[(paramInt1 - 1)].setType(4);
      this.boundCols[(paramInt1 - 1)].setRowValues(i, new Integer(paramInt2), 4);
    }
  }

  public void updateLong(int paramInt, long paramLong)
    throws SQLException
  {
    checkOpen();
    updateFloat(paramInt, (float)paramLong);
  }

  public void updateFloat(int paramInt, float paramFloat)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
    {
      int j = getColumnType(paramInt);
      if (j != 6)
        this.boundCols[(paramInt - 1)].setType(6);
      this.boundCols[(paramInt - 1)].setRowValues(i, new Float(paramFloat), 4);
    }
  }

  public void updateDouble(int paramInt, double paramDouble)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
    {
      int j = getColumnType(paramInt);
      if (j != 8)
        this.boundCols[(paramInt - 1)].setType(8);
      this.boundCols[(paramInt - 1)].setRowValues(i, new Double(paramDouble), 8);
    }
  }

  public void updateBigDecimal(int paramInt, BigDecimal paramBigDecimal)
    throws SQLException
  {
    checkOpen();
    int i = getColumnType(paramInt);
    if ((i == 3) || (i == 2))
      if (paramBigDecimal == null)
        updateChar(paramInt, i, null);
      else
        updateChar(paramInt, i, paramBigDecimal.toString());
    else if (paramBigDecimal == null)
      updateChar(paramInt, 2, null);
    else
      updateChar(paramInt, 2, paramBigDecimal.toString());
  }

  public void updateString(int paramInt, String paramString)
    throws SQLException
  {
    byte[] arrayOfByte;
    checkOpen();
    try
    {
      if (paramString == null)
        arrayOfByte = null;
      else
        arrayOfByte = paramString.getBytes(this.OdbcApi.charSet);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new SQLException(localUnsupportedEncodingException.getMessage());
    }
    updateBytes(paramInt, arrayOfByte);
  }

  public void updateBytes(int paramInt, byte[] paramArrayOfByte)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramArrayOfByte != null) && (paramArrayOfByte.length > 8000))
      updateBinaryStream(paramInt, new ByteArrayInputStream(paramArrayOfByte), paramArrayOfByte.length);
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
    {
      int j = getColumnType(paramInt);
      if ((j != -2) && (j != -3))
      {
        this.boundCols[(paramInt - 1)].setType(-2);
        if (paramArrayOfByte != null)
          this.boundCols[(paramInt - 1)].setLength(paramArrayOfByte.length);
      }
      if (paramArrayOfByte == null)
      {
        updateNull(paramInt);
        return;
      }
      if (paramArrayOfByte.length > this.boundCols[(paramInt - 1)].getLength())
        this.boundCols[(paramInt - 1)].setLength(paramArrayOfByte.length);
      this.boundCols[(paramInt - 1)].setRowValues(i, (byte[])paramArrayOfByte, paramArrayOfByte.length);
    }
  }

  public void updateDate(int paramInt, Date paramDate)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
    {
      if (getColumnType(paramInt) != 91)
        this.boundCols[(paramInt - 1)].setType(91);
      if (paramDate == null)
      {
        updateNull(paramInt);
        return;
      }
      this.boundCols[(paramInt - 1)].setRowValues(i, paramDate, 6);
    }
  }

  public void updateTime(int paramInt, Time paramTime)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
    {
      if (getColumnType(paramInt) != 92)
        this.boundCols[(paramInt - 1)].setType(92);
      if (paramTime == null)
      {
        updateNull(paramInt);
        return;
      }
      this.boundCols[(paramInt - 1)].setRowValues(i, paramTime, 6);
    }
  }

  public void updateTimestamp(int paramInt, Timestamp paramTimestamp)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
    {
      if (getColumnType(paramInt) != 93)
        this.boundCols[(paramInt - 1)].setType(93);
      if (paramTimestamp == null)
      {
        updateNull(paramInt);
        return;
      }
      this.boundCols[(paramInt - 1)].setRowValues(i, paramTimestamp, 16);
    }
  }

  public void updateAsciiStream(int paramInt1, InputStream paramInputStream, int paramInt2)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt1 > 0) && (paramInt1 <= this.numberOfCols))
    {
      if (getColumnType(paramInt1) != -1)
        this.boundCols[(paramInt1 - 1)].setType(-1);
      if (paramInputStream == null)
      {
        updateNull(paramInt1);
        return;
      }
      if (paramInt2 != this.boundCols[(paramInt1 - 1)].getLength())
        this.boundCols[(paramInt1 - 1)].setLength(paramInt2);
      this.boundCols[(paramInt1 - 1)].setRowValues(i, paramInputStream, paramInt2);
      this.boundCols[(paramInt1 - 1)].setStreamType(1);
    }
  }

  public void updateBinaryStream(int paramInt1, InputStream paramInputStream, int paramInt2)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt1 > 0) && (paramInt1 <= this.numberOfCols))
    {
      if (getColumnType(paramInt1) != -4)
        this.boundCols[(paramInt1 - 1)].setType(-4);
      if (paramInputStream == null)
      {
        updateNull(paramInt1);
        return;
      }
      if (paramInt2 != this.boundCols[(paramInt1 - 1)].getLength())
        this.boundCols[(paramInt1 - 1)].setLength(paramInt2);
      this.boundCols[(paramInt1 - 1)].setRowValues(i, paramInputStream, paramInt2);
      this.boundCols[(paramInt1 - 1)].setStreamType(3);
    }
  }

  public void updateCharacterStream(int paramInt1, Reader paramReader, int paramInt2)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    BufferedReader localBufferedReader = null;
    BufferedOutputStream localBufferedOutputStream = null;
    ByteArrayOutputStream localByteArrayOutputStream = null;
    ByteArrayInputStream localByteArrayInputStream = null;
    String str = this.OdbcApi.charSet;
    int j = 300;
    if (paramInt2 < j)
      j = paramInt2;
    int k = 0;
    int l = 0;
    try
    {
      l = (int)Charset.forName(str).newEncoder().maxBytesPerChar();
    }
    catch (UnsupportedCharsetException localUnsupportedCharsetException)
    {
    }
    catch (IllegalCharsetNameException localIllegalCharsetNameException)
    {
    }
    if (l == 0)
      l = 1;
    try
    {
      if (paramReader != null)
      {
        int i1 = 0;
        int i2 = 0;
        localBufferedReader = new BufferedReader(paramReader);
        localByteArrayOutputStream = new ByteArrayOutputStream();
        localBufferedOutputStream = new BufferedOutputStream(localByteArrayOutputStream);
        char[] arrayOfChar1 = new char[j];
        while (i2 != -1)
        {
          byte[] arrayOfByte = new byte[0];
          i2 = localBufferedReader.read(arrayOfChar1);
          if (i2 != -1)
          {
            char[] arrayOfChar2 = new char[i2];
            for (int i3 = 0; i3 < i2; ++i3)
              arrayOfChar2[i3] = arrayOfChar1[i3];
            arrayOfByte = CharsToBytes(str, arrayOfChar2);
            i3 = arrayOfByte.length - 1;
            localBufferedOutputStream.write(arrayOfByte, 0, i3);
            localBufferedOutputStream.flush();
          }
        }
        k = localByteArrayOutputStream.size();
        localByteArrayInputStream = new ByteArrayInputStream(localByteArrayOutputStream.toByteArray());
      }
    }
    catch (IOException localIOException)
    {
      throw new SQLException("CharsToBytes Reader Conversion: " + localIOException.getMessage());
    }
    if ((paramInt1 > 0) && (paramInt1 <= this.numberOfCols))
    {
      if ((getColumnType(paramInt1) != -1) || (getColumnType(paramInt1) != 12))
        this.boundCols[(paramInt1 - 1)].setType(-1);
      if (paramReader == null)
      {
        updateNull(paramInt1);
        return;
      }
      if (k != this.boundCols[(paramInt1 - 1)].getLength())
        this.boundCols[(paramInt1 - 1)].setLength(k);
      this.boundCols[(paramInt1 - 1)].setRowValues(i, localByteArrayInputStream, k);
      this.boundCols[(paramInt1 - 1)].setStreamType(3);
    }
  }

  public void updateObject(int paramInt1, Object paramObject, int paramInt2)
    throws SQLException
  {
    updateObject(paramInt1, paramObject, paramInt2, this.boundCols[(paramInt1 - 1)].getType());
  }

  public void updateObject(int paramInt, Object paramObject)
    throws SQLException
  {
    updateObject(paramInt, paramObject, 0, this.boundCols[(paramInt - 1)].getType());
  }

  protected void updateObject(int paramInt1, Object paramObject, int paramInt2, int paramInt3)
    throws SQLException
  {
    checkOpen();
    if ((paramInt3 == 9999) && (paramObject != null))
      paramInt3 = JdbcOdbcStatement.getTypeFromObject(paramObject);
    else if (paramObject == null)
      paramInt3 = 0;
    if ((paramInt1 > 0) && (paramInt1 <= this.numberOfCols))
      switch (paramInt3)
      {
      case 1:
      case 12:
        updateString(paramInt1, (String)paramObject);
        break;
      case -1:
        if ((paramObject instanceof byte[]) && ((byte[])(byte[])paramObject != null))
        {
          byte[] arrayOfByte1 = (byte[])(byte[])paramObject;
          updateAsciiStream(paramInt1, new ByteArrayInputStream(arrayOfByte1), arrayOfByte1.length);
          return;
        }
        if ((paramObject instanceof Reader) && ((Reader)paramObject != null))
          throw new SQLException("Unknown length for Reader Object, try updateCharacterStream.");
        if ((!(paramObject instanceof String)) || ((String)paramObject == null))
          return;
        updateString(paramInt1, (String)paramObject);
        break;
      case 2:
      case 3:
        updateBigDecimal(paramInt1, (BigDecimal)paramObject);
        break;
      case -7:
        updateBoolean(paramInt1, ((Boolean)paramObject).booleanValue());
        break;
      case -6:
        updateByte(paramInt1, (byte)((Integer)paramObject).intValue());
        break;
      case 5:
        updateShort(paramInt1, (short)((Integer)paramObject).intValue());
        break;
      case 4:
        updateInt(paramInt1, ((Integer)paramObject).intValue());
        break;
      case -5:
        updateLong(paramInt1, ((Integer)paramObject).longValue());
        break;
      case 6:
      case 7:
        updateFloat(paramInt1, ((Float)paramObject).floatValue());
        break;
      case 8:
        updateDouble(paramInt1, ((Double)paramObject).doubleValue());
        break;
      case -2:
        if (paramObject instanceof String)
          try
          {
            updateBytes(paramInt1, ((String)paramObject).getBytes(this.OdbcApi.charSet));
          }
          catch (UnsupportedEncodingException localUnsupportedEncodingException1)
          {
            throw new SQLException(localUnsupportedEncodingException1.getMessage());
          }
        updateBytes(paramInt1, (byte[])(byte[])paramObject);
        break;
      case -4:
      case -3:
        byte[] arrayOfByte2 = null;
        if (paramObject instanceof String)
          try
          {
            arrayOfByte2 = ((String)paramObject).getBytes(this.OdbcApi.charSet);
          }
          catch (UnsupportedEncodingException localUnsupportedEncodingException2)
          {
            throw new SQLException(localUnsupportedEncodingException2.getMessage());
          }
        arrayOfByte2 = (byte[])(byte[])paramObject;
        if (arrayOfByte2.length > 8000)
        {
          updateBinaryStream(paramInt1, new ByteArrayInputStream(arrayOfByte2), arrayOfByte2.length);
          return;
        }
        updateBytes(paramInt1, arrayOfByte2);
        break;
      case 91:
        updateDate(paramInt1, (Date)paramObject);
        break;
      case 92:
        updateTime(paramInt1, (Time)paramObject);
        break;
      case 93:
        updateTimestamp(paramInt1, (Timestamp)paramObject);
        break;
      case 0:
        updateNull(paramInt1);
        break;
      default:
        throw new SQLException("Unknown SQL Type for ResultSet.updateObject SQL Type = " + paramInt3);
      }
  }

  protected void updateChar(int paramInt1, int paramInt2, String paramString)
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if ((paramInt1 > 0) && (paramInt1 <= this.numberOfCols))
    {
      int j = getColumnType(paramInt1);
      if (j != paramInt2)
        this.boundCols[(paramInt1 - 1)].setType(paramInt2);
      if (paramString == null)
        updateNull(paramInt1);
      else
        this.boundCols[(paramInt1 - 1)].setRowValues(i, paramString, -3);
    }
  }

  public void updateNull(String paramString)
    throws SQLException
  {
    updateNull(findColumn(paramString));
  }

  public void updateBoolean(String paramString, boolean paramBoolean)
    throws SQLException
  {
    updateBoolean(findColumn(paramString), paramBoolean);
  }

  public void updateByte(String paramString, byte paramByte)
    throws SQLException
  {
    updateInt(findColumn(paramString), paramByte);
  }

  public void updateShort(String paramString, short paramShort)
    throws SQLException
  {
    updateInt(findColumn(paramString), paramShort);
  }

  public void updateInt(String paramString, int paramInt)
    throws SQLException
  {
    updateInt(findColumn(paramString), paramInt);
  }

  public void updateLong(String paramString, long paramLong)
    throws SQLException
  {
    updateFloat(findColumn(paramString), (float)paramLong);
  }

  public void updateFloat(String paramString, float paramFloat)
    throws SQLException
  {
    updateFloat(findColumn(paramString), paramFloat);
  }

  public void updateDouble(String paramString, double paramDouble)
    throws SQLException
  {
    updateDouble(findColumn(paramString), paramDouble);
  }

  public void updateBigDecimal(String paramString, BigDecimal paramBigDecimal)
    throws SQLException
  {
    updateBigDecimal(findColumn(paramString), paramBigDecimal);
  }

  public void updateString(String paramString1, String paramString2)
    throws SQLException
  {
    updateString(findColumn(paramString1), paramString2);
  }

  public void updateBytes(String paramString, byte[] paramArrayOfByte)
    throws SQLException
  {
    updateBytes(findColumn(paramString), paramArrayOfByte);
  }

  public void updateDate(String paramString, Date paramDate)
    throws SQLException
  {
    updateDate(findColumn(paramString), paramDate);
  }

  public void updateTime(String paramString, Time paramTime)
    throws SQLException
  {
    updateTime(findColumn(paramString), paramTime);
  }

  public void updateTimestamp(String paramString, Timestamp paramTimestamp)
    throws SQLException
  {
    updateTimestamp(findColumn(paramString), paramTimestamp);
  }

  public void updateAsciiStream(String paramString, InputStream paramInputStream, int paramInt)
    throws SQLException
  {
    updateAsciiStream(findColumn(paramString), paramInputStream, paramInt);
  }

  public void updateBinaryStream(String paramString, InputStream paramInputStream, int paramInt)
    throws SQLException
  {
    updateBinaryStream(findColumn(paramString), paramInputStream, paramInt);
  }

  public void updateCharacterStream(String paramString, Reader paramReader, int paramInt)
    throws SQLException
  {
    updateCharacterStream(findColumn(paramString), paramReader, paramInt);
  }

  public void updateObject(String paramString, Object paramObject, int paramInt)
    throws SQLException
  {
    updateObject(findColumn(paramString), paramObject, paramInt);
  }

  public void updateObject(String paramString, Object paramObject)
    throws SQLException
  {
    updateObject(findColumn(paramString), paramObject);
  }

  public void insertRow()
    throws SQLException
  {
    checkOpen();
    for (int i = 0; i < this.numberOfCols; ++i)
    {
      int j = this.boundCols[i].getType();
      bindCol(i + 1, j);
    }
    if (getType() != 1003)
    {
      if (this.blockCursor)
        setPos(this.currentBlockCell, 4);
      else
        setPos(this.rowSet, 4);
      FreeCols();
      if (this.ownInsertsAreVisible)
        this.numberOfRows += 1;
      resetColumnState();
      resetInsertRow();
    }
    else
    {
      throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
    }
  }

  public void updateRow()
    throws SQLException
  {
    checkOpen();
    for (int i = 0; i < this.numberOfCols; ++i)
    {
      int j = this.boundCols[i].getType();
      bindCol(i + 1, j);
    }
    if (getType() != 1003)
    {
      if (this.blockCursor)
        setPos(this.currentBlockCell, 2);
      else
        setPos(this.rowSet, 2);
      FreeCols();
      resetColumnState();
      this.rowUpdated = true;
    }
    else
    {
      throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
    }
  }

  public void deleteRow()
    throws SQLException
  {
    checkOpen();
    if (this.blockCursor)
      setPos(this.currentBlockCell, 3);
    else
      setPos(this.rowSet, 3);
    if (this.ownDeletesAreVisible)
      this.numberOfRows -= 1;
  }

  private void setResultSetVisibilityIndicators()
    throws SQLException
  {
    int i = this.OdbcApi.SQLGetStmtAttr(this.hStmt, 6);
    short s = 0;
    switch (i)
    {
    case 1:
      s = 151;
      break;
    case 2:
      s = 145;
      break;
    case 3:
      s = 168;
    }
    if (s > 0)
      try
      {
        int j = this.OdbcApi.SQLGetInfo(this.hDbc, s);
        if ((j & 0x20) > 0)
          this.ownDeletesAreVisible = true;
        if ((j & 0x10) > 0)
          this.ownInsertsAreVisible = true;
      }
      catch (SQLException localSQLException)
      {
      }
  }

  public void refreshRow()
    throws SQLException
  {
    checkOpen();
    if (getType() != 1003)
    {
      if ((!(this.atInsertRow)) && (getRow() > 0))
      {
        fetchScrollOption(0, 6);
        return;
      }
      throw new SQLException("Cursor position is invalid");
    }
    throw new SQLException("Result set type is TYPE_FORWARD_ONLY");
  }

  public void cancelRowUpdates()
    throws SQLException
  {
    checkOpen();
    if (!(this.atInsertRow))
      resetColumnState();
    else
      throw new SQLException("Cursor position on insert row");
  }

  public void moveToInsertRow()
    throws SQLException
  {
    if (getType() == 1003)
      throw new SQLException("Invalid Cursor Type: " + getType());
    checkOpen();
    this.atInsertRow = true;
    this.lastRowPosition = this.rowPosition;
    this.lastBlockPosition = this.currentBlockCell;
    if (this.blockCursor)
      this.currentBlockCell = (this.rowSet + 1);
    resetInsertRow();
  }

  public void moveToCurrentRow()
    throws SQLException
  {
    checkOpen();
    boolean bool = false;
    if (this.atInsertRow)
    {
      resetInsertRow();
      this.rowPosition = this.lastRowPosition;
      this.currentBlockCell = this.lastBlockPosition;
      bool = absolute(this.rowPosition);
      if (bool)
      {
        this.lastRowPosition = 0;
        this.lastBlockPosition = 0;
      }
      this.atInsertRow = false;
    }
  }

  public Statement getStatement()
    throws SQLException
  {
    checkOpen();
    if (this.ownerStatement != null)
      return this.ownerStatement;
    return null;
  }

  public Object getObject(int paramInt, Map<String, Class<?>> paramMap)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Ref getRef(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Blob getBlob(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Clob getClob(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Array getArray(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Object getObject(String paramString, Map<String, Class<?>> paramMap)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Ref getRef(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Blob getBlob(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Clob getClob(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Array getArray(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public URL getURL(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public URL getURL(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void updateRef(int paramInt, Ref paramRef)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void updateRef(String paramString, Ref paramRef)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void updateBlob(int paramInt, Blob paramBlob)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void updateBlob(String paramString, Blob paramBlob)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void updateClob(int paramInt, Clob paramClob)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void updateClob(String paramString, Clob paramClob)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void updateArray(int paramInt, Array paramArray)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void updateArray(String paramString, Array paramArray)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Date getDate(int paramInt, Calendar paramCalendar)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    long l = 3412047153814568960L;
    paramInt = mapColumn(paramInt);
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      return null;
    }
    l = getDataLongDate(paramInt, paramCalendar);
    if (l == 3412046672778231808L)
      return null;
    return new Date(l);
  }

  public Date getDate(String paramString, Calendar paramCalendar)
    throws SQLException
  {
    return getDate(findColumn(paramString), paramCalendar);
  }

  public Time getTime(int paramInt, Calendar paramCalendar)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    long l = 3412047153814568960L;
    paramInt = mapColumn(paramInt);
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      return null;
    }
    l = getDataLongTime(paramInt, paramCalendar);
    if (l == 3412046672778231808L)
      return null;
    return new Time(l);
  }

  public Time getTime(String paramString, Calendar paramCalendar)
    throws SQLException
  {
    return getTime(findColumn(paramString), paramCalendar);
  }

  public Timestamp getTimestamp(int paramInt, Calendar paramCalendar)
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.lastColumnNull = false;
    long l = 3412047153814568960L;
    paramInt = mapColumn(paramInt);
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      return null;
    }
    l = getDataLongTimestamp(paramInt, paramCalendar);
    if (l == 3412046672778231808L)
      return null;
    return new Timestamp(l);
  }

  public Timestamp getTimestamp(String paramString, Calendar paramCalendar)
    throws SQLException
  {
    return getTimestamp(findColumn(paramString), paramCalendar);
  }

  protected void setRowStatusPtr()
    throws SQLException
  {
    checkOpen();
    clearWarnings();
    this.rowStatusArray = new int[this.rowSet + 1];
    this.pA = new long[2];
    this.pA[0] = 3412046827397054464L;
    this.pA[1] = 3412046827397054464L;
    this.OdbcApi.SQLSetStmtAttrPtr(this.hStmt, 25, this.rowStatusArray, 0, this.pA);
  }

  protected boolean setRowArraySize()
  {
    int i = 0;
    try
    {
      clearWarnings();
      if (this.rowSet > 1)
      {
        if (this.numberOfRows < this.rowSet)
          this.rowSet = this.numberOfRows;
        this.OdbcApi.SQLSetStmtAttr(this.hStmt, 5, 0, 0);
        this.OdbcApi.SQLSetStmtAttr(this.hStmt, 27, this.rowSet, 0);
        i = this.OdbcApi.SQLGetStmtAttr(this.hStmt, 27);
        if ((i > 1) && (i < this.rowSet))
        {
          this.rowSet = i;
          return true;
        }
      }
    }
    catch (SQLException localSQLException)
    {
      return false;
    }
    return (i == this.rowSet);
  }

  protected void resetInsertRow()
    throws SQLException
  {
    checkOpen();
    int i = getRowIndex();
    if (this.atInsertRow)
      for (int j = 0; j < this.numberOfCols; ++j)
        this.boundCols[j].resetColumnToIgnoreData();
  }

  protected void resetColumnState()
    throws SQLException
  {
    checkOpen();
    if (this.hStmt != 3412046672778231808L)
      this.OdbcApi.SQLFreeStmt(this.hStmt, 2);
    for (int i = 0; i < this.numberOfCols; ++i)
      this.boundCols[i].resetColumnToIgnoreData();
  }

  protected void bindCol(int paramInt1, int paramInt2)
    throws SQLException
  {
    int i = 0;
    int j = 0;
    Object localObject1 = null;
    Object[] arrayOfObject = this.boundCols[(paramInt1 - 1)].getRowValues();
    byte[] arrayOfByte = this.boundCols[(paramInt1 - 1)].getRowLengths();
    if ((!(this.blockCursor)) && (this.atInsertRow))
      j = 0;
    if (this.blockCursor)
      j = this.currentBlockCell - 1;
    Object localObject2 = this.boundCols[(paramInt1 - 1)].getRowValue(j);
    int k = this.boundCols[(paramInt1 - 1)].getLength();
    if (k < 0)
      k = getColumnLength(paramInt1);
    try
    {
      switch (paramInt2)
      {
      case 1:
      case 2:
      case 3:
      case 12:
        if ((((paramInt2 == 2) || (paramInt2 == 3))) && (localObject2 != null))
        {
          Object localObject3 = localObject2;
          String str = localObject3.toString();
          k = str.length();
          BigDecimal localBigDecimal = new BigDecimal(str);
          int i1 = localBigDecimal.scale();
          if (i1 <= 0);
        }
        bindStringCol(paramInt1, paramInt2, arrayOfObject, arrayOfByte, ++k);
        break;
      case -1:
        int l = JdbcOdbcStatement.getTypeFromObject(localObject2);
        if ((l == -4) || (l == 0))
          bindAtExecCol(paramInt1, paramInt2, arrayOfByte);
        else if (l == -2)
          bindBinaryCol(paramInt1, arrayOfObject, arrayOfByte, k);
        break;
      case -7:
      case -6:
      case 4:
      case 5:
        bindIntegerCol(paramInt1, arrayOfObject, arrayOfByte);
        break;
      case 8:
        bindDoubleCol(paramInt1, arrayOfObject, arrayOfByte);
        break;
      case -5:
      case 6:
      case 7:
        bindFloatCol(paramInt1, arrayOfObject, arrayOfByte);
        break;
      case 91:
        bindDateCol(paramInt1, arrayOfObject, arrayOfByte);
        break;
      case 92:
        bindTimeCol(paramInt1, arrayOfObject, arrayOfByte);
        break;
      case 93:
        bindTimestampCol(paramInt1, arrayOfObject, arrayOfByte);
        break;
      case -3:
      case -2:
        bindBinaryCol(paramInt1, arrayOfObject, arrayOfByte, k);
        break;
      case -4:
        bindAtExecCol(paramInt1, paramInt2, arrayOfByte);
      }
    }
    catch (SQLException localSQLException)
    {
      throw new SQLException("SQLBinCol (" + paramInt1 + ") SQLType = " + paramInt2 + ". " + localSQLException.getMessage());
    }
  }

  protected void bindStringCol(int paramInt1, int paramInt2, Object[] paramArrayOfObject, byte[] paramArrayOfByte, int paramInt3)
    throws SQLException
  {
    byte[] arrayOfByte = this.boundCols[(paramInt1 - 1)].allocBindDataBuffer((paramInt3 + 1) * paramArrayOfObject.length);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    this.OdbcApi.SQLBindColString(this.hStmt, paramInt1, paramInt2, paramArrayOfObject, paramInt3, paramArrayOfByte, arrayOfByte, arrayOfLong);
    this.boundCols[(paramInt1 - 1)].pA1 = arrayOfLong[0];
    this.boundCols[(paramInt1 - 1)].pA2 = arrayOfLong[1];
    this.boundCols[(paramInt1 - 1)].pC1 = arrayOfLong[2];
    this.boundCols[(paramInt1 - 1)].pC2 = arrayOfLong[3];
  }

  protected void bindIntegerCol(int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte)
    throws SQLException
  {
    byte[] arrayOfByte = this.boundCols[(paramInt - 1)].allocBindDataBuffer(4 * paramArrayOfObject.length);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    this.OdbcApi.SQLBindColInteger(this.hStmt, paramInt, paramArrayOfObject, paramArrayOfByte, arrayOfByte, arrayOfLong);
    this.boundCols[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundCols[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundCols[(paramInt - 1)].pC1 = arrayOfLong[2];
    this.boundCols[(paramInt - 1)].pC2 = arrayOfLong[3];
  }

  protected void bindFloatCol(int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte)
    throws SQLException
  {
    byte[] arrayOfByte = this.boundCols[(paramInt - 1)].allocBindDataBuffer(8 * paramArrayOfObject.length);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    this.OdbcApi.SQLBindColFloat(this.hStmt, paramInt, paramArrayOfObject, paramArrayOfByte, arrayOfByte, arrayOfLong);
    this.boundCols[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundCols[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundCols[(paramInt - 1)].pC1 = arrayOfLong[2];
    this.boundCols[(paramInt - 1)].pC2 = arrayOfLong[3];
  }

  protected void bindDoubleCol(int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte)
    throws SQLException
  {
    byte[] arrayOfByte = this.boundCols[(paramInt - 1)].allocBindDataBuffer(8 * paramArrayOfObject.length);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    this.OdbcApi.SQLBindColDouble(this.hStmt, paramInt, paramArrayOfObject, paramArrayOfByte, arrayOfByte, arrayOfLong);
    this.boundCols[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundCols[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundCols[(paramInt - 1)].pC1 = arrayOfLong[2];
    this.boundCols[(paramInt - 1)].pC2 = arrayOfLong[3];
  }

  protected void bindDateCol(int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte)
    throws SQLException
  {
    byte[] arrayOfByte = this.boundCols[(paramInt - 1)].allocBindDataBuffer(10 * paramArrayOfObject.length);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    this.OdbcApi.SQLBindColDate(this.hStmt, paramInt, paramArrayOfObject, paramArrayOfByte, arrayOfByte, arrayOfLong);
    this.boundCols[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundCols[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundCols[(paramInt - 1)].pC1 = arrayOfLong[2];
    this.boundCols[(paramInt - 1)].pC2 = arrayOfLong[3];
  }

  protected void bindTimeCol(int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte)
    throws SQLException
  {
    byte[] arrayOfByte = this.boundCols[(paramInt - 1)].allocBindDataBuffer(9 * paramArrayOfObject.length);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    this.OdbcApi.SQLBindColTime(this.hStmt, paramInt, paramArrayOfObject, paramArrayOfByte, arrayOfByte, arrayOfLong);
    this.boundCols[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundCols[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundCols[(paramInt - 1)].pC1 = arrayOfLong[2];
    this.boundCols[(paramInt - 1)].pC2 = arrayOfLong[3];
  }

  protected void bindTimestampCol(int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte)
    throws SQLException
  {
    byte[] arrayOfByte = this.boundCols[(paramInt - 1)].allocBindDataBuffer(30 * paramArrayOfObject.length);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    this.OdbcApi.SQLBindColTimestamp(this.hStmt, paramInt, paramArrayOfObject, paramArrayOfByte, arrayOfByte, arrayOfLong);
    this.boundCols[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundCols[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundCols[(paramInt - 1)].pC1 = arrayOfLong[2];
    this.boundCols[(paramInt - 1)].pC2 = arrayOfLong[3];
  }

  protected void bindBinaryCol(int paramInt1, Object[] paramArrayOfObject, byte[] paramArrayOfByte, int paramInt2)
    throws SQLException
  {
    byte[] arrayOfByte = this.boundCols[(paramInt1 - 1)].allocBindDataBuffer((paramInt2 + 1) * paramArrayOfObject.length);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    this.OdbcApi.SQLBindColBinary(this.hStmt, paramInt1, paramArrayOfObject, paramArrayOfByte, paramInt2, arrayOfByte, arrayOfLong);
    this.boundCols[(paramInt1 - 1)].pA1 = arrayOfLong[0];
    this.boundCols[(paramInt1 - 1)].pA2 = arrayOfLong[1];
    this.boundCols[(paramInt1 - 1)].pC1 = arrayOfLong[2];
    this.boundCols[(paramInt1 - 1)].pC2 = arrayOfLong[3];
  }

  protected void bindAtExecCol(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    throws SQLException
  {
    byte[] arrayOfByte = this.boundCols[(paramInt1 - 1)].allocBindDataBuffer(4);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    this.OdbcApi.SQLBindColAtExec(this.hStmt, paramInt1, paramInt2, paramArrayOfByte, arrayOfByte, arrayOfLong);
    this.boundCols[(paramInt1 - 1)].pA1 = arrayOfLong[0];
    this.boundCols[(paramInt1 - 1)].pA2 = arrayOfLong[1];
    this.boundCols[(paramInt1 - 1)].pC1 = arrayOfLong[2];
    this.boundCols[(paramInt1 - 1)].pC2 = arrayOfLong[3];
  }

  protected void setPos(int paramInt1, int paramInt2)
    throws SQLException
  {
    Object localObject = null;
    boolean bool = false;
    try
    {
      clearWarnings();
      bool = this.OdbcApi.SQLSetPos(this.hStmt, paramInt1, paramInt2, 0);
      int i = 0;
      while (bool)
      {
        int j = getRowIndex();
        String str = this.OdbcApi.odbcDriverName;
        if ((this.blockCursor) && (str.indexOf("(IV") == -1))
          i = this.OdbcApi.SQLParamDataInBlock(this.hStmt, j);
        else
          i = this.OdbcApi.SQLParamData(this.hStmt);
        if (i == -1)
          bool = false;
        else
          putColumnData(i);
      }
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      throw new SQLException(localSQLException.getMessage());
    }
  }

  protected void putColumnData(int paramInt)
    throws SQLException, JdbcOdbcSQLWarning
  {
    int i = 2000;
    byte[] arrayOfByte = new byte[i];
    int l = 0;
    if ((paramInt < 1) || (paramInt > this.numberOfCols))
    {
      if (this.OdbcApi.getTracer().isTracing())
        this.OdbcApi.getTracer().trace("Invalid index for putColumnData()");
      return;
    }
    InputStream localInputStream = null;
    int i1 = getRowIndex();
    try
    {
      localInputStream = (InputStream)this.boundCols[(paramInt - 1)].getRowValue(i1);
    }
    catch (Exception localException)
    {
      throw new SQLException("Invalid data for columnIndex(" + paramInt + "): " + localException.getMessage());
    }
    int i2 = this.boundCols[(paramInt - 1)].getLength();
    int i3 = this.boundCols[(paramInt - 1)].getStreamType();
    while (true)
    {
      do
      {
        int j;
        if (l != 0)
          return;
        try
        {
          if (this.OdbcApi.getTracer().isTracing())
            this.OdbcApi.getTracer().trace("Reading from input stream");
          j = localInputStream.read(arrayOfByte);
          if (this.OdbcApi.getTracer().isTracing())
            this.OdbcApi.getTracer().trace("Bytes read: " + j);
        }
        catch (IOException localIOException)
        {
          throw new SQLException(localIOException.getMessage());
        }
        if (j == -1)
        {
          if (i2 != 0)
            throw new SQLException("End of InputStream reached before satisfying length specified when InputStream was set");
          l = 1;
          return;
        }
        if (j > i2)
        {
          j = i2;
          l = 1;
        }
        int k = j;
        try
        {
          this.OdbcApi.SQLPutData(this.hStmt, arrayOfByte, k);
        }
        catch (SQLWarning localSQLWarning)
        {
          setWarning(localSQLWarning);
        }
        catch (SQLException localSQLException)
        {
        }
        i2 -= j;
        if (this.OdbcApi.getTracer().isTracing())
          this.OdbcApi.getTracer().trace("" + i2 + " bytes remaining");
      }
      while (i2 != 0);
      l = 1;
    }
  }

  public int getColAttribute(int paramInt1, int paramInt2)
    throws SQLException
  {
    int i = 0;
    clearWarnings();
    try
    {
      i = this.OdbcApi.SQLColAttributes(this.hStmt, paramInt1, paramInt2);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      BigDecimal localBigDecimal = (BigDecimal)localJdbcOdbcSQLWarning.value;
      i = localBigDecimal.intValue();
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    return i;
  }

  protected int getMaxCharLen(int paramInt)
    throws SQLException
  {
    int i = getColumnType(paramInt);
    int j = getColumnLength(paramInt);
    if (j != -1)
    {
      switch (i)
      {
      case -4:
      case -3:
      case -2:
        j *= 2;
        break;
      case 91:
        j = 10;
        break;
      case 92:
        j = 8;
        break;
      case 93:
        j = 29;
        break;
      case 2:
      case 3:
        j += 2;
        break;
      case -7:
        j = 1;
        break;
      case -6:
        j = 4;
        break;
      case 5:
        j = 6;
        break;
      case 4:
        j = 11;
        break;
      case -5:
        j = 20;
        break;
      case 7:
        j = 13;
        break;
      case 6:
      case 8:
        j = 22;
      }
      if ((j <= 0) || (j > 32767))
        j = 32767;
    }
    return j;
  }

  protected int getMaxBinaryLen(int paramInt)
    throws SQLException
  {
    int i = getColumnLength(paramInt);
    if ((i != -1) && (((i <= 0) || (i > 32767))))
      i = 32767;
    return i;
  }

  public Double getDataDouble(int paramInt)
    throws SQLException
  {
    Double localDouble;
    this.lastColumnNull = false;
    try
    {
      localDouble = this.OdbcApi.SQLGetDataDouble(this.hStmt, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      localDouble = (Double)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (localDouble == null)
      this.lastColumnNull = true;
    return localDouble;
  }

  public Float getDataFloat(int paramInt)
    throws SQLException
  {
    Float localFloat;
    this.lastColumnNull = false;
    try
    {
      localFloat = this.OdbcApi.SQLGetDataFloat(this.hStmt, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      localFloat = (Float)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (localFloat == null)
      this.lastColumnNull = true;
    return localFloat;
  }

  public Integer getDataInteger(int paramInt)
    throws SQLException
  {
    Integer localInteger;
    this.lastColumnNull = false;
    try
    {
      localInteger = this.OdbcApi.SQLGetDataInteger(this.hStmt, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      localInteger = (Integer)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (localInteger == null)
      this.lastColumnNull = true;
    else if (paramInt == this.sqlTypeColumn)
      localInteger = new Integer(OdbcDef.odbcTypeToJdbc(localInteger.intValue()));
    return localInteger;
  }

  public Long getDataLong(int paramInt)
    throws SQLException
  {
    Long localLong = null;
    Double localDouble = getDataDouble(paramInt);
    if (localDouble != null)
      localLong = new Long(localDouble.longValue());
    return localLong;
  }

  public String getDataString(int paramInt1, int paramInt2, boolean paramBoolean)
    throws SQLException
  {
    String str;
    this.lastColumnNull = false;
    try
    {
      str = this.OdbcApi.SQLGetDataString(this.hStmt, paramInt1, paramInt2, paramBoolean);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      str = (String)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (str == null)
      this.lastColumnNull = true;
    else if (paramInt1 == this.sqlTypeColumn)
      try
      {
        int i = OdbcDef.odbcTypeToJdbc(Integer.valueOf(str).intValue());
        str = "" + i;
      }
      catch (Exception localException)
      {
      }
    return str;
  }

  public String getDataStringDate(int paramInt)
    throws SQLException
  {
    String str;
    this.lastColumnNull = false;
    try
    {
      str = this.OdbcApi.SQLGetDataStringDate(this.hStmt, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      str = (String)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (str == null)
      this.lastColumnNull = true;
    return str;
  }

  public String getDataStringTime(int paramInt)
    throws SQLException
  {
    String str;
    this.lastColumnNull = false;
    try
    {
      str = this.OdbcApi.SQLGetDataStringTime(this.hStmt, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      str = (String)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (str == null)
      this.lastColumnNull = true;
    return str;
  }

  public String getDataStringTimestamp(int paramInt)
    throws SQLException
  {
    String str;
    this.lastColumnNull = false;
    try
    {
      str = this.OdbcApi.SQLGetDataStringTimestamp(this.hStmt, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      str = (String)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (str == null)
      this.lastColumnNull = true;
    return str;
  }

  public long getDataLongDate(int paramInt, Calendar paramCalendar)
    throws SQLException
  {
    String str;
    this.lastColumnNull = false;
    Date localDate = null;
    long l = 3412047153814568960L;
    try
    {
      str = this.OdbcApi.SQLGetDataStringDate(this.hStmt, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      str = (String)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (str != null)
    {
      localDate = Date.valueOf(str);
      l = this.utils.convertFromGMT(localDate, paramCalendar);
    }
    else if (str == null)
    {
      this.lastColumnNull = true;
    }
    return l;
  }

  public long getDataLongTime(int paramInt, Calendar paramCalendar)
    throws SQLException
  {
    String str;
    this.lastColumnNull = false;
    Time localTime = null;
    long l = 3412047153814568960L;
    try
    {
      str = this.OdbcApi.SQLGetDataStringTime(this.hStmt, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      str = (String)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (str != null)
    {
      localTime = Time.valueOf(str);
      l = this.utils.convertFromGMT(localTime, paramCalendar);
    }
    else if (str == null)
    {
      this.lastColumnNull = true;
    }
    return l;
  }

  public long getDataLongTimestamp(int paramInt, Calendar paramCalendar)
    throws SQLException
  {
    String str;
    this.lastColumnNull = false;
    Timestamp localTimestamp = null;
    long l = 3412047153814568960L;
    try
    {
      str = this.OdbcApi.SQLGetDataStringTimestamp(this.hStmt, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      str = (String)localJdbcOdbcSQLWarning.value;
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    if (str != null)
    {
      localTimestamp = Timestamp.valueOf(str);
      l = this.utils.convertFromGMT(localTimestamp, paramCalendar);
    }
    else if (str == null)
    {
      this.lastColumnNull = true;
    }
    return l;
  }

  public int getColumnLength(int paramInt)
    throws SQLException
  {
    int i = -1;
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
      i = this.boundCols[(paramInt - 1)].getLength();
    if (i == -1)
    {
      i = getColAttribute(paramInt, 3);
      if ((paramInt > 0) && (paramInt <= this.numberOfCols))
        this.boundCols[(paramInt - 1)].setLength(i);
    }
    return i;
  }

  public int getScale(int paramInt)
    throws SQLException
  {
    int i;
    if (getPseudoCol(paramInt) != null)
    {
      this.lastColumnNull = true;
      i = 0;
    }
    else
    {
      i = getColAttribute(paramInt, 5);
    }
    return i;
  }

  public int getColumnType(int paramInt)
    throws SQLException
  {
    int i = 9999;
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
      i = this.boundCols[(paramInt - 1)].getType();
    if (i == 9999)
    {
      i = getColAttribute(paramInt, 2);
      i = OdbcDef.odbcTypeToJdbc(i);
      if ((paramInt > 0) && (paramInt <= this.numberOfCols))
        this.boundCols[(paramInt - 1)].setType(i);
    }
    return i;
  }

  public void setPseudoCols(int paramInt1, int paramInt2, JdbcOdbcPseudoCol[] paramArrayOfJdbcOdbcPseudoCol)
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("Setting pseudo columns, first=" + paramInt1 + ", last=" + paramInt2);
    this.firstPseudoCol = paramInt1;
    this.lastPseudoCol = paramInt2;
    this.pseudoCols = paramArrayOfJdbcOdbcPseudoCol;
  }

  public JdbcOdbcPseudoCol getPseudoCol(int paramInt)
  {
    JdbcOdbcPseudoCol localJdbcOdbcPseudoCol = null;
    if ((paramInt > 0) && (paramInt >= this.firstPseudoCol) && (paramInt <= this.lastPseudoCol))
      localJdbcOdbcPseudoCol = this.pseudoCols[(paramInt - this.firstPseudoCol)];
    return localJdbcOdbcPseudoCol;
  }

  public void setSQLTypeColumn(int paramInt)
  {
    this.sqlTypeColumn = paramInt;
  }

  protected void setInputStream(int paramInt, JdbcOdbcInputStream paramJdbcOdbcInputStream)
  {
    if ((paramInt > 0) && (paramInt <= this.numberOfCols))
      this.boundCols[(paramInt - 1)].setInputStream(paramJdbcOdbcInputStream);
  }

  protected void closeInputStreams()
  {
    for (int i = 0; i < this.numberOfCols; ++i)
      this.boundCols[i].closeInputStream();
  }

  public void setColumnMappings(int[] paramArrayOfInt)
  {
    this.colMappings = paramArrayOfInt;
  }

  public int mapColumn(int paramInt)
  {
    int i = paramInt;
    if (this.colMappings != null)
      if ((paramInt > 0) && (paramInt <= this.colMappings.length))
        i = this.colMappings[(paramInt - 1)];
      else
        i = -1;
    return i;
  }

  protected void calculateRowCount()
    throws SQLException
  {
    Object localObject1;
    try
    {
      this.numberOfRows = this.OdbcApi.SQLRowCount(this.hStmt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      localObject1 = (BigDecimal)localJdbcOdbcSQLWarning.value;
      this.numberOfRows = ((BigDecimal)localObject1).intValue();
    }
    if (this.numberOfRows > 0)
      return;
    try
    {
      this.OdbcApi.SQLFetchScroll(this.hStmt, 3, 0);
      this.numberOfRows = (int)this.OdbcApi.SQLGetStmtOption(this.hStmt, 14);
    }
    catch (SQLException localSQLException)
    {
    }
    finally
    {
      this.OdbcApi.SQLFetchScroll(this.hStmt, 5, 0);
    }
    if (this.numberOfRows > 0)
    {
      this.OdbcApi.SQLFetchScroll(this.hStmt, 5, 0);
      return;
    }
    if (this.ownerStatement != null)
    {
      Connection localConnection = this.ownerStatement.getConnection();
      localObject1 = this.ownerStatement.getSql();
      String str1 = null;
      String str2 = this.ownerStatement.getClass().getName();
      int i = 0;
      i = this.ownerStatement.getParamCount();
      if (i > 0)
      {
        if (str2.indexOf("CallableStatement") > 0)
          throw new SQLException("Unable to obtain result set row count. From " + ((String)localObject1));
        if (str2.indexOf("PreparedStatement") > 0)
        {
          if (((String)localObject1).toLowerCase().indexOf("select") == -1)
            throw new SQLException("Cannot obtain result set row count for " + ((String)localObject1));
          str1 = reWordAsCountQuery((String)localObject1);
          if (str1.indexOf("?") > 0)
            this.numberOfRows = parameterQuery(localConnection.prepareStatement(str1));
          else
            i = 0;
        }
      }
      if ((str2.indexOf("Statement") > 0) && (i == 0))
      {
        Statement localStatement = localConnection.createStatement();
        if ((localObject1 != null) && (((String)localObject1).startsWith("SELECT")))
        {
          if (str1 == null)
            str1 = reWordAsCountQuery((String)localObject1);
          ResultSet localResultSet = localStatement.executeQuery(str1);
          localResultSet.next();
          this.numberOfRows = localResultSet.getInt(1);
          if ((str1.indexOf("COUNT(*)") < 0) && (this.numberOfRows > 0))
          {
            this.numberOfRows = 1;
            setWarning(new SQLWarning("ResultSet is not updatable."));
          }
        }
        if (localStatement != null)
          localStatement.close();
      }
    }
    if (this.numberOfRows > 0)
      return;
    setWarning(new SQLWarning("Can not determine result set row count."));
  }

  protected int parameterQuery(PreparedStatement paramPreparedStatement)
    throws SQLException
  {
    int i = 0;
    Object[] arrayOfObject = null;
    int[] arrayOfInt = null;
    if (paramPreparedStatement != null)
    {
      try
      {
        arrayOfObject = this.ownerStatement.getObjects();
        arrayOfInt = this.ownerStatement.getObjectTypes();
        for (int j = 0; j < arrayOfObject.length; ++j)
          paramPreparedStatement.setObject(j + 1, arrayOfObject[j], arrayOfInt[j]);
      }
      catch (Exception localException)
      {
        throw new SQLException("while calculating row count: " + localException.getMessage());
      }
      ResultSet localResultSet = paramPreparedStatement.executeQuery();
      localResultSet.next();
      i = localResultSet.getInt(1);
      paramPreparedStatement.close();
    }
    return i;
  }

  protected String reWordAsCountQuery(String paramString)
  {
    int i = paramString.indexOf(" COUNT(*) ");
    int j = -1;
    int k = paramString.indexOf(" FROM ");
    int l = paramString.indexOf("'");
    int i1 = -1;
    if (l > 0)
      i1 = paramString.indexOf("'", l + 2);
    if ((k > l) && (i1 > k))
      k = paramString.indexOf(" FROM ", i1);
    if ((i > l) && (i1 > i))
      i = -1;
    int i2 = -1;
    int i3 = -1;
    int i4 = -1;
    int i5 = -1;
    int i6 = -1;
    int i7 = paramString.indexOf("WHERE");
    int i8 = -1;
    if (i7 < k)
      i7 = paramString.indexOf("WHERE", i7 + 2);
    String str1 = "";
    if (i6 < 0)
    {
      i2 = paramString.lastIndexOf("ORDER BY");
      if (i2 > i7)
        i6 = i2;
      str1 = "ORDER BY";
    }
    if (i6 < 0)
    {
      i3 = paramString.lastIndexOf("GROUP BY");
      if ((i3 > i7) && (i3 > i2))
        i6 = i3;
      str1 = "GROUP BY";
    }
    if (i6 < 0)
    {
      i4 = paramString.lastIndexOf("FOR UPDATE");
      if ((i4 > i7) && (i4 > i3))
        i6 = i4;
      str1 = "FOR UPDATE";
    }
    if (i6 < 0)
    {
      i5 = paramString.lastIndexOf("UNION");
      if ((i5 > i7) && (i5 > i4))
        i6 = i5;
      str1 = "UNION";
    }
    if ((i6 > 0) && (i6 > k))
      if ((i7 > 0) && (i7 > k))
      {
        int i9 = paramString.indexOf("'", i7);
        int i10 = -1;
        if (i9 > 0)
          i10 = paramString.indexOf("'", i9 + 2);
        if ((i6 > i9) && (i1 > i6))
          i6 = paramString.indexOf(str1, i10);
        if (i6 > i10)
          paramString = paramString.substring(0, i6);
      }
      else
      {
        paramString = paramString.substring(0, i6);
      }
    String str2 = paramString.substring(0, k);
    StringBuffer localStringBuffer = new StringBuffer(paramString);
    if ((i < 0) && (j < 0))
    {
      j = str2.lastIndexOf(")");
      if (j > 0)
      {
        int i11 = paramString.indexOf(" (");
        if (i11 > 0)
        {
          if ((i11 < l) && (i11 < i1))
            j = -1;
        }
        else if (i11 < 0)
        {
          j = -1;
          i = i11;
        }
      }
    }
    if (j > 0)
      localStringBuffer.insert(6, " COUNT(*), ");
    else if ((i < 0) && (k > 0))
      localStringBuffer.replace(6, k, " COUNT(*) ");
    return localStringBuffer.toString();
  }

  protected void setCursorType()
    throws SQLException
  {
    clearWarnings();
    try
    {
      long l = this.OdbcApi.SQLGetStmtOption(this.hStmt, 6);
      this.odbcCursorType = (short)(int)l;
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      BigDecimal localBigDecimal = (BigDecimal)localJdbcOdbcSQLWarning.value;
      this.odbcCursorType = localBigDecimal.shortValue();
      setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
  }

  protected void checkOpen()
    throws SQLException
  {
    if (this.closed)
      throw new SQLException("ResultSet is closed");
  }

  public SQLXML getSQLXML(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public SQLXML getSQLXML(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public RowId getRowId(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public RowId getRowId(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateRowId(int paramInt, RowId paramRowId)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateRowId(String paramString, RowId paramRowId)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public int getHoldability()
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public boolean isClosed()
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateNString(int paramInt, String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateNString(String paramString1, String paramString2)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateNClob(int paramInt, NClob paramNClob)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateNClob(String paramString, NClob paramNClob)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public NClob getNClob(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public NClob getNClob(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
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

  public Reader getNCharacterStream(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public Reader getNCharacterStream(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateSQLXML(int paramInt, SQLXML paramSQLXML)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateSQLXML(String paramString, SQLXML paramSQLXML)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public String getNString(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public String getNString(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateNCharacterStream(int paramInt1, Reader paramReader, int paramInt2)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateNCharacterStream(String paramString, Reader paramReader, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void updateNClob(String paramString, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void updateNClob(int paramInt, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void updateClob(String paramString, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void updateClob(int paramInt, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void updateBlob(String paramString, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void updateBlob(int paramInt, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void updateCharacterStream(String paramString, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void updateBinaryStream(String paramString, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void updateAsciiStream(String paramString, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void updateCharacterStream(int paramInt, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void updateBinaryStream(int paramInt, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void updateAsciiStream(int paramInt, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void updateNCharacterStream(String paramString, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void updateNCharacterStream(int paramInt, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void updateNCharacterStream(int paramInt, Reader paramReader)
    throws SQLException
  {
  }

  public void updateNCharacterStream(String paramString, Reader paramReader)
    throws SQLException
  {
  }

  public void updateAsciiStream(int paramInt, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void updateBinaryStream(int paramInt, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void updateCharacterStream(int paramInt, Reader paramReader)
    throws SQLException
  {
  }

  public void updateAsciiStream(String paramString, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void updateBinaryStream(String paramString, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void updateCharacterStream(String paramString, Reader paramReader)
    throws SQLException
  {
  }

  public void updateBlob(int paramInt, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void updateBlob(String paramString, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void updateClob(int paramInt, Reader paramReader)
    throws SQLException
  {
  }

  public void updateClob(String paramString, Reader paramReader)
    throws SQLException
  {
  }

  public void updateNClob(int paramInt, Reader paramReader)
    throws SQLException
  {
  }

  public void updateNClob(String paramString, Reader paramReader)
    throws SQLException
  {
  }
}
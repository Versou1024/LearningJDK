package sun.jdbc.odbc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

public class JdbcOdbcPreparedStatement extends JdbcOdbcStatement
  implements PreparedStatement
{
  protected int numParams;
  protected JdbcOdbcBoundParam[] boundParams;
  protected JdbcOdbcBoundArrayOfParams arrayParams;
  protected Vector batchSqlVec;
  protected boolean batchSupport;
  protected boolean batchParamsOn;
  protected int batchSize;
  protected int arrayDef;
  protected int arrayScale;
  protected int StringDef;
  protected int NumberDef;
  protected int NumberScale;
  protected int batchRCFlag;
  protected int[] paramsProcessed;
  protected int[] paramStatusArray;
  protected long[] pA1;
  protected long[] pA2;
  protected int binaryPrec;
  protected JdbcOdbcUtils utils = new JdbcOdbcUtils();

  public JdbcOdbcPreparedStatement(JdbcOdbcConnectionInterface paramJdbcOdbcConnectionInterface)
  {
    super(paramJdbcOdbcConnectionInterface);
  }

  public void initialize(JdbcOdbc paramJdbcOdbc, long paramLong1, long paramLong2, Hashtable paramHashtable, int paramInt1, int paramInt2)
    throws SQLException
  {
    super.initialize(paramJdbcOdbc, paramLong1, paramLong2, paramHashtable, paramInt1, paramInt2);
  }

  public ResultSet executeQuery()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.executeQuery");
    ResultSet localResultSet = null;
    if (execute())
      localResultSet = getResultSet(false);
    else
      throw new SQLException("No ResultSet was produced");
    return localResultSet;
  }

  public ResultSet executeQuery(String paramString)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.executeQuery (" + paramString + ")");
    throw new SQLException("Driver does not support this function", "IM001");
  }

  public int executeUpdate()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.executeUpdate");
    int i = -1;
    if (!(execute()))
      i = getUpdateCount();
    else
      throw new SQLException("No row count was produced");
    return i;
  }

  public int executeUpdate(String paramString)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.executeUpdate (" + paramString + ")");
    throw new SQLException("Driver does not support this function", "IM001");
  }

  public boolean execute(String paramString)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.execute (" + paramString + ")");
    throw new SQLException("Driver does not support this function", "IM001");
  }

  public synchronized boolean execute()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.execute");
    int i = 0;
    Object localObject = null;
    boolean bool = false;
    clearWarnings();
    reset();
    try
    {
      bool = this.OdbcApi.SQLExecute(this.hStmt);
      while (bool)
      {
        int j = this.OdbcApi.SQLParamData(this.hStmt);
        if (j == -1)
        {
          bool = false;
        }
        else
        {
          if (this.batchParamsOn)
          {
            InputStream localInputStream = null;
            int k = this.paramsProcessed[0];
            localInputStream = this.arrayParams.getInputStreamElement(j, k);
            this.boundParams[(j - 1)].setInputStream(localInputStream, this.arrayParams.getElementLength(j, k));
          }
          putParamData(j);
        }
      }
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    if (getColumnCount() > 0)
      i = 1;
    return i;
  }

  public void setNull(int paramInt1, int paramInt2)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.setNull (" + paramInt1 + "," + paramInt2 + ")");
    clearParameter(paramInt1);
    setInputParameter(paramInt1, true);
    byte[] arrayOfByte = getLengthBuf(paramInt1);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    int i = 0;
    int j = 0;
    if ((paramInt2 == 1) || (paramInt2 == 12))
    {
      i = this.StringDef;
    }
    else if ((paramInt2 == 2) || (paramInt2 == 3))
    {
      i = this.NumberDef;
      j = this.NumberScale;
    }
    else if ((paramInt2 == -2) || (paramInt2 == -3) || (paramInt2 == -4))
    {
      paramInt2 = this.boundParams[(paramInt1 - 1)].boundType;
      i = this.binaryPrec;
    }
    if (i <= 0)
      i = getPrecision(paramInt2);
    if (i <= 0)
      i = 1;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterNull(this.hStmt, paramInt1, paramInt2, i, j, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt1 - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt1 - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt1 - 1)].scale = j;
    this.boundParams[(paramInt1 - 1)].boundType = paramInt2;
    this.boundParams[(paramInt1 - 1)].boundValue = null;
    this.arrayParams.storeValue(paramInt1 - 1, null, -1);
    setSqlType(paramInt1, paramInt2);
  }

  public void setBoolean(int paramInt, boolean paramBoolean)
    throws SQLException
  {
    int i = 0;
    if (paramBoolean)
      i = 1;
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 4);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterInteger(this.hStmt, paramInt, -7, i, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundType = -7;
    this.boundParams[(paramInt - 1)].boundValue = new Boolean(paramBoolean);
    this.arrayParams.storeValue(paramInt - 1, new Boolean(paramBoolean), 0);
    setSqlType(paramInt, -7);
  }

  public void setByte(int paramInt, byte paramByte)
    throws SQLException
  {
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 4);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterInteger(this.hStmt, paramInt, -6, paramByte, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundType = -6;
    this.boundParams[(paramInt - 1)].boundValue = new Byte(paramByte);
    this.arrayParams.storeValue(paramInt - 1, new Byte(paramByte), 0);
    setSqlType(paramInt, -6);
  }

  public void setShort(int paramInt, short paramShort)
    throws SQLException
  {
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 4);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterInteger(this.hStmt, paramInt, 5, paramShort, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundType = 5;
    this.boundParams[(paramInt - 1)].boundValue = new Short(paramShort);
    this.arrayParams.storeValue(paramInt - 1, new Short(paramShort), 0);
    setSqlType(paramInt, 5);
  }

  public void setInt(int paramInt1, int paramInt2)
    throws SQLException
  {
    clearParameter(paramInt1);
    setInputParameter(paramInt1, true);
    byte[] arrayOfByte = allocBindBuf(paramInt1, 4);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterInteger(this.hStmt, paramInt1, 4, paramInt2, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt1 - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt1 - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt1 - 1)].boundType = 4;
    this.boundParams[(paramInt1 - 1)].boundValue = new Integer(paramInt2);
    this.arrayParams.storeValue(paramInt1 - 1, new Integer(paramInt2), 0);
    setSqlType(paramInt1, 4);
  }

  public void setLong(int paramInt, long paramLong)
    throws SQLException
  {
    clearParameter(paramInt);
    if (this.myConnection.getODBCVer() == 2)
    {
      setChar(paramInt, -5, new Long(paramLong).intValue(), String.valueOf(paramLong));
    }
    else if (this.myConnection.getODBCVer() >= 3)
    {
      setInputParameter(paramInt, true);
      byte[] arrayOfByte = allocBindBuf(paramInt, 8);
      long[] arrayOfLong = new long[2];
      arrayOfLong[0] = 3412047600491167744L;
      arrayOfLong[1] = 3412047600491167744L;
      if (!(this.batchOn))
        this.OdbcApi.SQLBindInParameterBigint(this.hStmt, paramInt, -5, 0, paramLong, arrayOfByte, arrayOfLong);
      this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
      this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
      this.arrayParams.storeValue(paramInt - 1, new BigInteger(String.valueOf(paramLong)), 0);
      setSqlType(paramInt, -5);
    }
    this.boundParams[(paramInt - 1)].boundType = -5;
    this.boundParams[(paramInt - 1)].boundValue = new BigInteger(String.valueOf(paramLong));
  }

  public void setReal(int paramInt, float paramFloat)
    throws SQLException
  {
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 8);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterFloat(this.hStmt, paramInt, 7, 0, paramFloat, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.arrayParams.storeValue(paramInt - 1, new Float(paramFloat), 0);
    setSqlType(paramInt, 7);
  }

  public void setFloat(int paramInt, float paramFloat)
    throws SQLException
  {
    setDouble(paramInt, paramFloat);
  }

  public void setDouble(int paramInt, double paramDouble)
    throws SQLException
  {
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 8);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterDouble(this.hStmt, paramInt, 8, 0, paramDouble, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundType = 8;
    this.boundParams[(paramInt - 1)].boundValue = new Double(paramDouble);
    this.arrayParams.storeValue(paramInt - 1, new Double(paramDouble), 0);
    setSqlType(paramInt, 8);
  }

  public void setBigDecimal(int paramInt, BigDecimal paramBigDecimal)
    throws SQLException
  {
    clearParameter(paramInt);
    if (paramBigDecimal == null)
      setNull(paramInt, 2);
    else
      setChar(paramInt, 2, paramBigDecimal.scale(), paramBigDecimal.toString());
    this.boundParams[(paramInt - 1)].boundType = 2;
    this.boundParams[(paramInt - 1)].boundValue = paramBigDecimal;
  }

  public void setDecimal(int paramInt, BigDecimal paramBigDecimal)
    throws SQLException
  {
    clearParameter(paramInt);
    if (paramBigDecimal == null)
      setNull(paramInt, 3);
    else
      setChar(paramInt, 3, paramBigDecimal.scale(), paramBigDecimal.toString());
    this.boundParams[(paramInt - 1)].boundType = 3;
    this.boundParams[(paramInt - 1)].boundValue = paramBigDecimal;
  }

  public void setString(int paramInt, String paramString)
    throws SQLException
  {
    if (paramString == null)
      setNull(paramInt, 1);
    else if (paramString.length() >= 254)
      setChar(paramInt, -1, 0, paramString);
    else
      setChar(paramInt, 1, 0, paramString);
  }

  public void setBytes(int paramInt, byte[] paramArrayOfByte)
    throws SQLException
  {
    if (paramArrayOfByte == null)
      setNull(paramInt, -2);
    else if (paramArrayOfByte.length > 8000)
      setBinaryStream(paramInt, new ByteArrayInputStream(paramArrayOfByte), paramArrayOfByte.length);
    else
      setBinary(paramInt, -2, paramArrayOfByte);
    this.boundParams[(paramInt - 1)].boundType = -2;
    this.boundParams[(paramInt - 1)].boundValue = paramArrayOfByte;
  }

  public void setDate(int paramInt, Date paramDate)
    throws SQLException
  {
    if (paramDate == null)
    {
      setNull(paramInt, 91);
      return;
    }
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 32);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterDate(this.hStmt, paramInt, paramDate, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundType = 91;
    this.boundParams[(paramInt - 1)].boundValue = paramDate;
    this.arrayParams.storeValue(paramInt - 1, paramDate, -3);
    setSqlType(paramInt, 91);
  }

  public void setTime(int paramInt, Time paramTime)
    throws SQLException
  {
    if (paramTime == null)
    {
      setNull(paramInt, 92);
      return;
    }
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 32);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterTime(this.hStmt, paramInt, paramTime, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundType = 92;
    this.boundParams[(paramInt - 1)].boundValue = paramTime;
    this.arrayParams.storeValue(paramInt - 1, paramTime, -3);
    setSqlType(paramInt, 92);
  }

  public void setTimestamp(int paramInt, Timestamp paramTimestamp)
    throws SQLException
  {
    if (paramTimestamp == null)
    {
      setNull(paramInt, 93);
      return;
    }
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 32);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterTimestamp(this.hStmt, paramInt, paramTimestamp, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundValue = paramTimestamp;
    this.boundParams[(paramInt - 1)].boundType = 93;
    this.arrayParams.storeValue(paramInt - 1, paramTimestamp, -3);
    setSqlType(paramInt, 93);
  }

  public void setAsciiStream(int paramInt1, InputStream paramInputStream, int paramInt2)
    throws SQLException
  {
    setStream(paramInt1, paramInputStream, paramInt2, -1, 1);
  }

  public void setUnicodeStream(int paramInt1, InputStream paramInputStream, int paramInt2)
    throws SQLException
  {
    setStream(paramInt1, paramInputStream, paramInt2, -1, 2);
  }

  public void setBinaryStream(int paramInt1, InputStream paramInputStream, int paramInt2)
    throws SQLException
  {
    setStream(paramInt1, paramInputStream, paramInt2, -4, 3);
    this.binaryPrec = paramInt2;
  }

  public void clearParameters()
    throws SQLException
  {
    if (this.hStmt != 3412046672778231808L)
    {
      this.OdbcApi.SQLFreeStmt(this.hStmt, 3);
      FreeParams();
      for (int i = 1; (this.boundParams != null) && (i <= this.boundParams.length); ++i)
      {
        this.boundParams[(i - 1)].binaryData = null;
        this.boundParams[(i - 1)].initialize();
        this.boundParams[(i - 1)].paramInputStream = null;
        this.boundParams[(i - 1)].inputParameter = false;
      }
    }
  }

  public void clearParameter(int paramInt)
    throws SQLException
  {
    if (this.hStmt != 3412046672778231808L)
    {
      if (this.boundParams[(paramInt - 1)].pA1 != 3412047033555484672L)
      {
        JdbcOdbc.ReleaseStoredBytes(this.boundParams[(paramInt - 1)].pA1, this.boundParams[(paramInt - 1)].pA2);
        this.boundParams[(paramInt - 1)].pA1 = 3412047823829467136L;
        this.boundParams[(paramInt - 1)].pA2 = 3412047823829467136L;
      }
      if (this.boundParams[(paramInt - 1)].pB1 != 3412047033555484672L)
      {
        JdbcOdbc.ReleaseStoredBytes(this.boundParams[(paramInt - 1)].pB1, this.boundParams[(paramInt - 1)].pB2);
        this.boundParams[(paramInt - 1)].pB1 = 3412047823829467136L;
        this.boundParams[(paramInt - 1)].pB2 = 3412047823829467136L;
      }
      if (this.boundParams[(paramInt - 1)].pC1 != 3412047033555484672L)
      {
        JdbcOdbc.ReleaseStoredBytes(this.boundParams[(paramInt - 1)].pC1, this.boundParams[(paramInt - 1)].pC2);
        this.boundParams[(paramInt - 1)].pC1 = 3412047823829467136L;
        this.boundParams[(paramInt - 1)].pC2 = 3412047823829467136L;
      }
      if (this.boundParams[(paramInt - 1)].pS1 != 3412047033555484672L)
      {
        JdbcOdbc.ReleaseStoredChars(this.boundParams[(paramInt - 1)].pS1, this.boundParams[(paramInt - 1)].pS2);
        this.boundParams[(paramInt - 1)].pS1 = 3412047823829467136L;
        this.boundParams[(paramInt - 1)].pS2 = 3412047823829467136L;
      }
      this.boundParams[(paramInt - 1)].binaryData = null;
      this.boundParams[(paramInt - 1)].initialize();
      this.boundParams[(paramInt - 1)].paramInputStream = null;
      this.boundParams[(paramInt - 1)].inputParameter = false;
    }
  }

  public void setObject(int paramInt, Object paramObject)
    throws SQLException
  {
    setObject(paramInt, paramObject, getTypeFromObject(paramObject));
  }

  public void setObject(int paramInt1, Object paramObject, int paramInt2)
    throws SQLException
  {
    setObject(paramInt1, paramObject, paramInt2, 0);
  }

  public void setObject(int paramInt1, Object paramObject, int paramInt2, int paramInt3)
    throws SQLException
  {
    if (paramObject == null)
    {
      setNull(paramInt1, paramInt2);
      return;
    }
    String str = null;
    if (paramObject instanceof byte[])
      str = new String("byte[]");
    else
      str = new String(paramObject.getClass().getName());
    int i = 0;
    BigInteger localBigInteger = null;
    if (str.equalsIgnoreCase("java.lang.Boolean"))
      if (paramObject.toString().equalsIgnoreCase("true"))
      {
        i = 1;
        localBigInteger = BigInteger.ONE;
      }
      else
      {
        i = 0;
        localBigInteger = BigInteger.ZERO;
      }
    try
    {
      Object localObject;
      BigDecimal localBigDecimal1;
      BigDecimal localBigDecimal2;
      switch (paramInt2)
      {
      case 1:
        if ((str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.math.BigDecimal")) || (str.equalsIgnoreCase("java.lang.Boolean")) || (str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Short")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.lang.Float")) || (str.equalsIgnoreCase("java.lang.Double")) || (str.equalsIgnoreCase("java.sql.Date")) || (str.equalsIgnoreCase("java.sql.Time")) || (str.equalsIgnoreCase("java.sql.Timestamp")))
          setString(paramInt1, paramObject.toString());
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case 12:
        if ((str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.math.BigDecimal")) || (str.equalsIgnoreCase("java.lang.Boolean")) || (str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Short")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.lang.Float")) || (str.equalsIgnoreCase("java.lang.Double")) || (str.equalsIgnoreCase("java.sql.Date")) || (str.equalsIgnoreCase("java.sql.Time")) || (str.equalsIgnoreCase("java.sql.Timestamp")))
          setChar(paramInt1, paramInt2, 0, paramObject.toString());
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case -1:
        if ((str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.math.BigDecimal")) || (str.equalsIgnoreCase("java.lang.Boolean")) || (str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Short")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.lang.Float")) || (str.equalsIgnoreCase("java.lang.Double")) || (str.equalsIgnoreCase("java.sql.Date")) || (str.equalsIgnoreCase("java.sql.Time")) || (str.equalsIgnoreCase("java.sql.Timestamp")))
          setChar(paramInt1, paramInt2, 0, paramObject.toString());
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case 2:
        if ((str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.lang.Short")))
        {
          localObject = new BigDecimal(new BigInteger(paramObject.toString()), 0);
          localBigDecimal1 = ((BigDecimal)localObject).movePointRight(paramInt3);
          localBigDecimal2 = localBigDecimal1.movePointLeft(paramInt3);
          setBigDecimal(paramInt1, localBigDecimal2);
        }
        else if ((str.equalsIgnoreCase("java.lang.Float")) || (str.equalsIgnoreCase("java.lang.Double")) || (str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.math.BigDecimal")))
        {
          setBigDecimal(paramInt1, new BigDecimal(paramObject.toString()));
        }
        else if (str.equalsIgnoreCase("java.lang.Boolean"))
        {
          setBigDecimal(paramInt1, new BigDecimal(localBigInteger.toString()));
        }
        else
        {
          throw new SQLException("Conversion not supported by setObject!!");
        }
      case 3:
        if ((str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.lang.Short")))
        {
          localObject = new BigDecimal(new BigInteger(paramObject.toString()), 0);
          localBigDecimal1 = ((BigDecimal)localObject).movePointRight(paramInt3);
          localBigDecimal2 = localBigDecimal1.movePointLeft(paramInt3);
          setDecimal(paramInt1, localBigDecimal2);
        }
        else if ((str.equalsIgnoreCase("java.lang.Float")) || (str.equalsIgnoreCase("java.lang.Double")) || (str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.math.BigDecimal")))
        {
          setDecimal(paramInt1, new BigDecimal(paramObject.toString()));
        }
        else if (str.equalsIgnoreCase("java.lang.Boolean"))
        {
          setDecimal(paramInt1, new BigDecimal(localBigInteger.toString()));
        }
        else
        {
          throw new SQLException("Conversion not supported by setObject!!");
        }
      case -7:
        if ((str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.lang.Boolean")))
          if (paramObject.toString().equalsIgnoreCase("true"))
            setBoolean(paramInt1, true);
          else
            setBoolean(paramInt1, false);
        else if ((str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.lang.Short")) || (str.equalsIgnoreCase("java.math.BigDecimal")))
          if (paramObject.toString().equalsIgnoreCase("1"))
            setBoolean(paramInt1, true);
          else
            setBoolean(paramInt1, false);
        else if (str.equalsIgnoreCase("java.lang.Float"))
          if (new Float(0F).compareTo((Float)paramObject) == 0)
            setBoolean(paramInt1, false);
          else
            setBoolean(paramInt1, true);
        else if (str.equalsIgnoreCase("java.lang.Double"))
          if (new Double(0D).compareTo((Double)paramObject) == 0)
            setBoolean(paramInt1, false);
          else
            setBoolean(paramInt1, true);
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case -6:
        if (str.equalsIgnoreCase("java.lang.Float"))
          setByte(paramInt1, new Float(paramObject.toString()).byteValue());
        else if (str.equalsIgnoreCase("java.lang.Double"))
          setByte(paramInt1, new Double(paramObject.toString()).byteValue());
        else if (str.equalsIgnoreCase("java.lang.Boolean"))
          setByte(paramInt1, (byte)i);
        else if ((str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Short")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.math.BigDecimal")))
          setByte(paramInt1, new Byte(paramObject.toString()).byteValue());
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case 5:
        if (str.equalsIgnoreCase("java.lang.Float"))
          setShort(paramInt1, new Float(paramObject.toString()).shortValue());
        else if (str.equalsIgnoreCase("java.lang.Double"))
          setShort(paramInt1, new Double(paramObject.toString()).shortValue());
        else if (str.equalsIgnoreCase("java.lang.Boolean"))
          setShort(paramInt1, (short)i);
        else if ((str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Short")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.math.BigDecimal")))
          setShort(paramInt1, new Short(paramObject.toString()).shortValue());
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case 4:
        if (str.equalsIgnoreCase("java.lang.Float"))
          setInt(paramInt1, new Float(paramObject.toString()).intValue());
        else if (str.equalsIgnoreCase("java.lang.Double"))
          setInt(paramInt1, new Double(paramObject.toString()).intValue());
        else if (str.equalsIgnoreCase("java.lang.Boolean"))
          setInt(paramInt1, i);
        else if ((str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Short")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.math.BigDecimal")))
          setInt(paramInt1, new Integer(paramObject.toString()).intValue());
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case -5:
        if (str.equalsIgnoreCase("java.lang.Float"))
          setLong(paramInt1, new Float(paramObject.toString()).longValue());
        else if (str.equalsIgnoreCase("java.lang.Double"))
          setLong(paramInt1, new Double(paramObject.toString()).longValue());
        else if (str.equalsIgnoreCase("java.lang.Boolean"))
          setLong(paramInt1, i);
        else if ((str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Short")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.math.BigDecimal")))
          setLong(paramInt1, new Long(paramObject.toString()).longValue());
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case 6:
      case 7:
      case 8:
        if (str.equalsIgnoreCase("java.lang.Boolean"))
          setDouble(paramInt1, i);
        else if ((str.equalsIgnoreCase("java.lang.String")) || (str.equalsIgnoreCase("java.lang.Integer")) || (str.equalsIgnoreCase("java.lang.Short")) || (str.equalsIgnoreCase("java.lang.Long")) || (str.equalsIgnoreCase("java.math.BigDecimal")) || (str.equalsIgnoreCase("java.lang.Float")) || (str.equalsIgnoreCase("java.lang.Double")))
          setDouble(paramInt1, new Double(paramObject.toString()).doubleValue());
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case -2:
        if (str.equalsIgnoreCase("java.lang.String"))
          setBytes(paramInt1, ((String)paramObject).getBytes());
        else if (str.equalsIgnoreCase("byte[]"))
          setBytes(paramInt1, (byte[])(byte[])paramObject);
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case -4:
      case -3:
        localObject = null;
        if (str.equalsIgnoreCase("java.lang.String"))
          localObject = ((String)paramObject).getBytes();
        else if (str.equalsIgnoreCase("byte[]"))
          localObject = (byte[])(byte[])paramObject;
        else
          throw new SQLException("Conversion not supported by setObject!!");
        if (localObject.length > 8000)
          setBinaryStream(paramInt1, new ByteArrayInputStream(localObject), localObject.length);
        else
          setBinary(paramInt1, paramInt2, localObject);
        break;
      case 91:
        if (str.equalsIgnoreCase("java.lang.String"))
          setDate(paramInt1, Date.valueOf(paramObject.toString()));
        else if (str.equalsIgnoreCase("java.sql.Timestamp"))
          setDate(paramInt1, new Date(Timestamp.valueOf(paramObject.toString()).getTime()));
        else if (str.equalsIgnoreCase("java.sql.Date"))
          setDate(paramInt1, (Date)paramObject);
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case 92:
        if (str.equalsIgnoreCase("java.lang.String"))
          setTime(paramInt1, Time.valueOf(paramObject.toString()));
        else if (str.equalsIgnoreCase("java.sql.Timestamp"))
          setTime(paramInt1, new Time(Timestamp.valueOf(paramObject.toString()).getTime()));
        else if (str.equalsIgnoreCase("java.sql.Time"))
          setTime(paramInt1, (Time)paramObject);
        else
          throw new SQLException("Conversion not supported by setObject!!");
      case 93:
        if (str.equalsIgnoreCase("java.lang.String"))
          setTimestamp(paramInt1, Timestamp.valueOf(paramObject.toString()));
        else if (str.equalsIgnoreCase("java.sql.Date"))
          setTimestamp(paramInt1, new Timestamp(Date.valueOf(paramObject.toString()).getTime()));
        else if (str.equalsIgnoreCase("java.sql.Timestamp"))
          setTimestamp(paramInt1, (Timestamp)paramObject);
        else
          throw new SQLException("Conversion not supported by setObject!!");
      default:
        throw new SQLException("Unknown SQL Type for PreparedStatement.setObject (SQL Type=" + paramInt2);
      }
    }
    catch (SQLException localSQLException)
    {
      throw new SQLException("SQL Exception : " + localSQLException.getMessage());
    }
    catch (Exception localException)
    {
      throw new SQLException("Unexpected exception : " + localException.getMessage());
    }
  }

  public void addBatch(String paramString)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.addBatch (" + paramString + ")");
    throw new SQLException("Driver does not support this function", "IM001");
  }

  public void clearBatch()
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.clearBatch");
    try
    {
      if (this.batchSqlVec != null)
      {
        cleanUpBatch();
        this.batchOn = false;
        this.batchParamsOn = false;
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }

  public void addBatch()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.addBatch");
    try
    {
      int i;
      this.batchSqlVec = this.myConnection.getBatchVector(this);
      if (this.batchSqlVec == null)
      {
        this.batchSqlVec = new Vector(5, 10);
        i = 0;
      }
      else
      {
        i = this.batchSqlVec.size();
      }
      Object[] arrayOfObject = this.arrayParams.getStoredParameterSet();
      int[] arrayOfInt = this.arrayParams.getStoredIndexSet();
      int j = arrayOfObject.length;
      int k = arrayOfInt.length;
      if (k == this.numParams)
      {
        this.batchSqlVec.addElement(arrayOfObject);
        this.myConnection.setBatchVector(this.batchSqlVec, this);
        this.arrayParams.storeRowIndex(i, arrayOfInt);
        this.batchOn = true;
      }
      else
      {
        if (i == 0)
          throw new SQLException("Parameter-Set has missing values.");
        this.batchOn = true;
      }
    }
    catch (NullPointerException localNullPointerException)
    {
      this.batchOn = false;
    }
  }

  public int[] executeBatchUpdate()
    throws BatchUpdateException
  {
    int[] arrayOfInt1 = new int[0];
    if (this.numParams <= 0)
    {
      this.batchSize = 0;
      this.batchOn = false;
      this.batchParamsOn = false;
      return executeNoParametersBatch();
    }
    this.batchSqlVec = this.myConnection.getBatchVector(this);
    if (this.batchSqlVec != null)
    {
      this.batchSize = this.batchSqlVec.size();
    }
    else
    {
      arrayOfInt1 = new int[0];
      return arrayOfInt1;
    }
    if (this.batchSize > 0)
    {
      arrayOfInt1 = new int[this.batchSize];
      FreeIntParams();
      this.paramStatusArray = new int[this.batchSize];
      this.paramsProcessed = new int[this.batchSize];
      int i = 1;
      int j = 0;
      try
      {
        if (i == 0)
        {
          this.OdbcApi.SQLSetStmtAttr(this.hStmt, 18, 0, 0);
          try
          {
            setStmtParameterSize(this.batchSize);
            j = getStmtParameterAttr(22);
          }
          catch (SQLException localSQLException1)
          {
            this.batchSupport = false;
          }
        }
        if (j != this.batchSize)
        {
          this.batchSupport = false;
          try
          {
            setStmtParameterSize(1);
          }
          catch (SQLException localSQLException2)
          {
          }
        }
        else
        {
          this.pA2 = new long[2];
          this.pA2[0] = 3412048407945019392L;
          this.pA2[1] = 3412048407945019392L;
          this.OdbcApi.SQLSetStmtAttrPtr(this.hStmt, 20, this.paramStatusArray, 0, this.pA2);
          this.pA1 = new long[2];
          this.pA1[0] = 3412048407945019392L;
          this.pA1[1] = 3412048407945019392L;
          this.OdbcApi.SQLSetStmtAttrPtr(this.hStmt, 21, this.paramsProcessed, 0, this.pA1);
          this.batchSupport = true;
        }
      }
      catch (SQLException localSQLException3)
      {
        this.batchSupport = false;
      }
      if (this.batchSupport == true)
      {
        this.batchParamsOn = true;
        int[] arrayOfInt2 = new int[0];
        this.arrayParams.builtColumWiseParameteSets(this.batchSize, this.batchSqlVec);
        for (int k = 0; k < this.numParams; ++k)
        {
          this.arrayDef = 0;
          this.arrayScale = 0;
          int l = 0;
          int i1 = k + 1;
          try
          {
            Object[] arrayOfObject = this.arrayParams.getColumnWiseParamSet(i1);
            int[] arrayOfInt3 = this.arrayParams.getColumnWiseIndexArray(i1);
            setPrecisionScaleArgs(arrayOfObject, arrayOfInt3);
            l = getSqlType(i1);
            bindArrayOfParameters(i1, l, this.arrayDef, this.arrayScale, arrayOfObject, arrayOfInt3);
          }
          catch (SQLException localSQLException6)
          {
            localSQLException6.printStackTrace();
          }
        }
        try
        {
          if (!(execute()))
          {
            this.paramStatusArray[0] = getUpdateCount();
            this.arrayParams.clearStoredRowIndexs();
            arrayOfInt1 = this.paramStatusArray;
            this.batchOn = false;
            this.batchParamsOn = false;
            cleanUpBatch();
          }
          else
          {
            cleanUpBatch();
            throw new JdbcOdbcBatchUpdateException("SQL Attempt to produce a ResultSet from executeBatch", this.paramStatusArray);
          }
        }
        catch (SQLException localSQLException4)
        {
          try
          {
            this.paramStatusArray[0] = getUpdateCount();
          }
          catch (SQLException localSQLException5)
          {
          }
          arrayOfInt2 = new int[this.paramsProcessed[0] - 1];
          cleanUpBatch();
          throw new JdbcOdbcBatchUpdateException(localSQLException4.getMessage(), localSQLException4.getSQLState(), arrayOfInt2);
        }
      }
      else if (!(this.batchSupport))
      {
        this.batchOn = false;
        this.batchParamsOn = false;
        return emulateExecuteBatch();
      }
    }
    return arrayOfInt1;
  }

  protected int[] executeNoParametersBatch()
    throws BatchUpdateException
  {
    int[] arrayOfInt = new int[1];
    try
    {
      if (!(execute()))
      {
        cleanUpBatch();
        arrayOfInt[0] = getUpdateCount();
      }
      else
      {
        cleanUpBatch();
        throw new JdbcOdbcBatchUpdateException("SQL Attempt to produce a ResultSet from executeBatch", arrayOfInt);
      }
    }
    catch (SQLException localSQLException1)
    {
      try
      {
        arrayOfInt[0] = getUpdateCount();
      }
      catch (SQLException localSQLException2)
      {
      }
      cleanUpBatch();
      throw new JdbcOdbcBatchUpdateException(localSQLException1.getMessage(), localSQLException1.getSQLState(), arrayOfInt);
    }
    return arrayOfInt;
  }

  protected int getStmtParameterAttr(int paramInt)
    throws SQLException
  {
    try
    {
      clearWarnings();
      return this.OdbcApi.SQLGetStmtAttr(this.hStmt, paramInt);
    }
    catch (SQLException localSQLException)
    {
      localSQLException.printStackTrace();
    }
    return -1;
  }

  protected void setStmtParameterSize(int paramInt)
    throws SQLException
  {
    try
    {
      clearWarnings();
      this.OdbcApi.SQLSetStmtAttr(this.hStmt, 22, paramInt, 0);
    }
    catch (SQLException localSQLException)
    {
      localSQLException.printStackTrace();
    }
  }

  protected void bindArrayOfParameters(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Object[] paramArrayOfObject, int[] paramArrayOfInt)
    throws SQLException
  {
    switch (paramInt2)
    {
    case 1:
    case 2:
    case 3:
    case 12:
      this.OdbcApi.SQLBindInParameterStringArray(this.hStmt, paramInt1, paramInt2, paramArrayOfObject, paramInt3, paramInt4, paramArrayOfInt);
      break;
    case -1:
      if (getTypeFromObjectArray(paramArrayOfObject) == -4)
      {
        this.arrayParams.setInputStreamElements(paramInt1, paramArrayOfObject);
        this.OdbcApi.SQLBindInParameterAtExecArray(this.hStmt, paramInt1, paramInt2, paramInt3, paramArrayOfInt);
        return;
      }
      this.OdbcApi.SQLBindInParameterStringArray(this.hStmt, paramInt1, paramInt2, paramArrayOfObject, paramInt3, paramInt4, paramArrayOfInt);
      break;
    case -7:
    case -6:
    case 4:
    case 5:
      this.OdbcApi.SQLBindInParameterIntegerArray(this.hStmt, paramInt1, paramInt2, paramArrayOfObject, paramArrayOfInt);
      break;
    case 8:
      this.OdbcApi.SQLBindInParameterDoubleArray(this.hStmt, paramInt1, paramInt2, paramArrayOfObject, paramArrayOfInt);
      break;
    case -5:
    case 6:
    case 7:
      this.OdbcApi.SQLBindInParameterFloatArray(this.hStmt, paramInt1, paramInt2, paramArrayOfObject, paramArrayOfInt);
      break;
    case 91:
      this.OdbcApi.SQLBindInParameterDateArray(this.hStmt, paramInt1, paramArrayOfObject, paramArrayOfInt);
      break;
    case 92:
      this.OdbcApi.SQLBindInParameterTimeArray(this.hStmt, paramInt1, paramArrayOfObject, paramArrayOfInt);
      break;
    case 93:
      this.OdbcApi.SQLBindInParameterTimestampArray(this.hStmt, paramInt1, paramArrayOfObject, paramArrayOfInt);
      break;
    case -3:
    case -2:
      this.OdbcApi.SQLBindInParameterBinaryArray(this.hStmt, paramInt1, paramInt2, paramArrayOfObject, paramInt3, paramArrayOfInt);
      break;
    case -4:
      this.arrayParams.setInputStreamElements(paramInt1, paramArrayOfObject);
      this.OdbcApi.SQLBindInParameterAtExecArray(this.hStmt, paramInt1, paramInt2, paramInt3, paramArrayOfInt);
    }
  }

  protected int[] emulateExecuteBatch()
    throws BatchUpdateException
  {
    int[] arrayOfInt1 = new int[this.batchSize];
    int[] arrayOfInt2 = new int[0];
    int i = 0;
    for (int j = 0; j < this.batchSize; ++j)
    {
      int i1;
      Object[] arrayOfObject = (Object[])(Object[])this.batchSqlVec.elementAt(j);
      int[] arrayOfInt3 = this.arrayParams.getStoredRowIndex(j);
      try
      {
        for (int k = 0; k < arrayOfObject.length; ++k)
        {
          i1 = 1111;
          int i2 = 0;
          int i3 = 0;
          int i4 = k + 1;
          InputStream localInputStream = null;
          i1 = getTypeFromObject(arrayOfObject[k]);
          int i5 = getSqlType(i4);
          if (i1 == -4)
          {
            localInputStream = (InputStream)arrayOfObject[k];
            i2 = arrayOfInt3[k];
            switch (i5)
            {
            case -4:
              i3 = 3;
              break;
            case -1:
              i3 = this.boundParams[k].getStreamType();
            }
          }
          if ((i2 > 0) && (0 < i3))
            switch (i3)
            {
            case 1:
            case 2:
              setStream(i4, localInputStream, i2, -1, i3);
              break;
            case 3:
              setStream(i4, localInputStream, i2, -4, i3);
            }
          else if (i1 != 1111)
            if (i1 != 0)
              setObject(i4, arrayOfObject[k], i5);
            else
              setNull(i4, i5);
        }
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
      try
      {
        if (!(execute()))
        {
          this.myConnection.removeBatchVector(this);
          arrayOfInt1[j] = getUpdateCount();
          ++i;
        }
        else
        {
          for (int l = 0; l < j - 1; ++l)
          {
            arrayOfInt2 = new int[i];
            arrayOfInt2[l] = arrayOfInt1[l];
          }
          cleanUpBatch();
          throw new JdbcOdbcBatchUpdateException("SQL Attempt to produce a ResultSet from executeBatch", arrayOfInt2);
        }
      }
      catch (SQLException localSQLException)
      {
        for (i1 = 0; i1 < j - 1; ++i1)
        {
          arrayOfInt2 = new int[i];
          arrayOfInt2[i1] = arrayOfInt1[i1];
        }
        cleanUpBatch();
        throw new JdbcOdbcBatchUpdateException(localSQLException.getMessage(), localSQLException.getSQLState(), arrayOfInt2);
      }
    }
    cleanUpBatch();
    return arrayOfInt1;
  }

  protected void cleanUpBatch()
  {
    this.myConnection.removeBatchVector(this);
    if (this.batchSqlVec != null)
    {
      this.batchSqlVec.setSize(0);
      this.batchSize = 0;
    }
  }

  protected void setPrecisionScaleArgs(Object[] paramArrayOfObject, int[] paramArrayOfInt)
  {
    int i = getTypeFromObjectArray(paramArrayOfObject);
    for (int j = 0; j < this.batchSize; ++j)
    {
      byte[] arrayOfByte = null;
      String str = null;
      BigDecimal localBigDecimal = null;
      int k = 0;
      try
      {
        if ((i == 3) || (i == 2))
        {
          if (paramArrayOfObject[j] != null)
          {
            int l = 0;
            localBigDecimal = (BigDecimal)paramArrayOfObject[j];
            str = localBigDecimal.toString();
            k = str.indexOf(46);
            if (k == -1)
            {
              k = str.length();
            }
            else
            {
              l = localBigDecimal.scale();
              k += l + 1;
            }
            if (l > this.arrayScale)
              this.arrayScale = l;
          }
        }
        else if ((i == 1) || (i == 12))
        {
          if (paramArrayOfObject[j] != null)
          {
            str = (String)paramArrayOfObject[j];
            k = str.length();
          }
        }
        else if (i == -4)
        {
          if (paramArrayOfInt[j] > this.arrayDef)
            this.arrayDef = paramArrayOfInt[j];
        }
        else if ((((i == -2) || (i == -3))) && (paramArrayOfObject[j] != null))
        {
          arrayOfByte = (byte[])(byte[])paramArrayOfObject[j];
          k = arrayOfByte.length;
        }
        if (k > this.arrayDef)
          this.arrayDef = k;
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
    }
  }

  protected void setSqlType(int paramInt1, int paramInt2)
  {
    if ((paramInt1 >= 1) && (paramInt1 <= this.numParams))
      this.boundParams[(paramInt1 - 1)].setSqlType(paramInt2);
  }

  protected int getSqlType(int paramInt)
  {
    int i = 1111;
    if ((paramInt >= 1) && (paramInt <= this.numParams))
      i = this.boundParams[(paramInt - 1)].getSqlType();
    return i;
  }

  public void setCharacterStream(int paramInt1, Reader paramReader, int paramInt2)
    throws SQLException
  {
    clearParameter(paramInt1);
    BufferedReader localBufferedReader = null;
    BufferedOutputStream localBufferedOutputStream = null;
    ByteArrayOutputStream localByteArrayOutputStream = null;
    ByteArrayInputStream localByteArrayInputStream = null;
    String str = this.OdbcApi.charSet;
    int i = 300;
    if (paramInt2 < i)
      i = paramInt2;
    int j = 0;
    int k = 0;
    try
    {
      k = (int)Charset.forName(str).newEncoder().maxBytesPerChar();
    }
    catch (UnsupportedCharsetException localUnsupportedCharsetException)
    {
    }
    catch (IllegalCharsetNameException localIllegalCharsetNameException)
    {
    }
    if (k == 0)
      k = 1;
    try
    {
      if (paramReader != null)
      {
        int l = 0;
        int i1 = 0;
        localBufferedReader = new BufferedReader(paramReader);
        localByteArrayOutputStream = new ByteArrayOutputStream();
        localBufferedOutputStream = new BufferedOutputStream(localByteArrayOutputStream);
        char[] arrayOfChar1 = new char[i];
        while (i1 != -1)
        {
          byte[] arrayOfByte = new byte[0];
          i1 = localBufferedReader.read(arrayOfChar1);
          if (i1 != -1)
          {
            char[] arrayOfChar2 = new char[i1];
            for (int i2 = 0; i2 < i1; ++i2)
              arrayOfChar2[i2] = arrayOfChar1[i2];
            arrayOfByte = CharsToBytes(str, arrayOfChar2);
            i2 = arrayOfByte.length - 1;
            localBufferedOutputStream.write(arrayOfByte, 0, i2);
            localBufferedOutputStream.flush();
          }
        }
        j = localByteArrayOutputStream.size();
        localByteArrayInputStream = new ByteArrayInputStream(localByteArrayOutputStream.toByteArray());
      }
    }
    catch (IOException localIOException)
    {
      throw new SQLException("CharsToBytes Reader Conversion: " + localIOException.getMessage());
    }
    setStream(paramInt1, localByteArrayInputStream, j, -1, 3);
  }

  public void setRef(int paramInt, Ref paramRef)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBlob(int paramInt, Blob paramBlob)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setClob(int paramInt, Clob paramClob)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setArray(int paramInt, Array paramArray)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public ResultSetMetaData getMetaData()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.getMetaData");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    if (this.hStmt == 3412046672778231808L)
      throw new SQLException("Statement is closed");
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, this.hStmt, true, null);
    return new JdbcOdbcResultSetMetaData(this.OdbcApi, localJdbcOdbcResultSet);
  }

  public void setDate(int paramInt, Date paramDate, Calendar paramCalendar)
    throws SQLException
  {
    if (paramDate == null)
    {
      setNull(paramInt, 91);
      return;
    }
    long l = this.utils.convertToGMT(paramDate, paramCalendar);
    paramDate = new Date(l);
    paramCalendar = Calendar.getInstance();
    paramCalendar.setTime(paramDate);
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 32);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterCalendarDate(this.hStmt, paramInt, paramCalendar, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundType = 91;
    this.boundParams[(paramInt - 1)].boundValue = paramDate;
    this.arrayParams.storeValue(paramInt - 1, paramCalendar, -3);
    setSqlType(paramInt, 91);
  }

  public void setTime(int paramInt, Time paramTime, Calendar paramCalendar)
    throws SQLException
  {
    if (paramTime == null)
    {
      setNull(paramInt, 92);
      return;
    }
    long l = this.utils.convertToGMT(paramTime, paramCalendar);
    paramTime = new Time(l);
    paramCalendar = Calendar.getInstance();
    paramCalendar.setTime(paramTime);
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 32);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterCalendarTime(this.hStmt, paramInt, paramCalendar, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundType = 92;
    this.boundParams[(paramInt - 1)].boundValue = paramTime;
    this.arrayParams.storeValue(paramInt - 1, paramCalendar, -3);
    setSqlType(paramInt, 92);
  }

  public void setTimestamp(int paramInt, Timestamp paramTimestamp, Calendar paramCalendar)
    throws SQLException
  {
    if (paramTimestamp == null)
    {
      setNull(paramInt, 93);
      return;
    }
    long l = this.utils.convertToGMT(paramTimestamp, paramCalendar);
    paramTimestamp = new Timestamp(l);
    paramCalendar = Calendar.getInstance();
    paramCalendar.setTime(paramTimestamp);
    clearParameter(paramInt);
    setInputParameter(paramInt, true);
    byte[] arrayOfByte = allocBindBuf(paramInt, 32);
    long[] arrayOfLong = new long[2];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterCalendarTimestamp(this.hStmt, paramInt, paramCalendar, arrayOfByte, arrayOfLong);
    this.boundParams[(paramInt - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt - 1)].boundType = 93;
    this.boundParams[(paramInt - 1)].boundValue = paramTimestamp;
    this.arrayParams.storeValue(paramInt - 1, paramCalendar, -3);
    setSqlType(paramInt, 93);
  }

  public void setNull(int paramInt1, int paramInt2, String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void initBoundParam()
    throws SQLException
  {
    this.numParams = this.OdbcApi.SQLNumParams(this.hStmt);
    if (this.numParams > 0)
    {
      this.boundParams = new JdbcOdbcBoundParam[this.numParams];
      for (int i = 0; i < this.numParams; ++i)
      {
        this.boundParams[i] = new JdbcOdbcBoundParam();
        this.boundParams[i].initialize();
      }
      this.arrayParams = new JdbcOdbcBoundArrayOfParams(this.numParams);
      this.batchRCFlag = this.myConnection.getBatchRowCountFlag(1);
      if ((this.batchRCFlag > 0) && (this.batchRCFlag == 1))
        this.batchSupport = true;
      else
        this.batchSupport = false;
      this.StringDef = 0;
      this.NumberDef = 0;
      this.NumberDef = 0;
      this.binaryPrec = 0;
    }
  }

  protected byte[] allocBindBuf(int paramInt1, int paramInt2)
  {
    byte[] arrayOfByte = null;
    if ((paramInt1 >= 1) && (paramInt1 <= this.numParams))
      arrayOfByte = this.boundParams[(paramInt1 - 1)].allocBindDataBuffer(paramInt2);
    return arrayOfByte;
  }

  protected byte[] getDataBuf(int paramInt)
  {
    byte[] arrayOfByte = null;
    if ((paramInt >= 1) && (paramInt <= this.numParams))
      arrayOfByte = this.boundParams[(paramInt - 1)].getBindDataBuffer();
    return arrayOfByte;
  }

  protected byte[] getLengthBuf(int paramInt)
  {
    byte[] arrayOfByte = null;
    if ((paramInt >= 1) && (paramInt <= this.numParams))
      arrayOfByte = this.boundParams[(paramInt - 1)].getBindLengthBuffer();
    return arrayOfByte;
  }

  public int getParamLength(int paramInt)
  {
    int i = -1;
    if ((paramInt >= 1) && (paramInt <= this.numParams))
      i = this.OdbcApi.bufferToInt(this.boundParams[(paramInt - 1)].getBindLengthBuffer());
    return i;
  }

  protected void putParamData(int paramInt)
    throws SQLException, JdbcOdbcSQLWarning
  {
    int i = 2000;
    byte[] arrayOfByte = new byte[i];
    int l = 0;
    if ((paramInt < 1) || (paramInt > this.numParams))
    {
      if (this.OdbcApi.getTracer().isTracing())
        this.OdbcApi.getTracer().trace("Invalid index for putParamData()");
      return;
    }
    InputStream localInputStream = this.boundParams[(paramInt - 1)].getInputStream();
    int i1 = this.boundParams[(paramInt - 1)].getInputStreamLen();
    int i2 = this.boundParams[(paramInt - 1)].getStreamType();
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
          if (i1 != 0)
            throw new SQLException("End of InputStream reached before satisfying length specified when InputStream was set");
          l = 1;
          return;
        }
        if (j > i1)
        {
          j = i1;
          l = 1;
        }
        int k = j;
        if (i2 == 2)
        {
          k = j / 2;
          for (int i3 = 0; i3 < k; ++i3)
            arrayOfByte[i3] = arrayOfByte[(i3 * 2 + 1)];
        }
        this.OdbcApi.SQLPutData(this.hStmt, arrayOfByte, k);
        i1 -= j;
        if (this.OdbcApi.getTracer().isTracing())
          this.OdbcApi.getTracer().trace("" + i1 + " bytes remaining");
      }
      while (i1 != 0);
      l = 1;
    }
  }

  public void setStream(int paramInt1, InputStream paramInputStream, int paramInt2, int paramInt3, int paramInt4)
    throws SQLException
  {
    clearParameter(paramInt1);
    setInputParameter(paramInt1, true);
    byte[] arrayOfByte1 = getLengthBuf(paramInt1);
    byte[] arrayOfByte2 = allocBindBuf(paramInt1, 4);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterAtExec(this.hStmt, paramInt1, paramInt3, paramInt2, arrayOfByte2, arrayOfByte1, arrayOfLong);
    this.boundParams[(paramInt1 - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt1 - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt1 - 1)].pB1 = arrayOfLong[2];
    this.boundParams[(paramInt1 - 1)].pB2 = arrayOfLong[3];
    this.boundParams[(paramInt1 - 1)].boundType = paramInt3;
    this.boundParams[(paramInt1 - 1)].boundValue = paramInputStream;
    this.boundParams[(paramInt1 - 1)].setInputStream(paramInputStream, paramInt2);
    this.boundParams[(paramInt1 - 1)].setStreamType(paramInt4);
    this.arrayParams.storeValue(paramInt1 - 1, paramInputStream, paramInt2);
    setSqlType(paramInt1, paramInt3);
  }

  protected void setChar(int paramInt1, int paramInt2, int paramInt3, String paramString)
    throws SQLException
  {
    clearParameter(paramInt1);
    setInputParameter(paramInt1, true);
    int i = 0;
    int j = 0;
    char[] arrayOfChar = paramString.toCharArray();
    byte[] arrayOfByte1 = new byte[0];
    try
    {
      arrayOfByte1 = CharsToBytes(this.OdbcApi.charSet, arrayOfChar);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    byte[] arrayOfByte2 = allocBindBuf(paramInt1, arrayOfByte1.length);
    i = getPrecision(paramInt2);
    if ((i < 0) || (i > 8000))
      i = arrayOfByte1.length;
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterString(this.hStmt, paramInt1, paramInt2, i, paramInt3, arrayOfByte1, arrayOfByte2, arrayOfLong);
    this.boundParams[(paramInt1 - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt1 - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt1 - 1)].pB1 = arrayOfLong[2];
    this.boundParams[(paramInt1 - 1)].pB2 = arrayOfLong[3];
    this.boundParams[(paramInt1 - 1)].scale = paramInt3;
    this.boundParams[(paramInt1 - 1)].boundType = paramInt2;
    this.boundParams[(paramInt1 - 1)].boundValue = paramString;
    if ((paramInt2 == 2) || (paramInt2 == 3))
    {
      this.arrayParams.storeValue(paramInt1 - 1, new BigDecimal(paramString.trim()), -3);
      this.NumberDef = i;
      if (paramInt3 > this.NumberScale)
        this.NumberScale = paramInt3;
    }
    else if (paramInt2 == -5)
    {
      this.arrayParams.storeValue(paramInt1 - 1, new BigInteger(paramString.trim()), -3);
      this.NumberDef = i;
      if (paramInt3 > this.NumberScale)
        this.NumberScale = paramInt3;
    }
    else
    {
      this.arrayParams.storeValue(paramInt1 - 1, paramString, -3);
      this.StringDef = i;
    }
    setSqlType(paramInt1, paramInt2);
  }

  protected void setBinary(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    throws SQLException
  {
    clearParameter(paramInt1);
    setInputParameter(paramInt1, true);
    byte[] arrayOfByte1 = allocBindBuf(paramInt1, paramArrayOfByte.length);
    byte[] arrayOfByte2 = getLengthBuf(paramInt1);
    long[] arrayOfLong = new long[6];
    arrayOfLong[0] = 3412046827397054464L;
    arrayOfLong[1] = 3412046827397054464L;
    arrayOfLong[2] = 3412046827397054464L;
    arrayOfLong[3] = 3412046827397054464L;
    arrayOfLong[4] = 3412046827397054464L;
    arrayOfLong[5] = 3412046827397054464L;
    if (!(this.batchOn))
      this.OdbcApi.SQLBindInParameterBinary(this.hStmt, paramInt1, paramInt2, paramArrayOfByte, arrayOfByte1, arrayOfByte2, arrayOfLong);
    this.boundParams[(paramInt1 - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt1 - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt1 - 1)].pB1 = arrayOfLong[2];
    this.boundParams[(paramInt1 - 1)].pB2 = arrayOfLong[3];
    this.boundParams[(paramInt1 - 1)].pC1 = arrayOfLong[4];
    this.boundParams[(paramInt1 - 1)].pC2 = arrayOfLong[5];
    this.boundParams[(paramInt1 - 1)].boundType = paramInt2;
    this.boundParams[(paramInt1 - 1)].boundValue = paramArrayOfByte;
    this.binaryPrec = paramArrayOfByte.length;
    this.arrayParams.storeValue(paramInt1 - 1, (byte[])paramArrayOfByte, -3);
    setSqlType(paramInt1, paramInt2);
  }

  protected int getTypeFromObjectArray(Object[] paramArrayOfObject)
  {
    int i = 1111;
    for (int j = 0; j < this.batchSize; ++j)
    {
      i = getTypeFromObject(paramArrayOfObject[j]);
      if (i != 0)
        break;
    }
    return i;
  }

  public synchronized void close()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*PreparedStatement.close");
    clearMyResultSet();
    try
    {
      clearWarnings();
      if (this.hStmt != 3412047102274961408L)
      {
        if (this.closeCalledFromFinalize == true)
          if (!(this.myConnection.isFreeStmtsFromConnectionOnly()))
            this.OdbcApi.SQLFreeStmt(this.hStmt, 1);
        else
          this.OdbcApi.SQLFreeStmt(this.hStmt, 1);
        this.hStmt = 3412047892548943872L;
        FreeParams();
        for (int i = 1; (this.boundParams != null) && (i <= this.boundParams.length); ++i)
        {
          this.boundParams[(i - 1)].binaryData = null;
          this.boundParams[(i - 1)].initialize();
          this.boundParams[(i - 1)].paramInputStream = null;
          this.boundParams[(i - 1)].inputParameter = false;
        }
      }
    }
    catch (SQLException localSQLException)
    {
    }
    FreeIntParams();
    this.myConnection.deregisterStatement(this);
    if (this.batchOn)
      clearBatch();
  }

  public synchronized void FreeIntParams()
  {
    if ((this.pA1 != null) && (this.pA1[0] != 3412046827397054464L))
    {
      JdbcOdbc.ReleaseStoredIntegers(this.pA1[0], this.pA1[1]);
      this.pA1[0] = 3412047325613260800L;
      this.pA1[1] = 3412047325613260800L;
    }
    if ((this.pA2 != null) && (this.pA2[0] != 3412046827397054464L))
    {
      JdbcOdbc.ReleaseStoredIntegers(this.pA2[0], this.pA2[1]);
      this.pA2[0] = 3412047325613260800L;
      this.pA2[1] = 3412047325613260800L;
    }
  }

  public synchronized void FreeParams()
    throws NullPointerException
  {
    int i;
    try
    {
      for (i = 1; i <= this.boundParams.length; ++i)
      {
        if (this.boundParams[(i - 1)].pA1 != 3412047600491167744L)
        {
          JdbcOdbc.ReleaseStoredBytes(this.boundParams[(i - 1)].pA1, this.boundParams[(i - 1)].pA2);
          this.boundParams[(i - 1)].pA1 = 3412048390765150208L;
          this.boundParams[(i - 1)].pA2 = 3412048390765150208L;
        }
        if (this.boundParams[(i - 1)].pB1 != 3412047600491167744L)
        {
          JdbcOdbc.ReleaseStoredBytes(this.boundParams[(i - 1)].pB1, this.boundParams[(i - 1)].pB2);
          this.boundParams[(i - 1)].pB1 = 3412048390765150208L;
          this.boundParams[(i - 1)].pB2 = 3412048390765150208L;
        }
        if (this.boundParams[(i - 1)].pC1 != 3412047600491167744L)
        {
          JdbcOdbc.ReleaseStoredBytes(this.boundParams[(i - 1)].pC1, this.boundParams[(i - 1)].pC2);
          this.boundParams[(i - 1)].pC1 = 3412048390765150208L;
          this.boundParams[(i - 1)].pC2 = 3412048390765150208L;
        }
        if (this.boundParams[(i - 1)].pS1 != 3412047600491167744L)
        {
          JdbcOdbc.ReleaseStoredChars(this.boundParams[(i - 1)].pS1, this.boundParams[(i - 1)].pS2);
          this.boundParams[(i - 1)].pS1 = 3412048390765150208L;
          this.boundParams[(i - 1)].pS2 = 3412048390765150208L;
        }
      }
    }
    catch (NullPointerException localNullPointerException)
    {
    }
  }

  public void setSql(String paramString)
  {
    this.mySql = paramString.toUpperCase();
  }

  public Object[] getObjects()
  {
    Object[] arrayOfObject1 = new Object[this.numParams];
    Object[] arrayOfObject2 = this.arrayParams.getStoredParameterSet();
    if (arrayOfObject2 != null)
      try
      {
        for (int i = 0; i < this.numParams; ++i)
          arrayOfObject1[i] = arrayOfObject2[i];
      }
      catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
      {
        System.out.println("Exception, while calculating row count: " + localArrayIndexOutOfBoundsException.getMessage());
        localArrayIndexOutOfBoundsException.printStackTrace();
      }
    return arrayOfObject1;
  }

  public int[] getObjectTypes()
  {
    int[] arrayOfInt = new int[this.numParams];
    for (int i = 0; i < this.numParams; ++i)
      arrayOfInt[i] = this.boundParams[i].getSqlType();
    return arrayOfInt;
  }

  public int getParamCount()
  {
    return this.numParams;
  }

  protected void setInputParameter(int paramInt, boolean paramBoolean)
  {
    if ((paramInt >= 1) && (paramInt <= this.numParams))
      this.boundParams[(paramInt - 1)].setInputParameter(paramBoolean);
  }

  public void setURL(int paramInt, URL paramURL)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public ParameterMetaData getParameterMetaData()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setRowId(int paramInt, RowId paramRowId)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNString(int paramInt, String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNCharacterStream(int paramInt, Reader paramReader, long paramLong)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNClob(int paramInt, NClob paramNClob)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setClob(int paramInt, Reader paramReader, long paramLong)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBlob(int paramInt, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNClob(int paramInt, Reader paramReader, long paramLong)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setSQLXML(int paramInt, SQLXML paramSQLXML)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setPoolable(boolean paramBoolean)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean isPoolable()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean isClosed()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setCharacterStream(int paramInt, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void setBinaryStream(int paramInt, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void setAsciiStream(int paramInt, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void setAsciiStream(int paramInt, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void setBinaryStream(int paramInt, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void setCharacterStream(int paramInt, Reader paramReader)
    throws SQLException
  {
  }

  public void setNCharacterStream(int paramInt, Reader paramReader)
    throws SQLException
  {
  }

  public void setClob(int paramInt, Reader paramReader)
    throws SQLException
  {
  }

  public void setBlob(int paramInt, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void setNClob(int paramInt, Reader paramReader)
    throws SQLException
  {
  }
}
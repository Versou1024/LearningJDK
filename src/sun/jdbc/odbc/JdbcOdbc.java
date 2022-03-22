package sun.jdbc.odbc;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.AccessController;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import sun.security.action.LoadLibraryAction;

public class JdbcOdbc extends JdbcOdbcObject
{
  public static final int MajorVersion = 2;
  public static final int MinorVersion = 1;
  public String charSet;
  public String odbcDriverName;
  private static Map hstmtMap;
  public JdbcOdbcTracer tracer = new JdbcOdbcTracer();

  public static void addHstmt(long paramLong1, long paramLong2)
  {
    hstmtMap.put(new Long(paramLong1), new Long(paramLong2));
  }

  JdbcOdbc(JdbcOdbcTracer paramJdbcOdbcTracer, String paramString)
    throws SQLException
  {
    this.tracer = paramJdbcOdbcTracer;
    try
    {
      if (paramJdbcOdbcTracer.isTracing())
      {
        java.util.Date localDate = new java.util.Date();
        String str = "";
        int i = 1;
        if (i < 1000)
          str = str + "0";
        if (i < 100)
          str = str + "0";
        if (i < 10)
          str = str + "0";
        str = str + "" + i;
        paramJdbcOdbcTracer.trace("JDBC to ODBC Bridge 2." + str);
        paramJdbcOdbcTracer.trace("Current Date/Time: " + localDate.toString());
        paramJdbcOdbcTracer.trace("Loading " + paramString + "JdbcOdbc library");
      }
      AccessController.doPrivileged(new LoadLibraryAction(paramString + "JdbcOdbc"));
      if (hstmtMap == null)
        hstmtMap = Collections.synchronizedMap(new HashMap());
    }
    catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
    {
      if (paramJdbcOdbcTracer.isTracing())
        paramJdbcOdbcTracer.trace("Unable to load " + paramString + "JdbcOdbc library");
      throw new SQLException("Unable to load " + paramString + "JdbcOdbc library");
    }
  }

  public long SQLAllocConnect(long paramLong)
    throws SQLException
  {
    long l = 3412047153814568960L;
    if (this.tracer.isTracing())
      this.tracer.trace("Allocating Connection handle (SQLAllocConnect)");
    byte[] arrayOfByte = new byte[1];
    l = allocConnect(paramLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      throwGenericSQLException();
    else if (this.tracer.isTracing())
      this.tracer.trace("hDbc=" + l);
    return l;
  }

  public long SQLAllocEnv()
    throws SQLException
  {
    long l = 3412047153814568960L;
    if (this.tracer.isTracing())
      this.tracer.trace("Allocating Environment handle (SQLAllocEnv)");
    byte[] arrayOfByte = new byte[1];
    l = allocEnv(arrayOfByte);
    if (arrayOfByte[0] != 0)
      throwGenericSQLException();
    else if (this.tracer.isTracing())
      this.tracer.trace("hEnv=" + l);
    return l;
  }

  public long SQLAllocStmt(long paramLong)
    throws SQLException
  {
    long l = 3412047153814568960L;
    if (this.tracer.isTracing())
      this.tracer.trace("Allocating Statement Handle (SQLAllocStmt), hDbc=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    l = allocStmt(paramLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      throwGenericSQLException();
    else if (this.tracer.isTracing())
      this.tracer.trace("hStmt=" + l);
    addHstmt(l, paramLong);
    return l;
  }

  public void SQLBindColAtExec(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding Column DATA_AT_EXEC (SQLBindCol), hStmt=" + paramLong + ", icol=" + paramInt1 + ", SQLtype=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindColAtExec(paramLong, paramInt1, paramInt2, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindColBinary(long paramLong, int paramInt1, Object[] paramArrayOfObject, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Bind column binary (SQLBindColBinary), hStmt=" + paramLong + ", icol=" + paramInt1);
    byte[] arrayOfByte = new byte[1];
    bindColBinary(paramLong, paramInt1, paramArrayOfObject, paramArrayOfByte1, paramInt2, paramArrayOfByte2, paramArrayOfLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindColDate(long paramLong, int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Bound Column Date (SQLBindColDate), hStmt=" + paramLong + ", icol=" + paramInt);
    java.sql.Date localDate = null;
    int i = paramArrayOfObject.length;
    int[] arrayOfInt1 = new int[i];
    int[] arrayOfInt2 = new int[i];
    int[] arrayOfInt3 = new int[i];
    byte[] arrayOfByte = new byte[1];
    Calendar localCalendar = Calendar.getInstance();
    for (int j = 0; j < i; ++j)
      if (paramArrayOfObject[j] != null)
      {
        localDate = (java.sql.Date)paramArrayOfObject[j];
        localCalendar.setTime(localDate);
        arrayOfInt1[j] = localCalendar.get(1);
        arrayOfInt2[j] = (localCalendar.get(2) + 1);
        arrayOfInt3[j] = localCalendar.get(5);
      }
    bindColDate(paramLong, paramInt, arrayOfInt1, arrayOfInt2, arrayOfInt3, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindColDefault(long paramLong, int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding default column (SQLBindCol), hStmt=" + paramLong + ", ipar=" + paramInt + ", \t\t\tlength=" + paramArrayOfByte1.length);
    byte[] arrayOfByte = new byte[1];
    bindColDefault(paramLong, paramInt, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindColDouble(long paramLong, int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Bind column Double (SQLBindColDouble), hStmt=" + paramLong + ", icol=" + paramInt);
    double[] arrayOfDouble = new double[paramArrayOfObject.length];
    for (int i = 0; i < paramArrayOfObject.length; ++i)
      if (paramArrayOfObject[i] != null)
        arrayOfDouble[i] = ((Double)paramArrayOfObject[i]).doubleValue();
    byte[] arrayOfByte = new byte[1];
    bindColDouble(paramLong, paramInt, arrayOfDouble, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindColFloat(long paramLong, int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    float[] arrayOfFloat = new float[paramArrayOfObject.length];
    for (int i = 0; i < paramArrayOfObject.length; ++i)
      if (paramArrayOfObject[i] != null)
        arrayOfFloat[i] = ((Float)paramArrayOfObject[i]).floatValue();
    if (this.tracer.isTracing())
      this.tracer.trace("Binding default column (SQLBindCol Float), hStmt=" + paramLong + ", icol=" + paramInt);
    byte[] arrayOfByte = new byte[1];
    bindColFloat(paramLong, paramInt, arrayOfFloat, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindColInteger(long paramLong, int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding default column (SQLBindCol Integer), hStmt=" + paramLong + ", icol=" + paramInt);
    int[] arrayOfInt = new int[paramArrayOfObject.length];
    for (int i = 0; i < paramArrayOfObject.length; ++i)
      if (paramArrayOfObject[i] != null)
        arrayOfInt[i] = ((Integer)paramArrayOfObject[i]).intValue();
    byte[] arrayOfByte = new byte[1];
    bindColInteger(paramLong, paramInt, arrayOfInt, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindColString(long paramLong, int paramInt1, int paramInt2, Object[] paramArrayOfObject, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding string/decimal Column (SQLBindColString), hStmt=" + paramLong + ", icol=" + paramInt1 + ", SQLtype=" + paramInt2 + ", rgbValue=" + paramArrayOfObject);
    byte[] arrayOfByte = new byte[1];
    bindColString(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramArrayOfObject, paramInt3, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindColTime(long paramLong, int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Bind column Time (SQLBindColTime), hStmt=" + paramLong + ", icol=" + paramInt);
    Time localTime = null;
    int i = paramArrayOfObject.length;
    int[] arrayOfInt1 = new int[i];
    int[] arrayOfInt2 = new int[i];
    int[] arrayOfInt3 = new int[i];
    byte[] arrayOfByte = new byte[1];
    Calendar localCalendar = Calendar.getInstance();
    for (int j = 0; j < i; ++j)
      if (paramArrayOfObject[j] != null)
      {
        localTime = (Time)paramArrayOfObject[j];
        localCalendar.setTime(localTime);
        arrayOfInt1[j] = localCalendar.get(11);
        arrayOfInt2[j] = localCalendar.get(12);
        arrayOfInt3[j] = localCalendar.get(13);
      }
    bindColTime(paramLong, paramInt, arrayOfInt1, arrayOfInt2, arrayOfInt3, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindColTimestamp(long paramLong, int paramInt, Object[] paramArrayOfObject, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Bind Column Timestamp (SQLBindColTimestamp), hStmt=" + paramLong + ", icol=" + paramInt);
    Timestamp localTimestamp = null;
    int i = paramArrayOfObject.length;
    int[] arrayOfInt1 = new int[i];
    int[] arrayOfInt2 = new int[i];
    int[] arrayOfInt3 = new int[i];
    int[] arrayOfInt4 = new int[i];
    int[] arrayOfInt5 = new int[i];
    int[] arrayOfInt6 = new int[i];
    int[] arrayOfInt7 = new int[i];
    byte[] arrayOfByte = new byte[1];
    Calendar localCalendar = Calendar.getInstance();
    for (int j = 0; j < i; ++j)
      if (paramArrayOfObject[j] != null)
      {
        localTimestamp = (Timestamp)paramArrayOfObject[j];
        localCalendar.setTime(localTimestamp);
        arrayOfInt1[j] = localCalendar.get(1);
        arrayOfInt2[j] = (localCalendar.get(2) + 1);
        arrayOfInt3[j] = localCalendar.get(5);
        arrayOfInt4[j] = localCalendar.get(11);
        arrayOfInt5[j] = localCalendar.get(12);
        arrayOfInt6[j] = localCalendar.get(13);
        arrayOfInt7[j] = localTimestamp.getNanos();
      }
    bindColTimestamp(paramLong, paramInt, arrayOfInt1, arrayOfInt2, arrayOfInt3, arrayOfInt4, arrayOfInt5, arrayOfInt6, arrayOfInt7, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterAtExec(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding DATA_AT_EXEC parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", len=" + paramInt3);
    byte[] arrayOfByte = new byte[1];
    bindInParameterAtExec(paramLong, paramInt1, paramInt2, paramInt3, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterAtExec(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, int paramInt5, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding DATA_AT_EXEC parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt3 + ", streamLength = " + paramInt5 + " ,dataBufLen = " + paramInt4);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterAtExec(paramLong, paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfByte1, paramInt5, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterBinary(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong)
    throws SQLException
  {
    int i = 0;
    if (paramArrayOfByte2.length < 8000)
      i = paramArrayOfByte2.length;
    else
      i = 8000;
    if (this.tracer.isTracing())
    {
      this.tracer.trace("Binding IN binary parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2);
      dumpByte(paramArrayOfByte1, paramArrayOfByte1.length);
    }
    byte[] arrayOfByte = new byte[1];
    bindInParameterBinary(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramArrayOfByte1, i, paramArrayOfByte2, paramArrayOfByte3, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterDate(long paramLong, int paramInt, java.sql.Date paramDate, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter date (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt + ", rgbValue=" + paramDate.toString());
    byte[] arrayOfByte = new byte[1];
    Calendar localCalendar = Calendar.getInstance();
    localCalendar.setTime(paramDate);
    bindInParameterDate(paramLong, paramInt, localCalendar.get(1), localCalendar.get(2) + 1, localCalendar.get(5), paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterCalendarDate(long paramLong, int paramInt, Calendar paramCalendar, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter date (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt + ", rgbValue=" + paramCalendar.toString());
    byte[] arrayOfByte = new byte[1];
    bindInParameterDate(paramLong, paramInt, paramCalendar.get(1), paramCalendar.get(2) + 1, paramCalendar.get(5), paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterDouble(long paramLong, int paramInt1, int paramInt2, int paramInt3, double paramDouble, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter double (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", scale=" + paramInt3 + ", rgbValue=" + paramDouble);
    byte[] arrayOfByte = new byte[1];
    bindInParameterDouble(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramDouble, paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterFloat(long paramLong, int paramInt1, int paramInt2, int paramInt3, float paramFloat, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter float (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", scale=" + paramInt3 + ", rgbValue=" + paramFloat);
    byte[] arrayOfByte = new byte[1];
    bindInParameterFloat(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramFloat, paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterInteger(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter integer (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + "SQLtype=" + paramInt2 + ", rgbValue=" + paramInt3);
    byte[] arrayOfByte = new byte[1];
    bindInParameterInteger(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterNull(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN NULL parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindInParameterNull(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramInt4, paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterString(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN string parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", precision=" + paramInt3 + ", scale=" + paramInt4 + ", rgbValue=" + paramArrayOfByte1);
    byte[] arrayOfByte = new byte[1];
    bindInParameterString(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramArrayOfByte1, paramInt3, paramInt4, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterTime(long paramLong, int paramInt, Time paramTime, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter time (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt + ", rgbValue=" + paramTime.toString());
    byte[] arrayOfByte = new byte[1];
    Calendar localCalendar = Calendar.getInstance();
    localCalendar.setTime(paramTime);
    bindInParameterTime(paramLong, paramInt, localCalendar.get(11), localCalendar.get(12), localCalendar.get(13), paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterCalendarTime(long paramLong, int paramInt, Calendar paramCalendar, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter time (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt + ", rgbValue=" + paramCalendar.toString());
    byte[] arrayOfByte = new byte[1];
    bindInParameterTime(paramLong, paramInt, paramCalendar.get(11), paramCalendar.get(12), paramCalendar.get(13), paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterTimestamp(long paramLong, int paramInt, Timestamp paramTimestamp, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter timestamp (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt + ", rgbValue=" + paramTimestamp.toString());
    byte[] arrayOfByte = new byte[1];
    Calendar localCalendar = Calendar.getInstance();
    localCalendar.setTime(paramTimestamp);
    bindInParameterTimestamp(paramLong, paramInt, localCalendar.get(1), localCalendar.get(2) + 1, localCalendar.get(5), localCalendar.get(11), localCalendar.get(12), localCalendar.get(13), paramTimestamp.getNanos(), paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterCalendarTimestamp(long paramLong, int paramInt, Calendar paramCalendar, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter timestamp (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt + ", rgbValue=" + paramCalendar.toString());
    byte[] arrayOfByte = new byte[1];
    int i = paramCalendar.get(14) * 1000000;
    bindInParameterTimestamp(paramLong, paramInt, paramCalendar.get(1), paramCalendar.get(2) + 1, paramCalendar.get(5), paramCalendar.get(11), paramCalendar.get(12), paramCalendar.get(13), i, paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterBigint(long paramLong1, int paramInt1, int paramInt2, int paramInt3, long paramLong2, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter bigint (SQLBindParameter), hStmt=" + paramLong1 + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", scale=" + paramInt3 + ", rgbValue=" + paramLong2);
    byte[] arrayOfByte = new byte[1];
    bindInParameterBigint(paramLong1, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramLong2, paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong1);
  }

  public void SQLBindOutParameterString(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding OUT string parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", prec=" + (paramArrayOfByte1.length - 1) + ", scale=" + paramInt3);
    byte[] arrayOfByte = new byte[1];
    bindOutParameterString(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterDate(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN OUT date parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", prec=" + (paramArrayOfByte1.length - 1) + ", scale=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterDate(paramLong, paramInt1, paramInt2, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterTime(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN OUT time parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", prec=" + (paramArrayOfByte1.length - 1) + ", scale=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterTime(paramLong, paramInt1, paramInt2, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterTimestamp(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN OUT time parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", scale=" + paramInt3 + "length = " + (paramArrayOfByte1.length - 1) + ", precision=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterTimestamp(paramLong, paramInt1, paramInt2, paramInt3, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterString(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding INOUT string parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", precision=" + paramInt3 + ", scale=" + paramInt4 + ", rgbValue=" + paramArrayOfByte1 + ", lenBuf=" + paramArrayOfByte2);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterString(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramInt4, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterStr(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, int paramInt4)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding INOUT string parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", precision=" + paramInt3 + ", rgbValue=" + paramArrayOfByte1 + ", lenBuf=" + paramArrayOfByte2);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterStr(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong, paramInt4);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterBin(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, int paramInt4)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding INOUT binary parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", precision=" + paramInt3 + ", rgbValue=" + paramArrayOfByte1 + ", lenBuf=" + paramArrayOfByte2);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterBin(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong, paramInt4);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterBinary(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding INOUT binary parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", precision=" + paramInt3 + ", scale=" + paramInt4 + ", rgbValue=" + paramArrayOfByte1 + ", lenBuf=" + paramArrayOfByte2);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterBinary(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramInt4, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterFixed(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN OUT parameter for fixed types (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + "SQLtype=" + paramInt2 + ", maxLen=" + paramInt3);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterFixed(paramLong, paramInt1, OdbcDef.jdbcTypeToCType(paramInt2), OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterTimeStamp(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding INOUT string parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", precision=" + paramInt3 + ", scale=" + paramInt4 + ", rgbValue=" + paramArrayOfByte1 + ", lenBuf=" + paramArrayOfByte2);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterTimeStamp(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramInt4, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameter(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, double paramDouble, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding INOUT parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", precision=" + paramInt3 + ", scale=" + paramInt4 + ", rgbValue=" + paramDouble);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameter(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramInt4, paramDouble, paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInOutParameterNull(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN OUT NULL parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindInOutParameterNull(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramInt4, paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterStringArray(long paramLong, int paramInt1, int paramInt2, Object[] paramArrayOfObject, int paramInt3, int paramInt4, int[] paramArrayOfInt)
    throws SQLException
  {
    int i = paramArrayOfObject.length;
    Object[] arrayOfObject = new Object[i];
    if ((paramInt2 == 2) || (paramInt2 == 3))
      for (int j = 0; j < i; ++j)
        if (paramArrayOfObject[j] != null)
        {
          localObject = (BigDecimal)paramArrayOfObject[j];
          String str1 = ((BigDecimal)localObject).toString();
          int k = str1.indexOf(46);
          if (k != -1)
          {
            String str2 = str1.substring(k + 1, str1.length());
            int l = str2.length();
            if (l < paramInt4)
              for (int i1 = 0; i1 < paramInt4 - l; ++i1)
                str1 = str1 + "0";
          }
          arrayOfObject[j] = str1;
        }
    else
      arrayOfObject = paramArrayOfObject;
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter timestamp (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1);
    byte[] arrayOfByte = new byte[1];
    Object localObject = new byte[(paramInt3 + 1) * i];
    bindInParameterStringArray(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), arrayOfObject, localObject, paramInt3, paramInt4, paramArrayOfInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterIntegerArray(long paramLong, int paramInt1, int paramInt2, Object[] paramArrayOfObject, int[] paramArrayOfInt)
    throws SQLException
  {
    int[] arrayOfInt = new int[paramArrayOfObject.length];
    for (int i = 0; i < paramArrayOfObject.length; ++i)
      if (paramArrayOfObject[i] != null)
        arrayOfInt[i] = ((Integer)paramArrayOfObject[i]).intValue();
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter Integer Array (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1);
    byte[] arrayOfByte = new byte[1];
    bindInParameterIntegerArray(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), arrayOfInt, paramArrayOfInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterFloatArray(long paramLong, int paramInt1, int paramInt2, Object[] paramArrayOfObject, int[] paramArrayOfInt)
    throws SQLException
  {
    float[] arrayOfFloat = new float[paramArrayOfObject.length];
    for (int i = 0; i < paramArrayOfObject.length; ++i)
      if (paramArrayOfObject[i] != null)
        arrayOfFloat[i] = ((Float)paramArrayOfObject[i]).floatValue();
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter timestamp (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1);
    byte[] arrayOfByte = new byte[1];
    bindInParameterFloatArray(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), 0, arrayOfFloat, paramArrayOfInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterDoubleArray(long paramLong, int paramInt1, int paramInt2, Object[] paramArrayOfObject, int[] paramArrayOfInt)
    throws SQLException
  {
    double[] arrayOfDouble = new double[paramArrayOfObject.length];
    for (int i = 0; i < paramArrayOfObject.length; ++i)
      if (paramArrayOfObject[i] != null)
        arrayOfDouble[i] = ((Double)paramArrayOfObject[i]).doubleValue();
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter timestamp (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1);
    byte[] arrayOfByte = new byte[1];
    bindInParameterDoubleArray(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), 0, arrayOfDouble, paramArrayOfInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterDateArray(long paramLong, int paramInt, Object[] paramArrayOfObject, int[] paramArrayOfInt)
    throws SQLException
  {
    Calendar localCalendar;
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter timestamp (SQLBindParameterDateArray), hStmt=" + paramLong + ", ipar=" + paramInt);
    int i = paramArrayOfObject.length;
    int[] arrayOfInt1 = new int[i];
    int[] arrayOfInt2 = new int[i];
    int[] arrayOfInt3 = new int[i];
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = new byte[11 * i];
    if ((java.sql.Date)paramArrayOfObject[0] != null)
    {
      localCalendar = Calendar.getInstance();
      java.sql.Date localDate = null;
      for (int k = 0; k < i; ++k)
        if (paramArrayOfObject[k] != null)
        {
          localDate = (java.sql.Date)paramArrayOfObject[k];
          localCalendar.setTime(localDate);
          arrayOfInt1[k] = localCalendar.get(1);
          arrayOfInt2[k] = (localCalendar.get(2) + 1);
          arrayOfInt3[k] = localCalendar.get(5);
        }
    }
    else
    {
      for (int j = 0; j < i; ++j)
        if (paramArrayOfObject[j] != null)
        {
          localCalendar = (Calendar)paramArrayOfObject[j];
          arrayOfInt1[j] = localCalendar.get(1);
          arrayOfInt2[j] = (localCalendar.get(2) + 1);
          arrayOfInt3[j] = localCalendar.get(5);
        }
    }
    bindInParameterDateArray(paramLong, paramInt, arrayOfInt1, arrayOfInt2, arrayOfInt3, arrayOfByte2, arrayOfByte1, paramArrayOfInt);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterTimeArray(long paramLong, int paramInt, Object[] paramArrayOfObject, int[] paramArrayOfInt)
    throws SQLException
  {
    Calendar localCalendar;
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter timestamp (SQLBindParameterTimeArray), hStmt=" + paramLong + ", ipar=" + paramInt);
    int i = paramArrayOfObject.length;
    int[] arrayOfInt1 = new int[i];
    int[] arrayOfInt2 = new int[i];
    int[] arrayOfInt3 = new int[i];
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = new byte[9 * i];
    if ((Time)paramArrayOfObject[0] != null)
    {
      localCalendar = Calendar.getInstance();
      Time localTime = null;
      for (int k = 0; k < i; ++k)
        if (paramArrayOfObject[k] != null)
        {
          localTime = (Time)paramArrayOfObject[k];
          localCalendar.setTime(localTime);
          arrayOfInt1[k] = localCalendar.get(11);
          arrayOfInt2[k] = localCalendar.get(12);
          arrayOfInt3[k] = localCalendar.get(13);
        }
    }
    else
    {
      for (int j = 0; j < i; ++j)
        if (paramArrayOfObject[j] != null)
        {
          localCalendar = (Calendar)paramArrayOfObject[j];
          arrayOfInt1[j] = localCalendar.get(11);
          arrayOfInt2[j] = localCalendar.get(12);
          arrayOfInt3[j] = localCalendar.get(13);
        }
    }
    bindInParameterTimeArray(paramLong, paramInt, arrayOfInt1, arrayOfInt2, arrayOfInt3, arrayOfByte2, arrayOfByte1, paramArrayOfInt);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterTimestampArray(long paramLong, int paramInt, Object[] paramArrayOfObject, int[] paramArrayOfInt)
    throws SQLException
  {
    Calendar localCalendar;
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN parameter timestamp (SQLBindParameterTimestampArray), hStmt=" + paramLong + ", ipar=" + paramInt);
    int i = paramArrayOfObject.length;
    int[] arrayOfInt1 = new int[i];
    int[] arrayOfInt2 = new int[i];
    int[] arrayOfInt3 = new int[i];
    int[] arrayOfInt4 = new int[i];
    int[] arrayOfInt5 = new int[i];
    int[] arrayOfInt6 = new int[i];
    int[] arrayOfInt7 = new int[i];
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = new byte[30 * i];
    if ((Timestamp)paramArrayOfObject[0] != null)
    {
      localCalendar = Calendar.getInstance();
      Timestamp localTimestamp = null;
      for (int k = 0; k < i; ++k)
        if (paramArrayOfObject[k] != null)
        {
          localTimestamp = (Timestamp)paramArrayOfObject[k];
          localCalendar.setTime(localTimestamp);
          arrayOfInt1[k] = localCalendar.get(1);
          arrayOfInt2[k] = (localCalendar.get(2) + 1);
          arrayOfInt3[k] = localCalendar.get(5);
          arrayOfInt4[k] = localCalendar.get(11);
          arrayOfInt5[k] = localCalendar.get(12);
          arrayOfInt6[k] = localCalendar.get(13);
          arrayOfInt7[k] = localTimestamp.getNanos();
        }
    }
    else
    {
      for (int j = 0; j < i; ++j)
        if (paramArrayOfObject[j] != null)
        {
          localCalendar = (Calendar)paramArrayOfObject[j];
          arrayOfInt1[j] = localCalendar.get(1);
          arrayOfInt2[j] = (localCalendar.get(2) + 1);
          arrayOfInt3[j] = localCalendar.get(5);
          arrayOfInt4[j] = localCalendar.get(11);
          arrayOfInt5[j] = localCalendar.get(12);
          arrayOfInt6[j] = localCalendar.get(13);
          arrayOfInt7[j] = localCalendar.get(14);
        }
    }
    bindInParameterTimestampArray(paramLong, paramInt, arrayOfInt1, arrayOfInt2, arrayOfInt3, arrayOfInt4, arrayOfInt5, arrayOfInt6, arrayOfInt7, arrayOfByte2, arrayOfByte1, paramArrayOfInt);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterBinaryArray(long paramLong, int paramInt1, int paramInt2, Object[] paramArrayOfObject, int paramInt3, int[] paramArrayOfInt)
    throws SQLException
  {
    int i = paramArrayOfObject.length;
    int j = 8000;
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN binary parameter (SQLBindParameterBinaryArray), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = new byte[paramInt3 * i];
    bindInParameterBinaryArray(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramArrayOfObject, paramInt3, arrayOfByte2, paramArrayOfInt, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindInParameterAtExecArray(long paramLong, int paramInt1, int paramInt2, int paramInt3, int[] paramArrayOfInt)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding DATA_AT_EXEC Array parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", len=" + paramInt3);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = new byte[paramArrayOfInt.length];
    bindInParameterAtExecArray(paramLong, paramInt1, paramInt2, paramInt3, arrayOfByte2, paramArrayOfInt, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindOutParameterNull(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding OUT NULL parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindOutParameterNull(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramInt4, paramArrayOfByte, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindOutParameterFixed(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding OUT string parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", maxLen=" + paramInt3);
    byte[] arrayOfByte = new byte[1];
    bindOutParameterFixed(paramLong, paramInt1, OdbcDef.jdbcTypeToCType(paramInt2), OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindOutParameterBinary(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding INOUT binary parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", SQLtype=" + paramInt2 + ", precision=" + paramInt3 + ", scale=" + paramInt4 + ", rgbValue=" + paramArrayOfByte1 + ", lenBuf=" + paramArrayOfByte2);
    byte[] arrayOfByte = new byte[1];
    bindOutParameterBinary(paramLong, paramInt1, OdbcDef.jdbcTypeToOdbc(paramInt2), paramInt3, paramInt4, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindOutParameterDate(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN OUT date parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", prec=" + (paramArrayOfByte1.length - 1) + ", scale=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindOutParameterDate(paramLong, paramInt1, paramInt2, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindOutParameterTime(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding IN OUT time parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", prec=" + (paramArrayOfByte1.length - 1) + ", scale=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindOutParameterTime(paramLong, paramInt1, paramInt2, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLBindOutParameterTimestamp(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Binding OUT time parameter (SQLBindParameter), hStmt=" + paramLong + ", ipar=" + paramInt1 + ", prec=" + (paramArrayOfByte1.length - 1) + ", precision=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    bindOutParameterTimestamp(paramLong, paramInt1, paramInt2, paramArrayOfByte1, paramArrayOfByte2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public String SQLBrowseConnect(long paramLong, String paramString)
    throws SQLException, SQLWarning
  {
    String str = null;
    if (this.tracer.isTracing())
      this.tracer.trace("Connecting (SQLBrowseConnect), hDbc=" + paramLong + ", szConnStrIn=" + paramString);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = new byte[2000];
    byte[] arrayOfByte3 = null;
    char[] arrayOfChar = null;
    if (paramString != null)
      arrayOfChar = paramString.toCharArray();
    try
    {
      if (paramString != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    browseConnect(paramLong, arrayOfByte3, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] == 99)
    {
      str = new String(arrayOfByte2);
      str = str.trim();
      arrayOfByte1[0] = 0;
    }
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, paramLong, 3412047669210644480L);
    if (this.tracer.isTracing())
      this.tracer.trace("Attributes=" + str);
    return str;
  }

  public void SQLCancel(long paramLong)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Cancelling (SQLCancel), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    cancel(paramLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public int SQLColAttributes(long paramLong, int paramInt1, int paramInt2)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("Column attributes (SQLColAttributes), hStmt=" + paramLong + ", icol=" + paramInt1 + ", type=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    i = colAttributes(paramLong, paramInt1, paramInt2, arrayOfByte);
    if (arrayOfByte[0] != 0)
      try
      {
        standardError((short)arrayOfByte[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (this.tracer.isTracing())
          this.tracer.trace("value (int)=" + i);
        localJdbcOdbcSQLWarning.value = BigDecimal.valueOf(i);
        throw localJdbcOdbcSQLWarning;
      }
    if (this.tracer.isTracing())
      this.tracer.trace("value (int)=" + i);
    return i;
  }

  public String SQLColAttributesString(long paramLong, int paramInt1, int paramInt2)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Column attributes (SQLColAttributes), hStmt=" + paramLong + ", icol=" + paramInt1 + ", type=" + paramInt2);
    byte[] arrayOfByte2 = new byte[1];
    byte[] arrayOfByte1 = new byte[300];
    colAttributesString(paramLong, paramInt1, paramInt2, arrayOfByte1, arrayOfByte2);
    if (arrayOfByte2[0] != 0)
      try
      {
        standardError((short)arrayOfByte2[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        String str2 = new String();
        try
        {
          str2 = BytesToChars(this.charSet, arrayOfByte1);
        }
        catch (UnsupportedEncodingException localUnsupportedEncodingException2)
        {
          System.out.println(localUnsupportedEncodingException2);
        }
        if (this.tracer.isTracing())
          this.tracer.trace("value (String)=" + str2.trim());
        localJdbcOdbcSQLWarning.value = str2.trim();
        throw localJdbcOdbcSQLWarning;
      }
    String str1 = new String();
    try
    {
      str1 = BytesToChars(this.charSet, arrayOfByte1);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException1)
    {
      System.out.println(localUnsupportedEncodingException1);
    }
    if (this.tracer.isTracing())
      this.tracer.trace("value (String)=" + str1.trim());
    return str1.trim();
  }

  public void SQLColumns(long paramLong, String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("(SQLColumns), hStmt=" + paramLong + ", catalog=" + paramString1 + ", schema=" + paramString2 + ", table=" + paramString3 + ", column=" + paramString4);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    byte[] arrayOfByte5 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    char[] arrayOfChar4 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    if (paramString4 != null)
      arrayOfChar4 = paramString4.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
      if (paramString4 != null)
        arrayOfByte5 = CharsToBytes(this.charSet, arrayOfChar4);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    columns(paramLong, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, arrayOfByte5, (paramString4 == null) ? 1 : false, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLColumnPrivileges(long paramLong, String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("(SQLColumnPrivileges), hStmt=" + paramLong + ", catalog=" + paramString1 + ", schema=" + paramString2 + ", table=" + paramString3 + ", column=" + paramString4);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    byte[] arrayOfByte5 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    char[] arrayOfChar4 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    if (paramString4 != null)
      arrayOfChar4 = paramString4.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
      if (paramString4 != null)
        arrayOfByte5 = CharsToBytes(this.charSet, arrayOfChar4);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    columnPrivileges(paramLong, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, arrayOfByte5, (paramString4 == null) ? 1 : false, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public boolean SQLDescribeParamNullable(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    boolean bool = false;
    if (this.tracer.isTracing())
      this.tracer.trace("Parameter nullable (SQLDescribeParam), hStmt=" + paramLong + ", ipar=" + paramInt);
    byte[] arrayOfByte = new byte[1];
    int i = describeParam(paramLong, paramInt, 4, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    if (i == 1)
      bool = true;
    if (this.tracer.isTracing())
      this.tracer.trace("nullable=" + bool);
    return bool;
  }

  public int SQLDescribeParamPrecision(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Parameter precision (SQLDescribeParam), hStmt=" + paramLong + ", ipar=" + paramInt);
    byte[] arrayOfByte = new byte[1];
    int i = describeParam(paramLong, paramInt, 2, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    if (this.tracer.isTracing())
      this.tracer.trace("precision=" + i);
    return i;
  }

  public int SQLDescribeParamScale(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Parameter scale (SQLDescribeParam), hStmt=" + paramLong + ", ipar=" + paramInt);
    byte[] arrayOfByte = new byte[1];
    int i = describeParam(paramLong, paramInt, 3, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    if (this.tracer.isTracing())
      this.tracer.trace("scale=" + i);
    return i;
  }

  public int SQLDescribeParamType(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Parameter type (SQLDescribeParam), hStmt=" + paramLong + ", ipar=" + paramInt);
    byte[] arrayOfByte = new byte[1];
    int i = describeParam(paramLong, paramInt, 1, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    if (this.tracer.isTracing())
      this.tracer.trace("type=" + i);
    return i;
  }

  public void SQLDisconnect(long paramLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Disconnecting (SQLDisconnect), hDbc=" + paramLong);
    Set localSet = hstmtMap.keySet();
    Object[] arrayOfObject = localSet.toArray();
    int i = arrayOfObject.length;
    for (int j = 0; j < i; ++j)
    {
      Long localLong = (Long)hstmtMap.get(arrayOfObject[j]);
      if ((localLong != null) && (localLong.longValue() == paramLong))
        SQLFreeStmt(((Long)arrayOfObject[j]).longValue(), 1);
    }
    byte[] arrayOfByte = new byte[1];
    disconnect(paramLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, paramLong, 3412047669210644480L);
  }

  public void SQLDriverConnect(long paramLong, String paramString)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Connecting (SQLDriverConnect), hDbc=" + paramLong + ", szConnStrIn=" + paramString);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    char[] arrayOfChar = null;
    if (paramString != null)
      arrayOfChar = paramString.toCharArray();
    try
    {
      if (paramString != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    driverConnect(paramLong, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, paramLong, 3412047669210644480L);
  }

  public void SQLExecDirect(long paramLong, String paramString)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Executing (SQLExecDirect), hStmt=" + paramLong + ", szSqlStr=" + paramString);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    char[] arrayOfChar = null;
    if (paramString != null)
      arrayOfChar = paramString.toCharArray();
    try
    {
      if (paramString != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    execDirect(paramLong, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public boolean SQLExecute(long paramLong)
    throws SQLException, SQLWarning
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("Executing (SQLExecute), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    execute(paramLong, arrayOfByte);
    if (arrayOfByte[0] == 99)
    {
      if (this.tracer.isTracing())
        this.tracer.trace("SQL_NEED_DATA returned");
      i = 1;
      arrayOfByte[0] = 0;
    }
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    return i;
  }

  public boolean SQLFetch(long paramLong)
    throws SQLException, SQLWarning
  {
    int i = 1;
    if (this.tracer.isTracing())
      this.tracer.trace("Fetching (SQLFetch), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    fetch(paramLong, arrayOfByte);
    if (arrayOfByte[0] == 100)
    {
      i = 0;
      arrayOfByte[0] = 0;
      if (this.tracer.isTracing())
        this.tracer.trace("End of result set (SQL_NO_DATA)");
    }
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    return i;
  }

  public boolean SQLFetchScroll(long paramLong, short paramShort, int paramInt)
    throws SQLException, SQLWarning
  {
    int i = 1;
    if (this.tracer.isTracing())
      this.tracer.trace("Fetching (SQLFetchScroll), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    fetchScroll(paramLong, paramShort, paramInt, arrayOfByte);
    if (arrayOfByte[0] == 100)
    {
      i = 0;
      arrayOfByte[0] = 0;
      if (this.tracer.isTracing())
        this.tracer.trace("End of result set (SQL_NO_DATA)");
    }
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    return i;
  }

  public void SQLForeignKeys(long paramLong, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("(SQLForeignKeys), hStmt=" + paramLong + ", Pcatalog=" + paramString1 + ", Pschema=" + paramString2 + ", Ptable=" + paramString3 + ", Fcatalog=" + paramString4 + ", Fschema=" + paramString5 + ", Ftable=" + paramString6);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    byte[] arrayOfByte5 = null;
    byte[] arrayOfByte6 = null;
    byte[] arrayOfByte7 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    char[] arrayOfChar4 = null;
    char[] arrayOfChar5 = null;
    char[] arrayOfChar6 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    if (paramString4 != null)
      arrayOfChar4 = paramString4.toCharArray();
    if (paramString5 != null)
      arrayOfChar5 = paramString5.toCharArray();
    if (paramString6 != null)
      arrayOfChar6 = paramString6.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
      if (paramString4 != null)
        arrayOfByte5 = CharsToBytes(this.charSet, arrayOfChar4);
      if (paramString5 != null)
        arrayOfByte6 = CharsToBytes(this.charSet, arrayOfChar5);
      if (paramString6 != null)
        arrayOfByte7 = CharsToBytes(this.charSet, arrayOfChar6);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    foreignKeys(paramLong, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, arrayOfByte5, (paramString4 == null) ? 1 : false, arrayOfByte6, (paramString5 == null) ? 1 : false, arrayOfByte7, (paramString6 == null) ? 1 : false, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLFreeConnect(long paramLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Closing connection (SQLFreeConnect), hDbc=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    freeConnect(paramLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, paramLong, 3412047669210644480L);
  }

  public void SQLFreeEnv(long paramLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Closing environment (SQLFreeEnv), hEnv=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    freeEnv(paramLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      throwGenericSQLException();
  }

  public synchronized void SQLFreeStmt(long paramLong, int paramInt)
    throws SQLException
  {
    byte[] arrayOfByte = new byte[1];
    Long localLong = new Long(paramLong);
    if (paramInt == 1)
    {
      if (hstmtMap.containsKey(localLong))
      {
        hstmtMap.remove(localLong);
        freeStmt(paramLong, paramInt, arrayOfByte);
        if (this.tracer.isTracing())
          this.tracer.trace("Free statement (SQLFreeStmt), hStmt=" + paramLong + ", fOption=" + paramInt);
      }
    }
    else
    {
      freeStmt(paramLong, paramInt, arrayOfByte);
      if (this.tracer.isTracing())
        this.tracer.trace("Free statement (SQLFreeStmt), hStmt=" + paramLong + ", fOption=" + paramInt);
    }
    if (arrayOfByte[0] != 0)
      throwGenericSQLException();
  }

  public long SQLGetConnectOption(long paramLong, short paramShort)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Connection Option (SQLGetConnectOption), hDbc=" + paramLong + ", fOption=" + paramShort);
    byte[] arrayOfByte = new byte[1];
    long l = getConnectOption(paramLong, paramShort, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, paramLong, 3412047669210644480L);
    if (this.tracer.isTracing())
      this.tracer.trace("option value (int)=" + l);
    return l;
  }

  public String SQLGetConnectOptionString(long paramLong, short paramShort)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Connection Option (SQLGetConnectOption), hDbc=" + paramLong + ", fOption=" + paramShort);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = new byte[300];
    getConnectOptionString(paramLong, paramShort, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, paramLong, 3412047669210644480L);
    String str = new String();
    try
    {
      str = BytesToChars(this.charSet, arrayOfByte2);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    if (this.tracer.isTracing())
      this.tracer.trace("option value (int)=" + str.trim());
    return str.trim();
  }

  public String SQLGetCursorName(long paramLong)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Cursor name (SQLGetCursorName), hStmt=" + paramLong);
    byte[] arrayOfByte2 = new byte[1];
    byte[] arrayOfByte1 = new byte[300];
    getCursorName(paramLong, arrayOfByte1, arrayOfByte2);
    if (arrayOfByte2[0] != 0)
      try
      {
        standardError((short)arrayOfByte2[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        String str2 = new String();
        try
        {
          str2 = BytesToChars(this.charSet, arrayOfByte1);
        }
        catch (UnsupportedEncodingException localUnsupportedEncodingException)
        {
        }
        if (this.tracer.isTracing())
          this.tracer.trace("value=" + str2.trim());
        localJdbcOdbcSQLWarning.value = str2.trim();
        throw localJdbcOdbcSQLWarning;
      }
    String str1 = new String(arrayOfByte1);
    if (this.tracer.isTracing())
      this.tracer.trace("value=" + str1.trim());
    return str1.trim();
  }

  public int SQLGetDataBinary(long paramLong, int paramInt, byte[] paramArrayOfByte)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    return SQLGetDataBinary(paramLong, paramInt, -2, paramArrayOfByte, paramArrayOfByte.length);
  }

  public int SQLGetDataBinary(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("Get binary data (SQLGetData), hStmt=" + paramLong + ", column=" + paramInt1 + ", type=" + paramInt2 + ", length=" + paramInt3);
    byte[] arrayOfByte = new byte[2];
    i = getDataBinary(paramLong, paramInt1, paramInt2, paramArrayOfByte, paramInt3, arrayOfByte);
    if (arrayOfByte[0] == 100)
    {
      i = -1;
      arrayOfByte[0] = 0;
    }
    if (arrayOfByte[0] != 0)
      try
      {
        standardError((short)arrayOfByte[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (this.tracer.isTracing())
          if (i == -1)
            this.tracer.trace("NULL");
          else if (this.tracer.isTracing())
            this.tracer.trace("Bytes: " + i);
        localJdbcOdbcSQLWarning.value = new Integer(i);
        throw localJdbcOdbcSQLWarning;
      }
    if (this.tracer.isTracing())
      if (i == -1)
        this.tracer.trace("NULL");
      else if (this.tracer.isTracing())
        this.tracer.trace("Bytes: " + i);
    return i;
  }

  public Double SQLGetDataDouble(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get double data (SQLGetData), hStmt=" + paramLong + ", column=" + paramInt);
    byte[] arrayOfByte = new byte[2];
    double d = getDataDouble(paramLong, paramInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      try
      {
        standardError((short)arrayOfByte[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (arrayOfByte[1] == 0)
        {
          if (this.tracer.isTracing())
            this.tracer.trace("value=" + d);
          localJdbcOdbcSQLWarning.value = new Double(d);
        }
        else
        {
          if (this.tracer.isTracing())
            this.tracer.trace("NULL");
          localJdbcOdbcSQLWarning.value = null;
        }
        throw localJdbcOdbcSQLWarning;
      }
    if (arrayOfByte[1] == 0)
    {
      if (this.tracer.isTracing())
        this.tracer.trace("value=" + d);
      return new Double(d);
    }
    if (this.tracer.isTracing())
      this.tracer.trace("NULL");
    return null;
  }

  public Float SQLGetDataFloat(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get float data (SQLGetData), hStmt=" + paramLong + ", column=" + paramInt);
    byte[] arrayOfByte = new byte[2];
    float f = (float)getDataFloat(paramLong, paramInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      try
      {
        standardError((short)arrayOfByte[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (arrayOfByte[1] == 0)
        {
          if (this.tracer.isTracing())
            this.tracer.trace("value=" + f);
          localJdbcOdbcSQLWarning.value = new Float(f);
        }
        else
        {
          if (this.tracer.isTracing())
            this.tracer.trace("NULL");
          localJdbcOdbcSQLWarning.value = null;
        }
        throw localJdbcOdbcSQLWarning;
      }
    if (arrayOfByte[1] == 0)
    {
      if (this.tracer.isTracing())
        this.tracer.trace("value=" + f);
      return new Float(f);
    }
    if (this.tracer.isTracing())
      this.tracer.trace("NULL");
    return null;
  }

  public Integer SQLGetDataInteger(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get integer data (SQLGetData), hStmt=" + paramLong + ", column=" + paramInt);
    byte[] arrayOfByte = new byte[2];
    int i = getDataInteger(paramLong, paramInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      try
      {
        standardError((short)arrayOfByte[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (arrayOfByte[1] == 0)
        {
          if (this.tracer.isTracing())
            this.tracer.trace("value=" + i);
          localJdbcOdbcSQLWarning.value = new Integer(i);
        }
        else
        {
          if (this.tracer.isTracing())
            this.tracer.trace("NULL");
          localJdbcOdbcSQLWarning.value = null;
        }
        throw localJdbcOdbcSQLWarning;
      }
    if (arrayOfByte[1] == 0)
    {
      if (this.tracer.isTracing())
        this.tracer.trace("value=" + i);
      return new Integer(i);
    }
    if (this.tracer.isTracing())
      this.tracer.trace("NULL");
    return null;
  }

  public String SQLGetDataString(long paramLong, int paramInt1, int paramInt2, boolean paramBoolean)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    char[] arrayOfChar;
    if (this.tracer.isTracing())
      this.tracer.trace("Get string data (SQLGetData), hStmt=" + paramLong + ", column=" + paramInt1 + ", maxLen=" + paramInt2);
    byte[] arrayOfByte1 = new byte[2];
    byte[] arrayOfByte2 = new byte[paramInt2];
    int i = getDataString(paramLong, paramInt1, arrayOfByte2, arrayOfByte1);
    if (i < 0)
      arrayOfByte1[1] = 1;
    if (i > paramInt2)
      i = paramInt2;
    if (arrayOfByte1[0] != 0)
      try
      {
        standardError((short)arrayOfByte1[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (arrayOfByte1[1] == 0)
        {
          arrayOfChar = new char[i];
          String str2 = new String();
          if (i > 0)
            try
            {
              str2 = BytesToChars(this.charSet, arrayOfByte2);
            }
            catch (UnsupportedEncodingException localUnsupportedEncodingException3)
            {
              System.out.println(localUnsupportedEncodingException3);
            }
          else
            try
            {
              str2 = BytesToChars(this.charSet, arrayOfByte2);
            }
            catch (UnsupportedEncodingException localUnsupportedEncodingException4)
            {
              System.out.println(localUnsupportedEncodingException4);
            }
          if (this.tracer.isTracing())
            this.tracer.trace(str2.trim());
          if (paramBoolean)
            localJdbcOdbcSQLWarning.value = str2.trim();
          else
            localJdbcOdbcSQLWarning.value = str2;
        }
        else
        {
          if (this.tracer.isTracing())
            this.tracer.trace("NULL");
          localJdbcOdbcSQLWarning.value = null;
        }
        throw localJdbcOdbcSQLWarning;
      }
    if (arrayOfByte1[1] == 0)
    {
      String str1 = new String();
      arrayOfChar = new char[i];
      if (i > 0)
        try
        {
          str1 = BytesToChars(this.charSet, arrayOfByte2);
        }
        catch (UnsupportedEncodingException localUnsupportedEncodingException1)
        {
          System.out.println(localUnsupportedEncodingException1);
        }
      else
        try
        {
          str1 = BytesToChars(this.charSet, arrayOfByte2);
        }
        catch (UnsupportedEncodingException localUnsupportedEncodingException2)
        {
          System.out.println(localUnsupportedEncodingException2);
        }
      if (this.tracer.isTracing())
        this.tracer.trace(str1.trim());
      if (paramBoolean)
        return str1.trim();
      return str1;
    }
    if (this.tracer.isTracing())
      this.tracer.trace("NULL");
    return null;
  }

  public String SQLGetDataStringDate(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get date data (SQLGetData), hStmt=" + paramLong + ", column=" + paramInt);
    byte[] arrayOfByte1 = new byte[2];
    byte[] arrayOfByte2 = new byte[11];
    getDataStringDate(paramLong, paramInt, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      try
      {
        standardError((short)arrayOfByte1[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (arrayOfByte1[1] == 0)
        {
          String str2 = new String();
          try
          {
            str2 = BytesToChars(this.charSet, arrayOfByte2);
          }
          catch (UnsupportedEncodingException localUnsupportedEncodingException2)
          {
          }
          if (this.tracer.isTracing())
            this.tracer.trace(str2.trim());
          localJdbcOdbcSQLWarning.value = str2.trim();
        }
        else
        {
          if (this.tracer.isTracing())
            this.tracer.trace("NULL");
          localJdbcOdbcSQLWarning.value = null;
        }
        throw localJdbcOdbcSQLWarning;
      }
    if (arrayOfByte1[1] == 0)
    {
      String str1 = new String();
      try
      {
        str1 = BytesToChars(this.charSet, arrayOfByte2);
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException1)
      {
      }
      if (this.tracer.isTracing())
        this.tracer.trace(str1.trim());
      return str1.trim();
    }
    if (this.tracer.isTracing())
      this.tracer.trace("NULL");
    return null;
  }

  public String SQLGetDataStringTime(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get time data (SQLGetData), hStmt=" + paramLong + ", column=" + paramInt);
    byte[] arrayOfByte1 = new byte[2];
    byte[] arrayOfByte2 = new byte[9];
    getDataStringTime(paramLong, paramInt, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      try
      {
        standardError((short)arrayOfByte1[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (arrayOfByte1[1] == 0)
        {
          String str2 = new String();
          try
          {
            str2 = BytesToChars(this.charSet, arrayOfByte2);
          }
          catch (UnsupportedEncodingException localUnsupportedEncodingException2)
          {
          }
          if (this.tracer.isTracing())
            this.tracer.trace(str2.trim());
          localJdbcOdbcSQLWarning.value = str2.trim();
        }
        else
        {
          if (this.tracer.isTracing())
            this.tracer.trace("NULL");
          localJdbcOdbcSQLWarning.value = null;
        }
        throw localJdbcOdbcSQLWarning;
      }
    if (arrayOfByte1[1] == 0)
    {
      String str1 = new String();
      try
      {
        str1 = BytesToChars(this.charSet, arrayOfByte2);
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException1)
      {
      }
      if (this.tracer.isTracing())
        this.tracer.trace(str1.trim());
      return str1.trim();
    }
    if (this.tracer.isTracing())
      this.tracer.trace("NULL");
    return null;
  }

  public String SQLGetDataStringTimestamp(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get timestamp data (SQLGetData), hStmt=" + paramLong + ", column=" + paramInt);
    byte[] arrayOfByte1 = new byte[2];
    byte[] arrayOfByte2 = new byte[30];
    getDataStringTimestamp(paramLong, paramInt, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      try
      {
        standardError((short)arrayOfByte1[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (arrayOfByte1[1] == 0)
        {
          String str2 = new String();
          try
          {
            str2 = BytesToChars(this.charSet, arrayOfByte2);
          }
          catch (UnsupportedEncodingException localUnsupportedEncodingException2)
          {
          }
          if (this.tracer.isTracing())
            this.tracer.trace(str2.trim());
          localJdbcOdbcSQLWarning.value = str2.trim();
        }
        else
        {
          if (this.tracer.isTracing())
            this.tracer.trace("NULL");
          localJdbcOdbcSQLWarning.value = null;
        }
        throw localJdbcOdbcSQLWarning;
      }
    if (arrayOfByte1[1] == 0)
    {
      String str1 = new String();
      try
      {
        str1 = BytesToChars(this.charSet, arrayOfByte2);
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException1)
      {
      }
      if (this.tracer.isTracing())
        this.tracer.trace(str1.trim());
      return str1.trim();
    }
    if (this.tracer.isTracing())
      this.tracer.trace("NULL");
    return null;
  }

  public int SQLGetInfo(long paramLong, short paramShort)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get connection info (SQLGetInfo), hDbc=" + paramLong + ", fInfoType=" + paramShort);
    byte[] arrayOfByte = new byte[1];
    int i = getInfo(paramLong, paramShort, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, paramLong, 3412047669210644480L);
    if (this.tracer.isTracing())
      this.tracer.trace(" int value=" + i);
    return i;
  }

  public int SQLGetInfoShort(long paramLong, short paramShort)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get connection info (SQLGetInfo), hDbc=" + paramLong + ", fInfoType=" + paramShort);
    byte[] arrayOfByte = new byte[1];
    int i = getInfoShort(paramLong, paramShort, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, paramLong, 3412047669210644480L);
    if (this.tracer.isTracing())
      this.tracer.trace(" short value=" + i);
    return i;
  }

  public String SQLGetInfoString(long paramLong, short paramShort)
    throws SQLException
  {
    return SQLGetInfoString(paramLong, paramShort, 300);
  }

  public String SQLGetInfoString(long paramLong, short paramShort, int paramInt)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get connection info string (SQLGetInfo), hDbc=" + paramLong + ", fInfoType=" + paramShort + ", len=" + paramInt);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = new byte[paramInt];
    getInfoString(paramLong, paramShort, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, paramLong, 3412047669210644480L);
    String str = new String();
    try
    {
      str = BytesToChars(this.charSet, arrayOfByte2);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    if (this.tracer.isTracing())
      this.tracer.trace(str.trim());
    return str.trim();
  }

  public long SQLGetStmtOption(long paramLong, short paramShort)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    long l = 3412047153814568960L;
    if (this.tracer.isTracing())
      this.tracer.trace("Get statement option (SQLGetStmtOption), hStmt=" + paramLong + ", fOption=" + paramShort);
    byte[] arrayOfByte = new byte[1];
    l = getStmtOption(paramLong, paramShort, arrayOfByte);
    if (arrayOfByte[0] != 0)
      try
      {
        standardError((short)arrayOfByte[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (this.tracer.isTracing())
          this.tracer.trace("value=" + l);
        localJdbcOdbcSQLWarning.value = BigDecimal.valueOf(l);
        throw localJdbcOdbcSQLWarning;
      }
    if (this.tracer.isTracing())
      this.tracer.trace("value=" + l);
    return l;
  }

  public int SQLGetStmtAttr(long paramLong, int paramInt)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get Statement Attribute (SQLGetStmtAttr), hDbc=" + paramLong + ", AttrType=" + paramInt);
    byte[] arrayOfByte = new byte[1];
    int i = getStmtAttr(paramLong, paramInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      try
      {
        standardError((short)arrayOfByte[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (this.tracer.isTracing())
          this.tracer.trace("value=" + i);
        localJdbcOdbcSQLWarning.value = BigDecimal.valueOf(i);
        throw localJdbcOdbcSQLWarning;
      }
    if (this.tracer.isTracing())
      this.tracer.trace(" int value=" + i);
    return i;
  }

  public void SQLGetTypeInfo(long paramLong, short paramShort)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Get type info (SQLGetTypeInfo), hStmt=" + paramLong + ", fSqlType=" + paramShort);
    byte[] arrayOfByte = new byte[1];
    getTypeInfo(paramLong, paramShort, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public boolean SQLMoreResults(long paramLong)
    throws SQLException, SQLWarning
  {
    boolean bool = true;
    if (this.tracer.isTracing())
      this.tracer.trace("Get more results (SQLMoreResults), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    moreResults(paramLong, arrayOfByte);
    if (arrayOfByte[0] == 100)
    {
      bool = false;
      arrayOfByte[0] = 0;
    }
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    if (this.tracer.isTracing())
      this.tracer.trace("More results: " + bool);
    return bool;
  }

  public String SQLNativeSql(long paramLong, String paramString)
    throws SQLException
  {
    byte[] arrayOfByte1 = new byte[1];
    int i = 1024;
    if (paramString.length() * 4 > i)
    {
      i = paramString.length() * 4;
      if (i > 32768)
        i = 32768;
    }
    if (this.tracer.isTracing())
      this.tracer.trace("Convert native SQL (SQLNativeSql), hDbc=" + paramLong + ", nativeLen=" + i + ", SQL=" + paramString);
    byte[] arrayOfByte2 = new byte[i];
    byte[] arrayOfByte3 = null;
    char[] arrayOfChar = null;
    if (paramString != null)
      arrayOfChar = paramString.toCharArray();
    try
    {
      if (paramString != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException1)
    {
    }
    nativeSql(paramLong, arrayOfByte3, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, paramLong, 3412047669210644480L);
    String str = new String();
    try
    {
      str = BytesToChars(this.charSet, arrayOfByte2);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException2)
    {
    }
    if (this.tracer.isTracing())
      this.tracer.trace("Native SQL=" + str.trim());
    return str.trim();
  }

  public int SQLNumParams(long paramLong)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("Number of parameter markers (SQLNumParams), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    i = numParams(paramLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    if (this.tracer.isTracing())
      this.tracer.trace("value=" + i);
    return i;
  }

  public int SQLNumResultCols(long paramLong)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("Number of result columns (SQLNumResultCols), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    i = numResultCols(paramLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      try
      {
        standardError((short)arrayOfByte[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (this.tracer.isTracing())
          this.tracer.trace("value=" + i);
        localJdbcOdbcSQLWarning.value = BigDecimal.valueOf(i);
        throw localJdbcOdbcSQLWarning;
      }
    if (this.tracer.isTracing())
      this.tracer.trace("value=" + i);
    return i;
  }

  public int SQLParamData(long paramLong)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("Get parameter number (SQLParamData), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    i = paramData(paramLong, arrayOfByte);
    if (arrayOfByte[0] == 99)
      arrayOfByte[0] = 0;
    else
      i = -1;
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    if (this.tracer.isTracing())
      this.tracer.trace("Parameter needing data=" + i);
    return i;
  }

  public int SQLParamDataInBlock(long paramLong, int paramInt)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("Get parameter number (SQLParamData in block-cursor), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    i = paramDataInBlock(paramLong, paramInt, arrayOfByte);
    if (arrayOfByte[0] == 99)
      arrayOfByte[0] = 0;
    else
      i = -1;
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    if (this.tracer.isTracing())
      this.tracer.trace("Parameter needing data=" + i);
    return i;
  }

  public void SQLPrepare(long paramLong, String paramString)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Preparing (SQLPrepare), hStmt=" + paramLong + ", szSqlStr=" + paramString);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    char[] arrayOfChar = null;
    if (paramString != null)
      arrayOfChar = paramString.toCharArray();
    try
    {
      if (paramString != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    prepare(paramLong, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLPutData(long paramLong, byte[] paramArrayOfByte, int paramInt)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Putting data (SQLPutData), hStmt=" + paramLong + ", len=" + paramInt);
    byte[] arrayOfByte = new byte[1];
    putData(paramLong, paramArrayOfByte, paramInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLPrimaryKeys(long paramLong, String paramString1, String paramString2, String paramString3)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Primary keys (SQLPrimaryKeys), hStmt=" + paramLong + ", catalog=" + paramString1 + ", schema=" + paramString2 + ", table=" + paramString3);
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    byte[] arrayOfByte1 = new byte[1];
    primaryKeys(paramLong, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLProcedures(long paramLong, String paramString1, String paramString2, String paramString3)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Procedures (SQLProcedures), hStmt=" + paramLong + ", catalog=" + paramString1 + ", schema=" + paramString2 + ", procedure=" + paramString3);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    procedures(paramLong, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLProcedureColumns(long paramLong, String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Procedure columns (SQLProcedureColumns), hStmt=" + paramLong + ", catalog=" + paramString1 + ", schema=" + paramString2 + ", procedure=" + paramString3 + ", column=" + paramString4);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    byte[] arrayOfByte5 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    char[] arrayOfChar4 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    if (paramString4 != null)
      arrayOfChar4 = paramString4.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
      if (paramString4 != null)
        arrayOfByte5 = CharsToBytes(this.charSet, arrayOfChar4);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    procedureColumns(paramLong, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, arrayOfByte5, (paramString4 == null) ? 1 : false, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public int SQLRowCount(long paramLong)
    throws SQLException, sun.jdbc.odbc.JdbcOdbcSQLWarning
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("Number of affected rows (SQLRowCount), hStmt=" + paramLong);
    byte[] arrayOfByte = new byte[1];
    i = rowCount(paramLong, arrayOfByte);
    if (arrayOfByte[0] != 0)
      try
      {
        standardError((short)arrayOfByte[0], 3412048236146327552L, 3412048236146327552L, paramLong);
      }
      catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
      {
        if (this.tracer.isTracing())
          this.tracer.trace("value=" + i);
        localJdbcOdbcSQLWarning.value = BigDecimal.valueOf(i);
        throw localJdbcOdbcSQLWarning;
      }
    if (this.tracer.isTracing())
      this.tracer.trace("value=" + i);
    return i;
  }

  public void SQLSetConnectOption(long paramLong, short paramShort, int paramInt)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Setting connection option (SQLSetConnectOption), hDbc=" + paramLong + ", fOption=" + paramShort + ", vParam=" + paramInt);
    byte[] arrayOfByte = new byte[1];
    setConnectOption(paramLong, paramShort, paramInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, paramLong, 3412047669210644480L);
  }

  public void SQLSetConnectOption(long paramLong, short paramShort, String paramString)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Setting connection option string (SQLSetConnectOption), hDbc=" + paramLong + ", fOption=" + paramShort + ", vParam=" + paramString);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    char[] arrayOfChar = null;
    if (paramString != null)
      arrayOfChar = paramString.toCharArray();
    try
    {
      if (paramString != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    setConnectOptionString(paramLong, paramShort, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, paramLong, 3412047669210644480L);
  }

  public void SQLSetCursorName(long paramLong, String paramString)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Setting cursor name (SQLSetCursorName), hStmt=" + paramLong + ", szCursor=" + paramString);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    char[] arrayOfChar = null;
    if (paramString != null)
      arrayOfChar = paramString.toCharArray();
    try
    {
      if (paramString != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    setCursorName(paramLong, arrayOfByte2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLSetStmtOption(long paramLong, short paramShort, int paramInt)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Setting statement option (SQLSetStmtOption), hStmt=" + paramLong + ", fOption=" + paramShort + ", vParam=" + paramInt);
    byte[] arrayOfByte = new byte[1];
    setStmtOption(paramLong, paramShort, paramInt, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLSetStmtAttr(long paramLong, int paramInt1, int paramInt2, int paramInt3)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Setting statement option (SQLSetStmtAttr), hStmt=" + paramLong + ", fOption=" + paramInt1 + ", vParam=" + paramInt2);
    byte[] arrayOfByte = new byte[1];
    setStmtAttr(paramLong, paramInt1, paramInt2, paramInt3, arrayOfByte);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLSetStmtAttrPtr(long paramLong, int paramInt1, int[] paramArrayOfInt, int paramInt2, long[] paramArrayOfLong)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Setting statement option (SQLSetStmtAttr), hStmt=" + paramLong + ", fOption=" + paramInt1);
    byte[] arrayOfByte = new byte[1];
    setStmtAttrPtr(paramLong, paramInt1, paramArrayOfInt, paramInt2, arrayOfByte, paramArrayOfLong);
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public boolean SQLSetPos(long paramLong, int paramInt1, int paramInt2, int paramInt3)
    throws SQLException
  {
    int i = 0;
    if (this.tracer.isTracing())
      this.tracer.trace("Setting row position (SQLSetPos), hStmt=" + paramLong + ", operation = " + paramInt2);
    byte[] arrayOfByte = new byte[1];
    setPos(paramLong, paramInt1, paramInt2, paramInt3, arrayOfByte);
    if (arrayOfByte[0] == 99)
    {
      if (this.tracer.isTracing())
        this.tracer.trace("SQL_NEED_DATA returned");
      i = 1;
      arrayOfByte[0] = 0;
    }
    if (arrayOfByte[0] != 0)
      standardError((short)arrayOfByte[0], 3412047669210644480L, 3412047669210644480L, paramLong);
    return i;
  }

  public void SQLSpecialColumns(long paramLong, short paramShort, String paramString1, String paramString2, String paramString3, int paramInt, boolean paramBoolean)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Special columns (SQLSpecialColumns), hStmt=" + paramLong + ", fColType=" + paramShort + ",catalog=" + paramString1 + ", schema=" + paramString2 + ", table=" + paramString3 + ", fScope=" + paramInt + ", fNullable=" + paramBoolean);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    specialColumns(paramLong, paramShort, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, paramInt, paramBoolean, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLStatistics(long paramLong, String paramString1, String paramString2, String paramString3, boolean paramBoolean1, boolean paramBoolean2)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Statistics (SQLStatistics), hStmt=" + paramLong + ",catalog=" + paramString1 + ", schema=" + paramString2 + ", table=" + paramString3 + ", unique=" + paramBoolean1 + ", approximate=" + paramBoolean2);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    statistics(paramLong, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, paramBoolean1, paramBoolean2, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLTables(long paramLong, String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Tables (SQLTables), hStmt=" + paramLong + ",catalog=" + paramString1 + ", schema=" + paramString2 + ", table=" + paramString3 + ", types=" + paramString4);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    byte[] arrayOfByte5 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    char[] arrayOfChar4 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    if (paramString4 != null)
      arrayOfChar4 = paramString4.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
      if (paramString4 != null)
        arrayOfByte5 = CharsToBytes(this.charSet, arrayOfChar4);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    tables(paramLong, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, arrayOfByte5, (paramString4 == null) ? 1 : false, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLTablePrivileges(long paramLong, String paramString1, String paramString2, String paramString3)
    throws SQLException, SQLWarning
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Tables (SQLTables), hStmt=" + paramLong + ",catalog=" + paramString1 + ", schema=" + paramString2 + ", table=" + paramString3);
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = null;
    byte[] arrayOfByte3 = null;
    byte[] arrayOfByte4 = null;
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    char[] arrayOfChar3 = null;
    if (paramString1 != null)
      arrayOfChar1 = paramString1.toCharArray();
    if (paramString2 != null)
      arrayOfChar2 = paramString2.toCharArray();
    if (paramString3 != null)
      arrayOfChar3 = paramString3.toCharArray();
    try
    {
      if (paramString1 != null)
        arrayOfByte2 = CharsToBytes(this.charSet, arrayOfChar1);
      if (paramString2 != null)
        arrayOfByte3 = CharsToBytes(this.charSet, arrayOfChar2);
      if (paramString3 != null)
        arrayOfByte4 = CharsToBytes(this.charSet, arrayOfChar3);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    tablePrivileges(paramLong, arrayOfByte2, (paramString1 == null) ? 1 : false, arrayOfByte3, (paramString2 == null) ? 1 : false, arrayOfByte4, (paramString3 == null) ? 1 : false, arrayOfByte1);
    if (arrayOfByte1[0] != 0)
      standardError((short)arrayOfByte1[0], 3412047669210644480L, 3412047669210644480L, paramLong);
  }

  public void SQLTransact(long paramLong1, long paramLong2, short paramShort)
    throws SQLException
  {
    if (this.tracer.isTracing())
      this.tracer.trace("Transaction (SQLTransact), hEnv=" + paramLong1 + ", hDbc=" + paramLong2 + ", fType=" + paramShort);
    byte[] arrayOfByte = new byte[1];
    transact(paramLong1, paramLong2, paramShort, arrayOfByte);
    if (arrayOfByte[0] != 0)
      throwGenericSQLException();
  }

  public native int bufferToInt(byte[] paramArrayOfByte);

  public native float bufferToFloat(byte[] paramArrayOfByte);

  public native double bufferToDouble(byte[] paramArrayOfByte);

  public native long bufferToLong(byte[] paramArrayOfByte);

  public native void convertDateString(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  public native void getDateStruct(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3);

  public native void convertTimeString(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  public native void getTimeStruct(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3);

  public native void getTimestampStruct(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, long paramLong);

  public native void convertTimestampString(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  public static native int getSQLLENSize();

  public static native void intToBytes(int paramInt, byte[] paramArrayOfByte);

  public static native void longToBytes(long paramLong, byte[] paramArrayOfByte);

  public static native void intTo4Bytes(int paramInt, byte[] paramArrayOfByte);

  public static SQLWarning convertWarning(JdbcOdbcSQLWarning paramJdbcOdbcSQLWarning)
  {
    Object localObject = paramJdbcOdbcSQLWarning;
    if (paramJdbcOdbcSQLWarning.getSQLState().equals("01004"))
    {
      DataTruncation localDataTruncation = new DataTruncation(-1, false, true, 0, 0);
      localObject = localDataTruncation;
    }
    return ((SQLWarning)localObject);
  }

  protected native long allocConnect(long paramLong, byte[] paramArrayOfByte);

  protected native long allocEnv(byte[] paramArrayOfByte);

  protected native long allocStmt(long paramLong, byte[] paramArrayOfByte);

  protected native void cancel(long paramLong, byte[] paramArrayOfByte);

  protected native void bindColAtExec(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, byte[] paramArrayOfByte3);

  protected native void bindColBinary(long paramLong, int paramInt1, Object[] paramArrayOfObject, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, long[] paramArrayOfLong, byte[] paramArrayOfByte3);

  protected native void bindColDate(long paramLong, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, byte[] paramArrayOfByte3);

  protected native void bindColDefault(long paramLong, int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3);

  protected native void bindColDouble(long paramLong, int paramInt, double[] paramArrayOfDouble, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, byte[] paramArrayOfByte3);

  protected native void bindColFloat(long paramLong, int paramInt, float[] paramArrayOfFloat, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, byte[] paramArrayOfByte3);

  protected native void bindColInteger(long paramLong, int paramInt, int[] paramArrayOfInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, byte[] paramArrayOfByte3);

  protected native void bindColString(long paramLong, int paramInt1, int paramInt2, Object[] paramArrayOfObject, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, byte[] paramArrayOfByte3);

  protected native void bindColTime(long paramLong, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, byte[] paramArrayOfByte3);

  protected native void bindColTimestamp(long paramLong, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, int[] paramArrayOfInt4, int[] paramArrayOfInt5, int[] paramArrayOfInt6, int[] paramArrayOfInt7, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong, byte[] paramArrayOfByte3);

  protected native void bindInParameterAtExec(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInOutParameterAtExec(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, int paramInt5, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInParameterBinary(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, int paramInt3, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4, long[] paramArrayOfLong);

  protected native void bindInParameterDate(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindInParameterDouble(long paramLong, int paramInt1, int paramInt2, int paramInt3, double paramDouble, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindInParameterFloat(long paramLong, int paramInt1, int paramInt2, int paramInt3, double paramDouble, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindInParameterBigint(long paramLong1, int paramInt1, int paramInt2, int paramInt3, long paramLong2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindInParameterInteger(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindInParameterNull(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindInParameterString(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, int paramInt3, int paramInt4, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInParameterTime(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindInParameterTimestamp(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindOutParameterString(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInOutParameterDate(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInOutParameterTime(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInOutParameterString(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInOutParameterStr(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong, int paramInt4);

  protected native void bindInOutParameterBin(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong, int paramInt4);

  protected native void bindInOutParameterBinary(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInOutParameterFixed(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInOutParameterTimeStamp(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInOutParameter(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, double paramDouble, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindInOutParameterNull(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindInOutParameterTimestamp(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindInParameterStringArray(long paramLong, int paramInt1, int paramInt2, Object[] paramArrayOfObject, byte[] paramArrayOfByte1, int paramInt3, int paramInt4, int[] paramArrayOfInt, byte[] paramArrayOfByte2);

  protected native void bindInParameterIntegerArray(long paramLong, int paramInt1, int paramInt2, int[] paramArrayOfInt1, int[] paramArrayOfInt2, byte[] paramArrayOfByte);

  protected native void bindInParameterFloatArray(long paramLong, int paramInt1, int paramInt2, int paramInt3, float[] paramArrayOfFloat, int[] paramArrayOfInt, byte[] paramArrayOfByte);

  protected native void bindInParameterDoubleArray(long paramLong, int paramInt1, int paramInt2, int paramInt3, double[] paramArrayOfDouble, int[] paramArrayOfInt, byte[] paramArrayOfByte);

  protected native void bindInParameterDateArray(long paramLong, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int[] paramArrayOfInt4);

  protected native void bindInParameterTimeArray(long paramLong, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int[] paramArrayOfInt4);

  protected native void bindInParameterTimestampArray(long paramLong, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, int[] paramArrayOfInt4, int[] paramArrayOfInt5, int[] paramArrayOfInt6, int[] paramArrayOfInt7, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int[] paramArrayOfInt8);

  protected native void bindInParameterBinaryArray(long paramLong, int paramInt1, int paramInt2, Object[] paramArrayOfObject, int paramInt3, byte[] paramArrayOfByte1, int[] paramArrayOfInt, byte[] paramArrayOfByte2);

  protected native void bindInParameterAtExecArray(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte1, int[] paramArrayOfInt, byte[] paramArrayOfByte2);

  protected native void bindOutParameterNull(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong);

  protected native void bindOutParameterFixed(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindOutParameterBinary(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindOutParameterDate(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindOutParameterTime(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void bindOutParameterTimestamp(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, long[] paramArrayOfLong);

  protected native void browseConnect(long paramLong, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3);

  protected native int colAttributes(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte);

  protected native void colAttributesString(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void columns(long paramLong, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, byte[] paramArrayOfByte4, boolean paramBoolean4, byte[] paramArrayOfByte5);

  protected native void columnPrivileges(long paramLong, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, byte[] paramArrayOfByte4, boolean paramBoolean4, byte[] paramArrayOfByte5);

  protected native int describeParam(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte);

  protected native void disconnect(long paramLong, byte[] paramArrayOfByte);

  protected native void driverConnect(long paramLong, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native int error(long paramLong1, long paramLong2, long paramLong3, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3);

  protected native void execDirect(long paramLong, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void execute(long paramLong, byte[] paramArrayOfByte);

  protected native void fetch(long paramLong, byte[] paramArrayOfByte);

  protected native void fetchScroll(long paramLong, short paramShort, int paramInt, byte[] paramArrayOfByte);

  protected native void foreignKeys(long paramLong, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, byte[] paramArrayOfByte4, boolean paramBoolean4, byte[] paramArrayOfByte5, boolean paramBoolean5, byte[] paramArrayOfByte6, boolean paramBoolean6, byte[] paramArrayOfByte7);

  protected native void freeConnect(long paramLong, byte[] paramArrayOfByte);

  protected native void freeEnv(long paramLong, byte[] paramArrayOfByte);

  protected native void freeStmt(long paramLong, int paramInt, byte[] paramArrayOfByte);

  protected native long getConnectOption(long paramLong, short paramShort, byte[] paramArrayOfByte);

  protected native void getConnectOptionString(long paramLong, short paramShort, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void getCursorName(long paramLong, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native long getStmtOption(long paramLong, short paramShort, byte[] paramArrayOfByte);

  protected native int getStmtAttr(long paramLong, int paramInt, byte[] paramArrayOfByte);

  protected native int getDataBinary(long paramLong, int paramInt1, int paramInt2, byte[] paramArrayOfByte1, int paramInt3, byte[] paramArrayOfByte2);

  protected native double getDataDouble(long paramLong, int paramInt, byte[] paramArrayOfByte);

  protected native double getDataFloat(long paramLong, int paramInt, byte[] paramArrayOfByte);

  protected native int getDataInteger(long paramLong, int paramInt, byte[] paramArrayOfByte);

  protected native int getDataString(long paramLong, int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void getDataStringDate(long paramLong, int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void getDataStringTime(long paramLong, int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void getDataStringTimestamp(long paramLong, int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native int getInfo(long paramLong, short paramShort, byte[] paramArrayOfByte);

  protected native int getInfoShort(long paramLong, short paramShort, byte[] paramArrayOfByte);

  protected native void getInfoString(long paramLong, short paramShort, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void getTypeInfo(long paramLong, short paramShort, byte[] paramArrayOfByte);

  protected native void moreResults(long paramLong, byte[] paramArrayOfByte);

  protected native void nativeSql(long paramLong, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3);

  protected native int numParams(long paramLong, byte[] paramArrayOfByte);

  protected native int numResultCols(long paramLong, byte[] paramArrayOfByte);

  protected native int paramData(long paramLong, byte[] paramArrayOfByte);

  protected native int paramDataInBlock(long paramLong, int paramInt, byte[] paramArrayOfByte);

  protected native void prepare(long paramLong, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void primaryKeys(long paramLong, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, byte[] paramArrayOfByte4);

  protected native void procedures(long paramLong, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, byte[] paramArrayOfByte4);

  protected native void procedureColumns(long paramLong, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, byte[] paramArrayOfByte4, boolean paramBoolean4, byte[] paramArrayOfByte5);

  protected native void putData(long paramLong, byte[] paramArrayOfByte1, int paramInt, byte[] paramArrayOfByte2);

  protected native int rowCount(long paramLong, byte[] paramArrayOfByte);

  protected native void setConnectOption(long paramLong, short paramShort, int paramInt, byte[] paramArrayOfByte);

  protected native void setConnectOptionString(long paramLong, short paramShort, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void setCursorName(long paramLong, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

  protected native void setStmtOption(long paramLong, short paramShort, int paramInt, byte[] paramArrayOfByte);

  protected native void setStmtAttr(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte);

  protected native void setStmtAttrPtr(long paramLong, int paramInt1, int[] paramArrayOfInt, int paramInt2, byte[] paramArrayOfByte, long[] paramArrayOfLong);

  protected native void setPos(long paramLong, int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte);

  protected native void specialColumns(long paramLong, short paramShort, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, int paramInt, boolean paramBoolean4, byte[] paramArrayOfByte4);

  protected native void statistics(long paramLong, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, boolean paramBoolean4, boolean paramBoolean5, byte[] paramArrayOfByte4);

  protected native void tables(long paramLong, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, byte[] paramArrayOfByte4, boolean paramBoolean4, byte[] paramArrayOfByte5);

  protected native void tablePrivileges(long paramLong, byte[] paramArrayOfByte1, boolean paramBoolean1, byte[] paramArrayOfByte2, boolean paramBoolean2, byte[] paramArrayOfByte3, boolean paramBoolean3, byte[] paramArrayOfByte4);

  protected native void transact(long paramLong1, long paramLong2, short paramShort, byte[] paramArrayOfByte);

  protected static native void ReleaseStoredBytes(long paramLong1, long paramLong2);

  protected static native void ReleaseStoredChars(long paramLong1, long paramLong2);

  protected static native void ReleaseStoredIntegers(long paramLong1, long paramLong2);

  SQLException createSQLException(long paramLong1, long paramLong2, long paramLong3)
  {
    Object localObject3;
    String str1;
    int j = 0;
    Object localObject1 = null;
    Object localObject2 = null;
    if (this.tracer.isTracing())
      this.tracer.trace("ERROR - Generating SQLException...");
    while (true)
    {
      byte[] arrayOfByte1;
      byte[] arrayOfByte2;
      int i;
      while (true)
      {
        if (j != 0)
          break label178;
        byte[] arrayOfByte3 = new byte[1];
        arrayOfByte1 = new byte[6];
        arrayOfByte2 = new byte[300];
        i = error(paramLong1, paramLong2, paramLong3, arrayOfByte1, arrayOfByte2, arrayOfByte3);
        if (arrayOfByte3[0] == 0)
          break;
        j = 1;
      }
      localObject3 = null;
      str1 = new String();
      String str2 = new String();
      try
      {
        str1 = BytesToChars(this.charSet, arrayOfByte2);
        str2 = BytesToChars(this.charSet, arrayOfByte1);
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException)
      {
      }
      localObject3 = new SQLException(str1.trim(), str2.trim(), i);
      if (localObject1 == null)
        localObject1 = localObject3;
      else
        localObject2.setNextException((SQLException)localObject3);
      localObject2 = localObject3;
    }
    if (localObject1 == null)
    {
      label178: localObject3 = "General error";
      str1 = "S1000";
      if (this.tracer.isTracing())
        this.tracer.trace("ERROR - " + str1 + " " + ((String)localObject3));
      localObject1 = new SQLException((String)localObject3, str1);
    }
    return ((SQLException)(SQLException)localObject1);
  }

  SQLWarning createSQLWarning(long paramLong1, long paramLong2, long paramLong3)
  {
    Object localObject3;
    String str1;
    int j = 0;
    Object localObject1 = null;
    Object localObject2 = null;
    if (this.tracer.isTracing())
      this.tracer.trace("WARNING - Generating SQLWarning...");
    while (true)
    {
      byte[] arrayOfByte1;
      byte[] arrayOfByte2;
      int i;
      while (true)
      {
        if (j != 0)
          break label178;
        byte[] arrayOfByte3 = new byte[1];
        arrayOfByte1 = new byte[6];
        arrayOfByte2 = new byte[300];
        i = error(paramLong1, paramLong2, paramLong3, arrayOfByte1, arrayOfByte2, arrayOfByte3);
        if (arrayOfByte3[0] == 0)
          break;
        j = 1;
      }
      localObject3 = null;
      str1 = new String();
      String str2 = new String();
      try
      {
        str1 = BytesToChars(this.charSet, arrayOfByte2);
        str2 = BytesToChars(this.charSet, arrayOfByte1);
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException)
      {
      }
      localObject3 = new sun.jdbc.odbc.JdbcOdbcSQLWarning(str1.trim(), str2.trim(), i);
      if (localObject1 == null)
        localObject1 = localObject3;
      else
        localObject2.setNextWarning((SQLWarning)localObject3);
      localObject2 = localObject3;
    }
    if (localObject1 == null)
    {
      label178: localObject3 = "General warning";
      str1 = "S1000";
      if (this.tracer.isTracing())
        this.tracer.trace("WARNING - " + str1 + " " + ((String)localObject3));
      localObject1 = new sun.jdbc.odbc.JdbcOdbcSQLWarning((String)localObject3, str1);
    }
    return ((SQLWarning)(SQLWarning)localObject1);
  }

  void throwGenericSQLException()
    throws SQLException
  {
    String str1 = "General error";
    String str2 = "S1000";
    if (this.tracer.isTracing())
      this.tracer.trace("ERROR - " + str2 + " " + str1);
    throw new SQLException(str1, str2);
  }

  void standardError(short paramShort, long paramLong1, long paramLong2, long paramLong3)
    throws SQLException, SQLWarning
  {
    String str;
    if (this.tracer.isTracing())
      this.tracer.trace("RETCODE = " + paramShort);
    switch (paramShort)
    {
    case -1:
      throw createSQLException(paramLong1, paramLong2, paramLong3);
    case 1:
      throw createSQLWarning(paramLong1, paramLong2, paramLong3);
    case -2:
      str = "Invalid handle";
      if (this.tracer.isTracing())
        this.tracer.trace("ERROR - " + str);
      throw new SQLException(str);
    case 100:
      str = "No data found";
      if (this.tracer.isTracing())
        this.tracer.trace("ERROR - " + str);
      throw new SQLException(str);
    }
    throwGenericSQLException();
  }

  public JdbcOdbcTracer getTracer()
  {
    return this.tracer;
  }
}
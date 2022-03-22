package sun.jdbc.odbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public class JdbcOdbcCallableStatement extends JdbcOdbcPreparedStatement
  implements CallableStatement
{
  public byte[] scalez = new byte[200];
  private boolean lastParameterNull = false;

  public JdbcOdbcCallableStatement(JdbcOdbcConnectionInterface paramJdbcOdbcConnectionInterface)
  {
    super(paramJdbcOdbcConnectionInterface);
  }

  public void registerOutParameter(int paramInt1, int paramInt2)
    throws SQLException
  {
    registerOutParameter(paramInt1, paramInt2, 0);
  }

  public void registerOutParameter(int paramInt1, int paramInt2, int paramInt3)
    throws SQLException
  {
    int i;
    setSqlType(paramInt1, paramInt2);
    if (paramInt1 <= 200)
      this.scalez[paramInt1] = (byte)paramInt3;
    setOutputParameter(paramInt1, true);
    switch (paramInt2)
    {
    case 91:
      i = 10;
      break;
    case 92:
      i = 8;
      break;
    case 93:
      i = 15;
      if (paramInt3 > 0)
        i += paramInt3 + 1;
      break;
    case -7:
      i = 3;
      break;
    case -6:
      i = 4;
      break;
    case 5:
      i = 6;
      break;
    case 4:
      i = 11;
      break;
    case -5:
      i = 20;
      break;
    case 7:
      i = 13;
      break;
    case 6:
    case 8:
      i = 22;
      break;
    case 2:
    case 3:
      i = 38;
      break;
    default:
      i = getPrecision(paramInt2);
      if ((i <= 0) || (i > 8000))
        i = 8000;
    }
    byte[] arrayOfByte1 = getLengthBuf(paramInt1);
    paramInt2 = OdbcDef.jdbcTypeToOdbc(paramInt2);
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = 3412046964836007936L;
    arrayOfLong[1] = 3412046964836007936L;
    arrayOfLong[2] = 3412046964836007936L;
    arrayOfLong[3] = 3412046964836007936L;
    byte[] arrayOfByte2 = null;
    if ((this.boundParams[(paramInt1 - 1)].isInOutParameter()) && (this.boundParams[(paramInt1 - 1)].boundValue == null))
      arrayOfByte2 = null;
    else
      arrayOfByte2 = allocBindBuf(paramInt1, i + 1);
    if (this.boundParams[(paramInt1 - 1)].isInOutParameter())
    {
      Object localObject1;
      Object localObject2;
      int k;
      Calendar localCalendar;
      Object localObject3;
      int i3;
      Object localObject4;
      int i5;
      int i6;
      byte[] arrayOfByte8;
      if (this.boundParams[(paramInt1 - 1)].boundValue == null)
      {
        this.OdbcApi.SQLBindInOutParameterNull(this.hStmt, paramInt1, paramInt2, i, this.boundParams[(paramInt1 - 1)].scale, arrayOfByte1, arrayOfLong);
        break label3107:
      }
      if (paramInt2 == 4)
      {
        if (this.boundParams[(paramInt1 - 1)].boundType == 4)
        {
          this.OdbcApi.SQLBindInOutParameterFixed(this.hStmt, paramInt1, paramInt2, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if (paramInt2 == -7)
      {
        if (this.boundParams[(paramInt1 - 1)].boundType == -7)
        {
          this.OdbcApi.SQLBindInOutParameterFixed(this.hStmt, paramInt1, paramInt2, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if (paramInt2 == -6)
      {
        if (this.boundParams[(paramInt1 - 1)].boundType == -6)
        {
          this.OdbcApi.SQLBindInOutParameterFixed(this.hStmt, paramInt1, paramInt2, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if (paramInt2 == 5)
      {
        if (this.boundParams[(paramInt1 - 1)].boundType == 5)
        {
          this.OdbcApi.SQLBindInOutParameterFixed(this.hStmt, paramInt1, paramInt2, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if ((paramInt2 == 8) || (paramInt2 == 6) || (paramInt2 == 7))
      {
        if ((this.boundParams[(paramInt1 - 1)].boundType == 8) || (this.boundParams[(paramInt1 - 1)].boundType == 6) || (this.boundParams[(paramInt1 - 1)].boundType == 7))
        {
          this.OdbcApi.SQLBindInOutParameterFixed(this.hStmt, paramInt1, 8, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if (paramInt2 == -5)
      {
        if (this.boundParams[(paramInt1 - 1)].boundType == -5)
        {
          this.OdbcApi.SQLBindInOutParameterFixed(this.hStmt, paramInt1, paramInt2, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if ((paramInt2 == 1) || (paramInt2 == 12))
      {
        if (this.boundParams[(paramInt1 - 1)].boundType == 1)
        {
          localObject1 = new byte[i + 1];
          for (k = 0; k < localObject1.length; ++k)
            localObject1[k] = 0;
          for (k = 0; k < arrayOfByte2.length; ++k)
            localObject1[k] = arrayOfByte2[k];
          this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(localObject1);
          this.OdbcApi.SQLBindInOutParameterStr(this.hStmt, paramInt1, paramInt2, i, localObject1, arrayOfByte1, arrayOfLong, -3);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if ((paramInt2 == -2) || (paramInt2 == -3))
      {
        if ((this.boundParams[(paramInt1 - 1)].boundType == -2) || (this.boundParams[(paramInt1 - 1)].boundType == -3))
        {
          localObject1 = new byte[i + 1];
          for (k = 0; k < localObject1.length; ++k)
            localObject1[k] = 0;
          for (k = 0; k < arrayOfByte2.length; ++k)
            localObject1[k] = arrayOfByte2[k];
          this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(localObject1);
          this.OdbcApi.SQLBindInOutParameterBin(this.hStmt, paramInt1, paramInt2, i, localObject1, arrayOfByte1, arrayOfLong, arrayOfByte2.length);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if ((paramInt2 == 2) || (paramInt2 == 3))
      {
        localObject1 = null;
        try
        {
          localObject1 = BytesToChars(this.OdbcApi.charSet, arrayOfByte2);
        }
        catch (UnsupportedEncodingException localUnsupportedEncodingException1)
        {
        }
        BigDecimal localBigDecimal1 = new BigDecimal((String)localObject1);
        localObject3 = null;
        localObject4 = null;
        if (paramInt3 >= localBigDecimal1.scale())
        {
          BigDecimal localBigDecimal2 = localBigDecimal1.movePointRight(paramInt3).movePointLeft(paramInt3);
          localObject3 = new byte[i];
          for (int i7 = 0; i7 < localObject3.length; ++i7)
            localObject3[i7] = 48;
          try
          {
            localObject4 = CharsToBytes(this.OdbcApi.charSet, localBigDecimal2.toString().toCharArray());
            for (i7 = localObject3.length - localObject4.length; i7 < localObject3.length; ++i7)
              localObject3[i7] = localObject4[(i7 - localObject3.length - localObject4.length)];
          }
          catch (UnsupportedEncodingException localUnsupportedEncodingException3)
          {
          }
          this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(localObject3);
          this.boundParams[(paramInt1 - 1)].scale = localBigDecimal2.scale();
        }
        this.OdbcApi.SQLBindInOutParameterString(this.hStmt, paramInt1, paramInt2, i, this.boundParams[(paramInt1 - 1)].scale, localObject3, arrayOfByte1, arrayOfLong);
        break label3107:
      }
      if (paramInt2 == 11)
      {
        if (this.boundParams[(paramInt1 - 1)].boundType == 93)
        {
          int j = 0;
          int l = 0;
          localObject3 = (Timestamp)this.boundParams[(paramInt1 - 1)].boundValue;
          localObject4 = Calendar.getInstance();
          ((Calendar)localObject4).setTime((java.util.Date)localObject3);
          i6 = ((Calendar)localObject4).get(1);
          int i8 = ((Calendar)localObject4).get(2);
          int i9 = ((Calendar)localObject4).get(5);
          int i10 = ((Calendar)localObject4).get(11);
          int i11 = ((Calendar)localObject4).get(12);
          int i12 = ((Calendar)localObject4).get(13);
          int i13 = ((Timestamp)localObject3).getNanos();
          i8 += 1;
          byte[] arrayOfByte9 = new byte[16];
          this.OdbcApi.getTimestampStruct(arrayOfByte9, i6, i8, i9, i10, i11, i12, i13);
          this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(arrayOfByte9);
          Integer localInteger = new Integer(i13);
          String str = localInteger.toString();
          char[] arrayOfChar = str.toCharArray();
          for (j = arrayOfChar.length; j > 0; --j)
            if (arrayOfChar[(j - 1)] != '0')
              break;
          if (i13 == 0)
            j = 1;
          l = 20 + j;
          this.OdbcApi.SQLBindInOutParameterTimestamp(this.hStmt, paramInt1, 29, 9, arrayOfByte9, arrayOfByte1, arrayOfLong);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if (paramInt2 == 9)
      {
        if (this.boundParams[(paramInt1 - 1)].boundType == 91)
        {
          localObject2 = (java.sql.Date)this.boundParams[(paramInt1 - 1)].boundValue;
          localCalendar = Calendar.getInstance();
          localCalendar.setTime((java.util.Date)localObject2);
          i3 = localCalendar.get(1);
          i5 = localCalendar.get(2);
          i6 = localCalendar.get(5);
          i5 += 1;
          arrayOfByte8 = new byte[6];
          this.OdbcApi.getDateStruct(arrayOfByte8, i3, i5, i6);
          this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(arrayOfByte8);
          this.OdbcApi.SQLBindInOutParameterDate(this.hStmt, paramInt1, this.boundParams[(paramInt1 - 1)].scale, arrayOfByte8, arrayOfByte1, arrayOfLong);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if (paramInt2 == 10)
      {
        if (this.boundParams[(paramInt1 - 1)].boundType == 92)
        {
          localObject2 = (Time)this.boundParams[(paramInt1 - 1)].boundValue;
          localCalendar = Calendar.getInstance();
          localCalendar.setTime((java.util.Date)localObject2);
          i3 = localCalendar.get(11);
          i5 = localCalendar.get(12);
          i6 = localCalendar.get(13);
          arrayOfByte8 = new byte[6];
          this.OdbcApi.getTimeStruct(arrayOfByte8, i3, i5, i6);
          this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(arrayOfByte8);
          this.OdbcApi.SQLBindInOutParameterTime(this.hStmt, paramInt1, this.boundParams[(paramInt1 - 1)].scale, arrayOfByte8, arrayOfByte1, arrayOfLong);
          break label3107:
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if (paramInt2 == -1)
      {
        if ((this.boundParams[(paramInt1 - 1)].boundType == -1) || (this.boundParams[(paramInt1 - 1)].boundType == 12) || (this.boundParams[(paramInt1 - 1)].boundType == 1))
        {
          if (this.boundParams[(paramInt1 - 1)].boundValue instanceof InputStream)
          {
            localObject2 = new byte[8000];
            for (int i1 = 0; i1 < localObject2.length; ++i1)
              localObject2[i1] = 0;
            for (i1 = 0; i1 < arrayOfByte2.length; ++i1)
              localObject2[i1] = arrayOfByte2[i1];
            this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(localObject2);
            byte[] arrayOfByte3 = getLengthBuf(paramInt1);
            this.OdbcApi.SQLBindInOutParameterAtExec(this.hStmt, paramInt1, 1, -1, localObject2.length, localObject2, this.boundParams[(paramInt1 - 1)].getInputStreamLen(), arrayOfByte3, arrayOfLong);
            break label3107:
          }
          if (this.boundParams[(paramInt1 - 1)].boundValue instanceof String)
          {
            localObject2 = null;
            try
            {
              localObject2 = ((String)(String)this.boundParams[(paramInt1 - 1)].boundValue).getBytes(this.OdbcApi.charSet);
            }
            catch (UnsupportedEncodingException localUnsupportedEncodingException2)
            {
              throw new SQLException(localUnsupportedEncodingException2.getMessage());
            }
            ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(localObject2);
            this.boundParams[(paramInt1 - 1)].setInputStream(localByteArrayInputStream, localObject2.length);
            byte[] arrayOfByte5 = new byte[8000];
            for (i5 = 0; i5 < arrayOfByte5.length; ++i5)
              arrayOfByte5[i5] = 0;
            JdbcOdbc.intTo4Bytes(paramInt1, arrayOfByte5);
            this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(arrayOfByte5);
            byte[] arrayOfByte7 = getLengthBuf(paramInt1);
            this.OdbcApi.SQLBindInOutParameterAtExec(this.hStmt, paramInt1, 1, -1, arrayOfByte5.length, arrayOfByte5, this.boundParams[(paramInt1 - 1)].getInputStreamLen(), arrayOfByte7, arrayOfLong);
            break label3107:
          }
          throw new UnsupportedOperationException();
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      if (paramInt2 == -4)
      {
        if ((this.boundParams[(paramInt1 - 1)].boundType == -4) || (this.boundParams[(paramInt1 - 1)].boundType == -3) || (this.boundParams[(paramInt1 - 1)].boundType == -2))
        {
          byte[] arrayOfByte4;
          if (this.boundParams[(paramInt1 - 1)].boundValue instanceof InputStream)
          {
            localObject2 = new byte[8000];
            for (int i2 = 0; i2 < localObject2.length; ++i2)
              localObject2[i2] = 48;
            for (i2 = 0; i2 < arrayOfByte2.length; ++i2)
              localObject2[i2] = arrayOfByte2[i2];
            this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(localObject2);
            arrayOfByte4 = getLengthBuf(paramInt1);
            this.OdbcApi.SQLBindInOutParameterAtExec(this.hStmt, paramInt1, -2, -4, localObject2.length, localObject2, this.boundParams[(paramInt1 - 1)].getInputStreamLen(), arrayOfByte4, arrayOfLong);
            break label3107:
          }
          if (this.boundParams[(paramInt1 - 1)].boundValue instanceof byte[])
          {
            localObject2 = (byte[])(byte[])this.boundParams[(paramInt1 - 1)].boundValue;
            this.boundParams[(paramInt1 - 1)].setInputStream(new ByteArrayInputStream(localObject2), localObject2.length);
            arrayOfByte4 = new byte[8000];
            for (int i4 = 0; i4 < arrayOfByte4.length; ++i4)
              arrayOfByte4[i4] = 48;
            JdbcOdbc.intTo4Bytes(paramInt1, arrayOfByte4);
            this.boundParams[(paramInt1 - 1)].resetBindDataBuffer(arrayOfByte4);
            byte[] arrayOfByte6 = getLengthBuf(paramInt1);
            this.OdbcApi.SQLBindInOutParameterAtExec(this.hStmt, paramInt1, -2, -4, arrayOfByte4.length, arrayOfByte4, this.boundParams[(paramInt1 - 1)].getInputStreamLen(), arrayOfByte6, arrayOfLong);
            break label3107:
          }
          throw new UnsupportedOperationException();
        }
        throw new SQLException("Type mismatch between the set function and registerOutParameter");
      }
      throw new UnsupportedOperationException();
    }
    if (paramInt2 == 0)
      this.OdbcApi.SQLBindOutParameterNull(this.hStmt, paramInt1, paramInt2, i, this.boundParams[(paramInt1 - 1)].scale, arrayOfByte1, arrayOfLong);
    else if ((paramInt2 == 8) || (paramInt2 == 6) || (paramInt2 == 7))
      this.OdbcApi.SQLBindOutParameterFixed(this.hStmt, paramInt1, 8, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if (paramInt2 == 4)
      this.OdbcApi.SQLBindOutParameterFixed(this.hStmt, paramInt1, paramInt2, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if (paramInt2 == -6)
      this.OdbcApi.SQLBindOutParameterFixed(this.hStmt, paramInt1, paramInt2, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if (paramInt2 == 5)
      this.OdbcApi.SQLBindOutParameterFixed(this.hStmt, paramInt1, paramInt2, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if (paramInt2 == -7)
      this.OdbcApi.SQLBindOutParameterFixed(this.hStmt, paramInt1, paramInt2, 1, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if (paramInt2 == -5)
      this.OdbcApi.SQLBindOutParameterFixed(this.hStmt, paramInt1, paramInt2, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if ((paramInt2 == 2) || (paramInt2 == 3))
      this.OdbcApi.SQLBindOutParameterString(this.hStmt, paramInt1, paramInt2, paramInt3, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if (paramInt2 == 11)
      this.OdbcApi.SQLBindOutParameterTimestamp(this.hStmt, paramInt1, i, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if (paramInt2 == 9)
      this.OdbcApi.SQLBindOutParameterDate(this.hStmt, paramInt1, this.boundParams[(paramInt1 - 1)].scale, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if (paramInt2 == 10)
      this.OdbcApi.SQLBindOutParameterTime(this.hStmt, paramInt1, this.boundParams[(paramInt1 - 1)].scale, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if ((paramInt2 == -2) || (paramInt2 == -3) || (paramInt2 == -4))
      this.OdbcApi.SQLBindOutParameterBinary(this.hStmt, paramInt1, paramInt2, i, paramInt3, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else if ((paramInt2 == 1) || (paramInt2 == 12) || (paramInt2 == -1))
      this.OdbcApi.SQLBindOutParameterString(this.hStmt, paramInt1, paramInt2, paramInt3, arrayOfByte2, arrayOfByte1, arrayOfLong);
    else
      this.OdbcApi.SQLBindOutParameterString(this.hStmt, paramInt1, paramInt2, paramInt3, arrayOfByte2, arrayOfByte1, arrayOfLong);
    label3107: this.boundParams[(paramInt1 - 1)].pA1 = arrayOfLong[0];
    this.boundParams[(paramInt1 - 1)].pA2 = arrayOfLong[1];
    this.boundParams[(paramInt1 - 1)].pB1 = arrayOfLong[2];
    this.boundParams[(paramInt1 - 1)].pB2 = arrayOfLong[3];
  }

  public boolean wasNull()
    throws SQLException
  {
    return this.lastParameterNull;
  }

  public String getString(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return null;
    int i = getSqlType(paramInt);
    String str = null;
    try
    {
      byte[] arrayOfByte = getDataBuf(paramInt);
      if (arrayOfByte != null)
        str = BytesToChars(this.OdbcApi.charSet, arrayOfByte);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("String value for OUT parameter " + paramInt + "=" + str);
    return str;
  }

  public boolean getBoolean(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return false;
    return (getInt(paramInt) == 1);
  }

  public byte getByte(int paramInt)
    throws SQLException
  {
    return (byte)getInt(paramInt);
  }

  public short getShort(int paramInt)
    throws SQLException
  {
    return (short)getInt(paramInt);
  }

  public int getInt(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return 0;
    return this.OdbcApi.bufferToInt(getDataBuf(paramInt));
  }

  public long getLong(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return 3412047463052214272L;
    return this.OdbcApi.bufferToLong(getDataBuf(paramInt));
  }

  public float getFloat(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return 0F;
    return (float)this.OdbcApi.bufferToDouble(getDataBuf(paramInt));
  }

  public double getDouble(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return 0D;
    return this.OdbcApi.bufferToDouble(getDataBuf(paramInt));
  }

  public BigDecimal getBigDecimal(int paramInt1, int paramInt2)
    throws SQLException
  {
    if (isNull(paramInt1))
      return null;
    BigDecimal localBigDecimal = new BigDecimal(getString(paramInt1).trim());
    return localBigDecimal.setScale(paramInt2, 6);
  }

  public byte[] getBytes(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return null;
    if ((this.boundParams[(paramInt - 1)].isInOutParameter()) || (this.boundParams[(paramInt - 1)].isOutputParameter()))
    {
      int i = getParamLength(paramInt);
      byte[] arrayOfByte1 = getDataBuf(paramInt);
      if (i < arrayOfByte1.length)
      {
        byte[] arrayOfByte2 = new byte[i];
        for (int j = 0; j < getParamLength(paramInt); ++j)
          arrayOfByte2[j] = arrayOfByte1[j];
        this.boundParams[(paramInt - 1)].resetBindDataBuffer(arrayOfByte2);
        return arrayOfByte2;
      }
      return arrayOfByte1;
    }
    return hexStringToByteArray(getString(paramInt).trim());
  }

  public java.sql.Date getDate(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return null;
    byte[] arrayOfByte1 = getDataBuf(paramInt);
    byte[] arrayOfByte2 = new byte[11];
    this.OdbcApi.convertDateString(arrayOfByte1, arrayOfByte2);
    return java.sql.Date.valueOf(new String(arrayOfByte2).trim());
  }

  public Time getTime(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return null;
    byte[] arrayOfByte1 = getDataBuf(paramInt);
    byte[] arrayOfByte2 = new byte[9];
    this.OdbcApi.convertTimeString(arrayOfByte1, arrayOfByte2);
    return Time.valueOf(new String(arrayOfByte2).trim());
  }

  public Timestamp getTimestamp(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return null;
    byte[] arrayOfByte1 = getDataBuf(paramInt);
    byte[] arrayOfByte2 = new byte[30];
    this.OdbcApi.convertTimestampString(arrayOfByte1, arrayOfByte2);
    return Timestamp.valueOf(new String(arrayOfByte2).trim());
  }

  public Object getObject(int paramInt)
    throws SQLException
  {
    Object localObject = null;
    int i = getSqlType(paramInt);
    if (isNull(paramInt))
      return null;
    switch (i)
    {
    case -1:
    case 1:
    case 12:
      localObject = getString(paramInt);
      break;
    case 2:
    case 3:
      if (paramInt <= 200)
      {
        localObject = getBigDecimal(paramInt, this.scalez[paramInt]);
        break label366:
      }
      localObject = getBigDecimal(paramInt, 4);
      break;
    case -7:
      localObject = new Boolean(getBoolean(paramInt));
      break;
    case -6:
      localObject = new Integer(getByte(paramInt));
      break;
    case 5:
      localObject = new Integer(getShort(paramInt));
      break;
    case 4:
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
    label366: return localObject;
  }

  public BigDecimal getBigDecimal(int paramInt)
    throws SQLException
  {
    if (isNull(paramInt))
      return null;
    BigDecimal localBigDecimal = new BigDecimal(getString(paramInt).trim());
    return localBigDecimal;
  }

  public Object getObject(int paramInt, Map<String, Class<?>> paramMap)
    throws SQLException
  {
    return null;
  }

  public Ref getRef(int paramInt)
    throws SQLException
  {
    return null;
  }

  public Blob getBlob(int paramInt)
    throws SQLException
  {
    return null;
  }

  public Clob getClob(int paramInt)
    throws SQLException
  {
    return null;
  }

  public Array getArray(int paramInt)
    throws SQLException
  {
    return null;
  }

  public java.sql.Date getDate(int paramInt, Calendar paramCalendar)
    throws SQLException
  {
    long l = 3412047291253522432L;
    if (getDate(paramInt) == null)
      return null;
    if (getDate(paramInt) != null)
      l = this.utils.convertFromGMT(getDate(paramInt), paramCalendar);
    if (l == 3412046810217185280L)
      return null;
    return new java.sql.Date(l);
  }

  public Time getTime(int paramInt, Calendar paramCalendar)
    throws SQLException
  {
    long l = 3412047291253522432L;
    if (getTime(paramInt) == null)
      return null;
    if (getTime(paramInt) != null)
      try
      {
        l = this.utils.convertFromGMT(getTime(paramInt), paramCalendar);
      }
      catch (Exception localException)
      {
      }
    if (l == 3412046810217185280L)
      return null;
    return new Time(l);
  }

  public Timestamp getTimestamp(int paramInt, Calendar paramCalendar)
    throws SQLException
  {
    long l = 3412047291253522432L;
    if (getTimestamp(paramInt) == null)
      return null;
    try
    {
      l = this.utils.convertFromGMT(getTimestamp(paramInt), paramCalendar);
    }
    catch (Exception localException)
    {
    }
    if (l == 3412046810217185280L)
      return null;
    return new Timestamp(l);
  }

  public void registerOutParameter(int paramInt1, int paramInt2, String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  protected boolean isNull(int paramInt)
    throws SQLException
  {
    if (!(isOutputParameter(paramInt)))
      throw new SQLException("Parameter " + paramInt + " is not an OUTPUT parameter");
    boolean bool = false;
    bool = getParamLength(paramInt) == -1;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("Output Parameter " + paramInt + " null: " + bool);
    this.lastParameterNull = bool;
    return bool;
  }

  protected void setOutputParameter(int paramInt, boolean paramBoolean)
  {
    if ((paramInt >= 1) && (paramInt <= this.numParams))
      this.boundParams[(paramInt - 1)].setOutputParameter(paramBoolean);
  }

  protected boolean isOutputParameter(int paramInt)
  {
    boolean bool = false;
    if ((paramInt >= 1) && (paramInt <= this.numParams))
      bool = this.boundParams[(paramInt - 1)].isOutputParameter();
    return bool;
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
    this.myConnection.deregisterStatement(this);
  }

  public synchronized void FreeParams()
    throws NullPointerException
  {
    int i;
    try
    {
      for (i = 1; i <= this.boundParams.length; ++i)
      {
        if (this.boundParams[(i - 1)].pA1 != 3412047737930121216L)
        {
          JdbcOdbc.ReleaseStoredBytes(this.boundParams[(i - 1)].pA1, this.boundParams[(i - 1)].pA2);
          this.boundParams[(i - 1)].pA1 = 3412039732111081472L;
          this.boundParams[(i - 1)].pA2 = 3412039732111081472L;
        }
        if (this.boundParams[(i - 1)].pB1 != 3412047737930121216L)
        {
          JdbcOdbc.ReleaseStoredBytes(this.boundParams[(i - 1)].pB1, this.boundParams[(i - 1)].pB2);
          this.boundParams[(i - 1)].pB1 = 3412039732111081472L;
          this.boundParams[(i - 1)].pB2 = 3412039732111081472L;
        }
        if (this.boundParams[(i - 1)].pC1 != 3412047737930121216L)
        {
          JdbcOdbc.ReleaseStoredBytes(this.boundParams[(i - 1)].pC1, this.boundParams[(i - 1)].pC2);
          this.boundParams[(i - 1)].pC1 = 3412039732111081472L;
          this.boundParams[(i - 1)].pC2 = 3412039732111081472L;
        }
        if (this.boundParams[(i - 1)].pS1 != 3412047737930121216L)
        {
          JdbcOdbc.ReleaseStoredChars(this.boundParams[(i - 1)].pS1, this.boundParams[(i - 1)].pS2);
          this.boundParams[(i - 1)].pS1 = 3412039732111081472L;
          this.boundParams[(i - 1)].pS2 = 3412039732111081472L;
        }
      }
    }
    catch (NullPointerException localNullPointerException)
    {
    }
  }

  public URL getURL(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setURL(String paramString, URL paramURL)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNull(String paramString, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBoolean(String paramString, boolean paramBoolean)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setByte(String paramString, byte paramByte)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setShort(String paramString, short paramShort)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setInt(String paramString, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setLong(String paramString, long paramLong)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setFloat(String paramString, float paramFloat)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setDouble(String paramString, double paramDouble)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBigDecimal(String paramString, BigDecimal paramBigDecimal)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setString(String paramString1, String paramString2)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBytes(String paramString, byte[] paramArrayOfByte)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setDate(String paramString, java.sql.Date paramDate)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setTime(String paramString, Time paramTime)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setTimestamp(String paramString, Timestamp paramTimestamp)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setAsciiStream(String paramString, InputStream paramInputStream, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBinaryStream(String paramString, InputStream paramInputStream, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setObject(String paramString, Object paramObject, int paramInt1, int paramInt2)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setObject(String paramString, Object paramObject, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setObject(String paramString, Object paramObject)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setCharacterStream(String paramString, Reader paramReader, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setDate(String paramString, java.sql.Date paramDate, Calendar paramCalendar)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setTime(String paramString, Time paramTime, Calendar paramCalendar)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setTimestamp(String paramString, Timestamp paramTimestamp, Calendar paramCalendar)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNull(String paramString1, int paramInt, String paramString2)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void registerOutParameter(String paramString, int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void registerOutParameter(String paramString, int paramInt1, int paramInt2)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void registerOutParameter(String paramString1, int paramInt, String paramString2)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public String getString(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean getBoolean(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public byte getByte(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public short getShort(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int getInt(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public long getLong(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public float getFloat(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public double getDouble(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public byte[] getBytes(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public java.sql.Date getDate(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Time getTime(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Timestamp getTimestamp(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Object getObject(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public BigDecimal getBigDecimal(String paramString)
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

  public java.sql.Date getDate(String paramString, Calendar paramCalendar)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Time getTime(String paramString, Calendar paramCalendar)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Timestamp getTimestamp(String paramString, Calendar paramCalendar)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public URL getURL(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public RowId getRowId(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public RowId getRowId(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setRowId(String paramString, RowId paramRowId)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNString(String paramString1, String paramString2)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNCharacterStream(String paramString, Reader paramReader, long paramLong)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNClob(String paramString, NClob paramNClob)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setClob(String paramString, Reader paramReader, long paramLong)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setBlob(String paramString, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setNClob(String paramString, Reader paramReader, long paramLong)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public NClob getNClob(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public NClob getNClob(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public void setSQLXML(String paramString, SQLXML paramSQLXML)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public SQLXML getSQLXML(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public SQLXML getSQLXML(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean isClosed()
    throws SQLException
  {
    throw new UnsupportedOperationException();
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

  public Reader getCharacterStream(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public Reader getCharacterStream(String paramString)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void setBlob(String paramString, Blob paramBlob)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void setClob(String paramString, Clob paramClob)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public void setCharacterStream(String paramString, Reader paramReader, long paramLong)
    throws SQLException
  {
  }

  public void setBinaryStream(String paramString, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void setAsciiStream(String paramString, InputStream paramInputStream, long paramLong)
    throws SQLException
  {
  }

  public void setAsciiStream(String paramString, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void setBinaryStream(String paramString, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void setCharacterStream(String paramString, Reader paramReader)
    throws SQLException
  {
  }

  public void setNCharacterStream(String paramString, Reader paramReader)
    throws SQLException
  {
  }

  public void setClob(String paramString, Reader paramReader)
    throws SQLException
  {
  }

  public void setBlob(String paramString, InputStream paramInputStream)
    throws SQLException
  {
  }

  public void setNClob(String paramString, Reader paramReader)
    throws SQLException
  {
  }
}
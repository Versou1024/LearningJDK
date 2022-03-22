package sun.jdbc.odbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class JdbcOdbcInputStream extends InputStream
{
  protected JdbcOdbc OdbcApi;
  protected long hStmt;
  protected int column;
  protected short type;
  public static final short ASCII = 1;
  public static final short UNICODE = 2;
  public static final short BINARY = 3;
  public static final short LOCAL = 4;
  public static final short CHARACTER = 5;
  protected byte[] localByteArray;
  protected int localOffset;
  protected boolean invalid;
  protected boolean highRead;
  protected int sqlType;
  protected byte[] buf;
  public static final int MAX_BUF_LEN = 5120;
  protected int convertType;
  public static final int CONVERT_NONE = 0;
  public static final int CONVERT_UNICODE = 1;
  public static final int CONVERT_ASCII = 2;
  public static final int CONVERT_BOTH = 3;
  protected int convertMultiplier;
  protected int bytesInBuf;
  protected int bufOffset;
  protected Statement ownerStatement;

  public JdbcOdbcInputStream(JdbcOdbc paramJdbcOdbc, long paramLong, int paramInt1, short paramShort, int paramInt2, Statement paramStatement)
  {
    this.OdbcApi = paramJdbcOdbc;
    this.hStmt = paramLong;
    this.column = paramInt1;
    this.type = paramShort;
    this.invalid = false;
    this.ownerStatement = paramStatement;
    this.sqlType = -2;
    switch (paramInt2)
    {
    case -10:
    case -9:
    case -8:
    case -1:
    case 1:
    case 12:
      this.sqlType = 1;
    }
    this.convertMultiplier = 1;
    this.convertType = 0;
    switch (this.type)
    {
    case 1:
      if (this.sqlType == -2)
      {
        this.convertMultiplier = 2;
        this.convertType = 2;
      }
      break;
    case 2:
      if (this.sqlType == -2)
      {
        this.convertType = 3;
        this.convertMultiplier = 4;
      }
      else
      {
        this.convertType = 1;
        this.convertMultiplier = 2;
      }
      break;
    case 5:
      this.convertType = 0;
      this.convertMultiplier = 1;
    case 3:
    case 4:
    }
    this.buf = new byte[5120 * this.convertMultiplier];
    this.bytesInBuf = 0;
    this.bufOffset = 0;
  }

  public JdbcOdbcInputStream(JdbcOdbc paramJdbcOdbc, long paramLong, int paramInt, byte[] paramArrayOfByte)
  {
    this.OdbcApi = paramJdbcOdbc;
    this.hStmt = paramLong;
    this.column = paramInt;
    this.type = 4;
    this.localByteArray = paramArrayOfByte;
    this.localOffset = 0;
    this.invalid = false;
  }

  public int read()
    throws IOException
  {
    byte[] arrayOfByte = new byte[1];
    int i = read(arrayOfByte);
    if (i != -1)
      i = arrayOfByte[0] & 0xFF;
    return i;
  }

  public int read(byte[] paramArrayOfByte)
    throws IOException
  {
    return read(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public byte[] readAllData()
    throws IOException
  {
    byte[] arrayOfByte;
    int i = 0;
    if (this.invalid)
      throw new IOException("InputStream is no longer valid - the Statement has been closed, or the cursor has been moved");
    switch (this.type)
    {
    case 4:
      if (this.localOffset + i > this.localByteArray.length)
        i = this.localByteArray.length - this.localOffset;
      arrayOfByte = new byte[this.localByteArray.length];
      if (i == 0)
      {
        i = -1;
        break label117:
      }
      System.arraycopy(this.localByteArray, this.localOffset, arrayOfByte, this.localOffset, i);
      this.localOffset += i;
      break;
    default:
      arrayOfByte = readData();
    }
    label117: return arrayOfByte;
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if ((paramInt1 < 0) || (paramInt1 > paramArrayOfByte.length) || (paramInt2 < 0) || (paramInt1 + paramInt2 > paramArrayOfByte.length) || (paramInt1 + paramInt2 < 0))
      throw new IndexOutOfBoundsException();
    if (paramInt2 == 0)
      return -1;
    int i = 0;
    if (this.invalid)
      throw new IOException("InputStream is no longer valid - the Statement has been closed, or the cursor has been moved");
    switch (this.type)
    {
    case 4:
      i = paramInt2;
      if (this.localOffset + i > this.localByteArray.length)
        i = this.localByteArray.length - this.localOffset;
      if (i == 0)
      {
        i = -1;
        break label180:
      }
      for (int j = paramInt1; j < i; ++j)
        paramArrayOfByte[j] = this.localByteArray[(this.localOffset + j)];
      this.localOffset += i;
      break;
    default:
      i = readData(paramArrayOfByte, paramInt1, paramInt2);
    }
    label180: return i;
  }

  public int available()
    throws IOException
  {
    throw new IOException();
  }

  public void invalidate()
  {
    this.invalid = true;
  }

  public byte[] readData()
    throws IOException
  {
    Object localObject = null;
    int i = 0;
    while (true)
    {
      this.bytesInBuf = readBinaryData(this.buf, 5120);
      this.bytesInBuf = convertData(this.buf, this.bytesInBuf);
      if (this.bytesInBuf == -1)
        return localObject;
      try
      {
        if (localObject == null)
        {
          localObject = new byte[this.bytesInBuf];
        }
        else
        {
          byte[] arrayOfByte = new byte[i + this.bytesInBuf];
          System.arraycopy(localObject, 0, arrayOfByte, 0, i);
          localObject = arrayOfByte;
        }
      }
      catch (OutOfMemoryError localOutOfMemoryError)
      {
        ((JdbcOdbcStatement)this.ownerStatement).setWarning(new SQLWarning("Data has been truncated. " + localOutOfMemoryError.getMessage()));
        return localObject;
      }
      System.arraycopy(this.buf, 0, localObject, i, this.bytesInBuf);
      i += this.bytesInBuf;
    }
  }

  protected int readData(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    int i = -1;
    int j = paramInt1;
    while (true)
    {
      while (true)
      {
        if ((this.bytesInBuf == -1) || (j - paramInt1 >= paramInt2))
          break label101;
        if (this.bufOffset < this.bytesInBuf)
          break;
        this.bytesInBuf = readBinaryData(this.buf, 5120);
        this.bytesInBuf = convertData(this.buf, this.bytesInBuf);
        this.bufOffset = 0;
      }
      paramArrayOfByte[j] = this.buf[this.bufOffset];
      ++j;
      this.bufOffset += 1;
    }
    if (j > paramInt1)
      label101: i = j;
    return i;
  }

  protected int readBinaryData(byte[] paramArrayOfByte, int paramInt)
    throws IOException
  {
    int i = 0;
    try
    {
      i = this.OdbcApi.SQLGetDataBinary(this.hStmt, this.column, -2, paramArrayOfByte, paramInt);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      Integer localInteger = (Integer)localJdbcOdbcSQLWarning.value;
      i = localInteger.intValue();
    }
    catch (SQLException localSQLException)
    {
      throw new IOException(localSQLException.getMessage());
    }
    return i;
  }

  protected int convertData(byte[] paramArrayOfByte, int paramInt)
  {
    if (this.convertType == 0)
      return paramInt;
    String str = "0123456789ABCDEF";
    if (paramInt <= 0)
      return paramInt;
    for (int i = paramInt - 1; i >= 0; --i)
      if (this.convertType == 3)
      {
        paramArrayOfByte[(i * 4 + 3)] = (byte)str.charAt(paramArrayOfByte[i] & 0xF);
        paramArrayOfByte[(i * 4 + 2)] = 0;
        paramArrayOfByte[(i * 4 + 1)] = (byte)str.charAt(paramArrayOfByte[i] >> 4 & 0xF);
        paramArrayOfByte[(i * 4)] = 0;
      }
      else if (this.convertType == 2)
      {
        paramArrayOfByte[(i * 2 + 1)] = (byte)str.charAt(paramArrayOfByte[i] & 0xF);
        paramArrayOfByte[(i * 2)] = (byte)str.charAt(paramArrayOfByte[i] >> 4 & 0xF);
      }
      else
      {
        paramArrayOfByte[(i * 2 + 1)] = paramArrayOfByte[i];
        paramArrayOfByte[(i * 2)] = 0;
      }
    return (paramInt * this.convertMultiplier);
  }
}
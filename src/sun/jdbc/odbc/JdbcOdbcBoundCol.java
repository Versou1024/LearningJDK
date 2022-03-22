package sun.jdbc.odbc;

import java.io.InputStream;

public class JdbcOdbcBoundCol extends JdbcOdbcObject
{
  protected int type = 9999;
  protected int len = -1;
  protected JdbcOdbcInputStream inputStream;
  protected boolean isRenamed = false;
  protected String aliasName = null;
  protected int rowSetSize;
  protected Object colObj;
  protected Object[] columnWiseData;
  protected byte[] columnWiseLength;
  protected byte[] binaryData;
  protected int streamType;
  public static final short ASCII = 1;
  public static final short UNICODE = 2;
  public static final short BINARY = 3;
  protected long pA1 = 3412045521726996480L;
  protected long pA2 = 3412045521726996480L;
  protected long pB1 = 3412045521726996480L;
  protected long pB2 = 3412045521726996480L;
  protected long pC1 = 3412045521726996480L;
  protected long pC2 = 3412045521726996480L;
  protected long pS1 = 3412045521726996480L;
  protected long pS2 = 3412045521726996480L;

  public void setInputStream(JdbcOdbcInputStream paramJdbcOdbcInputStream)
  {
    this.inputStream = paramJdbcOdbcInputStream;
  }

  public void closeInputStream()
  {
    if (this.inputStream != null)
    {
      this.inputStream.invalidate();
      this.inputStream = null;
    }
  }

  public void setType(int paramInt)
  {
    this.type = paramInt;
  }

  public int getType()
  {
    return this.type;
  }

  public void setLength(int paramInt)
  {
    this.len = paramInt;
  }

  public int getLength()
  {
    return this.len;
  }

  public void setAliasName(String paramString)
  {
    this.aliasName = paramString;
    this.isRenamed = true;
  }

  public String mapAliasName(String paramString)
  {
    if (this.isRenamed == true)
      return this.aliasName;
    return paramString;
  }

  public void setColumnValue(Object paramObject, int paramInt)
  {
    try
    {
      if ((this.type == -1) || (this.type == -4))
        if ((InputStream)paramObject != null)
          setInputStream((JdbcOdbcInputStream)paramObject);
        else
          this.colObj = paramObject;
      else
        this.colObj = paramObject;
      setLength(paramInt);
    }
    catch (Exception localException)
    {
    }
  }

  public Object getColumnValue()
  {
    if ((this.type == -1) || (this.type == -4))
    {
      if (this.inputStream != null)
        return this.inputStream;
      return this.colObj;
    }
    return this.colObj;
  }

  public JdbcOdbcInputStream getInputStream()
  {
    return this.inputStream;
  }

  public void initStagingArea(int paramInt)
  {
    this.rowSetSize = paramInt;
    this.columnWiseData = new Object[this.rowSetSize + 1];
    this.columnWiseLength = new byte[(this.rowSetSize + 1) * JdbcOdbcPlatform.getLengthBufferSize()];
    byte[] arrayOfByte = JdbcOdbcPlatform.convertIntToByteArray(-6);
    int i = 0;
    while (i < (this.rowSetSize + 1) * arrayOfByte.length)
    {
      for (int j = 0; j < arrayOfByte.length; ++j)
        this.columnWiseLength[(i + j)] = arrayOfByte[j];
      i += arrayOfByte.length;
    }
  }

  public void resetColumnToIgnoreData()
  {
    byte[] arrayOfByte = JdbcOdbcPlatform.convertIntToByteArray(-6);
    int i = 0;
    while (i < (this.rowSetSize + 1) * arrayOfByte.length)
    {
      for (int j = 0; j < arrayOfByte.length; ++j)
        this.columnWiseLength[(i + j)] = arrayOfByte[j];
      i += arrayOfByte.length;
    }
  }

  public void setRowValues(int paramInt1, Object paramObject, int paramInt2)
  {
    this.columnWiseData[paramInt1] = paramObject;
    byte[] arrayOfByte = JdbcOdbcPlatform.convertIntToByteArray(paramInt2);
    int i = paramInt1 * arrayOfByte.length;
    for (int j = i; j < i + arrayOfByte.length; ++j)
      this.columnWiseLength[j] = arrayOfByte[(j - i)];
  }

  public Object getRowValue(int paramInt)
  {
    return this.columnWiseData[paramInt];
  }

  public int getRowLenInd(int paramInt)
  {
    return this.columnWiseLength[paramInt];
  }

  public Object[] getRowValues()
  {
    return this.columnWiseData;
  }

  public byte[] getRowLengths()
  {
    return this.columnWiseLength;
  }

  public byte[] allocBindDataBuffer(int paramInt)
  {
    this.binaryData = new byte[paramInt];
    return this.binaryData;
  }

  public void setStreamType(int paramInt)
  {
    this.streamType = paramInt;
  }

  public int getStreamType()
  {
    return this.streamType;
  }
}
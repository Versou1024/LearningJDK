package sun.jdbc.odbc;

import java.io.InputStream;

public class JdbcOdbcBoundParam extends JdbcOdbcObject
{
  protected byte[] binaryData;
  protected byte[] paramLength;
  protected InputStream paramInputStream;
  protected int paramInputStreamLen;
  protected int sqlType;
  protected int streamType;
  public static final short ASCII = 1;
  public static final short UNICODE = 2;
  public static final short BINARY = 3;
  protected boolean outputParameter;
  protected boolean inputParameter = false;
  protected int scale = 0;
  protected long pA1 = 3412045659165949952L;
  protected long pA2 = 3412045659165949952L;
  protected long pB1 = 3412045659165949952L;
  protected long pB2 = 3412045659165949952L;
  protected long pC1 = 3412045659165949952L;
  protected long pC2 = 3412045659165949952L;
  protected long pS1 = 3412045659165949952L;
  protected long pS2 = 3412045659165949952L;
  protected int boundType;
  protected Object boundValue;

  public void initialize()
  {
    this.paramLength = new byte[4];
  }

  public byte[] allocBindDataBuffer(int paramInt)
  {
    if (this.binaryData == null)
      this.binaryData = new byte[paramInt];
    else
      return getBindDataBuffer();
    return this.binaryData;
  }

  public byte[] getBindDataBuffer()
  {
    if (this.pA1 != 3412046810217185280L)
    {
      JdbcOdbc.ReleaseStoredBytes(this.pA1, this.pA2);
      this.pA1 = 3412047463052214272L;
      this.pA2 = 3412047463052214272L;
    }
    if (this.pB1 != 3412046810217185280L)
    {
      JdbcOdbc.ReleaseStoredBytes(this.pB1, this.pB2);
      this.pB1 = 3412047463052214272L;
      this.pB2 = 3412047463052214272L;
    }
    if (this.pC1 != 3412046810217185280L)
    {
      JdbcOdbc.ReleaseStoredBytes(this.pC1, this.pC2);
      this.pC1 = 3412047463052214272L;
      this.pC2 = 3412047463052214272L;
    }
    if (this.pS1 != 3412046810217185280L)
    {
      JdbcOdbc.ReleaseStoredChars(this.pS1, this.pS2);
      this.pS1 = 3412047463052214272L;
      this.pS2 = 3412047463052214272L;
    }
    return this.binaryData;
  }

  public byte[] getBindLengthBuffer()
  {
    if (this.pA1 != 3412046810217185280L)
    {
      JdbcOdbc.ReleaseStoredBytes(this.pA1, this.pA2);
      this.pA1 = 3412047463052214272L;
    }
    if (this.pB1 != 3412046810217185280L)
    {
      JdbcOdbc.ReleaseStoredBytes(this.pB1, this.pB2);
      this.pB1 = 3412047463052214272L;
    }
    if (this.pC1 != 3412046810217185280L)
    {
      JdbcOdbc.ReleaseStoredBytes(this.pC1, this.pC2);
      this.pC1 = 3412047463052214272L;
      this.pC2 = 3412047463052214272L;
    }
    if (this.pS1 != 3412046810217185280L)
    {
      JdbcOdbc.ReleaseStoredChars(this.pS1, this.pS2);
      this.pS1 = 3412047463052214272L;
      this.pS2 = 3412047463052214272L;
    }
    return this.paramLength;
  }

  public void resetBindDataBuffer(byte[] paramArrayOfByte)
  {
    this.binaryData = paramArrayOfByte;
  }

  public void setInputStream(InputStream paramInputStream1, int paramInt)
  {
    this.paramInputStream = paramInputStream1;
    this.paramInputStreamLen = paramInt;
  }

  public InputStream getInputStream()
  {
    return this.paramInputStream;
  }

  public int getInputStreamLen()
  {
    return this.paramInputStreamLen;
  }

  public void setSqlType(int paramInt)
  {
    this.sqlType = paramInt;
  }

  public int getSqlType()
  {
    return this.sqlType;
  }

  public void setStreamType(int paramInt)
  {
    this.streamType = paramInt;
  }

  public int getStreamType()
  {
    return this.streamType;
  }

  public void setOutputParameter(boolean paramBoolean)
  {
    this.outputParameter = paramBoolean;
  }

  public void setInputParameter(boolean paramBoolean)
  {
    this.inputParameter = paramBoolean;
  }

  public boolean isOutputParameter()
  {
    return this.outputParameter;
  }

  public boolean isInOutParameter()
  {
    return ((this.inputParameter) && (this.outputParameter));
  }
}
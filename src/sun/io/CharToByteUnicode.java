package sun.io;

import java.security.AccessController;
import sun.security.action.GetPropertyAction;

public class CharToByteUnicode extends CharToByteConverter
{
  static final char BYTE_ORDER_MARK = 65279;
  protected boolean usesMark;
  private boolean markWritten;
  static final int UNKNOWN = 0;
  static final int BIG = 1;
  static final int LITTLE = 2;
  protected int byteOrder;

  public CharToByteUnicode()
  {
    this.usesMark = true;
    this.markWritten = false;
    this.byteOrder = 0;
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.io.unicode.encoding", "UnicodeBig"));
    if (str.equals("UnicodeBig"))
      this.byteOrder = 1;
    else if (str.equals("UnicodeLittle"))
      this.byteOrder = 2;
    else
      this.byteOrder = 1;
  }

  public CharToByteUnicode(int paramInt, boolean paramBoolean)
  {
    this.usesMark = true;
    this.markWritten = false;
    this.byteOrder = 0;
    this.byteOrder = paramInt;
    this.usesMark = paramBoolean;
  }

  public CharToByteUnicode(boolean paramBoolean)
  {
    this.usesMark = paramBoolean;
  }

  public String getCharacterEncoding()
  {
    switch (this.byteOrder)
    {
    case 1:
      return ((this.usesMark) ? "UnicodeBig" : "UnicodeBigUnmarked");
    case 2:
      return ((this.usesMark) ? "UnicodeLittle" : "UnicodeLittleUnmarked");
    }
    return "UnicodeUnknown";
  }

  public int convert(char[] paramArrayOfChar, int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3, int paramInt4)
    throws sun.io.ConversionBufferFullException, sun.io.MalformedInputException
  {
    int l;
    this.charOff = paramInt1;
    this.byteOff = paramInt3;
    if (paramInt1 >= paramInt2)
      return 0;
    int i = paramInt1;
    int j = paramInt3;
    int k = paramInt4 - 2;
    if ((this.usesMark) && (!(this.markWritten)))
    {
      if (j > k)
        throw new sun.io.ConversionBufferFullException();
      if (this.byteOrder == 1)
      {
        paramArrayOfByte[(j++)] = -2;
        paramArrayOfByte[(j++)] = -1;
      }
      else
      {
        paramArrayOfByte[(j++)] = -1;
        paramArrayOfByte[(j++)] = -2;
      }
      this.markWritten = true;
    }
    if (this.byteOrder == 1)
      while (true)
      {
        if (i >= paramInt2)
          break label270;
        if (j > k)
        {
          this.charOff = i;
          this.byteOff = j;
          throw new sun.io.ConversionBufferFullException();
        }
        l = paramArrayOfChar[(i++)];
        paramArrayOfByte[(j++)] = (byte)(l >> 8);
        paramArrayOfByte[(j++)] = (byte)(l & 0xFF);
      }
    while (i < paramInt2)
    {
      if (j > k)
      {
        this.charOff = i;
        this.byteOff = j;
        throw new sun.io.ConversionBufferFullException();
      }
      l = paramArrayOfChar[(i++)];
      paramArrayOfByte[(j++)] = (byte)(l & 0xFF);
      paramArrayOfByte[(j++)] = (byte)(l >> 8);
    }
    label270: this.charOff = i;
    this.byteOff = j;
    return (j - paramInt3);
  }

  public int flush(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    this.byteOff = (this.charOff = 0);
    return 0;
  }

  public void reset()
  {
    this.byteOff = (this.charOff = 0);
    this.markWritten = false;
  }

  public int getMaxBytesPerChar()
  {
    return 4;
  }
}
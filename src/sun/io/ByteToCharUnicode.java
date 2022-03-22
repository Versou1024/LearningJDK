package sun.io;

public class ByteToCharUnicode extends ByteToCharConverter
{
  static final char BYTE_ORDER_MARK = 65279;
  static final char REVERSED_MARK = 65534;
  static final int AUTO = 0;
  static final int BIG = 1;
  static final int LITTLE = 2;
  int originalByteOrder;
  int byteOrder;
  boolean usesMark;
  boolean started = false;
  int leftOverByte;
  boolean leftOver = false;

  public ByteToCharUnicode()
  {
    this.originalByteOrder = (this.byteOrder = 0);
    this.usesMark = true;
  }

  protected ByteToCharUnicode(int paramInt, boolean paramBoolean)
  {
    this.originalByteOrder = (this.byteOrder = paramInt);
    this.usesMark = paramBoolean;
  }

  public String getCharacterEncoding()
  {
    switch (this.originalByteOrder)
    {
    case 1:
      return ((this.usesMark) ? "UnicodeBig" : "UnicodeBigUnmarked");
    case 2:
      return ((this.usesMark) ? "UnicodeLittle" : "UnicodeLittleUnmarked");
    }
    return "Unicode";
  }

  public int convert(byte[] paramArrayOfByte, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3, int paramInt4)
    throws sun.io.ConversionBufferFullException, sun.io.MalformedInputException
  {
    int i;
    int j;
    int i2;
    this.byteOff = paramInt1;
    this.charOff = paramInt3;
    if (paramInt1 >= paramInt2)
      return 0;
    int k = 0;
    int l = paramInt1;
    int i1 = paramInt3;
    if (this.leftOver)
    {
      i = this.leftOverByte & 0xFF;
      this.leftOver = false;
    }
    else
    {
      i = paramArrayOfByte[(l++)] & 0xFF;
    }
    k = 1;
    if ((this.usesMark) && (!(this.started)) && (l < paramInt2))
    {
      j = paramArrayOfByte[(l++)] & 0xFF;
      k = 2;
      i2 = (char)(i << 8 | j);
      int i3 = 0;
      if (i2 == 65279)
        i3 = 1;
      else if (i2 == 65534)
        i3 = 2;
      if (this.byteOrder == 0)
      {
        if (i3 == 0)
        {
          this.badInputLength = k;
          throw new sun.io.MalformedInputException("Missing byte-order mark");
        }
        this.byteOrder = i3;
        if (l < paramInt2)
        {
          i = paramArrayOfByte[(l++)] & 0xFF;
          k = 1;
        }
      }
      else if (i3 == 0)
      {
        --l;
        k = 1;
      }
      else if (this.byteOrder == i3)
      {
        if (l < paramInt2)
        {
          i = paramArrayOfByte[(l++)] & 0xFF;
          k = 1;
        }
      }
      else
      {
        this.badInputLength = k;
        throw new sun.io.MalformedInputException("Incorrect byte-order mark");
      }
      this.started = true;
    }
    while (l < paramInt2)
    {
      j = paramArrayOfByte[(l++)] & 0xFF;
      k = 2;
      if (this.byteOrder == 1)
        i2 = (char)(i << 8 | j);
      else
        i2 = (char)(j << 8 | i);
      if (i2 == 65534)
        throw new sun.io.MalformedInputException("Reversed byte-order mark");
      if (i1 >= paramInt4)
        throw new sun.io.ConversionBufferFullException();
      paramArrayOfChar[(i1++)] = i2;
      this.byteOff = l;
      this.charOff = i1;
      if (l < paramInt2)
      {
        i = paramArrayOfByte[(l++)] & 0xFF;
        k = 1;
      }
    }
    if (k == 1)
    {
      this.leftOverByte = i;
      this.byteOff = l;
      this.leftOver = true;
    }
    return (i1 - paramInt3);
  }

  public void reset()
  {
    this.leftOver = false;
    this.byteOff = (this.charOff = 0);
    this.started = false;
    this.byteOrder = this.originalByteOrder;
  }

  public int flush(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws sun.io.MalformedInputException
  {
    if (this.leftOver)
    {
      reset();
      throw new sun.io.MalformedInputException();
    }
    this.byteOff = (this.charOff = 0);
    return 0;
  }
}
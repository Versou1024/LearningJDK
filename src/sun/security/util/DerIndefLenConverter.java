package sun.security.util;

import java.io.IOException;
import java.util.ArrayList;

class DerIndefLenConverter
{
  private static final int TAG_MASK = 31;
  private static final int FORM_MASK = 32;
  private static final int CLASS_MASK = 192;
  private static final int LEN_LONG = 128;
  private static final int LEN_MASK = 127;
  private static final int SKIP_EOC_BYTES = 2;
  private byte[] data;
  private byte[] newData;
  private int newDataPos;
  private int dataPos;
  private int dataSize;
  private int index;
  private ArrayList ndefsList = new ArrayList();
  private int numOfTotalLenBytes = 0;

  private boolean isEOC(int paramInt)
  {
    return (((paramInt & 0x1F) == 0) && ((paramInt & 0x20) == 0) && ((paramInt & 0xC0) == 0));
  }

  static boolean isLongForm(int paramInt)
  {
    return ((paramInt & 0x80) == 128);
  }

  static boolean isIndefinite(int paramInt)
  {
    return ((isLongForm(paramInt)) && ((paramInt & 0x7F) == 0));
  }

  private void parseTag()
    throws IOException
  {
    if (this.dataPos == this.dataSize)
      return;
    if ((isEOC(this.data[this.dataPos])) && (this.data[(this.dataPos + 1)] == 0))
    {
      int i = 0;
      Object localObject = null;
      for (int j = this.ndefsList.size() - 1; j >= 0; --j)
      {
        localObject = this.ndefsList.get(j);
        if (localObject instanceof Integer)
          break;
        i += ((byte[])(byte[])localObject).length - 3;
      }
      if (j < 0)
        throw new IOException("EOC does not have matching indefinite-length tag");
      int k = this.dataPos - ((Integer)localObject).intValue() + i;
      byte[] arrayOfByte = getLengthBytes(k);
      this.ndefsList.set(j, arrayOfByte);
      this.numOfTotalLenBytes += arrayOfByte.length - 3;
    }
    this.dataPos += 1;
  }

  private void writeTag()
  {
    if (this.dataPos == this.dataSize)
      return;
    int i = this.data[(this.dataPos++)];
    if ((isEOC(i)) && (this.data[this.dataPos] == 0))
    {
      this.dataPos += 1;
      writeTag();
    }
    else
    {
      this.newData[(this.newDataPos++)] = (byte)i;
    }
  }

  private int parseLength()
    throws IOException
  {
    int i = 0;
    if (this.dataPos == this.dataSize)
      return i;
    int j = this.data[(this.dataPos++)] & 0xFF;
    if (isIndefinite(j))
    {
      this.ndefsList.add(new Integer(this.dataPos));
      return i;
    }
    if (isLongForm(j))
    {
      j &= 127;
      if (j > 4)
        throw new IOException("Too much data");
      if (this.dataSize - this.dataPos < j + 1)
        throw new IOException("Too little data");
      for (int k = 0; k < j; ++k)
        i = (i << 8) + (this.data[(this.dataPos++)] & 0xFF);
    }
    else
    {
      i = j & 0x7F;
    }
    return i;
  }

  private void writeLengthAndValue()
    throws IOException
  {
    if (this.dataPos == this.dataSize)
      return;
    int i = 0;
    int j = this.data[(this.dataPos++)] & 0xFF;
    if (isIndefinite(j))
    {
      byte[] arrayOfByte = (byte[])(byte[])this.ndefsList.get(this.index++);
      System.arraycopy(arrayOfByte, 0, this.newData, this.newDataPos, arrayOfByte.length);
      this.newDataPos += arrayOfByte.length;
      return;
    }
    if (isLongForm(j))
    {
      j &= 127;
      for (int k = 0; k < j; ++k)
        i = (i << 8) + (this.data[(this.dataPos++)] & 0xFF);
    }
    else
    {
      i = j & 0x7F;
    }
    writeLength(i);
    writeValue(i);
  }

  private void writeLength(int paramInt)
  {
    if (paramInt < 128)
    {
      this.newData[(this.newDataPos++)] = (byte)paramInt;
    }
    else if (paramInt < 256)
    {
      this.newData[(this.newDataPos++)] = -127;
      this.newData[(this.newDataPos++)] = (byte)paramInt;
    }
    else if (paramInt < 65536)
    {
      this.newData[(this.newDataPos++)] = -126;
      this.newData[(this.newDataPos++)] = (byte)(paramInt >> 8);
      this.newData[(this.newDataPos++)] = (byte)paramInt;
    }
    else if (paramInt < 16777216)
    {
      this.newData[(this.newDataPos++)] = -125;
      this.newData[(this.newDataPos++)] = (byte)(paramInt >> 16);
      this.newData[(this.newDataPos++)] = (byte)(paramInt >> 8);
      this.newData[(this.newDataPos++)] = (byte)paramInt;
    }
    else
    {
      this.newData[(this.newDataPos++)] = -124;
      this.newData[(this.newDataPos++)] = (byte)(paramInt >> 24);
      this.newData[(this.newDataPos++)] = (byte)(paramInt >> 16);
      this.newData[(this.newDataPos++)] = (byte)(paramInt >> 8);
      this.newData[(this.newDataPos++)] = (byte)paramInt;
    }
  }

  private byte[] getLengthBytes(int paramInt)
  {
    byte[] arrayOfByte;
    int i = 0;
    if (paramInt < 128)
    {
      arrayOfByte = new byte[1];
      arrayOfByte[(i++)] = (byte)paramInt;
    }
    else if (paramInt < 256)
    {
      arrayOfByte = new byte[2];
      arrayOfByte[(i++)] = -127;
      arrayOfByte[(i++)] = (byte)paramInt;
    }
    else if (paramInt < 65536)
    {
      arrayOfByte = new byte[3];
      arrayOfByte[(i++)] = -126;
      arrayOfByte[(i++)] = (byte)(paramInt >> 8);
      arrayOfByte[(i++)] = (byte)paramInt;
    }
    else if (paramInt < 16777216)
    {
      arrayOfByte = new byte[4];
      arrayOfByte[(i++)] = -125;
      arrayOfByte[(i++)] = (byte)(paramInt >> 16);
      arrayOfByte[(i++)] = (byte)(paramInt >> 8);
      arrayOfByte[(i++)] = (byte)paramInt;
    }
    else
    {
      arrayOfByte = new byte[5];
      arrayOfByte[(i++)] = -124;
      arrayOfByte[(i++)] = (byte)(paramInt >> 24);
      arrayOfByte[(i++)] = (byte)(paramInt >> 16);
      arrayOfByte[(i++)] = (byte)(paramInt >> 8);
      arrayOfByte[(i++)] = (byte)paramInt;
    }
    return arrayOfByte;
  }

  private int getNumOfLenBytes(int paramInt)
  {
    int i = 0;
    if (paramInt < 128)
      i = 1;
    else if (paramInt < 256)
      i = 2;
    else if (paramInt < 65536)
      i = 3;
    else if (paramInt < 16777216)
      i = 4;
    else
      i = 5;
    return i;
  }

  private void parseValue(int paramInt)
  {
    this.dataPos += paramInt;
  }

  private void writeValue(int paramInt)
  {
    for (int i = 0; i < paramInt; ++i)
      this.newData[(this.newDataPos++)] = this.data[(this.dataPos++)];
  }

  byte[] convert(byte[] paramArrayOfByte)
    throws IOException
  {
    this.data = paramArrayOfByte;
    this.dataPos = 0;
    this.index = 0;
    this.dataSize = this.data.length;
    int i = 0;
    while (this.dataPos < this.dataSize)
    {
      parseTag();
      i = parseLength();
      parseValue(i);
    }
    this.newData = new byte[this.dataSize + this.numOfTotalLenBytes];
    this.dataPos = 0;
    this.newDataPos = 0;
    this.index = 0;
    while (this.dataPos < this.dataSize)
    {
      writeTag();
      writeLengthAndValue();
    }
    return this.newData;
  }
}
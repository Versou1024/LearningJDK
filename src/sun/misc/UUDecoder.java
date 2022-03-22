package sun.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;

public class UUDecoder extends CharacterDecoder
{
  public String bufferName;
  public int mode;
  private byte[] decoderBuffer = new byte[4];

  protected int bytesPerAtom()
  {
    return 3;
  }

  protected int bytesPerLine()
  {
    return 45;
  }

  protected void decodeAtom(PushbackInputStream paramPushbackInputStream, OutputStream paramOutputStream, int paramInt)
    throws IOException
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < 4; ++i)
    {
      int j = paramPushbackInputStream.read();
      if (j == -1)
        throw new CEStreamExhausted();
      localStringBuffer.append((char)j);
      this.decoderBuffer[i] = (byte)(j - 32 & 0x3F);
    }
    int k = this.decoderBuffer[0] << 2 & 0xFC | this.decoderBuffer[1] >>> 4 & 0x3;
    int l = this.decoderBuffer[1] << 4 & 0xF0 | this.decoderBuffer[2] >>> 2 & 0xF;
    int i1 = this.decoderBuffer[2] << 6 & 0xC0 | this.decoderBuffer[3] & 0x3F;
    paramOutputStream.write((byte)(k & 0xFF));
    if (paramInt > 1)
      paramOutputStream.write((byte)(l & 0xFF));
    if (paramInt > 2)
      paramOutputStream.write((byte)(i1 & 0xFF));
  }

  protected void decodeBufferPrefix(PushbackInputStream paramPushbackInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    int i;
    StringBuffer localStringBuffer = new StringBuffer(32);
    int j = 1;
    while (true)
    {
      i = paramPushbackInputStream.read();
      if (i == -1)
        throw new CEFormatException("UUDecoder: No begin line.");
      if ((i == 98) && (j != 0))
      {
        i = paramPushbackInputStream.read();
        if (i == 101)
          break;
      }
      j = ((i == 10) || (i == 13)) ? 1 : 0;
    }
    while (true)
    {
      do
      {
        if ((i == 10) || (i == 13))
          break label136;
        i = paramPushbackInputStream.read();
        if (i == -1)
          throw new CEFormatException("UUDecoder: No begin line.");
      }
      while ((i == 10) || (i == 13));
      localStringBuffer.append((char)i);
    }
    label136: String str = localStringBuffer.toString();
    if (str.indexOf(32) != 3)
      throw new CEFormatException("UUDecoder: Malformed begin line.");
    this.mode = Integer.parseInt(str.substring(4, 7));
    this.bufferName = str.substring(str.indexOf(32, 6) + 1);
    if (i == 13)
    {
      i = paramPushbackInputStream.read();
      if ((i != 10) && (i != -1))
        paramPushbackInputStream.unread(i);
    }
  }

  protected int decodeLinePrefix(PushbackInputStream paramPushbackInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    int i = paramPushbackInputStream.read();
    if (i == 32)
    {
      i = paramPushbackInputStream.read();
      i = paramPushbackInputStream.read();
      if ((i != 10) && (i != -1))
        paramPushbackInputStream.unread(i);
      throw new CEStreamExhausted();
    }
    if (i == -1)
      throw new CEFormatException("UUDecoder: Short Buffer.");
    i = i - 32 & 0x3F;
    if (i > bytesPerLine())
      throw new CEFormatException("UUDecoder: Bad Line Length.");
    return i;
  }

  protected void decodeLineSuffix(PushbackInputStream paramPushbackInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    do
    {
      i = paramPushbackInputStream.read();
      if (i == -1)
        throw new CEStreamExhausted();
      if (i == 10)
        return;
    }
    while (i != 13);
    int i = paramPushbackInputStream.read();
    if ((i != 10) && (i != -1))
      paramPushbackInputStream.unread(i);
  }

  protected void decodeBufferSuffix(PushbackInputStream paramPushbackInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    int i = paramPushbackInputStream.read(this.decoderBuffer);
    if ((this.decoderBuffer[0] != 101) || (this.decoderBuffer[1] != 110) || (this.decoderBuffer[2] != 100))
      throw new CEFormatException("UUDecoder: Missing 'end' line.");
  }
}
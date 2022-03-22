package sun.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class UUEncoder extends CharacterEncoder
{
  private String bufferName;
  private int mode;

  public UUEncoder()
  {
    this.bufferName = "encoder.buf";
    this.mode = 644;
  }

  public UUEncoder(String paramString)
  {
    this.bufferName = paramString;
    this.mode = 644;
  }

  public UUEncoder(String paramString, int paramInt)
  {
    this.bufferName = paramString;
    this.mode = paramInt;
  }

  protected int bytesPerAtom()
  {
    return 3;
  }

  protected int bytesPerLine()
  {
    return 45;
  }

  protected void encodeAtom(OutputStream paramOutputStream, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    int j = 1;
    int k = 1;
    int i = paramArrayOfByte[paramInt1];
    if (paramInt2 > 1)
      j = paramArrayOfByte[(paramInt1 + 1)];
    if (paramInt2 > 2)
      k = paramArrayOfByte[(paramInt1 + 2)];
    int l = i >>> 2 & 0x3F;
    int i1 = i << 4 & 0x30 | j >>> 4 & 0xF;
    int i2 = j << 2 & 0x3C | k >>> 6 & 0x3;
    int i3 = k & 0x3F;
    paramOutputStream.write(l + 32);
    paramOutputStream.write(i1 + 32);
    paramOutputStream.write(i2 + 32);
    paramOutputStream.write(i3 + 32);
  }

  protected void encodeLinePrefix(OutputStream paramOutputStream, int paramInt)
    throws IOException
  {
    paramOutputStream.write((paramInt & 0x3F) + 32);
  }

  protected void encodeLineSuffix(OutputStream paramOutputStream)
    throws IOException
  {
    this.pStream.println();
  }

  protected void encodeBufferPrefix(OutputStream paramOutputStream)
    throws IOException
  {
    this.pStream = new PrintStream(paramOutputStream);
    this.pStream.print("begin " + this.mode + " ");
    if (this.bufferName != null)
      this.pStream.println(this.bufferName);
    else
      this.pStream.println("encoder.bin");
    this.pStream.flush();
  }

  protected void encodeBufferSuffix(OutputStream paramOutputStream)
    throws IOException
  {
    this.pStream.println(" \nend");
    this.pStream.flush();
  }
}
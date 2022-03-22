package sun.net.httpserver;

import java.io.IOException;
import java.io.InputStream;

class ChunkedInputStream extends LeftOverInputStream
{
  private int remaining;
  private boolean needToReadHeader = true;
  static char CR;
  static char LF;

  ChunkedInputStream(ExchangeImpl paramExchangeImpl, InputStream paramInputStream)
  {
    super(paramExchangeImpl, paramInputStream);
  }

  private int numeric(char[] paramArrayOfChar, int paramInt)
    throws IOException
  {
    if ((!($assertionsDisabled)) && (paramArrayOfChar.length < paramInt))
      throw new AssertionError();
    int i = 0;
    for (int j = 0; j < paramInt; ++j)
    {
      int k = paramArrayOfChar[j];
      int l = 0;
      if ((k >= 48) && (k <= 57))
        l = k - 48;
      else if ((k >= 97) && (k <= 102))
        l = k - 97 + 10;
      else if ((k >= 65) && (k <= 70))
        l = k - 65 + 10;
      else
        throw new IOException("invalid chunk length");
      i = i * 16 + l;
    }
    return i;
  }

  private int readChunkHeader()
    throws IOException
  {
    int i = 0;
    char[] arrayOfChar = new char[16];
    int k = 0;
    int l = 0;
    while (true)
    {
      int j;
      do
        while (true)
        {
          while (true)
          {
            while (true)
            {
              do
              {
                if ((j = (char)this.in.read()) == '￿)
                  break label127;
                if (k == arrayOfChar.length - 1)
                  throw new IOException("invalid chunk header");
                if (i == 0)
                  break label87;
                if (j == LF)
                {
                  int i1 = numeric(arrayOfChar, k);
                  return i1;
                }
                i = 0;
              }
              while (l != 0);
              arrayOfChar[(k++)] = j;
            }
            label87: if (j != CR)
              break;
            i = 1;
          }
          if (j != 59)
            break;
          l = 1;
        }
      while (l != 0);
      arrayOfChar[(k++)] = j;
    }
    label127: throw new IOException("end of stream reading chunk header");
  }

  protected int readImpl(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (this.eof)
      return -1;
    if (this.needToReadHeader)
    {
      this.remaining = readChunkHeader();
      if (this.remaining == 0)
      {
        this.eof = true;
        consumeCRLF();
        return -1;
      }
      this.needToReadHeader = false;
    }
    if (paramInt2 > this.remaining)
      paramInt2 = this.remaining;
    int i = this.in.read(paramArrayOfByte, paramInt1, paramInt2);
    if (i > -1)
      this.remaining -= i;
    if (this.remaining == 0)
    {
      this.needToReadHeader = true;
      consumeCRLF();
    }
    return i;
  }

  private void consumeCRLF()
    throws IOException
  {
    int i = (char)this.in.read();
    if (i != CR)
      throw new IOException("invalid chunk end");
    i = (char)this.in.read();
    if (i != LF)
      throw new IOException("invalid chunk end");
  }

  public int available()
    throws IOException
  {
    if ((this.eof) || (this.closed))
      return 0;
    int i = this.in.available();
    return ((i > this.remaining) ? this.remaining : i);
  }

  public boolean isDataBuffered()
    throws IOException
  {
    if ((!($assertionsDisabled)) && (!(this.eof)))
      throw new AssertionError();
    return (this.in.available() > 0);
  }

  public boolean markSupported()
  {
    return false;
  }

  public void mark(int paramInt)
  {
  }

  public void reset()
    throws IOException
  {
    throw new IOException("mark/reset not supported");
  }

  static
  {
    CR = '\r';
    LF = '\n';
  }
}
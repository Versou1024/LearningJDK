package sun.awt.windows;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class HTMLDecodingInputStream extends InputStream
{
  private final BufferedInputStream bufferedStream;
  private boolean descriptionParsed = false;
  private boolean closed = false;
  private int index;
  private int end;
  public static final int BYTE_BUFFER_LEN = 8192;
  public static final int CHAR_BUFFER_LEN = 2730;
  private static final String FAILURE_MSG = "Unable to parse HTML description: ";
  private static final String INVALID_MSG = " invalid";

  public HTMLDecodingInputStream(InputStream paramInputStream)
    throws IOException
  {
    this.bufferedStream = new BufferedInputStream(paramInputStream, 8192);
  }

  private void parseDescription()
    throws IOException
  {
    int j;
    int k;
    int l;
    this.bufferedStream.mark(8192);
    BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(this.bufferedStream, "UTF-8"), 2730);
    String str1 = localBufferedReader.readLine().trim();
    if ((str1 == null) || (!(str1.startsWith("Version:"))))
    {
      this.index = 0;
      this.end = -1;
      this.bufferedStream.reset();
      return;
    }
    int i = j = k = l = 0;
    try
    {
      do
      {
        int i1;
        String str2 = localBufferedReader.readLine().trim();
        if (str2 == null)
        {
          close();
          throw new IOException("Unable to parse HTML description: ");
        }
        if (str2.startsWith("StartHTML:"))
        {
          i1 = Integer.parseInt(str2.substring("StartHTML:".length(), str2.length()).trim());
          if (i1 >= 0)
          {
            this.index = i1;
            i = 1;
          }
          else if (i1 != -1)
          {
            close();
            throw new IOException("Unable to parse HTML description: StartHTML: invalid");
          }
        }
        else if (str2.startsWith("EndHTML:"))
        {
          i1 = Integer.parseInt(str2.substring("EndHTML:".length(), str2.length()).trim());
          if (i1 >= 0)
          {
            this.end = i1;
            j = 1;
          }
          else if (i1 != -1)
          {
            close();
            throw new IOException("Unable to parse HTML description: EndHTML: invalid");
          }
        }
        else if ((i == 0) && (j == 0) && (str2.startsWith("StartFragment:")))
        {
          this.index = Integer.parseInt(str2.substring("StartFragment:".length(), str2.length()).trim());
          if (this.index < 0)
          {
            close();
            throw new IOException("Unable to parse HTML description: StartFragment: invalid");
          }
          k = 1;
        }
        else if ((i == 0) && (j == 0) && (str2.startsWith("EndFragment:")))
        {
          this.end = Integer.parseInt(str2.substring("EndFragment:".length(), str2.length()).trim());
          if (this.end < 0)
          {
            close();
            throw new IOException("Unable to parse HTML description: EndFragment: invalid");
          }
          l = 1;
        }
        if ((i != 0) && (j != 0))
          break;
      }
      while ((k == 0) || (l == 0));
    }
    catch (NumberFormatException localNumberFormatException)
    {
      close();
      throw new IOException("Unable to parse HTML description: " + localNumberFormatException);
    }
    this.bufferedStream.reset();
    for (int i2 = 0; i2 < this.index; ++i2)
      if (this.bufferedStream.read() == -1)
      {
        close();
        throw new IOException("Unable to parse HTML description: Byte stream ends in description.");
      }
  }

  public int read()
    throws IOException
  {
    if (this.closed)
      throw new IOException("Stream closed");
    if (!(this.descriptionParsed))
    {
      parseDescription();
      this.descriptionParsed = true;
    }
    if ((this.end != -1) && (this.index >= this.end))
      return -1;
    int i = this.bufferedStream.read();
    if (i == -1)
    {
      this.index = (this.end = 0);
      return -1;
    }
    this.index += 1;
    return i;
  }

  public void close()
    throws IOException
  {
    if (!(this.closed))
    {
      this.closed = true;
      this.bufferedStream.close();
    }
  }
}
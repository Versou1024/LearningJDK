package sun.net.smtp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

class SmtpPrintStream extends PrintStream
{
  private SmtpClient target;
  private int lastc = 10;

  SmtpPrintStream(OutputStream paramOutputStream, SmtpClient paramSmtpClient)
    throws UnsupportedEncodingException
  {
    super(paramOutputStream, false, paramSmtpClient.getEncoding());
    this.target = paramSmtpClient;
  }

  public void close()
  {
    if (this.target == null)
      return;
    if (this.lastc != 10)
      write(10);
    try
    {
      this.target.issueCommand(".\r\n", 250);
      this.target.message = null;
      this.out = null;
      this.target = null;
    }
    catch (IOException localIOException)
    {
    }
  }

  public void write(int paramInt)
  {
    try
    {
      if ((this.lastc == 10) && (paramInt == 46))
        this.out.write(46);
      if ((paramInt == 10) && (this.lastc != 13))
        this.out.write(13);
      this.out.write(paramInt);
      this.lastc = paramInt;
    }
    catch (IOException localIOException)
    {
    }
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    int i;
    try
    {
      for (i = this.lastc; --paramInt2 >= 0; i = j)
      {
        int j = paramArrayOfByte[(paramInt1++)];
        if ((i == 10) && (j == 46))
          this.out.write(46);
        if ((j == 10) && (i != 13))
          this.out.write(13);
        this.out.write(j);
      }
      this.lastc = i;
    }
    catch (IOException localIOException)
    {
    }
  }

  public void print(String paramString)
  {
    int i = paramString.length();
    for (int j = 0; j < i; ++j)
      write(paramString.charAt(j));
  }
}
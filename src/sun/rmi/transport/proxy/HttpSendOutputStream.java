package sun.rmi.transport.proxy;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class HttpSendOutputStream extends FilterOutputStream
{
  HttpSendSocket owner;

  public HttpSendOutputStream(OutputStream paramOutputStream, HttpSendSocket paramHttpSendSocket)
    throws IOException
  {
    super(paramOutputStream);
    this.owner = paramHttpSendSocket;
  }

  public void deactivate()
  {
    this.out = null;
  }

  public void write(int paramInt)
    throws IOException
  {
    if (this.out == null)
      this.out = this.owner.writeNotify();
    this.out.write(paramInt);
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (paramInt2 == 0)
      return;
    if (this.out == null)
      this.out = this.owner.writeNotify();
    this.out.write(paramArrayOfByte, paramInt1, paramInt2);
  }

  public void flush()
    throws IOException
  {
    if (this.out != null)
      this.out.flush();
  }

  public void close()
    throws IOException
  {
    flush();
    this.owner.close();
  }
}
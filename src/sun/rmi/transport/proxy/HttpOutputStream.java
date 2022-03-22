package sun.rmi.transport.proxy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class HttpOutputStream extends ByteArrayOutputStream
{
  protected OutputStream out;
  boolean responseSent = false;
  private static byte[] emptyData = { 0 };

  public HttpOutputStream(OutputStream paramOutputStream)
  {
    this.out = paramOutputStream;
  }

  public synchronized void close()
    throws IOException
  {
    if (!(this.responseSent))
    {
      if (size() == 0)
        write(emptyData);
      DataOutputStream localDataOutputStream = new DataOutputStream(this.out);
      localDataOutputStream.writeBytes("Content-type: application/octet-stream\r\n");
      localDataOutputStream.writeBytes("Content-length: " + size() + "\r\n");
      localDataOutputStream.writeBytes("\r\n");
      writeTo(localDataOutputStream);
      localDataOutputStream.flush();
      reset();
      this.responseSent = true;
    }
  }
}
package sun.misc;

import B;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.jar.Manifest;
import sun.nio.ByteBuffered;

public abstract class Resource
{
  private InputStream cis;

  public abstract String getName();

  public abstract URL getURL();

  public abstract URL getCodeSourceURL();

  public abstract InputStream getInputStream()
    throws IOException;

  public abstract int getContentLength()
    throws IOException;

  private synchronized InputStream cachedInputStream()
    throws IOException
  {
    if (this.cis == null)
      this.cis = getInputStream();
    return this.cis;
  }

  public byte[] getBytes()
    throws IOException
  {
    Object localObject1;
    int i;
    InputStream localInputStream = cachedInputStream();
    boolean bool = Thread.interrupted();
    try
    {
      i = getContentLength();
    }
    catch (InterruptedIOException localInterruptedIOException1)
    {
      while (true)
      {
        Thread.interrupted();
        bool = true;
      }
    }
    try
    {
      byte[] arrayOfByte;
      if (i != -1)
      {
        localObject1 = new byte[i];
        while (true)
        {
          if (i <= 0)
            break label209;
          j = 0;
          try
          {
            j = localInputStream.read(localObject1, localObject1.length - i, i);
          }
          catch (InterruptedIOException localInterruptedIOException2)
          {
            Thread.interrupted();
            bool = true;
          }
          if (j == -1)
            throw new IOException("unexpected EOF");
          i -= j;
        }
      }
      localObject1 = new byte[1024];
      int j = 0;
      while (true)
      {
        do
        {
          i = 0;
          try
          {
            i = localInputStream.read(localObject1, j, localObject1.length - j);
            if (i == -1)
              break label183:
          }
          catch (InterruptedIOException localInterruptedIOException3)
          {
            Thread.interrupted();
            bool = true;
          }
          j += i;
        }
        while (j < localObject1.length);
        arrayOfByte = new byte[j * 2];
        System.arraycopy(localObject1, 0, arrayOfByte, 0, j);
        localObject1 = arrayOfByte;
      }
      if (j != localObject1.length)
      {
        label183: arrayOfByte = new byte[j];
        System.arraycopy(localObject1, 0, arrayOfByte, 0, j);
        label209: localObject1 = arrayOfByte;
      }
    }
    finally
    {
      try
      {
        localInputStream.close();
      }
      catch (InterruptedIOException localInterruptedIOException4)
      {
        bool = true;
      }
      catch (IOException localIOException)
      {
      }
      if (bool)
        Thread.currentThread().interrupt();
    }
    return ((B)localObject1);
  }

  public ByteBuffer getByteBuffer()
    throws IOException
  {
    InputStream localInputStream = cachedInputStream();
    if (localInputStream instanceof ByteBuffered)
      return ((ByteBuffered)localInputStream).getByteBuffer();
    return null;
  }

  public Manifest getManifest()
    throws IOException
  {
    return null;
  }

  public Certificate[] getCertificates()
  {
    return null;
  }

  public CodeSigner[] getCodeSigners()
  {
    return null;
  }
}
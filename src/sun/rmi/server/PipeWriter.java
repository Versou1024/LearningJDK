package sun.rmi.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.AccessController;
import java.util.Date;
import sun.rmi.runtime.NewThreadAction;
import sun.security.action.GetPropertyAction;

class PipeWriter
  implements Runnable
{
  private ByteArrayOutputStream bufOut;
  private int cLast;
  private byte[] currSep;
  private PrintWriter out;
  private InputStream in;
  private String pipeString;
  private String execString;
  private static String lineSeparator;
  private static int lineSeparatorLength;
  private static int numExecs = 0;

  private PipeWriter(InputStream paramInputStream, OutputStream paramOutputStream, String paramString, int paramInt)
  {
    this.in = paramInputStream;
    this.out = new PrintWriter(paramOutputStream);
    this.bufOut = new ByteArrayOutputStream();
    this.currSep = new byte[lineSeparatorLength];
    this.execString = ":ExecGroup-" + Integer.toString(paramInt) + ':' + paramString + ':';
  }

  public void run()
  {
    byte[] arrayOfByte = new byte[256];
    try
    {
      while ((i = this.in.read(arrayOfByte)) != -1)
      {
        int i;
        write(arrayOfByte, 0, i);
      }
      String str = this.bufOut.toString();
      this.bufOut.reset();
      if (str.length() > 0)
      {
        this.out.println(createAnnotation() + str);
        this.out.flush();
      }
    }
    catch (IOException localIOException)
    {
    }
  }

  private void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (paramInt2 < 0)
      throw new ArrayIndexOutOfBoundsException(paramInt2);
    for (int i = 0; i < paramInt2; ++i)
      write(paramArrayOfByte[(paramInt1 + i)]);
  }

  private void write(byte paramByte)
    throws IOException
  {
    int i = 0;
    for (i = 1; i < this.currSep.length; ++i)
      this.currSep[(i - 1)] = this.currSep[i];
    this.currSep[(i - 1)] = paramByte;
    this.bufOut.write(paramByte);
    if ((this.cLast >= lineSeparatorLength - 1) && (lineSeparator.equals(new String(this.currSep))))
    {
      this.cLast = 0;
      this.out.print(createAnnotation() + this.bufOut.toString());
      this.out.flush();
      this.bufOut.reset();
      if (!(this.out.checkError()))
        return;
      throw new IOException("PipeWriter: IO Exception when writing to output stream.");
    }
    this.cLast += 1;
  }

  private String createAnnotation()
  {
    return new Date().toString() + this.execString;
  }

  static void plugTogetherPair(InputStream paramInputStream1, OutputStream paramOutputStream1, InputStream paramInputStream2, OutputStream paramOutputStream2)
  {
    Thread localThread1 = null;
    Thread localThread2 = null;
    int i = getNumExec();
    localThread1 = (Thread)AccessController.doPrivileged(new NewThreadAction(new PipeWriter(paramInputStream1, paramOutputStream1, "out", i), "out", true));
    localThread2 = (Thread)AccessController.doPrivileged(new NewThreadAction(new PipeWriter(paramInputStream2, paramOutputStream2, "err", i), "err", true));
    localThread1.start();
    localThread2.start();
  }

  private static synchronized int getNumExec()
  {
    return (numExecs++);
  }

  static
  {
    lineSeparator = (String)AccessController.doPrivileged(new GetPropertyAction("line.separator"));
    lineSeparatorLength = lineSeparator.length();
  }
}
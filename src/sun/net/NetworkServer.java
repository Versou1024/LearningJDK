package sun.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkServer
  implements Runnable, Cloneable
{
  public Socket clientSocket = null;
  private Thread serverInstance;
  private ServerSocket serverSocket;
  public PrintStream clientOutput;
  public InputStream clientInput;

  public void close()
    throws IOException
  {
    this.clientSocket.close();
    this.clientSocket = null;
    this.clientInput = null;
    this.clientOutput = null;
  }

  public boolean clientIsOpen()
  {
    return (this.clientSocket != null);
  }

  public final void run()
  {
    if (this.serverSocket != null)
    {
      Thread.currentThread().setPriority(10);
      try
      {
        Socket localSocket = this.serverSocket.accept();
        NetworkServer localNetworkServer = (NetworkServer)clone();
        localNetworkServer.serverSocket = null;
        localNetworkServer.clientSocket = localSocket;
        new Thread(localNetworkServer).start();
      }
      catch (Exception localException1)
      {
        System.out.print("Server failure\n");
        localException1.printStackTrace();
        try
        {
          this.serverSocket.close();
        }
        catch (IOException localIOException2)
        {
        }
        System.out.print("cs=" + this.serverSocket + "\n");
      }
    }
    else
    {
      try
      {
        this.clientOutput = new PrintStream(new BufferedOutputStream(this.clientSocket.getOutputStream()), false, "ISO8859_1");
        this.clientInput = new BufferedInputStream(this.clientSocket.getInputStream());
        serviceRequest();
      }
      catch (Exception localException2)
      {
      }
      try
      {
        close();
      }
      catch (IOException localIOException1)
      {
      }
    }
  }

  public final void startServer(int paramInt)
    throws IOException
  {
    this.serverSocket = new ServerSocket(paramInt, 50);
    this.serverInstance = new Thread(this);
    this.serverInstance.start();
  }

  public void serviceRequest()
    throws IOException
  {
    byte[] arrayOfByte = new byte[300];
    this.clientOutput.print("Echo server " + super.getClass().getName() + "\n");
    this.clientOutput.flush();
    while ((i = this.clientInput.read(arrayOfByte, 0, arrayOfByte.length)) >= 0)
    {
      int i;
      this.clientOutput.write(arrayOfByte, 0, i);
    }
  }

  public static void main(String[] paramArrayOfString)
  {
    try
    {
      new NetworkServer().startServer(8888);
    }
    catch (IOException localIOException)
    {
      System.out.print("Server failed: " + localIOException + "\n");
    }
  }

  public Object clone()
  {
    try
    {
      return super.clone();
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      throw new InternalError();
    }
  }
}
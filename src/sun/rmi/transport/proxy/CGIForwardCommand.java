package sun.rmi.transport.proxy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

final class CGIForwardCommand
  implements CGICommandHandler
{
  public String getName()
  {
    return "forward";
  }

  public void execute(String paramString)
    throws sun.rmi.transport.proxy.CGIClientException, sun.rmi.transport.proxy.CGIServerException
  {
    int i;
    Socket localSocket;
    DataInputStream localDataInputStream2;
    String str2;
    if (!(CGIHandler.RequestMethod.equals("POST")))
      throw new sun.rmi.transport.proxy.CGIClientException("can only forward POST requests");
    try
    {
      i = Integer.parseInt(paramString);
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new sun.rmi.transport.proxy.CGIClientException("invalid port number: " + paramString);
    }
    if ((i <= 0) || (i > 65535))
      throw new sun.rmi.transport.proxy.CGIClientException("invalid port: " + i);
    if (i < 1024)
      throw new sun.rmi.transport.proxy.CGIClientException("permission denied for port: " + i);
    try
    {
      localSocket = new Socket(InetAddress.getLocalHost(), i);
    }
    catch (IOException localIOException1)
    {
      throw new sun.rmi.transport.proxy.CGIServerException("could not connect to local port");
    }
    DataInputStream localDataInputStream1 = new DataInputStream(System.in);
    byte[] arrayOfByte = new byte[CGIHandler.ContentLength];
    try
    {
      localDataInputStream1.readFully(arrayOfByte);
    }
    catch (EOFException localEOFException1)
    {
      throw new sun.rmi.transport.proxy.CGIClientException("unexpected EOF reading request body");
    }
    catch (IOException localIOException2)
    {
      throw new sun.rmi.transport.proxy.CGIClientException("error reading request body");
    }
    try
    {
      DataOutputStream localDataOutputStream = new DataOutputStream(localSocket.getOutputStream());
      localDataOutputStream.writeBytes("POST / HTTP/1.0\r\n");
      localDataOutputStream.writeBytes("Content-length: " + CGIHandler.ContentLength + "\r\n\r\n");
      localDataOutputStream.write(arrayOfByte);
      localDataOutputStream.flush();
    }
    catch (IOException localIOException3)
    {
      throw new sun.rmi.transport.proxy.CGIServerException("error writing to server");
    }
    try
    {
      localDataInputStream2 = new DataInputStream(localSocket.getInputStream());
    }
    catch (IOException localIOException4)
    {
      throw new sun.rmi.transport.proxy.CGIServerException("error reading from server");
    }
    String str1 = "Content-length:".toLowerCase();
    int j = 0;
    int k = -1;
    do
    {
      try
      {
        str2 = localDataInputStream2.readLine();
      }
      catch (IOException localIOException5)
      {
        throw new sun.rmi.transport.proxy.CGIServerException("error reading from server");
      }
      if (str2 == null)
        throw new sun.rmi.transport.proxy.CGIServerException("unexpected EOF reading server response");
      if (str2.toLowerCase().startsWith(str1))
      {
        if (j != 0);
        k = Integer.parseInt(str2.substring(str1.length()).trim());
        j = 1;
      }
    }
    while ((str2.length() != 0) && (str2.charAt(0) != '\r') && (str2.charAt(0) != '\n'));
    if ((j == 0) || (k < 0))
      throw new sun.rmi.transport.proxy.CGIServerException("missing or invalid content length in server response");
    arrayOfByte = new byte[k];
    try
    {
      localDataInputStream2.readFully(arrayOfByte);
    }
    catch (EOFException localEOFException2)
    {
      throw new sun.rmi.transport.proxy.CGIServerException("unexpected EOF reading server response");
    }
    catch (IOException localIOException6)
    {
      throw new sun.rmi.transport.proxy.CGIServerException("error reading from server");
    }
    System.out.println("Status: 200 OK");
    System.out.println("Content-type: application/octet-stream");
    System.out.println("");
    try
    {
      System.out.write(arrayOfByte);
    }
    catch (IOException localIOException7)
    {
      throw new sun.rmi.transport.proxy.CGIServerException("error writing response");
    }
    System.out.flush();
  }
}
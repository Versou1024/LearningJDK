package sun.security.krb5.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class TCPClient
{
  private Socket tcpSocket;
  private BufferedOutputStream out;
  private BufferedInputStream in;

  public TCPClient(String paramString, int paramInt)
    throws IOException
  {
    this.tcpSocket = new Socket(paramString, paramInt);
    this.out = new BufferedOutputStream(this.tcpSocket.getOutputStream());
    this.in = new BufferedInputStream(this.tcpSocket.getInputStream());
  }

  public void send(byte[] paramArrayOfByte)
    throws IOException
  {
    byte[] arrayOfByte = new byte[4];
    intToNetworkByteOrder(paramArrayOfByte.length, arrayOfByte, 0, 4);
    this.out.write(arrayOfByte);
    this.out.write(paramArrayOfByte);
    this.out.flush();
  }

  public byte[] receive()
    throws IOException
  {
    byte[] arrayOfByte1 = new byte[4];
    int i = readFully(arrayOfByte1, 4);
    if (i != 4)
    {
      if (Krb5.DEBUG)
        System.out.println(">>>DEBUG: TCPClient could not read length field");
      return null;
    }
    int j = networkByteOrderToInt(arrayOfByte1, 0, 4);
    if (Krb5.DEBUG)
      System.out.println(">>>DEBUG: TCPClient reading " + j + " bytes");
    if (j <= 0)
    {
      if (Krb5.DEBUG)
        System.out.println(">>>DEBUG: TCPClient zero or negative length field: " + j);
      return null;
    }
    byte[] arrayOfByte2 = new byte[j];
    i = readFully(arrayOfByte2, j);
    if (i != j)
    {
      if (Krb5.DEBUG)
        System.out.println(">>>DEBUG: TCPClient could not read complete packet (" + j + "/" + i + ")");
      return null;
    }
    return arrayOfByte2;
  }

  public void close()
    throws IOException
  {
    this.tcpSocket.close();
  }

  private int readFully(byte[] paramArrayOfByte, int paramInt)
    throws IOException
  {
    int j = 0;
    while (paramInt > 0)
    {
      int i = this.in.read(paramArrayOfByte, j, paramInt);
      if (i == -1)
        return ((j == 0) ? -1 : j);
      j += i;
      paramInt -= i;
    }
    return j;
  }

  private static final int networkByteOrderToInt(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (paramInt2 > 4)
      throw new IllegalArgumentException("Cannot handle more than 4 bytes");
    int i = 0;
    for (int j = 0; j < paramInt2; ++j)
    {
      i <<= 8;
      i |= paramArrayOfByte[(paramInt1 + j)] & 0xFF;
    }
    return i;
  }

  private static final void intToNetworkByteOrder(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
  {
    if (paramInt3 > 4)
      throw new IllegalArgumentException("Cannot handle more than 4 bytes");
    for (int i = paramInt3 - 1; i >= 0; --i)
    {
      paramArrayOfByte[(paramInt2 + i)] = (byte)(paramInt1 & 0xFF);
      paramInt1 >>>= 8;
    }
  }
}
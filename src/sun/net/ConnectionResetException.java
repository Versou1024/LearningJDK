package sun.net;

import java.net.SocketException;

public class ConnectionResetException extends SocketException
{
  public ConnectionResetException(String paramString)
  {
    super(paramString);
  }

  public ConnectionResetException()
  {
  }
}
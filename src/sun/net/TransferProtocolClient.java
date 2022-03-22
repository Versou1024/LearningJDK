package sun.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;

public class TransferProtocolClient extends NetworkClient
{
  static final boolean debug = 0;
  protected Vector serverResponse = new Vector(1);
  protected int lastReplyCode;

  public int readServerResponse()
    throws IOException
  {
    label20: int k;
    StringBuffer localStringBuffer = new StringBuffer(32);
    int j = -1;
    this.serverResponse.setSize(0);
    while (true)
    {
      String str;
      while (true)
      {
        do
        {
          int i;
          do
          {
            if ((i = this.serverInput.read()) == -1)
              break;
            if (i == 13)
              if ((i = this.serverInput.read()) != 10)
                localStringBuffer.append('\r');
            localStringBuffer.append((char)i);
          }
          while (i != 10);
          str = localStringBuffer.toString();
          localStringBuffer.setLength(0);
          if (str.length() == 0)
            k = -1;
          else
            try
            {
              k = Integer.parseInt(str.substring(0, 3));
            }
            catch (NumberFormatException localNumberFormatException)
            {
              k = -1;
            }
            catch (StringIndexOutOfBoundsException localStringIndexOutOfBoundsException)
            {
              break label20:
            }
          this.serverResponse.addElement(str);
          if (j == -1)
            break label177;
        }
        while (k != j);
        if ((str.length() < 4) || (str.charAt(3) != '-'))
          break;
      }
      j = -1;
      break;
      label177: if ((str.length() < 4) || (str.charAt(3) != '-'))
        break;
      j = k;
    }
    return (this.lastReplyCode = k);
  }

  public void sendServer(String paramString)
  {
    this.serverOutput.print(paramString);
  }

  public String getResponseString()
  {
    return ((String)this.serverResponse.elementAt(0));
  }

  public Vector getResponseStrings()
  {
    return this.serverResponse;
  }

  public TransferProtocolClient(String paramString, int paramInt)
    throws IOException
  {
    super(paramString, paramInt);
  }

  public TransferProtocolClient()
  {
  }
}
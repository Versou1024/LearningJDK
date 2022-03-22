package sun.net.smtp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.AccessController;
import sun.net.TransferProtocolClient;
import sun.security.action.GetPropertyAction;

public class SmtpClient extends TransferProtocolClient
{
  String mailhost;
  SmtpPrintStream message;

  public void closeServer()
    throws IOException
  {
    if (serverIsOpen())
    {
      closeMessage();
      issueCommand("QUIT\r\n", 221);
      super.closeServer();
    }
  }

  void issueCommand(String paramString, int paramInt)
    throws IOException
  {
    int i;
    sendServer(paramString);
    do
      if ((i = readServerResponse()) == paramInt)
        return;
    while (i == 220);
    throw new SmtpProtocolException(getResponseString());
  }

  private void toCanonical(String paramString)
    throws IOException
  {
    if (paramString.startsWith("<"))
      issueCommand("rcpt to: " + paramString + "\r\n", 250);
    else
      issueCommand("rcpt to: <" + paramString + ">\r\n", 250);
  }

  public void to(String paramString)
    throws IOException
  {
    int i = 0;
    int j = paramString.length();
    int k = 0;
    int l = 0;
    int i1 = 0;
    int i2 = 0;
    while (k < j)
    {
      int i3 = paramString.charAt(k);
      if (i1 > 0)
      {
        if (i3 == 40)
          ++i1;
        else if (i3 == 41)
          --i1;
        if (i1 == 0)
          if (l > i)
            i2 = 1;
          else
            i = k + 1;
      }
      else if (i3 == 40)
      {
        ++i1;
      }
      else if (i3 == 60)
      {
        i = ++k;
      }
      else if (i3 == 62)
      {
        i2 = 1;
      }
      else if (i3 == 44)
      {
        if (l > i)
          toCanonical(paramString.substring(i, l));
        i = k + 1;
        i2 = 0;
      }
      else if ((i3 > 32) && (i2 == 0))
      {
        l = k + 1;
      }
      else if (i == k)
      {
        ++i;
      }
      ++k;
    }
    if (l > i)
      toCanonical(paramString.substring(i, l));
  }

  public void from(String paramString)
    throws IOException
  {
    if (paramString.startsWith("<"))
      issueCommand("mail from: " + paramString + "\r\n", 250);
    else
      issueCommand("mail from: <" + paramString + ">\r\n", 250);
  }

  private void openServer(String paramString)
    throws IOException
  {
    this.mailhost = paramString;
    openServer(this.mailhost, 25);
    issueCommand("helo " + InetAddress.getLocalHost().getHostName() + "\r\n", 250);
  }

  public PrintStream startMessage()
    throws IOException
  {
    issueCommand("data\r\n", 354);
    try
    {
      this.message = new SmtpPrintStream(this.serverOutput, this);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new InternalError(encoding + " encoding not found");
    }
    return this.message;
  }

  void closeMessage()
    throws IOException
  {
    if (this.message != null)
      this.message.close();
  }

  public SmtpClient(String paramString)
    throws IOException
  {
    if (paramString != null);
    try
    {
      openServer(paramString);
      this.mailhost = paramString;
      return;
    }
    catch (Exception localException3)
    {
      try
      {
        this.mailhost = ((String)AccessController.doPrivileged(new GetPropertyAction("mail.host")));
        if (this.mailhost != null)
        {
          openServer(this.mailhost);
          return;
        }
      }
      catch (Exception localException2)
      {
      }
      try
      {
        this.mailhost = "localhost";
        openServer(this.mailhost);
      }
      catch (Exception localException3)
      {
        this.mailhost = "mailhost";
        openServer(this.mailhost);
      }
    }
  }

  public SmtpClient()
    throws IOException
  {
    this(null);
  }

  public SmtpClient(int paramInt)
    throws IOException
  {
    setConnectTimeout(paramInt);
    try
    {
      this.mailhost = ((String)AccessController.doPrivileged(new GetPropertyAction("mail.host")));
      if (this.mailhost != null)
      {
        openServer(this.mailhost);
        return;
      }
    }
    catch (Exception localException1)
    {
    }
    try
    {
      this.mailhost = "localhost";
      openServer(this.mailhost);
    }
    catch (Exception localException2)
    {
      this.mailhost = "mailhost";
      openServer(this.mailhost);
    }
  }

  public String getMailHost()
  {
    return this.mailhost;
  }

  String getEncoding()
  {
    return encoding;
  }
}
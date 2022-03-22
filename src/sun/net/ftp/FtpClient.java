package sun.net.ftp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.misc.REException;
import sun.misc.RegexpPool;
import sun.net.TelnetInputStream;
import sun.net.TelnetOutputStream;
import sun.net.TransferProtocolClient;
import sun.security.action.GetPropertyAction;

public class FtpClient extends TransferProtocolClient
{
  public static final int FTP_PORT = 21;
  static int FTP_SUCCESS;
  static int FTP_TRY_AGAIN;
  static int FTP_ERROR;
  private String serverName = null;
  private boolean replyPending = false;
  private boolean binaryMode = false;
  private boolean loggedIn = false;
  private static RegexpPool nonProxyHostsPool;
  private static String nonProxyHostsSource;
  String command;
  int lastReplyCode;
  public String welcomeMsg;

  public static boolean getUseFtpProxy()
  {
    return (getFtpProxyHost() != null);
  }

  public static String getFtpProxyHost()
  {
    return ((String)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        String str = System.getProperty("ftp.proxyHost");
        if (str == null)
          str = System.getProperty("ftpProxyHost");
        if ((str == null) && (Boolean.getBoolean("ftp.useProxy")))
          str = System.getProperty("proxyHost");
        return str;
      }
    }));
  }

  public static int getFtpProxyPort()
  {
    int[] arrayOfInt = { 80 };
    AccessController.doPrivileged(new PrivilegedAction(arrayOfInt)
    {
      public Object run()
      {
        String str = System.getProperty("ftp.proxyPort");
        if (str == null)
          str = System.getProperty("ftpProxyPort");
        if ((str == null) && (Boolean.getBoolean("ftp.useProxy")))
          str = System.getProperty("proxyPort");
        if (str != null)
          this.val$result[0] = Integer.parseInt(str);
        return null;
      }
    });
    return arrayOfInt[0];
  }

  public static boolean matchNonProxyHosts(String paramString)
  {
    synchronized (FtpClient.class)
    {
      String str = (String)AccessController.doPrivileged(new GetPropertyAction("ftp.nonProxyHosts"));
      if (str == null)
      {
        nonProxyHostsPool = null;
      }
      else if (!(str.equals(nonProxyHostsSource)))
      {
        RegexpPool localRegexpPool = new RegexpPool();
        StringTokenizer localStringTokenizer = new StringTokenizer(str, "|", false);
        try
        {
          while (localStringTokenizer.hasMoreTokens())
            localRegexpPool.add(localStringTokenizer.nextToken().toLowerCase(), Boolean.TRUE);
        }
        catch (REException localREException)
        {
          System.err.println("Error in http.nonProxyHosts system property: " + localREException);
        }
        nonProxyHostsPool = localRegexpPool;
      }
      nonProxyHostsSource = str;
    }
    if (nonProxyHostsPool == null)
      return false;
    return (nonProxyHostsPool.match(paramString) != null);
  }

  public void closeServer()
    throws IOException
  {
    if (serverIsOpen())
    {
      issueCommand("QUIT");
      super.closeServer();
    }
  }

  protected int issueCommand(String paramString)
    throws IOException
  {
    label37: int i;
    this.command = paramString;
    do
    {
      if (!(this.replyPending))
        break label37;
      this.replyPending = false;
    }
    while (readReply() != FTP_ERROR);
    throw new FtpProtocolException("Error reading FTP pending reply\n");
    do
    {
      sendServer(paramString + "\r\n");
      i = readReply();
    }
    while (i == FTP_TRY_AGAIN);
    return i;
  }

  protected void issueCommandCheck(String paramString)
    throws IOException
  {
    if (issueCommand(paramString) != FTP_SUCCESS)
      throw new FtpProtocolException(paramString + ":" + getResponseString());
  }

  protected int readReply()
    throws IOException
  {
    this.lastReplyCode = readServerResponse();
    switch (this.lastReplyCode / 100)
    {
    case 1:
      this.replyPending = true;
    case 2:
    case 3:
      return FTP_SUCCESS;
    case 5:
      if (this.lastReplyCode == 530)
      {
        if (!(this.loggedIn))
          throw new FtpLoginException("Not logged in");
        return FTP_ERROR;
      }
      if (this.lastReplyCode != 550)
        break label135;
      throw new FileNotFoundException(this.command + ": " + getResponseString());
    case 4:
    }
    label135: return FTP_ERROR;
  }

  protected Socket openPassiveDataConnection()
    throws IOException
  {
    String str1;
    int i;
    Object localObject;
    Matcher localMatcher;
    String str2;
    InetSocketAddress localInetSocketAddress = null;
    if (issueCommand("EPSV ALL") == FTP_SUCCESS)
    {
      if (issueCommand("EPSV") == FTP_ERROR)
        throw new FtpProtocolException("EPSV Failed: " + getResponseString());
      str1 = getResponseString();
      localObject = Pattern.compile("^229 .* \\(\\|\\|\\|(\\d+)\\|\\)");
      localMatcher = ((Pattern)localObject).matcher(str1);
      if (!(localMatcher.find()))
        throw new FtpProtocolException("EPSV failed : " + str1);
      str2 = localMatcher.group(1);
      i = Integer.parseInt(str2);
      InetAddress localInetAddress = this.serverSocket.getInetAddress();
      if (localInetAddress != null)
        localInetSocketAddress = new InetSocketAddress(localInetAddress, i);
      else
        localInetSocketAddress = InetSocketAddress.createUnresolved(this.serverName, i);
    }
    else
    {
      if (issueCommand("PASV") == FTP_ERROR)
        throw new FtpProtocolException("PASV failed: " + getResponseString());
      str1 = getResponseString();
      localObject = Pattern.compile("227 .* \\(?(\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}),(\\d{1,3}),(\\d{1,3})\\)?");
      localMatcher = ((Pattern)localObject).matcher(str1);
      if (!(localMatcher.find()))
        throw new FtpProtocolException("PASV failed : " + str1);
      i = Integer.parseInt(localMatcher.group(3)) + (Integer.parseInt(localMatcher.group(2)) << 8);
      str2 = localMatcher.group(1).replace(',', '.');
      localInetSocketAddress = new InetSocketAddress(str2, i);
    }
    if (this.proxy != null)
      if (this.proxy.type() == Proxy.Type.SOCKS)
        localObject = (Socket)AccessController.doPrivileged(new PrivilegedAction(this)
        {
          public Object run()
          {
            return new Socket(FtpClient.access$000(this.this$0));
          }
        });
      else
        localObject = new Socket(Proxy.NO_PROXY);
    else
      localObject = new Socket();
    if (this.connectTimeout >= 0)
      ((Socket)localObject).connect(localInetSocketAddress, this.connectTimeout);
    else if (defaultConnectTimeout > 0)
      ((Socket)localObject).connect(localInetSocketAddress, defaultConnectTimeout);
    else
      ((Socket)localObject).connect(localInetSocketAddress);
    if (this.readTimeout >= 0)
      ((Socket)localObject).setSoTimeout(this.readTimeout);
    else if (defaultSoTimeout > 0)
      ((Socket)localObject).setSoTimeout(defaultSoTimeout);
    return ((Socket)localObject);
  }

  protected Socket openDataConnection(String paramString)
    throws IOException
  {
    Socket localSocket = null;
    try
    {
      localSocket = openPassiveDataConnection();
    }
    catch (IOException localIOException1)
    {
      localSocket = null;
    }
    if (localSocket != null)
      try
      {
        if (issueCommand(paramString) == FTP_ERROR)
        {
          localSocket.close();
          throw new FtpProtocolException(getResponseString());
        }
        return localSocket;
      }
      catch (IOException localIOException2)
      {
        localSocket.close();
        throw localIOException2;
      }
    if ((!($assertionsDisabled)) && (localSocket != null))
      throw new AssertionError();
    if ((this.proxy != null) && (this.proxy.type() == Proxy.Type.SOCKS))
      throw new FtpProtocolException("Passive mode failed");
    ServerSocket localServerSocket = new ServerSocket(0, 1);
    try
    {
      InetAddress localInetAddress = localServerSocket.getInetAddress();
      if (localInetAddress.isAnyLocalAddress())
        localInetAddress = getLocalAddress();
      String str = "EPRT |" + ((localInetAddress instanceof java.net.Inet6Address) ? "2" : "1") + "|" + localInetAddress.getHostAddress() + "|" + localServerSocket.getLocalPort() + "|";
      if ((issueCommand(str) == FTP_ERROR) || (issueCommand(paramString) == FTP_ERROR))
      {
        FtpProtocolException localFtpProtocolException;
        str = "PORT ";
        byte[] arrayOfByte = localInetAddress.getAddress();
        for (int i = 0; i < arrayOfByte.length; ++i)
          str = str + (arrayOfByte[i] & 0xFF) + ",";
        str = str + (localServerSocket.getLocalPort() >>> 8 & 0xFF) + "," + (localServerSocket.getLocalPort() & 0xFF);
        if (issueCommand(str) == FTP_ERROR)
        {
          localFtpProtocolException = new FtpProtocolException("PORT :" + getResponseString());
          throw localFtpProtocolException;
        }
        if (issueCommand(paramString) == FTP_ERROR)
        {
          localFtpProtocolException = new FtpProtocolException(paramString + ":" + getResponseString());
          throw localFtpProtocolException;
        }
      }
      if (this.connectTimeout >= 0)
        localServerSocket.setSoTimeout(this.connectTimeout);
      else if (defaultConnectTimeout > 0)
        localServerSocket.setSoTimeout(defaultConnectTimeout);
      localSocket = localServerSocket.accept();
      if (this.readTimeout >= 0)
        localSocket.setSoTimeout(this.readTimeout);
      else if (defaultSoTimeout > 0)
        localSocket.setSoTimeout(defaultSoTimeout);
    }
    finally
    {
      localServerSocket.close();
    }
    return localSocket;
  }

  public void openServer(String paramString)
    throws IOException
  {
    openServer(paramString, 21);
  }

  public void openServer(String paramString, int paramInt)
    throws IOException
  {
    this.serverName = paramString;
    super.openServer(paramString, paramInt);
    if (readReply() == FTP_ERROR)
      throw new FtpProtocolException("Welcome message: " + getResponseString());
  }

  public void login(String paramString1, String paramString2)
    throws IOException
  {
    if (!(serverIsOpen()))
      throw new FtpLoginException("not connected to host");
    if ((paramString1 == null) || (paramString1.length() == 0))
      return;
    if (issueCommand("USER " + paramString1) == FTP_ERROR)
      throw new FtpLoginException("user " + paramString1 + " : " + getResponseString());
    if ((this.lastReplyCode == 331) && (((paramString2 == null) || (paramString2.length() == 0) || (issueCommand("PASS " + paramString2) == FTP_ERROR))))
      throw new FtpLoginException("password: " + getResponseString());
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < this.serverResponse.size(); ++i)
    {
      String str = (String)this.serverResponse.elementAt(i);
      if (str != null)
      {
        if ((str.length() >= 4) && (str.startsWith("230")))
          str = str.substring(4);
        localStringBuffer.append(str);
      }
    }
    this.welcomeMsg = localStringBuffer.toString();
    this.loggedIn = true;
  }

  public TelnetInputStream get(String paramString)
    throws IOException
  {
    Socket localSocket;
    try
    {
      localSocket = openDataConnection("RETR " + paramString);
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      if (paramString.indexOf(47) == -1)
        throw localFileNotFoundException;
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "/");
      String str = null;
      if (localStringTokenizer.hasMoreElements())
      {
        str = localStringTokenizer.nextToken();
        if (!(localStringTokenizer.hasMoreElements()))
          break label94:
        try
        {
          cd(str);
        }
        catch (FtpProtocolException localFtpProtocolException)
        {
          throw localFileNotFoundException;
        }
      }
      if (str != null)
        label94: localSocket = openDataConnection("RETR " + str);
      else
        throw localFileNotFoundException;
    }
    return new TelnetInputStream(localSocket.getInputStream(), this.binaryMode);
  }

  public TelnetOutputStream put(String paramString)
    throws IOException
  {
    Socket localSocket = openDataConnection("STOR " + paramString);
    TelnetOutputStream localTelnetOutputStream = new TelnetOutputStream(localSocket.getOutputStream(), this.binaryMode);
    if (!(this.binaryMode))
      localTelnetOutputStream.setStickyCRLF(true);
    return localTelnetOutputStream;
  }

  public TelnetOutputStream append(String paramString)
    throws IOException
  {
    Socket localSocket = openDataConnection("APPE " + paramString);
    TelnetOutputStream localTelnetOutputStream = new TelnetOutputStream(localSocket.getOutputStream(), this.binaryMode);
    if (!(this.binaryMode))
      localTelnetOutputStream.setStickyCRLF(true);
    return localTelnetOutputStream;
  }

  public TelnetInputStream list()
    throws IOException
  {
    Socket localSocket = openDataConnection("LIST");
    return new TelnetInputStream(localSocket.getInputStream(), this.binaryMode);
  }

  public TelnetInputStream nameList(String paramString)
    throws IOException
  {
    Socket localSocket;
    if (paramString != null)
      localSocket = openDataConnection("NLST " + paramString);
    else
      localSocket = openDataConnection("NLST");
    return new TelnetInputStream(localSocket.getInputStream(), this.binaryMode);
  }

  public void cd(String paramString)
    throws IOException
  {
    if ((paramString == null) || ("".equals(paramString)))
      return;
    issueCommandCheck("CWD " + paramString);
  }

  public void cdUp()
    throws IOException
  {
    issueCommandCheck("CDUP");
  }

  public String pwd()
    throws IOException
  {
    issueCommandCheck("PWD");
    String str = getResponseString();
    if (!(str.startsWith("257")))
      throw new FtpProtocolException("PWD failed. " + str);
    return str.substring(5, str.lastIndexOf(34));
  }

  public void binary()
    throws IOException
  {
    issueCommandCheck("TYPE I");
    this.binaryMode = true;
  }

  public void ascii()
    throws IOException
  {
    issueCommandCheck("TYPE A");
    this.binaryMode = false;
  }

  public void rename(String paramString1, String paramString2)
    throws IOException
  {
    issueCommandCheck("RNFR " + paramString1);
    issueCommandCheck("RNTO " + paramString2);
  }

  public String system()
    throws IOException
  {
    issueCommandCheck("SYST");
    String str = getResponseString();
    if (!(str.startsWith("215")))
      throw new FtpProtocolException("SYST failed." + str);
    return str.substring(4);
  }

  public void noop()
    throws IOException
  {
    issueCommandCheck("NOOP");
  }

  public void reInit()
    throws IOException
  {
    issueCommandCheck("REIN");
    this.loggedIn = false;
  }

  public FtpClient(String paramString)
    throws IOException
  {
    openServer(paramString, 21);
  }

  public FtpClient(String paramString, int paramInt)
    throws IOException
  {
    openServer(paramString, paramInt);
  }

  public FtpClient()
  {
  }

  public FtpClient(Proxy paramProxy)
  {
    this.proxy = paramProxy;
  }

  protected void finalize()
    throws IOException
  {
    if (serverIsOpen())
      super.closeServer();
  }

  static
  {
    FTP_SUCCESS = 1;
    FTP_TRY_AGAIN = 2;
    FTP_ERROR = 3;
    nonProxyHostsPool = null;
    nonProxyHostsSource = null;
  }
}
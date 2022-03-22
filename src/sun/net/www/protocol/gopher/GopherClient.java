package sun.net.www.protocol.gopher;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.AccessController;
import sun.net.NetworkClient;
import sun.net.www.MessageHeader;
import sun.net.www.URLConnection;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetPropertyAction;

public class GopherClient extends NetworkClient
  implements Runnable
{

  @Deprecated
  public static boolean useGopherProxy;

  @Deprecated
  public static String gopherProxyHost;

  @Deprecated
  public static int gopherProxyPort;
  PipedOutputStream os;
  URL u;
  int gtype;
  String gkey;
  URLConnection connection;

  GopherClient(URLConnection paramURLConnection)
  {
    this.connection = paramURLConnection;
  }

  public static boolean getUseGopherProxy()
  {
    return ((Boolean)AccessController.doPrivileged(new GetBooleanAction("gopherProxySet"))).booleanValue();
  }

  public static String getGopherProxyHost()
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("gopherProxyHost"));
    if ("".equals(str))
      str = null;
    return str;
  }

  public static int getGopherProxyPort()
  {
    return ((Integer)AccessController.doPrivileged(new GetIntegerAction("gopherProxyPort", 80))).intValue();
  }

  InputStream openStream(URL paramURL)
    throws IOException
  {
    this.u = paramURL;
    this.os = this.os;
    int i = 0;
    String str = paramURL.getFile();
    int j = str.length();
    int k = 49;
    while (i < j)
    {
      if ((k = str.charAt(i)) != '/')
        break;
      ++i;
    }
    this.gtype = ((k == 47) ? 49 : k);
    if (i < j);
    this.gkey = ???.substring(++i);
    openServer(paramURL.getHost(), (paramURL.getPort() <= 0) ? 70 : paramURL.getPort());
    MessageHeader localMessageHeader = new MessageHeader();
    switch (this.gtype)
    {
    case 48:
    case 55:
      localMessageHeader.add("content-type", "text/plain");
      break;
    case 49:
      localMessageHeader.add("content-type", "text/html");
      break;
    case 73:
    case 103:
      localMessageHeader.add("content-type", "image/gif");
      break;
    default:
      localMessageHeader.add("content-type", "content/unknown");
    }
    if (this.gtype != 55)
    {
      this.serverOutput.print(decodePercent(this.gkey) + "\r\n");
      this.serverOutput.flush();
    }
    else if ((i = this.gkey.indexOf(63)) >= 0)
    {
      this.serverOutput.print(decodePercent(this.gkey.substring(0, i) + "\t" + this.gkey.substring(i + 1) + "\r\n"));
      this.serverOutput.flush();
      localMessageHeader.add("content-type", "text/html");
    }
    else
    {
      localMessageHeader.add("content-type", "text/html");
    }
    this.connection.setProperties(localMessageHeader);
    if (localMessageHeader.findValue("content-type") == "text/html")
    {
      this.os = new PipedOutputStream();
      PipedInputStream localPipedInputStream = new PipedInputStream();
      localPipedInputStream.connect(this.os);
      new Thread(this).start();
      return localPipedInputStream;
    }
    return new GopherInputStream(this, this.serverInput);
  }

  private String decodePercent(String paramString)
  {
    if ((paramString == null) || (paramString.indexOf(37) < 0))
      return paramString;
    int i = paramString.length();
    char[] arrayOfChar = new char[i];
    int j = 0;
    for (int k = 0; k < i; ++k)
    {
      int l = paramString.charAt(k);
      if ((l == 37) && (k + 2 < i))
      {
        int i1 = paramString.charAt(k + 1);
        int i2 = paramString.charAt(k + 2);
        if ((48 <= i1) && (i1 <= 57))
          i1 -= 48;
        else if ((97 <= i1) && (i1 <= 102))
          i1 = i1 - 97 + 10;
        else if ((65 <= i1) && (i1 <= 70))
          i1 = i1 - 65 + 10;
        else
          i1 = -1;
        if ((48 <= i2) && (i2 <= 57))
          i2 -= 48;
        else if ((97 <= i2) && (i2 <= 102))
          i2 = i2 - 97 + 10;
        else if ((65 <= i2) && (i2 <= 70))
          i2 = i2 - 65 + 10;
        else
          i2 = -1;
        if ((i1 >= 0) && (i2 >= 0))
        {
          l = i1 << 4 | i2;
          k += 2;
        }
      }
      arrayOfChar[(j++)] = (char)l;
    }
    return new String(arrayOfChar, 0, j);
  }

  private String encodePercent(String paramString)
  {
    if (paramString == null)
      return paramString;
    int i = paramString.length();
    Object localObject = null;
    int j = 0;
    for (int k = 0; k < i; ++k)
    {
      int l = paramString.charAt(k);
      if ((l <= 32) || (l == 34) || (l == 37))
      {
        if (localObject == null)
          localObject = paramString.toCharArray();
        if (j + 3 >= localObject.length)
        {
          char[] arrayOfChar1 = new char[j + 10];
          System.arraycopy(localObject, 0, arrayOfChar1, 0, j);
          localObject = arrayOfChar1;
        }
        localObject[j] = 37;
        int i1 = l >> 4 & 0xF;
        localObject[(j + 1)] = (char)((i1 < 10) ? 48 + i1 : 55 + i1);
        i1 = l & 0xF;
        localObject[(j + 2)] = (char)((i1 < 10) ? 48 + i1 : 55 + i1);
        j += 3;
      }
      else
      {
        if (localObject != null)
        {
          if (j >= localObject.length)
          {
            char[] arrayOfChar2 = new char[j + 10];
            System.arraycopy(localObject, 0, arrayOfChar2, 0, j);
            localObject = arrayOfChar2;
          }
          localObject[j] = (char)l;
        }
        ++j;
      }
    }
    return ((String)new String(localObject, 0, j));
  }

  public void run()
  {
    int i = -1;
    try
    {
      Object localObject1;
      if (this.gtype == 55)
      {
        if ((i = this.gkey.indexOf(63)) < 0)
        {
          localObject1 = new PrintStream(this.os, false, encoding);
          ((PrintStream)localObject1).print("<html><head><title>Searchable Gopher Index</title></head>\n<body><h1>Searchable Gopher Index</h1><isindex>\n</body></html>\n");
        }
      }
      else if ((this.gtype != 49) && (this.gtype != 55))
      {
        localObject1 = new byte[2048];
        label652: 
        try
        {
          while ((j = this.serverInput.read(localObject1)) >= 0)
          {
            int j;
            this.os.write(localObject1, 0, j);
          }
        }
        catch (Exception localException)
        {
        }
      }
      else
      {
        localObject1 = new PrintStream(this.os, false, encoding);
        String str1 = null;
        if (this.gtype == 55)
          str1 = "Results of searching for \"" + this.gkey.substring(i + 1) + "\" on " + this.u.getHost();
        else
          str1 = "Gopher directory " + this.gkey + " from " + this.u.getHost();
        ((PrintStream)localObject1).print("<html><head><title>");
        ((PrintStream)localObject1).print(str1);
        ((PrintStream)localObject1).print("</title></head>\n<body>\n<H1>");
        ((PrintStream)localObject1).print(str1);
        ((PrintStream)localObject1).print("</h1><dl compact>\n");
        DataInputStream localDataInputStream = new DataInputStream(this.serverInput);
        while (true)
        {
          String str2;
          int k;
          int l;
          int i1;
          int i2;
          int i3;
          while (true)
          {
            while (true)
            {
              if ((str2 = localDataInputStream.readLine()) == null)
                break label652;
              for (k = str2.length(); (k > 0) && (str2.charAt(k - 1) <= ' '); --k);
              if (k > 0)
                break;
            }
            l = str2.charAt(0);
            i1 = str2.indexOf(9);
            i2 = (i1 > 0) ? str2.indexOf(9, i1 + 1) : -1;
            i3 = (i2 > 0) ? str2.indexOf(9, i2 + 1) : -1;
            if (i3 >= 0)
              break;
          }
          String str3 = (i3 + 1 < k) ? ":" + str2.substring(i3 + 1, k) : "";
          String str4 = (i2 + 1 < i3) ? str2.substring(i2 + 1, i3) : this.u.getHost();
          ((PrintStream)localObject1).print("<dt><a href=\"gopher://" + str4 + str3 + "/" + str2.substring(0, 1) + encodePercent(str2.substring(i1 + 1, i2)) + "\">\n");
          ((PrintStream)localObject1).print("<img align=middle border=0 width=25 height=32 src=");
          switch (l)
          {
          default:
            ((PrintStream)localObject1).print(System.getProperty("java.net.ftp.imagepath.file"));
            break;
          case 48:
            ((PrintStream)localObject1).print(System.getProperty("java.net.ftp.imagepath.text"));
            break;
          case 49:
            ((PrintStream)localObject1).print(System.getProperty("java.net.ftp.imagepath.directory"));
            break;
          case 103:
            ((PrintStream)localObject1).print(System.getProperty("java.net.ftp.imagepath.gif"));
          }
          ((PrintStream)localObject1).print(".gif align=middle><dd>\n");
          ((PrintStream)localObject1).print(str2.substring(1, i1) + "</a>\n");
        }
        ((PrintStream)localObject1).print("</dl></body>\n");
        ((PrintStream)localObject1).close();
      }
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    catch (IOException localIOException3)
    {
    }
    finally
    {
      try
      {
        closeServer();
        this.os.close();
      }
      catch (IOException localIOException4)
      {
      }
    }
  }

  static
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new GetBooleanAction("gopherProxySet"));
    useGopherProxy = localBoolean.booleanValue();
    gopherProxyHost = (String)AccessController.doPrivileged(new GetPropertyAction("gopherProxyHost"));
    gopherProxyPort = ((Integer)AccessController.doPrivileged(new GetIntegerAction("gopherProxyPort", 80))).intValue();
  }
}
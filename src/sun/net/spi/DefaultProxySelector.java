package sun.net.spi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.List<Ljava.net.Proxy;>;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.misc.REException;
import sun.misc.RegexpPool;
import sun.net.NetProperties;
import sun.security.action.LoadLibraryAction;

public class DefaultProxySelector extends ProxySelector
{
  static final String[][] props = { { "http", "http.proxy", "proxy", "socksProxy" }, { "https", "https.proxy", "proxy", "socksProxy" }, { "ftp", "ftp.proxy", "ftpProxy", "proxy", "socksProxy" }, { "gopher", "gopherProxy", "socksProxy" }, { "socket", "socksProxy" } };
  private static boolean hasSystemProxies = false;
  private static Properties defprops = new Properties();
  private static NonProxyInfo ftpNonProxyInfo;
  private static NonProxyInfo httpNonProxyInfo;
  private static final Pattern p6;
  private static final long L_DIGIT;
  private static final long H_DIGIT = 0L;
  private int number;

  public List<Proxy> select(URI paramURI)
  {
    if (paramURI == null)
      throw new IllegalArgumentException("URI can't be null.");
    String str1 = paramURI.getScheme();
    Object localObject1 = paramURI.getHost();
    int i = paramURI.getPort();
    if (localObject1 == null)
    {
      localObject2 = paramURI.getAuthority();
      if (localObject2 != null)
      {
        int j = ((String)localObject2).indexOf(64);
        if (j >= 0)
          localObject2 = ((String)localObject2).substring(j + 1);
        j = ((String)localObject2).lastIndexOf(58);
        if (j >= 0)
        {
          try
          {
            i = Integer.parseInt(((String)localObject2).substring(j + 1));
          }
          catch (NumberFormatException localNumberFormatException)
          {
            i = -1;
          }
          localObject2 = ((String)localObject2).substring(0, j);
        }
        localObject1 = localObject2;
      }
    }
    if ((str1 == null) || (localObject1 == null))
      throw new IllegalArgumentException("protocol = " + str1 + " host = " + ((String)localObject1));
    Object localObject2 = new ArrayList(1);
    if (isLoopback((String)localObject1))
    {
      ((List)localObject2).add(Proxy.NO_PROXY);
      return localObject2;
    }
    NonProxyInfo localNonProxyInfo1 = null;
    if ("http".equalsIgnoreCase(str1))
      localNonProxyInfo1 = httpNonProxyInfo;
    else if ("https".equalsIgnoreCase(str1))
      localNonProxyInfo1 = httpNonProxyInfo;
    else if ("ftp".equalsIgnoreCase(str1))
      localNonProxyInfo1 = ftpNonProxyInfo;
    String str2 = str1;
    NonProxyInfo localNonProxyInfo2 = localNonProxyInfo1;
    String str3 = ((String)localObject1).toLowerCase();
    Proxy localProxy = (Proxy)AccessController.doPrivileged(new PrivilegedAction(this, str2, str3, localNonProxyInfo2)
    {
      public Object run()
      {
        String str1 = null;
        int k = 0;
        String str2 = null;
        InetSocketAddress localInetSocketAddress = null;
        for (int i = 0; i < DefaultProxySelector.props.length; ++i)
          if (DefaultProxySelector.props[i][0].equalsIgnoreCase(this.val$proto))
          {
            Object localObject2;
            for (int j = 1; j < DefaultProxySelector.props[i].length; ++j)
            {
              str1 = NetProperties.get(DefaultProxySelector.props[i][j] + "Host");
              if ((str1 != null) && (str1.length() != 0))
                break;
            }
            if ((str1 == null) || (str1.length() == 0))
            {
              if (DefaultProxySelector.access$000())
              {
                if (this.val$proto.equalsIgnoreCase("socket"))
                  ??? = "socks";
                else
                  ??? = this.val$proto;
                localObject2 = DefaultProxySelector.access$100(this.this$0, (String)???, this.val$urlhost);
                if (localObject2 != null)
                  return localObject2;
              }
              return Proxy.NO_PROXY;
            }
            if (this.val$nprop != null)
            {
              str2 = NetProperties.get(this.val$nprop.property);
              synchronized (this.val$nprop)
              {
                if (str2 == null)
                {
                  this.val$nprop.hostsSource = null;
                  this.val$nprop.hostsPool = null;
                }
                else if (!(str2.equals(this.val$nprop.hostsSource)))
                {
                  localObject2 = new RegexpPool();
                  StringTokenizer localStringTokenizer = new StringTokenizer(str2, "|", false);
                  try
                  {
                    while (localStringTokenizer.hasMoreTokens())
                      ((RegexpPool)localObject2).add(localStringTokenizer.nextToken().toLowerCase(), Boolean.TRUE);
                  }
                  catch (REException localREException)
                  {
                  }
                  this.val$nprop.hostsPool = ((RegexpPool)localObject2);
                  this.val$nprop.hostsSource = str2;
                }
                if ((this.val$nprop.hostsPool == null) || (this.val$nprop.hostsPool.match(this.val$urlhost) == null))
                  break label341;
                label341: return Proxy.NO_PROXY;
              }
            }
            k = NetProperties.getInteger(DefaultProxySelector.props[i][j] + "Port", 0).intValue();
            if ((k == 0) && (j < DefaultProxySelector.props[i].length - 1))
              for (int l = 1; l < DefaultProxySelector.props[i].length - 1; ++l)
                if ((l != j) && (k == 0))
                  k = NetProperties.getInteger(DefaultProxySelector.props[i][l] + "Port", 0).intValue();
            if (k == 0)
              if (j == DefaultProxySelector.props[i].length - 1)
                k = DefaultProxySelector.access$200(this.this$0, "socket");
              else
                k = DefaultProxySelector.access$200(this.this$0, this.val$proto);
            localInetSocketAddress = InetSocketAddress.createUnresolved(str1, k);
            if (j == DefaultProxySelector.props[i].length - 1)
              return new Proxy(Proxy.Type.SOCKS, localInetSocketAddress);
            return new Proxy(Proxy.Type.HTTP, localInetSocketAddress);
          }
        return Proxy.NO_PROXY;
      }
    });
    ((List)localObject2).add(localProxy);
    return ((List<Proxy>)(List<Proxy>)localObject2);
  }

  public void connectFailed(URI paramURI, SocketAddress paramSocketAddress, IOException paramIOException)
  {
    if ((paramURI == null) || (paramSocketAddress == null) || (paramIOException == null))
      throw new IllegalArgumentException("Arguments can't be null.");
  }

  private int defaultPort(String paramString)
  {
    if ("http".equalsIgnoreCase(paramString))
      return 80;
    if ("https".equalsIgnoreCase(paramString))
      return 443;
    if ("ftp".equalsIgnoreCase(paramString))
      return 80;
    if ("socket".equalsIgnoreCase(paramString))
      return 1080;
    if ("gopher".equalsIgnoreCase(paramString))
      return 80;
    return -1;
  }

  private boolean isLoopback(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0))
      return false;
    if (paramString.equalsIgnoreCase("localhost"))
      return true;
    if (paramString.startsWith("127."))
    {
      int j;
      int i = 4;
      int k = paramString.length();
      if ((j = scanByte(paramString, i, k)) <= i)
        return false;
      i = j;
      if ((j = scan(paramString, i, k, '.')) <= i)
        return ((j == k) && (this.number > 0));
      i = j;
      if ((j = scanByte(paramString, i, k)) <= i)
        return false;
      i = j;
      if ((j = scan(paramString, i, k, '.')) <= i)
        return ((j == k) && (this.number > 0));
      i = j;
      if ((j = scanByte(paramString, i, k)) <= i)
        return false;
      return ((j == k) && (this.number > 0));
    }
    if (paramString.endsWith(":1"))
      return p6.matcher(paramString).matches();
    return false;
  }

  private static long lowMask(char paramChar1, char paramChar2)
  {
    long l = 3412047153814568960L;
    int i = Math.max(Math.min(paramChar1, 63), 0);
    int j = Math.max(Math.min(paramChar2, 63), 0);
    for (int k = i; k <= j; ++k)
      l |= 3412048167426850817L << k;
    return l;
  }

  private int scanByte(String paramString, int paramInt1, int paramInt2)
  {
    int i = paramInt1;
    int j = scan(paramString, i, paramInt2, L_DIGIT, 3412047944088551424L);
    if (j <= i)
      return j;
    this.number = Integer.parseInt(paramString.substring(i, j));
    if (this.number > 255)
      return i;
    return j;
  }

  private int scan(String paramString, int paramInt1, int paramInt2, char paramChar)
  {
    if ((paramInt1 < paramInt2) && (paramString.charAt(paramInt1) == paramChar))
      return (paramInt1 + 1);
    return paramInt1;
  }

  private int scan(String paramString, int paramInt1, int paramInt2, long paramLong1, long paramLong2)
  {
    for (int i = paramInt1; i < paramInt2; ++i)
    {
      char c = paramString.charAt(i);
      if (!(match(c, paramLong1, paramLong2)))
        break;
    }
    return i;
  }

  private boolean match(char paramChar, long paramLong1, long paramLong2)
  {
    if (paramChar < '@')
      return ((3412040144427941889L << paramChar & paramLong1) != 3412047463052214272L);
    if (paramChar < 128)
      return ((3412040144427941889L << paramChar - '@' & paramLong2) != 3412047463052214272L);
    return false;
  }

  private static native boolean init();

  private native Proxy getSystemProxy(String paramString1, String paramString2);

  static
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return NetProperties.getBoolean("java.net.useSystemProxies");
      }
    });
    if ((localBoolean != null) && (localBoolean.booleanValue()))
    {
      AccessController.doPrivileged(new LoadLibraryAction("net"));
      hasSystemProxies = init();
    }
    ftpNonProxyInfo = new NonProxyInfo("ftp.nonProxyHosts", null, null);
    httpNonProxyInfo = new NonProxyInfo("http.nonProxyHosts", null, null);
    p6 = Pattern.compile("::1|(0:){7}1|(0:){1,6}:1");
    L_DIGIT = lowMask('0', '9');
  }

  static class NonProxyInfo
  {
    String hostsSource;
    RegexpPool hostsPool;
    String property;

    NonProxyInfo(String paramString1, String paramString2, RegexpPool paramRegexpPool)
    {
      this.property = paramString1;
      this.hostsSource = paramString2;
      this.hostsPool = paramRegexpPool;
    }
  }
}
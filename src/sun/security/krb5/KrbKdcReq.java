package sun.security.krb5;

import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.StringTokenizer;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.TCPClient;
import sun.security.krb5.internal.UDPClient;

public abstract class KrbKdcReq
{
  private static final int DEFAULT_KDC_PORT = 88;
  private static final int DEFAULT_KDC_RETRY_LIMIT = 3;
  public static final int DEFAULT_KDC_TIMEOUT;
  private static final boolean DEBUG = Krb5.DEBUG;
  private static int udpPrefLimit = -1;
  protected byte[] obuf;
  protected byte[] ibuf;

  public String send(String paramString)
    throws IOException, sun.security.krb5.KrbException
  {
    boolean bool = (udpPrefLimit > 0) && (this.obuf != null) && (this.obuf.length > udpPrefLimit);
    return send(paramString, bool);
  }

  public String send(String paramString, boolean paramBoolean)
    throws IOException, sun.security.krb5.KrbException
  {
    if (this.obuf == null)
      return null;
    Object localObject = null;
    Config localConfig = Config.getInstance();
    if (paramString == null)
    {
      paramString = localConfig.getDefaultRealm();
      if (paramString == null)
        throw new sun.security.krb5.KrbException(60, "Cannot find default realm");
    }
    int i = getKdcTimeout(paramString);
    String str1 = localConfig.getKDCList(paramString);
    if (str1 == null)
      throw new sun.security.krb5.KrbException("Cannot get kdc for realm " + paramString);
    String str2 = null;
    StringTokenizer localStringTokenizer = new StringTokenizer(str1);
    if (localStringTokenizer.hasMoreTokens())
    {
      str2 = localStringTokenizer.nextToken();
      try
      {
        send(paramString, str2, paramBoolean);
      }
      catch (Exception localException)
      {
        while (true)
          localObject = localException;
      }
    }
    if ((this.ibuf == null) && (localObject != null))
    {
      if (localObject instanceof IOException)
        throw ((IOException)localObject);
      throw ((sun.security.krb5.KrbException)localObject);
    }
    return str2;
  }

  public void send(String paramString1, String paramString2, boolean paramBoolean)
    throws IOException, sun.security.krb5.KrbException
  {
    if (this.obuf == null)
      return;
    Object localObject1 = null;
    int i = 88;
    int j = getKdcTimeout(paramString1);
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString2, ":");
    String str = localStringTokenizer.nextToken();
    if (localStringTokenizer.hasMoreTokens())
    {
      localObject2 = localStringTokenizer.nextToken();
      int k = parsePositiveIntString((String)localObject2);
      if (k > 0)
        i = k;
    }
    if (DEBUG)
      System.out.println(">>> KrbKdcReq send: kdc=" + str + ((paramBoolean) ? " TCP:" : " UDP:") + i + ", timeout=" + j + ", number of retries =" + 3 + ", #bytes=" + this.obuf.length);
    Object localObject2 = new KdcCommunication(str, i, paramBoolean, j, this.obuf);
    try
    {
      this.ibuf = ((byte[])(byte[])AccessController.doPrivileged((PrivilegedExceptionAction)localObject2));
      if (DEBUG)
        System.out.println(">>> KrbKdcReq send: #bytes read=" + ((this.ibuf != null) ? this.ibuf.length : 0));
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Exception localException = localPrivilegedActionException.getException();
      if (localException instanceof IOException)
        throw ((IOException)localException);
      throw ((sun.security.krb5.KrbException)localException);
    }
    if (DEBUG)
      System.out.println(">>> KrbKdcReq send: #bytes read=" + ((this.ibuf != null) ? this.ibuf.length : 0));
  }

  private int getKdcTimeout(String paramString)
  {
    int i = DEFAULT_KDC_TIMEOUT;
    if (paramString == null)
      return i;
    int j = -1;
    try
    {
      String str = Config.getInstance().getDefault("kdc_timeout", paramString);
      j = parsePositiveIntString(str);
    }
    catch (Exception localException)
    {
    }
    if (j > 0)
      i = j;
    return i;
  }

  private static int parsePositiveIntString(String paramString)
  {
    if (paramString == null)
      return -1;
    int i = -1;
    try
    {
      i = Integer.parseInt(paramString);
    }
    catch (Exception localException)
    {
      return -1;
    }
    if (i >= 0)
      return i;
    return -1;
  }

  static
  {
    int i = -1;
    try
    {
      Config localConfig = Config.getInstance();
      String str = localConfig.getDefault("kdc_timeout", "libdefaults");
      i = parsePositiveIntString(str);
      str = localConfig.getDefault("udp_preference_limit", "libdefaults");
      udpPrefLimit = parsePositiveIntString(str);
    }
    catch (Exception localException)
    {
      if (DEBUG)
        System.out.println("Exception in getting kdc_timeout value, using default value " + localException.getMessage());
    }
    if (i > 0)
      DEFAULT_KDC_TIMEOUT = i;
    else
      DEFAULT_KDC_TIMEOUT = 30000;
  }

  private static class KdcCommunication
  implements PrivilegedExceptionAction
  {
    private String kdc;
    private int port;
    private boolean useTCP;
    private int timeout;
    private byte[] obuf;

    public KdcCommunication(String paramString, int paramInt1, boolean paramBoolean, int paramInt2, byte[] paramArrayOfByte)
    {
      this.kdc = paramString;
      this.port = paramInt1;
      this.useTCP = paramBoolean;
      this.timeout = paramInt2;
      this.obuf = paramArrayOfByte;
    }

    public Object run()
      throws IOException, sun.security.krb5.KrbException
    {
      byte[] arrayOfByte = null;
      if (this.useTCP)
      {
        TCPClient localTCPClient = new TCPClient(this.kdc, this.port);
        try
        {
          localTCPClient.send(this.obuf);
          arrayOfByte = localTCPClient.receive();
        }
        finally
        {
          localTCPClient.close();
        }
      }
      else
      {
        int i = 1;
        if (i <= 3)
        {
          UDPClient localUDPClient = new UDPClient(this.kdc, this.port, this.timeout);
          if (KrbKdcReq.access$000())
            System.out.println(">>> KDCCommunication: kdc=" + this.kdc + ((this.useTCP) ? " TCP:" : " UDP:") + this.port + ", timeout=" + this.timeout + ",Attempt =" + i + ", #bytes=" + this.obuf.length);
          localUDPClient.send(this.obuf);
          try
          {
            arrayOfByte = localUDPClient.receive();
          }
          catch (SocketTimeoutException localSocketTimeoutException)
          {
            while (true)
            {
              if (KrbKdcReq.access$000())
                System.out.println("SocketTimeOutException with attempt: " + i);
              if (i == 3)
              {
                arrayOfByte = null;
                throw localSocketTimeoutException;
              }
              ++i;
            }
          }
        }
      }
      return arrayOfByte;
    }
  }
}
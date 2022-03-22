package sun.security.x509;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class URIName
  implements GeneralNameInterface
{
  private URI uri;
  private String host;
  private DNSName hostDNS;
  private IPAddressName hostIP;

  public URIName(DerValue paramDerValue)
    throws IOException
  {
    this(paramDerValue.getIA5String());
  }

  public URIName(String paramString)
    throws IOException
  {
    try
    {
      this.uri = new URI(paramString);
    }
    catch (URISyntaxException localURISyntaxException)
    {
      throw ((IOException)new IOException("invalid URI name:" + paramString).initCause(localURISyntaxException));
    }
    if (this.uri.getScheme() == null)
      throw new IOException("URI name must include scheme:" + paramString);
    this.host = this.uri.getHost();
    if (this.host != null)
      if (this.host.charAt(0) == '[')
      {
        String str = this.host.substring(1, this.host.length() - 1);
        try
        {
          this.hostIP = new IPAddressName(str);
        }
        catch (IOException localIOException2)
        {
          throw new IOException("invalid URI name (host portion is not a valid IPv6 address):" + paramString);
        }
      }
      else
      {
        try
        {
          this.hostDNS = new DNSName(this.host);
        }
        catch (IOException localIOException1)
        {
          try
          {
            this.hostIP = new IPAddressName(this.host);
          }
          catch (Exception localException)
          {
            throw new IOException("invalid URI name (host portion is not a valid DNS name, IPv4 address, or IPv6 address):" + paramString);
          }
        }
      }
  }

  public static URIName nameConstraint(DerValue paramDerValue)
    throws IOException
  {
    URI localURI;
    String str1 = paramDerValue.getIA5String();
    try
    {
      localURI = new URI(str1);
    }
    catch (URISyntaxException localURISyntaxException)
    {
      throw ((IOException)new IOException("invalid URI name constraint:" + str1).initCause(localURISyntaxException));
    }
    if (localURI.getScheme() == null)
    {
      String str2 = localURI.getSchemeSpecificPart();
      try
      {
        DNSName localDNSName;
        if (str2.charAt(0) == '.')
          localDNSName = new DNSName(str2.substring(1));
        else
          localDNSName = new DNSName(str2);
        return new URIName(localURI, str2, localDNSName);
      }
      catch (IOException localIOException)
      {
        throw ((IOException)new IOException("invalid URI name constraint:" + str1).initCause(localIOException));
      }
    }
    throw new IOException("invalid URI name constraint (should not include scheme):" + str1);
  }

  URIName(URI paramURI, String paramString, DNSName paramDNSName)
  {
    this.uri = paramURI;
    this.host = paramString;
    this.hostDNS = paramDNSName;
  }

  public int getType()
  {
    return 6;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    paramDerOutputStream.putIA5String(this.uri.toASCIIString());
  }

  public String toString()
  {
    return "URIName: " + this.uri.toString();
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof URIName))
      return false;
    URIName localURIName = (URIName)paramObject;
    return this.uri.equals(localURIName.getURI());
  }

  public URI getURI()
  {
    return this.uri;
  }

  public String getName()
  {
    return this.uri.toString();
  }

  public String getScheme()
  {
    return this.uri.getScheme();
  }

  public String getHost()
  {
    return this.host;
  }

  public Object getHostObject()
  {
    if (this.hostIP != null)
      return this.hostIP;
    return this.hostDNS;
  }

  public int hashCode()
  {
    return this.uri.hashCode();
  }

  public int constrains(GeneralNameInterface paramGeneralNameInterface)
    throws UnsupportedOperationException
  {
    int i;
    if (paramGeneralNameInterface == null)
    {
      i = -1;
    }
    else if (paramGeneralNameInterface.getType() != 6)
    {
      i = -1;
    }
    else
    {
      String str = ((URIName)paramGeneralNameInterface).getHost();
      if (str.equalsIgnoreCase(this.host))
      {
        i = 0;
      }
      else
      {
        Object localObject = ((URIName)paramGeneralNameInterface).getHostObject();
        if ((this.hostDNS == null) || (!(localObject instanceof DNSName)))
        {
          i = 3;
        }
        else
        {
          int j = (this.host.charAt(0) == '.') ? 1 : 0;
          int k = (str.charAt(0) == '.') ? 1 : 0;
          DNSName localDNSName = (DNSName)localObject;
          i = this.hostDNS.constrains(localDNSName);
          if ((j == 0) && (k == 0) && (((i == 2) || (i == 1))))
            i = 3;
          if ((j != k) && (i == 0))
            if (j != 0)
              i = 2;
            else
              i = 1;
        }
      }
    }
    return i;
  }

  public int subtreeDepth()
    throws UnsupportedOperationException
  {
    DNSName localDNSName = null;
    try
    {
      localDNSName = new DNSName(this.host);
    }
    catch (IOException localIOException)
    {
      throw new UnsupportedOperationException(localIOException.getMessage());
    }
    return localDNSName.subtreeDepth();
  }
}
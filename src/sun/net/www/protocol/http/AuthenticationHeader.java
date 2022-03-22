package sun.net.www.protocol.http;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;

public class AuthenticationHeader
{
  MessageHeader rsp;
  HeaderParser preferred;
  String preferred_r;
  String host = null;
  static String authPref = null;
  String hdrname;
  HashMap schemes;

  public String toString()
  {
    return "AuthenticationHeader: prefer " + this.preferred_r;
  }

  public AuthenticationHeader(String paramString, MessageHeader paramMessageHeader)
  {
    this.rsp = paramMessageHeader;
    this.hdrname = paramString;
    this.schemes = new HashMap();
    parse();
  }

  public AuthenticationHeader(String paramString1, MessageHeader paramMessageHeader, String paramString2)
  {
    this.host = paramString2;
    this.rsp = paramMessageHeader;
    this.hdrname = paramString1;
    this.schemes = new HashMap();
    parse();
  }

  private void parse()
  {
    Object localObject2;
    Iterator localIterator1 = this.rsp.multiValueIterator(this.hdrname);
    while (localIterator1.hasNext())
    {
      HeaderParser localHeaderParser;
      String str;
      localObject1 = (String)localIterator1.next();
      localObject2 = new HeaderParser((String)localObject1);
      Iterator localIterator2 = ((HeaderParser)localObject2).keys();
      int i = 0;
      int j = -1;
      while (localIterator2.hasNext())
      {
        localIterator2.next();
        if (((HeaderParser)localObject2).findValue(i) == null)
        {
          if (j != -1)
          {
            localHeaderParser = ((HeaderParser)localObject2).subsequence(j, i);
            str = localHeaderParser.findKey(0);
            this.schemes.put(str, new SchemeMapValue(localHeaderParser, (String)localObject1));
          }
          j = i;
        }
        ++i;
      }
      if (i > j)
      {
        localHeaderParser = ((HeaderParser)localObject2).subsequence(j, i);
        str = localHeaderParser.findKey(0);
        this.schemes.put(str, new SchemeMapValue(localHeaderParser, (String)localObject1));
      }
    }
    Object localObject1 = null;
    if (authPref != null)
      if ((localObject1 = (SchemeMapValue)this.schemes.get(authPref)) != null)
        break label346;
    if (localObject1 == null)
    {
      localObject2 = (SchemeMapValue)this.schemes.get("negotiate");
      if (localObject2 != null)
      {
        if ((this.host == null) || (!(NegotiateAuthentication.isSupported(this.host, "Negotiate"))))
          localObject2 = null;
        localObject1 = localObject2;
      }
    }
    if (localObject1 == null)
    {
      localObject2 = (SchemeMapValue)this.schemes.get("kerberos");
      if (localObject2 != null)
      {
        if ((this.host == null) || (!(NegotiateAuthentication.isSupported(this.host, "Kerberos"))))
          localObject2 = null;
        localObject1 = localObject2;
      }
    }
    if (localObject1 == null)
      if ((localObject1 = (SchemeMapValue)this.schemes.get("digest")) == null)
        if ((localObject1 = (SchemeMapValue)this.schemes.get("ntlm")) == null)
          localObject1 = (SchemeMapValue)this.schemes.get("basic");
    if (localObject1 != null)
    {
      label346: this.preferred = ((SchemeMapValue)localObject1).parser;
      this.preferred_r = ((SchemeMapValue)localObject1).raw;
    }
  }

  public HeaderParser headerParser()
  {
    return this.preferred;
  }

  public String scheme()
  {
    if (this.preferred != null)
      return this.preferred.findKey(0);
    return null;
  }

  public String raw()
  {
    return this.preferred_r;
  }

  public boolean isPresent()
  {
    return (this.preferred != null);
  }

  static
  {
    authPref = (String)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return System.getProperty("http.auth.preference");
      }
    });
    if (authPref != null)
    {
      authPref = authPref.toLowerCase();
      if ((authPref.equals("spnego")) || (authPref.equals("kerberos")))
        authPref = "negotiate";
    }
  }

  static class SchemeMapValue
  {
    String raw;
    HeaderParser parser;

    SchemeMapValue(HeaderParser paramHeaderParser, String paramString)
    {
      this.raw = paramString;
      this.parser = paramHeaderParser;
    }
  }
}
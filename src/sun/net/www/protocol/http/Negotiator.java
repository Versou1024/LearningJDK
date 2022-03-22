package sun.net.www.protocol.http;

import java.lang.reflect.Constructor;

abstract class Negotiator
{
  static Negotiator getSupported(String paramString1, String paramString2)
    throws Exception
  {
    Class localClass = Class.forName("sun.net.www.protocol.http.NegotiatorImpl");
    Constructor localConstructor = localClass.getConstructor(new Class[] { String.class, String.class });
    return ((Negotiator)(Negotiator)localConstructor.newInstance(new Object[] { paramString1, paramString2 }));
  }

  abstract byte[] firstToken()
    throws Exception;

  abstract byte[] nextToken(byte[] paramArrayOfByte)
    throws Exception;
}
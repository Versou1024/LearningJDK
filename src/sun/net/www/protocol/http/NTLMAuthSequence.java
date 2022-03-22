package sun.net.www.protocol.http;

import java.io.IOException;
import sun.misc.BASE64Decoder;

public class NTLMAuthSequence
{
  private String username;
  private String password;
  private String ntdomain;
  private int state;
  private long crdHandle;
  private long ctxHandle;

  NTLMAuthSequence(String paramString1, String paramString2, String paramString3)
    throws IOException
  {
    this.username = paramString1;
    this.password = paramString2;
    this.ntdomain = paramString3;
    this.state = 0;
    this.crdHandle = getCredentialsHandle(paramString1, paramString3, paramString2);
    if (this.crdHandle == 3412046810217185280L)
      throw new IOException("could not get credentials handle");
  }

  public String getAuthHeader(String paramString)
    throws IOException
  {
    byte[] arrayOfByte1 = null;
    if (paramString != null)
      arrayOfByte1 = new BASE64Decoder().decodeBuffer(paramString);
    byte[] arrayOfByte2 = getNextToken(this.crdHandle, arrayOfByte1);
    if (arrayOfByte2 == null)
      throw new IOException("Internal authentication error");
    return new B64Encoder().encode(arrayOfByte2);
  }

  private static native void initFirst();

  private native long getCredentialsHandle(String paramString1, String paramString2, String paramString3);

  private native byte[] getNextToken(long paramLong, byte[] paramArrayOfByte);

  static
  {
    initFirst();
  }
}
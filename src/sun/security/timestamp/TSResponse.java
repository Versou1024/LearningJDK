package sun.security.timestamp;

import java.io.IOException;
import sun.security.pkcs.PKCS7;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

public class TSResponse
{
  public static final int GRANTED = 0;
  public static final int GRANTED_WITH_MODS = 1;
  public static final int REJECTION = 2;
  public static final int WAITING = 3;
  public static final int REVOCATION_WARNING = 4;
  public static final int REVOCATION_NOTIFICATION = 5;
  public static final int BAD_ALG = 0;
  public static final int BAD_REQUEST = 2;
  public static final int BAD_DATA_FORMAT = 5;
  public static final int TIME_NOT_AVAILABLE = 14;
  public static final int UNACCEPTED_POLICY = 15;
  public static final int UNACCEPTED_EXTENSION = 16;
  public static final int ADD_INFO_NOT_AVAILABLE = 17;
  public static final int SYSTEM_FAILURE = 25;
  private static final boolean DEBUG = 0;
  private int status;
  private String[] statusString = null;
  private int failureInfo = -1;
  private byte[] encodedTsToken = null;
  private PKCS7 tsToken = null;

  TSResponse(byte[] paramArrayOfByte)
    throws IOException
  {
    parse(paramArrayOfByte);
  }

  public int getStatusCode()
  {
    return this.status;
  }

  public String[] getStatusMessages()
  {
    return this.statusString;
  }

  public int getFailureCode()
  {
    return this.failureInfo;
  }

  public String getStatusCodeAsText()
  {
    switch (this.status)
    {
    case 0:
      return "the timestamp request was granted.";
    case 1:
      return "the timestamp request was granted with some modifications.";
    case 2:
      return "the timestamp request was rejected.";
    case 3:
      return "the timestamp request has not yet been processed.";
    case 4:
      return "warning: a certificate revocation is imminent.";
    case 5:
      return "notification: a certificate revocation has occurred.";
    }
    return "unknown status code " + this.status + ".";
  }

  public String getFailureCodeAsText()
  {
    if (this.failureInfo == -1)
      return null;
    switch (this.failureInfo)
    {
    case 0:
      return "Unrecognized or unsupported alrorithm identifier.";
    case 2:
      return "The requested transaction is not permitted or supported.";
    case 5:
      return "The data submitted has the wrong format.";
    case 14:
      return "The TSA's time source is not available.";
    case 15:
      return "The requested TSA policy is not supported by the TSA.";
    case 16:
      return "The requested extension is not supported by the TSA.";
    case 17:
      return "The additional information requested could not be understood or is not available.";
    case 25:
      return "The request cannot be handled due to system failure.";
    case 1:
    case 3:
    case 4:
    case 6:
    case 7:
    case 8:
    case 9:
    case 10:
    case 11:
    case 12:
    case 13:
    case 18:
    case 19:
    case 20:
    case 21:
    case 22:
    case 23:
    case 24:
    }
    return "unknown status code " + this.status;
  }

  public PKCS7 getToken()
  {
    return this.tsToken;
  }

  public byte[] getEncodedToken()
  {
    return this.encodedTsToken;
  }

  private void parse(byte[] paramArrayOfByte)
    throws IOException
  {
    Object localObject;
    int i;
    DerValue localDerValue1 = new DerValue(paramArrayOfByte);
    if (localDerValue1.tag != 48)
      throw new IOException("Bad encoding for timestamp response");
    DerValue localDerValue2 = localDerValue1.data.getDerValue();
    this.status = localDerValue2.data.getInteger();
    if (localDerValue2.data.available() > 0)
    {
      localObject = localDerValue2.data.getSequence(1);
      this.statusString = new String[localObject.length];
      for (i = 0; i < localObject.length; ++i)
        this.statusString[i] = localObject[i].data.getUTF8String();
    }
    if (localDerValue2.data.available() > 0)
    {
      localObject = localDerValue2.data.getBitString();
      i = new Byte(localObject[0]).intValue();
      if ((i < 0) || (i > 25) || (localObject.length != 1))
        throw new IOException("Bad encoding for timestamp response: unrecognized value for the failInfo element");
      this.failureInfo = i;
    }
    if (localDerValue1.data.available() > 0)
    {
      localObject = localDerValue1.data.getDerValue();
      this.encodedTsToken = ((DerValue)localObject).toByteArray();
      this.tsToken = new PKCS7(this.encodedTsToken);
    }
    if ((this.status == 0) || (this.status == 1))
    {
      if (this.tsToken != null)
        return;
      throw new TimestampException("Bad encoding for timestamp response: expected a timeStampToken element to be present");
    }
    if (this.tsToken != null)
      throw new TimestampException("Bad encoding for timestamp response: expected no timeStampToken element to be present");
  }

  static final class TimestampException extends IOException
  {
    TimestampException(String paramString)
    {
      super(paramString);
    }
  }
}
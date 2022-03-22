package sun.security.timestamp;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpTimestamper
  implements Timestamper
{
  private static final int CONNECT_TIMEOUT = 15000;
  private static final String TS_QUERY_MIME_TYPE = "application/timestamp-query";
  private static final String TS_REPLY_MIME_TYPE = "application/timestamp-reply";
  private static final boolean DEBUG = 0;
  private String tsaUrl = null;

  public HttpTimestamper(String paramString)
  {
    this.tsaUrl = paramString;
  }

  public TSResponse generateTimestamp(TSRequest paramTSRequest)
    throws IOException
  {
    HttpURLConnection localHttpURLConnection = (HttpURLConnection)new URL(this.tsaUrl).openConnection();
    localHttpURLConnection.setDoOutput(true);
    localHttpURLConnection.setUseCaches(false);
    localHttpURLConnection.setRequestProperty("Content-Type", "application/timestamp-query");
    localHttpURLConnection.setRequestMethod("POST");
    localHttpURLConnection.setConnectTimeout(15000);
    localHttpURLConnection.connect();
    DataOutputStream localDataOutputStream = new DataOutputStream(localHttpURLConnection.getOutputStream());
    byte[] arrayOfByte1 = paramTSRequest.encode();
    localDataOutputStream.write(arrayOfByte1, 0, arrayOfByte1.length);
    localDataOutputStream.flush();
    localDataOutputStream.close();
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(localHttpURLConnection.getInputStream());
    int i = localHttpURLConnection.getContentLength();
    if (i == -1)
      i = 2147483647;
    verifyMimeType(localHttpURLConnection.getContentType());
    byte[] arrayOfByte2 = new byte[i];
    int j = 0;
    int k = 0;
    while ((k != -1) && (j < i))
    {
      k = localBufferedInputStream.read(arrayOfByte2, j, arrayOfByte2.length - j);
      j += k;
    }
    localBufferedInputStream.close();
    return new TSResponse(arrayOfByte2);
  }

  private static void verifyMimeType(String paramString)
    throws IOException
  {
    if (!("application/timestamp-reply".equalsIgnoreCase(paramString)))
      throw new IOException("MIME Content-Type is not application/timestamp-reply");
  }
}
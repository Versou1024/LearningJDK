package sun.security.timestamp;

import java.io.IOException;

public abstract interface Timestamper
{
  public abstract TSResponse generateTimestamp(TSRequest paramTSRequest)
    throws IOException;
}
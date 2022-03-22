package sun.security.util;

import java.io.IOException;
import java.io.OutputStream;

public abstract interface DerEncoder
{
  public abstract void derEncode(OutputStream paramOutputStream)
    throws IOException;
}
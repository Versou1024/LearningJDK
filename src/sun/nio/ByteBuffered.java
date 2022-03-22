package sun.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract interface ByteBuffered
{
  public abstract ByteBuffer getByteBuffer()
    throws IOException;
}
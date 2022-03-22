package sun.audio;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NativeAudioStream extends FilterInputStream
{
  public NativeAudioStream(InputStream paramInputStream)
    throws IOException
  {
    super(paramInputStream);
  }

  public int getLength()
  {
    return 0;
  }
}
package sun.audio;

import java.io.IOException;
import java.io.InputStream;

public class AudioTranslatorStream extends NativeAudioStream
{
  private int length = 0;

  public AudioTranslatorStream(InputStream paramInputStream)
    throws IOException
  {
    super(paramInputStream);
    throw new InvalidAudioFormatException();
  }

  public int getLength()
  {
    return this.length;
  }
}
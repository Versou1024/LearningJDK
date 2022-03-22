package sun.audio;

import java.io.IOException;

class InvalidAudioFormatException extends IOException
{
  public InvalidAudioFormatException()
  {
  }

  public InvalidAudioFormatException(String paramString)
  {
    super(paramString);
  }
}
package sun.audio;

public class ContinuousAudioDataStream extends AudioDataStream
{
  public ContinuousAudioDataStream(AudioData paramAudioData)
  {
    super(paramAudioData);
  }

  public int read()
  {
    int i = super.read();
    if (i == -1)
    {
      reset();
      i = super.read();
    }
    return i;
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    int i = 0;
    while (i < paramInt2)
    {
      int j = super.read(paramArrayOfByte, paramInt1 + i, paramInt2 - i);
      if (j >= 0)
        i += j;
      else
        reset();
    }
    return i;
  }
}
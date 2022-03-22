package sun.audio;

import java.io.ByteArrayInputStream;

public class AudioDataStream extends ByteArrayInputStream
{
  AudioData ad;

  public AudioDataStream(AudioData paramAudioData)
  {
    super(paramAudioData.buffer);
    this.ad = paramAudioData;
  }

  AudioData getAudioData()
  {
    return this.ad;
  }
}
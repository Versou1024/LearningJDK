package sun.audio;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioStream extends FilterInputStream
{
  protected AudioInputStream ais = null;
  protected AudioFormat format = null;
  protected MidiFileFormat midiformat = null;
  protected InputStream stream = null;

  public AudioStream(InputStream paramInputStream)
    throws IOException
  {
    super(paramInputStream);
    this.stream = paramInputStream;
    if (!(paramInputStream.markSupported()))
      this.stream = new BufferedInputStream(paramInputStream, 1024);
    try
    {
      this.ais = AudioSystem.getAudioInputStream(this.stream);
      this.format = this.ais.getFormat();
      this.in = this.ais;
    }
    catch (UnsupportedAudioFileException localUnsupportedAudioFileException)
    {
      try
      {
        this.midiformat = MidiSystem.getMidiFileFormat(this.stream);
      }
      catch (InvalidMidiDataException localInvalidMidiDataException)
      {
        throw new IOException("could not create audio stream from input stream");
      }
    }
  }

  public AudioData getData()
    throws IOException
  {
    int i = getLength();
    if (i < 1048576)
    {
      byte[] arrayOfByte = new byte[i];
      try
      {
        this.ais.read(arrayOfByte, 0, i);
      }
      catch (IOException localIOException)
      {
        throw new IOException("Could not create AudioData Object");
      }
      return new AudioData(this.format, arrayOfByte);
    }
    throw new IOException("could not create AudioData object");
  }

  public int getLength()
  {
    if ((this.ais != null) && (this.format != null))
      return (int)(this.ais.getFrameLength() * this.ais.getFormat().getFrameSize());
    if (this.midiformat != null)
      return this.midiformat.getByteLength();
    return -1;
  }
}
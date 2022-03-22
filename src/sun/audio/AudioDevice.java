package sun.audio;

import com.sun.media.sound.DataPusher;
import com.sun.media.sound.Toolkit;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioDevice
{
  private boolean DEBUG = false;
  private Hashtable clipStreams = new Hashtable();
  private Vector infos = new Vector();
  private boolean playing = false;
  private Mixer mixer = null;
  public static final AudioDevice device = new AudioDevice();

  private synchronized void startSampled(AudioInputStream paramAudioInputStream, InputStream paramInputStream)
    throws UnsupportedAudioFileException, LineUnavailableException
  {
    Info localInfo = null;
    DataPusher localDataPusher = null;
    DataLine.Info localInfo1 = null;
    SourceDataLine localSourceDataLine = null;
    paramAudioInputStream = Toolkit.getPCMConvertedAudioInputStream(paramAudioInputStream);
    if (paramAudioInputStream == null)
      return;
    localInfo1 = new DataLine.Info(SourceDataLine.class, paramAudioInputStream.getFormat());
    if (!(AudioSystem.isLineSupported(localInfo1)))
      return;
    localSourceDataLine = (SourceDataLine)AudioSystem.getLine(localInfo1);
    localDataPusher = new DataPusher(localSourceDataLine, paramAudioInputStream);
    localInfo = new Info(this, null, paramInputStream, localDataPusher);
    this.infos.addElement(localInfo);
    localDataPusher.start();
  }

  private synchronized void startMidi(InputStream paramInputStream1, InputStream paramInputStream2)
    throws InvalidMidiDataException, MidiUnavailableException
  {
    Sequencer localSequencer = null;
    Info localInfo = null;
    localSequencer = MidiSystem.getSequencer();
    localSequencer.open();
    try
    {
      localSequencer.setSequence(paramInputStream1);
    }
    catch (IOException localIOException)
    {
      throw new InvalidMidiDataException(localIOException.getMessage());
    }
    localInfo = new Info(this, localSequencer, paramInputStream2, null);
    this.infos.addElement(localInfo);
    localSequencer.addMetaEventListener(localInfo);
    localSequencer.start();
  }

  public synchronized void openChannel(InputStream paramInputStream)
  {
    if (this.DEBUG)
    {
      System.out.println("AudioDevice: openChannel");
      System.out.println("input stream =" + paramInputStream);
    }
    Info localInfo = null;
    for (int i = 0; i < this.infos.size(); ++i)
    {
      localInfo = (Info)this.infos.elementAt(i);
      if (localInfo.in == paramInputStream)
        return;
    }
    AudioInputStream localAudioInputStream1 = null;
    if (paramInputStream instanceof AudioStream)
    {
      if (((AudioStream)paramInputStream).midiformat != null)
        try
        {
          startMidi(((AudioStream)paramInputStream).stream, paramInputStream);
        }
        catch (Exception localException1)
        {
          return;
        }
      if (((AudioStream)paramInputStream).ais == null)
        break label368;
      try
      {
        startSampled(((AudioStream)paramInputStream).ais, paramInputStream);
      }
      catch (Exception localException2)
      {
        return;
      }
    }
    if (paramInputStream instanceof AudioDataStream)
    {
      if (paramInputStream instanceof ContinuousAudioDataStream)
        try
        {
          AudioInputStream localAudioInputStream2 = new AudioInputStream(paramInputStream, ((AudioDataStream)paramInputStream).getAudioData().format, -1L);
          startSampled(localAudioInputStream2, paramInputStream);
        }
        catch (Exception localException3)
        {
          return;
        }
      try
      {
        AudioInputStream localAudioInputStream3 = new AudioInputStream(paramInputStream, ((AudioDataStream)paramInputStream).getAudioData().format, ((AudioDataStream)paramInputStream).getAudioData().buffer.length);
        startSampled(localAudioInputStream3, paramInputStream);
      }
      catch (Exception localException4)
      {
        return;
      }
    }
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(paramInputStream, 1024);
    try
    {
      try
      {
        localAudioInputStream1 = AudioSystem.getAudioInputStream(localBufferedInputStream);
      }
      catch (IOException localIOException1)
      {
        return;
      }
      startSampled(localAudioInputStream1, paramInputStream);
    }
    catch (UnsupportedAudioFileException localUnsupportedAudioFileException1)
    {
      try
      {
        try
        {
          MidiFileFormat localMidiFileFormat = MidiSystem.getMidiFileFormat(localBufferedInputStream);
        }
        catch (IOException localIOException2)
        {
          return;
        }
        startMidi(localBufferedInputStream, paramInputStream);
      }
      catch (InvalidMidiDataException localInvalidMidiDataException)
      {
        AudioFormat localAudioFormat = new AudioFormat(AudioFormat.Encoding.ULAW, 8000.0F, 8, 1, 1, 8000.0F, true);
        try
        {
          AudioInputStream localAudioInputStream4 = new AudioInputStream(localBufferedInputStream, localAudioFormat, -1L);
          startSampled(localAudioInputStream4, paramInputStream);
        }
        catch (UnsupportedAudioFileException localUnsupportedAudioFileException2)
        {
          return;
        }
        catch (LineUnavailableException localLineUnavailableException2)
        {
          return;
        }
      }
      catch (MidiUnavailableException localMidiUnavailableException)
      {
        return;
      }
    }
    catch (LineUnavailableException localLineUnavailableException1)
    {
      return;
    }
    label368: super.notify();
  }

  public synchronized void closeChannel(InputStream paramInputStream)
  {
    if (this.DEBUG)
      System.out.println("AudioDevice.closeChannel");
    if (paramInputStream == null)
      return;
    for (int i = 0; i < this.infos.size(); ++i)
    {
      Info localInfo = (Info)this.infos.elementAt(i);
      if (localInfo.in == paramInputStream)
        if (localInfo.sequencer != null)
        {
          localInfo.sequencer.stop();
          this.infos.removeElement(localInfo);
        }
        else if (localInfo.datapusher != null)
        {
          localInfo.datapusher.stop();
          this.infos.removeElement(localInfo);
        }
    }
    super.notify();
  }

  public synchronized void open()
  {
  }

  public synchronized void close()
  {
  }

  public void play()
  {
    if (this.DEBUG)
      System.out.println("exiting play()");
  }

  public synchronized void closeStreams()
  {
    for (int i = 0; i < this.infos.size(); ++i)
    {
      Info localInfo = (Info)this.infos.elementAt(i);
      if (localInfo.sequencer != null)
      {
        localInfo.sequencer.stop();
        localInfo.sequencer.close();
        this.infos.removeElement(localInfo);
      }
      else if (localInfo.datapusher != null)
      {
        localInfo.datapusher.stop();
        this.infos.removeElement(localInfo);
      }
    }
    if (this.DEBUG)
      System.err.println("Audio Device: Streams all closed.");
    this.clipStreams = new Hashtable();
    this.infos = new Vector();
  }

  public int openChannels()
  {
    return this.infos.size();
  }

  void setVerbose(boolean paramBoolean)
  {
    this.DEBUG = paramBoolean;
  }

  class Info
  implements MetaEventListener
  {
    Sequencer sequencer;
    InputStream in;
    DataPusher datapusher;

    Info(, Sequencer paramSequencer, InputStream paramInputStream, DataPusher paramDataPusher)
    {
      this.sequencer = paramSequencer;
      this.in = paramInputStream;
      this.datapusher = paramDataPusher;
    }

    public void meta()
    {
      if ((paramMetaMessage.getType() == 47) && (this.sequencer != null))
        this.sequencer.close();
    }
  }
}
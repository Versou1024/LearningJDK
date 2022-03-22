package sun.audio;

import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class AudioPlayer extends Thread
{
  private AudioDevice devAudio;
  private static boolean DEBUG = false;
  public static final AudioPlayer player = getAudioPlayer();

  private static ThreadGroup getAudioThreadGroup()
  {
    if (DEBUG)
      System.out.println("AudioPlayer.getAudioThreadGroup()");
    for (ThreadGroup localThreadGroup = currentThread().getThreadGroup(); (localThreadGroup.getParent() != null) && (localThreadGroup.getParent().getParent() != null); localThreadGroup = localThreadGroup.getParent());
    return localThreadGroup;
  }

  private static AudioPlayer getAudioPlayer()
  {
    if (DEBUG)
      System.out.println("> AudioPlayer.getAudioPlayer()");
    1 local1 = new PrivilegedAction()
    {
      public Object run()
      {
        AudioPlayer localAudioPlayer = new AudioPlayer(null);
        localAudioPlayer.setPriority(10);
        localAudioPlayer.setDaemon(true);
        localAudioPlayer.start();
        return localAudioPlayer;
      }
    };
    AudioPlayer localAudioPlayer = (AudioPlayer)AccessController.doPrivileged(local1);
    return localAudioPlayer;
  }

  private AudioPlayer()
  {
    super(getAudioThreadGroup(), "Audio Player");
    if (DEBUG)
      System.out.println("> AudioPlayer private constructor");
    this.devAudio = AudioDevice.device;
    this.devAudio.open();
    if (DEBUG)
      System.out.println("< AudioPlayer private constructor completed");
  }

  public synchronized void start(InputStream paramInputStream)
  {
    if (DEBUG)
    {
      System.out.println("> AudioPlayer.start");
      System.out.println("  InputStream = " + paramInputStream);
    }
    this.devAudio.openChannel(paramInputStream);
    notify();
    if (DEBUG)
      System.out.println("< AudioPlayer.start completed");
  }

  public synchronized void stop(InputStream paramInputStream)
  {
    if (DEBUG)
      System.out.println("> AudioPlayer.stop");
    this.devAudio.closeChannel(paramInputStream);
    if (DEBUG)
      System.out.println("< AudioPlayer.stop completed");
  }

  public void run()
  {
    this.devAudio.play();
    if (DEBUG)
      System.out.println("AudioPlayer mixing loop.");
    try
    {
      Thread.sleep(5000L);
    }
    catch (Exception localException)
    {
      if (DEBUG)
        System.out.println("AudioPlayer exited.");
    }
  }
}
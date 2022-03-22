package sun.security.tools;

class AudioPerm extends Perm
{
  public AudioPerm()
  {
    super("AudioPermission", "javax.sound.sampled.AudioPermission", new String[] { "play", "record" }, null);
  }
}
package sun.applet;

public class AppletThreadGroup extends ThreadGroup
{
  public AppletThreadGroup(String paramString)
  {
    this(Thread.currentThread().getThreadGroup(), paramString);
  }

  public AppletThreadGroup(ThreadGroup paramThreadGroup, String paramString)
  {
    super(paramThreadGroup, paramString);
    setMaxPriority(4);
  }
}
package sun.applet;

import java.util.EventObject;

public class AppletEvent extends EventObject
{
  private Object arg;
  private int id;

  public AppletEvent(Object paramObject1, int paramInt, Object paramObject2)
  {
    super(paramObject1);
    this.arg = paramObject2;
    this.id = paramInt;
  }

  public int getID()
  {
    return this.id;
  }

  public Object getArgument()
  {
    return this.arg;
  }

  public String toString()
  {
    String str = getClass().getName() + "[source=" + this.source + " + id=" + this.id;
    if (this.arg != null)
      str = str + " + arg=" + this.arg;
    str = str + " ]";
    return str;
  }
}
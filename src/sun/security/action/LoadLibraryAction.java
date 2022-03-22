package sun.security.action;

import java.security.PrivilegedAction;

public class LoadLibraryAction
  implements PrivilegedAction
{
  private String theLib;

  public LoadLibraryAction(String paramString)
  {
    this.theLib = paramString;
  }

  public Object run()
  {
    System.loadLibrary(this.theLib);
    return null;
  }
}
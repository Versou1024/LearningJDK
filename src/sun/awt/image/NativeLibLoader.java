package sun.awt.image;

import java.security.AccessController;
import sun.security.action.LoadLibraryAction;

class NativeLibLoader
{
  static void loadLibraries()
  {
    AccessController.doPrivileged(new LoadLibraryAction("awt"));
  }
}
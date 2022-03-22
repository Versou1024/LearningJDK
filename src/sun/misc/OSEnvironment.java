package sun.misc;

import sun.io.Win32ErrorMode;

public class OSEnvironment
{
  public static void initialize()
  {
    Win32ErrorMode.initialize();
  }
}
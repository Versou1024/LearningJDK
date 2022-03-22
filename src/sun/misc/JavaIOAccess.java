package sun.misc;

import java.io.Console;
import java.nio.charset.Charset;

public abstract interface JavaIOAccess
{
  public abstract Console console();

  public abstract Runnable consoleRestoreHook();

  public abstract Charset charset();
}
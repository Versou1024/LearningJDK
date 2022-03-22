package sun.management;

import java.io.File;
import java.io.IOException;

public abstract class FileSystem
{
  private static final Object lock = new Object();
  private static FileSystem fs;

  public static FileSystem open()
  {
    synchronized (lock)
    {
      if (fs == null)
        fs = new FileSystemImpl();
      return fs;
    }
  }

  public abstract boolean supportsFileSecurity(File paramFile)
    throws IOException;

  public abstract boolean isAccessUserOnly(File paramFile)
    throws IOException;
}
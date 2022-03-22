package sun.misc;

import java.io.File;
import java.io.FilenameFilter;

public class JarFilter
  implements FilenameFilter
{
  public boolean accept(File paramFile, String paramString)
  {
    String str = paramString.toLowerCase();
    return ((str.endsWith(".jar")) || (str.endsWith(".zip")));
  }
}
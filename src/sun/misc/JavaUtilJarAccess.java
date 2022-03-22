package sun.misc;

import java.io.IOException;
import java.util.jar.JarFile;

public abstract interface JavaUtilJarAccess
{
  public abstract boolean jarFileHasClassPathAttribute(JarFile paramJarFile)
    throws IOException;
}
package sun.net.www.protocol.jar;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

public abstract interface URLJarFileCallBack
{
  public abstract JarFile retrieve(URL paramURL)
    throws IOException;
}
package sun.management;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import sun.security.action.LoadLibraryAction;

public class FileSystemImpl extends FileSystem
{
  public boolean supportsFileSecurity(File paramFile)
    throws IOException
  {
    return isSecuritySupported0(paramFile.getAbsolutePath());
  }

  public boolean isAccessUserOnly(File paramFile)
    throws IOException
  {
    String str = paramFile.getAbsolutePath();
    if (!(isSecuritySupported0(str)))
      throw new UnsupportedOperationException("File system does not support file security");
    return isAccessUserOnly0(str);
  }

  static native void init0();

  static native boolean isSecuritySupported0(String paramString)
    throws IOException;

  static native boolean isAccessUserOnly0(String paramString)
    throws IOException;

  static
  {
    AccessController.doPrivileged(new LoadLibraryAction("management"));
    init0();
  }
}
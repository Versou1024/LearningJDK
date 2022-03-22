package sun.security.action;

import java.io.File;
import java.io.FileInputStream;
import java.security.PrivilegedExceptionAction;

public class OpenFileInputStreamAction
  implements PrivilegedExceptionAction<FileInputStream>
{
  private final File file;

  public OpenFileInputStreamAction(File paramFile)
  {
    this.file = paramFile;
  }

  public OpenFileInputStreamAction(String paramString)
  {
    this.file = new File(paramString);
  }

  public FileInputStream run()
    throws Exception
  {
    return new FileInputStream(this.file);
  }
}
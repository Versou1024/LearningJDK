package sun.awt.shell;

import java.io.File;
import java.io.ObjectStreamException;

class DefaultShellFolder extends ShellFolder
{
  DefaultShellFolder(ShellFolder paramShellFolder, File paramFile)
  {
    super(paramShellFolder, paramFile.getAbsolutePath());
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return new File(getPath());
  }

  public File[] listFiles()
  {
    File[] arrayOfFile = super.listFiles();
    if (arrayOfFile != null)
      for (int i = 0; i < arrayOfFile.length; ++i)
        arrayOfFile[i] = new DefaultShellFolder(this, arrayOfFile[i]);
    return arrayOfFile;
  }

  public boolean isLink()
  {
    return false;
  }

  public boolean isHidden()
  {
    String str = getName();
    if (str.length() > 0)
      return (str.charAt(0) == '.');
    return false;
  }

  public ShellFolder getLinkLocation()
  {
    return null;
  }

  public String getDisplayName()
  {
    return getName();
  }

  public String getFolderType()
  {
    if (isDirectory())
      return "File Folder";
    return "File";
  }

  public String getExecutableType()
  {
    return null;
  }
}
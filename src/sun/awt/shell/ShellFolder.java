package sun.awt.shell;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.net.URI;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

public abstract class ShellFolder extends File
{
  protected ShellFolder parent;
  private static ShellFolderManager shellFolderManager;
  private static Invoker invoker;

  ShellFolder(ShellFolder paramShellFolder, String paramString)
  {
    super((paramString != null) ? paramString : "ShellFolder");
    this.parent = paramShellFolder;
  }

  public boolean isFileSystem()
  {
    return (!(getPath().startsWith("ShellFolder")));
  }

  protected abstract Object writeReplace()
    throws ObjectStreamException;

  public String getParent()
  {
    if ((this.parent == null) && (isFileSystem()))
      return super.getParent();
    if (this.parent != null)
      return this.parent.getPath();
    return null;
  }

  public File getParentFile()
  {
    if (this.parent != null)
      return this.parent;
    if (isFileSystem())
      return super.getParentFile();
    return null;
  }

  public File[] listFiles()
  {
    return listFiles(true);
  }

  public File[] listFiles(boolean paramBoolean)
  {
    File[] arrayOfFile = super.listFiles();
    if (!(paramBoolean))
    {
      Vector localVector = new Vector();
      int i = (arrayOfFile == null) ? 0 : arrayOfFile.length;
      for (int j = 0; j < i; ++j)
        if (!(arrayOfFile[j].isHidden()))
          localVector.addElement(arrayOfFile[j]);
      arrayOfFile = (File[])(File[])localVector.toArray(new File[localVector.size()]);
    }
    return arrayOfFile;
  }

  public abstract boolean isLink();

  public abstract ShellFolder getLinkLocation()
    throws FileNotFoundException;

  public abstract String getDisplayName();

  public abstract String getFolderType();

  public abstract String getExecutableType();

  public int compareTo(File paramFile)
  {
    if ((paramFile == null) || (!(paramFile instanceof ShellFolder)) || ((paramFile instanceof ShellFolder) && (((ShellFolder)paramFile).isFileSystem())))
    {
      if (isFileSystem())
        return super.compareTo(paramFile);
      return -1;
    }
    if (isFileSystem())
      return 1;
    return getName().compareTo(paramFile.getName());
  }

  public Image getIcon(boolean paramBoolean)
  {
    return null;
  }

  public static ShellFolder getShellFolder(File paramFile)
    throws FileNotFoundException
  {
    if (paramFile instanceof ShellFolder)
      return ((ShellFolder)paramFile);
    if (!(paramFile.exists()))
      throw new FileNotFoundException();
    return shellFolderManager.createShellFolder(paramFile);
  }

  public static Object get(String paramString)
  {
    return shellFolderManager.get(paramString);
  }

  public static boolean isComputerNode(File paramFile)
  {
    return shellFolderManager.isComputerNode(paramFile);
  }

  public static boolean isFileSystemRoot(File paramFile)
  {
    return shellFolderManager.isFileSystemRoot(paramFile);
  }

  public static File getNormalizedFile(File paramFile)
    throws IOException
  {
    File localFile = paramFile.getCanonicalFile();
    if (paramFile.equals(localFile))
      return localFile;
    return new File(paramFile.toURI().normalize());
  }

  public static void sortFiles(List paramList)
  {
    shellFolderManager.sortFiles(paramList);
  }

  public boolean isAbsolute()
  {
    return ((!(isFileSystem())) || (super.isAbsolute()));
  }

  public File getAbsoluteFile()
  {
    return ((isFileSystem()) ? super.getAbsoluteFile() : this);
  }

  public boolean canRead()
  {
    return ((isFileSystem()) ? super.canRead() : true);
  }

  public boolean canWrite()
  {
    return ((isFileSystem()) ? super.canWrite() : false);
  }

  public boolean exists()
  {
    return ((!(isFileSystem())) || (isFileSystemRoot(this)) || (super.exists()));
  }

  public boolean isDirectory()
  {
    return ((isFileSystem()) ? super.isDirectory() : true);
  }

  public boolean isFile()
  {
    return ((!(isDirectory())) ? true : (isFileSystem()) ? super.isFile() : false);
  }

  public long lastModified()
  {
    return ((isFileSystem()) ? super.lastModified() : 3412047377152868352L);
  }

  public long length()
  {
    return ((isFileSystem()) ? super.length() : 3412047377152868352L);
  }

  public boolean createNewFile()
    throws IOException
  {
    return ((isFileSystem()) ? super.createNewFile() : false);
  }

  public boolean delete()
  {
    return ((isFileSystem()) ? super.delete() : false);
  }

  public void deleteOnExit()
  {
    if (isFileSystem())
      super.deleteOnExit();
  }

  public boolean mkdir()
  {
    return ((isFileSystem()) ? super.mkdir() : false);
  }

  public boolean mkdirs()
  {
    return ((isFileSystem()) ? super.mkdirs() : false);
  }

  public boolean renameTo(File paramFile)
  {
    return ((isFileSystem()) ? super.renameTo(paramFile) : false);
  }

  public boolean setLastModified(long paramLong)
  {
    return ((isFileSystem()) ? super.setLastModified(paramLong) : false);
  }

  public boolean setReadOnly()
  {
    return ((isFileSystem()) ? super.setReadOnly() : false);
  }

  public String toString()
  {
    return ((isFileSystem()) ? super.toString() : getDisplayName());
  }

  public static ShellFolderColumnInfo[] getFolderColumns(File paramFile)
  {
    return shellFolderManager.getFolderColumns(paramFile);
  }

  public static Object getFolderColumnValue(File paramFile, int paramInt)
  {
    return shellFolderManager.getFolderColumnValue(paramFile, paramInt);
  }

  public ShellFolderColumnInfo[] getFolderColumns()
  {
    return null;
  }

  public Object getFolderColumnValue(int paramInt)
  {
    return null;
  }

  public static Invoker getInvoker()
  {
    if (invoker == null)
      invoker = shellFolderManager.createInvoker();
    return invoker;
  }

  static
  {
    Object localObject = (Class)Toolkit.getDefaultToolkit().getDesktopProperty("Shell.shellFolderManager");
    if (localObject == null)
      localObject = ShellFolderManager.class;
    try
    {
      shellFolderManager = (ShellFolderManager)((Class)localObject).newInstance();
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new Error("Could not instantiate Shell Folder Manager: " + ((Class)localObject).getName());
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new Error("Could not access Shell Folder Manager: " + ((Class)localObject).getName());
    }
  }

  public static abstract interface Invoker
  {
    public abstract <T> T invoke(Callable<T> paramCallable);
  }
}
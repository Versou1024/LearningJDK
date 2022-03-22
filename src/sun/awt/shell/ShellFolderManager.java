package sun.awt.shell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

class ShellFolderManager
{
  private static final String COLUMN_NAME = "FileChooser.fileNameHeaderText";
  private static final String COLUMN_SIZE = "FileChooser.fileSizeHeaderText";
  private static final String COLUMN_DATE = "FileChooser.fileDateHeaderText";
  private Comparator fileComparator = new Comparator(this)
  {
    public int compare(, Object paramObject2)
    {
      return compare((File)paramObject1, (File)paramObject2);
    }

    public int compare(, File paramFile2)
    {
      ShellFolder localShellFolder1 = null;
      ShellFolder localShellFolder2 = null;
      if (paramFile1 instanceof ShellFolder)
      {
        localShellFolder1 = (ShellFolder)paramFile1;
        if (localShellFolder1.isFileSystem())
          localShellFolder1 = null;
      }
      if (paramFile2 instanceof ShellFolder)
      {
        localShellFolder2 = (ShellFolder)paramFile2;
        if (localShellFolder2.isFileSystem())
          localShellFolder2 = null;
      }
      if ((localShellFolder1 != null) && (localShellFolder2 != null))
        return localShellFolder1.compareTo(localShellFolder2);
      if (localShellFolder1 != null)
        return -1;
      if (localShellFolder2 != null)
        return 1;
      String str1 = paramFile1.getName();
      String str2 = paramFile2.getName();
      int i = str1.toLowerCase().compareTo(str2.toLowerCase());
      if (i != 0)
        return i;
      return str1.compareTo(str2);
    }
  };

  public ShellFolder createShellFolder(File paramFile)
    throws FileNotFoundException
  {
    return new DefaultShellFolder(null, paramFile);
  }

  public Object get(String paramString)
  {
    if (paramString.equals("fileChooserDefaultFolder"))
    {
      File localFile = new File(System.getProperty("user.home"));
      try
      {
        return createShellFolder(localFile);
      }
      catch (FileNotFoundException localFileNotFoundException)
      {
        return localFile;
      }
    }
    if (paramString.equals("roots"))
      return File.listRoots();
    if (paramString.equals("fileChooserComboBoxFolders"))
      return get("roots");
    if (paramString.equals("fileChooserShortcutPanelFolders"))
      return { (File)get("fileChooserDefaultFolder") };
    return null;
  }

  public boolean isComputerNode(File paramFile)
  {
    return false;
  }

  public boolean isFileSystemRoot(File paramFile)
  {
    if ((paramFile instanceof ShellFolder) && (!(((ShellFolder)paramFile).isFileSystem())))
      return false;
    return (paramFile.getParentFile() == null);
  }

  public void sortFiles(List paramList)
  {
    Collections.sort(paramList, this.fileComparator);
  }

  public ShellFolderColumnInfo[] getFolderColumns(File paramFile)
  {
    ShellFolderColumnInfo[] arrayOfShellFolderColumnInfo = null;
    if (paramFile instanceof ShellFolder)
      arrayOfShellFolderColumnInfo = ((ShellFolder)paramFile).getFolderColumns();
    if (arrayOfShellFolderColumnInfo == null)
      arrayOfShellFolderColumnInfo = { new ShellFolderColumnInfo("FileChooser.fileNameHeaderText", Integer.valueOf(150), Integer.valueOf(10), true, null, this.fileComparator), new ShellFolderColumnInfo("FileChooser.fileSizeHeaderText", Integer.valueOf(75), Integer.valueOf(4), true, null, ComparableComparator.getInstance(), true), new ShellFolderColumnInfo("FileChooser.fileDateHeaderText", Integer.valueOf(130), Integer.valueOf(10), true, null, ComparableComparator.getInstance(), true) };
    return arrayOfShellFolderColumnInfo;
  }

  public Object getFolderColumnValue(File paramFile, int paramInt)
  {
    if (paramFile instanceof ShellFolder)
    {
      Object localObject = ((ShellFolder)paramFile).getFolderColumnValue(paramInt);
      if (localObject != null)
        return localObject;
    }
    if ((paramFile == null) || (!(paramFile.exists())))
      return null;
    switch (paramInt)
    {
    case 0:
      return paramFile;
    case 1:
      return new Long(paramFile.length());
    case 2:
      if (isFileSystemRoot(paramFile))
        return null;
      long l = paramFile.lastModified();
      return new Date(l);
    }
    return null;
  }

  protected ShellFolder.Invoker createInvoker()
  {
    return new DirectInvoker(null);
  }

  private static class ComparableComparator
  implements Comparator
  {
    private static Comparator instance;

    public static Comparator getInstance()
    {
      if (instance == null)
        instance = new ComparableComparator();
      return instance;
    }

    public int compare(Object paramObject1, Object paramObject2)
    {
      int i;
      if ((paramObject1 == null) && (paramObject2 == null))
        i = 0;
      else if ((paramObject1 != null) && (paramObject2 == null))
        i = 1;
      else if ((paramObject1 == null) && (paramObject2 != null))
        i = -1;
      else if (paramObject1 instanceof Comparable)
        i = ((Comparable)paramObject1).compareTo(paramObject2);
      else
        i = 0;
      return i;
    }
  }

  private static class DirectInvoker
  implements ShellFolder.Invoker
  {
    public <T> T invoke(Callable<T> paramCallable)
    {
      try
      {
        return paramCallable.call();
      }
      catch (Exception localException)
      {
        throw new RuntimeException(localException);
      }
    }
  }
}
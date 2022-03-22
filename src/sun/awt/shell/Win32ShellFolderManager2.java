package sun.awt.shell;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import sun.security.action.LoadLibraryAction;

public class Win32ShellFolderManager2 extends ShellFolderManager
{
  private static Win32ShellFolder2 desktop;
  private static Win32ShellFolder2 drives;
  private static Win32ShellFolder2 recent;
  private static Win32ShellFolder2 network;
  private static Win32ShellFolder2 personal;
  private static String osVersion;
  private static final boolean useShell32Icons;
  private static File[] roots;
  private Comparator driveComparator = new Comparator(this)
  {
    public int compare(, Object paramObject2)
    {
      return ((ShellFolder)paramObject1).getPath().compareTo(((ShellFolder)paramObject2).getPath());
    }
  };
  private static List topFolderList;
  private Comparator fileComparator = new Comparator(this)
  {
    public int compare(, Object paramObject2)
    {
      return compare((File)paramObject1, (File)paramObject2);
    }

    public int compare(, File paramFile2)
    {
      return Win32ShellFolderManager2.compareFiles(paramFile1, paramFile2);
    }
  };

  public ShellFolder createShellFolder(File paramFile)
    throws FileNotFoundException
  {
    return createShellFolder(getDesktop(), paramFile);
  }

  static Win32ShellFolder2 createShellFolder(Win32ShellFolder2 paramWin32ShellFolder2, File paramFile)
    throws FileNotFoundException
  {
    long l = 3412047153814568960L;
    try
    {
      l = paramWin32ShellFolder2.parseDisplayName(paramFile.getCanonicalPath());
    }
    catch (IOException localIOException)
    {
      l = 3412047463052214272L;
    }
    if (l == 3412046672778231808L)
      throw new FileNotFoundException("File " + paramFile.getAbsolutePath() + " not found");
    Win32ShellFolder2 localWin32ShellFolder2 = createShellFolderFromRelativePIDL(paramWin32ShellFolder2, l);
    Win32ShellFolder2.releasePIDL(l);
    return localWin32ShellFolder2;
  }

  static Win32ShellFolder2 createShellFolderFromRelativePIDL(Win32ShellFolder2 paramWin32ShellFolder2, long paramLong)
  {
    while (paramLong != 3412046552519147520L)
    {
      long l = Win32ShellFolder2.copyFirstPIDLEntry(paramLong);
      if (l == 3412046999195746304L)
        break;
      paramWin32ShellFolder2 = new Win32ShellFolder2(paramWin32ShellFolder2, l);
      paramLong = Win32ShellFolder2.getNextPIDLEntry(paramLong);
    }
    return paramWin32ShellFolder2;
  }

  static Win32ShellFolder2 getDesktop()
  {
    if (desktop == null)
      try
      {
        desktop = new Win32ShellFolder2(0);
      }
      catch (IOException localIOException)
      {
        desktop = null;
      }
    return desktop;
  }

  static Win32ShellFolder2 getDrives()
  {
    if (drives == null)
      try
      {
        drives = new Win32ShellFolder2(17);
      }
      catch (IOException localIOException)
      {
        drives = null;
      }
    return drives;
  }

  static Win32ShellFolder2 getRecent()
  {
    if (recent == null)
      try
      {
        String str = Win32ShellFolder2.getFileSystemPath(8);
        if (str != null)
          recent = createShellFolder(getDesktop(), new File(str));
      }
      catch (IOException localIOException)
      {
        recent = null;
      }
    return recent;
  }

  static Win32ShellFolder2 getNetwork()
  {
    if (network == null)
      try
      {
        network = new Win32ShellFolder2(18);
      }
      catch (IOException localIOException)
      {
        network = null;
      }
    return network;
  }

  static Win32ShellFolder2 getPersonal()
  {
    if (personal == null)
      try
      {
        String str = Win32ShellFolder2.getFileSystemPath(5);
        if (str != null)
        {
          Win32ShellFolder2 localWin32ShellFolder2 = getDesktop();
          personal = localWin32ShellFolder2.getChildByPath(str);
          if (personal == null)
            personal = createShellFolder(getDesktop(), new File(str));
          if (personal != null)
            personal.setIsPersonal();
        }
      }
      catch (IOException localIOException)
      {
        personal = null;
      }
    return personal;
  }

  public Object get(String paramString)
  {
    Object localObject1;
    Object localObject2;
    Object localObject3;
    if (paramString.equals("fileChooserDefaultFolder"))
    {
      localObject1 = getPersonal();
      if (localObject1 == null)
        localObject1 = getDesktop();
      return localObject1;
    }
    if (paramString.equals("roots"))
    {
      if (roots == null)
      {
        localObject1 = getDesktop();
        if (localObject1 != null)
          roots = { localObject1 };
        else
          roots = (File[])(File[])super.get(paramString);
      }
      return roots;
    }
    if (paramString.equals("fileChooserComboBoxFolders"))
    {
      localObject1 = getDesktop();
      if (localObject1 != null)
      {
        localObject2 = new ArrayList();
        Win32ShellFolder2 localWin32ShellFolder21 = getDrives();
        ((ArrayList)localObject2).add(localObject1);
        localObject3 = ((Win32ShellFolder2)localObject1).listFiles();
        Arrays.sort(localObject3);
        for (int l = 0; l < localObject3.length; ++l)
        {
          Win32ShellFolder2 localWin32ShellFolder22 = (Win32ShellFolder2)localObject3[l];
          if ((!(localWin32ShellFolder22.isFileSystem())) || (localWin32ShellFolder22.isDirectory()))
          {
            ((ArrayList)localObject2).add(localWin32ShellFolder22);
            if (localWin32ShellFolder22.equals(localWin32ShellFolder21))
            {
              File[] arrayOfFile2 = localWin32ShellFolder22.listFiles();
              if (arrayOfFile2 != null)
              {
                Arrays.sort(arrayOfFile2, this.driveComparator);
                for (int i3 = 0; i3 < arrayOfFile2.length; ++i3)
                  ((ArrayList)localObject2).add(arrayOfFile2[i3]);
              }
            }
          }
        }
        return ((ArrayList)localObject2).toArray(new File[((ArrayList)localObject2).size()]);
      }
      return super.get(paramString);
    }
    if (paramString.equals("fileChooserShortcutPanelFolders"))
    {
      localObject1 = Toolkit.getDefaultToolkit();
      localObject2 = new ArrayList();
      int k = 0;
      do
      {
        localObject3 = ((Toolkit)localObject1).getDesktopProperty("win.comdlg.placesBarPlace" + (k++));
        try
        {
          if (localObject3 instanceof Integer)
            ((ArrayList)localObject2).add(new Win32ShellFolder2(((Integer)localObject3).intValue()));
          else if (localObject3 instanceof String)
            ((ArrayList)localObject2).add(createShellFolder(new File((String)localObject3)));
        }
        catch (IOException localIOException)
        {
        }
      }
      while (localObject3 != null);
      if (((ArrayList)localObject2).size() == 0)
      {
        File[] arrayOfFile1 = { getRecent(), getDesktop(), getPersonal(), getDrives(), getNetwork() };
        int i1 = arrayOfFile1.length;
        for (int i2 = 0; i2 < i1; ++i2)
        {
          File localFile = arrayOfFile1[i2];
          if (localFile != null)
            ((ArrayList)localObject2).add(localFile);
        }
      }
      return ((ArrayList)localObject2).toArray(new File[((ArrayList)localObject2).size()]);
    }
    if (paramString.startsWith("fileChooserIcon "))
    {
      int i = -1;
      localObject2 = paramString.substring(paramString.indexOf(" ") + 1);
      try
      {
        i = Integer.parseInt((String)localObject2);
      }
      catch (NumberFormatException localNumberFormatException1)
      {
        if (((String)localObject2).equals("ListView"))
          i = (useShell32Icons) ? 21 : 2;
        else if (((String)localObject2).equals("DetailsView"))
          i = (useShell32Icons) ? 23 : 3;
        else if (((String)localObject2).equals("UpFolder"))
          i = (useShell32Icons) ? 28 : 8;
        else if (((String)localObject2).equals("NewFolder"))
          i = (useShell32Icons) ? 31 : 11;
      }
      if (i >= 0)
        return Win32ShellFolder2.getFileChooserIcon(i);
    }
    else
    {
      if (paramString.startsWith("optionPaneIcon "))
      {
        Win32ShellFolder2.SystemIcon localSystemIcon;
        if (paramString == "optionPaneIcon Error")
          localSystemIcon = Win32ShellFolder2.SystemIcon.IDI_ERROR;
        else if (paramString == "optionPaneIcon Information")
          localSystemIcon = Win32ShellFolder2.SystemIcon.IDI_INFORMATION;
        else if (paramString == "optionPaneIcon Question")
          localSystemIcon = Win32ShellFolder2.SystemIcon.IDI_QUESTION;
        else if (paramString == "optionPaneIcon Warning")
          localSystemIcon = Win32ShellFolder2.SystemIcon.IDI_EXCLAMATION;
        else
          return null;
        return Win32ShellFolder2.getSystemIcon(localSystemIcon);
      }
      if (paramString.startsWith("shell32Icon "))
      {
        int j = -1;
        localObject2 = paramString.substring(paramString.indexOf(" ") + 1);
        try
        {
          j = Integer.parseInt((String)localObject2);
          if (j >= 0)
            return Win32ShellFolder2.getShell32Icon(j);
        }
        catch (NumberFormatException localNumberFormatException2)
        {
        }
      }
    }
    return null;
  }

  public boolean isComputerNode(File paramFile)
  {
    if ((paramFile != null) && (paramFile == getDrives()))
      return true;
    String str = paramFile.getAbsolutePath();
    return ((str.startsWith("\\\\")) && (str.indexOf("\\", 2) < 0));
  }

  public boolean isFileSystemRoot(File paramFile)
  {
    if (paramFile != null)
    {
      Win32ShellFolder2 localWin32ShellFolder2 = getDrives();
      if (paramFile instanceof Win32ShellFolder2)
      {
        localObject = (Win32ShellFolder2)paramFile;
        if (((Win32ShellFolder2)localObject).isFileSystem())
        {
          if (((Win32ShellFolder2)localObject).parent == null)
            break label45;
          return ((Win32ShellFolder2)localObject).parent.equals(localWin32ShellFolder2);
        }
        return false;
      }
      label45: Object localObject = paramFile.getPath();
      return ((((String)localObject).length() == 3) && (((String)localObject).charAt(1) == ':') && (Arrays.asList(localWin32ShellFolder2.listFiles()).contains(paramFile)));
    }
    return false;
  }

  public void sortFiles(List paramList)
  {
    Collections.sort(paramList, this.fileComparator);
  }

  static int compareShellFolders(Win32ShellFolder2 paramWin32ShellFolder21, Win32ShellFolder2 paramWin32ShellFolder22)
  {
    boolean bool1 = paramWin32ShellFolder21.isSpecial();
    boolean bool2 = paramWin32ShellFolder22.isSpecial();
    if ((bool1) || (bool2))
    {
      if (topFolderList == null)
      {
        ArrayList localArrayList = new ArrayList();
        localArrayList.add(getPersonal());
        localArrayList.add(getDesktop());
        localArrayList.add(getDrives());
        localArrayList.add(getNetwork());
        topFolderList = localArrayList;
      }
      int i = topFolderList.indexOf(paramWin32ShellFolder21);
      int j = topFolderList.indexOf(paramWin32ShellFolder22);
      if ((i >= 0) && (j >= 0))
        return (i - j);
      if (i >= 0)
        return -1;
      if (j >= 0)
        return 1;
    }
    if ((bool1) && (!(bool2)))
      return -1;
    if ((bool2) && (!(bool1)))
      return 1;
    return compareNames(paramWin32ShellFolder21.getAbsolutePath(), paramWin32ShellFolder22.getAbsolutePath());
  }

  static int compareFiles(File paramFile1, File paramFile2)
  {
    if (paramFile1 instanceof Win32ShellFolder2)
      return paramFile1.compareTo(paramFile2);
    if (paramFile2 instanceof Win32ShellFolder2)
      return (-1 * paramFile2.compareTo(paramFile1));
    return compareNames(paramFile1.getName(), paramFile2.getName());
  }

  static int compareNames(String paramString1, String paramString2)
  {
    int i = paramString1.toLowerCase().compareTo(paramString2.toLowerCase());
    if (i != 0)
      return i;
    return paramString1.compareTo(paramString2);
  }

  protected ShellFolder.Invoker createInvoker()
  {
    return new ComInvoker(null);
  }

  static native void initializeCom();

  static native void uninitializeCom();

  static
  {
    AccessController.doPrivileged(new LoadLibraryAction("awt"));
    osVersion = System.getProperty("os.version");
    useShell32Icons = (osVersion != null) && (osVersion.compareTo("5.1") >= 0);
    topFolderList = null;
  }

  private static class ComInvoker extends ThreadPoolExecutor
  implements ThreadFactory, ShellFolder.Invoker
  {
    private static Thread comThread;

    private ComInvoker()
    {
      super(1, 1, 3412047806649597952L, TimeUnit.DAYS, new LinkedBlockingQueue());
      allowCoreThreadTimeOut(false);
      setThreadFactory(this);
      1 local1 = new Runnable(this)
      {
        public void run()
        {
          AccessController.doPrivileged(new PrivilegedAction(this)
          {
            public Void run()
            {
              this.this$1.this$0.shutdownNow();
              return null;
            }
          });
        }
      };
      AccessController.doPrivileged(new PrivilegedAction(this, local1)
      {
        public Void run()
        {
          Runtime.getRuntime().addShutdownHook(new Thread(this.val$shutdownHook));
          return null;
        }
      });
    }

    public synchronized Thread newThread(Runnable paramRunnable)
    {
      3 local3 = new Runnable(this, paramRunnable)
      {
        public void run()
        {
          try
          {
            Win32ShellFolderManager2.initializeCom();
            this.val$task.run();
          }
          finally
          {
            Win32ShellFolderManager2.uninitializeCom();
          }
        }
      };
      comThread = (Thread)AccessController.doPrivileged(new PrivilegedAction(this, local3)
      {
        public Thread run()
        {
          Object localObject1 = Thread.currentThread().getThreadGroup();
          for (Object localObject2 = localObject1; localObject2 != null; localObject2 = ((ThreadGroup)localObject1).getParent())
            localObject1 = localObject2;
          localObject2 = new Thread((ThreadGroup)localObject1, this.val$comRun, "Swing-Shell");
          ((Thread)localObject2).setDaemon(true);
          return ((Thread)(Thread)localObject2);
        }
      });
      return comThread;
    }

    public <T> T invoke(Callable<T> paramCallable)
    {
      Object localObject2;
      try
      {
        Object localObject1;
        if (Thread.currentThread() == comThread)
        {
          localObject1 = paramCallable.call();
        }
        else
        {
          localObject2 = submit(paramCallable);
          try
          {
            localObject1 = ((Future)localObject2).get();
          }
          catch (InterruptedException localInterruptedException)
          {
            localObject1 = null;
            ((Future)localObject2).cancel(true);
          }
        }
        return localObject1;
      }
      catch (Exception localException)
      {
        localObject2 = (localException instanceof java.util.concurrent.ExecutionException) ? localException.getCause() : localException;
        if (localObject2 instanceof RuntimeException)
          throw ((RuntimeException)localObject2);
        if (localObject2 instanceof Error)
          throw ((Error)localObject2);
        throw new RuntimeException((Throwable)localObject2);
      }
    }
  }
}
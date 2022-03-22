package sun.awt.shell;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

final class Win32ShellFolder2 extends ShellFolder
{
  private static final boolean is98;
  public static final int DESKTOP = 0;
  public static final int INTERNET = 1;
  public static final int PROGRAMS = 2;
  public static final int CONTROLS = 3;
  public static final int PRINTERS = 4;
  public static final int PERSONAL = 5;
  public static final int FAVORITES = 6;
  public static final int STARTUP = 7;
  public static final int RECENT = 8;
  public static final int SENDTO = 9;
  public static final int BITBUCKET = 10;
  public static final int STARTMENU = 11;
  public static final int DESKTOPDIRECTORY = 16;
  public static final int DRIVES = 17;
  public static final int NETWORK = 18;
  public static final int NETHOOD = 19;
  public static final int FONTS = 20;
  public static final int TEMPLATES = 21;
  public static final int COMMON_STARTMENU = 22;
  public static final int COMMON_PROGRAMS = 23;
  public static final int COMMON_STARTUP = 24;
  public static final int COMMON_DESKTOPDIRECTORY = 25;
  public static final int APPDATA = 26;
  public static final int PRINTHOOD = 27;
  public static final int ALTSTARTUP = 29;
  public static final int COMMON_ALTSTARTUP = 30;
  public static final int COMMON_FAVORITES = 31;
  public static final int INTERNET_CACHE = 32;
  public static final int COOKIES = 33;
  public static final int HISTORY = 34;
  public static final int ATTRIB_CANCOPY = 1;
  public static final int ATTRIB_CANMOVE = 2;
  public static final int ATTRIB_CANLINK = 4;
  public static final int ATTRIB_CANRENAME = 16;
  public static final int ATTRIB_CANDELETE = 32;
  public static final int ATTRIB_HASPROPSHEET = 64;
  public static final int ATTRIB_DROPTARGET = 256;
  public static final int ATTRIB_LINK = 65536;
  public static final int ATTRIB_SHARE = 131072;
  public static final int ATTRIB_READONLY = 262144;
  public static final int ATTRIB_GHOSTED = 524288;
  public static final int ATTRIB_HIDDEN = 524288;
  public static final int ATTRIB_FILESYSANCESTOR = 268435456;
  public static final int ATTRIB_FOLDER = 536870912;
  public static final int ATTRIB_FILESYSTEM = 1073741824;
  public static final int ATTRIB_HASSUBFOLDER = -2147483648;
  public static final int ATTRIB_VALIDATE = 16777216;
  public static final int ATTRIB_REMOVABLE = 33554432;
  public static final int ATTRIB_COMPRESSED = 67108864;
  public static final int ATTRIB_BROWSABLE = 134217728;
  public static final int ATTRIB_NONENUMERATED = 1048576;
  public static final int ATTRIB_NEWCONTENT = 2097152;
  public static final int SHGDN_NORMAL = 0;
  public static final int SHGDN_INFOLDER = 1;
  public static final int SHGDN_INCLUDE_NONFILESYS = 8192;
  public static final int SHGDN_FORADDRESSBAR = 16384;
  public static final int SHGDN_FORPARSING = 32768;
  FolderDisposer disposer = new FolderDisposer();
  private long pIShellIcon = -1L;
  private String folderType = null;
  private String displayName = null;
  private Image smallIcon = null;
  private Image largeIcon = null;
  private Boolean isDir = null;
  private boolean isPersonal;
  private Boolean cachedIsFileSystem;
  private Boolean cachedIsLink;
  private static Map smallSystemImages;
  private static Map largeSystemImages;
  private static Map smallLinkedSystemImages;
  private static Map largeLinkedSystemImages;
  static int[] fileChooserBitmapBits;
  static Image[] fileChooserIcons;
  private List topFolderList = null;
  private static final int LVCFMT_LEFT = 0;
  private static final int LVCFMT_RIGHT = 1;
  private static final int LVCFMT_CENTER = 2;

  private static native void initIDs();

  private void setIShellFolder(long paramLong)
  {
    this.disposer.pIShellFolder = paramLong;
  }

  private void setRelativePIDL(long paramLong)
  {
    this.disposer.relativePIDL = paramLong;
  }

  private static String composePathForCsidl(int paramInt)
    throws IOException
  {
    String str = getFileSystemPath(paramInt);
    return ((str == null) ? "ShellFolder: 0x" + Integer.toHexString(paramInt) : str);
  }

  Win32ShellFolder2(int paramInt)
    throws IOException
  {
    super(null, composePathForCsidl(paramInt));
    ShellFolder.getInvoker().invoke(new Callable(this, paramInt)
    {
      public Void call()
        throws Exception
      {
        if (this.val$csidl == 0)
        {
          Win32ShellFolder2.access$100(this.this$0);
        }
        else
        {
          Win32ShellFolder2.access$200(this.this$0, this.this$0.getDesktop().getIShellFolder(), this.val$csidl);
          long l1 = this.this$0.disposer.relativePIDL;
          this.this$0.parent = this.this$0.getDesktop();
          while (l1 != 3412041793695383552L)
          {
            long l2 = Win32ShellFolder2.copyFirstPIDLEntry(l1);
            if (l2 == 3412042102933028864L)
              break;
            l1 = Win32ShellFolder2.getNextPIDLEntry(l1);
            if (l1 != 3412042309091459072L)
              this.this$0.parent = new Win32ShellFolder2((Win32ShellFolder2)this.this$0.parent, l2);
            else
              this.this$0.disposer.relativePIDL = l2;
          }
        }
        return null;
      }
    });
    Disposer.addRecord(this, this.disposer);
  }

  Win32ShellFolder2(Win32ShellFolder2 paramWin32ShellFolder2, long paramLong1, long paramLong2, String paramString)
  {
    super(paramWin32ShellFolder2, (paramString != null) ? paramString : "ShellFolder: ");
    this.disposer.pIShellFolder = paramLong1;
    this.disposer.relativePIDL = paramLong2;
    Disposer.addRecord(this, this.disposer);
  }

  Win32ShellFolder2(Win32ShellFolder2 paramWin32ShellFolder2, long paramLong)
  {
    super(paramWin32ShellFolder2, (String)ShellFolder.getInvoker().invoke(new Callable(paramWin32ShellFolder2, paramLong)
    {
      public String call()
        throws Exception
      {
        return Win32ShellFolder2.access$300(this.val$parent.getIShellFolder(), this.val$relativePIDL);
      }
    }));
    this.disposer.relativePIDL = paramLong;
    String str = getAbsolutePath();
    Disposer.addRecord(this, this.disposer);
  }

  private native void initDesktop();

  private native void initSpecial(long paramLong, int paramInt);

  public void setIsPersonal()
  {
    this.isPersonal = true;
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return ShellFolder.getInvoker().invoke(new Callable(this)
    {
      public File call()
        throws Exception
      {
        if (this.this$0.isFileSystem())
          return new File(this.this$0.getPath());
        Win32ShellFolder2 localWin32ShellFolder21 = Win32ShellFolderManager2.getDrives();
        if (localWin32ShellFolder21 != null)
        {
          File[] arrayOfFile = localWin32ShellFolder21.listFiles();
          if (arrayOfFile != null)
            for (int i = 0; i < arrayOfFile.length; ++i)
              if (arrayOfFile[i] instanceof Win32ShellFolder2)
              {
                Win32ShellFolder2 localWin32ShellFolder22 = (Win32ShellFolder2)arrayOfFile[i];
                if ((localWin32ShellFolder22.isFileSystem()) && (!(localWin32ShellFolder22.hasAttribute(33554432))))
                  return new File(localWin32ShellFolder22.getPath());
              }
        }
        return new File("C:\\");
      }
    });
  }

  protected void dispose()
  {
    this.disposer.dispose();
  }

  static native long getNextPIDLEntry(long paramLong);

  static native long copyFirstPIDLEntry(long paramLong);

  private static native long combinePIDLs(long paramLong1, long paramLong2);

  static native void releasePIDL(long paramLong);

  private static native void releaseIShellFolder(long paramLong);

  public long getIShellFolder()
  {
    if (this.disposer.pIShellFolder == 3412046810217185280L)
      this.disposer.pIShellFolder = ((Long)ShellFolder.getInvoker().invoke(new Callable(this)
      {
        public Long call()
          throws Exception
        {
          if ((!($assertionsDisabled)) && (!(this.this$0.isDirectory())))
            throw new AssertionError();
          if ((!($assertionsDisabled)) && (this.this$0.parent == null))
            throw new AssertionError();
          long l1 = this.this$0.getParentIShellFolder();
          if (l1 == 3412042961926488064L)
            throw new InternalError("Parent IShellFolder was null for " + this.this$0.getAbsolutePath());
          long l2 = Win32ShellFolder2.access$400(l1, this.this$0.disposer.relativePIDL);
          if (l2 == 3412042961926488064L)
            throw new InternalError("Unable to bind " + this.this$0.getAbsolutePath() + " to parent");
          return Long.valueOf(l2);
        }
      })).longValue();
    return this.disposer.pIShellFolder;
  }

  public long getParentIShellFolder()
  {
    Win32ShellFolder2 localWin32ShellFolder2 = (Win32ShellFolder2)getParentFile();
    if (localWin32ShellFolder2 == null)
      return getIShellFolder();
    return localWin32ShellFolder2.getIShellFolder();
  }

  public long getRelativePIDL()
  {
    if (this.disposer.relativePIDL == 3412046810217185280L)
      throw new InternalError("Should always have a relative PIDL");
    return this.disposer.relativePIDL;
  }

  private long getAbsolutePIDL()
  {
    if (this.parent == null)
      return getRelativePIDL();
    if (this.disposer.absolutePIDL == 3412046810217185280L)
      this.disposer.absolutePIDL = combinePIDLs(((Win32ShellFolder2)this.parent).getAbsolutePIDL(), getRelativePIDL());
    return this.disposer.absolutePIDL;
  }

  public Win32ShellFolder2 getDesktop()
  {
    return Win32ShellFolderManager2.getDesktop();
  }

  public long getDesktopIShellFolder()
  {
    return getDesktop().getIShellFolder();
  }

  private static boolean pathsEqual(String paramString1, String paramString2)
  {
    return paramString1.equalsIgnoreCase(paramString2);
  }

  public boolean equals(Object paramObject)
  {
    if ((paramObject == null) || (!(paramObject instanceof Win32ShellFolder2)))
    {
      if (!(paramObject instanceof File))
        return super.equals(paramObject);
      return pathsEqual(getPath(), ((File)paramObject).getPath());
    }
    Win32ShellFolder2 localWin32ShellFolder2 = (Win32ShellFolder2)paramObject;
    if (((this.parent == null) && (localWin32ShellFolder2.parent != null)) || ((this.parent != null) && (localWin32ShellFolder2.parent == null)))
      return false;
    if ((isFileSystem()) && (localWin32ShellFolder2.isFileSystem()))
      return ((pathsEqual(getPath(), localWin32ShellFolder2.getPath())) && (((this.parent == localWin32ShellFolder2.parent) || (this.parent.equals(localWin32ShellFolder2.parent)))));
    if ((this.parent == null) || (this.parent == localWin32ShellFolder2.parent) || (this.parent.equals(localWin32ShellFolder2.parent)))
      return pidlsEqual(getParentIShellFolder(), this.disposer.relativePIDL, localWin32ShellFolder2.disposer.relativePIDL);
    return false;
  }

  private static boolean pidlsEqual(long paramLong1, long paramLong2, long paramLong3)
  {
    return ((Boolean)ShellFolder.getInvoker().invoke(new Callable(paramLong1, paramLong2, paramLong3)
    {
      public Boolean call()
        throws Exception
      {
        return Boolean.valueOf(Win32ShellFolder2.access$500(this.val$pIShellFolder, this.val$pidl1, this.val$pidl2) == 0);
      }
    })).booleanValue();
  }

  private static native int compareIDs(long paramLong1, long paramLong2, long paramLong3);

  public synchronized boolean isFileSystem()
  {
    if (this.cachedIsFileSystem == null)
      this.cachedIsFileSystem = Boolean.valueOf(hasAttribute(1073741824));
    return this.cachedIsFileSystem.booleanValue();
  }

  public boolean hasAttribute(int paramInt)
  {
    return ((Boolean)ShellFolder.getInvoker().invoke(new Callable(this, paramInt)
    {
      public Boolean call()
        throws Exception
      {
        return Boolean.valueOf((Win32ShellFolder2.access$600(this.this$0.getParentIShellFolder(), this.this$0.getRelativePIDL(), this.val$attribute) & this.val$attribute) != 0);
      }
    })).booleanValue();
  }

  private static native int getAttributes0(long paramLong1, long paramLong2, int paramInt);

  private static String getFileSystemPath(long paramLong1, long paramLong2)
  {
    return ((String)ShellFolder.getInvoker().invoke(new Callable(paramLong1, paramLong2)
    {
      public String call()
        throws Exception
      {
        int i = 536936448;
        if ((this.val$parentIShellFolder == Win32ShellFolderManager2.getNetwork().getIShellFolder()) && (Win32ShellFolder2.access$600(this.val$parentIShellFolder, this.val$relativePIDL, i) == i))
        {
          String str = Win32ShellFolder2.access$300(Win32ShellFolderManager2.getDesktop().getIShellFolder(), Win32ShellFolder2.access$700(this.val$parentIShellFolder, this.val$relativePIDL, false));
          if ((str != null) && (str.startsWith("\\\\")))
            return str;
        }
        return Win32ShellFolder2.access$800(this.val$parentIShellFolder, this.val$relativePIDL, 32768);
      }
    }));
  }

  static String getFileSystemPath(int paramInt)
    throws IOException
  {
    return ((String)ShellFolder.getInvoker().invoke(new Callable(paramInt)
    {
      public String call()
        throws Exception
      {
        return Win32ShellFolder2.access$900(this.val$csidl);
      }
    }));
  }

  private static native String getFileSystemPath0(int paramInt)
    throws IOException;

  private static boolean isNetworkRoot(String paramString)
  {
    return ((paramString.equals("\\\\")) || (paramString.equals("\\")) || (paramString.equals("//")) || (paramString.equals("/")));
  }

  public File getParentFile()
  {
    return this.parent;
  }

  public boolean isDirectory()
  {
    if (this.isDir == null)
      if ((hasAttribute(536870912)) && (((!(hasAttribute(134217728))) || ((is98) && (equals(Win32ShellFolderManager2.getPersonal()))))))
      {
        this.isDir = Boolean.TRUE;
      }
      else if (isLink())
      {
        ShellFolder localShellFolder = getLinkLocation(false);
        this.isDir = Boolean.valueOf((localShellFolder != null) && (localShellFolder.isDirectory()));
      }
      else
      {
        this.isDir = Boolean.FALSE;
      }
    return this.isDir.booleanValue();
  }

  private long getEnumObjects(long paramLong, boolean paramBoolean)
  {
    boolean bool = this.disposer.pIShellFolder == getDesktopIShellFolder();
    return ((Long)ShellFolder.getInvoker().invoke(new Callable(this, bool, paramBoolean)
    {
      public Long call()
        throws Exception
      {
        return Long.valueOf(Win32ShellFolder2.access$1000(this.this$0, this.this$0.disposer.pIShellFolder, this.val$isDesktop, this.val$includeHiddenFiles));
      }
    })).longValue();
  }

  private native long getEnumObjects(long paramLong, boolean paramBoolean1, boolean paramBoolean2);

  private native long getNextChild(long paramLong);

  private native void releaseEnumObjects(long paramLong);

  private static native long bindToObject(long paramLong1, long paramLong2);

  public File[] listFiles(boolean paramBoolean)
  {
    return ((File[])ShellFolder.getInvoker().invoke(new Callable(this, paramBoolean)
    {
      public File[] call()
        throws Exception
      {
        if (!(this.this$0.isDirectory()))
          return null;
        if ((this.this$0.isLink()) && (!(this.this$0.hasAttribute(536870912))))
          return new File[0];
        Win32ShellFolder2 localWin32ShellFolder21 = Win32ShellFolderManager2.getDesktop();
        Win32ShellFolder2 localWin32ShellFolder22 = Win32ShellFolderManager2.getPersonal();
        long l1 = this.this$0.getIShellFolder();
        ArrayList localArrayList = new ArrayList();
        long l2 = Win32ShellFolder2.access$1100(this.this$0, l1, this.val$includeHiddenFiles);
        if (l2 != 3412041673436299264L)
        {
          long l3 = 3412042652688842752L;
          int i = 1342177280;
          do
          {
            l3 = Win32ShellFolder2.access$1200(this.this$0, l2);
            int j = 1;
            if ((l3 != 3412042961926488064L) && ((Win32ShellFolder2.access$600(l1, l3, i) & i) != 0))
            {
              Win32ShellFolder2 localWin32ShellFolder23 = null;
              if ((this.this$0.equals(localWin32ShellFolder21)) && (localWin32ShellFolder22 != null) && (Win32ShellFolder2.access$1300(l1, l3, localWin32ShellFolder22.disposer.relativePIDL)))
              {
                localWin32ShellFolder23 = localWin32ShellFolder22;
              }
              else
              {
                localWin32ShellFolder23 = new Win32ShellFolder2(this.this$0, l3);
                j = 0;
              }
              localArrayList.add(localWin32ShellFolder23);
            }
            if (j != 0)
              Win32ShellFolder2.releasePIDL(l3);
          }
          while ((l3 != 3412042463710281728L) && (!(Thread.currentThread().isInterrupted())));
          Win32ShellFolder2.access$1400(this.this$0, l2);
        }
        return ((Thread.currentThread().isInterrupted()) ? new File[0] : (ShellFolder[])(ShellFolder[])localArrayList.toArray(new ShellFolder[localArrayList.size()]));
      }
    }));
  }

  Win32ShellFolder2 getChildByPath(String paramString)
  {
    return ((Win32ShellFolder2)ShellFolder.getInvoker().invoke(new Callable(this, paramString)
    {
      public Win32ShellFolder2 call()
        throws Exception
      {
        long l1 = this.this$0.getIShellFolder();
        long l2 = Win32ShellFolder2.access$1100(this.this$0, l1, true);
        Win32ShellFolder2 localWin32ShellFolder2 = null;
        long l3 = 3412042154472636416L;
        while ((l3 = Win32ShellFolder2.access$1200(this.this$0, l2)) != 3412041690616168448L)
        {
          if (Win32ShellFolder2.access$600(l1, l3, 1073741824) != 0)
          {
            String str = Win32ShellFolder2.access$300(l1, l3);
            if ((str != null) && (str.equalsIgnoreCase(this.val$filePath)))
            {
              long l4 = Win32ShellFolder2.access$400(l1, l3);
              localWin32ShellFolder2 = new Win32ShellFolder2(this.this$0, l4, l3, str);
              break;
            }
          }
          Win32ShellFolder2.releasePIDL(l3);
        }
        Win32ShellFolder2.access$1400(this.this$0, l2);
        return localWin32ShellFolder2;
      }
    }));
  }

  public synchronized boolean isLink()
  {
    if (this.cachedIsLink == null)
      this.cachedIsLink = Boolean.valueOf(hasAttribute(65536));
    return this.cachedIsLink.booleanValue();
  }

  public boolean isHidden()
  {
    return hasAttribute(524288);
  }

  private static native long getLinkLocation(long paramLong1, long paramLong2, boolean paramBoolean);

  public ShellFolder getLinkLocation()
  {
    return getLinkLocation(true);
  }

  private ShellFolder getLinkLocation(boolean paramBoolean)
  {
    return ((ShellFolder)ShellFolder.getInvoker().invoke(new Callable(this, paramBoolean)
    {
      public ShellFolder call()
        throws Exception
      {
        if (!(this.this$0.isLink()))
          return null;
        Win32ShellFolder2 localWin32ShellFolder2 = null;
        long l = Win32ShellFolder2.access$700(this.this$0.getParentIShellFolder(), this.this$0.getRelativePIDL(), this.val$resolve);
        if (l != 3412041673436299264L)
          try
          {
            localWin32ShellFolder2 = Win32ShellFolderManager2.createShellFolderFromRelativePIDL(this.this$0.getDesktop(), l);
          }
          catch (InternalError localInternalError)
          {
          }
        return localWin32ShellFolder2;
      }
    }));
  }

  long parseDisplayName(String paramString)
    throws FileNotFoundException
  {
    try
    {
      return ((Long)ShellFolder.getInvoker().invoke(new Callable(this, paramString)
      {
        public Long call()
          throws Exception
        {
          return Long.valueOf(Win32ShellFolder2.access$1500(this.this$0.getIShellFolder(), this.val$name));
        }
      })).longValue();
    }
    catch (RuntimeException localRuntimeException)
    {
      if (localRuntimeException.getCause() instanceof IOException)
        throw new FileNotFoundException("Could not find file " + paramString);
      throw localRuntimeException;
    }
  }

  private static native long parseDisplayName0(long paramLong, String paramString)
    throws IOException;

  private static native String getDisplayNameOf(long paramLong1, long paramLong2, int paramInt);

  public String getDisplayName()
  {
    if (this.displayName == null)
      this.displayName = ((String)ShellFolder.getInvoker().invoke(new Callable(this)
      {
        public String call()
          throws Exception
        {
          return Win32ShellFolder2.access$800(this.this$0.getParentIShellFolder(), this.this$0.getRelativePIDL(), 0);
        }
      }));
    return this.displayName;
  }

  private static native String getFolderType(long paramLong);

  public String getFolderType()
  {
    if (this.folderType == null)
    {
      long l = getAbsolutePIDL();
      this.folderType = ((String)ShellFolder.getInvoker().invoke(new Callable(this, l)
      {
        public String call()
          throws Exception
        {
          return Win32ShellFolder2.access$1600(this.val$absolutePIDL);
        }
      }));
    }
    return this.folderType;
  }

  private native String getExecutableType(String paramString);

  public String getExecutableType()
  {
    if (!(isFileSystem()))
      return null;
    return getExecutableType(getAbsolutePath());
  }

  private static native long getIShellIcon(long paramLong);

  private static native int getIconIndex(long paramLong1, long paramLong2);

  private static native long getIcon(String paramString, boolean paramBoolean);

  private static native long extractIcon(long paramLong1, long paramLong2, boolean paramBoolean);

  private static native long getSystemIcon(int paramInt);

  private static native long getIconResource(String paramString, int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean);

  private static native int[] getIconBits(long paramLong, int paramInt);

  private static native void disposeIcon(long paramLong);

  public static native int[] getFileChooserBitmapBits();

  private long getIShellIcon()
  {
    if (this.pIShellIcon == -1L)
      this.pIShellIcon = ((Long)ShellFolder.getInvoker().invoke(new Callable(this)
      {
        public Long call()
          throws Exception
        {
          return Long.valueOf(Win32ShellFolder2.access$1700(this.this$0.getIShellFolder()));
        }
      })).longValue();
    return this.pIShellIcon;
  }

  static Image getFileChooserIcon(int paramInt)
  {
    if (fileChooserIcons[paramInt] != null)
      return fileChooserIcons[paramInt];
    if (fileChooserBitmapBits == null)
      fileChooserBitmapBits = getFileChooserBitmapBits();
    if (fileChooserBitmapBits != null)
    {
      int i = fileChooserBitmapBits.length / 256;
      int[] arrayOfInt = new int[256];
      for (int j = 0; j < 16; ++j)
        for (int k = 0; k < 16; ++k)
          arrayOfInt[(j * 16 + k)] = fileChooserBitmapBits[(j * i * 16 + paramInt * 16 + k)];
      BufferedImage localBufferedImage = new BufferedImage(16, 16, 2);
      localBufferedImage.setRGB(0, 0, 16, 16, arrayOfInt, 0, 16);
      fileChooserIcons[paramInt] = localBufferedImage;
    }
    return fileChooserIcons[paramInt];
  }

  private static Image makeIcon(long paramLong, boolean paramBoolean)
  {
    if ((paramLong != 3412046964836007936L) && (paramLong != -1L))
    {
      int i = (paramBoolean) ? 32 : 16;
      int[] arrayOfInt = getIconBits(paramLong, i);
      if (arrayOfInt != null)
      {
        BufferedImage localBufferedImage = new BufferedImage(i, i, 2);
        localBufferedImage.setRGB(0, 0, i, i, arrayOfInt, 0, i);
        return localBufferedImage;
      }
    }
    return null;
  }

  public Image getIcon(boolean paramBoolean)
  {
    Image localImage = (paramBoolean) ? this.largeIcon : this.smallIcon;
    if (localImage == null)
    {
      localImage = (Image)ShellFolder.getInvoker().invoke(new Callable(this, paramBoolean)
      {
        public Image call()
          throws Exception
        {
          long l1;
          Image localImage = null;
          if (this.this$0.isFileSystem())
          {
            l1 = (this.this$0.parent != null) ? Win32ShellFolder2.access$1800((Win32ShellFolder2)this.this$0.parent) : 3412043580401778688L;
            long l2 = this.this$0.getRelativePIDL();
            int i = Win32ShellFolder2.access$1900(l1, l2);
            if (i > 0)
            {
              Map localMap;
              if (this.this$0.isLink())
                localMap = (this.val$getLargeIcon) ? Win32ShellFolder2.access$2000() : Win32ShellFolder2.access$2100();
              else
                localMap = (this.val$getLargeIcon) ? Win32ShellFolder2.access$2200() : Win32ShellFolder2.access$2300();
              localImage = (Image)localMap.get(Integer.valueOf(i));
              if (localImage == null)
              {
                long l3 = Win32ShellFolder2.access$2400(this.this$0.getAbsolutePath(), this.val$getLargeIcon);
                localImage = Win32ShellFolder2.access$2500(l3, this.val$getLargeIcon);
                Win32ShellFolder2.access$2600(l3);
                if (localImage != null)
                  localMap.put(Integer.valueOf(i), localImage);
              }
            }
          }
          if (localImage == null)
          {
            l1 = Win32ShellFolder2.access$2700(this.this$0.getParentIShellFolder(), this.this$0.getRelativePIDL(), this.val$getLargeIcon);
            localImage = Win32ShellFolder2.access$2500(l1, this.val$getLargeIcon);
            Win32ShellFolder2.access$2600(l1);
          }
          if (localImage == null)
            localImage = Win32ShellFolder2.access$2801(this.this$0, this.val$getLargeIcon);
          return localImage;
        }
      });
      if (paramBoolean)
        this.largeIcon = localImage;
      else
        this.smallIcon = localImage;
    }
    return localImage;
  }

  static Image getSystemIcon(SystemIcon paramSystemIcon)
  {
    long l = getSystemIcon(paramSystemIcon.getIconID());
    Image localImage = makeIcon(l, true);
    disposeIcon(l);
    return localImage;
  }

  static Image getShell32Icon(int paramInt)
  {
    boolean bool = true;
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    String str = (String)localToolkit.getDesktopProperty("win.icon.shellIconBPP");
    if (str != null)
      bool = str.equals("4");
    long l = getIconResource("shell32.dll", paramInt, 16, 16, bool);
    if (l != 3412046810217185280L)
    {
      Image localImage = makeIcon(l, false);
      disposeIcon(l);
      return localImage;
    }
    return null;
  }

  public File getCanonicalFile()
    throws IOException
  {
    return this;
  }

  public boolean isSpecial()
  {
    return ((this.isPersonal) || (!(isFileSystem())) || (this == getDesktop()));
  }

  public int compareTo(File paramFile)
  {
    if (!(paramFile instanceof Win32ShellFolder2))
    {
      if ((isFileSystem()) && (!(isSpecial())))
        return super.compareTo(paramFile);
      return -1;
    }
    return Win32ShellFolderManager2.compareShellFolders(this, (Win32ShellFolder2)paramFile);
  }

  public ShellFolderColumnInfo[] getFolderColumns()
  {
    return ((ShellFolderColumnInfo[])ShellFolder.getInvoker().invoke(new Callable(this)
    {
      public ShellFolderColumnInfo[] call()
        throws Exception
      {
        ShellFolderColumnInfo[] arrayOfShellFolderColumnInfo = Win32ShellFolder2.access$2900(this.this$0, this.this$0.getIShellFolder());
        if (arrayOfShellFolderColumnInfo != null)
        {
          ArrayList localArrayList = new ArrayList();
          for (int i = 0; i < arrayOfShellFolderColumnInfo.length; ++i)
          {
            ShellFolderColumnInfo localShellFolderColumnInfo = arrayOfShellFolderColumnInfo[i];
            if (localShellFolderColumnInfo != null)
            {
              localShellFolderColumnInfo.setAlignment(Integer.valueOf((localShellFolderColumnInfo.getAlignment().intValue() == 2) ? 0 : (localShellFolderColumnInfo.getAlignment().intValue() == 1) ? 4 : 10));
              localShellFolderColumnInfo.setComparator(new Win32ShellFolder2.ColumnComparator(this.this$0, i));
              localArrayList.add(localShellFolderColumnInfo);
            }
          }
          arrayOfShellFolderColumnInfo = new ShellFolderColumnInfo[localArrayList.size()];
          localArrayList.toArray(arrayOfShellFolderColumnInfo);
        }
        return arrayOfShellFolderColumnInfo;
      }
    }));
  }

  public Object getFolderColumnValue(int paramInt)
  {
    return ShellFolder.getInvoker().invoke(new Callable(this, paramInt)
    {
      public Object call()
        throws Exception
      {
        return Win32ShellFolder2.access$3000(this.this$0, this.this$0.getParentIShellFolder(), this.this$0.getRelativePIDL(), this.val$column);
      }
    });
  }

  private native ShellFolderColumnInfo[] doGetColumnInfo(long paramLong);

  private native Object doGetColumnValue(long paramLong1, long paramLong2, int paramInt);

  private native int compareIDsByColumn(long paramLong1, long paramLong2, long paramLong3, int paramInt);

  static
  {
    String str = System.getProperty("os.name");
    is98 = (str != null) && (str.startsWith("Windows 98"));
    initIDs();
    smallSystemImages = new HashMap();
    largeSystemImages = new HashMap();
    smallLinkedSystemImages = new HashMap();
    largeLinkedSystemImages = new HashMap();
    fileChooserBitmapBits = null;
    fileChooserIcons = new Image[47];
  }

  private class ColumnComparator
  implements Comparator
  {
    private final int columnIdx;

    public ColumnComparator(, int paramInt)
    {
      this.columnIdx = paramInt;
    }

    public int compare(, Object paramObject2)
    {
      return ((Integer)ShellFolder.getInvoker().invoke(new Callable(this, paramObject1, paramObject2)
      {
        public Integer call()
          throws Exception
        {
          if ((this.val$o instanceof Win32ShellFolder2) && (this.val$o1 instanceof Win32ShellFolder2))
            return Integer.valueOf(Win32ShellFolder2.access$3200(this.this$1.this$0, this.this$1.this$0.getIShellFolder(), ((Win32ShellFolder2)this.val$o).getRelativePIDL(), ((Win32ShellFolder2)this.val$o1).getRelativePIDL(), Win32ShellFolder2.ColumnComparator.access$3100(this.this$1)));
          return Integer.valueOf(0);
        }
      })).intValue();
    }
  }

  static class FolderDisposer
  implements DisposerRecord
  {
    long absolutePIDL;
    long pIShellFolder;
    long relativePIDL;
    boolean disposed;

    public void dispose()
    {
      if (this.disposed)
        return;
      ShellFolder.getInvoker().invoke(new Callable(this)
      {
        public Void call()
          throws Exception
        {
          if (this.this$0.relativePIDL != 3412041759335645184L)
            Win32ShellFolder2.releasePIDL(this.this$0.relativePIDL);
          if (this.this$0.absolutePIDL != 3412041759335645184L)
            Win32ShellFolder2.releasePIDL(this.this$0.absolutePIDL);
          if (this.this$0.pIShellFolder != 3412041759335645184L)
            Win32ShellFolder2.access$000(this.this$0.pIShellFolder);
          return null;
        }
      });
      this.disposed = true;
    }
  }

  public static enum SystemIcon
  {
    IDI_APPLICATION, IDI_HAND, IDI_ERROR, IDI_QUESTION, IDI_EXCLAMATION, IDI_WARNING, IDI_ASTERISK, IDI_INFORMATION, IDI_WINLOGO;

    private final int iconID;

    public int getIconID()
    {
      return this.iconID;
    }
  }
}
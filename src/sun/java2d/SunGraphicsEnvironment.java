package sun.java2d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.peer.ComponentPeer;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.awt.DisplayChangedListener;
import sun.awt.FontConfiguration;
import sun.awt.SunDisplayChanger;
import sun.font.CompositeFontDescriptor;
import sun.font.Font2D;
import sun.font.FontManager;
import sun.font.NativeFont;
import sun.font.PhysicalFont;

public abstract class SunGraphicsEnvironment extends GraphicsEnvironment
  implements FontSupport, DisplayChangedListener
{
  public static boolean isLinux;
  public static boolean isSolaris;
  public static boolean noType1Font;
  private static Font defaultFont;
  private static java.lang.String lucidaSansFileName;
  public static final java.lang.String lucidaFontName = "Lucida Sans Regular";
  public static boolean debugFonts = false;
  protected static Logger logger = null;
  private static ArrayList badFonts;
  public static java.lang.String jreLibDirName;
  public static java.lang.String jreFontDirName;
  private static HashSet<java.lang.String> missingFontFiles = null;
  private FontConfiguration fontConfig;
  protected java.lang.String fontPath;
  private boolean discoveredAllFonts = false;
  private boolean loadedAllFontFiles = false;
  protected HashSet registeredFontFiles = new HashSet();
  public static java.lang.String eudcFontFileName;
  protected GraphicsDevice[] screens;
  private Font[] allFonts;
  private static Locale systemLocale;
  private java.lang.String[] allFamilies;
  private Locale lastDefaultLocale;
  public static final TTFilter ttFilter;
  public static final T1Filter t1Filter;
  protected SunDisplayChanger displayChanger = new SunDisplayChanger();

  public SunGraphicsEnvironment()
  {
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        java.lang.String str1 = System.getProperty("os.name");
        if ("Linux".equals(str1))
          SunGraphicsEnvironment.isLinux = true;
        else if ("SunOS".equals(str1))
          SunGraphicsEnvironment.isSolaris = true;
        SunGraphicsEnvironment.noType1Font = "true".equals(System.getProperty("sun.java2d.noType1Font"));
        SunGraphicsEnvironment.jreLibDirName = System.getProperty("java.home", "") + File.separator + "lib";
        SunGraphicsEnvironment.jreFontDirName = SunGraphicsEnvironment.jreLibDirName + File.separator + "fonts";
        if (this.this$0.useAbsoluteFontFileNames())
          SunGraphicsEnvironment.access$002(SunGraphicsEnvironment.jreFontDirName + File.separator + "LucidaSansRegular.ttf");
        else
          SunGraphicsEnvironment.access$002("LucidaSansRegular.ttf");
        File localFile = new File(SunGraphicsEnvironment.jreFontDirName + File.separator + "badfonts.txt");
        if (localFile.exists())
        {
          localObject = null;
          try
          {
            SunGraphicsEnvironment.access$102(new ArrayList());
            localObject = new FileInputStream(localFile);
            InputStreamReader localInputStreamReader = new InputStreamReader((InputStream)localObject);
            BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader);
            while (true)
            {
              str2 = localBufferedReader.readLine();
              if (str2 == null)
                break;
              if (SunGraphicsEnvironment.debugFonts)
                SunGraphicsEnvironment.logger.warning("read bad font: " + str2);
              SunGraphicsEnvironment.access$100().add(str2);
            }
          }
          catch (IOException localIOException1)
          {
            try
            {
              if (localObject != null)
                ((FileInputStream)localObject).close();
            }
            catch (IOException localIOException2)
            {
            }
          }
        }
        if (SunGraphicsEnvironment.isLinux)
          this.this$0.registerFontDir(SunGraphicsEnvironment.jreFontDirName);
        SunGraphicsEnvironment.access$200(this.this$0, SunGraphicsEnvironment.jreFontDirName, true, 2, true, false);
        this.this$0.registerJREFontsWithPlatform(SunGraphicsEnvironment.jreFontDirName);
        SunGraphicsEnvironment.access$302(this.this$0, this.this$0.createFontConfiguration());
        this.this$0.getPlatformFontPathFromFontConfig();
        Object localObject = SunGraphicsEnvironment.access$300(this.this$0).getExtraFontPath();
        int i = 0;
        int j = 0;
        java.lang.String str2 = System.getProperty("sun.java2d.fontpath");
        if (str2 != null)
          if (str2.startsWith("prepend:"))
          {
            i = 1;
            str2 = str2.substring("prepend:".length());
          }
          else if (str2.startsWith("append:"))
          {
            j = 1;
            str2 = str2.substring("append:".length());
          }
        if (SunGraphicsEnvironment.debugFonts)
        {
          SunGraphicsEnvironment.logger.info("JRE font directory: " + SunGraphicsEnvironment.jreFontDirName);
          SunGraphicsEnvironment.logger.info("Extra font path: " + ((java.lang.String)localObject));
          SunGraphicsEnvironment.logger.info("Debug font path: " + str2);
        }
        if (str2 != null)
        {
          this.this$0.fontPath = this.this$0.getPlatformFontPath(SunGraphicsEnvironment.noType1Font);
          if (localObject != null)
            this.this$0.fontPath = ((java.lang.String)localObject) + File.pathSeparator + this.this$0.fontPath;
          if (j != 0)
            this.this$0.fontPath = this.this$0.fontPath + File.pathSeparator + str2;
          else if (i != 0)
            this.this$0.fontPath = str2 + File.pathSeparator + this.this$0.fontPath;
          else
            this.this$0.fontPath = str2;
          this.this$0.registerFontDirs(this.this$0.fontPath);
        }
        else if (localObject != null)
        {
          this.this$0.registerFontDirs((java.lang.String)localObject);
        }
        if ((SunGraphicsEnvironment.isSolaris) && (Locale.JAPAN.equals(Locale.getDefault())))
          this.this$0.registerFontDir("/usr/openwin/lib/locale/ja/X11/fonts/TT");
        SunGraphicsEnvironment.access$400(this.this$0, SunGraphicsEnvironment.access$300(this.this$0), null);
        SunGraphicsEnvironment.access$502(new Font("Dialog", 0, 12));
        return null;
      }
    });
  }

  public synchronized GraphicsDevice[] getScreenDevices()
  {
    GraphicsDevice[] arrayOfGraphicsDevice = this.screens;
    if (arrayOfGraphicsDevice == null)
    {
      int i = getNumScreens();
      arrayOfGraphicsDevice = new GraphicsDevice[i];
      for (int j = 0; j < i; ++j)
        arrayOfGraphicsDevice[j] = makeScreenDevice(j);
      this.screens = arrayOfGraphicsDevice;
    }
    return arrayOfGraphicsDevice;
  }

  protected abstract int getNumScreens();

  protected abstract GraphicsDevice makeScreenDevice(int paramInt);

  public GraphicsDevice getDefaultScreenDevice()
  {
    return getScreenDevices()[0];
  }

  public Graphics2D createGraphics(BufferedImage paramBufferedImage)
  {
    if (paramBufferedImage == null)
      throw new NullPointerException("BufferedImage cannot be null");
    SurfaceData localSurfaceData = SurfaceData.getDestSurfaceData(paramBufferedImage);
    return new SunGraphics2D(localSurfaceData, Color.white, Color.black, defaultFont);
  }

  protected java.lang.String getPlatformFontPath(boolean paramBoolean)
  {
    return FontManager.getFontPath(paramBoolean);
  }

  protected boolean useAbsoluteFontFileNames()
  {
    return true;
  }

  public java.lang.String getDefaultFontFile()
  {
    return lucidaSansFileName;
  }

  public java.lang.String getDefaultFontFaceName()
  {
    return "Lucida Sans Regular";
  }

  public void loadFonts()
  {
    if (this.discoveredAllFonts)
      return;
    synchronized ("Lucida Sans Regular")
    {
      if (debugFonts)
      {
        Thread.dumpStack();
        logger.info("SunGraphicsEnvironment.loadFonts() called");
      }
      FontManager.initialiseDeferredFonts();
      AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          if (this.this$0.fontPath == null)
          {
            this.this$0.fontPath = this.this$0.getPlatformFontPath(SunGraphicsEnvironment.noType1Font);
            this.this$0.registerFontDirs(this.this$0.fontPath);
          }
          if ((this.this$0.fontPath != null) && (!(FontManager.gotFontsFromPlatform())))
          {
            SunGraphicsEnvironment.access$600(this.this$0, this.this$0.fontPath, false, 6, false, true);
            SunGraphicsEnvironment.access$702(this.this$0, true);
          }
          FontManager.registerOtherFontFiles(this.this$0.registeredFontFiles);
          SunGraphicsEnvironment.access$802(this.this$0, true);
          return null;
        }
      });
    }
  }

  public void loadFontFiles()
  {
    loadFonts();
    if (this.loadedAllFontFiles)
      return;
    synchronized ("Lucida Sans Regular")
    {
      if (debugFonts)
      {
        Thread.dumpStack();
        logger.info("loadAllFontFiles() called");
      }
      AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          if (this.this$0.fontPath == null)
            this.this$0.fontPath = this.this$0.getPlatformFontPath(SunGraphicsEnvironment.noType1Font);
          if (this.this$0.fontPath != null)
            SunGraphicsEnvironment.access$600(this.this$0, this.this$0.fontPath, false, 6, false, true);
          SunGraphicsEnvironment.access$702(this.this$0, true);
          return null;
        }
      });
    }
  }

  private boolean isNameForRegisteredFile(java.lang.String paramString)
  {
    java.lang.String str = FontManager.getFileNameForFontName(paramString);
    if (str == null)
      return false;
    return this.registeredFontFiles.contains(str);
  }

  public Font[] getAllInstalledFonts()
  {
    if (this.allFonts == null)
    {
      loadFonts();
      localObject1 = new TreeMap();
      Font2D[] arrayOfFont2D = FontManager.getRegisteredFonts();
      for (int i = 0; i < arrayOfFont2D.length; ++i)
        if (!(arrayOfFont2D[i] instanceof NativeFont))
          ((TreeMap)localObject1).put(arrayOfFont2D[i].getFontName(null), arrayOfFont2D[i]);
      java.lang.String[] arrayOfString1 = FontManager.getFontNamesFromPlatform();
      if (arrayOfString1 != null)
        for (int j = 0; j < arrayOfString1.length; ++j)
          if (!(isNameForRegisteredFile(arrayOfString1[j])))
            ((TreeMap)localObject1).put(arrayOfString1[j], null);
      java.lang.String[] arrayOfString2 = null;
      if (((TreeMap)localObject1).size() > 0)
      {
        arrayOfString2 = new java.lang.String[((TreeMap)localObject1).size()];
        localObject2 = ((TreeMap)localObject1).keySet().toArray();
        for (k = 0; k < localObject2.length; ++k)
          arrayOfString2[k] = ((java.lang.String)localObject2[k]);
      }
      Object localObject2 = new Font[arrayOfString2.length];
      for (int k = 0; k < arrayOfString2.length; ++k)
      {
        localObject2[k] = new Font(arrayOfString2[k], 0, 1);
        Font2D localFont2D = (Font2D)((TreeMap)localObject1).get(arrayOfString2[k]);
        if (localFont2D != null)
          FontManager.setFont2D(localObject2[k], localFont2D.handle);
      }
      this.allFonts = ((Font)localObject2);
    }
    Object localObject1 = new Font[this.allFonts.length];
    System.arraycopy(this.allFonts, 0, localObject1, 0, this.allFonts.length);
    return ((Font)(Font)localObject1);
  }

  public Font[] getAllFonts()
  {
    Font[] arrayOfFont1 = getAllInstalledFonts();
    Font[] arrayOfFont2 = FontManager.getCreatedFonts();
    if ((arrayOfFont2 == null) || (arrayOfFont2.length == 0))
      return arrayOfFont1;
    int i = arrayOfFont1.length + arrayOfFont2.length;
    Font[] arrayOfFont3 = (Font[])Arrays.copyOf(arrayOfFont1, i);
    System.arraycopy(arrayOfFont2, 0, arrayOfFont3, arrayOfFont1.length, arrayOfFont2.length);
    return arrayOfFont3;
  }

  public static Locale getSystemStartupLocale()
  {
    if (systemLocale == null)
      systemLocale = (Locale)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          java.lang.String str1 = System.getProperty("file.encoding", "");
          java.lang.String str2 = System.getProperty("sun.jnu.encoding");
          if ((str2 != null) && (!(str2.equals(str1))))
            return Locale.ROOT;
          java.lang.String str3 = System.getProperty("user.language", "en");
          java.lang.String str4 = System.getProperty("user.country", "");
          java.lang.String str5 = System.getProperty("user.variant", "");
          return new Locale(str3, str4, str5);
        }
      });
    return systemLocale;
  }

  protected void getJREFontFamilyNames(TreeMap<java.lang.String, java.lang.String> paramTreeMap, Locale paramLocale)
  {
    FontManager.registerDeferredJREFonts(jreFontDirName);
    PhysicalFont[] arrayOfPhysicalFont = FontManager.getPhysicalFonts();
    for (int i = 0; i < arrayOfPhysicalFont.length; ++i)
      if (!(arrayOfPhysicalFont[i] instanceof NativeFont))
      {
        java.lang.String str = arrayOfPhysicalFont[i].getFamilyName(paramLocale);
        paramTreeMap.put(str.toLowerCase(paramLocale), str);
      }
  }

  public java.lang.String[] getInstalledFontFamilyNames(Locale paramLocale)
  {
    if (paramLocale == null)
      paramLocale = Locale.getDefault();
    if ((this.allFamilies != null) && (this.lastDefaultLocale != null) && (paramLocale.equals(this.lastDefaultLocale)))
    {
      localObject1 = new java.lang.String[this.allFamilies.length];
      System.arraycopy(this.allFamilies, 0, localObject1, 0, this.allFamilies.length);
      return localObject1;
    }
    Object localObject1 = new TreeMap();
    java.lang.String str1 = "Serif";
    ((TreeMap)localObject1).put(str1.toLowerCase(), str1);
    str1 = "SansSerif";
    ((TreeMap)localObject1).put(str1.toLowerCase(), str1);
    str1 = "Monospaced";
    ((TreeMap)localObject1).put(str1.toLowerCase(), str1);
    str1 = "Dialog";
    ((TreeMap)localObject1).put(str1.toLowerCase(), str1);
    str1 = "DialogInput";
    ((TreeMap)localObject1).put(str1.toLowerCase(), str1);
    if ((paramLocale.equals(getSystemStartupLocale())) && (FontManager.getFamilyNamesFromPlatform((TreeMap)localObject1, paramLocale)))
    {
      getJREFontFamilyNames((TreeMap)localObject1, paramLocale);
    }
    else
    {
      loadFontFiles();
      localObject2 = FontManager.getPhysicalFonts();
      for (int i = 0; i < localObject2.length; ++i)
        if (!(localObject2[i] instanceof NativeFont))
        {
          java.lang.String str2 = localObject2[i].getFamilyName(paramLocale);
          ((TreeMap)localObject1).put(str2.toLowerCase(paramLocale), str2);
        }
    }
    Object localObject2 = new java.lang.String[((TreeMap)localObject1).size()];
    Object[] arrayOfObject = ((TreeMap)localObject1).keySet().toArray();
    for (int j = 0; j < arrayOfObject.length; ++j)
      localObject2[j] = ((java.lang.String)((TreeMap)localObject1).get(arrayOfObject[j]));
    if (paramLocale.equals(Locale.getDefault()))
    {
      this.lastDefaultLocale = paramLocale;
      this.allFamilies = new java.lang.String[localObject2.length];
      System.arraycopy(localObject2, 0, this.allFamilies, 0, this.allFamilies.length);
    }
    return ((java.lang.String)(java.lang.String)localObject2);
  }

  public java.lang.String[] getAvailableFontFamilyNames(Locale paramLocale)
  {
    java.lang.String[] arrayOfString1 = getInstalledFontFamilyNames(paramLocale);
    TreeMap localTreeMap = FontManager.getCreatedFontFamilyNames();
    if ((localTreeMap == null) || (localTreeMap.size() == 0))
      return arrayOfString1;
    for (int i = 0; i < arrayOfString1.length; ++i)
      localTreeMap.put(arrayOfString1[i].toLowerCase(paramLocale), arrayOfString1[i]);
    java.lang.String[] arrayOfString2 = new java.lang.String[localTreeMap.size()];
    Object[] arrayOfObject = localTreeMap.keySet().toArray();
    for (int j = 0; j < arrayOfObject.length; ++j)
      arrayOfString2[j] = ((java.lang.String)localTreeMap.get(arrayOfObject[j]));
    return arrayOfString2;
  }

  public java.lang.String[] getAvailableFontFamilyNames()
  {
    return getAvailableFontFamilyNames(Locale.getDefault());
  }

  protected java.lang.String getFileNameFromPlatformName(java.lang.String paramString)
  {
    return this.fontConfig.getFileNameFromPlatformName(paramString);
  }

  public PrinterJob getPrinterJob()
  {
    new Exception().printStackTrace();
    return null;
  }

  protected void registerJREFontsWithPlatform(java.lang.String paramString)
  {
  }

  public void register1dot0Fonts()
  {
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        java.lang.String str = "/usr/openwin/lib/X11/fonts/Type1";
        SunGraphicsEnvironment.access$200(this.this$0, str, true, 4, false, false);
        return null;
      }
    });
  }

  protected void registerFontDirs(java.lang.String paramString)
  {
  }

  public void registerFontsInDir(java.lang.String paramString)
  {
    registerFontsInDir(paramString, true, 2, true, false);
  }

  private void registerFontsInDir(java.lang.String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2, boolean paramBoolean3)
  {
    File localFile = new File(paramString);
    addDirFonts(paramString, localFile, ttFilter, 0, paramBoolean1, (paramInt == 6) ? 3 : paramInt, paramBoolean2, paramBoolean3);
    addDirFonts(paramString, localFile, t1Filter, 1, paramBoolean1, (paramInt == 6) ? 4 : paramInt, paramBoolean2, paramBoolean3);
  }

  private void registerFontsOnPath(java.lang.String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2, boolean paramBoolean3)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, File.pathSeparator);
    try
    {
      while (localStringTokenizer.hasMoreTokens())
        registerFontsInDir(localStringTokenizer.nextToken(), paramBoolean1, paramInt, paramBoolean2, paramBoolean3);
    }
    catch (NoSuchElementException localNoSuchElementException)
    {
    }
  }

  protected void registerFontFile(java.lang.String paramString, java.lang.String[] paramArrayOfString, int paramInt, boolean paramBoolean)
  {
    int i;
    if (this.registeredFontFiles.contains(paramString))
      return;
    if (ttFilter.accept(null, paramString))
      i = 0;
    else if (t1Filter.accept(null, paramString))
      i = 1;
    else
      i = 5;
    this.registeredFontFiles.add(paramString);
    if (paramBoolean)
      FontManager.registerDeferredFont(paramString, paramString, paramArrayOfString, i, false, paramInt);
    else
      FontManager.registerFontFile(paramString, paramArrayOfString, i, false, paramInt);
  }

  protected void registerFontDir(java.lang.String paramString)
  {
  }

  protected java.lang.String[] getNativeNames(java.lang.String paramString1, java.lang.String paramString2)
  {
    return null;
  }

  private void addDirFonts(java.lang.String paramString, File paramFile, FilenameFilter paramFilenameFilter, int paramInt1, boolean paramBoolean1, int paramInt2, boolean paramBoolean2, boolean paramBoolean3)
  {
    java.lang.String[] arrayOfString1 = paramFile.list(paramFilenameFilter);
    if ((arrayOfString1 == null) || (arrayOfString1.length == 0))
      return;
    java.lang.String[] arrayOfString2 = new java.lang.String[arrayOfString1.length];
    [Ljava.lang.String[] arrayOfString; = new java.lang.String[arrayOfString1.length][];
    int i = 0;
    for (int j = 0; j < arrayOfString1.length; ++j)
    {
      File localFile = new File(paramFile, arrayOfString1[j]);
      java.lang.String str1 = null;
      if (paramBoolean3)
        try
        {
          str1 = localFile.getCanonicalPath();
        }
        catch (IOException localIOException)
        {
        }
      if (str1 == null)
        str1 = paramString + File.separator + arrayOfString1[j];
      if (this.registeredFontFiles.contains(str1))
        break label381:
      if ((badFonts != null) && (badFonts.contains(str1)))
      {
        if (debugFonts)
          logger.warning("skip bad font " + str1);
      }
      else
      {
        this.registeredFontFiles.add(str1);
        if ((debugFonts) && (logger.isLoggable(Level.INFO)))
        {
          java.lang.String str2 = "Registering font " + str1;
          java.lang.String[] arrayOfString3 = getNativeNames(str1, null);
          if (arrayOfString3 == null)
          {
            str2 = str2 + " with no native name";
          }
          else
          {
            str2 = str2 + " with native name(s) " + arrayOfString3[0];
            for (int k = 1; k < arrayOfString3.length; ++k)
              str2 = str2 + ", " + arrayOfString3[k];
          }
          logger.info(str2);
        }
        arrayOfString2[i] = str1;
        label381: arrayOfString;[(i++)] = getNativeNames(str1, null);
      }
    }
    FontManager.registerFonts(arrayOfString2, arrayOfString;, i, paramInt1, paramBoolean1, paramInt2, paramBoolean2);
  }

  protected void addToMissingFontFileList(java.lang.String paramString)
  {
    if (missingFontFiles == null)
      missingFontFiles = new HashSet();
    missingFontFiles.add(paramString);
  }

  protected abstract FontConfiguration createFontConfiguration();

  public abstract FontConfiguration createFontConfiguration(boolean paramBoolean1, boolean paramBoolean2);

  private void initCompositeFonts(FontConfiguration paramFontConfiguration, Hashtable paramHashtable)
  {
    Object localObject;
    java.lang.String[] arrayOfString2;
    int i = paramFontConfiguration.getNumberCoreFonts();
    java.lang.String[] arrayOfString1 = paramFontConfiguration.getPlatformFontNames();
    for (int j = 0; j < arrayOfString1.length; ++j)
    {
      java.lang.String str = arrayOfString1[j];
      localObject = getFileNameFromPlatformName(str);
      arrayOfString2 = null;
      if (localObject == null)
      {
        localObject = str;
      }
      else
      {
        if (j < i)
          addFontToPlatformFontPath(str);
        arrayOfString2 = getNativeNames((java.lang.String)localObject, str);
      }
      registerFontFile((java.lang.String)localObject, arrayOfString2, 2, true);
    }
    registerPlatformFontsUsedByFontConfiguration();
    CompositeFontDescriptor[] arrayOfCompositeFontDescriptor = paramFontConfiguration.get2DCompositeFontInfo();
    for (int k = 0; k < arrayOfCompositeFontDescriptor.length; ++k)
    {
      localObject = arrayOfCompositeFontDescriptor[k];
      arrayOfString2 = ((CompositeFontDescriptor)localObject).getComponentFileNames();
      java.lang.String[] arrayOfString3 = ((CompositeFontDescriptor)localObject).getComponentFaceNames();
      if (missingFontFiles != null)
        for (int l = 0; l < arrayOfString2.length; ++l)
          if (missingFontFiles.contains(arrayOfString2[l]))
          {
            arrayOfString2[l] = getDefaultFontFile();
            arrayOfString3[l] = getDefaultFontFaceName();
          }
      if (paramHashtable != null)
        FontManager.registerCompositeFont(((CompositeFontDescriptor)localObject).getFaceName(), arrayOfString2, arrayOfString3, ((CompositeFontDescriptor)localObject).getCoreComponentCount(), ((CompositeFontDescriptor)localObject).getExclusionRanges(), ((CompositeFontDescriptor)localObject).getExclusionRangeLimits(), true, paramHashtable);
      else
        FontManager.registerCompositeFont(((CompositeFontDescriptor)localObject).getFaceName(), arrayOfString2, arrayOfString3, ((CompositeFontDescriptor)localObject).getCoreComponentCount(), ((CompositeFontDescriptor)localObject).getExclusionRanges(), ((CompositeFontDescriptor)localObject).getExclusionRangeLimits(), true);
      if (debugFonts)
        logger.info("registered " + ((CompositeFontDescriptor)localObject).getFaceName());
    }
  }

  protected void addFontToPlatformFontPath(java.lang.String paramString)
  {
  }

  protected void registerPlatformFontsUsedByFontConfiguration()
  {
  }

  public static boolean isLogicalFont(Font paramFont)
  {
    return FontConfiguration.isLogicalFontFamilyName(paramFont.getFamily());
  }

  public FontConfiguration getFontConfiguration()
  {
    return this.fontConfig;
  }

  public static Rectangle getUsableBounds(GraphicsDevice paramGraphicsDevice)
  {
    GraphicsConfiguration localGraphicsConfiguration = paramGraphicsDevice.getDefaultConfiguration();
    Insets localInsets = Toolkit.getDefaultToolkit().getScreenInsets(localGraphicsConfiguration);
    Rectangle localRectangle = localGraphicsConfiguration.getBounds();
    localRectangle.x += localInsets.left;
    localRectangle.y += localInsets.top;
    localRectangle.width -= localInsets.left + localInsets.right;
    localRectangle.height -= localInsets.top + localInsets.bottom;
    return localRectangle;
  }

  public static boolean fontSupportsDefaultEncoding(Font paramFont)
  {
    return FontManager.fontSupportsDefaultEncoding(paramFont);
  }

  public static void useAlternateFontforJALocales()
  {
    FontManager.useAlternateFontforJALocales();
  }

  public void createCompositeFonts(Hashtable paramHashtable, boolean paramBoolean1, boolean paramBoolean2)
  {
    FontConfiguration localFontConfiguration = createFontConfiguration(paramBoolean1, paramBoolean2);
    initCompositeFonts(localFontConfiguration, paramHashtable);
  }

  protected void getPlatformFontPathFromFontConfig()
  {
  }

  public void displayChanged()
  {
    GraphicsDevice[] arrayOfGraphicsDevice = getScreenDevices();
    int i = arrayOfGraphicsDevice.length;
    for (int j = 0; j < i; ++j)
    {
      GraphicsDevice localGraphicsDevice = arrayOfGraphicsDevice[j];
      if (localGraphicsDevice instanceof DisplayChangedListener)
        ((DisplayChangedListener)localGraphicsDevice).displayChanged();
    }
    this.displayChanger.notifyListeners();
  }

  public void paletteChanged()
  {
    this.displayChanger.notifyPaletteChanged();
  }

  public void addDisplayChangedListener(DisplayChangedListener paramDisplayChangedListener)
  {
    this.displayChanger.add(paramDisplayChangedListener);
  }

  public void removeDisplayChangedListener(DisplayChangedListener paramDisplayChangedListener)
  {
    this.displayChanger.remove(paramDisplayChangedListener);
  }

  public boolean isFlipStrategyPreferred(ComponentPeer paramComponentPeer)
  {
    return false;
  }

  static
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        java.lang.String str = System.getProperty("sun.java2d.debugfonts");
        if ((str != null) && (!(str.equals("false"))))
        {
          SunGraphicsEnvironment.debugFonts = true;
          SunGraphicsEnvironment.logger = Logger.getLogger("sun.java2d");
          if (str.equals("warning"))
            SunGraphicsEnvironment.logger.setLevel(Level.WARNING);
          else if (str.equals("severe"))
            SunGraphicsEnvironment.logger.setLevel(Level.SEVERE);
        }
        return null;
      }
    });
    systemLocale = null;
    ttFilter = new TTFilter();
    t1Filter = new T1Filter();
  }

  public static class T1Filter
  implements FilenameFilter
  {
    public boolean accept(File paramFile, java.lang.String paramString)
    {
      if (SunGraphicsEnvironment.noType1Font)
        return false;
      int i = paramString.length() - 4;
      if (i <= 0)
        return false;
      return ((paramString.startsWith(".pfa", i)) || (paramString.startsWith(".pfb", i)) || (paramString.startsWith(".PFA", i)) || (paramString.startsWith(".PFB", i)));
    }
  }

  public static class TTFilter
  implements FilenameFilter
  {
    public boolean accept(File paramFile, java.lang.String paramString)
    {
      int i = paramString.length() - 4;
      if (i <= 0)
        return false;
      return ((paramString.startsWith(".ttf", i)) || (paramString.startsWith(".TTF", i)) || (paramString.startsWith(".ttc", i)) || (paramString.startsWith(".TTC", i)));
    }
  }
}
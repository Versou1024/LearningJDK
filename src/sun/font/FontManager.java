package sun.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.FontUIResource;
import sun.applet.AppletSecurity;
import sun.awt.AppContext;
import sun.awt.FontConfiguration;
import sun.awt.SunHints.Value;
import sun.awt.SunToolkit;
import sun.java2d.HeadlessGraphicsEnvironment;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.SunGraphicsEnvironment.T1Filter;
import sun.java2d.SunGraphicsEnvironment.TTFilter;

public final class FontManager
{
  public static final int FONTFORMAT_NONE = -1;
  public static final int FONTFORMAT_TRUETYPE = 0;
  public static final int FONTFORMAT_TYPE1 = 1;
  public static final int FONTFORMAT_T2K = 2;
  public static final int FONTFORMAT_TTC = 3;
  public static final int FONTFORMAT_COMPOSITE = 4;
  public static final int FONTFORMAT_NATIVE = 5;
  public static final int NO_FALLBACK = 0;
  public static final int PHYSICAL_FALLBACK = 1;
  public static final int LOGICAL_FALLBACK = 2;
  public static final int QUADPATHTYPE = 1;
  public static final int CUBICPATHTYPE = 2;
  private static final int CHANNELPOOLSIZE = 20;
  private static int lastPoolIndex = 0;
  private static FileFont[] fontFileCache = new FileFont[20];
  private static int maxCompFont = 0;
  private static CompositeFont[] compFonts = new CompositeFont[20];
  private static Hashtable compositeFonts = new Hashtable();
  private static Hashtable physicalFonts = new Hashtable();
  private static ConcurrentHashMap<String, PhysicalFont> registeredFontFiles = new ConcurrentHashMap();
  private static Hashtable fullNameToFont = new Hashtable();
  private static HashMap localeFullNamesToFont;
  private static PhysicalFont defaultPhysicalFont;
  private static boolean usePlatformFontMetrics = false;
  public static Logger logger = null;
  public static boolean logging;
  static boolean longAddresses;
  static String osName;
  static boolean useT2K;
  static boolean isWindows;
  static boolean isSolaris;
  public static boolean isSolaris8;
  public static boolean isSolaris9;
  private static boolean loaded1dot0Fonts = false;
  static SunGraphicsEnvironment sgEnv;
  static boolean loadedAllFonts = false;
  static boolean loadedAllFontFiles = false;
  static TrueTypeFont eudcFont;
  static HashMap<String, String> jreFontMap;
  static HashSet<String> jreLucidaFontFiles;
  static String[] jreOtherFontFiles;
  static boolean noOtherJREFontFiles = false;
  private static String[] STR_ARRAY = new String[0];
  private static final Hashtable deferredFontFiles;
  private static final Hashtable initialisedFonts;
  private static HashMap<String, String> fontToFileMap;
  private static HashMap<String, String> fontToFamilyNameMap;
  private static HashMap<String, ArrayList<String>> familyToFontListMap;
  private static String[] pathDirs;
  private static Hashtable fontNameCache;
  private static final short US_LCID = 1033;
  private static Map lcidMap;
  private static Thread fileCloser;
  static java.util.Vector<File> tmpFontFiles;
  private static final Object altJAFontKey;
  private static final Object localeFontKey;
  private static final Object proportionalFontKey;
  public static boolean usingPerAppContextComposites;
  private static boolean usingAlternateComposites;
  private static boolean gAltJAFont;
  private static boolean gLocalePref;
  private static boolean gPropPref;
  private static HashSet installedNames;
  private static final Object regFamilyKey;
  private static final Object regFullNameKey;
  private static Hashtable<String, FontFamily> createdByFamilyName;
  private static Hashtable<String, Font2D> createdByFullName;
  private static boolean fontsAreRegistered;
  private static boolean fontsAreRegisteredPerAppContext;
  private static final String[][] nameMap;
  private static String[] fontConfigNames;
  private static FontConfigInfo[] fontConfigFonts;

  private static native void initIDs();

  public static void addToPool(FileFont paramFileFont)
  {
    FileFont localFileFont = null;
    int i = -1;
    synchronized (fontFileCache)
    {
      for (int j = 0; j < 20; ++j)
      {
        if (fontFileCache[j] == paramFileFont)
          return;
        if ((fontFileCache[j] == null) && (i < 0))
          i = j;
      }
      if (i < 0)
        break label68;
      fontFileCache[i] = paramFileFont;
      return;
      label68: localFileFont = fontFileCache[lastPoolIndex];
      fontFileCache[lastPoolIndex] = paramFileFont;
      lastPoolIndex = (lastPoolIndex + 1) % 20;
    }
    if (localFileFont != null)
      localFileFont.close();
  }

  public static void removeFromPool(FileFont paramFileFont)
  {
    synchronized (fontFileCache)
    {
      for (int i = 0; i < 20; ++i)
        if (fontFileCache[i] == paramFileFont)
          fontFileCache[i] = null;
    }
  }

  public static boolean fontSupportsDefaultEncoding(Font paramFont)
  {
    return getFont2D(paramFont) instanceof CompositeFont;
  }

  public static FontUIResource getCompositeFontUIResource(Font paramFont)
  {
    FontUIResource localFontUIResource = new FontUIResource(paramFont.getName(), paramFont.getStyle(), paramFont.getSize());
    Font2D localFont2D = getFont2D(paramFont);
    if (!(localFont2D instanceof PhysicalFont))
      return localFontUIResource;
    CompositeFont localCompositeFont1 = (CompositeFont)findFont2D("dialog", paramFont.getStyle(), 0);
    if (localCompositeFont1 == null)
      return localFontUIResource;
    PhysicalFont localPhysicalFont = (PhysicalFont)localFont2D;
    CompositeFont localCompositeFont2 = new CompositeFont(localPhysicalFont, localCompositeFont1);
    setFont2D(localFontUIResource, localCompositeFont2.handle);
    setCreatedFont(localFontUIResource);
    return localFontUIResource;
  }

  public static Font2DHandle getNewComposite(String paramString, int paramInt, Font2DHandle paramFont2DHandle)
  {
    if (!(paramFont2DHandle.font2D instanceof CompositeFont))
      return paramFont2DHandle;
    CompositeFont localCompositeFont1 = (CompositeFont)paramFont2DHandle.font2D;
    PhysicalFont localPhysicalFont1 = localCompositeFont1.getSlotFont(0);
    if (paramString == null)
      paramString = localPhysicalFont1.getFamilyName(null);
    if (paramInt == -1)
      paramInt = localCompositeFont1.getStyle();
    Object localObject = findFont2D(paramString, paramInt, 0);
    if (!(localObject instanceof PhysicalFont))
      localObject = localPhysicalFont1;
    PhysicalFont localPhysicalFont2 = (PhysicalFont)localObject;
    CompositeFont localCompositeFont2 = (CompositeFont)findFont2D("dialog", paramInt, 0);
    if (localCompositeFont2 == null)
      return paramFont2DHandle;
    CompositeFont localCompositeFont3 = new CompositeFont(localPhysicalFont2, localCompositeFont2);
    Font2DHandle localFont2DHandle = new Font2DHandle(localCompositeFont3);
    return ((Font2DHandle)localFont2DHandle);
  }

  public static native void setFont2D(Font paramFont, Font2DHandle paramFont2DHandle);

  private static native boolean isCreatedFont(Font paramFont);

  private static native void setCreatedFont(Font paramFont);

  public static void registerCompositeFont(String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, boolean paramBoolean)
  {
    CompositeFont localCompositeFont = new CompositeFont(paramString, paramArrayOfString1, paramArrayOfString2, paramInt, paramArrayOfInt1, paramArrayOfInt2, paramBoolean);
    addCompositeToFontList(localCompositeFont, 2);
    synchronized (compFonts)
    {
      compFonts[(maxCompFont++)] = localCompositeFont;
    }
  }

  public static void registerCompositeFont(String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, boolean paramBoolean, Hashtable paramHashtable)
  {
    CompositeFont localCompositeFont = new CompositeFont(paramString, paramArrayOfString1, paramArrayOfString2, paramInt, paramArrayOfInt1, paramArrayOfInt2, paramBoolean);
    Font2D localFont2D = (Font2D)paramHashtable.get(paramString.toLowerCase(Locale.ENGLISH));
    if (localFont2D instanceof CompositeFont)
      localFont2D.handle.font2D = localCompositeFont;
    paramHashtable.put(paramString.toLowerCase(Locale.ENGLISH), localCompositeFont);
  }

  private static void addCompositeToFontList(CompositeFont paramCompositeFont, int paramInt)
  {
    if (logging)
      logger.info("Add to Family " + paramCompositeFont.familyName + ", Font " + paramCompositeFont.fullName + " rank=" + paramInt);
    paramCompositeFont.setRank(paramInt);
    compositeFonts.put(paramCompositeFont.fullName, paramCompositeFont);
    fullNameToFont.put(paramCompositeFont.fullName.toLowerCase(Locale.ENGLISH), paramCompositeFont);
    FontFamily localFontFamily = FontFamily.getFamily(paramCompositeFont.familyName);
    if (localFontFamily == null)
      localFontFamily = new FontFamily(paramCompositeFont.familyName, true, paramInt);
    localFontFamily.setFont(paramCompositeFont, paramCompositeFont.style);
  }

  private static PhysicalFont addToFontList(PhysicalFont paramPhysicalFont, int paramInt)
  {
    String str1 = paramPhysicalFont.fullName;
    String str2 = paramPhysicalFont.familyName;
    if ((str1 == null) || ("".equals(str1)))
      return null;
    if (compositeFonts.containsKey(str1))
      return null;
    paramPhysicalFont.setRank(paramInt);
    if (!(physicalFonts.containsKey(str1)))
    {
      if (logging)
        logger.info("Add to Family " + str2 + ", Font " + str1 + " rank=" + paramInt);
      physicalFonts.put(str1, paramPhysicalFont);
      localObject1 = FontFamily.getFamily(str2);
      if (localObject1 == null)
      {
        localObject1 = new FontFamily(str2, false, paramInt);
        ((FontFamily)localObject1).setFont(paramPhysicalFont, paramPhysicalFont.style);
      }
      else if (((FontFamily)localObject1).getRank() >= paramInt)
      {
        ((FontFamily)localObject1).setFont(paramPhysicalFont, paramPhysicalFont.style);
      }
      fullNameToFont.put(str1.toLowerCase(Locale.ENGLISH), paramPhysicalFont);
      return paramPhysicalFont;
    }
    Object localObject1 = paramPhysicalFont;
    PhysicalFont localPhysicalFont = (PhysicalFont)physicalFonts.get(str1);
    if (localPhysicalFont == null)
      return null;
    if (localPhysicalFont.getRank() >= paramInt)
    {
      if ((localPhysicalFont.mapper != null) && (paramInt > 2))
        return localPhysicalFont;
      if (localPhysicalFont.getRank() == paramInt)
        if ((localPhysicalFont instanceof TrueTypeFont) && (localObject1 instanceof TrueTypeFont))
        {
          localObject2 = (TrueTypeFont)localPhysicalFont;
          TrueTypeFont localTrueTypeFont = (TrueTypeFont)localObject1;
          if (((TrueTypeFont)localObject2).fileSize >= localTrueTypeFont.fileSize)
            return localPhysicalFont;
        }
        else
        {
          return localPhysicalFont;
        }
      if (localPhysicalFont.platName.startsWith(SunGraphicsEnvironment.jreFontDirName))
      {
        if (logging)
          logger.warning("Unexpected attempt to replace a JRE  font " + str1 + " from " + localPhysicalFont.platName + " with " + ((PhysicalFont)localObject1).platName);
        return localPhysicalFont;
      }
      if (logging)
        logger.info("Replace in Family " + str2 + ",Font " + str1 + " new rank=" + paramInt + " from " + localPhysicalFont.platName + " with " + ((PhysicalFont)localObject1).platName);
      replaceFont(localPhysicalFont, (PhysicalFont)localObject1);
      physicalFonts.put(str1, localObject1);
      fullNameToFont.put(str1.toLowerCase(Locale.ENGLISH), localObject1);
      Object localObject2 = FontFamily.getFamily(str2);
      if (localObject2 == null)
      {
        localObject2 = new FontFamily(str2, false, paramInt);
        ((FontFamily)localObject2).setFont((Font2D)localObject1, ((PhysicalFont)localObject1).style);
      }
      else if (((FontFamily)localObject2).getRank() >= paramInt)
      {
        ((FontFamily)localObject2).setFont((Font2D)localObject1, ((PhysicalFont)localObject1).style);
      }
      return localObject1;
    }
    return ((PhysicalFont)(PhysicalFont)localPhysicalFont);
  }

  public static Font2D[] getRegisteredFonts()
  {
    PhysicalFont[] arrayOfPhysicalFont = getPhysicalFonts();
    int i = maxCompFont;
    Font2D[] arrayOfFont2D = new Font2D[arrayOfPhysicalFont.length + i];
    System.arraycopy(compFonts, 0, arrayOfFont2D, 0, i);
    System.arraycopy(arrayOfPhysicalFont, 0, arrayOfFont2D, i, arrayOfPhysicalFont.length);
    return arrayOfFont2D;
  }

  public static PhysicalFont[] getPhysicalFonts()
  {
    return ((PhysicalFont[])(PhysicalFont[])physicalFonts.values().toArray(new PhysicalFont[0]));
  }

  public static synchronized void initialiseDeferredFonts()
  {
    String[] arrayOfString = (String[])(String[])deferredFontFiles.keySet().toArray(STR_ARRAY);
    for (int i = 0; i < arrayOfString.length; ++i)
      initialiseDeferredFont(arrayOfString[i]);
  }

  public static synchronized void registerDeferredJREFonts(String paramString)
  {
    FontRegistrationInfo[] arrayOfFontRegistrationInfo = (FontRegistrationInfo[])(FontRegistrationInfo[])deferredFontFiles.values().toArray(new FontRegistrationInfo[0]);
    for (int i = 0; i < arrayOfFontRegistrationInfo.length; ++i)
      if ((arrayOfFontRegistrationInfo[i].fontFilePath != null) && (arrayOfFontRegistrationInfo[i].fontFilePath.startsWith(paramString)))
        initialiseDeferredFont(arrayOfFontRegistrationInfo[i].fontFilePath);
  }

  private static PhysicalFont findJREDeferredFont(String paramString, int paramInt)
  {
    PhysicalFont localPhysicalFont;
    String str1 = paramString.toLowerCase(Locale.ENGLISH) + paramInt;
    String str2 = (String)jreFontMap.get(str1);
    if (str2 != null)
    {
      initSGEnv();
      str2 = SunGraphicsEnvironment.jreFontDirName + File.separator + str2;
      if (deferredFontFiles.get(str2) != null)
      {
        localPhysicalFont = initialiseDeferredFont(str2);
        if ((localPhysicalFont != null) && (((localPhysicalFont.getFontName(null).equalsIgnoreCase(paramString)) || (localPhysicalFont.getFamilyName(null).equalsIgnoreCase(paramString)))) && (localPhysicalFont.style == paramInt))
          return localPhysicalFont;
      }
    }
    if (noOtherJREFontFiles)
      return null;
    synchronized (jreLucidaFontFiles)
    {
      if (jreOtherFontFiles == null)
      {
        HashSet localHashSet2 = new HashSet();
        String[] arrayOfString = (String[])(String[])deferredFontFiles.keySet().toArray(STR_ARRAY);
        for (int j = 0; j < arrayOfString.length; ++j)
        {
          File localFile = new File(arrayOfString[j]);
          String str3 = localFile.getParent();
          String str4 = localFile.getName();
          if ((str3 != null) && (str3.equals(SunGraphicsEnvironment.jreFontDirName)))
          {
            if (jreLucidaFontFiles.contains(str4))
              break label261:
            label261: localHashSet2.add(arrayOfString[j]);
          }
        }
        jreOtherFontFiles = (String[])localHashSet2.toArray(STR_ARRAY);
        if (jreOtherFontFiles.length == 0)
          noOtherJREFontFiles = true;
      }
      int i = 0;
      while (true)
      {
        if (i >= jreOtherFontFiles.length)
          break label380;
        str2 = jreOtherFontFiles[i];
        if (str2 == null)
          break label374:
        jreOtherFontFiles[i] = null;
        localPhysicalFont = initialiseDeferredFont(str2);
        if ((localPhysicalFont != null) && (((localPhysicalFont.getFontName(null).equalsIgnoreCase(paramString)) || (localPhysicalFont.getFamilyName(null).equalsIgnoreCase(paramString)))) && (localPhysicalFont.style == paramInt))
          return localPhysicalFont;
        label374: label380: ++i;
      }
    }
    return null;
  }

  private static PhysicalFont findOtherDeferredFont(String paramString, int paramInt)
  {
    String[] arrayOfString = (String[])(String[])deferredFontFiles.keySet().toArray(STR_ARRAY);
    for (int i = 0; i < arrayOfString.length; ++i)
    {
      File localFile = new File(arrayOfString[i]);
      String str1 = localFile.getParent();
      String str2 = localFile.getName();
      if ((str1 != null) && (str1.equals(SunGraphicsEnvironment.jreFontDirName)) && (jreLucidaFontFiles.contains(str2)))
        break label136:
      PhysicalFont localPhysicalFont = initialiseDeferredFont(arrayOfString[i]);
      label136: if ((localPhysicalFont != null) && (((localPhysicalFont.getFontName(null).equalsIgnoreCase(paramString)) || (localPhysicalFont.getFamilyName(null).equalsIgnoreCase(paramString)))) && (localPhysicalFont.style == paramInt))
        return localPhysicalFont;
    }
    return null;
  }

  private static PhysicalFont findDeferredFont(String paramString, int paramInt)
  {
    PhysicalFont localPhysicalFont = findJREDeferredFont(paramString, paramInt);
    if (localPhysicalFont != null)
      return localPhysicalFont;
    return findOtherDeferredFont(paramString, paramInt);
  }

  public static void registerDeferredFont(String paramString1, String paramString2, String[] paramArrayOfString, int paramInt1, boolean paramBoolean, int paramInt2)
  {
    FontRegistrationInfo localFontRegistrationInfo = new FontRegistrationInfo(paramString2, paramArrayOfString, paramInt1, paramBoolean, paramInt2);
    deferredFontFiles.put(paramString1, localFontRegistrationInfo);
  }

  public static synchronized PhysicalFont initialiseDeferredFont(String paramString)
  {
    PhysicalFont localPhysicalFont;
    if (paramString == null)
      return null;
    if (logging)
      logger.info("Opening deferred font file " + paramString);
    FontRegistrationInfo localFontRegistrationInfo = (FontRegistrationInfo)deferredFontFiles.get(paramString);
    if (localFontRegistrationInfo != null)
    {
      deferredFontFiles.remove(paramString);
      localPhysicalFont = registerFontFile(localFontRegistrationInfo.fontFilePath, localFontRegistrationInfo.nativeNames, localFontRegistrationInfo.fontFormat, localFontRegistrationInfo.javaRasterizer, localFontRegistrationInfo.fontRank);
      if (localPhysicalFont != null)
        initialisedFonts.put(paramString, localPhysicalFont.handle);
      else
        initialisedFonts.put(paramString, getDefaultPhysicalFont().handle);
    }
    else
    {
      Font2DHandle localFont2DHandle = (Font2DHandle)initialisedFonts.get(paramString);
      if (localFont2DHandle == null)
        localPhysicalFont = getDefaultPhysicalFont();
      else
        localPhysicalFont = (PhysicalFont)(PhysicalFont)localFont2DHandle.font2D;
    }
    return localPhysicalFont;
  }

  public static PhysicalFont registerFontFile(String paramString, String[] paramArrayOfString, int paramInt1, boolean paramBoolean, int paramInt2)
  {
    PhysicalFont localPhysicalFont = (PhysicalFont)registeredFontFiles.get(paramString);
    if (localPhysicalFont != null)
      return localPhysicalFont;
    Object localObject1 = null;
    try
    {
      Object localObject2;
      switch (paramInt1)
      {
      case 0:
        TrueTypeFont localTrueTypeFont;
        int i = 0;
        do
        {
          localTrueTypeFont = new TrueTypeFont(paramString, paramArrayOfString, i++, paramBoolean);
          localObject2 = addToFontList(localTrueTypeFont, paramInt2);
          if (localObject1 == null)
            localObject1 = localObject2;
        }
        while (i < localTrueTypeFont.getFontCount());
        break;
      case 1:
        localObject2 = new Type1Font(paramString, paramArrayOfString);
        localObject1 = addToFontList((PhysicalFont)localObject2, paramInt2);
        break;
      case 5:
        NativeFont localNativeFont = new NativeFont(paramString, false);
        localObject1 = addToFontList(localNativeFont, paramInt2);
      }
      if (logging)
        logger.info("Registered file " + paramString + " as font " + localObject1 + " rank=" + paramInt2);
    }
    catch (FontFormatException localFontFormatException)
    {
      if (logging)
        logger.warning("Unusable font: " + paramString + " " + localFontFormatException.toString());
    }
    if ((localObject1 != null) && (paramInt1 != 5))
      registeredFontFiles.put(paramString, localObject1);
    return ((PhysicalFont)(PhysicalFont)localObject1);
  }

  public static void registerFonts(String[] paramArrayOfString, String[][] paramArrayOfString1, int paramInt1, int paramInt2, boolean paramBoolean1, int paramInt3, boolean paramBoolean2)
  {
    for (int i = 0; i < paramInt1; ++i)
      if (paramBoolean2)
        registerDeferredFont(paramArrayOfString[i], paramArrayOfString[i], paramArrayOfString1[i], paramInt2, paramBoolean1, paramInt3);
      else
        registerFontFile(paramArrayOfString[i], paramArrayOfString1[i], paramInt2, paramBoolean1, paramInt3);
  }

  public static PhysicalFont getDefaultPhysicalFont()
  {
    if (defaultPhysicalFont == null)
    {
      defaultPhysicalFont = (PhysicalFont)findFont2D("Lucida Sans Regular", 0, 0);
      if (defaultPhysicalFont == null)
        defaultPhysicalFont = (PhysicalFont)findFont2D("Arial", 0, 0);
      if (defaultPhysicalFont == null)
      {
        Iterator localIterator = physicalFonts.values().iterator();
        if (localIterator.hasNext())
          defaultPhysicalFont = (PhysicalFont)localIterator.next();
        else
          throw new Error("Probable fatal error:No fonts found.");
      }
    }
    return defaultPhysicalFont;
  }

  public static CompositeFont getDefaultLogicalFont(int paramInt)
  {
    return ((CompositeFont)findFont2D("dialog", paramInt, 0));
  }

  private static String dotStyleStr(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return ".bold";
    case 2:
      return ".italic";
    case 3:
      return ".bolditalic";
    }
    return ".plain";
  }

  static void initSGEnv()
  {
    if (sgEnv == null)
    {
      GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      if (localGraphicsEnvironment instanceof HeadlessGraphicsEnvironment)
      {
        HeadlessGraphicsEnvironment localHeadlessGraphicsEnvironment = (HeadlessGraphicsEnvironment)localGraphicsEnvironment;
        sgEnv = (SunGraphicsEnvironment)localHeadlessGraphicsEnvironment.getSunGraphicsEnvironment();
      }
      else
      {
        sgEnv = (SunGraphicsEnvironment)localGraphicsEnvironment;
      }
    }
  }

  private static native void populateFontFileNameMap(HashMap<String, String> paramHashMap1, HashMap<String, String> paramHashMap2, HashMap<String, ArrayList<String>> paramHashMap, Locale paramLocale);

  private static String[] getFontFilesFromPath()
  {
    return ((String[])(String[])AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        if (FontManager.access$200().length == 1)
        {
          localObject = new File(FontManager.access$200()[0]);
          String[] arrayOfString1 = ((File)localObject).list(SunGraphicsEnvironment.ttFilter);
          if (arrayOfString1 == null)
            return new String[0];
          for (int j = 0; j < arrayOfString1.length; ++j)
            arrayOfString1[j] = arrayOfString1[j].toLowerCase();
          return arrayOfString1;
        }
        Object localObject = new ArrayList();
        for (int i = 0; i < FontManager.access$200().length; ++i)
        {
          File localFile = new File(FontManager.access$200()[i]);
          String[] arrayOfString2 = localFile.list(SunGraphicsEnvironment.ttFilter);
          if (arrayOfString2 == null)
            break label141:
          label141: for (int k = 0; k < arrayOfString2.length; ++k)
            ((ArrayList)localObject).add(arrayOfString2[k].toLowerCase());
        }
        return ((ArrayList)localObject).toArray(FontManager.access$300());
      }
    }));
  }

  private static void resolveWindowsFonts()
  {
    Object localObject2;
    Object localObject3;
    Object localObject4;
    ArrayList localArrayList = null;
    Object localObject1 = fontToFamilyNameMap.keySet().iterator();
    while (((Iterator)localObject1).hasNext())
    {
      localObject2 = (String)((Iterator)localObject1).next();
      localObject3 = (String)fontToFileMap.get(localObject2);
      if (localObject3 == null)
        if (((String)localObject2).indexOf("  ") > 0)
        {
          localObject4 = ((String)localObject2).replaceFirst("  ", " ");
          localObject3 = (String)fontToFileMap.get(localObject4);
          if ((localObject3 != null) && (!(fontToFamilyNameMap.containsKey(localObject4))))
          {
            fontToFileMap.remove(localObject4);
            fontToFileMap.put(localObject2, localObject3);
          }
        }
        else if (((String)localObject2).equals("marlett"))
        {
          fontToFileMap.put(localObject2, "marlett.ttf");
        }
        else if (((String)localObject2).equals("david"))
        {
          localObject3 = (String)fontToFileMap.get("david regular");
          if (localObject3 != null)
          {
            fontToFileMap.remove("david regular");
            fontToFileMap.put("david", localObject3);
          }
        }
        else
        {
          if (localArrayList == null)
            localArrayList = new ArrayList();
          localArrayList.add(localObject2);
        }
    }
    if (localArrayList != null)
    {
      Object localObject5;
      localObject1 = new HashSet();
      localObject2 = (HashMap)(HashMap)fontToFileMap.clone();
      localObject3 = fontToFamilyNameMap.keySet().iterator();
      while (((Iterator)localObject3).hasNext())
      {
        localObject4 = (String)((Iterator)localObject3).next();
        ((HashMap)localObject2).remove(localObject4);
      }
      localObject3 = ((HashMap)localObject2).keySet().iterator();
      while (((Iterator)localObject3).hasNext())
      {
        localObject4 = (String)((Iterator)localObject3).next();
        ((HashSet)localObject1).add(((HashMap)localObject2).get(localObject4));
        fontToFileMap.remove(localObject4);
      }
      resolveFontFiles((HashSet)localObject1, localArrayList);
      if (localArrayList.size() > 0)
      {
        localObject3 = new ArrayList();
        localObject4 = fontToFileMap.values().iterator();
        while (((Iterator)localObject4).hasNext())
        {
          String str1 = (String)((Iterator)localObject4).next();
          ((ArrayList)localObject3).add(str1.toLowerCase());
        }
        localObject4 = getFontFilesFromPath();
        int k = localObject4.length;
        for (int l = 0; l < k; ++l)
        {
          localObject5 = localObject4[l];
          if (!(((ArrayList)localObject3).contains(localObject5)))
            ((HashSet)localObject1).add(localObject5);
        }
        resolveFontFiles((HashSet)localObject1, localArrayList);
      }
      if (localArrayList.size() > 0)
      {
        int i = localArrayList.size();
        for (int j = 0; j < i; ++j)
        {
          String str2 = (String)localArrayList.get(j);
          String str3 = (String)fontToFamilyNameMap.get(str2);
          if (str3 != null)
          {
            localObject5 = (ArrayList)familyToFontListMap.get(str3);
            if ((localObject5 != null) && (((ArrayList)localObject5).size() <= 1))
              familyToFontListMap.remove(str3);
          }
          fontToFamilyNameMap.remove(str2);
          if (logging)
            logger.info("No file for font:" + str2);
        }
      }
    }
  }

  private static void resolveFontFiles(HashSet<String> paramHashSet, ArrayList<String> paramArrayList)
  {
    Locale localLocale = SunToolkit.getStartupLocale();
    Iterator localIterator = paramHashSet.iterator();
    while (localIterator.hasNext())
    {
      String str1 = (String)localIterator.next();
      try
      {
        TrueTypeFont localTrueTypeFont;
        int i = 0;
        String str2 = getPathName(str1);
        if (logging)
          logger.info("Trying to resolve file " + str2);
        do
        {
          localTrueTypeFont = new TrueTypeFont(str2, null, i++, true);
          String str3 = localTrueTypeFont.getFontName(localLocale).toLowerCase();
          if (paramArrayList.contains(str3))
          {
            fontToFileMap.put(str3, str1);
            paramArrayList.remove(str3);
            if (logging)
              logger.info("Resolved absent registry entry for " + str3 + " located in " + str2);
          }
        }
        while (i < localTrueTypeFont.getFontCount());
      }
      catch (Exception localException)
      {
      }
    }
  }

  private static synchronized HashMap<String, String> getFullNameToFileMap()
  {
    if (fontToFileMap == null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(getFontPath(SunGraphicsEnvironment.noType1Font), File.pathSeparator);
      ArrayList localArrayList = new ArrayList();
      try
      {
        while (localStringTokenizer.hasMoreTokens())
          localArrayList.add(localStringTokenizer.nextToken());
      }
      catch (NoSuchElementException localNoSuchElementException)
      {
      }
      pathDirs = (String[])localArrayList.toArray(STR_ARRAY);
      fontToFileMap = new HashMap(100);
      fontToFamilyNameMap = new HashMap(100);
      familyToFontListMap = new HashMap(50);
      populateFontFileNameMap(fontToFileMap, fontToFamilyNameMap, familyToFontListMap, Locale.ENGLISH);
      if (isWindows)
        resolveWindowsFonts();
      if (logging)
        logPlatformFontInfo();
    }
    return fontToFileMap;
  }

  private static void logPlatformFontInfo()
  {
    String str;
    for (int i = 0; i < pathDirs.length; ++i)
      logger.info("fontdir=" + pathDirs[i]);
    Iterator localIterator = fontToFileMap.keySet().iterator();
    while (localIterator.hasNext())
    {
      str = (String)localIterator.next();
      logger.info("font=" + str + " file=" + ((String)fontToFileMap.get(str)));
    }
    localIterator = fontToFamilyNameMap.keySet().iterator();
    while (localIterator.hasNext())
    {
      str = (String)localIterator.next();
      logger.info("font=" + str + " family=" + ((String)fontToFamilyNameMap.get(str)));
    }
    localIterator = familyToFontListMap.keySet().iterator();
    while (localIterator.hasNext())
    {
      str = (String)localIterator.next();
      logger.info("family=" + str + " fonts=" + familyToFontListMap.get(str));
    }
  }

  public static String[] getFontNamesFromPlatform()
  {
    if (getFullNameToFileMap().size() == 0)
      return null;
    ArrayList localArrayList1 = new ArrayList();
    Iterator localIterator1 = familyToFontListMap.values().iterator();
    while (localIterator1.hasNext())
    {
      ArrayList localArrayList2 = (ArrayList)localIterator1.next();
      Iterator localIterator2 = localArrayList2.iterator();
      while (localIterator2.hasNext())
      {
        String str = (String)localIterator2.next();
        localArrayList1.add(str);
      }
    }
    return ((String[])localArrayList1.toArray(STR_ARRAY));
  }

  public static boolean gotFontsFromPlatform()
  {
    return (getFullNameToFileMap().size() != 0);
  }

  public static String getFileNameForFontName(String paramString)
  {
    String str = paramString.toLowerCase(Locale.ENGLISH);
    return ((String)fontToFileMap.get(str));
  }

  public static void registerOtherFontFiles(HashSet paramHashSet)
  {
    if (getFullNameToFileMap().size() == 0)
      return;
    String[] arrayOfString = (String[])fontToFileMap.values().toArray(STR_ARRAY);
    label127: for (int i = 0; i < arrayOfString.length; ++i)
      if ((new File(arrayOfString[i]).isAbsolute()) && (!(paramHashSet.contains(arrayOfString[i]))))
      {
        int j = -1;
        int k = 6;
        if (SunGraphicsEnvironment.ttFilter.accept(null, arrayOfString[i]))
        {
          j = 0;
          k = 3;
        }
        else if (SunGraphicsEnvironment.t1Filter.accept(null, arrayOfString[i]))
        {
          j = 1;
          k = 4;
        }
        if (j == -1)
          break label127:
        registerFontFile(arrayOfString[i], null, j, false, k);
      }
  }

  public static boolean getFamilyNamesFromPlatform(TreeMap<String, String> paramTreeMap, Locale paramLocale)
  {
    if (getFullNameToFileMap().size() == 0)
      return false;
    String[] arrayOfString = (String[])fontToFamilyNameMap.values().toArray(STR_ARRAY);
    for (int i = 0; i < arrayOfString.length; ++i)
      paramTreeMap.put(arrayOfString[i].toLowerCase(paramLocale), arrayOfString[i]);
    return true;
  }

  private static String getPathName(String paramString)
  {
    File localFile = new File(paramString);
    if (localFile.isAbsolute())
      return paramString;
    if (pathDirs.length == 1)
      return pathDirs[0] + File.separator + paramString;
    for (int i = 0; i < pathDirs.length; ++i)
    {
      localFile = new File(pathDirs[i] + File.separator + paramString);
      if (localFile.exists())
        return localFile.getAbsolutePath();
    }
    return paramString;
  }

  private static Font2D findFontFromPlatform(String paramString, int paramInt)
  {
    if (getFullNameToFileMap().size() == 0)
      return null;
    ArrayList localArrayList = null;
    String str1 = null;
    String str2 = (String)fontToFamilyNameMap.get(paramString);
    if (str2 != null)
    {
      str1 = (String)fontToFileMap.get(paramString);
      localArrayList = (ArrayList)familyToFontListMap.get(str2.toLowerCase(Locale.ENGLISH));
    }
    else
    {
      localArrayList = (ArrayList)familyToFontListMap.get(paramString);
      if ((localArrayList != null) && (localArrayList.size() > 0))
      {
        localObject1 = ((String)localArrayList.get(0)).toLowerCase(Locale.ENGLISH);
        if (localObject1 != null)
          str2 = (String)fontToFamilyNameMap.get(localObject1);
      }
    }
    if ((localArrayList == null) || (str2 == null))
      return null;
    Object localObject1 = (String[])(String[])localArrayList.toArray(STR_ARRAY);
    if (localObject1.length == 0)
      return null;
    for (int i = 0; i < localObject1.length; ++i)
    {
      String str3 = localObject1[i].toLowerCase(Locale.ENGLISH);
      localObject2 = (String)fontToFileMap.get(str3);
      if (localObject2 == null)
      {
        if (logging)
          logger.info("Platform lookup : No file for font " + localObject1[i] + " in family " + str2);
        return null;
      }
    }
    PhysicalFont localPhysicalFont = null;
    if (str1 != null)
      localPhysicalFont = registerFontFile(getPathName(str1), null, 0, false, 3);
    for (int j = 0; j < localObject1.length; ++j)
    {
      localObject2 = localObject1[j].toLowerCase(Locale.ENGLISH);
      String str4 = (String)fontToFileMap.get(localObject2);
      if ((str1 != null) && (str1.equals(str4)))
        break label335:
      label335: registerFontFile(getPathName(str4), null, 0, false, 3);
    }
    Font2D localFont2D = null;
    Object localObject2 = FontFamily.getFamily(str2);
    if (localPhysicalFont != null)
      paramInt |= localPhysicalFont.style;
    if (localObject2 != null)
    {
      localFont2D = ((FontFamily)localObject2).getFont(paramInt);
      if (localFont2D == null)
        localFont2D = ((FontFamily)localObject2).getClosestStyle(paramInt);
    }
    return ((Font2D)(Font2D)localFont2D);
  }

  public static Font2D findFont2D(String paramString, int paramInt1, int paramInt2)
  {
    Object localObject3;
    String str1 = paramString.toLowerCase(Locale.ENGLISH);
    String str2 = str1 + dotStyleStr(paramInt1);
    if (usingPerAppContextComposites)
    {
      localObject2 = (Hashtable)AppContext.getAppContext().get(CompositeFont.class);
      if (localObject2 != null)
        localObject1 = (Font2D)((Hashtable)localObject2).get(str2);
      else
        localObject1 = null;
    }
    else
    {
      localObject1 = (Font2D)fontNameCache.get(str2);
    }
    if (localObject1 != null)
      return localObject1;
    if (logging)
      logger.info("Search for font: " + paramString);
    if (isWindows)
      if (str1.equals("ms sans serif"))
        paramString = "sansserif";
      else if (str1.equals("ms serif"))
        paramString = "serif";
    if (str1.equals("default"))
      paramString = "dialog";
    Object localObject2 = FontFamily.getFamily(paramString);
    if (localObject2 != null)
    {
      localObject1 = ((FontFamily)localObject2).getFontWithExactStyleMatch(paramInt1);
      if (localObject1 == null)
        localObject1 = findDeferredFont(paramString, paramInt1);
      if (localObject1 == null)
        localObject1 = ((FontFamily)localObject2).getFont(paramInt1);
      if (localObject1 == null)
        localObject1 = ((FontFamily)localObject2).getClosestStyle(paramInt1);
      if (localObject1 != null)
      {
        fontNameCache.put(str2, localObject1);
        return localObject1;
      }
    }
    Object localObject1 = (Font2D)fullNameToFont.get(str1);
    if (localObject1 != null)
    {
      if ((((Font2D)localObject1).style == paramInt1) || (paramInt1 == 0))
      {
        fontNameCache.put(str2, localObject1);
        return localObject1;
      }
      localObject2 = FontFamily.getFamily(((Font2D)localObject1).getFamilyName(null));
      if (localObject2 != null)
      {
        localObject3 = ((FontFamily)localObject2).getFont(paramInt1 | ((Font2D)localObject1).style);
        if (localObject3 != null)
        {
          fontNameCache.put(str2, localObject3);
          return localObject3;
        }
        localObject3 = ((FontFamily)localObject2).getClosestStyle(paramInt1 | ((Font2D)localObject1).style);
        if ((localObject3 != null) && (((Font2D)localObject3).canDoStyle(paramInt1 | ((Font2D)localObject1).style)))
        {
          fontNameCache.put(str2, localObject3);
          return localObject3;
        }
      }
    }
    if (sgEnv == null)
    {
      initSGEnv();
      return findFont2D(paramString, paramInt1, paramInt2);
    }
    if (isWindows)
    {
      if (deferredFontFiles.size() > 0)
      {
        localObject1 = findJREDeferredFont(str1, paramInt1);
        if (localObject1 != null)
        {
          fontNameCache.put(str2, localObject1);
          return localObject1;
        }
      }
      localObject1 = findFontFromPlatform(str1, paramInt1);
      if (localObject1 != null)
      {
        if (logging)
          logger.info("Found font via platform API for request:\"" + paramString + "\":, style=" + paramInt1 + " found font: " + localObject1);
        fontNameCache.put(str2, localObject1);
        return localObject1;
      }
    }
    if (deferredFontFiles.size() > 0)
    {
      localObject1 = findDeferredFont(paramString, paramInt1);
      if (localObject1 != null)
      {
        fontNameCache.put(str2, localObject1);
        return localObject1;
      }
    }
    if ((isSolaris) && (!(loaded1dot0Fonts)))
    {
      if (str1.equals("timesroman"))
      {
        localObject1 = findFont2D("serif", paramInt1, paramInt2);
        fontNameCache.put(str2, localObject1);
      }
      sgEnv.register1dot0Fonts();
      loaded1dot0Fonts = true;
      localObject3 = findFont2D(paramString, paramInt1, paramInt2);
      return localObject3;
    }
    if ((fontsAreRegistered) || (fontsAreRegisteredPerAppContext))
    {
      Hashtable localHashtable;
      localObject3 = null;
      if (fontsAreRegistered)
      {
        localObject3 = createdByFamilyName;
        localHashtable = createdByFullName;
      }
      else
      {
        AppContext localAppContext = AppContext.getAppContext();
        localObject3 = (Hashtable)localAppContext.get(regFamilyKey);
        localHashtable = (Hashtable)localAppContext.get(regFullNameKey);
      }
      localObject2 = (FontFamily)((Hashtable)localObject3).get(str1);
      if (localObject2 != null)
      {
        localObject1 = ((FontFamily)localObject2).getFontWithExactStyleMatch(paramInt1);
        if (localObject1 == null)
          localObject1 = ((FontFamily)localObject2).getFont(paramInt1);
        if (localObject1 == null)
          localObject1 = ((FontFamily)localObject2).getClosestStyle(paramInt1);
        if (localObject1 != null)
        {
          if (fontsAreRegistered)
            fontNameCache.put(str2, localObject1);
          return localObject1;
        }
      }
      localObject1 = (Font2D)localHashtable.get(str1);
      if (localObject1 != null)
      {
        if (fontsAreRegistered)
          fontNameCache.put(str2, localObject1);
        return localObject1;
      }
    }
    if (!(loadedAllFonts))
    {
      if (logging)
        logger.info("Load fonts looking for:" + paramString);
      sgEnv.loadFonts();
      loadedAllFonts = true;
      return findFont2D(paramString, paramInt1, paramInt2);
    }
    if (!(loadedAllFontFiles))
    {
      if (logging)
        logger.info("Load font files looking for:" + paramString);
      sgEnv.loadFontFiles();
      loadedAllFontFiles = true;
      return findFont2D(paramString, paramInt1, paramInt2);
    }
    if ((localObject1 = findFont2DAllLocales(paramString, paramInt1)) != null)
    {
      fontNameCache.put(str2, localObject1);
      return localObject1;
    }
    if (isWindows)
    {
      localObject3 = sgEnv.getFontConfiguration().getFallbackFamilyName(paramString, null);
      if (localObject3 != null)
      {
        localObject1 = findFont2D((String)localObject3, paramInt1, paramInt2);
        fontNameCache.put(str2, localObject1);
        return localObject1;
      }
    }
    else
    {
      if (str1.equals("timesroman"))
      {
        localObject1 = findFont2D("serif", paramInt1, paramInt2);
        fontNameCache.put(str2, localObject1);
        return localObject1;
      }
      if (str1.equals("helvetica"))
      {
        localObject1 = findFont2D("sansserif", paramInt1, paramInt2);
        fontNameCache.put(str2, localObject1);
        return localObject1;
      }
      if (str1.equals("courier"))
      {
        localObject1 = findFont2D("monospaced", paramInt1, paramInt2);
        fontNameCache.put(str2, localObject1);
        return localObject1;
      }
    }
    if (logging)
      logger.info("No font found for:" + paramString);
    switch (paramInt2)
    {
    case 1:
      return getDefaultPhysicalFont();
    case 2:
      return getDefaultLogicalFont(paramInt1);
    }
    return ((Font2D)(Font2D)(Font2D)null);
  }

  public static native Font2D getFont2D(Font paramFont);

  public static boolean usePlatformFontMetrics()
  {
    return usePlatformFontMetrics;
  }

  static native boolean getPlatformFontVar();

  public static short getLCIDFromLocale(Locale paramLocale)
  {
    if (paramLocale.equals(Locale.US))
      return 1033;
    if (lcidMap == null)
      createLCIDMap();
    for (String str = paramLocale.toString(); !("".equals(str)); str = str.substring(0, i))
    {
      Short localShort = (Short)lcidMap.get(str);
      if (localShort != null)
        return localShort.shortValue();
      int i = str.lastIndexOf(95);
      if (i < 1)
        return 1033;
    }
    return 1033;
  }

  private static void addLCIDMapEntry(Map paramMap, String paramString, short paramShort)
  {
    paramMap.put(paramString, new Short(paramShort));
  }

  private static synchronized void createLCIDMap()
  {
    if (lcidMap != null)
      return;
    HashMap localHashMap = new HashMap(200);
    addLCIDMapEntry(localHashMap, "ar", 1025);
    addLCIDMapEntry(localHashMap, "bg", 1026);
    addLCIDMapEntry(localHashMap, "ca", 1027);
    addLCIDMapEntry(localHashMap, "zh", 1028);
    addLCIDMapEntry(localHashMap, "cs", 1029);
    addLCIDMapEntry(localHashMap, "da", 1030);
    addLCIDMapEntry(localHashMap, "de", 1031);
    addLCIDMapEntry(localHashMap, "el", 1032);
    addLCIDMapEntry(localHashMap, "es", 1034);
    addLCIDMapEntry(localHashMap, "fi", 1035);
    addLCIDMapEntry(localHashMap, "fr", 1036);
    addLCIDMapEntry(localHashMap, "iw", 1037);
    addLCIDMapEntry(localHashMap, "hu", 1038);
    addLCIDMapEntry(localHashMap, "is", 1039);
    addLCIDMapEntry(localHashMap, "it", 1040);
    addLCIDMapEntry(localHashMap, "ja", 1041);
    addLCIDMapEntry(localHashMap, "ko", 1042);
    addLCIDMapEntry(localHashMap, "nl", 1043);
    addLCIDMapEntry(localHashMap, "no", 1044);
    addLCIDMapEntry(localHashMap, "pl", 1045);
    addLCIDMapEntry(localHashMap, "pt", 1046);
    addLCIDMapEntry(localHashMap, "rm", 1047);
    addLCIDMapEntry(localHashMap, "ro", 1048);
    addLCIDMapEntry(localHashMap, "ru", 1049);
    addLCIDMapEntry(localHashMap, "hr", 1050);
    addLCIDMapEntry(localHashMap, "sk", 1051);
    addLCIDMapEntry(localHashMap, "sq", 1052);
    addLCIDMapEntry(localHashMap, "sv", 1053);
    addLCIDMapEntry(localHashMap, "th", 1054);
    addLCIDMapEntry(localHashMap, "tr", 1055);
    addLCIDMapEntry(localHashMap, "ur", 1056);
    addLCIDMapEntry(localHashMap, "in", 1057);
    addLCIDMapEntry(localHashMap, "uk", 1058);
    addLCIDMapEntry(localHashMap, "be", 1059);
    addLCIDMapEntry(localHashMap, "sl", 1060);
    addLCIDMapEntry(localHashMap, "et", 1061);
    addLCIDMapEntry(localHashMap, "lv", 1062);
    addLCIDMapEntry(localHashMap, "lt", 1063);
    addLCIDMapEntry(localHashMap, "fa", 1065);
    addLCIDMapEntry(localHashMap, "vi", 1066);
    addLCIDMapEntry(localHashMap, "hy", 1067);
    addLCIDMapEntry(localHashMap, "eu", 1069);
    addLCIDMapEntry(localHashMap, "mk", 1071);
    addLCIDMapEntry(localHashMap, "tn", 1074);
    addLCIDMapEntry(localHashMap, "xh", 1076);
    addLCIDMapEntry(localHashMap, "zu", 1077);
    addLCIDMapEntry(localHashMap, "af", 1078);
    addLCIDMapEntry(localHashMap, "ka", 1079);
    addLCIDMapEntry(localHashMap, "fo", 1080);
    addLCIDMapEntry(localHashMap, "hi", 1081);
    addLCIDMapEntry(localHashMap, "mt", 1082);
    addLCIDMapEntry(localHashMap, "se", 1083);
    addLCIDMapEntry(localHashMap, "gd", 1084);
    addLCIDMapEntry(localHashMap, "ms", 1086);
    addLCIDMapEntry(localHashMap, "kk", 1087);
    addLCIDMapEntry(localHashMap, "ky", 1088);
    addLCIDMapEntry(localHashMap, "sw", 1089);
    addLCIDMapEntry(localHashMap, "tt", 1092);
    addLCIDMapEntry(localHashMap, "bn", 1093);
    addLCIDMapEntry(localHashMap, "pa", 1094);
    addLCIDMapEntry(localHashMap, "gu", 1095);
    addLCIDMapEntry(localHashMap, "ta", 1097);
    addLCIDMapEntry(localHashMap, "te", 1098);
    addLCIDMapEntry(localHashMap, "kn", 1099);
    addLCIDMapEntry(localHashMap, "ml", 1100);
    addLCIDMapEntry(localHashMap, "mr", 1102);
    addLCIDMapEntry(localHashMap, "sa", 1103);
    addLCIDMapEntry(localHashMap, "mn", 1104);
    addLCIDMapEntry(localHashMap, "cy", 1106);
    addLCIDMapEntry(localHashMap, "gl", 1110);
    addLCIDMapEntry(localHashMap, "dv", 1125);
    addLCIDMapEntry(localHashMap, "qu", 1131);
    addLCIDMapEntry(localHashMap, "mi", 1153);
    addLCIDMapEntry(localHashMap, "ar_IQ", 2049);
    addLCIDMapEntry(localHashMap, "zh_CN", 2052);
    addLCIDMapEntry(localHashMap, "de_CH", 2055);
    addLCIDMapEntry(localHashMap, "en_GB", 2057);
    addLCIDMapEntry(localHashMap, "es_MX", 2058);
    addLCIDMapEntry(localHashMap, "fr_BE", 2060);
    addLCIDMapEntry(localHashMap, "it_CH", 2064);
    addLCIDMapEntry(localHashMap, "nl_BE", 2067);
    addLCIDMapEntry(localHashMap, "no_NO_NY", 2068);
    addLCIDMapEntry(localHashMap, "pt_PT", 2070);
    addLCIDMapEntry(localHashMap, "ro_MD", 2072);
    addLCIDMapEntry(localHashMap, "ru_MD", 2073);
    addLCIDMapEntry(localHashMap, "sr_CS", 2074);
    addLCIDMapEntry(localHashMap, "sv_FI", 2077);
    addLCIDMapEntry(localHashMap, "az_AZ", 2092);
    addLCIDMapEntry(localHashMap, "se_SE", 2107);
    addLCIDMapEntry(localHashMap, "ga_IE", 2108);
    addLCIDMapEntry(localHashMap, "ms_BN", 2110);
    addLCIDMapEntry(localHashMap, "uz_UZ", 2115);
    addLCIDMapEntry(localHashMap, "qu_EC", 2155);
    addLCIDMapEntry(localHashMap, "ar_EG", 3073);
    addLCIDMapEntry(localHashMap, "zh_HK", 3076);
    addLCIDMapEntry(localHashMap, "de_AT", 3079);
    addLCIDMapEntry(localHashMap, "en_AU", 3081);
    addLCIDMapEntry(localHashMap, "fr_CA", 3084);
    addLCIDMapEntry(localHashMap, "sr_CS", 3098);
    addLCIDMapEntry(localHashMap, "se_FI", 3131);
    addLCIDMapEntry(localHashMap, "qu_PE", 3179);
    addLCIDMapEntry(localHashMap, "ar_LY", 4097);
    addLCIDMapEntry(localHashMap, "zh_SG", 4100);
    addLCIDMapEntry(localHashMap, "de_LU", 4103);
    addLCIDMapEntry(localHashMap, "en_CA", 4105);
    addLCIDMapEntry(localHashMap, "es_GT", 4106);
    addLCIDMapEntry(localHashMap, "fr_CH", 4108);
    addLCIDMapEntry(localHashMap, "hr_BA", 4122);
    addLCIDMapEntry(localHashMap, "ar_DZ", 5121);
    addLCIDMapEntry(localHashMap, "zh_MO", 5124);
    addLCIDMapEntry(localHashMap, "de_LI", 5127);
    addLCIDMapEntry(localHashMap, "en_NZ", 5129);
    addLCIDMapEntry(localHashMap, "es_CR", 5130);
    addLCIDMapEntry(localHashMap, "fr_LU", 5132);
    addLCIDMapEntry(localHashMap, "bs_BA", 5146);
    addLCIDMapEntry(localHashMap, "ar_MA", 6145);
    addLCIDMapEntry(localHashMap, "en_IE", 6153);
    addLCIDMapEntry(localHashMap, "es_PA", 6154);
    addLCIDMapEntry(localHashMap, "fr_MC", 6156);
    addLCIDMapEntry(localHashMap, "sr_BA", 6170);
    addLCIDMapEntry(localHashMap, "ar_TN", 7169);
    addLCIDMapEntry(localHashMap, "en_ZA", 7177);
    addLCIDMapEntry(localHashMap, "es_DO", 7178);
    addLCIDMapEntry(localHashMap, "sr_BA", 7194);
    addLCIDMapEntry(localHashMap, "ar_OM", 8193);
    addLCIDMapEntry(localHashMap, "en_JM", 8201);
    addLCIDMapEntry(localHashMap, "es_VE", 8202);
    addLCIDMapEntry(localHashMap, "ar_YE", 9217);
    addLCIDMapEntry(localHashMap, "es_CO", 9226);
    addLCIDMapEntry(localHashMap, "ar_SY", 10241);
    addLCIDMapEntry(localHashMap, "en_BZ", 10249);
    addLCIDMapEntry(localHashMap, "es_PE", 10250);
    addLCIDMapEntry(localHashMap, "ar_JO", 11265);
    addLCIDMapEntry(localHashMap, "en_TT", 11273);
    addLCIDMapEntry(localHashMap, "es_AR", 11274);
    addLCIDMapEntry(localHashMap, "ar_LB", 12289);
    addLCIDMapEntry(localHashMap, "en_ZW", 12297);
    addLCIDMapEntry(localHashMap, "es_EC", 12298);
    addLCIDMapEntry(localHashMap, "ar_KW", 13313);
    addLCIDMapEntry(localHashMap, "en_PH", 13321);
    addLCIDMapEntry(localHashMap, "es_CL", 13322);
    addLCIDMapEntry(localHashMap, "ar_AE", 14337);
    addLCIDMapEntry(localHashMap, "es_UY", 14346);
    addLCIDMapEntry(localHashMap, "ar_BH", 15361);
    addLCIDMapEntry(localHashMap, "es_PY", 15370);
    addLCIDMapEntry(localHashMap, "ar_QA", 16385);
    addLCIDMapEntry(localHashMap, "es_BO", 16394);
    addLCIDMapEntry(localHashMap, "es_SV", 17418);
    addLCIDMapEntry(localHashMap, "es_HN", 18442);
    addLCIDMapEntry(localHashMap, "es_NI", 19466);
    addLCIDMapEntry(localHashMap, "es_PR", 20490);
    lcidMap = localHashMap;
  }

  public static int getNumFonts()
  {
    return (physicalFonts.size() + maxCompFont);
  }

  private static boolean fontSupportsEncoding(Font paramFont, String paramString)
  {
    return getFont2D(paramFont).supportsEncoding(paramString);
  }

  public static synchronized native String getFontPath(boolean paramBoolean);

  public static synchronized native void setNativeFontPath(String paramString);

  public static Font2D createFont2D(File paramFile, int paramInt, boolean paramBoolean)
    throws FontFormatException
  {
    String str = paramFile.getPath();
    Object localObject1 = null;
    File localFile = paramFile;
    try
    {
      switch (paramInt)
      {
      case 0:
        localObject1 = new TrueTypeFont(str, null, 0, true);
        break;
      case 1:
        localObject1 = new Type1Font(str, null);
        break;
      default:
        throw new FontFormatException("Unrecognised Font Format");
      }
    }
    catch (FontFormatException localFontFormatException)
    {
      if (paramBoolean)
        AccessController.doPrivileged(new PrivilegedAction(localFile)
        {
          public Object run()
          {
            this.val$fFile.delete();
            return null;
          }
        });
      throw localFontFormatException;
    }
    if (paramBoolean)
    {
      ((FileFont)localObject1).setFileToRemove(paramFile);
      synchronized (FontManager.class)
      {
        if (tmpFontFiles == null)
          tmpFontFiles = new java.util.Vector();
        tmpFontFiles.add(paramFile);
        if (fileCloser == null)
        {
          4 local4 = new Runnable()
          {
            public void run()
            {
              AccessController.doPrivileged(new PrivilegedAction(this)
              {
                public Object run()
                {
                  for (int i = 0; i < 20; ++i)
                    if (FontManager.access$400()[i] != null)
                      try
                      {
                        FontManager.access$400()[i].close();
                      }
                      catch (Exception localException1)
                      {
                      }
                  if (FontManager.tmpFontFiles != null)
                  {
                    File[] arrayOfFile = new File[FontManager.tmpFontFiles.size()];
                    arrayOfFile = (File[])FontManager.tmpFontFiles.toArray(arrayOfFile);
                    for (int j = 0; j < arrayOfFile.length; ++j)
                      try
                      {
                        arrayOfFile[j].delete();
                      }
                      catch (Exception localException2)
                      {
                      }
                  }
                  return null;
                }
              });
            }
          };
          AccessController.doPrivileged(new PrivilegedAction(local4)
          {
            public Object run()
            {
              Object localObject1 = Thread.currentThread().getThreadGroup();
              for (Object localObject2 = localObject1; localObject2 != null; localObject2 = ((ThreadGroup)localObject1).getParent())
                localObject1 = localObject2;
              FontManager.access$502(new Thread((ThreadGroup)localObject1, this.val$fileCloserRunnable));
              Runtime.getRuntime().addShutdownHook(FontManager.access$500());
              return null;
            }
          });
        }
      }
    }
    return ((Font2D)localObject1);
  }

  public static synchronized String getFullNameByFileName(String paramString)
  {
    PhysicalFont[] arrayOfPhysicalFont = getPhysicalFonts();
    for (int i = 0; i < arrayOfPhysicalFont.length; ++i)
      if (arrayOfPhysicalFont[i].platName.equals(paramString))
        return arrayOfPhysicalFont[i].getFontName(null);
    return null;
  }

  public static synchronized void deRegisterBadFont(Font2D paramFont2D)
  {
    if (!(paramFont2D instanceof PhysicalFont))
      return;
    if (logging)
      logger.severe("Deregister bad font: " + paramFont2D);
    replaceFont((PhysicalFont)paramFont2D, getDefaultPhysicalFont());
  }

  public static synchronized void replaceFont(PhysicalFont paramPhysicalFont1, PhysicalFont paramPhysicalFont2)
  {
    if (paramPhysicalFont1.handle.font2D != paramPhysicalFont1)
      return;
    paramPhysicalFont1.handle.font2D = paramPhysicalFont2;
    physicalFonts.remove(paramPhysicalFont1.fullName);
    fullNameToFont.remove(paramPhysicalFont1.fullName.toLowerCase(Locale.ENGLISH));
    FontFamily.remove(paramPhysicalFont1);
    if (localeFullNamesToFont != null)
    {
      Map.Entry[] arrayOfEntry = (Map.Entry[])(Map.Entry[])localeFullNamesToFont.entrySet().toArray(new Map.Entry[0]);
      for (int j = 0; j < arrayOfEntry.length; ++j)
        if (arrayOfEntry[j].getValue() == paramPhysicalFont1)
          try
          {
            arrayOfEntry[j].setValue(paramPhysicalFont2);
          }
          catch (Exception localException)
          {
            localeFullNamesToFont.remove(arrayOfEntry[j].getKey());
          }
    }
    for (int i = 0; i < maxCompFont; ++i)
      if (paramPhysicalFont2.getRank() > 2)
        compFonts[i].replaceComponentFont(paramPhysicalFont1, paramPhysicalFont2);
  }

  private static synchronized void loadLocaleNames()
  {
    if (localeFullNamesToFont != null)
      return;
    localeFullNamesToFont = new HashMap();
    Font2D[] arrayOfFont2D = getRegisteredFonts();
    for (int i = 0; i < arrayOfFont2D.length; ++i)
      if (arrayOfFont2D[i] instanceof TrueTypeFont)
      {
        TrueTypeFont localTrueTypeFont = (TrueTypeFont)arrayOfFont2D[i];
        String[] arrayOfString = localTrueTypeFont.getAllFullNames();
        for (int j = 0; j < arrayOfString.length; ++j)
          localeFullNamesToFont.put(arrayOfString[j], localTrueTypeFont);
        FontFamily localFontFamily = FontFamily.getFamily(localTrueTypeFont.familyName);
        if (localFontFamily != null)
          FontFamily.addLocaleNames(localFontFamily, localTrueTypeFont.getAllFamilyNames());
      }
  }

  private static Font2D findFont2DAllLocales(String paramString, int paramInt)
  {
    if (logging)
      logger.info("Searching localised font names for:" + paramString);
    if (localeFullNamesToFont == null)
      loadLocaleNames();
    String str = paramString.toLowerCase();
    Font2D localFont2D = null;
    FontFamily localFontFamily = FontFamily.getLocaleFamily(str);
    if (localFontFamily != null)
    {
      localFont2D = localFontFamily.getFont(paramInt);
      if (localFont2D == null)
        localFont2D = localFontFamily.getClosestStyle(paramInt);
      if (localFont2D != null)
        return localFont2D;
    }
    synchronized (FontManager.class)
    {
      localFont2D = (Font2D)localeFullNamesToFont.get(paramString);
    }
    if (localFont2D != null)
    {
      if ((localFont2D.style == paramInt) || (paramInt == 0))
        return localFont2D;
      localFontFamily = FontFamily.getFamily(localFont2D.getFamilyName(null));
      if (localFontFamily != null)
      {
        ??? = localFontFamily.getFont(paramInt);
        if (??? != null)
          return ???;
        ??? = localFontFamily.getClosestStyle(paramInt);
        if (??? != null)
        {
          if (!(((Font2D)???).canDoStyle(paramInt)))
            ??? = null;
          return ???;
        }
      }
    }
    return ((Font2D)localFont2D);
  }

  static boolean maybeUsingAlternateCompositeFonts()
  {
    return ((usingAlternateComposites) || (usingPerAppContextComposites));
  }

  public static boolean usingAlternateCompositeFonts()
  {
    return ((usingAlternateComposites) || ((usingPerAppContextComposites) && (AppContext.getAppContext().get(CompositeFont.class) != null)));
  }

  private static boolean maybeMultiAppContext()
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        SecurityManager localSecurityManager = System.getSecurityManager();
        return new Boolean(localSecurityManager instanceof AppletSecurity);
      }
    });
    return localBoolean.booleanValue();
  }

  public static synchronized void useAlternateFontforJALocales()
  {
    if (!(isWindows))
      return;
    initSGEnv();
    if (!(maybeMultiAppContext()))
    {
      gAltJAFont = true;
    }
    else
    {
      AppContext localAppContext = AppContext.getAppContext();
      localAppContext.put(altJAFontKey, altJAFontKey);
    }
  }

  public static boolean usingAlternateFontforJALocales()
  {
    if (!(maybeMultiAppContext()))
      return gAltJAFont;
    AppContext localAppContext = AppContext.getAppContext();
    return (localAppContext.get(altJAFontKey) == altJAFontKey);
  }

  public static synchronized void preferLocaleFonts()
  {
    initSGEnv();
    if (!(FontConfiguration.willReorderForStartupLocale()))
      return;
    if (!(maybeMultiAppContext()))
    {
      if (gLocalePref == true)
        return;
      gLocalePref = true;
      sgEnv.createCompositeFonts(fontNameCache, gLocalePref, gPropPref);
      usingAlternateComposites = true;
    }
    else
    {
      AppContext localAppContext = AppContext.getAppContext();
      if (localAppContext.get(localeFontKey) == localeFontKey)
        return;
      localAppContext.put(localeFontKey, localeFontKey);
      boolean bool = localAppContext.get(proportionalFontKey) == proportionalFontKey;
      Hashtable localHashtable = new Hashtable();
      localAppContext.put(CompositeFont.class, localHashtable);
      usingPerAppContextComposites = true;
      sgEnv.createCompositeFonts(localHashtable, true, bool);
    }
  }

  public static synchronized void preferProportionalFonts()
  {
    if (!(FontConfiguration.hasMonoToPropMap()))
      return;
    initSGEnv();
    if (!(maybeMultiAppContext()))
    {
      if (gPropPref == true)
        return;
      gPropPref = true;
      sgEnv.createCompositeFonts(fontNameCache, gLocalePref, gPropPref);
      usingAlternateComposites = true;
    }
    else
    {
      AppContext localAppContext = AppContext.getAppContext();
      if (localAppContext.get(proportionalFontKey) == proportionalFontKey)
        return;
      localAppContext.put(proportionalFontKey, proportionalFontKey);
      boolean bool = localAppContext.get(localeFontKey) == localeFontKey;
      Hashtable localHashtable = new Hashtable();
      localAppContext.put(CompositeFont.class, localHashtable);
      usingPerAppContextComposites = true;
      sgEnv.createCompositeFonts(localHashtable, bool, true);
    }
  }

  private static HashSet getInstalledNames()
  {
    if (installedNames == null)
    {
      Locale localLocale = SunGraphicsEnvironment.getSystemStartupLocale();
      String[] arrayOfString = sgEnv.getInstalledFontFamilyNames(localLocale);
      Font[] arrayOfFont = sgEnv.getAllInstalledFonts();
      HashSet localHashSet = new HashSet();
      for (int i = 0; i < arrayOfString.length; ++i)
        localHashSet.add(arrayOfString[i].toLowerCase(localLocale));
      for (i = 0; i < arrayOfFont.length; ++i)
        localHashSet.add(arrayOfFont[i].getFontName(localLocale).toLowerCase(localLocale));
      installedNames = localHashSet;
    }
    return installedNames;
  }

  public static boolean registerFont(Font paramFont)
  {
    Hashtable localHashtable1;
    Hashtable localHashtable2;
    synchronized (regFamilyKey)
    {
      if (createdByFamilyName == null)
      {
        createdByFamilyName = new Hashtable();
        createdByFullName = new Hashtable();
      }
    }
    if (!(isCreatedFont(paramFont)))
      return false;
    if (sgEnv == null)
      initSGEnv();
    ??? = getInstalledNames();
    Locale localLocale = SunGraphicsEnvironment.getSystemStartupLocale();
    String str1 = paramFont.getFamily(localLocale).toLowerCase();
    String str2 = paramFont.getFontName(localLocale).toLowerCase();
    if ((((HashSet)???).contains(str1)) || (((HashSet)???).contains(str2)))
      return false;
    if (!(maybeMultiAppContext()))
    {
      localHashtable1 = createdByFamilyName;
      localHashtable2 = createdByFullName;
      fontsAreRegistered = true;
    }
    else
    {
      localObject3 = AppContext.getAppContext();
      localHashtable1 = (Hashtable)((AppContext)localObject3).get(regFamilyKey);
      localHashtable2 = (Hashtable)((AppContext)localObject3).get(regFullNameKey);
      if (localHashtable1 == null)
      {
        localHashtable1 = new Hashtable();
        localHashtable2 = new Hashtable();
        ((AppContext)localObject3).put(regFamilyKey, localHashtable1);
        ((AppContext)localObject3).put(regFullNameKey, localHashtable2);
      }
      fontsAreRegisteredPerAppContext = true;
    }
    Object localObject3 = getFont2D(paramFont);
    int i = ((Font2D)localObject3).getStyle();
    FontFamily localFontFamily = (FontFamily)localHashtable1.get(str1);
    if (localFontFamily == null)
    {
      localFontFamily = new FontFamily(paramFont.getFamily(localLocale));
      localHashtable1.put(str1, localFontFamily);
    }
    if (fontsAreRegistered)
    {
      removeFromCache(localFontFamily.getFont(0));
      removeFromCache(localFontFamily.getFont(1));
      removeFromCache(localFontFamily.getFont(2));
      removeFromCache(localFontFamily.getFont(3));
      removeFromCache((Font2D)localHashtable2.get(str2));
    }
    localFontFamily.setFont((Font2D)localObject3, i);
    localHashtable2.put(str2, localObject3);
    return true;
  }

  private static void removeFromCache(Font2D paramFont2D)
  {
    if (paramFont2D == null)
      return;
    String[] arrayOfString = (String[])(String[])fontNameCache.keySet().toArray(STR_ARRAY);
    for (int i = 0; i < arrayOfString.length; ++i)
      if (fontNameCache.get(arrayOfString[i]) == paramFont2D)
        fontNameCache.remove(arrayOfString[i]);
  }

  public static TreeMap<String, String> getCreatedFontFamilyNames()
  {
    Hashtable localHashtable1;
    if (fontsAreRegistered)
    {
      localHashtable1 = createdByFamilyName;
    }
    else if (fontsAreRegisteredPerAppContext)
    {
      localObject1 = AppContext.getAppContext();
      localHashtable1 = (Hashtable)((AppContext)localObject1).get(regFamilyKey);
    }
    else
    {
      return null;
    }
    Object localObject1 = SunGraphicsEnvironment.getSystemStartupLocale();
    synchronized (localHashtable1)
    {
      TreeMap localTreeMap = new TreeMap();
      Iterator localIterator = localHashtable1.values().iterator();
      while (localIterator.hasNext())
      {
        FontFamily localFontFamily = (FontFamily)localIterator.next();
        Font2D localFont2D = localFontFamily.getFont(0);
        if (localFont2D == null)
          localFont2D = localFontFamily.getClosestStyle(0);
        String str = localFont2D.getFamilyName((Locale)localObject1);
        localTreeMap.put(str.toLowerCase((Locale)localObject1), str);
      }
      return localTreeMap;
    }
  }

  public static Font[] getCreatedFonts()
  {
    Hashtable localHashtable1;
    if (fontsAreRegistered)
    {
      localHashtable1 = createdByFullName;
    }
    else if (fontsAreRegisteredPerAppContext)
    {
      localObject1 = AppContext.getAppContext();
      localHashtable1 = (Hashtable)((AppContext)localObject1).get(regFullNameKey);
    }
    else
    {
      return null;
    }
    Object localObject1 = SunGraphicsEnvironment.getSystemStartupLocale();
    synchronized (localHashtable1)
    {
      Font[] arrayOfFont = new Font[localHashtable1.size()];
      int i = 0;
      Iterator localIterator = localHashtable1.values().iterator();
      while (localIterator.hasNext())
      {
        Font2D localFont2D = (Font2D)localIterator.next();
        arrayOfFont[(i++)] = new Font(localFont2D.getFontName((Locale)localObject1), 0, 1);
      }
      return arrayOfFont;
    }
  }

  public static String mapFcName(String paramString)
  {
    for (int i = 0; i < nameMap.length; ++i)
      if (paramString.equals(nameMap[i][0]))
        return nameMap[i][1];
    return null;
  }

  private static String getFCLocaleStr()
  {
    Locale localLocale = SunToolkit.getStartupLocale();
    String str1 = localLocale.getLanguage();
    String str2 = localLocale.getCountry();
    if (!(str2.equals("")))
      str1 = str1 + "-" + str2;
    return str1;
  }

  private static native int getFontConfigAASettings(String paramString1, String paramString2);

  public static Object getFontConfigAAHint(String paramString)
  {
    if (isWindows)
      return null;
    int i = getFontConfigAASettings(getFCLocaleStr(), paramString);
    if (i < 0)
      return null;
    return SunHints.Value.get(2, i);
  }

  public static Object getFontConfigAAHint()
  {
    return getFontConfigAAHint("sans");
  }

  private static native void getFontConfig(String paramString, FontConfigInfo[] paramArrayOfFontConfigInfo);

  private static void initFontConfigFonts()
  {
    if (isWindows)
      return;
    FontConfigInfo[] arrayOfFontConfigInfo = new FontConfigInfo[fontConfigNames.length];
    for (int i = 0; i < arrayOfFontConfigInfo.length; ++i)
    {
      arrayOfFontConfigInfo[i] = new FontConfigInfo(null);
      arrayOfFontConfigInfo[i].fcName = fontConfigNames[i];
      int j = arrayOfFontConfigInfo[i].fcName.indexOf(58);
      arrayOfFontConfigInfo[i].fcFamily = arrayOfFontConfigInfo[i].fcName.substring(0, j);
      arrayOfFontConfigInfo[i].jdkName = mapFcName(arrayOfFontConfigInfo[i].fcFamily);
      arrayOfFontConfigInfo[i].style = (i % 4);
    }
    getFontConfig(getFCLocaleStr(), arrayOfFontConfigInfo);
    fontConfigFonts = arrayOfFontConfigInfo;
  }

  private static PhysicalFont registerFromFcInfo(FontConfigInfo paramFontConfigInfo)
  {
    Font2D localFont2D;
    int i = paramFontConfigInfo.fontFile.length() - 4;
    if (i <= 0)
      return null;
    String str = paramFontConfigInfo.fontFile.substring(i).toLowerCase();
    boolean bool = str.equals(".ttc");
    PhysicalFont localPhysicalFont = (PhysicalFont)registeredFontFiles.get(paramFontConfigInfo.fontFile);
    if (localPhysicalFont != null)
    {
      if (bool)
      {
        localFont2D = findFont2D(paramFontConfigInfo.familyName, paramFontConfigInfo.style, 0);
        if (localFont2D instanceof PhysicalFont)
          return ((PhysicalFont)localFont2D);
        return null;
      }
      return localPhysicalFont;
    }
    localPhysicalFont = findJREDeferredFont(paramFontConfigInfo.familyName, paramFontConfigInfo.style);
    if ((localPhysicalFont == null) && (deferredFontFiles.get(paramFontConfigInfo.fontFile) != null))
    {
      localPhysicalFont = initialiseDeferredFont(paramFontConfigInfo.fontFile);
      if (localPhysicalFont != null)
      {
        if (bool)
        {
          localFont2D = findFont2D(paramFontConfigInfo.familyName, paramFontConfigInfo.style, 0);
          if (localFont2D instanceof PhysicalFont)
            return ((PhysicalFont)localFont2D);
          return null;
        }
        return localPhysicalFont;
      }
    }
    if (localPhysicalFont == null)
    {
      int j = -1;
      int k = 6;
      if ((str.equals(".ttf")) || (bool))
      {
        j = 0;
        k = 3;
      }
      else if ((str.equals(".pfa")) || (str.equals(".pfb")))
      {
        j = 1;
        k = 4;
      }
      localPhysicalFont = registerFontFile(paramFontConfigInfo.fontFile, null, j, true, k);
    }
    return localPhysicalFont;
  }

  private static CompositeFont getFontConfigFont(String paramString, int paramInt)
  {
    PhysicalFont localPhysicalFont = null;
    if (fontConfigFonts == null)
    {
      long l1 = 3412047652030775296L;
      if (logging)
        l1 = System.currentTimeMillis();
      initFontConfigFonts();
      if (logging)
      {
        long l2 = System.currentTimeMillis();
        logger.info("Time spent accessing fontconfig=" + (l2 - l1) + "ms.");
        for (int k = 0; k < fontConfigFonts.length; ++k)
        {
          FontConfigInfo localFontConfigInfo3 = fontConfigFonts[k];
          logger.info("FC font " + localFontConfigInfo3.fcName + " maps to family " + localFontConfigInfo3.familyName + " in file " + localFontConfigInfo3.fontFile);
        }
      }
    }
    paramString = paramString.toLowerCase();
    FontConfigInfo localFontConfigInfo1 = null;
    for (int i = 0; i < fontConfigFonts.length; ++i)
      if ((paramString.equals(fontConfigFonts[i].fcFamily)) && (paramInt == fontConfigFonts[i].style))
      {
        localFontConfigInfo1 = fontConfigFonts[i];
        break;
      }
    if (localFontConfigInfo1 == null)
      localFontConfigInfo1 = fontConfigFonts[0];
    if (logging)
      logger.info("FC name=" + paramString + " style=" + paramInt + " uses " + localFontConfigInfo1.familyName + " in file: " + localFontConfigInfo1.fontFile);
    if (localFontConfigInfo1.compFont != null)
      return localFontConfigInfo1.compFont;
    CompositeFont localCompositeFont = (CompositeFont)findFont2D(localFontConfigInfo1.jdkName, paramInt, 2);
    if ((localFontConfigInfo1.familyName == null) || (localFontConfigInfo1.fontFile == null))
      return (localFontConfigInfo1.compFont = localCompositeFont);
    FontFamily localFontFamily = FontFamily.getFamily(localFontConfigInfo1.familyName);
    if (localFontFamily != null)
    {
      Font2D localFont2D = localFontFamily.getFontWithExactStyleMatch(localFontConfigInfo1.style);
      if (localFont2D instanceof PhysicalFont)
        localPhysicalFont = (PhysicalFont)localFont2D;
    }
    if ((localPhysicalFont == null) || (!(localFontConfigInfo1.fontFile.equals(localPhysicalFont.platName))))
    {
      localPhysicalFont = registerFromFcInfo(localFontConfigInfo1);
      if (localPhysicalFont == null)
        return (localFontConfigInfo1.compFont = localCompositeFont);
      localFontFamily = FontFamily.getFamily(localPhysicalFont.getFamilyName(null));
    }
    for (int j = 0; j < fontConfigFonts.length; ++j)
    {
      FontConfigInfo localFontConfigInfo2 = fontConfigFonts[j];
      if ((localFontConfigInfo2 != localFontConfigInfo1) && (localPhysicalFont.getFamilyName(null).equals(localFontConfigInfo2.familyName)) && (!(localFontConfigInfo2.fontFile.equals(localPhysicalFont.platName))) && (localFontFamily.getFontWithExactStyleMatch(localFontConfigInfo2.style) == null))
        registerFromFcInfo(fontConfigFonts[j]);
    }
    return (localFontConfigInfo1.compFont = new CompositeFont(localPhysicalFont, localCompositeFont));
  }

  public static FontUIResource getFontConfigFUIR(String paramString, int paramInt1, int paramInt2)
  {
    String str = mapFcName(paramString);
    if (str == null)
      str = "sansserif";
    if (isWindows)
      return new FontUIResource(str, paramInt1, paramInt2);
    CompositeFont localCompositeFont = getFontConfigFont(paramString, paramInt1);
    if (localCompositeFont == null)
      return new FontUIResource(str, paramInt1, paramInt2);
    FontUIResource localFontUIResource = new FontUIResource(localCompositeFont.getFamilyName(null), paramInt1, paramInt2);
    setFont2D(localFontUIResource, localCompositeFont.handle);
    setCreatedFont(localFontUIResource);
    return localFontUIResource;
  }

  static
  {
    if (SunGraphicsEnvironment.debugFonts)
    {
      logger = Logger.getLogger("sun.java2d", null);
      logging = logger.getLevel() != Level.OFF;
    }
    jreFontMap = new HashMap();
    jreFontMap.put("lucida sans0", "LucidaSansRegular.ttf");
    jreFontMap.put("lucida sans1", "LucidaSansDemiBold.ttf");
    jreFontMap.put("lucida sans regular0", "LucidaSansRegular.ttf");
    jreFontMap.put("lucida sans regular1", "LucidaSansDemiBold.ttf");
    jreFontMap.put("lucida sans bold1", "LucidaSansDemiBold.ttf");
    jreFontMap.put("lucida sans demibold1", "LucidaSansDemiBold.ttf");
    jreFontMap.put("lucida sans typewriter0", "LucidaTypewriter.ttf");
    jreFontMap.put("lucida sans typewriter1", "LucidaTypewriterBold.ttf");
    jreFontMap.put("lucida sans typewriter regular0", "LucidaTypewriter.ttf");
    jreFontMap.put("lucida sans typewriter regular1", "LucidaTypewriterBold.ttf");
    jreFontMap.put("lucida sans typewriter bold1", "LucidaTypewriterBold.ttf");
    jreFontMap.put("lucida sans typewriter demibold1", "LucidaTypewriterBold.ttf");
    jreFontMap.put("lucida bright0", "LucidaBrightRegular.ttf");
    jreFontMap.put("lucida bright1", "LucidaBrightDemiBold.ttf");
    jreFontMap.put("lucida bright2", "LucidaBrightItalic.ttf");
    jreFontMap.put("lucida bright3", "LucidaBrightDemiItalic.ttf");
    jreFontMap.put("lucida bright regular0", "LucidaBrightRegular.ttf");
    jreFontMap.put("lucida bright regular1", "LucidaBrightDemiBold.ttf");
    jreFontMap.put("lucida bright regular2", "LucidaBrightItalic.ttf");
    jreFontMap.put("lucida bright regular3", "LucidaBrightDemiItalic.ttf");
    jreFontMap.put("lucida bright bold1", "LucidaBrightDemiBold.ttf");
    jreFontMap.put("lucida bright bold3", "LucidaBrightDemiItalic.ttf");
    jreFontMap.put("lucida bright demibold1", "LucidaBrightDemiBold.ttf");
    jreFontMap.put("lucida bright demibold3", "LucidaBrightDemiItalic.ttf");
    jreFontMap.put("lucida bright italic2", "LucidaBrightItalic.ttf");
    jreFontMap.put("lucida bright italic3", "LucidaBrightDemiItalic.ttf");
    jreFontMap.put("lucida bright bold italic3", "LucidaBrightDemiItalic.ttf");
    jreFontMap.put("lucida bright demibold italic3", "LucidaBrightDemiItalic.ttf");
    jreLucidaFontFiles = new HashSet();
    String[] arrayOfString = (String[])jreFontMap.values().toArray(STR_ARRAY);
    for (int i = 0; i < arrayOfString.length; ++i)
      jreLucidaFontFiles.add(arrayOfString[i]);
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        String str2;
        System.loadLibrary("awt");
        System.loadLibrary("fontmanager");
        FontManager.access$000();
        switch (StrikeCache.nativeAddressSize)
        {
        case 8:
          FontManager.longAddresses = true;
          break;
        case 4:
          FontManager.longAddresses = false;
          break;
        default:
          throw new RuntimeException("Unexpected address size");
        }
        FontManager.osName = System.getProperty("os.name", "unknownOS");
        FontManager.isSolaris = FontManager.osName.startsWith("SunOS");
        String str1 = System.getProperty("sun.java2d.font.scaler");
        if (str1 != null)
          FontManager.useT2K = "t2k".equals(str1);
        if (FontManager.isSolaris)
        {
          str2 = System.getProperty("os.version", "unk");
          FontManager.isSolaris8 = str2.equals("5.8");
          FontManager.isSolaris9 = str2.equals("5.9");
        }
        else
        {
          FontManager.isWindows = FontManager.osName.startsWith("Windows");
          if (FontManager.isWindows)
          {
            str2 = SunGraphicsEnvironment.eudcFontFileName;
            if (str2 != null)
              try
              {
                FontManager.eudcFont = new TrueTypeFont(str2, null, 0, true);
              }
              catch (FontFormatException localFontFormatException)
              {
              }
            String str3 = System.getProperty("java2d.font.usePlatformFont");
            if (("true".equals(str3)) || (FontManager.getPlatformFontVar()))
            {
              FontManager.access$102(true);
              System.out.println("Enabling platform font metrics for win32. This is an unsupported option.");
              System.out.println("This yields incorrect composite font metrics as reported by 1.1.x releases.");
              System.out.println("It is appropriate only for use by applications which do not use any Java 2");
              System.out.println("functionality. This property will be removed in a later release.");
            }
          }
        }
        return null;
      }
    });
    deferredFontFiles = new Hashtable();
    initialisedFonts = new Hashtable();
    fontToFileMap = null;
    fontToFamilyNameMap = null;
    familyToFontListMap = null;
    pathDirs = null;
    fontNameCache = new Hashtable(10, 0.75F);
    fileCloser = null;
    tmpFontFiles = null;
    altJAFontKey = new Object();
    localeFontKey = new Object();
    proportionalFontKey = new Object();
    usingPerAppContextComposites = false;
    usingAlternateComposites = false;
    gAltJAFont = false;
    gLocalePref = false;
    gPropPref = false;
    installedNames = null;
    regFamilyKey = new Object();
    regFullNameKey = new Object();
    fontsAreRegistered = false;
    fontsAreRegisteredPerAppContext = false;
    nameMap = { { "sans", "sansserif" }, { "sans-serif", "sansserif" }, { "serif", "serif" }, { "monospace", "monospaced" } };
    fontConfigNames = { "sans:regular:roman", "sans:bold:roman", "sans:regular:italic", "sans:bold:italic", "serif:regular:roman", "serif:bold:roman", "serif:regular:italic", "serif:bold:italic", "monospace:regular:roman", "monospace:bold:roman", "monospace:regular:italic", "monospace:bold:italic" };
  }

  private static class FontConfigInfo
  {
    String fcName;
    String fcFamily;
    String jdkName;
    int style;
    String familyName;
    String fontFile;
    CompositeFont compFont;
  }

  private static final class FontRegistrationInfo
  {
    String fontFilePath;
    String[] nativeNames;
    int fontFormat;
    boolean javaRasterizer;
    int fontRank;

    FontRegistrationInfo(String paramString, String[] paramArrayOfString, int paramInt1, boolean paramBoolean, int paramInt2)
    {
      this.fontFilePath = paramString;
      this.nativeNames = paramArrayOfString;
      this.fontFormat = paramInt1;
      this.javaRasterizer = paramBoolean;
      this.fontRank = paramInt2;
    }
  }
}
package sun.awt;

import S;
import [I;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import sun.font.CompositeFontDescriptor;
import sun.font.FontManager;
import sun.java2d.SunGraphicsEnvironment;

public abstract class FontConfiguration
{
  protected static String osVersion;
  protected static String osName;
  protected static String encoding;
  protected static Locale startupLocale;
  protected static Hashtable localeMap;
  private static FontConfiguration fontConfig;
  private static Logger logger;
  protected static boolean isProperties;
  protected SunGraphicsEnvironment environment;
  protected boolean preferLocaleFonts;
  protected boolean preferPropFonts;
  private static short stringIDNum;
  private static short[] stringIDs;
  private static StringBuilder stringTable;
  public static boolean verbose;
  private short initELC = -1;
  private Locale initLocale;
  private String initEncoding;
  private String alphabeticSuffix;
  private short[][][] compFontNameIDs = new short[5][4];
  private int[][][] compExclusions = new int[5][][];
  private int[] compCoreNum = new int[5];
  private Set<Short> coreFontNameIDs = new HashSet();
  private Set<Short> fallbackFontNameIDs = new HashSet();
  protected static final int NUM_FONTS = 5;
  protected static final int NUM_STYLES = 4;
  private static final String[] fontNames;
  private static final String[] publicFontNames;
  private static final String[] styleNames;
  private static String[] installedFallbackFontFiles;
  protected HashMap reorderMap = null;
  private Hashtable charsetRegistry = new Hashtable(5);
  private FontDescriptor[][][] fontDescriptors = new FontDescriptor[5][4];
  HashMap<String, Boolean> existsMap;
  private int numCoreFonts = -1;
  private String[] componentFonts = null;
  HashMap<String, String> filenamesMap = new HashMap();
  HashSet<String> coreFontFileNames = new HashSet();
  private static final int HEAD_LENGTH = 20;
  private static final int INDEX_scriptIDs = 0;
  private static final int INDEX_scriptFonts = 1;
  private static final int INDEX_elcIDs = 2;
  private static final int INDEX_sequences = 3;
  private static final int INDEX_fontfileNameIDs = 4;
  private static final int INDEX_componentFontNameIDs = 5;
  private static final int INDEX_filenames = 6;
  private static final int INDEX_awtfontpaths = 7;
  private static final int INDEX_exclusions = 8;
  private static final int INDEX_proportionals = 9;
  private static final int INDEX_scriptFontsMotif = 10;
  private static final int INDEX_alphabeticSuffix = 11;
  private static final int INDEX_stringIDs = 12;
  private static final int INDEX_stringTable = 13;
  private static final int INDEX_TABLEEND = 14;
  private static final int INDEX_fallbackScripts = 15;
  private static final int INDEX_appendedfontpath = 16;
  private static final int INDEX_version = 17;
  private static short[] head;
  private static short[] table_scriptIDs;
  private static short[] table_scriptFonts;
  private static short[] table_elcIDs;
  private static short[] table_sequences;
  private static short[] table_fontfileNameIDs;
  private static short[] table_componentFontNameIDs;
  private static short[] table_filenames;
  protected static short[] table_awtfontpaths;
  private static short[] table_exclusions;
  private static short[] table_proportionals;
  private static short[] table_scriptFontsMotif;
  private static short[] table_alphabeticSuffix;
  private static short[] table_stringIDs;
  private static char[] table_stringTable;
  private HashMap<String, Short> reorderScripts;
  private static String[] stringCache;
  private static final int[] EMPTY_INT_ARRAY;
  private static final String[] EMPTY_STRING_ARRAY;
  private static final short[] EMPTY_SHORT_ARRAY;
  private static final String UNDEFINED_COMPONENT_FONT = "unknown";

  public FontConfiguration(SunGraphicsEnvironment paramSunGraphicsEnvironment)
  {
    if ((SunGraphicsEnvironment.debugFonts) && (logger == null))
      logger = Logger.getLogger("sun.awt.FontConfiguration");
    this.environment = paramSunGraphicsEnvironment;
    this.preferLocaleFonts = false;
    this.preferPropFonts = false;
    setOsNameAndVersion();
    setEncoding();
    fontConfig = this;
    readFontConfigFile();
    initFontConfig();
  }

  public FontConfiguration(SunGraphicsEnvironment paramSunGraphicsEnvironment, boolean paramBoolean1, boolean paramBoolean2)
  {
    this.environment = paramSunGraphicsEnvironment;
    this.preferLocaleFonts = paramBoolean1;
    this.preferPropFonts = paramBoolean2;
    initFontConfig();
  }

  protected void setOsNameAndVersion()
  {
    osName = System.getProperty("os.name");
    osVersion = System.getProperty("os.version");
  }

  private void setEncoding()
  {
    encoding = Charset.defaultCharset().name();
    startupLocale = SunToolkit.getStartupLocale();
  }

  private void readFontConfigFile()
  {
    File localFile = null;
    String str1 = System.getProperty("java.home");
    if (str1 == null)
      throw new Error("java.home property not set");
    String str2 = str1 + File.separator + "lib";
    String str3 = System.getProperty("sun.awt.fontconfig");
    if (str3 != null)
      localFile = new File(str3);
    else
      localFile = findFontConfigFile(str2);
    getInstalledFallbackFonts(str2);
    if (localFile != null)
      try
      {
        FileInputStream localFileInputStream = new FileInputStream(localFile.getPath());
        if (isProperties)
          loadProperties(localFileInputStream);
        else
          loadBinary(localFileInputStream);
        localFileInputStream.close();
        if (SunGraphicsEnvironment.debugFonts)
          logger.config("Read logical font configuration from " + localFile);
      }
      catch (IOException localIOException)
      {
        if (SunGraphicsEnvironment.debugFonts)
          logger.config("Failed to read logical font configuration from " + localFile);
      }
    String str4 = getVersion();
    if ((!("1".equals(str4))) && (SunGraphicsEnvironment.debugFonts))
      logger.config("Unsupported fontconfig version: " + str4);
  }

  private void getInstalledFallbackFonts(String paramString)
  {
    String str = paramString + File.separator + "fonts" + File.separator + "fallback";
    File localFile = new File(str);
    if ((localFile.exists()) && (localFile.isDirectory()))
    {
      String[] arrayOfString1 = localFile.list(SunGraphicsEnvironment.ttFilter);
      String[] arrayOfString2 = localFile.list(SunGraphicsEnvironment.t1Filter);
      int i = (arrayOfString1 == null) ? 0 : arrayOfString1.length;
      int j = (arrayOfString2 == null) ? 0 : arrayOfString2.length;
      int k = i + j;
      if (i + j == 0)
        return;
      installedFallbackFontFiles = new String[k];
      for (int l = 0; l < i; ++l)
        installedFallbackFontFiles[l] = localFile + File.separator + arrayOfString1[l];
      for (l = 0; l < j; ++l)
        installedFallbackFontFiles[(l + i)] = localFile + File.separator + arrayOfString2[l];
      this.environment.registerFontsInDir(str);
    }
  }

  private File findImpl(String paramString)
  {
    File localFile = new File(paramString + ".properties");
    if (localFile.canRead())
    {
      isProperties = true;
      return localFile;
    }
    localFile = new File(paramString + ".bfc");
    if (localFile.canRead())
    {
      isProperties = false;
      return localFile;
    }
    return null;
  }

  private File findFontConfigFile(String paramString)
  {
    String str = paramString + File.separator + "fontconfig";
    if ((osVersion != null) && (osName != null))
    {
      localFile = findImpl(str + "." + osName + "." + osVersion);
      if (localFile != null)
        return localFile;
    }
    if (osName != null)
    {
      localFile = findImpl(str + "." + osName);
      if (localFile != null)
        return localFile;
    }
    if (osVersion != null)
    {
      localFile = findImpl(str + "." + osVersion);
      if (localFile != null)
        return localFile;
    }
    File localFile = findImpl(str);
    if (localFile != null)
      return localFile;
    return null;
  }

  public static void loadBinary(InputStream paramInputStream)
    throws IOException
  {
    DataInputStream localDataInputStream = new DataInputStream(paramInputStream);
    head = readShortTable(localDataInputStream, 20);
    int[] arrayOfInt = new int[14];
    for (int i = 0; i < 14; ++i)
      arrayOfInt[i] = (head[(i + 1)] - head[i]);
    table_scriptIDs = readShortTable(localDataInputStream, arrayOfInt[0]);
    table_scriptFonts = readShortTable(localDataInputStream, arrayOfInt[1]);
    table_elcIDs = readShortTable(localDataInputStream, arrayOfInt[2]);
    table_sequences = readShortTable(localDataInputStream, arrayOfInt[3]);
    table_fontfileNameIDs = readShortTable(localDataInputStream, arrayOfInt[4]);
    table_componentFontNameIDs = readShortTable(localDataInputStream, arrayOfInt[5]);
    table_filenames = readShortTable(localDataInputStream, arrayOfInt[6]);
    table_awtfontpaths = readShortTable(localDataInputStream, arrayOfInt[7]);
    table_exclusions = readShortTable(localDataInputStream, arrayOfInt[8]);
    table_proportionals = readShortTable(localDataInputStream, arrayOfInt[9]);
    table_scriptFontsMotif = readShortTable(localDataInputStream, arrayOfInt[10]);
    table_alphabeticSuffix = readShortTable(localDataInputStream, arrayOfInt[11]);
    table_stringIDs = readShortTable(localDataInputStream, arrayOfInt[12]);
    stringCache = new String[table_stringIDs.length + 1];
    i = arrayOfInt[13];
    byte[] arrayOfByte = new byte[i * 2];
    table_stringTable = new char[i];
    localDataInputStream.read(arrayOfByte);
    int j = 0;
    int k = 0;
    while (j < i)
      table_stringTable[(j++)] = (char)(arrayOfByte[(k++)] << 8 | arrayOfByte[(k++)] & 0xFF);
    if (verbose)
      dump();
  }

  public static void saveBinary(OutputStream paramOutputStream)
    throws IOException
  {
    DataOutputStream localDataOutputStream = new DataOutputStream(paramOutputStream);
    writeShortTable(localDataOutputStream, head);
    writeShortTable(localDataOutputStream, table_scriptIDs);
    writeShortTable(localDataOutputStream, table_scriptFonts);
    writeShortTable(localDataOutputStream, table_elcIDs);
    writeShortTable(localDataOutputStream, table_sequences);
    writeShortTable(localDataOutputStream, table_fontfileNameIDs);
    writeShortTable(localDataOutputStream, table_componentFontNameIDs);
    writeShortTable(localDataOutputStream, table_filenames);
    writeShortTable(localDataOutputStream, table_awtfontpaths);
    writeShortTable(localDataOutputStream, table_exclusions);
    writeShortTable(localDataOutputStream, table_proportionals);
    writeShortTable(localDataOutputStream, table_scriptFontsMotif);
    writeShortTable(localDataOutputStream, table_alphabeticSuffix);
    writeShortTable(localDataOutputStream, table_stringIDs);
    localDataOutputStream.writeChars(new String(table_stringTable));
    paramOutputStream.close();
    if (verbose)
      dump();
    sanityCheck();
  }

  public static void loadProperties(InputStream paramInputStream)
    throws IOException
  {
    stringIDNum = 1;
    stringIDs = new short[1000];
    stringTable = new StringBuilder(4096);
    if ((verbose) && (logger == null))
      logger = Logger.getLogger("sun.awt.FontConfiguration");
    new PropertiesHandler().load(paramInputStream);
    stringIDs = null;
    stringTable = null;
  }

  private void initFontConfig()
  {
    this.initLocale = startupLocale;
    this.initEncoding = encoding;
    if ((this.preferLocaleFonts) && (!(willReorderForStartupLocale())))
      this.preferLocaleFonts = false;
    this.initELC = getInitELC();
    initAllComponentFonts();
  }

  private short getInitELC()
  {
    String str3;
    if (this.initELC != -1)
      return this.initELC;
    HashMap localHashMap = new HashMap();
    for (int i = 0; i < table_elcIDs.length; ++i)
      localHashMap.put(getString(table_elcIDs[i]), Integer.valueOf(i));
    String str1 = this.initLocale.getLanguage();
    String str2 = this.initLocale.getCountry();
    if (!(localHashMap.containsKey(str3 = this.initEncoding + "." + str1 + "." + str2)))
      if (!(localHashMap.containsKey(str3 = this.initEncoding + "." + str1)))
        if (!(localHashMap.containsKey(str3 = this.initEncoding)))
          break label184;
    this.initELC = ((Integer)localHashMap.get(str3)).shortValue();
    break label200:
    label184: this.initELC = ((Integer)localHashMap.get("NULL.NULL.NULL")).shortValue();
    for (int j = 0; j < table_alphabeticSuffix.length; j += 2)
      if (this.initELC == table_alphabeticSuffix[j])
      {
        label200: this.alphabeticSuffix = getString(table_alphabeticSuffix[(j + 1)]);
        return this.initELC;
      }
    return this.initELC;
  }

  private void initAllComponentFonts()
  {
    short[] arrayOfShort1 = getFallbackScripts();
    for (int i = 0; i < 5; ++i)
    {
      short[] arrayOfShort2 = getCoreScripts(i);
      this.compCoreNum[i] = arrayOfShort2.length;
      [I[] arrayOf[I = new int[arrayOfShort2.length][];
      for (int j = 0; j < arrayOfShort2.length; ++j)
        arrayOf[I[j] = getExclusionRanges(arrayOfShort2[j]);
      this.compExclusions[i] = arrayOf[I;
      for (j = 0; j < 4; ++j)
      {
        Object localObject = new short[arrayOfShort2.length + arrayOfShort1.length];
        for (int k = 0; k < arrayOfShort2.length; ++k)
        {
          localObject[k] = getComponentFontID(arrayOfShort2[k], i, j);
          if ((this.preferLocaleFonts) && (localeMap != null) && (FontManager.usingAlternateFontforJALocales()))
            localObject[k] = remapLocaleMap(i, j, arrayOfShort2[k], localObject[k]);
          if (this.preferPropFonts)
            localObject[k] = remapProportional(i, localObject[k]);
          this.coreFontNameIDs.add(Short.valueOf(localObject[k]));
        }
        for (int l = 0; l < arrayOfShort1.length; ++l)
        {
          short s = getComponentFontID(arrayOfShort1[l], i, j);
          if ((this.preferLocaleFonts) && (localeMap != null) && (FontManager.usingAlternateFontforJALocales()))
            s = remapLocaleMap(i, j, arrayOfShort1[l], s);
          if (this.preferPropFonts)
            s = remapProportional(i, s);
          if (contains(localObject, s, k))
            break label309:
          this.fallbackFontNameIDs.add(Short.valueOf(s));
          label309: localObject[(k++)] = s;
        }
        if (k < localObject.length)
        {
          short[] arrayOfShort3 = new short[k];
          System.arraycopy(localObject, 0, arrayOfShort3, 0, k);
          localObject = arrayOfShort3;
        }
        this.compFontNameIDs[i][j] = localObject;
      }
    }
  }

  private short remapLocaleMap(int paramInt1, int paramInt2, short paramShort1, short paramShort2)
  {
    String str4;
    String str1 = getString(table_scriptIDs[paramShort1]);
    String str2 = (String)localeMap.get(str1);
    if (str2 == null)
    {
      String str3 = fontNames[paramInt1];
      str4 = styleNames[paramInt2];
      str2 = (String)localeMap.get(str3 + "." + str4 + "." + str1);
    }
    if (str2 == null)
      return paramShort2;
    for (int i = 0; i < table_componentFontNameIDs.length; ++i)
    {
      str4 = getString(table_componentFontNameIDs[i]);
      if (str2.equalsIgnoreCase(str4))
      {
        paramShort2 = (short)i;
        break;
      }
    }
    return paramShort2;
  }

  public static boolean hasMonoToPropMap()
  {
    return ((table_proportionals != null) && (table_proportionals.length != 0));
  }

  private short remapProportional(int paramInt, short paramShort)
  {
    if ((this.preferPropFonts) && (table_proportionals.length != 0) && (paramInt != 2) && (paramInt != 4))
      for (int i = 0; i < table_proportionals.length; i += 2)
        if (table_proportionals[i] == paramShort)
          return table_proportionals[(i + 1)];
    return paramShort;
  }

  public static boolean isLogicalFontFamilyName(String paramString)
  {
    return isLogicalFontFamilyNameLC(paramString.toLowerCase(Locale.ENGLISH));
  }

  public static boolean isLogicalFontFamilyNameLC(String paramString)
  {
    for (int i = 0; i < fontNames.length; ++i)
      if (paramString.equals(fontNames[i]))
        return true;
    return false;
  }

  private static boolean isLogicalFontStyleName(String paramString)
  {
    for (int i = 0; i < styleNames.length; ++i)
      if (paramString.equals(styleNames[i]))
        return true;
    return false;
  }

  public static boolean isLogicalFontFaceName(String paramString)
  {
    return isLogicalFontFaceNameLC(paramString.toLowerCase(Locale.ENGLISH));
  }

  public static boolean isLogicalFontFaceNameLC(String paramString)
  {
    int i = paramString.indexOf(46);
    if (i >= 0)
    {
      String str1 = paramString.substring(0, i);
      String str2 = paramString.substring(i + 1);
      return ((isLogicalFontFamilyName(str1)) && (isLogicalFontStyleName(str2)));
    }
    return isLogicalFontFamilyName(paramString);
  }

  protected static int getFontIndex(String paramString)
  {
    return getArrayIndex(fontNames, paramString);
  }

  protected static int getStyleIndex(String paramString)
  {
    return getArrayIndex(styleNames, paramString);
  }

  private static int getArrayIndex(String[] paramArrayOfString, String paramString)
  {
    for (int i = 0; i < paramArrayOfString.length; ++i)
      if (paramString.equals(paramArrayOfString[i]))
        return i;
    if (!($assertionsDisabled))
      throw new AssertionError();
    return 0;
  }

  protected static int getStyleIndex(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return 0;
    case 1:
      return 1;
    case 2:
      return 2;
    case 3:
      return 3;
    }
    return 0;
  }

  protected static String getFontName(int paramInt)
  {
    return fontNames[paramInt];
  }

  protected static String getStyleName(int paramInt)
  {
    return styleNames[paramInt];
  }

  public static String getLogicalFontFaceName(String paramString, int paramInt)
  {
    if ((!($assertionsDisabled)) && (!(isLogicalFontFamilyName(paramString))))
      throw new AssertionError();
    return paramString.toLowerCase(Locale.ENGLISH) + "." + getStyleString(paramInt);
  }

  public static String getStyleString(int paramInt)
  {
    return getStyleName(getStyleIndex(paramInt));
  }

  public abstract String getFallbackFamilyName(String paramString1, String paramString2);

  protected String getCompatibilityFamilyName(String paramString)
  {
    paramString = paramString.toLowerCase(Locale.ENGLISH);
    if (paramString.equals("timesroman"))
      return "serif";
    if (paramString.equals("helvetica"))
      return "sansserif";
    if (paramString.equals("courier"))
      return "monospaced";
    return null;
  }

  protected String mapFileName(String paramString)
  {
    return paramString;
  }

  protected abstract void initReorderMap();

  private void shuffle(String[] paramArrayOfString, int paramInt1, int paramInt2)
  {
    if (paramInt2 >= paramInt1)
      return;
    String str = paramArrayOfString[paramInt1];
    for (int i = paramInt1; i > paramInt2; --i)
      paramArrayOfString[i] = paramArrayOfString[(i - 1)];
    paramArrayOfString[paramInt2] = str;
  }

  public static boolean willReorderForStartupLocale()
  {
    return (getReorderSequence() != null);
  }

  private static Object getReorderSequence()
  {
    if (fontConfig.reorderMap == null)
      fontConfig.initReorderMap();
    HashMap localHashMap = fontConfig.reorderMap;
    String str1 = startupLocale.getLanguage();
    String str2 = startupLocale.getCountry();
    Object localObject = localHashMap.get(encoding + "." + str1 + "." + str2);
    if (localObject == null)
      localObject = localHashMap.get(encoding + "." + str1);
    if (localObject == null)
      localObject = localHashMap.get(encoding);
    return localObject;
  }

  private void reorderSequenceForLocale(String[] paramArrayOfString)
  {
    Object localObject = getReorderSequence();
    if (localObject instanceof String)
    {
      for (int i = 0; i < paramArrayOfString.length; ++i)
        if (paramArrayOfString[i].equals(localObject))
        {
          shuffle(paramArrayOfString, i, 0);
          return;
        }
    }
    else if (localObject instanceof String[])
    {
      String[] arrayOfString = (String[])(String[])localObject;
      for (int j = 0; j < arrayOfString.length; ++j)
        for (int k = 0; k < paramArrayOfString.length; ++k)
          if (paramArrayOfString[k].equals(arrayOfString[j]))
            shuffle(paramArrayOfString, k, j);
    }
  }

  private static Vector splitSequence(String paramString)
  {
    Vector localVector = new Vector();
    for (int i = 0; (j = paramString.indexOf(44, i)) >= 0; i = j + 1)
    {
      int j;
      localVector.add(paramString.substring(i, j));
    }
    if (paramString.length() > i)
      localVector.add(paramString.substring(i, paramString.length()));
    return localVector;
  }

  protected String[] split(String paramString)
  {
    Vector localVector = splitSequence(paramString);
    return ((String[])(String[])localVector.toArray(new String[0]));
  }

  public FontDescriptor[] getFontDescriptors(String paramString, int paramInt)
  {
    if ((!($assertionsDisabled)) && (!(isLogicalFontFamilyName(paramString))))
      throw new AssertionError();
    paramString = paramString.toLowerCase(Locale.ENGLISH);
    int i = getFontIndex(paramString);
    int j = getStyleIndex(paramInt);
    return getFontDescriptors(i, j);
  }

  private FontDescriptor[] getFontDescriptors(int paramInt1, int paramInt2)
  {
    FontDescriptor[] arrayOfFontDescriptor = this.fontDescriptors[paramInt1][paramInt2];
    if (arrayOfFontDescriptor == null)
    {
      arrayOfFontDescriptor = buildFontDescriptors(paramInt1, paramInt2);
      this.fontDescriptors[paramInt1][paramInt2] = arrayOfFontDescriptor;
    }
    return arrayOfFontDescriptor;
  }

  private FontDescriptor[] buildFontDescriptors(int paramInt1, int paramInt2)
  {
    String str1 = fontNames[paramInt1];
    String str2 = styleNames[paramInt2];
    short[] arrayOfShort1 = getCoreScripts(paramInt1);
    short[] arrayOfShort2 = this.compFontNameIDs[paramInt1][paramInt2];
    String[] arrayOfString1 = new String[arrayOfShort1.length];
    String[] arrayOfString2 = new String[arrayOfShort1.length];
    for (int i = 0; i < arrayOfString1.length; ++i)
    {
      arrayOfString2[i] = getComponentFontName(arrayOfShort2[i]);
      arrayOfString1[i] = getScriptName(arrayOfShort1[i]);
      if ((this.alphabeticSuffix != null) && ("alphabetic".equals(arrayOfString1[i])))
        arrayOfString1[i] = arrayOfString1[i] + "/" + this.alphabeticSuffix;
    }
    int[][] arrayOfInt = this.compExclusions[paramInt1];
    FontDescriptor[] arrayOfFontDescriptor = new FontDescriptor[arrayOfString2.length];
    for (int j = 0; j < arrayOfString2.length; ++j)
    {
      String str3 = makeAWTFontName(arrayOfString2[j], arrayOfString1[j]);
      String str4 = getEncoding(arrayOfString2[j], arrayOfString1[j]);
      if (str4 == null)
        str4 = "default";
      CharsetEncoder localCharsetEncoder = getFontCharsetEncoder(str4.trim(), str3);
      int[] arrayOfInt1 = arrayOfInt[j];
      arrayOfFontDescriptor[j] = new FontDescriptor(str3, localCharsetEncoder, arrayOfInt1);
    }
    return arrayOfFontDescriptor;
  }

  protected String makeAWTFontName(String paramString1, String paramString2)
  {
    return paramString1;
  }

  protected abstract String getEncoding(String paramString1, String paramString2);

  private CharsetEncoder getFontCharsetEncoder(String paramString1, String paramString2)
  {
    Charset localCharset = null;
    if (paramString1.equals("default"))
      localCharset = (Charset)this.charsetRegistry.get(paramString2);
    else
      localCharset = (Charset)this.charsetRegistry.get(paramString1);
    if (localCharset != null)
      return localCharset.newEncoder();
    if ((!(paramString1.startsWith("sun.awt."))) && (!(paramString1.equals("default"))))
    {
      localCharset = Charset.forName(paramString1);
    }
    else if (!(paramString1.equals("default")))
    {
      Class localClass = (Class)AccessController.doPrivileged(new PrivilegedAction(this, paramString1)
      {
        public Object run()
        {
          try
          {
            return Class.forName(this.val$charsetName, true, Thread.currentThread().getContextClassLoader());
          }
          catch (ClassNotFoundException localClassNotFoundException)
          {
          }
          return null;
        }
      });
      if (localClass != null)
        try
        {
          localCharset = (Charset)localClass.newInstance();
        }
        catch (Exception localException)
        {
        }
    }
    if (localCharset == null)
      localCharset = getDefaultFontCharset(paramString2);
    if (paramString1.equals("default"))
      this.charsetRegistry.put(paramString2, localCharset);
    else
      this.charsetRegistry.put(paramString1, localCharset);
    return localCharset.newEncoder();
  }

  protected abstract Charset getDefaultFontCharset(String paramString);

  public HashSet<String> getAWTFontPathSet()
  {
    return null;
  }

  public CompositeFontDescriptor[] get2DCompositeFontInfo()
  {
    CompositeFontDescriptor[] arrayOfCompositeFontDescriptor = new CompositeFontDescriptor[20];
    String str1 = this.environment.getDefaultFontFile();
    String str2 = this.environment.getDefaultFontFaceName();
    for (int i = 0; i < 5; ++i)
    {
      int i4;
      String str3 = publicFontNames[i];
      int[][] arrayOfInt = this.compExclusions[i];
      int j = 0;
      for (int k = 0; k < arrayOfInt.length; ++k)
        j += arrayOfInt[k].length;
      int[] arrayOfInt1 = new int[j];
      int[] arrayOfInt2 = new int[arrayOfInt.length];
      int l = 0;
      int i1 = 0;
      for (int i2 = 0; i2 < arrayOfInt.length; ++i2)
      {
        int[] arrayOfInt3 = arrayOfInt[i2];
        i4 = 0;
        while (i4 < arrayOfInt3.length)
        {
          int i5 = arrayOfInt3[i4];
          arrayOfInt1[(l++)] = arrayOfInt3[(i4++)];
          arrayOfInt1[(l++)] = arrayOfInt3[(i4++)];
        }
        arrayOfInt2[i2] = l;
      }
      for (i2 = 0; i2 < 4; ++i2)
      {
        short s1;
        int i6;
        String[] arrayOfString2;
        String[] arrayOfString4;
        int i3 = this.compFontNameIDs[i][i2].length;
        i4 = 0;
        if (installedFallbackFontFiles != null)
          i3 += installedFallbackFontFiles.length;
        String str4 = str3 + "." + styleNames[i2];
        Object localObject1 = new String[i3];
        Object localObject2 = new String[i3];
        for (String[] arrayOfString1 = 0; arrayOfString1 < this.compFontNameIDs[i][i2].length; ++arrayOfString1)
        {
          s1 = this.compFontNameIDs[i][i2][arrayOfString1];
          short s2 = getComponentFileID(s1);
          localObject1[arrayOfString1] = getFaceNameFromComponentFontName(getComponentFontName(s1));
          localObject2[arrayOfString1] = mapFileName(getComponentFileName(s2));
          if ((localObject2[arrayOfString1] == null) || (needToSearchForFile(localObject2[arrayOfString1])))
            localObject2[arrayOfString1] = getFileNameFromComponentFontName(getComponentFontName(s1));
          if ((i4 == 0) && (str1.equals(localObject2[arrayOfString1])))
            i4 = 1;
        }
        if (i4 == 0)
        {
          s1 = 0;
          if (installedFallbackFontFiles != null)
            i6 = installedFallbackFontFiles.length;
          if (arrayOfString1 + i6 == i3)
          {
            arrayOfString2 = new String[i3 + 1];
            System.arraycopy(localObject1, 0, arrayOfString2, 0, arrayOfString1);
            localObject1 = arrayOfString2;
            arrayOfString4 = new String[i3 + 1];
            System.arraycopy(localObject2, 0, arrayOfString4, 0, arrayOfString1);
            localObject2 = arrayOfString4;
          }
          localObject1[arrayOfString1] = str2;
          localObject2[arrayOfString1] = str1;
          ++arrayOfString1;
        }
        if (installedFallbackFontFiles != null)
          for (i6 = 0; i6 < installedFallbackFontFiles.length; ++i6)
          {
            localObject1[arrayOfString1] = null;
            localObject2[arrayOfString1] = installedFallbackFontFiles[i6];
            ++arrayOfString1;
          }
        if (arrayOfString1 < i3)
        {
          localObject3 = new String[arrayOfString1];
          System.arraycopy(localObject1, 0, localObject3, 0, arrayOfString1);
          localObject1 = localObject3;
          arrayOfString2 = new String[arrayOfString1];
          System.arraycopy(localObject2, 0, arrayOfString2, 0, arrayOfString1);
          localObject2 = arrayOfString2;
        }
        Object localObject3 = arrayOfInt2;
        if (arrayOfString1 != localObject3.length)
        {
          String[] arrayOfString3 = arrayOfInt2.length;
          localObject3 = new int[arrayOfString1];
          System.arraycopy(arrayOfInt2, 0, localObject3, 0, arrayOfString3);
          for (arrayOfString4 = arrayOfString3; arrayOfString4 < arrayOfString1; ++arrayOfString4)
            localObject3[arrayOfString4] = arrayOfInt1.length;
        }
        arrayOfCompositeFontDescriptor[(i * 4 + i2)] = new CompositeFontDescriptor(str4, this.compCoreNum[i], localObject1, localObject2, arrayOfInt1, localObject3);
      }
    }
    return ((CompositeFontDescriptor)(CompositeFontDescriptor)(CompositeFontDescriptor)arrayOfCompositeFontDescriptor);
  }

  protected abstract String getFaceNameFromComponentFontName(String paramString);

  protected abstract String getFileNameFromComponentFontName(String paramString);

  public boolean needToSearchForFile(String paramString)
  {
    if (!(SunGraphicsEnvironment.isLinux))
      return false;
    if (this.existsMap == null)
      this.existsMap = new HashMap();
    Boolean localBoolean = (Boolean)this.existsMap.get(paramString);
    if (localBoolean == null)
    {
      getNumberCoreFonts();
      if (!(this.coreFontFileNames.contains(paramString)))
      {
        localBoolean = Boolean.TRUE;
      }
      else
      {
        localBoolean = Boolean.valueOf(new File(paramString).exists());
        this.existsMap.put(paramString, localBoolean);
        if ((SunGraphicsEnvironment.debugFonts) && (localBoolean == Boolean.FALSE))
          logger.warning("Couldn't locate font file " + paramString);
      }
    }
    return (localBoolean == Boolean.FALSE);
  }

  public int getNumberCoreFonts()
  {
    if (this.numCoreFonts == -1)
    {
      short s1;
      this.numCoreFonts = this.coreFontNameIDs.size();
      Short[] arrayOfShort1 = new Short[0];
      Short[] arrayOfShort2 = (Short[])this.coreFontNameIDs.toArray(arrayOfShort1);
      Short[] arrayOfShort3 = (Short[])this.fallbackFontNameIDs.toArray(arrayOfShort1);
      int i = 0;
      for (int j = 0; j < arrayOfShort3.length; ++j)
        if (this.coreFontNameIDs.contains(arrayOfShort3[j]))
          arrayOfShort3[j] = null;
        else
          ++i;
      this.componentFonts = new String[this.numCoreFonts + i];
      Object localObject = null;
      for (j = 0; j < arrayOfShort2.length; ++j)
      {
        k = arrayOfShort2[j].shortValue();
        s1 = getComponentFileID(k);
        this.componentFonts[j] = getComponentFontName(k);
        String str = getComponentFileName(s1);
        if (str != null)
          this.coreFontFileNames.add(str);
        this.filenamesMap.put(this.componentFonts[j], mapFileName(str));
      }
      for (int k = 0; k < arrayOfShort3.length; ++k)
        if (arrayOfShort3[k] != null)
        {
          s1 = arrayOfShort3[k].shortValue();
          short s2 = getComponentFileID(s1);
          this.componentFonts[j] = getComponentFontName(s1);
          this.filenamesMap.put(this.componentFonts[j], mapFileName(getComponentFileName(s2)));
          ++j;
        }
    }
    return this.numCoreFonts;
  }

  public String[] getPlatformFontNames()
  {
    if (this.numCoreFonts == -1)
      getNumberCoreFonts();
    return this.componentFonts;
  }

  public String getFileNameFromPlatformName(String paramString)
  {
    return ((String)this.filenamesMap.get(paramString));
  }

  public String getExtraFontPath()
  {
    return getString(head[16]);
  }

  public String getVersion()
  {
    return getString(head[17]);
  }

  protected static FontConfiguration getFontConfiguration()
  {
    return fontConfig;
  }

  private static void sanityCheck()
  {
    int i = 0;
    String str1 = (String)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return System.getProperty("os.name");
      }
    });
    for (int j = 1; j < table_filenames.length; ++j)
      if (table_filenames[j] == -1)
      {
        System.out.println("\n Warning: <filename." + getString(table_componentFontNameIDs[j]) + "> entry is missing!!!");
        if (!(str1.contains("Linux")))
          ++i;
      }
    for (j = 0; j < table_scriptIDs.length; ++j)
    {
      int k = table_scriptFonts[j];
      if (k == 0)
      {
        System.out.println("\n Error: <allfonts." + getString(table_scriptIDs[j]) + "> entry is missing!!!");
        ++i;
      }
      else if (k < 0)
      {
        k = (short)(-k);
        for (int l = 0; l < 5; ++l)
          for (int i1 = 0; i1 < 4; ++i1)
          {
            int i2 = l * 4 + i1;
            int i3 = table_scriptFonts[(k + i2)];
            if (i3 == 0)
            {
              System.out.println("\n Error: <" + getFontName(l) + "." + getStyleName(i1) + "." + getString(table_scriptIDs[j]) + "> entry is missing!!!");
              ++i;
            }
          }
      }
    }
    if ("SunOS".equals(str1))
      label399: for (j = 0; j < table_awtfontpaths.length; ++j)
        if (table_awtfontpaths[j] == 0)
        {
          String str2 = getString(table_scriptIDs[j]);
          if ((!(str2.contains("lucida"))) && (!(str2.contains("dingbats"))))
          {
            if (str2.contains("symbol"))
              break label399:
            System.out.println("\nError: <awtfontpath." + str2 + "> entry is missing!!!");
            ++i;
          }
        }
    if (i != 0)
    {
      System.out.println("!!THERE ARE " + i + " ERROR(S) IN " + "THE FONTCONFIG FILE, PLEASE CHECK ITS CONTENT!!\n");
      System.exit(1);
    }
  }

  private static void dump()
  {
    int j;
    System.out.println("\n----Head Table------------");
    for (int i = 0; i < 20; ++i)
      System.out.println("  " + i + " : " + head[i]);
    System.out.println("\n----scriptIDs-------------");
    printTable(table_scriptIDs, 0);
    System.out.println("\n----scriptFonts----------------");
    for (i = 0; i < table_scriptIDs.length; ++i)
    {
      j = table_scriptFonts[i];
      if (j >= 0)
        System.out.println("  allfonts." + getString(table_scriptIDs[i]) + "=" + getString(table_componentFontNameIDs[j]));
    }
    for (i = 0; i < table_scriptIDs.length; ++i)
    {
      j = table_scriptFonts[i];
      if (j < 0)
      {
        j = (short)(-j);
        for (k = 0; k < 5; ++k)
          for (int l = 0; l < 4; ++l)
          {
            int i1 = k * 4 + l;
            int i2 = table_scriptFonts[(j + i1)];
            System.out.println("  " + getFontName(k) + "." + getStyleName(l) + "." + getString(table_scriptIDs[i]) + "=" + getString(table_componentFontNameIDs[i2]));
          }
      }
    }
    System.out.println("\n----elcIDs----------------");
    printTable(table_elcIDs, 0);
    System.out.println("\n----sequences-------------");
    for (i = 0; i < table_elcIDs.length; ++i)
    {
      System.out.println("  " + i + "/" + getString(table_elcIDs[i]));
      arrayOfShort = getShortArray(table_sequences[(i * 5 + 0)]);
      for (k = 0; k < arrayOfShort.length; ++k)
        System.out.println("     " + getString(table_scriptIDs[arrayOfShort[k]]));
    }
    System.out.println("\n----fontfileNameIDs-------");
    printTable(table_fontfileNameIDs, 0);
    System.out.println("\n----componentFontNameIDs--");
    printTable(table_componentFontNameIDs, 1);
    System.out.println("\n----filenames-------------");
    for (i = 0; i < table_filenames.length; ++i)
      if (table_filenames[i] == -1)
        System.out.println("  " + i + " : null");
      else
        System.out.println("  " + i + " : " + getString(table_fontfileNameIDs[table_filenames[i]]));
    System.out.println("\n----awtfontpaths---------");
    for (i = 0; i < table_awtfontpaths.length; ++i)
      System.out.println("  " + getString(table_scriptIDs[i]) + " : " + getString(table_awtfontpaths[i]));
    System.out.println("\n----proportionals--------");
    for (i = 0; i < table_proportionals.length; ++i)
      System.out.println("  " + getString(table_componentFontNameIDs[table_proportionals[(i++)]]) + " -> " + getString(table_componentFontNameIDs[table_proportionals[i]]));
    i = 0;
    System.out.println("\n----alphabeticSuffix----");
    while (i < table_alphabeticSuffix.length)
      System.out.println("    " + getString(table_elcIDs[table_alphabeticSuffix[(i++)]]) + " -> " + getString(table_alphabeticSuffix[(i++)]));
    System.out.println("\n----String Table---------");
    System.out.println("    stringID:    Num =" + table_stringIDs.length);
    System.out.println("    stringTable: Size=" + (table_stringTable.length * 2));
    System.out.println("\n----fallbackScriptIDs---");
    short[] arrayOfShort = getShortArray(head[15]);
    for (int k = 0; k < arrayOfShort.length; ++k)
      System.out.println("  " + getString(table_scriptIDs[arrayOfShort[k]]));
    System.out.println("\n----appendedfontpath-----");
    System.out.println("  " + getString(head[16]));
    System.out.println("\n----Version--------------");
    System.out.println("  " + getString(head[17]));
  }

  protected static short getComponentFontID(short paramShort, int paramInt1, int paramInt2)
  {
    int i = table_scriptFonts[paramShort];
    if (i >= 0)
      return i;
    return table_scriptFonts[(-i + paramInt1 * 4 + paramInt2)];
  }

  protected static short getComponentFontIDMotif(short paramShort, int paramInt1, int paramInt2)
  {
    if (table_scriptFontsMotif.length == 0)
      return 0;
    int i = table_scriptFontsMotif[paramShort];
    if (i >= 0)
      return i;
    return table_scriptFontsMotif[(-i + paramInt1 * 4 + paramInt2)];
  }

  private static int[] getExclusionRanges(short paramShort)
  {
    short s = table_exclusions[paramShort];
    if (s == 0)
      return EMPTY_INT_ARRAY;
    char[] arrayOfChar = getString(s).toCharArray();
    int[] arrayOfInt = new int[arrayOfChar.length / 2];
    int i = 0;
    for (int j = 0; j < arrayOfInt.length; ++j)
      arrayOfInt[j] = ((arrayOfChar[(i++)] << '\16') + (arrayOfChar[(i++)] & 0xFFFF));
    return arrayOfInt;
  }

  private static boolean contains(short[] paramArrayOfShort, short paramShort, int paramInt)
  {
    for (int i = 0; i < paramInt; ++i)
      if (paramArrayOfShort[i] == paramShort)
        return true;
    return false;
  }

  protected static String getComponentFontName(short paramShort)
  {
    if (paramShort < 0)
      return null;
    return getString(table_componentFontNameIDs[paramShort]);
  }

  private static String getComponentFileName(short paramShort)
  {
    if (paramShort < 0)
      return null;
    return getString(table_fontfileNameIDs[paramShort]);
  }

  private static short getComponentFileID(short paramShort)
  {
    return table_filenames[paramShort];
  }

  private static String getScriptName(short paramShort)
  {
    return getString(table_scriptIDs[paramShort]);
  }

  protected short[] getCoreScripts(int paramInt)
  {
    int i = getInitELC();
    short[] arrayOfShort = getShortArray(table_sequences[(i * 5 + paramInt)]);
    if (this.preferLocaleFonts)
    {
      if (this.reorderScripts == null)
        this.reorderScripts = new HashMap();
      String[] arrayOfString = new String[arrayOfShort.length];
      for (int j = 0; j < arrayOfString.length; ++j)
      {
        arrayOfString[j] = getScriptName(arrayOfShort[j]);
        this.reorderScripts.put(arrayOfString[j], Short.valueOf(arrayOfShort[j]));
      }
      reorderSequenceForLocale(arrayOfString);
      for (j = 0; j < arrayOfString.length; ++j)
        arrayOfShort[j] = ((Short)this.reorderScripts.get(arrayOfString[j])).shortValue();
    }
    return arrayOfShort;
  }

  private static short[] getFallbackScripts()
  {
    return getShortArray(head[15]);
  }

  private static void printTable(short[] paramArrayOfShort, int paramInt)
  {
    for (int i = paramInt; i < paramArrayOfShort.length; ++i)
      System.out.println("  " + i + " : " + getString(paramArrayOfShort[i]));
  }

  private static short[] readShortTable(DataInputStream paramDataInputStream, int paramInt)
    throws IOException
  {
    if (paramInt == 0)
      return EMPTY_SHORT_ARRAY;
    short[] arrayOfShort = new short[paramInt];
    byte[] arrayOfByte = new byte[paramInt * 2];
    paramDataInputStream.read(arrayOfByte);
    int i = 0;
    int j = 0;
    while (i < paramInt)
      arrayOfShort[(i++)] = (short)(arrayOfByte[(j++)] << 8 | arrayOfByte[(j++)] & 0xFF);
    return arrayOfShort;
  }

  private static void writeShortTable(DataOutputStream paramDataOutputStream, short[] paramArrayOfShort)
    throws IOException
  {
    for (int i = 0; i < paramArrayOfShort.length; ++i)
      paramDataOutputStream.writeShort(paramArrayOfShort[i]);
  }

  private static short[] toList(HashMap paramHashMap)
  {
    short[] arrayOfShort = new short[paramHashMap.size()];
    for (int i = 0; i < arrayOfShort.length; ++i)
      arrayOfShort[i] = -1;
    Iterator localIterator = paramHashMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      arrayOfShort[((Short)localEntry.getValue()).shortValue()] = getStringID((String)localEntry.getKey());
    }
    return arrayOfShort;
  }

  protected static String getString(short paramShort)
  {
    if (paramShort == 0)
      return null;
    if (stringCache[paramShort] == null)
      stringCache[paramShort] = new String(table_stringTable, table_stringIDs[paramShort], table_stringIDs[(paramShort + 1)] - table_stringIDs[paramShort]);
    return stringCache[paramShort];
  }

  private static short[] getShortArray(short paramShort)
  {
    String str = getString(paramShort);
    char[] arrayOfChar = str.toCharArray();
    short[] arrayOfShort = new short[arrayOfChar.length];
    for (int i = 0; i < arrayOfChar.length; ++i)
      arrayOfShort[i] = (short)(arrayOfChar[i] & 0xFFFF);
    return arrayOfShort;
  }

  private static short getStringID(String paramString)
  {
    if (paramString == null)
      return 0;
    int i = (short)stringTable.length();
    stringTable.append(paramString);
    int j = (short)stringTable.length();
    stringIDs[stringIDNum] = i;
    stringIDs[(stringIDNum + 1)] = j;
    stringIDNum = (short)(stringIDNum + 1);
    if (stringIDNum + 1 >= stringIDs.length)
    {
      short[] arrayOfShort = new short[stringIDNum + 1000];
      System.arraycopy(stringIDs, 0, arrayOfShort, 0, stringIDNum);
      stringIDs = arrayOfShort;
    }
    return (short)(stringIDNum - 1);
  }

  private static short getShortArrayID(short[] paramArrayOfShort)
  {
    char[] arrayOfChar = new char[paramArrayOfShort.length];
    for (int i = 0; i < paramArrayOfShort.length; ++i)
      arrayOfChar[i] = (char)paramArrayOfShort[i];
    String str = new String(arrayOfChar);
    return getStringID(str);
  }

  static
  {
    startupLocale = null;
    localeMap = null;
    isProperties = true;
    fontNames = { "serif", "sansserif", "monospaced", "dialog", "dialoginput" };
    publicFontNames = { "Serif", "SansSerif", "Monospaced", "Dialog", "DialogInput" };
    styleNames = { "plain", "bold", "italic", "bolditalic" };
    installedFallbackFontFiles = null;
    EMPTY_INT_ARRAY = new int[0];
    EMPTY_STRING_ARRAY = new String[0];
    EMPTY_SHORT_ARRAY = new short[0];
  }

  static class PropertiesHandler
  {
    private HashMap<String, Short> scriptIDs;
    private HashMap<String, Short> elcIDs;
    private HashMap<String, Short> componentFontNameIDs;
    private HashMap<String, Short> fontfileNameIDs;
    private HashMap<String, Integer> logicalFontIDs;
    private HashMap<String, Integer> fontStyleIDs;
    private HashMap<Short, Short> filenames;
    private HashMap<Short, short[]> sequences;
    private HashMap<Short, Short[]> scriptFonts;
    private HashMap<Short, Short> scriptAllfonts;
    private HashMap<Short, int[]> exclusions;
    private HashMap<Short, Short> awtfontpaths;
    private HashMap<Short, Short> proportionals;
    private HashMap<Short, Short> scriptAllfontsMotif;
    private HashMap<Short, Short[]> scriptFontsMotif;
    private HashMap<Short, Short> alphabeticSuffix;
    private short[] fallbackScriptIDs;
    private String version;
    private String appendedfontpath;

    public void load(InputStream paramInputStream)
      throws IOException
    {
      initLogicalNameStyle();
      initHashMaps();
      FontProperties localFontProperties = new FontProperties(this);
      localFontProperties.load(paramInputStream);
      initBinaryTable();
    }

    private void initBinaryTable()
    {
      Map.Entry localEntry2;
      Map.Entry localEntry3;
      Object localObject1;
      Object localObject2;
      int i3;
      FontConfiguration.access$002(new short[20]);
      FontConfiguration.access$000()[0] = 20;
      FontConfiguration.access$102(FontConfiguration.access$200(this.scriptIDs));
      FontConfiguration.access$000()[1] = (short)(FontConfiguration.access$000()[0] + FontConfiguration.access$100().length);
      int i = FontConfiguration.access$100().length + this.scriptFonts.size() * 20;
      FontConfiguration.access$302(new short[i]);
      Iterator localIterator = this.scriptAllfonts.entrySet().iterator();
      while (localIterator.hasNext())
      {
        Map.Entry localEntry1 = (Map.Entry)localIterator.next();
        FontConfiguration.access$300()[((Short)localEntry1.getKey()).intValue()] = ((Short)localEntry1.getValue()).shortValue();
      }
      int j = FontConfiguration.access$100().length;
      localIterator = this.scriptFonts.entrySet().iterator();
      while (localIterator.hasNext())
      {
        localEntry2 = (Map.Entry)localIterator.next();
        FontConfiguration.access$300()[((Short)localEntry2.getKey()).intValue()] = (short)(-j);
        Short[] arrayOfShort = (Short[])localEntry2.getValue();
        for (int i2 = 0; i2 < 20; ++i2)
          if (arrayOfShort[i2] != null)
            FontConfiguration.access$300()[(j++)] = arrayOfShort[i2].shortValue();
          else
            FontConfiguration.access$300()[(j++)] = 0;
      }
      FontConfiguration.access$000()[2] = (short)(FontConfiguration.access$000()[1] + FontConfiguration.access$300().length);
      FontConfiguration.access$402(FontConfiguration.access$200(this.elcIDs));
      FontConfiguration.access$000()[3] = (short)(FontConfiguration.access$000()[2] + FontConfiguration.access$400().length);
      FontConfiguration.access$502(new short[this.elcIDs.size() * 5]);
      localIterator = this.sequences.entrySet().iterator();
      while (localIterator.hasNext())
      {
        localEntry2 = (Map.Entry)localIterator.next();
        int i1 = ((Short)localEntry2.getKey()).intValue();
        localObject2 = (short[])localEntry2.getValue();
        if (localObject2.length == 1)
          for (i3 = 0; i3 < 5; ++i3)
            FontConfiguration.access$500()[(i1 * 5 + i3)] = localObject2[0];
        else
          for (i3 = 0; i3 < 5; ++i3)
            FontConfiguration.access$500()[(i1 * 5 + i3)] = localObject2[i3];
      }
      FontConfiguration.access$000()[4] = (short)(FontConfiguration.access$000()[3] + FontConfiguration.access$500().length);
      FontConfiguration.access$602(FontConfiguration.access$200(this.fontfileNameIDs));
      FontConfiguration.access$000()[5] = (short)(FontConfiguration.access$000()[4] + FontConfiguration.access$600().length);
      FontConfiguration.access$702(FontConfiguration.access$200(this.componentFontNameIDs));
      FontConfiguration.access$000()[6] = (short)(FontConfiguration.access$000()[5] + FontConfiguration.access$700().length);
      FontConfiguration.access$802(new short[FontConfiguration.access$700().length]);
      for (int k = 0; k < FontConfiguration.access$800().length; ++k)
        FontConfiguration.access$800()[k] = -1;
      localIterator = this.filenames.entrySet().iterator();
      while (localIterator.hasNext())
      {
        localEntry3 = (Map.Entry)localIterator.next();
        FontConfiguration.access$800()[((Short)localEntry3.getKey()).shortValue()] = ((Short)localEntry3.getValue()).shortValue();
      }
      FontConfiguration.access$000()[7] = (short)(FontConfiguration.access$000()[6] + FontConfiguration.access$800().length);
      FontConfiguration.table_awtfontpaths = new short[FontConfiguration.access$100().length];
      localIterator = this.awtfontpaths.entrySet().iterator();
      while (localIterator.hasNext())
      {
        localEntry3 = (Map.Entry)localIterator.next();
        FontConfiguration.table_awtfontpaths[((Short)localEntry3.getKey()).shortValue()] = ((Short)localEntry3.getValue()).shortValue();
      }
      FontConfiguration.access$000()[8] = (short)(FontConfiguration.access$000()[7] + FontConfiguration.table_awtfontpaths.length);
      FontConfiguration.access$902(new short[this.scriptIDs.size()]);
      localIterator = this.exclusions.entrySet().iterator();
      while (localIterator.hasNext())
      {
        localEntry3 = (Map.Entry)localIterator.next();
        localObject1 = (int[])localEntry3.getValue();
        localObject2 = new char[localObject1.length * 2];
        i3 = 0;
        for (int i4 = 0; i4 < localObject1.length; ++i4)
        {
          localObject2[(i3++)] = (char)(localObject1[i4] >> 16);
          localObject2[(i3++)] = (char)(localObject1[i4] & 0xFFFF);
        }
        FontConfiguration.access$900()[((Short)localEntry3.getKey()).shortValue()] = FontConfiguration.access$1000(new String(localObject2));
      }
      FontConfiguration.access$000()[9] = (short)(FontConfiguration.access$000()[8] + FontConfiguration.access$900().length);
      FontConfiguration.access$1102(new short[this.proportionals.size() * 2]);
      localIterator = this.proportionals.entrySet().iterator();
      int l = 0;
      while (localIterator.hasNext())
      {
        localObject1 = (Map.Entry)localIterator.next();
        FontConfiguration.access$1100()[(l++)] = ((Short)((Map.Entry)localObject1).getKey()).shortValue();
        FontConfiguration.access$1100()[(l++)] = ((Short)((Map.Entry)localObject1).getValue()).shortValue();
      }
      FontConfiguration.access$000()[10] = (short)(FontConfiguration.access$000()[9] + FontConfiguration.access$1100().length);
      if ((this.scriptAllfontsMotif.size() != 0) || (this.scriptFontsMotif.size() != 0))
      {
        i = FontConfiguration.access$100().length + this.scriptFontsMotif.size() * 20;
        FontConfiguration.access$1202(new short[i]);
        localIterator = this.scriptAllfontsMotif.entrySet().iterator();
        while (localIterator.hasNext())
        {
          localObject1 = (Map.Entry)localIterator.next();
          FontConfiguration.access$1200()[((Short)localObject1.getKey()).intValue()] = ((Short)((Map.Entry)localObject1).getValue()).shortValue();
        }
        j = FontConfiguration.access$100().length;
        localIterator = this.scriptFontsMotif.entrySet().iterator();
        while (true)
        {
          if (!(localIterator.hasNext()))
            break label1256;
          localObject1 = (Map.Entry)localIterator.next();
          FontConfiguration.access$1200()[((Short)localObject1.getKey()).intValue()] = (short)(-j);
          localObject2 = (Short[])((Map.Entry)localObject1).getValue();
          for (i3 = 0; i3 < 20; ++i3)
            if (localObject2[i3] != null)
              FontConfiguration.access$1200()[(j++)] = localObject2[i3].shortValue();
            else
              FontConfiguration.access$1200()[(j++)] = 0;
        }
      }
      FontConfiguration.access$1202(FontConfiguration.access$1300());
      label1256: FontConfiguration.access$000()[11] = (short)(FontConfiguration.access$000()[10] + FontConfiguration.access$1200().length);
      FontConfiguration.access$1402(new short[this.alphabeticSuffix.size() * 2]);
      localIterator = this.alphabeticSuffix.entrySet().iterator();
      l = 0;
      while (localIterator.hasNext())
      {
        localObject1 = (Map.Entry)localIterator.next();
        FontConfiguration.access$1400()[(l++)] = ((Short)((Map.Entry)localObject1).getKey()).shortValue();
        FontConfiguration.access$1400()[(l++)] = ((Short)((Map.Entry)localObject1).getValue()).shortValue();
      }
      FontConfiguration.access$000()[15] = FontConfiguration.access$1500(this.fallbackScriptIDs);
      FontConfiguration.access$000()[16] = FontConfiguration.access$1000(this.appendedfontpath);
      FontConfiguration.access$000()[17] = FontConfiguration.access$1000(this.version);
      FontConfiguration.access$000()[12] = (short)(FontConfiguration.access$000()[11] + FontConfiguration.access$1400().length);
      FontConfiguration.access$1602(new short[FontConfiguration.access$1700() + 1]);
      System.arraycopy(FontConfiguration.access$1800(), 0, FontConfiguration.access$1600(), 0, FontConfiguration.access$1700() + 1);
      FontConfiguration.access$000()[13] = (short)(FontConfiguration.access$000()[12] + FontConfiguration.access$1700() + 1);
      FontConfiguration.access$1902(FontConfiguration.access$2000().toString().toCharArray());
      FontConfiguration.access$000()[14] = (short)(FontConfiguration.access$000()[13] + FontConfiguration.access$2000().length());
      FontConfiguration.access$2102(new String[FontConfiguration.access$1600().length]);
    }

    private void initLogicalNameStyle()
    {
      this.logicalFontIDs = new HashMap();
      this.fontStyleIDs = new HashMap();
      this.logicalFontIDs.put("serif", Integer.valueOf(0));
      this.logicalFontIDs.put("sansserif", Integer.valueOf(1));
      this.logicalFontIDs.put("monospaced", Integer.valueOf(2));
      this.logicalFontIDs.put("dialog", Integer.valueOf(3));
      this.logicalFontIDs.put("dialoginput", Integer.valueOf(4));
      this.fontStyleIDs.put("plain", Integer.valueOf(0));
      this.fontStyleIDs.put("bold", Integer.valueOf(1));
      this.fontStyleIDs.put("italic", Integer.valueOf(2));
      this.fontStyleIDs.put("bolditalic", Integer.valueOf(3));
    }

    private void initHashMaps()
    {
      this.scriptIDs = new HashMap();
      this.elcIDs = new HashMap();
      this.componentFontNameIDs = new HashMap();
      this.componentFontNameIDs.put("", new Short(0));
      this.fontfileNameIDs = new HashMap();
      this.filenames = new HashMap();
      this.sequences = new HashMap();
      this.scriptFonts = new HashMap();
      this.scriptAllfonts = new HashMap();
      this.exclusions = new HashMap();
      this.awtfontpaths = new HashMap();
      this.proportionals = new HashMap();
      this.scriptFontsMotif = new HashMap();
      this.scriptAllfontsMotif = new HashMap();
      this.alphabeticSuffix = new HashMap();
      this.fallbackScriptIDs = FontConfiguration.access$1300();
    }

    private int[] parseExclusions(String paramString1, String paramString2)
    {
      if (paramString2 == null)
        return FontConfiguration.access$2200();
      int i = 1;
      for (int j = 0; (j = paramString2.indexOf(44, j)) != -1; ++j)
        ++i;
      int[] arrayOfInt = new int[i * 2];
      j = 0;
      int k = 0;
      int l = 0;
      while (l < i * 2)
      {
        int i1 = 0;
        int i2 = 0;
        try
        {
          k = paramString2.indexOf(45, j);
          String str1 = paramString2.substring(j, k);
          j = k + 1;
          k = paramString2.indexOf(44, j);
          if (k == -1)
            k = paramString2.length();
          String str2 = paramString2.substring(j, k);
          j = k + 1;
          int i3 = str1.length();
          int i4 = str2.length();
          if (((i3 != 4) && (i3 != 6)) || ((i4 != 4) && (i4 != 6)))
            throw new Exception();
          i1 = Integer.parseInt(str1, 16);
          i2 = Integer.parseInt(str2, 16);
          if (i1 > i2)
            throw new Exception();
        }
        catch (Exception localException)
        {
          if ((SunGraphicsEnvironment.debugFonts) && (FontConfiguration.access$2300() != null))
            FontConfiguration.access$2300().config("Failed parsing " + paramString1 + " property of font configuration.");
          return FontConfiguration.access$2200();
        }
        arrayOfInt[(l++)] = i1;
        arrayOfInt[(l++)] = i2;
      }
      return arrayOfInt;
    }

    private Short getID(HashMap<String, Short> paramHashMap, String paramString)
    {
      Short localShort = (Short)paramHashMap.get(paramString);
      if (localShort == null)
      {
        paramHashMap.put(paramString, Short.valueOf((short)paramHashMap.size()));
        return ((Short)paramHashMap.get(paramString));
      }
      return localShort;
    }

    private void parseProperty(String paramString1, String paramString2)
    {
      if (paramString1.startsWith("filename."))
      {
        paramString1 = paramString1.substring(9);
        if (!("MingLiU_HKSCS".equals(paramString1)))
          paramString1 = paramString1.replace('_', ' ');
        Short localShort1 = getID(this.componentFontNameIDs, paramString1);
        Short localShort2 = getID(this.fontfileNameIDs, paramString2);
        label370: this.filenames.put(localShort1, localShort2);
      }
      else if (paramString1.startsWith("exclusion."))
      {
        paramString1 = paramString1.substring(10);
        this.exclusions.put(getID(this.scriptIDs, paramString1), parseExclusions(paramString1, paramString2));
      }
      else
      {
        int i;
        int j;
        Object localObject;
        Short localShort3;
        if (paramString1.startsWith("sequence."))
        {
          paramString1 = paramString1.substring(9);
          i = 0;
          j = 0;
          String[] arrayOfString = (String[])(String[])FontConfiguration.access$2600(paramString2).toArray(FontConfiguration.access$2500());
          localObject = new short[arrayOfString.length];
          for (int l = 0; l < arrayOfString.length; ++l)
          {
            if ("alphabetic/default".equals(arrayOfString[l]))
            {
              arrayOfString[l] = "alphabetic";
              i = 1;
            }
            else if ("alphabetic/1252".equals(arrayOfString[l]))
            {
              arrayOfString[l] = "alphabetic";
              j = 1;
            }
            localObject[l] = getID(this.scriptIDs, arrayOfString[l]).shortValue();
          }
          l = FontConfiguration.access$1500(localObject);
          localShort3 = null;
          int i1 = paramString1.indexOf(46);
          if (i1 == -1)
          {
            if ("fallback".equals(paramString1))
            {
              this.fallbackScriptIDs = ((S)localObject);
              return;
            }
            if ("allfonts".equals(paramString1))
            {
              localShort3 = getID(this.elcIDs, "NULL.NULL.NULL");
              break label370:
            }
            if (FontConfiguration.access$2300() != null)
              FontConfiguration.access$2300().config("Error sequence def: <sequence." + paramString1 + ">");
            return;
          }
          localShort3 = getID(this.elcIDs, paramString1.substring(i1 + 1));
          paramString1 = paramString1.substring(0, i1);
          short[] arrayOfShort1 = null;
          if ("allfonts".equals(paramString1))
          {
            arrayOfShort1 = new short[1];
            arrayOfShort1[0] = l;
          }
          else
          {
            arrayOfShort1 = (short[])this.sequences.get(localShort3);
            if (arrayOfShort1 == null)
              arrayOfShort1 = new short[5];
            Integer localInteger2 = (Integer)this.logicalFontIDs.get(paramString1);
            if (localInteger2 == null)
            {
              if (FontConfiguration.access$2300() != null)
                FontConfiguration.access$2300().config("Unrecognizable logicfont name " + paramString1);
              return;
            }
            arrayOfShort1[localInteger2.intValue()] = l;
          }
          this.sequences.put(localShort3, arrayOfShort1);
          if (i != 0)
            this.alphabeticSuffix.put(localShort3, Short.valueOf(FontConfiguration.access$1000("default")));
          else if (j != 0)
            this.alphabeticSuffix.put(localShort3, Short.valueOf(FontConfiguration.access$1000("1252")));
        }
        else if (paramString1.startsWith("allfonts."))
        {
          paramString1 = paramString1.substring(9);
          if (paramString1.endsWith(".motif"))
          {
            paramString1 = paramString1.substring(0, paramString1.length() - 6);
            this.scriptAllfontsMotif.put(getID(this.scriptIDs, paramString1), getID(this.componentFontNameIDs, paramString2));
          }
          else
          {
            this.scriptAllfonts.put(getID(this.scriptIDs, paramString1), getID(this.componentFontNameIDs, paramString2));
          }
        }
        else if (paramString1.startsWith("awtfontpath."))
        {
          paramString1 = paramString1.substring(12);
          this.awtfontpaths.put(getID(this.scriptIDs, paramString1), Short.valueOf(FontConfiguration.access$1000(paramString2)));
        }
        else if ("version".equals(paramString1))
        {
          this.version = paramString2;
        }
        else if ("appendedfontpath".equals(paramString1))
        {
          this.appendedfontpath = paramString2;
        }
        else if (paramString1.startsWith("proportional."))
        {
          paramString1 = paramString1.substring(13).replace('_', ' ');
          this.proportionals.put(getID(this.componentFontNameIDs, paramString1), getID(this.componentFontNameIDs, paramString2));
        }
        else
        {
          Short[] arrayOfShort;
          int k = 0;
          i = paramString1.indexOf(46);
          if (i == -1)
          {
            if (FontConfiguration.access$2300() != null)
              FontConfiguration.access$2300().config("Failed parsing " + paramString1 + " property of font configuration.");
            return;
          }
          j = paramString1.indexOf(46, i + 1);
          if (j == -1)
          {
            if (FontConfiguration.access$2300() != null)
              FontConfiguration.access$2300().config("Failed parsing " + paramString1 + " property of font configuration.");
            return;
          }
          if (paramString1.endsWith(".motif"))
          {
            paramString1 = paramString1.substring(0, paramString1.length() - 6);
            k = 1;
          }
          localObject = (Integer)this.logicalFontIDs.get(paramString1.substring(0, i));
          Integer localInteger1 = (Integer)this.fontStyleIDs.get(paramString1.substring(i + 1, j));
          localShort3 = getID(this.scriptIDs, paramString1.substring(j + 1));
          if ((localObject == null) || (localInteger1 == null))
          {
            if (FontConfiguration.access$2300() != null)
              FontConfiguration.access$2300().config("unrecognizable logicfont name/style at " + paramString1);
            return;
          }
          if (k != 0)
            arrayOfShort = (Short[])this.scriptFontsMotif.get(localShort3);
          else
            arrayOfShort = (Short[])this.scriptFonts.get(localShort3);
          if (arrayOfShort == null)
            arrayOfShort = new Short[20];
          arrayOfShort[(localObject.intValue() * 4 + localInteger1.intValue())] = getID(this.componentFontNameIDs, paramString2);
          if (k != 0)
            this.scriptFontsMotif.put(localShort3, arrayOfShort);
          else
            this.scriptFonts.put(localShort3, arrayOfShort);
        }
      }
    }

    class FontProperties extends Properties
    {
      public synchronized Object put(, Object paramObject2)
      {
        FontConfiguration.PropertiesHandler.access$2400(this.this$0, (String)paramObject1, (String)paramObject2);
        return null;
      }
    }
  }
}
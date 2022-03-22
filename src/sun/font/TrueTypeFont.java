package sun.font;

import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D.Float;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Locale;
import java.util.logging.Logger;
import sun.awt.SunToolkit;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;
import sun.security.action.GetPropertyAction;

public class TrueTypeFont extends FileFont
{
  public static final int cmapTag = 1668112752;
  public static final int glyfTag = 1735162214;
  public static final int headTag = 1751474532;
  public static final int hheaTag = 1751672161;
  public static final int hmtxTag = 1752003704;
  public static final int locaTag = 1819239265;
  public static final int maxpTag = 1835104368;
  public static final int nameTag = 1851878757;
  public static final int postTag = 1886352244;
  public static final int os_2Tag = 1330851634;
  public static final int GDEFTag = 1195656518;
  public static final int GPOSTag = 1196445523;
  public static final int GSUBTag = 1196643650;
  public static final int mortTag = 1836020340;
  public static final int fdscTag = 1717859171;
  public static final int fvarTag = 1719034226;
  public static final int featTag = 1717920116;
  public static final int EBLCTag = 1161972803;
  public static final int gaspTag = 1734439792;
  public static final int ttcfTag = 1953784678;
  public static final int v1ttTag = 65536;
  public static final int trueTag = 1953658213;
  public static final int MS_PLATFORM_ID = 3;
  public static final short ENGLISH_LOCALE_ID = 1033;
  public static final int FAMILY_NAME_ID = 1;
  public static final int FULL_NAME_ID = 4;
  public static final int POSTSCRIPT_NAME_ID = 6;
  TTDisposerRecord disposerRecord = new TTDisposerRecord(null);
  int fontIndex = 0;
  int directoryCount = 1;
  int directoryOffset;
  int numTables;
  DirectoryEntry[] tableDirectory;
  private boolean supportsJA;
  private boolean supportsCJK;
  private static final int TTCHEADERSIZE = 12;
  private static final int DIRECTORYHEADERSIZE = 12;
  private static final int DIRECTORYENTRYSIZE = 16;
  static final String[] encoding_mapping = { "cp1252", "cp1250", "cp1251", "cp1253", "cp1254", "cp1255", "cp1256", "cp1257", "", "", "", "", "", "", "", "", "ms874", "ms932", "gbk", "ms949", "ms950", "ms1361", "", "", "", "", "", "", "", "", "", "" };
  private static final String[][] languages = { { "en", "ca", "da", "de", "es", "fi", "fr", "is", "it", "nl", "no", "pt", "sq", "sv" }, { "cs", "cz", "et", "hr", "hu", "nr", "pl", "ro", "sk", "sl", "sq", "sr" }, { "bg", "mk", "ru", "sh", "uk" }, { "el" }, { "tr" }, { "he" }, { "ar" }, { "et", "lt", "lv" }, { "th" }, { "ja" }, { "zh", "zh_CN" }, { "ko" }, { "zh_HK", "zh_TW" }, { "ko" } };
  private static final String[] codePages = { "cp1252", "cp1250", "cp1251", "cp1253", "cp1254", "cp1255", "cp1256", "cp1257", "ms874", "ms932", "gbk", "ms949", "ms950", "ms1361" };
  private static String defaultCodePage = null;
  public static final int reserved_bits1 = -2147483648;
  public static final int reserved_bits2 = 65535;
  private static final int fsSelectionItalicBit = 1;
  private static final int fsSelectionBoldBit = 32;
  private static final int fsSelectionRegularBit = 64;
  private float stSize;
  private float stPos;
  private float ulSize;
  private float ulPos;
  private int[] bwGlyphs;
  private char[] gaspTable;

  TrueTypeFont(String paramString, java.lang.Object paramObject, int paramInt, boolean paramBoolean)
    throws FontFormatException
  {
    super(paramString, paramObject);
    this.useJavaRasterizer = paramBoolean;
    this.fontRank = 3;
    verify();
    init(paramInt);
    Disposer.addObjectRecord(this, this.disposerRecord);
  }

  protected boolean checkUseNatives()
  {
    java.lang.Object localObject;
    if (this.checkedNatives)
      return this.useNatives;
    if ((!(FontManager.isSolaris)) || (this.useJavaRasterizer) || (FontManager.useT2K) || (this.nativeNames == null) || (getDirectoryEntry(1161972803) != null) || (GraphicsEnvironment.isHeadless()))
    {
      this.checkedNatives = true;
      return false;
    }
    if (this.nativeNames instanceof String)
    {
      localObject = (String)this.nativeNames;
      if (((String)localObject).indexOf("8859") > 0)
      {
        this.checkedNatives = true;
        return false;
      }
      if (NativeFont.hasExternalBitmaps((String)localObject))
      {
        this.nativeFonts = new NativeFont[1];
        try
        {
          this.nativeFonts[0] = new NativeFont((String)localObject, true);
          this.useNatives = true;
        }
        catch (FontFormatException localFontFormatException1)
        {
          this.nativeFonts = null;
        }
      }
    }
    else if (this.nativeNames instanceof String[])
    {
      localObject = (String[])(String[])this.nativeNames;
      int i = localObject.length;
      int j = 0;
      for (int k = 0; k < i; ++k)
      {
        if (localObject[k].indexOf("8859") > 0)
        {
          this.checkedNatives = true;
          return false;
        }
        if (NativeFont.hasExternalBitmaps(localObject[k]))
          j = 1;
      }
      if (j == 0)
      {
        this.checkedNatives = true;
        return false;
      }
      this.useNatives = true;
      this.nativeFonts = new NativeFont[i];
      for (k = 0; k < i; ++k)
        try
        {
          this.nativeFonts[k] = new NativeFont(localObject[k], true);
        }
        catch (FontFormatException localFontFormatException2)
        {
          this.useNatives = false;
          this.nativeFonts = null;
        }
    }
    if (this.useNatives)
      this.glyphToCharMap = new char[getMapper().getNumGlyphs()];
    this.checkedNatives = true;
    return this.useNatives;
  }

  private synchronized FileChannel open()
    throws FontFormatException
  {
    if (this.disposerRecord.channel == null)
    {
      if (FontManager.logging)
        FontManager.logger.info("open TTF: " + this.platName);
      try
      {
        RandomAccessFile localRandomAccessFile = (RandomAccessFile)AccessController.doPrivileged(new PrivilegedAction(this)
        {
          public java.lang.Object run()
          {
            try
            {
              return new RandomAccessFile(this.this$0.platName, "r");
            }
            catch (FileNotFoundException localFileNotFoundException)
            {
            }
            return null;
          }
        });
        this.disposerRecord.channel = localRandomAccessFile.getChannel();
        this.fileSize = (int)this.disposerRecord.channel.size();
        FontManager.addToPool(this);
      }
      catch (NullPointerException localNullPointerException)
      {
        close();
        throw new FontFormatException(localNullPointerException.toString());
      }
      catch (ClosedChannelException localClosedChannelException)
      {
        Thread.interrupted();
        close();
        open();
      }
      catch (IOException localIOException)
      {
        close();
        throw new FontFormatException(localIOException.toString());
      }
    }
    return this.disposerRecord.channel;
  }

  protected synchronized void close()
  {
    this.disposerRecord.dispose();
  }

  int readBlock(ByteBuffer paramByteBuffer, int paramInt1, int paramInt2)
  {
    int i = 0;
    try
    {
      synchronized (this)
      {
        if (this.disposerRecord.channel == null)
          open();
        if (paramInt1 + paramInt2 <= this.fileSize)
          break label53;
        if (paramInt1 < this.fileSize)
          break label46;
        return 0;
        label46: paramInt2 = this.fileSize - paramInt1;
        label53: paramByteBuffer.clear();
        while (i != paramInt2)
        {
          this.disposerRecord.channel.position(paramInt1 + i);
          int j = this.disposerRecord.channel.read(paramByteBuffer);
          if (j == -1)
          {
            paramByteBuffer.flip();
            throw new IOException("unexpected EOF" + this);
          }
          i += j;
        }
        paramByteBuffer.flip();
      }
    }
    catch (FontFormatException localFontFormatException)
    {
      localFontFormatException.printStackTrace();
    }
    catch (ClosedChannelException localClosedChannelException)
    {
      Thread.interrupted();
      close();
      return readBlock(paramByteBuffer, paramInt1, paramInt2);
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return i;
  }

  ByteBuffer readBlock(int paramInt1, int paramInt2)
  {
    ByteBuffer localByteBuffer = ByteBuffer.allocate(paramInt2);
    try
    {
      synchronized (this)
      {
        if (this.disposerRecord.channel == null)
          open();
        if (paramInt1 + paramInt2 <= this.fileSize)
          break label58;
        if (paramInt1 <= this.fileSize)
          break label48;
        return null;
        label48: localByteBuffer = ByteBuffer.allocate(this.fileSize - paramInt1);
        label58: this.disposerRecord.channel.position(paramInt1);
        this.disposerRecord.channel.read(localByteBuffer);
        localByteBuffer.flip();
      }
    }
    catch (FontFormatException localFontFormatException)
    {
      return null;
    }
    catch (ClosedChannelException localClosedChannelException)
    {
      Thread.interrupted();
      close();
      readBlock(localByteBuffer, paramInt1, paramInt2);
    }
    catch (IOException localIOException)
    {
      return null;
    }
    return localByteBuffer;
  }

  byte[] readBytes(int paramInt1, int paramInt2)
  {
    ByteBuffer localByteBuffer = readBlock(paramInt1, paramInt2);
    if (localByteBuffer.hasArray())
      return localByteBuffer.array();
    byte[] arrayOfByte = new byte[localByteBuffer.limit()];
    localByteBuffer.get(arrayOfByte);
    return arrayOfByte;
  }

  private void verify()
    throws FontFormatException
  {
    open();
  }

  protected void init(int paramInt)
    throws FontFormatException
  {
    int i = 0;
    ByteBuffer localByteBuffer1 = readBlock(0, 12);
    try
    {
      switch (localByteBuffer1.getInt())
      {
      case 1953784678:
        localByteBuffer1.getInt();
        this.directoryCount = localByteBuffer1.getInt();
        if (paramInt >= this.directoryCount)
          throw new FontFormatException("Bad collection index");
        this.fontIndex = paramInt;
        localByteBuffer1 = readBlock(12 + 4 * paramInt, 4);
        i = localByteBuffer1.getInt();
        break;
      case 65536:
      case 1953658213:
        break;
      default:
        throw new FontFormatException("Unsupported sfnt " + this.platName);
      }
      localByteBuffer1 = readBlock(i + 4, 2);
      this.numTables = localByteBuffer1.getShort();
      this.directoryOffset = (i + 12);
      ByteBuffer localByteBuffer2 = readBlock(this.directoryOffset, this.numTables * 16);
      localObject1 = localByteBuffer2.asIntBuffer();
      this.tableDirectory = new DirectoryEntry[this.numTables];
      for (int k = 0; k < this.numTables; ++k)
      {
        DirectoryEntry localDirectoryEntry;
        this.tableDirectory[k] = (localDirectoryEntry = new DirectoryEntry(this));
        localDirectoryEntry.tag = ((IntBuffer)localObject1).get();
        ((IntBuffer)localObject1).get();
        localDirectoryEntry.offset = ((IntBuffer)localObject1).get();
        localDirectoryEntry.length = ((IntBuffer)localObject1).get();
        if (localDirectoryEntry.offset + localDirectoryEntry.length > this.fileSize)
          throw new FontFormatException("bad table, tag=" + localDirectoryEntry.tag);
      }
      initNames();
    }
    catch (Exception localException)
    {
      if (FontManager.logging)
        FontManager.logger.severe(localException.toString());
      if (localException instanceof FontFormatException)
        throw ((FontFormatException)localException);
      throw new FontFormatException(localException.toString());
    }
    if ((this.familyName == null) || (this.fullName == null))
      throw new FontFormatException("Font name not found");
    ByteBuffer localByteBuffer3 = getTableBuffer(1330851634);
    setStyle(localByteBuffer3);
    setCJKSupport(localByteBuffer3);
    java.lang.Object localObject1 = getTableBuffer(1751474532);
    int j = -1;
    if ((localObject1 != null) && (((ByteBuffer)localObject1).capacity() >= 18))
    {
      localObject2 = ((ByteBuffer)localObject1).asShortBuffer();
      j = ((ShortBuffer)localObject2).get(9) & 0xFFFF;
    }
    setStrikethroughMetrics(localByteBuffer3, j);
    java.lang.Object localObject2 = getTableBuffer(1886352244);
    setUnderlineMetrics((ByteBuffer)localObject2, j);
  }

  static String getCodePage()
  {
    if (defaultCodePage != null)
      return defaultCodePage;
    if (FontManager.isWindows)
    {
      defaultCodePage = (String)AccessController.doPrivileged(new GetPropertyAction("file.encoding"));
    }
    else
    {
      if (languages.length != codePages.length)
        throw new InternalError("wrong code pages array length");
      Locale localLocale = SunToolkit.getStartupLocale();
      String str1 = localLocale.getLanguage();
      if (str1 != null)
      {
        if (str1.equals("zh"))
        {
          String str2 = localLocale.getCountry();
          if (str2 != null)
            str1 = str1 + "_" + str2;
        }
        for (int i = 0; i < languages.length; ++i)
          for (int j = 0; j < languages[i].length; ++j)
            if (str1.equals(languages[i][j]))
            {
              defaultCodePage = codePages[i];
              return defaultCodePage;
            }
      }
    }
    if (defaultCodePage == null)
      defaultCodePage = "";
    return defaultCodePage;
  }

  boolean supportsEncoding(String paramString)
  {
    if (paramString == null)
      paramString = getCodePage();
    if ("".equals(paramString))
      return false;
    paramString = paramString.toLowerCase();
    if (paramString.equals("gb18030"))
      paramString = "gbk";
    else if (paramString.equals("ms950_hkscs"))
      paramString = "ms950";
    ByteBuffer localByteBuffer = getTableBuffer(1330851634);
    if ((localByteBuffer == null) || (localByteBuffer.capacity() < 86))
      return false;
    int i = localByteBuffer.getInt(78);
    int j = localByteBuffer.getInt(82);
    for (int k = 0; k < encoding_mapping.length; ++k)
      if ((encoding_mapping[k].equals(paramString)) && ((1 << k & i) != 0))
        return true;
    return false;
  }

  private void setCJKSupport(ByteBuffer paramByteBuffer)
  {
    if ((paramByteBuffer == null) || (paramByteBuffer.capacity() < 50))
      return;
    int i = paramByteBuffer.getInt(46);
    this.supportsCJK = ((i & 0x29BF0000) != 0);
    this.supportsJA = ((i & 0x60000) != 0);
  }

  boolean supportsJA()
  {
    return this.supportsJA;
  }

  // ERROR //
  ByteBuffer getTableBuffer(int paramInt)
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore_2
    //   2: iconst_0
    //   3: istore_3
    //   4: iload_3
    //   5: aload_0
    //   6: getfield 675	sun/font/TrueTypeFont:numTables	I
    //   9: if_icmpge +32 -> 41
    //   12: aload_0
    //   13: getfield 696	sun/font/TrueTypeFont:tableDirectory	[Lsun/font/TrueTypeFont$DirectoryEntry;
    //   16: iload_3
    //   17: aaload
    //   18: getfield 700	sun/font/TrueTypeFont$DirectoryEntry:tag	I
    //   21: iload_1
    //   22: if_icmpne +13 -> 35
    //   25: aload_0
    //   26: getfield 696	sun/font/TrueTypeFont:tableDirectory	[Lsun/font/TrueTypeFont$DirectoryEntry;
    //   29: iload_3
    //   30: aaload
    //   31: astore_2
    //   32: goto +9 -> 41
    //   35: iinc 3 1
    //   38: goto -34 -> 4
    //   41: aload_2
    //   42: ifnull +26 -> 68
    //   45: aload_2
    //   46: getfield 698	sun/font/TrueTypeFont$DirectoryEntry:length	I
    //   49: ifeq +19 -> 68
    //   52: aload_2
    //   53: getfield 699	sun/font/TrueTypeFont$DirectoryEntry:offset	I
    //   56: aload_2
    //   57: getfield 698	sun/font/TrueTypeFont$DirectoryEntry:length	I
    //   60: iadd
    //   61: aload_0
    //   62: getfield 672	sun/font/TrueTypeFont:fileSize	I
    //   65: if_icmple +5 -> 70
    //   68: aconst_null
    //   69: areturn
    //   70: iconst_0
    //   71: istore_3
    //   72: aload_2
    //   73: getfield 698	sun/font/TrueTypeFont$DirectoryEntry:length	I
    //   76: invokestatic 736	java/nio/ByteBuffer:allocate	(I)Ljava/nio/ByteBuffer;
    //   79: astore 4
    //   81: aload_0
    //   82: dup
    //   83: astore 5
    //   85: monitorenter
    //   86: aload_0
    //   87: getfield 697	sun/font/TrueTypeFont:disposerRecord	Lsun/font/TrueTypeFont$TTDisposerRecord;
    //   90: getfield 701	sun/font/TrueTypeFont$TTDisposerRecord:channel	Ljava/nio/channels/FileChannel;
    //   93: ifnonnull +8 -> 101
    //   96: aload_0
    //   97: invokespecial 791	sun/font/TrueTypeFont:open	()Ljava/nio/channels/FileChannel;
    //   100: pop
    //   101: aload_0
    //   102: getfield 697	sun/font/TrueTypeFont:disposerRecord	Lsun/font/TrueTypeFont$TTDisposerRecord;
    //   105: getfield 701	sun/font/TrueTypeFont$TTDisposerRecord:channel	Ljava/nio/channels/FileChannel;
    //   108: aload_2
    //   109: getfield 699	sun/font/TrueTypeFont$DirectoryEntry:offset	I
    //   112: i2l
    //   113: invokevirtual 751	java/nio/channels/FileChannel:position	(J)Ljava/nio/channels/FileChannel;
    //   116: pop
    //   117: aload_0
    //   118: getfield 697	sun/font/TrueTypeFont:disposerRecord	Lsun/font/TrueTypeFont$TTDisposerRecord;
    //   121: getfield 701	sun/font/TrueTypeFont$TTDisposerRecord:channel	Ljava/nio/channels/FileChannel;
    //   124: aload 4
    //   126: invokevirtual 750	java/nio/channels/FileChannel:read	(Ljava/nio/ByteBuffer;)I
    //   129: istore_3
    //   130: aload 4
    //   132: invokevirtual 734	java/nio/ByteBuffer:flip	()Ljava/nio/Buffer;
    //   135: pop
    //   136: goto +36 -> 172
    //   139: astore 6
    //   141: invokestatic 723	java/lang/Thread:interrupted	()Z
    //   144: pop
    //   145: aload_0
    //   146: invokevirtual 773	sun/font/TrueTypeFont:close	()V
    //   149: aload_0
    //   150: iload_1
    //   151: invokevirtual 784	sun/font/TrueTypeFont:getTableBuffer	(I)Ljava/nio/ByteBuffer;
    //   154: aload 5
    //   156: monitorexit
    //   157: areturn
    //   158: astore 6
    //   160: aconst_null
    //   161: aload 5
    //   163: monitorexit
    //   164: areturn
    //   165: astore 6
    //   167: aconst_null
    //   168: aload 5
    //   170: monitorexit
    //   171: areturn
    //   172: iload_3
    //   173: aload_2
    //   174: getfield 698	sun/font/TrueTypeFont$DirectoryEntry:length	I
    //   177: if_icmpge +8 -> 185
    //   180: aconst_null
    //   181: aload 5
    //   183: monitorexit
    //   184: areturn
    //   185: aload 4
    //   187: aload 5
    //   189: monitorexit
    //   190: areturn
    //   191: astore 7
    //   193: aload 5
    //   195: monitorexit
    //   196: aload 7
    //   198: athrow
    //
    // Exception table:
    //   from	to	target	type
    //   86	136	139	java/nio/channels/ClosedChannelException
    //   86	136	158	java/io/IOException
    //   86	136	165	FontFormatException
    //   86	157	191	finally
    //   158	164	191	finally
    //   165	171	191	finally
    //   172	184	191	finally
    //   185	190	191	finally
    //   191	196	191	finally
  }

  byte[] getTableBytes(int paramInt)
  {
    byte[] arrayOfByte;
    ByteBuffer localByteBuffer = getTableBuffer(paramInt);
    if (localByteBuffer == null)
      return null;
    if (localByteBuffer.hasArray());
    try
    {
      return localByteBuffer.array();
    }
    catch (Exception arrayOfByte)
    {
      arrayOfByte = new byte[getTableSize(paramInt)];
      localByteBuffer.get(arrayOfByte);
    }
    return arrayOfByte;
  }

  int getTableSize(int paramInt)
  {
    for (int i = 0; i < this.numTables; ++i)
      if (this.tableDirectory[i].tag == paramInt)
        return this.tableDirectory[i].length;
    return 0;
  }

  int getTableOffset(int paramInt)
  {
    for (int i = 0; i < this.numTables; ++i)
      if (this.tableDirectory[i].tag == paramInt)
        return this.tableDirectory[i].offset;
    return 0;
  }

  DirectoryEntry getDirectoryEntry(int paramInt)
  {
    for (int i = 0; i < this.numTables; ++i)
      if (this.tableDirectory[i].tag == paramInt)
        return this.tableDirectory[i];
    return null;
  }

  boolean useEmbeddedBitmapsForSize(int paramInt)
  {
    if (!(this.supportsCJK))
      return false;
    if (getDirectoryEntry(1161972803) == null)
      return false;
    ByteBuffer localByteBuffer = getTableBuffer(1161972803);
    int i = localByteBuffer.getInt(4);
    for (int j = 0; j < i; ++j)
    {
      int k = localByteBuffer.get(8 + j * 48 + 45) & 0xFF;
      if (k == paramInt)
        return true;
    }
    return false;
  }

  public String getFullName()
  {
    return this.fullName;
  }

  protected void setStyle()
  {
    setStyle(getTableBuffer(1330851634));
  }

  private void setStyle(ByteBuffer paramByteBuffer)
  {
    if ((paramByteBuffer == null) || (paramByteBuffer.capacity() < 64))
    {
      super.setStyle();
      return;
    }
    int i = paramByteBuffer.getChar(62) & 0xFFFF;
    int j = i & 0x1;
    int k = i & 0x20;
    int l = i & 0x40;
    if ((l != 0) && ((j | k) != 0))
    {
      super.setStyle();
      return;
    }
    if ((l | j | k) == 0)
    {
      super.setStyle();
      return;
    }
    switch (k | j)
    {
    case 1:
      this.style = 2;
      break;
    case 32:
      if ((FontManager.isSolaris) && (this.platName.endsWith("HG-GothicB.ttf")))
      {
        this.style = 0;
        return;
      }
      this.style = 1;
      break;
    case 33:
      this.style = 3;
    }
  }

  private void setStrikethroughMetrics(ByteBuffer paramByteBuffer, int paramInt)
  {
    if ((paramByteBuffer == null) || (paramByteBuffer.capacity() < 30) || (paramInt < 0))
    {
      this.stSize = 0.05000000074505806F;
      this.stPos = -0.40000000596046448F;
      return;
    }
    ShortBuffer localShortBuffer = paramByteBuffer.asShortBuffer();
    this.stSize = (localShortBuffer.get(13) / paramInt);
    this.stPos = (-localShortBuffer.get(14) / paramInt);
  }

  private void setUnderlineMetrics(ByteBuffer paramByteBuffer, int paramInt)
  {
    if ((paramByteBuffer == null) || (paramByteBuffer.capacity() < 12) || (paramInt < 0))
    {
      this.ulSize = 0.05000000074505806F;
      this.ulPos = 0.10000000149011612F;
      return;
    }
    ShortBuffer localShortBuffer = paramByteBuffer.asShortBuffer();
    this.ulSize = (localShortBuffer.get(5) / paramInt);
    this.ulPos = (-localShortBuffer.get(4) / paramInt);
  }

  public void getStyleMetrics(float paramFloat, float[] paramArrayOfFloat, int paramInt)
  {
    paramArrayOfFloat[paramInt] = (this.stPos * paramFloat);
    paramArrayOfFloat[(paramInt + 1)] = (this.stSize * paramFloat);
    paramArrayOfFloat[(paramInt + 2)] = (this.ulPos * paramFloat);
    paramArrayOfFloat[(paramInt + 3)] = (this.ulSize * paramFloat);
  }

  private String makeString(byte[] paramArrayOfByte, int paramInt, short paramShort)
  {
    java.lang.Object localObject;
    if ((paramShort >= 2) && (paramShort <= 6))
    {
      localObject = paramArrayOfByte;
      int i = paramInt;
      paramArrayOfByte = new byte[i];
      paramInt = 0;
      for (int j = 0; j < i; ++j)
        if (localObject[j] != 0)
          paramArrayOfByte[(paramInt++)] = localObject[j];
    }
    switch (paramShort)
    {
    case 1:
      localObject = "UTF-16";
      break;
    case 0:
      localObject = "UTF-16";
      break;
    case 2:
      localObject = "SJIS";
      break;
    case 3:
      localObject = "GBK";
      break;
    case 4:
      localObject = "MS950";
      break;
    case 5:
      localObject = "EUC_KR";
      break;
    case 6:
      localObject = "Johab";
      break;
    default:
      localObject = "UTF-16";
    }
    try
    {
      return new String(paramArrayOfByte, 0, paramInt, (String)localObject);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      if (FontManager.logging)
        FontManager.logger.warning(localUnsupportedEncodingException + " EncodingID=" + paramShort);
      return new String(paramArrayOfByte, 0, paramInt);
    }
    catch (Throwable localThrowable)
    {
    }
    return ((String)null);
  }

  protected void initNames()
  {
    byte[] arrayOfByte = new byte[256];
    ByteBuffer localByteBuffer = getTableBuffer(1851878757);
    if (localByteBuffer != null)
    {
      ShortBuffer localShortBuffer = localByteBuffer.asShortBuffer();
      localShortBuffer.get();
      int i = localShortBuffer.get();
      int j = localShortBuffer.get() & 0xFFFF;
      for (int k = 0; k < i; ++k)
      {
        int l = localShortBuffer.get();
        if (l != 3)
        {
          localShortBuffer.position(localShortBuffer.position() + 5);
        }
        else
        {
          short s = localShortBuffer.get();
          int i1 = localShortBuffer.get();
          int i2 = localShortBuffer.get();
          int i3 = localShortBuffer.get() & 0xFFFF;
          int i4 = (localShortBuffer.get() & 0xFFFF) + j;
          switch (i2)
          {
          case 1:
            if ((this.familyName == null) || (i1 == 1033))
            {
              localByteBuffer.position(i4);
              localByteBuffer.get(arrayOfByte, 0, i3);
              this.familyName = makeString(arrayOfByte, i3, s);
            }
            break;
          case 4:
            if ((this.fullName == null) || (i1 == 1033))
            {
              localByteBuffer.position(i4);
              localByteBuffer.get(arrayOfByte, 0, i3);
              this.fullName = makeString(arrayOfByte, i3, s);
            }
          }
        }
      }
    }
  }

  protected String lookupName(short paramShort, int paramInt)
  {
    String str = null;
    byte[] arrayOfByte = new byte[1024];
    ByteBuffer localByteBuffer = getTableBuffer(1851878757);
    if (localByteBuffer != null)
    {
      ShortBuffer localShortBuffer = localByteBuffer.asShortBuffer();
      localShortBuffer.get();
      int i = localShortBuffer.get();
      int j = localShortBuffer.get() & 0xFFFF;
      for (int k = 0; k < i; ++k)
      {
        int l = localShortBuffer.get();
        if (l != 3)
        {
          localShortBuffer.position(localShortBuffer.position() + 5);
        }
        else
        {
          short s1 = localShortBuffer.get();
          short s2 = localShortBuffer.get();
          int i1 = localShortBuffer.get();
          int i2 = localShortBuffer.get() & 0xFFFF;
          int i3 = (localShortBuffer.get() & 0xFFFF) + j;
          if ((i1 == paramInt) && ((((str == null) && (s2 == 1033)) || (s2 == paramShort))))
          {
            localByteBuffer.position(i3);
            localByteBuffer.get(arrayOfByte, 0, i2);
            str = makeString(arrayOfByte, i2, s1);
            if (s2 == paramShort)
              return str;
          }
        }
      }
    }
    return str;
  }

  public int getFontCount()
  {
    return this.directoryCount;
  }

  private void initBWGlyphs()
  {
    if ("Courier New".equals(this.fullName))
    {
      this.bwGlyphs = new int[2];
      CharToGlyphMapper localCharToGlyphMapper = getMapper();
      this.bwGlyphs[0] = localCharToGlyphMapper.charToGlyph('W');
      this.bwGlyphs[1] = localCharToGlyphMapper.charToGlyph('w');
    }
  }

  private native long createScaler(int paramInt1, int paramInt2, boolean paramBoolean, int[] paramArrayOfInt);

  protected synchronized long getScaler()
  {
    if (this.pScaler == 3412046810217185280L)
    {
      initBWGlyphs();
      this.pScaler = createScaler(this.fileSize, this.fontIndex, this.supportsCJK, this.bwGlyphs);
      if (this.pScaler != 3412047325613260800L)
      {
        Disposer.addObjectRecord(this, new FileFont.FileFontDisposer(this.pScaler));
      }
      else
      {
        this.pScaler = getNullScaler();
        FontManager.deRegisterBadFont(this);
      }
    }
    return this.pScaler;
  }

  public String getPostscriptName()
  {
    String str = lookupName(1033, 6);
    if (str == null)
      return this.fullName;
    return str;
  }

  public String getFontName(Locale paramLocale)
  {
    if (paramLocale == null)
      return this.fullName;
    short s = FontManager.getLCIDFromLocale(paramLocale);
    String str = lookupName(s, 4);
    if (str == null)
      return this.fullName;
    return str;
  }

  public String getFamilyName(Locale paramLocale)
  {
    if (paramLocale == null)
      return this.familyName;
    short s = FontManager.getLCIDFromLocale(paramLocale);
    String str = lookupName(s, 1);
    if (str == null)
      return this.familyName;
    return str;
  }

  public CharToGlyphMapper getMapper()
  {
    if (this.mapper == null)
      this.mapper = new TrueTypeGlyphMapper(this);
    return this.mapper;
  }

  protected void initAllNames(int paramInt, HashSet paramHashSet)
  {
    byte[] arrayOfByte = new byte[256];
    ByteBuffer localByteBuffer = getTableBuffer(1851878757);
    if (localByteBuffer != null)
    {
      ShortBuffer localShortBuffer = localByteBuffer.asShortBuffer();
      localShortBuffer.get();
      int i = localShortBuffer.get();
      int j = localShortBuffer.get() & 0xFFFF;
      for (int k = 0; k < i; ++k)
      {
        int l = localShortBuffer.get();
        if (l != 3)
        {
          localShortBuffer.position(localShortBuffer.position() + 5);
        }
        else
        {
          short s = localShortBuffer.get();
          int i1 = localShortBuffer.get();
          int i2 = localShortBuffer.get();
          int i3 = localShortBuffer.get() & 0xFFFF;
          int i4 = (localShortBuffer.get() & 0xFFFF) + j;
          if (i2 == paramInt)
          {
            localByteBuffer.position(i4);
            localByteBuffer.get(arrayOfByte, 0, i3);
            paramHashSet.add(makeString(arrayOfByte, i3, s));
          }
        }
      }
    }
  }

  String[] getAllFamilyNames()
  {
    HashSet localHashSet = new HashSet();
    try
    {
      initAllNames(1, localHashSet);
    }
    catch (Exception localException)
    {
    }
    return ((String[])(String[])localHashSet.toArray(new String[0]));
  }

  String[] getAllFullNames()
  {
    HashSet localHashSet = new HashSet();
    try
    {
      initAllNames(4, localHashSet);
    }
    catch (Exception localException)
    {
    }
    return ((String[])(String[])localHashSet.toArray(new String[0]));
  }

  synchronized native Point2D.Float getGlyphPoint(long paramLong, int paramInt1, int paramInt2);

  private char[] getGaspTable()
  {
    if (this.gaspTable != null)
      return this.gaspTable;
    ByteBuffer localByteBuffer = getTableBuffer(1734439792);
    if (localByteBuffer == null)
      return (this.gaspTable = new char[0]);
    CharBuffer localCharBuffer = localByteBuffer.asCharBuffer();
    int i = localCharBuffer.get();
    if (i > 1)
      return (this.gaspTable = new char[0]);
    int j = localCharBuffer.get();
    if (4 + j * 4 > getTableSize(1734439792))
      return (this.gaspTable = new char[0]);
    this.gaspTable = new char[2 * j];
    localCharBuffer.get(this.gaspTable);
    return this.gaspTable;
  }

  public boolean useAAForPtSize(int paramInt)
  {
    char[] arrayOfChar = getGaspTable();
    if (arrayOfChar.length > 0)
    {
      for (int i = 0; i < arrayOfChar.length; i += 2)
        if (paramInt <= arrayOfChar[i])
          return ((arrayOfChar[(i + 1)] & 0x2) != 0);
      return true;
    }
    if (this.style == 1)
      return true;
    return ((paramInt <= 8) || (paramInt >= 18));
  }

  public boolean hasSupplementaryChars()
  {
    return ((TrueTypeGlyphMapper)getMapper()).hasSupplementaryChars();
  }

  public String toString()
  {
    return "** TrueType Font: Family=" + this.familyName + " Name=" + this.fullName + " style=" + this.style + " fileName=" + this.platName;
  }

  class DirectoryEntry
  {
    int tag;
    int offset;
    int length;
  }

  private static class TTDisposerRecord
  implements DisposerRecord
  {
    FileChannel channel = null;

    public synchronized void dispose()
    {
      try
      {
        if (this.channel != null)
          this.channel.close();
      }
      catch (IOException localIOException)
      {
      }
      finally
      {
        this.channel = null;
      }
    }
  }
}
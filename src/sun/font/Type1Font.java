package sun.font;

import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import sun.java2d.Disposer;

public class Type1Font extends FileFont
{
  WeakReference bufferRef = new WeakReference(null);
  private String psName = null;
  private static HashMap styleAbbreviationsMapping = new HashMap();
  private static HashSet styleNameTokes = new HashSet();
  private static final int PSEOFTOKEN = 0;
  private static final int PSNAMETOKEN = 1;
  private static final int PSSTRINGTOKEN = 2;

  public Type1Font(String paramString, Object paramObject)
    throws FontFormatException
  {
    super(paramString, paramObject);
    this.fontRank = 4;
    this.checkedNatives = true;
    verify();
  }

  private synchronized ByteBuffer getBuffer()
    throws FontFormatException
  {
    MappedByteBuffer localMappedByteBuffer = (MappedByteBuffer)this.bufferRef.get();
    if (localMappedByteBuffer == null)
      try
      {
        RandomAccessFile localRandomAccessFile = (RandomAccessFile)AccessController.doPrivileged(new PrivilegedAction(this)
        {
          public Object run()
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
        FileChannel localFileChannel = localRandomAccessFile.getChannel();
        this.fileSize = (int)localFileChannel.size();
        localMappedByteBuffer = localFileChannel.map(FileChannel.MapMode.READ_ONLY, 3412040092888334336L, this.fileSize);
        localMappedByteBuffer.position(0);
        this.bufferRef = new WeakReference(localMappedByteBuffer);
        localFileChannel.close();
      }
      catch (NullPointerException localNullPointerException)
      {
        throw new FontFormatException(localNullPointerException.toString());
      }
      catch (ClosedChannelException localClosedChannelException)
      {
        Thread.interrupted();
        return getBuffer();
      }
      catch (IOException localIOException)
      {
        throw new FontFormatException(localIOException.toString());
      }
    return localMappedByteBuffer;
  }

  protected void close()
  {
  }

  void readFile(ByteBuffer paramByteBuffer)
  {
    RandomAccessFile localRandomAccessFile = null;
    try
    {
      localRandomAccessFile = (RandomAccessFile)AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
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
      FileChannel localFileChannel = localRandomAccessFile.getChannel();
      localFileChannel.read(paramByteBuffer);
    }
    catch (NullPointerException localIOException2)
    {
    }
    catch (ClosedChannelException localIOException3)
    {
      try
      {
        if (localRandomAccessFile != null)
        {
          localRandomAccessFile.close();
          localRandomAccessFile = null;
        }
      }
      catch (IOException localIOException6)
      {
      }
      Thread.interrupted();
      readFile(paramByteBuffer);
    }
    catch (IOException localIOException5)
    {
    }
    finally
    {
      if (localRandomAccessFile != null)
        try
        {
          localRandomAccessFile.close();
        }
        catch (IOException localIOException7)
        {
        }
    }
  }

  public synchronized ByteBuffer readBlock(int paramInt1, int paramInt2)
  {
    ByteBuffer localByteBuffer = null;
    try
    {
      localByteBuffer = getBuffer();
      if (paramInt1 > this.fileSize)
        paramInt1 = this.fileSize;
      localByteBuffer.position(paramInt1);
      return localByteBuffer.slice();
    }
    catch (FontFormatException localFontFormatException)
    {
    }
    return null;
  }

  private void verify()
    throws FontFormatException
  {
    ByteBuffer localByteBuffer = getBuffer();
    if (localByteBuffer.capacity() < 6)
      throw new FontFormatException("short file");
    int i = localByteBuffer.get(0) & 0xFF;
    if ((localByteBuffer.get(0) & 0xFF) == 128)
    {
      verifyPFB(localByteBuffer);
      localByteBuffer.position(6);
    }
    else
    {
      verifyPFA(localByteBuffer);
      localByteBuffer.position(0);
    }
    initNames(localByteBuffer);
    if ((this.familyName == null) || (this.fullName == null))
      throw new FontFormatException("Font name not found");
    setStyle();
  }

  public int getFileSize()
  {
    if (this.fileSize == 0)
      try
      {
        getBuffer();
      }
      catch (FontFormatException localFontFormatException)
      {
      }
    return this.fileSize;
  }

  private void verifyPFA(ByteBuffer paramByteBuffer)
    throws FontFormatException
  {
    if (paramByteBuffer.getShort() != 9505)
      throw new FontFormatException("bad pfa font");
  }

  private void verifyPFB(ByteBuffer paramByteBuffer)
    throws FontFormatException
  {
    int i = 0;
    try
    {
      int j = paramByteBuffer.getShort(i) & 0xFFFF;
      if ((j == 32769) || (j == 32770))
      {
        paramByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int k = paramByteBuffer.getInt(i + 2);
        paramByteBuffer.order(ByteOrder.BIG_ENDIAN);
        if (k <= 0)
          throw new FontFormatException("bad segment length");
        i += k + 6;
      }
      else
      {
        if (j == 32771)
          return;
        throw new FontFormatException("bad pfb file");
      }
    }
    catch (BufferUnderflowException localBufferUnderflowException)
    {
      throw new FontFormatException(localBufferUnderflowException.toString());
    }
    catch (Exception localException)
    {
      throw new FontFormatException(localException.toString());
    }
  }

  private void initNames(ByteBuffer paramByteBuffer)
    throws FontFormatException
  {
    int i = 0;
    Object localObject = null;
    try
    {
      while ((((this.fullName == null) || (this.familyName == null) || (this.psName == null) || (localObject == null))) && (i == 0))
      {
        int j = nextTokenType(paramByteBuffer);
        if (j == 1)
        {
          int k = paramByteBuffer.position();
          if (paramByteBuffer.get(k) == 70)
          {
            String str2 = getSimpleToken(paramByteBuffer);
            if ("FullName".equals(str2))
            {
              if (nextTokenType(paramByteBuffer) == 2)
                this.fullName = getString(paramByteBuffer);
            }
            else if ("FamilyName".equals(str2))
            {
              if (nextTokenType(paramByteBuffer) == 2)
                this.familyName = getString(paramByteBuffer);
            }
            else if ("FontName".equals(str2))
            {
              if (nextTokenType(paramByteBuffer) == 1)
                this.psName = getSimpleToken(paramByteBuffer);
            }
            else if ("FontType".equals(str2))
            {
              String str3 = getSimpleToken(paramByteBuffer);
              if ("def".equals(getSimpleToken(paramByteBuffer)))
                localObject = str3;
            }
          }
          else
          {
            while (paramByteBuffer.get() > 32);
          }
        }
        else if (j == 0)
        {
          i = 1;
        }
      }
    }
    catch (Exception localException)
    {
      throw new FontFormatException(localException.toString());
    }
    if (!("1".equals(localObject)))
      throw new FontFormatException("Unsupported font type");
    if (this.psName == null)
    {
      paramByteBuffer.position(0);
      if (paramByteBuffer.getShort() != 9505)
        paramByteBuffer.position(8);
      String str1 = getSimpleToken(paramByteBuffer);
      if ((!(str1.startsWith("FontType1-"))) && (!(str1.startsWith("PS-AdobeFont-"))))
        throw new FontFormatException("Unsupported font format [" + str1 + "]");
      this.psName = getSimpleToken(paramByteBuffer);
    }
    if (i != 0)
      if (this.fullName != null)
      {
        this.familyName = fullName2FamilyName(this.fullName);
      }
      else if (this.familyName != null)
      {
        this.fullName = this.familyName;
      }
      else
      {
        this.fullName = psName2FullName(this.psName);
        this.familyName = psName2FamilyName(this.psName);
      }
  }

  private String fullName2FamilyName(String paramString)
  {
    for (int j = paramString.length(); j > 0; j = i)
    {
      for (int i = j - 1; (i > 0) && (paramString.charAt(i) != ' '); --i);
      if (!(isStyleToken(paramString.substring(i + 1, j))))
        return paramString.substring(0, j);
    }
    return paramString;
  }

  private String expandAbbreviation(String paramString)
  {
    if (styleAbbreviationsMapping.containsKey(paramString))
      return ((String)styleAbbreviationsMapping.get(paramString));
    return paramString;
  }

  private boolean isStyleToken(String paramString)
  {
    return styleNameTokes.contains(paramString);
  }

  private String psName2FullName(String paramString)
  {
    String str;
    int i = paramString.indexOf("-");
    if (i >= 0)
    {
      str = expandName(paramString.substring(0, i), false);
      str = str + " " + expandName(paramString.substring(i + 1), true);
    }
    else
    {
      str = expandName(paramString, false);
    }
    return str;
  }

  private String psName2FamilyName(String paramString)
  {
    String str = paramString;
    if (str.indexOf("-") > 0)
      str = str.substring(0, str.indexOf("-"));
    return expandName(str, false);
  }

  private int nextCapitalLetter(String paramString, int paramInt)
  {
    while ((paramInt >= 0) && (paramInt < paramString.length()))
    {
      if ((paramString.charAt(paramInt) >= 'A') && (paramString.charAt(paramInt) <= 'Z'))
        return paramInt;
      ++paramInt;
    }
    return -1;
  }

  private String expandName(String paramString, boolean paramBoolean)
  {
    StringBuffer localStringBuffer = new StringBuffer(paramString.length() + 10);
    for (int i = 0; i < paramString.length(); i = j)
    {
      int j = nextCapitalLetter(paramString, i + 1);
      if (j < 0)
        j = paramString.length();
      if (i != 0)
        localStringBuffer.append(" ");
      if (paramBoolean)
        localStringBuffer.append(expandAbbreviation(paramString.substring(i, j)));
      else
        localStringBuffer.append(paramString.substring(i, j));
    }
    return localStringBuffer.toString();
  }

  private byte skip(ByteBuffer paramByteBuffer)
  {
    int i = paramByteBuffer.get();
    while (i == 37)
      do
        do
          i = paramByteBuffer.get();
        while (i == 13);
      while (i != 10);
    while (i <= 32)
      i = paramByteBuffer.get();
    return i;
  }

  private int nextTokenType(ByteBuffer paramByteBuffer)
  {
    int i;
    try
    {
      i = skip(paramByteBuffer);
      while (true)
      {
        if (i == 47)
          return 1;
        if (i == 40)
          return 2;
        if ((i != 13) && (i != 10))
          break;
        i = skip(paramByteBuffer);
      }
      i = paramByteBuffer.get();
    }
    catch (BufferUnderflowException localBufferUnderflowException)
    {
    }
    return 0;
  }

  private String getSimpleToken(ByteBuffer paramByteBuffer)
  {
    while (paramByteBuffer.get() <= 32);
    int i = paramByteBuffer.position() - 1;
    while (paramByteBuffer.get() > 32);
    int j = paramByteBuffer.position();
    byte[] arrayOfByte = new byte[j - i - 1];
    paramByteBuffer.position(i);
    paramByteBuffer.get(arrayOfByte);
    try
    {
      return new String(arrayOfByte, "US-ASCII");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    return new String(arrayOfByte);
  }

  private String getString(ByteBuffer paramByteBuffer)
  {
    int i = paramByteBuffer.position();
    while (paramByteBuffer.get() != 41);
    int j = paramByteBuffer.position();
    byte[] arrayOfByte = new byte[j - i - 1];
    paramByteBuffer.position(i);
    paramByteBuffer.get(arrayOfByte);
    try
    {
      return new String(arrayOfByte, "US-ASCII");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    return new String(arrayOfByte);
  }

  public String getPostscriptName()
  {
    return this.psName;
  }

  private native long createScaler(int paramInt);

  protected synchronized long getScaler()
  {
    if (this.pScaler == 3412046810217185280L)
    {
      this.pScaler = createScaler(this.fileSize);
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

  CharToGlyphMapper getMapper()
  {
    if (this.mapper == null)
      this.mapper = new Type1GlyphMapper(this);
    return this.mapper;
  }

  native int getNumGlyphs(long paramLong);

  native int getMissingGlyphCode(long paramLong);

  synchronized native int getGlyphCode(long paramLong, char paramChar);

  public String toString()
  {
    return "** Type1 Font: Family=" + this.familyName + " Name=" + this.fullName + " style=" + this.style + " fileName=" + this.platName;
  }

  static
  {
    String[] arrayOfString1 = { "Black", "Bold", "Book", "Demi", "Heavy", "Light", "Meduium", "Nord", "Poster", "Regular", "Super", "Thin", "Compressed", "Condensed", "Compact", "Extended", "Narrow", "Inclined", "Italic", "Kursiv", "Oblique", "Upright", "Sloped", "Semi", "Ultra", "Extra", "Alternate", "Alternate", "Deutsche Fraktur", "Expert", "Inline", "Ornaments", "Outline", "Roman", "Rounded", "Script", "Shaded", "Swash", "Titling", "Typewriter" };
    String[] arrayOfString2 = { "Blk", "Bd", "Bk", "Dm", "Hv", "Lt", "Md", "Nd", "Po", "Rg", "Su", "Th", "Cm", "Cn", "Ct", "Ex", "Nr", "Ic", "It", "Ks", "Obl", "Up", "Sl", "Sm", "Ult", "X", "A", "Alt", "Dfr", "Exp", "In", "Or", "Ou", "Rm", "Rd", "Scr", "Sh", "Sw", "Ti", "Typ" };
    String[] arrayOfString3 = { "Black", "Bold", "Book", "Demi", "Heavy", "Light", "Medium", "Nord", "Poster", "Regular", "Super", "Thin", "Compressed", "Condensed", "Compact", "Extended", "Narrow", "Inclined", "Italic", "Kursiv", "Oblique", "Upright", "Sloped", "Slanted", "Semi", "Ultra", "Extra" };
    for (int i = 0; i < arrayOfString1.length; ++i)
      styleAbbreviationsMapping.put(arrayOfString2[i], arrayOfString1[i]);
    for (i = 0; i < arrayOfString3.length; ++i)
      styleNameTokes.add(arrayOfString3[i]);
  }
}
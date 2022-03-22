package sun.awt;

import java.awt.GraphicsEnvironment;
import java.awt.peer.FontPeer;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.util.Locale;
import java.util.Vector;
import sun.java2d.FontSupport;

public abstract class PlatformFont
  implements FontPeer
{
  protected FontDescriptor[] componentFonts;
  protected char defaultChar;
  protected FontConfiguration fontConfig;
  protected FontDescriptor defaultFont;
  protected String familyName;
  private Object[] fontCache;
  protected static int FONTCACHESIZE;
  protected static int FONTCACHEMASK;
  protected static String osVersion;

  public PlatformFont(String paramString, int paramInt)
  {
    GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    if (localGraphicsEnvironment instanceof FontSupport)
      this.fontConfig = ((FontSupport)localGraphicsEnvironment).getFontConfiguration();
    if (this.fontConfig == null)
      return;
    this.familyName = paramString.toLowerCase(Locale.ENGLISH);
    if (!(FontConfiguration.isLogicalFontFamilyName(this.familyName)))
      this.familyName = this.fontConfig.getFallbackFamilyName(this.familyName, "sansserif");
    this.componentFonts = this.fontConfig.getFontDescriptors(this.familyName, paramInt);
    char c = getMissingGlyphCharacter();
    this.defaultChar = '?';
    if (this.componentFonts.length > 0)
      this.defaultFont = this.componentFonts[0];
    for (int i = 0; i < this.componentFonts.length; ++i)
    {
      if (this.componentFonts[i].isExcluded(c))
        break label189:
      if (this.componentFonts[i].encoder.canEncode(c))
      {
        this.defaultFont = this.componentFonts[i];
        this.defaultChar = c;
        label189: return;
      }
    }
  }

  protected abstract char getMissingGlyphCharacter();

  public CharsetString[] makeMultiCharsetString(String paramString)
  {
    return makeMultiCharsetString(paramString.toCharArray(), 0, paramString.length(), true);
  }

  public CharsetString[] makeMultiCharsetString(String paramString, boolean paramBoolean)
  {
    return makeMultiCharsetString(paramString.toCharArray(), 0, paramString.length(), paramBoolean);
  }

  public CharsetString[] makeMultiCharsetString(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    return makeMultiCharsetString(paramArrayOfChar, paramInt1, paramInt2, true);
  }

  public CharsetString[] makeMultiCharsetString(char[] paramArrayOfChar, int paramInt1, int paramInt2, boolean paramBoolean)
  {
    label104: CharsetString[] arrayOfCharsetString;
    if (paramInt2 < 1)
      return new CharsetString[0];
    Vector localVector = null;
    char[] arrayOfChar = new char[paramInt2];
    int i = this.defaultChar;
    int j = 0;
    Object localObject = this.defaultFont;
    for (int k = 0; k < this.componentFonts.length; ++k)
    {
      if (this.componentFonts[k].isExcluded(paramArrayOfChar[paramInt1]))
        break label104:
      if (this.componentFonts[k].encoder.canEncode(paramArrayOfChar[paramInt1]))
      {
        localObject = this.componentFonts[k];
        i = paramArrayOfChar[paramInt1];
        j = 1;
        break;
      }
    }
    if ((!(paramBoolean)) && (j == 0))
      return null;
    arrayOfChar[0] = i;
    k = 0;
    for (int l = 1; l < paramInt2; ++l)
    {
      char c = paramArrayOfChar[(paramInt1 + l)];
      FontDescriptor localFontDescriptor = this.defaultFont;
      i = this.defaultChar;
      j = 0;
      for (int i2 = 0; i2 < this.componentFonts.length; ++i2)
      {
        if (this.componentFonts[i2].isExcluded(c))
          break label231:
        if (this.componentFonts[i2].encoder.canEncode(c))
        {
          localFontDescriptor = this.componentFonts[i2];
          i = c;
          j = 1;
          label231: break;
        }
      }
      if ((!(paramBoolean)) && (j == 0))
        return null;
      arrayOfChar[l] = i;
      if (localObject != localFontDescriptor)
      {
        if (localVector == null)
          localVector = new Vector(3);
        localVector.addElement(new CharsetString(arrayOfChar, k, l - k, (FontDescriptor)localObject));
        localObject = localFontDescriptor;
        localFontDescriptor = this.defaultFont;
        k = l;
      }
    }
    CharsetString localCharsetString = new CharsetString(arrayOfChar, k, paramInt2 - k, (FontDescriptor)localObject);
    if (localVector == null)
    {
      arrayOfCharsetString = new CharsetString[1];
      arrayOfCharsetString[0] = localCharsetString;
    }
    else
    {
      localVector.addElement(localCharsetString);
      arrayOfCharsetString = new CharsetString[localVector.size()];
      for (int i1 = 0; i1 < localVector.size(); ++i1)
        arrayOfCharsetString[i1] = ((CharsetString)localVector.elementAt(i1));
    }
    return ((CharsetString)arrayOfCharsetString);
  }

  public boolean mightHaveMultiFontMetrics()
  {
    return (this.fontConfig != null);
  }

  public Object[] makeConvertedMultiFontString(String paramString)
  {
    return makeConvertedMultiFontChars(paramString.toCharArray(), 0, paramString.length());
  }

  public Object[] makeConvertedMultiFontChars(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    Object localObject1 = new Object[2];
    byte[] arrayOfByte = null;
    int i = paramInt1;
    int j = 0;
    int k = 0;
    Object localObject2 = null;
    FontDescriptor localFontDescriptor1 = null;
    int i2 = paramInt1 + paramInt2;
    if ((paramInt1 < 0) || (i2 > paramArrayOfChar.length))
      throw new ArrayIndexOutOfBoundsException();
    if (i >= i2)
      return null;
    while (i < i2)
    {
      int i1 = paramArrayOfChar[i];
      int l = i1 & FONTCACHEMASK;
      PlatformFontCache localPlatformFontCache = (PlatformFontCache)getFontCache()[l];
      if ((localPlatformFontCache == null) || (localPlatformFontCache.uniChar != i1))
      {
        localObject2 = this.defaultFont;
        i1 = this.defaultChar;
        int i3 = paramArrayOfChar[i];
        i4 = this.componentFonts.length;
        for (int i5 = 0; i5 < i4; ++i5)
        {
          FontDescriptor localFontDescriptor2 = this.componentFonts[i5];
          localFontDescriptor2.encoder.reset();
          if (localFontDescriptor2.isExcluded(i3))
            break label197:
          if (localFontDescriptor2.encoder.canEncode(i3))
          {
            localObject2 = localFontDescriptor2;
            i1 = i3;
            label197: break;
          }
        }
        try
        {
          char[] arrayOfChar = new char[1];
          arrayOfChar[0] = i1;
          localPlatformFontCache = new PlatformFontCache(this);
          if (((FontDescriptor)localObject2).useUnicode())
            if (FontDescriptor.isLE)
            {
              localPlatformFontCache.bb.put((byte)(arrayOfChar[0] & 0xFF));
              localPlatformFontCache.bb.put((byte)(arrayOfChar[0] >> '\b'));
            }
            else
            {
              localPlatformFontCache.bb.put((byte)(arrayOfChar[0] >> '\b'));
              localPlatformFontCache.bb.put((byte)(arrayOfChar[0] & 0xFF));
            }
          else
            ((FontDescriptor)localObject2).encoder.encode(CharBuffer.wrap(arrayOfChar), localPlatformFontCache.bb, true);
          localPlatformFontCache.fontDescriptor = ((FontDescriptor)localObject2);
          localPlatformFontCache.uniChar = paramArrayOfChar[i];
          getFontCache()[l] = localPlatformFontCache;
        }
        catch (Exception localException)
        {
          System.err.println(localException);
          localException.printStackTrace();
          return null;
        }
      }
      if (localFontDescriptor1 != localPlatformFontCache.fontDescriptor)
      {
        if (localFontDescriptor1 != null)
        {
          localObject1[(k++)] = localFontDescriptor1;
          localObject1[(k++)] = arrayOfByte;
          if (arrayOfByte != null)
          {
            arrayOfByte[0] = (byte)((j -= 4) >> 24);
            arrayOfByte[1] = (byte)(j >> 16);
            arrayOfByte[2] = (byte)(j >> 8);
            arrayOfByte[3] = (byte)j;
          }
          if (k >= localObject1.length)
          {
            localObject3 = new Object[localObject1.length * 2];
            System.arraycopy(localObject1, 0, localObject3, 0, localObject1.length);
            localObject1 = localObject3;
          }
        }
        if (localPlatformFontCache.fontDescriptor.useUnicode())
          arrayOfByte = new byte[(i2 - i + 1) * (int)localPlatformFontCache.fontDescriptor.unicodeEncoder.maxBytesPerChar() + 4];
        else
          arrayOfByte = new byte[(i2 - i + 1) * (int)localPlatformFontCache.fontDescriptor.encoder.maxBytesPerChar() + 4];
        j = 4;
        localFontDescriptor1 = localPlatformFontCache.fontDescriptor;
      }
      Object localObject3 = localPlatformFontCache.bb.array();
      int i4 = localPlatformFontCache.bb.position();
      if (i4 == 1)
      {
        arrayOfByte[(j++)] = localObject3[0];
      }
      else if (i4 == 2)
      {
        arrayOfByte[(j++)] = localObject3[0];
        arrayOfByte[(j++)] = localObject3[1];
      }
      else if (i4 == 3)
      {
        arrayOfByte[(j++)] = localObject3[0];
        arrayOfByte[(j++)] = localObject3[1];
        arrayOfByte[(j++)] = localObject3[2];
      }
      else if (i4 == 4)
      {
        arrayOfByte[(j++)] = localObject3[0];
        arrayOfByte[(j++)] = localObject3[1];
        arrayOfByte[(j++)] = localObject3[2];
        arrayOfByte[(j++)] = localObject3[3];
      }
      ++i;
    }
    localObject1[(k++)] = localFontDescriptor1;
    localObject1[(k++)] = arrayOfByte;
    if (arrayOfByte != null)
    {
      arrayOfByte[0] = (byte)((j -= 4) >> 24);
      arrayOfByte[1] = (byte)(j >> 16);
      arrayOfByte[2] = (byte)(j >> 8);
      arrayOfByte[3] = (byte)j;
    }
    return ((Object)(Object)(Object)localObject1);
  }

  protected final Object[] getFontCache()
  {
    if (this.fontCache == null)
      this.fontCache = new Object[FONTCACHESIZE];
    return this.fontCache;
  }

  private static native void initIDs();

  static
  {
    NativeLibLoader.loadLibraries();
    initIDs();
    FONTCACHESIZE = 256;
    FONTCACHEMASK = FONTCACHESIZE - 1;
  }

  class PlatformFontCache
  {
    char uniChar;
    FontDescriptor fontDescriptor;
    ByteBuffer bb = ByteBuffer.allocate(4);
  }
}
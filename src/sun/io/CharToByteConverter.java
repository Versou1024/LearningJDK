package sun.io;

import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;

@Deprecated
public abstract class CharToByteConverter
{
  protected boolean subMode = true;
  protected byte[] subBytes = { 63 };
  protected int charOff;
  protected int byteOff;
  protected int badInputLength;

  public static CharToByteConverter getDefault()
  {
    Object localObject = Converters.newDefaultConverter(1);
    return ((CharToByteConverter)localObject);
  }

  public static CharToByteConverter getConverter(String paramString)
    throws UnsupportedEncodingException
  {
    Object localObject = Converters.newConverter(1, paramString);
    return ((CharToByteConverter)localObject);
  }

  public abstract String getCharacterEncoding();

  public abstract int convert(char[] paramArrayOfChar, int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3, int paramInt4)
    throws sun.io.MalformedInputException, sun.io.UnknownCharacterException, sun.io.ConversionBufferFullException;

  public int convertAny(char[] paramArrayOfChar, int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3, int paramInt4)
    throws sun.io.ConversionBufferFullException
  {
    if (!(this.subMode))
      throw new IllegalStateException("Substitution mode is not on");
    int i = paramInt1;
    int j = paramInt3;
    if (i < paramInt2)
      try
      {
        int k = convert(paramArrayOfChar, i, paramInt2, paramArrayOfByte, j, paramInt4);
        return (nextByteIndex() - paramInt3);
      }
      catch (MalformedInputException localMalformedInputException)
      {
        byte[] arrayOfByte = this.subBytes;
        int l = arrayOfByte.length;
        j = nextByteIndex();
        if (j + l > paramInt4)
          throw new sun.io.ConversionBufferFullException();
        for (int i1 = 0; i1 < l; ++i1)
          paramArrayOfByte[(j++)] = arrayOfByte[i1];
        i = nextCharIndex();
        i += this.badInputLength;
        this.badInputLength = 0;
        if (i >= paramInt2)
        {
          this.byteOff = j;
          return (this.byteOff - paramInt3);
        }
      }
      catch (UnknownCharacterException localUnknownCharacterException)
      {
        throw new Error("UnknownCharacterException thrown in substititution mode", localUnknownCharacterException);
      }
    return (nextByteIndex() - paramInt3);
  }

  public byte[] convertAll(char[] paramArrayOfChar)
    throws sun.io.MalformedInputException
  {
    reset();
    boolean bool = this.subMode;
    this.subMode = true;
    byte[] arrayOfByte1 = new byte[getMaxBytesPerChar() * paramArrayOfChar.length];
    try
    {
      int i = convert(paramArrayOfChar, 0, paramArrayOfChar.length, arrayOfByte1, 0, arrayOfByte1.length);
      i += flush(arrayOfByte1, nextByteIndex(), arrayOfByte1.length);
      byte[] arrayOfByte2 = new byte[i];
      System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, i);
      byte[] arrayOfByte3 = arrayOfByte2;
      return arrayOfByte3;
    }
    catch (ConversionBufferFullException localConversionBufferFullException)
    {
    }
    catch (UnknownCharacterException localUnknownCharacterException)
    {
    }
    finally
    {
      this.subMode = bool;
    }
  }

  public abstract int flush(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws sun.io.MalformedInputException, sun.io.ConversionBufferFullException;

  public int flushAny(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws sun.io.ConversionBufferFullException
  {
    int i;
    if (!(this.subMode))
      throw new IllegalStateException("Substitution mode is not on");
    try
    {
      return flush(paramArrayOfByte, paramInt1, paramInt2);
    }
    catch (MalformedInputException localMalformedInputException)
    {
      i = this.subBytes.length;
      byte[] arrayOfByte = this.subBytes;
      int j = paramInt1;
      if (paramInt1 + i > paramInt2)
        throw new sun.io.ConversionBufferFullException();
      for (int k = 0; k < i; ++k)
        paramArrayOfByte[(j++)] = arrayOfByte[k];
      this.byteOff = (this.charOff = 0);
      this.badInputLength = 0;
    }
    return i;
  }

  public abstract void reset();

  public boolean canConvert(char paramChar)
  {
    char[] arrayOfChar;
    try
    {
      arrayOfChar = new char[1];
      byte[] arrayOfByte = new byte[3];
      arrayOfChar[0] = paramChar;
      convert(arrayOfChar, 0, 1, arrayOfByte, 0, 3);
      return true;
    }
    catch (CharConversionException localCharConversionException)
    {
    }
    return false;
  }

  public abstract int getMaxBytesPerChar();

  public int getBadInputLength()
  {
    return this.badInputLength;
  }

  public int nextCharIndex()
  {
    return this.charOff;
  }

  public int nextByteIndex()
  {
    return this.byteOff;
  }

  public void setSubstitutionMode(boolean paramBoolean)
  {
    this.subMode = paramBoolean;
  }

  public void setSubstitutionBytes(byte[] paramArrayOfByte)
    throws IllegalArgumentException
  {
    if (paramArrayOfByte.length > getMaxBytesPerChar())
      throw new IllegalArgumentException();
    this.subBytes = new byte[paramArrayOfByte.length];
    System.arraycopy(paramArrayOfByte, 0, this.subBytes, 0, paramArrayOfByte.length);
  }

  public String toString()
  {
    return "CharToByteConverter: " + getCharacterEncoding();
  }
}
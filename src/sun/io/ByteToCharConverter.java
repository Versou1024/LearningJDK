package sun.io;

import java.io.UnsupportedEncodingException;

@Deprecated
public abstract class ByteToCharConverter
{
  protected boolean subMode = true;
  protected char[] subChars = { 65533 };
  protected int charOff;
  protected int byteOff;
  protected int badInputLength;

  public static ByteToCharConverter getDefault()
  {
    Object localObject = Converters.newDefaultConverter(0);
    return ((ByteToCharConverter)localObject);
  }

  public static ByteToCharConverter getConverter(String paramString)
    throws UnsupportedEncodingException
  {
    Object localObject = Converters.newConverter(0, paramString);
    return ((ByteToCharConverter)localObject);
  }

  public abstract String getCharacterEncoding();

  public abstract int convert(byte[] paramArrayOfByte, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3, int paramInt4)
    throws sun.io.MalformedInputException, sun.io.UnknownCharacterException, sun.io.ConversionBufferFullException;

  public char[] convertAll(byte[] paramArrayOfByte)
    throws sun.io.MalformedInputException
  {
    reset();
    boolean bool = this.subMode;
    this.subMode = true;
    char[] arrayOfChar1 = new char[getMaxCharsPerByte() * paramArrayOfByte.length];
    try
    {
      int i = convert(paramArrayOfByte, 0, paramArrayOfByte.length, arrayOfChar1, 0, arrayOfChar1.length);
      i += flush(arrayOfChar1, i, arrayOfChar1.length);
      char[] arrayOfChar2 = new char[i];
      System.arraycopy(arrayOfChar1, 0, arrayOfChar2, 0, i);
      char[] arrayOfChar3 = arrayOfChar2;
      return arrayOfChar3;
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

  public abstract int flush(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws sun.io.MalformedInputException, sun.io.ConversionBufferFullException;

  public abstract void reset();

  public int getMaxCharsPerByte()
  {
    return 1;
  }

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

  public void setSubstitutionChars(char[] paramArrayOfChar)
    throws IllegalArgumentException
  {
    if (paramArrayOfChar.length > getMaxCharsPerByte())
      throw new IllegalArgumentException();
    this.subChars = new char[paramArrayOfChar.length];
    System.arraycopy(paramArrayOfChar, 0, this.subChars, 0, paramArrayOfChar.length);
  }

  public String toString()
  {
    return "ByteToCharConverter: " + getCharacterEncoding();
  }
}
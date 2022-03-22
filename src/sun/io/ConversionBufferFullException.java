package sun.io;

import java.io.CharConversionException;

@Deprecated
public class ConversionBufferFullException extends CharConversionException
{
  public ConversionBufferFullException()
  {
  }

  public ConversionBufferFullException(String paramString)
  {
    super(paramString);
  }
}
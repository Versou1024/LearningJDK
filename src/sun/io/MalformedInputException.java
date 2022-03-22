package sun.io;

import java.io.CharConversionException;

@Deprecated
public class MalformedInputException extends CharConversionException
{
  public MalformedInputException()
  {
  }

  public MalformedInputException(String paramString)
  {
    super(paramString);
  }
}
package sun.io;

import java.io.CharConversionException;

@Deprecated
public class UnknownCharacterException extends CharConversionException
{
  public UnknownCharacterException()
  {
  }

  public UnknownCharacterException(String paramString)
  {
    super(paramString);
  }
}
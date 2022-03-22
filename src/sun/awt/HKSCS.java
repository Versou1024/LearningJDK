package sun.awt;

import java.nio.charset.Charset;

public class HKSCS extends sun.nio.cs.ext.HKSCS
{
  public boolean contains(Charset paramCharset)
  {
    return paramCharset instanceof HKSCS;
  }
}
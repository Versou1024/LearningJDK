package sun.net.www.content.text;

import java.io.FilterInputStream;
import java.io.InputStream;

public class PlainTextInputStream extends FilterInputStream
{
  PlainTextInputStream(InputStream paramInputStream)
  {
    super(paramInputStream);
  }
}
package sun.util;

import java.util.ListResourceBundle;

public class EmptyListResourceBundle extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return new Object[0][];
  }
}
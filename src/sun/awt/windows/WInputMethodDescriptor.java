package sun.awt.windows;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;

class WInputMethodDescriptor
  implements InputMethodDescriptor
{
  public Locale[] getAvailableLocales()
  {
    Locale[] arrayOfLocale1 = getAvailableLocalesInternal();
    Locale[] arrayOfLocale2 = new Locale[arrayOfLocale1.length];
    System.arraycopy(arrayOfLocale1, 0, arrayOfLocale2, 0, arrayOfLocale1.length);
    return arrayOfLocale2;
  }

  static Locale[] getAvailableLocalesInternal()
  {
    return getNativeAvailableLocales();
  }

  public boolean hasDynamicLocaleList()
  {
    return true;
  }

  public synchronized String getInputMethodDisplayName(Locale paramLocale1, Locale paramLocale2)
  {
    String str = "System Input Methods";
    if (Locale.getDefault().equals(paramLocale2))
      str = Toolkit.getProperty("AWT.HostInputMethodDisplayName", str);
    return str;
  }

  public Image getInputMethodIcon(Locale paramLocale)
  {
    return null;
  }

  public InputMethod createInputMethod()
    throws Exception
  {
    return new WInputMethod();
  }

  private static native Locale[] getNativeAvailableLocales();
}
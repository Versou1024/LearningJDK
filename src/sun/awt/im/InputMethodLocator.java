package sun.awt.im;

import java.awt.AWTException;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;

final class InputMethodLocator
{
  private InputMethodDescriptor descriptor;
  private ClassLoader loader;
  private Locale locale;

  InputMethodLocator(InputMethodDescriptor paramInputMethodDescriptor, ClassLoader paramClassLoader, Locale paramLocale)
  {
    if (paramInputMethodDescriptor == null)
      throw new NullPointerException("descriptor can't be null");
    this.descriptor = paramInputMethodDescriptor;
    this.loader = paramClassLoader;
    this.locale = paramLocale;
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject == this)
      return true;
    if ((paramObject == null) || (super.getClass() != paramObject.getClass()))
      return false;
    InputMethodLocator localInputMethodLocator = (InputMethodLocator)paramObject;
    if (!(this.descriptor.getClass().equals(localInputMethodLocator.descriptor.getClass())))
      return false;
    if (((this.loader == null) && (localInputMethodLocator.loader != null)) || ((this.loader != null) && (!(this.loader.equals(localInputMethodLocator.loader)))))
      return false;
    return ((((this.locale != null) || (localInputMethodLocator.locale == null))) && (((this.locale == null) || (this.locale.equals(localInputMethodLocator.locale)))));
  }

  public int hashCode()
  {
    int i = this.descriptor.hashCode();
    if (this.loader != null)
      i |= this.loader.hashCode() << 10;
    if (this.locale != null)
      i |= this.locale.hashCode() << 20;
    return i;
  }

  InputMethodDescriptor getDescriptor()
  {
    return this.descriptor;
  }

  ClassLoader getClassLoader()
  {
    return this.loader;
  }

  Locale getLocale()
  {
    return this.locale;
  }

  boolean isLocaleAvailable(Locale paramLocale)
  {
    Locale[] arrayOfLocale;
    try
    {
      arrayOfLocale = this.descriptor.getAvailableLocales();
      for (int i = 0; i < arrayOfLocale.length; ++i)
        if (arrayOfLocale[i].equals(paramLocale))
          return true;
    }
    catch (AWTException localAWTException)
    {
    }
    return false;
  }

  InputMethodLocator deriveLocator(Locale paramLocale)
  {
    if (paramLocale == this.locale)
      return this;
    return new InputMethodLocator(this.descriptor, this.loader, paramLocale);
  }

  boolean sameInputMethod(InputMethodLocator paramInputMethodLocator)
  {
    if (paramInputMethodLocator == this)
      return true;
    if (paramInputMethodLocator == null)
      return false;
    if (!(this.descriptor.getClass().equals(paramInputMethodLocator.descriptor.getClass())))
      return false;
    return ((((this.loader != null) || (paramInputMethodLocator.loader == null))) && (((this.loader == null) || (this.loader.equals(paramInputMethodLocator.loader)))));
  }

  String getActionCommandString()
  {
    String str = this.descriptor.getClass().getName();
    if (this.locale == null)
      return str;
    return str + "\n" + this.locale.toString();
  }
}
package sun.awt.im;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;
import javax.swing.JDialog;
import javax.swing.JFrame;

abstract class InputMethodPopupMenu
  implements ActionListener
{
  static InputMethodPopupMenu getInstance(Component paramComponent, String paramString)
  {
    if ((paramComponent instanceof JFrame) || (paramComponent instanceof JDialog))
      return new JInputMethodPopupMenu(paramString);
    return new AWTInputMethodPopupMenu(paramString);
  }

  abstract void show(Component paramComponent, int paramInt1, int paramInt2);

  abstract void removeAll();

  abstract void addSeparator();

  abstract void addToComponent(Component paramComponent);

  abstract Object createSubmenu(String paramString);

  abstract void add(Object paramObject);

  abstract void addMenuItem(String paramString1, String paramString2, String paramString3);

  abstract void addMenuItem(Object paramObject, String paramString1, String paramString2, String paramString3);

  void addOneInputMethodToMenu(InputMethodLocator paramInputMethodLocator, String paramString)
  {
    int i;
    InputMethodDescriptor localInputMethodDescriptor = paramInputMethodLocator.getDescriptor();
    String str1 = localInputMethodDescriptor.getInputMethodDisplayName(null, Locale.getDefault());
    String str2 = paramInputMethodLocator.getActionCommandString();
    Locale[] arrayOfLocale = null;
    try
    {
      arrayOfLocale = localInputMethodDescriptor.getAvailableLocales();
      i = arrayOfLocale.length;
    }
    catch (AWTException localAWTException)
    {
      i = 0;
    }
    if (i == 0)
    {
      addMenuItem(str1, null, paramString);
    }
    else if (i == 1)
    {
      if (localInputMethodDescriptor.hasDynamicLocaleList())
      {
        str1 = localInputMethodDescriptor.getInputMethodDisplayName(arrayOfLocale[0], Locale.getDefault());
        str2 = paramInputMethodLocator.deriveLocator(arrayOfLocale[0]).getActionCommandString();
      }
      addMenuItem(str1, str2, paramString);
    }
    else
    {
      Object localObject = createSubmenu(str1);
      add(localObject);
      for (int j = 0; j < i; ++j)
      {
        Locale localLocale = arrayOfLocale[j];
        String str3 = getLocaleName(localLocale);
        String str4 = paramInputMethodLocator.deriveLocator(localLocale).getActionCommandString();
        addMenuItem(localObject, str3, str4, paramString);
      }
    }
  }

  static boolean isSelected(String paramString1, String paramString2)
  {
    if ((paramString1 == null) || (paramString2 == null))
      return false;
    if (paramString1.equals(paramString2))
      return true;
    int i = paramString2.indexOf(10);
    return ((i != -1) && (paramString2.substring(0, i).equals(paramString1)));
  }

  String getLocaleName(Locale paramLocale)
  {
    String str1 = paramLocale.toString();
    String str2 = Toolkit.getProperty("AWT.InputMethodLanguage." + str1, null);
    if (str2 == null)
    {
      str2 = paramLocale.getDisplayName();
      if ((str2 == null) || (str2.length() == 0))
        str2 = str1;
    }
    return str2;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    String str = paramActionEvent.getActionCommand();
    ((ExecutableInputMethodManager)InputMethodManager.getInstance()).changeInputMethod(str);
  }
}
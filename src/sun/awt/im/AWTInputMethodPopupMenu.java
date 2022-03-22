package sun.awt.im;

import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;

class AWTInputMethodPopupMenu extends InputMethodPopupMenu
{
  static PopupMenu delegate = null;

  AWTInputMethodPopupMenu(String paramString)
  {
    synchronized (this)
    {
      if (delegate == null)
        delegate = new PopupMenu(paramString);
    }
  }

  void show(Component paramComponent, int paramInt1, int paramInt2)
  {
    delegate.show(paramComponent, paramInt1, paramInt2);
  }

  void removeAll()
  {
    delegate.removeAll();
  }

  void addSeparator()
  {
    delegate.addSeparator();
  }

  void addToComponent(Component paramComponent)
  {
    paramComponent.add(delegate);
  }

  Object createSubmenu(String paramString)
  {
    return new Menu(paramString);
  }

  void add(Object paramObject)
  {
    delegate.add((MenuItem)paramObject);
  }

  void addMenuItem(String paramString1, String paramString2, String paramString3)
  {
    addMenuItem(delegate, paramString1, paramString2, paramString3);
  }

  void addMenuItem(Object paramObject, String paramString1, String paramString2, String paramString3)
  {
    Object localObject;
    if (isSelected(paramString2, paramString3))
      localObject = new CheckboxMenuItem(paramString1, true);
    else
      localObject = new MenuItem(paramString1);
    ((MenuItem)localObject).setActionCommand(paramString2);
    ((MenuItem)localObject).addActionListener(this);
    ((MenuItem)localObject).setEnabled(paramString2 != null);
    ((Menu)paramObject).add((MenuItem)localObject);
  }
}
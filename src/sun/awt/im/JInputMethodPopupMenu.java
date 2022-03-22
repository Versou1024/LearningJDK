package sun.awt.im;

import java.awt.Component;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

class JInputMethodPopupMenu extends InputMethodPopupMenu
{
  static JPopupMenu delegate = null;

  JInputMethodPopupMenu(String paramString)
  {
    synchronized (this)
    {
      if (delegate == null)
        delegate = new JPopupMenu(paramString);
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
  }

  Object createSubmenu(String paramString)
  {
    return new JMenu(paramString);
  }

  void add(Object paramObject)
  {
    delegate.add((JMenuItem)paramObject);
  }

  void addMenuItem(String paramString1, String paramString2, String paramString3)
  {
    addMenuItem(delegate, paramString1, paramString2, paramString3);
  }

  void addMenuItem(Object paramObject, String paramString1, String paramString2, String paramString3)
  {
    Object localObject;
    if (isSelected(paramString2, paramString3))
      localObject = new JCheckBoxMenuItem(paramString1, true);
    else
      localObject = new JMenuItem(paramString1);
    ((JMenuItem)localObject).setActionCommand(paramString2);
    ((JMenuItem)localObject).addActionListener(this);
    ((JMenuItem)localObject).setEnabled(paramString2 != null);
    if (paramObject instanceof JMenu)
      ((JMenu)paramObject).add((JMenuItem)localObject);
    else
      ((JPopupMenu)paramObject).add((JMenuItem)localObject);
  }
}
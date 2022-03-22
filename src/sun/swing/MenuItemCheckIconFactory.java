package sun.swing;

import javax.swing.Icon;
import javax.swing.JMenuItem;

public abstract interface MenuItemCheckIconFactory
{
  public abstract Icon getIcon(JMenuItem paramJMenuItem);

  public abstract boolean isCompatible(Object paramObject, String paramString);
}
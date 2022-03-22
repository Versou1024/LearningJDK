package sun.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class VerticalBagLayout
  implements LayoutManager
{
  int vgap;

  public VerticalBagLayout()
  {
    this(0);
  }

  public VerticalBagLayout(int paramInt)
  {
    this.vgap = paramInt;
  }

  public void addLayoutComponent(String paramString, Component paramComponent)
  {
  }

  public void removeLayoutComponent(Component paramComponent)
  {
  }

  public Dimension minimumLayoutSize(Container paramContainer)
  {
    Dimension localDimension1 = new Dimension();
    int i = paramContainer.countComponents();
    for (int j = 0; j < i; ++j)
    {
      Component localComponent = paramContainer.getComponent(j);
      if (localComponent.isVisible())
      {
        Dimension localDimension2 = localComponent.minimumSize();
        localDimension1.width = Math.max(localDimension2.width, localDimension1.width);
        localDimension1.height += localDimension2.height + this.vgap;
      }
    }
    Insets localInsets = paramContainer.insets();
    localDimension1.width += localInsets.left + localInsets.right;
    localDimension1.height += localInsets.top + localInsets.bottom;
    return localDimension1;
  }

  public Dimension preferredLayoutSize(Container paramContainer)
  {
    Dimension localDimension1 = new Dimension();
    int i = paramContainer.countComponents();
    for (int j = 0; j < i; ++j)
    {
      Component localComponent = paramContainer.getComponent(j);
      Dimension localDimension2 = localComponent.preferredSize();
      localDimension1.width = Math.max(localDimension2.width, localDimension1.width);
      localDimension1.height += localDimension2.height + this.vgap;
    }
    Insets localInsets = paramContainer.insets();
    localDimension1.width += localInsets.left + localInsets.right;
    localDimension1.height += localInsets.top + localInsets.bottom;
    return localDimension1;
  }

  public void layoutContainer(Container paramContainer)
  {
    Insets localInsets = paramContainer.insets();
    int i = localInsets.top;
    int j = paramContainer.size().height - localInsets.bottom;
    int k = localInsets.left;
    int l = paramContainer.size().width - localInsets.right;
    int i1 = paramContainer.countComponents();
    for (int i2 = 0; i2 < i1; ++i2)
    {
      Component localComponent = paramContainer.getComponent(i2);
      if (localComponent.isVisible())
      {
        int i3 = localComponent.size().height;
        localComponent.resize(l - k, i3);
        Dimension localDimension = localComponent.preferredSize();
        localComponent.reshape(k, i, l - k, localDimension.height);
        i += localDimension.height + this.vgap;
      }
    }
  }

  public String toString()
  {
    return super.getClass().getName() + "[vgap=" + this.vgap + "]";
  }
}
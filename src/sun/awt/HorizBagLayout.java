package sun.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class HorizBagLayout
  implements LayoutManager
{
  int hgap;

  public HorizBagLayout()
  {
    this(0);
  }

  public HorizBagLayout(int paramInt)
  {
    this.hgap = paramInt;
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
    for (int i = 0; i < paramContainer.countComponents(); ++i)
    {
      Component localComponent = paramContainer.getComponent(i);
      if (localComponent.isVisible())
      {
        Dimension localDimension2 = localComponent.minimumSize();
        localDimension1.width += localDimension2.width + this.hgap;
        localDimension1.height = Math.max(localDimension2.height, localDimension1.height);
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
    for (int i = 0; i < paramContainer.countComponents(); ++i)
    {
      Component localComponent = paramContainer.getComponent(i);
      if (localComponent.isVisible())
      {
        Dimension localDimension2 = localComponent.preferredSize();
        localDimension1.width += localDimension2.width + this.hgap;
        localDimension1.height = Math.max(localDimension1.height, localDimension2.height);
      }
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
    for (int i1 = 0; i1 < paramContainer.countComponents(); ++i1)
    {
      Component localComponent = paramContainer.getComponent(i1);
      if (localComponent.isVisible())
      {
        int i2 = localComponent.size().width;
        localComponent.resize(i2, j - i);
        Dimension localDimension = localComponent.preferredSize();
        localComponent.reshape(k, i, localDimension.width, j - i);
        k += localDimension.width + this.hgap;
      }
    }
  }

  public String toString()
  {
    return super.getClass().getName() + "[hgap=" + this.hgap + "]";
  }
}
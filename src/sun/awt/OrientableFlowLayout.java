package sun.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

public class OrientableFlowLayout extends FlowLayout
{
  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  public static final int TOP = 0;
  public static final int BOTTOM = 2;
  int orientation;
  int vAlign;
  int vHGap;
  int vVGap;

  public OrientableFlowLayout()
  {
    this(0, 1, 1, 5, 5, 5, 5);
  }

  public OrientableFlowLayout(int paramInt)
  {
    this(paramInt, 1, 1, 5, 5, 5, 5);
  }

  public OrientableFlowLayout(int paramInt1, int paramInt2, int paramInt3)
  {
    this(paramInt1, paramInt2, paramInt3, 5, 5, 5, 5);
  }

  public OrientableFlowLayout(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
  {
    super(paramInt2, paramInt4, paramInt5);
    this.orientation = paramInt1;
    this.vAlign = paramInt3;
    this.vHGap = paramInt6;
    this.vVGap = paramInt7;
  }

  public synchronized void orientHorizontally()
  {
    this.orientation = 0;
  }

  public synchronized void orientVertically()
  {
    this.orientation = 1;
  }

  public Dimension preferredLayoutSize(Container paramContainer)
  {
    if (this.orientation == 0)
      return super.preferredLayoutSize(paramContainer);
    Dimension localDimension1 = new Dimension(0, 0);
    int i = paramContainer.countComponents();
    for (int j = 0; j < i; ++j)
    {
      Component localComponent = paramContainer.getComponent(j);
      if (localComponent.isVisible())
      {
        Dimension localDimension2 = localComponent.preferredSize();
        localDimension1.width = Math.max(localDimension1.width, localDimension2.width);
        if (j > 0)
          localDimension1.height += this.vVGap;
        localDimension1.height += localDimension2.height;
      }
    }
    Insets localInsets = paramContainer.insets();
    localDimension1.width += localInsets.left + localInsets.right + this.vHGap * 2;
    localDimension1.height += localInsets.top + localInsets.bottom + this.vVGap * 2;
    return localDimension1;
  }

  public Dimension minimumLayoutSize(Container paramContainer)
  {
    if (this.orientation == 0)
      return super.minimumLayoutSize(paramContainer);
    Dimension localDimension1 = new Dimension(0, 0);
    int i = paramContainer.countComponents();
    for (int j = 0; j < i; ++j)
    {
      Component localComponent = paramContainer.getComponent(j);
      if (localComponent.isVisible())
      {
        Dimension localDimension2 = localComponent.minimumSize();
        localDimension1.width = Math.max(localDimension1.width, localDimension2.width);
        if (j > 0)
          localDimension1.height += this.vVGap;
        localDimension1.height += localDimension2.height;
      }
    }
    Insets localInsets = paramContainer.insets();
    localDimension1.width += localInsets.left + localInsets.right + this.vHGap * 2;
    localDimension1.height += localInsets.top + localInsets.bottom + this.vVGap * 2;
    return localDimension1;
  }

  public void layoutContainer(Container paramContainer)
  {
    if (this.orientation == 0)
    {
      super.layoutContainer(paramContainer);
    }
    else
    {
      Insets localInsets = paramContainer.insets();
      Dimension localDimension1 = paramContainer.size();
      int i = localDimension1.height - localInsets.top + localInsets.bottom + this.vVGap * 2;
      int j = localInsets.left + this.vHGap;
      int k = 0;
      int l = 0;
      int i1 = 0;
      int i2 = paramContainer.countComponents();
      for (int i3 = 0; i3 < i2; ++i3)
      {
        Component localComponent = paramContainer.getComponent(i3);
        if (localComponent.isVisible())
        {
          Dimension localDimension2 = localComponent.preferredSize();
          localComponent.resize(localDimension2.width, localDimension2.height);
          if ((k == 0) || (k + localDimension2.height <= i))
          {
            if (k > 0)
              k += this.vVGap;
            k += localDimension2.height;
            l = Math.max(l, localDimension2.width);
          }
          else
          {
            moveComponents(paramContainer, j, localInsets.top + this.vVGap, l, i - k, i1, i3);
            j += this.vHGap + l;
            k = localDimension2.width;
            l = localDimension2.width;
            i1 = i3;
          }
        }
      }
      moveComponents(paramContainer, j, localInsets.top + this.vVGap, l, i - k, i1, i2);
    }
  }

  private void moveComponents(Container paramContainer, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    switch (this.vAlign)
    {
    case 0:
      break;
    case 1:
      paramInt2 += paramInt4 / 2;
      break;
    case 2:
      paramInt2 += paramInt4;
    }
    for (int i = paramInt5; i < paramInt6; ++i)
    {
      Component localComponent = paramContainer.getComponent(i);
      Dimension localDimension = localComponent.size();
      if (localComponent.isVisible())
      {
        localComponent.move(paramInt1 + (paramInt3 - localDimension.width) / 2, paramInt2);
        paramInt2 += this.vVGap + localDimension.height;
      }
    }
  }

  public String toString()
  {
    String str = "";
    switch (this.orientation)
    {
    case 0:
      str = "orientation=horizontal, ";
      break;
    case 1:
      str = "orientation=vertical, ";
    }
    return getClass().getName() + "[" + str + super.toString() + "]";
  }
}
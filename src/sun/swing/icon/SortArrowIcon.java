package sun.swing.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

public class SortArrowIcon
  implements Icon, UIResource, Serializable
{
  private static final int ARROW_HEIGHT = 5;
  private static final int X_PADDING = 7;
  private boolean ascending;
  private Color color;
  private String colorKey;

  public SortArrowIcon(boolean paramBoolean, Color paramColor)
  {
    this.ascending = paramBoolean;
    this.color = paramColor;
    if (paramColor == null)
      throw new IllegalArgumentException();
  }

  public SortArrowIcon(boolean paramBoolean, String paramString)
  {
    this.ascending = paramBoolean;
    this.colorKey = paramString;
    if (paramString == null)
      throw new IllegalArgumentException();
  }

  public void paintIcon(Component paramComponent, Graphics paramGraphics, int paramInt1, int paramInt2)
  {
    int j;
    int k;
    paramGraphics.setColor(getColor());
    int i = 7 + paramInt1 + 2;
    if (this.ascending)
    {
      j = paramInt2;
      paramGraphics.fillRect(i, j, 1, 1);
      for (k = 1; k < 5; ++k)
        paramGraphics.fillRect(i - k, j + k, k + k + 1, 1);
    }
    else
    {
      j = paramInt2 + 5 - 1;
      paramGraphics.fillRect(i, j, 1, 1);
      for (k = 1; k < 5; ++k)
        paramGraphics.fillRect(i - k, j - k, k + k + 1, 1);
    }
  }

  public int getIconWidth()
  {
    return 17;
  }

  public int getIconHeight()
  {
    return 7;
  }

  private Color getColor()
  {
    if (this.color != null)
      return this.color;
    return UIManager.getColor(this.colorKey);
  }
}
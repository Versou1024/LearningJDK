package sun.swing.plaf.synth;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.plaf.synth.SynthContext;

public abstract class SynthIcon
  implements Icon
{
  public static int getIconWidth(Icon paramIcon, SynthContext paramSynthContext)
  {
    if (paramIcon == null)
      return 0;
    if (paramIcon instanceof SynthIcon)
      return ((SynthIcon)paramIcon).getIconWidth(paramSynthContext);
    return paramIcon.getIconWidth();
  }

  public static int getIconHeight(Icon paramIcon, SynthContext paramSynthContext)
  {
    if (paramIcon == null)
      return 0;
    if (paramIcon instanceof SynthIcon)
      return ((SynthIcon)paramIcon).getIconHeight(paramSynthContext);
    return paramIcon.getIconHeight();
  }

  public static void paintIcon(Icon paramIcon, SynthContext paramSynthContext, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (paramIcon instanceof SynthIcon)
      ((SynthIcon)paramIcon).paintIcon(paramSynthContext, paramGraphics, paramInt1, paramInt2, paramInt3, paramInt4);
    else if (paramIcon != null)
      paramIcon.paintIcon(paramSynthContext.getComponent(), paramGraphics, paramInt1, paramInt2);
  }

  public abstract void paintIcon(SynthContext paramSynthContext, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public abstract int getIconWidth(SynthContext paramSynthContext);

  public abstract int getIconHeight(SynthContext paramSynthContext);

  public void paintIcon(Component paramComponent, Graphics paramGraphics, int paramInt1, int paramInt2)
  {
    paintIcon(null, paramGraphics, paramInt1, paramInt2, 0, 0);
  }

  public int getIconWidth()
  {
    return getIconWidth(null);
  }

  public int getIconHeight()
  {
    return getIconHeight(null);
  }
}
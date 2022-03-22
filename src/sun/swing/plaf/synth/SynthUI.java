package sun.swing.plaf.synth;

import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;

public abstract interface SynthUI extends SynthConstants
{
  public abstract SynthContext getContext(JComponent paramJComponent);

  public abstract void paintBorder(SynthContext paramSynthContext, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4);
}
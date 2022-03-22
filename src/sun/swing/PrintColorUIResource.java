package sun.swing;

import java.awt.Color;
import javax.swing.plaf.ColorUIResource;

public class PrintColorUIResource extends ColorUIResource
{
  private Color printColor;

  public PrintColorUIResource(int paramInt, Color paramColor)
  {
    super(paramInt);
    this.printColor = paramColor;
  }

  public Color getPrintColor()
  {
    return ((this.printColor != null) ? this.printColor : this);
  }

  private Object writeReplace()
  {
    return new ColorUIResource(this);
  }
}
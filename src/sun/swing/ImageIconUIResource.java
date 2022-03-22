package sun.swing;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.plaf.UIResource;

public class ImageIconUIResource extends ImageIcon
  implements UIResource
{
  public ImageIconUIResource(byte[] paramArrayOfByte)
  {
    super(paramArrayOfByte);
  }

  public ImageIconUIResource(Image paramImage)
  {
    super(paramImage);
  }
}
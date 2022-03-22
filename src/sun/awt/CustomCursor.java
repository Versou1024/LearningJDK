package sun.awt;

import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;

public abstract class CustomCursor extends Cursor
{
  protected Image image;

  public CustomCursor(Image paramImage, Point paramPoint, String paramString)
    throws IndexOutOfBoundsException
  {
    super(paramString);
    this.image = paramImage;
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    Canvas localCanvas = new Canvas();
    MediaTracker localMediaTracker = new MediaTracker(localCanvas);
    localMediaTracker.addImage(paramImage, 0);
    try
    {
      localMediaTracker.waitForAll();
    }
    catch (InterruptedException localInterruptedException1)
    {
    }
    int i = paramImage.getWidth(localCanvas);
    int j = paramImage.getHeight(localCanvas);
    if ((localMediaTracker.isErrorAny()) || (i < 0) || (j < 0))
      paramPoint.x = (paramPoint.y = 0);
    Dimension localDimension = localToolkit.getBestCursorSize(i, j);
    if ((localDimension.width != i) || (localDimension.height != j))
    {
      paramImage = paramImage.getScaledInstance(localDimension.width, localDimension.height, 1);
      i = localDimension.width;
      j = localDimension.height;
    }
    if ((paramPoint.x >= i) || (paramPoint.y >= j) || (paramPoint.x < 0) || (paramPoint.y < 0))
      throw new IndexOutOfBoundsException("invalid hotSpot");
    int[] arrayOfInt = new int[i * j];
    ImageProducer localImageProducer = paramImage.getSource();
    PixelGrabber localPixelGrabber = new PixelGrabber(localImageProducer, 0, 0, i, j, arrayOfInt, 0, i);
    try
    {
      localPixelGrabber.grabPixels();
    }
    catch (InterruptedException localInterruptedException2)
    {
    }
    createNativeCursor(this.image, arrayOfInt, i, j, paramPoint.x, paramPoint.y);
  }

  protected abstract void createNativeCursor(Image paramImage, int[] paramArrayOfInt, int paramInt1, int paramInt2, int paramInt3, int paramInt4);
}
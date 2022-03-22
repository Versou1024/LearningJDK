package sun.awt;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

public class RepaintArea
{
  private static final int MAX_BENEFIT_RATIO = 4;
  private static final int HORIZONTAL = 0;
  private static final int VERTICAL = 1;
  private static final int UPDATE = 2;
  private static final int RECT_COUNT = 3;
  private Rectangle[] paintRects = new Rectangle[3];

  public RepaintArea()
  {
  }

  private RepaintArea(RepaintArea paramRepaintArea)
  {
    for (int i = 0; i < 3; ++i)
      this.paintRects[i] = paramRepaintArea.paintRects[i];
  }

  public synchronized void add(Rectangle paramRectangle, int paramInt)
  {
    if (paramRectangle.isEmpty())
      return;
    int i = 2;
    if (paramInt == 800)
      i = (paramRectangle.width > paramRectangle.height) ? 0 : 1;
    if (this.paintRects[i] != null)
      this.paintRects[i].add(paramRectangle);
    else
      this.paintRects[i] = new Rectangle(paramRectangle);
  }

  private synchronized RepaintArea cloneAndReset()
  {
    RepaintArea localRepaintArea = new RepaintArea(this);
    for (int i = 0; i < 3; ++i)
      this.paintRects[i] = null;
    return localRepaintArea;
  }

  public boolean isEmpty()
  {
    for (int i = 0; i < 3; ++i)
      if (this.paintRects[i] != null)
        return false;
    return true;
  }

  public synchronized void constrain(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    for (int i = 0; i < 3; ++i)
    {
      Rectangle localRectangle = this.paintRects[i];
      if (localRectangle != null)
      {
        if (localRectangle.x < paramInt1)
        {
          localRectangle.width -= paramInt1 - localRectangle.x;
          localRectangle.x = paramInt1;
        }
        if (localRectangle.y < paramInt2)
        {
          localRectangle.height -= paramInt2 - localRectangle.y;
          localRectangle.y = paramInt2;
        }
        int j = localRectangle.x + localRectangle.width - paramInt1 - paramInt3;
        if (j > 0)
          localRectangle.width -= j;
        int k = localRectangle.y + localRectangle.height - paramInt2 - paramInt4;
        if (k > 0)
          localRectangle.height -= k;
        if ((localRectangle.width <= 0) || (localRectangle.height <= 0))
          this.paintRects[i] = null;
      }
    }
  }

  public synchronized void subtract(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Rectangle localRectangle = new Rectangle(paramInt1, paramInt2, paramInt3, paramInt4);
    for (int i = 0; i < 3; ++i)
      if ((subtract(this.paintRects[i], localRectangle)) && (this.paintRects[i] != null) && (this.paintRects[i].isEmpty()))
        this.paintRects[i] = null;
  }

  public void paint(Object paramObject, boolean paramBoolean)
  {
    Component localComponent = (Component)paramObject;
    if (isEmpty())
      return;
    if (!(localComponent.isVisible()))
      return;
    RepaintArea localRepaintArea = cloneAndReset();
    if (!(subtract(localRepaintArea.paintRects[1], localRepaintArea.paintRects[0])))
      subtract(localRepaintArea.paintRects[0], localRepaintArea.paintRects[1]);
    if ((localRepaintArea.paintRects[0] != null) && (localRepaintArea.paintRects[1] != null))
    {
      Rectangle localRectangle = localRepaintArea.paintRects[0].union(localRepaintArea.paintRects[1]);
      int j = localRectangle.width * localRectangle.height;
      int k = j - localRepaintArea.paintRects[0].width * localRepaintArea.paintRects[0].height - localRepaintArea.paintRects[1].width * localRepaintArea.paintRects[1].height;
      if (4 * k < j)
      {
        localRepaintArea.paintRects[0] = localRectangle;
        localRepaintArea.paintRects[1] = null;
      }
    }
    for (int i = 0; i < this.paintRects.length; ++i)
      if ((localRepaintArea.paintRects[i] != null) && (!(localRepaintArea.paintRects[i].isEmpty())))
      {
        Graphics localGraphics = localComponent.getGraphics();
        if (localGraphics != null)
          try
          {
            localGraphics.setClip(localRepaintArea.paintRects[i]);
            if (i == 2)
            {
              updateComponent(localComponent, localGraphics);
            }
            else
            {
              if (paramBoolean)
                localGraphics.clearRect(localRepaintArea.paintRects[i].x, localRepaintArea.paintRects[i].y, localRepaintArea.paintRects[i].width, localRepaintArea.paintRects[i].height);
              paintComponent(localComponent, localGraphics);
            }
          }
          finally
          {
            localGraphics.dispose();
          }
      }
  }

  protected void updateComponent(Component paramComponent, Graphics paramGraphics)
  {
    if (paramComponent != null)
      paramComponent.update(paramGraphics);
  }

  protected void paintComponent(Component paramComponent, Graphics paramGraphics)
  {
    if (paramComponent != null)
      paramComponent.paint(paramGraphics);
  }

  static boolean subtract(Rectangle paramRectangle1, Rectangle paramRectangle2)
  {
    if ((paramRectangle1 == null) || (paramRectangle2 == null))
      return true;
    Rectangle localRectangle = paramRectangle1.intersection(paramRectangle2);
    if (localRectangle.isEmpty())
      return true;
    if ((paramRectangle1.x == localRectangle.x) && (paramRectangle1.y == localRectangle.y))
    {
      if (paramRectangle1.width == localRectangle.width)
      {
        paramRectangle1.y += localRectangle.height;
        paramRectangle1.height -= localRectangle.height;
        return true;
      }
      if (paramRectangle1.height != localRectangle.height)
        break label219;
      paramRectangle1.x += localRectangle.width;
      paramRectangle1.width -= localRectangle.width;
      return true;
    }
    if ((paramRectangle1.x + paramRectangle1.width == localRectangle.x + localRectangle.width) && (paramRectangle1.y + paramRectangle1.height == localRectangle.y + localRectangle.height))
    {
      if (paramRectangle1.width == localRectangle.width)
      {
        paramRectangle1.height -= localRectangle.height;
        return true;
      }
      if (paramRectangle1.height == localRectangle.height)
      {
        paramRectangle1.width -= localRectangle.width;
        return true;
      }
    }
    label219: return false;
  }

  public String toString()
  {
    return super.toString() + "[ horizontal=" + this.paintRects[0] + " vertical=" + this.paintRects[1] + " update=" + this.paintRects[2] + "]";
  }
}
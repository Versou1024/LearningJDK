package sun.awt;

import java.awt.Adjustable;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.MouseWheelEvent;

public abstract class ScrollPaneWheelScroller
{
  private static final DebugHelper dbg = DebugHelper.create(ScrollPaneWheelScroller.class);

  public static void handleWheelScrolling(ScrollPane paramScrollPane, MouseWheelEvent paramMouseWheelEvent)
  {
    int i = 0;
    if ((paramScrollPane != null) && (paramMouseWheelEvent.getScrollAmount() != 0))
    {
      Adjustable localAdjustable = getAdjustableToScroll(paramScrollPane);
      if (localAdjustable != null)
      {
        i = getIncrementFromAdjustable(localAdjustable, paramMouseWheelEvent);
        scrollAdjustable(localAdjustable, i);
      }
    }
  }

  public static Adjustable getAdjustableToScroll(ScrollPane paramScrollPane)
  {
    int i = paramScrollPane.getScrollbarDisplayPolicy();
    if ((i == 1) || (i == 2))
      return paramScrollPane.getVAdjustable();
    Insets localInsets = paramScrollPane.getInsets();
    int j = paramScrollPane.getVScrollbarWidth();
    if (localInsets.right >= j)
      return paramScrollPane.getVAdjustable();
    int k = paramScrollPane.getHScrollbarHeight();
    if (localInsets.bottom >= k)
      return paramScrollPane.getHAdjustable();
    return null;
  }

  public static int getIncrementFromAdjustable(Adjustable paramAdjustable, MouseWheelEvent paramMouseWheelEvent)
  {
    int i = 0;
    if (paramMouseWheelEvent.getScrollType() == 0)
      i = paramMouseWheelEvent.getUnitsToScroll() * paramAdjustable.getUnitIncrement();
    else if (paramMouseWheelEvent.getScrollType() == 1)
      i = paramAdjustable.getBlockIncrement() * paramMouseWheelEvent.getWheelRotation();
    return i;
  }

  public static void scrollAdjustable(Adjustable paramAdjustable, int paramInt)
  {
    int i = paramAdjustable.getValue();
    int j = paramAdjustable.getMaximum() - paramAdjustable.getVisibleAmount();
    if ((paramInt > 0) && (i < j))
    {
      if (i + paramInt < j)
      {
        paramAdjustable.setValue(i + paramInt);
        return;
      }
      paramAdjustable.setValue(j);
      return;
    }
    if ((paramInt < 0) && (i > paramAdjustable.getMinimum()))
    {
      if (i + paramInt > paramAdjustable.getMinimum())
      {
        paramAdjustable.setValue(i + paramInt);
        return;
      }
      paramAdjustable.setValue(paramAdjustable.getMinimum());
      return;
    }
  }
}
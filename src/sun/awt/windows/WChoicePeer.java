package sun.awt.windows;

import java.awt.Choice;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ItemEvent;
import java.awt.peer.ChoicePeer;

class WChoicePeer extends WComponentPeer
  implements ChoicePeer
{
  public Dimension getMinimumSize()
  {
    FontMetrics localFontMetrics = getFontMetrics(((Choice)this.target).getFont());
    Choice localChoice = (Choice)this.target;
    int i = 0;
    int j = localChoice.getItemCount();
    while (j-- > 0)
      i = Math.max(localFontMetrics.stringWidth(localChoice.getItem(j)), i);
    return new Dimension(28 + i, Math.max(localFontMetrics.getHeight() + 6, 15));
  }

  public boolean isFocusable()
  {
    return true;
  }

  public native void select(int paramInt);

  public void add(String paramString, int paramInt)
  {
    addItem(paramString, paramInt);
  }

  public boolean shouldClearRectBeforePaint()
  {
    return false;
  }

  public native void removeAll();

  public native void remove(int paramInt);

  public void addItem(String paramString, int paramInt)
  {
    addItems(new String[] { paramString }, paramInt);
  }

  public native void addItems(String[] paramArrayOfString, int paramInt);

  public native void reshape(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  WChoicePeer(Choice paramChoice)
  {
    super(paramChoice);
  }

  native void create(WComponentPeer paramWComponentPeer);

  void initialize()
  {
    Choice localChoice = (Choice)this.target;
    int i = localChoice.getItemCount();
    if (i > 0)
    {
      String[] arrayOfString = new String[i];
      for (int j = 0; j < i; ++j)
        arrayOfString[j] = localChoice.getItem(j);
      addItems(arrayOfString, 0);
      if (localChoice.getSelectedIndex() >= 0)
        select(localChoice.getSelectedIndex());
    }
    super.initialize();
  }

  void handleAction(int paramInt)
  {
    Choice localChoice = (Choice)this.target;
    WToolkit.executeOnEventHandlerThread(localChoice, new Runnable(this, localChoice, paramInt)
    {
      public void run()
      {
        this.val$c.select(this.val$index);
        this.this$0.postEvent(new ItemEvent(this.val$c, 701, this.val$c.getItem(this.val$index), 1));
      }
    });
  }

  int getDropDownHeight()
  {
    Choice localChoice = (Choice)this.target;
    FontMetrics localFontMetrics = getFontMetrics(localChoice.getFont());
    int i = Math.min(localChoice.getItemCount(), 8);
    return (localFontMetrics.getHeight() * i);
  }

  public Dimension minimumSize()
  {
    return getMinimumSize();
  }
}
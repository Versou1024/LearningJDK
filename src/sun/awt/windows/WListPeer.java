package sun.awt.windows;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.peer.ListPeer;

class WListPeer extends WComponentPeer
  implements ListPeer
{
  private FontMetrics fm;

  public Dimension minimumSize()
  {
    return minimumSize(4);
  }

  public boolean isFocusable()
  {
    return true;
  }

  public int[] getSelectedIndexes()
  {
    List localList = (List)this.target;
    int i = localList.countItems();
    int[] arrayOfInt1 = new int[i];
    int j = 0;
    for (int k = 0; k < i; ++k)
      if (isSelected(k))
        arrayOfInt1[(j++)] = k;
    int[] arrayOfInt2 = new int[j];
    System.arraycopy(arrayOfInt1, 0, arrayOfInt2, 0, j);
    return arrayOfInt2;
  }

  public void add(String paramString, int paramInt)
  {
    addItem(paramString, paramInt);
  }

  public void removeAll()
  {
    clear();
  }

  public void setMultipleMode(boolean paramBoolean)
  {
    setMultipleSelections(paramBoolean);
  }

  public Dimension getPreferredSize(int paramInt)
  {
    return preferredSize(paramInt);
  }

  public Dimension getMinimumSize(int paramInt)
  {
    return minimumSize(paramInt);
  }

  public void addItem(String paramString, int paramInt)
  {
    addItems(new String[] { paramString }, paramInt, this.fm.stringWidth(paramString));
  }

  native void addItems(String[] paramArrayOfString, int paramInt1, int paramInt2);

  public native void delItems(int paramInt1, int paramInt2);

  public void clear()
  {
    List localList = (List)this.target;
    delItems(0, localList.countItems());
  }

  public native void select(int paramInt);

  public native void deselect(int paramInt);

  public native void makeVisible(int paramInt);

  public native void setMultipleSelections(boolean paramBoolean);

  public native int getMaxWidth();

  public Dimension preferredSize(int paramInt)
  {
    if (this.fm == null)
    {
      localObject = (List)this.target;
      this.fm = getFontMetrics(((List)localObject).getFont());
    }
    Object localObject = minimumSize(paramInt);
    ((Dimension)localObject).width = Math.max(((Dimension)localObject).width, getMaxWidth() + 20);
    return ((Dimension)localObject);
  }

  public Dimension minimumSize(int paramInt)
  {
    return new Dimension(20 + this.fm.stringWidth("0123456789abcde"), this.fm.getHeight() * paramInt + 4);
  }

  WListPeer(List paramList)
  {
    super(paramList);
  }

  native void create(WComponentPeer paramWComponentPeer);

  void initialize()
  {
    List localList = (List)this.target;
    this.fm = getFontMetrics(localList.getFont());
    Font localFont = localList.getFont();
    if (localFont != null)
      setFont(localFont);
    int i = localList.countItems();
    if (i > 0)
    {
      localObject = new String[i];
      j = 0;
      int k = 0;
      for (int l = 0; l < i; ++l)
      {
        localObject[l] = localList.getItem(l);
        k = this.fm.stringWidth(localObject[l]);
        if (k > j)
          j = k;
      }
      addItems(localObject, 0, j);
    }
    setMultipleSelections(localList.allowsMultipleSelections());
    Object localObject = localList.getSelectedIndexes();
    for (int j = 0; j < localObject.length; ++j)
      select(localObject[j]);
    j = localList.getVisibleIndex();
    if ((j < 0) && (localObject.length > 0))
      j = localObject[0];
    if (j >= 0)
      makeVisible(j);
    super.initialize();
  }

  public boolean shouldClearRectBeforePaint()
  {
    return false;
  }

  private native void updateMaxItemWidth();

  native boolean isSelected(int paramInt);

  public synchronized void _setFont(Font paramFont)
  {
    super._setFont(paramFont);
    this.fm = getFontMetrics(((List)this.target).getFont());
    updateMaxItemWidth();
  }

  void handleAction(int paramInt1, long paramLong, int paramInt2)
  {
    List localList = (List)this.target;
    WToolkit.executeOnEventHandlerThread(localList, new Runnable(this, localList, paramInt1, paramLong, paramInt2)
    {
      public void run()
      {
        this.val$l.select(this.val$index);
        this.this$0.postEvent(new ActionEvent(this.this$0.target, 1001, this.val$l.getItem(this.val$index), this.val$when, this.val$modifiers));
      }
    });
  }

  void handleListChanged(int paramInt)
  {
    List localList = (List)this.target;
    WToolkit.executeOnEventHandlerThread(localList, new Runnable(this, localList, paramInt)
    {
      public void run()
      {
        this.this$0.postEvent(new ItemEvent(this.val$l, 701, Integer.valueOf(this.val$index), 2));
      }
    });
  }
}
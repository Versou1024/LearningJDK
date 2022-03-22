package sun.awt.windows;

import java.awt.Rectangle;
import java.awt.TextComponent;
import java.awt.event.TextEvent;
import java.awt.peer.TextComponentPeer;

abstract class WTextComponentPeer extends WComponentPeer
  implements TextComponentPeer
{
  public void setEditable(boolean paramBoolean)
  {
    enableEditing(paramBoolean);
    setBackground(((TextComponent)this.target).getBackground());
  }

  public native String getText();

  public native void setText(String paramString);

  public native int getSelectionStart();

  public native int getSelectionEnd();

  public native void select(int paramInt1, int paramInt2);

  WTextComponentPeer(TextComponent paramTextComponent)
  {
    super(paramTextComponent);
  }

  void initialize()
  {
    TextComponent localTextComponent = (TextComponent)this.target;
    String str = localTextComponent.getText();
    if (str != null)
      setText(str);
    select(localTextComponent.getSelectionStart(), localTextComponent.getSelectionEnd());
    setEditable(localTextComponent.isEditable());
    super.initialize();
  }

  native void enableEditing(boolean paramBoolean);

  public boolean isFocusable()
  {
    return true;
  }

  public void setCaretPosition(int paramInt)
  {
    select(paramInt, paramInt);
  }

  public int getCaretPosition()
  {
    return getSelectionStart();
  }

  public void valueChanged()
  {
    postEvent(new TextEvent(this.target, 900));
  }

  private static native void initIDs();

  public int getIndexAtPoint(int paramInt1, int paramInt2)
  {
    return -1;
  }

  public Rectangle getCharacterBounds(int paramInt)
  {
    return null;
  }

  public long filterEvents(long paramLong)
  {
    return 3412046964836007936L;
  }

  public boolean shouldClearRectBeforePaint()
  {
    return false;
  }

  static
  {
    initIDs();
  }
}
package sun.awt.windows;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.TextArea;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextAreaPeer;

class WTextAreaPeer extends WTextComponentPeer
  implements TextAreaPeer
{
  public Dimension getMinimumSize()
  {
    return getMinimumSize(10, 60);
  }

  public void insert(String paramString, int paramInt)
  {
    insertText(paramString, paramInt);
  }

  public void replaceRange(String paramString, int paramInt1, int paramInt2)
  {
    replaceText(paramString, paramInt1, paramInt2);
  }

  public Dimension getPreferredSize(int paramInt1, int paramInt2)
  {
    return getMinimumSize(paramInt1, paramInt2);
  }

  public Dimension getMinimumSize(int paramInt1, int paramInt2)
  {
    FontMetrics localFontMetrics = getFontMetrics(((TextArea)this.target).getFont());
    return new Dimension(localFontMetrics.charWidth('0') * paramInt2 + 20, localFontMetrics.getHeight() * paramInt1 + 20);
  }

  public InputMethodRequests getInputMethodRequests()
  {
    return null;
  }

  WTextAreaPeer(TextArea paramTextArea)
  {
    super(paramTextArea);
  }

  native void create(WComponentPeer paramWComponentPeer);

  public native void insertText(String paramString, int paramInt);

  public native void replaceText(String paramString, int paramInt1, int paramInt2);

  public Dimension minimumSize()
  {
    return getMinimumSize();
  }

  public Dimension minimumSize(int paramInt1, int paramInt2)
  {
    return getMinimumSize(paramInt1, paramInt2);
  }

  public Dimension preferredSize(int paramInt1, int paramInt2)
  {
    return getPreferredSize(paramInt1, paramInt2);
  }
}
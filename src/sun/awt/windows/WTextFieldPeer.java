package sun.awt.windows;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextFieldPeer;

class WTextFieldPeer extends WTextComponentPeer
  implements TextFieldPeer
{
  public Dimension getMinimumSize()
  {
    FontMetrics localFontMetrics = getFontMetrics(((TextField)this.target).getFont());
    return new Dimension(localFontMetrics.stringWidth(getText()) + 24, localFontMetrics.getHeight() + 8);
  }

  public boolean handleJavaKeyEvent(KeyEvent paramKeyEvent)
  {
    switch (paramKeyEvent.getID())
    {
    case 400:
      if ((paramKeyEvent.getKeyChar() != '\n') || (paramKeyEvent.isAltDown()) || (paramKeyEvent.isControlDown()))
        break label79;
      postEvent(new ActionEvent(this.target, 1001, getText(), paramKeyEvent.getWhen(), paramKeyEvent.getModifiers()));
      return true;
    }
    label79: return false;
  }

  public void setEchoChar(char paramChar)
  {
    setEchoCharacter(paramChar);
  }

  public Dimension getPreferredSize(int paramInt)
  {
    return getMinimumSize(paramInt);
  }

  public Dimension getMinimumSize(int paramInt)
  {
    FontMetrics localFontMetrics = getFontMetrics(((TextField)this.target).getFont());
    return new Dimension(localFontMetrics.charWidth('0') * paramInt + 24, localFontMetrics.getHeight() + 8);
  }

  public InputMethodRequests getInputMethodRequests()
  {
    return null;
  }

  WTextFieldPeer(TextField paramTextField)
  {
    super(paramTextField);
  }

  native void create(WComponentPeer paramWComponentPeer);

  void initialize()
  {
    TextField localTextField = (TextField)this.target;
    if (localTextField.echoCharIsSet())
      setEchoChar(localTextField.getEchoChar());
    super.initialize();
  }

  public native void setEchoCharacter(char paramChar);

  public Dimension minimumSize()
  {
    return getMinimumSize();
  }

  public Dimension minimumSize(int paramInt)
  {
    return getMinimumSize(paramInt);
  }

  public Dimension preferredSize(int paramInt)
  {
    return getPreferredSize(paramInt);
  }
}
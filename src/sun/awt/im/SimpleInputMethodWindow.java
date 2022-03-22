package sun.awt.im;

import java.awt.Frame;

public class SimpleInputMethodWindow extends Frame
  implements InputMethodWindow
{
  InputContext inputContext = null;

  public SimpleInputMethodWindow(String paramString, InputContext paramInputContext)
  {
    super(paramString);
    if (paramInputContext != null)
      this.inputContext = paramInputContext;
    setFocusableWindowState(false);
  }

  public void setInputContext(InputContext paramInputContext)
  {
    this.inputContext = paramInputContext;
  }

  public java.awt.im.InputContext getInputContext()
  {
    if (this.inputContext != null)
      return this.inputContext;
    return super.getInputContext();
  }
}
package sun.awt.im;

import javax.swing.JFrame;
import javax.swing.JRootPane;

public class InputMethodJFrame extends JFrame
  implements InputMethodWindow
{
  InputContext inputContext = null;

  public InputMethodJFrame(String paramString, InputContext paramInputContext)
  {
    super(paramString);
    if (JFrame.isDefaultLookAndFeelDecorated())
    {
      setUndecorated(true);
      getRootPane().setWindowDecorationStyle(0);
    }
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
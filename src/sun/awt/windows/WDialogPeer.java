package sun.awt.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.peer.DialogPeer;
import sun.awt.im.InputMethodManager;

class WDialogPeer extends WWindowPeer
  implements DialogPeer
{
  static final Color defaultBackground = SystemColor.control;
  boolean needDefaultBackground;

  WDialogPeer(Dialog paramDialog)
  {
    super(paramDialog);
    InputMethodManager localInputMethodManager = InputMethodManager.getInstance();
    String str = localInputMethodManager.getTriggerMenuString();
    if (str != null)
      pSetIMMOption(str);
  }

  native void create(WComponentPeer paramWComponentPeer);

  native void showModal();

  native void endModal();

  void initialize()
  {
    Dialog localDialog = (Dialog)this.target;
    if (this.needDefaultBackground)
      localDialog.setBackground(defaultBackground);
    super.initialize();
    if (localDialog.getTitle() != null)
      setTitle(localDialog.getTitle());
    setResizable(localDialog.isResizable());
  }

  protected void realShow()
  {
    Dialog localDialog = (Dialog)this.target;
    if (localDialog.getModalityType() != Dialog.ModalityType.MODELESS)
    {
      showModal();
      this.visible = true;
    }
    else
    {
      super.realShow();
    }
  }

  public void hide()
  {
    Dialog localDialog = (Dialog)this.target;
    if (localDialog.getModalityType() != Dialog.ModalityType.MODELESS)
    {
      endModal();
      this.visible = false;
    }
    else
    {
      super.hide();
    }
  }

  public Dimension getMinimumSize()
  {
    if (((Dialog)this.target).isUndecorated())
      return super.getMinimumSize();
    return new Dimension(getSysMinWidth(), getSysMinHeight());
  }

  boolean isTargetUndecorated()
  {
    return ((Dialog)this.target).isUndecorated();
  }

  public void reshape(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Rectangle localRectangle = constrainBounds(paramInt1, paramInt2, paramInt3, paramInt4);
    if (((Dialog)this.target).isUndecorated())
      super.reshape(localRectangle.x, localRectangle.y, localRectangle.width, localRectangle.height);
    else
      reshapeFrame(localRectangle.x, localRectangle.y, localRectangle.width, localRectangle.height);
  }

  private void setDefaultColor()
  {
    this.needDefaultBackground = true;
  }

  native void pSetIMMOption(String paramString);

  void notifyIMMOptionChange()
  {
    InputMethodManager.getInstance().notifyChangeRequest((Component)this.target);
  }
}
package sun.awt;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;

public class ModalityEvent extends AWTEvent
  implements ActiveEvent
{
  public static final int MODALITY_PUSHED = 1300;
  public static final int MODALITY_POPPED = 1301;
  private ModalityListener listener;

  public ModalityEvent(Object paramObject, ModalityListener paramModalityListener, int paramInt)
  {
    super(paramObject, paramInt);
    this.listener = paramModalityListener;
  }

  public void dispatch()
  {
    switch (getID())
    {
    case 1300:
      this.listener.modalityPushed(this);
      break;
    case 1301:
      this.listener.modalityPopped(this);
      break;
    default:
      throw new Error("Invalid event id.");
    }
  }
}
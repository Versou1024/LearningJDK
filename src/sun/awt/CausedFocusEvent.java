package sun.awt;

import java.awt.Component;
import java.awt.event.FocusEvent;

public class CausedFocusEvent extends FocusEvent
{
  private final Cause cause;

  public Cause getCause()
  {
    return this.cause;
  }

  public String toString()
  {
    return "java.awt.FocusEvent[" + super.paramString() + ",cause=" + this.cause + "] on " + getSource();
  }

  public CausedFocusEvent(Component paramComponent1, int paramInt, boolean paramBoolean, Component paramComponent2, Cause paramCause)
  {
    super(paramComponent1, paramInt, paramBoolean, paramComponent2);
    if (paramCause == null)
      paramCause = Cause.UNKNOWN;
    this.cause = paramCause;
  }

  public static FocusEvent retarget(FocusEvent paramFocusEvent, Component paramComponent)
  {
    if (paramFocusEvent == null)
      return null;
    return new CausedFocusEvent(paramComponent, paramFocusEvent.getID(), paramFocusEvent.isTemporary(), paramFocusEvent.getOppositeComponent(), Cause.RETARGETED);
  }

  public static enum Cause
  {
    UNKNOWN, MOUSE_EVENT, TRAVERSAL, TRAVERSAL_UP, TRAVERSAL_DOWN, TRAVERSAL_FORWARD, TRAVERSAL_BACKWARD, MANUAL_REQUEST, AUTOMATIC_TRAVERSE, ROLLBACK, NATIVE_SYSTEM, ACTIVATION, CLEAR_GLOBAL_FOCUS_OWNER, RETARGETED;
  }
}
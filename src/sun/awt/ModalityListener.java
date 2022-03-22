package sun.awt;

public abstract interface ModalityListener
{
  public abstract void modalityPushed(ModalityEvent paramModalityEvent);

  public abstract void modalityPopped(ModalityEvent paramModalityEvent);
}
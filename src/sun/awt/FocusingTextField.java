package sun.awt;

import java.awt.Event;
import java.awt.TextField;

public class FocusingTextField extends TextField
{
  TextField next;
  boolean willSelect;

  public FocusingTextField(int paramInt)
  {
    super("", paramInt);
  }

  public FocusingTextField(int paramInt, boolean paramBoolean)
  {
    this(paramInt);
    this.willSelect = paramBoolean;
  }

  public void setWillSelect(boolean paramBoolean)
  {
    this.willSelect = paramBoolean;
  }

  public boolean getWillSelect()
  {
    return this.willSelect;
  }

  public void setNextField(TextField paramTextField)
  {
    this.next = paramTextField;
  }

  public boolean gotFocus(Event paramEvent, Object paramObject)
  {
    if (this.willSelect)
      select(0, getText().length());
    return true;
  }

  public boolean lostFocus(Event paramEvent, Object paramObject)
  {
    if (this.willSelect)
      select(0, 0);
    return true;
  }

  public void nextFocus()
  {
    if (this.next != null)
      this.next.requestFocus();
    super.nextFocus();
  }
}
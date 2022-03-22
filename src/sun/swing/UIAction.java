package sun.swing;

import java.beans.PropertyChangeListener;
import javax.swing.Action;

public abstract class UIAction
  implements Action
{
  private String name;

  public UIAction(String paramString)
  {
    this.name = paramString;
  }

  public final String getName()
  {
    return this.name;
  }

  public Object getValue(String paramString)
  {
    if (paramString == "Name")
      return this.name;
    return null;
  }

  public void putValue(String paramString, Object paramObject)
  {
  }

  public void setEnabled(boolean paramBoolean)
  {
  }

  public final boolean isEnabled()
  {
    return isEnabled(null);
  }

  public boolean isEnabled(Object paramObject)
  {
    return true;
  }

  public void addPropertyChangeListener(PropertyChangeListener paramPropertyChangeListener)
  {
  }

  public void removePropertyChangeListener(PropertyChangeListener paramPropertyChangeListener)
  {
  }
}
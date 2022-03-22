package sun.beans.editors;

import java.beans.PropertyEditorSupport;

public abstract class NumberEditor extends PropertyEditorSupport
{
  public String getJavaInitializationString()
  {
    return "" + getValue();
  }
}
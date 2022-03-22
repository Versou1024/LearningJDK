package sun.beans.editors;

import java.beans.PropertyEditorSupport;

public class StringEditor extends PropertyEditorSupport
{
  public String getJavaInitializationString()
  {
    return "\"" + getValue() + "\"";
  }

  public void setAsText(String paramString)
  {
    setValue(paramString);
  }
}
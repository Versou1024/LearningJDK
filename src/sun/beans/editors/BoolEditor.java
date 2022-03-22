package sun.beans.editors;

import java.beans.PropertyEditorSupport;

public class BoolEditor extends PropertyEditorSupport
{
  public String getJavaInitializationString()
  {
    if (((Boolean)getValue()).booleanValue())
      return "true";
    return "false";
  }

  public String getAsText()
  {
    if (((Boolean)getValue()).booleanValue())
      return "True";
    return "False";
  }

  public void setAsText(String paramString)
    throws IllegalArgumentException
  {
    if (paramString.toLowerCase().equals("true"))
      setValue(Boolean.TRUE);
    else if (paramString.toLowerCase().equals("false"))
      setValue(Boolean.FALSE);
    else
      throw new IllegalArgumentException(paramString);
  }

  public String[] getTags()
  {
    String[] arrayOfString = { "True", "False" };
    return arrayOfString;
  }
}
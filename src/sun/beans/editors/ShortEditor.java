package sun.beans.editors;

public class ShortEditor extends NumberEditor
{
  public String getJavaInitializationString()
  {
    return "((short)" + getValue() + ")";
  }

  public void setAsText(String paramString)
    throws IllegalArgumentException
  {
    setValue(Short.valueOf(paramString));
  }
}
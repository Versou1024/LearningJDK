package sun.beans.editors;

public class FloatEditor extends NumberEditor
{
  public String getJavaInitializationString()
  {
    return getValue() + "F";
  }

  public void setAsText(String paramString)
    throws IllegalArgumentException
  {
    setValue(Float.valueOf(paramString));
  }
}
package sun.beans.editors;

public class LongEditor extends NumberEditor
{
  public String getJavaInitializationString()
  {
    return getValue() + "L";
  }

  public void setAsText(String paramString)
    throws IllegalArgumentException
  {
    setValue(Long.valueOf(paramString));
  }
}
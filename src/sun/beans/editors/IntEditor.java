package sun.beans.editors;

public class IntEditor extends NumberEditor
{
  public void setAsText(String paramString)
    throws IllegalArgumentException
  {
    setValue(Integer.valueOf(paramString));
  }
}
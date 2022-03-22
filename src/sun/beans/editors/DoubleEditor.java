package sun.beans.editors;

public class DoubleEditor extends NumberEditor
{
  public void setAsText(String paramString)
    throws IllegalArgumentException
  {
    setValue(Double.valueOf(paramString));
  }
}
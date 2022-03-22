package sun.beans.editors;

public class ByteEditor extends NumberEditor
{
  public String getJavaInitializationString()
  {
    return "((byte)" + getValue() + ")";
  }

  public void setAsText(String paramString)
    throws IllegalArgumentException
  {
    setValue(Byte.valueOf(paramString));
  }
}
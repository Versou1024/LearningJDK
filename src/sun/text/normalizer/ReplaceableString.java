package sun.text.normalizer;

public class ReplaceableString
  implements Replaceable
{
  private StringBuffer buf;

  public ReplaceableString(String paramString)
  {
    this.buf = new StringBuffer(paramString);
  }

  public ReplaceableString(StringBuffer paramStringBuffer)
  {
    this.buf = paramStringBuffer;
  }

  public int length()
  {
    return this.buf.length();
  }

  public char charAt(int paramInt)
  {
    return this.buf.charAt(paramInt);
  }

  public void getChars(int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3)
  {
    Utility.getChars(this.buf, paramInt1, paramInt2, paramArrayOfChar, paramInt3);
  }
}
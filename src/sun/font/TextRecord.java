package sun.font;

public final class TextRecord
{
  public char[] text;
  public int start;
  public int limit;
  public int min;
  public int max;

  public void init(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.text = paramArrayOfChar;
    this.start = paramInt1;
    this.limit = paramInt2;
    this.min = paramInt3;
    this.max = paramInt4;
  }
}
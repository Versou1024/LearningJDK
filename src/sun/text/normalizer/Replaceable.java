package sun.text.normalizer;

public abstract interface Replaceable
{
  public abstract int length();

  public abstract char charAt(int paramInt);

  public abstract void getChars(int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3);
}
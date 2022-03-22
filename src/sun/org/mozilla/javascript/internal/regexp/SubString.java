package sun.org.mozilla.javascript.internal.regexp;

class SubString
{
  static final SubString emptySubString = new SubString();
  char[] charArray;
  int index;
  int length;

  public SubString()
  {
  }

  public SubString(String paramString)
  {
    this.index = 0;
    this.charArray = paramString.toCharArray();
    this.length = paramString.length();
  }

  public SubString(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    this.index = 0;
    this.length = paramInt2;
    this.charArray = new char[paramInt2];
    for (int i = 0; i < paramInt2; ++i)
      this.charArray[i] = paramArrayOfChar[(paramInt1 + i)];
  }

  public String toString()
  {
    return new String(this.charArray, this.index, this.length);
  }
}
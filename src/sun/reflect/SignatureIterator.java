package sun.reflect;

public class SignatureIterator
{
  private String sig;
  private int idx;

  public SignatureIterator(String paramString)
  {
    this.sig = paramString;
    reset();
  }

  public void reset()
  {
    this.idx = 1;
  }

  public boolean atEnd()
  {
    return (this.sig.charAt(this.idx) == ')');
  }

  public String next()
  {
    if (atEnd())
      return null;
    int i = this.sig.charAt(this.idx);
    if ((i != 91) && (i != 76))
    {
      this.idx += 1;
      return new String(new char[] { i });
    }
    int j = this.idx;
    if (i == 91)
      while ((i = this.sig.charAt(j)) == '[')
        ++j;
    while ((i == 76) && (this.sig.charAt(j) != ';'))
      ++j;
    int k = this.idx;
    this.idx = (j + 1);
    return this.sig.substring(k, this.idx);
  }

  public String returnType()
  {
    if (!(atEnd()))
      throw new InternalError("Illegal use of SignatureIterator");
    return this.sig.substring(this.idx + 1, this.sig.length());
  }
}
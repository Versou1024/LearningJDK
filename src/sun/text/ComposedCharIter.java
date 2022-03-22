package sun.text;

import sun.text.normalizer.NormalizerImpl;

public final class ComposedCharIter
{
  public static final int DONE = -1;
  private static int[] chars;
  private static String[] decomps;
  private static int decompNum;
  private int curChar = -1;

  public int next()
  {
    if (this.curChar == decompNum - 1)
      return -1;
    return chars[(++this.curChar)];
  }

  public String decomposition()
  {
    return decomps[this.curChar];
  }

  static
  {
    int i = 2000;
    chars = new int[i];
    decomps = new String[i];
    decompNum = NormalizerImpl.getDecompose(chars, decomps);
  }
}
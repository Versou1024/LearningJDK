package sun.org.mozilla.javascript.internal.regexp;

class REGlobalData
{
  boolean multiline;
  RECompiled regexp;
  int lastParen;
  int skipped;
  int cp;
  long[] parens;
  REProgState stateStackTop;
  REBackTrackData backTrackStackTop;

  int parens_index(int paramInt)
  {
    return (int)this.parens[paramInt];
  }

  int parens_length(int paramInt)
  {
    return (int)(this.parens[paramInt] >>> 32);
  }

  void set_parens(int paramInt1, int paramInt2, int paramInt3)
  {
    this.parens[paramInt1] = (paramInt2 & 0xFFFFFFFF | paramInt3 << 32);
  }
}
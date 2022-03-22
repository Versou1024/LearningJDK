package sun.org.mozilla.javascript.internal.regexp;

import J;

class REBackTrackData
{
  REBackTrackData previous;
  int continuation_op;
  int continuation_pc;
  int lastParen;
  long[] parens;
  int cp;
  REProgState stateStackTop;

  REBackTrackData(REGlobalData paramREGlobalData, int paramInt1, int paramInt2)
  {
    this.previous = paramREGlobalData.backTrackStackTop;
    this.continuation_op = paramInt1;
    this.continuation_pc = paramInt2;
    this.lastParen = paramREGlobalData.lastParen;
    if (paramREGlobalData.parens != null)
      this.parens = ((long[])(long[])paramREGlobalData.parens.clone());
    this.cp = paramREGlobalData.cp;
    this.stateStackTop = paramREGlobalData.stateStackTop;
  }
}
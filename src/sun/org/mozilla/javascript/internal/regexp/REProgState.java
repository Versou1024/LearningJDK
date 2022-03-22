package sun.org.mozilla.javascript.internal.regexp;

class REProgState
{
  REProgState previous;
  int min;
  int max;
  int index;
  int continuation_op;
  int continuation_pc;
  REBackTrackData backTrack;

  REProgState(REProgState paramREProgState, int paramInt1, int paramInt2, int paramInt3, REBackTrackData paramREBackTrackData, int paramInt4, int paramInt5)
  {
    this.previous = paramREProgState;
    this.min = paramInt1;
    this.max = paramInt2;
    this.index = paramInt3;
    this.continuation_op = paramInt5;
    this.continuation_pc = paramInt4;
    this.backTrack = paramREBackTrackData;
  }
}
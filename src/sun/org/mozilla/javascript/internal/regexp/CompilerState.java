package sun.org.mozilla.javascript.internal.regexp;

import sun.org.mozilla.javascript.internal.Context;

class CompilerState
{
  Context cx;
  char[] cpbegin;
  int cpend;
  int cp;
  int flags;
  int parenCount;
  int parenNesting;
  int classCount;
  int progLength;
  RENode result;

  CompilerState(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    this.cpbegin = paramArrayOfChar;
    this.cp = 0;
    this.cpend = paramInt1;
    this.flags = paramInt2;
    this.parenCount = 0;
    this.classCount = 0;
    this.progLength = 0;
  }
}
package sun.org.mozilla.javascript.internal.regexp;

class RENode
{
  byte op;
  RENode next;
  RENode kid;
  RENode kid2;
  int num;
  int parenIndex;
  int min;
  int max;
  int parenCount;
  boolean greedy;
  int startIndex;
  int kidlen;
  int bmsize;
  int index;
  char chr;
  int length;
  int flatIndex;

  RENode(byte paramByte)
  {
    this.op = paramByte;
  }
}
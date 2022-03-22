package sun.org.mozilla.javascript.internal.regexp;

import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

final class GlobData
{
  int mode;
  int optarg;
  boolean global;
  String str;
  NativeRegExp regexp;
  Scriptable arrayobj;
  Function lambda;
  String repstr;
  int dollar = -1;
  StringBuffer charBuf;
  int leftIndex;
}
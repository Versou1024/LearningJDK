package sun.org.mozilla.javascript.internal.regexp;

import java.io.Serializable;

class RECompiled
  implements Serializable
{
  static final long serialVersionUID = -6144956577595844213L;
  char[] source;
  int parenCount;
  int flags;
  byte[] program;
  int classCount;
  RECharSet[] classList;
  int anchorCh = -1;
}
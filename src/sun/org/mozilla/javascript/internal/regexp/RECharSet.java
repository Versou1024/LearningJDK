package sun.org.mozilla.javascript.internal.regexp;

import java.io.Serializable;

final class RECharSet
  implements Serializable
{
  static final long serialVersionUID = 7931787979395898394L;
  int length;
  int startIndex;
  int strlength;
  volatile transient boolean converted;
  volatile transient boolean sense;
  volatile transient byte[] bits;

  RECharSet(int paramInt1, int paramInt2, int paramInt3)
  {
    this.length = paramInt1;
    this.startIndex = paramInt2;
    this.strlength = paramInt3;
  }
}
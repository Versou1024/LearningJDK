package sun.net.www.protocol.http;

import sun.misc.BASE64Encoder;

class B64Encoder extends BASE64Encoder
{
  protected int bytesPerLine()
  {
    return 1024;
  }
}
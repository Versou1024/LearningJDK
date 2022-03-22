package sun.jkernel;

final class StandaloneSHA extends StandaloneMessageDigest
{
  static final boolean debug = 0;
  private final int[] W = new int[80];
  private final int[] state = new int[5];
  private static final int round1_kt = 1518500249;
  private static final int round2_kt = 1859775393;
  private static final int round3_kt = -1894007588;
  private static final int round4_kt = -899497514;

  StandaloneSHA()
  {
    super("SHA-1", 20, 64);
    implReset();
  }

  void implReset()
  {
    this.state[0] = 1732584193;
    this.state[1] = -271733879;
    this.state[2] = -1732584194;
    this.state[3] = 271733878;
    this.state[4] = -1009589776;
  }

  void implDigest(byte[] paramArrayOfByte, int paramInt)
  {
    long l = this.bytesProcessed << 3;
    int i = (int)this.bytesProcessed & 0x3F;
    int j = (i < 56) ? 56 - i : 120 - i;
    engineUpdate(padding, 0, j);
    StandaloneByteArrayAccess.i2bBig4((int)(l >>> 32), this.buffer, 56);
    StandaloneByteArrayAccess.i2bBig4((int)l, this.buffer, 60);
    implCompress(this.buffer, 0);
    StandaloneByteArrayAccess.i2bBig(this.state, 0, paramArrayOfByte, paramInt, 20);
  }

  void implCompress(byte[] paramArrayOfByte, int paramInt)
  {
    int i3;
    StandaloneByteArrayAccess.b2iBig(paramArrayOfByte, paramInt, this.W, 0, 64);
    for (int i = 16; i <= 79; ++i)
    {
      j = this.W[(i - 3)] ^ this.W[(i - 8)] ^ this.W[(i - 14)] ^ this.W[(i - 16)];
      this.W[i] = (j << 1 | j >>> 31);
    }
    i = this.state[0];
    int j = this.state[1];
    int k = this.state[2];
    int l = this.state[3];
    int i1 = this.state[4];
    for (int i2 = 0; i2 < 20; ++i2)
    {
      i3 = (i << 5 | i >>> 27) + (j & k | (j ^ 0xFFFFFFFF) & l) + i1 + this.W[i2] + 1518500249;
      i1 = l;
      l = k;
      k = j << 30 | j >>> 2;
      j = i;
      i = i3;
    }
    for (i2 = 20; i2 < 40; ++i2)
    {
      i3 = (i << 5 | i >>> 27) + (j ^ k ^ l) + i1 + this.W[i2] + 1859775393;
      i1 = l;
      l = k;
      k = j << 30 | j >>> 2;
      j = i;
      i = i3;
    }
    for (i2 = 40; i2 < 60; ++i2)
    {
      i3 = (i << 5 | i >>> 27) + (j & k | j & l | k & l) + i1 + this.W[i2] + -1894007588;
      i1 = l;
      l = k;
      k = j << 30 | j >>> 2;
      j = i;
      i = i3;
    }
    for (i2 = 60; i2 < 80; ++i2)
    {
      i3 = (i << 5 | i >>> 27) + (j ^ k ^ l) + i1 + this.W[i2] + -899497514;
      i1 = l;
      l = k;
      k = j << 30 | j >>> 2;
      j = i;
      i = i3;
    }
    this.state[0] += i;
    this.state[1] += j;
    this.state[2] += k;
    this.state[3] += l;
    this.state[4] += i1;
  }
}
package sun.security.provider;

import I;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;

public final class MD4 extends DigestBase
{
  private final int[] state;
  private final int[] x;
  private static final int S11 = 3;
  private static final int S12 = 7;
  private static final int S13 = 11;
  private static final int S14 = 19;
  private static final int S21 = 3;
  private static final int S22 = 5;
  private static final int S23 = 9;
  private static final int S24 = 13;
  private static final int S31 = 3;
  private static final int S32 = 9;
  private static final int S33 = 11;
  private static final int S34 = 15;
  private static final Provider md4Provider = new Provider("MD4Provider", 1D, "MD4 MessageDigest")
  {
  };

  public static MessageDigest getInstance()
  {
    try
    {
      return MessageDigest.getInstance("MD4", md4Provider);
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new ProviderException(localNoSuchAlgorithmException);
    }
  }

  public MD4()
  {
    super("MD4", 16, 64);
    this.state = new int[4];
    this.x = new int[16];
    implReset();
  }

  private MD4(MD4 paramMD4)
  {
    super(paramMD4);
    this.state = ((int[])paramMD4.state.clone());
    this.x = new int[16];
  }

  public Object clone()
  {
    return new MD4(this);
  }

  void implReset()
  {
    this.state[0] = 1732584193;
    this.state[1] = -271733879;
    this.state[2] = -1732584194;
    this.state[3] = 271733878;
  }

  void implDigest(byte[] paramArrayOfByte, int paramInt)
  {
    long l = this.bytesProcessed << 3;
    int i = (int)this.bytesProcessed & 0x3F;
    int j = (i < 56) ? 56 - i : 120 - i;
    engineUpdate(padding, 0, j);
    ByteArrayAccess.i2bLittle4((int)l, this.buffer, 56);
    ByteArrayAccess.i2bLittle4((int)(l >>> 32), this.buffer, 60);
    implCompress(this.buffer, 0);
    ByteArrayAccess.i2bLittle(this.state, 0, paramArrayOfByte, paramInt, 16);
  }

  private static int FF(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    paramInt1 += (paramInt2 & paramInt3 | (paramInt2 ^ 0xFFFFFFFF) & paramInt4) + paramInt5;
    return (paramInt1 << paramInt6 | paramInt1 >>> 32 - paramInt6);
  }

  private static int GG(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    paramInt1 += (paramInt2 & paramInt3 | paramInt2 & paramInt4 | paramInt3 & paramInt4) + paramInt5 + 1518500249;
    return (paramInt1 << paramInt6 | paramInt1 >>> 32 - paramInt6);
  }

  private static int HH(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    paramInt1 += (paramInt2 ^ paramInt3 ^ paramInt4) + paramInt5 + 1859775393;
    return (paramInt1 << paramInt6 | paramInt1 >>> 32 - paramInt6);
  }

  void implCompress(byte[] paramArrayOfByte, int paramInt)
  {
    ByteArrayAccess.b2iLittle64(paramArrayOfByte, paramInt, this.x);
    int i = this.state[0];
    int j = this.state[1];
    int k = this.state[2];
    int l = this.state[3];
    i = FF(i, j, k, l, this.x[0], 3);
    l = FF(l, i, j, k, this.x[1], 7);
    k = FF(k, l, i, j, this.x[2], 11);
    j = FF(j, k, l, i, this.x[3], 19);
    i = FF(i, j, k, l, this.x[4], 3);
    l = FF(l, i, j, k, this.x[5], 7);
    k = FF(k, l, i, j, this.x[6], 11);
    j = FF(j, k, l, i, this.x[7], 19);
    i = FF(i, j, k, l, this.x[8], 3);
    l = FF(l, i, j, k, this.x[9], 7);
    k = FF(k, l, i, j, this.x[10], 11);
    j = FF(j, k, l, i, this.x[11], 19);
    i = FF(i, j, k, l, this.x[12], 3);
    l = FF(l, i, j, k, this.x[13], 7);
    k = FF(k, l, i, j, this.x[14], 11);
    j = FF(j, k, l, i, this.x[15], 19);
    i = GG(i, j, k, l, this.x[0], 3);
    l = GG(l, i, j, k, this.x[4], 5);
    k = GG(k, l, i, j, this.x[8], 9);
    j = GG(j, k, l, i, this.x[12], 13);
    i = GG(i, j, k, l, this.x[1], 3);
    l = GG(l, i, j, k, this.x[5], 5);
    k = GG(k, l, i, j, this.x[9], 9);
    j = GG(j, k, l, i, this.x[13], 13);
    i = GG(i, j, k, l, this.x[2], 3);
    l = GG(l, i, j, k, this.x[6], 5);
    k = GG(k, l, i, j, this.x[10], 9);
    j = GG(j, k, l, i, this.x[14], 13);
    i = GG(i, j, k, l, this.x[3], 3);
    l = GG(l, i, j, k, this.x[7], 5);
    k = GG(k, l, i, j, this.x[11], 9);
    j = GG(j, k, l, i, this.x[15], 13);
    i = HH(i, j, k, l, this.x[0], 3);
    l = HH(l, i, j, k, this.x[8], 9);
    k = HH(k, l, i, j, this.x[4], 11);
    j = HH(j, k, l, i, this.x[12], 15);
    i = HH(i, j, k, l, this.x[2], 3);
    l = HH(l, i, j, k, this.x[10], 9);
    k = HH(k, l, i, j, this.x[6], 11);
    j = HH(j, k, l, i, this.x[14], 15);
    i = HH(i, j, k, l, this.x[1], 3);
    l = HH(l, i, j, k, this.x[9], 9);
    k = HH(k, l, i, j, this.x[5], 11);
    j = HH(j, k, l, i, this.x[13], 15);
    i = HH(i, j, k, l, this.x[3], 3);
    l = HH(l, i, j, k, this.x[11], 9);
    k = HH(k, l, i, j, this.x[7], 11);
    j = HH(j, k, l, i, this.x[15], 15);
    this.state[0] += i;
    this.state[1] += j;
    this.state[2] += k;
    this.state[3] += l;
  }

  static
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Void run()
      {
        MD4.access$000().put("MessageDigest.MD4", "sun.security.provider.MD4");
        return null;
      }
    });
  }
}
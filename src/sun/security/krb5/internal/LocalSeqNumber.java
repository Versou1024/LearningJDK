package sun.security.krb5.internal;

import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.internal.crypto.Confounder;

public class LocalSeqNumber
  implements SeqNumber
{
  private int lastSeqNumber;

  public LocalSeqNumber()
    throws KrbCryptoException
  {
    randInit();
  }

  public LocalSeqNumber(int paramInt)
  {
    init(paramInt);
  }

  public LocalSeqNumber(Integer paramInteger)
  {
    init(paramInteger.intValue());
  }

  public synchronized void randInit()
    throws KrbCryptoException
  {
    byte[] arrayOfByte = Confounder.bytes(4);
    arrayOfByte[0] = (byte)(arrayOfByte[0] & 0x3F);
    int i = arrayOfByte[3] & 0xFF | (arrayOfByte[2] & 0xFF) << 8 | (arrayOfByte[1] & 0xFF) << 16 | (arrayOfByte[0] & 0xFF) << 24;
    if (i == 0)
      i = 1;
    this.lastSeqNumber = i;
  }

  public synchronized void init(int paramInt)
  {
    this.lastSeqNumber = paramInt;
  }

  public synchronized int current()
  {
    return this.lastSeqNumber;
  }

  public synchronized int next()
  {
    return (this.lastSeqNumber + 1);
  }

  public synchronized int step()
  {
    return (++this.lastSeqNumber);
  }
}
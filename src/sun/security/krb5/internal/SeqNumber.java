package sun.security.krb5.internal;

import sun.security.krb5.KrbCryptoException;

public abstract interface SeqNumber
{
  public abstract void randInit()
    throws KrbCryptoException;

  public abstract void init(int paramInt);

  public abstract int current();

  public abstract int next();

  public abstract int step();
}
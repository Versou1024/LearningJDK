package sun.security.jgss.spi;

import java.security.Provider;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

public abstract interface GSSNameSpi
{
  public abstract Provider getProvider();

  public abstract boolean equals(GSSNameSpi paramGSSNameSpi)
    throws GSSException;

  public abstract boolean equals(Object paramObject);

  public abstract int hashCode();

  public abstract byte[] export()
    throws GSSException;

  public abstract Oid getMechanism();

  public abstract String toString();

  public abstract Oid getStringNameType();

  public abstract boolean isAnonymousName();
}
package sun.jdbc.odbc.ee;

import java.util.Properties;

public abstract interface PooledObject
{
  public static final int CHECKEDIN = 1;
  public static final int CHECKEDOUT = 2;
  public static final int MARKEDFORSWEEP = 3;

  public abstract boolean isMatching(Properties paramProperties);

  public abstract boolean isUsable();

  public abstract void markUsable();

  public abstract void checkedOut();

  public abstract void checkedIn();

  public abstract void markForSweep();

  public abstract boolean isMarkedForSweep();

  public abstract void destroy()
    throws Exception;

  public abstract long getCreatedTime();
}
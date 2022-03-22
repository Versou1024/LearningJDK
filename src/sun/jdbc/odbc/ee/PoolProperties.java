package sun.jdbc.odbc.ee;

import java.util.Hashtable;

public class PoolProperties
{
  public static final String MINPOOLSIZE = "minPoolSize";
  public static final String MAXPOOLSIZE = "maxPoolSize";
  public static final String INITIALPOOLSIZE = "initialPoolSize";
  public static final String MAXIDLETIME = "maxIdleTime";
  public static final String TIMEOUTFROMPOOL = "timeOutFromPool";
  public static final String MAINTENANCEINTERVAL = "mInterval";
  private Hashtable properties = new Hashtable();

  public PoolProperties()
  {
    this.properties.put("minPoolSize", new Integer(0));
    this.properties.put("maxPoolSize", new Integer(0));
    this.properties.put("initialPoolSize", new Integer(0));
    this.properties.put("maxIdleTime", new Integer(0));
    this.properties.put("timeOutFromPool", new Integer(0));
    this.properties.put("mInterval", new Integer(0));
  }

  public int get(String paramString)
  {
    return ((Integer)this.properties.get(paramString)).intValue();
  }

  public void set(String paramString, int paramInt)
  {
    this.properties.put(paramString, new Integer(paramInt));
  }
}
package sun.net.httpserver;

import java.security.AccessController;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetLongAction;

class ServerConfig
{
  static int clockTick;
  static int defaultClockTick = 10000;
  static long defaultReadTimeout = 20L;
  static long defaultWriteTimeout = 60L;
  static long defaultIdleInterval = 300L;
  static long defaultSelCacheTimeout = 120L;
  static int defaultMaxIdleConnections = 200;
  static long defaultDrainAmount = 65536L;
  static long readTimeout;
  static long writeTimeout;
  static long idleInterval;
  static long selCacheTimeout;
  static long drainAmount;
  static int maxIdleConnections;
  static boolean debug = false;

  static long getReadTimeout()
  {
    return readTimeout;
  }

  static long getSelCacheTimeout()
  {
    return selCacheTimeout;
  }

  static boolean debugEnabled()
  {
    return debug;
  }

  static long getIdleInterval()
  {
    return idleInterval;
  }

  static int getClockTick()
  {
    return clockTick;
  }

  static int getMaxIdleConnections()
  {
    return maxIdleConnections;
  }

  static long getWriteTimeout()
  {
    return writeTimeout;
  }

  static long getDrainAmount()
  {
    return drainAmount;
  }

  static
  {
    idleInterval = ((Long)AccessController.doPrivileged(new GetLongAction("sun.net.httpserver.idleInterval", defaultIdleInterval))).longValue() * 1000L;
    clockTick = ((Integer)AccessController.doPrivileged(new GetIntegerAction("sun.net.httpserver.clockTick", defaultClockTick))).intValue();
    maxIdleConnections = ((Integer)AccessController.doPrivileged(new GetIntegerAction("sun.net.httpserver.maxIdleConnections", defaultMaxIdleConnections))).intValue();
    readTimeout = ((Long)AccessController.doPrivileged(new GetLongAction("sun.net.httpserver.readTimeout", defaultReadTimeout))).longValue() * 1000L;
    selCacheTimeout = ((Long)AccessController.doPrivileged(new GetLongAction("sun.net.httpserver.selCacheTimeout", defaultSelCacheTimeout))).longValue() * 1000L;
    writeTimeout = ((Long)AccessController.doPrivileged(new GetLongAction("sun.net.httpserver.writeTimeout", defaultWriteTimeout))).longValue() * 1000L;
    drainAmount = ((Long)AccessController.doPrivileged(new GetLongAction("sun.net.httpserver.drainAmount", defaultDrainAmount))).longValue();
    debug = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.net.httpserver.debug"))).booleanValue();
  }
}
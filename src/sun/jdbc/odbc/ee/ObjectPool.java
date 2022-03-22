package sun.jdbc.odbc.ee;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import sun.jdbc.odbc.JdbcOdbcTracer;

public abstract class ObjectPool
{
  private int initialSize;
  private int maxSize;
  private int minSize;
  private int maxIdleTime;
  private int timeoutFromPool;
  private int mInterval;
  private int currentSize = 0;
  private String name;
  private Hashtable freePool;
  private Hashtable lockedObjects;
  private Hashtable garbagePool;
  private PoolWorker worker;
  private JdbcOdbcTracer tracer = new JdbcOdbcTracer();
  private boolean usable = true;
  private boolean initialized = false;
  private String errorMessage;

  public ObjectPool(String paramString)
  {
    this.name = paramString;
    this.worker = new PoolWorker(this);
    this.freePool = new Hashtable();
    this.lockedObjects = new Hashtable();
    this.garbagePool = new Hashtable();
  }

  public void setProperties(PoolProperties paramPoolProperties)
    throws SQLException
  {
    this.tracer.trace("Setting the properties in Pool");
    this.initialSize = paramPoolProperties.get("initialPoolSize");
    this.minSize = paramPoolProperties.get("minPoolSize");
    this.maxSize = paramPoolProperties.get("maxPoolSize");
    this.timeoutFromPool = paramPoolProperties.get("timeOutFromPool");
    this.mInterval = paramPoolProperties.get("mInterval");
    this.maxIdleTime = paramPoolProperties.get("maxIdleTime");
    if (this.minSize > this.initialSize)
    {
      this.initialSize = this.minSize;
      this.tracer.trace("Connection Pool: Initial Size is set to Max Size ");
    }
    if ((this.maxSize < this.initialSize) && (this.maxSize != 0))
    {
      this.maxSize = this.initialSize;
      this.tracer.trace("Connection Pool: Maximum size is less than Initial size, using the Initial size ");
    }
    if (this.mInterval == 0)
      throw new SQLException("Maintenance interval cannot be zero");
  }

  public void initializePool()
    throws SQLException
  {
    this.tracer.trace("Setting the properties in Pool");
    if (this.initialized)
      return;
    this.initialized = true;
    fillThePool(this.initialSize);
    this.worker.start();
  }

  protected void fillThePool(int paramInt)
    throws SQLException
  {
    this.tracer.trace("fillThePool: Filling the pool upto :" + paramInt + "from :" + this.currentSize);
    if (!(this.usable))
    {
      this.tracer.trace("The pool is marked non usable. Not filling the pool");
      return;
    }
    try
    {
      while (this.currentSize < paramInt)
        addNew(createObject());
    }
    catch (Exception localException)
    {
      this.tracer.trace("fillThePool: Exception thrown in filling." + localException.getMessage());
      throw new SQLException(localException.getMessage());
    }
  }

  protected PooledObject createObject()
    throws SQLException
  {
    return createObject(null);
  }

  protected synchronized PooledObject createObject(Properties paramProperties)
    throws SQLException
  {
    PooledObject localPooledObject = create(paramProperties);
    this.currentSize += 1;
    return localPooledObject;
  }

  protected abstract PooledObject create(Properties paramProperties)
    throws SQLException;

  protected synchronized void addNew(PooledObject paramPooledObject)
  {
    this.freePool.put(paramPooledObject, new Long(System.currentTimeMillis()));
  }

  protected boolean checkAndMark(PooledObject paramPooledObject)
  {
    if (this.freePool.containsKey(paramPooledObject))
    {
      long l = ((Long)this.freePool.get(paramPooledObject)).longValue();
      int i = 0;
      int j = 0;
      if ((paramPooledObject.getCreatedTime() + this.timeoutFromPool * 1000 < System.currentTimeMillis()) && (this.timeoutFromPool != 0))
        i = 1;
      if ((l + this.maxIdleTime * 1000 < System.currentTimeMillis()) && (this.maxIdleTime != 0))
        j = 1;
      if ((i != 0) || (j != 0) || (!(paramPooledObject.isUsable())))
      {
        paramPooledObject.markForSweep();
        this.garbagePool.put(paramPooledObject, "");
        this.freePool.remove(paramPooledObject);
        return true;
      }
      if (paramPooledObject.isMarkedForSweep())
      {
        this.garbagePool.put(paramPooledObject, "");
        this.freePool.remove(paramPooledObject);
        return true;
      }
      return false;
    }
    return false;
  }

  protected void destroyFromPool(PooledObject paramPooledObject, Hashtable paramHashtable)
  {
    try
    {
      paramPooledObject.destroy();
    }
    catch (Exception localException)
    {
      this.tracer.trace("Connection Pool : Exception while destroying + e.getMessage()");
    }
    paramHashtable.remove(paramPooledObject);
    this.currentSize -= 1;
  }

  public synchronized PooledObject checkOut()
    throws SQLException
  {
    PooledObject localPooledObject;
    if (!(this.usable))
      throw new SQLException(" Connection Pool: " + this.errorMessage);
    Enumeration localEnumeration = this.freePool.keys();
    while (localEnumeration.hasMoreElements())
    {
      localPooledObject = (PooledObject)localEnumeration.nextElement();
      if ((!(checkAndMark(localPooledObject))) && (this.freePool.containsKey(localPooledObject)))
      {
        this.lockedObjects.put(localPooledObject, "");
        this.freePool.remove(localPooledObject);
        localPooledObject.checkedOut();
        return localPooledObject;
      }
    }
    if ((this.currentSize < this.maxSize) || (this.maxSize == 0))
    {
      localPooledObject = createObject();
      this.lockedObjects.put(localPooledObject, "");
      localPooledObject.checkedOut();
      return localPooledObject;
    }
    throw new SQLException("Maximum limit has reached and no connection is free");
  }

  public synchronized PooledObject checkOut(Properties paramProperties)
    throws SQLException
  {
    PooledObject localPooledObject;
    if (!(this.usable))
      throw new SQLException(" Connection Pool: " + this.errorMessage);
    Enumeration localEnumeration = this.freePool.keys();
    while (localEnumeration.hasMoreElements())
    {
      localPooledObject = (PooledObject)localEnumeration.nextElement();
      if ((!(checkAndMark(localPooledObject))) && (this.freePool.containsKey(localPooledObject)) && (localPooledObject.isMatching(paramProperties)))
      {
        this.lockedObjects.put(localPooledObject, "");
        this.freePool.remove(localPooledObject);
        localPooledObject.checkedOut();
        return localPooledObject;
      }
    }
    if ((this.currentSize < this.maxSize) || (this.maxSize == 0))
    {
      localPooledObject = createObject(paramProperties);
      this.lockedObjects.put(localPooledObject, "");
      localPooledObject.checkedOut();
      return localPooledObject;
    }
    throw new SQLException("Maximum limit has reached and no connection is free");
  }

  public synchronized void tryCheckOut(PooledObject paramPooledObject)
    throws SQLException
  {
    if ((!(checkAndMark(paramPooledObject))) && (this.freePool.containsKey(paramPooledObject)))
    {
      this.lockedObjects.put(paramPooledObject, "");
      this.freePool.remove(paramPooledObject);
      paramPooledObject.checkedOut();
    }
    else
    {
      throw new SQLException("Object is not available for use" + this.freePool.containsKey(paramPooledObject));
    }
  }

  public synchronized void checkIn(PooledObject paramPooledObject)
  {
    int i = 0;
    if ((paramPooledObject.getCreatedTime() + this.timeoutFromPool * 1000 < System.currentTimeMillis()) && (this.timeoutFromPool != 0))
      i = 1;
    if ((i != 0) || (!(paramPooledObject.isUsable())))
    {
      paramPooledObject.markForSweep();
      this.garbagePool.put(paramPooledObject, "");
      this.lockedObjects.remove(paramPooledObject);
    }
    else
    {
      paramPooledObject.checkedIn();
      this.freePool.put(paramPooledObject, new Long(System.currentTimeMillis()));
      this.lockedObjects.remove(paramPooledObject);
    }
  }

  public int getCurrentSize()
  {
    return this.currentSize;
  }

  public int getMaintenanceInterval()
  {
    return this.mInterval;
  }

  public void setTracer(JdbcOdbcTracer paramJdbcOdbcTracer)
  {
    if (paramJdbcOdbcTracer != null)
      this.tracer = paramJdbcOdbcTracer;
  }

  public void markError(String paramString)
  {
    this.usable = false;
    this.errorMessage = paramString;
  }

  public JdbcOdbcTracer getTracer()
  {
    return this.tracer;
  }

  public String getName()
  {
    return this.name;
  }

  public void maintain()
    throws SQLException
  {
    this.tracer.trace("Before <maintenance> Locked :" + this.lockedObjects.size() + " free :" + this.freePool.size() + "garbage :" + this.garbagePool.size() + "current size :" + this.currentSize);
    Enumeration localEnumeration1 = this.garbagePool.keys();
    while (localEnumeration1.hasMoreElements())
    {
      ??? = (PooledObject)localEnumeration1.nextElement();
      destroyFromPool((PooledObject)???, this.garbagePool);
    }
    synchronized (this)
    {
      Enumeration localEnumeration2 = this.freePool.keys();
      while (localEnumeration2.hasMoreElements())
      {
        PooledObject localPooledObject = (PooledObject)localEnumeration2.nextElement();
        checkAndMark(localPooledObject);
      }
    }
    fillThePool(this.minSize);
    this.tracer.trace("Before <maintenance> Locked :" + this.lockedObjects.size() + " free :" + this.freePool.size() + "garbage :" + this.garbagePool.size() + "current size :" + this.currentSize);
  }

  public void shutDown(boolean paramBoolean)
  {
    if (paramBoolean == true)
    {
      this.worker.release();
      shutDownNow();
    }
    else
    {
      markError("Being shut down now");
    }
  }

  private synchronized void shutDownNow()
  {
    try
    {
      PooledObject localPooledObject;
      this.tracer.trace("Shutting down the pool");
      ConnectionPoolFactory.removePool(this.name);
      Enumeration localEnumeration = this.garbagePool.keys();
      while (localEnumeration.hasMoreElements())
      {
        localPooledObject = (PooledObject)localEnumeration.nextElement();
        destroyFromPool(localPooledObject, this.garbagePool);
      }
      localEnumeration = this.freePool.keys();
      while (localEnumeration.hasMoreElements())
      {
        localPooledObject = (PooledObject)localEnumeration.nextElement();
        destroyFromPool(localPooledObject, this.freePool);
      }
      localEnumeration = this.lockedObjects.keys();
      while (localEnumeration.hasMoreElements())
      {
        localPooledObject = (PooledObject)localEnumeration.nextElement();
        destroyFromPool(localPooledObject, this.lockedObjects);
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
      this.tracer.trace("An error occurred while shutting down " + localException);
    }
  }
}
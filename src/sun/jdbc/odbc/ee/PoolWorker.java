package sun.jdbc.odbc.ee;

import sun.jdbc.odbc.JdbcOdbcTracer;

public class PoolWorker extends Thread
{
  private ObjectPool pool;
  private final int NOTSTARTED = 0;
  private final int STARTED = 1;
  private final int STOPPED = 2;
  private int state = 0;

  public PoolWorker(ObjectPool paramObjectPool)
  {
    this.pool = paramObjectPool;
  }

  public void start()
  {
    if (this.state == 0)
    {
      this.state = 1;
      super.start();
    }
  }

  public void release()
  {
    this.state = 2;
    this.pool.markError("Pool maintenance stopped. Pool is either shutdown or there is an error!");
  }

  public void run()
  {
    if (this.state == 1)
      try
      {
        this.pool.getTracer().trace("Worker Thread : Maintenance of " + this.pool.getName() + "started");
        this.pool.maintain();
        if (this.pool.getCurrentSize() == 0)
          this.pool.shutDown(true);
        this.pool.getTracer().trace("Worker Thread : Maintenance of " + this.pool.getName() + "completed");
        sleep(this.pool.getMaintenanceInterval() * 1000);
      }
      catch (InterruptedException localInterruptedException)
      {
        this.pool.markError("Maintenance Thread Interrupted : " + localInterruptedException.getMessage());
      }
      catch (Exception localException)
      {
        this.pool.markError("Maintenance Thread Error : " + localException.getMessage());
      }
  }
}
package sun.misc;

public class RequestProcessor
  implements Runnable
{
  private static Queue requestQueue;
  private static Thread dispatcher;

  public static void postRequest(Request paramRequest)
  {
    lazyInitialize();
    requestQueue.enqueue(paramRequest);
  }

  public void run()
  {
    lazyInitialize();
    try
    {
      Object localObject = requestQueue.dequeue();
      if (localObject instanceof Request)
      {
        Request localRequest = (Request)localObject;
        try
        {
          localRequest.execute();
        }
        catch (Throwable localThrowable)
        {
        }
      }
    }
    catch (InterruptedException localInterruptedException)
    {
    }
  }

  public static synchronized void startProcessing()
  {
    if (dispatcher == null)
    {
      dispatcher = new Thread(new RequestProcessor(), "Request Processor");
      dispatcher.setPriority(7);
      dispatcher.start();
    }
  }

  private static synchronized void lazyInitialize()
  {
    if (requestQueue == null)
      requestQueue = new Queue();
  }
}
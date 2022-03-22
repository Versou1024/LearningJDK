package sun.rmi.transport.tcp;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import sun.rmi.runtime.NewThreadAction;
import sun.rmi.transport.Connection;

class ConnectionAcceptor
  implements Runnable
{
  private TCPTransport transport;
  private List<Connection> queue = new ArrayList();
  private static int threadNum = 0;

  public ConnectionAcceptor(TCPTransport paramTCPTransport)
  {
    this.transport = paramTCPTransport;
  }

  public void startNewAcceptor()
  {
    Thread localThread = (Thread)AccessController.doPrivileged(new NewThreadAction(this, "Multiplex Accept-" + (++threadNum), true));
    localThread.start();
  }

  public void accept(Connection paramConnection)
  {
    synchronized (this.queue)
    {
      this.queue.add(paramConnection);
      this.queue.notify();
    }
  }

  // ERROR //
  public void run()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 81	sun/rmi/transport/tcp/ConnectionAcceptor:queue	Ljava/util/List;
    //   4: dup
    //   5: astore_2
    //   6: monitorenter
    //   7: aload_0
    //   8: getfield 81	sun/rmi/transport/tcp/ConnectionAcceptor:queue	Ljava/util/List;
    //   11: invokeinterface 96 1 0
    //   16: ifne +17 -> 33
    //   19: aload_0
    //   20: getfield 81	sun/rmi/transport/tcp/ConnectionAcceptor:queue	Ljava/util/List;
    //   23: invokevirtual 85	java/lang/Object:wait	()V
    //   26: goto -19 -> 7
    //   29: astore_3
    //   30: goto -23 -> 7
    //   33: aload_0
    //   34: invokevirtual 94	sun/rmi/transport/tcp/ConnectionAcceptor:startNewAcceptor	()V
    //   37: aload_0
    //   38: getfield 81	sun/rmi/transport/tcp/ConnectionAcceptor:queue	Ljava/util/List;
    //   41: iconst_0
    //   42: invokeinterface 97 2 0
    //   47: checkcast 49	sun/rmi/transport/Connection
    //   50: astore_1
    //   51: aload_2
    //   52: monitorexit
    //   53: goto +10 -> 63
    //   56: astore 4
    //   58: aload_2
    //   59: monitorexit
    //   60: aload 4
    //   62: athrow
    //   63: aload_0
    //   64: getfield 82	sun/rmi/transport/tcp/ConnectionAcceptor:transport	Lsun/rmi/transport/tcp/TCPTransport;
    //   67: aload_1
    //   68: iconst_1
    //   69: invokevirtual 95	sun/rmi/transport/tcp/TCPTransport:handleMessages	(Lsun/rmi/transport/Connection;Z)V
    //   72: return
    //
    // Exception table:
    //   from	to	target	type
    //   19	26	29	java/lang/InterruptedException
    //   7	53	56	finally
    //   56	60	56	finally
  }
}
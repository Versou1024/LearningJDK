package sun.net.httpserver;

import com.sun.net.httpserver.Filter.Chain;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

class ServerImpl
  implements TimeSource
{
  private String protocol;
  private boolean https;
  private Executor executor;
  private HttpsConfigurator httpsConfig;
  private SSLContext sslContext;
  private ContextList contexts;
  private InetSocketAddress address;
  private ServerSocketChannel schan;
  private Selector selector;
  private SelectionKey listenerKey;
  private Set<HttpConnection> idleConnections;
  private Set<HttpConnection> allConnections;
  private List<Event> events;
  private Object lolock = new Object();
  private volatile boolean finished = false;
  private volatile boolean terminating = false;
  private boolean bound = false;
  private boolean started = false;
  private volatile long time;
  private volatile long ticks;
  private HttpServer wrapper;
  static final int CLOCK_TICK;
  static final long IDLE_INTERVAL;
  static final int MAX_IDLE_CONNECTIONS;
  private Timer timer;
  private Logger logger;
  Dispatcher dispatcher;
  static boolean debug;
  private int exchangeCount = 0;

  ServerImpl(HttpServer paramHttpServer, String paramString, InetSocketAddress paramInetSocketAddress, int paramInt)
    throws IOException
  {
    this.protocol = paramString;
    this.wrapper = paramHttpServer;
    this.logger = Logger.getLogger("com.sun.net.httpserver");
    this.https = paramString.equalsIgnoreCase("https");
    this.address = paramInetSocketAddress;
    this.contexts = new ContextList();
    this.schan = ServerSocketChannel.open();
    if (paramInetSocketAddress != null)
    {
      ServerSocket localServerSocket = this.schan.socket();
      localServerSocket.bind(paramInetSocketAddress, paramInt);
      this.bound = true;
    }
    this.selector = Selector.open();
    this.schan.configureBlocking(false);
    this.listenerKey = this.schan.register(this.selector, 16);
    this.dispatcher = new Dispatcher(this);
    this.idleConnections = Collections.synchronizedSet(new HashSet());
    this.allConnections = Collections.synchronizedSet(new HashSet());
    this.time = System.currentTimeMillis();
    this.timer = new Timer("server-timer", true);
    this.timer.schedule(new ServerTimerTask(this), CLOCK_TICK, CLOCK_TICK);
    this.events = new LinkedList();
    this.logger.config("HttpServer created " + paramString + " " + paramInetSocketAddress);
  }

  public void bind(InetSocketAddress paramInetSocketAddress, int paramInt)
    throws IOException
  {
    if (this.bound)
      throw new BindException("HttpServer already bound");
    if (paramInetSocketAddress == null)
      throw new NullPointerException("null address");
    ServerSocket localServerSocket = this.schan.socket();
    localServerSocket.bind(paramInetSocketAddress, paramInt);
    this.bound = true;
  }

  public void start()
  {
    if ((!(this.bound)) || (this.started) || (this.finished))
      throw new IllegalStateException("server in wrong state");
    if (this.executor == null)
      this.executor = new DefaultExecutor(null);
    Thread localThread = new Thread(this.dispatcher);
    this.started = true;
    localThread.start();
  }

  public void setExecutor(Executor paramExecutor)
  {
    if (this.started)
      throw new IllegalStateException("server already started");
    this.executor = paramExecutor;
  }

  public Executor getExecutor()
  {
    return this.executor;
  }

  public void setHttpsConfigurator(HttpsConfigurator paramHttpsConfigurator)
  {
    if (paramHttpsConfigurator == null)
      throw new NullPointerException("null HttpsConfigurator");
    if (this.started)
      throw new IllegalStateException("server already started");
    this.httpsConfig = paramHttpsConfigurator;
    this.sslContext = paramHttpsConfigurator.getSSLContext();
  }

  public HttpsConfigurator getHttpsConfigurator()
  {
    return this.httpsConfig;
  }

  public void stop(int paramInt)
  {
    if (paramInt < 0)
      throw new IllegalArgumentException("negative delay parameter");
    this.terminating = true;
    try
    {
      this.schan.close();
    }
    catch (IOException localIOException)
    {
    }
    this.selector.wakeup();
    long l = System.currentTimeMillis() + paramInt * 1000;
    do
    {
      if (System.currentTimeMillis() >= l)
        break;
      delay();
    }
    while (!(this.finished));
    this.finished = true;
    this.selector.wakeup();
    synchronized (this.allConnections)
    {
      Iterator localIterator = this.allConnections.iterator();
      while (localIterator.hasNext())
      {
        HttpConnection localHttpConnection = (HttpConnection)localIterator.next();
        localHttpConnection.close();
      }
    }
    this.allConnections.clear();
    this.idleConnections.clear();
    this.timer.cancel();
  }

  public synchronized HttpContextImpl createContext(String paramString, HttpHandler paramHttpHandler)
  {
    if ((paramHttpHandler == null) || (paramString == null))
      throw new NullPointerException("null handler, or path parameter");
    HttpContextImpl localHttpContextImpl = new HttpContextImpl(this.protocol, paramString, paramHttpHandler, this);
    this.contexts.add(localHttpContextImpl);
    this.logger.config("context created: " + paramString);
    return localHttpContextImpl;
  }

  public synchronized HttpContextImpl createContext(String paramString)
  {
    if (paramString == null)
      throw new NullPointerException("null path parameter");
    HttpContextImpl localHttpContextImpl = new HttpContextImpl(this.protocol, paramString, null, this);
    this.contexts.add(localHttpContextImpl);
    this.logger.config("context created: " + paramString);
    return localHttpContextImpl;
  }

  public synchronized void removeContext(String paramString)
    throws IllegalArgumentException
  {
    if (paramString == null)
      throw new NullPointerException("null path parameter");
    this.contexts.remove(this.protocol, paramString);
    this.logger.config("context removed: " + paramString);
  }

  public synchronized void removeContext(HttpContext paramHttpContext)
    throws IllegalArgumentException
  {
    if (!(paramHttpContext instanceof HttpContextImpl))
      throw new IllegalArgumentException("wrong HttpContext type");
    this.contexts.remove((HttpContextImpl)paramHttpContext);
    this.logger.config("context removed: " + paramHttpContext.getPath());
  }

  public InetSocketAddress getAddress()
  {
    return ((InetSocketAddress)this.schan.socket().getLocalSocketAddress());
  }

  Selector getSelector()
  {
    return this.selector;
  }

  void addEvent(Event paramEvent)
  {
    synchronized (this.lolock)
    {
      this.events.add(paramEvent);
      this.selector.wakeup();
    }
  }

  int resultSize()
  {
    synchronized (this.lolock)
    {
      return this.events.size();
    }
  }

  static synchronized void dprint(String paramString)
  {
    if (debug)
      System.out.println(paramString);
  }

  static synchronized void dprint(Exception paramException)
  {
    if (debug)
    {
      System.out.println(paramException);
      paramException.printStackTrace();
    }
  }

  Logger getLogger()
  {
    return this.logger;
  }

  void logReply(int paramInt, String paramString1, String paramString2)
  {
    if (paramString2 == null)
      paramString2 = "";
    String str = paramString1 + " [" + paramInt + " " + Code.msg(paramInt) + "] (" + paramString2 + ")";
    this.logger.fine(str);
  }

  long getTicks()
  {
    return this.ticks;
  }

  public long getTime()
  {
    return this.time;
  }

  void delay()
  {
    Thread.yield();
    try
    {
      Thread.sleep(200L);
    }
    catch (InterruptedException localInterruptedException)
    {
    }
  }

  synchronized void startExchange()
  {
    this.exchangeCount += 1;
  }

  synchronized int endExchange()
  {
    this.exchangeCount -= 1;
    if ((!($assertionsDisabled)) && (this.exchangeCount < 0))
      throw new AssertionError();
    return this.exchangeCount;
  }

  HttpServer getWrapper()
  {
    return this.wrapper;
  }

  static
  {
    CLOCK_TICK = ServerConfig.getClockTick();
    IDLE_INTERVAL = ServerConfig.getIdleInterval();
    MAX_IDLE_CONNECTIONS = ServerConfig.getMaxIdleConnections();
    debug = ServerConfig.debugEnabled();
  }

  private static class DefaultExecutor
  implements Executor
  {
    public void execute(Runnable paramRunnable)
    {
      paramRunnable.run();
    }
  }

  class Dispatcher
  implements Runnable
  {
    private void handleEvent()
    {
      ExchangeImpl localExchangeImpl = paramEvent.exchange;
      HttpConnection localHttpConnection = localExchangeImpl.getConnection();
      try
      {
        if (paramEvent instanceof WriteFinishedEvent)
        {
          int i = this.this$0.endExchange();
          if ((ServerImpl.access$100(this.this$0)) && (i == 0))
            ServerImpl.access$202(this.this$0, true);
          SocketChannel localSocketChannel = localHttpConnection.getChannel();
          LeftOverInputStream localLeftOverInputStream = localExchangeImpl.getOriginalInputStream();
          if (!(localLeftOverInputStream.isEOF()))
            localExchangeImpl.close = true;
          if ((localExchangeImpl.close) || (ServerImpl.access$300(this.this$0).size() >= ServerImpl.MAX_IDLE_CONNECTIONS))
          {
            localHttpConnection.close();
            ServerImpl.access$400(this.this$0).remove(localHttpConnection);
          }
          else if (localLeftOverInputStream.isDataBuffered())
          {
            handle(localHttpConnection.getChannel(), localHttpConnection);
          }
          else
          {
            SelectionKey localSelectionKey = localHttpConnection.getSelectionKey();
            if (localSelectionKey.isValid())
              localSelectionKey.interestOps(localSelectionKey.interestOps() | 0x1);
            localHttpConnection.time = (this.this$0.getTime() + ServerImpl.IDLE_INTERVAL);
            ServerImpl.access$300(this.this$0).add(localHttpConnection);
          }
        }
      }
      catch (IOException localIOException)
      {
        ServerImpl.access$500(this.this$0).log(Level.FINER, "Dispatcher (1)", localIOException);
        localHttpConnection.close();
      }
    }

    public void run()
    {
      if (!(ServerImpl.access$200(this.this$0)))
        try
        {
          while (this.this$0.resultSize() > 0)
            synchronized (ServerImpl.access$600(this.this$0))
            {
              localObject1 = (Event)ServerImpl.access$700(this.this$0).remove(0);
              handleEvent((Event)localObject1);
            }
          ServerImpl.access$800(this.this$0).select(1000L);
          Object localObject1 = ServerImpl.access$800(this.this$0).selectedKeys();
          ??? = ((Set)localObject1).iterator();
          while (true)
          {
            SelectionKey localSelectionKey;
            SocketChannel localSocketChannel;
            while (true)
            {
              while (true)
              {
                if (!(((Iterator)???).hasNext()))
                  break label335;
                localSelectionKey = (SelectionKey)((Iterator)???).next();
                ((Iterator)???).remove();
                if (!(localSelectionKey.equals(ServerImpl.access$900(this.this$0))))
                  break label240;
                if (!(ServerImpl.access$100(this.this$0)))
                  break;
              }
              localSocketChannel = ServerImpl.access$1000(this.this$0).accept();
              if (localSocketChannel != null)
                break;
            }
            localSocketChannel.configureBlocking(false);
            Object localObject4 = localSocketChannel.register(ServerImpl.access$800(this.this$0), 1);
            HttpConnection localHttpConnection = new HttpConnection();
            localHttpConnection.selectionKey = ((SelectionKey)localObject4);
            localHttpConnection.setChannel(localSocketChannel);
            ((SelectionKey)localObject4).attach(localHttpConnection);
            ServerImpl.access$400(this.this$0).add(localHttpConnection);
            continue;
            try
            {
              if (localSelectionKey.isReadable())
              {
                label240: localObject4 = (SocketChannel)localSelectionKey.channel();
                localHttpConnection = (HttpConnection)localSelectionKey.attachment();
                localSelectionKey.interestOps(0);
                handle((SocketChannel)localObject4, localHttpConnection);
              }
              else if (!($assertionsDisabled))
              {
                throw new AssertionError();
              }
            }
            catch (IOException localIOException2)
            {
              localObject4 = (HttpConnection)localSelectionKey.attachment();
              ServerImpl.access$500(this.this$0).log(Level.FINER, "Dispatcher (2)", localIOException2);
              label335: ((HttpConnection)localObject4).close();
            }
          }
        }
        catch (CancelledKeyException localCancelledKeyException)
        {
          ServerImpl.access$500(this.this$0).log(Level.FINER, "Dispatcher (3)", localCancelledKeyException);
        }
        catch (IOException localIOException1)
        {
          ServerImpl.access$500(this.this$0).log(Level.FINER, "Dispatcher (4)", localIOException1);
        }
        catch (Exception localException)
        {
          ServerImpl.access$500(this.this$0).log(Level.FINER, "Dispatcher (7)", localException);
        }
    }

    public void handle(, HttpConnection paramHttpConnection)
      throws IOException
    {
      ServerImpl.Exchange localExchange;
      try
      {
        localExchange = new ServerImpl.Exchange(this.this$0, paramSocketChannel, ServerImpl.access$1100(this.this$0), paramHttpConnection);
        ServerImpl.access$1200(this.this$0).execute(localExchange);
      }
      catch (HttpError localHttpError)
      {
        ServerImpl.access$500(this.this$0).log(Level.FINER, "Dispatcher (5)", localHttpError);
        paramHttpConnection.close();
      }
      catch (IOException localIOException)
      {
        ServerImpl.access$500(this.this$0).log(Level.FINER, "Dispatcher (6)", localIOException);
        paramHttpConnection.close();
      }
    }
  }

  class Exchange
  implements Runnable
  {
    SocketChannel chan;
    HttpConnection connection;
    HttpContextImpl context;
    InputStream rawin;
    OutputStream rawout;
    String protocol;
    ExchangeImpl tx;
    HttpContextImpl ctx;
    boolean rejected = false;

    Exchange(, SocketChannel paramSocketChannel, String paramString, HttpConnection paramHttpConnection)
      throws IOException
    {
      this.chan = paramSocketChannel;
      this.connection = paramHttpConnection;
      this.protocol = paramString;
    }

    public void run()
    {
      this.context = this.connection.getHttpContext();
      SSLEngine localSSLEngine = null;
      String str1 = null;
      SSLStreams localSSLStreams = null;
      try
      {
        int i;
        if (this.context != null)
        {
          this.rawin = this.connection.getInputStream();
          this.rawout = this.connection.getRawOutputStream();
          i = 0;
        }
        else
        {
          i = 1;
          if (ServerImpl.access$1300(this.this$0))
          {
            if (ServerImpl.access$1400(this.this$0) == null)
            {
              ServerImpl.access$500(this.this$0).warning("SSL connection received. No https contxt created");
              throw new HttpError("No SSL context established");
            }
            localSSLStreams = new SSLStreams(this.this$0, ServerImpl.access$1400(this.this$0), this.chan);
            this.rawin = localSSLStreams.getInputStream();
            this.rawout = localSSLStreams.getOutputStream();
            localSSLEngine = localSSLStreams.getSSLEngine();
          }
          else
          {
            this.rawin = new BufferedInputStream(new Request.ReadStream(this.this$0, this.chan));
            this.rawout = new Request.WriteStream(this.this$0, this.chan);
          }
        }
        Request localRequest = new Request(this.rawin, this.rawout);
        str1 = localRequest.requestLine();
        if (str1 == null)
        {
          this.connection.close();
          return;
        }
        int j = str1.indexOf(32);
        if (j == -1)
        {
          reject(400, str1, "Bad request line");
          return;
        }
        String str2 = str1.substring(0, j);
        int k = j + 1;
        j = str1.indexOf(32, k);
        if (j == -1)
        {
          reject(400, str1, "Bad request line");
          return;
        }
        String str3 = str1.substring(k, j);
        URI localURI = new URI(str3);
        k = j + 1;
        String str4 = str1.substring(k);
        Headers localHeaders = localRequest.headers();
        String str5 = localHeaders.getFirst("Transfer-encoding");
        int l = 0;
        if ((str5 != null) && (str5.equalsIgnoreCase("chunked")))
        {
          l = -1;
        }
        else
        {
          str5 = localHeaders.getFirst("Content-Length");
          if (str5 != null)
            l = Integer.parseInt(str5);
        }
        this.ctx = ServerImpl.access$1500(this.this$0).findContext(this.protocol, localURI.getPath());
        if (this.ctx == null)
        {
          reject(404, str1, "No context found for request");
          return;
        }
        this.connection.setContext(this.ctx);
        if (this.ctx.getHandler() == null)
        {
          reject(500, str1, "No handler for context");
          return;
        }
        this.tx = new ExchangeImpl(str2, localURI, localRequest, l, this.connection);
        String str6 = localHeaders.getFirst("Connection");
        if ((str6 != null) && (str6.equalsIgnoreCase("close")))
          this.tx.close = true;
        if (i != 0)
          this.connection.setParameters(this.rawin, this.rawout, this.chan, localSSLEngine, localSSLStreams, ServerImpl.access$1400(this.this$0), this.protocol, this.ctx, this.rawin);
        String str7 = localHeaders.getFirst("Expect");
        if ((str7 != null) && (str7.equalsIgnoreCase("100-continue")))
        {
          this.this$0.logReply(100, str1, null);
          sendReply(100, false, null);
        }
        List localList1 = this.ctx.getSystemFilters();
        List localList2 = this.ctx.getFilters();
        Filter.Chain localChain1 = new Filter.Chain(localList1, this.ctx.getHandler());
        Filter.Chain localChain2 = new Filter.Chain(localList2, new LinkHandler(this, localChain1));
        this.tx.getRequestBody();
        this.tx.getResponseBody();
        if (ServerImpl.access$1300(this.this$0))
          localChain2.doFilter(new HttpsExchangeImpl(this.tx));
        else
          localChain2.doFilter(new HttpExchangeImpl(this.tx));
      }
      catch (IOException localIOException)
      {
        ServerImpl.access$500(this.this$0).log(Level.FINER, "ServerImpl.Exchange (1)", localIOException);
        this.connection.close();
      }
      catch (NumberFormatException localNumberFormatException)
      {
        reject(400, str1, "NumberFormatException thrown");
      }
      catch (URISyntaxException localURISyntaxException)
      {
        reject(400, str1, "URISyntaxException thrown");
      }
      catch (Exception localException)
      {
        ServerImpl.access$500(this.this$0).log(Level.FINER, "ServerImpl.Exchange (2)", localException);
        this.connection.close();
      }
    }

    void reject(, String paramString1, String paramString2)
    {
      this.rejected = true;
      this.this$0.logReply(paramInt, paramString1, paramString2);
      sendReply(paramInt, true, "<h1>" + paramInt + Code.msg(paramInt) + "</h1>" + paramString2);
    }

    void sendReply(, boolean paramBoolean, String paramString)
    {
      String str;
      try
      {
        str = "HTTP/1.1 " + paramInt + Code.msg(paramInt) + "\r\n";
        if ((paramString != null) && (paramString.length() != 0))
        {
          str = str + "Content-Length: " + paramString.length() + "\r\n";
          str = str + "Content-Type: text/html\r\n";
        }
        else
        {
          str = str + "Content-Length: 0\r\n";
          paramString = "";
        }
        if (paramBoolean)
          str = str + "Connection: close\r\n";
        str = str + "\r\n" + paramString;
        byte[] arrayOfByte = str.getBytes("ISO8859_1");
        this.rawout.write(arrayOfByte);
        this.rawout.flush();
        if (paramBoolean)
          this.connection.close();
      }
      catch (IOException localIOException)
      {
        ServerImpl.access$500(this.this$0).log(Level.FINER, "ServerImpl.sendReply", localIOException);
        this.connection.close();
      }
    }

    class LinkHandler
  implements HttpHandler
    {
      Filter.Chain nextChain;

      LinkHandler(, Filter.Chain paramChain)
      {
        this.nextChain = paramChain;
      }

      public void handle()
        throws IOException
      {
        this.nextChain.doFilter(paramHttpExchange);
      }
    }
  }

  class ServerTimerTask extends TimerTask
  {
    public void run()
    {
      LinkedList localLinkedList = new LinkedList();
      ServerImpl.access$1602(this.this$0, System.currentTimeMillis());
      ServerImpl.access$1708(this.this$0);
      synchronized (ServerImpl.access$300(this.this$0))
      {
        HttpConnection localHttpConnection;
        Iterator localIterator = ServerImpl.access$300(this.this$0).iterator();
        while (localIterator.hasNext())
        {
          localHttpConnection = (HttpConnection)localIterator.next();
          if (localHttpConnection.time <= ServerImpl.access$1600(this.this$0))
            localLinkedList.add(localHttpConnection);
        }
        localIterator = localLinkedList.iterator();
        while (localIterator.hasNext())
        {
          localHttpConnection = (HttpConnection)localIterator.next();
          ServerImpl.access$300(this.this$0).remove(localHttpConnection);
          ServerImpl.access$400(this.this$0).remove(localHttpConnection);
          localHttpConnection.close();
        }
      }
    }
  }
}
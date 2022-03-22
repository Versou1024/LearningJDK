package sun.net.dns;

import java.security.AccessController;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import sun.security.action.LoadLibraryAction;

public class ResolverConfigurationImpl extends ResolverConfiguration
{
  private static Object lock;
  private final ResolverConfiguration.Options opts = new OptionsImpl();
  private static boolean changed;
  private static long lastRefresh;
  private static final int TIMEOUT = 120000;
  private static String os_searchlist;
  private static String os_nameservers;
  private static LinkedList searchlist;
  private static LinkedList nameservers;

  private LinkedList stringToList(String paramString)
  {
    LinkedList localLinkedList = new LinkedList();
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ", ");
    while (localStringTokenizer.hasMoreTokens())
    {
      String str = localStringTokenizer.nextToken();
      if (!(localLinkedList.contains(str)))
        localLinkedList.add(str);
    }
    return localLinkedList;
  }

  private void loadConfig()
  {
    if ((!($assertionsDisabled)) && (!(Thread.holdsLock(lock))))
      throw new AssertionError();
    if (changed)
    {
      changed = false;
    }
    else if (lastRefresh >= 3412047085095092224L)
    {
      long l = System.currentTimeMillis();
      if (l - lastRefresh < 120000L)
        return;
    }
    loadDNSconfig0();
    lastRefresh = System.currentTimeMillis();
    searchlist = stringToList(os_searchlist);
    nameservers = stringToList(os_nameservers);
    os_searchlist = null;
    os_nameservers = null;
  }

  public List searchlist()
  {
    synchronized (lock)
    {
      loadConfig();
      return ((List)searchlist.clone());
    }
  }

  public List nameservers()
  {
    synchronized (lock)
    {
      loadConfig();
      return ((List)nameservers.clone());
    }
  }

  public ResolverConfiguration.Options options()
  {
    return this.opts;
  }

  static native void init0();

  static native void loadDNSconfig0();

  static native int notifyAddrChange0();

  static
  {
    lock = new Object();
    changed = false;
    lastRefresh = -1L;
    AccessController.doPrivileged(new LoadLibraryAction("net"));
    init0();
    AddressChangeListener localAddressChangeListener = new AddressChangeListener();
    localAddressChangeListener.setDaemon(true);
    localAddressChangeListener.start();
  }

  static class AddressChangeListener extends Thread
  {
    public void run()
    {
      while (true)
      {
        if (ResolverConfigurationImpl.notifyAddrChange0() != 0)
          return;
        synchronized (ResolverConfigurationImpl.access$000())
        {
          ResolverConfigurationImpl.access$102(true);
        }
      }
    }
  }
}
package sun.net.httpserver;

import java.util.Iterator;
import java.util.LinkedList;

class ContextList
{
  static final int MAX_CONTEXTS = 50;
  LinkedList<HttpContextImpl> list = new LinkedList();

  public synchronized void add(HttpContextImpl paramHttpContextImpl)
  {
    if ((!($assertionsDisabled)) && (paramHttpContextImpl.getPath() == null))
      throw new AssertionError();
    this.list.add(paramHttpContextImpl);
  }

  public synchronized int size()
  {
    return this.list.size();
  }

  synchronized HttpContextImpl findContext(String paramString1, String paramString2)
  {
    return findContext(paramString1, paramString2, false);
  }

  synchronized HttpContextImpl findContext(String paramString1, String paramString2, boolean paramBoolean)
  {
    paramString1 = paramString1.toLowerCase();
    Object localObject1 = "";
    Object localObject2 = null;
    Iterator localIterator = this.list.iterator();
    while (true)
    {
      HttpContextImpl localHttpContextImpl;
      String str;
      while (true)
      {
        while (true)
        {
          while (true)
          {
            if (!(localIterator.hasNext()))
              break label121;
            localHttpContextImpl = (HttpContextImpl)localIterator.next();
            if (localHttpContextImpl.getProtocol().equals(paramString1))
              break;
          }
          str = localHttpContextImpl.getPath();
          if ((!(paramBoolean)) || (str.equals(paramString2)))
            break;
        }
        if ((paramBoolean) || (paramString2.startsWith(str)))
          break;
      }
      if (str.length() > ((String)localObject1).length())
      {
        localObject1 = str;
        localObject2 = localHttpContextImpl;
      }
    }
    label121: return ((HttpContextImpl)localObject2);
  }

  public synchronized void remove(String paramString1, String paramString2)
    throws IllegalArgumentException
  {
    HttpContextImpl localHttpContextImpl = findContext(paramString1, paramString2, true);
    if (localHttpContextImpl == null)
      throw new IllegalArgumentException("cannot remove element from list");
    this.list.remove(localHttpContextImpl);
  }

  public synchronized void remove(HttpContextImpl paramHttpContextImpl)
    throws IllegalArgumentException
  {
    Iterator localIterator = this.list.iterator();
    while (localIterator.hasNext())
    {
      HttpContextImpl localHttpContextImpl = (HttpContextImpl)localIterator.next();
      if (localHttpContextImpl.equals(paramHttpContextImpl))
      {
        this.list.remove(localHttpContextImpl);
        return;
      }
    }
    throw new IllegalArgumentException("no such context in list");
  }
}
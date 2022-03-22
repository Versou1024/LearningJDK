package sun.net.www.protocol.http;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryCookieStore
  implements CookieStore
{
  private List<HttpCookie> cookieJar = null;
  private Map<String, List<HttpCookie>> domainIndex = null;
  private Map<URI, List<HttpCookie>> uriIndex = null;
  private ReentrantLock lock = null;

  public void add(URI paramURI, HttpCookie paramHttpCookie)
  {
    if (paramHttpCookie == null)
      throw new NullPointerException("cookie is null");
    this.lock.lock();
    try
    {
      this.cookieJar.remove(paramHttpCookie);
      if (paramHttpCookie.getMaxAge() != 3412047239713914880L)
      {
        this.cookieJar.add(paramHttpCookie);
        addIndex(this.domainIndex, paramHttpCookie.getDomain(), paramHttpCookie);
        addIndex(this.uriIndex, getEffectiveURI(paramURI), paramHttpCookie);
      }
    }
    finally
    {
      this.lock.unlock();
    }
  }

  public List<HttpCookie> get(URI paramURI)
  {
    if (paramURI == null)
      throw new NullPointerException("uri is null");
    ArrayList localArrayList = new ArrayList();
    this.lock.lock();
    try
    {
      getInternal(localArrayList, this.domainIndex, new DomainComparator(paramURI.getHost()));
      getInternal(localArrayList, this.uriIndex, getEffectiveURI(paramURI));
    }
    finally
    {
      this.lock.unlock();
    }
    return localArrayList;
  }

  public List<HttpCookie> getCookies()
  {
    label50: List localList;
    this.lock.lock();
    try
    {
      Iterator localIterator = this.cookieJar.iterator();
      while (true)
      {
        do
          if (!(localIterator.hasNext()))
            break label50;
        while (!(((HttpCookie)localIterator.next()).hasExpired()));
        localIterator.remove();
      }
    }
    finally
    {
      localList = Collections.unmodifiableList(this.cookieJar);
      this.lock.unlock();
    }
    return localList;
  }

  public List<URI> getURIs()
  {
    ArrayList localArrayList = new ArrayList();
    this.lock.lock();
    try
    {
      Iterator localIterator = this.uriIndex.keySet().iterator();
      while (localIterator.hasNext())
      {
        URI localURI = (URI)localIterator.next();
        List localList = (List)this.uriIndex.get(localURI);
        if ((localList == null) || (localList.size() == 0))
          localIterator.remove();
      }
    }
    finally
    {
      localArrayList.addAll(this.uriIndex.keySet());
      this.lock.unlock();
    }
    return localArrayList;
  }

  public boolean remove(URI paramURI, HttpCookie paramHttpCookie)
  {
    if (paramHttpCookie == null)
      throw new NullPointerException("cookie is null");
    boolean bool = false;
    this.lock.lock();
    try
    {
      bool = this.cookieJar.remove(paramHttpCookie);
    }
    finally
    {
      this.lock.unlock();
    }
    return bool;
  }

  public boolean removeAll()
  {
    this.lock.lock();
    try
    {
      this.cookieJar.clear();
      this.domainIndex.clear();
      this.uriIndex.clear();
    }
    finally
    {
      this.lock.unlock();
    }
    return true;
  }

  private <T> void getInternal(List<HttpCookie> paramList, Map<T, List<HttpCookie>> paramMap, Comparable<T> paramComparable)
  {
    Iterator localIterator1 = paramMap.keySet().iterator();
    while (localIterator1.hasNext())
    {
      Object localObject = localIterator1.next();
      if (paramComparable.compareTo(localObject) == 0)
      {
        List localList = (List)paramMap.get(localObject);
        if (localList != null)
        {
          Iterator localIterator2 = localList.iterator();
          while (localIterator2.hasNext())
          {
            HttpCookie localHttpCookie = (HttpCookie)localIterator2.next();
            if (this.cookieJar.indexOf(localHttpCookie) != -1)
              if (!(localHttpCookie.hasExpired()))
              {
                if (!(paramList.contains(localHttpCookie)))
                  paramList.add(localHttpCookie);
              }
              else
              {
                localIterator2.remove();
                this.cookieJar.remove(localHttpCookie);
              }
            else
              localIterator2.remove();
          }
        }
      }
    }
  }

  private <T> void addIndex(Map<T, List<HttpCookie>> paramMap, T paramT, HttpCookie paramHttpCookie)
  {
    if (paramT != null)
    {
      Object localObject = (List)paramMap.get(paramT);
      if (localObject != null)
      {
        ((List)localObject).remove(paramHttpCookie);
        ((List)localObject).add(paramHttpCookie);
      }
      else
      {
        localObject = new ArrayList();
        ((List)localObject).add(paramHttpCookie);
        paramMap.put(paramT, localObject);
      }
    }
  }

  private URI getEffectiveURI(URI paramURI)
  {
    URI localURI = null;
    try
    {
      localURI = new URI(paramURI.getScheme(), paramURI.getAuthority(), null, null, null);
    }
    catch (URISyntaxException localURISyntaxException)
    {
      localURI = paramURI;
    }
    return localURI;
  }

  static class DomainComparator
  implements Comparable<String>
  {
    String host = null;

    public DomainComparator(String paramString)
    {
      this.host = paramString;
    }

    public int compareTo(String paramString)
    {
      if (HttpCookie.domainMatches(paramString, this.host))
        return 0;
      return -1;
    }
  }
}
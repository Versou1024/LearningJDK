package sun.net.www.protocol.http;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class AuthCacheImpl
  implements AuthCache
{
  HashMap hashtable = new HashMap();

  public void setMap(HashMap paramHashMap)
  {
    this.hashtable = paramHashMap;
  }

  public synchronized void put(String paramString, AuthCacheValue paramAuthCacheValue)
  {
    LinkedList localLinkedList = (LinkedList)this.hashtable.get(paramString);
    String str = paramAuthCacheValue.getPath();
    if (localLinkedList == null)
    {
      localLinkedList = new LinkedList();
      this.hashtable.put(paramString, localLinkedList);
    }
    ListIterator localListIterator = localLinkedList.listIterator();
    while (localListIterator.hasNext())
    {
      AuthenticationInfo localAuthenticationInfo = (AuthenticationInfo)localListIterator.next();
      if ((localAuthenticationInfo.path == null) || (localAuthenticationInfo.path.startsWith(str)))
        localListIterator.remove();
    }
    localListIterator.add(paramAuthCacheValue);
  }

  public synchronized AuthCacheValue get(String paramString1, String paramString2)
  {
    Object localObject = null;
    LinkedList localLinkedList = (LinkedList)this.hashtable.get(paramString1);
    if ((localLinkedList == null) || (localLinkedList.size() == 0))
      return null;
    if (paramString2 == null)
      return ((AuthenticationInfo)localLinkedList.get(0));
    ListIterator localListIterator = localLinkedList.listIterator();
    while (localListIterator.hasNext())
    {
      AuthenticationInfo localAuthenticationInfo = (AuthenticationInfo)localListIterator.next();
      if (paramString2.startsWith(localAuthenticationInfo.path))
        return localAuthenticationInfo;
    }
    return null;
  }

  public synchronized void remove(String paramString, AuthCacheValue paramAuthCacheValue)
  {
    LinkedList localLinkedList = (LinkedList)this.hashtable.get(paramString);
    if (localLinkedList == null)
      return;
    if (paramAuthCacheValue == null)
    {
      localLinkedList.clear();
      return;
    }
    ListIterator localListIterator = localLinkedList.listIterator();
    while (localListIterator.hasNext())
    {
      AuthenticationInfo localAuthenticationInfo = (AuthenticationInfo)localListIterator.next();
      if (paramAuthCacheValue.equals(localAuthenticationInfo))
        localListIterator.remove();
    }
  }
}
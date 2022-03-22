package sun.security.krb5.internal.rcache;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.ListIterator;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;

public class ReplayCache extends LinkedList
{
  private static final long serialVersionUID = 2997933194993803994L;
  private String principal;
  private CacheTable table;
  private int nap = 600000;
  private boolean DEBUG = Krb5.DEBUG;

  public ReplayCache(String paramString, CacheTable paramCacheTable)
  {
    this.principal = paramString;
    this.table = paramCacheTable;
  }

  public synchronized void put(AuthTime paramAuthTime, long paramLong)
  {
    if (size() == 0)
    {
      addFirst(paramAuthTime);
    }
    else
    {
      AuthTime localAuthTime1 = (AuthTime)getFirst();
      if (localAuthTime1.kerberosTime < paramAuthTime.kerberosTime)
      {
        addFirst(paramAuthTime);
      }
      else if (localAuthTime1.kerberosTime == paramAuthTime.kerberosTime)
      {
        if (localAuthTime1.cusec < paramAuthTime.cusec)
          addFirst(paramAuthTime);
      }
      else
      {
        ListIterator localListIterator1 = listIterator(1);
        do
        {
          if (!(localListIterator1.hasNext()))
            break label176;
          localAuthTime1 = (AuthTime)(AuthTime)localListIterator1.next();
          if (localAuthTime1.kerberosTime < paramAuthTime.kerberosTime)
          {
            add(indexOf(localAuthTime1), paramAuthTime);
            break label176:
          }
        }
        while ((localAuthTime1.kerberosTime != paramAuthTime.kerberosTime) || (localAuthTime1.cusec >= paramAuthTime.cusec));
        add(indexOf(localAuthTime1), paramAuthTime);
      }
    }
    label176: long l = paramLong - KerberosTime.getDefaultSkew() * 1000L;
    ListIterator localListIterator2 = listIterator(0);
    AuthTime localAuthTime2 = null;
    int i = -1;
    do
    {
      if (!(localListIterator2.hasNext()))
        break label248;
      localAuthTime2 = (AuthTime)(AuthTime)localListIterator2.next();
    }
    while (localAuthTime2.kerberosTime >= l);
    i = indexOf(localAuthTime2);
    if (i > -1)
      do
        label248: removeLast();
      while (size() > i);
    if (this.DEBUG)
      printList();
    if (size() == 0)
      this.table.remove(this.principal);
    if (this.DEBUG)
      printList();
  }

  private void printList()
  {
    Object[] arrayOfObject = toArray();
    for (int i = 0; i < arrayOfObject.length; ++i)
      System.out.println("object " + i + ": " + ((AuthTime)arrayOfObject[i]).kerberosTime + "/" + ((AuthTime)arrayOfObject[i]).cusec);
  }
}
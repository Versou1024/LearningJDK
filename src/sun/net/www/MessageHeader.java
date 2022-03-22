package sun.net.www;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class MessageHeader
{
  private String[] keys;
  private String[] values;
  private int nkeys;

  public MessageHeader()
  {
    grow();
  }

  public MessageHeader(InputStream paramInputStream)
    throws IOException
  {
    parseHeader(paramInputStream);
  }

  public synchronized void reset()
  {
    this.keys = null;
    this.values = null;
    this.nkeys = 0;
    grow();
  }

  public synchronized String findValue(String paramString)
  {
    int i;
    if (paramString == null)
    {
      i = this.nkeys;
      do
        if (--i < 0)
          break label32;
      while (this.keys[i] != null);
      label32: return this.values[i];
    }
    else
    {
      i = this.nkeys;
      do
        if (--i < 0)
          break label67;
      while (!(paramString.equalsIgnoreCase(this.keys[i])));
      return this.values[i];
    }
    label67: return null;
  }

  public synchronized int getKey(String paramString)
  {
    int i = this.nkeys;
    do
      if (--i < 0)
        break label41;
    while ((this.keys[i] != paramString) && (((paramString == null) || (!(paramString.equalsIgnoreCase(this.keys[i]))))));
    return i;
    label41: return -1;
  }

  public synchronized String getKey(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= this.nkeys))
      return null;
    return this.keys[paramInt];
  }

  public synchronized String getValue(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= this.nkeys))
      return null;
    return this.values[paramInt];
  }

  public synchronized String findNextValue(String paramString1, String paramString2)
  {
    int j;
    int i = 0;
    if (paramString1 == null)
    {
      j = this.nkeys;
      while (true)
      {
        do
        {
          do
            if (--j < 0)
              break label58;
          while (this.keys[j] != null);
          if (i != 0)
            return this.values[j];
        }
        while (this.values[j] != paramString2);
        label58: i = 1;
      }
    }
    else
    {
      j = this.nkeys;
      while (true)
      {
        do
        {
          do
            if (--j < 0)
              break label117;
          while (!(paramString1.equalsIgnoreCase(this.keys[j])));
          if (i != 0)
            return this.values[j];
        }
        while (this.values[j] != paramString2);
        i = 1;
      }
    }
    label117: return null;
  }

  public Iterator multiValueIterator(String paramString)
  {
    return new HeaderIterator(this, paramString, this);
  }

  public synchronized Map getHeaders()
  {
    return getHeaders(null);
  }

  public synchronized Map getHeaders(String[] paramArrayOfString)
  {
    int i = 0;
    HashMap localHashMap = new HashMap();
    int j = this.nkeys;
    while (true)
    {
      while (true)
      {
        if (--j < 0)
          break label148;
        if (paramArrayOfString != null)
          for (int k = 0; k < paramArrayOfString.length; ++k)
            if ((paramArrayOfString[k] != null) && (paramArrayOfString[k].equalsIgnoreCase(this.keys[j])))
            {
              i = 1;
              break;
            }
        if (i != 0)
          break;
        localObject1 = (List)localHashMap.get(this.keys[j]);
        if (localObject1 == null)
        {
          localObject1 = new ArrayList();
          localHashMap.put(this.keys[j], localObject1);
        }
        ((List)localObject1).add(this.values[j]);
      }
      i = 0;
    }
    label148: Set localSet = localHashMap.keySet();
    Object localObject1 = localSet.iterator();
    while (((Iterator)localObject1).hasNext())
    {
      Object localObject2 = ((Iterator)localObject1).next();
      List localList = (List)localHashMap.get(localObject2);
      localHashMap.put(localObject2, Collections.unmodifiableList(localList));
    }
    return ((Map)Collections.unmodifiableMap(localHashMap));
  }

  public synchronized void print(PrintStream paramPrintStream)
  {
    for (int i = 0; i < this.nkeys; ++i)
      if (this.keys[i] != null)
        paramPrintStream.print(this.keys[i] + ((this.values[i] != null) ? ": " + this.values[i] : "") + "\r\n");
    paramPrintStream.print("\r\n");
    paramPrintStream.flush();
  }

  public synchronized void add(String paramString1, String paramString2)
  {
    grow();
    this.keys[this.nkeys] = paramString1;
    this.values[this.nkeys] = paramString2;
    this.nkeys += 1;
  }

  public synchronized void prepend(String paramString1, String paramString2)
  {
    grow();
    for (int i = this.nkeys; i > 0; --i)
    {
      this.keys[i] = this.keys[(i - 1)];
      this.values[i] = this.values[(i - 1)];
    }
    this.keys[0] = paramString1;
    this.values[0] = paramString2;
    this.nkeys += 1;
  }

  public synchronized void set(int paramInt, String paramString1, String paramString2)
  {
    grow();
    if (paramInt < 0)
      return;
    if (paramInt >= this.nkeys)
    {
      add(paramString1, paramString2);
    }
    else
    {
      this.keys[paramInt] = paramString1;
      this.values[paramInt] = paramString2;
    }
  }

  private void grow()
  {
    if ((this.keys == null) || (this.nkeys >= this.keys.length))
    {
      String[] arrayOfString1 = new String[this.nkeys + 4];
      String[] arrayOfString2 = new String[this.nkeys + 4];
      if (this.keys != null)
        System.arraycopy(this.keys, 0, arrayOfString1, 0, this.nkeys);
      if (this.values != null)
        System.arraycopy(this.values, 0, arrayOfString2, 0, this.nkeys);
      this.keys = arrayOfString1;
      this.values = arrayOfString2;
    }
  }

  public synchronized void remove(String paramString)
  {
    int i;
    int j;
    if (paramString == null)
      for (i = 0; i < this.nkeys; ++i)
        while ((this.keys[i] == null) && (i < this.nkeys))
        {
          for (j = i; j < this.nkeys - 1; ++j)
          {
            this.keys[j] = this.keys[(j + 1)];
            this.values[j] = this.values[(j + 1)];
          }
          this.nkeys -= 1;
        }
    else
      for (i = 0; i < this.nkeys; ++i)
        while ((paramString.equalsIgnoreCase(this.keys[i])) && (i < this.nkeys))
        {
          for (j = i; j < this.nkeys - 1; ++j)
          {
            this.keys[j] = this.keys[(j + 1)];
            this.values[j] = this.values[(j + 1)];
          }
          this.nkeys -= 1;
        }
  }

  public synchronized void set(String paramString1, String paramString2)
  {
    int i = this.nkeys;
    do
      if (--i < 0)
        break label33;
    while (!(paramString1.equalsIgnoreCase(this.keys[i])));
    this.values[i] = paramString2;
    return;
    label33: add(paramString1, paramString2);
  }

  public synchronized void setIfNotSet(String paramString1, String paramString2)
  {
    if (findValue(paramString1) == null)
      add(paramString1, paramString2);
  }

  public static String canonicalID(String paramString)
  {
    int l;
    if (paramString == null)
      return "";
    int i = 0;
    int j = paramString.length();
    for (int k = 0; i < j; k = 1)
    {
      if (((l = paramString.charAt(i)) != '<') && (l > 32))
        break;
      ++i;
    }
    while (i < j)
    {
      if (((l = paramString.charAt(j - 1)) != '>') && (l > 32))
        break;
      --j;
      k = 1;
    }
    return ((k != 0) ? paramString.substring(i, j) : paramString);
  }

  public void parseHeader(InputStream paramInputStream)
    throws IOException
  {
    synchronized (this)
    {
      this.nkeys = 0;
    }
    mergeHeader(paramInputStream);
  }

  public void mergeHeader(InputStream paramInputStream)
    throws IOException
  {
    if (paramInputStream == null)
      return;
    Object localObject1 = new char[10];
    int i = paramInputStream.read();
    while ((i != 10) && (i != 13) && (i >= 0))
    {
      Object localObject2;
      String str;
      int j = 0;
      int k = -1;
      int i1 = (i > 32) ? 1 : 0;
      localObject1[(j++)] = (char)i;
      while ((l = paramInputStream.read()) >= 0)
      {
        int l;
        switch (l)
        {
        case 58:
          if ((i1 != 0) && (j > 0))
            k = j;
          i1 = 0;
          break;
        case 9:
          l = 32;
        case 32:
          i1 = 0;
          break;
        case 10:
        case 13:
          i = paramInputStream.read();
          if ((l == 13) && (i == 10))
          {
            i = paramInputStream.read();
            if (i == 13)
              i = paramInputStream.read();
          }
          if ((i == 10) || (i == 13))
            break label252;
          if (i > 32)
            break label252:
          l = 32;
        }
        if (j >= localObject1.length)
        {
          localObject2 = new char[localObject1.length * 2];
          System.arraycopy(localObject1, 0, localObject2, 0, j);
          localObject1 = localObject2;
        }
        localObject1[(j++)] = (char)l;
      }
      i = -1;
      while ((j > 0) && (localObject1[(j - 1)] <= ' '))
        label252: --j;
      if (k <= 0)
      {
        localObject2 = null;
        k = 0;
      }
      else
      {
        localObject2 = String.copyValueOf(localObject1, 0, k);
        if ((k < j) && (localObject1[k] == ':'))
          ++k;
        while ((k < j) && (localObject1[k] <= ' '))
          ++k;
      }
      if (k >= j)
        str = new String();
      else
        str = String.copyValueOf(localObject1, k, j - k);
      add((String)localObject2, str);
    }
  }

  public synchronized String toString()
  {
    String str = super.toString() + this.nkeys + " pairs: ";
    for (int i = 0; (i < this.keys.length) && (i < this.nkeys); ++i)
      str = str + "{" + this.keys[i] + ": " + this.values[i] + "}";
    return str;
  }

  class HeaderIterator
  implements Iterator
  {
    int index = 0;
    int next = -1;
    String key;
    boolean haveNext = false;
    Object lock;

    public HeaderIterator(, String paramString, Object paramObject)
    {
      this.key = paramString;
      this.lock = paramObject;
    }

    public boolean hasNext()
    {
      synchronized (this.lock)
      {
        if (!(this.haveNext))
          break label18;
        return true;
        while (this.index < MessageHeader.access$000(this.this$0))
        {
          if (this.key.equalsIgnoreCase(MessageHeader.access$100(this.this$0)[this.index]))
          {
            label18: this.haveNext = true;
            this.next = (this.index++);
            return true;
          }
          this.index += 1;
        }
        return false;
      }
    }

    public Object next()
    {
      synchronized (this.lock)
      {
        if (!(this.haveNext))
          break label34;
        this.haveNext = false;
        return MessageHeader.access$200(this.this$0)[this.next];
        label34: if (!(hasNext()))
          break label48;
        return next();
        label48: throw new NoSuchElementException("No more elements");
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException("remove not allowed");
    }
  }
}
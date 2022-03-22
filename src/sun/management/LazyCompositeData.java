package sun.management;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularType;

public abstract class LazyCompositeData
  implements CompositeData, Serializable
{
  private CompositeData compositeData;

  public boolean containsKey(String paramString)
  {
    return compositeData().containsKey(paramString);
  }

  public boolean containsValue(Object paramObject)
  {
    return compositeData().containsValue(paramObject);
  }

  public boolean equals(Object paramObject)
  {
    return compositeData().equals(paramObject);
  }

  public Object get(String paramString)
  {
    return compositeData().get(paramString);
  }

  public Object[] getAll(String[] paramArrayOfString)
  {
    return compositeData().getAll(paramArrayOfString);
  }

  public CompositeType getCompositeType()
  {
    return compositeData().getCompositeType();
  }

  public int hashCode()
  {
    return compositeData().hashCode();
  }

  public String toString()
  {
    return compositeData().toString();
  }

  public Collection values()
  {
    return compositeData().values();
  }

  private synchronized CompositeData compositeData()
  {
    if (this.compositeData != null)
      return this.compositeData;
    this.compositeData = getCompositeData();
    return this.compositeData;
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return compositeData();
  }

  protected abstract CompositeData getCompositeData();

  static String getString(CompositeData paramCompositeData, String paramString)
  {
    if (paramCompositeData == null)
      throw new IllegalArgumentException("Null CompositeData");
    return ((String)paramCompositeData.get(paramString));
  }

  static boolean getBoolean(CompositeData paramCompositeData, String paramString)
  {
    if (paramCompositeData == null)
      throw new IllegalArgumentException("Null CompositeData");
    return ((Boolean)paramCompositeData.get(paramString)).booleanValue();
  }

  static long getLong(CompositeData paramCompositeData, String paramString)
  {
    if (paramCompositeData == null)
      throw new IllegalArgumentException("Null CompositeData");
    return ((Long)paramCompositeData.get(paramString)).longValue();
  }

  static int getInt(CompositeData paramCompositeData, String paramString)
  {
    if (paramCompositeData == null)
      throw new IllegalArgumentException("Null CompositeData");
    return ((Integer)paramCompositeData.get(paramString)).intValue();
  }

  protected static boolean isTypeMatched(CompositeType paramCompositeType1, CompositeType paramCompositeType2)
  {
    if (paramCompositeType1 == paramCompositeType2)
      return true;
    Set localSet = paramCompositeType1.keySet();
    if (!(paramCompositeType2.keySet().containsAll(localSet)))
      return false;
    Iterator localIterator = localSet.iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      OpenType localOpenType1 = paramCompositeType1.getType(str);
      OpenType localOpenType2 = paramCompositeType2.getType(str);
      if (localOpenType1 instanceof CompositeType)
      {
        if (!(localOpenType2 instanceof CompositeType))
          return false;
        if (isTypeMatched((CompositeType)localOpenType1, (CompositeType)localOpenType2))
          continue;
        return false;
      }
      if (localOpenType1 instanceof TabularType)
      {
        if (!(localOpenType2 instanceof TabularType))
          return false;
        if (isTypeMatched((TabularType)localOpenType1, (TabularType)localOpenType2))
          continue;
        return false;
      }
      if (!(localOpenType1.equals(localOpenType2)))
        return false;
    }
    return true;
  }

  protected static boolean isTypeMatched(TabularType paramTabularType1, TabularType paramTabularType2)
  {
    if (paramTabularType1 == paramTabularType2)
      return true;
    List localList1 = paramTabularType1.getIndexNames();
    List localList2 = paramTabularType2.getIndexNames();
    if (!(localList1.equals(localList2)))
      return false;
    return isTypeMatched(paramTabularType1.getRowType(), paramTabularType2.getRowType());
  }
}
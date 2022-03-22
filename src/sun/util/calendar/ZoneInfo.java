package sun.util.calendar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.SoftReference;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import sun.security.action.GetPropertyAction;

public class ZoneInfo extends TimeZone
{
  private static final int UTC_TIME = 0;
  private static final int STANDARD_TIME = 1;
  private static final int WALL_TIME = 2;
  private static final long OFFSET_MASK = 15L;
  private static final long DST_MASK = 240L;
  private static final int DST_NSHIFT = 4;
  private static final long ABBR_MASK = 3840L;
  private static final int TRANSITION_NSHIFT = 12;
  private static final boolean USE_OLDMAPPING;
  private static final CalendarSystem gcal;
  private int rawOffset;
  private int rawOffsetDiff;
  private int checksum;
  private int dstSavings;
  private long[] transitions;
  private int[] offsets;
  private int[] simpleTimeZoneParams;
  private boolean willGMTOffsetChange;
  private transient boolean dirty;
  private static final long serialVersionUID = 2653134537216586139L;
  private transient SimpleTimeZone lastRule;
  private static SoftReference<Map> aliasTable;

  public ZoneInfo()
  {
    this.rawOffsetDiff = 0;
    this.willGMTOffsetChange = false;
    this.dirty = false;
  }

  public ZoneInfo(String paramString, int paramInt)
  {
    this(paramString, paramInt, 0, 0, null, null, null, false);
  }

  ZoneInfo(String paramString, int paramInt1, int paramInt2, int paramInt3, long[] paramArrayOfLong, int[] paramArrayOfInt1, int[] paramArrayOfInt2, boolean paramBoolean)
  {
    this.rawOffsetDiff = 0;
    this.willGMTOffsetChange = false;
    this.dirty = false;
    setID(paramString);
    this.rawOffset = paramInt1;
    this.dstSavings = paramInt2;
    this.checksum = paramInt3;
    this.transitions = paramArrayOfLong;
    this.offsets = paramArrayOfInt1;
    this.simpleTimeZoneParams = paramArrayOfInt2;
    this.willGMTOffsetChange = paramBoolean;
  }

  public int getOffset(long paramLong)
  {
    return getOffsets(paramLong, null, 0);
  }

  public int getOffsets(long paramLong, int[] paramArrayOfInt)
  {
    return getOffsets(paramLong, paramArrayOfInt, 0);
  }

  public int getOffsetsByStandard(long paramLong, int[] paramArrayOfInt)
  {
    return getOffsets(paramLong, paramArrayOfInt, 1);
  }

  public int getOffsetsByWall(long paramLong, int[] paramArrayOfInt)
  {
    return getOffsets(paramLong, paramArrayOfInt, 2);
  }

  private int getOffsets(long paramLong, int[] paramArrayOfInt, int paramInt)
  {
    int i2;
    if (this.transitions == null)
    {
      i = getLastRawOffset();
      if (paramArrayOfInt != null)
      {
        paramArrayOfInt[0] = i;
        paramArrayOfInt[1] = 0;
      }
      return i;
    }
    paramLong -= this.rawOffsetDiff;
    int i = getTransitionIndex(paramLong, paramInt);
    if (i < 0)
    {
      int j = getLastRawOffset();
      if (paramArrayOfInt != null)
      {
        paramArrayOfInt[0] = j;
        paramArrayOfInt[1] = 0;
      }
      return j;
    }
    if (i < this.transitions.length)
    {
      long l1 = this.transitions[i];
      int l = this.offsets[(int)(l1 & 0xF)] + this.rawOffsetDiff;
      if (paramArrayOfInt != null)
      {
        int i1 = (int)(l1 >>> 4 & 0xF);
        i2 = (i1 == 0) ? 0 : this.offsets[i1];
        paramArrayOfInt[0] = (l - i2);
        paramArrayOfInt[1] = i2;
      }
      return l;
    }
    SimpleTimeZone localSimpleTimeZone = getLastRule();
    if (localSimpleTimeZone != null)
    {
      k = localSimpleTimeZone.getRawOffset();
      long l2 = paramLong;
      if (paramInt != 0)
        l2 -= this.rawOffset;
      i2 = (localSimpleTimeZone.inDaylightTime(new Date(l2))) ? localSimpleTimeZone.getDSTSavings() : 0;
      if (paramArrayOfInt != null)
      {
        paramArrayOfInt[0] = k;
        paramArrayOfInt[1] = i2;
      }
      return (k + i2);
    }
    int k = getLastRawOffset();
    if (paramArrayOfInt != null)
    {
      paramArrayOfInt[0] = k;
      paramArrayOfInt[1] = 0;
    }
    return k;
  }

  private final int getTransitionIndex(long paramLong, int paramInt)
  {
    int i = 0;
    int j = this.transitions.length - 1;
    while (i <= j)
    {
      int k = (i + j) / 2;
      long l1 = this.transitions[k];
      long l2 = l1 >> 12;
      if (paramInt != 0)
        l2 += this.offsets[(int)(l1 & 0xF)];
      if (paramInt == 1)
      {
        int l = (int)(l1 >>> 4 & 0xF);
        if (l != 0)
          l2 -= this.offsets[l];
      }
      if (l2 < paramLong)
        i = k + 1;
      else if (l2 > paramLong)
        j = k - 1;
      else
        return k;
    }
    if (i >= this.transitions.length)
      return i;
    return (i - 1);
  }

  public int getOffset(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    if ((paramInt6 < 0) || (paramInt6 >= 86400000))
      throw new IllegalArgumentException();
    if (paramInt1 == 0)
      paramInt2 = 1 - paramInt2;
    else if (paramInt1 != 1)
      throw new IllegalArgumentException();
    CalendarDate localCalendarDate = gcal.newCalendarDate(null);
    localCalendarDate.setDate(paramInt2, paramInt3 + 1, paramInt4);
    if (!(gcal.validate(localCalendarDate)))
      throw new IllegalArgumentException();
    if ((paramInt5 < 1) || (paramInt5 > 7))
      throw new IllegalArgumentException();
    if (this.transitions == null)
      return getLastRawOffset();
    long l = gcal.getTime(localCalendarDate) + paramInt6;
    l -= this.rawOffset;
    return getOffsets(l, null, 0);
  }

  public synchronized void setRawOffset(int paramInt)
  {
    if (paramInt == this.rawOffset + this.rawOffsetDiff)
      return;
    this.rawOffsetDiff = (paramInt - this.rawOffset);
    if (this.lastRule != null)
      this.lastRule.setRawOffset(paramInt);
    this.dirty = true;
  }

  public int getRawOffset()
  {
    if (!(this.willGMTOffsetChange))
      return (this.rawOffset + this.rawOffsetDiff);
    int[] arrayOfInt = new int[2];
    getOffsets(System.currentTimeMillis(), arrayOfInt, 0);
    return arrayOfInt[0];
  }

  public boolean isDirty()
  {
    return this.dirty;
  }

  private int getLastRawOffset()
  {
    return (this.rawOffset + this.rawOffsetDiff);
  }

  public boolean useDaylightTime()
  {
    return (this.simpleTimeZoneParams != null);
  }

  public boolean inDaylightTime(Date paramDate)
  {
    if (paramDate == null)
      throw new NullPointerException();
    if (this.transitions == null)
      return false;
    long l = paramDate.getTime() - this.rawOffsetDiff;
    int i = getTransitionIndex(l, 0);
    if (i < 0)
      return false;
    if (i < this.transitions.length)
      return ((this.transitions[i] & 0xF0) != 3412047600491167744L);
    SimpleTimeZone localSimpleTimeZone = getLastRule();
    if (localSimpleTimeZone != null)
      return localSimpleTimeZone.inDaylightTime(paramDate);
    return false;
  }

  public int getDSTSavings()
  {
    return this.dstSavings;
  }

  public String toString()
  {
    return getClass().getName() + "[id=\"" + getID() + "\"" + ",offset=" + getLastRawOffset() + ",dstSavings=" + this.dstSavings + ",useDaylight=" + useDaylightTime() + ",transitions=" + ((this.transitions != null) ? this.transitions.length : 0) + ",lastRule=" + ((this.lastRule == null) ? getLastRuleInstance() : this.lastRule) + "]";
  }

  public static String[] getAvailableIDs()
  {
    Object localObject1 = ZoneInfoFile.getZoneIDs();
    List localList = ZoneInfoFile.getExcludedZones();
    if (localList != null)
    {
      localObject2 = new ArrayList(((List)localObject1).size() + localList.size());
      ((List)localObject2).addAll((Collection)localObject1);
      ((List)localObject2).addAll(localList);
      localObject1 = localObject2;
    }
    Object localObject2 = new String[((List)localObject1).size()];
    return ((String)(String)(String[])((List)localObject1).toArray(localObject2));
  }

  public static String[] getAvailableIDs(int paramInt)
  {
    Object localObject;
    ArrayList localArrayList = new ArrayList();
    List localList1 = ZoneInfoFile.getZoneIDs();
    int[] arrayOfInt = ZoneInfoFile.getRawOffsets();
    for (int i = 0; i < arrayOfInt.length; ++i)
      if (arrayOfInt[i] == paramInt)
      {
        localObject = ZoneInfoFile.getRawOffsetIndices();
        for (int j = 0; j < localObject.length; ++j)
          if (localObject[j] == i)
          {
            localArrayList.add(localList1.get(j++));
            while (true)
            {
              if ((j >= localObject.length) || (localObject[j] != i))
                break label132;
              localArrayList.add(localList1.get(j++));
            }
          }
      }
    label132: List localList2 = ZoneInfoFile.getExcludedZones();
    if (localList2 != null)
    {
      localObject = localList2.iterator();
      while (((Iterator)localObject).hasNext())
      {
        String str = (String)((Iterator)localObject).next();
        TimeZone localTimeZone = getTimeZone(str);
        if ((localTimeZone != null) && (localTimeZone.getRawOffset() == paramInt))
          localArrayList.add(str);
      }
    }
    String[] arrayOfString = new String[localArrayList.size()];
    localArrayList.toArray(arrayOfString);
    return ((String)arrayOfString);
  }

  public static TimeZone getTimeZone(String paramString)
  {
    String str1 = null;
    if (USE_OLDMAPPING)
    {
      localObject = (String)TzIDOldMapping.MAP.get(paramString);
      if (localObject != null)
      {
        str1 = paramString;
        paramString = (String)localObject;
      }
    }
    Object localObject = ZoneInfoFile.getZoneInfo(paramString);
    if (localObject == null)
      try
      {
        Map localMap = getAliasTable();
        String str2 = paramString;
        do
        {
          if ((str2 = (String)localMap.get(str2)) == null)
            break label94;
          localObject = ZoneInfoFile.getZoneInfo(str2);
        }
        while (localObject == null);
        ((ZoneInfo)localObject).setID(paramString);
        localObject = ZoneInfoFile.addToCache(paramString, (ZoneInfo)localObject);
        localObject = (ZoneInfo)((ZoneInfo)localObject).clone();
        label94: break label94:
      }
      catch (Exception localException)
      {
      }
    if ((str1 != null) && (localObject != null))
      ((ZoneInfo)localObject).setID(str1);
    return ((TimeZone)localObject);
  }

  private synchronized SimpleTimeZone getLastRule()
  {
    if (this.lastRule == null)
      this.lastRule = getLastRuleInstance();
    return this.lastRule;
  }

  public SimpleTimeZone getLastRuleInstance()
  {
    if (this.simpleTimeZoneParams == null)
      return null;
    if (this.simpleTimeZoneParams.length == 10)
      return new SimpleTimeZone(getLastRawOffset(), getID(), this.simpleTimeZoneParams[0], this.simpleTimeZoneParams[1], this.simpleTimeZoneParams[2], this.simpleTimeZoneParams[3], this.simpleTimeZoneParams[4], this.simpleTimeZoneParams[5], this.simpleTimeZoneParams[6], this.simpleTimeZoneParams[7], this.simpleTimeZoneParams[8], this.simpleTimeZoneParams[9], this.dstSavings);
    return new SimpleTimeZone(getLastRawOffset(), getID(), this.simpleTimeZoneParams[0], this.simpleTimeZoneParams[1], this.simpleTimeZoneParams[2], this.simpleTimeZoneParams[3], this.simpleTimeZoneParams[4], this.simpleTimeZoneParams[5], this.simpleTimeZoneParams[6], this.simpleTimeZoneParams[7], this.dstSavings);
  }

  public Object clone()
  {
    ZoneInfo localZoneInfo = (ZoneInfo)super.clone();
    localZoneInfo.lastRule = null;
    return localZoneInfo;
  }

  public int hashCode()
  {
    return (getLastRawOffset() ^ this.checksum);
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof ZoneInfo))
      return false;
    ZoneInfo localZoneInfo = (ZoneInfo)paramObject;
    return ((getID().equals(localZoneInfo.getID())) && (getLastRawOffset() == localZoneInfo.getLastRawOffset()) && (this.checksum == localZoneInfo.checksum));
  }

  public boolean hasSameRules(TimeZone paramTimeZone)
  {
    if (this == paramTimeZone)
      return true;
    if (paramTimeZone == null)
      return false;
    if (!(paramTimeZone instanceof ZoneInfo))
    {
      if (getRawOffset() != paramTimeZone.getRawOffset())
        return false;
      return ((this.transitions == null) && (!(useDaylightTime())) && (!(paramTimeZone.useDaylightTime())));
    }
    if (getLastRawOffset() != ((ZoneInfo)paramTimeZone).getLastRawOffset())
      return false;
    return (this.checksum == ((ZoneInfo)paramTimeZone).checksum);
  }

  public static synchronized Map<String, String> getAliasTable()
  {
    Map localMap = null;
    SoftReference localSoftReference = aliasTable;
    if (localSoftReference != null)
    {
      localMap = (Map)localSoftReference.get();
      if (localMap != null)
        return localMap;
    }
    localMap = ZoneInfoFile.getZoneAliases();
    if (localMap != null)
      aliasTable = new SoftReference(localMap);
    return localMap;
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    this.dirty = true;
  }

  static
  {
    String str = ((String)AccessController.doPrivileged(new GetPropertyAction("sun.timezone.ids.oldmapping", "false"))).toLowerCase(Locale.ROOT);
    USE_OLDMAPPING = (str.equals("yes")) || (str.equals("true"));
    gcal = CalendarSystem.getGregorianCalendar();
  }
}
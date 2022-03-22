package sun.util.calendar;

import B;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.List<Ljava.lang.String;>;
import java.util.Map;
import sun.security.action.GetPropertyAction;

public class ZoneInfoFile
{
  public static final byte[] JAVAZI_LABEL = { 106, 97, 118, 97, 122, 105, 0 };
  private static final int JAVAZI_LABEL_LENGTH = JAVAZI_LABEL.length;
  public static final byte JAVAZI_VERSION = 1;
  public static final byte TAG_RawOffset = 1;
  public static final byte TAG_LastDSTSaving = 2;
  public static final byte TAG_CRC32 = 3;
  public static final byte TAG_Transition = 4;
  public static final byte TAG_Offset = 5;
  public static final byte TAG_SimpleTimeZone = 6;
  public static final byte TAG_GMTOffsetWillChange = 7;
  public static final String JAVAZM_FILE_NAME = "ZoneInfoMappings";
  public static final byte[] JAVAZM_LABEL = { 106, 97, 118, 97, 122, 109, 0 };
  private static final int JAVAZM_LABEL_LENGTH = JAVAZM_LABEL.length;
  public static final byte JAVAZM_VERSION = 1;
  public static final byte TAG_ZoneIDs = 64;
  public static final byte TAG_RawOffsets = 65;
  public static final byte TAG_RawOffsetIndices = 66;
  public static final byte TAG_ZoneAliases = 67;
  public static final byte TAG_TZDataVersion = 68;
  public static final byte TAG_ExcludedZones = 69;
  private static Map<String, ZoneInfo> zoneInfoObjects = null;
  private static volatile SoftReference<List<String>> zoneIDs = null;
  private static volatile SoftReference<List<String>> excludedIDs = null;
  private static volatile boolean hasNoExcludeList = false;
  private static volatile SoftReference<byte[]> rawOffsetIndices = null;
  private static volatile SoftReference<int[]> rawOffsets = null;
  private static volatile SoftReference<byte[]> zoneInfoMappings = null;

  public static String getFileName(String paramString)
  {
    if (File.separatorChar == '/')
      return paramString;
    return paramString.replace('/', File.separatorChar);
  }

  public static ZoneInfo getCustomTimeZone(String paramString, int paramInt)
  {
    String str = toCustomID(paramInt);
    ZoneInfo localZoneInfo = getFromCache(str);
    if (localZoneInfo == null)
    {
      localZoneInfo = new ZoneInfo(str, paramInt);
      localZoneInfo = addToCache(str, localZoneInfo);
      if (!(str.equals(paramString)))
        localZoneInfo = addToCache(paramString, localZoneInfo);
    }
    return ((ZoneInfo)localZoneInfo.clone());
  }

  public static String toCustomID(int paramInt)
  {
    int i;
    int j = paramInt / 60000;
    if (j >= 0)
    {
      i = 43;
    }
    else
    {
      i = 45;
      j = -j;
    }
    int k = j / 60;
    int l = j % 60;
    char[] arrayOfChar = { 'G', 'M', 'T', i, '0', '0', ':', '0', '0' };
    if (k >= 10)
    {
      int tmp94_93 = 4;
      char[] tmp94_91 = arrayOfChar;
      tmp94_91[tmp94_93] = (char)(tmp94_91[tmp94_93] + k / 10);
    }
    int tmp106_105 = 5;
    char[] tmp106_103 = arrayOfChar;
    tmp106_103[tmp106_105] = (char)(tmp106_103[tmp106_105] + k % 10);
    if (l != 0)
    {
      char[] tmp124_120 = arrayOfChar;
      tmp124_120[7] = (char)(tmp124_120[7] + l / 10);
      char[] tmp138_134 = arrayOfChar;
      tmp138_134[8] = (char)(tmp138_134[8] + l % 10);
    }
    return new String(arrayOfChar);
  }

  public static ZoneInfo getZoneInfo(String paramString)
  {
    ZoneInfo localZoneInfo = getFromCache(paramString);
    if (localZoneInfo == null)
    {
      localZoneInfo = createZoneInfo(paramString);
      if (localZoneInfo == null)
        return null;
      localZoneInfo = addToCache(paramString, localZoneInfo);
    }
    return ((ZoneInfo)localZoneInfo.clone());
  }

  static synchronized ZoneInfo getFromCache(String paramString)
  {
    if (zoneInfoObjects == null)
      return null;
    return ((ZoneInfo)zoneInfoObjects.get(paramString));
  }

  static synchronized ZoneInfo addToCache(String paramString, ZoneInfo paramZoneInfo)
  {
    if (zoneInfoObjects == null)
    {
      zoneInfoObjects = new HashMap();
    }
    else
    {
      ZoneInfo localZoneInfo = (ZoneInfo)zoneInfoObjects.get(paramString);
      if (localZoneInfo != null)
        return localZoneInfo;
    }
    zoneInfoObjects.put(paramString, paramZoneInfo);
    return paramZoneInfo;
  }

  private static ZoneInfo createZoneInfo(String paramString)
  {
    byte[] arrayOfByte = readZoneInfoFile(getFileName(paramString));
    if (arrayOfByte == null)
      return null;
    for (int i = 0; i < JAVAZI_LABEL.length; ++i)
      if (arrayOfByte[i] != JAVAZI_LABEL[i])
      {
        System.err.println("ZoneInfo: wrong magic number: " + paramString);
        return null;
      }
    if (arrayOfByte[(i++)] > 1)
    {
      System.err.println("ZoneInfo: incompatible version (" + arrayOfByte[(i - 1)] + "): " + paramString);
      return null;
    }
    int j = arrayOfByte.length;
    int k = 0;
    int l = 0;
    int i1 = 0;
    boolean bool = false;
    long[] arrayOfLong = null;
    int[] arrayOfInt1 = null;
    int[] arrayOfInt2 = null;
    try
    {
      int i2;
      int i3;
      while (i < j)
      {
        int i4;
        int i5;
        int i6;
        i2 = arrayOfByte[(i++)];
        i3 = ((arrayOfByte[(i++)] & 0xFF) << 8) + (arrayOfByte[(i++)] & 0xFF);
        if (j < i + i3)
          break;
        switch (i2)
        {
        case 3:
          i4 = arrayOfByte[(i++)] & 0xFF;
          i4 = (i4 << 8) + (arrayOfByte[(i++)] & 0xFF);
          i4 = (i4 << 8) + (arrayOfByte[(i++)] & 0xFF);
          i4 = (i4 << 8) + (arrayOfByte[(i++)] & 0xFF);
          i1 = i4;
          break;
        case 2:
          i4 = (short)(arrayOfByte[(i++)] & 0xFF);
          i4 = (short)((i4 << 8) + (arrayOfByte[(i++)] & 0xFF));
          l = i4 * 1000;
          break;
        case 1:
          i4 = arrayOfByte[(i++)] & 0xFF;
          i4 = (i4 << 8) + (arrayOfByte[(i++)] & 0xFF);
          i4 = (i4 << 8) + (arrayOfByte[(i++)] & 0xFF);
          i4 = (i4 << 8) + (arrayOfByte[(i++)] & 0xFF);
          k = i4;
          break;
        case 4:
          i4 = i3 / 8;
          arrayOfLong = new long[i4];
          for (i5 = 0; i5 < i4; ++i5)
          {
            long l1 = arrayOfByte[(i++)] & 0xFF;
            l1 = (l1 << 8) + (arrayOfByte[(i++)] & 0xFF);
            l1 = (l1 << 8) + (arrayOfByte[(i++)] & 0xFF);
            l1 = (l1 << 8) + (arrayOfByte[(i++)] & 0xFF);
            l1 = (l1 << 8) + (arrayOfByte[(i++)] & 0xFF);
            l1 = (l1 << 8) + (arrayOfByte[(i++)] & 0xFF);
            l1 = (l1 << 8) + (arrayOfByte[(i++)] & 0xFF);
            l1 = (l1 << 8) + (arrayOfByte[(i++)] & 0xFF);
            arrayOfLong[i5] = l1;
          }
          break;
        case 5:
          i4 = i3 / 4;
          arrayOfInt1 = new int[i4];
          for (i5 = 0; i5 < i4; ++i5)
          {
            i6 = arrayOfByte[(i++)] & 0xFF;
            i6 = (i6 << 8) + (arrayOfByte[(i++)] & 0xFF);
            i6 = (i6 << 8) + (arrayOfByte[(i++)] & 0xFF);
            i6 = (i6 << 8) + (arrayOfByte[(i++)] & 0xFF);
            arrayOfInt1[i5] = i6;
          }
          break;
        case 6:
          if ((i3 != 32) && (i3 != 40))
          {
            System.err.println("ZoneInfo: wrong SimpleTimeZone parameter size");
            return null;
          }
          i4 = i3 / 4;
          arrayOfInt2 = new int[i4];
          for (i5 = 0; i5 < i4; ++i5)
          {
            i6 = arrayOfByte[(i++)] & 0xFF;
            i6 = (i6 << 8) + (arrayOfByte[(i++)] & 0xFF);
            i6 = (i6 << 8) + (arrayOfByte[(i++)] & 0xFF);
            i6 = (i6 << 8) + (arrayOfByte[(i++)] & 0xFF);
            arrayOfInt2[i5] = i6;
          }
          break;
        case 7:
          if (i3 != 1)
            System.err.println("ZoneInfo: wrong byte length for TAG_GMTOffsetWillChange");
          bool = arrayOfByte[(i++)] == 1;
          break;
        default:
          System.err.println("ZoneInfo: unknown tag < " + i2 + ">. ignored.");
          i += i3;
        }
      }
    }
    catch (Exception localException)
    {
      System.err.println("ZoneInfo: corrupted zoneinfo file: " + paramString);
      return null;
    }
    if (i != j)
    {
      System.err.println("ZoneInfo: wrong file size: " + paramString);
      return null;
    }
    return new ZoneInfo(paramString, k, l, i1, arrayOfLong, arrayOfInt1, arrayOfInt2, bool);
  }

  static List<String> getZoneIDs()
  {
    Object localObject = null;
    SoftReference localSoftReference = zoneIDs;
    if (localSoftReference != null)
    {
      localObject = (List)localSoftReference.get();
      if (localObject != null)
        return localObject;
    }
    byte[] arrayOfByte = null;
    arrayOfByte = getZoneInfoMappings();
    int i = JAVAZM_LABEL_LENGTH + 1;
    int j = arrayOfByte.length;
    try
    {
      int l;
      while (i < j)
      {
        int k = arrayOfByte[(i++)];
        l = ((arrayOfByte[(i++)] & 0xFF) << 8) + (arrayOfByte[(i++)] & 0xFF);
        switch (k)
        {
        case 64:
          int i1 = (arrayOfByte[(i++)] << 8) + (arrayOfByte[(i++)] & 0xFF);
          localObject = new ArrayList(i1);
          for (int i2 = 0; i2 < i1; ++i2)
          {
            int i3 = arrayOfByte[(i++)];
            ((List)localObject).add(new String(arrayOfByte, i, i3, "UTF-8"));
            i += i3;
          }
          break;
        default:
          i += l;
        }
      }
    }
    catch (Exception localException)
    {
      System.err.println("ZoneInfo: corrupted ZoneInfoMappings");
    }
    zoneIDs = new SoftReference(localObject);
    return ((List<String>)localObject);
  }

  static Map<String, String> getZoneAliases()
  {
    byte[] arrayOfByte = getZoneInfoMappings();
    int i = JAVAZM_LABEL_LENGTH + 1;
    int j = arrayOfByte.length;
    HashMap localHashMap = null;
    try
    {
      int l;
      while (i < j)
      {
        int k = arrayOfByte[(i++)];
        l = ((arrayOfByte[(i++)] & 0xFF) << 8) + (arrayOfByte[(i++)] & 0xFF);
        switch (k)
        {
        case 67:
          int i1 = (arrayOfByte[(i++)] << 8) + (arrayOfByte[(i++)] & 0xFF);
          localHashMap = new HashMap(i1);
          for (int i2 = 0; i2 < i1; ++i2)
          {
            int i3 = arrayOfByte[(i++)];
            String str1 = new String(arrayOfByte, i, i3, "UTF-8");
            i += i3;
            i3 = arrayOfByte[(i++)];
            String str2 = new String(arrayOfByte, i, i3, "UTF-8");
            i += i3;
            localHashMap.put(str1, str2);
          }
          break;
        default:
          i += l;
        }
      }
    }
    catch (Exception localException)
    {
      System.err.println("ZoneInfo: corrupted ZoneInfoMappings");
      return null;
    }
    return localHashMap;
  }

  static List<String> getExcludedZones()
  {
    if (hasNoExcludeList)
      return null;
    Object localObject = null;
    SoftReference localSoftReference = excludedIDs;
    if (localSoftReference != null)
    {
      localObject = (List)localSoftReference.get();
      if (localObject != null)
        return localObject;
    }
    byte[] arrayOfByte = getZoneInfoMappings();
    int i = JAVAZM_LABEL_LENGTH + 1;
    int j = arrayOfByte.length;
    try
    {
      int l;
      while (i < j)
      {
        int k = arrayOfByte[(i++)];
        l = ((arrayOfByte[(i++)] & 0xFF) << 8) + (arrayOfByte[(i++)] & 0xFF);
        switch (k)
        {
        case 69:
          int i1 = (arrayOfByte[(i++)] << 8) + (arrayOfByte[(i++)] & 0xFF);
          localObject = new ArrayList();
          for (int i2 = 0; i2 < i1; ++i2)
          {
            int i3 = arrayOfByte[(i++)];
            String str = new String(arrayOfByte, i, i3, "UTF-8");
            i += i3;
            ((List)localObject).add(str);
          }
          break;
        default:
          i += l;
        }
      }
    }
    catch (Exception localException)
    {
      System.err.println("ZoneInfo: corrupted ZoneInfoMappings");
      return null;
    }
    if (localObject != null)
      excludedIDs = new SoftReference(localObject);
    else
      hasNoExcludeList = true;
    return ((List<String>)localObject);
  }

  static byte[] getRawOffsetIndices()
  {
    byte[] arrayOfByte1 = null;
    SoftReference localSoftReference = rawOffsetIndices;
    if (localSoftReference != null)
    {
      arrayOfByte1 = (byte[])localSoftReference.get();
      if (arrayOfByte1 != null)
        return arrayOfByte1;
    }
    byte[] arrayOfByte2 = getZoneInfoMappings();
    int i = JAVAZM_LABEL_LENGTH + 1;
    int j = arrayOfByte2.length;
    try
    {
      int l;
      while (i < j)
      {
        int k = arrayOfByte2[(i++)];
        l = ((arrayOfByte2[(i++)] & 0xFF) << 8) + (arrayOfByte2[(i++)] & 0xFF);
        switch (k)
        {
        case 66:
          arrayOfByte1 = new byte[l];
          for (int i1 = 0; i1 < l; ++i1)
            arrayOfByte1[i1] = arrayOfByte2[(i++)];
          break;
        default:
          i += l;
        }
      }
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
      System.err.println("ZoneInfo: corrupted ZoneInfoMappings");
    }
    rawOffsetIndices = new SoftReference(arrayOfByte1);
    return arrayOfByte1;
  }

  static int[] getRawOffsets()
  {
    int[] arrayOfInt = null;
    SoftReference localSoftReference = rawOffsets;
    if (localSoftReference != null)
    {
      arrayOfInt = (int[])localSoftReference.get();
      if (arrayOfInt != null)
        return arrayOfInt;
    }
    byte[] arrayOfByte = getZoneInfoMappings();
    int i = JAVAZM_LABEL_LENGTH + 1;
    int j = arrayOfByte.length;
    try
    {
      int l;
      while (i < j)
      {
        int k = arrayOfByte[(i++)];
        l = ((arrayOfByte[(i++)] & 0xFF) << 8) + (arrayOfByte[(i++)] & 0xFF);
        switch (k)
        {
        case 65:
          int i1 = l / 4;
          arrayOfInt = new int[i1];
          for (int i2 = 0; i2 < i1; ++i2)
          {
            int i3 = arrayOfByte[(i++)] & 0xFF;
            i3 = (i3 << 8) + (arrayOfByte[(i++)] & 0xFF);
            i3 = (i3 << 8) + (arrayOfByte[(i++)] & 0xFF);
            i3 = (i3 << 8) + (arrayOfByte[(i++)] & 0xFF);
            arrayOfInt[i2] = i3;
          }
          break;
        default:
          i += l;
        }
      }
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
      System.err.println("ZoneInfo: corrupted ZoneInfoMappings");
    }
    rawOffsets = new SoftReference(arrayOfInt);
    return arrayOfInt;
  }

  private static byte[] getZoneInfoMappings()
  {
    SoftReference localSoftReference = zoneInfoMappings;
    if (localSoftReference != null)
    {
      arrayOfByte = (byte[])localSoftReference.get();
      if (arrayOfByte != null)
        return arrayOfByte;
    }
    byte[] arrayOfByte = readZoneInfoFile("ZoneInfoMappings");
    if (arrayOfByte == null)
      return null;
    for (int i = 0; i < JAVAZM_LABEL.length; ++i)
      if (arrayOfByte[i] != JAVAZM_LABEL[i])
      {
        System.err.println("ZoneInfo: wrong magic number: ZoneInfoMappings");
        return null;
      }
    if (arrayOfByte[(i++)] > 1)
    {
      System.err.println("ZoneInfo: incompatible version (" + arrayOfByte[(i - 1)] + "): " + "ZoneInfoMappings");
      return null;
    }
    zoneInfoMappings = new SoftReference(arrayOfByte);
    return arrayOfByte;
  }

  private static byte[] readZoneInfoFile(String paramString)
  {
    Object localObject;
    byte[] arrayOfByte = null;
    try
    {
      String str = (String)AccessController.doPrivileged(new GetPropertyAction("java.home"));
      localObject = str + File.separator + "lib" + File.separator + "zi" + File.separator + paramString;
      arrayOfByte = (byte[])(byte[])AccessController.doPrivileged(new PrivilegedExceptionAction((String)localObject)
      {
        public Object run()
          throws IOException
        {
          File localFile = new File(this.val$fname);
          int i = (int)localFile.length();
          byte[] arrayOfByte = new byte[i];
          FileInputStream localFileInputStream = new FileInputStream(localFile);
          if (localFileInputStream.read(arrayOfByte) != i)
          {
            localFileInputStream.close();
            throw new IOException("read error on " + this.val$fname);
          }
          localFileInputStream.close();
          return arrayOfByte;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      localObject = localPrivilegedActionException.getException();
      if ((!(localObject instanceof FileNotFoundException)) || ("ZoneInfoMappings".equals(paramString)))
        System.err.println("ZoneInfo: " + ((Exception)localObject).getMessage());
    }
    return ((B)arrayOfByte);
  }
}
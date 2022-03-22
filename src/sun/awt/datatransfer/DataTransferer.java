package sun.awt.datatransfer;

import B;
import java.awt.AWTError;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.FlavorTable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import sun.awt.AppContext;
import sun.awt.DebugHelper;
import sun.awt.SunToolkit;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.ToolkitImage;

public abstract class DataTransferer
{
  private static final DebugHelper dbg = DebugHelper.create(DataTransferer.class);
  public static final Class charArrayClass;
  public static final Class byteArrayClass;
  public static final DataFlavor plainTextStringFlavor;
  public static final DataFlavor javaTextEncodingFlavor;
  private static SortedSet standardEncodings;
  private static final java.util.Map textMIMESubtypeCharsetSupport;
  private static String defaultEncoding;
  private static final Set textNatives = Collections.synchronizedSet(new HashSet());
  private static final java.util.Map nativeCharsets = Collections.synchronizedMap(new HashMap());
  private static final java.util.Map nativeEOLNs = Collections.synchronizedMap(new HashMap());
  private static final java.util.Map nativeTerminators = Collections.synchronizedMap(new HashMap());
  private static final String DATA_CONVERTER_KEY = "DATA_CONVERTER_KEY";
  private static DataTransferer transferer;
  private static final String[] DEPLOYMENT_CACHE_PROPERTIES;
  private static final ArrayList deploymentCacheDirectoryList;
  private static CharsetComparator defaultCharsetComparator;
  private static DataFlavorComparator defaultFlavorComparator;

  public static DataTransferer getInstance()
  {
    if (transferer == null)
      synchronized (DataTransferer.class)
      {
        if (transferer == null)
        {
          String str = SunToolkit.getDataTransfererClassName();
          if (str != null)
          {
            1 local1 = new PrivilegedAction(str)
            {
              public Object run()
              {
                Class localClass = null;
                Method localMethod = null;
                Object localObject = null;
                try
                {
                  localClass = Class.forName(this.val$name);
                }
                catch (ClassNotFoundException localClassNotFoundException1)
                {
                  ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
                  if (localClassLoader != null)
                    try
                    {
                      localClass = localClassLoader.loadClass(this.val$name);
                    }
                    catch (ClassNotFoundException localClassNotFoundException2)
                    {
                      localClassNotFoundException2.printStackTrace();
                      throw new AWTError("DataTransferer not found: " + this.val$name);
                    }
                }
                if (localClass != null)
                  try
                  {
                    localMethod = localClass.getDeclaredMethod("getInstanceImpl", new Class[0]);
                    localMethod.setAccessible(true);
                  }
                  catch (NoSuchMethodException localNoSuchMethodException)
                  {
                    localNoSuchMethodException.printStackTrace();
                    throw new AWTError("Cannot instantiate DataTransferer: " + this.val$name);
                  }
                  catch (SecurityException localSecurityException)
                  {
                    localSecurityException.printStackTrace();
                    throw new AWTError("Access is denied for DataTransferer: " + this.val$name);
                  }
                if (localMethod != null)
                  try
                  {
                    localObject = localMethod.invoke(null, new Object[0]);
                  }
                  catch (InvocationTargetException localInvocationTargetException)
                  {
                    localInvocationTargetException.printStackTrace();
                    throw new AWTError("Cannot instantiate DataTransferer: " + this.val$name);
                  }
                  catch (IllegalAccessException localIllegalAccessException)
                  {
                    localIllegalAccessException.printStackTrace();
                    throw new AWTError("Cannot access DataTransferer: " + this.val$name);
                  }
                return localObject;
              }
            };
            transferer = (DataTransferer)AccessController.doPrivileged(local1);
          }
        }
      }
    return transferer;
  }

  public static String canonicalName(String paramString)
  {
    if (paramString == null)
      return null;
    try
    {
      return Charset.forName(paramString).name();
    }
    catch (IllegalCharsetNameException localIllegalCharsetNameException)
    {
      return paramString;
    }
    catch (UnsupportedCharsetException localUnsupportedCharsetException)
    {
    }
    return paramString;
  }

  public static String getTextCharset(DataFlavor paramDataFlavor)
  {
    if (!(isFlavorCharsetTextType(paramDataFlavor)))
      return null;
    String str = paramDataFlavor.getParameter("charset");
    return ((str != null) ? str : getDefaultTextCharset());
  }

  public static String getDefaultTextCharset()
  {
    if (defaultEncoding != null)
      return defaultEncoding;
    return (DataTransferer.defaultEncoding = Charset.defaultCharset().name());
  }

  public static boolean doesSubtypeSupportCharset(DataFlavor paramDataFlavor)
  {
    String str = paramDataFlavor.getSubType();
    if (str == null)
      return false;
    Object localObject = textMIMESubtypeCharsetSupport.get(str);
    if (localObject != null)
      return (localObject == Boolean.TRUE);
    int i = (paramDataFlavor.getParameter("charset") != null) ? 1 : 0;
    textMIMESubtypeCharsetSupport.put(str, (i != 0) ? Boolean.TRUE : Boolean.FALSE);
    return i;
  }

  public static boolean doesSubtypeSupportCharset(String paramString1, String paramString2)
  {
    Object localObject = textMIMESubtypeCharsetSupport.get(paramString1);
    if (localObject != null)
      return (localObject == Boolean.TRUE);
    int i = (paramString2 != null) ? 1 : 0;
    textMIMESubtypeCharsetSupport.put(paramString1, (i != 0) ? Boolean.TRUE : Boolean.FALSE);
    return i;
  }

  public static boolean isFlavorCharsetTextType(DataFlavor paramDataFlavor)
  {
    if (DataFlavor.stringFlavor.equals(paramDataFlavor))
      return true;
    if ((!("text".equals(paramDataFlavor.getPrimaryType()))) || (!(doesSubtypeSupportCharset(paramDataFlavor))))
      return false;
    Class localClass = paramDataFlavor.getRepresentationClass();
    if ((paramDataFlavor.isRepresentationClassReader()) || (String.class.equals(localClass)) || (paramDataFlavor.isRepresentationClassCharBuffer()) || (charArrayClass.equals(localClass)))
      return true;
    if ((!(paramDataFlavor.isRepresentationClassInputStream())) && (!(paramDataFlavor.isRepresentationClassByteBuffer())) && (!(byteArrayClass.equals(localClass))))
      return false;
    String str = paramDataFlavor.getParameter("charset");
    return ((str != null) ? isEncodingSupported(str) : true);
  }

  public static boolean isFlavorNoncharsetTextType(DataFlavor paramDataFlavor)
  {
    if ((!("text".equals(paramDataFlavor.getPrimaryType()))) || (doesSubtypeSupportCharset(paramDataFlavor)))
      return false;
    return ((paramDataFlavor.isRepresentationClassInputStream()) || (paramDataFlavor.isRepresentationClassByteBuffer()) || (byteArrayClass.equals(paramDataFlavor.getRepresentationClass())));
  }

  public static boolean isEncodingSupported(String paramString)
  {
    if (paramString == null)
      return false;
    try
    {
      return Charset.isSupported(paramString);
    }
    catch (IllegalCharsetNameException localIllegalCharsetNameException)
    {
    }
    return false;
  }

  public static Iterator standardEncodings()
  {
    if (standardEncodings == null)
    {
      TreeSet localTreeSet = new TreeSet(defaultCharsetComparator);
      localTreeSet.add("US-ASCII");
      localTreeSet.add("ISO-8859-1");
      localTreeSet.add("UTF-8");
      localTreeSet.add("UTF-16BE");
      localTreeSet.add("UTF-16LE");
      localTreeSet.add("UTF-16");
      localTreeSet.add(getDefaultTextCharset());
      standardEncodings = Collections.unmodifiableSortedSet(localTreeSet);
    }
    return standardEncodings.iterator();
  }

  public static FlavorTable adaptFlavorMap(FlavorMap paramFlavorMap)
  {
    if (paramFlavorMap instanceof FlavorTable)
      return ((FlavorTable)paramFlavorMap);
    return new FlavorTable(paramFlavorMap)
    {
      public java.util.Map getNativesForFlavors(DataFlavor[] paramArrayOfDataFlavor)
      {
        return this.val$map.getNativesForFlavors(paramArrayOfDataFlavor);
      }

      public java.util.Map getFlavorsForNatives(String[] paramArrayOfString)
      {
        return this.val$map.getFlavorsForNatives(paramArrayOfString);
      }

      public List getNativesForFlavor(DataFlavor paramDataFlavor)
      {
        java.util.Map localMap = getNativesForFlavors(new DataFlavor[] { paramDataFlavor });
        String str = (String)localMap.get(paramDataFlavor);
        if (str != null)
        {
          ArrayList localArrayList = new ArrayList(1);
          localArrayList.add(str);
          return localArrayList;
        }
        return Collections.EMPTY_LIST;
      }

      public List getFlavorsForNative(String paramString)
      {
        java.util.Map localMap = getFlavorsForNatives(new String[] { paramString });
        DataFlavor localDataFlavor = (DataFlavor)localMap.get(paramString);
        if (localDataFlavor != null)
        {
          ArrayList localArrayList = new ArrayList(1);
          localArrayList.add(localDataFlavor);
          return localArrayList;
        }
        return Collections.EMPTY_LIST;
      }
    };
  }

  public abstract String getDefaultUnicodeEncoding();

  public void registerTextFlavorProperties(String paramString1, String paramString2, String paramString3, String paramString4)
  {
    Long localLong = getFormatForNativeAsLong(paramString1);
    textNatives.add(localLong);
    nativeCharsets.put(localLong, ((paramString2 != null) && (paramString2.length() != 0)) ? paramString2 : getDefaultTextCharset());
    if ((paramString3 != null) && (paramString3.length() != 0) && (!(paramString3.equals("\n"))))
      nativeEOLNs.put(localLong, paramString3);
    if ((paramString4 != null) && (paramString4.length() != 0))
    {
      Integer localInteger = Integer.valueOf(paramString4);
      if (localInteger.intValue() > 0)
        nativeTerminators.put(localLong, localInteger);
    }
  }

  protected boolean isTextFormat(long paramLong)
  {
    return textNatives.contains(Long.valueOf(paramLong));
  }

  protected String getCharsetForTextFormat(Long paramLong)
  {
    return ((String)nativeCharsets.get(paramLong));
  }

  public abstract boolean isLocaleDependentTextFormat(long paramLong);

  public abstract boolean isFileFormat(long paramLong);

  public abstract boolean isImageFormat(long paramLong);

  public SortedMap getFormatsForTransferable(Transferable paramTransferable, FlavorTable paramFlavorTable)
  {
    DataFlavor[] arrayOfDataFlavor = paramTransferable.getTransferDataFlavors();
    if (arrayOfDataFlavor == null)
      return new TreeMap();
    return getFormatsForFlavors(arrayOfDataFlavor, paramFlavorTable);
  }

  public SortedMap getFormatsForFlavor(DataFlavor paramDataFlavor, FlavorTable paramFlavorTable)
  {
    return getFormatsForFlavors(new DataFlavor[] { paramDataFlavor }, paramFlavorTable);
  }

  public SortedMap getFormatsForFlavors(DataFlavor[] paramArrayOfDataFlavor, FlavorTable paramFlavorTable)
  {
    HashMap localHashMap1 = new HashMap(paramArrayOfDataFlavor.length);
    HashMap localHashMap2 = new HashMap(paramArrayOfDataFlavor.length);
    HashMap localHashMap3 = new HashMap(paramArrayOfDataFlavor.length);
    HashMap localHashMap4 = new HashMap(paramArrayOfDataFlavor.length);
    int i = 0;
    for (int j = paramArrayOfDataFlavor.length - 1; j >= 0; --j)
    {
      localObject = paramArrayOfDataFlavor[j];
      if (localObject == null)
        break label288:
      if ((((DataFlavor)localObject).isFlavorTextType()) || (((DataFlavor)localObject).isFlavorJavaFileListType()) || (DataFlavor.imageFlavor.equals((DataFlavor)localObject)) || (((DataFlavor)localObject).isRepresentationClassSerializable()) || (((DataFlavor)localObject).isRepresentationClassInputStream()) || (((DataFlavor)localObject).isRepresentationClassRemote()))
      {
        List localList = paramFlavorTable.getNativesForFlavor((DataFlavor)localObject);
        i += localList.size();
        Iterator localIterator = localList.iterator();
        while (localIterator.hasNext())
        {
          Long localLong = getFormatForNativeAsLong((String)localIterator.next());
          Integer localInteger = Integer.valueOf(i--);
          localHashMap1.put(localLong, localObject);
          localHashMap3.put(localLong, localInteger);
          if ((("text".equals(((DataFlavor)localObject).getPrimaryType())) && ("plain".equals(((DataFlavor)localObject).getSubType()))) || (((DataFlavor)localObject).equals(DataFlavor.stringFlavor)))
          {
            localHashMap2.put(localLong, localObject);
            localHashMap4.put(localLong, localInteger);
          }
        }
        label288: i += localList.size();
      }
    }
    localHashMap1.putAll(localHashMap2);
    localHashMap3.putAll(localHashMap4);
    IndexOrderComparator localIndexOrderComparator = new IndexOrderComparator(localHashMap3, false);
    Object localObject = new TreeMap(localIndexOrderComparator);
    ((SortedMap)localObject).putAll(localHashMap1);
    return ((SortedMap)localObject);
  }

  public long[] getFormatsForTransferableAsArray(Transferable paramTransferable, FlavorTable paramFlavorTable)
  {
    return keysToLongArray(getFormatsForTransferable(paramTransferable, paramFlavorTable));
  }

  public long[] getFormatsForFlavorAsArray(DataFlavor paramDataFlavor, FlavorTable paramFlavorTable)
  {
    return keysToLongArray(getFormatsForFlavor(paramDataFlavor, paramFlavorTable));
  }

  public long[] getFormatsForFlavorsAsArray(DataFlavor[] paramArrayOfDataFlavor, FlavorTable paramFlavorTable)
  {
    return keysToLongArray(getFormatsForFlavors(paramArrayOfDataFlavor, paramFlavorTable));
  }

  public java.util.Map getFlavorsForFormat(long paramLong, FlavorTable paramFlavorTable)
  {
    return getFlavorsForFormats(new long[] { paramLong }, paramFlavorTable);
  }

  public java.util.Map getFlavorsForFormats(long[] paramArrayOfLong, FlavorTable paramFlavorTable)
  {
    Object localObject1;
    Object localObject2;
    Object localObject3;
    HashMap localHashMap = new HashMap(paramArrayOfLong.length);
    HashSet localHashSet1 = new HashSet(paramArrayOfLong.length);
    HashSet localHashSet2 = new HashSet(paramArrayOfLong.length);
    for (int i = 0; i < paramArrayOfLong.length; ++i)
    {
      long l = paramArrayOfLong[i];
      localObject1 = getNativeForFormat(l);
      localObject2 = paramFlavorTable.getFlavorsForNative((String)localObject1);
      localObject3 = ((List)localObject2).iterator();
      while (((Iterator)localObject3).hasNext())
      {
        DataFlavor localDataFlavor2 = (DataFlavor)((Iterator)localObject3).next();
        if ((localDataFlavor2.isFlavorTextType()) || (localDataFlavor2.isFlavorJavaFileListType()) || (DataFlavor.imageFlavor.equals(localDataFlavor2)) || (localDataFlavor2.isRepresentationClassSerializable()) || (localDataFlavor2.isRepresentationClassInputStream()) || (localDataFlavor2.isRepresentationClassRemote()))
        {
          Long localLong = Long.valueOf(l);
          Object localObject4 = createMapping(localLong, localDataFlavor2);
          localHashMap.put(localDataFlavor2, localLong);
          localHashSet1.add(localObject4);
          localHashSet2.add(localDataFlavor2);
        }
      }
    }
    Iterator localIterator = localHashSet2.iterator();
    while (localIterator.hasNext())
    {
      DataFlavor localDataFlavor1 = (DataFlavor)localIterator.next();
      List localList = paramFlavorTable.getNativesForFlavor(localDataFlavor1);
      localObject1 = localList.iterator();
      while (((Iterator)localObject1).hasNext())
      {
        localObject2 = getFormatForNativeAsLong((String)((Iterator)localObject1).next());
        localObject3 = createMapping(localObject2, localDataFlavor1);
        if (localHashSet1.contains(localObject3))
        {
          localHashMap.put(localDataFlavor1, localObject2);
          break;
        }
      }
    }
    return ((java.util.Map)(java.util.Map)(java.util.Map)localHashMap);
  }

  public Set getFlavorsForFormatsAsSet(long[] paramArrayOfLong, FlavorTable paramFlavorTable)
  {
    HashSet localHashSet = new HashSet(paramArrayOfLong.length);
    for (int i = 0; i < paramArrayOfLong.length; ++i)
    {
      String str = getNativeForFormat(paramArrayOfLong[i]);
      List localList = paramFlavorTable.getFlavorsForNative(str);
      Iterator localIterator = localList.iterator();
      while (localIterator.hasNext())
      {
        DataFlavor localDataFlavor = (DataFlavor)localIterator.next();
        if ((localDataFlavor.isFlavorTextType()) || (localDataFlavor.isFlavorJavaFileListType()) || (DataFlavor.imageFlavor.equals(localDataFlavor)) || (localDataFlavor.isRepresentationClassSerializable()) || (localDataFlavor.isRepresentationClassInputStream()) || (localDataFlavor.isRepresentationClassRemote()))
          localHashSet.add(localDataFlavor);
      }
    }
    return localHashSet;
  }

  public DataFlavor[] getFlavorsForFormatAsArray(long paramLong, FlavorTable paramFlavorTable)
  {
    return getFlavorsForFormatsAsArray(new long[] { paramLong }, paramFlavorTable);
  }

  public DataFlavor[] getFlavorsForFormatsAsArray(long[] paramArrayOfLong, FlavorTable paramFlavorTable)
  {
    return setToSortedDataFlavorArray(getFlavorsForFormatsAsSet(paramArrayOfLong, paramFlavorTable));
  }

  private static Object createMapping(Object paramObject1, Object paramObject2)
  {
    return Arrays.asList(new Object[] { paramObject1, paramObject2 });
  }

  protected abstract Long getFormatForNativeAsLong(String paramString);

  protected abstract String getNativeForFormat(long paramLong);

  public byte[] translateTransferable(Transferable paramTransferable, DataFlavor paramDataFlavor, long paramLong)
    throws IOException
  {
    Object localObject1;
    int i;
    Object localObject3;
    int j;
    Object localObject5;
    Object localObject6;
    int i5;
    Object localObject8;
    try
    {
      localObject1 = paramTransferable.getTransferData(paramDataFlavor);
      if (localObject1 == null)
        return null;
      if ((paramDataFlavor.equals(DataFlavor.plainTextFlavor)) && (!(localObject1 instanceof InputStream)))
      {
        localObject1 = paramTransferable.getTransferData(DataFlavor.stringFlavor);
        if (localObject1 == null)
          return null;
        i = 1;
      }
      else
      {
        i = 0;
      }
    }
    catch (UnsupportedFlavorException localUnsupportedFlavorException)
    {
      throw new IOException(localUnsupportedFlavorException.getMessage());
    }
    if ((i != 0) || ((String.class.equals(paramDataFlavor.getRepresentationClass())) && (isFlavorCharsetTextType(paramDataFlavor)) && (isTextFormat(paramLong))))
    {
      localObject2 = (String)localObject1;
      localObject2 = removeSuspectedData(paramDataFlavor, paramTransferable, (String)localObject2);
      localObject3 = Long.valueOf(paramLong);
      String str = getCharsetForTextFormat((Long)localObject3);
      if (str == null)
        str = getDefaultTextCharset();
      localObject6 = (String)nativeEOLNs.get(localObject3);
      Integer localInteger = (Integer)nativeTerminators.get(localObject3);
      if (localObject6 != null)
      {
        int i3 = ((String)localObject2).length();
        StringBuffer localStringBuffer = new StringBuffer(i3 * 2);
        for (int i7 = 0; i7 < i3; ++i7)
          if (((String)localObject2).startsWith((String)localObject6, i7))
          {
            localStringBuffer.append((String)localObject6);
            i7 += ((String)localObject6).length() - 1;
          }
          else
          {
            char c = ((String)localObject2).charAt(i7);
            if (c == '\n')
              localStringBuffer.append((String)localObject6);
            else
              localStringBuffer.append(c);
          }
        localObject2 = localStringBuffer.toString();
      }
      Object localObject7 = ((String)localObject2).getBytes(str);
      if (localInteger != null)
      {
        int i6 = localInteger.intValue();
        byte[] arrayOfByte3 = new byte[localObject7.length + i6];
        System.arraycopy(localObject7, 0, arrayOfByte3, 0, localObject7.length);
        for (int i8 = localObject7.length; i8 < arrayOfByte3.length; ++i8)
          arrayOfByte3[i8] = 0;
        localObject7 = arrayOfByte3;
      }
      return localObject7;
    }
    if (paramDataFlavor.isRepresentationClassReader())
    {
      if ((!(isFlavorCharsetTextType(paramDataFlavor))) || (!(isTextFormat(paramLong))))
        throw new IOException("cannot transfer non-text data as Reader");
      localObject2 = (Reader)localObject1;
      localObject3 = new StringBuffer();
      while ((k = ((Reader)localObject2).read()) != -1)
      {
        int k;
        ((StringBuffer)localObject3).append((char)k);
      }
      ((Reader)localObject2).close();
      return translateTransferable(new StringSelection(((StringBuffer)localObject3).toString()), DataFlavor.plainTextFlavor, paramLong);
    }
    if (paramDataFlavor.isRepresentationClassCharBuffer())
    {
      if ((!(isFlavorCharsetTextType(paramDataFlavor))) || (!(isTextFormat(paramLong))))
        throw new IOException("cannot transfer non-text data as CharBuffer");
      localObject2 = (CharBuffer)localObject1;
      j = ((CharBuffer)localObject2).remaining();
      localObject5 = new char[j];
      ((CharBuffer)localObject2).get(localObject5, 0, j);
      return translateTransferable(new StringSelection(new String(localObject5)), DataFlavor.plainTextFlavor, paramLong);
    }
    if (charArrayClass.equals(paramDataFlavor.getRepresentationClass()))
    {
      if ((!(isFlavorCharsetTextType(paramDataFlavor))) || (!(isTextFormat(paramLong))))
        throw new IOException("cannot transfer non-text data as char array");
      return translateTransferable(new StringSelection(new String((char[])(char[])localObject1)), DataFlavor.plainTextFlavor, paramLong);
    }
    if (paramDataFlavor.isRepresentationClassByteBuffer())
    {
      localObject2 = (ByteBuffer)localObject1;
      j = ((ByteBuffer)localObject2).remaining();
      localObject5 = new byte[j];
      ((ByteBuffer)localObject2).get(localObject5, 0, j);
      if ((isFlavorCharsetTextType(paramDataFlavor)) && (isTextFormat(paramLong)))
      {
        localObject6 = getTextCharset(paramDataFlavor);
        return translateTransferable(new StringSelection(new String(localObject5, (String)localObject6)), DataFlavor.plainTextFlavor, paramLong);
      }
      return localObject5;
    }
    if (byteArrayClass.equals(paramDataFlavor.getRepresentationClass()))
    {
      localObject2 = (byte[])(byte[])localObject1;
      if ((isFlavorCharsetTextType(paramDataFlavor)) && (isTextFormat(paramLong)))
      {
        localObject4 = getTextCharset(paramDataFlavor);
        return translateTransferable(new StringSelection(new String(localObject2, (String)localObject4)), DataFlavor.plainTextFlavor, paramLong);
      }
      return localObject2;
    }
    if (DataFlavor.imageFlavor.equals(paramDataFlavor))
    {
      if (!(isImageFormat(paramLong)))
        throw new IOException("Data translation failed: not an image format");
      localObject2 = (Image)localObject1;
      localObject4 = imageToPlatformBytes((Image)localObject2, paramLong);
      if (localObject4 == null)
        throw new IOException("Data translation failed: cannot convert java image to native format");
      return localObject4;
    }
    Object localObject2 = new ByteArrayOutputStream();
    if (isFileFormat(paramLong))
    {
      if (!(DataFlavor.javaFileListFlavor.equals(paramDataFlavor)))
        throw new IOException("data translation failed");
      localObject4 = (List)localObject1;
      localObject5 = new ArrayList();
      localObject6 = getUserProtactionDomain(paramTransferable);
      int i2 = 0;
      for (int i4 = 0; i4 < ((List)localObject4).size(); ++i4)
      {
        localObject8 = ((List)localObject4).get(i4);
        if ((localObject8 instanceof File) || (localObject8 instanceof String))
          ++i2;
      }
      try
      {
        AccessController.doPrivileged(new PrivilegedExceptionAction(this, (List)localObject4, (ProtectionDomain)localObject6, (ArrayList)localObject5)
        {
          public Object run()
            throws IOException
          {
            for (int i = 0; i < this.val$list.size(); ++i)
            {
              Object localObject = this.val$list.get(i);
              File localFile = DataTransferer.access$000(this.this$0, localObject);
              if (null != System.getSecurityManager())
                if (!(DataTransferer.access$100(localFile)))
                  if (DataTransferer.access$200(this.this$0, localFile, this.val$userProtectionDomain));
              else
                this.val$fileList.add(localFile.getCanonicalPath());
            }
            return null;
          }
        });
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        throw new IOException(localPrivilegedActionException.getMessage());
      }
      for (i5 = 0; i5 < ((ArrayList)localObject5).size(); ++i5)
      {
        localObject8 = ((String)((ArrayList)localObject5).get(i5)).getBytes();
        ((ByteArrayOutputStream)localObject2).write(localObject8, 0, localObject8.length);
        ((ByteArrayOutputStream)localObject2).write(0);
      }
      ((ByteArrayOutputStream)localObject2).write(0);
    }
    else if (paramDataFlavor.isRepresentationClassInputStream())
    {
      localObject4 = (InputStream)localObject1;
      int l = 0;
      int i1 = ((InputStream)localObject4).available();
      byte[] arrayOfByte1 = new byte[(i1 > 8192) ? i1 : 8192];
      do
        if ((l = ((i5 = ((InputStream)localObject4).read(arrayOfByte1, 0, arrayOfByte1.length)) == -1) ? 1 : 0) == 0)
          ((ByteArrayOutputStream)localObject2).write(arrayOfByte1, 0, i5);
      while (l == 0);
      ((InputStream)localObject4).close();
      if ((isFlavorCharsetTextType(paramDataFlavor)) && (isTextFormat(paramLong)))
      {
        byte[] arrayOfByte2 = ((ByteArrayOutputStream)localObject2).toByteArray();
        ((ByteArrayOutputStream)localObject2).close();
        localObject8 = getTextCharset(paramDataFlavor);
        return translateTransferable(new StringSelection(new String(arrayOfByte2, (String)localObject8)), DataFlavor.plainTextFlavor, paramLong);
      }
    }
    else if (paramDataFlavor.isRepresentationClassRemote())
    {
      localObject4 = new MarshalledObject(localObject1);
      ObjectOutputStream localObjectOutputStream = new ObjectOutputStream((OutputStream)localObject2);
      localObjectOutputStream.writeObject(localObject4);
      localObjectOutputStream.close();
    }
    else if (paramDataFlavor.isRepresentationClassSerializable())
    {
      localObject4 = new ObjectOutputStream((OutputStream)localObject2);
      ((ObjectOutputStream)localObject4).writeObject(localObject1);
      ((ObjectOutputStream)localObject4).close();
    }
    else
    {
      throw new IOException("data translation failed");
    }
    Object localObject4 = ((ByteArrayOutputStream)localObject2).toByteArray();
    ((ByteArrayOutputStream)localObject2).close();
    return ((B)(B)(B)(B)(B)(B)(B)localObject4);
  }

  private String removeSuspectedData(DataFlavor paramDataFlavor, Transferable paramTransferable, String paramString)
    throws IOException
  {
    if ((null == System.getSecurityManager()) || (!(paramDataFlavor.isMimeTypeEqual("text/uri-list"))))
      return paramString;
    String str = "";
    ProtectionDomain localProtectionDomain = getUserProtactionDomain(paramTransferable);
    try
    {
      str = (String)AccessController.doPrivileged(new PrivilegedExceptionAction(this, paramString, localProtectionDomain)
      {
        public Object run()
        {
          StringBuffer localStringBuffer = new StringBuffer(this.val$str.length());
          for (int i = 0; i < this.val$str.split("(\\s)+").length; ++i)
          {
            String str = this.val$str.split("(\\s)+")[i];
            File localFile = new File(str);
            if ((!(localFile.exists())) || (!(DataTransferer.access$100(localFile))))
            {
              if (DataTransferer.access$200(this.this$0, localFile, this.val$userProtectionDomain))
                break label109:
              if (0 != localStringBuffer.length())
                localStringBuffer.append("\\r\\n");
              label109: localStringBuffer.append(str);
            }
          }
          return localStringBuffer.toString();
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw new IOException(localPrivilegedActionException.getMessage(), localPrivilegedActionException);
    }
    return str;
  }

  private static ProtectionDomain getUserProtactionDomain(Transferable paramTransferable)
  {
    return paramTransferable.getClass().getProtectionDomain();
  }

  private boolean isForbiddenToRead(File paramFile, ProtectionDomain paramProtectionDomain)
  {
    if (null == paramProtectionDomain)
      return false;
    try
    {
      FilePermission localFilePermission = new FilePermission(paramFile.getCanonicalPath(), "read, delete");
      if (paramProtectionDomain.implies(localFilePermission))
        return false;
    }
    catch (IOException localIOException)
    {
    }
    return true;
  }

  private File castToFile(Object paramObject)
    throws IOException
  {
    String str = null;
    if (paramObject instanceof File)
      str = ((File)paramObject).getCanonicalPath();
    else if (paramObject instanceof String)
      str = (String)paramObject;
    return new File(str);
  }

  private static boolean isFileInWebstartedCache(File paramFile)
  {
    if (deploymentCacheDirectoryList.isEmpty())
    {
      localObject = DEPLOYMENT_CACHE_PROPERTIES;
      int i = localObject.length;
      for (int j = 0; j < i; ++j)
      {
        String str1 = localObject[j];
        String str2 = System.getProperty(str1);
        if (str2 != null)
          try
          {
            File localFile3 = new File(str2).getCanonicalFile();
            if (localFile3 != null)
              deploymentCacheDirectoryList.add(localFile3);
          }
          catch (IOException localIOException)
          {
          }
      }
    }
    Object localObject = deploymentCacheDirectoryList.iterator();
    while (((Iterator)localObject).hasNext())
    {
      File localFile1 = (File)((Iterator)localObject).next();
      for (File localFile2 = paramFile; localFile2 != null; localFile2 = localFile2.getParentFile())
        if (localFile2.equals(localFile1))
          return true;
    }
    return false;
  }

  public Object translateBytes(byte[] paramArrayOfByte, DataFlavor paramDataFlavor, long paramLong, Transferable paramTransferable)
    throws IOException
  {
    return translateBytesOrStream(null, paramArrayOfByte, paramDataFlavor, paramLong, paramTransferable);
  }

  public Object translateStream(InputStream paramInputStream, DataFlavor paramDataFlavor, long paramLong, Transferable paramTransferable)
    throws IOException
  {
    return translateBytesOrStream(paramInputStream, null, paramDataFlavor, paramLong, paramTransferable);
  }

  protected Object translateBytesOrStream(InputStream paramInputStream, byte[] paramArrayOfByte, DataFlavor paramDataFlavor, long paramLong, Transferable paramTransferable)
    throws IOException
  {
    Object localObject1;
    Object localObject2;
    Object localObject3;
    if (paramInputStream == null)
      paramInputStream = new ByteArrayInputStream(paramArrayOfByte);
    if (isFileFormat(paramLong))
    {
      if (!(DataFlavor.javaFileListFlavor.equals(paramDataFlavor)))
        throw new IOException("data translation failed");
      if (paramArrayOfByte == null)
        paramArrayOfByte = inputStreamToByteArray(paramInputStream);
      localObject1 = dragQueryFile(paramArrayOfByte);
      if (localObject1 == null)
      {
        paramInputStream.close();
        return null;
      }
      localObject2 = new File[localObject1.length];
      for (int i = 0; i < localObject1.length; ++i)
        localObject2[i] = new File(localObject1[i]);
      paramInputStream.close();
      return Arrays.asList(localObject2);
    }
    if ((String.class.equals(paramDataFlavor.getRepresentationClass())) && (isFlavorCharsetTextType(paramDataFlavor)) && (isTextFormat(paramLong)))
    {
      int j;
      if (paramArrayOfByte == null)
        paramArrayOfByte = inputStreamToByteArray(paramInputStream);
      paramInputStream.close();
      localObject1 = Long.valueOf(paramLong);
      localObject2 = null;
      if ((isLocaleDependentTextFormat(paramLong)) && (paramTransferable != null) && (paramTransferable.isDataFlavorSupported(javaTextEncodingFlavor)))
        try
        {
          localObject2 = new String((byte[])(byte[])paramTransferable.getTransferData(javaTextEncodingFlavor), "UTF-8");
        }
        catch (UnsupportedFlavorException localUnsupportedFlavorException)
        {
        }
      else
        localObject2 = getCharsetForTextFormat((Long)localObject1);
      if (localObject2 == null)
        localObject2 = getDefaultTextCharset();
      localObject3 = (String)nativeEOLNs.get(localObject1);
      Integer localInteger = (Integer)nativeTerminators.get(localObject1);
      if (localInteger != null)
      {
        int k = localInteger.intValue();
        j = 0;
        while (j < paramArrayOfByte.length - k + 1)
        {
          for (int l = j; l < j + k; ++l)
            if (paramArrayOfByte[l] != 0)
              break label343:
          break;
          label343: j += k;
        }
      }
      else
      {
        j = paramArrayOfByte.length;
      }
      String str = new String(paramArrayOfByte, 0, j, (String)localObject2);
      if (localObject3 != null)
      {
        char[] arrayOfChar1 = str.toCharArray();
        char[] arrayOfChar2 = ((String)localObject3).toCharArray();
        str = null;
        int i1 = 0;
        int i3 = 0;
        while (true)
        {
          while (true)
          {
            while (true)
            {
              if (i3 >= arrayOfChar1.length)
                break label533;
              if (i3 + arrayOfChar2.length <= arrayOfChar1.length)
                break;
              arrayOfChar1[(i1++)] = arrayOfChar1[(i3++)];
            }
            int i2 = 1;
            int i4 = 0;
            for (int i5 = i3; i4 < arrayOfChar2.length; ++i5)
            {
              if (arrayOfChar2[i4] != arrayOfChar1[i5])
              {
                i2 = 0;
                break;
              }
              ++i4;
            }
            if (i2 == 0)
              break;
            arrayOfChar1[(i1++)] = '\n';
            i3 += arrayOfChar2.length;
          }
          arrayOfChar1[(i1++)] = arrayOfChar1[(i3++)];
        }
        label533: str = new String(arrayOfChar1, 0, i1);
      }
      return str;
    }
    if (DataFlavor.plainTextFlavor.equals(paramDataFlavor))
      return new StringReader((String)translateBytesOrStream(paramInputStream, paramArrayOfByte, plainTextStringFlavor, paramLong, paramTransferable));
    if (paramDataFlavor.isRepresentationClassInputStream())
      return translateBytesOrStreamToInputStream(paramInputStream, paramDataFlavor, paramLong, paramTransferable);
    if (paramDataFlavor.isRepresentationClassReader())
    {
      if ((!(isFlavorCharsetTextType(paramDataFlavor))) || (!(isTextFormat(paramLong))))
        throw new IOException("cannot transfer non-text data as Reader");
      localObject1 = (InputStream)translateBytesOrStreamToInputStream(paramInputStream, DataFlavor.plainTextFlavor, paramLong, paramTransferable);
      localObject2 = getTextCharset(DataFlavor.plainTextFlavor);
      localObject3 = new InputStreamReader((InputStream)localObject1, (String)localObject2);
      return constructFlavoredObject(localObject3, paramDataFlavor, Reader.class);
    }
    if (paramDataFlavor.isRepresentationClassCharBuffer())
    {
      if ((!(isFlavorCharsetTextType(paramDataFlavor))) || (!(isTextFormat(paramLong))))
        throw new IOException("cannot transfer non-text data as CharBuffer");
      localObject1 = CharBuffer.wrap((String)translateBytesOrStream(paramInputStream, paramArrayOfByte, plainTextStringFlavor, paramLong, paramTransferable));
      return constructFlavoredObject(localObject1, paramDataFlavor, CharBuffer.class);
    }
    if (charArrayClass.equals(paramDataFlavor.getRepresentationClass()))
    {
      if ((!(isFlavorCharsetTextType(paramDataFlavor))) || (!(isTextFormat(paramLong))))
        throw new IOException("cannot transfer non-text data as char array");
      return ((String)translateBytesOrStream(paramInputStream, paramArrayOfByte, plainTextStringFlavor, paramLong, paramTransferable)).toCharArray();
    }
    if (paramDataFlavor.isRepresentationClassByteBuffer())
    {
      if ((isFlavorCharsetTextType(paramDataFlavor)) && (isTextFormat(paramLong)))
        paramArrayOfByte = ((String)translateBytesOrStream(paramInputStream, paramArrayOfByte, plainTextStringFlavor, paramLong, paramTransferable)).getBytes(getTextCharset(paramDataFlavor));
      else if (paramArrayOfByte == null)
        paramArrayOfByte = inputStreamToByteArray(paramInputStream);
      localObject1 = ByteBuffer.wrap(paramArrayOfByte);
      return constructFlavoredObject(localObject1, paramDataFlavor, ByteBuffer.class);
    }
    if (byteArrayClass.equals(paramDataFlavor.getRepresentationClass()))
    {
      if ((isFlavorCharsetTextType(paramDataFlavor)) && (isTextFormat(paramLong)))
        return ((String)translateBytesOrStream(paramInputStream, paramArrayOfByte, plainTextStringFlavor, paramLong, paramTransferable)).getBytes(getTextCharset(paramDataFlavor));
      return ((paramArrayOfByte != null) ? paramArrayOfByte : inputStreamToByteArray(paramInputStream));
    }
    if (paramDataFlavor.isRepresentationClassRemote())
      try
      {
        localObject1 = inputStreamToByteArray(paramInputStream);
        localObject2 = new ObjectInputStream(new ByteArrayInputStream(localObject1));
        localObject3 = ((MarshalledObject)(MarshalledObject)((ObjectInputStream)localObject2).readObject()).get();
        ((ObjectInputStream)localObject2).close();
        paramInputStream.close();
        return localObject3;
      }
      catch (Exception localException1)
      {
        throw new IOException(localException1.getMessage());
      }
    if (paramDataFlavor.isRepresentationClassSerializable())
      try
      {
        byte[] arrayOfByte = inputStreamToByteArray(paramInputStream);
        localObject2 = new ObjectInputStream(new ByteArrayInputStream(arrayOfByte));
        localObject3 = ((ObjectInputStream)localObject2).readObject();
        ((ObjectInputStream)localObject2).close();
        paramInputStream.close();
        return localObject3;
      }
      catch (Exception localException2)
      {
        throw new IOException(localException2.getMessage());
      }
    if (DataFlavor.imageFlavor.equals(paramDataFlavor))
    {
      if (!(isImageFormat(paramLong)))
        throw new IOException("data translation failed");
      Image localImage = platformImageBytesOrStreamToImage(paramInputStream, paramArrayOfByte, paramLong);
      paramInputStream.close();
      return localImage;
    }
    throw new IOException("data translation failed");
  }

  private Object translateBytesOrStreamToInputStream(InputStream paramInputStream, DataFlavor paramDataFlavor, long paramLong, Transferable paramTransferable)
    throws IOException
  {
    if ((isFlavorCharsetTextType(paramDataFlavor)) && (isTextFormat(paramLong)))
      paramInputStream = new ReencodingInputStream(this, paramInputStream, paramLong, getTextCharset(paramDataFlavor), paramTransferable);
    return constructFlavoredObject(paramInputStream, paramDataFlavor, InputStream.class);
  }

  private Object constructFlavoredObject(Object paramObject, DataFlavor paramDataFlavor, Class paramClass)
    throws IOException
  {
    Class localClass = paramDataFlavor.getRepresentationClass();
    if (paramClass.equals(localClass))
      return paramObject;
    Constructor[] arrayOfConstructor = null;
    try
    {
      arrayOfConstructor = (Constructor[])(Constructor[])AccessController.doPrivileged(new PrivilegedAction(this, localClass)
      {
        public Object run()
        {
          return this.val$dfrc.getConstructors();
        }
      });
    }
    catch (SecurityException localSecurityException)
    {
      throw new IOException(localSecurityException.getMessage());
    }
    Constructor localConstructor = null;
    for (int i = 0; i < arrayOfConstructor.length; ++i)
    {
      if (!(Modifier.isPublic(arrayOfConstructor[i].getModifiers())))
        break label133:
      Class[] arrayOfClass = arrayOfConstructor[i].getParameterTypes();
      if ((arrayOfClass != null) && (arrayOfClass.length == 1) && (paramClass.equals(arrayOfClass[0])))
      {
        localConstructor = arrayOfConstructor[i];
        label133: break;
      }
    }
    if (localConstructor == null)
      throw new IOException("can't find <init>(L" + paramClass + ";)V for class: " + localClass.getName());
    try
    {
      return localConstructor.newInstance(new Object[] { paramObject });
    }
    catch (Exception localException)
    {
      throw new IOException(localException.getMessage());
    }
  }

  protected abstract String[] dragQueryFile(byte[] paramArrayOfByte);

  protected abstract Image platformImageBytesOrStreamToImage(InputStream paramInputStream, byte[] paramArrayOfByte, long paramLong)
    throws IOException;

  protected Image standardImageBytesOrStreamToImage(InputStream paramInputStream, byte[] paramArrayOfByte, String paramString)
    throws IOException
  {
    if (paramInputStream == null)
      paramInputStream = new ByteArrayInputStream(paramArrayOfByte);
    Iterator localIterator = ImageIO.getImageReadersByMIMEType(paramString);
    if (!(localIterator.hasNext()))
      throw new IOException("No registered service provider can decode  an image from " + paramString);
    Object localObject1 = null;
    while (localIterator.hasNext())
    {
      ImageReader localImageReader = (ImageReader)localIterator.next();
      try
      {
        ImageInputStream localImageInputStream = ImageIO.createImageInputStream(paramInputStream);
        try
        {
          ImageReadParam localImageReadParam = localImageReader.getDefaultReadParam();
          localImageReader.setInput(localImageInputStream, true, true);
          BufferedImage localBufferedImage1 = localImageReader.read(localImageReader.getMinIndex(), localImageReadParam);
          if (localBufferedImage1 != null)
          {
            BufferedImage localBufferedImage2 = localBufferedImage1;
            return localBufferedImage2;
          }
        }
        finally
        {
          localImageInputStream.close();
          localImageReader.dispose();
        }
      }
      catch (IOException localIOException)
      {
        while (true)
          localObject1 = localIOException;
      }
    }
    if (localObject1 == null)
      localObject1 = new IOException("Registered service providers failed to decode an image from " + paramString);
    throw ((Throwable)localObject1);
  }

  protected abstract byte[] imageToPlatformBytes(Image paramImage, long paramLong)
    throws IOException;

  protected byte[] imageToStandardBytes(Image paramImage, String paramString)
    throws IOException
  {
    Object localObject1 = null;
    Iterator localIterator = ImageIO.getImageWritersByMIMEType(paramString);
    if (!(localIterator.hasNext()))
      throw new IOException("No registered service provider can encode  an image to " + paramString);
    if (paramImage instanceof RenderedImage);
    try
    {
      return imageToStandardBytesImpl((RenderedImage)paramImage, paramString);
    }
    catch (IOException i)
    {
      localObject1 = localIOException1;
      int i = 0;
      int j = 0;
      if (paramImage instanceof ToolkitImage)
      {
        localObject2 = ((ToolkitImage)paramImage).getImageRep();
        ((ImageRepresentation)localObject2).reconstruct(32);
        i = ((ImageRepresentation)localObject2).getWidth();
        j = ((ImageRepresentation)localObject2).getHeight();
      }
      else
      {
        i = paramImage.getWidth(null);
        j = paramImage.getHeight(null);
      }
      Object localObject2 = ColorModel.getRGBdefault();
      WritableRaster localWritableRaster = ((ColorModel)localObject2).createCompatibleWritableRaster(i, j);
      BufferedImage localBufferedImage = new BufferedImage((ColorModel)localObject2, localWritableRaster, ((ColorModel)localObject2).isAlphaPremultiplied(), null);
      Graphics localGraphics = localBufferedImage.getGraphics();
      try
      {
        localGraphics.drawImage(paramImage, 0, 0, i, j, null);
      }
      finally
      {
        localGraphics.dispose();
      }
      try
      {
        return imageToStandardBytesImpl(localBufferedImage, paramString);
      }
      catch (IOException localIOException2)
      {
        if (localObject1 != null)
          throw localObject1;
        throw localIOException2;
      }
    }
  }

  protected byte[] imageToStandardBytesImpl(RenderedImage paramRenderedImage, String paramString)
    throws IOException
  {
    ImageWriter localImageWriter;
    Iterator localIterator = ImageIO.getImageWritersByMIMEType(paramString);
    ImageTypeSpecifier localImageTypeSpecifier = new ImageTypeSpecifier(paramRenderedImage);
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    Object localObject1 = null;
    while (true)
    {
      if (!(localIterator.hasNext()))
        break label154;
      localImageWriter = (ImageWriter)localIterator.next();
      ImageWriterSpi localImageWriterSpi = localImageWriter.getOriginatingProvider();
      if (localImageWriterSpi.canEncodeImage(localImageTypeSpecifier))
        break;
    }
    try
    {
      ImageOutputStream localImageOutputStream = ImageIO.createImageOutputStream(localByteArrayOutputStream);
      try
      {
        localImageWriter.setOutput(localImageOutputStream);
        localImageWriter.write(paramRenderedImage);
        localImageOutputStream.flush();
      }
      finally
      {
        localImageOutputStream.close();
      }
    }
    catch (IOException localIOException)
    {
      while (true)
      {
        localImageWriter.dispose();
        localByteArrayOutputStream.reset();
        localObject1 = localIOException;
      }
    }
    localImageWriter.dispose();
    localByteArrayOutputStream.close();
    return localByteArrayOutputStream.toByteArray();
    label154: localByteArrayOutputStream.close();
    if (localObject1 == null)
      localObject1 = new IOException("Registered service providers failed to encode " + paramRenderedImage + " to " + paramString);
    throw ((Throwable)localObject1);
  }

  private Object concatData(Object paramObject1, Object paramObject2)
  {
    Object localObject1 = null;
    Object localObject2 = null;
    if (paramObject1 instanceof byte[])
    {
      byte[] arrayOfByte1 = (byte[])(byte[])paramObject1;
      if (paramObject2 instanceof byte[])
      {
        byte[] arrayOfByte2 = (byte[])(byte[])paramObject2;
        byte[] arrayOfByte3 = new byte[arrayOfByte1.length + arrayOfByte2.length];
        System.arraycopy(arrayOfByte1, 0, arrayOfByte3, 0, arrayOfByte1.length);
        System.arraycopy(arrayOfByte2, 0, arrayOfByte3, arrayOfByte1.length, arrayOfByte2.length);
        return arrayOfByte3;
      }
      localObject1 = new ByteArrayInputStream(arrayOfByte1);
      localObject2 = (InputStream)paramObject2;
    }
    else
    {
      localObject1 = (InputStream)paramObject1;
      if (paramObject2 instanceof byte[])
        localObject2 = new ByteArrayInputStream((byte[])(byte[])paramObject2);
      else
        localObject2 = (InputStream)paramObject2;
    }
    return new SequenceInputStream((InputStream)localObject1, (InputStream)localObject2);
  }

  public byte[] convertData(Object paramObject, Transferable paramTransferable, long paramLong, java.util.Map paramMap, boolean paramBoolean)
    throws IOException
  {
    Object localObject1;
    byte[] arrayOfByte = null;
    if (paramBoolean)
    {
      try
      {
        localObject1 = new Stack();
        6 local6 = new Runnable(this, paramMap, paramLong, paramTransferable, (Stack)localObject1)
        {
          private boolean done = false;

          public void run()
          {
            if (this.done)
              return;
            byte[] arrayOfByte = null;
            try
            {
              DataFlavor localDataFlavor = (DataFlavor)this.val$formatMap.get(Long.valueOf(this.val$format));
              if (localDataFlavor != null)
                arrayOfByte = this.this$0.translateTransferable(this.val$contents, localDataFlavor, this.val$format);
            }
            catch (Exception localException)
            {
              localException.printStackTrace();
              arrayOfByte = null;
            }
            try
            {
              this.this$0.getToolkitThreadBlockedHandler().lock();
              this.val$stack.push(arrayOfByte);
              this.this$0.getToolkitThreadBlockedHandler().exit();
            }
            finally
            {
              this.this$0.getToolkitThreadBlockedHandler().unlock();
              this.done = true;
            }
          }
        };
        AppContext localAppContext = SunToolkit.targetToAppContext(paramObject);
        getToolkitThreadBlockedHandler().lock();
        if (localAppContext != null)
          localAppContext.put("DATA_CONVERTER_KEY", local6);
        SunToolkit.executeOnEventHandlerThread(paramObject, local6);
        while (((Stack)localObject1).empty())
          getToolkitThreadBlockedHandler().enter();
        if (localAppContext != null)
          localAppContext.remove("DATA_CONVERTER_KEY");
        arrayOfByte = (byte[])(byte[])((Stack)localObject1).pop();
      }
      finally
      {
        getToolkitThreadBlockedHandler().unlock();
      }
    }
    else
    {
      localObject1 = (DataFlavor)paramMap.get(Long.valueOf(paramLong));
      if (localObject1 != null)
        arrayOfByte = translateTransferable(paramTransferable, (DataFlavor)localObject1, paramLong);
    }
    return ((B)arrayOfByte);
  }

  public void processDataConversionRequests()
  {
    if (EventQueue.isDispatchThread())
    {
      AppContext localAppContext = AppContext.getAppContext();
      getToolkitThreadBlockedHandler().lock();
      try
      {
        Runnable localRunnable = (Runnable)localAppContext.get("DATA_CONVERTER_KEY");
        if (localRunnable != null)
        {
          localRunnable.run();
          localAppContext.remove("DATA_CONVERTER_KEY");
        }
      }
      finally
      {
        getToolkitThreadBlockedHandler().unlock();
      }
    }
  }

  public abstract ToolkitThreadBlockedHandler getToolkitThreadBlockedHandler();

  public static long[] keysToLongArray(SortedMap paramSortedMap)
  {
    Set localSet = paramSortedMap.keySet();
    long[] arrayOfLong = new long[localSet.size()];
    int i = 0;
    Iterator localIterator = localSet.iterator();
    while (localIterator.hasNext())
    {
      arrayOfLong[i] = ((Long)localIterator.next()).longValue();
      ++i;
    }
    return arrayOfLong;
  }

  public static DataFlavor[] keysToDataFlavorArray(java.util.Map paramMap)
  {
    return setToSortedDataFlavorArray(paramMap.keySet(), paramMap);
  }

  public static DataFlavor[] setToSortedDataFlavorArray(Set paramSet)
  {
    DataFlavor[] arrayOfDataFlavor = new DataFlavor[paramSet.size()];
    paramSet.toArray(arrayOfDataFlavor);
    Arrays.sort(arrayOfDataFlavor, defaultFlavorComparator);
    return arrayOfDataFlavor;
  }

  public static DataFlavor[] setToSortedDataFlavorArray(Set paramSet, java.util.Map paramMap)
  {
    DataFlavor[] arrayOfDataFlavor = new DataFlavor[paramSet.size()];
    paramSet.toArray(arrayOfDataFlavor);
    DataFlavorComparator localDataFlavorComparator = new DataFlavorComparator(paramMap, false);
    Arrays.sort(arrayOfDataFlavor, localDataFlavorComparator);
    return arrayOfDataFlavor;
  }

  protected static byte[] inputStreamToByteArray(InputStream paramInputStream)
    throws IOException
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    int i = 0;
    byte[] arrayOfByte = new byte[8192];
    while ((i = paramInputStream.read(arrayOfByte)) != -1)
      localByteArrayOutputStream.write(arrayOfByte, 0, i);
    return localByteArrayOutputStream.toByteArray();
  }

  public List getPlatformMappingsForNative(String paramString)
  {
    return new ArrayList();
  }

  public List getPlatformMappingsForFlavor(DataFlavor paramDataFlavor)
  {
    return new ArrayList();
  }

  static
  {
    Class localClass1 = null;
    Class localClass2 = null;
    try
    {
      localClass1 = Class.forName("[C");
      localClass2 = Class.forName("[B");
    }
    catch (ClassNotFoundException localClassNotFoundException1)
    {
    }
    charArrayClass = localClass1;
    byteArrayClass = localClass2;
    DataFlavor localDataFlavor1 = null;
    try
    {
      localDataFlavor1 = new DataFlavor("text/plain;charset=Unicode;class=java.lang.String");
    }
    catch (ClassNotFoundException localClassNotFoundException2)
    {
    }
    plainTextStringFlavor = localDataFlavor1;
    DataFlavor localDataFlavor2 = null;
    try
    {
      localDataFlavor2 = new DataFlavor("application/x-java-text-encoding;class=\"[B\"");
    }
    catch (ClassNotFoundException localClassNotFoundException3)
    {
    }
    javaTextEncodingFlavor = localDataFlavor2;
    HashMap localHashMap = new HashMap(17);
    localHashMap.put("sgml", Boolean.TRUE);
    localHashMap.put("xml", Boolean.TRUE);
    localHashMap.put("html", Boolean.TRUE);
    localHashMap.put("enriched", Boolean.TRUE);
    localHashMap.put("richtext", Boolean.TRUE);
    localHashMap.put("uri-list", Boolean.TRUE);
    localHashMap.put("directory", Boolean.TRUE);
    localHashMap.put("css", Boolean.TRUE);
    localHashMap.put("calendar", Boolean.TRUE);
    localHashMap.put("plain", Boolean.TRUE);
    localHashMap.put("rtf", Boolean.FALSE);
    localHashMap.put("tab-separated-values", Boolean.FALSE);
    localHashMap.put("t140", Boolean.FALSE);
    localHashMap.put("rfc822-headers", Boolean.FALSE);
    localHashMap.put("parityfec", Boolean.FALSE);
    textMIMESubtypeCharsetSupport = Collections.synchronizedMap(localHashMap);
    DEPLOYMENT_CACHE_PROPERTIES = { "deployment.system.cachedir", "deployment.user.cachedir", "deployment.javaws.cachedir", "deployment.javapi.cachedir" };
    deploymentCacheDirectoryList = new ArrayList();
    defaultCharsetComparator = new CharsetComparator(false);
    defaultFlavorComparator = new DataFlavorComparator(false);
  }

  public static class CharsetComparator extends DataTransferer.IndexedComparator
  {
    private static final java.util.Map charsets;
    private static String defaultEncoding;
    private static final Integer DEFAULT_CHARSET_INDEX = Integer.valueOf(2);
    private static final Integer OTHER_CHARSET_INDEX = Integer.valueOf(1);
    private static final Integer WORST_CHARSET_INDEX = Integer.valueOf(0);
    private static final Integer UNSUPPORTED_CHARSET_INDEX = Integer.valueOf(-2147483648);
    private static final String UNSUPPORTED_CHARSET = "UNSUPPORTED";

    public CharsetComparator()
    {
      this(true);
    }

    public CharsetComparator(boolean paramBoolean)
    {
      super(paramBoolean);
    }

    public int compare(Object paramObject1, Object paramObject2)
    {
      String str1 = null;
      String str2 = null;
      if (this.order == true)
      {
        str1 = (String)paramObject1;
        str2 = (String)paramObject2;
      }
      else
      {
        str1 = (String)paramObject2;
        str2 = (String)paramObject1;
      }
      return compareCharsets(str1, str2);
    }

    protected int compareCharsets(String paramString1, String paramString2)
    {
      paramString1 = getEncoding(paramString1);
      paramString2 = getEncoding(paramString2);
      int i = compareIndices(charsets, paramString1, paramString2, OTHER_CHARSET_INDEX);
      if (i == 0)
        return paramString2.compareTo(paramString1);
      return i;
    }

    protected static String getEncoding(String paramString)
    {
      if (paramString == null)
        return null;
      if (!(DataTransferer.isEncodingSupported(paramString)))
        return "UNSUPPORTED";
      String str = DataTransferer.canonicalName(paramString);
      return ((charsets.containsKey(str)) ? str : paramString);
    }

    static
    {
      HashMap localHashMap = new HashMap(8, 1F);
      localHashMap.put(DataTransferer.canonicalName("UTF-16LE"), Integer.valueOf(4));
      localHashMap.put(DataTransferer.canonicalName("UTF-16BE"), Integer.valueOf(5));
      localHashMap.put(DataTransferer.canonicalName("UTF-8"), Integer.valueOf(6));
      localHashMap.put(DataTransferer.canonicalName("UTF-16"), Integer.valueOf(7));
      localHashMap.put(DataTransferer.canonicalName("US-ASCII"), WORST_CHARSET_INDEX);
      String str = DataTransferer.canonicalName(DataTransferer.getDefaultTextCharset());
      if (localHashMap.get(defaultEncoding) == null)
        localHashMap.put(defaultEncoding, DEFAULT_CHARSET_INDEX);
      localHashMap.put("UNSUPPORTED", UNSUPPORTED_CHARSET_INDEX);
      charsets = Collections.unmodifiableMap(localHashMap);
    }
  }

  public static class DataFlavorComparator extends DataTransferer.IndexedComparator
  {
    protected final java.util.Map flavorToFormatMap;
    private final DataTransferer.CharsetComparator charsetComparator;
    private static final java.util.Map exactTypes;
    private static final java.util.Map primaryTypes;
    private static final java.util.Map nonTextRepresentations;
    private static final java.util.Map textTypes;
    private static final java.util.Map decodedTextRepresentations;
    private static final java.util.Map encodedTextRepresentations;
    private static final Integer UNKNOWN_OBJECT_LOSES = Integer.valueOf(-2147483648);
    private static final Integer UNKNOWN_OBJECT_WINS = Integer.valueOf(2147483647);
    private static final Long UNKNOWN_OBJECT_LOSES_L = Long.valueOf(-9223372036854775808L);
    private static final Long UNKNOWN_OBJECT_WINS_L = Long.valueOf(9223372036854775807L);

    public DataFlavorComparator()
    {
      this(true);
    }

    public DataFlavorComparator(boolean paramBoolean)
    {
      super(paramBoolean);
      this.charsetComparator = new DataTransferer.CharsetComparator(paramBoolean);
      this.flavorToFormatMap = Collections.EMPTY_MAP;
    }

    public DataFlavorComparator(java.util.Map paramMap)
    {
      this(paramMap, true);
    }

    public DataFlavorComparator(java.util.Map paramMap, boolean paramBoolean)
    {
      super(paramBoolean);
      this.charsetComparator = new DataTransferer.CharsetComparator(paramBoolean);
      HashMap localHashMap = new HashMap(paramMap.size());
      localHashMap.putAll(paramMap);
      this.flavorToFormatMap = Collections.unmodifiableMap(localHashMap);
    }

    public int compare(Object paramObject1, Object paramObject2)
    {
      DataFlavor localDataFlavor1 = null;
      DataFlavor localDataFlavor2 = null;
      if (this.order == true)
      {
        localDataFlavor1 = (DataFlavor)paramObject1;
        localDataFlavor2 = (DataFlavor)paramObject2;
      }
      else
      {
        localDataFlavor1 = (DataFlavor)paramObject2;
        localDataFlavor2 = (DataFlavor)paramObject1;
      }
      if (localDataFlavor1.equals(localDataFlavor2))
        return 0;
      int i = 0;
      String str1 = localDataFlavor1.getPrimaryType();
      String str2 = localDataFlavor1.getSubType();
      String str3 = str1 + "/" + str2;
      Class localClass1 = localDataFlavor1.getRepresentationClass();
      String str4 = localDataFlavor2.getPrimaryType();
      String str5 = localDataFlavor2.getSubType();
      String str6 = str4 + "/" + str5;
      Class localClass2 = localDataFlavor2.getRepresentationClass();
      if ((localDataFlavor1.isFlavorTextType()) && (localDataFlavor2.isFlavorTextType()))
      {
        i = compareIndices(textTypes, str3, str6, UNKNOWN_OBJECT_LOSES);
        if (i != 0)
          return i;
        if (DataTransferer.doesSubtypeSupportCharset(localDataFlavor1))
        {
          i = compareIndices(decodedTextRepresentations, localClass1, localClass2, UNKNOWN_OBJECT_LOSES);
          if (i != 0)
            return i;
          i = this.charsetComparator.compareCharsets(DataTransferer.getTextCharset(localDataFlavor1), DataTransferer.getTextCharset(localDataFlavor2));
          if (i != 0)
            return i;
        }
        i = compareIndices(encodedTextRepresentations, localClass1, localClass2, UNKNOWN_OBJECT_LOSES);
        if (i == 0)
          break label331;
        return i;
      }
      i = compareIndices(primaryTypes, str1, str4, UNKNOWN_OBJECT_LOSES);
      if (i != 0)
        return i;
      i = compareIndices(exactTypes, str3, str6, UNKNOWN_OBJECT_WINS);
      if (i != 0)
        return i;
      i = compareIndices(nonTextRepresentations, localClass1, localClass2, UNKNOWN_OBJECT_LOSES);
      if (i != 0)
        return i;
      label331: return compareLongs(this.flavorToFormatMap, localDataFlavor1, localDataFlavor2, UNKNOWN_OBJECT_LOSES_L);
    }

    static
    {
      HashMap localHashMap = new HashMap(4, 1F);
      localHashMap.put("application/x-java-file-list", Integer.valueOf(0));
      localHashMap.put("application/x-java-serialized-object", Integer.valueOf(1));
      localHashMap.put("application/x-java-jvm-local-objectref", Integer.valueOf(2));
      localHashMap.put("application/x-java-remote-object", Integer.valueOf(3));
      exactTypes = Collections.unmodifiableMap(localHashMap);
      localHashMap = new HashMap(1, 1F);
      localHashMap.put("application", Integer.valueOf(0));
      primaryTypes = Collections.unmodifiableMap(localHashMap);
      localHashMap = new HashMap(3, 1F);
      localHashMap.put(InputStream.class, Integer.valueOf(0));
      localHashMap.put(Serializable.class, Integer.valueOf(1));
      localHashMap.put(Remote.class, Integer.valueOf(2));
      nonTextRepresentations = Collections.unmodifiableMap(localHashMap);
      localHashMap = new HashMap(16, 1F);
      localHashMap.put("text/plain", Integer.valueOf(0));
      localHashMap.put("application/x-java-serialized-object", Integer.valueOf(1));
      localHashMap.put("text/calendar", Integer.valueOf(2));
      localHashMap.put("text/css", Integer.valueOf(3));
      localHashMap.put("text/directory", Integer.valueOf(4));
      localHashMap.put("text/parityfec", Integer.valueOf(5));
      localHashMap.put("text/rfc822-headers", Integer.valueOf(6));
      localHashMap.put("text/t140", Integer.valueOf(7));
      localHashMap.put("text/tab-separated-values", Integer.valueOf(8));
      localHashMap.put("text/uri-list", Integer.valueOf(9));
      localHashMap.put("text/richtext", Integer.valueOf(10));
      localHashMap.put("text/enriched", Integer.valueOf(11));
      localHashMap.put("text/rtf", Integer.valueOf(12));
      localHashMap.put("text/html", Integer.valueOf(13));
      localHashMap.put("text/xml", Integer.valueOf(14));
      localHashMap.put("text/sgml", Integer.valueOf(15));
      textTypes = Collections.unmodifiableMap(localHashMap);
      localHashMap = new HashMap(4, 1F);
      localHashMap.put(DataTransferer.charArrayClass, Integer.valueOf(0));
      localHashMap.put(CharBuffer.class, Integer.valueOf(1));
      localHashMap.put(String.class, Integer.valueOf(2));
      localHashMap.put(Reader.class, Integer.valueOf(3));
      decodedTextRepresentations = Collections.unmodifiableMap(localHashMap);
      localHashMap = new HashMap(3, 1F);
      localHashMap.put(DataTransferer.byteArrayClass, Integer.valueOf(0));
      localHashMap.put(ByteBuffer.class, Integer.valueOf(1));
      localHashMap.put(InputStream.class, Integer.valueOf(2));
      encodedTextRepresentations = Collections.unmodifiableMap(localHashMap);
    }
  }

  public static class IndexOrderComparator extends DataTransferer.IndexedComparator
  {
    private final java.util.Map indexMap;
    private static final Integer FALLBACK_INDEX = Integer.valueOf(-2147483648);

    public IndexOrderComparator(java.util.Map paramMap)
    {
      super(true);
      this.indexMap = paramMap;
    }

    public IndexOrderComparator(java.util.Map paramMap, boolean paramBoolean)
    {
      super(paramBoolean);
      this.indexMap = paramMap;
    }

    public int compare(Object paramObject1, Object paramObject2)
    {
      if (!(this.order))
        return (-compareIndices(this.indexMap, paramObject1, paramObject2, FALLBACK_INDEX));
      return compareIndices(this.indexMap, paramObject1, paramObject2, FALLBACK_INDEX);
    }
  }

  public static abstract class IndexedComparator
  implements Comparator
  {
    public static final boolean SELECT_BEST = 1;
    public static final boolean SELECT_WORST = 0;
    protected final boolean order;

    public IndexedComparator()
    {
      this(true);
    }

    public IndexedComparator(boolean paramBoolean)
    {
      this.order = paramBoolean;
    }

    protected static int compareIndices(java.util.Map paramMap, Object paramObject1, Object paramObject2, Integer paramInteger)
    {
      Integer localInteger1 = (Integer)paramMap.get(paramObject1);
      Integer localInteger2 = (Integer)paramMap.get(paramObject2);
      if (localInteger1 == null)
        localInteger1 = paramInteger;
      if (localInteger2 == null)
        localInteger2 = paramInteger;
      return localInteger1.compareTo(localInteger2);
    }

    protected static int compareLongs(java.util.Map paramMap, Object paramObject1, Object paramObject2, Long paramLong)
    {
      Long localLong1 = (Long)paramMap.get(paramObject1);
      Long localLong2 = (Long)paramMap.get(paramObject2);
      if (localLong1 == null)
        localLong1 = paramLong;
      if (localLong2 == null)
        localLong2 = paramLong;
      return localLong1.compareTo(localLong2);
    }
  }

  public class ReencodingInputStream extends InputStream
  {
    protected BufferedReader wrapped;
    protected final char[] in = new char[1];
    protected byte[] out;
    protected CharsetEncoder encoder;
    protected CharBuffer inBuf;
    protected ByteBuffer outBuf;
    protected char[] eoln;
    protected int numTerminators;
    protected boolean eos;
    protected int index;
    protected int limit;

    public ReencodingInputStream(, InputStream paramInputStream, long paramLong, String paramString, Transferable paramTransferable)
      throws IOException
    {
      Long localLong = Long.valueOf(paramLong);
      String str1 = null;
      if ((paramDataTransferer.isLocaleDependentTextFormat(paramLong)) && (paramTransferable != null) && (paramTransferable.isDataFlavorSupported(DataTransferer.javaTextEncodingFlavor)))
        try
        {
          str1 = new String((byte[])(byte[])paramTransferable.getTransferData(DataTransferer.javaTextEncodingFlavor), "UTF-8");
        }
        catch (UnsupportedFlavorException localUnsupportedFlavorException)
        {
        }
      else
        str1 = paramDataTransferer.getCharsetForTextFormat(localLong);
      if (str1 == null)
        str1 = DataTransferer.getDefaultTextCharset();
      this.wrapped = new BufferedReader(new InputStreamReader(paramInputStream, str1));
      if (paramString == null)
        throw new NullPointerException("null target encoding");
      try
      {
        this.encoder = Charset.forName(paramString).newEncoder();
        this.out = new byte[(int)(this.encoder.maxBytesPerChar() + 0.5D)];
        this.inBuf = CharBuffer.wrap(this.in);
        this.outBuf = ByteBuffer.wrap(this.out);
      }
      catch (IllegalCharsetNameException localIllegalCharsetNameException)
      {
        throw new IOException(localIllegalCharsetNameException.toString());
      }
      catch (UnsupportedCharsetException localUnsupportedCharsetException)
      {
        throw new IOException(localUnsupportedCharsetException.toString());
      }
      catch (UnsupportedOperationException localUnsupportedOperationException)
      {
        throw new IOException(localUnsupportedOperationException.toString());
      }
      String str2 = (String)DataTransferer.access$300().get(localLong);
      if (str2 != null)
        this.eoln = str2.toCharArray();
      Integer localInteger = (Integer)DataTransferer.access$400().get(localLong);
      if (localInteger != null)
        this.numTerminators = localInteger.intValue();
    }

    public int read()
      throws IOException
    {
      if (this.eos)
        return -1;
      if (this.index >= this.limit)
      {
        int i = this.wrapped.read();
        if (i == -1)
        {
          this.eos = true;
          return -1;
        }
        if ((this.numTerminators > 0) && (i == 0))
        {
          this.eos = true;
          return -1;
        }
        if ((this.eoln != null) && (matchCharArray(this.eoln, i)))
          i = 10;
        this.in[0] = (char)i;
        this.inBuf.rewind();
        this.outBuf.rewind();
        this.encoder.encode(this.inBuf, this.outBuf, false);
        this.outBuf.flip();
        this.limit = this.outBuf.limit();
        this.index = 0;
        return read();
      }
      return (this.out[(this.index++)] & 0xFF);
    }

    public int available()
      throws IOException
    {
      return ((this.eos) ? 0 : this.limit - this.index);
    }

    public void close()
      throws IOException
    {
      this.wrapped.close();
    }

    private boolean matchCharArray(, int paramInt)
      throws IOException
    {
      this.wrapped.mark(paramArrayOfChar.length);
      int i = 0;
      if ((char)paramInt == paramArrayOfChar[0])
        for (i = 1; i < paramArrayOfChar.length; ++i)
        {
          paramInt = this.wrapped.read();
          if (paramInt == -1)
            break;
          if ((char)paramInt != paramArrayOfChar[i])
            break;
        }
      if (i == paramArrayOfChar.length)
        return true;
      this.wrapped.reset();
      return false;
    }
  }
}
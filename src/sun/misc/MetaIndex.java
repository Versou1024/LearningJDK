package sun.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaIndex
{
  private static volatile Map<File, MetaIndex> jarMap;
  private String[] contents;
  private boolean isClassOnlyJar;

  public static MetaIndex forJar(File paramFile)
  {
    return ((MetaIndex)getJarMap().get(paramFile));
  }

  public static synchronized void registerDirectory(File paramFile)
  {
    File localFile = new File(paramFile, "meta-index");
    if (localFile.exists())
      try
      {
        BufferedReader localBufferedReader = new BufferedReader(new FileReader(localFile));
        String str1 = null;
        String str2 = null;
        boolean bool = false;
        ArrayList localArrayList = new ArrayList();
        Map localMap = getJarMap();
        paramFile = paramFile.getCanonicalFile();
        str1 = localBufferedReader.readLine();
        if ((str1 == null) || (!(str1.equals("% VERSION 2"))))
        {
          localBufferedReader.close();
          return;
        }
        while (true)
        {
          while (true)
            while (true)
            {
              do
                while (true)
                {
                  if ((str1 = localBufferedReader.readLine()) == null)
                    break label240;
                  switch (str1.charAt(0))
                  {
                  case '!':
                  case '#':
                  case '@':
                    if ((str2 != null) && (localArrayList.size() > 0))
                    {
                      localMap.put(new File(paramFile, str2), new MetaIndex(localArrayList, bool));
                      localArrayList.clear();
                    }
                    str2 = str1.substring(2);
                    if (str1.charAt(0) != '!')
                      break;
                    bool = true;
                  case '%':
                  }
                }
              while (!(bool));
              bool = false;
            }
          localArrayList.add(str1);
        }
        if ((str2 != null) && (localArrayList.size() > 0))
          label240: localMap.put(new File(paramFile, str2), new MetaIndex(localArrayList, bool));
        localBufferedReader.close();
      }
      catch (IOException localIOException)
      {
      }
  }

  public boolean mayContain(String paramString)
  {
    if ((this.isClassOnlyJar) && (!(paramString.endsWith(".class"))))
      return false;
    String[] arrayOfString = this.contents;
    for (int i = 0; i < arrayOfString.length; ++i)
      if (paramString.startsWith(arrayOfString[i]))
        return true;
    return false;
  }

  private MetaIndex(List<String> paramList, boolean paramBoolean)
    throws IllegalArgumentException
  {
    if (paramList == null)
      throw new IllegalArgumentException();
    this.contents = ((String[])paramList.toArray(new String[0]));
    this.isClassOnlyJar = paramBoolean;
  }

  private static Map<File, MetaIndex> getJarMap()
  {
    if (jarMap == null)
      synchronized (MetaIndex.class)
      {
        if (jarMap == null)
          jarMap = new HashMap();
      }
    if ((!($assertionsDisabled)) && (jarMap == null))
      throw new AssertionError();
    return jarMap;
  }
}
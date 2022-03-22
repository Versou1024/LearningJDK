package sun.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarIndex
{
  private HashMap indexMap;
  private HashMap jarMap;
  private String[] jarFiles;
  public static final String INDEX_NAME = "META-INF/INDEX.LIST";

  public JarIndex()
  {
    this.indexMap = new HashMap();
    this.jarMap = new HashMap();
  }

  public JarIndex(InputStream paramInputStream)
    throws IOException
  {
    read(paramInputStream);
  }

  public JarIndex(String[] paramArrayOfString)
    throws IOException
  {
    this.jarFiles = paramArrayOfString;
    parseJars(paramArrayOfString);
  }

  public static JarIndex getJarIndex(JarFile paramJarFile, MetaIndex paramMetaIndex)
    throws IOException
  {
    JarIndex localJarIndex = null;
    if ((paramMetaIndex != null) && (!(paramMetaIndex.mayContain("META-INF/INDEX.LIST"))))
      return null;
    JarEntry localJarEntry = paramJarFile.getJarEntry("META-INF/INDEX.LIST");
    if (localJarEntry != null)
      localJarIndex = new JarIndex(paramJarFile.getInputStream(localJarEntry));
    return localJarIndex;
  }

  public String[] getJarFiles()
  {
    return this.jarFiles;
  }

  private void addToList(String paramString1, String paramString2, HashMap paramHashMap)
  {
    LinkedList localLinkedList = (LinkedList)paramHashMap.get(paramString1);
    if (localLinkedList == null)
    {
      localLinkedList = new LinkedList();
      localLinkedList.add(paramString2);
      paramHashMap.put(paramString1, localLinkedList);
    }
    else if (!(localLinkedList.contains(paramString2)))
    {
      localLinkedList.add(paramString2);
    }
  }

  public LinkedList get(String paramString)
  {
    LinkedList localLinkedList = null;
    if ((localLinkedList = (LinkedList)this.indexMap.get(paramString)) == null)
    {
      int i;
      if ((i = paramString.lastIndexOf("/")) != -1)
        localLinkedList = (LinkedList)this.indexMap.get(paramString.substring(0, i));
    }
    return localLinkedList;
  }

  public void add(String paramString1, String paramString2)
  {
    String str;
    int i;
    if ((i = paramString1.lastIndexOf("/")) != -1)
      str = paramString1.substring(0, i);
    else
      str = paramString1;
    addToList(str, paramString2, this.indexMap);
    addToList(paramString2, str, this.jarMap);
  }

  private void parseJars(String[] paramArrayOfString)
    throws IOException
  {
    if (paramArrayOfString == null)
      return;
    String str1 = null;
    for (int i = 0; i < paramArrayOfString.length; ++i)
    {
      str1 = paramArrayOfString[i];
      ZipFile localZipFile = new ZipFile(str1.replace('/', File.separatorChar));
      Enumeration localEnumeration = localZipFile.entries();
      while (localEnumeration.hasMoreElements())
      {
        String str2 = ((ZipEntry)(ZipEntry)localEnumeration.nextElement()).getName();
        if ((!(str2.startsWith("META-INF/"))) || ((!(str2.equals("META-INF/"))) && (!(str2.equals("META-INF/INDEX.LIST"))) && (!(str2.equals("META-INF/MANIFEST.MF")))))
          add(str2, str1);
      }
      localZipFile.close();
    }
  }

  public void write(OutputStream paramOutputStream)
    throws IOException
  {
    BufferedWriter localBufferedWriter = new BufferedWriter(new OutputStreamWriter(paramOutputStream, "UTF8"));
    localBufferedWriter.write("JarIndex-Version: 1.0\n\n");
    if (this.jarFiles != null)
    {
      for (int i = 0; i < this.jarFiles.length; ++i)
      {
        String str = this.jarFiles[i];
        localBufferedWriter.write(str + "\n");
        LinkedList localLinkedList = (LinkedList)this.jarMap.get(str);
        if (localLinkedList != null)
        {
          Iterator localIterator = localLinkedList.iterator();
          while (localIterator.hasNext())
            localBufferedWriter.write(((String)(String)localIterator.next()) + "\n");
        }
        localBufferedWriter.write("\n");
      }
      localBufferedWriter.flush();
    }
  }

  public void read(InputStream paramInputStream)
    throws IOException
  {
    BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(paramInputStream, "UTF8"));
    String str1 = null;
    String str2 = null;
    Vector localVector = new Vector();
    while (((str1 = localBufferedReader.readLine()) != null) && (!(str1.endsWith(".jar"))));
    while (str1 != null)
    {
      if (str1.length() == 0)
        break label117:
      if (str1.endsWith(".jar"))
      {
        str2 = str1;
        localVector.add(str2);
      }
      else
      {
        String str3 = str1;
        addToList(str3, str2, this.indexMap);
        addToList(str2, str3, this.jarMap);
      }
      label117: str1 = localBufferedReader.readLine();
    }
    this.jarFiles = ((String[])(String[])localVector.toArray(new String[localVector.size()]));
  }

  public void merge(JarIndex paramJarIndex, String paramString)
  {
    Iterator localIterator1 = this.indexMap.entrySet().iterator();
    while (localIterator1.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator1.next();
      String str1 = (String)localEntry.getKey();
      LinkedList localLinkedList = (LinkedList)localEntry.getValue();
      Iterator localIterator2 = localLinkedList.iterator();
      while (localIterator2.hasNext())
      {
        String str2 = (String)localIterator2.next();
        if (paramString != null)
          str2 = paramString.concat(str2);
        paramJarIndex.add(str1, str2);
      }
    }
  }
}
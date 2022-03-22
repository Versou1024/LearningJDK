package sun.misc;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.List<Ljava.io.IOException;>;
import java.util.Stack;
import java.util.jar.JarFile;

public class ClassLoaderUtil
{
  public static void releaseLoader(URLClassLoader paramURLClassLoader)
  {
    releaseLoader(paramURLClassLoader, null);
  }

  public static List<IOException> releaseLoader(URLClassLoader paramURLClassLoader, List<String> paramList)
  {
    LinkedList localLinkedList = new LinkedList();
    try
    {
      if (paramList != null)
        paramList.clear();
      System.out.println("classLoader = " + paramURLClassLoader);
      System.out.println("SharedSecrets.getJavaNetAccess()=" + SharedSecrets.getJavaNetAccess());
      URLClassPath localURLClassPath = SharedSecrets.getJavaNetAccess().getURLClassPath(paramURLClassLoader);
      ArrayList localArrayList = localURLClassPath.loaders;
      Stack localStack = localURLClassPath.urls;
      HashMap localHashMap = localURLClassPath.lmap;
      synchronized (localStack)
      {
        localStack.clear();
      }
      synchronized (localHashMap)
      {
        localHashMap.clear();
      }
      synchronized (localURLClassPath)
      {
        Iterator localIterator = localArrayList.iterator();
        while (localIterator.hasNext())
        {
          Object localObject4 = localIterator.next();
          if ((localObject4 != null) && (localObject4 instanceof URLClassPath.JarLoader))
          {
            URLClassPath.JarLoader localJarLoader = (URLClassPath.JarLoader)localObject4;
            JarFile localJarFile = localJarLoader.getJarFile();
            try
            {
              if (localJarFile != null)
              {
                localJarFile.close();
                if (paramList != null)
                  paramList.add(localJarFile.getName());
              }
            }
            catch (IOException localIOException1)
            {
              String str1 = (localJarFile == null) ? "filename not available" : localJarFile.getName();
              String str2 = "Error closing JAR file: " + str1;
              IOException localIOException2 = new IOException(str2);
              localIOException2.initCause(localIOException1);
              localLinkedList.add(localIOException2);
            }
          }
        }
        localArrayList.clear();
      }
    }
    catch (Throwable localThrowable)
    {
      throw new RuntimeException(localThrowable);
    }
    return ((List<IOException>)localLinkedList);
  }
}
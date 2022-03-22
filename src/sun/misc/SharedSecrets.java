package sun.misc;

import java.io.Console;
import java.io.File;
import java.util.jar.JarFile;

public class SharedSecrets
{
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static JavaUtilJarAccess javaUtilJarAccess;
  private static JavaLangAccess javaLangAccess;
  private static JavaIOAccess javaIOAccess;
  private static JavaIODeleteOnExitAccess javaIODeleteOnExitAccess;
  private static JavaNetAccess javaNetAccess;

  public static JavaUtilJarAccess javaUtilJarAccess()
  {
    if (javaUtilJarAccess == null)
      unsafe.ensureClassInitialized(JarFile.class);
    return javaUtilJarAccess;
  }

  public static void setJavaUtilJarAccess(JavaUtilJarAccess paramJavaUtilJarAccess)
  {
    javaUtilJarAccess = paramJavaUtilJarAccess;
  }

  public static void setJavaLangAccess(JavaLangAccess paramJavaLangAccess)
  {
    javaLangAccess = paramJavaLangAccess;
  }

  public static JavaLangAccess getJavaLangAccess()
  {
    return javaLangAccess;
  }

  public static void setJavaNetAccess(JavaNetAccess paramJavaNetAccess)
  {
    javaNetAccess = paramJavaNetAccess;
  }

  public static JavaNetAccess getJavaNetAccess()
  {
    return javaNetAccess;
  }

  public static void setJavaIOAccess(JavaIOAccess paramJavaIOAccess)
  {
    javaIOAccess = paramJavaIOAccess;
  }

  public static JavaIOAccess getJavaIOAccess()
  {
    if (javaIOAccess == null)
      unsafe.ensureClassInitialized(Console.class);
    return javaIOAccess;
  }

  public static void setJavaIODeleteOnExitAccess(JavaIODeleteOnExitAccess paramJavaIODeleteOnExitAccess)
  {
    javaIODeleteOnExitAccess = paramJavaIODeleteOnExitAccess;
  }

  public static JavaIODeleteOnExitAccess getJavaIODeleteOnExitAccess()
  {
    if (javaIODeleteOnExitAccess == null)
      unsafe.ensureClassInitialized(File.class);
    return javaIODeleteOnExitAccess;
  }
}
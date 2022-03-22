package sun.misc;

import java.io.PrintStream;

public class Version
{
  private static final String java_version = "1.6.0_10-rc2";
  private static final String java_runtime_name = "Java(TM) SE Runtime Environment";
  private static final String java_runtime_version = "1.6.0_10-rc2-b32";
  private static boolean versionsInitialized;
  private static int jvm_major_version;
  private static int jvm_minor_version;
  private static int jvm_micro_version;
  private static int jvm_update_version;
  private static int jvm_build_number;
  private static String jvm_special_version;
  private static int jdk_major_version;
  private static int jdk_minor_version;
  private static int jdk_micro_version;
  private static int jdk_update_version;
  private static int jdk_build_number;
  private static String jdk_special_version;
  private static boolean jvmVersionInfoAvailable;

  public static void init()
  {
    System.setProperty("java.version", "1.6.0_10-rc2");
    System.setProperty("java.runtime.version", "1.6.0_10-rc2-b32");
    System.setProperty("java.runtime.name", "Java(TM) SE Runtime Environment");
  }

  public static void print()
  {
    print(System.err);
  }

  public static void print(PrintStream paramPrintStream)
  {
    paramPrintStream.println("java version \"1.6.0_10-rc2\"");
    paramPrintStream.println("Java(TM) SE Runtime Environment (build 1.6.0_10-rc2-b32)");
    String str1 = System.getProperty("java.vm.name");
    String str2 = System.getProperty("java.vm.version");
    String str3 = System.getProperty("java.vm.info");
    paramPrintStream.println(str1 + " (build " + str2 + ", " + str3 + ")");
  }

  public static synchronized int jvmMajorVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    return jvm_major_version;
  }

  public static synchronized int jvmMinorVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    return jvm_minor_version;
  }

  public static synchronized int jvmMicroVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    return jvm_micro_version;
  }

  public static synchronized int jvmUpdateVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    return jvm_update_version;
  }

  public static synchronized String jvmSpecialVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    if (jvm_special_version == null)
      jvm_special_version = getJvmSpecialVersion();
    return jvm_special_version;
  }

  public static native String getJvmSpecialVersion();

  public static synchronized int jvmBuildNumber()
  {
    if (!(versionsInitialized))
      initVersions();
    return jvm_build_number;
  }

  public static synchronized int jdkMajorVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    return jdk_major_version;
  }

  public static synchronized int jdkMinorVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    return jdk_minor_version;
  }

  public static synchronized int jdkMicroVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    return jdk_micro_version;
  }

  public static synchronized int jdkUpdateVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    return jdk_update_version;
  }

  public static synchronized String jdkSpecialVersion()
  {
    if (!(versionsInitialized))
      initVersions();
    if (jdk_special_version == null)
      jdk_special_version = getJdkSpecialVersion();
    return jdk_special_version;
  }

  public static native String getJdkSpecialVersion();

  public static synchronized int jdkBuildNumber()
  {
    if (!(versionsInitialized))
      initVersions();
    return jdk_build_number;
  }

  private static synchronized void initVersions()
  {
    if (versionsInitialized)
      return;
    jvmVersionInfoAvailable = getJvmVersionInfo();
    if (!(jvmVersionInfoAvailable))
    {
      Object localObject = System.getProperty("java.vm.version");
      if ((((CharSequence)localObject).length() >= 5) && (Character.isDigit(((CharSequence)localObject).charAt(0))) && (((CharSequence)localObject).charAt(1) == '.') && (Character.isDigit(((CharSequence)localObject).charAt(2))) && (((CharSequence)localObject).charAt(3) == '.') && (Character.isDigit(((CharSequence)localObject).charAt(4))))
      {
        jvm_major_version = Character.digit(((CharSequence)localObject).charAt(0), 10);
        jvm_minor_version = Character.digit(((CharSequence)localObject).charAt(2), 10);
        jvm_micro_version = Character.digit(((CharSequence)localObject).charAt(4), 10);
        localObject = ((CharSequence)localObject).subSequence(5, ((CharSequence)localObject).length());
        if ((((CharSequence)localObject).charAt(0) == '_') && (((CharSequence)localObject).length() >= 3) && (Character.isDigit(((CharSequence)localObject).charAt(1))) && (Character.isDigit(((CharSequence)localObject).charAt(2))))
        {
          int i = 3;
          try
          {
            String str1 = ((CharSequence)localObject).subSequence(1, 3).toString();
            jvm_update_version = Integer.valueOf(str1).intValue();
            if (((CharSequence)localObject).length() >= 4)
            {
              char c = ((CharSequence)localObject).charAt(3);
              if ((c >= 'a') && (c <= 'z'))
              {
                jvm_special_version = Character.toString(c);
                ++i;
              }
            }
          }
          catch (NumberFormatException localNumberFormatException)
          {
            return;
          }
          localObject = ((CharSequence)localObject).subSequence(i, ((CharSequence)localObject).length());
        }
        if (((CharSequence)localObject).charAt(0) == '-')
        {
          localObject = ((CharSequence)localObject).subSequence(1, ((CharSequence)localObject).length());
          String[] arrayOfString1 = localObject.toString().split("-");
          String[] arrayOfString2 = arrayOfString1;
          int j = arrayOfString2.length;
          for (int k = 0; k < j; ++k)
          {
            String str2 = arrayOfString2[k];
            if ((str2.charAt(0) == 'b') && (str2.length() == 3) && (Character.isDigit(str2.charAt(1))) && (Character.isDigit(str2.charAt(2))))
            {
              jvm_build_number = Integer.valueOf(str2.substring(1, 3)).intValue();
              break;
            }
          }
        }
      }
    }
    getJdkVersionInfo();
    versionsInitialized = true;
  }

  private static native boolean getJvmVersionInfo();

  private static native void getJdkVersionInfo();

  static
  {
    init();
    versionsInitialized = false;
    jvm_major_version = 0;
    jvm_minor_version = 0;
    jvm_micro_version = 0;
    jvm_update_version = 0;
    jvm_build_number = 0;
    jvm_special_version = null;
    jdk_major_version = 0;
    jdk_minor_version = 0;
    jdk_micro_version = 0;
    jdk_update_version = 0;
    jdk_build_number = 0;
    jdk_special_version = null;
  }
}
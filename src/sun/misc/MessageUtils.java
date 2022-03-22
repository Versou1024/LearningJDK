package sun.misc;

public class MessageUtils
{
  public static String subst(String paramString1, String paramString2)
  {
    String[] arrayOfString = { paramString2 };
    return subst(paramString1, arrayOfString);
  }

  public static String subst(String paramString1, String paramString2, String paramString3)
  {
    String[] arrayOfString = { paramString2, paramString3 };
    return subst(paramString1, arrayOfString);
  }

  public static String subst(String paramString1, String paramString2, String paramString3, String paramString4)
  {
    String[] arrayOfString = { paramString2, paramString3, paramString4 };
    return subst(paramString1, arrayOfString);
  }

  public static String subst(String paramString, String[] paramArrayOfString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = paramString.length();
    for (int j = 0; (j >= 0) && (j < i); ++j)
    {
      char c = paramString.charAt(j);
      if (c == '%')
        if (j != i)
        {
          int k = Character.digit(paramString.charAt(j + 1), 10);
          if (k == -1)
          {
            localStringBuffer.append(paramString.charAt(j + 1));
            ++j;
          }
          else if (k < paramArrayOfString.length)
          {
            localStringBuffer.append(paramArrayOfString[k]);
            ++j;
          }
        }
      else
        localStringBuffer.append(c);
    }
    return localStringBuffer.toString();
  }

  public static String substProp(String paramString1, String paramString2)
  {
    return subst(System.getProperty(paramString1), paramString2);
  }

  public static String substProp(String paramString1, String paramString2, String paramString3)
  {
    return subst(System.getProperty(paramString1), paramString2, paramString3);
  }

  public static String substProp(String paramString1, String paramString2, String paramString3, String paramString4)
  {
    return subst(System.getProperty(paramString1), paramString2, paramString3, paramString4);
  }

  public static native void toStderr(String paramString);

  public static native void toStdout(String paramString);

  public static void err(String paramString)
  {
    toStderr(paramString + "\n");
  }

  public static void out(String paramString)
  {
    toStdout(paramString + "\n");
  }

  public static void where()
  {
    Throwable localThrowable = new Throwable();
    StackTraceElement[] arrayOfStackTraceElement = localThrowable.getStackTrace();
    for (int i = 1; i < arrayOfStackTraceElement.length; ++i)
      toStderr("\t" + arrayOfStackTraceElement[i].toString() + "\n");
  }
}
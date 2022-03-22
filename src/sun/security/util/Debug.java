package sun.security.util;

import java.io.PrintStream;
import java.math.BigInteger;
import java.security.AccessController;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.security.action.GetPropertyAction;

public class Debug
{
  private String prefix;
  private static String args = (String)AccessController.doPrivileged(new GetPropertyAction("java.security.debug"));
  private static final char[] hexDigits;

  public static void Help()
  {
    System.err.println();
    System.err.println("all           turn on all debugging");
    System.err.println("access        print all checkPermission results");
    System.err.println("combiner      SubjectDomainCombiner debugging");
    System.err.println("gssloginconfig");
    System.err.println("              GSS LoginConfigImpl debugging");
    System.err.println("jar           jar verification");
    System.err.println("logincontext  login context results");
    System.err.println("policy        loading and granting");
    System.err.println("provider      security provider debugging");
    System.err.println("scl           permissions SecureClassLoader assigns");
    System.err.println();
    System.err.println("The following can be used with access:");
    System.err.println();
    System.err.println("stack         include stack trace");
    System.err.println("domain        dump all domains in context");
    System.err.println("failure       before throwing exception, dump stack");
    System.err.println("              and domain that didn't have permission");
    System.err.println();
    System.err.println("The following can be used with stack and domain:");
    System.err.println();
    System.err.println("permission=<classname>");
    System.err.println("              only dump output if specified permission");
    System.err.println("              is being checked");
    System.err.println("codebase=<URL>");
    System.err.println("              only dump output if specified codebase");
    System.err.println("              is being checked");
    System.err.println();
    System.err.println("Note: Separate multiple options with a comma");
    System.exit(0);
  }

  public static Debug getInstance(String paramString)
  {
    return getInstance(paramString, paramString);
  }

  public static Debug getInstance(String paramString1, String paramString2)
  {
    if (isOn(paramString1))
    {
      Debug localDebug = new Debug();
      localDebug.prefix = paramString2;
      return localDebug;
    }
    return null;
  }

  public static boolean isOn(String paramString)
  {
    if (args == null)
      return false;
    if (args.indexOf("all") != -1)
      return true;
    return (args.indexOf(paramString) != -1);
  }

  public void println(String paramString)
  {
    System.err.println(this.prefix + ": " + paramString);
  }

  public void println()
  {
    System.err.println(this.prefix + ":");
  }

  public static void println(String paramString1, String paramString2)
  {
    System.err.println(paramString1 + ": " + paramString2);
  }

  public static String toHexString(BigInteger paramBigInteger)
  {
    String str = paramBigInteger.toString(16);
    StringBuffer localStringBuffer = new StringBuffer(str.length() * 2);
    if (str.startsWith("-"))
    {
      localStringBuffer.append("   -");
      str = str.substring(1);
    }
    else
    {
      localStringBuffer.append("    ");
    }
    if (str.length() % 2 != 0)
      str = "0" + str;
    int i = 0;
    while (true)
    {
      do
        while (true)
        {
          do
          {
            if (i >= str.length())
              break label150;
            localStringBuffer.append(str.substring(i, i + 2));
          }
          while ((i += 2) == str.length());
          if (i % 64 != 0)
            break;
          localStringBuffer.append("\n    ");
        }
      while (i % 8 != 0);
      localStringBuffer.append(" ");
    }
    label150: return localStringBuffer.toString();
  }

  private static String marshal(String paramString)
  {
    if (paramString != null)
    {
      String str4;
      StringBuffer localStringBuffer1 = new StringBuffer();
      Object localObject = new StringBuffer(paramString);
      String str1 = "[Pp][Ee][Rr][Mm][Ii][Ss][Ss][Ii][Oo][Nn]=";
      String str2 = "permission=";
      String str3 = str1 + "[a-zA-Z_$][a-zA-Z0-9_$]*([.][a-zA-Z_$][a-zA-Z0-9_$]*)*";
      Pattern localPattern = Pattern.compile(str3);
      Matcher localMatcher = localPattern.matcher((CharSequence)localObject);
      StringBuffer localStringBuffer2 = new StringBuffer();
      while (localMatcher.find())
      {
        str4 = localMatcher.group();
        localStringBuffer1.append(str4.replaceFirst(str1, str2));
        localStringBuffer1.append("  ");
        localMatcher.appendReplacement(localStringBuffer2, "");
      }
      localMatcher.appendTail(localStringBuffer2);
      localObject = localStringBuffer2;
      str1 = "[Cc][Oo][Dd][Ee][Bb][Aa][Ss][Ee]=";
      str2 = "codebase=";
      str3 = str1 + "[^, ;]*";
      localPattern = Pattern.compile(str3);
      localMatcher = localPattern.matcher((CharSequence)localObject);
      localStringBuffer2 = new StringBuffer();
      while (localMatcher.find())
      {
        str4 = localMatcher.group();
        localStringBuffer1.append(str4.replaceFirst(str1, str2));
        localStringBuffer1.append("  ");
        localMatcher.appendReplacement(localStringBuffer2, "");
      }
      localMatcher.appendTail(localStringBuffer2);
      localObject = localStringBuffer2;
      localStringBuffer1.append(((StringBuffer)localObject).toString().toLowerCase());
      return localStringBuffer1.toString();
    }
    return ((String)null);
  }

  public static String toString(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte == null)
      return "(null)";
    StringBuilder localStringBuilder = new StringBuilder(paramArrayOfByte.length * 3);
    for (int i = 0; i < paramArrayOfByte.length; ++i)
    {
      int j = paramArrayOfByte[i] & 0xFF;
      if (i != 0)
        localStringBuilder.append(':');
      localStringBuilder.append(hexDigits[(j >>> 4)]);
      localStringBuilder.append(hexDigits[(j & 0xF)]);
    }
    return localStringBuilder.toString();
  }

  static
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("java.security.auth.debug"));
    if (args == null)
      args = str;
    else if (str != null)
      args = args + "," + str;
    if (args != null)
    {
      args = marshal(args);
      if (args.equals("help"))
        Help();
    }
    hexDigits = "0123456789abcdef".toCharArray();
  }
}
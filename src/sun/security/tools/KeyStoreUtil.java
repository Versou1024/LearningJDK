package sun.security.tools;

public class KeyStoreUtil
{
  public static boolean isWindowsKeyStore(String paramString)
  {
    return ((paramString.equalsIgnoreCase("Windows-MY")) || (paramString.equalsIgnoreCase("Windows-ROOT")));
  }

  public static String niceStoreTypeName(String paramString)
  {
    if (paramString.equalsIgnoreCase("Windows-MY"))
      return "Windows-MY";
    if (paramString.equalsIgnoreCase("Windows-ROOT"))
      return "Windows-ROOT";
    return paramString.toUpperCase();
  }
}
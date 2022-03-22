package sun.util.resources;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class TimeZoneNamesBundle extends OpenListResourceBundle
{
  public Object handleGetObject(String paramString)
  {
    String[] arrayOfString1 = (String[])(String[])super.handleGetObject(paramString);
    if (arrayOfString1 == null)
      return null;
    int i = arrayOfString1.length;
    String[] arrayOfString2 = new String[i + 1];
    arrayOfString2[0] = paramString;
    for (int j = 0; j < i; ++j)
      arrayOfString2[(j + 1)] = arrayOfString1[j];
    return arrayOfString2;
  }

  protected Map createMap(int paramInt)
  {
    return new LinkedHashMap(paramInt);
  }

  protected abstract Object[][] getContents();
}
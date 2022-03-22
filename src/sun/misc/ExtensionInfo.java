package sun.misc;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

public class ExtensionInfo
{
  public static final int COMPATIBLE = 0;
  public static final int REQUIRE_SPECIFICATION_UPGRADE = 1;
  public static final int REQUIRE_IMPLEMENTATION_UPGRADE = 2;
  public static final int REQUIRE_VENDOR_SWITCH = 3;
  public static final int INCOMPATIBLE = 4;
  public String title;
  public String name;
  public String specVersion;
  public String specVendor;
  public String implementationVersion;
  public String vendor;
  public String vendorId;
  public String url;
  private static final ResourceBundle rb = ResourceBundle.getBundle("sun.misc.resources.Messages");

  public ExtensionInfo()
  {
  }

  public ExtensionInfo(String paramString, Attributes paramAttributes)
    throws NullPointerException
  {
    if (paramString != null)
      str1 = paramString + "-";
    else
      str1 = "";
    String str2 = str1 + Attributes.Name.EXTENSION_NAME.toString();
    this.name = paramAttributes.getValue(str2);
    if (this.name != null)
      this.name = this.name.trim();
    str2 = str1 + Attributes.Name.SPECIFICATION_TITLE.toString();
    this.title = paramAttributes.getValue(str2);
    if (this.title != null)
      this.title = this.title.trim();
    str2 = str1 + Attributes.Name.SPECIFICATION_VERSION.toString();
    this.specVersion = paramAttributes.getValue(str2);
    if (this.specVersion != null)
      this.specVersion = this.specVersion.trim();
    str2 = str1 + Attributes.Name.SPECIFICATION_VENDOR.toString();
    this.specVendor = paramAttributes.getValue(str2);
    if (this.specVendor != null)
      this.specVendor = this.specVendor.trim();
    str2 = str1 + Attributes.Name.IMPLEMENTATION_VERSION.toString();
    this.implementationVersion = paramAttributes.getValue(str2);
    if (this.implementationVersion != null)
      this.implementationVersion = this.implementationVersion.trim();
    str2 = str1 + Attributes.Name.IMPLEMENTATION_VENDOR.toString();
    this.vendor = paramAttributes.getValue(str2);
    if (this.vendor != null)
      this.vendor = this.vendor.trim();
    str2 = str1 + Attributes.Name.IMPLEMENTATION_VENDOR_ID.toString();
    this.vendorId = paramAttributes.getValue(str2);
    if (this.vendorId != null)
      this.vendorId = this.vendorId.trim();
    str2 = str1 + Attributes.Name.IMPLEMENTATION_URL.toString();
    this.url = paramAttributes.getValue(str2);
    if (this.url != null)
      this.url = this.url.trim();
  }

  public int isCompatibleWith(ExtensionInfo paramExtensionInfo)
  {
    if ((this.name == null) || (paramExtensionInfo.name == null))
      return 4;
    if (this.name.compareTo(paramExtensionInfo.name) == 0)
    {
      if ((this.specVersion == null) || (paramExtensionInfo.specVersion == null))
        return 0;
      int i = compareExtensionVersion(this.specVersion, paramExtensionInfo.specVersion);
      if (i < 0)
      {
        if ((this.vendorId != null) && (paramExtensionInfo.vendorId != null) && (this.vendorId.compareTo(paramExtensionInfo.vendorId) != 0))
          return 3;
        return 1;
      }
      if ((this.vendorId != null) && (paramExtensionInfo.vendorId != null))
      {
        if (this.vendorId.compareTo(paramExtensionInfo.vendorId) != 0)
          return 3;
        if ((this.implementationVersion != null) && (paramExtensionInfo.implementationVersion != null))
        {
          i = compareExtensionVersion(this.implementationVersion, paramExtensionInfo.implementationVersion);
          if (i < 0)
            return 2;
        }
      }
      return 0;
    }
    return 4;
  }

  public String toString()
  {
    return "Extension : title(" + this.title + "), name(" + this.name + "), spec vendor(" + this.specVendor + "), spec version(" + this.specVersion + "), impl vendor(" + this.vendor + "), impl vendor id(" + this.vendorId + "), impl version(" + this.implementationVersion + "), impl url(" + this.url + ")";
  }

  private int compareExtensionVersion(String paramString1, String paramString2)
    throws NumberFormatException
  {
    paramString1 = paramString1.toLowerCase();
    paramString2 = paramString2.toLowerCase();
    return strictCompareExtensionVersion(paramString1, paramString2);
  }

  private int strictCompareExtensionVersion(String paramString1, String paramString2)
    throws NumberFormatException
  {
    if (paramString1.equals(paramString2))
      return 0;
    StringTokenizer localStringTokenizer1 = new StringTokenizer(paramString1, ".,");
    StringTokenizer localStringTokenizer2 = new StringTokenizer(paramString2, ".,");
    int i = 0;
    int j = 0;
    int k = 0;
    if (localStringTokenizer1.hasMoreTokens())
      i = convertToken(localStringTokenizer1.nextToken().toString());
    if (localStringTokenizer2.hasMoreTokens())
      j = convertToken(localStringTokenizer2.nextToken().toString());
    if (i > j)
      return 1;
    if (j > i)
      return -1;
    int l = paramString1.indexOf(".");
    int i1 = paramString2.indexOf(".");
    if (l == -1)
      l = paramString1.length() - 1;
    if (i1 == -1)
      i1 = paramString2.length() - 1;
    return strictCompareExtensionVersion(paramString1.substring(l + 1), paramString2.substring(i1 + 1));
  }

  private int convertToken(String paramString)
  {
    int i4;
    if ((paramString == null) || (paramString.equals("")))
      return 0;
    int i = 0;
    int j = 0;
    int k = 0;
    int l = paramString.length();
    int i1 = l;
    Object[] arrayOfObject = { this.name };
    MessageFormat localMessageFormat = new MessageFormat(rb.getString("optpkg.versionerror"));
    String str1 = localMessageFormat.format(arrayOfObject);
    int i2 = paramString.indexOf("-");
    int i3 = paramString.indexOf("_");
    if ((i2 == -1) && (i3 == -1))
      try
      {
        return (Integer.parseInt(paramString) * 100);
      }
      catch (NumberFormatException localNumberFormatException1)
      {
        System.out.println(str1);
        return 0;
      }
    if (i3 != -1)
    {
      try
      {
        i4 = Integer.parseInt(paramString.substring(0, i3));
        char c = paramString.charAt(l - 1);
        if (Character.isLetter(c))
        {
          i = Character.getNumericValue(c);
          i1 = l - 1;
          k = Integer.parseInt(paramString.substring(i3 + 1, i1));
          if ((i >= Character.getNumericValue('a')) && (i <= Character.getNumericValue('z')))
          {
            j = k * 100 + i;
          }
          else
          {
            j = 0;
            System.out.println(str1);
          }
        }
        else
        {
          k = Integer.parseInt(paramString.substring(i3 + 1, i1));
        }
      }
      catch (NumberFormatException localNumberFormatException2)
      {
        System.out.println(str1);
        return 0;
      }
      return (i4 * 100 + k + j);
    }
    try
    {
      i4 = Integer.parseInt(paramString.substring(0, i2));
    }
    catch (NumberFormatException localNumberFormatException3)
    {
      System.out.println(str1);
      return 0;
    }
    String str2 = paramString.substring(i2 + 1);
    String str3 = "";
    int i5 = 0;
    if (str2.indexOf("ea") != -1)
    {
      str3 = str2.substring(2);
      i5 = 50;
    }
    else if (str2.indexOf("alpha") != -1)
    {
      str3 = str2.substring(5);
      i5 = 40;
    }
    else if (str2.indexOf("beta") != -1)
    {
      str3 = str2.substring(4);
      i5 = 30;
    }
    else if (str2.indexOf("rc") != -1)
    {
      str3 = str2.substring(2);
      i5 = 20;
    }
    if ((str3 == null) || (str3.equals("")))
      return (i4 * 100 - i5);
    try
    {
      return (i4 * 100 - i5 + Integer.parseInt(str3));
    }
    catch (NumberFormatException localNumberFormatException4)
    {
      System.out.println(str1);
    }
    return 0;
  }
}
package sun.awt;

import java.util.HashMap;

class PackageDebugHelperImpl extends DebugHelperImpl
{
  private static HashMap hashMap = new HashMap();
  private String packageName;

  private PackageDebugHelperImpl(Package paramPackage)
  {
    super(globalDebugHelperImpl);
    this.packageName = paramPackage.getName();
    loadSettings();
  }

  public synchronized String getString(String paramString1, String paramString2)
  {
    return super.getString(paramString1 + "." + this.packageName, paramString2);
  }

  static DebugHelperImpl getInstance(Package paramPackage)
  {
    if (paramPackage == null)
      return globalDebugHelperImpl;
    PackageDebugHelperImpl localPackageDebugHelperImpl = (PackageDebugHelperImpl)hashMap.get(paramPackage);
    if (localPackageDebugHelperImpl == null)
    {
      localPackageDebugHelperImpl = new PackageDebugHelperImpl(paramPackage);
      hashMap.put(paramPackage, localPackageDebugHelperImpl);
    }
    return localPackageDebugHelperImpl;
  }
}
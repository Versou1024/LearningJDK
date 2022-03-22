package sun.security.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ResourceBundle;

public class ResourcesMgr
{
  private static ResourceBundle bundle;
  private static ResourceBundle altBundle;

  public static String getString(String paramString)
  {
    if (bundle == null)
      bundle = (ResourceBundle)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          return ResourceBundle.getBundle("sun.security.util.Resources");
        }
      });
    return bundle.getString(paramString);
  }

  public static String getString(String paramString1, String paramString2)
  {
    if (altBundle == null)
      altBundle = (ResourceBundle)AccessController.doPrivileged(new PrivilegedAction(paramString2)
      {
        public Object run()
        {
          return ResourceBundle.getBundle(this.val$altBundleName);
        }
      });
    return altBundle.getString(paramString1);
  }
}
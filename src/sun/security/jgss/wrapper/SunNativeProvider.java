package sun.security.jgss.wrapper;

import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.HashMap;
import sun.security.action.PutAllAction;

public final class SunNativeProvider extends Provider
{
  private static final long serialVersionUID = -238911724858694204L;
  private static final String NAME = "SunNativeGSS";
  private static final String INFO = "Sun Native GSS provider";
  private static final String MF_CLASS = "sun.security.jgss.wrapper.NativeGSSFactory";
  private static final String LIB_PROP = "sun.security.jgss.lib";
  private static final String DEBUG_PROP = "sun.security.nativegss.debug";
  private static HashMap MECH_MAP;
  static final Provider INSTANCE = new SunNativeProvider();
  static boolean DEBUG;

  static void debug(String paramString)
  {
    if (DEBUG)
    {
      if (paramString == null)
        throw new NullPointerException();
      System.out.println("SunNativeGSS: " + paramString);
    }
  }

  public SunNativeProvider()
  {
    super("SunNativeGSS", 1D, "Sun Native GSS provider");
    if (MECH_MAP != null)
      AccessController.doPrivileged(new PutAllAction(this, MECH_MAP));
  }

  static
  {
    MECH_MAP = (HashMap)AccessController.doPrivileged(new PrivilegedAction()
    {
      public HashMap run()
      {
        Object localObject;
        SunNativeProvider.DEBUG = Boolean.parseBoolean(System.getProperty("sun.security.nativegss.debug"));
        try
        {
          System.loadLibrary("j2gss");
        }
        catch (Error localError)
        {
          SunNativeProvider.debug("No j2gss library found!");
          if (SunNativeProvider.DEBUG)
            localError.printStackTrace();
          return null;
        }
        String str = System.getProperty("sun.security.jgss.lib");
        if ((str == null) || (str.trim().equals("")))
        {
          localObject = System.getProperty("os.name");
          if (((String)localObject).startsWith("SunOS"))
            str = "libgss.so";
          else if (((String)localObject).startsWith("Linux"))
            str = "libgssapi.so";
        }
        if (GSSLibStub.init(str))
        {
          SunNativeProvider.debug("Loaded GSS library: " + str);
          localObject = GSSLibStub.indicateMechs();
          HashMap localHashMap = new HashMap();
          for (int i = 0; i < localObject.length; ++i)
          {
            SunNativeProvider.debug("Native MF for " + localObject[i]);
            localHashMap.put("GssApiMechanism." + localObject[i], "sun.security.jgss.wrapper.NativeGSSFactory");
          }
          return localHashMap;
        }
        return ((HashMap)null);
      }
    });
  }
}
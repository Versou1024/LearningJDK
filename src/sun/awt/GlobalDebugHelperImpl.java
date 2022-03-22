package sun.awt;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

class GlobalDebugHelperImpl extends DebugHelperImpl
{
  private static final String PROP_CTRACE = "ctrace";
  private static final int PROP_CTRACE_LEN = "ctrace".length();
  private static DebugHelperImpl instance = null;
  private boolean ctracingOn;

  static final DebugHelperImpl getInstance()
  {
    if (instance == null)
      instance = new GlobalDebugHelperImpl();
    return instance;
  }

  private GlobalDebugHelperImpl()
  {
    super(null);
    setParent(this);
    loadSettings();
  }

  protected void loadSettings()
  {
    super.loadSettings();
    loadNativeSettings();
  }

  private void loadNativeSettings()
  {
    boolean bool1 = getBoolean("ctrace", false);
    setCTracingOn(bool1);
    Vector localVector = new Vector();
    Enumeration localEnumeration = settings.getPropertyNames();
    while (localEnumeration.hasMoreElements())
    {
      localObject = (String)localEnumeration.nextElement();
      if ((((String)localObject).startsWith("ctrace")) && (((String)localObject).length() > PROP_CTRACE_LEN))
        localVector.addElement(localObject);
    }
    Collections.sort(localVector);
    Object localObject = localVector.elements();
    while (((Enumeration)localObject).hasMoreElements())
    {
      String str1 = (String)((Enumeration)localObject).nextElement();
      String str2 = str1.substring(PROP_CTRACE_LEN + 1);
      int i = str2.indexOf(64);
      String str3 = (i != -1) ? str2.substring(0, i) : str2;
      String str4 = (i != -1) ? str2.substring(i + 1) : "";
      boolean bool2 = settings.getBoolean(str1, false);
      if (str4.length() == 0)
      {
        setCTracingOn(bool2, str3);
      }
      else
      {
        int j = Integer.parseInt(str4, 10);
        setCTracingOn(bool2, str3, j);
      }
    }
  }
}
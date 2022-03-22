package sun.management;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HotSpotDiagnostic
  implements HotSpotDiagnosticMXBean
{
  public native void dumpHeap(String paramString, boolean paramBoolean)
    throws IOException;

  public List<VMOption> getDiagnosticOptions()
  {
    List localList = Flag.getAllFlags();
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = localList.iterator();
    while (localIterator.hasNext())
    {
      Flag localFlag = (Flag)localIterator.next();
      if ((localFlag.isWriteable()) && (localFlag.isExternal()))
        localArrayList.add(localFlag.getVMOption());
    }
    return localArrayList;
  }

  public VMOption getVMOption(String paramString)
  {
    if (paramString == null)
      throw new NullPointerException("name cannot be null");
    Flag localFlag = Flag.getFlag(paramString);
    if (localFlag == null)
      throw new IllegalArgumentException("VM option \"" + paramString + "\" does not exist");
    return localFlag.getVMOption();
  }

  public void setVMOption(String paramString1, String paramString2)
  {
    if (paramString1 == null)
      throw new NullPointerException("name cannot be null");
    if (paramString2 == null)
      throw new NullPointerException("value cannot be null");
    ManagementFactory.checkControlAccess();
    Flag localFlag = Flag.getFlag(paramString1);
    if (localFlag == null)
      throw new IllegalArgumentException("VM option \"" + paramString1 + "\" does not exist");
    if (!(localFlag.isWriteable()))
      throw new IllegalArgumentException("VM Option \"" + paramString1 + "\" is not writeable");
    Object localObject = localFlag.getValue();
    if (localObject instanceof Long)
      try
      {
        long l = Long.parseLong(paramString2);
        Flag.setLongValue(paramString1, l);
      }
      catch (NumberFormatException localNumberFormatException)
      {
        IllegalArgumentException localIllegalArgumentException = new IllegalArgumentException("Invalid value: VM Option \"" + paramString1 + "\"" + " expects numeric value");
        localIllegalArgumentException.initCause(localNumberFormatException);
        throw localIllegalArgumentException;
      }
    if (localObject instanceof Boolean)
    {
      if ((!(paramString2.equalsIgnoreCase("true"))) && (!(paramString2.equalsIgnoreCase("false"))))
        throw new IllegalArgumentException("Invalid value: VM Option \"" + paramString1 + "\"" + " expects \"true\" or \"false\".");
      Flag.setBooleanValue(paramString1, Boolean.parseBoolean(paramString2));
    }
    else if (localObject instanceof String)
    {
      Flag.setStringValue(paramString1, paramString2);
    }
    else
    {
      throw new IllegalArgumentException("VM Option \"" + paramString1 + "\" is of an unsupported type: " + localObject.getClass().getName());
    }
  }
}
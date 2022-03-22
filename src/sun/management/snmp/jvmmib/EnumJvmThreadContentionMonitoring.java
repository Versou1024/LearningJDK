package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.Enumerated;
import java.io.Serializable;
import java.util.Hashtable;

public class EnumJvmThreadContentionMonitoring extends Enumerated
  implements Serializable
{
  protected static Hashtable intTable = new Hashtable();
  protected static Hashtable stringTable = new Hashtable();

  public EnumJvmThreadContentionMonitoring(int paramInt)
    throws IllegalArgumentException
  {
    super(paramInt);
  }

  public EnumJvmThreadContentionMonitoring(Integer paramInteger)
    throws IllegalArgumentException
  {
    super(paramInteger);
  }

  public EnumJvmThreadContentionMonitoring()
    throws IllegalArgumentException
  {
  }

  public EnumJvmThreadContentionMonitoring(String paramString)
    throws IllegalArgumentException
  {
    super(paramString);
  }

  protected Hashtable getIntTable()
  {
    return intTable;
  }

  protected Hashtable getStringTable()
  {
    return stringTable;
  }

  static
  {
    intTable.put(new Integer(3), "enabled");
    intTable.put(new Integer(4), "disabled");
    intTable.put(new Integer(1), "unsupported");
    stringTable.put("enabled", new Integer(3));
    stringTable.put("disabled", new Integer(4));
    stringTable.put("unsupported", new Integer(1));
  }
}
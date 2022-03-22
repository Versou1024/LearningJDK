package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.Enumerated;
import java.io.Serializable;
import java.util.Hashtable;

public class EnumJvmMemPoolThreshdSupport extends Enumerated
  implements Serializable
{
  protected static Hashtable intTable = new Hashtable();
  protected static Hashtable stringTable = new Hashtable();

  public EnumJvmMemPoolThreshdSupport(int paramInt)
    throws IllegalArgumentException
  {
    super(paramInt);
  }

  public EnumJvmMemPoolThreshdSupport(Integer paramInteger)
    throws IllegalArgumentException
  {
    super(paramInteger);
  }

  public EnumJvmMemPoolThreshdSupport()
    throws IllegalArgumentException
  {
  }

  public EnumJvmMemPoolThreshdSupport(String paramString)
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
    intTable.put(new Integer(2), "supported");
    intTable.put(new Integer(1), "unsupported");
    stringTable.put("supported", new Integer(2));
    stringTable.put("unsupported", new Integer(1));
  }
}
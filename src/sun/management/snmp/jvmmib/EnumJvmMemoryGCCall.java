package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.Enumerated;
import java.io.Serializable;
import java.util.Hashtable;

public class EnumJvmMemoryGCCall extends Enumerated
  implements Serializable
{
  protected static Hashtable intTable = new Hashtable();
  protected static Hashtable stringTable = new Hashtable();

  public EnumJvmMemoryGCCall(int paramInt)
    throws IllegalArgumentException
  {
    super(paramInt);
  }

  public EnumJvmMemoryGCCall(Integer paramInteger)
    throws IllegalArgumentException
  {
    super(paramInteger);
  }

  public EnumJvmMemoryGCCall()
    throws IllegalArgumentException
  {
  }

  public EnumJvmMemoryGCCall(String paramString)
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
    intTable.put(new Integer(5), "failed");
    intTable.put(new Integer(4), "started");
    intTable.put(new Integer(1), "unsupported");
    intTable.put(new Integer(3), "start");
    stringTable.put("supported", new Integer(2));
    stringTable.put("failed", new Integer(5));
    stringTable.put("started", new Integer(4));
    stringTable.put("unsupported", new Integer(1));
    stringTable.put("start", new Integer(3));
  }
}
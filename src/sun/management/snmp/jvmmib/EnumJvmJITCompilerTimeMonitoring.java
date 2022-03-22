package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.Enumerated;
import java.io.Serializable;
import java.util.Hashtable;

public class EnumJvmJITCompilerTimeMonitoring extends Enumerated
  implements Serializable
{
  protected static Hashtable intTable = new Hashtable();
  protected static Hashtable stringTable = new Hashtable();

  public EnumJvmJITCompilerTimeMonitoring(int paramInt)
    throws IllegalArgumentException
  {
    super(paramInt);
  }

  public EnumJvmJITCompilerTimeMonitoring(Integer paramInteger)
    throws IllegalArgumentException
  {
    super(paramInteger);
  }

  public EnumJvmJITCompilerTimeMonitoring()
    throws IllegalArgumentException
  {
  }

  public EnumJvmJITCompilerTimeMonitoring(String paramString)
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
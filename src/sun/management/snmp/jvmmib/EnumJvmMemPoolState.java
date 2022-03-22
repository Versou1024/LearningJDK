package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.Enumerated;
import java.io.Serializable;
import java.util.Hashtable;

public class EnumJvmMemPoolState extends Enumerated
  implements Serializable
{
  protected static Hashtable intTable = new Hashtable();
  protected static Hashtable stringTable = new Hashtable();

  public EnumJvmMemPoolState(int paramInt)
    throws IllegalArgumentException
  {
    super(paramInt);
  }

  public EnumJvmMemPoolState(Integer paramInteger)
    throws IllegalArgumentException
  {
    super(paramInteger);
  }

  public EnumJvmMemPoolState()
    throws IllegalArgumentException
  {
  }

  public EnumJvmMemPoolState(String paramString)
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
    intTable.put(new Integer(2), "valid");
    intTable.put(new Integer(1), "invalid");
    stringTable.put("valid", new Integer(2));
    stringTable.put("invalid", new Integer(1));
  }
}
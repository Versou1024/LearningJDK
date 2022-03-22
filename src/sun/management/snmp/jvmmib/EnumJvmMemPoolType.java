package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.Enumerated;
import java.io.Serializable;
import java.util.Hashtable;

public class EnumJvmMemPoolType extends Enumerated
  implements Serializable
{
  protected static Hashtable intTable = new Hashtable();
  protected static Hashtable stringTable = new Hashtable();

  public EnumJvmMemPoolType(int paramInt)
    throws IllegalArgumentException
  {
    super(paramInt);
  }

  public EnumJvmMemPoolType(Integer paramInteger)
    throws IllegalArgumentException
  {
    super(paramInteger);
  }

  public EnumJvmMemPoolType()
    throws IllegalArgumentException
  {
  }

  public EnumJvmMemPoolType(String paramString)
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
    intTable.put(new Integer(2), "heap");
    intTable.put(new Integer(1), "nonheap");
    stringTable.put("heap", new Integer(2));
    stringTable.put("nonheap", new Integer(1));
  }
}
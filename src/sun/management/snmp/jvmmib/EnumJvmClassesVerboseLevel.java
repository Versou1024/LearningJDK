package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.Enumerated;
import java.io.Serializable;
import java.util.Hashtable;

public class EnumJvmClassesVerboseLevel extends Enumerated
  implements Serializable
{
  protected static Hashtable intTable = new Hashtable();
  protected static Hashtable stringTable = new Hashtable();

  public EnumJvmClassesVerboseLevel(int paramInt)
    throws IllegalArgumentException
  {
    super(paramInt);
  }

  public EnumJvmClassesVerboseLevel(Integer paramInteger)
    throws IllegalArgumentException
  {
    super(paramInteger);
  }

  public EnumJvmClassesVerboseLevel()
    throws IllegalArgumentException
  {
  }

  public EnumJvmClassesVerboseLevel(String paramString)
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
    intTable.put(new Integer(2), "verbose");
    intTable.put(new Integer(1), "silent");
    stringTable.put("verbose", new Integer(2));
    stringTable.put("silent", new Integer(1));
  }
}
package sun.security.x509;

import java.lang.reflect.Field;
import sun.misc.HexDumpEncoder;

class UnparseableExtension extends Extension
{
  private String name = "";
  private Throwable why;

  public UnparseableExtension(Extension paramExtension, Throwable paramThrowable)
  {
    super(paramExtension);
    try
    {
      Class localClass = OIDMap.getClass(paramExtension.getExtensionId());
      if (localClass != null)
      {
        Field localField = localClass.getDeclaredField("NAME");
        this.name = ((String)(String)localField.get(null)) + " ";
      }
    }
    catch (Exception localException)
    {
    }
    this.why = paramThrowable;
  }

  public String toString()
  {
    return super.toString() + "Unparseable " + this.name + "extension due to\n" + this.why + "\n\n" + new HexDumpEncoder().encodeBuffer(getExtensionValue());
  }
}
package sun.print;

import javax.print.AttributeException;
import javax.print.PrintException;
import javax.print.attribute.Attribute;

class PrintJobAttributeException extends PrintException
  implements AttributeException
{
  private Attribute attr;
  private Class category;

  PrintJobAttributeException(String paramString, Class paramClass, Attribute paramAttribute)
  {
    super(paramString);
    this.attr = paramAttribute;
    this.category = paramClass;
  }

  public Class[] getUnsupportedAttributes()
  {
    if (this.category == null)
      return null;
    Class[] arrayOfClass = { this.category };
    return arrayOfClass;
  }

  public Attribute[] getUnsupportedValues()
  {
    if (this.attr == null)
      return null;
    Attribute[] arrayOfAttribute = { this.attr };
    return arrayOfAttribute;
  }
}
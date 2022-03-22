package sun.reflect.annotation;

public class EnumConstantNotPresentExceptionProxy extends ExceptionProxy
{
  Class<? extends Enum> enumType;
  String constName;

  public EnumConstantNotPresentExceptionProxy(Class<? extends Enum> paramClass, String paramString)
  {
    this.enumType = paramClass;
    this.constName = paramString;
  }

  protected RuntimeException generateException()
  {
    return new EnumConstantNotPresentException(this.enumType, this.constName);
  }
}
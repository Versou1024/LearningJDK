package sun.reflect.annotation;

public class TypeNotPresentExceptionProxy extends ExceptionProxy
{
  String typeName;
  Throwable cause;

  public TypeNotPresentExceptionProxy(String paramString, Throwable paramThrowable)
  {
    this.typeName = paramString;
    this.cause = paramThrowable;
  }

  protected RuntimeException generateException()
  {
    return new TypeNotPresentException(this.typeName, this.cause);
  }
}
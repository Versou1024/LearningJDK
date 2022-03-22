package sun.management;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;

public class StackTraceElementCompositeData extends LazyCompositeData
{
  private final StackTraceElement ste;
  private static final CompositeType stackTraceElementCompositeType;
  private static final String CLASS_NAME = "className";
  private static final String METHOD_NAME = "methodName";
  private static final String FILE_NAME = "fileName";
  private static final String LINE_NUMBER = "lineNumber";
  private static final String NATIVE_METHOD = "nativeMethod";
  private static final String[] stackTraceElementItemNames;

  private StackTraceElementCompositeData(StackTraceElement paramStackTraceElement)
  {
    this.ste = paramStackTraceElement;
  }

  public StackTraceElement getStackTraceElement()
  {
    return this.ste;
  }

  public static StackTraceElement from(CompositeData paramCompositeData)
  {
    validateCompositeData(paramCompositeData);
    return new StackTraceElement(getString(paramCompositeData, "className"), getString(paramCompositeData, "methodName"), getString(paramCompositeData, "fileName"), getInt(paramCompositeData, "lineNumber"));
  }

  public static CompositeData toCompositeData(StackTraceElement paramStackTraceElement)
  {
    StackTraceElementCompositeData localStackTraceElementCompositeData = new StackTraceElementCompositeData(paramStackTraceElement);
    return localStackTraceElementCompositeData.getCompositeData();
  }

  protected CompositeData getCompositeData()
  {
    Object[] arrayOfObject = { this.ste.getClassName(), this.ste.getMethodName(), this.ste.getFileName(), new Integer(this.ste.getLineNumber()), new Boolean(this.ste.isNativeMethod()) };
    try
    {
      return new CompositeDataSupport(stackTraceElementCompositeType, stackTraceElementItemNames, arrayOfObject);
    }
    catch (OpenDataException localOpenDataException)
    {
      throw Util.newInternalError(localOpenDataException);
    }
  }

  public static void validateCompositeData(CompositeData paramCompositeData)
  {
    if (paramCompositeData == null)
      throw new NullPointerException("Null CompositeData");
    if (!(isTypeMatched(stackTraceElementCompositeType, paramCompositeData.getCompositeType())))
      throw new IllegalArgumentException("Unexpected composite type for StackTraceElement");
  }

  static
  {
    try
    {
      stackTraceElementCompositeType = (CompositeType)MappedMXBeanType.toOpenType(StackTraceElement.class);
    }
    catch (OpenDataException localOpenDataException)
    {
      throw Util.newInternalError(localOpenDataException);
    }
    stackTraceElementItemNames = { "className", "methodName", "fileName", "lineNumber", "nativeMethod" };
  }
}
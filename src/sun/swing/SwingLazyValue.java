package sun.swing;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.swing.UIDefaults;
import javax.swing.UIDefaults.LazyValue;
import javax.swing.plaf.ColorUIResource;

public class SwingLazyValue
  implements UIDefaults.LazyValue
{
  private String className;
  private String methodName;
  private Object[] args;

  public SwingLazyValue(String paramString)
  {
    this(paramString, (String)null);
  }

  public SwingLazyValue(String paramString1, String paramString2)
  {
    this(paramString1, paramString2, null);
  }

  public SwingLazyValue(String paramString, Object[] paramArrayOfObject)
  {
    this(paramString, null, paramArrayOfObject);
  }

  public SwingLazyValue(String paramString1, String paramString2, Object[] paramArrayOfObject)
  {
    this.className = paramString1;
    this.methodName = paramString2;
    if (paramArrayOfObject != null)
      this.args = ((Object[])(Object[])paramArrayOfObject.clone());
  }

  public Object createValue(UIDefaults paramUIDefaults)
  {
    Class localClass;
    try
    {
      localClass = Class.forName(this.className, true, null);
      if (this.methodName != null)
      {
        arrayOfClass = getClassArray(this.args);
        localObject = localClass.getMethod(this.methodName, arrayOfClass);
        return ((Method)localObject).invoke(localClass, this.args);
      }
      Class[] arrayOfClass = getClassArray(this.args);
      Object localObject = localClass.getConstructor(arrayOfClass);
      return ((Constructor)localObject).newInstance(this.args);
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  private Class[] getClassArray(Object[] paramArrayOfObject)
  {
    Class[] arrayOfClass = null;
    if (paramArrayOfObject != null)
    {
      arrayOfClass = new Class[paramArrayOfObject.length];
      for (int i = 0; i < paramArrayOfObject.length; ++i)
        if (paramArrayOfObject[i] instanceof Integer)
          arrayOfClass[i] = Integer.TYPE;
        else if (paramArrayOfObject[i] instanceof Boolean)
          arrayOfClass[i] = Boolean.TYPE;
        else if (paramArrayOfObject[i] instanceof ColorUIResource)
          arrayOfClass[i] = Color.class;
        else
          arrayOfClass[i] = paramArrayOfObject[i].getClass();
    }
    return arrayOfClass;
  }
}
package sun.awt;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

class ClassDebugHelperImpl extends DebugHelperImpl
{
  private String className;

  ClassDebugHelperImpl(Class paramClass)
  {
    super(globalDebugHelperImpl);
    checkDeclaration(paramClass);
    this.className = paramClass.getName();
    Package localPackage = paramClass.getPackage();
    DebugHelperImpl localDebugHelperImpl = PackageDebugHelperImpl.getInstance(localPackage);
    setParent(localDebugHelperImpl);
    loadSettings();
  }

  public synchronized String getString(String paramString1, String paramString2)
  {
    return super.getString(paramString1 + "." + this.className, paramString2);
  }

  private void checkDeclaration(Class paramClass)
  {
    int i = 0;
    Field localField = null;
    Class localClass = paramClass;
    localField = (Field)AccessController.doPrivileged(new PrivilegedAction(this, localClass)
    {
      public Object run()
      {
        Field localField;
        try
        {
          localField = this.val$classToCheck.getDeclaredField("dbg");
          localField.setAccessible(true);
          return localField;
        }
        catch (NoSuchFieldException localNoSuchFieldException)
        {
          localNoSuchFieldException.printStackTrace();
        }
        catch (SecurityException localSecurityException)
        {
          localSecurityException.printStackTrace();
        }
        return null;
      }
    });
    i = ((localField != null) && (localField.getType() == DebugHelper.class) && (java.lang.reflect.Modifier.isPrivate(localField.getModifiers())) && (java.lang.reflect.Modifier.isStatic(localField.getModifiers())) && (java.lang.reflect.Modifier.isFinal(localField.getModifiers()))) ? 1 : 0;
    if (i == 0)
      throw new DebugHelperImpl.AssertionFailure(this, "Incorrect or missing declaration of dbg field. Must be declared 'private static final DebugHelper dbg'");
  }
}
package sun.org.mozilla.javascript.internal.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.Undefined;
import sun.org.mozilla.javascript.internal.UniqueTag;

public class ScriptableInputStream extends ObjectInputStream
{
  private Scriptable scope;
  private ClassLoader classLoader;

  public ScriptableInputStream(InputStream paramInputStream, Scriptable paramScriptable)
    throws IOException
  {
    super(paramInputStream);
    this.scope = paramScriptable;
    enableResolveObject(true);
    Context localContext = Context.getCurrentContext();
    if (localContext != null)
      this.classLoader = localContext.getApplicationClassLoader();
  }

  protected Class resolveClass(ObjectStreamClass paramObjectStreamClass)
    throws IOException, ClassNotFoundException
  {
    String str = paramObjectStreamClass.getName();
    if (this.classLoader != null)
      try
      {
        return this.classLoader.loadClass(str);
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
      }
    return super.resolveClass(paramObjectStreamClass);
  }

  protected Object resolveObject(Object paramObject)
    throws IOException
  {
    if (paramObject instanceof ScriptableOutputStream.PendingLookup)
    {
      String str = ((ScriptableOutputStream.PendingLookup)paramObject).getName();
      paramObject = ScriptableOutputStream.lookupQualifiedName(this.scope, str);
      if (paramObject == Scriptable.NOT_FOUND)
        throw new IOException("Object " + str + " not found upon " + "deserialization.");
    }
    else if (paramObject instanceof UniqueTag)
    {
      paramObject = ((UniqueTag)paramObject).readResolve();
    }
    else if (paramObject instanceof Undefined)
    {
      paramObject = ((Undefined)paramObject).readResolve();
    }
    return paramObject;
  }
}
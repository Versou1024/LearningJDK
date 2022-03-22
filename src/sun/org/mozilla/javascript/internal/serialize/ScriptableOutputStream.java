package sun.org.mozilla.javascript.internal.serialize;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.StringTokenizer;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

public class ScriptableOutputStream extends ObjectOutputStream
{
  private Scriptable scope;
  private Hashtable table;

  public ScriptableOutputStream(OutputStream paramOutputStream, Scriptable paramScriptable)
    throws IOException
  {
    super(paramOutputStream);
    this.scope = paramScriptable;
    this.table = new Hashtable(31);
    this.table.put(paramScriptable, "");
    enableReplaceObject(true);
    excludeStandardObjectNames();
  }

  public void addExcludedName(String paramString)
  {
    Object localObject = lookupQualifiedName(this.scope, paramString);
    if (!(localObject instanceof Scriptable))
      throw new IllegalArgumentException("Object for excluded name " + paramString + " not found.");
    this.table.put(localObject, paramString);
  }

  public boolean hasExcludedName(String paramString)
  {
    return (this.table.get(paramString) != null);
  }

  public void removeExcludedName(String paramString)
  {
    this.table.remove(paramString);
  }

  public void excludeStandardObjectNames()
  {
    String[] arrayOfString = { "Object", "Object.prototype", "Function", "Function.prototype", "String", "String.prototype", "Math", "Array", "Array.prototype", "Error", "Error.prototype", "Number", "Number.prototype", "Date", "Date.prototype", "RegExp", "RegExp.prototype", "Script", "Script.prototype", "Continuation", "Continuation.prototype", "XML", "XML.prototype", "XMLList", "XMLList.prototype" };
    for (int i = 0; i < arrayOfString.length; ++i)
      addExcludedName(arrayOfString[i]);
  }

  static Object lookupQualifiedName(Scriptable paramScriptable, String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ".");
    Object localObject = paramScriptable;
    while (localStringTokenizer.hasMoreTokens())
    {
      String str = localStringTokenizer.nextToken();
      localObject = ScriptableObject.getProperty((Scriptable)localObject, str);
      if (localObject == null)
        break;
      if (!(localObject instanceof Scriptable))
        break;
    }
    return localObject;
  }

  protected Object replaceObject(Object paramObject)
    throws IOException
  {
    String str = (String)this.table.get(paramObject);
    if (str == null)
      return paramObject;
    return new PendingLookup(str);
  }

  static class PendingLookup
  implements Serializable
  {
    static final long serialVersionUID = -2692990309789917727L;
    private String name;

    PendingLookup(String paramString)
    {
      this.name = paramString;
    }

    String getName()
    {
      return this.name;
    }
  }
}
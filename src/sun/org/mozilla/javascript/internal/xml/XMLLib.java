package sun.org.mozilla.javascript.internal.xml;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Ref;
import sun.org.mozilla.javascript.internal.ScriptRuntime;
import sun.org.mozilla.javascript.internal.Scriptable;
import sun.org.mozilla.javascript.internal.ScriptableObject;

public abstract class XMLLib
{
  private static final Object XML_LIB_KEY = new Object();

  public static XMLLib extractFromScopeOrNull(Scriptable paramScriptable)
  {
    ScriptableObject localScriptableObject = ScriptRuntime.getLibraryScopeOrNull(paramScriptable);
    if (localScriptableObject == null)
      return null;
    ScriptableObject.getProperty(localScriptableObject, "XML");
    return ((XMLLib)localScriptableObject.getAssociatedValue(XML_LIB_KEY));
  }

  public static XMLLib extractFromScope(Scriptable paramScriptable)
  {
    XMLLib localXMLLib = extractFromScopeOrNull(paramScriptable);
    if (localXMLLib != null)
      return localXMLLib;
    String str = ScriptRuntime.getMessage0("msg.XML.not.available");
    throw Context.reportRuntimeError(str);
  }

  protected final XMLLib bindToScope(Scriptable paramScriptable)
  {
    ScriptableObject localScriptableObject = ScriptRuntime.getLibraryScopeOrNull(paramScriptable);
    if (localScriptableObject == null)
      throw new IllegalStateException();
    return ((XMLLib)localScriptableObject.associateValue(XML_LIB_KEY, this));
  }

  public abstract boolean isXMLName(Context paramContext, Object paramObject);

  public abstract Ref nameRef(Context paramContext, Object paramObject, Scriptable paramScriptable, int paramInt);

  public abstract Ref nameRef(Context paramContext, Object paramObject1, Object paramObject2, Scriptable paramScriptable, int paramInt);

  public abstract String escapeAttributeValue(Object paramObject);

  public abstract String escapeTextValue(Object paramObject);

  public abstract Object toDefaultXmlNamespace(Context paramContext, Object paramObject);
}
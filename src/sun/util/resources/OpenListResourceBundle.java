package sun.util.resources;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import sun.util.ResourceBundleEnumeration;

public abstract class OpenListResourceBundle extends ResourceBundle
{
  private Map lookup = null;

  public Object handleGetObject(String paramString)
  {
    if (paramString == null)
      throw new NullPointerException();
    loadLookupTablesIfNecessary();
    return this.lookup.get(paramString);
  }

  public Enumeration<String> getKeys()
  {
    ResourceBundle localResourceBundle = this.parent;
    return new ResourceBundleEnumeration(handleGetKeys(), null);
  }

  public Set<String> handleGetKeys()
  {
    loadLookupTablesIfNecessary();
    return this.lookup.keySet();
  }

  public OpenListResourceBundle getParent()
  {
    return ((OpenListResourceBundle)this.parent);
  }

  protected abstract Object[][] getContents();

  void loadLookupTablesIfNecessary()
  {
    if (this.lookup == null)
      loadLookup();
  }

  private synchronized void loadLookup()
  {
    if (this.lookup != null)
      return;
    Object[][] arrayOfObject = getContents();
    Map localMap = createMap(arrayOfObject.length);
    for (int i = 0; i < arrayOfObject.length; ++i)
    {
      String str = (String)arrayOfObject[i][0];
      Object localObject = arrayOfObject[i][1];
      if ((str == null) || (localObject == null))
        throw new NullPointerException();
      localMap.put(str, localObject);
    }
    this.lookup = localMap;
  }

  protected Map createMap(int paramInt)
  {
    return new HashMap(paramInt);
  }
}
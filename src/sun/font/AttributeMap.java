package sun.font;

import java.awt.font.TextAttribute;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class AttributeMap extends AbstractMap<TextAttribute, Object>
{
  private AttributeValues values;
  private Map<TextAttribute, Object> delegateMap;
  private static boolean first = false;

  public AttributeMap(AttributeValues paramAttributeValues)
  {
    this.values = paramAttributeValues;
  }

  public Set<Map.Entry<TextAttribute, Object>> entrySet()
  {
    return delegate().entrySet();
  }

  public Object put(TextAttribute paramTextAttribute, Object paramObject)
  {
    return delegate().put(paramTextAttribute, paramObject);
  }

  public AttributeValues getValues()
  {
    return this.values;
  }

  private Map<TextAttribute, Object> delegate()
  {
    if (this.delegateMap == null)
    {
      if (first)
      {
        first = false;
        Thread.dumpStack();
      }
      this.delegateMap = this.values.toMap(new HashMap(27));
      this.values = null;
    }
    return this.delegateMap;
  }

  public String toString()
  {
    if (this.values != null)
      return "map of " + this.values.toString();
    return super.toString();
  }
}
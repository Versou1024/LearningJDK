package sun.security.tools;

import java.util.LinkedList;

class TaggedList extends java.awt.List
{
  private java.util.List data = new LinkedList();

  public TaggedList(int paramInt, boolean paramBoolean)
  {
    super(paramInt, paramBoolean);
  }

  public Object getObject(int paramInt)
  {
    return this.data.get(paramInt);
  }

  @Deprecated
  public void add(String paramString)
  {
    throw new AssertionError("should not call add in TaggedList");
  }

  public void addTaggedItem(String paramString, Object paramObject)
  {
    super.add(paramString);
    this.data.add(paramObject);
  }

  @Deprecated
  public void replaceItem(String paramString, int paramInt)
  {
    throw new AssertionError("should not call replaceItem in TaggedList");
  }

  public void replaceTaggedItem(String paramString, Object paramObject, int paramInt)
  {
    super.replaceItem(paramString, paramInt);
    this.data.set(paramInt, paramObject);
  }

  @Deprecated
  public void remove(int paramInt)
  {
    super.remove(paramInt);
  }

  public void removeTaggedItem(int paramInt)
  {
    super.remove(paramInt);
    this.data.remove(paramInt);
  }
}
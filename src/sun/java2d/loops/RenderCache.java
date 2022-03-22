package sun.java2d.loops;

public final class RenderCache
{
  private Entry[] entries;

  public RenderCache(int paramInt)
  {
    this.entries = new Entry[paramInt];
  }

  public synchronized Object get(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    int i = this.entries.length - 1;
    for (int j = i; j >= 0; --j)
    {
      Entry localEntry = this.entries[j];
      if (localEntry == null)
        break;
      if (localEntry.matches(paramSurfaceType1, paramCompositeType, paramSurfaceType2))
      {
        if (j < i - 4)
        {
          System.arraycopy(this.entries, j + 1, this.entries, j, i - j);
          this.entries[i] = localEntry;
        }
        return localEntry.getValue();
      }
    }
    return null;
  }

  public synchronized void put(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2, Object paramObject)
  {
    Entry localEntry = new Entry(this, paramSurfaceType1, paramCompositeType, paramSurfaceType2, paramObject);
    int i = this.entries.length;
    System.arraycopy(this.entries, 1, this.entries, 0, i - 1);
    this.entries[(i - 1)] = localEntry;
  }

  final class Entry
  {
    private SurfaceType src;
    private CompositeType comp;
    private SurfaceType dst;
    private Object value;

    public Entry(, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2, Object paramObject)
    {
      this.src = paramSurfaceType1;
      this.comp = paramCompositeType;
      this.dst = paramSurfaceType2;
      this.value = paramObject;
    }

    public boolean matches(, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
    {
      return ((this.src == paramSurfaceType1) && (this.comp == paramCompositeType) && (this.dst == paramSurfaceType2));
    }

    public Object getValue()
    {
      return this.value;
    }
  }
}
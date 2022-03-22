package sun.misc;

import java.lang.ref.SoftReference;

@Deprecated
public abstract class Ref
{
  private SoftReference soft = null;

  public synchronized Object get()
  {
    Object localObject = check();
    if (localObject == null)
    {
      localObject = reconstitute();
      setThing(localObject);
    }
    return localObject;
  }

  public abstract Object reconstitute();

  public synchronized void flush()
  {
    SoftReference localSoftReference = this.soft;
    if (localSoftReference != null)
      localSoftReference.clear();
    this.soft = null;
  }

  public synchronized void setThing(Object paramObject)
  {
    flush();
    this.soft = new SoftReference(paramObject);
  }

  public synchronized Object check()
  {
    SoftReference localSoftReference = this.soft;
    if (localSoftReference == null)
      return null;
    return localSoftReference.get();
  }

  public Ref()
  {
  }

  public Ref(Object paramObject)
  {
    setThing(paramObject);
  }
}
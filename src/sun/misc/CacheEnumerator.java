package sun.misc;

import java.util.Enumeration;
import java.util.NoSuchElementException;

class CacheEnumerator
  implements Enumeration
{
  boolean keys;
  int index;
  CacheEntry[] table;
  CacheEntry entry;

  CacheEnumerator(CacheEntry[] paramArrayOfCacheEntry, boolean paramBoolean)
  {
    this.table = paramArrayOfCacheEntry;
    this.keys = paramBoolean;
    this.index = paramArrayOfCacheEntry.length;
  }

  public boolean hasMoreElements()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 45	sun/misc/CacheEnumerator:index	I
    //   4: iflt +70 -> 74
    //   7: aload_0
    //   8: getfield 47	sun/misc/CacheEnumerator:entry	Lsun/misc/CacheEntry;
    //   11: ifnull +29 -> 40
    //   14: aload_0
    //   15: getfield 47	sun/misc/CacheEnumerator:entry	Lsun/misc/CacheEntry;
    //   18: invokevirtual 51	sun/misc/CacheEntry:check	()Ljava/lang/Object;
    //   21: ifnull +5 -> 26
    //   24: iconst_1
    //   25: ireturn
    //   26: aload_0
    //   27: aload_0
    //   28: getfield 47	sun/misc/CacheEnumerator:entry	Lsun/misc/CacheEntry;
    //   31: getfield 44	sun/misc/CacheEntry:next	Lsun/misc/CacheEntry;
    //   34: putfield 47	sun/misc/CacheEnumerator:entry	Lsun/misc/CacheEntry;
    //   37: goto -30 -> 7
    //   40: aload_0
    //   41: dup
    //   42: getfield 45	sun/misc/CacheEnumerator:index	I
    //   45: iconst_1
    //   46: isub
    //   47: dup_x1
    //   48: putfield 45	sun/misc/CacheEnumerator:index	I
    //   51: iflt -51 -> 0
    //   54: aload_0
    //   55: aload_0
    //   56: getfield 48	sun/misc/CacheEnumerator:table	[Lsun/misc/CacheEntry;
    //   59: aload_0
    //   60: getfield 45	sun/misc/CacheEnumerator:index	I
    //   63: aaload
    //   64: dup_x1
    //   65: putfield 47	sun/misc/CacheEnumerator:entry	Lsun/misc/CacheEntry;
    //   68: ifnonnull -68 -> 0
    //   71: goto -31 -> 40
    //   74: iconst_0
    //   75: ireturn
  }

  public Object nextElement()
  {
    while (true)
    {
      do
      {
        if (this.index < 0)
          break label97;
        if (this.entry == null)
          while (--this.index >= 0)
            if ((this.entry = this.table[this.index]) != null)
              break;
      }
      while (this.entry == null);
      CacheEntry localCacheEntry = this.entry;
      this.entry = localCacheEntry.next;
      if (localCacheEntry.check() != null)
        return ((this.keys) ? localCacheEntry.key : localCacheEntry.check());
    }
    label97: throw new NoSuchElementException("CacheEnumerator");
  }
}
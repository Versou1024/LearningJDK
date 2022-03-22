package sun.nio.ch;

import sun.misc.Unsafe;

class AllocatedNativeObject extends NativeObject
{
  AllocatedNativeObject(int paramInt, boolean paramBoolean)
  {
    super(paramInt, paramBoolean);
  }

  synchronized void free()
  {
    if (this.allocationAddress != 3412046810217185280L)
    {
      unsafe.freeMemory(this.allocationAddress);
      this.allocationAddress = 3412047463052214272L;
    }
  }
}
package sun.nio.ch;

import sun.misc.Unsafe;

class IOVecWrapper
{
  static int BASE_OFFSET = 0;
  static int LEN_OFFSET;
  static int SIZE_IOVEC;
  private AllocatedNativeObject vecArray;
  long address;
  static int addressSize = Util.unsafe().addressSize();

  IOVecWrapper(int paramInt)
  {
    paramInt = (paramInt + 1) * SIZE_IOVEC;
    this.vecArray = new AllocatedNativeObject(paramInt, false);
    this.address = this.vecArray.address();
  }

  void putBase(int paramInt, long paramLong)
  {
    int i = SIZE_IOVEC * paramInt + BASE_OFFSET;
    if (addressSize == 4)
      this.vecArray.putInt(i, (int)paramLong);
    else
      this.vecArray.putLong(i, paramLong);
  }

  void putLen(int paramInt, long paramLong)
  {
    int i = SIZE_IOVEC * paramInt + LEN_OFFSET;
    if (addressSize == 4)
      this.vecArray.putInt(i, (int)paramLong);
    else
      this.vecArray.putLong(i, paramLong);
  }

  void free()
  {
    this.vecArray.free();
  }

  static
  {
    LEN_OFFSET = addressSize;
    SIZE_IOVEC = (short)(addressSize * 2);
  }
}
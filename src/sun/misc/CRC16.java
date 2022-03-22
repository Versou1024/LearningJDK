package sun.misc;

public class CRC16
{
  public int value = 0;

  public void update(byte paramByte)
  {
    int i = paramByte;
    for (int k = 7; k >= 0; --k)
    {
      i <<= 1;
      int j = i >>> 8 & 0x1;
      if ((this.value & 0x8000) != 0)
        this.value = ((this.value << 1) + j ^ 0x1021);
      else
        this.value = ((this.value << 1) + j);
    }
    this.value &= 65535;
  }

  public void reset()
  {
    this.value = 0;
  }
}
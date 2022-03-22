package sun.management.counter;

public abstract interface ByteArrayCounter extends Counter
{
  public abstract byte[] byteArrayValue();

  public abstract byte byteAt(int paramInt);
}
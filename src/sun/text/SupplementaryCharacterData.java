package sun.text;

public final class SupplementaryCharacterData
  implements Cloneable
{
  private int[] dataTable;

  public SupplementaryCharacterData(int[] paramArrayOfInt)
  {
    this.dataTable = paramArrayOfInt;
  }

  public int getValue(int paramInt)
  {
    if ((!($assertionsDisabled)) && (((paramInt < 65536) || (paramInt > 1114111))))
      throw new AssertionError("Invalid code point:" + Integer.toHexString(paramInt));
    int i = 0;
    int j = this.dataTable.length - 1;
    while (true)
    {
      int k = (i + j) / 2;
      int l = this.dataTable[k] >> 8;
      int i1 = this.dataTable[(k + 1)] >> 8;
      if (paramInt < l)
        j = k;
      else if (paramInt > i1 - 1)
        i = k;
      else
        return (this.dataTable[k] & 0xFF);
    }
  }

  public int[] getArray()
  {
    return this.dataTable;
  }
}
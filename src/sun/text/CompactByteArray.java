package sun.text;

import B;
import I;
import S;

public final class CompactByteArray
  implements Cloneable
{
  public static final int UNICODECOUNT = 65536;
  private static final int BLOCKSHIFT = 7;
  private static final int BLOCKCOUNT = 128;
  private static final int INDEXSHIFT = 9;
  private static final int INDEXCOUNT = 512;
  private static final int BLOCKMASK = 127;
  private byte[] values;
  private short[] indices;
  private boolean isCompact;
  private int[] hashes;

  public CompactByteArray(byte paramByte)
  {
    this.values = new byte[65536];
    this.indices = new short[512];
    this.hashes = new int[512];
    for (int i = 0; i < 65536; ++i)
      this.values[i] = paramByte;
    for (i = 0; i < 512; ++i)
    {
      this.indices[i] = (short)(i << 7);
      this.hashes[i] = 0;
    }
    this.isCompact = false;
  }

  public CompactByteArray(short[] paramArrayOfShort, byte[] paramArrayOfByte)
  {
    if (paramArrayOfShort.length != 512)
      throw new IllegalArgumentException("Index out of bounds!");
    for (int i = 0; i < 512; ++i)
    {
      int j = paramArrayOfShort[i];
      if ((j < 0) || (j >= paramArrayOfByte.length + 128))
        throw new IllegalArgumentException("Index out of bounds!");
    }
    this.indices = paramArrayOfShort;
    this.values = paramArrayOfByte;
    this.isCompact = true;
  }

  public byte elementAt(char paramChar)
  {
    return this.values[((this.indices[(paramChar >> '\7')] & 0xFFFF) + (paramChar & 0x7F))];
  }

  public void setElementAt(char paramChar, byte paramByte)
  {
    if (this.isCompact)
      expand();
    this.values[paramChar] = paramByte;
    touchBlock(paramChar >> '\7', paramByte);
  }

  public void setElementAt(char paramChar1, char paramChar2, byte paramByte)
  {
    if (this.isCompact)
      expand();
    for (char c = paramChar1; c <= paramChar2; ++c)
    {
      this.values[c] = paramByte;
      touchBlock(c >> '\7', paramByte);
    }
  }

  public void compact()
  {
    if (!(this.isCompact))
    {
      int i = 0;
      int j = 0;
      int k = -1;
      int l = 0;
      while (l < this.indices.length)
      {
        this.indices[l] = -1;
        boolean bool = blockTouched(l);
        if ((!(bool)) && (k != -1))
        {
          this.indices[l] = k;
        }
        else
        {
          int i1 = 0;
          int i2 = 0;
          i2 = 0;
          while (i2 < i)
          {
            if ((this.hashes[l] == this.hashes[i2]) && (arrayRegionMatches(this.values, j, this.values, i1, 128)))
            {
              this.indices[l] = (short)i1;
              break;
            }
            ++i2;
            i1 += 128;
          }
          if (this.indices[l] == -1)
          {
            System.arraycopy(this.values, j, this.values, i1, 128);
            this.indices[l] = (short)i1;
            this.hashes[i2] = this.hashes[l];
            ++i;
            if (!(bool))
              k = (short)i1;
          }
        }
        ++l;
        j += 128;
      }
      l = i * 128;
      byte[] arrayOfByte = new byte[l];
      System.arraycopy(this.values, 0, arrayOfByte, 0, l);
      this.values = arrayOfByte;
      this.isCompact = true;
      this.hashes = null;
    }
  }

  static final boolean arrayRegionMatches(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2, int paramInt3)
  {
    int i = paramInt1 + paramInt3;
    int j = paramInt2 - paramInt1;
    for (int k = paramInt1; k < i; ++k)
      if (paramArrayOfByte1[k] != paramArrayOfByte2[(k + j)])
        return false;
    return true;
  }

  private final void touchBlock(int paramInt1, int paramInt2)
  {
    this.hashes[paramInt1] = (this.hashes[paramInt1] + (paramInt2 << 1) | 0x1);
  }

  private final boolean blockTouched(int paramInt)
  {
    return (this.hashes[paramInt] != 0);
  }

  public short[] getIndexArray()
  {
    return this.indices;
  }

  public byte[] getStringArray()
  {
    return this.values;
  }

  public Object clone()
  {
    CompactByteArray localCompactByteArray;
    try
    {
      localCompactByteArray = (CompactByteArray)super.clone();
      localCompactByteArray.values = ((byte[])(byte[])this.values.clone());
      localCompactByteArray.indices = ((short[])(short[])this.indices.clone());
      if (this.hashes != null)
        localCompactByteArray.hashes = ((int[])(int[])this.hashes.clone());
      return localCompactByteArray;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      throw new InternalError();
    }
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject == null)
      return false;
    if (this == paramObject)
      return true;
    if (super.getClass() != paramObject.getClass())
      return false;
    CompactByteArray localCompactByteArray = (CompactByteArray)paramObject;
    for (int i = 0; i < 65536; ++i)
      if (elementAt((char)i) != localCompactByteArray.elementAt((char)i))
        return false;
    return true;
  }

  public int hashCode()
  {
    int i = 0;
    int j = Math.min(3, this.values.length / 16);
    int k = 0;
    while (k < this.values.length)
    {
      i = i * 37 + this.values[k];
      k += j;
    }
    return i;
  }

  private void expand()
  {
    if (this.isCompact)
    {
      this.hashes = new int[512];
      byte[] arrayOfByte = new byte[65536];
      for (int i = 0; i < 65536; ++i)
      {
        int j = elementAt((char)i);
        arrayOfByte[i] = j;
        touchBlock(i >> 7, j);
      }
      for (i = 0; i < 512; ++i)
        this.indices[i] = (short)(i << 7);
      this.values = null;
      this.values = arrayOfByte;
      this.isCompact = false;
    }
  }

  private byte[] getArray()
  {
    return this.values;
  }
}
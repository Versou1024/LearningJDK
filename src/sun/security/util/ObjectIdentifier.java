package sun.security.util;

import I;
import java.io.IOException;
import java.io.Serializable;

public final class ObjectIdentifier
  implements Serializable
{
  private static final long serialVersionUID = 8697030238860181294L;
  private static final int maxFirstComponent = 2;
  private static final int maxSecondComponent = 39;
  private int[] components;
  private int componentLen;
  private volatile transient String stringForm;
  private static final int allocationQuantum = 5;

  public ObjectIdentifier(String paramString)
    throws IOException
  {
    int i = 46;
    int j = 0;
    int k = 0;
    this.componentLen = 0;
    while ((k = paramString.indexOf(i, j)) != -1)
    {
      j = k + 1;
      this.componentLen += 1;
    }
    this.componentLen += 1;
    this.components = new int[this.componentLen];
    j = 0;
    int l = 0;
    String str = null;
    try
    {
      while ((k = paramString.indexOf(i, j)) != -1)
      {
        str = paramString.substring(j, k);
        this.components[(l++)] = Integer.valueOf(str).intValue();
        j = k + 1;
      }
      str = paramString.substring(j);
      this.components[l] = Integer.valueOf(str).intValue();
    }
    catch (Exception localException)
    {
      throw new IOException("ObjectIdentifier() -- Invalid format: " + localException.toString(), localException);
    }
    checkValidOid(this.components, this.componentLen);
    this.stringForm = paramString;
  }

  private void checkValidOid(int[] paramArrayOfInt, int paramInt)
    throws IOException
  {
    if ((paramArrayOfInt == null) || (paramInt < 2))
      throw new IOException("ObjectIdentifier() -- Must be at least two oid components ");
    for (int i = 0; i < paramInt; ++i)
      if (paramArrayOfInt[i] < 0)
        throw new IOException("ObjectIdentifier() -- oid component #" + (i + 1) + " must be non-negative ");
    if (paramArrayOfInt[0] > 2)
      throw new IOException("ObjectIdentifier() -- First oid component is invalid ");
    if ((paramArrayOfInt[0] < 2) && (paramArrayOfInt[1] > 39))
      throw new IOException("ObjectIdentifier() -- Second oid component is invalid ");
  }

  public ObjectIdentifier(int[] paramArrayOfInt)
    throws IOException
  {
    checkValidOid(paramArrayOfInt, paramArrayOfInt.length);
    this.components = ((int[])(int[])paramArrayOfInt.clone());
    this.componentLen = paramArrayOfInt.length;
  }

  public ObjectIdentifier(DerInputStream paramDerInputStream)
    throws IOException
  {
    int i = (byte)paramDerInputStream.getByte();
    if (i != 6)
      throw new IOException("ObjectIdentifier() -- data isn't an object ID (tag = " + i + ")");
    int j = paramDerInputStream.available() - paramDerInputStream.getLength() - 1;
    if (j < 0)
      throw new IOException("ObjectIdentifier() -- not enough data");
    initFromEncoding(paramDerInputStream, j);
  }

  ObjectIdentifier(DerInputBuffer paramDerInputBuffer)
    throws IOException
  {
    initFromEncoding(new DerInputStream(paramDerInputBuffer), 0);
  }

  private ObjectIdentifier(int[] paramArrayOfInt, boolean paramBoolean)
  {
    this.components = paramArrayOfInt;
    this.componentLen = paramArrayOfInt.length;
  }

  public static ObjectIdentifier newInternal(int[] paramArrayOfInt)
  {
    return new ObjectIdentifier(paramArrayOfInt, true);
  }

  private void initFromEncoding(DerInputStream paramDerInputStream, int paramInt)
    throws IOException
  {
    int j = 1;
    this.components = new int[5];
    this.componentLen = 0;
    while (true)
    {
      int i;
      while (true)
      {
        int k;
        if (paramDerInputStream.available() <= paramInt)
          break label175;
        i = getComponent(paramDerInputStream);
        if (i < 0)
          throw new IOException("ObjectIdentifier() -- component values must be nonnegative");
        if (j == 0)
          break;
        if (i < 40)
          k = 0;
        else if (i < 80)
          k = 1;
        else
          k = 2;
        int l = i - k * 40;
        this.components[0] = k;
        this.components[1] = l;
        this.componentLen = 2;
        j = 0;
      }
      if (this.componentLen >= this.components.length)
      {
        int[] arrayOfInt = new int[this.components.length + 5];
        System.arraycopy(this.components, 0, arrayOfInt, 0, this.components.length);
        this.components = arrayOfInt;
      }
      this.components[(this.componentLen++)] = i;
    }
    label175: checkValidOid(this.components, this.componentLen);
    if (paramDerInputStream.available() != paramInt)
      throw new IOException("ObjectIdentifier() -- malformed input data");
  }

  void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.components[0] < 2)
      localDerOutputStream.write(this.components[0] * 40 + this.components[1]);
    else
      putComponent(localDerOutputStream, this.components[0] * 40 + this.components[1]);
    for (int i = 2; i < this.componentLen; ++i)
      putComponent(localDerOutputStream, this.components[i]);
    paramDerOutputStream.write(6, localDerOutputStream);
  }

  private static int getComponent(DerInputStream paramDerInputStream)
    throws IOException
  {
    int j = 0;
    int i = 0;
    while (j < 4)
    {
      i <<= 7;
      int k = paramDerInputStream.getByte();
      i |= k & 0x7F;
      if ((k & 0x80) == 0)
        return i;
      ++j;
    }
    throw new IOException("ObjectIdentifier() -- component value too big");
  }

  private static void putComponent(DerOutputStream paramDerOutputStream, int paramInt)
    throws IOException
  {
    byte[] arrayOfByte = new byte[4];
    for (int i = 0; i < 4; ++i)
    {
      arrayOfByte[i] = (byte)(paramInt & 0x7F);
      paramInt >>>= 7;
      if (paramInt == 0)
        break;
    }
    while (i > 0)
    {
      paramDerOutputStream.write(arrayOfByte[i] | 0x80);
      --i;
    }
    paramDerOutputStream.write(arrayOfByte[0]);
  }

  public boolean precedes(ObjectIdentifier paramObjectIdentifier)
  {
    if ((paramObjectIdentifier == this) || (this.componentLen < paramObjectIdentifier.componentLen))
      return false;
    if (paramObjectIdentifier.componentLen < this.componentLen)
      return true;
    for (int i = 0; i < this.componentLen; ++i)
      if (paramObjectIdentifier.components[i] < this.components[i])
        return true;
    return false;
  }

  @Deprecated
  public boolean equals(ObjectIdentifier paramObjectIdentifier)
  {
    return equals(paramObjectIdentifier);
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof ObjectIdentifier))
      return false;
    ObjectIdentifier localObjectIdentifier = (ObjectIdentifier)paramObject;
    if (this.componentLen != localObjectIdentifier.componentLen)
      return false;
    for (int i = 0; i < this.componentLen; ++i)
      if (this.components[i] != localObjectIdentifier.components[i])
        return false;
    return true;
  }

  public int hashCode()
  {
    int i = this.componentLen;
    for (int j = 0; j < this.componentLen; ++j)
      i += this.components[j] * 37;
    return i;
  }

  public String toString()
  {
    String str = this.stringForm;
    if (str == null)
    {
      StringBuffer localStringBuffer = new StringBuffer(this.componentLen * 4);
      for (int i = 0; i < this.componentLen; ++i)
      {
        if (i != 0)
          localStringBuffer.append('.');
        localStringBuffer.append(this.components[i]);
      }
      str = localStringBuffer.toString();
      this.stringForm = str;
    }
    return str;
  }
}
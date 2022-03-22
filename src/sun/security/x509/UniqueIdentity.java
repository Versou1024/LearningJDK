package sun.security.x509;

import java.io.IOException;
import sun.security.util.BitArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class UniqueIdentity
{
  private BitArray id;

  public UniqueIdentity(BitArray paramBitArray)
  {
    this.id = paramBitArray;
  }

  public UniqueIdentity(byte[] paramArrayOfByte)
  {
    this.id = new BitArray(paramArrayOfByte.length * 8, paramArrayOfByte);
  }

  public UniqueIdentity(DerInputStream paramDerInputStream)
    throws IOException
  {
    DerValue localDerValue = paramDerInputStream.getDerValue();
    this.id = localDerValue.getUnalignedBitString(true);
  }

  public UniqueIdentity(DerValue paramDerValue)
    throws IOException
  {
    this.id = paramDerValue.getUnalignedBitString(true);
  }

  public String toString()
  {
    return "UniqueIdentity:" + this.id.toString() + "\n";
  }

  public void encode(DerOutputStream paramDerOutputStream, byte paramByte)
    throws IOException
  {
    byte[] arrayOfByte = this.id.toByteArray();
    int i = arrayOfByte.length * 8 - this.id.length();
    paramDerOutputStream.write(paramByte);
    paramDerOutputStream.putLength(arrayOfByte.length + 1);
    paramDerOutputStream.write(i);
    paramDerOutputStream.write(arrayOfByte);
  }

  public boolean[] getId()
  {
    if (this.id == null)
      return null;
    return this.id.toBooleanArray();
  }
}
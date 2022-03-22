package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class X400Address
  implements GeneralNameInterface
{
  byte[] nameValue = null;

  public X400Address(byte[] paramArrayOfByte)
  {
    this.nameValue = paramArrayOfByte;
  }

  public X400Address(DerValue paramDerValue)
    throws IOException
  {
    this.nameValue = paramDerValue.toByteArray();
  }

  public int getType()
  {
    return 3;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerValue localDerValue = new DerValue(this.nameValue);
    paramDerOutputStream.putDerValue(localDerValue);
  }

  public String toString()
  {
    return "X400Address: <DER-encoded value>";
  }

  public int constrains(GeneralNameInterface paramGeneralNameInterface)
    throws UnsupportedOperationException
  {
    int i;
    if (paramGeneralNameInterface == null)
      i = -1;
    else if (paramGeneralNameInterface.getType() != 3)
      i = -1;
    else
      throw new UnsupportedOperationException("Narrowing, widening, and match are not supported for X400Address.");
    return i;
  }

  public int subtreeDepth()
    throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("subtreeDepth not supported for X400Address");
  }
}
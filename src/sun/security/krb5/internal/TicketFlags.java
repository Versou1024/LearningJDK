package sun.security.krb5.internal;

import java.io.IOException;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.internal.util.KrbBitArray;
import sun.security.util.BitArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class TicketFlags extends KrbBitArray
{
  public TicketFlags()
  {
    super(32);
  }

  public TicketFlags(boolean[] paramArrayOfBoolean)
    throws Asn1Exception
  {
    super(paramArrayOfBoolean);
    if (paramArrayOfBoolean.length > 32)
      throw new Asn1Exception(502);
  }

  public TicketFlags(int paramInt, byte[] paramArrayOfByte)
    throws Asn1Exception
  {
    super(paramInt, paramArrayOfByte);
    if ((paramInt > paramArrayOfByte.length * 8) || (paramInt > 32))
      throw new Asn1Exception(502);
  }

  public TicketFlags(DerValue paramDerValue)
    throws IOException, Asn1Exception
  {
    this(paramDerValue.getUnalignedBitString(true).toBooleanArray());
  }

  public static TicketFlags parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new TicketFlags(localDerValue2);
  }

  public byte[] asn1Encode()
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putUnalignedBitString(new BitArray(toBooleanArray()));
    return localDerOutputStream.toByteArray();
  }

  public Object clone()
  {
    try
    {
      return new TicketFlags(toBooleanArray());
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public boolean match(LoginOptions paramLoginOptions)
  {
    int i = 0;
    if ((get(1) == paramLoginOptions.get(1)) && (get(3) == paramLoginOptions.get(3)) && (get(8) == paramLoginOptions.get(8)))
      i = 1;
    return i;
  }

  public boolean match(TicketFlags paramTicketFlags)
  {
    int i = 1;
    for (int j = 0; j < 12; ++j)
      if (get(j) != paramTicketFlags.get(j))
        return false;
    return i;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    boolean[] arrayOfBoolean = toBooleanArray();
    for (int i = 0; i < arrayOfBoolean.length; ++i)
      if (arrayOfBoolean[i] == 1)
        switch (i)
        {
        case 0:
          localStringBuffer.append("RESERVED;");
          break;
        case 1:
          localStringBuffer.append("FORWARDABLE;");
          break;
        case 2:
          localStringBuffer.append("FORWARDED;");
          break;
        case 3:
          localStringBuffer.append("PROXIABLE;");
          break;
        case 4:
          localStringBuffer.append("PROXY;");
          break;
        case 5:
          localStringBuffer.append("MAY-POSTDATE;");
          break;
        case 6:
          localStringBuffer.append("POSTDATED;");
          break;
        case 7:
          localStringBuffer.append("INVALID;");
          break;
        case 8:
          localStringBuffer.append("RENEWABLE;");
          break;
        case 9:
          localStringBuffer.append("INITIAL;");
          break;
        case 10:
          localStringBuffer.append("PRE-AUTHENT;");
          break;
        case 11:
          localStringBuffer.append("HW-AUTHENT;");
        }
    String str = localStringBuffer.toString();
    if (str.length() > 0)
      str = str.substring(0, str.length() - 1);
    return str;
  }
}
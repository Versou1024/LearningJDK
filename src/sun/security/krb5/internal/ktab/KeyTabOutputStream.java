package sun.security.krb5.internal.ktab;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.util.KrbDataOutputStream;

public class KeyTabOutputStream extends KrbDataOutputStream
  implements KeyTabConstants
{
  private KeyTabEntry entry;
  private int keyType;
  private byte[] keyValue;
  public int version;

  public KeyTabOutputStream(OutputStream paramOutputStream)
  {
    super(paramOutputStream);
  }

  public void writeVersion(int paramInt)
    throws IOException
  {
    this.version = paramInt;
    write16(paramInt);
  }

  public void writeEntry(KeyTabEntry paramKeyTabEntry)
    throws IOException
  {
    write32(paramKeyTabEntry.entryLength());
    String[] arrayOfString = paramKeyTabEntry.service.getNameStrings();
    int i = arrayOfString.length;
    if (this.version == 1281)
      write16(i + 1);
    else
      write16(i);
    byte[] arrayOfByte = null;
    try
    {
      arrayOfByte = paramKeyTabEntry.service.getRealmString().getBytes("8859_1");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException1)
    {
    }
    write16(arrayOfByte.length);
    write(arrayOfByte);
    for (int j = 0; j < i; ++j)
      try
      {
        write16(arrayOfString[j].getBytes("8859_1").length);
        write(arrayOfString[j].getBytes("8859_1"));
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException2)
      {
      }
    write32(paramKeyTabEntry.service.getNameType());
    write32((int)(paramKeyTabEntry.timestamp.getTime() / 1000L));
    write8(paramKeyTabEntry.keyVersion % 256);
    write16(paramKeyTabEntry.keyType);
    write16(paramKeyTabEntry.keyblock.length);
    write(paramKeyTabEntry.keyblock);
  }
}
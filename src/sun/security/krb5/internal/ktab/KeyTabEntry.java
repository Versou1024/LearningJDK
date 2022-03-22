package sun.security.krb5.internal.ktab;

import B;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;

public class KeyTabEntry
  implements KeyTabConstants
{
  PrincipalName service;
  Realm realm;
  KerberosTime timestamp;
  int keyVersion;
  int keyType;
  byte[] keyblock = null;
  boolean DEBUG = Krb5.DEBUG;

  public KeyTabEntry(PrincipalName paramPrincipalName, Realm paramRealm, KerberosTime paramKerberosTime, int paramInt1, int paramInt2, byte[] paramArrayOfByte)
  {
    this.service = paramPrincipalName;
    this.realm = paramRealm;
    this.timestamp = paramKerberosTime;
    this.keyVersion = paramInt1;
    this.keyType = paramInt2;
    if (paramArrayOfByte != null)
      this.keyblock = ((byte[])(byte[])paramArrayOfByte.clone());
  }

  public PrincipalName getService()
  {
    return this.service;
  }

  public EncryptionKey getKey()
  {
    EncryptionKey localEncryptionKey = new EncryptionKey(this.keyblock, this.keyType, new Integer(this.keyVersion));
    return localEncryptionKey;
  }

  public String getKeyString()
  {
    StringBuffer localStringBuffer = new StringBuffer("0x");
    for (int i = 0; i < this.keyblock.length; ++i)
      localStringBuffer.append(Integer.toHexString(this.keyblock[i] & 0xFF));
    return localStringBuffer.toString();
  }

  public int entryLength()
  {
    int i = 0;
    String[] arrayOfString = this.service.getNameStrings();
    for (int j = 0; j < arrayOfString.length; ++j)
      try
      {
        i += 2 + arrayOfString[j].getBytes("8859_1").length;
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException1)
      {
      }
    j = 0;
    try
    {
      j = this.realm.toString().getBytes("8859_1").length;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException2)
    {
    }
    int k = 4 + j + i + 4 + 4 + 1 + 2 + 2 + this.keyblock.length;
    if (this.DEBUG)
      System.out.println(">>> KeyTabEntry: key tab entry size is " + k);
    return k;
  }

  public KerberosTime getTimeStamp()
  {
    return this.timestamp;
  }
}
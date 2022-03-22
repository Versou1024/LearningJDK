package sun.security.pkcs;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class ContentInfo
{
  private static int[] pkcs7 = { 1, 2, 840, 113549, 1, 7 };
  private static int[] data = { 1, 2, 840, 113549, 1, 7, 1 };
  private static int[] sdata = { 1, 2, 840, 113549, 1, 7, 2 };
  private static int[] edata = { 1, 2, 840, 113549, 1, 7, 3 };
  private static int[] sedata = { 1, 2, 840, 113549, 1, 7, 4 };
  private static int[] ddata = { 1, 2, 840, 113549, 1, 7, 5 };
  private static int[] crdata = { 1, 2, 840, 113549, 1, 7, 6 };
  private static int[] nsdata = { 2, 16, 840, 1, 113730, 2, 5 };
  private static int[] tstInfo = { 1, 2, 840, 113549, 1, 9, 16, 1, 4 };
  private static final int[] OLD_SDATA = { 1, 2, 840, 1113549, 1, 7, 2 };
  private static final int[] OLD_DATA = { 1, 2, 840, 1113549, 1, 7, 1 };
  public static ObjectIdentifier PKCS7_OID = ObjectIdentifier.newInternal(pkcs7);
  public static ObjectIdentifier DATA_OID = ObjectIdentifier.newInternal(data);
  public static ObjectIdentifier SIGNED_DATA_OID = ObjectIdentifier.newInternal(sdata);
  public static ObjectIdentifier ENVELOPED_DATA_OID = ObjectIdentifier.newInternal(edata);
  public static ObjectIdentifier SIGNED_AND_ENVELOPED_DATA_OID = ObjectIdentifier.newInternal(sedata);
  public static ObjectIdentifier DIGESTED_DATA_OID = ObjectIdentifier.newInternal(ddata);
  public static ObjectIdentifier ENCRYPTED_DATA_OID = ObjectIdentifier.newInternal(crdata);
  public static ObjectIdentifier OLD_SIGNED_DATA_OID = ObjectIdentifier.newInternal(OLD_SDATA);
  public static ObjectIdentifier OLD_DATA_OID = ObjectIdentifier.newInternal(OLD_DATA);
  public static ObjectIdentifier NETSCAPE_CERT_SEQUENCE_OID = ObjectIdentifier.newInternal(nsdata);
  public static ObjectIdentifier TIMESTAMP_TOKEN_INFO_OID = ObjectIdentifier.newInternal(tstInfo);
  ObjectIdentifier contentType;
  DerValue content;

  public ContentInfo(ObjectIdentifier paramObjectIdentifier, DerValue paramDerValue)
  {
    this.contentType = paramObjectIdentifier;
    this.content = paramDerValue;
  }

  public ContentInfo(byte[] paramArrayOfByte)
  {
    DerValue localDerValue = new DerValue(4, paramArrayOfByte);
    this.contentType = DATA_OID;
    this.content = localDerValue;
  }

  public ContentInfo(DerInputStream paramDerInputStream)
    throws IOException, sun.security.pkcs.ParsingException
  {
    this(paramDerInputStream, false);
  }

  public ContentInfo(DerInputStream paramDerInputStream, boolean paramBoolean)
    throws IOException, sun.security.pkcs.ParsingException
  {
    DerValue[] arrayOfDerValue1 = paramDerInputStream.getSequence(2);
    DerValue localDerValue1 = arrayOfDerValue1[0];
    DerInputStream localDerInputStream1 = new DerInputStream(localDerValue1.toByteArray());
    this.contentType = localDerInputStream1.getOID();
    if (paramBoolean)
    {
      this.content = arrayOfDerValue1[1];
    }
    else if (arrayOfDerValue1.length > 1)
    {
      DerValue localDerValue2 = arrayOfDerValue1[1];
      DerInputStream localDerInputStream2 = new DerInputStream(localDerValue2.toByteArray());
      DerValue[] arrayOfDerValue2 = localDerInputStream2.getSet(1, true);
      this.content = arrayOfDerValue2[0];
    }
  }

  public DerValue getContent()
  {
    return this.content;
  }

  public ObjectIdentifier getContentType()
  {
    return this.contentType;
  }

  public byte[] getData()
    throws IOException
  {
    if ((this.contentType.equals(DATA_OID)) || (this.contentType.equals(OLD_DATA_OID)) || (this.contentType.equals(TIMESTAMP_TOKEN_INFO_OID)))
    {
      if (this.content == null)
        return null;
      return this.content.getOctetString();
    }
    throw new IOException("content type is not DATA: " + this.contentType);
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putOID(this.contentType);
    if (this.content != null)
    {
      DerValue localDerValue = null;
      DerOutputStream localDerOutputStream1 = new DerOutputStream();
      this.content.encode(localDerOutputStream1);
      localDerValue = new DerValue(-96, localDerOutputStream1.toByteArray());
      localDerOutputStream2.putDerValue(localDerValue);
    }
    paramDerOutputStream.write(48, localDerOutputStream2);
  }

  public byte[] getContentBytes()
    throws IOException
  {
    if (this.content == null)
      return null;
    DerInputStream localDerInputStream = new DerInputStream(this.content.toByteArray());
    return localDerInputStream.getOctetString();
  }

  public String toString()
  {
    String str = "";
    str = str + "Content Info Sequence\n\tContent type: " + this.contentType + "\n";
    str = str + "\tContent: " + this.content;
    return str;
  }
}
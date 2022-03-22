package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class NetscapeCertTypeExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.NetscapeCertType";
  public static final String NAME = "NetscapeCertType";
  public static final String SSL_CLIENT = "ssl_client";
  public static final String SSL_SERVER = "ssl_server";
  public static final String S_MIME = "s_mime";
  public static final String OBJECT_SIGNING = "object_signing";
  public static final String SSL_CA = "ssl_ca";
  public static final String S_MIME_CA = "s_mime_ca";
  public static final String OBJECT_SIGNING_CA = "object_signing_ca";
  private static final int[] CertType_data = { 2, 16, 840, 1, 113730, 1, 1 };
  public static ObjectIdentifier NetscapeCertType_Id;
  private boolean[] bitString;
  private static MapEntry[] mMapData;
  private static final Vector<String> mAttributeNames;

  private static int getPosition(String paramString)
    throws IOException
  {
    for (int i = 0; i < mMapData.length; ++i)
      if (paramString.equalsIgnoreCase(mMapData[i].mName))
        return mMapData[i].mPosition;
    throw new IOException("Attribute name [" + paramString + "] not recognized by CertAttrSet:NetscapeCertType.");
  }

  private void encodeThis()
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putUnalignedBitString(new BitArray(this.bitString));
    this.extensionValue = localDerOutputStream.toByteArray();
  }

  private boolean isSet(int paramInt)
  {
    return this.bitString[paramInt];
  }

  private void set(int paramInt, boolean paramBoolean)
  {
    if (paramInt >= this.bitString.length)
    {
      boolean[] arrayOfBoolean = new boolean[paramInt + 1];
      System.arraycopy(this.bitString, 0, arrayOfBoolean, 0, this.bitString.length);
      this.bitString = arrayOfBoolean;
    }
    this.bitString[paramInt] = paramBoolean;
  }

  public NetscapeCertTypeExtension(byte[] paramArrayOfByte)
    throws IOException
  {
    this.bitString = new BitArray(paramArrayOfByte.length * 8, paramArrayOfByte).toBooleanArray();
    this.extensionId = NetscapeCertType_Id;
    this.critical = true;
    encodeThis();
  }

  public NetscapeCertTypeExtension(boolean[] paramArrayOfBoolean)
    throws IOException
  {
    this.bitString = paramArrayOfBoolean;
    this.extensionId = NetscapeCertType_Id;
    this.critical = true;
    encodeThis();
  }

  public NetscapeCertTypeExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = NetscapeCertType_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue = new DerValue(this.extensionValue);
    this.bitString = localDerValue.getUnalignedBitString().toBooleanArray();
  }

  public NetscapeCertTypeExtension()
  {
    this.extensionId = NetscapeCertType_Id;
    this.critical = true;
    this.bitString = new boolean[0];
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (!(paramObject instanceof Boolean))
      throw new IOException("Attribute must be of type Boolean.");
    boolean bool = ((Boolean)paramObject).booleanValue();
    set(getPosition(paramString), bool);
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    return Boolean.valueOf(isSet(getPosition(paramString)));
  }

  public void delete(String paramString)
    throws IOException
  {
    set(getPosition(paramString), false);
    encodeThis();
  }

  public String toString()
  {
    String str = super.toString() + "NetscapeCertType [\n";
    try
    {
      if (isSet(getPosition("ssl_client")))
        str = str + "   SSL client\n";
      if (isSet(getPosition("ssl_server")))
        str = str + "   SSL server\n";
      if (isSet(getPosition("s_mime")))
        str = str + "   S/MIME\n";
      if (isSet(getPosition("object_signing")))
        str = str + "   Object Signing\n";
      if (isSet(getPosition("ssl_ca")))
        str = str + "   SSL CA\n";
      if (isSet(getPosition("s_mime_ca")))
        str = str + "   S/MIME CA\n";
      if (isSet(getPosition("object_signing_ca")))
        str = str + "   Object Signing CA";
    }
    catch (Exception localException)
    {
    }
    str = str + "]\n";
    return str;
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = NetscapeCertType_Id;
      this.critical = true;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public Enumeration<String> getElements()
  {
    return mAttributeNames.elements();
  }

  public String getName()
  {
    return "NetscapeCertType";
  }

  public boolean[] getKeyUsageMappedBits()
  {
    KeyUsageExtension localKeyUsageExtension = new KeyUsageExtension();
    Boolean localBoolean = Boolean.TRUE;
    try
    {
      if ((isSet(getPosition("ssl_client"))) || (isSet(getPosition("s_mime"))) || (isSet(getPosition("object_signing"))))
        localKeyUsageExtension.set("digital_signature", localBoolean);
      if (isSet(getPosition("ssl_server")))
        localKeyUsageExtension.set("key_encipherment", localBoolean);
      if ((isSet(getPosition("ssl_ca"))) || (isSet(getPosition("s_mime_ca"))) || (isSet(getPosition("object_signing_ca"))))
        localKeyUsageExtension.set("key_certsign", localBoolean);
    }
    catch (IOException localIOException)
    {
    }
    return localKeyUsageExtension.getBits();
  }

  static
  {
    try
    {
      NetscapeCertType_Id = new ObjectIdentifier(CertType_data);
    }
    catch (IOException localIOException)
    {
    }
    mMapData = { new MapEntry("ssl_client", 0), new MapEntry("ssl_server", 1), new MapEntry("s_mime", 2), new MapEntry("object_signing", 3), new MapEntry("ssl_ca", 5), new MapEntry("s_mime_ca", 6), new MapEntry("object_signing_ca", 7) };
    mAttributeNames = new Vector();
    MapEntry[] arrayOfMapEntry = mMapData;
    int i = arrayOfMapEntry.length;
    for (int j = 0; j < i; ++j)
    {
      MapEntry localMapEntry = arrayOfMapEntry[j];
      mAttributeNames.add(localMapEntry.mName);
    }
  }

  private static class MapEntry
  {
    String mName;
    int mPosition;

    MapEntry(String paramString, int paramInt)
    {
      this.mName = paramString;
      this.mPosition = paramInt;
    }
  }
}
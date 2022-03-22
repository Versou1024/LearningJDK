package sun.security.x509;

import java.io.IOException;
import java.util.Arrays;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class Extension
{
  protected ObjectIdentifier extensionId = null;
  protected boolean critical = false;
  protected byte[] extensionValue = null;
  private static final int hashMagic = 31;

  public Extension()
  {
  }

  public Extension(DerValue paramDerValue)
    throws IOException
  {
    DerInputStream localDerInputStream = paramDerValue.toDerInputStream();
    this.extensionId = localDerInputStream.getOID();
    DerValue localDerValue = localDerInputStream.getDerValue();
    if (localDerValue.tag == 1)
    {
      this.critical = localDerValue.getBoolean();
      localDerValue = localDerInputStream.getDerValue();
      this.extensionValue = localDerValue.getOctetString();
    }
    else
    {
      this.critical = false;
      this.extensionValue = localDerValue.getOctetString();
    }
  }

  public Extension(ObjectIdentifier paramObjectIdentifier, boolean paramBoolean, byte[] paramArrayOfByte)
    throws IOException
  {
    this.extensionId = paramObjectIdentifier;
    this.critical = paramBoolean;
    DerValue localDerValue = new DerValue(paramArrayOfByte);
    this.extensionValue = localDerValue.getOctetString();
  }

  public Extension(Extension paramExtension)
  {
    this.extensionId = paramExtension.extensionId;
    this.critical = paramExtension.critical;
    this.extensionValue = paramExtension.extensionValue;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    if (this.extensionId == null)
      throw new IOException("Null OID to encode for the extension!");
    if (this.extensionValue == null)
      throw new IOException("No value to encode for the extension!");
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putOID(this.extensionId);
    if (this.critical)
      localDerOutputStream.putBoolean(this.critical);
    localDerOutputStream.putOctetString(this.extensionValue);
    paramDerOutputStream.write(48, localDerOutputStream);
  }

  public boolean isCritical()
  {
    return this.critical;
  }

  public ObjectIdentifier getExtensionId()
  {
    return this.extensionId;
  }

  public byte[] getExtensionValue()
  {
    return this.extensionValue;
  }

  public String toString()
  {
    String str = "ObjectId: " + this.extensionId.toString();
    if (this.critical)
      str = str + " Criticality=true\n";
    else
      str = str + " Criticality=false\n";
    return str;
  }

  public int hashCode()
  {
    int i = 0;
    if (this.extensionValue != null)
    {
      byte[] arrayOfByte = this.extensionValue;
      int j = arrayOfByte.length;
      while (j > 0)
        i += j * arrayOfByte[(--j)];
    }
    i = i * 31 + this.extensionId.hashCode();
    i = i * 31 + ((this.critical) ? 1231 : 1237);
    return i;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof Extension))
      return false;
    Extension localExtension = (Extension)paramObject;
    if (this.critical != localExtension.critical)
      return false;
    if (!(this.extensionId.equals(localExtension.extensionId)))
      return false;
    return Arrays.equals(this.extensionValue, localExtension.extensionValue);
  }
}
package sun.security.x509;

import B;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class OtherName
  implements GeneralNameInterface
{
  private String name;
  private ObjectIdentifier oid;
  private byte[] nameValue = null;
  private GeneralNameInterface gni = null;
  private static final byte TAG_VALUE = 0;
  private int myhash = -1;

  public OtherName(ObjectIdentifier paramObjectIdentifier, byte[] paramArrayOfByte)
    throws IOException
  {
    if ((paramObjectIdentifier == null) || (paramArrayOfByte == null))
      throw new NullPointerException("parameters may not be null");
    this.oid = paramObjectIdentifier;
    this.nameValue = paramArrayOfByte;
    this.gni = getGNI(paramObjectIdentifier, paramArrayOfByte);
    if (this.gni != null)
      this.name = this.gni.toString();
    else
      this.name = "Unrecognized ObjectIdentifier: " + paramObjectIdentifier.toString();
  }

  public OtherName(DerValue paramDerValue)
    throws IOException
  {
    DerInputStream localDerInputStream = paramDerValue.toDerInputStream();
    this.oid = localDerInputStream.getOID();
    DerValue localDerValue = localDerInputStream.getDerValue();
    this.nameValue = localDerValue.toByteArray();
    this.gni = getGNI(this.oid, this.nameValue);
    if (this.gni != null)
      this.name = this.gni.toString();
    else
      this.name = "Unrecognized ObjectIdentifier: " + this.oid.toString();
  }

  public ObjectIdentifier getOID()
  {
    return this.oid;
  }

  public byte[] getNameValue()
  {
    return ((byte[])(byte[])this.nameValue.clone());
  }

  private GeneralNameInterface getGNI(ObjectIdentifier paramObjectIdentifier, byte[] paramArrayOfByte)
    throws IOException
  {
    Class localClass;
    try
    {
      localClass = OIDMap.getClass(paramObjectIdentifier);
      if (localClass == null)
        return null;
      Class[] arrayOfClass = { Object.class };
      Constructor localConstructor = localClass.getConstructor(arrayOfClass);
      Object[] arrayOfObject = { paramArrayOfByte };
      GeneralNameInterface localGeneralNameInterface = (GeneralNameInterface)localConstructor.newInstance(arrayOfObject);
      return localGeneralNameInterface;
    }
    catch (Exception localException)
    {
      throw ((IOException)new IOException("Instantiation error: " + localException).initCause(localException));
    }
  }

  public int getType()
  {
    return 0;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    if (this.gni != null)
    {
      this.gni.encode(paramDerOutputStream);
      return;
    }
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putOID(this.oid);
    localDerOutputStream.write(DerValue.createTag(-128, true, 0), this.nameValue);
    paramDerOutputStream.write(48, localDerOutputStream);
  }

  public boolean equals(Object paramObject)
  {
    boolean bool;
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof OtherName))
      return false;
    OtherName localOtherName = (OtherName)paramObject;
    if (!(localOtherName.oid.equals(this.oid)))
      return false;
    GeneralNameInterface localGeneralNameInterface = null;
    try
    {
      localGeneralNameInterface = getGNI(localOtherName.oid, localOtherName.nameValue);
    }
    catch (IOException localIOException)
    {
      return false;
    }
    if (localGeneralNameInterface != null)
      try
      {
        bool = localGeneralNameInterface.constrains(this) == 0;
      }
      catch (UnsupportedOperationException localUnsupportedOperationException)
      {
        bool = false;
      }
    else
      bool = Arrays.equals(this.nameValue, localOtherName.nameValue);
    return bool;
  }

  public int hashCode()
  {
    if (this.myhash == -1)
    {
      this.myhash = (37 + this.oid.hashCode());
      for (int i = 0; i < this.nameValue.length; ++i)
        this.myhash = (37 * this.myhash + this.nameValue[i]);
    }
    return this.myhash;
  }

  public String toString()
  {
    return "Other-Name: " + this.name;
  }

  public int constrains(GeneralNameInterface paramGeneralNameInterface)
  {
    int i;
    if (paramGeneralNameInterface == null)
      i = -1;
    else if (paramGeneralNameInterface.getType() != 0)
      i = -1;
    else
      throw new UnsupportedOperationException("Narrowing, widening, and matching are not supported for OtherName.");
    return i;
  }

  public int subtreeDepth()
  {
    throw new UnsupportedOperationException("subtreeDepth() not supported for generic OtherName");
  }
}
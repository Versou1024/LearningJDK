package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class OIDName
  implements GeneralNameInterface
{
  private ObjectIdentifier oid;

  public OIDName(DerValue paramDerValue)
    throws IOException
  {
    this.oid = paramDerValue.getOID();
  }

  public OIDName(ObjectIdentifier paramObjectIdentifier)
  {
    this.oid = paramObjectIdentifier;
  }

  public OIDName(String paramString)
    throws IOException
  {
    try
    {
      this.oid = new ObjectIdentifier(paramString);
    }
    catch (Exception localException)
    {
      throw new IOException("Unable to create OIDName: " + localException);
    }
  }

  public int getType()
  {
    return 8;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    paramDerOutputStream.putOID(this.oid);
  }

  public String toString()
  {
    return "OIDName: " + this.oid.toString();
  }

  public ObjectIdentifier getOID()
  {
    return this.oid;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof OIDName))
      return false;
    OIDName localOIDName = (OIDName)paramObject;
    return this.oid.equals(localOIDName.oid);
  }

  public int hashCode()
  {
    return this.oid.hashCode();
  }

  public int constrains(GeneralNameInterface paramGeneralNameInterface)
    throws UnsupportedOperationException
  {
    int i;
    if (paramGeneralNameInterface == null)
      i = -1;
    else if (paramGeneralNameInterface.getType() != 8)
      i = -1;
    else if (equals((OIDName)paramGeneralNameInterface))
      i = 0;
    else
      throw new UnsupportedOperationException("Narrowing and widening are not supported for OIDNames");
    return i;
  }

  public int subtreeDepth()
    throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("subtreeDepth() not supported for OIDName.");
  }
}
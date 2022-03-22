package sun.security.jgss.wrapper;

import java.io.IOException;
import java.security.Provider;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.GSSExceptionImpl;
import sun.security.jgss.GSSUtil;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.ObjectIdentifier;

public class GSSNameElement
  implements GSSNameSpi
{
  long pName = 3412045659165949952L;
  private String printableName;
  private Oid printableType;
  private GSSLibStub cStub;
  static final GSSNameElement DEF_ACCEPTOR;

  private static Oid getNativeNameType(Oid paramOid, GSSLibStub paramGSSLibStub)
  {
    if ((GSSUtil.NT_GSS_KRB5_PRINCIPAL.equals(paramOid)) || (GSSName.NT_HOSTBASED_SERVICE.equals(paramOid)))
    {
      Oid[] arrayOfOid = null;
      try
      {
        arrayOfOid = paramGSSLibStub.inquireNamesForMech();
      }
      catch (GSSException localGSSException1)
      {
        if ((localGSSException1.getMajor() == 2) && (GSSUtil.isSpNegoMech(paramGSSLibStub.getMech())))
          try
          {
            paramGSSLibStub = GSSLibStub.getInstance(GSSUtil.GSS_KRB5_MECH_OID);
            arrayOfOid = paramGSSLibStub.inquireNamesForMech();
          }
          catch (GSSException localGSSException2)
          {
            SunNativeProvider.debug("Name type list unavailable: " + localGSSException2.getMajorString());
          }
        else
          SunNativeProvider.debug("Name type list unavailable: " + localGSSException1.getMajorString());
      }
      if (arrayOfOid != null)
      {
        for (int i = 0; i < arrayOfOid.length; ++i)
          if (arrayOfOid[i].equals(paramOid))
            return paramOid;
        if (GSSUtil.NT_GSS_KRB5_PRINCIPAL.equals(paramOid))
        {
          SunNativeProvider.debug("Override " + paramOid + " with mechanism default(null)");
          return null;
        }
        SunNativeProvider.debug("Override " + paramOid + " with " + GSSUtil.NT_HOSTBASED_SERVICE2);
        return GSSUtil.NT_HOSTBASED_SERVICE2;
      }
    }
    return paramOid;
  }

  private GSSNameElement()
  {
    this.printableName = "<DEFAULT ACCEPTOR>";
  }

  GSSNameElement(long paramLong, GSSLibStub paramGSSLibStub)
    throws GSSException
  {
    if ((!($assertionsDisabled)) && (paramGSSLibStub == null))
      throw new AssertionError();
    if (paramLong == 3412046810217185280L)
      throw new GSSException(3);
    this.pName = paramLong;
    this.cStub = paramGSSLibStub;
    setPrintables();
  }

  GSSNameElement(byte[] paramArrayOfByte, Oid paramOid, GSSLibStub paramGSSLibStub)
    throws GSSException
  {
    if ((!($assertionsDisabled)) && (paramGSSLibStub == null))
      throw new AssertionError();
    if (paramArrayOfByte == null)
      throw new GSSException(3);
    this.cStub = paramGSSLibStub;
    byte[] arrayOfByte1 = paramArrayOfByte;
    if (paramOid != null)
    {
      paramOid = getNativeNameType(paramOid, paramGSSLibStub);
      if (GSSName.NT_EXPORT_NAME.equals(paramOid))
      {
        byte[] arrayOfByte2 = null;
        DerOutputStream localDerOutputStream = new DerOutputStream();
        Oid localOid = this.cStub.getMech();
        try
        {
          localDerOutputStream.putOID(new ObjectIdentifier(localOid.toString()));
        }
        catch (IOException localIOException)
        {
          throw new GSSExceptionImpl(11, localIOException);
        }
        arrayOfByte2 = localDerOutputStream.toByteArray();
        arrayOfByte1 = new byte[4 + arrayOfByte2.length + 4 + paramArrayOfByte.length];
        int i = 0;
        arrayOfByte1[(i++)] = 4;
        arrayOfByte1[(i++)] = 1;
        arrayOfByte1[(i++)] = (byte)(arrayOfByte2.length >>> 8);
        arrayOfByte1[(i++)] = (byte)arrayOfByte2.length;
        System.arraycopy(arrayOfByte2, 0, arrayOfByte1, i, arrayOfByte2.length);
        i += arrayOfByte2.length;
        arrayOfByte1[(i++)] = (byte)(paramArrayOfByte.length >>> 24);
        arrayOfByte1[(i++)] = (byte)(paramArrayOfByte.length >>> 16);
        arrayOfByte1[(i++)] = (byte)(paramArrayOfByte.length >>> 8);
        arrayOfByte1[(i++)] = (byte)paramArrayOfByte.length;
        System.arraycopy(paramArrayOfByte, 0, arrayOfByte1, i, paramArrayOfByte.length);
      }
    }
    this.pName = this.cStub.importName(arrayOfByte1, paramOid);
    setPrintables();
    SunNativeProvider.debug("Imported " + this.printableName + " w/ type " + this.printableType);
  }

  private void setPrintables()
    throws GSSException
  {
    Object[] arrayOfObject = null;
    arrayOfObject = this.cStub.displayName(this.pName);
    if ((!($assertionsDisabled)) && (((arrayOfObject == null) || (arrayOfObject.length != 2))))
      throw new AssertionError();
    this.printableName = ((String)arrayOfObject[0]);
    if ((!($assertionsDisabled)) && (this.printableName == null))
      throw new AssertionError();
    this.printableType = ((Oid)arrayOfObject[1]);
    if (this.printableType == null)
      this.printableType = GSSName.NT_USER_NAME;
  }

  public String getKrbName()
    throws GSSException
  {
    long l = 3412047291253522432L;
    GSSLibStub localGSSLibStub = this.cStub;
    if (!(GSSUtil.isKerberosMech(this.cStub.getMech())))
      localGSSLibStub = GSSLibStub.getInstance(GSSUtil.GSS_KRB5_MECH_OID);
    l = localGSSLibStub.canonicalizeName(this.pName);
    Object[] arrayOfObject = localGSSLibStub.displayName(l);
    localGSSLibStub.releaseName(l);
    SunNativeProvider.debug("Got kerberized name: " + arrayOfObject[0]);
    return ((String)arrayOfObject[0]);
  }

  public Provider getProvider()
  {
    return SunNativeProvider.INSTANCE;
  }

  public boolean equals(GSSNameSpi paramGSSNameSpi)
    throws GSSException
  {
    if (!(paramGSSNameSpi instanceof GSSNameElement))
      return false;
    return this.cStub.compareName(this.pName, ((GSSNameElement)paramGSSNameSpi).pName);
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof GSSNameElement))
      return false;
    try
    {
      return equals((GSSNameElement)paramObject);
    }
    catch (GSSException localGSSException)
    {
    }
    return false;
  }

  public int hashCode()
  {
    return new Long(this.pName).hashCode();
  }

  public byte[] export()
    throws GSSException
  {
    byte[] arrayOfByte1 = this.cStub.exportName(this.pName);
    int i = 0;
    if ((arrayOfByte1[(i++)] != 4) || (arrayOfByte1[(i++)] != 1))
      throw new GSSException(3);
    int j = (0xFF & arrayOfByte1[(i++)]) << 8 | 0xFF & arrayOfByte1[(i++)];
    ObjectIdentifier localObjectIdentifier = null;
    try
    {
      DerInputStream localDerInputStream = new DerInputStream(arrayOfByte1, i, j);
      localObjectIdentifier = new ObjectIdentifier(localDerInputStream);
    }
    catch (IOException localIOException)
    {
      throw new GSSExceptionImpl(3, localIOException);
    }
    Oid localOid = new Oid(localObjectIdentifier.toString());
    if ((!($assertionsDisabled)) && (!(localOid.equals(getMechanism()))))
      throw new AssertionError();
    i += j;
    int k = (0xFF & arrayOfByte1[(i++)]) << 24 | (0xFF & arrayOfByte1[(i++)]) << 16 | (0xFF & arrayOfByte1[(i++)]) << 8 | 0xFF & arrayOfByte1[(i++)];
    byte[] arrayOfByte2 = new byte[k];
    System.arraycopy(arrayOfByte1, i, arrayOfByte2, 0, k);
    return arrayOfByte2;
  }

  public Oid getMechanism()
  {
    return this.cStub.getMech();
  }

  public String toString()
  {
    return this.printableName;
  }

  public Oid getStringNameType()
  {
    return this.printableType;
  }

  public boolean isAnonymousName()
  {
    return GSSName.NT_ANONYMOUS.equals(this.printableType);
  }

  public void dispose()
  {
    if (this.pName != 3412046810217185280L)
    {
      this.cStub.releaseName(this.pName);
      this.pName = 3412047463052214272L;
    }
  }

  protected void finalize()
    throws Throwable
  {
    dispose();
  }

  static
  {
    DEF_ACCEPTOR = new GSSNameElement();
  }
}
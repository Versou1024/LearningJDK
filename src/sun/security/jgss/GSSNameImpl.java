package sun.security.jgss;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.ObjectIdentifier;

public class GSSNameImpl
  implements GSSName
{
  private GSSManagerImpl gssManager;
  private String appNameStr;
  private byte[] appNameBytes;
  private Oid appNameType;
  private String printableName;
  private Oid printableNameType;
  private HashMap elements;
  private GSSNameSpi mechElement;

  static GSSNameImpl wrapElement(GSSManagerImpl paramGSSManagerImpl, GSSNameSpi paramGSSNameSpi)
    throws GSSException
  {
    return new GSSNameImpl(paramGSSManagerImpl, paramGSSNameSpi);
  }

  GSSNameImpl(GSSManagerImpl paramGSSManagerImpl, GSSNameSpi paramGSSNameSpi)
  {
    this.gssManager = null;
    this.appNameStr = null;
    this.appNameBytes = null;
    this.appNameType = null;
    this.printableName = null;
    this.printableNameType = null;
    this.elements = null;
    this.mechElement = null;
    this.gssManager = paramGSSManagerImpl;
    this.appNameStr = (this.printableName = paramGSSNameSpi.toString());
    this.appNameType = (this.printableNameType = paramGSSNameSpi.getStringNameType());
    this.mechElement = paramGSSNameSpi;
    this.elements = new HashMap(1);
    this.elements.put(paramGSSNameSpi.getMechanism(), this.mechElement);
  }

  GSSNameImpl(GSSManagerImpl paramGSSManagerImpl, Object paramObject, Oid paramOid)
    throws GSSException
  {
    this(paramGSSManagerImpl, paramObject, paramOid, null);
  }

  GSSNameImpl(GSSManagerImpl paramGSSManagerImpl, Object paramObject, Oid paramOid1, Oid paramOid2)
    throws GSSException
  {
    this.gssManager = null;
    this.appNameStr = null;
    this.appNameBytes = null;
    this.appNameType = null;
    this.printableName = null;
    this.printableNameType = null;
    this.elements = null;
    this.mechElement = null;
    if (paramObject == null)
      throw new GSSExceptionImpl(3, "Cannot import null name");
    if (paramOid2 == null)
      paramOid2 = ProviderList.DEFAULT_MECH_OID;
    if (NT_EXPORT_NAME.equals(paramOid1))
      importName(paramGSSManagerImpl, paramObject);
    else
      init(paramGSSManagerImpl, paramObject, paramOid1, paramOid2);
  }

  private void init(GSSManagerImpl paramGSSManagerImpl, Object paramObject, Oid paramOid1, Oid paramOid2)
    throws GSSException
  {
    this.gssManager = paramGSSManagerImpl;
    this.elements = new HashMap(paramGSSManagerImpl.getMechs().length);
    if (paramObject instanceof String)
    {
      this.appNameStr = ((String)paramObject);
      if (paramOid1 != null)
      {
        this.printableName = this.appNameStr;
        this.printableNameType = paramOid1;
      }
    }
    else
    {
      this.appNameBytes = ((byte[])(byte[])paramObject);
    }
    this.appNameType = paramOid1;
    this.mechElement = getElement(paramOid2);
    if (this.printableName == null)
    {
      this.printableName = this.mechElement.toString();
      this.printableNameType = this.mechElement.getStringNameType();
    }
  }

  private void importName(GSSManagerImpl paramGSSManagerImpl, Object paramObject)
    throws GSSException
  {
    int i = 0;
    byte[] arrayOfByte1 = null;
    if (paramObject instanceof String)
      try
      {
        arrayOfByte1 = ((String)paramObject).getBytes("UTF-8");
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException)
      {
      }
    else
      arrayOfByte1 = (byte[])(byte[])paramObject;
    if ((arrayOfByte1[(i++)] != 4) || (arrayOfByte1[(i++)] != 1))
      throw new GSSExceptionImpl(3, "Exported name token id is corrupted!");
    int j = (0xFF & arrayOfByte1[(i++)]) << 8 | 0xFF & arrayOfByte1[(i++)];
    ObjectIdentifier localObjectIdentifier = null;
    try
    {
      DerInputStream localDerInputStream = new DerInputStream(arrayOfByte1, i, j);
      localObjectIdentifier = new ObjectIdentifier(localDerInputStream);
    }
    catch (IOException localIOException)
    {
      throw new GSSExceptionImpl(3, "Exported name Object identifier is corrupted!");
    }
    Oid localOid = new Oid(localObjectIdentifier.toString());
    i += j;
    int k = (0xFF & arrayOfByte1[(i++)]) << 24 | (0xFF & arrayOfByte1[(i++)]) << 16 | (0xFF & arrayOfByte1[(i++)]) << 8 | 0xFF & arrayOfByte1[(i++)];
    byte[] arrayOfByte2 = new byte[k];
    System.arraycopy(arrayOfByte1, i, arrayOfByte2, 0, k);
    init(paramGSSManagerImpl, arrayOfByte2, NT_EXPORT_NAME, localOid);
  }

  public GSSName canonicalize(Oid paramOid)
    throws GSSException
  {
    if (paramOid == null)
      paramOid = ProviderList.DEFAULT_MECH_OID;
    return wrapElement(this.gssManager, getElement(paramOid));
  }

  public boolean equals(GSSName paramGSSName)
    throws GSSException
  {
    if ((isAnonymous()) || (paramGSSName.isAnonymous()))
      return false;
    if (paramGSSName == this)
      return true;
    if (!(paramGSSName instanceof GSSNameImpl))
      return equals(this.gssManager.createName(paramGSSName.toString(), paramGSSName.getStringNameType()));
    GSSNameImpl localGSSNameImpl = (GSSNameImpl)paramGSSName;
    GSSNameSpi localGSSNameSpi1 = this.mechElement;
    GSSNameSpi localGSSNameSpi2 = localGSSNameImpl.mechElement;
    if ((localGSSNameSpi1 == null) && (localGSSNameSpi2 != null))
      localGSSNameSpi1 = getElement(localGSSNameSpi2.getMechanism());
    else if ((localGSSNameSpi1 != null) && (localGSSNameSpi2 == null))
      localGSSNameSpi2 = localGSSNameImpl.getElement(localGSSNameSpi1.getMechanism());
    if ((localGSSNameSpi1 != null) && (localGSSNameSpi2 != null))
      return localGSSNameSpi1.equals(localGSSNameSpi2);
    if ((this.appNameType != null) && (localGSSNameImpl.appNameType != null))
    {
      if (!(this.appNameType.equals(localGSSNameImpl.appNameType)))
        return false;
      byte[] arrayOfByte1 = null;
      byte[] arrayOfByte2 = null;
      try
      {
        arrayOfByte1 = (this.appNameStr != null) ? this.appNameStr.getBytes("UTF-8") : this.appNameBytes;
        arrayOfByte2 = (localGSSNameImpl.appNameStr != null) ? localGSSNameImpl.appNameStr.getBytes("UTF-8") : localGSSNameImpl.appNameBytes;
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException)
      {
      }
      return Arrays.equals(arrayOfByte1, arrayOfByte2);
    }
    return false;
  }

  public int hashCode()
  {
    return 1;
  }

  public boolean equals(Object paramObject)
  {
    try
    {
      if (paramObject instanceof GSSName)
        return equals((GSSName)paramObject);
    }
    catch (GSSException localGSSException)
    {
    }
    return false;
  }

  public byte[] export()
    throws GSSException
  {
    if (this.mechElement == null)
      this.mechElement = getElement(ProviderList.DEFAULT_MECH_OID);
    byte[] arrayOfByte1 = this.mechElement.export();
    byte[] arrayOfByte2 = null;
    ObjectIdentifier localObjectIdentifier = null;
    try
    {
      localObjectIdentifier = new ObjectIdentifier(this.mechElement.getMechanism().toString());
    }
    catch (IOException localIOException1)
    {
      throw new GSSExceptionImpl(11, "Invalid OID String ");
    }
    DerOutputStream localDerOutputStream = new DerOutputStream();
    try
    {
      localDerOutputStream.putOID(localObjectIdentifier);
    }
    catch (IOException localIOException2)
    {
      throw new GSSExceptionImpl(11, "Could not ASN.1 Encode " + localObjectIdentifier.toString());
    }
    arrayOfByte2 = localDerOutputStream.toByteArray();
    byte[] arrayOfByte3 = new byte[4 + arrayOfByte2.length + 4 + arrayOfByte1.length];
    int i = 0;
    arrayOfByte3[(i++)] = 4;
    arrayOfByte3[(i++)] = 1;
    arrayOfByte3[(i++)] = (byte)(arrayOfByte2.length >>> 8);
    arrayOfByte3[(i++)] = (byte)arrayOfByte2.length;
    System.arraycopy(arrayOfByte2, 0, arrayOfByte3, i, arrayOfByte2.length);
    i += arrayOfByte2.length;
    arrayOfByte3[(i++)] = (byte)(arrayOfByte1.length >>> 24);
    arrayOfByte3[(i++)] = (byte)(arrayOfByte1.length >>> 16);
    arrayOfByte3[(i++)] = (byte)(arrayOfByte1.length >>> 8);
    arrayOfByte3[(i++)] = (byte)arrayOfByte1.length;
    System.arraycopy(arrayOfByte1, 0, arrayOfByte3, i, arrayOfByte1.length);
    return arrayOfByte3;
  }

  public String toString()
  {
    return this.printableName;
  }

  public Oid getStringNameType()
    throws GSSException
  {
    return this.printableNameType;
  }

  public boolean isAnonymous()
  {
    if (this.printableNameType == null)
      return false;
    return GSSName.NT_ANONYMOUS.equals(this.printableNameType);
  }

  public boolean isMN()
  {
    return true;
  }

  public synchronized GSSNameSpi getElement(Oid paramOid)
    throws GSSException
  {
    GSSNameSpi localGSSNameSpi = (GSSNameSpi)this.elements.get(paramOid);
    if (localGSSNameSpi == null)
    {
      if (this.appNameStr != null)
        localGSSNameSpi = this.gssManager.getNameElement(this.appNameStr, this.appNameType, paramOid);
      else
        localGSSNameSpi = this.gssManager.getNameElement(this.appNameBytes, this.appNameType, paramOid);
      this.elements.put(paramOid, localGSSNameSpi);
    }
    return localGSSNameSpi;
  }

  Set getElements()
  {
    return new HashSet(this.elements.values());
  }

  private static String getNameTypeStr(Oid paramOid)
  {
    if (paramOid == null)
      return "(NT is null)";
    if (paramOid.equals(NT_USER_NAME))
      return "NT_USER_NAME";
    if (paramOid.equals(NT_HOSTBASED_SERVICE))
      return "NT_HOSTBASED_SERVICE";
    if (paramOid.equals(NT_EXPORT_NAME))
      return "NT_EXPORT_NAME";
    if (paramOid.equals(GSSUtil.NT_GSS_KRB5_PRINCIPAL))
      return "NT_GSS_KRB5_PRINCIPAL";
    return "Unknown";
  }
}
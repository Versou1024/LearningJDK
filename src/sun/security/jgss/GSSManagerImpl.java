package sun.security.jgss;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.jgss.spi.MechanismFactory;

public class GSSManagerImpl extends GSSManager
{
  private static final String USE_NATIVE_PROP = "sun.security.jgss.native";
  private static final Boolean USE_NATIVE = (Boolean)AccessController.doPrivileged(new PrivilegedAction()
  {
    public Boolean run()
    {
      String str = System.getProperty("os.name");
      if ((str.startsWith("SunOS")) || (str.startsWith("Linux")))
        return new Boolean(System.getProperty("sun.security.jgss.native"));
      return Boolean.FALSE;
    }
  });
  private ProviderList list;

  public GSSManagerImpl(int paramInt, boolean paramBoolean)
  {
    this.list = new ProviderList(paramInt, paramBoolean);
  }

  public GSSManagerImpl(int paramInt)
  {
    this.list = new ProviderList(paramInt, USE_NATIVE.booleanValue());
  }

  public GSSManagerImpl()
  {
    this.list = new ProviderList(-1, USE_NATIVE.booleanValue());
  }

  public Oid[] getMechs()
  {
    return this.list.getMechs();
  }

  public Oid[] getNamesForMech(Oid paramOid)
    throws GSSException
  {
    MechanismFactory localMechanismFactory = this.list.getMechFactory(paramOid);
    return ((Oid[])(Oid[])localMechanismFactory.getNameTypes().clone());
  }

  public Oid[] getMechsForName(Oid paramOid)
  {
    Oid[] arrayOfOid1 = this.list.getMechs();
    Object localObject = new Oid[arrayOfOid1.length];
    int i = 0;
    for (int j = 0; j < arrayOfOid1.length; ++j)
    {
      Oid localOid = arrayOfOid1[j];
      try
      {
        Oid[] arrayOfOid3 = getNamesForMech(localOid);
        if (paramOid.containedIn(arrayOfOid3))
          localObject[(i++)] = localOid;
      }
      catch (GSSException localGSSException)
      {
        GSSUtil.debug("Skip " + localOid + ": error retrieving supported name types");
      }
    }
    if (i < localObject.length)
    {
      Oid[] arrayOfOid2 = new Oid[i];
      for (int k = 0; k < i; ++k)
        arrayOfOid2[k] = localObject[k];
      localObject = arrayOfOid2;
    }
    return ((Oid)localObject);
  }

  public GSSName createName(String paramString, Oid paramOid)
    throws GSSException
  {
    return new GSSNameImpl(this, paramString, paramOid);
  }

  public GSSName createName(byte[] paramArrayOfByte, Oid paramOid)
    throws GSSException
  {
    return new GSSNameImpl(this, paramArrayOfByte, paramOid);
  }

  public GSSName createName(String paramString, Oid paramOid1, Oid paramOid2)
    throws GSSException
  {
    return new GSSNameImpl(this, paramString, paramOid1, paramOid2);
  }

  public GSSName createName(byte[] paramArrayOfByte, Oid paramOid1, Oid paramOid2)
    throws GSSException
  {
    return new GSSNameImpl(this, paramArrayOfByte, paramOid1, paramOid2);
  }

  public GSSCredential createCredential(int paramInt)
    throws GSSException
  {
    return new GSSCredentialImpl(this, paramInt);
  }

  public GSSCredential createCredential(GSSName paramGSSName, int paramInt1, Oid paramOid, int paramInt2)
    throws GSSException
  {
    return new GSSCredentialImpl(this, paramGSSName, paramInt1, paramOid, paramInt2);
  }

  public GSSCredential createCredential(GSSName paramGSSName, int paramInt1, Oid[] paramArrayOfOid, int paramInt2)
    throws GSSException
  {
    return new GSSCredentialImpl(this, paramGSSName, paramInt1, paramArrayOfOid, paramInt2);
  }

  public GSSContext createContext(GSSName paramGSSName, Oid paramOid, GSSCredential paramGSSCredential, int paramInt)
    throws GSSException
  {
    return new GSSContextImpl(this, paramGSSName, paramOid, paramGSSCredential, paramInt);
  }

  public GSSContext createContext(GSSCredential paramGSSCredential)
    throws GSSException
  {
    return new GSSContextImpl(this, paramGSSCredential);
  }

  public GSSContext createContext(byte[] paramArrayOfByte)
    throws GSSException
  {
    return new GSSContextImpl(this, paramArrayOfByte);
  }

  public void addProviderAtFront(Provider paramProvider, Oid paramOid)
    throws GSSException
  {
    this.list.addProviderAtFront(paramProvider, paramOid);
  }

  public void addProviderAtEnd(Provider paramProvider, Oid paramOid)
    throws GSSException
  {
    this.list.addProviderAtEnd(paramProvider, paramOid);
  }

  public GSSCredentialSpi getCredentialElement(GSSNameSpi paramGSSNameSpi, int paramInt1, int paramInt2, Oid paramOid, int paramInt3)
    throws GSSException
  {
    MechanismFactory localMechanismFactory = this.list.getMechFactory(paramOid);
    return localMechanismFactory.getCredentialElement(paramGSSNameSpi, paramInt1, paramInt2, paramInt3);
  }

  public GSSNameSpi getNameElement(String paramString, Oid paramOid1, Oid paramOid2)
    throws GSSException
  {
    MechanismFactory localMechanismFactory = this.list.getMechFactory(paramOid2);
    return localMechanismFactory.getNameElement(paramString, paramOid1);
  }

  public GSSNameSpi getNameElement(byte[] paramArrayOfByte, Oid paramOid1, Oid paramOid2)
    throws GSSException
  {
    MechanismFactory localMechanismFactory = this.list.getMechFactory(paramOid2);
    return localMechanismFactory.getNameElement(paramArrayOfByte, paramOid1);
  }

  GSSContextSpi getMechanismContext(GSSNameSpi paramGSSNameSpi, GSSCredentialSpi paramGSSCredentialSpi, int paramInt, Oid paramOid)
    throws GSSException
  {
    Provider localProvider = null;
    if (paramGSSCredentialSpi != null)
      localProvider = paramGSSCredentialSpi.getProvider();
    MechanismFactory localMechanismFactory = this.list.getMechFactory(paramOid, localProvider);
    return localMechanismFactory.getMechanismContext(paramGSSNameSpi, paramGSSCredentialSpi, paramInt);
  }

  GSSContextSpi getMechanismContext(GSSCredentialSpi paramGSSCredentialSpi, Oid paramOid)
    throws GSSException
  {
    Provider localProvider = null;
    if (paramGSSCredentialSpi != null)
      localProvider = paramGSSCredentialSpi.getProvider();
    MechanismFactory localMechanismFactory = this.list.getMechFactory(paramOid, localProvider);
    return localMechanismFactory.getMechanismContext(paramGSSCredentialSpi);
  }

  GSSContextSpi getMechanismContext(byte[] paramArrayOfByte)
    throws GSSException
  {
    if ((paramArrayOfByte == null) || (paramArrayOfByte.length == 0))
      throw new GSSException(12);
    GSSContextSpi localGSSContextSpi = null;
    Oid[] arrayOfOid = this.list.getMechs();
    for (int i = 0; i < arrayOfOid.length; ++i)
    {
      MechanismFactory localMechanismFactory = this.list.getMechFactory(arrayOfOid[i]);
      if (localMechanismFactory.getProvider().getName().equals("SunNativeGSS"))
      {
        localGSSContextSpi = localMechanismFactory.getMechanismContext(paramArrayOfByte);
        if (localGSSContextSpi != null)
          break;
      }
    }
    if (localGSSContextSpi == null)
      throw new GSSException(16);
    return localGSSContextSpi;
  }
}
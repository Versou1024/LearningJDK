package sun.security.jgss;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;

public class GSSCredentialImpl
  implements GSSCredential
{
  private GSSManagerImpl gssManager;
  private boolean destroyed;
  private Hashtable hashtable;
  private GSSCredentialSpi tempCred;

  GSSCredentialImpl(GSSManagerImpl paramGSSManagerImpl, int paramInt)
    throws GSSException
  {
    this(paramGSSManagerImpl, null, 0, (Oid[])null, paramInt);
  }

  GSSCredentialImpl(GSSManagerImpl paramGSSManagerImpl, GSSName paramGSSName, int paramInt1, Oid paramOid, int paramInt2)
    throws GSSException
  {
    this.gssManager = null;
    this.destroyed = false;
    this.hashtable = null;
    this.tempCred = null;
    if (paramOid == null)
      paramOid = ProviderList.DEFAULT_MECH_OID;
    init(paramGSSManagerImpl);
    add(paramGSSName, paramInt1, paramInt1, paramOid, paramInt2);
  }

  GSSCredentialImpl(GSSManagerImpl paramGSSManagerImpl, GSSName paramGSSName, int paramInt1, Oid[] paramArrayOfOid, int paramInt2)
    throws GSSException
  {
    this.gssManager = null;
    this.destroyed = false;
    this.hashtable = null;
    this.tempCred = null;
    init(paramGSSManagerImpl);
    int i = 0;
    if (paramArrayOfOid == null)
    {
      paramArrayOfOid = paramGSSManagerImpl.getMechs();
      i = 1;
    }
    for (int j = 0; j < paramArrayOfOid.length; ++j)
      try
      {
        add(paramGSSName, paramInt1, paramInt1, paramArrayOfOid[j], paramInt2);
      }
      catch (GSSException localGSSException)
      {
        if (i != 0)
          GSSUtil.debug("Ignore " + localGSSException + " while acquring cred for " + paramArrayOfOid[j]);
        else
          throw localGSSException;
      }
    if ((this.hashtable.size() == 0) || (paramInt2 != getUsage()))
      throw new GSSException(13);
  }

  public GSSCredentialImpl(GSSManagerImpl paramGSSManagerImpl, GSSCredentialSpi paramGSSCredentialSpi)
    throws GSSException
  {
    this.gssManager = null;
    this.destroyed = false;
    this.hashtable = null;
    this.tempCred = null;
    init(paramGSSManagerImpl);
    int i = 2;
    if (paramGSSCredentialSpi.isInitiatorCredential())
      if (paramGSSCredentialSpi.isAcceptorCredential())
        i = 0;
      else
        i = 1;
    SearchKey localSearchKey = new SearchKey(paramGSSCredentialSpi.getMechanism(), i);
    this.tempCred = paramGSSCredentialSpi;
    this.hashtable.put(localSearchKey, this.tempCred);
  }

  void init(GSSManagerImpl paramGSSManagerImpl)
  {
    this.gssManager = paramGSSManagerImpl;
    this.hashtable = new Hashtable(paramGSSManagerImpl.getMechs().length);
  }

  public void dispose()
    throws GSSException
  {
    if (!(this.destroyed))
    {
      Enumeration localEnumeration = this.hashtable.elements();
      while (localEnumeration.hasMoreElements())
      {
        Object localObject = localEnumeration.nextElement();
        ((GSSCredentialSpi)localObject).dispose();
      }
      this.destroyed = true;
    }
  }

  public GSSName getName()
    throws GSSException
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    return GSSNameImpl.wrapElement(this.gssManager, this.tempCred.getName());
  }

  public GSSName getName(Oid paramOid)
    throws GSSException
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    SearchKey localSearchKey = null;
    GSSCredentialSpi localGSSCredentialSpi = null;
    if (paramOid == null)
      paramOid = ProviderList.DEFAULT_MECH_OID;
    localSearchKey = new SearchKey(paramOid, 1);
    localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    if (localGSSCredentialSpi == null)
    {
      localSearchKey = new SearchKey(paramOid, 2);
      localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    }
    if (localGSSCredentialSpi == null)
    {
      localSearchKey = new SearchKey(paramOid, 0);
      localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    }
    if (localGSSCredentialSpi == null)
      throw new GSSExceptionImpl(2, paramOid);
    return GSSNameImpl.wrapElement(this.gssManager, localGSSCredentialSpi.getName());
  }

  public int getRemainingLifetime()
    throws GSSException
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 2147483647;
    Enumeration localEnumeration = this.hashtable.keys();
    while (true)
    {
      do
      {
        if (!(localEnumeration.hasMoreElements()))
          break label150;
        SearchKey localSearchKey = (SearchKey)localEnumeration.nextElement();
        GSSCredentialSpi localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
        if (localSearchKey.getUsage() == 1)
        {
          i = localGSSCredentialSpi.getInitLifetime();
        }
        else if (localSearchKey.getUsage() == 2)
        {
          i = localGSSCredentialSpi.getAcceptLifetime();
        }
        else
        {
          j = localGSSCredentialSpi.getInitLifetime();
          k = localGSSCredentialSpi.getAcceptLifetime();
          i = (j < k) ? j : k;
        }
      }
      while (l <= i);
      l = i;
    }
    label150: return l;
  }

  public int getRemainingInitLifetime(Oid paramOid)
    throws GSSException
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    GSSCredentialSpi localGSSCredentialSpi = null;
    SearchKey localSearchKey = null;
    int i = 0;
    int j = 0;
    if (paramOid == null)
      paramOid = ProviderList.DEFAULT_MECH_OID;
    localSearchKey = new SearchKey(paramOid, 1);
    localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    if (localGSSCredentialSpi != null)
    {
      i = 1;
      if (j < localGSSCredentialSpi.getInitLifetime())
        j = localGSSCredentialSpi.getInitLifetime();
    }
    localSearchKey = new SearchKey(paramOid, 0);
    localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    if (localGSSCredentialSpi != null)
    {
      i = 1;
      if (j < localGSSCredentialSpi.getInitLifetime())
        j = localGSSCredentialSpi.getInitLifetime();
    }
    if (i == 0)
      throw new GSSExceptionImpl(2, paramOid);
    return j;
  }

  public int getRemainingAcceptLifetime(Oid paramOid)
    throws GSSException
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    GSSCredentialSpi localGSSCredentialSpi = null;
    SearchKey localSearchKey = null;
    int i = 0;
    int j = 0;
    if (paramOid == null)
      paramOid = ProviderList.DEFAULT_MECH_OID;
    localSearchKey = new SearchKey(paramOid, 2);
    localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    if (localGSSCredentialSpi != null)
    {
      i = 1;
      if (j < localGSSCredentialSpi.getAcceptLifetime())
        j = localGSSCredentialSpi.getAcceptLifetime();
    }
    localSearchKey = new SearchKey(paramOid, 0);
    localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    if (localGSSCredentialSpi != null)
    {
      i = 1;
      if (j < localGSSCredentialSpi.getAcceptLifetime())
        j = localGSSCredentialSpi.getAcceptLifetime();
    }
    if (i == 0)
      throw new GSSExceptionImpl(2, paramOid);
    return j;
  }

  public int getUsage()
    throws GSSException
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    int i = 0;
    int j = 0;
    Enumeration localEnumeration = this.hashtable.keys();
    while (true)
    {
      SearchKey localSearchKey;
      while (true)
      {
        if (!(localEnumeration.hasMoreElements()))
          break label79;
        localSearchKey = (SearchKey)localEnumeration.nextElement();
        if (localSearchKey.getUsage() != 1)
          break;
        i = 1;
      }
      if (localSearchKey.getUsage() != 2)
        break;
      j = 1;
    }
    return 0;
    if (i != 0)
    {
      if (j != 0)
        label79: return 0;
      return 1;
    }
    return 2;
  }

  public int getUsage(Oid paramOid)
    throws GSSException
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    GSSCredentialSpi localGSSCredentialSpi = null;
    SearchKey localSearchKey = null;
    int i = 0;
    int j = 0;
    if (paramOid == null)
      paramOid = ProviderList.DEFAULT_MECH_OID;
    localSearchKey = new SearchKey(paramOid, 1);
    localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    if (localGSSCredentialSpi != null)
      i = 1;
    localSearchKey = new SearchKey(paramOid, 2);
    localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    if (localGSSCredentialSpi != null)
      j = 1;
    localSearchKey = new SearchKey(paramOid, 0);
    localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
    if (localGSSCredentialSpi != null)
    {
      i = 1;
      j = 1;
    }
    if ((i != 0) && (j != 0))
      return 0;
    if (i != 0)
      return 1;
    if (j != 0)
      return 2;
    throw new GSSExceptionImpl(2, paramOid);
  }

  public Oid[] getMechs()
    throws GSSException
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    Vector localVector = new Vector(this.hashtable.size());
    Enumeration localEnumeration = this.hashtable.keys();
    while (localEnumeration.hasMoreElements())
    {
      SearchKey localSearchKey = (SearchKey)localEnumeration.nextElement();
      localVector.addElement(localSearchKey.getMech());
    }
    return ((Oid[])(Oid[])localVector.toArray(new Oid[0]));
  }

  public void add(GSSName paramGSSName, int paramInt1, int paramInt2, Oid paramOid, int paramInt3)
    throws GSSException
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    if (paramOid == null)
      paramOid = ProviderList.DEFAULT_MECH_OID;
    SearchKey localSearchKey = new SearchKey(paramOid, paramInt3);
    if (this.hashtable.containsKey(localSearchKey))
      throw new GSSExceptionImpl(17, "Duplicate element found: " + getElementStr(paramOid, paramInt3));
    GSSNameSpi localGSSNameSpi = (paramGSSName == null) ? null : ((GSSNameImpl)paramGSSName).getElement(paramOid);
    this.tempCred = this.gssManager.getCredentialElement(localGSSNameSpi, paramInt1, paramInt2, paramOid, paramInt3);
    if (this.tempCred != null)
      if ((paramInt3 == 0) && (((!(this.tempCred.isAcceptorCredential())) || (!(this.tempCred.isInitiatorCredential())))))
      {
        int i;
        int j;
        if (!(this.tempCred.isInitiatorCredential()))
        {
          i = 2;
          j = 1;
        }
        else
        {
          i = 1;
          j = 2;
        }
        localSearchKey = new SearchKey(paramOid, i);
        this.hashtable.put(localSearchKey, this.tempCred);
        this.tempCred = this.gssManager.getCredentialElement(localGSSNameSpi, paramInt1, paramInt2, paramOid, j);
        localSearchKey = new SearchKey(paramOid, j);
        this.hashtable.put(localSearchKey, this.tempCred);
      }
      else
      {
        this.hashtable.put(localSearchKey, this.tempCred);
      }
  }

  public boolean equals(Object paramObject)
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    if (this == paramObject)
      return true;
    return (paramObject instanceof GSSCredentialImpl);
  }

  public int hashCode()
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    return 1;
  }

  public GSSCredentialSpi getElement(Oid paramOid, boolean paramBoolean)
    throws GSSException
  {
    SearchKey localSearchKey;
    GSSCredentialSpi localGSSCredentialSpi;
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    if (paramOid == null)
    {
      paramOid = ProviderList.DEFAULT_MECH_OID;
      localSearchKey = new SearchKey(paramOid, 2);
      localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
      if (localGSSCredentialSpi == null)
      {
        localSearchKey = new SearchKey(paramOid, 0);
        localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
        if (localGSSCredentialSpi == null)
        {
          Object[] arrayOfObject = this.hashtable.entrySet().toArray();
          for (int i = 0; i < arrayOfObject.length; ++i)
          {
            localGSSCredentialSpi = (GSSCredentialSpi)((Map.Entry)arrayOfObject[i]).getValue();
            if (localGSSCredentialSpi.isInitiatorCredential() == paramBoolean)
              break;
          }
        }
      }
    }
    else
    {
      if (paramBoolean)
        localSearchKey = new SearchKey(paramOid, 1);
      else
        localSearchKey = new SearchKey(paramOid, 2);
      localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
      if (localGSSCredentialSpi == null)
      {
        localSearchKey = new SearchKey(paramOid, 0);
        localGSSCredentialSpi = (GSSCredentialSpi)this.hashtable.get(localSearchKey);
      }
    }
    if (localGSSCredentialSpi == null)
      throw new GSSExceptionImpl(13, "No credential found for: " + paramOid + getElementStr(paramOid, 2));
    return localGSSCredentialSpi;
  }

  Set getElements()
  {
    HashSet localHashSet = new HashSet(this.hashtable.size());
    Enumeration localEnumeration = this.hashtable.elements();
    while (localEnumeration.hasMoreElements())
    {
      Object localObject = localEnumeration.nextElement();
      localHashSet.add(localObject);
    }
    return localHashSet;
  }

  private static String getElementStr(Oid paramOid, int paramInt)
  {
    String str = paramOid.toString();
    if (paramInt == 1)
      str = str.concat(" usage: Initiate");
    else if (paramInt == 2)
      str = str.concat(" usage: Accept");
    else
      str = str.concat(" usage: Initiate and Accept");
    return str;
  }

  public String toString()
  {
    if (this.destroyed)
      throw new IllegalStateException("This credential is no longer valid");
    GSSCredentialSpi localGSSCredentialSpi = null;
    StringBuffer localStringBuffer = new StringBuffer("[GSSCredential: ");
    Object[] arrayOfObject = this.hashtable.entrySet().toArray();
    for (int i = 0; i < arrayOfObject.length; ++i)
      try
      {
        localStringBuffer.append('\n');
        localGSSCredentialSpi = (GSSCredentialSpi)((Map.Entry)arrayOfObject[i]).getValue();
        localStringBuffer.append(localGSSCredentialSpi.getName());
        localStringBuffer.append(' ');
        localStringBuffer.append(localGSSCredentialSpi.getMechanism());
        localStringBuffer.append((localGSSCredentialSpi.isInitiatorCredential()) ? " Initiate" : "");
        localStringBuffer.append((localGSSCredentialSpi.isAcceptorCredential()) ? " Accept" : "");
        localStringBuffer.append(" [");
        localStringBuffer.append(localGSSCredentialSpi.toString());
        localStringBuffer.append(']');
      }
      catch (GSSException localGSSException)
      {
      }
    localStringBuffer.append(']');
    return localStringBuffer.toString();
  }

  static class SearchKey
  {
    private Oid mechOid = null;
    private int usage = 0;

    public SearchKey(Oid paramOid, int paramInt)
    {
      this.mechOid = paramOid;
      this.usage = paramInt;
    }

    public Oid getMech()
    {
      return this.mechOid;
    }

    public int getUsage()
    {
      return this.usage;
    }

    public boolean equals(Object paramObject)
    {
      if (!(paramObject instanceof SearchKey))
        return false;
      SearchKey localSearchKey = (SearchKey)paramObject;
      return ((this.mechOid.equals(localSearchKey.mechOid)) && (this.usage == localSearchKey.usage));
    }

    public int hashCode()
    {
      return this.mechOid.hashCode();
    }
  }
}
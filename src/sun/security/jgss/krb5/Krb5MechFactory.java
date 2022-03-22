package sun.security.jgss.krb5;

import java.io.PrintStream;
import java.security.Provider;
import java.util.Vector;
import javax.security.auth.kerberos.ServicePermission;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.GSSUtil;
import sun.security.jgss.SunProvider;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.jgss.spi.MechanismFactory;
import sun.security.krb5.PrincipalName;

public final class Krb5MechFactory
  implements MechanismFactory
{
  private static final boolean DEBUG = Krb5Util.DEBUG;
  static final Provider PROVIDER = new SunProvider();
  static final Oid GSS_KRB5_MECH_OID = createOid("1.2.840.113554.1.2.2");
  static final Oid NT_GSS_KRB5_PRINCIPAL = createOid("1.2.840.113554.1.2.2.1");
  private static Oid[] nameTypes = { GSSName.NT_USER_NAME, GSSName.NT_HOSTBASED_SERVICE, GSSName.NT_EXPORT_NAME, NT_GSS_KRB5_PRINCIPAL };
  private final int caller;

  private static Krb5CredElement getCredFromSubject(GSSNameSpi paramGSSNameSpi, boolean paramBoolean)
    throws GSSException
  {
    Vector localVector = GSSUtil.searchSubject(paramGSSNameSpi, GSS_KRB5_MECH_OID, paramBoolean, (paramBoolean) ? Krb5InitCredential.class : Krb5AcceptCredential.class);
    Krb5CredElement localKrb5CredElement = ((localVector == null) || (localVector.isEmpty())) ? null : (Krb5CredElement)localVector.firstElement();
    if (localKrb5CredElement != null)
      if (paramBoolean)
        checkInitCredPermission((Krb5NameElement)localKrb5CredElement.getName());
      else
        checkAcceptCredPermission((Krb5NameElement)localKrb5CredElement.getName(), paramGSSNameSpi);
    return localKrb5CredElement;
  }

  public Krb5MechFactory(int paramInt)
  {
    this.caller = paramInt;
  }

  public GSSNameSpi getNameElement(String paramString, Oid paramOid)
    throws GSSException
  {
    return Krb5NameElement.getInstance(paramString, paramOid);
  }

  public GSSNameSpi getNameElement(byte[] paramArrayOfByte, Oid paramOid)
    throws GSSException
  {
    return Krb5NameElement.getInstance(new String(paramArrayOfByte), paramOid);
  }

  public GSSCredentialSpi getCredentialElement(GSSNameSpi paramGSSNameSpi, int paramInt1, int paramInt2, int paramInt3)
    throws GSSException
  {
    if ((paramGSSNameSpi != null) && (!(paramGSSNameSpi instanceof Krb5NameElement)))
      paramGSSNameSpi = Krb5NameElement.getInstance(paramGSSNameSpi.toString(), paramGSSNameSpi.getStringNameType());
    Object localObject = getCredFromSubject(paramGSSNameSpi, paramInt3 != 2);
    if (localObject == null)
      if ((paramInt3 == 1) || (paramInt3 == 0))
      {
        localObject = Krb5InitCredential.getInstance(this.caller, (Krb5NameElement)paramGSSNameSpi, paramInt1);
        checkInitCredPermission((Krb5NameElement)((Krb5CredElement)localObject).getName());
      }
      else if (paramInt3 == 2)
      {
        localObject = Krb5AcceptCredential.getInstance(this.caller, (Krb5NameElement)paramGSSNameSpi);
        checkAcceptCredPermission((Krb5NameElement)((Krb5CredElement)localObject).getName(), paramGSSNameSpi);
      }
      else
      {
        throw new GSSException(11, -1, "Unknown usage mode requested");
      }
    return ((GSSCredentialSpi)localObject);
  }

  public static void checkInitCredPermission(Krb5NameElement paramKrb5NameElement)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      String str1 = paramKrb5NameElement.getKrb5PrincipalName().getRealmAsString();
      String str2 = new String("krbtgt/" + str1 + '@' + str1);
      ServicePermission localServicePermission = new ServicePermission(str2, "initiate");
      try
      {
        localSecurityManager.checkPermission(localServicePermission);
      }
      catch (SecurityException localSecurityException)
      {
        if (DEBUG)
          System.out.println("Permission to initiatekerberos init credential" + localSecurityException.getMessage());
        throw localSecurityException;
      }
    }
  }

  public static void checkAcceptCredPermission(Krb5NameElement paramKrb5NameElement, GSSNameSpi paramGSSNameSpi)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      ServicePermission localServicePermission = new ServicePermission(paramKrb5NameElement.getKrb5PrincipalName().getName(), "accept");
      try
      {
        localSecurityManager.checkPermission(localServicePermission);
      }
      catch (SecurityException localSecurityException2)
      {
        SecurityException localSecurityException2;
        if (paramGSSNameSpi == null)
          localSecurityException2 = new SecurityException("No permission to acquire Kerberos accept credential");
        throw localSecurityException2;
      }
    }
  }

  public GSSContextSpi getMechanismContext(GSSNameSpi paramGSSNameSpi, GSSCredentialSpi paramGSSCredentialSpi, int paramInt)
    throws GSSException
  {
    if ((paramGSSNameSpi != null) && (!(paramGSSNameSpi instanceof Krb5NameElement)))
      paramGSSNameSpi = Krb5NameElement.getInstance(paramGSSNameSpi.toString(), paramGSSNameSpi.getStringNameType());
    if (paramGSSCredentialSpi == null)
      paramGSSCredentialSpi = getCredentialElement(null, paramInt, 0, 1);
    return new Krb5Context(this.caller, (Krb5NameElement)paramGSSNameSpi, (Krb5CredElement)paramGSSCredentialSpi, paramInt);
  }

  public GSSContextSpi getMechanismContext(GSSCredentialSpi paramGSSCredentialSpi)
    throws GSSException
  {
    if (paramGSSCredentialSpi == null)
      paramGSSCredentialSpi = getCredentialElement(null, 0, 2147483647, 2);
    return new Krb5Context(this.caller, (Krb5CredElement)paramGSSCredentialSpi);
  }

  public GSSContextSpi getMechanismContext(byte[] paramArrayOfByte)
    throws GSSException
  {
    return new Krb5Context(this.caller, paramArrayOfByte);
  }

  public final Oid getMechanismOid()
  {
    return GSS_KRB5_MECH_OID;
  }

  public Provider getProvider()
  {
    return PROVIDER;
  }

  public Oid[] getNameTypes()
  {
    return nameTypes;
  }

  private static Oid createOid(String paramString)
  {
    Oid localOid = null;
    try
    {
      localOid = new Oid(paramString);
    }
    catch (GSSException localGSSException)
    {
    }
    return localOid;
  }
}
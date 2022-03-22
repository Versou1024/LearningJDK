package sun.security.jgss.krb5;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;

public class Krb5AcceptCredential extends KerberosKey
  implements Krb5CredElement
{
  private static final long serialVersionUID = 7714332137352567952L;
  private Krb5NameElement name;
  private EncryptionKey[] krb5EncryptionKeys;

  private Krb5AcceptCredential(Krb5NameElement paramKrb5NameElement, KerberosKey[] paramArrayOfKerberosKey)
  {
    super(paramArrayOfKerberosKey[0].getPrincipal(), paramArrayOfKerberosKey[0].getEncoded(), paramArrayOfKerberosKey[0].getKeyType(), paramArrayOfKerberosKey[0].getVersionNumber());
    this.name = paramKrb5NameElement;
    this.krb5EncryptionKeys = new EncryptionKey[paramArrayOfKerberosKey.length];
    for (int i = 0; i < paramArrayOfKerberosKey.length; ++i)
      this.krb5EncryptionKeys[i] = new EncryptionKey(paramArrayOfKerberosKey[i].getEncoded(), paramArrayOfKerberosKey[i].getKeyType(), new Integer(paramArrayOfKerberosKey[i].getVersionNumber()));
  }

  static Krb5AcceptCredential getInstance(int paramInt, Krb5NameElement paramKrb5NameElement)
    throws GSSException
  {
    KerberosKey[] arrayOfKerberosKey;
    String str1 = (paramKrb5NameElement == null) ? null : paramKrb5NameElement.getKrb5PrincipalName().getName();
    AccessControlContext localAccessControlContext = AccessController.getContext();
    try
    {
      arrayOfKerberosKey = (KerberosKey[])(KerberosKey[])AccessController.doPrivileged(new PrivilegedExceptionAction(paramInt, str1, localAccessControlContext)
      {
        public Object run()
          throws Exception
        {
          return Krb5Util.getKeys((this.val$caller == -1) ? 2 : this.val$caller, this.val$serverPrinc, this.val$acc);
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      GSSException localGSSException = new GSSException(13, -1, "Attempt to obtain new ACCEPT credentials failed!");
      localGSSException.initCause(localPrivilegedActionException.getException());
      throw localGSSException;
    }
    if ((arrayOfKerberosKey == null) || (arrayOfKerberosKey.length == 0))
      throw new GSSException(13, -1, "Failed to find any Kerberos Key");
    if (paramKrb5NameElement == null)
    {
      String str2 = arrayOfKerberosKey[0].getPrincipal().getName();
      paramKrb5NameElement = Krb5NameElement.getInstance(str2, Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL);
    }
    return new Krb5AcceptCredential(paramKrb5NameElement, arrayOfKerberosKey);
  }

  public final GSSNameSpi getName()
    throws GSSException
  {
    return this.name;
  }

  public int getInitLifetime()
    throws GSSException
  {
    return 0;
  }

  public int getAcceptLifetime()
    throws GSSException
  {
    return 2147483647;
  }

  public boolean isInitiatorCredential()
    throws GSSException
  {
    return false;
  }

  public boolean isAcceptorCredential()
    throws GSSException
  {
    return true;
  }

  public final Oid getMechanism()
  {
    return Krb5MechFactory.GSS_KRB5_MECH_OID;
  }

  public final Provider getProvider()
  {
    return Krb5MechFactory.PROVIDER;
  }

  EncryptionKey[] getKrb5EncryptionKeys()
  {
    return this.krb5EncryptionKeys;
  }

  public void dispose()
    throws GSSException
  {
    try
    {
      destroy();
    }
    catch (DestroyFailedException localDestroyFailedException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not destroy credentials - " + localDestroyFailedException.getMessage());
      localGSSException.initCause(localDestroyFailedException);
    }
  }

  public void destroy()
    throws DestroyFailedException
  {
    if (this.krb5EncryptionKeys != null)
    {
      for (int i = 0; i < this.krb5EncryptionKeys.length; ++i)
        this.krb5EncryptionKeys[i].destroy();
      this.krb5EncryptionKeys = null;
    }
    super.destroy();
  }
}
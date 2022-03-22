package sun.security.jgss.krb5;

import java.io.IOException;
import java.net.InetAddress;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.krb5.Config;
import sun.security.krb5.Credentials;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;

public class Krb5InitCredential extends KerberosTicket
  implements Krb5CredElement
{
  private static final long serialVersionUID = 7723415700837898232L;
  private Krb5NameElement name;
  private Credentials krb5Credentials;

  private Krb5InitCredential(Krb5NameElement paramKrb5NameElement, byte[] paramArrayOfByte1, KerberosPrincipal paramKerberosPrincipal1, KerberosPrincipal paramKerberosPrincipal2, byte[] paramArrayOfByte2, int paramInt, boolean[] paramArrayOfBoolean, Date paramDate1, Date paramDate2, Date paramDate3, Date paramDate4, InetAddress[] paramArrayOfInetAddress)
    throws GSSException
  {
    super(paramArrayOfByte1, paramKerberosPrincipal1, paramKerberosPrincipal2, paramArrayOfByte2, paramInt, paramArrayOfBoolean, paramDate1, paramDate2, paramDate3, paramDate4, paramArrayOfInetAddress);
    this.name = paramKrb5NameElement;
    try
    {
      this.krb5Credentials = new Credentials(paramArrayOfByte1, paramKerberosPrincipal1.getName(), paramKerberosPrincipal2.getName(), paramArrayOfByte2, paramInt, paramArrayOfBoolean, paramDate1, paramDate2, paramDate3, paramDate4, paramArrayOfInetAddress);
    }
    catch (KrbException localKrbException)
    {
      throw new GSSException(13, -1, localKrbException.getMessage());
    }
    catch (IOException localIOException)
    {
      throw new GSSException(13, -1, localIOException.getMessage());
    }
  }

  private Krb5InitCredential(Krb5NameElement paramKrb5NameElement, Credentials paramCredentials, byte[] paramArrayOfByte1, KerberosPrincipal paramKerberosPrincipal1, KerberosPrincipal paramKerberosPrincipal2, byte[] paramArrayOfByte2, int paramInt, boolean[] paramArrayOfBoolean, Date paramDate1, Date paramDate2, Date paramDate3, Date paramDate4, InetAddress[] paramArrayOfInetAddress)
    throws GSSException
  {
    super(paramArrayOfByte1, paramKerberosPrincipal1, paramKerberosPrincipal2, paramArrayOfByte2, paramInt, paramArrayOfBoolean, paramDate1, paramDate2, paramDate3, paramDate4, paramArrayOfInetAddress);
    this.name = paramKrb5NameElement;
    this.krb5Credentials = paramCredentials;
  }

  static Krb5InitCredential getInstance(int paramInt1, Krb5NameElement paramKrb5NameElement, int paramInt2)
    throws GSSException
  {
    KerberosTicket localKerberosTicket = getTgt(paramInt1, paramKrb5NameElement, paramInt2);
    if (localKerberosTicket == null)
      throw new GSSException(13, -1, "Failed to find any Kerberos tgt");
    if (paramKrb5NameElement == null)
    {
      String str = localKerberosTicket.getClient().getName();
      paramKrb5NameElement = Krb5NameElement.getInstance(str, Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL);
    }
    return new Krb5InitCredential(paramKrb5NameElement, localKerberosTicket.getEncoded(), localKerberosTicket.getClient(), localKerberosTicket.getServer(), localKerberosTicket.getSessionKey().getEncoded(), localKerberosTicket.getSessionKeyType(), localKerberosTicket.getFlags(), localKerberosTicket.getAuthTime(), localKerberosTicket.getStartTime(), localKerberosTicket.getEndTime(), localKerberosTicket.getRenewTill(), localKerberosTicket.getClientAddresses());
  }

  static Krb5InitCredential getInstance(Krb5NameElement paramKrb5NameElement, Credentials paramCredentials)
    throws GSSException
  {
    EncryptionKey localEncryptionKey = paramCredentials.getSessionKey();
    PrincipalName localPrincipalName1 = paramCredentials.getClient();
    PrincipalName localPrincipalName2 = paramCredentials.getServer();
    KerberosPrincipal localKerberosPrincipal1 = null;
    KerberosPrincipal localKerberosPrincipal2 = null;
    Krb5NameElement localKrb5NameElement = null;
    if (localPrincipalName1 != null)
    {
      String str = localPrincipalName1.getName();
      localKrb5NameElement = Krb5NameElement.getInstance(str, Krb5MechFactory.NT_GSS_KRB5_PRINCIPAL);
      localKerberosPrincipal1 = new KerberosPrincipal(str);
    }
    if (localPrincipalName2 != null)
      localKerberosPrincipal2 = new KerberosPrincipal(localPrincipalName2.getName());
    return new Krb5InitCredential(localKrb5NameElement, paramCredentials, paramCredentials.getEncoded(), localKerberosPrincipal1, localKerberosPrincipal2, localEncryptionKey.getBytes(), localEncryptionKey.getEType(), paramCredentials.getFlags(), paramCredentials.getAuthTime(), paramCredentials.getStartTime(), paramCredentials.getEndTime(), paramCredentials.getRenewTill(), paramCredentials.getClientAddresses());
  }

  public final GSSNameSpi getName()
    throws GSSException
  {
    return this.name;
  }

  public int getInitLifetime()
    throws GSSException
  {
    int i = 0;
    i = (int)(getEndTime().getTime() - new Date().getTime());
    return i;
  }

  public int getAcceptLifetime()
    throws GSSException
  {
    return 0;
  }

  public boolean isInitiatorCredential()
    throws GSSException
  {
    return true;
  }

  public boolean isAcceptorCredential()
    throws GSSException
  {
    return false;
  }

  public final Oid getMechanism()
  {
    return Krb5MechFactory.GSS_KRB5_MECH_OID;
  }

  public final Provider getProvider()
  {
    return Krb5MechFactory.PROVIDER;
  }

  Credentials getKrb5Credentials()
  {
    return this.krb5Credentials;
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

  private static KerberosTicket getTgt(int paramInt1, Krb5NameElement paramKrb5NameElement, int paramInt2)
    throws GSSException
  {
    String str2;
    String str1 = null;
    String str3 = null;
    if (paramKrb5NameElement != null)
    {
      str2 = paramKrb5NameElement.getKrb5PrincipalName().getName();
      str1 = paramKrb5NameElement.getKrb5PrincipalName().getRealmAsString();
    }
    else
    {
      str2 = null;
      try
      {
        Config localConfig = Config.getInstance();
        str1 = localConfig.getDefaultRealm();
      }
      catch (KrbException localKrbException)
      {
        GSSException localGSSException1 = new GSSException(13, -1, "Attempt to obtain INITIATE credentials failed! (" + localKrbException.getMessage() + ")");
        localGSSException1.initCause(localKrbException);
        throw localGSSException1;
      }
    }
    AccessControlContext localAccessControlContext = AccessController.getContext();
    try
    {
      int i = (paramInt1 == -1) ? 1 : paramInt1;
      return ((KerberosTicket)AccessController.doPrivileged(new PrivilegedExceptionAction(i, str2, str3, localAccessControlContext)
      {
        public Object run()
          throws Exception
        {
          return Krb5Util.getTicket(this.val$realCaller, this.val$clientPrincipal, this.val$tgsPrincipal, this.val$acc);
        }
      }));
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      GSSException localGSSException2 = new GSSException(13, -1, "Attempt to obtain new INITIATE credentials failed! (" + localPrivilegedActionException.getMessage() + ")");
      localGSSException2.initCause(localPrivilegedActionException.getException());
      throw localGSSException2;
    }
  }
}
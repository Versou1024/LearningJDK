package sun.security.jgss.krb5;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.security.auth.kerberos.DelegationPermission;
import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSException;
import sun.security.jgss.GSSToken;
import sun.security.krb5.Checksum;
import sun.security.krb5.Credentials;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbCred;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;

abstract class InitialToken extends Krb5Token
{
  private static final int CHECKSUM_TYPE = 32771;
  private static final int CHECKSUM_LENGTH_SIZE = 4;
  private static final int CHECKSUM_BINDINGS_SIZE = 16;
  private static final int CHECKSUM_FLAGS_SIZE = 4;
  private static final int CHECKSUM_DELEG_OPT_SIZE = 2;
  private static final int CHECKSUM_DELEG_LGTH_SIZE = 2;
  private static final int CHECKSUM_DELEG_FLAG = 1;
  private static final int CHECKSUM_MUTUAL_FLAG = 2;
  private static final int CHECKSUM_REPLAY_FLAG = 4;
  private static final int CHECKSUM_SEQUENCE_FLAG = 8;
  private static final int CHECKSUM_CONF_FLAG = 16;
  private static final int CHECKSUM_INTEG_FLAG = 32;
  private final byte[] CHECKSUM_FIRST_BYTES = { 16, 0, 0, 0 };
  private static final int CHANNEL_BINDING_AF_INET = 2;
  private static final int CHANNEL_BINDING_AF_INET6 = 24;
  private static final int CHANNEL_BINDING_AF_NULL_ADDR = 255;
  private static final int Inet4_ADDRSZ = 4;
  private static final int Inet6_ADDRSZ = 16;

  private int getAddrType(InetAddress paramInetAddress)
  {
    int i = 255;
    if (paramInetAddress instanceof Inet4Address)
      i = 2;
    else if (paramInetAddress instanceof Inet6Address)
      i = 24;
    return i;
  }

  private byte[] getAddrBytes(InetAddress paramInetAddress)
    throws GSSException
  {
    int i = getAddrType(paramInetAddress);
    byte[] arrayOfByte = paramInetAddress.getAddress();
    if (arrayOfByte != null)
    {
      switch (i)
      {
      case 2:
        if (arrayOfByte.length != 4)
          throw new GSSException(11, -1, "Incorrect AF-INET address length in ChannelBinding.");
        return arrayOfByte;
      case 24:
        if (arrayOfByte.length != 16)
          throw new GSSException(11, -1, "Incorrect AF-INET6 address length in ChannelBinding.");
        return arrayOfByte;
      }
      throw new GSSException(11, -1, "Cannot handle non AF-INET addresses in ChannelBinding.");
    }
    return null;
  }

  private byte[] computeChannelBinding(ChannelBinding paramChannelBinding)
    throws GSSException
  {
    InetAddress localInetAddress1 = paramChannelBinding.getInitiatorAddress();
    InetAddress localInetAddress2 = paramChannelBinding.getAcceptorAddress();
    int i = 20;
    int j = getAddrType(localInetAddress1);
    int k = getAddrType(localInetAddress2);
    byte[] arrayOfByte1 = null;
    if (localInetAddress1 != null)
    {
      arrayOfByte1 = getAddrBytes(localInetAddress1);
      i += arrayOfByte1.length;
    }
    byte[] arrayOfByte2 = null;
    if (localInetAddress2 != null)
    {
      arrayOfByte2 = getAddrBytes(localInetAddress2);
      i += arrayOfByte2.length;
    }
    byte[] arrayOfByte3 = paramChannelBinding.getApplicationData();
    if (arrayOfByte3 != null)
      i += arrayOfByte3.length;
    byte[] arrayOfByte4 = new byte[i];
    int l = 0;
    writeLittleEndian(j, arrayOfByte4, l);
    l += 4;
    if (arrayOfByte1 != null)
    {
      writeLittleEndian(arrayOfByte1.length, arrayOfByte4, l);
      System.arraycopy(arrayOfByte1, 0, arrayOfByte4, l += 4, arrayOfByte1.length);
      l += arrayOfByte1.length;
    }
    writeLittleEndian(k, arrayOfByte4, l += 4);
    l += 4;
    if (arrayOfByte2 != null)
    {
      writeLittleEndian(arrayOfByte2.length, arrayOfByte4, l);
      System.arraycopy(arrayOfByte2, 0, arrayOfByte4, l += 4, arrayOfByte2.length);
      l += arrayOfByte2.length;
    }
    else
    {
      l += 4;
    }
    if (arrayOfByte3 != null)
    {
      writeLittleEndian(arrayOfByte3.length, arrayOfByte4, l);
      System.arraycopy(arrayOfByte3, 0, arrayOfByte4, l += 4, arrayOfByte3.length);
      l += arrayOfByte3.length;
    }
    else
    {
      l += 4;
    }
    try
    {
      MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
      return localMessageDigest.digest(arrayOfByte4);
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new GSSException(11, -1, "Could not get MD5 Message Digest - " + localNoSuchAlgorithmException.getMessage());
    }
  }

  public abstract byte[] encode()
    throws IOException;

  protected class OverloadedChecksum
  {
    private byte[] checksumBytes = null;
    private Credentials delegCreds = null;
    private int flags = 0;

    public OverloadedChecksum(, Krb5Context paramKrb5Context, Credentials paramCredentials1, Credentials paramCredentials2)
      throws KrbException, IOException, GSSException
    {
      byte[] arrayOfByte = null;
      int i = 0;
      int j = 24;
      if (paramKrb5Context.getCredDelegState())
        if ((paramKrb5Context.getCaller() == 5) && (paramCredentials2.getFlags()[13] == 0))
        {
          paramKrb5Context.setCredDelegState(false);
        }
        else if (!(paramCredentials1.isForwardable()))
        {
          paramKrb5Context.setCredDelegState(false);
        }
        else
        {
          localObject1 = null;
          localObject2 = paramKrb5Context.getCipherHelper(paramCredentials2.getSessionKey());
          if (useNullKey((CipherHelper)localObject2))
            localObject1 = new KrbCred(paramCredentials1, paramCredentials2, EncryptionKey.NULL_KEY);
          else
            localObject1 = new KrbCred(paramCredentials1, paramCredentials2, paramCredentials2.getSessionKey());
          arrayOfByte = ((KrbCred)localObject1).getMessage();
          j += 4 + arrayOfByte.length;
        }
      this.checksumBytes = new byte[j];
      this.checksumBytes[(i++)] = InitialToken.access$000(paramInitialToken)[0];
      this.checksumBytes[(i++)] = InitialToken.access$000(paramInitialToken)[1];
      this.checksumBytes[(i++)] = InitialToken.access$000(paramInitialToken)[2];
      this.checksumBytes[(i++)] = InitialToken.access$000(paramInitialToken)[3];
      Object localObject1 = paramKrb5Context.getChannelBinding();
      if (localObject1 != null)
      {
        localObject2 = InitialToken.access$100(paramInitialToken, paramKrb5Context.getChannelBinding());
        System.arraycopy(localObject2, 0, this.checksumBytes, i, localObject2.length);
      }
      i += 16;
      if (paramKrb5Context.getCredDelegState())
        this.flags |= 1;
      if (paramKrb5Context.getMutualAuthState())
        this.flags |= 2;
      if (paramKrb5Context.getReplayDetState())
        this.flags |= 4;
      if (paramKrb5Context.getSequenceDetState())
        this.flags |= 8;
      if (paramKrb5Context.getIntegState())
        this.flags |= 32;
      if (paramKrb5Context.getConfState())
        this.flags |= 16;
      Object localObject2 = new byte[4];
      GSSToken.writeLittleEndian(this.flags, localObject2);
      this.checksumBytes[(i++)] = localObject2[0];
      this.checksumBytes[(i++)] = localObject2[1];
      this.checksumBytes[(i++)] = localObject2[2];
      this.checksumBytes[(i++)] = localObject2[3];
      if (paramKrb5Context.getCredDelegState())
      {
        PrincipalName localPrincipalName = paramCredentials2.getServer();
        StringBuffer localStringBuffer = new StringBuffer("\"");
        localStringBuffer.append(localPrincipalName.getName()).append('"');
        String str = localPrincipalName.getRealmAsString();
        localStringBuffer.append(" \"krbtgt/").append(str).append('@');
        localStringBuffer.append(str).append('"');
        SecurityManager localSecurityManager = System.getSecurityManager();
        if (localSecurityManager != null)
        {
          DelegationPermission localDelegationPermission = new DelegationPermission(localStringBuffer.toString());
          localSecurityManager.checkPermission(localDelegationPermission);
        }
        this.checksumBytes[(i++)] = 1;
        this.checksumBytes[(i++)] = 0;
        if (arrayOfByte.length > 65535)
          throw new GSSException(11, -1, "Incorrect messsage length");
        GSSToken.writeLittleEndian(arrayOfByte.length, localObject2);
        this.checksumBytes[(i++)] = localObject2[0];
        this.checksumBytes[(i++)] = localObject2[1];
        System.arraycopy(arrayOfByte, 0, this.checksumBytes, i, arrayOfByte.length);
      }
    }

    public OverloadedChecksum(, Krb5Context paramKrb5Context, Checksum paramChecksum, EncryptionKey paramEncryptionKey)
      throws GSSException, KrbException, IOException
    {
      int i = 0;
      this.checksumBytes = paramChecksum.getBytes();
      if ((this.checksumBytes[0] != InitialToken.access$000(paramInitialToken)[0]) || (this.checksumBytes[1] != InitialToken.access$000(paramInitialToken)[1]) || (this.checksumBytes[2] != InitialToken.access$000(paramInitialToken)[2]) || (this.checksumBytes[3] != InitialToken.access$000(paramInitialToken)[3]))
        throw new GSSException(11, -1, "Incorrect checksum");
      byte[] arrayOfByte1 = new byte[16];
      System.arraycopy(this.checksumBytes, 4, arrayOfByte1, 0, 16);
      byte[] arrayOfByte2 = new byte[16];
      int j = (!(java.util.Arrays.equals(arrayOfByte2, arrayOfByte1))) ? 1 : 0;
      ChannelBinding localChannelBinding = paramKrb5Context.getChannelBinding();
      if ((j != 0) || (localChannelBinding != null))
      {
        k = 0;
        localObject1 = null;
        if ((j != 0) && (localChannelBinding != null))
        {
          localObject2 = InitialToken.access$100(paramInitialToken, localChannelBinding);
          k = (!(java.util.Arrays.equals(localObject2, arrayOfByte1))) ? 1 : 0;
          localObject1 = "Bytes mismatch!";
        }
        else if (localChannelBinding == null)
        {
          localObject1 = "ChannelBinding not provided!";
          k = 1;
        }
        else
        {
          localObject1 = "Token missing ChannelBinding!";
          k = 1;
        }
        if (k != 0)
          throw new GSSException(1, -1, (String)localObject1);
      }
      this.flags = GSSToken.readLittleEndian(this.checksumBytes, 20, 4);
      if ((this.flags & 0x1) > 0)
      {
        k = GSSToken.readLittleEndian(this.checksumBytes, 26, 2);
        localObject1 = new byte[k];
        System.arraycopy(this.checksumBytes, 28, localObject1, 0, k);
        localObject2 = paramKrb5Context.getCipherHelper(paramEncryptionKey);
        if (useNullKey((CipherHelper)localObject2))
          this.delegCreds = new KrbCred(localObject1, EncryptionKey.NULL_KEY).getDelegatedCreds()[0];
        else
          this.delegCreds = new KrbCred(localObject1, paramEncryptionKey).getDelegatedCreds()[0];
      }
    }

    private boolean useNullKey()
    {
      int i = 1;
      if ((paramCipherHelper.getProto() == 1) || (paramCipherHelper.isArcFour()))
        i = 0;
      return i;
    }

    public Checksum getChecksum()
      throws KrbException
    {
      return new Checksum(this.checksumBytes, 32771);
    }

    public Credentials getDelegatedCreds()
    {
      return this.delegCreds;
    }

    public void setContextFlags()
    {
      if ((this.flags & 0x1) > 0)
        paramKrb5Context.setCredDelegState(true);
      if ((this.flags & 0x2) == 0)
        paramKrb5Context.setMutualAuthState(false);
      if ((this.flags & 0x4) == 0)
        paramKrb5Context.setReplayDetState(false);
      if ((this.flags & 0x8) == 0)
        paramKrb5Context.setSequenceDetState(false);
      if ((this.flags & 0x10) == 0)
        paramKrb5Context.setConfState(false);
      if ((this.flags & 0x20) == 0)
        paramKrb5Context.setIntegState(false);
    }
  }
}
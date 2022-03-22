package sun.security.jgss.krb5;

import B;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import java.util.Set;
import javax.crypto.Cipher;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.kerberos.ServicePermission;
import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;
import sun.misc.HexDumpEncoder;
import sun.security.jgss.GSSUtil;
import sun.security.jgss.TokenTracker;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.krb5.Credentials;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbApReq;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;

class Krb5Context
  implements GSSContextSpi
{
  private static final int STATE_NEW = 1;
  private static final int STATE_IN_PROCESS = 2;
  private static final int STATE_DONE = 3;
  private static final int STATE_DELETED = 4;
  private int state = 1;
  private boolean credDelegState = false;
  private boolean mutualAuthState = true;
  private boolean replayDetState = true;
  private boolean sequenceDetState = true;
  private boolean confState = true;
  private boolean integState = true;
  private int mySeqNumber;
  private int peerSeqNumber;
  private TokenTracker peerTokenTracker;
  private CipherHelper cipherHelper = null;
  private Object mySeqNumberLock = new Object();
  private Object peerSeqNumberLock = new Object();
  private EncryptionKey key;
  private Krb5NameElement myName;
  private Krb5NameElement peerName;
  private int lifetime;
  private boolean initiator;
  private ChannelBinding channelBinding;
  private Krb5CredElement myCred;
  private Krb5CredElement delegatedCred;
  private Cipher desCipher = null;
  private Credentials serviceCreds;
  private KrbApReq apReq;
  private final int caller;
  private static final boolean DEBUG = Krb5Util.DEBUG;

  Krb5Context(int paramInt1, Krb5NameElement paramKrb5NameElement, Krb5CredElement paramKrb5CredElement, int paramInt2)
    throws GSSException
  {
    if (paramKrb5NameElement == null)
      throw new IllegalArgumentException("Cannot have null peer name");
    this.caller = paramInt1;
    this.peerName = paramKrb5NameElement;
    this.myCred = paramKrb5CredElement;
    this.lifetime = paramInt2;
    this.initiator = true;
  }

  Krb5Context(int paramInt, Krb5CredElement paramKrb5CredElement)
    throws GSSException
  {
    this.caller = paramInt;
    this.myCred = paramKrb5CredElement;
    this.initiator = false;
  }

  public Krb5Context(int paramInt, byte[] paramArrayOfByte)
    throws GSSException
  {
    throw new GSSException(16, -1, "GSS Import Context not available");
  }

  public final boolean isTransferable()
    throws GSSException
  {
    return false;
  }

  public final int getLifetime()
  {
    return 2147483647;
  }

  public void requestLifetime(int paramInt)
    throws GSSException
  {
    if ((this.state == 1) && (isInitiator()))
      this.lifetime = paramInt;
  }

  public final void requestConf(boolean paramBoolean)
    throws GSSException
  {
    if ((this.state == 1) && (isInitiator()))
      this.confState = paramBoolean;
  }

  public final boolean getConfState()
  {
    return this.confState;
  }

  public final void requestInteg(boolean paramBoolean)
    throws GSSException
  {
    if ((this.state == 1) && (isInitiator()))
      this.integState = paramBoolean;
  }

  public final boolean getIntegState()
  {
    return this.integState;
  }

  public final void requestCredDeleg(boolean paramBoolean)
    throws GSSException
  {
    if ((this.state == 1) && (isInitiator()))
      this.credDelegState = paramBoolean;
  }

  public final boolean getCredDelegState()
  {
    return this.credDelegState;
  }

  public final void requestMutualAuth(boolean paramBoolean)
    throws GSSException
  {
    if ((this.state == 1) && (isInitiator()))
      this.mutualAuthState = paramBoolean;
  }

  public final boolean getMutualAuthState()
  {
    return this.mutualAuthState;
  }

  public final void requestReplayDet(boolean paramBoolean)
    throws GSSException
  {
    if ((this.state == 1) && (isInitiator()))
      this.replayDetState = paramBoolean;
  }

  public final boolean getReplayDetState()
  {
    return ((this.replayDetState) || (this.sequenceDetState));
  }

  public final void requestSequenceDet(boolean paramBoolean)
    throws GSSException
  {
    if ((this.state == 1) && (isInitiator()))
      this.sequenceDetState = paramBoolean;
  }

  public final boolean getSequenceDetState()
  {
    return ((this.sequenceDetState) || (this.replayDetState));
  }

  public final void requestAnonymity(boolean paramBoolean)
    throws GSSException
  {
  }

  public final boolean getAnonymityState()
  {
    return false;
  }

  final CipherHelper getCipherHelper(EncryptionKey paramEncryptionKey)
    throws GSSException
  {
    EncryptionKey localEncryptionKey = null;
    if (this.cipherHelper == null)
    {
      localEncryptionKey = (getKey() == null) ? paramEncryptionKey : getKey();
      this.cipherHelper = new CipherHelper(localEncryptionKey);
    }
    return this.cipherHelper;
  }

  final int incrementMySequenceNumber()
  {
    int i;
    synchronized (this.mySeqNumberLock)
    {
      i = this.mySeqNumber;
      this.mySeqNumber = (i + 1);
    }
    return i;
  }

  final void resetMySequenceNumber(int paramInt)
  {
    if (DEBUG)
      System.out.println("Krb5Context setting mySeqNumber to: " + paramInt);
    synchronized (this.mySeqNumberLock)
    {
      this.mySeqNumber = paramInt;
    }
  }

  final void resetPeerSequenceNumber(int paramInt)
  {
    if (DEBUG)
      System.out.println("Krb5Context setting peerSeqNumber to: " + paramInt);
    synchronized (this.peerSeqNumberLock)
    {
      this.peerSeqNumber = paramInt;
      this.peerTokenTracker = new TokenTracker(this.peerSeqNumber);
    }
  }

  final void setKey(EncryptionKey paramEncryptionKey)
    throws GSSException
  {
    this.key = paramEncryptionKey;
    this.cipherHelper = new CipherHelper(paramEncryptionKey);
  }

  private final EncryptionKey getKey()
  {
    return this.key;
  }

  final void setDelegCred(Krb5CredElement paramKrb5CredElement)
  {
    this.delegatedCred = paramKrb5CredElement;
  }

  final void setCredDelegState(boolean paramBoolean)
  {
    this.credDelegState = paramBoolean;
  }

  final void setMutualAuthState(boolean paramBoolean)
  {
    this.mutualAuthState = paramBoolean;
  }

  final void setReplayDetState(boolean paramBoolean)
  {
    this.replayDetState = paramBoolean;
  }

  final void setSequenceDetState(boolean paramBoolean)
  {
    this.sequenceDetState = paramBoolean;
  }

  final void setConfState(boolean paramBoolean)
  {
    this.confState = paramBoolean;
  }

  final void setIntegState(boolean paramBoolean)
  {
    this.integState = paramBoolean;
  }

  public final void setChannelBinding(ChannelBinding paramChannelBinding)
    throws GSSException
  {
    this.channelBinding = paramChannelBinding;
  }

  final ChannelBinding getChannelBinding()
  {
    return this.channelBinding;
  }

  public final Oid getMech()
  {
    return Krb5MechFactory.GSS_KRB5_MECH_OID;
  }

  public final GSSNameSpi getSrcName()
    throws GSSException
  {
    return ((isInitiator()) ? this.myName : this.peerName);
  }

  public final GSSNameSpi getTargName()
    throws GSSException
  {
    return ((!(isInitiator())) ? this.myName : this.peerName);
  }

  public final GSSCredentialSpi getDelegCred()
    throws GSSException
  {
    if ((this.state != 2) && (this.state != 3))
      throw new GSSException(12);
    if (this.delegatedCred == null)
      throw new GSSException(13);
    return this.delegatedCred;
  }

  public final boolean isInitiator()
  {
    return this.initiator;
  }

  public final boolean isProtReady()
  {
    return (this.state == 3);
  }

  public final byte[] initSecContext(InputStream paramInputStream, int paramInt)
    throws GSSException
  {
    Object localObject1;
    byte[] arrayOfByte = null;
    InitSecContextToken localInitSecContextToken = null;
    int i = 11;
    if (DEBUG)
      System.out.println("Entered Krb5Context.initSecContext with state=" + printState(this.state));
    if (!(isInitiator()))
      throw new GSSException(11, -1, "initSecContext on an acceptor GSSContext");
    try
    {
      if (this.state == 1)
      {
        Object localObject2;
        this.state = 2;
        i = 13;
        if (this.myCred == null)
          this.myCred = Krb5InitCredential.getInstance(this.caller, this.myName, 0);
        else if (!(this.myCred.isInitiatorCredential()))
          throw new GSSException(i, -1, "No TGT available");
        this.myName = ((Krb5NameElement)this.myCred.getName());
        Credentials localCredentials = ((Krb5InitCredential)this.myCred).getKrb5Credentials();
        checkPermission(this.peerName.getKrb5PrincipalName().getName(), "initiate");
        localObject1 = AccessController.getContext();
        if (GSSUtil.useSubjectCredsOnly())
        {
          localObject2 = null;
          try
          {
            localObject2 = (KerberosTicket)AccessController.doPrivileged(new PrivilegedExceptionAction(this, (AccessControlContext)localObject1)
            {
              public Object run()
                throws Exception
              {
                return Krb5Util.getTicket(-1, Krb5Context.access$000(this.this$0).getKrb5PrincipalName().getName(), Krb5Context.access$100(this.this$0).getKrb5PrincipalName().getName(), this.val$acc);
              }
            });
          }
          catch (PrivilegedActionException localPrivilegedActionException)
          {
            if (DEBUG)
              System.out.println("Attempt to obtain service ticket from the subject failed!");
          }
          if (localObject2 != null)
          {
            if (DEBUG)
              System.out.println("Found service ticket in the subject" + localObject2);
            this.serviceCreds = Krb5Util.ticketToCreds((KerberosTicket)localObject2);
          }
        }
        if (this.serviceCreds == null)
        {
          if (DEBUG)
            System.out.println("Service ticket not found in the subject");
          this.serviceCreds = Credentials.acquireServiceCreds(this.peerName.getKrb5PrincipalName().getName(), localCredentials);
          if (GSSUtil.useSubjectCredsOnly())
          {
            localObject2 = (Subject)AccessController.doPrivileged(new PrivilegedAction(this, (AccessControlContext)localObject1)
            {
              public Object run()
              {
                return Subject.getSubject(this.val$acc);
              }
            });
            if ((localObject2 != null) && (!(((Subject)localObject2).isReadOnly())))
            {
              KerberosTicket localKerberosTicket = Krb5Util.credsToTicket(this.serviceCreds);
              AccessController.doPrivileged(new PrivilegedAction(this, (Subject)localObject2, localKerberosTicket)
              {
                public Object run()
                {
                  this.val$subject.getPrivateCredentials().add(this.val$kt);
                  return null;
                }
              });
            }
            else if (DEBUG)
            {
              System.out.println("Subject is readOnly;Kerberos Service ticket not stored");
            }
          }
        }
        i = 11;
        localInitSecContextToken = new InitSecContextToken(this, localCredentials, this.serviceCreds);
        this.apReq = ((InitSecContextToken)localInitSecContextToken).getKrbApReq();
        arrayOfByte = localInitSecContextToken.encode();
        this.myCred = null;
        if (!(getMutualAuthState()))
          this.state = 3;
        if (DEBUG)
          System.out.println("Created InitSecContextToken:\n" + new HexDumpEncoder().encodeBuffer(arrayOfByte));
      }
      else if (this.state == 2)
      {
        new AcceptSecContextToken(this, this.serviceCreds, this.apReq, paramInputStream);
        this.serviceCreds = null;
        this.apReq = null;
        this.state = 3;
      }
      else if (DEBUG)
      {
        System.out.println(this.state);
      }
    }
    catch (KrbException localKrbException)
    {
      if (DEBUG)
        localKrbException.printStackTrace();
      localObject1 = new GSSException(i, -1, localKrbException.getMessage());
      ((GSSException)localObject1).initCause(localKrbException);
      throw ((Throwable)localObject1);
    }
    catch (IOException localIOException)
    {
      localObject1 = new GSSException(i, -1, localIOException.getMessage());
      ((GSSException)localObject1).initCause(localIOException);
      throw ((Throwable)localObject1);
    }
    return ((B)(B)arrayOfByte);
  }

  public final boolean isEstablished()
  {
    return (this.state == 3);
  }

  public final byte[] acceptSecContext(InputStream paramInputStream, int paramInt)
    throws GSSException
  {
    Object localObject;
    byte[] arrayOfByte = null;
    if (DEBUG)
      System.out.println("Entered Krb5Context.acceptSecContext with state=" + printState(this.state));
    if (isInitiator())
      throw new GSSException(11, -1, "acceptSecContext on an initiator GSSContext");
    try
    {
      if (this.state == 1)
      {
        this.state = 2;
        if (this.myCred == null)
          this.myCred = Krb5AcceptCredential.getInstance(this.caller, this.myName);
        else if (!(this.myCred.isAcceptorCredential()))
          throw new GSSException(13, -1, "No Secret Key available");
        this.myName = ((Krb5NameElement)this.myCred.getName());
        checkPermission(this.myName.getKrb5PrincipalName().getName(), "accept");
        EncryptionKey[] arrayOfEncryptionKey = ((Krb5AcceptCredential)this.myCred).getKrb5EncryptionKeys();
        localObject = new InitSecContextToken(this, arrayOfEncryptionKey, paramInputStream);
        PrincipalName localPrincipalName = ((InitSecContextToken)localObject).getKrbApReq().getClient();
        this.peerName = Krb5NameElement.getInstance(localPrincipalName);
        if (getMutualAuthState())
          arrayOfByte = new AcceptSecContextToken(this, ((InitSecContextToken)localObject).getKrbApReq()).encode();
        this.myCred = null;
        this.state = 3;
      }
      else if (DEBUG)
      {
        System.out.println(this.state);
      }
    }
    catch (KrbException localKrbException)
    {
      localObject = new GSSException(11, -1, localKrbException.getMessage());
      ((GSSException)localObject).initCause(localKrbException);
      throw ((Throwable)localObject);
    }
    catch (IOException localIOException)
    {
      if (DEBUG)
        localIOException.printStackTrace();
      localObject = new GSSException(11, -1, localIOException.getMessage());
      ((GSSException)localObject).initCause(localIOException);
      throw ((Throwable)localObject);
    }
    return ((B)arrayOfByte);
  }

  public final int getWrapSizeLimit(int paramInt1, boolean paramBoolean, int paramInt2)
    throws GSSException
  {
    int i = 0;
    if (this.cipherHelper.getProto() == 0)
      i = WrapToken.getSizeLimit(paramInt1, paramBoolean, paramInt2, getCipherHelper(null));
    else if (this.cipherHelper.getProto() == 1)
      i = WrapToken_v2.getSizeLimit(paramInt1, paramBoolean, paramInt2, getCipherHelper(null));
    return i;
  }

  public final byte[] wrap(byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    if (DEBUG)
      System.out.println("Krb5Context.wrap: data=[" + getHexBytes(paramArrayOfByte, paramInt1, paramInt2) + "]");
    if (this.state != 3)
      throw new GSSException(12, -1, "Wrap called in invalid state!");
    byte[] arrayOfByte = null;
    try
    {
      Object localObject;
      if (this.cipherHelper.getProto() == 0)
      {
        localObject = new WrapToken(this, paramMessageProp, paramArrayOfByte, paramInt1, paramInt2);
        arrayOfByte = ((WrapToken)localObject).encode();
      }
      else if (this.cipherHelper.getProto() == 1)
      {
        localObject = new WrapToken_v2(this, paramMessageProp, paramArrayOfByte, paramInt1, paramInt2);
        arrayOfByte = ((WrapToken_v2)localObject).encode();
      }
      if (DEBUG)
        System.out.println("Krb5Context.wrap: token=[" + getHexBytes(arrayOfByte, 0, arrayOfByte.length) + "]");
      return arrayOfByte;
    }
    catch (IOException localIOException)
    {
      arrayOfByte = null;
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
  }

  public final int wrap(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.state != 3)
      throw new GSSException(12, -1, "Wrap called in invalid state!");
    int i = 0;
    try
    {
      Object localObject;
      if (this.cipherHelper.getProto() == 0)
      {
        localObject = new WrapToken(this, paramMessageProp, paramArrayOfByte1, paramInt1, paramInt2);
        i = ((WrapToken)localObject).encode(paramArrayOfByte2, paramInt3);
      }
      else if (this.cipherHelper.getProto() == 1)
      {
        localObject = new WrapToken_v2(this, paramMessageProp, paramArrayOfByte1, paramInt1, paramInt2);
        i = ((WrapToken_v2)localObject).encode(paramArrayOfByte2, paramInt3);
      }
      if (DEBUG)
        System.out.println("Krb5Context.wrap: token=[" + getHexBytes(paramArrayOfByte2, paramInt3, i) + "]");
      return i;
    }
    catch (IOException localIOException)
    {
      i = 0;
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
  }

  public final void wrap(byte[] paramArrayOfByte, int paramInt1, int paramInt2, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.state != 3)
      throw new GSSException(12, -1, "Wrap called in invalid state!");
    byte[] arrayOfByte = null;
    try
    {
      Object localObject;
      if (this.cipherHelper.getProto() == 0)
      {
        localObject = new WrapToken(this, paramMessageProp, paramArrayOfByte, paramInt1, paramInt2);
        ((WrapToken)localObject).encode(paramOutputStream);
        if (DEBUG)
          arrayOfByte = ((WrapToken)localObject).encode();
      }
      else if (this.cipherHelper.getProto() == 1)
      {
        localObject = new WrapToken_v2(this, paramMessageProp, paramArrayOfByte, paramInt1, paramInt2);
        ((WrapToken_v2)localObject).encode(paramOutputStream);
        if (DEBUG)
          arrayOfByte = ((WrapToken_v2)localObject).encode();
      }
    }
    catch (IOException localIOException)
    {
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
    if (DEBUG)
      System.out.println("Krb5Context.wrap: token=[" + getHexBytes(arrayOfByte, 0, arrayOfByte.length) + "]");
  }

  public final void wrap(InputStream paramInputStream, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    byte[] arrayOfByte;
    try
    {
      arrayOfByte = new byte[paramInputStream.available()];
      paramInputStream.read(arrayOfByte);
    }
    catch (IOException localIOException)
    {
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
    wrap(arrayOfByte, 0, arrayOfByte.length, paramOutputStream, paramMessageProp);
  }

  public final byte[] unwrap(byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    Object localObject;
    if (DEBUG)
      System.out.println("Krb5Context.unwrap: token=[" + getHexBytes(paramArrayOfByte, paramInt1, paramInt2) + "]");
    if (this.state != 3)
      throw new GSSException(12, -1, " Unwrap called in invalid state!");
    byte[] arrayOfByte = null;
    if (this.cipherHelper.getProto() == 0)
    {
      localObject = new WrapToken(this, paramArrayOfByte, paramInt1, paramInt2, paramMessageProp);
      arrayOfByte = ((WrapToken)localObject).getData();
      setSequencingAndReplayProps((MessageToken)localObject, paramMessageProp);
    }
    else if (this.cipherHelper.getProto() == 1)
    {
      localObject = new WrapToken_v2(this, paramArrayOfByte, paramInt1, paramInt2, paramMessageProp);
      arrayOfByte = ((WrapToken_v2)localObject).getData();
      setSequencingAndReplayProps((MessageToken_v2)localObject, paramMessageProp);
    }
    if (DEBUG)
      System.out.println("Krb5Context.unwrap: data=[" + getHexBytes(arrayOfByte, 0, arrayOfByte.length) + "]");
    return ((B)arrayOfByte);
  }

  public final int unwrap(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, MessageProp paramMessageProp)
    throws GSSException
  {
    Object localObject;
    if (this.state != 3)
      throw new GSSException(12, -1, "Unwrap called in invalid state!");
    if (this.cipherHelper.getProto() == 0)
    {
      localObject = new WrapToken(this, paramArrayOfByte1, paramInt1, paramInt2, paramMessageProp);
      paramInt2 = ((WrapToken)localObject).getData(paramArrayOfByte2, paramInt3);
      setSequencingAndReplayProps((MessageToken)localObject, paramMessageProp);
    }
    else if (this.cipherHelper.getProto() == 1)
    {
      localObject = new WrapToken_v2(this, paramArrayOfByte1, paramInt1, paramInt2, paramMessageProp);
      paramInt2 = ((WrapToken_v2)localObject).getData(paramArrayOfByte2, paramInt3);
      setSequencingAndReplayProps((MessageToken_v2)localObject, paramMessageProp);
    }
    return paramInt2;
  }

  public final int unwrap(InputStream paramInputStream, byte[] paramArrayOfByte, int paramInt, MessageProp paramMessageProp)
    throws GSSException
  {
    Object localObject;
    if (this.state != 3)
      throw new GSSException(12, -1, "Unwrap called in invalid state!");
    int i = 0;
    if (this.cipherHelper.getProto() == 0)
    {
      localObject = new WrapToken(this, paramInputStream, paramMessageProp);
      i = ((WrapToken)localObject).getData(paramArrayOfByte, paramInt);
      setSequencingAndReplayProps((MessageToken)localObject, paramMessageProp);
    }
    else if (this.cipherHelper.getProto() == 1)
    {
      localObject = new WrapToken_v2(this, paramInputStream, paramMessageProp);
      i = ((WrapToken_v2)localObject).getData(paramArrayOfByte, paramInt);
      setSequencingAndReplayProps((MessageToken_v2)localObject, paramMessageProp);
    }
    return i;
  }

  public final void unwrap(InputStream paramInputStream, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    Object localObject;
    if (this.state != 3)
      throw new GSSException(12, -1, "Unwrap called in invalid state!");
    byte[] arrayOfByte = null;
    if (this.cipherHelper.getProto() == 0)
    {
      localObject = new WrapToken(this, paramInputStream, paramMessageProp);
      arrayOfByte = ((WrapToken)localObject).getData();
      setSequencingAndReplayProps((MessageToken)localObject, paramMessageProp);
    }
    else if (this.cipherHelper.getProto() == 1)
    {
      localObject = new WrapToken_v2(this, paramInputStream, paramMessageProp);
      arrayOfByte = ((WrapToken_v2)localObject).getData();
      setSequencingAndReplayProps((MessageToken_v2)localObject, paramMessageProp);
    }
    try
    {
      paramOutputStream.write(arrayOfByte);
    }
    catch (IOException localIOException)
    {
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
  }

  public final byte[] getMIC(byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    byte[] arrayOfByte = null;
    try
    {
      Object localObject;
      if (this.cipherHelper.getProto() == 0)
      {
        localObject = new MicToken(this, paramMessageProp, paramArrayOfByte, paramInt1, paramInt2);
        arrayOfByte = ((MicToken)localObject).encode();
      }
      else if (this.cipherHelper.getProto() == 1)
      {
        localObject = new MicToken_v2(this, paramMessageProp, paramArrayOfByte, paramInt1, paramInt2);
        arrayOfByte = ((MicToken_v2)localObject).encode();
      }
      return arrayOfByte;
    }
    catch (IOException localIOException)
    {
      arrayOfByte = null;
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
  }

  private int getMIC(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, MessageProp paramMessageProp)
    throws GSSException
  {
    int i = 0;
    try
    {
      Object localObject;
      if (this.cipherHelper.getProto() == 0)
      {
        localObject = new MicToken(this, paramMessageProp, paramArrayOfByte1, paramInt1, paramInt2);
        i = ((MicToken)localObject).encode(paramArrayOfByte2, paramInt3);
      }
      else if (this.cipherHelper.getProto() == 1)
      {
        localObject = new MicToken_v2(this, paramMessageProp, paramArrayOfByte1, paramInt1, paramInt2);
        i = ((MicToken_v2)localObject).encode(paramArrayOfByte2, paramInt3);
      }
      return i;
    }
    catch (IOException localIOException)
    {
      i = 0;
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
  }

  private void getMIC(byte[] paramArrayOfByte, int paramInt1, int paramInt2, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    try
    {
      Object localObject;
      if (this.cipherHelper.getProto() == 0)
      {
        localObject = new MicToken(this, paramMessageProp, paramArrayOfByte, paramInt1, paramInt2);
        ((MicToken)localObject).encode(paramOutputStream);
      }
      else if (this.cipherHelper.getProto() == 1)
      {
        localObject = new MicToken_v2(this, paramMessageProp, paramArrayOfByte, paramInt1, paramInt2);
        ((MicToken_v2)localObject).encode(paramOutputStream);
      }
    }
    catch (IOException localIOException)
    {
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
  }

  public final void getMIC(InputStream paramInputStream, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    byte[] arrayOfByte;
    try
    {
      arrayOfByte = new byte[paramInputStream.available()];
      paramInputStream.read(arrayOfByte);
    }
    catch (IOException localIOException)
    {
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
    getMIC(arrayOfByte, 0, arrayOfByte.length, paramOutputStream, paramMessageProp);
  }

  public final void verifyMIC(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, int paramInt4, MessageProp paramMessageProp)
    throws GSSException
  {
    Object localObject;
    if (this.cipherHelper.getProto() == 0)
    {
      localObject = new MicToken(this, paramArrayOfByte1, paramInt1, paramInt2, paramMessageProp);
      ((MicToken)localObject).verify(paramArrayOfByte2, paramInt3, paramInt4);
      setSequencingAndReplayProps((MessageToken)localObject, paramMessageProp);
    }
    else if (this.cipherHelper.getProto() == 1)
    {
      localObject = new MicToken_v2(this, paramArrayOfByte1, paramInt1, paramInt2, paramMessageProp);
      ((MicToken_v2)localObject).verify(paramArrayOfByte2, paramInt3, paramInt4);
      setSequencingAndReplayProps((MessageToken_v2)localObject, paramMessageProp);
    }
  }

  private void verifyMIC(InputStream paramInputStream, byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    Object localObject;
    if (this.cipherHelper.getProto() == 0)
    {
      localObject = new MicToken(this, paramInputStream, paramMessageProp);
      ((MicToken)localObject).verify(paramArrayOfByte, paramInt1, paramInt2);
      setSequencingAndReplayProps((MessageToken)localObject, paramMessageProp);
    }
    else if (this.cipherHelper.getProto() == 1)
    {
      localObject = new MicToken_v2(this, paramInputStream, paramMessageProp);
      ((MicToken_v2)localObject).verify(paramArrayOfByte, paramInt1, paramInt2);
      setSequencingAndReplayProps((MessageToken_v2)localObject, paramMessageProp);
    }
  }

  public final void verifyMIC(InputStream paramInputStream1, InputStream paramInputStream2, MessageProp paramMessageProp)
    throws GSSException
  {
    byte[] arrayOfByte;
    try
    {
      arrayOfByte = new byte[paramInputStream2.available()];
      paramInputStream2.read(arrayOfByte);
    }
    catch (IOException localIOException)
    {
      GSSException localGSSException = new GSSException(11, -1, localIOException.getMessage());
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
    verifyMIC(paramInputStream1, arrayOfByte, 0, arrayOfByte.length, paramMessageProp);
  }

  public final byte[] export()
    throws GSSException
  {
    throw new GSSException(16, -1, "GSS Export Context not available");
  }

  public final void dispose()
    throws GSSException
  {
    this.state = 4;
    this.delegatedCred = null;
  }

  public final Provider getProvider()
  {
    return Krb5MechFactory.PROVIDER;
  }

  private void setSequencingAndReplayProps(MessageToken paramMessageToken, MessageProp paramMessageProp)
  {
    if ((this.replayDetState) || (this.sequenceDetState))
    {
      int i = paramMessageToken.getSequenceNumber();
      this.peerTokenTracker.getProps(i, paramMessageProp);
    }
  }

  private void setSequencingAndReplayProps(MessageToken_v2 paramMessageToken_v2, MessageProp paramMessageProp)
  {
    if ((this.replayDetState) || (this.sequenceDetState))
    {
      int i = paramMessageToken_v2.getSequenceNumber();
      this.peerTokenTracker.getProps(i, paramMessageProp);
    }
  }

  private void checkPermission(String paramString1, String paramString2)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      ServicePermission localServicePermission = new ServicePermission(paramString1, paramString2);
      localSecurityManager.checkPermission(localServicePermission);
    }
  }

  private static String getHexBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < paramInt2; ++i)
    {
      int j = paramArrayOfByte[i] >> 4 & 0xF;
      int k = paramArrayOfByte[i] & 0xF;
      localStringBuffer.append(java.lang.Integer.toHexString(j));
      localStringBuffer.append(java.lang.Integer.toHexString(k));
      localStringBuffer.append(' ');
    }
    return localStringBuffer.toString();
  }

  private static String printState(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return "STATE_NEW";
    case 2:
      return "STATE_IN_PROCESS";
    case 3:
      return "STATE_DONE";
    case 4:
      return "STATE_DELETED";
    }
    return "Unknown state " + paramInt;
  }

  int getCaller()
  {
    return this.caller;
  }
}
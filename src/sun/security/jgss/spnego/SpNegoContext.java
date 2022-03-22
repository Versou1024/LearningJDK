package sun.security.jgss.spnego;

import B;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.Provider;
import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;
import sun.security.action.GetBooleanAction;
import sun.security.jgss.GSSCredentialImpl;
import sun.security.jgss.GSSManagerImpl;
import sun.security.jgss.GSSNameImpl;
import sun.security.jgss.GSSUtil;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.util.DerOutputStream;

public class SpNegoContext
  implements GSSContextSpi
{
  private static final int STATE_NEW = 1;
  private static final int STATE_IN_PROCESS = 2;
  private static final int STATE_DONE = 3;
  private static final int STATE_DELETED = 4;
  private int state = 1;
  private static final int CHECKSUM_DELEG_FLAG = 1;
  private static final int CHECKSUM_MUTUAL_FLAG = 2;
  private static final int CHECKSUM_REPLAY_FLAG = 4;
  private static final int CHECKSUM_SEQUENCE_FLAG = 8;
  private static final int CHECKSUM_CONF_FLAG = 16;
  private static final int CHECKSUM_INTEG_FLAG = 32;
  private boolean credDelegState = false;
  private boolean mutualAuthState = true;
  private boolean replayDetState = true;
  private boolean sequenceDetState = true;
  private boolean confState = true;
  private boolean integState = true;
  private GSSNameSpi peerName = null;
  private GSSNameSpi myName = null;
  private SpNegoCredElement myCred = null;
  private GSSContext mechContext = null;
  private byte[] DER_mechTypes = null;
  private int lifetime;
  private ChannelBinding channelBinding;
  private boolean initiator;
  private Oid internal_mech = null;
  private final SpNegoMechFactory factory;
  static final boolean DEBUG = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.security.spnego.debug"))).booleanValue();

  public SpNegoContext(SpNegoMechFactory paramSpNegoMechFactory, GSSNameSpi paramGSSNameSpi, GSSCredentialSpi paramGSSCredentialSpi, int paramInt)
    throws GSSException
  {
    if (paramGSSNameSpi == null)
      throw new IllegalArgumentException("Cannot have null peer name");
    if ((paramGSSCredentialSpi != null) && (!(paramGSSCredentialSpi instanceof SpNegoCredElement)))
      throw new IllegalArgumentException("Wrong cred element type");
    this.peerName = paramGSSNameSpi;
    this.myCred = ((SpNegoCredElement)paramGSSCredentialSpi);
    this.lifetime = paramInt;
    this.initiator = true;
    this.factory = paramSpNegoMechFactory;
  }

  public SpNegoContext(SpNegoMechFactory paramSpNegoMechFactory, GSSCredentialSpi paramGSSCredentialSpi)
    throws GSSException
  {
    if ((paramGSSCredentialSpi != null) && (!(paramGSSCredentialSpi instanceof SpNegoCredElement)))
      throw new IllegalArgumentException("Wrong cred element type");
    this.myCred = ((SpNegoCredElement)paramGSSCredentialSpi);
    this.initiator = false;
    this.factory = paramSpNegoMechFactory;
  }

  public SpNegoContext(SpNegoMechFactory paramSpNegoMechFactory, byte[] paramArrayOfByte)
    throws GSSException
  {
    throw new GSSException(16, -1, "GSS Import Context not available");
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
    if ((this.mechContext != null) && (((this.state == 2) || (this.state == 3))))
      return this.mechContext.getCredDelegState();
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

  public final Oid getMech()
  {
    if (isEstablished())
      return getNegotiatedMech();
    return SpNegoMechFactory.GSS_SPNEGO_MECH_OID;
  }

  public final Oid getNegotiatedMech()
  {
    return this.internal_mech;
  }

  public final Provider getProvider()
  {
    return SpNegoMechFactory.PROVIDER;
  }

  public final void dispose()
    throws GSSException
  {
    this.mechContext = null;
    this.state = 4;
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
    Object localObject3;
    Object localObject1 = null;
    NegTokenInit localNegTokenInit = null;
    byte[] arrayOfByte1 = null;
    int i = 11;
    if (DEBUG)
      System.out.println("Entered SpNego.initSecContext with state=" + printState(this.state));
    if (!(isInitiator()))
      throw new GSSException(11, -1, "initSecContext on an acceptor GSSContext");
    try
    {
      Object localObject2;
      if (this.state == 1)
      {
        this.state = 2;
        i = 13;
        localObject2 = getAvailableMechs();
        this.DER_mechTypes = getEncodedMechs(localObject2);
        this.internal_mech = localObject2[0];
        arrayOfByte1 = GSS_initSecContext(null);
        i = 10;
        localObject3 = null;
        if (!(GSSUtil.useMSInterop()))
          localObject3 = generateMechListMIC(this.DER_mechTypes);
        localNegTokenInit = new NegTokenInit(this.DER_mechTypes, getContextFlags(), arrayOfByte1, localObject3);
        if (DEBUG)
          System.out.println("SpNegoContext.initSecContext: sending token of type = " + SpNegoToken.getTokenName(localNegTokenInit.getType()));
        localObject1 = localNegTokenInit.getEncoded();
      }
      else if (this.state == 2)
      {
        i = 11;
        if (paramInputStream == null)
          throw new GSSException(i, -1, "No token received from peer!");
        i = 10;
        localObject2 = new byte[paramInputStream.available()];
        SpNegoToken.readFully(paramInputStream, localObject2);
        if (DEBUG)
          System.out.println("SpNegoContext.initSecContext: process received token = " + SpNegoToken.getHexBytes(localObject2));
        localObject3 = new NegTokenTarg(localObject2);
        if (DEBUG)
          System.out.println("SpNegoContext.initSecContext: received token of type = " + SpNegoToken.getTokenName(((NegTokenTarg)localObject3).getType()));
        this.internal_mech = ((NegTokenTarg)localObject3).getSupportedMech();
        if (this.internal_mech == null)
          throw new GSSException(i, -1, "supported mechansim from server is null");
        SpNegoToken.NegoResult localNegoResult = null;
        int j = ((NegTokenTarg)localObject3).getNegotiatedResult();
        switch (j)
        {
        case 0:
          localNegoResult = SpNegoToken.NegoResult.ACCEPT_COMPLETE;
          this.state = 3;
          break;
        case 1:
          localNegoResult = SpNegoToken.NegoResult.ACCEPT_INCOMPLETE;
          this.state = 2;
          break;
        case 2:
          localNegoResult = SpNegoToken.NegoResult.REJECT;
          this.state = 4;
          break;
        default:
          this.state = 3;
        }
        i = 2;
        if (localNegoResult == SpNegoToken.NegoResult.REJECT)
          throw new GSSException(i, -1, this.internal_mech.toString());
        i = 10;
        if ((localNegoResult == SpNegoToken.NegoResult.ACCEPT_COMPLETE) || (localNegoResult == SpNegoToken.NegoResult.ACCEPT_INCOMPLETE))
        {
          byte[] arrayOfByte2 = ((NegTokenTarg)localObject3).getResponseToken();
          if (arrayOfByte2 == null)
            throw new GSSException(i, -1, "mechansim token from server is null");
          arrayOfByte1 = GSS_initSecContext(arrayOfByte2);
          if (!(GSSUtil.useMSInterop()))
          {
            byte[] arrayOfByte3 = ((NegTokenTarg)localObject3).getMechListMIC();
            if (!(verifyMechListMIC(this.DER_mechTypes, arrayOfByte3)))
              throw new GSSException(i, -1, "verification of MIC on MechList Failed!");
          }
          if (isMechContextEstablished())
          {
            this.state = 3;
            localObject1 = arrayOfByte1;
            if (DEBUG)
              System.out.println("SPNEGO Negotiated Mechanism = " + this.internal_mech + " " + GSSUtil.getMechStr(this.internal_mech));
          }
          else
          {
            localNegTokenInit = new NegTokenInit(null, null, arrayOfByte1, null);
            if (DEBUG)
              System.out.println("SpNegoContext.initSecContext: continue sending token of type = " + SpNegoToken.getTokenName(localNegTokenInit.getType()));
            localObject1 = localNegTokenInit.getEncoded();
          }
        }
      }
      else if (DEBUG)
      {
        System.out.println(this.state);
      }
      if ((DEBUG) && (localObject1 != null))
        System.out.println("SNegoContext.initSecContext: sending token = " + SpNegoToken.getHexBytes(localObject1));
    }
    catch (GSSException localGSSException)
    {
      localObject3 = new GSSException(i, -1, localGSSException.getMessage());
      ((GSSException)localObject3).initCause(localGSSException);
      throw ((Throwable)localObject3);
    }
    catch (IOException localIOException)
    {
      localObject3 = new GSSException(11, -1, localIOException.getMessage());
      ((GSSException)localObject3).initCause(localIOException);
      throw ((Throwable)localObject3);
    }
    return ((B)(B)(B)localObject1);
  }

  public final byte[] acceptSecContext(InputStream paramInputStream, int paramInt)
    throws GSSException
  {
    Object localObject1;
    byte[] arrayOfByte1 = null;
    boolean bool = true;
    if (DEBUG)
      System.out.println("Entered SpNegoContext.acceptSecContext with state=" + printState(this.state));
    if (isInitiator())
      throw new GSSException(11, -1, "acceptSecContext on an initiator GSSContext");
    try
    {
      SpNegoToken.NegoResult localNegoResult;
      byte[] arrayOfByte2;
      Object localObject2;
      if (this.state == 1)
      {
        this.state = 2;
        arrayOfByte2 = new byte[paramInputStream.available()];
        SpNegoToken.readFully(paramInputStream, arrayOfByte2);
        if (DEBUG)
          System.out.println("SpNegoContext.acceptSecContext: receiving token = " + SpNegoToken.getHexBytes(arrayOfByte2));
        localObject1 = new NegTokenInit(arrayOfByte2);
        if (DEBUG)
          System.out.println("SpNegoContext.acceptSecContext: received token of type = " + SpNegoToken.getTokenName(((NegTokenInit)localObject1).getType()));
        localObject2 = ((NegTokenInit)localObject1).getMechTypeList();
        this.DER_mechTypes = ((NegTokenInit)localObject1).getMechTypes();
        if (this.DER_mechTypes == null)
          bool = false;
        byte[] arrayOfByte3 = ((NegTokenInit)localObject1).getMechToken();
        Oid[] arrayOfOid = getAvailableMechs();
        Oid localOid = negotiate_mech_type(arrayOfOid, localObject2);
        if (localOid == null)
          bool = false;
        this.internal_mech = localOid;
        byte[] arrayOfByte4 = GSS_acceptSecContext(arrayOfByte3);
        if (arrayOfByte4 == null)
          bool = false;
        if ((!(GSSUtil.useMSInterop())) && (bool))
          bool = verifyMechListMIC(this.DER_mechTypes, ((NegTokenInit)localObject1).getMechListMIC());
        if (bool)
        {
          if (isMechContextEstablished())
          {
            localNegoResult = SpNegoToken.NegoResult.ACCEPT_COMPLETE;
            this.state = 3;
            setContextFlags();
            if (DEBUG)
              System.out.println("SPNEGO Negotiated Mechanism = " + this.internal_mech + " " + GSSUtil.getMechStr(this.internal_mech));
          }
          else
          {
            localNegoResult = SpNegoToken.NegoResult.ACCEPT_INCOMPLETE;
            this.state = 2;
          }
        }
        else
        {
          localNegoResult = SpNegoToken.NegoResult.REJECT;
          this.state = 3;
        }
        if (DEBUG)
        {
          System.out.println("SpNegoContext.acceptSecContext: mechanism wanted = " + localOid);
          System.out.println("SpNegoContext.acceptSecContext: negotiated result = " + localNegoResult);
        }
        byte[] arrayOfByte5 = null;
        if ((!(GSSUtil.useMSInterop())) && (bool))
          arrayOfByte5 = generateMechListMIC(this.DER_mechTypes);
        NegTokenTarg localNegTokenTarg = new NegTokenTarg(localNegoResult.ordinal(), localOid, arrayOfByte4, arrayOfByte5);
        if (DEBUG)
          System.out.println("SpNegoContext.acceptSecContext: sending token of type = " + SpNegoToken.getTokenName(localNegTokenTarg.getType()));
        arrayOfByte1 = localNegTokenTarg.getEncoded();
      }
      else if (this.state == 2)
      {
        arrayOfByte2 = new byte[paramInputStream.available()];
        SpNegoToken.readFully(paramInputStream, arrayOfByte2);
        localObject1 = GSS_acceptSecContext(arrayOfByte2);
        if (localObject1 == null)
          bool = false;
        if (bool)
        {
          if (isMechContextEstablished())
          {
            localNegoResult = SpNegoToken.NegoResult.ACCEPT_COMPLETE;
            this.state = 3;
          }
          else
          {
            localNegoResult = SpNegoToken.NegoResult.ACCEPT_INCOMPLETE;
            this.state = 2;
          }
        }
        else
        {
          localNegoResult = SpNegoToken.NegoResult.REJECT;
          this.state = 3;
        }
        localObject2 = new NegTokenTarg(localNegoResult.ordinal(), null, localObject1, null);
        if (DEBUG)
          System.out.println("SpNegoContext.acceptSecContext: sending token of type = " + SpNegoToken.getTokenName(((NegTokenTarg)localObject2).getType()));
        arrayOfByte1 = ((NegTokenTarg)localObject2).getEncoded();
      }
      else if (DEBUG)
      {
        System.out.println("AcceptSecContext: state = " + this.state);
      }
      if (DEBUG)
        System.out.println("SpNegoContext.acceptSecContext: sending token = " + SpNegoToken.getHexBytes(arrayOfByte1));
    }
    catch (IOException localIOException)
    {
      localObject1 = new GSSException(11, -1, localIOException.getMessage());
      ((GSSException)localObject1).initCause(localIOException);
      throw ((Throwable)localObject1);
    }
    return ((B)(B)arrayOfByte1);
  }

  private Oid[] getAvailableMechs()
  {
    if (this.myCred != null)
    {
      Oid[] arrayOfOid = new Oid[1];
      arrayOfOid[0] = this.myCred.getInternalMech();
      return arrayOfOid;
    }
    return this.factory.availableMechs;
  }

  private byte[] getEncodedMechs(Oid[] paramArrayOfOid)
    throws IOException, GSSException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    for (int i = 0; i < paramArrayOfOid.length; ++i)
    {
      arrayOfByte = paramArrayOfOid[i].getDER();
      localDerOutputStream1.write(arrayOfByte);
    }
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    byte[] arrayOfByte = localDerOutputStream2.toByteArray();
    return arrayOfByte;
  }

  private byte[] getContextFlags()
  {
    int i = 0;
    if (getCredDelegState())
      i |= 1;
    if (getMutualAuthState())
      i |= 2;
    if (getReplayDetState())
      i |= 4;
    if (getSequenceDetState())
      i |= 8;
    if (getIntegState())
      i |= 32;
    if (getConfState())
      i |= 16;
    byte[] arrayOfByte = new byte[1];
    arrayOfByte[0] = (byte)(i & 0xFF);
    return arrayOfByte;
  }

  private void setContextFlags()
  {
    if (this.mechContext != null)
    {
      if (this.mechContext.getCredDelegState())
        setCredDelegState(true);
      if (!(this.mechContext.getMutualAuthState()))
        setMutualAuthState(false);
      if (!(this.mechContext.getReplayDetState()))
        setReplayDetState(false);
      if (!(this.mechContext.getSequenceDetState()))
        setSequenceDetState(false);
      if (!(this.mechContext.getIntegState()))
        setIntegState(false);
      if (!(this.mechContext.getConfState()))
        setConfState(false);
    }
  }

  private byte[] generateMechListMIC(byte[] paramArrayOfByte)
    throws GSSException
  {
    if (paramArrayOfByte == null)
    {
      if (DEBUG)
        System.out.println("SpNegoContext: no MIC token included");
      return null;
    }
    if (!(this.mechContext.getIntegState()))
    {
      if (DEBUG)
        System.out.println("SpNegoContext: no MIC token included - mechanism does not support integrity");
      return null;
    }
    byte[] arrayOfByte = null;
    try
    {
      MessageProp localMessageProp = new MessageProp(0, true);
      arrayOfByte = getMIC(paramArrayOfByte, 0, paramArrayOfByte.length, localMessageProp);
      if (DEBUG)
        System.out.println("SpNegoContext: getMIC = " + SpNegoToken.getHexBytes(arrayOfByte));
    }
    catch (GSSException localGSSException)
    {
      arrayOfByte = null;
      if (DEBUG)
        System.out.println("SpNegoContext: no MIC token included - getMIC failed : " + localGSSException.getMessage());
    }
    return arrayOfByte;
  }

  private boolean verifyMechListMIC(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws GSSException
  {
    if (paramArrayOfByte2 == null)
    {
      if (DEBUG)
        System.out.println("SpNegoContext: no MIC token validation");
      return true;
    }
    if (!(this.mechContext.getIntegState()))
    {
      if (DEBUG)
        System.out.println("SpNegoContext: no MIC token validation - mechanism does not support integrity");
      return true;
    }
    int i = 0;
    try
    {
      MessageProp localMessageProp = new MessageProp(0, true);
      verifyMIC(paramArrayOfByte2, 0, paramArrayOfByte2.length, paramArrayOfByte1, 0, paramArrayOfByte1.length, localMessageProp);
      i = 1;
    }
    catch (GSSException localGSSException)
    {
      i = 0;
      if (DEBUG)
        System.out.println("SpNegoContext: MIC validation failed! " + localGSSException.getMessage());
    }
    return i;
  }

  private byte[] GSS_initSecContext(byte[] paramArrayOfByte)
    throws GSSException
  {
    byte[] arrayOfByte = null;
    if (this.mechContext == null)
    {
      localObject = this.factory.manager.createName(this.peerName.toString(), this.peerName.getStringNameType(), this.internal_mech);
      GSSCredentialImpl localGSSCredentialImpl = null;
      if (this.myCred != null)
        localGSSCredentialImpl = new GSSCredentialImpl(this.factory.manager, this.myCred.getInternalCred());
      this.mechContext = this.factory.manager.createContext((GSSName)localObject, this.internal_mech, localGSSCredentialImpl, 0);
      this.mechContext.requestConf(this.confState);
      this.mechContext.requestInteg(this.integState);
      this.mechContext.requestCredDeleg(this.credDelegState);
      this.mechContext.requestMutualAuth(this.mutualAuthState);
      this.mechContext.requestReplayDet(this.replayDetState);
      this.mechContext.requestSequenceDet(this.sequenceDetState);
    }
    if (paramArrayOfByte != null)
      arrayOfByte = paramArrayOfByte;
    else
      arrayOfByte = new byte[0];
    Object localObject = this.mechContext.initSecContext(arrayOfByte, 0, arrayOfByte.length);
    return ((B)localObject);
  }

  private byte[] GSS_acceptSecContext(byte[] paramArrayOfByte)
    throws GSSException
  {
    if (this.mechContext == null)
    {
      localObject = null;
      if (this.myCred != null)
        localObject = new GSSCredentialImpl(this.factory.manager, this.myCred.getInternalCred());
      this.mechContext = this.factory.manager.createContext((GSSCredential)localObject);
    }
    Object localObject = this.mechContext.acceptSecContext(paramArrayOfByte, 0, paramArrayOfByte.length);
    return ((B)localObject);
  }

  private static Oid negotiate_mech_type(Oid[] paramArrayOfOid1, Oid[] paramArrayOfOid2)
  {
    for (int i = 0; i < paramArrayOfOid1.length; ++i)
      for (int j = 0; j < paramArrayOfOid2.length; ++j)
        if (paramArrayOfOid2[j].equals(paramArrayOfOid1[i]))
        {
          if (DEBUG)
            System.out.println("SpNegoContext: negotiated mechanism = " + paramArrayOfOid2[j]);
          return paramArrayOfOid2[j];
        }
    return null;
  }

  public final boolean isEstablished()
  {
    return (this.state == 3);
  }

  public final boolean isMechContextEstablished()
  {
    if (this.mechContext != null)
      return this.mechContext.isEstablished();
    if (DEBUG)
      System.out.println("The underlying mechansim context has not been initialized");
    return false;
  }

  public final byte[] export()
    throws GSSException
  {
    throw new GSSException(16, -1, "GSS Export Context not available");
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

  public final void requestAnonymity(boolean paramBoolean)
    throws GSSException
  {
  }

  public final boolean getAnonymityState()
  {
    return false;
  }

  public void requestLifetime(int paramInt)
    throws GSSException
  {
    if ((this.state == 1) && (isInitiator()))
      this.lifetime = paramInt;
  }

  public final int getLifetime()
  {
    if (this.mechContext != null)
      return this.mechContext.getLifetime();
    return 2147483647;
  }

  public final boolean isTransferable()
    throws GSSException
  {
    return false;
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

  public final GSSNameSpi getTargName()
    throws GSSException
  {
    if (this.mechContext != null)
    {
      GSSNameImpl localGSSNameImpl = (GSSNameImpl)this.mechContext.getTargName();
      this.peerName = localGSSNameImpl.getElement(this.internal_mech);
      return this.peerName;
    }
    if (DEBUG)
      System.out.println("The underlying mechansim context has not been initialized");
    return null;
  }

  public final GSSNameSpi getSrcName()
    throws GSSException
  {
    if (this.mechContext != null)
    {
      GSSNameImpl localGSSNameImpl = (GSSNameImpl)this.mechContext.getSrcName();
      this.myName = localGSSNameImpl.getElement(this.internal_mech);
      return this.myName;
    }
    if (DEBUG)
      System.out.println("The underlying mechansim context has not been initialized");
    return null;
  }

  public final GSSCredentialSpi getDelegCred()
    throws GSSException
  {
    if ((this.state != 2) && (this.state != 3))
      throw new GSSException(12);
    if (this.mechContext != null)
    {
      GSSCredentialImpl localGSSCredentialImpl = (GSSCredentialImpl)this.mechContext.getDelegCred();
      boolean bool = false;
      if (localGSSCredentialImpl.getUsage() == 1)
        bool = true;
      GSSCredentialSpi localGSSCredentialSpi = localGSSCredentialImpl.getElement(this.internal_mech, bool);
      SpNegoCredElement localSpNegoCredElement = new SpNegoCredElement(localGSSCredentialSpi);
      return localSpNegoCredElement.getInternalCred();
    }
    throw new GSSException(12, -1, "getDelegCred called in invalid state!");
  }

  public final int getWrapSizeLimit(int paramInt1, boolean paramBoolean, int paramInt2)
    throws GSSException
  {
    if (this.mechContext != null)
      return this.mechContext.getWrapSizeLimit(paramInt1, paramBoolean, paramInt2);
    throw new GSSException(12, -1, "getWrapSizeLimit called in invalid state!");
  }

  public final byte[] wrap(byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechContext != null)
      return this.mechContext.wrap(paramArrayOfByte, paramInt1, paramInt2, paramMessageProp);
    throw new GSSException(12, -1, "Wrap called in invalid state!");
  }

  public final void wrap(InputStream paramInputStream, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechContext != null)
      this.mechContext.wrap(paramInputStream, paramOutputStream, paramMessageProp);
    else
      throw new GSSException(12, -1, "Wrap called in invalid state!");
  }

  public final byte[] unwrap(byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechContext != null)
      return this.mechContext.unwrap(paramArrayOfByte, paramInt1, paramInt2, paramMessageProp);
    throw new GSSException(12, -1, "UnWrap called in invalid state!");
  }

  public final void unwrap(InputStream paramInputStream, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechContext != null)
      this.mechContext.unwrap(paramInputStream, paramOutputStream, paramMessageProp);
    else
      throw new GSSException(12, -1, "UnWrap called in invalid state!");
  }

  public final byte[] getMIC(byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechContext != null)
      return this.mechContext.getMIC(paramArrayOfByte, paramInt1, paramInt2, paramMessageProp);
    throw new GSSException(12, -1, "getMIC called in invalid state!");
  }

  public final void getMIC(InputStream paramInputStream, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechContext != null)
      this.mechContext.getMIC(paramInputStream, paramOutputStream, paramMessageProp);
    else
      throw new GSSException(12, -1, "getMIC called in invalid state!");
  }

  public final void verifyMIC(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, int paramInt4, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechContext != null)
      this.mechContext.verifyMIC(paramArrayOfByte1, paramInt1, paramInt2, paramArrayOfByte2, paramInt3, paramInt4, paramMessageProp);
    else
      throw new GSSException(12, -1, "verifyMIC called in invalid state!");
  }

  public final void verifyMIC(InputStream paramInputStream1, InputStream paramInputStream2, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechContext != null)
      this.mechContext.verifyMIC(paramInputStream1, paramInputStream2, paramMessageProp);
    else
      throw new GSSException(12, -1, "verifyMIC called in invalid state!");
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
}
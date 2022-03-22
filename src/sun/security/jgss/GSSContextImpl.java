package sun.security.jgss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Provider;
import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.util.ObjectIdentifier;

class GSSContextImpl
  implements GSSContext
{
  private GSSManagerImpl gssManager = null;
  private static final int PRE_INIT = 1;
  private static final int IN_PROGRESS = 2;
  private static final int READY = 3;
  private static final int DELETED = 4;
  private int currentState = 1;
  private boolean initiator;
  private GSSContextSpi mechCtxt = null;
  private Oid mechOid = null;
  private ObjectIdentifier objId = null;
  private GSSCredentialImpl myCred = null;
  private GSSCredentialImpl delegCred = null;
  private GSSNameImpl srcName = null;
  private GSSNameImpl targName = null;
  private int reqLifetime = 2147483647;
  private ChannelBinding channelBindings = null;
  private boolean reqConfState = true;
  private boolean reqIntegState = true;
  private boolean reqMutualAuthState = true;
  private boolean reqReplayDetState = true;
  private boolean reqSequenceDetState = true;
  private boolean reqCredDelegState = false;
  private boolean reqAnonState = false;

  public GSSContextImpl(GSSManagerImpl paramGSSManagerImpl, GSSName paramGSSName, Oid paramOid, GSSCredential paramGSSCredential, int paramInt)
    throws GSSException
  {
    if ((paramGSSName == null) || (!(paramGSSName instanceof GSSNameImpl)))
      throw new GSSException(3);
    if (paramOid == null)
      paramOid = ProviderList.DEFAULT_MECH_OID;
    this.gssManager = paramGSSManagerImpl;
    this.myCred = ((GSSCredentialImpl)paramGSSCredential);
    this.reqLifetime = paramInt;
    this.targName = ((GSSNameImpl)paramGSSName);
    this.mechOid = paramOid;
    this.initiator = true;
  }

  public GSSContextImpl(GSSManagerImpl paramGSSManagerImpl, GSSCredential paramGSSCredential)
    throws GSSException
  {
    this.gssManager = paramGSSManagerImpl;
    this.myCred = ((GSSCredentialImpl)paramGSSCredential);
    this.initiator = false;
  }

  public GSSContextImpl(GSSManagerImpl paramGSSManagerImpl, byte[] paramArrayOfByte)
    throws GSSException
  {
    this.gssManager = paramGSSManagerImpl;
    this.mechCtxt = paramGSSManagerImpl.getMechanismContext(paramArrayOfByte);
    this.initiator = this.mechCtxt.isInitiator();
    this.mechOid = this.mechCtxt.getMech();
  }

  public byte[] initSecContext(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws GSSException
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(600);
    ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(paramArrayOfByte, paramInt1, paramInt2);
    int i = initSecContext(localByteArrayInputStream, localByteArrayOutputStream);
    return ((i == 0) ? null : localByteArrayOutputStream.toByteArray());
  }

  public int initSecContext(InputStream paramInputStream, OutputStream paramOutputStream)
    throws GSSException
  {
    if ((this.mechCtxt != null) && (this.currentState != 2))
      throw new GSSExceptionImpl(11, "Illegal call to initSecContext");
    GSSHeader localGSSHeader = null;
    int i = -1;
    GSSCredentialSpi localGSSCredentialSpi = null;
    int j = 0;
    try
    {
      if (this.mechCtxt == null)
      {
        if (this.myCred != null)
          try
          {
            localGSSCredentialSpi = this.myCred.getElement(this.mechOid, true);
          }
          catch (GSSException localGSSException)
          {
            if ((GSSUtil.isSpNegoMech(this.mechOid)) && (localGSSException.getMajor() == 13))
              localGSSCredentialSpi = this.myCred.getElement(this.myCred.getMechs()[0], true);
            else
              throw localGSSException;
          }
        localObject = this.targName.getElement(this.mechOid);
        this.mechCtxt = this.gssManager.getMechanismContext((GSSNameSpi)localObject, localGSSCredentialSpi, this.reqLifetime, this.mechOid);
        this.mechCtxt.requestConf(this.reqConfState);
        this.mechCtxt.requestInteg(this.reqIntegState);
        this.mechCtxt.requestCredDeleg(this.reqCredDelegState);
        this.mechCtxt.requestMutualAuth(this.reqMutualAuthState);
        this.mechCtxt.requestReplayDet(this.reqReplayDetState);
        this.mechCtxt.requestSequenceDet(this.reqSequenceDetState);
        this.mechCtxt.requestAnonymity(this.reqAnonState);
        this.mechCtxt.setChannelBinding(this.channelBindings);
        this.objId = new ObjectIdentifier(this.mechOid.toString());
        this.currentState = 2;
        j = 1;
      }
      else if (!(this.mechCtxt.getProvider().getName().equals("SunNativeGSS")))
      {
        if (GSSUtil.isSpNegoMech(this.mechOid))
          break label387:
        localGSSHeader = new GSSHeader(paramInputStream);
        if (!(localGSSHeader.getOid().equals(this.objId)))
          throw new GSSExceptionImpl(10, "Mechanism not equal to " + this.mechOid.toString() + " in initSecContext token");
        i = localGSSHeader.getMechTokenLength();
      }
      label387: Object localObject = this.mechCtxt.initSecContext(paramInputStream, i);
      int k = 0;
      if (localObject != null)
      {
        k = localObject.length;
        if (!(this.mechCtxt.getProvider().getName().equals("SunNativeGSS")))
        {
          if ((j == 0) && (GSSUtil.isSpNegoMech(this.mechOid)))
            break label477:
          localGSSHeader = new GSSHeader(this.objId, localObject.length);
          k += localGSSHeader.encode(paramOutputStream);
        }
        label477: paramOutputStream.write(localObject);
      }
      if (this.mechCtxt.isEstablished())
        this.currentState = 3;
      return k;
    }
    catch (IOException localIOException)
    {
      throw new GSSExceptionImpl(10, localIOException.getMessage());
    }
  }

  public byte[] acceptSecContext(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws GSSException
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(100);
    acceptSecContext(new ByteArrayInputStream(paramArrayOfByte, paramInt1, paramInt2), localByteArrayOutputStream);
    return localByteArrayOutputStream.toByteArray();
  }

  public void acceptSecContext(InputStream paramInputStream, OutputStream paramOutputStream)
    throws GSSException
  {
    if ((this.mechCtxt != null) && (this.currentState != 2))
      throw new GSSExceptionImpl(11, "Illegal call to acceptSecContext");
    GSSHeader localGSSHeader = null;
    int i = -1;
    GSSCredentialSpi localGSSCredentialSpi = null;
    try
    {
      if (this.mechCtxt == null)
      {
        localGSSHeader = new GSSHeader(paramInputStream);
        i = localGSSHeader.getMechTokenLength();
        this.objId = localGSSHeader.getOid();
        this.mechOid = new Oid(this.objId.toString());
        if (this.myCred != null)
          localGSSCredentialSpi = this.myCred.getElement(this.mechOid, false);
        this.mechCtxt = this.gssManager.getMechanismContext(localGSSCredentialSpi, this.mechOid);
        this.mechCtxt.setChannelBinding(this.channelBindings);
        this.currentState = 2;
      }
      else if (!(this.mechCtxt.getProvider().getName().equals("SunNativeGSS")))
      {
        if (GSSUtil.isSpNegoMech(this.mechOid))
          break label244:
        localGSSHeader = new GSSHeader(paramInputStream);
        if (!(localGSSHeader.getOid().equals(this.objId)))
          throw new GSSExceptionImpl(10, "Mechanism not equal to " + this.mechOid.toString() + " in acceptSecContext token");
        i = localGSSHeader.getMechTokenLength();
      }
      label244: byte[] arrayOfByte = this.mechCtxt.acceptSecContext(paramInputStream, i);
      if (arrayOfByte != null)
      {
        int j = arrayOfByte.length;
        if (!(this.mechCtxt.getProvider().getName().equals("SunNativeGSS")))
        {
          if (GSSUtil.isSpNegoMech(this.mechOid))
            break label326:
          localGSSHeader = new GSSHeader(this.objId, arrayOfByte.length);
          j += localGSSHeader.encode(paramOutputStream);
        }
        label326: paramOutputStream.write(arrayOfByte);
      }
      if (this.mechCtxt.isEstablished())
        this.currentState = 3;
    }
    catch (IOException localIOException)
    {
      throw new GSSExceptionImpl(10, localIOException.getMessage());
    }
  }

  public boolean isEstablished()
  {
    if (this.mechCtxt == null)
      return false;
    return (this.currentState == 3);
  }

  public int getWrapSizeLimit(int paramInt1, boolean paramBoolean, int paramInt2)
    throws GSSException
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getWrapSizeLimit(paramInt1, paramBoolean, paramInt2);
    throw new GSSExceptionImpl(12, "No mechanism context yet!");
  }

  public byte[] wrap(byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.wrap(paramArrayOfByte, paramInt1, paramInt2, paramMessageProp);
    throw new GSSExceptionImpl(12, "No mechanism context yet!");
  }

  public void wrap(InputStream paramInputStream, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechCtxt != null)
      this.mechCtxt.wrap(paramInputStream, paramOutputStream, paramMessageProp);
    else
      throw new GSSExceptionImpl(12, "No mechanism context yet!");
  }

  public byte[] unwrap(byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.unwrap(paramArrayOfByte, paramInt1, paramInt2, paramMessageProp);
    throw new GSSExceptionImpl(12, "No mechanism context yet!");
  }

  public void unwrap(InputStream paramInputStream, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechCtxt != null)
      this.mechCtxt.unwrap(paramInputStream, paramOutputStream, paramMessageProp);
    else
      throw new GSSExceptionImpl(12, "No mechanism context yet!");
  }

  public byte[] getMIC(byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getMIC(paramArrayOfByte, paramInt1, paramInt2, paramMessageProp);
    throw new GSSExceptionImpl(12, "No mechanism context yet!");
  }

  public void getMIC(InputStream paramInputStream, OutputStream paramOutputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechCtxt != null)
      this.mechCtxt.getMIC(paramInputStream, paramOutputStream, paramMessageProp);
    else
      throw new GSSExceptionImpl(12, "No mechanism context yet!");
  }

  public void verifyMIC(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, int paramInt4, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechCtxt != null)
      this.mechCtxt.verifyMIC(paramArrayOfByte1, paramInt1, paramInt2, paramArrayOfByte2, paramInt3, paramInt4, paramMessageProp);
    else
      throw new GSSExceptionImpl(12, "No mechanism context yet!");
  }

  public void verifyMIC(InputStream paramInputStream1, InputStream paramInputStream2, MessageProp paramMessageProp)
    throws GSSException
  {
    if (this.mechCtxt != null)
      this.mechCtxt.verifyMIC(paramInputStream1, paramInputStream2, paramMessageProp);
    else
      throw new GSSExceptionImpl(12, "No mechanism context yet!");
  }

  public byte[] export()
    throws GSSException
  {
    byte[] arrayOfByte = null;
    if ((this.mechCtxt.isTransferable()) && (this.mechCtxt.getProvider().getName().equals("SunNativeGSS")))
      arrayOfByte = this.mechCtxt.export();
    return arrayOfByte;
  }

  public void requestMutualAuth(boolean paramBoolean)
    throws GSSException
  {
    if (this.mechCtxt == null)
      this.reqMutualAuthState = paramBoolean;
  }

  public void requestReplayDet(boolean paramBoolean)
    throws GSSException
  {
    if (this.mechCtxt == null)
      this.reqReplayDetState = paramBoolean;
  }

  public void requestSequenceDet(boolean paramBoolean)
    throws GSSException
  {
    if (this.mechCtxt == null)
      this.reqSequenceDetState = paramBoolean;
  }

  public void requestCredDeleg(boolean paramBoolean)
    throws GSSException
  {
    if (this.mechCtxt == null)
      this.reqCredDelegState = paramBoolean;
  }

  public void requestAnonymity(boolean paramBoolean)
    throws GSSException
  {
    if (this.mechCtxt == null)
      this.reqAnonState = paramBoolean;
  }

  public void requestConf(boolean paramBoolean)
    throws GSSException
  {
    if (this.mechCtxt == null)
      this.reqConfState = paramBoolean;
  }

  public void requestInteg(boolean paramBoolean)
    throws GSSException
  {
    if (this.mechCtxt == null)
      this.reqIntegState = paramBoolean;
  }

  public void requestLifetime(int paramInt)
    throws GSSException
  {
    if (this.mechCtxt == null)
      this.reqLifetime = paramInt;
  }

  public void setChannelBinding(ChannelBinding paramChannelBinding)
    throws GSSException
  {
    if (this.mechCtxt == null)
      this.channelBindings = paramChannelBinding;
  }

  public boolean getCredDelegState()
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getCredDelegState();
    return this.reqCredDelegState;
  }

  public boolean getMutualAuthState()
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getMutualAuthState();
    return this.reqMutualAuthState;
  }

  public boolean getReplayDetState()
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getReplayDetState();
    return this.reqReplayDetState;
  }

  public boolean getSequenceDetState()
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getSequenceDetState();
    return this.reqSequenceDetState;
  }

  public boolean getAnonymityState()
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getAnonymityState();
    return this.reqAnonState;
  }

  public boolean isTransferable()
    throws GSSException
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.isTransferable();
    return false;
  }

  public boolean isProtReady()
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.isProtReady();
    return false;
  }

  public boolean getConfState()
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getConfState();
    return this.reqConfState;
  }

  public boolean getIntegState()
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getIntegState();
    return this.reqIntegState;
  }

  public int getLifetime()
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getLifetime();
    return this.reqLifetime;
  }

  public GSSName getSrcName()
    throws GSSException
  {
    if (this.srcName == null)
      this.srcName = GSSNameImpl.wrapElement(this.gssManager, this.mechCtxt.getSrcName());
    return this.srcName;
  }

  public GSSName getTargName()
    throws GSSException
  {
    if (this.targName == null)
      this.targName = GSSNameImpl.wrapElement(this.gssManager, this.mechCtxt.getTargName());
    return this.targName;
  }

  public Oid getMech()
    throws GSSException
  {
    if (this.mechCtxt != null)
      return this.mechCtxt.getMech();
    return this.mechOid;
  }

  public GSSCredential getDelegCred()
    throws GSSException
  {
    if (this.mechCtxt == null)
      throw new GSSExceptionImpl(12, "No mechanism context yet!");
    GSSCredentialSpi localGSSCredentialSpi = this.mechCtxt.getDelegCred();
    return new GSSCredentialImpl(this.gssManager, localGSSCredentialSpi);
  }

  public boolean isInitiator()
    throws GSSException
  {
    return this.initiator;
  }

  public void dispose()
    throws GSSException
  {
    this.currentState = 4;
    if (this.mechCtxt != null)
    {
      this.mechCtxt.dispose();
      this.mechCtxt = null;
    }
    this.myCred = null;
    this.srcName = null;
    this.targName = null;
  }
}
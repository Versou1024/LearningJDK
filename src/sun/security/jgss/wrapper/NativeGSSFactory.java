package sun.security.jgss.wrapper;

import java.io.UnsupportedEncodingException;
import java.security.Provider;
import java.util.Vector;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import sun.security.jgss.GSSExceptionImpl;
import sun.security.jgss.GSSUtil;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.jgss.spi.MechanismFactory;

public final class NativeGSSFactory
  implements MechanismFactory
{
  GSSLibStub cStub = null;

  private GSSCredElement getCredFromSubject(GSSNameElement paramGSSNameElement, boolean paramBoolean)
    throws GSSException
  {
    Oid localOid = this.cStub.getMech();
    Vector localVector = GSSUtil.searchSubject(paramGSSNameElement, localOid, paramBoolean, GSSCredElement.class);
    if ((localVector != null) && (localVector.isEmpty()) && (GSSUtil.useSubjectCredsOnly()))
      throw new GSSException(13);
    GSSCredElement localGSSCredElement = ((localVector == null) || (localVector.isEmpty())) ? null : (GSSCredElement)localVector.firstElement();
    if (localGSSCredElement != null)
      localGSSCredElement.doServicePermCheck();
    return localGSSCredElement;
  }

  public void setMech(Oid paramOid)
    throws GSSException
  {
    this.cStub = GSSLibStub.getInstance(paramOid);
  }

  public GSSNameSpi getNameElement(String paramString, Oid paramOid)
    throws GSSException
  {
    try
    {
      byte[] arrayOfByte = (paramString == null) ? null : paramString.getBytes("UTF-8");
      return new GSSNameElement(arrayOfByte, paramOid, this.cStub);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new GSSExceptionImpl(11, localUnsupportedEncodingException);
    }
  }

  public GSSNameSpi getNameElement(byte[] paramArrayOfByte, Oid paramOid)
    throws GSSException
  {
    return new GSSNameElement(paramArrayOfByte, paramOid, this.cStub);
  }

  public GSSCredentialSpi getCredentialElement(GSSNameSpi paramGSSNameSpi, int paramInt1, int paramInt2, int paramInt3)
    throws GSSException
  {
    GSSNameElement localGSSNameElement = null;
    if ((paramGSSNameSpi != null) && (!(paramGSSNameSpi instanceof GSSNameElement)))
      localGSSNameElement = (GSSNameElement)getNameElement(paramGSSNameSpi.toString(), paramGSSNameSpi.getStringNameType());
    else
      localGSSNameElement = (GSSNameElement)paramGSSNameSpi;
    if (paramInt3 == 0)
      paramInt3 = 1;
    GSSCredElement localGSSCredElement = getCredFromSubject(localGSSNameElement, paramInt3 == 1);
    if (localGSSCredElement == null)
      if (paramInt3 == 1)
      {
        localGSSCredElement = new GSSCredElement(localGSSNameElement, paramInt1, paramInt3, this.cStub);
      }
      else if (paramInt3 == 2)
      {
        if (localGSSNameElement == null)
          localGSSNameElement = GSSNameElement.DEF_ACCEPTOR;
        localGSSCredElement = new GSSCredElement(localGSSNameElement, paramInt2, paramInt3, this.cStub);
      }
      else
      {
        throw new GSSException(11, -1, "Unknown usage mode requested");
      }
    return localGSSCredElement;
  }

  public GSSContextSpi getMechanismContext(GSSNameSpi paramGSSNameSpi, GSSCredentialSpi paramGSSCredentialSpi, int paramInt)
    throws GSSException
  {
    if (paramGSSNameSpi == null)
      throw new GSSException(3);
    if (!(paramGSSNameSpi instanceof GSSNameElement))
      paramGSSNameSpi = (GSSNameElement)getNameElement(paramGSSNameSpi.toString(), paramGSSNameSpi.getStringNameType());
    if (paramGSSCredentialSpi == null)
      paramGSSCredentialSpi = getCredFromSubject(null, true);
    else if (!(paramGSSCredentialSpi instanceof GSSCredElement))
      throw new GSSException(13);
    return new NativeGSSContext((GSSNameElement)paramGSSNameSpi, (GSSCredElement)paramGSSCredentialSpi, paramInt, this.cStub);
  }

  public GSSContextSpi getMechanismContext(GSSCredentialSpi paramGSSCredentialSpi)
    throws GSSException
  {
    if (paramGSSCredentialSpi == null)
      paramGSSCredentialSpi = getCredFromSubject(null, false);
    else if (!(paramGSSCredentialSpi instanceof GSSCredElement))
      throw new GSSException(13);
    return new NativeGSSContext((GSSCredElement)paramGSSCredentialSpi, this.cStub);
  }

  public GSSContextSpi getMechanismContext(byte[] paramArrayOfByte)
    throws GSSException
  {
    return this.cStub.importContext(paramArrayOfByte);
  }

  public final Oid getMechanismOid()
  {
    return this.cStub.getMech();
  }

  public Provider getProvider()
  {
    return SunNativeProvider.INSTANCE;
  }

  public Oid[] getNameTypes()
    throws GSSException
  {
    return this.cStub.inquireNamesForMech();
  }
}
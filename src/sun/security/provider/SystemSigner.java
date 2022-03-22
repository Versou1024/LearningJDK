package sun.security.provider;

import java.security.Certificate;
import java.security.IdentityScope;
import java.security.InvalidParameterException;
import java.security.KeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Signer;

public class SystemSigner extends Signer
{
  private static final long serialVersionUID = -2127743304301557711L;
  private boolean trusted = false;

  public SystemSigner(String paramString)
  {
    super(paramString);
  }

  public SystemSigner(String paramString, IdentityScope paramIdentityScope)
    throws KeyManagementException
  {
    super(paramString, paramIdentityScope);
  }

  void setTrusted(boolean paramBoolean)
  {
    this.trusted = paramBoolean;
  }

  public boolean isTrusted()
  {
    return this.trusted;
  }

  void setSignerKeyPair(KeyPair paramKeyPair)
    throws InvalidParameterException, KeyException
  {
    setKeyPair(paramKeyPair);
  }

  PrivateKey getSignerPrivateKey()
  {
    return getPrivateKey();
  }

  void setSignerInfo(String paramString)
  {
    setInfo(paramString);
  }

  void addSignerCertificate(Certificate paramCertificate)
    throws KeyManagementException
  {
    addCertificate(paramCertificate);
  }

  void clearCertificates()
    throws KeyManagementException
  {
    Certificate[] arrayOfCertificate = certificates();
    for (int i = 0; i < arrayOfCertificate.length; ++i)
      removeCertificate(arrayOfCertificate[i]);
  }

  public String toString()
  {
    String str = "not trusted";
    if (this.trusted)
      str = "trusted";
    return super.toString() + "[" + str + "]";
  }
}
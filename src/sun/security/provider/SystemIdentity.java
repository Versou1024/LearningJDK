package sun.security.provider;

import java.io.Serializable;
import java.security.Certificate;
import java.security.Identity;
import java.security.IdentityScope;
import java.security.InvalidParameterException;
import java.security.KeyManagementException;
import java.security.PublicKey;

public class SystemIdentity extends Identity
  implements Serializable
{
  private static final long serialVersionUID = 9060648952088498478L;
  boolean trusted = false;
  private String info;

  public SystemIdentity(String paramString, IdentityScope paramIdentityScope)
    throws InvalidParameterException, KeyManagementException
  {
    super(paramString, paramIdentityScope);
  }

  public boolean isTrusted()
  {
    return this.trusted;
  }

  protected void setTrusted(boolean paramBoolean)
  {
    this.trusted = paramBoolean;
  }

  void setIdentityInfo(String paramString)
  {
    super.setInfo(paramString);
  }

  String getIndentityInfo()
  {
    return super.getInfo();
  }

  void setIdentityPublicKey(PublicKey paramPublicKey)
    throws KeyManagementException
  {
    setPublicKey(paramPublicKey);
  }

  void addIdentityCertificate(Certificate paramCertificate)
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
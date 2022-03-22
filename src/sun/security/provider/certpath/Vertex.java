package sun.security.provider.certpath;

import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import sun.security.util.Debug;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.SubjectKeyIdentifierExtension;
import sun.security.x509.X509CertImpl;

public class Vertex
{
  private static final Debug debug = Debug.getInstance("certpath");
  private Certificate cert;
  private int index;
  private Throwable throwable;

  Vertex(Certificate paramCertificate)
  {
    this.cert = paramCertificate;
    this.index = -1;
  }

  public Certificate getCertificate()
  {
    return this.cert;
  }

  public int getIndex()
  {
    return this.index;
  }

  void setIndex(int paramInt)
  {
    this.index = paramInt;
  }

  public Throwable getThrowable()
  {
    return this.throwable;
  }

  void setThrowable(Throwable paramThrowable)
  {
    this.throwable = paramThrowable;
  }

  public String toString()
  {
    return certToString() + throwableToString() + indexToString();
  }

  public String certToString()
  {
    String str = "";
    if ((this.cert == null) || (!(this.cert instanceof X509Certificate)))
      return "Cert:       Not an X509Certificate\n";
    X509CertImpl localX509CertImpl = null;
    try
    {
      localX509CertImpl = X509CertImpl.toImpl((X509Certificate)this.cert);
    }
    catch (CertificateException localCertificateException)
    {
      if (debug != null)
      {
        debug.println("Vertex.certToString() unexpected exception");
        localCertificateException.printStackTrace();
      }
      return str;
    }
    str = "Issuer:     " + localX509CertImpl.getIssuerX500Principal() + "\n";
    str = str + "Subject:    " + localX509CertImpl.getSubjectX500Principal() + "\n";
    str = str + "SerialNum:  " + localX509CertImpl.getSerialNumber().toString(16) + "\n";
    str = str + "Expires:    " + localX509CertImpl.getNotAfter().toString() + "\n";
    boolean[] arrayOfBoolean1 = localX509CertImpl.getIssuerUniqueID();
    if (arrayOfBoolean1 != null)
    {
      str = str + "IssuerUID:  ";
      for (int i = 0; i < arrayOfBoolean1.length; ++i)
        str = str + ((arrayOfBoolean1[i] != 0) ? 1 : 0);
      str = str + "\n";
    }
    boolean[] arrayOfBoolean2 = localX509CertImpl.getSubjectUniqueID();
    if (arrayOfBoolean2 != null)
    {
      str = str + "SubjectUID: ";
      for (int j = 0; j < arrayOfBoolean2.length; ++j)
        str = str + ((arrayOfBoolean2[j] != 0) ? 1 : 0);
      str = str + "\n";
    }
    SubjectKeyIdentifierExtension localSubjectKeyIdentifierExtension = null;
    try
    {
      localSubjectKeyIdentifierExtension = localX509CertImpl.getSubjectKeyIdentifierExtension();
      if (localSubjectKeyIdentifierExtension != null)
      {
        KeyIdentifier localKeyIdentifier1 = (KeyIdentifier)localSubjectKeyIdentifierExtension.get("key_id");
        str = str + "SubjKeyID:  " + localKeyIdentifier1.toString();
      }
    }
    catch (Exception localException1)
    {
      if (debug != null)
      {
        debug.println("Vertex.certToString() unexpected exception");
        localException1.printStackTrace();
      }
    }
    AuthorityKeyIdentifierExtension localAuthorityKeyIdentifierExtension = null;
    try
    {
      localAuthorityKeyIdentifierExtension = localX509CertImpl.getAuthorityKeyIdentifierExtension();
      if (localAuthorityKeyIdentifierExtension != null)
      {
        KeyIdentifier localKeyIdentifier2 = (KeyIdentifier)localAuthorityKeyIdentifierExtension.get("key_id");
        str = str + "AuthKeyID:  " + localKeyIdentifier2.toString();
      }
    }
    catch (Exception localException2)
    {
      if (debug != null)
      {
        debug.println("Vertex.certToString() 2 unexpected exception");
        localException2.printStackTrace();
      }
    }
    return str;
  }

  public String throwableToString()
  {
    String str = "Exception:  ";
    if (this.throwable != null)
      str = str + this.throwable.toString();
    else
      str = str + "null";
    str = str + "\n";
    return str;
  }

  public String moreToString()
  {
    String str = "Last cert?  ";
    str = str + ((this.index == -1) ? "Yes" : "No");
    str = str + "\n";
    return str;
  }

  public String indexToString()
  {
    String str = "Index:      " + this.index + "\n";
    return str;
  }
}
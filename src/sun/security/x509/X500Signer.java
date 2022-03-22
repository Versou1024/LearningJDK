package sun.security.x509;

import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.Signer;

public final class X500Signer extends Signer
{
  private static final long serialVersionUID = -8609982645394364834L;
  private Signature sig;
  private X500Name agent;
  private AlgorithmId algid;

  public void update(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws SignatureException
  {
    this.sig.update(paramArrayOfByte, paramInt1, paramInt2);
  }

  public byte[] sign()
    throws SignatureException
  {
    return this.sig.sign();
  }

  public AlgorithmId getAlgorithmId()
  {
    return this.algid;
  }

  public X500Name getSigner()
  {
    return this.agent;
  }

  public X500Signer(Signature paramSignature, X500Name paramX500Name)
  {
    if ((paramSignature == null) || (paramX500Name == null))
      throw new IllegalArgumentException("null parameter");
    this.sig = paramSignature;
    this.agent = paramX500Name;
    try
    {
      this.algid = AlgorithmId.getAlgorithmId(paramSignature.getAlgorithm());
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new RuntimeException("internal error! " + localNoSuchAlgorithmException.getMessage());
    }
  }
}
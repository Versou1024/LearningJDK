package sun.security.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.security.ProviderException;
import java.security.interfaces.DSAParams;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class AlgIdDSA extends AlgorithmId
  implements DSAParams
{
  private static final long serialVersionUID = 3437177836797504046L;
  private BigInteger p;
  private BigInteger q;
  private BigInteger g;

  public BigInteger getP()
  {
    return this.p;
  }

  public BigInteger getQ()
  {
    return this.q;
  }

  public BigInteger getG()
  {
    return this.g;
  }

  public AlgIdDSA()
  {
  }

  AlgIdDSA(DerValue paramDerValue)
    throws IOException
  {
    super(paramDerValue.getOID());
  }

  public AlgIdDSA(byte[] paramArrayOfByte)
    throws IOException
  {
    super(new DerValue(paramArrayOfByte).getOID());
  }

  public AlgIdDSA(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3)
    throws IOException
  {
    this(new BigInteger(1, paramArrayOfByte1), new BigInteger(1, paramArrayOfByte2), new BigInteger(1, paramArrayOfByte3));
  }

  public AlgIdDSA(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3)
  {
    super(DSA_oid);
    if ((paramBigInteger1 != null) || (paramBigInteger2 != null) || (paramBigInteger3 != null))
    {
      if ((paramBigInteger1 == null) || (paramBigInteger2 == null) || (paramBigInteger3 == null))
        throw new ProviderException("Invalid parameters for DSS/DSA Algorithm ID");
      try
      {
        this.p = paramBigInteger1;
        this.q = paramBigInteger2;
        this.g = paramBigInteger3;
        initializeParams();
      }
      catch (IOException localIOException)
      {
        throw new ProviderException("Construct DSS/DSA Algorithm ID");
      }
    }
  }

  public String getName()
  {
    return "DSA";
  }

  private void initializeParams()
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putInteger(this.p);
    localDerOutputStream.putInteger(this.q);
    localDerOutputStream.putInteger(this.g);
    this.params = new DerValue(48, localDerOutputStream.toByteArray());
  }

  protected void decodeParams()
    throws IOException
  {
    if (this.params == null)
      throw new IOException("DSA alg params are null");
    if (this.params.tag != 48)
      throw new IOException("DSA alg parsing error");
    this.params.data.reset();
    this.p = this.params.data.getBigInteger();
    this.q = this.params.data.getBigInteger();
    this.g = this.params.data.getBigInteger();
    if (this.params.data.available() != 0)
      throw new IOException("AlgIdDSA params, extra=" + this.params.data.available());
  }

  public String toString()
  {
    return paramsToString();
  }

  protected String paramsToString()
  {
    if (this.params == null)
      return " null\n";
    return "\n    p:\n" + Debug.toHexString(this.p) + "\n    q:\n" + Debug.toHexString(this.q) + "\n    g:\n" + Debug.toHexString(this.g) + "\n";
  }
}
package sun.security.rsa;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public abstract class RSASignature extends SignatureSpi
{
  private static final int baseLength = 8;
  private final ObjectIdentifier digestOID;
  private final int encodedLength;
  private final MessageDigest md;
  private boolean digestReset;
  private RSAPrivateKey privateKey;
  private RSAPublicKey publicKey;
  private RSAPadding padding;

  RSASignature(String paramString, ObjectIdentifier paramObjectIdentifier, int paramInt)
  {
    this.digestOID = paramObjectIdentifier;
    try
    {
      this.md = MessageDigest.getInstance(paramString);
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new ProviderException(localNoSuchAlgorithmException);
    }
    this.digestReset = true;
    this.encodedLength = (8 + paramInt + this.md.getDigestLength());
  }

  protected void engineInitVerify(PublicKey paramPublicKey)
    throws InvalidKeyException
  {
    RSAPublicKey localRSAPublicKey = (RSAPublicKey)RSAKeyFactory.toRSAKey(paramPublicKey);
    this.privateKey = null;
    this.publicKey = localRSAPublicKey;
    initCommon(localRSAPublicKey, null);
  }

  protected void engineInitSign(PrivateKey paramPrivateKey)
    throws InvalidKeyException
  {
    engineInitSign(paramPrivateKey, null);
  }

  protected void engineInitSign(PrivateKey paramPrivateKey, SecureRandom paramSecureRandom)
    throws InvalidKeyException
  {
    RSAPrivateKey localRSAPrivateKey = (RSAPrivateKey)RSAKeyFactory.toRSAKey(paramPrivateKey);
    this.privateKey = localRSAPrivateKey;
    this.publicKey = null;
    initCommon(localRSAPrivateKey, paramSecureRandom);
  }

  private void initCommon(RSAKey paramRSAKey, SecureRandom paramSecureRandom)
    throws InvalidKeyException
  {
    resetDigest();
    int i = RSACore.getByteLength(paramRSAKey);
    try
    {
      this.padding = RSAPadding.getInstance(1, i, paramSecureRandom);
    }
    catch (InvalidAlgorithmParameterException localInvalidAlgorithmParameterException)
    {
      throw new InvalidKeyException(localInvalidAlgorithmParameterException.getMessage());
    }
    int j = this.padding.getMaxDataSize();
    if (this.encodedLength > j)
      throw new InvalidKeyException("Key is too short for this signature algorithm");
  }

  private void resetDigest()
  {
    if (!(this.digestReset))
    {
      this.md.reset();
      this.digestReset = true;
    }
  }

  private byte[] getDigestValue()
  {
    this.digestReset = true;
    return this.md.digest();
  }

  protected void engineUpdate(byte paramByte)
    throws SignatureException
  {
    this.md.update(paramByte);
    this.digestReset = false;
  }

  protected void engineUpdate(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws SignatureException
  {
    this.md.update(paramArrayOfByte, paramInt1, paramInt2);
    this.digestReset = false;
  }

  protected void engineUpdate(ByteBuffer paramByteBuffer)
  {
    this.md.update(paramByteBuffer);
    this.digestReset = false;
  }

  protected byte[] engineSign()
    throws SignatureException
  {
    byte[] arrayOfByte1 = getDigestValue();
    try
    {
      byte[] arrayOfByte2 = encodeSignature(this.digestOID, arrayOfByte1);
      byte[] arrayOfByte3 = this.padding.pad(arrayOfByte2);
      byte[] arrayOfByte4 = RSACore.rsa(arrayOfByte3, this.privateKey);
      return arrayOfByte4;
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw new SignatureException("Could not sign data", localGeneralSecurityException);
    }
    catch (IOException localIOException)
    {
      throw new SignatureException("Could not encode data", localIOException);
    }
  }

  protected boolean engineVerify(byte[] paramArrayOfByte)
    throws SignatureException
  {
    byte[] arrayOfByte1 = getDigestValue();
    try
    {
      byte[] arrayOfByte2 = RSACore.rsa(paramArrayOfByte, this.publicKey);
      byte[] arrayOfByte3 = this.padding.unpad(arrayOfByte2);
      byte[] arrayOfByte4 = decodeSignature(this.digestOID, arrayOfByte3);
      return Arrays.equals(arrayOfByte1, arrayOfByte4);
    }
    catch (BadPaddingException localBadPaddingException)
    {
      return false;
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw new SignatureException("Signature verification failed", localGeneralSecurityException);
    }
    catch (IOException localIOException)
    {
      throw new SignatureException("Signature encoding error", localIOException);
    }
  }

  public static byte[] encodeSignature(ObjectIdentifier paramObjectIdentifier, byte[] paramArrayOfByte)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    new AlgorithmId(paramObjectIdentifier).encode(localDerOutputStream);
    localDerOutputStream.putOctetString(paramArrayOfByte);
    DerValue localDerValue = new DerValue(48, localDerOutputStream.toByteArray());
    return localDerValue.toByteArray();
  }

  public static byte[] decodeSignature(ObjectIdentifier paramObjectIdentifier, byte[] paramArrayOfByte)
    throws IOException
  {
    DerInputStream localDerInputStream = new DerInputStream(paramArrayOfByte);
    DerValue[] arrayOfDerValue = localDerInputStream.getSequence(2);
    if ((arrayOfDerValue.length != 2) || (localDerInputStream.available() != 0))
      throw new IOException("SEQUENCE length error");
    AlgorithmId localAlgorithmId = AlgorithmId.parse(arrayOfDerValue[0]);
    if (!(localAlgorithmId.getOID().equals(paramObjectIdentifier)))
      throw new IOException("ObjectIdentifier mismatch: " + localAlgorithmId.getOID());
    if (localAlgorithmId.getEncodedParams() != null)
      throw new IOException("Unexpected AlgorithmId parameters");
    byte[] arrayOfByte = arrayOfDerValue[1].getOctetString();
    return arrayOfByte;
  }

  protected void engineSetParameter(String paramString, Object paramObject)
    throws InvalidParameterException
  {
    throw new UnsupportedOperationException("setParameter() not supported");
  }

  protected Object engineGetParameter(String paramString)
    throws InvalidParameterException
  {
    throw new UnsupportedOperationException("getParameter() not supported");
  }

  public static final class MD2withRSA extends RSASignature
  {
    public MD2withRSA()
    {
      super("MD2", AlgorithmId.MD2_oid, 10);
    }
  }

  public static final class MD5withRSA extends RSASignature
  {
    public MD5withRSA()
    {
      super("MD5", AlgorithmId.MD5_oid, 10);
    }
  }

  public static final class SHA1withRSA extends RSASignature
  {
    public SHA1withRSA()
    {
      super("SHA-1", AlgorithmId.SHA_oid, 7);
    }
  }

  public static final class SHA256withRSA extends RSASignature
  {
    public SHA256withRSA()
    {
      super("SHA-256", AlgorithmId.SHA256_oid, 11);
    }
  }

  public static final class SHA384withRSA extends RSASignature
  {
    public SHA384withRSA()
    {
      super("SHA-384", AlgorithmId.SHA384_oid, 11);
    }
  }

  public static final class SHA512withRSA extends RSASignature
  {
    public SHA512withRSA()
    {
      super("SHA-512", AlgorithmId.SHA512_oid, 11);
    }
  }
}
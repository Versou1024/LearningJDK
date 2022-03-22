package sun.security.jgss.krb5;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import sun.security.jgss.GSSHeader;
import sun.security.jgss.GSSToken;
import sun.security.util.ObjectIdentifier;

abstract class MessageToken extends Krb5Token
{
  private static final int TOKEN_NO_CKSUM_SIZE = 16;
  private static final int FILLER = 65535;
  static final int SGN_ALG_DES_MAC_MD5 = 0;
  static final int SGN_ALG_DES_MAC = 512;
  static final int SGN_ALG_HMAC_SHA1_DES3_KD = 1024;
  static final int SEAL_ALG_NONE = 65535;
  static final int SEAL_ALG_DES = 0;
  static final int SEAL_ALG_DES3_KD = 512;
  static final int SEAL_ALG_ARCFOUR_HMAC = 4096;
  static final int SGN_ALG_HMAC_MD5_ARCFOUR = 4352;
  private static final int TOKEN_ID_POS = 0;
  private static final int SIGN_ALG_POS = 2;
  private static final int SEAL_ALG_POS = 4;
  private int seqNumber;
  private boolean confState;
  private boolean initiator;
  private int tokenId;
  private GSSHeader gssHeader;
  private MessageTokenHeader tokenHeader;
  private byte[] checksum;
  private byte[] encSeqNumber;
  private byte[] seqNumberData;
  CipherHelper cipherHelper;

  MessageToken(int paramInt1, Krb5Context paramKrb5Context, byte[] paramArrayOfByte, int paramInt2, int paramInt3, MessageProp paramMessageProp)
    throws GSSException
  {
    this(paramInt1, paramKrb5Context, new ByteArrayInputStream(paramArrayOfByte, paramInt2, paramInt3), paramMessageProp);
  }

  MessageToken(int paramInt, Krb5Context paramKrb5Context, InputStream paramInputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    this.confState = true;
    this.initiator = true;
    this.tokenId = 0;
    this.gssHeader = null;
    this.tokenHeader = null;
    this.checksum = null;
    this.encSeqNumber = null;
    this.seqNumberData = null;
    this.cipherHelper = null;
    init(paramInt, paramKrb5Context);
    try
    {
      this.gssHeader = new GSSHeader(paramInputStream);
      if (!(this.gssHeader.getOid().equals(OID)))
        throw new GSSException(10, -1, getTokenName(paramInt));
      if (!(this.confState))
        paramMessageProp.setPrivacy(false);
      this.tokenHeader = new MessageTokenHeader(this, paramInputStream, paramMessageProp);
      this.encSeqNumber = new byte[8];
      readFully(paramInputStream, this.encSeqNumber);
      this.checksum = new byte[this.cipherHelper.getChecksumLength()];
      readFully(paramInputStream, this.checksum);
    }
    catch (IOException localIOException)
    {
      throw new GSSException(10, -1, getTokenName(paramInt) + ":" + localIOException.getMessage());
    }
  }

  public final GSSHeader getGSSHeader()
  {
    return this.gssHeader;
  }

  public final int getTokenId()
  {
    return this.tokenId;
  }

  public final byte[] getEncSeqNumber()
  {
    return this.encSeqNumber;
  }

  public final byte[] getChecksum()
  {
    return this.checksum;
  }

  public final boolean getConfState()
  {
    return this.confState;
  }

  public void genSignAndSeqNumber(MessageProp paramMessageProp, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2, byte[] paramArrayOfByte3)
    throws GSSException
  {
    int i = paramMessageProp.getQOP();
    if (i != 0)
    {
      i = 0;
      paramMessageProp.setQOP(i);
    }
    if (!(this.confState))
      paramMessageProp.setPrivacy(false);
    this.tokenHeader = new MessageTokenHeader(this, this.tokenId, paramMessageProp.getPrivacy(), i);
    this.checksum = getChecksum(paramArrayOfByte1, paramArrayOfByte2, paramInt1, paramInt2, paramArrayOfByte3);
    this.seqNumberData = new byte[8];
    if (this.cipherHelper.isArcFour())
      writeBigEndian(this.seqNumber, this.seqNumberData);
    else
      writeLittleEndian(this.seqNumber, this.seqNumberData);
    if (!(this.initiator))
    {
      this.seqNumberData[4] = -1;
      this.seqNumberData[5] = -1;
      this.seqNumberData[6] = -1;
      this.seqNumberData[7] = -1;
    }
    this.encSeqNumber = this.cipherHelper.encryptSeq(this.checksum, this.seqNumberData, 0, 8);
  }

  public final boolean verifySignAndSeqNumber(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2, byte[] paramArrayOfByte3)
    throws GSSException
  {
    byte[] arrayOfByte = getChecksum(paramArrayOfByte1, paramArrayOfByte2, paramInt1, paramInt2, paramArrayOfByte3);
    if (!(MessageDigest.isEqual(this.checksum, arrayOfByte)))
      break label108;
    this.seqNumberData = this.cipherHelper.decryptSeq(this.checksum, this.encSeqNumber, 0, 8);
    int i = 0;
    if (this.initiator)
      i = -1;
    label108: return ((this.seqNumberData[4] == i) && (this.seqNumberData[5] == i) && (this.seqNumberData[6] == i) && (this.seqNumberData[7] == i));
  }

  public final int getSequenceNumber()
  {
    int i = 0;
    if (this.cipherHelper.isArcFour())
      i = readBigEndian(this.seqNumberData, 0, 4);
    else
      i = readLittleEndian(this.seqNumberData, 0, 4);
    return i;
  }

  private byte[] getChecksum(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2, byte[] paramArrayOfByte3)
    throws GSSException
  {
    byte[] arrayOfByte1 = this.tokenHeader.getBytes();
    byte[] arrayOfByte2 = paramArrayOfByte1;
    byte[] arrayOfByte3 = arrayOfByte1;
    if (arrayOfByte2 != null)
    {
      arrayOfByte3 = new byte[arrayOfByte1.length + arrayOfByte2.length];
      System.arraycopy(arrayOfByte1, 0, arrayOfByte3, 0, arrayOfByte1.length);
      System.arraycopy(arrayOfByte2, 0, arrayOfByte3, arrayOfByte1.length, arrayOfByte2.length);
    }
    return this.cipherHelper.calculateChecksum(this.tokenHeader.getSignAlg(), arrayOfByte3, paramArrayOfByte3, paramArrayOfByte2, paramInt1, paramInt2, this.tokenId);
  }

  MessageToken(int paramInt, Krb5Context paramKrb5Context)
    throws GSSException
  {
    this.confState = true;
    this.initiator = true;
    this.tokenId = 0;
    this.gssHeader = null;
    this.tokenHeader = null;
    this.checksum = null;
    this.encSeqNumber = null;
    this.seqNumberData = null;
    this.cipherHelper = null;
    init(paramInt, paramKrb5Context);
    this.seqNumber = paramKrb5Context.incrementMySequenceNumber();
  }

  private void init(int paramInt, Krb5Context paramKrb5Context)
    throws GSSException
  {
    this.tokenId = paramInt;
    this.confState = paramKrb5Context.getConfState();
    this.initiator = paramKrb5Context.isInitiator();
    this.cipherHelper = paramKrb5Context.getCipherHelper(null);
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException, GSSException
  {
    this.gssHeader = new GSSHeader(OID, getKrb5TokenSize());
    this.gssHeader.encode(paramOutputStream);
    this.tokenHeader.encode(paramOutputStream);
    paramOutputStream.write(this.encSeqNumber);
    paramOutputStream.write(this.checksum);
  }

  protected int getKrb5TokenSize()
    throws GSSException
  {
    return getTokenSize();
  }

  protected final int getTokenSize()
    throws GSSException
  {
    return (16 + this.cipherHelper.getChecksumLength());
  }

  protected static final int getTokenSize(CipherHelper paramCipherHelper)
    throws GSSException
  {
    return (16 + paramCipherHelper.getChecksumLength());
  }

  protected abstract int getSealAlg(boolean paramBoolean, int paramInt)
    throws GSSException;

  protected int getSgnAlg(int paramInt)
    throws GSSException
  {
    return this.cipherHelper.getSgnAlg();
  }

  class MessageTokenHeader
  {
    private int tokenId;
    private int signAlg;
    private int sealAlg;
    private byte[] bytes = new byte[8];

    public MessageTokenHeader(, int paramInt1, boolean paramBoolean, int paramInt2)
      throws GSSException
    {
      this.tokenId = paramInt1;
      this.signAlg = paramMessageToken.getSgnAlg(paramInt2);
      this.sealAlg = paramMessageToken.getSealAlg(paramBoolean, paramInt2);
      this.bytes[0] = (byte)(paramInt1 >>> 8);
      this.bytes[1] = (byte)paramInt1;
      this.bytes[2] = (byte)(this.signAlg >>> 8);
      this.bytes[3] = (byte)this.signAlg;
      this.bytes[4] = (byte)(this.sealAlg >>> 8);
      this.bytes[5] = (byte)this.sealAlg;
      this.bytes[6] = -1;
      this.bytes[7] = -1;
    }

    public MessageTokenHeader(, InputStream paramInputStream, MessageProp paramMessageProp)
      throws IOException
    {
      GSSToken.readFully(paramInputStream, this.bytes);
      this.tokenId = GSSToken.readInt(this.bytes, 0);
      this.signAlg = GSSToken.readInt(this.bytes, 2);
      this.sealAlg = GSSToken.readInt(this.bytes, 4);
      int i = GSSToken.readInt(this.bytes, 6);
      switch (this.sealAlg)
      {
      case 0:
      case 512:
      case 4096:
        paramMessageProp.setPrivacy(true);
        break;
      default:
        paramMessageProp.setPrivacy(false);
      }
      paramMessageProp.setQOP(0);
    }

    public final void encode()
      throws IOException
    {
      paramOutputStream.write(this.bytes);
    }

    public final int getTokenId()
    {
      return this.tokenId;
    }

    public final int getSignAlg()
    {
      return this.signAlg;
    }

    public final int getSealAlg()
    {
      return this.sealAlg;
    }

    public final byte[] getBytes()
    {
      return this.bytes;
    }
  }
}
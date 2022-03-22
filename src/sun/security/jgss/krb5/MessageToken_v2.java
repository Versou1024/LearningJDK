package sun.security.jgss.krb5;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import sun.security.jgss.GSSToken;

abstract class MessageToken_v2 extends Krb5Token
{
  private static final int TOKEN_ID_POS = 0;
  private static final int TOKEN_FLAG_POS = 2;
  private static final int TOKEN_EC_POS = 4;
  private static final int TOKEN_RRC_POS = 6;
  static final int TOKEN_HEADER_SIZE = 16;
  private int tokenId;
  private int seqNumber;
  private int ec;
  private int rrc;
  private boolean confState;
  private boolean initiator;
  byte[] confounder;
  byte[] checksum;
  private int key_usage;
  private byte[] seqNumberData;
  private MessageTokenHeader tokenHeader;
  CipherHelper cipherHelper;
  static final int KG_USAGE_ACCEPTOR_SEAL = 22;
  static final int KG_USAGE_ACCEPTOR_SIGN = 23;
  static final int KG_USAGE_INITIATOR_SEAL = 24;
  static final int KG_USAGE_INITIATOR_SIGN = 25;
  private static final int FLAG_SENDER_IS_ACCEPTOR = 1;
  private static final int FLAG_WRAP_CONFIDENTIAL = 2;
  private static final int FLAG_ACCEPTOR_SUBKEY = 4;
  private static final int FILLER = 255;

  MessageToken_v2(int paramInt1, Krb5Context paramKrb5Context, byte[] paramArrayOfByte, int paramInt2, int paramInt3, MessageProp paramMessageProp)
    throws GSSException
  {
    this(paramInt1, paramKrb5Context, new ByteArrayInputStream(paramArrayOfByte, paramInt2, paramInt3), paramMessageProp);
  }

  MessageToken_v2(int paramInt, Krb5Context paramKrb5Context, InputStream paramInputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    this.tokenId = 0;
    this.ec = 0;
    this.rrc = 0;
    this.confState = true;
    this.initiator = true;
    this.confounder = null;
    this.checksum = null;
    this.key_usage = 0;
    this.seqNumberData = null;
    this.tokenHeader = null;
    this.cipherHelper = null;
    init(paramInt, paramKrb5Context);
    try
    {
      if (!(this.confState))
        paramMessageProp.setPrivacy(false);
      this.tokenHeader = new MessageTokenHeader(this, paramInputStream, paramMessageProp, paramInt);
      if (paramInt == 1284)
        this.key_usage = ((!(this.initiator)) ? 24 : 22);
      else if (paramInt == 1028)
        this.key_usage = ((!(this.initiator)) ? 25 : 23);
      int i = paramInputStream.available();
      byte[] arrayOfByte = new byte[i];
      readFully(paramInputStream, arrayOfByte);
      this.checksum = new byte[this.cipherHelper.getChecksumLength()];
      System.arraycopy(arrayOfByte, i - this.cipherHelper.getChecksumLength(), this.checksum, 0, this.cipherHelper.getChecksumLength());
      if ((!(paramMessageProp.getPrivacy())) && (paramInt == 1284) && (this.checksum.length != this.ec))
        throw new GSSException(10, -1, getTokenName(paramInt) + ":" + "EC incorrect!");
    }
    catch (IOException localIOException)
    {
      throw new GSSException(10, -1, getTokenName(paramInt) + ":" + localIOException.getMessage());
    }
  }

  public final int getTokenId()
  {
    return this.tokenId;
  }

  public final int getKeyUsage()
  {
    return this.key_usage;
  }

  public final boolean getConfState()
  {
    return this.confState;
  }

  public void genSignAndSeqNumber(MessageProp paramMessageProp, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
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
    this.tokenHeader = new MessageTokenHeader(this, this.tokenId, paramMessageProp.getPrivacy(), true);
    if (this.tokenId == 1284)
      this.key_usage = ((this.initiator) ? 24 : 22);
    else if (this.tokenId == 1028)
      this.key_usage = ((this.initiator) ? 25 : 23);
    if ((this.tokenId == 1028) || ((!(paramMessageProp.getPrivacy())) && (this.tokenId == 1284)))
      this.checksum = getChecksum(paramArrayOfByte, paramInt1, paramInt2);
    if ((!(paramMessageProp.getPrivacy())) && (this.tokenId == 1284))
    {
      byte[] arrayOfByte = this.tokenHeader.getBytes();
      arrayOfByte[4] = (byte)(this.checksum.length >>> 8);
      arrayOfByte[5] = (byte)this.checksum.length;
    }
  }

  public final boolean verifySign(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws GSSException
  {
    byte[] arrayOfByte = getChecksum(paramArrayOfByte, paramInt1, paramInt2);
    return (MessageDigest.isEqual(this.checksum, arrayOfByte));
  }

  public boolean rotate_left(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2)
  {
    int i = 0;
    if (this.rrc > 0)
    {
      if (paramInt2 == 0)
        return false;
      this.rrc %= (paramInt2 - 16);
      if (this.rrc == 0)
        return false;
      if (paramInt1 > 0)
        i += paramInt1;
      System.arraycopy(paramArrayOfByte1, i, paramArrayOfByte2, 0, 16);
      System.arraycopy(paramArrayOfByte1, (i += 16) + this.rrc, paramArrayOfByte2, 16, paramInt2 - 16 - this.rrc);
      System.arraycopy(paramArrayOfByte1, i, paramArrayOfByte2, paramInt2 - 16 - this.rrc, this.rrc);
      return true;
    }
    return false;
  }

  public final int getSequenceNumber()
  {
    return readBigEndian(this.seqNumberData, 0, 4);
  }

  byte[] getChecksum(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws GSSException
  {
    byte[] arrayOfByte = this.tokenHeader.getBytes();
    int i = arrayOfByte[2] & 0x2;
    if ((i == 0) && (this.tokenId == 1284))
    {
      arrayOfByte[4] = 0;
      arrayOfByte[5] = 0;
    }
    return this.cipherHelper.calculateChecksum(arrayOfByte, paramArrayOfByte, paramInt1, paramInt2, this.key_usage);
  }

  MessageToken_v2(int paramInt, Krb5Context paramKrb5Context)
    throws GSSException
  {
    this.tokenId = 0;
    this.ec = 0;
    this.rrc = 0;
    this.confState = true;
    this.initiator = true;
    this.confounder = null;
    this.checksum = null;
    this.key_usage = 0;
    this.seqNumberData = null;
    this.tokenHeader = null;
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
    this.tokenId = paramInt;
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException, GSSException
  {
    this.tokenHeader.encode(paramOutputStream);
    if (this.tokenId == 1028)
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

  protected final byte[] getTokenHeader()
  {
    return this.tokenHeader.getBytes();
  }

  class MessageTokenHeader
  {
    private int tokenId;
    private byte[] bytes = new byte[16];

    public MessageTokenHeader(, int paramInt, boolean paramBoolean1, boolean paramBoolean2)
      throws GSSException
    {
      this.tokenId = paramInt;
      this.bytes[0] = (byte)(paramInt >>> 8);
      this.bytes[1] = (byte)paramInt;
      int i = 0;
      i = ((MessageToken_v2.access$000(paramMessageToken_v2)) ? 0 : 1) | (((paramBoolean1) && (paramInt != 1028)) ? 2 : 0) | ((paramBoolean2) ? 4 : 0);
      this.bytes[2] = (byte)i;
      this.bytes[3] = -1;
      if (paramInt == 1284)
      {
        this.bytes[4] = 0;
        this.bytes[5] = 0;
        this.bytes[6] = 0;
        this.bytes[7] = 0;
      }
      else if (paramInt == 1028)
      {
        for (int j = 4; j < 8; ++j)
          this.bytes[j] = -1;
      }
      MessageToken_v2.access$102(paramMessageToken_v2, new byte[8]);
      GSSToken.writeBigEndian(MessageToken_v2.access$200(paramMessageToken_v2), MessageToken_v2.access$100(paramMessageToken_v2), 4);
      System.arraycopy(MessageToken_v2.access$100(paramMessageToken_v2), 0, this.bytes, 8, 8);
    }

    public MessageTokenHeader(, InputStream paramInputStream, MessageProp paramMessageProp, int paramInt)
      throws IOException, GSSException
    {
      GSSToken.readFully(paramInputStream, this.bytes, 0, 16);
      this.tokenId = GSSToken.readInt(this.bytes, 0);
      int i = (MessageToken_v2.access$000(paramMessageToken_v2)) ? 1 : 0;
      int j = this.bytes[2] & 0x1;
      if (j != i)
        throw new GSSException(10, -1, Krb5Token.getTokenName(this.tokenId) + ":" + "Acceptor Flag Missing!");
      int k = this.bytes[2] & 0x2;
      if ((k == 2) && (this.tokenId == 1284))
        paramMessageProp.setPrivacy(true);
      else
        paramMessageProp.setPrivacy(false);
      if (this.tokenId != paramInt)
        throw new GSSException(10, -1, Krb5Token.getTokenName(this.tokenId) + ":" + "Defective Token ID!");
      if ((this.bytes[3] & 0xFF) != 255)
        throw new GSSException(10, -1, Krb5Token.getTokenName(this.tokenId) + ":" + "Defective Token Filler!");
      if (this.tokenId == 1028)
        for (int l = 4; l < 8; ++l)
          if ((this.bytes[l] & 0xFF) != 255)
            throw new GSSException(10, -1, Krb5Token.getTokenName(this.tokenId) + ":" + "Defective Token Filler!");
      MessageToken_v2.access$302(paramMessageToken_v2, GSSToken.readBigEndian(this.bytes, 4, 2));
      MessageToken_v2.access$402(paramMessageToken_v2, GSSToken.readBigEndian(this.bytes, 6, 2));
      paramMessageProp.setQOP(0);
      MessageToken_v2.access$102(paramMessageToken_v2, new byte[8]);
      System.arraycopy(this.bytes, 8, MessageToken_v2.access$100(paramMessageToken_v2), 0, 8);
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

    public final byte[] getBytes()
    {
      return this.bytes;
    }
  }
}
package sun.security.jgss.krb5;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import sun.security.jgss.GSSHeader;
import sun.security.krb5.Confounder;
import sun.security.krb5.KrbException;

class WrapToken_v2 extends MessageToken_v2
{
  static final int CONFOUNDER_SIZE = 16;
  private boolean readTokenFromInputStream = true;
  private InputStream is = null;
  private byte[] tokenBytes = null;
  private int tokenOffset = 0;
  private int tokenLen = 0;
  private byte[] dataBytes = null;
  private int dataOffset = 0;
  private int dataLen = 0;
  private int dataSize = 0;
  byte[] confounder = null;
  private boolean privacy = false;
  private boolean initiator = true;

  public WrapToken_v2(Krb5Context paramKrb5Context, byte[] paramArrayOfByte, int paramInt1, int paramInt2, MessageProp paramMessageProp)
    throws GSSException
  {
    super(1284, paramKrb5Context, paramArrayOfByte, paramInt1, paramInt2, paramMessageProp);
    this.readTokenFromInputStream = false;
    byte[] arrayOfByte = new byte[paramInt2];
    if (rotate_left(paramArrayOfByte, paramInt1, arrayOfByte, paramInt2))
    {
      this.tokenBytes = arrayOfByte;
      this.tokenOffset = 0;
    }
    else
    {
      this.tokenBytes = paramArrayOfByte;
      this.tokenOffset = paramInt1;
    }
    this.tokenLen = paramInt2;
    this.privacy = paramMessageProp.getPrivacy();
    this.dataSize = (paramInt2 - 16);
    this.initiator = paramKrb5Context.isInitiator();
  }

  public WrapToken_v2(Krb5Context paramKrb5Context, InputStream paramInputStream, MessageProp paramMessageProp)
    throws GSSException
  {
    super(1284, paramKrb5Context, paramInputStream, paramMessageProp);
    this.is = paramInputStream;
    this.privacy = paramMessageProp.getPrivacy();
    try
    {
      this.tokenLen = paramInputStream.available();
    }
    catch (IOException localIOException)
    {
      throw new GSSException(10, -1, getTokenName(getTokenId()) + ": " + localIOException.getMessage());
    }
    this.dataSize = (this.tokenLen - 16);
    this.initiator = paramKrb5Context.isInitiator();
  }

  public byte[] getData()
    throws GSSException
  {
    byte[] arrayOfByte1 = new byte[this.dataSize];
    int i = getData(arrayOfByte1, 0);
    byte[] arrayOfByte2 = new byte[i];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte2.length);
    return arrayOfByte2;
  }

  public int getData(byte[] paramArrayOfByte, int paramInt)
    throws GSSException
  {
    if (this.readTokenFromInputStream)
      getDataFromStream(paramArrayOfByte, paramInt);
    else
      getDataFromBuffer(paramArrayOfByte, paramInt);
    int i = 0;
    if (this.privacy)
      i = this.dataSize - this.confounder.length - 16 - this.cipherHelper.getChecksumLength();
    else
      i = this.dataSize - this.cipherHelper.getChecksumLength();
    return i;
  }

  private void getDataFromBuffer(byte[] paramArrayOfByte, int paramInt)
    throws GSSException
  {
    int i = this.tokenOffset + 16;
    int j = 0;
    if (i + this.dataSize > this.tokenOffset + this.tokenLen)
      throw new GSSException(10, -1, "Insufficient data in " + getTokenName(getTokenId()));
    this.confounder = new byte[16];
    if (this.privacy)
    {
      this.cipherHelper.decryptData(this, this.tokenBytes, i, this.dataSize, paramArrayOfByte, paramInt, getKeyUsage());
      j = this.dataSize - 16 - 16 - this.cipherHelper.getChecksumLength();
    }
    else
    {
      debug("\t\tNo encryption was performed by peer.\n");
      j = this.dataSize - this.cipherHelper.getChecksumLength();
      System.arraycopy(this.tokenBytes, i, paramArrayOfByte, paramInt, j);
      if (!(verifySign(paramArrayOfByte, paramInt, j)))
        throw new GSSException(6, -1, "Corrupt checksum in Wrap token");
    }
  }

  private void getDataFromStream(byte[] paramArrayOfByte, int paramInt)
    throws GSSException
  {
    int i = 0;
    this.confounder = new byte[16];
    try
    {
      if (this.privacy)
      {
        this.cipherHelper.decryptData(this, this.is, this.dataSize, paramArrayOfByte, paramInt, getKeyUsage());
        i = this.dataSize - 16 - 16 - this.cipherHelper.getChecksumLength();
      }
      else
      {
        debug("\t\tNo encryption was performed by peer.\n");
        readFully(this.is, this.confounder);
        i = this.dataSize - this.cipherHelper.getChecksumLength();
        readFully(this.is, paramArrayOfByte, paramInt, i);
        if (!(verifySign(paramArrayOfByte, paramInt, i)))
          throw new GSSException(6, -1, "Corrupt checksum in Wrap token");
      }
    }
    catch (IOException localIOException)
    {
      throw new GSSException(10, -1, getTokenName(getTokenId()) + ": " + localIOException.getMessage());
    }
  }

  public WrapToken_v2(Krb5Context paramKrb5Context, MessageProp paramMessageProp, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws GSSException
  {
    super(1284, paramKrb5Context);
    try
    {
      new Confounder();
      this.confounder = Confounder.bytes(16);
    }
    catch (KrbException localKrbException)
    {
      throw new GSSException(11, -1, localKrbException.getMessage());
    }
    this.dataSize = (this.confounder.length + paramInt2 + 16 + this.cipherHelper.getChecksumLength());
    this.dataBytes = paramArrayOfByte;
    this.dataOffset = paramInt1;
    this.dataLen = paramInt2;
    this.initiator = paramKrb5Context.isInitiator();
    genSignAndSeqNumber(paramMessageProp, paramArrayOfByte, paramInt1, paramInt2);
    if (!(paramKrb5Context.getConfState()))
      paramMessageProp.setPrivacy(false);
    this.privacy = paramMessageProp.getPrivacy();
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException, GSSException
  {
    super.encode(paramOutputStream);
    if (!(this.privacy))
    {
      byte[] arrayOfByte = getChecksum(this.dataBytes, this.dataOffset, this.dataLen);
      paramOutputStream.write(this.dataBytes, this.dataOffset, this.dataLen);
      paramOutputStream.write(arrayOfByte);
    }
    else
    {
      this.cipherHelper.encryptData(this, this.confounder, getTokenHeader(), this.dataBytes, this.dataOffset, this.dataLen, getKeyUsage(), paramOutputStream);
    }
  }

  public byte[] encode()
    throws IOException, GSSException
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(this.dataSize + 50);
    encode(localByteArrayOutputStream);
    return localByteArrayOutputStream.toByteArray();
  }

  public int encode(byte[] paramArrayOfByte, int paramInt)
    throws IOException, GSSException
  {
    int i = 0;
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    super.encode(localByteArrayOutputStream);
    byte[] arrayOfByte1 = localByteArrayOutputStream.toByteArray();
    System.arraycopy(arrayOfByte1, 0, paramArrayOfByte, paramInt, arrayOfByte1.length);
    paramInt += arrayOfByte1.length;
    if (!(this.privacy))
    {
      byte[] arrayOfByte2 = getChecksum(this.dataBytes, this.dataOffset, this.dataLen);
      System.arraycopy(this.dataBytes, this.dataOffset, paramArrayOfByte, paramInt, this.dataLen);
      paramInt += this.dataLen;
      System.arraycopy(arrayOfByte2, 0, paramArrayOfByte, paramInt, this.cipherHelper.getChecksumLength());
      i = arrayOfByte1.length + this.dataLen + this.cipherHelper.getChecksumLength();
    }
    else
    {
      int j = this.cipherHelper.encryptData(this, this.confounder, getTokenHeader(), this.dataBytes, this.dataOffset, this.dataLen, paramArrayOfByte, paramInt, getKeyUsage());
      i = arrayOfByte1.length + j;
    }
    return i;
  }

  protected int getKrb5TokenSize()
    throws GSSException
  {
    return (getTokenSize() + this.dataSize);
  }

  static int getSizeLimit(int paramInt1, boolean paramBoolean, int paramInt2, CipherHelper paramCipherHelper)
    throws GSSException
  {
    return (GSSHeader.getMaxMechTokenSize(OID, paramInt2) - getTokenSize(paramCipherHelper) + 16 - 8);
  }
}
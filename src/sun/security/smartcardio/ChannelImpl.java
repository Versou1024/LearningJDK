package sun.security.smartcardio;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.AccessController;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import sun.security.action.GetPropertyAction;

final class ChannelImpl extends CardChannel
{
  private final CardImpl card;
  private final int channel;
  private volatile boolean isClosed;
  private static final boolean t0GetResponse = getBooleanProperty("sun.security.smartcardio.t0GetResponse", true);
  private static final boolean t1GetResponse = getBooleanProperty("sun.security.smartcardio.t1GetResponse", true);
  private static final boolean t1StripLe = getBooleanProperty("sun.security.smartcardio.t1StripLe", false);
  private static final byte[] B0 = new byte[0];

  ChannelImpl(CardImpl paramCardImpl, int paramInt)
  {
    this.card = paramCardImpl;
    this.channel = paramInt;
  }

  void checkClosed()
  {
    this.card.checkState();
    if (this.isClosed)
      throw new IllegalStateException("Logical channel has been closed");
  }

  public Card getCard()
  {
    return this.card;
  }

  public int getChannelNumber()
  {
    checkClosed();
    return this.channel;
  }

  private static void checkManageChannel(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte.length < 4)
      throw new IllegalArgumentException("Command APDU must be at least 4 bytes long");
    if ((paramArrayOfByte[0] >= 0) && (paramArrayOfByte[1] == 112))
      throw new IllegalArgumentException("Manage channel command not allowed, use openLogicalChannel()");
  }

  public ResponseAPDU transmit(CommandAPDU paramCommandAPDU)
    throws CardException
  {
    checkClosed();
    this.card.checkExclusive();
    byte[] arrayOfByte1 = paramCommandAPDU.getBytes();
    byte[] arrayOfByte2 = doTransmit(arrayOfByte1);
    return new ResponseAPDU(arrayOfByte2);
  }

  public int transmit(ByteBuffer paramByteBuffer1, ByteBuffer paramByteBuffer2)
    throws CardException
  {
    checkClosed();
    this.card.checkExclusive();
    if ((paramByteBuffer1 == null) || (paramByteBuffer2 == null))
      throw new NullPointerException();
    if (paramByteBuffer2.isReadOnly())
      throw new ReadOnlyBufferException();
    if (paramByteBuffer1 == paramByteBuffer2)
      throw new IllegalArgumentException("command and response must not be the same object");
    if (paramByteBuffer2.remaining() < 258)
      throw new IllegalArgumentException("Insufficient space in response buffer");
    byte[] arrayOfByte1 = new byte[paramByteBuffer1.remaining()];
    paramByteBuffer1.get(arrayOfByte1);
    byte[] arrayOfByte2 = doTransmit(arrayOfByte1);
    paramByteBuffer2.put(arrayOfByte2);
    return arrayOfByte2.length;
  }

  private static boolean getBooleanProperty(String paramString, boolean paramBoolean)
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction(paramString));
    if (str == null)
      return paramBoolean;
    if (str.equalsIgnoreCase("true"))
      return true;
    if (str.equalsIgnoreCase("false"))
      return false;
    throw new IllegalArgumentException(paramString + " must be either 'true' or 'false'");
  }

  private byte[] concat(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt)
  {
    int i = paramArrayOfByte1.length;
    if ((i == 0) && (paramInt == paramArrayOfByte2.length))
      return paramArrayOfByte2;
    byte[] arrayOfByte = new byte[i + paramInt];
    System.arraycopy(paramArrayOfByte1, 0, arrayOfByte, 0, i);
    System.arraycopy(paramArrayOfByte2, 0, arrayOfByte, i, paramInt);
    return arrayOfByte;
  }

  private byte[] doTransmit(byte[] paramArrayOfByte)
    throws CardException
  {
    try
    {
      byte[] arrayOfByte2;
      int i2;
      checkManageChannel(paramArrayOfByte);
      setChannel(paramArrayOfByte);
      int i = paramArrayOfByte.length;
      int j = (this.card.protocol == 1) ? 1 : 0;
      int k = (this.card.protocol == 2) ? 1 : 0;
      if ((j != 0) && (i >= 7) && (paramArrayOfByte[4] == 0))
        throw new CardException("Extended length forms not supported for T=0");
      if ((((j != 0) || ((k != 0) && (t1StripLe)))) && (i >= 7))
      {
        l = paramArrayOfByte[4] & 0xFF;
        if (l != 0)
        {
          if (i == l + 6)
            --i;
        }
        else
        {
          l = (paramArrayOfByte[5] & 0xFF) << 8 | paramArrayOfByte[6] & 0xFF;
          if (i == l + 9)
            i -= 2;
        }
      }
      int l = (((j != 0) && (t0GetResponse)) || ((k != 0) && (t1GetResponse))) ? 1 : 0;
      int i1 = 0;
      byte[] arrayOfByte1 = B0;
      while (true)
      {
        while (true)
        {
          if (++i1 >= 32)
            throw new CardException("Could not obtain response");
          arrayOfByte2 = PCSC.SCardTransmit(this.card.cardId, this.card.protocol, paramArrayOfByte, 0, i);
          i2 = arrayOfByte2.length;
          if ((l == 0) || (i2 < 2))
            break label337;
          if ((i2 != 2) || (arrayOfByte2[0] != 108))
            break;
          paramArrayOfByte[(i - 1)] = arrayOfByte2[1];
        }
        if (arrayOfByte2[(i2 - 2)] != 97)
          break;
        if (i2 > 2)
          arrayOfByte1 = concat(arrayOfByte1, arrayOfByte2, i2 - 2);
        paramArrayOfByte[1] = -64;
        paramArrayOfByte[2] = 0;
        paramArrayOfByte[3] = 0;
        paramArrayOfByte[4] = arrayOfByte2[(i2 - 1)];
        i = 5;
      }
      label337: arrayOfByte1 = concat(arrayOfByte1, arrayOfByte2, i2);
      return arrayOfByte1;
    }
    catch (PCSCException localPCSCException)
    {
      this.card.handleError(localPCSCException);
      throw new CardException(localPCSCException);
    }
  }

  private static int getSW(byte[] paramArrayOfByte)
    throws CardException
  {
    if (paramArrayOfByte.length < 2)
      throw new CardException("Invalid response length: " + paramArrayOfByte.length);
    int i = paramArrayOfByte[(paramArrayOfByte.length - 2)] & 0xFF;
    int j = paramArrayOfByte[(paramArrayOfByte.length - 1)] & 0xFF;
    return (i << 8 | j);
  }

  private static boolean isOK(byte[] paramArrayOfByte)
    throws CardException
  {
    return ((paramArrayOfByte.length == 2) && (getSW(paramArrayOfByte) == 36864));
  }

  private void setChannel(byte[] paramArrayOfByte)
  {
    int i = paramArrayOfByte[0];
    if (i < 0)
      return;
    if ((i & 0xE0) == 32)
      return;
    if (this.channel <= 3)
    {
      int tmp30_29 = 0;
      byte[] tmp30_28 = paramArrayOfByte;
      tmp30_28[tmp30_29] = (byte)(tmp30_28[tmp30_29] & 0xBC);
      int tmp40_39 = 0;
      byte[] tmp40_38 = paramArrayOfByte;
      tmp40_38[tmp40_39] = (byte)(tmp40_38[tmp40_39] | this.channel);
    }
    else if (this.channel <= 19)
    {
      int tmp63_62 = 0;
      byte[] tmp63_61 = paramArrayOfByte;
      tmp63_61[tmp63_62] = (byte)(tmp63_61[tmp63_62] & 0xB0);
      int tmp73_72 = 0;
      byte[] tmp73_71 = paramArrayOfByte;
      tmp73_71[tmp73_72] = (byte)(tmp73_71[tmp73_72] | 0x40);
      int tmp82_81 = 0;
      byte[] tmp82_80 = paramArrayOfByte;
      tmp82_80[tmp82_81] = (byte)(tmp82_80[tmp82_81] | this.channel - 4);
    }
    else
    {
      throw new RuntimeException("Unsupported channel number: " + this.channel);
    }
  }

  public void close()
    throws CardException
  {
    if (getChannelNumber() == 0)
      throw new IllegalStateException("Cannot close basic logical channel");
    if (this.isClosed)
      return;
    this.card.checkExclusive();
    try
    {
      byte[] arrayOfByte1 = { 0, 112, -128, 0 };
      arrayOfByte1[3] = (byte)getChannelNumber();
      setChannel(arrayOfByte1);
      byte[] arrayOfByte2 = PCSC.SCardTransmit(this.card.cardId, this.card.protocol, arrayOfByte1, 0, arrayOfByte1.length);
      if (!(isOK(arrayOfByte2)))
        throw new CardException("close() failed: " + PCSC.toString(arrayOfByte2));
    }
    catch (PCSCException localPCSCException)
    {
      throw new CardException("Could not close channel", localPCSCException);
    }
    finally
    {
      this.isClosed = true;
    }
  }

  public String toString()
  {
    return "PC/SC channel " + this.channel;
  }
}
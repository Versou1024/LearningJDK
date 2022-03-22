package sun.security.smartcardio;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardPermission;

final class CardImpl extends Card
{
  private final TerminalImpl terminal;
  final long cardId;
  private final ATR atr;
  final int protocol;
  private final ChannelImpl basicChannel;
  private volatile State state;
  private volatile Thread exclusiveThread;
  private static byte[] commandOpenChannel = { 0, 112, 0, 0, 1 };

  CardImpl(TerminalImpl paramTerminalImpl, String paramString)
    throws PCSCException
  {
    this.terminal = paramTerminalImpl;
    int i = 2;
    if (paramString.equals("*"))
    {
      j = 3;
    }
    else if (paramString.equalsIgnoreCase("T=0"))
    {
      j = 1;
    }
    else if (paramString.equalsIgnoreCase("T=1"))
    {
      j = 2;
    }
    else if (paramString.equalsIgnoreCase("direct"))
    {
      j = 0;
      i = 3;
    }
    else
    {
      throw new IllegalArgumentException("Unsupported protocol " + paramString);
    }
    this.cardId = PCSC.SCardConnect(paramTerminalImpl.contextId, paramTerminalImpl.name, i, j);
    byte[] arrayOfByte1 = new byte[2];
    byte[] arrayOfByte2 = PCSC.SCardStatus(this.cardId, arrayOfByte1);
    this.atr = new ATR(arrayOfByte2);
    this.protocol = (arrayOfByte1[1] & 0xFF);
    this.basicChannel = new ChannelImpl(this, 0);
    this.state = State.OK;
  }

  void checkState()
  {
    State localState = this.state;
    if (localState == State.DISCONNECTED)
      throw new IllegalStateException("Card has been disconnected");
    if (localState == State.REMOVED)
      throw new IllegalStateException("Card has been removed");
  }

  boolean isValid()
  {
    if (this.state != State.OK)
      return false;
    try
    {
      PCSC.SCardStatus(this.cardId, new byte[2]);
      return true;
    }
    catch (PCSCException localPCSCException)
    {
      this.state = State.REMOVED;
    }
    return false;
  }

  private void checkSecurity(String paramString)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPermission(new CardPermission(this.terminal.name, paramString));
  }

  void handleError(PCSCException paramPCSCException)
  {
    if (paramPCSCException.code == -2146434967)
      this.state = State.REMOVED;
  }

  public ATR getATR()
  {
    return this.atr;
  }

  public String getProtocol()
  {
    switch (this.protocol)
    {
    case 1:
      return "T=0";
    case 2:
      return "T=1";
    }
    return "Unknown protocol " + this.protocol;
  }

  public CardChannel getBasicChannel()
  {
    checkSecurity("getBasicChannel");
    checkState();
    return this.basicChannel;
  }

  private static int getSW(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte.length < 2)
      return -1;
    int i = paramArrayOfByte[(paramArrayOfByte.length - 2)] & 0xFF;
    int j = paramArrayOfByte[(paramArrayOfByte.length - 1)] & 0xFF;
    return (i << 8 | j);
  }

  public CardChannel openLogicalChannel()
    throws CardException
  {
    checkSecurity("openLogicalChannel");
    checkState();
    checkExclusive();
    try
    {
      byte[] arrayOfByte = PCSC.SCardTransmit(this.cardId, this.protocol, commandOpenChannel, 0, commandOpenChannel.length);
      if ((arrayOfByte.length != 3) || (getSW(arrayOfByte) != 36864))
        throw new CardException("openLogicalChannel() failed, card response: " + PCSC.toString(arrayOfByte));
      return new ChannelImpl(this, arrayOfByte[0]);
    }
    catch (PCSCException localPCSCException)
    {
      handleError(localPCSCException);
      throw new CardException("openLogicalChannel() failed", localPCSCException);
    }
  }

  void checkExclusive()
    throws CardException
  {
    Thread localThread = this.exclusiveThread;
    if (localThread == null)
      return;
    if (localThread != Thread.currentThread())
      throw new CardException("Exclusive access established by another Thread");
  }

  public synchronized void beginExclusive()
    throws CardException
  {
    checkSecurity("exclusive");
    checkState();
    if (this.exclusiveThread != null)
      throw new CardException("Exclusive access has already been assigned to Thread " + this.exclusiveThread.getName());
    try
    {
      PCSC.SCardBeginTransaction(this.cardId);
    }
    catch (PCSCException localPCSCException)
    {
      handleError(localPCSCException);
      throw new CardException("beginExclusive() failed", localPCSCException);
    }
    this.exclusiveThread = Thread.currentThread();
  }

  public synchronized void endExclusive()
    throws CardException
  {
    checkState();
    if (this.exclusiveThread != Thread.currentThread())
      throw new IllegalStateException("Exclusive access not assigned to current Thread");
    try
    {
      PCSC.SCardEndTransaction(this.cardId, 0);
    }
    catch (PCSCException localPCSCException)
    {
      throw new CardException("beginExclusive() failed", localPCSCException);
    }
    finally
    {
      this.exclusiveThread = null;
    }
  }

  public byte[] transmitControlCommand(int paramInt, byte[] paramArrayOfByte)
    throws CardException
  {
    checkSecurity("transmitControl");
    checkState();
    checkExclusive();
    if (paramArrayOfByte == null)
      throw new NullPointerException();
    try
    {
      byte[] arrayOfByte = PCSC.SCardControl(this.cardId, paramInt, paramArrayOfByte);
      return arrayOfByte;
    }
    catch (PCSCException localPCSCException)
    {
      handleError(localPCSCException);
      throw new CardException("transmitControlCommand() failed", localPCSCException);
    }
  }

  public void disconnect(boolean paramBoolean)
    throws CardException
  {
    if (paramBoolean)
      checkSecurity("reset");
    if (this.state != State.OK)
      return;
    checkExclusive();
    try
    {
      PCSC.SCardDisconnect(this.cardId, (paramBoolean) ? 0 : 1);
    }
    catch (PCSCException localPCSCException)
    {
    }
    finally
    {
      this.state = State.DISCONNECTED;
      this.exclusiveThread = null;
    }
  }

  public String toString()
  {
    return "PC/SC card in " + this.terminal.getName() + ", protocol " + getProtocol() + ", state " + this.state;
  }

  protected void finalize()
    throws Throwable
  {
    try
    {
      if (this.state == State.OK)
        PCSC.SCardDisconnect(this.cardId, 0);
    }
    finally
    {
      finalize();
    }
  }

  private static enum State
  {
    OK, REMOVED, DISCONNECTED;
  }
}
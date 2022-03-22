package sun.security.smartcardio;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardPermission;
import javax.smartcardio.CardTerminal;

final class TerminalImpl extends CardTerminal
{
  final long contextId;
  final String name;
  private CardImpl card;

  TerminalImpl(long paramLong, String paramString)
  {
    this.contextId = paramLong;
    this.name = paramString;
  }

  public String getName()
  {
    return this.name;
  }

  public synchronized Card connect(String paramString)
    throws CardException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPermission(new CardPermission(this.name, "connect"));
    if (this.card != null)
    {
      if (this.card.isValid())
      {
        String str = this.card.getProtocol();
        if ((paramString.equals("*")) || (paramString.equalsIgnoreCase(str)))
          return this.card;
        throw new CardException("Cannot connect using " + paramString + ", connection already established using " + str);
      }
      this.card = null;
    }
    try
    {
      this.card = new CardImpl(this, paramString);
      return this.card;
    }
    catch (PCSCException localPCSCException)
    {
      if (localPCSCException.code == -2146434967)
        throw new CardNotPresentException("No card present", localPCSCException);
      throw new CardException("connect() failed", localPCSCException);
    }
  }

  public boolean isCardPresent()
    throws CardException
  {
    try
    {
      int[] arrayOfInt = PCSC.SCardGetStatusChange(this.contextId, 3412039852370165760L, new int[] { 0 }, new String[] { this.name });
      return ((arrayOfInt[0] & 0x20) != 0);
    }
    catch (PCSCException localPCSCException)
    {
      throw new CardException("isCardPresent() failed", localPCSCException);
    }
  }

  private boolean waitForCard(boolean paramBoolean, long paramLong)
    throws CardException
  {
    if (paramLong < 3412046810217185280L)
      throw new IllegalArgumentException("timeout must not be negative");
    if (paramLong == 3412046810217185280L)
      paramLong = -1L;
    int[] arrayOfInt = { 0 };
    String[] arrayOfString = { this.name };
    try
    {
      arrayOfInt = PCSC.SCardGetStatusChange(this.contextId, 3412039732111081472L, arrayOfInt, arrayOfString);
      boolean bool1 = (arrayOfInt[0] & 0x20) != 0;
      if (paramBoolean == bool1)
        return true;
      arrayOfInt = PCSC.SCardGetStatusChange(this.contextId, paramLong, arrayOfInt, arrayOfString);
      boolean bool2 = (arrayOfInt[0] & 0x20) != 0;
      if (paramBoolean != bool2)
        throw new CardException("wait mismatch");
      return true;
    }
    catch (PCSCException localPCSCException)
    {
      if (localPCSCException.code == -2146435062)
        return false;
      throw new CardException("waitForCard() failed", localPCSCException);
    }
  }

  public boolean waitForCardPresent(long paramLong)
    throws CardException
  {
    return waitForCard(true, paramLong);
  }

  public boolean waitForCardAbsent(long paramLong)
    throws CardException
  {
    return waitForCard(false, paramLong);
  }

  public String toString()
  {
    return "PC/SC terminal " + this.name;
  }
}
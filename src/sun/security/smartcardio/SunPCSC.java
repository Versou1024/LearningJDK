package sun.security.smartcardio;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactorySpi;

public final class SunPCSC extends Provider
{
  private static final long serialVersionUID = 6168388284028876579L;

  public SunPCSC()
  {
    super("SunPCSC", 1.6000000000000001D, "Sun PC/SC provider");
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Void run()
      {
        this.this$0.put("TerminalFactory.PC/SC", "sun.security.smartcardio.SunPCSC$Factory");
        return null;
      }
    });
  }

  public static final class Factory extends TerminalFactorySpi
  {
    public Factory(Object paramObject)
      throws sun.security.smartcardio.PCSCException
    {
      if (paramObject != null)
        throw new IllegalArgumentException("SunPCSC factory does not use parameters");
      PCSC.checkAvailable();
      PCSCTerminals.initContext();
    }

    protected CardTerminals engineTerminals()
    {
      return new PCSCTerminals();
    }
  }
}
package sun.security.smartcardio;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CardTerminals.State;

final class PCSCTerminals extends CardTerminals
{
  private static long contextId;
  private Map<String, ReaderState> stateMap;
  private static final Map<String, Reference<TerminalImpl>> terminals = new HashMap();

  static synchronized void initContext()
    throws PCSCException
  {
    if (contextId == 3412046810217185280L)
      contextId = PCSC.SCardEstablishContext(0);
  }

  private static synchronized TerminalImpl implGetTerminal(String paramString)
  {
    Reference localReference = (Reference)terminals.get(paramString);
    TerminalImpl localTerminalImpl = (localReference != null) ? (TerminalImpl)localReference.get() : null;
    if (localTerminalImpl != null)
      return localTerminalImpl;
    localTerminalImpl = new TerminalImpl(contextId, paramString);
    terminals.put(paramString, new WeakReference(localTerminalImpl));
    return localTerminalImpl;
  }

  public synchronized List<CardTerminal> list(CardTerminals.State paramState)
    throws CardException
  {
    if (paramState == null)
      throw new NullPointerException();
    try
    {
      String[] arrayOfString1 = PCSC.SCardListReaders(contextId);
      ArrayList localArrayList = new ArrayList(arrayOfString1.length);
      if (this.stateMap == null)
        if (paramState == CardTerminals.State.CARD_INSERTION)
          paramState = CardTerminals.State.CARD_PRESENT;
        else if (paramState == CardTerminals.State.CARD_REMOVAL)
          paramState = CardTerminals.State.CARD_ABSENT;
      String[] arrayOfString2 = arrayOfString1;
      int i = arrayOfString2.length;
      for (int j = 0; j < i; ++j)
      {
        ReaderState localReaderState;
        String str = arrayOfString2[j];
        TerminalImpl localTerminalImpl = implGetTerminal(str);
        switch (1.$SwitchMap$javax$smartcardio$CardTerminals$State[paramState.ordinal()])
        {
        case 1:
          localArrayList.add(localTerminalImpl);
          break;
        case 2:
          if (localTerminalImpl.isCardPresent())
            localArrayList.add(localTerminalImpl);
          break;
        case 3:
          if (!(localTerminalImpl.isCardPresent()))
            localArrayList.add(localTerminalImpl);
          break;
        case 4:
          localReaderState = (ReaderState)this.stateMap.get(str);
          if ((localReaderState != null) && (localReaderState.isInsertion()))
            localArrayList.add(localTerminalImpl);
          break;
        case 5:
          localReaderState = (ReaderState)this.stateMap.get(str);
          if ((localReaderState != null) && (localReaderState.isRemoval()))
            localArrayList.add(localTerminalImpl);
          break;
        default:
          throw new CardException("Unknown state: " + paramState);
        }
      }
      return Collections.unmodifiableList(localArrayList);
    }
    catch (PCSCException localPCSCException)
    {
      throw new CardException("list() failed", localPCSCException);
    }
  }

  public synchronized boolean waitForChange(long paramLong)
    throws CardException
  {
    if (paramLong < 3412046810217185280L)
      throw new IllegalArgumentException("Timeout must not be negative: " + paramLong);
    if (this.stateMap == null)
    {
      this.stateMap = new HashMap();
      waitForChange(3412048390765150208L);
    }
    if (paramLong == 3412046810217185280L)
      paramLong = -1L;
    try
    {
      Object localObject;
      String[] arrayOfString = PCSC.SCardListReaders(contextId);
      int i = arrayOfString.length;
      if (i == 0)
        throw new IllegalStateException("No terminals available");
      int[] arrayOfInt = new int[i];
      ReaderState[] arrayOfReaderState = new ReaderState[i];
      for (int j = 0; j < arrayOfString.length; ++j)
      {
        localObject = arrayOfString[j];
        ReaderState localReaderState = (ReaderState)this.stateMap.get(localObject);
        if (localReaderState == null)
          localReaderState = new ReaderState();
        arrayOfReaderState[j] = localReaderState;
        arrayOfInt[j] = localReaderState.get();
      }
      arrayOfInt = PCSC.SCardGetStatusChange(contextId, paramLong, arrayOfInt, arrayOfString);
      this.stateMap.clear();
      for (j = 0; j < i; ++j)
      {
        localObject = arrayOfReaderState[j];
        ((ReaderState)localObject).update(arrayOfInt[j]);
        this.stateMap.put(arrayOfString[j], localObject);
      }
      return true;
    }
    catch (PCSCException localPCSCException)
    {
      if (localPCSCException.code == -2146435062)
        return false;
      throw new CardException("waitForChange() failed", localPCSCException);
    }
  }

  static List<CardTerminal> waitForCards(List<? extends CardTerminal> paramList, long paramLong, boolean paramBoolean)
    throws CardException
  {
    long l;
    Object localObject2;
    if (paramLong == 3412046827397054464L)
    {
      paramLong = -1L;
      l = -1L;
    }
    else
    {
      l = 3412047686390513664L;
    }
    String[] arrayOfString = new String[paramList.size()];
    int i = 0;
    Object localObject1 = paramList.iterator();
    while (((Iterator)localObject1).hasNext())
    {
      localObject2 = (CardTerminal)((Iterator)localObject1).next();
      if (!(localObject2 instanceof TerminalImpl))
        throw new IllegalArgumentException("Invalid terminal type: " + localObject2.getClass().getName());
      TerminalImpl localTerminalImpl = (TerminalImpl)localObject2;
      arrayOfString[(i++)] = localTerminalImpl.name;
    }
    localObject1 = new int[arrayOfString.length];
    Arrays.fill(localObject1, 0);
    try
    {
      localObject1 = PCSC.SCardGetStatusChange(contextId, l, localObject1, arrayOfString);
      l = paramLong;
      localObject2 = null;
      for (i = 0; i < arrayOfString.length; ++i)
      {
        boolean bool = (localObject1[i] & 0x20) != 0;
        if (bool == paramBoolean)
        {
          if (localObject2 == null)
            localObject2 = new ArrayList();
          ((List)localObject2).add(implGetTerminal(arrayOfString[i]));
        }
      }
      if (localObject2 != null)
        return Collections.unmodifiableList((List)localObject2);
    }
    catch (PCSCException localPCSCException)
    {
      if (localPCSCException.code == -2146435062)
        return Collections.emptyList();
      throw new CardException("waitForCard() failed", localPCSCException);
    }
  }

  private static class ReaderState
  {
    private int current = 0;
    private int previous = 0;

    int get()
    {
      return this.current;
    }

    void update(int paramInt)
    {
      this.previous = this.current;
      this.current = paramInt;
    }

    boolean isInsertion()
    {
      return ((!(present(this.previous))) && (present(this.current)));
    }

    boolean isRemoval()
    {
      return ((present(this.previous)) && (!(present(this.current))));
    }

    static boolean present(int paramInt)
    {
      return ((paramInt & 0x20) != 0);
    }
  }
}
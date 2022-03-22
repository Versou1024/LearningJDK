package sun.awt.datatransfer;

import java.awt.EventQueue;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.FlavorTable;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import sun.awt.AppContext;
import sun.awt.EventListenerAggregate;
import sun.awt.PeerEvent;
import sun.awt.SunToolkit;

public abstract class SunClipboard extends Clipboard
  implements PropertyChangeListener
{
  public static final FlavorTable flavorMap = (FlavorTable)SystemFlavorMap.getDefaultFlavorMap();
  private AppContext contentsContext = null;
  private final Object CLIPBOARD_FLAVOR_LISTENER_KEY;
  private volatile int numberOfFlavorListeners = 0;
  private volatile Set currentDataFlavors;

  public SunClipboard(String paramString)
  {
    super(paramString);
    this.CLIPBOARD_FLAVOR_LISTENER_KEY = new StringBuffer(paramString + "_CLIPBOARD_FLAVOR_LISTENER_KEY");
  }

  public synchronized void setContents(Transferable paramTransferable, ClipboardOwner paramClipboardOwner)
  {
    if (paramTransferable == null)
      throw new NullPointerException("contents");
    initContext();
    ClipboardOwner localClipboardOwner = this.owner;
    Transferable localTransferable = this.contents;
    try
    {
      this.owner = paramClipboardOwner;
      this.contents = new TransferableProxy(paramTransferable, true);
      setContentsNative(paramTransferable);
    }
    finally
    {
      if ((localClipboardOwner != null) && (localClipboardOwner != paramClipboardOwner))
        EventQueue.invokeLater(new Runnable(this, localClipboardOwner, localTransferable)
        {
          public void run()
          {
            this.val$oldOwner.lostOwnership(this.this$0, this.val$oldContents);
          }
        });
    }
  }

  private synchronized void initContext()
  {
    AppContext localAppContext1 = AppContext.getAppContext();
    if (this.contentsContext != localAppContext1)
    {
      synchronized (localAppContext1)
      {
        if (localAppContext1.isDisposed())
          throw new IllegalStateException("Can't set contents from disposed AppContext");
        localAppContext1.addPropertyChangeListener("disposed", this);
      }
      if (this.contentsContext != null)
        this.contentsContext.removePropertyChangeListener("disposed", this);
      this.contentsContext = localAppContext1;
    }
  }

  public synchronized Transferable getContents(Object paramObject)
  {
    if (this.contents != null)
      return this.contents;
    return new ClipboardTransferable(this);
  }

  private synchronized Transferable getContextContents()
  {
    AppContext localAppContext = AppContext.getAppContext();
    return ((localAppContext == this.contentsContext) ? this.contents : null);
  }

  public DataFlavor[] getAvailableDataFlavors()
  {
    Transferable localTransferable = getContextContents();
    if (localTransferable != null)
      return localTransferable.getTransferDataFlavors();
    long[] arrayOfLong = getClipboardFormatsOpenClose();
    return DataTransferer.getInstance().getFlavorsForFormatsAsArray(arrayOfLong, flavorMap);
  }

  public boolean isDataFlavorAvailable(DataFlavor paramDataFlavor)
  {
    if (paramDataFlavor == null)
      throw new NullPointerException("flavor");
    Transferable localTransferable = getContextContents();
    if (localTransferable != null)
      return localTransferable.isDataFlavorSupported(paramDataFlavor);
    long[] arrayOfLong = getClipboardFormatsOpenClose();
    return formatArrayAsDataFlavorSet(arrayOfLong).contains(paramDataFlavor);
  }

  public Object getData(DataFlavor paramDataFlavor)
    throws UnsupportedFlavorException, IOException
  {
    if (paramDataFlavor == null)
      throw new NullPointerException("flavor");
    Transferable localTransferable1 = getContextContents();
    if (localTransferable1 != null)
      return localTransferable1.getTransferData(paramDataFlavor);
    long l = 3412047291253522432L;
    byte[] arrayOfByte = null;
    Transferable localTransferable2 = null;
    try
    {
      openClipboard(null);
      long[] arrayOfLong = getClipboardFormats();
      Long localLong = (Long)DataTransferer.getInstance().getFlavorsForFormats(arrayOfLong, flavorMap).get(paramDataFlavor);
      if (localLong == null)
        throw new UnsupportedFlavorException(paramDataFlavor);
      l = localLong.longValue();
      arrayOfByte = getClipboardData(l);
      if (DataTransferer.getInstance().isLocaleDependentTextFormat(l))
        localTransferable2 = createLocaleTransferable(arrayOfLong);
    }
    finally
    {
      closeClipboard();
    }
    return DataTransferer.getInstance().translateBytes(arrayOfByte, paramDataFlavor, l, localTransferable2);
  }

  protected Transferable createLocaleTransferable(long[] paramArrayOfLong)
    throws IOException
  {
    return null;
  }

  public void openClipboard(SunClipboard paramSunClipboard)
  {
  }

  public void closeClipboard()
  {
  }

  public abstract long getID();

  public void propertyChange(PropertyChangeEvent paramPropertyChangeEvent)
  {
    if (("disposed".equals(paramPropertyChangeEvent.getPropertyName())) && (Boolean.TRUE.equals(paramPropertyChangeEvent.getNewValue())))
    {
      AppContext localAppContext = (AppContext)paramPropertyChangeEvent.getSource();
      lostOwnershipLater(localAppContext);
    }
  }

  protected void lostOwnershipImpl()
  {
    lostOwnershipLater(null);
  }

  protected void lostOwnershipLater(AppContext paramAppContext)
  {
    AppContext localAppContext = this.contentsContext;
    if (localAppContext == null)
      return;
    2 local2 = new Runnable(this, paramAppContext)
    {
      public void run()
      {
        SunClipboard localSunClipboard1 = this.this$0;
        ClipboardOwner localClipboardOwner = null;
        Transferable localTransferable = null;
        synchronized (localSunClipboard1)
        {
          AppContext localAppContext = SunClipboard.access$000(localSunClipboard1);
          if (localAppContext != null)
            break label29;
          return;
          label29: if ((this.val$disposedContext != null) && (localAppContext != this.val$disposedContext))
            break label88;
          localClipboardOwner = SunClipboard.access$100(localSunClipboard1);
          localTransferable = SunClipboard.access$200(localSunClipboard1);
          SunClipboard.access$002(localSunClipboard1, null);
          SunClipboard.access$302(localSunClipboard1, null);
          SunClipboard.access$402(localSunClipboard1, null);
          localSunClipboard1.clearNativeContext();
          localAppContext.removePropertyChangeListener("disposed", localSunClipboard1);
          break label92:
          label88: label92: return;
        }
        if (localClipboardOwner != null)
          localClipboardOwner.lostOwnership(localSunClipboard1, localTransferable);
      }
    };
    SunToolkit.postEvent(localAppContext, new PeerEvent(this, local2, 3412048253326196737L));
  }

  protected abstract void clearNativeContext();

  protected abstract void setContentsNative(Transferable paramTransferable);

  protected long[] getClipboardFormatsOpenClose()
  {
    try
    {
      openClipboard(null);
      long[] arrayOfLong = getClipboardFormats();
      return arrayOfLong;
    }
    finally
    {
      closeClipboard();
    }
  }

  protected abstract long[] getClipboardFormats();

  protected abstract byte[] getClipboardData(long paramLong)
    throws IOException;

  private static Set formatArrayAsDataFlavorSet(long[] paramArrayOfLong)
  {
    return ((paramArrayOfLong == null) ? null : DataTransferer.getInstance().getFlavorsForFormatsAsSet(paramArrayOfLong, flavorMap));
  }

  public synchronized void addFlavorListener(FlavorListener paramFlavorListener)
  {
    if (paramFlavorListener == null)
      return;
    AppContext localAppContext = AppContext.getAppContext();
    EventListenerAggregate localEventListenerAggregate = (EventListenerAggregate)localAppContext.get(this.CLIPBOARD_FLAVOR_LISTENER_KEY);
    if (localEventListenerAggregate == null)
    {
      localEventListenerAggregate = new EventListenerAggregate(FlavorListener.class);
      localAppContext.put(this.CLIPBOARD_FLAVOR_LISTENER_KEY, localEventListenerAggregate);
    }
    localEventListenerAggregate.add(paramFlavorListener);
    if (this.numberOfFlavorListeners++ == 0)
    {
      long[] arrayOfLong = null;
      try
      {
        openClipboard(null);
        arrayOfLong = getClipboardFormats();
      }
      catch (IllegalStateException localIllegalStateException)
      {
      }
      finally
      {
        closeClipboard();
      }
      this.currentDataFlavors = formatArrayAsDataFlavorSet(arrayOfLong);
      registerClipboardViewerChecked();
    }
  }

  public synchronized void removeFlavorListener(FlavorListener paramFlavorListener)
  {
    if (paramFlavorListener == null)
      return;
    AppContext localAppContext = AppContext.getAppContext();
    EventListenerAggregate localEventListenerAggregate = (EventListenerAggregate)localAppContext.get(this.CLIPBOARD_FLAVOR_LISTENER_KEY);
    if (localEventListenerAggregate.remove(paramFlavorListener))
      if (--this.numberOfFlavorListeners == 0)
      {
        unregisterClipboardViewerChecked();
        this.currentDataFlavors = null;
      }
  }

  public synchronized FlavorListener[] getFlavorListeners()
  {
    EventListenerAggregate localEventListenerAggregate = (EventListenerAggregate)AppContext.getAppContext().get(this.CLIPBOARD_FLAVOR_LISTENER_KEY);
    return ((localEventListenerAggregate == null) ? new FlavorListener[0] : (FlavorListener[])(FlavorListener[])localEventListenerAggregate.getListenersCopy());
  }

  public boolean areFlavorListenersRegistered()
  {
    return (this.numberOfFlavorListeners > 0);
  }

  protected abstract void registerClipboardViewerChecked();

  protected abstract void unregisterClipboardViewerChecked();

  public void checkChange(long[] paramArrayOfLong)
  {
    Set localSet = this.currentDataFlavors;
    this.currentDataFlavors = formatArrayAsDataFlavorSet(paramArrayOfLong);
    if ((localSet != null) && (this.currentDataFlavors != null) && (localSet.equals(this.currentDataFlavors)))
      return;
    Iterator localIterator = AppContext.getAppContexts().iterator();
    while (true)
    {
      AppContext localAppContext;
      while (true)
      {
        do
        {
          if (!(localIterator.hasNext()))
            return;
          localAppContext = (AppContext)localIterator.next();
        }
        while (localAppContext == null);
        if (!(localAppContext.isDisposed()))
          break;
      }
      EventListenerAggregate localEventListenerAggregate = (EventListenerAggregate)localAppContext.get(this.CLIPBOARD_FLAVOR_LISTENER_KEY);
      if (localEventListenerAggregate != null)
      {
        FlavorListener[] arrayOfFlavorListener = (FlavorListener[])(FlavorListener[])localEventListenerAggregate.getListenersInternal();
        for (int i = 0; i < arrayOfFlavorListener.length; ++i)
          SunToolkit.postEvent(localAppContext, new PeerEvent(this, new Runnable(this, arrayOfFlavorListener[i])
          {
            private final FlavorListener flavorListener;

            public void run()
            {
              if (this.flavorListener != null)
                this.flavorListener.flavorsChanged(new FlavorEvent(this.this$0));
            }
          }
          , 3412040728543494145L));
      }
    }
  }
}
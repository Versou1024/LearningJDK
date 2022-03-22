package sun.awt.windows;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import sun.awt.datatransfer.DataTransferer;
import sun.awt.datatransfer.SunClipboard;

public class WClipboard extends SunClipboard
{
  private boolean isClipboardViewerRegistered;

  public WClipboard()
  {
    super("System");
  }

  public long getID()
  {
    return 3412046827397054464L;
  }

  protected void setContentsNative(Transferable paramTransferable)
  {
    SortedMap localSortedMap = WDataTransferer.getInstance().getFormatsForTransferable(paramTransferable, flavorMap);
    openClipboard(this);
    try
    {
      Iterator localIterator = localSortedMap.keySet().iterator();
      while (localIterator.hasNext())
      {
        Long localLong = (Long)localIterator.next();
        long l = localLong.longValue();
        DataFlavor localDataFlavor = (DataFlavor)localSortedMap.get(localLong);
        try
        {
          byte[] arrayOfByte = WDataTransferer.getInstance().translateTransferable(paramTransferable, localDataFlavor, l);
          publishClipboardData(l, arrayOfByte);
        }
        catch (IOException localIOException)
        {
          if ((!(localDataFlavor.isMimeTypeEqual("application/x-java-jvm-local-objectref"))) || (!(localIOException instanceof NotSerializableException)))
            localIOException.printStackTrace();
        }
      }
    }
    finally
    {
      closeClipboard();
    }
  }

  private void lostSelectionOwnershipImpl()
  {
    lostOwnershipImpl();
  }

  protected void clearNativeContext()
  {
  }

  public native void openClipboard(SunClipboard paramSunClipboard)
    throws IllegalStateException;

  public native void closeClipboard();

  private native void publishClipboardData(long paramLong, byte[] paramArrayOfByte);

  private static native void init();

  protected native long[] getClipboardFormats();

  protected native byte[] getClipboardData(long paramLong)
    throws IOException;

  protected void registerClipboardViewerChecked()
  {
    if (!(this.isClipboardViewerRegistered))
    {
      registerClipboardViewer();
      this.isClipboardViewerRegistered = true;
    }
  }

  private native void registerClipboardViewer();

  protected void unregisterClipboardViewerChecked()
  {
  }

  private void handleContentsChanged()
  {
    if (!(areFlavorListenersRegistered()))
      return;
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
    checkChange(arrayOfLong);
  }

  protected Transferable createLocaleTransferable(long[] paramArrayOfLong)
    throws IOException
  {
    int i = 0;
    for (int j = 0; j < paramArrayOfLong.length; ++j)
      if (paramArrayOfLong[j] == 16L)
      {
        i = 1;
        break;
      }
    if (i == 0)
      return null;
    byte[] arrayOfByte1 = null;
    try
    {
      arrayOfByte1 = getClipboardData(16L);
    }
    catch (IOException localIOException)
    {
      return null;
    }
    byte[] arrayOfByte2 = arrayOfByte1;
    return new Transferable(this, arrayOfByte2)
    {
      public DataFlavor[] getTransferDataFlavors()
      {
        return { DataTransferer.javaTextEncodingFlavor };
      }

      public boolean isDataFlavorSupported()
      {
        return paramDataFlavor.equals(DataTransferer.javaTextEncodingFlavor);
      }

      public Object getTransferData()
        throws UnsupportedFlavorException
      {
        if (isDataFlavorSupported(paramDataFlavor))
          return this.val$localeDataFinal;
        throw new UnsupportedFlavorException(paramDataFlavor);
      }
    };
  }

  static
  {
    init();
  }
}
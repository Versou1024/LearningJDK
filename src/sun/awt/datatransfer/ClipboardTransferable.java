package sun.awt.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ClipboardTransferable
  implements Transferable
{
  private final HashMap flavorsToData = new HashMap();
  private DataFlavor[] flavors = new DataFlavor[0];

  public ClipboardTransferable(SunClipboard paramSunClipboard)
  {
    paramSunClipboard.openClipboard(null);
    try
    {
      long[] arrayOfLong = paramSunClipboard.getClipboardFormats();
      if ((arrayOfLong != null) && (arrayOfLong.length > 0))
      {
        HashMap localHashMap = new HashMap(arrayOfLong.length, 1F);
        Map localMap = DataTransferer.getInstance().getFlavorsForFormats(arrayOfLong, SunClipboard.flavorMap);
        Iterator localIterator = localMap.keySet().iterator();
        while (localIterator.hasNext())
        {
          DataFlavor localDataFlavor = (DataFlavor)localIterator.next();
          Long localLong = (Long)localMap.get(localDataFlavor);
          fetchOneFlavor(paramSunClipboard, localDataFlavor, localLong, localHashMap);
        }
        DataTransferer.getInstance();
        this.flavors = DataTransferer.setToSortedDataFlavorArray(this.flavorsToData.keySet(), localMap);
      }
    }
    finally
    {
      paramSunClipboard.closeClipboard();
    }
  }

  private boolean fetchOneFlavor(SunClipboard paramSunClipboard, DataFlavor paramDataFlavor, Long paramLong, HashMap paramHashMap)
  {
    if (!(this.flavorsToData.containsKey(paramDataFlavor)))
    {
      long l = paramLong.longValue();
      Object localObject = null;
      if (!(paramHashMap.containsKey(paramLong)))
      {
        try
        {
          localObject = paramSunClipboard.getClipboardData(l);
        }
        catch (IOException localIOException)
        {
          localObject = localIOException;
        }
        catch (Throwable localThrowable)
        {
          localThrowable.printStackTrace();
        }
        paramHashMap.put(paramLong, localObject);
      }
      else
      {
        localObject = paramHashMap.get(paramLong);
      }
      if (localObject instanceof IOException)
      {
        this.flavorsToData.put(paramDataFlavor, localObject);
        return false;
      }
      if (localObject != null)
      {
        this.flavorsToData.put(paramDataFlavor, new DataFactory(this, l, (byte[])(byte[])localObject));
        return true;
      }
    }
    return false;
  }

  public DataFlavor[] getTransferDataFlavors()
  {
    return ((DataFlavor[])(DataFlavor[])this.flavors.clone());
  }

  public boolean isDataFlavorSupported(DataFlavor paramDataFlavor)
  {
    return this.flavorsToData.containsKey(paramDataFlavor);
  }

  public Object getTransferData(DataFlavor paramDataFlavor)
    throws UnsupportedFlavorException, IOException
  {
    if (!(isDataFlavorSupported(paramDataFlavor)))
      throw new UnsupportedFlavorException(paramDataFlavor);
    Object localObject = this.flavorsToData.get(paramDataFlavor);
    if (localObject instanceof IOException)
      throw ((IOException)localObject);
    if (localObject instanceof DataFactory)
    {
      DataFactory localDataFactory = (DataFactory)localObject;
      localObject = localDataFactory.getTransferData(paramDataFlavor);
    }
    return localObject;
  }

  private final class DataFactory
  {
    final long format;
    final byte[] data;

    DataFactory(, long paramLong, byte[] paramArrayOfByte)
    {
      this.format = paramLong;
      this.data = paramArrayOfByte;
    }

    public Object getTransferData()
      throws IOException
    {
      return DataTransferer.getInstance().translateBytes(this.data, paramDataFlavor, this.format, this.this$0);
    }
  }
}
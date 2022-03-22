package sun.awt.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TransferableProxy
  implements Transferable
{
  protected final Transferable transferable;
  protected final boolean isLocal;

  public TransferableProxy(Transferable paramTransferable, boolean paramBoolean)
  {
    this.transferable = paramTransferable;
    this.isLocal = paramBoolean;
  }

  public DataFlavor[] getTransferDataFlavors()
  {
    return this.transferable.getTransferDataFlavors();
  }

  public boolean isDataFlavorSupported(DataFlavor paramDataFlavor)
  {
    return this.transferable.isDataFlavorSupported(paramDataFlavor);
  }

  public Object getTransferData(DataFlavor paramDataFlavor)
    throws UnsupportedFlavorException, IOException
  {
    Object localObject = this.transferable.getTransferData(paramDataFlavor);
    if ((localObject != null) && (this.isLocal) && (paramDataFlavor.isFlavorSerializedObjectType()))
    {
      ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
      ClassLoaderObjectOutputStream localClassLoaderObjectOutputStream = new ClassLoaderObjectOutputStream(localByteArrayOutputStream);
      localClassLoaderObjectOutputStream.writeObject(localObject);
      ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(localByteArrayOutputStream.toByteArray());
      try
      {
        ClassLoaderObjectInputStream localClassLoaderObjectInputStream = new ClassLoaderObjectInputStream(localByteArrayInputStream, localClassLoaderObjectOutputStream.getClassLoaderMap());
        localObject = localClassLoaderObjectInputStream.readObject();
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        throw ((IOException)new IOException().initCause(localClassNotFoundException));
      }
    }
    return localObject;
  }
}
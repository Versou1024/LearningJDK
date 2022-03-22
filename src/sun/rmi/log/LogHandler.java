package sun.rmi.log;

import java.io.InputStream;
import java.io.OutputStream;
import sun.rmi.server.MarshalInputStream;
import sun.rmi.server.MarshalOutputStream;

public abstract class LogHandler
{
  public abstract Object initialSnapshot()
    throws Exception;

  public void snapshot(OutputStream paramOutputStream, Object paramObject)
    throws Exception
  {
    MarshalOutputStream localMarshalOutputStream = new MarshalOutputStream(paramOutputStream);
    localMarshalOutputStream.writeObject(paramObject);
    localMarshalOutputStream.flush();
  }

  public Object recover(InputStream paramInputStream)
    throws Exception
  {
    MarshalInputStream localMarshalInputStream = new MarshalInputStream(paramInputStream);
    return localMarshalInputStream.readObject();
  }

  public void writeUpdate(LogOutputStream paramLogOutputStream, Object paramObject)
    throws Exception
  {
    MarshalOutputStream localMarshalOutputStream = new MarshalOutputStream(paramLogOutputStream);
    localMarshalOutputStream.writeObject(paramObject);
    localMarshalOutputStream.flush();
  }

  public Object readUpdate(LogInputStream paramLogInputStream, Object paramObject)
    throws Exception
  {
    MarshalInputStream localMarshalInputStream = new MarshalInputStream(paramLogInputStream);
    return applyUpdate(localMarshalInputStream.readObject(), paramObject);
  }

  public abstract Object applyUpdate(Object paramObject1, Object paramObject2)
    throws Exception;
}
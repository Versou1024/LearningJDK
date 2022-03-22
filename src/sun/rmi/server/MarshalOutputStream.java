package sun.rmi.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RemoteStub;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.rmi.transport.ObjectTable;
import sun.rmi.transport.Target;

public class MarshalOutputStream extends ObjectOutputStream
{
  public MarshalOutputStream(OutputStream paramOutputStream)
    throws IOException
  {
    this(paramOutputStream, 1);
  }

  public MarshalOutputStream(OutputStream paramOutputStream, int paramInt)
    throws IOException
  {
    super(paramOutputStream);
    useProtocolVersion(paramInt);
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        MarshalOutputStream.access$000(this.this$0, true);
        return null;
      }
    });
  }

  protected final Object replaceObject(Object paramObject)
    throws IOException
  {
    if ((paramObject instanceof Remote) && (!(paramObject instanceof RemoteStub)))
    {
      Target localTarget = ObjectTable.getTarget((Remote)paramObject);
      if (localTarget != null)
        return localTarget.getStub();
    }
    return paramObject;
  }

  protected void annotateClass(Class<?> paramClass)
    throws IOException
  {
    writeLocation(RMIClassLoader.getClassAnnotation(paramClass));
  }

  protected void annotateProxyClass(Class<?> paramClass)
    throws IOException
  {
    annotateClass(paramClass);
  }

  protected void writeLocation(String paramString)
    throws IOException
  {
    writeObject(paramString);
  }
}
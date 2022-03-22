package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;

class Net
{
  static InetSocketAddress checkAddress(SocketAddress paramSocketAddress)
  {
    if (paramSocketAddress == null)
      throw new IllegalArgumentException();
    if (!(paramSocketAddress instanceof InetSocketAddress))
      throw new UnsupportedAddressTypeException();
    InetSocketAddress localInetSocketAddress = (InetSocketAddress)paramSocketAddress;
    if (localInetSocketAddress.isUnresolved())
      throw new UnresolvedAddressException();
    return localInetSocketAddress;
  }

  static InetSocketAddress asInetSocketAddress(SocketAddress paramSocketAddress)
  {
    if (!(paramSocketAddress instanceof InetSocketAddress))
      throw new UnsupportedAddressTypeException();
    return ((InetSocketAddress)paramSocketAddress);
  }

  static void translateToSocketException(Exception paramException)
    throws SocketException
  {
    if (paramException instanceof SocketException)
      throw ((SocketException)paramException);
    Object localObject = paramException;
    if (paramException instanceof ClosedChannelException)
      localObject = new SocketException("Socket is closed");
    else if (paramException instanceof AlreadyBoundException)
      localObject = new SocketException("Already bound");
    else if (paramException instanceof NotYetBoundException)
      localObject = new SocketException("Socket is not bound yet");
    else if (paramException instanceof UnsupportedAddressTypeException)
      localObject = new SocketException("Unsupported address type");
    else if (paramException instanceof UnresolvedAddressException)
      localObject = new SocketException("Unresolved address");
    if (localObject != paramException)
      ((Exception)localObject).initCause(paramException);
    if (localObject instanceof SocketException)
      throw ((SocketException)localObject);
    if (localObject instanceof RuntimeException)
      throw ((RuntimeException)localObject);
    throw new Error("Untranslated exception", (Throwable)localObject);
  }

  static void translateException(Exception paramException, boolean paramBoolean)
    throws IOException
  {
    if (paramException instanceof IOException)
      throw ((IOException)paramException);
    if ((paramBoolean) && (paramException instanceof UnresolvedAddressException))
      throw new UnknownHostException();
    translateToSocketException(paramException);
  }

  static void translateException(Exception paramException)
    throws IOException
  {
    translateException(paramException, false);
  }

  static FileDescriptor socket(boolean paramBoolean)
  {
    return IOUtil.newFD(socket0(paramBoolean, false));
  }

  static FileDescriptor serverSocket(boolean paramBoolean)
  {
    return IOUtil.newFD(socket0(paramBoolean, true));
  }

  private static native int socket0(boolean paramBoolean1, boolean paramBoolean2);

  static native void bind(FileDescriptor paramFileDescriptor, InetAddress paramInetAddress, int paramInt)
    throws IOException;

  static native int connect(FileDescriptor paramFileDescriptor, InetAddress paramInetAddress, int paramInt1, int paramInt2)
    throws IOException;

  private static native int localPort(FileDescriptor paramFileDescriptor)
    throws IOException;

  private static native InetAddress localInetAddress(FileDescriptor paramFileDescriptor)
    throws IOException;

  static InetSocketAddress localAddress(FileDescriptor paramFileDescriptor)
  {
    try
    {
      return new InetSocketAddress(localInetAddress(paramFileDescriptor), localPort(paramFileDescriptor));
    }
    catch (IOException localIOException)
    {
      throw new Error(localIOException);
    }
  }

  static int localPortNumber(FileDescriptor paramFileDescriptor)
  {
    try
    {
      return localPort(paramFileDescriptor);
    }
    catch (IOException localIOException)
    {
      throw new Error(localIOException);
    }
  }

  private static native int getIntOption0(FileDescriptor paramFileDescriptor, int paramInt)
    throws IOException;

  static int getIntOption(FileDescriptor paramFileDescriptor, int paramInt)
    throws IOException
  {
    return getIntOption0(paramFileDescriptor, paramInt);
  }

  private static native void setIntOption0(FileDescriptor paramFileDescriptor, int paramInt1, int paramInt2)
    throws IOException;

  static void setIntOption(FileDescriptor paramFileDescriptor, int paramInt1, int paramInt2)
    throws IOException
  {
    setIntOption0(paramFileDescriptor, paramInt1, paramInt2);
  }

  private static native void initIDs();

  static
  {
    Util.load();
    initIDs();
  }
}
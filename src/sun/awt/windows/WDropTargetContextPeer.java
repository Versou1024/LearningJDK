package sun.awt.windows;

import java.io.FileInputStream;
import java.io.IOException;
import sun.awt.PeerEvent;
import sun.awt.SunToolkit;
import sun.awt.dnd.SunDropTargetContextPeer;
import sun.awt.dnd.SunDropTargetContextPeer.EventDispatcher;
import sun.awt.dnd.SunDropTargetEvent;

final class WDropTargetContextPeer extends SunDropTargetContextPeer
{
  static WDropTargetContextPeer getWDropTargetContextPeer()
  {
    return new WDropTargetContextPeer();
  }

  private static FileInputStream getFileStream(String paramString, long paramLong)
    throws IOException
  {
    return new WDropTargetContextPeerFileStream(paramString, paramLong);
  }

  private static Object getIStream(long paramLong)
    throws IOException
  {
    return new WDropTargetContextPeerIStream(paramLong);
  }

  protected Object getNativeData(long paramLong)
  {
    return getData(getNativeDragContext(), paramLong);
  }

  protected void doDropDone(boolean paramBoolean1, int paramInt, boolean paramBoolean2)
  {
    dropDone(getNativeDragContext(), paramBoolean1, paramInt);
  }

  protected void eventPosted(SunDropTargetEvent paramSunDropTargetEvent)
  {
    if (paramSunDropTargetEvent.getID() != 502)
    {
      1 local1 = new Runnable(this, paramSunDropTargetEvent)
      {
        public void run()
        {
          this.val$e.getDispatcher().unregisterAllEvents();
        }
      };
      PeerEvent localPeerEvent = new PeerEvent(paramSunDropTargetEvent.getSource(), local1, 3412048459484626944L);
      SunToolkit.executeOnEventHandlerThread(localPeerEvent);
    }
  }

  private native Object getData(long paramLong1, long paramLong2);

  private native void dropDone(long paramLong, boolean paramBoolean, int paramInt);
}
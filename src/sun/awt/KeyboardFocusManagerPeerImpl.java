package sun.awt;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class KeyboardFocusManagerPeerImpl
  implements KeyboardFocusManagerPeer
{
  static Method m_removeLastFocusRequest = null;

  static native Window getNativeFocusedWindow();

  static native Component getNativeFocusOwner();

  static native void clearNativeGlobalFocusOwner(Window paramWindow);

  public void setCurrentFocusedWindow(Window paramWindow)
  {
  }

  public Window getCurrentFocusedWindow()
  {
    return getNativeFocusedWindow();
  }

  public void setCurrentFocusOwner(Component paramComponent)
  {
  }

  public Component getCurrentFocusOwner()
  {
    return getNativeFocusOwner();
  }

  public void clearGlobalFocusOwner(Window paramWindow)
  {
    clearNativeGlobalFocusOwner(paramWindow);
  }

  public static void removeLastFocusRequest(Component paramComponent)
  {
    try
    {
      if (m_removeLastFocusRequest == null)
        m_removeLastFocusRequest = SunToolkit.getMethod(KeyboardFocusManager.class, "removeLastFocusRequest", new Class[] { Component.class });
      m_removeLastFocusRequest.invoke(null, new Object[] { paramComponent });
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      localInvocationTargetException.printStackTrace();
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      localIllegalAccessException.printStackTrace();
    }
  }
}
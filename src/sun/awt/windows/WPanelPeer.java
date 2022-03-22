package sun.awt.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.peer.ComponentPeer;
import java.awt.peer.PanelPeer;
import java.util.Vector;
import sun.awt.SunGraphicsCallback.PaintHeavyweightComponentsCallback;
import sun.awt.SunGraphicsCallback.PrintHeavyweightComponentsCallback;

class WPanelPeer extends WCanvasPeer
  implements PanelPeer
{
  Insets insets_;

  public void paint(Graphics paramGraphics)
  {
    super.paint(paramGraphics);
    SunGraphicsCallback.PaintHeavyweightComponentsCallback.getInstance().runComponents(((Container)this.target).getComponents(), paramGraphics, 3);
  }

  public void print(Graphics paramGraphics)
  {
    super.print(paramGraphics);
    SunGraphicsCallback.PrintHeavyweightComponentsCallback.getInstance().runComponents(((Container)this.target).getComponents(), paramGraphics, 3);
  }

  public Insets getInsets()
  {
    return this.insets_;
  }

  private static native void initIDs();

  WPanelPeer(Component paramComponent)
  {
    super(paramComponent);
  }

  void initialize()
  {
    super.initialize();
    this.insets_ = new Insets(0, 0, 0, 0);
    Color localColor = ((Component)this.target).getBackground();
    if (localColor == null)
    {
      localColor = WColor.getDefaultColor(1);
      ((Component)this.target).setBackground(localColor);
      setBackground(localColor);
    }
    localColor = ((Component)this.target).getForeground();
    if (localColor == null)
    {
      localColor = WColor.getDefaultColor(2);
      ((Component)this.target).setForeground(localColor);
      setForeground(localColor);
    }
  }

  public Insets insets()
  {
    return getInsets();
  }

  private void recursiveDisplayChanged(Component paramComponent)
  {
    Object localObject;
    ComponentPeer localComponentPeer = paramComponent.getPeer();
    if ((paramComponent instanceof Container) && (!(localComponentPeer instanceof WPanelPeer)))
    {
      localObject = ((Container)paramComponent).getComponents();
      for (int i = 0; i < localObject.length; ++i)
        recursiveDisplayChanged(localObject[i]);
    }
    if ((localComponentPeer != null) && (localComponentPeer instanceof WComponentPeer))
    {
      localObject = (WComponentPeer)localComponentPeer;
      ((WComponentPeer)localObject).displayChanged();
    }
  }

  public void displayChanged()
  {
    super.displayChanged();
    Component[] arrayOfComponent = ((Container)this.target).getComponents();
    for (int i = 0; i < arrayOfComponent.length; ++i)
      recursiveDisplayChanged(arrayOfComponent[i]);
  }

  private native void pRestack(Object[] paramArrayOfObject);

  private void restack(Container paramContainer, Vector paramVector)
  {
    for (int i = 0; i < paramContainer.getComponentCount(); ++i)
    {
      Component localComponent = paramContainer.getComponent(i);
      if ((!(localComponent.isLightweight())) && (localComponent.getPeer() != null))
        paramVector.add(localComponent.getPeer());
      if ((localComponent.isLightweight()) && (localComponent instanceof Container))
        restack((Container)localComponent, paramVector);
    }
  }

  public void restack()
  {
    Vector localVector = new Vector();
    localVector.add(this);
    Container localContainer = (Container)this.target;
    restack(localContainer, localVector);
    pRestack(localVector.toArray());
  }

  public boolean isRestackSupported()
  {
    return true;
  }

  static
  {
    initIDs();
  }
}
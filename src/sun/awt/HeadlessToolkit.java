package sun.awt;

import java.awt.AWTException;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.JobAttributes;
import java.awt.KeyboardFocusManager;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PageAttributes;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.PrintJob;
import java.awt.Robot;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.AWTEventListener;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DesktopPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.RobotPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.SystemTrayPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.TrayIconPeer;
import java.awt.peer.WindowPeer;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class HeadlessToolkit extends Toolkit
  implements ComponentFactory
{
  private Toolkit tk;
  private ComponentFactory componentFactory;

  public HeadlessToolkit(Toolkit paramToolkit)
  {
    this.tk = paramToolkit;
    if (paramToolkit instanceof ComponentFactory)
      this.componentFactory = ((ComponentFactory)paramToolkit);
  }

  public Toolkit getUnderlyingToolkit()
  {
    return this.tk;
  }

  public CanvasPeer createCanvas(Canvas paramCanvas)
  {
    return ((CanvasPeer)createComponent(paramCanvas));
  }

  public PanelPeer createPanel(Panel paramPanel)
  {
    return ((PanelPeer)createComponent(paramPanel));
  }

  public WindowPeer createWindow(Window paramWindow)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public FramePeer createFrame(Frame paramFrame)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public DialogPeer createDialog(Dialog paramDialog)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public ButtonPeer createButton(Button paramButton)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public TextFieldPeer createTextField(TextField paramTextField)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public ChoicePeer createChoice(Choice paramChoice)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public LabelPeer createLabel(Label paramLabel)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public ListPeer createList(List paramList)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public CheckboxPeer createCheckbox(Checkbox paramCheckbox)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public ScrollbarPeer createScrollbar(Scrollbar paramScrollbar)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public ScrollPanePeer createScrollPane(ScrollPane paramScrollPane)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public TextAreaPeer createTextArea(TextArea paramTextArea)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public FileDialogPeer createFileDialog(FileDialog paramFileDialog)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public MenuBarPeer createMenuBar(MenuBar paramMenuBar)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public MenuPeer createMenu(Menu paramMenu)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public PopupMenuPeer createPopupMenu(PopupMenu paramPopupMenu)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public MenuItemPeer createMenuItem(MenuItem paramMenuItem)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem paramCheckboxMenuItem)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent paramDragGestureEvent)
    throws InvalidDnDOperationException
  {
    throw new InvalidDnDOperationException("Headless environment");
  }

  public RobotPeer createRobot(Robot paramRobot, GraphicsDevice paramGraphicsDevice)
    throws AWTException, HeadlessException
  {
    throw new HeadlessException();
  }

  public KeyboardFocusManagerPeer createKeyboardFocusManagerPeer(KeyboardFocusManager paramKeyboardFocusManager)
    throws HeadlessException
  {
    KeyboardFocusManagerPeerImpl localKeyboardFocusManagerPeerImpl = new KeyboardFocusManagerPeerImpl(paramKeyboardFocusManager);
    return localKeyboardFocusManagerPeerImpl;
  }

  public TrayIconPeer createTrayIcon(TrayIcon paramTrayIcon)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public SystemTrayPeer createSystemTray(SystemTray paramSystemTray)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public boolean isTraySupported()
  {
    return false;
  }

  public GlobalCursorManager getGlobalCursorManager()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  protected void loadSystemColors(int[] paramArrayOfInt)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public ColorModel getColorModel()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public int getScreenResolution()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public Map mapInputMethodHighlight(InputMethodHighlight paramInputMethodHighlight)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public int getMenuShortcutKeyMask()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public boolean getLockingKeyState(int paramInt)
    throws UnsupportedOperationException
  {
    throw new HeadlessException();
  }

  public void setLockingKeyState(int paramInt, boolean paramBoolean)
    throws UnsupportedOperationException
  {
    throw new HeadlessException();
  }

  public Cursor createCustomCursor(Image paramImage, Point paramPoint, String paramString)
    throws IndexOutOfBoundsException, HeadlessException
  {
    throw new HeadlessException();
  }

  public Dimension getBestCursorSize(int paramInt1, int paramInt2)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public int getMaximumCursorColors()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> paramClass, DragSource paramDragSource, Component paramComponent, int paramInt, DragGestureListener paramDragGestureListener)
  {
    return null;
  }

  public int getScreenHeight()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public int getScreenWidth()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public Dimension getScreenSize()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public Insets getScreenInsets(GraphicsConfiguration paramGraphicsConfiguration)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public void setDynamicLayout(boolean paramBoolean)
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  protected boolean isDynamicLayoutSet()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public boolean isDynamicLayoutActive()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public Clipboard getSystemClipboard()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public PrintJob getPrintJob(Frame paramFrame, String paramString, JobAttributes paramJobAttributes, PageAttributes paramPageAttributes)
  {
    if (paramFrame != null)
      throw new HeadlessException();
    throw new IllegalArgumentException("PrintJob not supported in a headless environment");
  }

  public PrintJob getPrintJob(Frame paramFrame, String paramString, Properties paramProperties)
  {
    if (paramFrame != null)
      throw new HeadlessException();
    throw new IllegalArgumentException("PrintJob not supported in a headless environment");
  }

  public void sync()
  {
  }

  public void beep()
  {
    System.out.write(7);
  }

  public EventQueue getSystemEventQueueImpl()
  {
    return SunToolkit.getSystemEventQueueImplPP();
  }

  public int checkImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    return this.tk.checkImage(paramImage, paramInt1, paramInt2, paramImageObserver);
  }

  public boolean prepareImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    return this.tk.prepareImage(paramImage, paramInt1, paramInt2, paramImageObserver);
  }

  public Image getImage(String paramString)
  {
    return this.tk.getImage(paramString);
  }

  public Image getImage(URL paramURL)
  {
    return this.tk.getImage(paramURL);
  }

  public Image createImage(String paramString)
  {
    return this.tk.createImage(paramString);
  }

  public Image createImage(URL paramURL)
  {
    return this.tk.createImage(paramURL);
  }

  public Image createImage(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    return this.tk.createImage(paramArrayOfByte, paramInt1, paramInt2);
  }

  public Image createImage(ImageProducer paramImageProducer)
  {
    return this.tk.createImage(paramImageProducer);
  }

  public Image createImage(byte[] paramArrayOfByte)
  {
    return this.tk.createImage(paramArrayOfByte);
  }

  public FontPeer getFontPeer(String paramString, int paramInt)
  {
    if (this.componentFactory != null)
      return this.componentFactory.getFontPeer(paramString, paramInt);
    return null;
  }

  public FontMetrics getFontMetrics(Font paramFont)
  {
    return this.tk.getFontMetrics(paramFont);
  }

  public String[] getFontList()
  {
    return this.tk.getFontList();
  }

  public void addPropertyChangeListener(String paramString, PropertyChangeListener paramPropertyChangeListener)
  {
    this.tk.addPropertyChangeListener(paramString, paramPropertyChangeListener);
  }

  public void removePropertyChangeListener(String paramString, PropertyChangeListener paramPropertyChangeListener)
  {
    this.tk.removePropertyChangeListener(paramString, paramPropertyChangeListener);
  }

  public boolean isModalityTypeSupported(Dialog.ModalityType paramModalityType)
  {
    return false;
  }

  public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType paramModalExclusionType)
  {
    return false;
  }

  public void addAWTEventListener(AWTEventListener paramAWTEventListener, long paramLong)
  {
    this.tk.addAWTEventListener(paramAWTEventListener, paramLong);
  }

  public void removeAWTEventListener(AWTEventListener paramAWTEventListener)
  {
    this.tk.removeAWTEventListener(paramAWTEventListener);
  }

  public AWTEventListener[] getAWTEventListeners()
  {
    return this.tk.getAWTEventListeners();
  }

  public boolean isDesktopSupported()
  {
    return false;
  }

  public DesktopPeer createDesktopPeer(Desktop paramDesktop)
    throws HeadlessException
  {
    throw new HeadlessException();
  }
}
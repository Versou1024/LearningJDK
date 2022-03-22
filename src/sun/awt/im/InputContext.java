package sun.awt.im;

import java.awt.AWTEvent;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.im.InputMethodRequests;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import sun.awt.SunToolkit;

public class InputContext extends java.awt.im.InputContext
  implements ComponentListener, WindowListener
{
  private static final Logger log = Logger.getLogger("sun.awt.im.InputContext");
  private InputMethodLocator inputMethodLocator;
  private InputMethod inputMethod;
  private boolean inputMethodCreationFailed;
  private HashMap usedInputMethods;
  private Component currentClientComponent;
  private Component awtFocussedComponent;
  private boolean isInputMethodActive;
  private Character.Subset[] characterSubsets = null;
  private boolean compositionAreaHidden = false;
  private static InputContext inputMethodWindowContext;
  private static InputMethod previousInputMethod = null;
  private boolean clientWindowNotificationEnabled = false;
  private Window clientWindowListened;
  private Rectangle clientWindowLocation = null;
  private HashMap perInputMethodState;
  private static AWTKeyStroke inputMethodSelectionKey;
  private static boolean inputMethodSelectionKeyInitialized = false;
  private static final String inputMethodSelectionKeyPath = "/java/awt/im/selectionKey";
  private static final String inputMethodSelectionKeyCodeName = "keyCode";
  private static final String inputMethodSelectionKeyModifiersName = "modifiers";

  protected InputContext()
  {
    InputMethodManager localInputMethodManager = InputMethodManager.getInstance();
    synchronized (InputContext.class)
    {
      if (!(inputMethodSelectionKeyInitialized))
      {
        inputMethodSelectionKeyInitialized = true;
        if (localInputMethodManager.hasMultipleInputMethods())
          initializeInputMethodSelectionKey();
      }
    }
    selectInputMethod(localInputMethodManager.getDefaultKeyboardLocale());
  }

  public synchronized boolean selectInputMethod(Locale paramLocale)
  {
    if (paramLocale == null)
      throw new NullPointerException();
    if (this.inputMethod != null)
    {
      if (!(this.inputMethod.setLocale(paramLocale)))
        break label66;
      return true;
    }
    if ((this.inputMethodLocator != null) && (this.inputMethodLocator.isLocaleAvailable(paramLocale)))
    {
      this.inputMethodLocator = this.inputMethodLocator.deriveLocator(paramLocale);
      return true;
    }
    label66: InputMethodLocator localInputMethodLocator = InputMethodManager.getInstance().findInputMethod(paramLocale);
    if (localInputMethodLocator != null)
    {
      changeInputMethod(localInputMethodLocator);
      return true;
    }
    if ((this.inputMethod == null) && (this.inputMethodLocator != null))
    {
      this.inputMethod = getInputMethod();
      if (this.inputMethod != null)
        return this.inputMethod.setLocale(paramLocale);
    }
    return false;
  }

  public Locale getLocale()
  {
    if (this.inputMethod != null)
      return this.inputMethod.getLocale();
    if (this.inputMethodLocator != null)
      return this.inputMethodLocator.getLocale();
    return null;
  }

  public void setCharacterSubsets(Character.Subset[] paramArrayOfSubset)
  {
    if (paramArrayOfSubset == null)
    {
      this.characterSubsets = null;
    }
    else
    {
      this.characterSubsets = new Character.Subset[paramArrayOfSubset.length];
      System.arraycopy(paramArrayOfSubset, 0, this.characterSubsets, 0, this.characterSubsets.length);
    }
    if (this.inputMethod != null)
      this.inputMethod.setCharacterSubsets(paramArrayOfSubset);
  }

  public synchronized void reconvert()
  {
    InputMethod localInputMethod = getInputMethod();
    if (localInputMethod == null)
      throw new UnsupportedOperationException();
    localInputMethod.reconvert();
  }

  public void dispatchEvent(AWTEvent paramAWTEvent)
  {
    if (paramAWTEvent instanceof InputMethodEvent)
      return;
    if (paramAWTEvent instanceof FocusEvent)
    {
      localObject = ((FocusEvent)paramAWTEvent).getOppositeComponent();
      if ((localObject != null) && (getComponentWindow((Component)localObject) instanceof InputMethodWindow) && (((Component)localObject).getInputContext() == this))
        return;
    }
    Object localObject = getInputMethod();
    int i = paramAWTEvent.getID();
    switch (i)
    {
    case 1004:
      focusGained((Component)paramAWTEvent.getSource());
      break;
    case 1005:
      focusLost((Component)paramAWTEvent.getSource(), ((FocusEvent)paramAWTEvent).isTemporary());
      break;
    case 401:
      if (!(checkInputMethodSelectionKey((KeyEvent)paramAWTEvent)))
        break label154;
      InputMethodManager.getInstance().notifyChangeRequestByHotKey((Component)paramAWTEvent.getSource());
      break;
    default:
      if ((localObject == null) || (!(paramAWTEvent instanceof InputEvent)))
        label154: return;
      ((InputMethod)localObject).dispatchEvent(paramAWTEvent);
    }
  }

  private void focusGained(Component paramComponent)
  {
    synchronized (paramComponent.getTreeLock())
    {
      synchronized (this)
      {
        if ("sun.awt.im.CompositionArea".equals(paramComponent.getClass().getName()))
          break label102:
        if (getComponentWindow(paramComponent) instanceof InputMethodWindow)
          break label102:
        if (paramComponent.isDisplayable())
          break label54;
        monitorexit;
        return;
        label54: if ((this.inputMethod == null) || (this.currentClientComponent == null) || (this.currentClientComponent == paramComponent))
          break label97;
        if (this.isInputMethodActive)
          break label88;
        activateInputMethod(false);
        label88: endComposition();
        deactivateInputMethod(false);
        label97: this.currentClientComponent = paramComponent;
        label102: this.awtFocussedComponent = paramComponent;
        if (!(this.inputMethod instanceof InputMethodAdapter))
          break label128;
        ((InputMethodAdapter)this.inputMethod).setAWTFocussedComponent(paramComponent);
        label128: if (this.isInputMethodActive)
          break label140;
        activateInputMethod(true);
        label140: InputMethodContext localInputMethodContext = (InputMethodContext)this;
        if (localInputMethodContext.isCompositionAreaVisible())
          break label188;
        InputMethodRequests localInputMethodRequests = paramComponent.getInputMethodRequests();
        if ((localInputMethodRequests == null) || (!(localInputMethodContext.useBelowTheSpotInput())))
          break label182;
        localInputMethodContext.setCompositionAreaUndecorated(true);
        break label188:
        label182: localInputMethodContext.setCompositionAreaUndecorated(false);
        label188: if (this.compositionAreaHidden != true)
          break label209;
        ((InputMethodContext)this).setCompositionAreaVisible(true);
        label209: this.compositionAreaHidden = false;
      }
    }
  }

  private void activateInputMethod(boolean paramBoolean)
  {
    if ((inputMethodWindowContext != null) && (inputMethodWindowContext != this) && (inputMethodWindowContext.inputMethodLocator != null) && (!(inputMethodWindowContext.inputMethodLocator.sameInputMethod(this.inputMethodLocator))) && (inputMethodWindowContext.inputMethod != null))
      inputMethodWindowContext.inputMethod.hideWindows();
    inputMethodWindowContext = this;
    if (this.inputMethod != null)
    {
      if ((previousInputMethod != this.inputMethod) && (previousInputMethod instanceof InputMethodAdapter))
        ((InputMethodAdapter)previousInputMethod).stopListening();
      previousInputMethod = null;
      if (log.isLoggable(Level.FINE))
        log.fine("Current client component " + this.currentClientComponent);
      if (this.inputMethod instanceof InputMethodAdapter)
        ((InputMethodAdapter)this.inputMethod).setClientComponent(this.currentClientComponent);
      this.inputMethod.activate();
      this.isInputMethodActive = true;
      if (this.perInputMethodState != null)
      {
        ??? = (Boolean)this.perInputMethodState.remove(this.inputMethod);
        if (??? != null)
          this.clientWindowNotificationEnabled = ((Boolean)???).booleanValue();
      }
      if (this.clientWindowNotificationEnabled)
      {
        if (!(addedClientWindowListeners()))
          addClientWindowListeners();
        synchronized (this)
        {
          if (this.clientWindowListened != null)
            notifyClientWindowChange(this.clientWindowListened);
        }
      }
      else if (addedClientWindowListeners())
      {
        removeClientWindowListeners();
      }
    }
    InputMethodManager.getInstance().setInputContext(this);
    ((InputMethodContext)this).grabCompositionArea(paramBoolean);
  }

  static Window getComponentWindow(Component paramComponent)
  {
    while (true)
    {
      if (paramComponent == null)
        return null;
      if (paramComponent instanceof Window)
        return ((Window)paramComponent);
      paramComponent = paramComponent.getParent();
    }
  }

  private void focusLost(Component paramComponent, boolean paramBoolean)
  {
    synchronized (paramComponent.getTreeLock())
    {
      synchronized (this)
      {
        if (this.isInputMethodActive)
          deactivateInputMethod(paramBoolean);
        this.awtFocussedComponent = null;
        if (this.inputMethod instanceof InputMethodAdapter)
          ((InputMethodAdapter)this.inputMethod).setAWTFocussedComponent(null);
        InputMethodContext localInputMethodContext = (InputMethodContext)this;
        if (localInputMethodContext.isCompositionAreaVisible())
        {
          localInputMethodContext.setCompositionAreaVisible(false);
          this.compositionAreaHidden = true;
        }
      }
    }
  }

  private boolean checkInputMethodSelectionKey(KeyEvent paramKeyEvent)
  {
    if (inputMethodSelectionKey != null)
    {
      AWTKeyStroke localAWTKeyStroke = AWTKeyStroke.getAWTKeyStrokeForEvent(paramKeyEvent);
      return inputMethodSelectionKey.equals(localAWTKeyStroke);
    }
    return false;
  }

  private void deactivateInputMethod(boolean paramBoolean)
  {
    InputMethodManager.getInstance().setInputContext(null);
    if (this.inputMethod != null)
    {
      this.isInputMethodActive = false;
      this.inputMethod.deactivate(paramBoolean);
      previousInputMethod = this.inputMethod;
    }
  }

  synchronized void changeInputMethod(InputMethodLocator paramInputMethodLocator)
  {
    if (this.inputMethodLocator == null)
    {
      this.inputMethodLocator = paramInputMethodLocator;
      this.inputMethodCreationFailed = false;
      return;
    }
    if (this.inputMethodLocator.sameInputMethod(paramInputMethodLocator))
    {
      localLocale = paramInputMethodLocator.getLocale();
      if ((localLocale != null) && (this.inputMethodLocator.getLocale() != localLocale))
      {
        if (this.inputMethod != null)
          this.inputMethod.setLocale(localLocale);
        this.inputMethodLocator = paramInputMethodLocator;
      }
      return;
    }
    Locale localLocale = this.inputMethodLocator.getLocale();
    boolean bool1 = this.isInputMethodActive;
    int i = 0;
    boolean bool2 = false;
    if (this.inputMethod != null)
    {
      try
      {
        bool2 = this.inputMethod.isCompositionEnabled();
        i = 1;
      }
      catch (UnsupportedOperationException localUnsupportedOperationException1)
      {
      }
      if (this.currentClientComponent != null)
      {
        if (!(this.isInputMethodActive))
          activateInputMethod(false);
        endComposition();
        deactivateInputMethod(false);
        if (this.inputMethod instanceof InputMethodAdapter)
          ((InputMethodAdapter)this.inputMethod).setClientComponent(null);
      }
      localLocale = this.inputMethod.getLocale();
      if (this.usedInputMethods == null)
        this.usedInputMethods = new HashMap(5);
      if (this.perInputMethodState == null)
        this.perInputMethodState = new HashMap(5);
      this.usedInputMethods.put(this.inputMethodLocator.deriveLocator(null), this.inputMethod);
      this.perInputMethodState.put(this.inputMethod, new Boolean(this.clientWindowNotificationEnabled));
      enableClientWindowNotification(this.inputMethod, false);
      if (this == inputMethodWindowContext)
      {
        this.inputMethod.hideWindows();
        inputMethodWindowContext = null;
      }
      this.inputMethodLocator = null;
      this.inputMethod = null;
      this.inputMethodCreationFailed = false;
    }
    if ((paramInputMethodLocator.getLocale() == null) && (localLocale != null) && (paramInputMethodLocator.isLocaleAvailable(localLocale)))
      paramInputMethodLocator = paramInputMethodLocator.deriveLocator(localLocale);
    this.inputMethodLocator = paramInputMethodLocator;
    this.inputMethodCreationFailed = false;
    if (bool1)
    {
      this.inputMethod = getInputMethodInstance();
      if (this.inputMethod instanceof InputMethodAdapter)
        ((InputMethodAdapter)this.inputMethod).setAWTFocussedComponent(this.awtFocussedComponent);
      activateInputMethod(true);
    }
    if (i != 0)
    {
      this.inputMethod = getInputMethod();
      if (this.inputMethod != null)
        try
        {
          this.inputMethod.setCompositionEnabled(bool2);
        }
        catch (UnsupportedOperationException localUnsupportedOperationException2)
        {
        }
    }
  }

  Component getClientComponent()
  {
    return this.currentClientComponent;
  }

  public synchronized void removeNotify(Component paramComponent)
  {
    if (paramComponent == null)
      throw new NullPointerException();
    if (this.inputMethod == null)
    {
      if (paramComponent == this.currentClientComponent)
        this.currentClientComponent = null;
      return;
    }
    if (paramComponent == this.awtFocussedComponent)
      focusLost(paramComponent, false);
    if (paramComponent == this.currentClientComponent)
    {
      if (this.isInputMethodActive)
        deactivateInputMethod(false);
      this.inputMethod.removeNotify();
      if ((this.clientWindowNotificationEnabled) && (addedClientWindowListeners()))
        removeClientWindowListeners();
      this.currentClientComponent = null;
      if (this.inputMethod instanceof InputMethodAdapter)
        ((InputMethodAdapter)this.inputMethod).setClientComponent(null);
      if (EventQueue.isDispatchThread())
        ((InputMethodContext)this).releaseCompositionArea();
      else
        EventQueue.invokeLater(new Runnable(this)
        {
          public void run()
          {
            ((InputMethodContext)this.this$0).releaseCompositionArea();
          }
        });
    }
  }

  public synchronized void dispose()
  {
    if (this.currentClientComponent != null)
      throw new IllegalStateException("Can't dispose InputContext while it's active");
    if (this.inputMethod != null)
    {
      if (this == inputMethodWindowContext)
      {
        this.inputMethod.hideWindows();
        inputMethodWindowContext = null;
      }
      if (this.inputMethod == previousInputMethod)
        previousInputMethod = null;
      if (this.clientWindowNotificationEnabled)
      {
        if (addedClientWindowListeners())
          removeClientWindowListeners();
        this.clientWindowNotificationEnabled = false;
      }
      this.inputMethod.dispose();
      if (this.clientWindowNotificationEnabled)
        enableClientWindowNotification(this.inputMethod, false);
      this.inputMethod = null;
    }
    this.inputMethodLocator = null;
    if ((this.usedInputMethods != null) && (!(this.usedInputMethods.isEmpty())))
    {
      Iterator localIterator = this.usedInputMethods.values().iterator();
      this.usedInputMethods = null;
      while (localIterator.hasNext())
        ((InputMethod)localIterator.next()).dispose();
    }
    this.clientWindowNotificationEnabled = false;
    this.clientWindowListened = null;
    this.perInputMethodState = null;
  }

  public synchronized Object getInputMethodControlObject()
  {
    InputMethod localInputMethod = getInputMethod();
    if (localInputMethod != null)
      return localInputMethod.getControlObject();
    return null;
  }

  public void setCompositionEnabled(boolean paramBoolean)
  {
    InputMethod localInputMethod = getInputMethod();
    if (localInputMethod == null)
      throw new UnsupportedOperationException();
    localInputMethod.setCompositionEnabled(paramBoolean);
  }

  public boolean isCompositionEnabled()
  {
    InputMethod localInputMethod = getInputMethod();
    if (localInputMethod == null)
      throw new UnsupportedOperationException();
    return localInputMethod.isCompositionEnabled();
  }

  public String getInputMethodInfo()
  {
    InputMethod localInputMethod = getInputMethod();
    if (localInputMethod == null)
      throw new UnsupportedOperationException("Null input method");
    String str = null;
    if (localInputMethod instanceof InputMethodAdapter)
      str = ((InputMethodAdapter)localInputMethod).getNativeInputMethodInfo();
    if ((str == null) && (this.inputMethodLocator != null))
      str = this.inputMethodLocator.getDescriptor().getInputMethodDisplayName(getLocale(), SunToolkit.getStartupLocale());
    if ((str != null) && (!(str.equals(""))))
      return str;
    return localInputMethod.toString() + "-" + localInputMethod.getLocale().toString();
  }

  public void disableNativeIM()
  {
    InputMethod localInputMethod = getInputMethod();
    if ((localInputMethod != null) && (localInputMethod instanceof InputMethodAdapter))
      ((InputMethodAdapter)localInputMethod).disableInputMethod();
  }

  private synchronized InputMethod getInputMethod()
  {
    if (this.inputMethod != null)
      return this.inputMethod;
    if (this.inputMethodCreationFailed)
      return null;
    this.inputMethod = getInputMethodInstance();
    return this.inputMethod;
  }

  private InputMethod getInputMethodInstance()
  {
    InputMethodLocator localInputMethodLocator = this.inputMethodLocator;
    if (localInputMethodLocator == null)
    {
      this.inputMethodCreationFailed = true;
      return null;
    }
    Locale localLocale = localInputMethodLocator.getLocale();
    InputMethod localInputMethod = null;
    if (this.usedInputMethods != null)
    {
      localInputMethod = (InputMethod)this.usedInputMethods.remove(localInputMethodLocator.deriveLocator(null));
      if (localInputMethod != null)
      {
        if (localLocale != null)
          localInputMethod.setLocale(localLocale);
        localInputMethod.setCharacterSubsets(this.characterSubsets);
        Boolean localBoolean = (Boolean)this.perInputMethodState.remove(localInputMethod);
        if (localBoolean != null)
          enableClientWindowNotification(localInputMethod, localBoolean.booleanValue());
        ((InputMethodContext)this).setInputMethodSupportsBelowTheSpot((!(localInputMethod instanceof InputMethodAdapter)) || (((InputMethodAdapter)localInputMethod).supportsBelowTheSpot()));
        return localInputMethod;
      }
    }
    try
    {
      localInputMethod = localInputMethodLocator.getDescriptor().createInputMethod();
      if (localLocale != null)
        localInputMethod.setLocale(localLocale);
      localInputMethod.setInputMethodContext((InputMethodContext)this);
      localInputMethod.setCharacterSubsets(this.characterSubsets);
    }
    catch (Exception localException)
    {
      logCreationFailed(localException);
      this.inputMethodCreationFailed = true;
      if (localInputMethod != null)
        localInputMethod = null;
    }
    catch (LinkageError localLinkageError)
    {
      logCreationFailed(localLinkageError);
      this.inputMethodCreationFailed = true;
    }
    ((InputMethodContext)this).setInputMethodSupportsBelowTheSpot((!(localInputMethod instanceof InputMethodAdapter)) || (((InputMethodAdapter)localInputMethod).supportsBelowTheSpot()));
    return localInputMethod;
  }

  private void logCreationFailed(Throwable paramThrowable)
  {
    String str = Toolkit.getProperty("AWT.InputMethodCreationFailed", "Could not create {0}. Reason: {1}");
    Object[] arrayOfObject = { this.inputMethodLocator.getDescriptor().getInputMethodDisplayName(null, Locale.getDefault()), paramThrowable.getLocalizedMessage() };
    MessageFormat localMessageFormat = new MessageFormat(str);
    Logger localLogger = Logger.getLogger("sun.awt.im");
    localLogger.config(localMessageFormat.format(arrayOfObject));
  }

  InputMethodLocator getInputMethodLocator()
  {
    if (this.inputMethod != null)
      return this.inputMethodLocator.deriveLocator(this.inputMethod.getLocale());
    return this.inputMethodLocator;
  }

  public synchronized void endComposition()
  {
    if (this.inputMethod != null)
      this.inputMethod.endComposition();
  }

  synchronized void enableClientWindowNotification(InputMethod paramInputMethod, boolean paramBoolean)
  {
    if (paramInputMethod != this.inputMethod)
    {
      if (this.perInputMethodState == null)
        this.perInputMethodState = new HashMap(5);
      this.perInputMethodState.put(paramInputMethod, new Boolean(paramBoolean));
      return;
    }
    if (this.clientWindowNotificationEnabled != paramBoolean)
    {
      this.clientWindowLocation = null;
      this.clientWindowNotificationEnabled = paramBoolean;
    }
    if (this.clientWindowNotificationEnabled)
    {
      if (!(addedClientWindowListeners()))
        addClientWindowListeners();
      if (this.clientWindowListened != null)
      {
        this.clientWindowLocation = null;
        notifyClientWindowChange(this.clientWindowListened);
      }
    }
    else if (addedClientWindowListeners())
    {
      removeClientWindowListeners();
    }
  }

  private synchronized void notifyClientWindowChange(Window paramWindow)
  {
    if (this.inputMethod == null)
      return;
    if ((!(paramWindow.isVisible())) || ((paramWindow instanceof Frame) && (((Frame)paramWindow).getState() == 1)))
    {
      this.clientWindowLocation = null;
      this.inputMethod.notifyClientWindowChange(null);
      return;
    }
    Rectangle localRectangle = paramWindow.getBounds();
    if ((this.clientWindowLocation == null) || (!(this.clientWindowLocation.equals(localRectangle))))
    {
      this.clientWindowLocation = localRectangle;
      this.inputMethod.notifyClientWindowChange(this.clientWindowLocation);
    }
  }

  private synchronized void addClientWindowListeners()
  {
    Component localComponent = getClientComponent();
    if (localComponent == null)
      return;
    Window localWindow = getComponentWindow(localComponent);
    if (localWindow == null)
      return;
    localWindow.addComponentListener(this);
    localWindow.addWindowListener(this);
    this.clientWindowListened = localWindow;
  }

  private synchronized void removeClientWindowListeners()
  {
    this.clientWindowListened.removeComponentListener(this);
    this.clientWindowListened.removeWindowListener(this);
    this.clientWindowListened = null;
  }

  private boolean addedClientWindowListeners()
  {
    return (this.clientWindowListened != null);
  }

  public void componentResized(ComponentEvent paramComponentEvent)
  {
    notifyClientWindowChange((Window)paramComponentEvent.getComponent());
  }

  public void componentMoved(ComponentEvent paramComponentEvent)
  {
    notifyClientWindowChange((Window)paramComponentEvent.getComponent());
  }

  public void componentShown(ComponentEvent paramComponentEvent)
  {
    notifyClientWindowChange((Window)paramComponentEvent.getComponent());
  }

  public void componentHidden(ComponentEvent paramComponentEvent)
  {
    notifyClientWindowChange((Window)paramComponentEvent.getComponent());
  }

  public void windowOpened(WindowEvent paramWindowEvent)
  {
  }

  public void windowClosing(WindowEvent paramWindowEvent)
  {
  }

  public void windowClosed(WindowEvent paramWindowEvent)
  {
  }

  public void windowIconified(WindowEvent paramWindowEvent)
  {
    notifyClientWindowChange(paramWindowEvent.getWindow());
  }

  public void windowDeiconified(WindowEvent paramWindowEvent)
  {
    notifyClientWindowChange(paramWindowEvent.getWindow());
  }

  public void windowActivated(WindowEvent paramWindowEvent)
  {
  }

  public void windowDeactivated(WindowEvent paramWindowEvent)
  {
  }

  private void initializeInputMethodSelectionKey()
  {
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        Preferences localPreferences = Preferences.userRoot();
        InputContext.access$002(InputContext.access$100(this.this$0, localPreferences));
        if (InputContext.access$000() == null)
        {
          localPreferences = Preferences.systemRoot();
          InputContext.access$002(InputContext.access$100(this.this$0, localPreferences));
        }
        return null;
      }
    });
  }

  private AWTKeyStroke getInputMethodSelectionKeyStroke(Preferences paramPreferences)
  {
    try
    {
      if (paramPreferences.nodeExists("/java/awt/im/selectionKey"))
      {
        Preferences localPreferences = paramPreferences.node("/java/awt/im/selectionKey");
        int i = localPreferences.getInt("keyCode", 0);
        if (i != 0)
        {
          int j = localPreferences.getInt("modifiers", 0);
          return AWTKeyStroke.getAWTKeyStroke(i, j);
        }
      }
    }
    catch (BackingStoreException localBackingStoreException)
    {
    }
    return null;
  }
}
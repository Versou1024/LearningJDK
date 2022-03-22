package sun.awt.im;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.awt.im.spi.InputMethodDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import sun.awt.AppContext;
import sun.awt.InputMethodSupport;
import sun.awt.SunToolkit;
import sun.misc.Service;

class ExecutableInputMethodManager extends InputMethodManager
  implements Runnable
{
  private InputContext currentInputContext;
  private String triggerMenuString;
  private InputMethodPopupMenu selectionMenu;
  private static String selectInputMethodMenuTitle;
  private InputMethodLocator hostAdapterLocator;
  private int javaInputMethodCount;
  private Vector javaInputMethodLocatorList;
  private Component requestComponent;
  private InputContext requestInputContext;
  private static final String preferredIMNode = "/sun/awt/im/preferredInputMethod";
  private static final String descriptorKey = "descriptor";
  private Hashtable preferredLocatorCache = new Hashtable();
  private Preferences userRoot;

  ExecutableInputMethodManager()
  {
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    try
    {
      if (localToolkit instanceof InputMethodSupport)
      {
        InputMethodDescriptor localInputMethodDescriptor = ((InputMethodSupport)localToolkit).getInputMethodAdapterDescriptor();
        if (localInputMethodDescriptor != null)
          this.hostAdapterLocator = new InputMethodLocator(localInputMethodDescriptor, null, null);
      }
    }
    catch (AWTException localAWTException)
    {
    }
    this.javaInputMethodLocatorList = new Vector();
    initializeInputMethodLocatorList();
  }

  synchronized void initialize()
  {
    selectInputMethodMenuTitle = Toolkit.getProperty("AWT.InputMethodSelectionMenu", "Select Input Method");
    this.triggerMenuString = selectInputMethodMenuTitle;
  }

  public void run()
  {
    label0: if (!(hasMultipleInputMethods()));
    try
    {
      synchronized (this)
      {
        wait();
      }
    }
    catch (InterruptedException localInvocationTargetException)
    {
      break label0:
      waitForChangeRequest();
      initializeInputMethodLocatorList();
      try
      {
        if (this.requestComponent != null)
          try
          {
            showInputMethodMenuOnRequesterEDT(this.requestComponent);
          }
          catch (Exception localException)
          {
            throw new RuntimeException(localException);
          }
        EventQueue.invokeAndWait(new Runnable(this)
        {
          public void run()
          {
            ExecutableInputMethodManager.access$000(this.this$0);
          }
        });
      }
      catch (InterruptedException localInterruptedException2)
      {
      }
      catch (InvocationTargetException localInvocationTargetException)
      {
      }
    }
  }

  private void showInputMethodMenuOnRequesterEDT(Component paramComponent)
    throws InterruptedException, InvocationTargetException
  {
    if (paramComponent == null)
      return;
    1AWTInvocationLock local1AWTInvocationLock = new Object(this)
    {
    };
    InvocationEvent localInvocationEvent = new InvocationEvent(paramComponent, new Runnable(this)
    {
      public void run()
      {
        ExecutableInputMethodManager.access$000(this.this$0);
      }
    }
    , local1AWTInvocationLock, true);
    AppContext localAppContext = SunToolkit.targetToAppContext(paramComponent);
    synchronized (local1AWTInvocationLock)
    {
      SunToolkit.postEvent(localAppContext, localInvocationEvent);
    }
    ??? = localInvocationEvent.getThrowable();
    if (??? != null)
      throw new InvocationTargetException((Throwable)???);
  }

  void setInputContext(InputContext paramInputContext)
  {
    if ((this.currentInputContext != null) && (paramInputContext != null));
    this.currentInputContext = paramInputContext;
  }

  public synchronized void notifyChangeRequest(Component paramComponent)
  {
    if ((!(paramComponent instanceof Frame)) && (!(paramComponent instanceof Dialog)))
      return;
    if (this.requestComponent != null)
      return;
    this.requestComponent = paramComponent;
    notify();
  }

  public synchronized void notifyChangeRequestByHotKey(Component paramComponent)
  {
    while ((!(paramComponent instanceof Frame)) && (!(paramComponent instanceof Dialog)))
    {
      if (paramComponent == null)
        return;
      paramComponent = paramComponent.getParent();
    }
    notifyChangeRequest(paramComponent);
  }

  public String getTriggerMenuString()
  {
    return this.triggerMenuString;
  }

  boolean hasMultipleInputMethods()
  {
    return (((this.hostAdapterLocator != null) && (this.javaInputMethodCount > 0)) || (this.javaInputMethodCount > 1));
  }

  private synchronized void waitForChangeRequest()
  {
    try
    {
      while (this.requestComponent == null)
        wait();
    }
    catch (InterruptedException localInterruptedException)
    {
    }
  }

  private void initializeInputMethodLocatorList()
  {
    synchronized (this.javaInputMethodLocatorList)
    {
      this.javaInputMethodLocatorList.clear();
      try
      {
        AccessController.doPrivileged(new PrivilegedExceptionAction(this)
        {
          public Object run()
          {
            Iterator localIterator = Service.installedProviders(InputMethodDescriptor.class);
            while (localIterator.hasNext())
            {
              InputMethodDescriptor localInputMethodDescriptor = (InputMethodDescriptor)localIterator.next();
              ClassLoader localClassLoader = localInputMethodDescriptor.getClass().getClassLoader();
              ExecutableInputMethodManager.access$100(this.this$0).add(new InputMethodLocator(localInputMethodDescriptor, localClassLoader, null));
            }
            return null;
          }
        });
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        localPrivilegedActionException.printStackTrace();
      }
      this.javaInputMethodCount = this.javaInputMethodLocatorList.size();
    }
    if (hasMultipleInputMethods())
      if (this.userRoot == null)
        this.userRoot = getUserRoot();
    else
      this.triggerMenuString = null;
  }

  private void showInputMethodMenu()
  {
    if (!(hasMultipleInputMethods()))
    {
      this.requestComponent = null;
      return;
    }
    this.selectionMenu = InputMethodPopupMenu.getInstance(this.requestComponent, selectInputMethodMenuTitle);
    this.selectionMenu.removeAll();
    String str = getCurrentSelection();
    if (this.hostAdapterLocator != null)
    {
      this.selectionMenu.addOneInputMethodToMenu(this.hostAdapterLocator, str);
      this.selectionMenu.addSeparator();
    }
    for (int i = 0; i < this.javaInputMethodLocatorList.size(); ++i)
    {
      InputMethodLocator localInputMethodLocator = (InputMethodLocator)this.javaInputMethodLocatorList.get(i);
      this.selectionMenu.addOneInputMethodToMenu(localInputMethodLocator, str);
    }
    synchronized (this)
    {
      this.selectionMenu.addToComponent(this.requestComponent);
      this.requestInputContext = this.currentInputContext;
      this.selectionMenu.show(this.requestComponent, 60, 80);
      this.requestComponent = null;
    }
  }

  private String getCurrentSelection()
  {
    InputContext localInputContext = this.currentInputContext;
    if (localInputContext != null)
    {
      InputMethodLocator localInputMethodLocator = localInputContext.getInputMethodLocator();
      if (localInputMethodLocator != null)
        return localInputMethodLocator.getActionCommandString();
    }
    return null;
  }

  synchronized void changeInputMethod(String paramString)
  {
    Object localObject2;
    String str4;
    Object localObject1 = null;
    String str1 = paramString;
    String str2 = null;
    int i = paramString.indexOf(10);
    if (i != -1)
    {
      str2 = paramString.substring(i + 1);
      str1 = paramString.substring(0, i);
    }
    if (this.hostAdapterLocator.getActionCommandString().equals(str1))
      localObject1 = this.hostAdapterLocator;
    else
      for (int j = 0; j < this.javaInputMethodLocatorList.size(); ++j)
      {
        localObject2 = (InputMethodLocator)this.javaInputMethodLocatorList.get(j);
        str4 = ((InputMethodLocator)localObject2).getActionCommandString();
        if (str4.equals(str1))
        {
          localObject1 = localObject2;
          break;
        }
      }
    if ((localObject1 != null) && (str2 != null))
    {
      String str3 = "";
      localObject2 = "";
      str4 = "";
      int k = str2.indexOf(95);
      if (k == -1)
      {
        str3 = str2;
      }
      else
      {
        str3 = str2.substring(0, k);
        int l = k + 1;
        k = str2.indexOf(95, l);
        if (k == -1)
        {
          localObject2 = str2.substring(l);
        }
        else
        {
          localObject2 = str2.substring(l, k);
          str4 = str2.substring(k + 1);
        }
      }
      Locale localLocale = new Locale(str3, (String)localObject2, str4);
      localObject1 = ((InputMethodLocator)localObject1).deriveLocator(localLocale);
    }
    if (localObject1 == null)
      return;
    if (this.requestInputContext != null)
    {
      this.requestInputContext.changeInputMethod((InputMethodLocator)localObject1);
      this.requestInputContext = null;
      putPreferredInputMethod((InputMethodLocator)localObject1);
    }
  }

  InputMethodLocator findInputMethod(Locale paramLocale)
  {
    InputMethodLocator localInputMethodLocator1 = getPreferredInputMethod(paramLocale);
    if (localInputMethodLocator1 != null)
      return localInputMethodLocator1;
    if ((this.hostAdapterLocator != null) && (this.hostAdapterLocator.isLocaleAvailable(paramLocale)))
      return this.hostAdapterLocator.deriveLocator(paramLocale);
    initializeInputMethodLocatorList();
    for (int i = 0; i < this.javaInputMethodLocatorList.size(); ++i)
    {
      InputMethodLocator localInputMethodLocator2 = (InputMethodLocator)this.javaInputMethodLocatorList.get(i);
      if (localInputMethodLocator2.isLocaleAvailable(paramLocale))
        return localInputMethodLocator2.deriveLocator(paramLocale);
    }
    return null;
  }

  Locale getDefaultKeyboardLocale()
  {
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    if (localToolkit instanceof InputMethodSupport)
      return ((InputMethodSupport)localToolkit).getDefaultKeyboardLocale();
    return Locale.getDefault();
  }

  private synchronized InputMethodLocator getPreferredInputMethod(Locale paramLocale)
  {
    InputMethodLocator localInputMethodLocator1 = null;
    if (!(hasMultipleInputMethods()))
      return null;
    localInputMethodLocator1 = (InputMethodLocator)this.preferredLocatorCache.get(paramLocale.toString().intern());
    if (localInputMethodLocator1 != null)
      return localInputMethodLocator1;
    String str1 = findPreferredInputMethodNode(paramLocale);
    String str2 = readPreferredInputMethod(str1);
    if (str2 != null)
    {
      Locale localLocale;
      if ((this.hostAdapterLocator != null) && (this.hostAdapterLocator.getDescriptor().getClass().getName().equals(str2)))
      {
        localLocale = getAdvertisedLocale(this.hostAdapterLocator, paramLocale);
        if (localLocale != null)
        {
          localInputMethodLocator1 = this.hostAdapterLocator.deriveLocator(localLocale);
          this.preferredLocatorCache.put(paramLocale.toString().intern(), localInputMethodLocator1);
        }
        return localInputMethodLocator1;
      }
      for (int i = 0; i < this.javaInputMethodLocatorList.size(); ++i)
      {
        InputMethodLocator localInputMethodLocator2 = (InputMethodLocator)this.javaInputMethodLocatorList.get(i);
        InputMethodDescriptor localInputMethodDescriptor = localInputMethodLocator2.getDescriptor();
        if (localInputMethodDescriptor.getClass().getName().equals(str2))
        {
          localLocale = getAdvertisedLocale(localInputMethodLocator2, paramLocale);
          if (localLocale != null)
          {
            localInputMethodLocator1 = localInputMethodLocator2.deriveLocator(localLocale);
            this.preferredLocatorCache.put(paramLocale.toString().intern(), localInputMethodLocator1);
          }
          return localInputMethodLocator1;
        }
      }
      writePreferredInputMethod(str1, null);
    }
    return null;
  }

  private String findPreferredInputMethodNode(Locale paramLocale)
  {
    if (this.userRoot == null)
      return null;
    for (String str = "/sun/awt/im/preferredInputMethod/" + createLocalePath(paramLocale); !(str.equals("/sun/awt/im/preferredInputMethod")); str = str.substring(0, str.lastIndexOf(47)))
      try
      {
        if ((this.userRoot.nodeExists(str)) && (readPreferredInputMethod(str) != null))
          return str;
      }
      catch (BackingStoreException localBackingStoreException)
      {
      }
    return null;
  }

  private String readPreferredInputMethod(String paramString)
  {
    if ((this.userRoot == null) || (paramString == null))
      return null;
    return this.userRoot.node(paramString).get("descriptor", null);
  }

  private synchronized void putPreferredInputMethod(InputMethodLocator paramInputMethodLocator)
  {
    InputMethodDescriptor localInputMethodDescriptor = paramInputMethodLocator.getDescriptor();
    Locale localLocale = paramInputMethodLocator.getLocale();
    if (localLocale == null)
      try
      {
        Locale[] arrayOfLocale = localInputMethodDescriptor.getAvailableLocales();
        if (arrayOfLocale.length == 1)
          localLocale = arrayOfLocale[0];
        else
          return;
      }
      catch (AWTException localAWTException)
      {
        return;
      }
    if (localLocale.equals(Locale.JAPAN))
      localLocale = Locale.JAPANESE;
    if (localLocale.equals(Locale.KOREA))
      localLocale = Locale.KOREAN;
    if (localLocale.equals(new Locale("th", "TH")))
      localLocale = new Locale("th");
    String str = "/sun/awt/im/preferredInputMethod/" + createLocalePath(localLocale);
    writePreferredInputMethod(str, localInputMethodDescriptor.getClass().getName());
    this.preferredLocatorCache.put(localLocale.toString().intern(), paramInputMethodLocator.deriveLocator(localLocale));
  }

  private String createLocalePath(Locale paramLocale)
  {
    String str1 = paramLocale.getLanguage();
    String str2 = paramLocale.getCountry();
    String str3 = paramLocale.getVariant();
    String str4 = null;
    if (!(str3.equals("")))
      str4 = "_" + str1 + "/_" + str2 + "/_" + str3;
    else if (!(str2.equals("")))
      str4 = "_" + str1 + "/_" + str2;
    else
      str4 = "_" + str1;
    return str4;
  }

  private void writePreferredInputMethod(String paramString1, String paramString2)
  {
    if (this.userRoot != null)
    {
      Preferences localPreferences = this.userRoot.node(paramString1);
      if (paramString2 != null)
        localPreferences.put("descriptor", paramString2);
      else
        localPreferences.remove("descriptor");
    }
  }

  private Preferences getUserRoot()
  {
    return ((Preferences)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        return Preferences.userRoot();
      }
    }));
  }

  private Locale getAdvertisedLocale(InputMethodLocator paramInputMethodLocator, Locale paramLocale)
  {
    Locale localLocale = null;
    if (paramInputMethodLocator.isLocaleAvailable(paramLocale))
      localLocale = paramLocale;
    else if (paramLocale.getLanguage().equals("ja"))
      if (paramInputMethodLocator.isLocaleAvailable(Locale.JAPAN))
        localLocale = Locale.JAPAN;
      else if (paramInputMethodLocator.isLocaleAvailable(Locale.JAPANESE))
        localLocale = Locale.JAPANESE;
    else if (paramLocale.getLanguage().equals("ko"))
      if (paramInputMethodLocator.isLocaleAvailable(Locale.KOREA))
        localLocale = Locale.KOREA;
      else if (paramInputMethodLocator.isLocaleAvailable(Locale.KOREAN))
        localLocale = Locale.KOREAN;
    else if (paramLocale.getLanguage().equals("th"))
      if (paramInputMethodLocator.isLocaleAvailable(new Locale("th", "TH")))
        localLocale = new Locale("th", "TH");
      else if (paramInputMethodLocator.isLocaleAvailable(new Locale("th")))
        localLocale = new Locale("th");
    return localLocale;
  }
}
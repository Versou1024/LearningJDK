package sun.applet;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import javax.print.attribute.HashPrintRequestAttributeSet;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.misc.Ref;

public class AppletViewer extends Frame
  implements AppletContext, Printable
{
  private static String defaultSaveFile = "Applet.ser";
  AppletViewerPanel panel;
  Label label;
  PrintStream statusMsgStream;
  AppletViewerFactory factory;
  private static Map audioClips = new HashMap();
  private static Map imageRefs = new HashMap();
  static Vector appletPanels = new Vector();
  static Hashtable systemParam = new Hashtable();
  static AppletProps props;
  static int c;
  private static int x;
  private static int y;
  private static final int XDELTA = 30;
  private static final int YDELTA = 30;
  static String encoding;
  private static AppletMessageHandler amh;

  public AppletViewer(int paramInt1, int paramInt2, URL paramURL, Hashtable paramHashtable, PrintStream paramPrintStream, AppletViewerFactory paramAppletViewerFactory)
  {
    this.factory = paramAppletViewerFactory;
    this.statusMsgStream = paramPrintStream;
    setTitle(amh.getMessage("tool.title", paramHashtable.get("code")));
    MenuBar localMenuBar = paramAppletViewerFactory.getBaseMenuBar();
    Menu localMenu = new Menu(amh.getMessage("menu.applet"));
    addMenuItem(localMenu, "menuitem.restart");
    addMenuItem(localMenu, "menuitem.reload");
    addMenuItem(localMenu, "menuitem.stop");
    addMenuItem(localMenu, "menuitem.save");
    addMenuItem(localMenu, "menuitem.start");
    addMenuItem(localMenu, "menuitem.clone");
    localMenu.add(new MenuItem("-"));
    addMenuItem(localMenu, "menuitem.tag");
    addMenuItem(localMenu, "menuitem.info");
    addMenuItem(localMenu, "menuitem.edit").disable();
    addMenuItem(localMenu, "menuitem.encoding");
    localMenu.add(new MenuItem("-"));
    addMenuItem(localMenu, "menuitem.print");
    localMenu.add(new MenuItem("-"));
    addMenuItem(localMenu, "menuitem.props");
    localMenu.add(new MenuItem("-"));
    addMenuItem(localMenu, "menuitem.close");
    if (paramAppletViewerFactory.isStandalone())
      addMenuItem(localMenu, "menuitem.quit");
    localMenuBar.add(localMenu);
    setMenuBar(localMenuBar);
    add("Center", this.panel = new AppletViewerPanel(paramURL, paramHashtable));
    add("South", this.label = new Label(amh.getMessage("label.hello")));
    this.panel.init();
    appletPanels.addElement(this.panel);
    pack();
    move(paramInt1, paramInt2);
    setVisible(true);
    1 local1 = new WindowAdapter(this)
    {
      public void windowClosing()
      {
        this.this$0.appletClose();
      }

      public void windowIconified()
      {
        this.this$0.appletStop();
      }

      public void windowDeiconified()
      {
        this.this$0.appletStart();
      }
    };
    addWindowListener(local1);
    this.panel.addAppletListener(new AppletListener(this, this)
    {
      final Frame frame;

      public void appletStateChanged()
      {
        AppletPanel localAppletPanel = (AppletPanel)paramAppletEvent.getSource();
        switch (paramAppletEvent.getID())
        {
        case 51234:
          if (localAppletPanel == null)
            return;
          this.this$0.resize(this.this$0.preferredSize());
          this.this$0.validate();
          break;
        case 51236:
          Applet localApplet = localAppletPanel.getApplet();
          if (localApplet != null)
          {
            AppletPanel.changeFrameAppContext(this.frame, SunToolkit.targetToAppContext(localApplet));
            return;
          }
          AppletPanel.changeFrameAppContext(this.frame, AppContext.getAppContext());
        }
      }
    });
    showStatus(amh.getMessage("status.start"));
    initEventQueue();
  }

  public MenuItem addMenuItem(Menu paramMenu, String paramString)
  {
    MenuItem localMenuItem = new MenuItem(amh.getMessage(paramString));
    localMenuItem.addActionListener(new UserActionListener(this, null));
    return paramMenu.add(localMenuItem);
  }

  private void initEventQueue()
  {
    String str = java.lang.System.getProperty("appletviewer.send.event");
    if (str == null)
    {
      this.panel.sendEvent(1);
      this.panel.sendEvent(2);
      this.panel.sendEvent(3);
    }
    else
    {
      String[] arrayOfString = splitSeparator(",", str);
      for (int i = 0; i < arrayOfString.length; ++i)
      {
        java.lang.System.out.println("Adding event to queue: " + arrayOfString[i]);
        if (arrayOfString[i].equals("dispose"))
          this.panel.sendEvent(0);
        else if (arrayOfString[i].equals("load"))
          this.panel.sendEvent(1);
        else if (arrayOfString[i].equals("init"))
          this.panel.sendEvent(2);
        else if (arrayOfString[i].equals("start"))
          this.panel.sendEvent(3);
        else if (arrayOfString[i].equals("stop"))
          this.panel.sendEvent(4);
        else if (arrayOfString[i].equals("destroy"))
          this.panel.sendEvent(5);
        else if (arrayOfString[i].equals("quit"))
          this.panel.sendEvent(6);
        else if (arrayOfString[i].equals("error"))
          this.panel.sendEvent(7);
        else
          java.lang.System.out.println("Unrecognized event name: " + arrayOfString[i]);
      }
      while (!(this.panel.emptyEventQueue()));
      appletSystemExit();
    }
  }

  private String[] splitSeparator(String paramString1, String paramString2)
  {
    Vector localVector = new Vector();
    int i = 0;
    int j = 0;
    while ((j = paramString2.indexOf(paramString1, i)) != -1)
    {
      localVector.addElement(paramString2.substring(i, j));
      i = j + 1;
    }
    localVector.addElement(paramString2.substring(i));
    String[] arrayOfString = new String[localVector.size()];
    localVector.copyInto(arrayOfString);
    return arrayOfString;
  }

  public AudioClip getAudioClip(URL paramURL)
  {
    checkConnect(paramURL);
    synchronized (audioClips)
    {
      Object localObject1 = (AudioClip)audioClips.get(paramURL);
      if (localObject1 == null)
        audioClips.put(paramURL, localObject1 = new AppletAudioClip(paramURL));
      return localObject1;
    }
  }

  public Image getImage(URL paramURL)
  {
    return getCachedImage(paramURL);
  }

  static Image getCachedImage(URL paramURL)
  {
    return ((Image)getCachedImageRef(paramURL).get());
  }

  static Ref getCachedImageRef(URL paramURL)
  {
    synchronized (imageRefs)
    {
      AppletImageRef localAppletImageRef = (AppletImageRef)imageRefs.get(paramURL);
      if (localAppletImageRef == null)
      {
        localAppletImageRef = new AppletImageRef(paramURL);
        imageRefs.put(paramURL, localAppletImageRef);
      }
      return localAppletImageRef;
    }
  }

  static void flushImageCache()
  {
    imageRefs.clear();
  }

  public Applet getApplet(String paramString)
  {
    AppletSecurity localAppletSecurity = (AppletSecurity)java.lang.System.getSecurityManager();
    paramString = paramString.toLowerCase();
    SocketPermission localSocketPermission1 = new SocketPermission(this.panel.getCodeBase().getHost(), "connect");
    Enumeration localEnumeration = appletPanels.elements();
    while (localEnumeration.hasMoreElements())
    {
      AppletPanel localAppletPanel = (AppletPanel)localEnumeration.nextElement();
      String str = localAppletPanel.getParameter("name");
      if (str != null)
        str = str.toLowerCase();
      if ((paramString.equals(str)) && (localAppletPanel.getDocumentBase().equals(this.panel.getDocumentBase())))
      {
        SocketPermission localSocketPermission2 = new SocketPermission(localAppletPanel.getCodeBase().getHost(), "connect");
        if (localSocketPermission1.implies(localSocketPermission2))
          return localAppletPanel.applet;
      }
    }
    return null;
  }

  public Enumeration getApplets()
  {
    AppletSecurity localAppletSecurity = (AppletSecurity)java.lang.System.getSecurityManager();
    Vector localVector = new Vector();
    SocketPermission localSocketPermission1 = new SocketPermission(this.panel.getCodeBase().getHost(), "connect");
    Enumeration localEnumeration = appletPanels.elements();
    while (localEnumeration.hasMoreElements())
    {
      AppletPanel localAppletPanel = (AppletPanel)localEnumeration.nextElement();
      if (localAppletPanel.getDocumentBase().equals(this.panel.getDocumentBase()))
      {
        SocketPermission localSocketPermission2 = new SocketPermission(localAppletPanel.getCodeBase().getHost(), "connect");
        if (localSocketPermission1.implies(localSocketPermission2))
          localVector.addElement(localAppletPanel.applet);
      }
    }
    return localVector.elements();
  }

  public void showDocument(URL paramURL)
  {
  }

  public void showDocument(URL paramURL, String paramString)
  {
  }

  public void showStatus(String paramString)
  {
    this.label.setText(paramString);
  }

  public void setStream(String paramString, InputStream paramInputStream)
    throws IOException
  {
  }

  public InputStream getStream(String paramString)
  {
    return null;
  }

  public Iterator getStreamKeys()
  {
    return null;
  }

  public static void printTag(PrintStream paramPrintStream, Hashtable paramHashtable)
  {
    String str2;
    paramPrintStream.print("<applet");
    String str1 = (String)paramHashtable.get("codebase");
    if (str1 != null)
      paramPrintStream.print(" codebase=\"" + str1 + "\"");
    str1 = (String)paramHashtable.get("code");
    if (str1 == null)
      str1 = "applet.class";
    paramPrintStream.print(" code=\"" + str1 + "\"");
    str1 = (String)paramHashtable.get("width");
    if (str1 == null)
      str1 = "150";
    paramPrintStream.print(" width=" + str1);
    str1 = (String)paramHashtable.get("height");
    if (str1 == null)
      str1 = "100";
    paramPrintStream.print(" height=" + str1);
    str1 = (String)paramHashtable.get("name");
    if (str1 != null)
      paramPrintStream.print(" name=\"" + str1 + "\"");
    paramPrintStream.println(">");
    int i = paramHashtable.size();
    String[] arrayOfString = new String[i];
    i = 0;
    Enumeration localEnumeration = paramHashtable.keys();
    while (localEnumeration.hasMoreElements())
    {
      str2 = (String)localEnumeration.nextElement();
      for (int k = 0; k < i; ++k)
        if (arrayOfString[k].compareTo(str2) >= 0)
          break;
      java.lang.System.arraycopy(arrayOfString, k, arrayOfString, k + 1, i - k);
      arrayOfString[k] = str2;
      ++i;
    }
    for (int j = 0; j < i; ++j)
    {
      str2 = arrayOfString[j];
      if (systemParam.get(str2) == null)
        paramPrintStream.println("<param name=" + str2 + " value=\"" + paramHashtable.get(str2) + "\">");
    }
    paramPrintStream.println("</applet>");
  }

  public void updateAtts()
  {
    Dimension localDimension = this.panel.size();
    Insets localInsets = this.panel.insets();
    this.panel.atts.put("width", new Integer(localDimension.width - localInsets.left + localInsets.right).toString());
    this.panel.atts.put("height", new Integer(localDimension.height - localInsets.top + localInsets.bottom).toString());
  }

  void appletRestart()
  {
    this.panel.sendEvent(4);
    this.panel.sendEvent(5);
    this.panel.sendEvent(2);
    this.panel.sendEvent(3);
  }

  void appletReload()
  {
    this.panel.sendEvent(4);
    this.panel.sendEvent(5);
    this.panel.sendEvent(0);
    AppletPanel.flushClassLoader(this.panel.getClassLoaderCacheKey());
    try
    {
      this.panel.joinAppletThread();
      this.panel.release();
    }
    catch (InterruptedException localInterruptedException)
    {
      return;
    }
    this.panel.createAppletThread();
    this.panel.sendEvent(1);
    this.panel.sendEvent(2);
    this.panel.sendEvent(3);
  }

  void appletSave()
  {
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        this.this$0.panel.sendEvent(4);
        FileDialog localFileDialog = new FileDialog(this.this$0, AppletViewer.access$100().getMessage("appletsave.filedialogtitle"), 1);
        localFileDialog.setDirectory(java.lang.System.getProperty("user.dir"));
        localFileDialog.setFile(AppletViewer.access$200());
        localFileDialog.show();
        String str1 = localFileDialog.getFile();
        if (str1 == null)
        {
          this.this$0.panel.sendEvent(3);
          return null;
        }
        String str2 = localFileDialog.getDirectory();
        File localFile = new File(str2, str1);
        try
        {
          BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile));
          ObjectOutputStream localObjectOutputStream = new ObjectOutputStream(localBufferedOutputStream);
          this.this$0.showStatus(AppletViewer.access$100().getMessage("appletsave.err1", this.this$0.panel.applet.toString(), localFile.toString()));
          localObjectOutputStream.writeObject(this.this$0.panel.applet);
        }
        catch (IOException localIOException)
        {
          java.lang.System.err.println(AppletViewer.access$100().getMessage("appletsave.err2", localIOException));
        }
        finally
        {
          this.this$0.panel.sendEvent(3);
        }
        return null;
      }
    });
  }

  void appletClone()
  {
    Point localPoint = location();
    updateAtts();
    this.factory.createAppletViewer(localPoint.x + 30, localPoint.y + 30, this.panel.documentURL, (Hashtable)this.panel.atts.clone());
  }

  void appletTag()
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    updateAtts();
    printTag(new PrintStream(localByteArrayOutputStream), this.panel.atts);
    showStatus(amh.getMessage("applettag"));
    Point localPoint = location();
    new TextFrame(localPoint.x + 30, localPoint.y + 30, amh.getMessage("applettag.textframe"), localByteArrayOutputStream.toString());
  }

  void appletInfo()
  {
    String str = this.panel.applet.getAppletInfo();
    if (str == null)
      str = amh.getMessage("appletinfo.applet");
    str = str + "\n\n";
    String[][] arrayOfString = this.panel.applet.getParameterInfo();
    if (arrayOfString != null)
      for (int i = 0; i < arrayOfString.length; ++i)
        str = str + arrayOfString[i][0] + " -- " + arrayOfString[i][1] + " -- " + arrayOfString[i][2] + "\n";
    else
      str = str + amh.getMessage("appletinfo.param");
    Point localPoint = location();
    new TextFrame(localPoint.x + 30, localPoint.y + 30, amh.getMessage("appletinfo.textframe"), str);
  }

  void appletCharacterEncoding()
  {
    showStatus(amh.getMessage("appletencoding", encoding));
  }

  void appletEdit()
  {
  }

  void appletPrint()
  {
    PrinterJob localPrinterJob = PrinterJob.getPrinterJob();
    if (localPrinterJob != null)
    {
      HashPrintRequestAttributeSet localHashPrintRequestAttributeSet = new HashPrintRequestAttributeSet();
      if (localPrinterJob.printDialog(localHashPrintRequestAttributeSet))
      {
        localPrinterJob.setPrintable(this);
        try
        {
          localPrinterJob.print(localHashPrintRequestAttributeSet);
          this.statusMsgStream.println(amh.getMessage("appletprint.finish"));
        }
        catch (PrinterException localPrinterException)
        {
          this.statusMsgStream.println(amh.getMessage("appletprint.fail"));
        }
      }
      else
      {
        this.statusMsgStream.println(amh.getMessage("appletprint.cancel"));
      }
    }
    else
    {
      this.statusMsgStream.println(amh.getMessage("appletprint.fail"));
    }
  }

  public int print(Graphics paramGraphics, PageFormat paramPageFormat, int paramInt)
  {
    if (paramInt > 0)
      return 1;
    Graphics2D localGraphics2D = (Graphics2D)paramGraphics;
    localGraphics2D.translate(paramPageFormat.getImageableX(), paramPageFormat.getImageableY());
    this.panel.applet.printAll(paramGraphics);
    return 0;
  }

  public static synchronized void networkProperties()
  {
    if (props == null)
      props = new AppletProps();
    props.addNotify();
    props.setVisible(true);
  }

  void appletStart()
  {
    this.panel.sendEvent(3);
  }

  void appletStop()
  {
    this.panel.sendEvent(4);
  }

  private void appletShutdown(AppletPanel paramAppletPanel)
  {
    paramAppletPanel.sendEvent(4);
    paramAppletPanel.sendEvent(5);
    paramAppletPanel.sendEvent(0);
    paramAppletPanel.sendEvent(6);
  }

  void appletClose()
  {
    AppletViewerPanel localAppletViewerPanel = this.panel;
    new Thread(new Runnable(this, localAppletViewerPanel)
    {
      public void run()
      {
        AppletViewer.access$300(this.this$0, this.val$p);
        AppletViewer.appletPanels.removeElement(this.val$p);
        this.this$0.dispose();
        if (AppletViewer.countApplets() == 0)
          AppletViewer.access$400(this.this$0);
      }
    }).start();
  }

  private void appletSystemExit()
  {
    if (this.factory.isStandalone())
      java.lang.System.exit(0);
  }

  protected void appletQuit()
  {
    new Thread(new Runnable(this)
    {
      public void run()
      {
        Enumeration localEnumeration = AppletViewer.appletPanels.elements();
        while (localEnumeration.hasMoreElements())
        {
          AppletPanel localAppletPanel = (AppletPanel)localEnumeration.nextElement();
          AppletViewer.access$300(this.this$0, localAppletPanel);
        }
        AppletViewer.access$400(this.this$0);
      }
    }).start();
  }

  public void processUserAction(ActionEvent paramActionEvent)
  {
    String str = ((MenuItem)paramActionEvent.getSource()).getLabel();
    if (amh.getMessage("menuitem.restart").equals(str))
    {
      appletRestart();
      return;
    }
    if (amh.getMessage("menuitem.reload").equals(str))
    {
      appletReload();
      return;
    }
    if (amh.getMessage("menuitem.clone").equals(str))
    {
      appletClone();
      return;
    }
    if (amh.getMessage("menuitem.stop").equals(str))
    {
      appletStop();
      return;
    }
    if (amh.getMessage("menuitem.save").equals(str))
    {
      appletSave();
      return;
    }
    if (amh.getMessage("menuitem.start").equals(str))
    {
      appletStart();
      return;
    }
    if (amh.getMessage("menuitem.tag").equals(str))
    {
      appletTag();
      return;
    }
    if (amh.getMessage("menuitem.info").equals(str))
    {
      appletInfo();
      return;
    }
    if (amh.getMessage("menuitem.encoding").equals(str))
    {
      appletCharacterEncoding();
      return;
    }
    if (amh.getMessage("menuitem.edit").equals(str))
    {
      appletEdit();
      return;
    }
    if (amh.getMessage("menuitem.print").equals(str))
    {
      appletPrint();
      return;
    }
    if (amh.getMessage("menuitem.props").equals(str))
    {
      networkProperties();
      return;
    }
    if (amh.getMessage("menuitem.close").equals(str))
    {
      appletClose();
      return;
    }
    if ((this.factory.isStandalone()) && (amh.getMessage("menuitem.quit").equals(str)))
    {
      appletQuit();
      return;
    }
  }

  public static int countApplets()
  {
    return appletPanels.size();
  }

  public static void skipSpace(Reader paramReader)
    throws IOException
  {
    while ((c >= 0) && (((c == 32) || (c == 9) || (c == 10) || (c == 13))))
      c = paramReader.read();
  }

  public static String scanIdentifier(Reader paramReader)
    throws IOException
  {
    StringBuffer localStringBuffer = new StringBuffer();
    while (((c >= 97) && (c <= 122)) || ((c >= 65) && (c <= 90)) || ((c >= 48) && (c <= 57)) || (c == 95))
    {
      localStringBuffer.append((char)c);
      c = paramReader.read();
    }
    return localStringBuffer.toString();
  }

  public static Hashtable scanTag(Reader paramReader)
    throws IOException
  {
    Hashtable localHashtable = new Hashtable();
    skipSpace(paramReader);
    while ((c >= 0) && (c != 62))
    {
      String str1 = scanIdentifier(paramReader);
      String str2 = "";
      skipSpace(paramReader);
      if (c == 61)
      {
        int i = -1;
        c = paramReader.read();
        skipSpace(paramReader);
        if ((c == 39) || (c == 34))
        {
          i = c;
          c = paramReader.read();
        }
        StringBuffer localStringBuffer = new StringBuffer();
        while ((c > 0) && ((((i < 0) && (c != 32) && (c != 9) && (c != 10) && (c != 13) && (c != 62)) || ((i >= 0) && (c != i)))))
        {
          localStringBuffer.append((char)c);
          c = paramReader.read();
        }
        if (c == i)
          c = paramReader.read();
        skipSpace(paramReader);
        str2 = localStringBuffer.toString();
      }
      if (!(str2.equals("")))
        localHashtable.put(str1.toLowerCase(Locale.ENGLISH), str2);
      while ((c != 62) && (c >= 0) && (((c < 97) || (c > 122))) && (((c < 65) || (c > 90))) && (((c < 48) || (c > 57))))
      {
        if (c == 95)
          break;
        c = paramReader.read();
      }
    }
    return localHashtable;
  }

  private static Reader makeReader(InputStream paramInputStream)
  {
    InputStreamReader localInputStreamReader;
    if (encoding != null);
    try
    {
      return new BufferedReader(new InputStreamReader(paramInputStream, encoding));
    }
    catch (IOException localInputStreamReader)
    {
      localInputStreamReader = new InputStreamReader(paramInputStream);
      encoding = localInputStreamReader.getEncoding();
    }
    return new BufferedReader(localInputStreamReader);
  }

  public static void parse(URL paramURL, String paramString)
    throws IOException
  {
    encoding = paramString;
    parse(paramURL, java.lang.System.out, new StdAppletViewerFactory());
  }

  public static void parse(URL paramURL)
    throws IOException
  {
    parse(paramURL, java.lang.System.out, new StdAppletViewerFactory());
  }

  public static void parse(URL paramURL, PrintStream paramPrintStream, AppletViewerFactory paramAppletViewerFactory)
    throws IOException
  {
    int i = 0;
    int j = 0;
    int k = 0;
    String str1 = amh.getMessage("parse.warning.requiresname");
    String str2 = amh.getMessage("parse.warning.paramoutside");
    String str3 = amh.getMessage("parse.warning.applet.requirescode");
    String str4 = amh.getMessage("parse.warning.applet.requiresheight");
    String str5 = amh.getMessage("parse.warning.applet.requireswidth");
    String str6 = amh.getMessage("parse.warning.object.requirescode");
    String str7 = amh.getMessage("parse.warning.object.requiresheight");
    String str8 = amh.getMessage("parse.warning.object.requireswidth");
    String str9 = amh.getMessage("parse.warning.embed.requirescode");
    String str10 = amh.getMessage("parse.warning.embed.requiresheight");
    String str11 = amh.getMessage("parse.warning.embed.requireswidth");
    String str12 = amh.getMessage("parse.warning.appnotLongersupported");
    URLConnection localURLConnection = paramURL.openConnection();
    Reader localReader = makeReader(localURLConnection.getInputStream());
    paramURL = localURLConnection.getURL();
    int l = 1;
    Hashtable localHashtable = null;
    while (true)
    {
      Object localObject;
      while (true)
      {
        do
        {
          c = localReader.read();
          if (c == -1)
            break label957:
        }
        while (c != 60);
        c = localReader.read();
        if (c != 47)
          break;
        c = localReader.read();
        str13 = scanIdentifier(localReader);
        if ((str13.equalsIgnoreCase("applet")) || (str13.equalsIgnoreCase("object")) || (str13.equalsIgnoreCase("embed")))
        {
          if ((j != 0) && (localHashtable.get("code") == null) && (localHashtable.get("object") == null))
          {
            paramPrintStream.println(str6);
            localHashtable = null;
          }
          if (localHashtable != null)
          {
            paramAppletViewerFactory.createAppletViewer(x, y, paramURL, localHashtable);
            x += 30;
            y += 30;
            localObject = Toolkit.getDefaultToolkit().getScreenSize();
            if ((x > ((Dimension)localObject).width - 300) || (y > ((Dimension)localObject).height - 300))
            {
              x = 0;
              y = 2 * l * 30;
              ++l;
            }
          }
          localHashtable = null;
          i = 0;
          j = 0;
          k = 0;
        }
      }
      String str13 = scanIdentifier(localReader);
      if (str13.equalsIgnoreCase("param"))
      {
        localObject = scanTag(localReader);
        String str14 = (String)((Hashtable)localObject).get("name");
        if (str14 == null)
        {
          paramPrintStream.println(str1);
        }
        else
        {
          String str15 = (String)((Hashtable)localObject).get("value");
          if (str15 == null)
            paramPrintStream.println(str1);
          else if (localHashtable != null)
            localHashtable.put(str14.toLowerCase(), str15);
          else
            paramPrintStream.println(str2);
        }
      }
      else if (str13.equalsIgnoreCase("applet"))
      {
        i = 1;
        localHashtable = scanTag(localReader);
        if ((localHashtable.get("code") == null) && (localHashtable.get("object") == null))
        {
          paramPrintStream.println(str3);
          localHashtable = null;
        }
        else if (localHashtable.get("width") == null)
        {
          paramPrintStream.println(str5);
          localHashtable = null;
        }
        else if (localHashtable.get("height") == null)
        {
          paramPrintStream.println(str4);
          localHashtable = null;
        }
      }
      else if (str13.equalsIgnoreCase("object"))
      {
        j = 1;
        localHashtable = scanTag(localReader);
        if (localHashtable.get("codebase") != null)
          localHashtable.remove("codebase");
        if (localHashtable.get("width") == null)
        {
          paramPrintStream.println(str8);
          localHashtable = null;
        }
        else if (localHashtable.get("height") == null)
        {
          paramPrintStream.println(str7);
          localHashtable = null;
        }
      }
      else if (str13.equalsIgnoreCase("embed"))
      {
        k = 1;
        localHashtable = scanTag(localReader);
        if ((localHashtable.get("code") == null) && (localHashtable.get("object") == null))
        {
          paramPrintStream.println(str9);
          localHashtable = null;
        }
        else if (localHashtable.get("width") == null)
        {
          paramPrintStream.println(str11);
          localHashtable = null;
        }
        else if (localHashtable.get("height") == null)
        {
          paramPrintStream.println(str10);
          localHashtable = null;
        }
      }
      else if (str13.equalsIgnoreCase("app"))
      {
        paramPrintStream.println(str12);
        localObject = scanTag(localReader);
        str13 = (String)((Hashtable)localObject).get("class");
        if (str13 != null)
        {
          ((Hashtable)localObject).remove("class");
          ((Hashtable)localObject).put("code", str13 + ".class");
        }
        str13 = (String)((Hashtable)localObject).get("src");
        if (str13 != null)
        {
          ((Hashtable)localObject).remove("src");
          ((Hashtable)localObject).put("codebase", str13);
        }
        if (((Hashtable)localObject).get("width") == null)
          ((Hashtable)localObject).put("width", "100");
        if (((Hashtable)localObject).get("height") == null)
          ((Hashtable)localObject).put("height", "100");
        printTag(paramPrintStream, (Hashtable)localObject);
        paramPrintStream.println();
      }
    }
    label957: localReader.close();
  }

  @Deprecated
  public static void main(String[] paramArrayOfString)
  {
    Main.main(paramArrayOfString);
  }

  private static void checkConnect(URL paramURL)
  {
    SecurityManager localSecurityManager = java.lang.System.getSecurityManager();
    if (localSecurityManager != null)
      try
      {
        Permission localPermission = paramURL.openConnection().getPermission();
        if (localPermission != null)
          localSecurityManager.checkPermission(localPermission);
        else
          localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
      }
      catch (IOException localIOException)
      {
        localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
      }
  }

  static
  {
    systemParam.put("codebase", "codebase");
    systemParam.put("code", "code");
    systemParam.put("alt", "alt");
    systemParam.put("width", "width");
    systemParam.put("height", "height");
    systemParam.put("align", "align");
    systemParam.put("vspace", "vspace");
    systemParam.put("hspace", "hspace");
    x = 0;
    y = 0;
    encoding = null;
    amh = new AppletMessageHandler("appletviewer");
  }

  private final class UserActionListener
  implements ActionListener
  {
    public void actionPerformed()
    {
      this.this$0.processUserAction(paramActionEvent);
    }
  }
}
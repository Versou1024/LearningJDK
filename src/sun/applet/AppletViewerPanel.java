package sun.applet;

import java.applet.AppletContext;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

class AppletViewerPanel extends AppletPanel
{
  static boolean debug = false;
  URL documentURL;
  URL baseURL;
  Hashtable atts;
  private static final long serialVersionUID = 8890989370785545619L;

  AppletViewerPanel(URL paramURL, Hashtable paramHashtable)
  {
    this.documentURL = paramURL;
    this.atts = paramHashtable;
    String str1 = getParameter("codebase");
    if (str1 != null)
    {
      if (!(str1.endsWith("/")))
        str1 = str1 + "/";
      try
      {
        this.baseURL = new URL(paramURL, str1);
      }
      catch (MalformedURLException localMalformedURLException1)
      {
      }
    }
    if (this.baseURL == null)
    {
      String str2 = paramURL.getFile();
      int i = str2.lastIndexOf(47);
      if ((i >= 0) && (i < str2.length() - 1))
        try
        {
          this.baseURL = new URL(paramURL, str2.substring(0, i + 1));
        }
        catch (MalformedURLException localMalformedURLException2)
        {
        }
    }
    if (this.baseURL == null)
      this.baseURL = paramURL;
  }

  public String getParameter(String paramString)
  {
    return ((String)this.atts.get(paramString.toLowerCase()));
  }

  public URL getDocumentBase()
  {
    return this.documentURL;
  }

  public URL getCodeBase()
  {
    return this.baseURL;
  }

  public int getWidth()
  {
    String str = getParameter("width");
    if (str != null)
      return Integer.valueOf(str).intValue();
    return 0;
  }

  public int getHeight()
  {
    String str = getParameter("height");
    if (str != null)
      return Integer.valueOf(str).intValue();
    return 0;
  }

  public boolean hasInitialFocus()
  {
    if ((isJDK11Applet()) || (isJDK12Applet()))
      return false;
    String str = getParameter("initial_focus");
    return ((str == null) || (!(str.toLowerCase().equals("false"))));
  }

  public String getCode()
  {
    return getParameter("code");
  }

  public void updateHostIPFile(String paramString)
  {
  }

  public String getJarFiles()
  {
    return getParameter("archive");
  }

  public String getSerializedObject()
  {
    return getParameter("object");
  }

  public AppletContext getAppletContext()
  {
    return ((AppletContext)getParent());
  }

  static void debug(String paramString)
  {
    if (debug)
      System.err.println("AppletViewerPanel:::" + paramString);
  }

  static void debug(String paramString, Throwable paramThrowable)
  {
    if (debug)
    {
      paramThrowable.printStackTrace();
      debug(paramString);
    }
  }
}
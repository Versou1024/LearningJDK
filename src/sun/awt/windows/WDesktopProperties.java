package sun.awt.windows;

import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.awt.SunToolkit;

class WDesktopProperties
{
  private static final Logger log;
  private static final String PREFIX = "win.";
  private static final String FILE_PREFIX = "awt.file.";
  private static final String PROP_NAMES = "win.propNames";
  private long pData;
  private WToolkit wToolkit;
  private HashMap<String, Object> map = new HashMap();
  static HashMap<String, String> fontNameMap;

  private static native void initIDs();

  static boolean isWindowsProperty(String paramString)
  {
    return ((paramString.startsWith("win.")) || (paramString.startsWith("awt.file.")) || (paramString.equals("awt.font.desktophints")));
  }

  WDesktopProperties(WToolkit paramWToolkit)
  {
    this.wToolkit = paramWToolkit;
    init();
  }

  private native void init();

  private String[] getKeyNames()
  {
    Object[] arrayOfObject = this.map.keySet().toArray();
    String[] arrayOfString = new String[arrayOfObject.length];
    for (int i = 0; i < arrayOfObject.length; ++i)
      arrayOfString[i] = arrayOfObject[i].toString();
    Arrays.sort(arrayOfString);
    return arrayOfString;
  }

  private native void getWindowsParameters();

  private synchronized void setBooleanProperty(String paramString, boolean paramBoolean)
  {
    if ((!($assertionsDisabled)) && (paramString == null))
      throw new AssertionError();
    if (log.isLoggable(Level.FINE))
      log.fine(paramString + "=" + String.valueOf(paramBoolean));
    this.map.put(paramString, Boolean.valueOf(paramBoolean));
  }

  private synchronized void setIntegerProperty(String paramString, int paramInt)
  {
    if ((!($assertionsDisabled)) && (paramString == null))
      throw new AssertionError();
    if (log.isLoggable(Level.FINE))
      log.fine(paramString + "=" + String.valueOf(paramInt));
    this.map.put(paramString, Integer.valueOf(paramInt));
  }

  private synchronized void setStringProperty(String paramString1, String paramString2)
  {
    if ((!($assertionsDisabled)) && (paramString1 == null))
      throw new AssertionError();
    if (log.isLoggable(Level.FINE))
      log.fine(paramString1 + "=" + paramString2);
    this.map.put(paramString1, paramString2);
  }

  private synchronized void setColorProperty(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    if ((!($assertionsDisabled)) && (((paramString == null) || (paramInt1 > 255) || (paramInt2 > 255) || (paramInt3 > 255))))
      throw new AssertionError();
    Color localColor = new Color(paramInt1, paramInt2, paramInt3);
    if (log.isLoggable(Level.FINE))
      log.fine(paramString + "=" + localColor);
    this.map.put(paramString, localColor);
  }

  private synchronized void setFontProperty(String paramString1, String paramString2, int paramInt1, int paramInt2)
  {
    if ((!($assertionsDisabled)) && (((paramString1 == null) || (paramInt1 > 3) || (paramInt2 < 0))))
      throw new AssertionError();
    String str1 = (String)fontNameMap.get(paramString2);
    if (str1 != null)
      paramString2 = str1;
    Font localFont = new Font(paramString2, paramInt1, paramInt2);
    if (log.isLoggable(Level.FINE))
      log.fine(paramString1 + "=" + localFont);
    this.map.put(paramString1, localFont);
    String str2 = paramString1 + ".height";
    Integer localInteger = Integer.valueOf(paramInt2);
    if (log.isLoggable(Level.FINE))
      log.fine(str2 + "=" + localInteger);
    this.map.put(str2, localInteger);
  }

  private synchronized void setSoundProperty(String paramString1, String paramString2)
  {
    if ((!($assertionsDisabled)) && (((paramString1 == null) || (paramString2 == null))))
      throw new AssertionError();
    WinPlaySound localWinPlaySound = new WinPlaySound(this, paramString2);
    if (log.isLoggable(Level.FINE))
      log.fine(paramString1 + "=" + localWinPlaySound);
    this.map.put(paramString1, localWinPlaySound);
  }

  private native void playWindowsSound(String paramString);

  synchronized Map<String, Object> getProperties()
  {
    ThemeReader.flush();
    this.map = new HashMap();
    getWindowsParameters();
    this.map.put("awt.font.desktophints", SunToolkit.getDesktopFontHints());
    this.map.put("win.propNames", getKeyNames());
    this.map.put("DnD.Autoscroll.cursorHysteresis", this.map.get("win.drag.x"));
    return ((Map)this.map.clone());
  }

  synchronized RenderingHints getDesktopAAHints()
  {
    Object localObject1 = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
    Integer localInteger1 = null;
    Boolean localBoolean = (Boolean)this.map.get("win.text.fontSmoothingOn");
    if ((localBoolean != null) && (localBoolean.equals(Boolean.TRUE)))
    {
      localObject2 = (Integer)this.map.get("win.text.fontSmoothingType");
      if ((localObject2 == null) || (((Integer)localObject2).intValue() <= 1) || (((Integer)localObject2).intValue() > 2))
      {
        localObject1 = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
      }
      else
      {
        Integer localInteger2 = (Integer)this.map.get("win.text.fontSmoothingOrientation");
        if ((localInteger2 == null) || (localInteger2.intValue() != 0))
          localObject1 = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
        else
          localObject1 = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
        localInteger1 = (Integer)this.map.get("win.text.fontSmoothingContrast");
        if (localInteger1 == null)
          localInteger1 = new Integer(140);
        else
          localInteger1 = new Integer(localInteger1.intValue() / 10);
      }
    }
    Object localObject2 = new RenderingHints(null);
    ((RenderingHints)localObject2).put(RenderingHints.KEY_TEXT_ANTIALIASING, localObject1);
    if (localInteger1 != null)
      ((RenderingHints)localObject2).put(RenderingHints.KEY_TEXT_LCD_CONTRAST, localInteger1);
    return ((RenderingHints)localObject2);
  }

  static
  {
    log = Logger.getLogger("sun.awt.windows.WDesktopProperties");
    initIDs();
    fontNameMap = new HashMap();
    fontNameMap.put("Courier", "Monospaced");
    fontNameMap.put("MS Serif", "Microsoft Serif");
    fontNameMap.put("MS Sans Serif", "Microsoft Sans Serif");
    fontNameMap.put("Terminal", "Dialog");
    fontNameMap.put("FixedSys", "Monospaced");
    fontNameMap.put("System", "Dialog");
  }

  class WinPlaySound
  implements Runnable
  {
    String winEventName;

    WinPlaySound(, String paramString)
    {
      this.winEventName = paramString;
    }

    public void run()
    {
      WDesktopProperties.access$000(this.this$0, this.winEventName);
    }

    public String toString()
    {
      return "WinPlaySound(" + this.winEventName + ")";
    }

    public boolean equals()
    {
      if (paramObject == this)
        return true;
      try
      {
        return this.winEventName.equals(((WinPlaySound)paramObject).winEventName);
      }
      catch (Exception localException)
      {
      }
      return false;
    }

    public int hashCode()
    {
      return this.winEventName.hashCode();
    }
  }
}
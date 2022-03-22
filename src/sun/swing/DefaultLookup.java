package sun.swing;

import java.awt.Color;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import sun.awt.AppContext;

public class DefaultLookup
{
  private static final Object DEFAULT_LOOKUP_KEY = new StringBuffer("DefaultLookup");
  private static Thread currentDefaultThread;
  private static DefaultLookup currentDefaultLookup;
  private static boolean isLookupSet;

  public static void setDefaultLookup(DefaultLookup paramDefaultLookup)
  {
    synchronized (DefaultLookup.class)
    {
      if ((isLookupSet) || (paramDefaultLookup != null))
        break label19;
      return;
      label19: if (paramDefaultLookup != null)
        break label31;
      paramDefaultLookup = new DefaultLookup();
      label31: isLookupSet = true;
      AppContext.getAppContext().put(DEFAULT_LOOKUP_KEY, paramDefaultLookup);
      currentDefaultThread = Thread.currentThread();
      currentDefaultLookup = paramDefaultLookup;
    }
  }

  public static Object get(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString)
  {
    boolean bool;
    DefaultLookup localDefaultLookup1;
    synchronized (DefaultLookup.class)
    {
      bool = isLookupSet;
    }
    if (!(bool))
      return UIManager.get(paramString, paramJComponent.getLocale());
    ??? = Thread.currentThread();
    synchronized (DefaultLookup.class)
    {
      if (??? == currentDefaultThread)
      {
        localDefaultLookup1 = currentDefaultLookup;
      }
      else
      {
        localDefaultLookup1 = (DefaultLookup)AppContext.getAppContext().get(DEFAULT_LOOKUP_KEY);
        if (localDefaultLookup1 == null)
        {
          localDefaultLookup1 = new DefaultLookup();
          AppContext.getAppContext().put(DEFAULT_LOOKUP_KEY, localDefaultLookup1);
        }
        currentDefaultThread = (Thread)???;
        currentDefaultLookup = localDefaultLookup1;
      }
    }
    return localDefaultLookup1.getDefault(paramJComponent, paramComponentUI, paramString);
  }

  public static int getInt(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString, int paramInt)
  {
    Object localObject = get(paramJComponent, paramComponentUI, paramString);
    if ((localObject == null) || (!(localObject instanceof Number)))
      return paramInt;
    return ((Number)localObject).intValue();
  }

  public static int getInt(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString)
  {
    return getInt(paramJComponent, paramComponentUI, paramString, -1);
  }

  public static Insets getInsets(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString, Insets paramInsets)
  {
    Object localObject = get(paramJComponent, paramComponentUI, paramString);
    if ((localObject == null) || (!(localObject instanceof Insets)))
      return paramInsets;
    return ((Insets)localObject);
  }

  public static Insets getInsets(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString)
  {
    return getInsets(paramJComponent, paramComponentUI, paramString, null);
  }

  public static boolean getBoolean(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString, boolean paramBoolean)
  {
    Object localObject = get(paramJComponent, paramComponentUI, paramString);
    if ((localObject == null) || (!(localObject instanceof Boolean)))
      return paramBoolean;
    return ((Boolean)localObject).booleanValue();
  }

  public static boolean getBoolean(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString)
  {
    return getBoolean(paramJComponent, paramComponentUI, paramString, false);
  }

  public static Color getColor(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString, Color paramColor)
  {
    Object localObject = get(paramJComponent, paramComponentUI, paramString);
    if ((localObject == null) || (!(localObject instanceof Color)))
      return paramColor;
    return ((Color)localObject);
  }

  public static Color getColor(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString)
  {
    return getColor(paramJComponent, paramComponentUI, paramString, null);
  }

  public static Icon getIcon(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString, Icon paramIcon)
  {
    Object localObject = get(paramJComponent, paramComponentUI, paramString);
    if ((localObject == null) || (!(localObject instanceof Icon)))
      return paramIcon;
    return ((Icon)localObject);
  }

  public static Icon getIcon(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString)
  {
    return getIcon(paramJComponent, paramComponentUI, paramString, null);
  }

  public static Border getBorder(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString, Border paramBorder)
  {
    Object localObject = get(paramJComponent, paramComponentUI, paramString);
    if ((localObject == null) || (!(localObject instanceof Border)))
      return paramBorder;
    return ((Border)localObject);
  }

  public static Border getBorder(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString)
  {
    return getBorder(paramJComponent, paramComponentUI, paramString, null);
  }

  public Object getDefault(JComponent paramJComponent, ComponentUI paramComponentUI, String paramString)
  {
    return UIManager.get(paramString, paramJComponent.getLocale());
  }
}
package sun.swing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.PrintGraphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterGraphics;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.Bidi;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIDefaults.LazyValue;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.font.FontDesignMetrics;
import sun.print.ProxyPrintGraphics;

public class SwingUtilities2
{
  public static final Object LAF_STATE_KEY;
  private static LSBCacheEntry[] fontCache;
  private static final int CACHE_SIZE = 6;
  private static int nextIndex;
  private static LSBCacheEntry searchKey;
  private static final int MIN_CHAR_INDEX = 87;
  private static final int MAX_CHAR_INDEX = 88;
  public static final FontRenderContext DEFAULT_FRC;
  public static final Object AA_TEXT_PROPERTY_KEY;
  public static final String IMPLIED_CR = "CR";
  private static final StringBuilder SKIP_CLICK_COUNT;
  public static final Object COMPONENT_UI_PROPERTY_KEY;
  public static final Object BASICMENUITEMUI_MAX_TEXT_OFFSET;
  private static Field inputEvent_CanAccessSystemClipboard_Field;
  private static final String UntrustedClipboardAccess = "UNTRUSTED_CLIPBOARD_ACCESS_KEY";
  private static final int CHAR_BUFFER_SIZE = 100;
  private static final Object charsBufferLock;
  private static char[] charsBuffer;

  private static final boolean isComplexLayout(char paramChar)
  {
    return (((paramChar >= 2304) && (paramChar <= 3455)) || ((paramChar >= 3584) && (paramChar <= 3711)) || ((paramChar >= 6016) && (paramChar <= 6143)) || ((paramChar >= 55296) && (paramChar <= 57343)));
  }

  private static final boolean isSimpleLayout(char paramChar)
  {
    return ((paramChar < 1424) || ((11776 <= paramChar) && (paramChar < 55296)));
  }

  public static final boolean isComplexLayout(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    boolean bool = true;
    for (int i = paramInt1; i < paramInt2; ++i)
    {
      char c = paramArrayOfChar[i];
      if (isComplexLayout(c))
        return true;
      if (bool)
        bool = isSimpleLayout(c);
    }
    if (bool)
      return false;
    return Bidi.requiresBidi(paramArrayOfChar, paramInt1, paramInt2);
  }

  public static AATextInfo drawTextAntialiased(JComponent paramJComponent)
  {
    if (paramJComponent != null)
      return ((AATextInfo)paramJComponent.getClientProperty(AA_TEXT_PROPERTY_KEY));
    return null;
  }

  public static int getLeftSideBearing(JComponent paramJComponent, FontMetrics paramFontMetrics, String paramString)
  {
    return getLeftSideBearing(paramJComponent, paramFontMetrics, paramString.charAt(0));
  }

  public static int getLeftSideBearing(JComponent paramJComponent, FontMetrics paramFontMetrics, char paramChar)
  {
    int i = paramChar;
    if ((i < 88) && (i >= 87))
    {
      Object localObject1 = null;
      FontRenderContext localFontRenderContext = getFontRenderContext(paramJComponent, paramFontMetrics);
      Font localFont = paramFontMetrics.getFont();
      synchronized (SwingUtilities2.class)
      {
        Object localObject2 = null;
        if (searchKey == null)
          searchKey = new LSBCacheEntry(localFontRenderContext, localFont);
        else
          searchKey.reset(localFontRenderContext, localFont);
        LSBCacheEntry[] arrayOfLSBCacheEntry = fontCache;
        int j = arrayOfLSBCacheEntry.length;
        for (int k = 0; k < j; ++k)
        {
          LSBCacheEntry localLSBCacheEntry = arrayOfLSBCacheEntry[k];
          if (searchKey.equals(localLSBCacheEntry))
          {
            localObject2 = localLSBCacheEntry;
            break;
          }
        }
        if (localObject2 == null)
        {
          localObject2 = searchKey;
          fontCache[nextIndex] = searchKey;
          searchKey = null;
          nextIndex = (nextIndex + 1) % 6;
        }
        return ((LSBCacheEntry)localObject2).getLeftSideBearing(paramChar);
      }
    }
    return 0;
  }

  public static FontMetrics getFontMetrics(JComponent paramJComponent, Graphics paramGraphics)
  {
    return getFontMetrics(paramJComponent, paramGraphics, paramGraphics.getFont());
  }

  public static FontMetrics getFontMetrics(JComponent paramJComponent, Graphics paramGraphics, Font paramFont)
  {
    if (paramJComponent != null)
      return paramJComponent.getFontMetrics(paramFont);
    return Toolkit.getDefaultToolkit().getFontMetrics(paramFont);
  }

  public static int stringWidth(JComponent paramJComponent, FontMetrics paramFontMetrics, String paramString)
  {
    if ((paramString == null) || (paramString.equals("")))
      return 0;
    return paramFontMetrics.stringWidth(paramString);
  }

  public static String clipStringIfNecessary(JComponent paramJComponent, FontMetrics paramFontMetrics, String paramString, int paramInt)
  {
    if ((paramString == null) || (paramString.equals("")))
      return "";
    int i = stringWidth(paramJComponent, paramFontMetrics, paramString);
    if (i > paramInt)
      return clipString(paramJComponent, paramFontMetrics, paramString, paramInt);
    return paramString;
  }

  public static String clipString(JComponent paramJComponent, FontMetrics paramFontMetrics, String paramString, int paramInt)
  {
    String str = "...";
    int i = paramString.length();
    paramInt -= stringWidth(paramJComponent, paramFontMetrics, str);
    if (paramInt <= 0)
      return str;
    boolean bool = false;
    synchronized (charsBufferLock)
    {
      if ((charsBuffer == null) || (charsBuffer.length < i))
        charsBuffer = paramString.toCharArray();
      else
        paramString.getChars(0, i, charsBuffer, 0);
      bool = isComplexLayout(charsBuffer, 0, i);
      if (!(bool))
      {
        int j = 0;
        for (int k = 0; k < i; ++k)
        {
          j += paramFontMetrics.charWidth(charsBuffer[k]);
          if (j > paramInt)
          {
            paramString = paramString.substring(0, k);
            break;
          }
        }
      }
    }
    if (bool)
    {
      ??? = getFontRenderContext(paramJComponent, paramFontMetrics);
      AttributedString localAttributedString = new AttributedString(paramString);
      LineBreakMeasurer localLineBreakMeasurer = new LineBreakMeasurer(localAttributedString.getIterator(), (FontRenderContext)???);
      int l = localLineBreakMeasurer.nextOffset(paramInt);
      paramString = paramString.substring(0, l);
    }
    return ((String)paramString + str);
  }

  public static void drawString(JComponent paramJComponent, Graphics paramGraphics, String paramString, int paramInt1, int paramInt2)
  {
    Object localObject2;
    Object localObject3;
    if ((paramString == null) || (paramString.length() <= 0))
      return;
    if (isPrinting(paramGraphics))
    {
      localObject1 = getGraphics2D(paramGraphics);
      if (localObject1 != null)
      {
        float f = (float)((Graphics2D)localObject1).getFont().getStringBounds(paramString, DEFAULT_FRC).getWidth();
        localObject2 = new TextLayout(paramString, ((Graphics2D)localObject1).getFont(), ((Graphics2D)localObject1).getFontRenderContext());
        localObject2 = ((TextLayout)localObject2).getJustifiedLayout(f);
        localObject3 = ((Graphics2D)localObject1).getColor();
        if (localObject3 instanceof PrintColorUIResource)
          ((Graphics2D)localObject1).setColor(((PrintColorUIResource)localObject3).getPrintColor());
        ((TextLayout)localObject2).draw((Graphics2D)localObject1, paramInt1, paramInt2);
        ((Graphics2D)localObject1).setColor((Color)localObject3);
        return;
      }
    }
    Object localObject1 = drawTextAntialiased(paramJComponent);
    if ((localObject1 != null) && (paramGraphics instanceof Graphics2D))
    {
      Graphics2D localGraphics2D = (Graphics2D)paramGraphics;
      localObject2 = null;
      localObject3 = localGraphics2D.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
      if (((AATextInfo)localObject1).aaHint != localObject3)
        localGraphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, ((AATextInfo)localObject1).aaHint);
      else
        localObject3 = null;
      if (((AATextInfo)localObject1).lcdContrastHint != null)
      {
        localObject2 = localGraphics2D.getRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST);
        if (((AATextInfo)localObject1).lcdContrastHint.equals(localObject2))
          localObject2 = null;
        else
          localGraphics2D.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, ((AATextInfo)localObject1).lcdContrastHint);
      }
      paramGraphics.drawString(paramString, paramInt1, paramInt2);
      if (localObject3 != null)
        localGraphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, localObject3);
      if (localObject2 != null)
        localGraphics2D.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, localObject2);
    }
    else
    {
      paramGraphics.drawString(paramString, paramInt1, paramInt2);
    }
  }

  public static void drawStringUnderlineCharAt(JComponent paramJComponent, Graphics paramGraphics, String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    if ((paramString == null) || (paramString.length() <= 0))
      return;
    drawString(paramJComponent, paramGraphics, paramString, paramInt2, paramInt3);
    int i = paramString.length();
    if ((paramInt1 >= 0) && (paramInt1 < i))
    {
      int j = paramInt3;
      int k = 1;
      int l = 0;
      int i1 = 0;
      boolean bool1 = isPrinting(paramGraphics);
      boolean bool2 = bool1;
      if (!(bool2))
        synchronized (charsBufferLock)
        {
          if ((charsBuffer == null) || (charsBuffer.length < i))
            charsBuffer = paramString.toCharArray();
          else
            paramString.getChars(0, i, charsBuffer, 0);
          bool2 = isComplexLayout(charsBuffer, 0, i);
        }
      if (!(bool2))
      {
        ??? = paramGraphics.getFontMetrics();
        l = paramInt2 + stringWidth(paramJComponent, (FontMetrics)???, paramString.substring(0, paramInt1));
        i1 = ((FontMetrics)???).charWidth(paramString.charAt(paramInt1));
      }
      else
      {
        ??? = getGraphics2D(paramGraphics);
        if (??? != null)
        {
          TextLayout localTextLayout = new TextLayout(paramString, ((Graphics2D)???).getFont(), ((Graphics2D)???).getFontRenderContext());
          if (bool1)
          {
            float f = (float)((Graphics2D)???).getFont().getStringBounds(paramString, DEFAULT_FRC).getWidth();
            localTextLayout = localTextLayout.getJustifiedLayout(f);
          }
          TextHitInfo localTextHitInfo1 = TextHitInfo.leading(paramInt1);
          TextHitInfo localTextHitInfo2 = TextHitInfo.trailing(paramInt1);
          Shape localShape = localTextLayout.getVisualHighlightShape(localTextHitInfo1, localTextHitInfo2);
          Rectangle localRectangle = localShape.getBounds();
          l = paramInt2 + localRectangle.x;
          i1 = localRectangle.width;
        }
      }
      paramGraphics.fillRect(l, j + 1, i1, k);
    }
  }

  public static int loc2IndexFileList(JList paramJList, Point paramPoint)
  {
    int i = paramJList.locationToIndex(paramPoint);
    if (i != -1)
    {
      Object localObject = paramJList.getClientProperty("List.isFileList");
      if ((localObject instanceof Boolean) && (((Boolean)localObject).booleanValue()) && (!(pointIsInActualBounds(paramJList, i, paramPoint))))
        i = -1;
    }
    return i;
  }

  private static boolean pointIsInActualBounds(JList paramJList, int paramInt, Point paramPoint)
  {
    ListCellRenderer localListCellRenderer = paramJList.getCellRenderer();
    ListModel localListModel = paramJList.getModel();
    Object localObject = localListModel.getElementAt(paramInt);
    Component localComponent = localListCellRenderer.getListCellRendererComponent(paramJList, localObject, paramInt, false, false);
    Dimension localDimension = localComponent.getPreferredSize();
    Rectangle localRectangle = paramJList.getCellBounds(paramInt, paramInt);
    if (!(localComponent.getComponentOrientation().isLeftToRight()))
      localRectangle.x += localRectangle.width - localDimension.width;
    localRectangle.width = localDimension.width;
    return localRectangle.contains(paramPoint);
  }

  public static boolean pointOutsidePrefSize(JTable paramJTable, int paramInt1, int paramInt2, Point paramPoint)
  {
    if ((paramJTable.convertColumnIndexToModel(paramInt2) != 0) || (paramInt1 == -1))
      return true;
    TableCellRenderer localTableCellRenderer = paramJTable.getCellRenderer(paramInt1, paramInt2);
    Object localObject = paramJTable.getValueAt(paramInt1, paramInt2);
    Component localComponent = localTableCellRenderer.getTableCellRendererComponent(paramJTable, localObject, false, false, paramInt1, paramInt2);
    Dimension localDimension = localComponent.getPreferredSize();
    Rectangle localRectangle = paramJTable.getCellRect(paramInt1, paramInt2, false);
    localRectangle.width = localDimension.width;
    localRectangle.height = localDimension.height;
    if ((!($assertionsDisabled)) && (((paramPoint.x < localRectangle.x) || (paramPoint.y < localRectangle.y))))
      throw new AssertionError();
    return ((paramPoint.x > localRectangle.x + localRectangle.width) || (paramPoint.y > localRectangle.y + localRectangle.height));
  }

  public static void setLeadAnchorWithoutSelection(ListSelectionModel paramListSelectionModel, int paramInt1, int paramInt2)
  {
    if (paramInt2 == -1)
      paramInt2 = paramInt1;
    if (paramInt1 == -1)
    {
      paramListSelectionModel.setAnchorSelectionIndex(-1);
      paramListSelectionModel.setLeadSelectionIndex(-1);
    }
    else
    {
      if (paramListSelectionModel.isSelectedIndex(paramInt1))
        paramListSelectionModel.addSelectionInterval(paramInt1, paramInt1);
      else
        paramListSelectionModel.removeSelectionInterval(paramInt1, paramInt1);
      paramListSelectionModel.setAnchorSelectionIndex(paramInt2);
    }
  }

  public static boolean shouldIgnore(MouseEvent paramMouseEvent, JComponent paramJComponent)
  {
    return ((paramJComponent == null) || (!(paramJComponent.isEnabled())) || (!(SwingUtilities.isLeftMouseButton(paramMouseEvent))) || (paramMouseEvent.isConsumed()));
  }

  public static void adjustFocus(JComponent paramJComponent)
  {
    if ((!(paramJComponent.hasFocus())) && (paramJComponent.isRequestFocusEnabled()))
      paramJComponent.requestFocus();
  }

  public static int drawChars(JComponent paramJComponent, Graphics paramGraphics, char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Object localObject2;
    Object localObject3;
    Object localObject4;
    if (paramInt2 <= 0)
      return paramInt3;
    int i = paramInt3 + getFontMetrics(paramJComponent, paramGraphics).charsWidth(paramArrayOfChar, paramInt1, paramInt2);
    if (isPrinting(paramGraphics))
    {
      localObject1 = getGraphics2D(paramGraphics);
      if (localObject1 != null)
      {
        localObject2 = ((Graphics2D)localObject1).getFontRenderContext();
        localObject3 = getFontRenderContext(paramJComponent);
        if ((localObject3 != null) && (!(isFontRenderContextPrintCompatible((FontRenderContext)localObject2, (FontRenderContext)localObject3))))
        {
          localObject4 = new TextLayout(new String(paramArrayOfChar, paramInt1, paramInt2), ((Graphics2D)localObject1).getFont(), (FontRenderContext)localObject2);
          float f = (float)((Graphics2D)localObject1).getFont().getStringBounds(paramArrayOfChar, paramInt1, paramInt1 + paramInt2, (FontRenderContext)localObject3).getWidth();
          localObject4 = ((TextLayout)localObject4).getJustifiedLayout(f);
          Color localColor = ((Graphics2D)localObject1).getColor();
          if (localColor instanceof PrintColorUIResource)
            ((Graphics2D)localObject1).setColor(((PrintColorUIResource)localColor).getPrintColor());
          ((TextLayout)localObject4).draw((Graphics2D)localObject1, paramInt3, paramInt4);
          ((Graphics2D)localObject1).setColor(localColor);
          return i;
        }
      }
    }
    Object localObject1 = drawTextAntialiased(paramJComponent);
    if ((localObject1 != null) && (paramGraphics instanceof Graphics2D))
    {
      localObject2 = (Graphics2D)paramGraphics;
      localObject3 = null;
      localObject4 = ((Graphics2D)localObject2).getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
      if ((((AATextInfo)localObject1).aaHint != null) && (((AATextInfo)localObject1).aaHint != localObject4))
        ((Graphics2D)localObject2).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, ((AATextInfo)localObject1).aaHint);
      else
        localObject4 = null;
      if (((AATextInfo)localObject1).lcdContrastHint != null)
      {
        localObject3 = ((Graphics2D)localObject2).getRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST);
        if (((AATextInfo)localObject1).lcdContrastHint.equals(localObject3))
          localObject3 = null;
        else
          ((Graphics2D)localObject2).setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, ((AATextInfo)localObject1).lcdContrastHint);
      }
      paramGraphics.drawChars(paramArrayOfChar, paramInt1, paramInt2, paramInt3, paramInt4);
      if (localObject4 != null)
        ((Graphics2D)localObject2).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, localObject4);
      if (localObject3 != null)
        ((Graphics2D)localObject2).setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, localObject3);
    }
    else
    {
      paramGraphics.drawChars(paramArrayOfChar, paramInt1, paramInt2, paramInt3, paramInt4);
    }
    return i;
  }

  public static float drawString(JComponent paramJComponent, Graphics paramGraphics, AttributedCharacterIterator paramAttributedCharacterIterator, int paramInt1, int paramInt2)
  {
    float f1;
    boolean bool = isPrinting(paramGraphics);
    Color localColor = paramGraphics.getColor();
    if ((bool) && (localColor instanceof PrintColorUIResource))
      paramGraphics.setColor(((PrintColorUIResource)localColor).getPrintColor());
    Graphics2D localGraphics2D = getGraphics2D(paramGraphics);
    if (localGraphics2D == null)
    {
      paramGraphics.drawString(paramAttributedCharacterIterator, paramInt1, paramInt2);
      label129: f1 = paramInt1;
    }
    else
    {
      FontRenderContext localFontRenderContext1;
      if (bool)
      {
        localFontRenderContext1 = getFontRenderContext(paramJComponent);
        if ((localFontRenderContext1.isAntiAliased()) || (localFontRenderContext1.usesFractionalMetrics()))
          localFontRenderContext1 = new FontRenderContext(localFontRenderContext1.getTransform(), false, false);
      }
      else
      {
        if ((localFontRenderContext1 = getFRCProperty(paramJComponent)) != null)
          break label129:
        localFontRenderContext1 = localGraphics2D.getFontRenderContext();
      }
      TextLayout localTextLayout = new TextLayout(paramAttributedCharacterIterator, localFontRenderContext1);
      if (bool)
      {
        FontRenderContext localFontRenderContext2 = localGraphics2D.getFontRenderContext();
        if (!(isFontRenderContextPrintCompatible(localFontRenderContext1, localFontRenderContext2)))
        {
          float f2 = localTextLayout.getAdvance();
          localTextLayout = new TextLayout(paramAttributedCharacterIterator, localFontRenderContext2);
          localTextLayout = localTextLayout.getJustifiedLayout(f2);
        }
      }
      localTextLayout.draw(localGraphics2D, paramInt1, paramInt2);
      f1 = localTextLayout.getAdvance();
    }
    if (bool)
      paramGraphics.setColor(localColor);
    return f1;
  }

  private static boolean isFontRenderContextPrintCompatible(FontRenderContext paramFontRenderContext1, FontRenderContext paramFontRenderContext2)
  {
    if (paramFontRenderContext1 == paramFontRenderContext2)
      return true;
    if ((paramFontRenderContext1 == null) || (paramFontRenderContext2 == null))
      return false;
    if (paramFontRenderContext1.getFractionalMetricsHint() != paramFontRenderContext2.getFractionalMetricsHint())
      return false;
    if ((!(paramFontRenderContext1.isTransformed())) && (!(paramFontRenderContext2.isTransformed())))
      return true;
    double[] arrayOfDouble1 = new double[4];
    double[] arrayOfDouble2 = new double[4];
    paramFontRenderContext1.getTransform().getMatrix(arrayOfDouble1);
    paramFontRenderContext2.getTransform().getMatrix(arrayOfDouble2);
    return ((arrayOfDouble1[0] == arrayOfDouble2[0]) && (arrayOfDouble1[1] == arrayOfDouble2[1]) && (arrayOfDouble1[2] == arrayOfDouble2[2]) && (arrayOfDouble1[3] == arrayOfDouble2[3]));
  }

  public static Graphics2D getGraphics2D(Graphics paramGraphics)
  {
    if (paramGraphics instanceof Graphics2D)
      return ((Graphics2D)paramGraphics);
    if (paramGraphics instanceof ProxyPrintGraphics)
      return ((Graphics2D)(Graphics2D)((ProxyPrintGraphics)paramGraphics).getGraphics());
    return null;
  }

  public static FontRenderContext getFontRenderContext(Component paramComponent)
  {
    if ((!($assertionsDisabled)) && (paramComponent == null))
      throw new AssertionError();
    if (paramComponent == null)
      return DEFAULT_FRC;
    return paramComponent.getFontMetrics(paramComponent.getFont()).getFontRenderContext();
  }

  private static FontRenderContext getFontRenderContext(Component paramComponent, FontMetrics paramFontMetrics)
  {
    if ((!($assertionsDisabled)) && (paramFontMetrics == null) && (paramComponent == null))
      throw new AssertionError();
    return ((paramFontMetrics != null) ? paramFontMetrics.getFontRenderContext() : getFontRenderContext(paramComponent));
  }

  public static FontMetrics getFontMetrics(JComponent paramJComponent, Font paramFont)
  {
    FontRenderContext localFontRenderContext = getFRCProperty(paramJComponent);
    if (localFontRenderContext == null)
      localFontRenderContext = DEFAULT_FRC;
    return FontDesignMetrics.getMetrics(paramFont, localFontRenderContext);
  }

  private static FontRenderContext getFRCProperty(JComponent paramJComponent)
  {
    if (paramJComponent != null)
    {
      AATextInfo localAATextInfo = (AATextInfo)paramJComponent.getClientProperty(AA_TEXT_PROPERTY_KEY);
      if (localAATextInfo != null)
        return localAATextInfo.frc;
    }
    return null;
  }

  static boolean isPrinting(Graphics paramGraphics)
  {
    return ((paramGraphics instanceof PrinterGraphics) || (paramGraphics instanceof PrintGraphics));
  }

  public static boolean useSelectedTextColor(Highlighter.Highlight paramHighlight, JTextComponent paramJTextComponent)
  {
    Highlighter.HighlightPainter localHighlightPainter = paramHighlight.getPainter();
    String str = localHighlightPainter.getClass().getName();
    if ((str.indexOf("javax.swing.text.DefaultHighlighter") != 0) && (str.indexOf("com.sun.java.swing.plaf.windows.WindowsTextUI") != 0))
      return false;
    try
    {
      DefaultHighlighter.DefaultHighlightPainter localDefaultHighlightPainter = (DefaultHighlighter.DefaultHighlightPainter)localHighlightPainter;
      if ((localDefaultHighlightPainter.getColor() != null) && (!(localDefaultHighlightPainter.getColor().equals(paramJTextComponent.getSelectionColor()))))
        return false;
    }
    catch (ClassCastException localClassCastException)
    {
      return false;
    }
    return true;
  }

  public static boolean canAccessSystemClipboard()
  {
    boolean bool = false;
    if (!(GraphicsEnvironment.isHeadless()))
    {
      SecurityManager localSecurityManager = System.getSecurityManager();
      if (localSecurityManager == null)
      {
        bool = true;
      }
      else
      {
        try
        {
          localSecurityManager.checkSystemClipboardAccess();
          bool = true;
        }
        catch (SecurityException localSecurityException)
        {
        }
        if ((bool) && (!(isTrustedContext())))
          bool = canCurrentEventAccessSystemClipboard(true);
      }
    }
    return bool;
  }

  public static boolean canCurrentEventAccessSystemClipboard()
  {
    return ((isTrustedContext()) || (canCurrentEventAccessSystemClipboard(false)));
  }

  public static boolean canEventAccessSystemClipboard(AWTEvent paramAWTEvent)
  {
    return ((isTrustedContext()) || (canEventAccessSystemClipboard(paramAWTEvent, false)));
  }

  private static synchronized boolean inputEvent_canAccessSystemClipboard(InputEvent paramInputEvent)
  {
    if (inputEvent_CanAccessSystemClipboard_Field == null)
      inputEvent_CanAccessSystemClipboard_Field = (Field)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          Field localField = null;
          try
          {
            localField = InputEvent.class.getDeclaredField("canAccessSystemClipboard");
            localField.setAccessible(true);
            return localField;
          }
          catch (SecurityException localSecurityException)
          {
          }
          catch (NoSuchFieldException localNoSuchFieldException)
          {
          }
          return null;
        }
      });
    if (inputEvent_CanAccessSystemClipboard_Field == null)
      return false;
    boolean bool = false;
    try
    {
      bool = inputEvent_CanAccessSystemClipboard_Field.getBoolean(paramInputEvent);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
    }
    return bool;
  }

  private static boolean isAccessClipboardGesture(InputEvent paramInputEvent)
  {
    int i = 0;
    if (paramInputEvent instanceof KeyEvent)
    {
      KeyEvent localKeyEvent = (KeyEvent)paramInputEvent;
      int j = localKeyEvent.getKeyCode();
      int k = localKeyEvent.getModifiers();
      switch (j)
      {
      case 67:
      case 86:
      case 88:
        i = (k == 2) ? 1 : 0;
        break;
      case 155:
        i = ((k == 2) || (k == 1)) ? 1 : 0;
        break;
      case 65485:
      case 65487:
      case 65489:
        i = 1;
        break;
      case 127:
        i = (k == 1) ? 1 : 0;
      }
    }
    return i;
  }

  private static boolean canEventAccessSystemClipboard(AWTEvent paramAWTEvent, boolean paramBoolean)
  {
    if (EventQueue.isDispatchThread())
    {
      if ((paramAWTEvent instanceof InputEvent) && (((!(paramBoolean)) || (isAccessClipboardGesture((InputEvent)paramAWTEvent)))))
        return inputEvent_canAccessSystemClipboard((InputEvent)paramAWTEvent);
      return false;
    }
    return true;
  }

  private static boolean canCurrentEventAccessSystemClipboard(boolean paramBoolean)
  {
    AWTEvent localAWTEvent = EventQueue.getCurrentEvent();
    return canEventAccessSystemClipboard(localAWTEvent, paramBoolean);
  }

  private static boolean isTrustedContext()
  {
    return ((System.getSecurityManager() == null) || (AppContext.getAppContext().get("UNTRUSTED_CLIPBOARD_ACCESS_KEY") == null));
  }

  public static String displayPropertiesToCSS(Font paramFont, Color paramColor)
  {
    StringBuffer localStringBuffer = new StringBuffer("body {");
    if (paramFont != null)
    {
      localStringBuffer.append(" font-family: ");
      localStringBuffer.append(paramFont.getFamily());
      localStringBuffer.append(" ; ");
      localStringBuffer.append(" font-size: ");
      localStringBuffer.append(paramFont.getSize());
      localStringBuffer.append("pt ;");
      if (paramFont.isBold())
        localStringBuffer.append(" font-weight: 700 ; ");
      if (paramFont.isItalic())
        localStringBuffer.append(" font-style: italic ; ");
    }
    if (paramColor != null)
    {
      localStringBuffer.append(" color: #");
      if (paramColor.getRed() < 16)
        localStringBuffer.append('0');
      localStringBuffer.append(Integer.toHexString(paramColor.getRed()));
      if (paramColor.getGreen() < 16)
        localStringBuffer.append('0');
      localStringBuffer.append(Integer.toHexString(paramColor.getGreen()));
      if (paramColor.getBlue() < 16)
        localStringBuffer.append('0');
      localStringBuffer.append(Integer.toHexString(paramColor.getBlue()));
      localStringBuffer.append(" ; ");
    }
    localStringBuffer.append(" }");
    return localStringBuffer.toString();
  }

  public static Object makeIcon(Class<?> paramClass1, Class<?> paramClass2, String paramString)
  {
    return new UIDefaults.LazyValue(paramClass1, paramString, paramClass2)
    {
      public Object createValue(UIDefaults paramUIDefaults)
      {
        byte[] arrayOfByte = (byte[])(byte[])AccessController.doPrivileged(new PrivilegedAction(this)
        {
          public Object run()
          {
            InputStream localInputStream;
            try
            {
              localInputStream = null;
              for (Class localClass = this.this$0.val$baseClass; localClass != null; localClass = localClass.getSuperclass())
              {
                localInputStream = localClass.getResourceAsStream(this.this$0.val$imageFile);
                if (localInputStream != null)
                  break;
                if (localClass == this.this$0.val$rootClass)
                  break;
              }
              if (localInputStream == null)
                return null;
              BufferedInputStream localBufferedInputStream = new BufferedInputStream(localInputStream);
              ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(1024);
              byte[] arrayOfByte = new byte[1024];
              while ((i = localBufferedInputStream.read(arrayOfByte)) > 0)
              {
                int i;
                localByteArrayOutputStream.write(arrayOfByte, 0, i);
              }
              localBufferedInputStream.close();
              localByteArrayOutputStream.flush();
              return localByteArrayOutputStream.toByteArray();
            }
            catch (IOException localIOException)
            {
              System.err.println(localIOException.toString());
            }
            return null;
          }
        });
        if (arrayOfByte == null)
          return null;
        if (arrayOfByte.length == 0)
        {
          System.err.println("warning: " + this.val$imageFile + " is zero-length");
          return null;
        }
        return new ImageIconUIResource(arrayOfByte);
      }
    };
  }

  public static boolean isLocalDisplay()
  {
    try
    {
      if (System.getProperty("os.name").startsWith("Windows"))
        return true;
      Class localClass = Class.forName("sun.awt.X11GraphicsEnvironment");
      Method localMethod = localClass.getMethod("isDisplayLocal", new Class[0]);
      return ((Boolean)localMethod.invoke(null, (Object[])null)).booleanValue();
    }
    catch (Throwable localThrowable)
    {
    }
    return true;
  }

  public static int getUIDefaultsInt(Object paramObject)
  {
    return getUIDefaultsInt(paramObject, 0);
  }

  public static int getUIDefaultsInt(Object paramObject, Locale paramLocale)
  {
    return getUIDefaultsInt(paramObject, paramLocale, 0);
  }

  public static int getUIDefaultsInt(Object paramObject, int paramInt)
  {
    return getUIDefaultsInt(paramObject, null, paramInt);
  }

  public static int getUIDefaultsInt(Object paramObject, Locale paramLocale, int paramInt)
  {
    Object localObject = UIManager.get(paramObject, paramLocale);
    if (localObject instanceof Integer)
      return ((Integer)localObject).intValue();
    if (localObject instanceof String)
      try
      {
        return Integer.parseInt((String)localObject);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    return paramInt;
  }

  public static Component compositeRequestFocus(Component paramComponent)
  {
    if (paramComponent instanceof Container)
    {
      Object localObject2;
      Container localContainer = (Container)paramComponent;
      if (localContainer.isFocusCycleRoot())
      {
        localObject1 = localContainer.getFocusTraversalPolicy();
        localObject2 = ((FocusTraversalPolicy)localObject1).getDefaultComponent(localContainer);
        if (localObject2 != null)
        {
          ((Component)localObject2).requestFocus();
          return localObject2;
        }
      }
      Object localObject1 = localContainer.getFocusCycleRootAncestor();
      if (localObject1 != null)
      {
        localObject2 = ((Container)localObject1).getFocusTraversalPolicy();
        Component localComponent = ((FocusTraversalPolicy)localObject2).getComponentAfter((Container)localObject1, localContainer);
        if ((localComponent != null) && (SwingUtilities.isDescendingFrom(localComponent, localContainer)))
        {
          localComponent.requestFocus();
          return localComponent;
        }
      }
    }
    if (paramComponent.isFocusable())
    {
      paramComponent.requestFocus();
      return paramComponent;
    }
    return ((Component)(Component)null);
  }

  public static boolean tabbedPaneChangeFocusTo(Component paramComponent)
  {
    if (paramComponent == null)
      break label37;
    if (paramComponent.isFocusTraversable())
    {
      compositeRequestFocus(paramComponent);
      return true;
    }
    label37: return ((paramComponent instanceof JComponent) && (((JComponent)paramComponent).requestDefaultFocus()));
  }

  public static <V> Future<V> submit(Callable<V> paramCallable)
  {
    if (paramCallable == null)
      throw new NullPointerException();
    FutureTask localFutureTask = new FutureTask(paramCallable);
    execute(localFutureTask);
    return localFutureTask;
  }

  public static <V> Future<V> submit(Runnable paramRunnable, V paramV)
  {
    if (paramRunnable == null)
      throw new NullPointerException();
    FutureTask localFutureTask = new FutureTask(paramRunnable, paramV);
    execute(localFutureTask);
    return localFutureTask;
  }

  private static void execute(Runnable paramRunnable)
  {
    SwingUtilities.invokeLater(paramRunnable);
  }

  public static void setSkipClickCount(Component paramComponent, int paramInt)
  {
    if ((paramComponent instanceof JTextComponent) && (((JTextComponent)paramComponent).getCaret() instanceof DefaultCaret))
      ((JTextComponent)paramComponent).putClientProperty(SKIP_CLICK_COUNT, Integer.valueOf(paramInt));
  }

  public static int getAdjustedClickCount(JTextComponent paramJTextComponent, MouseEvent paramMouseEvent)
  {
    int i = paramMouseEvent.getClickCount();
    if (i == 1)
    {
      paramJTextComponent.putClientProperty(SKIP_CLICK_COUNT, null);
    }
    else
    {
      Integer localInteger = (Integer)paramJTextComponent.getClientProperty(SKIP_CLICK_COUNT);
      if (localInteger != null)
        return (i - localInteger.intValue());
    }
    return i;
  }

  private static Section liesIn(Rectangle paramRectangle, Point paramPoint, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    int i;
    int j;
    int k;
    boolean bool;
    if (paramBoolean1)
    {
      i = paramRectangle.x;
      j = paramPoint.x;
      k = paramRectangle.width;
      bool = paramBoolean2;
    }
    else
    {
      i = paramRectangle.y;
      j = paramPoint.y;
      k = paramRectangle.height;
      bool = true;
    }
    if (paramBoolean3)
    {
      l = (k >= 30) ? 10 : k / 3;
      if (j < i + l)
        return ((bool) ? Section.LEADING : Section.TRAILING);
      if (j >= i + k - l)
        return ((bool) ? Section.TRAILING : Section.LEADING);
      return Section.MIDDLE;
    }
    int l = i + k / 2;
    if (bool)
      return ((j >= l) ? Section.TRAILING : Section.LEADING);
    return ((j < l) ? Section.TRAILING : Section.LEADING);
  }

  public static Section liesInHorizontal(Rectangle paramRectangle, Point paramPoint, boolean paramBoolean1, boolean paramBoolean2)
  {
    return liesIn(paramRectangle, paramPoint, true, paramBoolean1, paramBoolean2);
  }

  public static Section liesInVertical(Rectangle paramRectangle, Point paramPoint, boolean paramBoolean)
  {
    return liesIn(paramRectangle, paramPoint, false, false, paramBoolean);
  }

  static
  {
    LAF_STATE_KEY = new StringBuffer("LookAndFeel State");
    DEFAULT_FRC = new FontRenderContext(null, false, false);
    AA_TEXT_PROPERTY_KEY = new StringBuffer("AATextInfoPropertyKey");
    SKIP_CLICK_COUNT = new StringBuilder("skipClickCount");
    COMPONENT_UI_PROPERTY_KEY = new StringBuffer("ComponentUIPropertyKey");
    BASICMENUITEMUI_MAX_TEXT_OFFSET = new StringBuilder("maxTextOffset");
    inputEvent_CanAccessSystemClipboard_Field = null;
    charsBufferLock = new Object();
    charsBuffer = new char[100];
    fontCache = new LSBCacheEntry[6];
  }

  public static class AATextInfo
  {
    Object aaHint;
    Integer lcdContrastHint;
    FontRenderContext frc;

    private static AATextInfo getAATextInfoFromMap(Map paramMap)
    {
      Object localObject1 = paramMap.get(RenderingHints.KEY_TEXT_ANTIALIASING);
      Object localObject2 = paramMap.get(RenderingHints.KEY_TEXT_LCD_CONTRAST);
      if ((localObject1 == null) || (localObject1 == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) || (localObject1 == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT))
        return null;
      return new AATextInfo(localObject1, (Integer)localObject2);
    }

    public static AATextInfo getAATextInfo(boolean paramBoolean)
    {
      SunToolkit.setAAFontSettingsCondition(paramBoolean);
      Toolkit localToolkit = Toolkit.getDefaultToolkit();
      Object localObject = localToolkit.getDesktopProperty("awt.font.desktophints");
      if (localObject instanceof Map)
        return getAATextInfoFromMap((Map)localObject);
      return null;
    }

    public AATextInfo(Object paramObject, Integer paramInteger)
    {
      if (paramObject == null)
        throw new InternalError("null not allowed here");
      if ((paramObject == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) || (paramObject == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT))
        throw new InternalError("AA must be on");
      this.aaHint = paramObject;
      this.lcdContrastHint = paramInteger;
      this.frc = new FontRenderContext(null, paramObject, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
    }
  }

  private static class LSBCacheEntry
  {
    private static final byte UNSET = 127;
    private static final char[] oneChar;
    private byte[] lsbCache = new byte[1];
    private Font font;
    private FontRenderContext frc;

    public LSBCacheEntry(FontRenderContext paramFontRenderContext, Font paramFont)
    {
      reset(paramFontRenderContext, paramFont);
    }

    public void reset(FontRenderContext paramFontRenderContext, Font paramFont)
    {
      this.font = paramFont;
      this.frc = paramFontRenderContext;
      for (int i = this.lsbCache.length - 1; i >= 0; --i)
        this.lsbCache[i] = 127;
    }

    public int getLeftSideBearing(char paramChar)
    {
      int i = paramChar - 'W';
      if ((!($assertionsDisabled)) && (((i < 0) || (i >= 1))))
        throw new AssertionError();
      int j = this.lsbCache[i];
      if (j == 127)
      {
        oneChar[0] = paramChar;
        GlyphVector localGlyphVector = this.font.createGlyphVector(this.frc, oneChar);
        j = (byte)localGlyphVector.getGlyphPixelBounds(0, this.frc, 0F, 0F).x;
        if (j < 0)
        {
          Object localObject = this.frc.getAntiAliasingHint();
          if ((localObject == RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB) || (localObject == RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR))
            j = (byte)(j + 1);
        }
        this.lsbCache[i] = j;
      }
      return j;
    }

    public boolean equals(Object paramObject)
    {
      if (paramObject == this)
        return true;
      if (!(paramObject instanceof LSBCacheEntry))
        return false;
      LSBCacheEntry localLSBCacheEntry = (LSBCacheEntry)paramObject;
      return ((this.font.equals(localLSBCacheEntry.font)) && (this.frc.equals(localLSBCacheEntry.frc)));
    }

    public int hashCode()
    {
      int i = 17;
      if (this.font != null)
        i = 37 * i + this.font.hashCode();
      if (this.frc != null)
        i = 37 * i + this.frc.hashCode();
      return i;
    }

    static
    {
      oneChar = new char[1];
    }
  }

  public static enum Section
  {
    LEADING, MIDDLE, TRAILING;
  }
}
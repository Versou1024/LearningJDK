package sun.awt.im;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.im.InputMethodRequests;
import java.text.AttributedCharacterIterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public final class CompositionArea extends JPanel
  implements InputMethodListener
{
  private CompositionAreaHandler handler;
  private TextLayout composedTextLayout;
  private TextHitInfo caret = null;
  private JFrame compositionWindow;
  private static final int TEXT_ORIGIN_X = 5;
  private static final int TEXT_ORIGIN_Y = 15;
  private static final int PASSIVE_WIDTH = 480;
  private static final int WIDTH_MARGIN = 10;
  private static final int HEIGHT_MARGIN = 3;

  CompositionArea()
  {
    String str = Toolkit.getProperty("AWT.CompositionWindowTitle", "Input Window");
    this.compositionWindow = ((JFrame)InputMethodContext.createInputMethodWindow(str, null, true));
    setOpaque(true);
    setBorder(LineBorder.createGrayLineBorder());
    setForeground(Color.black);
    setBackground(Color.white);
    enableInputMethods(true);
    enableEvents(8L);
    this.compositionWindow.getContentPane().add(this);
    this.compositionWindow.addWindowListener(new FrameWindowAdapter(this));
    addInputMethodListener(this);
    this.compositionWindow.enableInputMethods(false);
    this.compositionWindow.pack();
    Dimension localDimension1 = this.compositionWindow.getSize();
    Dimension localDimension2 = getToolkit().getScreenSize();
    this.compositionWindow.setLocation(localDimension2.width - localDimension1.width - 20, localDimension2.height - localDimension1.height - 100);
    this.compositionWindow.setVisible(false);
  }

  synchronized void setHandlerInfo(CompositionAreaHandler paramCompositionAreaHandler, InputContext paramInputContext)
  {
    this.handler = paramCompositionAreaHandler;
    ((InputMethodWindow)this.compositionWindow).setInputContext(paramInputContext);
  }

  public InputMethodRequests getInputMethodRequests()
  {
    return this.handler;
  }

  private Rectangle getCaretRectangle(TextHitInfo paramTextHitInfo)
  {
    int i = 0;
    TextLayout localTextLayout = this.composedTextLayout;
    if (localTextLayout != null)
      i = Math.round(localTextLayout.getCaretInfo(paramTextHitInfo)[0]);
    Graphics localGraphics = getGraphics();
    FontMetrics localFontMetrics = null;
    try
    {
      localFontMetrics = localGraphics.getFontMetrics();
    }
    finally
    {
      localGraphics.dispose();
    }
    return new Rectangle(5 + i, 15 - localFontMetrics.getAscent(), 0, localFontMetrics.getAscent() + localFontMetrics.getDescent());
  }

  public void paint(Graphics paramGraphics)
  {
    super.paint(paramGraphics);
    paramGraphics.setColor(getForeground());
    TextLayout localTextLayout = this.composedTextLayout;
    if (localTextLayout != null)
      localTextLayout.draw((Graphics2D)paramGraphics, 5.0F, 15.0F);
    if (this.caret != null)
    {
      Rectangle localRectangle = getCaretRectangle(this.caret);
      paramGraphics.setXORMode(getBackground());
      paramGraphics.fillRect(localRectangle.x, localRectangle.y, 1, localRectangle.height);
      paramGraphics.setPaintMode();
    }
  }

  void setCompositionAreaVisible(boolean paramBoolean)
  {
    this.compositionWindow.setVisible(paramBoolean);
  }

  boolean isCompositionAreaVisible()
  {
    return this.compositionWindow.isVisible();
  }

  public void inputMethodTextChanged(InputMethodEvent paramInputMethodEvent)
  {
    this.handler.inputMethodTextChanged(paramInputMethodEvent);
  }

  public void caretPositionChanged(InputMethodEvent paramInputMethodEvent)
  {
    this.handler.caretPositionChanged(paramInputMethodEvent);
  }

  void setText(AttributedCharacterIterator paramAttributedCharacterIterator, TextHitInfo paramTextHitInfo)
  {
    this.composedTextLayout = null;
    if (paramAttributedCharacterIterator == null)
    {
      this.compositionWindow.setVisible(false);
      this.caret = null;
    }
    else
    {
      if (!(this.compositionWindow.isVisible()))
        this.compositionWindow.setVisible(true);
      Graphics localGraphics = getGraphics();
      if (localGraphics == null)
        return;
      try
      {
        updateWindowLocation();
        FontRenderContext localFontRenderContext = ((Graphics2D)localGraphics).getFontRenderContext();
        this.composedTextLayout = new TextLayout(paramAttributedCharacterIterator, localFontRenderContext);
        Rectangle2D localRectangle2D1 = this.composedTextLayout.getBounds();
        this.caret = paramTextHitInfo;
        FontMetrics localFontMetrics = localGraphics.getFontMetrics();
        Rectangle2D localRectangle2D2 = localFontMetrics.getMaxCharBounds(localGraphics);
        int i = (int)localRectangle2D2.getHeight() + 3;
        int j = i + this.compositionWindow.getInsets().top + this.compositionWindow.getInsets().bottom;
        InputMethodRequests localInputMethodRequests = this.handler.getClientInputMethodRequests();
        int k = (localInputMethodRequests == null) ? 480 : (int)localRectangle2D1.getWidth() + 10;
        int l = k + this.compositionWindow.getInsets().left + this.compositionWindow.getInsets().right;
        setPreferredSize(new Dimension(k, i));
        this.compositionWindow.setSize(new Dimension(l, j));
        paint(localGraphics);
      }
      finally
      {
        localGraphics.dispose();
      }
    }
  }

  void setCaret(TextHitInfo paramTextHitInfo)
  {
    this.caret = paramTextHitInfo;
    if (this.compositionWindow.isVisible())
    {
      Graphics localGraphics = getGraphics();
      try
      {
        paint(localGraphics);
      }
      finally
      {
        localGraphics.dispose();
      }
    }
  }

  void updateWindowLocation()
  {
    InputMethodRequests localInputMethodRequests = this.handler.getClientInputMethodRequests();
    if (localInputMethodRequests == null)
      return;
    Point localPoint = new Point();
    Rectangle localRectangle = localInputMethodRequests.getTextLocation(null);
    Dimension localDimension1 = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension localDimension2 = this.compositionWindow.getSize();
    if (localRectangle.x + localDimension2.width > localDimension1.width)
      localPoint.x = (localDimension1.width - localDimension2.width);
    else
      localPoint.x = localRectangle.x;
    if (localRectangle.y + localRectangle.height + 2 + localDimension2.height > localDimension1.height)
      localPoint.y = (localRectangle.y - 2 - localDimension2.height);
    else
      localPoint.y = (localRectangle.y + localRectangle.height + 2);
    this.compositionWindow.setLocation(localPoint);
  }

  Rectangle getTextLocation(TextHitInfo paramTextHitInfo)
  {
    Rectangle localRectangle = getCaretRectangle(paramTextHitInfo);
    Point localPoint = getLocationOnScreen();
    localRectangle.translate(localPoint.x, localPoint.y);
    return localRectangle;
  }

  TextHitInfo getLocationOffset(int paramInt1, int paramInt2)
  {
    TextLayout localTextLayout = this.composedTextLayout;
    if (localTextLayout == null)
      return null;
    Point localPoint = getLocationOnScreen();
    paramInt1 -= localPoint.x + 5;
    paramInt2 -= localPoint.y + 15;
    if (localTextLayout.getBounds().contains(paramInt1, paramInt2))
      return localTextLayout.hitTestChar(paramInt1, paramInt2);
    return null;
  }

  void setCompositionAreaUndecorated(boolean paramBoolean)
  {
    if (this.compositionWindow.isDisplayable())
      this.compositionWindow.removeNotify();
    this.compositionWindow.setUndecorated(paramBoolean);
    this.compositionWindow.pack();
  }

  class FrameWindowAdapter extends WindowAdapter
  {
    public void windowActivated()
    {
      this.this$0.requestFocus();
    }
  }
}
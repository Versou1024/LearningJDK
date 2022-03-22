package sun.swing.text;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLDocument.Iterator;
import sun.font.FontDesignMetrics;
import sun.swing.text.html.FrameEditorPaneTag;

public class TextComponentPrintable
  implements CountingPrintable
{
  private static final int LIST_SIZE = 1000;
  private boolean isLayouted = false;
  private final JTextComponent textComponentToPrint;
  private final AtomicReference<FontRenderContext> frc = new AtomicReference(null);
  private final JTextComponent printShell;
  private final MessageFormat headerFormat;
  private final MessageFormat footerFormat;
  private static final float HEADER_FONT_SIZE = 18.0F;
  private static final float FOOTER_FONT_SIZE = 12.0F;
  private final Font headerFont;
  private final Font footerFont;
  private final List<IntegerSegment> rowsMetrics;
  private final List<IntegerSegment> pagesMetrics;
  private boolean needReadLock = false;

  public static Printable getPrintable(JTextComponent paramJTextComponent, MessageFormat paramMessageFormat1, MessageFormat paramMessageFormat2)
  {
    if ((paramJTextComponent instanceof JEditorPane) && (isFrameSetDocument(paramJTextComponent.getDocument())))
    {
      List localList = getFrames((JEditorPane)paramJTextComponent);
      ArrayList localArrayList = new ArrayList();
      Iterator localIterator = localList.iterator();
      while (localIterator.hasNext())
      {
        JEditorPane localJEditorPane = (JEditorPane)localIterator.next();
        localArrayList.add((CountingPrintable)getPrintable(localJEditorPane, paramMessageFormat1, paramMessageFormat2));
      }
      return new CompoundPrintable(localArrayList);
    }
    return new TextComponentPrintable(paramJTextComponent, paramMessageFormat1, paramMessageFormat2);
  }

  private static boolean isFrameSetDocument(Document paramDocument)
  {
    int i = 0;
    if (paramDocument instanceof HTMLDocument)
    {
      HTMLDocument localHTMLDocument = (HTMLDocument)paramDocument;
      if (localHTMLDocument.getIterator(HTML.Tag.FRAME).isValid())
        i = 1;
    }
    return i;
  }

  private static List<JEditorPane> getFrames(JEditorPane paramJEditorPane)
  {
    ArrayList localArrayList = new ArrayList();
    getFrames(paramJEditorPane, localArrayList);
    if (localArrayList.size() == 0)
    {
      createFrames(paramJEditorPane);
      getFrames(paramJEditorPane, localArrayList);
    }
    return localArrayList;
  }

  private static void getFrames(Container paramContainer, List<JEditorPane> paramList)
  {
    Component[] arrayOfComponent = paramContainer.getComponents();
    int i = arrayOfComponent.length;
    for (int j = 0; j < i; ++j)
    {
      Component localComponent = arrayOfComponent[j];
      if ((localComponent instanceof FrameEditorPaneTag) && (localComponent instanceof JEditorPane))
        paramList.add((JEditorPane)localComponent);
      else if (localComponent instanceof Container)
        getFrames((Container)localComponent, paramList);
    }
  }

  private static void createFrames(JEditorPane paramJEditorPane)
  {
    1 local1 = new Runnable(paramJEditorPane)
    {
      public void run()
      {
        CellRendererPane localCellRendererPane = new CellRendererPane();
        localCellRendererPane.add(this.val$editor);
        localCellRendererPane.setSize(500, 500);
      }
    };
    if (SwingUtilities.isEventDispatchThread())
      local1.run();
    else
      try
      {
        SwingUtilities.invokeAndWait(local1);
      }
      catch (Exception localException)
      {
        if (localException instanceof RuntimeException)
          throw ((RuntimeException)localException);
        throw new RuntimeException(localException);
      }
  }

  private TextComponentPrintable(JTextComponent paramJTextComponent, MessageFormat paramMessageFormat1, MessageFormat paramMessageFormat2)
  {
    this.textComponentToPrint = paramJTextComponent;
    this.headerFormat = paramMessageFormat1;
    this.footerFormat = paramMessageFormat2;
    this.headerFont = paramJTextComponent.getFont().deriveFont(1, 18.0F);
    this.footerFont = paramJTextComponent.getFont().deriveFont(0, 12.0F);
    this.pagesMetrics = Collections.synchronizedList(new ArrayList());
    this.rowsMetrics = new ArrayList(1000);
    this.printShell = createPrintShell(paramJTextComponent);
  }

  private JTextComponent createPrintShell(JTextComponent paramJTextComponent)
  {
    if (SwingUtilities.isEventDispatchThread())
      return createPrintShellOnEDT(paramJTextComponent);
    FutureTask localFutureTask = new FutureTask(new Callable(this, paramJTextComponent)
    {
      public JTextComponent call()
        throws Exception
      {
        return TextComponentPrintable.access$000(this.this$0, this.val$textComponent);
      }
    });
    SwingUtilities.invokeLater(localFutureTask);
    try
    {
      return ((JTextComponent)localFutureTask.get());
    }
    catch (InterruptedException localInterruptedException)
    {
      throw new RuntimeException(localInterruptedException);
    }
    catch (ExecutionException localExecutionException)
    {
      Throwable localThrowable = localExecutionException.getCause();
      if (localThrowable instanceof Error)
        throw ((Error)localThrowable);
      if (localThrowable instanceof RuntimeException)
        throw ((RuntimeException)localThrowable);
      throw new AssertionError(localThrowable);
    }
  }

  private JTextComponent createPrintShellOnEDT(JTextComponent paramJTextComponent)
  {
    if ((!($assertionsDisabled)) && (!(SwingUtilities.isEventDispatchThread())))
      throw new AssertionError();
    Object localObject = null;
    if (paramJTextComponent instanceof JTextField)
      localObject = new JTextField(this, paramJTextComponent)
      {
        public FontMetrics getFontMetrics()
        {
          return ((TextComponentPrintable.access$100(this.this$0).get() == null) ? super.getFontMetrics(paramFont) : FontDesignMetrics.getMetrics(paramFont, (FontRenderContext)TextComponentPrintable.access$100(this.this$0).get()));
        }
      };
    else if (paramJTextComponent instanceof JTextArea)
      localObject = new JTextArea(this, paramJTextComponent)
      {
        public FontMetrics getFontMetrics()
        {
          return ((TextComponentPrintable.access$100(this.this$0).get() == null) ? super.getFontMetrics(paramFont) : FontDesignMetrics.getMetrics(paramFont, (FontRenderContext)TextComponentPrintable.access$100(this.this$0).get()));
        }
      };
    else if (paramJTextComponent instanceof JTextPane)
      localObject = new JTextPane(this, paramJTextComponent)
      {
        public FontMetrics getFontMetrics()
        {
          return ((TextComponentPrintable.access$100(this.this$0).get() == null) ? super.getFontMetrics(paramFont) : FontDesignMetrics.getMetrics(paramFont, (FontRenderContext)TextComponentPrintable.access$100(this.this$0).get()));
        }

        public EditorKit getEditorKit()
        {
          if (getDocument() == this.val$textComponent.getDocument())
            return ((JTextPane)this.val$textComponent).getEditorKit();
          return super.getEditorKit();
        }
      };
    else if (paramJTextComponent instanceof JEditorPane)
      localObject = new JEditorPane(this, paramJTextComponent)
      {
        public FontMetrics getFontMetrics()
        {
          return ((TextComponentPrintable.access$100(this.this$0).get() == null) ? super.getFontMetrics(paramFont) : FontDesignMetrics.getMetrics(paramFont, (FontRenderContext)TextComponentPrintable.access$100(this.this$0).get()));
        }

        public EditorKit getEditorKit()
        {
          if (getDocument() == this.val$textComponent.getDocument())
            return ((JEditorPane)this.val$textComponent).getEditorKit();
          return super.getEditorKit();
        }
      };
    ((JTextComponent)localObject).setBorder(null);
    ((JTextComponent)localObject).setOpaque(paramJTextComponent.isOpaque());
    ((JTextComponent)localObject).setEditable(paramJTextComponent.isEditable());
    ((JTextComponent)localObject).setEnabled(paramJTextComponent.isEnabled());
    ((JTextComponent)localObject).setFont(paramJTextComponent.getFont());
    ((JTextComponent)localObject).setBackground(paramJTextComponent.getBackground());
    ((JTextComponent)localObject).setForeground(paramJTextComponent.getForeground());
    ((JTextComponent)localObject).setComponentOrientation(paramJTextComponent.getComponentOrientation());
    if (localObject instanceof JEditorPane)
    {
      ((JTextComponent)localObject).putClientProperty("JEditorPane.honorDisplayProperties", paramJTextComponent.getClientProperty("JEditorPane.honorDisplayProperties"));
      ((JTextComponent)localObject).putClientProperty("JEditorPane.w3cLengthUnits", paramJTextComponent.getClientProperty("JEditorPane.w3cLengthUnits"));
      ((JTextComponent)localObject).putClientProperty("charset", paramJTextComponent.getClientProperty("charset"));
    }
    ((JTextComponent)localObject).setDocument(paramJTextComponent.getDocument());
    return ((JTextComponent)localObject);
  }

  public int getNumberOfPages()
  {
    return this.pagesMetrics.size();
  }

  public int print(Graphics paramGraphics, PageFormat paramPageFormat, int paramInt)
    throws PrinterException
  {
    int i;
    if (!(this.isLayouted))
    {
      if (paramGraphics instanceof Graphics2D)
        this.frc.set(((Graphics2D)paramGraphics).getFontRenderContext());
      layout((int)Math.floor(paramPageFormat.getImageableWidth()));
      calculateRowsMetrics();
    }
    if (!(SwingUtilities.isEventDispatchThread()))
    {
      7 local7 = new Callable(this, paramGraphics, paramPageFormat, paramInt)
      {
        public Integer call()
          throws Exception
        {
          return Integer.valueOf(TextComponentPrintable.access$200(this.this$0, this.val$graphics, this.val$pf, this.val$pageIndex));
        }
      };
      FutureTask localFutureTask = new FutureTask(local7);
      SwingUtilities.invokeLater(localFutureTask);
      try
      {
        i = ((Integer)localFutureTask.get()).intValue();
      }
      catch (InterruptedException localInterruptedException)
      {
        throw new RuntimeException(localInterruptedException);
      }
      catch (ExecutionException localExecutionException)
      {
        Throwable localThrowable = localExecutionException.getCause();
        if (localThrowable instanceof PrinterException)
          throw ((PrinterException)localThrowable);
        if (localThrowable instanceof RuntimeException)
          throw ((RuntimeException)localThrowable);
        if (localThrowable instanceof Error)
          throw ((Error)localThrowable);
        throw new RuntimeException(localThrowable);
      }
    }
    else
    {
      i = printOnEDT(paramGraphics, paramPageFormat, paramInt);
    }
    return i;
  }

  private int printOnEDT(Graphics paramGraphics, PageFormat paramPageFormat, int paramInt)
    throws PrinterException
  {
    if ((!($assertionsDisabled)) && (!(SwingUtilities.isEventDispatchThread())))
      throw new AssertionError();
    Object localObject1 = BorderFactory.createEmptyBorder();
    if ((this.headerFormat != null) || (this.footerFormat != null))
    {
      localObject2 = { Integer.valueOf(paramInt + 1) };
      if (this.headerFormat != null)
        localObject1 = new TitledBorder((Border)localObject1, this.headerFormat.format(localObject2), 2, 1, this.headerFont, this.printShell.getForeground());
      if (this.footerFormat != null)
        localObject1 = new TitledBorder((Border)localObject1, this.footerFormat.format(localObject2), 2, 6, this.footerFont, this.printShell.getForeground());
    }
    Object localObject2 = ((Border)localObject1).getBorderInsets(this.printShell);
    updatePagesMetrics(paramInt, (int)Math.floor(paramPageFormat.getImageableHeight()) - ((Insets)localObject2).top - ((Insets)localObject2).bottom);
    if (this.pagesMetrics.size() <= paramInt)
      return 1;
    Graphics2D localGraphics2D = (Graphics2D)paramGraphics.create();
    localGraphics2D.translate(paramPageFormat.getImageableX(), paramPageFormat.getImageableY());
    ((Border)localObject1).paintBorder(this.printShell, localGraphics2D, 0, 0, (int)Math.floor(paramPageFormat.getImageableWidth()), (int)Math.floor(paramPageFormat.getImageableHeight()));
    localGraphics2D.translate(0, ((Insets)localObject2).top);
    Rectangle localRectangle = new Rectangle(0, 0, (int)paramPageFormat.getWidth(), ((IntegerSegment)this.pagesMetrics.get(paramInt)).end - ((IntegerSegment)this.pagesMetrics.get(paramInt)).start + 1);
    localGraphics2D.clip(localRectangle);
    int i = 0;
    if (ComponentOrientation.RIGHT_TO_LEFT == this.printShell.getComponentOrientation())
      i = (int)paramPageFormat.getImageableWidth() - this.printShell.getWidth();
    localGraphics2D.translate(i, -((IntegerSegment)this.pagesMetrics.get(paramInt)).start);
    this.printShell.print(localGraphics2D);
    return 0;
  }

  private void releaseReadLock()
  {
    if ((!($assertionsDisabled)) && (SwingUtilities.isEventDispatchThread()))
      throw new AssertionError();
    Document localDocument = this.textComponentToPrint.getDocument();
    if (localDocument instanceof AbstractDocument)
      try
      {
        ((AbstractDocument)localDocument).readUnlock();
        this.needReadLock = true;
      }
      catch (Error localError)
      {
      }
  }

  private void acquireReadLock()
  {
    if ((!($assertionsDisabled)) && (SwingUtilities.isEventDispatchThread()))
      throw new AssertionError();
    if (this.needReadLock)
    {
      try
      {
        SwingUtilities.invokeAndWait(new Runnable(this)
        {
          public void run()
          {
          }
        });
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      catch (InvocationTargetException localInvocationTargetException)
      {
      }
      Document localDocument = this.textComponentToPrint.getDocument();
      ((AbstractDocument)localDocument).readLock();
      this.needReadLock = false;
    }
  }

  private void layout(int paramInt)
  {
    if (!(SwingUtilities.isEventDispatchThread()))
    {
      9 local9 = new Callable(this, paramInt)
      {
        public Object call()
          throws Exception
        {
          TextComponentPrintable.access$300(this.this$0, this.val$width);
          return null;
        }
      };
      FutureTask localFutureTask = new FutureTask(local9);
      releaseReadLock();
      SwingUtilities.invokeLater(localFutureTask);
      try
      {
        localFutureTask.get();
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      catch (ExecutionException localExecutionException)
      {
        Throwable localThrowable = localExecutionException.getCause();
        if (localThrowable instanceof RuntimeException)
          throw ((RuntimeException)localThrowable);
        if (localThrowable instanceof Error);
        throw new RuntimeException(localThrowable);
      }
      finally
      {
        acquireReadLock();
      }
    }
    else
    {
      layoutOnEDT(paramInt);
    }
    this.isLayouted = true;
  }

  private void layoutOnEDT(int paramInt)
  {
    if ((!($assertionsDisabled)) && (!(SwingUtilities.isEventDispatchThread())))
      throw new AssertionError();
    CellRendererPane localCellRendererPane = new CellRendererPane();
    JViewport localJViewport = new JViewport();
    localJViewport.setBorder(null);
    Dimension localDimension = new Dimension(paramInt, 2147482647);
    if (this.printShell instanceof JTextField)
      localDimension = new Dimension(localDimension.width, this.printShell.getPreferredSize().height);
    this.printShell.setSize(localDimension);
    localJViewport.setComponentOrientation(this.printShell.getComponentOrientation());
    localJViewport.setSize(localDimension);
    localJViewport.add(this.printShell);
    localCellRendererPane.add(localJViewport);
  }

  private void updatePagesMetrics(int paramInt1, int paramInt2)
  {
    while ((paramInt1 >= this.pagesMetrics.size()) && (!(this.rowsMetrics.isEmpty())))
    {
      int i = this.pagesMetrics.size() - 1;
      int j = (i >= 0) ? ((IntegerSegment)this.pagesMetrics.get(i)).end + 1 : 0;
      for (int k = 0; (k < this.rowsMetrics.size()) && (((IntegerSegment)this.rowsMetrics.get(k)).end - j + 1 <= paramInt2); ++k);
      if (k == 0)
      {
        this.pagesMetrics.add(new IntegerSegment(j, j + paramInt2 - 1));
      }
      else
      {
        --k;
        this.pagesMetrics.add(new IntegerSegment(j, ((IntegerSegment)this.rowsMetrics.get(k)).end));
        for (int l = 0; l <= k; ++l)
          this.rowsMetrics.remove(0);
      }
    }
  }

  private void calculateRowsMetrics()
  {
    int i = this.printShell.getDocument().getLength();
    ArrayList localArrayList = new ArrayList(1000);
    int j = 0;
    int k = -1;
    int l = -1;
    while (j < i)
    {
      try
      {
        Rectangle localRectangle = this.printShell.modelToView(j);
        if (localRectangle != null)
        {
          int i1 = (int)localRectangle.getY();
          int i2 = (int)localRectangle.getHeight();
          if ((i2 != 0) && (((i1 != k) || (i2 != l))))
          {
            k = i1;
            l = i2;
            localArrayList.add(new IntegerSegment(i1, i1 + i2 - 1));
          }
        }
      }
      catch (BadLocationException localBadLocationException)
      {
        if (!($assertionsDisabled))
          throw new AssertionError();
      }
      ++j;
    }
    Collections.sort(localArrayList);
    j = -2147483648;
    k = -2147483648;
    Iterator localIterator = localArrayList.iterator();
    while (localIterator.hasNext())
    {
      IntegerSegment localIntegerSegment = (IntegerSegment)localIterator.next();
      if (k < localIntegerSegment.start)
      {
        if (k != -2147483648)
          this.rowsMetrics.add(new IntegerSegment(j, k));
        j = localIntegerSegment.start;
        k = localIntegerSegment.end;
      }
      else
      {
        k = localIntegerSegment.end;
      }
    }
    if (k != -2147483648)
      this.rowsMetrics.add(new IntegerSegment(j, k));
  }

  private static class IntegerSegment
  implements Comparable<IntegerSegment>
  {
    final int start;
    final int end;

    IntegerSegment(int paramInt1, int paramInt2)
    {
      this.start = paramInt1;
      this.end = paramInt2;
    }

    public int compareTo(IntegerSegment paramIntegerSegment)
    {
      int i = this.start - paramIntegerSegment.start;
      return ((i != 0) ? i : this.end - paramIntegerSegment.end);
    }

    public boolean equals(Object paramObject)
    {
      if (paramObject instanceof IntegerSegment)
        return (compareTo((IntegerSegment)paramObject) == 0);
      return false;
    }

    public int hashCode()
    {
      int i = 17;
      i = 37 * i + this.start;
      i = 37 * i + this.end;
      return i;
    }

    public String toString()
    {
      return "IntegerSegment [" + this.start + ", " + this.end + "]";
    }
  }
}
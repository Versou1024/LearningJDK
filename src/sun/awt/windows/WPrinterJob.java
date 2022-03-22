package sun.awt.windows;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.peer.ComponentPeer;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FilePermission;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.StreamPrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.standard.Sides;
import sun.awt.Win32GraphicsEnvironment;
import sun.font.Font2D;
import sun.font.FontManager;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;
import sun.java2d.DisposerTarget;
import sun.print.PeekGraphics;
import sun.print.PeekMetrics;
import sun.print.RasterPrinterJob;
import sun.print.ServiceDialog;
import sun.print.SunAlternateMedia;
import sun.print.SunMinMaxPage;
import sun.print.SunPageSelection;
import sun.print.Win32MediaTray;
import sun.print.Win32PrintService;
import sun.print.Win32PrintServiceLookup;

public class WPrinterJob extends RasterPrinterJob
  implements DisposerTarget
{
  protected static final long PS_ENDCAP_ROUND = 0L;
  protected static final long PS_ENDCAP_SQUARE = 256L;
  protected static final long PS_ENDCAP_FLAT = 512L;
  protected static final long PS_JOIN_ROUND = 0L;
  protected static final long PS_JOIN_BEVEL = 4096L;
  protected static final long PS_JOIN_MITER = 8192L;
  protected static final int POLYFILL_ALTERNATE = 1;
  protected static final int POLYFILL_WINDING = 2;
  private static final int MAX_WCOLOR = 255;
  private static final int SET_DUP_VERTICAL = 16;
  private static final int SET_DUP_HORIZONTAL = 32;
  private static final int SET_RES_HIGH = 64;
  private static final int SET_RES_LOW = 128;
  private static final int SET_COLOR = 512;
  private static final int SET_ORIENTATION = 16384;
  private static final int PD_ALLPAGES = 0;
  private static final int PD_SELECTION = 1;
  private static final int PD_PAGENUMS = 2;
  private static final int PD_NOSELECTION = 4;
  private static final int PD_COLLATE = 16;
  private static final int PD_PRINTTOFILE = 32;
  private static final int DM_ORIENTATION = 1;
  private static final int DM_PRINTQUALITY = 1024;
  private static final int DM_COLOR = 2048;
  private static final int DM_DUPLEX = 4096;
  private static final int MAX_UNKNOWN_PAGES = 9999;
  private boolean driverDoesMultipleCopies = false;
  private boolean driverDoesCollation = false;
  private boolean userRequestedCollation = false;
  private boolean noDefaultPrinter = false;
  private HandleRecord handleRecord = new HandleRecord();
  private int mPrintPaperSize;
  private int mPrintXRes;
  private int mPrintYRes;
  private int mPrintPhysX;
  private int mPrintPhysY;
  private int mPrintWidth;
  private int mPrintHeight;
  private int mPageWidth;
  private int mPageHeight;
  private int mAttSides;
  private int mAttChromaticity;
  private int mAttXRes;
  private int mAttYRes;
  private int mAttQuality;
  private int mAttCollate;
  private int mAttCopies;
  private int mAttOrientation;
  private int mAttMediaSizeName;
  private int mAttMediaTray;
  private String mDestination = null;
  private Color mLastColor;
  private Color mLastTextColor;
  private Font mLastFont;
  private int mLastRotation;
  private float mLastAwScale;
  Hashtable<FontKey1, Font> fontCache1 = new Hashtable();
  Hashtable<FontKey2, Font> fontCache2 = new Hashtable();
  private PrinterJob pjob;
  private ComponentPeer dialogOwnerPeer = null;
  private Object disposerReferent = new Object();

  public WPrinterJob()
  {
    Disposer.addRecord(this.disposerReferent, this.handleRecord = new HandleRecord());
    initAttributeMembers();
  }

  public Object getDisposerReferent()
  {
    return this.disposerReferent;
  }

  public PageFormat pageDialog(PageFormat paramPageFormat)
    throws HeadlessException
  {
    if (GraphicsEnvironment.isHeadless())
      throw new HeadlessException();
    if (getPrintService() instanceof StreamPrintService)
      return super.pageDialog(paramPageFormat);
    PageFormat localPageFormat = (PageFormat)paramPageFormat.clone();
    boolean bool = false;
    WPageDialog localWPageDialog = new WPageDialog((Frame)null, this, localPageFormat, null);
    localWPageDialog.setRetVal(false);
    localWPageDialog.setVisible(true);
    bool = localWPageDialog.getRetVal();
    localWPageDialog.dispose();
    if ((bool) && (this.myService != null))
    {
      String str = getNativePrintService();
      if (!(this.myService.getName().equals(str)))
        try
        {
          setPrintService(Win32PrintServiceLookup.getWin32PrintLUS().getPrintServiceByName(str));
        }
        catch (PrinterException localPrinterException)
        {
        }
      updatePageAttributes(this.myService, localPageFormat);
      return localPageFormat;
    }
    return paramPageFormat;
  }

  private boolean displayNativeDialog()
  {
    if (this.attributes == null)
      return false;
    WPrintDialog localWPrintDialog = new WPrintDialog((Frame)null, this);
    localWPrintDialog.setRetVal(false);
    localWPrintDialog.setVisible(true);
    boolean bool = localWPrintDialog.getRetVal();
    localWPrintDialog.dispose();
    Destination localDestination = (Destination)this.attributes.get(Destination.class);
    if ((localDestination == null) || (!(bool)))
      return bool;
    FileDialog localFileDialog = new FileDialog((Frame)null, null, 1);
    URI localURI = localDestination.getURI();
    String str = (localURI != null) ? localURI.getSchemeSpecificPart() : null;
    if (str != null)
    {
      localObject1 = new File(str);
      localFileDialog.setFile(((File)localObject1).getName());
      localObject2 = ((File)localObject1).getParentFile();
      if (localObject2 != null)
        localFileDialog.setDirectory(((File)localObject2).getPath());
    }
    else
    {
      localFileDialog.setFile("out.prn");
    }
    localFileDialog.setVisible(true);
    Object localObject1 = localFileDialog.getFile();
    if (localObject1 == null)
    {
      localFileDialog.dispose();
      return false;
    }
    Object localObject2 = localFileDialog.getDirectory() + ((String)localObject1);
    File localFile1 = new File((String)localObject2);
    for (File localFile2 = localFile1.getParentFile(); ((localFile1.exists()) && (((!(localFile1.isFile())) || (!(localFile1.canWrite()))))) || ((localFile2 != null) && (((!(localFile2.exists())) || ((localFile2.exists()) && (!(localFile2.canWrite())))))); localFile2 = localFile1.getParentFile())
    {
      new PrintToFileErrorDialog(this, (Frame)null, ServiceDialog.getMsg("dialog.owtitle"), ServiceDialog.getMsg("dialog.writeerror") + " " + ((String)localObject2), ServiceDialog.getMsg("button.ok")).setVisible(true);
      localFileDialog.setVisible(true);
      localObject1 = localFileDialog.getFile();
      if (localObject1 == null)
      {
        localFileDialog.dispose();
        return false;
      }
      localObject2 = localFileDialog.getDirectory() + ((String)localObject1);
      localFile1 = new File((String)localObject2);
    }
    localFileDialog.dispose();
    this.attributes.add(new Destination(localFile1.toURI()));
    return true;
  }

  public boolean printDialog()
    throws HeadlessException
  {
    if (GraphicsEnvironment.isHeadless())
      throw new HeadlessException();
    if (this.attributes == null)
      this.attributes = new HashPrintRequestAttributeSet();
    if (getPrintService() instanceof StreamPrintService)
      return super.printDialog(this.attributes);
    if (this.noDefaultPrinter == true)
      return false;
    return displayNativeDialog();
  }

  public void setPrintService(PrintService paramPrintService)
    throws PrinterException
  {
    super.setPrintService(paramPrintService);
    if (paramPrintService instanceof StreamPrintService)
      return;
    this.driverDoesMultipleCopies = false;
    this.driverDoesCollation = false;
    setNativePrintService(paramPrintService.getName());
  }

  private native void setNativePrintService(String paramString);

  public PrintService getPrintService()
  {
    if (this.myService == null)
    {
      String str = getNativePrintService();
      if (str != null)
      {
        this.myService = Win32PrintServiceLookup.getWin32PrintLUS().getPrintServiceByName(str);
        if (this.myService != null)
          return this.myService;
      }
      this.myService = PrintServiceLookup.lookupDefaultPrintService();
      if (this.myService != null)
        setNativePrintService(this.myService.getName());
    }
    return this.myService;
  }

  private native String getNativePrintService();

  private void initAttributeMembers()
  {
    this.mAttSides = 0;
    this.mAttChromaticity = 0;
    this.mAttXRes = 0;
    this.mAttYRes = 0;
    this.mAttQuality = 0;
    this.mAttCollate = -1;
    this.mAttCopies = 0;
    this.mAttOrientation = 0;
    this.mAttMediaTray = 0;
    this.mAttMediaSizeName = 0;
    this.mDestination = null;
  }

  protected void setAttributes(PrintRequestAttributeSet paramPrintRequestAttributeSet)
    throws PrinterException
  {
    initAttributeMembers();
    super.setAttributes(paramPrintRequestAttributeSet);
    this.mAttCopies = getCopiesInt();
    this.mDestination = this.destinationAttr;
    if (paramPrintRequestAttributeSet == null)
      return;
    Attribute[] arrayOfAttribute = paramPrintRequestAttributeSet.toArray();
    for (int i = 0; i < arrayOfAttribute.length; ++i)
      try
      {
        Object localObject1 = arrayOfAttribute[i];
        if (((Attribute)localObject1).getCategory() == Sides.class)
        {
          if (localObject1.equals(Sides.TWO_SIDED_LONG_EDGE))
            this.mAttSides = 2;
          else if (localObject1.equals(Sides.TWO_SIDED_SHORT_EDGE))
            this.mAttSides = 3;
          else
            this.mAttSides = 1;
        }
        else if (((Attribute)localObject1).getCategory() == Chromaticity.class)
        {
          if (localObject1.equals(Chromaticity.COLOR))
            this.mAttChromaticity = 2;
          else
            this.mAttChromaticity = 1;
        }
        else
        {
          Object localObject2;
          if (((Attribute)localObject1).getCategory() == PrinterResolution.class)
          {
            localObject2 = (PrinterResolution)localObject1;
            this.mAttXRes = ((PrinterResolution)localObject2).getCrossFeedResolution(100);
            this.mAttYRes = ((PrinterResolution)localObject2).getFeedResolution(100);
          }
          else if (((Attribute)localObject1).getCategory() == PrintQuality.class)
          {
            if (localObject1.equals(PrintQuality.HIGH))
              this.mAttQuality = -4;
            else if (localObject1.equals(PrintQuality.NORMAL))
              this.mAttQuality = -3;
            else
              this.mAttQuality = -2;
          }
          else if (((Attribute)localObject1).getCategory() == SheetCollate.class)
          {
            if (localObject1.equals(SheetCollate.COLLATED))
              this.mAttCollate = 1;
            else
              this.mAttCollate = 0;
          }
          else if ((((Attribute)localObject1).getCategory() == Media.class) || (((Attribute)localObject1).getCategory() == SunAlternateMedia.class))
          {
            if (((Attribute)localObject1).getCategory() == SunAlternateMedia.class)
            {
              localObject2 = (Media)paramPrintRequestAttributeSet.get(Media.class);
              if ((localObject2 == null) || (!(localObject2 instanceof MediaTray)))
                localObject1 = ((SunAlternateMedia)localObject1).getMedia();
            }
            if ((!(localObject1 instanceof MediaSizeName)) || (localObject1 instanceof MediaTray))
              if (localObject1.equals(MediaTray.BOTTOM))
                this.mAttMediaTray = 2;
              else if (localObject1.equals(MediaTray.ENVELOPE))
                this.mAttMediaTray = 5;
              else if (localObject1.equals(MediaTray.LARGE_CAPACITY))
                this.mAttMediaTray = 11;
              else if (localObject1.equals(MediaTray.MAIN))
                this.mAttMediaTray = 1;
              else if (localObject1.equals(MediaTray.MANUAL))
                this.mAttMediaTray = 4;
              else if (localObject1.equals(MediaTray.MIDDLE))
                this.mAttMediaTray = 3;
              else if (localObject1.equals(MediaTray.SIDE))
                this.mAttMediaTray = 7;
              else if (localObject1.equals(MediaTray.TOP))
                this.mAttMediaTray = 1;
              else if (localObject1 instanceof Win32MediaTray)
                this.mAttMediaTray = ((Win32MediaTray)localObject1).winID;
              else
                this.mAttMediaTray = 1;
          }
        }
      }
      catch (ClassCastException localClassCastException)
      {
      }
  }

  private native void getDefaultPage(PageFormat paramPageFormat);

  public PageFormat defaultPage(PageFormat paramPageFormat)
  {
    PageFormat localPageFormat = (PageFormat)paramPageFormat.clone();
    getDefaultPage(localPageFormat);
    return localPageFormat;
  }

  protected native void validatePaper(Paper paramPaper1, Paper paramPaper2);

  protected Graphics2D createPathGraphics(PeekGraphics paramPeekGraphics, PrinterJob paramPrinterJob, Printable paramPrintable, PageFormat paramPageFormat, int paramInt)
  {
    WPathGraphics localWPathGraphics;
    PeekMetrics localPeekMetrics = paramPeekGraphics.getMetrics();
    if ((!(forcePDL)) && (((forceRaster == true) || (localPeekMetrics.hasNonSolidColors()) || (localPeekMetrics.hasCompositing()))))
    {
      localWPathGraphics = null;
    }
    else
    {
      BufferedImage localBufferedImage = new BufferedImage(8, 8, 1);
      Graphics2D localGraphics2D = localBufferedImage.createGraphics();
      boolean bool = !(paramPeekGraphics.getAWTDrawingOnly());
      localWPathGraphics = new WPathGraphics(localGraphics2D, paramPrinterJob, paramPrintable, paramPageFormat, paramInt, bool);
    }
    return localWPathGraphics;
  }

  protected double getXRes()
  {
    if (this.mAttXRes != 0)
      return this.mAttXRes;
    return this.mPrintXRes;
  }

  protected double getYRes()
  {
    if (this.mAttYRes != 0)
      return this.mAttYRes;
    return this.mPrintYRes;
  }

  protected double getPhysicalPrintableX(Paper paramPaper)
  {
    return this.mPrintPhysX;
  }

  protected double getPhysicalPrintableY(Paper paramPaper)
  {
    return this.mPrintPhysY;
  }

  protected double getPhysicalPrintableWidth(Paper paramPaper)
  {
    return this.mPrintWidth;
  }

  protected double getPhysicalPrintableHeight(Paper paramPaper)
  {
    return this.mPrintHeight;
  }

  protected double getPhysicalPageWidth(Paper paramPaper)
  {
    return this.mPageWidth;
  }

  protected double getPhysicalPageHeight(Paper paramPaper)
  {
    return this.mPageHeight;
  }

  protected boolean isCollated()
  {
    return this.userRequestedCollation;
  }

  protected int getCollatedCopies()
  {
    debug_println("driverDoesMultipleCopies=" + this.driverDoesMultipleCopies + " driverDoesCollation=" + this.driverDoesCollation);
    if ((super.isCollated()) && (!(this.driverDoesCollation)))
    {
      this.mAttCollate = 0;
      this.mAttCopies = 1;
      return getCopies();
    }
    return 1;
  }

  protected int getNoncollatedCopies()
  {
    if ((this.driverDoesMultipleCopies) || (super.isCollated()))
      return 1;
    return getCopies();
  }

  private long getPrintDC()
  {
    return HandleRecord.access$100(this.handleRecord);
  }

  private void setPrintDC(long paramLong)
  {
    HandleRecord.access$102(this.handleRecord, paramLong);
  }

  private long getDevMode()
  {
    return HandleRecord.access$200(this.handleRecord);
  }

  private void setDevMode(long paramLong)
  {
    HandleRecord.access$202(this.handleRecord, paramLong);
  }

  private long getDevNames()
  {
    return HandleRecord.access$300(this.handleRecord);
  }

  private void setDevNames(long paramLong)
  {
    HandleRecord.access$302(this.handleRecord, paramLong);
  }

  protected void beginPath()
  {
    beginPath(getPrintDC());
  }

  protected void endPath()
  {
    endPath(getPrintDC());
  }

  protected void closeFigure()
  {
    closeFigure(getPrintDC());
  }

  protected void fillPath()
  {
    fillPath(getPrintDC());
  }

  protected void moveTo(float paramFloat1, float paramFloat2)
  {
    moveTo(getPrintDC(), paramFloat1, paramFloat2);
  }

  protected void lineTo(float paramFloat1, float paramFloat2)
  {
    lineTo(getPrintDC(), paramFloat1, paramFloat2);
  }

  protected void polyBezierTo(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6)
  {
    polyBezierTo(getPrintDC(), paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramFloat5, paramFloat6);
  }

  protected void setPolyFillMode(int paramInt)
  {
    setPolyFillMode(getPrintDC(), paramInt);
  }

  protected void selectSolidBrush(Color paramColor)
  {
    if (!(paramColor.equals(this.mLastColor)))
    {
      this.mLastColor = paramColor;
      float[] arrayOfFloat = paramColor.getRGBColorComponents(null);
      selectSolidBrush(getPrintDC(), (int)(arrayOfFloat[0] * 255.0F), (int)(arrayOfFloat[1] * 255.0F), (int)(arrayOfFloat[2] * 255.0F));
    }
  }

  protected int getPenX()
  {
    return getPenX(getPrintDC());
  }

  protected int getPenY()
  {
    return getPenY(getPrintDC());
  }

  protected void selectClipPath()
  {
    selectClipPath(getPrintDC());
  }

  protected void frameRect(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
  {
    frameRect(getPrintDC(), paramFloat1, paramFloat2, paramFloat3, paramFloat4);
  }

  protected void fillRect(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, Color paramColor)
  {
    float[] arrayOfFloat = paramColor.getRGBColorComponents(null);
    fillRect(getPrintDC(), paramFloat1, paramFloat2, paramFloat3, paramFloat4, (int)(arrayOfFloat[0] * 255.0F), (int)(arrayOfFloat[1] * 255.0F), (int)(arrayOfFloat[2] * 255.0F));
  }

  protected void selectPen(float paramFloat, Color paramColor)
  {
    float[] arrayOfFloat = paramColor.getRGBColorComponents(null);
    selectPen(getPrintDC(), paramFloat, (int)(arrayOfFloat[0] * 255.0F), (int)(arrayOfFloat[1] * 255.0F), (int)(arrayOfFloat[2] * 255.0F));
  }

  protected boolean selectStylePen(int paramInt1, int paramInt2, float paramFloat, Color paramColor)
  {
    long l1;
    long l2;
    float[] arrayOfFloat = paramColor.getRGBColorComponents(null);
    switch (paramInt1)
    {
    case 0:
      l1 = 512L;
      break;
    case 1:
      l1 = 3412047583311298560L;
      break;
    case 2:
    default:
      l1 = 256L;
    }
    switch (paramInt2)
    {
    case 2:
      l2 = 4096L;
      break;
    case 0:
    default:
      l2 = 8192L;
      break;
    case 1:
      l2 = 3412047583311298560L;
    }
    return selectStylePen(getPrintDC(), l1, l2, paramFloat, (int)(arrayOfFloat[0] * 255.0F), (int)(arrayOfFloat[1] * 255.0F), (int)(arrayOfFloat[2] * 255.0F));
  }

  protected boolean setFont(Font paramFont, int paramInt, float paramFloat)
  {
    boolean bool = true;
    if ((!(paramFont.equals(this.mLastFont))) || (paramInt != this.mLastRotation) || (paramFloat != this.mLastAwScale))
    {
      int i = FontManager.getFont2D(paramFont).getStyle();
      int j = paramFont.getStyle() | i;
      bool = setFont(getPrintDC(), paramFont.getFamily(), paramFont.getSize2D(), ((j & 0x1) != 0) ? 1 : false, ((j & 0x2) != 0) ? 1 : false, paramInt, paramFloat);
      if (bool)
      {
        this.mLastFont = paramFont;
        this.mLastRotation = paramInt;
        this.mLastAwScale = paramFloat;
      }
    }
    return bool;
  }

  protected void setTextColor(Color paramColor)
  {
    if (!(paramColor.equals(this.mLastTextColor)))
    {
      this.mLastTextColor = paramColor;
      float[] arrayOfFloat = paramColor.getRGBColorComponents(null);
      setTextColor(getPrintDC(), (int)(arrayOfFloat[0] * 255.0F), (int)(arrayOfFloat[1] * 255.0F), (int)(arrayOfFloat[2] * 255.0F));
    }
  }

  protected void textOut(String paramString, float paramFloat1, float paramFloat2, Font paramFont)
  {
    paramString = removeControlChars(paramString);
    if (paramString.length() == 0)
      return;
    textOut(getPrintDC(), paramString, paramFloat1, paramFloat2, paramFont);
  }

  protected void drawImage3ByteBGR(byte[] paramArrayOfByte, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, float paramFloat8)
  {
    drawDIBImage(getPrintDC(), paramArrayOfByte, paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramFloat5, paramFloat6, paramFloat7, paramFloat8, 24, null);
  }

  protected void drawDIBImage(byte[] paramArrayOfByte, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, float paramFloat8, IndexColorModel paramIndexColorModel)
  {
    int i = 24;
    byte[] arrayOfByte = null;
    if (paramIndexColorModel != null)
    {
      i = paramIndexColorModel.getPixelSize();
      arrayOfByte = new byte[(1 << i) * 4];
      for (int j = 0; j < paramIndexColorModel.getMapSize(); ++j)
      {
        arrayOfByte[(j * 4 + 0)] = (byte)(paramIndexColorModel.getBlue(j) & 0xFF);
        arrayOfByte[(j * 4 + 1)] = (byte)(paramIndexColorModel.getGreen(j) & 0xFF);
        arrayOfByte[(j * 4 + 2)] = (byte)(paramIndexColorModel.getRed(j) & 0xFF);
      }
    }
    drawDIBImage(getPrintDC(), paramArrayOfByte, paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramFloat5, paramFloat6, paramFloat7, paramFloat8, i, arrayOfByte);
  }

  protected void startPage(PageFormat paramPageFormat, Printable paramPrintable, int paramInt)
  {
    invalidateCachedState();
    deviceStartPage(paramPageFormat, paramPrintable, paramInt);
  }

  protected void endPage(PageFormat paramPageFormat, Printable paramPrintable, int paramInt)
  {
    deviceEndPage(paramPageFormat, paramPrintable, paramInt);
  }

  private void invalidateCachedState()
  {
    this.mLastColor = null;
    this.mLastTextColor = null;
    this.mLastFont = null;
  }

  public void setCopies(int paramInt)
  {
    super.setCopies(paramInt);
    setNativeCopies(paramInt);
  }

  public native void setNativeCopies(int paramInt);

  private native boolean jobSetup(Pageable paramPageable, boolean paramBoolean);

  protected native void initPrinter();

  private native void _startDoc(String paramString);

  protected void startDoc()
  {
    _startDoc(this.mDestination);
  }

  protected native void endDoc();

  protected native void abortDoc();

  private static native void deleteDC(long paramLong1, long paramLong2, long paramLong3);

  protected native void deviceStartPage(PageFormat paramPageFormat, Printable paramPrintable, int paramInt);

  protected native void deviceEndPage(PageFormat paramPageFormat, Printable paramPrintable, int paramInt);

  protected native void printBand(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  protected native void beginPath(long paramLong);

  protected native void endPath(long paramLong);

  protected native void closeFigure(long paramLong);

  protected native void fillPath(long paramLong);

  protected native void moveTo(long paramLong, float paramFloat1, float paramFloat2);

  protected native void lineTo(long paramLong, float paramFloat1, float paramFloat2);

  protected native void polyBezierTo(long paramLong, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6);

  protected native void setPolyFillMode(long paramLong, int paramInt);

  protected native void selectSolidBrush(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  protected native int getPenX(long paramLong);

  protected native int getPenY(long paramLong);

  protected native void selectClipPath(long paramLong);

  protected native void frameRect(long paramLong, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4);

  protected native void fillRect(long paramLong, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, int paramInt1, int paramInt2, int paramInt3);

  protected native void selectPen(long paramLong, float paramFloat, int paramInt1, int paramInt2, int paramInt3);

  protected native boolean selectStylePen(long paramLong1, long paramLong2, long paramLong3, float paramFloat, int paramInt1, int paramInt2, int paramInt3);

  protected native boolean setLogicalFont(Font paramFont, int paramInt, float paramFloat);

  protected native boolean setFont(long paramLong, String paramString, float paramFloat1, boolean paramBoolean1, boolean paramBoolean2, int paramInt, float paramFloat2);

  protected native void setTextColor(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  protected native void textOut(long paramLong, String paramString, float paramFloat1, float paramFloat2, Font paramFont);

  private native void drawDIBImage(long paramLong, byte[] paramArrayOfByte1, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, float paramFloat8, int paramInt, byte[] paramArrayOfByte2);

  public final String getPrinterAttrib()
  {
    PrintService localPrintService = getPrintService();
    Object localObject = (localPrintService != null) ? localPrintService.getName() : null;
    return localObject;
  }

  public final int getCopiesAttrib()
  {
    return getCopiesInt();
  }

  public final boolean getMDHAttrib()
  {
    return (this.mAttCollate == 1);
  }

  public final int getOrientAttrib()
  {
    int i = 1;
    OrientationRequested localOrientationRequested = (this.attributes == null) ? null : (OrientationRequested)this.attributes.get(OrientationRequested.class);
    if (localOrientationRequested != null)
      if (localOrientationRequested.equals(OrientationRequested.REVERSE_LANDSCAPE))
        i = 2;
      else if (localOrientationRequested.equals(OrientationRequested.LANDSCAPE))
        i = 0;
    return i;
  }

  public final int getFromPageAttrib()
  {
    if (this.attributes != null)
    {
      PageRanges localPageRanges = (PageRanges)this.attributes.get(PageRanges.class);
      if (localPageRanges != null)
      {
        int[][] arrayOfInt = localPageRanges.getMembers();
        return arrayOfInt[0][0];
      }
    }
    return getMinPageAttrib();
  }

  public final int getToPageAttrib()
  {
    if (this.attributes != null)
    {
      PageRanges localPageRanges = (PageRanges)this.attributes.get(PageRanges.class);
      if (localPageRanges != null)
      {
        int[][] arrayOfInt = localPageRanges.getMembers();
        return arrayOfInt[(arrayOfInt.length - 1)][1];
      }
    }
    return getMaxPageAttrib();
  }

  public final int getMinPageAttrib()
  {
    if (this.attributes != null)
    {
      SunMinMaxPage localSunMinMaxPage = (SunMinMaxPage)this.attributes.get(SunMinMaxPage.class);
      if (localSunMinMaxPage != null)
        return localSunMinMaxPage.getMin();
    }
    return 1;
  }

  public final int getMaxPageAttrib()
  {
    if (this.attributes != null)
    {
      localObject = (SunMinMaxPage)this.attributes.get(SunMinMaxPage.class);
      if (localObject != null)
        return ((SunMinMaxPage)localObject).getMax();
    }
    Object localObject = getPageable();
    if (localObject != null)
    {
      int i = ((Pageable)localObject).getNumberOfPages();
      if (i <= -1)
        i = 9999;
      return ((i == 0) ? 1 : i);
    }
    return 2147483647;
  }

  public final boolean getDestAttrib()
  {
    return (this.mDestination != null);
  }

  public final int getQualityAttrib()
  {
    return this.mAttQuality;
  }

  public final int getColorAttrib()
  {
    return this.mAttChromaticity;
  }

  public final int getSidesAttrib()
  {
    return this.mAttSides;
  }

  public final int[] getWin32MediaAttrib()
  {
    int[] arrayOfInt = { 0, 0 };
    if (this.attributes != null)
    {
      Media localMedia = (Media)this.attributes.get(Media.class);
      if (localMedia instanceof MediaSizeName)
      {
        MediaSizeName localMediaSizeName = (MediaSizeName)localMedia;
        MediaSize localMediaSize = MediaSize.getMediaSizeForName(localMediaSizeName);
        if (localMediaSize != null)
        {
          arrayOfInt[0] = (int)(localMediaSize.getX(25400) * 72.0D);
          arrayOfInt[1] = (int)(localMediaSize.getY(25400) * 72.0D);
        }
      }
    }
    return arrayOfInt;
  }

  public final int getSelectAttrib()
  {
    if (this.attributes != null)
    {
      SunPageSelection localSunPageSelection = (SunPageSelection)this.attributes.get(SunPageSelection.class);
      if (localSunPageSelection == SunPageSelection.RANGE)
        return 2;
      if (localSunPageSelection == SunPageSelection.SELECTION)
        return 1;
      if (localSunPageSelection == SunPageSelection.ALL)
        return 0;
    }
    return 4;
  }

  public final boolean getPrintToFileEnabled()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      FilePermission localFilePermission = new FilePermission("<<ALL FILES>>", "read,write");
      try
      {
        localSecurityManager.checkPermission(localFilePermission);
      }
      catch (SecurityException localSecurityException)
      {
        return false;
      }
    }
    return true;
  }

  public final void setNativeAttributes(int paramInt1, int paramInt2)
  {
    if (this.attributes == null)
      return;
    if ((paramInt1 & 0x20) != 0)
    {
      Destination localDestination = (Destination)this.attributes.get(Destination.class);
      if (localDestination == null)
        try
        {
          this.attributes.add(new Destination(new File("./out.prn").toURI()));
        }
        catch (SecurityException localSecurityException)
        {
          try
          {
            this.attributes.add(new Destination(new URI("file:out.prn")));
          }
          catch (URISyntaxException localURISyntaxException)
          {
          }
        }
    }
    else
    {
      this.attributes.remove(Destination.class);
    }
    if ((paramInt1 & 0x10) != 0)
      this.attributes.add(SheetCollate.COLLATED);
    else
      this.attributes.add(SheetCollate.UNCOLLATED);
    if ((paramInt1 & 0x2) != 0)
      this.attributes.add(SunPageSelection.RANGE);
    else if ((paramInt1 & 0x1) != 0)
      this.attributes.add(SunPageSelection.SELECTION);
    else
      this.attributes.add(SunPageSelection.ALL);
    if ((paramInt2 & 0x1) != 0)
      if ((paramInt2 & 0x4000) != 0)
        this.attributes.add(OrientationRequested.LANDSCAPE);
      else
        this.attributes.add(OrientationRequested.PORTRAIT);
    if ((paramInt2 & 0x800) != 0)
      if ((paramInt2 & 0x200) != 0)
        this.attributes.add(Chromaticity.COLOR);
      else
        this.attributes.add(Chromaticity.MONOCHROME);
    if ((paramInt2 & 0x400) != 0)
      if ((paramInt2 & 0x80) != 0)
        this.attributes.add(PrintQuality.DRAFT);
      else if ((paramInt2 & 0x40) != 0)
        this.attributes.add(PrintQuality.HIGH);
      else
        this.attributes.add(PrintQuality.NORMAL);
    if ((paramInt2 & 0x1000) != 0)
      if ((paramInt2 & 0x10) != 0)
        this.attributes.add(Sides.TWO_SIDED_LONG_EDGE);
      else if ((paramInt2 & 0x20) != 0)
        this.attributes.add(Sides.TWO_SIDED_SHORT_EDGE);
      else
        this.attributes.add(Sides.ONE_SIDED);
  }

  public final void setResolutionDPI(int paramInt1, int paramInt2)
  {
    if (this.attributes != null)
      this.attributes.add(new PrinterResolution(paramInt1, paramInt2, 100));
  }

  public final void setRangeCopiesAttribute(int paramInt1, int paramInt2, boolean paramBoolean, int paramInt3)
  {
    if (this.attributes != null)
    {
      if (paramBoolean)
        this.attributes.add(new PageRanges(paramInt1, paramInt2));
      this.attributes.add(new Copies(paramInt3));
    }
  }

  public void setWin32MediaAttrib(int paramInt1, int paramInt2, int paramInt3)
  {
    MediaSizeName localMediaSizeName = ((Win32PrintService)this.myService).findWin32Media(paramInt1);
    if (localMediaSizeName == null)
      localMediaSizeName = ((Win32PrintService)this.myService).findMatchingMediaSizeNameMM(paramInt2, paramInt3);
    if ((localMediaSizeName != null) && (this.attributes != null))
      this.attributes.add(localMediaSizeName);
  }

  public void setPrinterNameAttrib(String paramString)
  {
    PrintService localPrintService = getPrintService();
    if (paramString == null)
      return;
    if ((localPrintService != null) && (paramString.equals(localPrintService.getName())))
      return;
    PrintService[] arrayOfPrintService = PrinterJob.lookupPrintServices();
    for (int i = 0; i < arrayOfPrintService.length; ++i)
      if (paramString.equals(arrayOfPrintService[i].getName()))
      {
        try
        {
          setPrintService(arrayOfPrintService[i]);
        }
        catch (PrinterException localPrinterException)
        {
        }
        return;
      }
  }

  private static native void initIDs();

  static
  {
    Toolkit.getDefaultToolkit();
    initIDs();
    Win32GraphicsEnvironment.registerJREFontsForPrinting();
  }

  static class HandleRecord
  implements DisposerRecord
  {
    private long mPrintDC;
    private long mPrintHDevMode;
    private long mPrintHDevNames;

    public void dispose()
    {
      WPrinterJob.access$000(this.mPrintDC, this.mPrintHDevMode, this.mPrintHDevNames);
    }
  }

  class PrintToFileErrorDialog extends Dialog
  implements ActionListener
  {
    public PrintToFileErrorDialog(, Frame paramFrame, String paramString1, String paramString2, String paramString3)
    {
      super(paramFrame, paramString1, true);
      init(paramFrame, paramString1, paramString2, paramString3);
    }

    public PrintToFileErrorDialog(, Dialog paramDialog, String paramString1, String paramString2, String paramString3)
    {
      super(paramDialog, paramString1, true);
      init(paramDialog, paramString1, paramString2, paramString3);
    }

    private void init(, String paramString1, String paramString2, String paramString3)
    {
      Panel localPanel = new Panel();
      add("Center", new Label(paramString2));
      Button localButton = new Button(paramString3);
      localButton.addActionListener(this);
      localPanel.add(localButton);
      add("South", localPanel);
      pack();
      Dimension localDimension = getSize();
      if (paramComponent != null)
      {
        Rectangle localRectangle = paramComponent.getBounds();
        setLocation(localRectangle.x + (localRectangle.width - localDimension.width) / 2, localRectangle.y + (localRectangle.height - localDimension.height) / 2);
      }
    }

    public void actionPerformed()
    {
      setVisible(false);
      dispose();
    }
  }
}
package sun.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.awt.print.PrinterJob;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderMalfunctionError;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import javax.print.PrintService;
import javax.print.StreamPrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Sides;
import sun.awt.CharsetString;
import sun.awt.FontConfiguration;
import sun.awt.FontDescriptor;
import sun.awt.PlatformFont;
import sun.awt.SunToolkit;
import sun.font.Font2D;
import sun.font.FontManager;

public class PSPrinterJob extends RasterPrinterJob
{
  protected static final int FILL_EVEN_ODD = 1;
  protected static final int FILL_WINDING = 2;
  private static final int MAX_PSSTR = 65535;
  private static final int RED_MASK = 16711680;
  private static final int GREEN_MASK = 65280;
  private static final int BLUE_MASK = 255;
  private static final int RED_SHIFT = 16;
  private static final int GREEN_SHIFT = 8;
  private static final int BLUE_SHIFT = 0;
  private static final int LOWNIBBLE_MASK = 15;
  private static final int HINIBBLE_MASK = 240;
  private static final int HINIBBLE_SHIFT = 4;
  private static final byte[] hexDigits = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70 };
  private static final int PS_XRES = 300;
  private static final int PS_YRES = 300;
  private static final String ADOBE_PS_STR = "%!PS-Adobe-3.0";
  private static final String EOF_COMMENT = "%%EOF";
  private static final String PAGE_COMMENT = "%%Page: ";
  private static final String READIMAGEPROC = "/imStr 0 def /imageSrc {currentfile /ASCII85Decode filter /RunLengthDecode filter  imStr readstring pop } def";
  private static final String COPIES = "/#copies exch def";
  private static final String PAGE_SAVE = "/pgSave save def";
  private static final String PAGE_RESTORE = "pgSave restore";
  private static final String SHOWPAGE = "showpage";
  private static final String IMAGE_SAVE = "/imSave save def";
  private static final String IMAGE_STR = " string /imStr exch def";
  private static final String IMAGE_RESTORE = "imSave restore";
  private static final String COORD_PREP = " 0 exch translate 1 -1 scale[72 300 div 0 0 72 300 div 0 0]concat";
  private static final String SetFontName = "F";
  private static final String DrawStringName = "S";
  private static final String EVEN_ODD_FILL_STR = "EF";
  private static final String WINDING_FILL_STR = "WF";
  private static final String EVEN_ODD_CLIP_STR = "EC";
  private static final String WINDING_CLIP_STR = "WC";
  private static final String MOVETO_STR = " M";
  private static final String LINETO_STR = " L";
  private static final String CURVETO_STR = " C";
  private static final String GRESTORE_STR = "R";
  private static final String GSAVE_STR = "G";
  private static final String NEWPATH_STR = "N";
  private static final String CLOSEPATH_STR = "P";
  private static final String SETRGBCOLOR_STR = " SC";
  private static final String SETGRAY_STR = " SG";
  private int mDestType;
  private String mDestination = "lp";
  private boolean mNoJobSheet = false;
  private String mOptions;
  private Font mLastFont;
  private Color mLastColor;
  private Shape mLastClip;
  private AffineTransform mLastTransform;
  private EPSPrinter epsPrinter = null;
  FontMetrics mCurMetrics;
  PrintStream mPSStream;
  File spoolFile;
  private String mFillOpStr = "WF";
  private String mClipOpStr = "WC";
  ArrayList mGStateStack = new ArrayList();
  private float mPenX;
  private float mPenY;
  private float mStartPathX;
  private float mStartPathY;
  private static Properties mFontProps = null;

  private static Properties initProps()
  {
    String str1 = System.getProperty("java.home");
    if (str1 != null)
    {
      String str2 = SunToolkit.getStartupLocale().getLanguage();
      try
      {
        File localFile = new File(str1 + File.separator + "lib" + File.separator + "psfontj2d.properties." + str2);
        if (!(localFile.canRead()))
        {
          localFile = new File(str1 + File.separator + "lib" + File.separator + "psfont.properties." + str2);
          if (!(localFile.canRead()))
          {
            localFile = new File(str1 + File.separator + "lib" + File.separator + "psfontj2d.properties");
            if (!(localFile.canRead()))
            {
              localFile = new File(str1 + File.separator + "lib" + File.separator + "psfont.properties");
              if (!(localFile.canRead()))
                return ((Properties)null);
            }
          }
        }
        BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(localFile.getPath()));
        Properties localProperties = new Properties();
        localProperties.load(localBufferedInputStream);
        localBufferedInputStream.close();
        return localProperties;
      }
      catch (Exception localException)
      {
        return ((Properties)null);
      }
    }
    return ((Properties)null);
  }

  public boolean printDialog()
    throws HeadlessException
  {
    if (GraphicsEnvironment.isHeadless())
      throw new HeadlessException();
    if (this.attributes == null)
      this.attributes = new HashPrintRequestAttributeSet();
    this.attributes.add(new Copies(getCopies()));
    this.attributes.add(new JobName(getJobName(), null));
    boolean bool = false;
    DialogTypeSelection localDialogTypeSelection = (DialogTypeSelection)this.attributes.get(DialogTypeSelection.class);
    if (localDialogTypeSelection == DialogTypeSelection.NATIVE)
    {
      this.attributes.remove(DialogTypeSelection.class);
      bool = printDialog(this.attributes);
      this.attributes.add(DialogTypeSelection.NATIVE);
    }
    else
    {
      bool = printDialog(this.attributes);
    }
    if (bool)
    {
      JobName localJobName = (JobName)this.attributes.get(JobName.class);
      if (localJobName != null)
        setJobName(localJobName.getValue());
      Copies localCopies = (Copies)this.attributes.get(Copies.class);
      if (localCopies != null)
        setCopies(localCopies.getValue());
      Destination localDestination = (Destination)this.attributes.get(Destination.class);
      if (localDestination != null)
      {
        try
        {
          this.mDestType = 1;
          this.mDestination = new File(localDestination.getURI()).getPath();
        }
        catch (Exception localException)
        {
          this.mDestination = "out.ps";
        }
      }
      else
      {
        this.mDestType = 0;
        PrintService localPrintService = getPrintService();
        if (localPrintService != null)
          this.mDestination = localPrintService.getName();
      }
    }
    return bool;
  }

  protected void startDoc()
    throws PrinterException
  {
    if (this.epsPrinter == null)
    {
      Object localObject;
      if (getPrintService() instanceof PSStreamPrintService)
      {
        StreamPrintService localStreamPrintService = (StreamPrintService)getPrintService();
        this.mDestType = 2;
        if (localStreamPrintService.isDisposed())
          throw new PrinterException("service is disposed");
        localObject = localStreamPrintService.getOutputStream();
        if (localObject == null)
          throw new PrinterException("Null output stream");
      }
      else
      {
        this.mNoJobSheet = this.noJobSheet;
        if (this.destinationAttr != null)
        {
          this.mDestType = 1;
          this.mDestination = this.destinationAttr;
        }
        if (this.mDestType == 1)
          try
          {
            this.spoolFile = new File(this.mDestination);
            localObject = new FileOutputStream(this.spoolFile);
          }
          catch (IOException localIOException)
          {
            throw new PrinterIOException(localIOException);
          }
        PrinterOpener localPrinterOpener = new PrinterOpener(this, null);
        AccessController.doPrivileged(localPrinterOpener);
        if (localPrinterOpener.pex != null)
          throw localPrinterOpener.pex;
        localObject = localPrinterOpener.result;
      }
      this.mPSStream = new PrintStream(new BufferedOutputStream((OutputStream)localObject));
      this.mPSStream.println("%!PS-Adobe-3.0");
    }
    this.mPSStream.println("%%BeginProlog");
    this.mPSStream.println("/imStr 0 def /imageSrc {currentfile /ASCII85Decode filter /RunLengthDecode filter  imStr readstring pop } def");
    this.mPSStream.println("/BD {bind def} bind def");
    this.mPSStream.println("/D {def} BD");
    this.mPSStream.println("/C {curveto} BD");
    this.mPSStream.println("/L {lineto} BD");
    this.mPSStream.println("/M {moveto} BD");
    this.mPSStream.println("/R {grestore} BD");
    this.mPSStream.println("/G {gsave} BD");
    this.mPSStream.println("/N {newpath} BD");
    this.mPSStream.println("/P {closepath} BD");
    this.mPSStream.println("/EC {eoclip} BD");
    this.mPSStream.println("/WC {clip} BD");
    this.mPSStream.println("/EF {eofill} BD");
    this.mPSStream.println("/WF {fill} BD");
    this.mPSStream.println("/SG {setgray} BD");
    this.mPSStream.println("/SC {setrgbcolor} BD");
    this.mPSStream.println("/ISOF {");
    this.mPSStream.println("     dup findfont dup length 1 add dict begin {");
    this.mPSStream.println("             1 index /FID eq {pop pop} {D} ifelse");
    this.mPSStream.println("     } forall /Encoding ISOLatin1Encoding D");
    this.mPSStream.println("     currentdict end definefont");
    this.mPSStream.println("} BD");
    this.mPSStream.println("/NZ {dup 1 lt {pop 1} if} BD");
    this.mPSStream.println("/S {");
    this.mPSStream.println("     moveto 1 index stringwidth pop NZ sub");
    this.mPSStream.println("     1 index length 1 sub NZ div 0");
    this.mPSStream.println("     3 2 roll ashow newpath} BD");
    this.mPSStream.println("/FL [");
    if (mFontProps == null)
    {
      this.mPSStream.println(" /Helvetica ISOF");
      this.mPSStream.println(" /Helvetica-Bold ISOF");
      this.mPSStream.println(" /Helvetica-Oblique ISOF");
      this.mPSStream.println(" /Helvetica-BoldOblique ISOF");
      this.mPSStream.println(" /Times-Roman ISOF");
      this.mPSStream.println(" /Times-Bold ISOF");
      this.mPSStream.println(" /Times-Italic ISOF");
      this.mPSStream.println(" /Times-BoldItalic ISOF");
      this.mPSStream.println(" /Courier ISOF");
      this.mPSStream.println(" /Courier-Bold ISOF");
      this.mPSStream.println(" /Courier-Oblique ISOF");
      this.mPSStream.println(" /Courier-BoldOblique ISOF");
    }
    else
    {
      int i = Integer.parseInt(mFontProps.getProperty("font.num", "9"));
      for (int j = 0; j < i; ++j)
        this.mPSStream.println("    /" + mFontProps.getProperty(new StringBuilder().append("font.").append(String.valueOf(j)).toString(), "Courier ISOF"));
    }
    this.mPSStream.println("] D");
    this.mPSStream.println("/F {");
    this.mPSStream.println("     FL exch get exch scalefont");
    this.mPSStream.println("     [1 0 0 -1 0 0] makefont setfont} BD");
    this.mPSStream.println("%%EndProlog");
    this.mPSStream.println("%%BeginSetup");
    if (this.epsPrinter == null)
    {
      PageFormat localPageFormat = getPageable().getPageFormat(0);
      double d1 = localPageFormat.getPaper().getHeight();
      double d2 = localPageFormat.getPaper().getWidth();
      this.mPSStream.print("<< /PageSize [" + d2 + " " + d1 + "]");
      PrintService localPrintService = getPrintService();
      Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(this, localPrintService)
      {
        public Object run()
        {
          Class localClass;
          try
          {
            localClass = Class.forName("sun.print.IPPPrintService");
            if (localClass.isInstance(this.val$pservice))
            {
              Method localMethod = localClass.getMethod("isPostscript", (Class[])null);
              return ((Boolean)localMethod.invoke(this.val$pservice, (Object[])null));
            }
          }
          catch (Throwable localThrowable)
          {
          }
          return Boolean.TRUE;
        }
      });
      if (localBoolean.booleanValue())
        this.mPSStream.print(" /DeferredMediaSelection true");
      this.mPSStream.print(" /ImagingBBox null /ManualFeed false");
      this.mPSStream.print((isCollated()) ? " /Collate true" : "");
      this.mPSStream.print(" /NumCopies " + getCopiesInt());
      if (this.sidesAttr != Sides.ONE_SIDED)
        if (this.sidesAttr == Sides.TWO_SIDED_LONG_EDGE)
          this.mPSStream.print(" /Duplex true ");
        else if (this.sidesAttr == Sides.TWO_SIDED_SHORT_EDGE)
          this.mPSStream.print(" /Duplex true /Tumble true ");
      this.mPSStream.println(" >> setpagedevice ");
    }
    this.mPSStream.println("%%EndSetup");
  }

  protected void abortDoc()
  {
    if ((this.mPSStream != null) && (this.mDestType != 2))
      this.mPSStream.close();
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        if ((this.this$0.spoolFile != null) && (this.this$0.spoolFile.exists()))
          this.this$0.spoolFile.delete();
        return null;
      }
    });
  }

  protected void endDoc()
    throws PrinterException
  {
    if (this.mPSStream != null)
    {
      this.mPSStream.println("%%EOF");
      this.mPSStream.flush();
      if (this.mDestType != 2)
        this.mPSStream.close();
    }
    if (this.mDestType == 0)
    {
      if (getPrintService() != null)
        this.mDestination = getPrintService().getName();
      PrinterSpooler localPrinterSpooler = new PrinterSpooler(this, null);
      AccessController.doPrivileged(localPrinterSpooler);
      if (localPrinterSpooler.pex != null)
        throw localPrinterSpooler.pex;
    }
  }

  protected void startPage(PageFormat paramPageFormat, Printable paramPrintable, int paramInt)
    throws PrinterException
  {
    double d1 = paramPageFormat.getPaper().getHeight();
    double d2 = paramPageFormat.getPaper().getWidth();
    int i = paramInt + 1;
    this.mGStateStack = new ArrayList();
    this.mGStateStack.add(new GState(this));
    this.mPSStream.println("%%Page: " + i + " " + i);
    if (paramInt > 0)
    {
      PageFormat localPageFormat = getPageable().getPageFormat(paramInt - 1);
      double d3 = localPageFormat.getPaper().getHeight();
      double d4 = localPageFormat.getPaper().getWidth();
      if ((d1 != d3) || (d2 != d4))
      {
        this.mPSStream.print("<< /PageSize [" + d2 + " " + d1 + "]");
        PrintService localPrintService = getPrintService();
        Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(this, localPrintService)
        {
          public Object run()
          {
            Class localClass;
            try
            {
              localClass = Class.forName("sun.print.IPPPrintService");
              if (localClass.isInstance(this.val$pservice))
              {
                Method localMethod = localClass.getMethod("isPostscript", (Class[])null);
                return ((Boolean)localMethod.invoke(this.val$pservice, (Object[])null));
              }
            }
            catch (Throwable localThrowable)
            {
            }
            return Boolean.TRUE;
          }
        });
        if (localBoolean.booleanValue())
          this.mPSStream.print(" /DeferredMediaSelection true");
        this.mPSStream.println(" >> setpagedevice");
      }
    }
    this.mPSStream.println("/pgSave save def");
    this.mPSStream.println(d1 + " 0 exch translate 1 -1 scale[72 300 div 0 0 72 300 div 0 0]concat");
  }

  protected void endPage(PageFormat paramPageFormat, Printable paramPrintable, int paramInt)
    throws PrinterException
  {
    this.mPSStream.println("pgSave restore");
    this.mPSStream.println("showpage");
  }

  protected void drawImageBGR(byte[] paramArrayOfByte, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, float paramFloat8, int paramInt1, int paramInt2)
  {
    setTransform(new AffineTransform());
    prepDrawing();
    int i = (int)paramFloat7;
    int j = (int)paramFloat8;
    this.mPSStream.println("/imSave save def");
    int k = 3 * i;
    while (k > 65535)
      k /= 2;
    this.mPSStream.println(k + " string /imStr exch def");
    this.mPSStream.println("[" + paramFloat3 + " 0 " + "0 " + paramFloat4 + " " + paramFloat1 + " " + paramFloat2 + "]concat");
    this.mPSStream.println(i + " " + j + " " + 8 + "[" + i + " 0 " + "0 " + j + " 0 " + 0 + "]" + "/imageSrc load false 3 colorimage");
    int l = 0;
    byte[] arrayOfByte1 = new byte[i * 3];
    try
    {
      l = (int)paramFloat6 * paramInt1;
      for (int i1 = 0; i1 < j; ++i1)
      {
        l += (int)paramFloat5;
        l = swapBGRtoRGB(paramArrayOfByte, l, arrayOfByte1);
        byte[] arrayOfByte2 = rlEncode(arrayOfByte1);
        byte[] arrayOfByte3 = ascii85Encode(arrayOfByte2);
        this.mPSStream.write(arrayOfByte3);
        this.mPSStream.println("");
      }
    }
    catch (IOException localIOException)
    {
    }
    this.mPSStream.println("imSave restore");
  }

  protected void printBand(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws PrinterException
  {
    this.mPSStream.println("/imSave save def");
    int i = 3 * paramInt3;
    while (i > 65535)
      i /= 2;
    this.mPSStream.println(i + " string /imStr exch def");
    this.mPSStream.println("[" + paramInt3 + " 0 " + "0 " + paramInt4 + " " + paramInt1 + " " + paramInt2 + "]concat");
    this.mPSStream.println(paramInt3 + " " + paramInt4 + " " + 8 + "[" + paramInt3 + " 0 " + "0 " + (-paramInt4) + " 0 " + paramInt4 + "]" + "/imageSrc load false 3 colorimage");
    int j = 0;
    byte[] arrayOfByte1 = new byte[paramInt3 * 3];
    try
    {
      for (int k = 0; k < paramInt4; ++k)
      {
        j = swapBGRtoRGB(paramArrayOfByte, j, arrayOfByte1);
        byte[] arrayOfByte2 = rlEncode(arrayOfByte1);
        byte[] arrayOfByte3 = ascii85Encode(arrayOfByte2);
        this.mPSStream.write(arrayOfByte3);
        this.mPSStream.println("");
      }
    }
    catch (IOException localIOException)
    {
      throw new PrinterIOException(localIOException);
    }
    this.mPSStream.println("imSave restore");
  }

  protected Graphics2D createPathGraphics(PeekGraphics paramPeekGraphics, PrinterJob paramPrinterJob, Printable paramPrintable, PageFormat paramPageFormat, int paramInt)
  {
    PSPathGraphics localPSPathGraphics;
    PeekMetrics localPeekMetrics = paramPeekGraphics.getMetrics();
    if ((!(forcePDL)) && (((forceRaster == true) || (localPeekMetrics.hasNonSolidColors()) || (localPeekMetrics.hasCompositing()))))
    {
      localPSPathGraphics = null;
    }
    else
    {
      BufferedImage localBufferedImage = new BufferedImage(8, 8, 1);
      Graphics2D localGraphics2D = localBufferedImage.createGraphics();
      boolean bool = !(paramPeekGraphics.getAWTDrawingOnly());
      localPSPathGraphics = new PSPathGraphics(localGraphics2D, paramPrinterJob, paramPrintable, paramPageFormat, paramInt, bool);
    }
    return localPSPathGraphics;
  }

  protected void selectClipPath()
  {
    this.mPSStream.println(this.mClipOpStr);
  }

  protected void setClip(Shape paramShape)
  {
    this.mLastClip = paramShape;
  }

  protected void setTransform(AffineTransform paramAffineTransform)
  {
    this.mLastTransform = paramAffineTransform;
  }

  protected boolean setFont(Font paramFont)
  {
    this.mLastFont = paramFont;
    return true;
  }

  private int[] getPSFontIndexArray(Font paramFont, CharsetString[] paramArrayOfCharsetString)
  {
    int[] arrayOfInt = null;
    if (mFontProps != null)
      arrayOfInt = new int[paramArrayOfCharsetString.length];
    for (int i = 0; (i < paramArrayOfCharsetString.length) && (arrayOfInt != null); ++i)
    {
      CharsetString localCharsetString = paramArrayOfCharsetString[i];
      CharsetEncoder localCharsetEncoder = localCharsetString.fontDescriptor.encoder;
      String str1 = localCharsetString.fontDescriptor.getFontCharsetName();
      if ("Symbol".equals(str1))
        str1 = "symbol";
      else if (("WingDings".equals(str1)) || ("X11Dingbats".equals(str1)))
        str1 = "dingbats";
      else
        str1 = makeCharsetName(str1, localCharsetString.charsetChars);
      int j = paramFont.getStyle() | FontManager.getFont2D(paramFont).getStyle();
      String str2 = FontConfiguration.getStyleString(j);
      String str3 = paramFont.getFamily().toLowerCase(Locale.ENGLISH);
      str3 = str3.replace(' ', '_');
      String str4 = mFontProps.getProperty(str3, "");
      String str5 = mFontProps.getProperty(str4 + "." + str1 + "." + str2, null);
      if (str5 != null)
        try
        {
          arrayOfInt[i] = Integer.parseInt(mFontProps.getProperty(str5));
        }
        catch (NumberFormatException localNumberFormatException)
        {
          arrayOfInt = null;
        }
      else
        arrayOfInt = null;
    }
    return arrayOfInt;
  }

  private static String escapeParens(String paramString)
  {
    if ((paramString.indexOf(40) == -1) && (paramString.indexOf(41) == -1))
      return paramString;
    int i = 0;
    for (int j = 0; (j = paramString.indexOf(40, j)) != -1; ++j)
      ++i;
    for (j = 0; (j = paramString.indexOf(41, j)) != -1; ++j)
      ++i;
    char[] arrayOfChar1 = paramString.toCharArray();
    char[] arrayOfChar2 = new char[arrayOfChar1.length + i];
    j = 0;
    for (int k = 0; k < arrayOfChar1.length; ++k)
    {
      if ((arrayOfChar1[k] == '(') || (arrayOfChar1[k] == ')'))
        arrayOfChar2[(j++)] = '\\';
      arrayOfChar2[(j++)] = arrayOfChar1[k];
    }
    return new String(arrayOfChar2);
  }

  protected int platformFontCount(Font paramFont, String paramString)
  {
    if (mFontProps == null)
      return 0;
    CharsetString[] arrayOfCharsetString = ((PlatformFont)(PlatformFont)paramFont.getPeer()).makeMultiCharsetString(paramString, false);
    if (arrayOfCharsetString == null)
      return 0;
    int[] arrayOfInt = getPSFontIndexArray(paramFont, arrayOfCharsetString);
    return ((arrayOfInt == null) ? 0 : arrayOfInt.length);
  }

  protected boolean textOut(Graphics paramGraphics, String paramString, float paramFloat1, float paramFloat2, Font paramFont, FontRenderContext paramFontRenderContext, float paramFloat3)
  {
    int i = 1;
    if (mFontProps == null)
      return false;
    prepDrawing();
    paramString = removeControlChars(paramString);
    if (paramString.length() == 0)
      return true;
    CharsetString[] arrayOfCharsetString = ((PlatformFont)(PlatformFont)paramFont.getPeer()).makeMultiCharsetString(paramString, false);
    if (arrayOfCharsetString == null)
      return false;
    int[] arrayOfInt = getPSFontIndexArray(paramFont, arrayOfCharsetString);
    if (arrayOfInt != null)
      for (int j = 0; j < arrayOfCharsetString.length; ++j)
      {
        float f;
        CharsetString localCharsetString = arrayOfCharsetString[j];
        CharsetEncoder localCharsetEncoder = localCharsetString.fontDescriptor.encoder;
        StringBuffer localStringBuffer = new StringBuffer();
        byte[] arrayOfByte = new byte[localCharsetString.length * 2];
        int k = 0;
        try
        {
          ByteBuffer localByteBuffer = ByteBuffer.wrap(arrayOfByte);
          localCharsetEncoder.encode(CharBuffer.wrap(localCharsetString.charsetChars, localCharsetString.offset, localCharsetString.length), localByteBuffer, true);
          localByteBuffer.flip();
          k = localByteBuffer.limit();
        }
        catch (IllegalStateException localIllegalStateException)
        {
          break label462:
        }
        catch (CoderMalfunctionError localCoderMalfunctionError)
        {
        }
        break label462:
        if ((arrayOfCharsetString.length == 1) && (paramFloat3 != 0F))
        {
          f = paramFloat3;
        }
        else
        {
          Rectangle2D localRectangle2D = paramFont.getStringBounds(localCharsetString.charsetChars, localCharsetString.offset, localCharsetString.offset + localCharsetString.length, paramFontRenderContext);
          f = (float)localRectangle2D.getWidth();
        }
        if (f == 0F)
          return i;
        localStringBuffer.append('<');
        for (int l = 0; l < k; ++l)
        {
          int i1 = arrayOfByte[l];
          String str = Integer.toHexString(i1);
          int i2 = str.length();
          if (i2 > 2)
            str = str.substring(i2 - 2, i2);
          else if (i2 == 1)
            str = "0" + str;
          else if (i2 == 0)
            str = "00";
          localStringBuffer.append(str);
        }
        localStringBuffer.append('>');
        getGState().emitPSFont(arrayOfInt[j], paramFont.getSize2D());
        this.mPSStream.println(localStringBuffer.toString() + " " + f + " " + paramFloat1 + " " + paramFloat2 + " " + "S");
        label462: paramFloat1 += f;
      }
    else
      i = 0;
    return i;
  }

  protected void setFillMode(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      this.mFillOpStr = "EF";
      this.mClipOpStr = "EC";
      break;
    case 2:
      this.mFillOpStr = "WF";
      this.mClipOpStr = "WC";
      break;
    default:
      throw new IllegalArgumentException();
    }
  }

  protected void setColor(Color paramColor)
  {
    this.mLastColor = paramColor;
  }

  protected void fillPath()
  {
    this.mPSStream.println(this.mFillOpStr);
  }

  protected void beginPath()
  {
    prepDrawing();
    this.mPSStream.println("N");
    this.mPenX = 0F;
    this.mPenY = 0F;
  }

  protected void closeSubpath()
  {
    this.mPSStream.println("P");
    this.mPenX = this.mStartPathX;
    this.mPenY = this.mStartPathY;
  }

  protected void moveTo(float paramFloat1, float paramFloat2)
  {
    this.mPSStream.println(trunc(paramFloat1) + " " + trunc(paramFloat2) + " M");
    this.mStartPathX = paramFloat1;
    this.mStartPathY = paramFloat2;
    this.mPenX = paramFloat1;
    this.mPenY = paramFloat2;
  }

  protected void lineTo(float paramFloat1, float paramFloat2)
  {
    this.mPSStream.println(trunc(paramFloat1) + " " + trunc(paramFloat2) + " L");
    this.mPenX = paramFloat1;
    this.mPenY = paramFloat2;
  }

  protected void bezierTo(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6)
  {
    this.mPSStream.println(trunc(paramFloat1) + " " + trunc(paramFloat2) + " " + trunc(paramFloat3) + " " + trunc(paramFloat4) + " " + trunc(paramFloat5) + " " + trunc(paramFloat6) + " C");
    this.mPenX = paramFloat5;
    this.mPenY = paramFloat6;
  }

  String trunc(float paramFloat)
  {
    float f = Math.abs(paramFloat);
    if ((f >= 1F) && (f <= 1000.0F))
      paramFloat = Math.round(paramFloat * 1000.0F) / 1000.0F;
    return Float.toString(paramFloat);
  }

  protected float getPenX()
  {
    return this.mPenX;
  }

  protected float getPenY()
  {
    return this.mPenY;
  }

  protected double getXRes()
  {
    return 300.0D;
  }

  protected double getYRes()
  {
    return 300.0D;
  }

  protected double getPhysicalPrintableX(Paper paramPaper)
  {
    return 0D;
  }

  protected double getPhysicalPrintableY(Paper paramPaper)
  {
    return 0D;
  }

  protected double getPhysicalPrintableWidth(Paper paramPaper)
  {
    return paramPaper.getImageableWidth();
  }

  protected double getPhysicalPrintableHeight(Paper paramPaper)
  {
    return paramPaper.getImageableHeight();
  }

  protected double getPhysicalPageWidth(Paper paramPaper)
  {
    return paramPaper.getWidth();
  }

  protected double getPhysicalPageHeight(Paper paramPaper)
  {
    return paramPaper.getHeight();
  }

  protected int getNoncollatedCopies()
  {
    return 1;
  }

  protected int getCollatedCopies()
  {
    return 1;
  }

  private String[] printExecCmd(String paramString1, String paramString2, boolean paramBoolean, String paramString3, int paramInt, String paramString4)
  {
    String[] arrayOfString;
    int i = 1;
    int j = 2;
    int k = 4;
    int l = 8;
    int i1 = 16;
    int i2 = 0;
    int i3 = 2;
    int i4 = 0;
    if ((paramString1 != null) && (!(paramString1.equals(""))) && (!(paramString1.equals("lp"))))
    {
      i2 |= i;
      ++i3;
    }
    if ((paramString2 != null) && (!(paramString2.equals(""))))
    {
      i2 |= j;
      ++i3;
    }
    if ((paramString3 != null) && (!(paramString3.equals(""))))
    {
      i2 |= k;
      ++i3;
    }
    if (paramInt > 1)
    {
      i2 |= l;
      ++i3;
    }
    if (paramBoolean)
    {
      i2 |= i1;
      ++i3;
    }
    if (System.getProperty("os.name").equals("Linux"))
    {
      arrayOfString = new String[i3];
      arrayOfString[(i4++)] = "/usr/bin/lpr";
      if ((i2 & i) != 0)
        arrayOfString[(i4++)] = new String("-P" + paramString1);
      if ((i2 & k) != 0)
        arrayOfString[(i4++)] = new String("-J" + paramString3);
      if ((i2 & l) != 0)
        arrayOfString[(i4++)] = new String("-#" + new Integer(paramInt).toString());
      if ((i2 & i1) != 0)
        arrayOfString[(i4++)] = new String("-h");
      if ((i2 & j) != 0)
        arrayOfString[(i4++)] = new String(paramString2);
    }
    else
    {
      arrayOfString = new String[++i3];
      arrayOfString[(i4++)] = "/usr/bin/lp";
      arrayOfString[(i4++)] = "-c";
      if ((i2 & i) != 0)
        arrayOfString[(i4++)] = new String("-d" + paramString1);
      if ((i2 & k) != 0)
        arrayOfString[(i4++)] = new String("-t" + paramString3);
      if ((i2 & l) != 0)
        arrayOfString[(i4++)] = new String("-n" + new Integer(paramInt).toString());
      if ((i2 & i1) != 0)
        arrayOfString[(i4++)] = new String("-o nobanner");
      if ((i2 & j) != 0)
        arrayOfString[(i4++)] = new String("-o" + paramString2);
    }
    arrayOfString[(i4++)] = paramString4;
    return arrayOfString;
  }

  private static int swapBGRtoRGB(byte[] paramArrayOfByte1, int paramInt, byte[] paramArrayOfByte2)
  {
    int i = 0;
    while ((paramInt < paramArrayOfByte1.length - 2) && (i < paramArrayOfByte2.length - 2))
    {
      paramArrayOfByte2[(i++)] = paramArrayOfByte1[(paramInt + 2)];
      paramArrayOfByte2[(i++)] = paramArrayOfByte1[(paramInt + 1)];
      paramArrayOfByte2[(i++)] = paramArrayOfByte1[(paramInt + 0)];
      paramInt += 3;
    }
    return paramInt;
  }

  private String makeCharsetName(String paramString, char[] paramArrayOfChar)
  {
    int i;
    if ((paramString.equals("Cp1252")) || (paramString.equals("ISO8859_1")))
      return "latin1";
    if (paramString.equals("UTF8"))
    {
      for (i = 0; i < paramArrayOfChar.length; ++i)
        if (paramArrayOfChar[i] > 255)
          return paramString.toLowerCase();
      return "latin1";
    }
    if (paramString.startsWith("ISO8859"))
    {
      for (i = 0; i < paramArrayOfChar.length; ++i)
        if (paramArrayOfChar[i] > '')
          return paramString.toLowerCase();
      return "latin1";
    }
    return paramString.toLowerCase();
  }

  private void prepDrawing()
  {
    while ((!(isOuterGState())) && (((!(getGState().canSetClip(this.mLastClip))) || (!(getGState().mTransform.equals(this.mLastTransform))))))
      grestore();
    getGState().emitPSColor(this.mLastColor);
    if (isOuterGState())
    {
      gsave();
      getGState().emitTransform(this.mLastTransform);
      getGState().emitPSClip(this.mLastClip);
    }
  }

  private GState getGState()
  {
    int i = this.mGStateStack.size();
    return ((GState)this.mGStateStack.get(i - 1));
  }

  private void gsave()
  {
    GState localGState = getGState();
    this.mGStateStack.add(new GState(this, localGState));
    this.mPSStream.println("G");
  }

  private void grestore()
  {
    int i = this.mGStateStack.size();
    this.mGStateStack.remove(i - 1);
    this.mPSStream.println("R");
  }

  private boolean isOuterGState()
  {
    return (this.mGStateStack.size() == 1);
  }

  void convertToPSPath(PathIterator paramPathIterator)
  {
    int j;
    float[] arrayOfFloat = new float[6];
    if (paramPathIterator.getWindingRule() == 0)
      j = 1;
    else
      j = 2;
    beginPath();
    setFillMode(j);
    while (!(paramPathIterator.isDone()))
    {
      int i = paramPathIterator.currentSegment(arrayOfFloat);
      switch (i)
      {
      case 0:
        moveTo(arrayOfFloat[0], arrayOfFloat[1]);
        break;
      case 1:
        lineTo(arrayOfFloat[0], arrayOfFloat[1]);
        break;
      case 2:
        float f1 = getPenX();
        float f2 = getPenY();
        float f3 = f1 + (arrayOfFloat[0] - f1) * 2F / 3.0F;
        float f4 = f2 + (arrayOfFloat[1] - f2) * 2F / 3.0F;
        float f5 = arrayOfFloat[2] - (arrayOfFloat[2] - arrayOfFloat[0]) * 2F / 3.0F;
        float f6 = arrayOfFloat[3] - (arrayOfFloat[3] - arrayOfFloat[1]) * 2F / 3.0F;
        bezierTo(f3, f4, f5, f6, arrayOfFloat[2], arrayOfFloat[3]);
        break;
      case 3:
        bezierTo(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2], arrayOfFloat[3], arrayOfFloat[4], arrayOfFloat[5]);
        break;
      case 4:
        closeSubpath();
      }
      paramPathIterator.next();
    }
  }

  protected void deviceFill(PathIterator paramPathIterator, Color paramColor, AffineTransform paramAffineTransform, Shape paramShape)
  {
    setTransform(paramAffineTransform);
    setClip(paramShape);
    setColor(paramColor);
    convertToPSPath(paramPathIterator);
    this.mPSStream.println("G");
    selectClipPath();
    fillPath();
    this.mPSStream.println("R N");
  }

  private byte[] rlEncode(byte[] paramArrayOfByte)
  {
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    byte[] arrayOfByte1 = new byte[paramArrayOfByte.length * 2 + 2];
    while (true)
    {
      while (true)
      {
        if (i >= paramArrayOfByte.length)
          break label193;
        if (l == 0)
        {
          k = i++;
          l = 1;
        }
        while ((l < 128) && (i < paramArrayOfByte.length) && (paramArrayOfByte[i] == paramArrayOfByte[k]))
        {
          ++l;
          ++i;
        }
        if (l <= 1)
          break;
        arrayOfByte1[(j++)] = (byte)(257 - l);
        arrayOfByte1[(j++)] = paramArrayOfByte[k];
        l = 0;
      }
      while ((l < 128) && (i < paramArrayOfByte.length) && (paramArrayOfByte[i] != paramArrayOfByte[(i - 1)]))
      {
        ++l;
        ++i;
      }
      arrayOfByte1[(j++)] = (byte)(l - 1);
      for (int i1 = k; i1 < k + l; ++i1)
        arrayOfByte1[(j++)] = paramArrayOfByte[i1];
      l = 0;
    }
    label193: arrayOfByte1[(j++)] = -128;
    byte[] arrayOfByte2 = new byte[j];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, j);
    return arrayOfByte2;
  }

  private byte[] ascii85Encode(byte[] paramArrayOfByte)
  {
    long l5;
    long l6;
    byte[] arrayOfByte1 = new byte[(paramArrayOfByte.length + 4) * 5 / 4 + 2];
    long l1 = 85L;
    long l2 = l1 * l1;
    long l3 = l1 * l2;
    long l4 = l1 * l3;
    int i = 33;
    int j = 0;
    int k = 0;
    while (true)
    {
      while (true)
      {
        if (j + 3 >= paramArrayOfByte.length)
          break label254;
        l5 = ((paramArrayOfByte[(j++)] & 0xFF) << 24) + ((paramArrayOfByte[(j++)] & 0xFF) << 16) + ((paramArrayOfByte[(j++)] & 0xFF) << 8) + (paramArrayOfByte[(j++)] & 0xFF);
        if (l5 != 3412047445872345088L)
          break;
        arrayOfByte1[(k++)] = 122;
      }
      l6 = l5;
      arrayOfByte1[(k++)] = (byte)(int)(l6 / l4 + i);
      l6 %= l4;
      arrayOfByte1[(k++)] = (byte)(int)(l6 / l3 + i);
      l6 %= l3;
      arrayOfByte1[(k++)] = (byte)(int)(l6 / l2 + i);
      l6 %= l2;
      arrayOfByte1[(k++)] = (byte)(int)(l6 / l1 + i);
      l6 %= l1;
      arrayOfByte1[(k++)] = (byte)(int)(l6 + i);
    }
    if (j < paramArrayOfByte.length)
    {
      label254: int l = paramArrayOfByte.length - j;
      l5 = 3412047531771691008L;
      while (j < paramArrayOfByte.length)
        l5 = (l5 << 8) + (paramArrayOfByte[(j++)] & 0xFF);
      int i1 = 4 - l;
      while (i1-- > 0)
        l5 <<= 8;
      byte[] arrayOfByte3 = new byte[5];
      l6 = l5;
      arrayOfByte3[0] = (byte)(int)(l6 / l4 + i);
      l6 %= l4;
      arrayOfByte3[1] = (byte)(int)(l6 / l3 + i);
      l6 %= l3;
      arrayOfByte3[2] = (byte)(int)(l6 / l2 + i);
      l6 %= l2;
      arrayOfByte3[3] = (byte)(int)(l6 / l1 + i);
      l6 %= l1;
      arrayOfByte3[4] = (byte)(int)(l6 + i);
      for (int i2 = 0; i2 < l + 1; ++i2)
        arrayOfByte1[(k++)] = arrayOfByte3[i2];
    }
    arrayOfByte1[(k++)] = 126;
    arrayOfByte1[(k++)] = 62;
    byte[] arrayOfByte2 = new byte[k];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, k);
    return arrayOfByte2;
  }

  static
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        PSPrinterJob.access$002(PSPrinterJob.access$100());
        return null;
      }
    });
  }

  public static class EPSPrinter
  implements Pageable
  {
    private PageFormat pf;
    private PSPrinterJob job;
    private int llx;
    private int lly;
    private int urx;
    private int ury;
    private Printable printable;
    private PrintStream stream;
    private String epsTitle;

    public EPSPrinter(Printable paramPrintable, String paramString, PrintStream paramPrintStream, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      this.printable = paramPrintable;
      this.epsTitle = paramString;
      this.stream = paramPrintStream;
      this.llx = paramInt1;
      this.lly = paramInt2;
      this.urx = (this.llx + paramInt3);
      this.ury = (this.lly + paramInt4);
      Paper localPaper = new Paper();
      localPaper.setSize(paramInt3, paramInt4);
      localPaper.setImageableArea(0D, 0D, paramInt3, paramInt4);
      this.pf = new PageFormat();
      this.pf.setPaper(localPaper);
    }

    public void print()
      throws PrinterException
    {
      this.stream.println("%!PS-Adobe-3.0 EPSF-3.0");
      this.stream.println("%%BoundingBox: " + this.llx + " " + this.lly + " " + this.urx + " " + this.ury);
      this.stream.println("%%Title: " + this.epsTitle);
      this.stream.println("%%Creator: Java Printing");
      this.stream.println("%%CreationDate: " + new Date());
      this.stream.println("%%EndComments");
      this.stream.println("/pluginSave save def");
      this.stream.println("mark");
      this.job = new PSPrinterJob();
      PSPrinterJob.access$1002(this.job, this);
      this.job.mPSStream = this.stream;
      PSPrinterJob.access$1102(this.job, 2);
      this.job.startDoc();
      try
      {
        this.job.printPage(this, 0);
      }
      catch (Throwable localThrowable)
      {
      }
      finally
      {
        this.stream.println("cleartomark");
        this.stream.println("pluginSave restore");
        this.job.endDoc();
      }
      this.stream.flush();
    }

    public int getNumberOfPages()
    {
      return 1;
    }

    public PageFormat getPageFormat(int paramInt)
    {
      if (paramInt > 0)
        throw new IndexOutOfBoundsException("pgIndex");
      return this.pf;
    }

    public Printable getPrintable(int paramInt)
    {
      if (paramInt > 0)
        throw new IndexOutOfBoundsException("pgIndex");
      return this.printable;
    }
  }

  private class GState
  {
    Color mColor;
    Shape mClip;
    Font mFont;
    AffineTransform mTransform;

    GState()
    {
      this.mColor = Color.black;
      this.mClip = null;
      this.mFont = null;
      this.mTransform = new AffineTransform();
    }

    GState(, GState paramGState)
    {
      this.mColor = paramGState.mColor;
      this.mClip = paramGState.mClip;
      this.mFont = paramGState.mFont;
      this.mTransform = paramGState.mTransform;
    }

    boolean canSetClip()
    {
      return ((this.mClip == null) || (this.mClip.equals(paramShape)));
    }

    void emitPSClip()
    {
      if ((paramShape != null) && (((this.mClip == null) || (!(this.mClip.equals(paramShape))))))
      {
        String str1 = PSPrinterJob.access$800(this.this$0);
        String str2 = PSPrinterJob.access$900(this.this$0);
        this.this$0.convertToPSPath(paramShape.getPathIterator(new AffineTransform()));
        this.this$0.selectClipPath();
        this.mClip = paramShape;
        PSPrinterJob.access$902(this.this$0, str1);
        PSPrinterJob.access$802(this.this$0, str1);
      }
    }

    void emitTransform()
    {
      if ((paramAffineTransform != null) && (!(paramAffineTransform.equals(this.mTransform))))
      {
        double[] arrayOfDouble = new double[6];
        paramAffineTransform.getMatrix(arrayOfDouble);
        this.this$0.mPSStream.println("[" + (float)arrayOfDouble[0] + " " + (float)arrayOfDouble[1] + " " + (float)arrayOfDouble[2] + " " + (float)arrayOfDouble[3] + " " + (float)arrayOfDouble[4] + " " + (float)arrayOfDouble[5] + "] concat");
        this.mTransform = paramAffineTransform;
      }
    }

    void emitPSColor()
    {
      if ((paramColor != null) && (!(paramColor.equals(this.mColor))))
      {
        float[] arrayOfFloat = paramColor.getRGBColorComponents(null);
        if ((arrayOfFloat[0] == arrayOfFloat[1]) && (arrayOfFloat[1] == arrayOfFloat[2]))
          this.this$0.mPSStream.println(arrayOfFloat[0] + " SG");
        else
          this.this$0.mPSStream.println(arrayOfFloat[0] + " " + arrayOfFloat[1] + " " + arrayOfFloat[2] + " " + " SC");
        this.mColor = paramColor;
      }
    }

    void emitPSFont(, float paramFloat)
    {
      this.this$0.mPSStream.println(paramFloat + " " + paramInt + " " + "F");
    }
  }

  public static class PluginPrinter
  implements Printable
  {
    private PSPrinterJob.EPSPrinter epsPrinter;
    private Component applet;
    private PrintStream stream;
    private String epsTitle;
    private int bx;
    private int by;
    private int bw;
    private int bh;
    private int width;
    private int height;

    public PluginPrinter(Component paramComponent, PrintStream paramPrintStream, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      this.applet = paramComponent;
      this.epsTitle = "Java Plugin Applet";
      this.stream = paramPrintStream;
      this.bx = paramInt1;
      this.by = paramInt2;
      this.bw = paramInt3;
      this.bh = paramInt4;
      this.width = paramComponent.size().width;
      this.height = paramComponent.size().height;
      this.epsPrinter = new PSPrinterJob.EPSPrinter(this, this.epsTitle, paramPrintStream, 0, 0, this.width, this.height);
    }

    public void printPluginPSHeader()
    {
      this.stream.println("%%BeginDocument: JavaPluginApplet");
    }

    public void printPluginApplet()
    {
      try
      {
        this.epsPrinter.print();
      }
      catch (PrinterException localPrinterException)
      {
      }
    }

    public void printPluginPSTrailer()
    {
      this.stream.println("%%EndDocument: JavaPluginApplet");
      this.stream.flush();
    }

    public void printAll()
    {
      printPluginPSHeader();
      printPluginApplet();
      printPluginPSTrailer();
    }

    public int print(Graphics paramGraphics, PageFormat paramPageFormat, int paramInt)
    {
      if (paramInt > 0)
        return 1;
      this.applet.printAll(paramGraphics);
      return 0;
    }
  }

  private class PrinterOpener
  implements PrivilegedAction
  {
    PrinterException pex;
    OutputStream result;

    public Object run()
    {
      try
      {
        this.this$0.spoolFile = File.createTempFile("javaprint", ".ps", null);
        this.this$0.spoolFile.deleteOnExit();
        this.result = new FileOutputStream(this.this$0.spoolFile);
        return this.result;
      }
      catch (IOException localIOException)
      {
        this.pex = new PrinterIOException(localIOException);
      }
      return null;
    }
  }

  private class PrinterSpooler
  implements PrivilegedAction
  {
    PrinterException pex;

    public Object run()
    {
      try
      {
        if ((this.this$0.spoolFile == null) || (!(this.this$0.spoolFile.exists())))
        {
          this.pex = new PrinterException("No spool file");
          return null;
        }
        String str = this.this$0.spoolFile.getAbsolutePath();
        String[] arrayOfString = PSPrinterJob.access$600(this.this$0, PSPrinterJob.access$300(this.this$0), PSPrinterJob.access$400(this.this$0), PSPrinterJob.access$500(this.this$0), this.this$0.getJobNameInt(), 1, str);
        Process localProcess = Runtime.getRuntime().exec(arrayOfString);
        localProcess.waitFor();
        this.this$0.spoolFile.delete();
      }
      catch (IOException localIOException)
      {
        this.pex = new PrinterIOException(localIOException);
      }
      catch (InterruptedException localInterruptedException)
      {
        this.pex = new PrinterException(localInterruptedException.toString());
      }
      return null;
    }
  }
}
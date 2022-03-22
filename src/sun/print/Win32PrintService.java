package sun.print;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.print.DocFlavor;
import javax.print.DocFlavor.BYTE_ARRAY;
import javax.print.DocFlavor.INPUT_STREAM;
import javax.print.DocFlavor.SERVICE_FORMATTED;
import javax.print.DocFlavor.URL;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.CopiesSupported;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.Fidelity;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.PrinterState;
import javax.print.attribute.standard.PrinterStateReason;
import javax.print.attribute.standard.PrinterStateReasons;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.Severity;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.standard.Sides;
import javax.print.event.PrintServiceAttributeListener;
import sun.awt.windows.WPrinterJob;

public class Win32PrintService
  implements PrintService, AttributeUpdater, SunPrinterJobService
{
  public static MediaSize[] predefMedia;
  private static final DocFlavor[] supportedFlavors;
  private static final Class[] serviceAttrCats;
  private static Class[] otherAttrCats;
  public static final MediaSizeName[] dmPaperToPrintService;
  private static final MediaTray[] dmPaperBinToPrintService;
  private static int DM_PAPERSIZE;
  private static int DM_PRINTQUALITY;
  private static int DM_YRESOLUTION;
  private static final int DMRES_MEDIUM = -3;
  private static final int DMRES_HIGH = -4;
  private static final int DMORIENT_LANDSCAPE = 2;
  private static final int DMDUP_VERTICAL = 2;
  private static final int DMDUP_HORIZONTAL = 3;
  private static final int DMCOLLATE_TRUE = 1;
  private static final int DMPAPER_A2 = 66;
  private static final int DMPAPER_A6 = 70;
  private static final int DMPAPER_B6_JIS = 88;
  private static final int DEVCAP_COLOR = 1;
  private static final int DEVCAP_DUPLEX = 2;
  private static final int DEVCAP_COLLATE = 4;
  private static final int DEVCAP_QUALITY = 8;
  private static final int DEVCAP_POSTSCRIPT = 16;
  private String printer;
  private PrinterName name;
  private String port;
  private transient PrintServiceAttributeSet lastSet;
  private transient ServiceNotifier notifier = null;
  private MediaSizeName[] mediaSizeNames;
  private MediaPrintableArea[] mediaPrintables;
  private MediaTray[] mediaTrays;
  private PrinterResolution[] printRes;
  private int nCopies;
  private int prnCaps;
  private int[] defaultSettings;
  private boolean gotTrays;
  private boolean gotCopies;
  private boolean mediaInitialized;
  private ArrayList idList;
  private MediaSize[] mediaSizes;
  private boolean isInvalid;

  Win32PrintService(String paramString)
  {
    if (paramString == null)
      throw new IllegalArgumentException("null printer name");
    this.printer = paramString;
    this.mediaInitialized = false;
    this.gotTrays = false;
    this.gotCopies = false;
    this.isInvalid = false;
    this.printRes = null;
    this.prnCaps = 0;
    this.defaultSettings = null;
    this.port = null;
  }

  public void invalidateService()
  {
    this.isInvalid = true;
  }

  public String getName()
  {
    return this.printer;
  }

  private PrinterName getPrinterName()
  {
    if (this.name == null)
      this.name = new PrinterName(this.printer, null);
    return this.name;
  }

  public MediaSizeName findWin32Media(int paramInt)
  {
    if ((paramInt >= 1) && (paramInt <= dmPaperToPrintService.length))
    {
      switch (paramInt)
      {
      case 66:
        return MediaSizeName.ISO_A2;
      case 70:
        return MediaSizeName.ISO_A6;
      case 88:
        return MediaSizeName.JIS_B6;
      }
      return dmPaperToPrintService[(paramInt - 1)];
    }
    return null;
  }

  private boolean addToUniqueList(ArrayList paramArrayList, MediaSizeName paramMediaSizeName)
  {
    for (int i = 0; i < paramArrayList.size(); ++i)
    {
      MediaSizeName localMediaSizeName = (MediaSizeName)paramArrayList.get(i);
      if (localMediaSizeName == paramMediaSizeName)
        return false;
    }
    paramArrayList.add(paramMediaSizeName);
    return true;
  }

  private synchronized void initMedia()
  {
    if (this.mediaInitialized == true)
      return;
    this.mediaInitialized = true;
    int[] arrayOfInt = getAllMediaIDs(this.printer, getPort());
    if (arrayOfInt == null)
      return;
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    int i = 0;
    this.idList = new ArrayList();
    for (int j = 0; j < arrayOfInt.length; ++j)
      this.idList.add(new Integer(arrayOfInt[j]));
    this.mediaSizes = getMediaSizes(this.idList, arrayOfInt);
    for (j = 0; j < this.idList.size(); ++j)
    {
      MediaSizeName localMediaSizeName = findWin32Media(((Integer)this.idList.get(j)).intValue());
      if ((localMediaSizeName == null) && (this.idList.size() == this.mediaSizes.length))
        localMediaSizeName = this.mediaSizes[j].getMediaSizeName();
      if (localMediaSizeName != null)
      {
        boolean bool = addToUniqueList(localArrayList1, localMediaSizeName);
        if ((bool) && (i == 0))
        {
          float[] arrayOfFloat = getMediaPrintableArea(this.printer, ((Integer)this.idList.get(j)).intValue());
          if (arrayOfFloat != null)
            try
            {
              MediaPrintableArea localMediaPrintableArea = new MediaPrintableArea(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2], arrayOfFloat[3], 25400);
              localArrayList2.add(localMediaPrintableArea);
            }
            catch (IllegalArgumentException localIllegalArgumentException)
            {
            }
          else if (j == 0)
            i = 1;
        }
      }
    }
    this.mediaSizeNames = new MediaSizeName[localArrayList1.size()];
    localArrayList1.toArray(this.mediaSizeNames);
    this.mediaPrintables = new MediaPrintableArea[localArrayList2.size()];
    localArrayList2.toArray(this.mediaPrintables);
  }

  private synchronized MediaTray[] getMediaTrays()
  {
    if (this.gotTrays == true)
      return this.mediaTrays;
    this.gotTrays = true;
    String str = getPort();
    int[] arrayOfInt = getAllMediaTrays(this.printer, str);
    String[] arrayOfString = getAllMediaTrayNames(this.printer, str);
    if ((arrayOfInt == null) || (arrayOfString == null))
      return null;
    int i = 0;
    for (int j = 0; j < arrayOfInt.length; ++j)
      if (arrayOfInt[j] > 0)
        ++i;
    MediaTray[] arrayOfMediaTray = new MediaTray[i];
    int l = 0;
    int i1 = 0;
    while (l < arrayOfInt.length)
    {
      int k = arrayOfInt[l];
      if (k > 0)
        if ((k > dmPaperBinToPrintService.length) || (dmPaperBinToPrintService[(k - 1)] == null))
          arrayOfMediaTray[(i1++)] = new Win32MediaTray(k, arrayOfString[l]);
        else
          arrayOfMediaTray[(i1++)] = dmPaperBinToPrintService[(k - 1)];
      ++l;
    }
    return arrayOfMediaTray;
  }

  private boolean isSameSize(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
  {
    float f1 = paramFloat1 - paramFloat3;
    float f2 = paramFloat2 - paramFloat4;
    float f3 = paramFloat1 - paramFloat4;
    float f4 = paramFloat2 - paramFloat3;
    return (((Math.abs(f1) <= 1F) && (Math.abs(f2) <= 1F)) || ((Math.abs(f3) <= 1F) && (Math.abs(f4) <= 1F)));
  }

  public MediaSizeName findMatchingMediaSizeNameMM(float paramFloat1, float paramFloat2)
  {
    if (predefMedia != null)
      for (int i = 0; i < predefMedia.length; ++i)
      {
        if (predefMedia[i] == null)
          break label67:
        label67: if (isSameSize(predefMedia[i].getX(1000), predefMedia[i].getY(1000), paramFloat1, paramFloat2))
          return predefMedia[i].getMediaSizeName();
      }
    return null;
  }

  private MediaSize[] getMediaSizes(ArrayList paramArrayList, int[] paramArrayOfInt)
  {
    String str = getPort();
    int[] arrayOfInt = getAllMediaSizes(this.printer, str);
    String[] arrayOfString = getAllMediaNames(this.printer, str);
    MediaSizeName localMediaSizeName = null;
    MediaSize localMediaSize = null;
    if ((arrayOfInt == null) || (arrayOfString == null))
      return null;
    int i = arrayOfInt.length / 2;
    ArrayList localArrayList = new ArrayList();
    int j = 0;
    while (j < i)
    {
      Object localObject;
      float f1 = arrayOfInt[(j * 2)] / 10;
      float f2 = arrayOfInt[(j * 2 + 1)] / 10;
      if ((f1 <= 0F) || (f2 <= 0F))
      {
        if (i == paramArrayOfInt.length)
        {
          localObject = new Integer(paramArrayOfInt[j]);
          paramArrayList.remove(paramArrayList.indexOf(localObject));
        }
      }
      else
      {
        localMediaSizeName = findMatchingMediaSizeNameMM(f1, f2);
        if (localMediaSizeName != null)
          localMediaSize = MediaSize.getMediaSizeForName(localMediaSizeName);
        if (localMediaSize != null)
        {
          localArrayList.add(localMediaSize);
        }
        else
        {
          localObject = new Win32MediaSize(arrayOfString[j]);
          try
          {
            localMediaSize = new MediaSize(f1, f2, 1000, (MediaSizeName)localObject);
            localArrayList.add(localMediaSize);
          }
          catch (IllegalArgumentException localIllegalArgumentException)
          {
            if (i == paramArrayOfInt.length)
            {
              Integer localInteger = new Integer(paramArrayOfInt[j]);
              paramArrayList.remove(paramArrayList.indexOf(localInteger));
            }
          }
        }
      }
      ++j;
      localMediaSize = null;
    }
    MediaSize[] arrayOfMediaSize = new MediaSize[localArrayList.size()];
    localArrayList.toArray(arrayOfMediaSize);
    return ((MediaSize)arrayOfMediaSize);
  }

  private PrinterIsAcceptingJobs getPrinterIsAcceptingJobs()
  {
    if (getJobStatus(this.printer, 2) != 1)
      return PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS;
    return PrinterIsAcceptingJobs.ACCEPTING_JOBS;
  }

  private PrinterState getPrinterState()
  {
    if (this.isInvalid)
      return PrinterState.STOPPED;
    return null;
  }

  private PrinterStateReasons getPrinterStateReasons()
  {
    if (this.isInvalid)
    {
      PrinterStateReasons localPrinterStateReasons = new PrinterStateReasons();
      localPrinterStateReasons.put(PrinterStateReason.SHUTDOWN, Severity.ERROR);
      return localPrinterStateReasons;
    }
    return null;
  }

  private QueuedJobCount getQueuedJobCount()
  {
    int i = getJobStatus(this.printer, 1);
    if (i != -1)
      return new QueuedJobCount(i);
    return new QueuedJobCount(0);
  }

  private boolean isSupportedCopies(Copies paramCopies)
  {
    synchronized (this)
    {
      if (!(this.gotCopies))
      {
        this.nCopies = getCopiesSupported(this.printer, getPort());
        this.gotCopies = true;
      }
    }
    int i = paramCopies.getValue();
    return ((i > 0) && (i <= this.nCopies));
  }

  private boolean isSupportedMedia(MediaSizeName paramMediaSizeName)
  {
    initMedia();
    if (this.mediaSizeNames != null)
      for (int i = 0; i < this.mediaSizeNames.length; ++i)
        if (paramMediaSizeName.equals(this.mediaSizeNames[i]))
          return true;
    return false;
  }

  private boolean isSupportedMediaPrintableArea(MediaPrintableArea paramMediaPrintableArea)
  {
    initMedia();
    if (this.mediaPrintables != null)
      for (int i = 0; i < this.mediaPrintables.length; ++i)
        if (paramMediaPrintableArea.equals(this.mediaPrintables[i]))
          return true;
    return false;
  }

  private boolean isSupportedMediaTray(MediaTray paramMediaTray)
  {
    this.mediaTrays = getMediaTrays();
    if (this.mediaTrays != null)
      for (int i = 0; i < this.mediaTrays.length; ++i)
        if (paramMediaTray.equals(this.mediaTrays[i]))
          return true;
    return false;
  }

  private int getPrinterCapabilities()
  {
    if (this.prnCaps == 0)
      this.prnCaps = getCapabilities(this.printer, getPort());
    return this.prnCaps;
  }

  private String getPort()
  {
    if (this.port == null)
      this.port = getPrinterPort(this.printer);
    return this.port;
  }

  private int[] getDefaultPrinterSettings()
  {
    if (this.defaultSettings == null)
      this.defaultSettings = getDefaultSettings(this.printer);
    return this.defaultSettings;
  }

  private PrinterResolution[] getPrintResolutions()
  {
    if (this.printRes == null)
    {
      int[] arrayOfInt = getAllResolutions(this.printer, getPort());
      if (arrayOfInt == null)
      {
        this.printRes = new PrinterResolution[0];
      }
      else
      {
        int i = arrayOfInt.length / 2;
        ArrayList localArrayList = new ArrayList();
        for (int j = 0; j < i; ++j)
          try
          {
            PrinterResolution localPrinterResolution = new PrinterResolution(arrayOfInt[(j * 2)], arrayOfInt[(j * 2 + 1)], 100);
            localArrayList.add(localPrinterResolution);
          }
          catch (IllegalArgumentException localIllegalArgumentException)
          {
          }
        this.printRes = ((PrinterResolution[])(PrinterResolution[])localArrayList.toArray(new PrinterResolution[localArrayList.size()]));
      }
    }
    return this.printRes;
  }

  private boolean isSupportedResolution(PrinterResolution paramPrinterResolution)
  {
    PrinterResolution[] arrayOfPrinterResolution = getPrintResolutions();
    if (arrayOfPrinterResolution != null)
      for (int i = 0; i < arrayOfPrinterResolution.length; ++i)
        if (paramPrinterResolution.equals(arrayOfPrinterResolution[i]))
          return true;
    return false;
  }

  public DocPrintJob createPrintJob()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPrintJobAccess();
    return new Win32PrintJob(this);
  }

  private PrintServiceAttributeSet getDynamicAttributes()
  {
    HashPrintServiceAttributeSet localHashPrintServiceAttributeSet = new HashPrintServiceAttributeSet();
    localHashPrintServiceAttributeSet.add(getPrinterIsAcceptingJobs());
    localHashPrintServiceAttributeSet.add(getQueuedJobCount());
    return localHashPrintServiceAttributeSet;
  }

  public PrintServiceAttributeSet getUpdatedAttributes()
  {
    PrintServiceAttributeSet localPrintServiceAttributeSet = getDynamicAttributes();
    if (this.lastSet == null)
    {
      this.lastSet = localPrintServiceAttributeSet;
      return AttributeSetUtilities.unmodifiableView(localPrintServiceAttributeSet);
    }
    HashPrintServiceAttributeSet localHashPrintServiceAttributeSet = new HashPrintServiceAttributeSet();
    Attribute[] arrayOfAttribute = localPrintServiceAttributeSet.toArray();
    for (int i = 0; i < arrayOfAttribute.length; ++i)
    {
      Attribute localAttribute = arrayOfAttribute[i];
      if (!(this.lastSet.containsValue(localAttribute)))
        localHashPrintServiceAttributeSet.add(localAttribute);
    }
    this.lastSet = localPrintServiceAttributeSet;
    return AttributeSetUtilities.unmodifiableView(localHashPrintServiceAttributeSet);
  }

  public void wakeNotifier()
  {
    synchronized (this)
    {
      if (this.notifier != null)
        this.notifier.wake();
    }
  }

  public void addPrintServiceAttributeListener(PrintServiceAttributeListener paramPrintServiceAttributeListener)
  {
    synchronized (this)
    {
      if (paramPrintServiceAttributeListener != null)
        break label11;
      return;
      label11: if (this.notifier != null)
        break label30;
      this.notifier = new ServiceNotifier(this);
      label30: this.notifier.addListener(paramPrintServiceAttributeListener);
    }
  }

  public void removePrintServiceAttributeListener(PrintServiceAttributeListener paramPrintServiceAttributeListener)
  {
    synchronized (this)
    {
      if ((paramPrintServiceAttributeListener != null) && (this.notifier != null))
        break label18;
      return;
      label18: this.notifier.removeListener(paramPrintServiceAttributeListener);
      if (!(this.notifier.isEmpty()))
        break label48;
      this.notifier.stopNotifier();
      label48: this.notifier = null;
    }
  }

  public <T extends PrintServiceAttribute> T getAttribute(Class<T> paramClass)
  {
    if (paramClass == null)
      throw new NullPointerException("category");
    if (!(PrintServiceAttribute.class.isAssignableFrom(paramClass)))
      throw new IllegalArgumentException("Not a PrintServiceAttribute");
    if (paramClass == ColorSupported.class)
    {
      int i = getPrinterCapabilities();
      if ((i & 0x1) != 0)
        return ColorSupported.SUPPORTED;
      return ColorSupported.NOT_SUPPORTED;
    }
    if (paramClass == PrinterName.class)
      return getPrinterName();
    if (paramClass == PrinterState.class)
      return getPrinterState();
    if (paramClass == PrinterStateReasons.class)
      return getPrinterStateReasons();
    if (paramClass == QueuedJobCount.class)
      return getQueuedJobCount();
    if (paramClass == PrinterIsAcceptingJobs.class)
      return getPrinterIsAcceptingJobs();
    return null;
  }

  public PrintServiceAttributeSet getAttributes()
  {
    HashPrintServiceAttributeSet localHashPrintServiceAttributeSet = new HashPrintServiceAttributeSet();
    localHashPrintServiceAttributeSet.add(getPrinterName());
    localHashPrintServiceAttributeSet.add(getPrinterIsAcceptingJobs());
    PrinterState localPrinterState = getPrinterState();
    if (localPrinterState != null)
      localHashPrintServiceAttributeSet.add(localPrinterState);
    PrinterStateReasons localPrinterStateReasons = getPrinterStateReasons();
    if (localPrinterStateReasons != null)
      localHashPrintServiceAttributeSet.add(localPrinterStateReasons);
    localHashPrintServiceAttributeSet.add(getQueuedJobCount());
    int i = getPrinterCapabilities();
    if ((i & 0x1) != 0)
      localHashPrintServiceAttributeSet.add(ColorSupported.SUPPORTED);
    else
      localHashPrintServiceAttributeSet.add(ColorSupported.NOT_SUPPORTED);
    return AttributeSetUtilities.unmodifiableView(localHashPrintServiceAttributeSet);
  }

  public DocFlavor[] getSupportedDocFlavors()
  {
    DocFlavor[] arrayOfDocFlavor;
    int i = supportedFlavors.length;
    int j = getPrinterCapabilities();
    if ((j & 0x10) != 0)
    {
      arrayOfDocFlavor = new DocFlavor[i + 3];
      System.arraycopy(supportedFlavors, 0, arrayOfDocFlavor, 0, i);
      arrayOfDocFlavor[i] = DocFlavor.BYTE_ARRAY.POSTSCRIPT;
      arrayOfDocFlavor[(i + 1)] = DocFlavor.INPUT_STREAM.POSTSCRIPT;
      arrayOfDocFlavor[(i + 2)] = DocFlavor.URL.POSTSCRIPT;
    }
    else
    {
      arrayOfDocFlavor = new DocFlavor[i];
      System.arraycopy(supportedFlavors, 0, arrayOfDocFlavor, 0, i);
    }
    return arrayOfDocFlavor;
  }

  public boolean isDocFlavorSupported(DocFlavor paramDocFlavor)
  {
    DocFlavor[] arrayOfDocFlavor;
    if (isPostScriptFlavor(paramDocFlavor))
      arrayOfDocFlavor = getSupportedDocFlavors();
    else
      arrayOfDocFlavor = supportedFlavors;
    for (int i = 0; i < arrayOfDocFlavor.length; ++i)
      if (paramDocFlavor.equals(arrayOfDocFlavor[i]))
        return true;
    return false;
  }

  public Class<?>[] getSupportedAttributeCategories()
  {
    ArrayList localArrayList = new ArrayList(otherAttrCats.length + 3);
    for (int i = 0; i < otherAttrCats.length; ++i)
      localArrayList.add(otherAttrCats[i]);
    i = getPrinterCapabilities();
    if ((i & 0x2) != 0)
      localArrayList.add(Sides.class);
    if ((i & 0x8) != 0)
    {
      localObject = getDefaultPrinterSettings();
      if ((localObject[3] >= -4) && (localObject[3] < 0))
        localArrayList.add(PrintQuality.class);
    }
    Object localObject = getPrintResolutions();
    if ((localObject != null) && (localObject.length > 0))
      localArrayList.add(PrinterResolution.class);
    return ((Class<?>)(Class[])(Class[])localArrayList.toArray(new Class[localArrayList.size()]));
  }

  public boolean isAttributeCategorySupported(Class<? extends Attribute> paramClass)
  {
    if (paramClass == null)
      throw new NullPointerException("null category");
    if (!(Attribute.class.isAssignableFrom(paramClass)))
      throw new IllegalArgumentException(paramClass + " is not an Attribute");
    Class[] arrayOfClass = getSupportedAttributeCategories();
    for (int i = 0; i < arrayOfClass.length; ++i)
      if (paramClass.equals(arrayOfClass[i]))
        return true;
    return false;
  }

  public Object getDefaultAttributeValue(Class<? extends Attribute> paramClass)
  {
    Object localObject1;
    Object localObject2;
    if (paramClass == null)
      throw new NullPointerException("null category");
    if (!(Attribute.class.isAssignableFrom(paramClass)))
      throw new IllegalArgumentException(paramClass + " is not an Attribute");
    if (!(isAttributeCategorySupported(paramClass)))
      return null;
    int[] arrayOfInt = getDefaultPrinterSettings();
    int i = arrayOfInt[0];
    SecurityException localSecurityException1 = arrayOfInt[2];
    URISyntaxException localURISyntaxException1 = arrayOfInt[3];
    int j = arrayOfInt[4];
    int k = arrayOfInt[5];
    int l = arrayOfInt[6];
    int i1 = arrayOfInt[7];
    if (paramClass == Copies.class)
    {
      if (j > 0)
        return new Copies(j);
      return new Copies(1);
    }
    if (paramClass == Chromaticity.class)
    {
      int i2 = getPrinterCapabilities();
      if ((i2 & 0x1) == 0)
        return Chromaticity.MONOCHROME;
      return Chromaticity.COLOR;
    }
    if (paramClass == JobName.class)
      return new JobName("Java Printing", null);
    if (paramClass == OrientationRequested.class)
    {
      if (k == 2)
        return OrientationRequested.LANDSCAPE;
      return OrientationRequested.PORTRAIT;
    }
    if (paramClass == PageRanges.class)
      return new PageRanges(1, 2147483647);
    if (paramClass == Media.class)
    {
      localObject1 = findWin32Media(i);
      if (localObject1 != null)
        return localObject1;
      initMedia();
      if ((this.mediaSizeNames != null) && (this.mediaSizeNames.length > 0))
      {
        if ((this.idList != null) && (this.mediaSizes != null) && (this.idList.size() == this.mediaSizes.length))
        {
          localObject2 = new Integer(i);
          int i4 = this.idList.indexOf(localObject2);
          if ((i4 >= 0) && (i4 < this.mediaSizes.length))
            return this.mediaSizes[i4].getMediaSizeName();
        }
        return this.mediaSizeNames[0];
      }
    }
    else
    {
      if (paramClass == MediaPrintableArea.class)
      {
        localObject1 = getMediaPrintableArea(this.printer, i);
        if (localObject1 != null)
        {
          localObject2 = null;
          try
          {
            localObject2 = new MediaPrintableArea(localObject1[0], localObject1[1], localObject1[2], localObject1[3], 25400);
          }
          catch (IllegalArgumentException localIllegalArgumentException)
          {
          }
          return localObject2;
        }
        return null;
      }
      if (paramClass == SunAlternateMedia.class)
        return null;
      if (paramClass == Destination.class);
      try
      {
        return new Destination(new File("out.prn").toURI());
      }
      catch (SecurityException str)
      {
        try
        {
          return new Destination(new URI("file:out.prn"));
        }
        catch (URISyntaxException localURISyntaxException2)
        {
          return null;
        }
        if (paramClass == Sides.class)
        {
          switch (l)
          {
          case 2:
            return Sides.TWO_SIDED_LONG_EDGE;
          case 3:
            return Sides.TWO_SIDED_SHORT_EDGE;
          }
          return Sides.ONE_SIDED;
        }
        if (paramClass == PrinterResolution.class)
        {
          localSecurityException2 = localSecurityException1;
          localURISyntaxException2 = localURISyntaxException1;
          if ((localURISyntaxException2 < 0) || (localSecurityException2 < 0))
          {
            int i5 = (localSecurityException2 > localURISyntaxException2) ? localSecurityException2 : localURISyntaxException2;
            if (i5 > 0)
              return new PrinterResolution(i5, i5, 100);
          }
          else
          {
            return new PrinterResolution(localURISyntaxException2, localSecurityException2, 100);
          }
        }
        else
        {
          if (paramClass == ColorSupported.class)
          {
            int i3 = getPrinterCapabilities();
            if ((i3 & 0x1) != 0)
              return ColorSupported.SUPPORTED;
            return ColorSupported.NOT_SUPPORTED;
          }
          if (paramClass == PrintQuality.class)
          {
            if ((localURISyntaxException1 >= 0) || (localURISyntaxException1 < -4))
              break label752;
            switch (localURISyntaxException1)
            {
            case -4:
              return PrintQuality.HIGH;
            case -3:
              return PrintQuality.NORMAL;
            }
            return PrintQuality.DRAFT;
          }
          if (paramClass == RequestingUserName.class)
          {
            String str = "";
            try
            {
              str = System.getProperty("user.name", "");
            }
            catch (SecurityException localSecurityException3)
            {
            }
            return new RequestingUserName(str, null);
          }
          if (paramClass == SheetCollate.class)
          {
            if (i1 == 1)
              return SheetCollate.COLLATED;
            return SheetCollate.UNCOLLATED;
          }
          if (paramClass == Fidelity.class)
            return Fidelity.FIDELITY_FALSE;
        }
      }
    }
    label752: return null;
  }

  private boolean isPostScriptFlavor(DocFlavor paramDocFlavor)
  {
    return ((paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.POSTSCRIPT)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.POSTSCRIPT)) || (paramDocFlavor.equals(DocFlavor.URL.POSTSCRIPT)));
  }

  private boolean isPSDocAttr(Class paramClass)
  {
    return ((paramClass == OrientationRequested.class) || (paramClass == Copies.class));
  }

  private boolean isAutoSense(DocFlavor paramDocFlavor)
  {
    return ((paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.AUTOSENSE)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.AUTOSENSE)) || (paramDocFlavor.equals(DocFlavor.URL.AUTOSENSE)));
  }

  public Object getSupportedAttributeValues(Class<? extends Attribute> paramClass, DocFlavor paramDocFlavor, AttributeSet paramAttributeSet)
  {
    int i;
    Object localObject2;
    Object localObject3;
    if (paramClass == null)
      throw new NullPointerException("null category");
    if (!(Attribute.class.isAssignableFrom(paramClass)))
      throw new IllegalArgumentException(paramClass + " does not implement Attribute");
    if (paramDocFlavor != null)
    {
      if (!(isDocFlavorSupported(paramDocFlavor)))
        throw new IllegalArgumentException(paramDocFlavor + " is an unsupported flavor");
      if ((isAutoSense(paramDocFlavor)) || ((isPostScriptFlavor(paramDocFlavor)) && (isPSDocAttr(paramClass))))
        return null;
    }
    if (!(isAttributeCategorySupported(paramClass)))
      return null;
    if (paramClass == JobName.class)
      return new JobName("Java Printing", null);
    if (paramClass == RequestingUserName.class)
    {
      String str = "";
      try
      {
        str = System.getProperty("user.name", "");
      }
      catch (SecurityException localSecurityException2)
      {
      }
      return new RequestingUserName(str, null);
    }
    if (paramClass == ColorSupported.class)
    {
      i = getPrinterCapabilities();
      if ((i & 0x1) != 0)
        return ColorSupported.SUPPORTED;
      return ColorSupported.NOT_SUPPORTED;
    }
    if (paramClass == Chromaticity.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.GIF)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.GIF)) || (paramDocFlavor.equals(DocFlavor.URL.GIF)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.JPEG)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.JPEG)) || (paramDocFlavor.equals(DocFlavor.URL.JPEG)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.PNG)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.PNG)) || (paramDocFlavor.equals(DocFlavor.URL.PNG)))
      {
        i = getPrinterCapabilities();
        if ((i & 0x1) == 0)
        {
          arrayOfChromaticity = new Chromaticity[1];
          arrayOfChromaticity[0] = Chromaticity.MONOCHROME;
          return arrayOfChromaticity;
        }
        Chromaticity[] arrayOfChromaticity = new Chromaticity[2];
        arrayOfChromaticity[0] = Chromaticity.MONOCHROME;
        arrayOfChromaticity[1] = Chromaticity.COLOR;
        return arrayOfChromaticity;
      }
      return null;
    }
    if (paramClass == Destination.class);
    try
    {
      return new Destination(new File("out.prn").toURI());
    }
    catch (SecurityException localSecurityException1)
    {
      try
      {
        return new Destination(new URI("file:out.prn"));
      }
      catch (URISyntaxException localURISyntaxException)
      {
        return null;
      }
      if (paramClass == OrientationRequested.class)
      {
        if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.GIF)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.JPEG)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.PNG)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.GIF)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.JPEG)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.PNG)) || (paramDocFlavor.equals(DocFlavor.URL.GIF)) || (paramDocFlavor.equals(DocFlavor.URL.JPEG)) || (paramDocFlavor.equals(DocFlavor.URL.PNG)))
        {
          ??? = new OrientationRequested[3];
          ???[0] = OrientationRequested.PORTRAIT;
          ???[1] = OrientationRequested.LANDSCAPE;
          ???[2] = OrientationRequested.REVERSE_LANDSCAPE;
          return ???;
        }
        return null;
      }
      if ((paramClass == Copies.class) || (paramClass == CopiesSupported.class))
      {
        synchronized (this)
        {
          if (!(this.gotCopies))
          {
            this.nCopies = getCopiesSupported(this.printer, getPort());
            this.gotCopies = true;
          }
        }
        return new CopiesSupported(1, this.nCopies);
      }
    }
    if (paramClass == Media.class)
    {
      initMedia();
      int j = (this.mediaSizeNames == null) ? 0 : this.mediaSizeNames.length;
      this.mediaTrays = getMediaTrays();
      j += ((this.mediaTrays == null) ? 0 : this.mediaTrays.length);
      localObject3 = new Media[j];
      if (this.mediaSizeNames != null)
        System.arraycopy(this.mediaSizeNames, 0, localObject3, 0, this.mediaSizeNames.length);
      if (this.mediaTrays != null)
        System.arraycopy(this.mediaTrays, 0, localObject3, this.mediaSizeNames.length, this.mediaTrays.length);
      return localObject3;
    }
    if (paramClass == MediaPrintableArea.class)
    {
      initMedia();
      if (this.mediaPrintables == null)
        return null;
      if (paramAttributeSet != null)
        if (((localObject2 = (Media)paramAttributeSet.get(Media.class)) != null) && (localObject2 instanceof MediaSizeName))
        {
          localObject3 = new MediaPrintableArea[1];
          if (this.mediaSizeNames.length == this.mediaPrintables.length)
            for (int k = 0; k < this.mediaSizeNames.length; ++k)
              if (((Media)localObject2).equals(this.mediaSizeNames[k]))
              {
                localObject3[0] = this.mediaPrintables[k];
                return localObject3;
              }
          MediaSize localMediaSize = MediaSize.getMediaSizeForName((MediaSizeName)localObject2);
          if (localMediaSize != null)
          {
            localObject3[0] = new MediaPrintableArea(0F, 0F, localMediaSize.getX(25400), localMediaSize.getY(25400), 25400);
            return localObject3;
          }
          return null;
        }
      localObject3 = new MediaPrintableArea[this.mediaPrintables.length];
      System.arraycopy(this.mediaPrintables, 0, localObject3, 0, this.mediaPrintables.length);
      return localObject3;
    }
    if (paramClass == SunAlternateMedia.class)
      return new SunAlternateMedia((Media)getDefaultAttributeValue(Media.class));
    if (paramClass == PageRanges.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
      {
        localObject2 = new PageRanges[1];
        localObject2[0] = new PageRanges(1, 2147483647);
        return localObject2;
      }
      return null;
    }
    if (paramClass == PrinterResolution.class)
    {
      localObject2 = getPrintResolutions();
      if (localObject2 == null)
        return null;
      localObject3 = new PrinterResolution[localObject2.length];
      System.arraycopy(localObject2, 0, localObject3, 0, localObject2.length);
      return localObject3;
    }
    if (paramClass == Sides.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
      {
        localObject2 = new Sides[3];
        localObject2[0] = Sides.ONE_SIDED;
        localObject2[1] = Sides.TWO_SIDED_LONG_EDGE;
        localObject2[2] = Sides.TWO_SIDED_SHORT_EDGE;
        return localObject2;
      }
      return null;
    }
    if (paramClass == PrintQuality.class)
    {
      localObject2 = new PrintQuality[3];
      localObject2[0] = PrintQuality.DRAFT;
      localObject2[1] = PrintQuality.HIGH;
      localObject2[2] = PrintQuality.NORMAL;
      return localObject2;
    }
    if (paramClass == SheetCollate.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
      {
        localObject2 = new SheetCollate[2];
        localObject2[0] = SheetCollate.COLLATED;
        localObject2[1] = SheetCollate.UNCOLLATED;
        return localObject2;
      }
      return null;
    }
    if (paramClass == Fidelity.class)
    {
      localObject2 = new Fidelity[2];
      localObject2[0] = Fidelity.FIDELITY_FALSE;
      localObject2[1] = Fidelity.FIDELITY_TRUE;
      return localObject2;
    }
    return null;
  }

  public boolean isAttributeValueSupported(Attribute paramAttribute, DocFlavor paramDocFlavor, AttributeSet paramAttributeSet)
  {
    Object localObject;
    if (paramAttribute == null)
      throw new NullPointerException("null attribute");
    Class localClass = paramAttribute.getCategory();
    if (paramDocFlavor != null)
    {
      if (!(isDocFlavorSupported(paramDocFlavor)))
        throw new IllegalArgumentException(paramDocFlavor + " is an unsupported flavor");
      if ((isAutoSense(paramDocFlavor)) || ((isPostScriptFlavor(paramDocFlavor)) && (isPSDocAttr(localClass))))
        return false;
    }
    if (!(isAttributeCategorySupported(localClass)))
      return false;
    if (paramAttribute.getCategory() == Chromaticity.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.GIF)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.GIF)) || (paramDocFlavor.equals(DocFlavor.URL.GIF)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.JPEG)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.JPEG)) || (paramDocFlavor.equals(DocFlavor.URL.JPEG)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.PNG)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.PNG)) || (paramDocFlavor.equals(DocFlavor.URL.PNG)))
      {
        int i = getPrinterCapabilities();
        if ((i & 0x1) != 0)
          return true;
        return (paramAttribute == Chromaticity.MONOCHROME);
      }
      return false;
    }
    if (paramAttribute.getCategory() == Copies.class)
      return isSupportedCopies((Copies)paramAttribute);
    if (paramAttribute.getCategory() == Destination.class)
    {
      localObject = ((Destination)paramAttribute).getURI();
      return (("file".equals(((URI)localObject).getScheme())) && (!(((URI)localObject).getSchemeSpecificPart().equals(""))));
    }
    if (paramAttribute.getCategory() == Media.class)
    {
      if (paramAttribute instanceof MediaSizeName)
        return isSupportedMedia((MediaSizeName)paramAttribute);
      if (!(paramAttribute instanceof MediaTray))
        break label757;
      return isSupportedMediaTray((MediaTray)paramAttribute);
    }
    if (paramAttribute.getCategory() == MediaPrintableArea.class)
      return isSupportedMediaPrintableArea((MediaPrintableArea)paramAttribute);
    if (paramAttribute.getCategory() == SunAlternateMedia.class)
    {
      localObject = ((SunAlternateMedia)paramAttribute).getMedia();
      return isAttributeValueSupported((Attribute)localObject, paramDocFlavor, paramAttributeSet);
    }
    if (paramAttribute.getCategory() == PageRanges.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
        break label757;
      return false;
    }
    if (paramAttribute.getCategory() == SheetCollate.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
        break label757;
      return false;
    }
    if (paramAttribute.getCategory() == Sides.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
        break label757;
      return false;
    }
    if (paramAttribute.getCategory() == PrinterResolution.class)
    {
      if (!(paramAttribute instanceof PrinterResolution))
        break label757;
      return isSupportedResolution((PrinterResolution)paramAttribute);
    }
    if (paramAttribute.getCategory() == OrientationRequested.class)
    {
      if ((paramAttribute != OrientationRequested.REVERSE_PORTRAIT) && (((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.GIF)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.JPEG)) || (paramDocFlavor.equals(DocFlavor.INPUT_STREAM.PNG)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.GIF)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.JPEG)) || (paramDocFlavor.equals(DocFlavor.BYTE_ARRAY.PNG)) || (paramDocFlavor.equals(DocFlavor.URL.GIF)) || (paramDocFlavor.equals(DocFlavor.URL.JPEG)) || (paramDocFlavor.equals(DocFlavor.URL.PNG)))))
        break label757;
      return false;
    }
    if (paramAttribute.getCategory() != ColorSupported.class)
      break label757;
    int j = getPrinterCapabilities();
    int k = ((j & 0x1) != 0) ? 1 : 0;
    label757: return ((((k != 0) || (paramAttribute != ColorSupported.SUPPORTED))) && (((k == 0) || (paramAttribute != ColorSupported.NOT_SUPPORTED))));
  }

  public AttributeSet getUnsupportedAttributes(DocFlavor paramDocFlavor, AttributeSet paramAttributeSet)
  {
    if ((paramDocFlavor != null) && (!(isDocFlavorSupported(paramDocFlavor))))
      throw new IllegalArgumentException("flavor " + paramDocFlavor + "is not supported");
    if (paramAttributeSet == null)
      return null;
    HashAttributeSet localHashAttributeSet = new HashAttributeSet();
    Attribute[] arrayOfAttribute = paramAttributeSet.toArray();
    for (int i = 0; i < arrayOfAttribute.length; ++i)
      try
      {
        Attribute localAttribute = arrayOfAttribute[i];
        if (!(isAttributeCategorySupported(localAttribute.getCategory())))
          localHashAttributeSet.add(localAttribute);
        else if (!(isAttributeValueSupported(localAttribute, paramDocFlavor, paramAttributeSet)))
          localHashAttributeSet.add(localAttribute);
      }
      catch (ClassCastException localClassCastException)
      {
      }
    if (localHashAttributeSet.isEmpty())
      return null;
    return localHashAttributeSet;
  }

  public ServiceUIFactory getServiceUIFactory()
  {
    return null;
  }

  public String toString()
  {
    return "Win32 Printer : " + getName();
  }

  public boolean equals(Object paramObject)
  {
    return ((paramObject == this) || ((paramObject instanceof Win32PrintService) && (((Win32PrintService)paramObject).getName().equals(getName()))));
  }

  public int hashCode()
  {
    return (super.getClass().hashCode() + getName().hashCode());
  }

  public boolean usesClass(Class paramClass)
  {
    return (paramClass == WPrinterJob.class);
  }

  private native int[] getAllMediaIDs(String paramString1, String paramString2);

  private native int[] getAllMediaSizes(String paramString1, String paramString2);

  private native int[] getAllMediaTrays(String paramString1, String paramString2);

  private native float[] getMediaPrintableArea(String paramString, int paramInt);

  private native String[] getAllMediaNames(String paramString1, String paramString2);

  private native String[] getAllMediaTrayNames(String paramString1, String paramString2);

  private native int getCopiesSupported(String paramString1, String paramString2);

  private native int[] getAllResolutions(String paramString1, String paramString2);

  private native int getCapabilities(String paramString1, String paramString2);

  private native int[] getDefaultSettings(String paramString);

  private native int getJobStatus(String paramString, int paramInt);

  private native String getPrinterPort(String paramString);

  static
  {
    Win32MediaSize localWin32MediaSize = Win32MediaSize.class;
    supportedFlavors = { DocFlavor.BYTE_ARRAY.GIF, DocFlavor.INPUT_STREAM.GIF, DocFlavor.URL.GIF, DocFlavor.BYTE_ARRAY.JPEG, DocFlavor.INPUT_STREAM.JPEG, DocFlavor.URL.JPEG, DocFlavor.BYTE_ARRAY.PNG, DocFlavor.INPUT_STREAM.PNG, DocFlavor.URL.PNG, DocFlavor.SERVICE_FORMATTED.PAGEABLE, DocFlavor.SERVICE_FORMATTED.PRINTABLE, DocFlavor.BYTE_ARRAY.AUTOSENSE, DocFlavor.URL.AUTOSENSE, DocFlavor.INPUT_STREAM.AUTOSENSE };
    serviceAttrCats = { PrinterName.class, PrinterIsAcceptingJobs.class, QueuedJobCount.class, ColorSupported.class };
    otherAttrCats = { JobName.class, RequestingUserName.class, Copies.class, Destination.class, OrientationRequested.class, PageRanges.class, Media.class, MediaPrintableArea.class, Fidelity.class, SheetCollate.class, SunAlternateMedia.class, Chromaticity.class };
    dmPaperToPrintService = { MediaSizeName.NA_LETTER, MediaSizeName.NA_LETTER, MediaSizeName.TABLOID, MediaSizeName.LEDGER, MediaSizeName.NA_LEGAL, MediaSizeName.INVOICE, MediaSizeName.EXECUTIVE, MediaSizeName.ISO_A3, MediaSizeName.ISO_A4, MediaSizeName.ISO_A4, MediaSizeName.ISO_A5, MediaSizeName.JIS_B4, MediaSizeName.JIS_B5, MediaSizeName.FOLIO, MediaSizeName.QUARTO, MediaSizeName.NA_10X14_ENVELOPE, MediaSizeName.B, MediaSizeName.NA_LETTER, MediaSizeName.NA_NUMBER_9_ENVELOPE, MediaSizeName.NA_NUMBER_10_ENVELOPE, MediaSizeName.NA_NUMBER_11_ENVELOPE, MediaSizeName.NA_NUMBER_12_ENVELOPE, MediaSizeName.NA_NUMBER_14_ENVELOPE, MediaSizeName.C, MediaSizeName.D, MediaSizeName.E, MediaSizeName.ISO_DESIGNATED_LONG, MediaSizeName.ISO_C5, MediaSizeName.ISO_C3, MediaSizeName.ISO_C4, MediaSizeName.ISO_C6, MediaSizeName.ITALY_ENVELOPE, MediaSizeName.ISO_B4, MediaSizeName.ISO_B5, MediaSizeName.ISO_B6, MediaSizeName.ITALY_ENVELOPE, MediaSizeName.MONARCH_ENVELOPE, MediaSizeName.PERSONAL_ENVELOPE, MediaSizeName.NA_10X15_ENVELOPE, MediaSizeName.NA_9X12_ENVELOPE, MediaSizeName.FOLIO, MediaSizeName.ISO_B4, MediaSizeName.JAPANESE_POSTCARD, MediaSizeName.NA_9X11_ENVELOPE };
    dmPaperBinToPrintService = { MediaTray.TOP, MediaTray.BOTTOM, MediaTray.MIDDLE, MediaTray.MANUAL, MediaTray.ENVELOPE, Win32MediaTray.ENVELOPE_MANUAL, Win32MediaTray.AUTO, Win32MediaTray.TRACTOR, Win32MediaTray.SMALL_FORMAT, Win32MediaTray.LARGE_FORMAT, MediaTray.LARGE_CAPACITY, null, null, MediaTray.MAIN, Win32MediaTray.FORMSOURCE };
    DM_PAPERSIZE = 2;
    DM_PRINTQUALITY = 1024;
    DM_YRESOLUTION = 8192;
  }
}
package sun.print;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.Vector;
import javax.print.CancelablePrintJob;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocFlavor.BYTE_ARRAY;
import javax.print.DocFlavor.INPUT_STREAM;
import javax.print.DocFlavor.URL;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.DocumentName;
import javax.print.attribute.standard.Fidelity;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.JobOriginatingUserName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterState;
import javax.print.attribute.standard.PrinterStateReason;
import javax.print.attribute.standard.PrinterStateReasons;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import sun.awt.windows.WPrinterJob;

public class Win32PrintJob
  implements CancelablePrintJob
{
  private transient Vector jobListeners;
  private transient Vector attrListeners;
  private transient Vector listenedAttributeSets;
  private Win32PrintService service;
  private boolean fidelity;
  private boolean printing = false;
  private boolean printReturned = false;
  private PrintRequestAttributeSet reqAttrSet = null;
  private PrintJobAttributeSet jobAttrSet = null;
  private PrinterJob job;
  private Doc doc;
  private String mDestination = null;
  private InputStream instream = null;
  private Reader reader = null;
  private String jobName = "Java Printing";
  private int copies = 0;
  private MediaSizeName mediaName = null;
  private MediaSize mediaSize = null;
  private OrientationRequested orient = null;
  private long hPrintJob;
  private static final int PRINTBUFFERLEN = 8192;

  Win32PrintJob(Win32PrintService paramWin32PrintService)
  {
    this.service = paramWin32PrintService;
  }

  public PrintService getPrintService()
  {
    return this.service;
  }

  public PrintJobAttributeSet getAttributes()
  {
    synchronized (this)
    {
      if (this.jobAttrSet != null)
        break label26;
      HashPrintJobAttributeSet localHashPrintJobAttributeSet = new HashPrintJobAttributeSet();
      return AttributeSetUtilities.unmodifiableView(localHashPrintJobAttributeSet);
      label26: return this.jobAttrSet;
    }
  }

  public void addPrintJobListener(PrintJobListener paramPrintJobListener)
  {
    synchronized (this)
    {
      if (paramPrintJobListener != null)
        break label11;
      return;
      label11: if (this.jobListeners != null)
        break label29;
      this.jobListeners = new Vector();
      label29: this.jobListeners.add(paramPrintJobListener);
    }
  }

  public void removePrintJobListener(PrintJobListener paramPrintJobListener)
  {
    synchronized (this)
    {
      if ((paramPrintJobListener != null) && (this.jobListeners != null))
        break label18;
      return;
      label18: this.jobListeners.remove(paramPrintJobListener);
      if (!(this.jobListeners.isEmpty()))
        break label42;
      label42: this.jobListeners = null;
    }
  }

  private void closeDataStreams()
  {
    if (this.doc == null)
      return;
    Object localObject1 = null;
    try
    {
      localObject1 = this.doc.getPrintData();
    }
    catch (IOException localIOException1)
    {
      return;
    }
    if (this.instream != null)
      try
      {
        this.instream.close();
      }
      catch (IOException localIOException2)
      {
      }
      finally
      {
        this.instream = null;
      }
    else if (this.reader != null)
      try
      {
        this.reader.close();
      }
      catch (IOException localIOException3)
      {
      }
      finally
      {
        this.reader = null;
      }
    else if (localObject1 instanceof InputStream)
      try
      {
        ((InputStream)localObject1).close();
      }
      catch (IOException localIOException4)
      {
      }
    else if (localObject1 instanceof Reader)
      try
      {
        ((Reader)localObject1).close();
      }
      catch (IOException localIOException5)
      {
      }
  }

  private void notifyEvent(int paramInt)
  {
    switch (paramInt)
    {
    case 101:
    case 102:
    case 103:
    case 105:
    case 106:
      closeDataStreams();
    case 104:
    }
    synchronized (this)
    {
      if (this.jobListeners != null)
      {
        PrintJobEvent localPrintJobEvent = new PrintJobEvent(this, paramInt);
        for (int i = 0; i < this.jobListeners.size(); ++i)
        {
          PrintJobListener localPrintJobListener = (PrintJobListener)(PrintJobListener)this.jobListeners.elementAt(i);
          switch (paramInt)
          {
          case 102:
            localPrintJobListener.printJobCompleted(localPrintJobEvent);
            break;
          case 101:
            localPrintJobListener.printJobCanceled(localPrintJobEvent);
            break;
          case 103:
            localPrintJobListener.printJobFailed(localPrintJobEvent);
            break;
          case 106:
            localPrintJobListener.printDataTransferCompleted(localPrintJobEvent);
            break;
          case 105:
            localPrintJobListener.printJobNoMoreEvents(localPrintJobEvent);
          case 104:
          }
        }
      }
    }
  }

  public void addPrintJobAttributeListener(PrintJobAttributeListener paramPrintJobAttributeListener, PrintJobAttributeSet paramPrintJobAttributeSet)
  {
    synchronized (this)
    {
      if (paramPrintJobAttributeListener != null)
        break label11;
      return;
      label11: if (this.attrListeners != null)
        break label40;
      this.attrListeners = new Vector();
      this.listenedAttributeSets = new Vector();
      label40: this.attrListeners.add(paramPrintJobAttributeListener);
      if (paramPrintJobAttributeSet != null)
        break label61;
      paramPrintJobAttributeSet = new HashPrintJobAttributeSet();
      label61: this.listenedAttributeSets.add(paramPrintJobAttributeSet);
    }
  }

  public void removePrintJobAttributeListener(PrintJobAttributeListener paramPrintJobAttributeListener)
  {
    synchronized (this)
    {
      if ((paramPrintJobAttributeListener != null) && (this.attrListeners != null))
        break label18;
      return;
      label18: int i = this.attrListeners.indexOf(paramPrintJobAttributeListener);
      if (i != -1)
        break label35;
      return;
      label35: this.attrListeners.remove(i);
      this.listenedAttributeSets.remove(i);
      if (!(this.attrListeners.isEmpty()))
        break label73;
      this.attrListeners = null;
      label73: this.listenedAttributeSets = null;
    }
  }

  public void print(Doc paramDoc, PrintRequestAttributeSet paramPrintRequestAttributeSet)
    throws PrintException
  {
    Object localObject4;
    synchronized (this)
    {
      if (this.printing)
        throw new PrintException("already printing");
      this.printing = true;
    }
    ??? = (PrinterState)this.service.getAttribute(PrinterState.class);
    if (??? == PrinterState.STOPPED)
    {
      localObject3 = (PrinterStateReasons)this.service.getAttribute(PrinterStateReasons.class);
      if ((localObject3 != null) && (((PrinterStateReasons)localObject3).containsKey(PrinterStateReason.SHUTDOWN)))
        throw new PrintException("PrintService is no longer available.");
    }
    if ((PrinterIsAcceptingJobs)this.service.getAttribute(PrinterIsAcceptingJobs.class) == PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS)
      throw new PrintException("Printer is not accepting job.");
    this.doc = paramDoc;
    Object localObject3 = paramDoc.getDocFlavor();
    try
    {
      localObject4 = paramDoc.getPrintData();
    }
    catch (IOException localIOException1)
    {
      notifyEvent(103);
      throw new PrintException("can't get print data: " + localIOException1.toString());
    }
    if ((localObject3 == null) || (!(this.service.isDocFlavorSupported((DocFlavor)localObject3))))
    {
      notifyEvent(103);
      throw new PrintJobFlavorException("invalid flavor", (DocFlavor)localObject3);
    }
    initializeAttributeSets(paramDoc, paramPrintRequestAttributeSet);
    getAttributeValues((DocFlavor)localObject3);
    String str = ((DocFlavor)localObject3).getRepresentationClassName();
    if ((((DocFlavor)localObject3).equals(DocFlavor.INPUT_STREAM.GIF)) || (((DocFlavor)localObject3).equals(DocFlavor.INPUT_STREAM.JPEG)) || (((DocFlavor)localObject3).equals(DocFlavor.INPUT_STREAM.PNG)) || (((DocFlavor)localObject3).equals(DocFlavor.BYTE_ARRAY.GIF)) || (((DocFlavor)localObject3).equals(DocFlavor.BYTE_ARRAY.JPEG)) || (((DocFlavor)localObject3).equals(DocFlavor.BYTE_ARRAY.PNG)))
      try
      {
        this.instream = paramDoc.getStreamForBytes();
        if (this.instream == null)
        {
          notifyEvent(103);
          throw new PrintException("No stream for data");
        }
        printableJob(new ImagePrinter(this.instream));
        this.service.wakeNotifier();
        return;
      }
      catch (ClassCastException localClassCastException1)
      {
        notifyEvent(103);
        throw new PrintException(localClassCastException1);
      }
      catch (IOException localIOException2)
      {
        notifyEvent(103);
        throw new PrintException(localIOException2);
      }
    if ((((DocFlavor)localObject3).equals(DocFlavor.URL.GIF)) || (((DocFlavor)localObject3).equals(DocFlavor.URL.JPEG)) || (((DocFlavor)localObject3).equals(DocFlavor.URL.PNG)))
      try
      {
        printableJob(new ImagePrinter((URL)localObject4));
        this.service.wakeNotifier();
        return;
      }
      catch (ClassCastException localClassCastException2)
      {
        notifyEvent(103);
        throw new PrintException(localClassCastException2);
      }
    if (str.equals("java.awt.print.Pageable"))
      try
      {
        pageableJob((Pageable)paramDoc.getPrintData());
        this.service.wakeNotifier();
        return;
      }
      catch (ClassCastException localClassCastException3)
      {
        notifyEvent(103);
        throw new PrintException(localClassCastException3);
      }
      catch (IOException localIOException3)
      {
        notifyEvent(103);
        throw new PrintException(localIOException3);
      }
    if (str.equals("java.awt.print.Printable"))
      try
      {
        printableJob((Printable)paramDoc.getPrintData());
        this.service.wakeNotifier();
        return;
      }
      catch (ClassCastException localClassCastException4)
      {
        notifyEvent(103);
        throw new PrintException(localClassCastException4);
      }
      catch (IOException localIOException4)
      {
        notifyEvent(103);
        throw new PrintException(localIOException4);
      }
    if ((str.equals("[B")) || (str.equals("java.io.InputStream")) || (str.equals("java.net.URL")))
    {
      if (str.equals("java.net.URL"))
      {
        URL localURL = (URL)localObject4;
        try
        {
          this.instream = localURL.openStream();
        }
        catch (IOException localIOException7)
        {
          notifyEvent(103);
          throw new PrintException(localIOException7.toString());
        }
      }
      else
      {
        try
        {
          this.instream = paramDoc.getStreamForBytes();
        }
        catch (IOException localIOException5)
        {
          notifyEvent(103);
          throw new PrintException(localIOException5.toString());
        }
      }
      if (this.instream == null)
      {
        notifyEvent(103);
        throw new PrintException("No stream for data");
      }
      if (this.mDestination != null)
      {
        try
        {
          FileOutputStream localFileOutputStream = new FileOutputStream(this.mDestination);
          byte[] arrayOfByte1 = new byte[1024];
          while ((j = this.instream.read(arrayOfByte1, 0, arrayOfByte1.length)) >= 0)
          {
            int j;
            localFileOutputStream.write(arrayOfByte1, 0, j);
          }
          localFileOutputStream.flush();
          localFileOutputStream.close();
        }
        catch (FileNotFoundException localFileNotFoundException)
        {
          notifyEvent(103);
          throw new PrintException(localFileNotFoundException.toString());
        }
        catch (IOException localIOException6)
        {
          notifyEvent(103);
          throw new PrintException(localIOException6.toString());
        }
        notifyEvent(106);
        notifyEvent(102);
        this.service.wakeNotifier();
        return;
      }
      if (!(startPrintRawData(this.service.getName(), this.jobName)))
      {
        notifyEvent(103);
        throw new PrintException("Print job failed to start.");
      }
      BufferedInputStream localBufferedInputStream = new BufferedInputStream(this.instream);
      int i = 0;
      try
      {
        byte[] arrayOfByte2 = new byte[8192];
        do
          if ((i = localBufferedInputStream.read(arrayOfByte2, 0, 8192)) < 0)
            break label993;
        while (printRawData(arrayOfByte2, i));
        localBufferedInputStream.close();
        notifyEvent(103);
        throw new PrintException("Problem while spooling data");
        label993: localBufferedInputStream.close();
        if (!(endPrintRawData()))
        {
          notifyEvent(103);
          throw new PrintException("Print job failed to close properly.");
        }
        notifyEvent(106);
      }
      catch (IOException localIOException8)
      {
        throw new PrintException(localIOException8.toString());
      }
      finally
      {
        notifyEvent(105);
      }
    }
    else
    {
      notifyEvent(103);
      throw new PrintException("unrecognized class: " + str);
    }
    this.service.wakeNotifier();
  }

  public void printableJob(Printable paramPrintable)
    throws PrintException
  {
    try
    {
      synchronized (this)
      {
        if (this.job != null)
          throw new PrintException("already printing");
        this.job = new WPrinterJob();
      }
      ??? = getPrintService();
      this.job.setPrintService((PrintService)???);
      if (this.copies == 0)
      {
        localObject3 = (Copies)((PrintService)???).getDefaultAttributeValue(Copies.class);
        this.copies = ((Copies)localObject3).getValue();
      }
      if (this.mediaName == null)
      {
        localObject3 = ((PrintService)???).getDefaultAttributeValue(Media.class);
        if (localObject3 instanceof MediaSizeName)
        {
          this.mediaName = ((MediaSizeName)localObject3);
          this.mediaSize = MediaSize.getMediaSizeForName(this.mediaName);
        }
      }
      if (this.orient == null)
        this.orient = ((OrientationRequested)((PrintService)???).getDefaultAttributeValue(OrientationRequested.class));
      this.job.setCopies(this.copies);
      this.job.setJobName(this.jobName);
      Object localObject3 = new PageFormat();
      if (this.mediaSize != null)
      {
        Paper localPaper = new Paper();
        localPaper.setSize(this.mediaSize.getX(25400) * 72.0D, this.mediaSize.getY(25400) * 72.0D);
        localPaper.setImageableArea(72.0D, 72.0D, localPaper.getWidth() - 144.0D, localPaper.getHeight() - 144.0D);
        ((PageFormat)localObject3).setPaper(localPaper);
      }
      if (this.orient == OrientationRequested.REVERSE_LANDSCAPE)
        ((PageFormat)localObject3).setOrientation(2);
      else if (this.orient == OrientationRequested.LANDSCAPE)
        ((PageFormat)localObject3).setOrientation(0);
      this.job.setPrintable(paramPrintable, (PageFormat)localObject3);
      this.job.print(this.reqAttrSet);
      notifyEvent(106);
      return;
    }
    catch (PrinterException localPrinterException)
    {
      throw new PrintException(localPrinterException);
    }
    finally
    {
      this.printReturned = true;
      notifyEvent(105);
    }
  }

  public void pageableJob(Pageable paramPageable)
    throws PrintException
  {
    try
    {
      synchronized (this)
      {
        if (this.job != null)
          throw new PrintException("already printing");
        this.job = new WPrinterJob();
      }
      ??? = getPrintService();
      this.job.setPrintService((PrintService)???);
      if (this.copies == 0)
      {
        Copies localCopies = (Copies)((PrintService)???).getDefaultAttributeValue(Copies.class);
        this.copies = localCopies.getValue();
      }
      this.job.setCopies(this.copies);
      this.job.setJobName(this.jobName);
      this.job.setPageable(paramPageable);
      this.job.print(this.reqAttrSet);
      notifyEvent(106);
      return;
    }
    catch (PrinterException localPrinterException)
    {
      throw new PrintException(localPrinterException);
    }
    finally
    {
      this.printReturned = true;
      notifyEvent(105);
    }
  }

  private synchronized void initializeAttributeSets(Doc paramDoc, PrintRequestAttributeSet paramPrintRequestAttributeSet)
  {
    Attribute[] arrayOfAttribute;
    Object localObject1;
    this.reqAttrSet = new HashPrintRequestAttributeSet();
    this.jobAttrSet = new HashPrintJobAttributeSet();
    if (paramPrintRequestAttributeSet != null)
    {
      this.reqAttrSet.addAll(paramPrintRequestAttributeSet);
      arrayOfAttribute = paramPrintRequestAttributeSet.toArray();
      for (int i = 0; i < arrayOfAttribute.length; ++i)
        if (arrayOfAttribute[i] instanceof PrintJobAttribute)
          this.jobAttrSet.add(arrayOfAttribute[i]);
    }
    DocAttributeSet localDocAttributeSet = paramDoc.getAttributes();
    if (localDocAttributeSet != null)
    {
      arrayOfAttribute = localDocAttributeSet.toArray();
      for (int j = 0; j < arrayOfAttribute.length; ++j)
      {
        if (arrayOfAttribute[j] instanceof PrintRequestAttribute)
          this.reqAttrSet.add(arrayOfAttribute[j]);
        if (arrayOfAttribute[j] instanceof PrintJobAttribute)
          this.jobAttrSet.add(arrayOfAttribute[j]);
      }
    }
    String str = "";
    try
    {
      str = System.getProperty("user.name");
    }
    catch (SecurityException localSecurityException)
    {
    }
    if ((str == null) || (str.equals("")))
    {
      localObject1 = (RequestingUserName)paramPrintRequestAttributeSet.get(RequestingUserName.class);
      if (localObject1 != null)
        this.jobAttrSet.add(new JobOriginatingUserName(((RequestingUserName)localObject1).getValue(), ((RequestingUserName)localObject1).getLocale()));
      else
        this.jobAttrSet.add(new JobOriginatingUserName("", null));
    }
    else
    {
      this.jobAttrSet.add(new JobOriginatingUserName(str, null));
    }
    if (this.jobAttrSet.get(JobName.class) == null)
    {
      Object localObject2;
      if ((localDocAttributeSet != null) && (localDocAttributeSet.get(DocumentName.class) != null))
      {
        localObject2 = (DocumentName)localDocAttributeSet.get(DocumentName.class);
        localObject1 = new JobName(((DocumentName)localObject2).getValue(), ((DocumentName)localObject2).getLocale());
        this.jobAttrSet.add((Attribute)localObject1);
      }
      else
      {
        localObject2 = "JPS Job:" + paramDoc;
        try
        {
          Object localObject3 = paramDoc.getPrintData();
          if (localObject3 instanceof URL)
            localObject2 = ((URL)(URL)paramDoc.getPrintData()).toString();
        }
        catch (IOException localIOException)
        {
        }
        localObject1 = new JobName((String)localObject2, null);
        this.jobAttrSet.add((Attribute)localObject1);
      }
    }
    this.jobAttrSet = AttributeSetUtilities.unmodifiableView(this.jobAttrSet);
  }

  private void getAttributeValues(DocFlavor paramDocFlavor)
    throws PrintException
  {
    if (this.reqAttrSet.get(Fidelity.class) == Fidelity.FIDELITY_TRUE)
      this.fidelity = true;
    else
      this.fidelity = false;
    Attribute[] arrayOfAttribute = this.reqAttrSet.toArray();
    for (int i = 0; i < arrayOfAttribute.length; ++i)
    {
      Attribute localAttribute = arrayOfAttribute[i];
      Class localClass = localAttribute.getCategory();
      if (this.fidelity == true)
      {
        if (!(this.service.isAttributeCategorySupported(localClass)))
        {
          notifyEvent(103);
          throw new PrintJobAttributeException("unsupported category: " + localClass, localClass, null);
        }
        if (!(this.service.isAttributeValueSupported(localAttribute, paramDocFlavor, null)))
        {
          notifyEvent(103);
          throw new PrintJobAttributeException("unsupported attribute: " + localAttribute, null, localAttribute);
        }
      }
      if (localClass == Destination.class)
      {
        URI localURI = ((Destination)localAttribute).getURI();
        if (!("file".equals(localURI.getScheme())))
        {
          notifyEvent(103);
          throw new PrintException("Not a file: URI");
        }
        try
        {
          this.mDestination = new File(localURI).getPath();
        }
        catch (Exception localException)
        {
          throw new PrintException(localException);
        }
        SecurityManager localSecurityManager = System.getSecurityManager();
        if (localSecurityManager != null)
          try
          {
            localSecurityManager.checkWrite(this.mDestination);
          }
          catch (SecurityException localSecurityException)
          {
            notifyEvent(103);
            throw new PrintException(localSecurityException);
          }
      }
      else if (localClass == JobName.class)
      {
        this.jobName = ((JobName)localAttribute).getValue();
      }
      else if (localClass == Copies.class)
      {
        this.copies = ((Copies)localAttribute).getValue();
      }
      else if (localClass == Media.class)
      {
        if (localAttribute instanceof MediaSizeName)
        {
          this.mediaName = ((MediaSizeName)localAttribute);
          if (!(this.service.isAttributeValueSupported(localAttribute, null, null)))
            this.mediaSize = MediaSize.getMediaSizeForName(this.mediaName);
        }
      }
      else if (localClass == OrientationRequested.class)
      {
        this.orient = ((OrientationRequested)localAttribute);
      }
    }
  }

  private native boolean startPrintRawData(String paramString1, String paramString2);

  private native boolean printRawData(byte[] paramArrayOfByte, int paramInt);

  private native boolean endPrintRawData();

  public void cancel()
    throws PrintException
  {
    synchronized (this)
    {
      if (!(this.printing))
        throw new PrintException("Job is not yet submitted.");
      if ((this.job == null) || (this.printReturned))
        break label51;
      this.job.cancel();
      notifyEvent(101);
      return;
      label51: throw new PrintException("Job could not be cancelled.");
    }
  }
}
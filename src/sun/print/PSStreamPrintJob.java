package sun.print;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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
import javax.print.attribute.standard.DocumentName;
import javax.print.attribute.standard.Fidelity;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.JobOriginatingUserName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSize.NA;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

public class PSStreamPrintJob
  implements CancelablePrintJob
{
  private transient Vector jobListeners;
  private transient Vector attrListeners;
  private transient Vector listenedAttributeSets;
  private PSStreamPrintService service;
  private boolean fidelity;
  private boolean printing = false;
  private boolean printReturned = false;
  private PrintRequestAttributeSet reqAttrSet = null;
  private PrintJobAttributeSet jobAttrSet = null;
  private PrinterJob job;
  private Doc doc;
  private InputStream instream = null;
  private Reader reader = null;
  private String jobName = "Java Printing";
  private int copies = 1;
  private MediaSize mediaSize = MediaSize.NA.LETTER;
  private OrientationRequested orient = OrientationRequested.PORTRAIT;

  PSStreamPrintJob(PSStreamPrintService paramPSStreamPrintService)
  {
    this.service = paramPSStreamPrintService;
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
            break;
          case 102:
            localPrintJobListener.printJobCompleted(localPrintJobEvent);
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
    Object localObject3;
    synchronized (this)
    {
      if (this.printing)
        throw new PrintException("already printing");
      this.printing = true;
    }
    this.doc = paramDoc;
    ??? = paramDoc.getDocFlavor();
    try
    {
      localObject3 = paramDoc.getPrintData();
    }
    catch (IOException localIOException1)
    {
      notifyEvent(103);
      throw new PrintException("can't get print data: " + localIOException1.toString());
    }
    if ((??? == null) || (!(this.service.isDocFlavorSupported((DocFlavor)???))))
    {
      notifyEvent(103);
      throw new PrintJobFlavorException("invalid flavor", (DocFlavor)???);
    }
    initializeAttributeSets(paramDoc, paramPrintRequestAttributeSet);
    getAttributeValues((DocFlavor)???);
    String str = ((DocFlavor)???).getRepresentationClassName();
    if ((((DocFlavor)???).equals(DocFlavor.INPUT_STREAM.GIF)) || (((DocFlavor)???).equals(DocFlavor.INPUT_STREAM.JPEG)) || (((DocFlavor)???).equals(DocFlavor.INPUT_STREAM.PNG)) || (((DocFlavor)???).equals(DocFlavor.BYTE_ARRAY.GIF)) || (((DocFlavor)???).equals(DocFlavor.BYTE_ARRAY.JPEG)) || (((DocFlavor)???).equals(DocFlavor.BYTE_ARRAY.PNG)))
      try
      {
        this.instream = paramDoc.getStreamForBytes();
        printableJob(new ImagePrinter(this.instream), this.reqAttrSet);
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
    if ((((DocFlavor)???).equals(DocFlavor.URL.GIF)) || (((DocFlavor)???).equals(DocFlavor.URL.JPEG)) || (((DocFlavor)???).equals(DocFlavor.URL.PNG)))
      try
      {
        printableJob(new ImagePrinter((URL)localObject3), this.reqAttrSet);
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
        pageableJob((Pageable)paramDoc.getPrintData(), this.reqAttrSet);
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
        printableJob((Printable)paramDoc.getPrintData(), this.reqAttrSet);
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
    notifyEvent(103);
    throw new PrintException("unrecognized class: " + str);
  }

  public void printableJob(Printable paramPrintable, PrintRequestAttributeSet paramPrintRequestAttributeSet)
    throws PrintException
  {
    try
    {
      synchronized (this)
      {
        if (this.job != null)
          throw new PrintException("already printing");
        this.job = new PSPrinterJob();
      }
      this.job.setPrintService(getPrintService());
      ??? = new PageFormat();
      if (this.mediaSize != null)
      {
        Paper localPaper = new Paper();
        localPaper.setSize(this.mediaSize.getX(25400) * 72.0D, this.mediaSize.getY(25400) * 72.0D);
        localPaper.setImageableArea(72.0D, 72.0D, localPaper.getWidth() - 144.0D, localPaper.getHeight() - 144.0D);
        ((PageFormat)???).setPaper(localPaper);
      }
      if (this.orient == OrientationRequested.REVERSE_LANDSCAPE)
        ((PageFormat)???).setOrientation(2);
      else if (this.orient == OrientationRequested.LANDSCAPE)
        ((PageFormat)???).setOrientation(0);
      this.job.setPrintable(paramPrintable, (PageFormat)???);
      this.job.print(paramPrintRequestAttributeSet);
      notifyEvent(102);
      return;
    }
    catch (PrinterException localPrinterException)
    {
      throw new PrintException(localPrinterException);
    }
    finally
    {
      this.printReturned = true;
    }
  }

  public void pageableJob(Pageable paramPageable, PrintRequestAttributeSet paramPrintRequestAttributeSet)
    throws PrintException
  {
    try
    {
      synchronized (this)
      {
        if (this.job != null)
          throw new PrintException("already printing");
        this.job = new PSPrinterJob();
      }
      this.job.setPrintService(getPrintService());
      this.job.setPageable(paramPageable);
      this.job.print(paramPrintRequestAttributeSet);
      notifyEvent(102);
      return;
    }
    catch (PrinterException localPrinterException)
    {
      throw new PrintException(localPrinterException);
    }
    finally
    {
      this.printReturned = true;
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
      if (localClass == JobName.class)
        this.jobName = ((JobName)localAttribute).getValue();
      else if (localClass == Copies.class)
        this.copies = ((Copies)localAttribute).getValue();
      else if (localClass == Media.class)
        if ((localAttribute instanceof MediaSizeName) && (this.service.isAttributeValueSupported(localAttribute, null, null)))
          this.mediaSize = MediaSize.getMediaSizeForName((MediaSizeName)localAttribute);
      else if (localClass == OrientationRequested.class)
        this.orient = ((OrientationRequested)localAttribute);
    }
  }

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
package sun.print;

import java.security.AccessController;
import java.util.ArrayList;
import javax.print.DocFlavor;
import javax.print.MultiDocPrintService;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import sun.security.action.GetPropertyAction;
import sun.security.action.LoadLibraryAction;

public class Win32PrintServiceLookup extends PrintServiceLookup
{
  private String defaultPrinter;
  private PrintService defaultPrintService;
  private String[] printers;
  private PrintService[] printServices;
  private static Win32PrintServiceLookup win32PrintLUS;

  public static Win32PrintServiceLookup getWin32PrintLUS()
  {
    if (win32PrintLUS == null)
      PrintServiceLookup.lookupDefaultPrintService();
    return win32PrintLUS;
  }

  public Win32PrintServiceLookup()
  {
    if (win32PrintLUS == null)
    {
      win32PrintLUS = this;
      String str = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
      if ((str != null) && (str.startsWith("Windows 98")))
        return;
      PrinterChangeListener localPrinterChangeListener = new PrinterChangeListener(this);
      localPrinterChangeListener.setDaemon(true);
      localPrinterChangeListener.start();
    }
  }

  public synchronized PrintService[] getPrintServices()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPrintJobAccess();
    if (this.printServices == null)
      refreshServices();
    return this.printServices;
  }

  private synchronized void refreshServices()
  {
    this.printers = getAllPrinterNames();
    if (this.printers == null)
    {
      this.printServices = null;
      return;
    }
    PrintService[] arrayOfPrintService = new PrintService[this.printers.length];
    PrintService localPrintService = getDefaultPrintService();
    for (int i = 0; i < this.printers.length; ++i)
      if ((localPrintService != null) && (this.printers[i].equals(localPrintService.getName())))
      {
        arrayOfPrintService[i] = localPrintService;
      }
      else if (this.printServices == null)
      {
        arrayOfPrintService[i] = new Win32PrintService(this.printers[i]);
      }
      else
      {
        for (int j = 0; j < this.printServices.length; ++j)
          if ((this.printServices[j] != null) && (this.printers[i].equals(this.printServices[j].getName())))
          {
            arrayOfPrintService[i] = this.printServices[j];
            this.printServices[j] = null;
            break;
          }
        if (j == this.printServices.length)
          arrayOfPrintService[i] = new Win32PrintService(this.printers[i]);
      }
    if (this.printServices != null)
      for (i = 0; i < this.printServices.length; ++i)
        if ((this.printServices[i] instanceof Win32PrintService) && (!(this.printServices[i].equals(this.defaultPrintService))))
          ((Win32PrintService)this.printServices[i]).invalidateService();
    this.printServices = arrayOfPrintService;
  }

  public synchronized PrintService getPrintServiceByName(String paramString)
  {
    if ((paramString == null) || (paramString.equals("")))
      return null;
    if (this.printServices == null)
    {
      String[] arrayOfString = getAllPrinterNames();
      for (int j = 0; j < arrayOfString.length; ++j)
        if (arrayOfString[j].equals(paramString))
          return new Win32PrintService(paramString);
      return null;
    }
    for (int i = 0; i < this.printServices.length; ++i)
      if (this.printServices[i].getName().equals(paramString))
        return this.printServices[i];
    return null;
  }

  boolean matchingService(PrintService paramPrintService, PrintServiceAttributeSet paramPrintServiceAttributeSet)
  {
    if (paramPrintServiceAttributeSet != null)
    {
      Attribute[] arrayOfAttribute = paramPrintServiceAttributeSet.toArray();
      for (int i = 0; i < arrayOfAttribute.length; ++i)
      {
        PrintServiceAttribute localPrintServiceAttribute = paramPrintService.getAttribute(arrayOfAttribute[i].getCategory());
        if ((localPrintServiceAttribute == null) || (!(localPrintServiceAttribute.equals(arrayOfAttribute[i]))))
          return false;
      }
    }
    return true;
  }

  public PrintService[] getPrintServices(DocFlavor paramDocFlavor, AttributeSet paramAttributeSet)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPrintJobAccess();
    HashPrintRequestAttributeSet localHashPrintRequestAttributeSet = null;
    HashPrintServiceAttributeSet localHashPrintServiceAttributeSet = null;
    if ((paramAttributeSet != null) && (!(paramAttributeSet.isEmpty())))
    {
      localHashPrintRequestAttributeSet = new HashPrintRequestAttributeSet();
      localHashPrintServiceAttributeSet = new HashPrintServiceAttributeSet();
      localObject1 = paramAttributeSet.toArray();
      for (int i = 0; i < localObject1.length; ++i)
        if (localObject1[i] instanceof PrintRequestAttribute)
          localHashPrintRequestAttributeSet.add(localObject1[i]);
        else if (localObject1[i] instanceof PrintServiceAttribute)
          localHashPrintServiceAttributeSet.add(localObject1[i]);
    }
    Object localObject1 = null;
    if ((localHashPrintServiceAttributeSet != null) && (localHashPrintServiceAttributeSet.get(PrinterName.class) != null))
    {
      localObject2 = (PrinterName)localHashPrintServiceAttributeSet.get(PrinterName.class);
      PrintService localPrintService = getPrintServiceByName(((PrinterName)localObject2).getValue());
      if ((localPrintService == null) || (!(matchingService(localPrintService, localHashPrintServiceAttributeSet))))
      {
        localObject1 = new PrintService[0];
      }
      else
      {
        localObject1 = new PrintService[1];
        localObject1[0] = localPrintService;
      }
    }
    else
    {
      localObject1 = getPrintServices();
    }
    if (localObject1.length == 0)
      return localObject1;
    Object localObject2 = new ArrayList();
    for (int j = 0; j < localObject1.length; ++j)
      try
      {
        if (localObject1[j].getUnsupportedAttributes(paramDocFlavor, localHashPrintRequestAttributeSet) == null)
          ((ArrayList)localObject2).add(localObject1[j]);
      }
      catch (IllegalArgumentException localIllegalArgumentException)
      {
      }
    localObject1 = new PrintService[((ArrayList)localObject2).size()];
    return ((PrintService)(PrintService)(PrintService[])(PrintService[])((ArrayList)localObject2).toArray(localObject1));
  }

  public MultiDocPrintService[] getMultiDocPrintServices(DocFlavor[] paramArrayOfDocFlavor, AttributeSet paramAttributeSet)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPrintJobAccess();
    return new MultiDocPrintService[0];
  }

  public synchronized PrintService getDefaultPrintService()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPrintJobAccess();
    this.defaultPrintService = null;
    this.defaultPrinter = getDefaultPrinterName();
    if (this.defaultPrinter == null)
      return null;
    if (this.printServices != null)
      for (int i = 0; i < this.printServices.length; ++i)
        if (this.defaultPrinter.equals(this.printServices[i].getName()))
        {
          this.defaultPrintService = this.printServices[i];
          break;
        }
    if (this.defaultPrintService == null)
      this.defaultPrintService = new Win32PrintService(this.defaultPrinter);
    return this.defaultPrintService;
  }

  private native String getDefaultPrinterName();

  private native String[] getAllPrinterNames();

  private native long notifyFirstPrinterChange(String paramString);

  private native void notifyClosePrinterChange(long paramLong);

  private native int notifyPrinterChange(long paramLong);

  static
  {
    AccessController.doPrivileged(new LoadLibraryAction("awt"));
  }

  class PrinterChangeListener extends Thread
  {
    long chgObj;

    PrinterChangeListener()
    {
      this.chgObj = Win32PrintServiceLookup.access$000(paramWin32PrintServiceLookup, null);
    }

    public void run()
    {
      if (this.chgObj != -1L)
        if (Win32PrintServiceLookup.access$100(this.this$0, this.chgObj) != 0)
          try
          {
            Win32PrintServiceLookup.access$200(this.this$0);
          }
          catch (SecurityException localSecurityException)
          {
          }
        else
          Win32PrintServiceLookup.access$300(this.this$0, this.chgObj);
    }
  }
}
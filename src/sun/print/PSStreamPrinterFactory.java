package sun.print;

import java.io.OutputStream;
import javax.print.DocFlavor;
import javax.print.DocFlavor.BYTE_ARRAY;
import javax.print.DocFlavor.INPUT_STREAM;
import javax.print.DocFlavor.SERVICE_FORMATTED;
import javax.print.DocFlavor.URL;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;

public class PSStreamPrinterFactory extends StreamPrintServiceFactory
{
  static final String psMimeType = "application/postscript";
  static final DocFlavor[] supportedDocFlavors = { DocFlavor.SERVICE_FORMATTED.PAGEABLE, DocFlavor.SERVICE_FORMATTED.PRINTABLE, DocFlavor.BYTE_ARRAY.GIF, DocFlavor.INPUT_STREAM.GIF, DocFlavor.URL.GIF, DocFlavor.BYTE_ARRAY.JPEG, DocFlavor.INPUT_STREAM.JPEG, DocFlavor.URL.JPEG, DocFlavor.BYTE_ARRAY.PNG, DocFlavor.INPUT_STREAM.PNG, DocFlavor.URL.PNG };

  public String getOutputFormat()
  {
    return "application/postscript";
  }

  public DocFlavor[] getSupportedDocFlavors()
  {
    return getFlavors();
  }

  static DocFlavor[] getFlavors()
  {
    DocFlavor[] arrayOfDocFlavor = new DocFlavor[supportedDocFlavors.length];
    System.arraycopy(supportedDocFlavors, 0, arrayOfDocFlavor, 0, arrayOfDocFlavor.length);
    return arrayOfDocFlavor;
  }

  public StreamPrintService getPrintService(OutputStream paramOutputStream)
  {
    return new PSStreamPrintService(paramOutputStream);
  }
}
package sun.print;

import javax.print.DocFlavor;
import javax.print.FlavorException;
import javax.print.PrintException;

class PrintJobFlavorException extends PrintException
  implements FlavorException
{
  private DocFlavor flavor;

  PrintJobFlavorException(String paramString, DocFlavor paramDocFlavor)
  {
    super(paramString);
    this.flavor = paramDocFlavor;
  }

  public DocFlavor[] getUnsupportedFlavors()
  {
    DocFlavor[] arrayOfDocFlavor = { this.flavor };
    return arrayOfDocFlavor;
  }
}
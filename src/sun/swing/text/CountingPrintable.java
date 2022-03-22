package sun.swing.text;

import java.awt.print.Printable;

public abstract interface CountingPrintable extends Printable
{
  public abstract int getNumberOfPages();
}
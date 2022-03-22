package sun.awt.windows;

import java.awt.Desktop.Action;
import java.awt.peer.DesktopPeer;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class WDesktopPeer
  implements DesktopPeer
{
  private static String ACTION_OPEN_VERB = "open";
  private static String ACTION_EDIT_VERB = "edit";
  private static String ACTION_PRINT_VERB = "print";

  public boolean isSupported(Desktop.Action paramAction)
  {
    return true;
  }

  public void open(File paramFile)
    throws IOException
  {
    ShellExecute(paramFile.toURI(), ACTION_OPEN_VERB);
  }

  public void edit(File paramFile)
    throws IOException
  {
    ShellExecute(paramFile.toURI(), ACTION_EDIT_VERB);
  }

  public void print(File paramFile)
    throws IOException
  {
    ShellExecute(paramFile.toURI(), ACTION_PRINT_VERB);
  }

  public void mail(URI paramURI)
    throws IOException
  {
    ShellExecute(paramURI, ACTION_OPEN_VERB);
  }

  public void browse(URI paramURI)
    throws IOException
  {
    ShellExecute(paramURI, ACTION_OPEN_VERB);
  }

  private void ShellExecute(URI paramURI, String paramString)
    throws IOException
  {
    String str = ShellExecute(paramURI.toString(), paramString);
    if (str != null)
      throw new IOException("Failed to " + paramString + " " + paramURI + ". Error message: " + str);
  }

  private static native String ShellExecute(String paramString1, String paramString2);
}
package sun.awt.windows;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class WDropTargetContextPeerFileStream extends FileInputStream
{
  private long stgmedium;

  WDropTargetContextPeerFileStream(String paramString, long paramLong)
    throws FileNotFoundException
  {
    super(paramString);
    this.stgmedium = paramLong;
  }

  public void close()
    throws IOException
  {
    if (this.stgmedium != 3412046810217185280L)
    {
      super.close();
      freeStgMedium(this.stgmedium);
      this.stgmedium = 3412047463052214272L;
    }
  }

  private native void freeStgMedium(long paramLong);
}
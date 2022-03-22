package sun.java2d.pipe.hw;

import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.ImageCapabilities;

public class ExtendedBufferCapabilities extends BufferCapabilities
{
  private VSyncType vsync;

  public ExtendedBufferCapabilities(BufferCapabilities paramBufferCapabilities)
  {
    super(paramBufferCapabilities.getFrontBufferCapabilities(), paramBufferCapabilities.getBackBufferCapabilities(), paramBufferCapabilities.getFlipContents());
    this.vsync = VSyncType.VSYNC_DEFAULT;
  }

  public ExtendedBufferCapabilities(ImageCapabilities paramImageCapabilities1, ImageCapabilities paramImageCapabilities2, BufferCapabilities.FlipContents paramFlipContents)
  {
    super(paramImageCapabilities1, paramImageCapabilities2, paramFlipContents);
    this.vsync = VSyncType.VSYNC_DEFAULT;
  }

  public ExtendedBufferCapabilities(ImageCapabilities paramImageCapabilities1, ImageCapabilities paramImageCapabilities2, BufferCapabilities.FlipContents paramFlipContents, VSyncType paramVSyncType)
  {
    super(paramImageCapabilities1, paramImageCapabilities2, paramFlipContents);
    this.vsync = paramVSyncType;
  }

  public ExtendedBufferCapabilities(BufferCapabilities paramBufferCapabilities, VSyncType paramVSyncType)
  {
    super(paramBufferCapabilities.getFrontBufferCapabilities(), paramBufferCapabilities.getBackBufferCapabilities(), paramBufferCapabilities.getFlipContents());
    this.vsync = paramVSyncType;
  }

  public ExtendedBufferCapabilities derive(VSyncType paramVSyncType)
  {
    return new ExtendedBufferCapabilities(this, paramVSyncType);
  }

  public VSyncType getVSync()
  {
    return this.vsync;
  }

  public final boolean isPageFlipping()
  {
    return true;
  }

  public static enum VSyncType
  {
    VSYNC_DEFAULT, VSYNC_ON, VSYNC_OFF;

    private int id;

    public int id()
    {
      return this.id;
    }
  }
}
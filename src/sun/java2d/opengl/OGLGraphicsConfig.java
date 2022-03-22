package sun.java2d.opengl;

import sun.java2d.pipe.hw.AccelGraphicsConfig;

abstract interface OGLGraphicsConfig extends AccelGraphicsConfig
{
  public abstract OGLContext getContext();

  public abstract long getNativeConfigInfo();

  public abstract boolean isCapPresent(int paramInt);
}
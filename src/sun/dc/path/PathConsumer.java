package sun.dc.path;

public abstract interface PathConsumer
{
  public abstract void beginPath()
    throws sun.dc.path.PathError;

  public abstract void beginSubpath(float paramFloat1, float paramFloat2)
    throws sun.dc.path.PathError;

  public abstract void appendLine(float paramFloat1, float paramFloat2)
    throws sun.dc.path.PathError;

  public abstract void appendQuadratic(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
    throws sun.dc.path.PathError;

  public abstract void appendCubic(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6)
    throws sun.dc.path.PathError;

  public abstract void closedSubpath()
    throws sun.dc.path.PathError;

  public abstract void endPath()
    throws sun.dc.path.PathError, sun.dc.path.PathException;

  public abstract void useProxy(FastPathProducer paramFastPathProducer)
    throws sun.dc.path.PathError, sun.dc.path.PathException;

  public abstract long getCPathConsumer();

  public abstract void dispose();

  public abstract PathConsumer getConsumer();
}
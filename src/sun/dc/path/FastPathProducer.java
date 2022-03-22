package sun.dc.path;

public abstract interface FastPathProducer
{
  public abstract void getBox(float[] paramArrayOfFloat)
    throws sun.dc.path.PathError;

  public abstract void sendTo(PathConsumer paramPathConsumer)
    throws sun.dc.path.PathError, sun.dc.path.PathException;
}
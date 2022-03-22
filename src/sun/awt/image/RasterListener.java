package sun.awt.image;

public abstract interface RasterListener
{
  public abstract void rasterChanged();

  public abstract void rasterStolen();
}
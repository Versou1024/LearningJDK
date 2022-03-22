package sun.java2d.pipe;

public class RegionIterator
{
  Region region;
  int curIndex;
  int numXbands;

  RegionIterator(Region paramRegion)
  {
    this.region = paramRegion;
  }

  public RegionIterator createCopy()
  {
    RegionIterator localRegionIterator = new RegionIterator(this.region);
    localRegionIterator.curIndex = this.curIndex;
    localRegionIterator.numXbands = this.numXbands;
    return localRegionIterator;
  }

  public void copyStateFrom(RegionIterator paramRegionIterator)
  {
    if (this.region != paramRegionIterator.region)
      throw new InternalError("region mismatch");
    this.curIndex = paramRegionIterator.curIndex;
    this.numXbands = paramRegionIterator.numXbands;
  }

  public boolean nextYRange(int[] paramArrayOfInt)
  {
    this.curIndex += this.numXbands * 2;
    this.numXbands = 0;
    if (this.curIndex >= this.region.endIndex)
      return false;
    paramArrayOfInt[1] = this.region.bands[(this.curIndex++)];
    paramArrayOfInt[3] = this.region.bands[(this.curIndex++)];
    this.numXbands = this.region.bands[(this.curIndex++)];
    return true;
  }

  public boolean nextXBand(int[] paramArrayOfInt)
  {
    if (this.numXbands <= 0)
      return false;
    this.numXbands -= 1;
    paramArrayOfInt[0] = this.region.bands[(this.curIndex++)];
    paramArrayOfInt[2] = this.region.bands[(this.curIndex++)];
    return true;
  }
}
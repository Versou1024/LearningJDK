package sun.java2d.pipe;

public class RegionClipSpanIterator
  implements SpanIterator
{
  Region rgn;
  SpanIterator spanIter;
  RegionIterator resetState;
  RegionIterator lwm;
  RegionIterator row;
  RegionIterator box;
  int spanlox;
  int spanhix;
  int spanloy;
  int spanhiy;
  int lwmloy;
  int lwmhiy;
  int rgnlox;
  int rgnloy;
  int rgnhix;
  int rgnhiy;
  int rgnbndslox;
  int rgnbndsloy;
  int rgnbndshix;
  int rgnbndshiy;
  int[] rgnbox = new int[4];
  int[] spanbox = new int[4];
  boolean doNextSpan;
  boolean doNextBox;
  boolean done = false;

  public RegionClipSpanIterator(Region paramRegion, SpanIterator paramSpanIterator)
  {
    this.spanIter = paramSpanIterator;
    this.resetState = paramRegion.getIterator();
    this.lwm = this.resetState.createCopy();
    if (!(this.lwm.nextYRange(this.rgnbox)))
    {
      this.done = true;
      return;
    }
    this.rgnloy = (this.lwmloy = this.rgnbox[1]);
    this.rgnhiy = (this.lwmhiy = this.rgnbox[3]);
    paramRegion.getBounds(this.rgnbox);
    this.rgnbndslox = this.rgnbox[0];
    this.rgnbndsloy = this.rgnbox[1];
    this.rgnbndshix = this.rgnbox[2];
    this.rgnbndshiy = this.rgnbox[3];
    if ((this.rgnbndslox >= this.rgnbndshix) || (this.rgnbndsloy >= this.rgnbndshiy))
    {
      this.done = true;
      return;
    }
    this.rgn = paramRegion;
    this.row = this.lwm.createCopy();
    this.box = this.row.createCopy();
    this.doNextSpan = true;
    this.doNextBox = false;
  }

  public void getPathBox(int[] paramArrayOfInt)
  {
    int[] arrayOfInt = new int[4];
    this.rgn.getBounds(arrayOfInt);
    this.spanIter.getPathBox(paramArrayOfInt);
    if (paramArrayOfInt[0] < arrayOfInt[0])
      paramArrayOfInt[0] = arrayOfInt[0];
    if (paramArrayOfInt[1] < arrayOfInt[1])
      paramArrayOfInt[1] = arrayOfInt[1];
    if (paramArrayOfInt[2] > arrayOfInt[2])
      paramArrayOfInt[2] = arrayOfInt[2];
    if (paramArrayOfInt[3] > arrayOfInt[3])
      paramArrayOfInt[3] = arrayOfInt[3];
  }

  public void intersectClipBox(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.spanIter.intersectClipBox(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public boolean nextSpan(int[] paramArrayOfInt)
  {
    label327: label507: int i;
    int j;
    int k;
    int l;
    if (this.done)
      return false;
    int i1 = 0;
    while (true)
    {
      do
      {
        while (true)
        {
          while (true)
          {
            while (true)
            {
              while (true)
              {
                while (true)
                {
                  while (true)
                  {
                    do
                    {
                      while (true)
                      {
                        while (true)
                        {
                          while (true)
                          {
                            while (true)
                            {
                              if (!(this.doNextSpan))
                                break label327;
                              if (!(this.spanIter.nextSpan(this.spanbox)))
                              {
                                this.done = true;
                                return false;
                              }
                              this.spanlox = this.spanbox[0];
                              if (this.spanlox < this.rgnbndshix)
                                break;
                            }
                            this.spanloy = this.spanbox[1];
                            if (this.spanloy < this.rgnbndshiy)
                              break;
                          }
                          this.spanhix = this.spanbox[2];
                          if (this.spanhix > this.rgnbndslox)
                            break;
                        }
                        this.spanhiy = this.spanbox[3];
                        if (this.spanhiy > this.rgnbndsloy)
                          break;
                      }
                      if (this.lwmloy > this.spanloy)
                      {
                        this.lwm.copyStateFrom(this.resetState);
                        this.lwm.nextYRange(this.rgnbox);
                        this.lwmloy = this.rgnbox[1];
                      }
                      for (this.lwmhiy = this.rgnbox[3]; this.lwmhiy <= this.spanloy; this.lwmhiy = this.rgnbox[3])
                      {
                        if (!(this.lwm.nextYRange(this.rgnbox)))
                          break;
                        this.lwmloy = this.rgnbox[1];
                      }
                    }
                    while ((this.lwmhiy <= this.spanloy) || (this.lwmloy >= this.spanhiy));
                    if (this.rgnloy != this.lwmloy)
                    {
                      this.row.copyStateFrom(this.lwm);
                      this.rgnloy = this.lwmloy;
                      this.rgnhiy = this.lwmhiy;
                    }
                    this.box.copyStateFrom(this.row);
                    this.doNextBox = true;
                    this.doNextSpan = false;
                  }
                  if (i1 == 0)
                    break label416;
                  i1 = 0;
                  bool = this.row.nextYRange(this.rgnbox);
                  if (bool)
                  {
                    this.rgnloy = this.rgnbox[1];
                    this.rgnhiy = this.rgnbox[3];
                  }
                  if ((bool) && (this.rgnloy < this.spanhiy))
                    break;
                  this.doNextSpan = true;
                }
                this.box.copyStateFrom(this.row);
                this.doNextBox = true;
              }
              label416: if (!(this.doNextBox))
                break label530;
              boolean bool = this.box.nextXBand(this.rgnbox);
              if (bool)
              {
                this.rgnlox = this.rgnbox[0];
                this.rgnhix = this.rgnbox[2];
              }
              if ((bool) && (this.rgnlox < this.spanhix))
                break label507;
              this.doNextBox = false;
              if (this.rgnhiy < this.spanhiy)
                break;
              this.doNextSpan = true;
            }
            i1 = 1;
          }
          this.doNextBox = (this.rgnhix <= this.spanlox);
        }
        label530: this.doNextBox = true;
        if (this.spanlox > this.rgnlox)
          i = this.spanlox;
        else
          i = this.rgnlox;
        if (this.spanloy > this.rgnloy)
          j = this.spanloy;
        else
          j = this.rgnloy;
        if (this.spanhix < this.rgnhix)
          k = this.spanhix;
        else
          k = this.rgnhix;
        if (this.spanhiy < this.rgnhiy)
          l = this.spanhiy;
        else
          l = this.rgnhiy;
      }
      while (i >= k);
      if (j < l)
        break;
    }
    paramArrayOfInt[0] = i;
    paramArrayOfInt[1] = j;
    paramArrayOfInt[2] = k;
    paramArrayOfInt[3] = l;
    return true;
  }

  public void skipDownTo(int paramInt)
  {
    this.spanIter.skipDownTo(paramInt);
  }

  public long getNativeIterator()
  {
    return 3412046964836007936L;
  }

  protected void finalize()
  {
  }
}
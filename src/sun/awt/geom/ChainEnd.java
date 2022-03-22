package sun.awt.geom;

final class ChainEnd
{
  CurveLink head;
  CurveLink tail;
  ChainEnd partner;
  int etag;

  public ChainEnd(CurveLink paramCurveLink, ChainEnd paramChainEnd)
  {
    this.head = paramCurveLink;
    this.tail = paramCurveLink;
    this.partner = paramChainEnd;
    this.etag = paramCurveLink.getEdgeTag();
  }

  public CurveLink getChain()
  {
    return this.head;
  }

  public void setOtherEnd(ChainEnd paramChainEnd)
  {
    this.partner = paramChainEnd;
  }

  public ChainEnd getPartner()
  {
    return this.partner;
  }

  public CurveLink linkTo(ChainEnd paramChainEnd)
  {
    ChainEnd localChainEnd1;
    ChainEnd localChainEnd2;
    if ((this.etag == 0) || (paramChainEnd.etag == 0))
      throw new InternalError("ChainEnd linked more than once!");
    if (this.etag == paramChainEnd.etag)
      throw new InternalError("Linking chains of the same type!");
    if (this.etag == 1)
    {
      localChainEnd1 = this;
      localChainEnd2 = paramChainEnd;
    }
    else
    {
      localChainEnd1 = paramChainEnd;
      localChainEnd2 = this;
    }
    this.etag = 0;
    paramChainEnd.etag = 0;
    localChainEnd1.tail.setNext(localChainEnd2.head);
    localChainEnd1.tail = localChainEnd2.tail;
    if (this.partner == paramChainEnd)
      return localChainEnd1.head;
    ChainEnd localChainEnd3 = localChainEnd2.partner;
    ChainEnd localChainEnd4 = localChainEnd1.partner;
    localChainEnd3.partner = localChainEnd4;
    localChainEnd4.partner = localChainEnd3;
    if (localChainEnd1.head.getYTop() < localChainEnd3.head.getYTop())
    {
      localChainEnd1.tail.setNext(localChainEnd3.head);
      localChainEnd3.head = localChainEnd1.head;
    }
    else
    {
      localChainEnd4.tail.setNext(localChainEnd1.head);
      localChainEnd4.tail = localChainEnd1.tail;
    }
    return null;
  }

  public void addLink(CurveLink paramCurveLink)
  {
    if (this.etag == 1)
    {
      this.tail.setNext(paramCurveLink);
      this.tail = paramCurveLink;
    }
    else
    {
      paramCurveLink.setNext(this.head);
      this.head = paramCurveLink;
    }
  }

  public double getX()
  {
    if (this.etag == 1)
      return this.tail.getXBot();
    return this.head.getXBot();
  }
}
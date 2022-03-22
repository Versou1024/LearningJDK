package sun.rmi.transport;

class SequenceEntry
{
  long sequenceNum;
  boolean keep;

  SequenceEntry(long paramLong)
  {
    this.sequenceNum = paramLong;
    this.keep = false;
  }

  void retain(long paramLong)
  {
    this.sequenceNum = paramLong;
    this.keep = true;
  }

  void update(long paramLong)
  {
    this.sequenceNum = paramLong;
  }
}
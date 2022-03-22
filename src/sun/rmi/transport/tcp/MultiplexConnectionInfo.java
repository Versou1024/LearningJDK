package sun.rmi.transport.tcp;

class MultiplexConnectionInfo
{
  int id;
  MultiplexInputStream in = null;
  MultiplexOutputStream out = null;
  boolean closed = false;

  MultiplexConnectionInfo(int paramInt)
  {
    this.id = paramInt;
  }
}
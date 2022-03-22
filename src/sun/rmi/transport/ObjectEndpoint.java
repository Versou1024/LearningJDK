package sun.rmi.transport;

import java.rmi.server.ObjID;

class ObjectEndpoint
{
  private final ObjID id;
  private final Transport transport;

  ObjectEndpoint(ObjID paramObjID, Transport paramTransport)
  {
    if (paramObjID == null)
      throw new NullPointerException();
    if ((!($assertionsDisabled)) && (paramTransport == null) && (!(paramObjID.equals(new ObjID(2)))))
      throw new AssertionError();
    this.id = paramObjID;
    this.transport = paramTransport;
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof ObjectEndpoint)
    {
      ObjectEndpoint localObjectEndpoint = (ObjectEndpoint)paramObject;
      return ((this.id.equals(localObjectEndpoint.id)) && (this.transport == localObjectEndpoint.transport));
    }
    return false;
  }

  public int hashCode()
  {
    return (this.id.hashCode() ^ ((this.transport != null) ? this.transport.hashCode() : 0));
  }

  public String toString()
  {
    return this.id.toString();
  }
}
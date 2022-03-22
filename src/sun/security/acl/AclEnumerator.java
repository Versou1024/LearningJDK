package sun.security.acl;

import java.security.acl.Acl;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

final class AclEnumerator
  implements Enumeration
{
  Acl acl;
  Enumeration u1;
  Enumeration u2;
  Enumeration g1;
  Enumeration g2;

  AclEnumerator(Acl paramAcl, Hashtable paramHashtable1, Hashtable paramHashtable2, Hashtable paramHashtable3, Hashtable paramHashtable4)
  {
    this.acl = paramAcl;
    this.u1 = paramHashtable1.elements();
    this.u2 = paramHashtable3.elements();
    this.g1 = paramHashtable2.elements();
    this.g2 = paramHashtable4.elements();
  }

  public boolean hasMoreElements()
  {
    return ((this.u1.hasMoreElements()) || (this.u2.hasMoreElements()) || (this.g1.hasMoreElements()) || (this.g2.hasMoreElements()));
  }

  public Object nextElement()
  {
    synchronized (this.acl)
    {
      if (!(this.u1.hasMoreElements()))
        break label31;
      return this.u1.nextElement();
      label31: if (!(this.u2.hasMoreElements()))
        break label55;
      return this.u2.nextElement();
      label55: if (!(this.g1.hasMoreElements()))
        break label79;
      return this.g1.nextElement();
      label79: if (!(this.g2.hasMoreElements()))
        break label103;
      label103: return this.g2.nextElement();
    }
    throw new NoSuchElementException("Acl Enumerator");
  }
}
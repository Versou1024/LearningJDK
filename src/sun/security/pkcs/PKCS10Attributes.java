package sun.security.pkcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class PKCS10Attributes
  implements DerEncoder
{
  private Hashtable map = new Hashtable(3);

  public PKCS10Attributes()
  {
  }

  public PKCS10Attributes(PKCS10Attribute[] paramArrayOfPKCS10Attribute)
  {
    for (int i = 0; i < paramArrayOfPKCS10Attribute.length; ++i)
      this.map.put(paramArrayOfPKCS10Attribute[i].getAttributeId().toString(), paramArrayOfPKCS10Attribute[i]);
  }

  public PKCS10Attributes(DerInputStream paramDerInputStream)
    throws IOException
  {
    DerValue[] arrayOfDerValue = paramDerInputStream.getSet(3, true);
    if (arrayOfDerValue == null)
      throw new IOException("Illegal encoding of attributes");
    for (int i = 0; i < arrayOfDerValue.length; ++i)
    {
      PKCS10Attribute localPKCS10Attribute = new PKCS10Attribute(arrayOfDerValue[i]);
      this.map.put(localPKCS10Attribute.getAttributeId().toString(), localPKCS10Attribute);
    }
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    derEncode(paramOutputStream);
  }

  public void derEncode(OutputStream paramOutputStream)
    throws IOException
  {
    Collection localCollection = this.map.values();
    PKCS10Attribute[] arrayOfPKCS10Attribute = (PKCS10Attribute[])(PKCS10Attribute[])localCollection.toArray(new PKCS10Attribute[this.map.size()]);
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putOrderedSetOf(DerValue.createTag(-128, true, 0), arrayOfPKCS10Attribute);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void setAttribute(String paramString, Object paramObject)
  {
    this.map.put(paramString, paramObject);
  }

  public Object getAttribute(String paramString)
  {
    return this.map.get(paramString);
  }

  public void deleteAttribute(String paramString)
  {
    this.map.remove(paramString);
  }

  public Enumeration getElements()
  {
    return this.map.elements();
  }

  public Collection getAttributes()
  {
    return Collections.unmodifiableCollection(this.map.values());
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof PKCS10Attributes))
      return false;
    Collection localCollection = ((PKCS10Attributes)paramObject).getAttributes();
    Object[] arrayOfObject = localCollection.toArray();
    int i = arrayOfObject.length;
    if (i != this.map.size())
      return false;
    String str = null;
    for (int j = 0; j < i; ++j)
      if (arrayOfObject[j] instanceof PKCS10Attribute)
      {
        PKCS10Attribute localPKCS10Attribute2 = (PKCS10Attribute)arrayOfObject[j];
        str = localPKCS10Attribute2.getAttributeId().toString();
        if (str == null)
          return false;
        PKCS10Attribute localPKCS10Attribute1 = (PKCS10Attribute)this.map.get(str);
        if (localPKCS10Attribute1 == null)
          return false;
        if (!(localPKCS10Attribute1.equals(localPKCS10Attribute2)))
          return false;
      }
    return true;
  }

  public int hashCode()
  {
    return this.map.hashCode();
  }

  public String toString()
  {
    String str = this.map.size() + "\n" + this.map.toString();
    return str;
  }
}
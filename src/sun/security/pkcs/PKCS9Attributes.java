package sun.security.pkcs;

import B;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Hashtable;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class PKCS9Attributes
{
  private final Hashtable attributes;
  private final Hashtable permittedAttributes;
  private final byte[] derEncoding;
  private boolean ignoreUnsupportedAttributes;

  public PKCS9Attributes(ObjectIdentifier[] paramArrayOfObjectIdentifier, DerInputStream paramDerInputStream)
    throws IOException
  {
    this.attributes = new Hashtable(3);
    this.ignoreUnsupportedAttributes = false;
    if (paramArrayOfObjectIdentifier != null)
    {
      this.permittedAttributes = new Hashtable(paramArrayOfObjectIdentifier.length);
      for (int i = 0; i < paramArrayOfObjectIdentifier.length; ++i)
        this.permittedAttributes.put(paramArrayOfObjectIdentifier[i], paramArrayOfObjectIdentifier[i]);
    }
    else
    {
      this.permittedAttributes = null;
    }
    this.derEncoding = decode(paramDerInputStream);
  }

  public PKCS9Attributes(DerInputStream paramDerInputStream)
    throws IOException
  {
    this(paramDerInputStream, false);
  }

  public PKCS9Attributes(DerInputStream paramDerInputStream, boolean paramBoolean)
    throws IOException
  {
    this.attributes = new Hashtable(3);
    this.ignoreUnsupportedAttributes = false;
    this.ignoreUnsupportedAttributes = paramBoolean;
    this.derEncoding = decode(paramDerInputStream);
    this.permittedAttributes = null;
  }

  public PKCS9Attributes(PKCS9Attribute[] paramArrayOfPKCS9Attribute)
    throws IllegalArgumentException, IOException
  {
    this.attributes = new Hashtable(3);
    this.ignoreUnsupportedAttributes = false;
    for (int i = 0; i < paramArrayOfPKCS9Attribute.length; ++i)
    {
      ObjectIdentifier localObjectIdentifier = paramArrayOfPKCS9Attribute[i].getOID();
      if (this.attributes.containsKey(localObjectIdentifier))
        throw new IllegalArgumentException("PKCSAttribute " + paramArrayOfPKCS9Attribute[i].getOID() + " duplicated while constructing " + "PKCS9Attributes.");
      this.attributes.put(localObjectIdentifier, paramArrayOfPKCS9Attribute[i]);
    }
    this.derEncoding = generateDerEncoding();
    this.permittedAttributes = null;
  }

  private byte[] decode(DerInputStream paramDerInputStream)
    throws IOException
  {
    DerValue localDerValue = paramDerInputStream.getDerValue();
    byte[] arrayOfByte = localDerValue.toByteArray();
    arrayOfByte[0] = 49;
    DerInputStream localDerInputStream = new DerInputStream(arrayOfByte);
    DerValue[] arrayOfDerValue = localDerInputStream.getSet(3, true);
    int i = 1;
    for (int j = 0; j < arrayOfDerValue.length; ++j)
    {
      PKCS9Attribute localPKCS9Attribute;
      try
      {
        localPKCS9Attribute = new PKCS9Attribute(arrayOfDerValue[j]);
      }
      catch (ParsingException localParsingException)
      {
        if (this.ignoreUnsupportedAttributes)
        {
          i = 0;
          break label194:
        }
        throw localParsingException;
      }
      ObjectIdentifier localObjectIdentifier = localPKCS9Attribute.getOID();
      if (this.attributes.get(localObjectIdentifier) != null)
        throw new IOException("Duplicate PKCS9 attribute: " + localObjectIdentifier);
      if ((this.permittedAttributes != null) && (!(this.permittedAttributes.containsKey(localObjectIdentifier))))
        throw new IOException("Attribute " + localObjectIdentifier + " not permitted in this attribute set");
      label194: this.attributes.put(localObjectIdentifier, localPKCS9Attribute);
    }
    return ((i != 0) ? arrayOfByte : generateDerEncoding());
  }

  public void encode(byte paramByte, OutputStream paramOutputStream)
    throws IOException
  {
    paramOutputStream.write(paramByte);
    paramOutputStream.write(this.derEncoding, 1, this.derEncoding.length - 1);
  }

  private byte[] generateDerEncoding()
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    Object[] arrayOfObject = this.attributes.values().toArray();
    localDerOutputStream.putOrderedSetOf(49, castToDerEncoder(arrayOfObject));
    return localDerOutputStream.toByteArray();
  }

  public byte[] getDerEncoding()
    throws IOException
  {
    return ((byte[])(byte[])this.derEncoding.clone());
  }

  public PKCS9Attribute getAttribute(ObjectIdentifier paramObjectIdentifier)
  {
    return ((PKCS9Attribute)this.attributes.get(paramObjectIdentifier));
  }

  public PKCS9Attribute getAttribute(String paramString)
  {
    return ((PKCS9Attribute)this.attributes.get(PKCS9Attribute.getOID(paramString)));
  }

  public PKCS9Attribute[] getAttributes()
  {
    PKCS9Attribute[] arrayOfPKCS9Attribute = new PKCS9Attribute[this.attributes.size()];
    int i = 0;
    for (int j = 1; (j < PKCS9Attribute.PKCS9_OIDS.length) && (i < arrayOfPKCS9Attribute.length); ++j)
    {
      arrayOfPKCS9Attribute[i] = getAttribute(PKCS9Attribute.PKCS9_OIDS[j]);
      if (arrayOfPKCS9Attribute[i] != null)
        ++i;
    }
    return arrayOfPKCS9Attribute;
  }

  public Object getAttributeValue(ObjectIdentifier paramObjectIdentifier)
    throws IOException
  {
    Object localObject;
    try
    {
      localObject = getAttribute(paramObjectIdentifier).getValue();
      return localObject;
    }
    catch (NullPointerException localNullPointerException)
    {
      throw new IOException("No value found for attribute " + paramObjectIdentifier);
    }
  }

  public Object getAttributeValue(String paramString)
    throws IOException
  {
    ObjectIdentifier localObjectIdentifier = PKCS9Attribute.getOID(paramString);
    if (localObjectIdentifier == null)
      throw new IOException("Attribute name " + paramString + " not recognized or not supported.");
    return getAttributeValue(localObjectIdentifier);
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer(200);
    localStringBuffer.append("PKCS9 Attributes: [\n\t");
    int i = 1;
    for (int j = 1; j < PKCS9Attribute.PKCS9_OIDS.length; ++j)
    {
      PKCS9Attribute localPKCS9Attribute = getAttribute(PKCS9Attribute.PKCS9_OIDS[j]);
      if (localPKCS9Attribute == null)
        break label78:
      if (i != 0)
        i = 0;
      else
        localStringBuffer.append(";\n\t");
      label78: localStringBuffer.append(localPKCS9Attribute.toString());
    }
    localStringBuffer.append("\n\t] (end PKCS9 Attributes)");
    return localStringBuffer.toString();
  }

  static DerEncoder[] castToDerEncoder(Object[] paramArrayOfObject)
  {
    DerEncoder[] arrayOfDerEncoder = new DerEncoder[paramArrayOfObject.length];
    for (int i = 0; i < arrayOfDerEncoder.length; ++i)
      arrayOfDerEncoder[i] = ((DerEncoder)paramArrayOfObject[i]);
    return arrayOfDerEncoder;
  }
}
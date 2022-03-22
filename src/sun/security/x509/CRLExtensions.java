package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CRLExtensions
{
  private Hashtable<String, Extension> map = new Hashtable();
  private boolean unsupportedCritExt = false;
  private static final Class[] PARAMS = { Boolean.class, Object.class };

  public CRLExtensions()
  {
  }

  public CRLExtensions(DerInputStream paramDerInputStream)
    throws CRLException
  {
    init(paramDerInputStream);
  }

  private void init(DerInputStream paramDerInputStream)
    throws CRLException
  {
    DerInputStream localDerInputStream;
    try
    {
      localDerInputStream = paramDerInputStream;
      int i = (byte)paramDerInputStream.peekByte();
      if (((i & 0xC0) == 128) && ((i & 0x1F) == 0))
      {
        localObject = localDerInputStream.getDerValue();
        localDerInputStream = ((DerValue)localObject).data;
      }
      Object localObject = localDerInputStream.getSequence(5);
      for (int j = 0; j < localObject.length; ++j)
      {
        Extension localExtension = new Extension(localObject[j]);
        parseExtension(localExtension);
      }
    }
    catch (IOException localIOException)
    {
      throw new CRLException("Parsing error: " + localIOException.toString());
    }
  }

  private void parseExtension(Extension paramExtension)
    throws CRLException
  {
    Class localClass;
    try
    {
      localClass = OIDMap.getClass(paramExtension.getExtensionId());
      if (localClass == null)
      {
        if (paramExtension.isCritical())
          this.unsupportedCritExt = true;
        if (this.map.put(paramExtension.getExtensionId().toString(), paramExtension) != null)
          throw new CRLException("Duplicate extensions not allowed");
        return;
      }
      Constructor localConstructor = localClass.getConstructor(PARAMS);
      Object[] arrayOfObject = { Boolean.valueOf(paramExtension.isCritical()), paramExtension.getExtensionValue() };
      CertAttrSet localCertAttrSet = (CertAttrSet)localConstructor.newInstance(arrayOfObject);
      if (this.map.put(localCertAttrSet.getName(), (Extension)localCertAttrSet) != null)
        throw new CRLException("Duplicate extensions not allowed");
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      throw new CRLException(localInvocationTargetException.getTargetException().getMessage());
    }
    catch (Exception localException)
    {
      throw new CRLException(localException.toString());
    }
  }

  public void encode(OutputStream paramOutputStream, boolean paramBoolean)
    throws CRLException
  {
    DerOutputStream localDerOutputStream1;
    try
    {
      localDerOutputStream1 = new DerOutputStream();
      Collection localCollection = this.map.values();
      Object[] arrayOfObject = localCollection.toArray();
      for (int i = 0; i < arrayOfObject.length; ++i)
        if (arrayOfObject[i] instanceof CertAttrSet)
          ((CertAttrSet)arrayOfObject[i]).encode(localDerOutputStream1);
        else if (arrayOfObject[i] instanceof Extension)
          ((Extension)arrayOfObject[i]).encode(localDerOutputStream1);
        else
          throw new CRLException("Illegal extension object");
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.write(48, localDerOutputStream1);
      DerOutputStream localDerOutputStream3 = new DerOutputStream();
      if (paramBoolean)
        localDerOutputStream3.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
      else
        localDerOutputStream3 = localDerOutputStream2;
      paramOutputStream.write(localDerOutputStream3.toByteArray());
    }
    catch (IOException localIOException)
    {
      throw new CRLException("Encoding error: " + localIOException.toString());
    }
    catch (CertificateException localCertificateException)
    {
      throw new CRLException("Encoding error: " + localCertificateException.toString());
    }
  }

  public Extension get(String paramString)
  {
    String str1;
    X509AttributeName localX509AttributeName = new X509AttributeName(paramString);
    String str2 = localX509AttributeName.getPrefix();
    if (str2.equalsIgnoreCase("x509"))
    {
      int i = paramString.lastIndexOf(".");
      str1 = paramString.substring(i + 1);
    }
    else
    {
      str1 = paramString;
    }
    return ((Extension)this.map.get(str1));
  }

  public void set(String paramString, Object paramObject)
  {
    this.map.put(paramString, (Extension)paramObject);
  }

  public void delete(String paramString)
  {
    this.map.remove(paramString);
  }

  public Enumeration<Extension> getElements()
  {
    return this.map.elements();
  }

  public Collection<Extension> getAllExtensions()
  {
    return this.map.values();
  }

  public boolean hasUnsupportedCriticalExtension()
  {
    return this.unsupportedCritExt;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof CRLExtensions))
      return false;
    Collection localCollection = ((CRLExtensions)paramObject).getAllExtensions();
    Object[] arrayOfObject = localCollection.toArray();
    int i = arrayOfObject.length;
    if (i != this.map.size())
      return false;
    String str = null;
    for (int j = 0; j < i; ++j)
    {
      if (arrayOfObject[j] instanceof CertAttrSet)
        str = ((CertAttrSet)arrayOfObject[j]).getName();
      Extension localExtension1 = (Extension)arrayOfObject[j];
      if (str == null)
        str = localExtension1.getExtensionId().toString();
      Extension localExtension2 = (Extension)this.map.get(str);
      if (localExtension2 == null)
        return false;
      if (!(localExtension2.equals(localExtension1)))
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
    return this.map.toString();
  }
}
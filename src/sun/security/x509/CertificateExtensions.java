package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CertificateExtensions
  implements CertAttrSet<Extension>
{
  public static final String IDENT = "x509.info.extensions";
  public static final String NAME = "extensions";
  private static final Debug debug = Debug.getInstance("x509");
  private Hashtable<String, Extension> map = new Hashtable();
  private boolean unsupportedCritExt = false;
  private Map<String, Extension> unparseableExtensions;
  private static Class[] PARAMS = { Boolean.class, Object.class };

  public CertificateExtensions()
  {
  }

  public CertificateExtensions(DerInputStream paramDerInputStream)
    throws IOException
  {
    init(paramDerInputStream);
  }

  private void init(DerInputStream paramDerInputStream)
    throws IOException
  {
    DerValue[] arrayOfDerValue = paramDerInputStream.getSequence(5);
    for (int i = 0; i < arrayOfDerValue.length; ++i)
    {
      Extension localExtension = new Extension(arrayOfDerValue[i]);
      parseExtension(localExtension);
    }
  }

  private void parseExtension(Extension paramExtension)
    throws IOException
  {
    Class localClass;
    Object localObject1;
    Object localObject2;
    try
    {
      localClass = OIDMap.getClass(paramExtension.getExtensionId());
      if (localClass == null)
      {
        if (paramExtension.isCritical())
          this.unsupportedCritExt = true;
        if (this.map.put(paramExtension.getExtensionId().toString(), paramExtension) == null)
          return;
        throw new IOException("Duplicate extensions not allowed");
      }
      localObject1 = localClass.getConstructor(PARAMS);
      localObject2 = { Boolean.valueOf(paramExtension.isCritical()), paramExtension.getExtensionValue() };
      CertAttrSet localCertAttrSet = (CertAttrSet)((Constructor)localObject1).newInstance(localObject2);
      if (this.map.put(localCertAttrSet.getName(), (Extension)localCertAttrSet) != null)
        throw new IOException("Duplicate extensions not allowed");
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      localObject1 = localInvocationTargetException.getTargetException();
      if (!(paramExtension.isCritical()))
      {
        if (this.unparseableExtensions == null)
          this.unparseableExtensions = new HashMap();
        this.unparseableExtensions.put(paramExtension.getExtensionId().toString(), new UnparseableExtension(paramExtension, (Throwable)localObject1));
        if (debug != null)
        {
          debug.println("Error parsing extension: " + paramExtension);
          ((Throwable)localObject1).printStackTrace();
          localObject2 = new HexDumpEncoder();
          System.err.println(((HexDumpEncoder)localObject2).encodeBuffer(paramExtension.getExtensionValue()));
        }
        return;
      }
      if (localObject1 instanceof IOException)
        throw ((IOException)localObject1);
      throw ((IOException)new IOException(((Throwable)localObject1).toString()).initCause((Throwable)localObject1));
    }
    catch (IOException localIOException)
    {
      throw localIOException;
    }
    catch (Exception localException)
    {
      throw ((IOException)new IOException(localException.toString()).initCause(localException));
    }
  }

  public void encode(OutputStream paramOutputStream)
    throws CertificateException, IOException
  {
    encode(paramOutputStream, false);
  }

  public void encode(OutputStream paramOutputStream, boolean paramBoolean)
    throws CertificateException, IOException
  {
    DerOutputStream localDerOutputStream3;
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    Collection localCollection = this.map.values();
    Object[] arrayOfObject = localCollection.toArray();
    for (int i = 0; i < arrayOfObject.length; ++i)
      if (arrayOfObject[i] instanceof CertAttrSet)
        ((CertAttrSet)arrayOfObject[i]).encode(localDerOutputStream1);
      else if (arrayOfObject[i] instanceof Extension)
        ((Extension)arrayOfObject[i]).encode(localDerOutputStream1);
      else
        throw new CertificateException("Illegal extension object");
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    if (!(paramBoolean))
    {
      localDerOutputStream3 = new DerOutputStream();
      localDerOutputStream3.write(DerValue.createTag(-128, true, 3), localDerOutputStream2);
    }
    else
    {
      localDerOutputStream3 = localDerOutputStream2;
    }
    paramOutputStream.write(localDerOutputStream3.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramObject instanceof Extension)
      this.map.put(paramString, (Extension)paramObject);
    else
      throw new IOException("Unknown extension type.");
  }

  public Object get(String paramString)
    throws IOException
  {
    Object localObject = this.map.get(paramString);
    if (localObject == null)
      throw new IOException("No extension found with name " + paramString);
    return localObject;
  }

  public void delete(String paramString)
    throws IOException
  {
    Object localObject = this.map.get(paramString);
    if (localObject == null)
      throw new IOException("No extension found with name " + paramString);
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

  public Map<String, Extension> getUnparseableExtensions()
  {
    if (this.unparseableExtensions == null)
      return Collections.emptyMap();
    return this.unparseableExtensions;
  }

  public String getName()
  {
    return "extensions";
  }

  public boolean hasUnsupportedCriticalExtension()
  {
    return this.unsupportedCritExt;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof CertificateExtensions))
      return false;
    Collection localCollection = ((CertificateExtensions)paramObject).getAllExtensions();
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
    return getUnparseableExtensions().equals(((CertificateExtensions)paramObject).getUnparseableExtensions());
  }

  public int hashCode()
  {
    return (this.map.hashCode() + getUnparseableExtensions().hashCode());
  }

  public String toString()
  {
    return this.map.toString();
  }
}
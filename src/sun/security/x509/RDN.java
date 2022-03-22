package sun.security.x509;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class RDN
{
  final AVA[] assertion;
  private volatile List<AVA> avaList;
  private volatile String canonicalString;

  public RDN(String paramString)
    throws IOException
  {
    this(paramString, Collections.EMPTY_MAP);
  }

  public RDN(String paramString, Map<String, String> paramMap)
    throws IOException
  {
    int i = 0;
    int j = 0;
    int k = 0;
    ArrayList localArrayList = new ArrayList(3);
    for (int l = paramString.indexOf(43); l >= 0; l = paramString.indexOf(43, j))
    {
      i += X500Name.countQuotes(paramString, j, l);
      if ((l > 0) && (paramString.charAt(l - 1) != '\\') && (i != 1))
      {
        str = paramString.substring(k, l);
        if (str.length() == 0)
          throw new IOException("empty AVA in RDN \"" + paramString + "\"");
        localAVA = new AVA(new StringReader(str), paramMap);
        localArrayList.add(localAVA);
        k = l + 1;
        i = 0;
      }
      j = l + 1;
    }
    String str = paramString.substring(k);
    if (str.length() == 0)
      throw new IOException("empty AVA in RDN \"" + paramString + "\"");
    AVA localAVA = new AVA(new StringReader(str), paramMap);
    localArrayList.add(localAVA);
    this.assertion = ((AVA[])localArrayList.toArray(new AVA[localArrayList.size()]));
  }

  RDN(String paramString1, String paramString2)
    throws IOException
  {
    this(paramString1, paramString2, Collections.EMPTY_MAP);
  }

  RDN(String paramString1, String paramString2, Map<String, String> paramMap)
    throws IOException
  {
    if (!(paramString2.equalsIgnoreCase("RFC2253")))
      throw new IOException("Unsupported format " + paramString2);
    int i = 0;
    int j = 0;
    ArrayList localArrayList = new ArrayList(3);
    for (int k = paramString1.indexOf(43); k >= 0; k = paramString1.indexOf(43, i))
    {
      if ((k > 0) && (paramString1.charAt(k - 1) != '\\'))
      {
        str = paramString1.substring(j, k);
        if (str.length() == 0)
          throw new IOException("empty AVA in RDN \"" + paramString1 + "\"");
        localAVA = new AVA(new StringReader(str), 3, paramMap);
        localArrayList.add(localAVA);
        j = k + 1;
      }
      i = k + 1;
    }
    String str = paramString1.substring(j);
    if (str.length() == 0)
      throw new IOException("empty AVA in RDN \"" + paramString1 + "\"");
    AVA localAVA = new AVA(new StringReader(str), 3, paramMap);
    localArrayList.add(localAVA);
    this.assertion = ((AVA[])localArrayList.toArray(new AVA[localArrayList.size()]));
  }

  RDN(DerValue paramDerValue)
    throws IOException
  {
    if (paramDerValue.tag != 49)
      throw new IOException("X500 RDN");
    DerInputStream localDerInputStream = new DerInputStream(paramDerValue.toByteArray());
    DerValue[] arrayOfDerValue = localDerInputStream.getSet(5);
    this.assertion = new AVA[arrayOfDerValue.length];
    for (int i = 0; i < arrayOfDerValue.length; ++i)
      this.assertion[i] = new AVA(arrayOfDerValue[i]);
  }

  RDN(int paramInt)
  {
    this.assertion = new AVA[paramInt];
  }

  public RDN(AVA paramAVA)
  {
    if (paramAVA == null)
      throw new NullPointerException();
    this.assertion = { paramAVA };
  }

  public RDN(AVA[] paramArrayOfAVA)
  {
    this.assertion = ((AVA[])(AVA[])paramArrayOfAVA.clone());
    for (int i = 0; i < this.assertion.length; ++i)
      if (this.assertion[i] == null)
        throw new NullPointerException();
  }

  public List<AVA> avas()
  {
    List localList = this.avaList;
    if (localList == null)
    {
      localList = Collections.unmodifiableList(Arrays.asList(this.assertion));
      this.avaList = localList;
    }
    return localList;
  }

  public int size()
  {
    return this.assertion.length;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof RDN))
      return false;
    RDN localRDN = (RDN)paramObject;
    if (this.assertion.length != localRDN.assertion.length)
      return false;
    String str1 = toRFC2253String(true);
    String str2 = localRDN.toRFC2253String(true);
    return str1.equals(str2);
  }

  public int hashCode()
  {
    return toRFC2253String(true).hashCode();
  }

  DerValue findAttribute(ObjectIdentifier paramObjectIdentifier)
  {
    for (int i = 0; i < this.assertion.length; ++i)
      if (this.assertion[i].oid.equals(paramObjectIdentifier))
        return this.assertion[i].value;
    return null;
  }

  void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    paramDerOutputStream.putOrderedSetOf(49, this.assertion);
  }

  public String toString()
  {
    if (this.assertion.length == 1)
      return this.assertion[0].toString();
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; i < this.assertion.length; ++i)
    {
      if (i != 0)
        localStringBuilder.append(" + ");
      localStringBuilder.append(this.assertion[i].toString());
    }
    return localStringBuilder.toString();
  }

  public String toRFC1779String()
  {
    return toRFC1779String(Collections.EMPTY_MAP);
  }

  public String toRFC1779String(Map<String, String> paramMap)
  {
    if (this.assertion.length == 1)
      return this.assertion[0].toRFC1779String(paramMap);
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; i < this.assertion.length; ++i)
    {
      if (i != 0)
        localStringBuilder.append(" + ");
      localStringBuilder.append(this.assertion[i].toRFC1779String(paramMap));
    }
    return localStringBuilder.toString();
  }

  public String toRFC2253String()
  {
    return toRFC2253StringInternal(false, Collections.EMPTY_MAP);
  }

  public String toRFC2253String(Map<String, String> paramMap)
  {
    return toRFC2253StringInternal(false, paramMap);
  }

  public String toRFC2253String(boolean paramBoolean)
  {
    if (!(paramBoolean))
      return toRFC2253StringInternal(false, Collections.EMPTY_MAP);
    String str = this.canonicalString;
    if (str == null)
    {
      str = toRFC2253StringInternal(true, Collections.EMPTY_MAP);
      this.canonicalString = str;
    }
    return str;
  }

  private String toRFC2253StringInternal(boolean paramBoolean, Map<String, String> paramMap)
  {
    if (this.assertion.length == 1)
      return ((paramBoolean) ? this.assertion[0].toRFC2253CanonicalString() : this.assertion[0].toRFC2253String(paramMap));
    StringBuilder localStringBuilder = new StringBuilder();
    if (!(paramBoolean))
    {
      for (int i = 0; i < this.assertion.length; ++i)
      {
        if (i > 0)
          localStringBuilder.append('+');
        localStringBuilder.append(this.assertion[i].toRFC2253String(paramMap));
      }
    }
    else
    {
      ArrayList localArrayList = new ArrayList(this.assertion.length);
      for (int j = 0; j < this.assertion.length; ++j)
        localArrayList.add(this.assertion[j]);
      Collections.sort(localArrayList, AVAComparator.getInstance());
      for (j = 0; j < localArrayList.size(); ++j)
      {
        if (j > 0)
          localStringBuilder.append('+');
        localStringBuilder.append(((AVA)localArrayList.get(j)).toRFC2253CanonicalString());
      }
    }
    return localStringBuilder.toString();
  }
}
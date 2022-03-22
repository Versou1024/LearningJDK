package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class GeneralNames
{
  private final List<GeneralName> names;

  public GeneralNames(DerValue paramDerValue)
    throws IOException
  {
    if (paramDerValue.tag != 48)
      throw new IOException("Invalid encoding for GeneralNames.");
    if (paramDerValue.data.available() == 0)
      throw new IOException("No data available in passed DER encoded value.");
    while (paramDerValue.data.available() != 0)
    {
      DerValue localDerValue = paramDerValue.data.getDerValue();
      GeneralName localGeneralName = new GeneralName(localDerValue);
      add(localGeneralName);
    }
  }

  public GeneralNames()
  {
    this.names = new ArrayList();
  }

  public GeneralNames add(GeneralName paramGeneralName)
  {
    if (paramGeneralName == null)
      throw new NullPointerException();
    this.names.add(paramGeneralName);
    return this;
  }

  public GeneralName get(int paramInt)
  {
    return ((GeneralName)this.names.get(paramInt));
  }

  public boolean isEmpty()
  {
    return this.names.isEmpty();
  }

  public int size()
  {
    return this.names.size();
  }

  public Iterator<GeneralName> iterator()
  {
    return this.names.iterator();
  }

  public List<GeneralName> names()
  {
    return this.names;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    if (isEmpty())
      return;
    DerOutputStream localDerOutputStream = new DerOutputStream();
    Iterator localIterator = this.names.iterator();
    while (localIterator.hasNext())
    {
      GeneralName localGeneralName = (GeneralName)localIterator.next();
      localGeneralName.encode(localDerOutputStream);
    }
    paramDerOutputStream.write(48, localDerOutputStream);
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof GeneralNames))
      return false;
    GeneralNames localGeneralNames = (GeneralNames)paramObject;
    return this.names.equals(localGeneralNames.names);
  }

  public int hashCode()
  {
    return this.names.hashCode();
  }

  public String toString()
  {
    return this.names.toString();
  }
}
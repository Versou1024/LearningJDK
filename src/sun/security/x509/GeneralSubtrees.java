package sun.security.x509;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class GeneralSubtrees
  implements Cloneable
{
  private final List<GeneralSubtree> trees;
  private static final int NAME_DIFF_TYPE = -1;
  private static final int NAME_MATCH = 0;
  private static final int NAME_NARROWS = 1;
  private static final int NAME_WIDENS = 2;
  private static final int NAME_SAME_TYPE = 3;

  public GeneralSubtrees()
  {
    this.trees = new ArrayList();
  }

  private GeneralSubtrees(GeneralSubtrees paramGeneralSubtrees)
  {
    this.trees = new ArrayList(paramGeneralSubtrees.trees);
  }

  public GeneralSubtrees(DerValue paramDerValue)
    throws IOException
  {
    if (paramDerValue.tag != 48)
      throw new IOException("Invalid encoding of GeneralSubtrees.");
    while (paramDerValue.data.available() != 0)
    {
      DerValue localDerValue = paramDerValue.data.getDerValue();
      GeneralSubtree localGeneralSubtree = new GeneralSubtree(localDerValue);
      add(localGeneralSubtree);
    }
  }

  public GeneralSubtree get(int paramInt)
  {
    return ((GeneralSubtree)this.trees.get(paramInt));
  }

  public void remove(int paramInt)
  {
    this.trees.remove(paramInt);
  }

  public void add(GeneralSubtree paramGeneralSubtree)
  {
    if (paramGeneralSubtree == null)
      throw new NullPointerException();
    this.trees.add(paramGeneralSubtree);
  }

  public boolean contains(GeneralSubtree paramGeneralSubtree)
  {
    if (paramGeneralSubtree == null)
      throw new NullPointerException();
    return this.trees.contains(paramGeneralSubtree);
  }

  public int size()
  {
    return this.trees.size();
  }

  public Iterator<GeneralSubtree> iterator()
  {
    return this.trees.iterator();
  }

  public List<GeneralSubtree> trees()
  {
    return this.trees;
  }

  public Object clone()
  {
    return new GeneralSubtrees(this);
  }

  public String toString()
  {
    String str = "   GeneralSubtrees:\n" + this.trees.toString() + "\n";
    return str;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    int i = 0;
    int j = size();
    while (i < j)
    {
      get(i).encode(localDerOutputStream);
      ++i;
    }
    paramDerOutputStream.write(48, localDerOutputStream);
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof GeneralSubtrees))
      return false;
    GeneralSubtrees localGeneralSubtrees = (GeneralSubtrees)paramObject;
    return this.trees.equals(localGeneralSubtrees.trees);
  }

  public int hashCode()
  {
    return this.trees.hashCode();
  }

  private GeneralNameInterface getGeneralNameInterface(int paramInt)
  {
    return getGeneralNameInterface(get(paramInt));
  }

  private static GeneralNameInterface getGeneralNameInterface(GeneralSubtree paramGeneralSubtree)
  {
    GeneralName localGeneralName = paramGeneralSubtree.getName();
    GeneralNameInterface localGeneralNameInterface = localGeneralName.getName();
    return localGeneralNameInterface;
  }

  private void minimize()
  {
    for (int i = 0; i < size(); ++i)
    {
      GeneralNameInterface localGeneralNameInterface1 = getGeneralNameInterface(i);
      int j = 0;
      for (int k = i + 1; k < size(); ++k)
      {
        GeneralNameInterface localGeneralNameInterface2 = getGeneralNameInterface(k);
        switch (localGeneralNameInterface1.constrains(localGeneralNameInterface2))
        {
        case -1:
          break;
        case 0:
          j = 1;
          break;
        case 1:
          remove(k);
          --k;
          break;
        case 2:
          j = 1;
          break;
        case 3:
          break;
        default:
          break;
        }
      }
      if (j != 0)
      {
        remove(i);
        --i;
      }
    }
  }

  private GeneralSubtree createWidestSubtree(GeneralNameInterface paramGeneralNameInterface)
  {
    try
    {
      GeneralName localGeneralName;
      switch (paramGeneralNameInterface.getType())
      {
      case 0:
        ObjectIdentifier localObjectIdentifier = ((OtherName)paramGeneralNameInterface).getOID();
        localGeneralName = new GeneralName(new OtherName(localObjectIdentifier, null));
        break;
      case 1:
        localGeneralName = new GeneralName(new RFC822Name(""));
        break;
      case 2:
        localGeneralName = new GeneralName(new DNSName(""));
        break;
      case 3:
        localGeneralName = new GeneralName(new X400Address((byte[])null));
        break;
      case 4:
        localGeneralName = new GeneralName(new X500Name(""));
        break;
      case 5:
        localGeneralName = new GeneralName(new EDIPartyName(""));
        break;
      case 6:
        localGeneralName = new GeneralName(new URIName(""));
        break;
      case 7:
        localGeneralName = new GeneralName(new IPAddressName((byte[])null));
        break;
      case 8:
        localGeneralName = new GeneralName(new OIDName(new ObjectIdentifier((int[])null)));
        break;
      default:
        throw new IOException("Unsupported GeneralNameInterface type: " + paramGeneralNameInterface.getType());
      }
      return new GeneralSubtree(localGeneralName, 0, -1);
    }
    catch (IOException localIOException)
    {
      throw new RuntimeException("Unexpected error: " + localIOException, localIOException);
    }
  }

  public GeneralSubtrees intersect(GeneralSubtrees paramGeneralSubtrees)
  {
    Object localObject1;
    int k;
    int l;
    Object localObject2;
    if (paramGeneralSubtrees == null)
      throw new NullPointerException("other GeneralSubtrees must not be null");
    GeneralSubtrees localGeneralSubtrees1 = new GeneralSubtrees();
    GeneralSubtrees localGeneralSubtrees2 = null;
    if (size() == 0)
    {
      union(paramGeneralSubtrees);
      return null;
    }
    minimize();
    paramGeneralSubtrees.minimize();
    for (int i = 0; i < size(); ++i)
    {
      GeneralNameInterface localGeneralNameInterface2;
      localObject1 = getGeneralNameInterface(i);
      int j = 0;
      k = 0;
      l = 0;
      while (l < paramGeneralSubtrees.size())
      {
        GeneralSubtree localGeneralSubtree = paramGeneralSubtrees.get(l);
        localGeneralNameInterface2 = getGeneralNameInterface(localGeneralSubtree);
        switch (((GeneralNameInterface)localObject1).constrains(localGeneralNameInterface2))
        {
        case 1:
          remove(i);
          --i;
          localGeneralSubtrees1.add(localGeneralSubtree);
          k = 0;
          break;
        case 3:
          k = 1;
          break;
        case 0:
        case 2:
          k = 0;
          break;
        case -1:
        default:
          ++l;
        }
      }
      if (k != 0)
      {
        l = 0;
        for (int i1 = 0; i1 < size(); ++i1)
        {
          localGeneralNameInterface2 = getGeneralNameInterface(i1);
          if (localGeneralNameInterface2.getType() == ((GeneralNameInterface)localObject1).getType())
            for (int i2 = 0; i2 < paramGeneralSubtrees.size(); ++i2)
            {
              GeneralNameInterface localGeneralNameInterface3 = paramGeneralSubtrees.getGeneralNameInterface(i2);
              int i3 = localGeneralNameInterface2.constrains(localGeneralNameInterface3);
              if ((i3 == 0) || (i3 == 2) || (i3 == 1))
              {
                l = 1;
                break;
              }
            }
        }
        if (l == 0)
        {
          if (localGeneralSubtrees2 == null)
            localGeneralSubtrees2 = new GeneralSubtrees();
          localObject2 = createWidestSubtree((GeneralNameInterface)localObject1);
          if (!(localGeneralSubtrees2.contains((GeneralSubtree)localObject2)))
            localGeneralSubtrees2.add((GeneralSubtree)localObject2);
        }
        remove(i);
        --i;
      }
    }
    if (localGeneralSubtrees1.size() > 0)
      union(localGeneralSubtrees1);
    for (i = 0; i < paramGeneralSubtrees.size(); ++i)
    {
      localObject1 = paramGeneralSubtrees.get(i);
      GeneralNameInterface localGeneralNameInterface1 = getGeneralNameInterface((GeneralSubtree)localObject1);
      k = 0;
      l = 0;
      while (l < size())
      {
        localObject2 = getGeneralNameInterface(l);
        switch (((GeneralNameInterface)localObject2).constrains(localGeneralNameInterface1))
        {
        case -1:
          k = 1;
          break;
        case 0:
        case 1:
        case 2:
        case 3:
          k = 0;
          break;
        default:
          ++l;
        }
      }
      if (k != 0)
        add((GeneralSubtree)localObject1);
    }
    return ((GeneralSubtrees)(GeneralSubtrees)localGeneralSubtrees2);
  }

  public void union(GeneralSubtrees paramGeneralSubtrees)
  {
    if (paramGeneralSubtrees != null)
    {
      int i = 0;
      int j = paramGeneralSubtrees.size();
      while (i < j)
      {
        add(paramGeneralSubtrees.get(i));
        ++i;
      }
      minimize();
    }
  }

  public void reduce(GeneralSubtrees paramGeneralSubtrees)
  {
    if (paramGeneralSubtrees == null)
      return;
    int i = 0;
    int j = paramGeneralSubtrees.size();
    while (i < j)
    {
      GeneralNameInterface localGeneralNameInterface1 = paramGeneralSubtrees.getGeneralNameInterface(i);
      for (int k = 0; k < size(); ++k)
      {
        GeneralNameInterface localGeneralNameInterface2 = getGeneralNameInterface(k);
        switch (localGeneralNameInterface1.constrains(localGeneralNameInterface2))
        {
        case -1:
          break;
        case 0:
          remove(k);
          --k;
          break;
        case 1:
          remove(k);
          --k;
          break;
        case 2:
        case 3:
        }
      }
      ++i;
    }
  }
}
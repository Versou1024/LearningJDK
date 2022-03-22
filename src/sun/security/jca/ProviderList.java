package sun.security.jca;

import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import sun.security.util.Debug;

public final class ProviderList
{
  static final Debug debug = Debug.getInstance("jca", "ProviderList");
  private static final ProviderConfig[] PC0 = new ProviderConfig[0];
  private static final Provider[] P0 = new Provider[0];
  static final ProviderList EMPTY = new ProviderList(PC0, true);
  private static final Provider EMPTY_PROVIDER = new Provider("##Empty##", 1D, "initialization in progress")
  {
    public Provider.Service getService(String paramString1, String paramString2)
    {
      return null;
    }
  };
  private final ProviderConfig[] configs;
  private volatile boolean allLoaded;
  private final List<Provider> userList = new AbstractList(???)
  {
    public int size()
    {
      return ProviderList.access$100(this.this$0).length;
    }

    public Provider get()
    {
      return this.this$0.getProvider(paramInt);
    }
  };

  static ProviderList fromSecurityProperties()
  {
    Object localObject = AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return new ProviderList(null);
      }
    });
    return ((ProviderList)localObject);
  }

  public static ProviderList add(ProviderList paramProviderList, Provider paramProvider)
  {
    return insertAt(paramProviderList, paramProvider, -1);
  }

  public static ProviderList insertAt(ProviderList paramProviderList, Provider paramProvider, int paramInt)
  {
    if (paramProviderList.getProvider(paramProvider.getName()) != null)
      return paramProviderList;
    ArrayList localArrayList = new ArrayList(Arrays.asList(paramProviderList.configs));
    int i = localArrayList.size();
    if ((paramInt < 0) || (paramInt > i))
      paramInt = i;
    localArrayList.add(paramInt, new ProviderConfig(paramProvider));
    return new ProviderList((ProviderConfig[])localArrayList.toArray(PC0), true);
  }

  public static ProviderList remove(ProviderList paramProviderList, String paramString)
  {
    if (paramProviderList.getProvider(paramString) == null)
      return paramProviderList;
    ProviderConfig[] arrayOfProviderConfig1 = new ProviderConfig[paramProviderList.size() - 1];
    int i = 0;
    ProviderConfig[] arrayOfProviderConfig2 = paramProviderList.configs;
    int j = arrayOfProviderConfig2.length;
    for (int k = 0; k < j; ++k)
    {
      ProviderConfig localProviderConfig = arrayOfProviderConfig2[k];
      if (!(localProviderConfig.getProvider().getName().equals(paramString)))
        arrayOfProviderConfig1[(i++)] = localProviderConfig;
    }
    return new ProviderList(arrayOfProviderConfig1, true);
  }

  public static ProviderList newList(Provider[] paramArrayOfProvider)
  {
    ProviderConfig[] arrayOfProviderConfig = new ProviderConfig[paramArrayOfProvider.length];
    for (int i = 0; i < paramArrayOfProvider.length; ++i)
      arrayOfProviderConfig[i] = new ProviderConfig(paramArrayOfProvider[i]);
    return new ProviderList(arrayOfProviderConfig, true);
  }

  private ProviderList(ProviderConfig[] paramArrayOfProviderConfig, boolean paramBoolean)
  {
    this.configs = paramArrayOfProviderConfig;
    this.allLoaded = paramBoolean;
  }

  private ProviderList()
  {
    ArrayList localArrayList = new ArrayList();
    int i = 1;
    while (true)
    {
      ProviderConfig localProviderConfig;
      String str1 = Security.getProperty("security.provider." + i);
      if (str1 == null)
        break;
      str1 = str1.trim();
      if (str1.length() == 0)
      {
        System.err.println("invalid entry for security.provider." + i);
        break;
      }
      int j = str1.indexOf(32);
      if (j == -1)
      {
        localProviderConfig = new ProviderConfig(str1);
      }
      else
      {
        String str2 = str1.substring(0, j);
        String str3 = str1.substring(j + 1).trim();
        localProviderConfig = new ProviderConfig(str2, str3);
      }
      if (!(localArrayList.contains(localProviderConfig)))
        localArrayList.add(localProviderConfig);
      ++i;
    }
    this.configs = ((ProviderConfig[])(ProviderConfig[])localArrayList.toArray(PC0));
    if (debug != null)
      debug.println("provider configuration: " + localArrayList);
  }

  ProviderList getJarList(String[] paramArrayOfString)
  {
    ArrayList localArrayList = new ArrayList();
    Object localObject1 = paramArrayOfString;
    int i = localObject1.length;
    for (int j = 0; j < i; ++j)
    {
      String str = localObject1[j];
      Object localObject2 = new ProviderConfig(str);
      ProviderConfig[] arrayOfProviderConfig = this.configs;
      int k = arrayOfProviderConfig.length;
      for (int l = 0; l < k; ++l)
      {
        ProviderConfig localProviderConfig = arrayOfProviderConfig[l];
        if (localProviderConfig.equals(localObject2))
        {
          localObject2 = localProviderConfig;
          break;
        }
      }
      localArrayList.add(localObject2);
    }
    localObject1 = (ProviderConfig[])(ProviderConfig[])localArrayList.toArray(PC0);
    return ((ProviderList)(ProviderList)new ProviderList(localObject1, false));
  }

  public int size()
  {
    return this.configs.length;
  }

  Provider getProvider(int paramInt)
  {
    Provider localProvider = this.configs[paramInt].getProvider();
    return ((localProvider != null) ? localProvider : EMPTY_PROVIDER);
  }

  public List<Provider> providers()
  {
    return this.userList;
  }

  private ProviderConfig getProviderConfig(String paramString)
  {
    int i = getIndex(paramString);
    return ((i != -1) ? this.configs[i] : null);
  }

  public Provider getProvider(String paramString)
  {
    ProviderConfig localProviderConfig = getProviderConfig(paramString);
    return ((localProviderConfig == null) ? null : localProviderConfig.getProvider());
  }

  public int getIndex(String paramString)
  {
    for (int i = 0; i < this.configs.length; ++i)
    {
      Provider localProvider = getProvider(i);
      if (localProvider.getName().equals(paramString))
        return i;
    }
    return -1;
  }

  private int loadAll()
  {
    if (this.allLoaded)
      return this.configs.length;
    if (debug != null)
    {
      debug.println("Loading all providers");
      new Exception("Call trace").printStackTrace();
    }
    int i = 0;
    for (int j = 0; j < this.configs.length; ++j)
    {
      Provider localProvider = this.configs[j].getProvider();
      if (localProvider != null)
        ++i;
    }
    if (i == this.configs.length)
      this.allLoaded = true;
    return i;
  }

  ProviderList removeInvalid()
  {
    int i = loadAll();
    if (i == this.configs.length)
      return this;
    ProviderConfig[] arrayOfProviderConfig = new ProviderConfig[i];
    int j = 0;
    int k = 0;
    while (j < this.configs.length)
    {
      ProviderConfig localProviderConfig = this.configs[j];
      if (localProviderConfig.isLoaded())
        arrayOfProviderConfig[(k++)] = localProviderConfig;
      ++j;
    }
    return new ProviderList(arrayOfProviderConfig, true);
  }

  public Provider[] toArray()
  {
    return ((Provider[])providers().toArray(P0));
  }

  public String toString()
  {
    return Arrays.asList(this.configs).toString();
  }

  public Provider.Service getService(String paramString1, String paramString2)
  {
    for (int i = 0; i < this.configs.length; ++i)
    {
      Provider localProvider = getProvider(i);
      Provider.Service localService = localProvider.getService(paramString1, paramString2);
      if (localService != null)
        return localService;
    }
    return null;
  }

  public List<Provider.Service> getServices(String paramString1, String paramString2)
  {
    return new ServiceList(this, paramString1, paramString2);
  }

  @Deprecated
  public List<Provider.Service> getServices(String paramString, List<String> paramList)
  {
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = paramList.iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      localArrayList.add(new ServiceId(paramString, str));
    }
    return getServices(localArrayList);
  }

  public List<Provider.Service> getServices(List<ServiceId> paramList)
  {
    return new ServiceList(this, paramList);
  }

  private final class ServiceList extends AbstractList<Provider.Service>
  {
    private final String type;
    private final String algorithm;
    private final List<ServiceId> ids;
    private Provider.Service firstService;
    private List<Provider.Service> services;
    private int providerIndex;

    ServiceList(, String paramString1, String paramString2)
    {
      this.type = paramString1;
      this.algorithm = paramString2;
      this.ids = null;
    }

    ServiceList()
    {
      this.type = null;
      this.algorithm = null;
      this.ids = localObject;
    }

    private void addService()
    {
      if (this.firstService == null)
      {
        this.firstService = paramService;
      }
      else
      {
        if (this.services == null)
        {
          this.services = new ArrayList(4);
          this.services.add(this.firstService);
        }
        this.services.add(paramService);
      }
    }

    private Provider.Service tryGet()
    {
      while (true)
      {
        Object localObject;
        if ((paramInt == 0) && (this.firstService != null))
          return this.firstService;
        if ((this.services != null) && (this.services.size() > paramInt))
          return ((Provider.Service)this.services.get(paramInt));
        if (this.providerIndex >= ProviderList.access$100(this.this$0).length)
          return null;
        Provider localProvider = this.this$0.getProvider(this.providerIndex++);
        if (this.type != null)
        {
          localObject = localProvider.getService(this.type, this.algorithm);
          if (localObject != null)
            addService((Provider.Service)localObject);
        }
        else
        {
          localObject = this.ids.iterator();
          while (((Iterator)localObject).hasNext())
          {
            ServiceId localServiceId = (ServiceId)((Iterator)localObject).next();
            Provider.Service localService = localProvider.getService(localServiceId.type, localServiceId.algorithm);
            if (localService != null)
              addService(localService);
          }
        }
      }
    }

    public Provider.Service get()
    {
      Provider.Service localService = tryGet(paramInt);
      if (localService == null)
        throw new IndexOutOfBoundsException();
      return localService;
    }

    public int size()
    {
      if (this.services != null)
        i = this.services.size();
      for (int i = (this.firstService != null) ? 1 : 0; tryGet(i) != null; ++i);
      return i;
    }

    public boolean isEmpty()
    {
      return (tryGet(0) == null);
    }

    public Iterator<Provider.Service> iterator()
    {
      return new Iterator(this)
      {
        int index;

        public boolean hasNext()
        {
          return (ProviderList.ServiceList.access$200(this.this$1, this.index) != null);
        }

        public Provider.Service next()
        {
          Provider.Service localService = ProviderList.ServiceList.access$200(this.this$1, this.index);
          if (localService == null)
            throw new NoSuchElementException();
          this.index += 1;
          return localService;
        }

        public void remove()
        {
          throw new UnsupportedOperationException();
        }
      };
    }
  }
}
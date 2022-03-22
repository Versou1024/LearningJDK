package sun.net.spi.nameservice;

public abstract interface NameServiceDescriptor
{
  public abstract NameService createNameService()
    throws Exception;

  public abstract String getProviderName();

  public abstract String getType();
}
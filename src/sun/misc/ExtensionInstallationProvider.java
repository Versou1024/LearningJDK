package sun.misc;

public abstract interface ExtensionInstallationProvider
{
  public abstract boolean installExtension(ExtensionInfo paramExtensionInfo1, ExtensionInfo paramExtensionInfo2)
    throws sun.misc.ExtensionInstallationException;
}
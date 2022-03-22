package sun.security.validator;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class KeyStores
{
  public static Set getTrustedCerts(KeyStore paramKeyStore)
  {
    HashSet localHashSet = new HashSet();
    try
    {
      Enumeration localEnumeration = paramKeyStore.aliases();
      while (localEnumeration.hasMoreElements())
      {
        Object localObject;
        String str = (String)localEnumeration.nextElement();
        if (paramKeyStore.isCertificateEntry(str))
        {
          localObject = paramKeyStore.getCertificate(str);
          if (localObject instanceof X509Certificate)
            localHashSet.add(localObject);
        }
        else if (paramKeyStore.isKeyEntry(str))
        {
          localObject = paramKeyStore.getCertificateChain(str);
          if ((localObject != null) && (localObject.length > 0) && (localObject[0] instanceof X509Certificate))
            localHashSet.add(localObject[0]);
        }
      }
    }
    catch (KeyStoreException localKeyStoreException)
    {
    }
    return ((Set)localHashSet);
  }
}
package sun.security.provider;

import com.sun.security.auth.PrincipalComparator;
import java.awt.AWTPermission;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Identity;
import java.security.IdentityScope;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.Security;
import java.security.UnresolvedPermission;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PropertyPermission;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import sun.net.www.ParseUtil;
import sun.security.util.Debug;
import sun.security.util.PolicyUtil;
import sun.security.util.PropertyExpander;
import sun.security.util.ResourcesMgr;
import sun.security.util.SecurityConstants;

public class PolicyFile extends Policy
{
  private static final Debug debug = Debug.getInstance("policy");
  private static final String NONE = "NONE";
  private static final String P11KEYSTORE = "PKCS11";
  private static final String SELF = "${{self}}";
  private static final String X500PRINCIPAL = "javax.security.auth.x500.X500Principal";
  private static final String POLICY = "java.security.policy";
  private static final String SECURITY_MANAGER = "java.security.manager";
  private static final String POLICY_URL = "policy.url.";
  private static final String AUTH_POLICY = "java.security.auth.policy";
  private static final String AUTH_POLICY_URL = "auth.policy.url.";
  private static final int DEFAULT_CACHE_SIZE = 1;
  private static IdentityScope scope = null;
  private AtomicReference<PolicyInfo> policyInfo = new AtomicReference();
  private boolean constructed = false;
  private boolean expandProperties = true;
  private boolean ignoreIdentityScope = false;
  private boolean allowSystemProperties = true;
  private boolean notUtf8 = false;
  private URL url;
  private static final Class[] PARAMS0 = new Class[0];
  private static final Class[] PARAMS1 = { String.class };
  private static final Class[] PARAMS2 = { String.class, String.class };

  public PolicyFile()
  {
    init((URL)null);
  }

  public PolicyFile(URL paramURL)
  {
    this.url = paramURL;
    init(paramURL);
  }

  public PolicyFile(boolean paramBoolean)
  {
    if (!(paramBoolean))
    {
      init((URL)null);
    }
    else
    {
      PolicyInfo localPolicyInfo = new PolicyInfo(1);
      initStaticPolicy(localPolicyInfo);
      this.policyInfo.set(localPolicyInfo);
    }
  }

  private void init(URL paramURL)
  {
    int i;
    String str = (String)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        PolicyFile.access$002(this.this$0, "true".equalsIgnoreCase(Security.getProperty("policy.expandProperties")));
        PolicyFile.access$102(this.this$0, "true".equalsIgnoreCase(Security.getProperty("policy.ignoreIdentityScope")));
        PolicyFile.access$202(this.this$0, "true".equalsIgnoreCase(Security.getProperty("policy.allowSystemProperty")));
        PolicyFile.access$302(this.this$0, "false".equalsIgnoreCase(System.getProperty("sun.security.policy.utf8")));
        return System.getProperty("sun.security.policy.numcaches");
      }
    });
    if (str != null)
      try
      {
        i = Integer.parseInt(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
        i = 1;
      }
    else
      i = 1;
    PolicyInfo localPolicyInfo = new PolicyInfo(i);
    initPolicyFile(localPolicyInfo, paramURL);
    this.policyInfo.set(localPolicyInfo);
  }

  private void initPolicyFile(PolicyInfo paramPolicyInfo, URL paramURL)
  {
    if (paramURL != null)
    {
      if (debug != null)
        debug.println("reading " + paramURL);
      AccessController.doPrivileged(new PrivilegedAction(this, paramURL, paramPolicyInfo)
      {
        public Object run()
        {
          if (!(PolicyFile.access$400(this.this$0, this.val$url, this.val$newInfo)))
            PolicyFile.access$500(this.this$0, this.val$newInfo);
          return null;
        }
      });
    }
    else
    {
      boolean bool = initPolicyFile("java.security.policy", "policy.url.", paramPolicyInfo);
      if (!(bool))
        initStaticPolicy(paramPolicyInfo);
      initPolicyFile("java.security.auth.policy", "auth.policy.url.", paramPolicyInfo);
    }
  }

  private boolean initPolicyFile(String paramString1, String paramString2, PolicyInfo paramPolicyInfo)
  {
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(this, paramString1, paramPolicyInfo, paramString2)
    {
      public Object run()
      {
        Object localObject;
        boolean bool = false;
        if (PolicyFile.access$200(this.this$0))
        {
          String str1 = System.getProperty(this.val$propname);
          if (str1 != null)
          {
            int j = 0;
            if (str1.startsWith("="))
            {
              j = 1;
              str1 = str1.substring(1);
            }
            try
            {
              URL localURL1;
              str1 = PropertyExpander.expand(str1);
              localObject = new File(str1);
              if (((File)localObject).exists())
                localURL1 = ParseUtil.fileToEncodedURL(new File(((File)localObject).getCanonicalPath()));
              else
                localURL1 = new URL(str1);
              if (PolicyFile.access$600() != null)
                PolicyFile.access$600().println("reading " + localURL1);
              if (PolicyFile.access$400(this.this$0, localURL1, this.val$newInfo))
                bool = true;
            }
            catch (Exception localException1)
            {
              if (PolicyFile.access$600() != null)
                PolicyFile.access$600().println("caught exception: " + localException1);
            }
            if (j != 0)
            {
              if (PolicyFile.access$600() != null)
                PolicyFile.access$600().println("overriding other policies!");
              return Boolean.valueOf(bool);
            }
          }
        }
        for (int i = 1; (str2 = Security.getProperty(this.val$urlname + i)) != null; ++i)
        {
          String str2;
          try
          {
            URL localURL2 = null;
            localObject = PropertyExpander.expand(str2).replace(File.separatorChar, '/');
            if ((str2.startsWith("file:${java.home}/")) || (str2.startsWith("file:${user.home}/")))
              localURL2 = new File(((String)localObject).substring(5)).toURI().toURL();
            else
              localURL2 = new URI((String)localObject).toURL();
            if (PolicyFile.access$600() != null)
              PolicyFile.access$600().println("reading " + localURL2);
            if (PolicyFile.access$400(this.this$0, localURL2, this.val$newInfo))
              bool = true;
          }
          catch (Exception localException2)
          {
            if (PolicyFile.access$600() != null)
            {
              PolicyFile.access$600().println("error reading policy " + localException2);
              localException2.printStackTrace();
            }
          }
        }
        return Boolean.valueOf(bool);
      }
    });
    return localBoolean.booleanValue();
  }

  private boolean init(URL paramURL, PolicyInfo paramPolicyInfo)
  {
    Object localObject1;
    Object localObject2;
    int i = 0;
    PolicyParser localPolicyParser = new PolicyParser(this.expandProperties);
    try
    {
      InputStreamReader localInputStreamReader;
      if (this.notUtf8)
        localInputStreamReader = new InputStreamReader(PolicyUtil.getInputStream(paramURL));
      else
        localInputStreamReader = new InputStreamReader(PolicyUtil.getInputStream(paramURL), "UTF-8");
      localPolicyParser.read(localInputStreamReader);
      localInputStreamReader.close();
      localObject1 = null;
      try
      {
        localObject1 = PolicyUtil.getKeyStore(paramURL, localPolicyParser.getKeyStoreUrl(), localPolicyParser.getKeyStoreType(), localPolicyParser.getKeyStoreProvider(), localPolicyParser.getStorePassURL(), debug);
      }
      catch (Exception localException2)
      {
        if (debug != null)
          localException2.printStackTrace();
      }
      localObject2 = localPolicyParser.grantElements();
      while (((Enumeration)localObject2).hasMoreElements())
      {
        PolicyParser.GrantEntry localGrantEntry = (PolicyParser.GrantEntry)((Enumeration)localObject2).nextElement();
        addGrantEntry(localGrantEntry, (KeyStore)localObject1, paramPolicyInfo);
      }
      i = 1;
    }
    catch (PolicyParser.ParsingException localParsingException)
    {
      localObject1 = new MessageFormat(ResourcesMgr.getString("java.security.policy: error parsing policy:\n\tmessage"));
      localObject2 = { paramURL, localParsingException.getLocalizedMessage() };
      System.err.println(((MessageFormat)localObject1).format(localObject2));
      if (debug != null)
        localParsingException.printStackTrace();
    }
    catch (Exception localException1)
    {
      if (debug != null)
      {
        debug.println("error parsing " + paramURL);
        debug.println(localException1.toString());
        localException1.printStackTrace();
      }
    }
    return i;
  }

  private void initStaticPolicy(PolicyInfo paramPolicyInfo)
  {
    AccessController.doPrivileged(new PrivilegedAction(this, paramPolicyInfo)
    {
      public Object run()
      {
        PolicyFile.PolicyEntry localPolicyEntry = new PolicyFile.PolicyEntry(new CodeSource(null, (Certificate[])null));
        localPolicyEntry.add(SecurityConstants.LOCAL_LISTEN_PERMISSION);
        localPolicyEntry.add(new PropertyPermission("java.version", "read"));
        localPolicyEntry.add(new PropertyPermission("java.vendor", "read"));
        localPolicyEntry.add(new PropertyPermission("java.vendor.url", "read"));
        localPolicyEntry.add(new PropertyPermission("java.class.version", "read"));
        localPolicyEntry.add(new PropertyPermission("os.name", "read"));
        localPolicyEntry.add(new PropertyPermission("os.version", "read"));
        localPolicyEntry.add(new PropertyPermission("os.arch", "read"));
        localPolicyEntry.add(new PropertyPermission("file.separator", "read"));
        localPolicyEntry.add(new PropertyPermission("path.separator", "read"));
        localPolicyEntry.add(new PropertyPermission("line.separator", "read"));
        localPolicyEntry.add(new PropertyPermission("java.specification.version", "read"));
        localPolicyEntry.add(new PropertyPermission("java.specification.vendor", "read"));
        localPolicyEntry.add(new PropertyPermission("java.specification.name", "read"));
        localPolicyEntry.add(new PropertyPermission("java.vm.specification.version", "read"));
        localPolicyEntry.add(new PropertyPermission("java.vm.specification.vendor", "read"));
        localPolicyEntry.add(new PropertyPermission("java.vm.specification.name", "read"));
        localPolicyEntry.add(new PropertyPermission("java.vm.version", "read"));
        localPolicyEntry.add(new PropertyPermission("java.vm.vendor", "read"));
        localPolicyEntry.add(new PropertyPermission("java.vm.name", "read"));
        this.val$newInfo.policyEntries.add(localPolicyEntry);
        String[] arrayOfString = PolicyParser.parseExtDirs("${{java.ext.dirs}}", 0);
        if ((arrayOfString != null) && (arrayOfString.length > 0))
          for (int i = 0; i < arrayOfString.length; ++i)
            try
            {
              localPolicyEntry = new PolicyFile.PolicyEntry(PolicyFile.access$700(this.this$0, new CodeSource(new URL(arrayOfString[i]), (Certificate[])null), false));
              localPolicyEntry.add(SecurityConstants.ALL_PERMISSION);
              this.val$newInfo.policyEntries.add(localPolicyEntry);
            }
            catch (Exception localException)
            {
            }
        return null;
      }
    });
  }

  private CodeSource getCodeSource(PolicyParser.GrantEntry paramGrantEntry, KeyStore paramKeyStore, PolicyInfo paramPolicyInfo)
    throws MalformedURLException
  {
    URL localURL;
    Certificate[] arrayOfCertificate = null;
    if (paramGrantEntry.signedBy != null)
    {
      arrayOfCertificate = getCertificates(paramKeyStore, paramGrantEntry.signedBy, paramPolicyInfo);
      if (arrayOfCertificate == null)
      {
        if (debug != null)
          debug.println("  -- No certs for alias '" + paramGrantEntry.signedBy + "' - ignoring entry");
        return null;
      }
    }
    if (paramGrantEntry.codeBase != null)
      localURL = new URL(paramGrantEntry.codeBase);
    else
      localURL = null;
    return canonicalizeCodebase(new CodeSource(localURL, arrayOfCertificate), false);
  }

  private void addGrantEntry(PolicyParser.GrantEntry paramGrantEntry, KeyStore paramKeyStore, PolicyInfo paramPolicyInfo)
  {
    Object localObject1;
    Object localObject2;
    Object localObject3;
    if (debug != null)
    {
      debug.println("Adding policy entry: ");
      debug.println("  signedBy " + paramGrantEntry.signedBy);
      debug.println("  codeBase " + paramGrantEntry.codeBase);
      if ((paramGrantEntry.principals != null) && (paramGrantEntry.principals.size() > 0))
      {
        localObject1 = paramGrantEntry.principals.listIterator();
        while (((ListIterator)localObject1).hasNext())
        {
          localObject2 = (PolicyParser.PrincipalEntry)((ListIterator)localObject1).next();
          debug.println("  " + ((PolicyParser.PrincipalEntry)localObject2).toString());
        }
      }
    }
    try
    {
      localObject1 = getCodeSource(paramGrantEntry, paramKeyStore, paramPolicyInfo);
      if (localObject1 == null)
        return;
      if (!(replacePrincipals(paramGrantEntry.principals, paramKeyStore)))
        return;
      localObject2 = new PolicyEntry((CodeSource)localObject1, paramGrantEntry.principals);
      localObject3 = paramGrantEntry.permissionElements();
      while (((Enumeration)localObject3).hasMoreElements())
      {
        Object localObject5;
        Object localObject6;
        PolicyParser.PermissionEntry localPermissionEntry = (PolicyParser.PermissionEntry)((Enumeration)localObject3).nextElement();
        try
        {
          Object localObject4;
          expandPermissionName(localPermissionEntry, paramKeyStore);
          if ((localPermissionEntry.permission.equals("javax.security.auth.PrivateCredentialPermission")) && (localPermissionEntry.name.endsWith(" self")))
            localPermissionEntry.name = localPermissionEntry.name.substring(0, localPermissionEntry.name.indexOf("self")) + "${{self}}";
          if ((localPermissionEntry.name != null) && (localPermissionEntry.name.indexOf("${{self}}") != -1))
          {
            if (localPermissionEntry.signedBy != null)
              localObject5 = getCertificates(paramKeyStore, localPermissionEntry.signedBy, paramPolicyInfo);
            else
              localObject5 = null;
            localObject4 = new SelfPermission(localPermissionEntry.permission, localPermissionEntry.name, localPermissionEntry.action, localObject5);
          }
          else
          {
            localObject4 = getInstance(localPermissionEntry.permission, localPermissionEntry.name, localPermissionEntry.action);
          }
          ((PolicyEntry)localObject2).add((Permission)localObject4);
          if (debug != null)
            debug.println("  " + localObject4);
        }
        catch (ClassNotFoundException localClassNotFoundException)
        {
          if (localPermissionEntry.signedBy != null)
            localObject5 = getCertificates(paramKeyStore, localPermissionEntry.signedBy, paramPolicyInfo);
          else
            localObject5 = null;
          if ((localObject5 != null) || (localPermissionEntry.signedBy == null))
          {
            localObject6 = new UnresolvedPermission(localPermissionEntry.permission, localPermissionEntry.name, localPermissionEntry.action, localObject5);
            ((PolicyEntry)localObject2).add((Permission)localObject6);
            if (debug != null)
              debug.println("  " + localObject6);
          }
        }
        catch (InvocationTargetException localInvocationTargetException)
        {
          localObject5 = new MessageFormat(ResourcesMgr.getString("java.security.policy: error adding Permission, perm:\n\tmessage"));
          localObject6 = { localPermissionEntry.permission, localInvocationTargetException.getTargetException().toString() };
          System.err.println(((MessageFormat)localObject5).format(localObject6));
        }
        catch (Exception localException2)
        {
          localObject5 = new MessageFormat(ResourcesMgr.getString("java.security.policy: error adding Permission, perm:\n\tmessage"));
          localObject6 = { localPermissionEntry.permission, localException2.toString() };
          System.err.println(((MessageFormat)localObject5).format(localObject6));
        }
      }
      paramPolicyInfo.policyEntries.add(localObject2);
    }
    catch (Exception localException1)
    {
      localObject2 = new MessageFormat(ResourcesMgr.getString("java.security.policy: error adding Entry:\n\tmessage"));
      localObject3 = { localException1.toString() };
      System.err.println(((MessageFormat)localObject2).format(localObject3));
    }
    if (debug != null)
      debug.println();
  }

  private static final Permission getInstance(String paramString1, String paramString2, String paramString3)
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokestatic 831	java/lang/Class:forName	(Ljava/lang/String;)Ljava/lang/Class;
    //   4: astore_3
    //   5: aload_3
    //   6: aload_1
    //   7: aload_2
    //   8: invokestatic 934	sun/security/provider/PolicyFile:getKnownInstance	(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/security/Permission;
    //   11: astore 4
    //   13: aload 4
    //   15: ifnull +6 -> 21
    //   18: aload 4
    //   20: areturn
    //   21: aload_1
    //   22: ifnonnull +89 -> 111
    //   25: aload_2
    //   26: ifnonnull +85 -> 111
    //   29: aload_3
    //   30: getstatic 799	sun/security/provider/PolicyFile:PARAMS0	[Ljava/lang/Class;
    //   33: invokevirtual 832	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   36: astore 5
    //   38: aload 5
    //   40: iconst_0
    //   41: anewarray 449	java/lang/Object
    //   44: invokevirtual 859	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   47: checkcast 470	java/security/Permission
    //   50: areturn
    //   51: astore 5
    //   53: aload_3
    //   54: getstatic 800	sun/security/provider/PolicyFile:PARAMS1	[Ljava/lang/Class;
    //   57: invokevirtual 832	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   60: astore 6
    //   62: aload 6
    //   64: iconst_1
    //   65: anewarray 449	java/lang/Object
    //   68: dup
    //   69: iconst_0
    //   70: aload_1
    //   71: aastore
    //   72: invokevirtual 859	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   75: checkcast 470	java/security/Permission
    //   78: areturn
    //   79: astore 6
    //   81: aload_3
    //   82: getstatic 801	sun/security/provider/PolicyFile:PARAMS2	[Ljava/lang/Class;
    //   85: invokevirtual 832	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   88: astore 7
    //   90: aload 7
    //   92: iconst_2
    //   93: anewarray 449	java/lang/Object
    //   96: dup
    //   97: iconst_0
    //   98: aload_1
    //   99: aastore
    //   100: dup
    //   101: iconst_1
    //   102: aload_2
    //   103: aastore
    //   104: invokevirtual 859	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   107: checkcast 470	java/security/Permission
    //   110: areturn
    //   111: aload_1
    //   112: ifnull +65 -> 177
    //   115: aload_2
    //   116: ifnonnull +61 -> 177
    //   119: aload_3
    //   120: getstatic 800	sun/security/provider/PolicyFile:PARAMS1	[Ljava/lang/Class;
    //   123: invokevirtual 832	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   126: astore 5
    //   128: aload 5
    //   130: iconst_1
    //   131: anewarray 449	java/lang/Object
    //   134: dup
    //   135: iconst_0
    //   136: aload_1
    //   137: aastore
    //   138: invokevirtual 859	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   141: checkcast 470	java/security/Permission
    //   144: areturn
    //   145: astore 5
    //   147: aload_3
    //   148: getstatic 801	sun/security/provider/PolicyFile:PARAMS2	[Ljava/lang/Class;
    //   151: invokevirtual 832	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   154: astore 6
    //   156: aload 6
    //   158: iconst_2
    //   159: anewarray 449	java/lang/Object
    //   162: dup
    //   163: iconst_0
    //   164: aload_1
    //   165: aastore
    //   166: dup
    //   167: iconst_1
    //   168: aload_2
    //   169: aastore
    //   170: invokevirtual 859	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   173: checkcast 470	java/security/Permission
    //   176: areturn
    //   177: aload_3
    //   178: getstatic 801	sun/security/provider/PolicyFile:PARAMS2	[Ljava/lang/Class;
    //   181: invokevirtual 832	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   184: astore 5
    //   186: aload 5
    //   188: iconst_2
    //   189: anewarray 449	java/lang/Object
    //   192: dup
    //   193: iconst_0
    //   194: aload_1
    //   195: aastore
    //   196: dup
    //   197: iconst_1
    //   198: aload_2
    //   199: aastore
    //   200: invokevirtual 859	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   203: checkcast 470	java/security/Permission
    //   206: areturn
    //
    // Exception table:
    //   from	to	target	type
    //   29	50	51	NoSuchMethodException
    //   53	78	79	NoSuchMethodException
    //   119	144	145	NoSuchMethodException
  }

  private static final Permission getKnownInstance(Class paramClass, String paramString1, String paramString2)
  {
    if (paramClass.equals(FilePermission.class))
      return new FilePermission(paramString1, paramString2);
    if (paramClass.equals(SocketPermission.class))
      return new SocketPermission(paramString1, paramString2);
    if (paramClass.equals(RuntimePermission.class))
      return new RuntimePermission(paramString1, paramString2);
    if (paramClass.equals(PropertyPermission.class))
      return new PropertyPermission(paramString1, paramString2);
    if (paramClass.equals(NetPermission.class))
      return new NetPermission(paramString1, paramString2);
    if (paramClass.equals(AllPermission.class))
      return SecurityConstants.ALL_PERMISSION;
    if (paramClass.equals(AWTPermission.class))
      return new AWTPermission(paramString1, paramString2);
    return null;
  }

  private Certificate[] getCertificates(KeyStore paramKeyStore, String paramString, PolicyInfo paramPolicyInfo)
  {
    Object localObject1;
    ArrayList localArrayList = null;
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ",");
    int i = 0;
    while (localStringTokenizer.hasMoreTokens())
    {
      localObject1 = localStringTokenizer.nextToken().trim();
      ++i;
      Certificate localCertificate = null;
      synchronized (paramPolicyInfo.aliasMapping)
      {
        localCertificate = (Certificate)paramPolicyInfo.aliasMapping.get(localObject1);
        if ((localCertificate == null) && (paramKeyStore != null))
        {
          try
          {
            localCertificate = paramKeyStore.getCertificate((String)localObject1);
          }
          catch (KeyStoreException localKeyStoreException)
          {
          }
          if (localCertificate != null)
          {
            paramPolicyInfo.aliasMapping.put(localObject1, localCertificate);
            paramPolicyInfo.aliasMapping.put(localCertificate, localObject1);
          }
        }
      }
      if (localCertificate != null)
      {
        if (localArrayList == null)
          localArrayList = new ArrayList();
        localArrayList.add(localCertificate);
      }
    }
    if ((localArrayList != null) && (i == localArrayList.size()))
    {
      localObject1 = new Certificate[localArrayList.size()];
      localArrayList.toArray(localObject1);
      return localObject1;
    }
    return ((Certificate)null);
  }

  public void refresh()
  {
    init(this.url);
  }

  public boolean implies(ProtectionDomain paramProtectionDomain, Permission paramPermission)
  {
    PolicyInfo localPolicyInfo = (PolicyInfo)this.policyInfo.get();
    java.util.Map localMap = localPolicyInfo.getPdMapping();
    PermissionCollection localPermissionCollection = (PermissionCollection)localMap.get(paramProtectionDomain);
    if (localPermissionCollection != null)
      return localPermissionCollection.implies(paramPermission);
    localPermissionCollection = getPermissions(paramProtectionDomain);
    if (localPermissionCollection == null)
      return false;
    localMap.put(paramProtectionDomain, localPermissionCollection);
    return localPermissionCollection.implies(paramPermission);
  }

  public PermissionCollection getPermissions(ProtectionDomain paramProtectionDomain)
  {
    Permissions localPermissions = new Permissions();
    if (paramProtectionDomain == null)
      return localPermissions;
    getPermissions(localPermissions, paramProtectionDomain);
    PermissionCollection localPermissionCollection1 = paramProtectionDomain.getPermissions();
    if (localPermissionCollection1 != null)
      synchronized (localPermissionCollection1)
      {
        Enumeration localEnumeration = localPermissionCollection1.elements();
        while (localEnumeration.hasMoreElements())
          localPermissions.add((Permission)localEnumeration.nextElement());
      }
    return localPermissions;
  }

  public PermissionCollection getPermissions(CodeSource paramCodeSource)
  {
    return getPermissions(new Permissions(), paramCodeSource);
  }

  private PermissionCollection getPermissions(Permissions paramPermissions, ProtectionDomain paramProtectionDomain)
  {
    if (debug != null)
      AccessController.doPrivileged(new PrivilegedAction(this, paramProtectionDomain)
      {
        public Object run()
        {
          PolicyFile.access$600().println("getPermissions:\n\t" + PolicyFile.access$800(this.this$0, this.val$pd));
          return null;
        }
      });
    CodeSource localCodeSource1 = paramProtectionDomain.getCodeSource();
    if (localCodeSource1 == null)
      return paramPermissions;
    CodeSource localCodeSource2 = (CodeSource)AccessController.doPrivileged(new PrivilegedAction(this, localCodeSource1)
    {
      public Object run()
      {
        return PolicyFile.access$700(this.this$0, this.val$cs, true);
      }
    });
    return getPermissions(paramPermissions, localCodeSource2, paramProtectionDomain.getPrincipals());
  }

  private PermissionCollection getPermissions(Permissions paramPermissions, CodeSource paramCodeSource)
  {
    CodeSource localCodeSource = (CodeSource)AccessController.doPrivileged(new PrivilegedAction(this, paramCodeSource)
    {
      public Object run()
      {
        return PolicyFile.access$700(this.this$0, this.val$cs, true);
      }
    });
    return getPermissions(paramPermissions, localCodeSource, null);
  }

  private Permissions getPermissions(Permissions paramPermissions, CodeSource paramCodeSource, Principal[] paramArrayOfPrincipal)
  {
    Object localObject2;
    Object localObject3;
    PolicyInfo localPolicyInfo = (PolicyInfo)this.policyInfo.get();
    ??? = localPolicyInfo.policyEntries.iterator();
    while (((Iterator)???).hasNext())
    {
      localObject2 = (PolicyEntry)((Iterator)???).next();
      addPermissions(paramPermissions, paramCodeSource, paramArrayOfPrincipal, (PolicyEntry)localObject2);
    }
    synchronized (localPolicyInfo.identityPolicyEntries)
    {
      localObject2 = localPolicyInfo.identityPolicyEntries.iterator();
      while (((Iterator)localObject2).hasNext())
      {
        localObject3 = (PolicyEntry)((Iterator)localObject2).next();
        addPermissions(paramPermissions, paramCodeSource, paramArrayOfPrincipal, (PolicyEntry)localObject3);
      }
    }
    if (!(this.ignoreIdentityScope))
    {
      ??? = paramCodeSource.getCertificates();
      if (??? != null)
        for (int i = 0; i < ???.length; ++i)
        {
          localObject3 = localPolicyInfo.aliasMapping.get(???[i]);
          if ((localObject3 == null) && (checkForTrustedIdentity(???[i], localPolicyInfo)))
            paramPermissions.add(SecurityConstants.ALL_PERMISSION);
        }
    }
    return ((Permissions)(Permissions)(Permissions)paramPermissions);
  }

  private void addPermissions(Permissions paramPermissions, CodeSource paramCodeSource, Principal[] paramArrayOfPrincipal, PolicyEntry paramPolicyEntry)
  {
    if (debug != null)
      debug.println("evaluate codesources:\n\tPolicy CodeSource: " + paramPolicyEntry.getCodeSource() + "\n" + "\tActive CodeSource: " + paramCodeSource);
    Boolean localBoolean = (Boolean)AccessController.doPrivileged(new PrivilegedAction(this, paramPolicyEntry, paramCodeSource)
    {
      public Object run()
      {
        return new Boolean(this.val$entry.getCodeSource().implies(this.val$cs));
      }
    });
    if (!(localBoolean.booleanValue()))
    {
      if (debug != null)
        debug.println("evaluation (codesource) failed");
      return;
    }
    List localList = paramPolicyEntry.getPrincipals();
    if (debug != null)
    {
      ArrayList localArrayList = new ArrayList();
      if (paramArrayOfPrincipal != null)
        for (int j = 0; j < paramArrayOfPrincipal.length; ++j)
          localArrayList.add(new PolicyParser.PrincipalEntry(paramArrayOfPrincipal[j].getClass().getName(), paramArrayOfPrincipal[j].getName()));
      debug.println("evaluate principals:\n\tPolicy Principals: " + localList + "\n" + "\tActive Principals: " + localArrayList);
    }
    if ((localList == null) || (localList.size() == 0))
    {
      addPerms(paramPermissions, paramArrayOfPrincipal, paramPolicyEntry);
      if (debug != null)
        debug.println("evaluation (codesource/principals) passed");
      return;
    }
    if ((paramArrayOfPrincipal == null) || (paramArrayOfPrincipal.length == 0))
    {
      if (debug != null)
        debug.println("evaluation (principals) failed");
      return;
    }
    for (int i = 0; i < localList.size(); ++i)
    {
      PolicyParser.PrincipalEntry localPrincipalEntry = (PolicyParser.PrincipalEntry)localList.get(i);
      try
      {
        Class localClass = Class.forName(localPrincipalEntry.principalClass, true, Thread.currentThread().getContextClassLoader());
        if (!(PrincipalComparator.class.isAssignableFrom(localClass)))
        {
          if (checkEntryPs(paramArrayOfPrincipal, localPrincipalEntry))
            break label504;
          if (debug != null)
            debug.println("evaluation (principals) failed");
          return;
        }
        Constructor localConstructor = localClass.getConstructor(PARAMS1);
        PrincipalComparator localPrincipalComparator = (PrincipalComparator)localConstructor.newInstance(new Object[] { localPrincipalEntry.principalName });
        if (debug != null)
          debug.println("found PrincipalComparator " + localPrincipalComparator.getClass().getName());
        HashSet localHashSet = new HashSet(paramArrayOfPrincipal.length);
        for (int k = 0; k < paramArrayOfPrincipal.length; ++k)
          localHashSet.add(paramArrayOfPrincipal[k]);
        Subject localSubject = new Subject(true, localHashSet, Collections.EMPTY_SET, Collections.EMPTY_SET);
        if (!(localPrincipalComparator.implies(localSubject)))
        {
          if (debug != null)
            debug.println("evaluation (principal comparator) failed");
          label504: return;
        }
      }
      catch (Exception localException)
      {
        if (debug != null)
          localException.printStackTrace();
        if (!(checkEntryPs(paramArrayOfPrincipal, localPrincipalEntry)))
        {
          if (debug != null)
            debug.println("evaluation (principals) failed");
          return;
        }
      }
    }
    if (debug != null)
      debug.println("evaluation (codesource/principals) passed");
    addPerms(paramPermissions, paramArrayOfPrincipal, paramPolicyEntry);
  }

  private void addPerms(Permissions paramPermissions, Principal[] paramArrayOfPrincipal, PolicyEntry paramPolicyEntry)
  {
    for (int i = 0; i < paramPolicyEntry.permissions.size(); ++i)
    {
      Permission localPermission = (Permission)paramPolicyEntry.permissions.get(i);
      if (debug != null)
        debug.println("  granting " + localPermission);
      if (localPermission instanceof SelfPermission)
        expandSelf((SelfPermission)localPermission, paramPolicyEntry.getPrincipals(), paramArrayOfPrincipal, paramPermissions);
      else
        paramPermissions.add(localPermission);
    }
  }

  private boolean checkEntryPs(Principal[] paramArrayOfPrincipal, PolicyParser.PrincipalEntry paramPrincipalEntry)
  {
    for (int i = 0; i < paramArrayOfPrincipal.length; ++i)
      if ((((paramPrincipalEntry.principalClass.equals("WILDCARD_PRINCIPAL_CLASS")) || (paramPrincipalEntry.principalClass.equals(paramArrayOfPrincipal[i].getClass().getName())))) && (((paramPrincipalEntry.principalName.equals("WILDCARD_PRINCIPAL_NAME")) || (paramPrincipalEntry.principalName.equals(paramArrayOfPrincipal[i].getName())))))
        return true;
    return false;
  }

  private void expandSelf(SelfPermission paramSelfPermission, List paramList, Principal[] paramArrayOfPrincipal, Permissions paramPermissions)
  {
    Object localObject1;
    if ((paramList == null) || (paramList.size() == 0))
    {
      if (debug != null)
        debug.println("Ignoring permission " + paramSelfPermission.getSelfType() + " with target name (" + paramSelfPermission.getSelfName() + ").  " + "No Principal(s) specified " + "in the grant clause.  " + "SELF-based target names are " + "only valid in the context " + "of a Principal-based grant entry.");
      return;
    }
    int i = 0;
    StringBuilder localStringBuilder = new StringBuilder();
    while ((j = paramSelfPermission.getSelfName().indexOf("${{self}}", i)) != -1)
    {
      int j;
      localStringBuilder.append(paramSelfPermission.getSelfName().substring(i, j));
      ListIterator localListIterator = paramList.listIterator();
      while (localListIterator.hasNext())
      {
        localObject1 = (PolicyParser.PrincipalEntry)localListIterator.next();
        ??? = getPrincipalInfo((PolicyParser.PrincipalEntry)localObject1, paramArrayOfPrincipal);
        for (int k = 0; k < ???.length; ++k)
        {
          if (k != 0)
            localStringBuilder.append(", ");
          localStringBuilder.append(???[k][0] + " " + "\"" + ???[k][1] + "\"");
        }
        if (localListIterator.hasNext())
          localStringBuilder.append(", ");
      }
      i = j + "${{self}}".length();
    }
    localStringBuilder.append(paramSelfPermission.getSelfName().substring(i));
    if (debug != null)
      debug.println("  expanded:\n\t" + paramSelfPermission.getSelfName() + "\n  into:\n\t" + localStringBuilder.toString());
    try
    {
      paramPermissions.add(getInstance(paramSelfPermission.getSelfType(), localStringBuilder.toString(), paramSelfPermission.getSelfActions()));
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      localObject1 = null;
      synchronized (paramPermissions)
      {
        Enumeration localEnumeration = paramPermissions.elements();
        while (localEnumeration.hasMoreElements())
        {
          Permission localPermission = (Permission)localEnumeration.nextElement();
          if (localPermission.getClass().getName().equals(paramSelfPermission.getSelfType()))
          {
            localObject1 = localPermission.getClass();
            break;
          }
        }
      }
      if (localObject1 == null)
        paramPermissions.add(new UnresolvedPermission(paramSelfPermission.getSelfType(), localStringBuilder.toString(), paramSelfPermission.getSelfActions(), paramSelfPermission.getCerts()));
      else
        try
        {
          if (paramSelfPermission.getSelfActions() == null)
          {
            try
            {
              ??? = ((Class)localObject1).getConstructor(PARAMS1);
              paramPermissions.add((Permission)((Constructor)???).newInstance(new Object[] { localStringBuilder.toString() }));
            }
            catch (NoSuchMethodException localNoSuchMethodException)
            {
              ??? = ((Class)localObject1).getConstructor(PARAMS2);
              paramPermissions.add((Permission)((Constructor)???).newInstance(new Object[] { localStringBuilder.toString(), paramSelfPermission.getSelfActions() }));
            }
          }
          else
          {
            ??? = ((Class)localObject1).getConstructor(PARAMS2);
            paramPermissions.add((Permission)((Constructor)???).newInstance(new Object[] { localStringBuilder.toString(), paramSelfPermission.getSelfActions() }));
          }
        }
        catch (Exception localException2)
        {
          if (debug != null)
            debug.println("self entry expansion  instantiation failed: " + localException2.toString());
        }
    }
    catch (Exception localException1)
    {
      if (debug != null)
        debug.println(localException1.toString());
    }
  }

  private String[][] getPrincipalInfo(PolicyParser.PrincipalEntry paramPrincipalEntry, Principal[] paramArrayOfPrincipal)
  {
    if ((!(paramPrincipalEntry.principalClass.equals("WILDCARD_PRINCIPAL_CLASS"))) && (!(paramPrincipalEntry.principalName.equals("WILDCARD_PRINCIPAL_NAME"))))
    {
      localObject = new String[1][2];
      localObject[0][0] = paramPrincipalEntry.principalClass;
      localObject[0][1] = paramPrincipalEntry.principalName;
      return localObject;
    }
    if ((!(paramPrincipalEntry.principalClass.equals("WILDCARD_PRINCIPAL_CLASS"))) && (paramPrincipalEntry.principalName.equals("WILDCARD_PRINCIPAL_NAME")))
    {
      localObject = new ArrayList();
      for (int i = 0; i < paramArrayOfPrincipal.length; ++i)
        if (paramPrincipalEntry.principalClass.equals(paramArrayOfPrincipal[i].getClass().getName()))
          ((List)localObject).add(paramArrayOfPrincipal[i]);
      String[][] arrayOfString = new String[((List)localObject).size()][2];
      int k = 0;
      Iterator localIterator = ((List)localObject).iterator();
      while (localIterator.hasNext())
      {
        Principal localPrincipal = (Principal)localIterator.next();
        arrayOfString[k][0] = localPrincipal.getClass().getName();
        arrayOfString[k][1] = localPrincipal.getName();
        ++k;
      }
      return arrayOfString;
    }
    Object localObject = new String[paramArrayOfPrincipal.length][2];
    for (int j = 0; j < paramArrayOfPrincipal.length; ++j)
    {
      localObject[j][0] = paramArrayOfPrincipal[j].getClass().getName();
      localObject[j][1] = paramArrayOfPrincipal[j].getName();
    }
    return ((String)localObject);
  }

  protected Certificate[] getSignerCertificates(CodeSource paramCodeSource)
  {
    Certificate[] arrayOfCertificate1 = null;
    if ((arrayOfCertificate1 = paramCodeSource.getCertificates()) == null)
      return null;
    for (int i = 0; i < arrayOfCertificate1.length; ++i)
      if (!(arrayOfCertificate1[i] instanceof X509Certificate))
        return paramCodeSource.getCertificates();
    i = 0;
    int j = 0;
    while (i < arrayOfCertificate1.length)
    {
      ++j;
      while ((i + 1 < arrayOfCertificate1.length) && (((X509Certificate)arrayOfCertificate1[i]).getIssuerDN().equals(((X509Certificate)arrayOfCertificate1[(i + 1)]).getSubjectDN())))
        ++i;
      ++i;
    }
    if (j == arrayOfCertificate1.length)
      return arrayOfCertificate1;
    ArrayList localArrayList = new ArrayList();
    for (i = 0; i < arrayOfCertificate1.length; ++i)
    {
      localArrayList.add(arrayOfCertificate1[i]);
      while ((i + 1 < arrayOfCertificate1.length) && (((X509Certificate)arrayOfCertificate1[i]).getIssuerDN().equals(((X509Certificate)arrayOfCertificate1[(i + 1)]).getSubjectDN())))
        ++i;
    }
    Certificate[] arrayOfCertificate2 = new Certificate[localArrayList.size()];
    localArrayList.toArray(arrayOfCertificate2);
    return arrayOfCertificate2;
  }

  private CodeSource canonicalizeCodebase(CodeSource paramCodeSource, boolean paramBoolean)
  {
    Object localObject;
    String str = null;
    CodeSource localCodeSource = paramCodeSource;
    URL localURL = paramCodeSource.getLocation();
    if (localURL != null)
    {
      try
      {
        localObject = localURL.openConnection().getPermission();
      }
      catch (IOException localIOException2)
      {
        localObject = null;
      }
      if (localObject instanceof FilePermission)
      {
        str = ((Permission)localObject).getName();
      }
      else if ((localObject == null) && (localURL.getProtocol().equals("file")))
      {
        str = localURL.getFile().replace('/', File.separatorChar);
        str = ParseUtil.decode(str);
      }
    }
    if (str != null)
      try
      {
        localObject = null;
        str = canonPath(str);
        localObject = ParseUtil.fileToEncodedURL(new File(str));
        if (paramBoolean)
          localCodeSource = new CodeSource((URL)localObject, getSignerCertificates(paramCodeSource));
        else
          localCodeSource = new CodeSource((URL)localObject, paramCodeSource.getCertificates());
      }
      catch (IOException localIOException1)
      {
        if (paramBoolean)
          localCodeSource = new CodeSource(paramCodeSource.getLocation(), getSignerCertificates(paramCodeSource));
      }
    else if (paramBoolean)
      localCodeSource = new CodeSource(paramCodeSource.getLocation(), getSignerCertificates(paramCodeSource));
    return ((CodeSource)localCodeSource);
  }

  public static String canonPath(String paramString)
    throws IOException
  {
    if (paramString.endsWith("*"))
    {
      paramString = paramString.substring(0, paramString.length() - 1) + "-";
      paramString = new File(paramString).getCanonicalPath();
      return paramString.substring(0, paramString.length() - 1) + "*";
    }
    return new File(paramString).getCanonicalPath();
  }

  private String printPD(ProtectionDomain paramProtectionDomain)
  {
    Principal[] arrayOfPrincipal = paramProtectionDomain.getPrincipals();
    String str = "<no principals>";
    if ((arrayOfPrincipal != null) && (arrayOfPrincipal.length > 0))
    {
      StringBuilder localStringBuilder = new StringBuilder("(principals ");
      for (int i = 0; i < arrayOfPrincipal.length; ++i)
      {
        localStringBuilder.append(arrayOfPrincipal[i].getClass().getName() + " \"" + arrayOfPrincipal[i].getName() + "\"");
        if (i < arrayOfPrincipal.length - 1)
          localStringBuilder.append(", ");
        else
          localStringBuilder.append(")");
      }
      str = localStringBuilder.toString();
    }
    return "PD CodeSource: " + paramProtectionDomain.getCodeSource() + "\n\t" + "PD ClassLoader: " + paramProtectionDomain.getClassLoader() + "\n\t" + "PD Principals: " + str;
  }

  private boolean replacePrincipals(List paramList, KeyStore paramKeyStore)
  {
    if ((paramList == null) || (paramList.size() == 0) || (paramKeyStore == null))
      return true;
    ListIterator localListIterator = paramList.listIterator();
    while (localListIterator.hasNext())
    {
      PolicyParser.PrincipalEntry localPrincipalEntry = (PolicyParser.PrincipalEntry)localListIterator.next();
      if (localPrincipalEntry.principalClass.equals("PolicyParser.REPLACE_NAME"))
      {
        String str;
        if ((str = getDN(localPrincipalEntry.principalName, paramKeyStore)) == null)
          return false;
        if (debug != null)
          debug.println("  Replacing \"" + localPrincipalEntry.principalName + "\" with " + "javax.security.auth.x500.X500Principal" + "/\"" + str + "\"");
        localPrincipalEntry.principalClass = "javax.security.auth.x500.X500Principal";
        localPrincipalEntry.principalName = str;
      }
    }
    return true;
  }

  private void expandPermissionName(PolicyParser.PermissionEntry paramPermissionEntry, KeyStore paramKeyStore)
    throws Exception
  {
    if ((paramPermissionEntry.name == null) || (paramPermissionEntry.name.indexOf("${{", 0) == -1))
      return;
    int i = 0;
    StringBuilder localStringBuilder = new StringBuilder();
    while (true)
    {
      int k;
      String str1;
      int l;
      String str2;
      MessageFormat localMessageFormat;
      Object[] arrayOfObject;
      while (true)
      {
        int j;
        if ((j = paramPermissionEntry.name.indexOf("${{", i)) == -1)
          break label389;
        k = paramPermissionEntry.name.indexOf("}}", j);
        if (k < 1)
          break label389:
        localStringBuilder.append(paramPermissionEntry.name.substring(i, j));
        str1 = paramPermissionEntry.name.substring(j + 3, k);
        str2 = str1;
        if ((l = str1.indexOf(":")) != -1)
          str2 = str1.substring(0, l);
        if (!(str2.equalsIgnoreCase("self")))
          break;
        localStringBuilder.append(paramPermissionEntry.name.substring(j, k + 2));
        i = k + 2;
      }
      if (str2.equalsIgnoreCase("alias"))
      {
        if (l == -1)
        {
          localMessageFormat = new MessageFormat(ResourcesMgr.getString("alias name not provided (pe.name)"));
          arrayOfObject = { paramPermissionEntry.name };
          throw new Exception(localMessageFormat.format(arrayOfObject));
        }
        String str3 = str1.substring(l + 1);
        if ((str3 = getDN(str3, paramKeyStore)) == null)
        {
          localMessageFormat = new MessageFormat(ResourcesMgr.getString("unable to perform substitution on alias, suffix"));
          arrayOfObject = { str1.substring(l + 1) };
          throw new Exception(localMessageFormat.format(arrayOfObject));
        }
        localStringBuilder.append("javax.security.auth.x500.X500Principal \"" + str3 + "\"");
        i = k + 2;
      }
      else
      {
        localMessageFormat = new MessageFormat(ResourcesMgr.getString("substitution value, prefix, unsupported"));
        arrayOfObject = { str2 };
        throw new Exception(localMessageFormat.format(arrayOfObject));
      }
    }
    label389: localStringBuilder.append(paramPermissionEntry.name.substring(i));
    if (debug != null)
      debug.println("  Permission name expanded from:\n\t" + paramPermissionEntry.name + "\nto\n\t" + localStringBuilder.toString());
    paramPermissionEntry.name = localStringBuilder.toString();
  }

  private String getDN(String paramString, KeyStore paramKeyStore)
  {
    Certificate localCertificate = null;
    try
    {
      localCertificate = paramKeyStore.getCertificate(paramString);
    }
    catch (Exception localException)
    {
      if (debug != null)
        debug.println("  Error retrieving certificate for '" + paramString + "': " + localException.toString());
      return null;
    }
    if ((localCertificate == null) || (!(localCertificate instanceof X509Certificate)))
    {
      if (debug != null)
        debug.println("  -- No certificate for '" + paramString + "' - ignoring entry");
      return null;
    }
    X509Certificate localX509Certificate = (X509Certificate)localCertificate;
    X500Principal localX500Principal = new X500Principal(localX509Certificate.getSubjectX500Principal().toString());
    return localX500Principal.getName();
  }

  private boolean checkForTrustedIdentity(Certificate paramCertificate, PolicyInfo paramPolicyInfo)
  {
    Object localObject2;
    if (paramCertificate == null)
      return false;
    if (this.ignoreIdentityScope)
      return false;
    synchronized (PolicyFile.class)
    {
      if (scope == null)
      {
        localObject2 = IdentityScope.getSystemScope();
        if (localObject2 instanceof IdentityDatabase)
          scope = (IdentityScope)localObject2;
      }
    }
    if (scope == null)
    {
      this.ignoreIdentityScope = true;
      return false;
    }
    ??? = (Identity)AccessController.doPrivileged(new PrivilegedAction(this, paramCertificate)
    {
      public Object run()
      {
        return PolicyFile.access$900().getIdentity(this.val$cert.getPublicKey());
      }
    });
    if (isTrusted((Identity)???))
    {
      if (debug != null)
      {
        debug.println("Adding policy entry for trusted Identity: ");
        AccessController.doPrivileged(new PrivilegedAction(this, (Identity)???)
        {
          public Object run()
          {
            PolicyFile.access$600().println("  identity = " + this.val$id);
            return null;
          }
        });
        debug.println("");
      }
      localObject2 = { paramCertificate };
      PolicyEntry localPolicyEntry = new PolicyEntry(new CodeSource(null, localObject2));
      localPolicyEntry.add(SecurityConstants.ALL_PERMISSION);
      paramPolicyInfo.identityPolicyEntries.add(localPolicyEntry);
      paramPolicyInfo.aliasMapping.put(paramCertificate, ((Identity)???).getName());
      return true;
    }
    return false;
  }

  private static boolean isTrusted(Identity paramIdentity)
  {
    if (paramIdentity instanceof SystemIdentity)
    {
      localObject = (SystemIdentity)paramIdentity;
      if (((SystemIdentity)localObject).isTrusted())
        return true;
      break label45:
    }
    if (!(paramIdentity instanceof SystemSigner))
      break label45;
    Object localObject = (SystemSigner)paramIdentity;
    label45: return (((SystemSigner)localObject).isTrusted());
  }

  private static class PolicyEntry
  {
    private final CodeSource codesource;
    final List permissions;
    private final List principals;

    PolicyEntry(CodeSource paramCodeSource, List paramList)
    {
      this.codesource = paramCodeSource;
      this.permissions = new ArrayList();
      this.principals = paramList;
    }

    PolicyEntry(CodeSource paramCodeSource)
    {
      this(paramCodeSource, null);
    }

    List getPrincipals()
    {
      return this.principals;
    }

    void add(Permission paramPermission)
    {
      this.permissions.add(paramPermission);
    }

    CodeSource getCodeSource()
    {
      return this.codesource;
    }

    public String toString()
    {
      StringBuilder localStringBuilder = new StringBuilder();
      localStringBuilder.append(ResourcesMgr.getString("("));
      localStringBuilder.append(getCodeSource());
      localStringBuilder.append("\n");
      for (int i = 0; i < this.permissions.size(); ++i)
      {
        Permission localPermission = (Permission)this.permissions.get(i);
        localStringBuilder.append(ResourcesMgr.getString(" "));
        localStringBuilder.append(ResourcesMgr.getString(" "));
        localStringBuilder.append(localPermission);
        localStringBuilder.append(ResourcesMgr.getString("\n"));
      }
      localStringBuilder.append(ResourcesMgr.getString(")"));
      localStringBuilder.append(ResourcesMgr.getString("\n"));
      return localStringBuilder.toString();
    }
  }

  private static class PolicyInfo
  {
    private static final boolean verbose = 0;
    final List<PolicyFile.PolicyEntry> policyEntries = new ArrayList();
    final List<PolicyFile.PolicyEntry> identityPolicyEntries = Collections.synchronizedList(new ArrayList(2));
    final java.util.Map aliasMapping = Collections.synchronizedMap(new HashMap(11));
    private final java.util.Map<ProtectionDomain, PermissionCollection>[] pdMapping;
    private Random random;

    PolicyInfo(int paramInt)
    {
      this.pdMapping = new java.util.Map[paramInt];
      for (int i = 0; i < paramInt; ++i)
        this.pdMapping[i] = Collections.synchronizedMap(new WeakHashMap());
      if (paramInt > 1)
        this.random = new Random();
    }

    java.util.Map<ProtectionDomain, PermissionCollection> getPdMapping()
    {
      if (this.pdMapping.length == 1)
        return this.pdMapping[0];
      int i = Math.abs(this.random.nextInt() % this.pdMapping.length);
      return this.pdMapping[i];
    }
  }

  private static class SelfPermission extends Permission
  {
    private static final long serialVersionUID = -8315562579967246806L;
    private String type;
    private String name;
    private String actions;
    private Certificate[] certs;

    public SelfPermission(String paramString1, String paramString2, String paramString3, Certificate[] paramArrayOfCertificate)
    {
      super(paramString1);
      if (paramString1 == null)
        throw new NullPointerException(ResourcesMgr.getString("type can't be null"));
      this.type = paramString1;
      this.name = paramString2;
      this.actions = paramString3;
      if (paramArrayOfCertificate != null)
      {
        for (int i = 0; i < paramArrayOfCertificate.length; ++i)
          if (!(paramArrayOfCertificate[i] instanceof X509Certificate))
          {
            this.certs = ((Certificate[])(Certificate[])paramArrayOfCertificate.clone());
            break;
          }
        if (this.certs == null)
        {
          i = 0;
          int j = 0;
          while (i < paramArrayOfCertificate.length)
          {
            ++j;
            while ((i + 1 < paramArrayOfCertificate.length) && (((X509Certificate)paramArrayOfCertificate[i]).getIssuerDN().equals(((X509Certificate)paramArrayOfCertificate[(i + 1)]).getSubjectDN())))
              ++i;
            ++i;
          }
          if (j == paramArrayOfCertificate.length)
            this.certs = ((Certificate[])(Certificate[])paramArrayOfCertificate.clone());
          if (this.certs == null)
          {
            ArrayList localArrayList = new ArrayList();
            for (i = 0; i < paramArrayOfCertificate.length; ++i)
            {
              localArrayList.add(paramArrayOfCertificate[i]);
              while ((i + 1 < paramArrayOfCertificate.length) && (((X509Certificate)paramArrayOfCertificate[i]).getIssuerDN().equals(((X509Certificate)paramArrayOfCertificate[(i + 1)]).getSubjectDN())))
                ++i;
            }
            this.certs = new Certificate[localArrayList.size()];
            localArrayList.toArray(this.certs);
          }
        }
      }
    }

    public boolean implies(Permission paramPermission)
    {
      return false;
    }

    public boolean equals(Object paramObject)
    {
      int j;
      int k;
      if (paramObject == this)
        return true;
      if (!(paramObject instanceof SelfPermission))
        return false;
      SelfPermission localSelfPermission = (SelfPermission)paramObject;
      if ((!(this.type.equals(localSelfPermission.type))) || (!(this.name.equals(localSelfPermission.name))) || (!(this.actions.equals(localSelfPermission.actions))))
        return false;
      if (this.certs.length != localSelfPermission.certs.length)
        return false;
      for (int i = 0; i < this.certs.length; ++i)
      {
        k = 0;
        for (j = 0; j < localSelfPermission.certs.length; ++j)
          if (this.certs[i].equals(localSelfPermission.certs[j]))
          {
            k = 1;
            break;
          }
        if (k == 0)
          return false;
      }
      for (i = 0; i < localSelfPermission.certs.length; ++i)
      {
        k = 0;
        for (j = 0; j < this.certs.length; ++j)
          if (localSelfPermission.certs[i].equals(this.certs[j]))
          {
            k = 1;
            break;
          }
        if (k == 0)
          return false;
      }
      return true;
    }

    public int hashCode()
    {
      int i = this.type.hashCode();
      if (this.name != null)
        i ^= this.name.hashCode();
      if (this.actions != null)
        i ^= this.actions.hashCode();
      return i;
    }

    public String getActions()
    {
      return "";
    }

    public String getSelfType()
    {
      return this.type;
    }

    public String getSelfName()
    {
      return this.name;
    }

    public String getSelfActions()
    {
      return this.actions;
    }

    public Certificate[] getCerts()
    {
      return this.certs;
    }

    public String toString()
    {
      return "(SelfPermission " + this.type + " " + this.name + " " + this.actions + ")";
    }
  }
}
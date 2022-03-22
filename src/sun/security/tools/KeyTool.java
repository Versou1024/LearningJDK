package sun.security.tools;

import C;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.net.URLClassLoader;
import java.security.Identity;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import javax.crypto.KeyGenerator;
import sun.misc.BASE64Encoder;
import sun.security.pkcs.PKCS10;
import sun.security.provider.IdentityDatabase;
import sun.security.provider.SystemIdentity;
import sun.security.provider.SystemSigner;
import sun.security.util.DerOutputStream;
import sun.security.util.ObjectIdentifier;
import sun.security.util.Password;
import sun.security.util.PathList;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertAndKeyGen;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.Extension;
import sun.security.x509.X500Name;
import sun.security.x509.X500Signer;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public final class KeyTool
{
  private boolean debug = false;
  private int command = -1;
  private String sigAlgName = null;
  private String keyAlgName = null;
  private boolean verbose = false;
  private int keysize = -1;
  private boolean rfc = false;
  private long validity = 90L;
  private String alias = null;
  private String dname = null;
  private String dest = null;
  private String filename = null;
  private String srcksfname = null;
  private Set<Pair<String, String>> providers = null;
  private String storetype = null;
  private String srcProviderName = null;
  private String providerName = null;
  private String pathlist = null;
  private char[] storePass = null;
  private char[] storePassNew = null;
  private char[] keyPass = null;
  private char[] keyPassNew = null;
  private char[] oldPass = null;
  private char[] newPass = null;
  private char[] destKeyPass = null;
  private char[] srckeyPass = null;
  private String ksfname = null;
  private File ksfile = null;
  private InputStream ksStream = null;
  private InputStream inStream = null;
  private KeyStore keyStore = null;
  private boolean token = false;
  private boolean nullStream = false;
  private boolean kssave = false;
  private boolean noprompt = false;
  private boolean trustcacerts = false;
  private boolean protectedPath = false;
  private boolean srcprotectedPath = false;
  private CertificateFactory cf = null;
  private KeyStore caks = null;
  private char[] srcstorePass = null;
  private String srcstoretype = null;
  private Set<char[]> passwords = new HashSet();
  private String startDate = null;
  private static final int CERTREQ = 1;
  private static final int CHANGEALIAS = 2;
  private static final int DELETE = 3;
  private static final int EXPORTCERT = 4;
  private static final int GENKEYPAIR = 5;
  private static final int GENSECKEY = 6;
  private static final int IDENTITYDB = 7;
  private static final int IMPORTCERT = 8;
  private static final int IMPORTKEYSTORE = 9;
  private static final int KEYCLONE = 10;
  private static final int KEYPASSWD = 11;
  private static final int LIST = 12;
  private static final int PRINTCERT = 13;
  private static final int SELFCERT = 14;
  private static final int STOREPASSWD = 15;
  private static final Class[] PARAM_STRING = { String.class };
  private static final String JKS = "jks";
  private static final String NONE = "NONE";
  private static final String P11KEYSTORE = "PKCS11";
  private static final String P12KEYSTORE = "PKCS12";
  private final String keyAlias = "mykey";
  private static final ResourceBundle rb = ResourceBundle.getBundle("sun.security.util.Resources");
  private static final Collator collator = Collator.getInstance();

  public static void main(String[] paramArrayOfString)
    throws Exception
  {
    KeyTool localKeyTool = new KeyTool();
    localKeyTool.run(paramArrayOfString, System.out);
  }

  public void run(String[] paramArrayOfString, PrintStream paramPrintStream)
    throws Exception
  {
    char[] arrayOfChar1;
    try
    {
      parseArgs(paramArrayOfString);
      doCommands(paramPrintStream);
    }
    catch (Exception localIterator2)
    {
      Iterator localIterator1;
      System.out.println(rb.getString("keytool error: ") + localException);
      if (this.verbose)
        localException.printStackTrace(System.out);
      if (!(this.debug))
        System.exit(1);
      else
        throw localException;
    }
    finally
    {
      Iterator localIterator2;
      Iterator localIterator3 = this.passwords.iterator();
      while (localIterator3.hasNext())
      {
        char[] arrayOfChar2 = (char[])localIterator3.next();
        if (arrayOfChar2 != null)
        {
          Arrays.fill(arrayOfChar2, ' ');
          arrayOfChar2 = null;
        }
      }
    }
  }

  void parseArgs(String[] paramArrayOfString)
  {
    Object localObject1;
    Object localObject2;
    if (paramArrayOfString.length == 0)
      usage();
    int i = 0;
    for (i = 0; (i < paramArrayOfString.length) && (paramArrayOfString[i].startsWith("-")); ++i)
    {
      localObject1 = paramArrayOfString[i];
      if (collator.compare((String)localObject1, "-certreq") == 0)
      {
        this.command = 1;
      }
      else if (collator.compare((String)localObject1, "-delete") == 0)
      {
        this.command = 3;
      }
      else if ((collator.compare((String)localObject1, "-export") == 0) || (collator.compare((String)localObject1, "-exportcert") == 0))
      {
        this.command = 4;
      }
      else if ((collator.compare((String)localObject1, "-genkey") == 0) || (collator.compare((String)localObject1, "-genkeypair") == 0))
      {
        this.command = 5;
      }
      else
      {
        if (collator.compare((String)localObject1, "-help") == 0)
        {
          usage();
          return;
        }
        if (collator.compare((String)localObject1, "-identitydb") == 0)
        {
          this.command = 7;
        }
        else if ((collator.compare((String)localObject1, "-import") == 0) || (collator.compare((String)localObject1, "-importcert") == 0))
        {
          this.command = 8;
        }
        else if (collator.compare((String)localObject1, "-keyclone") == 0)
        {
          this.command = 10;
        }
        else if (collator.compare((String)localObject1, "-changealias") == 0)
        {
          this.command = 2;
        }
        else if (collator.compare((String)localObject1, "-keypasswd") == 0)
        {
          this.command = 11;
        }
        else if (collator.compare((String)localObject1, "-list") == 0)
        {
          this.command = 12;
        }
        else if (collator.compare((String)localObject1, "-printcert") == 0)
        {
          this.command = 13;
        }
        else if (collator.compare((String)localObject1, "-selfcert") == 0)
        {
          this.command = 14;
        }
        else if (collator.compare((String)localObject1, "-storepasswd") == 0)
        {
          this.command = 15;
        }
        else if (collator.compare((String)localObject1, "-importkeystore") == 0)
        {
          this.command = 9;
        }
        else if (collator.compare((String)localObject1, "-genseckey") == 0)
        {
          this.command = 6;
        }
        else if ((collator.compare((String)localObject1, "-keystore") == 0) || (collator.compare((String)localObject1, "-destkeystore") == 0))
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.ksfname = paramArrayOfString[i];
        }
        else if ((collator.compare((String)localObject1, "-storepass") == 0) || (collator.compare((String)localObject1, "-deststorepass") == 0))
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.storePass = paramArrayOfString[i].toCharArray();
          this.passwords.add(this.storePass);
        }
        else if ((collator.compare((String)localObject1, "-storetype") == 0) || (collator.compare((String)localObject1, "-deststoretype") == 0))
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.storetype = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-srcstorepass") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.srcstorePass = paramArrayOfString[i].toCharArray();
          this.passwords.add(this.srcstorePass);
        }
        else if (collator.compare((String)localObject1, "-srcstoretype") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.srcstoretype = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-srckeypass") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.srckeyPass = paramArrayOfString[i].toCharArray();
          this.passwords.add(this.srckeyPass);
        }
        else if (collator.compare((String)localObject1, "-srcprovidername") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.srcProviderName = paramArrayOfString[i];
        }
        else if ((collator.compare((String)localObject1, "-providername") == 0) || (collator.compare((String)localObject1, "-destprovidername") == 0))
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.providerName = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-providerpath") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.pathlist = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-keypass") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.keyPass = paramArrayOfString[i].toCharArray();
          this.passwords.add(this.keyPass);
        }
        else if (collator.compare((String)localObject1, "-new") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.newPass = paramArrayOfString[i].toCharArray();
          this.passwords.add(this.newPass);
        }
        else if (collator.compare((String)localObject1, "-destkeypass") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.destKeyPass = paramArrayOfString[i].toCharArray();
          this.passwords.add(this.destKeyPass);
        }
        else if ((collator.compare((String)localObject1, "-alias") == 0) || (collator.compare((String)localObject1, "-srcalias") == 0))
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.alias = paramArrayOfString[i];
        }
        else if ((collator.compare((String)localObject1, "-dest") == 0) || (collator.compare((String)localObject1, "-destalias") == 0))
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.dest = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-dname") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.dname = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-keysize") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.keysize = Integer.parseInt(paramArrayOfString[i]);
        }
        else if (collator.compare((String)localObject1, "-keyalg") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.keyAlgName = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-sigalg") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.sigAlgName = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-startdate") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.startDate = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-validity") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.validity = Long.parseLong(paramArrayOfString[i]);
        }
        else if (collator.compare((String)localObject1, "-file") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.filename = paramArrayOfString[i];
        }
        else if (collator.compare((String)localObject1, "-srckeystore") == 0)
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          this.srcksfname = paramArrayOfString[i];
        }
        else if ((collator.compare((String)localObject1, "-provider") == 0) || (collator.compare((String)localObject1, "-providerclass") == 0))
        {
          if (++i == paramArrayOfString.length)
            errorNeedArgument((String)localObject1);
          if (this.providers == null)
            this.providers = new HashSet(3);
          localObject2 = paramArrayOfString[i];
          String str = null;
          if (paramArrayOfString.length > i + 1)
          {
            localObject1 = paramArrayOfString[(i + 1)];
            if (collator.compare((String)localObject1, "-providerarg") == 0)
            {
              if (paramArrayOfString.length == i + 2)
                errorNeedArgument((String)localObject1);
              str = paramArrayOfString[(i + 2)];
              i += 2;
            }
          }
          this.providers.add(new Pair(localObject2, str));
        }
        else if (collator.compare((String)localObject1, "-v") == 0)
        {
          this.verbose = true;
        }
        else if (collator.compare((String)localObject1, "-debug") == 0)
        {
          this.debug = true;
        }
        else if (collator.compare((String)localObject1, "-rfc") == 0)
        {
          this.rfc = true;
        }
        else if (collator.compare((String)localObject1, "-noprompt") == 0)
        {
          this.noprompt = true;
        }
        else if (collator.compare((String)localObject1, "-trustcacerts") == 0)
        {
          this.trustcacerts = true;
        }
        else if ((collator.compare((String)localObject1, "-protected") == 0) || (collator.compare((String)localObject1, "-destprotected") == 0))
        {
          this.protectedPath = true;
        }
        else if (collator.compare((String)localObject1, "-srcprotected") == 0)
        {
          this.srcprotectedPath = true;
        }
        else
        {
          System.err.println(rb.getString("Illegal option:  ") + ((String)localObject1));
          tinyHelp();
        }
      }
    }
    if (i < paramArrayOfString.length)
    {
      localObject1 = new MessageFormat(rb.getString("Usage error, <arg> is not a legal command"));
      localObject2 = { paramArrayOfString[i] };
      throw new RuntimeException(((MessageFormat)localObject1).format(localObject2));
    }
    if (this.command == -1)
    {
      System.err.println(rb.getString("Usage error: no command provided"));
      tinyHelp();
    }
  }

  void doCommands(PrintStream paramPrintStream)
    throws Exception
  {
    label1902: Object localObject2;
    Object localObject3;
    if (this.storetype == null)
      this.storetype = KeyStore.getDefaultType();
    this.storetype = KeyStoreUtil.niceStoreTypeName(this.storetype);
    if (this.srcstoretype == null)
      this.srcstoretype = KeyStore.getDefaultType();
    this.srcstoretype = KeyStoreUtil.niceStoreTypeName(this.srcstoretype);
    if (("PKCS11".equalsIgnoreCase(this.storetype)) || (KeyStoreUtil.isWindowsKeyStore(this.storetype)))
    {
      this.token = true;
      if (this.ksfname == null)
        this.ksfname = "NONE";
    }
    if ("NONE".equals(this.ksfname))
      this.nullStream = true;
    if ((this.token) && (!(this.nullStream)))
    {
      System.err.println(MessageFormat.format(rb.getString("-keystore must be NONE if -storetype is {0}"), new Object[] { this.storetype }));
      System.err.println();
      tinyHelp();
    }
    if ((this.token) && (((this.command == 11) || (this.command == 15))))
      throw new UnsupportedOperationException(MessageFormat.format(rb.getString("-storepasswd and -keypasswd commands not supported if -storetype is {0}"), new Object[] { this.storetype }));
    if (("PKCS12".equalsIgnoreCase(this.storetype)) && (this.command == 11))
      throw new UnsupportedOperationException(rb.getString("-keypasswd commands not supported if -storetype is PKCS12"));
    if ((this.token) && (((this.keyPass != null) || (this.newPass != null) || (this.destKeyPass != null))))
      throw new IllegalArgumentException(MessageFormat.format(rb.getString("-keypass and -new can not be specified if -storetype is {0}"), new Object[] { this.storetype }));
    if ((this.protectedPath) && (((this.storePass != null) || (this.keyPass != null) || (this.newPass != null) || (this.destKeyPass != null))))
      throw new IllegalArgumentException(rb.getString("if -protected is specified, then -storepass, -keypass, and -new must not be specified"));
    if ((this.srcprotectedPath) && (((this.srcstorePass != null) || (this.srckeyPass != null))))
      throw new IllegalArgumentException(rb.getString("if -srcprotected is specified, then -srcstorepass and -srckeypass must not be specified"));
    if ((KeyStoreUtil.isWindowsKeyStore(this.storetype)) && (((this.storePass != null) || (this.keyPass != null) || (this.newPass != null) || (this.destKeyPass != null))))
      throw new IllegalArgumentException(rb.getString("if keystore is not password protected, then -storepass, -keypass, and -new must not be specified"));
    if ((KeyStoreUtil.isWindowsKeyStore(this.srcstoretype)) && (((this.srcstorePass != null) || (this.srckeyPass != null))))
      throw new IllegalArgumentException(rb.getString("if source keystore is not password protected, then -srcstorepass and -srckeypass must not be specified"));
    if (this.validity <= 3412046810217185280L)
      throw new Exception(rb.getString("Validity must be greater than zero"));
    if (this.providers != null)
    {
      Object localObject4;
      Object localObject1 = null;
      if (this.pathlist != null)
      {
        localObject3 = null;
        localObject3 = PathList.appendPath((String)localObject3, System.getProperty("java.class.path"));
        localObject3 = PathList.appendPath((String)localObject3, System.getProperty("env.class.path"));
        localObject3 = PathList.appendPath((String)localObject3, this.pathlist);
        localObject4 = PathList.pathToURLs((String)localObject3);
        localObject1 = new URLClassLoader(localObject4);
      }
      else
      {
        localObject1 = ClassLoader.getSystemClassLoader();
      }
      localObject3 = this.providers.iterator();
      while (((Iterator)localObject3).hasNext())
      {
        Class localClass;
        Object localObject5;
        Object localObject6;
        localObject4 = (Pair)((Iterator)localObject3).next();
        String str1 = (String)((Pair)localObject4).fst;
        if (localObject1 != null)
          localClass = ((ClassLoader)localObject1).loadClass(str1);
        else
          localClass = Class.forName(str1);
        String str2 = (String)((Pair)localObject4).snd;
        if (str2 == null)
        {
          localObject5 = localClass.newInstance();
        }
        else
        {
          localObject6 = localClass.getConstructor(PARAM_STRING);
          localObject5 = ((Constructor)localObject6).newInstance(new Object[] { str2 });
        }
        if (!(localObject5 instanceof Provider))
        {
          localObject6 = new MessageFormat(rb.getString("provName not a provider"));
          Object[] arrayOfObject = { str1 };
          throw new Exception(((MessageFormat)localObject6).format(arrayOfObject));
        }
        Security.addProvider((Provider)localObject5);
      }
    }
    if ((this.command == 12) && (this.verbose) && (this.rfc))
    {
      System.err.println(rb.getString("Must not specify both -v and -rfc with 'list' command"));
      tinyHelp();
    }
    if ((this.command == 5) && (this.keyPass != null) && (this.keyPass.length < 6))
      throw new Exception(rb.getString("Key password must be at least 6 characters"));
    if ((this.newPass != null) && (this.newPass.length < 6))
      throw new Exception(rb.getString("New password must be at least 6 characters"));
    if ((this.destKeyPass != null) && (this.destKeyPass.length < 6))
      throw new Exception(rb.getString("New password must be at least 6 characters"));
    if (this.command != 13)
    {
      if (this.ksfname == null)
        this.ksfname = System.getProperty("user.home") + File.separator + ".keystore";
      if (!(this.nullStream))
        try
        {
          this.ksfile = new File(this.ksfname);
          if ((this.ksfile.exists()) && (this.ksfile.length() == 3412039732111081472L))
            throw new Exception(rb.getString("Keystore file exists, but is empty: ") + this.ksfname);
          this.ksStream = new FileInputStream(this.ksfile);
        }
        catch (FileNotFoundException localFileNotFoundException)
        {
          if ((this.command != 5) && (this.command != 6) && (this.command != 7) && (this.command != 8) && (this.command != 9))
            throw new Exception(rb.getString("Keystore file does not exist: ") + this.ksfname);
        }
    }
    if ((((this.command == 10) || (this.command == 2))) && (this.dest == null))
    {
      this.dest = getAlias("destination");
      if ("".equals(this.dest))
        throw new Exception(rb.getString("Must specify destination alias"));
    }
    if ((this.command == 3) && (this.alias == null))
    {
      this.alias = getAlias(null);
      if ("".equals(this.alias))
        throw new Exception(rb.getString("Must specify alias"));
    }
    if (this.providerName == null)
      this.keyStore = KeyStore.getInstance(this.storetype);
    else
      this.keyStore = KeyStore.getInstance(this.storetype, this.providerName);
    if (!(this.nullStream))
    {
      this.keyStore.load(this.ksStream, this.storePass);
      if (this.ksStream != null)
        this.ksStream.close();
    }
    if ((this.nullStream) && (this.storePass != null))
    {
      this.keyStore.load(null, this.storePass);
    }
    else
    {
      if ((!(this.nullStream)) && (this.storePass != null))
      {
        if ((this.ksStream != null) || (this.storePass.length >= 6))
          break label1902;
        throw new Exception(rb.getString("Keystore password must be at least 6 characters"));
      }
      if (this.storePass == null)
      {
        if ((!(this.protectedPath)) && (!(KeyStoreUtil.isWindowsKeyStore(this.storetype))) && (((this.command == 1) || (this.command == 3) || (this.command == 5) || (this.command == 6) || (this.command == 8) || (this.command == 9) || (this.command == 10) || (this.command == 2) || (this.command == 14) || (this.command == 15) || (this.command == 11) || (this.command == 7))))
        {
          int i = 0;
          do
          {
            if (this.command == 9)
              System.err.print(rb.getString("Enter destination keystore password:  "));
            else
              System.err.print(rb.getString("Enter keystore password:  "));
            System.err.flush();
            this.storePass = Password.readPassword(System.in);
            this.passwords.add(this.storePass);
            if ((!(this.nullStream)) && (((this.storePass == null) || (this.storePass.length < 6))))
            {
              System.err.println(rb.getString("Keystore password is too short - must be at least 6 characters"));
              this.storePass = null;
            }
            if ((this.storePass != null) && (!(this.nullStream)) && (this.ksStream == null))
            {
              System.err.print(rb.getString("Re-enter new password: "));
              localObject3 = Password.readPassword(System.in);
              this.passwords.add(localObject3);
              if (!(Arrays.equals(this.storePass, localObject3)))
              {
                System.err.println(rb.getString("They don't match. Try again"));
                this.storePass = null;
              }
            }
            ++i;
          }
          while ((this.storePass == null) && (i < 3));
          if (this.storePass == null)
          {
            System.err.println(rb.getString("Too many failures - try later"));
            return;
          }
        }
        else if ((!(this.protectedPath)) && (!(KeyStoreUtil.isWindowsKeyStore(this.storetype))) && (this.command != 13))
        {
          System.err.print(rb.getString("Enter keystore password:  "));
          System.err.flush();
          this.storePass = Password.readPassword(System.in);
          this.passwords.add(this.storePass);
        }
        if (this.nullStream)
        {
          this.keyStore.load(null, this.storePass);
        }
        else if (this.ksStream != null)
        {
          this.ksStream = new FileInputStream(this.ksfile);
          this.keyStore.load(this.ksStream, this.storePass);
          this.ksStream.close();
        }
      }
    }
    if ((this.storePass != null) && ("PKCS12".equalsIgnoreCase(this.storetype)))
    {
      localObject2 = new MessageFormat(rb.getString("Warning:  Different store and key passwords not supported for PKCS12 KeyStores. Ignoring user-specified <command> value."));
      if ((this.keyPass != null) && (!(Arrays.equals(this.storePass, this.keyPass))))
      {
        localObject3 = { "-keypass" };
        System.err.println(((MessageFormat)localObject2).format(localObject3));
        this.keyPass = this.storePass;
      }
      if ((this.newPass != null) && (!(Arrays.equals(this.storePass, this.newPass))))
      {
        localObject3 = { "-new" };
        System.err.println(((MessageFormat)localObject2).format(localObject3));
        this.newPass = this.storePass;
      }
      if ((this.destKeyPass != null) && (!(Arrays.equals(this.storePass, this.destKeyPass))))
      {
        localObject3 = { "-destkeypass" };
        System.err.println(((MessageFormat)localObject2).format(localObject3));
        this.destKeyPass = this.storePass;
      }
    }
    if ((this.command == 13) || (this.command == 8) || (this.command == 7))
      this.cf = CertificateFactory.getInstance("X509");
    if (this.trustcacerts)
      this.caks = getCacertsKeyStore();
    if (this.command == 1)
    {
      if (this.filename != null)
      {
        localObject2 = new PrintStream(new FileOutputStream(this.filename));
        paramPrintStream = (PrintStream)localObject2;
      }
      doCertReq(this.alias, this.sigAlgName, paramPrintStream);
      if ((this.verbose) && (this.filename != null))
      {
        localObject2 = new MessageFormat(rb.getString("Certification request stored in file <filename>"));
        localObject3 = { this.filename };
        System.err.println(((MessageFormat)localObject2).format(localObject3));
        System.err.println(rb.getString("Submit this to your CA"));
      }
    }
    else if (this.command == 3)
    {
      doDeleteEntry(this.alias);
      this.kssave = true;
    }
    else if (this.command == 4)
    {
      if (this.filename != null)
      {
        localObject2 = new PrintStream(new FileOutputStream(this.filename));
        paramPrintStream = (PrintStream)localObject2;
      }
      doExportCert(this.alias, paramPrintStream);
      if (this.filename != null)
      {
        localObject2 = new MessageFormat(rb.getString("Certificate stored in file <filename>"));
        localObject3 = { this.filename };
        System.err.println(((MessageFormat)localObject2).format(localObject3));
      }
    }
    else if (this.command == 5)
    {
      if (this.keyAlgName == null)
        this.keyAlgName = "DSA";
      doGenKeyPair(this.alias, this.dname, this.keyAlgName, this.keysize, this.sigAlgName);
      this.kssave = true;
    }
    else if (this.command == 6)
    {
      if (this.keyAlgName == null)
        this.keyAlgName = "DES";
      doGenSecretKey(this.alias, this.keyAlgName, this.keysize);
      this.kssave = true;
    }
    else if (this.command == 7)
    {
      localObject2 = System.in;
      if (this.filename != null)
        localObject2 = new FileInputStream(this.filename);
      doImportIdentityDatabase((InputStream)localObject2);
    }
    else if (this.command == 8)
    {
      localObject2 = System.in;
      if (this.filename != null)
        localObject2 = new FileInputStream(this.filename);
      localObject3 = (this.alias != null) ? this.alias : "mykey";
      if (this.keyStore.entryInstanceOf((String)localObject3, KeyStore.PrivateKeyEntry.class))
      {
        this.kssave = installReply((String)localObject3, (InputStream)localObject2);
        if (this.kssave)
          System.err.println(rb.getString("Certificate reply was installed in keystore"));
        else
          System.err.println(rb.getString("Certificate reply was not installed in keystore"));
      }
      else if ((!(this.keyStore.containsAlias((String)localObject3))) || (this.keyStore.entryInstanceOf((String)localObject3, KeyStore.TrustedCertificateEntry.class)))
      {
        this.kssave = addTrustedCert((String)localObject3, (InputStream)localObject2);
        if (this.kssave)
          System.err.println(rb.getString("Certificate was added to keystore"));
        else
          System.err.println(rb.getString("Certificate was not added to keystore"));
      }
    }
    else if (this.command == 9)
    {
      doImportKeyStore();
      this.kssave = true;
    }
    else if (this.command == 10)
    {
      this.keyPassNew = this.newPass;
      if (this.alias == null)
        this.alias = "mykey";
      if (!(this.keyStore.containsAlias(this.alias)))
      {
        localObject2 = new MessageFormat(rb.getString("Alias <alias> does not exist"));
        localObject3 = { this.alias };
        throw new Exception(((MessageFormat)localObject2).format(localObject3));
      }
      if (!(this.keyStore.entryInstanceOf(this.alias, KeyStore.PrivateKeyEntry.class)))
      {
        localObject2 = new MessageFormat(rb.getString("Alias <alias> references an entry type that is not a private key entry.  The -keyclone command only supports cloning of private key entries"));
        localObject3 = { this.alias };
        throw new Exception(((MessageFormat)localObject2).format(localObject3));
      }
      doCloneEntry(this.alias, this.dest, true);
      this.kssave = true;
    }
    else if (this.command == 2)
    {
      if (this.alias == null)
        this.alias = "mykey";
      doCloneEntry(this.alias, this.dest, false);
      if (this.keyStore.containsAlias(this.alias))
        doDeleteEntry(this.alias);
      this.kssave = true;
    }
    else if (this.command == 11)
    {
      this.keyPassNew = this.newPass;
      doChangeKeyPasswd(this.alias);
      this.kssave = true;
    }
    else if (this.command == 12)
    {
      if (this.alias != null)
        doPrintEntry(this.alias, paramPrintStream, true);
      else
        doPrintEntries(paramPrintStream);
    }
    else if (this.command == 13)
    {
      localObject2 = System.in;
      if (this.filename != null)
        localObject2 = new FileInputStream(this.filename);
      doPrintCert((InputStream)localObject2, paramPrintStream);
    }
    else if (this.command == 14)
    {
      doSelfCert(this.alias, this.dname, this.sigAlgName);
      this.kssave = true;
    }
    else if (this.command == 15)
    {
      this.storePassNew = this.newPass;
      if (this.storePassNew == null)
        this.storePassNew = getNewPasswd("keystore password", this.storePass);
      this.kssave = true;
    }
    if (this.kssave)
    {
      if (this.verbose)
      {
        localObject2 = new MessageFormat(rb.getString("[Storing ksfname]"));
        localObject3 = { (this.nullStream) ? "keystore" : this.ksfname };
        System.err.println(((MessageFormat)localObject2).format(localObject3));
      }
      if (this.token)
      {
        this.keyStore.store(null, null);
      }
      else
      {
        localObject2 = new FileOutputStream(this.ksfname);
        this.keyStore.store((OutputStream)localObject2, (this.storePassNew != null) ? this.storePassNew : this.storePass);
        if (localObject2 != null)
          ((FileOutputStream)localObject2).close();
      }
    }
  }

  private void doCertReq(String paramString1, String paramString2, PrintStream paramPrintStream)
    throws Exception
  {
    if (paramString1 == null)
      paramString1 = "mykey";
    Object[] arrayOfObject = recoverKey(paramString1, this.storePass, this.keyPass);
    PrivateKey localPrivateKey = (PrivateKey)arrayOfObject[0];
    if (this.keyPass == null)
      this.keyPass = ((char[])(char[])arrayOfObject[1]);
    java.security.cert.Certificate localCertificate = this.keyStore.getCertificate(paramString1);
    if (localCertificate == null)
    {
      localObject1 = new MessageFormat(rb.getString("alias has no public key (certificate)"));
      localObject2 = { paramString1 };
      throw new Exception(((MessageFormat)localObject1).format(localObject2));
    }
    Object localObject1 = new PKCS10(localCertificate.getPublicKey());
    if (paramString2 == null)
    {
      localObject2 = localPrivateKey.getAlgorithm();
      if (("DSA".equalsIgnoreCase((String)localObject2)) || ("DSS".equalsIgnoreCase((String)localObject2)))
        paramString2 = "SHA1WithDSA";
      else if ("RSA".equalsIgnoreCase((String)localObject2))
        paramString2 = "SHA1WithRSA";
      else
        throw new Exception(rb.getString("Cannot derive signature algorithm"));
    }
    Object localObject2 = Signature.getInstance(paramString2);
    ((Signature)localObject2).initSign(localPrivateKey);
    X500Name localX500Name = new X500Name(((X509Certificate)localCertificate).getSubjectDN().toString());
    X500Signer localX500Signer = new X500Signer((Signature)localObject2, localX500Name);
    ((PKCS10)localObject1).encodeAndSign(localX500Signer);
    ((PKCS10)localObject1).print(paramPrintStream);
  }

  private void doDeleteEntry(String paramString)
    throws Exception
  {
    if (!(this.keyStore.containsAlias(paramString)))
    {
      MessageFormat localMessageFormat = new MessageFormat(rb.getString("Alias <alias> does not exist"));
      Object[] arrayOfObject = { paramString };
      throw new Exception(localMessageFormat.format(arrayOfObject));
    }
    this.keyStore.deleteEntry(paramString);
  }

  private void doExportCert(String paramString, PrintStream paramPrintStream)
    throws Exception
  {
    Object localObject2;
    if ((this.storePass == null) && (!(KeyStoreUtil.isWindowsKeyStore(this.storetype))))
      printWarning();
    if (paramString == null)
      paramString = "mykey";
    if (!(this.keyStore.containsAlias(paramString)))
    {
      localObject1 = new MessageFormat(rb.getString("Alias <alias> does not exist"));
      localObject2 = { paramString };
      throw new Exception(((MessageFormat)localObject1).format(localObject2));
    }
    Object localObject1 = (X509Certificate)this.keyStore.getCertificate(paramString);
    if (localObject1 == null)
    {
      localObject2 = new MessageFormat(rb.getString("Alias <alias> has no certificate"));
      Object[] arrayOfObject = { paramString };
      throw new Exception(((MessageFormat)localObject2).format(arrayOfObject));
    }
    dumpCert((java.security.cert.Certificate)localObject1, paramPrintStream);
  }

  private char[] promptForKeyPass(String paramString1, String paramString2, char[] paramArrayOfChar)
    throws Exception
  {
    if ("PKCS12".equalsIgnoreCase(this.storetype))
      return paramArrayOfChar;
    if (!(this.token))
    {
      for (int i = 0; i < 3; ++i)
      {
        MessageFormat localMessageFormat = new MessageFormat(rb.getString("Enter key password for <alias>"));
        Object[] arrayOfObject = { paramString1 };
        System.err.println(localMessageFormat.format(arrayOfObject));
        if (paramString2 == null)
        {
          System.err.print(rb.getString("\t(RETURN if same as keystore password):  "));
        }
        else
        {
          localMessageFormat = new MessageFormat(rb.getString("\t(RETURN if same as for <otherAlias>)"));
          localObject = { paramString2 };
          System.err.print(localMessageFormat.format(localObject));
        }
        System.err.flush();
        Object localObject = Password.readPassword(System.in);
        this.passwords.add(localObject);
        if (localObject == null)
          return paramArrayOfChar;
        if (localObject.length >= 6)
        {
          System.err.print(rb.getString("Re-enter new password: "));
          char[] arrayOfChar = Password.readPassword(System.in);
          this.passwords.add(arrayOfChar);
          if (!(Arrays.equals(localObject, arrayOfChar)))
          {
            System.err.println(rb.getString("They don't match. Try again"));
            break label254:
          }
          return localObject;
        }
        label254: System.err.println(rb.getString("Key password is too short - must be at least 6 characters"));
      }
      if (i == 3)
      {
        if (this.command == 10)
          throw new Exception(rb.getString("Too many failures. Key entry not cloned"));
        throw new Exception(rb.getString("Too many failures - key not added to keystore"));
      }
    }
    return ((C)null);
  }

  private void doGenSecretKey(String paramString1, String paramString2, int paramInt)
    throws Exception
  {
    if (paramString1 == null)
      paramString1 = "mykey";
    if (this.keyStore.containsAlias(paramString1))
    {
      localObject1 = new MessageFormat(rb.getString("Secret key not generated, alias <alias> already exists"));
      localObject2 = { paramString1 };
      throw new Exception(((MessageFormat)localObject1).format(localObject2));
    }
    Object localObject1 = null;
    Object localObject2 = KeyGenerator.getInstance(paramString2);
    if (paramInt != -1)
      ((KeyGenerator)localObject2).init(paramInt);
    else if ("DES".equalsIgnoreCase(paramString2))
      ((KeyGenerator)localObject2).init(56);
    else if ("DESede".equalsIgnoreCase(paramString2))
      ((KeyGenerator)localObject2).init(168);
    else
      throw new Exception(rb.getString("Please provide -keysize for secret key generation"));
    localObject1 = ((KeyGenerator)localObject2).generateKey();
    if (this.keyPass == null)
      this.keyPass = promptForKeyPass(paramString1, null, this.storePass);
    this.keyStore.setKeyEntry(paramString1, (Key)localObject1, this.keyPass, null);
  }

  private void doGenKeyPair(String paramString1, String paramString2, String paramString3, int paramInt, String paramString4)
    throws Exception
  {
    Object localObject2;
    if (paramInt == -1)
      if ("EC".equalsIgnoreCase(paramString3))
        paramInt = 256;
      else
        paramInt = 1024;
    if (paramString1 == null)
      paramString1 = "mykey";
    if (this.keyStore.containsAlias(paramString1))
    {
      localObject1 = new MessageFormat(rb.getString("Key pair not generated, alias <alias> already exists"));
      localObject2 = { paramString1 };
      throw new Exception(((MessageFormat)localObject1).format(localObject2));
    }
    if (paramString4 == null)
      if ("DSA".equalsIgnoreCase(paramString3))
        paramString4 = "SHA1WithDSA";
      else if ("RSA".equalsIgnoreCase(paramString3))
        paramString4 = "SHA1WithRSA";
      else if ("EC".equalsIgnoreCase(paramString3))
        paramString4 = "SHA1withECDSA";
      else
        throw new Exception(rb.getString("Cannot derive signature algorithm"));
    Object localObject1 = new CertAndKeyGen(paramString3, paramString4, this.providerName);
    if (paramString2 == null)
      localObject2 = getX500Name();
    else
      localObject2 = new X500Name(paramString2);
    ((CertAndKeyGen)localObject1).generate(paramInt);
    PrivateKey localPrivateKey = ((CertAndKeyGen)localObject1).getPrivateKey();
    X509Certificate[] arrayOfX509Certificate = new X509Certificate[1];
    arrayOfX509Certificate[0] = ((CertAndKeyGen)localObject1).getSelfCertificate((X500Name)localObject2, getStartDate(this.startDate), this.validity * 24L * 60L * 60L);
    if (this.verbose)
    {
      MessageFormat localMessageFormat = new MessageFormat(rb.getString("Generating keysize bit keyAlgName key pair and self-signed certificate (sigAlgName) with a validity of validality days\n\tfor: x500Name"));
      Object[] arrayOfObject = { new Integer(paramInt), localPrivateKey.getAlgorithm(), arrayOfX509Certificate[0].getSigAlgName(), new Long(this.validity), localObject2 };
      System.err.println(localMessageFormat.format(arrayOfObject));
    }
    if (this.keyPass == null)
      this.keyPass = promptForKeyPass(paramString1, null, this.storePass);
    this.keyStore.setKeyEntry(paramString1, localPrivateKey, this.keyPass, arrayOfX509Certificate);
  }

  private void doCloneEntry(String paramString1, String paramString2, boolean paramBoolean)
    throws Exception
  {
    if (paramString1 == null)
      paramString1 = "mykey";
    if (this.keyStore.containsAlias(paramString2))
    {
      localObject1 = new MessageFormat(rb.getString("Destination alias <dest> already exists"));
      localObject2 = { paramString2 };
      throw new Exception(((MessageFormat)localObject1).format(localObject2));
    }
    Object localObject1 = recoverEntry(this.keyStore, paramString1, this.storePass, this.keyPass);
    Object localObject2 = (KeyStore.Entry)localObject1[0];
    this.keyPass = ((char[])(char[])localObject1[1]);
    KeyStore.PasswordProtection localPasswordProtection = null;
    if (this.keyPass != null)
    {
      if ((!(paramBoolean)) || ("PKCS12".equalsIgnoreCase(this.storetype)))
        this.keyPassNew = this.keyPass;
      else if (this.keyPassNew == null)
        this.keyPassNew = promptForKeyPass(paramString2, paramString1, this.keyPass);
      localPasswordProtection = new KeyStore.PasswordProtection(this.keyPassNew);
    }
    this.keyStore.setEntry(paramString2, (KeyStore.Entry)localObject2, localPasswordProtection);
  }

  private void doChangeKeyPasswd(String paramString)
    throws Exception
  {
    if (paramString == null)
      paramString = "mykey";
    Object[] arrayOfObject1 = recoverKey(paramString, this.storePass, this.keyPass);
    Key localKey = (Key)arrayOfObject1[0];
    if (this.keyPass == null)
      this.keyPass = ((char[])(char[])arrayOfObject1[1]);
    if (this.keyPassNew == null)
    {
      MessageFormat localMessageFormat = new MessageFormat(rb.getString("key password for <alias>"));
      Object[] arrayOfObject2 = { paramString };
      this.keyPassNew = getNewPasswd(localMessageFormat.format(arrayOfObject2), this.keyPass);
    }
    this.keyStore.setKeyEntry(paramString, localKey, this.keyPassNew, this.keyStore.getCertificateChain(paramString));
  }

  private void doImportIdentityDatabase(InputStream paramInputStream)
    throws Exception
  {
    java.security.cert.Certificate[] arrayOfCertificate = null;
    int i = 0;
    IdentityDatabase localIdentityDatabase = IdentityDatabase.fromStream(paramInputStream);
    Enumeration localEnumeration = localIdentityDatabase.identities();
    while (true)
    {
      label19: X509Certificate localX509Certificate;
      Identity localIdentity;
      Object localObject2;
      while (true)
      {
        if (!(localEnumeration.hasMoreElements()))
          break label371;
        localIdentity = (Identity)localEnumeration.nextElement();
        localX509Certificate = null;
        if ((((!(localIdentity instanceof SystemSigner)) || (!(((SystemSigner)localIdentity).isTrusted())))) && (((!(localIdentity instanceof SystemIdentity)) || (!(((SystemIdentity)localIdentity).isTrusted())))))
          break label368;
        if (!(this.keyStore.containsAlias(localIdentity.getName())))
          break;
        localObject1 = new MessageFormat(rb.getString("Keystore entry for <id.getName()> already exists"));
        localObject2 = { localIdentity.getName() };
        System.err.println(((MessageFormat)localObject1).format(localObject2));
      }
      Object localObject1 = localIdentity.certificates();
      if ((localObject1 != null) && (localObject1.length > 0))
      {
        Object localObject3;
        localObject2 = new DerOutputStream();
        localObject1[0].encode((OutputStream)localObject2);
        byte[] arrayOfByte = ((DerOutputStream)localObject2).toByteArray();
        ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(arrayOfByte);
        localX509Certificate = (X509Certificate)this.cf.generateCertificate(localByteArrayInputStream);
        localByteArrayInputStream.close();
        if (isSelfSigned(localX509Certificate))
        {
          localObject3 = localX509Certificate.getPublicKey();
          try
          {
            localX509Certificate.verify((PublicKey)localObject3);
          }
          catch (Exception localException)
          {
            break label19:
          }
        }
        if (localIdentity instanceof SystemSigner)
        {
          localObject3 = new MessageFormat(rb.getString("Creating keystore entry for <id.getName()> ..."));
          Object[] arrayOfObject = { localIdentity.getName() };
          System.err.println(((MessageFormat)localObject3).format(arrayOfObject));
          if (arrayOfCertificate == null)
            arrayOfCertificate = new java.security.cert.Certificate[1];
          arrayOfCertificate[0] = localX509Certificate;
          PrivateKey localPrivateKey = ((SystemSigner)localIdentity).getPrivateKey();
          this.keyStore.setKeyEntry(localIdentity.getName(), localPrivateKey, this.storePass, arrayOfCertificate);
        }
        else
        {
          this.keyStore.setCertificateEntry(localIdentity.getName(), localX509Certificate);
        }
        label368: this.kssave = true;
      }
    }
    if (!(this.kssave))
      label371: System.err.println(rb.getString("No entries from identity database added"));
  }

  private void doPrintEntry(String paramString, PrintStream paramPrintStream, boolean paramBoolean)
    throws Exception
  {
    Object localObject1;
    Object[] arrayOfObject1;
    Object localObject2;
    if ((this.storePass == null) && (paramBoolean) && (!(KeyStoreUtil.isWindowsKeyStore(this.storetype))))
      printWarning();
    if (!(this.keyStore.containsAlias(paramString)))
    {
      localObject1 = new MessageFormat(rb.getString("Alias <alias> does not exist"));
      arrayOfObject1 = { paramString };
      throw new Exception(((MessageFormat)localObject1).format(arrayOfObject1));
    }
    if ((this.verbose) || (this.rfc) || (this.debug))
    {
      localObject1 = new MessageFormat(rb.getString("Alias name: alias"));
      arrayOfObject1 = { paramString };
      paramPrintStream.println(((MessageFormat)localObject1).format(arrayOfObject1));
      if (!(this.token))
      {
        localObject1 = new MessageFormat(rb.getString("Creation date: keyStore.getCreationDate(alias)"));
        localObject2 = { this.keyStore.getCreationDate(paramString) };
        paramPrintStream.println(((MessageFormat)localObject1).format(localObject2));
      }
    }
    else if (!(this.token))
    {
      localObject1 = new MessageFormat(rb.getString("alias, keyStore.getCreationDate(alias), "));
      arrayOfObject1 = { paramString, this.keyStore.getCreationDate(paramString) };
      paramPrintStream.print(((MessageFormat)localObject1).format(arrayOfObject1));
    }
    else
    {
      localObject1 = new MessageFormat(rb.getString("alias, "));
      arrayOfObject1 = { paramString };
      paramPrintStream.print(((MessageFormat)localObject1).format(arrayOfObject1));
    }
    if (this.keyStore.entryInstanceOf(paramString, KeyStore.SecretKeyEntry.class))
    {
      if ((this.verbose) || (this.rfc) || (this.debug))
      {
        localObject1 = { "SecretKeyEntry" };
        paramPrintStream.println(new MessageFormat(rb.getString("Entry type: <type>")).format(localObject1));
      }
      else
      {
        paramPrintStream.println("SecretKeyEntry, ");
      }
    }
    else if (this.keyStore.entryInstanceOf(paramString, KeyStore.PrivateKeyEntry.class))
    {
      if ((this.verbose) || (this.rfc) || (this.debug))
      {
        localObject1 = { "PrivateKeyEntry" };
        paramPrintStream.println(new MessageFormat(rb.getString("Entry type: <type>")).format(localObject1));
      }
      else
      {
        paramPrintStream.println("PrivateKeyEntry, ");
      }
      localObject1 = this.keyStore.getCertificateChain(paramString);
      if (localObject1 != null)
        if ((this.verbose) || (this.rfc) || (this.debug))
        {
          paramPrintStream.println(rb.getString("Certificate chain length: ") + localObject1.length);
          for (int i = 0; i < localObject1.length; ++i)
          {
            localObject2 = new MessageFormat(rb.getString("Certificate[(i + 1)]:"));
            Object[] arrayOfObject2 = { new Integer(i + 1) };
            paramPrintStream.println(((MessageFormat)localObject2).format(arrayOfObject2));
            if ((this.verbose) && (localObject1[i] instanceof X509Certificate))
              printX509Cert((X509Certificate)(X509Certificate)localObject1[i], paramPrintStream);
            else if (this.debug)
              paramPrintStream.println(localObject1[i].toString());
            else
              dumpCert(localObject1[i], paramPrintStream);
          }
        }
        else
        {
          paramPrintStream.println(rb.getString("Certificate fingerprint (MD5): ") + getCertFingerPrint("MD5", localObject1[0]));
        }
    }
    else if (this.keyStore.entryInstanceOf(paramString, KeyStore.TrustedCertificateEntry.class))
    {
      localObject1 = this.keyStore.getCertificate(paramString);
      if ((this.verbose) && (localObject1 instanceof X509Certificate))
      {
        paramPrintStream.println(rb.getString("Entry type: trustedCertEntry\n"));
        printX509Cert((X509Certificate)localObject1, paramPrintStream);
      }
      else if (this.rfc)
      {
        paramPrintStream.println(rb.getString("Entry type: trustedCertEntry\n"));
        dumpCert((java.security.cert.Certificate)localObject1, paramPrintStream);
      }
      else if (this.debug)
      {
        paramPrintStream.println(((java.security.cert.Certificate)localObject1).toString());
      }
      else
      {
        paramPrintStream.println(rb.getString("trustedCertEntry,"));
        paramPrintStream.println(rb.getString("Certificate fingerprint (MD5): ") + getCertFingerPrint("MD5", (java.security.cert.Certificate)localObject1));
      }
    }
    else
    {
      paramPrintStream.println(rb.getString("Unknown Entry Type"));
    }
  }

  KeyStore loadSourceKeyStore()
    throws Exception
  {
    Object localObject;
    int i = 0;
    FileInputStream localFileInputStream = null;
    if (("PKCS11".equalsIgnoreCase(this.srcstoretype)) || (KeyStoreUtil.isWindowsKeyStore(this.srcstoretype)))
    {
      if (!("NONE".equals(this.srcksfname)))
      {
        System.err.println(MessageFormat.format(rb.getString("-keystore must be NONE if -storetype is {0}"), new Object[] { this.srcstoretype }));
        System.err.println();
        tinyHelp();
      }
      i = 1;
    }
    else if (this.srcksfname != null)
    {
      localObject = new File(this.srcksfname);
      if ((((File)localObject).exists()) && (((File)localObject).length() == 3412047617671036928L))
        throw new Exception(rb.getString("Source keystore file exists, but is empty: ") + this.srcksfname);
      localFileInputStream = new FileInputStream((File)localObject);
    }
    else
    {
      throw new Exception(rb.getString("Please specify -srckeystore"));
    }
    if (this.srcProviderName == null)
      localObject = KeyStore.getInstance(this.srcstoretype);
    else
      localObject = KeyStore.getInstance(this.srcstoretype, this.srcProviderName);
    if ((this.srcstorePass == null) && (!(this.srcprotectedPath)) && (!(KeyStoreUtil.isWindowsKeyStore(this.srcstoretype))))
    {
      System.err.print(rb.getString("Enter source keystore password:  "));
      System.err.flush();
      this.srcstorePass = Password.readPassword(System.in);
      this.passwords.add(this.srcstorePass);
    }
    if (("PKCS12".equalsIgnoreCase(this.srcstoretype)) && (this.srckeyPass != null) && (this.srcstorePass != null) && (!(Arrays.equals(this.srcstorePass, this.srckeyPass))))
    {
      MessageFormat localMessageFormat = new MessageFormat(rb.getString("Warning:  Different store and key passwords not supported for PKCS12 KeyStores. Ignoring user-specified <command> value."));
      Object[] arrayOfObject = { "-srckeypass" };
      System.err.println(localMessageFormat.format(arrayOfObject));
      this.srckeyPass = this.srcstorePass;
    }
    ((KeyStore)localObject).load(localFileInputStream, this.srcstorePass);
    if ((this.srcstorePass == null) && (!(KeyStoreUtil.isWindowsKeyStore(this.srcstoretype))))
    {
      System.err.println();
      System.err.println(rb.getString("*****************  WARNING WARNING WARNING  *****************"));
      System.err.println(rb.getString("* The integrity of the information stored in the srckeystore*"));
      System.err.println(rb.getString("* has NOT been verified!  In order to verify its integrity, *"));
      System.err.println(rb.getString("* you must provide the srckeystore password.                *"));
      System.err.println(rb.getString("*****************  WARNING WARNING WARNING  *****************"));
      System.err.println();
    }
    return ((KeyStore)localObject);
  }

  private void doImportKeyStore()
    throws Exception
  {
    if (this.alias != null)
    {
      doImportKeyStoreSingle(loadSourceKeyStore(), this.alias);
    }
    else
    {
      if ((this.dest != null) || (this.srckeyPass != null) || (this.destKeyPass != null))
        throw new Exception(rb.getString("if alias not specified, destalias, srckeypass, and destkeypass must not be specified"));
      doImportKeyStoreAll(loadSourceKeyStore());
    }
  }

  private int doImportKeyStoreSingle(KeyStore paramKeyStore, String paramString)
    throws Exception
  {
    String str = (this.dest == null) ? paramString : this.dest;
    if (this.keyStore.containsAlias(str))
    {
      arrayOfObject1 = { paramString };
      if (this.noprompt)
      {
        System.err.println(new MessageFormat(rb.getString("Warning: Overwriting existing alias <alias> in destination keystore")).format(arrayOfObject1));
      }
      else
      {
        localObject = getYesNoReply(new MessageFormat(rb.getString("Existing entry alias <alias> exists, overwrite? [no]:  ")).format(arrayOfObject1));
        if ("NO".equals(localObject))
        {
          str = inputStringFromStdin(rb.getString("Enter new alias name\t(RETURN to cancel import for this entry):  "));
          if ("".equals(str))
          {
            System.err.println(new MessageFormat(rb.getString("Entry for alias <alias> not imported.")).format(arrayOfObject1));
            return 0;
          }
        }
      }
    }
    Object[] arrayOfObject1 = recoverEntry(paramKeyStore, paramString, this.srcstorePass, this.srckeyPass);
    Object localObject = (KeyStore.Entry)arrayOfObject1[0];
    KeyStore.PasswordProtection localPasswordProtection = null;
    if (this.destKeyPass != null)
      localPasswordProtection = new KeyStore.PasswordProtection(this.destKeyPass);
    else if (arrayOfObject1[1] != null)
      localPasswordProtection = new KeyStore.PasswordProtection((char[])(char[])arrayOfObject1[1]);
    try
    {
      this.keyStore.setEntry(str, (KeyStore.Entry)localObject, localPasswordProtection);
      return 1;
    }
    catch (KeyStoreException localKeyStoreException)
    {
      Object[] arrayOfObject2 = { paramString, localKeyStoreException.toString() };
      MessageFormat localMessageFormat = new MessageFormat(rb.getString("Problem importing entry for alias <alias>: <exception>.\nEntry for alias <alias> not imported."));
      System.err.println(localMessageFormat.format(arrayOfObject2));
    }
    return 2;
  }

  private void doImportKeyStoreAll(KeyStore paramKeyStore)
    throws Exception
  {
    int i = 0;
    int j = paramKeyStore.size();
    Object localObject1 = paramKeyStore.aliases();
    while (((Enumeration)localObject1).hasMoreElements())
    {
      Object localObject3;
      localObject2 = (String)((Enumeration)localObject1).nextElement();
      int k = doImportKeyStoreSingle(paramKeyStore, (String)localObject2);
      if (k == 1)
      {
        ++i;
        localObject3 = { localObject2 };
        MessageFormat localMessageFormat = new MessageFormat(rb.getString("Entry for alias <alias> successfully imported."));
        System.err.println(localMessageFormat.format(localObject3));
      }
      else if ((k == 2) && (!(this.noprompt)))
      {
        localObject3 = getYesNoReply("Do you want to quit the import process? [no]:  ");
        if ("YES".equals(localObject3))
          break;
      }
    }
    localObject1 = { Integer.valueOf(i), Integer.valueOf(j - i) };
    Object localObject2 = new MessageFormat(rb.getString("Import command completed:  <ok> entries successfully imported, <fail> entries failed or cancelled"));
    System.err.println(((MessageFormat)localObject2).format(localObject1));
  }

  private void doPrintEntries(PrintStream paramPrintStream)
    throws Exception
  {
    if ((this.storePass == null) && (!(KeyStoreUtil.isWindowsKeyStore(this.storetype))))
      printWarning();
    else
      paramPrintStream.println();
    paramPrintStream.println(rb.getString("Keystore type: ") + this.keyStore.getType());
    paramPrintStream.println(rb.getString("Keystore provider: ") + this.keyStore.getProvider().getName());
    paramPrintStream.println();
    MessageFormat localMessageFormat = new MessageFormat(rb.getString("Your keystore contains keyStore.size() entries"));
    Object[] arrayOfObject = { new Integer(this.keyStore.size()) };
    paramPrintStream.println(localMessageFormat.format(arrayOfObject));
    paramPrintStream.println();
    Enumeration localEnumeration = this.keyStore.aliases();
    while (localEnumeration.hasMoreElements())
    {
      String str = (String)localEnumeration.nextElement();
      doPrintEntry(str, paramPrintStream, false);
      if ((this.verbose) || (this.rfc))
      {
        paramPrintStream.println(rb.getString("\n"));
        paramPrintStream.println(rb.getString("*******************************************"));
        paramPrintStream.println(rb.getString("*******************************************\n\n"));
      }
    }
  }

  private void doPrintCert(InputStream paramInputStream, PrintStream paramPrintStream)
    throws Exception
  {
    Collection localCollection = null;
    try
    {
      localCollection = this.cf.generateCertificates(paramInputStream);
    }
    catch (CertificateException localCertificateException)
    {
      throw new Exception(rb.getString("Failed to parse input"), localCertificateException);
    }
    if (localCollection.isEmpty())
      throw new Exception(rb.getString("Empty input"));
    java.security.cert.Certificate[] arrayOfCertificate = (java.security.cert.Certificate[])(java.security.cert.Certificate[])localCollection.toArray(new java.security.cert.Certificate[localCollection.size()]);
    for (int i = 0; i < arrayOfCertificate.length; ++i)
    {
      X509Certificate localX509Certificate = null;
      try
      {
        localX509Certificate = (X509Certificate)arrayOfCertificate[i];
      }
      catch (ClassCastException localClassCastException)
      {
        throw new Exception(rb.getString("Not X.509 certificate"));
      }
      if (arrayOfCertificate.length > 1)
      {
        MessageFormat localMessageFormat = new MessageFormat(rb.getString("Certificate[(i + 1)]:"));
        Object[] arrayOfObject = { new Integer(i + 1) };
        paramPrintStream.println(localMessageFormat.format(arrayOfObject));
      }
      printX509Cert(localX509Certificate, paramPrintStream);
      if (i < arrayOfCertificate.length - 1)
        paramPrintStream.println();
    }
  }

  private void doSelfCert(String paramString1, String paramString2, String paramString3)
    throws Exception
  {
    X500Name localX500Name;
    if (paramString1 == null)
      paramString1 = "mykey";
    Object[] arrayOfObject = recoverKey(paramString1, this.storePass, this.keyPass);
    PrivateKey localPrivateKey = (PrivateKey)arrayOfObject[0];
    if (this.keyPass == null)
      this.keyPass = ((char[])(char[])arrayOfObject[1]);
    if (paramString3 == null)
    {
      localObject1 = localPrivateKey.getAlgorithm();
      if (("DSA".equalsIgnoreCase((String)localObject1)) || ("DSS".equalsIgnoreCase((String)localObject1)))
        paramString3 = "SHA1WithDSA";
      else if ("RSA".equalsIgnoreCase((String)localObject1))
        paramString3 = "SHA1WithRSA";
      else if ("EC".equalsIgnoreCase((String)localObject1))
        paramString3 = "SHA1withECDSA";
      else
        throw new Exception(rb.getString("Cannot derive signature algorithm"));
    }
    Object localObject1 = this.keyStore.getCertificate(paramString1);
    if (localObject1 == null)
    {
      localObject2 = new MessageFormat(rb.getString("alias has no public key"));
      localObject3 = { paramString1 };
      throw new Exception(((MessageFormat)localObject2).format(localObject3));
    }
    if (!(localObject1 instanceof X509Certificate))
    {
      localObject2 = new MessageFormat(rb.getString("alias has no X.509 certificate"));
      localObject3 = { paramString1 };
      throw new Exception(((MessageFormat)localObject2).format(localObject3));
    }
    Object localObject2 = ((java.security.cert.Certificate)localObject1).getEncoded();
    Object localObject3 = new X509CertImpl(localObject2);
    X509CertInfo localX509CertInfo = (X509CertInfo)((X509CertImpl)localObject3).get("x509.info");
    Date localDate1 = getStartDate(this.startDate);
    Date localDate2 = new Date();
    localDate2.setTime(localDate1.getTime() + this.validity * 1000L * 24L * 60L * 60L);
    CertificateValidity localCertificateValidity = new CertificateValidity(localDate1, localDate2);
    localX509CertInfo.set("validity", localCertificateValidity);
    localX509CertInfo.set("serialNumber", new CertificateSerialNumber((int)(localDate1.getTime() / 1000L)));
    if (paramString2 == null)
    {
      localX500Name = (X500Name)localX509CertInfo.get("subject.dname");
    }
    else
    {
      localX500Name = new X500Name(paramString2);
      localX509CertInfo.set("subject.dname", localX500Name);
    }
    localX509CertInfo.set("issuer.dname", localX500Name);
    X509CertImpl localX509CertImpl = new X509CertImpl(localX509CertInfo);
    localX509CertImpl.sign(localPrivateKey, paramString3);
    AlgorithmId localAlgorithmId = (AlgorithmId)localX509CertImpl.get("x509.algorithm");
    localX509CertInfo.set("algorithmID.algorithm", localAlgorithmId);
    localX509CertInfo.set("version", new CertificateVersion(2));
    localX509CertImpl = new X509CertImpl(localX509CertInfo);
    localX509CertImpl.sign(localPrivateKey, paramString3);
    this.keyStore.setKeyEntry(paramString1, localPrivateKey, (this.keyPass != null) ? this.keyPass : this.storePass, new java.security.cert.Certificate[] { localX509CertImpl });
    if (this.verbose)
    {
      System.err.println(rb.getString("New certificate (self-signed):"));
      System.err.print(localX509CertImpl.toString());
      System.err.println();
    }
  }

  private boolean installReply(String paramString, InputStream paramInputStream)
    throws Exception
  {
    java.security.cert.Certificate[] arrayOfCertificate;
    if (paramString == null)
      paramString = "mykey";
    Object[] arrayOfObject = recoverKey(paramString, this.storePass, this.keyPass);
    PrivateKey localPrivateKey = (PrivateKey)arrayOfObject[0];
    if (this.keyPass == null)
      this.keyPass = ((char[])(char[])arrayOfObject[1]);
    java.security.cert.Certificate localCertificate = this.keyStore.getCertificate(paramString);
    if (localCertificate == null)
    {
      localObject1 = new MessageFormat(rb.getString("alias has no public key (certificate)"));
      localObject2 = { paramString };
      throw new Exception(((MessageFormat)localObject1).format(localObject2));
    }
    Object localObject1 = this.cf.generateCertificates(paramInputStream);
    if (((Collection)localObject1).isEmpty())
      throw new Exception(rb.getString("Reply has no certificates"));
    Object localObject2 = (java.security.cert.Certificate[])(java.security.cert.Certificate[])((Collection)localObject1).toArray(new java.security.cert.Certificate[((Collection)localObject1).size()]);
    if (localObject2.length == 1)
      arrayOfCertificate = establishCertChain(localCertificate, localObject2[0]);
    else
      arrayOfCertificate = validateReply(paramString, localCertificate, localObject2);
    if (arrayOfCertificate != null)
    {
      this.keyStore.setKeyEntry(paramString, localPrivateKey, (this.keyPass != null) ? this.keyPass : this.storePass, arrayOfCertificate);
      return true;
    }
    return false;
  }

  private boolean addTrustedCert(String paramString, InputStream paramInputStream)
    throws Exception
  {
    Object localObject2;
    Object[] arrayOfObject2;
    if (paramString == null)
      throw new Exception(rb.getString("Must specify alias"));
    if (this.keyStore.containsAlias(paramString))
    {
      localObject1 = new MessageFormat(rb.getString("Certificate not imported, alias <alias> already exists"));
      Object[] arrayOfObject1 = { paramString };
      throw new Exception(((MessageFormat)localObject1).format(arrayOfObject1));
    }
    Object localObject1 = null;
    try
    {
      localObject1 = (X509Certificate)this.cf.generateCertificate(paramInputStream);
    }
    catch (ClassCastException localClassCastException)
    {
      throw new Exception(rb.getString("Input not an X.509 certificate"));
    }
    catch (CertificateException localCertificateException)
    {
      throw new Exception(rb.getString("Input not an X.509 certificate"));
    }
    int i = 0;
    if (isSelfSigned((X509Certificate)localObject1))
    {
      ((X509Certificate)localObject1).verify(((X509Certificate)localObject1).getPublicKey());
      i = 1;
    }
    if (this.noprompt)
    {
      this.keyStore.setCertificateEntry(paramString, (java.security.cert.Certificate)localObject1);
      return true;
    }
    String str1 = null;
    String str2 = this.keyStore.getCertificateAlias((java.security.cert.Certificate)localObject1);
    if (str2 != null)
    {
      localObject2 = new MessageFormat(rb.getString("Certificate already exists in keystore under alias <trustalias>"));
      arrayOfObject2 = { str2 };
      System.err.println(((MessageFormat)localObject2).format(arrayOfObject2));
      str1 = getYesNoReply(rb.getString("Do you still want to add it? [no]:  "));
    }
    else if (i != 0)
    {
      if ((this.trustcacerts) && (this.caks != null))
        if ((str2 = this.caks.getCertificateAlias((java.security.cert.Certificate)localObject1)) != null)
        {
          localObject2 = new MessageFormat(rb.getString("Certificate already exists in system-wide CA keystore under alias <trustalias>"));
          arrayOfObject2 = { str2 };
          System.err.println(((MessageFormat)localObject2).format(arrayOfObject2));
          str1 = getYesNoReply(rb.getString("Do you still want to add it to your own keystore? [no]:  "));
        }
      if (str2 == null)
      {
        printX509Cert((X509Certificate)localObject1, System.out);
        str1 = getYesNoReply(rb.getString("Trust this certificate? [no]:  "));
      }
    }
    if (str1 != null)
    {
      if ("YES".equals(str1))
      {
        this.keyStore.setCertificateEntry(paramString, (java.security.cert.Certificate)localObject1);
        return true;
      }
      return false;
    }
    try
    {
      localObject2 = establishCertChain(null, (java.security.cert.Certificate)localObject1);
      if (localObject2 != null)
      {
        this.keyStore.setCertificateEntry(paramString, (java.security.cert.Certificate)localObject1);
        return true;
      }
    }
    catch (Exception localException)
    {
      printX509Cert((X509Certificate)localObject1, System.out);
      str1 = getYesNoReply(rb.getString("Trust this certificate? [no]:  "));
      if ("YES".equals(str1))
      {
        this.keyStore.setCertificateEntry(paramString, (java.security.cert.Certificate)localObject1);
        return true;
      }
      return false;
    }
    return false;
  }

  private char[] getNewPasswd(String paramString, char[] paramArrayOfChar)
    throws Exception
  {
    char[] arrayOfChar1 = null;
    char[] arrayOfChar2 = null;
    for (int i = 0; i < 3; ++i)
    {
      MessageFormat localMessageFormat = new MessageFormat(rb.getString("New prompt: "));
      Object[] arrayOfObject1 = { paramString };
      System.err.print(localMessageFormat.format(arrayOfObject1));
      arrayOfChar1 = Password.readPassword(System.in);
      this.passwords.add(arrayOfChar1);
      if ((arrayOfChar1 == null) || (arrayOfChar1.length < 6))
      {
        System.err.println(rb.getString("Password is too short - must be at least 6 characters"));
      }
      else if (Arrays.equals(arrayOfChar1, paramArrayOfChar))
      {
        System.err.println(rb.getString("Passwords must differ"));
      }
      else
      {
        localMessageFormat = new MessageFormat(rb.getString("Re-enter new prompt: "));
        Object[] arrayOfObject2 = { paramString };
        System.err.print(localMessageFormat.format(arrayOfObject2));
        arrayOfChar2 = Password.readPassword(System.in);
        this.passwords.add(arrayOfChar2);
        if (!(Arrays.equals(arrayOfChar1, arrayOfChar2)))
        {
          System.err.println(rb.getString("They don't match. Try again"));
        }
        else
        {
          Arrays.fill(arrayOfChar2, ' ');
          return arrayOfChar1;
        }
      }
      if (arrayOfChar1 != null)
      {
        Arrays.fill(arrayOfChar1, ' ');
        arrayOfChar1 = null;
      }
      if (arrayOfChar2 != null)
      {
        Arrays.fill(arrayOfChar2, ' ');
        arrayOfChar2 = null;
      }
    }
    throw new Exception(rb.getString("Too many failures - try later"));
  }

  private String getAlias(String paramString)
    throws Exception
  {
    if (paramString != null)
    {
      MessageFormat localMessageFormat = new MessageFormat(rb.getString("Enter prompt alias name:  "));
      Object[] arrayOfObject = { paramString };
      System.err.print(localMessageFormat.format(arrayOfObject));
    }
    else
    {
      System.err.print(rb.getString("Enter alias name:  "));
    }
    return new BufferedReader(new InputStreamReader(System.in)).readLine();
  }

  private String inputStringFromStdin(String paramString)
    throws Exception
  {
    System.err.print(paramString);
    return new BufferedReader(new InputStreamReader(System.in)).readLine();
  }

  private char[] getKeyPasswd(String paramString1, String paramString2, char[] paramArrayOfChar)
    throws Exception
  {
    int i = 0;
    char[] arrayOfChar = null;
    do
    {
      MessageFormat localMessageFormat;
      Object[] arrayOfObject1;
      if (paramArrayOfChar != null)
      {
        localMessageFormat = new MessageFormat(rb.getString("Enter key password for <alias>"));
        arrayOfObject1 = { paramString1 };
        System.err.println(localMessageFormat.format(arrayOfObject1));
        localMessageFormat = new MessageFormat(rb.getString("\t(RETURN if same as for <otherAlias>)"));
        Object[] arrayOfObject2 = { paramString2 };
        System.err.print(localMessageFormat.format(arrayOfObject2));
      }
      else
      {
        localMessageFormat = new MessageFormat(rb.getString("Enter key password for <alias>"));
        arrayOfObject1 = { paramString1 };
        System.err.print(localMessageFormat.format(arrayOfObject1));
      }
      System.err.flush();
      arrayOfChar = Password.readPassword(System.in);
      this.passwords.add(arrayOfChar);
      if (arrayOfChar == null)
        arrayOfChar = paramArrayOfChar;
      ++i;
    }
    while ((arrayOfChar == null) && (i < 3));
    if (arrayOfChar == null)
      throw new Exception(rb.getString("Too many failures - try later"));
    return arrayOfChar;
  }

  private void printX509Cert(X509Certificate paramX509Certificate, PrintStream paramPrintStream)
    throws Exception
  {
    MessageFormat localMessageFormat = new MessageFormat(rb.getString("*PATTERN* printX509Cert"));
    Object[] arrayOfObject = { paramX509Certificate.getSubjectDN().toString(), paramX509Certificate.getIssuerDN().toString(), paramX509Certificate.getSerialNumber().toString(16), paramX509Certificate.getNotBefore().toString(), paramX509Certificate.getNotAfter().toString(), getCertFingerPrint("MD5", paramX509Certificate), getCertFingerPrint("SHA1", paramX509Certificate), paramX509Certificate.getSigAlgName(), Integer.valueOf(paramX509Certificate.getVersion()) };
    paramPrintStream.println(localMessageFormat.format(arrayOfObject));
    int i = 0;
    if (paramX509Certificate instanceof X509CertImpl)
    {
      Iterator localIterator;
      String str;
      X509CertImpl localX509CertImpl = (X509CertImpl)paramX509Certificate;
      if (paramX509Certificate.getCriticalExtensionOIDs() != null)
      {
        localIterator = paramX509Certificate.getCriticalExtensionOIDs().iterator();
        while (localIterator.hasNext())
        {
          str = (String)localIterator.next();
          if (i == 0)
          {
            paramPrintStream.println();
            paramPrintStream.println(rb.getString("Extensions: "));
            paramPrintStream.println();
          }
          paramPrintStream.println("#" + (++i) + ": " + localX509CertImpl.getExtension(new ObjectIdentifier(str)));
        }
      }
      if (paramX509Certificate.getNonCriticalExtensionOIDs() != null)
      {
        localIterator = paramX509Certificate.getNonCriticalExtensionOIDs().iterator();
        while (localIterator.hasNext())
        {
          str = (String)localIterator.next();
          if (i == 0)
          {
            paramPrintStream.println();
            paramPrintStream.println(rb.getString("Extensions: "));
            paramPrintStream.println();
          }
          Extension localExtension = localX509CertImpl.getExtension(new ObjectIdentifier(str));
          if (localExtension != null)
            paramPrintStream.println("#" + (++i) + ": " + localExtension);
          else
            paramPrintStream.println("#" + (++i) + ": " + localX509CertImpl.getUnparseableExtension(new ObjectIdentifier(str)));
        }
      }
    }
  }

  private boolean isSelfSigned(X509Certificate paramX509Certificate)
  {
    return paramX509Certificate.getSubjectDN().equals(paramX509Certificate.getIssuerDN());
  }

  private boolean isTrusted(java.security.cert.Certificate paramCertificate)
    throws Exception
  {
    if (this.keyStore.getCertificateAlias(paramCertificate) != null)
      return true;
    return ((this.trustcacerts) && (this.caks != null) && (this.caks.getCertificateAlias(paramCertificate) != null));
  }

  private X500Name getX500Name()
    throws IOException
  {
    X500Name localX500Name;
    BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(System.in));
    String str1 = "Unknown";
    String str2 = "Unknown";
    String str3 = "Unknown";
    String str4 = "Unknown";
    String str5 = "Unknown";
    String str6 = "Unknown";
    String str7 = null;
    int i = 20;
    do
    {
      if (i-- < 0)
        throw new RuntimeException(rb.getString("Too may retries, program terminated"));
      str1 = inputString(localBufferedReader, rb.getString("What is your first and last name?"), str1);
      str2 = inputString(localBufferedReader, rb.getString("What is the name of your organizational unit?"), str2);
      str3 = inputString(localBufferedReader, rb.getString("What is the name of your organization?"), str3);
      str4 = inputString(localBufferedReader, rb.getString("What is the name of your City or Locality?"), str4);
      str5 = inputString(localBufferedReader, rb.getString("What is the name of your State or Province?"), str5);
      str6 = inputString(localBufferedReader, rb.getString("What is the two-letter country code for this unit?"), str6);
      localX500Name = new X500Name(str1, str2, str3, str4, str5, str6);
      MessageFormat localMessageFormat = new MessageFormat(rb.getString("Is <name> correct?"));
      Object[] arrayOfObject = { localX500Name };
      str7 = inputString(localBufferedReader, localMessageFormat.format(arrayOfObject), rb.getString("no"));
    }
    while ((collator.compare(str7, rb.getString("yes")) != 0) && (collator.compare(str7, rb.getString("y")) != 0));
    System.err.println();
    return localX500Name;
  }

  private String inputString(BufferedReader paramBufferedReader, String paramString1, String paramString2)
    throws IOException
  {
    System.err.println(paramString1);
    MessageFormat localMessageFormat = new MessageFormat(rb.getString("  [defaultValue]:  "));
    Object[] arrayOfObject = { paramString2 };
    System.err.print(localMessageFormat.format(arrayOfObject));
    System.err.flush();
    String str = paramBufferedReader.readLine();
    if ((str == null) || (collator.compare(str, "") == 0))
      str = paramString2;
    return str;
  }

  private void dumpCert(java.security.cert.Certificate paramCertificate, PrintStream paramPrintStream)
    throws IOException, CertificateException
  {
    if (this.rfc)
    {
      BASE64Encoder localBASE64Encoder = new BASE64Encoder();
      paramPrintStream.println("-----BEGIN CERTIFICATE-----");
      localBASE64Encoder.encodeBuffer(paramCertificate.getEncoded(), paramPrintStream);
      paramPrintStream.println("-----END CERTIFICATE-----");
    }
    else
    {
      paramPrintStream.write(paramCertificate.getEncoded());
    }
  }

  private void byte2hex(byte paramByte, StringBuffer paramStringBuffer)
  {
    char[] arrayOfChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    int i = (paramByte & 0xF0) >> 4;
    int j = paramByte & 0xF;
    paramStringBuffer.append(arrayOfChar[i]);
    paramStringBuffer.append(arrayOfChar[j]);
  }

  private String toHexString(byte[] paramArrayOfByte)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    int i = paramArrayOfByte.length;
    for (int j = 0; j < i; ++j)
    {
      byte2hex(paramArrayOfByte[j], localStringBuffer);
      if (j < i - 1)
        localStringBuffer.append(":");
    }
    return localStringBuffer.toString();
  }

  private Object[] recoverKey(String paramString, char[] paramArrayOfChar1, char[] paramArrayOfChar2)
    throws Exception
  {
    MessageFormat localMessageFormat;
    Object[] arrayOfObject;
    Key localKey = null;
    if (!(this.keyStore.containsAlias(paramString)))
    {
      localMessageFormat = new MessageFormat(rb.getString("Alias <alias> does not exist"));
      arrayOfObject = { paramString };
      throw new Exception(localMessageFormat.format(arrayOfObject));
    }
    if ((!(this.keyStore.entryInstanceOf(paramString, KeyStore.PrivateKeyEntry.class))) && (!(this.keyStore.entryInstanceOf(paramString, KeyStore.SecretKeyEntry.class))))
    {
      localMessageFormat = new MessageFormat(rb.getString("Alias <alias> has no key"));
      arrayOfObject = { paramString };
      throw new Exception(localMessageFormat.format(arrayOfObject));
    }
    if (paramArrayOfChar2 == null)
      try
      {
        localKey = this.keyStore.getKey(paramString, paramArrayOfChar1);
        paramArrayOfChar2 = paramArrayOfChar1;
        this.passwords.add(paramArrayOfChar2);
      }
      catch (UnrecoverableKeyException localUnrecoverableKeyException)
      {
        if (!(this.token))
        {
          paramArrayOfChar2 = getKeyPasswd(paramString, null, null);
          localKey = this.keyStore.getKey(paramString, paramArrayOfChar2);
        }
        else
        {
          throw localUnrecoverableKeyException;
        }
      }
    else
      localKey = this.keyStore.getKey(paramString, paramArrayOfChar2);
    return { localKey, paramArrayOfChar2 };
  }

  private Object[] recoverEntry(KeyStore paramKeyStore, String paramString, char[] paramArrayOfChar1, char[] paramArrayOfChar2)
    throws Exception
  {
    Object localObject2;
    if (!(paramKeyStore.containsAlias(paramString)))
    {
      localObject1 = new MessageFormat(rb.getString("Alias <alias> does not exist"));
      localObject2 = { paramString };
      throw new Exception(((MessageFormat)localObject1).format(localObject2));
    }
    Object localObject1 = null;
    try
    {
      localObject2 = paramKeyStore.getEntry(paramString, (KeyStore.ProtectionParameter)localObject1);
      paramArrayOfChar2 = null;
    }
    catch (UnrecoverableEntryException localUnrecoverableEntryException1)
    {
      if (("PKCS11".equalsIgnoreCase(paramKeyStore.getType())) || (KeyStoreUtil.isWindowsKeyStore(paramKeyStore.getType())))
        throw localUnrecoverableEntryException1;
    }
    if (paramArrayOfChar2 != null)
    {
      localObject1 = new KeyStore.PasswordProtection(paramArrayOfChar2);
      localObject2 = paramKeyStore.getEntry(paramString, (KeyStore.ProtectionParameter)localObject1);
    }
    else
    {
      try
      {
        localObject1 = new KeyStore.PasswordProtection(paramArrayOfChar1);
        localObject2 = paramKeyStore.getEntry(paramString, (KeyStore.ProtectionParameter)localObject1);
        paramArrayOfChar2 = paramArrayOfChar1;
      }
      catch (UnrecoverableEntryException localUnrecoverableEntryException2)
      {
        if ("PKCS12".equalsIgnoreCase(paramKeyStore.getType()))
          throw localUnrecoverableEntryException2;
        paramArrayOfChar2 = getKeyPasswd(paramString, null, null);
        localObject1 = new KeyStore.PasswordProtection(paramArrayOfChar2);
        localObject2 = paramKeyStore.getEntry(paramString, (KeyStore.ProtectionParameter)localObject1);
      }
    }
    return ((Object)(Object){ localObject2, paramArrayOfChar2 });
  }

  private String getCertFingerPrint(String paramString, java.security.cert.Certificate paramCertificate)
    throws Exception
  {
    byte[] arrayOfByte1 = paramCertificate.getEncoded();
    MessageDigest localMessageDigest = MessageDigest.getInstance(paramString);
    byte[] arrayOfByte2 = localMessageDigest.digest(arrayOfByte1);
    return toHexString(arrayOfByte2);
  }

  private void printWarning()
  {
    System.err.println();
    System.err.println(rb.getString("*****************  WARNING WARNING WARNING  *****************"));
    System.err.println(rb.getString("* The integrity of the information stored in your keystore  *"));
    System.err.println(rb.getString("* has NOT been verified!  In order to verify its integrity, *"));
    System.err.println(rb.getString("* you must provide your keystore password.                  *"));
    System.err.println(rb.getString("*****************  WARNING WARNING WARNING  *****************"));
    System.err.println();
  }

  private java.security.cert.Certificate[] validateReply(String paramString, java.security.cert.Certificate paramCertificate, java.security.cert.Certificate[] paramArrayOfCertificate)
    throws Exception
  {
    PublicKey localPublicKey = paramCertificate.getPublicKey();
    for (int i = 0; i < paramArrayOfCertificate.length; ++i)
      if (localPublicKey.equals(paramArrayOfCertificate[i].getPublicKey()))
        break;
    if (i == paramArrayOfCertificate.length)
    {
      localObject1 = new MessageFormat(rb.getString("Certificate reply does not contain public key for <alias>"));
      localObject2 = { paramString };
      throw new Exception(((MessageFormat)localObject1).format(localObject2));
    }
    Object localObject1 = paramArrayOfCertificate[0];
    paramArrayOfCertificate[0] = paramArrayOfCertificate[i];
    paramArrayOfCertificate[i] = localObject1;
    Object localObject2 = ((X509Certificate)paramArrayOfCertificate[0]).getIssuerDN();
    for (i = 1; i < paramArrayOfCertificate.length - 1; ++i)
    {
      for (int j = i; j < paramArrayOfCertificate.length; ++j)
      {
        Principal localPrincipal = ((X509Certificate)paramArrayOfCertificate[j]).getSubjectDN();
        if (localPrincipal.equals(localObject2))
        {
          localObject1 = paramArrayOfCertificate[i];
          paramArrayOfCertificate[i] = paramArrayOfCertificate[j];
          paramArrayOfCertificate[j] = localObject1;
          localObject2 = ((X509Certificate)paramArrayOfCertificate[i]).getIssuerDN();
          break;
        }
      }
      if (j == paramArrayOfCertificate.length)
        throw new Exception(rb.getString("Incomplete certificate chain in reply"));
    }
    for (i = 0; i < paramArrayOfCertificate.length - 1; ++i)
    {
      localObject3 = paramArrayOfCertificate[(i + 1)].getPublicKey();
      try
      {
        paramArrayOfCertificate[i].verify((PublicKey)localObject3);
      }
      catch (Exception localException1)
      {
        throw new Exception(rb.getString("Certificate chain in reply does not verify: ") + localException1.getMessage());
      }
    }
    if (this.noprompt)
      return paramArrayOfCertificate;
    Object localObject3 = paramArrayOfCertificate[(paramArrayOfCertificate.length - 1)];
    if (!(isTrusted((java.security.cert.Certificate)localObject3)))
    {
      Object localObject4;
      int k = 0;
      java.security.cert.Certificate localCertificate = null;
      if ((this.trustcacerts) && (this.caks != null))
      {
        localObject4 = this.caks.aliases();
        if (((Enumeration)localObject4).hasMoreElements())
        {
          label371: String str = (String)((Enumeration)localObject4).nextElement();
          localCertificate = this.caks.getCertificate(str);
          if (localCertificate != null);
          try
          {
            ((java.security.cert.Certificate)localObject3).verify(localCertificate.getPublicKey());
            k = 1;
          }
          catch (Exception localException2)
          {
            break label371:
          }
        }
      }
      if (k == 0)
      {
        System.err.println();
        System.err.println(rb.getString("Top-level certificate in reply:\n"));
        printX509Cert((X509Certificate)localObject3, System.out);
        System.err.println();
        System.err.print(rb.getString("... is not trusted. "));
        localObject4 = getYesNoReply(rb.getString("Install reply anyway? [no]:  "));
        if ("NO".equals(localObject4))
          return null;
      }
      else if (!(isSelfSigned((X509Certificate)localObject3)))
      {
        localObject4 = new java.security.cert.Certificate[paramArrayOfCertificate.length + 1];
        System.arraycopy(paramArrayOfCertificate, 0, localObject4, 0, paramArrayOfCertificate.length);
        localObject4[(localObject4.length - 1)] = localCertificate;
        paramArrayOfCertificate = (java.security.cert.Certificate)localObject4;
      }
    }
    return ((java.security.cert.Certificate)(java.security.cert.Certificate)(java.security.cert.Certificate)(java.security.cert.Certificate)paramArrayOfCertificate);
  }

  private java.security.cert.Certificate[] establishCertChain(java.security.cert.Certificate paramCertificate1, java.security.cert.Certificate paramCertificate2)
    throws Exception
  {
    if (paramCertificate1 != null)
    {
      localObject1 = paramCertificate1.getPublicKey();
      localObject2 = paramCertificate2.getPublicKey();
      if (!(localObject1.equals(localObject2)))
        throw new Exception(rb.getString("Public keys in reply and keystore don't match"));
      if (paramCertificate2.equals(paramCertificate1))
        throw new Exception(rb.getString("Certificate reply and certificate in keystore are identical"));
    }
    Object localObject1 = null;
    if (this.keyStore.size() > 0)
    {
      localObject1 = new Hashtable(11);
      keystorecerts2Hashtable(this.keyStore, (Hashtable)localObject1);
    }
    if ((this.trustcacerts) && (this.caks != null) && (this.caks.size() > 0))
    {
      if (localObject1 == null)
        localObject1 = new Hashtable(11);
      keystorecerts2Hashtable(this.caks, (Hashtable)localObject1);
    }
    Object localObject2 = new Vector(2);
    if (buildChain((X509Certificate)paramCertificate2, (Vector)localObject2, (Hashtable)localObject1))
    {
      java.security.cert.Certificate[] arrayOfCertificate = new java.security.cert.Certificate[((Vector)localObject2).size()];
      int i = 0;
      for (int j = ((Vector)localObject2).size() - 1; j >= 0; --j)
      {
        arrayOfCertificate[i] = ((java.security.cert.Certificate)((Vector)localObject2).elementAt(j));
        ++i;
      }
      return arrayOfCertificate;
    }
    throw new Exception(rb.getString("Failed to establish chain from reply"));
  }

  private boolean buildChain(X509Certificate paramX509Certificate, Vector paramVector, Hashtable paramHashtable)
  {
    Principal localPrincipal1 = paramX509Certificate.getSubjectDN();
    Principal localPrincipal2 = paramX509Certificate.getIssuerDN();
    if (localPrincipal1.equals(localPrincipal2))
    {
      paramVector.addElement(paramX509Certificate);
      return true;
    }
    Vector localVector = (Vector)paramHashtable.get(localPrincipal2);
    if (localVector == null)
      return false;
    Enumeration localEnumeration = localVector.elements();
    while (localEnumeration.hasMoreElements())
    {
      label54: X509Certificate localX509Certificate = (X509Certificate)localEnumeration.nextElement();
      PublicKey localPublicKey = localX509Certificate.getPublicKey();
      try
      {
        paramX509Certificate.verify(localPublicKey);
      }
      catch (Exception localException)
      {
        break label54:
      }
      if (buildChain(localX509Certificate, paramVector, paramHashtable))
      {
        paramVector.addElement(paramX509Certificate);
        return true;
      }
    }
    return false;
  }

  private String getYesNoReply(String paramString)
    throws IOException
  {
    String str = null;
    int i = 20;
    do
    {
      if (i-- < 0)
        throw new RuntimeException(rb.getString("Too may retries, program terminated"));
      System.err.print(paramString);
      System.err.flush();
      str = new BufferedReader(new InputStreamReader(System.in)).readLine();
      if ((collator.compare(str, "") == 0) || (collator.compare(str, rb.getString("n")) == 0) || (collator.compare(str, rb.getString("no")) == 0))
      {
        str = "NO";
      }
      else if ((collator.compare(str, rb.getString("y")) == 0) || (collator.compare(str, rb.getString("yes")) == 0))
      {
        str = "YES";
      }
      else
      {
        System.err.println(rb.getString("Wrong answer, try again"));
        str = null;
      }
    }
    while (str == null);
    return str;
  }

  private KeyStore getCacertsKeyStore()
    throws Exception
  {
    String str = File.separator;
    File localFile = new File(System.getProperty("java.home") + str + "lib" + str + "security" + str + "cacerts");
    if (!(localFile.exists()))
      return null;
    FileInputStream localFileInputStream = new FileInputStream(localFile);
    KeyStore localKeyStore = KeyStore.getInstance("jks");
    localKeyStore.load(localFileInputStream, null);
    localFileInputStream.close();
    return localKeyStore;
  }

  private void keystorecerts2Hashtable(KeyStore paramKeyStore, Hashtable paramHashtable)
    throws Exception
  {
    Enumeration localEnumeration = paramKeyStore.aliases();
    while (localEnumeration.hasMoreElements())
    {
      String str = (String)localEnumeration.nextElement();
      java.security.cert.Certificate localCertificate = paramKeyStore.getCertificate(str);
      if (localCertificate != null)
      {
        Principal localPrincipal = ((X509Certificate)localCertificate).getSubjectDN();
        Vector localVector = (Vector)paramHashtable.get(localPrincipal);
        if (localVector == null)
        {
          localVector = new Vector();
          localVector.addElement(localCertificate);
        }
        else if (!(localVector.contains(localCertificate)))
        {
          localVector.addElement(localCertificate);
        }
        paramHashtable.put(localPrincipal, localVector);
      }
    }
  }

  private static Date getStartDate(String paramString)
    throws IOException
  {
    GregorianCalendar localGregorianCalendar = new GregorianCalendar();
    if (paramString != null)
    {
      IOException localIOException = new IOException("Illegal startdate value");
      int i = paramString.length();
      if (i == 0)
        throw localIOException;
      if ((paramString.charAt(0) == '-') || (paramString.charAt(0) == '+'))
      {
        for (int j = 0; j < i; j = l + 1)
        {
          int k = 0;
          switch (paramString.charAt(j))
          {
          case '+':
            k = 1;
            break;
          case '-':
            k = -1;
            break;
          default:
            throw localIOException;
          }
          for (int l = j + 1; l < i; ++l)
          {
            i1 = paramString.charAt(l);
            if (i1 < 48)
              break;
            if (i1 > 57)
              break;
          }
          if (l == j + 1)
            throw localIOException;
          int i1 = Integer.parseInt(paramString.substring(j + 1, l));
          if (l >= i)
            throw localIOException;
          int i2 = 0;
          switch (paramString.charAt(l))
          {
          case 'y':
            i2 = 1;
            break;
          case 'm':
            i2 = 2;
            break;
          case 'd':
            i2 = 5;
            break;
          case 'H':
            i2 = 10;
            break;
          case 'M':
            i2 = 12;
            break;
          case 'S':
            i2 = 13;
            break;
          default:
            throw localIOException;
          }
          localGregorianCalendar.add(i2, k * i1);
        }
      }
      else
      {
        String str1 = null;
        String str2 = null;
        if (i == 19)
        {
          str1 = paramString.substring(0, 10);
          str2 = paramString.substring(11);
          if (paramString.charAt(10) == ' ')
            break label392;
          throw localIOException;
        }
        if (i == 10)
          str1 = paramString;
        else if (i == 8)
          str2 = paramString;
        else
          throw localIOException;
        if (str1 != null)
          if (str1.matches("\\d\\d\\d\\d\\/\\d\\d\\/\\d\\d"))
            label392: localGregorianCalendar.set(Integer.valueOf(str1.substring(0, 4)).intValue(), Integer.valueOf(str1.substring(5, 7)).intValue() - 1, Integer.valueOf(str1.substring(8, 10)).intValue());
          else
            throw localIOException;
        if (str2 != null)
          if (str2.matches("\\d\\d:\\d\\d:\\d\\d"))
          {
            localGregorianCalendar.set(11, Integer.valueOf(str2.substring(0, 2)).intValue());
            localGregorianCalendar.set(12, Integer.valueOf(str2.substring(0, 2)).intValue());
            localGregorianCalendar.set(13, Integer.valueOf(str2.substring(0, 2)).intValue());
            localGregorianCalendar.set(14, 0);
          }
          else
          {
            throw localIOException;
          }
      }
    }
    return localGregorianCalendar.getTime();
  }

  private void usage()
  {
    System.err.println(rb.getString("keytool usage:\n"));
    System.err.println(rb.getString("-certreq     [-v] [-protected]"));
    System.err.println(rb.getString("\t     [-alias <alias>] [-sigalg <sigalg>]"));
    System.err.println(rb.getString("\t     [-file <csr_file>] [-keypass <keypass>]"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-changealias [-v] [-protected] -alias <alias> -destalias <destalias>"));
    System.err.println(rb.getString("\t     [-keypass <keypass>]"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-delete      [-v] [-protected] -alias <alias>"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-exportcert  [-v] [-rfc] [-protected]"));
    System.err.println(rb.getString("\t     [-alias <alias>] [-file <cert_file>]"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-genkeypair  [-v] [-protected]"));
    System.err.println(rb.getString("\t     [-alias <alias>]"));
    System.err.println(rb.getString("\t     [-keyalg <keyalg>] [-keysize <keysize>]"));
    System.err.println(rb.getString("\t     [-sigalg <sigalg>] [-dname <dname>]"));
    System.err.println(rb.getString("\t     [-validity <valDays>] [-keypass <keypass>]"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-genseckey   [-v] [-protected]"));
    System.err.println(rb.getString("\t     [-alias <alias>] [-keypass <keypass>]"));
    System.err.println(rb.getString("\t     [-keyalg <keyalg>] [-keysize <keysize>]"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-help"));
    System.err.println();
    System.err.println(rb.getString("-importcert  [-v] [-noprompt] [-trustcacerts] [-protected]"));
    System.err.println(rb.getString("\t     [-alias <alias>]"));
    System.err.println(rb.getString("\t     [-file <cert_file>] [-keypass <keypass>]"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-importkeystore [-v] "));
    System.err.println(rb.getString("\t     [-srckeystore <srckeystore>] [-destkeystore <destkeystore>]"));
    System.err.println(rb.getString("\t     [-srcstoretype <srcstoretype>] [-deststoretype <deststoretype>]"));
    System.err.println(rb.getString("\t     [-srcstorepass <srcstorepass>] [-deststorepass <deststorepass>]"));
    System.err.println(rb.getString("\t     [-srcprotected] [-destprotected]"));
    System.err.println(rb.getString("\t     [-srcprovidername <srcprovidername>]\n\t     [-destprovidername <destprovidername>]"));
    System.err.println(rb.getString("\t     [-srcalias <srcalias> [-destalias <destalias>]"));
    System.err.println(rb.getString("\t       [-srckeypass <srckeypass>] [-destkeypass <destkeypass>]]"));
    System.err.println(rb.getString("\t     [-noprompt]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-keypasswd   [-v] [-alias <alias>]"));
    System.err.println(rb.getString("\t     [-keypass <old_keypass>] [-new <new_keypass>]"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-list        [-v | -rfc] [-protected]"));
    System.err.println(rb.getString("\t     [-alias <alias>]"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    System.err.println();
    System.err.println(rb.getString("-printcert   [-v] [-file <cert_file>]"));
    System.err.println();
    System.err.println(rb.getString("-storepasswd [-v] [-new <new_storepass>]"));
    System.err.println(rb.getString("\t     [-keystore <keystore>] [-storepass <storepass>]"));
    System.err.println(rb.getString("\t     [-storetype <storetype>] [-providername <name>]"));
    System.err.println(rb.getString("\t     [-providerclass <provider_class_name> [-providerarg <arg>]] ..."));
    System.err.println(rb.getString("\t     [-providerpath <pathlist>]"));
    if (this.debug)
      throw new RuntimeException("NO ERROR, SORRY");
    System.exit(1);
  }

  private void tinyHelp()
  {
    System.err.println(rb.getString("Try keytool -help"));
    if (this.debug)
      throw new RuntimeException("NO BIG ERROR, SORRY");
    System.exit(1);
  }

  private void errorNeedArgument(String paramString)
  {
    Object[] arrayOfObject = { paramString };
    System.err.println(new MessageFormat(rb.getString("Command option <flag> needs an argument.")).format(arrayOfObject));
    tinyHelp();
  }

  static
  {
    collator.setStrength(0);
  }
}
package sun.security.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.security.AccessController;
import java.security.Identity;
import java.security.IdentityScope;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.PrivilegedAction;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signer;
import java.util.Enumeration;
import java.util.Hashtable;

public class IdentityDatabase extends IdentityScope
  implements Serializable
{
  private static final long serialVersionUID = 4923799573357658384L;
  private static final boolean debug = 0;
  private static final boolean error = 1;
  File sourceFile;
  Hashtable identities;

  IdentityDatabase()
    throws InvalidParameterException
  {
    this("restoring...");
  }

  public IdentityDatabase(File paramFile)
    throws InvalidParameterException
  {
    this(paramFile.getName());
    this.sourceFile = paramFile;
  }

  public IdentityDatabase(String paramString)
    throws InvalidParameterException
  {
    super(paramString);
    this.identities = new Hashtable();
  }

  public static IdentityDatabase fromStream(InputStream paramInputStream)
    throws IOException
  {
    IdentityDatabase localIdentityDatabase = null;
    try
    {
      ObjectInputStream localObjectInputStream = new ObjectInputStream(paramInputStream);
      localIdentityDatabase = (IdentityDatabase)localObjectInputStream.readObject();
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      debug("This should not be happening.", localClassNotFoundException);
      error("The version of the database is obsolete. Cannot initialize.");
    }
    catch (InvalidClassException localInvalidClassException)
    {
      debug("This should not be happening.", localInvalidClassException);
      error("Unable to initialize system identity scope:  InvalidClassException. \nThis is most likely due to a serialization versioning problem: a class used in key management was obsoleted");
    }
    catch (StreamCorruptedException localStreamCorruptedException)
    {
      debug("The serialization stream is corrupted. Unable to load.", localStreamCorruptedException);
      error("Unable to initialize system identity scope. StreamCorruptedException.");
    }
    if (localIdentityDatabase == null)
      localIdentityDatabase = new IdentityDatabase("uninitialized");
    return localIdentityDatabase;
  }

  public static IdentityDatabase fromFile(File paramFile)
    throws IOException
  {
    FileInputStream localFileInputStream = new FileInputStream(paramFile);
    IdentityDatabase localIdentityDatabase = fromStream(localFileInputStream);
    localIdentityDatabase.sourceFile = paramFile;
    return localIdentityDatabase;
  }

  public int size()
  {
    return this.identities.size();
  }

  public Identity getIdentity(String paramString)
  {
    Identity localIdentity = (Identity)this.identities.get(paramString);
    if (localIdentity instanceof Signer)
      localCheck("get.signer");
    return localIdentity;
  }

  public Identity getIdentity(PublicKey paramPublicKey)
  {
    if (paramPublicKey == null)
      return null;
    Enumeration localEnumeration = identities();
    while (localEnumeration.hasMoreElements())
    {
      Identity localIdentity = (Identity)localEnumeration.nextElement();
      PublicKey localPublicKey = localIdentity.getPublicKey();
      if ((localPublicKey != null) && (keyEqual(localPublicKey, paramPublicKey)))
      {
        if (localIdentity instanceof Signer)
          localCheck("get.signer");
        return localIdentity;
      }
    }
    return null;
  }

  private boolean keyEqual(Key paramKey1, Key paramKey2)
  {
    if (paramKey1 == paramKey2)
      return true;
    return MessageDigest.isEqual(paramKey1.getEncoded(), paramKey2.getEncoded());
  }

  public void addIdentity(Identity paramIdentity)
    throws KeyManagementException
  {
    localCheck("add.identity");
    Identity localIdentity1 = getIdentity(paramIdentity.getName());
    Identity localIdentity2 = getIdentity(paramIdentity.getPublicKey());
    String str = null;
    if (localIdentity1 != null)
      str = "name conflict";
    if (localIdentity2 != null)
      str = "key conflict";
    if (str != null)
      throw new KeyManagementException(str);
    this.identities.put(paramIdentity.getName(), paramIdentity);
  }

  public void removeIdentity(Identity paramIdentity)
    throws KeyManagementException
  {
    localCheck("remove.identity");
    String str = paramIdentity.getName();
    if (this.identities.get(str) == null)
      throw new KeyManagementException("there is no identity named " + str + " in " + this);
    this.identities.remove(str);
  }

  public Enumeration identities()
  {
    return this.identities.elements();
  }

  void setSourceFile(File paramFile)
  {
    this.sourceFile = paramFile;
  }

  File getSourceFile()
  {
    return this.sourceFile;
  }

  public void save(OutputStream paramOutputStream)
    throws IOException
  {
    ObjectOutputStream localObjectOutputStream;
    try
    {
      localObjectOutputStream = new ObjectOutputStream(paramOutputStream);
      localObjectOutputStream.writeObject(this);
      localObjectOutputStream.flush();
    }
    catch (InvalidClassException localInvalidClassException)
    {
      debug("This should not be happening.", localInvalidClassException);
      return;
    }
  }

  void save(File paramFile)
    throws IOException
  {
    setSourceFile(paramFile);
    FileOutputStream localFileOutputStream = new FileOutputStream(paramFile);
    save(localFileOutputStream);
  }

  public void save()
    throws IOException
  {
    if (this.sourceFile == null)
      throw new IOException("this database has no source file");
    save(this.sourceFile);
  }

  private static File systemDatabaseFile()
  {
    String str = Security.getProperty("identity.database");
    if (str == null)
      str = System.getProperty("user.home") + File.separatorChar + "identitydb.obj";
    return new File(str);
  }

  private static void initializeSystem()
  {
    File localFile = systemDatabaseFile();
    try
    {
      IdentityDatabase localIdentityDatabase;
      if (localFile.exists())
      {
        debug("loading system database from file: " + localFile);
        localIdentityDatabase = fromFile(localFile);
      }
      else
      {
        localIdentityDatabase = new IdentityDatabase(localFile);
      }
      IdentityScope.setSystemScope(localIdentityDatabase);
      debug("System database initialized: " + localIdentityDatabase);
    }
    catch (IOException localIOException)
    {
      debug("Error initializing identity database: " + localFile, localIOException);
      return;
    }
    catch (InvalidParameterException localInvalidParameterException)
    {
      debug("Error trying to instantiate a system identities db in " + localFile, localInvalidParameterException);
      return;
    }
  }

  public String toString()
  {
    return "sun.security.provider.IdentityDatabase, source file: " + this.sourceFile;
  }

  private static void debug(String paramString)
  {
  }

  private static void debug(String paramString, Throwable paramThrowable)
  {
  }

  private static void error(String paramString)
  {
    System.err.println(paramString);
  }

  void localCheck(String paramString)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      paramString = getClass().getName() + "." + paramString + "." + localFullName();
      localSecurityManager.checkSecurityAccess(paramString);
    }
  }

  String localFullName()
  {
    String str = getName();
    if (getScope() != null)
      str = str + "." + getScope().getName();
    return str;
  }

  private synchronized void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    localCheck("serialize.identity.database");
    paramObjectOutputStream.writeObject(this.identities);
    paramObjectOutputStream.writeObject(this.sourceFile);
  }

  static
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        IdentityDatabase.access$000();
        return null;
      }
    });
  }
}
package sun.jkernel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class BundleCheck
{
  private static final int DIGEST_STREAM_BUFFER_SIZE = 2048;
  private static final String BUNDLE_SUFFIX = ".zip";
  private static volatile Properties properties;
  private byte[] checkBytes;

  private BundleCheck()
  {
  }

  public static void storeProperties(String paramString)
  {
    File localFile;
    try
    {
      localFile = new File(paramString);
      localFile.getParentFile().mkdirs();
      FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
      properties.store(localFileOutputStream, null);
      localFileOutputStream.close();
    }
    catch (Exception localException)
    {
      throw new RuntimeException("BundleCheck: storing properties threw: " + localException);
    }
  }

  private static void loadProperties()
  {
    properties = new Properties();
    try
    {
      BufferedInputStream localBufferedInputStream = new BufferedInputStream(DownloadManager.class.getResourceAsStream(DownloadManager.CHECK_VALUES_FILE));
      if (localBufferedInputStream == null)
        throw new RuntimeException("BundleCheck: unable to locate " + DownloadManager.CHECK_VALUES_FILE + " as resource");
      properties.load(localBufferedInputStream);
      localBufferedInputStream.close();
    }
    catch (Exception localException)
    {
      throw new RuntimeException("BundleCheck: loadProperties threw " + localException);
    }
  }

  private static synchronized Properties getProperties()
  {
    if (properties == null)
      loadProperties();
    return properties;
  }

  public static void resetProperties()
  {
    properties = null;
  }

  public String toString()
  {
    return ByteArrayToFromHexDigits.bytesToHexString(this.checkBytes);
  }

  private void addProperty(String paramString)
  {
    if (properties == null)
      properties = new Properties();
    getProperties().put(paramString, toString());
  }

  private BundleCheck(byte[] paramArrayOfByte)
  {
    this.checkBytes = paramArrayOfByte;
  }

  private BundleCheck(String paramString)
  {
    String str = getProperties().getProperty(paramString);
    if (str == null)
      throw new RuntimeException("BundleCheck: no check property for bundle: " + paramString);
    this.checkBytes = ByteArrayToFromHexDigits.hexStringToBytes(str);
  }

  private static BundleCheck getInstance(String paramString, File paramFile, boolean paramBoolean)
  {
    if (paramFile == null)
      return new BundleCheck(paramString);
    StandaloneMessageDigest localStandaloneMessageDigest = null;
    try
    {
      int i;
      FileInputStream localFileInputStream = new FileInputStream(paramFile);
      localStandaloneMessageDigest = StandaloneMessageDigest.getInstance("SHA-1");
      byte[] arrayOfByte = new byte[2048];
      do
      {
        i = localFileInputStream.read(arrayOfByte);
        if (i > 0)
          localStandaloneMessageDigest.update(arrayOfByte, 0, i);
      }
      while (i != -1);
      localFileInputStream.close();
    }
    catch (Exception localException)
    {
      throw new RuntimeException("BundleCheck.addProperty() caught: " + localException);
    }
    BundleCheck localBundleCheck = new BundleCheck(localStandaloneMessageDigest.digest());
    if (paramBoolean)
      localBundleCheck.addProperty(paramString);
    return localBundleCheck;
  }

  public static BundleCheck getInstance(File paramFile)
  {
    return getInstance(null, paramFile, false);
  }

  static BundleCheck getInstance(String paramString)
  {
    return getInstance(paramString, null, false);
  }

  public static void addProperty(String paramString, File paramFile)
  {
    getInstance(paramString, paramFile, true);
  }

  static void add(String paramString, File paramFile)
  {
    getInstance(paramString, paramFile, true).addProperty(paramString);
  }

  boolean equals(BundleCheck paramBundleCheck)
  {
    if ((this.checkBytes == null) || (paramBundleCheck.checkBytes == null))
      return false;
    if (this.checkBytes.length != paramBundleCheck.checkBytes.length)
      return false;
    for (int i = 0; i < this.checkBytes.length; ++i)
      if (this.checkBytes[i] != paramBundleCheck.checkBytes[i])
      {
        if (DownloadManager.debug)
          System.out.println("BundleCheck.equals mismatch between this: " + toString() + " and param: " + paramBundleCheck.toString());
        return false;
      }
    return true;
  }

  public static void main(String[] paramArrayOfString)
  {
    if (paramArrayOfString.length < 2)
    {
      System.err.println("Usage: java BundleCheck <jre path> <bundle 1 name> ... <bundle N name>");
      return;
    }
    for (int i = 1; i < paramArrayOfString.length; ++i)
      addProperty(paramArrayOfString[i], new File(paramArrayOfString[i] + ".zip"));
    storeProperties(DownloadManager.CHECK_VALUES_DIR);
    try
    {
      i = Runtime.getRuntime().exec("jar uf " + paramArrayOfString[0] + "\\lib\\rt.jar " + DownloadManager.CHECK_VALUES_DIR).waitFor();
      if (i != 0)
      {
        System.err.println("BundleCheck: exec of jar uf gave nonzero status");
        return;
      }
    }
    catch (Exception localException)
    {
      System.err.println("BundleCheck: exec of jar uf threw: " + localException);
      return;
    }
  }
}
package sun.security.provider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;
import sun.security.util.Debug;

abstract class SeedGenerator
{
  private static SeedGenerator instance;
  private static final Debug debug = Debug.getInstance("provider");
  static final String URL_DEV_RANDOM = "file:/dev/random";
  static final String URL_DEV_URANDOM = "file:/dev/urandom";

  public static void generateSeed(byte[] paramArrayOfByte)
  {
    instance.getSeedBytes(paramArrayOfByte);
  }

  void getSeedBytes(byte[] paramArrayOfByte)
  {
    for (int i = 0; i < paramArrayOfByte.length; ++i)
      paramArrayOfByte[i] = getSeedByte();
  }

  abstract byte getSeedByte();

  static byte[] getSystemEntropy()
  {
    MessageDigest localMessageDigest;
    try
    {
      localMessageDigest = MessageDigest.getInstance("SHA");
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new InternalError("internal error: SHA-1 not available.");
    }
    byte b = (byte)(int)System.currentTimeMillis();
    localMessageDigest.update(b);
    AccessController.doPrivileged(new PrivilegedAction(localMessageDigest)
    {
      public Object run()
      {
        try
        {
          localObject = System.getProperties();
          Enumeration localEnumeration = ((Properties)localObject).propertyNames();
          while (localEnumeration.hasMoreElements())
          {
            String str = (String)localEnumeration.nextElement();
            this.val$md.update(str.getBytes());
            this.val$md.update(((Properties)localObject).getProperty(str).getBytes());
          }
          this.val$md.update(InetAddress.getLocalHost().toString().getBytes());
          File localFile = new File(((Properties)localObject).getProperty("java.io.tmpdir"));
          String[] arrayOfString = localFile.list();
          for (int i = 0; i < arrayOfString.length; ++i)
            this.val$md.update(arrayOfString[i].getBytes());
        }
        catch (Exception localException)
        {
          this.val$md.update((byte)localException.hashCode());
        }
        Runtime localRuntime = Runtime.getRuntime();
        Object localObject = SeedGenerator.access$000(localRuntime.totalMemory());
        this.val$md.update(localObject, 0, localObject.length);
        localObject = SeedGenerator.access$000(localRuntime.freeMemory());
        this.val$md.update(localObject, 0, localObject.length);
        return null;
      }
    });
    return localMessageDigest.digest();
  }

  private static byte[] longToByteArray(long paramLong)
  {
    byte[] arrayOfByte = new byte[8];
    for (int i = 0; i < 8; ++i)
    {
      arrayOfByte[i] = (byte)(int)paramLong;
      paramLong >>= 8;
    }
    return arrayOfByte;
  }

  static
  {
    String str = Sun.getSeedSource();
    if ((str.equals("file:/dev/random")) || (str.equals("file:/dev/urandom")))
      try
      {
        instance = new NativeSeedGenerator();
        if (debug != null)
          debug.println("Using operating system seed generator");
      }
      catch (IOException localIOException1)
      {
        if (debug != null)
          debug.println("Failed to use operating system seed generator: " + localIOException1.toString());
      }
    else if (str.length() != 0)
      try
      {
        instance = new URLSeedGenerator(str);
        if (debug != null)
          debug.println("Using URL seed generator reading from " + str);
      }
      catch (IOException localIOException2)
      {
        if (debug != null)
          debug.println("Failed to create seed generator with " + str + ": " + localIOException2.toString());
      }
    if (instance == null)
    {
      if (debug != null)
        debug.println("Using default threaded seed generator");
      instance = new ThreadedSeedGenerator();
    }
  }

  private static class ThreadedSeedGenerator extends SeedGenerator
  implements Runnable
  {
    private byte[] pool = new byte[20];
    private int start = this.end = 0;
    private int end;
    private int count;
    ThreadGroup seedGroup;
    private static byte[] rndTab = { 56, 30, -107, -6, -86, 25, -83, 75, -12, -64, 5, -128, 78, 21, 16, 32, 70, -81, 37, -51, -43, -46, -108, 87, 29, 17, -55, 22, -11, -111, -115, 84, -100, 108, -45, -15, -98, 72, -33, -28, 31, -52, -37, -117, -97, -27, 93, -123, 47, 126, -80, -62, -93, -79, 61, -96, -65, -5, -47, -119, 14, 89, 81, -118, -88, 20, 67, -126, -113, 60, -102, 55, 110, 28, 85, 121, 122, -58, 2, 45, 43, 24, -9, 103, -13, 102, -68, -54, -101, -104, 19, 13, -39, -26, -103, 62, 77, 51, 44, 111, 73, 18, -127, -82, 4, -30, 11, -99, -74, 40, -89, 42, -76, -77, -94, -35, -69, 35, 120, 76, 33, -73, -7, 82, -25, -10, 88, 125, -112, 58, 83, 95, 6, 10, 98, -34, 80, 15, -91, 86, -19, 52, -17, 117, 49, -63, 118, -90, 36, -116, -40, -71, 97, -53, -109, -85, 109, -16, -3, 104, -95, 68, 54, 34, 26, 114, -1, 106, -121, 3, 66, 0, 100, -84, 57, 107, 119, -42, 112, -61, 1, 48, 38, 12, -56, -57, 39, -106, -72, 41, 7, 71, -29, -59, -8, -38, 79, -31, 124, -124, 8, 91, 116, 99, -4, 9, -36, -78, 63, -49, -67, -87, 59, 101, -32, 92, 94, 53, -41, 115, -66, -70, -122, 50, -50, -22, -20, -18, -21, 23, -2, -48, 96, 65, -105, 123, -14, -110, 69, -24, -120, -75, 74, 127, -60, 113, 90, -114, 105, 46, 27, -125, -23, -44, 64 };

    ThreadedSeedGenerator()
    {
      try
      {
        MessageDigest localMessageDigest = MessageDigest.getInstance("SHA");
      }
      catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
      {
        throw new InternalError("internal error: SHA-1 not available.");
      }
      ThreadGroup[] arrayOfThreadGroup = new ThreadGroup[1];
      Thread localThread = (Thread)AccessController.doPrivileged(new PrivilegedAction(this, arrayOfThreadGroup)
      {
        public Object run()
        {
          for (Object localObject = Thread.currentThread().getThreadGroup(); (localThreadGroup = ((ThreadGroup)localObject).getParent()) != null; localObject = localThreadGroup)
            ThreadGroup localThreadGroup;
          this.val$finalsg[0] = new ThreadGroup((ThreadGroup)localObject, "SeedGenerator ThreadGroup");
          Thread localThread = new Thread(this.val$finalsg[0], this.this$0, "SeedGenerator Thread");
          localThread.setPriority(1);
          localThread.setDaemon(true);
          return localThread;
        }
      });
      this.seedGroup = arrayOfThreadGroup[0];
      localThread.start();
    }

    public final void run()
    {
      try
      {
        int j;
        synchronized (this)
        {
          while (this.count >= this.pool.length)
            wait();
        }
        int k = 0;
        int i = j = 0;
        while ((i < 64000) && (j < 6))
        {
          try
          {
            BogusThread localBogusThread = new BogusThread(null);
            Thread localThread = new Thread(this.seedGroup, localBogusThread, "SeedGenerator Thread");
            localThread.start();
          }
          catch (Exception localException2)
          {
            throw new InternalError("internal error: SeedGenerator thread creation error.");
          }
          int l = 0;
          l = 0;
          long l1 = System.currentTimeMillis() + 250L;
          while (System.currentTimeMillis() < l1)
          {
            synchronized (this)
            {
            }
            ++l;
          }
          k = (byte)(k ^ rndTab[(l % 255)]);
          i += l;
          ++j;
        }
        synchronized (this)
        {
          this.pool[this.end] = k;
          this.end += 1;
          this.count += 1;
          if (this.end >= this.pool.length)
            this.end = 0;
          notifyAll();
        }
      }
      catch (Exception localException1)
      {
        throw new InternalError("internal error: SeedGenerator thread generated an exception.");
      }
    }

    byte getSeedByte()
    {
      int i = 0;
      try
      {
        synchronized (this)
        {
          while (this.count <= 0)
            wait();
        }
      }
      catch (Exception localException)
      {
        if (this.count <= 0)
          throw new InternalError("internal error: SeedGenerator thread generated an exception.");
      }
      synchronized (this)
      {
        i = this.pool[this.start];
        this.pool[this.start] = 0;
        this.start += 1;
        this.count -= 1;
        if (this.start == this.pool.length)
          this.start = 0;
        notifyAll();
      }
      return i;
    }

    private static class BogusThread
  implements Runnable
    {
      public final void run()
      {
        int i;
        try
        {
          for (i = 0; i < 5; ++i)
            Thread.sleep(50L);
        }
        catch (Exception localException)
        {
        }
      }
    }
  }

  static class URLSeedGenerator extends SeedGenerator
  {
    private String deviceName;
    private BufferedInputStream devRandom;

    URLSeedGenerator(String paramString)
      throws IOException
    {
      if (paramString == null)
        throw new IOException("No random source specified");
      this.deviceName = paramString;
      init();
    }

    URLSeedGenerator()
      throws IOException
    {
      this("file:/dev/random");
    }

    private void init()
      throws IOException
    {
      URL localURL = new URL(this.deviceName);
      this.devRandom = ((BufferedInputStream)AccessController.doPrivileged(new PrivilegedAction(this, localURL)
      {
        public Object run()
        {
          try
          {
            return new BufferedInputStream(this.val$device.openStream());
          }
          catch (IOException localIOException)
          {
          }
          return null;
        }
      }));
      if (this.devRandom == null)
        throw new IOException("failed to open " + localURL);
    }

    byte getSeedByte()
    {
      int i;
      byte[] arrayOfByte = new byte[1];
      try
      {
        i = this.devRandom.read(arrayOfByte, 0, arrayOfByte.length);
      }
      catch (IOException localIOException)
      {
        throw new InternalError("URLSeedGenerator " + this.deviceName + " generated exception: " + localIOException.getMessage());
      }
      if (i == arrayOfByte.length)
        return arrayOfByte[0];
      if (i == -1)
        throw new InternalError("URLSeedGenerator " + this.deviceName + " reached end of file");
      throw new InternalError("URLSeedGenerator " + this.deviceName + " failed read");
    }
  }
}
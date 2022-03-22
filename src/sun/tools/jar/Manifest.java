package sun.tools.jar;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import sun.misc.BASE64Encoder;
import sun.net.www.MessageHeader;

public class Manifest
{
  private Vector entries;
  private byte[] tmpbuf;
  private Hashtable tableEntries;
  static final String[] hashes = { "SHA" };
  static final byte[] EOL = { 13, 10 };
  static final boolean debug = 0;
  static final String VERSION = "1.0";

  static final void debug(String paramString)
  {
  }

  public Manifest()
  {
    this.entries = new Vector();
    this.tmpbuf = new byte[512];
    this.tableEntries = new Hashtable();
  }

  public Manifest(byte[] paramArrayOfByte)
    throws IOException
  {
    this(new ByteArrayInputStream(paramArrayOfByte), false);
  }

  public Manifest(InputStream paramInputStream)
    throws IOException
  {
    this(paramInputStream, true);
  }

  public Manifest(InputStream paramInputStream, boolean paramBoolean)
    throws IOException
  {
    this.entries = new Vector();
    this.tmpbuf = new byte[512];
    this.tableEntries = new Hashtable();
    if (!(paramInputStream.markSupported()))
      paramInputStream = new BufferedInputStream(paramInputStream);
    while (true)
    {
      paramInputStream.mark(1);
      if (paramInputStream.read() == -1)
        return;
      paramInputStream.reset();
      MessageHeader localMessageHeader = new MessageHeader(paramInputStream);
      if (paramBoolean)
        doHashes(localMessageHeader);
      addEntry(localMessageHeader);
    }
  }

  public Manifest(String[] paramArrayOfString)
    throws IOException
  {
    this.entries = new Vector();
    this.tmpbuf = new byte[512];
    this.tableEntries = new Hashtable();
    MessageHeader localMessageHeader = new MessageHeader();
    localMessageHeader.add("Manifest-Version", "1.0");
    String str = System.getProperty("java.version");
    localMessageHeader.add("Created-By", "Manifest JDK " + str);
    addEntry(localMessageHeader);
    addFiles(null, paramArrayOfString);
  }

  public void addEntry(MessageHeader paramMessageHeader)
  {
    this.entries.addElement(paramMessageHeader);
    String str = paramMessageHeader.findValue("Name");
    debug("addEntry for name: " + str);
    if (str != null)
      this.tableEntries.put(str, paramMessageHeader);
  }

  public MessageHeader getEntry(String paramString)
  {
    return ((MessageHeader)this.tableEntries.get(paramString));
  }

  public MessageHeader entryAt(int paramInt)
  {
    return ((MessageHeader)this.entries.elementAt(paramInt));
  }

  public Enumeration entries()
  {
    return this.entries.elements();
  }

  public void addFiles(File paramFile, String[] paramArrayOfString)
    throws IOException
  {
    if (paramArrayOfString == null)
      return;
    for (int i = 0; i < paramArrayOfString.length; ++i)
    {
      File localFile;
      if (paramFile == null)
        localFile = new File(paramArrayOfString[i]);
      else
        localFile = new File(paramFile, paramArrayOfString[i]);
      if (localFile.isDirectory())
        addFiles(localFile, localFile.list());
      else
        addFile(localFile);
    }
  }

  private final String stdToLocal(String paramString)
  {
    return paramString.replace('/', File.separatorChar);
  }

  private final String localToStd(String paramString)
  {
    paramString = paramString.replace(File.separatorChar, '/');
    if (paramString.startsWith("./"))
      paramString = paramString.substring(2);
    else if (paramString.startsWith("/"))
      paramString = paramString.substring(1);
    return paramString;
  }

  public void addFile(File paramFile)
    throws IOException
  {
    String str = localToStd(paramFile.getPath());
    if (this.tableEntries.get(str) == null)
    {
      MessageHeader localMessageHeader = new MessageHeader();
      localMessageHeader.add("Name", str);
      addEntry(localMessageHeader);
    }
  }

  public void doHashes(MessageHeader paramMessageHeader)
    throws IOException
  {
    String str = paramMessageHeader.findValue("Name");
    if ((str == null) || (str.endsWith("/")))
      return;
    BASE64Encoder localBASE64Encoder = new BASE64Encoder();
    for (int i = 0; i < hashes.length; ++i)
    {
      FileInputStream localFileInputStream = new FileInputStream(stdToLocal(str));
      try
      {
        MessageDigest localMessageDigest = MessageDigest.getInstance(hashes[i]);
        while ((j = localFileInputStream.read(this.tmpbuf, 0, this.tmpbuf.length)) != -1)
        {
          int j;
          localMessageDigest.update(this.tmpbuf, 0, j);
        }
        paramMessageHeader.set(hashes[i] + "-Digest", localBASE64Encoder.encode(localMessageDigest.digest()));
      }
      catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
      {
      }
      finally
      {
        localFileInputStream.close();
      }
    }
  }

  public void stream(OutputStream paramOutputStream)
    throws IOException
  {
    PrintStream localPrintStream;
    if (paramOutputStream instanceof PrintStream)
      localPrintStream = (PrintStream)paramOutputStream;
    else
      localPrintStream = new PrintStream(paramOutputStream);
    MessageHeader localMessageHeader1 = (MessageHeader)this.entries.elementAt(0);
    if (localMessageHeader1.findValue("Manifest-Version") == null)
    {
      String str = System.getProperty("java.version");
      if (localMessageHeader1.findValue("Name") == null)
      {
        localMessageHeader1.prepend("Manifest-Version", "1.0");
        localMessageHeader1.add("Created-By", "Manifest JDK " + str);
      }
      else
      {
        localPrintStream.print("Manifest-Version: 1.0\r\nCreated-By: " + str + "\r\n\r\n");
      }
      localPrintStream.flush();
    }
    localMessageHeader1.print(localPrintStream);
    for (int i = 1; i < this.entries.size(); ++i)
    {
      MessageHeader localMessageHeader2 = (MessageHeader)this.entries.elementAt(i);
      localMessageHeader2.print(localPrintStream);
    }
  }

  public static boolean isManifestName(String paramString)
  {
    if (paramString.charAt(0) == '/')
      paramString = paramString.substring(1, paramString.length());
    paramString = paramString.toUpperCase();
    return (paramString.equals("META-INF/MANIFEST.MF"));
  }
}
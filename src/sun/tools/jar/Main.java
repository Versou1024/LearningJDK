package sun.tools.jar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import sun.misc.JarIndex;

public class Main
{
  String program;
  PrintStream out;
  PrintStream err;
  String fname;
  String mname;
  String ename;
  String zname = "";
  String[] files;
  String rootjar = null;
  Hashtable filesTable = new Hashtable();
  Vector paths = new Vector();
  Vector v;
  CRC32 crc32 = new CRC32();
  boolean cflag;
  boolean uflag;
  boolean xflag;
  boolean tflag;
  boolean vflag;
  boolean flag0;
  boolean Mflag;
  boolean iflag;
  static final String MANIFEST = "META-INF/MANIFEST.MF";
  static final String MANIFEST_DIR = "META-INF/";
  static final String VERSION = "1.0";
  static final char SEPARATOR = File.separatorChar;
  static final String INDEX = "META-INF/INDEX.LIST";
  private static ResourceBundle rsrc;
  private static final boolean useExtractionTime = Boolean.getBoolean("sun.tools.jar.useExtractionTime");
  private boolean ok;
  private Hashtable jarTable = new Hashtable();

  private String getMsg(String paramString)
  {
    try
    {
      return rsrc.getString(paramString);
    }
    catch (MissingResourceException localMissingResourceException)
    {
      throw new Error("Error in message file");
    }
  }

  private String formatMsg(String paramString1, String paramString2)
  {
    String str = getMsg(paramString1);
    String[] arrayOfString = new String[1];
    arrayOfString[0] = paramString2;
    return MessageFormat.format(str, (Object[])arrayOfString);
  }

  private String formatMsg2(String paramString1, String paramString2, String paramString3)
  {
    String str = getMsg(paramString1);
    String[] arrayOfString = new String[2];
    arrayOfString[0] = paramString2;
    arrayOfString[1] = paramString3;
    return MessageFormat.format(str, (Object[])arrayOfString);
  }

  public Main(PrintStream paramPrintStream1, PrintStream paramPrintStream2, String paramString)
  {
    this.out = paramPrintStream1;
    this.err = paramPrintStream2;
    this.program = paramString;
  }

  public synchronized boolean run(String[] paramArrayOfString)
  {
    this.ok = true;
    if (!(parseArgs(paramArrayOfString)))
      return false;
    try
    {
      Object localObject1;
      Object localObject2;
      Object localObject3;
      if ((((this.cflag) || (this.uflag))) && (this.fname != null))
      {
        this.zname = this.fname.replace(File.separatorChar, '/');
        if (this.zname.startsWith("./"))
          this.zname = this.zname.substring(2);
      }
      if (this.cflag)
      {
        localObject1 = null;
        localObject2 = null;
        if (!(this.Mflag))
        {
          if (this.mname != null)
          {
            localObject2 = new FileInputStream(this.mname);
            localObject1 = new Manifest(new BufferedInputStream((InputStream)localObject2));
          }
          else
          {
            localObject1 = new Manifest();
          }
          addVersion((Manifest)localObject1);
          addCreatedBy((Manifest)localObject1);
          if (isAmbigousMainClass((Manifest)localObject1))
          {
            if (localObject2 != null)
              ((InputStream)localObject2).close();
            return false;
          }
          if (this.ename != null)
            addMainClass((Manifest)localObject1, this.ename);
        }
        if (this.fname != null)
        {
          localObject3 = new FileOutputStream(this.fname);
        }
        else
        {
          localObject3 = new FileOutputStream(FileDescriptor.out);
          if (this.vflag)
            this.vflag = false;
        }
        create(new BufferedOutputStream((OutputStream)localObject3), expand(this.files), (Manifest)localObject1);
        if (localObject2 != null)
          ((InputStream)localObject2).close();
        ((OutputStream)localObject3).close();
      }
      else if (this.uflag)
      {
        FileOutputStream localFileOutputStream;
        localObject1 = null;
        localObject2 = null;
        if (this.fname != null)
        {
          localObject1 = new File(this.fname);
          str = ((File)localObject1).getParent();
          localObject2 = File.createTempFile("tmp", null, new File(str));
          localObject3 = new FileInputStream((File)localObject1);
          localFileOutputStream = new FileOutputStream((File)localObject2);
        }
        else
        {
          localObject3 = new FileInputStream(FileDescriptor.in);
          localFileOutputStream = new FileOutputStream(FileDescriptor.out);
          this.vflag = false;
        }
        String str = ((!(this.Mflag)) && (this.mname != null)) ? new FileInputStream(this.mname) : null;
        expand(this.files);
        boolean bool = update((InputStream)localObject3, new BufferedOutputStream(localFileOutputStream), str);
        if (this.ok)
          this.ok = bool;
        ((FileInputStream)localObject3).close();
        localFileOutputStream.close();
        if (str != null)
          str.close();
        if (this.fname != null)
        {
          ((File)localObject1).delete();
          if (!(((File)localObject2).renameTo((File)localObject1)))
          {
            ((File)localObject2).delete();
            throw new IOException(getMsg("error.write.file"));
          }
          ((File)localObject2).delete();
        }
      }
      else if ((this.xflag) || (this.tflag))
      {
        if (this.fname != null)
          localObject1 = new FileInputStream(this.fname);
        else
          localObject1 = new FileInputStream(FileDescriptor.in);
        if (this.xflag)
          extract(new BufferedInputStream((InputStream)localObject1), this.files);
        else
          list(new BufferedInputStream((InputStream)localObject1), this.files);
        ((InputStream)localObject1).close();
      }
      else if (this.iflag)
      {
        genIndex(this.rootjar, this.files);
      }
    }
    catch (IOException localIOException)
    {
      fatalError(localIOException);
      this.ok = false;
    }
    catch (Error localError)
    {
      localError.printStackTrace();
      this.ok = false;
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
      this.ok = false;
    }
    this.out.flush();
    this.err.flush();
    return this.ok;
  }

  boolean parseArgs(String[] paramArrayOfString)
  {
    int k;
    try
    {
      paramArrayOfString = CommandLine.parse(paramArrayOfString);
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      fatalError(formatMsg("error.cant.open", localFileNotFoundException.getMessage()));
      return false;
    }
    catch (IOException localIOException)
    {
      fatalError(localIOException);
      return false;
    }
    int i = 1;
    try
    {
      String str1 = paramArrayOfString[0];
      if (str1.startsWith("-"))
        str1 = str1.substring(1);
      for (k = 0; k < str1.length(); ++k)
        switch (str1.charAt(k))
        {
        case 'c':
          if ((this.xflag) || (this.tflag) || (this.uflag))
          {
            usageError();
            return false;
          }
          this.cflag = true;
          break;
        case 'u':
          if ((this.cflag) || (this.xflag) || (this.tflag))
          {
            usageError();
            return false;
          }
          this.uflag = true;
          break;
        case 'x':
          if ((this.cflag) || (this.uflag) || (this.tflag))
          {
            usageError();
            return false;
          }
          this.xflag = true;
          break;
        case 't':
          if ((this.cflag) || (this.uflag) || (this.xflag))
          {
            usageError();
            return false;
          }
          this.tflag = true;
          break;
        case 'M':
          this.Mflag = true;
          break;
        case 'v':
          this.vflag = true;
          break;
        case 'f':
          this.fname = paramArrayOfString[(i++)];
          break;
        case 'm':
          this.mname = paramArrayOfString[(i++)];
          break;
        case '0':
          this.flag0 = true;
          break;
        case 'i':
          this.rootjar = paramArrayOfString[(i++)];
          this.iflag = true;
          break;
        case 'e':
          this.ename = paramArrayOfString[(i++)];
          break;
        default:
          error(formatMsg("error.illegal.option", String.valueOf(str1.charAt(k))));
          usageError();
          return false;
        }
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException1)
    {
      usageError();
      return false;
    }
    if ((!(this.cflag)) && (!(this.tflag)) && (!(this.xflag)) && (!(this.uflag)) && (!(this.iflag)))
    {
      error(getMsg("error.bad.option"));
      usageError();
      return false;
    }
    int j = paramArrayOfString.length - i;
    if (j > 0)
    {
      k = 0;
      String[] arrayOfString = new String[j];
      try
      {
        for (int l = i; l < paramArrayOfString.length; ++l)
          if (paramArrayOfString[l].equals("-C"))
          {
            String str2 = paramArrayOfString[(++l)];
            str2 = str2 + File.separator;
            this.paths.addElement(str2.replace(File.separatorChar, '/'));
            arrayOfString[(k++)] = str2 + paramArrayOfString[(++l)];
          }
          else
          {
            arrayOfString[(k++)] = paramArrayOfString[l];
          }
      }
      catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException2)
      {
        usageError();
        return false;
      }
      this.files = new String[k];
      System.arraycopy(arrayOfString, 0, this.files, 0, k);
    }
    else
    {
      if ((this.cflag) && (this.mname == null))
      {
        error(getMsg("error.bad.cflag"));
        usageError();
        return false;
      }
      if (this.uflag)
      {
        if ((this.mname != null) || (this.ename != null))
          return true;
        error(getMsg("error.bad.uflag"));
        usageError();
        return false;
      }
    }
    return true;
  }

  String[] expand(String[] paramArrayOfString)
  {
    this.v = new Vector();
    expand(null, paramArrayOfString, this.v, this.filesTable);
    paramArrayOfString = new String[this.v.size()];
    for (int i = 0; i < paramArrayOfString.length; ++i)
      paramArrayOfString[i] = ((File)this.v.elementAt(i)).getPath();
    return paramArrayOfString;
  }

  void expand(File paramFile, String[] paramArrayOfString, Vector paramVector, Hashtable paramHashtable)
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
      if (localFile.isFile())
      {
        if (!(paramHashtable.contains(localFile)))
        {
          paramHashtable.put(entryName(localFile.getPath()), localFile);
          paramVector.addElement(localFile);
        }
      }
      else if (localFile.isDirectory())
      {
        String str = localFile.getPath();
        str = str + File.separator;
        paramHashtable.put(entryName(str), localFile);
        paramVector.addElement(localFile);
        expand(localFile, localFile.list(), paramVector, paramHashtable);
      }
      else
      {
        error(formatMsg("error.nosuch.fileordir", String.valueOf(localFile)));
        this.ok = false;
      }
    }
  }

  void create(OutputStream paramOutputStream, String[] paramArrayOfString, Manifest paramManifest)
    throws IOException
  {
    JarOutputStream localJarOutputStream = new JarOutputStream(paramOutputStream);
    if (this.flag0)
      localJarOutputStream.setMethod(0);
    if (paramManifest != null)
    {
      if (this.vflag)
        output(getMsg("out.added.manifest"));
      ZipEntry localZipEntry = new ZipEntry("META-INF/");
      localZipEntry.setTime(System.currentTimeMillis());
      localZipEntry.setSize(3412047669210644480L);
      localZipEntry.setCrc(3412047669210644480L);
      localJarOutputStream.putNextEntry(localZipEntry);
      localZipEntry = new ZipEntry("META-INF/MANIFEST.MF");
      localZipEntry.setTime(System.currentTimeMillis());
      if (this.flag0)
        crc32Manifest(localZipEntry, paramManifest);
      localJarOutputStream.putNextEntry(localZipEntry);
      paramManifest.write(localJarOutputStream);
      localJarOutputStream.closeEntry();
    }
    for (int i = 0; i < paramArrayOfString.length; ++i)
      addFile(localJarOutputStream, new File(paramArrayOfString[i]));
    localJarOutputStream.close();
  }

  boolean update(InputStream paramInputStream1, OutputStream paramOutputStream, InputStream paramInputStream2)
    throws IOException
  {
    Hashtable localHashtable = this.filesTable;
    Vector localVector = this.v;
    ZipInputStream localZipInputStream = new ZipInputStream(paramInputStream1);
    JarOutputStream localJarOutputStream = new JarOutputStream(paramOutputStream);
    ZipEntry localZipEntry = null;
    int i = 0;
    byte[] arrayOfByte = new byte[1024];
    int j = 0;
    int k = 1;
    if (localHashtable.containsKey("META-INF/INDEX.LIST"))
      addIndex((JarIndex)localHashtable.get("META-INF/INDEX.LIST"), localJarOutputStream);
    while (true)
    {
      String str;
      boolean bool1;
      Object localObject;
      while (true)
      {
        do
        {
          if ((localZipEntry = localZipInputStream.getNextEntry()) == null)
            break label403;
          str = localZipEntry.getName();
          bool1 = str.toUpperCase(Locale.ENGLISH).equals("META-INF/MANIFEST.MF");
        }
        while ((str.toUpperCase().equals("META-INF/INDEX.LIST")) && (localHashtable.containsKey("META-INF/INDEX.LIST")));
        if ((!(this.Mflag)) || (!(bool1)))
          break;
      }
      if ((bool1) && (((paramInputStream2 != null) || (this.ename != null))))
      {
        i = 1;
        if (paramInputStream2 != null)
        {
          localObject = new FileInputStream(this.mname);
          boolean bool2 = isAmbigousMainClass(new Manifest((InputStream)localObject));
          ((FileInputStream)localObject).close();
          if (bool2)
            return false;
        }
        localObject = new Manifest(localZipInputStream);
        if (paramInputStream2 != null)
          ((Manifest)localObject).read(paramInputStream2);
        updateManifest((Manifest)localObject, localJarOutputStream);
      }
      else if (!(localHashtable.containsKey(str)))
      {
        localObject = new ZipEntry(str);
        ((ZipEntry)localObject).setMethod(localZipEntry.getMethod());
        ((ZipEntry)localObject).setTime(localZipEntry.getTime());
        ((ZipEntry)localObject).setComment(localZipEntry.getComment());
        ((ZipEntry)localObject).setExtra(localZipEntry.getExtra());
        if (localZipEntry.getMethod() == 0)
        {
          ((ZipEntry)localObject).setSize(localZipEntry.getSize());
          ((ZipEntry)localObject).setCrc(localZipEntry.getCrc());
        }
        localJarOutputStream.putNextEntry((ZipEntry)localObject);
        while ((j = localZipInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1)
          localJarOutputStream.write(arrayOfByte, 0, j);
      }
      else
      {
        addFile(localJarOutputStream, (File)(File)localHashtable.get(str));
        localHashtable.remove(str);
      }
    }
    label403: localHashtable.remove("META-INF/INDEX.LIST");
    if (!(localHashtable.isEmpty()))
      for (int l = 0; l < localVector.size(); ++l)
      {
        File localFile = (File)localVector.elementAt(l);
        if (localHashtable.containsValue(localFile))
          addFile(localJarOutputStream, localFile);
      }
    if (i == 0)
      if (paramInputStream2 != null)
      {
        Manifest localManifest = new Manifest(paramInputStream2);
        k = (!(isAmbigousMainClass(localManifest))) ? 1 : 0;
        if (k != 0)
          updateManifest(localManifest, localJarOutputStream);
      }
      else if (this.ename != null)
      {
        updateManifest(new Manifest(), localJarOutputStream);
      }
    localZipInputStream.close();
    localJarOutputStream.close();
    return k;
  }

  private void addIndex(JarIndex paramJarIndex, ZipOutputStream paramZipOutputStream)
    throws IOException
  {
    ZipEntry localZipEntry = new ZipEntry("META-INF/INDEX.LIST");
    localZipEntry.setTime(System.currentTimeMillis());
    if (this.flag0)
    {
      localZipEntry.setMethod(0);
      File localFile = File.createTempFile("index", null, new File("."));
      BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile));
      paramJarIndex.write(localBufferedOutputStream);
      crc32File(localZipEntry, localFile);
      localBufferedOutputStream.close();
      localFile.delete();
    }
    paramZipOutputStream.putNextEntry(localZipEntry);
    paramJarIndex.write(paramZipOutputStream);
    if (this.vflag);
  }

  private void updateManifest(Manifest paramManifest, ZipOutputStream paramZipOutputStream)
    throws IOException
  {
    addVersion(paramManifest);
    addCreatedBy(paramManifest);
    if (this.ename != null)
      addMainClass(paramManifest, this.ename);
    ZipEntry localZipEntry = new ZipEntry("META-INF/MANIFEST.MF");
    localZipEntry.setTime(System.currentTimeMillis());
    if (this.flag0)
    {
      localZipEntry.setMethod(0);
      crc32Manifest(localZipEntry, paramManifest);
    }
    paramZipOutputStream.putNextEntry(localZipEntry);
    paramManifest.write(paramZipOutputStream);
    if (this.vflag)
      output(getMsg("out.update.manifest"));
  }

  private String entryName(String paramString)
  {
    paramString = paramString.replace(File.separatorChar, '/');
    Object localObject = "";
    for (int i = 0; i < this.paths.size(); ++i)
    {
      String str = (String)this.paths.elementAt(i);
      if ((paramString.startsWith(str)) && (str.length() > ((String)localObject).length()))
        localObject = str;
    }
    paramString = paramString.substring(((String)localObject).length());
    if (paramString.startsWith("/"))
      paramString = paramString.substring(1);
    else if (paramString.startsWith("./"))
      paramString = paramString.substring(2);
    return ((String)paramString);
  }

  private void addVersion(Manifest paramManifest)
  {
    Attributes localAttributes = paramManifest.getMainAttributes();
    if (localAttributes.getValue(Attributes.Name.MANIFEST_VERSION) == null)
      localAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
  }

  private void addCreatedBy(Manifest paramManifest)
  {
    Attributes localAttributes = paramManifest.getMainAttributes();
    if (localAttributes.getValue(new Attributes.Name("Created-By")) == null)
    {
      String str1 = System.getProperty("java.vendor");
      String str2 = System.getProperty("java.version");
      localAttributes.put(new Attributes.Name("Created-By"), str2 + " (" + str1 + ")");
    }
  }

  private void addMainClass(Manifest paramManifest, String paramString)
  {
    Attributes localAttributes = paramManifest.getMainAttributes();
    localAttributes.put(Attributes.Name.MAIN_CLASS, paramString);
  }

  private boolean isAmbigousMainClass(Manifest paramManifest)
  {
    if (this.ename != null)
    {
      Attributes localAttributes = paramManifest.getMainAttributes();
      if (localAttributes.get(Attributes.Name.MAIN_CLASS) != null)
      {
        error(getMsg("error.bad.eflag"));
        usageError();
        return true;
      }
    }
    return false;
  }

  void addFile(ZipOutputStream paramZipOutputStream, File paramFile)
    throws IOException
  {
    String str = paramFile.getPath();
    boolean bool = paramFile.isDirectory();
    if (bool)
      str = str + File.separator;
    str = entryName(str);
    if ((str.equals("")) || (str.equals(".")) || (str.equals(this.zname)))
      return;
    if ((((str.equals("META-INF/")) || (str.equals("META-INF/MANIFEST.MF")))) && (!(this.Mflag)))
    {
      if (this.vflag)
        output(formatMsg("out.ignore.entry", str));
      return;
    }
    long l1 = (bool) ? 3412047600491167744L : paramFile.length();
    if (this.vflag)
      this.out.print(formatMsg("out.adding", str));
    ZipEntry localZipEntry = new ZipEntry(str);
    localZipEntry.setTime(paramFile.lastModified());
    if (l1 == 3412046689958100992L)
    {
      localZipEntry.setMethod(0);
      localZipEntry.setSize(3412047823829467136L);
      localZipEntry.setCrc(3412047823829467136L);
    }
    else if (this.flag0)
    {
      localZipEntry.setSize(l1);
      localZipEntry.setMethod(0);
      crc32File(localZipEntry, paramFile);
    }
    paramZipOutputStream.putNextEntry(localZipEntry);
    if (!(bool))
    {
      byte[] arrayOfByte = new byte[1024];
      BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(paramFile));
      while ((i = localBufferedInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1)
      {
        int i;
        paramZipOutputStream.write(arrayOfByte, 0, i);
      }
      localBufferedInputStream.close();
    }
    paramZipOutputStream.closeEntry();
    if (this.vflag)
    {
      l1 = localZipEntry.getSize();
      long l2 = localZipEntry.getCompressedSize();
      this.out.print(formatMsg2("out.size", String.valueOf(l1), String.valueOf(l2)));
      if (localZipEntry.getMethod() == 8)
      {
        long l3 = 3412048167426850816L;
        if (l1 != 3412047686390513664L)
          l3 = (l1 - l2) * 100L / l1;
        output(formatMsg("out.deflated", String.valueOf(l3)));
      }
      else
      {
        output(getMsg("out.stored"));
      }
    }
  }

  private void crc32Manifest(ZipEntry paramZipEntry, Manifest paramManifest)
    throws IOException
  {
    this.crc32.reset();
    CRC32OutputStream localCRC32OutputStream = new CRC32OutputStream(this.crc32);
    paramManifest.write(localCRC32OutputStream);
    paramZipEntry.setSize(localCRC32OutputStream.n);
    paramZipEntry.setCrc(this.crc32.getValue());
  }

  private void crc32File(ZipEntry paramZipEntry, File paramFile)
    throws IOException
  {
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(paramFile));
    byte[] arrayOfByte = new byte[1024];
    this.crc32.reset();
    int i = 0;
    int j = 0;
    long l = paramFile.length();
    while ((i = localBufferedInputStream.read(arrayOfByte)) != -1)
    {
      j += i;
      this.crc32.update(arrayOfByte, 0, i);
    }
    localBufferedInputStream.close();
    if (j != (int)l)
      throw new JarException(formatMsg("error.incorrect.length", paramFile.getPath()));
    paramZipEntry.setCrc(this.crc32.getValue());
  }

  void extract(InputStream paramInputStream, String[] paramArrayOfString)
    throws IOException
  {
    ZipInputStream localZipInputStream = new ZipInputStream(paramInputStream);
    1 local1 = new HashSet(this)
    {
      public boolean add()
      {
        return (((paramZipEntry == null) || (Main.access$000())) ? false : super.add(paramZipEntry));
      }
    };
    while (true)
    {
      ZipEntry localZipEntry1;
      while (true)
      {
        if ((localZipEntry1 = localZipInputStream.getNextEntry()) == null)
          break label119;
        if (paramArrayOfString != null)
          break;
        local1.add(extractFile(localZipInputStream, localZipEntry1));
      }
      localObject = localZipEntry1.getName();
      for (int i = 0; i < paramArrayOfString.length; ++i)
      {
        String str = paramArrayOfString[i].replace(File.separatorChar, '/');
        if (((String)localObject).startsWith(str))
        {
          local1.add(extractFile(localZipInputStream, localZipEntry1));
          break;
        }
      }
    }
    label119: Object localObject = local1.iterator();
    while (((Iterator)localObject).hasNext())
    {
      ZipEntry localZipEntry2 = (ZipEntry)((Iterator)localObject).next();
      long l = localZipEntry2.getTime();
      if (l != -1L)
      {
        File localFile = new File(localZipEntry2.getName().replace('/', File.separatorChar));
        localFile.setLastModified(l);
      }
    }
  }

  ZipEntry extractFile(ZipInputStream paramZipInputStream, ZipEntry paramZipEntry)
    throws IOException
  {
    ZipEntry localZipEntry = null;
    String str = paramZipEntry.getName();
    File localFile = new File(paramZipEntry.getName().replace('/', File.separatorChar));
    if (paramZipEntry.isDirectory())
    {
      if (localFile.exists())
      {
        if (localFile.isDirectory())
          break label100;
        throw new IOException(formatMsg("error.create.dir", localFile.getPath()));
      }
      if (!(localFile.mkdirs()))
        throw new IOException(formatMsg("error.create.dir", localFile.getPath()));
      localZipEntry = paramZipEntry;
      if (this.vflag)
        label100: output(formatMsg("out.create", str));
    }
    else
    {
      if (localFile.getParent() != null)
      {
        localObject = new File(localFile.getParent());
        if (((!(((File)localObject).exists())) && (!(((File)localObject).mkdirs()))) || (!(((File)localObject).isDirectory())))
          throw new IOException(formatMsg("error.create.dir", ((File)localObject).getPath()));
      }
      Object localObject = new FileOutputStream(localFile);
      byte[] arrayOfByte = new byte[512];
      while ((i = paramZipInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1)
      {
        int i;
        ((OutputStream)localObject).write(arrayOfByte, 0, i);
      }
      paramZipInputStream.closeEntry();
      ((OutputStream)localObject).close();
      if (this.vflag)
        if (paramZipEntry.getMethod() == 8)
          output(formatMsg("out.inflated", str));
        else
          output(formatMsg("out.extracted", str));
    }
    if (!(useExtractionTime))
    {
      long l = paramZipEntry.getTime();
      if (l != -1L)
        localFile.setLastModified(l);
    }
    return ((ZipEntry)localZipEntry);
  }

  void list(InputStream paramInputStream, String[] paramArrayOfString)
    throws IOException
  {
    ZipInputStream localZipInputStream = new ZipInputStream(paramInputStream);
    while ((localZipEntry = localZipInputStream.getNextEntry()) != null)
    {
      ZipEntry localZipEntry;
      String str1 = localZipEntry.getName();
      localZipInputStream.closeEntry();
      if (paramArrayOfString == null)
        printEntry(localZipEntry);
      else
        for (int i = 0; i < paramArrayOfString.length; ++i)
        {
          String str2 = paramArrayOfString[i].replace(File.separatorChar, '/');
          if (str1.startsWith(str2))
          {
            printEntry(localZipEntry);
            break;
          }
        }
    }
  }

  void dumpIndex(String paramString, JarIndex paramJarIndex)
    throws IOException
  {
    this.filesTable.put("META-INF/INDEX.LIST", paramJarIndex);
    File localFile1 = File.createTempFile("scratch", null, new File("."));
    File localFile2 = new File(paramString);
    boolean bool = update(new FileInputStream(localFile2), new FileOutputStream(localFile1), null);
    localFile2.delete();
    if (!(localFile1.renameTo(localFile2)))
    {
      localFile1.delete();
      throw new IOException(getMsg("error.write.file"));
    }
    localFile1.delete();
  }

  Vector getJarPath(String paramString)
    throws IOException
  {
    Vector localVector = new Vector();
    localVector.add(paramString);
    this.jarTable.put(paramString, paramString);
    String str1 = paramString.substring(0, Math.max(0, paramString.lastIndexOf(47) + 1));
    JarFile localJarFile = new JarFile(paramString.replace('/', File.separatorChar));
    if (localJarFile != null)
    {
      Manifest localManifest = localJarFile.getManifest();
      if (localManifest != null)
      {
        Attributes localAttributes = localManifest.getMainAttributes();
        if (localAttributes != null)
        {
          String str2 = localAttributes.getValue(Attributes.Name.CLASS_PATH);
          if (str2 != null)
          {
            StringTokenizer localStringTokenizer = new StringTokenizer(str2);
            while (localStringTokenizer.hasMoreTokens())
            {
              String str3 = localStringTokenizer.nextToken();
              if (!(str3.endsWith("/")))
              {
                str3 = str1.concat(str3);
                if (this.jarTable.get(str3) == null)
                  localVector.addAll(getJarPath(str3));
              }
            }
          }
        }
      }
    }
    localJarFile.close();
    return localVector;
  }

  void genIndex(String paramString, String[] paramArrayOfString)
    throws IOException
  {
    Vector localVector = getJarPath(paramString);
    int i = localVector.size();
    if ((i == 1) && (paramArrayOfString != null))
    {
      for (int j = 0; j < paramArrayOfString.length; ++j)
        localVector.addAll(getJarPath(paramArrayOfString[j]));
      i = localVector.size();
    }
    String[] arrayOfString = (String[])(String[])localVector.toArray(new String[i]);
    JarIndex localJarIndex = new JarIndex(arrayOfString);
    dumpIndex(paramString, localJarIndex);
  }

  void printEntry(ZipEntry paramZipEntry)
    throws IOException
  {
    if (this.vflag)
    {
      StringBuffer localStringBuffer = new StringBuffer();
      String str = Long.toString(paramZipEntry.getSize());
      for (int i = 6 - str.length(); i > 0; --i)
        localStringBuffer.append(' ');
      localStringBuffer.append(str).append(' ').append(new Date(paramZipEntry.getTime()).toString());
      localStringBuffer.append(' ').append(paramZipEntry.getName());
      output(localStringBuffer.toString());
    }
    else
    {
      output(paramZipEntry.getName());
    }
  }

  void usageError()
  {
    error(getMsg("usage"));
  }

  void fatalError(Exception paramException)
  {
    paramException.printStackTrace();
  }

  void fatalError(String paramString)
  {
    error(this.program + ": " + paramString);
  }

  protected void output(String paramString)
  {
    this.out.println(paramString);
  }

  protected void error(String paramString)
  {
    this.err.println(paramString);
  }

  public static void main(String[] paramArrayOfString)
  {
    Main localMain = new Main(System.out, System.err, "jar");
    System.exit((localMain.run(paramArrayOfString)) ? 0 : 1);
  }

  static
  {
    try
    {
      rsrc = ResourceBundle.getBundle("sun.tools.jar.resources.jar");
    }
    catch (MissingResourceException localMissingResourceException)
    {
      throw new Error("Fatal: Resource for jar is missing");
    }
  }
}
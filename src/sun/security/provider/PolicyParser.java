package sun.security.provider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.security.auth.x500.X500Principal;
import sun.net.www.ParseUtil;
import sun.security.util.Debug;
import sun.security.util.PropertyExpander;
import sun.security.util.PropertyExpander.ExpandException;
import sun.security.util.ResourcesMgr;

public class PolicyParser
{
  public static final String REPLACE_NAME = "PolicyParser.REPLACE_NAME";
  private static final String EXTDIRS_PROPERTY = "java.ext.dirs";
  private static final String OLD_EXTDIRS_EXPANSION = "${java.ext.dirs}";
  static final String EXTDIRS_EXPANSION = "${{java.ext.dirs}}";
  private Vector grantEntries;
  private static final Debug debug = Debug.getInstance("parser", "\t[Policy Parser]");
  private StreamTokenizer st;
  private int lookahead;
  private boolean expandProp;
  private String keyStoreUrlString;
  private String keyStoreType;
  private String keyStoreProvider;
  private String storePassURL;

  private String expand(String paramString)
    throws PropertyExpander.ExpandException
  {
    return expand(paramString, false);
  }

  private String expand(String paramString, boolean paramBoolean)
    throws PropertyExpander.ExpandException
  {
    if (!(this.expandProp))
      return paramString;
    return PropertyExpander.expand(paramString, paramBoolean);
  }

  public PolicyParser()
  {
    this.expandProp = false;
    this.keyStoreUrlString = null;
    this.keyStoreType = null;
    this.keyStoreProvider = null;
    this.storePassURL = null;
    this.grantEntries = new Vector();
  }

  public PolicyParser(boolean paramBoolean)
  {
    this.expandProp = paramBoolean;
  }

  public void read(Reader paramReader)
    throws sun.security.provider.PolicyParser.ParsingException, IOException
  {
    if (!(paramReader instanceof BufferedReader))
      paramReader = new BufferedReader(paramReader);
    this.st = new StreamTokenizer(paramReader);
    this.st.resetSyntax();
    this.st.wordChars(97, 122);
    this.st.wordChars(65, 90);
    this.st.wordChars(46, 46);
    this.st.wordChars(48, 57);
    this.st.wordChars(95, 95);
    this.st.wordChars(36, 36);
    this.st.wordChars(160, 255);
    this.st.whitespaceChars(0, 32);
    this.st.commentChar(47);
    this.st.quoteChar(39);
    this.st.quoteChar(34);
    this.st.lowerCaseMode(false);
    this.st.ordinaryChar(47);
    this.st.slashSlashComments(true);
    this.st.slashStarComments(true);
    this.lookahead = this.st.nextToken();
    while (this.lookahead != -1)
    {
      if (peek("grant"))
      {
        GrantEntry localGrantEntry = parseGrantEntry();
        if (localGrantEntry != null)
          add(localGrantEntry);
      }
      else if ((peek("keystore")) && (this.keyStoreUrlString == null))
      {
        parseKeyStoreEntry();
      }
      else if ((peek("keystorePasswordURL")) && (this.storePassURL == null))
      {
        parseStorePassURL();
      }
      match(";");
    }
    if ((this.keyStoreUrlString == null) && (this.storePassURL != null))
      throw new sun.security.provider.PolicyParser.ParsingException(ResourcesMgr.getString("keystorePasswordURL can not be specified without also specifying keystore"));
  }

  public void add(GrantEntry paramGrantEntry)
  {
    this.grantEntries.addElement(paramGrantEntry);
  }

  public void replace(GrantEntry paramGrantEntry1, GrantEntry paramGrantEntry2)
  {
    this.grantEntries.setElementAt(paramGrantEntry2, this.grantEntries.indexOf(paramGrantEntry1));
  }

  public boolean remove(GrantEntry paramGrantEntry)
  {
    return this.grantEntries.removeElement(paramGrantEntry);
  }

  public String getKeyStoreUrl()
  {
    try
    {
      if ((this.keyStoreUrlString != null) && (this.keyStoreUrlString.length() != 0))
        return expand(this.keyStoreUrlString, true).replace(File.separatorChar, '/');
    }
    catch (PropertyExpander.ExpandException localExpandException)
    {
      if (debug != null)
        debug.println(localExpandException.toString());
      return null;
    }
    return null;
  }

  public void setKeyStoreUrl(String paramString)
  {
    this.keyStoreUrlString = paramString;
  }

  public String getKeyStoreType()
  {
    return this.keyStoreType;
  }

  public void setKeyStoreType(String paramString)
  {
    this.keyStoreType = paramString;
  }

  public String getKeyStoreProvider()
  {
    return this.keyStoreProvider;
  }

  public void setKeyStoreProvider(String paramString)
  {
    this.keyStoreProvider = paramString;
  }

  public String getStorePassURL()
  {
    try
    {
      if ((this.storePassURL != null) && (this.storePassURL.length() != 0))
        return expand(this.storePassURL, true).replace(File.separatorChar, '/');
    }
    catch (PropertyExpander.ExpandException localExpandException)
    {
      if (debug != null)
        debug.println(localExpandException.toString());
      return null;
    }
    return null;
  }

  public void setStorePassURL(String paramString)
  {
    this.storePassURL = paramString;
  }

  public Enumeration grantElements()
  {
    return this.grantEntries.elements();
  }

  public void write(Writer paramWriter)
  {
    PrintWriter localPrintWriter = new PrintWriter(new BufferedWriter(paramWriter));
    Enumeration localEnumeration = grantElements();
    localPrintWriter.println("/* AUTOMATICALLY GENERATED ON " + new Date() + "*/");
    localPrintWriter.println("/* DO NOT EDIT */");
    localPrintWriter.println();
    if (this.keyStoreUrlString != null)
      writeKeyStoreEntry(localPrintWriter);
    if (this.storePassURL != null)
      writeStorePassURL(localPrintWriter);
    while (localEnumeration.hasMoreElements())
    {
      GrantEntry localGrantEntry = (GrantEntry)localEnumeration.nextElement();
      localGrantEntry.write(localPrintWriter);
      localPrintWriter.println();
    }
    localPrintWriter.flush();
  }

  private void parseKeyStoreEntry()
    throws sun.security.provider.PolicyParser.ParsingException, IOException
  {
    match("keystore");
    this.keyStoreUrlString = match("quoted string");
    if (!(peek(",")))
      return;
    match(",");
    if (peek("\""))
      this.keyStoreType = match("quoted string");
    else
      throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), ResourcesMgr.getString("expected keystore type"));
    if (!(peek(",")))
      return;
    match(",");
    if (peek("\""))
      this.keyStoreProvider = match("quoted string");
    else
      throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), ResourcesMgr.getString("expected keystore provider"));
  }

  private void parseStorePassURL()
    throws sun.security.provider.PolicyParser.ParsingException, IOException
  {
    match("keyStorePasswordURL");
    this.storePassURL = match("quoted string");
  }

  private void writeKeyStoreEntry(PrintWriter paramPrintWriter)
  {
    paramPrintWriter.print("keystore \"");
    paramPrintWriter.print(this.keyStoreUrlString);
    paramPrintWriter.print('"');
    if ((this.keyStoreType != null) && (this.keyStoreType.length() > 0))
      paramPrintWriter.print(", \"" + this.keyStoreType + "\"");
    if ((this.keyStoreProvider != null) && (this.keyStoreProvider.length() > 0))
      paramPrintWriter.print(", \"" + this.keyStoreProvider + "\"");
    paramPrintWriter.println(";");
    paramPrintWriter.println();
  }

  private void writeStorePassURL(PrintWriter paramPrintWriter)
  {
    paramPrintWriter.print("keystorePasswordURL \"");
    paramPrintWriter.print(this.storePassURL);
    paramPrintWriter.print('"');
    paramPrintWriter.println(";");
    paramPrintWriter.println();
  }

  private GrantEntry parseGrantEntry()
    throws sun.security.provider.PolicyParser.ParsingException, IOException
  {
    Object localObject1;
    Object localObject2;
    Object localObject3;
    GrantEntry localGrantEntry = new GrantEntry();
    LinkedList localLinkedList = null;
    int i = 0;
    match("grant");
    while (true)
    {
      while (true)
      {
        while (true)
        {
          if (peek("{"))
            break label539;
          if (!(peekAndMatch("Codebase")))
            break;
          if (localGrantEntry.codeBase != null)
            throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), ResourcesMgr.getString("multiple Codebase expressions"));
          localGrantEntry.codeBase = match("quoted string");
          peekAndMatch(",");
        }
        if (!(peekAndMatch("SignedBy")))
          break;
        if (localGrantEntry.signedBy != null)
          throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), ResourcesMgr.getString("multiple SignedBy expressions"));
        localGrantEntry.signedBy = match("quoted string");
        localObject1 = new StringTokenizer(localGrantEntry.signedBy, ",", true);
        int k = 0;
        int l = 0;
        while (((StringTokenizer)localObject1).hasMoreTokens())
        {
          localObject3 = ((StringTokenizer)localObject1).nextToken().trim();
          if (((String)localObject3).equals(","))
            ++l;
          else if (((String)localObject3).length() > 0)
            ++k;
        }
        if (k <= l)
          throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), ResourcesMgr.getString("SignedBy has empty alias"));
        peekAndMatch(",");
      }
      if (!(peekAndMatch("Principal")))
        break;
      if (localLinkedList == null)
        localLinkedList = new LinkedList();
      if (peek("\""))
      {
        localObject1 = "PolicyParser.REPLACE_NAME";
        localObject2 = match("principal type");
      }
      else
      {
        if (peek("*"))
        {
          match("*");
          localObject1 = "WILDCARD_PRINCIPAL_CLASS";
        }
        else
        {
          localObject1 = match("principal type");
        }
        if (peek("*"))
        {
          match("*");
          localObject2 = "WILDCARD_PRINCIPAL_NAME";
        }
        else
        {
          localObject2 = match("quoted string");
        }
        if ((((String)localObject1).equals("WILDCARD_PRINCIPAL_CLASS")) && (!(((String)localObject2).equals("WILDCARD_PRINCIPAL_NAME"))))
        {
          if (debug != null)
            debug.println("disallowing principal that has WILDCARD class but no WILDCARD name");
          throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), ResourcesMgr.getString("can not specify Principal with a wildcard class without a wildcard name"));
        }
      }
      try
      {
        localObject2 = expand((String)localObject2);
        if ((((String)localObject1).equals("javax.security.auth.x500.X500Principal")) && (!(((String)localObject2).equals("WILDCARD_PRINCIPAL_NAME"))))
        {
          X500Principal localX500Principal = new X500Principal(new X500Principal((String)localObject2).toString());
          localObject2 = localX500Principal.getName();
        }
        localLinkedList.add(new PrincipalEntry((String)localObject1, (String)localObject2));
      }
      catch (PropertyExpander.ExpandException localExpandException3)
      {
        if (debug != null)
          debug.println("principal name expansion failed: " + ((String)localObject2));
        i = 1;
      }
      peekAndMatch(",");
    }
    throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), ResourcesMgr.getString("expected codeBase or SignedBy or Principal"));
    if (localLinkedList != null)
      label539: localGrantEntry.principals = localLinkedList;
    match("{");
    while (true)
    {
      if (peek("}"))
        break label641;
      if (!(peek("Permission")))
        break;
      try
      {
        localObject1 = parsePermissionEntry();
        localGrantEntry.add((PermissionEntry)localObject1);
      }
      catch (PropertyExpander.ExpandException localExpandException1)
      {
        if (debug != null)
          debug.println(localExpandException1.toString());
        skipEntry();
      }
      match(";");
    }
    throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), ResourcesMgr.getString("expected permission entry"));
    label641: match("}");
    try
    {
      if (localGrantEntry.signedBy != null)
        localGrantEntry.signedBy = expand(localGrantEntry.signedBy);
      if (localGrantEntry.codeBase != null)
      {
        int j;
        if (localGrantEntry.codeBase.equals("${java.ext.dirs}"))
          localGrantEntry.codeBase = "${{java.ext.dirs}}";
        if ((j = localGrantEntry.codeBase.indexOf("${{java.ext.dirs}}")) < 0)
        {
          localGrantEntry.codeBase = expand(localGrantEntry.codeBase, true).replace(File.separatorChar, '/');
        }
        else
        {
          localObject2 = parseExtDirs(localGrantEntry.codeBase, j);
          if ((localObject2 != null) && (localObject2.length > 0))
            for (int i1 = 0; i1 < localObject2.length; ++i1)
            {
              localObject3 = (GrantEntry)localGrantEntry.clone();
              ((GrantEntry)localObject3).codeBase = localObject2[i1];
              add((GrantEntry)localObject3);
              if (debug != null)
                debug.println("creating policy entry for expanded java.ext.dirs path:\n\t\t" + localObject2[i1]);
            }
          i = 1;
        }
      }
    }
    catch (PropertyExpander.ExpandException localExpandException2)
    {
      if (debug != null)
        debug.println(localExpandException2.toString());
      return null;
    }
    return ((i == 1) ? null : (GrantEntry)(GrantEntry)(GrantEntry)localGrantEntry);
  }

  private PermissionEntry parsePermissionEntry()
    throws sun.security.provider.PolicyParser.ParsingException, IOException, PropertyExpander.ExpandException
  {
    PermissionEntry localPermissionEntry = new PermissionEntry();
    match("Permission");
    localPermissionEntry.permission = match("permission type");
    if (peek("\""))
      localPermissionEntry.name = expand(match("quoted string"));
    if (!(peek(",")))
      return localPermissionEntry;
    match(",");
    if (peek("\""))
    {
      localPermissionEntry.action = expand(match("quoted string"));
      if (!(peek(",")))
        return localPermissionEntry;
      match(",");
    }
    if (peekAndMatch("SignedBy"))
      localPermissionEntry.signedBy = expand(match("quoted string"));
    return localPermissionEntry;
  }

  static String[] parseExtDirs(String paramString, int paramInt)
  {
    String str1 = System.getProperty("java.ext.dirs");
    String str2 = (paramInt > 0) ? paramString.substring(0, paramInt) : "file:";
    int i = paramInt + "${{java.ext.dirs}}".length();
    String str3 = (i < paramString.length()) ? paramString.substring(i) : (String)null;
    String[] arrayOfString = null;
    if (str1 != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(str1, File.pathSeparator);
      int j = localStringTokenizer.countTokens();
      arrayOfString = new String[j];
      for (int k = 0; k < j; ++k)
      {
        File localFile = new File(localStringTokenizer.nextToken());
        arrayOfString[k] = ParseUtil.encodePath(localFile.getAbsolutePath());
        if (!(arrayOfString[k].startsWith("/")))
          arrayOfString[k] = "/" + arrayOfString[k];
        String str4 = (str3 == null) ? "/*" : (arrayOfString[k].endsWith("/")) ? "*" : str3;
        arrayOfString[k] = str2 + arrayOfString[k] + str4;
      }
    }
    return arrayOfString;
  }

  private boolean peekAndMatch(String paramString)
    throws sun.security.provider.PolicyParser.ParsingException, IOException
  {
    if (peek(paramString))
    {
      match(paramString);
      return true;
    }
    return false;
  }

  private boolean peek(String paramString)
  {
    int i = 0;
    switch (this.lookahead)
    {
    case -3:
      if (!(paramString.equalsIgnoreCase(this.st.sval)))
        break label153;
      i = 1;
      break;
    case 44:
      if (!(paramString.equalsIgnoreCase(",")))
        break label153;
      i = 1;
      break;
    case 123:
      if (!(paramString.equalsIgnoreCase("{")))
        break label153;
      i = 1;
      break;
    case 125:
      if (!(paramString.equalsIgnoreCase("}")))
        break label153;
      i = 1;
      break;
    case 34:
      if (!(paramString.equalsIgnoreCase("\"")))
        break label153;
      i = 1;
      break;
    case 42:
      if (!(paramString.equalsIgnoreCase("*")))
        break label153;
      i = 1;
    }
    label153: return i;
  }

  private String match(String paramString)
    throws sun.security.provider.PolicyParser.ParsingException, IOException
  {
    String str = null;
    switch (this.lookahead)
    {
    case -2:
      throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), paramString, ResourcesMgr.getString("number ") + String.valueOf(this.st.nval));
    case -1:
      MessageFormat localMessageFormat = new MessageFormat(ResourcesMgr.getString("expected [expect], read [end of file]"));
      Object[] arrayOfObject = { paramString };
      throw new sun.security.provider.PolicyParser.ParsingException(localMessageFormat.format(arrayOfObject));
    case -3:
      if (paramString.equalsIgnoreCase(this.st.sval))
      {
        this.lookahead = this.st.nextToken();
      }
      else if (paramString.equalsIgnoreCase("permission type"))
      {
        str = this.st.sval;
        this.lookahead = this.st.nextToken();
      }
      else if (paramString.equalsIgnoreCase("principal type"))
      {
        str = this.st.sval;
        this.lookahead = this.st.nextToken();
      }
      else
      {
        throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), paramString, this.st.sval);
      }
    case 34:
      if (paramString.equalsIgnoreCase("quoted string"))
      {
        str = this.st.sval;
        this.lookahead = this.st.nextToken();
      }
      else if (paramString.equalsIgnoreCase("permission type"))
      {
        str = this.st.sval;
        this.lookahead = this.st.nextToken();
      }
      else if (paramString.equalsIgnoreCase("principal type"))
      {
        str = this.st.sval;
        this.lookahead = this.st.nextToken();
      }
      else
      {
        throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), paramString, this.st.sval);
      }
    case 44:
      if (paramString.equalsIgnoreCase(","))
        this.lookahead = this.st.nextToken();
      else
        throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), paramString, ",");
    case 123:
      if (paramString.equalsIgnoreCase("{"))
        this.lookahead = this.st.nextToken();
      else
        throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), paramString, "{");
    case 125:
      if (paramString.equalsIgnoreCase("}"))
        this.lookahead = this.st.nextToken();
      else
        throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), paramString, "}");
    case 59:
      if (paramString.equalsIgnoreCase(";"))
        this.lookahead = this.st.nextToken();
      else
        throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), paramString, ";");
    case 42:
      if (paramString.equalsIgnoreCase("*"))
        this.lookahead = this.st.nextToken();
      else
        throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), paramString, "*");
    }
    throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), paramString, new String(new char[] { (char)this.lookahead }));
    return str;
  }

  private void skipEntry()
    throws sun.security.provider.PolicyParser.ParsingException, IOException
  {
    while (this.lookahead != 59)
    {
      switch (this.lookahead)
      {
      case -2:
        throw new sun.security.provider.PolicyParser.ParsingException(this.st.lineno(), ";", ResourcesMgr.getString("number ") + String.valueOf(this.st.nval));
      case -1:
        throw new sun.security.provider.PolicyParser.ParsingException(ResourcesMgr.getString("expected [;], read [end of file]"));
      }
      this.lookahead = this.st.nextToken();
    }
  }

  public static void main(String[] paramArrayOfString)
    throws Exception
  {
    PolicyParser localPolicyParser = new PolicyParser(true);
    localPolicyParser.read(new FileReader(paramArrayOfString[0]));
    FileWriter localFileWriter = new FileWriter(paramArrayOfString[1]);
    localPolicyParser.write(localFileWriter);
    localFileWriter.close();
  }

  public static class GrantEntry
  {
    public String signedBy;
    public String codeBase;
    public LinkedList principals;
    public Vector permissionEntries;

    public GrantEntry()
    {
      this.principals = new LinkedList();
      this.permissionEntries = new Vector();
    }

    public GrantEntry(String paramString1, String paramString2)
    {
      this.codeBase = paramString2;
      this.signedBy = paramString1;
      this.principals = new LinkedList();
      this.permissionEntries = new Vector();
    }

    public void add(PolicyParser.PermissionEntry paramPermissionEntry)
    {
      this.permissionEntries.addElement(paramPermissionEntry);
    }

    public boolean remove(PolicyParser.PrincipalEntry paramPrincipalEntry)
    {
      return this.principals.remove(paramPrincipalEntry);
    }

    public boolean remove(PolicyParser.PermissionEntry paramPermissionEntry)
    {
      return this.permissionEntries.removeElement(paramPermissionEntry);
    }

    public boolean contains(PolicyParser.PrincipalEntry paramPrincipalEntry)
    {
      return this.principals.contains(paramPrincipalEntry);
    }

    public boolean contains(PolicyParser.PermissionEntry paramPermissionEntry)
    {
      return this.permissionEntries.contains(paramPermissionEntry);
    }

    public Enumeration permissionElements()
    {
      return this.permissionEntries.elements();
    }

    public void write(PrintWriter paramPrintWriter)
    {
      Object localObject2;
      paramPrintWriter.print("grant");
      if (this.signedBy != null)
      {
        paramPrintWriter.print(" signedBy \"");
        paramPrintWriter.print(this.signedBy);
        paramPrintWriter.print('"');
        if (this.codeBase != null)
          paramPrintWriter.print(", ");
      }
      if (this.codeBase != null)
      {
        paramPrintWriter.print(" codeBase \"");
        paramPrintWriter.print(this.codeBase);
        paramPrintWriter.print('"');
        if ((this.principals != null) && (this.principals.size() > 0))
          paramPrintWriter.print(",\n");
      }
      if ((this.principals != null) && (this.principals.size() > 0))
      {
        localObject1 = this.principals.listIterator();
        while (((ListIterator)localObject1).hasNext())
        {
          paramPrintWriter.print("      ");
          localObject2 = (PolicyParser.PrincipalEntry)((ListIterator)localObject1).next();
          ((PolicyParser.PrincipalEntry)localObject2).write(paramPrintWriter);
          if (((ListIterator)localObject1).hasNext())
            paramPrintWriter.print(",\n");
        }
      }
      paramPrintWriter.println(" {");
      Object localObject1 = this.permissionEntries.elements();
      while (((Enumeration)localObject1).hasMoreElements())
      {
        localObject2 = (PolicyParser.PermissionEntry)((Enumeration)localObject1).nextElement();
        paramPrintWriter.write("  ");
        ((PolicyParser.PermissionEntry)localObject2).write(paramPrintWriter);
      }
      paramPrintWriter.println("};");
    }

    public Object clone()
    {
      GrantEntry localGrantEntry = new GrantEntry();
      localGrantEntry.codeBase = this.codeBase;
      localGrantEntry.signedBy = this.signedBy;
      localGrantEntry.principals = new LinkedList(this.principals);
      localGrantEntry.permissionEntries = new Vector(this.permissionEntries);
      return localGrantEntry;
    }
  }

  public static class ParsingException extends GeneralSecurityException
  {
    private static final long serialVersionUID = -4330692689482574072L;
    private String i18nMessage;

    public ParsingException(String paramString)
    {
      super(paramString);
      this.i18nMessage = paramString;
    }

    public ParsingException(int paramInt, String paramString)
    {
      super("line " + paramInt + ": " + paramString);
      MessageFormat localMessageFormat = new MessageFormat(ResourcesMgr.getString("line number: msg"));
      Object[] arrayOfObject = { new Integer(paramInt), paramString };
      this.i18nMessage = localMessageFormat.format(arrayOfObject);
    }

    public ParsingException(int paramInt, String paramString1, String paramString2)
    {
      super("line " + paramInt + ": expected [" + paramString1 + "], found [" + paramString2 + "]");
      MessageFormat localMessageFormat = new MessageFormat(ResourcesMgr.getString("line number: expected [expect], found [actual]"));
      Object[] arrayOfObject = { new Integer(paramInt), paramString1, paramString2 };
      this.i18nMessage = localMessageFormat.format(arrayOfObject);
    }

    public String getLocalizedMessage()
    {
      return this.i18nMessage;
    }
  }

  public static class PermissionEntry
  {
    public String permission;
    public String name;
    public String action;
    public String signedBy;

    public PermissionEntry()
    {
    }

    public PermissionEntry(String paramString1, String paramString2, String paramString3)
    {
      this.permission = paramString1;
      this.name = paramString2;
      this.action = paramString3;
    }

    public int hashCode()
    {
      int i = this.permission.hashCode();
      if (this.name != null)
        i ^= this.name.hashCode();
      if (this.action != null)
        i ^= this.action.hashCode();
      return i;
    }

    public boolean equals(Object paramObject)
    {
      if (paramObject == this)
        return true;
      if (!(paramObject instanceof PermissionEntry))
        return false;
      PermissionEntry localPermissionEntry = (PermissionEntry)paramObject;
      if (this.permission == null)
      {
        if (localPermissionEntry.permission == null)
          break label53;
        return false;
      }
      if (!(this.permission.equals(localPermissionEntry.permission)))
        return false;
      if (this.name == null)
      {
        label53: if (localPermissionEntry.name == null)
          break label85;
        return false;
      }
      if (!(this.name.equals(localPermissionEntry.name)))
        return false;
      if (this.action == null)
      {
        label85: if (localPermissionEntry.action == null)
          break label117;
        return false;
      }
      if (!(this.action.equals(localPermissionEntry.action)))
        return false;
      if (this.signedBy == null)
      {
        label117: if (localPermissionEntry.signedBy == null)
          break label149;
        return false;
      }
      label149: return (this.signedBy.equals(localPermissionEntry.signedBy));
    }

    public void write(PrintWriter paramPrintWriter)
    {
      paramPrintWriter.print("permission ");
      paramPrintWriter.print(this.permission);
      if (this.name != null)
      {
        paramPrintWriter.print(" \"");
        paramPrintWriter.print(this.name.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\"", "\\\\\\\""));
        paramPrintWriter.print('"');
      }
      if (this.action != null)
      {
        paramPrintWriter.print(", \"");
        paramPrintWriter.print(this.action);
        paramPrintWriter.print('"');
      }
      if (this.signedBy != null)
      {
        paramPrintWriter.print(", signedBy \"");
        paramPrintWriter.print(this.signedBy);
        paramPrintWriter.print('"');
      }
      paramPrintWriter.println(";");
    }
  }

  public static class PrincipalEntry
  {
    public static final String WILDCARD_CLASS = "WILDCARD_PRINCIPAL_CLASS";
    public static final String WILDCARD_NAME = "WILDCARD_PRINCIPAL_NAME";
    String principalClass;
    String principalName;

    public PrincipalEntry(String paramString1, String paramString2)
    {
      if ((paramString1 == null) || (paramString2 == null))
        throw new NullPointerException(ResourcesMgr.getString("null principalClass or principalName"));
      this.principalClass = paramString1;
      this.principalName = paramString2;
    }

    public String getPrincipalClass()
    {
      return this.principalClass;
    }

    public String getPrincipalName()
    {
      return this.principalName;
    }

    public String getDisplayClass()
    {
      if (this.principalClass.equals("WILDCARD_PRINCIPAL_CLASS"))
        return "*";
      if (this.principalClass.equals("PolicyParser.REPLACE_NAME"))
        return "";
      return this.principalClass;
    }

    public String getDisplayName()
    {
      return getDisplayName(false);
    }

    public String getDisplayName(boolean paramBoolean)
    {
      if (this.principalName.equals("WILDCARD_PRINCIPAL_NAME"))
        return "*";
      if (paramBoolean)
        return "\"" + this.principalName + "\"";
      return this.principalName;
    }

    public String toString()
    {
      if (!(this.principalClass.equals("PolicyParser.REPLACE_NAME")))
        return getDisplayClass() + "/" + getDisplayName();
      return getDisplayName();
    }

    public boolean equals(Object paramObject)
    {
      if (this == paramObject)
        return true;
      if (!(paramObject instanceof PrincipalEntry))
        return false;
      PrincipalEntry localPrincipalEntry = (PrincipalEntry)paramObject;
      return ((this.principalClass.equals(localPrincipalEntry.principalClass)) && (this.principalName.equals(localPrincipalEntry.principalName)));
    }

    public int hashCode()
    {
      return this.principalClass.hashCode();
    }

    public void write(PrintWriter paramPrintWriter)
    {
      paramPrintWriter.print("principal " + getDisplayClass() + " " + getDisplayName(true));
    }
  }
}
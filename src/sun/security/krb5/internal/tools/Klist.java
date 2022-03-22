package sun.security.krb5.internal.tools;

import java.io.PrintStream;
import java.util.Date;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.TicketFlags;
import sun.security.krb5.internal.ccache.Credentials;
import sun.security.krb5.internal.ccache.CredentialsCache;
import sun.security.krb5.internal.crypto.EType;
import sun.security.krb5.internal.ktab.KeyTab;
import sun.security.krb5.internal.ktab.KeyTabEntry;

public class Klist
{
  Object target;
  char[] options = new char[3];
  String name;
  char action;
  private static boolean DEBUG = Krb5.DEBUG;

  public static void main(String[] paramArrayOfString)
  {
    Klist localKlist = new Klist();
    if ((paramArrayOfString == null) || (paramArrayOfString.length == 0))
      localKlist.action = 'c';
    else
      localKlist.processArgs(paramArrayOfString);
    switch (localKlist.action)
    {
    case 'c':
      if (localKlist.name == null)
      {
        localKlist.target = CredentialsCache.getInstance();
        localKlist.name = CredentialsCache.cacheName();
      }
      else
      {
        localKlist.target = CredentialsCache.getInstance(localKlist.name);
      }
      if (localKlist.target != null)
      {
        localKlist.displayCache();
        return;
      }
      localKlist.displayMessage("Credentials cache");
      System.exit(-1);
      break;
    case 'k':
      if (localKlist.name == null)
      {
        localKlist.target = KeyTab.getInstance();
        localKlist.name = KeyTab.tabName();
      }
      else
      {
        localKlist.target = KeyTab.getInstance(localKlist.name);
      }
      if (localKlist.target != null)
      {
        localKlist.displayTab();
        return;
      }
      localKlist.displayMessage("KeyTab");
      System.exit(-1);
      break;
    default:
      if (localKlist.name != null)
      {
        localKlist.printHelp();
        System.exit(-1);
        return;
      }
      localKlist.target = CredentialsCache.getInstance();
      localKlist.name = CredentialsCache.cacheName();
      if (localKlist.target != null)
      {
        localKlist.displayCache();
        return;
      }
      localKlist.displayMessage("Credentials cache");
      System.exit(-1);
    }
  }

  void processArgs(String[] paramArrayOfString)
  {
    for (int i = 0; i < paramArrayOfString.length; ++i)
    {
      Character localCharacter;
      if ((paramArrayOfString[i].length() >= 2) && (paramArrayOfString[i].startsWith("-")))
        localCharacter = new Character(paramArrayOfString[i].charAt(1));
      switch (localCharacter.charValue())
      {
      case 'c':
        this.action = 'c';
        break;
      case 'k':
        this.action = 'k';
        break;
      case 'f':
        this.options[1] = 'f';
        break;
      case 'e':
        this.options[0] = 'e';
        break;
      case 'K':
        this.options[1] = 'K';
        break;
      case 't':
        this.options[2] = 't';
        break;
      default:
        printHelp();
        System.exit(-1);
        break label220:
        if ((!(paramArrayOfString[i].startsWith("-"))) && (i == paramArrayOfString.length - 1))
        {
          this.name = paramArrayOfString[i];
          label220: localCharacter = null;
        }
        else
        {
          printHelp();
          System.exit(-1);
        }
      }
    }
  }

  void displayTab()
  {
    KeyTab localKeyTab = (KeyTab)this.target;
    KeyTabEntry[] arrayOfKeyTabEntry = localKeyTab.getEntries();
    if (arrayOfKeyTabEntry.length == 0)
    {
      System.out.println("\nKey tab: " + this.name + ", " + " 0 entries found.\n");
    }
    else
    {
      if (arrayOfKeyTabEntry.length == 1)
        System.out.println("\nKey tab: " + this.name + ", " + arrayOfKeyTabEntry.length + " entry found.\n");
      else
        System.out.println("\nKey tab: " + this.name + ", " + arrayOfKeyTabEntry.length + " entries found.\n");
      for (int i = 0; i < arrayOfKeyTabEntry.length; ++i)
      {
        EncryptionKey localEncryptionKey;
        System.out.println("[" + (i + 1) + "] " + "Service principal: " + arrayOfKeyTabEntry[i].getService().toString());
        System.out.println("\t KVNO: " + arrayOfKeyTabEntry[i].getKey().getKeyVersionNumber());
        if (this.options[0] == 'e')
        {
          localEncryptionKey = arrayOfKeyTabEntry[i].getKey();
          System.out.println("\t Key type: " + localEncryptionKey.getEType());
        }
        if (this.options[1] == 'K')
        {
          localEncryptionKey = arrayOfKeyTabEntry[i].getKey();
          System.out.println("\t Key: " + arrayOfKeyTabEntry[i].getKeyString());
        }
        if (this.options[2] == 't')
          System.out.println("\t Time stamp: " + reformat(arrayOfKeyTabEntry[i].getTimeStamp().toDate().toString()));
      }
    }
  }

  void displayCache()
  {
    CredentialsCache localCredentialsCache = (CredentialsCache)this.target;
    Credentials[] arrayOfCredentials = localCredentialsCache.getCredsList();
    if (arrayOfCredentials == null)
    {
      System.out.println("No credentials available in the cache " + this.name);
      System.exit(-1);
    }
    System.out.println("\nCredentials cache: " + this.name);
    String str1 = localCredentialsCache.getPrimaryPrincipal().toString();
    int i = arrayOfCredentials.length;
    if (i == 1)
      System.out.println("\nDefault principal: " + str1 + ", " + arrayOfCredentials.length + " entry found.\n");
    else
      System.out.println("\nDefault principal: " + str1 + ", " + arrayOfCredentials.length + " entries found.\n");
    String str2 = null;
    String str3 = null;
    String str4 = null;
    String str5 = null;
    if (arrayOfCredentials != null)
      for (int j = 0; j < arrayOfCredentials.length; ++j)
        try
        {
          str2 = reformat(arrayOfCredentials[j].getAuthTime().toDate().toString());
          str3 = reformat(arrayOfCredentials[j].getEndTime().toDate().toString());
          str4 = arrayOfCredentials[j].getServicePrincipal().toString();
          System.out.println("[" + (j + 1) + "] " + " Service Principal:  " + str4);
          System.out.println("     Valid starting:  " + str2);
          System.out.println("     Expires:         " + str3);
          if (this.options[0] == 'e')
          {
            str5 = EType.toString(arrayOfCredentials[j].getEType());
            System.out.println("\t Encryption type: " + str5);
          }
          if (this.options[1] == 'f')
            System.out.println("\t Flags:           " + arrayOfCredentials[j].getTicketFlags().toString());
        }
        catch (RealmException localRealmException)
        {
          System.out.println("Error reading principal from the entry.");
          if (DEBUG)
            localRealmException.printStackTrace();
          System.exit(-1);
        }
    else
      System.out.println("\nNo entries found.");
  }

  void displayMessage(String paramString)
  {
    if (this.name == null)
      this.name = "";
    System.out.println(paramString + " " + this.name + " not found.");
  }

  String reformat(String paramString)
  {
    return paramString.substring(4, 7) + " " + paramString.substring(8, 10) + ", " + paramString.substring(24) + " " + paramString.substring(11, 16);
  }

  void printHelp()
  {
    System.out.println("\nUsage: klist [[-c] [-f] [-e]] [-k [-t] [-K]] [name]");
    System.out.println("   name\t name of credentials cache or  keytab with the prefix. File-based cache or keytab's prefix is FILE:.");
    System.out.println("   -c specifes that credential cache is to be listed");
    System.out.println("   -k specifies that key tab is to be listed");
    System.out.println("   options for credentials caches:");
    System.out.println("\t-f \t shows credentials flags");
    System.out.println("\t-e \t shows the encryption type");
    System.out.println("   options for keytabs:");
    System.out.println("\t-t \t shows keytab entry timestamps");
    System.out.println("\t-K \t shows keytab entry key value");
    System.out.println("\t-e \t shows keytab entry key type");
    System.out.println("\nUsage: java sun.security.krb5.tools.Klist -help for help.");
  }
}
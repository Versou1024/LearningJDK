package sun.security.krb5.internal.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import sun.security.krb5.Config;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.ktab.KeyTab;
import sun.security.krb5.internal.ktab.KeyTabEntry;

public class Ktab
{
  KeyTab table;
  char action;
  String name;
  String principal;
  char[] password = null;

  public static void main(String[] paramArrayOfString)
  {
    Ktab localKtab = new Ktab();
    if ((paramArrayOfString.length == 1) && (paramArrayOfString[0].equalsIgnoreCase("-help")))
    {
      localKtab.printHelp();
      System.exit(0);
    }
    else if ((paramArrayOfString == null) || (paramArrayOfString.length == 0))
    {
      localKtab.action = 'l';
    }
    else
    {
      localKtab.processArgs(paramArrayOfString);
    }
    try
    {
      if (localKtab.name == null)
      {
        localKtab.table = KeyTab.getInstance();
        if (localKtab.table == null)
          if (localKtab.action == 'a')
          {
            localKtab.table = KeyTab.create();
          }
          else
          {
            System.out.println("No default key table exists.");
            System.exit(-1);
          }
      }
      else
      {
        if ((localKtab.action != 'a') && (!(new File(localKtab.name).exists())))
        {
          System.out.println("Key table " + localKtab.name + " does not exist.");
          System.exit(-1);
        }
        else
        {
          localKtab.table = KeyTab.getInstance(localKtab.name);
        }
        if (localKtab.table == null)
          if (localKtab.action == 'a')
          {
            localKtab.table = KeyTab.create(localKtab.name);
          }
          else
          {
            System.out.println("The format of key table " + localKtab.name + " is incorrect.");
            System.exit(-1);
          }
      }
    }
    catch (RealmException localRealmException)
    {
      System.err.println("Error loading key table.");
      System.exit(-1);
    }
    catch (IOException localIOException)
    {
      System.err.println("Error loading key table.");
      System.exit(-1);
    }
    switch (localKtab.action)
    {
    case 'l':
      localKtab.listKt();
      break;
    case 'a':
      localKtab.addEntry();
      break;
    case 'd':
      localKtab.deleteEntry();
      break;
    default:
      localKtab.printHelp();
      System.exit(-1);
    }
  }

  void processArgs(String[] paramArrayOfString)
  {
    Character localCharacter = null;
    for (int i = 0; i < paramArrayOfString.length; ++i)
    {
      if ((paramArrayOfString[i].length() == 2) && (paramArrayOfString[i].startsWith("-")))
      {
        localCharacter = new Character(paramArrayOfString[i].charAt(1));
      }
      else
      {
        printHelp();
        System.exit(-1);
      }
      switch (localCharacter.charValue())
      {
      case 'L':
      case 'l':
        this.action = 'l';
        break;
      case 'A':
      case 'a':
        this.action = 'a';
        if ((++i < paramArrayOfString.length) && (!(paramArrayOfString[i].startsWith("-"))))
        {
          this.principal = paramArrayOfString[i];
        }
        else
        {
          System.out.println("Please specify the principal name after -a option.");
          printHelp();
          System.exit(-1);
        }
        if ((i + 1 < paramArrayOfString.length) && (!(paramArrayOfString[(i + 1)].startsWith("-"))))
        {
          this.password = paramArrayOfString[(i + 1)].toCharArray();
          ++i;
        }
        else
        {
          this.password = null;
        }
        break;
      case 'D':
      case 'd':
        this.action = 'd';
        if ((++i < paramArrayOfString.length) && (!(paramArrayOfString[i].startsWith("-"))))
        {
          this.principal = paramArrayOfString[i];
        }
        else
        {
          System.out.println("Please specify the principalname of the entry you want to  delete after -d option.");
          printHelp();
          System.exit(-1);
        }
        break;
      case 'K':
      case 'k':
        if ((++i < paramArrayOfString.length) && (!(paramArrayOfString[i].startsWith("-"))))
        {
          if ((paramArrayOfString[i].length() >= 5) && (paramArrayOfString[i].substring(0, 5).equalsIgnoreCase("FILE:")))
            this.name = paramArrayOfString[i].substring(5);
          else
            this.name = paramArrayOfString[i];
        }
        else
        {
          System.out.println("Please specify the keytab file name and location after -k option");
          printHelp();
          System.exit(-1);
        }
        break;
      default:
        printHelp();
        System.exit(-1);
      }
    }
  }

  void addEntry()
  {
    PrincipalName localPrincipalName = null;
    try
    {
      localPrincipalName = new PrincipalName(this.principal);
      if (localPrincipalName.getRealm() == null)
        localPrincipalName.setRealm(Config.getInstance().getDefaultRealm());
    }
    catch (KrbException localKrbException1)
    {
      System.err.println("Failed to add " + this.principal + " to keytab.");
      localKrbException1.printStackTrace();
      System.exit(-1);
    }
    if (this.password == null)
      try
      {
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Password for " + localPrincipalName.toString() + ":");
        System.out.flush();
        this.password = localBufferedReader.readLine().toCharArray();
      }
      catch (IOException localIOException1)
      {
        System.err.println("Failed to read the password.");
        localIOException1.printStackTrace();
        System.exit(-1);
      }
    try
    {
      this.table.addEntry(localPrincipalName, this.password);
      Arrays.fill(this.password, '0');
      this.table.save();
      System.out.println("Done!");
      System.out.println("Service key for " + this.principal + " is saved in " + KeyTab.tabName());
    }
    catch (KrbException localKrbException2)
    {
      System.err.println("Failed to add " + this.principal + " to keytab.");
      localKrbException2.printStackTrace();
      System.exit(-1);
    }
    catch (IOException localIOException2)
    {
      System.err.println("Failed to save new entry.");
      localIOException2.printStackTrace();
      System.exit(-1);
    }
  }

  void listKt()
  {
    System.out.println("Keytab name: " + KeyTab.tabName());
    KeyTabEntry[] arrayOfKeyTabEntry = this.table.getEntries();
    if ((arrayOfKeyTabEntry != null) && (arrayOfKeyTabEntry.length > 0))
    {
      System.out.println("KVNO    Principal");
      for (int j = 0; j < arrayOfKeyTabEntry.length; ++j)
      {
        int i = arrayOfKeyTabEntry[j].getKey().getKeyVersionNumber().intValue();
        String str = arrayOfKeyTabEntry[j].getService().toString();
        if (j == 0)
        {
          StringBuffer localStringBuffer = new StringBuffer();
          for (int k = 0; k < 9 + str.length(); ++k)
            localStringBuffer.append("-");
          System.out.println(localStringBuffer.toString());
        }
        System.out.println("  " + i + "     " + str);
      }
    }
    else
    {
      System.out.println("0 entry.");
    }
  }

  void deleteEntry()
  {
    PrincipalName localPrincipalName = null;
    try
    {
      localPrincipalName = new PrincipalName(this.principal);
      if (localPrincipalName.getRealm() == null)
        localPrincipalName.setRealm(Config.getInstance().getDefaultRealm());
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("Are you sure you want to  delete service key for " + localPrincipalName.toString() + " in " + KeyTab.tabName() + "?(Y/N) :");
      System.out.flush();
      String str = localBufferedReader.readLine();
      if (!(str.equalsIgnoreCase("Y")))
      {
        if (str.equalsIgnoreCase("Yes"))
          break label134:
        label134: System.exit(0);
      }
    }
    catch (KrbException localKrbException)
    {
      System.err.println("Error occured while deleting the entry. Deletion failed.");
      localKrbException.printStackTrace();
      System.exit(-1);
    }
    catch (IOException localIOException1)
    {
      System.err.println("Error occured while deleting the entry.  Deletion failed.");
      localIOException1.printStackTrace();
      System.exit(-1);
    }
    this.table.deleteEntry(localPrincipalName);
    try
    {
      this.table.save();
    }
    catch (IOException localIOException2)
    {
      System.err.println("Error occurs while saving the keytab.Deletion fails.");
      localIOException2.printStackTrace();
      System.exit(-1);
    }
    System.out.println("Done!");
  }

  void printHelp()
  {
    System.out.println("\nUsage: ktab <options>");
    System.out.println("available options to Ktab:");
    System.out.println("-l\t\t\t\tlist the keytab name and entries");
    System.out.println("-a <principal name> (<password>)add an entry to the keytab");
    System.out.println("-d <principal name>\t\tdelete an entry from the keytab");
    System.out.println("-k <keytab name>\t\tspecify keytab name and  path with prefix FILE:");
  }
}
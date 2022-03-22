package sun.misc;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.PropertyPermission;
import sun.security.util.SecurityConstants;

class PathPermissions extends PermissionCollection
{
  private static final long serialVersionUID = 8133287259134945693L;
  private File[] path;
  private Permissions perms;
  URL codeBase;

  PathPermissions(File[] paramArrayOfFile)
  {
    this.path = paramArrayOfFile;
    this.perms = null;
    this.codeBase = null;
  }

  URL getCodeBase()
  {
    return this.codeBase;
  }

  public void add(Permission paramPermission)
  {
    throw new SecurityException("attempt to add a permission");
  }

  private synchronized void init()
  {
    if (this.perms != null)
      return;
    this.perms = new Permissions();
    this.perms.add(SecurityConstants.CREATE_CLASSLOADER_PERMISSION);
    this.perms.add(new PropertyPermission("java.*", "read"));
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        for (int i = 0; i < PathPermissions.access$000(this.this$0).length; ++i)
        {
          String str;
          File localFile = PathPermissions.access$000(this.this$0)[i];
          try
          {
            str = localFile.getCanonicalPath();
          }
          catch (IOException localIOException)
          {
            str = localFile.getAbsolutePath();
          }
          if (i == 0)
            this.this$0.codeBase = Launcher.getFileURL(new File(str));
          if (localFile.isDirectory())
          {
            if (str.endsWith(File.separator))
              PathPermissions.access$100(this.this$0).add(new FilePermission(str + "-", "read"));
            else
              PathPermissions.access$100(this.this$0).add(new FilePermission(str + File.separator + "-", "read"));
          }
          else
          {
            int j = str.lastIndexOf(File.separatorChar);
            if (j != -1)
            {
              str = str.substring(0, j + 1) + "-";
              PathPermissions.access$100(this.this$0).add(new FilePermission(str, "read"));
            }
          }
        }
        return null;
      }
    });
  }

  public boolean implies(Permission paramPermission)
  {
    if (this.perms == null)
      init();
    return this.perms.implies(paramPermission);
  }

  public Enumeration elements()
  {
    if (this.perms == null)
      init();
    synchronized (this.perms)
    {
      return this.perms.elements();
    }
  }

  public String toString()
  {
    if (this.perms == null)
      init();
    return this.perms.toString();
  }
}
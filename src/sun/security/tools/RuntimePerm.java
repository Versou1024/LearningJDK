package sun.security.tools;

import java.util.ResourceBundle;

class RuntimePerm extends Perm
{
  public RuntimePerm()
  {
    super("RuntimePermission", "java.lang.RuntimePermission", new String[] { "createClassLoader", "getClassLoader", "setContextClassLoader", "enableContextClassLoaderOverride", "setSecurityManage", "createSecurityManager", "getenv.<" + PolicyTool.rb.getString("environment variable name") + ">", "exitVM", "shutdownHooks", "setFactory", "setIO", "modifyThread", "stopThread", "modifyThreadGroup", "getProtectionDomain", "readFileDescriptor", "writeFileDescriptor", "loadLibrary.<" + PolicyTool.rb.getString("library name") + ">", "accessClassInPackage.<" + PolicyTool.rb.getString("package name") + ">", "defineClassInPackage.<" + PolicyTool.rb.getString("package name") + ">", "accessDeclaredMembers", "queuePrintJob", "getStackTrace", "setDefaultUncaughtExceptionHandler", "preferences", "usePolicy" }, null);
  }
}
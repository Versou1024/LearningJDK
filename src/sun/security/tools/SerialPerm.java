package sun.security.tools;

class SerialPerm extends Perm
{
  public SerialPerm()
  {
    super("SerializablePermission", "java.io.SerializablePermission", new String[] { "enableSubclassImplementation", "enableSubstitution" }, null);
  }
}
package sun.management;

import com.sun.management.VMOption;
import com.sun.management.VMOption.Origin;
import java.util.Arrays;
import java.util.List;

class Flag
{
  private String name;
  private Object value;
  private VMOption.Origin origin;
  private boolean writeable;
  private boolean external;

  Flag(String paramString, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, VMOption.Origin paramOrigin)
  {
    this.name = paramString;
    this.value = paramObject;
    this.origin = paramOrigin;
    this.writeable = paramBoolean1;
    this.external = paramBoolean2;
  }

  Object getValue()
  {
    return this.value;
  }

  boolean isWriteable()
  {
    return this.writeable;
  }

  boolean isExternal()
  {
    return this.external;
  }

  VMOption getVMOption()
  {
    String str = (this.value == null) ? "" : this.value.toString();
    return new VMOption(this.name, str, this.writeable, this.origin);
  }

  static Flag getFlag(String paramString)
  {
    Flag[] arrayOfFlag = new Flag[1];
    String[] arrayOfString = new String[1];
    arrayOfString[0] = paramString;
    int i = getFlags(arrayOfString, arrayOfFlag, 1);
    if (i == 1)
      return arrayOfFlag[0];
    return null;
  }

  static List<Flag> getAllFlags()
  {
    int i = getInternalFlagCount();
    Flag[] arrayOfFlag = new Flag[i];
    int j = getFlags(null, arrayOfFlag, i);
    return Arrays.asList(arrayOfFlag);
  }

  private static native String[] getAllFlagNames();

  private static native int getFlags(String[] paramArrayOfString, Flag[] paramArrayOfFlag, int paramInt);

  private static native int getInternalFlagCount();

  static synchronized native void setLongValue(String paramString, long paramLong);

  static synchronized native void setBooleanValue(String paramString, boolean paramBoolean);

  static synchronized native void setStringValue(String paramString1, String paramString2);

  private static native void initialize();

  static
  {
    initialize();
  }
}
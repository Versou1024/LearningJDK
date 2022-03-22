package sun.security.action;

import java.security.PrivilegedAction;

public class GetLongAction
  implements PrivilegedAction<Long>
{
  private String theProp;
  private long defaultVal;
  private boolean defaultSet = false;

  public GetLongAction(String paramString)
  {
    this.theProp = paramString;
  }

  public GetLongAction(String paramString, long paramLong)
  {
    this.theProp = paramString;
    this.defaultVal = paramLong;
    this.defaultSet = true;
  }

  public Long run()
  {
    Long localLong = Long.getLong(this.theProp);
    if ((localLong == null) && (this.defaultSet))
      return new Long(this.defaultVal);
    return localLong;
  }
}
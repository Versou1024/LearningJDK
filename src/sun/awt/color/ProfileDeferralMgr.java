package sun.awt.color;

import java.util.Vector;

public class ProfileDeferralMgr
{
  public static boolean deferring = true;
  private static Vector aVector;

  public static void registerDeferral(ProfileActivator paramProfileActivator)
  {
    if (!(deferring))
      return;
    if (aVector == null)
      aVector = new Vector(3, 3);
    aVector.addElement(paramProfileActivator);
  }

  public static void unregisterDeferral(ProfileActivator paramProfileActivator)
  {
    if (!(deferring))
      return;
    if (aVector == null)
      return;
    aVector.removeElement(paramProfileActivator);
  }

  public static void activateProfiles()
  {
    deferring = false;
    if (aVector == null)
      return;
    int j = aVector.size();
    for (int i = 0; i < j; ++i)
      ((ProfileActivator)aVector.get(i)).activate();
    aVector.removeAllElements();
    aVector = null;
  }
}
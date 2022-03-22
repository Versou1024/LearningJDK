package sun.awt;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.PrintStream;
import java.util.StringTokenizer;

public class TracedEventQueue extends EventQueue
{
  static boolean trace = false;
  static int[] suppressedIDs = null;

  public void postEvent(AWTEvent paramAWTEvent)
  {
    int i = 1;
    int j = paramAWTEvent.getID();
    for (int k = 0; k < suppressedIDs.length; ++k)
      if (j == suppressedIDs[k])
      {
        i = 0;
        break;
      }
    if (i != 0)
      System.out.println(Thread.currentThread().getName() + ": " + paramAWTEvent);
    super.postEvent(paramAWTEvent);
  }

  static
  {
    String str1 = Toolkit.getProperty("AWT.IgnoreEventIDs", "");
    if (str1.length() > 0)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(str1, ",");
      int i = localStringTokenizer.countTokens();
      suppressedIDs = new int[i];
      for (int j = 0; j < i; ++j)
      {
        String str2 = localStringTokenizer.nextToken();
        try
        {
          suppressedIDs[j] = Integer.parseInt(str2);
        }
        catch (NumberFormatException localNumberFormatException)
        {
          System.err.println("Bad ID listed in AWT.IgnoreEventIDs in awt.properties: \"" + str2 + "\" -- skipped");
          suppressedIDs[j] = 0;
        }
      }
    }
    else
    {
      suppressedIDs = new int[0];
    }
  }
}
package sun.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;

public abstract class AccumulativeRunnable<T>
  implements Runnable
{
  private List<T> arguments = null;

  protected abstract void run(List<T> paramList);

  public final void run()
  {
    run(flush());
  }

  public final synchronized void add(T[] paramArrayOfT)
  {
    int i = 1;
    if (this.arguments == null)
    {
      i = 0;
      this.arguments = new ArrayList();
    }
    Collections.addAll(this.arguments, paramArrayOfT);
    if (i == 0)
      submit();
  }

  protected void submit()
  {
    SwingUtilities.invokeLater(this);
  }

  private final synchronized List<T> flush()
  {
    List localList = this.arguments;
    this.arguments = null;
    return localList;
  }
}
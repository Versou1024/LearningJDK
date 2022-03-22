package sun.misc;

import java.io.PrintStream;

public class RegexpPool
{
  private RegexpNode prefixMachine = new RegexpNode();
  private RegexpNode suffixMachine = new RegexpNode();
  private static final int BIG = 2147483647;
  private int lastDepth = 2147483647;

  public void add(String paramString, Object paramObject)
    throws sun.misc.REException
  {
    add(paramString, paramObject, false);
  }

  public void replace(String paramString, Object paramObject)
  {
    try
    {
      add(paramString, paramObject, true);
    }
    catch (Exception localException)
    {
    }
  }

  public Object delete(String paramString)
  {
    Object localObject = null;
    RegexpNode localRegexpNode1 = this.prefixMachine;
    RegexpNode localRegexpNode2 = localRegexpNode1;
    int i = paramString.length() - 1;
    int k = 1;
    if ((!(paramString.startsWith("*"))) || (!(paramString.endsWith("*"))))
      ++i;
    if (i <= 0)
      return null;
    for (int j = 0; localRegexpNode1 != null; ++j)
    {
      if ((localRegexpNode1.result != null) && (localRegexpNode1.depth < 2147483647) && (((!(localRegexpNode1.exact)) || (j == i))))
        localRegexpNode2 = localRegexpNode1;
      if (j >= i)
        break;
      localRegexpNode1 = localRegexpNode1.find(paramString.charAt(j));
    }
    localRegexpNode1 = this.suffixMachine;
    j = i;
    while ((--j >= 0) && (localRegexpNode1 != null))
    {
      if ((localRegexpNode1.result != null) && (localRegexpNode1.depth < 2147483647))
      {
        k = 0;
        localRegexpNode2 = localRegexpNode1;
      }
      localRegexpNode1 = localRegexpNode1.find(paramString.charAt(j));
    }
    if (k != 0)
    {
      if (paramString.equals(localRegexpNode2.re))
      {
        localObject = localRegexpNode2.result;
        localRegexpNode2.result = null;
      }
    }
    else if (paramString.equals(localRegexpNode2.re))
    {
      localObject = localRegexpNode2.result;
      localRegexpNode2.result = null;
    }
    return localObject;
  }

  public Object match(String paramString)
  {
    return matchAfter(paramString, 2147483647);
  }

  public Object matchNext(String paramString)
  {
    return matchAfter(paramString, this.lastDepth);
  }

  private void add(String paramString, Object paramObject, boolean paramBoolean)
    throws sun.misc.REException
  {
    int i = paramString.length();
    if (paramString.charAt(0) == '*')
    {
      localRegexpNode = this.suffixMachine;
      while (true)
      {
        if (i <= 1)
          break label114;
        localRegexpNode = localRegexpNode.add(paramString.charAt(--i));
      }
    }
    int j = 0;
    if (paramString.charAt(i - 1) == '*')
      --i;
    else
      j = 1;
    RegexpNode localRegexpNode = this.prefixMachine;
    for (int k = 0; k < i; ++k)
      localRegexpNode = localRegexpNode.add(paramString.charAt(k));
    localRegexpNode.exact = j;
    if ((localRegexpNode.result != null) && (!(paramBoolean)))
      label114: throw new sun.misc.REException(paramString + " is a duplicate");
    localRegexpNode.re = paramString;
    localRegexpNode.result = paramObject;
  }

  private Object matchAfter(String paramString, int paramInt)
  {
    RegexpNode localRegexpNode1 = this.prefixMachine;
    RegexpNode localRegexpNode2 = localRegexpNode1;
    int i = 0;
    int j = 0;
    int k = paramString.length();
    if (k <= 0)
      return null;
    for (int l = 0; localRegexpNode1 != null; ++l)
    {
      if ((localRegexpNode1.result != null) && (localRegexpNode1.depth < paramInt) && (((!(localRegexpNode1.exact)) || (l == k))))
      {
        this.lastDepth = localRegexpNode1.depth;
        localRegexpNode2 = localRegexpNode1;
        i = l;
        j = k;
      }
      if (l >= k)
        break;
      localRegexpNode1 = localRegexpNode1.find(paramString.charAt(l));
    }
    localRegexpNode1 = this.suffixMachine;
    l = k;
    while ((--l >= 0) && (localRegexpNode1 != null))
    {
      if ((localRegexpNode1.result != null) && (localRegexpNode1.depth < paramInt))
      {
        this.lastDepth = localRegexpNode1.depth;
        localRegexpNode2 = localRegexpNode1;
        i = 0;
        j = l + 1;
      }
      localRegexpNode1 = localRegexpNode1.find(paramString.charAt(l));
    }
    Object localObject = localRegexpNode2.result;
    if ((localObject != null) && (localObject instanceof RegexpTarget))
      localObject = ((RegexpTarget)localObject).found(paramString.substring(i, j));
    return localObject;
  }

  public void reset()
  {
    this.lastDepth = 2147483647;
  }

  public void print(PrintStream paramPrintStream)
  {
    paramPrintStream.print("Regexp pool:\n");
    if (this.suffixMachine.firstchild != null)
    {
      paramPrintStream.print(" Suffix machine: ");
      this.suffixMachine.firstchild.print(paramPrintStream);
      paramPrintStream.print("\n");
    }
    if (this.prefixMachine.firstchild != null)
    {
      paramPrintStream.print(" Prefix machine: ");
      this.prefixMachine.firstchild.print(paramPrintStream);
      paramPrintStream.print("\n");
    }
  }
}
package sun.misc;

import java.io.PrintStream;

class RegexpNode
{
  char c;
  RegexpNode firstchild;
  RegexpNode nextsibling;
  int depth;
  boolean exact;
  Object result;
  String re = null;

  RegexpNode()
  {
    this.c = '#';
    this.depth = 0;
  }

  RegexpNode(char paramChar, int paramInt)
  {
    this.c = paramChar;
    this.depth = paramInt;
  }

  RegexpNode add(char paramChar)
  {
    RegexpNode localRegexpNode = this.firstchild;
    if (localRegexpNode == null)
    {
      localRegexpNode = new RegexpNode(paramChar, this.depth + 1);
    }
    else
    {
      while (localRegexpNode != null)
      {
        if (localRegexpNode.c == paramChar)
          return localRegexpNode;
        localRegexpNode = localRegexpNode.nextsibling;
      }
      localRegexpNode = new RegexpNode(paramChar, this.depth + 1);
      localRegexpNode.nextsibling = this.firstchild;
    }
    this.firstchild = localRegexpNode;
    return localRegexpNode;
  }

  RegexpNode find(char paramChar)
  {
    for (RegexpNode localRegexpNode = this.firstchild; localRegexpNode != null; localRegexpNode = localRegexpNode.nextsibling)
      if (localRegexpNode.c == paramChar)
        return localRegexpNode;
    return null;
  }

  void print(PrintStream paramPrintStream)
  {
    if (this.nextsibling != null)
    {
      RegexpNode localRegexpNode = this;
      paramPrintStream.print("(");
      while (localRegexpNode != null)
      {
        paramPrintStream.write(localRegexpNode.c);
        if (localRegexpNode.firstchild != null)
          localRegexpNode.firstchild.print(paramPrintStream);
        localRegexpNode = localRegexpNode.nextsibling;
        paramPrintStream.write((localRegexpNode != null) ? 124 : 41);
      }
    }
    else
    {
      paramPrintStream.write(this.c);
      if (this.firstchild != null)
        this.firstchild.print(paramPrintStream);
    }
  }
}
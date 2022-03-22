package sun.org.mozilla.javascript.internal;

public class Node
{
  public static final int FUNCTION_PROP = 1;
  public static final int LOCAL_PROP = 2;
  public static final int LOCAL_BLOCK_PROP = 3;
  public static final int REGEXP_PROP = 4;
  public static final int CASEARRAY_PROP = 5;
  public static final int TARGETBLOCK_PROP = 6;
  public static final int VARIABLE_PROP = 7;
  public static final int ISNUMBER_PROP = 8;
  public static final int DIRECTCALL_PROP = 9;
  public static final int SPECIALCALL_PROP = 10;
  public static final int SKIP_INDEXES_PROP = 11;
  public static final int OBJECT_IDS_PROP = 12;
  public static final int INCRDECR_PROP = 13;
  public static final int CATCH_SCOPE_PROP = 14;
  public static final int LABEL_ID_PROP = 15;
  public static final int MEMBER_TYPE_PROP = 16;
  public static final int NAME_PROP = 17;
  public static final int LAST_PROP = 17;
  public static final int BOTH = 0;
  public static final int LEFT = 1;
  public static final int RIGHT = 2;
  public static final int NON_SPECIALCALL = 0;
  public static final int SPECIALCALL_EVAL = 1;
  public static final int SPECIALCALL_WITH = 2;
  public static final int DECR_FLAG = 1;
  public static final int POST_FLAG = 2;
  public static final int PROPERTY_FLAG = 1;
  public static final int ATTRIBUTE_FLAG = 2;
  public static final int DESCENDANTS_FLAG = 4;
  int type;
  Node next;
  private Node first;
  private Node last;
  private int lineno;
  private PropListItem propListHead;

  public Node(int paramInt)
  {
    this.lineno = -1;
    this.type = paramInt;
  }

  public Node(int paramInt, Node paramNode)
  {
    this.lineno = -1;
    this.type = paramInt;
    this.first = (this.last = paramNode);
    paramNode.next = null;
  }

  public Node(int paramInt, Node paramNode1, Node paramNode2)
  {
    this.lineno = -1;
    this.type = paramInt;
    this.first = paramNode1;
    this.last = paramNode2;
    paramNode1.next = paramNode2;
    paramNode2.next = null;
  }

  public Node(int paramInt, Node paramNode1, Node paramNode2, Node paramNode3)
  {
    this.lineno = -1;
    this.type = paramInt;
    this.first = paramNode1;
    this.last = paramNode3;
    paramNode1.next = paramNode2;
    paramNode2.next = paramNode3;
    paramNode3.next = null;
  }

  public Node(int paramInt1, int paramInt2)
  {
    this.lineno = -1;
    this.type = paramInt1;
    this.lineno = paramInt2;
  }

  public Node(int paramInt1, Node paramNode, int paramInt2)
  {
    this(paramInt1, paramNode);
    this.lineno = paramInt2;
  }

  public Node(int paramInt1, Node paramNode1, Node paramNode2, int paramInt2)
  {
    this(paramInt1, paramNode1, paramNode2);
    this.lineno = paramInt2;
  }

  public Node(int paramInt1, Node paramNode1, Node paramNode2, Node paramNode3, int paramInt2)
  {
    this(paramInt1, paramNode1, paramNode2, paramNode3);
    this.lineno = paramInt2;
  }

  public static Node newNumber(double paramDouble)
  {
    return new NumberNode(paramDouble);
  }

  public static Node newString(String paramString)
  {
    return new StringNode(40, paramString);
  }

  public static Node newString(int paramInt, String paramString)
  {
    return new StringNode(paramInt, paramString);
  }

  public int getType()
  {
    return this.type;
  }

  public void setType(int paramInt)
  {
    this.type = paramInt;
  }

  public boolean hasChildren()
  {
    return (this.first != null);
  }

  public Node getFirstChild()
  {
    return this.first;
  }

  public Node getLastChild()
  {
    return this.last;
  }

  public Node getNext()
  {
    return this.next;
  }

  public Node getChildBefore(Node paramNode)
  {
    if (paramNode == this.first)
      return null;
    Node localNode = this.first;
    do
    {
      if (localNode.next == paramNode)
        break label42;
      localNode = localNode.next;
    }
    while (localNode != null);
    throw new RuntimeException("node is not a child");
    label42: return localNode;
  }

  public Node getLastSibling()
  {
    for (Node localNode = this; localNode.next != null; localNode = localNode.next);
    return localNode;
  }

  public void addChildToFront(Node paramNode)
  {
    paramNode.next = this.first;
    this.first = paramNode;
    if (this.last == null)
      this.last = paramNode;
  }

  public void addChildToBack(Node paramNode)
  {
    paramNode.next = null;
    if (this.last == null)
    {
      this.first = (this.last = paramNode);
      return;
    }
    this.last.next = paramNode;
    this.last = paramNode;
  }

  public void addChildrenToFront(Node paramNode)
  {
    Node localNode = paramNode.getLastSibling();
    localNode.next = this.first;
    this.first = paramNode;
    if (this.last == null)
      this.last = localNode;
  }

  public void addChildrenToBack(Node paramNode)
  {
    if (this.last != null)
      this.last.next = paramNode;
    this.last = paramNode.getLastSibling();
    if (this.first == null)
      this.first = paramNode;
  }

  public void addChildBefore(Node paramNode1, Node paramNode2)
  {
    if (paramNode1.next != null)
      throw new RuntimeException("newChild had siblings in addChildBefore");
    if (this.first == paramNode2)
    {
      paramNode1.next = this.first;
      this.first = paramNode1;
      return;
    }
    Node localNode = getChildBefore(paramNode2);
    addChildAfter(paramNode1, localNode);
  }

  public void addChildAfter(Node paramNode1, Node paramNode2)
  {
    if (paramNode1.next != null)
      throw new RuntimeException("newChild had siblings in addChildAfter");
    paramNode1.next = paramNode2.next;
    paramNode2.next = paramNode1;
    if (this.last == paramNode2)
      this.last = paramNode1;
  }

  public void removeChild(Node paramNode)
  {
    Node localNode = getChildBefore(paramNode);
    if (localNode == null)
      this.first = this.first.next;
    else
      localNode.next = paramNode.next;
    if (paramNode == this.last)
      this.last = localNode;
    paramNode.next = null;
  }

  public void replaceChild(Node paramNode1, Node paramNode2)
  {
    paramNode2.next = paramNode1.next;
    if (paramNode1 == this.first)
    {
      this.first = paramNode2;
    }
    else
    {
      Node localNode = getChildBefore(paramNode1);
      localNode.next = paramNode2;
    }
    if (paramNode1 == this.last)
      this.last = paramNode2;
    paramNode1.next = null;
  }

  public void replaceChildAfter(Node paramNode1, Node paramNode2)
  {
    Node localNode = paramNode1.next;
    paramNode2.next = localNode.next;
    paramNode1.next = paramNode2;
    if (localNode == this.last)
      this.last = paramNode2;
    localNode.next = null;
  }

  private static final String propToString(int paramInt)
  {
    return null;
  }

  private PropListItem lookupProperty(int paramInt)
  {
    for (PropListItem localPropListItem = this.propListHead; (localPropListItem != null) && (paramInt != localPropListItem.type); localPropListItem = localPropListItem.next);
    return localPropListItem;
  }

  private PropListItem ensureProperty(int paramInt)
  {
    PropListItem localPropListItem = lookupProperty(paramInt);
    if (localPropListItem == null)
    {
      localPropListItem = new PropListItem(null);
      localPropListItem.type = paramInt;
      localPropListItem.next = this.propListHead;
      this.propListHead = localPropListItem;
    }
    return localPropListItem;
  }

  public void removeProp(int paramInt)
  {
    PropListItem localPropListItem1 = this.propListHead;
    if (localPropListItem1 != null)
    {
      PropListItem localPropListItem2 = null;
      do
      {
        if (localPropListItem1.type == paramInt)
          break label31;
        localPropListItem2 = localPropListItem1;
        localPropListItem1 = localPropListItem1.next;
      }
      while (localPropListItem1 != null);
      return;
      if (localPropListItem2 == null)
        label31: this.propListHead = localPropListItem1.next;
      else
        localPropListItem2.next = localPropListItem1.next;
    }
  }

  public Object getProp(int paramInt)
  {
    PropListItem localPropListItem = lookupProperty(paramInt);
    if (localPropListItem == null)
      return null;
    return localPropListItem.objectValue;
  }

  public int getIntProp(int paramInt1, int paramInt2)
  {
    PropListItem localPropListItem = lookupProperty(paramInt1);
    if (localPropListItem == null)
      return paramInt2;
    return localPropListItem.intValue;
  }

  public int getExistingIntProp(int paramInt)
  {
    PropListItem localPropListItem = lookupProperty(paramInt);
    if (localPropListItem == null)
      Kit.codeBug();
    return localPropListItem.intValue;
  }

  public void putProp(int paramInt, Object paramObject)
  {
    if (paramObject == null)
    {
      removeProp(paramInt);
    }
    else
    {
      PropListItem localPropListItem = ensureProperty(paramInt);
      localPropListItem.objectValue = paramObject;
    }
  }

  public void putIntProp(int paramInt1, int paramInt2)
  {
    PropListItem localPropListItem = ensureProperty(paramInt1);
    localPropListItem.intValue = paramInt2;
  }

  public int getLineno()
  {
    return this.lineno;
  }

  public final double getDouble()
  {
    return ((NumberNode)this).number;
  }

  public final void setDouble(double paramDouble)
  {
    ((NumberNode)this).number = paramDouble;
  }

  public final String getString()
  {
    return ((StringNode)this).str;
  }

  public final void setString(String paramString)
  {
    if (paramString == null)
      Kit.codeBug();
    ((StringNode)this).str = paramString;
  }

  public static Node newTarget()
  {
    return new Node(127);
  }

  public final int labelId()
  {
    if (this.type != 127)
      Kit.codeBug();
    return getIntProp(15, -1);
  }

  public void labelId(int paramInt)
  {
    if (this.type != 127)
      Kit.codeBug();
    putIntProp(15, paramInt);
  }

  public String toString()
  {
    return String.valueOf(this.type);
  }

  private void toString(ObjToIntMap paramObjToIntMap, StringBuffer paramStringBuffer)
  {
  }

  public String toStringTree(ScriptOrFnNode paramScriptOrFnNode)
  {
    return null;
  }

  private static void toStringTreeHelper(ScriptOrFnNode paramScriptOrFnNode, Node paramNode, ObjToIntMap paramObjToIntMap, int paramInt, StringBuffer paramStringBuffer)
  {
  }

  private static void generatePrintIds(Node paramNode, ObjToIntMap paramObjToIntMap)
  {
  }

  private static void appendPrintId(Node paramNode, ObjToIntMap paramObjToIntMap, StringBuffer paramStringBuffer)
  {
  }

  public static class Jump extends Node
  {
    public Node target;
    private Node target2;
    private Jump jumpNode;

    public Jump(int paramInt)
    {
      super(paramInt);
    }

    Jump(int paramInt1, int paramInt2)
    {
      super(paramInt1, paramInt2);
    }

    Jump(int paramInt, Node paramNode)
    {
      super(paramInt, paramNode);
    }

    Jump(int paramInt1, Node paramNode, int paramInt2)
    {
      super(paramInt1, paramNode, paramInt2);
    }

    public final Jump getJumpStatement()
    {
      if ((this.type != 116) && (this.type != 117))
        Kit.codeBug();
      return this.jumpNode;
    }

    public final void setJumpStatement(Jump paramJump)
    {
      if ((this.type != 116) && (this.type != 117))
        Kit.codeBug();
      if (paramJump == null)
        Kit.codeBug();
      if (this.jumpNode != null)
        Kit.codeBug();
      this.jumpNode = paramJump;
    }

    public final Node getDefault()
    {
      if (this.type != 110)
        Kit.codeBug();
      return this.target2;
    }

    public final void setDefault(Node paramNode)
    {
      if (this.type != 110)
        Kit.codeBug();
      if (paramNode.type != 127)
        Kit.codeBug();
      if (this.target2 != null)
        Kit.codeBug();
      this.target2 = paramNode;
    }

    public final Node getFinally()
    {
      if (this.type != 77)
        Kit.codeBug();
      return this.target2;
    }

    public final void setFinally(Node paramNode)
    {
      if (this.type != 77)
        Kit.codeBug();
      if (paramNode.type != 127)
        Kit.codeBug();
      if (this.target2 != null)
        Kit.codeBug();
      this.target2 = paramNode;
    }

    public final Jump getLoop()
    {
      if (this.type != 126)
        Kit.codeBug();
      return this.jumpNode;
    }

    public final void setLoop(Jump paramJump)
    {
      if (this.type != 126)
        Kit.codeBug();
      if (paramJump == null)
        Kit.codeBug();
      if (this.jumpNode != null)
        Kit.codeBug();
      this.jumpNode = paramJump;
    }

    public final Node getContinue()
    {
      if (this.type != 128)
        Kit.codeBug();
      return this.target2;
    }

    public final void setContinue(Node paramNode)
    {
      if (this.type != 128)
        Kit.codeBug();
      if (paramNode.type != 127)
        Kit.codeBug();
      if (this.target2 != null)
        Kit.codeBug();
      this.target2 = paramNode;
    }
  }

  private static class NumberNode extends Node
  {
    double number;

    NumberNode(double paramDouble)
    {
      super(39);
      this.number = paramDouble;
    }
  }

  private static class PropListItem
  {
    PropListItem next;
    int type;
    int intValue;
    Object objectValue;
  }

  private static class StringNode extends Node
  {
    String str;

    StringNode(int paramInt, String paramString)
    {
      super(paramInt);
      this.str = paramString;
    }
  }
}
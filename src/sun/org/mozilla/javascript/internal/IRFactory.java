package sun.org.mozilla.javascript.internal;

import java.util.Hashtable;

final class IRFactory
{
  private Parser parser;
  private static final int LOOP_DO_WHILE = 0;
  private static final int LOOP_WHILE = 1;
  private static final int LOOP_FOR = 2;
  private static final int ALWAYS_TRUE_BOOLEAN = 1;
  private static final int ALWAYS_FALSE_BOOLEAN = -1;

  IRFactory(Parser paramParser)
  {
    this.parser = paramParser;
  }

  ScriptOrFnNode createScript()
  {
    return new ScriptOrFnNode(132);
  }

  void initScript(ScriptOrFnNode paramScriptOrFnNode, Node paramNode)
  {
    Node localNode = paramNode.getFirstChild();
    if (localNode != null)
      paramScriptOrFnNode.addChildrenToBack(localNode);
  }

  Node createLeaf(int paramInt)
  {
    return new Node(paramInt);
  }

  Node createLeaf(int paramInt1, int paramInt2)
  {
    return new Node(paramInt1, paramInt2);
  }

  Node createSwitch(Node paramNode, int paramInt)
  {
    Node.Jump localJump = new Node.Jump(110, paramNode, paramInt);
    Node localNode = new Node(125, localJump);
    return localNode;
  }

  void addSwitchCase(Node paramNode1, Node paramNode2, Node paramNode3)
  {
    if (paramNode1.getType() != 125)
      throw Kit.codeBug();
    Node.Jump localJump1 = (Node.Jump)paramNode1.getFirstChild();
    if (localJump1.getType() != 110)
      throw Kit.codeBug();
    Node localNode = Node.newTarget();
    if (paramNode2 != null)
    {
      Node.Jump localJump2 = new Node.Jump(111, paramNode2);
      localJump2.target = localNode;
      localJump1.addChildToBack(localJump2);
    }
    else
    {
      localJump1.setDefault(localNode);
    }
    paramNode1.addChildToBack(localNode);
    paramNode1.addChildToBack(paramNode3);
  }

  void closeSwitch(Node paramNode)
  {
    if (paramNode.getType() != 125)
      throw Kit.codeBug();
    Node.Jump localJump = (Node.Jump)paramNode.getFirstChild();
    if (localJump.getType() != 110)
      throw Kit.codeBug();
    Node localNode1 = Node.newTarget();
    localJump.target = localNode1;
    Node localNode2 = localJump.getDefault();
    if (localNode2 == null)
      localNode2 = localNode1;
    paramNode.addChildAfter(makeJump(5, localNode2), localJump);
    paramNode.addChildToBack(localNode1);
  }

  Node createVariables(int paramInt)
  {
    return new Node(118, paramInt);
  }

  Node createExprStatement(Node paramNode, int paramInt)
  {
    int i;
    if (this.parser.insideFunction())
      i = 129;
    else
      i = 130;
    return new Node(i, paramNode, paramInt);
  }

  Node createExprStatementNoReturn(Node paramNode, int paramInt)
  {
    return new Node(129, paramNode, paramInt);
  }

  Node createDefaultNamespace(Node paramNode, int paramInt)
  {
    setRequiresActivation();
    Node localNode1 = createUnary(70, paramNode);
    Node localNode2 = createExprStatement(localNode1, paramInt);
    return localNode2;
  }

  Node createName(String paramString)
  {
    checkActivationName(paramString, 38);
    return Node.newString(38, paramString);
  }

  Node createString(String paramString)
  {
    return Node.newString(paramString);
  }

  Node createNumber(double paramDouble)
  {
    return Node.newNumber(paramDouble);
  }

  Node createCatch(String paramString, Node paramNode1, Node paramNode2, int paramInt)
  {
    if (paramNode1 == null)
      paramNode1 = new Node(124);
    return new Node(120, createName(paramString), paramNode1, paramNode2, paramInt);
  }

  Node createThrow(Node paramNode, int paramInt)
  {
    return new Node(49, paramNode, paramInt);
  }

  Node createReturn(Node paramNode, int paramInt)
  {
    return new Node(4, paramNode, paramInt);
  }

  Node createLabel(int paramInt)
  {
    return new Node.Jump(126, paramInt);
  }

  Node getLabelLoop(Node paramNode)
  {
    return ((Node.Jump)paramNode).getLoop();
  }

  Node createLabeledStatement(Node paramNode1, Node paramNode2)
  {
    Node.Jump localJump = (Node.Jump)paramNode1;
    Node localNode1 = Node.newTarget();
    Node localNode2 = new Node(125, localJump, paramNode2, localNode1);
    localJump.target = localNode1;
    return localNode2;
  }

  Node createBreak(Node paramNode, int paramInt)
  {
    Node.Jump localJump2;
    Node.Jump localJump1 = new Node.Jump(116, paramInt);
    int i = paramNode.getType();
    if ((i == 128) || (i == 126))
      localJump2 = (Node.Jump)paramNode;
    else if ((i == 125) && (paramNode.getFirstChild().getType() == 110))
      localJump2 = (Node.Jump)paramNode.getFirstChild();
    else
      throw Kit.codeBug();
    localJump1.setJumpStatement(localJump2);
    return localJump1;
  }

  Node createContinue(Node paramNode, int paramInt)
  {
    if (paramNode.getType() != 128)
      Kit.codeBug();
    Node.Jump localJump = new Node.Jump(117, paramInt);
    localJump.setJumpStatement((Node.Jump)paramNode);
    return localJump;
  }

  Node createBlock(int paramInt)
  {
    return new Node(125, paramInt);
  }

  FunctionNode createFunction(String paramString)
  {
    return new FunctionNode(paramString);
  }

  Node initFunction(FunctionNode paramFunctionNode, int paramInt1, Node paramNode, int paramInt2)
  {
    paramFunctionNode.itsFunctionType = paramInt2;
    paramFunctionNode.addChildToBack(paramNode);
    int i = paramFunctionNode.getFunctionCount();
    if (i != 0)
    {
      paramFunctionNode.itsNeedsActivation = true;
      for (int j = 0; j != i; ++j)
      {
        localObject2 = paramFunctionNode.getFunctionNode(j);
        if (((FunctionNode)localObject2).getFunctionType() == 3)
        {
          String str = ((FunctionNode)localObject2).getFunctionName();
          if ((str != null) && (str.length() != 0))
            paramFunctionNode.removeParamOrVar(str);
        }
      }
    }
    if (paramInt2 == 2)
    {
      localObject1 = paramFunctionNode.getFunctionName();
      if ((localObject1 != null) && (((String)localObject1).length() != 0) && (!(paramFunctionNode.hasParamOrVar((String)localObject1))))
      {
        paramFunctionNode.addVar((String)localObject1);
        localObject2 = new Node(129, new Node(8, Node.newString(48, (String)localObject1), new Node(61)));
        paramNode.addChildrenToFront((Node)localObject2);
      }
    }
    Object localObject1 = paramNode.getLastChild();
    if ((localObject1 == null) || (((Node)localObject1).getType() != 4))
      paramNode.addChildToBack(new Node(4));
    Object localObject2 = Node.newString(105, paramFunctionNode.getFunctionName());
    ((Node)localObject2).putIntProp(1, paramInt1);
    return ((Node)(Node)localObject2);
  }

  void addChildToBack(Node paramNode1, Node paramNode2)
  {
    paramNode1.addChildToBack(paramNode2);
  }

  Node createLoopNode(Node paramNode, int paramInt)
  {
    Node.Jump localJump = new Node.Jump(128, paramInt);
    if (paramNode != null)
      ((Node.Jump)paramNode).setLoop(localJump);
    return localJump;
  }

  Node createWhile(Node paramNode1, Node paramNode2, Node paramNode3)
  {
    return createLoop((Node.Jump)paramNode1, 1, paramNode3, paramNode2, null, null);
  }

  Node createDoWhile(Node paramNode1, Node paramNode2, Node paramNode3)
  {
    return createLoop((Node.Jump)paramNode1, 0, paramNode2, paramNode3, null, null);
  }

  Node createFor(Node paramNode1, Node paramNode2, Node paramNode3, Node paramNode4, Node paramNode5)
  {
    return createLoop((Node.Jump)paramNode1, 2, paramNode5, paramNode3, paramNode2, paramNode4);
  }

  private Node createLoop(Node.Jump paramJump, int paramInt, Node paramNode1, Node paramNode2, Node paramNode3, Node paramNode4)
  {
    Node localNode1 = Node.newTarget();
    Node localNode2 = Node.newTarget();
    if ((paramInt == 2) && (paramNode2.getType() == 124))
      paramNode2 = new Node(44);
    Node.Jump localJump = new Node.Jump(6, paramNode2);
    localJump.target = localNode1;
    Node localNode3 = Node.newTarget();
    paramJump.addChildToBack(localNode1);
    paramJump.addChildrenToBack(paramNode1);
    if ((paramInt == 1) || (paramInt == 2))
      paramJump.addChildrenToBack(new Node(124, paramJump.getLineno()));
    paramJump.addChildToBack(localNode2);
    paramJump.addChildToBack(localJump);
    paramJump.addChildToBack(localNode3);
    paramJump.target = localNode3;
    Object localObject = localNode2;
    if ((paramInt == 1) || (paramInt == 2))
    {
      paramJump.addChildToFront(makeJump(5, localNode2));
      if (paramInt == 2)
      {
        if (paramNode3.getType() != 124)
        {
          if (paramNode3.getType() != 118)
            paramNode3 = new Node(129, paramNode3);
          paramJump.addChildToFront(paramNode3);
        }
        Node localNode4 = Node.newTarget();
        paramJump.addChildAfter(localNode4, paramNode1);
        if (paramNode4.getType() != 124)
        {
          paramNode4 = new Node(129, paramNode4);
          paramJump.addChildAfter(paramNode4, localNode4);
        }
        localObject = localNode4;
      }
    }
    paramJump.setContinue((Node)localObject);
    return ((Node)paramJump);
  }

  Node createForIn(Node paramNode1, Node paramNode2, Node paramNode3, Node paramNode4, boolean paramBoolean)
  {
    Node localNode1;
    int i = paramNode2.getType();
    if (i == 118)
    {
      localNode2 = paramNode2.getLastChild();
      if (paramNode2.getFirstChild() != localNode2)
        this.parser.reportError("msg.mult.index");
      localNode1 = Node.newString(38, localNode2.getString());
    }
    else
    {
      localNode1 = makeReference(paramNode2);
      if (localNode1 == null)
      {
        this.parser.reportError("msg.bad.for.in.lhs");
        return paramNode3;
      }
    }
    Node localNode2 = new Node(137);
    int j = (paramBoolean) ? 58 : 57;
    Node localNode3 = new Node(j, paramNode3);
    localNode3.putProp(3, localNode2);
    Node localNode4 = new Node(59);
    localNode4.putProp(3, localNode2);
    Node localNode5 = new Node(60);
    localNode5.putProp(3, localNode2);
    Node localNode6 = new Node(125);
    Node localNode7 = simpleAssignment(localNode1, localNode5);
    localNode6.addChildToBack(new Node(129, localNode7));
    localNode6.addChildToBack(paramNode4);
    paramNode1 = createWhile(paramNode1, localNode4, localNode6);
    paramNode1.addChildToFront(localNode3);
    if (i == 118)
      paramNode1.addChildToFront(paramNode2);
    localNode2.addChildToBack(paramNode1);
    return localNode2;
  }

  Node createTryCatchFinally(Node paramNode1, Node paramNode2, Node paramNode3, int paramInt)
  {
    Node localNode2;
    Node localNode3;
    Node localNode4;
    int i = ((paramNode3 != null) && (((paramNode3.getType() != 125) || (paramNode3.hasChildren())))) ? 1 : 0;
    if ((paramNode1.getType() == 125) && (!(paramNode1.hasChildren())) && (i == 0))
      return paramNode1;
    boolean bool = paramNode2.hasChildren();
    if ((i == 0) && (!(bool)))
      return paramNode1;
    Node localNode1 = new Node(137);
    Node.Jump localJump = new Node.Jump(77, paramNode1, paramInt);
    localJump.putProp(3, localNode1);
    if (bool)
    {
      localNode2 = Node.newTarget();
      localJump.addChildToBack(makeJump(5, localNode2));
      localNode3 = Node.newTarget();
      localJump.target = localNode3;
      localJump.addChildToBack(localNode3);
      localNode4 = new Node(137);
      Node localNode5 = paramNode2.getFirstChild();
      int j = 0;
      for (int k = 0; localNode5 != null; ++k)
      {
        Node localNode10;
        int l = localNode5.getLineno();
        Node localNode7 = localNode5.getFirstChild();
        Node localNode8 = localNode7.getNext();
        Node localNode9 = localNode8.getNext();
        localNode5.removeChild(localNode7);
        localNode5.removeChild(localNode8);
        localNode5.removeChild(localNode9);
        localNode9.addChildToBack(new Node(3));
        localNode9.addChildToBack(makeJump(5, localNode2));
        if (localNode8.getType() == 124)
        {
          localNode10 = localNode9;
          j = 1;
        }
        else
        {
          localNode10 = createIf(localNode8, localNode9, null, l);
        }
        Node localNode11 = new Node(56, localNode7, createUseLocal(localNode1));
        localNode11.putProp(3, localNode4);
        localNode11.putIntProp(14, k);
        localNode4.addChildToBack(localNode11);
        localNode4.addChildToBack(createWith(createUseLocal(localNode4), localNode10, l));
        localNode5 = localNode5.getNext();
      }
      localJump.addChildToBack(localNode4);
      if (j == 0)
      {
        Node localNode6 = new Node(50);
        localNode6.putProp(3, localNode1);
        localJump.addChildToBack(localNode6);
      }
      localJump.addChildToBack(localNode2);
    }
    if (i != 0)
    {
      localNode2 = Node.newTarget();
      localJump.setFinally(localNode2);
      localJump.addChildToBack(makeJump(131, localNode2));
      localNode3 = Node.newTarget();
      localJump.addChildToBack(makeJump(5, localNode3));
      localJump.addChildToBack(localNode2);
      localNode4 = new Node(121, paramNode3);
      localNode4.putProp(3, localNode1);
      localJump.addChildToBack(localNode4);
      localJump.addChildToBack(localNode3);
    }
    localNode1.addChildToBack(localJump);
    return localNode1;
  }

  Node createWith(Node paramNode1, Node paramNode2, int paramInt)
  {
    setRequiresActivation();
    Node localNode1 = new Node(125, paramInt);
    localNode1.addChildToBack(new Node(2, paramNode1));
    Node localNode2 = new Node(119, paramNode2, paramInt);
    localNode1.addChildrenToBack(localNode2);
    localNode1.addChildToBack(new Node(3));
    return localNode1;
  }

  public Node createDotQuery(Node paramNode1, Node paramNode2, int paramInt)
  {
    setRequiresActivation();
    Node localNode = new Node(142, paramNode1, paramNode2, paramInt);
    return localNode;
  }

  Node createArrayLiteral(ObjArray paramObjArray, int paramInt)
  {
    int i = paramObjArray.size();
    int[] arrayOfInt = null;
    if (paramInt != 0)
      arrayOfInt = new int[paramInt];
    Node localNode1 = new Node(63);
    int j = 0;
    int k = 0;
    while (j != i)
    {
      Node localNode2 = (Node)paramObjArray.get(j);
      if (localNode2 != null)
      {
        localNode1.addChildToBack(localNode2);
      }
      else
      {
        arrayOfInt[k] = j;
        ++k;
      }
      ++j;
    }
    if (paramInt != 0)
      localNode1.putProp(11, arrayOfInt);
    return localNode1;
  }

  Node createObjectLiteral(ObjArray paramObjArray)
  {
    Object[] arrayOfObject;
    int i = paramObjArray.size() / 2;
    Node localNode1 = new Node(64);
    if (i == 0)
    {
      arrayOfObject = ScriptRuntime.emptyArgs;
    }
    else
    {
      arrayOfObject = new Object[i];
      for (int j = 0; j != i; ++j)
      {
        arrayOfObject[j] = paramObjArray.get(2 * j);
        Node localNode2 = (Node)paramObjArray.get(2 * j + 1);
        localNode1.addChildToBack(localNode2);
      }
    }
    localNode1.putProp(12, arrayOfObject);
    return localNode1;
  }

  Node createRegExp(int paramInt)
  {
    Node localNode = new Node(47);
    localNode.putIntProp(4, paramInt);
    return localNode;
  }

  Node createIf(Node paramNode1, Node paramNode2, Node paramNode3, int paramInt)
  {
    int i = isAlwaysDefinedBoolean(paramNode1);
    if (i == 1)
      return paramNode2;
    if (i == -1)
    {
      if (paramNode3 != null)
        return paramNode3;
      return new Node(125, paramInt);
    }
    Node localNode1 = new Node(125, paramInt);
    Node localNode2 = Node.newTarget();
    Node.Jump localJump = new Node.Jump(7, paramNode1);
    localJump.target = localNode2;
    localNode1.addChildToBack(localJump);
    localNode1.addChildrenToBack(paramNode2);
    if (paramNode3 != null)
    {
      Node localNode3 = Node.newTarget();
      localNode1.addChildToBack(makeJump(5, localNode3));
      localNode1.addChildToBack(localNode2);
      localNode1.addChildrenToBack(paramNode3);
      localNode1.addChildToBack(localNode3);
    }
    else
    {
      localNode1.addChildToBack(localNode2);
    }
    return localNode1;
  }

  Node createCondExpr(Node paramNode1, Node paramNode2, Node paramNode3)
  {
    int i = isAlwaysDefinedBoolean(paramNode1);
    if (i == 1)
      return paramNode2;
    if (i == -1)
      return paramNode3;
    return new Node(98, paramNode1, paramNode2, paramNode3);
  }

  Node createUnary(int paramInt, Node paramNode)
  {
    int j;
    int i = paramNode.getType();
    switch (paramInt)
    {
    case 31:
      Node localNode1;
      Node localNode2;
      Node localNode3;
      if (i == 38)
      {
        paramNode.setType(48);
        localNode2 = paramNode;
        localNode3 = Node.newString(paramNode.getString());
        localNode1 = new Node(paramInt, localNode2, localNode3);
      }
      else if ((i == 33) || (i == 35))
      {
        localNode2 = paramNode.getFirstChild();
        localNode3 = paramNode.getLastChild();
        paramNode.removeChild(localNode2);
        paramNode.removeChild(localNode3);
        localNode1 = new Node(paramInt, localNode2, localNode3);
      }
      else if (i == 65)
      {
        localNode2 = paramNode.getFirstChild();
        paramNode.removeChild(localNode2);
        localNode1 = new Node(67, localNode2);
      }
      else
      {
        localNode1 = new Node(44);
      }
      return localNode1;
    case 32:
      if (i != 38)
        break label306;
      paramNode.setType(133);
      return paramNode;
    case 27:
      if (i != 39)
        break label306;
      j = ScriptRuntime.toInt32(paramNode.getDouble());
      paramNode.setDouble(j ^ 0xFFFFFFFF);
      return paramNode;
    case 29:
      if (i != 39)
        break label306;
      paramNode.setDouble(-paramNode.getDouble());
      return paramNode;
    case 26:
      int k;
      j = isAlwaysDefinedBoolean(paramNode);
      if (j == 0)
        break label306;
      if (j == 1)
        k = 43;
      else
        k = 44;
      if ((i == 44) || (i == 43))
      {
        paramNode.setType(k);
        return paramNode;
      }
      return new Node(k);
    case 28:
    case 30:
    }
    label306: return new Node(paramInt, paramNode);
  }

  Node createCallOrNew(int paramInt, Node paramNode)
  {
    int i = 0;
    if (paramNode.getType() == 38)
    {
      localObject = paramNode.getString();
      if (((String)localObject).equals("eval"))
        i = 1;
      else if (((String)localObject).equals("With"))
        i = 2;
    }
    else if (paramNode.getType() == 33)
    {
      localObject = paramNode.getLastChild().getString();
      if (((String)localObject).equals("eval"))
        i = 1;
    }
    Object localObject = new Node(paramInt, paramNode);
    if (i != 0)
    {
      setRequiresActivation();
      ((Node)localObject).putIntProp(10, i);
    }
    return ((Node)localObject);
  }

  Node createIncDec(int paramInt, boolean paramBoolean, Node paramNode)
  {
    paramNode = makeReference(paramNode);
    if (paramNode == null)
    {
      String str;
      if (paramInt == 103)
        str = "msg.bad.decr";
      else
        str = "msg.bad.incr";
      this.parser.reportError(str);
      return null;
    }
    int i = paramNode.getType();
    switch (i)
    {
    case 33:
    case 35:
    case 38:
    case 65:
      Node localNode = new Node(paramInt, paramNode);
      int j = 0;
      if (paramInt == 103)
        j |= 1;
      if (paramBoolean)
        j |= 2;
      localNode.putIntProp(13, j);
      return localNode;
    }
    throw Kit.codeBug();
  }

  Node createPropertyGet(Node paramNode, String paramString1, String paramString2, int paramInt)
  {
    if ((paramString1 == null) && (paramInt == 0))
    {
      if (paramNode == null)
        return createName(paramString2);
      checkActivationName(paramString2, 33);
      if (ScriptRuntime.isSpecialProperty(paramString2))
      {
        localNode = new Node(69, paramNode);
        localNode.putProp(17, paramString2);
        return new Node(65, localNode);
      }
      return new Node(33, paramNode, createString(paramString2));
    }
    Node localNode = createString(paramString2);
    paramInt |= 1;
    return createMemberRefGet(paramNode, paramString1, localNode, paramInt);
  }

  Node createElementGet(Node paramNode1, String paramString, Node paramNode2, int paramInt)
  {
    if ((paramString == null) && (paramInt == 0))
    {
      if (paramNode1 == null)
        throw Kit.codeBug();
      return new Node(35, paramNode1, paramNode2);
    }
    return createMemberRefGet(paramNode1, paramString, paramNode2, paramInt);
  }

  private Node createMemberRefGet(Node paramNode1, String paramString, Node paramNode2, int paramInt)
  {
    Node localNode2;
    Node localNode1 = null;
    if (paramString != null)
      if (paramString.equals("*"))
        localNode1 = new Node(41);
      else
        localNode1 = createName(paramString);
    if (paramNode1 == null)
      if (paramString == null)
        localNode2 = new Node(75, paramNode2);
      else
        localNode2 = new Node(76, localNode1, paramNode2);
    else if (paramString == null)
      localNode2 = new Node(73, paramNode1, paramNode2);
    else
      localNode2 = new Node(74, paramNode1, localNode1, paramNode2);
    if (paramInt != 0)
      localNode2.putIntProp(16, paramInt);
    return new Node(65, localNode2);
  }

  Node createBinary(int paramInt, Node paramNode1, Node paramNode2)
  {
    double d;
    label427: int i;
    int j;
    switch (paramInt)
    {
    case 21:
      if (paramNode1.type == 40)
      {
        if (paramNode2.type == 40)
        {
          str1 = paramNode2.getString();
        }
        else
        {
          if (paramNode2.type != 39)
            break label564;
          str1 = ScriptRuntime.numberToString(paramNode2.getDouble(), 10);
        }
        str2 = paramNode1.getString();
        paramNode1.setString(str2.concat(str1));
        return paramNode1;
      }
      if (paramNode1.type != 39)
        break label564;
      if (paramNode2.type == 39)
      {
        paramNode1.setDouble(paramNode1.getDouble() + paramNode2.getDouble());
        return paramNode1;
      }
      if (paramNode2.type != 40)
        break label564;
      String str1 = ScriptRuntime.numberToString(paramNode1.getDouble(), 10);
      String str2 = paramNode2.getString();
      paramNode2.setString(str1.concat(str2));
      return paramNode2;
    case 22:
      if (paramNode1.type == 39)
      {
        d = paramNode1.getDouble();
        if (paramNode2.type == 39)
        {
          paramNode1.setDouble(d - paramNode2.getDouble());
          return paramNode1;
        }
        if (d == 0D)
          return new Node(29, paramNode2);
        break label564:
      }
      if ((paramNode2.type != 39) || (paramNode2.getDouble() != 0D))
        break label564;
      return new Node(28, paramNode1);
    case 23:
      if (paramNode1.type == 39)
      {
        d = paramNode1.getDouble();
        if (paramNode2.type == 39)
        {
          paramNode1.setDouble(d * paramNode2.getDouble());
          return paramNode1;
        }
        if (d == 1D)
          return new Node(28, paramNode2);
        break label564:
      }
      if ((paramNode2.type != 39) || (paramNode2.getDouble() != 1D))
        break label564;
      return new Node(28, paramNode1);
    case 24:
      if (paramNode2.type != 39)
        break label564;
      d = paramNode2.getDouble();
      if (paramNode1.type == 39)
      {
        paramNode1.setDouble(paramNode1.getDouble() / d);
        return paramNode1;
      }
      if (d != 1D)
        break label427;
      return new Node(28, paramNode1);
      break;
    case 101:
      i = isAlwaysDefinedBoolean(paramNode1);
      if (i == -1)
        return new Node(43);
      if (i == 1)
        return paramNode2;
      j = isAlwaysDefinedBoolean(paramNode2);
      if (j == -1)
      {
        if (hasSideEffects(paramNode1))
          break label564;
        return new Node(43);
      }
      if (j != 1)
        break label564;
      return paramNode1;
    case 100:
      i = isAlwaysDefinedBoolean(paramNode1);
      if (i == 1)
        return new Node(44);
      if (i == -1)
        return paramNode2;
      j = isAlwaysDefinedBoolean(paramNode2);
      if (j == 1)
      {
        if (hasSideEffects(paramNode1))
          break label564;
        return new Node(44);
      }
      if (j != -1)
        break label564;
      return paramNode1;
    }
    label564: return new Node(paramInt, paramNode1, paramNode2);
  }

  private Node simpleAssignment(Node paramNode1, Node paramNode2)
  {
    Node localNode1;
    int i = paramNode1.getType();
    switch (i)
    {
    case 38:
      paramNode1.setType(48);
      return new Node(8, paramNode1, paramNode2);
    case 33:
    case 35:
      int j;
      localNode1 = paramNode1.getFirstChild();
      Node localNode2 = paramNode1.getLastChild();
      if (i == 33)
        j = 34;
      else
        j = 36;
      return new Node(j, localNode1, localNode2, paramNode2);
    case 65:
      localNode1 = paramNode1.getFirstChild();
      checkMutableReference(localNode1);
      return new Node(66, localNode1, paramNode2);
    }
    throw Kit.codeBug();
  }

  private void checkMutableReference(Node paramNode)
  {
    int i = paramNode.getIntProp(16, 0);
    if ((i & 0x4) != 0)
      this.parser.reportError("msg.bad.assign.left");
  }

  Node createAssignment(int paramInt, Node paramNode1, Node paramNode2)
  {
    int i;
    Object localObject;
    Node localNode1;
    Node localNode4;
    paramNode1 = makeReference(paramNode1);
    if (paramNode1 == null)
    {
      this.parser.reportError("msg.bad.assign.left");
      return paramNode2;
    }
    switch (paramInt)
    {
    case 86:
      return simpleAssignment(paramNode1, paramNode2);
    case 87:
      i = 9;
      break;
    case 88:
      i = 10;
      break;
    case 89:
      i = 11;
      break;
    case 90:
      i = 18;
      break;
    case 91:
      i = 19;
      break;
    case 92:
      i = 20;
      break;
    case 93:
      i = 21;
      break;
    case 94:
      i = 22;
      break;
    case 95:
      i = 23;
      break;
    case 96:
      i = 24;
      break;
    case 97:
      i = 25;
      break;
    default:
      throw Kit.codeBug();
    }
    int j = paramNode1.getType();
    switch (j)
    {
    case 38:
      localObject = paramNode1.getString();
      localNode1 = Node.newString(38, (String)localObject);
      Node localNode2 = new Node(i, localNode1, paramNode2);
      localNode4 = Node.newString(48, (String)localObject);
      return new Node(8, localNode4, localNode2);
    case 33:
    case 35:
      localObject = paramNode1.getFirstChild();
      localNode1 = paramNode1.getLastChild();
      int k = (j == 33) ? 135 : 136;
      localNode4 = new Node(134);
      Node localNode5 = new Node(i, localNode4, paramNode2);
      return new Node(k, (Node)localObject, localNode1, localNode5);
    case 65:
      localObject = paramNode1.getFirstChild();
      checkMutableReference((Node)localObject);
      localNode1 = new Node(134);
      Node localNode3 = new Node(i, localNode1, paramNode2);
      return new Node(138, (Node)localObject, localNode3);
    }
    throw Kit.codeBug();
  }

  Node createUseLocal(Node paramNode)
  {
    if (137 != paramNode.getType())
      throw Kit.codeBug();
    Node localNode = new Node(53);
    localNode.putProp(3, paramNode);
    return localNode;
  }

  private Node.Jump makeJump(int paramInt, Node paramNode)
  {
    Node.Jump localJump = new Node.Jump(paramInt);
    localJump.target = paramNode;
    return localJump;
  }

  private Node makeReference(Node paramNode)
  {
    int i = paramNode.getType();
    switch (i)
    {
    case 33:
    case 35:
    case 38:
    case 65:
      return paramNode;
    case 37:
      paramNode.setType(68);
      return new Node(65, paramNode);
    }
    return null;
  }

  private static int isAlwaysDefinedBoolean(Node paramNode)
  {
    switch (paramNode.getType())
    {
    case 41:
    case 43:
      return -1;
    case 44:
      return 1;
    case 39:
      double d = paramNode.getDouble();
      if ((d == d) && (d != 0D))
        return 1;
      return -1;
    case 40:
    case 42:
    }
    return 0;
  }

  private static boolean hasSideEffects(Node paramNode)
  {
    switch (paramNode.getType())
    {
    case 8:
    case 30:
    case 34:
    case 36:
    case 37:
    case 102:
    case 103:
      return true;
    }
    for (Node localNode = paramNode.getFirstChild(); localNode != null; localNode = localNode.getNext())
      if (hasSideEffects(localNode))
        return true;
    return false;
  }

  private void checkActivationName(String paramString, int paramInt)
  {
    if (this.parser.insideFunction())
    {
      int i = 0;
      if (("arguments".equals(paramString)) || ((this.parser.compilerEnv.activationNames != null) && (this.parser.compilerEnv.activationNames.containsKey(paramString))))
        i = 1;
      else if (("length".equals(paramString)) && (paramInt == 33) && (this.parser.compilerEnv.getLanguageVersion() == 120))
        i = 1;
      if (i != 0)
        setRequiresActivation();
    }
  }

  private void setRequiresActivation()
  {
    if (this.parser.insideFunction())
      ((FunctionNode)this.parser.currentScriptOrFn).itsNeedsActivation = true;
  }
}
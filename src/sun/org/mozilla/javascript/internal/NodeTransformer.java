package sun.org.mozilla.javascript.internal;

public class NodeTransformer
{
  private ObjArray loops;
  private ObjArray loopEnds;
  private boolean hasFinally;

  public final void transform(ScriptOrFnNode paramScriptOrFnNode)
  {
    transformCompilationUnit(paramScriptOrFnNode);
    for (int i = 0; i != paramScriptOrFnNode.getFunctionCount(); ++i)
    {
      FunctionNode localFunctionNode = paramScriptOrFnNode.getFunctionNode(i);
      transform(localFunctionNode);
    }
  }

  private void transformCompilationUnit(ScriptOrFnNode paramScriptOrFnNode)
  {
    this.loops = new ObjArray();
    this.loopEnds = new ObjArray();
    this.hasFinally = false;
    transformCompilationUnit_r(paramScriptOrFnNode, paramScriptOrFnNode);
  }

  private void transformCompilationUnit_r(ScriptOrFnNode paramScriptOrFnNode, Node paramNode)
  {
    Node localNode1 = null;
    while (true)
    {
      Node localNode2;
      int i;
      Object localObject3;
      Node localNode5;
      Object localObject5;
      Object localObject6;
      while (true)
      {
        Node localNode4;
        while (true)
        {
          localNode2 = null;
          if (localNode1 == null)
          {
            localNode1 = paramNode.getFirstChild();
          }
          else
          {
            localNode2 = localNode1;
            localNode1 = localNode1.getNext();
          }
          if (localNode1 == null)
            return;
          i = localNode1.getType();
          switch (i)
          {
          case 110:
          case 126:
          case 128:
            this.loops.push(localNode1);
            this.loopEnds.push(((Node.Jump)localNode1).target);
            break;
          case 119:
            this.loops.push(localNode1);
            localObject1 = localNode1.getNext();
            if (((Node)localObject1).getType() != 3)
              Kit.codeBug();
            this.loopEnds.push(localObject1);
            break;
          case 77:
            localObject1 = (Node.Jump)localNode1;
            Node localNode3 = ((Node.Jump)localObject1).getFinally();
            if (localNode3 == null)
              break label1068;
            this.hasFinally = true;
            this.loops.push(localNode1);
            this.loopEnds.push(localNode3);
            break;
          case 3:
          case 127:
            if ((this.loopEnds.isEmpty()) || (this.loopEnds.peek() != localNode1))
              break label1068;
            this.loopEnds.pop();
            this.loops.pop();
            break;
          case 4:
            if (!(this.hasFinally))
              break label1068:
            localObject1 = null;
            for (int j = this.loops.size() - 1; j >= 0; --j)
            {
              localNode4 = (Node)this.loops.get(j);
              int l = localNode4.getType();
              if ((l == 77) || (l == 119))
              {
                Object localObject4;
                if (l == 77)
                {
                  localObject5 = new Node.Jump(131);
                  localObject6 = ((Node.Jump)localNode4).getFinally();
                  ((Node.Jump)localObject5).target = ((Node)localObject6);
                  localObject4 = localObject5;
                }
                else
                {
                  localObject4 = new Node(3);
                }
                if (localObject1 == null)
                  localObject1 = new Node(125, localNode1.getLineno());
                ((Node)localObject1).addChildToBack((Node)localObject4);
              }
            }
            if (localObject1 == null)
              break label1068;
            localObject2 = localNode1;
            localNode4 = ((Node)localObject2).getFirstChild();
            localNode1 = replaceCurrent(paramNode, localNode2, localNode1, (Node)localObject1);
            if (localNode4 != null)
              break;
            ((Node)localObject1).addChildToBack((Node)localObject2);
          case 116:
          case 117:
          case 37:
          case 30:
          case 118:
          case 8:
          case 31:
          case 38:
          }
        }
        localNode5 = new Node(130, localNode4);
        ((Node)localObject1).addChildToFront(localNode5);
        localObject2 = new Node(62);
        ((Node)localObject1).addChildToBack((Node)localObject2);
        transformCompilationUnit_r(paramScriptOrFnNode, localNode5);
      }
      Object localObject1 = (Node.Jump)localNode1;
      Object localObject2 = ((Node.Jump)localObject1).getJumpStatement();
      if (localObject2 == null)
        Kit.codeBug();
      int k = this.loops.size();
      while (true)
      {
        if (k == 0)
          throw Kit.codeBug();
        localNode5 = (Node)this.loops.get(--k);
        if (localNode5 == localObject2)
          break;
        int i1 = localNode5.getType();
        if (i1 == 119)
        {
          localObject5 = new Node(3);
          localNode2 = addBeforeCurrent(paramNode, localNode2, localNode1, (Node)localObject5);
        }
        else if (i1 == 77)
        {
          localObject5 = (Node.Jump)localNode5;
          localObject6 = new Node.Jump(131);
          ((Node.Jump)localObject6).target = ((Node.Jump)localObject5).getFinally();
          localNode2 = addBeforeCurrent(paramNode, localNode2, localNode1, (Node)localObject6);
        }
      }
      if (i == 116)
        ((Node.Jump)localObject1).target = ((Node.Jump)localObject2).target;
      else
        ((Node.Jump)localObject1).target = ((Node.Jump)localObject2).getContinue();
      ((Node.Jump)localObject1).setType(5);
      break label1068:
      visitCall(localNode1, paramScriptOrFnNode);
      break label1068:
      visitNew(localNode1, paramScriptOrFnNode);
      break label1068:
      localObject1 = new Node(125);
      localObject2 = localNode1.getFirstChild();
      while (true)
      {
        while (true)
        {
          if (localObject2 == null)
            break label900;
          localObject3 = localObject2;
          if (((Node)localObject3).getType() != 38)
            Kit.codeBug();
          localObject2 = ((Node)localObject2).getNext();
          if (((Node)localObject3).hasChildren())
            break;
        }
        localNode5 = ((Node)localObject3).getFirstChild();
        ((Node)localObject3).removeChild(localNode5);
        ((Node)localObject3).setType(48);
        localObject3 = new Node(8, (Node)localObject3, localNode5);
        Node localNode6 = new Node(129, (Node)localObject3, localNode1.getLineno());
        ((Node)localObject1).addChildToBack(localNode6);
      }
      label900: localNode1 = replaceCurrent(paramNode, localNode2, localNode1, (Node)localObject1);
      if (paramScriptOrFnNode.getType() == 105)
      {
        if (((FunctionNode)paramScriptOrFnNode).requiresActivation())
          break label1068:
        if (i == 38)
        {
          localObject1 = localNode1;
        }
        else
        {
          localObject1 = localNode1.getFirstChild();
          if (((Node)localObject1).getType() != 48)
          {
            if (i == 31)
              break label1068:
            throw Kit.codeBug();
          }
        }
        localObject2 = ((Node)localObject1).getString();
        if (paramScriptOrFnNode.hasParamOrVar((String)localObject2))
          if (i == 38)
          {
            localNode1.setType(54);
          }
          else if (i == 8)
          {
            localNode1.setType(55);
            ((Node)localObject1).setType(40);
          }
          else if (i == 31)
          {
            localObject3 = new Node(43);
            localNode1 = replaceCurrent(paramNode, localNode2, localNode1, (Node)localObject3);
          }
          else
          {
            throw Kit.codeBug();
          }
      }
      label1068: transformCompilationUnit_r(paramScriptOrFnNode, localNode1);
    }
  }

  protected void visitNew(Node paramNode, ScriptOrFnNode paramScriptOrFnNode)
  {
  }

  protected void visitCall(Node paramNode, ScriptOrFnNode paramScriptOrFnNode)
  {
  }

  private static Node addBeforeCurrent(Node paramNode1, Node paramNode2, Node paramNode3, Node paramNode4)
  {
    if (paramNode2 == null)
    {
      if (paramNode3 != paramNode1.getFirstChild())
        Kit.codeBug();
      paramNode1.addChildToFront(paramNode4);
    }
    else
    {
      if (paramNode3 != paramNode2.getNext())
        Kit.codeBug();
      paramNode1.addChildAfter(paramNode4, paramNode2);
    }
    return paramNode4;
  }

  private static Node replaceCurrent(Node paramNode1, Node paramNode2, Node paramNode3, Node paramNode4)
  {
    if (paramNode2 == null)
    {
      if (paramNode3 != paramNode1.getFirstChild())
        Kit.codeBug();
      paramNode1.replaceChild(paramNode3, paramNode4);
    }
    else if (paramNode2.next == paramNode3)
    {
      paramNode1.replaceChildAfter(paramNode2, paramNode4);
    }
    else
    {
      paramNode1.replaceChild(paramNode3, paramNode4);
    }
    return paramNode4;
  }
}
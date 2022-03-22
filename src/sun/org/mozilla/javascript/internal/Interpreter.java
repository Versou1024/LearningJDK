package sun.org.mozilla.javascript.internal;

import B;
import I;
import java.io.PrintStream;
import java.io.Serializable;
import sun.org.mozilla.javascript.internal.continuations.Continuation;
import sun.org.mozilla.javascript.internal.debug.DebugFrame;
import sun.org.mozilla.javascript.internal.debug.Debugger;

public class Interpreter
{
  private static final int Icode_DUP = -1;
  private static final int Icode_DUP2 = -2;
  private static final int Icode_SWAP = -3;
  private static final int Icode_POP = -4;
  private static final int Icode_POP_RESULT = -5;
  private static final int Icode_IFEQ_POP = -6;
  private static final int Icode_VAR_INC_DEC = -7;
  private static final int Icode_NAME_INC_DEC = -8;
  private static final int Icode_PROP_INC_DEC = -9;
  private static final int Icode_ELEM_INC_DEC = -10;
  private static final int Icode_REF_INC_DEC = -11;
  private static final int Icode_SCOPE_LOAD = -12;
  private static final int Icode_SCOPE_SAVE = -13;
  private static final int Icode_TYPEOFNAME = -14;
  private static final int Icode_NAME_AND_THIS = -15;
  private static final int Icode_PROP_AND_THIS = -16;
  private static final int Icode_ELEM_AND_THIS = -17;
  private static final int Icode_VALUE_AND_THIS = -18;
  private static final int Icode_CLOSURE_EXPR = -19;
  private static final int Icode_CLOSURE_STMT = -20;
  private static final int Icode_CALLSPECIAL = -21;
  private static final int Icode_RETUNDEF = -22;
  private static final int Icode_GOSUB = -23;
  private static final int Icode_STARTSUB = -24;
  private static final int Icode_RETSUB = -25;
  private static final int Icode_LINE = -26;
  private static final int Icode_SHORTNUMBER = -27;
  private static final int Icode_INTNUMBER = -28;
  private static final int Icode_LITERAL_NEW = -29;
  private static final int Icode_LITERAL_SET = -30;
  private static final int Icode_SPARE_ARRAYLIT = -31;
  private static final int Icode_REG_IND_C0 = -32;
  private static final int Icode_REG_IND_C1 = -33;
  private static final int Icode_REG_IND_C2 = -34;
  private static final int Icode_REG_IND_C3 = -35;
  private static final int Icode_REG_IND_C4 = -36;
  private static final int Icode_REG_IND_C5 = -37;
  private static final int Icode_REG_IND1 = -38;
  private static final int Icode_REG_IND2 = -39;
  private static final int Icode_REG_IND4 = -40;
  private static final int Icode_REG_STR_C0 = -41;
  private static final int Icode_REG_STR_C1 = -42;
  private static final int Icode_REG_STR_C2 = -43;
  private static final int Icode_REG_STR_C3 = -44;
  private static final int Icode_REG_STR1 = -45;
  private static final int Icode_REG_STR2 = -46;
  private static final int Icode_REG_STR4 = -47;
  private static final int Icode_GETVAR1 = -48;
  private static final int Icode_SETVAR1 = -49;
  private static final int Icode_UNDEF = -50;
  private static final int Icode_ZERO = -51;
  private static final int Icode_ONE = -52;
  private static final int Icode_ENTERDQ = -53;
  private static final int Icode_LEAVEDQ = -54;
  private static final int Icode_TAIL_CALL = -55;
  private static final int Icode_LOCAL_CLEAR = -56;
  private static final int MIN_ICODE = -56;
  private CompilerEnvirons compilerEnv;
  private boolean itsInFunctionFlag;
  private InterpreterData itsData;
  private ScriptOrFnNode scriptOrFn;
  private int itsICodeTop;
  private int itsStackDepth;
  private int itsLineNumber;
  private int itsDoubleTableTop;
  private ObjToIntMap itsStrings = new ObjToIntMap(20);
  private int itsLocalTop;
  private static final int MIN_LABEL_TABLE_SIZE = 32;
  private static final int MIN_FIXUP_TABLE_SIZE = 40;
  private int[] itsLabelTable;
  private int itsLabelTableTop;
  private long[] itsFixupTable;
  private int itsFixupTableTop;
  private ObjArray itsLiteralIds = new ObjArray();
  private int itsExceptionTableTop;
  private static final int EXCEPTION_TRY_START_SLOT = 0;
  private static final int EXCEPTION_TRY_END_SLOT = 1;
  private static final int EXCEPTION_HANDLER_SLOT = 2;
  private static final int EXCEPTION_TYPE_SLOT = 3;
  private static final int EXCEPTION_LOCAL_SLOT = 4;
  private static final int EXCEPTION_SCOPE_SLOT = 5;
  private static final int EXCEPTION_SLOT_SIZE = 6;
  private static final int ECF_TAIL = 1;

  private static String bytecodeName(int paramInt)
  {
    if (!(validBytecode(paramInt)))
      throw new IllegalArgumentException(String.valueOf(paramInt));
    return String.valueOf(paramInt);
  }

  private static boolean validIcode(int paramInt)
  {
    return ((-56 <= paramInt) && (paramInt <= -1));
  }

  private static boolean validTokenCode(int paramInt)
  {
    return ((2 <= paramInt) && (paramInt <= 76));
  }

  private static boolean validBytecode(int paramInt)
  {
    return ((validIcode(paramInt)) || (validTokenCode(paramInt)));
  }

  public Object compile(CompilerEnvirons paramCompilerEnvirons, ScriptOrFnNode paramScriptOrFnNode, String paramString, boolean paramBoolean)
  {
    this.compilerEnv = paramCompilerEnvirons;
    new NodeTransformer().transform(paramScriptOrFnNode);
    if (paramBoolean)
      paramScriptOrFnNode = paramScriptOrFnNode.getFunctionNode(0);
    this.scriptOrFn = paramScriptOrFnNode;
    this.itsData = new InterpreterData(paramCompilerEnvirons.getLanguageVersion(), this.scriptOrFn.getSourceName(), paramString);
    this.itsData.topLevel = true;
    if (paramBoolean)
      generateFunctionICode();
    else
      generateICodeFromTree(this.scriptOrFn);
    return this.itsData;
  }

  public Script createScriptObject(Object paramObject1, Object paramObject2)
  {
    InterpreterData localInterpreterData = (InterpreterData)paramObject1;
    return InterpretedFunction.createScript(this.itsData, paramObject2);
  }

  public Function createFunctionObject(Context paramContext, Scriptable paramScriptable, Object paramObject1, Object paramObject2)
  {
    InterpreterData localInterpreterData = (InterpreterData)paramObject1;
    return InterpretedFunction.createFunction(paramContext, paramScriptable, this.itsData, paramObject2);
  }

  private void generateFunctionICode()
  {
    this.itsInFunctionFlag = true;
    FunctionNode localFunctionNode = (FunctionNode)this.scriptOrFn;
    this.itsData.itsFunctionType = localFunctionNode.getFunctionType();
    this.itsData.itsNeedsActivation = localFunctionNode.requiresActivation();
    this.itsData.itsName = localFunctionNode.getFunctionName();
    if ((!(localFunctionNode.getIgnoreDynamicScope())) && (this.compilerEnv.isUseDynamicScope()))
      this.itsData.useDynamicScope = true;
    generateICodeFromTree(localFunctionNode.getLastChild());
  }

  private void generateICodeFromTree(Node paramNode)
  {
    Object localObject;
    generateNestedFunctions();
    generateRegExpLiterals();
    visitStatement(paramNode);
    fixLabelGotos();
    if (this.itsData.itsFunctionType == 0)
      addToken(62);
    if (this.itsData.itsICode.length != this.itsICodeTop)
    {
      localObject = new byte[this.itsICodeTop];
      System.arraycopy(this.itsData.itsICode, 0, localObject, 0, this.itsICodeTop);
      this.itsData.itsICode = ((B)localObject);
    }
    if (this.itsStrings.size() == 0)
    {
      this.itsData.itsStringTable = null;
    }
    else
    {
      this.itsData.itsStringTable = new String[this.itsStrings.size()];
      localObject = this.itsStrings.newIterator();
      ((ObjToIntMap.Iterator)localObject).start();
      while (!(((ObjToIntMap.Iterator)localObject).done()))
      {
        String str = (String)((ObjToIntMap.Iterator)localObject).getKey();
        int i = ((ObjToIntMap.Iterator)localObject).getValue();
        if (this.itsData.itsStringTable[i] != null)
          Kit.codeBug();
        this.itsData.itsStringTable[i] = str;
        ((ObjToIntMap.Iterator)localObject).next();
      }
    }
    if (this.itsDoubleTableTop == 0)
    {
      this.itsData.itsDoubleTable = null;
    }
    else if (this.itsData.itsDoubleTable.length != this.itsDoubleTableTop)
    {
      localObject = new double[this.itsDoubleTableTop];
      System.arraycopy(this.itsData.itsDoubleTable, 0, localObject, 0, this.itsDoubleTableTop);
      this.itsData.itsDoubleTable = ((D)localObject);
    }
    if ((this.itsExceptionTableTop != 0) && (this.itsData.itsExceptionTable.length != this.itsExceptionTableTop))
    {
      localObject = new int[this.itsExceptionTableTop];
      System.arraycopy(this.itsData.itsExceptionTable, 0, localObject, 0, this.itsExceptionTableTop);
      this.itsData.itsExceptionTable = ((I)localObject);
    }
    this.itsData.itsMaxVars = this.scriptOrFn.getParamAndVarCount();
    this.itsData.itsMaxFrameArray = (this.itsData.itsMaxVars + this.itsData.itsMaxLocals + this.itsData.itsMaxStack);
    this.itsData.argNames = this.scriptOrFn.getParamAndVarNames();
    this.itsData.argCount = this.scriptOrFn.getParamCount();
    this.itsData.encodedSourceStart = this.scriptOrFn.getEncodedSourceStart();
    this.itsData.encodedSourceEnd = this.scriptOrFn.getEncodedSourceEnd();
    if (this.itsLiteralIds.size() != 0)
      this.itsData.literalIds = this.itsLiteralIds.toArray();
  }

  private void generateNestedFunctions()
  {
    int i = this.scriptOrFn.getFunctionCount();
    if (i == 0)
      return;
    InterpreterData[] arrayOfInterpreterData = new InterpreterData[i];
    for (int j = 0; j != i; ++j)
    {
      FunctionNode localFunctionNode = this.scriptOrFn.getFunctionNode(j);
      Interpreter localInterpreter = new Interpreter();
      localInterpreter.compilerEnv = this.compilerEnv;
      localInterpreter.scriptOrFn = localFunctionNode;
      localInterpreter.itsData = new InterpreterData(this.itsData);
      localInterpreter.generateFunctionICode();
      arrayOfInterpreterData[j] = localInterpreter.itsData;
    }
    this.itsData.itsNestedFunctions = arrayOfInterpreterData;
  }

  private void generateRegExpLiterals()
  {
    int i = this.scriptOrFn.getRegexpCount();
    if (i == 0)
      return;
    Context localContext = Context.getContext();
    RegExpProxy localRegExpProxy = ScriptRuntime.checkRegExpProxy(localContext);
    Object[] arrayOfObject = new Object[i];
    for (int j = 0; j != i; ++j)
    {
      String str1 = this.scriptOrFn.getRegexpString(j);
      String str2 = this.scriptOrFn.getRegexpFlags(j);
      arrayOfObject[j] = localRegExpProxy.compileRegExp(localContext, str1, str2);
    }
    this.itsData.itsRegExpLiterals = arrayOfObject;
  }

  private void updateLineNumber(Node paramNode)
  {
    int i = paramNode.getLineno();
    if ((i != this.itsLineNumber) && (i >= 0))
    {
      if (this.itsData.firstLinePC < 0)
        this.itsData.firstLinePC = i;
      this.itsLineNumber = i;
      addIcode(-26);
      addUint16(i & 0xFFFF);
    }
  }

  private RuntimeException badTree(Node paramNode)
  {
    throw new RuntimeException(paramNode.toString());
  }

  private void visitStatement(Node paramNode)
  {
    int j;
    Object localObject;
    int i2;
    int i = paramNode.getType();
    Node localNode1 = paramNode.getFirstChild();
    switch (i)
    {
    case 105:
      j = paramNode.getExistingIntProp(1);
      int i1 = this.scriptOrFn.getFunctionNode(j).getFunctionType();
      if (i1 == 3)
        addIndexOp(-20, j);
      else if (i1 != 1)
        throw Kit.codeBug();
      break;
    case 119:
    case 124:
    case 125:
    case 126:
    case 128:
    case 132:
      updateLineNumber(paramNode);
      while (true)
      {
        if (localNode1 == null)
          break label1037;
        visitStatement(localNode1);
        localNode1 = localNode1.getNext();
      }
    case 2:
      visitExpression(localNode1, 0);
      addToken(2);
      stackChange(-1);
      break;
    case 3:
      addToken(3);
      break;
    case 137:
      j = allocLocal();
      paramNode.putIntProp(2, j);
      updateLineNumber(paramNode);
      while (localNode1 != null)
      {
        visitStatement(localNode1);
        localNode1 = localNode1.getNext();
      }
      addIndexOp(-56, j);
      releaseLocal(j);
      break;
    case 110:
      updateLineNumber(paramNode);
      localObject = (Node.Jump)paramNode;
      visitExpression(localNode1, 0);
      for (Node.Jump localJump2 = (Node.Jump)localNode1.getNext(); localJump2 != null; localJump2 = (Node.Jump)localJump2.getNext())
      {
        if (localJump2.getType() != 111)
          throw badTree(localJump2);
        Node localNode2 = localJump2.getFirstChild();
        addIcode(-1);
        stackChange(1);
        visitExpression(localNode2, 0);
        addToken(45);
        stackChange(-1);
        addGoto(localJump2.target, -6);
        stackChange(-1);
      }
      addIcode(-4);
      stackChange(-1);
      break;
    case 127:
      markTargetLabel(paramNode);
      break;
    case 6:
    case 7:
      localObject = ((Node.Jump)paramNode).target;
      visitExpression(localNode1, 0);
      addGoto((Node)localObject, i);
      stackChange(-1);
      break;
    case 5:
      localObject = ((Node.Jump)paramNode).target;
      addGoto((Node)localObject, i);
      break;
    case 131:
      localObject = ((Node.Jump)paramNode).target;
      addGoto((Node)localObject, -23);
      break;
    case 121:
      stackChange(1);
      int k = getLocalBlockRef(paramNode);
      addIndexOp(-24, k);
      stackChange(-1);
      while (localNode1 != null)
      {
        visitStatement(localNode1);
        localNode1 = localNode1.getNext();
      }
      addIndexOp(-25, k);
      break;
    case 129:
    case 130:
      updateLineNumber(paramNode);
      visitExpression(localNode1, 0);
      addIcode((i == 129) ? -4 : -5);
      stackChange(-1);
      break;
    case 77:
      Node.Jump localJump1 = (Node.Jump)paramNode;
      i2 = getLocalBlockRef(localJump1);
      int i3 = allocLocal();
      addIndexOp(-13, i3);
      int i4 = this.itsICodeTop;
      while (localNode1 != null)
      {
        visitStatement(localNode1);
        localNode1 = localNode1.getNext();
      }
      Node localNode3 = localJump1.target;
      if (localNode3 != null)
      {
        int i5 = this.itsLabelTable[getTargetLabel(localNode3)];
        addExceptionHandler(i4, i5, i5, false, i2, i3);
      }
      Node localNode4 = localJump1.getFinally();
      if (localNode4 != null)
      {
        int i6 = this.itsLabelTable[getTargetLabel(localNode4)];
        addExceptionHandler(i4, i6, i6, true, i2, i3);
      }
      addIndexOp(-56, i3);
      releaseLocal(i3);
      break;
    case 56:
      int l = getLocalBlockRef(paramNode);
      i2 = paramNode.getExistingIntProp(14);
      String str = localNode1.getString();
      localNode1 = localNode1.getNext();
      visitExpression(localNode1, 0);
      addStringPrefix(str);
      addIndexPrefix(l);
      addToken(56);
      addUint8((i2 != 0) ? 1 : 0);
      stackChange(-1);
      break;
    case 49:
      updateLineNumber(paramNode);
      visitExpression(localNode1, 0);
      addToken(49);
      addUint16(this.itsLineNumber & 0xFFFF);
      stackChange(-1);
      break;
    case 50:
      updateLineNumber(paramNode);
      addIndexOp(50, getLocalBlockRef(paramNode));
      break;
    case 4:
      updateLineNumber(paramNode);
      if (localNode1 != null)
      {
        visitExpression(localNode1, 1);
        addToken(4);
        stackChange(-1);
      }
      else
      {
        addIcode(-22);
      }
      break;
    case 62:
      updateLineNumber(paramNode);
      addToken(62);
      break;
    case 57:
    case 58:
      visitExpression(localNode1, 0);
      addIndexOp(i, getLocalBlockRef(paramNode));
      stackChange(-1);
      break;
    default:
      throw badTree(paramNode);
    }
    if (this.itsStackDepth != 0)
      label1037: throw Kit.codeBug();
  }

  private void visitExpression(Node paramNode, int paramInt)
  {
    int k;
    int l;
    Object localObject;
    String str;
    int i1;
    int i2;
    int i3;
    int i4;
    int i5;
    int i = paramNode.getType();
    Node localNode1 = paramNode.getFirstChild();
    int j = this.itsStackDepth;
    switch (i)
    {
    case 105:
      k = paramNode.getExistingIntProp(1);
      FunctionNode localFunctionNode = this.scriptOrFn.getFunctionNode(k);
      if (localFunctionNode.getFunctionType() != 2)
        throw Kit.codeBug();
      addIndexOp(-19, k);
      stackChange(1);
      break;
    case 53:
      k = getLocalBlockRef(paramNode);
      addIndexOp(53, k);
      stackChange(1);
      break;
    case 85:
      Node localNode2 = paramNode.getLastChild();
      while (localNode1 != localNode2)
      {
        visitExpression(localNode1, 0);
        addIcode(-4);
        stackChange(-1);
        localNode1 = localNode1.getNext();
      }
      visitExpression(localNode1, paramInt & 0x1);
      break;
    case 134:
      stackChange(1);
      break;
    case 30:
    case 37:
    case 68:
      if (i == 30)
        visitExpression(localNode1, 0);
      else
        generateCallFunAndThis(localNode1);
      for (l = 0; (localNode1 = localNode1.getNext()) != null; ++l)
        visitExpression(localNode1, 0);
      i2 = paramNode.getIntProp(10, 0);
      if (i2 != 0)
      {
        addIndexOp(-21, l);
        addUint8(i2);
        addUint8((i == 30) ? 1 : 0);
        addUint16(this.itsLineNumber & 0xFFFF);
      }
      else
      {
        if ((i == 37) && ((paramInt & 0x1) != 0))
          i = -55;
        addIndexOp(i, l);
      }
      if (i == 30)
        stackChange(-l);
      else
        stackChange(-1 - l);
      if (l > this.itsData.itsMaxCalleeArgs)
        this.itsData.itsMaxCalleeArgs = l;
      break;
    case 100:
    case 101:
      visitExpression(localNode1, 0);
      addIcode(-1);
      stackChange(1);
      l = this.itsICodeTop;
      i2 = (i == 101) ? 7 : 6;
      addForwardGoto(i2);
      stackChange(-1);
      addIcode(-4);
      stackChange(-1);
      localNode1 = localNode1.getNext();
      visitExpression(localNode1, paramInt & 0x1);
      resolveForwardGoto(l);
      break;
    case 98:
      localObject = localNode1.getNext();
      Node localNode3 = ((Node)localObject).getNext();
      visitExpression(localNode1, 0);
      i4 = this.itsICodeTop;
      addForwardGoto(7);
      stackChange(-1);
      visitExpression((Node)localObject, paramInt & 0x1);
      i5 = this.itsICodeTop;
      addForwardGoto(5);
      resolveForwardGoto(i4);
      this.itsStackDepth = j;
      visitExpression(localNode3, paramInt & 0x1);
      resolveForwardGoto(i5);
      break;
    case 33:
      visitExpression(localNode1, 0);
      localNode1 = localNode1.getNext();
      addStringOp(33, localNode1.getString());
      break;
    case 9:
    case 10:
    case 11:
    case 12:
    case 13:
    case 14:
    case 15:
    case 16:
    case 17:
    case 18:
    case 19:
    case 20:
    case 21:
    case 22:
    case 23:
    case 24:
    case 25:
    case 31:
    case 35:
    case 45:
    case 46:
    case 51:
    case 52:
      visitExpression(localNode1, 0);
      localNode1 = localNode1.getNext();
      visitExpression(localNode1, 0);
      addToken(i);
      stackChange(-1);
      break;
    case 26:
    case 27:
    case 28:
    case 29:
    case 32:
    case 122:
      visitExpression(localNode1, 0);
      if (i == 122)
      {
        addIcode(-4);
        addIcode(-50);
      }
      else
      {
        addToken(i);
      }
      break;
    case 65:
    case 67:
      visitExpression(localNode1, 0);
      addToken(i);
      break;
    case 34:
    case 135:
      visitExpression(localNode1, 0);
      localNode1 = localNode1.getNext();
      localObject = localNode1.getString();
      localNode1 = localNode1.getNext();
      if (i == 135)
      {
        addIcode(-1);
        stackChange(1);
        addStringOp(33, (String)localObject);
        stackChange(-1);
      }
      visitExpression(localNode1, 0);
      addStringOp(34, (String)localObject);
      stackChange(-1);
      break;
    case 36:
    case 136:
      visitExpression(localNode1, 0);
      localNode1 = localNode1.getNext();
      visitExpression(localNode1, 0);
      localNode1 = localNode1.getNext();
      if (i == 136)
      {
        addIcode(-2);
        stackChange(2);
        addToken(35);
        stackChange(-1);
        stackChange(-1);
      }
      visitExpression(localNode1, 0);
      addToken(36);
      stackChange(-2);
      break;
    case 66:
    case 138:
      visitExpression(localNode1, 0);
      localNode1 = localNode1.getNext();
      if (i == 138)
      {
        addIcode(-1);
        stackChange(1);
        addToken(65);
        stackChange(-1);
      }
      visitExpression(localNode1, 0);
      addToken(66);
      stackChange(-1);
      break;
    case 8:
      localObject = localNode1.getString();
      visitExpression(localNode1, 0);
      localNode1 = localNode1.getNext();
      visitExpression(localNode1, 0);
      addStringOp(8, (String)localObject);
      stackChange(-1);
      break;
    case 133:
      localObject = paramNode.getString();
      i3 = -1;
      if ((this.itsInFunctionFlag) && (!(this.itsData.itsNeedsActivation)))
        i3 = this.scriptOrFn.getParamOrVarIndex((String)localObject);
      if (i3 == -1)
      {
        addStringOp(-14, (String)localObject);
        stackChange(1);
      }
      else
      {
        addVarOp(54, i3);
        stackChange(1);
        addToken(32);
      }
      break;
    case 38:
    case 40:
    case 48:
      addStringOp(i, paramNode.getString());
      stackChange(1);
      break;
    case 102:
    case 103:
      visitIncDec(paramNode, localNode1);
      break;
    case 39:
      double d = paramNode.getDouble();
      i4 = (int)d;
      if (i4 == d)
      {
        if (i4 == 0)
        {
          addIcode(-51);
          if (1D / d < 0D)
            addToken(29);
        }
        else if (i4 == 1)
        {
          addIcode(-52);
        }
        else if ((short)i4 == i4)
        {
          addIcode(-27);
          addUint16(i4 & 0xFFFF);
        }
        else
        {
          addIcode(-28);
          addInt(i4);
        }
      }
      else
      {
        i5 = getDoubleIndex(d);
        addIndexOp(39, i5);
      }
      stackChange(1);
      break;
    case 54:
      if (this.itsData.itsNeedsActivation)
        Kit.codeBug();
      str = paramNode.getString();
      i3 = this.scriptOrFn.getParamOrVarIndex(str);
      addVarOp(54, i3);
      stackChange(1);
      break;
    case 55:
      if (this.itsData.itsNeedsActivation)
        Kit.codeBug();
      str = localNode1.getString();
      localNode1 = localNode1.getNext();
      visitExpression(localNode1, 0);
      i3 = this.scriptOrFn.getParamOrVarIndex(str);
      addVarOp(55, i3);
      break;
    case 41:
    case 42:
    case 43:
    case 44:
    case 61:
      addToken(i);
      stackChange(1);
      break;
    case 59:
    case 60:
      addIndexOp(i, getLocalBlockRef(paramNode));
      stackChange(1);
      break;
    case 47:
      i1 = paramNode.getExistingIntProp(4);
      addIndexOp(47, i1);
      stackChange(1);
      break;
    case 63:
    case 64:
      visitLiteral(paramNode, localNode1);
      break;
    case 69:
      visitExpression(localNode1, 0);
      addStringOp(i, (String)paramNode.getProp(17));
      break;
    case 73:
    case 74:
    case 75:
    case 76:
      i1 = paramNode.getIntProp(16, 0);
      i3 = 0;
      do
      {
        visitExpression(localNode1, 0);
        ++i3;
        localNode1 = localNode1.getNext();
      }
      while (localNode1 != null);
      addIndexOp(i, i1);
      stackChange(1 - i3);
      break;
    case 142:
      updateLineNumber(paramNode);
      visitExpression(localNode1, 0);
      addIcode(-53);
      stackChange(-1);
      i1 = this.itsICodeTop;
      visitExpression(localNode1.getNext(), 0);
      addBackwardGoto(-54, i1);
      break;
    case 70:
    case 71:
    case 72:
      visitExpression(localNode1, 0);
      addToken(i);
      break;
    case 49:
    case 50:
    case 56:
    case 57:
    case 58:
    case 62:
    case 77:
    case 78:
    case 79:
    case 80:
    case 81:
    case 82:
    case 83:
    case 84:
    case 86:
    case 87:
    case 88:
    case 89:
    case 90:
    case 91:
    case 92:
    case 93:
    case 94:
    case 95:
    case 96:
    case 97:
    case 99:
    case 104:
    case 106:
    case 107:
    case 108:
    case 109:
    case 110:
    case 111:
    case 112:
    case 113:
    case 114:
    case 115:
    case 116:
    case 117:
    case 118:
    case 119:
    case 120:
    case 121:
    case 123:
    case 124:
    case 125:
    case 126:
    case 127:
    case 128:
    case 129:
    case 130:
    case 131:
    case 132:
    case 137:
    case 139:
    case 140:
    case 141:
    default:
      throw badTree(paramNode);
    }
    if (j + 1 != this.itsStackDepth)
      Kit.codeBug();
  }

  private void generateCallFunAndThis(Node paramNode)
  {
    Object localObject;
    int i = paramNode.getType();
    switch (i)
    {
    case 38:
      localObject = paramNode.getString();
      addStringOp(-15, (String)localObject);
      stackChange(2);
      break;
    case 33:
    case 35:
      localObject = paramNode.getFirstChild();
      visitExpression((Node)localObject, 0);
      Node localNode = ((Node)localObject).getNext();
      if (i == 33)
      {
        String str = localNode.getString();
        addStringOp(-16, str);
        stackChange(1);
        return;
      }
      visitExpression(localNode, 0);
      addIcode(-17);
      break;
    default:
      visitExpression(paramNode, 0);
      addIcode(-18);
      stackChange(1);
    }
  }

  private void visitIncDec(Node paramNode1, Node paramNode2)
  {
    Object localObject1;
    Object localObject2;
    int i = paramNode1.getExistingIntProp(13);
    int j = paramNode2.getType();
    switch (j)
    {
    case 54:
      if (this.itsData.itsNeedsActivation)
        Kit.codeBug();
      localObject1 = paramNode2.getString();
      int k = this.scriptOrFn.getParamOrVarIndex((String)localObject1);
      addVarOp(-7, k);
      addUint8(i);
      stackChange(1);
      break;
    case 38:
      localObject1 = paramNode2.getString();
      addStringOp(-8, (String)localObject1);
      addUint8(i);
      stackChange(1);
      break;
    case 33:
      localObject1 = paramNode2.getFirstChild();
      visitExpression((Node)localObject1, 0);
      localObject2 = ((Node)localObject1).getNext().getString();
      addStringOp(-9, (String)localObject2);
      addUint8(i);
      break;
    case 35:
      localObject1 = paramNode2.getFirstChild();
      visitExpression((Node)localObject1, 0);
      localObject2 = ((Node)localObject1).getNext();
      visitExpression((Node)localObject2, 0);
      addIcode(-10);
      addUint8(i);
      stackChange(-1);
      break;
    case 65:
      localObject1 = paramNode2.getFirstChild();
      visitExpression((Node)localObject1, 0);
      addIcode(-11);
      addUint8(i);
      break;
    default:
      throw badTree(paramNode1);
    }
  }

  private void visitLiteral(Node paramNode1, Node paramNode2)
  {
    int j;
    Object localObject;
    int i = paramNode1.getType();
    Object[] arrayOfObject = null;
    if (i == 63)
    {
      j = 0;
      for (localObject = paramNode2; localObject != null; localObject = ((Node)localObject).getNext())
        ++j;
    }
    else if (i == 64)
    {
      arrayOfObject = (Object[])(Object[])paramNode1.getProp(12);
      j = arrayOfObject.length;
    }
    else
    {
      throw badTree(paramNode1);
    }
    addIndexOp(-29, j);
    stackChange(1);
    while (paramNode2 != null)
    {
      visitExpression(paramNode2, 0);
      addIcode(-30);
      stackChange(-1);
      paramNode2 = paramNode2.getNext();
    }
    if (i == 63)
    {
      localObject = (int[])(int[])paramNode1.getProp(11);
      if (localObject == null)
      {
        addToken(63);
      }
      else
      {
        int l = this.itsLiteralIds.size();
        this.itsLiteralIds.add(localObject);
        addIndexOp(-31, l);
      }
    }
    else
    {
      int k = this.itsLiteralIds.size();
      this.itsLiteralIds.add(arrayOfObject);
      addIndexOp(64, k);
    }
  }

  private int getLocalBlockRef(Node paramNode)
  {
    Node localNode = (Node)paramNode.getProp(3);
    return localNode.getExistingIntProp(2);
  }

  private int getTargetLabel(Node paramNode)
  {
    int i = paramNode.labelId();
    if (i != -1)
      return i;
    i = this.itsLabelTableTop;
    if ((this.itsLabelTable == null) || (i == this.itsLabelTable.length))
      if (this.itsLabelTable == null)
      {
        this.itsLabelTable = new int[32];
      }
      else
      {
        int[] arrayOfInt = new int[this.itsLabelTable.length * 2];
        System.arraycopy(this.itsLabelTable, 0, arrayOfInt, 0, i);
        this.itsLabelTable = arrayOfInt;
      }
    this.itsLabelTableTop = (i + 1);
    this.itsLabelTable[i] = -1;
    paramNode.labelId(i);
    return i;
  }

  private void markTargetLabel(Node paramNode)
  {
    int i = getTargetLabel(paramNode);
    if (this.itsLabelTable[i] != -1)
      Kit.codeBug();
    this.itsLabelTable[i] = this.itsICodeTop;
  }

  private void addGoto(Node paramNode, int paramInt)
  {
    int i = getTargetLabel(paramNode);
    if (i >= this.itsLabelTableTop)
      Kit.codeBug();
    int j = this.itsLabelTable[i];
    int k = this.itsICodeTop;
    if (validIcode(paramInt))
      addIcode(paramInt);
    else
      addToken(paramInt);
    if (j != -1)
    {
      recordJump(k, j);
      this.itsICodeTop += 2;
    }
    else
    {
      addUint16(0);
      int l = this.itsFixupTableTop;
      if ((this.itsFixupTable == null) || (l == this.itsFixupTable.length))
        if (this.itsFixupTable == null)
        {
          this.itsFixupTable = new long[40];
        }
        else
        {
          long[] arrayOfLong = new long[this.itsFixupTable.length * 2];
          System.arraycopy(this.itsFixupTable, 0, arrayOfLong, 0, l);
          this.itsFixupTable = arrayOfLong;
        }
      this.itsFixupTableTop = (l + 1);
      this.itsFixupTable[l] = (i << 32 | k);
    }
  }

  private void fixLabelGotos()
  {
    for (int i = 0; i < this.itsFixupTableTop; ++i)
    {
      long l = this.itsFixupTable[i];
      int j = (int)(l >> 32);
      int k = (int)l;
      int i1 = this.itsLabelTable[j];
      if (i1 == -1)
        throw Kit.codeBug();
      recordJump(k, i1);
    }
    this.itsFixupTableTop = 0;
  }

  private void addBackwardGoto(int paramInt1, int paramInt2)
  {
    if (paramInt2 >= this.itsICodeTop)
      throw Kit.codeBug();
    int i = this.itsICodeTop;
    addIcode(paramInt1);
    recordJump(i, paramInt2);
    this.itsICodeTop += 2;
  }

  private void addForwardGoto(int paramInt)
  {
    addToken(paramInt);
    addUint16(0);
  }

  private void resolveForwardGoto(int paramInt)
  {
    if (paramInt + 3 > this.itsICodeTop)
      throw Kit.codeBug();
    recordJump(paramInt, this.itsICodeTop);
  }

  private void recordJump(int paramInt1, int paramInt2)
  {
    if (paramInt1 == paramInt2)
      throw Kit.codeBug();
    int i = paramInt1 + 1;
    int j = paramInt2 - paramInt1;
    if (j != (short)j)
    {
      if (this.itsData.longJumps == null)
        this.itsData.longJumps = new UintMap();
      this.itsData.longJumps.put(i, paramInt2);
      j = 0;
    }
    int k = i + 2 - this.itsData.itsICode.length;
    if (k > 0)
      increaseICodeCapasity(k);
    this.itsData.itsICode[i] = (byte)(j >> 8);
    this.itsData.itsICode[(i + 1)] = (byte)j;
  }

  private void addToken(int paramInt)
  {
    if (!(validTokenCode(paramInt)))
      throw Kit.codeBug();
    addUint8(paramInt);
  }

  private void addIcode(int paramInt)
  {
    if (!(validIcode(paramInt)))
      throw Kit.codeBug();
    addUint8(paramInt & 0xFF);
  }

  private void addUint8(int paramInt)
  {
    if ((paramInt & 0xFFFFFF00) != 0)
      throw Kit.codeBug();
    byte[] arrayOfByte = this.itsData.itsICode;
    int i = this.itsICodeTop;
    if (i == arrayOfByte.length)
      arrayOfByte = increaseICodeCapasity(1);
    arrayOfByte[i] = (byte)paramInt;
    this.itsICodeTop = (i + 1);
  }

  private void addUint16(int paramInt)
  {
    if ((paramInt & 0xFFFF0000) != 0)
      throw Kit.codeBug();
    byte[] arrayOfByte = this.itsData.itsICode;
    int i = this.itsICodeTop;
    if (i + 2 > arrayOfByte.length)
      arrayOfByte = increaseICodeCapasity(2);
    arrayOfByte[i] = (byte)(paramInt >>> 8);
    arrayOfByte[(i + 1)] = (byte)paramInt;
    this.itsICodeTop = (i + 2);
  }

  private void addInt(int paramInt)
  {
    byte[] arrayOfByte = this.itsData.itsICode;
    int i = this.itsICodeTop;
    if (i + 4 > arrayOfByte.length)
      arrayOfByte = increaseICodeCapasity(4);
    arrayOfByte[i] = (byte)(paramInt >>> 24);
    arrayOfByte[(i + 1)] = (byte)(paramInt >>> 16);
    arrayOfByte[(i + 2)] = (byte)(paramInt >>> 8);
    arrayOfByte[(i + 3)] = (byte)paramInt;
    this.itsICodeTop = (i + 4);
  }

  private int getDoubleIndex(double paramDouble)
  {
    int i = this.itsDoubleTableTop;
    if (i == 0)
    {
      this.itsData.itsDoubleTable = new double[64];
    }
    else if (this.itsData.itsDoubleTable.length == i)
    {
      double[] arrayOfDouble = new double[i * 2];
      System.arraycopy(this.itsData.itsDoubleTable, 0, arrayOfDouble, 0, i);
      this.itsData.itsDoubleTable = arrayOfDouble;
    }
    this.itsData.itsDoubleTable[i] = paramDouble;
    this.itsDoubleTableTop = (i + 1);
    return i;
  }

  private void addVarOp(int paramInt1, int paramInt2)
  {
    switch (paramInt1)
    {
    case 54:
    case 55:
      if (paramInt2 >= 128)
        break label66;
      addIcode((paramInt1 == 54) ? -48 : -49);
      addUint8(paramInt2);
      return;
    case -7:
      label66: addIndexOp(paramInt1, paramInt2);
      return;
    }
    throw Kit.codeBug();
  }

  private void addStringOp(int paramInt, String paramString)
  {
    addStringPrefix(paramString);
    if (validIcode(paramInt))
      addIcode(paramInt);
    else
      addToken(paramInt);
  }

  private void addIndexOp(int paramInt1, int paramInt2)
  {
    addIndexPrefix(paramInt2);
    if (validIcode(paramInt1))
      addIcode(paramInt1);
    else
      addToken(paramInt1);
  }

  private void addStringPrefix(String paramString)
  {
    int i = this.itsStrings.get(paramString, -1);
    if (i == -1)
    {
      i = this.itsStrings.size();
      this.itsStrings.put(paramString, i);
    }
    if (i < 4)
    {
      addIcode(-41 - i);
    }
    else if (i <= 255)
    {
      addIcode(-45);
      addUint8(i);
    }
    else if (i <= 65535)
    {
      addIcode(-46);
      addUint16(i);
    }
    else
    {
      addIcode(-47);
      addInt(i);
    }
  }

  private void addIndexPrefix(int paramInt)
  {
    if (paramInt < 0)
      Kit.codeBug();
    if (paramInt < 6)
    {
      addIcode(-32 - paramInt);
    }
    else if (paramInt <= 255)
    {
      addIcode(-38);
      addUint8(paramInt);
    }
    else if (paramInt <= 65535)
    {
      addIcode(-39);
      addUint16(paramInt);
    }
    else
    {
      addIcode(-40);
      addInt(paramInt);
    }
  }

  private void addExceptionHandler(int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean, int paramInt4, int paramInt5)
  {
    int i = this.itsExceptionTableTop;
    int[] arrayOfInt = this.itsData.itsExceptionTable;
    if (arrayOfInt == null)
    {
      if (i != 0)
        Kit.codeBug();
      arrayOfInt = new int[12];
      this.itsData.itsExceptionTable = arrayOfInt;
    }
    else if (arrayOfInt.length == i)
    {
      arrayOfInt = new int[arrayOfInt.length * 2];
      System.arraycopy(this.itsData.itsExceptionTable, 0, arrayOfInt, 0, i);
      this.itsData.itsExceptionTable = arrayOfInt;
    }
    arrayOfInt[(i + 0)] = paramInt1;
    arrayOfInt[(i + 1)] = paramInt2;
    arrayOfInt[(i + 2)] = paramInt3;
    arrayOfInt[(i + 3)] = ((paramBoolean) ? 1 : 0);
    arrayOfInt[(i + 4)] = paramInt4;
    arrayOfInt[(i + 5)] = paramInt5;
    this.itsExceptionTableTop = (i + 6);
  }

  private byte[] increaseICodeCapasity(int paramInt)
  {
    int i = this.itsData.itsICode.length;
    int j = this.itsICodeTop;
    if (j + paramInt <= i)
      throw Kit.codeBug();
    i *= 2;
    if (j + paramInt > i)
      i = j + paramInt;
    byte[] arrayOfByte = new byte[i];
    System.arraycopy(this.itsData.itsICode, 0, arrayOfByte, 0, j);
    this.itsData.itsICode = arrayOfByte;
    return arrayOfByte;
  }

  private void stackChange(int paramInt)
  {
    if (paramInt <= 0)
    {
      this.itsStackDepth += paramInt;
    }
    else
    {
      int i = this.itsStackDepth + paramInt;
      if (i > this.itsData.itsMaxStack)
        this.itsData.itsMaxStack = i;
      this.itsStackDepth = i;
    }
  }

  private int allocLocal()
  {
    int i = this.itsLocalTop;
    this.itsLocalTop += 1;
    if (this.itsLocalTop > this.itsData.itsMaxLocals)
      this.itsData.itsMaxLocals = this.itsLocalTop;
    return i;
  }

  private void releaseLocal(int paramInt)
  {
    this.itsLocalTop -= 1;
    if (paramInt != this.itsLocalTop)
      Kit.codeBug();
  }

  private static int getShort(byte[] paramArrayOfByte, int paramInt)
  {
    return (paramArrayOfByte[paramInt] << 8 | paramArrayOfByte[(paramInt + 1)] & 0xFF);
  }

  private static int getIndex(byte[] paramArrayOfByte, int paramInt)
  {
    return ((paramArrayOfByte[paramInt] & 0xFF) << 8 | paramArrayOfByte[(paramInt + 1)] & 0xFF);
  }

  private static int getInt(byte[] paramArrayOfByte, int paramInt)
  {
    return (paramArrayOfByte[paramInt] << 24 | (paramArrayOfByte[(paramInt + 1)] & 0xFF) << 16 | (paramArrayOfByte[(paramInt + 2)] & 0xFF) << 8 | paramArrayOfByte[(paramInt + 3)] & 0xFF);
  }

  private static int getExceptionHandler(CallFrame paramCallFrame, boolean paramBoolean)
  {
    int[] arrayOfInt = paramCallFrame.idata.itsExceptionTable;
    if (arrayOfInt == null)
      return -1;
    int i = paramCallFrame.pc - 1;
    int j = -1;
    int k = 0;
    int l = 0;
    for (int i1 = 0; i1 != arrayOfInt.length; i1 += 6)
    {
      int i2 = arrayOfInt[(i1 + 0)];
      int i3 = arrayOfInt[(i1 + 1)];
      if (i2 <= i)
      {
        if (i >= i3)
          break label137:
        if ((paramBoolean) && (arrayOfInt[(i1 + 3)] != 1))
          break label137:
        if (j >= 0)
        {
          if (l < i3)
            break label137:
          if (k > i2)
            Kit.codeBug();
          if (l == i3)
            Kit.codeBug();
        }
        j = i1;
        k = i2;
        label137: l = i3;
      }
    }
    return j;
  }

  private static void dumpICode(InterpreterData paramInterpreterData)
  {
  }

  private static int bytecodeSpan(int paramInt)
  {
    switch (paramInt)
    {
    case 49:
      return 3;
    case -54:
    case -23:
    case -6:
    case 5:
    case 6:
    case 7:
      return 3;
    case -21:
      return 5;
    case 56:
      return 2;
    case -11:
    case -10:
    case -9:
    case -8:
    case -7:
      return 2;
    case -27:
      return 3;
    case -28:
      return 5;
    case -38:
      return 2;
    case -39:
      return 3;
    case -40:
      return 5;
    case -45:
      return 2;
    case -46:
      return 3;
    case -47:
      return 5;
    case -49:
    case -48:
      return 2;
    case -26:
      return 3;
    case -53:
    case -52:
    case -51:
    case -50:
    case -44:
    case -43:
    case -42:
    case -41:
    case -37:
    case -36:
    case -35:
    case -34:
    case -33:
    case -32:
    case -31:
    case -30:
    case -29:
    case -25:
    case -24:
    case -22:
    case -20:
    case -19:
    case -18:
    case -17:
    case -16:
    case -15:
    case -14:
    case -13:
    case -12:
    case -5:
    case -4:
    case -3:
    case -2:
    case -1:
    case 0:
    case 1:
    case 2:
    case 3:
    case 4:
    case 8:
    case 9:
    case 10:
    case 11:
    case 12:
    case 13:
    case 14:
    case 15:
    case 16:
    case 17:
    case 18:
    case 19:
    case 20:
    case 21:
    case 22:
    case 23:
    case 24:
    case 25:
    case 26:
    case 27:
    case 28:
    case 29:
    case 30:
    case 31:
    case 32:
    case 33:
    case 34:
    case 35:
    case 36:
    case 37:
    case 38:
    case 39:
    case 40:
    case 41:
    case 42:
    case 43:
    case 44:
    case 45:
    case 46:
    case 47:
    case 48:
    case 50:
    case 51:
    case 52:
    case 53:
    case 54:
    case 55:
    }
    if (!(validBytecode(paramInt)))
      throw Kit.codeBug();
    return 1;
  }

  static int[] getLineNumbers(InterpreterData paramInterpreterData)
  {
    UintMap localUintMap = new UintMap();
    byte[] arrayOfByte = paramInterpreterData.itsICode;
    int i = arrayOfByte.length;
    int j = 0;
    while (j != i)
    {
      int k = arrayOfByte[j];
      int l = bytecodeSpan(k);
      if (k == -26)
      {
        if (l != 3)
          Kit.codeBug();
        int i1 = getIndex(arrayOfByte, j + 1);
        localUintMap.put(i1, 0);
      }
      j += l;
    }
    return localUintMap.getKeys();
  }

  static void captureInterpreterStackInfo(RhinoException paramRhinoException)
  {
    CallFrame[] arrayOfCallFrame;
    Context localContext = Context.getCurrentContext();
    if ((localContext == null) || (localContext.lastInterpreterFrame == null))
    {
      paramRhinoException.interpreterStackInfo = null;
      paramRhinoException.interpreterLineData = null;
      return;
    }
    if ((localContext.previousInterpreterInvocations == null) || (localContext.previousInterpreterInvocations.size() == 0))
    {
      arrayOfCallFrame = new CallFrame[1];
    }
    else
    {
      i = localContext.previousInterpreterInvocations.size();
      if (localContext.previousInterpreterInvocations.peek() == localContext.lastInterpreterFrame)
        --i;
      arrayOfCallFrame = new CallFrame[i + 1];
      localContext.previousInterpreterInvocations.toArray(arrayOfCallFrame);
    }
    arrayOfCallFrame[(arrayOfCallFrame.length - 1)] = ((CallFrame)localContext.lastInterpreterFrame);
    int i = 0;
    for (int j = 0; j != arrayOfCallFrame.length; ++j)
      i += 1 + arrayOfCallFrame[j].frameIndex;
    int[] arrayOfInt = new int[i];
    int k = i;
    int l = arrayOfCallFrame.length;
    while (l != 0)
      for (CallFrame localCallFrame = arrayOfCallFrame[(--l)]; localCallFrame != null; localCallFrame = localCallFrame.parentFrame)
        arrayOfInt[(--k)] = localCallFrame.pcSourceLineStart;
    if (k != 0)
      Kit.codeBug();
    paramRhinoException.interpreterStackInfo = arrayOfCallFrame;
    paramRhinoException.interpreterLineData = arrayOfInt;
  }

  static String getSourcePositionFromStack(Context paramContext, int[] paramArrayOfInt)
  {
    CallFrame localCallFrame = (CallFrame)paramContext.lastInterpreterFrame;
    InterpreterData localInterpreterData = localCallFrame.idata;
    if (localCallFrame.pcSourceLineStart >= 0)
      paramArrayOfInt[0] = getIndex(localInterpreterData.itsICode, localCallFrame.pcSourceLineStart);
    else
      paramArrayOfInt[0] = 0;
    return localInterpreterData.itsSourceFile;
  }

  static String getPatchedStack(RhinoException paramRhinoException, String paramString)
  {
    String str1 = "sun.org.mozilla.javascript.internal.Interpreter.interpretLoop";
    StringBuffer localStringBuffer = new StringBuffer(paramString.length() + 1000);
    String str2 = System.getProperty("line.separator");
    CallFrame[] arrayOfCallFrame = (CallFrame[])(CallFrame[])paramRhinoException.interpreterStackInfo;
    int[] arrayOfInt = paramRhinoException.interpreterLineData;
    int i = arrayOfCallFrame.length;
    int j = arrayOfInt.length;
    int k = 0;
    while (i != 0)
    {
      --i;
      int l = paramString.indexOf(str1, k);
      if (l < 0)
        break;
      l += str1.length();
      while (l != paramString.length())
      {
        int i1 = paramString.charAt(l);
        if (i1 == 10)
          break;
        if (i1 == 13)
          break;
        ++l;
      }
      localStringBuffer.append(paramString.substring(k, l));
      k = l;
      for (CallFrame localCallFrame = arrayOfCallFrame[i]; localCallFrame != null; localCallFrame = localCallFrame.parentFrame)
      {
        if (j == 0)
          Kit.codeBug();
        --j;
        InterpreterData localInterpreterData = localCallFrame.idata;
        localStringBuffer.append(str2);
        localStringBuffer.append("\tat script");
        if ((localInterpreterData.itsName != null) && (localInterpreterData.itsName.length() != 0))
        {
          localStringBuffer.append('.');
          localStringBuffer.append(localInterpreterData.itsName);
        }
        localStringBuffer.append('(');
        localStringBuffer.append(localInterpreterData.itsSourceFile);
        int i2 = arrayOfInt[j];
        if (i2 >= 0)
        {
          localStringBuffer.append(':');
          localStringBuffer.append(getIndex(localInterpreterData.itsICode, i2));
        }
        localStringBuffer.append(')');
      }
    }
    localStringBuffer.append(paramString.substring(k));
    return localStringBuffer.toString();
  }

  static String getEncodedSource(InterpreterData paramInterpreterData)
  {
    if (paramInterpreterData.encodedSource == null)
      return null;
    return paramInterpreterData.encodedSource.substring(paramInterpreterData.encodedSourceStart, paramInterpreterData.encodedSourceEnd);
  }

  private static void initFunction(Context paramContext, Scriptable paramScriptable, InterpretedFunction paramInterpretedFunction, int paramInt)
  {
    InterpretedFunction localInterpretedFunction = InterpretedFunction.createFunction(paramContext, paramScriptable, paramInterpretedFunction, paramInt);
    ScriptRuntime.initFunction(paramContext, paramScriptable, localInterpretedFunction, localInterpretedFunction.idata.itsFunctionType, paramInterpretedFunction.idata.evalScriptFlag);
  }

  static Object interpret(InterpretedFunction paramInterpretedFunction, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(ScriptRuntime.hasTopCall(paramContext)))
      Kit.codeBug();
    if (paramContext.interpreterSecurityDomain != paramInterpretedFunction.securityDomain)
    {
      localObject1 = paramContext.interpreterSecurityDomain;
      paramContext.interpreterSecurityDomain = paramInterpretedFunction.securityDomain;
      try
      {
        Object localObject2 = paramInterpretedFunction.securityController.callWithDomain(paramInterpretedFunction.securityDomain, paramContext, paramInterpretedFunction, paramScriptable1, paramScriptable2, paramArrayOfObject);
        return localObject2;
      }
      finally
      {
        paramContext.interpreterSecurityDomain = localObject1;
      }
    }
    Object localObject1 = new CallFrame(null);
    initFrame(paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject, null, 0, paramArrayOfObject.length, paramInterpretedFunction, null, (CallFrame)localObject1);
    return interpretLoop(paramContext, (CallFrame)localObject1, null);
  }

  public static Object restartContinuation(Continuation paramContinuation, Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    Object localObject;
    if (!(ScriptRuntime.hasTopCall(paramContext)))
      return ScriptRuntime.doTopCall(paramContinuation, paramContext, paramScriptable, null, paramArrayOfObject);
    if (paramArrayOfObject.length == 0)
      localObject = Undefined.instance;
    else
      localObject = paramArrayOfObject[0];
    CallFrame localCallFrame = (CallFrame)paramContinuation.getImplementation();
    if (localCallFrame == null)
      return localObject;
    ContinuationJump localContinuationJump = new ContinuationJump(paramContinuation, null);
    localContinuationJump.result = localObject;
    return interpretLoop(paramContext, null, localContinuationJump);
  }

  private static Object interpretLoop(Context paramContext, CallFrame paramCallFrame, Object paramObject)
  {
    Object localObject5;
    UniqueTag localUniqueTag = UniqueTag.DOUBLE_MARK;
    Object localObject1 = Undefined.instance;
    int i = (paramContext.instructionThreshold != 0) ? 1 : 0;
    String str = null;
    int j = -1;
    if (paramContext.lastInterpreterFrame != null)
    {
      if (paramContext.previousInterpreterInvocations == null)
        paramContext.previousInterpreterInvocations = new ObjArray();
      paramContext.previousInterpreterInvocations.push(paramContext.lastInterpreterFrame);
    }
    if ((paramObject != null) && (!(paramObject instanceof ContinuationJump)))
      Kit.codeBug();
    Object localObject2 = null;
    double d1 = 0D;
    try
    {
      while (true)
      {
        if (paramObject != null)
        {
          int k;
          int l;
          if (j >= 0)
          {
            if (paramCallFrame.frozen)
              paramCallFrame = paramCallFrame.cloneFrozen();
            localObject3 = paramCallFrame.idata.itsExceptionTable;
            paramCallFrame.pc = localObject3[(j + 2)];
            if (i != 0)
              paramCallFrame.pcPrevBranch = paramCallFrame.pc;
            paramCallFrame.savedStackTop = paramCallFrame.emptyStackTop;
            k = paramCallFrame.localShift + localObject3[(j + 5)];
            l = paramCallFrame.localShift + localObject3[(j + 4)];
            paramCallFrame.scope = ((Scriptable)paramCallFrame.stack[k]);
            paramCallFrame.stack[l] = paramObject;
            paramObject = null;
          }
          else
          {
            localObject3 = (ContinuationJump)paramObject;
            paramObject = null;
            if (((ContinuationJump)localObject3).branchFrame != paramCallFrame)
              Kit.codeBug();
            if (((ContinuationJump)localObject3).capturedFrame == null)
              Kit.codeBug();
            k = ((ContinuationJump)localObject3).capturedFrame.frameIndex + 1;
            if (((ContinuationJump)localObject3).branchFrame != null)
              k -= ((ContinuationJump)localObject3).branchFrame.frameIndex;
            l = 0;
            localObject4 = null;
            localObject5 = ((ContinuationJump)localObject3).capturedFrame;
            for (int i2 = 0; i2 != k; ++i2)
            {
              if (!(((CallFrame)localObject5).frozen))
                Kit.codeBug();
              if (isFrameEnterExitRequired((CallFrame)localObject5))
              {
                if (localObject4 == null)
                  localObject4 = new CallFrame[k - i2];
                localObject4[l] = localObject5;
                ++l;
              }
              localObject5 = ((CallFrame)localObject5).parentFrame;
            }
            while (l != 0)
            {
              localObject5 = localObject4[(--l)];
              enterFrame(paramContext, (CallFrame)localObject5, ScriptRuntime.emptyArgs);
            }
            paramCallFrame = ((ContinuationJump)localObject3).capturedFrame.cloneFrozen();
            setCallResult(paramCallFrame, ((ContinuationJump)localObject3).result, ((ContinuationJump)localObject3).resultDbl);
          }
          if (paramObject != null)
            Kit.codeBug();
        }
        else if (paramCallFrame.frozen)
        {
          Kit.codeBug();
        }
        Object localObject3 = paramCallFrame.stack;
        double[] arrayOfDouble = paramCallFrame.sDbl;
        Object[] arrayOfObject = paramCallFrame.varSource.stack;
        Object localObject4 = paramCallFrame.varSource.sDbl;
        localObject5 = paramCallFrame.idata.itsICode;
        String[] arrayOfString = paramCallFrame.idata.itsStringTable;
        int i3 = paramCallFrame.savedStackTop;
        paramContext.lastInterpreterFrame = paramCallFrame;
        while (true)
        {
          do
          {
            int i4;
            while (true)
            {
              while (true)
              {
                while (true)
                {
                  while (true)
                  {
                    while (true)
                    {
                      while (true)
                      {
                        while (true)
                        {
                          while (true)
                          {
                            while (true)
                            {
                              while (true)
                              {
                                while (true)
                                {
                                  while (true)
                                  {
                                    while (true)
                                    {
                                      while (true)
                                      {
                                        while (true)
                                        {
                                          while (true)
                                          {
                                            while (true)
                                            {
                                              while (true)
                                              {
                                                label1296: label1781: Number localNumber3;
                                                do
                                                {
                                                  while (true)
                                                  {
                                                    do
                                                    {
                                                      while (true)
                                                      {
                                                        while (true)
                                                        {
                                                          while (true)
                                                          {
                                                            while (true)
                                                            {
                                                              Object localObject17;
                                                              while (true)
                                                              {
                                                                while (true)
                                                                {
                                                                  while (true)
                                                                  {
                                                                    while (true)
                                                                    {
                                                                      while (true)
                                                                      {
                                                                        while (true)
                                                                        {
                                                                          while (true)
                                                                          {
                                                                            while (true)
                                                                            {
                                                                              while (true)
                                                                              {
                                                                                while (true)
                                                                                {
                                                                                  while (true)
                                                                                  {
                                                                                    while (true)
                                                                                    {
                                                                                      while (true)
                                                                                      {
                                                                                        while (true)
                                                                                        {
                                                                                          while (true)
                                                                                          {
                                                                                            while (true)
                                                                                            {
                                                                                              while (true)
                                                                                              {
                                                                                                while (true)
                                                                                                {
                                                                                                  while (true)
                                                                                                  {
                                                                                                    while (true)
                                                                                                    {
                                                                                                      while (true)
                                                                                                      {
                                                                                                        while (true)
                                                                                                        {
                                                                                                          while (true)
                                                                                                          {
                                                                                                            while (true)
                                                                                                            {
                                                                                                              while (true)
                                                                                                              {
                                                                                                                while (true)
                                                                                                                {
                                                                                                                  while (true)
                                                                                                                  {
                                                                                                                    while (true)
                                                                                                                    {
                                                                                                                      while (true)
                                                                                                                      {
                                                                                                                        while (true)
                                                                                                                        {
                                                                                                                          while (true)
                                                                                                                          {
                                                                                                                            while (true)
                                                                                                                            {
                                                                                                                              while (true)
                                                                                                                              {
                                                                                                                                while (true)
                                                                                                                                {
                                                                                                                                  while (true)
                                                                                                                                  {
                                                                                                                                    while (true)
                                                                                                                                    {
                                                                                                                                      while (true)
                                                                                                                                      {
                                                                                                                                        while (true)
                                                                                                                                        {
                                                                                                                                          while (true)
                                                                                                                                          {
                                                                                                                                            while (true)
                                                                                                                                            {
                                                                                                                                              while (true)
                                                                                                                                              {
                                                                                                                                                while (true)
                                                                                                                                                {
                                                                                                                                                  while (true)
                                                                                                                                                  {
                                                                                                                                                    while (true)
                                                                                                                                                    {
                                                                                                                                                      while (true)
                                                                                                                                                      {
                                                                                                                                                        while (true)
                                                                                                                                                        {
                                                                                                                                                          while (true)
                                                                                                                                                          {
                                                                                                                                                            while (true)
                                                                                                                                                            {
                                                                                                                                                              while (true)
                                                                                                                                                              {
                                                                                                                                                                while (true)
                                                                                                                                                                {
                                                                                                                                                                  while (true)
                                                                                                                                                                  {
                                                                                                                                                                    while (true)
                                                                                                                                                                    {
                                                                                                                                                                      while (true)
                                                                                                                                                                      {
                                                                                                                                                                        while (true)
                                                                                                                                                                        {
                                                                                                                                                                          while (true)
                                                                                                                                                                          {
                                                                                                                                                                            while (true)
                                                                                                                                                                            {
                                                                                                                                                                              while (true)
                                                                                                                                                                              {
                                                                                                                                                                                while (true)
                                                                                                                                                                                {
                                                                                                                                                                                  Object localObject15;
                                                                                                                                                                                  while (true)
                                                                                                                                                                                  {
                                                                                                                                                                                    double d7;
                                                                                                                                                                                    while (true)
                                                                                                                                                                                    {
                                                                                                                                                                                      while (true)
                                                                                                                                                                                      {
                                                                                                                                                                                        while (true)
                                                                                                                                                                                        {
                                                                                                                                                                                          while (true)
                                                                                                                                                                                          {
                                                                                                                                                                                            while (true)
                                                                                                                                                                                            {
                                                                                                                                                                                              while (true)
                                                                                                                                                                                              {
                                                                                                                                                                                                while (true)
                                                                                                                                                                                                {
                                                                                                                                                                                                  while (true)
                                                                                                                                                                                                  {
                                                                                                                                                                                                    while (true)
                                                                                                                                                                                                    {
                                                                                                                                                                                                      while (true)
                                                                                                                                                                                                      {
                                                                                                                                                                                                        while (true)
                                                                                                                                                                                                        {
                                                                                                                                                                                                          while (true)
                                                                                                                                                                                                          {
                                                                                                                                                                                                            while (true)
                                                                                                                                                                                                            {
                                                                                                                                                                                                              while (true)
                                                                                                                                                                                                              {
                                                                                                                                                                                                                while (true)
                                                                                                                                                                                                                {
                                                                                                                                                                                                                  while (true)
                                                                                                                                                                                                                  {
                                                                                                                                                                                                                    while (true)
                                                                                                                                                                                                                    {
                                                                                                                                                                                                                      while (true)
                                                                                                                                                                                                                      {
                                                                                                                                                                                                                        while (true)
                                                                                                                                                                                                                        {
                                                                                                                                                                                                                          do
                                                                                                                                                                                                                          {
                                                                                                                                                                                                                            while (true)
                                                                                                                                                                                                                            {
                                                                                                                                                                                                                              do
                                                                                                                                                                                                                                while (true)
                                                                                                                                                                                                                                {
                                                                                                                                                                                                                                  while (true)
                                                                                                                                                                                                                                  {
                                                                                                                                                                                                                                    while (true)
                                                                                                                                                                                                                                    {
                                                                                                                                                                                                                                      while (true)
                                                                                                                                                                                                                                      {
                                                                                                                                                                                                                                        while (true)
                                                                                                                                                                                                                                        {
                                                                                                                                                                                                                                          boolean bool5;
                                                                                                                                                                                                                                          double d10;
                                                                                                                                                                                                                                          while (true)
                                                                                                                                                                                                                                          {
                                                                                                                                                                                                                                            boolean bool2;
                                                                                                                                                                                                                                            while (true)
                                                                                                                                                                                                                                            {
                                                                                                                                                                                                                                              boolean bool4;
                                                                                                                                                                                                                                              while (true)
                                                                                                                                                                                                                                              {
                                                                                                                                                                                                                                                i4 = localObject5[(paramCallFrame.pc++)];
                                                                                                                                                                                                                                                switch (i4)
                                                                                                                                                                                                                                                {
                                                                                                                                                                                                                                                case 49:
                                                                                                                                                                                                                                                  localNumber1 = localObject3[i3];
                                                                                                                                                                                                                                                  if (localNumber1 == localUniqueTag)
                                                                                                                                                                                                                                                    localNumber1 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                                                                                  --i3;
                                                                                                                                                                                                                                                  int i10 = getIndex(localObject5, paramCallFrame.pc);
                                                                                                                                                                                                                                                  paramObject = new JavaScriptException(localNumber1, paramCallFrame.idata.itsSourceFile, i10);
                                                                                                                                                                                                                                                  break;
                                                                                                                                                                                                                                                case 50:
                                                                                                                                                                                                                                                  j += paramCallFrame.localShift;
                                                                                                                                                                                                                                                  paramObject = localObject3[j];
                                                                                                                                                                                                                                                  break;
                                                                                                                                                                                                                                                case 14:
                                                                                                                                                                                                                                                case 15:
                                                                                                                                                                                                                                                case 16:
                                                                                                                                                                                                                                                case 17:
                                                                                                                                                                                                                                                  localNumber1 = localObject3[(--i3 + 1)];
                                                                                                                                                                                                                                                  localObject10 = localObject3[i3];
                                                                                                                                                                                                                                                  if (localNumber1 == localUniqueTag)
                                                                                                                                                                                                                                                  {
                                                                                                                                                                                                                                                    d7 = arrayOfDouble[(i3 + 1)];
                                                                                                                                                                                                                                                    d10 = stack_double(paramCallFrame, i3);
                                                                                                                                                                                                                                                  }
                                                                                                                                                                                                                                                  else
                                                                                                                                                                                                                                                  {
                                                                                                                                                                                                                                                    if (localObject10 != localUniqueTag)
                                                                                                                                                                                                                                                      break label1296;
                                                                                                                                                                                                                                                    d7 = ScriptRuntime.toNumber(localNumber1);
                                                                                                                                                                                                                                                    d10 = arrayOfDouble[i3];
                                                                                                                                                                                                                                                  }
                                                                                                                                                                                                                                                  switch (i4)
                                                                                                                                                                                                                                                  {
                                                                                                                                                                                                                                                  case 17:
                                                                                                                                                                                                                                                    bool4 = d10 >= d7;
                                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                                  case 15:
                                                                                                                                                                                                                                                    bool4 = d10 <= d7;
                                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                                  case 16:
                                                                                                                                                                                                                                                    bool4 = d10 > d7;
                                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                                  case 14:
                                                                                                                                                                                                                                                    bool4 = d10 < d7;
                                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                                  default:
                                                                                                                                                                                                                                                    throw Kit.codeBug();
                                                                                                                                                                                                                                                    switch (i4)
                                                                                                                                                                                                                                                    {
                                                                                                                                                                                                                                                    case 17:
                                                                                                                                                                                                                                                      bool4 = ScriptRuntime.cmp_LE(localNumber1, localObject10);
                                                                                                                                                                                                                                                      break;
                                                                                                                                                                                                                                                    case 15:
                                                                                                                                                                                                                                                      bool4 = ScriptRuntime.cmp_LE(localObject10, localNumber1);
                                                                                                                                                                                                                                                      break;
                                                                                                                                                                                                                                                    case 16:
                                                                                                                                                                                                                                                      bool4 = ScriptRuntime.cmp_LT(localNumber1, localObject10);
                                                                                                                                                                                                                                                      break;
                                                                                                                                                                                                                                                    case 14:
                                                                                                                                                                                                                                                      bool4 = ScriptRuntime.cmp_LT(localObject10, localNumber1);
                                                                                                                                                                                                                                                      break;
                                                                                                                                                                                                                                                    default:
                                                                                                                                                                                                                                                      throw Kit.codeBug();
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                  }
                                                                                                                                                                                                                                                  localObject3[i3] = ScriptRuntime.wrapBoolean(bool4);
                                                                                                                                                                                                                                                case 51:
                                                                                                                                                                                                                                                case 52:
                                                                                                                                                                                                                                                case 12:
                                                                                                                                                                                                                                                case 13:
                                                                                                                                                                                                                                                case 45:
                                                                                                                                                                                                                                                case 46:
                                                                                                                                                                                                                                                case 7:
                                                                                                                                                                                                                                                case 6:
                                                                                                                                                                                                                                                case -6:
                                                                                                                                                                                                                                                case 5:
                                                                                                                                                                                                                                                case -23:
                                                                                                                                                                                                                                                case -24:
                                                                                                                                                                                                                                                case -25:
                                                                                                                                                                                                                                                case -4:
                                                                                                                                                                                                                                                case -5:
                                                                                                                                                                                                                                                case -1:
                                                                                                                                                                                                                                                case -2:
                                                                                                                                                                                                                                                case -3:
                                                                                                                                                                                                                                                case 4:
                                                                                                                                                                                                                                                case 62:
                                                                                                                                                                                                                                                case -22:
                                                                                                                                                                                                                                                case 27:
                                                                                                                                                                                                                                                case 9:
                                                                                                                                                                                                                                                case 10:
                                                                                                                                                                                                                                                case 11:
                                                                                                                                                                                                                                                case 18:
                                                                                                                                                                                                                                                case 19:
                                                                                                                                                                                                                                                case 20:
                                                                                                                                                                                                                                                case 28:
                                                                                                                                                                                                                                                case 29:
                                                                                                                                                                                                                                                case 21:
                                                                                                                                                                                                                                                case 22:
                                                                                                                                                                                                                                                case 23:
                                                                                                                                                                                                                                                case 24:
                                                                                                                                                                                                                                                case 25:
                                                                                                                                                                                                                                                case 26:
                                                                                                                                                                                                                                                case 48:
                                                                                                                                                                                                                                                case 8:
                                                                                                                                                                                                                                                case 31:
                                                                                                                                                                                                                                                case 33:
                                                                                                                                                                                                                                                case 34:
                                                                                                                                                                                                                                                case -9:
                                                                                                                                                                                                                                                case 35:
                                                                                                                                                                                                                                                case 36:
                                                                                                                                                                                                                                                case -10:
                                                                                                                                                                                                                                                case 65:
                                                                                                                                                                                                                                                case 66:
                                                                                                                                                                                                                                                case 67:
                                                                                                                                                                                                                                                case -11:
                                                                                                                                                                                                                                                case 53:
                                                                                                                                                                                                                                                case -56:
                                                                                                                                                                                                                                                case -15:
                                                                                                                                                                                                                                                case -16:
                                                                                                                                                                                                                                                case -17:
                                                                                                                                                                                                                                                case -18:
                                                                                                                                                                                                                                                case -21:
                                                                                                                                                                                                                                                case -55:
                                                                                                                                                                                                                                                case 37:
                                                                                                                                                                                                                                                case 68:
                                                                                                                                                                                                                                                case 30:
                                                                                                                                                                                                                                                case 32:
                                                                                                                                                                                                                                                case -14:
                                                                                                                                                                                                                                                case 40:
                                                                                                                                                                                                                                                case -27:
                                                                                                                                                                                                                                                case -28:
                                                                                                                                                                                                                                                case 39:
                                                                                                                                                                                                                                                case 38:
                                                                                                                                                                                                                                                case -8:
                                                                                                                                                                                                                                                case -49:
                                                                                                                                                                                                                                                case 55:
                                                                                                                                                                                                                                                case -48:
                                                                                                                                                                                                                                                case 54:
                                                                                                                                                                                                                                                case -7:
                                                                                                                                                                                                                                                case -51:
                                                                                                                                                                                                                                                case -52:
                                                                                                                                                                                                                                                case 41:
                                                                                                                                                                                                                                                case 42:
                                                                                                                                                                                                                                                case 61:
                                                                                                                                                                                                                                                case 43:
                                                                                                                                                                                                                                                case 44:
                                                                                                                                                                                                                                                case -50:
                                                                                                                                                                                                                                                case 2:
                                                                                                                                                                                                                                                case 3:
                                                                                                                                                                                                                                                case 56:
                                                                                                                                                                                                                                                case 57:
                                                                                                                                                                                                                                                case 58:
                                                                                                                                                                                                                                                case 59:
                                                                                                                                                                                                                                                case 60:
                                                                                                                                                                                                                                                case 69:
                                                                                                                                                                                                                                                case 73:
                                                                                                                                                                                                                                                case 74:
                                                                                                                                                                                                                                                case 75:
                                                                                                                                                                                                                                                case 76:
                                                                                                                                                                                                                                                case -12:
                                                                                                                                                                                                                                                case -13:
                                                                                                                                                                                                                                                case -19:
                                                                                                                                                                                                                                                case -20:
                                                                                                                                                                                                                                                case 47:
                                                                                                                                                                                                                                                case -29:
                                                                                                                                                                                                                                                case -30:
                                                                                                                                                                                                                                                case -31:
                                                                                                                                                                                                                                                case 63:
                                                                                                                                                                                                                                                case 64:
                                                                                                                                                                                                                                                case -53:
                                                                                                                                                                                                                                                case -54:
                                                                                                                                                                                                                                                case 70:
                                                                                                                                                                                                                                                case 71:
                                                                                                                                                                                                                                                case 72:
                                                                                                                                                                                                                                                case -26:
                                                                                                                                                                                                                                                case -32:
                                                                                                                                                                                                                                                case -33:
                                                                                                                                                                                                                                                case -34:
                                                                                                                                                                                                                                                case -35:
                                                                                                                                                                                                                                                case -36:
                                                                                                                                                                                                                                                case -37:
                                                                                                                                                                                                                                                case -38:
                                                                                                                                                                                                                                                case -39:
                                                                                                                                                                                                                                                case -40:
                                                                                                                                                                                                                                                case -41:
                                                                                                                                                                                                                                                case -42:
                                                                                                                                                                                                                                                case -43:
                                                                                                                                                                                                                                                case -44:
                                                                                                                                                                                                                                                case -45:
                                                                                                                                                                                                                                                case -46:
                                                                                                                                                                                                                                                case -47:
                                                                                                                                                                                                                                                case 0:
                                                                                                                                                                                                                                                case 1:
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                              }
                                                                                                                                                                                                                                              Number localNumber1 = localObject3[i3];
                                                                                                                                                                                                                                              if (localNumber1 == localUniqueTag)
                                                                                                                                                                                                                                                localNumber1 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                                                                              localObject10 = localObject3[(--i3)];
                                                                                                                                                                                                                                              if (localObject10 == localUniqueTag)
                                                                                                                                                                                                                                                localObject10 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                                                                              if (i4 == 51)
                                                                                                                                                                                                                                                bool4 = ScriptRuntime.in(localObject10, localNumber1, paramContext);
                                                                                                                                                                                                                                              else
                                                                                                                                                                                                                                                bool4 = ScriptRuntime.instanceOf(localObject10, localNumber1, paramContext);
                                                                                                                                                                                                                                              localObject3[i3] = ScriptRuntime.wrapBoolean(bool4);
                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                            localObject10 = localObject3[(--i3 + 1)];
                                                                                                                                                                                                                                            Object localObject14 = localObject3[i3];
                                                                                                                                                                                                                                            if (localObject10 == localUniqueTag)
                                                                                                                                                                                                                                              if (localObject14 == localUniqueTag)
                                                                                                                                                                                                                                                bool2 = arrayOfDouble[i3] == arrayOfDouble[(i3 + 1)];
                                                                                                                                                                                                                                              else
                                                                                                                                                                                                                                                bool2 = ScriptRuntime.eqNumber(arrayOfDouble[(i3 + 1)], localObject14);
                                                                                                                                                                                                                                            else if (localObject14 == localUniqueTag)
                                                                                                                                                                                                                                              bool2 = ScriptRuntime.eqNumber(arrayOfDouble[i3], localObject10);
                                                                                                                                                                                                                                            else
                                                                                                                                                                                                                                              bool2 = ScriptRuntime.eq(localObject14, localObject10);
                                                                                                                                                                                                                                            bool2 ^= i4 == 13;
                                                                                                                                                                                                                                            localObject3[i3] = ScriptRuntime.wrapBoolean(bool2);
                                                                                                                                                                                                                                          }
                                                                                                                                                                                                                                          localObject6 = localObject3[(--i3 + 1)];
                                                                                                                                                                                                                                          Object localObject10 = localObject3[i3];
                                                                                                                                                                                                                                          if (localObject6 == localUniqueTag)
                                                                                                                                                                                                                                          {
                                                                                                                                                                                                                                            d7 = arrayOfDouble[(i3 + 1)];
                                                                                                                                                                                                                                            if (localObject10 == localUniqueTag)
                                                                                                                                                                                                                                              d10 = arrayOfDouble[i3];
                                                                                                                                                                                                                                            else if (localObject10 instanceof Number)
                                                                                                                                                                                                                                              d10 = ((Number)localObject10).doubleValue();
                                                                                                                                                                                                                                            else
                                                                                                                                                                                                                                              bool5 = false;
                                                                                                                                                                                                                                          }
                                                                                                                                                                                                                                          else if (localObject10 == localUniqueTag)
                                                                                                                                                                                                                                          {
                                                                                                                                                                                                                                            d10 = arrayOfDouble[i3];
                                                                                                                                                                                                                                            if (localObject6 == localUniqueTag)
                                                                                                                                                                                                                                              d7 = arrayOfDouble[(i3 + 1)];
                                                                                                                                                                                                                                            else if (localObject6 instanceof Number)
                                                                                                                                                                                                                                              d7 = ((Number)localObject6).doubleValue();
                                                                                                                                                                                                                                            else
                                                                                                                                                                                                                                              bool5 = false;
                                                                                                                                                                                                                                          }
                                                                                                                                                                                                                                          else
                                                                                                                                                                                                                                          {
                                                                                                                                                                                                                                            bool5 = ScriptRuntime.shallowEq(localObject10, localObject6);
                                                                                                                                                                                                                                            break label1781:
                                                                                                                                                                                                                                            bool5 = d10 == d7;
                                                                                                                                                                                                                                          }
                                                                                                                                                                                                                                          bool5 ^= i4 == 46;
                                                                                                                                                                                                                                          localObject3[i3] = ScriptRuntime.wrapBoolean(bool5);
                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                        if (!(stack_boolean(paramCallFrame, i3--)))
                                                                                                                                                                                                                                          break label6572;
                                                                                                                                                                                                                                        paramCallFrame.pc += 2;
                                                                                                                                                                                                                                      }
                                                                                                                                                                                                                                      if (stack_boolean(paramCallFrame, i3--))
                                                                                                                                                                                                                                        break label6572;
                                                                                                                                                                                                                                      paramCallFrame.pc += 2;
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                    if (stack_boolean(paramCallFrame, i3--))
                                                                                                                                                                                                                                      break;
                                                                                                                                                                                                                                    paramCallFrame.pc += 2;
                                                                                                                                                                                                                                  }
                                                                                                                                                                                                                                  localObject3[(i3--)] = null;
                                                                                                                                                                                                                                  break label6572:
                                                                                                                                                                                                                                  break label6572:
                                                                                                                                                                                                                                  localObject3[(++i3)] = localUniqueTag;
                                                                                                                                                                                                                                  arrayOfDouble[i3] = (paramCallFrame.pc + 2);
                                                                                                                                                                                                                                  break label6572:
                                                                                                                                                                                                                                  if (i3 != paramCallFrame.emptyStackTop + 1)
                                                                                                                                                                                                                                    break;
                                                                                                                                                                                                                                  j += paramCallFrame.localShift;
                                                                                                                                                                                                                                  localObject3[j] = localObject3[i3];
                                                                                                                                                                                                                                  arrayOfDouble[j] = arrayOfDouble[i3];
                                                                                                                                                                                                                                  --i3;
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                              while (i3 == paramCallFrame.emptyStackTop);
                                                                                                                                                                                                                              Kit.codeBug();
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                            if (i != 0)
                                                                                                                                                                                                                              addInstructionCount(paramContext, paramCallFrame, 0);
                                                                                                                                                                                                                            j += paramCallFrame.localShift;
                                                                                                                                                                                                                            localObject6 = localObject3[j];
                                                                                                                                                                                                                            if (localObject6 != localUniqueTag)
                                                                                                                                                                                                                            {
                                                                                                                                                                                                                              paramObject = localObject6;
                                                                                                                                                                                                                              break label6733:
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                            paramCallFrame.pc = (int)arrayOfDouble[j];
                                                                                                                                                                                                                          }
                                                                                                                                                                                                                          while (i == 0);
                                                                                                                                                                                                                          paramCallFrame.pcPrevBranch = paramCallFrame.pc;
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                        localObject3[i3] = null;
                                                                                                                                                                                                                        --i3;
                                                                                                                                                                                                                      }
                                                                                                                                                                                                                      paramCallFrame.result = localObject3[i3];
                                                                                                                                                                                                                      paramCallFrame.resultDbl = arrayOfDouble[i3];
                                                                                                                                                                                                                      localObject3[i3] = null;
                                                                                                                                                                                                                      --i3;
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                    localObject3[(i3 + 1)] = localObject3[i3];
                                                                                                                                                                                                                    arrayOfDouble[(i3 + 1)] = arrayOfDouble[i3];
                                                                                                                                                                                                                    ++i3;
                                                                                                                                                                                                                  }
                                                                                                                                                                                                                  localObject3[(i3 + 1)] = localObject3[(i3 - 1)];
                                                                                                                                                                                                                  arrayOfDouble[(i3 + 1)] = arrayOfDouble[(i3 - 1)];
                                                                                                                                                                                                                  localObject3[(i3 + 2)] = localObject3[i3];
                                                                                                                                                                                                                  arrayOfDouble[(i3 + 2)] = arrayOfDouble[i3];
                                                                                                                                                                                                                  i3 += 2;
                                                                                                                                                                                                                }
                                                                                                                                                                                                                Object localObject6 = localObject3[i3];
                                                                                                                                                                                                                localObject3[i3] = localObject3[(i3 - 1)];
                                                                                                                                                                                                                localObject3[(i3 - 1)] = localObject6;
                                                                                                                                                                                                                double d3 = arrayOfDouble[i3];
                                                                                                                                                                                                                arrayOfDouble[i3] = arrayOfDouble[(i3 - 1)];
                                                                                                                                                                                                                arrayOfDouble[(i3 - 1)] = d3;
                                                                                                                                                                                                              }
                                                                                                                                                                                                              paramCallFrame.result = localObject3[i3];
                                                                                                                                                                                                              paramCallFrame.resultDbl = arrayOfDouble[i3];
                                                                                                                                                                                                              --i3;
                                                                                                                                                                                                              break label6649:
                                                                                                                                                                                                              break label6649:
                                                                                                                                                                                                              paramCallFrame.result = localObject1;
                                                                                                                                                                                                              break label6649:
                                                                                                                                                                                                              i5 = stack_int32(paramCallFrame, i3);
                                                                                                                                                                                                              localObject3[i3] = localUniqueTag;
                                                                                                                                                                                                              arrayOfDouble[i3] = (i5 ^ 0xFFFFFFFF);
                                                                                                                                                                                                            }
                                                                                                                                                                                                            i5 = stack_int32(paramCallFrame, i3);
                                                                                                                                                                                                            int i11 = stack_int32(paramCallFrame, --i3);
                                                                                                                                                                                                            localObject3[i3] = localUniqueTag;
                                                                                                                                                                                                            switch (i4)
                                                                                                                                                                                                            {
                                                                                                                                                                                                            case 11:
                                                                                                                                                                                                              i11 &= i5;
                                                                                                                                                                                                              break;
                                                                                                                                                                                                            case 9:
                                                                                                                                                                                                              i11 |= i5;
                                                                                                                                                                                                              break;
                                                                                                                                                                                                            case 10:
                                                                                                                                                                                                              i11 ^= i5;
                                                                                                                                                                                                              break;
                                                                                                                                                                                                            case 18:
                                                                                                                                                                                                              i11 <<= i5;
                                                                                                                                                                                                              break;
                                                                                                                                                                                                            case 19:
                                                                                                                                                                                                              i11 >>= i5;
                                                                                                                                                                                                            case 12:
                                                                                                                                                                                                            case 13:
                                                                                                                                                                                                            case 14:
                                                                                                                                                                                                            case 15:
                                                                                                                                                                                                            case 16:
                                                                                                                                                                                                            case 17:
                                                                                                                                                                                                            }
                                                                                                                                                                                                            arrayOfDouble[i3] = i11;
                                                                                                                                                                                                          }
                                                                                                                                                                                                          int i5 = stack_int32(paramCallFrame, i3) & 0x1F;
                                                                                                                                                                                                          double d4 = stack_double(paramCallFrame, --i3);
                                                                                                                                                                                                          localObject3[i3] = localUniqueTag;
                                                                                                                                                                                                          arrayOfDouble[i3] = (ScriptRuntime.toUint32(d4) >>> i5);
                                                                                                                                                                                                        }
                                                                                                                                                                                                        d2 = stack_double(paramCallFrame, i3);
                                                                                                                                                                                                        localObject3[i3] = localUniqueTag;
                                                                                                                                                                                                        if (i4 == 29)
                                                                                                                                                                                                          d2 = -d2;
                                                                                                                                                                                                        arrayOfDouble[i3] = d2;
                                                                                                                                                                                                      }
                                                                                                                                                                                                      do_add(localObject3, arrayOfDouble, --i3, paramContext);
                                                                                                                                                                                                    }
                                                                                                                                                                                                    double d2 = stack_double(paramCallFrame, i3);
                                                                                                                                                                                                    double d5 = stack_double(paramCallFrame, --i3);
                                                                                                                                                                                                    localObject3[i3] = localUniqueTag;
                                                                                                                                                                                                    switch (i4)
                                                                                                                                                                                                    {
                                                                                                                                                                                                    case 22:
                                                                                                                                                                                                      d5 -= d2;
                                                                                                                                                                                                      break;
                                                                                                                                                                                                    case 23:
                                                                                                                                                                                                      d5 *= d2;
                                                                                                                                                                                                      break;
                                                                                                                                                                                                    case 24:
                                                                                                                                                                                                      d5 /= d2;
                                                                                                                                                                                                      break;
                                                                                                                                                                                                    case 25:
                                                                                                                                                                                                      d5 %= d2;
                                                                                                                                                                                                    }
                                                                                                                                                                                                    arrayOfDouble[i3] = d5;
                                                                                                                                                                                                  }
                                                                                                                                                                                                  localObject3[i3] = ScriptRuntime.wrapBoolean(!(stack_boolean(paramCallFrame, i3)));
                                                                                                                                                                                                }
                                                                                                                                                                                                localObject3[(++i3)] = ScriptRuntime.bind(paramContext, paramCallFrame.scope, str);
                                                                                                                                                                                              }
                                                                                                                                                                                              localObject7 = localObject3[i3];
                                                                                                                                                                                              if (localObject7 == localUniqueTag)
                                                                                                                                                                                                localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                              localObject11 = (Scriptable)localObject3[(--i3)];
                                                                                                                                                                                              localObject3[i3] = ScriptRuntime.setName((Scriptable)localObject11, localObject7, paramContext, paramCallFrame.scope, str);
                                                                                                                                                                                            }
                                                                                                                                                                                            localObject7 = localObject3[i3];
                                                                                                                                                                                            if (localObject7 == localUniqueTag)
                                                                                                                                                                                              localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                            localObject11 = localObject3[(--i3)];
                                                                                                                                                                                            if (localObject11 == localUniqueTag)
                                                                                                                                                                                              localObject11 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                            localObject3[i3] = ScriptRuntime.delete(localObject11, localObject7, paramContext);
                                                                                                                                                                                          }
                                                                                                                                                                                          localObject7 = localObject3[i3];
                                                                                                                                                                                          if (localObject7 == localUniqueTag)
                                                                                                                                                                                            localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                          localObject3[i3] = ScriptRuntime.getObjectProp(localObject7, str, paramContext);
                                                                                                                                                                                        }
                                                                                                                                                                                        localObject7 = localObject3[i3];
                                                                                                                                                                                        if (localObject7 == localUniqueTag)
                                                                                                                                                                                          localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                        localObject11 = localObject3[(--i3)];
                                                                                                                                                                                        if (localObject11 == localUniqueTag)
                                                                                                                                                                                          localObject11 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                        localObject3[i3] = ScriptRuntime.setObjectProp(localObject11, str, localObject7, paramContext);
                                                                                                                                                                                      }
                                                                                                                                                                                      localObject7 = localObject3[i3];
                                                                                                                                                                                      if (localObject7 == localUniqueTag)
                                                                                                                                                                                        localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                      localObject3[i3] = ScriptRuntime.propIncrDecr(localObject7, str, paramContext, localObject5[paramCallFrame.pc]);
                                                                                                                                                                                      paramCallFrame.pc += 1;
                                                                                                                                                                                    }
                                                                                                                                                                                    localObject7 = localObject3[(--i3)];
                                                                                                                                                                                    if (localObject7 == localUniqueTag)
                                                                                                                                                                                      localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                    localObject15 = localObject3[(i3 + 1)];
                                                                                                                                                                                    if (localObject15 != localUniqueTag)
                                                                                                                                                                                    {
                                                                                                                                                                                      localObject11 = ScriptRuntime.getObjectElem(localObject7, localObject15, paramContext);
                                                                                                                                                                                    }
                                                                                                                                                                                    else
                                                                                                                                                                                    {
                                                                                                                                                                                      d7 = arrayOfDouble[(i3 + 1)];
                                                                                                                                                                                      localObject11 = ScriptRuntime.getObjectIndex(localObject7, d7, paramContext);
                                                                                                                                                                                    }
                                                                                                                                                                                    localObject3[i3] = localObject11;
                                                                                                                                                                                  }
                                                                                                                                                                                  localObject7 = localObject3[((i3 -= 2) + 2)];
                                                                                                                                                                                  if (localObject7 == localUniqueTag)
                                                                                                                                                                                    localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[(i3 + 2)]);
                                                                                                                                                                                  localObject11 = localObject3[i3];
                                                                                                                                                                                  if (localObject11 == localUniqueTag)
                                                                                                                                                                                    localObject11 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                  localObject18 = localObject3[(i3 + 1)];
                                                                                                                                                                                  if (localObject18 != localUniqueTag)
                                                                                                                                                                                  {
                                                                                                                                                                                    localObject15 = ScriptRuntime.setObjectElem(localObject11, localObject18, localObject7, paramContext);
                                                                                                                                                                                  }
                                                                                                                                                                                  else
                                                                                                                                                                                  {
                                                                                                                                                                                    double d8 = arrayOfDouble[(i3 + 1)];
                                                                                                                                                                                    localObject15 = ScriptRuntime.setObjectIndex(localObject11, d8, localObject7, paramContext);
                                                                                                                                                                                  }
                                                                                                                                                                                  localObject3[i3] = localObject15;
                                                                                                                                                                                }
                                                                                                                                                                                localObject7 = localObject3[i3];
                                                                                                                                                                                if (localObject7 == localUniqueTag)
                                                                                                                                                                                  localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                localObject11 = localObject3[(--i3)];
                                                                                                                                                                                if (localObject11 == localUniqueTag)
                                                                                                                                                                                  localObject11 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                                localObject3[i3] = ScriptRuntime.elemIncrDecr(localObject11, localObject7, paramContext, localObject5[paramCallFrame.pc]);
                                                                                                                                                                                paramCallFrame.pc += 1;
                                                                                                                                                                              }
                                                                                                                                                                              localObject7 = (Ref)localObject3[i3];
                                                                                                                                                                              localObject3[i3] = ScriptRuntime.refGet((Ref)localObject7, paramContext);
                                                                                                                                                                            }
                                                                                                                                                                            localObject7 = localObject3[i3];
                                                                                                                                                                            if (localObject7 == localUniqueTag)
                                                                                                                                                                              localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                            localObject11 = (Ref)localObject3[(--i3)];
                                                                                                                                                                            localObject3[i3] = ScriptRuntime.refSet((Ref)localObject11, localObject7, paramContext);
                                                                                                                                                                          }
                                                                                                                                                                          localObject7 = (Ref)localObject3[i3];
                                                                                                                                                                          localObject3[i3] = ScriptRuntime.refDel((Ref)localObject7, paramContext);
                                                                                                                                                                        }
                                                                                                                                                                        localObject7 = (Ref)localObject3[i3];
                                                                                                                                                                        localObject3[i3] = ScriptRuntime.refIncrDecr((Ref)localObject7, paramContext, localObject5[paramCallFrame.pc]);
                                                                                                                                                                        paramCallFrame.pc += 1;
                                                                                                                                                                      }
                                                                                                                                                                      ++i3;
                                                                                                                                                                      j += paramCallFrame.localShift;
                                                                                                                                                                      localObject3[i3] = localObject3[j];
                                                                                                                                                                      arrayOfDouble[i3] = arrayOfDouble[j];
                                                                                                                                                                    }
                                                                                                                                                                    j += paramCallFrame.localShift;
                                                                                                                                                                    localObject3[j] = null;
                                                                                                                                                                  }
                                                                                                                                                                  localObject3[(++i3)] = ScriptRuntime.getNameFunctionAndThis(str, paramContext, paramCallFrame.scope);
                                                                                                                                                                  localObject3[(++i3)] = ScriptRuntime.lastStoredScriptable(paramContext);
                                                                                                                                                                }
                                                                                                                                                                localObject7 = localObject3[i3];
                                                                                                                                                                if (localObject7 == localUniqueTag)
                                                                                                                                                                  localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                                localObject3[i3] = ScriptRuntime.getPropFunctionAndThis(localObject7, str, paramContext);
                                                                                                                                                                localObject3[(++i3)] = ScriptRuntime.lastStoredScriptable(paramContext);
                                                                                                                                                              }
                                                                                                                                                              localObject7 = localObject3[(i3 - 1)];
                                                                                                                                                              if (localObject7 == localUniqueTag)
                                                                                                                                                                localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[(i3 - 1)]);
                                                                                                                                                              Object localObject11 = localObject3[i3];
                                                                                                                                                              if (localObject11 == localUniqueTag)
                                                                                                                                                                localObject11 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                              localObject3[(i3 - 1)] = ScriptRuntime.getElemFunctionAndThis(localObject7, localObject11, paramContext);
                                                                                                                                                              localObject3[i3] = ScriptRuntime.lastStoredScriptable(paramContext);
                                                                                                                                                            }
                                                                                                                                                            Object localObject7 = localObject3[i3];
                                                                                                                                                            if (localObject7 == localUniqueTag)
                                                                                                                                                              localObject7 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                            localObject3[i3] = ScriptRuntime.getValueFunctionAndThis(localObject7, paramContext);
                                                                                                                                                            localObject3[(++i3)] = ScriptRuntime.lastStoredScriptable(paramContext);
                                                                                                                                                          }
                                                                                                                                                          if (i != 0)
                                                                                                                                                            paramContext.instructionCount += 100;
                                                                                                                                                          int i6 = localObject5[paramCallFrame.pc] & 0xFF;
                                                                                                                                                          int i12 = (localObject5[(paramCallFrame.pc + 1)] != 0) ? 1 : 0;
                                                                                                                                                          int i14 = getIndex(localObject5, paramCallFrame.pc + 2);
                                                                                                                                                          if (i12 != 0)
                                                                                                                                                          {
                                                                                                                                                            i3 -= j;
                                                                                                                                                            localObject18 = localObject3[i3];
                                                                                                                                                            if (localObject18 == localUniqueTag)
                                                                                                                                                              localObject18 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                            localObject19 = getArgsArray(localObject3, arrayOfDouble, i3 + 1, j);
                                                                                                                                                            localObject3[i3] = ScriptRuntime.newSpecial(paramContext, localObject18, localObject19, paramCallFrame.scope, i6);
                                                                                                                                                          }
                                                                                                                                                          else
                                                                                                                                                          {
                                                                                                                                                            i3 -= 1 + j;
                                                                                                                                                            localObject18 = (Scriptable)localObject3[(i3 + 1)];
                                                                                                                                                            localObject19 = (Callable)localObject3[i3];
                                                                                                                                                            localObject20 = getArgsArray(localObject3, arrayOfDouble, i3 + 2, j);
                                                                                                                                                            localObject3[i3] = ScriptRuntime.callSpecial(paramContext, (Callable)localObject19, (Scriptable)localObject18, localObject20, paramCallFrame.scope, paramCallFrame.thisObj, i6, paramCallFrame.idata.itsSourceFile, i14);
                                                                                                                                                          }
                                                                                                                                                          paramCallFrame.pc += 4;
                                                                                                                                                        }
                                                                                                                                                        if (i != 0)
                                                                                                                                                          paramContext.instructionCount += 100;
                                                                                                                                                        i3 -= 1 + j;
                                                                                                                                                        localObject8 = (Callable)localObject3[i3];
                                                                                                                                                        localObject12 = (Scriptable)localObject3[(i3 + 1)];
                                                                                                                                                        if (i4 != 68)
                                                                                                                                                          break;
                                                                                                                                                        localObject16 = getArgsArray(localObject3, arrayOfDouble, i3 + 2, j);
                                                                                                                                                        localObject3[i3] = ScriptRuntime.callRef((Callable)localObject8, (Scriptable)localObject12, localObject16, paramContext);
                                                                                                                                                      }
                                                                                                                                                      localObject16 = paramCallFrame.scope;
                                                                                                                                                      if (paramCallFrame.useActivation)
                                                                                                                                                        localObject16 = ScriptableObject.getTopLevelScope(paramCallFrame.scope);
                                                                                                                                                      if (!(localObject8 instanceof InterpretedFunction))
                                                                                                                                                        break;
                                                                                                                                                      localObject18 = (InterpretedFunction)localObject8;
                                                                                                                                                      if (paramCallFrame.fnOrScript.securityDomain != ((InterpretedFunction)localObject18).securityDomain)
                                                                                                                                                        break;
                                                                                                                                                      Object localObject19 = paramCallFrame;
                                                                                                                                                      Object localObject20 = new CallFrame(null);
                                                                                                                                                      if (i4 == -55)
                                                                                                                                                        localObject19 = paramCallFrame.parentFrame;
                                                                                                                                                      initFrame(paramContext, (Scriptable)localObject16, (Scriptable)localObject12, localObject3, arrayOfDouble, i3 + 2, j, (InterpretedFunction)localObject18, (CallFrame)localObject19, (CallFrame)localObject20);
                                                                                                                                                      if (i4 == -55)
                                                                                                                                                      {
                                                                                                                                                        exitFrame(paramContext, paramCallFrame, null);
                                                                                                                                                      }
                                                                                                                                                      else
                                                                                                                                                      {
                                                                                                                                                        paramCallFrame.savedStackTop = i3;
                                                                                                                                                        paramCallFrame.savedCallOp = i4;
                                                                                                                                                      }
                                                                                                                                                      paramCallFrame = (CallFrame)localObject20;
                                                                                                                                                    }
                                                                                                                                                    if (localObject8 instanceof Continuation)
                                                                                                                                                    {
                                                                                                                                                      localObject18 = new ContinuationJump((Continuation)localObject8, paramCallFrame);
                                                                                                                                                      if (j == 0)
                                                                                                                                                      {
                                                                                                                                                        ((ContinuationJump)localObject18).result = localObject1;
                                                                                                                                                      }
                                                                                                                                                      else
                                                                                                                                                      {
                                                                                                                                                        ((ContinuationJump)localObject18).result = localObject3[(i3 + 2)];
                                                                                                                                                        ((ContinuationJump)localObject18).resultDbl = arrayOfDouble[(i3 + 2)];
                                                                                                                                                      }
                                                                                                                                                      paramObject = localObject18;
                                                                                                                                                      break label6733:
                                                                                                                                                    }
                                                                                                                                                    if (!(localObject8 instanceof IdFunctionObject))
                                                                                                                                                      break;
                                                                                                                                                    localObject18 = (IdFunctionObject)localObject8;
                                                                                                                                                    if (!(Continuation.isContinuationConstructor((IdFunctionObject)localObject18)))
                                                                                                                                                      break;
                                                                                                                                                    captureContinuation(paramContext, paramCallFrame, i3);
                                                                                                                                                  }
                                                                                                                                                  localObject18 = getArgsArray(localObject3, arrayOfDouble, i3 + 2, j);
                                                                                                                                                  localObject3[i3] = ((Callable)localObject8).call(paramContext, (Scriptable)localObject16, (Scriptable)localObject12, localObject18);
                                                                                                                                                }
                                                                                                                                                if (i != 0)
                                                                                                                                                  paramContext.instructionCount += 100;
                                                                                                                                                i3 -= j;
                                                                                                                                                localObject8 = localObject3[i3];
                                                                                                                                                if (!(localObject8 instanceof InterpretedFunction))
                                                                                                                                                  break;
                                                                                                                                                localObject12 = (InterpretedFunction)localObject8;
                                                                                                                                                if (paramCallFrame.fnOrScript.securityDomain != ((InterpretedFunction)localObject12).securityDomain)
                                                                                                                                                  break;
                                                                                                                                                localObject16 = ((InterpretedFunction)localObject12).createObject(paramContext, paramCallFrame.scope);
                                                                                                                                                Object localObject18 = new CallFrame(null);
                                                                                                                                                initFrame(paramContext, paramCallFrame.scope, (Scriptable)localObject16, localObject3, arrayOfDouble, i3 + 1, j, (InterpretedFunction)localObject12, paramCallFrame, (CallFrame)localObject18);
                                                                                                                                                localObject3[i3] = localObject16;
                                                                                                                                                paramCallFrame.savedStackTop = i3;
                                                                                                                                                paramCallFrame.savedCallOp = i4;
                                                                                                                                                paramCallFrame = (CallFrame)localObject18;
                                                                                                                                              }
                                                                                                                                              if (!(localObject8 instanceof Function))
                                                                                                                                              {
                                                                                                                                                if (localObject8 == localUniqueTag)
                                                                                                                                                  localObject8 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                                throw ScriptRuntime.notFunctionError(localObject8);
                                                                                                                                              }
                                                                                                                                              localObject12 = (Function)localObject8;
                                                                                                                                              if (!(localObject12 instanceof IdFunctionObject))
                                                                                                                                                break;
                                                                                                                                              localObject16 = (IdFunctionObject)localObject12;
                                                                                                                                              if (!(Continuation.isContinuationConstructor((IdFunctionObject)localObject16)))
                                                                                                                                                break;
                                                                                                                                              captureContinuation(paramContext, paramCallFrame, i3);
                                                                                                                                            }
                                                                                                                                            Object localObject16 = getArgsArray(localObject3, arrayOfDouble, i3 + 1, j);
                                                                                                                                            localObject3[i3] = ((Function)localObject12).construct(paramContext, paramCallFrame.scope, localObject16);
                                                                                                                                          }
                                                                                                                                          localObject8 = localObject3[i3];
                                                                                                                                          if (localObject8 == localUniqueTag)
                                                                                                                                            localObject8 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                                          localObject3[i3] = ScriptRuntime.typeof(localObject8);
                                                                                                                                        }
                                                                                                                                        localObject3[(++i3)] = ScriptRuntime.typeofName(paramCallFrame.scope, str);
                                                                                                                                      }
                                                                                                                                      localObject3[(++i3)] = str;
                                                                                                                                    }
                                                                                                                                    localObject3[(++i3)] = localUniqueTag;
                                                                                                                                    arrayOfDouble[i3] = getShort(localObject5, paramCallFrame.pc);
                                                                                                                                    paramCallFrame.pc += 2;
                                                                                                                                  }
                                                                                                                                  localObject3[(++i3)] = localUniqueTag;
                                                                                                                                  arrayOfDouble[i3] = getInt(localObject5, paramCallFrame.pc);
                                                                                                                                  paramCallFrame.pc += 4;
                                                                                                                                }
                                                                                                                                localObject3[(++i3)] = localUniqueTag;
                                                                                                                                arrayOfDouble[i3] = paramCallFrame.idata.itsDoubleTable[j];
                                                                                                                              }
                                                                                                                              localObject3[(++i3)] = ScriptRuntime.name(paramContext, paramCallFrame.scope, str);
                                                                                                                            }
                                                                                                                            localObject3[(++i3)] = ScriptRuntime.nameIncrDecr(paramCallFrame.scope, str, localObject5[paramCallFrame.pc]);
                                                                                                                            paramCallFrame.pc += 1;
                                                                                                                          }
                                                                                                                          j = localObject5[(paramCallFrame.pc++)];
                                                                                                                          if (paramCallFrame.useActivation)
                                                                                                                            break;
                                                                                                                          arrayOfObject[j] = localObject3[i3];
                                                                                                                          localObject4[j] = arrayOfDouble[i3];
                                                                                                                        }
                                                                                                                        Object localObject8 = localObject3[i3];
                                                                                                                        if (localObject8 == localUniqueTag)
                                                                                                                          localObject8 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                                        str = paramCallFrame.idata.argNames[j];
                                                                                                                        paramCallFrame.scope.put(str, paramCallFrame.scope, localObject8);
                                                                                                                      }
                                                                                                                      j = localObject5[(paramCallFrame.pc++)];
                                                                                                                      ++i3;
                                                                                                                      if (paramCallFrame.useActivation)
                                                                                                                        break;
                                                                                                                      localObject3[i3] = arrayOfObject[j];
                                                                                                                      arrayOfDouble[i3] = localObject4[j];
                                                                                                                    }
                                                                                                                    str = paramCallFrame.idata.argNames[j];
                                                                                                                    localObject3[i3] = paramCallFrame.scope.get(str, paramCallFrame.scope);
                                                                                                                  }
                                                                                                                  ++i3;
                                                                                                                  int i7 = localObject5[paramCallFrame.pc];
                                                                                                                  if (!(paramCallFrame.useActivation))
                                                                                                                  {
                                                                                                                    double d6;
                                                                                                                    localObject3[i3] = localUniqueTag;
                                                                                                                    localObject12 = arrayOfObject[j];
                                                                                                                    if (localObject12 == localUniqueTag)
                                                                                                                    {
                                                                                                                      d6 = localObject4[j];
                                                                                                                    }
                                                                                                                    else
                                                                                                                    {
                                                                                                                      d6 = ScriptRuntime.toNumber(localObject12);
                                                                                                                      arrayOfObject[j] = localUniqueTag;
                                                                                                                    }
                                                                                                                    double d9 = ((i7 & 0x1) == 0) ? d6 + 1D : d6 - 1D;
                                                                                                                    localObject4[j] = d9;
                                                                                                                    arrayOfDouble[i3] = (((i7 & 0x2) == 0) ? d9 : d6);
                                                                                                                  }
                                                                                                                  else
                                                                                                                  {
                                                                                                                    localObject12 = paramCallFrame.idata.argNames[j];
                                                                                                                    localObject3[i3] = ScriptRuntime.nameIncrDecr(paramCallFrame.scope, (String)localObject12, i7);
                                                                                                                  }
                                                                                                                  paramCallFrame.pc += 1;
                                                                                                                }
                                                                                                                localObject3[(++i3)] = localUniqueTag;
                                                                                                                arrayOfDouble[i3] = 0D;
                                                                                                              }
                                                                                                              localObject3[(++i3)] = localUniqueTag;
                                                                                                              arrayOfDouble[i3] = 1D;
                                                                                                            }
                                                                                                            localObject3[(++i3)] = null;
                                                                                                          }
                                                                                                          localObject3[(++i3)] = paramCallFrame.thisObj;
                                                                                                        }
                                                                                                        localObject3[(++i3)] = paramCallFrame.fnOrScript;
                                                                                                      }
                                                                                                      localObject3[(++i3)] = Boolean.FALSE;
                                                                                                    }
                                                                                                    localObject3[(++i3)] = Boolean.TRUE;
                                                                                                  }
                                                                                                  localObject3[(++i3)] = localObject1;
                                                                                                }
                                                                                                Number localNumber2 = localObject3[i3];
                                                                                                if (localNumber2 == localUniqueTag)
                                                                                                  localNumber2 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                                --i3;
                                                                                                paramCallFrame.scope = ScriptRuntime.enterWith(localNumber2, paramContext, paramCallFrame.scope);
                                                                                              }
                                                                                              paramCallFrame.scope = ScriptRuntime.leaveWith(paramCallFrame.scope);
                                                                                            }
                                                                                            --i3;
                                                                                            j += paramCallFrame.localShift;
                                                                                            int i8 = (paramCallFrame.idata.itsICode[paramCallFrame.pc] != 0) ? 1 : 0;
                                                                                            localObject12 = (Throwable)localObject3[(i3 + 1)];
                                                                                            if (i8 == 0)
                                                                                              localObject17 = null;
                                                                                            else
                                                                                              localObject17 = (Scriptable)localObject3[j];
                                                                                            localObject3[j] = ScriptRuntime.newCatchScope((Throwable)localObject12, (Scriptable)localObject17, str, paramContext, paramCallFrame.scope);
                                                                                            paramCallFrame.pc += 1;
                                                                                          }
                                                                                          localObject9 = localObject3[i3];
                                                                                          if (localObject9 == localUniqueTag)
                                                                                            localObject9 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                          --i3;
                                                                                          j += paramCallFrame.localShift;
                                                                                          localObject3[j] = ScriptRuntime.enumInit(localObject9, paramContext, i4 == 58);
                                                                                        }
                                                                                        j += paramCallFrame.localShift;
                                                                                        localObject9 = localObject3[j];
                                                                                        localObject3[(++i3)] = ((i4 == 59) ? ScriptRuntime.enumNext(localObject9) : ScriptRuntime.enumId(localObject9, paramContext));
                                                                                      }
                                                                                      localObject9 = localObject3[i3];
                                                                                      if (localObject9 == localUniqueTag)
                                                                                        localObject9 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                      localObject3[i3] = ScriptRuntime.specialRef(localObject9, str, paramContext);
                                                                                    }
                                                                                    localObject9 = localObject3[i3];
                                                                                    if (localObject9 == localUniqueTag)
                                                                                      localObject9 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                    localObject12 = localObject3[(--i3)];
                                                                                    if (localObject12 == localUniqueTag)
                                                                                      localObject12 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                    localObject3[i3] = ScriptRuntime.memberRef(localObject12, localObject9, paramContext, j);
                                                                                  }
                                                                                  localObject9 = localObject3[i3];
                                                                                  if (localObject9 == localUniqueTag)
                                                                                    localObject9 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                  localObject12 = localObject3[(--i3)];
                                                                                  if (localObject12 == localUniqueTag)
                                                                                    localObject12 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                  localObject17 = localObject3[(--i3)];
                                                                                  if (localObject17 == localUniqueTag)
                                                                                    localObject17 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                  localObject3[i3] = ScriptRuntime.memberRef(localObject17, localObject12, localObject9, paramContext, j);
                                                                                }
                                                                                localObject9 = localObject3[i3];
                                                                                if (localObject9 == localUniqueTag)
                                                                                  localObject9 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                                localObject3[i3] = ScriptRuntime.nameRef(localObject9, paramContext, paramCallFrame.scope, j);
                                                                              }
                                                                              localObject9 = localObject3[i3];
                                                                              if (localObject9 == localUniqueTag)
                                                                                localObject9 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                              Object localObject12 = localObject3[(--i3)];
                                                                              if (localObject12 == localUniqueTag)
                                                                                localObject12 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                              localObject3[i3] = ScriptRuntime.nameRef(localObject12, localObject9, paramContext, paramCallFrame.scope, j);
                                                                            }
                                                                            j += paramCallFrame.localShift;
                                                                            paramCallFrame.scope = ((Scriptable)localObject3[j]);
                                                                          }
                                                                          j += paramCallFrame.localShift;
                                                                          localObject3[j] = paramCallFrame.scope;
                                                                        }
                                                                        localObject3[(++i3)] = InterpretedFunction.createFunction(paramContext, paramCallFrame.scope, paramCallFrame.fnOrScript, j);
                                                                      }
                                                                      initFunction(paramContext, paramCallFrame.scope, paramCallFrame.fnOrScript, j);
                                                                    }
                                                                    localObject3[(++i3)] = paramCallFrame.scriptRegExps[j];
                                                                  }
                                                                  localObject3[(++i3)] = new Object[j];
                                                                  arrayOfDouble[i3] = 0D;
                                                                }
                                                                localObject9 = localObject3[i3];
                                                                if (localObject9 == localUniqueTag)
                                                                  localObject9 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                                int i13 = (int)arrayOfDouble[(--i3)];
                                                                ((Object[])(Object[])localObject3[i3])[i13] = localObject9;
                                                                arrayOfDouble[i3] = (i13 + 1);
                                                              }
                                                              localObject9 = (Object[])(Object[])localObject3[i3];
                                                              if (i4 == 64)
                                                              {
                                                                localObject17 = (Object[])(Object[])paramCallFrame.idata.literalIds[j];
                                                                localObject13 = ScriptRuntime.newObjectLiteral(localObject17, localObject9, paramContext, paramCallFrame.scope);
                                                              }
                                                              else
                                                              {
                                                                localObject17 = null;
                                                                if (i4 == -31)
                                                                  localObject17 = (int[])(int[])paramCallFrame.idata.literalIds[j];
                                                                localObject13 = ScriptRuntime.newArrayLiteral(localObject9, localObject17, paramContext, paramCallFrame.scope);
                                                              }
                                                              localObject3[i3] = localObject13;
                                                            }
                                                            Object localObject9 = localObject3[i3];
                                                            if (localObject9 == localUniqueTag)
                                                              localObject9 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                            --i3;
                                                            paramCallFrame.scope = ScriptRuntime.enterDotQuery(localObject9, paramCallFrame.scope);
                                                          }
                                                          boolean bool3 = stack_boolean(paramCallFrame, i3);
                                                          Object localObject13 = ScriptRuntime.updateDotQuery(bool3, paramCallFrame.scope);
                                                          if (localObject13 == null)
                                                            break;
                                                          localObject3[i3] = localObject13;
                                                          paramCallFrame.scope = ScriptRuntime.leaveDotQuery(paramCallFrame.scope);
                                                          paramCallFrame.pc += 2;
                                                        }
                                                        --i3;
                                                        break label6572:
                                                        localNumber3 = localObject3[i3];
                                                        if (localNumber3 == localUniqueTag)
                                                          localNumber3 = ScriptRuntime.wrapNumber(arrayOfDouble[i3]);
                                                        localObject3[i3] = ScriptRuntime.setDefaultNamespace(localNumber3, paramContext);
                                                      }
                                                      localNumber3 = localObject3[i3];
                                                    }
                                                    while (localNumber3 == localUniqueTag);
                                                    localObject3[i3] = ScriptRuntime.escapeAttributeValue(localNumber3, paramContext);
                                                  }
                                                  localNumber3 = localObject3[i3];
                                                }
                                                while (localNumber3 == localUniqueTag);
                                                localObject3[i3] = ScriptRuntime.escapeTextValue(localNumber3, paramContext);
                                              }
                                              paramCallFrame.pcSourceLineStart = paramCallFrame.pc;
                                              if (paramCallFrame.debuggerFrame != null)
                                              {
                                                i9 = getIndex(localObject5, paramCallFrame.pc);
                                                paramCallFrame.debuggerFrame.onLineChange(paramContext, i9);
                                              }
                                              paramCallFrame.pc += 2;
                                            }
                                            j = 0;
                                          }
                                          j = 1;
                                        }
                                        j = 2;
                                      }
                                      j = 3;
                                    }
                                    j = 4;
                                  }
                                  j = 5;
                                }
                                j = 0xFF & localObject5[paramCallFrame.pc];
                                paramCallFrame.pc += 1;
                              }
                              j = getIndex(localObject5, paramCallFrame.pc);
                              paramCallFrame.pc += 2;
                            }
                            j = getInt(localObject5, paramCallFrame.pc);
                            paramCallFrame.pc += 4;
                          }
                          str = arrayOfString[0];
                        }
                        str = arrayOfString[1];
                      }
                      str = arrayOfString[2];
                    }
                    str = arrayOfString[3];
                  }
                  str = arrayOfString[(0xFF & localObject5[paramCallFrame.pc])];
                  paramCallFrame.pc += 1;
                }
                str = arrayOfString[getIndex(localObject5, paramCallFrame.pc)];
                paramCallFrame.pc += 2;
              }
              str = arrayOfString[getInt(localObject5, paramCallFrame.pc)];
              paramCallFrame.pc += 4;
            }
            dumpICode(paramCallFrame.idata);
            throw new RuntimeException("Unknown icode : " + i4 + " @ pc : " + (paramCallFrame.pc - 1));
            if (i != 0)
              label6572: addInstructionCount(paramContext, paramCallFrame, 2);
            int i9 = getShort(localObject5, paramCallFrame.pc);
            if (i9 != 0)
              paramCallFrame.pc += i9 - 1;
            else
              paramCallFrame.pc = paramCallFrame.idata.longJumps.getExistingInt(paramCallFrame.pc);
          }
          while (i == 0);
          paramCallFrame.pcPrevBranch = paramCallFrame.pc;
        }
        label6649: exitFrame(paramContext, paramCallFrame, null);
        localObject2 = paramCallFrame.result;
        d1 = paramCallFrame.resultDbl;
        if (paramCallFrame.parentFrame == null)
          break;
        paramCallFrame = paramCallFrame.parentFrame;
        if (paramCallFrame.frozen)
          paramCallFrame = paramCallFrame.cloneFrozen();
        setCallResult(paramCallFrame, localObject2, d1);
        localObject2 = null;
      }
    }
    catch (Throwable localThrowable1)
    {
      while (true)
      {
        while (true)
        {
          label6733: int i1;
          if (paramObject != null)
          {
            localThrowable1.printStackTrace(System.err);
            throw new IllegalStateException();
          }
          paramObject = localThrowable1;
          if (paramObject == null)
            Kit.codeBug();
          localObject5 = null;
          if (paramObject instanceof JavaScriptException)
          {
            i1 = 2;
          }
          else if (paramObject instanceof EcmaError)
          {
            i1 = 2;
          }
          else if (paramObject instanceof EvaluatorException)
          {
            i1 = 2;
          }
          else if (paramObject instanceof RuntimeException)
          {
            i1 = 1;
          }
          else if (paramObject instanceof Error)
          {
            i1 = 0;
          }
          else
          {
            i1 = 1;
            localObject5 = (ContinuationJump)paramObject;
          }
          if (i != 0)
            try
            {
              addInstructionCount(paramContext, paramCallFrame, 100);
            }
            catch (RuntimeException localRuntimeException1)
            {
              paramObject = localRuntimeException1;
              i1 = 1;
            }
            catch (Error localError)
            {
              paramObject = localError;
              localObject5 = null;
              i1 = 0;
            }
          if ((paramCallFrame.debuggerFrame != null) && (paramObject instanceof RuntimeException))
          {
            RuntimeException localRuntimeException2 = (RuntimeException)paramObject;
            try
            {
              paramCallFrame.debuggerFrame.onExceptionThrown(paramContext, localRuntimeException2);
            }
            catch (Throwable localThrowable2)
            {
              paramObject = localThrowable2;
              localObject5 = null;
              i1 = 0;
            }
          }
          do
          {
            while (i1 != 0)
            {
              boolean bool1 = i1 != 2;
              j = getExceptionHandler(paramCallFrame, bool1);
              if (j < 0)
                break;
            }
            exitFrame(paramContext, paramCallFrame, paramObject);
            paramCallFrame = paramCallFrame.parentFrame;
            if (paramCallFrame == null)
              break label6973:
          }
          while ((localObject5 == null) || (((ContinuationJump)localObject5).branchFrame != paramCallFrame));
          j = -1;
        }
        label6973: if (localObject5 == null)
          break label7023;
        if (((ContinuationJump)localObject5).branchFrame != null)
          Kit.codeBug();
        if (((ContinuationJump)localObject5).capturedFrame == null)
          break;
        j = -1;
      }
      localObject2 = ((ContinuationJump)localObject5).result;
      d1 = ((ContinuationJump)localObject5).resultDbl;
      paramObject = null;
    }
    if ((paramContext.previousInterpreterInvocations != null) && (paramContext.previousInterpreterInvocations.size() != 0))
    {
      label7023: paramContext.lastInterpreterFrame = paramContext.previousInterpreterInvocations.pop();
    }
    else
    {
      paramContext.lastInterpreterFrame = null;
      paramContext.previousInterpreterInvocations = null;
    }
    if (paramObject != null)
    {
      if (paramObject instanceof RuntimeException)
        throw ((RuntimeException)paramObject);
      throw ((Error)paramObject);
    }
    return ((localObject2 != localUniqueTag) ? localObject2 : ScriptRuntime.wrapNumber(d1));
  }

  private static void initFrame(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject, double[] paramArrayOfDouble, int paramInt1, int paramInt2, InterpretedFunction paramInterpretedFunction, CallFrame paramCallFrame1, CallFrame paramCallFrame2)
  {
    Scriptable localScriptable;
    Object[] arrayOfObject;
    double[] arrayOfDouble;
    int l;
    InterpreterData localInterpreterData1 = paramInterpretedFunction.idata;
    boolean bool = localInterpreterData1.itsNeedsActivation;
    DebugFrame localDebugFrame = null;
    if (paramContext.debugger != null)
    {
      localDebugFrame = paramContext.debugger.getFrame(paramContext, localInterpreterData1);
      if (localDebugFrame != null)
        bool = true;
    }
    if (bool)
    {
      if (paramArrayOfDouble != null)
        paramArrayOfObject = getArgsArray(paramArrayOfObject, paramArrayOfDouble, paramInt1, paramInt2);
      paramInt1 = 0;
      paramArrayOfDouble = null;
    }
    if (localInterpreterData1.itsFunctionType != 0)
    {
      if (!(localInterpreterData1.useDynamicScope))
        localScriptable = paramInterpretedFunction.getParentScope();
      else
        localScriptable = paramScriptable1;
      if (bool)
        localScriptable = ScriptRuntime.createFunctionActivation(paramInterpretedFunction, localScriptable, paramArrayOfObject);
    }
    else
    {
      localScriptable = paramScriptable1;
      ScriptRuntime.initScript(paramInterpretedFunction, paramScriptable2, paramContext, localScriptable, paramInterpretedFunction.idata.evalScriptFlag);
    }
    if (localInterpreterData1.itsNestedFunctions != null)
    {
      if ((localInterpreterData1.itsFunctionType != 0) && (!(localInterpreterData1.itsNeedsActivation)))
        Kit.codeBug();
      for (int i = 0; i < localInterpreterData1.itsNestedFunctions.length; ++i)
      {
        InterpreterData localInterpreterData2 = localInterpreterData1.itsNestedFunctions[i];
        if (localInterpreterData2.itsFunctionType == 1)
          initFunction(paramContext, localScriptable, paramInterpretedFunction, i);
      }
    }
    Scriptable[] arrayOfScriptable = null;
    if (localInterpreterData1.itsRegExpLiterals != null)
      if (localInterpreterData1.itsFunctionType != 0)
        arrayOfScriptable = paramInterpretedFunction.functionRegExps;
      else
        arrayOfScriptable = paramInterpretedFunction.createRegExpWraps(paramContext, localScriptable);
    int j = localInterpreterData1.itsMaxVars + localInterpreterData1.itsMaxLocals - 1;
    int k = localInterpreterData1.itsMaxFrameArray;
    if (k != j + localInterpreterData1.itsMaxStack + 1)
      Kit.codeBug();
    if ((paramCallFrame2.stack != null) && (k <= paramCallFrame2.stack.length))
    {
      l = 1;
      arrayOfObject = paramCallFrame2.stack;
      arrayOfDouble = paramCallFrame2.sDbl;
    }
    else
    {
      l = 0;
      arrayOfObject = new Object[k];
      arrayOfDouble = new double[k];
    }
    int i1 = localInterpreterData1.argCount;
    if (i1 > paramInt2)
      i1 = paramInt2;
    paramCallFrame2.parentFrame = paramCallFrame1;
    paramCallFrame2.frameIndex = ((paramCallFrame1 == null) ? 0 : paramCallFrame1.frameIndex + 1);
    paramCallFrame2.frozen = false;
    paramCallFrame2.fnOrScript = paramInterpretedFunction;
    paramCallFrame2.idata = localInterpreterData1;
    paramCallFrame2.stack = arrayOfObject;
    paramCallFrame2.sDbl = arrayOfDouble;
    paramCallFrame2.varSource = paramCallFrame2;
    paramCallFrame2.localShift = localInterpreterData1.itsMaxVars;
    paramCallFrame2.emptyStackTop = j;
    paramCallFrame2.debuggerFrame = localDebugFrame;
    paramCallFrame2.useActivation = bool;
    paramCallFrame2.thisObj = paramScriptable2;
    paramCallFrame2.scriptRegExps = arrayOfScriptable;
    paramCallFrame2.result = Undefined.instance;
    paramCallFrame2.pc = 0;
    paramCallFrame2.pcPrevBranch = 0;
    paramCallFrame2.pcSourceLineStart = localInterpreterData1.firstLinePC;
    paramCallFrame2.scope = localScriptable;
    paramCallFrame2.savedStackTop = j;
    paramCallFrame2.savedCallOp = 0;
    System.arraycopy(paramArrayOfObject, paramInt1, arrayOfObject, 0, i1);
    if (paramArrayOfDouble != null)
      System.arraycopy(paramArrayOfDouble, paramInt1, arrayOfDouble, 0, i1);
    for (int i2 = i1; i2 != localInterpreterData1.itsMaxVars; ++i2)
      arrayOfObject[i2] = Undefined.instance;
    if (l != 0)
      for (i2 = j + 1; i2 != arrayOfObject.length; ++i2)
        arrayOfObject[i2] = null;
    enterFrame(paramContext, paramCallFrame2, paramArrayOfObject);
  }

  private static boolean isFrameEnterExitRequired(CallFrame paramCallFrame)
  {
    return ((paramCallFrame.debuggerFrame != null) || (paramCallFrame.idata.itsNeedsActivation));
  }

  private static void enterFrame(Context paramContext, CallFrame paramCallFrame, Object[] paramArrayOfObject)
  {
    if (paramCallFrame.debuggerFrame != null)
      paramCallFrame.debuggerFrame.onEnter(paramContext, paramCallFrame.scope, paramCallFrame.thisObj, paramArrayOfObject);
    if (paramCallFrame.idata.itsNeedsActivation)
      ScriptRuntime.enterActivationFunction(paramContext, paramCallFrame.scope);
  }

  private static void exitFrame(Context paramContext, CallFrame paramCallFrame, Object paramObject)
  {
    if (paramCallFrame.idata.itsNeedsActivation)
      ScriptRuntime.exitActivationFunction(paramContext);
    if (paramCallFrame.debuggerFrame != null)
      try
      {
        if (paramObject instanceof Throwable)
        {
          paramCallFrame.debuggerFrame.onExit(paramContext, true, paramObject);
        }
        else
        {
          Object localObject;
          ContinuationJump localContinuationJump = (ContinuationJump)paramObject;
          if (localContinuationJump == null)
            localObject = paramCallFrame.result;
          else
            localObject = localContinuationJump.result;
          if (localObject == UniqueTag.DOUBLE_MARK)
          {
            double d;
            if (localContinuationJump == null)
              d = paramCallFrame.resultDbl;
            else
              d = localContinuationJump.resultDbl;
            localObject = ScriptRuntime.wrapNumber(d);
          }
          paramCallFrame.debuggerFrame.onExit(paramContext, false, localObject);
        }
      }
      catch (Throwable localThrowable)
      {
        System.err.println("RHINO USAGE WARNING: onExit terminated with exception");
        localThrowable.printStackTrace(System.err);
      }
  }

  private static void setCallResult(CallFrame paramCallFrame, Object paramObject, double paramDouble)
  {
    if (paramCallFrame.savedCallOp == 37)
    {
      paramCallFrame.stack[paramCallFrame.savedStackTop] = paramObject;
      paramCallFrame.sDbl[paramCallFrame.savedStackTop] = paramDouble;
    }
    else if (paramCallFrame.savedCallOp == 30)
    {
      if (paramObject instanceof Scriptable)
        paramCallFrame.stack[paramCallFrame.savedStackTop] = paramObject;
    }
    else
    {
      Kit.codeBug();
    }
    paramCallFrame.savedCallOp = 0;
  }

  private static void captureContinuation(Context paramContext, CallFrame paramCallFrame, int paramInt)
  {
    Continuation localContinuation = new Continuation();
    ScriptRuntime.setObjectProtoAndParent(localContinuation, ScriptRuntime.getTopCallScope(paramContext));
    for (CallFrame localCallFrame = paramCallFrame.parentFrame; (localCallFrame != null) && (!(localCallFrame.frozen)); localCallFrame = localCallFrame.parentFrame)
    {
      localCallFrame.frozen = true;
      for (int i = localCallFrame.savedStackTop + 1; i != localCallFrame.stack.length; ++i)
        localCallFrame.stack[i] = null;
      if (localCallFrame.savedCallOp == 37)
        localCallFrame.stack[localCallFrame.savedStackTop] = null;
      else if (localCallFrame.savedCallOp != 30)
        Kit.codeBug();
    }
    localContinuation.initImplementation(paramCallFrame.parentFrame);
    paramCallFrame.stack[paramInt] = localContinuation;
  }

  private static int stack_int32(CallFrame paramCallFrame, int paramInt)
  {
    double d;
    Object localObject = paramCallFrame.stack[paramInt];
    if (localObject == UniqueTag.DOUBLE_MARK)
      d = paramCallFrame.sDbl[paramInt];
    else
      d = ScriptRuntime.toNumber(localObject);
    return ScriptRuntime.toInt32(d);
  }

  private static double stack_double(CallFrame paramCallFrame, int paramInt)
  {
    Object localObject = paramCallFrame.stack[paramInt];
    if (localObject != UniqueTag.DOUBLE_MARK)
      return ScriptRuntime.toNumber(localObject);
    return paramCallFrame.sDbl[paramInt];
  }

  private static boolean stack_boolean(CallFrame paramCallFrame, int paramInt)
  {
    double d;
    Object localObject = paramCallFrame.stack[paramInt];
    if (localObject == Boolean.TRUE)
      return true;
    if (localObject == Boolean.FALSE)
      return false;
    if (localObject == UniqueTag.DOUBLE_MARK)
    {
      d = paramCallFrame.sDbl[paramInt];
      return ((d == d) && (d != 0D));
    }
    if ((localObject == null) || (localObject == Undefined.instance))
      return false;
    if (localObject instanceof Number)
    {
      d = ((Number)localObject).doubleValue();
      return ((d == d) && (d != 0D));
    }
    if (localObject instanceof Boolean)
      return ((Boolean)localObject).booleanValue();
    return ScriptRuntime.toBoolean(localObject);
  }

  private static void do_add(Object[] paramArrayOfObject, double[] paramArrayOfDouble, int paramInt, Context paramContext)
  {
    double d1;
    int i;
    Object localObject3;
    String str2;
    Object localObject1 = paramArrayOfObject[(paramInt + 1)];
    Object localObject2 = paramArrayOfObject[paramInt];
    if (localObject1 == UniqueTag.DOUBLE_MARK)
    {
      d1 = paramArrayOfDouble[(paramInt + 1)];
      if (localObject2 == UniqueTag.DOUBLE_MARK)
      {
        paramArrayOfDouble[paramInt] += d1;
        return;
      }
      i = 1;
    }
    else if (localObject2 == UniqueTag.DOUBLE_MARK)
    {
      d1 = paramArrayOfDouble[paramInt];
      localObject2 = localObject1;
      i = 0;
    }
    else
    {
      if ((localObject2 instanceof Scriptable) || (localObject1 instanceof Scriptable))
      {
        paramArrayOfObject[paramInt] = ScriptRuntime.add(localObject2, localObject1, paramContext);
      }
      else
      {
        String str1;
        if (localObject2 instanceof String)
        {
          str1 = (String)localObject2;
          str2 = ScriptRuntime.toString(localObject1);
          paramArrayOfObject[paramInt] = str1.concat(str2);
        }
        else if (localObject1 instanceof String)
        {
          str1 = ScriptRuntime.toString(localObject2);
          str2 = (String)localObject1;
          paramArrayOfObject[paramInt] = str1.concat(str2);
        }
        else
        {
          double d2 = (localObject2 instanceof Number) ? ((Number)localObject2).doubleValue() : ScriptRuntime.toNumber(localObject2);
          double d4 = (localObject1 instanceof Number) ? ((Number)localObject1).doubleValue() : ScriptRuntime.toNumber(localObject1);
          paramArrayOfObject[paramInt] = UniqueTag.DOUBLE_MARK;
          paramArrayOfDouble[paramInt] = (d2 + d4);
        }
      }
      return;
    }
    if (localObject2 instanceof Scriptable)
    {
      localObject1 = ScriptRuntime.wrapNumber(d1);
      if (i == 0)
      {
        localObject3 = localObject2;
        localObject2 = localObject1;
        localObject1 = localObject3;
      }
      paramArrayOfObject[paramInt] = ScriptRuntime.add(localObject2, localObject1, paramContext);
    }
    else if (localObject2 instanceof String)
    {
      localObject3 = (String)localObject2;
      str2 = ScriptRuntime.toString(d1);
      if (i != 0)
        paramArrayOfObject[paramInt] = ((String)localObject3).concat(str2);
      else
        paramArrayOfObject[paramInt] = str2.concat((String)localObject3);
    }
    else
    {
      double d3 = (localObject2 instanceof Number) ? ((Number)localObject2).doubleValue() : ScriptRuntime.toNumber(localObject2);
      paramArrayOfObject[paramInt] = UniqueTag.DOUBLE_MARK;
      paramArrayOfDouble[paramInt] = (d3 + d1);
    }
  }

  private static Object[] getArgsArray(Object[] paramArrayOfObject, double[] paramArrayOfDouble, int paramInt1, int paramInt2)
  {
    if (paramInt2 == 0)
      return ScriptRuntime.emptyArgs;
    Object[] arrayOfObject = new Object[paramInt2];
    int i = 0;
    while (i != paramInt2)
    {
      Object localObject = paramArrayOfObject[paramInt1];
      if (localObject == UniqueTag.DOUBLE_MARK)
        localObject = ScriptRuntime.wrapNumber(paramArrayOfDouble[paramInt1]);
      arrayOfObject[i] = localObject;
      ++i;
      ++paramInt1;
    }
    return ((Object)arrayOfObject);
  }

  private static void addInstructionCount(Context paramContext, CallFrame paramCallFrame, int paramInt)
  {
    paramContext.instructionCount += paramCallFrame.pc - paramCallFrame.pcPrevBranch + paramInt;
    if (paramContext.instructionCount > paramContext.instructionThreshold)
    {
      paramContext.observeInstructionCount(paramContext.instructionCount);
      paramContext.instructionCount = 0;
    }
  }

  private static class CallFrame
  implements Cloneable, Serializable
  {
    static final long serialVersionUID = -2843792508994958978L;
    CallFrame parentFrame;
    int frameIndex;
    boolean frozen;
    InterpretedFunction fnOrScript;
    InterpreterData idata;
    Object[] stack;
    double[] sDbl;
    CallFrame varSource;
    int localShift;
    int emptyStackTop;
    DebugFrame debuggerFrame;
    boolean useActivation;
    Scriptable thisObj;
    Scriptable[] scriptRegExps;
    Object result;
    double resultDbl;
    int pc;
    int pcPrevBranch;
    int pcSourceLineStart;
    Scriptable scope;
    int savedStackTop;
    int savedCallOp;

    CallFrame cloneFrozen()
    {
      CallFrame localCallFrame;
      if (!(this.frozen))
        Kit.codeBug();
      try
      {
        localCallFrame = (CallFrame)super.clone();
      }
      catch (CloneNotSupportedException localCloneNotSupportedException)
      {
        throw new IllegalStateException();
      }
      localCallFrame.stack = ((Object[])(Object[])this.stack.clone());
      localCallFrame.sDbl = ((double[])(double[])this.sDbl.clone());
      localCallFrame.frozen = false;
      return localCallFrame;
    }
  }

  private static final class ContinuationJump
  implements Serializable
  {
    static final long serialVersionUID = 7687739156004308247L;
    Interpreter.CallFrame capturedFrame;
    Interpreter.CallFrame branchFrame;
    Object result;
    double resultDbl;

    ContinuationJump(Continuation paramContinuation, Interpreter.CallFrame paramCallFrame)
    {
      this.capturedFrame = ((Interpreter.CallFrame)paramContinuation.getImplementation());
      if ((this.capturedFrame == null) || (paramCallFrame == null))
      {
        this.branchFrame = null;
      }
      else
      {
        Interpreter.CallFrame localCallFrame1 = this.capturedFrame;
        Interpreter.CallFrame localCallFrame2 = paramCallFrame;
        int i = localCallFrame1.frameIndex - localCallFrame2.frameIndex;
        if (i != 0)
        {
          if (i < 0)
          {
            localCallFrame1 = paramCallFrame;
            localCallFrame2 = this.capturedFrame;
            i = -i;
          }
          do
            localCallFrame1 = localCallFrame1.parentFrame;
          while (--i != 0);
          if (localCallFrame1.frameIndex != localCallFrame2.frameIndex)
            Kit.codeBug();
        }
        while ((localCallFrame1 != localCallFrame2) && (localCallFrame1 != null))
        {
          localCallFrame1 = localCallFrame1.parentFrame;
          localCallFrame2 = localCallFrame2.parentFrame;
        }
        this.branchFrame = localCallFrame1;
        if ((this.branchFrame != null) && (!(this.branchFrame.frozen)))
          Kit.codeBug();
      }
    }
  }
}
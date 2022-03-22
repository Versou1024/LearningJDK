package sun.org.mozilla.javascript.internal;

import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;

public class Parser
{
  static final int CLEAR_TI_MASK = 65535;
  static final int TI_AFTER_EOL = 65536;
  static final int TI_CHECK_LABEL = 131072;
  CompilerEnvirons compilerEnv;
  private ErrorReporter errorReporter;
  private String sourceURI;
  boolean calledByCompileFunction;
  private TokenStream ts;
  private int currentFlaggedToken;
  private int syntaxErrorCount;
  private IRFactory nf;
  private int nestingOfFunction;
  private Decompiler decompiler;
  private String encodedSource;
  ScriptOrFnNode currentScriptOrFn;
  private int nestingOfWith;
  private Hashtable labelSet;
  private ObjArray loopSet;
  private ObjArray loopAndSwitchSet;

  public Parser(CompilerEnvirons paramCompilerEnvirons, ErrorReporter paramErrorReporter)
  {
    this.compilerEnv = paramCompilerEnvirons;
    this.errorReporter = paramErrorReporter;
  }

  protected Decompiler createDecompiler(CompilerEnvirons paramCompilerEnvirons)
  {
    return new Decompiler();
  }

  void addWarning(String paramString1, String paramString2)
  {
    String str = ScriptRuntime.getMessage1(paramString1, paramString2);
    this.errorReporter.warning(str, this.sourceURI, this.ts.getLineno(), this.ts.getLine(), this.ts.getOffset());
  }

  void addError(String paramString)
  {
    this.syntaxErrorCount += 1;
    String str = ScriptRuntime.getMessage0(paramString);
    this.errorReporter.error(str, this.sourceURI, this.ts.getLineno(), this.ts.getLine(), this.ts.getOffset());
  }

  RuntimeException reportError(String paramString)
  {
    addError(paramString);
    throw new ParserException(null);
  }

  private int peekToken()
    throws IOException
  {
    int i = this.currentFlaggedToken;
    if (i == 0)
    {
      i = this.ts.getToken();
      if (i == 1)
      {
        do
          i = this.ts.getToken();
        while (i == 1);
        i |= 65536;
      }
      this.currentFlaggedToken = i;
    }
    return (i & 0xFFFF);
  }

  private int peekFlaggedToken()
    throws IOException
  {
    peekToken();
    return this.currentFlaggedToken;
  }

  private void consumeToken()
  {
    this.currentFlaggedToken = 0;
  }

  private int nextToken()
    throws IOException
  {
    int i = peekToken();
    consumeToken();
    return i;
  }

  private int nextFlaggedToken()
    throws IOException
  {
    peekToken();
    int i = this.currentFlaggedToken;
    consumeToken();
    return i;
  }

  private boolean matchToken(int paramInt)
    throws IOException
  {
    int i = peekToken();
    if (i != paramInt)
      return false;
    consumeToken();
    return true;
  }

  private int peekTokenOrEOL()
    throws IOException
  {
    int i = peekToken();
    if ((this.currentFlaggedToken & 0x10000) != 0)
      i = 1;
    return i;
  }

  private void setCheckForLabel()
  {
    if ((this.currentFlaggedToken & 0xFFFF) != 38)
      throw Kit.codeBug();
    this.currentFlaggedToken |= 131072;
  }

  private void mustMatchToken(int paramInt, String paramString)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    if (!(matchToken(paramInt)))
      reportError(paramString);
  }

  private void mustHaveXML()
  {
    if (!(this.compilerEnv.isXmlAvailable()))
      reportError("msg.XML.not.available");
  }

  public String getEncodedSource()
  {
    return this.encodedSource;
  }

  public boolean eof()
  {
    return this.ts.eof();
  }

  boolean insideFunction()
  {
    return (this.nestingOfFunction != 0);
  }

  private Node enterLoop(Node paramNode)
  {
    Node localNode = this.nf.createLoopNode(paramNode, this.ts.getLineno());
    if (this.loopSet == null)
    {
      this.loopSet = new ObjArray();
      if (this.loopAndSwitchSet == null)
        this.loopAndSwitchSet = new ObjArray();
    }
    this.loopSet.push(localNode);
    this.loopAndSwitchSet.push(localNode);
    return localNode;
  }

  private void exitLoop()
  {
    this.loopSet.pop();
    this.loopAndSwitchSet.pop();
  }

  private Node enterSwitch(Node paramNode1, int paramInt, Node paramNode2)
  {
    Node localNode = this.nf.createSwitch(paramNode1, paramInt);
    if (this.loopAndSwitchSet == null)
      this.loopAndSwitchSet = new ObjArray();
    this.loopAndSwitchSet.push(localNode);
    return localNode;
  }

  private void exitSwitch()
  {
    this.loopAndSwitchSet.pop();
  }

  public ScriptOrFnNode parse(String paramString1, String paramString2, int paramInt)
  {
    this.sourceURI = paramString2;
    this.ts = new TokenStream(this, null, paramString1, paramInt);
    try
    {
      return parse();
    }
    catch (IOException localIOException)
    {
      throw new IllegalStateException();
    }
  }

  public ScriptOrFnNode parse(Reader paramReader, String paramString, int paramInt)
    throws IOException
  {
    this.sourceURI = paramString;
    this.ts = new TokenStream(this, paramReader, null, paramInt);
    return parse();
  }

  private ScriptOrFnNode parse()
    throws IOException
  {
    Object localObject;
    this.decompiler = createDecompiler(this.compilerEnv);
    this.nf = new IRFactory(this);
    this.currentScriptOrFn = this.nf.createScript();
    int i = this.decompiler.getCurrentOffset();
    this.encodedSource = null;
    this.decompiler.addToken(132);
    this.currentFlaggedToken = 0;
    this.syntaxErrorCount = 0;
    int j = this.ts.getLineno();
    Node localNode = this.nf.createLeaf(125);
    try
    {
      while (true)
      {
        int k = peekToken();
        if (k <= 0)
          break;
        if (k == 105)
          consumeToken();
        try
        {
          localObject = function((this.calledByCompileFunction) ? 2 : 1);
        }
        catch (ParserException localParserException)
        {
          break label156:
          localObject = statement();
        }
        this.nf.addChildToBack(localNode, (Node)localObject);
      }
    }
    catch (StackOverflowError localStackOverflowError)
    {
      label156: localObject = ScriptRuntime.getMessage0("mag.too.deep.parser.recursion");
      throw Context.reportRuntimeError((String)localObject, this.sourceURI, this.ts.getLineno(), null, 0);
    }
    if (this.syntaxErrorCount != 0)
    {
      String str = String.valueOf(this.syntaxErrorCount);
      str = ScriptRuntime.getMessage1("msg.got.syntax.errors", str);
      throw this.errorReporter.runtimeError(str, this.sourceURI, j, null, 0);
    }
    this.currentScriptOrFn.setSourceName(this.sourceURI);
    this.currentScriptOrFn.setBaseLineno(j);
    this.currentScriptOrFn.setEndLineno(this.ts.getLineno());
    int l = this.decompiler.getCurrentOffset();
    this.currentScriptOrFn.setEncodedSourceBounds(i, l);
    this.nf.initScript(this.currentScriptOrFn, localNode);
    if (this.compilerEnv.isGeneratingSource())
      this.encodedSource = this.decompiler.getEncodedSource();
    this.decompiler = null;
    return ((ScriptOrFnNode)this.currentScriptOrFn);
  }

  private Node parseFunctionBody()
    throws IOException
  {
    this.nestingOfFunction += 1;
    Node localNode1 = this.nf.createBlock(this.ts.getLineno());
    try
    {
      while (true)
      {
        Node localNode2;
        int i = peekToken();
        switch (i)
        {
        case -1:
        case 0:
        case 82:
          break;
        case 105:
          consumeToken();
          localNode2 = function(1);
          break;
        default:
          localNode2 = statement();
        }
        this.nf.addChildToBack(localNode1, localNode2);
      }
    }
    catch (ParserException localParserException)
    {
    }
    finally
    {
      this.nestingOfFunction -= 1;
    }
    return localNode1;
  }

  private Node function(int paramInt)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    String str1;
    int i1;
    Node localNode3;
    int i = paramInt;
    int j = this.ts.getLineno();
    int k = this.decompiler.markFunctionStart(paramInt);
    Node localNode1 = null;
    if (matchToken(38))
    {
      str1 = this.ts.getString();
      this.decompiler.addName(str1);
      if (!(matchToken(83)))
      {
        if (this.compilerEnv.isAllowMemberExprAsFunctionName())
        {
          Node localNode2 = this.nf.createName(str1);
          str1 = "";
          localNode1 = memberExprTail(false, localNode2);
        }
        mustMatchToken(83, "msg.no.paren.parms");
      }
    }
    else if (matchToken(83))
    {
      str1 = "";
    }
    else
    {
      str1 = "";
      if (this.compilerEnv.isAllowMemberExprAsFunctionName())
        localNode1 = memberExpr(false);
      mustMatchToken(83, "msg.no.paren.parms");
    }
    if (localNode1 != null)
      i = 2;
    boolean bool = insideFunction();
    FunctionNode localFunctionNode = this.nf.createFunction(str1);
    if ((bool) || (this.nestingOfWith > 0))
      localFunctionNode.itsIgnoreDynamicScope = true;
    int l = this.currentScriptOrFn.addFunction(localFunctionNode);
    ScriptOrFnNode localScriptOrFnNode = this.currentScriptOrFn;
    this.currentScriptOrFn = localFunctionNode;
    int i2 = this.nestingOfWith;
    this.nestingOfWith = 0;
    Hashtable localHashtable = this.labelSet;
    this.labelSet = null;
    ObjArray localObjArray1 = this.loopSet;
    this.loopSet = null;
    ObjArray localObjArray2 = this.loopAndSwitchSet;
    this.loopAndSwitchSet = null;
    try
    {
      int i3;
      this.decompiler.addToken(83);
      if (!(matchToken(84)))
      {
        i3 = 1;
        do
        {
          if (i3 == 0)
            this.decompiler.addToken(85);
          i3 = 0;
          mustMatchToken(38, "msg.no.parm");
          String str2 = this.ts.getString();
          if (localFunctionNode.hasParamOrVar(str2))
            addWarning("msg.dup.parms", str2);
          localFunctionNode.addParam(str2);
          this.decompiler.addName(str2);
        }
        while (matchToken(85));
        mustMatchToken(84, "msg.no.paren.after.parms");
      }
      this.decompiler.addToken(84);
      mustMatchToken(81, "msg.no.brace.body");
      this.decompiler.addEOL(81);
      localNode3 = parseFunctionBody();
      mustMatchToken(82, "msg.no.brace.after.body");
      this.decompiler.addToken(82);
      i1 = this.decompiler.markFunctionEnd(k);
      if (paramInt != 2)
      {
        if (this.compilerEnv.getLanguageVersion() >= 120)
        {
          i3 = peekTokenOrEOL();
          if (i3 == 105)
            reportError("msg.no.semi.stmt");
        }
        this.decompiler.addToken(1);
      }
    }
    finally
    {
      this.loopAndSwitchSet = localObjArray2;
      this.loopSet = localObjArray1;
      this.labelSet = localHashtable;
      this.nestingOfWith = i2;
      this.currentScriptOrFn = localScriptOrFnNode;
    }
    localFunctionNode.setEncodedSourceBounds(k, i1);
    localFunctionNode.setSourceName(this.sourceURI);
    localFunctionNode.setBaseLineno(j);
    localFunctionNode.setEndLineno(this.ts.getLineno());
    Node localNode4 = this.nf.initFunction(localFunctionNode, l, localNode3, i);
    if (localNode1 != null)
    {
      localNode4 = this.nf.createAssignment(86, localNode1, localNode4);
      if (paramInt != 2)
        localNode4 = this.nf.createExprStatementNoReturn(localNode4, j);
    }
    return localNode4;
  }

  private Node statements()
    throws IOException
  {
    Node localNode = this.nf.createBlock(this.ts.getLineno());
    while (((i = peekToken()) > 0) && (i != 82))
    {
      int i;
      this.nf.addChildToBack(localNode, statement());
    }
    return localNode;
  }

  private Node condition()
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    mustMatchToken(83, "msg.no.paren.cond");
    this.decompiler.addToken(83);
    Node localNode = expr(false);
    mustMatchToken(84, "msg.no.paren.after.cond");
    this.decompiler.addToken(84);
    return localNode;
  }

  private Node matchJumpLabelName()
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    Node localNode = null;
    int i = peekTokenOrEOL();
    if (i == 38)
    {
      consumeToken();
      String str = this.ts.getString();
      this.decompiler.addName(str);
      if (this.labelSet != null)
        localNode = (Node)this.labelSet.get(str);
      if (localNode == null)
        reportError("msg.undef.label");
    }
    return localNode;
  }

  private Node statement()
    throws IOException
  {
    Node localNode;
    try
    {
      localNode = statementHelper(null);
      if (localNode != null)
        return localNode;
    }
    catch (ParserException localParserException)
    {
    }
    int i = this.ts.getLineno();
    while (true)
    {
      int j = peekTokenOrEOL();
      consumeToken();
      switch (j)
      {
      case -1:
      case 0:
      case 1:
      case 78:
        break;
      }
    }
    return this.nf.createExprStatement(this.nf.createName("error"), i);
  }

  private Node statementHelper(Node paramNode)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    int j;
    Node localNode2;
    Node localNode4;
    Node localNode5;
    int i1;
    Object localObject1;
    int i2;
    Node localNode1 = null;
    int i = peekToken();
    switch (i)
    {
    case 108:
      consumeToken();
      this.decompiler.addToken(108);
      j = this.ts.getLineno();
      Node localNode3 = condition();
      this.decompiler.addEOL(81);
      localNode5 = statement();
      localObject1 = null;
      if (matchToken(109))
      {
        this.decompiler.addToken(82);
        this.decompiler.addToken(109);
        this.decompiler.addEOL(81);
        localObject1 = statement();
      }
      this.decompiler.addEOL(82);
      localNode1 = this.nf.createIf(localNode3, localNode5, (Node)localObject1, j);
      return localNode1;
    case 110:
      consumeToken();
      this.decompiler.addToken(110);
      j = this.ts.getLineno();
      mustMatchToken(83, "msg.no.paren.switch");
      this.decompiler.addToken(83);
      localNode1 = enterSwitch(expr(false), j, paramNode);
      try
      {
        mustMatchToken(84, "msg.no.paren.after.switch");
        this.decompiler.addToken(84);
        mustMatchToken(81, "msg.no.brace.switch");
        this.decompiler.addEOL(81);
        int l = 0;
        while (true)
        {
          i = nextToken();
          switch (i)
          {
          case 82:
            break;
          case 111:
            this.decompiler.addToken(111);
            localNode5 = expr(false);
            mustMatchToken(99, "msg.no.colon.case");
            this.decompiler.addEOL(99);
            break;
          case 112:
            if (l != 0)
              reportError("msg.double.switch.default");
            this.decompiler.addToken(112);
            l = 1;
            localNode5 = null;
            mustMatchToken(99, "msg.no.colon.case");
            this.decompiler.addEOL(99);
            break;
          default:
            reportError("msg.bad.switch");
            break;
          }
          localObject1 = this.nf.createLeaf(125);
          while (((i = peekToken()) != 82) && (i != 111) && (i != 112) && (i != 0))
            this.nf.addChildToBack((Node)localObject1, statement());
          this.nf.addSwitchCase(localNode1, localNode5, (Node)localObject1);
        }
        this.decompiler.addEOL(82);
        this.nf.closeSwitch(localNode1);
      }
      finally
      {
        exitSwitch();
      }
      return localNode1;
    case 113:
      consumeToken();
      this.decompiler.addToken(113);
      localNode2 = enterLoop(paramNode);
      try
      {
        localNode4 = condition();
        this.decompiler.addEOL(81);
        localNode5 = statement();
        this.decompiler.addEOL(82);
        localNode1 = this.nf.createWhile(localNode2, localNode4, localNode5);
      }
      finally
      {
        exitLoop();
      }
      return localNode1;
    case 114:
      consumeToken();
      this.decompiler.addToken(114);
      this.decompiler.addEOL(81);
      localNode2 = enterLoop(paramNode);
      try
      {
        localNode4 = statement();
        this.decompiler.addToken(82);
        mustMatchToken(113, "msg.no.while.do");
        this.decompiler.addToken(113);
        localNode5 = condition();
        localNode1 = this.nf.createDoWhile(localNode2, localNode4, localNode5);
      }
      finally
      {
        exitLoop();
      }
      matchToken(78);
      this.decompiler.addEOL(78);
      return localNode1;
    case 115:
      consumeToken();
      boolean bool = false;
      this.decompiler.addToken(115);
      localNode4 = enterLoop(paramNode);
      try
      {
        Node localNode6 = null;
        if (matchToken(38))
        {
          this.decompiler.addName(this.ts.getString());
          if (this.ts.getString().equals("each"))
            bool = true;
          else
            reportError("msg.no.paren.for");
        }
        mustMatchToken(83, "msg.no.paren.for");
        this.decompiler.addToken(83);
        i = peekToken();
        if (i == 78)
        {
          localNode5 = this.nf.createLeaf(124);
        }
        else if (i == 118)
        {
          consumeToken();
          localNode5 = variables(true);
        }
        else
        {
          localNode5 = expr(true);
        }
        if (matchToken(51))
        {
          this.decompiler.addToken(51);
          localObject1 = expr(false);
        }
        else
        {
          mustMatchToken(78, "msg.no.semi.for");
          this.decompiler.addToken(78);
          if (peekToken() == 78)
            localObject1 = this.nf.createLeaf(124);
          else
            localObject1 = expr(false);
          mustMatchToken(78, "msg.no.semi.for.cond");
          this.decompiler.addToken(78);
          if (peekToken() == 84)
            localNode6 = this.nf.createLeaf(124);
          else
            localNode6 = expr(false);
        }
        mustMatchToken(84, "msg.no.paren.for.ctrl");
        this.decompiler.addToken(84);
        this.decompiler.addEOL(81);
        Node localNode7 = statement();
        this.decompiler.addEOL(82);
        if (localNode6 == null)
          localNode1 = this.nf.createForIn(localNode4, localNode5, (Node)localObject1, localNode7, bool);
        else
          localNode1 = this.nf.createFor(localNode4, localNode5, (Node)localObject1, localNode6, localNode7);
      }
      finally
      {
        exitLoop();
      }
      return localNode1;
    case 77:
      consumeToken();
      k = this.ts.getLineno();
      localNode5 = null;
      localObject1 = null;
      this.decompiler.addToken(77);
      this.decompiler.addEOL(81);
      localNode4 = statement();
      this.decompiler.addEOL(82);
      localNode5 = this.nf.createLeaf(125);
      i2 = 0;
      int i3 = peekToken();
      if (i3 == 120)
        while (true)
        {
          if (!(matchToken(120)))
            break label1462;
          if (i2 != 0)
            reportError("msg.catch.unreachable");
          this.decompiler.addToken(120);
          mustMatchToken(83, "msg.no.paren.catch");
          this.decompiler.addToken(83);
          mustMatchToken(38, "msg.bad.catchcond");
          String str = this.ts.getString();
          this.decompiler.addName(str);
          Node localNode8 = null;
          if (matchToken(108))
          {
            this.decompiler.addToken(108);
            localNode8 = expr(false);
          }
          else
          {
            i2 = 1;
          }
          mustMatchToken(84, "msg.bad.catchcond");
          this.decompiler.addToken(84);
          mustMatchToken(81, "msg.no.brace.catchblock");
          this.decompiler.addEOL(81);
          this.nf.addChildToBack(localNode5, this.nf.createCatch(str, localNode8, statements(), this.ts.getLineno()));
          mustMatchToken(82, "msg.no.brace.after.body");
          this.decompiler.addEOL(82);
        }
      if (i3 != 121)
        mustMatchToken(121, "msg.try.no.catchfinally");
      if (matchToken(121))
      {
        this.decompiler.addToken(121);
        this.decompiler.addEOL(81);
        localObject1 = statement();
        this.decompiler.addEOL(82);
      }
      localNode1 = this.nf.createTryCatchFinally(localNode4, localNode5, (Node)localObject1, k);
      return localNode1;
    case 49:
      consumeToken();
      if (peekTokenOrEOL() == 1)
        reportError("msg.bad.throw.eol");
      k = this.ts.getLineno();
      this.decompiler.addToken(49);
      localNode1 = this.nf.createThrow(expr(false), k);
      break;
    case 116:
      consumeToken();
      k = this.ts.getLineno();
      this.decompiler.addToken(116);
      localNode4 = matchJumpLabelName();
      if (localNode4 == null)
      {
        if ((this.loopAndSwitchSet == null) || (this.loopAndSwitchSet.size() == 0))
        {
          reportError("msg.bad.break");
          return null;
        }
        localNode4 = (Node)this.loopAndSwitchSet.peek();
      }
      localNode1 = this.nf.createBreak(localNode4, k);
      break;
    case 117:
      consumeToken();
      k = this.ts.getLineno();
      this.decompiler.addToken(117);
      localNode5 = matchJumpLabelName();
      if (localNode5 == null)
      {
        if ((this.loopSet == null) || (this.loopSet.size() == 0))
        {
          reportError("msg.continue.outside");
          return null;
        }
        localNode4 = (Node)this.loopSet.peek();
      }
      else
      {
        localNode4 = this.nf.getLabelLoop(localNode5);
        if (localNode4 == null)
        {
          reportError("msg.continue.nonloop");
          return null;
        }
      }
      localNode1 = this.nf.createContinue(localNode4, k);
      break;
    case 119:
      consumeToken();
      this.decompiler.addToken(119);
      k = this.ts.getLineno();
      mustMatchToken(83, "msg.no.paren.with");
      this.decompiler.addToken(83);
      localNode4 = expr(false);
      mustMatchToken(84, "msg.no.paren.after.with");
      this.decompiler.addToken(84);
      this.decompiler.addEOL(81);
      this.nestingOfWith += 1;
      try
      {
        localNode5 = statement();
      }
      finally
      {
        this.nestingOfWith -= 1;
      }
      this.decompiler.addEOL(82);
      localNode1 = this.nf.createWith(localNode4, localNode5, k);
      return localNode1;
    case 118:
      consumeToken();
      localNode1 = variables(false);
      break;
    case 4:
      if (!(insideFunction()))
        reportError("msg.bad.return");
      consumeToken();
      this.decompiler.addToken(4);
      k = this.ts.getLineno();
      i = peekTokenOrEOL();
      switch (i)
      {
      case -1:
      case 0:
      case 1:
      case 78:
      case 82:
        localNode4 = null;
        break;
      default:
        localNode4 = expr(false);
      }
      localNode1 = this.nf.createReturn(localNode4, k);
      break;
    case 81:
      consumeToken();
      if (paramNode != null)
        this.decompiler.addToken(81);
      localNode1 = statements();
      mustMatchToken(82, "msg.no.brace.block");
      if (paramNode != null)
        this.decompiler.addEOL(82);
      return localNode1;
    case -1:
    case 78:
      consumeToken();
      localNode1 = this.nf.createLeaf(124);
      return localNode1;
    case 105:
      consumeToken();
      localNode1 = function(3);
      return localNode1;
    case 112:
      consumeToken();
      mustHaveXML();
      this.decompiler.addToken(112);
      k = this.ts.getLineno();
      if ((!(matchToken(38))) || (!(this.ts.getString().equals("xml"))))
        reportError("msg.bad.namespace");
      this.decompiler.addName(this.ts.getString());
      if ((!(matchToken(38))) || (!(this.ts.getString().equals("namespace"))))
        reportError("msg.bad.namespace");
      this.decompiler.addName(this.ts.getString());
      if (!(matchToken(86)))
        reportError("msg.bad.namespace");
      this.decompiler.addToken(86);
      localNode4 = expr(false);
      localNode1 = this.nf.createDefaultNamespace(localNode4, k);
      break;
    case 38:
      i1 = this.ts.getLineno();
      localObject1 = this.ts.getString();
      setCheckForLabel();
      localNode1 = expr(false);
      if (localNode1.getType() != 126)
      {
        localNode1 = this.nf.createExprStatement(localNode1, i1);
      }
      else
      {
        if (peekToken() != 99)
          Kit.codeBug();
        consumeToken();
        this.decompiler.addName((String)localObject1);
        this.decompiler.addEOL(99);
        if (this.labelSet == null)
          this.labelSet = new Hashtable();
        else if (this.labelSet.containsKey(localObject1))
          reportError("msg.dup.label");
        if (paramNode == null)
        {
          i2 = 1;
          paramNode = localNode1;
        }
        else
        {
          i2 = 0;
        }
        this.labelSet.put(localObject1, paramNode);
        try
        {
          localNode1 = statementHelper(paramNode);
        }
        finally
        {
          this.labelSet.remove(localObject1);
        }
        if (i2 != 0)
          localNode1 = this.nf.createLabeledStatement(paramNode, localNode1);
        return localNode1;
      }
    default:
      label1462: i1 = this.ts.getLineno();
      localNode1 = expr(false);
      localNode1 = this.nf.createExprStatement(localNode1, i1);
    }
    int k = peekFlaggedToken();
    switch (k & 0xFFFF)
    {
    case 78:
      consumeToken();
      break;
    case -1:
    case 0:
    case 82:
      break;
    default:
      if ((k & 0x10000) == 0)
        reportError("msg.no.semi.stmt");
    }
    this.decompiler.addEOL(78);
    return ((Node)localNode1);
  }

  private Node variables(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    Node localNode1 = this.nf.createVariables(this.ts.getLineno());
    int i = 1;
    this.decompiler.addToken(118);
    while (true)
    {
      mustMatchToken(38, "msg.bad.var");
      String str = this.ts.getString();
      if (i == 0)
        this.decompiler.addToken(85);
      i = 0;
      this.decompiler.addName(str);
      this.currentScriptOrFn.addVar(str);
      Node localNode2 = this.nf.createName(str);
      if (matchToken(86))
      {
        this.decompiler.addToken(86);
        Node localNode3 = assignExpr(paramBoolean);
        this.nf.addChildToBack(localNode2, localNode3);
      }
      this.nf.addChildToBack(localNode1, localNode2);
      if (!(matchToken(85)))
        break;
    }
    return localNode1;
  }

  private Node expr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    for (Node localNode = assignExpr(paramBoolean); matchToken(85); localNode = this.nf.createBinary(85, localNode, assignExpr(paramBoolean)))
      this.decompiler.addToken(85);
    return localNode;
  }

  private Node assignExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    Node localNode = condExpr(paramBoolean);
    int i = peekToken();
    if ((86 <= i) && (i <= 97))
    {
      consumeToken();
      this.decompiler.addToken(i);
      localNode = this.nf.createAssignment(i, localNode, assignExpr(paramBoolean));
    }
    return localNode;
  }

  private Node condExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    Node localNode3 = orExpr(paramBoolean);
    if (matchToken(98))
    {
      this.decompiler.addToken(98);
      Node localNode1 = assignExpr(false);
      mustMatchToken(99, "msg.no.colon.cond");
      this.decompiler.addToken(99);
      Node localNode2 = assignExpr(paramBoolean);
      return this.nf.createCondExpr(localNode3, localNode1, localNode2);
    }
    return localNode3;
  }

  private Node orExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    Node localNode = andExpr(paramBoolean);
    if (matchToken(100))
    {
      this.decompiler.addToken(100);
      localNode = this.nf.createBinary(100, localNode, orExpr(paramBoolean));
    }
    return localNode;
  }

  private Node andExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    Node localNode = bitOrExpr(paramBoolean);
    if (matchToken(101))
    {
      this.decompiler.addToken(101);
      localNode = this.nf.createBinary(101, localNode, andExpr(paramBoolean));
    }
    return localNode;
  }

  private Node bitOrExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    for (Node localNode = bitXorExpr(paramBoolean); matchToken(9); localNode = this.nf.createBinary(9, localNode, bitXorExpr(paramBoolean)))
      this.decompiler.addToken(9);
    return localNode;
  }

  private Node bitXorExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    for (Node localNode = bitAndExpr(paramBoolean); matchToken(10); localNode = this.nf.createBinary(10, localNode, bitAndExpr(paramBoolean)))
      this.decompiler.addToken(10);
    return localNode;
  }

  private Node bitAndExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    for (Node localNode = eqExpr(paramBoolean); matchToken(11); localNode = this.nf.createBinary(11, localNode, eqExpr(paramBoolean)))
      this.decompiler.addToken(11);
    return localNode;
  }

  private Node eqExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    int i;
    Node localNode = relExpr(paramBoolean);
    while (true)
    {
      i = peekToken();
      switch (i)
      {
      case 12:
      case 13:
      case 45:
      case 46:
        consumeToken();
        int j = i;
        int k = i;
        if (this.compilerEnv.getLanguageVersion() == 120)
          switch (i)
          {
          case 12:
            k = 45;
            break;
          case 13:
            k = 46;
            break;
          case 45:
            j = 12;
            break;
          case 46:
            j = 13;
          }
        this.decompiler.addToken(j);
        localNode = this.nf.createBinary(k, localNode, relExpr(paramBoolean));
      }
    }
    return localNode;
  }

  private Node relExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    int i;
    Node localNode = shiftExpr();
    while (true)
    {
      i = peekToken();
      switch (i)
      {
      case 51:
        if (paramBoolean);
        break;
      case 14:
      case 15:
      case 16:
      case 17:
      case 52:
        consumeToken();
        this.decompiler.addToken(i);
        localNode = this.nf.createBinary(i, localNode, shiftExpr());
      }
    }
    return localNode;
  }

  private Node shiftExpr()
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    int i;
    Node localNode = addExpr();
    while (true)
    {
      i = peekToken();
      switch (i)
      {
      case 18:
      case 19:
      case 20:
        consumeToken();
        this.decompiler.addToken(i);
        localNode = this.nf.createBinary(i, localNode, addExpr());
      }
    }
    return localNode;
  }

  private Node addExpr()
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    Node localNode = mulExpr();
    while (true)
    {
      int i = peekToken();
      if ((i != 21) && (i != 22))
        break;
      consumeToken();
      this.decompiler.addToken(i);
      localNode = this.nf.createBinary(i, localNode, mulExpr());
    }
    return localNode;
  }

  private Node mulExpr()
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    int i;
    Node localNode = unaryExpr();
    while (true)
    {
      i = peekToken();
      switch (i)
      {
      case 23:
      case 24:
      case 25:
        consumeToken();
        this.decompiler.addToken(i);
        localNode = this.nf.createBinary(i, localNode, unaryExpr());
      }
    }
    return localNode;
  }

  private Node unaryExpr()
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    Node localNode;
    int i = peekToken();
    switch (i)
    {
    case 26:
    case 27:
    case 32:
    case 122:
      consumeToken();
      this.decompiler.addToken(i);
      return this.nf.createUnary(i, unaryExpr());
    case 21:
      consumeToken();
      this.decompiler.addToken(28);
      return this.nf.createUnary(28, unaryExpr());
    case 22:
      consumeToken();
      this.decompiler.addToken(29);
      return this.nf.createUnary(29, unaryExpr());
    case 102:
    case 103:
      consumeToken();
      this.decompiler.addToken(i);
      return this.nf.createIncDec(i, false, memberExpr(true));
    case 31:
      consumeToken();
      this.decompiler.addToken(31);
      return this.nf.createUnary(31, unaryExpr());
    case -1:
      consumeToken();
      break;
    case 14:
      if (!(this.compilerEnv.isXmlAvailable()))
        break label270;
      consumeToken();
      localNode = xmlInitializer();
      return memberExprTail(true, localNode);
    default:
      label270: localNode = memberExpr(true);
      i = peekTokenOrEOL();
      if ((i == 102) || (i == 103))
      {
        consumeToken();
        this.decompiler.addToken(i);
        return this.nf.createIncDec(i, true, localNode);
      }
      return localNode;
    }
    return this.nf.createName("err");
  }

  private Node xmlInitializer()
    throws IOException
  {
    int i = this.ts.getFirstXMLToken();
    if ((i != 141) && (i != 144))
    {
      reportError("msg.syntax");
      return null;
    }
    Node localNode1 = this.nf.createLeaf(30);
    this.decompiler.addToken(30);
    this.decompiler.addToken(104);
    String str = this.ts.getString();
    boolean bool = str.trim().startsWith("<>");
    this.decompiler.addName((bool) ? "XMLList" : "XML");
    Node localNode2 = this.nf.createName((bool) ? "XMLList" : "XML");
    this.nf.addChildToBack(localNode1, localNode2);
    localNode2 = null;
    while (true)
    {
      switch (i)
      {
      case 141:
        int j;
        str = this.ts.getString();
        this.decompiler.addString(str);
        mustMatchToken(81, "msg.syntax");
        this.decompiler.addToken(81);
        Node localNode3 = (peekToken() == 82) ? this.nf.createString("") : expr(false);
        mustMatchToken(82, "msg.syntax");
        this.decompiler.addToken(82);
        if (localNode2 == null)
          localNode2 = this.nf.createString(str);
        else
          localNode2 = this.nf.createBinary(21, localNode2, this.nf.createString(str));
        if (this.ts.isXMLAttribute())
          j = 71;
        else
          j = 72;
        localNode3 = this.nf.createUnary(j, localNode3);
        localNode2 = this.nf.createBinary(21, localNode2, localNode3);
        break;
      case 144:
        str = this.ts.getString();
        this.decompiler.addString(str);
        if (localNode2 == null)
          localNode2 = this.nf.createString(str);
        else
          localNode2 = this.nf.createBinary(21, localNode2, this.nf.createString(str));
        this.nf.addChildToBack(localNode1, localNode2);
        return localNode1;
      default:
        reportError("msg.syntax");
        return null;
      }
      i = this.ts.getNextXMLToken();
    }
  }

  private void argumentList(Node paramNode)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    boolean bool = matchToken(84);
    if (!(bool))
    {
      int i = 1;
      do
      {
        if (i == 0)
          this.decompiler.addToken(85);
        i = 0;
        this.nf.addChildToBack(paramNode, assignExpr(false));
      }
      while (matchToken(85));
      mustMatchToken(84, "msg.no.paren.arg");
    }
    this.decompiler.addToken(84);
  }

  private Node memberExpr(boolean paramBoolean)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    Node localNode;
    int i = peekToken();
    if (i == 30)
    {
      consumeToken();
      this.decompiler.addToken(30);
      localNode = this.nf.createCallOrNew(30, memberExpr(false));
      if (matchToken(83))
      {
        this.decompiler.addToken(83);
        argumentList(localNode);
      }
      i = peekToken();
      if (i == 81)
        this.nf.addChildToBack(localNode, primaryExpr());
    }
    else
    {
      localNode = primaryExpr();
    }
    return memberExprTail(paramBoolean, localNode);
  }

  private Node memberExprTail(boolean paramBoolean, Node paramNode)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    while (true)
    {
      int i = peekToken();
      switch (i)
      {
      case 104:
      case 139:
        String str;
        consumeToken();
        this.decompiler.addToken(i);
        int j = 0;
        if (i == 139)
        {
          mustHaveXML();
          j = 4;
        }
        if (!(this.compilerEnv.isXmlAvailable()))
        {
          mustMatchToken(38, "msg.no.name.after.dot");
          str = this.ts.getString();
          this.decompiler.addName(str);
          paramNode = this.nf.createPropertyGet(paramNode, null, str, j);
        }
        else
        {
          i = nextToken();
          switch (i)
          {
          case 38:
            str = this.ts.getString();
            this.decompiler.addName(str);
            paramNode = propertyName(paramNode, str, j);
            break;
          case 23:
            this.decompiler.addName("*");
            paramNode = propertyName(paramNode, "*", j);
            break;
          case 143:
            this.decompiler.addToken(143);
            paramNode = attributeAccess(paramNode, j);
            break;
          default:
            reportError("msg.no.name.after.dot");
          }
        }
        break;
      case 142:
        consumeToken();
        mustHaveXML();
        this.decompiler.addToken(142);
        paramNode = this.nf.createDotQuery(paramNode, expr(false), this.ts.getLineno());
        mustMatchToken(84, "msg.no.paren");
        this.decompiler.addToken(84);
        break;
      case 79:
        consumeToken();
        this.decompiler.addToken(79);
        paramNode = this.nf.createElementGet(paramNode, null, expr(false), 0);
        mustMatchToken(80, "msg.no.bracket.index");
        this.decompiler.addToken(80);
        break;
      case 83:
        if (!(paramBoolean))
          break;
        consumeToken();
        this.decompiler.addToken(83);
        paramNode = this.nf.createCallOrNew(37, paramNode);
        argumentList(paramNode);
        break;
      default:
        break;
      }
    }
    return paramNode;
  }

  private Node attributeAccess(Node paramNode, int paramInt)
    throws IOException
  {
    paramInt |= 2;
    int i = nextToken();
    switch (i)
    {
    case 38:
      String str = this.ts.getString();
      this.decompiler.addName(str);
      paramNode = propertyName(paramNode, str, paramInt);
      break;
    case 23:
      this.decompiler.addName("*");
      paramNode = propertyName(paramNode, "*", paramInt);
      break;
    case 79:
      this.decompiler.addToken(79);
      paramNode = this.nf.createElementGet(paramNode, null, expr(false), paramInt);
      mustMatchToken(80, "msg.no.bracket.index");
      this.decompiler.addToken(80);
      break;
    default:
      reportError("msg.no.name.after.xmlAttr");
      paramNode = this.nf.createPropertyGet(paramNode, null, "?", paramInt);
    }
    return paramNode;
  }

  private Node propertyName(Node paramNode, String paramString, int paramInt)
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    String str = null;
    if (matchToken(140))
    {
      this.decompiler.addToken(140);
      str = paramString;
      int i = nextToken();
      switch (i)
      {
      case 38:
        paramString = this.ts.getString();
        this.decompiler.addName(paramString);
        break;
      case 23:
        this.decompiler.addName("*");
        paramString = "*";
        break;
      case 79:
        this.decompiler.addToken(79);
        paramNode = this.nf.createElementGet(paramNode, str, expr(false), paramInt);
        mustMatchToken(80, "msg.no.bracket.index");
        this.decompiler.addToken(80);
        return paramNode;
      default:
        reportError("msg.no.name.after.coloncolon");
        paramString = "?";
      }
    }
    paramNode = this.nf.createPropertyGet(paramNode, str, paramString, paramInt);
    return paramNode;
  }

  private Node primaryExpr()
    throws IOException, sun.org.mozilla.javascript.internal.Parser.ParserException
  {
    label233: Node localNode;
    Object localObject1;
    String str1;
    int k;
    int i = nextFlaggedToken();
    int j = i & 0xFFFF;
    switch (j)
    {
    case 105:
      return function(2);
    case 79:
      localObject1 = new ObjArray();
      k = 0;
      this.decompiler.addToken(79);
      int l = 1;
      while (true)
      {
        while (true)
        {
          while (true)
          {
            j = peekToken();
            if (j != 85)
              break label233;
            consumeToken();
            this.decompiler.addToken(85);
            if (l != 0)
              break;
            l = 1;
          }
          ((ObjArray)localObject1).add(null);
          ++k;
        }
        if (j == 80)
        {
          consumeToken();
          this.decompiler.addToken(80);
          break;
        }
        if (l == 0)
          reportError("msg.no.bracket.arg");
        ((ObjArray)localObject1).add(assignExpr(false));
        l = 0;
      }
      return this.nf.createArrayLiteral((ObjArray)localObject1, k);
    case 81:
      localObject1 = new ObjArray();
      this.decompiler.addToken(81);
      if (!(matchToken(82)))
      {
        Object localObject2;
        k = 1;
        do
        {
          if (k == 0)
            this.decompiler.addToken(85);
          else
            k = 0;
          j = peekToken();
          switch (j)
          {
          case 38:
          case 40:
            consumeToken();
            String str3 = this.ts.getString();
            if (j == 38)
              this.decompiler.addName(str3);
            else
              this.decompiler.addString(str3);
            localObject2 = ScriptRuntime.getIndexObject(str3);
            break;
          case 39:
            consumeToken();
            double d2 = this.ts.getNumber();
            this.decompiler.addNumber(d2);
            localObject2 = ScriptRuntime.getIndexObject(d2);
            break;
          case 82:
            break;
          default:
            reportError("msg.bad.prop");
            break;
            mustMatchToken(99, "msg.no.colon.prop");
            this.decompiler.addToken(64);
            ((ObjArray)localObject1).add(localObject2);
            ((ObjArray)localObject1).add(assignExpr(false));
          }
        }
        while (matchToken(85));
        mustMatchToken(82, "msg.no.brace.prop");
      }
      this.decompiler.addToken(82);
      return this.nf.createObjectLiteral((ObjArray)localObject1);
    case 83:
      this.decompiler.addToken(83);
      localNode = expr(false);
      this.decompiler.addToken(84);
      mustMatchToken(84, "msg.no.paren");
      return localNode;
    case 143:
      mustHaveXML();
      this.decompiler.addToken(143);
      localNode = attributeAccess(null, 0);
      return localNode;
    case 38:
      localObject1 = this.ts.getString();
      if (((i & 0x20000) != 0) && (peekToken() == 99))
        return this.nf.createLabel(this.ts.getLineno());
      this.decompiler.addName((String)localObject1);
      if (this.compilerEnv.isXmlAvailable())
        localNode = propertyName(null, (String)localObject1, 0);
      else
        localNode = this.nf.createName((String)localObject1);
      return localNode;
    case 39:
      double d1 = this.ts.getNumber();
      this.decompiler.addNumber(d1);
      return this.nf.createNumber(d1);
    case 40:
      str1 = this.ts.getString();
      this.decompiler.addString(str1);
      return this.nf.createString(str1);
    case 24:
    case 96:
      this.ts.readRegExp(j);
      str1 = this.ts.regExpFlags;
      this.ts.regExpFlags = null;
      String str2 = this.ts.getString();
      this.decompiler.addRegexp(str2, str1);
      int i1 = this.currentScriptOrFn.addRegexp(str2, str1);
      return this.nf.createRegExp(i1);
    case 41:
    case 42:
    case 43:
    case 44:
      this.decompiler.addToken(j);
      return this.nf.createLeaf(j);
    case 123:
      reportError("msg.reserved.id");
      break;
    case -1:
      break;
    case 0:
      reportError("msg.unexpected.eof");
      break;
    default:
      reportError("msg.syntax");
    }
    return ((Node)null);
  }

  private static class ParserException extends RuntimeException
  {
    static final long serialVersionUID = 5882582646773765630L;
  }
}
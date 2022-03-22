package sun.org.mozilla.javascript.internal;

public class ScriptOrFnNode extends Node
{
  private int encodedSourceStart;
  private int encodedSourceEnd;
  private String sourceName;
  private int baseLineno = -1;
  private int endLineno = -1;
  private ObjArray functions;
  private ObjArray regexps;
  private ObjArray itsVariables = new ObjArray();
  private ObjToIntMap itsVariableNames = new ObjToIntMap(11);
  private int varStart;
  private Object compilerData;

  public ScriptOrFnNode(int paramInt)
  {
    super(paramInt);
  }

  public final String getSourceName()
  {
    return this.sourceName;
  }

  public final void setSourceName(String paramString)
  {
    this.sourceName = paramString;
  }

  public final int getEncodedSourceStart()
  {
    return this.encodedSourceStart;
  }

  public final int getEncodedSourceEnd()
  {
    return this.encodedSourceEnd;
  }

  public final void setEncodedSourceBounds(int paramInt1, int paramInt2)
  {
    this.encodedSourceStart = paramInt1;
    this.encodedSourceEnd = paramInt2;
  }

  public final int getBaseLineno()
  {
    return this.baseLineno;
  }

  public final void setBaseLineno(int paramInt)
  {
    if ((paramInt < 0) || (this.baseLineno >= 0))
      Kit.codeBug();
    this.baseLineno = paramInt;
  }

  public final int getEndLineno()
  {
    return this.baseLineno;
  }

  public final void setEndLineno(int paramInt)
  {
    if ((paramInt < 0) || (this.endLineno >= 0))
      Kit.codeBug();
    this.endLineno = paramInt;
  }

  public final int getFunctionCount()
  {
    if (this.functions == null)
      return 0;
    return this.functions.size();
  }

  public final FunctionNode getFunctionNode(int paramInt)
  {
    return ((FunctionNode)this.functions.get(paramInt));
  }

  public final int addFunction(FunctionNode paramFunctionNode)
  {
    if (paramFunctionNode == null)
      Kit.codeBug();
    if (this.functions == null)
      this.functions = new ObjArray();
    this.functions.add(paramFunctionNode);
    return (this.functions.size() - 1);
  }

  public final int getRegexpCount()
  {
    if (this.regexps == null)
      return 0;
    return (this.regexps.size() / 2);
  }

  public final String getRegexpString(int paramInt)
  {
    return ((String)this.regexps.get(paramInt * 2));
  }

  public final String getRegexpFlags(int paramInt)
  {
    return ((String)this.regexps.get(paramInt * 2 + 1));
  }

  public final int addRegexp(String paramString1, String paramString2)
  {
    if (paramString1 == null)
      Kit.codeBug();
    if (this.regexps == null)
      this.regexps = new ObjArray();
    this.regexps.add(paramString1);
    this.regexps.add(paramString2);
    return (this.regexps.size() / 2 - 1);
  }

  public final boolean hasParamOrVar(String paramString)
  {
    return this.itsVariableNames.has(paramString);
  }

  public final int getParamOrVarIndex(String paramString)
  {
    return this.itsVariableNames.get(paramString, -1);
  }

  public final String getParamOrVarName(int paramInt)
  {
    return ((String)this.itsVariables.get(paramInt));
  }

  public final int getParamCount()
  {
    return this.varStart;
  }

  public final int getParamAndVarCount()
  {
    return this.itsVariables.size();
  }

  public final String[] getParamAndVarNames()
  {
    int i = this.itsVariables.size();
    if (i == 0)
      return ScriptRuntime.emptyStrings;
    String[] arrayOfString = new String[i];
    this.itsVariables.toArray(arrayOfString);
    return arrayOfString;
  }

  public final void addParam(String paramString)
  {
    if (this.varStart != this.itsVariables.size())
      Kit.codeBug();
    int i = this.varStart++;
    this.itsVariables.add(paramString);
    this.itsVariableNames.put(paramString, i);
  }

  public final void addVar(String paramString)
  {
    int i = this.itsVariableNames.get(paramString, -1);
    if (i != -1)
      return;
    int j = this.itsVariables.size();
    this.itsVariables.add(paramString);
    this.itsVariableNames.put(paramString, j);
  }

  public final void removeParamOrVar(String paramString)
  {
    int i = this.itsVariableNames.get(paramString, -1);
    if (i != -1)
    {
      this.itsVariables.remove(i);
      this.itsVariableNames.remove(paramString);
      ObjToIntMap.Iterator localIterator = this.itsVariableNames.newIterator();
      localIterator.start();
      while (!(localIterator.done()))
      {
        int j = localIterator.getValue();
        if (j > i)
          localIterator.setValue(j - 1);
        localIterator.next();
      }
    }
  }

  public final Object getCompilerData()
  {
    return this.compilerData;
  }

  public final void setCompilerData(Object paramObject)
  {
    if (paramObject == null)
      throw new IllegalArgumentException();
    if (this.compilerData != null)
      throw new IllegalStateException();
    this.compilerData = paramObject;
  }
}
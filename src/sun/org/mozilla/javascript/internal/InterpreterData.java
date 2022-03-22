package sun.org.mozilla.javascript.internal;

import java.io.Serializable;
import sun.org.mozilla.javascript.internal.debug.DebuggableScript;

final class InterpreterData
  implements Serializable, DebuggableScript
{
  static final long serialVersionUID = 5067677351589230234L;
  static final int INITIAL_MAX_ICODE_LENGTH = 1024;
  static final int INITIAL_STRINGTABLE_SIZE = 64;
  static final int INITIAL_NUMBERTABLE_SIZE = 64;
  String itsName;
  String itsSourceFile;
  boolean itsNeedsActivation;
  int itsFunctionType;
  String[] itsStringTable;
  double[] itsDoubleTable;
  InterpreterData[] itsNestedFunctions;
  Object[] itsRegExpLiterals;
  byte[] itsICode;
  int[] itsExceptionTable;
  int itsMaxVars;
  int itsMaxLocals;
  int itsMaxStack;
  int itsMaxFrameArray;
  String[] argNames;
  int argCount;
  int itsMaxCalleeArgs;
  String encodedSource;
  int encodedSourceStart;
  int encodedSourceEnd;
  int languageVersion;
  boolean useDynamicScope;
  boolean topLevel;
  Object[] literalIds;
  UintMap longJumps;
  int firstLinePC = -1;
  InterpreterData parentData;
  boolean evalScriptFlag;

  InterpreterData(int paramInt, String paramString1, String paramString2)
  {
    this.languageVersion = paramInt;
    this.itsSourceFile = paramString1;
    this.encodedSource = paramString2;
    init();
  }

  InterpreterData(InterpreterData paramInterpreterData)
  {
    this.parentData = paramInterpreterData;
    this.languageVersion = paramInterpreterData.languageVersion;
    this.itsSourceFile = paramInterpreterData.itsSourceFile;
    this.encodedSource = paramInterpreterData.encodedSource;
    init();
  }

  private void init()
  {
    this.itsICode = new byte[1024];
    this.itsStringTable = new String[64];
  }

  public boolean isTopLevel()
  {
    return this.topLevel;
  }

  public boolean isFunction()
  {
    return (this.itsFunctionType != 0);
  }

  public String getFunctionName()
  {
    return this.itsName;
  }

  public int getParamCount()
  {
    return this.argCount;
  }

  public int getParamAndVarCount()
  {
    return this.argNames.length;
  }

  public String getParamOrVarName(int paramInt)
  {
    return this.argNames[paramInt];
  }

  public String getSourceName()
  {
    return this.itsSourceFile;
  }

  public boolean isGeneratedScript()
  {
    return ScriptRuntime.isGeneratedScript(this.itsSourceFile);
  }

  public int[] getLineNumbers()
  {
    return Interpreter.getLineNumbers(this);
  }

  public int getFunctionCount()
  {
    return ((this.itsNestedFunctions == null) ? 0 : this.itsNestedFunctions.length);
  }

  public DebuggableScript getFunction(int paramInt)
  {
    return this.itsNestedFunctions[paramInt];
  }

  public DebuggableScript getParent()
  {
    return this.parentData;
  }
}
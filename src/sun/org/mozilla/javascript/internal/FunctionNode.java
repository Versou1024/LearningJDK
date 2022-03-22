package sun.org.mozilla.javascript.internal;

public class FunctionNode extends ScriptOrFnNode
{
  public static final int FUNCTION_STATEMENT = 1;
  public static final int FUNCTION_EXPRESSION = 2;
  public static final int FUNCTION_EXPRESSION_STATEMENT = 3;
  String functionName;
  boolean itsNeedsActivation;
  int itsFunctionType;
  boolean itsIgnoreDynamicScope;

  public FunctionNode(String paramString)
  {
    super(105);
    this.functionName = paramString;
  }

  public String getFunctionName()
  {
    return this.functionName;
  }

  public boolean requiresActivation()
  {
    return this.itsNeedsActivation;
  }

  public boolean getIgnoreDynamicScope()
  {
    return this.itsIgnoreDynamicScope;
  }

  public int getFunctionType()
  {
    return this.itsFunctionType;
  }
}
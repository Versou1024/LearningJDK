package sun.org.mozilla.javascript.internal;

import java.util.Hashtable;

public class CompilerEnvirons
{
  private ErrorReporter errorReporter = DefaultErrorReporter.instance;
  private int languageVersion = 0;
  private boolean generateDebugInfo = true;
  private boolean useDynamicScope = false;
  private boolean reservedKeywordAsIdentifier = false;
  private boolean allowMemberExprAsFunctionName = false;
  private boolean xmlAvailable = true;
  private int optimizationLevel = 0;
  private boolean generatingSource = true;
  Hashtable activationNames;

  public void initFromContext(Context paramContext)
  {
    setErrorReporter(paramContext.getErrorReporter());
    this.languageVersion = paramContext.getLanguageVersion();
    this.useDynamicScope = paramContext.compileFunctionsWithDynamicScopeFlag;
    this.generateDebugInfo = ((!(paramContext.isGeneratingDebugChanged())) || (paramContext.isGeneratingDebug()));
    this.reservedKeywordAsIdentifier = paramContext.hasFeature(3);
    this.allowMemberExprAsFunctionName = paramContext.hasFeature(2);
    this.xmlAvailable = paramContext.hasFeature(6);
    this.optimizationLevel = paramContext.getOptimizationLevel();
    this.generatingSource = paramContext.isGeneratingSource();
    this.activationNames = paramContext.activationNames;
  }

  public final ErrorReporter getErrorReporter()
  {
    return this.errorReporter;
  }

  public void setErrorReporter(ErrorReporter paramErrorReporter)
  {
    if (paramErrorReporter == null)
      throw new IllegalArgumentException();
    this.errorReporter = paramErrorReporter;
  }

  public final int getLanguageVersion()
  {
    return this.languageVersion;
  }

  public void setLanguageVersion(int paramInt)
  {
    Context.checkLanguageVersion(paramInt);
    this.languageVersion = paramInt;
  }

  public final boolean isGenerateDebugInfo()
  {
    return this.generateDebugInfo;
  }

  public void setGenerateDebugInfo(boolean paramBoolean)
  {
    this.generateDebugInfo = paramBoolean;
  }

  public final boolean isUseDynamicScope()
  {
    return this.useDynamicScope;
  }

  public final boolean isReservedKeywordAsIdentifier()
  {
    return this.reservedKeywordAsIdentifier;
  }

  public void setReservedKeywordAsIdentifier(boolean paramBoolean)
  {
    this.reservedKeywordAsIdentifier = paramBoolean;
  }

  public final boolean isAllowMemberExprAsFunctionName()
  {
    return this.allowMemberExprAsFunctionName;
  }

  public void setAllowMemberExprAsFunctionName(boolean paramBoolean)
  {
    this.allowMemberExprAsFunctionName = paramBoolean;
  }

  public final boolean isXmlAvailable()
  {
    return this.xmlAvailable;
  }

  public void setXmlAvailable(boolean paramBoolean)
  {
    this.xmlAvailable = paramBoolean;
  }

  public final int getOptimizationLevel()
  {
    return this.optimizationLevel;
  }

  public void setOptimizationLevel(int paramInt)
  {
    Context.checkOptimizationLevel(paramInt);
    this.optimizationLevel = paramInt;
  }

  public final boolean isGeneratingSource()
  {
    return this.generatingSource;
  }

  public void setGeneratingSource(boolean paramBoolean)
  {
    this.generatingSource = paramBoolean;
  }
}
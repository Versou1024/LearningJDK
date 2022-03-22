package sun.text.normalizer;

import java.text.ParsePosition;

/**
 * @deprecated
 */
public abstract interface SymbolTable
{

  /**
   * @deprecated
   */
  public static final char SYMBOL_REF = 36;

  /**
   * @deprecated
   */
  public abstract char[] lookup(String paramString);

  /**
   * @deprecated
   */
  public abstract UnicodeMatcher lookupMatcher(int paramInt);

  /**
   * @deprecated
   */
  public abstract String parseReference(String paramString, ParsePosition paramParsePosition, int paramInt);
}
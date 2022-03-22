package sun.print;

import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintRequestAttribute;

public final class DialogTypeSelection extends EnumSyntax
  implements PrintRequestAttribute
{
  public static final DialogTypeSelection NATIVE = new DialogTypeSelection(0);
  public static final DialogTypeSelection COMMON = new DialogTypeSelection(1);
  private static final String[] myStringTable = { "native", "common" };
  private static final DialogTypeSelection[] myEnumValueTable = { NATIVE, COMMON };

  protected DialogTypeSelection(int paramInt)
  {
    super(paramInt);
  }

  protected String[] getStringTable()
  {
    return myStringTable;
  }

  protected EnumSyntax[] getEnumValueTable()
  {
    return myEnumValueTable;
  }

  public final Class getCategory()
  {
    return DialogTypeSelection.class;
  }

  public final String getName()
  {
    return "dialog-type-selection";
  }
}
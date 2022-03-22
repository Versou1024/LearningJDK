package sun.print;

import javax.print.attribute.PrintRequestAttribute;

public final class SunMinMaxPage
  implements PrintRequestAttribute
{
  private int page_max;
  private int page_min;

  public SunMinMaxPage(int paramInt1, int paramInt2)
  {
    this.page_min = paramInt1;
    this.page_max = paramInt2;
  }

  public final Class getCategory()
  {
    return SunMinMaxPage.class;
  }

  public final int getMin()
  {
    return this.page_min;
  }

  public final int getMax()
  {
    return this.page_max;
  }

  public final String getName()
  {
    return "sun-page-minmax";
  }
}
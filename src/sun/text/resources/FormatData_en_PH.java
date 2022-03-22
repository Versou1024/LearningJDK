package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_en_PH extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "NumberPatterns", { "#,##0.###", "¤#,##0.00;(¤#,##0.00)", "#,##0%" } }, { "NumberElements", { ".", ",", ";", "%", "0", "#", "-", "E", "‰", "∞", "NaN" } }, { "DateTimePatterns", { "h:mm:ss a z", "h:mm:ss a z", "h:mm:ss a", "h:mm a", "EEEE, MMMM d, yyyy", "MMMM d, yyyy", "MM d, yy", "M/d/yy", "{1} {0}" } } };
  }
}
package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_fr_CH extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "NumberPatterns", { "#,##0.###;-#,##0.###", "¤ #,##0.00;¤-#,##0.00", "#,##0 %" } }, { "NumberElements", { ".", "'", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "HH.mm.' h' z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE, d. MMMM yyyy", "d. MMMM yyyy", "d MMM yyyy", "dd.MM.yy", "{1} {0}" } }, { "DateTimePatternChars", "GaMjkHmsSEDFwWxhKzZ" } };
  }
}
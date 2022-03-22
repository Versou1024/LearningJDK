package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_fr_CA extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "NumberPatterns", { "#,##0.###;-#,##0.###", "#,##0.00 ¤;(#,##0.00¤)", "#,##0 %" } }, { "DateTimePatterns", { "H' h 'mm z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE d MMMM yyyy", "d MMMM yyyy", "yyyy-MM-dd", "yy-MM-dd", "{1} {0}" } }, { "DateTimePatternChars", "GaMjkHmsSEDFwWxhKzZ" } };
  }
}
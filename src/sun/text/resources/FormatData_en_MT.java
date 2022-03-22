package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_en_MT extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "NumberPatterns", { "#,##0.###", "¤#,##0.00", "#,##0%" } }, { "NumberElements", { ".", ",", ";", "%", "0", "#", "-", "E", "‰", "∞", "NaN" } }, { "DateTimePatterns", { "HH:mm:ss z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE, d MMMM yyyy", "dd MMMM yyyy", "dd MMM yyyy", "dd/MM/yyyy", "{1} {0}" } } };
  }
}
package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_pt_BR extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "NumberPatterns", { "#,##0.###;-#,##0.###", "¤ #,##0.00;-¤ #,##0.00", "#,##0%" } }, { "DateTimePatterns", { "HH'h'mm'min'ss's' z", "H'h'm'min's's' z", "HH:mm:ss", "HH:mm", "EEEE, d' de 'MMMM' de 'yyyy", "d' de 'MMMM' de 'yyyy", "dd/MM/yyyy", "dd/MM/yy", "{1} {0}" } } };
  }
}
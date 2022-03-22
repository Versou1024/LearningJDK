package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_es extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre", "" } }, { "MonthAbbreviations", { "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic", "" } }, { "DayNames", { "domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado" } }, { "DayAbbreviations", { "dom", "lun", "mar", "mié", "jue", "vie", "sáb" } }, { "NumberPatterns", { "#,##0.###;-#,##0.###", "¤#,##0.00;(¤#,##0.00)", "#,##0%" } }, { "NumberElements", { ",", ".", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "HH'H'mm'' z", "H:mm:ss z", "H:mm:ss", "H:mm", "EEEE d' de 'MMMM' de 'yyyy", "d' de 'MMMM' de 'yyyy", "dd-MMM-yyyy", "d/MM/yy", "{1} {0}" } }, { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" } };
  }
}
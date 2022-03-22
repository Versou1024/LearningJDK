package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_is extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "janúar", "febrúar", "mars", "apríl", "maí", "júní", "júlí", "ágúst", "september", "október", "nóvember", "desember", "" } }, { "MonthAbbreviations", { "jan.", "feb.", "mar.", "apr.", "maí", "jún.", "júl.", "ágú.", "sep.", "okt.", "nóv.", "des.", "" } }, { "DayNames", { "sunnudagur", "mánudagur", "þriðjudagur", "miðvikudagur", "fimmtudagur", "föstudagur", "laugardagur" } }, { "DayAbbreviations", { "sun.", "mán.", "þri.", "mið.", "fim.", "fös.", "lau." } }, { "NumberElements", { ",", ".", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "HH:mm:ss z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "d. MMMM yyyy", "d. MMMM yyyy", "d.M.yyyy", "d.M.yyyy", "{1} {0}" } }, { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" } };
  }
}
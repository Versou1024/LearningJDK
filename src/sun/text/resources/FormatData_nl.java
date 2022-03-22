package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_nl extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "januari", "februari", "maart", "april", "mei", "juni", "juli", "augustus", "september", "oktober", "november", "december", "" } }, { "MonthAbbreviations", { "jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec", "" } }, { "DayNames", { "zondag", "maandag", "dinsdag", "woensdag", "donderdag", "vrijdag", "zaterdag" } }, { "DayAbbreviations", { "zo", "ma", "di", "wo", "do", "vr", "za" } }, { "Eras", { "v. Chr.", "n. Chr." } }, { "NumberElements", { ",", ".", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "H:mm:ss' uur' z", "H:mm:ss z", "H:mm:ss", "H:mm", "EEEE d MMMM yyyy", "d MMMM yyyy", "d-MMM-yyyy", "d-M-yy", "{1} {0}" } }, { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" } };
  }
}
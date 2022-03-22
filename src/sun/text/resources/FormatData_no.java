package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_no extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "januar", "februar", "mars", "april", "mai", "juni", "juli", "august", "september", "oktober", "november", "desember", "" } }, { "MonthAbbreviations", { "jan", "feb", "mar", "apr", "mai", "jun", "jul", "aug", "sep", "okt", "nov", "des", "" } }, { "DayNames", { "søndag", "mandag", "tirsdag", "onsdag", "torsdag", "fredag", "lørdag" } }, { "DayAbbreviations", { "sø", "ma", "ti", "on", "to", "fr", "lø" } }, { "NumberElements", { ",", " ", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "'kl 'HH.mm z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "d. MMMM yyyy", "d. MMMM yyyy", "dd.MMM.yyyy", "dd.MM.yy", "{1} {0}" } }, { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" } };
  }
}
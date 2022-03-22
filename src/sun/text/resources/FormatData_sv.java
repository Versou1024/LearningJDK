package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_sv extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "januari", "februari", "mars", "april", "maj", "juni", "juli", "augusti", "september", "oktober", "november", "december", "" } }, { "MonthAbbreviations", { "jan", "feb", "mar", "apr", "maj", "jun", "jul", "aug", "sep", "okt", "nov", "dec", "" } }, { "DayNames", { "söndag", "måndag", "tisdag", "onsdag", "torsdag", "fredag", "lördag" } }, { "DayAbbreviations", { "sö", "må", "ti", "on", "to", "fr", "lö" } }, { "AmPmMarkers", { "fm", "em" } }, { "NumberElements", { ",", " ", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "'kl 'H:mm z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "'den 'd MMMM yyyy", "'den 'd MMMM yyyy", "yyyy-MMM-dd", "yyyy-MM-dd", "{1} {0}" } }, { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" } };
  }
}
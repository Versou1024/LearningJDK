package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_de extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember", "" } }, { "MonthAbbreviations", { "Jan", "Feb", "Mrz", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez", "" } }, { "DayNames", { "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag" } }, { "DayAbbreviations", { "So", "Mo", "Di", "Mi", "Do", "Fr", "Sa" } }, { "Eras", { "v. Chr.", "n. Chr." } }, { "NumberElements", { ",", ".", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "HH:mm' Uhr 'z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE, d. MMMM yyyy", "d. MMMM yyyy", "dd.MM.yyyy", "dd.MM.yy", "{1} {0}" } }, { "DateTimePatternChars", "GuMtkHmsSEDFwWahKzZ" } };
  }
}
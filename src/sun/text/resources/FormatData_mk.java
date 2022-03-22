package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_mk extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "јануари", "февруари", "март", "април", "мај", "јуни", "јули", "август", "септември", "октомври", "ноември", "декември", "" } }, { "MonthAbbreviations", { "јан.", "фев.", "мар.", "апр.", "мај.", "јун.", "јул.", "авг.", "септ.", "окт.", "ноем.", "декем.", "" } }, { "DayNames", { "недела", "понеделник", "вторник", "среда", "четврток", "петок", "сабота" } }, { "DayAbbreviations", { "нед.", "пон.", "вт.", "сре.", "чет.", "пет.", "саб." } }, { "Eras", { "пр.н.е.", "ае." } }, { "NumberElements", { ",", ".", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "HH:mm:ss z", "HH:mm:ss z", "HH:mm:", "HH:mm", "EEEE, d, MMMM yyyy", "d, MMMM yyyy", "d.M.yyyy", "d.M.yy", "{1} {0}" } }, { "DateTimePatternChars", "GuMtkHmsSEDFwWahKzZ" } };
  }
}
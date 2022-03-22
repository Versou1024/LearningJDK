package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_hu extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "január", "február", "március", "április", "május", "június", "július", "augusztus", "szeptember", "október", "november", "december", "" } }, { "MonthAbbreviations", { "jan.", "febr.", "márc.", "ápr.", "máj.", "jún.", "júl.", "aug.", "szept.", "okt.", "nov.", "dec.", "" } }, { "DayNames", { "vasárnap", "hétfő", "kedd", "szerda", "csütörtök", "péntek", "szombat" } }, { "DayAbbreviations", { "V", "H", "K", "Sze", "Cs", "P", "Szo" } }, { "AmPmMarkers", { "DE", "DU" } }, { "Eras", { "i.e.", "i.u." } }, { "NumberElements", { ",", " ", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "H:mm:ss z", "H:mm:ss z", "H:mm:ss", "H:mm", "yyyy. MMMM d.", "yyyy. MMMM d.", "yyyy.MM.dd.", "yyyy.MM.dd.", "{1} {0}" } }, { "DateTimePatternChars", "GanjkHmsSEDFwWxhKzZ" } };
  }
}
package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_pl extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec", "lipiec", "sierpień", "wrzesień", "październik", "listopad", "grudzień", "" } }, { "MonthAbbreviations", { "sty", "lut", "mar", "kwi", "maj", "cze", "lip", "sie", "wrz", "paź", "lis", "gru", "" } }, { "DayNames", { "niedziela", "poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota" } }, { "DayAbbreviations", { "N", "Pn", "Wt", "Śr", "Cz", "Pt", "So" } }, { "Eras", { "p.n.e.", "n.e." } }, { "NumberElements", { ",", " ", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "HH:mm:ss z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE, d MMMM yyyy", "d MMMM yyyy", "yyyy-MM-dd", "yy-MM-dd", "{1} {0}" } }, { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" } };
  }
}
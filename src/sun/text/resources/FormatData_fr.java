package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_fr extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre", "" } }, { "MonthAbbreviations", { "janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.", "nov.", "déc.", "" } }, { "DayNames", { "dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi" } }, { "DayAbbreviations", { "dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam." } }, { "Eras", { "BC", "ap. J.-C." } }, { "NumberPatterns", { "#,##0.###;-#,##0.###", "#,##0.00 ¤;-#,##0.00 ¤", "#,##0 %" } }, { "NumberElements", { ",", " ", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "HH' h 'mm z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE d MMMM yyyy", "d MMMM yyyy", "d MMM yyyy", "dd/MM/yy", "{1} {0}" } }, { "DateTimePatternChars", "GaMjkHmsSEDFwWxhKzZ" } };
  }
}
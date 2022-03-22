package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_in extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember", "" } }, { "MonthAbbreviations", { "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des", "" } }, { "DayNames", { "Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu" } }, { "DayAbbreviations", { "Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab" } }, { "Eras", { "BCE", "CE" } }, { "NumberPatterns", { "#,##0.###", "¤#,##0.00", "#,##0%" } }, { "NumberElements", { ",", ".", ";", "%", "0", "#", "-", "E", "‰", "∞", "NaN" } }, { "DateTimePatterns", { "HH:mm:ss z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE, yyyy MMMM dd", "yyyy MMMM d", "yyyy MMM d", "yy/MM/dd", "{1} {0}" } } };
  }
}
package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_ms extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "Januari", "Februari", "Mac", "April", "Mei", "Jun", "Julai", "Ogos", "September", "Oktober", "November", "Disember", "" } }, { "MonthAbbreviations", { "Jan", "Feb", "Mac", "Apr", "Mei", "Jun", "Jul", "Ogos", "Sep", "Okt", "Nov", "Dis", "" } }, { "DayNames", { "Ahad", "Isnin", "Selasa", "Rabu", "Khamis", "Jumaat", "Sabtu" } }, { "DayAbbreviations", { "Ahd", "Isn", "Sel", "Rab", "Kha", "Jum", "Sab" } }, { "Eras", { "BCE", "CE" } }, { "NumberPatterns", { "#,##0.###", "¤ #,##0.00", "#,##0%" } }, { "NumberElements", { ".", ",", ";", "%", "0", "#", "-", "E", "‰", "∞", "NaN" } }, { "DateTimePatterns", { "HH:mm:ss z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE, yyyy MMMM dd", "yyyy MMMM d", "yyyy MMM d", "yy/MM/dd", "{1} {0}" } } };
  }
}
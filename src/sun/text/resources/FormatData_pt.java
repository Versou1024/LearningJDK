package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_pt extends ListResourceBundle
{
  protected final Object[][] getContents()
  {
    return { { "MonthNames", { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro", "" } }, { "MonthAbbreviations", { "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez", "" } }, { "DayNames", { "Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado" } }, { "DayAbbreviations", { "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb" } }, { "NumberElements", { ",", ".", ";", "%", "0", "#", "-", "E", "‰", "∞", "�" } }, { "DateTimePatterns", { "HH'H'mm'm' z", "H:mm:ss z", "H:mm:ss", "H:mm", "EEEE, d' de 'MMMM' de 'yyyy", "d' de 'MMMM' de 'yyyy", "d/MMM/yyyy", "dd-MM-yyyy", "{1} {0}" } }, { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" } };
  }
}
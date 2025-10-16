package br.edu.atitus.currency_service.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter BCB_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    // Último dia útil a partir de uma data específica
    public static LocalDate getUltimoDiaUtil(LocalDate data) {
        while (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            data = data.minusDays(1);
        }
        return data;
    }

    // Último dia útil a partir de hoje (sem parâmetro)
    public static LocalDate getUltimoDiaUtil() {
        return getUltimoDiaUtil(LocalDate.now());
    }

    // Formata último dia útil
    public static String getUltimoDiaUtilFormatado() {
        return getUltimoDiaUtil().format(BCB_FORMATTER);
    }

    // Formata último dia útil retroativo N dias
    public static String getUltimoDiaUtilRetroativo(int diasRetrocedidos) {
        LocalDate dia = getUltimoDiaUtil().minusDays(diasRetrocedidos);
        while (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dia = dia.minusDays(1);
        }
        return dia.format(BCB_FORMATTER);
    }
}

package com.auroramc.punish.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
    public static String formateDate(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    public static String getOrdinalNumber(int number) {
        String[] suffixes = {"º", "º", "º", "º", "º", "º", "º", "º", "º", "º"};

        if (number >= 11 && number <= 13) {
            return number + "º";
        } else {
            int suffixIndex = number % 10;
            return number + suffixes[suffixIndex];
        }
    }

    public static String formatDuration(String duration) {
        if (duration.equals("0")) {
            return "Permanente";
        }

        long millis = durationToMillis(duration);
        return millisToReadable(millis);
    }

    public static long durationToMillis(String duration) {
        if (duration.equals("0")) {
            return 0;
        }

        int value = Integer.parseInt(duration.substring(0, duration.length() - 1));
        char unit = duration.charAt(duration.length() - 1);

        switch (unit) {
            case 's':
                return value * 1000L;
            case 'm':
                return value * 60 * 1000L;
            case 'h':
                return value * 60 * 60 * 1000L;
            case 'd':
                return value * 24 * 60 * 60 * 1000L;
            case 'w':
                return value * 7 * 24 * 60 * 60 * 1000L;
            default:
                throw new IllegalArgumentException("Unidade de tempo desconhecida: " + unit);
        }
    }

    public static String millisToReadable(long millis) {
        if (millis == 0) {
            return "Permanente";
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) {
            return years + (years > 1 ? " anos" : " ano");
        } else if (months > 0) {
            return months + (months > 1 ? " meses" : " mês");
        } else if (weeks > 0) {
            if (days % 7 == 0) {
                return weeks + (weeks > 1 ? " semanas" : " semana");
            } else {
                return weeks + (weeks > 1 ? " semanas e " : " semana e ") + (days % 7) + (days % 7 > 1 ? " dias" : " dia");
            }
        } else if (days > 0) {
            return days + (days > 1 ? " dias" : " dia");
        } else if (hours > 0) {
            if (minutes % 60 == 0) {
                return hours + (hours > 1 ? " horas" : " hora");
            } else {
                return hours + (hours > 1 ? " horas e " : " hora e ") + (minutes % 60) + (minutes % 60 > 1 ? " minutos" : " minuto");
            }
        } else if (minutes > 0) {
            if (seconds % 60 == 0) {
                return minutes + (minutes > 1 ? " minutos" : " minuto");
            } else {
                return minutes + (minutes > 1 ? " minutos e " : " minuto e ") + (seconds % 60) + (seconds % 60 > 1 ? " segundos" : " segundo");
            }
        } else {
            return seconds + (seconds > 1 ? " segundos" : " segundo");
        }
    }
    public static long parseDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
        try {
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}

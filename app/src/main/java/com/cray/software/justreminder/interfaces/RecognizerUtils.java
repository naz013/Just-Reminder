package com.cray.software.justreminder.interfaces;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecognizerUtils {

    public static String convertToNumbered(String input){
        StringBuilder sb = new StringBuilder();
        input = input.toLowerCase();
        String[] splitParts = input.split("\\s+");
        int decimal = 0;
        boolean isDecimalBefore = false;
        for (String splitPart : splitParts) {
            splitPart = splitPart.trim();
            if (isNumber(splitPart)){
                if (isDecimal(splitPart)){
                    isDecimalBefore = true;
                    decimal = getNumberFromString(splitPart);
                } else {
                    if (isDecimalBefore){
                        isDecimalBefore = false;
                        int number = getNumberFromString(splitPart);
                        number = number + decimal;
                        decimal = 0;
                        sb.append(" ").append(number);
                    } else sb.append(" ").append(getNumberFromString(splitPart));
                }
            } else sb.append(" ").append(splitPart);
        }
        return sb.toString().trim();
    }

    public static boolean isDecimal(String input){
        return getNumberFromString(input) > 19;
    }

    public static boolean isNumber(String input){
        return getNumberFromString(input) > 0;
    }

    public static int getNumberFromString(String input){
        int number = 0;
        input = input.toLowerCase();
        if (input.matches("one") || input.matches("один") || input.matches("одну") ||
                input.matches("одна")) number = 1;
        if (input.matches("two") || input.matches("два") || input.matches("дві") ||
                input.matches("две")) number = 2;
        if (input.matches("three") || input.matches("три")) number = 3;
        if (input.matches("four") || input.matches("чотири") || input.matches("четыре")) number = 4;
        if (input.matches("five") || input.matches("п'ять") || input.matches("пять")) number = 5;
        if (input.matches("six") || input.matches("шість") || input.matches("шесть")) number = 6;
        if (input.matches("seven") || input.matches("сім") || input.matches("семь")) number = 7;
        if (input.matches("eight") || input.matches("вісім") || input.matches("восемь")) number = 8;
        if (input.matches("nine") || input.matches("дев'ять") || input.matches("девять")) number = 9;
        if (input.matches("ten") || input.matches("десять") || input.matches("десять")) number = 10;
        if (input.matches("eleven") || input.matches("одинадцять") || input.matches("одиннадцать")) number = 11;
        if (input.matches("twelve") || input.matches("дванадцять") || input.matches("двенадцать")) number = 12;
        if (input.matches("thirteen") || input.matches("тринадцять") || input.matches("тринадцать")) number = 13;
        if (input.matches("fourteen") || input.matches("чотирнадцять") || input.matches("четырнадцать")) number = 14;
        if (input.matches("fifteen") || input.matches("п'ятнадцять") || input.matches("пятнадцать")) number = 15;
        if (input.matches("sixteen") || input.matches("шістнадцять") || input.matches("шестнадцать")) number = 16;
        if (input.matches("seventeen") || input.matches("сімнадцять") || input.matches("семнадцать")) number = 17;
        if (input.matches("eighteen") || input.matches("вісімнадцять") || input.matches("восемнадцать")) number = 18;
        if (input.matches("nineteen") || input.matches("дев'ятнадцять") || input.matches("девятнадцать")) number = 19;
        if (input.matches("twenty") || input.matches("двадцять") || input.matches("двадцать")) number = 20;
        if (input.matches("thirty") || input.matches("тридцять") || input.matches("тридцать")) number = 30;
        if (input.matches("forty") || input.matches("сорок") || input.matches("сорок")) number = 40;
        if (input.matches("fifty") || input.matches("п'ятдесят") || input.matches("пятьдесят")) number = 50;
        if (input.matches("sixty") || input.matches("шістдесят") || input.matches("шестьдесят")) number = 60;
        if (input.matches("seventy") || input.matches("сімдесят") || input.matches("семьдесят")) number = 70;
        if (input.matches("eighty") || input.matches("вісімдесят") || input.matches("восемьдесят")) number = 80;
        if (input.matches("ninety") || input.matches("дев'яносто") || input.matches("девяносто")) number = 90;

        if (input.matches("first") || input.matches("першого") || input.matches("першій")
                || input.matches("первого")) number = 1;
        if (input.matches("second") || input.matches("другого") || input.matches("другій")
                || input.matches("второго")) number = 2;
        if (input.matches("third") || input.matches("третього") || input.matches("третій")
                || input.matches("третьего")) number = 3;
        if (input.matches("fourth") || input.matches("четвертого") || input.matches("четвертій")
                || input.matches("четвертого")) number = 4;
        if (input.matches("fifth") || input.matches("п'ятого") || input.matches("п'ятій")
                || input.matches("пятого")) number = 5;
        if (input.matches("sixth") || input.matches("шостого") || input.matches("шостій")
                || input.matches("шестого")) number = 6;
        if (input.matches("seventh") || input.matches("сьомого") || input.matches("сьомій")
                || input.matches("седьмого")) number = 7;
        if (input.matches("eighth") || input.matches("восьмого") || input.matches("восьмій")
                || input.matches("восьмого")) number = 8;
        if (input.matches("ninth") || input.matches("дев'ятого") || input.matches("дев'ятій")
                || input.matches("девятого")) number = 9;
        if (input.matches("tenth") || input.matches("десятого") || input.matches("десятого") ||
                input.matches("десятій")) number = 10;
        if (input.matches("eleventh") || input.matches("одинадцятого") || input.matches("одинадцятій")
                || input.matches("одиннадцатого")) number = 11;
        if (input.matches("twelfth") || input.matches("дванадцятого") || input.matches("дванадцятій")
                || input.matches("двенадцатого")) number = 12;
        if (input.matches("thirteenth") || input.matches("тринадцятого") || input.matches("тринадцятій")
                || input.matches("тринадцатого")) number = 13;
        if (input.matches("fourteenth") || input.matches("чотирнадцятого") || input.matches("чотирнадцятій")
                || input.matches("четырнадцатого")) number = 14;
        if (input.matches("fifteenth") || input.matches("п'ятнадцятого") || input.matches("п'ятнадцятій")
                || input.matches("пятнадцатого")) number = 15;
        if (input.matches("sixteenth") || input.matches("шістнадцятого") || input.matches("шістнадцятій")
                || input.matches("шестнадцатого")) number = 16;
        if (input.matches("seventeenth") || input.matches("сімнадцятого") || input.matches("сімнадцятій")
                || input.matches("семнадцатого")) number = 17;
        if (input.matches("eighteenth") || input.matches("вісімнадцятого") || input.matches("вісімнадцятій")
                || input.matches("восемнадцатого")) number = 18;
        if (input.matches("nineteenth") || input.matches("дев'ятнадцятого") || input.matches("дев'ятнадцятій")
                || input.matches("девятнадцатого")) number = 19;
        if (input.matches("twentieth") || input.matches("двадцятого") || input.matches("двадцятій")
                || input.matches("двадцатого")) number = 20;
        if (input.matches("thirtieth") || input.matches("тридцятого") || input.matches("тридцатого")) number = 30;
        if (input.matches("fortieth") || input.matches("сорокового") || input.matches("сорокового")) number = 40;
        if (input.matches("fiftieth") || input.matches("п'ятдесятого") || input.matches("пятидесятого")) number = 50;
        if (input.matches("sixtieth") || input.matches("шістдесятого") || input.matches("шестидесятого")) number = 60;
        if (input.matches("seventieth") || input.matches("сімдесятого") || input.matches("семидесятого")) number = 70;
        if (input.matches("eightieth") || input.matches("вісімдесятого") || input.matches("восьмидесятого")) number = 80;
        if (input.matches("ninetieth") || input.matches("дев'яностого") || input.matches("девяностого"))
            number = 90;
        return number;
    }

    public static boolean isNumberContains(String input){
        return findNumberInString(input) > 0;
    }

    public static int findNumberInString(String input){
        int number = 0;
        input = input.toLowerCase();
        if (input.contains("one") || input.contains("один") || input.contains("одну") ||
                input.contains("одна")) number = 1;
        if (input.contains("two") || input.contains("два") || input.contains("дві") ||
                input.contains("две")) number = 2;
        if (input.contains("three") || input.contains("три")) number = 3;
        if (input.contains("four") || input.contains("чотири") || input.contains("четыре")) number = 4;
        if (input.contains("five") || input.contains("п'ять") || input.contains("пять")) number = 5;
        if (input.contains("six") || input.contains("шість") || input.contains("шесть")) number = 6;
        if (input.contains("seven") || input.contains("сім") || input.contains("семь")) number = 7;
        if (input.contains("eight") || input.contains("вісім") || input.contains("восемь")) number = 8;
        if (input.contains("nine") || input.contains("дев'ять") || input.contains("девять")) number = 9;
        if (input.contains("ten") || input.contains("десять") || input.contains("десять")) number = 10;
        if (input.contains("eleven") || input.contains("одинадцять") || input.contains("одиннадцать")) number = 11;
        if (input.contains("twelve") || input.contains("дванадцять") || input.contains("двенадцать")) number = 12;
        if (input.contains("thirteen") || input.contains("тринадцять") || input.contains("тринадцать")) number = 13;
        if (input.contains("fourteen") || input.contains("чотирнадцять") || input.contains("четырнадцать")) number = 14;
        if (input.contains("fifteen") || input.contains("п'ятнадцять") || input.contains("пятнадцать")) number = 15;
        if (input.contains("sixteen") || input.contains("шістнадцять") || input.contains("шестнадцать")) number = 16;
        if (input.contains("seventeen") || input.contains("сімнадцять") || input.contains("семнадцать")) number = 17;
        if (input.contains("eighteen") || input.contains("вісімнадцять") || input.contains("восемнадцать")) number = 18;
        if (input.contains("nineteen") || input.contains("дев'ятнадцять") || input.contains("девятнадцать")) number = 19;
        if (input.contains("twenty") || input.contains("двадцять") || input.contains("двадцать")) number = 20;
        if (input.contains("thirty") || input.contains("тридцять") || input.contains("тридцать")) number = 30;
        if (input.contains("forty") || input.contains("сорок") || input.contains("сорок")) number = 40;
        if (input.contains("fifty") || input.contains("п'ятдесят") || input.contains("пятьдесят")) number = 50;
        if (input.contains("sixty") || input.contains("шістдесят") || input.contains("шестьдесят")) number = 60;
        if (input.contains("seventy") || input.contains("сімдесят") || input.contains("семьдесят")) number = 70;
        if (input.contains("eighty") || input.contains("вісімдесят") || input.contains("восемьдесят")) number = 80;
        if (input.contains("ninety") || input.contains("дев'яносто") || input.contains("девяносто")) number = 90;

        if (input.contains("first") || input.contains("першого") || input.contains("першій")
                || input.contains("первого")) number = 1;
        if (input.contains("second") || input.contains("другого") || input.contains("другій")
                || input.contains("второго")) number = 2;
        if (input.contains("third") || input.contains("третього") || input.contains("третій")
                || input.contains("третьего")) number = 3;
        if (input.contains("fourth") || input.contains("четвертого") || input.contains("четвертій")
                || input.contains("четвертого")) number = 4;
        if (input.contains("fifth") || input.contains("п'ятого") || input.contains("п'ятій")
                || input.contains("пятого")) number = 5;
        if (input.contains("sixth") || input.contains("шостого") || input.contains("шостій")
                || input.contains("шестого")) number = 6;
        if (input.contains("seventh") || input.contains("сьомого") || input.contains("сьомій")
                || input.contains("седьмого")) number = 7;
        if (input.contains("eighth") || input.contains("восьмого") || input.contains("восьмій")
                || input.contains("восьмого")) number = 8;
        if (input.contains("ninth") || input.contains("дев'ятого") || input.contains("дев'ятій")
                || input.contains("девятого")) number = 9;
        if (input.contains("tenth") || input.contains("десятого") || input.contains("десятого") ||
                input.contains("десятій")) number = 10;
        if (input.contains("eleventh") || input.contains("одинадцятого") || input.contains("одинадцятій")
                || input.contains("одиннадцатого")) number = 11;
        if (input.contains("twelfth") || input.contains("дванадцятого") || input.contains("дванадцятій")
                || input.contains("двенадцатого")) number = 12;
        if (input.contains("thirteenth") || input.contains("тринадцятого") || input.contains("тринадцятій")
                || input.contains("тринадцатого")) number = 13;
        if (input.contains("fourteenth") || input.contains("чотирнадцятого") || input.contains("чотирнадцятій")
                || input.contains("четырнадцатого")) number = 14;
        if (input.contains("fifteenth") || input.contains("п'ятнадцятого") || input.contains("п'ятнадцятій")
                || input.contains("пятнадцатого")) number = 15;
        if (input.contains("sixteenth") || input.contains("шістнадцятого") || input.contains("шістнадцятій")
                || input.contains("шестнадцатого")) number = 16;
        if (input.contains("seventeenth") || input.contains("сімнадцятого") || input.contains("сімнадцятій")
                || input.contains("семнадцатого")) number = 17;
        if (input.contains("eighteenth") || input.contains("вісімнадцятого") || input.contains("вісімнадцятій")
                || input.contains("восемнадцатого")) number = 18;
        if (input.contains("nineteenth") || input.contains("дев'ятнадцятого") || input.contains("дев'ятнадцятій")
                || input.contains("девятнадцатого")) number = 19;
        if (input.contains("twentieth") || input.contains("двадцятого") || input.contains("двадцятій")
                || input.contains("двадцатого")) number = 20;
        if (input.contains("thirtieth") || input.contains("тридцятого") || input.contains("тридцатого")) number = 30;
        if (input.contains("fortieth") || input.contains("сорокового") || input.contains("сорокового")) number = 40;
        if (input.contains("fiftieth") || input.contains("п'ятдесятого") || input.contains("пятидесятого")) number = 50;
        if (input.contains("sixtieth") || input.contains("шістдесятого") || input.contains("шестидесятого")) number = 60;
        if (input.contains("seventieth") || input.contains("сімдесятого") || input.contains("семидесятого")) number = 70;
        if (input.contains("eightieth") || input.contains("вісімдесятого") || input.contains("восьмидесятого")) number = 80;
        if (input.contains("ninetieth") || input.contains("дев'яностого") || input.contains("девяностого")) number = 90;
        return number;
    }

    public static int[] getIndexes(String input){
        int multiplier = 1;
        int indexStart = input.indexOf(" днів");
        int increment = 5;
        if (indexStart == -1) {
            indexStart = input.indexOf(" дні");
            multiplier = 1;
            increment = 4;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" тижнів");
            multiplier = 7;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" тижні");
            multiplier = 7;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" місяців");
            multiplier = 30;
            increment = 8;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" місяці");
            multiplier = 30;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" роки");
            multiplier = 365;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" років");
            multiplier = 365;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" дня");
            multiplier = 1;
            increment = 4;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" дней");
            multiplier = 1;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" недели");
            multiplier = 7;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" недель");
            multiplier = 7;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" месяцев");
            multiplier = 30;
            increment = 8;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" месяца");
            multiplier = 30;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" года");
            multiplier = 365;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" лет");
            multiplier = 365;
            increment = 4;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" days");
            multiplier = 1;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" weeks");
            multiplier = 7;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" months");
            multiplier = 30;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" years");
            multiplier = 365;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" дня");
            multiplier = 1;
            increment = 4;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" день");
            multiplier = 1;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" тижня");
            multiplier = 7;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" тиждень");
            multiplier = 7;
            increment = 8;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" місяця");
            multiplier = 30;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" місяць");
            multiplier = 30;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" року");
            multiplier = 365;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" дня");
            multiplier = 1;
            increment = 4;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" неделю");
            multiplier = 7;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" месяца");
            multiplier = 30;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" месяц");
            multiplier = 30;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" day");
            multiplier = 1;
            increment = 4;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" week");
            multiplier = 7;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" month");
            multiplier = 30;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" year");
            multiplier = 365;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" рік");
            multiplier = 365;
            increment = 4;
        }

        // indexes for after time reminder

        if (indexStart == -1) {
            indexStart = input.indexOf(" minutes");
            multiplier = 1;
            increment = 8;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" minute");
            multiplier = 1;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" hours");
            multiplier = 60;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" hour");
            multiplier = 60;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" хвилину");
            multiplier = 1;
            increment = 8;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" хвилини");
            multiplier = 1;
            increment = 8;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" хвилин");
            multiplier = 1;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" минуту");
            multiplier = 1;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" минуты");
            multiplier = 1;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" минут");
            multiplier = 1;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" годину");
            multiplier = 60;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" години");
            multiplier = 60;
            increment = 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" годин");
            multiplier = 60;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" часа");
            multiplier = 60;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" часов");
            multiplier = 60;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" час");
            multiplier = 60;
            increment = 4;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" год");
            multiplier = 365;
            increment = 4;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf("щодня");
            multiplier = 1;
            increment = 5;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf("щотижня");
            multiplier = 7;
            increment = 6;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf("щомісяця");
            multiplier = 30;
            increment = 8;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf("щороку");
            multiplier = 365;
            increment = 6;
        }
        return new int[] {indexStart, increment, multiplier};
    }

    public static long getLongIndexes(String input){
        long minute = 1000 * 60;
        long hour = minute * 60;
        long day = hour * 24;
        long multiplier = day;
        int indexStart = input.indexOf(" днів");
        if (indexStart == -1) {
            indexStart = input.indexOf(" дні");
            multiplier = day;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" тижнів");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" тижні");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" місяців");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" місяці");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" роки");
            multiplier = day * 365;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" років");
            multiplier = day * 365;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" дня");
            multiplier = day;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" дней");
            multiplier = day;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" недели");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" недель");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" месяцев");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" месяца");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" года");
            multiplier = day * 365;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" лет");
            multiplier = day * 365;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" days");
            multiplier = day;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" weeks");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" months");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" years");
            multiplier = day * 365;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" дня");
            multiplier = day;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" день");
            multiplier = day;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" тижня");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" тиждень");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" місяця");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" місяць");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" року");
            multiplier = day * 365;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" дня");
            multiplier = day;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" неделю");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" месяца");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" месяц");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" day");
            multiplier = day;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" week");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" month");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" year");
            multiplier = day * 365;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" рік");
            multiplier = day * 365;
        }

        // indexes for after time reminder

        if (indexStart == -1) {
            indexStart = input.indexOf(" minutes");
            multiplier = minute;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" minute");
            multiplier = minute;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" hours");
            multiplier = hour;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" hour");
            multiplier = hour;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" хвилину");
            multiplier = minute;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" хвилини");
            multiplier = minute;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" хвилин");
            multiplier = minute;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" минуту");
            multiplier = minute;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" минуты");
            multiplier = minute;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" минут");
            multiplier = minute;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" годину");
            multiplier = hour;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" години");
            multiplier = hour;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" годин");
            multiplier = hour;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" часа");
            multiplier = hour;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" часов");
            multiplier = hour;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" час");
            multiplier = hour;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf(" год");
            multiplier = day * 365;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf("щодня");
            multiplier = day;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf("щотижня");
            multiplier = day * 7;
        }
        if (indexStart == -1) {
            indexStart = input.indexOf("щомісяця");
            multiplier = day * 30;
        }
        if (indexStart == -1) {
            multiplier = day * 365;
        }
        return multiplier;
    }

    public static boolean isCalendarExportable(String input){
        input = input.toLowerCase();
        return input.matches(".*  calendar.*") ||
                input.matches(".* календарь.*") ||
                input.matches(".* календар.*");
    }

    public static int getIndexFromDay(String input){
        input = input.toLowerCase();
        int index = input.lastIndexOf(" first ");
        if (index == -1) index = input.lastIndexOf(" second ");
        if (index == -1) index = input.lastIndexOf(" third ");
        if (index == -1) index = input.lastIndexOf(" fourth ");
        if (index == -1) index = input.lastIndexOf(" fifth ");
        if (index == -1) index = input.lastIndexOf(" sixth ");
        if (index == -1) index = input.lastIndexOf(" seventh");
        if (index == -1) index = input.lastIndexOf(" eighth ");
        if (index == -1) index = input.lastIndexOf(" ninth ");
        if (index == -1) index = input.lastIndexOf(" tenth ");
        if (index == -1) index = input.lastIndexOf(" eleventh ");
        if (index == -1) index = input.lastIndexOf(" twelfth ");
        if (index == -1) index = input.lastIndexOf(" thirteenth ");
        if (index == -1) index = input.lastIndexOf(" fourteenth ");
        if (index == -1) index = input.lastIndexOf(" fifteenth ");
        if (index == -1) index = input.lastIndexOf(" sixteenth ");
        if (index == -1) index = input.lastIndexOf(" seventeenth ");
        if (index == -1) index = input.lastIndexOf(" eighteenth ");
        if (index == -1) index = input.lastIndexOf(" nineteenth ");
        if (index == -1) index = input.lastIndexOf(" twentieth ");
        if (index == -1) index = input.lastIndexOf(" twenty first ");
        if (index == -1) index = input.lastIndexOf(" twenty second ");
        if (index == -1) index = input.lastIndexOf(" twenty third ");
        if (index == -1) index = input.lastIndexOf(" twenty fourth ");
        if (index == -1) index = input.lastIndexOf(" twenty fifth ");
        if (index == -1) index = input.lastIndexOf(" twenty-sixth ");
        if (index == -1) index = input.lastIndexOf(" twenty seventh ");
        if (index == -1) index = input.lastIndexOf(" twenty eighth ");
        if (index == -1) index = input.lastIndexOf(" twenty ninth ");
        if (index == -1) index = input.lastIndexOf(" thirtieth ");
        if (index == -1) index = input.lastIndexOf(" thirty first ");

        if (index == -1) index = input.lastIndexOf(" першого ");
        if (index == -1) index = input.lastIndexOf(" другого ");
        if (index == -1) index = input.lastIndexOf(" третього ");
        if (index == -1) index = input.lastIndexOf(" четвертого ");
        if (index == -1) index = input.lastIndexOf(" п'ятого ");
        if (index == -1) index = input.lastIndexOf(" шостого ");
        if (index == -1) index = input.lastIndexOf(" сьомого");
        if (index == -1) index = input.lastIndexOf(" восьмого ");
        if (index == -1) index = input.lastIndexOf(" дев'ятого ");
        if (index == -1) index = input.lastIndexOf(" десятого ");
        if (index == -1) index = input.lastIndexOf(" одинадцятого ");
        if (index == -1) index = input.lastIndexOf(" дванадцятого ");
        if (index == -1) index = input.lastIndexOf(" тринадцятого ");
        if (index == -1) index = input.lastIndexOf(" чотирнадцятого ");
        if (index == -1) index = input.lastIndexOf(" п'ятнадцятого ");
        if (index == -1) index = input.lastIndexOf(" шістнадцятого ");
        if (index == -1) index = input.lastIndexOf(" сімнадцятого ");
        if (index == -1) index = input.lastIndexOf(" вісімнадцятого ");
        if (index == -1) index = input.lastIndexOf(" дев'ятнадцятого ");
        if (index == -1) index = input.lastIndexOf(" двадцятого ");
        if (index == -1) index = input.lastIndexOf(" двадцять першого ");
        if (index == -1) index = input.lastIndexOf(" двадцять другого ");
        if (index == -1) index = input.lastIndexOf(" двадцять третього ");
        if (index == -1) index = input.lastIndexOf(" двадцять четвертого ");
        if (index == -1) index = input.lastIndexOf(" двадцять п'ятого ");
        if (index == -1) index = input.lastIndexOf(" двадцять шостого ");
        if (index == -1) index = input.lastIndexOf(" двадцять сьомого ");
        if (index == -1) index = input.lastIndexOf(" двадцять восьмого ");
        if (index == -1) index = input.lastIndexOf(" двадцять дев'ятого ");
        if (index == -1) index = input.lastIndexOf(" тридцятого ");
        if (index == -1) index = input.lastIndexOf(" тридцять першого ");

        if (index == -1) index = input.lastIndexOf(" первого ");
        if (index == -1) index = input.lastIndexOf(" второго ");
        if (index == -1) index = input.lastIndexOf(" третьего ");
        if (index == -1) index = input.lastIndexOf(" четвертого ");
        if (index == -1) index = input.lastIndexOf(" пятого ");
        if (index == -1) index = input.lastIndexOf(" шестого ");
        if (index == -1) index = input.lastIndexOf(" седьмого");
        if (index == -1) index = input.lastIndexOf(" восьмого ");
        if (index == -1) index = input.lastIndexOf(" девятого ");
        if (index == -1) index = input.lastIndexOf(" десятого ");
        if (index == -1) index = input.lastIndexOf(" одиннадцатого ");
        if (index == -1) index = input.lastIndexOf(" двенадцатого ");
        if (index == -1) index = input.lastIndexOf(" тринадцатого ");
        if (index == -1) index = input.lastIndexOf(" четырнадцатого ");
        if (index == -1) index = input.lastIndexOf(" пятнадцатого ");
        if (index == -1) index = input.lastIndexOf(" шестнадцатого ");
        if (index == -1) index = input.lastIndexOf(" семнадцатого ");
        if (index == -1) index = input.lastIndexOf(" восемнадцатого ");
        if (index == -1) index = input.lastIndexOf(" девятнадцатого ");
        if (index == -1) index = input.lastIndexOf(" двадцатого ");
        if (index == -1) index = input.lastIndexOf(" двадцать первого ");
        if (index == -1) index = input.lastIndexOf(" двадцать второго ");
        if (index == -1) index = input.lastIndexOf(" двадцать третьего ");
        if (index == -1) index = input.lastIndexOf(" двадцать четвёртого ");
        if (index == -1) index = input.lastIndexOf(" двадцать пятого ");
        if (index == -1) index = input.lastIndexOf(" двадцать шестого ");
        if (index == -1) index = input.lastIndexOf(" двадцать седьмого ");
        if (index == -1) index = input.lastIndexOf(" двадцать восьмого ");
        if (index == -1) index = input.lastIndexOf(" двадцать девятого ");
        if (index == -1) index = input.lastIndexOf(" тридцатог ");
        if (index == -1) index = input.lastIndexOf(" тридцать первого ");
        return index;
    }

    public static int getIndexForMonth(String input){
        int res;
        input = input.toLowerCase();
        res = input.lastIndexOf(" january");
        if (res == -1) res = input.lastIndexOf(" february");
        if (res == -1) res = input.lastIndexOf(" march");
        if (res == -1) res = input.lastIndexOf(" april");
        if (res == -1) res = input.lastIndexOf(" may");
        if (res == -1) res = input.lastIndexOf(" june");
        if (res == -1) res = input.lastIndexOf(" july");
        if (res == -1) res = input.lastIndexOf(" august");
        if (res == -1) res = input.lastIndexOf(" september");
        if (res == -1) res = input.lastIndexOf(" october");
        if (res == -1) res = input.lastIndexOf(" november");
        if (res == -1) res = input.lastIndexOf(" december");
        return res;
    }

    public static int getDayFromString(String input) {
        int res = 0;
        input = input.toLowerCase();
        if (input.contains("twenty first") || input.contains("двадцять першого") ||
                input.contains("двадцать первого")) res = 21;
        if (input.contains("twenty second") || input.contains("двадцять другого") ||
                input.contains("двадцать второго")) res = 22;
        if (input.contains("twenty third") || input.contains("двадцять третього") ||
                input.contains("двадцать третьего")) res = 23;
        if (input.contains("twenty fourth") || input.contains("двадцять четвертого") ||
                input.contains("двадцать четвёртого")) res = 24;
        if (input.contains("twenty fifth") || input.contains("двадцять п'ятого") ||
                input.contains("двадцать пятого")) res = 25;
        if (input.contains("twenty-sixth") || input.contains("двадцять шостого") ||
                input.contains("двадцать шестого")) res = 26;
        if (input.contains("twenty seventh") || input.contains("двадцять сьомого") ||
                input.contains("двадцать седьмого")) res = 27;
        if (input.contains("twenty eighth") || input.contains("двадцять восьмого") ||
                input.contains("двадцать восьмого")) res = 28;
        if (input.contains("twenty ninth") || input.contains("двадцять дев'ятого") ||
                input.contains("двадцать девятого")) res = 29;
        if (input.contains("thirtieth") || input.contains("тридцятого") ||
                input.contains("тридцатого")) res = 30;
        if (input.contains("thirty first") || input.contains("тридцять першого") ||
                input.contains("тридцать первого")) res = 31;
        if (input.contains("first") || input.contains("першого") ||
                input.contains("первого")) res = 1;
        if (input.contains("second") || input.contains("другого") ||
                input.contains("второго")) res = 2;
        if (input.contains("third") || input.contains("третього") ||
                input.contains("третьего")) res = 3;
        if (input.contains("fourth") || input.contains("четвертого") ||
                input.contains("четвертого")) res = 4;
        if (input.contains("fifth") || input.contains("п'ятого") ||
                input.contains("пятого")) res = 5;
        if (input.contains("sixth") || input.contains("шостого") ||
                input.contains("шестого")) res = 6;
        if (input.contains("seventh") || input.contains("сьомого") ||
                input.contains("седьмого")) res = 7;
        if (input.contains("eighth") || input.contains("восьмого") ||
                input.contains("восьмого")) res = 8;
        if (input.contains("ninth") || input.contains("дев'ятого") ||
                input.contains("девятого")) res = 9;
        if (input.contains("tenth") || input.contains("десятого") ||
                input.contains("десятого")) res = 10;
        if (input.contains("eleventh") || input.contains("одинадцятого") ||
                input.contains("одиннадцатого")) res = 11;
        if (input.contains("twelfth") || input.contains("дванадцятого") ||
                input.contains("двенадцатого")) res = 12;
        if (input.contains("thirteenth") || input.contains("тринадцятого") ||
                input.contains("тринадцатого")) res = 13;
        if (input.contains("fourteenth") || input.contains("чотирнадцятого") ||
                input.contains("четырнадцатого")) res = 14;
        if (input.contains("fifteenth") || input.contains("п'ятнадцятого") ||
                input.contains("пятнадцатого")) res = 15;
        if (input.contains("sixteenth") || input.contains("шістнадцятого") ||
                input.contains("шестнадцатого")) res = 16;
        if (input.contains("seventeenth") || input.contains("сімнадцятого") ||
                input.contains("семнадцатого")) res = 17;
        if (input.contains("eighteenth") || input.contains("вісімнадцятого") ||
                input.contains("восемнадцатого")) res = 18;
        if (input.contains("nineteenth") || input.contains("дев'ятнадцятого") ||
                input.contains("девятнадцатого")) res = 19;
        if (input.contains("twentieth") || input.contains("двадцятого") ||
                input.contains("двадцатого")) res = 20;
        return res;
    }

    public static int[] getMonthIndexes(String input){
        int month = 0;
        int indexStart = input.indexOf(" january");
        int increment = 8;
        if (indexStart == -1){
            month = 0;
            indexStart = input.indexOf(" січень");
            increment = 7;
        }
        if (indexStart == -1){
            month = 0;
            indexStart = input.indexOf(" січня");
            increment = 6;
        }
        if (indexStart == -1){
            month = 0;
            indexStart = input.indexOf(" январь");
            increment = 7;
        }
        if (indexStart == -1){
            month = 0;
            indexStart = input.indexOf(" января");
            increment = 7;
        }
        if (indexStart == -1){
            month = 1;
            indexStart = input.indexOf(" february");
            increment = 9;
        }
        if (indexStart == -1){
            month = 1;
            indexStart = input.indexOf(" лютий");
            increment = 6;
        }
        if (indexStart == -1){
            month = 1;
            indexStart = input.indexOf(" лютого");
            increment = 7;
        }
        if (indexStart == -1){
            month = 1;
            indexStart = input.indexOf(" февраль");
            increment = 8;
        }
        if (indexStart == -1){
            month = 1;
            indexStart = input.indexOf(" февраля");
            increment = 8;
        }
        if (indexStart == -1){
            month = 2;
            indexStart = input.indexOf(" march");
            increment = 6;
        }
        if (indexStart == -1){
            month = 2;
            indexStart = input.indexOf(" березень");
            increment = 9;
        }
        if (indexStart == -1){
            month = 2;
            indexStart = input.indexOf(" березня");
            increment = 8;
        }
        if (indexStart == -1){
            month = 2;
            indexStart = input.indexOf(" марта");
            increment = 6;
        }
        if (indexStart == -1){
            month = 2;
            indexStart = input.indexOf(" март");
            increment = 5;
        }
        if (indexStart == -1){
            month = 3;
            indexStart = input.indexOf(" april");
            increment = 6;
        }
        if (indexStart == -1){
            month = 3;
            indexStart = input.indexOf(" квітень");
            increment = 8;
        }
        if (indexStart == -1){
            month = 3;
            indexStart = input.indexOf(" квітня");
            increment = 7;
        }
        if (indexStart == -1){
            month = 3;
            indexStart = input.indexOf(" апрель");
            increment = 7;
        }
        if (indexStart == -1){
            month = 3;
            indexStart = input.indexOf(" апреля");
            increment = 7;
        }
        if (indexStart == -1){
            month = 4;
            indexStart = input.indexOf(" may");
            increment = 4;
        }
        if (indexStart == -1){
            month = 4;
            indexStart = input.indexOf(" травень");
            increment = 8;
        }
        if (indexStart == -1){
            month = 4;
            indexStart = input.indexOf(" травня");
            increment = 7;
        }
        if (indexStart == -1){
            month = 4;
            indexStart = input.indexOf(" май");
            increment = 4;
        }
        if (indexStart == -1){
            month = 4;
            indexStart = input.indexOf(" мая");
            increment = 4;
        }
        if (indexStart == -1){
            month = 5;
            indexStart = input.indexOf(" june");
            increment = 5;
        }
        if (indexStart == -1){
            month = 5;
            indexStart = input.indexOf(" червень");
            increment = 8;
        }
        if (indexStart == -1){
            month = 5;
            indexStart = input.indexOf(" червня");
            increment = 7;
        }
        if (indexStart == -1){
            month = 5;
            indexStart = input.indexOf(" июнь");
            increment = 5;
        }
        if (indexStart == -1){
            month = 5;
            indexStart = input.indexOf(" июня");
            increment = 5;
        }
        if (indexStart == -1){
            month = 6;
            indexStart = input.indexOf(" july");
            increment = 5;
        }
        if (indexStart == -1){
            month = 6;
            indexStart = input.indexOf(" липень");
            increment = 7;
        }
        if (indexStart == -1){
            month = 6;
            indexStart = input.indexOf(" липня");
            increment = 6;
        }
        if (indexStart == -1){
            month = 6;
            indexStart = input.indexOf(" июль");
            increment = 5;
        }
        if (indexStart == -1){
            month = 6;
            indexStart = input.indexOf(" июля");
            increment = 5;
        }
        if (indexStart == -1){
            month = 7;
            indexStart = input.indexOf(" august");
            increment = 7;
        }
        if (indexStart == -1){
            month = 7;
            indexStart = input.indexOf(" серпень");
            increment = 8;
        }
        if (indexStart == -1){
            month = 7;
            indexStart = input.indexOf(" серпня");
            increment = 7;
        }
        if (indexStart == -1){
            month = 7;
            indexStart = input.indexOf(" августа");
            increment = 8;
        }
        if (indexStart == -1){
            month = 7;
            indexStart = input.indexOf(" август");
            increment = 7;
        }
        if (indexStart == -1){
            month = 8;
            indexStart = input.indexOf(" september");
            increment = 10;
        }
        if (indexStart == -1){
            month = 8;
            indexStart = input.indexOf(" вересень");
            increment = 9;
        }
        if (indexStart == -1){
            month = 8;
            indexStart = input.indexOf(" вересня");
            increment = 8;
        }
        if (indexStart == -1){
            month = 8;
            indexStart = input.indexOf(" сентябрь");
            increment = 9;
        }
        if (indexStart == -1){
            month = 8;
            indexStart = input.indexOf(" сентября");
            increment = 9;
        }
        if (indexStart == -1){
            month = 9;
            indexStart = input.indexOf(" october");
            increment = 8;
        }
        if (indexStart == -1){
            month = 9;
            indexStart = input.indexOf(" жовтень");
            increment = 8;
        }
        if (indexStart == -1){
            month = 9;
            indexStart = input.indexOf(" жовтня");
            increment = 7;
        }
        if (indexStart == -1){
            month = 9;
            indexStart = input.indexOf(" октябрь");
            increment = 8;
        }
        if (indexStart == -1){
            month = 9;
            indexStart = input.indexOf(" октября");
            increment = 8;
        }
        if (indexStart == -1){
            month = 10;
            indexStart = input.indexOf(" november");
            increment = 9;
        }
        if (indexStart == -1){
            month = 10;
            indexStart = input.indexOf(" листопада");
            increment = 10;
        }
        if (indexStart == -1){
            month = 10;
            indexStart = input.indexOf(" листопад");
            increment = 9;
        }
        if (indexStart == -1){
            month = 10;
            indexStart = input.indexOf(" ноябрь");
            increment = 7;
        }
        if (indexStart == -1){
            month = 10;
            indexStart = input.indexOf(" ноября");
            increment = 7;
        }
        if (indexStart == -1){
            month = 11;
            indexStart = input.indexOf(" december");
            increment = 9;
        }
        if (indexStart == -1){
            month = 11;
            indexStart = input.indexOf(" грудень");
            increment = 8;
        }
        if (indexStart == -1){
            month = 11;
            indexStart = input.indexOf(" грудня");
            increment = 7;
        }
        if (indexStart == -1){
            month = 11;
            indexStart = input.indexOf(" декабрь");
            increment = 8;
        }
        if (indexStart == -1){
            month = 11;
            indexStart = input.indexOf(" декабря");
            increment = 8;
        }
        return new int[] {indexStart, increment, month};
    }

    public static int getMonthFromString(String input){
        int res = 0;
        input = input.toLowerCase();
        if (input.contains("january") || input.contains("січень") || input.contains("січня") ||
                input.contains("январь") || input.contains("января")) res = 0;
        if (input.contains("february") || input.contains("лютий") || input.contains("лютого") ||
                input.contains("февраль") || input.contains("февраля")) res = 1;
        if (input.contains("march") || input.contains("березень") || input.contains("березня") ||
                input.contains("март") || input.contains("марта")) res = 2;
        if (input.contains("april") || input.contains("квітень") || input.contains("квітня") ||
                input.contains("апрель") || input.contains("апреля")) res = 3;
        if (input.contains("may") || input.contains("травень") || input.contains("травня") ||
                input.contains("май") || input.contains("мая")) res = 4;
        if (input.contains("june") || input.contains("червень") || input.contains("червня") ||
                input.contains("июнь") || input.contains("июня")) res = 5;
        if (input.contains("july") || input.contains("липень") || input.contains("липня") ||
                input.contains("июль") || input.contains("июля")) res = 6;
        if (input.contains("august") || input.contains("серпень") || input.contains("серпня") ||
                input.contains("август") || input.contains("августа")) res = 7;
        if (input.contains("september") || input.contains("вересень") || input.contains("вересня") ||
                input.contains("сентябрь") || input.contains("сентября")) res = 8;
        if (input.contains("october") || input.contains("жовтень") || input.contains("жовтня") ||
                input.contains("октябрь") || input.contains("октября")) res = 9;
        if (input.contains("november") || input.contains("листопад") || input.contains("листопада") ||
                input.contains("ноябрь") || input.contains("ноября")) res = 10;
        if (input.contains("december") || input.contains("грудень") || input.contains("грудня") ||
                input.contains("декабрь") || input.contains("декабря")) res = 11;
        return res;
    }

    public static int getWeekDay(String input) {
        int i = 0;
        if (input.matches(".*monday.*") ||
                input.matches(".*понеділок.*") || input.matches(".*понеділка.*") ||
                input.matches(".*пенедельник.*")){
            i = 2;
        } else if (input.matches(".*tuesday.*") ||
                input.matches(".*вівторок.*") || input.matches(".*вівторка.*") ||
                input.matches(".*вторник.*")){
            i = 3;
        } else if (input.matches(".*wednesday.*") ||
                input.matches(".*середу.*") ||
                input.matches(".*среда.*") || input.matches(".*среду.*")){
            i = 4;
        } else if (input.matches(".*thursday.*") ||
                input.matches(".*четвер.*") ||
                input.matches(".*четверг.*") || input.matches(".*четверга.*")){
            i = 5;
        } else if (input.matches(".*friday.*") ||
                input.matches(".*п'ятницю.*") ||
                input.matches(".*пятницу.*")){
            i = 6;
        } else if (input.matches(".*saturday.*") ||
                input.matches(".*суботу.*") ||
                input.matches(".*субботу.*")){
            i = 7;
        } else if (input.matches(".*sunday.*") ||
                input.matches(".*неділю.*") ||
                input.matches(".*воскресенье.*")){
            i = 1;
        }
        return i;
    }

    public static boolean isCorrectTime(int hourOfDay, int minuteOfHour) {
        return hourOfDay < 24 && minuteOfHour < 60;
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0) && year % 100 != 0 ||
                (year % 4 == 0) && (year % 100 == 0) && (year % 400 == 0);
    }

    public static String getDays(String input, String locale) {
        StringBuilder sb = new StringBuilder();
        if (locale.matches("en")){
            Pattern pattern = Pattern.compile("(.*monday.*)");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*monday.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*tuesday.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*tuesday.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*wednesday.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*wednesday.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*thursday.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*thursday.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*friday.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*friday.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*saturday.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*saturday.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*sunday.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*sunday.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);
        } else if (locale.matches("ru")){
            Pattern pattern = Pattern.compile("(.*понедельник.*)");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*понедельник.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*вторник.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*вторник.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*среда.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*среда.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*четверг.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*четверг.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*пятница.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*пятница.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*суббота.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*суббота.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*воскресенье.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*воскресенье.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);
        } else if (locale.matches("uk")){
            Pattern pattern = Pattern.compile("(.*понеділок.*)");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*понеділок.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*вівторок.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*вівторок.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*середа.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*середа.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*четвер.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*четвер.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*п'ятниця.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*п'ятниця.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*субота.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*субота.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);

            pattern = Pattern.compile("(.*неділя.*)");
            matcher = pattern.matcher(input);
            if (matcher.find()) {
                if (matcher.group().trim().matches(".*неділя.*")) {
                    sb.append(Constants.DAY_CHECKED);
                } else sb.append(Constants.DAY_UNCHECKED);
            } else sb.append(Constants.DAY_UNCHECKED);
        }
        return sb.toString();
    }
}
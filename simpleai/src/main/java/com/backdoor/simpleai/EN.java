package com.backdoor.simpleai;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class EN extends RecUtils {

    private final static String[] weekDays = {
            "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"
    };

    public static boolean hasCalendar(String input) {
        return input.matches(".*calendar.*");
    }

    public static String clearCalendar(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*calendar.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public static ArrayList<Integer> getWeekDays(String input) {
        int[] array = {0, 0, 0, 0, 0, 0, 0};

        String[] parts = input.split("\\s");
        for (String part : parts) {
            for (int i = 0; i < weekDays.length; i++) {
                String day = weekDays[i];
                if (part.matches(".*" + day + ".*"))
                    array[i] = 1;
            }
        }
        ArrayList<Integer> list = new ArrayList<>();
        for (int anArray : array) list.add(anArray);
        return list;
    }

    public static String clearWeekDays(String input) {
        String[] parts = input.split("\\s");
        for (String part : parts) {
            for (String day : weekDays) {
                if (part.matches(".*" + day + ".*"))
                    input = input.replace(part, "");
            }
        }
        parts = input.split("\\s");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.matches("on") && !part.matches("in") && !part.matches("at"))
                sb.append(" ").append(part);
        }
        return sb.toString().trim();
    }

    public static long getDaysRepeat(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasDays(part)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    integer = 1;
                }
                return integer * DAY;
            }
        }
        return 0;
    }

    public static String clearDaysRepeat(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasDays(part)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (NumberFormatException e) {
                }
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
    }

    public static boolean hasRepeat(String input) {
        return input.matches(".*every.*");
    }

    public static String clearRepeat(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*every.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public static boolean hasTomorrow(String input) {
        return input.matches(".*tomorrow.*");
    }

    public static String clearTomorrow(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (string.matches(".*tomorrow.*")) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public static String getMessage(String input) {
        String[] parts = input.split("\\s");
        StringBuilder sb = new StringBuilder();
        boolean isStart = false;
        for (String part : parts) {
            if (isStart) sb.append(" ").append(part);
            if (part.matches("text"))
                isStart = true;
        }
        return sb.toString().trim();
    }

    public static String clearMessage(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("text")) {
                int index = input.indexOf(part);
                input = input.replace(parts[i - 1], "");
                input = input.substring(0, index - 1);
            }
        }
        return input.trim();
    }

    public static int getType(String input) {
        if (input.matches(".*message.*")) return MESSAGE;
        if (input.matches(".*letter.*")) return MAIL;
        return -1;
    }

    public static String clearType(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int type = getType(part);
            if (type != -1) {
                input = input.replace(part, "");
                if (parts[i + 1].matches("to")) input = input.replace(parts[i + 1], "");
                break;
            }
        }
        return input.trim();
    }

    public static int getAmpm(String input) {
        if (input.matches(".*morning.*")) return MORNING;
        if (input.matches(".*evening.*")) return EVENING;
        if (input.matches(".*noon.*")) return NOON;
        if (input.matches(".*night.*")) return NIGHT;
        if (input.matches(".*a m.*")) return MORNING;
        if (input.matches(".*a.m..*")) return MORNING;
        if (input.matches(".*am.*")) return MORNING;
        if (input.matches(".*p m.*")) return EVENING;
        if (input.matches(".*p.m..*")) return EVENING;
        if (input.matches(".*pm.*")) return EVENING;
        return -1;
    }

    public static String clearAmpm(String input) {
        String[] parts = input.split("\\s");
        for (String part : parts) {
            int ampm = getAmpm(part);
            if (ampm != -1) {
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
    }

    public static long getTime(String input, int ampm, String[] times) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasHours(part)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    integer = 1;
                }
                if (ampm == EVENING) integer += 12;

                calendar.set(Calendar.HOUR_OF_DAY, integer);
            }
            if (hasMinutes(part)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    integer = 1;
                }

                calendar.set(Calendar.MINUTE, integer);
            }
        }
        Date date = getShortTime(input);
        if (date != null) {
            calendar.setTime(date);
            if (ampm == EVENING) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(Calendar.HOUR_OF_DAY, hour < 12 ? hour + 12 : hour);
            }
            return calendar.getTimeInMillis();
        }
        if (calendar.getTimeInMillis() == 0 && ampm != -1) {
            try {
                if (ampm == MORNING)
                    calendar.setTime(mFormat.parse(times[0]));
                if (ampm == NOON)
                    calendar.setTime(mFormat.parse(times[1]));
                if (ampm == EVENING)
                    calendar.setTime(mFormat.parse(times[2]));
                if (ampm == NIGHT)
                    calendar.setTime(mFormat.parse(times[3]));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return calendar.getTimeInMillis();
    }

    static Date getShortTime(String input) {
        Pattern pattern = Pattern.compile("([01]?\\d|2[0-3])( |:)?(([0-5]?\\d?)?)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String time = matcher.group().trim();
            for (SimpleDateFormat format : RecUtils.dateTaskFormats){
                Date date;
                try {
                    date = format.parse(time);
                    if (date != null) return date;
                } catch (NullPointerException | ParseException e){
                }
            }
        }
        return null;
    }

    public static String clearTime(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (hasHours(part)) {
                input = input.replace(part, "");
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (NumberFormatException e) {
                }
            }
            if (hasMinutes(part)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (NumberFormatException e) {
                }
                input = input.replace(part, "");
            }
        }
        Pattern pattern = Pattern.compile("([01]?\\d|2[0-3])( |:)(([0-5]?\\d?)?)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String time = matcher.group().trim();
            input = input.replace(time, "");
        }
        parts = input.split("\\s");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.matches("at")) sb.append(" ").append(part);
        }
        return sb.toString().trim();
    }

    public static long getDate(String input) {
        long mills = 0;
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int month = getMonth(part);
            if (month != -1) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException e) {
                    integer = 1;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, integer);
                mills = calendar.getTimeInMillis();
                break;
            }
        }
        return mills;
    }

    public static String clearDate(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int month = getMonth(part);
            if (month != -1) {
                try {
                    Integer.parseInt(parts[i + 1]);
                    input = input.replace(parts[i + 1], "");
                } catch (NumberFormatException e) {
                }
                input = input.replace(part, "");
                break;
            }
        }
        return input.trim();
    }

    static int getMonth(String input){
        int res = -1;
        if (input.contains("january")) res = 0;
        if (input.contains("february")) res = 1;
        if (input.contains("march")) res = 2;
        if (input.contains("april")) res = 3;
        if (input.contains("may")) res = 4;
        if (input.contains("june")) res = 5;
        if (input.contains("july")) res = 6;
        if (input.contains("august")) res = 7;
        if (input.contains("september")) res = 8;
        if (input.contains("october")) res = 9;
        if (input.contains("november")) res = 10;
        if (input.contains("december")) res = 11;
        return res;
    }

    public static boolean hasCall(String input) {
        return input.matches(".*call.*");
    }

    public static String clearCall(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (hasCall(string)) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public static boolean isTimer(String input) {
        return input.matches(".*after.*");
    }

    public static String cleanTimer(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (isTimer(string)) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public static boolean hasSender(String input) {
        return input.matches(".*send.*");
    }

    public static String clearSender(String input) {
        String[] parts = input.split("\\s");
        for (String string : parts) {
            if (hasSender(string)) {
                input = input.replace(string, "");
                break;
            }
        }
        return input.trim();
    }

    public static boolean hasNote(String input) {
        return input.startsWith("note");
    }

    public static String clearNote(String input) {
        input = input.replace("note", "");
        return input.trim();
    }

    public static boolean hasAction(String input) {
        return input.startsWith("open") || input.matches(".*help.*")
                || input.matches(".*adjust.*") || input.matches(".*report.*") ||
                input.matches(".*change.*");
    }

    public static int getAction(String input) {
        if (input.matches(".*help.*"))
            return HELP;
        else if (input.matches(".*loudness.*") || input.matches(".*volume.*"))
            return VOLUME;
        else if (input.matches(".*settings.*"))
            return SETTINGS;
        else if (input.matches(".*report.*"))
            return REPORT;
        else return APP;
    }

    public static boolean hasEvent(String input) {
        return input.startsWith("new") || input.startsWith("add");
    }

    public static int getEvent(String input) {
        if (input.matches(".*birthday.*"))
            return BIRTHDAY;
        else return REMINDER;
    }

    public static long getMultiplier(String input) {
        long result = 0;
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (hasSeconds(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * SECOND;
            } else if (hasMinutes(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * MINUTE;
            } else if (hasHours(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * HOUR;
            } else if (hasDays(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * DAY;
            } else if (hasWeeks(string)) {
                int integer;
                try {
                    integer = Integer.parseInt(parts[i - 1]);
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    integer = 1;
                }
                result = result + integer * DAY * 7;
            }
        }
        return result;
    }

    public static String clearMultiplier(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            String string = parts[i];
            if (hasSeconds(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasMinutes(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasHours(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasDays(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            } else if (hasWeeks(string)) {
                try {
                    Integer.parseInt(parts[i - 1]);
                    input = input.replace(parts[i - 1], "");
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                }
                input = input.replace(string, "");
            }
        }
        return input.trim();
    }

    static boolean hasHours(String input) {
        return input.matches(".*hour.*") || input.matches(".*o'clock.*")
                || input.matches(".*am.*")  || input.matches(".*pm.*");
    }

    static boolean hasMinutes(String input) {
        return input.matches(".*minute.*");
    }

    static boolean hasSeconds(String input) {
        return input.matches(".*second.*");
    }

    static boolean hasDays(String input) {
        return input.matches(".* day.*");
    }

    static boolean hasWeeks(String input) {
        return input.matches(".*week.*");
    }

    public static String replaceNumbers(String input) {
        String[] parts = input.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            int number = getNumber(parts, i);
            if (number != -1) {
                if (number > 20 && (number % 10 > 0)) {
                    input = input.replace(parts[i] + " " + parts[i + 1], String.valueOf(number));
                } else input = input.replace(parts[i], String.valueOf(number));
            }
        }
        return input.trim();
    }

    static int getNumber(String[] parts, int index) {
        int number = findNumber(parts[index]);
        if (number == -1) return -1;
        if (number >= 20) {
            int res = getNumber(parts, index + 1);
            if (res != -1) return res + number;
            else return number;
        } else return number;
    }

    static int findNumber(String input){
        int number = -1;
        if (input.matches("zero") || input.matches("nil")) number = 0;
        if (input.matches("one") || input.matches("first")) number = 1;
        if (input.matches("two") || input.matches("second")) number = 2;
        if (input.matches("three") || input.matches("third")) number = 3;
        if (input.matches("four") || input.matches("fourth")) number = 4;
        if (input.matches("five") || input.matches("fifth")) number = 5;
        if (input.matches("six") || input.matches("sixth")) number = 6;
        if (input.matches("seven") || input.matches("seventh")) number = 7;
        if (input.matches("eight") || input.matches("eighth")) number = 8;
        if (input.matches("nine") || input.matches("ninth")) number = 9;
        if (input.matches("ten") || input.matches("tenth")) number = 10;
        if (input.matches("eleven") || input.matches("eleventh")) number = 11;
        if (input.matches("twelve") || input.matches("twelfth")) number = 12;
        if (input.matches("thirteen") || input.matches("thirteenth")) number = 13;
        if (input.matches("fourteen") || input.matches("fourteenth")) number = 14;
        if (input.matches("fifteen") || input.matches("fifteenth")) number = 15;
        if (input.matches("sixteen") || input.matches("sixteenth")) number = 16;
        if (input.matches("seventeen") || input.matches("seventeenth")) number = 17;
        if (input.matches("eighteen") || input.matches("eighteenth")) number = 18;
        if (input.matches("nineteen") || input.matches("nineteenth")) number = 19;
        if (input.matches("twenty") || input.matches("twentieth")) number = 20;
        if (input.matches("thirty") || input.matches("thirtieth")) number = 30;
        if (input.matches("forty") || input.matches("fortieth")) number = 40;
        if (input.matches("fifty") || input.matches("fiftieth")) number = 50;
        if (input.matches("sixty") || input.matches("sixtieth")) number = 60;
        if (input.matches("seventy") || input.matches("seventieth")) number = 70;
        if (input.matches("eighty") || input.matches("eightieth")) number = 80;
        if (input.matches("ninety") || input.matches("ninetieth")) number = 90;
        return number;
    }
}

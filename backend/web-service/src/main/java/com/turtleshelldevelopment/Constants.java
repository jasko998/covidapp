package com.turtleshelldevelopment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static List<String> CLIENT_SEX_IDENTITIES = List.of("Female", "Male", "Non-Binary");

    public static Map<String, Integer> VACCINE_SERIES = new HashMap<>();

    static {
        VACCINE_SERIES.put("adDose1", 1);
        VACCINE_SERIES.put("adDose2", 2);
        VACCINE_SERIES.put("adBooster1", 3);
        VACCINE_SERIES.put("adBooster2", 4);
        VACCINE_SERIES.put("adBoosterBiv", 5);
        VACCINE_SERIES.put("teenDose1", 6);
        VACCINE_SERIES.put("teenDose2", 7);
        VACCINE_SERIES.put("teenBooster1", 8);
        VACCINE_SERIES.put("teenBoosterBiv", 9);
        VACCINE_SERIES.put("chDose1", 10);
        VACCINE_SERIES.put("chDose2", 11);
        VACCINE_SERIES.put("chBooster1", 12);
        VACCINE_SERIES.put("chBoosterBiv", 13);
        VACCINE_SERIES.put("infDose1", 14);
        VACCINE_SERIES.put("infDose2", 15);
        VACCINE_SERIES.put("infBooster1", 16);
    }
}

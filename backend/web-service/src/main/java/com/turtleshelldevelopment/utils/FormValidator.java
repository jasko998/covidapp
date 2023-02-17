package com.turtleshelldevelopment.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class FormValidator {
    private static final Pattern phoneNumberPattern = Pattern.compile("^\\([0-9]{3}\\) [0-9]{3} - [0-9]{4}$");


    public static boolean checkValues(String... params) {
        for (String param: params) {
            if(param == null || param.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static LocalDate parseDateFromForm(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
            return LocalDate.parse(date, formatter);
        } catch(DateTimeParseException e) {
            return null;
        }
    }

    public static String parsePhoneNumberFromForm(String phoneNumber) {
        if(phoneNumberPattern.matcher(phoneNumber).find()) {
            String phoneNumberFormatted = phoneNumber.replaceAll("[ \\-()]", "");
            System.out.println("Phone number is now: " + phoneNumberFormatted);
            return phoneNumberFormatted;
        } else {
            return null;
        }
    }

    public static boolean isValidUsername(String username) {
        if(username == null) return false;
        int nameLen = username.length();
        for(int i = 0; i < nameLen; i++) {
            if((!Character.isLetterOrDigit(username.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public RequiredFormDataResponse checkRequired(String requiredField, Pattern validationPattern, String validationMessage) {
        if(requiredField != null) {
            if(requiredField.isEmpty()) return new RequiredFormDataResponse(false, "%s is a required field");
            if(validationPattern != null) {
                boolean validated = validationPattern.matcher(requiredField).find();
                return new RequiredFormDataResponse(validated, validated ? "" : "Invalid %s: %s");
            } else {
                return new RequiredFormDataResponse(true, "");
            }
        }
        return new RequiredFormDataResponse(false, "Invalid %s");
    }



}

record RequiredFormDataResponse (boolean valid, String error) {

}

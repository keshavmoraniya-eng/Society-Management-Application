package com.society.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class PhoneMaskUtil {

    //Mask phone no showing only first 3 & last 2 digits

    public static String mask(String phoneNo){
        if (phoneNo == null || phoneNo.isEmpty()){
            return "***";
        }

        //Remove + if Present
        String clean = phoneNo.replace("+", "").replaceAll("\\s", "");

        if (clean.length() < 5){
            return "***";
        }

        //Get country code
        String countryCode = "";
        String digits = clean;

        //Detect common country codes
        if (clean.length() > 10){
            if (clean.startsWith("91") || clean.startsWith("1") || clean.startsWith("44") || clean.startsWith("86") || clean.startsWith("971")){
                countryCode = clean.substring(0, 2);
                digits = clean.substring(2);
            }
        }

        if (digits.length() < 4){
            return "+" + countryCode + "***";
        }

        String firstPart = digits.substring(0,2);
        String lastPart = digits.substring(digits.length() -2);

        return "+" + countryCode + firstPart + "***" + lastPart;
    }

    //Mask for display

    public static String maskForDisplay(String phoneNo){
        if (phoneNo == null || phoneNo.length() < 5){
            return "***";
        }

        return phoneNo.substring(0,2) + "***" + phoneNo.substring(phoneNo.length() -2);
    }
}

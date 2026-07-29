package com.society.service;

import com.society.config.TwilioProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PhoneNumberFormatter {
    private final TwilioProperties twilioProperties;

    private static final String E164_PATTERN = "^\\+[1-9][0-9]{6,14}$";
    private static final int MAX_E164_LENGTH = 15;
    private static final int MIN_E164_LENGTH = 8;

    public String toE164(String phoneNo) {
        if (phoneNo == null || phoneNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // Remove all non-digit characters except leading +
        String cleaned = phoneNo.trim().replaceAll("[\\s\\-().]", "");

        // Remove leading + if present, we'll add it back
        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }

        // Remove leading 0 (common in some countries)
        if (cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1);
        }

        // Remove leading 00 (international prefix)
        if (cleaned.startsWith("00")) {
            cleaned = cleaned.substring(2);
        }

        // Validate - must be all digits now
        if (!cleaned.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("Phone number contains invalid characters");
        }

        // Check minimum length
        if (cleaned.length() < MIN_E164_LENGTH) {
            throw new IllegalArgumentException("Phone number too short");
        }

        // If doesn't start with country code, add default
        if (!hasCountryCode(cleaned)) {
            String defaultCode = twilioProperties.getDefaultCountryCode();
            if (defaultCode != null && !defaultCode.isEmpty()) {
                cleaned = defaultCode + cleaned;
            } else {
                throw new IllegalArgumentException("Cannot determine country code");
            }
        }

        // Add + prefix
        String e164 = "+" + cleaned;

        // Final validation
        if (!e164.matches(E164_PATTERN)) {
            throw new IllegalArgumentException("Phone number cannot be formatted to E.164");
        }

        if (e164.length() > MAX_E164_LENGTH + 1) { // +1 for the + sign
            throw new IllegalArgumentException("Phone number too long");
        }

        log.debug("Formatted phone: {} → {}", phoneNo, e164);
        return e164;
    }

    public boolean isValid(String phoneNo) {
        if (phoneNo == null || phoneNo.trim().isEmpty()) {
            return false;
        }
        try {
            String e164 = toE164(phoneNo);
            return e164.matches(E164_PATTERN);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean hasCountryCode(String digits) {
        // Known country codes (add more as needed)
        String[] countryCodes = {"1", "7", "20", "27", "30", "31", "32", "33", "34", "36",
                "39", "40", "41", "43", "44", "45", "46", "47", "48", "49",
                "51", "52", "53", "54", "55", "56", "57", "58", "60", "61",
                "62", "63", "64", "65", "66", "81", "82", "84", "86", "90",
                "91", "92", "93", "94", "95", "98", "211", "212", "213", "216",
                "218", "220", "221", "222", "223", "224", "225", "226", "227", "228",
                "229", "230", "231", "232", "233", "234", "235", "236", "237", "238",
                "239", "240", "241", "242", "243", "244", "245", "246", "247", "248",
                "249", "250", "251", "252", "253", "254", "255", "256", "257", "258",
                "260", "261", "262", "263", "264", "265", "266", "267", "268", "269",
                "290", "291", "297", "298", "299", "350", "351", "352", "353", "354",
                "355", "356", "357", "358", "359", "370", "371", "372", "373", "374",
                "375", "376", "377", "378", "379", "380", "381", "382", "385", "386",
                "387", "389", "420", "421", "423", "500", "501", "502", "503", "504",
                "505", "506", "507", "508", "509", "590", "591", "592", "593", "594",
                "595", "596", "597", "598", "599", "670", "672", "673", "674", "675",
                "676", "677", "678", "679", "680", "681", "682", "683", "685", "686",
                "687", "688", "689", "690", "691", "692", "850", "852", "853", "855",
                "856", "880", "886", "960", "961", "962", "963", "964", "965", "966",
                "967", "968", "970", "971", "972", "973", "974", "975", "976", "977",
                "992", "993", "994", "995", "996", "998"};

        for (String code : countryCodes) {
            if (digits.startsWith(code) && digits.length() > code.length() + 5) {
                return true;
            }
        }
        return false;
    }

    public String digitsOnly(String phoneNo) {
        if (phoneNo == null) return "";
        return phoneNo.replaceAll("\\D", "");
    }

}

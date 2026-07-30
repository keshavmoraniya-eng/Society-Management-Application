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

    /**
     * Format phone number to E.164 format
     *
     * Handles:
     *   - 7987948810     → +917987948810 (India - default)
     *   - 919876543210   → +919876543210 (India - has prefix)
     *   - +919876543210  → +919876543210 (Already formatted)
     *   - +7987948810    → +917987948810 (Strip bad +7, add +91)
     *   - 6281234567890  → +6281234567890 (Indonesia)
     *   - 628123456      → +91628123456 (India default for 9 digits)
     */
    public String toE164(String phoneNo) {

        if (phoneNo != null && !phoneNo.trim().isEmpty()) {// Step 1: Trim
            String cleaned = phoneNo.trim();

            // Step 2: Remove all non-digit characters
            String digits = cleaned.replaceAll("[^0-9]", "");

            if (digits.isEmpty()) {
                throw new IllegalArgumentException("Phone number contains no digits");
            }

            // Step 3: Remove leading zeros (international format)
            while (digits.startsWith("0")) {
                digits = digits.substring(1);
            }

            // Step 4: Detect country code or use default
            String result = addCountryCode(digits);

            // Step 5: Add + prefix
            String e164 = "+" + result;

            // Step 6: Validate E.164 format
            if (!e164.matches(E164_PATTERN)) {
                throw new IllegalArgumentException(
                        "Invalid E.164 format: " + e164 + " (digits: " + digits + ")"
                );
            }

            log.debug("Formatted phone: {} → {}", phoneNo, e164);
            return e164;
        } else {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

    }

    /**
     * Add country code to phone digits
     * Returns digits WITH country code prepended
     */
    private String addCountryCode(String digits) {
        // If already starts with valid country code, return as-is
        String detectedCode = detectCountryCode(digits);
        if (detectedCode != null) {
            log.debug("Detected country code: {} in {}", detectedCode, digits);
            return digits;
        }

        // No country code detected - use default
        String defaultCode = twilioProperties.getDefaultCountryCode();
        if (defaultCode == null || defaultCode.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot detect country code. Please include country code (e.g., +91 for India)"
            );
        }

        log.debug("Using default country code: {} for {}", defaultCode, digits);
        return defaultCode + digits;
    }

    /**
     * Detect country code from phone digits
     * Returns the country code if found, null otherwise
     */
    private String detectCountryCode(String digits) {
        // 3-digit codes (longest first - most specific)
        String[] threeDigitCodes = {
                "880", // Bangladesh
                "886", // Taiwan
                "855", // Cambodia
                "856", // Laos
                "850", // North Korea
                "852", // Hong Kong
                "853", // Macau
                "670", // Northern Mariana Islands
                "672", // Australian External Territories
                "673", // Brunei
                "674", // Nauru
                "675", // Papua New Guinea
                "676", // Tonga
                "677", // Solomon Islands
                "678", // Vanuatu
                "679", // Fiji
                "680", // Palau
                "681", // Wallis and Futuna
                "682", // Cook Islands
                "683", // Niue
                "685", // Samoa
                "686", // Kiribati
                "687", // New Caledonia
                "688", // Tuvalu
                "689", // French Polynesia
                "690", // Tokelau
                "691", // Federated States of Micronesia
                "692", // Marshall Islands
                "970", // Palestine
                "971", // UAE
                "972", // Israel
                "973", // Bahrain
                "974", // Qatar
                "975", // Bhutan
                "976", // Mongolia
                "977", // Nepal
                "992", // Tajikistan
                "993", // Turkmenistan
                "994", // Azerbaijan
                "995", // Georgia
                "996", // Kyrgyzstan
                "998", // Uzbekistan
        };

        for (String code : threeDigitCodes) {
            if (digits.startsWith(code)) {
                // Verify total length is reasonable (country code + 6-12 digits)
                if (digits.length() >= code.length() + 6 && digits.length() <= code.length() + 12) {
                    return code;
                }
            }
        }

        // 2-digit codes
        String[] twoDigitCodes = {
                "20", // Egypt
                "27", // South Africa
                "30", // Greece
                "31", // Netherlands
                "32", // Belgium
                "33", // France
                "34", // Spain
                "36", // Hungary
                "39", // Italy
                "40", // Romania
                "41", // Switzerland
                "43", // Austria
                "44", // UK
                "45", // Denmark
                "46", // Sweden
                "47", // Norway
                "48", // Poland
                "49", // Germany
                "51", // Peru
                "52", // Mexico
                "53", // Cuba
                "54", // Argentina
                "55", // Brazil
                "56", // Chile
                "57", // Colombia
                "58", // Venezuela
                "60", // Malaysia
                "61", // Australia
                "62", // Indonesia
                "63", // Philippines
                "64", // New Zealand
                "65", // Singapore
                "66", // Thailand
                "81", // Japan
                "82", // South Korea
                "84", // Vietnam
                "86", // China
                "90", // Turkey
                "91", // India
                "92", // Pakistan
                "93", // Afghanistan
                "94", // Sri Lanka
                "95", // Myanmar
                "98", // Iran
        };

        for (String code : twoDigitCodes) {
            if (digits.startsWith(code)) {
                // India (91), Indonesia (62), etc. - check length
                if (digits.length() >= code.length() + 8 && digits.length() <= code.length() + 10) {
                    return code;
                }
            }
        }

        // 1-digit codes (USA/Canada = 1, Russia = 7)
        String[] oneDigitCodes = {"1", "7"};

        for (String code : oneDigitCodes) {
            if (digits.startsWith(code)) {
                // USA/Canada: 1 + 10 digits = 11 total
                // Russia: 7 + 10 digits = 11 total
                if (digits.length() == code.length() + 10) {
                    return code;
                }
            }
        }

        return null;
    }

    /**
     * Validate phone number
     */
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

    /**
     * Extract just digits
     */
    public String digitsOnly(String phoneNo) {
        if (phoneNo == null) return "";
        return phoneNo.replaceAll("\\D", "");
    }
}

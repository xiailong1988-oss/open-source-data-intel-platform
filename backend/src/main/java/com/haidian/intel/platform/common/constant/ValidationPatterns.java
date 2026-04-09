package com.haidian.intel.platform.common.constant;

/**
 * Shared regular expressions for request payload validation.
 */
public final class ValidationPatterns {

    /**
     * Month value in yyyy-MM format.
     */
    public static final String YEAR_MONTH = "^\\d{4}-(0[1-9]|1[0-2])$";

    /**
     * Optional month value in yyyy-MM format.
     */
    public static final String OPTIONAL_YEAR_MONTH = "(^$)|(^\\d{4}-(0[1-9]|1[0-2])$)";

    /**
     * Address with a scheme prefix, such as https://, jdbc:, mqtt:, or smb://.
     */
    public static final String SCHEME_BASED_ADDRESS = "^[a-zA-Z][a-zA-Z0-9+.-]*:(//)?.+$";

    /**
     * Optional address with a scheme prefix.
     */
    public static final String OPTIONAL_SCHEME_BASED_ADDRESS = "(^$)|(^[a-zA-Z][a-zA-Z0-9+.-]*:(//)?.+$)";

    /**
     * Mainland China mobile number, service hotline, or area-code landline.
     */
    public static final String PHONE_NUMBER = "^(1\\d{10}|0\\d{2,3}-?\\d{7,8}|\\+?[1-9]\\d{5,19})$";

    /**
     * Optional phone number.
     */
    public static final String OPTIONAL_PHONE_NUMBER = "(^$)|(^((1\\d{10})|(0\\d{2,3}-?\\d{7,8})|(\\+?[1-9]\\d{5,19}))$)";

    private ValidationPatterns() {
    }
}

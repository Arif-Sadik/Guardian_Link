package util;

import exception.InvalidContactException;
import java.util.regex.Pattern;

/**
 * Utility class for validating email addresses and phone numbers.
 */
public class ContactValidator {

    // Email regex pattern - RFC 5322 simplified
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Phone number pattern - supports various formats
    // Accepts: 1234567890, 123-456-7890, (123) 456-7890, +1 123 456 7890, etc.
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+\\d{1,3}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$|^\\d{10}$"
    );

    /**
     * Validates an email address format.
     *
     * @param email the email address to validate
     * @throws InvalidContactException if email format is invalid or empty
     */
    public static void validateEmail(String email) throws InvalidContactException {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidContactException("Email address cannot be empty.");
        }
        
        email = email.trim();
        
        if (email.length() > 254) {
            throw new InvalidContactException("Email address is too long (max 254 characters).");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidContactException("Email address format is invalid. Please enter a valid email (e.g., user@example.com).");
        }
    }

    /**
     * Validates a phone number format.
     *
     * @param phoneNumber the phone number to validate
     * @throws InvalidContactException if phone number format is invalid or empty
     */
    public static void validatePhoneNumber(String phoneNumber) throws InvalidContactException {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new InvalidContactException("Phone number cannot be empty.");
        }
        
        phoneNumber = phoneNumber.trim();
        
        // Remove all whitespace and special characters for length check
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        
        if (digitsOnly.length() < 10 || digitsOnly.length() > 15) {
            throw new InvalidContactException("Phone number must contain between 10 and 15 digits.");
        }
        
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new InvalidContactException("Phone number format is invalid. Please enter a valid phone number (e.g., 123-456-7890, (123) 456-7890, +1 123 456 7890).");
        }
    }

    /**
     * Combined validation for both email and phone number.
     *
     * @param email the email address to validate
     * @param phoneNumber the phone number to validate
     * @throws InvalidContactException if either email or phone format is invalid
     */
    public static void validateContact(String email, String phoneNumber) throws InvalidContactException {
        validateEmail(email);
        validatePhoneNumber(phoneNumber);
    }
}

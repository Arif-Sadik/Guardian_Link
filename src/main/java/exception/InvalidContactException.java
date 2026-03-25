package exception;

/**
 * Exception thrown when email or phone number format is invalid.
 */
public class InvalidContactException extends Exception {
    
    public InvalidContactException(String message) {
        super(message);
    }
    
    public InvalidContactException(String message, Throwable cause) {
        super(message, cause);
    }
}

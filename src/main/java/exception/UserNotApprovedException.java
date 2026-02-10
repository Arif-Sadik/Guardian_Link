package exception;

/**
 * Thrown when an unapproved user attempts to log in.
 */
public class UserNotApprovedException extends Exception {

    public UserNotApprovedException(String message) {
        super(message);
    }
}

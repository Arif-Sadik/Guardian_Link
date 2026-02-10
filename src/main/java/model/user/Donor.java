package model.user;

/**
 * Donor — can view children in a read-only dashboard.
 */
public class Donor extends User {

    public Donor() {
        setRole(UserRole.DONOR);
    }

    public Donor(String username, String password) {
        super(username, password, UserRole.DONOR);
    }
}

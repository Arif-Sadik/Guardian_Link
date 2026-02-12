package model.user;

/**
 * Caregiver — manages and cares for children in the system.
 */
public class Caregiver extends User {

    public Caregiver() {
        setRole(UserRole.CAREGIVER);
    }

    public Caregiver(String username, String password) {
        super(username, password, UserRole.CAREGIVER);
    }
}

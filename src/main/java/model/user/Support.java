package model.user;

/**
 * Support — assists users with concerns and resolves problems in the system.
 */
public class Support extends User {

    public Support() {
        setRole(UserRole.SUPPORT);
    }

    public Support(String username, String password) {
        super(username, password, UserRole.SUPPORT);
    }
}

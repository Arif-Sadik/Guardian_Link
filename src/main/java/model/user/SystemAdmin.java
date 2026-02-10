package model.user;

/**
 * System Administrator — can create and approve users.
 */
public class SystemAdmin extends User {

    public SystemAdmin() {
        setRole(UserRole.SYSTEM_ADMIN);
    }

    public SystemAdmin(String username, String password) {
        super(username, password, UserRole.SYSTEM_ADMIN);
    }
}

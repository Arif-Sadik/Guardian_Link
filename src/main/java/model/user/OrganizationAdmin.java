package model.user;

/**
 * Organization Administrator — can add and manage children.
 */
public class OrganizationAdmin extends User {

    public OrganizationAdmin() {
        setRole(UserRole.ORGANIZATION_ADMIN);
    }

    public OrganizationAdmin(String username, String password) {
        super(username, password, UserRole.ORGANIZATION_ADMIN);
    }
}

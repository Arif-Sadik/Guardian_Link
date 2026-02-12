package model.user;

/**
 * Abstract base class for all users in the system.
 * Demonstrates OOP inheritance — concrete subclasses define specific roles.
 */
public abstract class User {

    private int id;
    private String username;
    private String password;
    private String email;
    private UserRole role;
    private boolean approved;

    // ── Constructors ──────────────────────────────────────────

    public User() {
    }

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.approved = false; // new users are unapproved by default
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return role + " [" + username + "] approved=" + approved;
    }
}

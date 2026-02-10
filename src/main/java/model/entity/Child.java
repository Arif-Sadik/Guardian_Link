package model.entity;

/**
 * Represents a child managed by an organization.
 * Extended with gender, date of birth, and status.
 */
public class Child {

    private int id;
    private String name;
    private int age;
    private String organization;
    private String gender;
    private String dateOfBirth;
    private String status; // Active, Graduated, Archived

    // ── Constructors ──────────────────────────────────────────

    public Child() {
    }

    public Child(String name, int age, String organization) {
        this.name = name;
        this.age = age;
        this.organization = organization;
        this.status = "Active";
    }

    public Child(String name, int age, String organization, String gender, String dateOfBirth, String status) {
        this.name = name;
        this.age = age;
        this.organization = organization;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " (age " + age + ") — " + organization;
    }
}

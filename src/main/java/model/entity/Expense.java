package model.entity;

/**
 * Represents an expense recorded against a child's welfare fund.
 */
public class Expense {

    private int id;
    private int childId;
    private String category;
    private double amount;
    private String description;
    private String date;

    public Expense() {
    }

    public Expense(int childId, String category, double amount, String description, String date) {
        this.childId = childId;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

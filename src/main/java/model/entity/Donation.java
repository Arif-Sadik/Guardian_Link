package model.entity;

/**
 * Represents a donation made by a donor to a child.
 */
public class Donation {

    private int id;
    private int donorId;
    private int childId;
    private double amount;
    private String purpose;
    private String date;
    private String status;
    private boolean isRecurring; // Whether it's a subscription/recurring donation
    private String endDate; // Subscription end date (format: YYYY-MM-DD)
    private String frequency; // Monthly, Quarterly, Annually, etc.

    public Donation() {
        this.status = "Completed";
        this.isRecurring = false;
    }

    public Donation(int donorId, int childId, double amount, String purpose, String date) {
        this.donorId = donorId;
        this.childId = childId;
        this.amount = amount;
        this.purpose = purpose;
        this.date = date;
        this.status = "Completed";
        this.isRecurring = false;
    }

    public Donation(int donorId, int childId, double amount, String purpose, String date, boolean isRecurring, String endDate, String frequency) {
        this.donorId = donorId;
        this.childId = childId;
        this.amount = amount;
        this.purpose = purpose;
        this.date = date;
        this.status = "Completed";
        this.isRecurring = isRecurring;
        this.endDate = endDate;
        this.frequency = frequency;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}

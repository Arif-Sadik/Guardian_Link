package model.entity;

/**
 * Represents a medical record associated with a child.
 * Uses proper encapsulation — getters only where setters aren't needed.
 */
public class MedicalRecord {

    private int id;
    private int childId;
    private String bloodGroup;
    private String medicalCondition;
    private String lastCheckup;

    // ── Constructor ───────────────────────────────────────────

    public MedicalRecord(int childId, String bloodGroup, String medicalCondition, String lastCheckup) {
        this.childId = childId;
        this.bloodGroup = bloodGroup;
        this.medicalCondition = medicalCondition;
        this.lastCheckup = lastCheckup;
    }

    // ── Getters ───────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public int getChildId() {
        return childId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getMedicalCondition() {
        return medicalCondition;
    }

    public String getLastCheckup() {
        return lastCheckup;
    }

    // ── Setters (only for DB mapping) ─────────────────────────

    public void setId(int id) {
        this.id = id;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public void setMedicalCondition(String medicalCondition) {
        this.medicalCondition = medicalCondition;
    }

    public void setLastCheckup(String lastCheckup) {
        this.lastCheckup = lastCheckup;
    }
}

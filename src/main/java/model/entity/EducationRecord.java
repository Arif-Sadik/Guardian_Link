package model.entity;

/**
 * Represents an education record associated with a child.
 * Uses proper encapsulation — getters only where setters aren't needed.
 */
public class EducationRecord {

    private int id;
    private int childId;
    private String schoolName;
    private String grade;
    private double attendancePercentage;

    // ── Constructor ───────────────────────────────────────────

    public EducationRecord(int childId, String schoolName, String grade, double attendancePercentage) {
        this.childId = childId;
        this.schoolName = schoolName;
        this.grade = grade;
        this.attendancePercentage = attendancePercentage;
    }

    // ── Getters ───────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public int getChildId() {
        return childId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getGrade() {
        return grade;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    // ── Setters (only for DB mapping) ─────────────────────────

    public void setId(int id) {
        this.id = id;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }
}

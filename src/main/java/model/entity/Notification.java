package model.entity;

/**
 * Represents a notification for caregiver assignments and changes.
 */
public class Notification {

    private int id;
    private int caregiverId;
    private String message;
    private String notificationType; // "ASSIGNMENT", "REMOVAL", "UPDATE"
    private String childName;
    private int childId;
    private String timestamp;
    private boolean isRead;

    // ── Constructors ──────────────────────────────────────────

    public Notification() {
    }

    public Notification(int caregiverId, String message, String notificationType, String childName, int childId, String timestamp) {
        this.caregiverId = caregiverId;
        this.message = message;
        this.notificationType = notificationType;
        this.childName = childName;
        this.childId = childId;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(int caregiverId) {
        this.caregiverId = caregiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return notificationType + ": " + message + " (" + childName + ")";
    }
}

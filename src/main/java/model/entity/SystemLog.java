package model.entity;

/**
 * Represents a system log entry for audit trails and activity tracking.
 */
public class SystemLog {

    private int id;
    private String eventType;
    private String description;
    private String actor;
    private String timestamp;

    public SystemLog() {
    }

    public SystemLog(String eventType, String description, String actor, String timestamp) {
        this.eventType = eventType;
        this.description = description;
        this.actor = actor;
        this.timestamp = timestamp;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

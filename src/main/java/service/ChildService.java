package service;

import model.entity.Child;
import model.entity.Notification;
import model.user.Caregiver;
import model.user.User;
import repository.ChildRepository;
import repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Business logic for child management.
 */
public class ChildService {

    private final ChildRepository childRepository = new ChildRepository();
    private final UserRepository userRepository = new UserRepository();
    private final NotificationService notificationService = new NotificationService();

    /**
     * Adds a new child (automatically approved for demo purposes).
     * @return the ID of the newly added child, or -1 if save failed
     */
    public int addChild(Child child) {
        return childRepository.save(child);
    }

    /**
     * Returns all children.
     */
    public List<Child> getAllChildren() {
        return childRepository.findAll();
    }

    /**
     * Finds a child by their database ID.
     */
    public Child getChildById(int id) {
        return childRepository.findById(id);
    }

    /**
     * Updates an existing child.
     */
    public boolean updateChild(Child child) {
        return childRepository.updateChild(child);
    }

    /**
     * Deletes a child by ID.
     */
    public boolean deleteChild(int id) {
        return childRepository.deleteChild(id);
    }

    /**
     * Assigns a caregiver to a child and creates a notification.
     */
    public boolean assignCaregiverToChild(int childId, int caregiverId, String adminUsername) {
        Child child = childRepository.findById(childId);
        User caregiver = userRepository.findById(caregiverId);
        
        if (child == null || caregiver == null) {
            return false;
        }
        
        // Check if this is a new assignment (wasn't already assigned)
        boolean isNewAssignment = child.getAssignedCaregiverId() == null || 
                                   !child.getAssignedCaregiverId().equals(caregiverId);
        
        // Set the caregiver assignment
        child.setAssignedCaregiverId(caregiverId);
        boolean updated = childRepository.updateChild(child);
        
        if (updated && isNewAssignment) {
            // Create notification for the caregiver
            String message = "You have been assigned a new child: " + child.getName() + 
                           " (Age: " + child.getAge() + ")";
            Notification notification = new Notification(
                    caregiverId,
                    message,
                    "ASSIGNMENT",
                    child.getName(),
                    childId,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
            notificationService.createNotification(notification);
        }
        
        return updated;
    }

    /**
     * Removes a caregiver assignment from a child and creates a notification.
     */
    public boolean removeCaregiverFromChild(int childId, String adminUsername) {
        Child child = childRepository.findById(childId);
        
        if (child == null || child.getAssignedCaregiverId() == null) {
            return false;
        }
        
        int caregiverId = child.getAssignedCaregiverId();
        
        // Remove the caregiver assignment
        boolean removed = childRepository.removeCaregiverAssignment(childId);
        
        if (removed) {
            // Create notification for the caregiver about removal
            String message = "Child " + child.getName() + " has been removed from your supervision.";
            Notification notification = new Notification(
                    caregiverId,
                    message,
                    "REMOVAL",
                    child.getName(),
                    childId,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
            notificationService.createNotification(notification);
        }
        
        return removed;
    }

    /**
     * Gets all children assigned to a specific caregiver.
     */
    public List<Child> getChildrenByCaregiver(int caregiverId) {
        return childRepository.findByCaregiver(caregiverId);
    }

    /**
     * Gets all approved caregivers.
     */
    public List<User> getAllCaregivers() {
        return userRepository.findAllCaregivers();
    }
}

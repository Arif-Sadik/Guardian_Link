package service;

import model.entity.Notification;
import repository.NotificationRepository;

import java.util.List;

/**
 * Business logic for notification management.
 */
public class NotificationService {

    private final NotificationRepository notificationRepository = new NotificationRepository();

    /**
     * Creates and saves a new notification.
     */
    public boolean createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * Retrieves all notifications for a caregiver.
     */
    public List<Notification> getNotificationsByCaregiver(int caregiverId) {
        return notificationRepository.findByCaregiver(caregiverId);
    }

    /**
     * Retrieves only unread notifications for a caregiver.
     */
    public List<Notification> getUnreadNotifications(int caregiverId) {
        return notificationRepository.findUnreadByCaregiver(caregiverId);
    }

    /**
     * Marks a specific notification as read.
     */
    public boolean markNotificationAsRead(int notificationId) {
        return notificationRepository.markAsRead(notificationId);
    }

    /**
     * Marks all notifications for a caregiver as read.
     */
    public boolean markAllNotificationsAsRead(int caregiverId) {
        return notificationRepository.markAllAsRead(caregiverId);
    }

    /**
     * Deletes a notification.
     */
    public boolean deleteNotification(int notificationId) {
        return notificationRepository.deleteById(notificationId);
    }

    /**
     * Gets count of unread notifications for a caregiver.
     */
    public int getUnreadCount(int caregiverId) {
        return notificationRepository.countUnread(caregiverId);
    }
}

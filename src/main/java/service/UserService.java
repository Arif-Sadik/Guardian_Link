package service;

import model.user.User;
import repository.UserRepository;

import java.util.List;

/**
 * Business logic for user management.
 */
public class UserService {

    private final UserRepository userRepository = new UserRepository();

    /**
     * Creates a new user (unapproved by default).
     * 
     * @return true if created successfully, false if username exists or save failed
     */
    public boolean createUser(User user) {
        if (userRepository.usernameExists(user.getUsername())) {
            return false;
        }
        return userRepository.save(user);
    }

    /**
     * Returns all users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Approves a user by their database ID.
     */
    public void approveUser(int userId) {
        userRepository.approveUser(userId);
    }

    /**
     * Deletes a user by their database ID.
     * 
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteUser(int userId) {
        return userRepository.deleteUser(userId);
    }

    /**
     * Updates an existing user's information.
     * 
     * @return true if updated successfully, false otherwise
     */
    public boolean updateUser(User user) {
        return userRepository.updateUser(user);
    }

    /**
     * Finds a user by their database ID.
     */
    public User findById(int userId) {
        return userRepository.findById(userId);
    }
}

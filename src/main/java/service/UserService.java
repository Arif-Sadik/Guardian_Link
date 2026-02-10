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
     */
    public void createUser(User user) {
        userRepository.save(user);
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
}

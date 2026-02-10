package service;

import exception.UserNotApprovedException;
import model.user.User;
import repository.UserRepository;
import util.PasswordUtil;

/**
 * Handles authentication logic.
 */
public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    /**
     * Authenticates a user by username and password.
     *
     * @return the authenticated User
     * @throws UserNotApprovedException if the user exists but is not approved
     * @throws IllegalArgumentException if credentials are invalid
     */
    public User login(String username, String password) throws UserNotApprovedException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        String hashedPassword = PasswordUtil.hash(password);
        if (!user.getPassword().equals(hashedPassword)) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        if (!user.isApproved()) {
            throw new UserNotApprovedException(
                    "Your account has not been approved yet. Please contact the System Admin.");
        }

        return user;
    }
}

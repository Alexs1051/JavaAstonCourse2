package org.aston.learning.stage2.service;

import org.aston.learning.stage2.dao.UserDao;
import org.aston.learning.stage2.dao.UserDaoImpl;
import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private final UserDao userDao;

    // Email regexp
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Constructs
    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
    }

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User createUser(String name, String email, Integer age) throws UserServiceException {
        logger.debug("Creating new user: name={}, email={}, age={}", name, email, age);

        // Data validation
        validateUserData(name, email, age);

        try {
            // Check if user with this email is already exist
            User user = new User(name, email, age);
            User savedUser = userDao.save(user);

            logger.info("User created successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (UserServiceException e) {
            logger.error("Failed to create user with email: {}", email, e);
            throw new UserServiceException("Failed to create user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> getUserById(Long id) throws UserServiceException {
        logger.debug("Retrieving user by ID: {}", id);

        if (id == null || id <= 0) {
            logger.warn("Invalid user ID provided: {}", id);
            throw new UserServiceException("Invalid user ID: " + id);
        }

        try {
            Optional<User> user = userDao.findById(id);
            if (user.isPresent()) {
                logger.debug("User found with ID: {}", id);
            } else {
                logger.debug("User not found with ID: {}", id);
            }
            return user;
        } catch (UserServiceException e) {
            logger.error("Failed to retrieve user with ID: {}", id, e);
            throw new UserServiceException("Failed to retrieve user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAllUsers() throws UserServiceException {
        logger.debug("Retrieving all users");

        try {
            List<User> users = userDao.findAll();
            logger.debug("Retrieved {} users", users.size());
            return users;
        } catch (UserServiceException e) {
            logger.error("Failed to retrieve all users", e);
            throw new UserServiceException("Failed to retrieve users: " + e.getMessage(), e);
        }
    }

    @Override
    public User updateUser(Long id, String name, String email, Integer age) throws UserServiceException {
        logger.debug("Updating user with ID: {}, name={}, email={}, age={}", id, name, email, age);

        if (id == null || id <= 0) {
            logger.warn("Invalid user ID provided for update: {}", id);
            throw new UserServiceException("Invalid user ID: " + id);
        }

        // Data validation (if present)
        if (name != null && name.trim().isEmpty()) {
            throw new UserServiceException("Name cannot be empty");
        }
        if (email != null && !isValidEmail(email)) {
            throw new UserServiceException("Invalid email format: " + email);
        }
        if (age != null && !isValidAge(age)) {
            throw new UserServiceException("Invalid age: " + age);
        }

        try {
            // Get existing user
            Optional<User> existingUser = getUserById(id);
            if (existingUser.isEmpty()) {
                logger.warn("User not found for update with ID: {}", id);
                throw new UserServiceException("User not found with ID: " + id);
            }

            User user = existingUser.get();

            // Update only present fields
            if (name != null) {
                user.setName(name);
            }
            if (email != null) {
                user.setEmail(email);
            }
            if (age != null) {
                user.setAge(age);
            }

            User updatedUser = userDao.update(user);
            logger.info("User updated successfully with ID: {}", updatedUser.getId());
            return updatedUser;
        } catch (UserServiceException e) {
            logger.error("Failed to update user with ID: {}", id, e);
            throw new UserServiceException("Failed to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(Long id) throws UserServiceException {
        logger.debug("Deleting user with ID: {}", id);

        if (id == null || id <= 0) {
            logger.warn("Invalid user ID provided for deletion: {}", id);
            throw new UserServiceException("Invalid user ID: " + id);
        }

        try {
            if (!userExists(id)) {
                logger.warn("Attempt to delete non-existent user with ID: {}", id);
                throw new UserServiceException("User not found with ID: " + id);
            }

            userDao.delete(id);
            logger.info("User deleted successfully with ID: {}", id);
        } catch (UserServiceException e) {
            logger.error("Failed to delete user with ID: {}", id, e);
            throw new UserServiceException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean userExists(Long id) throws UserServiceException {
        if (id == null || id <= 0) {
            return false;
        }

        try {
            return userDao.findById(id).isPresent();
        } catch (UserServiceException e) {
            logger.error("Failed to check user existence with ID: {}", id, e);
            throw new UserServiceException("Failed to check user existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public boolean isValidAge(Integer age) {
        return age != null && age >= 0 && age <= 150;
    }

    private void validateUserData(String name, String email, Integer age) throws UserServiceException {
        if (name == null || name.trim().isEmpty()) {
            throw new UserServiceException("Name cannot be empty");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new UserServiceException("Email cannot be empty");
        }

        if (!isValidEmail(email)) {
            throw new UserServiceException("Invalid email format: " + email);
        }

        if (age == null || !isValidAge(age)) {
            throw new UserServiceException("Invalid age: " + age);
        }
    }
}
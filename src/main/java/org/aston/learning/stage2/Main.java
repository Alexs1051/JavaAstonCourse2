package org.aston.learning.stage2;

import org.aston.learning.stage2.dao.UserDaoImpl;
import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import org.aston.learning.stage2.service.UserService;
import org.aston.learning.stage2.service.UserServiceImpl;
import org.aston.learning.stage2.util.DatabaseInitializer;
import org.aston.learning.stage2.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final UserService userService = new UserServiceImpl(new UserDaoImpl());
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        logger.info("User Service application starting...");

        try {
            // Initialize database and create tables
            DatabaseInitializer.initialize();
            logger.info("Database initialization completed");

            boolean running = true;

            while (running) {
                displayMenu();
                System.out.print("\nEnter your choice: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        createUser();
                        break;
                    case "2":
                        getUserById();
                        break;
                    case "3":
                        getAllUsers();
                        break;
                    case "4":
                        updateUser();
                        break;
                    case "5":
                        deleteUser();
                        break;
                    case "6":
                        running = false;
                        break;
                    default:
                        logger.warn("Invalid menu choice entered: {}", choice);
                }

                if (running) {
                    System.out.println("\nPress enter to continue...");
                    scanner.nextLine();
                }

            }

            logger.info("Application shutdown initiated by user");

        } catch (Exception e) {
            logger.error("Application error", e);
        } finally {
            HibernateUtil.shutdown();
            scanner.close();
            logger.info("User Service application stopped");
        }
    }

    private static void displayMenu() {
        System.out.println("\n=== User Service ===");
        System.out.println("1. Create User");
        System.out.println("2. Get User by ID");
        System.out.println("3. Get All Users");
        System.out.println("4. Update User");
        System.out.println("5. Delete User");
        System.out.println("6. Exit");
    }

    private static void createUser() {
        logger.debug("Starting create user operation");

        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            if (name.isEmpty()) {
                logger.warn("Empty name provided during user creation");
                return;
            }

            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            if (email.isEmpty()) {
                logger.warn("Empty email provided during user creation");
                return;
            }

            System.out.print("Enter age: ");
            String ageInput = scanner.nextLine().trim();
            if (ageInput.isEmpty()) {
                logger.warn("Empty age provided during user creation");
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageInput);
                if (age < 0 || age > 150) {
                    logger.warn("Invalid age range provided: {}", age);
                    return;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid age format provided: {}", ageInput, e);
                return;
            }

            User savedUser = userService.createUser(name, email, age);

            logger.info("User created successfully with ID: {}", savedUser.getId());
        } catch (UserServiceException e) {
            logger.error("Error creating user", e);
        }
    }

    private static void getUserById() {
        logger.debug("Starting get user by ID operation");

        try {
            System.out.print("Enter user ID: ");
            String idInput = scanner.nextLine().trim();

            if (idInput.isEmpty()) {
                logger.warn("Empty ID provided during user retrieval");
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idInput);
            } catch (NumberFormatException e) {
                logger.warn("Invalid ID format provided: {}", idInput, e);
                return;
            }

            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                logger.debug("User found with ID: {} - {}", id, user.get());
            } else {
                logger.warn("User not found with ID: {}", id);
            }

        } catch (UserServiceException e) {
            logger.error("Error retrieving user by ID", e);
        }
    }

    private static void getAllUsers() {
        logger.debug("Starting get all users operation");

        try {
            List<User> users = userService.getAllUsers();

            if (users.isEmpty()) {
                logger.info("No users found in database");
            } else {
                logger.debug("Found {} users in database", users.size());

                System.out.println("----------------------------------------");
                for (int i = 0; i < users.size(); i++) {
                    System.out.println((i + 1) + ". " + users.get(i));
                }
            }

        } catch (UserServiceException e) {
            logger.error("Error retrieving all users", e);
        }
    }

    private static void updateUser() {
        logger.debug("Starting update user operation");

        try {
            System.out.print("Enter user ID to update: ");
            String idInput = scanner.nextLine().trim();

            if (idInput.isEmpty()) {
                logger.warn("Empty ID provided during user update");
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idInput);
            } catch (NumberFormatException e) {
                logger.warn("Invalid ID format provided during update: {}", idInput, e);
                return;
            }

            Optional<User> existingUser = userService.getUserById(id);
            if (existingUser.isEmpty()) {
                logger.warn("User not found for update with ID: {}", id);
                return;
            }
            User user = existingUser.get();

            logger.debug("Found user for update: {}", user.getEmail());

            String newName = null;
            String newEmail = null;
            Integer newAge = null;

            System.out.print("Name [" + user.getName() + "]: ");
            String nameInput = scanner.nextLine().trim();
            if (!nameInput.isEmpty()) {
                newName = nameInput;
                logger.debug("Updated name for user ID: {}", id);
            }

            System.out.print("Email [" + user.getEmail() + "]: ");
            String emailInput = scanner.nextLine().trim();
            if (!emailInput.isEmpty()) {
                newEmail = emailInput;
                logger.debug("Updated email for user ID: {}", id);
            }

            System.out.print("Age [" + user.getAge() + "]: ");
            String ageInput = scanner.nextLine().trim();
            if (!ageInput.isEmpty()) {
                try {
                    newAge = Integer.parseInt(ageInput);
                    if (newAge < 0 || newAge > 150) {
                        logger.warn("Invalid age range during update: {}", newAge);
                        newAge = null;
                    } else {
                        logger.debug("Updated age for user ID: {}", id);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid age format during update: {}", ageInput, e);
                }
            }

            User updatedUser = userService.updateUser(id, newName, newEmail, newAge);
            logger.info("User updated successfully with ID: {}", updatedUser.getId());

        } catch (UserServiceException e) {
            logger.error("Error updating user", e);
        }
    }

    private static void deleteUser() {
        logger.debug("Starting delete user operation");

        try {
            System.out.print("Enter user ID to delete: ");
            String idInput = scanner.nextLine().trim();

            if (idInput.isEmpty()) {
                logger.warn("Empty ID provided during user deletion");
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idInput);
            } catch (NumberFormatException e) {
                logger.warn("Invalid ID format provided during deletion: {}", idInput, e);
                return;
            }

            if (!userService.userExists(id)) {
                logger.warn("Attempt to delete non-existent user with ID: {}", id);
                return;
            }

            // Accept deletion
            System.out.print("! Are you sure you want to delete user with ID " + id + "? (yes/no): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if ("yes".equals(confirmation) || "y".equals(confirmation)) {
                userService.deleteUser(id);
                logger.info("User deleted successfully with ID: {}", id);
            } else {
                logger.info("User deletion cancelled for ID: {}", id);
            }

        } catch (UserServiceException e) {
            logger.error("Error deleting user", e);
        }
    }
}
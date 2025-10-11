package org.aston.learning.stage2;

import org.aston.learning.stage2.dao.UserDao;
import org.aston.learning.stage2.dao.UserDaoImpl;
import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import org.aston.learning.stage2.util.DatabaseInitializer;
import org.aston.learning.stage2.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final UserDao userDao = new UserDaoImpl();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        logger.info("User Service application starting...");

        try {
            // Инициализируем базу данных и таблицы
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
                        System.out.println("Invalid choice. Please try again.");
                }

                if (running) {
                    System.out.println("\nPress enter to continue...");
                    scanner.nextLine();
                }

            }
        } catch (Exception e) {
            logger.error("Application error", e);
            System.out.println("An error occurred: " + e.getMessage());
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
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();

            System.out.print("Enter email: ");
            String email = scanner.nextLine();

            System.out.print("Enter age: ");
            int age = Integer.parseInt(scanner.nextLine());

            User user = new User(name, email, age);
            User savedUser = userDao.save(user);

            System.out.println("User created successfully: " + savedUser);
        } catch (NumberFormatException e) {
            System.out.println("Invalid age format. Please enter a number.");
        } catch (UserServiceException e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
    }

    private static void getUserById() {
        try {
            System.out.print("Enter user ID: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> user = userDao.findById(id);
            if (user.isPresent()) {
                System.out.println("User found: " + user.get());
            } else {
                System.out.println("User not found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a number.");
        } catch (UserServiceException e) {
            System.out.println("Error finding user: " + e.getMessage());
        }
    }

    private static void getAllUsers() {
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("No users found.");
            } else {
                System.out.println("Users:");
                users.forEach(System.out::println);
            }
        } catch (UserServiceException e) {
            System.out.println("Error retrieving users: " + e.getMessage());
        }
    }

    private static void updateUser() {
        try {
            System.out.print("Enter user ID to update: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> existingUser = userDao.findById(id);
            if (existingUser.isEmpty()) {
                System.out.println("User not found with ID: " + id);
                return;
            }

            User user = existingUser.get();

            System.out.print("Enter new name (current: " + user.getName() + "): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) {
                user.setName(name);
            }

            System.out.print("Enter new email (current: " + user.getEmail() + "): ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) {
                user.setEmail(email);
            }

            System.out.print("Enter new age (current: " + user.getAge() + "): ");
            String ageInput = scanner.nextLine();
            if (!ageInput.isEmpty()) {
                user.setAge(Integer.parseInt(ageInput));
            }

            User updatedUser = userDao.update(user);
            System.out.println("User updated successfully: " + updatedUser);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (UserServiceException e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        try {
            System.out.print("Enter user ID to delete: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> user = userDao.findById(id);
            if (user.isPresent()) {
                userDao.delete(id);
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("User not found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a number.");
        } catch (UserServiceException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }
}
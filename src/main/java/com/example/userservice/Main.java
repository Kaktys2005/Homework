package com.example.userservice;

import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.UserDaoImpl;
import com.example.userservice.entity.User;
import com.example.userservice.util.HibernateUtil;
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
        try {
            runApplication();
        } catch (Exception e) {
            logger.error("Application error: ", e);
            System.err.println("Critical error occurred. See logs for details.");
        } finally {
            shutdownResources();
        }
    }

    private static void runApplication() {
        boolean isRunning = true;

        while (isRunning) {
            printMenu();
            int choice = getMenuChoice();

            if (choice == 1) {
                handleUserCreation();
            } else if (choice == 2) {
                handleUserSearch();
            } else if (choice == 3) {
                displayAllUsers();
            } else if (choice == 4) {
                handleUserUpdate();
            } else if (choice == 5) {
                handleUserDeletion();
            } else if (choice == 0) {
                isRunning = false;
                System.out.println("Exiting application...");
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== User Management System ===");
        System.out.println("1. Create User");
        System.out.println("2. Find User by ID");
        System.out.println("3. Show All Users");
        System.out.println("4. Update User");
        System.out.println("5. Delete User");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private static int getMenuChoice() {
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a number!");
            scanner.next();
        }
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        return choice;
    }

    private static void handleUserCreation() {
        System.out.println("\n[Create New User]");
        User user = new User();

        System.out.print("Enter name: ");
        user.setName(scanner.nextLine());

        System.out.print("Enter email: ");
        user.setEmail(scanner.nextLine());

        System.out.print("Enter age: ");
        user.setAge(scanner.nextInt());
        scanner.nextLine(); // Consume newline

        try {
            User createdUser = userDao.save(user);
            System.out.println("User created successfully: " + createdUser);
            logger.info("Created user: {}", createdUser);
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            logger.error("User creation failed", e);
        }
    }

    private static void handleUserSearch() {
        System.out.println("\n[Find User by ID]");
        System.out.print("Enter user ID: ");
        long id = scanner.nextLong();
        scanner.nextLine(); // Consume newline

        try {
            Optional<User> user = userDao.findById(id);
            if (user.isPresent()) {
                System.out.println("Found user: " + user.get());
            } else {
                System.out.println("User with ID " + id + " not found");
            }
        } catch (Exception e) {
            System.out.println("Search error: " + e.getMessage());
            logger.error("User search failed", e);
        }
    }

    private static void displayAllUsers() {
        System.out.println("\n[All Users]");
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("No users found");
            } else {
                users.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("Error fetching users: " + e.getMessage());
            logger.error("Failed to retrieve users", e);
        }
    }

    private static void handleUserUpdate() {
        System.out.println("\n[Update User]");
        System.out.print("Enter user ID to update: ");
        long id = scanner.nextLong();
        scanner.nextLine(); // Consume newline

        try {
            Optional<User> userOpt = userDao.findById(id);
            if (userOpt.isEmpty()) {
                System.out.println("User not found");
                return;
            }

            User user = userOpt.get();
            System.out.println("Current data: " + user);

            System.out.print("Enter new name (leave blank to keep current): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) {
                user.setName(name);
            }

            System.out.print("Enter new email (leave blank to keep current): ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) {
                user.setEmail(email);
            }

            System.out.print("Enter new age (0 to keep current): ");
            int age = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            if (age > 0) {
                user.setAge(age);
            }

            User updatedUser = userDao.update(user);
            System.out.println("User updated: " + updatedUser);
            logger.info("Updated user: {}", updatedUser);
        } catch (Exception e) {
            System.out.println("Update error: " + e.getMessage());
            logger.error("User update failed", e);
        }
    }

    private static void handleUserDeletion() {
        System.out.println("\n[Delete User]");
        System.out.print("Enter user ID to delete: ");
        long id = scanner.nextLong();
        scanner.nextLine(); // Consume newline

        try {
            Optional<User> userOpt = userDao.findById(id);
            if (userOpt.isPresent()) {
                userDao.delete(userOpt.get());
                System.out.println("User deleted successfully");
                logger.info("Deleted user with ID: {}", id);
            } else {
                System.out.println("User not found");
            }
        } catch (Exception e) {
            System.out.println("Deletion error: " + e.getMessage());
            logger.error("User deletion failed", e);
        }
    }

    private static void shutdownResources() {
        try {
            HibernateUtil.shutdown();
            scanner.close();
            System.out.println("Resources released successfully");
        } catch (Exception e) {
            logger.error("Resource shutdown failed", e);
        }
    }
}
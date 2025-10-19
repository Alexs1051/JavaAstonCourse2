package org.aston.learning.stage2.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = "userdb";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "root";

    public static void initialize() {
        try {
            // Attempt to connect to database
            testConnection();
            logger.info("Database connection test successful");
        } catch (Exception e) {
            logger.warn("Database connection failed, attempting to create database...");
            createDatabase();
        }

        // Initialize Hibernate and create tables
        initializeHibernate();
    }

    static void testConnection() throws Exception {
        String url = DEFAULT_URL + DB_NAME;
        try (Connection connection = DriverManager.getConnection(url, USERNAME, PASSWORD)) {
            // Check connection
        }
    }

    static void createDatabase() {
        try (Connection connection = DriverManager.getConnection(DEFAULT_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {

            // Create database
            String createDbSQL = "CREATE DATABASE " + DB_NAME;
            statement.executeUpdate(createDbSQL);
            logger.info("Database '{}' created successfully", DB_NAME);

        } catch (SQLException e) {
            // If databases already exist
            if (e.getMessage().contains("already exists")) {
                logger.info("Database '{}' already exists, continuing...", DB_NAME);
                return;
            }
            logger.error("Failed to create database '{}'", DB_NAME, e);
            throw new RuntimeException("Database creation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to create database '{}'", DB_NAME, e);
            throw new RuntimeException("Database creation failed: " + e.getMessage(), e);
        }
    }

    static void initializeHibernate() {
        try {
            // Get SessionFactory - Hibernate will automatically create tables
            HibernateUtil.getSessionFactory();
            logger.info("Hibernate initialized successfully");
        } catch (Exception e) {
            logger.error("Hibernate initialization failed", e);
            throw new RuntimeException("Hibernate initialization failed", e);
        }
    }
}
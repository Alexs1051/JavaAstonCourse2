package org.aston.learning.stage2.util;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = "userdb";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "root";

    public static void initialize() {
        try {
            // Сначала пытаемся подключиться к существующей базе данных
            testConnection();
            logger.info("Database connection test successful");
        } catch (Exception e) {
            logger.warn("Database connection failed, attempting to create database...");
            createDatabase();
        }

        // Инициализируем Hibernate, который создаст таблицы
        initializeHibernate();
    }

    private static void testConnection() throws Exception {
        String url = DEFAULT_URL + DB_NAME;
        try (Connection connection = DriverManager.getConnection(url, USERNAME, PASSWORD)) {
            // Просто проверяем соединение
        }
    }

    private static void createDatabase() {
        try (Connection connection = DriverManager.getConnection(DEFAULT_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {

            // Создаем базу данных
            String createDbSQL = "CREATE DATABASE " + DB_NAME;
            statement.executeUpdate(createDbSQL);
            logger.info("Database '{}' created successfully", DB_NAME);

        } catch (Exception e) {
            logger.error("Failed to create database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void initializeHibernate() {
        try {
            // Просто получаем SessionFactory - Hibernate автоматически создаст таблицы
            HibernateUtil.getSessionFactory();
            logger.info("Hibernate initialized successfully");
        } catch (Exception e) {
            logger.error("Hibernate initialization failed", e);
            throw new RuntimeException("Hibernate initialization failed", e);
        }
    }
}
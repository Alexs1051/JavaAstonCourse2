package org.aston.learning.stage2.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestDatabaseConfig {
    private static PostgreSQLContainer<?> postgreSQLContainer;

    public static SessionFactory createTestSessionFactory() {
        if (postgreSQLContainer == null) {
            postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
            postgreSQLContainer.start();
        }

        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgreSQLContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgreSQLContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.setProperty("hibernate.current_session_context_class", "thread");

        configuration.setProperty("hibernate.cache.use_second_level_cache", "false");
        configuration.setProperty("hibernate.cache.use_query_cache", "false");

        configuration.addAnnotatedClass(org.aston.learning.stage2.entity.User.class);

        return configuration.buildSessionFactory();
    }

    public static void stopContainer() {
        if (postgreSQLContainer != null) {
            postgreSQLContainer.stop();
            postgreSQLContainer = null;
        }
    }
}
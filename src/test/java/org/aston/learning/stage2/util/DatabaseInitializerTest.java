package org.aston.learning.stage2.util;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DatabaseInitializerTest {

    private Connection mockConnection;
    private Statement mockStatement;
    private SessionFactory mockSessionFactory;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(Statement.class);
        mockSessionFactory = mock(SessionFactory.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
    }

    @Test
    @DisplayName("Test successful database connection")
    void testConnection_Success() {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            assertDoesNotThrow(DatabaseInitializer::testConnection);
        }
    }

    @Test
    @DisplayName("Test failed database connection")
    void testConnection_Failure() {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            SQLException sqlException = new SQLException("Connection failed");
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(sqlException);

            assertThrows(Exception.class, DatabaseInitializer::testConnection);
        }
    }

    @Test
    @DisplayName("Test successful database creation")
    void createDatabase_Success() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            when(mockStatement.executeUpdate(anyString())).thenReturn(1);

            assertDoesNotThrow(DatabaseInitializer::createDatabase);
        }
    }

    @Test
    @DisplayName("Test database creation when database already exists")
    void createDatabase_AlreadyExists() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Create exception with message containing "already exists"
            SQLException sqlException = new SQLException("database \"userdb\" already exists");
            when(mockStatement.executeUpdate(anyString())).thenThrow(sqlException);

            assertDoesNotThrow(DatabaseInitializer::createDatabase);
        }
    }

    @Test
    @DisplayName("Test database creation with other SQL errors during execution")
    void createDatabase_OtherSQLExceptionOnExecute() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Create exception with message that does NOT contain "already exists"
            SQLException sqlException = new SQLException("permission denied to create database");
            when(mockStatement.executeUpdate(anyString())).thenThrow(sqlException);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    DatabaseInitializer::createDatabase);
            assertTrue(exception.getMessage().contains("Database creation failed"));
        }
    }

    @Test
    @DisplayName("Test database creation with connection failure to default URL")
    void createDatabase_ConnectionToDefaultUrlFails() {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            SQLException sqlException = new SQLException("Cannot connect to PostgreSQL");
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(sqlException);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    DatabaseInitializer::createDatabase);
            assertTrue(exception.getMessage().contains("Database creation failed"));
        }
    }

    @Test
    @DisplayName("Test database creation with general connection exceptions")
    void createDatabase_GeneralExceptionOnConnection() {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            RuntimeException runtimeException = new RuntimeException("General error");
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(runtimeException);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    DatabaseInitializer::createDatabase);
            assertTrue(exception.getMessage().contains("Database creation failed"));
        }
    }

    @Test
    @DisplayName("Test database creation with SQL exception during statement execution")
    void createDatabase_SQLExceptionOnStatement() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            SQLException sqlException = new SQLException("syntax error");
            when(mockStatement.executeUpdate(anyString())).thenThrow(sqlException);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    DatabaseInitializer::createDatabase);
            assertTrue(exception.getMessage().contains("Database creation failed"));
        }
    }

    @Test
    @DisplayName("Test successful Hibernate initialization")
    void initializeHibernate_Success() {
        try (MockedStatic<HibernateUtil> hibernateUtilMock = mockStatic(HibernateUtil.class)) {
            hibernateUtilMock.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);

            assertDoesNotThrow(DatabaseInitializer::initializeHibernate);
        }
    }

    @Test
    @DisplayName("Test failed Hibernate initialization")
    void initializeHibernate_Failure() {
        try (MockedStatic<HibernateUtil> hibernateUtilMock = mockStatic(HibernateUtil.class)) {
            RuntimeException runtimeException = new RuntimeException("Hibernate config error");
            hibernateUtilMock.when(HibernateUtil::getSessionFactory).thenThrow(runtimeException);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    DatabaseInitializer::initializeHibernate);
            assertTrue(exception.getMessage().contains("Hibernate initialization failed"));
        }
    }

    @Test
    @DisplayName("Test complete initialization with existing database")
    void initialize_WithExistingDatabase() {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class);
             MockedStatic<HibernateUtil> hibernateUtilMock = mockStatic(HibernateUtil.class)) {

            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);
            hibernateUtilMock.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);

            assertDoesNotThrow(DatabaseInitializer::initialize);
        }
    }

    @Test
    @DisplayName("Test complete initialization with database creation")
    void initialize_WithDatabaseCreation() throws SQLException {
        try (MockedStatic<DriverManager> driverManagerMock = mockStatic(DriverManager.class);
             MockedStatic<HibernateUtil> hibernateUtilMock = mockStatic(HibernateUtil.class)) {

            SQLException sqlException = new SQLException("DB not exists");
            driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(sqlException)  // First call (testConnection) fails
                    .thenReturn(mockConnection); // Second call (createDatabase) succeeds

            when(mockStatement.executeUpdate(anyString())).thenReturn(1);
            hibernateUtilMock.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);

            assertDoesNotThrow(DatabaseInitializer::initialize);
        }
    }

    @Test
    @DisplayName("Test SQLException message handling for 'already exists' scenario")
    void testSQLExceptionMessageHandling() {
        SQLException withAlreadyExists = new SQLException("database already exists");
        SQLException withoutAlreadyExists = new SQLException("permission denied");

        assertTrue(containsAlreadyExists(withAlreadyExists));
        assertFalse(containsAlreadyExists(withoutAlreadyExists));
    }

    private boolean containsAlreadyExists(SQLException e) {
        return e.getMessage().toLowerCase().contains("already exists");
    }
}
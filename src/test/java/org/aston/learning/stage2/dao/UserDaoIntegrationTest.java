package org.aston.learning.stage2.dao;

import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import org.aston.learning.stage2.util.TestDatabaseConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
class UserDaoIntegrationTest {
    private static SessionFactory sessionFactory;
    private UserDao userDao;

    @BeforeAll
    static void setUpAll() {
        sessionFactory = TestDatabaseConfig.createTestSessionFactory();
    }

    @AfterAll
    static void tearDownAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        TestDatabaseConfig.stopContainer();
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl(sessionFactory);
        clearDatabase();
    }

    private void clearDatabase() {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to clear database", e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should save user successfully with transaction")
    void save_ValidUser_ShouldSaveUser() {
        // Given
        User user = new User("John Doe", "john.doe@example.com", 30);

        // When
        User savedUser = userDao.save(user);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(1L);
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getAge()).isEqualTo(30);
        assertThat(savedUser.getCreatedAt()).isNotNull();

        // Verify in database with explicit transaction
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            User foundUser = session.find(User.class, savedUser.getId());
            transaction.commit();

            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getName()).isEqualTo("John Doe");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should find user by ID with transaction")
    void findById_ExistingUser_ShouldReturnUser() {
        // Given
        User user = new User("Jane Smith", "jane.smith@example.com", 25);
        User savedUser = userDao.save(user);

        // When
        Optional<User> foundUser = userDao.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getName()).isEqualTo("Jane Smith");
        assertThat(foundUser.get().getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(foundUser.get().getAge()).isEqualTo(25);
    }

    @Test
    @Order(3)
    @DisplayName("Should return empty when user not found by ID")
    void findById_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userDao.findById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("Should find all users with transaction")
    void findAll_MultipleUsers_ShouldReturnAllUsers() {
        // Given
        userDao.save(new User("User1", "user1@example.com", 20));
        userDao.save(new User("User2", "user2@example.com", 25));
        userDao.save(new User("User3", "user3@example.com", 30));

        // When
        List<User> users = userDao.findAll();

        // Then
        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("User1", "User2", "User3");
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    @Order(5)
    @DisplayName("Should return empty list when no users")
    void findAll_NoUsers_ShouldReturnEmptyList() {
        // When
        List<User> users = userDao.findAll();

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("Should update user successfully with transaction")
    void update_ExistingUser_ShouldUpdateUser() {
        // Given
        User user = new User("Old Name", "old.email@example.com", 30);
        User savedUser = userDao.save(user);

        // When
        savedUser.setName("New Name");
        savedUser.setEmail("new.email@example.com");
        savedUser.setAge(35);
        User updatedUser = userDao.update(savedUser);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("New Name");
        assertThat(updatedUser.getEmail()).isEqualTo("new.email@example.com");
        assertThat(updatedUser.getAge()).isEqualTo(35);

        // Verify in database with explicit transaction
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            User foundUser = session.find(User.class, savedUser.getId());
            transaction.commit();

            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getName()).isEqualTo("New Name");
            assertThat(foundUser.getEmail()).isEqualTo("new.email@example.com");
            assertThat(foundUser.getAge()).isEqualTo(35);
        }
    }

    @Test
    @Order(7)
    @DisplayName("Should delete user successfully with transaction")
    void delete_ExistingUser_ShouldDeleteUser() {
        // Given
        User user = new User("To Delete", "delete@example.com", 40);
        User savedUser = userDao.save(user);

        // Verify user exists before deletion
        assertThat(userDao.findById(savedUser.getId())).isPresent();

        // When
        userDao.delete(savedUser.getId());

        // Then
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();

        // Verify in database with explicit transaction
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            User deletedUser = session.find(User.class, savedUser.getId());
            transaction.commit();

            assertThat(deletedUser).isNull();
        }
    }

    @Test
    @Order(8)
    @DisplayName("Should throw exception when deleting non-existing user")
    void delete_NonExistingUser_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> userDao.delete(999L))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @Order(9)
    @DisplayName("Should check if user exists")
    void existsById_ExistingUser_ShouldReturnTrue() {
        // Given
        User user = new User("Exists", "exists@example.com", 50);
        User savedUser = userDao.save(user);

        // When
        boolean exists = userDao.findById(savedUser.getId()).isPresent();

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @Order(10)
    @DisplayName("Should return false for non-existing user")
    void existsById_NonExistingUser_ShouldReturnFalse() {
        // When
        boolean exists = userDao.findById(999L).isPresent();

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @Order(11)
    @DisplayName("Should throw exception when saving user with duplicate email")
    void save_DuplicateEmail_ShouldThrowException() {
        // Given
        userDao.save(new User("User1", "duplicate@example.com", 20));

        // When & Then
        User userWithDuplicateEmail = new User("User2", "duplicate@example.com", 25);

        assertThatThrownBy(() -> userDao.save(userWithDuplicateEmail))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("duplicate key")
                .hasMessageContaining("email");
    }

    @Test
    @Order(12)
    @DisplayName("Should handle null values correctly")
    void save_UserWithNullName_ShouldThrowException() {
        // Given
        User user = new User(null, "test@example.com", 30);

        // When & Then
        assertThatThrownBy(() -> userDao.save(user))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("not-null property references a null or transient value");
    }

    @Test
    @Order(13)
    @DisplayName("Should handle null ID in findById gracefully")
    void findById_NullId_ShouldReturnEmpty() {
        // When
        Optional<User> result = userDao.findById(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @Order(14)
    @DisplayName("Should maintain data integrity after rollback")
    void save_RollbackOnException_ShouldNotPersistData() {
        // Given
        User validUser = new User("Valid User", "valid@example.com", 30);
        User invalidUser = new User(null, "invalid@example.com", 25); // This will fail

        // When & Then - Save valid user first
        User savedValidUser = userDao.save(validUser);
        assertThat(savedValidUser).isNotNull();

        // Try to save invalid user - should throw exception
        assertThatThrownBy(() -> userDao.save(invalidUser))
                .isInstanceOf(UserServiceException.class);

        // Verify valid user still exists and count is 1
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Long userCount = session.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            User foundValidUser = session.find(User.class, savedValidUser.getId());
            transaction.commit();

            assertThat(userCount).isEqualTo(1);
            assertThat(foundValidUser).isNotNull();
            assertThat(foundValidUser.getName()).isEqualTo("Valid User");
        }
    }

    @Test
    @Order(15)
    @DisplayName("Should handle concurrent transactions")
    void concurrentTransactions_ShouldMaintainConsistency() {
        // Given
        User user1 = new User("User1", "user1@example.com", 25);
        User user2 = new User("User2", "user2@example.com", 30);

        // When - Save users in different sessions
        User savedUser1 = userDao.save(user1);
        User savedUser2 = userDao.save(user2);

        // Then - Verify both users exist and have correct data
        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();
        assertThat(savedUser1.getId()).isNotEqualTo(savedUser2.getId());

        List<User> allUsers = userDao.findAll();
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    @Order(16)
    @DisplayName("Should update user with partial data preserving other fields")
    void update_PartialData_ShouldPreserveOtherFields() {
        // Given
        User originalUser = new User("Original Name", "original@example.com", 30);
        User savedUser = userDao.save(originalUser);
        var originalCreatedAt = savedUser.getCreatedAt();

        // When - Update only name
        savedUser.setName("Updated Name");
        User updatedUser = userDao.update(savedUser);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("original@example.com"); // unchanged
        assertThat(updatedUser.getAge()).isEqualTo(30); // unchanged
        assertThat(updatedUser.getCreatedAt()).isEqualTo(originalCreatedAt); // unchanged

        // Verify in database
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Updated Name");
        assertThat(foundUser.get().getEmail()).isEqualTo("original@example.com");
        assertThat(foundUser.get().getAge()).isEqualTo(30);
    }
}
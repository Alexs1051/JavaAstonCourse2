package org.aston.learning.stage2.dao;

import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import org.aston.learning.stage2.util.TestDatabaseConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
        try (Session session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                session.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
            }
        }
    }

    @Test
    @DisplayName("Should save user successfully")
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
    }

    @Test
    @DisplayName("Should find user by ID")
    void findById_ExistingUser_ShouldReturnUser() {
        // Given
        User user = new User("Jane Smith", "jane.smith@example.com", 25);
        User savedUser = userDao.save(user);

        // When
        Optional<User> foundUser = userDao.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(1L);
        assertThat(foundUser.get().getName()).isEqualTo("Jane Smith");
        assertThat(foundUser.get().getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void findById_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userDao.findById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find all users")
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
    }

    @Test
    @DisplayName("Should return empty list when no users")
    void findAll_NoUsers_ShouldReturnEmptyList() {
        // When
        List<User> users = userDao.findAll();

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Should update user successfully")
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

        // Verify in database
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void delete_ExistingUser_ShouldDeleteUser() {
        // Given
        User user = new User("To Delete", "delete@example.com", 40);
        User savedUser = userDao.save(user);

        // When
        userDao.delete(savedUser.getId());

        // Then
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing user")
    void delete_NonExistingUser_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> userDao.delete(999L))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User not found");
    }

    @Test
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
    @DisplayName("Should return false for non-existing user")
    void existsById_NonExistingUser_ShouldReturnFalse() {
        // When
        boolean exists = userDao.findById(999L).isPresent();

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when saving user with duplicate email")
    void save_DuplicateEmail_ShouldThrowException() {
        // Given
        userDao.save(new User("User1", "duplicate@example.com", 20));

        // When & Then
        User userWithDuplicateEmail = new User("User2", "duplicate@example.com", 25);

        assertThatThrownBy(() -> userDao.save(userWithDuplicateEmail))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("duplicate key");
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void save_UserWithNullName_ShouldThrowException() {
        // Given
        User user = new User(null, "test@example.com", 30);

        // When & Then
        assertThatThrownBy(() -> userDao.save(user))
                .isInstanceOf(UserServiceException.class);
    }
}
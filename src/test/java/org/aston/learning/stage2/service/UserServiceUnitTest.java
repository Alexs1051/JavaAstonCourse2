package org.aston.learning.stage2.service;

import org.aston.learning.stage2.dao.UserDao;
import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit Tests")
class UserServiceUnitTest {
    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao);
    }

    @Test
    @DisplayName("Should create user with valid data")
    void createUser_ValidData_ShouldCreateUser() {
        // Given
        String name = "John Doe";
        String email = "john.doe@example.com";
        Integer age = 30;

        User expectedUser = new User(name, email, age);
        expectedUser.setId(1L);
        expectedUser.setCreatedAt(LocalDateTime.now());

        when(userDao.save(any(User.class))).thenReturn(expectedUser);

        // When
        User result = userService.createUser(name, email, age);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getAge()).isEqualTo(age);

        verify(userDao).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with invalid email")
    void createUser_InvalidEmail_ShouldThrowException() {
        // Given
        String name = "John Doe";
        String invalidEmail = "invalid-email";
        Integer age = 30;

        // When & Then
        assertThatThrownBy(() -> userService.createUser(name, invalidEmail, age))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid email format");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getUserById_ExistingUser_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        User expectedUser = new User("John Doe", "john@example.com", 30);
        expectedUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(expectedUser));

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        assertThat(result.get().getName()).isEqualTo("John Doe");

        verify(userDao).findById(userId);
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void getUserById_NonExistingUser_ShouldReturnEmpty() {
        // Given
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertThat(result).isEmpty();
        verify(userDao).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when getting user with invalid ID")
    void getUserById_InvalidId_ShouldThrowException() {
        // Given
        Long invalidId = 0L;

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(invalidId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid user ID");

        verify(userDao, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should return all users")
    void getAllUsers_UsersExist_ShouldReturnAllUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(
                new User("User1", "user1@example.com", 20),
                new User("User2", "user2@example.com", 25)
        );

        when(userDao.findAll()).thenReturn(expectedUsers);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getName)
                .containsExactly("User1", "User2");

        verify(userDao).findAll();
    }

    @Test
    @DisplayName("Should update user with valid data")
    void updateUser_ValidData_ShouldUpdateUser() {
        // Given
        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 30);
        existingUser.setId(userId);

        User updatedUser = new User("New Name", "new@example.com", 35);
        updatedUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.update(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.updateUser(userId, "New Name", "new@example.com", 35);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getAge()).isEqualTo(35);

        verify(userDao).findById(userId);
        verify(userDao).update(any(User.class));
    }

    @Test
    @DisplayName("Should update user with partial data")
    void updateUser_PartialData_ShouldUpdateOnlyProvidedFields() {
        // Given
        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 30);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - update only name
        User result = userService.updateUser(userId, "New Name", null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("old@example.com"); // unchanged
        assertThat(result.getAge()).isEqualTo(30); // unchanged

        verify(userDao).findById(userId);
        verify(userDao).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing user")
    void updateUser_NonExistingUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, "New Name", "new@example.com", 35))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User not found");

        verify(userDao).findById(userId);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_ExistingUser_ShouldDeleteUser() {
        // Given
        Long userId = 1L;
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));

        // When
        userService.deleteUser(userId);

        // Then
        verify(userDao).findById(userId);
        verify(userDao).delete(userId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing user")
    void deleteUser_NonExistingUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("User not found");

        verify(userDao).findById(userId);
        verify(userDao, never()).delete(anyLong());
    }

    @Test
    @DisplayName("Should check user existence using findById")
    void userExists_UsingFindById_ExistingUser_ShouldReturnTrue() {
        // Given
        Long userId = 1L;
        User existingUser = new User("John Doe", "john@example.com", 30);
        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));

        // When
        boolean exists = userService.userExists(userId);

        // Then
        assertThat(exists).isTrue();
        verify(userDao).findById(userId);
    }

    @Test
    @DisplayName("Should return false for non-existing user using findById")
    void userExists_UsingFindById_NonExistingUser_ShouldReturnFalse() {
        // Given
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // When
        boolean exists = userService.userExists(userId);

        // Then
        assertThat(exists).isFalse();
        verify(userDao).findById(userId);
    }

    @Test
    @DisplayName("Should validate email correctly")
    void isValidEmail_ValidEmails_ShouldReturnTrue() {
        // Valid emails
        assertThat(userService.isValidEmail("test@example.com")).isTrue();
        assertThat(userService.isValidEmail("user.name@domain.co.uk")).isTrue();
        assertThat(userService.isValidEmail("user+tag@example.org")).isTrue();
    }

    @Test
    @DisplayName("Should invalidate email correctly")
    void isValidEmail_InvalidEmails_ShouldReturnFalse() {
        // Invalid emails
        assertThat(userService.isValidEmail("invalid-email")).isFalse();
        assertThat(userService.isValidEmail("user@")).isFalse();
        assertThat(userService.isValidEmail("@domain.com")).isFalse();
        assertThat(userService.isValidEmail("")).isFalse();
        assertThat(userService.isValidEmail(null)).isFalse();
    }

    @Test
    @DisplayName("Should validate age correctly")
    void isValidAge_ValidAges_ShouldReturnTrue() {
        assertThat(userService.isValidAge(0)).isTrue();
        assertThat(userService.isValidAge(25)).isTrue();
        assertThat(userService.isValidAge(150)).isTrue();
    }

    @Test
    @DisplayName("Should invalidate age correctly")
    void isValidAge_InvalidAges_ShouldReturnFalse() {
        assertThat(userService.isValidAge(-1)).isFalse();
        assertThat(userService.isValidAge(151)).isFalse();
        assertThat(userService.isValidAge(null)).isFalse();
    }

    @Test
    @DisplayName("Should handle DAO exception when creating user")
    void createUser_DaoThrowsException_ShouldPropagateException() {
        // Given
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;

        when(userDao.save(any(User.class)))
                .thenThrow(new UserServiceException("Database error"));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(name, email, age))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Failed to create user");

        verify(userDao).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when getUserById fails with database error")
    void getUserById_DaoThrowsException_ShouldPropagateException() {
        // Given
        Long userId = 1L;
        String errorMessage = "Database connection failed";

        when(userDao.findById(userId))
                .thenThrow(new UserServiceException(errorMessage));

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Failed to retrieve user")
                .hasMessageContaining(errorMessage)
                .hasCauseInstanceOf(UserServiceException.class);

        verify(userDao).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when getAllUsers fails with database error")
    void getAllUsers_DaoThrowsException_ShouldPropagateException() {
        // Given
        String errorMessage = "Connection timeout";

        when(userDao.findAll())
                .thenThrow(new UserServiceException(errorMessage));

        // When & Then
        assertThatThrownBy(() -> userService.getAllUsers())
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Failed to retrieve users")
                .hasMessageContaining(errorMessage)
                .hasCauseInstanceOf(UserServiceException.class);

        verify(userDao).findAll();
    }

    @Test
    @DisplayName("Should handle null ID in userExists gracefully")
    void userExists_NullId_ShouldReturnFalse() {
        // When
        boolean exists = userService.userExists(null);

        // Then
        assertThat(exists).isFalse();
        verify(userDao, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should handle invalid ID in userExists gracefully")
    void userExists_InvalidId_ShouldReturnFalse() {
        // When
        boolean exists = userService.userExists(0L);

        // Then
        assertThat(exists).isFalse();
        verify(userDao, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should handle negative ID in userExists gracefully")
    void userExists_NegativeId_ShouldReturnFalse() {
        // When
        boolean exists = userService.userExists(-1L);

        // Then
        assertThat(exists).isFalse();
        verify(userDao, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when updating user with null ID")
    void updateUser_NullId_ShouldThrowException() {
        // Given
        Long nullId = null;

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(nullId, "Name", "email@example.com", 25))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid user ID");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating user with invalid ID")
    void updateUser_InvalidId_ShouldThrowException() {
        // Given
        Long invalidId = 0L;

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(invalidId, "Name", "email@example.com", 25))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid user ID");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating user with negative ID")
    void updateUser_NegativeId_ShouldThrowException() {
        // Given
        Long negativeId = -1L;

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(negativeId, "Name", "email@example.com", 25))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid user ID");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating user with empty name")
    void updateUser_EmptyName_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String emptyName = "   ";

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, emptyName, "email@example.com", 25))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Name cannot be empty");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating user with invalid email")
    void updateUser_InvalidEmail_ShouldThrowException() {
        // Given
        Long userId = 1L;
        String invalidEmail = "invalid-email";

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, "Name", invalidEmail, 25))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid email format");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating user with invalid age")
    void updateUser_InvalidAge_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Integer invalidAge = -5;

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, "Name", "email@example.com", invalidAge))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid age");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating user with too high age")
    void updateUser_TooHighAge_ShouldThrowException() {
        // Given
        Long userId = 1L;
        Integer tooHighAge = 151;

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, "Name", "email@example.com", tooHighAge))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid age");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting user with null ID")
    void deleteUser_NullId_ShouldThrowException() {
        // Given
        Long nullId = null;

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(nullId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid user ID");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).delete(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when deleting user with invalid ID")
    void deleteUser_InvalidId_ShouldThrowException() {
        // Given
        Long invalidId = 0L;

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(invalidId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid user ID");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).delete(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when deleting user with negative ID")
    void deleteUser_NegativeId_ShouldThrowException() {
        // Given
        Long negativeId = -1L;

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(negativeId))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid user ID");

        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).delete(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when creating user with null name")
    void createUser_NullName_ShouldThrowException() {
        // Given
        String nullName = null;
        String email = "test@example.com";
        Integer age = 25;

        // When & Then
        assertThatThrownBy(() -> userService.createUser(nullName, email, age))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Name cannot be empty");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with empty name")
    void createUser_EmptyName_ShouldThrowException() {
        // Given
        String emptyName = "   ";
        String email = "test@example.com";
        Integer age = 25;

        // When & Then
        assertThatThrownBy(() -> userService.createUser(emptyName, email, age))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Name cannot be empty");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with null email")
    void createUser_NullEmail_ShouldThrowException() {
        // Given
        String name = "John Doe";
        String nullEmail = null;
        Integer age = 25;

        // When & Then
        assertThatThrownBy(() -> userService.createUser(name, nullEmail, age))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Email cannot be empty");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with empty email")
    void createUser_EmptyEmail_ShouldThrowException() {
        // Given
        String name = "John Doe";
        String emptyEmail = "   ";
        Integer age = 25;

        // When & Then
        assertThatThrownBy(() -> userService.createUser(name, emptyEmail, age))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Email cannot be empty");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with null age")
    void createUser_NullAge_ShouldThrowException() {
        // Given
        String name = "John Doe";
        String email = "test@example.com";
        Integer nullAge = null;

        // When & Then
        assertThatThrownBy(() -> userService.createUser(name, email, nullAge))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid age");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with negative age")
    void createUser_NegativeAge_ShouldThrowException() {
        // Given
        String name = "John Doe";
        String email = "test@example.com";
        Integer negativeAge = -1;

        // When & Then
        assertThatThrownBy(() -> userService.createUser(name, email, negativeAge))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid age");

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with too high age")
    void createUser_TooHighAge_ShouldThrowException() {
        // Given
        String name = "John Doe";
        String email = "test@example.com";
        Integer tooHighAge = 151;

        // When & Then
        assertThatThrownBy(() -> userService.createUser(name, email, tooHighAge))
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("Invalid age");

        verify(userDao, never()).save(any(User.class));
    }
}
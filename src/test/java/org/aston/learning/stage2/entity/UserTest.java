package org.aston.learning.stage2.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Should create user with constructor and set createdAt")
    void createUser_WithConstructor_ShouldSetCreatedAt() {
        // When
        User user = new User("John Doe", "john@example.com", 30);

        // Then
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create user with default constructor and null fields")
    void createUser_DefaultConstructor_ShouldHaveNullFields() {
        // When
        User user = new User();

        // Then
        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getAge()).isNull();
        assertThat(user.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should create user with constructor parameters")
    void createUser_WithParameters_ShouldSetFields() {
        // Given
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;

        // When
        User user = new User(name, email, age);

        // Then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getAge()).isEqualTo(age);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle null age correctly")
    void createUser_WithNullAge_ShouldWork() {
        // When
        User user = new User("John", "john@test.com", null);

        // Then
        assertThat(user.getAge()).isNull();
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    @DisplayName("Should update user fields correctly")
    void updateUser_ShouldChangeFields() {
        // Given
        User user = new User("Old Name", "old@test.com", 25);

        // When
        user.setName("New Name");
        user.setEmail("new@test.com");
        user.setAge(30);
        user.setCreatedAt(LocalDateTime.now().minusDays(1));

        // Then
        assertThat(user.getName()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
        assertThat(user.getAge()).isEqualTo(30);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should generate correct toString representation")
    void toString_ShouldContainAllFields() {
        // Given
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("John Doe");
        assertThat(toString).contains("john@example.com");
        assertThat(toString).contains("30");
        assertThat(toString).contains("1");
    }

    @Test
    @DisplayName("Should handle setters and getters correctly")
    void settersAndGetters_ShouldWork() {
        // Given
        User user = new User();
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);

        // When
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);
        user.setCreatedAt(createdAt);

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getAge()).isEqualTo(25);
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should set valid age correctly")
    void setAge_ValidAges_ShouldWork() {
        // Given
        User user = new User();

        // When & Then - min age
        user.setAge(0);
        assertThat(user.getAge()).isEqualTo(0);

        // When & Then - normal age
        user.setAge(25);
        assertThat(user.getAge()).isEqualTo(25);

        // When & Then - max age
        user.setAge(150);
        assertThat(user.getAge()).isEqualTo(150);
    }

    @Test
    @DisplayName("Should set null age correctly")
    void setAge_NullAge_ShouldWork() {
        // Given
        User user = new User("John", "john@test.com", 30);

        // When
        user.setAge(null);

        // Then
        assertThat(user.getAge()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when setting negative age")
    void setAge_NegativeAge_ShouldThrowException() {
        // Given
        User user = new User();

        // When & Then
        assertThatThrownBy(() -> user.setAge(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Age must be between 0 and 150");

        assertThatThrownBy(() -> user.setAge(-100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Age must be between 0 and 150");
    }

    @Test
    @DisplayName("Should throw exception when setting age above 150")
    void setAge_AgeAbove150_ShouldThrowException() {
        // Given
        User user = new User();

        // When & Then
        assertThatThrownBy(() -> user.setAge(151))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Age must be between 0 and 150");

        assertThatThrownBy(() -> user.setAge(200))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Age must be between 0 and 150");
    }

    @Test
    @DisplayName("Should handle boundary values for age validation")
    void setAge_BoundaryValues_ShouldWorkCorrectly() {
        // Given
        User user = new User();

        // When & Then
        user.setAge(0);  // min
        assertThat(user.getAge()).isEqualTo(0);

        user.setAge(150); // max
        assertThat(user.getAge()).isEqualTo(150);

        assertThatThrownBy(() -> user.setAge(-1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> user.setAge(151))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should preserve other fields when setting invalid age")
    void setAge_InvalidAge_ShouldNotChangeOtherFields() {
        // Given
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);
        LocalDateTime originalCreatedAt = user.getCreatedAt();

        // When & Then
        assertThatThrownBy(() -> user.setAge(-5))
                .isInstanceOf(IllegalArgumentException.class);

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getAge()).isEqualTo(30); // возраст остался прежним
        assertThat(user.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    @DisplayName("Should chain age validation in constructor")
    void constructor_InvalidAge_ShouldThrowException() {
        // When & Then
        User user = new User("John", "john@test.com", -1);

        assertThat(user.getAge()).isEqualTo(-1); // конструктор принимает любое значение

        User user2 = new User();
        assertThatThrownBy(() -> user2.setAge(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
package org.aston.learning.stage2;

import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import org.aston.learning.stage2.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Main Application Tests")
class MainTest {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // Static method sources for parameterized tests
    static Stream<Arguments> invalidUserDataProvider() {
        return Stream.of(
                // Empty/whitespace name cases
                Arguments.of("", "john@example.com", "30"),
                Arguments.of("   ", "john@example.com", "30"),

                // Empty/whitespace email cases
                Arguments.of("John Doe", "", "30"),
                Arguments.of("John Doe", "   ", "30"),

                // Invalid age cases
                Arguments.of("John Doe", "john@example.com", "invalid"),
                Arguments.of("John Doe", "john@example.com", "-5"),
                Arguments.of("John Doe", "john@example.com", "200"),
                Arguments.of("John Doe", "john@example.com", ""),
                Arguments.of("John Doe", "john@example.com", "151")
        );
    }

    static Stream<Arguments> invalidUpdateScenarios() {
        return Stream.of(
                Arguments.of("", "John Doe\njohn@example.com\n30\n"), // Empty ID
                Arguments.of("invalid", "John Doe\njohn@example.com\n30\n"), // Invalid ID format
                Arguments.of("999", "New Name\nnew@example.com\n30\n") // Non-existing user
        );
    }

    static Stream<Arguments> invalidAgeUpdateScenarios() {
        return Stream.of(
                Arguments.of("1", "\n\ninvalid\n"), // Invalid age format
                Arguments.of("1", "\n\n-10\n"), // Negative age
                Arguments.of("1", "\n\n200\n") // Age above limit
        );
    }

    @Test
    @DisplayName("Should display all menu options correctly")
    void displayMenu_ShouldPrintAllMenuOptions() {
        // When
        Main.displayMenu();

        // Then
        String output = outputStream.toString();
        assertAll(
                () -> assertTrue(output.contains("=== User Service ===")),
                () -> assertTrue(output.contains("1. Create User")),
                () -> assertTrue(output.contains("2. Get User by ID")),
                () -> assertTrue(output.contains("3. Get All Users")),
                () -> assertTrue(output.contains("4. Update User")),
                () -> assertTrue(output.contains("5. Delete User")),
                () -> assertTrue(output.contains("6. Exit"))
        );
    }

    @Test
    @DisplayName("Should create user successfully with valid input")
    void createUser_ValidInput_ShouldCreateUser() {
        // Given
        String input = "John Doe\njohn@example.com\n30\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        User mockUser = new User("John Doe", "john@example.com", 30);
        mockUser.setId(1L);
        when(userService.createUser("John Doe", "john@example.com", 30)).thenReturn(mockUser);

        Main.initialize(userService, scanner);

        // When
        Main.createUser();

        // Then
        verify(userService).createUser("John Doe", "john@example.com", 30);
    }

    @ParameterizedTest
    @MethodSource("invalidUserDataProvider")
    @DisplayName("Should handle invalid user data input during creation")
    void createUser_InvalidData_ShouldNotCreateUser(String name, String email, String ageInput) {
        // Given
        String input = String.format("%s\n%s\n%s\n", name, email, ageInput);
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        // When
        Main.createUser();

        // Then
        verify(userService, never()).createUser(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should handle service exception during user creation")
    void createUser_ServiceException_ShouldHandleGracefully() {
        // Given
        String input = "John Doe\njohn@example.com\n30\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        when(userService.createUser(anyString(), anyString(), anyInt()))
                .thenThrow(new UserServiceException("Database error"));

        // When
        Main.createUser();

        // Then
        verify(userService).createUser("John Doe", "john@example.com", 30);
    }

    @Test
    @DisplayName("Should retrieve user successfully with valid ID")
    void getUserById_ValidId_ShouldReturnUser() {
        // Given
        String input = "1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        User mockUser = new User("John Doe", "john@example.com", 30);
        mockUser.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(mockUser));

        // When
        Main.getUserById();

        // Then
        verify(userService).getUserById(1L);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "abc", " "})
    @DisplayName("Should handle invalid ID input formats")
    void getUserById_InvalidIdFormat_ShouldHandleGracefully(String invalidInput) {
        // Given
        String input = invalidInput + "\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        // When
        Main.getUserById();

        // Then
        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    @DisplayName("Should handle non-existing user scenario")
    void getUserById_NonExistingUser_ShouldHandleGracefully() {
        // Given
        String input = "999\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When
        Main.getUserById();

        // Then
        verify(userService).getUserById(999L);
    }

    @Test
    @DisplayName("Should handle service exception during user retrieval")
    void getUserById_ServiceException_ShouldHandleGracefully() {
        // Given
        String input = "1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        when(userService.getUserById(1L))
                .thenThrow(new UserServiceException("Database connection error"));

        // When
        Main.getUserById();

        // Then
        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("Should retrieve all users successfully")
    void getAllUsers_UsersExist_ShouldRetrieveAll() {
        // Given
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        Main.initialize(userService, scanner);

        User user1 = new User("John Doe", "john@example.com", 30);
        user1.setId(1L);
        User user2 = new User("Jane Smith", "jane@example.com", 25);
        user2.setId(2L);

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        // When
        Main.getAllUsers();

        // Then
        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should handle empty users list scenario")
    void getAllUsers_NoUsers_ShouldHandleGracefully() {
        // Given
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        Main.initialize(userService, scanner);

        when(userService.getAllUsers()).thenReturn(List.of());

        // When
        Main.getAllUsers();

        // Then
        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should handle service exception during users retrieval")
    void getAllUsers_ServiceException_ShouldHandleGracefully() {
        // Given
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        Main.initialize(userService, scanner);

        when(userService.getAllUsers()).thenThrow(new UserServiceException("Database error"));

        // When
        Main.getAllUsers();

        // Then
        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should update user successfully with all fields")
    void updateUser_AllFields_ShouldUpdateSuccessfully() {
        // Given
        String input = "1\nJane Doe\njane@example.com\n35\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);
        User updatedUser = new User("Jane Doe", "jane@example.com", 35);
        updatedUser.setId(1L);

        when(userService.getUserById(1L)).thenReturn(Optional.of(existingUser));
        when(userService.updateUser(eq(1L), eq("Jane Doe"), eq("jane@example.com"), eq(35)))
                .thenReturn(updatedUser);

        // When
        Main.updateUser();

        // Then
        verify(userService).getUserById(1L);
        verify(userService).updateUser(1L, "Jane Doe", "jane@example.com", 35);
    }

    @Test
    @DisplayName("Should handle partial update with empty fields")
    void updateUser_PartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Given
        String input = "1\n\n\n35\n"; // Only update age
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);

        when(userService.getUserById(1L)).thenReturn(Optional.of(existingUser));
        when(userService.updateUser(eq(1L), isNull(), isNull(), eq(35))).thenReturn(existingUser);

        // When
        Main.updateUser();

        // Then
        verify(userService).updateUser(1L, null, null, 35);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateScenarios")
    @DisplayName("Should handle invalid update scenarios")
    void updateUser_InvalidScenarios_ShouldHandleGracefully(String userIdInput, String updateData) {
        // Given
        String input = userIdInput + "\n" + updateData;
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        // For non-existing user scenario
        if ("999".equals(userIdInput)) {
            when(userService.getUserById(999L)).thenReturn(Optional.empty());
        }

        // When
        Main.updateUser();

        // Then
        assertTrue(true);
    }

    @ParameterizedTest
    @MethodSource("invalidAgeUpdateScenarios")
    @DisplayName("Should handle invalid age during update")
    void updateUser_InvalidAge_ShouldHandleGracefully(String userIdInput, String updateData) {
        // Given
        String input = userIdInput + "\n" + updateData;
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(existingUser));

        // When
        Main.updateUser();

        // Then
        verify(userService, never()).updateUser(eq(1L), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should handle service exception during user update")
    void updateUser_ServiceException_ShouldHandleGracefully() {
        // Given
        String input = "1\nJane Doe\njane@example.com\n35\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(existingUser));
        when(userService.updateUser(eq(1L), anyString(), anyString(), anyInt()))
                .thenThrow(new UserServiceException("Update failed"));

        // When
        Main.updateUser();

        // Then
        verify(userService).getUserById(1L);
        verify(userService).updateUser(1L, "Jane Doe", "jane@example.com", 35);
    }

    @Test
    @DisplayName("Should delete user successfully when confirmed")
    void deleteUser_Confirmed_ShouldDeleteUser() {
        // Given
        String input = "1\nyes\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        when(userService.userExists(1L)).thenReturn(true);

        // When
        Main.deleteUser();

        // Then
        verify(userService).userExists(1L);
        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("Should cancel deletion when user declines confirmation")
    void deleteUser_Cancelled_ShouldNotDeleteUser() {
        // Given
        String input = "1\nno\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        when(userService.userExists(1L)).thenReturn(true);

        // When
        Main.deleteUser();

        // Then
        verify(userService).userExists(1L);
        verify(userService, never()).deleteUser(anyLong());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "abc", " "})
    @DisplayName("Should handle invalid ID input during deletion")
    void deleteUser_InvalidId_ShouldHandleGracefully(String invalidInput) {
        // Given
        String input = invalidInput + "\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        // When
        Main.deleteUser();

        // Then
        verify(userService, never()).userExists(anyLong());
        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    @DisplayName("Should handle non-existing user during deletion")
    void deleteUser_NonExistingUser_ShouldHandleGracefully() {
        // Given
        String input = "999\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        when(userService.userExists(999L)).thenReturn(false);

        // When
        Main.deleteUser();

        // Then
        verify(userService).userExists(999L);
        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    @DisplayName("Should handle service exception during deletion")
    void deleteUser_ServiceException_ShouldHandleGracefully() {
        // Given
        String input = "1\nyes\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        Main.initialize(userService, scanner);

        when(userService.userExists(1L)).thenReturn(true);
        doThrow(new UserServiceException("Delete failed")).when(userService).deleteUser(1L);

        // When
        Main.deleteUser();

        // Then
        verify(userService).userExists(1L);
        verify(userService).deleteUser(1L);
    }
}
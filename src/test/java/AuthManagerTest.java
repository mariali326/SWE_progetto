import flightPlanner.Admin;
import flightPlanner.AuthManager;
import flightPlanner.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthManagerTest {

    private static AuthManager authManager;

    @BeforeAll
    public static void setUp() throws IOException {
        authManager = new AuthManager();
    }

    @Test
    @DisplayName("Test that checks when a user is registered, s/he can login successfully")
    public void testLoginSuccessful() {
        boolean loginResult = authManager.login("admin", "admin123");
        assertTrue(loginResult);
        assertEquals("admin", authManager.getLoggedInUser());
    }

    @Test
    @DisplayName("Test that checks when an unknown user tries to login, s/he will fail")
    public void testLoginFailure() {
        boolean loginResult = authManager.login("nonexistentUser", "password");
        assertFalse(loginResult);
        assertNull(authManager.getLoggedInUser());
    }

    @Test
    @DisplayName("Test that checks the registration of a new user")
    public void testRegisterNewUser() throws IOException {
        boolean registerResult = authManager.register("newUser", "newPass123", "newuser@example.com", "Passenger");
        assertTrue(registerResult);
        Map<String, User> users = authManager.getUsers();
        User user = users.get("newUser");
        assertEquals("newPass123", user.getPassword());
        assertEquals("newuser@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Test that checks when a user is already registered, s/he can't register again")
    public void testRegisterDuplicateUser() throws IOException {
        authManager.register("duplicateUser", "password", "duplicate@example.com", "Passenger");
        boolean registerResult = authManager.register("duplicateUser", "password", "duplicate@example.com", "Passenger");
        assertFalse(registerResult);  // Non dovrebbe permettere la registrazione
    }

    @Test
    @DisplayName("Test that checks the functionality of the logout() method")
    public void testLogout() {
        authManager.login("admin", "admin123");
        authManager.logout();
        assertNull(authManager.getLoggedInUser());
    }

    @Test
    @DisplayName("Test that checks if the role of an user matches")
    public void testGetCurrentUserRole() {
        authManager.login("admin", "admin123");
        assertTrue(authManager.isAdmin());
        String role = authManager.getCurrentUserRole();
        assertEquals("Admin", role);
    }

    @Test
    @DisplayName("Test that checks the validity of updating a user's password and email")
    public void testUpdateUser() throws IOException {
        authManager.register("updateUser", "oldPassword", "oldEmail@example.com", "Passenger");
        boolean updateResult = authManager.updateUser("updateUser", "newPassword", "newEmail@example.com");
        assertTrue(updateResult);

        Map<String, User> users = authManager.getUsers();
        User user = users.get("updateUser");
        assertEquals("newPassword", user.getPassword());
        assertEquals("newEmail@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Test that checks the validity of a user's unsubscription")
    public void testRemoveUser() throws IOException {
        authManager.register("deleteUser", "password", "delete@example.com", "Passenger");
        authManager.removeUser("deleteUser");

        assertNull(authManager.getUsers().get("deleteUser"));

    }

    @Test
    @DisplayName("Test that checks loading users from the CSV file works correctly and verifies that it's possible to find a user listed in the file")
    public void testLoadUsersFromCSV() {

        assertNotNull(authManager.getUsers());
        assertFalse(authManager.getUsers().isEmpty());

        User admin = authManager.getUsers().get("admin");
        assertInstanceOf(Admin.class, admin);
    }
}

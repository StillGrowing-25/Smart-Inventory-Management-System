package com.inventory.service;

import com.inventory.dao.UserDAO;
import com.inventory.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
public class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private AuthService authService;

    private User adminUser;
    private User managerUser;
    private User staffUser;

    @BeforeEach
    void setUp() {
        adminUser   = new User(1, "admin",   "admin123",  "Alice Admin",   "ADMIN",   0);
        managerUser = new User(2, "manager", "mgr123",    "Bob Manager",   "MANAGER", 2);
        staffUser   = new User(3, "staff1",  "staff123",  "Charlie Staff", "STAFF",   3);

        // Always reset static state before each test
        authService.logout();
    }

    @AfterEach
    void tearDown() {
        // Clean static state after each test
        authService.logout();
    }

    // ── login ────────────────────────────────────────────────

    @Test
    @DisplayName("login returns user on valid credentials")
    void testLogin_validCredentials() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);

        User result = authService.login("admin", "admin123");

        assertNotNull(result);
        assertEquals("Alice Admin", result.getFullName());
        assertEquals("ADMIN",       result.getRole());
    }

    @Test
    @DisplayName("login returns null on invalid credentials")
    void testLogin_invalidCredentials() {
        when(userDAO.login("admin", "wrongpass")).thenReturn(null);

        User result = authService.login("admin", "wrongpass");

        assertNull(result);
    }

    @Test
    @DisplayName("login returns null for null username")
    void testLogin_nullUsername() {
        User result = authService.login(null, "password");
        assertNull(result);
        verify(userDAO, never()).login(any(), any());
    }

    @Test
    @DisplayName("login returns null for null password")
    void testLogin_nullPassword() {
        User result = authService.login("admin", null);
        assertNull(result);
        verify(userDAO, never()).login(any(), any());
    }

    @Test
    @DisplayName("login returns null for empty username")
    void testLogin_emptyUsername() {
        User result = authService.login("  ", "admin123");
        assertNull(result);
        verify(userDAO, never()).login(any(), any());
    }

    @Test
    @DisplayName("login returns null for empty password")
    void testLogin_emptyPassword() {
        User result = authService.login("admin", "");
        assertNull(result);
    }

    @Test
    @DisplayName("login trims whitespace from credentials")
    void testLogin_trimsWhitespace() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);

        User result = authService.login(" admin ", " admin123 ");

        assertNotNull(result);
        verify(userDAO).login("admin", "admin123");
    }

    // ── isLoggedIn / getCurrentUser ───────────────────────────

    @Test
    @DisplayName("isLoggedIn is false before login")
    void testIsLoggedIn_beforeLogin() {
        assertFalse(AuthService.isLoggedIn());
        assertNull(AuthService.getCurrentUser());
    }

    @Test
    @DisplayName("isLoggedIn is true after successful login")
    void testIsLoggedIn_afterLogin() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);
        authService.login("admin", "admin123");

        assertTrue(AuthService.isLoggedIn());
        assertNotNull(AuthService.getCurrentUser());
    }

    @Test
    @DisplayName("isLoggedIn is false after logout")
    void testIsLoggedIn_afterLogout() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);
        authService.login("admin", "admin123");
        authService.logout();

        assertFalse(AuthService.isLoggedIn());
        assertNull(AuthService.getCurrentUser());
    }

    // ── isAdmin / isManagerOrAdmin ────────────────────────────

    @Test
    @DisplayName("isAdmin returns true when ADMIN is logged in")
    void testIsAdmin_withAdminLoggedIn() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);
        authService.login("admin", "admin123");

        assertTrue(AuthService.isAdmin());
    }

    @Test
    @DisplayName("isAdmin returns false when MANAGER is logged in")
    void testIsAdmin_withManagerLoggedIn() {
        when(userDAO.login("manager", "mgr123")).thenReturn(managerUser);
        authService.login("manager", "mgr123");

        assertFalse(AuthService.isAdmin());
    }

    @Test
    @DisplayName("isAdmin returns false when not logged in")
    void testIsAdmin_notLoggedIn() {
        assertFalse(AuthService.isAdmin());
    }

    @Test
    @DisplayName("isManagerOrAdmin returns true for ADMIN")
    void testIsManagerOrAdmin_admin() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);
        authService.login("admin", "admin123");

        assertTrue(AuthService.isManagerOrAdmin());
    }

    @Test
    @DisplayName("isManagerOrAdmin returns true for MANAGER")
    void testIsManagerOrAdmin_manager() {
        when(userDAO.login("manager", "mgr123")).thenReturn(managerUser);
        authService.login("manager", "mgr123");

        assertTrue(AuthService.isManagerOrAdmin());
    }

    @Test
    @DisplayName("isManagerOrAdmin returns false for STAFF")
    void testIsManagerOrAdmin_staff() {
        when(userDAO.login("staff1", "staff123")).thenReturn(staffUser);
        authService.login("staff1", "staff123");

        assertFalse(AuthService.isManagerOrAdmin());
    }

    // ── canAccessWarehouse ────────────────────────────────────

    @Test
    @DisplayName("canAccessWarehouse returns false when not logged in")
    void testCanAccessWarehouse_notLoggedIn() {
        assertFalse(AuthService.canAccessWarehouse(1));
    }

    @Test
    @DisplayName("ADMIN can access any warehouse")
    void testCanAccessWarehouse_admin() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);
        authService.login("admin", "admin123");

        assertTrue(AuthService.canAccessWarehouse(1));
        assertTrue(AuthService.canAccessWarehouse(99));
    }

    @Test
    @DisplayName("STAFF can only access their own warehouse")
    void testCanAccessWarehouse_staff_own() {
        when(userDAO.login("staff1", "staff123")).thenReturn(staffUser);
        authService.login("staff1", "staff123");

        assertTrue(AuthService.canAccessWarehouse(3));   // own warehouse
        assertFalse(AuthService.canAccessWarehouse(1));  // another warehouse
    }

    // ── getCurrentWarehouseId ─────────────────────────────────

    @Test
    @DisplayName("getCurrentWarehouseId returns -1 when not logged in")
    void testGetCurrentWarehouseId_notLoggedIn() {
        assertEquals(-1, AuthService.getCurrentWarehouseId());
    }

    @Test
    @DisplayName("getCurrentWarehouseId returns logged-in user's warehouse id")
    void testGetCurrentWarehouseId_loggedIn() {
        when(userDAO.login("staff1", "staff123")).thenReturn(staffUser);
        authService.login("staff1", "staff123");

        assertEquals(3, AuthService.getCurrentWarehouseId());
    }

    // ── isValidPassword ───────────────────────────────────────

    @Test
    @DisplayName("isValidPassword returns true for password >= 6 chars")
    void testIsValidPassword_valid() {
        assertTrue(authService.isValidPassword("secure1"));
        assertTrue(authService.isValidPassword("123456"));
        assertTrue(authService.isValidPassword("a_very_long_password_1234"));
    }

    @Test
    @DisplayName("isValidPassword returns false for password < 6 chars")
    void testIsValidPassword_tooShort() {
        assertFalse(authService.isValidPassword("12345"));
        assertFalse(authService.isValidPassword("ab"));
        assertFalse(authService.isValidPassword(""));
    }

    @Test
    @DisplayName("isValidPassword returns false for null password")
    void testIsValidPassword_null() {
        assertFalse(authService.isValidPassword(null));
    }

    @Test
    @DisplayName("isValidPassword accepts exactly 6 characters")
    void testIsValidPassword_exactlySixChars() {
        assertTrue(authService.isValidPassword("abcdef"));
    }

    // ── changePassword ────────────────────────────────────────

    @Test
    @DisplayName("changePassword returns false when not logged in")
    void testChangePassword_notLoggedIn() {
        assertFalse(authService.changePassword("old", "newpass1"));
    }

    @Test
    @DisplayName("changePassword returns false for wrong old password")
    void testChangePassword_wrongOldPassword() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);
        authService.login("admin", "admin123");

        assertFalse(authService.changePassword("wrongold", "newpass1"));
    }

    @Test
    @DisplayName("changePassword returns false for invalid new password")
    void testChangePassword_invalidNewPassword() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);
        authService.login("admin", "admin123");

        assertFalse(authService.changePassword("admin123", "short"));
    }

    @Test
    @DisplayName("changePassword succeeds with correct old password and valid new password")
    void testChangePassword_success() {
        when(userDAO.login("admin", "admin123")).thenReturn(adminUser);
        when(userDAO.updatePassword(1, "newpassword")).thenReturn(true);
        authService.login("admin", "admin123");

        boolean result = authService.changePassword("admin123", "newpassword");

        assertTrue(result);
        assertEquals("newpassword", AuthService.getCurrentUser().getPassword());
    }
}

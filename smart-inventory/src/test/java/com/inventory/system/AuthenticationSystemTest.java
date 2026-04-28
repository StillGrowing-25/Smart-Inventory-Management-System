package com.inventory.system;

import com.inventory.dao.UserDAO;
import com.inventory.model.User;
import com.inventory.service.AuthService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * System-level tests for the Authentication workflow.
 *
 * <p>Validates end-to-end login/logout/authorization flows
 * as a complete user-facing scenario rather than isolated units.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication System Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationSystemTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private AuthService authService;

    private User admin;
    private User manager;
    private User staff;

    @BeforeEach
    void setUp() {
        admin   = new User(1, "alice",   "pass123",  "Alice Admin",   "ADMIN",   0);
        manager = new User(2, "bob",     "mgr456",   "Bob Manager",   "MANAGER", 2);
        staff   = new User(3, "charlie", "staff789", "Charlie Staff", "STAFF",   3);
        authService.logout(); // always start clean
    }

    @AfterEach
    void tearDown() {
        authService.logout();
    }

    // ── Scenario 1: Full Admin Login/Logout Cycle ─────────────

    @Test
    @Order(1)
    @DisplayName("SYSTEM: Admin can login, perform privileged checks, then logout")
    void scenario_adminLoginLogoutCycle() {
        // Step 1: System starts with no one logged in
        assertFalse(AuthService.isLoggedIn(), "No user should be logged in initially");
        assertFalse(AuthService.isAdmin());

        // Step 2: Admin logs in
        when(userDAO.login("alice", "pass123")).thenReturn(admin);
        User result = authService.login("alice", "pass123");

        assertNotNull(result, "Login should succeed for valid admin");
        assertTrue(AuthService.isLoggedIn());
        assertTrue(AuthService.isAdmin());
        assertTrue(AuthService.isManagerOrAdmin());

        // Step 3: Admin can access any warehouse
        assertTrue(AuthService.canAccessWarehouse(1));
        assertTrue(AuthService.canAccessWarehouse(5));
        assertTrue(AuthService.canAccessWarehouse(99));

        // Step 4: Admin logs out
        authService.logout();

        assertFalse(AuthService.isLoggedIn(), "User should be logged out");
        assertNull(AuthService.getCurrentUser());
        assertFalse(AuthService.isAdmin());
        assertEquals(-1, AuthService.getCurrentWarehouseId());
    }

    // ── Scenario 2: Staff Warehouse Restriction ───────────────

    @Test
    @Order(2)
    @DisplayName("SYSTEM: Staff access is restricted to their assigned warehouse only")
    void scenario_staffWarehouseRestriction() {
        // Staff logs in
        when(userDAO.login("charlie", "staff789")).thenReturn(staff);
        authService.login("charlie", "staff789");

        assertTrue(AuthService.isLoggedIn());
        assertFalse(AuthService.isAdmin());
        assertFalse(AuthService.isManagerOrAdmin());

        // Staff can only access their own warehouse (id=3)
        assertTrue(AuthService.canAccessWarehouse(3),  "Staff should access own warehouse");
        assertFalse(AuthService.canAccessWarehouse(1), "Staff cannot access warehouse 1");
        assertFalse(AuthService.canAccessWarehouse(2), "Staff cannot access warehouse 2");

        // getCurrentWarehouseId returns correct id
        assertEquals(3, AuthService.getCurrentWarehouseId());
    }

    // ── Scenario 3: Failed Login Does Not Change State ────────

    @Test
    @Order(3)
    @DisplayName("SYSTEM: Failed login attempts leave system unauthenticated")
    void scenario_failedLoginLeavesSystemClean() {
        when(userDAO.login("alice", "wrongpass")).thenReturn(null);

        // Multiple failed attempts
        assertNull(authService.login("alice", "wrongpass"));
        assertNull(authService.login(null, "pass123"));
        assertNull(authService.login("alice", ""));
        assertNull(authService.login("", "pass123"));

        // System must remain unauthenticated throughout
        assertFalse(AuthService.isLoggedIn());
        assertNull(AuthService.getCurrentUser());
        assertFalse(AuthService.isAdmin());
        assertEquals(-1, AuthService.getCurrentWarehouseId());
    }

    // ── Scenario 4: Session Switch (Admin → Manager) ──────────

    @Test
    @Order(4)
    @DisplayName("SYSTEM: Logging in a second user replaces the first session")
    void scenario_sessionReplacement() {
        // Admin logs in
        when(userDAO.login("alice", "pass123")).thenReturn(admin);
        authService.login("alice", "pass123");
        assertTrue(AuthService.isAdmin());

        // Admin logs out; manager logs in
        authService.logout();
        when(userDAO.login("bob", "mgr456")).thenReturn(manager);
        authService.login("bob", "mgr456");

        // Should now reflect manager session
        assertFalse(AuthService.isAdmin(),         "Should no longer be admin");
        assertTrue(AuthService.isManagerOrAdmin(), "Manager should pass managerOrAdmin check");
        assertEquals("Bob Manager", AuthService.getCurrentUser().getFullName());
        assertEquals(2, AuthService.getCurrentWarehouseId());

        // Manager can access all warehouses
        assertTrue(AuthService.canAccessWarehouse(1));
        assertTrue(AuthService.canAccessWarehouse(99));
    }

    // ── Scenario 5: Password Change Flow ─────────────────────

    @Test
    @Order(5)
    @DisplayName("SYSTEM: Admin can change password through the complete change-password flow")
    void scenario_passwordChange() {
        when(userDAO.login("alice", "pass123")).thenReturn(admin);
        when(userDAO.updatePassword(1, "newsecure99")).thenReturn(true);
        authService.login("alice", "pass123");

        // Attempt with wrong old password — should fail
        assertFalse(authService.changePassword("wrongold", "newsecure99"),
                "Change should fail for wrong current password");

        // Attempt with new password too short — should fail
        assertFalse(authService.changePassword("pass123", "short"),
                "Change should fail for invalid new password");

        // Valid change
        assertTrue(authService.changePassword("pass123", "newsecure99"),
                "Change should succeed with correct old password and valid new password");

        // Session should still be active after password change
        assertTrue(AuthService.isLoggedIn());
        assertEquals("newsecure99", AuthService.getCurrentUser().getPassword());
    }

    // ── Scenario 6: Unauthenticated Access Guards ─────────────

    @Test
    @Order(6)
    @DisplayName("SYSTEM: All privileged checks deny access when no user is logged in")
    void scenario_unauthenticatedAccessGuards() {
        // No one is logged in
        assertFalse(AuthService.isLoggedIn());

        // All privilege checks must deny
        assertFalse(AuthService.isAdmin());
        assertFalse(AuthService.isManagerOrAdmin());
        assertFalse(AuthService.canAccessWarehouse(1));
        assertFalse(AuthService.canAccessWarehouse(0));
        assertEquals(-1, AuthService.getCurrentWarehouseId());

        // changePassword must fail
        assertFalse(authService.changePassword("any", "newpassword"));
    }
}

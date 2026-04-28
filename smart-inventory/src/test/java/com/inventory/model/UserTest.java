package com.inventory.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Model Unit Tests")
public class UserTest {

    // ── Role Checks ──────────────────────────────────────────

    @Test
    @DisplayName("isAdmin returns true for ADMIN role")
    void testIsAdmin_withAdminRole() {
        User admin = new User(1, "admin", "pass", "Alice", "ADMIN", 0);
        assertTrue(admin.isAdmin());
    }

    @Test
    @DisplayName("isAdmin returns false for non-ADMIN role")
    void testIsAdmin_withNonAdminRole() {
        User manager = new User(2, "mgr", "pass", "Bob", "MANAGER", 1);
        assertFalse(manager.isAdmin());
    }

    @Test
    @DisplayName("isManager returns true for MANAGER role")
    void testIsManager_withManagerRole() {
        User manager = new User(2, "mgr", "pass", "Bob", "MANAGER", 1);
        assertTrue(manager.isManager());
    }

    @Test
    @DisplayName("isManager returns false for ADMIN role")
    void testIsManager_withAdminRole() {
        User admin = new User(1, "admin", "pass", "Alice", "ADMIN", 0);
        assertFalse(admin.isManager());
    }

    @Test
    @DisplayName("isStaff returns true for STAFF role")
    void testIsStaff_withStaffRole() {
        User staff = new User(3, "staff1", "pass", "Charlie", "STAFF", 2);
        assertTrue(staff.isStaff());
    }

    @Test
    @DisplayName("isStaff returns false for ADMIN role")
    void testIsStaff_withAdminRole() {
        User admin = new User(1, "admin", "pass", "Alice", "ADMIN", 0);
        assertFalse(admin.isStaff());
    }

    // ── Warehouse Access ─────────────────────────────────────

    @Test
    @DisplayName("ADMIN can access all warehouses")
    void testCanAccessAllWarehouses_admin() {
        User admin = new User(1, "admin", "pass", "Alice", "ADMIN", 0);
        assertTrue(admin.canAccessAllWarehouses());
    }

    @Test
    @DisplayName("MANAGER can access all warehouses")
    void testCanAccessAllWarehouses_manager() {
        User manager = new User(2, "mgr", "pass", "Bob", "MANAGER", 1);
        assertTrue(manager.canAccessAllWarehouses());
    }

    @Test
    @DisplayName("STAFF cannot access all warehouses")
    void testCanAccessAllWarehouses_staff() {
        User staff = new User(3, "staff1", "pass", "Charlie", "STAFF", 2);
        assertFalse(staff.canAccessAllWarehouses());
    }

    // ── Getters & Setters ─────────────────────────────────────

    @Test
    @DisplayName("All-args constructor sets all fields correctly")
    void testAllArgsConstructor() {
        User user = new User(5, "john", "secret", "John Doe", "STAFF", 3);
        assertEquals(5,          user.getId());
        assertEquals("john",     user.getUsername());
        assertEquals("secret",   user.getPassword());
        assertEquals("John Doe", user.getFullName());
        assertEquals("STAFF",    user.getRole());
        assertEquals(3,          user.getWarehouseId());
    }

    @Test
    @DisplayName("setters update user fields correctly")
    void testSetters() {
        User user = new User();
        user.setId(10);
        user.setUsername("newuser");
        user.setPassword("newpass");
        user.setFullName("New User");
        user.setRole("MANAGER");
        user.setWarehouseId(5);

        assertEquals(10,        user.getId());
        assertEquals("newuser", user.getUsername());
        assertEquals("newpass", user.getPassword());
        assertEquals("New User",user.getFullName());
        assertEquals("MANAGER", user.getRole());
        assertEquals(5,         user.getWarehouseId());
    }

    // ── toString ─────────────────────────────────────────────

    @Test
    @DisplayName("toString returns fullName with role in brackets")
    void testToString() {
        User user = new User(1, "admin", "pass", "Alice Smith", "ADMIN", 0);
        assertEquals("Alice Smith (ADMIN)", user.toString());
    }

    // ── Role Boundary Cases ───────────────────────────────────

    @Test
    @DisplayName("Unknown role returns false for all role checks")
    void testUnknownRole() {
        User guest = new User(9, "guest", "pass", "Guest User", "GUEST", 1);
        assertFalse(guest.isAdmin());
        assertFalse(guest.isManager());
        assertFalse(guest.isStaff());
        assertFalse(guest.canAccessAllWarehouses());
    }

    @Test
    @DisplayName("null role returns false for all role checks without NPE")
    void testNullRole() {
        User user = new User();
        user.setRole(null);
        assertFalse(user.isAdmin());
        assertFalse(user.isManager());
        assertFalse(user.isStaff());
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "Admin", "administrator"})
    @DisplayName("Role check is case-sensitive — only 'ADMIN' matches")
    void testRoleCaseSensitivity(String role) {
        User user = new User();
        user.setRole(role);
        assertFalse(user.isAdmin(),
                "Expected isAdmin() to be false for role: " + role);
    }
}

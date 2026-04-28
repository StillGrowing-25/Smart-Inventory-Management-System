package com.inventory.service;

import com.inventory.dao.UserDAO;
import com.inventory.model.User;

public class AuthService {

    private UserDAO userDAO = new UserDAO();
    private static User currentUser = null;

    // Login — returns user if successful, null if failed
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return null;
        }

        User user = userDAO.login(username.trim(), password.trim());
        if (user != null) {
            currentUser = user;
            System.out.println("Login successful: " + user.getFullName() +
                               " (" + user.getRole() + ")");
        }
        return user;
    }

    // Logout
    public void logout() {
        System.out.println("User logged out: " +
                (currentUser != null ? currentUser.getUsername() : "unknown"));
        currentUser = null;
    }

    // Get currently logged in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Check if user is logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Check if current user can access a warehouse
    public static boolean canAccessWarehouse(int warehouseId) {
        if (currentUser == null) return false;
        if (currentUser.canAccessAllWarehouses()) return true;
        return currentUser.getWarehouseId() == warehouseId;
    }

    // Check if current user is admin
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    // Check if current user is manager or admin
    public static boolean isManagerOrAdmin() {
        return currentUser != null &&
               (currentUser.isAdmin() || currentUser.isManager());
    }

    // Get warehouse ID for current user
    public static int getCurrentWarehouseId() {
        if (currentUser == null) return -1;
        return currentUser.getWarehouseId();
    }

    // Validate password strength
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Change password
    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) return false;
        if (!currentUser.getPassword().equals(oldPassword)) return false;
        if (!isValidPassword(newPassword)) return false;

        boolean success = userDAO.updatePassword(
                currentUser.getId(), newPassword);
        if (success) {
            currentUser.setPassword(newPassword);
        }
        return success;
    }
}
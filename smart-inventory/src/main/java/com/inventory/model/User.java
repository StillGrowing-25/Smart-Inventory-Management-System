package com.inventory.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private int warehouseId;

    public User() {}

    public User(int id, String username, String password,
                String fullName, String role,
                int warehouseId) {
        this.id          = id;
        this.username    = username;
        this.password    = password;
        this.fullName    = fullName;
        this.role        = role;
        this.warehouseId = warehouseId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String u) {
        this.username = u; }

    public String getPassword() { return password; }
    public void setPassword(String p) {
        this.password = p; }

    public String getFullName() { return fullName; }
    public void setFullName(String n) {
        this.fullName = n; }

    public String getRole() { return role; }
    public void setRole(String r) { this.role = r; }

    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int w) {
        this.warehouseId = w; }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isManager() {
        return "MANAGER".equals(role);
    }

    public boolean isStaff() {
        return "STAFF".equals(role);
    }

    // FIXED — only ADMIN sees all warehouses
    // MANAGER and STAFF only see their own
    public boolean canAccessAllWarehouses() {
        return isAdmin();
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
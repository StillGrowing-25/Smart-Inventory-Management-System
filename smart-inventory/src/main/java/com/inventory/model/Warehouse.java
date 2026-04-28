package com.inventory.model;

public class Warehouse {
    private int id;
    private String name;
    private String location;
    private String managerName;

    public Warehouse() {}

    public Warehouse(int id, String name, String location, String managerName) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.managerName = managerName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    @Override
    public String toString() {
        return name + " - " + location;
    }
}
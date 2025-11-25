package edu.univ.erp.domain;

public class InstructorOption {
    private int userId;
    private String username;
    private String department;

    public InstructorOption(int userId, String username, String department) {
        this.userId = userId;
        this.username = username;
        this.department = department;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getDepartment() { return department; }

    @Override
    public String toString() {
        return username + " (" + department + ")";
    }
}


package edu.univ.erp.auth;

import edu.univ.erp.domain.Role;

public class SessionContext {
    private final int userId;
    private final String username;
    private final Role role;

    public SessionContext(int userId, String username, Role role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }

    public boolean isAdmin()      { return role == Role.ADMIN; }
    public boolean isInstructor() { return role == Role.INSTRUCTOR; }
    public boolean isStudent()    { return role == Role.STUDENT; }
}


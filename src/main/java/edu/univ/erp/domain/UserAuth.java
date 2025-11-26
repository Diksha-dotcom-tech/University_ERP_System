package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class UserAuth {
    private int userId;
    private String username;
    private Role role;
    private String passwordHash;
    private String status;
    private int failedAttempts;
    private LocalDateTime lastLogin;
    private LocalDateTime lockUntil;   // NEW: for 30s lock

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getLockUntil() { return lockUntil; }
    public void setLockUntil(LocalDateTime lockUntil) { this.lockUntil = lockUntil; }
}

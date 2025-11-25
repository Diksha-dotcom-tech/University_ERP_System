package edu.univ.erp.api.auth;

import edu.univ.erp.auth.AuthException;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.domain.Role;
import edu.univ.erp.util.DbUtil;

import java.sql.*;

public class AuthApi {

    /**
     * Login: returns SessionContext or throws AuthException.
     */
    public SessionContext login(String username, Role role, String plainPassword) throws AuthException {
        String sql = """
                SELECT user_id, password_hash, status, failed_attempts
                FROM users_auth
                WHERE username = ? AND role = ?
                """;

        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, role.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AuthException("Incorrect username or password.");
                }

                int userId = rs.getInt("user_id");
                String hash = rs.getString("password_hash");
                String status = rs.getString("status");
                int failedAttempts = rs.getInt("failed_attempts");

                if ("LOCKED".equals(status)) {
                    throw new AuthException("Account locked after too many failed attempts.");
                }

                boolean ok = PasswordHasher.verify(plainPassword, hash);
                if (!ok) {
                    incrementFailedAttempts(userId, failedAttempts + 1);
                    throw new AuthException("Incorrect username or password.");
                }

                resetFailedAttempts(userId);

                // update last_login
                try (PreparedStatement upd = conn.prepareStatement(
                        "UPDATE users_auth SET last_login = NOW() WHERE user_id = ?")) {
                    upd.setInt(1, userId);
                    upd.executeUpdate();
                }

                return new SessionContext(userId, username, role);
            }

        } catch (SQLException e) {
            throw new AuthException("Login failed: " + e.getMessage(), e);
        }
    }

    private void incrementFailedAttempts(int userId, int newValue) throws SQLException {
        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users_auth SET failed_attempts = ?, status = ? WHERE user_id = ?")) {
            String status = newValue >= 5 ? "LOCKED" : "ACTIVE";
            ps.setInt(1, newValue);
            ps.setString(2, status);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    private void resetFailedAttempts(int userId) throws SQLException {
        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users_auth SET failed_attempts = 0, status = 'ACTIVE' WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Change password for the current user.
     * Checks old password using bcrypt, then stores new hash.
     */
    public void changePassword(SessionContext session,
                               String oldPassword,
                               String newPassword) throws AuthException {

        if (session == null) {
            throw new AuthException("No active session.");
        }

        String selectSql = "SELECT password_hash FROM users_auth WHERE user_id = ?";
        String updateSql = "UPDATE users_auth SET password_hash = ?, failed_attempts = 0 WHERE user_id = ?";

        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement sel = conn.prepareStatement(selectSql)) {

            sel.setInt(1, session.getUserId());
            try (ResultSet rs = sel.executeQuery()) {
                if (!rs.next()) {
                    throw new AuthException("User not found.");
                }
                String currentHash = rs.getString(1);
                if (!PasswordHasher.verify(oldPassword, currentHash)) {
                    throw new AuthException("Current password is incorrect.");
                }
            }

            String newHash = PasswordHasher.hash(newPassword);
            try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                upd.setString(1, newHash);
                upd.setInt(2, session.getUserId());
                upd.executeUpdate();
            }

        } catch (SQLException e) {
            throw new AuthException("Could not change password: " + e.getMessage(), e);
        }
    }
}


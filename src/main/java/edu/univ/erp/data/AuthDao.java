package edu.univ.erp.data;

import edu.univ.erp.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO for the univ_auth.users_auth table.
 * Used by AuthService for login, lockout, password change, etc.
 */
public class AuthDao {

    // Simple record to hold auth user row
    public record AuthUser(
            int userId,
            String username,
            String role,
            String passwordHash,
            String status,
            int failedAttempts
    ) {}

    public AuthUser findByUsername(String username) throws Exception {
        String sql = """
            SELECT user_id, username, role, password_hash, status, failed_attempts
            FROM users_auth
            WHERE username = ?
        """;

        try (Connection c = DbUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AuthUser(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"),
                        rs.getString("status"),
                        rs.getInt("failed_attempts")
                );
            }
        }
    }

    public AuthUser findById(int userId) throws Exception {
        String sql = """
            SELECT user_id, username, role, password_hash, status, failed_attempts
            FROM users_auth
            WHERE user_id = ?
        """;

        try (Connection c = DbUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AuthUser(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"),
                        rs.getString("status"),
                        rs.getInt("failed_attempts")
                );
            }
        }
    }

    public void incrementFailedAttempts(int userId) throws Exception {
        String sql = """
            UPDATE users_auth
            SET failed_attempts = failed_attempts + 1,
                status = CASE
                           WHEN failed_attempts + 1 >= 5 THEN 'LOCKED'
                           ELSE status
                         END
            WHERE user_id = ?
        """;

        try (Connection c = DbUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void resetFailedAttempts(int userId) throws Exception {
        String sql = "UPDATE users_auth SET failed_attempts = 0, status = 'ACTIVE' WHERE user_id = ?";

        try (Connection c = DbUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void updateLastLogin(int userId) throws Exception {
        String sql = "UPDATE users_auth SET last_login = NOW() WHERE user_id = ?";

        try (Connection c = DbUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public int insertUser(String username, String role, String passwordHash) throws Exception {
        String sql = """
            INSERT INTO users_auth(username, role, password_hash, status)
            VALUES (?, ?, ?, 'ACTIVE')
        """;

        try (Connection c = DbUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, role);
            ps.setString(3, passwordHash);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new IllegalStateException("Failed to obtain generated user_id");
    }

    public void updatePassword(int userId, String newHash) throws Exception {
        String sql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";

        try (Connection c = DbUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}

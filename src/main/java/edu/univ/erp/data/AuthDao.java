package edu.univ.erp.data;

import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;

public class AuthDao {

    private static final String SELECT_BY_USERNAME = """
            SELECT user_id,
                   username,
                   role,
                   password_hash,
                   status,
                   failed_attempts,
                   last_login,
                   lock_until
            FROM users_auth
            WHERE username = ?
            """;

    private static final String UPDATE_FAILED_SQL = """
            UPDATE users_auth
            SET failed_attempts = ?, lock_until = ?
            WHERE user_id = ?
            """;

    private static final String RESET_FAILED_SQL = """
            UPDATE users_auth
            SET failed_attempts = 0, lock_until = NULL
            WHERE user_id = ?
            """;

    private static final String UPDATE_LAST_LOGIN_SQL =
            "UPDATE users_auth SET last_login = NOW() WHERE user_id = ?";

    public UserAuth getUserAuth(String username) throws SQLException {
        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_USERNAME)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UserAuth user = new UserAuth();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setStatus(rs.getString("status"));
                user.setFailedAttempts(rs.getInt("failed_attempts"));

                Timestamp lastLoginTs = rs.getTimestamp("last_login");
                if (lastLoginTs != null) {
                    user.setLastLogin(lastLoginTs.toLocalDateTime());
                }

                Timestamp lockTs = rs.getTimestamp("lock_until");
                if (lockTs != null) {
                    user.setLockUntil(lockTs.toLocalDateTime());
                }

                return user;
            }
        }
    }

    public void updateFailedAttempts(int userId, int failedAttempts, LocalDateTime lockUntil)
            throws SQLException {

        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_FAILED_SQL)) {

            ps.setInt(1, failedAttempts);
            if (lockUntil == null) {
                ps.setNull(2, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(2, Timestamp.valueOf(lockUntil));
            }
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void resetFailedAttempts(int userId) throws SQLException {
        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(RESET_FAILED_SQL)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void updateLastLogin(int userId) throws SQLException {
        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_LAST_LOGIN_SQL)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}

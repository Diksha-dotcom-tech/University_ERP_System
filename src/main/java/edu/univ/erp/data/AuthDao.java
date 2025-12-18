//package edu.univ.erp.data;
//
//import edu.univ.erp.domain.UserAuth;
//import edu.univ.erp.util.DbUtil;
//
//import java.sql.*;
//import java.time.LocalDateTime;
//
//public class AuthDao {
//
//    private static final String GET_USER_AUTH_SQL =
//            "SELECT user_id, username, role, password_hash, status, " +
//                    "       failed_attempts, last_login, lock_until, logged_in " +
//                    "FROM users_auth WHERE username = ?";
//
//    public UserAuth getUserAuth(String username) throws SQLException {
//        try (Connection conn = DbUtil.getAuthConnection();
//             PreparedStatement pstmt = conn.prepareStatement(GET_USER_AUTH_SQL)) {
//
//            pstmt.setString(1, username);
//
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (!rs.next()) return null;
//
//                UserAuth user = new UserAuth();
//                user.setUserId(rs.getInt("user_id"));
//                user.setUsername(rs.getString("username"));
//                user.setRole(rs.getString("role"));
//                user.setPasswordHash(rs.getString("password_hash"));
//                user.setStatus(rs.getString("status"));
//                user.setFailedAttempts(rs.getInt("failed_attempts"));
//
//                Timestamp lastLoginTs = rs.getTimestamp("last_login");
//                if (lastLoginTs != null) user.setLastLogin(lastLoginTs.toLocalDateTime());
//
//                Timestamp lockUntilTs = rs.getTimestamp("lock_until");
//                if (lockUntilTs != null) user.setLockUntil(lockUntilTs.toLocalDateTime());
//
//                int loggedInInt = rs.getInt("logged_in");
//                user.setLoggedIn(loggedInInt == 1);
//
//                return user;
//            }
//        }
//    }
//
//    /**
//     * Atomically mark login success:
//     * - only sets logged_in = 1 when the current flag is 0 or NULL (prevents overwriting an existing active session).
//     * - resets failed_attempts and lock_until, sets last_login = NOW()
//     *
//     * Returns true if the update actually affected a row (i.e., we claimed the login flag),
//     * false if the WHERE clause prevented the update because logged_in was already 1.
//     */
//    public boolean markLoginSuccess(int userId) throws SQLException {
//        String sql = """
//                UPDATE users_auth
//                SET failed_attempts = 0,
//                    lock_until      = NULL,
//                    last_login      = NOW(),
//                    logged_in       = 1
//                WHERE user_id = ?
//                  AND (logged_in = 0 OR logged_in IS NULL)
//                """;
//
//        try (Connection conn = DbUtil.getAuthConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            int updated = ps.executeUpdate();
//            return updated == 1;
//        }
//    }
//
//    public void clearLoginFlag(int userId) throws SQLException {
//        String sql = "UPDATE users_auth SET logged_in = 0 WHERE user_id = ?";
//
//        try (Connection conn = DbUtil.getAuthConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            ps.executeUpdate();
//        }
//    }
//
//    public void updateFailedLogin(int userId, int newFailedAttempts, LocalDateTime lockUntil) throws SQLException {
//        String sql = """
//                UPDATE users_auth
//                SET failed_attempts = ?,
//                    lock_until      = ?
//                WHERE user_id = ?
//                """;
//
//        try (Connection conn = DbUtil.getAuthConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setInt(1, newFailedAttempts);
//
//            if (lockUntil != null) {
//                ps.setTimestamp(2, Timestamp.valueOf(lockUntil));
//            } else {
//                ps.setNull(2, Types.TIMESTAMP);
//            }
//
//            ps.setInt(3, userId);
//            ps.executeUpdate();
//        }
//    }
//}

package edu.univ.erp.data;

import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;

public class AuthDao {

    // Read everything we need, including failed_attempts, lock_until, logged_in
    private static final String GET_USER_AUTH_SQL =
            "SELECT user_id, username, role, password_hash, status, " +
                    "       failed_attempts, last_login, lock_until, logged_in " +
                    "FROM users_auth WHERE username = ?";

    public UserAuth getUserAuth(String username) throws SQLException {
        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(GET_USER_AUTH_SQL)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

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

                Timestamp lockUntilTs = rs.getTimestamp("lock_until");
                if (lockUntilTs != null) {
                    user.setLockUntil(lockUntilTs.toLocalDateTime());
                }

                user.setLoggedIn(rs.getInt("logged_in") == 1);

                return user;
            }
        }
    }

    /**
     * Called after a SUCCESSFUL login:
     * - resets failed_attempts
     * - clears lock_until
     * - sets last_login = NOW()
     * - sets logged_in = 1 (prevents other terminals from logging in)
     */
    public void markLoginSuccess(int userId) throws SQLException {
        String sql = """
                UPDATE users_auth
                SET failed_attempts = 0,
                    lock_until      = NULL,
                    last_login      = NOW(),
                    logged_in       = 1
                WHERE user_id = ?
                """;

        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Called on logout to allow login from other terminals.
     */
    public void clearLoginFlag(int userId) throws SQLException {
        String sql = "UPDATE users_auth SET logged_in = 0 WHERE user_id = ?";

        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Called on FAILED login for an existing username.
     */
    public void updateFailedLogin(int userId, int newFailedAttempts, LocalDateTime lockUntil) throws SQLException {
        String sql = """
                UPDATE users_auth
                SET failed_attempts = ?,
                    lock_until      = ?
                WHERE user_id = ?
                """;

        try (Connection conn = DbUtil.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newFailedAttempts);

            if (lockUntil != null) {
                ps.setTimestamp(2, Timestamp.valueOf(lockUntil));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }

            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }
}
